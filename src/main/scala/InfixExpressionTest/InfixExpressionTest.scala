package InfixExpressionTest
import chisel3._ 
import chisel3.util._ 

class InfixExpressionTest extends Module {
     val io = IO(new Bundle {
        val valid = Input(Bool()) 
        val ready = Output(Bool()) 
    })

    val state = RegInit(15.U(4.W))

    val x = Reg(UInt(32.W))
    val y = Reg(UInt(32.W))
    val b = Reg(Bool())

    switch(state) {
        is(15.U) {
            state := Mux(io.valid, 0.U, state)
        }
        is(0.U) {
            x :=  1.U
            state := 1.U
        }
        is(1.U) {
            y :=  2.U
            state := 2.U
        }
        is(2.U) {
            x := x +  2.U
            state := 3.U
        }
        is(3.U) {
            y := y + x
            state := 4.U
        }
        is(4.U) {
            b := x >  2.U
            state := 5.U
        }
        is(5.U) {
            x := x +  3.U
            state := 6.U
        }
        is(6.U) {
            y := y + x
            state := 7.U
        }
        is(7.U) {
            b := x + y >  2.U
            state := 8.U
        }
        is(8.U) {
            state := Mux(b, 9.U, 13.U)
        }
        is(9.U) {
            b :=  false.B
            state := 10.U
        }
        is(10.U) {
            x := x +  7.U
            state := 11.U
        }
        is(11.U) {
            y := y + x
            state := 12.U
        }
        is(12.U) {
            x := x + y +  8.U
            state := 8.U
        }
        is(13.U) {
            b := x > y
            state := 14.U
        }
        is(14.U) {
            state := 31.U
        }

    }
    io.ready := state === 14.U
}