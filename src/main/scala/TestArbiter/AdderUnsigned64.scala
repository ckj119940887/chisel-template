package TestArbiter

import chisel3._
import chisel3.util._
import chisel3.experimental._


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

class AdderUnsigned64(val width: Int = 64) extends Module {
    val io = IO(new Bundle{
        val a = Input(UInt(width.W))
        val b = Input(UInt(width.W))
        val start = Input(Bool())
        val out = Output(UInt(width.W))
        val valid = Output(Bool())
    })
  val adder = Module(new XilinxAdderUnsigned64Wrapper)
  adder.io.clk := clock.asBool
  adder.io.A := RegNext(io.a)
  adder.io.B := RegNext(io.b)
  adder.io.ce := RegNext(io.start)
  io.valid := RegNext(adder.io.valid)
  io.out := RegNext(adder.io.S)
}


class AdderUnsigned64RequestBundle(dataWidth: Int) extends Bundle {
  val a = UInt(dataWidth.W)
  val b = UInt(dataWidth.W)
}


class AdderUnsigned64ResponseBundle(dataWidth: Int) extends Bundle {
  val out = UInt(dataWidth.W)
}


class AdderUnsigned64IO(dataWidth: Int) extends Bundle {
  val req = Valid(new AdderUnsigned64RequestBundle(dataWidth))
  val resp = Flipped(Valid(new AdderUnsigned64ResponseBundle(dataWidth)))
}


class AdderUnsigned64ArbiterIO(numIPs: Int, dataWidth: Int) extends Bundle {
  val ipReqs  = Flipped(Vec(numIPs, Valid(new AdderUnsigned64RequestBundle(dataWidth))))
  val ipResps = Vec(numIPs, Valid(new AdderUnsigned64ResponseBundle(dataWidth)))
  val ip      = new AdderUnsigned64IO(dataWidth)
}


class AdderUnsigned64ArbiterModule(numIPs: Int, dataWidth: Int) extends Module {
  val io = IO(new AdderUnsigned64ArbiterIO(numIPs, dataWidth))

  // ------------------ Stage 0: Input Cache ------------------
  val r_ipReq_valid = RegInit(VecInit(Seq.fill(numIPs)(false.B)))
  val r_ipReq_valid_next = RegInit(VecInit(Seq.fill(numIPs)(false.B)))
  val r_ipReq_enable = RegInit(VecInit(Seq.fill(numIPs)(false.B)))
  val r_ipReq_bits = Reg(Vec(numIPs, new AdderUnsigned64RequestBundle(dataWidth)))

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
  val r_reqBits  = Reg(new AdderUnsigned64RequestBundle(dataWidth))
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
  val r_ipResp_bits  = Reg(Vec(numIPs, new AdderUnsigned64ResponseBundle(dataWidth)))

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


class AdderUnsigned64Wrapper(val dataWidth: Int ) extends Module {
    val io = IO(new Bundle{
        val req = Input(Valid(new AdderUnsigned64RequestBundle(dataWidth)))
        val resp = Output(Valid(new AdderUnsigned64ResponseBundle(dataWidth)))

    })

    val mod = Module(new AdderUnsigned64(dataWidth))

    val r_req            = Reg(new AdderUnsigned64RequestBundle(dataWidth))
    val r_req_valid      = RegNext(io.req.valid, false.B)


    val r_resp_data  = RegNext(mod.io.out)
    val r_resp_valid = RegNext(mod.io.valid)


    val r_mod_start = RegInit(false.B)
    when(r_req_valid) {
        r_mod_start := true.B
    } .elsewhen(r_resp_valid) {
        r_mod_start := false.B
    }


    r_req := io.req.bits


    mod.io.a := r_req.a
    mod.io.b := r_req.b
    mod.io.start := r_mod_start
    io.resp.bits.out := r_resp_data
    io.resp.valid    := r_resp_valid
}


class AdderUnsigned64FunctionModule(dataWidth: Int) extends Module{
  val io = IO(new Bundle{
    val arb_req  = Valid(new AdderUnsigned64RequestBundle(dataWidth))
    val arb_resp = Flipped(Valid(new AdderUnsigned64ResponseBundle(dataWidth)))
  })

  val r_arb_req          = Reg(new AdderUnsigned64RequestBundle(dataWidth))
  val r_arb_req_valid    = RegInit(false.B)
  val r_arb_resp         = Reg(new AdderUnsigned64ResponseBundle(dataWidth))
  val r_arb_resp_valid   = RegInit(false.B)
  r_arb_resp       := io.arb_resp.bits
  r_arb_resp_valid := io.arb_resp.valid
  io.arb_req.bits  := r_arb_req
  io.arb_req.valid := r_arb_req_valid

  val AdderUnsigned64CP            = RegInit(0.U(4.W))
  val r_res            = Reg(UInt(dataWidth.W))

  switch(AdderUnsigned64CP) {
    is(0.U) {
      r_arb_req_valid := true.B
      r_arb_req.a     := 2.U
      r_arb_req.b     := 3.U
      when(r_arb_resp_valid) {
          r_res                := r_arb_resp.out
          r_arb_req_valid := false.B
          AdderUnsigned64CP := 1.U
      }
    }
    is(1.U) {
      r_arb_req_valid := true.B
      r_arb_req.a     := 3.U
      r_arb_req.b     := 3.U
      when(r_arb_resp_valid) {
          r_res                := r_arb_resp.out
          r_arb_req_valid := false.B
          AdderUnsigned64CP := 2.U
      }
    }
    is(2.U) {
      printf("result:%d\n", r_res)
    }
  }
}

