package GeneralRegFileToBRAM 

import chisel3._
import chisel3.util._
import chisel3.experimental._

class BRAM(depth: Int, width: Int) extends Module {
  val io = IO(new Bundle {
    val addr  = Input(UInt(log2Ceil(depth).W))
    val din   = Input(UInt(width.W))
    val dout  = Output(UInt(width.W))
    val we    = Input(Bool())
    val en    = Input(Bool())
  })

  // instantiate BRAM
  val mem = SyncReadMem(depth, UInt(width.W))

  // read
  io.dout := 0.U 
  when(io.en) {
    io.dout := mem.read(io.addr)
  }

  // write 
  when(io.we && io.en) {
    mem.write(io.addr, io.din)
  }
}