package NewArrayTest
import chisel3._ 
import chisel3.util._ 

class NewArrayTest extends Module {
     val io = IO(new Bundle {
        val valid = Input(Bool()) 
        val ready = Output(Bool()) 
    })

    val i = Reg(UInt(32.W))
    val a = RegInit(VecInit(Seq(1.U, 2.U, 3.U)))
    val b = Reg(Vec( 3, UInt(32.W)))
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
            b(i) := a(a(i))
            state := 2.U
        }
        is(2.U) {
            b(a(i) * i) := a(b(a(i)))
            state := 3.U
        }

    }
    io.ready := state === 3.U
}