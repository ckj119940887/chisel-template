package AnvilHardFloat

import chisel3._
import chisel3.util._
import chisel3.experimental._

class AnvilRecFNFromFN(val expWidth: Int, val sigWidth: Int) extends RawModule {
  val io = IO(new Bundle{
    val in = Input(Bits((expWidth + sigWidth).W))
    val out = Output(Bits((expWidth + sigWidth + 1).W))
  })

  val rawIn = Module(new AnvilRawFloatFromFN(expWidth, sigWidth))
  io.out := rawIn.io.out.sign ##
          (Mux(rawIn.io.out.isZero, 0.U(3.W), rawIn.io.out.sExp(expWidth, expWidth - 2)) | Mux(rawIn.io.out.isNaN, 1.U, 0.U)) ##
            rawIn.io.out.sExp(expWidth - 3, 0) ## rawIn.io.out.sig(sigWidth - 2, 0)
}

