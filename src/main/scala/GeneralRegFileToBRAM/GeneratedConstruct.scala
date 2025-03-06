package GeneralRegFileToBRAM 

import chisel3._
import chisel3.util._
import chisel3.experimental._

class mkIS (val C_S_AXI_DATA_WIDTH:  Int = 32,
               val C_S_AXI_ADDR_WIDTH:  Int = 32,
               val ARRAY_REG_WIDTH:     Int = 8,
               val ARRAY_REG_DEPTH:     Int = 2048,
               val GENERAL_REG_WIDTH:   Int = 64,
               val GENERAL_REG_DEPTH:   Int = 4,
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
        *1 = 3 [unsigned, SP, 2]  // data address of $res (size = 812)
        goto .4
        */


        SP := 0.U

        val __tmp_84 = 0.U
        val __tmp_85 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_84 + 0.U) := __tmp_85(7, 0)

        val __tmp_86 = 1.U
        val __tmp_87 = (3.U(16.W)).asUInt
        arrayRegFiles(__tmp_86 + 0.U) := __tmp_87(7, 0)
        arrayRegFiles(__tmp_86 + 1.U) := __tmp_87(15, 8)

        CP := 4.U
      }

      is(4.U) {
        /*
        $0 = *(SP + 815) [signed, Z, 8]  // $0 = x
        $1 = *(SP + 823) [signed, Z, 8]  // $1 = y
        $2 = *(SP + 831) [signed, Z, 8]  // $2 = z
        alloc $new@[5,10].C803F20F: IS[Z, Z] [@839, 812]
        goto .5
        */


        val __tmp_88 = (SP + 815.U).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_88 + 7.U),
          arrayRegFiles(__tmp_88 + 6.U),
          arrayRegFiles(__tmp_88 + 5.U),
          arrayRegFiles(__tmp_88 + 4.U),
          arrayRegFiles(__tmp_88 + 3.U),
          arrayRegFiles(__tmp_88 + 2.U),
          arrayRegFiles(__tmp_88 + 1.U),
          arrayRegFiles(__tmp_88 + 0.U)
        ).asUInt

        val __tmp_89 = (SP + 823.U).asUInt
        generalRegFiles(1.U) := Cat(
          arrayRegFiles(__tmp_89 + 7.U),
          arrayRegFiles(__tmp_89 + 6.U),
          arrayRegFiles(__tmp_89 + 5.U),
          arrayRegFiles(__tmp_89 + 4.U),
          arrayRegFiles(__tmp_89 + 3.U),
          arrayRegFiles(__tmp_89 + 2.U),
          arrayRegFiles(__tmp_89 + 1.U),
          arrayRegFiles(__tmp_89 + 0.U)
        ).asUInt

        val __tmp_90 = (SP + 831.U).asUInt
        generalRegFiles(2.U) := Cat(
          arrayRegFiles(__tmp_90 + 7.U),
          arrayRegFiles(__tmp_90 + 6.U),
          arrayRegFiles(__tmp_90 + 5.U),
          arrayRegFiles(__tmp_90 + 4.U),
          arrayRegFiles(__tmp_90 + 3.U),
          arrayRegFiles(__tmp_90 + 2.U),
          arrayRegFiles(__tmp_90 + 1.U),
          arrayRegFiles(__tmp_90 + 0.U)
        ).asUInt

        CP := 5.U
      }

      is(5.U) {
        /*
        *(SP + 839) = 2192300037 [unsigned, U32, 4]  // sha3 type signature of IS[Z, Z]: 0x82ABD805
        *(SP + 843) = 3 [signed, Z, 8]  // size of IS[Z, Z]($0, $1, $2)
        goto .6
        */


        val __tmp_91 = SP + 839.U
        val __tmp_92 = (2192300037L.U(32.W)).asUInt
        arrayRegFiles(__tmp_91 + 0.U) := __tmp_92(7, 0)
        arrayRegFiles(__tmp_91 + 1.U) := __tmp_92(15, 8)
        arrayRegFiles(__tmp_91 + 2.U) := __tmp_92(23, 16)
        arrayRegFiles(__tmp_91 + 3.U) := __tmp_92(31, 24)

        val __tmp_93 = SP + 843.U
        val __tmp_94 = (3.S(64.W)).asUInt
        arrayRegFiles(__tmp_93 + 0.U) := __tmp_94(7, 0)
        arrayRegFiles(__tmp_93 + 1.U) := __tmp_94(15, 8)
        arrayRegFiles(__tmp_93 + 2.U) := __tmp_94(23, 16)
        arrayRegFiles(__tmp_93 + 3.U) := __tmp_94(31, 24)
        arrayRegFiles(__tmp_93 + 4.U) := __tmp_94(39, 32)
        arrayRegFiles(__tmp_93 + 5.U) := __tmp_94(47, 40)
        arrayRegFiles(__tmp_93 + 6.U) := __tmp_94(55, 48)
        arrayRegFiles(__tmp_93 + 7.U) := __tmp_94(63, 56)

        CP := 6.U
      }

      is(6.U) {
        /*
        *(($3 + 12) + ((0 as SP) * 8)) = $0 [signed, Z, 8]  // $3(0) = $0
        *(($3 + 12) + ((1 as SP) * 8)) = $1 [signed, Z, 8]  // $3(1) = $1
        *(($3 + 12) + ((2 as SP) * 8)) = $2 [signed, Z, 8]  // $3(2) = $2
        unalloc $new@[5,10].C803F20F: IS[Z, Z] [@839, 812]
        goto .7
        */


        val __tmp_95 = generalRegFiles(3.U) + 12.U + 0.S.asUInt * 8.U
        val __tmp_96 = (generalRegFiles(0.U).asSInt).asUInt
        arrayRegFiles(__tmp_95 + 0.U) := __tmp_96(7, 0)
        arrayRegFiles(__tmp_95 + 1.U) := __tmp_96(15, 8)
        arrayRegFiles(__tmp_95 + 2.U) := __tmp_96(23, 16)
        arrayRegFiles(__tmp_95 + 3.U) := __tmp_96(31, 24)
        arrayRegFiles(__tmp_95 + 4.U) := __tmp_96(39, 32)
        arrayRegFiles(__tmp_95 + 5.U) := __tmp_96(47, 40)
        arrayRegFiles(__tmp_95 + 6.U) := __tmp_96(55, 48)
        arrayRegFiles(__tmp_95 + 7.U) := __tmp_96(63, 56)

        val __tmp_97 = generalRegFiles(3.U) + 12.U + 1.S.asUInt * 8.U
        val __tmp_98 = (generalRegFiles(1.U).asSInt).asUInt
        arrayRegFiles(__tmp_97 + 0.U) := __tmp_98(7, 0)
        arrayRegFiles(__tmp_97 + 1.U) := __tmp_98(15, 8)
        arrayRegFiles(__tmp_97 + 2.U) := __tmp_98(23, 16)
        arrayRegFiles(__tmp_97 + 3.U) := __tmp_98(31, 24)
        arrayRegFiles(__tmp_97 + 4.U) := __tmp_98(39, 32)
        arrayRegFiles(__tmp_97 + 5.U) := __tmp_98(47, 40)
        arrayRegFiles(__tmp_97 + 6.U) := __tmp_98(55, 48)
        arrayRegFiles(__tmp_97 + 7.U) := __tmp_98(63, 56)

        val __tmp_99 = generalRegFiles(3.U) + 12.U + 2.S.asUInt * 8.U
        val __tmp_100 = (generalRegFiles(2.U).asSInt).asUInt
        arrayRegFiles(__tmp_99 + 0.U) := __tmp_100(7, 0)
        arrayRegFiles(__tmp_99 + 1.U) := __tmp_100(15, 8)
        arrayRegFiles(__tmp_99 + 2.U) := __tmp_100(23, 16)
        arrayRegFiles(__tmp_99 + 3.U) := __tmp_100(31, 24)
        arrayRegFiles(__tmp_99 + 4.U) := __tmp_100(39, 32)
        arrayRegFiles(__tmp_99 + 5.U) := __tmp_100(47, 40)
        arrayRegFiles(__tmp_99 + 6.U) := __tmp_100(55, 48)
        arrayRegFiles(__tmp_99 + 7.U) := __tmp_100(63, 56)

        CP := 7.U
      }

      is(7.U) {
        /*
        *(SP + 1) [IS[Z, Z], 812]  <-  $3 [IS[Z, Z], 812]  // $res = $3
        goto $ret@0
        */


        val __tmp_101 = Cat(
          arrayRegFiles(SP + 1.U + 1.U),
          arrayRegFiles(SP + 1.U + 0.U)
        )
        val __tmp_102 = generalRegFiles(3.U)
        when(Idx < 101.U) {
          arrayRegFiles(__tmp_101 + Idx + 0.U) := arrayRegFiles(__tmp_102 + Idx + 0.U)
          arrayRegFiles(__tmp_101 + Idx + 1.U) := arrayRegFiles(__tmp_102 + Idx + 1.U)
          arrayRegFiles(__tmp_101 + Idx + 2.U) := arrayRegFiles(__tmp_102 + Idx + 2.U)
          arrayRegFiles(__tmp_101 + Idx + 3.U) := arrayRegFiles(__tmp_102 + Idx + 3.U)
          arrayRegFiles(__tmp_101 + Idx + 4.U) := arrayRegFiles(__tmp_102 + Idx + 4.U)
          arrayRegFiles(__tmp_101 + Idx + 5.U) := arrayRegFiles(__tmp_102 + Idx + 5.U)
          arrayRegFiles(__tmp_101 + Idx + 6.U) := arrayRegFiles(__tmp_102 + Idx + 6.U)
          arrayRegFiles(__tmp_101 + Idx + 7.U) := arrayRegFiles(__tmp_102 + Idx + 7.U)
          Idx := Idx + 8.U
        }
        .otherwise {
          arrayRegFiles(__tmp_101 + Idx + 0.U) := arrayRegFiles(__tmp_102 + Idx + 0.U)
          arrayRegFiles(__tmp_101 + Idx + 1.U) := arrayRegFiles(__tmp_102 + Idx + 1.U)
          arrayRegFiles(__tmp_101 + Idx + 2.U) := arrayRegFiles(__tmp_102 + Idx + 2.U)
          arrayRegFiles(__tmp_101 + Idx + 3.U) := arrayRegFiles(__tmp_102 + Idx + 3.U)
          Idx := 0.U
          CP := Cat(
            arrayRegFiles(SP + 0.U + 0.U)
          )

        }


      }

    }

}
