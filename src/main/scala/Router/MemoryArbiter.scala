package Router

import chisel3._
import chisel3.util._

class MemoryRequestBundle(addrWidth: Int, dataWidth: Int) extends Bundle {
  val addr = UInt(addrWidth.W)
  val data = UInt(dataWidth.W)
  val write = Bool()
}

class MemoryResponseBundle(dataWidth: Int) extends Bundle {
  val data = UInt(dataWidth.W)
}

class MemoryIO(addrWidth: Int, dataWidth: Int) extends Bundle {
  val req = Valid(new MemoryRequestBundle(addrWidth, dataWidth))
  val resp = Flipped(Valid(new MemoryResponseBundle(dataWidth)))
}

class MemoryArbiterIO(numIPs: Int, addrWidth: Int, dataWidth: Int) extends Bundle {
  val ipReqs  = Flipped(Vec(numIPs, Valid(new MemoryRequestBundle(addrWidth, dataWidth))))
  val ipResps = Vec(numIPs, Valid(new MemoryResponseBundle(dataWidth)))
  val ip      = new MemoryIO(addrWidth, dataWidth)
}

class MemoryArbiterModule(numIPs: Int, addrWidth: Int, dataWidth: Int) extends Module {
  val io = IO(new MemoryArbiterIO(numIPs, addrWidth, dataWidth))

  // ------------------ Stage 0: Input Cache ------------------
  val r_ipReq_valid = RegInit(VecInit(Seq.fill(numIPs)(false.B)))
  val r_ipReq_valid_next = RegInit(VecInit(Seq.fill(numIPs)(false.B)))
  val r_ipReq_enable = RegInit(VecInit(Seq.fill(numIPs)(false.B)))
  val r_ipReq_bits = Reg(Vec(numIPs, new MemoryRequestBundle(addrWidth, dataWidth)))

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
  val r_reqBits  = Reg(new MemoryRequestBundle(addrWidth, dataWidth))
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
  val r_ipResp_bits  = Reg(Vec(numIPs, new MemoryResponseBundle(dataWidth)))

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

class SimpleMemory(val depth: Int = 1024, val width: Int = 8) extends Module {
    val io = IO(new Bundle{
        val en = Input(Bool())
        val we = Input(Bool())
        val addr = Input(UInt(log2Ceil(depth).W))
        val din = Input(UInt(width.W))
        val dout = Output(UInt(width.W))
    })
    
    val mem = SyncReadMem(depth, UInt(width.W))

    io.dout := mem.read(io.addr)

    when(io.en & io.we) {
      mem.write(io.addr, io.din)
    }
}

class SimpleMemoryWrapper(val depth: Int = 1024, val addrWidth: Int = 32, val dataWidth: Int = 32) extends Module {
    val io = IO(new Bundle{
        val req = Input(Valid(new MemoryRequestBundle(addrWidth, dataWidth)))
        val resp = Output(Valid(new MemoryResponseBundle(dataWidth)))
    })

    val mem = Module(new SimpleMemory(depth, dataWidth))
    mem.io.en := false.B
    mem.io.we := false.B
    mem.io.addr := 0.U
    mem.io.din := 0.U

    val r_req = Reg(new MemoryRequestBundle(addrWidth, dataWidth))
    val r_req_valid = RegNext(io.req.valid, false.B)
    val r_req_valid_next = RegNext(r_req_valid, false.B)
    val r_resp_data = RegNext(mem.io.dout)
    val r_resp_valid = RegNext(RegNext(r_req_valid & ~r_req_valid_next))

    r_req := io.req.bits

    io.resp.valid := r_resp_valid
    io.resp.bits.data := r_resp_data

    when(r_req_valid) {
      mem.io.en := true.B
      mem.io.we := r_req.write
      mem.io.addr := r_req.addr
      mem.io.din := r_req.data
    }
}
