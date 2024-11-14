package InfixExpressionTest
import chisel3._ 
import chisel3.util._ 

class logicalOr extends Module {
     val io = IO(new Bundle {
        val x = Input(Bool()) 
        val y = Input(Bool()) 
        val valid = Input(Bool()) 
        val ready = Output(Bool()) 
        val out_logicalOr = Output(Bool()) 
    })

    io.out_logicalOr := io.x || io.y

    io.ready := io.valid
}