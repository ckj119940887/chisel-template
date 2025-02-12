package GeneralRegFileToBRAM 

import chisel3._
import chisel3.util._
import chisel3.experimental._

class GeneralRegFileToRegFile (val ARRAY_REG_WIDTH: Int = 8,
                               val ARRAY_REG_DEPTH: Int = 1024,
                               val GENERAL_REG_WIDTH: Int = 32,
                               val GENERAL_REG_DEPTH: Int = 64,
                               val NUM_STATES: Int = 10
                              ) extends Module {
    val io = IO(new Bundle{
        val valid = Input(Bool()) 
        val ready = Output(Bool())
        val i_array = Input(Vec(1 << log2Ceil(ARRAY_REG_DEPTH), UInt(ARRAY_REG_WIDTH.W))) 
        val o_array = Output(Vec(1 << log2Ceil(ARRAY_REG_DEPTH), UInt(ARRAY_REG_WIDTH.W)))
    })

    // reg for share array between software and IP
    val arrayRegFiles = Reg(Vec(1 << log2Ceil(ARRAY_REG_DEPTH), UInt(ARRAY_REG_WIDTH.W)))
    // reg for general purpose
    val generalRegFiles = Reg(Vec(1 << log2Ceil(GENERAL_REG_DEPTH), UInt(GENERAL_REG_WIDTH.W)))
    // reg for states
    val stateReg = RegInit(0.U(log2Ceil(NUM_STATES + 1).W))

    arrayRegFiles <> io.i_array
    io.o_array <> arrayRegFiles

    io.ready := Mux(stateReg === 5.U, true.B, false.B)

    switch(stateReg) {
        is(0.U) {
            stateReg := Mux(io.valid, 1.U, stateReg)
        }
        is(1.U) {
            generalRegFiles(0) := Cat(arrayRegFiles(3), arrayRegFiles(2), arrayRegFiles(1), arrayRegFiles(0))
            generalRegFiles(1) := Cat(arrayRegFiles(7), arrayRegFiles(6), arrayRegFiles(5), arrayRegFiles(4))
            generalRegFiles(2) := Cat(arrayRegFiles(11), arrayRegFiles(10), arrayRegFiles(9), arrayRegFiles(8))
            generalRegFiles(3) := Cat(arrayRegFiles(15), arrayRegFiles(14), arrayRegFiles(13), arrayRegFiles(12))
            stateReg := 2.U
        }
        is(2.U) {
            generalRegFiles(4) := generalRegFiles(0) + generalRegFiles(1)
            generalRegFiles(5) := generalRegFiles(2) + generalRegFiles(3)
            stateReg := 3.U
        }
        is(3.U) {
            generalRegFiles(6) := generalRegFiles(4) + generalRegFiles(5)
            stateReg := 4.U
        }
        is(4.U) {
            arrayRegFiles(19) := generalRegFiles(6)(31,24)
            arrayRegFiles(18) := generalRegFiles(6)(23,16)
            arrayRegFiles(17) := generalRegFiles(6)(15,8)
            arrayRegFiles(16) := generalRegFiles(6)(7,0)
            stateReg := 5.U
        }
        is(5.U) {
        }
    }
}