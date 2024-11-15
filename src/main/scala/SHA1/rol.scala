package SHA1
import chisel3._ 
import chisel3.util._ 

class rol extends Module {
     val io = IO(new Bundle {
        val num = Input(UInt(32.W)) 
        val cnt = Input(UInt(32.W)) 
        val valid = Input(Bool()) 
        val ready = Output(Bool()) 
        val out_rol = Output(UInt(32.W)) 
    })

    io.out_rol := io.num << io.cnt | io.num >>  32.U - io.cnt

    io.ready := io.valid
}