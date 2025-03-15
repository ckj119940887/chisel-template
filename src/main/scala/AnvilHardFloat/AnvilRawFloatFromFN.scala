package AnvilHardFloat

import chisel3._
import chisel3.util._
import chisel3.experimental._

// convert floating number to RawFloat format
class AnvilRawFloatFromFN(val expWidth: Int, val sigWidth: Int) extends RawModule {
  val io = IO(new Bundle{
    val in = Input(Bits((expWidth + sigWidth).W))
    val out = Output(new RawFloat(expWidth, sigWidth))
  })

  val sign = io.in(expWidth + sigWidth - 1)
  val expIn = io.in(expWidth + sigWidth - 2, sigWidth - 1)
  val fractIn = io.in(sigWidth - 2, 0)

  val isZeroExpIn = (expIn === 0.U)
  val isZeroFractIn = (fractIn === 0.U)

  val normDist = countLeadingZeros(fractIn)
  val subnormFract = (fractIn << normDist) (sigWidth - 3, 0) << 1
  val adjustedExp =
    Mux(isZeroExpIn,
      normDist ^ ((BigInt(1) << (expWidth + 1)) - 1).U,
      expIn
    ) + ((BigInt(1) << (expWidth - 1)).U
      | Mux(isZeroExpIn, 2.U, 1.U))

  val isZero = isZeroExpIn && isZeroFractIn
  val isSpecial = adjustedExp(expWidth, expWidth - 1) === 3.U

  io.out.isNaN := isSpecial && !isZeroFractIn
  io.out.isInf := isSpecial && isZeroFractIn
  io.out.isZero := isZero
  io.out.sign := sign
  io.out.sExp := adjustedExp(expWidth, 0).zext
  io.out.sig := 0.U(1.W) ## !isZero ## Mux(isZeroExpIn, subnormFract, fractIn)
}