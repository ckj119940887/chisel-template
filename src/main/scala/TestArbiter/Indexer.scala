package TestArbiter

import chisel3._
import chisel3.util._
import chisel3.experimental._


class Indexer(val width: Int = 16) extends Module {
    val io = IO(new Bundle{
        val baseOffset = Input(UInt(width.W))
        val dataOffset = Input(UInt(width.W))
        val index = Input(UInt(width.W))
        val elementSize = Input(UInt(width.W))
        val mask = Input(UInt(width.W))
        val ready = Input(Bool())
        val valid = Output(Bool())
        val out = Output(UInt(width.W))
    })

    val r_start      = RegInit(false.B)
    val r_start_next = RegInit(false.B)
    val r_busy       = RegInit(true.B)

    r_start      := io.ready
    r_start_next := r_start
    when(r_start & ~r_start_next) {
        r_busy := false.B
    } .elsewhen(io.valid) {
        r_busy := true.B
    }

    val stateReg = RegInit(0.U(2.W))
    switch(stateReg) {
        is(0.U) {
            stateReg := Mux(io.ready, 1.U, 0.U)
        }
        is(1.U) {
            stateReg := 2.U
        }
        is(2.U) {
            stateReg := 3.U
        }
        is(3.U) {
            stateReg := Mux(!io.ready, 0.U, 3.U)
        }
    }

    io.valid := Mux(stateReg === 3.U & ~r_busy, true.B, false.B)

    val regBaseAddr = RegNext(io.baseOffset + io.dataOffset)

    val regIndex = RegNext(io.index)
    val regMult = RegNext(regIndex * io.elementSize)

    io.out := RegNext(regBaseAddr + (regMult & io.mask))
}


class IndexerRequestBundle(dataWidth: Int) extends Bundle {
  val baseOffset = UInt(dataWidth.W)
  val dataOffset = UInt(dataWidth.W)
  val index = UInt(dataWidth.W)
  val elementSize = UInt(dataWidth.W)
  val mask = UInt(dataWidth.W)
}


class IndexerResponseBundle(dataWidth: Int) extends Bundle {
  val out = UInt(dataWidth.W)
}


class IndexerIO(dataWidth: Int) extends Bundle {
  val req = Valid(new IndexerRequestBundle(dataWidth))
  val resp = Flipped(Valid(new IndexerResponseBundle(dataWidth)))
}


class IndexerArbiterIO(numIPs: Int, dataWidth: Int) extends Bundle {
  val ipReqs  = Flipped(Vec(numIPs, Valid(new IndexerRequestBundle(dataWidth))))
  val ipResps = Vec(numIPs, Valid(new IndexerResponseBundle(dataWidth)))
  val ip      = new IndexerIO(dataWidth)
}


class IndexerArbiterModule(numIPs: Int, dataWidth: Int) extends Module {
  val io = IO(new IndexerArbiterIO(numIPs, dataWidth))

  // ------------------ Stage 0: Input Cache ------------------
  val r_ipReq_valid = RegInit(VecInit(Seq.fill(numIPs)(false.B)))
  val r_ipReq_valid_next = RegInit(VecInit(Seq.fill(numIPs)(false.B)))
  val r_ipReq_enable = RegInit(VecInit(Seq.fill(numIPs)(false.B)))
  val r_ipReq_bits = Reg(Vec(numIPs, new IndexerRequestBundle(dataWidth)))

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
  val r_reqBits  = Reg(new IndexerRequestBundle(dataWidth))
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
  val r_ipResp_bits  = Reg(Vec(numIPs, new IndexerResponseBundle(dataWidth)))

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


class IndexerWrapper(val dataWidth: Int ) extends Module {
    val io = IO(new Bundle{
        val req = Input(Valid(new IndexerRequestBundle(dataWidth)))
        val resp = Output(Valid(new IndexerResponseBundle(dataWidth)))

    })

    val mod = Module(new Indexer(dataWidth))

    val r_req            = Reg(new IndexerRequestBundle(dataWidth))
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


    mod.io.baseOffset := r_req.baseOffset
    mod.io.dataOffset := r_req.dataOffset
    mod.io.index := r_req.index
    mod.io.elementSize := r_req.elementSize
    mod.io.mask := r_req.mask
    mod.io.ready := r_mod_start
    io.resp.bits.out := r_resp_data
    io.resp.valid    := r_resp_valid
}


class IndexerFunctionModule(dataWidth: Int) extends Module{
  val io = IO(new Bundle{
    val arb_req  = Valid(new IndexerRequestBundle(dataWidth))
    val arb_resp = Flipped(Valid(new IndexerResponseBundle(dataWidth)))
  })

  val r_arb_req          = Reg(new IndexerRequestBundle(dataWidth))
  val r_arb_req_valid    = RegInit(false.B)
  val r_arb_resp         = Reg(new IndexerResponseBundle(dataWidth))
  val r_arb_resp_valid   = RegInit(false.B)
  r_arb_resp       := io.arb_resp.bits
  r_arb_resp_valid := io.arb_resp.valid
  io.arb_req.bits  := r_arb_req
  io.arb_req.valid := r_arb_req_valid

  val IndexerCP            = RegInit(0.U(4.W))
  val r_res            = Reg(UInt(dataWidth.W))

  switch(IndexerCP) {
    is(0.U) {
      r_arb_req_valid := true.B
      r_arb_req.baseOffset     := 11.U
      r_arb_req.dataOffset     := 2.U
      r_arb_req.index          := 1.U
      r_arb_req.elementSize    := 8.U
      r_arb_req.mask           := "hFFFF".U
      when(r_arb_resp_valid) {
          r_res                := r_arb_resp.out
          r_arb_req_valid := false.B
          IndexerCP := 1.U
      }
    }
    is(1.U) {
      printf("result:%d\n", r_res)
    }
  }
}

