package TestArbiter 

import chisel3._
import chisel3.util._
import chisel3.experimental._

class GlobalVar(nU1:Int, nU8:Int, nU16:Int, nU32:Int, nU64:Int, nS8:Int, nS16:Int, nS32:Int, nS64:Int, dataWidth: Int = 64) extends Module {
  private val len1  : Int = nU1
  private val len8  : Int = nU8  + nS8
  private val len16 : Int = nU16 + nS16
  private val len32 : Int = nU32 + nS32
  private val len64 : Int = nU64 + nS64
  private val maxLen: Int = Seq(len1, len8, len16, len32, len64).max

  val io = IO(new Bundle{
    val start = Input(Bool())
    val in    = Input(UInt(dataWidth.W))
    val gtype = Input(UInt(5.W))
    val op    = Input(Bool()) // false.B -- read, true.B -- write
    val index = Input(UInt((log2Up(maxLen)).W))
    val out   = Output(UInt(dataWidth.W))
    val valid = Output(Bool())
  })

  val regVec1  = RegInit(VecInit(Seq.fill(len1)(0.U(1.W))))
  val regVec8  = RegInit(VecInit(Seq.fill(len8)(0.U(8.W))))
  val regVec16 = RegInit(VecInit(Seq.fill(len16)(0.U(16.W))))
  val regVec32 = RegInit(VecInit(Seq.fill(len32)(0.U(32.W))))
  val regVec64 = RegInit(VecInit(Seq.fill(len64)(0.U(64.W))))

  val r_write      = RegNext(io.start & io.op)
  val r_write_next = RegNext(r_write)
  val r_read       = RegNext(io.start & ~io.op)
  val r_read_next  = RegNext(r_read)
  val r_in         = RegNext(io.in)
  val r_index      = RegNext(io.index)
  val r_valid      = RegInit(false.B)
  val r_out        = RegInit(0.U(dataWidth.W))

  val r_1_enable   = RegNext(io.gtype === 1.U )
  val r_8_enable   = RegNext(io.gtype === 2.U )
  val r_16_enable  = RegNext(io.gtype === 4.U )
  val r_32_enable  = RegNext(io.gtype === 8.U )
  val r_64_enable  = RegNext(io.gtype === 16.U)

  r_valid := (r_read_next & ~RegNext(r_read_next)) | (r_write & ~r_write_next)

  when(r_write) {
    when(r_1_enable) {
      regVec1(r_index) := r_in(0)
    }
    when(r_8_enable) {
      regVec8(r_index) := r_in(7, 0)
    }
    when(r_16_enable) {
      regVec16(r_index) := r_in(15, 0)
    }
    when(r_32_enable) {
      regVec32(r_index) := r_in(31, 0)
    }
    when(r_64_enable) {
      regVec64(r_index) := r_in
    }
  }

  when(r_read) {
    when(r_1_enable) {
      r_out := Cat(0.U, regVec1(r_index))
    }
    when(r_8_enable) {
      r_out := Cat(0.U, regVec8(r_index))
    }
    when(r_16_enable) {
      r_out := Cat(0.U, regVec16(r_index))
    }
    when(r_32_enable) {
      r_out := Cat(0.U, regVec32(r_index))
    }
    when(r_64_enable) {
      r_out := regVec64(r_index)
    }
  }

  io.valid := r_valid
  io.out   := r_out
}

class GlobalVarRequestBundle(nU1:Int, nU8:Int, nU16:Int, nU32:Int, nU64:Int, nS8:Int, nS16:Int, nS32:Int, nS64:Int, dataWidth:Int = 64) extends Bundle {
  private val len1  : Int = nU1
  private val len8  : Int = nU8  + nS8
  private val len16 : Int = nU16 + nS16
  private val len32 : Int = nU32 + nS32
  private val len64 : Int = nU64 + nS64
  private val maxLen: Int = Seq(len1, len8, len16, len32, len64).max
  
  // index for all types
  // 1bit:  1
  // 8bit:  2
  // 16bit: 4 
  // 32bit: 8
  // 64bit: 16

  val in    = UInt(dataWidth.W)
  val op    = Bool()
  val gtype = UInt(5.W)
  val index = UInt(log2Up(maxLen).W)
}

class GlobalVarResponseBundle(dataWidth: Int = 64) extends Bundle {
  val out = UInt(dataWidth.W)
}

class GlobalVarIO(nU1:Int, nU8:Int, nU16:Int, nU32:Int, nU64:Int, nS8:Int, nS16:Int, nS32:Int, nS64:Int, dataWidth: Int = 64) extends Bundle {
  val req = Valid(new GlobalVarRequestBundle(nU1, nU8, nU16, nU32, nU64, nS8, nS16, nS32, nS64, dataWidth))
  val resp = Flipped(Valid(new GlobalVarResponseBundle(dataWidth)))
}

class GlobalVarArbiterIO(numIPs: Int, nU1:Int, nU8:Int, nU16:Int, nU32:Int, nU64:Int, nS8:Int, nS16:Int, nS32:Int, nS64:Int, dataWidth: Int = 64) extends Bundle {
  val ipReqs  = Flipped(Vec(numIPs, Valid(new GlobalVarRequestBundle(nU1, nU8, nU16, nU32, nU64, nS8, nS16, nS32, nS64, dataWidth))))
  val ipResps = Vec(numIPs, Valid(new GlobalVarResponseBundle(dataWidth)))
  val ip      = new GlobalVarIO(nU1, nU8, nU16, nU32, nU64, nS8, nS16, nS32, nS64, dataWidth)
}

class GlobalVarArbiterModule(numIPs: Int, nU1:Int, nU8:Int, nU16:Int, nU32:Int, nU64:Int, nS8:Int, nS16:Int, nS32:Int, nS64:Int, dataWidth: Int = 64) extends Module {
  val io = IO(new GlobalVarArbiterIO(numIPs, nU1, nU8, nU16, nU32, nU64, nS8, nS16, nS32, nS64, dataWidth))

  // ------------------ Stage 0: Input Cache ------------------
  val r_ipReq_valid = RegInit(VecInit(Seq.fill(numIPs)(false.B)))
  val r_ipReq_valid_next = RegInit(VecInit(Seq.fill(numIPs)(false.B)))
  val r_ipReq_enable = RegInit(VecInit(Seq.fill(numIPs)(false.B)))
  val r_ipReq_bits = RegInit(VecInit(Seq.fill(numIPs)(0.U.asTypeOf(new GlobalVarRequestBundle(nU1, nU8, nU16, nU32, nU64, nS8, nS16, nS32, nS64, dataWidth)))))

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
  val r_reqBits  = RegInit(0.U.asTypeOf(new GlobalVarRequestBundle(nU1, nU8, nU16, nU32, nU64, nS8, nS16, nS32, nS64, dataWidth)))
  val r_chosen   = RegInit(0.U(log2Up(numIPs).W))

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
  val r_mem_resp_valid = RegNext(io.ip.resp.valid, init = false.B)
  val r_mem_resp_bits  = RegNext(io.ip.resp.bits)
  val r_mem_resp_id    = RegNext(r_chosen, init = 0.U)

  val r_ipResp_valid = RegInit(VecInit(Seq.fill(numIPs)(false.B)))
  val r_ipResp_bits  = RegInit(VecInit(Seq.fill(numIPs)(0.U.asTypeOf(new GlobalVarResponseBundle(dataWidth)))))

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

class GlobalVarWrapper(numIPs: Int, nU1:Int, nU8:Int, nU16:Int, nU32:Int, nU64:Int, nS8:Int, nS16:Int, nS32:Int, nS64:Int, dataWidth: Int = 64) extends Module {
    val io = IO(new Bundle{
        val req = Input(Valid(new GlobalVarRequestBundle(nU1, nU8, nU16, nU32, nU64, nS8, nS16, nS32, nS64, dataWidth)))
        val resp = Output(Valid(new GlobalVarResponseBundle(dataWidth)))

    })

    val mod = Module(new GlobalVar(nU1, nU8, nU16, nU32, nU64, nS8, nS16, nS32, nS64, dataWidth))

    val r_req            = RegInit(0.U.asTypeOf(new GlobalVarRequestBundle(nU1, nU8, nU16, nU32, nU64, nS8, nS16, nS32, nS64, dataWidth)))
    val r_req_valid      = RegNext(io.req.valid, init = false.B)



    val r_resp_data  = RegNext(mod.io.out)
    val r_resp_valid = RegNext(mod.io.valid, init = false.B)


    val r_mod_start = RegInit(false.B)
    when(r_req_valid) {
        r_mod_start := true.B
    } .elsewhen(r_resp_valid) {
        r_mod_start := false.B
    }


    r_req := io.req.bits


    mod.io.op := r_req.op
    mod.io.index := r_req.index
    mod.io.gtype := r_req.gtype
    mod.io.in := r_req.in
    mod.io.start := r_mod_start
    io.resp.bits.out := r_resp_data
    io.resp.valid    := r_resp_valid


}

class GlobalVarFunctionModule(nU1:Int, nU8:Int, nU16:Int, nU32:Int, nU64:Int, nS8:Int, nS16:Int, nS32:Int, nS64:Int, dataWidth: Int) extends Module{
  val io = IO(new Bundle{
    val arb_req  = Valid(new GlobalVarRequestBundle(nU1, nU8, nU16, nU32, nU64, nS8, nS16, nS32, nS64, dataWidth))
    val arb_resp = Flipped(Valid(new GlobalVarResponseBundle(dataWidth)))
  })

  val r_arb_req          = RegInit(0.U.asTypeOf(new GlobalVarRequestBundle(nU1, nU8, nU16, nU32, nU64, nS8, nS16, nS32, nS64, dataWidth)))
  val r_arb_req_valid    = RegInit(false.B)
  val r_arb_resp         = RegInit(0.U.asTypeOf(new GlobalVarResponseBundle(dataWidth)))
  val r_arb_resp_valid   = RegInit(false.B)
  r_arb_resp       := io.arb_resp.bits
  r_arb_resp_valid := io.arb_resp.valid
  io.arb_req.bits  := r_arb_req
  io.arb_req.valid := r_arb_req_valid

  val GlobalVarCP = RegInit(0.U(4.W))
  val r_res       = Reg(SInt(8.W))

  switch(GlobalVarCP) {
    is(0.U) {
      r_arb_req_valid := true.B
      r_arb_req.in    := (-2.S).pad(64).asUInt
      r_arb_req.op    := true.B
      r_arb_req.gtype := 2.U
      r_arb_req.index := 0.U
      when(r_arb_resp_valid) {
          r_arb_req_valid := false.B
          GlobalVarCP := 1.U
      }
    }
    is(1.U) {
      r_arb_req_valid := true.B
      r_arb_req.op    := false.B
      r_arb_req.gtype := 2.U
      r_arb_req.index := 0.U
      when(r_arb_resp_valid) {
          r_res           := r_arb_resp.out(7,0).asSInt
          r_arb_req_valid := false.B
          GlobalVarCP := 2.U
      }
    }
    is(2.U) {
      printf("r_res=%d\n", r_res.asSInt)
    }
  }
}