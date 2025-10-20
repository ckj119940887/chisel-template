package TestArbiter 

import chisel3._
import chisel3.util._
import chisel3.experimental._

class SaveRestoreRequestBundle(
  nU1:Int, nU8:Int, nU16:Int, nU32:Int, nU64:Int,
  nS1:Int, nS8:Int, nS16:Int, nS32:Int, nS64:Int,
  idWidth: Int, cpWidth: Int
) extends Bundle {
  val u1    = Vec(nU1 , UInt(1.W))
  val u8    = Vec(nU8 , UInt(8.W))
  val u16   = Vec(nU16, UInt(16.W))
  val u32   = Vec(nU32, UInt(32.W))
  val u64   = Vec(nU64, UInt(64.W))
  val s1    = Vec(nS1 , SInt(1.W))
  val s8    = Vec(nS8 , SInt(8.W))
  val s16   = Vec(nS16, SInt(16.W))
  val s32   = Vec(nS32, SInt(32.W))
  val s64   = Vec(nS64, SInt(64.W))
  val srcCp = UInt(idWidth.W)   
  val srcId = UInt(cpWidth.W)
  val op    = UInt(2.W)
}

class SaveRestoreResponseBundle(
  nU1:Int, nU8:Int, nU16:Int, nU32:Int, nU64:Int,
  nS1:Int, nS8:Int, nS16:Int, nS32:Int, nS64:Int,
  idWidth: Int, cpWidth: Int
) extends Bundle {
  val u1    = Vec(nU1 , UInt(1.W))
  val u8    = Vec(nU8 , UInt(8.W))
  val u16   = Vec(nU16, UInt(16.W))
  val u32   = Vec(nU32, UInt(32.W))
  val u64   = Vec(nU64, UInt(64.W))
  val s1    = Vec(nS1 , SInt(1.W))
  val s8    = Vec(nS8 , SInt(8.W))
  val s16   = Vec(nS16, SInt(16.W))
  val s32   = Vec(nS32, SInt(32.W))
  val s64   = Vec(nS64, SInt(64.W))
  val srcCp = UInt(idWidth.W)   
  val srcId = UInt(cpWidth.W)
}

class TempSaveRestore(
  nU1:Int, nU8:Int, nU16:Int, nU32:Int, nU64:Int,
  nS1:Int, nS8:Int, nS16:Int, nS32:Int, nS64:Int,
  addrWidth:Int, dataWidth:Int, depth:Int,
  stackMaxDepth:Int, idWidth: Int, cpWidth: Int
) extends Module {

  private val totalBits: Int =
    nU1*1 + nU8*8 + nU16*16 + nU32*32 + nU64*64 +
    nS1*1 + nS8*8 + nS16*16 + nS32*32 + nS64*64 + 
    idWidth + cpWidth
  private val totalWords: Int = (totalBits + 63) / 64

  val io = IO(new Bundle {
    val arbMem_req  = Valid(new BlockMemoryRequestBundle(addrWidth, dataWidth, depth))
    val arbMem_resp = Flipped(Valid(new BlockMemoryResponseBundle(dataWidth)))
    val req         = Flipped(Valid(new SaveRestoreRequestBundle(nU1,nU8,nU16,nU32,nU64,nS1,nS8,nS16,nS32,nS64,idWidth,cpWidth)))
    val resp        = Valid(new SaveRestoreResponseBundle(nU1,nU8,nU16,nU32,nU64,nS1,nS8,nS16,nS32,nS64,idWidth,cpWidth))
  })

  val r_arbMem_req            = Reg(new BlockMemoryRequestBundle(addrWidth, dataWidth, depth))
  val r_arbMem_req_valid      = RegInit(false.B)
  val r_arbMem_resp           = Reg(new BlockMemoryResponseBundle(dataWidth))
  val r_arbMem_resp_valid     = RegInit(false.B)

  r_arbMem_resp       := io.arbMem_resp.bits
  r_arbMem_resp_valid := io.arbMem_resp.valid
  io.arbMem_req.bits  := r_arbMem_req
  io.arbMem_req.valid := r_arbMem_req_valid

  val r_req        = RegInit(0.U.asTypeOf(new SaveRestoreRequestBundle(nU1,nU8,nU16,nU32,nU64,nS1,nS8,nS16,nS32,nS64,idWidth,cpWidth)))
  val r_req_valid  = RegNext(io.req.valid, init = false.B)
  r_req            := io.req.bits

  val r_resp       = RegInit(0.U.asTypeOf(new SaveRestoreResponseBundle(nU1,nU8,nU16,nU32,nU64,nS1,nS8,nS16,nS32,nS64,idWidth,cpWidth)))
  val r_resp_valid = RegInit(false.B)
  io.resp.bits     := r_resp
  io.resp.valid    := r_resp_valid 

  val r_push = RegInit(false.B)
  val r_pop  = RegInit(false.B)
  r_push := Mux(r_req_valid, r_req.op === 1.U, false.B)
  r_pop  := Mux(r_req_valid, r_req.op === 2.U, false.B)

  val r_save_state    = RegInit(0.U(2.W))
  val r_restore_state = RegInit(0.U(2.W))
  val r_index         = RegInit(0.U(log2Up(totalWords).W))
  val r_stack_addr    = RegInit(depth.U(log2Up(depth + stackMaxDepth * totalWords * 8).W))

  // ========== 组合：把“当前输入寄存器”按顺序拼成 bitstream ==========
  // 用一个可变列表收集各段 bit 向量
  val parts = Seq.newBuilder[UInt]

  // UInt 组
  if (nU1  > 0) parts += Cat(r_req.u1.reverse)
  if (nU8  > 0) parts += Cat(r_req.u8.reverse)
  if (nU16 > 0) parts += Cat(r_req.u16.reverse)
  if (nU32 > 0) parts += Cat(r_req.u32.reverse)
  if (nU64 > 0) parts += Cat(r_req.u64.reverse)

  // SInt 组（转为 UInt）
  if (nS1  > 0) parts += Cat(r_req.s1.map(_.asUInt).reverse)
  if (nS8  > 0) parts += Cat(r_req.s8.map(_.asUInt).reverse)
  if (nS16 > 0) parts += Cat(r_req.s16.map(_.asUInt).reverse)
  if (nS32 > 0) parts += Cat(r_req.s32.map(_.asUInt).reverse)
  if (nS64 > 0) parts += Cat(r_req.s64.map(_.asUInt).reverse)

  // 其他字段（这些总是存在）
  parts += r_req.srcId
  parts += r_req.srcCp

  // 获取 Seq
  val partSeq = parts.result()

  // 最终拼接
  val bitstream_w =
    if (partSeq.nonEmpty) partSeq.reduce(Cat(_, _))
    else 0.U
  
  // 把 bitstream 切成 64b 词（末词高位补 0）
  val words_w = Reg(Vec(totalWords, UInt(64.W)))
  when(r_push) {
    for (i <- 0 until totalWords) {
      val lo = i * 64
      val hi = math.min(lo + 64, totalBits)
      val k  = hi - lo
      val slice = bitstream_w(hi - 1, lo)
      words_w(i) := (if (k == 64) slice else Cat(0.U((64 - k).W), slice))
    }
  }
  
  switch(r_save_state) {
    is(0.U) {
      r_save_state := Mux(r_push, 1.U, 0.U)
      r_index      := 0.U
    }
    is(1.U) {
      r_save_state := Mux(r_index < totalWords.U, 2.U, 3.U)
    }
    is(2.U) {
      r_arbMem_req.mode := 2.U
      r_arbMem_req.writeAddr := r_stack_addr
      r_arbMem_req.writeOffset := 0.U
      r_arbMem_req.writeLen := 8.U
      r_arbMem_req.writeData := words_w(r_index)
      r_arbMem_req_valid := Mux(r_arbMem_resp_valid, false.B, true.B)
  
      when(r_arbMem_resp_valid) {
        r_arbMem_req.mode := 0.U
        r_index := r_index + 1.U
        r_stack_addr := r_stack_addr + 8.U
        r_save_state := 1.U
      }
    }
    is(3.U) {
      r_save_state := 0.U
    }
  }

  // ========== restore：从 restoreWordsIn 合回 bitstream，再按顺序写寄存器 ==========
  val r_readMem_valid = RegNext(r_restore_state === 3.U)
  val r_readMem_valid_next = RegNext(r_readMem_valid)
  r_resp_valid := RegNext(r_readMem_valid_next)

  switch(r_restore_state) {
    is(0.U) {
      r_restore_state := Mux(r_pop, 1.U, 0.U)
      r_index         := 0.U
    }
    is(1.U) {
      r_restore_state := Mux(r_index < totalWords.U, 2.U, 3.U)
    }
    is(2.U) {
      r_arbMem_req.mode := 1.U
      r_arbMem_req.readAddr := r_stack_addr
      r_arbMem_req.readOffset := 0.U
      r_arbMem_req.readLen := 8.U
      r_arbMem_req_valid := Mux(r_arbMem_resp_valid, false.B, true.B)
  
      when(r_arbMem_resp_valid) {
        words_w(r_index) := r_arbMem_resp.data
        r_arbMem_req.mode := 0.U
        r_index := r_index + 1.U
        r_stack_addr := r_stack_addr + 8.U
        r_restore_state := 1.U
      }
    }
    is(3.U) {
      r_restore_state := 0.U
    }
  }

  val bitstream_r = WireDefault(0.U(totalBits.W))
  when (r_readMem_valid) {
    val pieces = for (i <- 0 until totalWords) yield {
      val lo = i * 64
      val hi = math.min(lo + 64, totalBits)
      val k  = hi - lo
      val w  = words_w(i)
      if (k == 64) w else w(k - 1, 0)
    }
  
    // Chisel Cat() 会自动高位在前；因此如果你之前逻辑是 LSB-first，需要 reverse
    bitstream_r := Cat(pieces.reverse)
  }

  when(r_readMem_valid_next) {
    // 切回每个寄存器（顺序/位宽与上面一致）
    var off = 0
    // UInt
    for (i <- 0 until nU1 ) { r_resp.u1(i)  := bitstream_r(off); off += 1 }
    for (i <- 0 until nU8 ) { r_resp.u8(i)  := bitstream_r(off + 8 -1, off); off += 8  }
    for (i <- 0 until nU16) { r_resp.u16(i) := bitstream_r(off + 16-1, off); off += 16 }
    for (i <- 0 until nU32) { r_resp.u32(i) := bitstream_r(off + 32-1, off); off += 32 }
    for (i <- 0 until nU64) { r_resp.u64(i) := bitstream_r(off + 64-1, off); off += 64 }
    // SInt
    for (i <- 0 until nS1 ) { r_resp.s1(i)  := bitstream_r(off).asSInt; off += 1 }
    for (i <- 0 until nS8 ) { r_resp.s8(i)  := bitstream_r(off + 8 -1, off).asSInt; off += 8  }
    for (i <- 0 until nS16) { r_resp.s16(i) := bitstream_r(off + 16-1, off).asSInt; off += 16 }
    for (i <- 0 until nS32) { r_resp.s32(i) := bitstream_r(off + 32-1, off).asSInt; off += 32 }
    for (i <- 0 until nS64) { r_resp.s64(i) := bitstream_r(off + 64-1, off).asSInt; off += 64 }
    { r_resp.srcId := bitstream_r(off + idWidth-1, off); off += idWidth }
    { r_resp.srcCp := bitstream_r(off + cpWidth-1, off); off += cpWidth }
  }

}