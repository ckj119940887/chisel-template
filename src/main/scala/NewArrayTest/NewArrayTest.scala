package NewArrayTest
import chisel3._ 
import chisel3.util._ 

class NewArrayTest extends Module {
     val io = IO(new Bundle {
        val valid = Input(Bool()) 
        val ready = Output(Bool()) 
    })

    val state = RegInit(7.U(3.W))

    val i = Reg(SInt(32.W))
    val a = RegInit(VecInit(Seq(1.S, 2.S, 3.S)))
    val b = Reg(Vec(20,  SInt(32.W)))

    switch(state) {
        is(7.U) {
            state := Mux(io.valid, 0.U, state)
        }
        is(0.U) {
            i :=  0.S
            state := 1.U
        }
        is(1.U) {
            b((i).asUInt()) := a((a((i).asUInt())).asUInt())
            state := 2.U
        }
        is(2.U) {
            b((a((i).asUInt()) * i).asUInt()) := a((b((a((i).asUInt())).asUInt())).asUInt())
            state := 3.U
        }
        is(3.U) {
            state := 7.U
        }

    }
    io.ready := state === 3.U
}