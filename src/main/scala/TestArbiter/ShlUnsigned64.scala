package TestArbiter

import chisel3._
import chisel3.util._
import chisel3.experimental._


class ShlUnsigned64(val width: Int = 64) extends Module {
    val io = IO(new Bundle{
        val a = Input(UInt(width.W))
        val b = Input(UInt(width.W))
        val start = Input(Bool())
        val valid = Output(Bool())
        val out = Output(UInt(width.W))
    })

    val big = RegNext(io.b >= width.U)
    val sh  = RegNext(io.b(5, 0))
    val aU  = RegNext(io.a.asUInt)

    val r_busy       = RegInit(true.B)

    val v1 = RegNext(io.start, init=false.B)
    val v2 = RegNext(v1,       init=false.B)
    val v3 = RegNext(v2,       init=false.B)
    val v4 = RegNext(v3,       init=false.B)
    val v5 = RegNext(v4,       init=false.B)
    val v6 = RegNext(v5,       init=false.B)
    val v7 = RegNext(v6,       init=false.B)
    val v8 = RegNext(v7,       init=false.B)
    val v9 = RegNext(v8,       init=false.B)

    when(v8 & ~v9) {
        r_busy := false.B
    } .elsewhen(io.valid) {
        r_busy := true.B
    }

    val s1 = RegNext(Mux(sh(0), aU << 1, aU))
    val s2 = RegNext(Mux(sh(1), s1 << 2, s1))
    val s3 = RegNext(Mux(sh(2), s2 << 4, s2))
    val s4 = RegNext(Mux(sh(3), s3 << 8, s3))
    val s5 = RegNext(Mux(sh(4), s4 << 16, s4))
    val s6 = RegNext(Mux(sh(5), s5 << 32, s5))
    val s7 = RegNext(Mux(big, 0.U, s6))

    io.valid := v1 & ~r_busy
    io.out := s7
}


class ShlUnsigned64RequestBundle(dataWidth: Int) extends Bundle {
  val a = UInt(dataWidth.W)
  val b = UInt(dataWidth.W)
}


class ShlUnsigned64ResponseBundle(dataWidth: Int) extends Bundle {
  val out = UInt(dataWidth.W)
}


class ShlUnsigned64IO(dataWidth: Int) extends Bundle {
  val req = Valid(new ShlUnsigned64RequestBundle(dataWidth))
  val resp = Flipped(Valid(new ShlUnsigned64ResponseBundle(dataWidth)))
}


class ShlUnsigned64ArbiterIO(numIPs: Int, dataWidth: Int) extends Bundle {
  val ipReqs  = Flipped(Vec(numIPs, Valid(new ShlUnsigned64RequestBundle(dataWidth))))
  val ipResps = Vec(numIPs, Valid(new ShlUnsigned64ResponseBundle(dataWidth)))
  val ip      = new ShlUnsigned64IO(dataWidth)
}


class ShlUnsigned64ArbiterModule(numIPs: Int, dataWidth: Int) extends Module {
  val io = IO(new ShlUnsigned64ArbiterIO(numIPs, dataWidth))

  // ------------------ Stage 0: Input Cache ------------------
  val r_ipReq_valid = RegInit(VecInit(Seq.fill(numIPs)(false.B)))
  val r_ipReq_valid_next = RegInit(VecInit(Seq.fill(numIPs)(false.B)))
  val r_ipReq_enable = RegInit(VecInit(Seq.fill(numIPs)(false.B)))
  val r_ipReq_bits = Reg(Vec(numIPs, new ShlUnsigned64RequestBundle(dataWidth)))

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
  val r_reqBits  = Reg(new ShlUnsigned64RequestBundle(dataWidth))
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
  val r_ipResp_bits  = Reg(Vec(numIPs, new ShlUnsigned64ResponseBundle(dataWidth)))

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


class ShlUnsigned64Wrapper(val dataWidth: Int ) extends Module {
    val io = IO(new Bundle{
        val req = Input(Valid(new ShlUnsigned64RequestBundle(dataWidth)))
        val resp = Output(Valid(new ShlUnsigned64ResponseBundle(dataWidth)))

    })

    val mod = Module(new ShlUnsigned64(dataWidth))

    val r_req            = Reg(new ShlUnsigned64RequestBundle(dataWidth))
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


class ShlUnsigned64FunctionModule(dataWidth: Int) extends Module{
  val io = IO(new Bundle{
    val arb_req  = Valid(new ShlUnsigned64RequestBundle(dataWidth))
    val arb_resp = Flipped(Valid(new ShlUnsigned64ResponseBundle(dataWidth)))
  })

  val r_arb_req          = Reg(new ShlUnsigned64RequestBundle(dataWidth))
  val r_arb_req_valid    = RegInit(false.B)
  val r_arb_resp         = Reg(new ShlUnsigned64ResponseBundle(dataWidth))
  val r_arb_resp_valid   = RegInit(false.B)
  r_arb_resp       := io.arb_resp.bits
  r_arb_resp_valid := io.arb_resp.valid
  io.arb_req.bits  := r_arb_req
  io.arb_req.valid := r_arb_req_valid

  val ShlUnsigned64CP            = RegInit(0.U(4.W))
  val r_res            = Reg(UInt(dataWidth.W))

  switch(ShlUnsigned64CP) {
    is(0.U) {
      r_arb_req_valid := true.B
      r_arb_req.a     := 1.U
      r_arb_req.b     := 128.U
      when(r_arb_resp_valid) {
          r_res                := r_arb_resp.out
          r_arb_req_valid := false.B
          ShlUnsigned64CP := 1.U
      }
    }
    is(1.U) {
      printf("result:%d\n", r_res)
    }
  }
}

