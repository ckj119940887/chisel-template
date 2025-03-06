package GeneralRegFileToBRAM 

import chisel3._
import chisel3.util._
import chisel3.experimental._

class add (val C_S_AXI_DATA_WIDTH:  Int = 32,
               val C_S_AXI_ADDR_WIDTH:  Int = 32,
               val ARRAY_REG_WIDTH:     Int = 8,
               val ARRAY_REG_DEPTH:     Int = 1024,
               val GENERAL_REG_WIDTH:   Int = 64,
               val GENERAL_REG_DEPTH:   Int = 3,
               val STACK_POINTER_WIDTH: Int = 16,
               val CODE_POINTER_WIDTH:  Int = 8) extends Module {

    val io = IO(new Bundle{
        val valid          = Input(Bool())
        val ready          = Output(UInt(2.W))
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
    // reg for code pointer
    val CP = RegInit(2.U(CODE_POINTER_WIDTH.W))
    // reg for stack pointer
    val SP = RegInit(0.U(STACK_POINTER_WIDTH.W))
    // reg for display pointer
    val DP = RegInit(0.U(STACK_POINTER_WIDTH.W))
    // reg for index in memcopy
    val Idx = RegInit(0.U(16.W))

    // write operation
    for(byteIndex <- 0 until (C_S_AXI_DATA_WIDTH/8)) {
      when(io.arrayWe & (io.arrayStrb(byteIndex.U) === 1.U)) {
        arrayRegFiles(io.arrayWriteAddr + byteIndex.U) := io.arrayWData((byteIndex * 8) + 7, byteIndex * 8)
      }
    }

    // read operation
    io.arrayRData := Mux(io.arrayRe, Cat(arrayRegFiles(io.arrayReadAddr + 3.U),
                                         arrayRegFiles(io.arrayReadAddr + 2.U),
                                         arrayRegFiles(io.arrayReadAddr + 1.U),
                                         arrayRegFiles(io.arrayReadAddr + 0.U)), 0.U)

    io.ready := Mux(CP === 0.U, 0.U, Mux(CP === 1.U, 1.U, 2.U))


    switch(CP) {
      is(2.U) {
        CP := Mux(io.valid, 3.U, CP)
      }

      is(3.U) {
        /*
        SP = 0
        *0 = 0 [unsigned, CP, 1]  // $ret
        *1 = 3 [unsigned, SP, 2]  // data address of $res (size = 8)
        goto .4
        */


        SP := 0.U

        val __tmp_0 = 0.U
        val __tmp_1 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_0 + 0.U) := __tmp_1(7, 0)

        val __tmp_2 = 1.U
        val __tmp_3 = (3.U(16.W)).asUInt
        arrayRegFiles(__tmp_2 + 0.U) := __tmp_3(7, 0)
        arrayRegFiles(__tmp_2 + 1.U) := __tmp_3(15, 8)

        CP := 4.U
      }

      is(4.U) {
        /*
        $0 = *(SP + 11) [unsigned, U64, 8]  // $0 = x
        $1 = *(SP + 19) [unsigned, U64, 8]  // $1 = y
        goto .5
        */


        val __tmp_4 = (SP + 11.U).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_4 + 7.U),
          arrayRegFiles(__tmp_4 + 6.U),
          arrayRegFiles(__tmp_4 + 5.U),
          arrayRegFiles(__tmp_4 + 4.U),
          arrayRegFiles(__tmp_4 + 3.U),
          arrayRegFiles(__tmp_4 + 2.U),
          arrayRegFiles(__tmp_4 + 1.U),
          arrayRegFiles(__tmp_4 + 0.U)
        ).asUInt

        val __tmp_5 = (SP + 19.U).asUInt
        generalRegFiles(1.U) := Cat(
          arrayRegFiles(__tmp_5 + 7.U),
          arrayRegFiles(__tmp_5 + 6.U),
          arrayRegFiles(__tmp_5 + 5.U),
          arrayRegFiles(__tmp_5 + 4.U),
          arrayRegFiles(__tmp_5 + 3.U),
          arrayRegFiles(__tmp_5 + 2.U),
          arrayRegFiles(__tmp_5 + 1.U),
          arrayRegFiles(__tmp_5 + 0.U)
        ).asUInt

        CP := 5.U
      }

      is(5.U) {
        /*
        $2 = ($0 + $1)
        goto .6
        */


        generalRegFiles(2.U) := generalRegFiles(0.U) + generalRegFiles(1.U)
        CP := 6.U
      }

      is(6.U) {
        /*
        **(SP + 1) = $2 [unsigned, U64, 8]  // $res = $2
        goto $ret@0
        */


        val __tmp_6 = Cat(
          arrayRegFiles(SP + 1.U + 1.U),
          arrayRegFiles(SP + 1.U + 0.U)
        )
        val __tmp_7 = (generalRegFiles(2.U)).asUInt
        arrayRegFiles(__tmp_6 + 0.U) := __tmp_7(7, 0)
        arrayRegFiles(__tmp_6 + 1.U) := __tmp_7(15, 8)
        arrayRegFiles(__tmp_6 + 2.U) := __tmp_7(23, 16)
        arrayRegFiles(__tmp_6 + 3.U) := __tmp_7(31, 24)
        arrayRegFiles(__tmp_6 + 4.U) := __tmp_7(39, 32)
        arrayRegFiles(__tmp_6 + 5.U) := __tmp_7(47, 40)
        arrayRegFiles(__tmp_6 + 6.U) := __tmp_7(55, 48)
        arrayRegFiles(__tmp_6 + 7.U) := __tmp_7(63, 56)

        CP := Cat(
          arrayRegFiles(SP + 0.U + 0.U)
        )

      }

    }

}
