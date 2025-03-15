package GeneralRegFileToBRAM 

import chisel3._
import chisel3.util._
import chisel3.experimental._

class shift extends Module {
    val io = IO(new Bundle{
        val a = Input(SInt(32.W))
        val num = Input(UInt(32.W))
        val out = Output(UInt(32.W))
    })

    //io.out := ((io.a).asUInt >> 16.U).asUInt
    io.out := Cat(
        1.U(4.W),
        1.U(4.W)
    )(3,0)

}