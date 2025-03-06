package GeneralRegFileToBRAM 

import chisel3._
import chisel3.util._
import chisel3.experimental._

class mult (val C_S_AXI_DATA_WIDTH:  Int = 32,
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

        val __tmp_148 = 0.U
        val __tmp_149 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_148 + 0.U) := __tmp_149(7, 0)

        val __tmp_150 = 1.U
        val __tmp_151 = (3.U(16.W)).asUInt
        arrayRegFiles(__tmp_150 + 0.U) := __tmp_151(7, 0)
        arrayRegFiles(__tmp_150 + 1.U) := __tmp_151(15, 8)

        CP := 4.U
      }

      is(4.U) {
        /*
        decl r: U64 [@27, 8]
        *(SP + 27) = 0 [signed, U64, 8]  // r = 0
        decl i: U64 [@35, 8]
        *(SP + 35) = 0 [signed, U64, 8]  // i = 0
        goto .5
        */


        val __tmp_152 = SP + 27.U
        val __tmp_153 = (0.S(64.W)).asUInt
        arrayRegFiles(__tmp_152 + 0.U) := __tmp_153(7, 0)
        arrayRegFiles(__tmp_152 + 1.U) := __tmp_153(15, 8)
        arrayRegFiles(__tmp_152 + 2.U) := __tmp_153(23, 16)
        arrayRegFiles(__tmp_152 + 3.U) := __tmp_153(31, 24)
        arrayRegFiles(__tmp_152 + 4.U) := __tmp_153(39, 32)
        arrayRegFiles(__tmp_152 + 5.U) := __tmp_153(47, 40)
        arrayRegFiles(__tmp_152 + 6.U) := __tmp_153(55, 48)
        arrayRegFiles(__tmp_152 + 7.U) := __tmp_153(63, 56)

        val __tmp_154 = SP + 35.U
        val __tmp_155 = (0.S(64.W)).asUInt
        arrayRegFiles(__tmp_154 + 0.U) := __tmp_155(7, 0)
        arrayRegFiles(__tmp_154 + 1.U) := __tmp_155(15, 8)
        arrayRegFiles(__tmp_154 + 2.U) := __tmp_155(23, 16)
        arrayRegFiles(__tmp_154 + 3.U) := __tmp_155(31, 24)
        arrayRegFiles(__tmp_154 + 4.U) := __tmp_155(39, 32)
        arrayRegFiles(__tmp_154 + 5.U) := __tmp_155(47, 40)
        arrayRegFiles(__tmp_154 + 6.U) := __tmp_155(55, 48)
        arrayRegFiles(__tmp_154 + 7.U) := __tmp_155(63, 56)

        CP := 5.U
      }

      is(5.U) {
        /*
        $0 = *(SP + 35) [unsigned, U64, 8]  // $0 = i
        $1 = *(SP + 11) [unsigned, U64, 8]  // $1 = x
        goto .6
        */


        val __tmp_156 = (SP + 35.U).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_156 + 7.U),
          arrayRegFiles(__tmp_156 + 6.U),
          arrayRegFiles(__tmp_156 + 5.U),
          arrayRegFiles(__tmp_156 + 4.U),
          arrayRegFiles(__tmp_156 + 3.U),
          arrayRegFiles(__tmp_156 + 2.U),
          arrayRegFiles(__tmp_156 + 1.U),
          arrayRegFiles(__tmp_156 + 0.U)
        ).asUInt

        val __tmp_157 = (SP + 11.U).asUInt
        generalRegFiles(1.U) := Cat(
          arrayRegFiles(__tmp_157 + 7.U),
          arrayRegFiles(__tmp_157 + 6.U),
          arrayRegFiles(__tmp_157 + 5.U),
          arrayRegFiles(__tmp_157 + 4.U),
          arrayRegFiles(__tmp_157 + 3.U),
          arrayRegFiles(__tmp_157 + 2.U),
          arrayRegFiles(__tmp_157 + 1.U),
          arrayRegFiles(__tmp_157 + 0.U)
        ).asUInt

        CP := 6.U
      }

      is(6.U) {
        /*
        $2 = ($0 < $1)
        goto .7
        */


        generalRegFiles(2.U) := (generalRegFiles(0.U) < generalRegFiles(1.U)).asUInt
        CP := 7.U
      }

      is(7.U) {
        /*
        if $2 goto .8 else goto .14
        */


        CP := Mux((generalRegFiles(2.U).asUInt) === 1.U, 8.U, 14.U)
      }

      is(8.U) {
        /*
        $0 = *(SP + 27) [unsigned, U64, 8]  // $0 = r
        $1 = *(SP + 19) [unsigned, U64, 8]  // $1 = y
        goto .9
        */


        val __tmp_158 = (SP + 27.U).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_158 + 7.U),
          arrayRegFiles(__tmp_158 + 6.U),
          arrayRegFiles(__tmp_158 + 5.U),
          arrayRegFiles(__tmp_158 + 4.U),
          arrayRegFiles(__tmp_158 + 3.U),
          arrayRegFiles(__tmp_158 + 2.U),
          arrayRegFiles(__tmp_158 + 1.U),
          arrayRegFiles(__tmp_158 + 0.U)
        ).asUInt

        val __tmp_159 = (SP + 19.U).asUInt
        generalRegFiles(1.U) := Cat(
          arrayRegFiles(__tmp_159 + 7.U),
          arrayRegFiles(__tmp_159 + 6.U),
          arrayRegFiles(__tmp_159 + 5.U),
          arrayRegFiles(__tmp_159 + 4.U),
          arrayRegFiles(__tmp_159 + 3.U),
          arrayRegFiles(__tmp_159 + 2.U),
          arrayRegFiles(__tmp_159 + 1.U),
          arrayRegFiles(__tmp_159 + 0.U)
        ).asUInt

        CP := 9.U
      }

      is(9.U) {
        /*
        $2 = ($0 + $1)
        goto .10
        */


        generalRegFiles(2.U) := generalRegFiles(0.U) + generalRegFiles(1.U)
        CP := 10.U
      }

      is(10.U) {
        /*
        *(SP + 27) = $2 [signed, U64, 8]  // r = $2
        goto .11
        */


        val __tmp_160 = SP + 27.U
        val __tmp_161 = (generalRegFiles(2.U)).asUInt
        arrayRegFiles(__tmp_160 + 0.U) := __tmp_161(7, 0)
        arrayRegFiles(__tmp_160 + 1.U) := __tmp_161(15, 8)
        arrayRegFiles(__tmp_160 + 2.U) := __tmp_161(23, 16)
        arrayRegFiles(__tmp_160 + 3.U) := __tmp_161(31, 24)
        arrayRegFiles(__tmp_160 + 4.U) := __tmp_161(39, 32)
        arrayRegFiles(__tmp_160 + 5.U) := __tmp_161(47, 40)
        arrayRegFiles(__tmp_160 + 6.U) := __tmp_161(55, 48)
        arrayRegFiles(__tmp_160 + 7.U) := __tmp_161(63, 56)

        CP := 11.U
      }

      is(11.U) {
        /*
        $0 = *(SP + 35) [unsigned, U64, 8]  // $0 = i
        goto .12
        */


        val __tmp_162 = (SP + 35.U).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_162 + 7.U),
          arrayRegFiles(__tmp_162 + 6.U),
          arrayRegFiles(__tmp_162 + 5.U),
          arrayRegFiles(__tmp_162 + 4.U),
          arrayRegFiles(__tmp_162 + 3.U),
          arrayRegFiles(__tmp_162 + 2.U),
          arrayRegFiles(__tmp_162 + 1.U),
          arrayRegFiles(__tmp_162 + 0.U)
        ).asUInt

        CP := 12.U
      }

      is(12.U) {
        /*
        $1 = ($0 + 1)
        goto .13
        */


        generalRegFiles(1.U) := generalRegFiles(0.U) + 1.U
        CP := 13.U
      }

      is(13.U) {
        /*
        *(SP + 35) = $1 [signed, U64, 8]  // i = $1
        goto .5
        */


        val __tmp_163 = SP + 35.U
        val __tmp_164 = (generalRegFiles(1.U)).asUInt
        arrayRegFiles(__tmp_163 + 0.U) := __tmp_164(7, 0)
        arrayRegFiles(__tmp_163 + 1.U) := __tmp_164(15, 8)
        arrayRegFiles(__tmp_163 + 2.U) := __tmp_164(23, 16)
        arrayRegFiles(__tmp_163 + 3.U) := __tmp_164(31, 24)
        arrayRegFiles(__tmp_163 + 4.U) := __tmp_164(39, 32)
        arrayRegFiles(__tmp_163 + 5.U) := __tmp_164(47, 40)
        arrayRegFiles(__tmp_163 + 6.U) := __tmp_164(55, 48)
        arrayRegFiles(__tmp_163 + 7.U) := __tmp_164(63, 56)

        CP := 5.U
      }

      is(14.U) {
        /*
        $0 = *(SP + 27) [unsigned, U64, 8]  // $0 = r
        goto .15
        */


        val __tmp_165 = (SP + 27.U).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_165 + 7.U),
          arrayRegFiles(__tmp_165 + 6.U),
          arrayRegFiles(__tmp_165 + 5.U),
          arrayRegFiles(__tmp_165 + 4.U),
          arrayRegFiles(__tmp_165 + 3.U),
          arrayRegFiles(__tmp_165 + 2.U),
          arrayRegFiles(__tmp_165 + 1.U),
          arrayRegFiles(__tmp_165 + 0.U)
        ).asUInt

        CP := 15.U
      }

      is(15.U) {
        /*
        **(SP + 1) = $0 [unsigned, U64, 8]  // $res = $0
        goto $ret@0
        */


        val __tmp_166 = Cat(
          arrayRegFiles(SP + 1.U + 1.U),
          arrayRegFiles(SP + 1.U + 0.U)
        )
        val __tmp_167 = (generalRegFiles(0.U)).asUInt
        arrayRegFiles(__tmp_166 + 0.U) := __tmp_167(7, 0)
        arrayRegFiles(__tmp_166 + 1.U) := __tmp_167(15, 8)
        arrayRegFiles(__tmp_166 + 2.U) := __tmp_167(23, 16)
        arrayRegFiles(__tmp_166 + 3.U) := __tmp_167(31, 24)
        arrayRegFiles(__tmp_166 + 4.U) := __tmp_167(39, 32)
        arrayRegFiles(__tmp_166 + 5.U) := __tmp_167(47, 40)
        arrayRegFiles(__tmp_166 + 6.U) := __tmp_167(55, 48)
        arrayRegFiles(__tmp_166 + 7.U) := __tmp_167(63, 56)

        CP := Cat(
          arrayRegFiles(SP + 0.U + 0.U)
        )

      }

    }

}
