package MethodInvocation
import chisel3._ 
import chisel3.util._ 

class insertSort extends Module {
     val io = IO(new Bundle {
        val array = Input(Vec(20, SInt(32.W))) 
        val valid = Input(Bool()) 
        val array_out = Output(Vec(20, SInt(32.W))) 
        val ready = Output(Bool()) 
    })

    val state = RegInit(15.U(4.W))

    val array = Reg(Vec(20,  SInt(32.W)))
    when(RegNext(!io.valid)){
        array := io.array
    }
    io.array_out <> array
    val i = Reg(SInt(32.W))
    val key = Reg(SInt(32.W))
    val j = Reg(SInt(32.W))

    switch(state) {
        is(15.U) {
            state := Mux(io.valid, 0.U, state)
        }
        is(0.U) {
            i :=  1.S
            state := 1.U
        }
        is(1.U) {
            state := Mux(i <  10.S, 2.U, 12.U)
        }
        is(2.U) {
            key := array((i).asUInt())
            state := 3.U
        }
        is(3.U) {
            j := i -  1.S
            state := 4.U
        }
        is(4.U) {
            state := Mux(j >  0.S && array((j).asUInt()) > key, 5.U, 7.U)
        }
        is(5.U) {
            array((j +  1.S).asUInt()) := array((j).asUInt())
            state := 6.U
        }
        is(6.U) {
            j := j -  1.S
            state := 4.U
        }
        is(7.U) {
            state := Mux(j ===  0.S && array((j).asUInt()) > key, 8.U, 10.U)
        }
        is(8.U) {
            array((j +  1.S).asUInt()) := array((j).asUInt())
            state := 9.U
        }
        is(9.U) {
            array((j).asUInt()) := key
            state := 11.U
        }
        is(10.U) {
            array((j +  1.S).asUInt()) := key
            state := 11.U
        }
        is(11.U) {
            i := i  + 1.S
            state := 1.U
        }
        is(12.U) {
            state := 15.U
        }

    }
    io.ready := state === 12.U
}