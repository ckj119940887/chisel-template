package InfixExpressionTest
import chisel3._ 
import chisel3.util._ 

class div extends Module {
     val io = IO(new Bundle {
        val x = Input(SInt(32.W)) 
        val y = Input(SInt(32.W)) 
        val valid = Input(Bool()) 
        val ready = Output(Bool()) 
        val out_div = Output(SInt(32.W)) 
    })

    io.out_div := io.x / io.y

    io.ready := io.valid
}