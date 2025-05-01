package GeneralRegFileToBRAM
import chisel3._
import chisel3.util._
import chisel3.experimental._



class AddTest (val C_S_AXI_DATA_WIDTH:  Int = 32,
               val C_S_AXI_ADDR_WIDTH:  Int = 32,
               val ARRAY_REG_WIDTH:     Int = 8,
               val ARRAY_REG_DEPTH:     Int = 128,
               val GENERAL_REG_WIDTH:   Int = 64,
               val GENERAL_REG_DEPTH:   Int = 81,
               val STACK_POINTER_WIDTH: Int = 8,
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
    val arrayRegFiles = Reg(Vec(ARRAY_REG_DEPTH, UInt(ARRAY_REG_WIDTH.W)))
    // reg for general purpose
    val generalRegFiles = Reg(Vec(GENERAL_REG_DEPTH, UInt(GENERAL_REG_WIDTH.W)))
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
        SP = 36
        DP = 0
        *(9: U32) = (886747591: U32) [unsigned, U32, 4]  // $display.$type (MS[anvil.PrinterIndex.U, U8]: 0x34DAB1C7)
        *(13: SP) = (16: Z) [signed, Z, 8]  // $display.size
        *(36: CP) = (0: CP) [unsigned, CP, 2]  // $ret
        goto .4
        */


        SP := 36.U(8.W)

        DP := 0.U(64.W)

        val __tmp_0 = 9.U(32.W)
        val __tmp_1 = (886747591.U(32.W)).asUInt
        arrayRegFiles(__tmp_0 + 0.U) := __tmp_1(7, 0)
        arrayRegFiles(__tmp_0 + 1.U) := __tmp_1(15, 8)
        arrayRegFiles(__tmp_0 + 2.U) := __tmp_1(23, 16)
        arrayRegFiles(__tmp_0 + 3.U) := __tmp_1(31, 24)

        val __tmp_2 = 13.U(8.W)
        val __tmp_3 = (16.S(64.W)).asUInt
        arrayRegFiles(__tmp_2 + 0.U) := __tmp_3(7, 0)
        arrayRegFiles(__tmp_2 + 1.U) := __tmp_3(15, 8)
        arrayRegFiles(__tmp_2 + 2.U) := __tmp_3(23, 16)
        arrayRegFiles(__tmp_2 + 3.U) := __tmp_3(31, 24)
        arrayRegFiles(__tmp_2 + 4.U) := __tmp_3(39, 32)
        arrayRegFiles(__tmp_2 + 5.U) := __tmp_3(47, 40)
        arrayRegFiles(__tmp_2 + 6.U) := __tmp_3(55, 48)
        arrayRegFiles(__tmp_2 + 7.U) := __tmp_3(63, 56)

        val __tmp_4 = 36.U(16.W)
        val __tmp_5 = (0.U(16.W)).asUInt
        arrayRegFiles(__tmp_4 + 0.U) := __tmp_5(7, 0)
        arrayRegFiles(__tmp_4 + 1.U) := __tmp_5(15, 8)

        CP := 4.U
      }

      is(4.U) {
        /*
        $0 = *(0: SP) [signed, Z, 8]  // $0 = $testNum
        goto .5
        */


        val __tmp_6 = (0.U(8.W)).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_6 + 7.U),
          arrayRegFiles(__tmp_6 + 6.U),
          arrayRegFiles(__tmp_6 + 5.U),
          arrayRegFiles(__tmp_6 + 4.U),
          arrayRegFiles(__tmp_6 + 3.U),
          arrayRegFiles(__tmp_6 + 2.U),
          arrayRegFiles(__tmp_6 + 1.U),
          arrayRegFiles(__tmp_6 + 0.U)
        ).asSInt.pad(GENERAL_REG_WIDTH).asUInt

        CP := 5.U
      }

      is(5.U) {
        /*
        if ((($0: Z) < (0: Z)) | (($0: Z) ≡ (0: Z))) goto .6 else goto .12
        */


        CP := Mux((((generalRegFiles(0.U).asSInt < 0.S(64.W)).asUInt | (generalRegFiles(0.U).asSInt === 0.S(64.W)).asUInt).asUInt) === 1.U, 6.U, 12.U)
      }

      is(6.U) {
        /*
        SP = SP + 10
        goto .7
        */


        SP := SP + 10.U

        CP := 7.U
      }

      is(7.U) {
        /*
        *SP = (9: CP) [unsigned, CP, 2]  // $ret@0 = 1325
        *(SP - (8: SP)) = ($0: Z) [signed, Z, 8]  // save $0 (Z)
        goto .8
        */


        val __tmp_7 = SP
        val __tmp_8 = (9.U(16.W)).asUInt
        arrayRegFiles(__tmp_7 + 0.U) := __tmp_8(7, 0)
        arrayRegFiles(__tmp_7 + 1.U) := __tmp_8(15, 8)

        val __tmp_9 = (SP - 8.U(8.W))
        val __tmp_10 = (generalRegFiles(0.U).asSInt).asUInt
        arrayRegFiles(__tmp_9 + 0.U) := __tmp_10(7, 0)
        arrayRegFiles(__tmp_9 + 1.U) := __tmp_10(15, 8)
        arrayRegFiles(__tmp_9 + 2.U) := __tmp_10(23, 16)
        arrayRegFiles(__tmp_9 + 3.U) := __tmp_10(31, 24)
        arrayRegFiles(__tmp_9 + 4.U) := __tmp_10(39, 32)
        arrayRegFiles(__tmp_9 + 5.U) := __tmp_10(47, 40)
        arrayRegFiles(__tmp_9 + 6.U) := __tmp_10(55, 48)
        arrayRegFiles(__tmp_9 + 7.U) := __tmp_10(63, 56)

        CP := 8.U
      }

      is(8.U) {
        /*
        decl $ret: CP [@0, 2]
        goto .20
        */


        CP := 20.U
      }

      is(9.U) {
        /*
        $0 = *(SP - (8: SP)) [signed, Z, 8]  // restore $0 (Z)
        goto .10
        */


        val __tmp_11 = ((SP - 8.U(8.W))).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_11 + 7.U),
          arrayRegFiles(__tmp_11 + 6.U),
          arrayRegFiles(__tmp_11 + 5.U),
          arrayRegFiles(__tmp_11 + 4.U),
          arrayRegFiles(__tmp_11 + 3.U),
          arrayRegFiles(__tmp_11 + 2.U),
          arrayRegFiles(__tmp_11 + 1.U),
          arrayRegFiles(__tmp_11 + 0.U)
        ).asSInt.pad(GENERAL_REG_WIDTH).asUInt

        CP := 10.U
      }

      is(10.U) {
        /*
        *(SP + (0: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $ret byte 0
        *(SP + (1: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $ret byte 1
        undecl $ret: CP [@0, 2]
        goto .11
        */


        val __tmp_12 = (SP + 0.U(8.W))
        val __tmp_13 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_12 + 0.U) := __tmp_13(7, 0)

        val __tmp_14 = (SP + 1.U(8.W))
        val __tmp_15 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_14 + 0.U) := __tmp_15(7, 0)

        CP := 11.U
      }

      is(11.U) {
        /*
        SP = SP - 10
        goto .12
        */


        SP := SP - 10.U

        CP := 12.U
      }

      is(12.U) {
        /*
        if ((($0: Z) < (0: Z)) | (($0: Z) ≡ (1: Z))) goto .13 else goto .19
        */


        CP := Mux((((generalRegFiles(0.U).asSInt < 0.S(64.W)).asUInt | (generalRegFiles(0.U).asSInt === 1.S(64.W)).asUInt).asUInt) === 1.U, 13.U, 19.U)
      }

      is(13.U) {
        /*
        SP = SP + 2
        goto .14
        */


        SP := SP + 2.U

        CP := 14.U
      }

      is(14.U) {
        /*
        *SP = (16: CP) [unsigned, CP, 2]  // $ret@0 = 1327
        goto .15
        */


        val __tmp_16 = SP
        val __tmp_17 = (16.U(16.W)).asUInt
        arrayRegFiles(__tmp_16 + 0.U) := __tmp_17(7, 0)
        arrayRegFiles(__tmp_16 + 1.U) := __tmp_17(15, 8)

        CP := 15.U
      }

      is(15.U) {
        /*
        decl $ret: CP [@0, 2]
        goto .37
        */


        CP := 37.U
      }

      is(16.U) {
        /*
        goto .17
        */


        CP := 17.U
      }

      is(17.U) {
        /*
        *(SP + (0: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $ret byte 0
        *(SP + (1: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $ret byte 1
        undecl $ret: CP [@0, 2]
        goto .18
        */


        val __tmp_18 = (SP + 0.U(8.W))
        val __tmp_19 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_18 + 0.U) := __tmp_19(7, 0)

        val __tmp_20 = (SP + 1.U(8.W))
        val __tmp_21 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_20 + 0.U) := __tmp_21(7, 0)

        CP := 18.U
      }

      is(18.U) {
        /*
        SP = SP - 2
        goto .19
        */


        SP := SP - 2.U

        CP := 19.U
      }

      is(19.U) {
        /*
        goto $ret@0
        */


        CP := Cat(
          arrayRegFiles(SP + 0.U + 1.U),
          arrayRegFiles(SP + 0.U + 0.U)
        )

      }

      is(20.U) {
        /*
        alloc add$res@[10,11].CC0F12ED: U16 [@2, 2]
        goto .21
        */


        CP := 21.U
      }

      is(21.U) {
        /*
        SP = SP + 12
        goto .22
        */


        SP := SP + 12.U

        CP := 22.U
      }

      is(22.U) {
        /*
        *SP = (24: CP) [unsigned, CP, 2]  // $ret@0 = 1329
        *(SP + (2: SP)) = (SP - (10: SP)) [unsigned, SP, 1]  // $res@2 = -10
        $8 = (3: U16)
        $9 = (5: U16)
        goto .23
        */


        val __tmp_22 = SP
        val __tmp_23 = (24.U(16.W)).asUInt
        arrayRegFiles(__tmp_22 + 0.U) := __tmp_23(7, 0)
        arrayRegFiles(__tmp_22 + 1.U) := __tmp_23(15, 8)

        val __tmp_24 = (SP + 2.U(8.W))
        val __tmp_25 = ((SP - 10.U(8.W))).asUInt
        arrayRegFiles(__tmp_24 + 0.U) := __tmp_25(7, 0)


        generalRegFiles(8.U) := 3.U(16.W)


        generalRegFiles(9.U) := 5.U(16.W)

        CP := 23.U
      }

      is(23.U) {
        /*
        decl $ret: CP [@0, 2], $res: SP [@2, 3], x: U16 @$0, y: U16 @$1
        $0 = ($8: U16)
        $1 = ($9: U16)
        goto .54
        */



        generalRegFiles(0.U) := generalRegFiles(8.U)


        generalRegFiles(1.U) := generalRegFiles(9.U)

        CP := 54.U
      }

      is(24.U) {
        /*
        $2 = **(SP + (2: SP)) [unsigned, U16, 2]  // $2 = $res
        goto .25
        */


        val __tmp_26 = (Cat(
          arrayRegFiles((SP + 2.U(8.W)) + 0.U)
        )).asUInt
        generalRegFiles(2.U) := Cat(
          arrayRegFiles(__tmp_26 + 1.U),
          arrayRegFiles(__tmp_26 + 0.U)
        ).asUInt

        CP := 25.U
      }

      is(25.U) {
        /*
        *(SP + (2: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $res byte 0
        *(SP + (3: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $res byte 1
        *(SP + (4: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $res byte 2
        *(SP + (0: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $ret byte 0
        *(SP + (1: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $ret byte 1
        undecl y: U16 @$1, x: U16 @$0, $res: SP [@2, 3], $ret: CP [@0, 2]
        goto .26
        */


        val __tmp_27 = (SP + 2.U(8.W))
        val __tmp_28 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_27 + 0.U) := __tmp_28(7, 0)

        val __tmp_29 = (SP + 3.U(8.W))
        val __tmp_30 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_29 + 0.U) := __tmp_30(7, 0)

        val __tmp_31 = (SP + 4.U(8.W))
        val __tmp_32 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_31 + 0.U) := __tmp_32(7, 0)

        val __tmp_33 = (SP + 0.U(8.W))
        val __tmp_34 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_33 + 0.U) := __tmp_34(7, 0)

        val __tmp_35 = (SP + 1.U(8.W))
        val __tmp_36 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_35 + 0.U) := __tmp_36(7, 0)

        CP := 26.U
      }

      is(26.U) {
        /*
        SP = SP - 12
        goto .27
        */


        SP := SP - 12.U

        CP := 27.U
      }

      is(27.U) {
        /*
        alloc printU64Hex$res@[10,11].CC0F12ED: U64 [@4, 8]
        goto .28
        */


        CP := 28.U
      }

      is(28.U) {
        /*
        SP = SP + 12
        goto .29
        */


        SP := SP + 12.U

        CP := 29.U
      }

      is(29.U) {
        /*
        *SP = (31: CP) [unsigned, CP, 2]  // $ret@0 = 1331
        *(SP + (2: SP)) = (SP - (8: SP)) [unsigned, SP, 1]  // $res@2 = -8
        $76 = (8: SP)
        $77 = DP
        $78 = (15: anvil.PrinterIndex.U)
        $79 = (($2: U16) as U64)
        $80 = (4: Z)
        goto .30
        */


        val __tmp_37 = SP
        val __tmp_38 = (31.U(16.W)).asUInt
        arrayRegFiles(__tmp_37 + 0.U) := __tmp_38(7, 0)
        arrayRegFiles(__tmp_37 + 1.U) := __tmp_38(15, 8)

        val __tmp_39 = (SP + 2.U(8.W))
        val __tmp_40 = ((SP - 8.U(8.W))).asUInt
        arrayRegFiles(__tmp_39 + 0.U) := __tmp_40(7, 0)


        generalRegFiles(76.U) := 8.U(8.W)


        generalRegFiles(77.U) := DP


        generalRegFiles(78.U) := 15.U(64.W)


        generalRegFiles(79.U) := generalRegFiles(2.U).asUInt


        generalRegFiles(80.U) := (4.S(64.W)).asUInt

        CP := 30.U
      }

      is(30.U) {
        /*
        decl $ret: CP [@0, 2], $res: SP [@2, 1], buffer: MS[anvil.PrinterIndex.U, U8] @$0, index: anvil.PrinterIndex.U @$1, mask: anvil.PrinterIndex.U @$2, n: U64 @$3, digits: Z @$4
        $0 = ($76: MS[anvil.PrinterIndex.U, U8])
        $1 = ($77: anvil.PrinterIndex.U)
        $2 = ($78: anvil.PrinterIndex.U)
        $3 = ($79: U64)
        $4 = ($80: Z)
        goto .56
        */



        generalRegFiles(0.U) := generalRegFiles(76.U)


        generalRegFiles(1.U) := generalRegFiles(77.U)


        generalRegFiles(2.U) := generalRegFiles(78.U)


        generalRegFiles(3.U) := generalRegFiles(79.U)


        generalRegFiles(4.U) := (generalRegFiles(80.U).asSInt).asUInt

        CP := 56.U
      }

      is(31.U) {
        /*
        $3 = **(SP + (2: SP)) [unsigned, U64, 8]  // $3 = $res
        goto .32
        */


        val __tmp_41 = (Cat(
          arrayRegFiles((SP + 2.U(8.W)) + 0.U)
        )).asUInt
        generalRegFiles(3.U) := Cat(
          arrayRegFiles(__tmp_41 + 7.U),
          arrayRegFiles(__tmp_41 + 6.U),
          arrayRegFiles(__tmp_41 + 5.U),
          arrayRegFiles(__tmp_41 + 4.U),
          arrayRegFiles(__tmp_41 + 3.U),
          arrayRegFiles(__tmp_41 + 2.U),
          arrayRegFiles(__tmp_41 + 1.U),
          arrayRegFiles(__tmp_41 + 0.U)
        ).asUInt

        CP := 32.U
      }

      is(32.U) {
        /*
        *(SP + (2: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $res byte 0
        *(SP + (0: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $ret byte 0
        *(SP + (1: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $ret byte 1
        undecl digits: Z @$4, n: U64 @$3, mask: anvil.PrinterIndex.U @$2, index: anvil.PrinterIndex.U @$1, buffer: MS[anvil.PrinterIndex.U, U8] @$0, $res: SP [@2, 1], $ret: CP [@0, 2]
        goto .33
        */


        val __tmp_42 = (SP + 2.U(8.W))
        val __tmp_43 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_42 + 0.U) := __tmp_43(7, 0)

        val __tmp_44 = (SP + 0.U(8.W))
        val __tmp_45 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_44 + 0.U) := __tmp_45(7, 0)

        val __tmp_46 = (SP + 1.U(8.W))
        val __tmp_47 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_46 + 0.U) := __tmp_47(7, 0)

        CP := 33.U
      }

      is(33.U) {
        /*
        SP = SP - 12
        goto .34
        */


        SP := SP - 12.U

        CP := 34.U
      }

      is(34.U) {
        /*
        DP = DP + (($3: U64) as DP)
        goto .35
        */


        DP := DP + generalRegFiles(3.U).asUInt

        CP := 35.U
      }

      is(35.U) {
        /*
        *(((8: SP) + (12: SP)) + ((DP & (15: DP)) as SP)) = (10: U8) [unsigned, U8, 1]  // $display((DP & (15: DP))) = (10: U8)
        goto .36
        */


        val __tmp_48 = ((8.U(8.W) + 12.U(8.W)) + (DP & 15.U(64.W)).asUInt)
        val __tmp_49 = (10.U(8.W)).asUInt
        arrayRegFiles(__tmp_48 + 0.U) := __tmp_49(7, 0)

        CP := 36.U
      }

      is(36.U) {
        /*
        DP = DP + 1
        goto $ret@0
        */


        DP := DP + 1.U

        CP := Cat(
          arrayRegFiles(SP + 0.U + 1.U),
          arrayRegFiles(SP + 0.U + 0.U)
        )

      }

      is(37.U) {
        /*
        alloc add$res@[14,11].0FCE0892: U16 [@2, 2]
        goto .38
        */


        CP := 38.U
      }

      is(38.U) {
        /*
        SP = SP + 12
        goto .39
        */


        SP := SP + 12.U

        CP := 39.U
      }

      is(39.U) {
        /*
        *SP = (41: CP) [unsigned, CP, 2]  // $ret@0 = 1333
        *(SP + (2: SP)) = (SP - (10: SP)) [unsigned, SP, 1]  // $res@2 = -10
        $8 = (27181: U16)
        $9 = (5: U16)
        goto .40
        */


        val __tmp_50 = SP
        val __tmp_51 = (41.U(16.W)).asUInt
        arrayRegFiles(__tmp_50 + 0.U) := __tmp_51(7, 0)
        arrayRegFiles(__tmp_50 + 1.U) := __tmp_51(15, 8)

        val __tmp_52 = (SP + 2.U(8.W))
        val __tmp_53 = ((SP - 10.U(8.W))).asUInt
        arrayRegFiles(__tmp_52 + 0.U) := __tmp_53(7, 0)


        generalRegFiles(8.U) := 27181.U(16.W)


        generalRegFiles(9.U) := 5.U(16.W)

        CP := 40.U
      }

      is(40.U) {
        /*
        decl $ret: CP [@0, 2], $res: SP [@2, 3], x: U16 @$0, y: U16 @$1
        $0 = ($8: U16)
        $1 = ($9: U16)
        goto .54
        */



        generalRegFiles(0.U) := generalRegFiles(8.U)


        generalRegFiles(1.U) := generalRegFiles(9.U)

        CP := 54.U
      }

      is(41.U) {
        /*
        $2 = **(SP + (2: SP)) [unsigned, U16, 2]  // $2 = $res
        goto .42
        */


        val __tmp_54 = (Cat(
          arrayRegFiles((SP + 2.U(8.W)) + 0.U)
        )).asUInt
        generalRegFiles(2.U) := Cat(
          arrayRegFiles(__tmp_54 + 1.U),
          arrayRegFiles(__tmp_54 + 0.U)
        ).asUInt

        CP := 42.U
      }

      is(42.U) {
        /*
        *(SP + (2: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $res byte 0
        *(SP + (3: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $res byte 1
        *(SP + (4: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $res byte 2
        *(SP + (0: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $ret byte 0
        *(SP + (1: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $ret byte 1
        undecl y: U16 @$1, x: U16 @$0, $res: SP [@2, 3], $ret: CP [@0, 2]
        goto .43
        */


        val __tmp_55 = (SP + 2.U(8.W))
        val __tmp_56 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_55 + 0.U) := __tmp_56(7, 0)

        val __tmp_57 = (SP + 3.U(8.W))
        val __tmp_58 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_57 + 0.U) := __tmp_58(7, 0)

        val __tmp_59 = (SP + 4.U(8.W))
        val __tmp_60 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_59 + 0.U) := __tmp_60(7, 0)

        val __tmp_61 = (SP + 0.U(8.W))
        val __tmp_62 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_61 + 0.U) := __tmp_62(7, 0)

        val __tmp_63 = (SP + 1.U(8.W))
        val __tmp_64 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_63 + 0.U) := __tmp_64(7, 0)

        CP := 43.U
      }

      is(43.U) {
        /*
        SP = SP - 12
        goto .44
        */


        SP := SP - 12.U

        CP := 44.U
      }

      is(44.U) {
        /*
        alloc printU64Hex$res@[14,11].0FCE0892: U64 [@4, 8]
        goto .45
        */


        CP := 45.U
      }

      is(45.U) {
        /*
        SP = SP + 12
        goto .46
        */


        SP := SP + 12.U

        CP := 46.U
      }

      is(46.U) {
        /*
        *SP = (48: CP) [unsigned, CP, 2]  // $ret@0 = 1335
        *(SP + (2: SP)) = (SP - (8: SP)) [unsigned, SP, 1]  // $res@2 = -8
        $76 = (8: SP)
        $77 = DP
        $78 = (15: anvil.PrinterIndex.U)
        $79 = (($2: U16) as U64)
        $80 = (4: Z)
        goto .47
        */


        val __tmp_65 = SP
        val __tmp_66 = (48.U(16.W)).asUInt
        arrayRegFiles(__tmp_65 + 0.U) := __tmp_66(7, 0)
        arrayRegFiles(__tmp_65 + 1.U) := __tmp_66(15, 8)

        val __tmp_67 = (SP + 2.U(8.W))
        val __tmp_68 = ((SP - 8.U(8.W))).asUInt
        arrayRegFiles(__tmp_67 + 0.U) := __tmp_68(7, 0)


        generalRegFiles(76.U) := 8.U(8.W)


        generalRegFiles(77.U) := DP


        generalRegFiles(78.U) := 15.U(64.W)


        generalRegFiles(79.U) := generalRegFiles(2.U).asUInt


        generalRegFiles(80.U) := (4.S(64.W)).asUInt

        CP := 47.U
      }

      is(47.U) {
        /*
        decl $ret: CP [@0, 2], $res: SP [@2, 1], buffer: MS[anvil.PrinterIndex.U, U8] @$0, index: anvil.PrinterIndex.U @$1, mask: anvil.PrinterIndex.U @$2, n: U64 @$3, digits: Z @$4
        $0 = ($76: MS[anvil.PrinterIndex.U, U8])
        $1 = ($77: anvil.PrinterIndex.U)
        $2 = ($78: anvil.PrinterIndex.U)
        $3 = ($79: U64)
        $4 = ($80: Z)
        goto .56
        */



        generalRegFiles(0.U) := generalRegFiles(76.U)


        generalRegFiles(1.U) := generalRegFiles(77.U)


        generalRegFiles(2.U) := generalRegFiles(78.U)


        generalRegFiles(3.U) := generalRegFiles(79.U)


        generalRegFiles(4.U) := (generalRegFiles(80.U).asSInt).asUInt

        CP := 56.U
      }

      is(48.U) {
        /*
        $3 = **(SP + (2: SP)) [unsigned, U64, 8]  // $3 = $res
        goto .49
        */


        val __tmp_69 = (Cat(
          arrayRegFiles((SP + 2.U(8.W)) + 0.U)
        )).asUInt
        generalRegFiles(3.U) := Cat(
          arrayRegFiles(__tmp_69 + 7.U),
          arrayRegFiles(__tmp_69 + 6.U),
          arrayRegFiles(__tmp_69 + 5.U),
          arrayRegFiles(__tmp_69 + 4.U),
          arrayRegFiles(__tmp_69 + 3.U),
          arrayRegFiles(__tmp_69 + 2.U),
          arrayRegFiles(__tmp_69 + 1.U),
          arrayRegFiles(__tmp_69 + 0.U)
        ).asUInt

        CP := 49.U
      }

      is(49.U) {
        /*
        *(SP + (2: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $res byte 0
        *(SP + (0: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $ret byte 0
        *(SP + (1: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $ret byte 1
        undecl digits: Z @$4, n: U64 @$3, mask: anvil.PrinterIndex.U @$2, index: anvil.PrinterIndex.U @$1, buffer: MS[anvil.PrinterIndex.U, U8] @$0, $res: SP [@2, 1], $ret: CP [@0, 2]
        goto .50
        */


        val __tmp_70 = (SP + 2.U(8.W))
        val __tmp_71 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_70 + 0.U) := __tmp_71(7, 0)

        val __tmp_72 = (SP + 0.U(8.W))
        val __tmp_73 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_72 + 0.U) := __tmp_73(7, 0)

        val __tmp_74 = (SP + 1.U(8.W))
        val __tmp_75 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_74 + 0.U) := __tmp_75(7, 0)

        CP := 50.U
      }

      is(50.U) {
        /*
        SP = SP - 12
        goto .51
        */


        SP := SP - 12.U

        CP := 51.U
      }

      is(51.U) {
        /*
        DP = DP + (($3: U64) as DP)
        goto .52
        */


        DP := DP + generalRegFiles(3.U).asUInt

        CP := 52.U
      }

      is(52.U) {
        /*
        *(((8: SP) + (12: SP)) + ((DP & (15: DP)) as SP)) = (10: U8) [unsigned, U8, 1]  // $display((DP & (15: DP))) = (10: U8)
        goto .53
        */


        val __tmp_76 = ((8.U(8.W) + 12.U(8.W)) + (DP & 15.U(64.W)).asUInt)
        val __tmp_77 = (10.U(8.W)).asUInt
        arrayRegFiles(__tmp_76 + 0.U) := __tmp_77(7, 0)

        CP := 53.U
      }

      is(53.U) {
        /*
        DP = DP + 1
        goto $ret@0
        */


        DP := DP + 1.U

        CP := Cat(
          arrayRegFiles(SP + 0.U + 1.U),
          arrayRegFiles(SP + 0.U + 0.U)
        )

      }

      is(54.U) {
        /*
        $2 = (($0: U16) + ($1: U16))
        goto .55
        */



        generalRegFiles(2.U) := (generalRegFiles(0.U) + generalRegFiles(1.U))

        CP := 55.U
      }

      is(55.U) {
        /*
        **(SP + (2: SP)) = ($2: U16) [unsigned, U16, 2]  // $res = ($2: U16)
        goto $ret@0
        */


        val __tmp_78 = Cat(
          arrayRegFiles((SP + 2.U(8.W)) + 0.U)
        )
        val __tmp_79 = (generalRegFiles(2.U)).asUInt
        arrayRegFiles(__tmp_78 + 0.U) := __tmp_79(7, 0)
        arrayRegFiles(__tmp_78 + 1.U) := __tmp_79(15, 8)

        CP := Cat(
          arrayRegFiles(SP + 0.U + 1.U),
          arrayRegFiles(SP + 0.U + 0.U)
        )

      }

      is(56.U) {
        /*
        decl buff: MS[anvil.PrinterIndex.I16, U8] [@3, 30]
        alloc $new@[245,16].6203A7B3: MS[anvil.PrinterIndex.I16, U8] [@33, 30]
        $10 = (SP + (33: SP))
        *(SP + (33: SP)) = (1541243932: U32) [unsigned, U32, 4]  // sha3 type signature of MS[anvil.PrinterIndex.I16, U8]: 0x5BDD841C
        *(SP + (37: SP)) = (16: Z) [signed, Z, 8]  // size of MS[anvil.PrinterIndex.I16, U8]((0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8))
        goto .57
        */



        generalRegFiles(10.U) := (SP + 33.U(8.W))

        val __tmp_80 = (SP + 33.U(8.W))
        val __tmp_81 = (1541243932.U(32.W)).asUInt
        arrayRegFiles(__tmp_80 + 0.U) := __tmp_81(7, 0)
        arrayRegFiles(__tmp_80 + 1.U) := __tmp_81(15, 8)
        arrayRegFiles(__tmp_80 + 2.U) := __tmp_81(23, 16)
        arrayRegFiles(__tmp_80 + 3.U) := __tmp_81(31, 24)

        val __tmp_82 = (SP + 37.U(8.W))
        val __tmp_83 = (16.S(64.W)).asUInt
        arrayRegFiles(__tmp_82 + 0.U) := __tmp_83(7, 0)
        arrayRegFiles(__tmp_82 + 1.U) := __tmp_83(15, 8)
        arrayRegFiles(__tmp_82 + 2.U) := __tmp_83(23, 16)
        arrayRegFiles(__tmp_82 + 3.U) := __tmp_83(31, 24)
        arrayRegFiles(__tmp_82 + 4.U) := __tmp_83(39, 32)
        arrayRegFiles(__tmp_82 + 5.U) := __tmp_83(47, 40)
        arrayRegFiles(__tmp_82 + 6.U) := __tmp_83(55, 48)
        arrayRegFiles(__tmp_82 + 7.U) := __tmp_83(63, 56)

        CP := 57.U
      }

      is(57.U) {
        /*
        *((($10: MS[anvil.PrinterIndex.I16, U8]) + (12: SP)) + ((0: anvil.PrinterIndex.I16) as SP)) = (0: U8) [unsigned, U8, 1]  // ($10: MS[anvil.PrinterIndex.I16, U8])((0: anvil.PrinterIndex.I16)) = (0: U8)
        *((($10: MS[anvil.PrinterIndex.I16, U8]) + (12: SP)) + ((1: anvil.PrinterIndex.I16) as SP)) = (0: U8) [unsigned, U8, 1]  // ($10: MS[anvil.PrinterIndex.I16, U8])((1: anvil.PrinterIndex.I16)) = (0: U8)
        *((($10: MS[anvil.PrinterIndex.I16, U8]) + (12: SP)) + ((2: anvil.PrinterIndex.I16) as SP)) = (0: U8) [unsigned, U8, 1]  // ($10: MS[anvil.PrinterIndex.I16, U8])((2: anvil.PrinterIndex.I16)) = (0: U8)
        *((($10: MS[anvil.PrinterIndex.I16, U8]) + (12: SP)) + ((3: anvil.PrinterIndex.I16) as SP)) = (0: U8) [unsigned, U8, 1]  // ($10: MS[anvil.PrinterIndex.I16, U8])((3: anvil.PrinterIndex.I16)) = (0: U8)
        *((($10: MS[anvil.PrinterIndex.I16, U8]) + (12: SP)) + ((4: anvil.PrinterIndex.I16) as SP)) = (0: U8) [unsigned, U8, 1]  // ($10: MS[anvil.PrinterIndex.I16, U8])((4: anvil.PrinterIndex.I16)) = (0: U8)
        *((($10: MS[anvil.PrinterIndex.I16, U8]) + (12: SP)) + ((5: anvil.PrinterIndex.I16) as SP)) = (0: U8) [unsigned, U8, 1]  // ($10: MS[anvil.PrinterIndex.I16, U8])((5: anvil.PrinterIndex.I16)) = (0: U8)
        *((($10: MS[anvil.PrinterIndex.I16, U8]) + (12: SP)) + ((6: anvil.PrinterIndex.I16) as SP)) = (0: U8) [unsigned, U8, 1]  // ($10: MS[anvil.PrinterIndex.I16, U8])((6: anvil.PrinterIndex.I16)) = (0: U8)
        *((($10: MS[anvil.PrinterIndex.I16, U8]) + (12: SP)) + ((7: anvil.PrinterIndex.I16) as SP)) = (0: U8) [unsigned, U8, 1]  // ($10: MS[anvil.PrinterIndex.I16, U8])((7: anvil.PrinterIndex.I16)) = (0: U8)
        *((($10: MS[anvil.PrinterIndex.I16, U8]) + (12: SP)) + ((8: anvil.PrinterIndex.I16) as SP)) = (0: U8) [unsigned, U8, 1]  // ($10: MS[anvil.PrinterIndex.I16, U8])((8: anvil.PrinterIndex.I16)) = (0: U8)
        *((($10: MS[anvil.PrinterIndex.I16, U8]) + (12: SP)) + ((9: anvil.PrinterIndex.I16) as SP)) = (0: U8) [unsigned, U8, 1]  // ($10: MS[anvil.PrinterIndex.I16, U8])((9: anvil.PrinterIndex.I16)) = (0: U8)
        *((($10: MS[anvil.PrinterIndex.I16, U8]) + (12: SP)) + ((10: anvil.PrinterIndex.I16) as SP)) = (0: U8) [unsigned, U8, 1]  // ($10: MS[anvil.PrinterIndex.I16, U8])((10: anvil.PrinterIndex.I16)) = (0: U8)
        *((($10: MS[anvil.PrinterIndex.I16, U8]) + (12: SP)) + ((11: anvil.PrinterIndex.I16) as SP)) = (0: U8) [unsigned, U8, 1]  // ($10: MS[anvil.PrinterIndex.I16, U8])((11: anvil.PrinterIndex.I16)) = (0: U8)
        *((($10: MS[anvil.PrinterIndex.I16, U8]) + (12: SP)) + ((12: anvil.PrinterIndex.I16) as SP)) = (0: U8) [unsigned, U8, 1]  // ($10: MS[anvil.PrinterIndex.I16, U8])((12: anvil.PrinterIndex.I16)) = (0: U8)
        *((($10: MS[anvil.PrinterIndex.I16, U8]) + (12: SP)) + ((13: anvil.PrinterIndex.I16) as SP)) = (0: U8) [unsigned, U8, 1]  // ($10: MS[anvil.PrinterIndex.I16, U8])((13: anvil.PrinterIndex.I16)) = (0: U8)
        *((($10: MS[anvil.PrinterIndex.I16, U8]) + (12: SP)) + ((14: anvil.PrinterIndex.I16) as SP)) = (0: U8) [unsigned, U8, 1]  // ($10: MS[anvil.PrinterIndex.I16, U8])((14: anvil.PrinterIndex.I16)) = (0: U8)
        *((($10: MS[anvil.PrinterIndex.I16, U8]) + (12: SP)) + ((15: anvil.PrinterIndex.I16) as SP)) = (0: U8) [unsigned, U8, 1]  // ($10: MS[anvil.PrinterIndex.I16, U8])((15: anvil.PrinterIndex.I16)) = (0: U8)
        goto .58
        */


        val __tmp_84 = ((generalRegFiles(10.U) + 12.U(8.W)) + 0.S(8.W).asUInt)
        val __tmp_85 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_84 + 0.U) := __tmp_85(7, 0)

        val __tmp_86 = ((generalRegFiles(10.U) + 12.U(8.W)) + 1.S(8.W).asUInt)
        val __tmp_87 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_86 + 0.U) := __tmp_87(7, 0)

        val __tmp_88 = ((generalRegFiles(10.U) + 12.U(8.W)) + 2.S(8.W).asUInt)
        val __tmp_89 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_88 + 0.U) := __tmp_89(7, 0)

        val __tmp_90 = ((generalRegFiles(10.U) + 12.U(8.W)) + 3.S(8.W).asUInt)
        val __tmp_91 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_90 + 0.U) := __tmp_91(7, 0)

        val __tmp_92 = ((generalRegFiles(10.U) + 12.U(8.W)) + 4.S(8.W).asUInt)
        val __tmp_93 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_92 + 0.U) := __tmp_93(7, 0)

        val __tmp_94 = ((generalRegFiles(10.U) + 12.U(8.W)) + 5.S(8.W).asUInt)
        val __tmp_95 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_94 + 0.U) := __tmp_95(7, 0)

        val __tmp_96 = ((generalRegFiles(10.U) + 12.U(8.W)) + 6.S(8.W).asUInt)
        val __tmp_97 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_96 + 0.U) := __tmp_97(7, 0)

        val __tmp_98 = ((generalRegFiles(10.U) + 12.U(8.W)) + 7.S(8.W).asUInt)
        val __tmp_99 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_98 + 0.U) := __tmp_99(7, 0)

        val __tmp_100 = ((generalRegFiles(10.U) + 12.U(8.W)) + 8.S(8.W).asUInt)
        val __tmp_101 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_100 + 0.U) := __tmp_101(7, 0)

        val __tmp_102 = ((generalRegFiles(10.U) + 12.U(8.W)) + 9.S(8.W).asUInt)
        val __tmp_103 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_102 + 0.U) := __tmp_103(7, 0)

        val __tmp_104 = ((generalRegFiles(10.U) + 12.U(8.W)) + 10.S(8.W).asUInt)
        val __tmp_105 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_104 + 0.U) := __tmp_105(7, 0)

        val __tmp_106 = ((generalRegFiles(10.U) + 12.U(8.W)) + 11.S(8.W).asUInt)
        val __tmp_107 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_106 + 0.U) := __tmp_107(7, 0)

        val __tmp_108 = ((generalRegFiles(10.U) + 12.U(8.W)) + 12.S(8.W).asUInt)
        val __tmp_109 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_108 + 0.U) := __tmp_109(7, 0)

        val __tmp_110 = ((generalRegFiles(10.U) + 12.U(8.W)) + 13.S(8.W).asUInt)
        val __tmp_111 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_110 + 0.U) := __tmp_111(7, 0)

        val __tmp_112 = ((generalRegFiles(10.U) + 12.U(8.W)) + 14.S(8.W).asUInt)
        val __tmp_113 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_112 + 0.U) := __tmp_113(7, 0)

        val __tmp_114 = ((generalRegFiles(10.U) + 12.U(8.W)) + 15.S(8.W).asUInt)
        val __tmp_115 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_114 + 0.U) := __tmp_115(7, 0)

        CP := 58.U
      }

      is(58.U) {
        /*
        (SP + (3: SP)) [MS[anvil.PrinterIndex.I16, U8], 30]  <-  ($10: MS[anvil.PrinterIndex.I16, U8]) [MS[anvil.PrinterIndex.I16, U8], (30: SP)]  // buff = ($10: MS[anvil.PrinterIndex.I16, U8])
        goto .146
        */


        val __tmp_116 = (SP + 3.U(8.W))
        val __tmp_117 = generalRegFiles(10.U)
        val __tmp_118 = 30.U(8.W)

        when(Idx < __tmp_118) {
          arrayRegFiles(__tmp_116 + Idx + 0.U) := arrayRegFiles(__tmp_117 + Idx + 0.U)
          arrayRegFiles(__tmp_116 + Idx + 1.U) := arrayRegFiles(__tmp_117 + Idx + 1.U)
          arrayRegFiles(__tmp_116 + Idx + 2.U) := arrayRegFiles(__tmp_117 + Idx + 2.U)
          arrayRegFiles(__tmp_116 + Idx + 3.U) := arrayRegFiles(__tmp_117 + Idx + 3.U)
          arrayRegFiles(__tmp_116 + Idx + 4.U) := arrayRegFiles(__tmp_117 + Idx + 4.U)
          arrayRegFiles(__tmp_116 + Idx + 5.U) := arrayRegFiles(__tmp_117 + Idx + 5.U)
          arrayRegFiles(__tmp_116 + Idx + 6.U) := arrayRegFiles(__tmp_117 + Idx + 6.U)
          arrayRegFiles(__tmp_116 + Idx + 7.U) := arrayRegFiles(__tmp_117 + Idx + 7.U)
          Idx := Idx + 8.U
          LeftByteRounds := __tmp_118 - Idx
        } .elsewhen(IdxLeftByteRounds < LeftByteRounds) {
          val __tmp_119 = Idx - 8.U
          arrayRegFiles(__tmp_116 + __tmp_119 + IdxLeftByteRounds) := arrayRegFiles(__tmp_117 + __tmp_119 + IdxLeftByteRounds)
          IdxLeftByteRounds := IdxLeftByteRounds + 1.U
        } .otherwise {
          Idx := 0.U
          IdxLeftByteRounds := 0.U
          LeftByteRounds := 0.U
          CP := 146.U
        }


      }

      is(59.U) {
        /*
        decl i: anvil.PrinterIndex.I16 @$5
        $5 = (0: anvil.PrinterIndex.I16)
        goto .60
        */



        generalRegFiles(5.U) := (0.S(8.W)).asUInt

        CP := 60.U
      }

      is(60.U) {
        /*
        decl m: U64 @$6
        $6 = ($3: U64)
        goto .61
        */



        generalRegFiles(6.U) := generalRegFiles(3.U)

        CP := 61.U
      }

      is(61.U) {
        /*
        decl d: Z @$7
        $7 = ($4: Z)
        goto .62
        */



        generalRegFiles(7.U) := (generalRegFiles(4.U).asSInt).asUInt

        CP := 62.U
      }

      is(62.U) {
        /*
        $10 = ($6: U64)
        goto .63
        */



        generalRegFiles(10.U) := generalRegFiles(6.U)

        CP := 63.U
      }

      is(63.U) {
        /*
        $11 = (($10: U64) > (0: U64))
        goto .64
        */



        generalRegFiles(11.U) := (generalRegFiles(10.U) > 0.U(64.W)).asUInt

        CP := 64.U
      }

      is(64.U) {
        /*
        $12 = ($7: Z)
        goto .65
        */



        generalRegFiles(12.U) := (generalRegFiles(7.U).asSInt).asUInt

        CP := 65.U
      }

      is(65.U) {
        /*
        $13 = (($12: Z) > (0: Z))
        goto .66
        */



        generalRegFiles(13.U) := (generalRegFiles(12.U).asSInt > 0.S(64.W)).asUInt

        CP := 66.U
      }

      is(66.U) {
        /*
        $14 = (($11: B) & ($13: B))
        goto .67
        */



        generalRegFiles(14.U) := (generalRegFiles(11.U) & generalRegFiles(13.U))

        CP := 67.U
      }

      is(67.U) {
        /*
        if ($14: B) goto .68 else goto .112
        */


        CP := Mux((generalRegFiles(14.U).asUInt) === 1.U, 68.U, 112.U)
      }

      is(68.U) {
        /*
        $10 = ($6: U64)
        goto .69
        */



        generalRegFiles(10.U) := generalRegFiles(6.U)

        CP := 69.U
      }

      is(69.U) {
        /*
        $11 = (($10: U64) & (15: U64))
        goto .70
        */



        generalRegFiles(11.U) := (generalRegFiles(10.U) & 15.U(64.W))

        CP := 70.U
      }

      is(70.U) {
        /*
        switch (($11: U64))
          (0: U64): goto 71
          (1: U64): goto 73
          (2: U64): goto 75
          (3: U64): goto 77
          (4: U64): goto 79
          (5: U64): goto 81
          (6: U64): goto 83
          (7: U64): goto 85
          (8: U64): goto 87
          (9: U64): goto 89
          (10: U64): goto 91
          (11: U64): goto 93
          (12: U64): goto 95
          (13: U64): goto 97
          (14: U64): goto 99
          (15: U64): goto 101

        */


        val __tmp_120 = generalRegFiles(11.U)

        switch(__tmp_120) {

          is(0.U(64.W)) {
            CP := 71.U
          }


          is(1.U(64.W)) {
            CP := 73.U
          }


          is(2.U(64.W)) {
            CP := 75.U
          }


          is(3.U(64.W)) {
            CP := 77.U
          }


          is(4.U(64.W)) {
            CP := 79.U
          }


          is(5.U(64.W)) {
            CP := 81.U
          }


          is(6.U(64.W)) {
            CP := 83.U
          }


          is(7.U(64.W)) {
            CP := 85.U
          }


          is(8.U(64.W)) {
            CP := 87.U
          }


          is(9.U(64.W)) {
            CP := 89.U
          }


          is(10.U(64.W)) {
            CP := 91.U
          }


          is(11.U(64.W)) {
            CP := 93.U
          }


          is(12.U(64.W)) {
            CP := 95.U
          }


          is(13.U(64.W)) {
            CP := 97.U
          }


          is(14.U(64.W)) {
            CP := 99.U
          }


          is(15.U(64.W)) {
            CP := 101.U
          }

        }

      }

      is(71.U) {
        /*
        $10 = (SP + (3: SP))
        $11 = ($5: anvil.PrinterIndex.I16)
        goto .72
        */



        generalRegFiles(10.U) := (SP + 3.U(8.W))


        generalRegFiles(11.U) := (generalRegFiles(5.U).asSInt).asUInt

        CP := 72.U
      }

      is(72.U) {
        /*
        *((($10: MS[anvil.PrinterIndex.I16, U8]) + (12: SP)) + (($11: anvil.PrinterIndex.I16) as SP)) = (48: U8) [unsigned, U8, 1]  // ($10: MS[anvil.PrinterIndex.I16, U8])(($11: anvil.PrinterIndex.I16)) = (48: U8)
        goto .103
        */


        val __tmp_121 = ((generalRegFiles(10.U) + 12.U(8.W)) + generalRegFiles(11.U).asSInt.asUInt)
        val __tmp_122 = (48.U(8.W)).asUInt
        arrayRegFiles(__tmp_121 + 0.U) := __tmp_122(7, 0)

        CP := 103.U
      }

      is(73.U) {
        /*
        $10 = (SP + (3: SP))
        $11 = ($5: anvil.PrinterIndex.I16)
        goto .74
        */



        generalRegFiles(10.U) := (SP + 3.U(8.W))


        generalRegFiles(11.U) := (generalRegFiles(5.U).asSInt).asUInt

        CP := 74.U
      }

      is(74.U) {
        /*
        *((($10: MS[anvil.PrinterIndex.I16, U8]) + (12: SP)) + (($11: anvil.PrinterIndex.I16) as SP)) = (49: U8) [unsigned, U8, 1]  // ($10: MS[anvil.PrinterIndex.I16, U8])(($11: anvil.PrinterIndex.I16)) = (49: U8)
        goto .103
        */


        val __tmp_123 = ((generalRegFiles(10.U) + 12.U(8.W)) + generalRegFiles(11.U).asSInt.asUInt)
        val __tmp_124 = (49.U(8.W)).asUInt
        arrayRegFiles(__tmp_123 + 0.U) := __tmp_124(7, 0)

        CP := 103.U
      }

      is(75.U) {
        /*
        $10 = (SP + (3: SP))
        $11 = ($5: anvil.PrinterIndex.I16)
        goto .76
        */



        generalRegFiles(10.U) := (SP + 3.U(8.W))


        generalRegFiles(11.U) := (generalRegFiles(5.U).asSInt).asUInt

        CP := 76.U
      }

      is(76.U) {
        /*
        *((($10: MS[anvil.PrinterIndex.I16, U8]) + (12: SP)) + (($11: anvil.PrinterIndex.I16) as SP)) = (50: U8) [unsigned, U8, 1]  // ($10: MS[anvil.PrinterIndex.I16, U8])(($11: anvil.PrinterIndex.I16)) = (50: U8)
        goto .103
        */


        val __tmp_125 = ((generalRegFiles(10.U) + 12.U(8.W)) + generalRegFiles(11.U).asSInt.asUInt)
        val __tmp_126 = (50.U(8.W)).asUInt
        arrayRegFiles(__tmp_125 + 0.U) := __tmp_126(7, 0)

        CP := 103.U
      }

      is(77.U) {
        /*
        $10 = (SP + (3: SP))
        $11 = ($5: anvil.PrinterIndex.I16)
        goto .78
        */



        generalRegFiles(10.U) := (SP + 3.U(8.W))


        generalRegFiles(11.U) := (generalRegFiles(5.U).asSInt).asUInt

        CP := 78.U
      }

      is(78.U) {
        /*
        *((($10: MS[anvil.PrinterIndex.I16, U8]) + (12: SP)) + (($11: anvil.PrinterIndex.I16) as SP)) = (51: U8) [unsigned, U8, 1]  // ($10: MS[anvil.PrinterIndex.I16, U8])(($11: anvil.PrinterIndex.I16)) = (51: U8)
        goto .103
        */


        val __tmp_127 = ((generalRegFiles(10.U) + 12.U(8.W)) + generalRegFiles(11.U).asSInt.asUInt)
        val __tmp_128 = (51.U(8.W)).asUInt
        arrayRegFiles(__tmp_127 + 0.U) := __tmp_128(7, 0)

        CP := 103.U
      }

      is(79.U) {
        /*
        $10 = (SP + (3: SP))
        $11 = ($5: anvil.PrinterIndex.I16)
        goto .80
        */



        generalRegFiles(10.U) := (SP + 3.U(8.W))


        generalRegFiles(11.U) := (generalRegFiles(5.U).asSInt).asUInt

        CP := 80.U
      }

      is(80.U) {
        /*
        *((($10: MS[anvil.PrinterIndex.I16, U8]) + (12: SP)) + (($11: anvil.PrinterIndex.I16) as SP)) = (52: U8) [unsigned, U8, 1]  // ($10: MS[anvil.PrinterIndex.I16, U8])(($11: anvil.PrinterIndex.I16)) = (52: U8)
        goto .103
        */


        val __tmp_129 = ((generalRegFiles(10.U) + 12.U(8.W)) + generalRegFiles(11.U).asSInt.asUInt)
        val __tmp_130 = (52.U(8.W)).asUInt
        arrayRegFiles(__tmp_129 + 0.U) := __tmp_130(7, 0)

        CP := 103.U
      }

      is(81.U) {
        /*
        $10 = (SP + (3: SP))
        $11 = ($5: anvil.PrinterIndex.I16)
        goto .82
        */



        generalRegFiles(10.U) := (SP + 3.U(8.W))


        generalRegFiles(11.U) := (generalRegFiles(5.U).asSInt).asUInt

        CP := 82.U
      }

      is(82.U) {
        /*
        *((($10: MS[anvil.PrinterIndex.I16, U8]) + (12: SP)) + (($11: anvil.PrinterIndex.I16) as SP)) = (53: U8) [unsigned, U8, 1]  // ($10: MS[anvil.PrinterIndex.I16, U8])(($11: anvil.PrinterIndex.I16)) = (53: U8)
        goto .103
        */


        val __tmp_131 = ((generalRegFiles(10.U) + 12.U(8.W)) + generalRegFiles(11.U).asSInt.asUInt)
        val __tmp_132 = (53.U(8.W)).asUInt
        arrayRegFiles(__tmp_131 + 0.U) := __tmp_132(7, 0)

        CP := 103.U
      }

      is(83.U) {
        /*
        $10 = (SP + (3: SP))
        $11 = ($5: anvil.PrinterIndex.I16)
        goto .84
        */



        generalRegFiles(10.U) := (SP + 3.U(8.W))


        generalRegFiles(11.U) := (generalRegFiles(5.U).asSInt).asUInt

        CP := 84.U
      }

      is(84.U) {
        /*
        *((($10: MS[anvil.PrinterIndex.I16, U8]) + (12: SP)) + (($11: anvil.PrinterIndex.I16) as SP)) = (54: U8) [unsigned, U8, 1]  // ($10: MS[anvil.PrinterIndex.I16, U8])(($11: anvil.PrinterIndex.I16)) = (54: U8)
        goto .103
        */


        val __tmp_133 = ((generalRegFiles(10.U) + 12.U(8.W)) + generalRegFiles(11.U).asSInt.asUInt)
        val __tmp_134 = (54.U(8.W)).asUInt
        arrayRegFiles(__tmp_133 + 0.U) := __tmp_134(7, 0)

        CP := 103.U
      }

      is(85.U) {
        /*
        $10 = (SP + (3: SP))
        $11 = ($5: anvil.PrinterIndex.I16)
        goto .86
        */



        generalRegFiles(10.U) := (SP + 3.U(8.W))


        generalRegFiles(11.U) := (generalRegFiles(5.U).asSInt).asUInt

        CP := 86.U
      }

      is(86.U) {
        /*
        *((($10: MS[anvil.PrinterIndex.I16, U8]) + (12: SP)) + (($11: anvil.PrinterIndex.I16) as SP)) = (55: U8) [unsigned, U8, 1]  // ($10: MS[anvil.PrinterIndex.I16, U8])(($11: anvil.PrinterIndex.I16)) = (55: U8)
        goto .103
        */


        val __tmp_135 = ((generalRegFiles(10.U) + 12.U(8.W)) + generalRegFiles(11.U).asSInt.asUInt)
        val __tmp_136 = (55.U(8.W)).asUInt
        arrayRegFiles(__tmp_135 + 0.U) := __tmp_136(7, 0)

        CP := 103.U
      }

      is(87.U) {
        /*
        $10 = (SP + (3: SP))
        $11 = ($5: anvil.PrinterIndex.I16)
        goto .88
        */



        generalRegFiles(10.U) := (SP + 3.U(8.W))


        generalRegFiles(11.U) := (generalRegFiles(5.U).asSInt).asUInt

        CP := 88.U
      }

      is(88.U) {
        /*
        *((($10: MS[anvil.PrinterIndex.I16, U8]) + (12: SP)) + (($11: anvil.PrinterIndex.I16) as SP)) = (56: U8) [unsigned, U8, 1]  // ($10: MS[anvil.PrinterIndex.I16, U8])(($11: anvil.PrinterIndex.I16)) = (56: U8)
        goto .103
        */


        val __tmp_137 = ((generalRegFiles(10.U) + 12.U(8.W)) + generalRegFiles(11.U).asSInt.asUInt)
        val __tmp_138 = (56.U(8.W)).asUInt
        arrayRegFiles(__tmp_137 + 0.U) := __tmp_138(7, 0)

        CP := 103.U
      }

      is(89.U) {
        /*
        $10 = (SP + (3: SP))
        $11 = ($5: anvil.PrinterIndex.I16)
        goto .90
        */



        generalRegFiles(10.U) := (SP + 3.U(8.W))


        generalRegFiles(11.U) := (generalRegFiles(5.U).asSInt).asUInt

        CP := 90.U
      }

      is(90.U) {
        /*
        *((($10: MS[anvil.PrinterIndex.I16, U8]) + (12: SP)) + (($11: anvil.PrinterIndex.I16) as SP)) = (57: U8) [unsigned, U8, 1]  // ($10: MS[anvil.PrinterIndex.I16, U8])(($11: anvil.PrinterIndex.I16)) = (57: U8)
        goto .103
        */


        val __tmp_139 = ((generalRegFiles(10.U) + 12.U(8.W)) + generalRegFiles(11.U).asSInt.asUInt)
        val __tmp_140 = (57.U(8.W)).asUInt
        arrayRegFiles(__tmp_139 + 0.U) := __tmp_140(7, 0)

        CP := 103.U
      }

      is(91.U) {
        /*
        $10 = (SP + (3: SP))
        $11 = ($5: anvil.PrinterIndex.I16)
        goto .92
        */



        generalRegFiles(10.U) := (SP + 3.U(8.W))


        generalRegFiles(11.U) := (generalRegFiles(5.U).asSInt).asUInt

        CP := 92.U
      }

      is(92.U) {
        /*
        *((($10: MS[anvil.PrinterIndex.I16, U8]) + (12: SP)) + (($11: anvil.PrinterIndex.I16) as SP)) = (65: U8) [unsigned, U8, 1]  // ($10: MS[anvil.PrinterIndex.I16, U8])(($11: anvil.PrinterIndex.I16)) = (65: U8)
        goto .103
        */


        val __tmp_141 = ((generalRegFiles(10.U) + 12.U(8.W)) + generalRegFiles(11.U).asSInt.asUInt)
        val __tmp_142 = (65.U(8.W)).asUInt
        arrayRegFiles(__tmp_141 + 0.U) := __tmp_142(7, 0)

        CP := 103.U
      }

      is(93.U) {
        /*
        $10 = (SP + (3: SP))
        $11 = ($5: anvil.PrinterIndex.I16)
        goto .94
        */



        generalRegFiles(10.U) := (SP + 3.U(8.W))


        generalRegFiles(11.U) := (generalRegFiles(5.U).asSInt).asUInt

        CP := 94.U
      }

      is(94.U) {
        /*
        *((($10: MS[anvil.PrinterIndex.I16, U8]) + (12: SP)) + (($11: anvil.PrinterIndex.I16) as SP)) = (66: U8) [unsigned, U8, 1]  // ($10: MS[anvil.PrinterIndex.I16, U8])(($11: anvil.PrinterIndex.I16)) = (66: U8)
        goto .103
        */


        val __tmp_143 = ((generalRegFiles(10.U) + 12.U(8.W)) + generalRegFiles(11.U).asSInt.asUInt)
        val __tmp_144 = (66.U(8.W)).asUInt
        arrayRegFiles(__tmp_143 + 0.U) := __tmp_144(7, 0)

        CP := 103.U
      }

      is(95.U) {
        /*
        $10 = (SP + (3: SP))
        $11 = ($5: anvil.PrinterIndex.I16)
        goto .96
        */



        generalRegFiles(10.U) := (SP + 3.U(8.W))


        generalRegFiles(11.U) := (generalRegFiles(5.U).asSInt).asUInt

        CP := 96.U
      }

      is(96.U) {
        /*
        *((($10: MS[anvil.PrinterIndex.I16, U8]) + (12: SP)) + (($11: anvil.PrinterIndex.I16) as SP)) = (67: U8) [unsigned, U8, 1]  // ($10: MS[anvil.PrinterIndex.I16, U8])(($11: anvil.PrinterIndex.I16)) = (67: U8)
        goto .103
        */


        val __tmp_145 = ((generalRegFiles(10.U) + 12.U(8.W)) + generalRegFiles(11.U).asSInt.asUInt)
        val __tmp_146 = (67.U(8.W)).asUInt
        arrayRegFiles(__tmp_145 + 0.U) := __tmp_146(7, 0)

        CP := 103.U
      }

      is(97.U) {
        /*
        $10 = (SP + (3: SP))
        $11 = ($5: anvil.PrinterIndex.I16)
        goto .98
        */



        generalRegFiles(10.U) := (SP + 3.U(8.W))


        generalRegFiles(11.U) := (generalRegFiles(5.U).asSInt).asUInt

        CP := 98.U
      }

      is(98.U) {
        /*
        *((($10: MS[anvil.PrinterIndex.I16, U8]) + (12: SP)) + (($11: anvil.PrinterIndex.I16) as SP)) = (68: U8) [unsigned, U8, 1]  // ($10: MS[anvil.PrinterIndex.I16, U8])(($11: anvil.PrinterIndex.I16)) = (68: U8)
        goto .103
        */


        val __tmp_147 = ((generalRegFiles(10.U) + 12.U(8.W)) + generalRegFiles(11.U).asSInt.asUInt)
        val __tmp_148 = (68.U(8.W)).asUInt
        arrayRegFiles(__tmp_147 + 0.U) := __tmp_148(7, 0)

        CP := 103.U
      }

      is(99.U) {
        /*
        $10 = (SP + (3: SP))
        $11 = ($5: anvil.PrinterIndex.I16)
        goto .100
        */



        generalRegFiles(10.U) := (SP + 3.U(8.W))


        generalRegFiles(11.U) := (generalRegFiles(5.U).asSInt).asUInt

        CP := 100.U
      }

      is(100.U) {
        /*
        *((($10: MS[anvil.PrinterIndex.I16, U8]) + (12: SP)) + (($11: anvil.PrinterIndex.I16) as SP)) = (69: U8) [unsigned, U8, 1]  // ($10: MS[anvil.PrinterIndex.I16, U8])(($11: anvil.PrinterIndex.I16)) = (69: U8)
        goto .103
        */


        val __tmp_149 = ((generalRegFiles(10.U) + 12.U(8.W)) + generalRegFiles(11.U).asSInt.asUInt)
        val __tmp_150 = (69.U(8.W)).asUInt
        arrayRegFiles(__tmp_149 + 0.U) := __tmp_150(7, 0)

        CP := 103.U
      }

      is(101.U) {
        /*
        $10 = (SP + (3: SP))
        $11 = ($5: anvil.PrinterIndex.I16)
        goto .102
        */



        generalRegFiles(10.U) := (SP + 3.U(8.W))


        generalRegFiles(11.U) := (generalRegFiles(5.U).asSInt).asUInt

        CP := 102.U
      }

      is(102.U) {
        /*
        *((($10: MS[anvil.PrinterIndex.I16, U8]) + (12: SP)) + (($11: anvil.PrinterIndex.I16) as SP)) = (70: U8) [unsigned, U8, 1]  // ($10: MS[anvil.PrinterIndex.I16, U8])(($11: anvil.PrinterIndex.I16)) = (70: U8)
        goto .103
        */


        val __tmp_151 = ((generalRegFiles(10.U) + 12.U(8.W)) + generalRegFiles(11.U).asSInt.asUInt)
        val __tmp_152 = (70.U(8.W)).asUInt
        arrayRegFiles(__tmp_151 + 0.U) := __tmp_152(7, 0)

        CP := 103.U
      }

      is(103.U) {
        /*
        $10 = ($6: U64)
        goto .104
        */



        generalRegFiles(10.U) := generalRegFiles(6.U)

        CP := 104.U
      }

      is(104.U) {
        /*
        $11 = (($10: U64) >>> (4: U64))
        goto .105
        */



        generalRegFiles(11.U) := (((generalRegFiles(10.U)) >> 4.U(64.W)(4,0)))

        CP := 105.U
      }

      is(105.U) {
        /*
        $6 = ($11: U64)
        goto .106
        */



        generalRegFiles(6.U) := generalRegFiles(11.U)

        CP := 106.U
      }

      is(106.U) {
        /*
        $10 = ($5: anvil.PrinterIndex.I16)
        goto .107
        */



        generalRegFiles(10.U) := (generalRegFiles(5.U).asSInt).asUInt

        CP := 107.U
      }

      is(107.U) {
        /*
        $11 = (($10: anvil.PrinterIndex.I16) + (1: anvil.PrinterIndex.I16))
        goto .108
        */



        generalRegFiles(11.U) := ((generalRegFiles(10.U).asSInt + 1.S(8.W))).asUInt

        CP := 108.U
      }

      is(108.U) {
        /*
        $5 = ($11: anvil.PrinterIndex.I16)
        goto .109
        */



        generalRegFiles(5.U) := (generalRegFiles(11.U).asSInt).asUInt

        CP := 109.U
      }

      is(109.U) {
        /*
        $10 = ($7: Z)
        goto .110
        */



        generalRegFiles(10.U) := (generalRegFiles(7.U).asSInt).asUInt

        CP := 110.U
      }

      is(110.U) {
        /*
        $11 = (($10: Z) - (1: Z))
        goto .111
        */



        generalRegFiles(11.U) := ((generalRegFiles(10.U).asSInt - 1.S(64.W))).asUInt

        CP := 111.U
      }

      is(111.U) {
        /*
        $7 = ($11: Z)
        goto .62
        */



        generalRegFiles(7.U) := (generalRegFiles(11.U).asSInt).asUInt

        CP := 62.U
      }

      is(112.U) {
        /*
        decl idx: anvil.PrinterIndex.U @$8
        $10 = ($1: anvil.PrinterIndex.U)
        goto .113
        */



        generalRegFiles(10.U) := generalRegFiles(1.U)

        CP := 113.U
      }

      is(113.U) {
        /*
        $8 = ($10: anvil.PrinterIndex.U)
        goto .114
        */



        generalRegFiles(8.U) := generalRegFiles(10.U)

        CP := 114.U
      }

      is(114.U) {
        /*
        $10 = ($7: Z)
        goto .115
        */



        generalRegFiles(10.U) := (generalRegFiles(7.U).asSInt).asUInt

        CP := 115.U
      }

      is(115.U) {
        /*
        $11 = (($10: Z) > (0: Z))
        goto .116
        */



        generalRegFiles(11.U) := (generalRegFiles(10.U).asSInt > 0.S(64.W)).asUInt

        CP := 116.U
      }

      is(116.U) {
        /*
        if ($11: B) goto .117 else goto .126
        */


        CP := Mux((generalRegFiles(11.U).asUInt) === 1.U, 117.U, 126.U)
      }

      is(117.U) {
        /*
        $10 = ($0: MS[anvil.PrinterIndex.U, U8])
        $11 = ($8: anvil.PrinterIndex.U)
        $12 = ($2: anvil.PrinterIndex.U)
        goto .118
        */



        generalRegFiles(10.U) := generalRegFiles(0.U)


        generalRegFiles(11.U) := generalRegFiles(8.U)


        generalRegFiles(12.U) := generalRegFiles(2.U)

        CP := 118.U
      }

      is(118.U) {
        /*
        $13 = (($11: anvil.PrinterIndex.U) & ($12: anvil.PrinterIndex.U))
        goto .119
        */



        generalRegFiles(13.U) := (generalRegFiles(11.U) & generalRegFiles(12.U))

        CP := 119.U
      }

      is(119.U) {
        /*
        *((($10: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($13: anvil.PrinterIndex.U) as SP)) = (48: U8) [unsigned, U8, 1]  // ($10: MS[anvil.PrinterIndex.U, U8])(($13: anvil.PrinterIndex.U)) = (48: U8)
        goto .120
        */


        val __tmp_153 = ((generalRegFiles(10.U) + 12.U(8.W)) + generalRegFiles(13.U).asUInt)
        val __tmp_154 = (48.U(8.W)).asUInt
        arrayRegFiles(__tmp_153 + 0.U) := __tmp_154(7, 0)

        CP := 120.U
      }

      is(120.U) {
        /*
        $10 = ($7: Z)
        goto .121
        */



        generalRegFiles(10.U) := (generalRegFiles(7.U).asSInt).asUInt

        CP := 121.U
      }

      is(121.U) {
        /*
        $11 = (($10: Z) - (1: Z))
        goto .122
        */



        generalRegFiles(11.U) := ((generalRegFiles(10.U).asSInt - 1.S(64.W))).asUInt

        CP := 122.U
      }

      is(122.U) {
        /*
        $7 = ($11: Z)
        goto .123
        */



        generalRegFiles(7.U) := (generalRegFiles(11.U).asSInt).asUInt

        CP := 123.U
      }

      is(123.U) {
        /*
        $10 = ($8: anvil.PrinterIndex.U)
        goto .124
        */



        generalRegFiles(10.U) := generalRegFiles(8.U)

        CP := 124.U
      }

      is(124.U) {
        /*
        $11 = (($10: anvil.PrinterIndex.U) + (1: anvil.PrinterIndex.U))
        goto .125
        */



        generalRegFiles(11.U) := (generalRegFiles(10.U) + 1.U(64.W))

        CP := 125.U
      }

      is(125.U) {
        /*
        $8 = ($11: anvil.PrinterIndex.U)
        goto .114
        */



        generalRegFiles(8.U) := generalRegFiles(11.U)

        CP := 114.U
      }

      is(126.U) {
        /*
        decl j: anvil.PrinterIndex.I16 @$9
        $10 = ($5: anvil.PrinterIndex.I16)
        goto .147
        */



        generalRegFiles(10.U) := (generalRegFiles(5.U).asSInt).asUInt

        CP := 147.U
      }

      is(127.U) {
        /*
        $11 = (($10: anvil.PrinterIndex.I16) - (1: anvil.PrinterIndex.I16))
        goto .128
        */



        generalRegFiles(11.U) := ((generalRegFiles(10.U).asSInt - 1.S(8.W))).asUInt

        CP := 128.U
      }

      is(128.U) {
        /*
        $9 = ($11: anvil.PrinterIndex.I16)
        goto .129
        */



        generalRegFiles(9.U) := (generalRegFiles(11.U).asSInt).asUInt

        CP := 129.U
      }

      is(129.U) {
        /*
        $10 = ($9: anvil.PrinterIndex.I16)
        goto .130
        */



        generalRegFiles(10.U) := (generalRegFiles(9.U).asSInt).asUInt

        CP := 130.U
      }

      is(130.U) {
        /*
        $11 = (($10: anvil.PrinterIndex.I16) >= (0: anvil.PrinterIndex.I16))
        goto .131
        */



        generalRegFiles(11.U) := (generalRegFiles(10.U).asSInt >= 0.S(8.W)).asUInt

        CP := 131.U
      }

      is(131.U) {
        /*
        if ($11: B) goto .132 else goto .143
        */


        CP := Mux((generalRegFiles(11.U).asUInt) === 1.U, 132.U, 143.U)
      }

      is(132.U) {
        /*
        $10 = ($0: MS[anvil.PrinterIndex.U, U8])
        $11 = ($8: anvil.PrinterIndex.U)
        $12 = ($2: anvil.PrinterIndex.U)
        goto .133
        */



        generalRegFiles(10.U) := generalRegFiles(0.U)


        generalRegFiles(11.U) := generalRegFiles(8.U)


        generalRegFiles(12.U) := generalRegFiles(2.U)

        CP := 133.U
      }

      is(133.U) {
        /*
        $13 = (($11: anvil.PrinterIndex.U) & ($12: anvil.PrinterIndex.U))
        goto .134
        */



        generalRegFiles(13.U) := (generalRegFiles(11.U) & generalRegFiles(12.U))

        CP := 134.U
      }

      is(134.U) {
        /*
        $14 = (SP + (3: SP))
        $15 = ($9: anvil.PrinterIndex.I16)
        goto .135
        */



        generalRegFiles(14.U) := (SP + 3.U(8.W))


        generalRegFiles(15.U) := (generalRegFiles(9.U).asSInt).asUInt

        CP := 135.U
      }

      is(135.U) {
        /*
        $16 = *((($14: MS[anvil.PrinterIndex.I16, U8]) + (12: SP)) + (($15: anvil.PrinterIndex.I16) as SP)) [unsigned, U8, 1]  // $16 = ($14: MS[anvil.PrinterIndex.I16, U8])(($15: anvil.PrinterIndex.I16))
        goto .136
        */


        val __tmp_155 = (((generalRegFiles(14.U) + 12.U(8.W)) + generalRegFiles(15.U).asSInt.asUInt)).asUInt
        generalRegFiles(16.U) := Cat(
          arrayRegFiles(__tmp_155 + 0.U)
        ).asUInt

        CP := 136.U
      }

      is(136.U) {
        /*
        *((($10: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($13: anvil.PrinterIndex.U) as SP)) = ($16: U8) [unsigned, U8, 1]  // ($10: MS[anvil.PrinterIndex.U, U8])(($13: anvil.PrinterIndex.U)) = ($16: U8)
        goto .137
        */


        val __tmp_156 = ((generalRegFiles(10.U) + 12.U(8.W)) + generalRegFiles(13.U).asUInt)
        val __tmp_157 = (generalRegFiles(16.U)).asUInt
        arrayRegFiles(__tmp_156 + 0.U) := __tmp_157(7, 0)

        CP := 137.U
      }

      is(137.U) {
        /*
        $10 = ($9: anvil.PrinterIndex.I16)
        goto .138
        */



        generalRegFiles(10.U) := (generalRegFiles(9.U).asSInt).asUInt

        CP := 138.U
      }

      is(138.U) {
        /*
        $11 = (($10: anvil.PrinterIndex.I16) - (1: anvil.PrinterIndex.I16))
        goto .139
        */



        generalRegFiles(11.U) := ((generalRegFiles(10.U).asSInt - 1.S(8.W))).asUInt

        CP := 139.U
      }

      is(139.U) {
        /*
        $9 = ($11: anvil.PrinterIndex.I16)
        goto .140
        */



        generalRegFiles(9.U) := (generalRegFiles(11.U).asSInt).asUInt

        CP := 140.U
      }

      is(140.U) {
        /*
        $10 = ($8: anvil.PrinterIndex.U)
        goto .141
        */



        generalRegFiles(10.U) := generalRegFiles(8.U)

        CP := 141.U
      }

      is(141.U) {
        /*
        $11 = (($10: anvil.PrinterIndex.U) + (1: anvil.PrinterIndex.U))
        goto .142
        */



        generalRegFiles(11.U) := (generalRegFiles(10.U) + 1.U(64.W))

        CP := 142.U
      }

      is(142.U) {
        /*
        $8 = ($11: anvil.PrinterIndex.U)
        goto .129
        */



        generalRegFiles(8.U) := generalRegFiles(11.U)

        CP := 129.U
      }

      is(143.U) {
        /*
        $10 = ($4: Z)
        goto .144
        */



        generalRegFiles(10.U) := (generalRegFiles(4.U).asSInt).asUInt

        CP := 144.U
      }

      is(144.U) {
        /*
        $11 = (($10: Z) as U64)
        goto .145
        */



        generalRegFiles(11.U) := generalRegFiles(10.U).asSInt.asUInt

        CP := 145.U
      }

      is(145.U) {
        /*
        **(SP + (2: SP)) = ($11: U64) [unsigned, U64, 8]  // $res = ($11: U64)
        goto $ret@0
        */


        val __tmp_158 = Cat(
          arrayRegFiles((SP + 2.U(8.W)) + 0.U)
        )
        val __tmp_159 = (generalRegFiles(11.U)).asUInt
        arrayRegFiles(__tmp_158 + 0.U) := __tmp_159(7, 0)
        arrayRegFiles(__tmp_158 + 1.U) := __tmp_159(15, 8)
        arrayRegFiles(__tmp_158 + 2.U) := __tmp_159(23, 16)
        arrayRegFiles(__tmp_158 + 3.U) := __tmp_159(31, 24)
        arrayRegFiles(__tmp_158 + 4.U) := __tmp_159(39, 32)
        arrayRegFiles(__tmp_158 + 5.U) := __tmp_159(47, 40)
        arrayRegFiles(__tmp_158 + 6.U) := __tmp_159(55, 48)
        arrayRegFiles(__tmp_158 + 7.U) := __tmp_159(63, 56)

        CP := Cat(
          arrayRegFiles(SP + 0.U + 1.U),
          arrayRegFiles(SP + 0.U + 0.U)
        )

      }

      is(146.U) {
        /*
        *(SP + (33: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[245,16].6203A7B3 byte 0
        *(SP + (34: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[245,16].6203A7B3 byte 1
        *(SP + (35: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[245,16].6203A7B3 byte 2
        *(SP + (36: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[245,16].6203A7B3 byte 3
        *(SP + (37: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[245,16].6203A7B3 byte 4
        *(SP + (38: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[245,16].6203A7B3 byte 5
        *(SP + (39: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[245,16].6203A7B3 byte 6
        *(SP + (40: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[245,16].6203A7B3 byte 7
        *(SP + (41: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[245,16].6203A7B3 byte 8
        *(SP + (42: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[245,16].6203A7B3 byte 9
        *(SP + (43: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[245,16].6203A7B3 byte 10
        *(SP + (44: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[245,16].6203A7B3 byte 11
        *(SP + (45: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[245,16].6203A7B3 byte 12
        *(SP + (46: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[245,16].6203A7B3 byte 13
        *(SP + (47: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[245,16].6203A7B3 byte 14
        *(SP + (48: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[245,16].6203A7B3 byte 15
        *(SP + (49: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[245,16].6203A7B3 byte 16
        *(SP + (50: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[245,16].6203A7B3 byte 17
        *(SP + (51: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[245,16].6203A7B3 byte 18
        *(SP + (52: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[245,16].6203A7B3 byte 19
        *(SP + (53: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[245,16].6203A7B3 byte 20
        *(SP + (54: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[245,16].6203A7B3 byte 21
        *(SP + (55: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[245,16].6203A7B3 byte 22
        *(SP + (56: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[245,16].6203A7B3 byte 23
        *(SP + (57: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[245,16].6203A7B3 byte 24
        *(SP + (58: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[245,16].6203A7B3 byte 25
        *(SP + (59: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[245,16].6203A7B3 byte 26
        *(SP + (60: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[245,16].6203A7B3 byte 27
        *(SP + (61: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[245,16].6203A7B3 byte 28
        *(SP + (62: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[245,16].6203A7B3 byte 29
        unalloc $new@[245,16].6203A7B3: MS[anvil.PrinterIndex.I16, U8] [@33, 30]
        goto .59
        */


        val __tmp_160 = (SP + 33.U(8.W))
        val __tmp_161 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_160 + 0.U) := __tmp_161(7, 0)

        val __tmp_162 = (SP + 34.U(8.W))
        val __tmp_163 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_162 + 0.U) := __tmp_163(7, 0)

        val __tmp_164 = (SP + 35.U(8.W))
        val __tmp_165 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_164 + 0.U) := __tmp_165(7, 0)

        val __tmp_166 = (SP + 36.U(8.W))
        val __tmp_167 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_166 + 0.U) := __tmp_167(7, 0)

        val __tmp_168 = (SP + 37.U(8.W))
        val __tmp_169 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_168 + 0.U) := __tmp_169(7, 0)

        val __tmp_170 = (SP + 38.U(8.W))
        val __tmp_171 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_170 + 0.U) := __tmp_171(7, 0)

        val __tmp_172 = (SP + 39.U(8.W))
        val __tmp_173 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_172 + 0.U) := __tmp_173(7, 0)

        val __tmp_174 = (SP + 40.U(8.W))
        val __tmp_175 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_174 + 0.U) := __tmp_175(7, 0)

        val __tmp_176 = (SP + 41.U(8.W))
        val __tmp_177 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_176 + 0.U) := __tmp_177(7, 0)

        val __tmp_178 = (SP + 42.U(8.W))
        val __tmp_179 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_178 + 0.U) := __tmp_179(7, 0)

        val __tmp_180 = (SP + 43.U(8.W))
        val __tmp_181 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_180 + 0.U) := __tmp_181(7, 0)

        val __tmp_182 = (SP + 44.U(8.W))
        val __tmp_183 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_182 + 0.U) := __tmp_183(7, 0)

        val __tmp_184 = (SP + 45.U(8.W))
        val __tmp_185 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_184 + 0.U) := __tmp_185(7, 0)

        val __tmp_186 = (SP + 46.U(8.W))
        val __tmp_187 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_186 + 0.U) := __tmp_187(7, 0)

        val __tmp_188 = (SP + 47.U(8.W))
        val __tmp_189 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_188 + 0.U) := __tmp_189(7, 0)

        val __tmp_190 = (SP + 48.U(8.W))
        val __tmp_191 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_190 + 0.U) := __tmp_191(7, 0)

        val __tmp_192 = (SP + 49.U(8.W))
        val __tmp_193 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_192 + 0.U) := __tmp_193(7, 0)

        val __tmp_194 = (SP + 50.U(8.W))
        val __tmp_195 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_194 + 0.U) := __tmp_195(7, 0)

        val __tmp_196 = (SP + 51.U(8.W))
        val __tmp_197 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_196 + 0.U) := __tmp_197(7, 0)

        val __tmp_198 = (SP + 52.U(8.W))
        val __tmp_199 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_198 + 0.U) := __tmp_199(7, 0)

        val __tmp_200 = (SP + 53.U(8.W))
        val __tmp_201 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_200 + 0.U) := __tmp_201(7, 0)

        val __tmp_202 = (SP + 54.U(8.W))
        val __tmp_203 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_202 + 0.U) := __tmp_203(7, 0)

        val __tmp_204 = (SP + 55.U(8.W))
        val __tmp_205 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_204 + 0.U) := __tmp_205(7, 0)

        val __tmp_206 = (SP + 56.U(8.W))
        val __tmp_207 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_206 + 0.U) := __tmp_207(7, 0)

        val __tmp_208 = (SP + 57.U(8.W))
        val __tmp_209 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_208 + 0.U) := __tmp_209(7, 0)

        val __tmp_210 = (SP + 58.U(8.W))
        val __tmp_211 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_210 + 0.U) := __tmp_211(7, 0)

        val __tmp_212 = (SP + 59.U(8.W))
        val __tmp_213 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_212 + 0.U) := __tmp_213(7, 0)

        val __tmp_214 = (SP + 60.U(8.W))
        val __tmp_215 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_214 + 0.U) := __tmp_215(7, 0)

        val __tmp_216 = (SP + 61.U(8.W))
        val __tmp_217 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_216 + 0.U) := __tmp_217(7, 0)

        val __tmp_218 = (SP + 62.U(8.W))
        val __tmp_219 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_218 + 0.U) := __tmp_219(7, 0)

        CP := 59.U
      }

      is(147.U) {
        /*
        undecl i: anvil.PrinterIndex.I16 @$5
        goto .127
        */


        CP := 127.U
      }

    }

}


