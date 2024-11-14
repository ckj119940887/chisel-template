package IfStatementTest
import chisel3._ 
import chisel3.util._ 

class IfStatementTest extends Module {
     val io = IO(new Bundle {
        val valid = Input(Bool()) 
        val ready = Output(Bool()) 
    })

    val state = RegInit(3.U(2.W))

    val x = Reg(UInt(32.W))

    switch(state) {
        is(3.U) {
            state := Mux(io.valid, 0.U, state)
        }
        is(0.U) {
          when (x >  0.U) {
                        x := x -  1.U

          }
          .otherwise {
                        x :=  0.U
          }
            state := 1.U
        }
        is(1.U) {
            state := 3.U
        }

    }
    io.ready := state === 1.U
}