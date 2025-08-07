package GeneralRegFileToBRAM 

import chisel3._
import chisel3.util._
import chisel3.experimental._

class RecursiveFactorial(width: Int = 8, depth: Int = 32) extends Module {
  val io = IO(new Bundle {
    val n      = Input(UInt(width.W))
    val start  = Input(Bool())
    val result = Output(UInt((2 * width).W))
    val done   = Output(Bool())
  })

  val sIdle :: sPush :: sPop :: sDone :: Nil = Enum(4)
  val state = RegInit(sIdle)
  val sp      = RegInit(0.U(log2Ceil(depth).W)) // Stack pointer
  val stack_n = Mem(depth, UInt(width.W))       // Stack of n
  val stack_r = Mem(depth, UInt((2 * width).W)) // Stack of result accumulators
  val acc     = RegInit(1.U((2 * width).W))
  val current = Reg(UInt(width.W))

  switch(state) {
    is(sIdle) {
      when(io.start) {
        current := io.n
        acc     := 1.U
        sp      := 0.U
        state   := sPush
      }
    }
    is(sPush) {
      when(current > 1.U) {
        stack_n.write(sp, current)
        stack_r.write(sp, acc)
        sp := sp + 1.U
        current := current - 1.U
      }.otherwise {
        // current == 1, start popping
        acc := 1.U
        state := sPop
      }
    }
    is(sPop) {
      when(sp > 0.U) {
        val n_prev = stack_n.read(sp - 1.U)
        val acc_prev = stack_r.read(sp - 1.U)
        acc := acc * n_prev
        sp := sp - 1.U
      }.otherwise {
        state := sDone
      }
    }
    is(sDone) {
      // wait or reset
    }
  }
  io.result := acc
  io.done := (state === sDone)
}