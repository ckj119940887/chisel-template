package InfixExpressionTest
import chisel3._ 
import chisel3.util._ 

class equal extends Module {
     val io = IO(new Bundle {
        val x = Input(SInt(32.W)) 
        val y = Input(SInt(32.W)) 
        val valid = Input(Bool()) 
        val ready = Output(Bool()) 
        val out_equal = Output(Bool()) 
    })

    io.out_equal := io.x === io.y

    io.ready := io.valid
}