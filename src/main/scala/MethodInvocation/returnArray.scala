package MethodInvocation
import chisel3._ 
import chisel3.util._ 

class returnArray extends Module {
     val io = IO(new Bundle {
        val a = Input(new FieldAccessFoo) 
        val b = Input(UInt(32.W)) 
        val valid = Input(Bool()) 
        val ready = Output(Bool()) 
        val addr_returnArray = Input(UInt(4.W))
        val din_returnArray = Input(new FieldAccessFoo)
        val dout_returnArray = Output(new FieldAccessFoo)
        val we_returnArray = Input(Bool())
    })

    val state = RegInit(7.U(3.W))

    val i = Reg(UInt(32.W))
    val x = Reg(Vec( 10, new FieldAccessFoo))
    val __m_addMulMod = Module(new addMulMod())
    __m_addMulMod.io.a := 0.U
    __m_addMulMod.io.b := 0.U
    __m_addMulMod.io.valid := 0.U
    x(io.addr_returnArray) := Mux(io.we_returnArray, io.din_returnArray, x(io.addr_returnArray)) 
    io.dout_returnArray := Mux(!io.we_returnArray, x(io.addr_returnArray), DontCare)

    switch(state) {
        is(7.U) {
            state := Mux(io.valid, 0.U, state)
        }
        is(0.U) {
            i :=  0.U
            state := 1.U
        }
        is(1.U) {
            state := Mux(i <  10.U, 2.U, 4.U)
        }
        is(2.U) {
            x(i).i := __m_addMulMod.io.out_addMulMod
            __m_addMulMod.io.valid := Mux(__m_addMulMod.io.ready, RegNext(false.B), true.B)
            __m_addMulMod.io.a := io.a.i
            __m_addMulMod.io.b := io.b + i
            state := Mux(__m_addMulMod.io.ready, 3.U, state)
        }
        is(3.U) {
            i := i  + 1.U
            state := 1.U
        }
        is(4.U) {
            state := 7.U
        }

    }
    io.ready := state === 4.U
}