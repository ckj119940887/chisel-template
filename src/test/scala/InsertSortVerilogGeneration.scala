package MethodInvocation

import chisel3.stage.ChiselStage

object InsertSortVerilogGeneration extends App {
  println(
    ChiselStage.emitSystemVerilog(new insertSort)
  )
}