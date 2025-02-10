package GeneralRegFileToBRAM 

import chisel3._
import chisel3.util._
import chisel3.experimental._

class Top (val ADDR_WIDTH: Int = 11, val DATA_WIDTH: Int = 32) extends Module {
    val io = IO(new Bundle{
        val startAddr  = Input(UInt(ADDR_WIDTH.W))
        val valid = Input(Bool())
        val resultAddr = Input(UInt(ADDR_WIDTH.W))

        val ready = Output(Bool())

        val topAddr = Input(UInt(ADDR_WIDTH.W))  
        val topDin  = Input(UInt(DATA_WIDTH.W))      
        val topDout = Output(UInt(DATA_WIDTH.W))     
        val topWe   = Input(Bool())             
        val topEn   = Input(Bool())             
    })

    val regFileToBRAM = Module (new GeneralRegFileToBRAM (ADDR_WIDTH = 11, 
                                                          DATA_WIDTH = 32,
                                                          NUM_GENERAL_REGS = 10, 
                                                          NUM_STATES = 10)) 

    regFileToBRAM.io.startAddr := io.startAddr
    regFileToBRAM.io.valid := io.valid
    regFileToBRAM.io.resultAddr := io.resultAddr
    io.ready := regFileToBRAM.io.ready

    val dpBRAM = Module(new DualPortBRAM(width = DATA_WIDTH, depth = 2^ADDR_WIDTH))

    dpBRAM.clock := regFileToBRAM.io.bramClk
    dpBRAM.io.addrA := regFileToBRAM.io.bramAddr(ADDR_WIDTH-1, 2)
    dpBRAM.io.dinA := regFileToBRAM.io.bramWData
    regFileToBRAM.io.bramRData := dpBRAM.io.doutA
    dpBRAM.io.weA := regFileToBRAM.io.bramWe
    dpBRAM.io.enA := regFileToBRAM.io.bramEn

    dpBRAM.io.addrB := io.topAddr
    dpBRAM.io.dinB := io.topDin
    io.topDout := dpBRAM.io.doutB
    dpBRAM.io.weB := io.topWe
    dpBRAM.io.enB := true.B
}