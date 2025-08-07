package Router

import chisel3._
import chisel3.util._

class RequestBundle(addrWidth: Int, dataWidth: Int) extends Bundle {
  val addr = UInt(addrWidth.W)
  val data = UInt(dataWidth.W)
  val write = Bool()
}

class ResponseBundle(dataWidth: Int) extends Bundle {
  val data = UInt(dataWidth.W)
}

class MemoryIO(addrWidth: Int, dataWidth: Int) extends Bundle {
  val req = Valid(new RequestBundle(addrWidth, dataWidth))
  val resp = Flipped(Valid(new ResponseBundle(dataWidth)))
}

class ArbiterIO(numIPs: Int, addrWidth: Int, dataWidth: Int) extends Bundle {
  val ipReqs = Flipped(Vec(numIPs, Valid(new RequestBundle(addrWidth, dataWidth))))
  val ipResps = Vec(numIPs, Valid(new ResponseBundle(dataWidth)))
  val memory = new MemoryIO(addrWidth, dataWidth)
}

class MemoryArbiterModule(numIPs: Int, addrWidth: Int, dataWidth: Int) extends Module {
  val io = IO(new ArbiterIO(numIPs, addrWidth, dataWidth))

  // ------------------ Stage 0: Input Cache ------------------
  val r_ipReq_valid = RegInit(VecInit(Seq.fill(numIPs)(false.B)))
  val r_ipReq_valid_next = RegInit(VecInit(Seq.fill(numIPs)(false.B)))
  val r_ipReq_enable = RegInit(VecInit(Seq.fill(numIPs)(false.B)))
  val r_ipReq_bits = Reg(Vec(numIPs, new RequestBundle(addrWidth, dataWidth)))

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
  val r_reqBits  = Reg(new RequestBundle(addrWidth, dataWidth))
  val r_chosen   = Reg(UInt(log2Ceil(numIPs).W))

  r_foundReq := r_ipReq_enable.reduce(_ || _)
  for (i <- 0 until numIPs) {
    when(r_ipReq_enable(i)) {
      r_reqBits := r_ipReq_bits(i)
      r_chosen  := i.U
    }
  }

  io.memory.req.valid := r_foundReq
  io.memory.req.bits  := r_reqBits 

  // ------------------ Stage 2: memory.resp handling ------------------
  val r_mem_resp_valid = RegNext(io.memory.resp.valid)
  val r_mem_resp_bits  = RegNext(io.memory.resp.bits)
  val r_mem_resp_id    = RegNext(r_chosen)

  val r_ipResp_valid = RegInit(VecInit(Seq.fill(numIPs)(false.B)))
  val r_ipResp_bits  = Reg(Vec(numIPs, new ResponseBundle(dataWidth)))

  for (i <- 0 until numIPs) {
    r_ipResp_valid(i)     := false.B
    r_ipResp_bits(i).data := 0.U
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