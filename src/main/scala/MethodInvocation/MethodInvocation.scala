package MethodInvocation
import chisel3._ 
import chisel3.util._ 

class MethodInvocation extends Module {
     val io = IO(new Bundle {
        val valid = Input(Bool()) 
        val ready = Output(Bool()) 
    })

    val state = RegInit(3.U(2.W))
    when(reset.asBool()) {
        state := 3.U
    }
    val y = Reg(SInt(32.W))
    val __m_addMulMod_0 = Module(new addMulMod())
    __m_addMulMod_0.io.a := DontCare
    __m_addMulMod_0.io.b := DontCare
    __m_addMulMod_0.io.valid := false.B

    switch(state) {
        is(3.U) {
            state := Mux(io.valid, 0.U, state)
        }
        is(0.U) {
            y := __m_addMulMod_0.io.out_addMulMod
            __m_addMulMod_0.io.valid := Mux(__m_addMulMod_0.io.ready, RegNext(false.B), true.B)
            __m_addMulMod_0.io.a :=  1.S(32.W)
            __m_addMulMod_0.io.b :=  2.S(32.W)
            state := Mux(__m_addMulMod_0.io.ready, 1.U, state)
        }
        is(1.U) {
            state := 3.U
        }

    }
    io.ready := state === 1.U
}