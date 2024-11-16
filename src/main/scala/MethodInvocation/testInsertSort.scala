package MethodInvocation
import chisel3._ 
import chisel3.util._ 

class testInsertSort extends Module {
     val io = IO(new Bundle {
        val valid = Input(Bool()) 
        val ready = Output(Bool()) 
        val out_testInsertSort = Output(Vec(20, SInt(32.W))) 
    })

    val state = RegInit(7.U(3.W))

    val i = Reg(SInt(32.W))
    val test = Reg(Vec(20,  SInt(32.W)))
    val __m_insertSort_0 = Module(new insertSort())
    __m_insertSort_0.io.array := DontCare
    __m_insertSort_0.io.valid := false.B
    test <> io.out_testInsertSort

    switch(state) {
        is(7.U) {
            state := Mux(io.valid, 0.U, state)
        }
        is(0.U) {
            i :=  0.S
            state := 1.U
        }
        is(1.U) {
            state := Mux(i <  10.S, 2.U, 4.U)
        }
        is(2.U) {
            test((i).asUInt()) :=  10.S - i
            state := 3.U
        }
        is(3.U) {
            i := i  + 1.S
            state := 1.U
        }
        is(4.U) {
            __m_insertSort_0.io.array := test
            when(__m_insertSort_0.io.ready){
                test := __m_insertSort_0.io.array_out
            }
            __m_insertSort_0.io.valid := Mux(__m_insertSort_0.io.ready, RegNext(false.B), true.B)
            state := Mux(__m_insertSort_0.io.ready, 5.U, state)
        }
        is(5.U) {
            state := 7.U
        }

    }
    io.ready := state === 5.U
}