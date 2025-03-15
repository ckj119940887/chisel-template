package AnvilHardFloat

import chisel3._
import chisel3.util._
import chisel3.experimental._

// the signaling is set to true.B in the original test bench
class FPUCmp(val expWidth: Int, val sigWidth: Int) extends Module {
  val io = IO(new Bundle {
      val a = Input(Bits((expWidth + sigWidth).W))
      val b = Input(Bits((expWidth + sigWidth).W))
      val signaling = Input(Bool())
      val lt = Output(Bool())
      val eq = Output(Bool())
      val gt = Output(Bool())
      val exceptionFlags = Output(Bits(5.W))
  })

  val recA = Module(new AnvilRecFNFromFN(expWidth, sigWidth))
  recA.io.in := io.a

  val recB = Module(new AnvilRecFNFromFN(expWidth, sigWidth))
  recB.io.in := io.b

  val compareRecFN = Module(new CompareRecFN(expWidth, sigWidth))
  compareRecFN.io.a := recA.io.out
  compareRecFN.io.b := recB.io.out
  compareRecFN.io.signaling := io.signaling

  io.lt := compareRecFN.io.lt
  io.eq := compareRecFN.io.eq
  io.gt := compareRecFN.io.gt
  io.exceptionFlags := compareRecFN.io.exceptionFlags
}