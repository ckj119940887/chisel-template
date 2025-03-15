package GeneralRegFileToBRAM 

import chisel3._
import chisel3.util._
import chisel3.experimental._

class PipelinedDivMod(val N: Int) extends Module {
  val io = IO(new Bundle {
    val a = Input(UInt(N.W))
    val b = Input(UInt(N.W))
    val start = Input(Bool())   // 触发计算
    val valid = Output(Bool())  // 计算完成信号
    val quotient = Output(UInt(N.W))
    val remainder = Output(UInt(N.W))
  })

  val dividend = RegInit(0.U(N.W))
  val divisor = RegInit(0.U(N.W))
  val quotient = RegInit(0.U(N.W))
  val remainder = RegInit(0.U(N.W))
  val count = RegInit((N - 1).U((1+log2Ceil(N)).W))  // 适应不同位宽
  val busy = RegInit(false.B)

  when(io.start && !busy) {
    dividend := io.a
    divisor := io.b
    quotient := 0.U
    remainder := 0.U
    count := N.U
    busy := true.B
  }.elsewhen(busy) {
    when(count === 0.U) {
      busy := false.B
    } .otherwise {
      val shifted = remainder << 1 | (dividend >> (N - 1))
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

  io.quotient := quotient
  io.remainder := remainder
  io.valid := !busy
}