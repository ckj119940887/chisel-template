package EM

import chisel3._
import chisel3.util._
import chisel3.experimental._

class TriggerOfCounter(val counterWidth: Int = 15) extends Module {
    val io = IO(new Bundle{
        val start = Input(Bool())
        val count = Input(UInt(counterWidth.W))
        val clear = Output(Bool())
        val trigger = Output(UInt(1.W))
    })

    val state = RegInit(0.U(3.W))

    io.trigger := false.B
    io.clear := false.B

    switch(state) {
        is(0.U) {
            state := Mux(io.start, 1.U, 0.U)
        }
        is(1.U) {
            io.clear := true.B
            state := 2.U
        }
        is(2.U) {
            io.clear := false.B
            state := 3.U
        }
        is(3.U) {
            io.trigger := true.B
            //state := Mux(io.count === 200000.U, 4.U, 3.U)
            state := Mux(io.count === 20000.U, 4.U, 3.U)
        }
        is(4.U) {
            io.clear := true.B
            state := 5.U
        }
        is(5.U) {
            io.clear := false.B
            state := 6.U
        }
        is(6.U) {
            //state := Mux(io.count === 200.U, 1.U, 6.U)
        }
    }
}