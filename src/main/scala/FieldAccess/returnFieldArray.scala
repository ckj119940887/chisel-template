package FieldAccess
import chisel3._ 
import chisel3.util._ 

class returnFieldArray extends Module {
     val io = IO(new Bundle {
        val a = Input(new FieldAccessFoo) 
        val valid = Input(Bool()) 
        val ready = Output(Bool()) 
        val out_returnFieldArray = Output(Vec(20, new FieldAccessBar)) 
    })

    val state = RegInit(15.U(4.W))

    val i = Reg(SInt(32.W))
    val j = Reg(SInt(32.W))
    val f = Reg(Vec(20, new FieldAccessBar))
    f <> io.out_returnFieldArray

    switch(state) {
        is(15.U) {
            state := Mux(io.valid, 0.U, state)
        }
        is(0.U) {
            i :=  0.S
            state := 1.U
        }
        is(1.U) {
            state := Mux(i <  5.S, 2.U, 9.U)
        }
        is(2.U) {
            f((i).asUInt()).f := io.a
            state := 3.U
        }
        is(3.U) {
            f((i).asUInt()).i := i
            state := 4.U
        }
        is(4.U) {
            j :=  0.S
            state := 5.U
        }
        is(5.U) {
            state := Mux(j <  10.S, 6.U, 8.U)
        }
        is(6.U) {
            f((i).asUInt()).x((j).asUInt()) :=  true.B
            state := 7.U
        }
        is(7.U) {
            j := j  + 1.S
            state := 5.U
        }
        is(8.U) {
            i := i  + 1.S
            state := 1.U
        }
        is(9.U) {
            state := 15.U
        }

    }
    io.ready := state === 9.U
}