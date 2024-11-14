package InfixExpressionTest
import chisel3._ 
import chisel3.util._ 

class and extends Module {
     val io = IO(new Bundle {
        val x = Input(UInt(32.W)) 
        val y = Input(UInt(32.W)) 
        val valid = Input(Bool()) 
        val ready = Output(Bool()) 
        val out_and = Output(UInt(32.W)) 
    })

    io.out_and := io.x & io.y

    io.ready := io.valid
}