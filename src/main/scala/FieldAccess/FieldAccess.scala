package FieldAccess
import chisel3._ 
import chisel3.util._ 

class FieldAccess extends Module {
     val io = IO(new Bundle {
        val valid = Input(Bool()) 
        val ready = Output(Bool()) 
    })

    val i = Reg(UInt(32.W))
    val f = Reg(new FieldAccessFoo())
    val b = Reg(new FieldAccessBar())
    val state = RegInit(Fill(4, 1.B))

    switch(state) {
        is(15.U) {
            state := Mux(io.valid, 0.U, state)
        }
        is(0.U) {
            f.i :=  0.U
            state := 1.U
        }
        is(1.U) {
            i := f.i
            state := 2.U
        }
        is(2.U) {
            b.i :=  1.U
            state := 3.U
        }
        is(3.U) {
            i := b.i
            state := 4.U
        }
        is(4.U) {
            b.f := f
            state := 5.U
        }
        is(5.U) {
            b.f.i :=  2.U
            state := 6.U
        }
        is(6.U) {
            i := b.f.i
            state := 7.U
        }

    }
    io.ready := state === 7.U
}