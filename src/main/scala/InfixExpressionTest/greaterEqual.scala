package InfixExpressionTest
import chisel3._ 
import chisel3.util._ 

class greaterEqual extends Module {
     val io = IO(new Bundle {
        val x = Input(SInt(32.W)) 
        val y = Input(SInt(32.W)) 
        val valid = Input(Bool()) 
        val ready = Output(Bool()) 
        val out_greaterEqual = Output(Bool()) 
    })

    io.out_greaterEqual := io.x >= io.y

    io.ready := io.valid
}