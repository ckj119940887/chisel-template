package InfixExpressionTest
import chisel3._ 
import chisel3.util._ 

class leftshift extends Module {
     val io = IO(new Bundle {
        val x = Input(SInt(32.W)) 
        val y = Input(SInt(32.W)) 
        val valid = Input(Bool()) 
        val ready = Output(Bool()) 
        val out_leftshift = Output(SInt(32.W)) 
    })

    io.out_leftshift := io.x << (io.y).asUInt()

    io.ready := io.valid
}