package GeneralRegFileToBRAM 

import chisel3._
import chisel3.util._
import chisel3.experimental._

class testAdd (val C_S_AXI_DATA_WIDTH:  Int = 32,
               val C_S_AXI_ADDR_WIDTH:  Int = 32,
               val ARRAY_REG_WIDTH:     Int = 8,
               val ARRAY_REG_DEPTH:     Int = 1024,
               val GENERAL_REG_WIDTH:   Int = 64,
               val GENERAL_REG_DEPTH:   Int = 12,
               val STACK_POINTER_WIDTH: Int = 16,
               val CODE_POINTER_WIDTH:  Int = 16) extends Module {

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
    val DP = RegInit(0.U(64.W))
    // reg for index in memcopy
    val Idx = RegInit(0.U(16.W))
    // reg for recording how many rounds needed for the left bytes
    val LeftByteRounds = RegInit(0.U(8.W))
    val IdxLeftByteRounds = RegInit(0.U(8.W))

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
        SP = 162
        *0 = 886747591 [unsigned, U32, 4]  // memory $type (MS[anvil.PrinterIndex.U, U8]: 0x34DAB1C7)
        *4 = 1024 [signed, Z, 8]  // memory $size
        DP = 0
        *22 = 886747591 [unsigned, U32, 4]  // $display.$type (MS[anvil.PrinterIndex.U, U8]: 0x34DAB1C7)
        *26 = 128 [signed, Z, 8]  // $display.size
        *20 = 22 [unsigned, SP, 2]  // data address of $display (size = 140)
        *162 = 0 [unsigned, CP, 2]  // $ret
        goto .4
        */


        SP := 162.U(16.W)

        val __tmp_0 = 0.U(32.W)
        val __tmp_1 = (886747591.U(32.W)).asUInt
        arrayRegFiles(__tmp_0 + 0.U) := __tmp_1(7, 0)
        arrayRegFiles(__tmp_0 + 1.U) := __tmp_1(15, 8)
        arrayRegFiles(__tmp_0 + 2.U) := __tmp_1(23, 16)
        arrayRegFiles(__tmp_0 + 3.U) := __tmp_1(31, 24)

        val __tmp_2 = 4.U(16.W)
        val __tmp_3 = (1024.S(64.W)).asUInt
        arrayRegFiles(__tmp_2 + 0.U) := __tmp_3(7, 0)
        arrayRegFiles(__tmp_2 + 1.U) := __tmp_3(15, 8)
        arrayRegFiles(__tmp_2 + 2.U) := __tmp_3(23, 16)
        arrayRegFiles(__tmp_2 + 3.U) := __tmp_3(31, 24)
        arrayRegFiles(__tmp_2 + 4.U) := __tmp_3(39, 32)
        arrayRegFiles(__tmp_2 + 5.U) := __tmp_3(47, 40)
        arrayRegFiles(__tmp_2 + 6.U) := __tmp_3(55, 48)
        arrayRegFiles(__tmp_2 + 7.U) := __tmp_3(63, 56)

        DP := 0.U(64.W)

        val __tmp_4 = 22.U(32.W)
        val __tmp_5 = (886747591.U(32.W)).asUInt
        arrayRegFiles(__tmp_4 + 0.U) := __tmp_5(7, 0)
        arrayRegFiles(__tmp_4 + 1.U) := __tmp_5(15, 8)
        arrayRegFiles(__tmp_4 + 2.U) := __tmp_5(23, 16)
        arrayRegFiles(__tmp_4 + 3.U) := __tmp_5(31, 24)

        val __tmp_6 = 26.U(16.W)
        val __tmp_7 = (128.S(64.W)).asUInt
        arrayRegFiles(__tmp_6 + 0.U) := __tmp_7(7, 0)
        arrayRegFiles(__tmp_6 + 1.U) := __tmp_7(15, 8)
        arrayRegFiles(__tmp_6 + 2.U) := __tmp_7(23, 16)
        arrayRegFiles(__tmp_6 + 3.U) := __tmp_7(31, 24)
        arrayRegFiles(__tmp_6 + 4.U) := __tmp_7(39, 32)
        arrayRegFiles(__tmp_6 + 5.U) := __tmp_7(47, 40)
        arrayRegFiles(__tmp_6 + 6.U) := __tmp_7(55, 48)
        arrayRegFiles(__tmp_6 + 7.U) := __tmp_7(63, 56)

        val __tmp_8 = 20.U(16.W)
        val __tmp_9 = (22.U(16.W)).asUInt
        arrayRegFiles(__tmp_8 + 0.U) := __tmp_9(7, 0)
        arrayRegFiles(__tmp_8 + 1.U) := __tmp_9(15, 8)

        val __tmp_10 = 162.U(16.W)
        val __tmp_11 = (0.U(16.W)).asUInt
        arrayRegFiles(__tmp_10 + 0.U) := __tmp_11(7, 0)
        arrayRegFiles(__tmp_10 + 1.U) := __tmp_11(15, 8)

        CP := 4.U
      }

      is(4.U) {
        /*
        *(SP + 14) = 36 [unsigned, U8, 1]  // '$'
        *(SP + 15) = 116 [unsigned, U8, 1]  // 't'
        *(SP + 16) = 101 [unsigned, U8, 1]  // 'e'
        *(SP + 17) = 115 [unsigned, U8, 1]  // 's'
        *(SP + 18) = 116 [unsigned, U8, 1]  // 't'
        *(SP + 19) = 32 [unsigned, U8, 1]  // ' '
        *(SP + 20) = 40 [unsigned, U8, 1]  // '('
        *(SP + 21) = 97 [unsigned, U8, 1]  // 'a'
        *(SP + 22) = 100 [unsigned, U8, 1]  // 'd'
        *(SP + 23) = 100 [unsigned, U8, 1]  // 'd'
        *(SP + 24) = 45 [unsigned, U8, 1]  // '-'
        *(SP + 25) = 116 [unsigned, U8, 1]  // 't'
        *(SP + 26) = 101 [unsigned, U8, 1]  // 'e'
        *(SP + 27) = 115 [unsigned, U8, 1]  // 's'
        *(SP + 28) = 116 [unsigned, U8, 1]  // 't'
        *(SP + 29) = 46 [unsigned, U8, 1]  // '.'
        *(SP + 30) = 115 [unsigned, U8, 1]  // 's'
        *(SP + 31) = 99 [unsigned, U8, 1]  // 'c'
        *(SP + 32) = 58 [unsigned, U8, 1]  // ':'
        *(SP + 4) = 9 [signed, U32, 4]  // $sfLoc = 9
        goto .5
        */


        val __tmp_12 = SP + 14.U(16.W)
        val __tmp_13 = (36.U(8.W)).asUInt
        arrayRegFiles(__tmp_12 + 0.U) := __tmp_13(7, 0)

        val __tmp_14 = SP + 15.U(16.W)
        val __tmp_15 = (116.U(8.W)).asUInt
        arrayRegFiles(__tmp_14 + 0.U) := __tmp_15(7, 0)

        val __tmp_16 = SP + 16.U(16.W)
        val __tmp_17 = (101.U(8.W)).asUInt
        arrayRegFiles(__tmp_16 + 0.U) := __tmp_17(7, 0)

        val __tmp_18 = SP + 17.U(16.W)
        val __tmp_19 = (115.U(8.W)).asUInt
        arrayRegFiles(__tmp_18 + 0.U) := __tmp_19(7, 0)

        val __tmp_20 = SP + 18.U(16.W)
        val __tmp_21 = (116.U(8.W)).asUInt
        arrayRegFiles(__tmp_20 + 0.U) := __tmp_21(7, 0)

        val __tmp_22 = SP + 19.U(16.W)
        val __tmp_23 = (32.U(8.W)).asUInt
        arrayRegFiles(__tmp_22 + 0.U) := __tmp_23(7, 0)

        val __tmp_24 = SP + 20.U(16.W)
        val __tmp_25 = (40.U(8.W)).asUInt
        arrayRegFiles(__tmp_24 + 0.U) := __tmp_25(7, 0)

        val __tmp_26 = SP + 21.U(16.W)
        val __tmp_27 = (97.U(8.W)).asUInt
        arrayRegFiles(__tmp_26 + 0.U) := __tmp_27(7, 0)

        val __tmp_28 = SP + 22.U(16.W)
        val __tmp_29 = (100.U(8.W)).asUInt
        arrayRegFiles(__tmp_28 + 0.U) := __tmp_29(7, 0)

        val __tmp_30 = SP + 23.U(16.W)
        val __tmp_31 = (100.U(8.W)).asUInt
        arrayRegFiles(__tmp_30 + 0.U) := __tmp_31(7, 0)

        val __tmp_32 = SP + 24.U(16.W)
        val __tmp_33 = (45.U(8.W)).asUInt
        arrayRegFiles(__tmp_32 + 0.U) := __tmp_33(7, 0)

        val __tmp_34 = SP + 25.U(16.W)
        val __tmp_35 = (116.U(8.W)).asUInt
        arrayRegFiles(__tmp_34 + 0.U) := __tmp_35(7, 0)

        val __tmp_36 = SP + 26.U(16.W)
        val __tmp_37 = (101.U(8.W)).asUInt
        arrayRegFiles(__tmp_36 + 0.U) := __tmp_37(7, 0)

        val __tmp_38 = SP + 27.U(16.W)
        val __tmp_39 = (115.U(8.W)).asUInt
        arrayRegFiles(__tmp_38 + 0.U) := __tmp_39(7, 0)

        val __tmp_40 = SP + 28.U(16.W)
        val __tmp_41 = (116.U(8.W)).asUInt
        arrayRegFiles(__tmp_40 + 0.U) := __tmp_41(7, 0)

        val __tmp_42 = SP + 29.U(16.W)
        val __tmp_43 = (46.U(8.W)).asUInt
        arrayRegFiles(__tmp_42 + 0.U) := __tmp_43(7, 0)

        val __tmp_44 = SP + 30.U(16.W)
        val __tmp_45 = (115.U(8.W)).asUInt
        arrayRegFiles(__tmp_44 + 0.U) := __tmp_45(7, 0)

        val __tmp_46 = SP + 31.U(16.W)
        val __tmp_47 = (99.U(8.W)).asUInt
        arrayRegFiles(__tmp_46 + 0.U) := __tmp_47(7, 0)

        val __tmp_48 = SP + 32.U(16.W)
        val __tmp_49 = (58.U(8.W)).asUInt
        arrayRegFiles(__tmp_48 + 0.U) := __tmp_49(7, 0)

        val __tmp_50 = SP + 4.U(16.W)
        val __tmp_51 = (9.S(32.W)).asUInt
        arrayRegFiles(__tmp_50 + 0.U) := __tmp_51(7, 0)
        arrayRegFiles(__tmp_50 + 1.U) := __tmp_51(15, 8)
        arrayRegFiles(__tmp_50 + 2.U) := __tmp_51(23, 16)
        arrayRegFiles(__tmp_50 + 3.U) := __tmp_51(31, 24)

        CP := 5.U
      }

      is(5.U) {
        /*
        $0 = *12 [signed, Z, 8]  // $0 = $testNum
        goto .6
        */


        val __tmp_52 = (12.U(16.W)).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_52 + 7.U),
          arrayRegFiles(__tmp_52 + 6.U),
          arrayRegFiles(__tmp_52 + 5.U),
          arrayRegFiles(__tmp_52 + 4.U),
          arrayRegFiles(__tmp_52 + 3.U),
          arrayRegFiles(__tmp_52 + 2.U),
          arrayRegFiles(__tmp_52 + 1.U),
          arrayRegFiles(__tmp_52 + 0.U)
        ).asUInt

        CP := 6.U
      }

      is(6.U) {
        /*
        if (($0 < 0) | ($0 ≡ 0)) goto .7 else goto .19
        */


        CP := Mux(((generalRegFiles(0.U).asSInt < 0.S(64.W)).asUInt | (generalRegFiles(0.U).asSInt === 0.S(64.W)).asUInt.asUInt) === 1.U, 7.U, 19.U)
      }

      is(7.U) {
        /*
        *(SP + 4) = 9 [signed, U32, 4]  // $sfLoc = 9
        goto .8
        */


        val __tmp_53 = SP + 4.U(16.W)
        val __tmp_54 = (9.S(32.W)).asUInt
        arrayRegFiles(__tmp_53 + 0.U) := __tmp_54(7, 0)
        arrayRegFiles(__tmp_53 + 1.U) := __tmp_54(15, 8)
        arrayRegFiles(__tmp_53 + 2.U) := __tmp_54(23, 16)
        arrayRegFiles(__tmp_53 + 3.U) := __tmp_54(31, 24)

        CP := 8.U
      }

      is(8.U) {
        /*
        SP = SP + 135
        goto .9
        */


        SP := SP + 135.U

        CP := 9.U
      }

      is(9.U) {
        /*
        decl $ret: CP [@0, 2], $sfCaller: SP [@2, 2], $sfDesc: IS[22, U8] [@8, 34], $sfLoc: U32 [@4, 4]
        *SP = 29 [unsigned, CP, 2]  // $ret@0 = 2287
        *(SP + 2) = (SP - 133) [unsigned, SP, 2]  // $sfCaller@2 = -133
        *(SP - 96) = $0 [unsigned, U64, 8]  // save $0
        *(SP - 88) = $1 [unsigned, U64, 8]  // save $1
        *(SP - 80) = $2 [unsigned, U64, 8]  // save $2
        *(SP - 72) = $3 [unsigned, U64, 8]  // save $3
        *(SP - 64) = $4 [unsigned, U64, 8]  // save $4
        *(SP - 56) = $5 [unsigned, U64, 8]  // save $5
        *(SP - 48) = $6 [unsigned, U64, 8]  // save $6
        *(SP - 40) = $7 [unsigned, U64, 8]  // save $7
        *(SP - 32) = $8 [unsigned, U64, 8]  // save $8
        *(SP - 24) = $9 [unsigned, U64, 8]  // save $9
        *(SP - 16) = $10 [unsigned, U64, 8]  // save $10
        *(SP - 8) = $11 [unsigned, U64, 8]  // save $11
        goto .10
        */


        val __tmp_55 = SP
        val __tmp_56 = (29.U(16.W)).asUInt
        arrayRegFiles(__tmp_55 + 0.U) := __tmp_56(7, 0)
        arrayRegFiles(__tmp_55 + 1.U) := __tmp_56(15, 8)

        val __tmp_57 = SP + 2.U(16.W)
        val __tmp_58 = (SP - 133.U(16.W)).asUInt
        arrayRegFiles(__tmp_57 + 0.U) := __tmp_58(7, 0)
        arrayRegFiles(__tmp_57 + 1.U) := __tmp_58(15, 8)

        val __tmp_59 = SP - 96.U(16.W)
        val __tmp_60 = (generalRegFiles(0.U)).asUInt
        arrayRegFiles(__tmp_59 + 0.U) := __tmp_60(7, 0)
        arrayRegFiles(__tmp_59 + 1.U) := __tmp_60(15, 8)
        arrayRegFiles(__tmp_59 + 2.U) := __tmp_60(23, 16)
        arrayRegFiles(__tmp_59 + 3.U) := __tmp_60(31, 24)
        arrayRegFiles(__tmp_59 + 4.U) := __tmp_60(39, 32)
        arrayRegFiles(__tmp_59 + 5.U) := __tmp_60(47, 40)
        arrayRegFiles(__tmp_59 + 6.U) := __tmp_60(55, 48)
        arrayRegFiles(__tmp_59 + 7.U) := __tmp_60(63, 56)

        val __tmp_61 = SP - 88.U(16.W)
        val __tmp_62 = (generalRegFiles(1.U)).asUInt
        arrayRegFiles(__tmp_61 + 0.U) := __tmp_62(7, 0)
        arrayRegFiles(__tmp_61 + 1.U) := __tmp_62(15, 8)
        arrayRegFiles(__tmp_61 + 2.U) := __tmp_62(23, 16)
        arrayRegFiles(__tmp_61 + 3.U) := __tmp_62(31, 24)
        arrayRegFiles(__tmp_61 + 4.U) := __tmp_62(39, 32)
        arrayRegFiles(__tmp_61 + 5.U) := __tmp_62(47, 40)
        arrayRegFiles(__tmp_61 + 6.U) := __tmp_62(55, 48)
        arrayRegFiles(__tmp_61 + 7.U) := __tmp_62(63, 56)

        val __tmp_63 = SP - 80.U(16.W)
        val __tmp_64 = (generalRegFiles(2.U)).asUInt
        arrayRegFiles(__tmp_63 + 0.U) := __tmp_64(7, 0)
        arrayRegFiles(__tmp_63 + 1.U) := __tmp_64(15, 8)
        arrayRegFiles(__tmp_63 + 2.U) := __tmp_64(23, 16)
        arrayRegFiles(__tmp_63 + 3.U) := __tmp_64(31, 24)
        arrayRegFiles(__tmp_63 + 4.U) := __tmp_64(39, 32)
        arrayRegFiles(__tmp_63 + 5.U) := __tmp_64(47, 40)
        arrayRegFiles(__tmp_63 + 6.U) := __tmp_64(55, 48)
        arrayRegFiles(__tmp_63 + 7.U) := __tmp_64(63, 56)

        val __tmp_65 = SP - 72.U(16.W)
        val __tmp_66 = (generalRegFiles(3.U)).asUInt
        arrayRegFiles(__tmp_65 + 0.U) := __tmp_66(7, 0)
        arrayRegFiles(__tmp_65 + 1.U) := __tmp_66(15, 8)
        arrayRegFiles(__tmp_65 + 2.U) := __tmp_66(23, 16)
        arrayRegFiles(__tmp_65 + 3.U) := __tmp_66(31, 24)
        arrayRegFiles(__tmp_65 + 4.U) := __tmp_66(39, 32)
        arrayRegFiles(__tmp_65 + 5.U) := __tmp_66(47, 40)
        arrayRegFiles(__tmp_65 + 6.U) := __tmp_66(55, 48)
        arrayRegFiles(__tmp_65 + 7.U) := __tmp_66(63, 56)

        val __tmp_67 = SP - 64.U(16.W)
        val __tmp_68 = (generalRegFiles(4.U)).asUInt
        arrayRegFiles(__tmp_67 + 0.U) := __tmp_68(7, 0)
        arrayRegFiles(__tmp_67 + 1.U) := __tmp_68(15, 8)
        arrayRegFiles(__tmp_67 + 2.U) := __tmp_68(23, 16)
        arrayRegFiles(__tmp_67 + 3.U) := __tmp_68(31, 24)
        arrayRegFiles(__tmp_67 + 4.U) := __tmp_68(39, 32)
        arrayRegFiles(__tmp_67 + 5.U) := __tmp_68(47, 40)
        arrayRegFiles(__tmp_67 + 6.U) := __tmp_68(55, 48)
        arrayRegFiles(__tmp_67 + 7.U) := __tmp_68(63, 56)

        val __tmp_69 = SP - 56.U(16.W)
        val __tmp_70 = (generalRegFiles(5.U)).asUInt
        arrayRegFiles(__tmp_69 + 0.U) := __tmp_70(7, 0)
        arrayRegFiles(__tmp_69 + 1.U) := __tmp_70(15, 8)
        arrayRegFiles(__tmp_69 + 2.U) := __tmp_70(23, 16)
        arrayRegFiles(__tmp_69 + 3.U) := __tmp_70(31, 24)
        arrayRegFiles(__tmp_69 + 4.U) := __tmp_70(39, 32)
        arrayRegFiles(__tmp_69 + 5.U) := __tmp_70(47, 40)
        arrayRegFiles(__tmp_69 + 6.U) := __tmp_70(55, 48)
        arrayRegFiles(__tmp_69 + 7.U) := __tmp_70(63, 56)

        val __tmp_71 = SP - 48.U(16.W)
        val __tmp_72 = (generalRegFiles(6.U)).asUInt
        arrayRegFiles(__tmp_71 + 0.U) := __tmp_72(7, 0)
        arrayRegFiles(__tmp_71 + 1.U) := __tmp_72(15, 8)
        arrayRegFiles(__tmp_71 + 2.U) := __tmp_72(23, 16)
        arrayRegFiles(__tmp_71 + 3.U) := __tmp_72(31, 24)
        arrayRegFiles(__tmp_71 + 4.U) := __tmp_72(39, 32)
        arrayRegFiles(__tmp_71 + 5.U) := __tmp_72(47, 40)
        arrayRegFiles(__tmp_71 + 6.U) := __tmp_72(55, 48)
        arrayRegFiles(__tmp_71 + 7.U) := __tmp_72(63, 56)

        val __tmp_73 = SP - 40.U(16.W)
        val __tmp_74 = (generalRegFiles(7.U)).asUInt
        arrayRegFiles(__tmp_73 + 0.U) := __tmp_74(7, 0)
        arrayRegFiles(__tmp_73 + 1.U) := __tmp_74(15, 8)
        arrayRegFiles(__tmp_73 + 2.U) := __tmp_74(23, 16)
        arrayRegFiles(__tmp_73 + 3.U) := __tmp_74(31, 24)
        arrayRegFiles(__tmp_73 + 4.U) := __tmp_74(39, 32)
        arrayRegFiles(__tmp_73 + 5.U) := __tmp_74(47, 40)
        arrayRegFiles(__tmp_73 + 6.U) := __tmp_74(55, 48)
        arrayRegFiles(__tmp_73 + 7.U) := __tmp_74(63, 56)

        val __tmp_75 = SP - 32.U(16.W)
        val __tmp_76 = (generalRegFiles(8.U)).asUInt
        arrayRegFiles(__tmp_75 + 0.U) := __tmp_76(7, 0)
        arrayRegFiles(__tmp_75 + 1.U) := __tmp_76(15, 8)
        arrayRegFiles(__tmp_75 + 2.U) := __tmp_76(23, 16)
        arrayRegFiles(__tmp_75 + 3.U) := __tmp_76(31, 24)
        arrayRegFiles(__tmp_75 + 4.U) := __tmp_76(39, 32)
        arrayRegFiles(__tmp_75 + 5.U) := __tmp_76(47, 40)
        arrayRegFiles(__tmp_75 + 6.U) := __tmp_76(55, 48)
        arrayRegFiles(__tmp_75 + 7.U) := __tmp_76(63, 56)

        val __tmp_77 = SP - 24.U(16.W)
        val __tmp_78 = (generalRegFiles(9.U)).asUInt
        arrayRegFiles(__tmp_77 + 0.U) := __tmp_78(7, 0)
        arrayRegFiles(__tmp_77 + 1.U) := __tmp_78(15, 8)
        arrayRegFiles(__tmp_77 + 2.U) := __tmp_78(23, 16)
        arrayRegFiles(__tmp_77 + 3.U) := __tmp_78(31, 24)
        arrayRegFiles(__tmp_77 + 4.U) := __tmp_78(39, 32)
        arrayRegFiles(__tmp_77 + 5.U) := __tmp_78(47, 40)
        arrayRegFiles(__tmp_77 + 6.U) := __tmp_78(55, 48)
        arrayRegFiles(__tmp_77 + 7.U) := __tmp_78(63, 56)

        val __tmp_79 = SP - 16.U(16.W)
        val __tmp_80 = (generalRegFiles(10.U)).asUInt
        arrayRegFiles(__tmp_79 + 0.U) := __tmp_80(7, 0)
        arrayRegFiles(__tmp_79 + 1.U) := __tmp_80(15, 8)
        arrayRegFiles(__tmp_79 + 2.U) := __tmp_80(23, 16)
        arrayRegFiles(__tmp_79 + 3.U) := __tmp_80(31, 24)
        arrayRegFiles(__tmp_79 + 4.U) := __tmp_80(39, 32)
        arrayRegFiles(__tmp_79 + 5.U) := __tmp_80(47, 40)
        arrayRegFiles(__tmp_79 + 6.U) := __tmp_80(55, 48)
        arrayRegFiles(__tmp_79 + 7.U) := __tmp_80(63, 56)

        val __tmp_81 = SP - 8.U(16.W)
        val __tmp_82 = (generalRegFiles(11.U)).asUInt
        arrayRegFiles(__tmp_81 + 0.U) := __tmp_82(7, 0)
        arrayRegFiles(__tmp_81 + 1.U) := __tmp_82(15, 8)
        arrayRegFiles(__tmp_81 + 2.U) := __tmp_82(23, 16)
        arrayRegFiles(__tmp_81 + 3.U) := __tmp_82(31, 24)
        arrayRegFiles(__tmp_81 + 4.U) := __tmp_82(39, 32)
        arrayRegFiles(__tmp_81 + 5.U) := __tmp_82(47, 40)
        arrayRegFiles(__tmp_81 + 6.U) := __tmp_82(55, 48)
        arrayRegFiles(__tmp_81 + 7.U) := __tmp_82(63, 56)

        CP := 10.U
      }

      is(10.U) {
        /*
        *(SP + 14) = 116 [unsigned, U8, 1]  // 't'
        *(SP + 15) = 101 [unsigned, U8, 1]  // 'e'
        *(SP + 16) = 115 [unsigned, U8, 1]  // 's'
        *(SP + 17) = 116 [unsigned, U8, 1]  // 't'
        *(SP + 18) = 65 [unsigned, U8, 1]  // 'A'
        *(SP + 19) = 100 [unsigned, U8, 1]  // 'd'
        *(SP + 20) = 100 [unsigned, U8, 1]  // 'd'
        *(SP + 21) = 49 [unsigned, U8, 1]  // '1'
        *(SP + 22) = 32 [unsigned, U8, 1]  // ' '
        *(SP + 23) = 40 [unsigned, U8, 1]  // '('
        *(SP + 24) = 97 [unsigned, U8, 1]  // 'a'
        *(SP + 25) = 100 [unsigned, U8, 1]  // 'd'
        *(SP + 26) = 100 [unsigned, U8, 1]  // 'd'
        *(SP + 27) = 45 [unsigned, U8, 1]  // '-'
        *(SP + 28) = 116 [unsigned, U8, 1]  // 't'
        *(SP + 29) = 101 [unsigned, U8, 1]  // 'e'
        *(SP + 30) = 115 [unsigned, U8, 1]  // 's'
        *(SP + 31) = 116 [unsigned, U8, 1]  // 't'
        *(SP + 32) = 46 [unsigned, U8, 1]  // '.'
        *(SP + 33) = 115 [unsigned, U8, 1]  // 's'
        *(SP + 34) = 99 [unsigned, U8, 1]  // 'c'
        *(SP + 35) = 58 [unsigned, U8, 1]  // ':'
        *(SP + 4) = 10 [signed, U32, 4]  // $sfLoc = 10
        goto .11
        */


        val __tmp_83 = SP + 14.U(16.W)
        val __tmp_84 = (116.U(8.W)).asUInt
        arrayRegFiles(__tmp_83 + 0.U) := __tmp_84(7, 0)

        val __tmp_85 = SP + 15.U(16.W)
        val __tmp_86 = (101.U(8.W)).asUInt
        arrayRegFiles(__tmp_85 + 0.U) := __tmp_86(7, 0)

        val __tmp_87 = SP + 16.U(16.W)
        val __tmp_88 = (115.U(8.W)).asUInt
        arrayRegFiles(__tmp_87 + 0.U) := __tmp_88(7, 0)

        val __tmp_89 = SP + 17.U(16.W)
        val __tmp_90 = (116.U(8.W)).asUInt
        arrayRegFiles(__tmp_89 + 0.U) := __tmp_90(7, 0)

        val __tmp_91 = SP + 18.U(16.W)
        val __tmp_92 = (65.U(8.W)).asUInt
        arrayRegFiles(__tmp_91 + 0.U) := __tmp_92(7, 0)

        val __tmp_93 = SP + 19.U(16.W)
        val __tmp_94 = (100.U(8.W)).asUInt
        arrayRegFiles(__tmp_93 + 0.U) := __tmp_94(7, 0)

        val __tmp_95 = SP + 20.U(16.W)
        val __tmp_96 = (100.U(8.W)).asUInt
        arrayRegFiles(__tmp_95 + 0.U) := __tmp_96(7, 0)

        val __tmp_97 = SP + 21.U(16.W)
        val __tmp_98 = (49.U(8.W)).asUInt
        arrayRegFiles(__tmp_97 + 0.U) := __tmp_98(7, 0)

        val __tmp_99 = SP + 22.U(16.W)
        val __tmp_100 = (32.U(8.W)).asUInt
        arrayRegFiles(__tmp_99 + 0.U) := __tmp_100(7, 0)

        val __tmp_101 = SP + 23.U(16.W)
        val __tmp_102 = (40.U(8.W)).asUInt
        arrayRegFiles(__tmp_101 + 0.U) := __tmp_102(7, 0)

        val __tmp_103 = SP + 24.U(16.W)
        val __tmp_104 = (97.U(8.W)).asUInt
        arrayRegFiles(__tmp_103 + 0.U) := __tmp_104(7, 0)

        val __tmp_105 = SP + 25.U(16.W)
        val __tmp_106 = (100.U(8.W)).asUInt
        arrayRegFiles(__tmp_105 + 0.U) := __tmp_106(7, 0)

        val __tmp_107 = SP + 26.U(16.W)
        val __tmp_108 = (100.U(8.W)).asUInt
        arrayRegFiles(__tmp_107 + 0.U) := __tmp_108(7, 0)

        val __tmp_109 = SP + 27.U(16.W)
        val __tmp_110 = (45.U(8.W)).asUInt
        arrayRegFiles(__tmp_109 + 0.U) := __tmp_110(7, 0)

        val __tmp_111 = SP + 28.U(16.W)
        val __tmp_112 = (116.U(8.W)).asUInt
        arrayRegFiles(__tmp_111 + 0.U) := __tmp_112(7, 0)

        val __tmp_113 = SP + 29.U(16.W)
        val __tmp_114 = (101.U(8.W)).asUInt
        arrayRegFiles(__tmp_113 + 0.U) := __tmp_114(7, 0)

        val __tmp_115 = SP + 30.U(16.W)
        val __tmp_116 = (115.U(8.W)).asUInt
        arrayRegFiles(__tmp_115 + 0.U) := __tmp_116(7, 0)

        val __tmp_117 = SP + 31.U(16.W)
        val __tmp_118 = (116.U(8.W)).asUInt
        arrayRegFiles(__tmp_117 + 0.U) := __tmp_118(7, 0)

        val __tmp_119 = SP + 32.U(16.W)
        val __tmp_120 = (46.U(8.W)).asUInt
        arrayRegFiles(__tmp_119 + 0.U) := __tmp_120(7, 0)

        val __tmp_121 = SP + 33.U(16.W)
        val __tmp_122 = (115.U(8.W)).asUInt
        arrayRegFiles(__tmp_121 + 0.U) := __tmp_122(7, 0)

        val __tmp_123 = SP + 34.U(16.W)
        val __tmp_124 = (99.U(8.W)).asUInt
        arrayRegFiles(__tmp_123 + 0.U) := __tmp_124(7, 0)

        val __tmp_125 = SP + 35.U(16.W)
        val __tmp_126 = (58.U(8.W)).asUInt
        arrayRegFiles(__tmp_125 + 0.U) := __tmp_126(7, 0)

        val __tmp_127 = SP + 4.U(16.W)
        val __tmp_128 = (10.S(32.W)).asUInt
        arrayRegFiles(__tmp_127 + 0.U) := __tmp_128(7, 0)
        arrayRegFiles(__tmp_127 + 1.U) := __tmp_128(15, 8)
        arrayRegFiles(__tmp_127 + 2.U) := __tmp_128(23, 16)
        arrayRegFiles(__tmp_127 + 3.U) := __tmp_128(31, 24)

        CP := 11.U
      }

      is(11.U) {
        /*
        alloc add$res@[10,11].CE055965: U16 [@42, 2]
        goto .12
        */


        CP := 12.U
      }

      is(12.U) {
        /*
        SP = SP + 52
        goto .13
        */


        SP := SP + 52.U

        CP := 13.U
      }

      is(13.U) {
        /*
        decl $ret: CP [@0, 2], $res: SP [@2, 4], $sfCaller: SP [@6, 2], $sfDesc: IS[17, U8] [@12, 29], $sfLoc: U32 [@8, 4], x: U16 [@41, 2], y: U16 [@43, 2]
        *SP = 33 [unsigned, CP, 2]  // $ret@0 = 2289
        *(SP + 2) = (SP - 10) [unsigned, SP, 2]  // $res@2 = -10
        *(SP + 6) = (SP - 50) [unsigned, SP, 2]  // $sfCaller@6 = -50
        *(SP + 41) = 3 [unsigned, U16, 2]  // x = 3
        *(SP + 43) = 5 [unsigned, U16, 2]  // y = 5
        goto .14
        */


        val __tmp_129 = SP
        val __tmp_130 = (33.U(16.W)).asUInt
        arrayRegFiles(__tmp_129 + 0.U) := __tmp_130(7, 0)
        arrayRegFiles(__tmp_129 + 1.U) := __tmp_130(15, 8)

        val __tmp_131 = SP + 2.U(16.W)
        val __tmp_132 = (SP - 10.U(16.W)).asUInt
        arrayRegFiles(__tmp_131 + 0.U) := __tmp_132(7, 0)
        arrayRegFiles(__tmp_131 + 1.U) := __tmp_132(15, 8)

        val __tmp_133 = SP + 6.U(16.W)
        val __tmp_134 = (SP - 50.U(16.W)).asUInt
        arrayRegFiles(__tmp_133 + 0.U) := __tmp_134(7, 0)
        arrayRegFiles(__tmp_133 + 1.U) := __tmp_134(15, 8)

        val __tmp_135 = SP + 41.U(16.W)
        val __tmp_136 = (3.U(16.W)).asUInt
        arrayRegFiles(__tmp_135 + 0.U) := __tmp_136(7, 0)
        arrayRegFiles(__tmp_135 + 1.U) := __tmp_136(15, 8)

        val __tmp_137 = SP + 43.U(16.W)
        val __tmp_138 = (5.U(16.W)).asUInt
        arrayRegFiles(__tmp_137 + 0.U) := __tmp_138(7, 0)
        arrayRegFiles(__tmp_137 + 1.U) := __tmp_138(15, 8)

        CP := 14.U
      }

      is(14.U) {
        /*
        *(SP + 18) = 97 [unsigned, U8, 1]  // 'a'
        *(SP + 19) = 100 [unsigned, U8, 1]  // 'd'
        *(SP + 20) = 100 [unsigned, U8, 1]  // 'd'
        *(SP + 21) = 32 [unsigned, U8, 1]  // ' '
        *(SP + 22) = 40 [unsigned, U8, 1]  // '('
        *(SP + 23) = 97 [unsigned, U8, 1]  // 'a'
        *(SP + 24) = 100 [unsigned, U8, 1]  // 'd'
        *(SP + 25) = 100 [unsigned, U8, 1]  // 'd'
        *(SP + 26) = 45 [unsigned, U8, 1]  // '-'
        *(SP + 27) = 116 [unsigned, U8, 1]  // 't'
        *(SP + 28) = 101 [unsigned, U8, 1]  // 'e'
        *(SP + 29) = 115 [unsigned, U8, 1]  // 's'
        *(SP + 30) = 116 [unsigned, U8, 1]  // 't'
        *(SP + 31) = 46 [unsigned, U8, 1]  // '.'
        *(SP + 32) = 115 [unsigned, U8, 1]  // 's'
        *(SP + 33) = 99 [unsigned, U8, 1]  // 'c'
        *(SP + 34) = 58 [unsigned, U8, 1]  // ':'
        *(SP + 8) = 6 [signed, U32, 4]  // $sfLoc = 6
        goto .15
        */


        val __tmp_139 = SP + 18.U(16.W)
        val __tmp_140 = (97.U(8.W)).asUInt
        arrayRegFiles(__tmp_139 + 0.U) := __tmp_140(7, 0)

        val __tmp_141 = SP + 19.U(16.W)
        val __tmp_142 = (100.U(8.W)).asUInt
        arrayRegFiles(__tmp_141 + 0.U) := __tmp_142(7, 0)

        val __tmp_143 = SP + 20.U(16.W)
        val __tmp_144 = (100.U(8.W)).asUInt
        arrayRegFiles(__tmp_143 + 0.U) := __tmp_144(7, 0)

        val __tmp_145 = SP + 21.U(16.W)
        val __tmp_146 = (32.U(8.W)).asUInt
        arrayRegFiles(__tmp_145 + 0.U) := __tmp_146(7, 0)

        val __tmp_147 = SP + 22.U(16.W)
        val __tmp_148 = (40.U(8.W)).asUInt
        arrayRegFiles(__tmp_147 + 0.U) := __tmp_148(7, 0)

        val __tmp_149 = SP + 23.U(16.W)
        val __tmp_150 = (97.U(8.W)).asUInt
        arrayRegFiles(__tmp_149 + 0.U) := __tmp_150(7, 0)

        val __tmp_151 = SP + 24.U(16.W)
        val __tmp_152 = (100.U(8.W)).asUInt
        arrayRegFiles(__tmp_151 + 0.U) := __tmp_152(7, 0)

        val __tmp_153 = SP + 25.U(16.W)
        val __tmp_154 = (100.U(8.W)).asUInt
        arrayRegFiles(__tmp_153 + 0.U) := __tmp_154(7, 0)

        val __tmp_155 = SP + 26.U(16.W)
        val __tmp_156 = (45.U(8.W)).asUInt
        arrayRegFiles(__tmp_155 + 0.U) := __tmp_156(7, 0)

        val __tmp_157 = SP + 27.U(16.W)
        val __tmp_158 = (116.U(8.W)).asUInt
        arrayRegFiles(__tmp_157 + 0.U) := __tmp_158(7, 0)

        val __tmp_159 = SP + 28.U(16.W)
        val __tmp_160 = (101.U(8.W)).asUInt
        arrayRegFiles(__tmp_159 + 0.U) := __tmp_160(7, 0)

        val __tmp_161 = SP + 29.U(16.W)
        val __tmp_162 = (115.U(8.W)).asUInt
        arrayRegFiles(__tmp_161 + 0.U) := __tmp_162(7, 0)

        val __tmp_163 = SP + 30.U(16.W)
        val __tmp_164 = (116.U(8.W)).asUInt
        arrayRegFiles(__tmp_163 + 0.U) := __tmp_164(7, 0)

        val __tmp_165 = SP + 31.U(16.W)
        val __tmp_166 = (46.U(8.W)).asUInt
        arrayRegFiles(__tmp_165 + 0.U) := __tmp_166(7, 0)

        val __tmp_167 = SP + 32.U(16.W)
        val __tmp_168 = (115.U(8.W)).asUInt
        arrayRegFiles(__tmp_167 + 0.U) := __tmp_168(7, 0)

        val __tmp_169 = SP + 33.U(16.W)
        val __tmp_170 = (99.U(8.W)).asUInt
        arrayRegFiles(__tmp_169 + 0.U) := __tmp_170(7, 0)

        val __tmp_171 = SP + 34.U(16.W)
        val __tmp_172 = (58.U(8.W)).asUInt
        arrayRegFiles(__tmp_171 + 0.U) := __tmp_172(7, 0)

        val __tmp_173 = SP + 8.U(16.W)
        val __tmp_174 = (6.S(32.W)).asUInt
        arrayRegFiles(__tmp_173 + 0.U) := __tmp_174(7, 0)
        arrayRegFiles(__tmp_173 + 1.U) := __tmp_174(15, 8)
        arrayRegFiles(__tmp_173 + 2.U) := __tmp_174(23, 16)
        arrayRegFiles(__tmp_173 + 3.U) := __tmp_174(31, 24)

        CP := 15.U
      }

      is(15.U) {
        /*
        $0 = *(SP + 41) [unsigned, U16, 2]  // $0 = x
        $1 = *(SP + 43) [unsigned, U16, 2]  // $1 = y
        goto .16
        */


        val __tmp_175 = (SP + 41.U(16.W)).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_175 + 1.U),
          arrayRegFiles(__tmp_175 + 0.U)
        ).asUInt

        val __tmp_176 = (SP + 43.U(16.W)).asUInt
        generalRegFiles(1.U) := Cat(
          arrayRegFiles(__tmp_176 + 1.U),
          arrayRegFiles(__tmp_176 + 0.U)
        ).asUInt

        CP := 16.U
      }

      is(16.U) {
        /*
        $2 = ($0 + $1)
        goto .17
        */


        generalRegFiles(2.U) := generalRegFiles(0.U) + generalRegFiles(1.U)
        CP := 17.U
      }

      is(17.U) {
        /*
        *(SP + 8) = 5 [signed, U32, 4]  // $sfLoc = 5
        goto .18
        */


        val __tmp_177 = SP + 8.U(16.W)
        val __tmp_178 = (5.S(32.W)).asUInt
        arrayRegFiles(__tmp_177 + 0.U) := __tmp_178(7, 0)
        arrayRegFiles(__tmp_177 + 1.U) := __tmp_178(15, 8)
        arrayRegFiles(__tmp_177 + 2.U) := __tmp_178(23, 16)
        arrayRegFiles(__tmp_177 + 3.U) := __tmp_178(31, 24)

        CP := 18.U
      }

      is(18.U) {
        /*
        **(SP + 2) = $2 [unsigned, U16, 2]  // $res = $2
        goto $ret@0
        */


        val __tmp_179 = Cat(
          arrayRegFiles(SP + 2.U(16.W) + 1.U),
          arrayRegFiles(SP + 2.U(16.W) + 0.U)
        )
        val __tmp_180 = (generalRegFiles(2.U)).asUInt
        arrayRegFiles(__tmp_179 + 0.U) := __tmp_180(7, 0)
        arrayRegFiles(__tmp_179 + 1.U) := __tmp_180(15, 8)

        CP := Cat(
          arrayRegFiles(SP + 0.U + 1.U),
          arrayRegFiles(SP + 0.U + 0.U)
        )

      }

      is(19.U) {
        /*
        *(SP + 4) = 9 [signed, U32, 4]  // $sfLoc = 9
        goto .20
        */


        val __tmp_181 = SP + 4.U(16.W)
        val __tmp_182 = (9.S(32.W)).asUInt
        arrayRegFiles(__tmp_181 + 0.U) := __tmp_182(7, 0)
        arrayRegFiles(__tmp_181 + 1.U) := __tmp_182(15, 8)
        arrayRegFiles(__tmp_181 + 2.U) := __tmp_182(23, 16)
        arrayRegFiles(__tmp_181 + 3.U) := __tmp_182(31, 24)

        CP := 20.U
      }

      is(20.U) {
        /*
        if (($0 < 0) | ($0 ≡ 1)) goto .21 else goto .28
        */


        CP := Mux(((generalRegFiles(0.U).asSInt < 0.S(64.W)).asUInt | (generalRegFiles(0.U).asSInt === 1.S(64.W)).asUInt.asUInt) === 1.U, 21.U, 28.U)
      }

      is(21.U) {
        /*
        *(SP + 4) = 9 [signed, U32, 4]  // $sfLoc = 9
        goto .22
        */


        val __tmp_183 = SP + 4.U(16.W)
        val __tmp_184 = (9.S(32.W)).asUInt
        arrayRegFiles(__tmp_183 + 0.U) := __tmp_184(7, 0)
        arrayRegFiles(__tmp_183 + 1.U) := __tmp_184(15, 8)
        arrayRegFiles(__tmp_183 + 2.U) := __tmp_184(23, 16)
        arrayRegFiles(__tmp_183 + 3.U) := __tmp_184(31, 24)

        CP := 22.U
      }

      is(22.U) {
        /*
        SP = SP + 135
        goto .23
        */


        SP := SP + 135.U

        CP := 23.U
      }

      is(23.U) {
        /*
        decl $ret: CP [@0, 2], $sfCaller: SP [@2, 2], $sfDesc: IS[22, U8] [@8, 34], $sfLoc: U32 [@4, 4]
        *SP = 31 [unsigned, CP, 2]  // $ret@0 = 2288
        *(SP + 2) = (SP - 133) [unsigned, SP, 2]  // $sfCaller@2 = -133
        *(SP - 96) = $0 [unsigned, U64, 8]  // save $0
        *(SP - 88) = $1 [unsigned, U64, 8]  // save $1
        *(SP - 80) = $2 [unsigned, U64, 8]  // save $2
        *(SP - 72) = $3 [unsigned, U64, 8]  // save $3
        *(SP - 64) = $4 [unsigned, U64, 8]  // save $4
        *(SP - 56) = $5 [unsigned, U64, 8]  // save $5
        *(SP - 48) = $6 [unsigned, U64, 8]  // save $6
        *(SP - 40) = $7 [unsigned, U64, 8]  // save $7
        *(SP - 32) = $8 [unsigned, U64, 8]  // save $8
        *(SP - 24) = $9 [unsigned, U64, 8]  // save $9
        *(SP - 16) = $10 [unsigned, U64, 8]  // save $10
        *(SP - 8) = $11 [unsigned, U64, 8]  // save $11
        goto .24
        */


        val __tmp_185 = SP
        val __tmp_186 = (31.U(16.W)).asUInt
        arrayRegFiles(__tmp_185 + 0.U) := __tmp_186(7, 0)
        arrayRegFiles(__tmp_185 + 1.U) := __tmp_186(15, 8)

        val __tmp_187 = SP + 2.U(16.W)
        val __tmp_188 = (SP - 133.U(16.W)).asUInt
        arrayRegFiles(__tmp_187 + 0.U) := __tmp_188(7, 0)
        arrayRegFiles(__tmp_187 + 1.U) := __tmp_188(15, 8)

        val __tmp_189 = SP - 96.U(16.W)
        val __tmp_190 = (generalRegFiles(0.U)).asUInt
        arrayRegFiles(__tmp_189 + 0.U) := __tmp_190(7, 0)
        arrayRegFiles(__tmp_189 + 1.U) := __tmp_190(15, 8)
        arrayRegFiles(__tmp_189 + 2.U) := __tmp_190(23, 16)
        arrayRegFiles(__tmp_189 + 3.U) := __tmp_190(31, 24)
        arrayRegFiles(__tmp_189 + 4.U) := __tmp_190(39, 32)
        arrayRegFiles(__tmp_189 + 5.U) := __tmp_190(47, 40)
        arrayRegFiles(__tmp_189 + 6.U) := __tmp_190(55, 48)
        arrayRegFiles(__tmp_189 + 7.U) := __tmp_190(63, 56)

        val __tmp_191 = SP - 88.U(16.W)
        val __tmp_192 = (generalRegFiles(1.U)).asUInt
        arrayRegFiles(__tmp_191 + 0.U) := __tmp_192(7, 0)
        arrayRegFiles(__tmp_191 + 1.U) := __tmp_192(15, 8)
        arrayRegFiles(__tmp_191 + 2.U) := __tmp_192(23, 16)
        arrayRegFiles(__tmp_191 + 3.U) := __tmp_192(31, 24)
        arrayRegFiles(__tmp_191 + 4.U) := __tmp_192(39, 32)
        arrayRegFiles(__tmp_191 + 5.U) := __tmp_192(47, 40)
        arrayRegFiles(__tmp_191 + 6.U) := __tmp_192(55, 48)
        arrayRegFiles(__tmp_191 + 7.U) := __tmp_192(63, 56)

        val __tmp_193 = SP - 80.U(16.W)
        val __tmp_194 = (generalRegFiles(2.U)).asUInt
        arrayRegFiles(__tmp_193 + 0.U) := __tmp_194(7, 0)
        arrayRegFiles(__tmp_193 + 1.U) := __tmp_194(15, 8)
        arrayRegFiles(__tmp_193 + 2.U) := __tmp_194(23, 16)
        arrayRegFiles(__tmp_193 + 3.U) := __tmp_194(31, 24)
        arrayRegFiles(__tmp_193 + 4.U) := __tmp_194(39, 32)
        arrayRegFiles(__tmp_193 + 5.U) := __tmp_194(47, 40)
        arrayRegFiles(__tmp_193 + 6.U) := __tmp_194(55, 48)
        arrayRegFiles(__tmp_193 + 7.U) := __tmp_194(63, 56)

        val __tmp_195 = SP - 72.U(16.W)
        val __tmp_196 = (generalRegFiles(3.U)).asUInt
        arrayRegFiles(__tmp_195 + 0.U) := __tmp_196(7, 0)
        arrayRegFiles(__tmp_195 + 1.U) := __tmp_196(15, 8)
        arrayRegFiles(__tmp_195 + 2.U) := __tmp_196(23, 16)
        arrayRegFiles(__tmp_195 + 3.U) := __tmp_196(31, 24)
        arrayRegFiles(__tmp_195 + 4.U) := __tmp_196(39, 32)
        arrayRegFiles(__tmp_195 + 5.U) := __tmp_196(47, 40)
        arrayRegFiles(__tmp_195 + 6.U) := __tmp_196(55, 48)
        arrayRegFiles(__tmp_195 + 7.U) := __tmp_196(63, 56)

        val __tmp_197 = SP - 64.U(16.W)
        val __tmp_198 = (generalRegFiles(4.U)).asUInt
        arrayRegFiles(__tmp_197 + 0.U) := __tmp_198(7, 0)
        arrayRegFiles(__tmp_197 + 1.U) := __tmp_198(15, 8)
        arrayRegFiles(__tmp_197 + 2.U) := __tmp_198(23, 16)
        arrayRegFiles(__tmp_197 + 3.U) := __tmp_198(31, 24)
        arrayRegFiles(__tmp_197 + 4.U) := __tmp_198(39, 32)
        arrayRegFiles(__tmp_197 + 5.U) := __tmp_198(47, 40)
        arrayRegFiles(__tmp_197 + 6.U) := __tmp_198(55, 48)
        arrayRegFiles(__tmp_197 + 7.U) := __tmp_198(63, 56)

        val __tmp_199 = SP - 56.U(16.W)
        val __tmp_200 = (generalRegFiles(5.U)).asUInt
        arrayRegFiles(__tmp_199 + 0.U) := __tmp_200(7, 0)
        arrayRegFiles(__tmp_199 + 1.U) := __tmp_200(15, 8)
        arrayRegFiles(__tmp_199 + 2.U) := __tmp_200(23, 16)
        arrayRegFiles(__tmp_199 + 3.U) := __tmp_200(31, 24)
        arrayRegFiles(__tmp_199 + 4.U) := __tmp_200(39, 32)
        arrayRegFiles(__tmp_199 + 5.U) := __tmp_200(47, 40)
        arrayRegFiles(__tmp_199 + 6.U) := __tmp_200(55, 48)
        arrayRegFiles(__tmp_199 + 7.U) := __tmp_200(63, 56)

        val __tmp_201 = SP - 48.U(16.W)
        val __tmp_202 = (generalRegFiles(6.U)).asUInt
        arrayRegFiles(__tmp_201 + 0.U) := __tmp_202(7, 0)
        arrayRegFiles(__tmp_201 + 1.U) := __tmp_202(15, 8)
        arrayRegFiles(__tmp_201 + 2.U) := __tmp_202(23, 16)
        arrayRegFiles(__tmp_201 + 3.U) := __tmp_202(31, 24)
        arrayRegFiles(__tmp_201 + 4.U) := __tmp_202(39, 32)
        arrayRegFiles(__tmp_201 + 5.U) := __tmp_202(47, 40)
        arrayRegFiles(__tmp_201 + 6.U) := __tmp_202(55, 48)
        arrayRegFiles(__tmp_201 + 7.U) := __tmp_202(63, 56)

        val __tmp_203 = SP - 40.U(16.W)
        val __tmp_204 = (generalRegFiles(7.U)).asUInt
        arrayRegFiles(__tmp_203 + 0.U) := __tmp_204(7, 0)
        arrayRegFiles(__tmp_203 + 1.U) := __tmp_204(15, 8)
        arrayRegFiles(__tmp_203 + 2.U) := __tmp_204(23, 16)
        arrayRegFiles(__tmp_203 + 3.U) := __tmp_204(31, 24)
        arrayRegFiles(__tmp_203 + 4.U) := __tmp_204(39, 32)
        arrayRegFiles(__tmp_203 + 5.U) := __tmp_204(47, 40)
        arrayRegFiles(__tmp_203 + 6.U) := __tmp_204(55, 48)
        arrayRegFiles(__tmp_203 + 7.U) := __tmp_204(63, 56)

        val __tmp_205 = SP - 32.U(16.W)
        val __tmp_206 = (generalRegFiles(8.U)).asUInt
        arrayRegFiles(__tmp_205 + 0.U) := __tmp_206(7, 0)
        arrayRegFiles(__tmp_205 + 1.U) := __tmp_206(15, 8)
        arrayRegFiles(__tmp_205 + 2.U) := __tmp_206(23, 16)
        arrayRegFiles(__tmp_205 + 3.U) := __tmp_206(31, 24)
        arrayRegFiles(__tmp_205 + 4.U) := __tmp_206(39, 32)
        arrayRegFiles(__tmp_205 + 5.U) := __tmp_206(47, 40)
        arrayRegFiles(__tmp_205 + 6.U) := __tmp_206(55, 48)
        arrayRegFiles(__tmp_205 + 7.U) := __tmp_206(63, 56)

        val __tmp_207 = SP - 24.U(16.W)
        val __tmp_208 = (generalRegFiles(9.U)).asUInt
        arrayRegFiles(__tmp_207 + 0.U) := __tmp_208(7, 0)
        arrayRegFiles(__tmp_207 + 1.U) := __tmp_208(15, 8)
        arrayRegFiles(__tmp_207 + 2.U) := __tmp_208(23, 16)
        arrayRegFiles(__tmp_207 + 3.U) := __tmp_208(31, 24)
        arrayRegFiles(__tmp_207 + 4.U) := __tmp_208(39, 32)
        arrayRegFiles(__tmp_207 + 5.U) := __tmp_208(47, 40)
        arrayRegFiles(__tmp_207 + 6.U) := __tmp_208(55, 48)
        arrayRegFiles(__tmp_207 + 7.U) := __tmp_208(63, 56)

        val __tmp_209 = SP - 16.U(16.W)
        val __tmp_210 = (generalRegFiles(10.U)).asUInt
        arrayRegFiles(__tmp_209 + 0.U) := __tmp_210(7, 0)
        arrayRegFiles(__tmp_209 + 1.U) := __tmp_210(15, 8)
        arrayRegFiles(__tmp_209 + 2.U) := __tmp_210(23, 16)
        arrayRegFiles(__tmp_209 + 3.U) := __tmp_210(31, 24)
        arrayRegFiles(__tmp_209 + 4.U) := __tmp_210(39, 32)
        arrayRegFiles(__tmp_209 + 5.U) := __tmp_210(47, 40)
        arrayRegFiles(__tmp_209 + 6.U) := __tmp_210(55, 48)
        arrayRegFiles(__tmp_209 + 7.U) := __tmp_210(63, 56)

        val __tmp_211 = SP - 8.U(16.W)
        val __tmp_212 = (generalRegFiles(11.U)).asUInt
        arrayRegFiles(__tmp_211 + 0.U) := __tmp_212(7, 0)
        arrayRegFiles(__tmp_211 + 1.U) := __tmp_212(15, 8)
        arrayRegFiles(__tmp_211 + 2.U) := __tmp_212(23, 16)
        arrayRegFiles(__tmp_211 + 3.U) := __tmp_212(31, 24)
        arrayRegFiles(__tmp_211 + 4.U) := __tmp_212(39, 32)
        arrayRegFiles(__tmp_211 + 5.U) := __tmp_212(47, 40)
        arrayRegFiles(__tmp_211 + 6.U) := __tmp_212(55, 48)
        arrayRegFiles(__tmp_211 + 7.U) := __tmp_212(63, 56)

        CP := 24.U
      }

      is(24.U) {
        /*
        *(SP + 14) = 116 [unsigned, U8, 1]  // 't'
        *(SP + 15) = 101 [unsigned, U8, 1]  // 'e'
        *(SP + 16) = 115 [unsigned, U8, 1]  // 's'
        *(SP + 17) = 116 [unsigned, U8, 1]  // 't'
        *(SP + 18) = 65 [unsigned, U8, 1]  // 'A'
        *(SP + 19) = 100 [unsigned, U8, 1]  // 'd'
        *(SP + 20) = 100 [unsigned, U8, 1]  // 'd'
        *(SP + 21) = 50 [unsigned, U8, 1]  // '2'
        *(SP + 22) = 32 [unsigned, U8, 1]  // ' '
        *(SP + 23) = 40 [unsigned, U8, 1]  // '('
        *(SP + 24) = 97 [unsigned, U8, 1]  // 'a'
        *(SP + 25) = 100 [unsigned, U8, 1]  // 'd'
        *(SP + 26) = 100 [unsigned, U8, 1]  // 'd'
        *(SP + 27) = 45 [unsigned, U8, 1]  // '-'
        *(SP + 28) = 116 [unsigned, U8, 1]  // 't'
        *(SP + 29) = 101 [unsigned, U8, 1]  // 'e'
        *(SP + 30) = 115 [unsigned, U8, 1]  // 's'
        *(SP + 31) = 116 [unsigned, U8, 1]  // 't'
        *(SP + 32) = 46 [unsigned, U8, 1]  // '.'
        *(SP + 33) = 115 [unsigned, U8, 1]  // 's'
        *(SP + 34) = 99 [unsigned, U8, 1]  // 'c'
        *(SP + 35) = 58 [unsigned, U8, 1]  // ':'
        *(SP + 4) = 14 [signed, U32, 4]  // $sfLoc = 14
        goto .25
        */


        val __tmp_213 = SP + 14.U(16.W)
        val __tmp_214 = (116.U(8.W)).asUInt
        arrayRegFiles(__tmp_213 + 0.U) := __tmp_214(7, 0)

        val __tmp_215 = SP + 15.U(16.W)
        val __tmp_216 = (101.U(8.W)).asUInt
        arrayRegFiles(__tmp_215 + 0.U) := __tmp_216(7, 0)

        val __tmp_217 = SP + 16.U(16.W)
        val __tmp_218 = (115.U(8.W)).asUInt
        arrayRegFiles(__tmp_217 + 0.U) := __tmp_218(7, 0)

        val __tmp_219 = SP + 17.U(16.W)
        val __tmp_220 = (116.U(8.W)).asUInt
        arrayRegFiles(__tmp_219 + 0.U) := __tmp_220(7, 0)

        val __tmp_221 = SP + 18.U(16.W)
        val __tmp_222 = (65.U(8.W)).asUInt
        arrayRegFiles(__tmp_221 + 0.U) := __tmp_222(7, 0)

        val __tmp_223 = SP + 19.U(16.W)
        val __tmp_224 = (100.U(8.W)).asUInt
        arrayRegFiles(__tmp_223 + 0.U) := __tmp_224(7, 0)

        val __tmp_225 = SP + 20.U(16.W)
        val __tmp_226 = (100.U(8.W)).asUInt
        arrayRegFiles(__tmp_225 + 0.U) := __tmp_226(7, 0)

        val __tmp_227 = SP + 21.U(16.W)
        val __tmp_228 = (50.U(8.W)).asUInt
        arrayRegFiles(__tmp_227 + 0.U) := __tmp_228(7, 0)

        val __tmp_229 = SP + 22.U(16.W)
        val __tmp_230 = (32.U(8.W)).asUInt
        arrayRegFiles(__tmp_229 + 0.U) := __tmp_230(7, 0)

        val __tmp_231 = SP + 23.U(16.W)
        val __tmp_232 = (40.U(8.W)).asUInt
        arrayRegFiles(__tmp_231 + 0.U) := __tmp_232(7, 0)

        val __tmp_233 = SP + 24.U(16.W)
        val __tmp_234 = (97.U(8.W)).asUInt
        arrayRegFiles(__tmp_233 + 0.U) := __tmp_234(7, 0)

        val __tmp_235 = SP + 25.U(16.W)
        val __tmp_236 = (100.U(8.W)).asUInt
        arrayRegFiles(__tmp_235 + 0.U) := __tmp_236(7, 0)

        val __tmp_237 = SP + 26.U(16.W)
        val __tmp_238 = (100.U(8.W)).asUInt
        arrayRegFiles(__tmp_237 + 0.U) := __tmp_238(7, 0)

        val __tmp_239 = SP + 27.U(16.W)
        val __tmp_240 = (45.U(8.W)).asUInt
        arrayRegFiles(__tmp_239 + 0.U) := __tmp_240(7, 0)

        val __tmp_241 = SP + 28.U(16.W)
        val __tmp_242 = (116.U(8.W)).asUInt
        arrayRegFiles(__tmp_241 + 0.U) := __tmp_242(7, 0)

        val __tmp_243 = SP + 29.U(16.W)
        val __tmp_244 = (101.U(8.W)).asUInt
        arrayRegFiles(__tmp_243 + 0.U) := __tmp_244(7, 0)

        val __tmp_245 = SP + 30.U(16.W)
        val __tmp_246 = (115.U(8.W)).asUInt
        arrayRegFiles(__tmp_245 + 0.U) := __tmp_246(7, 0)

        val __tmp_247 = SP + 31.U(16.W)
        val __tmp_248 = (116.U(8.W)).asUInt
        arrayRegFiles(__tmp_247 + 0.U) := __tmp_248(7, 0)

        val __tmp_249 = SP + 32.U(16.W)
        val __tmp_250 = (46.U(8.W)).asUInt
        arrayRegFiles(__tmp_249 + 0.U) := __tmp_250(7, 0)

        val __tmp_251 = SP + 33.U(16.W)
        val __tmp_252 = (115.U(8.W)).asUInt
        arrayRegFiles(__tmp_251 + 0.U) := __tmp_252(7, 0)

        val __tmp_253 = SP + 34.U(16.W)
        val __tmp_254 = (99.U(8.W)).asUInt
        arrayRegFiles(__tmp_253 + 0.U) := __tmp_254(7, 0)

        val __tmp_255 = SP + 35.U(16.W)
        val __tmp_256 = (58.U(8.W)).asUInt
        arrayRegFiles(__tmp_255 + 0.U) := __tmp_256(7, 0)

        val __tmp_257 = SP + 4.U(16.W)
        val __tmp_258 = (14.S(32.W)).asUInt
        arrayRegFiles(__tmp_257 + 0.U) := __tmp_258(7, 0)
        arrayRegFiles(__tmp_257 + 1.U) := __tmp_258(15, 8)
        arrayRegFiles(__tmp_257 + 2.U) := __tmp_258(23, 16)
        arrayRegFiles(__tmp_257 + 3.U) := __tmp_258(31, 24)

        CP := 25.U
      }

      is(25.U) {
        /*
        alloc add$res@[14,11].A314137A: U16 [@42, 2]
        goto .26
        */


        CP := 26.U
      }

      is(26.U) {
        /*
        SP = SP + 52
        goto .27
        */


        SP := SP + 52.U

        CP := 27.U
      }

      is(27.U) {
        /*
        decl $ret: CP [@0, 2], $res: SP [@2, 4], $sfCaller: SP [@6, 2], $sfDesc: IS[17, U8] [@12, 29], $sfLoc: U32 [@8, 4], x: U16 [@41, 2], y: U16 [@43, 2]
        *SP = 196 [unsigned, CP, 2]  // $ret@0 = 2291
        *(SP + 2) = (SP - 10) [unsigned, SP, 2]  // $res@2 = -10
        *(SP + 6) = (SP - 50) [unsigned, SP, 2]  // $sfCaller@6 = -50
        *(SP + 41) = 27181 [unsigned, U16, 2]  // x = 27181
        *(SP + 43) = 5 [unsigned, U16, 2]  // y = 5
        goto .14
        */


        val __tmp_259 = SP
        val __tmp_260 = (196.U(16.W)).asUInt
        arrayRegFiles(__tmp_259 + 0.U) := __tmp_260(7, 0)
        arrayRegFiles(__tmp_259 + 1.U) := __tmp_260(15, 8)

        val __tmp_261 = SP + 2.U(16.W)
        val __tmp_262 = (SP - 10.U(16.W)).asUInt
        arrayRegFiles(__tmp_261 + 0.U) := __tmp_262(7, 0)
        arrayRegFiles(__tmp_261 + 1.U) := __tmp_262(15, 8)

        val __tmp_263 = SP + 6.U(16.W)
        val __tmp_264 = (SP - 50.U(16.W)).asUInt
        arrayRegFiles(__tmp_263 + 0.U) := __tmp_264(7, 0)
        arrayRegFiles(__tmp_263 + 1.U) := __tmp_264(15, 8)

        val __tmp_265 = SP + 41.U(16.W)
        val __tmp_266 = (27181.U(16.W)).asUInt
        arrayRegFiles(__tmp_265 + 0.U) := __tmp_266(7, 0)
        arrayRegFiles(__tmp_265 + 1.U) := __tmp_266(15, 8)

        val __tmp_267 = SP + 43.U(16.W)
        val __tmp_268 = (5.U(16.W)).asUInt
        arrayRegFiles(__tmp_267 + 0.U) := __tmp_268(7, 0)
        arrayRegFiles(__tmp_267 + 1.U) := __tmp_268(15, 8)

        CP := 14.U
      }

      is(28.U) {
        /*
        *(SP + 4) = 9 [signed, U32, 4]  // $sfLoc = 9
        goto $ret@0
        */


        val __tmp_269 = SP + 4.U(16.W)
        val __tmp_270 = (9.S(32.W)).asUInt
        arrayRegFiles(__tmp_269 + 0.U) := __tmp_270(7, 0)
        arrayRegFiles(__tmp_269 + 1.U) := __tmp_270(15, 8)
        arrayRegFiles(__tmp_269 + 2.U) := __tmp_270(23, 16)
        arrayRegFiles(__tmp_269 + 3.U) := __tmp_270(31, 24)

        CP := Cat(
          arrayRegFiles(SP + 0.U + 1.U),
          arrayRegFiles(SP + 0.U + 0.U)
        )

      }

      is(29.U) {
        /*
        $0 = *(SP - 96) [unsigned, U64, 8]  // restore $0
        $1 = *(SP - 88) [unsigned, U64, 8]  // restore $1
        $2 = *(SP - 80) [unsigned, U64, 8]  // restore $2
        $3 = *(SP - 72) [unsigned, U64, 8]  // restore $3
        $4 = *(SP - 64) [unsigned, U64, 8]  // restore $4
        $5 = *(SP - 56) [unsigned, U64, 8]  // restore $5
        $6 = *(SP - 48) [unsigned, U64, 8]  // restore $6
        $7 = *(SP - 40) [unsigned, U64, 8]  // restore $7
        $8 = *(SP - 32) [unsigned, U64, 8]  // restore $8
        $9 = *(SP - 24) [unsigned, U64, 8]  // restore $9
        $10 = *(SP - 16) [unsigned, U64, 8]  // restore $10
        $11 = *(SP - 8) [unsigned, U64, 8]  // restore $11
        undecl $sfLoc: U32 [@4, 4], $sfDesc: IS[22, U8] [@8, 34], $sfCaller: SP [@2, 2], $ret: CP [@0, 2]
        goto .30
        */


        val __tmp_271 = (SP - 96.U(16.W)).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_271 + 7.U),
          arrayRegFiles(__tmp_271 + 6.U),
          arrayRegFiles(__tmp_271 + 5.U),
          arrayRegFiles(__tmp_271 + 4.U),
          arrayRegFiles(__tmp_271 + 3.U),
          arrayRegFiles(__tmp_271 + 2.U),
          arrayRegFiles(__tmp_271 + 1.U),
          arrayRegFiles(__tmp_271 + 0.U)
        ).asUInt

        val __tmp_272 = (SP - 88.U(16.W)).asUInt
        generalRegFiles(1.U) := Cat(
          arrayRegFiles(__tmp_272 + 7.U),
          arrayRegFiles(__tmp_272 + 6.U),
          arrayRegFiles(__tmp_272 + 5.U),
          arrayRegFiles(__tmp_272 + 4.U),
          arrayRegFiles(__tmp_272 + 3.U),
          arrayRegFiles(__tmp_272 + 2.U),
          arrayRegFiles(__tmp_272 + 1.U),
          arrayRegFiles(__tmp_272 + 0.U)
        ).asUInt

        val __tmp_273 = (SP - 80.U(16.W)).asUInt
        generalRegFiles(2.U) := Cat(
          arrayRegFiles(__tmp_273 + 7.U),
          arrayRegFiles(__tmp_273 + 6.U),
          arrayRegFiles(__tmp_273 + 5.U),
          arrayRegFiles(__tmp_273 + 4.U),
          arrayRegFiles(__tmp_273 + 3.U),
          arrayRegFiles(__tmp_273 + 2.U),
          arrayRegFiles(__tmp_273 + 1.U),
          arrayRegFiles(__tmp_273 + 0.U)
        ).asUInt

        val __tmp_274 = (SP - 72.U(16.W)).asUInt
        generalRegFiles(3.U) := Cat(
          arrayRegFiles(__tmp_274 + 7.U),
          arrayRegFiles(__tmp_274 + 6.U),
          arrayRegFiles(__tmp_274 + 5.U),
          arrayRegFiles(__tmp_274 + 4.U),
          arrayRegFiles(__tmp_274 + 3.U),
          arrayRegFiles(__tmp_274 + 2.U),
          arrayRegFiles(__tmp_274 + 1.U),
          arrayRegFiles(__tmp_274 + 0.U)
        ).asUInt

        val __tmp_275 = (SP - 64.U(16.W)).asUInt
        generalRegFiles(4.U) := Cat(
          arrayRegFiles(__tmp_275 + 7.U),
          arrayRegFiles(__tmp_275 + 6.U),
          arrayRegFiles(__tmp_275 + 5.U),
          arrayRegFiles(__tmp_275 + 4.U),
          arrayRegFiles(__tmp_275 + 3.U),
          arrayRegFiles(__tmp_275 + 2.U),
          arrayRegFiles(__tmp_275 + 1.U),
          arrayRegFiles(__tmp_275 + 0.U)
        ).asUInt

        val __tmp_276 = (SP - 56.U(16.W)).asUInt
        generalRegFiles(5.U) := Cat(
          arrayRegFiles(__tmp_276 + 7.U),
          arrayRegFiles(__tmp_276 + 6.U),
          arrayRegFiles(__tmp_276 + 5.U),
          arrayRegFiles(__tmp_276 + 4.U),
          arrayRegFiles(__tmp_276 + 3.U),
          arrayRegFiles(__tmp_276 + 2.U),
          arrayRegFiles(__tmp_276 + 1.U),
          arrayRegFiles(__tmp_276 + 0.U)
        ).asUInt

        val __tmp_277 = (SP - 48.U(16.W)).asUInt
        generalRegFiles(6.U) := Cat(
          arrayRegFiles(__tmp_277 + 7.U),
          arrayRegFiles(__tmp_277 + 6.U),
          arrayRegFiles(__tmp_277 + 5.U),
          arrayRegFiles(__tmp_277 + 4.U),
          arrayRegFiles(__tmp_277 + 3.U),
          arrayRegFiles(__tmp_277 + 2.U),
          arrayRegFiles(__tmp_277 + 1.U),
          arrayRegFiles(__tmp_277 + 0.U)
        ).asUInt

        val __tmp_278 = (SP - 40.U(16.W)).asUInt
        generalRegFiles(7.U) := Cat(
          arrayRegFiles(__tmp_278 + 7.U),
          arrayRegFiles(__tmp_278 + 6.U),
          arrayRegFiles(__tmp_278 + 5.U),
          arrayRegFiles(__tmp_278 + 4.U),
          arrayRegFiles(__tmp_278 + 3.U),
          arrayRegFiles(__tmp_278 + 2.U),
          arrayRegFiles(__tmp_278 + 1.U),
          arrayRegFiles(__tmp_278 + 0.U)
        ).asUInt

        val __tmp_279 = (SP - 32.U(16.W)).asUInt
        generalRegFiles(8.U) := Cat(
          arrayRegFiles(__tmp_279 + 7.U),
          arrayRegFiles(__tmp_279 + 6.U),
          arrayRegFiles(__tmp_279 + 5.U),
          arrayRegFiles(__tmp_279 + 4.U),
          arrayRegFiles(__tmp_279 + 3.U),
          arrayRegFiles(__tmp_279 + 2.U),
          arrayRegFiles(__tmp_279 + 1.U),
          arrayRegFiles(__tmp_279 + 0.U)
        ).asUInt

        val __tmp_280 = (SP - 24.U(16.W)).asUInt
        generalRegFiles(9.U) := Cat(
          arrayRegFiles(__tmp_280 + 7.U),
          arrayRegFiles(__tmp_280 + 6.U),
          arrayRegFiles(__tmp_280 + 5.U),
          arrayRegFiles(__tmp_280 + 4.U),
          arrayRegFiles(__tmp_280 + 3.U),
          arrayRegFiles(__tmp_280 + 2.U),
          arrayRegFiles(__tmp_280 + 1.U),
          arrayRegFiles(__tmp_280 + 0.U)
        ).asUInt

        val __tmp_281 = (SP - 16.U(16.W)).asUInt
        generalRegFiles(10.U) := Cat(
          arrayRegFiles(__tmp_281 + 7.U),
          arrayRegFiles(__tmp_281 + 6.U),
          arrayRegFiles(__tmp_281 + 5.U),
          arrayRegFiles(__tmp_281 + 4.U),
          arrayRegFiles(__tmp_281 + 3.U),
          arrayRegFiles(__tmp_281 + 2.U),
          arrayRegFiles(__tmp_281 + 1.U),
          arrayRegFiles(__tmp_281 + 0.U)
        ).asUInt

        val __tmp_282 = (SP - 8.U(16.W)).asUInt
        generalRegFiles(11.U) := Cat(
          arrayRegFiles(__tmp_282 + 7.U),
          arrayRegFiles(__tmp_282 + 6.U),
          arrayRegFiles(__tmp_282 + 5.U),
          arrayRegFiles(__tmp_282 + 4.U),
          arrayRegFiles(__tmp_282 + 3.U),
          arrayRegFiles(__tmp_282 + 2.U),
          arrayRegFiles(__tmp_282 + 1.U),
          arrayRegFiles(__tmp_282 + 0.U)
        ).asUInt

        CP := 30.U
      }

      is(30.U) {
        /*
        SP = SP - 135
        goto .19
        */


        SP := SP - 135.U

        CP := 19.U
      }

      is(31.U) {
        /*
        $0 = *(SP - 96) [unsigned, U64, 8]  // restore $0
        $1 = *(SP - 88) [unsigned, U64, 8]  // restore $1
        $2 = *(SP - 80) [unsigned, U64, 8]  // restore $2
        $3 = *(SP - 72) [unsigned, U64, 8]  // restore $3
        $4 = *(SP - 64) [unsigned, U64, 8]  // restore $4
        $5 = *(SP - 56) [unsigned, U64, 8]  // restore $5
        $6 = *(SP - 48) [unsigned, U64, 8]  // restore $6
        $7 = *(SP - 40) [unsigned, U64, 8]  // restore $7
        $8 = *(SP - 32) [unsigned, U64, 8]  // restore $8
        $9 = *(SP - 24) [unsigned, U64, 8]  // restore $9
        $10 = *(SP - 16) [unsigned, U64, 8]  // restore $10
        $11 = *(SP - 8) [unsigned, U64, 8]  // restore $11
        undecl $sfLoc: U32 [@4, 4], $sfDesc: IS[22, U8] [@8, 34], $sfCaller: SP [@2, 2], $ret: CP [@0, 2]
        goto .32
        */


        val __tmp_283 = (SP - 96.U(16.W)).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_283 + 7.U),
          arrayRegFiles(__tmp_283 + 6.U),
          arrayRegFiles(__tmp_283 + 5.U),
          arrayRegFiles(__tmp_283 + 4.U),
          arrayRegFiles(__tmp_283 + 3.U),
          arrayRegFiles(__tmp_283 + 2.U),
          arrayRegFiles(__tmp_283 + 1.U),
          arrayRegFiles(__tmp_283 + 0.U)
        ).asUInt

        val __tmp_284 = (SP - 88.U(16.W)).asUInt
        generalRegFiles(1.U) := Cat(
          arrayRegFiles(__tmp_284 + 7.U),
          arrayRegFiles(__tmp_284 + 6.U),
          arrayRegFiles(__tmp_284 + 5.U),
          arrayRegFiles(__tmp_284 + 4.U),
          arrayRegFiles(__tmp_284 + 3.U),
          arrayRegFiles(__tmp_284 + 2.U),
          arrayRegFiles(__tmp_284 + 1.U),
          arrayRegFiles(__tmp_284 + 0.U)
        ).asUInt

        val __tmp_285 = (SP - 80.U(16.W)).asUInt
        generalRegFiles(2.U) := Cat(
          arrayRegFiles(__tmp_285 + 7.U),
          arrayRegFiles(__tmp_285 + 6.U),
          arrayRegFiles(__tmp_285 + 5.U),
          arrayRegFiles(__tmp_285 + 4.U),
          arrayRegFiles(__tmp_285 + 3.U),
          arrayRegFiles(__tmp_285 + 2.U),
          arrayRegFiles(__tmp_285 + 1.U),
          arrayRegFiles(__tmp_285 + 0.U)
        ).asUInt

        val __tmp_286 = (SP - 72.U(16.W)).asUInt
        generalRegFiles(3.U) := Cat(
          arrayRegFiles(__tmp_286 + 7.U),
          arrayRegFiles(__tmp_286 + 6.U),
          arrayRegFiles(__tmp_286 + 5.U),
          arrayRegFiles(__tmp_286 + 4.U),
          arrayRegFiles(__tmp_286 + 3.U),
          arrayRegFiles(__tmp_286 + 2.U),
          arrayRegFiles(__tmp_286 + 1.U),
          arrayRegFiles(__tmp_286 + 0.U)
        ).asUInt

        val __tmp_287 = (SP - 64.U(16.W)).asUInt
        generalRegFiles(4.U) := Cat(
          arrayRegFiles(__tmp_287 + 7.U),
          arrayRegFiles(__tmp_287 + 6.U),
          arrayRegFiles(__tmp_287 + 5.U),
          arrayRegFiles(__tmp_287 + 4.U),
          arrayRegFiles(__tmp_287 + 3.U),
          arrayRegFiles(__tmp_287 + 2.U),
          arrayRegFiles(__tmp_287 + 1.U),
          arrayRegFiles(__tmp_287 + 0.U)
        ).asUInt

        val __tmp_288 = (SP - 56.U(16.W)).asUInt
        generalRegFiles(5.U) := Cat(
          arrayRegFiles(__tmp_288 + 7.U),
          arrayRegFiles(__tmp_288 + 6.U),
          arrayRegFiles(__tmp_288 + 5.U),
          arrayRegFiles(__tmp_288 + 4.U),
          arrayRegFiles(__tmp_288 + 3.U),
          arrayRegFiles(__tmp_288 + 2.U),
          arrayRegFiles(__tmp_288 + 1.U),
          arrayRegFiles(__tmp_288 + 0.U)
        ).asUInt

        val __tmp_289 = (SP - 48.U(16.W)).asUInt
        generalRegFiles(6.U) := Cat(
          arrayRegFiles(__tmp_289 + 7.U),
          arrayRegFiles(__tmp_289 + 6.U),
          arrayRegFiles(__tmp_289 + 5.U),
          arrayRegFiles(__tmp_289 + 4.U),
          arrayRegFiles(__tmp_289 + 3.U),
          arrayRegFiles(__tmp_289 + 2.U),
          arrayRegFiles(__tmp_289 + 1.U),
          arrayRegFiles(__tmp_289 + 0.U)
        ).asUInt

        val __tmp_290 = (SP - 40.U(16.W)).asUInt
        generalRegFiles(7.U) := Cat(
          arrayRegFiles(__tmp_290 + 7.U),
          arrayRegFiles(__tmp_290 + 6.U),
          arrayRegFiles(__tmp_290 + 5.U),
          arrayRegFiles(__tmp_290 + 4.U),
          arrayRegFiles(__tmp_290 + 3.U),
          arrayRegFiles(__tmp_290 + 2.U),
          arrayRegFiles(__tmp_290 + 1.U),
          arrayRegFiles(__tmp_290 + 0.U)
        ).asUInt

        val __tmp_291 = (SP - 32.U(16.W)).asUInt
        generalRegFiles(8.U) := Cat(
          arrayRegFiles(__tmp_291 + 7.U),
          arrayRegFiles(__tmp_291 + 6.U),
          arrayRegFiles(__tmp_291 + 5.U),
          arrayRegFiles(__tmp_291 + 4.U),
          arrayRegFiles(__tmp_291 + 3.U),
          arrayRegFiles(__tmp_291 + 2.U),
          arrayRegFiles(__tmp_291 + 1.U),
          arrayRegFiles(__tmp_291 + 0.U)
        ).asUInt

        val __tmp_292 = (SP - 24.U(16.W)).asUInt
        generalRegFiles(9.U) := Cat(
          arrayRegFiles(__tmp_292 + 7.U),
          arrayRegFiles(__tmp_292 + 6.U),
          arrayRegFiles(__tmp_292 + 5.U),
          arrayRegFiles(__tmp_292 + 4.U),
          arrayRegFiles(__tmp_292 + 3.U),
          arrayRegFiles(__tmp_292 + 2.U),
          arrayRegFiles(__tmp_292 + 1.U),
          arrayRegFiles(__tmp_292 + 0.U)
        ).asUInt

        val __tmp_293 = (SP - 16.U(16.W)).asUInt
        generalRegFiles(10.U) := Cat(
          arrayRegFiles(__tmp_293 + 7.U),
          arrayRegFiles(__tmp_293 + 6.U),
          arrayRegFiles(__tmp_293 + 5.U),
          arrayRegFiles(__tmp_293 + 4.U),
          arrayRegFiles(__tmp_293 + 3.U),
          arrayRegFiles(__tmp_293 + 2.U),
          arrayRegFiles(__tmp_293 + 1.U),
          arrayRegFiles(__tmp_293 + 0.U)
        ).asUInt

        val __tmp_294 = (SP - 8.U(16.W)).asUInt
        generalRegFiles(11.U) := Cat(
          arrayRegFiles(__tmp_294 + 7.U),
          arrayRegFiles(__tmp_294 + 6.U),
          arrayRegFiles(__tmp_294 + 5.U),
          arrayRegFiles(__tmp_294 + 4.U),
          arrayRegFiles(__tmp_294 + 3.U),
          arrayRegFiles(__tmp_294 + 2.U),
          arrayRegFiles(__tmp_294 + 1.U),
          arrayRegFiles(__tmp_294 + 0.U)
        ).asUInt

        CP := 32.U
      }

      is(32.U) {
        /*
        SP = SP - 135
        goto .28
        */


        SP := SP - 135.U

        CP := 28.U
      }

      is(33.U) {
        /*
        $0 = **(SP + 2) [unsigned, U16, 2]  // $0 = $ret
        undecl y: U16 [@43, 2], x: U16 [@41, 2], $sfLoc: U32 [@8, 4], $sfDesc: IS[17, U8] [@12, 29], $sfCaller: SP [@6, 2], $res: SP [@2, 4], $ret: CP [@0, 2]
        goto .34
        */


        val __tmp_295 = (Cat(
          arrayRegFiles(SP + 2.U(16.W) + 1.U),
          arrayRegFiles(SP + 2.U(16.W) + 0.U)
        )).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_295 + 1.U),
          arrayRegFiles(__tmp_295 + 0.U)
        ).asUInt

        CP := 34.U
      }

      is(34.U) {
        /*
        SP = SP - 52
        goto .35
        */


        SP := SP - 52.U

        CP := 35.U
      }

      is(35.U) {
        /*
        alloc printU64Hex$res@[10,11].CE055965: U64 [@44, 8]
        goto .36
        */


        CP := 36.U
      }

      is(36.U) {
        /*
        SP = SP + 60
        goto .37
        */


        SP := SP + 60.U

        CP := 37.U
      }

      is(37.U) {
        /*
        decl $ret: CP [@0, 2], $res: SP [@2, 2], $sfCaller: SP [@4, 2], $sfDesc: IS[52, U8] [@10, 64], $sfLoc: U32 [@6, 4], buffer: MS[anvil.PrinterIndex.U, U8] [@74, 2], index: anvil.PrinterIndex.U [@76, 8], mask: anvil.PrinterIndex.U [@84, 8], n: U64 [@92, 8], digits: Z [@100, 8]
        *SP = 188 [unsigned, CP, 2]  // $ret@0 = 2290
        *(SP + 2) = (SP - 16) [unsigned, SP, 2]  // $res@2 = -16
        *(SP + 4) = (SP - 58) [unsigned, SP, 2]  // $sfCaller@4 = -58
        *(SP + 74) = *20 [unsigned, SP, 2]  // buffer = *20
        *(SP + 76) = DP [unsigned, anvil.PrinterIndex.U, 8]  // index = DP
        *(SP + 84) = 127 [unsigned, anvil.PrinterIndex.U, 8]  // mask = 127
        *(SP + 92) = ($0 as U64) [unsigned, U64, 8]  // n = ($0 as U64)
        *(SP + 100) = 4 [signed, Z, 8]  // digits = 4
        *(SP - 8) = $0 [unsigned, U64, 8]  // save $0
        goto .38
        */


        val __tmp_296 = SP
        val __tmp_297 = (188.U(16.W)).asUInt
        arrayRegFiles(__tmp_296 + 0.U) := __tmp_297(7, 0)
        arrayRegFiles(__tmp_296 + 1.U) := __tmp_297(15, 8)

        val __tmp_298 = SP + 2.U(16.W)
        val __tmp_299 = (SP - 16.U(16.W)).asUInt
        arrayRegFiles(__tmp_298 + 0.U) := __tmp_299(7, 0)
        arrayRegFiles(__tmp_298 + 1.U) := __tmp_299(15, 8)

        val __tmp_300 = SP + 4.U(16.W)
        val __tmp_301 = (SP - 58.U(16.W)).asUInt
        arrayRegFiles(__tmp_300 + 0.U) := __tmp_301(7, 0)
        arrayRegFiles(__tmp_300 + 1.U) := __tmp_301(15, 8)

        val __tmp_302 = SP + 74.U(16.W)
        val __tmp_303 = (Cat(
          arrayRegFiles(20.U(16.W) + 1.U),
          arrayRegFiles(20.U(16.W) + 0.U)
        )).asUInt
        arrayRegFiles(__tmp_302 + 0.U) := __tmp_303(7, 0)
        arrayRegFiles(__tmp_302 + 1.U) := __tmp_303(15, 8)

        val __tmp_304 = SP + 76.U(16.W)
        val __tmp_305 = (DP).asUInt
        arrayRegFiles(__tmp_304 + 0.U) := __tmp_305(7, 0)
        arrayRegFiles(__tmp_304 + 1.U) := __tmp_305(15, 8)
        arrayRegFiles(__tmp_304 + 2.U) := __tmp_305(23, 16)
        arrayRegFiles(__tmp_304 + 3.U) := __tmp_305(31, 24)
        arrayRegFiles(__tmp_304 + 4.U) := __tmp_305(39, 32)
        arrayRegFiles(__tmp_304 + 5.U) := __tmp_305(47, 40)
        arrayRegFiles(__tmp_304 + 6.U) := __tmp_305(55, 48)
        arrayRegFiles(__tmp_304 + 7.U) := __tmp_305(63, 56)

        val __tmp_306 = SP + 84.U(16.W)
        val __tmp_307 = (127.U(64.W)).asUInt
        arrayRegFiles(__tmp_306 + 0.U) := __tmp_307(7, 0)
        arrayRegFiles(__tmp_306 + 1.U) := __tmp_307(15, 8)
        arrayRegFiles(__tmp_306 + 2.U) := __tmp_307(23, 16)
        arrayRegFiles(__tmp_306 + 3.U) := __tmp_307(31, 24)
        arrayRegFiles(__tmp_306 + 4.U) := __tmp_307(39, 32)
        arrayRegFiles(__tmp_306 + 5.U) := __tmp_307(47, 40)
        arrayRegFiles(__tmp_306 + 6.U) := __tmp_307(55, 48)
        arrayRegFiles(__tmp_306 + 7.U) := __tmp_307(63, 56)

        val __tmp_308 = SP + 92.U(16.W)
        val __tmp_309 = (generalRegFiles(0.U).asUInt).asUInt
        arrayRegFiles(__tmp_308 + 0.U) := __tmp_309(7, 0)
        arrayRegFiles(__tmp_308 + 1.U) := __tmp_309(15, 8)
        arrayRegFiles(__tmp_308 + 2.U) := __tmp_309(23, 16)
        arrayRegFiles(__tmp_308 + 3.U) := __tmp_309(31, 24)
        arrayRegFiles(__tmp_308 + 4.U) := __tmp_309(39, 32)
        arrayRegFiles(__tmp_308 + 5.U) := __tmp_309(47, 40)
        arrayRegFiles(__tmp_308 + 6.U) := __tmp_309(55, 48)
        arrayRegFiles(__tmp_308 + 7.U) := __tmp_309(63, 56)

        val __tmp_310 = SP + 100.U(16.W)
        val __tmp_311 = (4.S(64.W)).asUInt
        arrayRegFiles(__tmp_310 + 0.U) := __tmp_311(7, 0)
        arrayRegFiles(__tmp_310 + 1.U) := __tmp_311(15, 8)
        arrayRegFiles(__tmp_310 + 2.U) := __tmp_311(23, 16)
        arrayRegFiles(__tmp_310 + 3.U) := __tmp_311(31, 24)
        arrayRegFiles(__tmp_310 + 4.U) := __tmp_311(39, 32)
        arrayRegFiles(__tmp_310 + 5.U) := __tmp_311(47, 40)
        arrayRegFiles(__tmp_310 + 6.U) := __tmp_311(55, 48)
        arrayRegFiles(__tmp_310 + 7.U) := __tmp_311(63, 56)

        val __tmp_312 = SP - 8.U(16.W)
        val __tmp_313 = (generalRegFiles(0.U)).asUInt
        arrayRegFiles(__tmp_312 + 0.U) := __tmp_313(7, 0)
        arrayRegFiles(__tmp_312 + 1.U) := __tmp_313(15, 8)
        arrayRegFiles(__tmp_312 + 2.U) := __tmp_313(23, 16)
        arrayRegFiles(__tmp_312 + 3.U) := __tmp_313(31, 24)
        arrayRegFiles(__tmp_312 + 4.U) := __tmp_313(39, 32)
        arrayRegFiles(__tmp_312 + 5.U) := __tmp_313(47, 40)
        arrayRegFiles(__tmp_312 + 6.U) := __tmp_313(55, 48)
        arrayRegFiles(__tmp_312 + 7.U) := __tmp_313(63, 56)

        CP := 38.U
      }

      is(38.U) {
        /*
        *(SP + 16) = 111 [unsigned, U8, 1]  // 'o'
        *(SP + 17) = 114 [unsigned, U8, 1]  // 'r'
        *(SP + 18) = 103 [unsigned, U8, 1]  // 'g'
        *(SP + 19) = 46 [unsigned, U8, 1]  // '.'
        *(SP + 20) = 115 [unsigned, U8, 1]  // 's'
        *(SP + 21) = 105 [unsigned, U8, 1]  // 'i'
        *(SP + 22) = 114 [unsigned, U8, 1]  // 'r'
        *(SP + 23) = 101 [unsigned, U8, 1]  // 'e'
        *(SP + 24) = 117 [unsigned, U8, 1]  // 'u'
        *(SP + 25) = 109 [unsigned, U8, 1]  // 'm'
        *(SP + 26) = 46 [unsigned, U8, 1]  // '.'
        *(SP + 27) = 97 [unsigned, U8, 1]  // 'a'
        *(SP + 28) = 110 [unsigned, U8, 1]  // 'n'
        *(SP + 29) = 118 [unsigned, U8, 1]  // 'v'
        *(SP + 30) = 105 [unsigned, U8, 1]  // 'i'
        *(SP + 31) = 108 [unsigned, U8, 1]  // 'l'
        *(SP + 32) = 46 [unsigned, U8, 1]  // '.'
        *(SP + 33) = 82 [unsigned, U8, 1]  // 'R'
        *(SP + 34) = 117 [unsigned, U8, 1]  // 'u'
        *(SP + 35) = 110 [unsigned, U8, 1]  // 'n'
        *(SP + 36) = 116 [unsigned, U8, 1]  // 't'
        *(SP + 37) = 105 [unsigned, U8, 1]  // 'i'
        *(SP + 38) = 109 [unsigned, U8, 1]  // 'm'
        *(SP + 39) = 101 [unsigned, U8, 1]  // 'e'
        *(SP + 40) = 46 [unsigned, U8, 1]  // '.'
        *(SP + 41) = 112 [unsigned, U8, 1]  // 'p'
        *(SP + 42) = 114 [unsigned, U8, 1]  // 'r'
        *(SP + 43) = 105 [unsigned, U8, 1]  // 'i'
        *(SP + 44) = 110 [unsigned, U8, 1]  // 'n'
        *(SP + 45) = 116 [unsigned, U8, 1]  // 't'
        *(SP + 46) = 85 [unsigned, U8, 1]  // 'U'
        *(SP + 47) = 54 [unsigned, U8, 1]  // '6'
        *(SP + 48) = 52 [unsigned, U8, 1]  // '4'
        *(SP + 49) = 72 [unsigned, U8, 1]  // 'H'
        *(SP + 50) = 101 [unsigned, U8, 1]  // 'e'
        *(SP + 51) = 120 [unsigned, U8, 1]  // 'x'
        *(SP + 52) = 32 [unsigned, U8, 1]  // ' '
        *(SP + 53) = 40 [unsigned, U8, 1]  // '('
        *(SP + 54) = 82 [unsigned, U8, 1]  // 'R'
        *(SP + 55) = 117 [unsigned, U8, 1]  // 'u'
        *(SP + 56) = 110 [unsigned, U8, 1]  // 'n'
        *(SP + 57) = 116 [unsigned, U8, 1]  // 't'
        *(SP + 58) = 105 [unsigned, U8, 1]  // 'i'
        *(SP + 59) = 109 [unsigned, U8, 1]  // 'm'
        *(SP + 60) = 101 [unsigned, U8, 1]  // 'e'
        *(SP + 61) = 46 [unsigned, U8, 1]  // '.'
        *(SP + 62) = 115 [unsigned, U8, 1]  // 's'
        *(SP + 63) = 99 [unsigned, U8, 1]  // 'c'
        *(SP + 64) = 97 [unsigned, U8, 1]  // 'a'
        *(SP + 65) = 108 [unsigned, U8, 1]  // 'l'
        *(SP + 66) = 97 [unsigned, U8, 1]  // 'a'
        *(SP + 67) = 58 [unsigned, U8, 1]  // ':'
        *(SP + 6) = 249 [signed, U32, 4]  // $sfLoc = 249
        goto .39
        */


        val __tmp_314 = SP + 16.U(16.W)
        val __tmp_315 = (111.U(8.W)).asUInt
        arrayRegFiles(__tmp_314 + 0.U) := __tmp_315(7, 0)

        val __tmp_316 = SP + 17.U(16.W)
        val __tmp_317 = (114.U(8.W)).asUInt
        arrayRegFiles(__tmp_316 + 0.U) := __tmp_317(7, 0)

        val __tmp_318 = SP + 18.U(16.W)
        val __tmp_319 = (103.U(8.W)).asUInt
        arrayRegFiles(__tmp_318 + 0.U) := __tmp_319(7, 0)

        val __tmp_320 = SP + 19.U(16.W)
        val __tmp_321 = (46.U(8.W)).asUInt
        arrayRegFiles(__tmp_320 + 0.U) := __tmp_321(7, 0)

        val __tmp_322 = SP + 20.U(16.W)
        val __tmp_323 = (115.U(8.W)).asUInt
        arrayRegFiles(__tmp_322 + 0.U) := __tmp_323(7, 0)

        val __tmp_324 = SP + 21.U(16.W)
        val __tmp_325 = (105.U(8.W)).asUInt
        arrayRegFiles(__tmp_324 + 0.U) := __tmp_325(7, 0)

        val __tmp_326 = SP + 22.U(16.W)
        val __tmp_327 = (114.U(8.W)).asUInt
        arrayRegFiles(__tmp_326 + 0.U) := __tmp_327(7, 0)

        val __tmp_328 = SP + 23.U(16.W)
        val __tmp_329 = (101.U(8.W)).asUInt
        arrayRegFiles(__tmp_328 + 0.U) := __tmp_329(7, 0)

        val __tmp_330 = SP + 24.U(16.W)
        val __tmp_331 = (117.U(8.W)).asUInt
        arrayRegFiles(__tmp_330 + 0.U) := __tmp_331(7, 0)

        val __tmp_332 = SP + 25.U(16.W)
        val __tmp_333 = (109.U(8.W)).asUInt
        arrayRegFiles(__tmp_332 + 0.U) := __tmp_333(7, 0)

        val __tmp_334 = SP + 26.U(16.W)
        val __tmp_335 = (46.U(8.W)).asUInt
        arrayRegFiles(__tmp_334 + 0.U) := __tmp_335(7, 0)

        val __tmp_336 = SP + 27.U(16.W)
        val __tmp_337 = (97.U(8.W)).asUInt
        arrayRegFiles(__tmp_336 + 0.U) := __tmp_337(7, 0)

        val __tmp_338 = SP + 28.U(16.W)
        val __tmp_339 = (110.U(8.W)).asUInt
        arrayRegFiles(__tmp_338 + 0.U) := __tmp_339(7, 0)

        val __tmp_340 = SP + 29.U(16.W)
        val __tmp_341 = (118.U(8.W)).asUInt
        arrayRegFiles(__tmp_340 + 0.U) := __tmp_341(7, 0)

        val __tmp_342 = SP + 30.U(16.W)
        val __tmp_343 = (105.U(8.W)).asUInt
        arrayRegFiles(__tmp_342 + 0.U) := __tmp_343(7, 0)

        val __tmp_344 = SP + 31.U(16.W)
        val __tmp_345 = (108.U(8.W)).asUInt
        arrayRegFiles(__tmp_344 + 0.U) := __tmp_345(7, 0)

        val __tmp_346 = SP + 32.U(16.W)
        val __tmp_347 = (46.U(8.W)).asUInt
        arrayRegFiles(__tmp_346 + 0.U) := __tmp_347(7, 0)

        val __tmp_348 = SP + 33.U(16.W)
        val __tmp_349 = (82.U(8.W)).asUInt
        arrayRegFiles(__tmp_348 + 0.U) := __tmp_349(7, 0)

        val __tmp_350 = SP + 34.U(16.W)
        val __tmp_351 = (117.U(8.W)).asUInt
        arrayRegFiles(__tmp_350 + 0.U) := __tmp_351(7, 0)

        val __tmp_352 = SP + 35.U(16.W)
        val __tmp_353 = (110.U(8.W)).asUInt
        arrayRegFiles(__tmp_352 + 0.U) := __tmp_353(7, 0)

        val __tmp_354 = SP + 36.U(16.W)
        val __tmp_355 = (116.U(8.W)).asUInt
        arrayRegFiles(__tmp_354 + 0.U) := __tmp_355(7, 0)

        val __tmp_356 = SP + 37.U(16.W)
        val __tmp_357 = (105.U(8.W)).asUInt
        arrayRegFiles(__tmp_356 + 0.U) := __tmp_357(7, 0)

        val __tmp_358 = SP + 38.U(16.W)
        val __tmp_359 = (109.U(8.W)).asUInt
        arrayRegFiles(__tmp_358 + 0.U) := __tmp_359(7, 0)

        val __tmp_360 = SP + 39.U(16.W)
        val __tmp_361 = (101.U(8.W)).asUInt
        arrayRegFiles(__tmp_360 + 0.U) := __tmp_361(7, 0)

        val __tmp_362 = SP + 40.U(16.W)
        val __tmp_363 = (46.U(8.W)).asUInt
        arrayRegFiles(__tmp_362 + 0.U) := __tmp_363(7, 0)

        val __tmp_364 = SP + 41.U(16.W)
        val __tmp_365 = (112.U(8.W)).asUInt
        arrayRegFiles(__tmp_364 + 0.U) := __tmp_365(7, 0)

        val __tmp_366 = SP + 42.U(16.W)
        val __tmp_367 = (114.U(8.W)).asUInt
        arrayRegFiles(__tmp_366 + 0.U) := __tmp_367(7, 0)

        val __tmp_368 = SP + 43.U(16.W)
        val __tmp_369 = (105.U(8.W)).asUInt
        arrayRegFiles(__tmp_368 + 0.U) := __tmp_369(7, 0)

        val __tmp_370 = SP + 44.U(16.W)
        val __tmp_371 = (110.U(8.W)).asUInt
        arrayRegFiles(__tmp_370 + 0.U) := __tmp_371(7, 0)

        val __tmp_372 = SP + 45.U(16.W)
        val __tmp_373 = (116.U(8.W)).asUInt
        arrayRegFiles(__tmp_372 + 0.U) := __tmp_373(7, 0)

        val __tmp_374 = SP + 46.U(16.W)
        val __tmp_375 = (85.U(8.W)).asUInt
        arrayRegFiles(__tmp_374 + 0.U) := __tmp_375(7, 0)

        val __tmp_376 = SP + 47.U(16.W)
        val __tmp_377 = (54.U(8.W)).asUInt
        arrayRegFiles(__tmp_376 + 0.U) := __tmp_377(7, 0)

        val __tmp_378 = SP + 48.U(16.W)
        val __tmp_379 = (52.U(8.W)).asUInt
        arrayRegFiles(__tmp_378 + 0.U) := __tmp_379(7, 0)

        val __tmp_380 = SP + 49.U(16.W)
        val __tmp_381 = (72.U(8.W)).asUInt
        arrayRegFiles(__tmp_380 + 0.U) := __tmp_381(7, 0)

        val __tmp_382 = SP + 50.U(16.W)
        val __tmp_383 = (101.U(8.W)).asUInt
        arrayRegFiles(__tmp_382 + 0.U) := __tmp_383(7, 0)

        val __tmp_384 = SP + 51.U(16.W)
        val __tmp_385 = (120.U(8.W)).asUInt
        arrayRegFiles(__tmp_384 + 0.U) := __tmp_385(7, 0)

        val __tmp_386 = SP + 52.U(16.W)
        val __tmp_387 = (32.U(8.W)).asUInt
        arrayRegFiles(__tmp_386 + 0.U) := __tmp_387(7, 0)

        val __tmp_388 = SP + 53.U(16.W)
        val __tmp_389 = (40.U(8.W)).asUInt
        arrayRegFiles(__tmp_388 + 0.U) := __tmp_389(7, 0)

        val __tmp_390 = SP + 54.U(16.W)
        val __tmp_391 = (82.U(8.W)).asUInt
        arrayRegFiles(__tmp_390 + 0.U) := __tmp_391(7, 0)

        val __tmp_392 = SP + 55.U(16.W)
        val __tmp_393 = (117.U(8.W)).asUInt
        arrayRegFiles(__tmp_392 + 0.U) := __tmp_393(7, 0)

        val __tmp_394 = SP + 56.U(16.W)
        val __tmp_395 = (110.U(8.W)).asUInt
        arrayRegFiles(__tmp_394 + 0.U) := __tmp_395(7, 0)

        val __tmp_396 = SP + 57.U(16.W)
        val __tmp_397 = (116.U(8.W)).asUInt
        arrayRegFiles(__tmp_396 + 0.U) := __tmp_397(7, 0)

        val __tmp_398 = SP + 58.U(16.W)
        val __tmp_399 = (105.U(8.W)).asUInt
        arrayRegFiles(__tmp_398 + 0.U) := __tmp_399(7, 0)

        val __tmp_400 = SP + 59.U(16.W)
        val __tmp_401 = (109.U(8.W)).asUInt
        arrayRegFiles(__tmp_400 + 0.U) := __tmp_401(7, 0)

        val __tmp_402 = SP + 60.U(16.W)
        val __tmp_403 = (101.U(8.W)).asUInt
        arrayRegFiles(__tmp_402 + 0.U) := __tmp_403(7, 0)

        val __tmp_404 = SP + 61.U(16.W)
        val __tmp_405 = (46.U(8.W)).asUInt
        arrayRegFiles(__tmp_404 + 0.U) := __tmp_405(7, 0)

        val __tmp_406 = SP + 62.U(16.W)
        val __tmp_407 = (115.U(8.W)).asUInt
        arrayRegFiles(__tmp_406 + 0.U) := __tmp_407(7, 0)

        val __tmp_408 = SP + 63.U(16.W)
        val __tmp_409 = (99.U(8.W)).asUInt
        arrayRegFiles(__tmp_408 + 0.U) := __tmp_409(7, 0)

        val __tmp_410 = SP + 64.U(16.W)
        val __tmp_411 = (97.U(8.W)).asUInt
        arrayRegFiles(__tmp_410 + 0.U) := __tmp_411(7, 0)

        val __tmp_412 = SP + 65.U(16.W)
        val __tmp_413 = (108.U(8.W)).asUInt
        arrayRegFiles(__tmp_412 + 0.U) := __tmp_413(7, 0)

        val __tmp_414 = SP + 66.U(16.W)
        val __tmp_415 = (97.U(8.W)).asUInt
        arrayRegFiles(__tmp_414 + 0.U) := __tmp_415(7, 0)

        val __tmp_416 = SP + 67.U(16.W)
        val __tmp_417 = (58.U(8.W)).asUInt
        arrayRegFiles(__tmp_416 + 0.U) := __tmp_417(7, 0)

        val __tmp_418 = SP + 6.U(16.W)
        val __tmp_419 = (249.S(32.W)).asUInt
        arrayRegFiles(__tmp_418 + 0.U) := __tmp_419(7, 0)
        arrayRegFiles(__tmp_418 + 1.U) := __tmp_419(15, 8)
        arrayRegFiles(__tmp_418 + 2.U) := __tmp_419(23, 16)
        arrayRegFiles(__tmp_418 + 3.U) := __tmp_419(31, 24)

        CP := 39.U
      }

      is(39.U) {
        /*
        decl buff: MS[anvil.PrinterIndex.I16, U8] [@108, 32]
        *(SP + 108) = (SP + 110) [unsigned, SP, 2]  // address of buff
        alloc $new@[249,16].11A13D4B: MS[anvil.PrinterIndex.I16, U8] [@140, 30]
        $0 = (SP + 140)
        *(SP + 140) = 1541243932 [unsigned, U32, 4]  // sha3 type signature of MS[anvil.PrinterIndex.I16, U8]: 0x5BDD841C
        *(SP + 144) = 16 [signed, Z, 8]  // size of MS[anvil.PrinterIndex.I16, U8](0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)
        goto .40
        */


        val __tmp_420 = SP + 108.U(16.W)
        val __tmp_421 = (SP + 110.U(16.W)).asUInt
        arrayRegFiles(__tmp_420 + 0.U) := __tmp_421(7, 0)
        arrayRegFiles(__tmp_420 + 1.U) := __tmp_421(15, 8)

        generalRegFiles(0.U) := SP + 140.U(16.W)
        val __tmp_422 = SP + 140.U(16.W)
        val __tmp_423 = (1541243932.U(32.W)).asUInt
        arrayRegFiles(__tmp_422 + 0.U) := __tmp_423(7, 0)
        arrayRegFiles(__tmp_422 + 1.U) := __tmp_423(15, 8)
        arrayRegFiles(__tmp_422 + 2.U) := __tmp_423(23, 16)
        arrayRegFiles(__tmp_422 + 3.U) := __tmp_423(31, 24)

        val __tmp_424 = SP + 144.U(16.W)
        val __tmp_425 = (16.S(64.W)).asUInt
        arrayRegFiles(__tmp_424 + 0.U) := __tmp_425(7, 0)
        arrayRegFiles(__tmp_424 + 1.U) := __tmp_425(15, 8)
        arrayRegFiles(__tmp_424 + 2.U) := __tmp_425(23, 16)
        arrayRegFiles(__tmp_424 + 3.U) := __tmp_425(31, 24)
        arrayRegFiles(__tmp_424 + 4.U) := __tmp_425(39, 32)
        arrayRegFiles(__tmp_424 + 5.U) := __tmp_425(47, 40)
        arrayRegFiles(__tmp_424 + 6.U) := __tmp_425(55, 48)
        arrayRegFiles(__tmp_424 + 7.U) := __tmp_425(63, 56)

        CP := 40.U
      }

      is(40.U) {
        /*
        *(($0 + 12) + (0 as SP)) = 0 [unsigned, U8, 1]  // $0(0) = 0
        *(($0 + 12) + (1 as SP)) = 0 [unsigned, U8, 1]  // $0(1) = 0
        *(($0 + 12) + (2 as SP)) = 0 [unsigned, U8, 1]  // $0(2) = 0
        *(($0 + 12) + (3 as SP)) = 0 [unsigned, U8, 1]  // $0(3) = 0
        *(($0 + 12) + (4 as SP)) = 0 [unsigned, U8, 1]  // $0(4) = 0
        *(($0 + 12) + (5 as SP)) = 0 [unsigned, U8, 1]  // $0(5) = 0
        *(($0 + 12) + (6 as SP)) = 0 [unsigned, U8, 1]  // $0(6) = 0
        *(($0 + 12) + (7 as SP)) = 0 [unsigned, U8, 1]  // $0(7) = 0
        *(($0 + 12) + (8 as SP)) = 0 [unsigned, U8, 1]  // $0(8) = 0
        *(($0 + 12) + (9 as SP)) = 0 [unsigned, U8, 1]  // $0(9) = 0
        *(($0 + 12) + (10 as SP)) = 0 [unsigned, U8, 1]  // $0(10) = 0
        *(($0 + 12) + (11 as SP)) = 0 [unsigned, U8, 1]  // $0(11) = 0
        *(($0 + 12) + (12 as SP)) = 0 [unsigned, U8, 1]  // $0(12) = 0
        *(($0 + 12) + (13 as SP)) = 0 [unsigned, U8, 1]  // $0(13) = 0
        *(($0 + 12) + (14 as SP)) = 0 [unsigned, U8, 1]  // $0(14) = 0
        *(($0 + 12) + (15 as SP)) = 0 [unsigned, U8, 1]  // $0(15) = 0
        goto .41
        */


        val __tmp_426 = generalRegFiles(0.U) + 12.U(16.W) + 0.S(8.W).asUInt
        val __tmp_427 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_426 + 0.U) := __tmp_427(7, 0)

        val __tmp_428 = generalRegFiles(0.U) + 12.U(16.W) + 1.S(8.W).asUInt
        val __tmp_429 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_428 + 0.U) := __tmp_429(7, 0)

        val __tmp_430 = generalRegFiles(0.U) + 12.U(16.W) + 2.S(8.W).asUInt
        val __tmp_431 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_430 + 0.U) := __tmp_431(7, 0)

        val __tmp_432 = generalRegFiles(0.U) + 12.U(16.W) + 3.S(8.W).asUInt
        val __tmp_433 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_432 + 0.U) := __tmp_433(7, 0)

        val __tmp_434 = generalRegFiles(0.U) + 12.U(16.W) + 4.S(8.W).asUInt
        val __tmp_435 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_434 + 0.U) := __tmp_435(7, 0)

        val __tmp_436 = generalRegFiles(0.U) + 12.U(16.W) + 5.S(8.W).asUInt
        val __tmp_437 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_436 + 0.U) := __tmp_437(7, 0)

        val __tmp_438 = generalRegFiles(0.U) + 12.U(16.W) + 6.S(8.W).asUInt
        val __tmp_439 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_438 + 0.U) := __tmp_439(7, 0)

        val __tmp_440 = generalRegFiles(0.U) + 12.U(16.W) + 7.S(8.W).asUInt
        val __tmp_441 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_440 + 0.U) := __tmp_441(7, 0)

        val __tmp_442 = generalRegFiles(0.U) + 12.U(16.W) + 8.S(8.W).asUInt
        val __tmp_443 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_442 + 0.U) := __tmp_443(7, 0)

        val __tmp_444 = generalRegFiles(0.U) + 12.U(16.W) + 9.S(8.W).asUInt
        val __tmp_445 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_444 + 0.U) := __tmp_445(7, 0)

        val __tmp_446 = generalRegFiles(0.U) + 12.U(16.W) + 10.S(8.W).asUInt
        val __tmp_447 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_446 + 0.U) := __tmp_447(7, 0)

        val __tmp_448 = generalRegFiles(0.U) + 12.U(16.W) + 11.S(8.W).asUInt
        val __tmp_449 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_448 + 0.U) := __tmp_449(7, 0)

        val __tmp_450 = generalRegFiles(0.U) + 12.U(16.W) + 12.S(8.W).asUInt
        val __tmp_451 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_450 + 0.U) := __tmp_451(7, 0)

        val __tmp_452 = generalRegFiles(0.U) + 12.U(16.W) + 13.S(8.W).asUInt
        val __tmp_453 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_452 + 0.U) := __tmp_453(7, 0)

        val __tmp_454 = generalRegFiles(0.U) + 12.U(16.W) + 14.S(8.W).asUInt
        val __tmp_455 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_454 + 0.U) := __tmp_455(7, 0)

        val __tmp_456 = generalRegFiles(0.U) + 12.U(16.W) + 15.S(8.W).asUInt
        val __tmp_457 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_456 + 0.U) := __tmp_457(7, 0)

        CP := 41.U
      }

      is(41.U) {
        /*
        *(SP + 108) [MS[anvil.PrinterIndex.I16, U8], 30]  <-  $0 [MS[anvil.PrinterIndex.I16, U8], 30]  // buff = $0
        goto .42
        */


        val __tmp_458 = Cat(
          arrayRegFiles(SP + 108.U(16.W) + 1.U),
          arrayRegFiles(SP + 108.U(16.W) + 0.U)
        )
        val __tmp_459 = generalRegFiles(0.U)
        val __tmp_460 = 30.U(64.W)

        when(Idx < __tmp_460) {
          arrayRegFiles(__tmp_458 + Idx + 0.U) := arrayRegFiles(__tmp_459 + Idx + 0.U)
          arrayRegFiles(__tmp_458 + Idx + 1.U) := arrayRegFiles(__tmp_459 + Idx + 1.U)
          arrayRegFiles(__tmp_458 + Idx + 2.U) := arrayRegFiles(__tmp_459 + Idx + 2.U)
          arrayRegFiles(__tmp_458 + Idx + 3.U) := arrayRegFiles(__tmp_459 + Idx + 3.U)
          arrayRegFiles(__tmp_458 + Idx + 4.U) := arrayRegFiles(__tmp_459 + Idx + 4.U)
          arrayRegFiles(__tmp_458 + Idx + 5.U) := arrayRegFiles(__tmp_459 + Idx + 5.U)
          arrayRegFiles(__tmp_458 + Idx + 6.U) := arrayRegFiles(__tmp_459 + Idx + 6.U)
          arrayRegFiles(__tmp_458 + Idx + 7.U) := arrayRegFiles(__tmp_459 + Idx + 7.U)
          Idx := Idx + 8.U
          LeftByteRounds := __tmp_460 - Idx
        } .elsewhen(IdxLeftByteRounds < LeftByteRounds) {
          val __tmp_461 = Idx - 8.U
          arrayRegFiles(__tmp_458 + __tmp_461 + IdxLeftByteRounds) := arrayRegFiles(__tmp_459 + __tmp_461 + IdxLeftByteRounds)
          IdxLeftByteRounds := IdxLeftByteRounds + 1.U
        } .otherwise {
          Idx := 0.U
          IdxLeftByteRounds := 0.U
          LeftByteRounds := 0.U
          CP := 42.U
        }


      }

      is(42.U) {
        /*
        unalloc $new@[249,16].11A13D4B: MS[anvil.PrinterIndex.I16, U8] [@140, 30]
        goto .43
        */


        CP := 43.U
      }

      is(43.U) {
        /*
        *(SP + 6) = 253 [signed, U32, 4]  // $sfLoc = 253
        goto .44
        */


        val __tmp_462 = SP + 6.U(16.W)
        val __tmp_463 = (253.S(32.W)).asUInt
        arrayRegFiles(__tmp_462 + 0.U) := __tmp_463(7, 0)
        arrayRegFiles(__tmp_462 + 1.U) := __tmp_463(15, 8)
        arrayRegFiles(__tmp_462 + 2.U) := __tmp_463(23, 16)
        arrayRegFiles(__tmp_462 + 3.U) := __tmp_463(31, 24)

        CP := 44.U
      }

      is(44.U) {
        /*
        decl i: anvil.PrinterIndex.I16 [@140, 1]
        *(SP + 140) = 0 [signed, anvil.PrinterIndex.I16, 1]  // i = 0
        goto .45
        */


        val __tmp_464 = SP + 140.U(16.W)
        val __tmp_465 = (0.S(8.W)).asUInt
        arrayRegFiles(__tmp_464 + 0.U) := __tmp_465(7, 0)

        CP := 45.U
      }

      is(45.U) {
        /*
        *(SP + 6) = 254 [signed, U32, 4]  // $sfLoc = 254
        goto .46
        */


        val __tmp_466 = SP + 6.U(16.W)
        val __tmp_467 = (254.S(32.W)).asUInt
        arrayRegFiles(__tmp_466 + 0.U) := __tmp_467(7, 0)
        arrayRegFiles(__tmp_466 + 1.U) := __tmp_467(15, 8)
        arrayRegFiles(__tmp_466 + 2.U) := __tmp_467(23, 16)
        arrayRegFiles(__tmp_466 + 3.U) := __tmp_467(31, 24)

        CP := 46.U
      }

      is(46.U) {
        /*
        decl m: U64 [@141, 8]
        $0 = *(SP + 92) [unsigned, U64, 8]  // $0 = n
        goto .47
        */


        val __tmp_468 = (SP + 92.U(16.W)).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_468 + 7.U),
          arrayRegFiles(__tmp_468 + 6.U),
          arrayRegFiles(__tmp_468 + 5.U),
          arrayRegFiles(__tmp_468 + 4.U),
          arrayRegFiles(__tmp_468 + 3.U),
          arrayRegFiles(__tmp_468 + 2.U),
          arrayRegFiles(__tmp_468 + 1.U),
          arrayRegFiles(__tmp_468 + 0.U)
        ).asUInt

        CP := 47.U
      }

      is(47.U) {
        /*
        *(SP + 141) = $0 [signed, U64, 8]  // m = $0
        goto .48
        */


        val __tmp_469 = SP + 141.U(16.W)
        val __tmp_470 = (generalRegFiles(0.U)).asUInt
        arrayRegFiles(__tmp_469 + 0.U) := __tmp_470(7, 0)
        arrayRegFiles(__tmp_469 + 1.U) := __tmp_470(15, 8)
        arrayRegFiles(__tmp_469 + 2.U) := __tmp_470(23, 16)
        arrayRegFiles(__tmp_469 + 3.U) := __tmp_470(31, 24)
        arrayRegFiles(__tmp_469 + 4.U) := __tmp_470(39, 32)
        arrayRegFiles(__tmp_469 + 5.U) := __tmp_470(47, 40)
        arrayRegFiles(__tmp_469 + 6.U) := __tmp_470(55, 48)
        arrayRegFiles(__tmp_469 + 7.U) := __tmp_470(63, 56)

        CP := 48.U
      }

      is(48.U) {
        /*
        *(SP + 6) = 255 [signed, U32, 4]  // $sfLoc = 255
        goto .49
        */


        val __tmp_471 = SP + 6.U(16.W)
        val __tmp_472 = (255.S(32.W)).asUInt
        arrayRegFiles(__tmp_471 + 0.U) := __tmp_472(7, 0)
        arrayRegFiles(__tmp_471 + 1.U) := __tmp_472(15, 8)
        arrayRegFiles(__tmp_471 + 2.U) := __tmp_472(23, 16)
        arrayRegFiles(__tmp_471 + 3.U) := __tmp_472(31, 24)

        CP := 49.U
      }

      is(49.U) {
        /*
        decl d: Z [@149, 8]
        $0 = *(SP + 100) [signed, Z, 8]  // $0 = digits
        goto .50
        */


        val __tmp_473 = (SP + 100.U(16.W)).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_473 + 7.U),
          arrayRegFiles(__tmp_473 + 6.U),
          arrayRegFiles(__tmp_473 + 5.U),
          arrayRegFiles(__tmp_473 + 4.U),
          arrayRegFiles(__tmp_473 + 3.U),
          arrayRegFiles(__tmp_473 + 2.U),
          arrayRegFiles(__tmp_473 + 1.U),
          arrayRegFiles(__tmp_473 + 0.U)
        ).asUInt

        CP := 50.U
      }

      is(50.U) {
        /*
        *(SP + 149) = $0 [signed, Z, 8]  // d = $0
        goto .51
        */


        val __tmp_474 = SP + 149.U(16.W)
        val __tmp_475 = (generalRegFiles(0.U).asSInt).asUInt
        arrayRegFiles(__tmp_474 + 0.U) := __tmp_475(7, 0)
        arrayRegFiles(__tmp_474 + 1.U) := __tmp_475(15, 8)
        arrayRegFiles(__tmp_474 + 2.U) := __tmp_475(23, 16)
        arrayRegFiles(__tmp_474 + 3.U) := __tmp_475(31, 24)
        arrayRegFiles(__tmp_474 + 4.U) := __tmp_475(39, 32)
        arrayRegFiles(__tmp_474 + 5.U) := __tmp_475(47, 40)
        arrayRegFiles(__tmp_474 + 6.U) := __tmp_475(55, 48)
        arrayRegFiles(__tmp_474 + 7.U) := __tmp_475(63, 56)

        CP := 51.U
      }

      is(51.U) {
        /*
        *(SP + 6) = 256 [signed, U32, 4]  // $sfLoc = 256
        goto .52
        */


        val __tmp_476 = SP + 6.U(16.W)
        val __tmp_477 = (256.S(32.W)).asUInt
        arrayRegFiles(__tmp_476 + 0.U) := __tmp_477(7, 0)
        arrayRegFiles(__tmp_476 + 1.U) := __tmp_477(15, 8)
        arrayRegFiles(__tmp_476 + 2.U) := __tmp_477(23, 16)
        arrayRegFiles(__tmp_476 + 3.U) := __tmp_477(31, 24)

        CP := 52.U
      }

      is(52.U) {
        /*
        *(SP + 6) = 256 [signed, U32, 4]  // $sfLoc = 256
        goto .53
        */


        val __tmp_478 = SP + 6.U(16.W)
        val __tmp_479 = (256.S(32.W)).asUInt
        arrayRegFiles(__tmp_478 + 0.U) := __tmp_479(7, 0)
        arrayRegFiles(__tmp_478 + 1.U) := __tmp_479(15, 8)
        arrayRegFiles(__tmp_478 + 2.U) := __tmp_479(23, 16)
        arrayRegFiles(__tmp_478 + 3.U) := __tmp_479(31, 24)

        CP := 53.U
      }

      is(53.U) {
        /*
        $0 = *(SP + 141) [unsigned, U64, 8]  // $0 = m
        goto .54
        */


        val __tmp_480 = (SP + 141.U(16.W)).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_480 + 7.U),
          arrayRegFiles(__tmp_480 + 6.U),
          arrayRegFiles(__tmp_480 + 5.U),
          arrayRegFiles(__tmp_480 + 4.U),
          arrayRegFiles(__tmp_480 + 3.U),
          arrayRegFiles(__tmp_480 + 2.U),
          arrayRegFiles(__tmp_480 + 1.U),
          arrayRegFiles(__tmp_480 + 0.U)
        ).asUInt

        CP := 54.U
      }

      is(54.U) {
        /*
        $1 = ($0 > 0)
        goto .55
        */


        generalRegFiles(1.U) := (generalRegFiles(0.U) > 0.U(64.W)).asUInt
        CP := 55.U
      }

      is(55.U) {
        /*
        if $1 goto .56 else goto .138
        */


        CP := Mux((generalRegFiles(1.U).asUInt) === 1.U, 56.U, 138.U)
      }

      is(56.U) {
        /*
        *(SP + 6) = 257 [signed, U32, 4]  // $sfLoc = 257
        goto .57
        */


        val __tmp_481 = SP + 6.U(16.W)
        val __tmp_482 = (257.S(32.W)).asUInt
        arrayRegFiles(__tmp_481 + 0.U) := __tmp_482(7, 0)
        arrayRegFiles(__tmp_481 + 1.U) := __tmp_482(15, 8)
        arrayRegFiles(__tmp_481 + 2.U) := __tmp_482(23, 16)
        arrayRegFiles(__tmp_481 + 3.U) := __tmp_482(31, 24)

        CP := 57.U
      }

      is(57.U) {
        /*
        $0 = *(SP + 141) [unsigned, U64, 8]  // $0 = m
        goto .58
        */


        val __tmp_483 = (SP + 141.U(16.W)).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_483 + 7.U),
          arrayRegFiles(__tmp_483 + 6.U),
          arrayRegFiles(__tmp_483 + 5.U),
          arrayRegFiles(__tmp_483 + 4.U),
          arrayRegFiles(__tmp_483 + 3.U),
          arrayRegFiles(__tmp_483 + 2.U),
          arrayRegFiles(__tmp_483 + 1.U),
          arrayRegFiles(__tmp_483 + 0.U)
        ).asUInt

        CP := 58.U
      }

      is(58.U) {
        /*
        $1 = ($0 & 15)
        goto .59
        */


        generalRegFiles(1.U) := generalRegFiles(0.U) & 15.U(64.W)
        CP := 59.U
      }

      is(59.U) {
        /*
        *(SP + 6) = 248 [signed, U32, 4]  // $sfLoc = 248
        goto .60
        */


        val __tmp_484 = SP + 6.U(16.W)
        val __tmp_485 = (248.S(32.W)).asUInt
        arrayRegFiles(__tmp_484 + 0.U) := __tmp_485(7, 0)
        arrayRegFiles(__tmp_484 + 1.U) := __tmp_485(15, 8)
        arrayRegFiles(__tmp_484 + 2.U) := __tmp_485(23, 16)
        arrayRegFiles(__tmp_484 + 3.U) := __tmp_485(31, 24)

        CP := 60.U
      }

      is(60.U) {
        /*
        switch ($1)
          0: goto 61
          1: goto 78
          2: goto 82
          3: goto 86
          4: goto 90
          5: goto 94
          6: goto 98
          7: goto 102
          8: goto 106
          9: goto 110
          10: goto 114
          11: goto 118
          12: goto 122
          13: goto 126
          14: goto 130
          15: goto 134

        */


        val __tmp_486 = generalRegFiles(1.U)

        switch(__tmp_486) {

          is(0.U(64.W)) {
            CP := 61.U
          }


          is(1.U(64.W)) {
            CP := 78.U
          }


          is(2.U(64.W)) {
            CP := 82.U
          }


          is(3.U(64.W)) {
            CP := 86.U
          }


          is(4.U(64.W)) {
            CP := 90.U
          }


          is(5.U(64.W)) {
            CP := 94.U
          }


          is(6.U(64.W)) {
            CP := 98.U
          }


          is(7.U(64.W)) {
            CP := 102.U
          }


          is(8.U(64.W)) {
            CP := 106.U
          }


          is(9.U(64.W)) {
            CP := 110.U
          }


          is(10.U(64.W)) {
            CP := 114.U
          }


          is(11.U(64.W)) {
            CP := 118.U
          }


          is(12.U(64.W)) {
            CP := 122.U
          }


          is(13.U(64.W)) {
            CP := 126.U
          }


          is(14.U(64.W)) {
            CP := 130.U
          }


          is(15.U(64.W)) {
            CP := 134.U
          }

        }

      }

      is(61.U) {
        /*
        *(SP + 6) = 258 [signed, U32, 4]  // $sfLoc = 258
        goto .62
        */


        val __tmp_487 = SP + 6.U(16.W)
        val __tmp_488 = (258.S(32.W)).asUInt
        arrayRegFiles(__tmp_487 + 0.U) := __tmp_488(7, 0)
        arrayRegFiles(__tmp_487 + 1.U) := __tmp_488(15, 8)
        arrayRegFiles(__tmp_487 + 2.U) := __tmp_488(23, 16)
        arrayRegFiles(__tmp_487 + 3.U) := __tmp_488(31, 24)

        CP := 62.U
      }

      is(62.U) {
        /*
        $0 = *(SP + 108) [unsigned, MS[anvil.PrinterIndex.I16, U8], 2]  // $0 = buff
        $1 = *(SP + 140) [signed, anvil.PrinterIndex.I16, 1]  // $1 = i
        goto .63
        */


        val __tmp_489 = (SP + 108.U(16.W)).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_489 + 1.U),
          arrayRegFiles(__tmp_489 + 0.U)
        ).asUInt

        val __tmp_490 = (SP + 140.U(16.W)).asUInt
        generalRegFiles(1.U) := Cat(
          arrayRegFiles(__tmp_490 + 0.U)
        ).asUInt

        CP := 63.U
      }

      is(63.U) {
        /*
        *(($0 + 12) + ($1 as SP)) = 48 [unsigned, U8, 1]  // $0($1) = 48
        goto .64
        */


        val __tmp_491 = generalRegFiles(0.U) + 12.U(16.W) + generalRegFiles(1.U).asSInt.asUInt
        val __tmp_492 = (48.U(8.W)).asUInt
        arrayRegFiles(__tmp_491 + 0.U) := __tmp_492(7, 0)

        CP := 64.U
      }

      is(64.U) {
        /*
        *(SP + 6) = 248 [signed, U32, 4]  // $sfLoc = 248
        goto .65
        */


        val __tmp_493 = SP + 6.U(16.W)
        val __tmp_494 = (248.S(32.W)).asUInt
        arrayRegFiles(__tmp_493 + 0.U) := __tmp_494(7, 0)
        arrayRegFiles(__tmp_493 + 1.U) := __tmp_494(15, 8)
        arrayRegFiles(__tmp_493 + 2.U) := __tmp_494(23, 16)
        arrayRegFiles(__tmp_493 + 3.U) := __tmp_494(31, 24)

        CP := 65.U
      }

      is(65.U) {
        /*
        *(SP + 6) = 275 [signed, U32, 4]  // $sfLoc = 275
        goto .66
        */


        val __tmp_495 = SP + 6.U(16.W)
        val __tmp_496 = (275.S(32.W)).asUInt
        arrayRegFiles(__tmp_495 + 0.U) := __tmp_496(7, 0)
        arrayRegFiles(__tmp_495 + 1.U) := __tmp_496(15, 8)
        arrayRegFiles(__tmp_495 + 2.U) := __tmp_496(23, 16)
        arrayRegFiles(__tmp_495 + 3.U) := __tmp_496(31, 24)

        CP := 66.U
      }

      is(66.U) {
        /*
        $0 = *(SP + 141) [unsigned, U64, 8]  // $0 = m
        goto .67
        */


        val __tmp_497 = (SP + 141.U(16.W)).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_497 + 7.U),
          arrayRegFiles(__tmp_497 + 6.U),
          arrayRegFiles(__tmp_497 + 5.U),
          arrayRegFiles(__tmp_497 + 4.U),
          arrayRegFiles(__tmp_497 + 3.U),
          arrayRegFiles(__tmp_497 + 2.U),
          arrayRegFiles(__tmp_497 + 1.U),
          arrayRegFiles(__tmp_497 + 0.U)
        ).asUInt

        CP := 67.U
      }

      is(67.U) {
        /*
        $1 = ($0 >>> 4)
        goto .68
        */


        generalRegFiles(1.U) := (generalRegFiles(0.U)).asUInt >> 4.U(64.W)(4,0)
        CP := 68.U
      }

      is(68.U) {
        /*
        *(SP + 141) = $1 [signed, U64, 8]  // m = $1
        goto .69
        */


        val __tmp_498 = SP + 141.U(16.W)
        val __tmp_499 = (generalRegFiles(1.U)).asUInt
        arrayRegFiles(__tmp_498 + 0.U) := __tmp_499(7, 0)
        arrayRegFiles(__tmp_498 + 1.U) := __tmp_499(15, 8)
        arrayRegFiles(__tmp_498 + 2.U) := __tmp_499(23, 16)
        arrayRegFiles(__tmp_498 + 3.U) := __tmp_499(31, 24)
        arrayRegFiles(__tmp_498 + 4.U) := __tmp_499(39, 32)
        arrayRegFiles(__tmp_498 + 5.U) := __tmp_499(47, 40)
        arrayRegFiles(__tmp_498 + 6.U) := __tmp_499(55, 48)
        arrayRegFiles(__tmp_498 + 7.U) := __tmp_499(63, 56)

        CP := 69.U
      }

      is(69.U) {
        /*
        *(SP + 6) = 276 [signed, U32, 4]  // $sfLoc = 276
        goto .70
        */


        val __tmp_500 = SP + 6.U(16.W)
        val __tmp_501 = (276.S(32.W)).asUInt
        arrayRegFiles(__tmp_500 + 0.U) := __tmp_501(7, 0)
        arrayRegFiles(__tmp_500 + 1.U) := __tmp_501(15, 8)
        arrayRegFiles(__tmp_500 + 2.U) := __tmp_501(23, 16)
        arrayRegFiles(__tmp_500 + 3.U) := __tmp_501(31, 24)

        CP := 70.U
      }

      is(70.U) {
        /*
        $0 = *(SP + 140) [signed, anvil.PrinterIndex.I16, 1]  // $0 = i
        goto .71
        */


        val __tmp_502 = (SP + 140.U(16.W)).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_502 + 0.U)
        ).asUInt

        CP := 71.U
      }

      is(71.U) {
        /*
        $1 = ($0 + 1)
        goto .72
        */


        generalRegFiles(1.U) := (generalRegFiles(0.U).asSInt + 1.S(8.W)).asUInt
        CP := 72.U
      }

      is(72.U) {
        /*
        *(SP + 140) = $1 [signed, anvil.PrinterIndex.I16, 1]  // i = $1
        goto .73
        */


        val __tmp_503 = SP + 140.U(16.W)
        val __tmp_504 = (generalRegFiles(1.U).asSInt).asUInt
        arrayRegFiles(__tmp_503 + 0.U) := __tmp_504(7, 0)

        CP := 73.U
      }

      is(73.U) {
        /*
        *(SP + 6) = 277 [signed, U32, 4]  // $sfLoc = 277
        goto .74
        */


        val __tmp_505 = SP + 6.U(16.W)
        val __tmp_506 = (277.S(32.W)).asUInt
        arrayRegFiles(__tmp_505 + 0.U) := __tmp_506(7, 0)
        arrayRegFiles(__tmp_505 + 1.U) := __tmp_506(15, 8)
        arrayRegFiles(__tmp_505 + 2.U) := __tmp_506(23, 16)
        arrayRegFiles(__tmp_505 + 3.U) := __tmp_506(31, 24)

        CP := 74.U
      }

      is(74.U) {
        /*
        $0 = *(SP + 149) [signed, Z, 8]  // $0 = d
        goto .75
        */


        val __tmp_507 = (SP + 149.U(16.W)).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_507 + 7.U),
          arrayRegFiles(__tmp_507 + 6.U),
          arrayRegFiles(__tmp_507 + 5.U),
          arrayRegFiles(__tmp_507 + 4.U),
          arrayRegFiles(__tmp_507 + 3.U),
          arrayRegFiles(__tmp_507 + 2.U),
          arrayRegFiles(__tmp_507 + 1.U),
          arrayRegFiles(__tmp_507 + 0.U)
        ).asUInt

        CP := 75.U
      }

      is(75.U) {
        /*
        $1 = ($0 - 1)
        goto .76
        */


        generalRegFiles(1.U) := (generalRegFiles(0.U).asSInt - 1.S(64.W)).asUInt
        CP := 76.U
      }

      is(76.U) {
        /*
        *(SP + 149) = $1 [signed, Z, 8]  // d = $1
        goto .77
        */


        val __tmp_508 = SP + 149.U(16.W)
        val __tmp_509 = (generalRegFiles(1.U).asSInt).asUInt
        arrayRegFiles(__tmp_508 + 0.U) := __tmp_509(7, 0)
        arrayRegFiles(__tmp_508 + 1.U) := __tmp_509(15, 8)
        arrayRegFiles(__tmp_508 + 2.U) := __tmp_509(23, 16)
        arrayRegFiles(__tmp_508 + 3.U) := __tmp_509(31, 24)
        arrayRegFiles(__tmp_508 + 4.U) := __tmp_509(39, 32)
        arrayRegFiles(__tmp_508 + 5.U) := __tmp_509(47, 40)
        arrayRegFiles(__tmp_508 + 6.U) := __tmp_509(55, 48)
        arrayRegFiles(__tmp_508 + 7.U) := __tmp_509(63, 56)

        CP := 77.U
      }

      is(77.U) {
        /*
        *(SP + 6) = 256 [signed, U32, 4]  // $sfLoc = 256
        goto .52
        */


        val __tmp_510 = SP + 6.U(16.W)
        val __tmp_511 = (256.S(32.W)).asUInt
        arrayRegFiles(__tmp_510 + 0.U) := __tmp_511(7, 0)
        arrayRegFiles(__tmp_510 + 1.U) := __tmp_511(15, 8)
        arrayRegFiles(__tmp_510 + 2.U) := __tmp_511(23, 16)
        arrayRegFiles(__tmp_510 + 3.U) := __tmp_511(31, 24)

        CP := 52.U
      }

      is(78.U) {
        /*
        *(SP + 6) = 259 [signed, U32, 4]  // $sfLoc = 259
        goto .79
        */


        val __tmp_512 = SP + 6.U(16.W)
        val __tmp_513 = (259.S(32.W)).asUInt
        arrayRegFiles(__tmp_512 + 0.U) := __tmp_513(7, 0)
        arrayRegFiles(__tmp_512 + 1.U) := __tmp_513(15, 8)
        arrayRegFiles(__tmp_512 + 2.U) := __tmp_513(23, 16)
        arrayRegFiles(__tmp_512 + 3.U) := __tmp_513(31, 24)

        CP := 79.U
      }

      is(79.U) {
        /*
        $0 = *(SP + 108) [unsigned, MS[anvil.PrinterIndex.I16, U8], 2]  // $0 = buff
        $1 = *(SP + 140) [signed, anvil.PrinterIndex.I16, 1]  // $1 = i
        goto .80
        */


        val __tmp_514 = (SP + 108.U(16.W)).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_514 + 1.U),
          arrayRegFiles(__tmp_514 + 0.U)
        ).asUInt

        val __tmp_515 = (SP + 140.U(16.W)).asUInt
        generalRegFiles(1.U) := Cat(
          arrayRegFiles(__tmp_515 + 0.U)
        ).asUInt

        CP := 80.U
      }

      is(80.U) {
        /*
        *(($0 + 12) + ($1 as SP)) = 49 [unsigned, U8, 1]  // $0($1) = 49
        goto .81
        */


        val __tmp_516 = generalRegFiles(0.U) + 12.U(16.W) + generalRegFiles(1.U).asSInt.asUInt
        val __tmp_517 = (49.U(8.W)).asUInt
        arrayRegFiles(__tmp_516 + 0.U) := __tmp_517(7, 0)

        CP := 81.U
      }

      is(81.U) {
        /*
        *(SP + 6) = 248 [signed, U32, 4]  // $sfLoc = 248
        goto .65
        */


        val __tmp_518 = SP + 6.U(16.W)
        val __tmp_519 = (248.S(32.W)).asUInt
        arrayRegFiles(__tmp_518 + 0.U) := __tmp_519(7, 0)
        arrayRegFiles(__tmp_518 + 1.U) := __tmp_519(15, 8)
        arrayRegFiles(__tmp_518 + 2.U) := __tmp_519(23, 16)
        arrayRegFiles(__tmp_518 + 3.U) := __tmp_519(31, 24)

        CP := 65.U
      }

      is(82.U) {
        /*
        *(SP + 6) = 260 [signed, U32, 4]  // $sfLoc = 260
        goto .83
        */


        val __tmp_520 = SP + 6.U(16.W)
        val __tmp_521 = (260.S(32.W)).asUInt
        arrayRegFiles(__tmp_520 + 0.U) := __tmp_521(7, 0)
        arrayRegFiles(__tmp_520 + 1.U) := __tmp_521(15, 8)
        arrayRegFiles(__tmp_520 + 2.U) := __tmp_521(23, 16)
        arrayRegFiles(__tmp_520 + 3.U) := __tmp_521(31, 24)

        CP := 83.U
      }

      is(83.U) {
        /*
        $0 = *(SP + 108) [unsigned, MS[anvil.PrinterIndex.I16, U8], 2]  // $0 = buff
        $1 = *(SP + 140) [signed, anvil.PrinterIndex.I16, 1]  // $1 = i
        goto .84
        */


        val __tmp_522 = (SP + 108.U(16.W)).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_522 + 1.U),
          arrayRegFiles(__tmp_522 + 0.U)
        ).asUInt

        val __tmp_523 = (SP + 140.U(16.W)).asUInt
        generalRegFiles(1.U) := Cat(
          arrayRegFiles(__tmp_523 + 0.U)
        ).asUInt

        CP := 84.U
      }

      is(84.U) {
        /*
        *(($0 + 12) + ($1 as SP)) = 50 [unsigned, U8, 1]  // $0($1) = 50
        goto .85
        */


        val __tmp_524 = generalRegFiles(0.U) + 12.U(16.W) + generalRegFiles(1.U).asSInt.asUInt
        val __tmp_525 = (50.U(8.W)).asUInt
        arrayRegFiles(__tmp_524 + 0.U) := __tmp_525(7, 0)

        CP := 85.U
      }

      is(85.U) {
        /*
        *(SP + 6) = 248 [signed, U32, 4]  // $sfLoc = 248
        goto .65
        */


        val __tmp_526 = SP + 6.U(16.W)
        val __tmp_527 = (248.S(32.W)).asUInt
        arrayRegFiles(__tmp_526 + 0.U) := __tmp_527(7, 0)
        arrayRegFiles(__tmp_526 + 1.U) := __tmp_527(15, 8)
        arrayRegFiles(__tmp_526 + 2.U) := __tmp_527(23, 16)
        arrayRegFiles(__tmp_526 + 3.U) := __tmp_527(31, 24)

        CP := 65.U
      }

      is(86.U) {
        /*
        *(SP + 6) = 261 [signed, U32, 4]  // $sfLoc = 261
        goto .87
        */


        val __tmp_528 = SP + 6.U(16.W)
        val __tmp_529 = (261.S(32.W)).asUInt
        arrayRegFiles(__tmp_528 + 0.U) := __tmp_529(7, 0)
        arrayRegFiles(__tmp_528 + 1.U) := __tmp_529(15, 8)
        arrayRegFiles(__tmp_528 + 2.U) := __tmp_529(23, 16)
        arrayRegFiles(__tmp_528 + 3.U) := __tmp_529(31, 24)

        CP := 87.U
      }

      is(87.U) {
        /*
        $0 = *(SP + 108) [unsigned, MS[anvil.PrinterIndex.I16, U8], 2]  // $0 = buff
        $1 = *(SP + 140) [signed, anvil.PrinterIndex.I16, 1]  // $1 = i
        goto .88
        */


        val __tmp_530 = (SP + 108.U(16.W)).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_530 + 1.U),
          arrayRegFiles(__tmp_530 + 0.U)
        ).asUInt

        val __tmp_531 = (SP + 140.U(16.W)).asUInt
        generalRegFiles(1.U) := Cat(
          arrayRegFiles(__tmp_531 + 0.U)
        ).asUInt

        CP := 88.U
      }

      is(88.U) {
        /*
        *(($0 + 12) + ($1 as SP)) = 51 [unsigned, U8, 1]  // $0($1) = 51
        goto .89
        */


        val __tmp_532 = generalRegFiles(0.U) + 12.U(16.W) + generalRegFiles(1.U).asSInt.asUInt
        val __tmp_533 = (51.U(8.W)).asUInt
        arrayRegFiles(__tmp_532 + 0.U) := __tmp_533(7, 0)

        CP := 89.U
      }

      is(89.U) {
        /*
        *(SP + 6) = 248 [signed, U32, 4]  // $sfLoc = 248
        goto .65
        */


        val __tmp_534 = SP + 6.U(16.W)
        val __tmp_535 = (248.S(32.W)).asUInt
        arrayRegFiles(__tmp_534 + 0.U) := __tmp_535(7, 0)
        arrayRegFiles(__tmp_534 + 1.U) := __tmp_535(15, 8)
        arrayRegFiles(__tmp_534 + 2.U) := __tmp_535(23, 16)
        arrayRegFiles(__tmp_534 + 3.U) := __tmp_535(31, 24)

        CP := 65.U
      }

      is(90.U) {
        /*
        *(SP + 6) = 262 [signed, U32, 4]  // $sfLoc = 262
        goto .91
        */


        val __tmp_536 = SP + 6.U(16.W)
        val __tmp_537 = (262.S(32.W)).asUInt
        arrayRegFiles(__tmp_536 + 0.U) := __tmp_537(7, 0)
        arrayRegFiles(__tmp_536 + 1.U) := __tmp_537(15, 8)
        arrayRegFiles(__tmp_536 + 2.U) := __tmp_537(23, 16)
        arrayRegFiles(__tmp_536 + 3.U) := __tmp_537(31, 24)

        CP := 91.U
      }

      is(91.U) {
        /*
        $0 = *(SP + 108) [unsigned, MS[anvil.PrinterIndex.I16, U8], 2]  // $0 = buff
        $1 = *(SP + 140) [signed, anvil.PrinterIndex.I16, 1]  // $1 = i
        goto .92
        */


        val __tmp_538 = (SP + 108.U(16.W)).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_538 + 1.U),
          arrayRegFiles(__tmp_538 + 0.U)
        ).asUInt

        val __tmp_539 = (SP + 140.U(16.W)).asUInt
        generalRegFiles(1.U) := Cat(
          arrayRegFiles(__tmp_539 + 0.U)
        ).asUInt

        CP := 92.U
      }

      is(92.U) {
        /*
        *(($0 + 12) + ($1 as SP)) = 52 [unsigned, U8, 1]  // $0($1) = 52
        goto .93
        */


        val __tmp_540 = generalRegFiles(0.U) + 12.U(16.W) + generalRegFiles(1.U).asSInt.asUInt
        val __tmp_541 = (52.U(8.W)).asUInt
        arrayRegFiles(__tmp_540 + 0.U) := __tmp_541(7, 0)

        CP := 93.U
      }

      is(93.U) {
        /*
        *(SP + 6) = 248 [signed, U32, 4]  // $sfLoc = 248
        goto .65
        */


        val __tmp_542 = SP + 6.U(16.W)
        val __tmp_543 = (248.S(32.W)).asUInt
        arrayRegFiles(__tmp_542 + 0.U) := __tmp_543(7, 0)
        arrayRegFiles(__tmp_542 + 1.U) := __tmp_543(15, 8)
        arrayRegFiles(__tmp_542 + 2.U) := __tmp_543(23, 16)
        arrayRegFiles(__tmp_542 + 3.U) := __tmp_543(31, 24)

        CP := 65.U
      }

      is(94.U) {
        /*
        *(SP + 6) = 263 [signed, U32, 4]  // $sfLoc = 263
        goto .95
        */


        val __tmp_544 = SP + 6.U(16.W)
        val __tmp_545 = (263.S(32.W)).asUInt
        arrayRegFiles(__tmp_544 + 0.U) := __tmp_545(7, 0)
        arrayRegFiles(__tmp_544 + 1.U) := __tmp_545(15, 8)
        arrayRegFiles(__tmp_544 + 2.U) := __tmp_545(23, 16)
        arrayRegFiles(__tmp_544 + 3.U) := __tmp_545(31, 24)

        CP := 95.U
      }

      is(95.U) {
        /*
        $0 = *(SP + 108) [unsigned, MS[anvil.PrinterIndex.I16, U8], 2]  // $0 = buff
        $1 = *(SP + 140) [signed, anvil.PrinterIndex.I16, 1]  // $1 = i
        goto .96
        */


        val __tmp_546 = (SP + 108.U(16.W)).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_546 + 1.U),
          arrayRegFiles(__tmp_546 + 0.U)
        ).asUInt

        val __tmp_547 = (SP + 140.U(16.W)).asUInt
        generalRegFiles(1.U) := Cat(
          arrayRegFiles(__tmp_547 + 0.U)
        ).asUInt

        CP := 96.U
      }

      is(96.U) {
        /*
        *(($0 + 12) + ($1 as SP)) = 53 [unsigned, U8, 1]  // $0($1) = 53
        goto .97
        */


        val __tmp_548 = generalRegFiles(0.U) + 12.U(16.W) + generalRegFiles(1.U).asSInt.asUInt
        val __tmp_549 = (53.U(8.W)).asUInt
        arrayRegFiles(__tmp_548 + 0.U) := __tmp_549(7, 0)

        CP := 97.U
      }

      is(97.U) {
        /*
        *(SP + 6) = 248 [signed, U32, 4]  // $sfLoc = 248
        goto .65
        */


        val __tmp_550 = SP + 6.U(16.W)
        val __tmp_551 = (248.S(32.W)).asUInt
        arrayRegFiles(__tmp_550 + 0.U) := __tmp_551(7, 0)
        arrayRegFiles(__tmp_550 + 1.U) := __tmp_551(15, 8)
        arrayRegFiles(__tmp_550 + 2.U) := __tmp_551(23, 16)
        arrayRegFiles(__tmp_550 + 3.U) := __tmp_551(31, 24)

        CP := 65.U
      }

      is(98.U) {
        /*
        *(SP + 6) = 264 [signed, U32, 4]  // $sfLoc = 264
        goto .99
        */


        val __tmp_552 = SP + 6.U(16.W)
        val __tmp_553 = (264.S(32.W)).asUInt
        arrayRegFiles(__tmp_552 + 0.U) := __tmp_553(7, 0)
        arrayRegFiles(__tmp_552 + 1.U) := __tmp_553(15, 8)
        arrayRegFiles(__tmp_552 + 2.U) := __tmp_553(23, 16)
        arrayRegFiles(__tmp_552 + 3.U) := __tmp_553(31, 24)

        CP := 99.U
      }

      is(99.U) {
        /*
        $0 = *(SP + 108) [unsigned, MS[anvil.PrinterIndex.I16, U8], 2]  // $0 = buff
        $1 = *(SP + 140) [signed, anvil.PrinterIndex.I16, 1]  // $1 = i
        goto .100
        */


        val __tmp_554 = (SP + 108.U(16.W)).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_554 + 1.U),
          arrayRegFiles(__tmp_554 + 0.U)
        ).asUInt

        val __tmp_555 = (SP + 140.U(16.W)).asUInt
        generalRegFiles(1.U) := Cat(
          arrayRegFiles(__tmp_555 + 0.U)
        ).asUInt

        CP := 100.U
      }

      is(100.U) {
        /*
        *(($0 + 12) + ($1 as SP)) = 54 [unsigned, U8, 1]  // $0($1) = 54
        goto .101
        */


        val __tmp_556 = generalRegFiles(0.U) + 12.U(16.W) + generalRegFiles(1.U).asSInt.asUInt
        val __tmp_557 = (54.U(8.W)).asUInt
        arrayRegFiles(__tmp_556 + 0.U) := __tmp_557(7, 0)

        CP := 101.U
      }

      is(101.U) {
        /*
        *(SP + 6) = 248 [signed, U32, 4]  // $sfLoc = 248
        goto .65
        */


        val __tmp_558 = SP + 6.U(16.W)
        val __tmp_559 = (248.S(32.W)).asUInt
        arrayRegFiles(__tmp_558 + 0.U) := __tmp_559(7, 0)
        arrayRegFiles(__tmp_558 + 1.U) := __tmp_559(15, 8)
        arrayRegFiles(__tmp_558 + 2.U) := __tmp_559(23, 16)
        arrayRegFiles(__tmp_558 + 3.U) := __tmp_559(31, 24)

        CP := 65.U
      }

      is(102.U) {
        /*
        *(SP + 6) = 265 [signed, U32, 4]  // $sfLoc = 265
        goto .103
        */


        val __tmp_560 = SP + 6.U(16.W)
        val __tmp_561 = (265.S(32.W)).asUInt
        arrayRegFiles(__tmp_560 + 0.U) := __tmp_561(7, 0)
        arrayRegFiles(__tmp_560 + 1.U) := __tmp_561(15, 8)
        arrayRegFiles(__tmp_560 + 2.U) := __tmp_561(23, 16)
        arrayRegFiles(__tmp_560 + 3.U) := __tmp_561(31, 24)

        CP := 103.U
      }

      is(103.U) {
        /*
        $0 = *(SP + 108) [unsigned, MS[anvil.PrinterIndex.I16, U8], 2]  // $0 = buff
        $1 = *(SP + 140) [signed, anvil.PrinterIndex.I16, 1]  // $1 = i
        goto .104
        */


        val __tmp_562 = (SP + 108.U(16.W)).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_562 + 1.U),
          arrayRegFiles(__tmp_562 + 0.U)
        ).asUInt

        val __tmp_563 = (SP + 140.U(16.W)).asUInt
        generalRegFiles(1.U) := Cat(
          arrayRegFiles(__tmp_563 + 0.U)
        ).asUInt

        CP := 104.U
      }

      is(104.U) {
        /*
        *(($0 + 12) + ($1 as SP)) = 55 [unsigned, U8, 1]  // $0($1) = 55
        goto .105
        */


        val __tmp_564 = generalRegFiles(0.U) + 12.U(16.W) + generalRegFiles(1.U).asSInt.asUInt
        val __tmp_565 = (55.U(8.W)).asUInt
        arrayRegFiles(__tmp_564 + 0.U) := __tmp_565(7, 0)

        CP := 105.U
      }

      is(105.U) {
        /*
        *(SP + 6) = 248 [signed, U32, 4]  // $sfLoc = 248
        goto .65
        */


        val __tmp_566 = SP + 6.U(16.W)
        val __tmp_567 = (248.S(32.W)).asUInt
        arrayRegFiles(__tmp_566 + 0.U) := __tmp_567(7, 0)
        arrayRegFiles(__tmp_566 + 1.U) := __tmp_567(15, 8)
        arrayRegFiles(__tmp_566 + 2.U) := __tmp_567(23, 16)
        arrayRegFiles(__tmp_566 + 3.U) := __tmp_567(31, 24)

        CP := 65.U
      }

      is(106.U) {
        /*
        *(SP + 6) = 266 [signed, U32, 4]  // $sfLoc = 266
        goto .107
        */


        val __tmp_568 = SP + 6.U(16.W)
        val __tmp_569 = (266.S(32.W)).asUInt
        arrayRegFiles(__tmp_568 + 0.U) := __tmp_569(7, 0)
        arrayRegFiles(__tmp_568 + 1.U) := __tmp_569(15, 8)
        arrayRegFiles(__tmp_568 + 2.U) := __tmp_569(23, 16)
        arrayRegFiles(__tmp_568 + 3.U) := __tmp_569(31, 24)

        CP := 107.U
      }

      is(107.U) {
        /*
        $0 = *(SP + 108) [unsigned, MS[anvil.PrinterIndex.I16, U8], 2]  // $0 = buff
        $1 = *(SP + 140) [signed, anvil.PrinterIndex.I16, 1]  // $1 = i
        goto .108
        */


        val __tmp_570 = (SP + 108.U(16.W)).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_570 + 1.U),
          arrayRegFiles(__tmp_570 + 0.U)
        ).asUInt

        val __tmp_571 = (SP + 140.U(16.W)).asUInt
        generalRegFiles(1.U) := Cat(
          arrayRegFiles(__tmp_571 + 0.U)
        ).asUInt

        CP := 108.U
      }

      is(108.U) {
        /*
        *(($0 + 12) + ($1 as SP)) = 56 [unsigned, U8, 1]  // $0($1) = 56
        goto .109
        */


        val __tmp_572 = generalRegFiles(0.U) + 12.U(16.W) + generalRegFiles(1.U).asSInt.asUInt
        val __tmp_573 = (56.U(8.W)).asUInt
        arrayRegFiles(__tmp_572 + 0.U) := __tmp_573(7, 0)

        CP := 109.U
      }

      is(109.U) {
        /*
        *(SP + 6) = 248 [signed, U32, 4]  // $sfLoc = 248
        goto .65
        */


        val __tmp_574 = SP + 6.U(16.W)
        val __tmp_575 = (248.S(32.W)).asUInt
        arrayRegFiles(__tmp_574 + 0.U) := __tmp_575(7, 0)
        arrayRegFiles(__tmp_574 + 1.U) := __tmp_575(15, 8)
        arrayRegFiles(__tmp_574 + 2.U) := __tmp_575(23, 16)
        arrayRegFiles(__tmp_574 + 3.U) := __tmp_575(31, 24)

        CP := 65.U
      }

      is(110.U) {
        /*
        *(SP + 6) = 267 [signed, U32, 4]  // $sfLoc = 267
        goto .111
        */


        val __tmp_576 = SP + 6.U(16.W)
        val __tmp_577 = (267.S(32.W)).asUInt
        arrayRegFiles(__tmp_576 + 0.U) := __tmp_577(7, 0)
        arrayRegFiles(__tmp_576 + 1.U) := __tmp_577(15, 8)
        arrayRegFiles(__tmp_576 + 2.U) := __tmp_577(23, 16)
        arrayRegFiles(__tmp_576 + 3.U) := __tmp_577(31, 24)

        CP := 111.U
      }

      is(111.U) {
        /*
        $0 = *(SP + 108) [unsigned, MS[anvil.PrinterIndex.I16, U8], 2]  // $0 = buff
        $1 = *(SP + 140) [signed, anvil.PrinterIndex.I16, 1]  // $1 = i
        goto .112
        */


        val __tmp_578 = (SP + 108.U(16.W)).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_578 + 1.U),
          arrayRegFiles(__tmp_578 + 0.U)
        ).asUInt

        val __tmp_579 = (SP + 140.U(16.W)).asUInt
        generalRegFiles(1.U) := Cat(
          arrayRegFiles(__tmp_579 + 0.U)
        ).asUInt

        CP := 112.U
      }

      is(112.U) {
        /*
        *(($0 + 12) + ($1 as SP)) = 57 [unsigned, U8, 1]  // $0($1) = 57
        goto .113
        */


        val __tmp_580 = generalRegFiles(0.U) + 12.U(16.W) + generalRegFiles(1.U).asSInt.asUInt
        val __tmp_581 = (57.U(8.W)).asUInt
        arrayRegFiles(__tmp_580 + 0.U) := __tmp_581(7, 0)

        CP := 113.U
      }

      is(113.U) {
        /*
        *(SP + 6) = 248 [signed, U32, 4]  // $sfLoc = 248
        goto .65
        */


        val __tmp_582 = SP + 6.U(16.W)
        val __tmp_583 = (248.S(32.W)).asUInt
        arrayRegFiles(__tmp_582 + 0.U) := __tmp_583(7, 0)
        arrayRegFiles(__tmp_582 + 1.U) := __tmp_583(15, 8)
        arrayRegFiles(__tmp_582 + 2.U) := __tmp_583(23, 16)
        arrayRegFiles(__tmp_582 + 3.U) := __tmp_583(31, 24)

        CP := 65.U
      }

      is(114.U) {
        /*
        *(SP + 6) = 268 [signed, U32, 4]  // $sfLoc = 268
        goto .115
        */


        val __tmp_584 = SP + 6.U(16.W)
        val __tmp_585 = (268.S(32.W)).asUInt
        arrayRegFiles(__tmp_584 + 0.U) := __tmp_585(7, 0)
        arrayRegFiles(__tmp_584 + 1.U) := __tmp_585(15, 8)
        arrayRegFiles(__tmp_584 + 2.U) := __tmp_585(23, 16)
        arrayRegFiles(__tmp_584 + 3.U) := __tmp_585(31, 24)

        CP := 115.U
      }

      is(115.U) {
        /*
        $0 = *(SP + 108) [unsigned, MS[anvil.PrinterIndex.I16, U8], 2]  // $0 = buff
        $1 = *(SP + 140) [signed, anvil.PrinterIndex.I16, 1]  // $1 = i
        goto .116
        */


        val __tmp_586 = (SP + 108.U(16.W)).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_586 + 1.U),
          arrayRegFiles(__tmp_586 + 0.U)
        ).asUInt

        val __tmp_587 = (SP + 140.U(16.W)).asUInt
        generalRegFiles(1.U) := Cat(
          arrayRegFiles(__tmp_587 + 0.U)
        ).asUInt

        CP := 116.U
      }

      is(116.U) {
        /*
        *(($0 + 12) + ($1 as SP)) = 65 [unsigned, U8, 1]  // $0($1) = 65
        goto .117
        */


        val __tmp_588 = generalRegFiles(0.U) + 12.U(16.W) + generalRegFiles(1.U).asSInt.asUInt
        val __tmp_589 = (65.U(8.W)).asUInt
        arrayRegFiles(__tmp_588 + 0.U) := __tmp_589(7, 0)

        CP := 117.U
      }

      is(117.U) {
        /*
        *(SP + 6) = 248 [signed, U32, 4]  // $sfLoc = 248
        goto .65
        */


        val __tmp_590 = SP + 6.U(16.W)
        val __tmp_591 = (248.S(32.W)).asUInt
        arrayRegFiles(__tmp_590 + 0.U) := __tmp_591(7, 0)
        arrayRegFiles(__tmp_590 + 1.U) := __tmp_591(15, 8)
        arrayRegFiles(__tmp_590 + 2.U) := __tmp_591(23, 16)
        arrayRegFiles(__tmp_590 + 3.U) := __tmp_591(31, 24)

        CP := 65.U
      }

      is(118.U) {
        /*
        *(SP + 6) = 269 [signed, U32, 4]  // $sfLoc = 269
        goto .119
        */


        val __tmp_592 = SP + 6.U(16.W)
        val __tmp_593 = (269.S(32.W)).asUInt
        arrayRegFiles(__tmp_592 + 0.U) := __tmp_593(7, 0)
        arrayRegFiles(__tmp_592 + 1.U) := __tmp_593(15, 8)
        arrayRegFiles(__tmp_592 + 2.U) := __tmp_593(23, 16)
        arrayRegFiles(__tmp_592 + 3.U) := __tmp_593(31, 24)

        CP := 119.U
      }

      is(119.U) {
        /*
        $0 = *(SP + 108) [unsigned, MS[anvil.PrinterIndex.I16, U8], 2]  // $0 = buff
        $1 = *(SP + 140) [signed, anvil.PrinterIndex.I16, 1]  // $1 = i
        goto .120
        */


        val __tmp_594 = (SP + 108.U(16.W)).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_594 + 1.U),
          arrayRegFiles(__tmp_594 + 0.U)
        ).asUInt

        val __tmp_595 = (SP + 140.U(16.W)).asUInt
        generalRegFiles(1.U) := Cat(
          arrayRegFiles(__tmp_595 + 0.U)
        ).asUInt

        CP := 120.U
      }

      is(120.U) {
        /*
        *(($0 + 12) + ($1 as SP)) = 66 [unsigned, U8, 1]  // $0($1) = 66
        goto .121
        */


        val __tmp_596 = generalRegFiles(0.U) + 12.U(16.W) + generalRegFiles(1.U).asSInt.asUInt
        val __tmp_597 = (66.U(8.W)).asUInt
        arrayRegFiles(__tmp_596 + 0.U) := __tmp_597(7, 0)

        CP := 121.U
      }

      is(121.U) {
        /*
        *(SP + 6) = 248 [signed, U32, 4]  // $sfLoc = 248
        goto .65
        */


        val __tmp_598 = SP + 6.U(16.W)
        val __tmp_599 = (248.S(32.W)).asUInt
        arrayRegFiles(__tmp_598 + 0.U) := __tmp_599(7, 0)
        arrayRegFiles(__tmp_598 + 1.U) := __tmp_599(15, 8)
        arrayRegFiles(__tmp_598 + 2.U) := __tmp_599(23, 16)
        arrayRegFiles(__tmp_598 + 3.U) := __tmp_599(31, 24)

        CP := 65.U
      }

      is(122.U) {
        /*
        *(SP + 6) = 270 [signed, U32, 4]  // $sfLoc = 270
        goto .123
        */


        val __tmp_600 = SP + 6.U(16.W)
        val __tmp_601 = (270.S(32.W)).asUInt
        arrayRegFiles(__tmp_600 + 0.U) := __tmp_601(7, 0)
        arrayRegFiles(__tmp_600 + 1.U) := __tmp_601(15, 8)
        arrayRegFiles(__tmp_600 + 2.U) := __tmp_601(23, 16)
        arrayRegFiles(__tmp_600 + 3.U) := __tmp_601(31, 24)

        CP := 123.U
      }

      is(123.U) {
        /*
        $0 = *(SP + 108) [unsigned, MS[anvil.PrinterIndex.I16, U8], 2]  // $0 = buff
        $1 = *(SP + 140) [signed, anvil.PrinterIndex.I16, 1]  // $1 = i
        goto .124
        */


        val __tmp_602 = (SP + 108.U(16.W)).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_602 + 1.U),
          arrayRegFiles(__tmp_602 + 0.U)
        ).asUInt

        val __tmp_603 = (SP + 140.U(16.W)).asUInt
        generalRegFiles(1.U) := Cat(
          arrayRegFiles(__tmp_603 + 0.U)
        ).asUInt

        CP := 124.U
      }

      is(124.U) {
        /*
        *(($0 + 12) + ($1 as SP)) = 67 [unsigned, U8, 1]  // $0($1) = 67
        goto .125
        */


        val __tmp_604 = generalRegFiles(0.U) + 12.U(16.W) + generalRegFiles(1.U).asSInt.asUInt
        val __tmp_605 = (67.U(8.W)).asUInt
        arrayRegFiles(__tmp_604 + 0.U) := __tmp_605(7, 0)

        CP := 125.U
      }

      is(125.U) {
        /*
        *(SP + 6) = 248 [signed, U32, 4]  // $sfLoc = 248
        goto .65
        */


        val __tmp_606 = SP + 6.U(16.W)
        val __tmp_607 = (248.S(32.W)).asUInt
        arrayRegFiles(__tmp_606 + 0.U) := __tmp_607(7, 0)
        arrayRegFiles(__tmp_606 + 1.U) := __tmp_607(15, 8)
        arrayRegFiles(__tmp_606 + 2.U) := __tmp_607(23, 16)
        arrayRegFiles(__tmp_606 + 3.U) := __tmp_607(31, 24)

        CP := 65.U
      }

      is(126.U) {
        /*
        *(SP + 6) = 271 [signed, U32, 4]  // $sfLoc = 271
        goto .127
        */


        val __tmp_608 = SP + 6.U(16.W)
        val __tmp_609 = (271.S(32.W)).asUInt
        arrayRegFiles(__tmp_608 + 0.U) := __tmp_609(7, 0)
        arrayRegFiles(__tmp_608 + 1.U) := __tmp_609(15, 8)
        arrayRegFiles(__tmp_608 + 2.U) := __tmp_609(23, 16)
        arrayRegFiles(__tmp_608 + 3.U) := __tmp_609(31, 24)

        CP := 127.U
      }

      is(127.U) {
        /*
        $0 = *(SP + 108) [unsigned, MS[anvil.PrinterIndex.I16, U8], 2]  // $0 = buff
        $1 = *(SP + 140) [signed, anvil.PrinterIndex.I16, 1]  // $1 = i
        goto .128
        */


        val __tmp_610 = (SP + 108.U(16.W)).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_610 + 1.U),
          arrayRegFiles(__tmp_610 + 0.U)
        ).asUInt

        val __tmp_611 = (SP + 140.U(16.W)).asUInt
        generalRegFiles(1.U) := Cat(
          arrayRegFiles(__tmp_611 + 0.U)
        ).asUInt

        CP := 128.U
      }

      is(128.U) {
        /*
        *(($0 + 12) + ($1 as SP)) = 68 [unsigned, U8, 1]  // $0($1) = 68
        goto .129
        */


        val __tmp_612 = generalRegFiles(0.U) + 12.U(16.W) + generalRegFiles(1.U).asSInt.asUInt
        val __tmp_613 = (68.U(8.W)).asUInt
        arrayRegFiles(__tmp_612 + 0.U) := __tmp_613(7, 0)

        CP := 129.U
      }

      is(129.U) {
        /*
        *(SP + 6) = 248 [signed, U32, 4]  // $sfLoc = 248
        goto .65
        */


        val __tmp_614 = SP + 6.U(16.W)
        val __tmp_615 = (248.S(32.W)).asUInt
        arrayRegFiles(__tmp_614 + 0.U) := __tmp_615(7, 0)
        arrayRegFiles(__tmp_614 + 1.U) := __tmp_615(15, 8)
        arrayRegFiles(__tmp_614 + 2.U) := __tmp_615(23, 16)
        arrayRegFiles(__tmp_614 + 3.U) := __tmp_615(31, 24)

        CP := 65.U
      }

      is(130.U) {
        /*
        *(SP + 6) = 272 [signed, U32, 4]  // $sfLoc = 272
        goto .131
        */


        val __tmp_616 = SP + 6.U(16.W)
        val __tmp_617 = (272.S(32.W)).asUInt
        arrayRegFiles(__tmp_616 + 0.U) := __tmp_617(7, 0)
        arrayRegFiles(__tmp_616 + 1.U) := __tmp_617(15, 8)
        arrayRegFiles(__tmp_616 + 2.U) := __tmp_617(23, 16)
        arrayRegFiles(__tmp_616 + 3.U) := __tmp_617(31, 24)

        CP := 131.U
      }

      is(131.U) {
        /*
        $0 = *(SP + 108) [unsigned, MS[anvil.PrinterIndex.I16, U8], 2]  // $0 = buff
        $1 = *(SP + 140) [signed, anvil.PrinterIndex.I16, 1]  // $1 = i
        goto .132
        */


        val __tmp_618 = (SP + 108.U(16.W)).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_618 + 1.U),
          arrayRegFiles(__tmp_618 + 0.U)
        ).asUInt

        val __tmp_619 = (SP + 140.U(16.W)).asUInt
        generalRegFiles(1.U) := Cat(
          arrayRegFiles(__tmp_619 + 0.U)
        ).asUInt

        CP := 132.U
      }

      is(132.U) {
        /*
        *(($0 + 12) + ($1 as SP)) = 69 [unsigned, U8, 1]  // $0($1) = 69
        goto .133
        */


        val __tmp_620 = generalRegFiles(0.U) + 12.U(16.W) + generalRegFiles(1.U).asSInt.asUInt
        val __tmp_621 = (69.U(8.W)).asUInt
        arrayRegFiles(__tmp_620 + 0.U) := __tmp_621(7, 0)

        CP := 133.U
      }

      is(133.U) {
        /*
        *(SP + 6) = 248 [signed, U32, 4]  // $sfLoc = 248
        goto .65
        */


        val __tmp_622 = SP + 6.U(16.W)
        val __tmp_623 = (248.S(32.W)).asUInt
        arrayRegFiles(__tmp_622 + 0.U) := __tmp_623(7, 0)
        arrayRegFiles(__tmp_622 + 1.U) := __tmp_623(15, 8)
        arrayRegFiles(__tmp_622 + 2.U) := __tmp_623(23, 16)
        arrayRegFiles(__tmp_622 + 3.U) := __tmp_623(31, 24)

        CP := 65.U
      }

      is(134.U) {
        /*
        *(SP + 6) = 273 [signed, U32, 4]  // $sfLoc = 273
        goto .135
        */


        val __tmp_624 = SP + 6.U(16.W)
        val __tmp_625 = (273.S(32.W)).asUInt
        arrayRegFiles(__tmp_624 + 0.U) := __tmp_625(7, 0)
        arrayRegFiles(__tmp_624 + 1.U) := __tmp_625(15, 8)
        arrayRegFiles(__tmp_624 + 2.U) := __tmp_625(23, 16)
        arrayRegFiles(__tmp_624 + 3.U) := __tmp_625(31, 24)

        CP := 135.U
      }

      is(135.U) {
        /*
        $0 = *(SP + 108) [unsigned, MS[anvil.PrinterIndex.I16, U8], 2]  // $0 = buff
        $1 = *(SP + 140) [signed, anvil.PrinterIndex.I16, 1]  // $1 = i
        goto .136
        */


        val __tmp_626 = (SP + 108.U(16.W)).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_626 + 1.U),
          arrayRegFiles(__tmp_626 + 0.U)
        ).asUInt

        val __tmp_627 = (SP + 140.U(16.W)).asUInt
        generalRegFiles(1.U) := Cat(
          arrayRegFiles(__tmp_627 + 0.U)
        ).asUInt

        CP := 136.U
      }

      is(136.U) {
        /*
        *(($0 + 12) + ($1 as SP)) = 70 [unsigned, U8, 1]  // $0($1) = 70
        goto .137
        */


        val __tmp_628 = generalRegFiles(0.U) + 12.U(16.W) + generalRegFiles(1.U).asSInt.asUInt
        val __tmp_629 = (70.U(8.W)).asUInt
        arrayRegFiles(__tmp_628 + 0.U) := __tmp_629(7, 0)

        CP := 137.U
      }

      is(137.U) {
        /*
        *(SP + 6) = 248 [signed, U32, 4]  // $sfLoc = 248
        goto .65
        */


        val __tmp_630 = SP + 6.U(16.W)
        val __tmp_631 = (248.S(32.W)).asUInt
        arrayRegFiles(__tmp_630 + 0.U) := __tmp_631(7, 0)
        arrayRegFiles(__tmp_630 + 1.U) := __tmp_631(15, 8)
        arrayRegFiles(__tmp_630 + 2.U) := __tmp_631(23, 16)
        arrayRegFiles(__tmp_630 + 3.U) := __tmp_631(31, 24)

        CP := 65.U
      }

      is(138.U) {
        /*
        *(SP + 6) = 279 [signed, U32, 4]  // $sfLoc = 279
        goto .139
        */


        val __tmp_632 = SP + 6.U(16.W)
        val __tmp_633 = (279.S(32.W)).asUInt
        arrayRegFiles(__tmp_632 + 0.U) := __tmp_633(7, 0)
        arrayRegFiles(__tmp_632 + 1.U) := __tmp_633(15, 8)
        arrayRegFiles(__tmp_632 + 2.U) := __tmp_633(23, 16)
        arrayRegFiles(__tmp_632 + 3.U) := __tmp_633(31, 24)

        CP := 139.U
      }

      is(139.U) {
        /*
        decl idx: anvil.PrinterIndex.U [@157, 8]
        $0 = *(SP + 76) [unsigned, anvil.PrinterIndex.U, 8]  // $0 = index
        goto .140
        */


        val __tmp_634 = (SP + 76.U(16.W)).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_634 + 7.U),
          arrayRegFiles(__tmp_634 + 6.U),
          arrayRegFiles(__tmp_634 + 5.U),
          arrayRegFiles(__tmp_634 + 4.U),
          arrayRegFiles(__tmp_634 + 3.U),
          arrayRegFiles(__tmp_634 + 2.U),
          arrayRegFiles(__tmp_634 + 1.U),
          arrayRegFiles(__tmp_634 + 0.U)
        ).asUInt

        CP := 140.U
      }

      is(140.U) {
        /*
        *(SP + 157) = $0 [signed, anvil.PrinterIndex.U, 8]  // idx = $0
        goto .141
        */


        val __tmp_635 = SP + 157.U(16.W)
        val __tmp_636 = (generalRegFiles(0.U)).asUInt
        arrayRegFiles(__tmp_635 + 0.U) := __tmp_636(7, 0)
        arrayRegFiles(__tmp_635 + 1.U) := __tmp_636(15, 8)
        arrayRegFiles(__tmp_635 + 2.U) := __tmp_636(23, 16)
        arrayRegFiles(__tmp_635 + 3.U) := __tmp_636(31, 24)
        arrayRegFiles(__tmp_635 + 4.U) := __tmp_636(39, 32)
        arrayRegFiles(__tmp_635 + 5.U) := __tmp_636(47, 40)
        arrayRegFiles(__tmp_635 + 6.U) := __tmp_636(55, 48)
        arrayRegFiles(__tmp_635 + 7.U) := __tmp_636(63, 56)

        CP := 141.U
      }

      is(141.U) {
        /*
        *(SP + 6) = 280 [signed, U32, 4]  // $sfLoc = 280
        goto .142
        */


        val __tmp_637 = SP + 6.U(16.W)
        val __tmp_638 = (280.S(32.W)).asUInt
        arrayRegFiles(__tmp_637 + 0.U) := __tmp_638(7, 0)
        arrayRegFiles(__tmp_637 + 1.U) := __tmp_638(15, 8)
        arrayRegFiles(__tmp_637 + 2.U) := __tmp_638(23, 16)
        arrayRegFiles(__tmp_637 + 3.U) := __tmp_638(31, 24)

        CP := 142.U
      }

      is(142.U) {
        /*
        *(SP + 6) = 280 [signed, U32, 4]  // $sfLoc = 280
        goto .143
        */


        val __tmp_639 = SP + 6.U(16.W)
        val __tmp_640 = (280.S(32.W)).asUInt
        arrayRegFiles(__tmp_639 + 0.U) := __tmp_640(7, 0)
        arrayRegFiles(__tmp_639 + 1.U) := __tmp_640(15, 8)
        arrayRegFiles(__tmp_639 + 2.U) := __tmp_640(23, 16)
        arrayRegFiles(__tmp_639 + 3.U) := __tmp_640(31, 24)

        CP := 143.U
      }

      is(143.U) {
        /*
        $0 = *(SP + 149) [signed, Z, 8]  // $0 = d
        goto .144
        */


        val __tmp_641 = (SP + 149.U(16.W)).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_641 + 7.U),
          arrayRegFiles(__tmp_641 + 6.U),
          arrayRegFiles(__tmp_641 + 5.U),
          arrayRegFiles(__tmp_641 + 4.U),
          arrayRegFiles(__tmp_641 + 3.U),
          arrayRegFiles(__tmp_641 + 2.U),
          arrayRegFiles(__tmp_641 + 1.U),
          arrayRegFiles(__tmp_641 + 0.U)
        ).asUInt

        CP := 144.U
      }

      is(144.U) {
        /*
        $1 = ($0 > 0)
        goto .145
        */


        generalRegFiles(1.U) := (generalRegFiles(0.U).asSInt > 0.S(64.W)).asUInt
        CP := 145.U
      }

      is(145.U) {
        /*
        if $1 goto .146 else goto .159
        */


        CP := Mux((generalRegFiles(1.U).asUInt) === 1.U, 146.U, 159.U)
      }

      is(146.U) {
        /*
        *(SP + 6) = 281 [signed, U32, 4]  // $sfLoc = 281
        goto .147
        */


        val __tmp_642 = SP + 6.U(16.W)
        val __tmp_643 = (281.S(32.W)).asUInt
        arrayRegFiles(__tmp_642 + 0.U) := __tmp_643(7, 0)
        arrayRegFiles(__tmp_642 + 1.U) := __tmp_643(15, 8)
        arrayRegFiles(__tmp_642 + 2.U) := __tmp_643(23, 16)
        arrayRegFiles(__tmp_642 + 3.U) := __tmp_643(31, 24)

        CP := 147.U
      }

      is(147.U) {
        /*
        $0 = *(SP + 74) [unsigned, MS[anvil.PrinterIndex.U, U8], 2]  // $0 = buffer
        $1 = *(SP + 157) [unsigned, anvil.PrinterIndex.U, 8]  // $1 = idx
        $2 = *(SP + 84) [unsigned, anvil.PrinterIndex.U, 8]  // $2 = mask
        goto .148
        */


        val __tmp_644 = (SP + 74.U(16.W)).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_644 + 1.U),
          arrayRegFiles(__tmp_644 + 0.U)
        ).asUInt

        val __tmp_645 = (SP + 157.U(16.W)).asUInt
        generalRegFiles(1.U) := Cat(
          arrayRegFiles(__tmp_645 + 7.U),
          arrayRegFiles(__tmp_645 + 6.U),
          arrayRegFiles(__tmp_645 + 5.U),
          arrayRegFiles(__tmp_645 + 4.U),
          arrayRegFiles(__tmp_645 + 3.U),
          arrayRegFiles(__tmp_645 + 2.U),
          arrayRegFiles(__tmp_645 + 1.U),
          arrayRegFiles(__tmp_645 + 0.U)
        ).asUInt

        val __tmp_646 = (SP + 84.U(16.W)).asUInt
        generalRegFiles(2.U) := Cat(
          arrayRegFiles(__tmp_646 + 7.U),
          arrayRegFiles(__tmp_646 + 6.U),
          arrayRegFiles(__tmp_646 + 5.U),
          arrayRegFiles(__tmp_646 + 4.U),
          arrayRegFiles(__tmp_646 + 3.U),
          arrayRegFiles(__tmp_646 + 2.U),
          arrayRegFiles(__tmp_646 + 1.U),
          arrayRegFiles(__tmp_646 + 0.U)
        ).asUInt

        CP := 148.U
      }

      is(148.U) {
        /*
        $3 = ($1 & $2)
        goto .149
        */


        generalRegFiles(3.U) := generalRegFiles(1.U) & generalRegFiles(2.U)
        CP := 149.U
      }

      is(149.U) {
        /*
        *(($0 + 12) + ($3 as SP)) = 48 [unsigned, U8, 1]  // $0($3) = 48
        goto .150
        */


        val __tmp_647 = generalRegFiles(0.U) + 12.U(16.W) + generalRegFiles(3.U).asUInt
        val __tmp_648 = (48.U(8.W)).asUInt
        arrayRegFiles(__tmp_647 + 0.U) := __tmp_648(7, 0)

        CP := 150.U
      }

      is(150.U) {
        /*
        *(SP + 6) = 282 [signed, U32, 4]  // $sfLoc = 282
        goto .151
        */


        val __tmp_649 = SP + 6.U(16.W)
        val __tmp_650 = (282.S(32.W)).asUInt
        arrayRegFiles(__tmp_649 + 0.U) := __tmp_650(7, 0)
        arrayRegFiles(__tmp_649 + 1.U) := __tmp_650(15, 8)
        arrayRegFiles(__tmp_649 + 2.U) := __tmp_650(23, 16)
        arrayRegFiles(__tmp_649 + 3.U) := __tmp_650(31, 24)

        CP := 151.U
      }

      is(151.U) {
        /*
        $0 = *(SP + 149) [signed, Z, 8]  // $0 = d
        goto .152
        */


        val __tmp_651 = (SP + 149.U(16.W)).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_651 + 7.U),
          arrayRegFiles(__tmp_651 + 6.U),
          arrayRegFiles(__tmp_651 + 5.U),
          arrayRegFiles(__tmp_651 + 4.U),
          arrayRegFiles(__tmp_651 + 3.U),
          arrayRegFiles(__tmp_651 + 2.U),
          arrayRegFiles(__tmp_651 + 1.U),
          arrayRegFiles(__tmp_651 + 0.U)
        ).asUInt

        CP := 152.U
      }

      is(152.U) {
        /*
        $1 = ($0 - 1)
        goto .153
        */


        generalRegFiles(1.U) := (generalRegFiles(0.U).asSInt - 1.S(64.W)).asUInt
        CP := 153.U
      }

      is(153.U) {
        /*
        *(SP + 149) = $1 [signed, Z, 8]  // d = $1
        goto .154
        */


        val __tmp_652 = SP + 149.U(16.W)
        val __tmp_653 = (generalRegFiles(1.U).asSInt).asUInt
        arrayRegFiles(__tmp_652 + 0.U) := __tmp_653(7, 0)
        arrayRegFiles(__tmp_652 + 1.U) := __tmp_653(15, 8)
        arrayRegFiles(__tmp_652 + 2.U) := __tmp_653(23, 16)
        arrayRegFiles(__tmp_652 + 3.U) := __tmp_653(31, 24)
        arrayRegFiles(__tmp_652 + 4.U) := __tmp_653(39, 32)
        arrayRegFiles(__tmp_652 + 5.U) := __tmp_653(47, 40)
        arrayRegFiles(__tmp_652 + 6.U) := __tmp_653(55, 48)
        arrayRegFiles(__tmp_652 + 7.U) := __tmp_653(63, 56)

        CP := 154.U
      }

      is(154.U) {
        /*
        *(SP + 6) = 283 [signed, U32, 4]  // $sfLoc = 283
        goto .155
        */


        val __tmp_654 = SP + 6.U(16.W)
        val __tmp_655 = (283.S(32.W)).asUInt
        arrayRegFiles(__tmp_654 + 0.U) := __tmp_655(7, 0)
        arrayRegFiles(__tmp_654 + 1.U) := __tmp_655(15, 8)
        arrayRegFiles(__tmp_654 + 2.U) := __tmp_655(23, 16)
        arrayRegFiles(__tmp_654 + 3.U) := __tmp_655(31, 24)

        CP := 155.U
      }

      is(155.U) {
        /*
        $0 = *(SP + 157) [unsigned, anvil.PrinterIndex.U, 8]  // $0 = idx
        goto .156
        */


        val __tmp_656 = (SP + 157.U(16.W)).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_656 + 7.U),
          arrayRegFiles(__tmp_656 + 6.U),
          arrayRegFiles(__tmp_656 + 5.U),
          arrayRegFiles(__tmp_656 + 4.U),
          arrayRegFiles(__tmp_656 + 3.U),
          arrayRegFiles(__tmp_656 + 2.U),
          arrayRegFiles(__tmp_656 + 1.U),
          arrayRegFiles(__tmp_656 + 0.U)
        ).asUInt

        CP := 156.U
      }

      is(156.U) {
        /*
        $1 = ($0 + 1)
        goto .157
        */


        generalRegFiles(1.U) := generalRegFiles(0.U) + 1.U(64.W)
        CP := 157.U
      }

      is(157.U) {
        /*
        *(SP + 157) = $1 [signed, anvil.PrinterIndex.U, 8]  // idx = $1
        goto .158
        */


        val __tmp_657 = SP + 157.U(16.W)
        val __tmp_658 = (generalRegFiles(1.U)).asUInt
        arrayRegFiles(__tmp_657 + 0.U) := __tmp_658(7, 0)
        arrayRegFiles(__tmp_657 + 1.U) := __tmp_658(15, 8)
        arrayRegFiles(__tmp_657 + 2.U) := __tmp_658(23, 16)
        arrayRegFiles(__tmp_657 + 3.U) := __tmp_658(31, 24)
        arrayRegFiles(__tmp_657 + 4.U) := __tmp_658(39, 32)
        arrayRegFiles(__tmp_657 + 5.U) := __tmp_658(47, 40)
        arrayRegFiles(__tmp_657 + 6.U) := __tmp_658(55, 48)
        arrayRegFiles(__tmp_657 + 7.U) := __tmp_658(63, 56)

        CP := 158.U
      }

      is(158.U) {
        /*
        *(SP + 6) = 280 [signed, U32, 4]  // $sfLoc = 280
        goto .142
        */


        val __tmp_659 = SP + 6.U(16.W)
        val __tmp_660 = (280.S(32.W)).asUInt
        arrayRegFiles(__tmp_659 + 0.U) := __tmp_660(7, 0)
        arrayRegFiles(__tmp_659 + 1.U) := __tmp_660(15, 8)
        arrayRegFiles(__tmp_659 + 2.U) := __tmp_660(23, 16)
        arrayRegFiles(__tmp_659 + 3.U) := __tmp_660(31, 24)

        CP := 142.U
      }

      is(159.U) {
        /*
        *(SP + 6) = 285 [signed, U32, 4]  // $sfLoc = 285
        goto .160
        */


        val __tmp_661 = SP + 6.U(16.W)
        val __tmp_662 = (285.S(32.W)).asUInt
        arrayRegFiles(__tmp_661 + 0.U) := __tmp_662(7, 0)
        arrayRegFiles(__tmp_661 + 1.U) := __tmp_662(15, 8)
        arrayRegFiles(__tmp_661 + 2.U) := __tmp_662(23, 16)
        arrayRegFiles(__tmp_661 + 3.U) := __tmp_662(31, 24)

        CP := 160.U
      }

      is(160.U) {
        /*
        decl j: anvil.PrinterIndex.I16 [@165, 1]
        $0 = *(SP + 140) [signed, anvil.PrinterIndex.I16, 1]  // $0 = i
        goto .161
        */


        val __tmp_663 = (SP + 140.U(16.W)).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_663 + 0.U)
        ).asUInt

        CP := 161.U
      }

      is(161.U) {
        /*
        $1 = ($0 - 1)
        goto .162
        */


        generalRegFiles(1.U) := (generalRegFiles(0.U).asSInt - 1.S(8.W)).asUInt
        CP := 162.U
      }

      is(162.U) {
        /*
        *(SP + 165) = $1 [signed, anvil.PrinterIndex.I16, 1]  // j = $1
        goto .163
        */


        val __tmp_664 = SP + 165.U(16.W)
        val __tmp_665 = (generalRegFiles(1.U).asSInt).asUInt
        arrayRegFiles(__tmp_664 + 0.U) := __tmp_665(7, 0)

        CP := 163.U
      }

      is(163.U) {
        /*
        *(SP + 6) = 286 [signed, U32, 4]  // $sfLoc = 286
        goto .164
        */


        val __tmp_666 = SP + 6.U(16.W)
        val __tmp_667 = (286.S(32.W)).asUInt
        arrayRegFiles(__tmp_666 + 0.U) := __tmp_667(7, 0)
        arrayRegFiles(__tmp_666 + 1.U) := __tmp_667(15, 8)
        arrayRegFiles(__tmp_666 + 2.U) := __tmp_667(23, 16)
        arrayRegFiles(__tmp_666 + 3.U) := __tmp_667(31, 24)

        CP := 164.U
      }

      is(164.U) {
        /*
        *(SP + 6) = 286 [signed, U32, 4]  // $sfLoc = 286
        goto .165
        */


        val __tmp_668 = SP + 6.U(16.W)
        val __tmp_669 = (286.S(32.W)).asUInt
        arrayRegFiles(__tmp_668 + 0.U) := __tmp_669(7, 0)
        arrayRegFiles(__tmp_668 + 1.U) := __tmp_669(15, 8)
        arrayRegFiles(__tmp_668 + 2.U) := __tmp_669(23, 16)
        arrayRegFiles(__tmp_668 + 3.U) := __tmp_669(31, 24)

        CP := 165.U
      }

      is(165.U) {
        /*
        $0 = *(SP + 165) [signed, anvil.PrinterIndex.I16, 1]  // $0 = j
        goto .166
        */


        val __tmp_670 = (SP + 165.U(16.W)).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_670 + 0.U)
        ).asUInt

        CP := 166.U
      }

      is(166.U) {
        /*
        $1 = ($0 >= 0)
        goto .167
        */


        generalRegFiles(1.U) := (generalRegFiles(0.U).asSInt >= 0.S(8.W)).asUInt
        CP := 167.U
      }

      is(167.U) {
        /*
        if $1 goto .168 else goto .183
        */


        CP := Mux((generalRegFiles(1.U).asUInt) === 1.U, 168.U, 183.U)
      }

      is(168.U) {
        /*
        *(SP + 6) = 287 [signed, U32, 4]  // $sfLoc = 287
        goto .169
        */


        val __tmp_671 = SP + 6.U(16.W)
        val __tmp_672 = (287.S(32.W)).asUInt
        arrayRegFiles(__tmp_671 + 0.U) := __tmp_672(7, 0)
        arrayRegFiles(__tmp_671 + 1.U) := __tmp_672(15, 8)
        arrayRegFiles(__tmp_671 + 2.U) := __tmp_672(23, 16)
        arrayRegFiles(__tmp_671 + 3.U) := __tmp_672(31, 24)

        CP := 169.U
      }

      is(169.U) {
        /*
        $0 = *(SP + 74) [unsigned, MS[anvil.PrinterIndex.U, U8], 2]  // $0 = buffer
        $1 = *(SP + 157) [unsigned, anvil.PrinterIndex.U, 8]  // $1 = idx
        $2 = *(SP + 84) [unsigned, anvil.PrinterIndex.U, 8]  // $2 = mask
        goto .170
        */


        val __tmp_673 = (SP + 74.U(16.W)).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_673 + 1.U),
          arrayRegFiles(__tmp_673 + 0.U)
        ).asUInt

        val __tmp_674 = (SP + 157.U(16.W)).asUInt
        generalRegFiles(1.U) := Cat(
          arrayRegFiles(__tmp_674 + 7.U),
          arrayRegFiles(__tmp_674 + 6.U),
          arrayRegFiles(__tmp_674 + 5.U),
          arrayRegFiles(__tmp_674 + 4.U),
          arrayRegFiles(__tmp_674 + 3.U),
          arrayRegFiles(__tmp_674 + 2.U),
          arrayRegFiles(__tmp_674 + 1.U),
          arrayRegFiles(__tmp_674 + 0.U)
        ).asUInt

        val __tmp_675 = (SP + 84.U(16.W)).asUInt
        generalRegFiles(2.U) := Cat(
          arrayRegFiles(__tmp_675 + 7.U),
          arrayRegFiles(__tmp_675 + 6.U),
          arrayRegFiles(__tmp_675 + 5.U),
          arrayRegFiles(__tmp_675 + 4.U),
          arrayRegFiles(__tmp_675 + 3.U),
          arrayRegFiles(__tmp_675 + 2.U),
          arrayRegFiles(__tmp_675 + 1.U),
          arrayRegFiles(__tmp_675 + 0.U)
        ).asUInt

        CP := 170.U
      }

      is(170.U) {
        /*
        $3 = ($1 & $2)
        goto .171
        */


        generalRegFiles(3.U) := generalRegFiles(1.U) & generalRegFiles(2.U)
        CP := 171.U
      }

      is(171.U) {
        /*
        $4 = *(SP + 108) [unsigned, MS[anvil.PrinterIndex.I16, U8], 2]  // $4 = buff
        $5 = *(SP + 165) [signed, anvil.PrinterIndex.I16, 1]  // $5 = j
        goto .172
        */


        val __tmp_676 = (SP + 108.U(16.W)).asUInt
        generalRegFiles(4.U) := Cat(
          arrayRegFiles(__tmp_676 + 1.U),
          arrayRegFiles(__tmp_676 + 0.U)
        ).asUInt

        val __tmp_677 = (SP + 165.U(16.W)).asUInt
        generalRegFiles(5.U) := Cat(
          arrayRegFiles(__tmp_677 + 0.U)
        ).asUInt

        CP := 172.U
      }

      is(172.U) {
        /*
        $6 = *(($4 + 12) + ($5 as SP)) [unsigned, U8, 1]  // $6 = $4($5)
        goto .173
        */


        val __tmp_678 = (generalRegFiles(4.U) + 12.U(16.W) + generalRegFiles(5.U).asSInt.asUInt).asUInt
        generalRegFiles(6.U) := Cat(
          arrayRegFiles(__tmp_678 + 0.U)
        ).asUInt

        CP := 173.U
      }

      is(173.U) {
        /*
        *(($0 + 12) + ($3 as SP)) = $6 [unsigned, U8, 1]  // $0($3) = $6
        goto .174
        */


        val __tmp_679 = generalRegFiles(0.U) + 12.U(16.W) + generalRegFiles(3.U).asUInt
        val __tmp_680 = (generalRegFiles(6.U)).asUInt
        arrayRegFiles(__tmp_679 + 0.U) := __tmp_680(7, 0)

        CP := 174.U
      }

      is(174.U) {
        /*
        *(SP + 6) = 288 [signed, U32, 4]  // $sfLoc = 288
        goto .175
        */


        val __tmp_681 = SP + 6.U(16.W)
        val __tmp_682 = (288.S(32.W)).asUInt
        arrayRegFiles(__tmp_681 + 0.U) := __tmp_682(7, 0)
        arrayRegFiles(__tmp_681 + 1.U) := __tmp_682(15, 8)
        arrayRegFiles(__tmp_681 + 2.U) := __tmp_682(23, 16)
        arrayRegFiles(__tmp_681 + 3.U) := __tmp_682(31, 24)

        CP := 175.U
      }

      is(175.U) {
        /*
        $0 = *(SP + 165) [signed, anvil.PrinterIndex.I16, 1]  // $0 = j
        goto .176
        */


        val __tmp_683 = (SP + 165.U(16.W)).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_683 + 0.U)
        ).asUInt

        CP := 176.U
      }

      is(176.U) {
        /*
        $1 = ($0 - 1)
        goto .177
        */


        generalRegFiles(1.U) := (generalRegFiles(0.U).asSInt - 1.S(8.W)).asUInt
        CP := 177.U
      }

      is(177.U) {
        /*
        *(SP + 165) = $1 [signed, anvil.PrinterIndex.I16, 1]  // j = $1
        goto .178
        */


        val __tmp_684 = SP + 165.U(16.W)
        val __tmp_685 = (generalRegFiles(1.U).asSInt).asUInt
        arrayRegFiles(__tmp_684 + 0.U) := __tmp_685(7, 0)

        CP := 178.U
      }

      is(178.U) {
        /*
        *(SP + 6) = 289 [signed, U32, 4]  // $sfLoc = 289
        goto .179
        */


        val __tmp_686 = SP + 6.U(16.W)
        val __tmp_687 = (289.S(32.W)).asUInt
        arrayRegFiles(__tmp_686 + 0.U) := __tmp_687(7, 0)
        arrayRegFiles(__tmp_686 + 1.U) := __tmp_687(15, 8)
        arrayRegFiles(__tmp_686 + 2.U) := __tmp_687(23, 16)
        arrayRegFiles(__tmp_686 + 3.U) := __tmp_687(31, 24)

        CP := 179.U
      }

      is(179.U) {
        /*
        $0 = *(SP + 157) [unsigned, anvil.PrinterIndex.U, 8]  // $0 = idx
        goto .180
        */


        val __tmp_688 = (SP + 157.U(16.W)).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_688 + 7.U),
          arrayRegFiles(__tmp_688 + 6.U),
          arrayRegFiles(__tmp_688 + 5.U),
          arrayRegFiles(__tmp_688 + 4.U),
          arrayRegFiles(__tmp_688 + 3.U),
          arrayRegFiles(__tmp_688 + 2.U),
          arrayRegFiles(__tmp_688 + 1.U),
          arrayRegFiles(__tmp_688 + 0.U)
        ).asUInt

        CP := 180.U
      }

      is(180.U) {
        /*
        $1 = ($0 + 1)
        goto .181
        */


        generalRegFiles(1.U) := generalRegFiles(0.U) + 1.U(64.W)
        CP := 181.U
      }

      is(181.U) {
        /*
        *(SP + 157) = $1 [signed, anvil.PrinterIndex.U, 8]  // idx = $1
        goto .182
        */


        val __tmp_689 = SP + 157.U(16.W)
        val __tmp_690 = (generalRegFiles(1.U)).asUInt
        arrayRegFiles(__tmp_689 + 0.U) := __tmp_690(7, 0)
        arrayRegFiles(__tmp_689 + 1.U) := __tmp_690(15, 8)
        arrayRegFiles(__tmp_689 + 2.U) := __tmp_690(23, 16)
        arrayRegFiles(__tmp_689 + 3.U) := __tmp_690(31, 24)
        arrayRegFiles(__tmp_689 + 4.U) := __tmp_690(39, 32)
        arrayRegFiles(__tmp_689 + 5.U) := __tmp_690(47, 40)
        arrayRegFiles(__tmp_689 + 6.U) := __tmp_690(55, 48)
        arrayRegFiles(__tmp_689 + 7.U) := __tmp_690(63, 56)

        CP := 182.U
      }

      is(182.U) {
        /*
        *(SP + 6) = 286 [signed, U32, 4]  // $sfLoc = 286
        goto .164
        */


        val __tmp_691 = SP + 6.U(16.W)
        val __tmp_692 = (286.S(32.W)).asUInt
        arrayRegFiles(__tmp_691 + 0.U) := __tmp_692(7, 0)
        arrayRegFiles(__tmp_691 + 1.U) := __tmp_692(15, 8)
        arrayRegFiles(__tmp_691 + 2.U) := __tmp_692(23, 16)
        arrayRegFiles(__tmp_691 + 3.U) := __tmp_692(31, 24)

        CP := 164.U
      }

      is(183.U) {
        /*
        *(SP + 6) = 291 [signed, U32, 4]  // $sfLoc = 291
        goto .184
        */


        val __tmp_693 = SP + 6.U(16.W)
        val __tmp_694 = (291.S(32.W)).asUInt
        arrayRegFiles(__tmp_693 + 0.U) := __tmp_694(7, 0)
        arrayRegFiles(__tmp_693 + 1.U) := __tmp_694(15, 8)
        arrayRegFiles(__tmp_693 + 2.U) := __tmp_694(23, 16)
        arrayRegFiles(__tmp_693 + 3.U) := __tmp_694(31, 24)

        CP := 184.U
      }

      is(184.U) {
        /*
        $0 = *(SP + 100) [signed, Z, 8]  // $0 = digits
        goto .185
        */


        val __tmp_695 = (SP + 100.U(16.W)).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_695 + 7.U),
          arrayRegFiles(__tmp_695 + 6.U),
          arrayRegFiles(__tmp_695 + 5.U),
          arrayRegFiles(__tmp_695 + 4.U),
          arrayRegFiles(__tmp_695 + 3.U),
          arrayRegFiles(__tmp_695 + 2.U),
          arrayRegFiles(__tmp_695 + 1.U),
          arrayRegFiles(__tmp_695 + 0.U)
        ).asUInt

        CP := 185.U
      }

      is(185.U) {
        /*
        $1 = ($0 as U64)
        goto .186
        */


        generalRegFiles(1.U) := generalRegFiles(0.U).asSInt.asUInt
        CP := 186.U
      }

      is(186.U) {
        /*
        *(SP + 6) = 248 [signed, U32, 4]  // $sfLoc = 248
        goto .187
        */


        val __tmp_696 = SP + 6.U(16.W)
        val __tmp_697 = (248.S(32.W)).asUInt
        arrayRegFiles(__tmp_696 + 0.U) := __tmp_697(7, 0)
        arrayRegFiles(__tmp_696 + 1.U) := __tmp_697(15, 8)
        arrayRegFiles(__tmp_696 + 2.U) := __tmp_697(23, 16)
        arrayRegFiles(__tmp_696 + 3.U) := __tmp_697(31, 24)

        CP := 187.U
      }

      is(187.U) {
        /*
        **(SP + 2) = $1 [unsigned, U64, 8]  // $res = $1
        goto $ret@0
        */


        val __tmp_698 = Cat(
          arrayRegFiles(SP + 2.U(16.W) + 1.U),
          arrayRegFiles(SP + 2.U(16.W) + 0.U)
        )
        val __tmp_699 = (generalRegFiles(1.U)).asUInt
        arrayRegFiles(__tmp_698 + 0.U) := __tmp_699(7, 0)
        arrayRegFiles(__tmp_698 + 1.U) := __tmp_699(15, 8)
        arrayRegFiles(__tmp_698 + 2.U) := __tmp_699(23, 16)
        arrayRegFiles(__tmp_698 + 3.U) := __tmp_699(31, 24)
        arrayRegFiles(__tmp_698 + 4.U) := __tmp_699(39, 32)
        arrayRegFiles(__tmp_698 + 5.U) := __tmp_699(47, 40)
        arrayRegFiles(__tmp_698 + 6.U) := __tmp_699(55, 48)
        arrayRegFiles(__tmp_698 + 7.U) := __tmp_699(63, 56)

        CP := Cat(
          arrayRegFiles(SP + 0.U + 1.U),
          arrayRegFiles(SP + 0.U + 0.U)
        )

      }

      is(188.U) {
        /*
        $0 = *(SP - 8) [unsigned, U64, 8]  // restore $0
        $1 = **(SP + 2) [unsigned, U64, 8]  // $1 = $ret
        undecl digits: Z [@100, 8], n: U64 [@92, 8], mask: anvil.PrinterIndex.U [@84, 8], index: anvil.PrinterIndex.U [@76, 8], buffer: MS[anvil.PrinterIndex.U, U8] [@74, 2], $sfLoc: U32 [@6, 4], $sfDesc: IS[52, U8] [@10, 64], $sfCaller: SP [@4, 2], $res: SP [@2, 2], $ret: CP [@0, 2]
        goto .189
        */


        val __tmp_700 = (SP - 8.U(16.W)).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_700 + 7.U),
          arrayRegFiles(__tmp_700 + 6.U),
          arrayRegFiles(__tmp_700 + 5.U),
          arrayRegFiles(__tmp_700 + 4.U),
          arrayRegFiles(__tmp_700 + 3.U),
          arrayRegFiles(__tmp_700 + 2.U),
          arrayRegFiles(__tmp_700 + 1.U),
          arrayRegFiles(__tmp_700 + 0.U)
        ).asUInt

        val __tmp_701 = (Cat(
          arrayRegFiles(SP + 2.U(16.W) + 1.U),
          arrayRegFiles(SP + 2.U(16.W) + 0.U)
        )).asUInt
        generalRegFiles(1.U) := Cat(
          arrayRegFiles(__tmp_701 + 7.U),
          arrayRegFiles(__tmp_701 + 6.U),
          arrayRegFiles(__tmp_701 + 5.U),
          arrayRegFiles(__tmp_701 + 4.U),
          arrayRegFiles(__tmp_701 + 3.U),
          arrayRegFiles(__tmp_701 + 2.U),
          arrayRegFiles(__tmp_701 + 1.U),
          arrayRegFiles(__tmp_701 + 0.U)
        ).asUInt

        CP := 189.U
      }

      is(189.U) {
        /*
        SP = SP - 60
        goto .190
        */


        SP := SP - 60.U

        CP := 190.U
      }

      is(190.U) {
        /*
        unalloc add$res@[10,11].CE055965: U16 [@42, 2]
        goto .191
        */


        CP := 191.U
      }

      is(191.U) {
        /*
        DP = DP + ($1 as DP)
        goto .192
        */


        DP := generalRegFiles(1.U).asUInt

        CP := 192.U
      }

      is(192.U) {
        /*
        unalloc printU64Hex$res@[10,11].CE055965: U64 [@44, 8]
        goto .193
        */


        CP := 193.U
      }

      is(193.U) {
        /*
        *((*20 + 12) + ((DP & 127) as SP)) = 10 [unsigned, U8, 1]  // $display((DP & 127)) = 10
        goto .194
        */


        val __tmp_702 = Cat(
          arrayRegFiles(20.U(16.W) + 1.U),
          arrayRegFiles(20.U(16.W) + 0.U)
        ) + 12.U(16.W) + DP & 127.U(64.W).asUInt
        val __tmp_703 = (10.U(8.W)).asUInt
        arrayRegFiles(__tmp_702 + 0.U) := __tmp_703(7, 0)

        CP := 194.U
      }

      is(194.U) {
        /*
        DP = DP + 1
        goto .195
        */


        DP := DP + 1.U

        CP := 195.U
      }

      is(195.U) {
        /*
        *(SP + 4) = 9 [signed, U32, 4]  // $sfLoc = 9
        goto $ret@0
        */


        val __tmp_704 = SP + 4.U(16.W)
        val __tmp_705 = (9.S(32.W)).asUInt
        arrayRegFiles(__tmp_704 + 0.U) := __tmp_705(7, 0)
        arrayRegFiles(__tmp_704 + 1.U) := __tmp_705(15, 8)
        arrayRegFiles(__tmp_704 + 2.U) := __tmp_705(23, 16)
        arrayRegFiles(__tmp_704 + 3.U) := __tmp_705(31, 24)

        CP := Cat(
          arrayRegFiles(SP + 0.U + 1.U),
          arrayRegFiles(SP + 0.U + 0.U)
        )

      }

      is(196.U) {
        /*
        $0 = **(SP + 2) [unsigned, U16, 2]  // $0 = $ret
        undecl y: U16 [@43, 2], x: U16 [@41, 2], $sfLoc: U32 [@8, 4], $sfDesc: IS[17, U8] [@12, 29], $sfCaller: SP [@6, 2], $res: SP [@2, 4], $ret: CP [@0, 2]
        goto .197
        */


        val __tmp_706 = (Cat(
          arrayRegFiles(SP + 2.U(16.W) + 1.U),
          arrayRegFiles(SP + 2.U(16.W) + 0.U)
        )).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_706 + 1.U),
          arrayRegFiles(__tmp_706 + 0.U)
        ).asUInt

        CP := 197.U
      }

      is(197.U) {
        /*
        SP = SP - 52
        goto .198
        */


        SP := SP - 52.U

        CP := 198.U
      }

      is(198.U) {
        /*
        alloc printU64Hex$res@[14,11].A314137A: U64 [@44, 8]
        goto .199
        */


        CP := 199.U
      }

      is(199.U) {
        /*
        SP = SP + 60
        goto .200
        */


        SP := SP + 60.U

        CP := 200.U
      }

      is(200.U) {
        /*
        decl $ret: CP [@0, 2], $res: SP [@2, 2], $sfCaller: SP [@4, 2], $sfDesc: IS[52, U8] [@10, 64], $sfLoc: U32 [@6, 4], buffer: MS[anvil.PrinterIndex.U, U8] [@74, 2], index: anvil.PrinterIndex.U [@76, 8], mask: anvil.PrinterIndex.U [@84, 8], n: U64 [@92, 8], digits: Z [@100, 8]
        *SP = 201 [unsigned, CP, 2]  // $ret@0 = 2292
        *(SP + 2) = (SP - 16) [unsigned, SP, 2]  // $res@2 = -16
        *(SP + 4) = (SP - 58) [unsigned, SP, 2]  // $sfCaller@4 = -58
        *(SP + 74) = *20 [unsigned, SP, 2]  // buffer = *20
        *(SP + 76) = DP [unsigned, anvil.PrinterIndex.U, 8]  // index = DP
        *(SP + 84) = 127 [unsigned, anvil.PrinterIndex.U, 8]  // mask = 127
        *(SP + 92) = ($0 as U64) [unsigned, U64, 8]  // n = ($0 as U64)
        *(SP + 100) = 4 [signed, Z, 8]  // digits = 4
        *(SP - 8) = $0 [unsigned, U64, 8]  // save $0
        goto .38
        */


        val __tmp_707 = SP
        val __tmp_708 = (201.U(16.W)).asUInt
        arrayRegFiles(__tmp_707 + 0.U) := __tmp_708(7, 0)
        arrayRegFiles(__tmp_707 + 1.U) := __tmp_708(15, 8)

        val __tmp_709 = SP + 2.U(16.W)
        val __tmp_710 = (SP - 16.U(16.W)).asUInt
        arrayRegFiles(__tmp_709 + 0.U) := __tmp_710(7, 0)
        arrayRegFiles(__tmp_709 + 1.U) := __tmp_710(15, 8)

        val __tmp_711 = SP + 4.U(16.W)
        val __tmp_712 = (SP - 58.U(16.W)).asUInt
        arrayRegFiles(__tmp_711 + 0.U) := __tmp_712(7, 0)
        arrayRegFiles(__tmp_711 + 1.U) := __tmp_712(15, 8)

        val __tmp_713 = SP + 74.U(16.W)
        val __tmp_714 = (Cat(
          arrayRegFiles(20.U(16.W) + 1.U),
          arrayRegFiles(20.U(16.W) + 0.U)
        )).asUInt
        arrayRegFiles(__tmp_713 + 0.U) := __tmp_714(7, 0)
        arrayRegFiles(__tmp_713 + 1.U) := __tmp_714(15, 8)

        val __tmp_715 = SP + 76.U(16.W)
        val __tmp_716 = (DP).asUInt
        arrayRegFiles(__tmp_715 + 0.U) := __tmp_716(7, 0)
        arrayRegFiles(__tmp_715 + 1.U) := __tmp_716(15, 8)
        arrayRegFiles(__tmp_715 + 2.U) := __tmp_716(23, 16)
        arrayRegFiles(__tmp_715 + 3.U) := __tmp_716(31, 24)
        arrayRegFiles(__tmp_715 + 4.U) := __tmp_716(39, 32)
        arrayRegFiles(__tmp_715 + 5.U) := __tmp_716(47, 40)
        arrayRegFiles(__tmp_715 + 6.U) := __tmp_716(55, 48)
        arrayRegFiles(__tmp_715 + 7.U) := __tmp_716(63, 56)

        val __tmp_717 = SP + 84.U(16.W)
        val __tmp_718 = (127.U(64.W)).asUInt
        arrayRegFiles(__tmp_717 + 0.U) := __tmp_718(7, 0)
        arrayRegFiles(__tmp_717 + 1.U) := __tmp_718(15, 8)
        arrayRegFiles(__tmp_717 + 2.U) := __tmp_718(23, 16)
        arrayRegFiles(__tmp_717 + 3.U) := __tmp_718(31, 24)
        arrayRegFiles(__tmp_717 + 4.U) := __tmp_718(39, 32)
        arrayRegFiles(__tmp_717 + 5.U) := __tmp_718(47, 40)
        arrayRegFiles(__tmp_717 + 6.U) := __tmp_718(55, 48)
        arrayRegFiles(__tmp_717 + 7.U) := __tmp_718(63, 56)

        val __tmp_719 = SP + 92.U(16.W)
        val __tmp_720 = (generalRegFiles(0.U).asUInt).asUInt
        arrayRegFiles(__tmp_719 + 0.U) := __tmp_720(7, 0)
        arrayRegFiles(__tmp_719 + 1.U) := __tmp_720(15, 8)
        arrayRegFiles(__tmp_719 + 2.U) := __tmp_720(23, 16)
        arrayRegFiles(__tmp_719 + 3.U) := __tmp_720(31, 24)
        arrayRegFiles(__tmp_719 + 4.U) := __tmp_720(39, 32)
        arrayRegFiles(__tmp_719 + 5.U) := __tmp_720(47, 40)
        arrayRegFiles(__tmp_719 + 6.U) := __tmp_720(55, 48)
        arrayRegFiles(__tmp_719 + 7.U) := __tmp_720(63, 56)

        val __tmp_721 = SP + 100.U(16.W)
        val __tmp_722 = (4.S(64.W)).asUInt
        arrayRegFiles(__tmp_721 + 0.U) := __tmp_722(7, 0)
        arrayRegFiles(__tmp_721 + 1.U) := __tmp_722(15, 8)
        arrayRegFiles(__tmp_721 + 2.U) := __tmp_722(23, 16)
        arrayRegFiles(__tmp_721 + 3.U) := __tmp_722(31, 24)
        arrayRegFiles(__tmp_721 + 4.U) := __tmp_722(39, 32)
        arrayRegFiles(__tmp_721 + 5.U) := __tmp_722(47, 40)
        arrayRegFiles(__tmp_721 + 6.U) := __tmp_722(55, 48)
        arrayRegFiles(__tmp_721 + 7.U) := __tmp_722(63, 56)

        val __tmp_723 = SP - 8.U(16.W)
        val __tmp_724 = (generalRegFiles(0.U)).asUInt
        arrayRegFiles(__tmp_723 + 0.U) := __tmp_724(7, 0)
        arrayRegFiles(__tmp_723 + 1.U) := __tmp_724(15, 8)
        arrayRegFiles(__tmp_723 + 2.U) := __tmp_724(23, 16)
        arrayRegFiles(__tmp_723 + 3.U) := __tmp_724(31, 24)
        arrayRegFiles(__tmp_723 + 4.U) := __tmp_724(39, 32)
        arrayRegFiles(__tmp_723 + 5.U) := __tmp_724(47, 40)
        arrayRegFiles(__tmp_723 + 6.U) := __tmp_724(55, 48)
        arrayRegFiles(__tmp_723 + 7.U) := __tmp_724(63, 56)

        CP := 38.U
      }

      is(201.U) {
        /*
        $0 = *(SP - 8) [unsigned, U64, 8]  // restore $0
        $1 = **(SP + 2) [unsigned, U64, 8]  // $1 = $ret
        undecl digits: Z [@100, 8], n: U64 [@92, 8], mask: anvil.PrinterIndex.U [@84, 8], index: anvil.PrinterIndex.U [@76, 8], buffer: MS[anvil.PrinterIndex.U, U8] [@74, 2], $sfLoc: U32 [@6, 4], $sfDesc: IS[52, U8] [@10, 64], $sfCaller: SP [@4, 2], $res: SP [@2, 2], $ret: CP [@0, 2]
        goto .202
        */


        val __tmp_725 = (SP - 8.U(16.W)).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_725 + 7.U),
          arrayRegFiles(__tmp_725 + 6.U),
          arrayRegFiles(__tmp_725 + 5.U),
          arrayRegFiles(__tmp_725 + 4.U),
          arrayRegFiles(__tmp_725 + 3.U),
          arrayRegFiles(__tmp_725 + 2.U),
          arrayRegFiles(__tmp_725 + 1.U),
          arrayRegFiles(__tmp_725 + 0.U)
        ).asUInt

        val __tmp_726 = (Cat(
          arrayRegFiles(SP + 2.U(16.W) + 1.U),
          arrayRegFiles(SP + 2.U(16.W) + 0.U)
        )).asUInt
        generalRegFiles(1.U) := Cat(
          arrayRegFiles(__tmp_726 + 7.U),
          arrayRegFiles(__tmp_726 + 6.U),
          arrayRegFiles(__tmp_726 + 5.U),
          arrayRegFiles(__tmp_726 + 4.U),
          arrayRegFiles(__tmp_726 + 3.U),
          arrayRegFiles(__tmp_726 + 2.U),
          arrayRegFiles(__tmp_726 + 1.U),
          arrayRegFiles(__tmp_726 + 0.U)
        ).asUInt

        CP := 202.U
      }

      is(202.U) {
        /*
        SP = SP - 60
        goto .203
        */


        SP := SP - 60.U

        CP := 203.U
      }

      is(203.U) {
        /*
        unalloc add$res@[14,11].A314137A: U16 [@42, 2]
        goto .204
        */


        CP := 204.U
      }

      is(204.U) {
        /*
        DP = DP + ($1 as DP)
        goto .205
        */


        DP := generalRegFiles(1.U).asUInt

        CP := 205.U
      }

      is(205.U) {
        /*
        unalloc printU64Hex$res@[14,11].A314137A: U64 [@44, 8]
        goto .206
        */


        CP := 206.U
      }

      is(206.U) {
        /*
        *((*20 + 12) + ((DP & 127) as SP)) = 10 [unsigned, U8, 1]  // $display((DP & 127)) = 10
        goto .207
        */


        val __tmp_727 = Cat(
          arrayRegFiles(20.U(16.W) + 1.U),
          arrayRegFiles(20.U(16.W) + 0.U)
        ) + 12.U(16.W) + DP & 127.U(64.W).asUInt
        val __tmp_728 = (10.U(8.W)).asUInt
        arrayRegFiles(__tmp_727 + 0.U) := __tmp_728(7, 0)

        CP := 207.U
      }

      is(207.U) {
        /*
        DP = DP + 1
        goto .208
        */


        DP := DP + 1.U

        CP := 208.U
      }

      is(208.U) {
        /*
        *(SP + 4) = 13 [signed, U32, 4]  // $sfLoc = 13
        goto $ret@0
        */


        val __tmp_729 = SP + 4.U(16.W)
        val __tmp_730 = (13.S(32.W)).asUInt
        arrayRegFiles(__tmp_729 + 0.U) := __tmp_730(7, 0)
        arrayRegFiles(__tmp_729 + 1.U) := __tmp_730(15, 8)
        arrayRegFiles(__tmp_729 + 2.U) := __tmp_730(23, 16)
        arrayRegFiles(__tmp_729 + 3.U) := __tmp_730(31, 24)

        CP := Cat(
          arrayRegFiles(SP + 0.U + 1.U),
          arrayRegFiles(SP + 0.U + 0.U)
        )

      }

    }

}
