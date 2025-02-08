package GeneralRegFileToBRAM 

import chisel3._
import chisel3.util._

class DualPortBRAM(width: Int, depth: Int) extends Module {
  val addrWidth = log2Ceil(depth)

  val io = IO(new Bundle {
    // port A
    val addrA = Input(UInt(addrWidth.W))  
    val dinA  = Input(UInt(width.W))      
    val doutA = Output(UInt(width.W))     
    val weA   = Input(Bool())             
    val enA   = Input(Bool())             

    // port B
    val addrB = Input(UInt(addrWidth.W))  
    val dinB  = Input(UInt(width.W))      
    val doutB = Output(UInt(width.W))     
    val weB   = Input(Bool())             
    val enB   = Input(Bool())             
  })

  // create BRAM 
  val mem = SyncReadMem(depth, UInt(width.W))

  // port A logic
  when(io.enA) {
    when(io.weA) {
      mem.write(io.addrA, io.dinA)  // A write port
    }
    io.doutA := mem.read(io.addrA, !io.weA)  // A read port
  }.otherwise {
    io.doutA := 0.U
  }

  // port B logic 
  when(io.enB) {
    when(io.weB) {
      mem.write(io.addrB, io.dinB)  // B write port 
    }
    io.doutB := mem.read(io.addrB, !io.weB)  // B read port
  }.otherwise {
    io.doutB := 0.U
  }
}