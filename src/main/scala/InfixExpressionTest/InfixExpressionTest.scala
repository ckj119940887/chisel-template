package InfixExpressionTest
import chisel3._ 
import chisel3.util._ 

class InfixExpressionTest extends Module {
     val io = IO(new Bundle {
        val valid = Input(Bool()) 
        val ready = Output(Bool()) 
    })

    val x = Reg(UInt(32.W))
    val y = Reg(UInt(32.W))
    val b = Reg(Bool())
    val state = RegInit(Fill(4, 1.B))

    switch(state) {
        is(15.U) {
            state := Mux(io.valid, 0.U, state)
        }
        is(0.U) {
            x :=  1.U
            y :=  2.U
            state := 1.U
        }
        is(1.U) {
            x := x +  2.U
            state := 2.U
        }
        is(2.U) {
            y := y + x
            state := 3.U
        }
        is(3.U) {
            b := x >  2.U
            state := 4.U
        }
        is(4.U) {
            x := x +  3.U
            state := 5.U
        }
        is(5.U) {
            y := y + x
            state := 6.U
        }
        is(6.U) {
            b := x + y >  2.U
            state := 7.U
        }
        is(7.U) {
            state := Mux(b, 8.U, 12.U)
        }
        is(8.U) {
            b :=  false.B
            state := 9.U
        }
        is(9.U) {
            x := x +  7.U
            state := 10.U
        }
        is(10.U) {
            y := y + x
            state := 11.U
        }
        is(11.U) {
            x := x + y +  8.U
            state := 7.U
        }
        is(12.U) {
            b := x > y
            state := 13.U
        }

    }
    io.ready := state === 13.U
}