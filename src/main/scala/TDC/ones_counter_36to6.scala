package TDC

import chisel3._
import chisel3.util._
import chisel3.experimental._

class ones_counter_36to6 extends BlackBox with HasBlackBoxResource {
    val io =  IO(new Bundle{
        val i_Clk = Input(Clock())
        val i_Reset_N = Input(Bool())
        val i_Sequence = Input(UInt(36.W))
        val o_Count = Output(UInt(6.W))
    })

    addResource("/TDC/ones_counter_36to6.v")
    addResource("/TDC/ones_counter_6to3.v")
}

class Wrapper_ones_counter_36to6 extends Module {
    val io =  IO(new Bundle{
        val i_Reset_N = Input(Bool())
        val i_Sequence = Input(UInt(36.W))
        val o_Count = Output(UInt(6.W))
    })

    val onesCounter = Module(new ones_counter_36to6())
    onesCounter.io.i_Clk := clock
    onesCounter.io.i_Reset_N := io.i_Reset_N
    onesCounter.io.i_Sequence := io.i_Sequence
    io.o_Count := onesCounter.io.o_Count
}