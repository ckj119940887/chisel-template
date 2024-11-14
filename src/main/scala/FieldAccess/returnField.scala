package FieldAccess
import chisel3._ 
import chisel3.util._ 

class returnField extends Module {
     val io = IO(new Bundle {
        val a = Input(UInt(32.W)) 
        val b = Input(new FieldAccessFoo) 
        val valid = Input(Bool()) 
        val ready = Output(Bool()) 
        val out_returnField = Output(new FieldAccessBar) 
    })

    val f = Reg(new FieldAccessBar())
    io.out_returnField := f
    val state = RegInit(Fill(2, 1.B))

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

    }
    io.ready := state === 2.U
}