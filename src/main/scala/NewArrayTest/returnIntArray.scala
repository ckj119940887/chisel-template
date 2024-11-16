package NewArrayTest
import chisel3._ 
import chisel3.util._ 

class returnIntArray extends Module {
     val io = IO(new Bundle {
        val a = Input(SInt(32.W)) 
        val b = Input(SInt(32.W)) 
        val valid = Input(Bool()) 
        val ready = Output(Bool()) 
        val out_returnIntArray = Output(Vec(20, SInt(32.W))) 
    })

    val state = RegInit(7.U(3.W))

    val i = Reg(SInt(32.W))
    val x = Reg(Vec(20,  SInt(32.W)))
    x <> io.out_returnIntArray

    switch(state) {
        is(7.U) {
            state := Mux(io.valid, 0.U, state)
        }
        is(0.U) {
            i :=  0.S
            state := 1.U
        }
        is(1.U) {
            state := Mux(i <  10.S, 2.U, 6.U)
        }
        is(2.U) {
            state := Mux(i %  2.S ===  0.S, 4.U, state)
        }
        is(3.U) {
            x((i).asUInt()) := io.a + i
            state := 5.U
        }
        is(4.U) {
            x((i).asUInt()) := io.b + i
            state := 5.U
        }
        is(5.U) {
            i := i  + 1.S
            state := 1.U
        }
        is(6.U) {
            state := 15.U
        }

    }
    io.ready := state === 6.U
}