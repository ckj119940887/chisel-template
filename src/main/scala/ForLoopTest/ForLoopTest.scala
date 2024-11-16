package ForLoopTest
import chisel3._ 
import chisel3.util._ 

class ForLoopTest extends Module {
     val io = IO(new Bundle {
        val valid = Input(Bool()) 
        val ready = Output(Bool()) 
    })

    val state = RegInit(7.U(3.W))

    val i = Reg(SInt(32.W))
    val step = Reg(SInt(32.W))
    val array = Reg(Vec(20,  SInt(32.W)))

    switch(state) {
        is(7.U) {
            state := Mux(io.valid, 0.U, state)
        }
        is(0.U) {
            i :=  0.S
            state := 1.U
        }
        is(1.U) {
            step :=  0.S
            state := 2.U
        }
        is(2.U) {
            state := Mux(i <  10.S, 3.U, 6.U)
        }
        is(3.U) {
            array((i).asUInt()) := i + step
            state := 4.U
        }
        is(4.U) {
            i := i  + 1.S
            state := 5.U
        }
        is(5.U) {
            step := step  + 1.S
            state := 2.U
        }
        is(6.U) {
            state := 15.U
        }

    }
    io.ready := state === 6.U
}