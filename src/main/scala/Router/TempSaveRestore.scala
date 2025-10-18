package Router

import chisel3._
import chisel3.util._
import chisel3.experimental._

class SaveRequestBundle(
  nU1:Int, nU8:Int, nU16:Int, nU32:Int, nU64:Int,
  nS1:Int, nS8:Int, nS16:Int, nS32:Int, nS64:Int
) extends Bundle {
  val u1    = Input (Vec(nU1 , UInt(1.W)))
  val u8    = Input (Vec(nU8 , UInt(8.W)))
  val u16   = Input (Vec(nU16, UInt(16.W)))
  val u32   = Input (Vec(nU32, UInt(32.W)))
  val u64   = Input (Vec(nU64, UInt(64.W)))
  val s1    = Input (Vec(nS1 , SInt(1.W)))
  val s8    = Input (Vec(nS8 , SInt(8.W)))
  val s16   = Input (Vec(nS16, SInt(16.W)))
  val s32   = Input (Vec(nS32, SInt(32.W)))
  val s64   = Input (Vec(nS64, SInt(64.W)))
}

class RestoreResponseBundle(
  nU1:Int, nU8:Int, nU16:Int, nU32:Int, nU64:Int,
  nS1:Int, nS8:Int, nS16:Int, nS32:Int, nS64:Int
) extends Bundle {
  val u1    = Output(Vec(nU1 , UInt(1.W)))
  val u8    = Output(Vec(nU8 , UInt(8.W)))
  val u16   = Output(Vec(nU16, UInt(16.W)))
  val u32   = Output(Vec(nU32, UInt(32.W)))
  val u64   = Output(Vec(nU64, UInt(64.W)))
  val s1    = Output(Vec(nS1 , SInt(1.W)))
  val s8    = Output(Vec(nS8 , SInt(8.W)))
  val s16   = Output(Vec(nS16, SInt(16.W)))
  val s32   = Output(Vec(nS32, SInt(32.W)))
  val s64   = Output(Vec(nS64, SInt(64.W)))
}

class TempSaveRestore(
  nU1:Int, nU8:Int, nU16:Int, nU32:Int, nU64:Int,
  nS1:Int, nS8:Int, nS16:Int, nS32:Int, nS64:Int
) extends Module {

  private val totalBits: Int =
    nU1*1 + nU8*8 + nU16*16 + nU32*32 + nU64*64 +
    nS1*1 + nS8*8 + nS16*16 + nS32*32 + nS64*64
  private val totalWords: Int = (totalBits + 63) / 64

  val io = IO(new Bundle {
    val arbMem_req   = Valid(new BlockMemoryRequestBundle(addrWidth, dataWidth, depth))
    val arbMem_resp  = Flipped(Valid(new BlockMemoryResponseBundle(dataWidth)))
    val save_req     = Valid(new SaveRequestBundle(nU1,nU8,nU16,nU32,nU64,nS1,nS8,nS16,nS32,nS64))
    val save_resp    = Flipped(Valid(Bool()))
    val restore_req  = Valid(Bool())
    val restore_resp = Flipped(Valid(new RestoreResponseBundle(nU1,nU8,nU16,nU32,nU64,nS1,nS8,nS16,nS32,nS64)))
  })

  val r_arbMem_req            = Reg(new BlockMemoryRequestBundle(addrWidth, dataWidth, depth))
  val r_arbMem_req_valid      = RegInit(false.B)
  val r_arbMem_resp           = Reg(new BlockMemoryResponseBundle(dataWidth))
  val r_arbMem_resp_valid     = RegInit(false.B)

  r_arbMem_resp       := io.arbMem_resp.bits
  r_arbMem_resp_valid := io.arbMem_resp.valid
  io.arbMem_req.bits  := r_arbMem_req
  io.arbMem_req.valid := r_arbMem_req_valid

  val r_save_req             = RegInit(0.U.asTypeOf(new SaveRequestBundle(nU1,nU8,nU16,nU32,nU64,nS1,nS8,nS16,nS32,nS64)))
  val r_save_req_valid       = RegNext(io.save_req.valid, init = false.B)
  val r_save_req_valid_next  = RegNext(r_save_req_valid, init = false.B)
  val r_save_resp_valid      = RegInit(false.B)
  r_save_req         := io.save_req.bits
  io.save_resp.valid := r_save_resp_valid

  val r_restore_req_valid  = RegNext(io.restore_req.valid, init = false.B)
  val r_restore_resp       = RegInit(0.U.asTypeOf(new RestoreResponseBundle(nU1,nU8,nU16,nU32,nU64,nS1,nS8,nS16,nS32,nS64)))
  val r_restore_resp_valid = RegInit(false.B)
  val r_readMem_valid      = RegInit(false.B)
  val r_readMem_valid_next = RegNext(r_readMem_valid)
  val r_restore_finish     = RegNext(r_readMem_valid_next)
  io.restore_resp.bits  := r_restore_resp
  io.restore_resp.valid := r_restore_resp_valid 

  val r_save_state    = RegInit(0.U(3.W))
  val r_restore_state = RegInit(0.U(3.W))
  val r_index         = RegInit(0.U(log2Up(totalWords).W))

  // ========== 组合：把“当前输入寄存器”按顺序拼成 bitstream ==========
  val bitstream_w = Reg(UInt(totalBits.W))
  when(r_save_req_valid)
  {
    var off = 0
    // UInt 组（LSB-first）
    for (i <- 0 until nU1 ) { bitstream_w(off)             := r_save_req.u1(i);  off += 1  }
    for (i <- 0 until nU8 ) { bitstream_w(off + 8 -1, off) := r_save_req.u8(i);  off += 8  }
    for (i <- 0 until nU16) { bitstream_w(off + 16-1, off) := r_save_req.u16(i); off += 16 }
    for (i <- 0 until nU32) { bitstream_w(off + 32-1, off) := r_save_req.u32(i); off += 32 }
    for (i <- 0 until nU64) { bitstream_w(off + 64-1, off) := r_save_req.u64(i); off += 64 }
    // SInt 组按 UInt 位模式保存
    for (i <- 0 until nS1 ) { bitstream_w(off)             := r_save_req.s1(i).asUInt;  off += 1  }
    for (i <- 0 until nS8 ) { bitstream_w(off + 8 -1, off) := r_save_req.s8(i).asUInt;  off += 8  }
    for (i <- 0 until nS16) { bitstream_w(off + 16-1, off) := r_save_req.s16(i).asUInt; off += 16 }
    for (i <- 0 until nS32) { bitstream_w(off + 32-1, off) := r_save_req.s32(i).asUInt; off += 32 }
    for (i <- 0 until nS64) { bitstream_w(off + 64-1, off) := r_save_req.s64(i).asUInt; off += 64 }
  }

  // 把 bitstream 切成 64b 词（末词高位补 0）
  val words_w = Reg(Vec(totalWords, UInt(64.W)))
  when(r_save_req_valid_next) {
    for (i <- 0 until totalWords) {
      val lo = i * 64
      val hi = math.min(lo + 64, totalBits)
      val k  = hi - lo
      val slice = bitstream_w(hi - 1, lo)
      words_w(i) := (if (k == 64) slice else Cat(0.U((64 - k).W), slice))
    }
  }
  
  r_save_resp_valid := r_save_state === 3.U
  switch(r_save_state) {
    is(0.U) {
      r_save_state := Mux(r_save_req_valid_next, 1.U, 0.U)
      r_index      := 0.U
    }
    is(1.U) {
      r_save_state := Mux(r_index < totalWords.U, 2.U, 3.U)
    }
    is(2.U) {
      r_arbMem_req.mode := 2.U
      r_arbMem_req.writeAddr := 0.U
      r_arbMem_req.writeOffset := 0.U
      r_arbMem_req.writeLen := 8.U
      r_arbMem_req.writeData := words_w(r_index)
      r_arbMem_req_valid := Mux(r_arbMem_resp_valid, false.B, true.B)
  
      when(r_arbMem_resp_valid) {
        r_arbMem_req.mode := 0.U
        r_index := r_index + 1.U
        r_save_state := 1.U
      }
    }
    is(3.U) {
      r_save_state := 0.U
    }
  }

  // ========== restore：从 restoreWordsIn 合回 bitstream，再按顺序写寄存器 ==========
  r_readMem_valid := r_restore_state === 3.U
  switch(r_restore_state) {
    is(0.U) {
      r_restore_state := Mux(r_restore_req_valid, 1.U, 0.U)
      r_index         := 0.U
    }
    is(1.U) {
      r_restore_state := Mux(r_index < totalWords.U, 2.U, 3.U)
    }
    is(2.U) {
      r_arbMem_req.mode := 1.U
      r_arbMem_req.readAddr := 0.U
      r_arbMem_req.readOffset := 0.U
      r_arbMem_req.readLen := 8.U
      r_arbMem_req_valid := Mux(r_arbMem_resp_valid, false.B, true.B)
  
      when(r_arbMem_resp_valid) {
        words_w(r_index) := r_arbMem_req.readData 
        r_arbMem_req.mode := 0.U
        r_index := r_index + 1.U
        r_restore_state := 1.U
      }
    }
    is(3.U) {
      r_restore_state := 0.U
    }
  }

  when (r_readMem_valid) {
    // 把 64b 词合回 bitstream（仅取末词有效低 k 位）
    for (i <- 0 until totalWords) {
      val lo = i * 64
      val hi = math.min(lo + 64, totalBits)
      val k  = hi - lo
      val w  = words_w(i)
      val lowK = if (k == 64) w else w(k - 1, 0)
      bitstream_w(hi - 1, lo) := lowK
    }
  }

  when(r_readMem_valid_next) {
    // 切回每个寄存器（顺序/位宽与上面一致）
    var off = 0
    // UInt
    for (i <- 0 until nU1 ) { r_restore_resp.u1(i)  := bitstream_w(off); off += 1 }
    for (i <- 0 until nU8 ) { r_restore_resp.u8(i)  := bitstream_w(off + 8 -1, off);  off += 8  }
    for (i <- 0 until nU16) { r_restore_resp.u16(i) := bitstream_w(off + 16-1, off); off += 16 }
    for (i <- 0 until nU32) { r_restore_resp.u32(i) := bitstream_w(off + 32-1, off); off += 32 }
    for (i <- 0 until nU64) { r_restore_resp.u64(i) := bitstream_w(off + 64-1, off); off += 64 }
    // SInt
    for (i <- 0 until nS1 ) { r_restore_resp.s1(i)  := bitstream_w(off).asSInt; off += 1 }
    for (i <- 0 until nS8 ) { r_restore_resp.s8(i)  := bitstream_w(off + 8 -1, off).asSInt;  off += 8  }
    for (i <- 0 until nS16) { r_restore_resp.s16(i) := bitstream_w(off + 16-1, off).asSInt; off += 16 }
    for (i <- 0 until nS32) { r_restore_resp.s32(i) := bitstream_w(off + 32-1, off).asSInt; off += 32 }
    for (i <- 0 until nS64) { r_restore_resp.s64(i) := bitstream_w(off + 64-1, off).asSInt; off += 64 }
  }

}