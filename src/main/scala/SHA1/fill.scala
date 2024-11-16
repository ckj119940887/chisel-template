package SHA1
import chisel3._ 
import chisel3.util._ 

class fill extends Module {
     val io = IO(new Bundle {
        val value = Input(SInt(32.W)) 
        val arr = Input(Vec(80, SInt(32.W))) 
        val off = Input(SInt(32.W)) 
        val valid = Input(Bool()) 
        val arr_out = Output(Vec(80, SInt(32.W))) 
        val ready = Output(Bool()) 
    })

    val state = RegInit(7.U(3.W))
    when(reset.asBool()) {
        state := 7.U
    }
    val arr = Reg(Vec(80,  SInt(32.W)))
    when(RegNext(!io.valid)){
        arr := io.arr
    }
    io.arr_out <> arr

    switch(state) {
        is(7.U) {
            state := Mux(io.valid, 0.U, state)
        }
        is(0.U) {
            arr((io.off).asUInt()) := (io.value.asUInt() >> ( 24.S(32.W)).asUInt()(4,0)).asSInt() &  255.S(32.W)
            state := 1.U
        }
        is(1.U) {
            arr((io.off +  1.S(32.W)).asUInt()) := (io.value.asUInt() >> ( 16.S(32.W)).asUInt()(4,0)).asSInt() &  255.S(32.W)
            state := 2.U
        }
        is(2.U) {
            arr((io.off +  2.S(32.W)).asUInt()) := (io.value.asUInt() >> ( 8.S(32.W)).asUInt()(4,0)).asSInt() &  255.S(32.W)
            state := 3.U
        }
        is(3.U) {
            arr((io.off +  3.S(32.W)).asUInt()) := io.value &  255.S(32.W)
            state := 4.U
        }
        is(4.U) {
            state := 7.U
        }

    }
    io.ready := state === 4.U
}