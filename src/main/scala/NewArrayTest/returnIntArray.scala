package NewArrayTest
import chisel3._ 
import chisel3.util._ 

class returnIntArray extends Module {
     val io = IO(new Bundle {
        val a = Input(UInt(32.W)) 
        val b = Input(UInt(32.W)) 
        val valid = Input(Bool()) 
        val we_returnIntArray = Input(Bool()) 
        val din_returnIntArray = Input(UInt(32.W)) 
        val addr_returnIntArray = Input(UInt(4.W)) 
        val ready = Output(Bool()) 
        val dout_returnIntArray = Output(UInt(32.W)) 
    })

    val state = RegInit(7.U(3.W))

    val i = Reg(UInt(32.W))
    val x = Reg(Vec( 10,  UInt(32.W)))
    x(io.addr_returnIntArray) := Mux(io.we_returnIntArray, io.din_returnIntArray, x(io.addr_returnIntArray)) 
    io.dout_returnIntArray := Mux(!io.we_returnIntArray, x(io.addr_returnIntArray), DontCare)

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
          when (i %  2.U ===  0.U) {
                        x(i) := io.a + i

          }
          .otherwise {
                        x(i) := io.b + i
          }
            state := 3.U
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