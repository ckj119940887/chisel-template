package GeneralRegFileToBRAM

import chisel3._
import chisel3.util._
import chisel3.experimental._

class Optimization1 extends Module {
    val io = IO(new Bundle{
        val valid = Input(Bool())
        val ready = Output(Bool())
    })

    // reg for share array between software and IP
    val arrayRegFiles = Reg(Vec(1024, UInt(8.W)))
    // stack pointer
    val sp = Reg(UInt(log2Ceil(1024).W))
    // reg for general purpose
    val generalRegFiles = Reg(Vec(32, UInt(32.W)))
    // reg for states
    val stateReg = RegInit(0.U(log2Ceil(10+ 1).W))

    io.ready := Mux(stateReg === 5.U, true.B, false.B)

    switch(stateReg) {
        is(0.U) {
            sp := 0.U
            stateReg := Mux(io.valid, 1.U, stateReg)
        }
        is(1.U) {
            arrayRegFiles(0) := 0.U
            arrayRegFiles(1) := 1.U
            arrayRegFiles(2) := 2.U
            arrayRegFiles(3) := 3.U
            arrayRegFiles(4) := 4.U
            arrayRegFiles(5) := 5.U
            arrayRegFiles(6) := 6.U
            arrayRegFiles(7) := 7.U
            arrayRegFiles(8) := 8.U
            arrayRegFiles(9) := 9.U

            stateReg := 2.U
        }
        is(2.U) {
            arrayRegFiles(8) := arrayRegFiles(9)
            arrayRegFiles(9) := 0.U
            stateReg := Mux(arrayRegFiles(arrayRegFiles(sp + 0.U)) < arrayRegFiles(arrayRegFiles(sp + 1.U) + 4.U), 5.U, 6.U)
        }
        is(3.U) {
            printf("arrayRegFiles(8):%x---arrayRegFiles(9):%x\n", arrayRegFiles(8), arrayRegFiles(9))
        }
        is(4.U) {
        }
        is(5.U) {
        }
        is(6.U) {
        }
        // is(2.U) {
        //     generalRegFiles(1) := arrayRegFiles(sp + 0.U) //$1 = 0
        //     generalRegFiles(0) := arrayRegFiles(sp + 1.U) //$0 = 1

        //     stateReg := 3.U
        // }
        // is(3.U) {
        //     generalRegFiles(2) := arrayRegFiles(generalRegFiles(0) + 4.U) //$2 = arrayRegFiles(5) = 5

        //     stateReg := 4.U
        // }
        // is(4.U) {
        //     stateReg := Mux(generalRegFiles(1) < generalRegFiles(2), 5.U, 6.U)
        //     printf("generalRegFiles(1)---%x, generalRegFiles(2)---%x\n", generalRegFiles(1), generalRegFiles(2))
        // }
        // is(5.U) {

        // }
        // is(6.U) {

        // }
    }
}