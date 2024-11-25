package MethodInvocation
import chisel3._ 
import chisel3.util._ 

class returnArray extends Module {
     val io = IO(new Bundle {
        val a = Input(new FieldAccessFoo) 
        val b = Input(SInt(32.W)) 
        val valid = Input(Bool()) 
        val ready = Output(Bool()) 
        val out_returnArray = Output(Vec(80, new FieldAccessFoo)) 
    })

    val state = RegInit(7.U(3.W))
    when(reset.asBool()) {
        state := 7.U
    }
    val i = Reg(SInt(32.W))
    val x = Reg(Vec(80, new FieldAccessFoo))
    val __m_addMulMod_0 = Module(new addMulMod())
    __m_addMulMod_0.io.a := DontCare
    __m_addMulMod_0.io.b := DontCare
    __m_addMulMod_0.io.valid := false.B
    x <> io.out_returnArray

    switch(state) {
        is(7.U) {
            state := Mux(io.valid, 0.U, state)
        }
        is(0.U) {
            i :=  0.S(32.W)
            state := 1.U
        }
        is(1.U) {
            state := Mux(i <  10.S(32.W), 2.U, 4.U)
        }
        is(2.U) {
            x((i).asUInt()).i := __m_addMulMod_0.io.out_addMulMod
            __m_addMulMod_0.io.valid := Mux(__m_addMulMod_0.io.ready, RegNext(false.B), true.B)
            __m_addMulMod_0.io.a := io.a.i
            __m_addMulMod_0.io.b := io.b + i
            state := Mux(__m_addMulMod_0.io.ready, 3.U, state)
        }
        is(3.U) {
            i := i  + 1.S
            state := 1.U
        }
        is(4.U) {
            state := 7.U
        }

    }
    io.ready := state === 4.U
}