package MethodInvocation
import chisel3._ 
import chisel3.util._ 

class addMulMod extends Module {
     val io = IO(new Bundle {
        val a = Input(SInt(32.W)) 
        val b = Input(SInt(32.W)) 
        val valid = Input(Bool()) 
        val ready = Output(Bool()) 
        val out_addMulMod = Output(SInt(32.W)) 
    })

    val state = RegInit(7.U(3.W))

    val x = Reg(SInt(32.W))
    io.out_addMulMod := x

    switch(state) {
        is(7.U) {
            state := Mux(io.valid, 0.U, state)
        }
        is(0.U) {
            x := io.a + io.b
            state := 1.U
        }
        is(1.U) {
            x := x << ( 2.S).asUInt()
            state := 2.U
        }
        is(2.U) {
            x := x %  10.S
            state := 3.U
        }
        is(3.U) {
            state := 7.U
        }

    }
    io.ready := state === 3.U
}