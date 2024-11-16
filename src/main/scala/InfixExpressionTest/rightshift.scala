package InfixExpressionTest
import chisel3._ 
import chisel3.util._ 

class rightshift extends Module {
     val io = IO(new Bundle {
        val x = Input(SInt(32.W)) 
        val y = Input(SInt(32.W)) 
        val valid = Input(Bool()) 
        val ready = Output(Bool()) 
        val out_rightshift = Output(SInt(32.W)) 
    })

    io.out_rightshift := io.x >> (io.y).asUInt()

    io.ready := io.valid
}