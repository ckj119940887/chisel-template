package Router

import chisel3._
import chisel3.util._

class AdderRequestBundle(dataWidth: Int) extends Bundle {
  val a = UInt(dataWidth.W)
  val b = UInt(dataWidth.W)
}

class AdderResponseBundle(dataWidth: Int) extends Bundle {
  val out = UInt(dataWidth.W)
}

class AdderIO(dataWidth: Int) extends Bundle {
  val req = Valid(new AdderRequestBundle(dataWidth))
  val resp = Flipped(Valid(new AdderResponseBundle(dataWidth)))
}

class AdderArbiterIO(numIPs: Int, dataWidth: Int) extends Bundle {
  val ipReqs  = Flipped(Vec(numIPs, Valid(new AdderRequestBundle(dataWidth))))
  val ipResps = Vec(numIPs, Valid(new AdderResponseBundle(dataWidth)))
  val ip      = new AdderIO(dataWidth)
}

class AdderArbiterModule(numIPs: Int, dataWidth: Int) extends Module {
  val io = IO(new AdderArbiterIO(numIPs, dataWidth))

  // ------------------ Stage 0: Input Cache ------------------
  val r_ipReq_valid = RegInit(VecInit(Seq.fill(numIPs)(false.B)))
  val r_ipReq_valid_next = RegInit(VecInit(Seq.fill(numIPs)(false.B)))
  val r_ipReq_enable = RegInit(VecInit(Seq.fill(numIPs)(false.B)))
  val r_ipReq_bits = Reg(Vec(numIPs, new AdderRequestBundle(dataWidth)))

  for (i <- 0 until numIPs) {
    r_ipReq_valid(i) := io.ipReqs(i).valid
    r_ipReq_valid_next(i) := r_ipReq_valid(i)
    when(r_ipReq_valid(i) & ~r_ipReq_valid_next(i)) {
      r_ipReq_enable(i) := true.B
      r_ipReq_bits(i) := io.ipReqs(i).bits
    } .otherwise {
      r_ipReq_enable(i) := false.B
    }
  }

  // ------------------ Stage 1: Arbitration Decision Pipeline ------------------
  val r_foundReq = RegInit(false.B)
  val r_reqBits  = Reg(new AdderRequestBundle(dataWidth))
  val r_chosen   = Reg(UInt(log2Ceil(numIPs).W))

  r_foundReq := r_ipReq_enable.reduce(_ || _)
  for (i <- 0 until numIPs) {
    when(r_ipReq_enable(i)) {
      r_reqBits := r_ipReq_bits(i)
      r_chosen  := i.U
    }
  }

  io.ip.req.valid := r_foundReq
  io.ip.req.bits  := r_reqBits 

  // ------------------ Stage 2: memory.resp handling ------------------
  val r_mem_resp_valid = RegNext(io.ip.resp.valid)
  val r_mem_resp_bits  = RegNext(io.ip.resp.bits)
  val r_mem_resp_id    = RegNext(r_chosen)

  val r_ipResp_valid = RegInit(VecInit(Seq.fill(numIPs)(false.B)))
  val r_ipResp_bits  = Reg(Vec(numIPs, new AdderResponseBundle(dataWidth)))

  for (i <- 0 until numIPs) {
    r_ipResp_valid(i)    := false.B
    r_ipResp_bits(i).out := 0.U
  }

  when(r_mem_resp_valid) {
    r_ipResp_valid(r_mem_resp_id) := true.B
    r_ipResp_bits(r_mem_resp_id)  := r_mem_resp_bits
  } .otherwise {
    r_ipResp_valid(r_mem_resp_id) := false.B
  }

  for (i <- 0 until numIPs) {
    io.ipResps(i).valid := r_ipResp_valid(i)
    io.ipResps(i).bits  := r_ipResp_bits(i)
  }
}

class XilinxAdderUnsigned64Wrapper extends BlackBox with HasBlackBoxResource {
  val io = IO(new Bundle {
    val clk = Input(Bool())
    val ce = Input(Bool())
    val A = Input(UInt(64.W))
    val B = Input(UInt(64.W))
    val valid = Output(Bool())
    val S = Output(UInt(64.W))
  })

  addResource("/verilog/XilinxAdderUnsigned64Wrapper.v")
}

class VerilogAdder(val width: Int = 64) extends Module {
    val io = IO(new Bundle{
        val a = Input(UInt(width.W))
        val b = Input(UInt(width.W))
        val start = Input(Bool())
        val out = Output(UInt(width.W))
        val valid = Output(Bool())
    })

  val adder = Module(new XilinxAdderUnsigned64Wrapper)

  val r_start      = RegInit(false.B)
  val r_start_next = RegInit(false.B)
  val r_busy       = RegInit(false.B)
  val r_valid      = RegNext(adder.io.valid)

  r_start      := io.start
  r_start_next := r_start
  when(r_start & ~r_start_next) {
      r_busy := false.B
  } .elsewhen(r_valid) {
      r_busy := true.B
  }

  adder.io.clk := clock.asBool
  adder.io.A := RegNext(io.a)
  adder.io.B := RegNext(io.b)
  adder.io.ce := RegNext(io.start) & ~r_busy
  io.valid := RegNext(adder.io.valid)
  io.out := RegNext(adder.io.S)
}

class ChiselAdder(val width: Int = 64) extends Module {
    val io = IO(new Bundle{
        val a = Input(UInt(width.W))
        val b = Input(UInt(width.W))
        val start = Input(Bool())
        val out = Output(UInt(width.W))
        val valid = Output(Bool())
    })

    val state  = RegInit(0.U(2.W))
    val regA   = Reg(UInt(width.W))
    val regB   = Reg(UInt(width.W))
    val result = Reg(UInt(width.W))

    val r_start      = RegInit(false.B)
    val r_start_next = RegInit(false.B)
    val r_busy       = RegInit(false.B)

    r_start      := io.start
    r_start_next := r_start
    when(r_start & ~r_start_next) {
        r_busy := false.B
    } .elsewhen(io.valid) {
        r_busy := true.B
    }

    io.valid := Mux(state === 2.U, true.B, false.B)
    io.out := Mux(state === 2.U, result, 0.U)
    switch(state) {
        is(0.U) {
            state := Mux(io.start & ~r_busy, 1.U, 0.U)
            regA := Mux(io.start, io.a, regA)
            regB := Mux(io.start, io.b, regB)
        }
        is(1.U) {
            result := regA + regB
            state := 2.U
        }
        is(2.U) {
            state := 0.U
        }
    }
}

class AdderWrapper(val dataWidth: Int = 32) extends Module {
    val io = IO(new Bundle{
        val req = Input(Valid(new AdderRequestBundle(dataWidth)))
        val resp = Output(Valid(new AdderResponseBundle(dataWidth)))
    })

    val add = Module(new VerilogAdder(dataWidth))

    val r_req            = Reg(new AdderRequestBundle(dataWidth))
    val r_req_valid      = RegNext(io.req.valid, false.B)

    val r_resp_data  = RegNext(add.io.out)
    val r_resp_valid = RegNext(add.io.valid)

    val r_add_start = RegInit(false.B)

    when(r_req_valid) {
        r_add_start := true.B
    } .elsewhen(r_resp_valid) {
        r_add_start := false.B
    }

    r_req := io.req.bits

    add.io.start := r_add_start
    add.io.a     := r_req.a
    add.io.b     := r_req.b

    io.resp.valid    := r_resp_valid
    io.resp.bits.out := r_resp_data
}