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
            state := 1.U
        }
        is(1.U) {
            step :=  0.U
            state := 2.U
        }
        is(2.U) {
            state := Mux(i <  10.U, 3.U, 6.U)
        }
        is(3.U) {
            array(i) := i + step
            state := 4.U
        }
        is(4.U) {
            i := i  + 1.U
            state := 5.U
        }
        is(5.U) {
            step := step  + 1.U
            state := 2.U
        }
        is(6.U) {
            state := 15.U
        }

    }
    io.ready := state === 6.U
}