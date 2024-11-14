package ForLoopTest
import chisel3._ 
import chisel3.util._ 

class ForLoopTest extends Module {
     val io = IO(new Bundle {
        val valid = Input(Bool()) 
        val ready = Output(Bool()) 
    })

    val state = RegInit(7.U(3.W))

    val i = Reg(UInt(32.W))
    val step = Reg(UInt(32.W))
    val array = Reg(Vec( 10,  UInt(32.W)))

    switch(state) {
        is(7.U) {
            state := Mux(io.valid, 0.U, state)
        }
        is(0.U) {
            i :=  0.U
            step :=  0.U
            state := 1.U
        }
        is(1.U) {
            state := Mux(true.B, 2.U, 5.U)
        }
        is(2.U) {
            array(i) := i + step
            state := 3.U
        }
        is(3.U) {
            i := i  + 1.U
            state := 4.U
        }
        is(4.U) {
            step := step  + 1.U
            state := 1.U
        }
        is(5.U) {
            state := 7.U
        }

    }
    io.ready := state === 5.U
}