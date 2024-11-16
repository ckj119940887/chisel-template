package InfixExpressionTest
import chisel3._ 
import chisel3.util._ 

class greater extends Module {
     val io = IO(new Bundle {
        val x = Input(SInt(32.W)) 
        val y = Input(SInt(32.W)) 
        val valid = Input(Bool()) 
        val ready = Output(Bool()) 
        val out_greater = Output(Bool()) 
    })

    io.out_greater := io.x > io.y

    io.ready := io.valid
}