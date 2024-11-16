package SHA1
import chisel3._ 
import chisel3.util._ 

class rol extends Module {
     val io = IO(new Bundle {
        val num = Input(SInt(32.W)) 
        val cnt = Input(SInt(32.W)) 
        val valid = Input(Bool()) 
        val ready = Output(Bool()) 
        val out_rol = Output(SInt(32.W)) 
    })

    val state = RegInit(3.U(2.W))
    when(reset.asBool()) {
        state := 3.U
    }
    val temp = Reg(SInt(32.W))
    io.out_rol := temp

    switch(state) {
        is(3.U) {
            state := Mux(io.valid, 0.U, state)
        }
        is(0.U) {
            temp := (io.num << (io.cnt).asUInt()(4,0)).asSInt()
            state := 1.U
        }
        is(1.U) {
            temp := temp | (io.num.asUInt() >> ( 32.S(32.W) - io.cnt).asUInt()(4,0)).asSInt()
            state := 2.U
        }
        is(2.U) {
            state := 7.U
        }

    }
    io.ready := state === 2.U
}