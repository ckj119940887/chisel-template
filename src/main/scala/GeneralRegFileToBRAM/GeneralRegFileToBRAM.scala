package GeneralRegFileToBRAM 

import chisel3._
import chisel3.util._
import chisel3.experimental._

class GeneralRegFileToBRAM (val ADDR_WIDTH: Int = 15,
                            val DATA_WIDTH: Int = 32,
                            val NUM_GENERAL_REGS: Int = 10, 
                            val NUM_STATES: Int = 10) extends Module {
    val io = IO(new Bundle{
        val startAddr  = Input(UInt(ADDR_WIDTH.W))
        val resultAddr = Input(UInt(ADDR_WIDTH.W))
        val valid      = Input(Bool())

        val ready      = Output(Bool())

        val bramClk       = Output(Clock())
        val bramWe        = Output(Bool())
        val bramEn        = Output(Bool())
        val bramAddr      = Output(UInt(ADDR_WIDTH.W))
        val bramWData     = Output(UInt(DATA_WIDTH.W))
        val bramRData     = Input(UInt(DATA_WIDTH.W))
    })

    // general register files
    val generalRegFiles = Reg(Vec(NUM_GENERAL_REGS, UInt(32.W)))
    // reg for states
    val stateReg = RegInit(0.U(log2Ceil(NUM_STATES + 1).W))

    // sum for all 10 numbers
    val sum = RegInit(0.U(32.W))

    io.bramClk := clock

    io.bramWe := Mux(stateReg === 8.U, true.B, false.B) 
    io.bramEn := true.B

    io.bramAddr := Mux(stateReg === 1.U, io.startAddr      , 0.U) |
                   Mux(stateReg === 2.U, io.startAddr + 4.U, 0.U) |
                   Mux(stateReg === 3.U, io.startAddr + 8.U, 0.U) |
                   Mux(stateReg === 4.U, io.startAddr + 12.U, 0.U) |
                   Mux(stateReg === 5.U, io.startAddr + 16.U, 0.U) |
                   Mux(stateReg === 8.U, io.resultAddr     , 0.U)

    io.bramWData := Mux(stateReg === 8.U, sum, 0.U) 

    io.ready := Mux(stateReg === 9.U, true.B, false.B)

    switch(stateReg) {
        is(0.U) {
            stateReg := Mux(io.valid, 1.U, stateReg)
        }
        is(1.U) {
            stateReg := 2.U
        }
        is(2.U) {
            // $0 = shareMem[0]
            generalRegFiles(0) := io.bramRData
            
            stateReg := 3.U
        }
        is(3.U) {
            // sum = sum + $0
            sum := sum + generalRegFiles(0)
            // $1 = shareMem[1]
            generalRegFiles(1) := io.bramRData

            stateReg := 4.U
        }
        is(4.U) {
            // sum = sum + $1
            sum := sum + generalRegFiles(1)
            // $2 = shareMem[2]
            generalRegFiles(2) := io.bramRData

            stateReg := 5.U
        }
        is(5.U) {
            // sum = sum + $2
            sum := sum + generalRegFiles(2)
            // $3 = shareMem[3]
            generalRegFiles(3) := io.bramRData

            stateReg := 6.U
        }
        is(6.U) {
            // sum = sum + $3
            sum := sum + generalRegFiles(3)
            // $4 = shareMem[4]
            generalRegFiles(4) := io.bramRData

            stateReg := 7.U
        }
        is(7.U) {
            // sum = sum + $4
            sum := sum + generalRegFiles(4)

            stateReg := 8.U
        }
        is(8.U) {

            stateReg := 9.U
        }
        is(9.U) {

        }
    }
}