package GeneralRegFileToBRAM 

import chisel3._
import chisel3.util._
import chisel3.experimental._

class GeneralRegFileToRegFilePro (val C_S_AXI_DATA_WIDTH: Int = 32,
                                  val C_S_AXI_ADDR_WIDTH: Int = 32,
                                  val ARRAY_REG_WIDTH: Int = 8,
                                  val ARRAY_REG_DEPTH: Int = 1024,
                                  val GENERAL_REG_WIDTH: Int = 32,
                                  val GENERAL_REG_DEPTH: Int = 64,
                                  val NUM_STATES: Int = 10
                                 ) extends Module {
    val io = IO(new Bundle{
        val valid = Input(Bool()) 
        val ready = Output(Bool())
        val arrayRe        = Input(Bool())
        val arrayWe        = Input(Bool())
        val arrayStrb      = Input(UInt((C_S_AXI_DATA_WIDTH/8).W))
        val arrayReadAddr  = Input(UInt((log2Ceil(ARRAY_REG_DEPTH)).W))
        val arrayWriteAddr = Input(UInt((log2Ceil(ARRAY_REG_DEPTH)).W))
        val arrayWData     = Input(UInt(C_S_AXI_DATA_WIDTH.W))
        val arrayRData     = Output(UInt(C_S_AXI_DATA_WIDTH.W))
    })

    // reg for share array between software and IP
    val arrayRegFiles = Reg(Vec(1 << log2Ceil(ARRAY_REG_DEPTH), UInt(ARRAY_REG_WIDTH.W)))
    // reg for general purpose
    val generalRegFiles = Reg(Vec(1 << log2Ceil(GENERAL_REG_DEPTH), UInt(GENERAL_REG_WIDTH.W)))
    // reg for states
    val stateReg = RegInit(0.U(log2Ceil(NUM_STATES + 1).W))

    for(byteIndex <- 0 until (C_S_AXI_DATA_WIDTH/8)) {
        when(io.arrayWe & (io.arrayStrb(byteIndex.U) === 1.U)) {
            arrayRegFiles(io.arrayWriteAddr + byteIndex.U) := io.arrayWData((byteIndex * 8) + 7, byteIndex * 8)
        }
    }

    io.arrayRData := Mux(io.arrayRe, Cat(arrayRegFiles(io.arrayReadAddr + 3.U), 
                                         arrayRegFiles(io.arrayReadAddr + 2.U),
                                         arrayRegFiles(io.arrayReadAddr + 1.U),
                                         arrayRegFiles(io.arrayReadAddr + 0.U)), 0.U)

    // val arrayRDataVec = VecInit(Seq.fill(C_S_AXI_DATA_WIDTH / 8)(0.U(8.W)))
    // for (byteIndex <- 0 until (C_S_AXI_DATA_WIDTH / 8)) {
    //     when(!io.arrayWe && io.arrayStrb(byteIndex.U) === 1.U && io.arrayAddr + byteIndex.U < ARRAY_REG_DEPTH.U) {
    //         arrayRDataVec(byteIndex) := arrayRegFiles(io.arrayAddr + byteIndex.U)
    //     }
    // }
    // io.arrayRData := arrayRDataVec.asUInt

    // val arrayRDataReg = Reg(Vec(C_S_AXI_DATA_WIDTH / 8, UInt(8.W)))
    // for (byteIndex <- 0 until (C_S_AXI_DATA_WIDTH / 8)) {
    //     when(!io.arrayWe && io.arrayStrb(byteIndex.U) === 1.U && io.arrayAddr + byteIndex.U < ARRAY_REG_DEPTH.U) {
    //         arrayRDataReg(byteIndex.U) := arrayRegFiles(io.arrayAddr + byteIndex.U)
    //     }
    // }
    // io.arrayRData := arrayRDataReg.asUInt

    // val arrayRDataReg = Reg(UInt(C_S_AXI_DATA_WIDTH.W))
    // arrayRDataReg := Cat((0 until (C_S_AXI_DATA_WIDTH / 8)).map { byteIndex =>
    //     Mux(!io.arrayWe && io.arrayStrb(byteIndex.U) === 1.U && io.arrayAddr + byteIndex.U < ARRAY_REG_DEPTH.U,
    //         arrayRegFiles(io.arrayAddr + byteIndex.U),
    //         0.U(8.W)
    //     )
    // }.reverse)
    // io.arrayRData := arrayRDataReg

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
            generalRegFiles(4) := (generalRegFiles(0).asSInt + generalRegFiles(1).asSInt).asUInt
            generalRegFiles(5) := (generalRegFiles(2).asSInt + generalRegFiles(3).asSInt).asUInt
            stateReg := 3.U
        }
        is(3.U) {
            generalRegFiles(6) := (generalRegFiles(4).asSInt + generalRegFiles(5).asSInt).asUInt
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