package MethodInvocation
import chisel3._ 
import chisel3.util._ 

class MethodInvocation extends Module {
     val io = IO(new Bundle {
        val valid = Input(Bool()) 
        val ready = Output(Bool()) 
    })

    val state = RegInit(3.U(2.W))

    val y = Reg(UInt(32.W))
    val __m_addMulMod = Module(new addMulMod())
    __m_addMulMod.io.a := 0.U
    __m_addMulMod.io.b := 0.U
    __m_addMulMod.io.valid := 0.U

    switch(state) {
        is(3.U) {
            state := Mux(io.valid, 0.U, state)
        }
        is(0.U) {
            y := __m_addMulMod.io.out_addMulMod
            __m_addMulMod.io.valid := Mux(__m_addMulMod.io.ready, RegNext(false.B), true.B)
            __m_addMulMod.io.a :=  1.U
            __m_addMulMod.io.b :=  2.U
            state := Mux(__m_addMulMod.io.ready, 1.U, state)
        }
        is(1.U) {
            state := 3.U
        }

    }
    io.ready := state === 1.U
}