package FieldAccess
import chisel3._ 
import chisel3.util._ 

class returnField extends Module {
     val io = IO(new Bundle {
        val a = Input(SInt(32.W)) 
        val b = Input(new FieldAccessFoo) 
        val valid = Input(Bool()) 
        val ready = Output(Bool()) 
        val out_returnField = Output(new FieldAccessBar) 
    })

    val state = RegInit(3.U(2.W))

    val f = Reg(new FieldAccessBar())
    io.out_returnField := f

    switch(state) {
        is(3.U) {
            state := Mux(io.valid, 0.U, state)
        }
        is(0.U) {
            f.f := io.b
            state := 1.U
        }
        is(1.U) {
            f.i := io.a
            state := 2.U
        }
        is(2.U) {
            state := 7.U
        }

    }
    io.ready := state === 2.U
}