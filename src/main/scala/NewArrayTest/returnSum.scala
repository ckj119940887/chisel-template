package NewArrayTest
import chisel3._ 
import chisel3.util._ 

class returnSum extends Module {
     val io = IO(new Bundle {
        val initial = Input(UInt(32.W)) 
        val valid = Input(Bool()) 
        val ready = Output(Bool()) 
        val out_returnSum = Output(UInt(32.W)) 
    })

    val state = RegInit(31.U(5.W))

    val i = Reg(UInt(32.W))
    val sum = Reg(UInt(32.W))
    val x = Reg(Vec( 10,  UInt(32.W)))
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
            x( 0.U) :=  0.U
            state := 2.U
        }
        is(2.U) {
            x( 1.U) :=  1.U
            state := 3.U
        }
        is(3.U) {
            x( 2.U) :=  2.U
            state := 4.U
        }
        is(4.U) {
            x( 3.U) :=  3.U
            state := 5.U
        }
        is(5.U) {
            x( 4.U) :=  4.U
            state := 6.U
        }
        is(6.U) {
            x( 5.U) :=  5.U
            state := 7.U
        }
        is(7.U) {
            x( 6.U) :=  6.U
            state := 8.U
        }
        is(8.U) {
            x( 7.U) :=  7.U
            state := 9.U
        }
        is(9.U) {
            x( 8.U) :=  8.U
            state := 10.U
        }
        is(10.U) {
            x( 9.U) :=  9.U
            state := 11.U
        }
        is(11.U) {
            i :=  0.U
            state := 12.U
        }
        is(12.U) {
            state := Mux(i <  10.U, 13.U, 15.U)
        }
        is(13.U) {
            sum := sum + x(i)
            state := 14.U
        }
        is(14.U) {
            i := i  + 1.U
            state := 12.U
        }
        is(15.U) {
            state := 31.U
        }

    }
    io.ready := state === 15.U
}