package FieldAccess
import chisel3._ 
import chisel3.util._ 

class returnFieldArray extends Module {
     val io = IO(new Bundle {
        val a = Input(new FieldAccessFoo) 
        val valid = Input(Bool()) 
        val ready = Output(Bool()) 
        val addr_returnFieldArray = Input(UInt(3.W))
        val din_returnFieldArray = Input(new FieldAccessBar)
        val dout_returnFieldArray = Output(new FieldAccessBar)
        val we_returnFieldArray = Input(Bool())
    })

    val state = RegInit(15.U(4.W))

    val i = Reg(UInt(32.W))
    val j = Reg(UInt(32.W))
    val f = Reg(Vec( 5, new FieldAccessBar))
    f(io.addr_returnFieldArray) := Mux(io.we_returnFieldArray, io.din_returnFieldArray, f(io.addr_returnFieldArray)) 
    io.dout_returnFieldArray := Mux(!io.we_returnFieldArray, f(io.addr_returnFieldArray), DontCare)

    switch(state) {
        is(15.U) {
            state := Mux(io.valid, 0.U, state)
        }
        is(0.U) {
            i :=  0.U
            state := 1.U
        }
        is(1.U) {
            state := Mux(i <  5.U, 2.U, 9.U)
        }
        is(2.U) {
            f(i).f := io.a
            state := 3.U
        }
        is(3.U) {
            f(i).i := i
            state := 4.U
        }
        is(4.U) {
            j :=  0.U
            state := 5.U
        }
        is(5.U) {
            state := Mux(j <  10.U, 6.U, 8.U)
        }
        is(6.U) {
            f(i).x(j) :=  true.B
            state := 7.U
        }
        is(7.U) {
            j := j  + 1.U
            state := 5.U
        }
        is(8.U) {
            i := i  + 1.U
            state := 1.U
        }
        is(9.U) {
            state := 15.U
        }

    }
    io.ready := state === 9.U
}