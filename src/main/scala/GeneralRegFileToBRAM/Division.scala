package GeneralRegFileToBRAM 

import chisel3._
import chisel3.util._
import chisel3.experimental._

class PipelinedDivMod(val width: Int) extends Module {
  val io = IO(new Bundle {
    val a = Input(SInt(width.W))
    val b = Input(SInt(width.W))
    val start = Input(Bool())   // 触发计算
    val valid = Output(Bool())  // 计算完成信号
    val quotient = Output(SInt(width.W))
    val remainder = Output(SInt(width.W))
  })

  val a_neg = io.a(width-1)
  val b_neg = io.b(width-1)
  val a_abs = Mux(a_neg, -io.a, io.a).asUInt
  val b_abs = Mux(b_neg, -io.b, io.b).asUInt

  val dividend = RegInit(0.U(width.W))
  val divisor = RegInit(0.U(width.W))
  val quotient = RegInit(0.U(width.W))
  val remainder = RegInit(0.U(width.W))
  val count = RegInit((width - 1).U((1+log2Ceil(width)).W))  // 适应不同位宽
  val busy = RegInit(false.B)

  when(io.start && !busy) {
    dividend := a_abs
    divisor := b_abs
    quotient := 0.U
    remainder := 0.U
    count := width.U
    busy := true.B
  }.elsewhen(busy) {
    when(count === 0.U) {
      busy := false.B
    } .otherwise {
      val shifted = remainder << 1 | (dividend >> (width - 1))
      remainder := shifted

      when (shifted >= divisor) {
        remainder := shifted - divisor
        quotient := (quotient << 1) | 1.U
      } .otherwise {
        quotient := quotient << 1
      }

      dividend := dividend << 1
      count := count - 1.U
    }
  }

  io.quotient := Mux(a_neg ^ b_neg, -quotient, quotient).asSInt
  io.remainder := Mux(a_neg, -remainder, remainder).asSInt
  io.valid := count === 0.U //!busy
}