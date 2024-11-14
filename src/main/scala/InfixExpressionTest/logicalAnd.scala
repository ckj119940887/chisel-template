package InfixExpressionTest
import chisel3._ 
import chisel3.util._ 

class logicalAnd extends Module {
     val io = IO(new Bundle {
        val x = Input(Bool()) 
        val y = Input(Bool()) 
        val valid = Input(Bool()) 
        val ready = Output(Bool()) 
        val out_logicalAnd = Output(Bool()) 
    })

    io.out_logicalAnd := io.x && io.y

    io.ready := io.valid
}