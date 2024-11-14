package MethodInvocation
import chisel3._ 
import chisel3.util._ 

class insertSort extends Module {
     val io = IO(new Bundle {
        val valid = Input(Bool()) 
        val we_insertSort = Input(Bool()) 
        val din_insertSort = Input(UInt(32.W)) 
        val addr_insertSort = Input(UInt(4.W)) 
        val ready = Output(Bool()) 
        val dout_insertSort = Output(UInt(32.W)) 
    })

    val state = RegInit(15.U(4.W))

    val i = Reg(UInt(32.W))
    val key = Reg(UInt(32.W))
    val j = Reg(UInt(32.W))
    val array = Reg(Vec( 10,  UInt(32.W)))
    array(io.addr_insertSort) := Mux(io.we_insertSort, io.din_insertSort, array(io.addr_insertSort)) 
    io.dout_insertSort := Mux(!io.we_insertSort, array(io.addr_insertSort), DontCare)

    switch(state) {
        is(15.U) {
            state := Mux(io.valid, 0.U, state)
        }
        is(0.U) {
            i :=  1.U
            state := 1.U
        }
        is(1.U) {
            state := Mux(i <  10.U, 2.U, 10.U)
        }
        is(2.U) {
            key := array(i)
            state := 3.U
        }
        is(3.U) {
            j := i -  1.U
            state := 4.U
        }
        is(4.U) {
            state := Mux(j >  0.U && array(j) > key, 5.U, 7.U)
        }
        is(5.U) {
            array(j +  1.U) := array(j)
            state := 6.U
        }
        is(6.U) {
            j := j -  1.U
            state := 4.U
        }
        is(7.U) {
          when (j ===  0.U && array(j) > key) {
                        array(j +  1.U) := array(j)

          }
          .otherwise {
                        array(j +  1.U) := key
          }
            state := 8.U
        }
        is(8.U) {
          when (j ===  0.U && array(j) > key) {
                        array(j) := key

          }
            state := 9.U
        }
        is(9.U) {
            i := i  + 1.U
            state := 1.U
        }
        is(10.U) {
            state := 15.U
        }

    }
    io.ready := state === 10.U
}