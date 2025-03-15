package GeneralRegFileToBRAM 

import chisel3._
import chisel3.util._
import chisel3.experimental._

class SignedPipelinedDivMod(val N: Int) extends Module {
  val io = IO(new Bundle {
    val a = Input(SInt(N.W))  // 有符号输入
    val b = Input(SInt(N.W))  
    val start = Input(Bool()) // 触发计算
    val valid = Output(Bool()) // 计算完成信号
    val quotient = Output(SInt(N.W))
    val remainder = Output(SInt(N.W))
  })

  // 计算符号（判断商是否应该是负数）
  val isNegativeResult = io.a(N-1) ^ io.b(N-1)  // 如果 a 和 b 符号不同，商应为负

  // 计算绝对值（处理负数）
  val dividend = RegInit(0.U(N.W))
  val divisor = RegInit(0.U(N.W))
  val quotient = RegInit(0.U(N.W))
  val remainder = RegInit(0.U(N.W))
  val count = RegInit((N - 1).U((1 + log2Ceil(N)).W))
  val busy = RegInit(false.B)

  when(io.start && !busy) {
    dividend := Mux(io.a(N-1), (~io.a) + 1.S, io.a).asUInt  // 取绝对值
    divisor := Mux(io.b(N-1), (~io.b) + 1.S, io.b).asUInt  // 取绝对值
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

      when(shifted >= divisor) {
        remainder := shifted - divisor
        quotient := (quotient << 1) | 1.U
      } .otherwise {
        quotient := quotient << 1
      }

      dividend := dividend << 1
      count := count - 1.U
    }
  }

  // 恢复符号
  io.quotient := Mux(isNegativeResult, (~quotient.asSInt) + 1.S, quotient.asSInt)  // 如果商是负数，补码表示
  io.remainder := Mux(io.a(N-1), (~remainder.asSInt) + 1.S, remainder.asSInt)  // 余数跟随被除数符号
  io.valid := !busy
}
