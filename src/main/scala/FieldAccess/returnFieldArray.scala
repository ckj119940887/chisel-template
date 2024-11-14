package FieldAccess
import chisel3._ 
import chisel3.util._ 

class returnFieldArray extends Module {
     val io = IO(new Bundle {
        val a = Input(new FieldAccessFoo) 
        val valid = Input(Bool()) 
        val we_returnFieldArray = Input(Bool()) 
        val din_returnFieldArray = Input(new FieldAccessBar) 
        val addr_returnFieldArray = Input(UInt(3.W)) 
        val ready = Output(Bool()) 
        val dout_returnFieldArray = Output(new FieldAccessBar) 
    })

    val i = Reg(UInt(32.W))
    val f = Reg(Vec( 5, new FieldAccessBar))
    f(io.addr_returnFieldArray) := Mux(io.we_returnFieldArray, io.din_returnFieldArray, f(io.addr_returnFieldArray)) 
    io.dout_returnFieldArray := Mux(!io.we_returnFieldArray, f(io.addr_returnFieldArray), DontCare)
    val state = RegInit(Fill(3, 1.B))

    switch(state) {
        is(7.U) {
            state := Mux(io.valid, 0.U, state)
        }
        is(0.U) {
            i :=  0.U
            state := 1.U
        }
        is(1.U) {
            state := Mux(i <  5.U, 2.U, 5.U)
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
            i := i  + 1.U
            state := 1.U
        }

    }
    io.ready := state === 5.U
}