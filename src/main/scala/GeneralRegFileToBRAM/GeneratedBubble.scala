package GeneralRegFileToBRAM
import chisel3._
import chisel3.util._
import chisel3.experimental._



class BubbleTest (val C_S_AXI_DATA_WIDTH:  Int = 32,
               val C_S_AXI_ADDR_WIDTH:  Int = 32,
               val ARRAY_REG_WIDTH:     Int = 8,
               val ARRAY_REG_DEPTH:     Int = 192,
               val GENERAL_REG_WIDTH:   Int = 64,
               val GENERAL_REG_DEPTH:   Int = 95,
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
        *(10: U32) = (886747591: U32) [unsigned, U32, 4]  // $display.$type (MS[anvil.PrinterIndex.U, U8]: 0x34DAB1C7)
        *(14: SP) = (16: Z) [signed, Z, 8]  // $display.size
        *(36: CP) = (0: CP) [unsigned, CP, 2]  // $ret
        goto .4
        */


        SP := 36.U(16.W)

        DP := 0.U(64.W)

        val __tmp_1093 = 10.U(32.W)
        val __tmp_1094 = (886747591.U(32.W)).asUInt
        arrayRegFiles(__tmp_1093 + 0.U) := __tmp_1094(7, 0)
        arrayRegFiles(__tmp_1093 + 1.U) := __tmp_1094(15, 8)
        arrayRegFiles(__tmp_1093 + 2.U) := __tmp_1094(23, 16)
        arrayRegFiles(__tmp_1093 + 3.U) := __tmp_1094(31, 24)

        val __tmp_1095 = 14.U(16.W)
        val __tmp_1096 = (16.S(64.W)).asUInt
        arrayRegFiles(__tmp_1095 + 0.U) := __tmp_1096(7, 0)
        arrayRegFiles(__tmp_1095 + 1.U) := __tmp_1096(15, 8)
        arrayRegFiles(__tmp_1095 + 2.U) := __tmp_1096(23, 16)
        arrayRegFiles(__tmp_1095 + 3.U) := __tmp_1096(31, 24)
        arrayRegFiles(__tmp_1095 + 4.U) := __tmp_1096(39, 32)
        arrayRegFiles(__tmp_1095 + 5.U) := __tmp_1096(47, 40)
        arrayRegFiles(__tmp_1095 + 6.U) := __tmp_1096(55, 48)
        arrayRegFiles(__tmp_1095 + 7.U) := __tmp_1096(63, 56)

        val __tmp_1097 = 36.U(16.W)
        val __tmp_1098 = (0.U(16.W)).asUInt
        arrayRegFiles(__tmp_1097 + 0.U) := __tmp_1098(7, 0)
        arrayRegFiles(__tmp_1097 + 1.U) := __tmp_1098(15, 8)

        CP := 4.U
      }

      is(4.U) {
        /*
        $0 = *(0: SP) [signed, Z, 8]  // $0 = $testNum
        goto .5
        */


        val __tmp_1099 = (0.U(16.W)).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_1099 + 7.U),
          arrayRegFiles(__tmp_1099 + 6.U),
          arrayRegFiles(__tmp_1099 + 5.U),
          arrayRegFiles(__tmp_1099 + 4.U),
          arrayRegFiles(__tmp_1099 + 3.U),
          arrayRegFiles(__tmp_1099 + 2.U),
          arrayRegFiles(__tmp_1099 + 1.U),
          arrayRegFiles(__tmp_1099 + 0.U)
        ).asSInt.pad(GENERAL_REG_WIDTH).asUInt

        CP := 5.U
      }

      is(5.U) {
        /*
        if ((($0: Z) < (0: Z)) | (($0: Z) ≡ (0: Z))) goto .6 else goto .11
        */


        CP := Mux((((generalRegFiles(0.U).asSInt < 0.S(64.W)).asUInt | (generalRegFiles(0.U).asSInt === 0.S(64.W)).asUInt).asUInt) === 1.U, 6.U, 11.U)
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
        *SP = (9: CP) [unsigned, CP, 2]  // $ret@0 = 1434
        *(SP - (8: SP)) = ($0: Z) [signed, Z, 8]  // save $0 (Z)
        goto .8
        */


        val __tmp_1100 = SP
        val __tmp_1101 = (9.U(16.W)).asUInt
        arrayRegFiles(__tmp_1100 + 0.U) := __tmp_1101(7, 0)
        arrayRegFiles(__tmp_1100 + 1.U) := __tmp_1101(15, 8)

        val __tmp_1102 = (SP - 8.U(16.W))
        val __tmp_1103 = (generalRegFiles(0.U).asSInt).asUInt
        arrayRegFiles(__tmp_1102 + 0.U) := __tmp_1103(7, 0)
        arrayRegFiles(__tmp_1102 + 1.U) := __tmp_1103(15, 8)
        arrayRegFiles(__tmp_1102 + 2.U) := __tmp_1103(23, 16)
        arrayRegFiles(__tmp_1102 + 3.U) := __tmp_1103(31, 24)
        arrayRegFiles(__tmp_1102 + 4.U) := __tmp_1103(39, 32)
        arrayRegFiles(__tmp_1102 + 5.U) := __tmp_1103(47, 40)
        arrayRegFiles(__tmp_1102 + 6.U) := __tmp_1103(55, 48)
        arrayRegFiles(__tmp_1102 + 7.U) := __tmp_1103(63, 56)

        CP := 8.U
      }

      is(8.U) {
        /*
        decl $ret: CP [@0, 2]
        goto .18
        */


        CP := 18.U
      }

      is(9.U) {
        /*
        $0 = *(SP - (8: SP)) [signed, Z, 8]  // restore $0 (Z)
        undecl $ret: CP [@0, 2]
        goto .10
        */


        val __tmp_1104 = ((SP - 8.U(16.W))).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_1104 + 7.U),
          arrayRegFiles(__tmp_1104 + 6.U),
          arrayRegFiles(__tmp_1104 + 5.U),
          arrayRegFiles(__tmp_1104 + 4.U),
          arrayRegFiles(__tmp_1104 + 3.U),
          arrayRegFiles(__tmp_1104 + 2.U),
          arrayRegFiles(__tmp_1104 + 1.U),
          arrayRegFiles(__tmp_1104 + 0.U)
        ).asSInt.pad(GENERAL_REG_WIDTH).asUInt

        CP := 10.U
      }

      is(10.U) {
        /*
        SP = SP - 10
        goto .11
        */


        SP := SP - 10.U

        CP := 11.U
      }

      is(11.U) {
        /*
        if ((($0: Z) < (0: Z)) | (($0: Z) ≡ (1: Z))) goto .12 else goto .17
        */


        CP := Mux((((generalRegFiles(0.U).asSInt < 0.S(64.W)).asUInt | (generalRegFiles(0.U).asSInt === 1.S(64.W)).asUInt).asUInt) === 1.U, 12.U, 17.U)
      }

      is(12.U) {
        /*
        SP = SP + 2
        goto .13
        */


        SP := SP + 2.U

        CP := 13.U
      }

      is(13.U) {
        /*
        *SP = (15: CP) [unsigned, CP, 2]  // $ret@0 = 1436
        goto .14
        */


        val __tmp_1105 = SP
        val __tmp_1106 = (15.U(16.W)).asUInt
        arrayRegFiles(__tmp_1105 + 0.U) := __tmp_1106(7, 0)
        arrayRegFiles(__tmp_1105 + 1.U) := __tmp_1106(15, 8)

        CP := 14.U
      }

      is(14.U) {
        /*
        decl $ret: CP [@0, 2]
        goto .35
        */


        CP := 35.U
      }

      is(15.U) {
        /*
        undecl $ret: CP [@0, 2]
        goto .16
        */


        CP := 16.U
      }

      is(16.U) {
        /*
        SP = SP - 2
        goto .17
        */


        SP := SP - 2.U

        CP := 17.U
      }

      is(17.U) {
        /*
        goto $ret@0
        */


        CP := Cat(
          arrayRegFiles(SP + 0.U + 1.U),
          arrayRegFiles(SP + 0.U + 0.U)
        )

      }

      is(18.U) {
        /*
        decl a: MS[Z, S16] [@2, 22]
        alloc $new@[41,11].C44C2B63: MS[Z, S16] [@24, 22]
        $0 = (SP + (24: SP))
        *(SP + (24: SP)) = (2583127857: U32) [unsigned, U32, 4]  // sha3 type signature of MS[Z, S16]: 0x99F76731
        *(SP + (28: SP)) = (1: Z) [signed, Z, 8]  // size of MS[Z, S16]((-4: S16))
        goto .19
        */



        generalRegFiles(0.U) := (SP + 24.U(16.W))

        val __tmp_1107 = (SP + 24.U(16.W))
        val __tmp_1108 = (BigInt("2583127857").U(32.W)).asUInt
        arrayRegFiles(__tmp_1107 + 0.U) := __tmp_1108(7, 0)
        arrayRegFiles(__tmp_1107 + 1.U) := __tmp_1108(15, 8)
        arrayRegFiles(__tmp_1107 + 2.U) := __tmp_1108(23, 16)
        arrayRegFiles(__tmp_1107 + 3.U) := __tmp_1108(31, 24)

        val __tmp_1109 = (SP + 28.U(16.W))
        val __tmp_1110 = (1.S(64.W)).asUInt
        arrayRegFiles(__tmp_1109 + 0.U) := __tmp_1110(7, 0)
        arrayRegFiles(__tmp_1109 + 1.U) := __tmp_1110(15, 8)
        arrayRegFiles(__tmp_1109 + 2.U) := __tmp_1110(23, 16)
        arrayRegFiles(__tmp_1109 + 3.U) := __tmp_1110(31, 24)
        arrayRegFiles(__tmp_1109 + 4.U) := __tmp_1110(39, 32)
        arrayRegFiles(__tmp_1109 + 5.U) := __tmp_1110(47, 40)
        arrayRegFiles(__tmp_1109 + 6.U) := __tmp_1110(55, 48)
        arrayRegFiles(__tmp_1109 + 7.U) := __tmp_1110(63, 56)

        CP := 19.U
      }

      is(19.U) {
        /*
        *((($0: MS[Z, S16]) + (12: SP)) + (((0: Z) as SP) * (2: SP))) = (-4: S16) [signed, S16, 2]  // ($0: MS[Z, S16])((0: Z)) = (-4: S16)
        goto .20
        */


        val __tmp_1111 = ((generalRegFiles(0.U) + 12.U(16.W)) + (0.S(64.W).asUInt * 2.U(16.W)))
        val __tmp_1112 = (-4.S(16.W)).asUInt
        arrayRegFiles(__tmp_1111 + 0.U) := __tmp_1112(7, 0)
        arrayRegFiles(__tmp_1111 + 1.U) := __tmp_1112(15, 8)

        CP := 20.U
      }

      is(20.U) {
        /*
        (SP + (2: SP)) [MS[Z, S16], 22]  <-  ($0: MS[Z, S16]) [MS[Z, S16], (((*(($0: MS[Z, S16]) + (4: SP)) as SP) * (2: SP)) + (12: SP))]  // a = ($0: MS[Z, S16])
        goto .21
        */


        val __tmp_1113 = (SP + 2.U(16.W))
        val __tmp_1114 = generalRegFiles(0.U)
        val __tmp_1115 = ((Cat(
            arrayRegFiles((generalRegFiles(0.U) + 4.U(16.W)) + 7.U),
            arrayRegFiles((generalRegFiles(0.U) + 4.U(16.W)) + 6.U),
            arrayRegFiles((generalRegFiles(0.U) + 4.U(16.W)) + 5.U),
            arrayRegFiles((generalRegFiles(0.U) + 4.U(16.W)) + 4.U),
            arrayRegFiles((generalRegFiles(0.U) + 4.U(16.W)) + 3.U),
            arrayRegFiles((generalRegFiles(0.U) + 4.U(16.W)) + 2.U),
            arrayRegFiles((generalRegFiles(0.U) + 4.U(16.W)) + 1.U),
            arrayRegFiles((generalRegFiles(0.U) + 4.U(16.W)) + 0.U)
          ).asSInt.asUInt * 2.U(16.W)) + 12.U(16.W))

        when(Idx < __tmp_1115) {
          arrayRegFiles(__tmp_1113 + Idx + 0.U) := arrayRegFiles(__tmp_1114 + Idx + 0.U)
          arrayRegFiles(__tmp_1113 + Idx + 1.U) := arrayRegFiles(__tmp_1114 + Idx + 1.U)
          arrayRegFiles(__tmp_1113 + Idx + 2.U) := arrayRegFiles(__tmp_1114 + Idx + 2.U)
          arrayRegFiles(__tmp_1113 + Idx + 3.U) := arrayRegFiles(__tmp_1114 + Idx + 3.U)
          arrayRegFiles(__tmp_1113 + Idx + 4.U) := arrayRegFiles(__tmp_1114 + Idx + 4.U)
          arrayRegFiles(__tmp_1113 + Idx + 5.U) := arrayRegFiles(__tmp_1114 + Idx + 5.U)
          arrayRegFiles(__tmp_1113 + Idx + 6.U) := arrayRegFiles(__tmp_1114 + Idx + 6.U)
          arrayRegFiles(__tmp_1113 + Idx + 7.U) := arrayRegFiles(__tmp_1114 + Idx + 7.U)
          Idx := Idx + 8.U
          LeftByteRounds := __tmp_1115 - Idx
        } .elsewhen(IdxLeftByteRounds < LeftByteRounds) {
          val __tmp_1116 = Idx - 8.U
          arrayRegFiles(__tmp_1113 + __tmp_1116 + IdxLeftByteRounds) := arrayRegFiles(__tmp_1114 + __tmp_1116 + IdxLeftByteRounds)
          IdxLeftByteRounds := IdxLeftByteRounds + 1.U
        } .otherwise {
          Idx := 0.U
          IdxLeftByteRounds := 0.U
          LeftByteRounds := 0.U
          CP := 21.U
        }


      }

      is(21.U) {
        /*
        unalloc $new@[41,11].C44C2B63: MS[Z, S16] [@24, 22]
        goto .22
        */


        CP := 22.U
      }

      is(22.U) {
        /*
        $0 = (SP + (2: SP))
        goto .23
        */



        generalRegFiles(0.U) := (SP + 2.U(16.W))

        CP := 23.U
      }

      is(23.U) {
        /*
        SP = SP + 46
        goto .24
        */


        SP := SP + 46.U

        CP := 24.U
      }

      is(24.U) {
        /*
        *SP = (26: CP) [unsigned, CP, 2]  // $ret@0 = 1438
        $35 = ($0: MS[Z, S16])
        goto .25
        */


        val __tmp_1117 = SP
        val __tmp_1118 = (26.U(16.W)).asUInt
        arrayRegFiles(__tmp_1117 + 0.U) := __tmp_1118(7, 0)
        arrayRegFiles(__tmp_1117 + 1.U) := __tmp_1118(15, 8)


        generalRegFiles(35.U) := generalRegFiles(0.U)

        CP := 25.U
      }

      is(25.U) {
        /*
        decl $ret: CP [@0, 2], a: MS[Z, S16] @$0, a: MS[Z, S16] [@2, 22]
        $0 = ($35: MS[Z, S16])
        goto .52
        */



        generalRegFiles(0.U) := generalRegFiles(35.U)

        CP := 52.U
      }

      is(26.U) {
        /*
        undecl a: MS[Z, S16] [@2, 22], a: MS[Z, S16] @$0, $ret: CP [@0, 2]
        goto .27
        */


        CP := 27.U
      }

      is(27.U) {
        /*
        SP = SP - 46
        goto .28
        */


        SP := SP - 46.U

        CP := 28.U
      }

      is(28.U) {
        /*
        $0 = (SP + (2: SP))
        goto .29
        */



        generalRegFiles(0.U) := (SP + 2.U(16.W))

        CP := 29.U
      }

      is(29.U) {
        /*
        SP = SP + 46
        goto .30
        */


        SP := SP + 46.U

        CP := 30.U
      }

      is(30.U) {
        /*
        *SP = (32: CP) [unsigned, CP, 2]  // $ret@0 = 1440
        $39 = ($0: MS[Z, S16])
        goto .31
        */


        val __tmp_1119 = SP
        val __tmp_1120 = (32.U(16.W)).asUInt
        arrayRegFiles(__tmp_1119 + 0.U) := __tmp_1120(7, 0)
        arrayRegFiles(__tmp_1119 + 1.U) := __tmp_1120(15, 8)


        generalRegFiles(39.U) := generalRegFiles(0.U)

        CP := 31.U
      }

      is(31.U) {
        /*
        decl $ret: CP [@0, 2], a: MS[Z, S16] @$0
        $0 = ($39: MS[Z, S16])
        goto .88
        */



        generalRegFiles(0.U) := generalRegFiles(39.U)

        CP := 88.U
      }

      is(32.U) {
        /*
        undecl a: MS[Z, S16] @$0, $ret: CP [@0, 2]
        goto .33
        */


        CP := 33.U
      }

      is(33.U) {
        /*
        SP = SP - 46
        goto .34
        */


        SP := SP - 46.U

        CP := 34.U
      }

      is(34.U) {
        /*
        undecl a: MS[Z, S16] [@2, 22]
        goto $ret@0
        */


        CP := Cat(
          arrayRegFiles(SP + 0.U + 1.U),
          arrayRegFiles(SP + 0.U + 0.U)
        )

      }

      is(35.U) {
        /*
        decl a: MS[Z, S16] [@2, 22]
        alloc $new@[47,11].E4931FFD: MS[Z, S16] [@24, 22]
        $0 = (SP + (24: SP))
        *(SP + (24: SP)) = (2583127857: U32) [unsigned, U32, 4]  // sha3 type signature of MS[Z, S16]: 0x99F76731
        *(SP + (28: SP)) = (3: Z) [signed, Z, 8]  // size of MS[Z, S16]((1: S16), (-2: S16), (3: S16))
        goto .36
        */



        generalRegFiles(0.U) := (SP + 24.U(16.W))

        val __tmp_1121 = (SP + 24.U(16.W))
        val __tmp_1122 = (BigInt("2583127857").U(32.W)).asUInt
        arrayRegFiles(__tmp_1121 + 0.U) := __tmp_1122(7, 0)
        arrayRegFiles(__tmp_1121 + 1.U) := __tmp_1122(15, 8)
        arrayRegFiles(__tmp_1121 + 2.U) := __tmp_1122(23, 16)
        arrayRegFiles(__tmp_1121 + 3.U) := __tmp_1122(31, 24)

        val __tmp_1123 = (SP + 28.U(16.W))
        val __tmp_1124 = (3.S(64.W)).asUInt
        arrayRegFiles(__tmp_1123 + 0.U) := __tmp_1124(7, 0)
        arrayRegFiles(__tmp_1123 + 1.U) := __tmp_1124(15, 8)
        arrayRegFiles(__tmp_1123 + 2.U) := __tmp_1124(23, 16)
        arrayRegFiles(__tmp_1123 + 3.U) := __tmp_1124(31, 24)
        arrayRegFiles(__tmp_1123 + 4.U) := __tmp_1124(39, 32)
        arrayRegFiles(__tmp_1123 + 5.U) := __tmp_1124(47, 40)
        arrayRegFiles(__tmp_1123 + 6.U) := __tmp_1124(55, 48)
        arrayRegFiles(__tmp_1123 + 7.U) := __tmp_1124(63, 56)

        CP := 36.U
      }

      is(36.U) {
        /*
        *((($0: MS[Z, S16]) + (12: SP)) + (((0: Z) as SP) * (2: SP))) = (1: S16) [signed, S16, 2]  // ($0: MS[Z, S16])((0: Z)) = (1: S16)
        *((($0: MS[Z, S16]) + (12: SP)) + (((1: Z) as SP) * (2: SP))) = (-2: S16) [signed, S16, 2]  // ($0: MS[Z, S16])((1: Z)) = (-2: S16)
        *((($0: MS[Z, S16]) + (12: SP)) + (((2: Z) as SP) * (2: SP))) = (3: S16) [signed, S16, 2]  // ($0: MS[Z, S16])((2: Z)) = (3: S16)
        goto .37
        */


        val __tmp_1125 = ((generalRegFiles(0.U) + 12.U(16.W)) + (0.S(64.W).asUInt * 2.U(16.W)))
        val __tmp_1126 = (1.S(16.W)).asUInt
        arrayRegFiles(__tmp_1125 + 0.U) := __tmp_1126(7, 0)
        arrayRegFiles(__tmp_1125 + 1.U) := __tmp_1126(15, 8)

        val __tmp_1127 = ((generalRegFiles(0.U) + 12.U(16.W)) + (1.S(64.W).asUInt * 2.U(16.W)))
        val __tmp_1128 = (-2.S(16.W)).asUInt
        arrayRegFiles(__tmp_1127 + 0.U) := __tmp_1128(7, 0)
        arrayRegFiles(__tmp_1127 + 1.U) := __tmp_1128(15, 8)

        val __tmp_1129 = ((generalRegFiles(0.U) + 12.U(16.W)) + (2.S(64.W).asUInt * 2.U(16.W)))
        val __tmp_1130 = (3.S(16.W)).asUInt
        arrayRegFiles(__tmp_1129 + 0.U) := __tmp_1130(7, 0)
        arrayRegFiles(__tmp_1129 + 1.U) := __tmp_1130(15, 8)

        CP := 37.U
      }

      is(37.U) {
        /*
        (SP + (2: SP)) [MS[Z, S16], 22]  <-  ($0: MS[Z, S16]) [MS[Z, S16], (((*(($0: MS[Z, S16]) + (4: SP)) as SP) * (2: SP)) + (12: SP))]  // a = ($0: MS[Z, S16])
        goto .38
        */


        val __tmp_1131 = (SP + 2.U(16.W))
        val __tmp_1132 = generalRegFiles(0.U)
        val __tmp_1133 = ((Cat(
            arrayRegFiles((generalRegFiles(0.U) + 4.U(16.W)) + 7.U),
            arrayRegFiles((generalRegFiles(0.U) + 4.U(16.W)) + 6.U),
            arrayRegFiles((generalRegFiles(0.U) + 4.U(16.W)) + 5.U),
            arrayRegFiles((generalRegFiles(0.U) + 4.U(16.W)) + 4.U),
            arrayRegFiles((generalRegFiles(0.U) + 4.U(16.W)) + 3.U),
            arrayRegFiles((generalRegFiles(0.U) + 4.U(16.W)) + 2.U),
            arrayRegFiles((generalRegFiles(0.U) + 4.U(16.W)) + 1.U),
            arrayRegFiles((generalRegFiles(0.U) + 4.U(16.W)) + 0.U)
          ).asSInt.asUInt * 2.U(16.W)) + 12.U(16.W))

        when(Idx < __tmp_1133) {
          arrayRegFiles(__tmp_1131 + Idx + 0.U) := arrayRegFiles(__tmp_1132 + Idx + 0.U)
          arrayRegFiles(__tmp_1131 + Idx + 1.U) := arrayRegFiles(__tmp_1132 + Idx + 1.U)
          arrayRegFiles(__tmp_1131 + Idx + 2.U) := arrayRegFiles(__tmp_1132 + Idx + 2.U)
          arrayRegFiles(__tmp_1131 + Idx + 3.U) := arrayRegFiles(__tmp_1132 + Idx + 3.U)
          arrayRegFiles(__tmp_1131 + Idx + 4.U) := arrayRegFiles(__tmp_1132 + Idx + 4.U)
          arrayRegFiles(__tmp_1131 + Idx + 5.U) := arrayRegFiles(__tmp_1132 + Idx + 5.U)
          arrayRegFiles(__tmp_1131 + Idx + 6.U) := arrayRegFiles(__tmp_1132 + Idx + 6.U)
          arrayRegFiles(__tmp_1131 + Idx + 7.U) := arrayRegFiles(__tmp_1132 + Idx + 7.U)
          Idx := Idx + 8.U
          LeftByteRounds := __tmp_1133 - Idx
        } .elsewhen(IdxLeftByteRounds < LeftByteRounds) {
          val __tmp_1134 = Idx - 8.U
          arrayRegFiles(__tmp_1131 + __tmp_1134 + IdxLeftByteRounds) := arrayRegFiles(__tmp_1132 + __tmp_1134 + IdxLeftByteRounds)
          IdxLeftByteRounds := IdxLeftByteRounds + 1.U
        } .otherwise {
          Idx := 0.U
          IdxLeftByteRounds := 0.U
          LeftByteRounds := 0.U
          CP := 38.U
        }


      }

      is(38.U) {
        /*
        unalloc $new@[47,11].E4931FFD: MS[Z, S16] [@24, 22]
        goto .39
        */


        CP := 39.U
      }

      is(39.U) {
        /*
        $0 = (SP + (2: SP))
        goto .40
        */



        generalRegFiles(0.U) := (SP + 2.U(16.W))

        CP := 40.U
      }

      is(40.U) {
        /*
        SP = SP + 46
        goto .41
        */


        SP := SP + 46.U

        CP := 41.U
      }

      is(41.U) {
        /*
        *SP = (43: CP) [unsigned, CP, 2]  // $ret@0 = 1442
        $35 = ($0: MS[Z, S16])
        goto .42
        */


        val __tmp_1135 = SP
        val __tmp_1136 = (43.U(16.W)).asUInt
        arrayRegFiles(__tmp_1135 + 0.U) := __tmp_1136(7, 0)
        arrayRegFiles(__tmp_1135 + 1.U) := __tmp_1136(15, 8)


        generalRegFiles(35.U) := generalRegFiles(0.U)

        CP := 42.U
      }

      is(42.U) {
        /*
        decl $ret: CP [@0, 2], a: MS[Z, S16] @$0, a: MS[Z, S16] [@2, 22]
        $0 = ($35: MS[Z, S16])
        goto .52
        */



        generalRegFiles(0.U) := generalRegFiles(35.U)

        CP := 52.U
      }

      is(43.U) {
        /*
        undecl a: MS[Z, S16] [@2, 22], a: MS[Z, S16] @$0, $ret: CP [@0, 2]
        goto .44
        */


        CP := 44.U
      }

      is(44.U) {
        /*
        SP = SP - 46
        goto .45
        */


        SP := SP - 46.U

        CP := 45.U
      }

      is(45.U) {
        /*
        $0 = (SP + (2: SP))
        goto .46
        */



        generalRegFiles(0.U) := (SP + 2.U(16.W))

        CP := 46.U
      }

      is(46.U) {
        /*
        SP = SP + 46
        goto .47
        */


        SP := SP + 46.U

        CP := 47.U
      }

      is(47.U) {
        /*
        *SP = (49: CP) [unsigned, CP, 2]  // $ret@0 = 1444
        $39 = ($0: MS[Z, S16])
        goto .48
        */


        val __tmp_1137 = SP
        val __tmp_1138 = (49.U(16.W)).asUInt
        arrayRegFiles(__tmp_1137 + 0.U) := __tmp_1138(7, 0)
        arrayRegFiles(__tmp_1137 + 1.U) := __tmp_1138(15, 8)


        generalRegFiles(39.U) := generalRegFiles(0.U)

        CP := 48.U
      }

      is(48.U) {
        /*
        decl $ret: CP [@0, 2], a: MS[Z, S16] @$0
        $0 = ($39: MS[Z, S16])
        goto .88
        */



        generalRegFiles(0.U) := generalRegFiles(39.U)

        CP := 88.U
      }

      is(49.U) {
        /*
        undecl a: MS[Z, S16] @$0, $ret: CP [@0, 2]
        goto .50
        */


        CP := 50.U
      }

      is(50.U) {
        /*
        SP = SP - 46
        goto .51
        */


        SP := SP - 46.U

        CP := 51.U
      }

      is(51.U) {
        /*
        undecl a: MS[Z, S16] [@2, 22]
        goto $ret@0
        */


        CP := Cat(
          arrayRegFiles(SP + 0.U + 1.U),
          arrayRegFiles(SP + 0.U + 0.U)
        )

      }

      is(52.U) {
        /*
        decl i: Z @$1
        $1 = (0: Z)
        goto .53
        */



        generalRegFiles(1.U) := (0.S(64.W)).asUInt

        CP := 53.U
      }

      is(53.U) {
        /*
        $3 = ($1: Z)
        $4 = ($0: MS[Z, S16])
        goto .54
        */



        generalRegFiles(3.U) := (generalRegFiles(1.U).asSInt).asUInt


        generalRegFiles(4.U) := generalRegFiles(0.U)

        CP := 54.U
      }

      is(54.U) {
        /*
        $5 = *(($4: MS[Z, S16]) + (4: SP)) [signed, Z, 8]  // $5 = ($4: MS[Z, S16]).size
        goto .55
        */


        val __tmp_1139 = ((generalRegFiles(4.U) + 4.U(16.W))).asUInt
        generalRegFiles(5.U) := Cat(
          arrayRegFiles(__tmp_1139 + 7.U),
          arrayRegFiles(__tmp_1139 + 6.U),
          arrayRegFiles(__tmp_1139 + 5.U),
          arrayRegFiles(__tmp_1139 + 4.U),
          arrayRegFiles(__tmp_1139 + 3.U),
          arrayRegFiles(__tmp_1139 + 2.U),
          arrayRegFiles(__tmp_1139 + 1.U),
          arrayRegFiles(__tmp_1139 + 0.U)
        ).asSInt.pad(GENERAL_REG_WIDTH).asUInt

        CP := 55.U
      }

      is(55.U) {
        /*
        $6 = (($3: Z) < ($5: Z))
        goto .56
        */



        generalRegFiles(6.U) := (generalRegFiles(3.U).asSInt < generalRegFiles(5.U).asSInt).asUInt

        CP := 56.U
      }

      is(56.U) {
        /*
        if ($6: B) goto .57 else goto .87
        */


        CP := Mux((generalRegFiles(6.U).asUInt) === 1.U, 57.U, 87.U)
      }

      is(57.U) {
        /*
        decl j: Z @$2
        $3 = ($1: Z)
        goto .58
        */



        generalRegFiles(3.U) := (generalRegFiles(1.U).asSInt).asUInt

        CP := 58.U
      }

      is(58.U) {
        /*
        $4 = (($3: Z) + (1: Z))
        goto .59
        */



        generalRegFiles(4.U) := ((generalRegFiles(3.U).asSInt + 1.S(64.W))).asUInt

        CP := 59.U
      }

      is(59.U) {
        /*
        $2 = ($4: Z)
        goto .60
        */



        generalRegFiles(2.U) := (generalRegFiles(4.U).asSInt).asUInt

        CP := 60.U
      }

      is(60.U) {
        /*
        $3 = ($2: Z)
        $4 = ($0: MS[Z, S16])
        goto .61
        */



        generalRegFiles(3.U) := (generalRegFiles(2.U).asSInt).asUInt


        generalRegFiles(4.U) := generalRegFiles(0.U)

        CP := 61.U
      }

      is(61.U) {
        /*
        $5 = *(($4: MS[Z, S16]) + (4: SP)) [signed, Z, 8]  // $5 = ($4: MS[Z, S16]).size
        goto .62
        */


        val __tmp_1140 = ((generalRegFiles(4.U) + 4.U(16.W))).asUInt
        generalRegFiles(5.U) := Cat(
          arrayRegFiles(__tmp_1140 + 7.U),
          arrayRegFiles(__tmp_1140 + 6.U),
          arrayRegFiles(__tmp_1140 + 5.U),
          arrayRegFiles(__tmp_1140 + 4.U),
          arrayRegFiles(__tmp_1140 + 3.U),
          arrayRegFiles(__tmp_1140 + 2.U),
          arrayRegFiles(__tmp_1140 + 1.U),
          arrayRegFiles(__tmp_1140 + 0.U)
        ).asSInt.pad(GENERAL_REG_WIDTH).asUInt

        CP := 62.U
      }

      is(62.U) {
        /*
        $6 = (($3: Z) < ($5: Z))
        goto .63
        */



        generalRegFiles(6.U) := (generalRegFiles(3.U).asSInt < generalRegFiles(5.U).asSInt).asUInt

        CP := 63.U
      }

      is(63.U) {
        /*
        if ($6: B) goto .64 else goto .84
        */


        CP := Mux((generalRegFiles(6.U).asUInt) === 1.U, 64.U, 84.U)
      }

      is(64.U) {
        /*
        $3 = ($0: MS[Z, S16])
        $4 = ($1: Z)
        goto .65
        */



        generalRegFiles(3.U) := generalRegFiles(0.U)


        generalRegFiles(4.U) := (generalRegFiles(1.U).asSInt).asUInt

        CP := 65.U
      }

      is(65.U) {
        /*
        if (((0: Z) <= ($4: Z)) & (($4: Z) <= *(($3: MS[Z, S16]) + (4: SP)))) goto .69 else goto .66
        */


        CP := Mux((((0.S(64.W) <= generalRegFiles(4.U).asSInt).asUInt & (generalRegFiles(4.U).asSInt <= Cat(
                            arrayRegFiles((generalRegFiles(3.U) + 4.U(16.W)) + 7.U),
                            arrayRegFiles((generalRegFiles(3.U) + 4.U(16.W)) + 6.U),
                            arrayRegFiles((generalRegFiles(3.U) + 4.U(16.W)) + 5.U),
                            arrayRegFiles((generalRegFiles(3.U) + 4.U(16.W)) + 4.U),
                            arrayRegFiles((generalRegFiles(3.U) + 4.U(16.W)) + 3.U),
                            arrayRegFiles((generalRegFiles(3.U) + 4.U(16.W)) + 2.U),
                            arrayRegFiles((generalRegFiles(3.U) + 4.U(16.W)) + 1.U),
                            arrayRegFiles((generalRegFiles(3.U) + 4.U(16.W)) + 0.U)
                          ).asSInt).asUInt).asUInt) === 1.U, 69.U, 66.U)
      }

      is(66.U) {
        /*
        *(((8: SP) + (12: SP)) + ((DP & (15: DP)) as SP)) = (73: U8) [unsigned, U8, 1]  // $display((DP & (15: DP))) = (73: U8)
        *(((8: SP) + (12: SP)) + (((DP + (1: DP)) & (15: DP)) as SP)) = (110: U8) [unsigned, U8, 1]  // $display(((DP + (1: DP)) & (15: DP))) = (110: U8)
        *(((8: SP) + (12: SP)) + (((DP + (2: DP)) & (15: DP)) as SP)) = (100: U8) [unsigned, U8, 1]  // $display(((DP + (2: DP)) & (15: DP))) = (100: U8)
        *(((8: SP) + (12: SP)) + (((DP + (3: DP)) & (15: DP)) as SP)) = (101: U8) [unsigned, U8, 1]  // $display(((DP + (3: DP)) & (15: DP))) = (101: U8)
        *(((8: SP) + (12: SP)) + (((DP + (4: DP)) & (15: DP)) as SP)) = (120: U8) [unsigned, U8, 1]  // $display(((DP + (4: DP)) & (15: DP))) = (120: U8)
        *(((8: SP) + (12: SP)) + (((DP + (5: DP)) & (15: DP)) as SP)) = (32: U8) [unsigned, U8, 1]  // $display(((DP + (5: DP)) & (15: DP))) = (32: U8)
        *(((8: SP) + (12: SP)) + (((DP + (6: DP)) & (15: DP)) as SP)) = (111: U8) [unsigned, U8, 1]  // $display(((DP + (6: DP)) & (15: DP))) = (111: U8)
        *(((8: SP) + (12: SP)) + (((DP + (7: DP)) & (15: DP)) as SP)) = (117: U8) [unsigned, U8, 1]  // $display(((DP + (7: DP)) & (15: DP))) = (117: U8)
        *(((8: SP) + (12: SP)) + (((DP + (8: DP)) & (15: DP)) as SP)) = (116: U8) [unsigned, U8, 1]  // $display(((DP + (8: DP)) & (15: DP))) = (116: U8)
        *(((8: SP) + (12: SP)) + (((DP + (9: DP)) & (15: DP)) as SP)) = (32: U8) [unsigned, U8, 1]  // $display(((DP + (9: DP)) & (15: DP))) = (32: U8)
        *(((8: SP) + (12: SP)) + (((DP + (10: DP)) & (15: DP)) as SP)) = (111: U8) [unsigned, U8, 1]  // $display(((DP + (10: DP)) & (15: DP))) = (111: U8)
        *(((8: SP) + (12: SP)) + (((DP + (11: DP)) & (15: DP)) as SP)) = (102: U8) [unsigned, U8, 1]  // $display(((DP + (11: DP)) & (15: DP))) = (102: U8)
        *(((8: SP) + (12: SP)) + (((DP + (12: DP)) & (15: DP)) as SP)) = (32: U8) [unsigned, U8, 1]  // $display(((DP + (12: DP)) & (15: DP))) = (32: U8)
        *(((8: SP) + (12: SP)) + (((DP + (13: DP)) & (15: DP)) as SP)) = (98: U8) [unsigned, U8, 1]  // $display(((DP + (13: DP)) & (15: DP))) = (98: U8)
        *(((8: SP) + (12: SP)) + (((DP + (14: DP)) & (15: DP)) as SP)) = (111: U8) [unsigned, U8, 1]  // $display(((DP + (14: DP)) & (15: DP))) = (111: U8)
        *(((8: SP) + (12: SP)) + (((DP + (15: DP)) & (15: DP)) as SP)) = (117: U8) [unsigned, U8, 1]  // $display(((DP + (15: DP)) & (15: DP))) = (117: U8)
        *(((8: SP) + (12: SP)) + (((DP + (16: DP)) & (15: DP)) as SP)) = (110: U8) [unsigned, U8, 1]  // $display(((DP + (16: DP)) & (15: DP))) = (110: U8)
        *(((8: SP) + (12: SP)) + (((DP + (17: DP)) & (15: DP)) as SP)) = (100: U8) [unsigned, U8, 1]  // $display(((DP + (17: DP)) & (15: DP))) = (100: U8)
        *(((8: SP) + (12: SP)) + (((DP + (18: DP)) & (15: DP)) as SP)) = (115: U8) [unsigned, U8, 1]  // $display(((DP + (18: DP)) & (15: DP))) = (115: U8)
        goto .120
        */


        val __tmp_1141 = ((8.U(16.W) + 12.U(16.W)) + (DP & 15.U(64.W)).asUInt)
        val __tmp_1142 = (73.U(8.W)).asUInt
        arrayRegFiles(__tmp_1141 + 0.U) := __tmp_1142(7, 0)

        val __tmp_1143 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 1.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1144 = (110.U(8.W)).asUInt
        arrayRegFiles(__tmp_1143 + 0.U) := __tmp_1144(7, 0)

        val __tmp_1145 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 2.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1146 = (100.U(8.W)).asUInt
        arrayRegFiles(__tmp_1145 + 0.U) := __tmp_1146(7, 0)

        val __tmp_1147 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 3.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1148 = (101.U(8.W)).asUInt
        arrayRegFiles(__tmp_1147 + 0.U) := __tmp_1148(7, 0)

        val __tmp_1149 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 4.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1150 = (120.U(8.W)).asUInt
        arrayRegFiles(__tmp_1149 + 0.U) := __tmp_1150(7, 0)

        val __tmp_1151 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 5.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1152 = (32.U(8.W)).asUInt
        arrayRegFiles(__tmp_1151 + 0.U) := __tmp_1152(7, 0)

        val __tmp_1153 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 6.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1154 = (111.U(8.W)).asUInt
        arrayRegFiles(__tmp_1153 + 0.U) := __tmp_1154(7, 0)

        val __tmp_1155 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 7.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1156 = (117.U(8.W)).asUInt
        arrayRegFiles(__tmp_1155 + 0.U) := __tmp_1156(7, 0)

        val __tmp_1157 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 8.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1158 = (116.U(8.W)).asUInt
        arrayRegFiles(__tmp_1157 + 0.U) := __tmp_1158(7, 0)

        val __tmp_1159 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 9.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1160 = (32.U(8.W)).asUInt
        arrayRegFiles(__tmp_1159 + 0.U) := __tmp_1160(7, 0)

        val __tmp_1161 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 10.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1162 = (111.U(8.W)).asUInt
        arrayRegFiles(__tmp_1161 + 0.U) := __tmp_1162(7, 0)

        val __tmp_1163 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 11.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1164 = (102.U(8.W)).asUInt
        arrayRegFiles(__tmp_1163 + 0.U) := __tmp_1164(7, 0)

        val __tmp_1165 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 12.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1166 = (32.U(8.W)).asUInt
        arrayRegFiles(__tmp_1165 + 0.U) := __tmp_1166(7, 0)

        val __tmp_1167 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 13.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1168 = (98.U(8.W)).asUInt
        arrayRegFiles(__tmp_1167 + 0.U) := __tmp_1168(7, 0)

        val __tmp_1169 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 14.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1170 = (111.U(8.W)).asUInt
        arrayRegFiles(__tmp_1169 + 0.U) := __tmp_1170(7, 0)

        val __tmp_1171 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 15.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1172 = (117.U(8.W)).asUInt
        arrayRegFiles(__tmp_1171 + 0.U) := __tmp_1172(7, 0)

        val __tmp_1173 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 16.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1174 = (110.U(8.W)).asUInt
        arrayRegFiles(__tmp_1173 + 0.U) := __tmp_1174(7, 0)

        val __tmp_1175 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 17.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1176 = (100.U(8.W)).asUInt
        arrayRegFiles(__tmp_1175 + 0.U) := __tmp_1176(7, 0)

        val __tmp_1177 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 18.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1178 = (115.U(8.W)).asUInt
        arrayRegFiles(__tmp_1177 + 0.U) := __tmp_1178(7, 0)

        CP := 120.U
      }

      is(69.U) {
        /*
        $5 = *((($3: MS[Z, S16]) + (12: SP)) + ((($4: Z) as SP) * (2: SP))) [signed, S16, 2]  // $5 = ($3: MS[Z, S16])(($4: Z))
        goto .70
        */


        val __tmp_1179 = (((generalRegFiles(3.U) + 12.U(16.W)) + (generalRegFiles(4.U).asSInt.asUInt * 2.U(16.W)))).asUInt
        generalRegFiles(5.U) := Cat(
          arrayRegFiles(__tmp_1179 + 1.U),
          arrayRegFiles(__tmp_1179 + 0.U)
        ).asSInt.pad(GENERAL_REG_WIDTH).asUInt

        CP := 70.U
      }

      is(70.U) {
        /*
        $6 = ($0: MS[Z, S16])
        $7 = ($2: Z)
        goto .71
        */



        generalRegFiles(6.U) := generalRegFiles(0.U)


        generalRegFiles(7.U) := (generalRegFiles(2.U).asSInt).asUInt

        CP := 71.U
      }

      is(71.U) {
        /*
        if (((0: Z) <= ($7: Z)) & (($7: Z) <= *(($6: MS[Z, S16]) + (4: SP)))) goto .75 else goto .72
        */


        CP := Mux((((0.S(64.W) <= generalRegFiles(7.U).asSInt).asUInt & (generalRegFiles(7.U).asSInt <= Cat(
                            arrayRegFiles((generalRegFiles(6.U) + 4.U(16.W)) + 7.U),
                            arrayRegFiles((generalRegFiles(6.U) + 4.U(16.W)) + 6.U),
                            arrayRegFiles((generalRegFiles(6.U) + 4.U(16.W)) + 5.U),
                            arrayRegFiles((generalRegFiles(6.U) + 4.U(16.W)) + 4.U),
                            arrayRegFiles((generalRegFiles(6.U) + 4.U(16.W)) + 3.U),
                            arrayRegFiles((generalRegFiles(6.U) + 4.U(16.W)) + 2.U),
                            arrayRegFiles((generalRegFiles(6.U) + 4.U(16.W)) + 1.U),
                            arrayRegFiles((generalRegFiles(6.U) + 4.U(16.W)) + 0.U)
                          ).asSInt).asUInt).asUInt) === 1.U, 75.U, 72.U)
      }

      is(72.U) {
        /*
        *(((8: SP) + (12: SP)) + ((DP & (15: DP)) as SP)) = (73: U8) [unsigned, U8, 1]  // $display((DP & (15: DP))) = (73: U8)
        *(((8: SP) + (12: SP)) + (((DP + (1: DP)) & (15: DP)) as SP)) = (110: U8) [unsigned, U8, 1]  // $display(((DP + (1: DP)) & (15: DP))) = (110: U8)
        *(((8: SP) + (12: SP)) + (((DP + (2: DP)) & (15: DP)) as SP)) = (100: U8) [unsigned, U8, 1]  // $display(((DP + (2: DP)) & (15: DP))) = (100: U8)
        *(((8: SP) + (12: SP)) + (((DP + (3: DP)) & (15: DP)) as SP)) = (101: U8) [unsigned, U8, 1]  // $display(((DP + (3: DP)) & (15: DP))) = (101: U8)
        *(((8: SP) + (12: SP)) + (((DP + (4: DP)) & (15: DP)) as SP)) = (120: U8) [unsigned, U8, 1]  // $display(((DP + (4: DP)) & (15: DP))) = (120: U8)
        *(((8: SP) + (12: SP)) + (((DP + (5: DP)) & (15: DP)) as SP)) = (32: U8) [unsigned, U8, 1]  // $display(((DP + (5: DP)) & (15: DP))) = (32: U8)
        *(((8: SP) + (12: SP)) + (((DP + (6: DP)) & (15: DP)) as SP)) = (111: U8) [unsigned, U8, 1]  // $display(((DP + (6: DP)) & (15: DP))) = (111: U8)
        *(((8: SP) + (12: SP)) + (((DP + (7: DP)) & (15: DP)) as SP)) = (117: U8) [unsigned, U8, 1]  // $display(((DP + (7: DP)) & (15: DP))) = (117: U8)
        *(((8: SP) + (12: SP)) + (((DP + (8: DP)) & (15: DP)) as SP)) = (116: U8) [unsigned, U8, 1]  // $display(((DP + (8: DP)) & (15: DP))) = (116: U8)
        *(((8: SP) + (12: SP)) + (((DP + (9: DP)) & (15: DP)) as SP)) = (32: U8) [unsigned, U8, 1]  // $display(((DP + (9: DP)) & (15: DP))) = (32: U8)
        *(((8: SP) + (12: SP)) + (((DP + (10: DP)) & (15: DP)) as SP)) = (111: U8) [unsigned, U8, 1]  // $display(((DP + (10: DP)) & (15: DP))) = (111: U8)
        *(((8: SP) + (12: SP)) + (((DP + (11: DP)) & (15: DP)) as SP)) = (102: U8) [unsigned, U8, 1]  // $display(((DP + (11: DP)) & (15: DP))) = (102: U8)
        *(((8: SP) + (12: SP)) + (((DP + (12: DP)) & (15: DP)) as SP)) = (32: U8) [unsigned, U8, 1]  // $display(((DP + (12: DP)) & (15: DP))) = (32: U8)
        *(((8: SP) + (12: SP)) + (((DP + (13: DP)) & (15: DP)) as SP)) = (98: U8) [unsigned, U8, 1]  // $display(((DP + (13: DP)) & (15: DP))) = (98: U8)
        *(((8: SP) + (12: SP)) + (((DP + (14: DP)) & (15: DP)) as SP)) = (111: U8) [unsigned, U8, 1]  // $display(((DP + (14: DP)) & (15: DP))) = (111: U8)
        *(((8: SP) + (12: SP)) + (((DP + (15: DP)) & (15: DP)) as SP)) = (117: U8) [unsigned, U8, 1]  // $display(((DP + (15: DP)) & (15: DP))) = (117: U8)
        *(((8: SP) + (12: SP)) + (((DP + (16: DP)) & (15: DP)) as SP)) = (110: U8) [unsigned, U8, 1]  // $display(((DP + (16: DP)) & (15: DP))) = (110: U8)
        *(((8: SP) + (12: SP)) + (((DP + (17: DP)) & (15: DP)) as SP)) = (100: U8) [unsigned, U8, 1]  // $display(((DP + (17: DP)) & (15: DP))) = (100: U8)
        *(((8: SP) + (12: SP)) + (((DP + (18: DP)) & (15: DP)) as SP)) = (115: U8) [unsigned, U8, 1]  // $display(((DP + (18: DP)) & (15: DP))) = (115: U8)
        goto .124
        */


        val __tmp_1180 = ((8.U(16.W) + 12.U(16.W)) + (DP & 15.U(64.W)).asUInt)
        val __tmp_1181 = (73.U(8.W)).asUInt
        arrayRegFiles(__tmp_1180 + 0.U) := __tmp_1181(7, 0)

        val __tmp_1182 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 1.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1183 = (110.U(8.W)).asUInt
        arrayRegFiles(__tmp_1182 + 0.U) := __tmp_1183(7, 0)

        val __tmp_1184 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 2.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1185 = (100.U(8.W)).asUInt
        arrayRegFiles(__tmp_1184 + 0.U) := __tmp_1185(7, 0)

        val __tmp_1186 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 3.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1187 = (101.U(8.W)).asUInt
        arrayRegFiles(__tmp_1186 + 0.U) := __tmp_1187(7, 0)

        val __tmp_1188 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 4.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1189 = (120.U(8.W)).asUInt
        arrayRegFiles(__tmp_1188 + 0.U) := __tmp_1189(7, 0)

        val __tmp_1190 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 5.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1191 = (32.U(8.W)).asUInt
        arrayRegFiles(__tmp_1190 + 0.U) := __tmp_1191(7, 0)

        val __tmp_1192 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 6.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1193 = (111.U(8.W)).asUInt
        arrayRegFiles(__tmp_1192 + 0.U) := __tmp_1193(7, 0)

        val __tmp_1194 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 7.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1195 = (117.U(8.W)).asUInt
        arrayRegFiles(__tmp_1194 + 0.U) := __tmp_1195(7, 0)

        val __tmp_1196 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 8.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1197 = (116.U(8.W)).asUInt
        arrayRegFiles(__tmp_1196 + 0.U) := __tmp_1197(7, 0)

        val __tmp_1198 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 9.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1199 = (32.U(8.W)).asUInt
        arrayRegFiles(__tmp_1198 + 0.U) := __tmp_1199(7, 0)

        val __tmp_1200 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 10.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1201 = (111.U(8.W)).asUInt
        arrayRegFiles(__tmp_1200 + 0.U) := __tmp_1201(7, 0)

        val __tmp_1202 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 11.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1203 = (102.U(8.W)).asUInt
        arrayRegFiles(__tmp_1202 + 0.U) := __tmp_1203(7, 0)

        val __tmp_1204 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 12.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1205 = (32.U(8.W)).asUInt
        arrayRegFiles(__tmp_1204 + 0.U) := __tmp_1205(7, 0)

        val __tmp_1206 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 13.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1207 = (98.U(8.W)).asUInt
        arrayRegFiles(__tmp_1206 + 0.U) := __tmp_1207(7, 0)

        val __tmp_1208 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 14.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1209 = (111.U(8.W)).asUInt
        arrayRegFiles(__tmp_1208 + 0.U) := __tmp_1209(7, 0)

        val __tmp_1210 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 15.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1211 = (117.U(8.W)).asUInt
        arrayRegFiles(__tmp_1210 + 0.U) := __tmp_1211(7, 0)

        val __tmp_1212 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 16.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1213 = (110.U(8.W)).asUInt
        arrayRegFiles(__tmp_1212 + 0.U) := __tmp_1213(7, 0)

        val __tmp_1214 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 17.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1215 = (100.U(8.W)).asUInt
        arrayRegFiles(__tmp_1214 + 0.U) := __tmp_1215(7, 0)

        val __tmp_1216 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 18.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1217 = (115.U(8.W)).asUInt
        arrayRegFiles(__tmp_1216 + 0.U) := __tmp_1217(7, 0)

        CP := 124.U
      }

      is(75.U) {
        /*
        $8 = *((($6: MS[Z, S16]) + (12: SP)) + ((($7: Z) as SP) * (2: SP))) [signed, S16, 2]  // $8 = ($6: MS[Z, S16])(($7: Z))
        goto .76
        */


        val __tmp_1218 = (((generalRegFiles(6.U) + 12.U(16.W)) + (generalRegFiles(7.U).asSInt.asUInt * 2.U(16.W)))).asUInt
        generalRegFiles(8.U) := Cat(
          arrayRegFiles(__tmp_1218 + 1.U),
          arrayRegFiles(__tmp_1218 + 0.U)
        ).asSInt.pad(GENERAL_REG_WIDTH).asUInt

        CP := 76.U
      }

      is(76.U) {
        /*
        $9 = (($5: S16) > ($8: S16))
        goto .77
        */



        generalRegFiles(9.U) := (generalRegFiles(5.U).asSInt > generalRegFiles(8.U).asSInt).asUInt

        CP := 77.U
      }

      is(77.U) {
        /*
        if ($9: B) goto .78 else goto .81
        */


        CP := Mux((generalRegFiles(9.U).asUInt) === 1.U, 78.U, 81.U)
      }

      is(78.U) {
        /*
        $3 = ($0: MS[Z, S16])
        $4 = ($1: Z)
        $5 = ($2: Z)
        goto .79
        */



        generalRegFiles(3.U) := generalRegFiles(0.U)


        generalRegFiles(4.U) := (generalRegFiles(1.U).asSInt).asUInt


        generalRegFiles(5.U) := (generalRegFiles(2.U).asSInt).asUInt

        CP := 79.U
      }

      is(79.U) {
        /*
        SP = SP + 42
        goto .128
        */


        SP := SP + 42.U

        CP := 128.U
      }

      is(81.U) {
        /*
        $3 = ($2: Z)
        goto .82
        */



        generalRegFiles(3.U) := (generalRegFiles(2.U).asSInt).asUInt

        CP := 82.U
      }

      is(82.U) {
        /*
        $4 = (($3: Z) + (1: Z))
        goto .83
        */



        generalRegFiles(4.U) := ((generalRegFiles(3.U).asSInt + 1.S(64.W))).asUInt

        CP := 83.U
      }

      is(83.U) {
        /*
        $2 = ($4: Z)
        goto .60
        */



        generalRegFiles(2.U) := (generalRegFiles(4.U).asSInt).asUInt

        CP := 60.U
      }

      is(84.U) {
        /*
        $3 = ($1: Z)
        goto .85
        */



        generalRegFiles(3.U) := (generalRegFiles(1.U).asSInt).asUInt

        CP := 85.U
      }

      is(85.U) {
        /*
        $4 = (($3: Z) + (1: Z))
        goto .86
        */



        generalRegFiles(4.U) := ((generalRegFiles(3.U).asSInt + 1.S(64.W))).asUInt

        CP := 86.U
      }

      is(86.U) {
        /*
        $1 = ($4: Z)
        goto .53
        */



        generalRegFiles(1.U) := (generalRegFiles(4.U).asSInt).asUInt

        CP := 53.U
      }

      is(87.U) {
        /*
        goto $ret@0
        */


        CP := Cat(
          arrayRegFiles(SP + 0.U + 1.U),
          arrayRegFiles(SP + 0.U + 0.U)
        )

      }

      is(88.U) {
        /*
        *(((8: SP) + (12: SP)) + ((DP & (15: DP)) as SP)) = (91: U8) [unsigned, U8, 1]  // $display((DP & (15: DP))) = (91: U8)
        goto .132
        */


        val __tmp_1219 = ((8.U(16.W) + 12.U(16.W)) + (DP & 15.U(64.W)).asUInt)
        val __tmp_1220 = (91.U(8.W)).asUInt
        arrayRegFiles(__tmp_1219 + 0.U) := __tmp_1220(7, 0)

        CP := 132.U
      }

      is(89.U) {
        /*
        $6 = *(($5: MS[Z, S16]) + (4: SP)) [signed, Z, 8]  // $6 = ($5: MS[Z, S16]).size
        goto .90
        */


        val __tmp_1221 = ((generalRegFiles(5.U) + 4.U(16.W))).asUInt
        generalRegFiles(6.U) := Cat(
          arrayRegFiles(__tmp_1221 + 7.U),
          arrayRegFiles(__tmp_1221 + 6.U),
          arrayRegFiles(__tmp_1221 + 5.U),
          arrayRegFiles(__tmp_1221 + 4.U),
          arrayRegFiles(__tmp_1221 + 3.U),
          arrayRegFiles(__tmp_1221 + 2.U),
          arrayRegFiles(__tmp_1221 + 1.U),
          arrayRegFiles(__tmp_1221 + 0.U)
        ).asSInt.pad(GENERAL_REG_WIDTH).asUInt

        CP := 90.U
      }

      is(90.U) {
        /*
        $1 = ($6: Z)
        goto .91
        */



        generalRegFiles(1.U) := (generalRegFiles(6.U).asSInt).asUInt

        CP := 91.U
      }

      is(91.U) {
        /*
        $6 = (($1: Z) > (0: Z))
        goto .92
        */



        generalRegFiles(6.U) := (generalRegFiles(1.U).asSInt > 0.S(64.W)).asUInt

        CP := 92.U
      }

      is(92.U) {
        /*
        if ($6: B) goto .93 else goto .119
        */


        CP := Mux((generalRegFiles(6.U).asUInt) === 1.U, 93.U, 119.U)
      }

      is(93.U) {
        /*
        $5 = ($0: MS[Z, S16])
        goto .94
        */



        generalRegFiles(5.U) := generalRegFiles(0.U)

        CP := 94.U
      }

      is(94.U) {
        /*
        if (((0: Z) <= (0: Z)) & ((0: Z) <= *(($5: MS[Z, S16]) + (4: SP)))) goto .98 else goto .95
        */


        CP := Mux((((0.S(64.W) <= 0.S(64.W)).asUInt & (0.S(64.W) <= Cat(
                            arrayRegFiles((generalRegFiles(5.U) + 4.U(16.W)) + 7.U),
                            arrayRegFiles((generalRegFiles(5.U) + 4.U(16.W)) + 6.U),
                            arrayRegFiles((generalRegFiles(5.U) + 4.U(16.W)) + 5.U),
                            arrayRegFiles((generalRegFiles(5.U) + 4.U(16.W)) + 4.U),
                            arrayRegFiles((generalRegFiles(5.U) + 4.U(16.W)) + 3.U),
                            arrayRegFiles((generalRegFiles(5.U) + 4.U(16.W)) + 2.U),
                            arrayRegFiles((generalRegFiles(5.U) + 4.U(16.W)) + 1.U),
                            arrayRegFiles((generalRegFiles(5.U) + 4.U(16.W)) + 0.U)
                          ).asSInt).asUInt).asUInt) === 1.U, 98.U, 95.U)
      }

      is(95.U) {
        /*
        *(((8: SP) + (12: SP)) + ((DP & (15: DP)) as SP)) = (73: U8) [unsigned, U8, 1]  // $display((DP & (15: DP))) = (73: U8)
        *(((8: SP) + (12: SP)) + (((DP + (1: DP)) & (15: DP)) as SP)) = (110: U8) [unsigned, U8, 1]  // $display(((DP + (1: DP)) & (15: DP))) = (110: U8)
        *(((8: SP) + (12: SP)) + (((DP + (2: DP)) & (15: DP)) as SP)) = (100: U8) [unsigned, U8, 1]  // $display(((DP + (2: DP)) & (15: DP))) = (100: U8)
        *(((8: SP) + (12: SP)) + (((DP + (3: DP)) & (15: DP)) as SP)) = (101: U8) [unsigned, U8, 1]  // $display(((DP + (3: DP)) & (15: DP))) = (101: U8)
        *(((8: SP) + (12: SP)) + (((DP + (4: DP)) & (15: DP)) as SP)) = (120: U8) [unsigned, U8, 1]  // $display(((DP + (4: DP)) & (15: DP))) = (120: U8)
        *(((8: SP) + (12: SP)) + (((DP + (5: DP)) & (15: DP)) as SP)) = (32: U8) [unsigned, U8, 1]  // $display(((DP + (5: DP)) & (15: DP))) = (32: U8)
        *(((8: SP) + (12: SP)) + (((DP + (6: DP)) & (15: DP)) as SP)) = (111: U8) [unsigned, U8, 1]  // $display(((DP + (6: DP)) & (15: DP))) = (111: U8)
        *(((8: SP) + (12: SP)) + (((DP + (7: DP)) & (15: DP)) as SP)) = (117: U8) [unsigned, U8, 1]  // $display(((DP + (7: DP)) & (15: DP))) = (117: U8)
        *(((8: SP) + (12: SP)) + (((DP + (8: DP)) & (15: DP)) as SP)) = (116: U8) [unsigned, U8, 1]  // $display(((DP + (8: DP)) & (15: DP))) = (116: U8)
        *(((8: SP) + (12: SP)) + (((DP + (9: DP)) & (15: DP)) as SP)) = (32: U8) [unsigned, U8, 1]  // $display(((DP + (9: DP)) & (15: DP))) = (32: U8)
        *(((8: SP) + (12: SP)) + (((DP + (10: DP)) & (15: DP)) as SP)) = (111: U8) [unsigned, U8, 1]  // $display(((DP + (10: DP)) & (15: DP))) = (111: U8)
        *(((8: SP) + (12: SP)) + (((DP + (11: DP)) & (15: DP)) as SP)) = (102: U8) [unsigned, U8, 1]  // $display(((DP + (11: DP)) & (15: DP))) = (102: U8)
        *(((8: SP) + (12: SP)) + (((DP + (12: DP)) & (15: DP)) as SP)) = (32: U8) [unsigned, U8, 1]  // $display(((DP + (12: DP)) & (15: DP))) = (32: U8)
        *(((8: SP) + (12: SP)) + (((DP + (13: DP)) & (15: DP)) as SP)) = (98: U8) [unsigned, U8, 1]  // $display(((DP + (13: DP)) & (15: DP))) = (98: U8)
        *(((8: SP) + (12: SP)) + (((DP + (14: DP)) & (15: DP)) as SP)) = (111: U8) [unsigned, U8, 1]  // $display(((DP + (14: DP)) & (15: DP))) = (111: U8)
        *(((8: SP) + (12: SP)) + (((DP + (15: DP)) & (15: DP)) as SP)) = (117: U8) [unsigned, U8, 1]  // $display(((DP + (15: DP)) & (15: DP))) = (117: U8)
        *(((8: SP) + (12: SP)) + (((DP + (16: DP)) & (15: DP)) as SP)) = (110: U8) [unsigned, U8, 1]  // $display(((DP + (16: DP)) & (15: DP))) = (110: U8)
        *(((8: SP) + (12: SP)) + (((DP + (17: DP)) & (15: DP)) as SP)) = (100: U8) [unsigned, U8, 1]  // $display(((DP + (17: DP)) & (15: DP))) = (100: U8)
        *(((8: SP) + (12: SP)) + (((DP + (18: DP)) & (15: DP)) as SP)) = (115: U8) [unsigned, U8, 1]  // $display(((DP + (18: DP)) & (15: DP))) = (115: U8)
        goto .134
        */


        val __tmp_1222 = ((8.U(16.W) + 12.U(16.W)) + (DP & 15.U(64.W)).asUInt)
        val __tmp_1223 = (73.U(8.W)).asUInt
        arrayRegFiles(__tmp_1222 + 0.U) := __tmp_1223(7, 0)

        val __tmp_1224 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 1.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1225 = (110.U(8.W)).asUInt
        arrayRegFiles(__tmp_1224 + 0.U) := __tmp_1225(7, 0)

        val __tmp_1226 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 2.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1227 = (100.U(8.W)).asUInt
        arrayRegFiles(__tmp_1226 + 0.U) := __tmp_1227(7, 0)

        val __tmp_1228 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 3.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1229 = (101.U(8.W)).asUInt
        arrayRegFiles(__tmp_1228 + 0.U) := __tmp_1229(7, 0)

        val __tmp_1230 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 4.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1231 = (120.U(8.W)).asUInt
        arrayRegFiles(__tmp_1230 + 0.U) := __tmp_1231(7, 0)

        val __tmp_1232 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 5.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1233 = (32.U(8.W)).asUInt
        arrayRegFiles(__tmp_1232 + 0.U) := __tmp_1233(7, 0)

        val __tmp_1234 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 6.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1235 = (111.U(8.W)).asUInt
        arrayRegFiles(__tmp_1234 + 0.U) := __tmp_1235(7, 0)

        val __tmp_1236 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 7.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1237 = (117.U(8.W)).asUInt
        arrayRegFiles(__tmp_1236 + 0.U) := __tmp_1237(7, 0)

        val __tmp_1238 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 8.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1239 = (116.U(8.W)).asUInt
        arrayRegFiles(__tmp_1238 + 0.U) := __tmp_1239(7, 0)

        val __tmp_1240 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 9.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1241 = (32.U(8.W)).asUInt
        arrayRegFiles(__tmp_1240 + 0.U) := __tmp_1241(7, 0)

        val __tmp_1242 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 10.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1243 = (111.U(8.W)).asUInt
        arrayRegFiles(__tmp_1242 + 0.U) := __tmp_1243(7, 0)

        val __tmp_1244 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 11.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1245 = (102.U(8.W)).asUInt
        arrayRegFiles(__tmp_1244 + 0.U) := __tmp_1245(7, 0)

        val __tmp_1246 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 12.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1247 = (32.U(8.W)).asUInt
        arrayRegFiles(__tmp_1246 + 0.U) := __tmp_1247(7, 0)

        val __tmp_1248 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 13.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1249 = (98.U(8.W)).asUInt
        arrayRegFiles(__tmp_1248 + 0.U) := __tmp_1249(7, 0)

        val __tmp_1250 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 14.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1251 = (111.U(8.W)).asUInt
        arrayRegFiles(__tmp_1250 + 0.U) := __tmp_1251(7, 0)

        val __tmp_1252 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 15.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1253 = (117.U(8.W)).asUInt
        arrayRegFiles(__tmp_1252 + 0.U) := __tmp_1253(7, 0)

        val __tmp_1254 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 16.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1255 = (110.U(8.W)).asUInt
        arrayRegFiles(__tmp_1254 + 0.U) := __tmp_1255(7, 0)

        val __tmp_1256 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 17.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1257 = (100.U(8.W)).asUInt
        arrayRegFiles(__tmp_1256 + 0.U) := __tmp_1257(7, 0)

        val __tmp_1258 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 18.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1259 = (115.U(8.W)).asUInt
        arrayRegFiles(__tmp_1258 + 0.U) := __tmp_1259(7, 0)

        CP := 134.U
      }

      is(98.U) {
        /*
        $6 = *((($5: MS[Z, S16]) + (12: SP)) + (((0: Z) as SP) * (2: SP))) [signed, S16, 2]  // $6 = ($5: MS[Z, S16])((0: Z))
        goto .99
        */


        val __tmp_1260 = (((generalRegFiles(5.U) + 12.U(16.W)) + (0.S(64.W).asUInt * 2.U(16.W)))).asUInt
        generalRegFiles(6.U) := Cat(
          arrayRegFiles(__tmp_1260 + 1.U),
          arrayRegFiles(__tmp_1260 + 0.U)
        ).asSInt.pad(GENERAL_REG_WIDTH).asUInt

        CP := 99.U
      }

      is(99.U) {
        /*
        alloc printS64$res@[29,11].7DF34DB5: U64 [@2, 8]
        goto .100
        */


        CP := 100.U
      }

      is(100.U) {
        /*
        SP = SP + 28
        goto .138
        */


        SP := SP + 28.U

        CP := 138.U
      }

      is(102.U) {
        /*
        DP = DP + (($7: U64) as DP)
        goto .142
        */


        DP := DP + generalRegFiles(7.U).asUInt

        CP := 142.U
      }

      is(103.U) {
        /*
        $2 = (1: Z)
        goto .104
        */



        generalRegFiles(2.U) := (1.S(64.W)).asUInt

        CP := 104.U
      }

      is(104.U) {
        /*
        $5 = ($2: Z)
        $6 = ($1: Z)
        goto .105
        */



        generalRegFiles(5.U) := (generalRegFiles(2.U).asSInt).asUInt


        generalRegFiles(6.U) := (generalRegFiles(1.U).asSInt).asUInt

        CP := 105.U
      }

      is(105.U) {
        /*
        $7 = (($5: Z) < ($6: Z))
        goto .106
        */



        generalRegFiles(7.U) := (generalRegFiles(5.U).asSInt < generalRegFiles(6.U).asSInt).asUInt

        CP := 106.U
      }

      is(106.U) {
        /*
        if ($7: B) goto .107 else goto .119
        */


        CP := Mux((generalRegFiles(7.U).asUInt) === 1.U, 107.U, 119.U)
      }

      is(107.U) {
        /*
        *(((8: SP) + (12: SP)) + ((DP & (15: DP)) as SP)) = (44: U8) [unsigned, U8, 1]  // $display((DP & (15: DP))) = (44: U8)
        *(((8: SP) + (12: SP)) + (((DP + (1: DP)) & (15: DP)) as SP)) = (32: U8) [unsigned, U8, 1]  // $display(((DP + (1: DP)) & (15: DP))) = (32: U8)
        goto .143
        */


        val __tmp_1261 = ((8.U(16.W) + 12.U(16.W)) + (DP & 15.U(64.W)).asUInt)
        val __tmp_1262 = (44.U(8.W)).asUInt
        arrayRegFiles(__tmp_1261 + 0.U) := __tmp_1262(7, 0)

        val __tmp_1263 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 1.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1264 = (32.U(8.W)).asUInt
        arrayRegFiles(__tmp_1263 + 0.U) := __tmp_1264(7, 0)

        CP := 143.U
      }

      is(108.U) {
        /*
        if (((0: Z) <= ($6: Z)) & (($6: Z) <= *(($5: MS[Z, S16]) + (4: SP)))) goto .112 else goto .109
        */


        CP := Mux((((0.S(64.W) <= generalRegFiles(6.U).asSInt).asUInt & (generalRegFiles(6.U).asSInt <= Cat(
                            arrayRegFiles((generalRegFiles(5.U) + 4.U(16.W)) + 7.U),
                            arrayRegFiles((generalRegFiles(5.U) + 4.U(16.W)) + 6.U),
                            arrayRegFiles((generalRegFiles(5.U) + 4.U(16.W)) + 5.U),
                            arrayRegFiles((generalRegFiles(5.U) + 4.U(16.W)) + 4.U),
                            arrayRegFiles((generalRegFiles(5.U) + 4.U(16.W)) + 3.U),
                            arrayRegFiles((generalRegFiles(5.U) + 4.U(16.W)) + 2.U),
                            arrayRegFiles((generalRegFiles(5.U) + 4.U(16.W)) + 1.U),
                            arrayRegFiles((generalRegFiles(5.U) + 4.U(16.W)) + 0.U)
                          ).asSInt).asUInt).asUInt) === 1.U, 112.U, 109.U)
      }

      is(109.U) {
        /*
        *(((8: SP) + (12: SP)) + ((DP & (15: DP)) as SP)) = (73: U8) [unsigned, U8, 1]  // $display((DP & (15: DP))) = (73: U8)
        *(((8: SP) + (12: SP)) + (((DP + (1: DP)) & (15: DP)) as SP)) = (110: U8) [unsigned, U8, 1]  // $display(((DP + (1: DP)) & (15: DP))) = (110: U8)
        *(((8: SP) + (12: SP)) + (((DP + (2: DP)) & (15: DP)) as SP)) = (100: U8) [unsigned, U8, 1]  // $display(((DP + (2: DP)) & (15: DP))) = (100: U8)
        *(((8: SP) + (12: SP)) + (((DP + (3: DP)) & (15: DP)) as SP)) = (101: U8) [unsigned, U8, 1]  // $display(((DP + (3: DP)) & (15: DP))) = (101: U8)
        *(((8: SP) + (12: SP)) + (((DP + (4: DP)) & (15: DP)) as SP)) = (120: U8) [unsigned, U8, 1]  // $display(((DP + (4: DP)) & (15: DP))) = (120: U8)
        *(((8: SP) + (12: SP)) + (((DP + (5: DP)) & (15: DP)) as SP)) = (32: U8) [unsigned, U8, 1]  // $display(((DP + (5: DP)) & (15: DP))) = (32: U8)
        *(((8: SP) + (12: SP)) + (((DP + (6: DP)) & (15: DP)) as SP)) = (111: U8) [unsigned, U8, 1]  // $display(((DP + (6: DP)) & (15: DP))) = (111: U8)
        *(((8: SP) + (12: SP)) + (((DP + (7: DP)) & (15: DP)) as SP)) = (117: U8) [unsigned, U8, 1]  // $display(((DP + (7: DP)) & (15: DP))) = (117: U8)
        *(((8: SP) + (12: SP)) + (((DP + (8: DP)) & (15: DP)) as SP)) = (116: U8) [unsigned, U8, 1]  // $display(((DP + (8: DP)) & (15: DP))) = (116: U8)
        *(((8: SP) + (12: SP)) + (((DP + (9: DP)) & (15: DP)) as SP)) = (32: U8) [unsigned, U8, 1]  // $display(((DP + (9: DP)) & (15: DP))) = (32: U8)
        *(((8: SP) + (12: SP)) + (((DP + (10: DP)) & (15: DP)) as SP)) = (111: U8) [unsigned, U8, 1]  // $display(((DP + (10: DP)) & (15: DP))) = (111: U8)
        *(((8: SP) + (12: SP)) + (((DP + (11: DP)) & (15: DP)) as SP)) = (102: U8) [unsigned, U8, 1]  // $display(((DP + (11: DP)) & (15: DP))) = (102: U8)
        *(((8: SP) + (12: SP)) + (((DP + (12: DP)) & (15: DP)) as SP)) = (32: U8) [unsigned, U8, 1]  // $display(((DP + (12: DP)) & (15: DP))) = (32: U8)
        *(((8: SP) + (12: SP)) + (((DP + (13: DP)) & (15: DP)) as SP)) = (98: U8) [unsigned, U8, 1]  // $display(((DP + (13: DP)) & (15: DP))) = (98: U8)
        *(((8: SP) + (12: SP)) + (((DP + (14: DP)) & (15: DP)) as SP)) = (111: U8) [unsigned, U8, 1]  // $display(((DP + (14: DP)) & (15: DP))) = (111: U8)
        *(((8: SP) + (12: SP)) + (((DP + (15: DP)) & (15: DP)) as SP)) = (117: U8) [unsigned, U8, 1]  // $display(((DP + (15: DP)) & (15: DP))) = (117: U8)
        *(((8: SP) + (12: SP)) + (((DP + (16: DP)) & (15: DP)) as SP)) = (110: U8) [unsigned, U8, 1]  // $display(((DP + (16: DP)) & (15: DP))) = (110: U8)
        *(((8: SP) + (12: SP)) + (((DP + (17: DP)) & (15: DP)) as SP)) = (100: U8) [unsigned, U8, 1]  // $display(((DP + (17: DP)) & (15: DP))) = (100: U8)
        *(((8: SP) + (12: SP)) + (((DP + (18: DP)) & (15: DP)) as SP)) = (115: U8) [unsigned, U8, 1]  // $display(((DP + (18: DP)) & (15: DP))) = (115: U8)
        goto .145
        */


        val __tmp_1265 = ((8.U(16.W) + 12.U(16.W)) + (DP & 15.U(64.W)).asUInt)
        val __tmp_1266 = (73.U(8.W)).asUInt
        arrayRegFiles(__tmp_1265 + 0.U) := __tmp_1266(7, 0)

        val __tmp_1267 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 1.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1268 = (110.U(8.W)).asUInt
        arrayRegFiles(__tmp_1267 + 0.U) := __tmp_1268(7, 0)

        val __tmp_1269 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 2.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1270 = (100.U(8.W)).asUInt
        arrayRegFiles(__tmp_1269 + 0.U) := __tmp_1270(7, 0)

        val __tmp_1271 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 3.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1272 = (101.U(8.W)).asUInt
        arrayRegFiles(__tmp_1271 + 0.U) := __tmp_1272(7, 0)

        val __tmp_1273 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 4.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1274 = (120.U(8.W)).asUInt
        arrayRegFiles(__tmp_1273 + 0.U) := __tmp_1274(7, 0)

        val __tmp_1275 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 5.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1276 = (32.U(8.W)).asUInt
        arrayRegFiles(__tmp_1275 + 0.U) := __tmp_1276(7, 0)

        val __tmp_1277 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 6.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1278 = (111.U(8.W)).asUInt
        arrayRegFiles(__tmp_1277 + 0.U) := __tmp_1278(7, 0)

        val __tmp_1279 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 7.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1280 = (117.U(8.W)).asUInt
        arrayRegFiles(__tmp_1279 + 0.U) := __tmp_1280(7, 0)

        val __tmp_1281 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 8.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1282 = (116.U(8.W)).asUInt
        arrayRegFiles(__tmp_1281 + 0.U) := __tmp_1282(7, 0)

        val __tmp_1283 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 9.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1284 = (32.U(8.W)).asUInt
        arrayRegFiles(__tmp_1283 + 0.U) := __tmp_1284(7, 0)

        val __tmp_1285 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 10.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1286 = (111.U(8.W)).asUInt
        arrayRegFiles(__tmp_1285 + 0.U) := __tmp_1286(7, 0)

        val __tmp_1287 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 11.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1288 = (102.U(8.W)).asUInt
        arrayRegFiles(__tmp_1287 + 0.U) := __tmp_1288(7, 0)

        val __tmp_1289 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 12.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1290 = (32.U(8.W)).asUInt
        arrayRegFiles(__tmp_1289 + 0.U) := __tmp_1290(7, 0)

        val __tmp_1291 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 13.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1292 = (98.U(8.W)).asUInt
        arrayRegFiles(__tmp_1291 + 0.U) := __tmp_1292(7, 0)

        val __tmp_1293 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 14.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1294 = (111.U(8.W)).asUInt
        arrayRegFiles(__tmp_1293 + 0.U) := __tmp_1294(7, 0)

        val __tmp_1295 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 15.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1296 = (117.U(8.W)).asUInt
        arrayRegFiles(__tmp_1295 + 0.U) := __tmp_1296(7, 0)

        val __tmp_1297 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 16.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1298 = (110.U(8.W)).asUInt
        arrayRegFiles(__tmp_1297 + 0.U) := __tmp_1298(7, 0)

        val __tmp_1299 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 17.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1300 = (100.U(8.W)).asUInt
        arrayRegFiles(__tmp_1299 + 0.U) := __tmp_1300(7, 0)

        val __tmp_1301 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 18.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1302 = (115.U(8.W)).asUInt
        arrayRegFiles(__tmp_1301 + 0.U) := __tmp_1302(7, 0)

        CP := 145.U
      }

      is(112.U) {
        /*
        $7 = *((($5: MS[Z, S16]) + (12: SP)) + ((($6: Z) as SP) * (2: SP))) [signed, S16, 2]  // $7 = ($5: MS[Z, S16])(($6: Z))
        goto .113
        */


        val __tmp_1303 = (((generalRegFiles(5.U) + 12.U(16.W)) + (generalRegFiles(6.U).asSInt.asUInt * 2.U(16.W)))).asUInt
        generalRegFiles(7.U) := Cat(
          arrayRegFiles(__tmp_1303 + 1.U),
          arrayRegFiles(__tmp_1303 + 0.U)
        ).asSInt.pad(GENERAL_REG_WIDTH).asUInt

        CP := 113.U
      }

      is(113.U) {
        /*
        alloc printS64$res@[33,13].FA1ACB48: U64 [@10, 8]
        goto .114
        */


        CP := 114.U
      }

      is(114.U) {
        /*
        SP = SP + 36
        goto .149
        */


        SP := SP + 36.U

        CP := 149.U
      }

      is(116.U) {
        /*
        DP = DP + (($8: U64) as DP)
        goto .153
        */


        DP := DP + generalRegFiles(8.U).asUInt

        CP := 153.U
      }

      is(117.U) {
        /*
        $6 = (($5: Z) + (1: Z))
        goto .118
        */



        generalRegFiles(6.U) := ((generalRegFiles(5.U).asSInt + 1.S(64.W))).asUInt

        CP := 118.U
      }

      is(118.U) {
        /*
        $2 = ($6: Z)
        goto .104
        */



        generalRegFiles(2.U) := (generalRegFiles(6.U).asSInt).asUInt

        CP := 104.U
      }

      is(119.U) {
        /*
        *(((8: SP) + (12: SP)) + ((DP & (15: DP)) as SP)) = (93: U8) [unsigned, U8, 1]  // $display((DP & (15: DP))) = (93: U8)
        *(((8: SP) + (12: SP)) + (((DP + (1: DP)) & (15: DP)) as SP)) = (10: U8) [unsigned, U8, 1]  // $display(((DP + (1: DP)) & (15: DP))) = (10: U8)
        goto .154
        */


        val __tmp_1304 = ((8.U(16.W) + 12.U(16.W)) + (DP & 15.U(64.W)).asUInt)
        val __tmp_1305 = (93.U(8.W)).asUInt
        arrayRegFiles(__tmp_1304 + 0.U) := __tmp_1305(7, 0)

        val __tmp_1306 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 1.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1307 = (10.U(8.W)).asUInt
        arrayRegFiles(__tmp_1306 + 0.U) := __tmp_1307(7, 0)

        CP := 154.U
      }

      is(120.U) {
        /*
        DP = DP + 19
        goto .121
        */


        DP := DP + 19.U

        CP := 121.U
      }

      is(121.U) {
        /*
        *(((8: SP) + (12: SP)) + ((DP & (15: DP)) as SP)) = (10: U8) [unsigned, U8, 1]  // $display((DP & (15: DP))) = (10: U8)
        goto .122
        */


        val __tmp_1308 = ((8.U(16.W) + 12.U(16.W)) + (DP & 15.U(64.W)).asUInt)
        val __tmp_1309 = (10.U(8.W)).asUInt
        arrayRegFiles(__tmp_1308 + 0.U) := __tmp_1309(7, 0)

        CP := 122.U
      }

      is(122.U) {
        /*
        DP = DP + 1
        goto .1
        */


        DP := DP + 1.U

        CP := 1.U
      }

      is(124.U) {
        /*
        DP = DP + 19
        goto .125
        */


        DP := DP + 19.U

        CP := 125.U
      }

      is(125.U) {
        /*
        *(((8: SP) + (12: SP)) + ((DP & (15: DP)) as SP)) = (10: U8) [unsigned, U8, 1]  // $display((DP & (15: DP))) = (10: U8)
        goto .126
        */


        val __tmp_1310 = ((8.U(16.W) + 12.U(16.W)) + (DP & 15.U(64.W)).asUInt)
        val __tmp_1311 = (10.U(8.W)).asUInt
        arrayRegFiles(__tmp_1310 + 0.U) := __tmp_1311(7, 0)

        CP := 126.U
      }

      is(126.U) {
        /*
        DP = DP + 1
        goto .1
        */


        DP := DP + 1.U

        CP := 1.U
      }

      is(128.U) {
        /*
        *SP = (130: CP) [unsigned, CP, 2]  // $ret@0 = 1452
        $35 = ($3: MS[Z, S16])
        $36 = ($4: Z)
        $37 = ($5: Z)
        *(SP - (8: SP)) = ($1: Z) [signed, Z, 8]  // save $1 (Z)
        *(SP - (10: SP)) = ($0: SP) [unsigned, SP, 2]  // save $0 (SP)
        *(SP - (18: SP)) = ($2: Z) [signed, Z, 8]  // save $2 (Z)
        goto .129
        */


        val __tmp_1312 = SP
        val __tmp_1313 = (130.U(16.W)).asUInt
        arrayRegFiles(__tmp_1312 + 0.U) := __tmp_1313(7, 0)
        arrayRegFiles(__tmp_1312 + 1.U) := __tmp_1313(15, 8)


        generalRegFiles(35.U) := generalRegFiles(3.U)


        generalRegFiles(36.U) := (generalRegFiles(4.U).asSInt).asUInt


        generalRegFiles(37.U) := (generalRegFiles(5.U).asSInt).asUInt

        val __tmp_1314 = (SP - 8.U(16.W))
        val __tmp_1315 = (generalRegFiles(1.U).asSInt).asUInt
        arrayRegFiles(__tmp_1314 + 0.U) := __tmp_1315(7, 0)
        arrayRegFiles(__tmp_1314 + 1.U) := __tmp_1315(15, 8)
        arrayRegFiles(__tmp_1314 + 2.U) := __tmp_1315(23, 16)
        arrayRegFiles(__tmp_1314 + 3.U) := __tmp_1315(31, 24)
        arrayRegFiles(__tmp_1314 + 4.U) := __tmp_1315(39, 32)
        arrayRegFiles(__tmp_1314 + 5.U) := __tmp_1315(47, 40)
        arrayRegFiles(__tmp_1314 + 6.U) := __tmp_1315(55, 48)
        arrayRegFiles(__tmp_1314 + 7.U) := __tmp_1315(63, 56)

        val __tmp_1316 = (SP - 10.U(16.W))
        val __tmp_1317 = (generalRegFiles(0.U)).asUInt
        arrayRegFiles(__tmp_1316 + 0.U) := __tmp_1317(7, 0)
        arrayRegFiles(__tmp_1316 + 1.U) := __tmp_1317(15, 8)

        val __tmp_1318 = (SP - 18.U(16.W))
        val __tmp_1319 = (generalRegFiles(2.U).asSInt).asUInt
        arrayRegFiles(__tmp_1318 + 0.U) := __tmp_1319(7, 0)
        arrayRegFiles(__tmp_1318 + 1.U) := __tmp_1319(15, 8)
        arrayRegFiles(__tmp_1318 + 2.U) := __tmp_1319(23, 16)
        arrayRegFiles(__tmp_1318 + 3.U) := __tmp_1319(31, 24)
        arrayRegFiles(__tmp_1318 + 4.U) := __tmp_1319(39, 32)
        arrayRegFiles(__tmp_1318 + 5.U) := __tmp_1319(47, 40)
        arrayRegFiles(__tmp_1318 + 6.U) := __tmp_1319(55, 48)
        arrayRegFiles(__tmp_1318 + 7.U) := __tmp_1319(63, 56)

        CP := 129.U
      }

      is(129.U) {
        /*
        decl $ret: CP [@0, 2], a: MS[Z, S16] @$0, i: Z @$1, j: Z @$2
        $0 = ($35: MS[Z, S16])
        $1 = ($36: Z)
        $2 = ($37: Z)
        goto .155
        */



        generalRegFiles(0.U) := generalRegFiles(35.U)


        generalRegFiles(1.U) := (generalRegFiles(36.U).asSInt).asUInt


        generalRegFiles(2.U) := (generalRegFiles(37.U).asSInt).asUInt

        CP := 155.U
      }

      is(130.U) {
        /*
        $1 = *(SP - (8: SP)) [signed, Z, 8]  // restore $1 (Z)
        $0 = *(SP - (10: SP)) [unsigned, SP, 2]  // restore $0 (SP)
        $2 = *(SP - (18: SP)) [signed, Z, 8]  // restore $2 (Z)
        undecl j: Z @$2, i: Z @$1, a: MS[Z, S16] @$0, $ret: CP [@0, 2]
        goto .131
        */


        val __tmp_1320 = ((SP - 8.U(16.W))).asUInt
        generalRegFiles(1.U) := Cat(
          arrayRegFiles(__tmp_1320 + 7.U),
          arrayRegFiles(__tmp_1320 + 6.U),
          arrayRegFiles(__tmp_1320 + 5.U),
          arrayRegFiles(__tmp_1320 + 4.U),
          arrayRegFiles(__tmp_1320 + 3.U),
          arrayRegFiles(__tmp_1320 + 2.U),
          arrayRegFiles(__tmp_1320 + 1.U),
          arrayRegFiles(__tmp_1320 + 0.U)
        ).asSInt.pad(GENERAL_REG_WIDTH).asUInt

        val __tmp_1321 = ((SP - 10.U(16.W))).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_1321 + 1.U),
          arrayRegFiles(__tmp_1321 + 0.U)
        ).asUInt

        val __tmp_1322 = ((SP - 18.U(16.W))).asUInt
        generalRegFiles(2.U) := Cat(
          arrayRegFiles(__tmp_1322 + 7.U),
          arrayRegFiles(__tmp_1322 + 6.U),
          arrayRegFiles(__tmp_1322 + 5.U),
          arrayRegFiles(__tmp_1322 + 4.U),
          arrayRegFiles(__tmp_1322 + 3.U),
          arrayRegFiles(__tmp_1322 + 2.U),
          arrayRegFiles(__tmp_1322 + 1.U),
          arrayRegFiles(__tmp_1322 + 0.U)
        ).asSInt.pad(GENERAL_REG_WIDTH).asUInt

        CP := 131.U
      }

      is(131.U) {
        /*
        SP = SP - 42
        goto .81
        */


        SP := SP - 42.U

        CP := 81.U
      }

      is(132.U) {
        /*
        DP = DP + 1
        goto .133
        */


        DP := DP + 1.U

        CP := 133.U
      }

      is(133.U) {
        /*
        decl size: Z @$1
        $5 = ($0: MS[Z, S16])
        goto .89
        */



        generalRegFiles(5.U) := generalRegFiles(0.U)

        CP := 89.U
      }

      is(134.U) {
        /*
        DP = DP + 19
        goto .135
        */


        DP := DP + 19.U

        CP := 135.U
      }

      is(135.U) {
        /*
        *(((8: SP) + (12: SP)) + ((DP & (15: DP)) as SP)) = (10: U8) [unsigned, U8, 1]  // $display((DP & (15: DP))) = (10: U8)
        goto .136
        */


        val __tmp_1323 = ((8.U(16.W) + 12.U(16.W)) + (DP & 15.U(64.W)).asUInt)
        val __tmp_1324 = (10.U(8.W)).asUInt
        arrayRegFiles(__tmp_1323 + 0.U) := __tmp_1324(7, 0)

        CP := 136.U
      }

      is(136.U) {
        /*
        DP = DP + 1
        goto .1
        */


        DP := DP + 1.U

        CP := 1.U
      }

      is(138.U) {
        /*
        *SP = (140: CP) [unsigned, CP, 2]  // $ret@0 = 1454
        *(SP + (2: SP)) = (SP - (26: SP)) [unsigned, SP, 2]  // $res@2 = -26
        $91 = (8: SP)
        $92 = DP
        $93 = (15: anvil.PrinterIndex.U)
        $94 = (($6: S16) as S64)
        *(SP - (2: SP)) = ($0: SP) [unsigned, SP, 2]  // save $0 (SP)
        *(SP - (10: SP)) = ($1: Z) [signed, Z, 8]  // save $1 (Z)
        goto .139
        */


        val __tmp_1325 = SP
        val __tmp_1326 = (140.U(16.W)).asUInt
        arrayRegFiles(__tmp_1325 + 0.U) := __tmp_1326(7, 0)
        arrayRegFiles(__tmp_1325 + 1.U) := __tmp_1326(15, 8)

        val __tmp_1327 = (SP + 2.U(16.W))
        val __tmp_1328 = ((SP - 26.U(16.W))).asUInt
        arrayRegFiles(__tmp_1327 + 0.U) := __tmp_1328(7, 0)
        arrayRegFiles(__tmp_1327 + 1.U) := __tmp_1328(15, 8)


        generalRegFiles(91.U) := 8.U(16.W)


        generalRegFiles(92.U) := DP


        generalRegFiles(93.U) := 15.U(64.W)


        generalRegFiles(94.U) := (generalRegFiles(6.U).asSInt.asSInt).asUInt

        val __tmp_1329 = (SP - 2.U(16.W))
        val __tmp_1330 = (generalRegFiles(0.U)).asUInt
        arrayRegFiles(__tmp_1329 + 0.U) := __tmp_1330(7, 0)
        arrayRegFiles(__tmp_1329 + 1.U) := __tmp_1330(15, 8)

        val __tmp_1331 = (SP - 10.U(16.W))
        val __tmp_1332 = (generalRegFiles(1.U).asSInt).asUInt
        arrayRegFiles(__tmp_1331 + 0.U) := __tmp_1332(7, 0)
        arrayRegFiles(__tmp_1331 + 1.U) := __tmp_1332(15, 8)
        arrayRegFiles(__tmp_1331 + 2.U) := __tmp_1332(23, 16)
        arrayRegFiles(__tmp_1331 + 3.U) := __tmp_1332(31, 24)
        arrayRegFiles(__tmp_1331 + 4.U) := __tmp_1332(39, 32)
        arrayRegFiles(__tmp_1331 + 5.U) := __tmp_1332(47, 40)
        arrayRegFiles(__tmp_1331 + 6.U) := __tmp_1332(55, 48)
        arrayRegFiles(__tmp_1331 + 7.U) := __tmp_1332(63, 56)

        CP := 139.U
      }

      is(139.U) {
        /*
        decl $ret: CP [@0, 2], $res: SP [@2, 2], buffer: MS[anvil.PrinterIndex.U, U8] @$0, index: anvil.PrinterIndex.U @$1, mask: anvil.PrinterIndex.U @$2, n: S64 @$3
        $0 = ($91: MS[anvil.PrinterIndex.U, U8])
        $1 = ($92: anvil.PrinterIndex.U)
        $2 = ($93: anvil.PrinterIndex.U)
        $3 = ($94: S64)
        goto .180
        */



        generalRegFiles(0.U) := generalRegFiles(91.U)


        generalRegFiles(1.U) := generalRegFiles(92.U)


        generalRegFiles(2.U) := generalRegFiles(93.U)


        generalRegFiles(3.U) := (generalRegFiles(94.U).asSInt).asUInt

        CP := 180.U
      }

      is(140.U) {
        /*
        $0 = *(SP - (2: SP)) [unsigned, SP, 2]  // restore $0 (SP)
        $1 = *(SP - (10: SP)) [signed, Z, 8]  // restore $1 (Z)
        $7 = **(SP + (2: SP)) [unsigned, U64, 8]  // $7 = $res
        undecl n: S64 @$3, mask: anvil.PrinterIndex.U @$2, index: anvil.PrinterIndex.U @$1, buffer: MS[anvil.PrinterIndex.U, U8] @$0, $res: SP [@2, 2], $ret: CP [@0, 2]
        goto .141
        */


        val __tmp_1333 = ((SP - 2.U(16.W))).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_1333 + 1.U),
          arrayRegFiles(__tmp_1333 + 0.U)
        ).asUInt

        val __tmp_1334 = ((SP - 10.U(16.W))).asUInt
        generalRegFiles(1.U) := Cat(
          arrayRegFiles(__tmp_1334 + 7.U),
          arrayRegFiles(__tmp_1334 + 6.U),
          arrayRegFiles(__tmp_1334 + 5.U),
          arrayRegFiles(__tmp_1334 + 4.U),
          arrayRegFiles(__tmp_1334 + 3.U),
          arrayRegFiles(__tmp_1334 + 2.U),
          arrayRegFiles(__tmp_1334 + 1.U),
          arrayRegFiles(__tmp_1334 + 0.U)
        ).asSInt.pad(GENERAL_REG_WIDTH).asUInt

        val __tmp_1335 = (Cat(
          arrayRegFiles((SP + 2.U(16.W)) + 1.U),
          arrayRegFiles((SP + 2.U(16.W)) + 0.U)
        )).asUInt
        generalRegFiles(7.U) := Cat(
          arrayRegFiles(__tmp_1335 + 7.U),
          arrayRegFiles(__tmp_1335 + 6.U),
          arrayRegFiles(__tmp_1335 + 5.U),
          arrayRegFiles(__tmp_1335 + 4.U),
          arrayRegFiles(__tmp_1335 + 3.U),
          arrayRegFiles(__tmp_1335 + 2.U),
          arrayRegFiles(__tmp_1335 + 1.U),
          arrayRegFiles(__tmp_1335 + 0.U)
        ).asUInt

        CP := 141.U
      }

      is(141.U) {
        /*
        SP = SP - 28
        goto .102
        */


        SP := SP - 28.U

        CP := 102.U
      }

      is(142.U) {
        /*
        decl i: Z @$2
        goto .103
        */


        CP := 103.U
      }

      is(143.U) {
        /*
        DP = DP + 2
        goto .144
        */


        DP := DP + 2.U

        CP := 144.U
      }

      is(144.U) {
        /*
        $5 = ($0: MS[Z, S16])
        $6 = ($2: Z)
        goto .108
        */



        generalRegFiles(5.U) := generalRegFiles(0.U)


        generalRegFiles(6.U) := (generalRegFiles(2.U).asSInt).asUInt

        CP := 108.U
      }

      is(145.U) {
        /*
        DP = DP + 19
        goto .146
        */


        DP := DP + 19.U

        CP := 146.U
      }

      is(146.U) {
        /*
        *(((8: SP) + (12: SP)) + ((DP & (15: DP)) as SP)) = (10: U8) [unsigned, U8, 1]  // $display((DP & (15: DP))) = (10: U8)
        goto .147
        */


        val __tmp_1336 = ((8.U(16.W) + 12.U(16.W)) + (DP & 15.U(64.W)).asUInt)
        val __tmp_1337 = (10.U(8.W)).asUInt
        arrayRegFiles(__tmp_1336 + 0.U) := __tmp_1337(7, 0)

        CP := 147.U
      }

      is(147.U) {
        /*
        DP = DP + 1
        goto .1
        */


        DP := DP + 1.U

        CP := 1.U
      }

      is(149.U) {
        /*
        *SP = (151: CP) [unsigned, CP, 2]  // $ret@0 = 1456
        *(SP + (2: SP)) = (SP - (26: SP)) [unsigned, SP, 2]  // $res@2 = -26
        $91 = (8: SP)
        $92 = DP
        $93 = (15: anvil.PrinterIndex.U)
        $94 = (($7: S16) as S64)
        *(SP - (2: SP)) = ($0: SP) [unsigned, SP, 2]  // save $0 (SP)
        *(SP - (10: SP)) = ($1: Z) [signed, Z, 8]  // save $1 (Z)
        *(SP - (18: SP)) = ($2: Z) [signed, Z, 8]  // save $2 (Z)
        goto .150
        */


        val __tmp_1338 = SP
        val __tmp_1339 = (151.U(16.W)).asUInt
        arrayRegFiles(__tmp_1338 + 0.U) := __tmp_1339(7, 0)
        arrayRegFiles(__tmp_1338 + 1.U) := __tmp_1339(15, 8)

        val __tmp_1340 = (SP + 2.U(16.W))
        val __tmp_1341 = ((SP - 26.U(16.W))).asUInt
        arrayRegFiles(__tmp_1340 + 0.U) := __tmp_1341(7, 0)
        arrayRegFiles(__tmp_1340 + 1.U) := __tmp_1341(15, 8)


        generalRegFiles(91.U) := 8.U(16.W)


        generalRegFiles(92.U) := DP


        generalRegFiles(93.U) := 15.U(64.W)


        generalRegFiles(94.U) := (generalRegFiles(7.U).asSInt.asSInt).asUInt

        val __tmp_1342 = (SP - 2.U(16.W))
        val __tmp_1343 = (generalRegFiles(0.U)).asUInt
        arrayRegFiles(__tmp_1342 + 0.U) := __tmp_1343(7, 0)
        arrayRegFiles(__tmp_1342 + 1.U) := __tmp_1343(15, 8)

        val __tmp_1344 = (SP - 10.U(16.W))
        val __tmp_1345 = (generalRegFiles(1.U).asSInt).asUInt
        arrayRegFiles(__tmp_1344 + 0.U) := __tmp_1345(7, 0)
        arrayRegFiles(__tmp_1344 + 1.U) := __tmp_1345(15, 8)
        arrayRegFiles(__tmp_1344 + 2.U) := __tmp_1345(23, 16)
        arrayRegFiles(__tmp_1344 + 3.U) := __tmp_1345(31, 24)
        arrayRegFiles(__tmp_1344 + 4.U) := __tmp_1345(39, 32)
        arrayRegFiles(__tmp_1344 + 5.U) := __tmp_1345(47, 40)
        arrayRegFiles(__tmp_1344 + 6.U) := __tmp_1345(55, 48)
        arrayRegFiles(__tmp_1344 + 7.U) := __tmp_1345(63, 56)

        val __tmp_1346 = (SP - 18.U(16.W))
        val __tmp_1347 = (generalRegFiles(2.U).asSInt).asUInt
        arrayRegFiles(__tmp_1346 + 0.U) := __tmp_1347(7, 0)
        arrayRegFiles(__tmp_1346 + 1.U) := __tmp_1347(15, 8)
        arrayRegFiles(__tmp_1346 + 2.U) := __tmp_1347(23, 16)
        arrayRegFiles(__tmp_1346 + 3.U) := __tmp_1347(31, 24)
        arrayRegFiles(__tmp_1346 + 4.U) := __tmp_1347(39, 32)
        arrayRegFiles(__tmp_1346 + 5.U) := __tmp_1347(47, 40)
        arrayRegFiles(__tmp_1346 + 6.U) := __tmp_1347(55, 48)
        arrayRegFiles(__tmp_1346 + 7.U) := __tmp_1347(63, 56)

        CP := 150.U
      }

      is(150.U) {
        /*
        decl $ret: CP [@0, 2], $res: SP [@2, 2], buffer: MS[anvil.PrinterIndex.U, U8] @$0, index: anvil.PrinterIndex.U @$1, mask: anvil.PrinterIndex.U @$2, n: S64 @$3
        $0 = ($91: MS[anvil.PrinterIndex.U, U8])
        $1 = ($92: anvil.PrinterIndex.U)
        $2 = ($93: anvil.PrinterIndex.U)
        $3 = ($94: S64)
        goto .180
        */



        generalRegFiles(0.U) := generalRegFiles(91.U)


        generalRegFiles(1.U) := generalRegFiles(92.U)


        generalRegFiles(2.U) := generalRegFiles(93.U)


        generalRegFiles(3.U) := (generalRegFiles(94.U).asSInt).asUInt

        CP := 180.U
      }

      is(151.U) {
        /*
        $0 = *(SP - (2: SP)) [unsigned, SP, 2]  // restore $0 (SP)
        $1 = *(SP - (10: SP)) [signed, Z, 8]  // restore $1 (Z)
        $2 = *(SP - (18: SP)) [signed, Z, 8]  // restore $2 (Z)
        $8 = **(SP + (2: SP)) [unsigned, U64, 8]  // $8 = $res
        undecl n: S64 @$3, mask: anvil.PrinterIndex.U @$2, index: anvil.PrinterIndex.U @$1, buffer: MS[anvil.PrinterIndex.U, U8] @$0, $res: SP [@2, 2], $ret: CP [@0, 2]
        goto .152
        */


        val __tmp_1348 = ((SP - 2.U(16.W))).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_1348 + 1.U),
          arrayRegFiles(__tmp_1348 + 0.U)
        ).asUInt

        val __tmp_1349 = ((SP - 10.U(16.W))).asUInt
        generalRegFiles(1.U) := Cat(
          arrayRegFiles(__tmp_1349 + 7.U),
          arrayRegFiles(__tmp_1349 + 6.U),
          arrayRegFiles(__tmp_1349 + 5.U),
          arrayRegFiles(__tmp_1349 + 4.U),
          arrayRegFiles(__tmp_1349 + 3.U),
          arrayRegFiles(__tmp_1349 + 2.U),
          arrayRegFiles(__tmp_1349 + 1.U),
          arrayRegFiles(__tmp_1349 + 0.U)
        ).asSInt.pad(GENERAL_REG_WIDTH).asUInt

        val __tmp_1350 = ((SP - 18.U(16.W))).asUInt
        generalRegFiles(2.U) := Cat(
          arrayRegFiles(__tmp_1350 + 7.U),
          arrayRegFiles(__tmp_1350 + 6.U),
          arrayRegFiles(__tmp_1350 + 5.U),
          arrayRegFiles(__tmp_1350 + 4.U),
          arrayRegFiles(__tmp_1350 + 3.U),
          arrayRegFiles(__tmp_1350 + 2.U),
          arrayRegFiles(__tmp_1350 + 1.U),
          arrayRegFiles(__tmp_1350 + 0.U)
        ).asSInt.pad(GENERAL_REG_WIDTH).asUInt

        val __tmp_1351 = (Cat(
          arrayRegFiles((SP + 2.U(16.W)) + 1.U),
          arrayRegFiles((SP + 2.U(16.W)) + 0.U)
        )).asUInt
        generalRegFiles(8.U) := Cat(
          arrayRegFiles(__tmp_1351 + 7.U),
          arrayRegFiles(__tmp_1351 + 6.U),
          arrayRegFiles(__tmp_1351 + 5.U),
          arrayRegFiles(__tmp_1351 + 4.U),
          arrayRegFiles(__tmp_1351 + 3.U),
          arrayRegFiles(__tmp_1351 + 2.U),
          arrayRegFiles(__tmp_1351 + 1.U),
          arrayRegFiles(__tmp_1351 + 0.U)
        ).asUInt

        CP := 152.U
      }

      is(152.U) {
        /*
        SP = SP - 36
        goto .116
        */


        SP := SP - 36.U

        CP := 116.U
      }

      is(153.U) {
        /*
        $5 = ($2: Z)
        goto .117
        */



        generalRegFiles(5.U) := (generalRegFiles(2.U).asSInt).asUInt

        CP := 117.U
      }

      is(154.U) {
        /*
        DP = DP + 2
        goto $ret@0
        */


        DP := DP + 2.U

        CP := Cat(
          arrayRegFiles(SP + 0.U + 1.U),
          arrayRegFiles(SP + 0.U + 0.U)
        )

      }

      is(155.U) {
        /*
        decl t: S16 @$3
        $4 = ($0: MS[Z, S16])
        goto .156
        */



        generalRegFiles(4.U) := generalRegFiles(0.U)

        CP := 156.U
      }

      is(156.U) {
        /*
        if (((0: Z) <= ($1: Z)) & (($1: Z) <= *(($4: MS[Z, S16]) + (4: SP)))) goto .160 else goto .157
        */


        CP := Mux((((0.S(64.W) <= generalRegFiles(1.U).asSInt).asUInt & (generalRegFiles(1.U).asSInt <= Cat(
                            arrayRegFiles((generalRegFiles(4.U) + 4.U(16.W)) + 7.U),
                            arrayRegFiles((generalRegFiles(4.U) + 4.U(16.W)) + 6.U),
                            arrayRegFiles((generalRegFiles(4.U) + 4.U(16.W)) + 5.U),
                            arrayRegFiles((generalRegFiles(4.U) + 4.U(16.W)) + 4.U),
                            arrayRegFiles((generalRegFiles(4.U) + 4.U(16.W)) + 3.U),
                            arrayRegFiles((generalRegFiles(4.U) + 4.U(16.W)) + 2.U),
                            arrayRegFiles((generalRegFiles(4.U) + 4.U(16.W)) + 1.U),
                            arrayRegFiles((generalRegFiles(4.U) + 4.U(16.W)) + 0.U)
                          ).asSInt).asUInt).asUInt) === 1.U, 160.U, 157.U)
      }

      is(157.U) {
        /*
        *(((8: SP) + (12: SP)) + ((DP & (15: DP)) as SP)) = (73: U8) [unsigned, U8, 1]  // $display((DP & (15: DP))) = (73: U8)
        *(((8: SP) + (12: SP)) + (((DP + (1: DP)) & (15: DP)) as SP)) = (110: U8) [unsigned, U8, 1]  // $display(((DP + (1: DP)) & (15: DP))) = (110: U8)
        *(((8: SP) + (12: SP)) + (((DP + (2: DP)) & (15: DP)) as SP)) = (100: U8) [unsigned, U8, 1]  // $display(((DP + (2: DP)) & (15: DP))) = (100: U8)
        *(((8: SP) + (12: SP)) + (((DP + (3: DP)) & (15: DP)) as SP)) = (101: U8) [unsigned, U8, 1]  // $display(((DP + (3: DP)) & (15: DP))) = (101: U8)
        *(((8: SP) + (12: SP)) + (((DP + (4: DP)) & (15: DP)) as SP)) = (120: U8) [unsigned, U8, 1]  // $display(((DP + (4: DP)) & (15: DP))) = (120: U8)
        *(((8: SP) + (12: SP)) + (((DP + (5: DP)) & (15: DP)) as SP)) = (32: U8) [unsigned, U8, 1]  // $display(((DP + (5: DP)) & (15: DP))) = (32: U8)
        *(((8: SP) + (12: SP)) + (((DP + (6: DP)) & (15: DP)) as SP)) = (111: U8) [unsigned, U8, 1]  // $display(((DP + (6: DP)) & (15: DP))) = (111: U8)
        *(((8: SP) + (12: SP)) + (((DP + (7: DP)) & (15: DP)) as SP)) = (117: U8) [unsigned, U8, 1]  // $display(((DP + (7: DP)) & (15: DP))) = (117: U8)
        *(((8: SP) + (12: SP)) + (((DP + (8: DP)) & (15: DP)) as SP)) = (116: U8) [unsigned, U8, 1]  // $display(((DP + (8: DP)) & (15: DP))) = (116: U8)
        *(((8: SP) + (12: SP)) + (((DP + (9: DP)) & (15: DP)) as SP)) = (32: U8) [unsigned, U8, 1]  // $display(((DP + (9: DP)) & (15: DP))) = (32: U8)
        *(((8: SP) + (12: SP)) + (((DP + (10: DP)) & (15: DP)) as SP)) = (111: U8) [unsigned, U8, 1]  // $display(((DP + (10: DP)) & (15: DP))) = (111: U8)
        *(((8: SP) + (12: SP)) + (((DP + (11: DP)) & (15: DP)) as SP)) = (102: U8) [unsigned, U8, 1]  // $display(((DP + (11: DP)) & (15: DP))) = (102: U8)
        *(((8: SP) + (12: SP)) + (((DP + (12: DP)) & (15: DP)) as SP)) = (32: U8) [unsigned, U8, 1]  // $display(((DP + (12: DP)) & (15: DP))) = (32: U8)
        *(((8: SP) + (12: SP)) + (((DP + (13: DP)) & (15: DP)) as SP)) = (98: U8) [unsigned, U8, 1]  // $display(((DP + (13: DP)) & (15: DP))) = (98: U8)
        *(((8: SP) + (12: SP)) + (((DP + (14: DP)) & (15: DP)) as SP)) = (111: U8) [unsigned, U8, 1]  // $display(((DP + (14: DP)) & (15: DP))) = (111: U8)
        *(((8: SP) + (12: SP)) + (((DP + (15: DP)) & (15: DP)) as SP)) = (117: U8) [unsigned, U8, 1]  // $display(((DP + (15: DP)) & (15: DP))) = (117: U8)
        *(((8: SP) + (12: SP)) + (((DP + (16: DP)) & (15: DP)) as SP)) = (110: U8) [unsigned, U8, 1]  // $display(((DP + (16: DP)) & (15: DP))) = (110: U8)
        *(((8: SP) + (12: SP)) + (((DP + (17: DP)) & (15: DP)) as SP)) = (100: U8) [unsigned, U8, 1]  // $display(((DP + (17: DP)) & (15: DP))) = (100: U8)
        *(((8: SP) + (12: SP)) + (((DP + (18: DP)) & (15: DP)) as SP)) = (115: U8) [unsigned, U8, 1]  // $display(((DP + (18: DP)) & (15: DP))) = (115: U8)
        goto .325
        */


        val __tmp_1352 = ((8.U(16.W) + 12.U(16.W)) + (DP & 15.U(64.W)).asUInt)
        val __tmp_1353 = (73.U(8.W)).asUInt
        arrayRegFiles(__tmp_1352 + 0.U) := __tmp_1353(7, 0)

        val __tmp_1354 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 1.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1355 = (110.U(8.W)).asUInt
        arrayRegFiles(__tmp_1354 + 0.U) := __tmp_1355(7, 0)

        val __tmp_1356 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 2.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1357 = (100.U(8.W)).asUInt
        arrayRegFiles(__tmp_1356 + 0.U) := __tmp_1357(7, 0)

        val __tmp_1358 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 3.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1359 = (101.U(8.W)).asUInt
        arrayRegFiles(__tmp_1358 + 0.U) := __tmp_1359(7, 0)

        val __tmp_1360 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 4.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1361 = (120.U(8.W)).asUInt
        arrayRegFiles(__tmp_1360 + 0.U) := __tmp_1361(7, 0)

        val __tmp_1362 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 5.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1363 = (32.U(8.W)).asUInt
        arrayRegFiles(__tmp_1362 + 0.U) := __tmp_1363(7, 0)

        val __tmp_1364 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 6.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1365 = (111.U(8.W)).asUInt
        arrayRegFiles(__tmp_1364 + 0.U) := __tmp_1365(7, 0)

        val __tmp_1366 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 7.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1367 = (117.U(8.W)).asUInt
        arrayRegFiles(__tmp_1366 + 0.U) := __tmp_1367(7, 0)

        val __tmp_1368 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 8.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1369 = (116.U(8.W)).asUInt
        arrayRegFiles(__tmp_1368 + 0.U) := __tmp_1369(7, 0)

        val __tmp_1370 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 9.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1371 = (32.U(8.W)).asUInt
        arrayRegFiles(__tmp_1370 + 0.U) := __tmp_1371(7, 0)

        val __tmp_1372 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 10.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1373 = (111.U(8.W)).asUInt
        arrayRegFiles(__tmp_1372 + 0.U) := __tmp_1373(7, 0)

        val __tmp_1374 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 11.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1375 = (102.U(8.W)).asUInt
        arrayRegFiles(__tmp_1374 + 0.U) := __tmp_1375(7, 0)

        val __tmp_1376 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 12.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1377 = (32.U(8.W)).asUInt
        arrayRegFiles(__tmp_1376 + 0.U) := __tmp_1377(7, 0)

        val __tmp_1378 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 13.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1379 = (98.U(8.W)).asUInt
        arrayRegFiles(__tmp_1378 + 0.U) := __tmp_1379(7, 0)

        val __tmp_1380 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 14.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1381 = (111.U(8.W)).asUInt
        arrayRegFiles(__tmp_1380 + 0.U) := __tmp_1381(7, 0)

        val __tmp_1382 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 15.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1383 = (117.U(8.W)).asUInt
        arrayRegFiles(__tmp_1382 + 0.U) := __tmp_1383(7, 0)

        val __tmp_1384 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 16.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1385 = (110.U(8.W)).asUInt
        arrayRegFiles(__tmp_1384 + 0.U) := __tmp_1385(7, 0)

        val __tmp_1386 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 17.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1387 = (100.U(8.W)).asUInt
        arrayRegFiles(__tmp_1386 + 0.U) := __tmp_1387(7, 0)

        val __tmp_1388 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 18.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1389 = (115.U(8.W)).asUInt
        arrayRegFiles(__tmp_1388 + 0.U) := __tmp_1389(7, 0)

        CP := 325.U
      }

      is(160.U) {
        /*
        $5 = *((($4: MS[Z, S16]) + (12: SP)) + ((($1: Z) as SP) * (2: SP))) [signed, S16, 2]  // $5 = ($4: MS[Z, S16])(($1: Z))
        goto .161
        */


        val __tmp_1390 = (((generalRegFiles(4.U) + 12.U(16.W)) + (generalRegFiles(1.U).asSInt.asUInt * 2.U(16.W)))).asUInt
        generalRegFiles(5.U) := Cat(
          arrayRegFiles(__tmp_1390 + 1.U),
          arrayRegFiles(__tmp_1390 + 0.U)
        ).asSInt.pad(GENERAL_REG_WIDTH).asUInt

        CP := 161.U
      }

      is(161.U) {
        /*
        $3 = ($5: S16)
        goto .162
        */



        generalRegFiles(3.U) := (generalRegFiles(5.U).asSInt).asUInt

        CP := 162.U
      }

      is(162.U) {
        /*
        $4 = ($0: MS[Z, S16])
        $5 = ($0: MS[Z, S16])
        goto .163
        */



        generalRegFiles(4.U) := generalRegFiles(0.U)


        generalRegFiles(5.U) := generalRegFiles(0.U)

        CP := 163.U
      }

      is(163.U) {
        /*
        if (((0: Z) <= ($2: Z)) & (($2: Z) <= *(($5: MS[Z, S16]) + (4: SP)))) goto .167 else goto .164
        */


        CP := Mux((((0.S(64.W) <= generalRegFiles(2.U).asSInt).asUInt & (generalRegFiles(2.U).asSInt <= Cat(
                            arrayRegFiles((generalRegFiles(5.U) + 4.U(16.W)) + 7.U),
                            arrayRegFiles((generalRegFiles(5.U) + 4.U(16.W)) + 6.U),
                            arrayRegFiles((generalRegFiles(5.U) + 4.U(16.W)) + 5.U),
                            arrayRegFiles((generalRegFiles(5.U) + 4.U(16.W)) + 4.U),
                            arrayRegFiles((generalRegFiles(5.U) + 4.U(16.W)) + 3.U),
                            arrayRegFiles((generalRegFiles(5.U) + 4.U(16.W)) + 2.U),
                            arrayRegFiles((generalRegFiles(5.U) + 4.U(16.W)) + 1.U),
                            arrayRegFiles((generalRegFiles(5.U) + 4.U(16.W)) + 0.U)
                          ).asSInt).asUInt).asUInt) === 1.U, 167.U, 164.U)
      }

      is(164.U) {
        /*
        *(((8: SP) + (12: SP)) + ((DP & (15: DP)) as SP)) = (73: U8) [unsigned, U8, 1]  // $display((DP & (15: DP))) = (73: U8)
        *(((8: SP) + (12: SP)) + (((DP + (1: DP)) & (15: DP)) as SP)) = (110: U8) [unsigned, U8, 1]  // $display(((DP + (1: DP)) & (15: DP))) = (110: U8)
        *(((8: SP) + (12: SP)) + (((DP + (2: DP)) & (15: DP)) as SP)) = (100: U8) [unsigned, U8, 1]  // $display(((DP + (2: DP)) & (15: DP))) = (100: U8)
        *(((8: SP) + (12: SP)) + (((DP + (3: DP)) & (15: DP)) as SP)) = (101: U8) [unsigned, U8, 1]  // $display(((DP + (3: DP)) & (15: DP))) = (101: U8)
        *(((8: SP) + (12: SP)) + (((DP + (4: DP)) & (15: DP)) as SP)) = (120: U8) [unsigned, U8, 1]  // $display(((DP + (4: DP)) & (15: DP))) = (120: U8)
        *(((8: SP) + (12: SP)) + (((DP + (5: DP)) & (15: DP)) as SP)) = (32: U8) [unsigned, U8, 1]  // $display(((DP + (5: DP)) & (15: DP))) = (32: U8)
        *(((8: SP) + (12: SP)) + (((DP + (6: DP)) & (15: DP)) as SP)) = (111: U8) [unsigned, U8, 1]  // $display(((DP + (6: DP)) & (15: DP))) = (111: U8)
        *(((8: SP) + (12: SP)) + (((DP + (7: DP)) & (15: DP)) as SP)) = (117: U8) [unsigned, U8, 1]  // $display(((DP + (7: DP)) & (15: DP))) = (117: U8)
        *(((8: SP) + (12: SP)) + (((DP + (8: DP)) & (15: DP)) as SP)) = (116: U8) [unsigned, U8, 1]  // $display(((DP + (8: DP)) & (15: DP))) = (116: U8)
        *(((8: SP) + (12: SP)) + (((DP + (9: DP)) & (15: DP)) as SP)) = (32: U8) [unsigned, U8, 1]  // $display(((DP + (9: DP)) & (15: DP))) = (32: U8)
        *(((8: SP) + (12: SP)) + (((DP + (10: DP)) & (15: DP)) as SP)) = (111: U8) [unsigned, U8, 1]  // $display(((DP + (10: DP)) & (15: DP))) = (111: U8)
        *(((8: SP) + (12: SP)) + (((DP + (11: DP)) & (15: DP)) as SP)) = (102: U8) [unsigned, U8, 1]  // $display(((DP + (11: DP)) & (15: DP))) = (102: U8)
        *(((8: SP) + (12: SP)) + (((DP + (12: DP)) & (15: DP)) as SP)) = (32: U8) [unsigned, U8, 1]  // $display(((DP + (12: DP)) & (15: DP))) = (32: U8)
        *(((8: SP) + (12: SP)) + (((DP + (13: DP)) & (15: DP)) as SP)) = (98: U8) [unsigned, U8, 1]  // $display(((DP + (13: DP)) & (15: DP))) = (98: U8)
        *(((8: SP) + (12: SP)) + (((DP + (14: DP)) & (15: DP)) as SP)) = (111: U8) [unsigned, U8, 1]  // $display(((DP + (14: DP)) & (15: DP))) = (111: U8)
        *(((8: SP) + (12: SP)) + (((DP + (15: DP)) & (15: DP)) as SP)) = (117: U8) [unsigned, U8, 1]  // $display(((DP + (15: DP)) & (15: DP))) = (117: U8)
        *(((8: SP) + (12: SP)) + (((DP + (16: DP)) & (15: DP)) as SP)) = (110: U8) [unsigned, U8, 1]  // $display(((DP + (16: DP)) & (15: DP))) = (110: U8)
        *(((8: SP) + (12: SP)) + (((DP + (17: DP)) & (15: DP)) as SP)) = (100: U8) [unsigned, U8, 1]  // $display(((DP + (17: DP)) & (15: DP))) = (100: U8)
        *(((8: SP) + (12: SP)) + (((DP + (18: DP)) & (15: DP)) as SP)) = (115: U8) [unsigned, U8, 1]  // $display(((DP + (18: DP)) & (15: DP))) = (115: U8)
        goto .329
        */


        val __tmp_1391 = ((8.U(16.W) + 12.U(16.W)) + (DP & 15.U(64.W)).asUInt)
        val __tmp_1392 = (73.U(8.W)).asUInt
        arrayRegFiles(__tmp_1391 + 0.U) := __tmp_1392(7, 0)

        val __tmp_1393 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 1.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1394 = (110.U(8.W)).asUInt
        arrayRegFiles(__tmp_1393 + 0.U) := __tmp_1394(7, 0)

        val __tmp_1395 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 2.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1396 = (100.U(8.W)).asUInt
        arrayRegFiles(__tmp_1395 + 0.U) := __tmp_1396(7, 0)

        val __tmp_1397 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 3.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1398 = (101.U(8.W)).asUInt
        arrayRegFiles(__tmp_1397 + 0.U) := __tmp_1398(7, 0)

        val __tmp_1399 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 4.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1400 = (120.U(8.W)).asUInt
        arrayRegFiles(__tmp_1399 + 0.U) := __tmp_1400(7, 0)

        val __tmp_1401 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 5.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1402 = (32.U(8.W)).asUInt
        arrayRegFiles(__tmp_1401 + 0.U) := __tmp_1402(7, 0)

        val __tmp_1403 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 6.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1404 = (111.U(8.W)).asUInt
        arrayRegFiles(__tmp_1403 + 0.U) := __tmp_1404(7, 0)

        val __tmp_1405 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 7.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1406 = (117.U(8.W)).asUInt
        arrayRegFiles(__tmp_1405 + 0.U) := __tmp_1406(7, 0)

        val __tmp_1407 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 8.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1408 = (116.U(8.W)).asUInt
        arrayRegFiles(__tmp_1407 + 0.U) := __tmp_1408(7, 0)

        val __tmp_1409 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 9.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1410 = (32.U(8.W)).asUInt
        arrayRegFiles(__tmp_1409 + 0.U) := __tmp_1410(7, 0)

        val __tmp_1411 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 10.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1412 = (111.U(8.W)).asUInt
        arrayRegFiles(__tmp_1411 + 0.U) := __tmp_1412(7, 0)

        val __tmp_1413 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 11.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1414 = (102.U(8.W)).asUInt
        arrayRegFiles(__tmp_1413 + 0.U) := __tmp_1414(7, 0)

        val __tmp_1415 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 12.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1416 = (32.U(8.W)).asUInt
        arrayRegFiles(__tmp_1415 + 0.U) := __tmp_1416(7, 0)

        val __tmp_1417 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 13.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1418 = (98.U(8.W)).asUInt
        arrayRegFiles(__tmp_1417 + 0.U) := __tmp_1418(7, 0)

        val __tmp_1419 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 14.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1420 = (111.U(8.W)).asUInt
        arrayRegFiles(__tmp_1419 + 0.U) := __tmp_1420(7, 0)

        val __tmp_1421 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 15.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1422 = (117.U(8.W)).asUInt
        arrayRegFiles(__tmp_1421 + 0.U) := __tmp_1422(7, 0)

        val __tmp_1423 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 16.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1424 = (110.U(8.W)).asUInt
        arrayRegFiles(__tmp_1423 + 0.U) := __tmp_1424(7, 0)

        val __tmp_1425 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 17.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1426 = (100.U(8.W)).asUInt
        arrayRegFiles(__tmp_1425 + 0.U) := __tmp_1426(7, 0)

        val __tmp_1427 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 18.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1428 = (115.U(8.W)).asUInt
        arrayRegFiles(__tmp_1427 + 0.U) := __tmp_1428(7, 0)

        CP := 329.U
      }

      is(167.U) {
        /*
        $6 = *((($5: MS[Z, S16]) + (12: SP)) + ((($2: Z) as SP) * (2: SP))) [signed, S16, 2]  // $6 = ($5: MS[Z, S16])(($2: Z))
        goto .168
        */


        val __tmp_1429 = (((generalRegFiles(5.U) + 12.U(16.W)) + (generalRegFiles(2.U).asSInt.asUInt * 2.U(16.W)))).asUInt
        generalRegFiles(6.U) := Cat(
          arrayRegFiles(__tmp_1429 + 1.U),
          arrayRegFiles(__tmp_1429 + 0.U)
        ).asSInt.pad(GENERAL_REG_WIDTH).asUInt

        CP := 168.U
      }

      is(168.U) {
        /*
        if (((0: Z) <= ($1: Z)) & (($1: Z) <= *(($4: MS[Z, S16]) + (4: SP)))) goto .172 else goto .169
        */


        CP := Mux((((0.S(64.W) <= generalRegFiles(1.U).asSInt).asUInt & (generalRegFiles(1.U).asSInt <= Cat(
                            arrayRegFiles((generalRegFiles(4.U) + 4.U(16.W)) + 7.U),
                            arrayRegFiles((generalRegFiles(4.U) + 4.U(16.W)) + 6.U),
                            arrayRegFiles((generalRegFiles(4.U) + 4.U(16.W)) + 5.U),
                            arrayRegFiles((generalRegFiles(4.U) + 4.U(16.W)) + 4.U),
                            arrayRegFiles((generalRegFiles(4.U) + 4.U(16.W)) + 3.U),
                            arrayRegFiles((generalRegFiles(4.U) + 4.U(16.W)) + 2.U),
                            arrayRegFiles((generalRegFiles(4.U) + 4.U(16.W)) + 1.U),
                            arrayRegFiles((generalRegFiles(4.U) + 4.U(16.W)) + 0.U)
                          ).asSInt).asUInt).asUInt) === 1.U, 172.U, 169.U)
      }

      is(169.U) {
        /*
        *(((8: SP) + (12: SP)) + ((DP & (15: DP)) as SP)) = (73: U8) [unsigned, U8, 1]  // $display((DP & (15: DP))) = (73: U8)
        *(((8: SP) + (12: SP)) + (((DP + (1: DP)) & (15: DP)) as SP)) = (110: U8) [unsigned, U8, 1]  // $display(((DP + (1: DP)) & (15: DP))) = (110: U8)
        *(((8: SP) + (12: SP)) + (((DP + (2: DP)) & (15: DP)) as SP)) = (100: U8) [unsigned, U8, 1]  // $display(((DP + (2: DP)) & (15: DP))) = (100: U8)
        *(((8: SP) + (12: SP)) + (((DP + (3: DP)) & (15: DP)) as SP)) = (101: U8) [unsigned, U8, 1]  // $display(((DP + (3: DP)) & (15: DP))) = (101: U8)
        *(((8: SP) + (12: SP)) + (((DP + (4: DP)) & (15: DP)) as SP)) = (120: U8) [unsigned, U8, 1]  // $display(((DP + (4: DP)) & (15: DP))) = (120: U8)
        *(((8: SP) + (12: SP)) + (((DP + (5: DP)) & (15: DP)) as SP)) = (32: U8) [unsigned, U8, 1]  // $display(((DP + (5: DP)) & (15: DP))) = (32: U8)
        *(((8: SP) + (12: SP)) + (((DP + (6: DP)) & (15: DP)) as SP)) = (111: U8) [unsigned, U8, 1]  // $display(((DP + (6: DP)) & (15: DP))) = (111: U8)
        *(((8: SP) + (12: SP)) + (((DP + (7: DP)) & (15: DP)) as SP)) = (117: U8) [unsigned, U8, 1]  // $display(((DP + (7: DP)) & (15: DP))) = (117: U8)
        *(((8: SP) + (12: SP)) + (((DP + (8: DP)) & (15: DP)) as SP)) = (116: U8) [unsigned, U8, 1]  // $display(((DP + (8: DP)) & (15: DP))) = (116: U8)
        *(((8: SP) + (12: SP)) + (((DP + (9: DP)) & (15: DP)) as SP)) = (32: U8) [unsigned, U8, 1]  // $display(((DP + (9: DP)) & (15: DP))) = (32: U8)
        *(((8: SP) + (12: SP)) + (((DP + (10: DP)) & (15: DP)) as SP)) = (111: U8) [unsigned, U8, 1]  // $display(((DP + (10: DP)) & (15: DP))) = (111: U8)
        *(((8: SP) + (12: SP)) + (((DP + (11: DP)) & (15: DP)) as SP)) = (102: U8) [unsigned, U8, 1]  // $display(((DP + (11: DP)) & (15: DP))) = (102: U8)
        *(((8: SP) + (12: SP)) + (((DP + (12: DP)) & (15: DP)) as SP)) = (32: U8) [unsigned, U8, 1]  // $display(((DP + (12: DP)) & (15: DP))) = (32: U8)
        *(((8: SP) + (12: SP)) + (((DP + (13: DP)) & (15: DP)) as SP)) = (98: U8) [unsigned, U8, 1]  // $display(((DP + (13: DP)) & (15: DP))) = (98: U8)
        *(((8: SP) + (12: SP)) + (((DP + (14: DP)) & (15: DP)) as SP)) = (111: U8) [unsigned, U8, 1]  // $display(((DP + (14: DP)) & (15: DP))) = (111: U8)
        *(((8: SP) + (12: SP)) + (((DP + (15: DP)) & (15: DP)) as SP)) = (117: U8) [unsigned, U8, 1]  // $display(((DP + (15: DP)) & (15: DP))) = (117: U8)
        *(((8: SP) + (12: SP)) + (((DP + (16: DP)) & (15: DP)) as SP)) = (110: U8) [unsigned, U8, 1]  // $display(((DP + (16: DP)) & (15: DP))) = (110: U8)
        *(((8: SP) + (12: SP)) + (((DP + (17: DP)) & (15: DP)) as SP)) = (100: U8) [unsigned, U8, 1]  // $display(((DP + (17: DP)) & (15: DP))) = (100: U8)
        *(((8: SP) + (12: SP)) + (((DP + (18: DP)) & (15: DP)) as SP)) = (115: U8) [unsigned, U8, 1]  // $display(((DP + (18: DP)) & (15: DP))) = (115: U8)
        goto .333
        */


        val __tmp_1430 = ((8.U(16.W) + 12.U(16.W)) + (DP & 15.U(64.W)).asUInt)
        val __tmp_1431 = (73.U(8.W)).asUInt
        arrayRegFiles(__tmp_1430 + 0.U) := __tmp_1431(7, 0)

        val __tmp_1432 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 1.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1433 = (110.U(8.W)).asUInt
        arrayRegFiles(__tmp_1432 + 0.U) := __tmp_1433(7, 0)

        val __tmp_1434 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 2.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1435 = (100.U(8.W)).asUInt
        arrayRegFiles(__tmp_1434 + 0.U) := __tmp_1435(7, 0)

        val __tmp_1436 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 3.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1437 = (101.U(8.W)).asUInt
        arrayRegFiles(__tmp_1436 + 0.U) := __tmp_1437(7, 0)

        val __tmp_1438 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 4.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1439 = (120.U(8.W)).asUInt
        arrayRegFiles(__tmp_1438 + 0.U) := __tmp_1439(7, 0)

        val __tmp_1440 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 5.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1441 = (32.U(8.W)).asUInt
        arrayRegFiles(__tmp_1440 + 0.U) := __tmp_1441(7, 0)

        val __tmp_1442 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 6.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1443 = (111.U(8.W)).asUInt
        arrayRegFiles(__tmp_1442 + 0.U) := __tmp_1443(7, 0)

        val __tmp_1444 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 7.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1445 = (117.U(8.W)).asUInt
        arrayRegFiles(__tmp_1444 + 0.U) := __tmp_1445(7, 0)

        val __tmp_1446 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 8.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1447 = (116.U(8.W)).asUInt
        arrayRegFiles(__tmp_1446 + 0.U) := __tmp_1447(7, 0)

        val __tmp_1448 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 9.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1449 = (32.U(8.W)).asUInt
        arrayRegFiles(__tmp_1448 + 0.U) := __tmp_1449(7, 0)

        val __tmp_1450 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 10.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1451 = (111.U(8.W)).asUInt
        arrayRegFiles(__tmp_1450 + 0.U) := __tmp_1451(7, 0)

        val __tmp_1452 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 11.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1453 = (102.U(8.W)).asUInt
        arrayRegFiles(__tmp_1452 + 0.U) := __tmp_1453(7, 0)

        val __tmp_1454 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 12.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1455 = (32.U(8.W)).asUInt
        arrayRegFiles(__tmp_1454 + 0.U) := __tmp_1455(7, 0)

        val __tmp_1456 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 13.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1457 = (98.U(8.W)).asUInt
        arrayRegFiles(__tmp_1456 + 0.U) := __tmp_1457(7, 0)

        val __tmp_1458 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 14.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1459 = (111.U(8.W)).asUInt
        arrayRegFiles(__tmp_1458 + 0.U) := __tmp_1459(7, 0)

        val __tmp_1460 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 15.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1461 = (117.U(8.W)).asUInt
        arrayRegFiles(__tmp_1460 + 0.U) := __tmp_1461(7, 0)

        val __tmp_1462 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 16.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1463 = (110.U(8.W)).asUInt
        arrayRegFiles(__tmp_1462 + 0.U) := __tmp_1463(7, 0)

        val __tmp_1464 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 17.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1465 = (100.U(8.W)).asUInt
        arrayRegFiles(__tmp_1464 + 0.U) := __tmp_1465(7, 0)

        val __tmp_1466 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 18.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1467 = (115.U(8.W)).asUInt
        arrayRegFiles(__tmp_1466 + 0.U) := __tmp_1467(7, 0)

        CP := 333.U
      }

      is(172.U) {
        /*
        *((($4: MS[Z, S16]) + (12: SP)) + ((($1: Z) as SP) * (2: SP))) = ($6: S16) [signed, S16, 2]  // ($4: MS[Z, S16])(($1: Z)) = ($6: S16)
        goto .173
        */


        val __tmp_1468 = ((generalRegFiles(4.U) + 12.U(16.W)) + (generalRegFiles(1.U).asSInt.asUInt * 2.U(16.W)))
        val __tmp_1469 = (generalRegFiles(6.U).asSInt).asUInt
        arrayRegFiles(__tmp_1468 + 0.U) := __tmp_1469(7, 0)
        arrayRegFiles(__tmp_1468 + 1.U) := __tmp_1469(15, 8)

        CP := 173.U
      }

      is(173.U) {
        /*
        $4 = ($0: MS[Z, S16])
        goto .174
        */



        generalRegFiles(4.U) := generalRegFiles(0.U)

        CP := 174.U
      }

      is(174.U) {
        /*
        if (((0: Z) <= ($2: Z)) & (($2: Z) <= *(($4: MS[Z, S16]) + (4: SP)))) goto .178 else goto .175
        */


        CP := Mux((((0.S(64.W) <= generalRegFiles(2.U).asSInt).asUInt & (generalRegFiles(2.U).asSInt <= Cat(
                            arrayRegFiles((generalRegFiles(4.U) + 4.U(16.W)) + 7.U),
                            arrayRegFiles((generalRegFiles(4.U) + 4.U(16.W)) + 6.U),
                            arrayRegFiles((generalRegFiles(4.U) + 4.U(16.W)) + 5.U),
                            arrayRegFiles((generalRegFiles(4.U) + 4.U(16.W)) + 4.U),
                            arrayRegFiles((generalRegFiles(4.U) + 4.U(16.W)) + 3.U),
                            arrayRegFiles((generalRegFiles(4.U) + 4.U(16.W)) + 2.U),
                            arrayRegFiles((generalRegFiles(4.U) + 4.U(16.W)) + 1.U),
                            arrayRegFiles((generalRegFiles(4.U) + 4.U(16.W)) + 0.U)
                          ).asSInt).asUInt).asUInt) === 1.U, 178.U, 175.U)
      }

      is(175.U) {
        /*
        *(((8: SP) + (12: SP)) + ((DP & (15: DP)) as SP)) = (73: U8) [unsigned, U8, 1]  // $display((DP & (15: DP))) = (73: U8)
        *(((8: SP) + (12: SP)) + (((DP + (1: DP)) & (15: DP)) as SP)) = (110: U8) [unsigned, U8, 1]  // $display(((DP + (1: DP)) & (15: DP))) = (110: U8)
        *(((8: SP) + (12: SP)) + (((DP + (2: DP)) & (15: DP)) as SP)) = (100: U8) [unsigned, U8, 1]  // $display(((DP + (2: DP)) & (15: DP))) = (100: U8)
        *(((8: SP) + (12: SP)) + (((DP + (3: DP)) & (15: DP)) as SP)) = (101: U8) [unsigned, U8, 1]  // $display(((DP + (3: DP)) & (15: DP))) = (101: U8)
        *(((8: SP) + (12: SP)) + (((DP + (4: DP)) & (15: DP)) as SP)) = (120: U8) [unsigned, U8, 1]  // $display(((DP + (4: DP)) & (15: DP))) = (120: U8)
        *(((8: SP) + (12: SP)) + (((DP + (5: DP)) & (15: DP)) as SP)) = (32: U8) [unsigned, U8, 1]  // $display(((DP + (5: DP)) & (15: DP))) = (32: U8)
        *(((8: SP) + (12: SP)) + (((DP + (6: DP)) & (15: DP)) as SP)) = (111: U8) [unsigned, U8, 1]  // $display(((DP + (6: DP)) & (15: DP))) = (111: U8)
        *(((8: SP) + (12: SP)) + (((DP + (7: DP)) & (15: DP)) as SP)) = (117: U8) [unsigned, U8, 1]  // $display(((DP + (7: DP)) & (15: DP))) = (117: U8)
        *(((8: SP) + (12: SP)) + (((DP + (8: DP)) & (15: DP)) as SP)) = (116: U8) [unsigned, U8, 1]  // $display(((DP + (8: DP)) & (15: DP))) = (116: U8)
        *(((8: SP) + (12: SP)) + (((DP + (9: DP)) & (15: DP)) as SP)) = (32: U8) [unsigned, U8, 1]  // $display(((DP + (9: DP)) & (15: DP))) = (32: U8)
        *(((8: SP) + (12: SP)) + (((DP + (10: DP)) & (15: DP)) as SP)) = (111: U8) [unsigned, U8, 1]  // $display(((DP + (10: DP)) & (15: DP))) = (111: U8)
        *(((8: SP) + (12: SP)) + (((DP + (11: DP)) & (15: DP)) as SP)) = (102: U8) [unsigned, U8, 1]  // $display(((DP + (11: DP)) & (15: DP))) = (102: U8)
        *(((8: SP) + (12: SP)) + (((DP + (12: DP)) & (15: DP)) as SP)) = (32: U8) [unsigned, U8, 1]  // $display(((DP + (12: DP)) & (15: DP))) = (32: U8)
        *(((8: SP) + (12: SP)) + (((DP + (13: DP)) & (15: DP)) as SP)) = (98: U8) [unsigned, U8, 1]  // $display(((DP + (13: DP)) & (15: DP))) = (98: U8)
        *(((8: SP) + (12: SP)) + (((DP + (14: DP)) & (15: DP)) as SP)) = (111: U8) [unsigned, U8, 1]  // $display(((DP + (14: DP)) & (15: DP))) = (111: U8)
        *(((8: SP) + (12: SP)) + (((DP + (15: DP)) & (15: DP)) as SP)) = (117: U8) [unsigned, U8, 1]  // $display(((DP + (15: DP)) & (15: DP))) = (117: U8)
        *(((8: SP) + (12: SP)) + (((DP + (16: DP)) & (15: DP)) as SP)) = (110: U8) [unsigned, U8, 1]  // $display(((DP + (16: DP)) & (15: DP))) = (110: U8)
        *(((8: SP) + (12: SP)) + (((DP + (17: DP)) & (15: DP)) as SP)) = (100: U8) [unsigned, U8, 1]  // $display(((DP + (17: DP)) & (15: DP))) = (100: U8)
        *(((8: SP) + (12: SP)) + (((DP + (18: DP)) & (15: DP)) as SP)) = (115: U8) [unsigned, U8, 1]  // $display(((DP + (18: DP)) & (15: DP))) = (115: U8)
        goto .337
        */


        val __tmp_1470 = ((8.U(16.W) + 12.U(16.W)) + (DP & 15.U(64.W)).asUInt)
        val __tmp_1471 = (73.U(8.W)).asUInt
        arrayRegFiles(__tmp_1470 + 0.U) := __tmp_1471(7, 0)

        val __tmp_1472 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 1.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1473 = (110.U(8.W)).asUInt
        arrayRegFiles(__tmp_1472 + 0.U) := __tmp_1473(7, 0)

        val __tmp_1474 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 2.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1475 = (100.U(8.W)).asUInt
        arrayRegFiles(__tmp_1474 + 0.U) := __tmp_1475(7, 0)

        val __tmp_1476 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 3.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1477 = (101.U(8.W)).asUInt
        arrayRegFiles(__tmp_1476 + 0.U) := __tmp_1477(7, 0)

        val __tmp_1478 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 4.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1479 = (120.U(8.W)).asUInt
        arrayRegFiles(__tmp_1478 + 0.U) := __tmp_1479(7, 0)

        val __tmp_1480 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 5.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1481 = (32.U(8.W)).asUInt
        arrayRegFiles(__tmp_1480 + 0.U) := __tmp_1481(7, 0)

        val __tmp_1482 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 6.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1483 = (111.U(8.W)).asUInt
        arrayRegFiles(__tmp_1482 + 0.U) := __tmp_1483(7, 0)

        val __tmp_1484 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 7.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1485 = (117.U(8.W)).asUInt
        arrayRegFiles(__tmp_1484 + 0.U) := __tmp_1485(7, 0)

        val __tmp_1486 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 8.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1487 = (116.U(8.W)).asUInt
        arrayRegFiles(__tmp_1486 + 0.U) := __tmp_1487(7, 0)

        val __tmp_1488 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 9.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1489 = (32.U(8.W)).asUInt
        arrayRegFiles(__tmp_1488 + 0.U) := __tmp_1489(7, 0)

        val __tmp_1490 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 10.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1491 = (111.U(8.W)).asUInt
        arrayRegFiles(__tmp_1490 + 0.U) := __tmp_1491(7, 0)

        val __tmp_1492 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 11.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1493 = (102.U(8.W)).asUInt
        arrayRegFiles(__tmp_1492 + 0.U) := __tmp_1493(7, 0)

        val __tmp_1494 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 12.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1495 = (32.U(8.W)).asUInt
        arrayRegFiles(__tmp_1494 + 0.U) := __tmp_1495(7, 0)

        val __tmp_1496 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 13.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1497 = (98.U(8.W)).asUInt
        arrayRegFiles(__tmp_1496 + 0.U) := __tmp_1497(7, 0)

        val __tmp_1498 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 14.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1499 = (111.U(8.W)).asUInt
        arrayRegFiles(__tmp_1498 + 0.U) := __tmp_1499(7, 0)

        val __tmp_1500 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 15.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1501 = (117.U(8.W)).asUInt
        arrayRegFiles(__tmp_1500 + 0.U) := __tmp_1501(7, 0)

        val __tmp_1502 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 16.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1503 = (110.U(8.W)).asUInt
        arrayRegFiles(__tmp_1502 + 0.U) := __tmp_1503(7, 0)

        val __tmp_1504 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 17.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1505 = (100.U(8.W)).asUInt
        arrayRegFiles(__tmp_1504 + 0.U) := __tmp_1505(7, 0)

        val __tmp_1506 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 18.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1507 = (115.U(8.W)).asUInt
        arrayRegFiles(__tmp_1506 + 0.U) := __tmp_1507(7, 0)

        CP := 337.U
      }

      is(178.U) {
        /*
        *((($4: MS[Z, S16]) + (12: SP)) + ((($2: Z) as SP) * (2: SP))) = ($3: S16) [signed, S16, 2]  // ($4: MS[Z, S16])(($2: Z)) = ($3: S16)
        goto .179
        */


        val __tmp_1508 = ((generalRegFiles(4.U) + 12.U(16.W)) + (generalRegFiles(2.U).asSInt.asUInt * 2.U(16.W)))
        val __tmp_1509 = (generalRegFiles(3.U).asSInt).asUInt
        arrayRegFiles(__tmp_1508 + 0.U) := __tmp_1509(7, 0)
        arrayRegFiles(__tmp_1508 + 1.U) := __tmp_1509(15, 8)

        CP := 179.U
      }

      is(179.U) {
        /*
        undecl t: S16 @$3
        goto $ret@0
        */


        CP := Cat(
          arrayRegFiles(SP + 0.U + 1.U),
          arrayRegFiles(SP + 0.U + 0.U)
        )

      }

      is(180.U) {
        /*
        $10 = (($3: S64) ≡ (-9223372036854775808: S64))
        goto .181
        */



        generalRegFiles(10.U) := (generalRegFiles(3.U).asSInt === BigInt("-9223372036854775808").S(64.W)).asUInt

        CP := 181.U
      }

      is(181.U) {
        /*
        if ($10: B) goto .182 else goto .242
        */


        CP := Mux((generalRegFiles(10.U).asUInt) === 1.U, 182.U, 242.U)
      }

      is(182.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $12 = (($1: anvil.PrinterIndex.U) & ($2: anvil.PrinterIndex.U))
        goto .183
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(12.U) := (generalRegFiles(1.U) & generalRegFiles(2.U))

        CP := 183.U
      }

      is(183.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($12: anvil.PrinterIndex.U) as SP)) = (45: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($12: anvil.PrinterIndex.U)) = (45: U8)
        goto .184
        */


        val __tmp_1510 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(12.U).asUInt)
        val __tmp_1511 = (45.U(8.W)).asUInt
        arrayRegFiles(__tmp_1510 + 0.U) := __tmp_1511(7, 0)

        CP := 184.U
      }

      is(184.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $13 = (($1: anvil.PrinterIndex.U) + (1: anvil.PrinterIndex.U))
        goto .185
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(13.U) := (generalRegFiles(1.U) + 1.U(64.W))

        CP := 185.U
      }

      is(185.U) {
        /*
        $14 = (($13: anvil.PrinterIndex.U) & ($2: anvil.PrinterIndex.U))
        goto .186
        */



        generalRegFiles(14.U) := (generalRegFiles(13.U) & generalRegFiles(2.U))

        CP := 186.U
      }

      is(186.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($14: anvil.PrinterIndex.U) as SP)) = (57: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($14: anvil.PrinterIndex.U)) = (57: U8)
        goto .187
        */


        val __tmp_1512 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(14.U).asUInt)
        val __tmp_1513 = (57.U(8.W)).asUInt
        arrayRegFiles(__tmp_1512 + 0.U) := __tmp_1513(7, 0)

        CP := 187.U
      }

      is(187.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $13 = (($1: anvil.PrinterIndex.U) + (2: anvil.PrinterIndex.U))
        goto .188
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(13.U) := (generalRegFiles(1.U) + 2.U(64.W))

        CP := 188.U
      }

      is(188.U) {
        /*
        $14 = (($13: anvil.PrinterIndex.U) & ($2: anvil.PrinterIndex.U))
        goto .189
        */



        generalRegFiles(14.U) := (generalRegFiles(13.U) & generalRegFiles(2.U))

        CP := 189.U
      }

      is(189.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($14: anvil.PrinterIndex.U) as SP)) = (50: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($14: anvil.PrinterIndex.U)) = (50: U8)
        goto .190
        */


        val __tmp_1514 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(14.U).asUInt)
        val __tmp_1515 = (50.U(8.W)).asUInt
        arrayRegFiles(__tmp_1514 + 0.U) := __tmp_1515(7, 0)

        CP := 190.U
      }

      is(190.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $13 = (($1: anvil.PrinterIndex.U) + (3: anvil.PrinterIndex.U))
        goto .191
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(13.U) := (generalRegFiles(1.U) + 3.U(64.W))

        CP := 191.U
      }

      is(191.U) {
        /*
        $14 = (($13: anvil.PrinterIndex.U) & ($2: anvil.PrinterIndex.U))
        goto .192
        */



        generalRegFiles(14.U) := (generalRegFiles(13.U) & generalRegFiles(2.U))

        CP := 192.U
      }

      is(192.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($14: anvil.PrinterIndex.U) as SP)) = (50: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($14: anvil.PrinterIndex.U)) = (50: U8)
        goto .193
        */


        val __tmp_1516 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(14.U).asUInt)
        val __tmp_1517 = (50.U(8.W)).asUInt
        arrayRegFiles(__tmp_1516 + 0.U) := __tmp_1517(7, 0)

        CP := 193.U
      }

      is(193.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $13 = (($1: anvil.PrinterIndex.U) + (4: anvil.PrinterIndex.U))
        goto .194
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(13.U) := (generalRegFiles(1.U) + 4.U(64.W))

        CP := 194.U
      }

      is(194.U) {
        /*
        $14 = (($13: anvil.PrinterIndex.U) & ($2: anvil.PrinterIndex.U))
        goto .195
        */



        generalRegFiles(14.U) := (generalRegFiles(13.U) & generalRegFiles(2.U))

        CP := 195.U
      }

      is(195.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($14: anvil.PrinterIndex.U) as SP)) = (51: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($14: anvil.PrinterIndex.U)) = (51: U8)
        goto .196
        */


        val __tmp_1518 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(14.U).asUInt)
        val __tmp_1519 = (51.U(8.W)).asUInt
        arrayRegFiles(__tmp_1518 + 0.U) := __tmp_1519(7, 0)

        CP := 196.U
      }

      is(196.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $13 = (($1: anvil.PrinterIndex.U) + (5: anvil.PrinterIndex.U))
        goto .197
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(13.U) := (generalRegFiles(1.U) + 5.U(64.W))

        CP := 197.U
      }

      is(197.U) {
        /*
        $14 = (($13: anvil.PrinterIndex.U) & ($2: anvil.PrinterIndex.U))
        goto .198
        */



        generalRegFiles(14.U) := (generalRegFiles(13.U) & generalRegFiles(2.U))

        CP := 198.U
      }

      is(198.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($14: anvil.PrinterIndex.U) as SP)) = (51: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($14: anvil.PrinterIndex.U)) = (51: U8)
        goto .199
        */


        val __tmp_1520 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(14.U).asUInt)
        val __tmp_1521 = (51.U(8.W)).asUInt
        arrayRegFiles(__tmp_1520 + 0.U) := __tmp_1521(7, 0)

        CP := 199.U
      }

      is(199.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $13 = (($1: anvil.PrinterIndex.U) + (6: anvil.PrinterIndex.U))
        goto .200
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(13.U) := (generalRegFiles(1.U) + 6.U(64.W))

        CP := 200.U
      }

      is(200.U) {
        /*
        $14 = (($13: anvil.PrinterIndex.U) & ($2: anvil.PrinterIndex.U))
        goto .201
        */



        generalRegFiles(14.U) := (generalRegFiles(13.U) & generalRegFiles(2.U))

        CP := 201.U
      }

      is(201.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($14: anvil.PrinterIndex.U) as SP)) = (55: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($14: anvil.PrinterIndex.U)) = (55: U8)
        goto .202
        */


        val __tmp_1522 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(14.U).asUInt)
        val __tmp_1523 = (55.U(8.W)).asUInt
        arrayRegFiles(__tmp_1522 + 0.U) := __tmp_1523(7, 0)

        CP := 202.U
      }

      is(202.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $13 = (($1: anvil.PrinterIndex.U) + (7: anvil.PrinterIndex.U))
        goto .203
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(13.U) := (generalRegFiles(1.U) + 7.U(64.W))

        CP := 203.U
      }

      is(203.U) {
        /*
        $14 = (($13: anvil.PrinterIndex.U) & ($2: anvil.PrinterIndex.U))
        goto .204
        */



        generalRegFiles(14.U) := (generalRegFiles(13.U) & generalRegFiles(2.U))

        CP := 204.U
      }

      is(204.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($14: anvil.PrinterIndex.U) as SP)) = (50: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($14: anvil.PrinterIndex.U)) = (50: U8)
        goto .205
        */


        val __tmp_1524 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(14.U).asUInt)
        val __tmp_1525 = (50.U(8.W)).asUInt
        arrayRegFiles(__tmp_1524 + 0.U) := __tmp_1525(7, 0)

        CP := 205.U
      }

      is(205.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $13 = (($1: anvil.PrinterIndex.U) + (8: anvil.PrinterIndex.U))
        goto .206
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(13.U) := (generalRegFiles(1.U) + 8.U(64.W))

        CP := 206.U
      }

      is(206.U) {
        /*
        $14 = (($13: anvil.PrinterIndex.U) & ($2: anvil.PrinterIndex.U))
        goto .207
        */



        generalRegFiles(14.U) := (generalRegFiles(13.U) & generalRegFiles(2.U))

        CP := 207.U
      }

      is(207.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($14: anvil.PrinterIndex.U) as SP)) = (48: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($14: anvil.PrinterIndex.U)) = (48: U8)
        goto .208
        */


        val __tmp_1526 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(14.U).asUInt)
        val __tmp_1527 = (48.U(8.W)).asUInt
        arrayRegFiles(__tmp_1526 + 0.U) := __tmp_1527(7, 0)

        CP := 208.U
      }

      is(208.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $13 = (($1: anvil.PrinterIndex.U) + (9: anvil.PrinterIndex.U))
        goto .209
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(13.U) := (generalRegFiles(1.U) + 9.U(64.W))

        CP := 209.U
      }

      is(209.U) {
        /*
        $14 = (($13: anvil.PrinterIndex.U) & ($2: anvil.PrinterIndex.U))
        goto .210
        */



        generalRegFiles(14.U) := (generalRegFiles(13.U) & generalRegFiles(2.U))

        CP := 210.U
      }

      is(210.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($14: anvil.PrinterIndex.U) as SP)) = (51: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($14: anvil.PrinterIndex.U)) = (51: U8)
        goto .211
        */


        val __tmp_1528 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(14.U).asUInt)
        val __tmp_1529 = (51.U(8.W)).asUInt
        arrayRegFiles(__tmp_1528 + 0.U) := __tmp_1529(7, 0)

        CP := 211.U
      }

      is(211.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $13 = (($1: anvil.PrinterIndex.U) + (10: anvil.PrinterIndex.U))
        goto .212
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(13.U) := (generalRegFiles(1.U) + 10.U(64.W))

        CP := 212.U
      }

      is(212.U) {
        /*
        $14 = (($13: anvil.PrinterIndex.U) & ($2: anvil.PrinterIndex.U))
        goto .213
        */



        generalRegFiles(14.U) := (generalRegFiles(13.U) & generalRegFiles(2.U))

        CP := 213.U
      }

      is(213.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($14: anvil.PrinterIndex.U) as SP)) = (54: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($14: anvil.PrinterIndex.U)) = (54: U8)
        goto .214
        */


        val __tmp_1530 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(14.U).asUInt)
        val __tmp_1531 = (54.U(8.W)).asUInt
        arrayRegFiles(__tmp_1530 + 0.U) := __tmp_1531(7, 0)

        CP := 214.U
      }

      is(214.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $13 = (($1: anvil.PrinterIndex.U) + (11: anvil.PrinterIndex.U))
        goto .215
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(13.U) := (generalRegFiles(1.U) + 11.U(64.W))

        CP := 215.U
      }

      is(215.U) {
        /*
        $14 = (($13: anvil.PrinterIndex.U) & ($2: anvil.PrinterIndex.U))
        goto .216
        */



        generalRegFiles(14.U) := (generalRegFiles(13.U) & generalRegFiles(2.U))

        CP := 216.U
      }

      is(216.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($14: anvil.PrinterIndex.U) as SP)) = (56: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($14: anvil.PrinterIndex.U)) = (56: U8)
        goto .217
        */


        val __tmp_1532 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(14.U).asUInt)
        val __tmp_1533 = (56.U(8.W)).asUInt
        arrayRegFiles(__tmp_1532 + 0.U) := __tmp_1533(7, 0)

        CP := 217.U
      }

      is(217.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $13 = (($1: anvil.PrinterIndex.U) + (12: anvil.PrinterIndex.U))
        goto .218
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(13.U) := (generalRegFiles(1.U) + 12.U(64.W))

        CP := 218.U
      }

      is(218.U) {
        /*
        $14 = (($13: anvil.PrinterIndex.U) & ($2: anvil.PrinterIndex.U))
        goto .219
        */



        generalRegFiles(14.U) := (generalRegFiles(13.U) & generalRegFiles(2.U))

        CP := 219.U
      }

      is(219.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($14: anvil.PrinterIndex.U) as SP)) = (53: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($14: anvil.PrinterIndex.U)) = (53: U8)
        goto .220
        */


        val __tmp_1534 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(14.U).asUInt)
        val __tmp_1535 = (53.U(8.W)).asUInt
        arrayRegFiles(__tmp_1534 + 0.U) := __tmp_1535(7, 0)

        CP := 220.U
      }

      is(220.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $13 = (($1: anvil.PrinterIndex.U) + (13: anvil.PrinterIndex.U))
        goto .221
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(13.U) := (generalRegFiles(1.U) + 13.U(64.W))

        CP := 221.U
      }

      is(221.U) {
        /*
        $14 = (($13: anvil.PrinterIndex.U) & ($2: anvil.PrinterIndex.U))
        goto .222
        */



        generalRegFiles(14.U) := (generalRegFiles(13.U) & generalRegFiles(2.U))

        CP := 222.U
      }

      is(222.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($14: anvil.PrinterIndex.U) as SP)) = (52: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($14: anvil.PrinterIndex.U)) = (52: U8)
        goto .223
        */


        val __tmp_1536 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(14.U).asUInt)
        val __tmp_1537 = (52.U(8.W)).asUInt
        arrayRegFiles(__tmp_1536 + 0.U) := __tmp_1537(7, 0)

        CP := 223.U
      }

      is(223.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $13 = (($1: anvil.PrinterIndex.U) + (14: anvil.PrinterIndex.U))
        goto .224
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(13.U) := (generalRegFiles(1.U) + 14.U(64.W))

        CP := 224.U
      }

      is(224.U) {
        /*
        $14 = (($13: anvil.PrinterIndex.U) & ($2: anvil.PrinterIndex.U))
        goto .225
        */



        generalRegFiles(14.U) := (generalRegFiles(13.U) & generalRegFiles(2.U))

        CP := 225.U
      }

      is(225.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($14: anvil.PrinterIndex.U) as SP)) = (55: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($14: anvil.PrinterIndex.U)) = (55: U8)
        goto .226
        */


        val __tmp_1538 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(14.U).asUInt)
        val __tmp_1539 = (55.U(8.W)).asUInt
        arrayRegFiles(__tmp_1538 + 0.U) := __tmp_1539(7, 0)

        CP := 226.U
      }

      is(226.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $13 = (($1: anvil.PrinterIndex.U) + (15: anvil.PrinterIndex.U))
        goto .227
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(13.U) := (generalRegFiles(1.U) + 15.U(64.W))

        CP := 227.U
      }

      is(227.U) {
        /*
        $14 = (($13: anvil.PrinterIndex.U) & ($2: anvil.PrinterIndex.U))
        goto .228
        */



        generalRegFiles(14.U) := (generalRegFiles(13.U) & generalRegFiles(2.U))

        CP := 228.U
      }

      is(228.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($14: anvil.PrinterIndex.U) as SP)) = (55: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($14: anvil.PrinterIndex.U)) = (55: U8)
        goto .229
        */


        val __tmp_1540 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(14.U).asUInt)
        val __tmp_1541 = (55.U(8.W)).asUInt
        arrayRegFiles(__tmp_1540 + 0.U) := __tmp_1541(7, 0)

        CP := 229.U
      }

      is(229.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $13 = (($1: anvil.PrinterIndex.U) + (16: anvil.PrinterIndex.U))
        goto .230
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(13.U) := (generalRegFiles(1.U) + 16.U(64.W))

        CP := 230.U
      }

      is(230.U) {
        /*
        $14 = (($13: anvil.PrinterIndex.U) & ($2: anvil.PrinterIndex.U))
        goto .231
        */



        generalRegFiles(14.U) := (generalRegFiles(13.U) & generalRegFiles(2.U))

        CP := 231.U
      }

      is(231.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($14: anvil.PrinterIndex.U) as SP)) = (53: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($14: anvil.PrinterIndex.U)) = (53: U8)
        goto .232
        */


        val __tmp_1542 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(14.U).asUInt)
        val __tmp_1543 = (53.U(8.W)).asUInt
        arrayRegFiles(__tmp_1542 + 0.U) := __tmp_1543(7, 0)

        CP := 232.U
      }

      is(232.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $13 = (($1: anvil.PrinterIndex.U) + (17: anvil.PrinterIndex.U))
        goto .233
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(13.U) := (generalRegFiles(1.U) + 17.U(64.W))

        CP := 233.U
      }

      is(233.U) {
        /*
        $14 = (($13: anvil.PrinterIndex.U) & ($2: anvil.PrinterIndex.U))
        goto .234
        */



        generalRegFiles(14.U) := (generalRegFiles(13.U) & generalRegFiles(2.U))

        CP := 234.U
      }

      is(234.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($14: anvil.PrinterIndex.U) as SP)) = (56: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($14: anvil.PrinterIndex.U)) = (56: U8)
        goto .235
        */


        val __tmp_1544 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(14.U).asUInt)
        val __tmp_1545 = (56.U(8.W)).asUInt
        arrayRegFiles(__tmp_1544 + 0.U) := __tmp_1545(7, 0)

        CP := 235.U
      }

      is(235.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $13 = (($1: anvil.PrinterIndex.U) + (18: anvil.PrinterIndex.U))
        goto .236
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(13.U) := (generalRegFiles(1.U) + 18.U(64.W))

        CP := 236.U
      }

      is(236.U) {
        /*
        $14 = (($13: anvil.PrinterIndex.U) & ($2: anvil.PrinterIndex.U))
        goto .237
        */



        generalRegFiles(14.U) := (generalRegFiles(13.U) & generalRegFiles(2.U))

        CP := 237.U
      }

      is(237.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($14: anvil.PrinterIndex.U) as SP)) = (48: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($14: anvil.PrinterIndex.U)) = (48: U8)
        goto .238
        */


        val __tmp_1546 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(14.U).asUInt)
        val __tmp_1547 = (48.U(8.W)).asUInt
        arrayRegFiles(__tmp_1546 + 0.U) := __tmp_1547(7, 0)

        CP := 238.U
      }

      is(238.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $13 = (($1: anvil.PrinterIndex.U) + (19: anvil.PrinterIndex.U))
        goto .239
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(13.U) := (generalRegFiles(1.U) + 19.U(64.W))

        CP := 239.U
      }

      is(239.U) {
        /*
        $14 = (($13: anvil.PrinterIndex.U) & ($2: anvil.PrinterIndex.U))
        goto .240
        */



        generalRegFiles(14.U) := (generalRegFiles(13.U) & generalRegFiles(2.U))

        CP := 240.U
      }

      is(240.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($14: anvil.PrinterIndex.U) as SP)) = (56: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($14: anvil.PrinterIndex.U)) = (56: U8)
        goto .241
        */


        val __tmp_1548 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(14.U).asUInt)
        val __tmp_1549 = (56.U(8.W)).asUInt
        arrayRegFiles(__tmp_1548 + 0.U) := __tmp_1549(7, 0)

        CP := 241.U
      }

      is(241.U) {
        /*
        **(SP + (2: SP)) = (20: U64) [unsigned, U64, 8]  // $res = (20: U64)
        goto $ret@0
        */


        val __tmp_1550 = Cat(
          arrayRegFiles((SP + 2.U(16.W)) + 1.U),
          arrayRegFiles((SP + 2.U(16.W)) + 0.U)
        )
        val __tmp_1551 = (20.U(64.W)).asUInt
        arrayRegFiles(__tmp_1550 + 0.U) := __tmp_1551(7, 0)
        arrayRegFiles(__tmp_1550 + 1.U) := __tmp_1551(15, 8)
        arrayRegFiles(__tmp_1550 + 2.U) := __tmp_1551(23, 16)
        arrayRegFiles(__tmp_1550 + 3.U) := __tmp_1551(31, 24)
        arrayRegFiles(__tmp_1550 + 4.U) := __tmp_1551(39, 32)
        arrayRegFiles(__tmp_1550 + 5.U) := __tmp_1551(47, 40)
        arrayRegFiles(__tmp_1550 + 6.U) := __tmp_1551(55, 48)
        arrayRegFiles(__tmp_1550 + 7.U) := __tmp_1551(63, 56)

        CP := Cat(
          arrayRegFiles(SP + 0.U + 1.U),
          arrayRegFiles(SP + 0.U + 0.U)
        )

      }

      is(242.U) {
        /*
        $10 = (($3: S64) ≡ (0: S64))
        goto .243
        */



        generalRegFiles(10.U) := (generalRegFiles(3.U).asSInt === 0.S(64.W)).asUInt

        CP := 243.U
      }

      is(243.U) {
        /*
        if ($10: B) goto .244 else goto .247
        */


        CP := Mux((generalRegFiles(10.U).asUInt) === 1.U, 244.U, 247.U)
      }

      is(244.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $12 = (($1: anvil.PrinterIndex.U) & ($2: anvil.PrinterIndex.U))
        goto .245
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(12.U) := (generalRegFiles(1.U) & generalRegFiles(2.U))

        CP := 245.U
      }

      is(245.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($12: anvil.PrinterIndex.U) as SP)) = (48: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($12: anvil.PrinterIndex.U)) = (48: U8)
        goto .246
        */


        val __tmp_1552 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(12.U).asUInt)
        val __tmp_1553 = (48.U(8.W)).asUInt
        arrayRegFiles(__tmp_1552 + 0.U) := __tmp_1553(7, 0)

        CP := 246.U
      }

      is(246.U) {
        /*
        **(SP + (2: SP)) = (1: U64) [unsigned, U64, 8]  // $res = (1: U64)
        goto $ret@0
        */


        val __tmp_1554 = Cat(
          arrayRegFiles((SP + 2.U(16.W)) + 1.U),
          arrayRegFiles((SP + 2.U(16.W)) + 0.U)
        )
        val __tmp_1555 = (1.U(64.W)).asUInt
        arrayRegFiles(__tmp_1554 + 0.U) := __tmp_1555(7, 0)
        arrayRegFiles(__tmp_1554 + 1.U) := __tmp_1555(15, 8)
        arrayRegFiles(__tmp_1554 + 2.U) := __tmp_1555(23, 16)
        arrayRegFiles(__tmp_1554 + 3.U) := __tmp_1555(31, 24)
        arrayRegFiles(__tmp_1554 + 4.U) := __tmp_1555(39, 32)
        arrayRegFiles(__tmp_1554 + 5.U) := __tmp_1555(47, 40)
        arrayRegFiles(__tmp_1554 + 6.U) := __tmp_1555(55, 48)
        arrayRegFiles(__tmp_1554 + 7.U) := __tmp_1555(63, 56)

        CP := Cat(
          arrayRegFiles(SP + 0.U + 1.U),
          arrayRegFiles(SP + 0.U + 0.U)
        )

      }

      is(247.U) {
        /*
        decl buff: MS[anvil.PrinterIndex.I20, U8] [@4, 34]
        alloc $new@[168,16].5BB7E063: MS[anvil.PrinterIndex.I20, U8] [@38, 34]
        $11 = (SP + (38: SP))
        *(SP + (38: SP)) = (323602724: U32) [unsigned, U32, 4]  // sha3 type signature of MS[anvil.PrinterIndex.I20, U8]: 0x1349C924
        *(SP + (42: SP)) = (20: Z) [signed, Z, 8]  // size of MS[anvil.PrinterIndex.I20, U8]((0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8))
        goto .248
        */



        generalRegFiles(11.U) := (SP + 38.U(16.W))

        val __tmp_1556 = (SP + 38.U(16.W))
        val __tmp_1557 = (323602724.U(32.W)).asUInt
        arrayRegFiles(__tmp_1556 + 0.U) := __tmp_1557(7, 0)
        arrayRegFiles(__tmp_1556 + 1.U) := __tmp_1557(15, 8)
        arrayRegFiles(__tmp_1556 + 2.U) := __tmp_1557(23, 16)
        arrayRegFiles(__tmp_1556 + 3.U) := __tmp_1557(31, 24)

        val __tmp_1558 = (SP + 42.U(16.W))
        val __tmp_1559 = (20.S(64.W)).asUInt
        arrayRegFiles(__tmp_1558 + 0.U) := __tmp_1559(7, 0)
        arrayRegFiles(__tmp_1558 + 1.U) := __tmp_1559(15, 8)
        arrayRegFiles(__tmp_1558 + 2.U) := __tmp_1559(23, 16)
        arrayRegFiles(__tmp_1558 + 3.U) := __tmp_1559(31, 24)
        arrayRegFiles(__tmp_1558 + 4.U) := __tmp_1559(39, 32)
        arrayRegFiles(__tmp_1558 + 5.U) := __tmp_1559(47, 40)
        arrayRegFiles(__tmp_1558 + 6.U) := __tmp_1559(55, 48)
        arrayRegFiles(__tmp_1558 + 7.U) := __tmp_1559(63, 56)

        CP := 248.U
      }

      is(248.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.I20, U8]) + (12: SP)) + ((0: anvil.PrinterIndex.I20) as SP)) = (0: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.I20, U8])((0: anvil.PrinterIndex.I20)) = (0: U8)
        *((($11: MS[anvil.PrinterIndex.I20, U8]) + (12: SP)) + ((1: anvil.PrinterIndex.I20) as SP)) = (0: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.I20, U8])((1: anvil.PrinterIndex.I20)) = (0: U8)
        *((($11: MS[anvil.PrinterIndex.I20, U8]) + (12: SP)) + ((2: anvil.PrinterIndex.I20) as SP)) = (0: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.I20, U8])((2: anvil.PrinterIndex.I20)) = (0: U8)
        *((($11: MS[anvil.PrinterIndex.I20, U8]) + (12: SP)) + ((3: anvil.PrinterIndex.I20) as SP)) = (0: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.I20, U8])((3: anvil.PrinterIndex.I20)) = (0: U8)
        *((($11: MS[anvil.PrinterIndex.I20, U8]) + (12: SP)) + ((4: anvil.PrinterIndex.I20) as SP)) = (0: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.I20, U8])((4: anvil.PrinterIndex.I20)) = (0: U8)
        *((($11: MS[anvil.PrinterIndex.I20, U8]) + (12: SP)) + ((5: anvil.PrinterIndex.I20) as SP)) = (0: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.I20, U8])((5: anvil.PrinterIndex.I20)) = (0: U8)
        *((($11: MS[anvil.PrinterIndex.I20, U8]) + (12: SP)) + ((6: anvil.PrinterIndex.I20) as SP)) = (0: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.I20, U8])((6: anvil.PrinterIndex.I20)) = (0: U8)
        *((($11: MS[anvil.PrinterIndex.I20, U8]) + (12: SP)) + ((7: anvil.PrinterIndex.I20) as SP)) = (0: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.I20, U8])((7: anvil.PrinterIndex.I20)) = (0: U8)
        *((($11: MS[anvil.PrinterIndex.I20, U8]) + (12: SP)) + ((8: anvil.PrinterIndex.I20) as SP)) = (0: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.I20, U8])((8: anvil.PrinterIndex.I20)) = (0: U8)
        *((($11: MS[anvil.PrinterIndex.I20, U8]) + (12: SP)) + ((9: anvil.PrinterIndex.I20) as SP)) = (0: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.I20, U8])((9: anvil.PrinterIndex.I20)) = (0: U8)
        *((($11: MS[anvil.PrinterIndex.I20, U8]) + (12: SP)) + ((10: anvil.PrinterIndex.I20) as SP)) = (0: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.I20, U8])((10: anvil.PrinterIndex.I20)) = (0: U8)
        *((($11: MS[anvil.PrinterIndex.I20, U8]) + (12: SP)) + ((11: anvil.PrinterIndex.I20) as SP)) = (0: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.I20, U8])((11: anvil.PrinterIndex.I20)) = (0: U8)
        *((($11: MS[anvil.PrinterIndex.I20, U8]) + (12: SP)) + ((12: anvil.PrinterIndex.I20) as SP)) = (0: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.I20, U8])((12: anvil.PrinterIndex.I20)) = (0: U8)
        *((($11: MS[anvil.PrinterIndex.I20, U8]) + (12: SP)) + ((13: anvil.PrinterIndex.I20) as SP)) = (0: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.I20, U8])((13: anvil.PrinterIndex.I20)) = (0: U8)
        *((($11: MS[anvil.PrinterIndex.I20, U8]) + (12: SP)) + ((14: anvil.PrinterIndex.I20) as SP)) = (0: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.I20, U8])((14: anvil.PrinterIndex.I20)) = (0: U8)
        *((($11: MS[anvil.PrinterIndex.I20, U8]) + (12: SP)) + ((15: anvil.PrinterIndex.I20) as SP)) = (0: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.I20, U8])((15: anvil.PrinterIndex.I20)) = (0: U8)
        *((($11: MS[anvil.PrinterIndex.I20, U8]) + (12: SP)) + ((16: anvil.PrinterIndex.I20) as SP)) = (0: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.I20, U8])((16: anvil.PrinterIndex.I20)) = (0: U8)
        *((($11: MS[anvil.PrinterIndex.I20, U8]) + (12: SP)) + ((17: anvil.PrinterIndex.I20) as SP)) = (0: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.I20, U8])((17: anvil.PrinterIndex.I20)) = (0: U8)
        *((($11: MS[anvil.PrinterIndex.I20, U8]) + (12: SP)) + ((18: anvil.PrinterIndex.I20) as SP)) = (0: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.I20, U8])((18: anvil.PrinterIndex.I20)) = (0: U8)
        *((($11: MS[anvil.PrinterIndex.I20, U8]) + (12: SP)) + ((19: anvil.PrinterIndex.I20) as SP)) = (0: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.I20, U8])((19: anvil.PrinterIndex.I20)) = (0: U8)
        goto .249
        */


        val __tmp_1560 = ((generalRegFiles(11.U) + 12.U(16.W)) + 0.S(8.W).asUInt)
        val __tmp_1561 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_1560 + 0.U) := __tmp_1561(7, 0)

        val __tmp_1562 = ((generalRegFiles(11.U) + 12.U(16.W)) + 1.S(8.W).asUInt)
        val __tmp_1563 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_1562 + 0.U) := __tmp_1563(7, 0)

        val __tmp_1564 = ((generalRegFiles(11.U) + 12.U(16.W)) + 2.S(8.W).asUInt)
        val __tmp_1565 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_1564 + 0.U) := __tmp_1565(7, 0)

        val __tmp_1566 = ((generalRegFiles(11.U) + 12.U(16.W)) + 3.S(8.W).asUInt)
        val __tmp_1567 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_1566 + 0.U) := __tmp_1567(7, 0)

        val __tmp_1568 = ((generalRegFiles(11.U) + 12.U(16.W)) + 4.S(8.W).asUInt)
        val __tmp_1569 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_1568 + 0.U) := __tmp_1569(7, 0)

        val __tmp_1570 = ((generalRegFiles(11.U) + 12.U(16.W)) + 5.S(8.W).asUInt)
        val __tmp_1571 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_1570 + 0.U) := __tmp_1571(7, 0)

        val __tmp_1572 = ((generalRegFiles(11.U) + 12.U(16.W)) + 6.S(8.W).asUInt)
        val __tmp_1573 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_1572 + 0.U) := __tmp_1573(7, 0)

        val __tmp_1574 = ((generalRegFiles(11.U) + 12.U(16.W)) + 7.S(8.W).asUInt)
        val __tmp_1575 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_1574 + 0.U) := __tmp_1575(7, 0)

        val __tmp_1576 = ((generalRegFiles(11.U) + 12.U(16.W)) + 8.S(8.W).asUInt)
        val __tmp_1577 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_1576 + 0.U) := __tmp_1577(7, 0)

        val __tmp_1578 = ((generalRegFiles(11.U) + 12.U(16.W)) + 9.S(8.W).asUInt)
        val __tmp_1579 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_1578 + 0.U) := __tmp_1579(7, 0)

        val __tmp_1580 = ((generalRegFiles(11.U) + 12.U(16.W)) + 10.S(8.W).asUInt)
        val __tmp_1581 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_1580 + 0.U) := __tmp_1581(7, 0)

        val __tmp_1582 = ((generalRegFiles(11.U) + 12.U(16.W)) + 11.S(8.W).asUInt)
        val __tmp_1583 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_1582 + 0.U) := __tmp_1583(7, 0)

        val __tmp_1584 = ((generalRegFiles(11.U) + 12.U(16.W)) + 12.S(8.W).asUInt)
        val __tmp_1585 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_1584 + 0.U) := __tmp_1585(7, 0)

        val __tmp_1586 = ((generalRegFiles(11.U) + 12.U(16.W)) + 13.S(8.W).asUInt)
        val __tmp_1587 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_1586 + 0.U) := __tmp_1587(7, 0)

        val __tmp_1588 = ((generalRegFiles(11.U) + 12.U(16.W)) + 14.S(8.W).asUInt)
        val __tmp_1589 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_1588 + 0.U) := __tmp_1589(7, 0)

        val __tmp_1590 = ((generalRegFiles(11.U) + 12.U(16.W)) + 15.S(8.W).asUInt)
        val __tmp_1591 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_1590 + 0.U) := __tmp_1591(7, 0)

        val __tmp_1592 = ((generalRegFiles(11.U) + 12.U(16.W)) + 16.S(8.W).asUInt)
        val __tmp_1593 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_1592 + 0.U) := __tmp_1593(7, 0)

        val __tmp_1594 = ((generalRegFiles(11.U) + 12.U(16.W)) + 17.S(8.W).asUInt)
        val __tmp_1595 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_1594 + 0.U) := __tmp_1595(7, 0)

        val __tmp_1596 = ((generalRegFiles(11.U) + 12.U(16.W)) + 18.S(8.W).asUInt)
        val __tmp_1597 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_1596 + 0.U) := __tmp_1597(7, 0)

        val __tmp_1598 = ((generalRegFiles(11.U) + 12.U(16.W)) + 19.S(8.W).asUInt)
        val __tmp_1599 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_1598 + 0.U) := __tmp_1599(7, 0)

        CP := 249.U
      }

      is(249.U) {
        /*
        (SP + (4: SP)) [MS[anvil.PrinterIndex.I20, U8], 34]  <-  ($11: MS[anvil.PrinterIndex.I20, U8]) [MS[anvil.PrinterIndex.I20, U8], ((*(($11: MS[anvil.PrinterIndex.I20, U8]) + (4: SP)) as SP) + (12: SP))]  // buff = ($11: MS[anvil.PrinterIndex.I20, U8])
        goto .250
        */


        val __tmp_1600 = (SP + 4.U(16.W))
        val __tmp_1601 = generalRegFiles(11.U)
        val __tmp_1602 = (Cat(
           arrayRegFiles((generalRegFiles(11.U) + 4.U(16.W)) + 7.U),
           arrayRegFiles((generalRegFiles(11.U) + 4.U(16.W)) + 6.U),
           arrayRegFiles((generalRegFiles(11.U) + 4.U(16.W)) + 5.U),
           arrayRegFiles((generalRegFiles(11.U) + 4.U(16.W)) + 4.U),
           arrayRegFiles((generalRegFiles(11.U) + 4.U(16.W)) + 3.U),
           arrayRegFiles((generalRegFiles(11.U) + 4.U(16.W)) + 2.U),
           arrayRegFiles((generalRegFiles(11.U) + 4.U(16.W)) + 1.U),
           arrayRegFiles((generalRegFiles(11.U) + 4.U(16.W)) + 0.U)
         ).asSInt.asUInt + 12.U(16.W))

        when(Idx < __tmp_1602) {
          arrayRegFiles(__tmp_1600 + Idx + 0.U) := arrayRegFiles(__tmp_1601 + Idx + 0.U)
          arrayRegFiles(__tmp_1600 + Idx + 1.U) := arrayRegFiles(__tmp_1601 + Idx + 1.U)
          arrayRegFiles(__tmp_1600 + Idx + 2.U) := arrayRegFiles(__tmp_1601 + Idx + 2.U)
          arrayRegFiles(__tmp_1600 + Idx + 3.U) := arrayRegFiles(__tmp_1601 + Idx + 3.U)
          arrayRegFiles(__tmp_1600 + Idx + 4.U) := arrayRegFiles(__tmp_1601 + Idx + 4.U)
          arrayRegFiles(__tmp_1600 + Idx + 5.U) := arrayRegFiles(__tmp_1601 + Idx + 5.U)
          arrayRegFiles(__tmp_1600 + Idx + 6.U) := arrayRegFiles(__tmp_1601 + Idx + 6.U)
          arrayRegFiles(__tmp_1600 + Idx + 7.U) := arrayRegFiles(__tmp_1601 + Idx + 7.U)
          Idx := Idx + 8.U
          LeftByteRounds := __tmp_1602 - Idx
        } .elsewhen(IdxLeftByteRounds < LeftByteRounds) {
          val __tmp_1603 = Idx - 8.U
          arrayRegFiles(__tmp_1600 + __tmp_1603 + IdxLeftByteRounds) := arrayRegFiles(__tmp_1601 + __tmp_1603 + IdxLeftByteRounds)
          IdxLeftByteRounds := IdxLeftByteRounds + 1.U
        } .otherwise {
          Idx := 0.U
          IdxLeftByteRounds := 0.U
          LeftByteRounds := 0.U
          CP := 250.U
        }


      }

      is(250.U) {
        /*
        unalloc $new@[168,16].5BB7E063: MS[anvil.PrinterIndex.I20, U8] [@38, 34]
        goto .251
        */


        CP := 251.U
      }

      is(251.U) {
        /*
        decl i: anvil.PrinterIndex.I20 @$4
        $4 = (0: anvil.PrinterIndex.I20)
        goto .252
        */



        generalRegFiles(4.U) := (0.S(8.W)).asUInt

        CP := 252.U
      }

      is(252.U) {
        /*
        decl neg: B @$5
        $10 = (($3: S64) < (0: S64))
        goto .253
        */



        generalRegFiles(10.U) := (generalRegFiles(3.U).asSInt < 0.S(64.W)).asUInt

        CP := 253.U
      }

      is(253.U) {
        /*
        $5 = ($10: B)
        goto .254
        */



        generalRegFiles(5.U) := generalRegFiles(10.U)

        CP := 254.U
      }

      is(254.U) {
        /*
        decl m: S64 @$6
        goto .255
        */


        CP := 255.U
      }

      is(255.U) {
        /*
        if ($5: B) goto .256 else goto .258
        */


        CP := Mux((generalRegFiles(5.U).asUInt) === 1.U, 256.U, 258.U)
      }

      is(256.U) {
        /*
        $12 = -(($3: S64))
        goto .257
        */



        generalRegFiles(12.U) := (-generalRegFiles(3.U).asSInt).asUInt

        CP := 257.U
      }

      is(257.U) {
        /*
        $10 = ($12: S64)
        goto .260
        */



        generalRegFiles(10.U) := (generalRegFiles(12.U).asSInt).asUInt

        CP := 260.U
      }

      is(258.U) {
        /*
        $14 = ($3: S64)
        goto .259
        */



        generalRegFiles(14.U) := (generalRegFiles(3.U).asSInt).asUInt

        CP := 259.U
      }

      is(259.U) {
        /*
        $10 = ($14: S64)
        goto .260
        */



        generalRegFiles(10.U) := (generalRegFiles(14.U).asSInt).asUInt

        CP := 260.U
      }

      is(260.U) {
        /*
        $6 = ($10: S64)
        goto .261
        */



        generalRegFiles(6.U) := (generalRegFiles(10.U).asSInt).asUInt

        CP := 261.U
      }

      is(261.U) {
        /*
        $11 = ($6: S64)
        goto .262
        */



        generalRegFiles(11.U) := (generalRegFiles(6.U).asSInt).asUInt

        CP := 262.U
      }

      is(262.U) {
        /*
        $10 = (($11: S64) > (0: S64))
        goto .263
        */



        generalRegFiles(10.U) := (generalRegFiles(11.U).asSInt > 0.S(64.W)).asUInt

        CP := 263.U
      }

      is(263.U) {
        /*
        if ($10: B) goto .264 else goto .293
        */


        CP := Mux((generalRegFiles(10.U).asUInt) === 1.U, 264.U, 293.U)
      }

      is(264.U) {
        /*
        $11 = ($6: S64)
        goto .265
        */



        generalRegFiles(11.U) := (generalRegFiles(6.U).asSInt).asUInt

        CP := 265.U
      }

      is(265.U) {
        /*
        $10 = (($11: S64) % (10: S64))
        goto .266
        */



        generalRegFiles(10.U) := ((generalRegFiles(11.U).asSInt % 10.S(64.W))).asUInt

        CP := 266.U
      }

      is(266.U) {
        /*
        switch (($10: S64))
          (0: S64): goto 267
          (1: S64): goto 269
          (2: S64): goto 271
          (3: S64): goto 273
          (4: S64): goto 275
          (5: S64): goto 277
          (6: S64): goto 279
          (7: S64): goto 281
          (8: S64): goto 283
          (9: S64): goto 285

        */


        val __tmp_1604 = generalRegFiles(10.U).asSInt

        switch(__tmp_1604) {

          is(0.S(64.W)) {
            CP := 267.U
          }


          is(1.S(64.W)) {
            CP := 269.U
          }


          is(2.S(64.W)) {
            CP := 271.U
          }


          is(3.S(64.W)) {
            CP := 273.U
          }


          is(4.S(64.W)) {
            CP := 275.U
          }


          is(5.S(64.W)) {
            CP := 277.U
          }


          is(6.S(64.W)) {
            CP := 279.U
          }


          is(7.S(64.W)) {
            CP := 281.U
          }


          is(8.S(64.W)) {
            CP := 283.U
          }


          is(9.S(64.W)) {
            CP := 285.U
          }

        }

      }

      is(267.U) {
        /*
        $11 = (SP + (4: SP))
        $10 = ($4: anvil.PrinterIndex.I20)
        goto .268
        */



        generalRegFiles(11.U) := (SP + 4.U(16.W))


        generalRegFiles(10.U) := (generalRegFiles(4.U).asSInt).asUInt

        CP := 268.U
      }

      is(268.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.I20, U8]) + (12: SP)) + (($10: anvil.PrinterIndex.I20) as SP)) = (48: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.I20, U8])(($10: anvil.PrinterIndex.I20)) = (48: U8)
        goto .287
        */


        val __tmp_1605 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(10.U).asSInt.asUInt)
        val __tmp_1606 = (48.U(8.W)).asUInt
        arrayRegFiles(__tmp_1605 + 0.U) := __tmp_1606(7, 0)

        CP := 287.U
      }

      is(269.U) {
        /*
        $11 = (SP + (4: SP))
        $10 = ($4: anvil.PrinterIndex.I20)
        goto .270
        */



        generalRegFiles(11.U) := (SP + 4.U(16.W))


        generalRegFiles(10.U) := (generalRegFiles(4.U).asSInt).asUInt

        CP := 270.U
      }

      is(270.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.I20, U8]) + (12: SP)) + (($10: anvil.PrinterIndex.I20) as SP)) = (49: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.I20, U8])(($10: anvil.PrinterIndex.I20)) = (49: U8)
        goto .287
        */


        val __tmp_1607 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(10.U).asSInt.asUInt)
        val __tmp_1608 = (49.U(8.W)).asUInt
        arrayRegFiles(__tmp_1607 + 0.U) := __tmp_1608(7, 0)

        CP := 287.U
      }

      is(271.U) {
        /*
        $11 = (SP + (4: SP))
        $10 = ($4: anvil.PrinterIndex.I20)
        goto .272
        */



        generalRegFiles(11.U) := (SP + 4.U(16.W))


        generalRegFiles(10.U) := (generalRegFiles(4.U).asSInt).asUInt

        CP := 272.U
      }

      is(272.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.I20, U8]) + (12: SP)) + (($10: anvil.PrinterIndex.I20) as SP)) = (50: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.I20, U8])(($10: anvil.PrinterIndex.I20)) = (50: U8)
        goto .287
        */


        val __tmp_1609 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(10.U).asSInt.asUInt)
        val __tmp_1610 = (50.U(8.W)).asUInt
        arrayRegFiles(__tmp_1609 + 0.U) := __tmp_1610(7, 0)

        CP := 287.U
      }

      is(273.U) {
        /*
        $11 = (SP + (4: SP))
        $10 = ($4: anvil.PrinterIndex.I20)
        goto .274
        */



        generalRegFiles(11.U) := (SP + 4.U(16.W))


        generalRegFiles(10.U) := (generalRegFiles(4.U).asSInt).asUInt

        CP := 274.U
      }

      is(274.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.I20, U8]) + (12: SP)) + (($10: anvil.PrinterIndex.I20) as SP)) = (51: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.I20, U8])(($10: anvil.PrinterIndex.I20)) = (51: U8)
        goto .287
        */


        val __tmp_1611 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(10.U).asSInt.asUInt)
        val __tmp_1612 = (51.U(8.W)).asUInt
        arrayRegFiles(__tmp_1611 + 0.U) := __tmp_1612(7, 0)

        CP := 287.U
      }

      is(275.U) {
        /*
        $11 = (SP + (4: SP))
        $10 = ($4: anvil.PrinterIndex.I20)
        goto .276
        */



        generalRegFiles(11.U) := (SP + 4.U(16.W))


        generalRegFiles(10.U) := (generalRegFiles(4.U).asSInt).asUInt

        CP := 276.U
      }

      is(276.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.I20, U8]) + (12: SP)) + (($10: anvil.PrinterIndex.I20) as SP)) = (52: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.I20, U8])(($10: anvil.PrinterIndex.I20)) = (52: U8)
        goto .287
        */


        val __tmp_1613 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(10.U).asSInt.asUInt)
        val __tmp_1614 = (52.U(8.W)).asUInt
        arrayRegFiles(__tmp_1613 + 0.U) := __tmp_1614(7, 0)

        CP := 287.U
      }

      is(277.U) {
        /*
        $11 = (SP + (4: SP))
        $10 = ($4: anvil.PrinterIndex.I20)
        goto .278
        */



        generalRegFiles(11.U) := (SP + 4.U(16.W))


        generalRegFiles(10.U) := (generalRegFiles(4.U).asSInt).asUInt

        CP := 278.U
      }

      is(278.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.I20, U8]) + (12: SP)) + (($10: anvil.PrinterIndex.I20) as SP)) = (53: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.I20, U8])(($10: anvil.PrinterIndex.I20)) = (53: U8)
        goto .287
        */


        val __tmp_1615 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(10.U).asSInt.asUInt)
        val __tmp_1616 = (53.U(8.W)).asUInt
        arrayRegFiles(__tmp_1615 + 0.U) := __tmp_1616(7, 0)

        CP := 287.U
      }

      is(279.U) {
        /*
        $11 = (SP + (4: SP))
        $10 = ($4: anvil.PrinterIndex.I20)
        goto .280
        */



        generalRegFiles(11.U) := (SP + 4.U(16.W))


        generalRegFiles(10.U) := (generalRegFiles(4.U).asSInt).asUInt

        CP := 280.U
      }

      is(280.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.I20, U8]) + (12: SP)) + (($10: anvil.PrinterIndex.I20) as SP)) = (54: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.I20, U8])(($10: anvil.PrinterIndex.I20)) = (54: U8)
        goto .287
        */


        val __tmp_1617 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(10.U).asSInt.asUInt)
        val __tmp_1618 = (54.U(8.W)).asUInt
        arrayRegFiles(__tmp_1617 + 0.U) := __tmp_1618(7, 0)

        CP := 287.U
      }

      is(281.U) {
        /*
        $11 = (SP + (4: SP))
        $10 = ($4: anvil.PrinterIndex.I20)
        goto .282
        */



        generalRegFiles(11.U) := (SP + 4.U(16.W))


        generalRegFiles(10.U) := (generalRegFiles(4.U).asSInt).asUInt

        CP := 282.U
      }

      is(282.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.I20, U8]) + (12: SP)) + (($10: anvil.PrinterIndex.I20) as SP)) = (55: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.I20, U8])(($10: anvil.PrinterIndex.I20)) = (55: U8)
        goto .287
        */


        val __tmp_1619 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(10.U).asSInt.asUInt)
        val __tmp_1620 = (55.U(8.W)).asUInt
        arrayRegFiles(__tmp_1619 + 0.U) := __tmp_1620(7, 0)

        CP := 287.U
      }

      is(283.U) {
        /*
        $11 = (SP + (4: SP))
        $10 = ($4: anvil.PrinterIndex.I20)
        goto .284
        */



        generalRegFiles(11.U) := (SP + 4.U(16.W))


        generalRegFiles(10.U) := (generalRegFiles(4.U).asSInt).asUInt

        CP := 284.U
      }

      is(284.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.I20, U8]) + (12: SP)) + (($10: anvil.PrinterIndex.I20) as SP)) = (56: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.I20, U8])(($10: anvil.PrinterIndex.I20)) = (56: U8)
        goto .287
        */


        val __tmp_1621 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(10.U).asSInt.asUInt)
        val __tmp_1622 = (56.U(8.W)).asUInt
        arrayRegFiles(__tmp_1621 + 0.U) := __tmp_1622(7, 0)

        CP := 287.U
      }

      is(285.U) {
        /*
        $11 = (SP + (4: SP))
        $10 = ($4: anvil.PrinterIndex.I20)
        goto .286
        */



        generalRegFiles(11.U) := (SP + 4.U(16.W))


        generalRegFiles(10.U) := (generalRegFiles(4.U).asSInt).asUInt

        CP := 286.U
      }

      is(286.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.I20, U8]) + (12: SP)) + (($10: anvil.PrinterIndex.I20) as SP)) = (57: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.I20, U8])(($10: anvil.PrinterIndex.I20)) = (57: U8)
        goto .287
        */


        val __tmp_1623 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(10.U).asSInt.asUInt)
        val __tmp_1624 = (57.U(8.W)).asUInt
        arrayRegFiles(__tmp_1623 + 0.U) := __tmp_1624(7, 0)

        CP := 287.U
      }

      is(287.U) {
        /*
        $11 = ($6: S64)
        goto .288
        */



        generalRegFiles(11.U) := (generalRegFiles(6.U).asSInt).asUInt

        CP := 288.U
      }

      is(288.U) {
        /*
        $10 = (($11: S64) / (10: S64))
        goto .289
        */



        generalRegFiles(10.U) := ((generalRegFiles(11.U).asSInt / 10.S(64.W))).asUInt

        CP := 289.U
      }

      is(289.U) {
        /*
        $6 = ($10: S64)
        goto .290
        */



        generalRegFiles(6.U) := (generalRegFiles(10.U).asSInt).asUInt

        CP := 290.U
      }

      is(290.U) {
        /*
        $11 = ($4: anvil.PrinterIndex.I20)
        goto .291
        */



        generalRegFiles(11.U) := (generalRegFiles(4.U).asSInt).asUInt

        CP := 291.U
      }

      is(291.U) {
        /*
        $10 = (($11: anvil.PrinterIndex.I20) + (1: anvil.PrinterIndex.I20))
        goto .292
        */



        generalRegFiles(10.U) := ((generalRegFiles(11.U).asSInt + 1.S(8.W))).asUInt

        CP := 292.U
      }

      is(292.U) {
        /*
        $4 = ($10: anvil.PrinterIndex.I20)
        goto .261
        */



        generalRegFiles(4.U) := (generalRegFiles(10.U).asSInt).asUInt

        CP := 261.U
      }

      is(293.U) {
        /*
        $11 = ($5: B)
        undecl neg: B @$5
        goto .294
        */



        generalRegFiles(11.U) := generalRegFiles(5.U)

        CP := 294.U
      }

      is(294.U) {
        /*
        if ($11: B) goto .295 else goto .300
        */


        CP := Mux((generalRegFiles(11.U).asUInt) === 1.U, 295.U, 300.U)
      }

      is(295.U) {
        /*
        $11 = (SP + (4: SP))
        $10 = ($4: anvil.PrinterIndex.I20)
        goto .296
        */



        generalRegFiles(11.U) := (SP + 4.U(16.W))


        generalRegFiles(10.U) := (generalRegFiles(4.U).asSInt).asUInt

        CP := 296.U
      }

      is(296.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.I20, U8]) + (12: SP)) + (($10: anvil.PrinterIndex.I20) as SP)) = (45: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.I20, U8])(($10: anvil.PrinterIndex.I20)) = (45: U8)
        goto .297
        */


        val __tmp_1625 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(10.U).asSInt.asUInt)
        val __tmp_1626 = (45.U(8.W)).asUInt
        arrayRegFiles(__tmp_1625 + 0.U) := __tmp_1626(7, 0)

        CP := 297.U
      }

      is(297.U) {
        /*
        $11 = ($4: anvil.PrinterIndex.I20)
        goto .298
        */



        generalRegFiles(11.U) := (generalRegFiles(4.U).asSInt).asUInt

        CP := 298.U
      }

      is(298.U) {
        /*
        $10 = (($11: anvil.PrinterIndex.I20) + (1: anvil.PrinterIndex.I20))
        goto .299
        */



        generalRegFiles(10.U) := ((generalRegFiles(11.U).asSInt + 1.S(8.W))).asUInt

        CP := 299.U
      }

      is(299.U) {
        /*
        $4 = ($10: anvil.PrinterIndex.I20)
        goto .300
        */



        generalRegFiles(4.U) := (generalRegFiles(10.U).asSInt).asUInt

        CP := 300.U
      }

      is(300.U) {
        /*
        decl j: anvil.PrinterIndex.I20 @$7
        $11 = ($4: anvil.PrinterIndex.I20)
        undecl i: anvil.PrinterIndex.I20 @$4
        goto .301
        */



        generalRegFiles(11.U) := (generalRegFiles(4.U).asSInt).asUInt

        CP := 301.U
      }

      is(301.U) {
        /*
        $10 = (($11: anvil.PrinterIndex.I20) - (1: anvil.PrinterIndex.I20))
        goto .302
        */



        generalRegFiles(10.U) := ((generalRegFiles(11.U).asSInt - 1.S(8.W))).asUInt

        CP := 302.U
      }

      is(302.U) {
        /*
        $7 = ($10: anvil.PrinterIndex.I20)
        goto .303
        */



        generalRegFiles(7.U) := (generalRegFiles(10.U).asSInt).asUInt

        CP := 303.U
      }

      is(303.U) {
        /*
        decl idx: anvil.PrinterIndex.U @$8
        $11 = ($1: anvil.PrinterIndex.U)
        goto .304
        */



        generalRegFiles(11.U) := generalRegFiles(1.U)

        CP := 304.U
      }

      is(304.U) {
        /*
        $8 = ($11: anvil.PrinterIndex.U)
        goto .305
        */



        generalRegFiles(8.U) := generalRegFiles(11.U)

        CP := 305.U
      }

      is(305.U) {
        /*
        decl r: U64 @$9
        $9 = (0: U64)
        goto .306
        */



        generalRegFiles(9.U) := 0.U(64.W)

        CP := 306.U
      }

      is(306.U) {
        /*
        $11 = ($7: anvil.PrinterIndex.I20)
        goto .307
        */



        generalRegFiles(11.U) := (generalRegFiles(7.U).asSInt).asUInt

        CP := 307.U
      }

      is(307.U) {
        /*
        $10 = (($11: anvil.PrinterIndex.I20) >= (0: anvil.PrinterIndex.I20))
        goto .308
        */



        generalRegFiles(10.U) := (generalRegFiles(11.U).asSInt >= 0.S(8.W)).asUInt

        CP := 308.U
      }

      is(308.U) {
        /*
        if ($10: B) goto .309 else goto .323
        */


        CP := Mux((generalRegFiles(10.U).asUInt) === 1.U, 309.U, 323.U)
      }

      is(309.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $10 = ($8: anvil.PrinterIndex.U)
        $13 = ($2: anvil.PrinterIndex.U)
        goto .310
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(10.U) := generalRegFiles(8.U)


        generalRegFiles(13.U) := generalRegFiles(2.U)

        CP := 310.U
      }

      is(310.U) {
        /*
        $12 = (($10: anvil.PrinterIndex.U) & ($13: anvil.PrinterIndex.U))
        goto .311
        */



        generalRegFiles(12.U) := (generalRegFiles(10.U) & generalRegFiles(13.U))

        CP := 311.U
      }

      is(311.U) {
        /*
        $14 = (SP + (4: SP))
        $15 = ($7: anvil.PrinterIndex.I20)
        goto .312
        */



        generalRegFiles(14.U) := (SP + 4.U(16.W))


        generalRegFiles(15.U) := (generalRegFiles(7.U).asSInt).asUInt

        CP := 312.U
      }

      is(312.U) {
        /*
        $16 = *((($14: MS[anvil.PrinterIndex.I20, U8]) + (12: SP)) + (($15: anvil.PrinterIndex.I20) as SP)) [unsigned, U8, 1]  // $16 = ($14: MS[anvil.PrinterIndex.I20, U8])(($15: anvil.PrinterIndex.I20))
        goto .313
        */


        val __tmp_1627 = (((generalRegFiles(14.U) + 12.U(16.W)) + generalRegFiles(15.U).asSInt.asUInt)).asUInt
        generalRegFiles(16.U) := Cat(
          arrayRegFiles(__tmp_1627 + 0.U)
        ).asUInt

        CP := 313.U
      }

      is(313.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($12: anvil.PrinterIndex.U) as SP)) = ($16: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($12: anvil.PrinterIndex.U)) = ($16: U8)
        goto .314
        */


        val __tmp_1628 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(12.U).asUInt)
        val __tmp_1629 = (generalRegFiles(16.U)).asUInt
        arrayRegFiles(__tmp_1628 + 0.U) := __tmp_1629(7, 0)

        CP := 314.U
      }

      is(314.U) {
        /*
        $11 = ($7: anvil.PrinterIndex.I20)
        goto .315
        */



        generalRegFiles(11.U) := (generalRegFiles(7.U).asSInt).asUInt

        CP := 315.U
      }

      is(315.U) {
        /*
        $10 = (($11: anvil.PrinterIndex.I20) - (1: anvil.PrinterIndex.I20))
        goto .316
        */



        generalRegFiles(10.U) := ((generalRegFiles(11.U).asSInt - 1.S(8.W))).asUInt

        CP := 316.U
      }

      is(316.U) {
        /*
        $7 = ($10: anvil.PrinterIndex.I20)
        goto .317
        */



        generalRegFiles(7.U) := (generalRegFiles(10.U).asSInt).asUInt

        CP := 317.U
      }

      is(317.U) {
        /*
        $11 = ($8: anvil.PrinterIndex.U)
        goto .318
        */



        generalRegFiles(11.U) := generalRegFiles(8.U)

        CP := 318.U
      }

      is(318.U) {
        /*
        $10 = (($11: anvil.PrinterIndex.U) + (1: anvil.PrinterIndex.U))
        goto .319
        */



        generalRegFiles(10.U) := (generalRegFiles(11.U) + 1.U(64.W))

        CP := 319.U
      }

      is(319.U) {
        /*
        $8 = ($10: anvil.PrinterIndex.U)
        goto .320
        */



        generalRegFiles(8.U) := generalRegFiles(10.U)

        CP := 320.U
      }

      is(320.U) {
        /*
        $11 = ($9: U64)
        goto .321
        */



        generalRegFiles(11.U) := generalRegFiles(9.U)

        CP := 321.U
      }

      is(321.U) {
        /*
        $10 = (($11: U64) + (1: U64))
        goto .322
        */



        generalRegFiles(10.U) := (generalRegFiles(11.U) + 1.U(64.W))

        CP := 322.U
      }

      is(322.U) {
        /*
        $9 = ($10: U64)
        goto .306
        */



        generalRegFiles(9.U) := generalRegFiles(10.U)

        CP := 306.U
      }

      is(323.U) {
        /*
        $11 = ($9: U64)
        undecl r: U64 @$9
        goto .324
        */



        generalRegFiles(11.U) := generalRegFiles(9.U)

        CP := 324.U
      }

      is(324.U) {
        /*
        **(SP + (2: SP)) = ($11: U64) [unsigned, U64, 8]  // $res = ($11: U64)
        goto $ret@0
        */


        val __tmp_1630 = Cat(
          arrayRegFiles((SP + 2.U(16.W)) + 1.U),
          arrayRegFiles((SP + 2.U(16.W)) + 0.U)
        )
        val __tmp_1631 = (generalRegFiles(11.U)).asUInt
        arrayRegFiles(__tmp_1630 + 0.U) := __tmp_1631(7, 0)
        arrayRegFiles(__tmp_1630 + 1.U) := __tmp_1631(15, 8)
        arrayRegFiles(__tmp_1630 + 2.U) := __tmp_1631(23, 16)
        arrayRegFiles(__tmp_1630 + 3.U) := __tmp_1631(31, 24)
        arrayRegFiles(__tmp_1630 + 4.U) := __tmp_1631(39, 32)
        arrayRegFiles(__tmp_1630 + 5.U) := __tmp_1631(47, 40)
        arrayRegFiles(__tmp_1630 + 6.U) := __tmp_1631(55, 48)
        arrayRegFiles(__tmp_1630 + 7.U) := __tmp_1631(63, 56)

        CP := Cat(
          arrayRegFiles(SP + 0.U + 1.U),
          arrayRegFiles(SP + 0.U + 0.U)
        )

      }

      is(325.U) {
        /*
        DP = DP + 19
        goto .326
        */


        DP := DP + 19.U

        CP := 326.U
      }

      is(326.U) {
        /*
        *(((8: SP) + (12: SP)) + ((DP & (15: DP)) as SP)) = (10: U8) [unsigned, U8, 1]  // $display((DP & (15: DP))) = (10: U8)
        goto .327
        */


        val __tmp_1632 = ((8.U(16.W) + 12.U(16.W)) + (DP & 15.U(64.W)).asUInt)
        val __tmp_1633 = (10.U(8.W)).asUInt
        arrayRegFiles(__tmp_1632 + 0.U) := __tmp_1633(7, 0)

        CP := 327.U
      }

      is(327.U) {
        /*
        DP = DP + 1
        goto .1
        */


        DP := DP + 1.U

        CP := 1.U
      }

      is(329.U) {
        /*
        DP = DP + 19
        goto .330
        */


        DP := DP + 19.U

        CP := 330.U
      }

      is(330.U) {
        /*
        *(((8: SP) + (12: SP)) + ((DP & (15: DP)) as SP)) = (10: U8) [unsigned, U8, 1]  // $display((DP & (15: DP))) = (10: U8)
        goto .331
        */


        val __tmp_1634 = ((8.U(16.W) + 12.U(16.W)) + (DP & 15.U(64.W)).asUInt)
        val __tmp_1635 = (10.U(8.W)).asUInt
        arrayRegFiles(__tmp_1634 + 0.U) := __tmp_1635(7, 0)

        CP := 331.U
      }

      is(331.U) {
        /*
        DP = DP + 1
        goto .1
        */


        DP := DP + 1.U

        CP := 1.U
      }

      is(333.U) {
        /*
        DP = DP + 19
        goto .334
        */


        DP := DP + 19.U

        CP := 334.U
      }

      is(334.U) {
        /*
        *(((8: SP) + (12: SP)) + ((DP & (15: DP)) as SP)) = (10: U8) [unsigned, U8, 1]  // $display((DP & (15: DP))) = (10: U8)
        goto .335
        */


        val __tmp_1636 = ((8.U(16.W) + 12.U(16.W)) + (DP & 15.U(64.W)).asUInt)
        val __tmp_1637 = (10.U(8.W)).asUInt
        arrayRegFiles(__tmp_1636 + 0.U) := __tmp_1637(7, 0)

        CP := 335.U
      }

      is(335.U) {
        /*
        DP = DP + 1
        goto .1
        */


        DP := DP + 1.U

        CP := 1.U
      }

      is(337.U) {
        /*
        DP = DP + 19
        goto .338
        */


        DP := DP + 19.U

        CP := 338.U
      }

      is(338.U) {
        /*
        *(((8: SP) + (12: SP)) + ((DP & (15: DP)) as SP)) = (10: U8) [unsigned, U8, 1]  // $display((DP & (15: DP))) = (10: U8)
        goto .339
        */


        val __tmp_1638 = ((8.U(16.W) + 12.U(16.W)) + (DP & 15.U(64.W)).asUInt)
        val __tmp_1639 = (10.U(8.W)).asUInt
        arrayRegFiles(__tmp_1638 + 0.U) := __tmp_1639(7, 0)

        CP := 339.U
      }

      is(339.U) {
        /*
        DP = DP + 1
        goto .1
        */


        DP := DP + 1.U

        CP := 1.U
      }

    }

}


