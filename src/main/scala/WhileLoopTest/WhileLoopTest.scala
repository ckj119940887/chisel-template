package WhileLoopTest
import chisel3._ 
import chisel3.util._ 

class WhileLoopTest extends Module {
     val io = IO(new Bundle {
        val valid = Input(Bool()) 
        val ready = Output(Bool()) 
    })

    val state = RegInit(7.U(3.W))

    val x = Reg(UInt(32.W))
    val y = Reg(UInt(32.W))
    val b = Reg(Bool())

    switch(state) {
        is(7.U) {
            state := Mux(io.valid, 0.U, state)
        }
        is(0.U) {
            b :=  true.B
            x :=  1.U
            y :=  2.U
            state := 1.U
        }
        is(1.U) {
            state := Mux(b, 2.U, 4.U)
        }
        is(2.U) {
            x := x + y
            state := 3.U
        }
        is(3.U) {
          when (x >  10.U) {
                        b :=  false.B

          }
            state := 1.U
        }
        is(4.U) {
            state := 7.U
        }

    }
    io.ready := state === 4.U
}