package GeneralRegFileToBRAM 

import chisel3._
import chisel3.util._
import chisel3.experimental._

class factorial (val C_S_AXI_DATA_WIDTH:  Int = 32,
               val C_S_AXI_ADDR_WIDTH:  Int = 32,
               val ARRAY_REG_WIDTH:     Int = 8,
               val ARRAY_REG_DEPTH:     Int = 1024,
               val GENERAL_REG_WIDTH:   Int = 64,
               val GENERAL_REG_DEPTH:   Int = 5,
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
        *1 = 3 [unsigned, SP, 2]  // data address of $res (size = 4)
        goto .4
        */


        SP := 0.U

        val __tmp_103 = 0.U
        val __tmp_104 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_103 + 0.U) := __tmp_104(7, 0)

        val __tmp_105 = 1.U
        val __tmp_106 = (3.U(16.W)).asUInt
        arrayRegFiles(__tmp_105 + 0.U) := __tmp_106(7, 0)
        arrayRegFiles(__tmp_105 + 1.U) := __tmp_106(15, 8)

        CP := 4.U
      }

      is(4.U) {
        /*
        $0 = *(SP + 7) [unsigned, U32, 4]  // $0 = n
        goto .5
        */


        val __tmp_107 = (SP + 7.U).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_107 + 3.U),
          arrayRegFiles(__tmp_107 + 2.U),
          arrayRegFiles(__tmp_107 + 1.U),
          arrayRegFiles(__tmp_107 + 0.U)
        ).asUInt

        CP := 5.U
      }

      is(5.U) {
        /*
        $1 = ($0 ≡ 0)
        goto .6
        */


        generalRegFiles(1.U) := (generalRegFiles(0.U) === 0.U).asUInt
        CP := 6.U
      }

      is(6.U) {
        /*
        if $1 goto .7 else goto .8
        */


        CP := Mux((generalRegFiles(1.U).asUInt) === 1.U, 7.U, 8.U)
      }

      is(7.U) {
        /*
        **(SP + 1) = 1 [unsigned, U32, 4]  // $res = 1
        goto $ret@0
        */


        val __tmp_108 = Cat(
          arrayRegFiles(SP + 1.U + 1.U),
          arrayRegFiles(SP + 1.U + 0.U)
        )
        val __tmp_109 = (1.U(32.W)).asUInt
        arrayRegFiles(__tmp_108 + 0.U) := __tmp_109(7, 0)
        arrayRegFiles(__tmp_108 + 1.U) := __tmp_109(15, 8)
        arrayRegFiles(__tmp_108 + 2.U) := __tmp_109(23, 16)
        arrayRegFiles(__tmp_108 + 3.U) := __tmp_109(31, 24)

        CP := Cat(
          arrayRegFiles(SP + 0.U + 0.U)
        )

      }

      is(8.U) {
        /*
        $0 = *(SP + 7) [unsigned, U32, 4]  // $0 = n
        $1 = *(SP + 7) [unsigned, U32, 4]  // $1 = n
        goto .9
        */


        val __tmp_110 = (SP + 7.U).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_110 + 3.U),
          arrayRegFiles(__tmp_110 + 2.U),
          arrayRegFiles(__tmp_110 + 1.U),
          arrayRegFiles(__tmp_110 + 0.U)
        ).asUInt

        val __tmp_111 = (SP + 7.U).asUInt
        generalRegFiles(1.U) := Cat(
          arrayRegFiles(__tmp_111 + 3.U),
          arrayRegFiles(__tmp_111 + 2.U),
          arrayRegFiles(__tmp_111 + 1.U),
          arrayRegFiles(__tmp_111 + 0.U)
        ).asUInt

        CP := 9.U
      }

      is(9.U) {
        /*
        $2 = ($1 - 1)
        alloc factorial$res@[9,14].A651BB8C: U32 [@11, 4]
        SP = SP + 39
        decl $ret: CP [@0, 1], $res: SP [@1, 6], n: U32 [@7, 4]
        goto .10
        */


        generalRegFiles(2.U) := generalRegFiles(1.U) - 1.U
        SP := SP + 39.U

        CP := 10.U
      }

      is(10.U) {
        /*
        *SP = 11 [signed, CP, 1]  // $ret@0 = 12
        *(SP + 1) = (SP - 28) [unsigned, SP, 2]  // $res@1 = -28
        *(SP + 7) = $2 [unsigned, U32, 4]  // n = $2
        *(SP - 24) = $0 [unsigned, U64, 8]  // save $0
        *(SP - 16) = $1 [unsigned, U64, 8]  // save $1
        *(SP - 8) = $2 [unsigned, U64, 8]  // save $2
        goto .4
        */


        val __tmp_112 = SP
        val __tmp_113 = (11.S(8.W)).asUInt
        arrayRegFiles(__tmp_112 + 0.U) := __tmp_113(7, 0)

        val __tmp_114 = SP + 1.U
        val __tmp_115 = (SP - 28.U).asUInt
        arrayRegFiles(__tmp_114 + 0.U) := __tmp_115(7, 0)
        arrayRegFiles(__tmp_114 + 1.U) := __tmp_115(15, 8)

        val __tmp_116 = SP + 7.U
        val __tmp_117 = (generalRegFiles(2.U)).asUInt
        arrayRegFiles(__tmp_116 + 0.U) := __tmp_117(7, 0)
        arrayRegFiles(__tmp_116 + 1.U) := __tmp_117(15, 8)
        arrayRegFiles(__tmp_116 + 2.U) := __tmp_117(23, 16)
        arrayRegFiles(__tmp_116 + 3.U) := __tmp_117(31, 24)

        val __tmp_118 = SP - 24.U
        val __tmp_119 = (generalRegFiles(0.U)).asUInt
        arrayRegFiles(__tmp_118 + 0.U) := __tmp_119(7, 0)
        arrayRegFiles(__tmp_118 + 1.U) := __tmp_119(15, 8)
        arrayRegFiles(__tmp_118 + 2.U) := __tmp_119(23, 16)
        arrayRegFiles(__tmp_118 + 3.U) := __tmp_119(31, 24)
        arrayRegFiles(__tmp_118 + 4.U) := __tmp_119(39, 32)
        arrayRegFiles(__tmp_118 + 5.U) := __tmp_119(47, 40)
        arrayRegFiles(__tmp_118 + 6.U) := __tmp_119(55, 48)
        arrayRegFiles(__tmp_118 + 7.U) := __tmp_119(63, 56)

        val __tmp_120 = SP - 16.U
        val __tmp_121 = (generalRegFiles(1.U)).asUInt
        arrayRegFiles(__tmp_120 + 0.U) := __tmp_121(7, 0)
        arrayRegFiles(__tmp_120 + 1.U) := __tmp_121(15, 8)
        arrayRegFiles(__tmp_120 + 2.U) := __tmp_121(23, 16)
        arrayRegFiles(__tmp_120 + 3.U) := __tmp_121(31, 24)
        arrayRegFiles(__tmp_120 + 4.U) := __tmp_121(39, 32)
        arrayRegFiles(__tmp_120 + 5.U) := __tmp_121(47, 40)
        arrayRegFiles(__tmp_120 + 6.U) := __tmp_121(55, 48)
        arrayRegFiles(__tmp_120 + 7.U) := __tmp_121(63, 56)

        val __tmp_122 = SP - 8.U
        val __tmp_123 = (generalRegFiles(2.U)).asUInt
        arrayRegFiles(__tmp_122 + 0.U) := __tmp_123(7, 0)
        arrayRegFiles(__tmp_122 + 1.U) := __tmp_123(15, 8)
        arrayRegFiles(__tmp_122 + 2.U) := __tmp_123(23, 16)
        arrayRegFiles(__tmp_122 + 3.U) := __tmp_123(31, 24)
        arrayRegFiles(__tmp_122 + 4.U) := __tmp_123(39, 32)
        arrayRegFiles(__tmp_122 + 5.U) := __tmp_123(47, 40)
        arrayRegFiles(__tmp_122 + 6.U) := __tmp_123(55, 48)
        arrayRegFiles(__tmp_122 + 7.U) := __tmp_123(63, 56)

        CP := 4.U
      }

      is(11.U) {
        /*
        $0 = *(SP - 24) [unsigned, U64, 8]  // restore $0
        $1 = *(SP - 16) [unsigned, U64, 8]  // restore $1
        $2 = *(SP - 8) [unsigned, U64, 8]  // restore $2
        $3 = **(SP + 1) [unsigned, SP, 2]  // $3 = $ret
        undecl n: U32 [@7, 4], $res: SP [@1, 6], $ret: CP [@0, 1]
        SP = SP - 39
        goto .12
        */


        val __tmp_124 = (SP - 24.U).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_124 + 7.U),
          arrayRegFiles(__tmp_124 + 6.U),
          arrayRegFiles(__tmp_124 + 5.U),
          arrayRegFiles(__tmp_124 + 4.U),
          arrayRegFiles(__tmp_124 + 3.U),
          arrayRegFiles(__tmp_124 + 2.U),
          arrayRegFiles(__tmp_124 + 1.U),
          arrayRegFiles(__tmp_124 + 0.U)
        ).asUInt

        val __tmp_125 = (SP - 16.U).asUInt
        generalRegFiles(1.U) := Cat(
          arrayRegFiles(__tmp_125 + 7.U),
          arrayRegFiles(__tmp_125 + 6.U),
          arrayRegFiles(__tmp_125 + 5.U),
          arrayRegFiles(__tmp_125 + 4.U),
          arrayRegFiles(__tmp_125 + 3.U),
          arrayRegFiles(__tmp_125 + 2.U),
          arrayRegFiles(__tmp_125 + 1.U),
          arrayRegFiles(__tmp_125 + 0.U)
        ).asUInt

        val __tmp_126 = (SP - 8.U).asUInt
        generalRegFiles(2.U) := Cat(
          arrayRegFiles(__tmp_126 + 7.U),
          arrayRegFiles(__tmp_126 + 6.U),
          arrayRegFiles(__tmp_126 + 5.U),
          arrayRegFiles(__tmp_126 + 4.U),
          arrayRegFiles(__tmp_126 + 3.U),
          arrayRegFiles(__tmp_126 + 2.U),
          arrayRegFiles(__tmp_126 + 1.U),
          arrayRegFiles(__tmp_126 + 0.U)
        ).asUInt

        val __tmp_127 = (Cat(
          arrayRegFiles(SP + 1.U + 3.U),
          arrayRegFiles(SP + 1.U + 2.U),
          arrayRegFiles(SP + 1.U + 1.U),
          arrayRegFiles(SP + 1.U + 0.U)
        )).asUInt
        generalRegFiles(3.U) := Cat(
          arrayRegFiles(__tmp_127 + 1.U),
          arrayRegFiles(__tmp_127 + 0.U)
        ).asUInt

        SP := SP - 39.U

        CP := 12.U
      }

      is(12.U) {
        /*
        $4 = ($0 * $3)
        unalloc factorial$res@[9,14].A651BB8C: U32 [@11, 4]
        goto .13
        */


        generalRegFiles(4.U) := generalRegFiles(0.U) * generalRegFiles(3.U)
        CP := 13.U
      }

      is(13.U) {
        /*
        **(SP + 1) = $4 [unsigned, U32, 4]  // $res = $4
        goto $ret@0
        */


        val __tmp_128 = Cat(
          arrayRegFiles(SP + 1.U + 1.U),
          arrayRegFiles(SP + 1.U + 0.U)
        )
        val __tmp_129 = (generalRegFiles(4.U)).asUInt
        arrayRegFiles(__tmp_128 + 0.U) := __tmp_129(7, 0)
        arrayRegFiles(__tmp_128 + 1.U) := __tmp_129(15, 8)
        arrayRegFiles(__tmp_128 + 2.U) := __tmp_129(23, 16)
        arrayRegFiles(__tmp_128 + 3.U) := __tmp_129(31, 24)

        CP := Cat(
          arrayRegFiles(SP + 0.U + 0.U)
        )

      }

    }

}
