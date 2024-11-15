package SHA1
import chisel3._ 
import chisel3.util._ 

class fill extends Module {
     val io = IO(new Bundle {
        val value = Input(UInt(32.W)) 
        val we_arr = Input(Bool()) 
        val addr_arr = Input(UInt(5.W)) 
        val din_arr = Input(UInt(32.W)) 
        val off = Input(UInt(32.W)) 
        val valid = Input(Bool()) 
        val dout_arr = Output(UInt(32.W)) 
        val ready = Output(Bool()) 
    })

    val state = RegInit(7.U(3.W))

    val arr = Reg(Vec(20,  UInt(32.W)))
    arr(io.addr_arr) := Mux(io.we_arr, io.din_arr, arr(io.addr_arr)) 
    io.dout_arr := Mux(!io.we_arr, arr(io.addr_arr), DontCare)

    switch(state) {
        is(7.U) {
            state := Mux(io.valid, 0.U, state)
        }
        is(0.U) {
            arr(io.off) := io.value >>  24.U &  255.U
            state := 1.U
        }
        is(1.U) {
            arr(io.off +  1.U) := io.value >>  16.U &  255.U
            state := 2.U
        }
        is(2.U) {
            arr(io.off +  2.U) := io.value >>  8.U &  255.U
            state := 3.U
        }
        is(3.U) {
            arr(io.off +  3.U) := io.value &  255.U
            state := 4.U
        }
        is(4.U) {
            state := 7.U
        }

    }
    io.ready := state === 4.U
}