package IfStatementTest
import chisel3._ 
import chisel3.util._ 

class IfStatementTest extends Module {
     val io = IO(new Bundle {
        val valid = Input(Bool()) 
        val ready = Output(Bool()) 
    })

    val state = RegInit(15.U(4.W))
    when(reset.asBool()) {
        state := 15.U
    }
    val x = Reg(SInt(32.W))
    val y = Reg(SInt(32.W))

    switch(state) {
        is(15.U) {
            state := Mux(io.valid, 0.U, state)
        }
        is(0.U) {
            x :=  5.S(32.W)
            state := 1.U
        }
        is(1.U) {
            y :=  10.S(32.W)
            state := 2.U
        }
        is(2.U) {
            state := Mux(x >  0.S(32.W), 3.U, 5.U)
        }
        is(3.U) {
            x := x -  1.S(32.W)
            state := 4.U
        }
        is(4.U) {
            y := y + x
            state := 7.U
        }
        is(5.U) {
            x :=  0.S(32.W)
            state := 6.U
        }
        is(6.U) {
            y := y - x
            state := 7.U
        }
        is(7.U) {
            state := 15.U
        }

    }
    io.ready := state === 7.U
}