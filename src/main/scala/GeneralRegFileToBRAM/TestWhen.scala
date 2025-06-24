package GeneralRegFileToBRAM 

import chisel3._
import chisel3.util._
import chisel3.experimental._
import chisel3.util.experimental.loadMemoryFromFile

class ConditionCheckExample extends Module {
    val io = IO(new Bundle {
    val inA = Input(UInt(4.W)) 
    val inB = Input(UInt(4.W)) 

    val outA = Output(UInt(8.W))
    val outB = Output(UInt(8.W))
  })

  val condRegA = RegInit(0.U(4.W))
  val condRegB = RegInit(0.U(4.W))

  condRegA := io.inA
  condRegB := io.inB

  io.outA := 0.U
  io.outB := 0.U

  switch(condRegA) {
    is(0.U) { io.outA := 10.U }
    is(1.U) { io.outA := 20.U }
    is(2.U) { io.outA := 30.U }
    is(3.U) { io.outA := 40.U }
  }

  switch(condRegB) {
    is(0.U) { io.outB := 100.U }
    is(1.U) { io.outB := 200.U }
    is(2.U) { io.outB := 255.U }
    is(3.U) { io.outB := 77.U }
  }
}