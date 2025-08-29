package Router

import chisel3._
import chisel3.util._
import chisel3.experimental._

class StackData(val addrWidth: Int, val idWidth: Int, val cpWidth: Int) extends Bundle {
  val srcID      = UInt(idWidth.W)
  val srcCP      = UInt(cpWidth.W)
  val srcResAddr = UInt(addrWidth.W)
}

class Stack[T <: Data](val gen: T, val width: Int, val depth: Int) extends Module {
  val io = IO(new Bundle {
    val push         = Input(Bool())
    val pop          = Input(Bool())
    val en           = Input(Bool())
    val dataIn       = Input(gen.cloneType)
    val dataOut      = Output(gen.cloneType)
    val valid        = Output(Bool())
  })

  val stack_mem = Mem(depth, gen)
  val sp        = RegInit(0.U(log2Ceil(depth+1).W))
  val out       = RegInit(0.U.asTypeOf(gen))
  val popValid  = Reg(Bool())
  val pushValid = Reg(Bool())

  popValid  := false.B
  pushValid := false.B

  when (io.en) {
    when(io.push && (sp < depth.U)) {
      stack_mem(sp) := io.dataIn
      sp            := sp + 1.U
      pushValid     := true.B
    } 
    when (io.pop && sp > 0.U) {
      out      := stack_mem(sp - 1.U)
      sp       := sp - 1.U
      popValid := true.B 
    }
  }

  io.dataOut := out
  io.valid   := pushValid | popValid
}