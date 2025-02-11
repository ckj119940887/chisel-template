package GeneralRegFileToBRAM

import chisel3._
import chisel3.util._
import chisel3.experimental._

class Adder64 extends Module {
    val io = IO(new Bundle{
        val valid = Input(Bool())
        val x = Input(UInt(64.W))
        val y = Input(UInt(64.W))
        val result = Output(UInt(64.W))
    })

    val regX = Reg(UInt(64.W))
    val regY = Reg(UInt(64.W))
    val regResult = Reg(UInt(64.W))

    regX := io.x
    regY := io.y

    when(io.valid) {
        regX := regX + regY 
    }

    io.result := regX 
}