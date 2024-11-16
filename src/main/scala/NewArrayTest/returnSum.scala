package NewArrayTest
import chisel3._ 
import chisel3.util._ 

class returnSum extends Module {
     val io = IO(new Bundle {
        val initial = Input(SInt(32.W)) 
        val valid = Input(Bool()) 
        val ready = Output(Bool()) 
        val out_returnSum = Output(SInt(32.W)) 
    })

    val state = RegInit(31.U(5.W))

    val i = Reg(SInt(32.W))
    val sum = Reg(SInt(32.W))
    val x = Reg(Vec(20,  SInt(32.W)))
    io.out_returnSum := sum

    switch(state) {
        is(31.U) {
            state := Mux(io.valid, 0.U, state)
        }
        is(0.U) {
            sum := io.initial
            state := 1.U
        }
        is(1.U) {
            x(( 0.S).asUInt()) :=  0.S
            state := 2.U
        }
        is(2.U) {
            x(( 1.S).asUInt()) :=  1.S
            state := 3.U
        }
        is(3.U) {
            x(( 2.S).asUInt()) :=  2.S
            state := 4.U
        }
        is(4.U) {
            x(( 3.S).asUInt()) :=  3.S
            state := 5.U
        }
        is(5.U) {
            x(( 4.S).asUInt()) :=  4.S
            state := 6.U
        }
        is(6.U) {
            x(( 5.S).asUInt()) :=  5.S
            state := 7.U
        }
        is(7.U) {
            x(( 6.S).asUInt()) :=  6.S
            state := 8.U
        }
        is(8.U) {
            x(( 7.S).asUInt()) :=  7.S
            state := 9.U
        }
        is(9.U) {
            x(( 8.S).asUInt()) :=  8.S
            state := 10.U
        }
        is(10.U) {
            x(( 9.S).asUInt()) :=  9.S
            state := 11.U
        }
        is(11.U) {
            i :=  0.S
            state := 12.U
        }
        is(12.U) {
            state := Mux(i <  10.S, 13.U, 15.U)
        }
        is(13.U) {
            sum := sum + x((i).asUInt())
            state := 14.U
        }
        is(14.U) {
            i := i  + 1.S
            state := 12.U
        }
        is(15.U) {
            state := 31.U
        }

    }
    io.ready := state === 15.U
}