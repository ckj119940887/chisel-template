package AnvilHardFloat

import chisel3._
import chisel3.util._
import chisel3.experimental._

// convert Recorded floating number to RawFloat format
class AnvilRawFloatFromRecFN (val expWidth: Int, val sigWidth: Int) extends Module {
  val io = IO(new Bundle{
    val in = Input(UInt((expWidth + sigWidth + 1).W))
    val out = Output(new RawFloat(expWidth, sigWidth))
  })
  val exp = io.in(expWidth + sigWidth - 1, sigWidth - 1)
  val isZero    = exp(expWidth, expWidth - 2) === 0.U
  val isSpecial = exp(expWidth, expWidth - 1) === 3.U

  io.out.isNaN  := isSpecial &&   exp(expWidth - 2)
  io.out.isInf  := isSpecial && ! exp(expWidth - 2)
  io.out.isZero := isZero
  io.out.sign   := io.in(expWidth + sigWidth)
  io.out.sExp   := exp.zext
  io.out.sig    := 0.U(1.W) ## ! isZero ## io.in(sigWidth - 2, 0)
}