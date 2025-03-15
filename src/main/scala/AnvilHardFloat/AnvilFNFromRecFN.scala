package AnvilHardFloat

import chisel3._
import chisel3.util._
import chisel3.experimental._

// convert Recorded floating number to floating number
class AnvilFNFromRecFN(val expWidth: Int, val sigWidth: Int) extends Module {
  val io = IO(new Bundle{
    val in = Input(UInt((expWidth + sigWidth + 1).W))
    val out = Output(Bits((expWidth + sigWidth).W))
  })

  val minNormExp = (BigInt(1)<<(expWidth - 1)) + 2

  val rawIn = Module(new AnvilRawFloatFromRecFN(expWidth, sigWidth))
  rawIn.io.in := io.in

  val isSubnormal = rawIn.io.out.sExp < minNormExp.S
  val denormShiftDist = 1.U - rawIn.io.out.sExp(log2Up(sigWidth - 1) - 1, 0)
  val denormFract = ((rawIn.io.out.sig>>1) >> denormShiftDist)(sigWidth - 2, 0)

  val expOut =
      Mux(isSubnormal,
          0.U,
          rawIn.io.out.sExp(expWidth - 1, 0) -
            ((BigInt(1)<<(expWidth - 1)) + 1).U
      ) | Fill(expWidth, rawIn.io.out.isNaN || rawIn.io.out.isInf)
  val fractOut =
      Mux(isSubnormal,
          denormFract,
          Mux(rawIn.io.out.isInf, 0.U, rawIn.io.out.sig(sigWidth - 2, 0))
      )

  io.out := Cat(rawIn.io.out.sign, expOut, fractOut)
}