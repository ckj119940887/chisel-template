package GeneralRegFileToBRAM
import chisel3._
import chisel3.util._
import chisel3.experimental._



class LocalReuseTest (val C_S_AXI_DATA_WIDTH:  Int = 32,
               val C_S_AXI_ADDR_WIDTH:  Int = 32,
               val ARRAY_REG_WIDTH:     Int = 8,
               val ARRAY_REG_DEPTH:     Int = 136,
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
        SP = 28
        DP = 0
        *(10: U32) = (886747591: U32) [unsigned, U32, 4]  // $display.$type (MS[anvil.PrinterIndex.U, U8]: 0x34DAB1C7)
        *(14: SP) = (8: Z) [signed, Z, 8]  // $display.size
        *(28: CP) = (0: CP) [unsigned, CP, 2]  // $ret
        goto .4
        */


        SP := 28.U(16.W)

        DP := 0.U(64.W)

        val __tmp_2749 = 10.U(32.W)
        val __tmp_2750 = (886747591.U(32.W)).asUInt
        arrayRegFiles(__tmp_2749 + 0.U) := __tmp_2750(7, 0)
        arrayRegFiles(__tmp_2749 + 1.U) := __tmp_2750(15, 8)
        arrayRegFiles(__tmp_2749 + 2.U) := __tmp_2750(23, 16)
        arrayRegFiles(__tmp_2749 + 3.U) := __tmp_2750(31, 24)

        val __tmp_2751 = 14.U(16.W)
        val __tmp_2752 = (8.S(64.W)).asUInt
        arrayRegFiles(__tmp_2751 + 0.U) := __tmp_2752(7, 0)
        arrayRegFiles(__tmp_2751 + 1.U) := __tmp_2752(15, 8)
        arrayRegFiles(__tmp_2751 + 2.U) := __tmp_2752(23, 16)
        arrayRegFiles(__tmp_2751 + 3.U) := __tmp_2752(31, 24)
        arrayRegFiles(__tmp_2751 + 4.U) := __tmp_2752(39, 32)
        arrayRegFiles(__tmp_2751 + 5.U) := __tmp_2752(47, 40)
        arrayRegFiles(__tmp_2751 + 6.U) := __tmp_2752(55, 48)
        arrayRegFiles(__tmp_2751 + 7.U) := __tmp_2752(63, 56)

        val __tmp_2753 = 28.U(16.W)
        val __tmp_2754 = (0.U(16.W)).asUInt
        arrayRegFiles(__tmp_2753 + 0.U) := __tmp_2754(7, 0)
        arrayRegFiles(__tmp_2753 + 1.U) := __tmp_2754(15, 8)

        CP := 4.U
      }

      is(4.U) {
        /*
        $0 = *(0: SP) [signed, Z, 8]  // $0 = $testNum
        goto .5
        */


        val __tmp_2755 = (0.U(16.W)).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_2755 + 7.U),
          arrayRegFiles(__tmp_2755 + 6.U),
          arrayRegFiles(__tmp_2755 + 5.U),
          arrayRegFiles(__tmp_2755 + 4.U),
          arrayRegFiles(__tmp_2755 + 3.U),
          arrayRegFiles(__tmp_2755 + 2.U),
          arrayRegFiles(__tmp_2755 + 1.U),
          arrayRegFiles(__tmp_2755 + 0.U)
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
        SP = SP + 2
        goto .7
        */


        SP := SP + 2.U

        CP := 7.U
      }

      is(7.U) {
        /*
        *SP = (9: CP) [unsigned, CP, 2]  // $ret@0 = 1326
        goto .8
        */


        val __tmp_2756 = SP
        val __tmp_2757 = (9.U(16.W)).asUInt
        arrayRegFiles(__tmp_2756 + 0.U) := __tmp_2757(7, 0)
        arrayRegFiles(__tmp_2756 + 1.U) := __tmp_2757(15, 8)

        CP := 8.U
      }

      is(8.U) {
        /*
        decl $ret: CP [@0, 2]
        goto .12
        */


        CP := 12.U
      }

      is(9.U) {
        /*
        undecl $ret: CP [@0, 2]
        goto .10
        */


        CP := 10.U
      }

      is(10.U) {
        /*
        SP = SP - 2
        goto .11
        */


        SP := SP - 2.U

        CP := 11.U
      }

      is(11.U) {
        /*
        goto $ret@0
        */


        CP := Cat(
          arrayRegFiles(SP + 0.U + 1.U),
          arrayRegFiles(SP + 0.U + 0.U)
        )

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
        *SP = (15: CP) [unsigned, CP, 2]  // $ret@0 = 1328
        goto .14
        */


        val __tmp_2758 = SP
        val __tmp_2759 = (15.U(16.W)).asUInt
        arrayRegFiles(__tmp_2758 + 0.U) := __tmp_2759(7, 0)
        arrayRegFiles(__tmp_2758 + 1.U) := __tmp_2759(15, 8)

        CP := 14.U
      }

      is(14.U) {
        /*
        decl $ret: CP [@0, 2]
        goto .18
        */


        CP := 18.U
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
        decl x: Z @$0
        $0 = (3: Z)
        goto .19
        */



        generalRegFiles(0.U) := (3.S(64.W)).asUInt

        CP := 19.U
      }

      is(19.U) {
        /*
        decl y: Z @$1
        $1 = (4: Z)
        goto .20
        */



        generalRegFiles(1.U) := (4.S(64.W)).asUInt

        CP := 20.U
      }

      is(20.U) {
        /*
        alloc printS64$res@[7,11].363B94F1: U64 [@2, 8]
        goto .21
        */


        CP := 21.U
      }

      is(21.U) {
        /*
        SP = SP + 34
        goto .22
        */


        SP := SP + 34.U

        CP := 22.U
      }

      is(22.U) {
        /*
        *SP = (24: CP) [unsigned, CP, 2]  // $ret@0 = 1330
        *(SP + (2: SP)) = (SP - (32: SP)) [unsigned, SP, 2]  // $res@2 = -32
        $91 = (8: SP)
        $92 = DP
        $93 = (7: anvil.PrinterIndex.U)
        $94 = (($0: Z) as S64)
        *(SP - (8: SP)) = ($1: Z) [signed, Z, 8]  // save $1 (Z)
        goto .23
        */


        val __tmp_2760 = SP
        val __tmp_2761 = (24.U(16.W)).asUInt
        arrayRegFiles(__tmp_2760 + 0.U) := __tmp_2761(7, 0)
        arrayRegFiles(__tmp_2760 + 1.U) := __tmp_2761(15, 8)

        val __tmp_2762 = (SP + 2.U(16.W))
        val __tmp_2763 = ((SP - 32.U(16.W))).asUInt
        arrayRegFiles(__tmp_2762 + 0.U) := __tmp_2763(7, 0)
        arrayRegFiles(__tmp_2762 + 1.U) := __tmp_2763(15, 8)


        generalRegFiles(91.U) := 8.U(16.W)


        generalRegFiles(92.U) := DP


        generalRegFiles(93.U) := 7.U(64.W)


        generalRegFiles(94.U) := (generalRegFiles(0.U).asSInt.asSInt).asUInt

        val __tmp_2764 = (SP - 8.U(16.W))
        val __tmp_2765 = (generalRegFiles(1.U).asSInt).asUInt
        arrayRegFiles(__tmp_2764 + 0.U) := __tmp_2765(7, 0)
        arrayRegFiles(__tmp_2764 + 1.U) := __tmp_2765(15, 8)
        arrayRegFiles(__tmp_2764 + 2.U) := __tmp_2765(23, 16)
        arrayRegFiles(__tmp_2764 + 3.U) := __tmp_2765(31, 24)
        arrayRegFiles(__tmp_2764 + 4.U) := __tmp_2765(39, 32)
        arrayRegFiles(__tmp_2764 + 5.U) := __tmp_2765(47, 40)
        arrayRegFiles(__tmp_2764 + 6.U) := __tmp_2765(55, 48)
        arrayRegFiles(__tmp_2764 + 7.U) := __tmp_2765(63, 56)

        CP := 23.U
      }

      is(23.U) {
        /*
        decl $ret: CP [@0, 2], $res: SP [@2, 2], buffer: MS[anvil.PrinterIndex.U, U8] @$0, index: anvil.PrinterIndex.U @$1, mask: anvil.PrinterIndex.U @$2, n: S64 @$3
        $0 = ($91: MS[anvil.PrinterIndex.U, U8])
        $1 = ($92: anvil.PrinterIndex.U)
        $2 = ($93: anvil.PrinterIndex.U)
        $3 = ($94: S64)
        goto .52
        */



        generalRegFiles(0.U) := generalRegFiles(91.U)


        generalRegFiles(1.U) := generalRegFiles(92.U)


        generalRegFiles(2.U) := generalRegFiles(93.U)


        generalRegFiles(3.U) := (generalRegFiles(94.U).asSInt).asUInt

        CP := 52.U
      }

      is(24.U) {
        /*
        $1 = *(SP - (8: SP)) [signed, Z, 8]  // restore $1 (Z)
        $6 = **(SP + (2: SP)) [unsigned, U64, 8]  // $6 = $res
        undecl n: S64 @$3, mask: anvil.PrinterIndex.U @$2, index: anvil.PrinterIndex.U @$1, buffer: MS[anvil.PrinterIndex.U, U8] @$0, $res: SP [@2, 2], $ret: CP [@0, 2]
        goto .25
        */


        val __tmp_2766 = ((SP - 8.U(16.W))).asUInt
        generalRegFiles(1.U) := Cat(
          arrayRegFiles(__tmp_2766 + 7.U),
          arrayRegFiles(__tmp_2766 + 6.U),
          arrayRegFiles(__tmp_2766 + 5.U),
          arrayRegFiles(__tmp_2766 + 4.U),
          arrayRegFiles(__tmp_2766 + 3.U),
          arrayRegFiles(__tmp_2766 + 2.U),
          arrayRegFiles(__tmp_2766 + 1.U),
          arrayRegFiles(__tmp_2766 + 0.U)
        ).asSInt.pad(GENERAL_REG_WIDTH).asUInt

        val __tmp_2767 = (Cat(
          arrayRegFiles((SP + 2.U(16.W)) + 1.U),
          arrayRegFiles((SP + 2.U(16.W)) + 0.U)
        )).asUInt
        generalRegFiles(6.U) := Cat(
          arrayRegFiles(__tmp_2767 + 7.U),
          arrayRegFiles(__tmp_2767 + 6.U),
          arrayRegFiles(__tmp_2767 + 5.U),
          arrayRegFiles(__tmp_2767 + 4.U),
          arrayRegFiles(__tmp_2767 + 3.U),
          arrayRegFiles(__tmp_2767 + 2.U),
          arrayRegFiles(__tmp_2767 + 1.U),
          arrayRegFiles(__tmp_2767 + 0.U)
        ).asUInt

        CP := 25.U
      }

      is(25.U) {
        /*
        SP = SP - 34
        goto .26
        */


        SP := SP - 34.U

        CP := 26.U
      }

      is(26.U) {
        /*
        undecl x: Z @$0
        goto .27
        */


        CP := 27.U
      }

      is(27.U) {
        /*
        DP = DP + (($6: U64) as DP)
        goto .28
        */


        DP := DP + generalRegFiles(6.U).asUInt

        CP := 28.U
      }

      is(28.U) {
        /*
        *(((8: SP) + (12: SP)) + ((DP & (7: DP)) as SP)) = (10: U8) [unsigned, U8, 1]  // $display((DP & (7: DP))) = (10: U8)
        goto .29
        */


        val __tmp_2768 = ((8.U(16.W) + 12.U(16.W)) + (DP & 7.U(64.W)).asUInt)
        val __tmp_2769 = (10.U(8.W)).asUInt
        arrayRegFiles(__tmp_2768 + 0.U) := __tmp_2769(7, 0)

        CP := 29.U
      }

      is(29.U) {
        /*
        DP = DP + 1
        goto .30
        */


        DP := DP + 1.U

        CP := 30.U
      }

      is(30.U) {
        /*
        decl z: Z @$2
        goto .31
        */


        CP := 31.U
      }

      is(31.U) {
        /*
        $2 = (1: Z)
        goto .32
        */



        generalRegFiles(2.U) := (1.S(64.W)).asUInt

        CP := 32.U
      }

      is(32.U) {
        /*
        alloc printS64$res@[9,11].E9B9CCEC: U64 [@10, 8]
        goto .33
        */


        CP := 33.U
      }

      is(33.U) {
        /*
        SP = SP + 34
        goto .34
        */


        SP := SP + 34.U

        CP := 34.U
      }

      is(34.U) {
        /*
        *SP = (36: CP) [unsigned, CP, 2]  // $ret@0 = 1332
        *(SP + (2: SP)) = (SP - (24: SP)) [unsigned, SP, 2]  // $res@2 = -24
        $91 = (8: SP)
        $92 = DP
        $93 = (7: anvil.PrinterIndex.U)
        $94 = (($2: Z) as S64)
        *(SP - (8: SP)) = ($1: Z) [signed, Z, 8]  // save $1 (Z)
        goto .35
        */


        val __tmp_2770 = SP
        val __tmp_2771 = (36.U(16.W)).asUInt
        arrayRegFiles(__tmp_2770 + 0.U) := __tmp_2771(7, 0)
        arrayRegFiles(__tmp_2770 + 1.U) := __tmp_2771(15, 8)

        val __tmp_2772 = (SP + 2.U(16.W))
        val __tmp_2773 = ((SP - 24.U(16.W))).asUInt
        arrayRegFiles(__tmp_2772 + 0.U) := __tmp_2773(7, 0)
        arrayRegFiles(__tmp_2772 + 1.U) := __tmp_2773(15, 8)


        generalRegFiles(91.U) := 8.U(16.W)


        generalRegFiles(92.U) := DP


        generalRegFiles(93.U) := 7.U(64.W)


        generalRegFiles(94.U) := (generalRegFiles(2.U).asSInt.asSInt).asUInt

        val __tmp_2774 = (SP - 8.U(16.W))
        val __tmp_2775 = (generalRegFiles(1.U).asSInt).asUInt
        arrayRegFiles(__tmp_2774 + 0.U) := __tmp_2775(7, 0)
        arrayRegFiles(__tmp_2774 + 1.U) := __tmp_2775(15, 8)
        arrayRegFiles(__tmp_2774 + 2.U) := __tmp_2775(23, 16)
        arrayRegFiles(__tmp_2774 + 3.U) := __tmp_2775(31, 24)
        arrayRegFiles(__tmp_2774 + 4.U) := __tmp_2775(39, 32)
        arrayRegFiles(__tmp_2774 + 5.U) := __tmp_2775(47, 40)
        arrayRegFiles(__tmp_2774 + 6.U) := __tmp_2775(55, 48)
        arrayRegFiles(__tmp_2774 + 7.U) := __tmp_2775(63, 56)

        CP := 35.U
      }

      is(35.U) {
        /*
        decl $ret: CP [@0, 2], $res: SP [@2, 2], buffer: MS[anvil.PrinterIndex.U, U8] @$0, index: anvil.PrinterIndex.U @$1, mask: anvil.PrinterIndex.U @$2, n: S64 @$3
        $0 = ($91: MS[anvil.PrinterIndex.U, U8])
        $1 = ($92: anvil.PrinterIndex.U)
        $2 = ($93: anvil.PrinterIndex.U)
        $3 = ($94: S64)
        goto .52
        */



        generalRegFiles(0.U) := generalRegFiles(91.U)


        generalRegFiles(1.U) := generalRegFiles(92.U)


        generalRegFiles(2.U) := generalRegFiles(93.U)


        generalRegFiles(3.U) := (generalRegFiles(94.U).asSInt).asUInt

        CP := 52.U
      }

      is(36.U) {
        /*
        $1 = *(SP - (8: SP)) [signed, Z, 8]  // restore $1 (Z)
        $6 = **(SP + (2: SP)) [unsigned, U64, 8]  // $6 = $res
        undecl n: S64 @$3, mask: anvil.PrinterIndex.U @$2, index: anvil.PrinterIndex.U @$1, buffer: MS[anvil.PrinterIndex.U, U8] @$0, $res: SP [@2, 2], $ret: CP [@0, 2]
        goto .37
        */


        val __tmp_2776 = ((SP - 8.U(16.W))).asUInt
        generalRegFiles(1.U) := Cat(
          arrayRegFiles(__tmp_2776 + 7.U),
          arrayRegFiles(__tmp_2776 + 6.U),
          arrayRegFiles(__tmp_2776 + 5.U),
          arrayRegFiles(__tmp_2776 + 4.U),
          arrayRegFiles(__tmp_2776 + 3.U),
          arrayRegFiles(__tmp_2776 + 2.U),
          arrayRegFiles(__tmp_2776 + 1.U),
          arrayRegFiles(__tmp_2776 + 0.U)
        ).asSInt.pad(GENERAL_REG_WIDTH).asUInt

        val __tmp_2777 = (Cat(
          arrayRegFiles((SP + 2.U(16.W)) + 1.U),
          arrayRegFiles((SP + 2.U(16.W)) + 0.U)
        )).asUInt
        generalRegFiles(6.U) := Cat(
          arrayRegFiles(__tmp_2777 + 7.U),
          arrayRegFiles(__tmp_2777 + 6.U),
          arrayRegFiles(__tmp_2777 + 5.U),
          arrayRegFiles(__tmp_2777 + 4.U),
          arrayRegFiles(__tmp_2777 + 3.U),
          arrayRegFiles(__tmp_2777 + 2.U),
          arrayRegFiles(__tmp_2777 + 1.U),
          arrayRegFiles(__tmp_2777 + 0.U)
        ).asUInt

        CP := 37.U
      }

      is(37.U) {
        /*
        SP = SP - 34
        goto .38
        */


        SP := SP - 34.U

        CP := 38.U
      }

      is(38.U) {
        /*
        undecl z: Z @$2
        goto .39
        */


        CP := 39.U
      }

      is(39.U) {
        /*
        DP = DP + (($6: U64) as DP)
        goto .40
        */


        DP := DP + generalRegFiles(6.U).asUInt

        CP := 40.U
      }

      is(40.U) {
        /*
        *(((8: SP) + (12: SP)) + ((DP & (7: DP)) as SP)) = (10: U8) [unsigned, U8, 1]  // $display((DP & (7: DP))) = (10: U8)
        goto .41
        */


        val __tmp_2778 = ((8.U(16.W) + 12.U(16.W)) + (DP & 7.U(64.W)).asUInt)
        val __tmp_2779 = (10.U(8.W)).asUInt
        arrayRegFiles(__tmp_2778 + 0.U) := __tmp_2779(7, 0)

        CP := 41.U
      }

      is(41.U) {
        /*
        DP = DP + 1
        goto .42
        */


        DP := DP + 1.U

        CP := 42.U
      }

      is(42.U) {
        /*
        alloc printS64$res@[10,11].8B1F04A5: U64 [@18, 8]
        goto .43
        */


        CP := 43.U
      }

      is(43.U) {
        /*
        SP = SP + 26
        goto .44
        */


        SP := SP + 26.U

        CP := 44.U
      }

      is(44.U) {
        /*
        *SP = (46: CP) [unsigned, CP, 2]  // $ret@0 = 1334
        *(SP + (2: SP)) = (SP - (8: SP)) [unsigned, SP, 2]  // $res@2 = -8
        $91 = (8: SP)
        $92 = DP
        $93 = (7: anvil.PrinterIndex.U)
        $94 = (($1: Z) as S64)
        goto .45
        */


        val __tmp_2780 = SP
        val __tmp_2781 = (46.U(16.W)).asUInt
        arrayRegFiles(__tmp_2780 + 0.U) := __tmp_2781(7, 0)
        arrayRegFiles(__tmp_2780 + 1.U) := __tmp_2781(15, 8)

        val __tmp_2782 = (SP + 2.U(16.W))
        val __tmp_2783 = ((SP - 8.U(16.W))).asUInt
        arrayRegFiles(__tmp_2782 + 0.U) := __tmp_2783(7, 0)
        arrayRegFiles(__tmp_2782 + 1.U) := __tmp_2783(15, 8)


        generalRegFiles(91.U) := 8.U(16.W)


        generalRegFiles(92.U) := DP


        generalRegFiles(93.U) := 7.U(64.W)


        generalRegFiles(94.U) := (generalRegFiles(1.U).asSInt.asSInt).asUInt

        CP := 45.U
      }

      is(45.U) {
        /*
        decl $ret: CP [@0, 2], $res: SP [@2, 2], buffer: MS[anvil.PrinterIndex.U, U8] @$0, index: anvil.PrinterIndex.U @$1, mask: anvil.PrinterIndex.U @$2, n: S64 @$3
        $0 = ($91: MS[anvil.PrinterIndex.U, U8])
        $1 = ($92: anvil.PrinterIndex.U)
        $2 = ($93: anvil.PrinterIndex.U)
        $3 = ($94: S64)
        goto .52
        */



        generalRegFiles(0.U) := generalRegFiles(91.U)


        generalRegFiles(1.U) := generalRegFiles(92.U)


        generalRegFiles(2.U) := generalRegFiles(93.U)


        generalRegFiles(3.U) := (generalRegFiles(94.U).asSInt).asUInt

        CP := 52.U
      }

      is(46.U) {
        /*
        $6 = **(SP + (2: SP)) [unsigned, U64, 8]  // $6 = $res
        undecl n: S64 @$3, mask: anvil.PrinterIndex.U @$2, index: anvil.PrinterIndex.U @$1, buffer: MS[anvil.PrinterIndex.U, U8] @$0, $res: SP [@2, 2], $ret: CP [@0, 2]
        goto .47
        */


        val __tmp_2784 = (Cat(
          arrayRegFiles((SP + 2.U(16.W)) + 1.U),
          arrayRegFiles((SP + 2.U(16.W)) + 0.U)
        )).asUInt
        generalRegFiles(6.U) := Cat(
          arrayRegFiles(__tmp_2784 + 7.U),
          arrayRegFiles(__tmp_2784 + 6.U),
          arrayRegFiles(__tmp_2784 + 5.U),
          arrayRegFiles(__tmp_2784 + 4.U),
          arrayRegFiles(__tmp_2784 + 3.U),
          arrayRegFiles(__tmp_2784 + 2.U),
          arrayRegFiles(__tmp_2784 + 1.U),
          arrayRegFiles(__tmp_2784 + 0.U)
        ).asUInt

        CP := 47.U
      }

      is(47.U) {
        /*
        SP = SP - 26
        goto .48
        */


        SP := SP - 26.U

        CP := 48.U
      }

      is(48.U) {
        /*
        undecl y: Z @$1
        goto .49
        */


        CP := 49.U
      }

      is(49.U) {
        /*
        DP = DP + (($6: U64) as DP)
        goto .50
        */


        DP := DP + generalRegFiles(6.U).asUInt

        CP := 50.U
      }

      is(50.U) {
        /*
        *(((8: SP) + (12: SP)) + ((DP & (7: DP)) as SP)) = (10: U8) [unsigned, U8, 1]  // $display((DP & (7: DP))) = (10: U8)
        goto .51
        */


        val __tmp_2785 = ((8.U(16.W) + 12.U(16.W)) + (DP & 7.U(64.W)).asUInt)
        val __tmp_2786 = (10.U(8.W)).asUInt
        arrayRegFiles(__tmp_2785 + 0.U) := __tmp_2786(7, 0)

        CP := 51.U
      }

      is(51.U) {
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

      is(52.U) {
        /*
        $10 = (($3: S64) ≡ (-9223372036854775808: S64))
        goto .53
        */



        generalRegFiles(10.U) := (generalRegFiles(3.U).asSInt === BigInt("-9223372036854775808").S(64.W)).asUInt

        CP := 53.U
      }

      is(53.U) {
        /*
        if ($10: B) goto .54 else goto .114
        */


        CP := Mux((generalRegFiles(10.U).asUInt) === 1.U, 54.U, 114.U)
      }

      is(54.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $12 = (($1: anvil.PrinterIndex.U) & ($2: anvil.PrinterIndex.U))
        goto .55
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(12.U) := (generalRegFiles(1.U) & generalRegFiles(2.U))

        CP := 55.U
      }

      is(55.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($12: anvil.PrinterIndex.U) as SP)) = (45: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($12: anvil.PrinterIndex.U)) = (45: U8)
        goto .56
        */


        val __tmp_2787 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(12.U).asUInt)
        val __tmp_2788 = (45.U(8.W)).asUInt
        arrayRegFiles(__tmp_2787 + 0.U) := __tmp_2788(7, 0)

        CP := 56.U
      }

      is(56.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $13 = (($1: anvil.PrinterIndex.U) + (1: anvil.PrinterIndex.U))
        goto .57
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(13.U) := (generalRegFiles(1.U) + 1.U(64.W))

        CP := 57.U
      }

      is(57.U) {
        /*
        $14 = (($13: anvil.PrinterIndex.U) & ($2: anvil.PrinterIndex.U))
        goto .58
        */



        generalRegFiles(14.U) := (generalRegFiles(13.U) & generalRegFiles(2.U))

        CP := 58.U
      }

      is(58.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($14: anvil.PrinterIndex.U) as SP)) = (57: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($14: anvil.PrinterIndex.U)) = (57: U8)
        goto .59
        */


        val __tmp_2789 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(14.U).asUInt)
        val __tmp_2790 = (57.U(8.W)).asUInt
        arrayRegFiles(__tmp_2789 + 0.U) := __tmp_2790(7, 0)

        CP := 59.U
      }

      is(59.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $13 = (($1: anvil.PrinterIndex.U) + (2: anvil.PrinterIndex.U))
        goto .60
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(13.U) := (generalRegFiles(1.U) + 2.U(64.W))

        CP := 60.U
      }

      is(60.U) {
        /*
        $14 = (($13: anvil.PrinterIndex.U) & ($2: anvil.PrinterIndex.U))
        goto .61
        */



        generalRegFiles(14.U) := (generalRegFiles(13.U) & generalRegFiles(2.U))

        CP := 61.U
      }

      is(61.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($14: anvil.PrinterIndex.U) as SP)) = (50: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($14: anvil.PrinterIndex.U)) = (50: U8)
        goto .62
        */


        val __tmp_2791 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(14.U).asUInt)
        val __tmp_2792 = (50.U(8.W)).asUInt
        arrayRegFiles(__tmp_2791 + 0.U) := __tmp_2792(7, 0)

        CP := 62.U
      }

      is(62.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $13 = (($1: anvil.PrinterIndex.U) + (3: anvil.PrinterIndex.U))
        goto .63
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(13.U) := (generalRegFiles(1.U) + 3.U(64.W))

        CP := 63.U
      }

      is(63.U) {
        /*
        $14 = (($13: anvil.PrinterIndex.U) & ($2: anvil.PrinterIndex.U))
        goto .64
        */



        generalRegFiles(14.U) := (generalRegFiles(13.U) & generalRegFiles(2.U))

        CP := 64.U
      }

      is(64.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($14: anvil.PrinterIndex.U) as SP)) = (50: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($14: anvil.PrinterIndex.U)) = (50: U8)
        goto .65
        */


        val __tmp_2793 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(14.U).asUInt)
        val __tmp_2794 = (50.U(8.W)).asUInt
        arrayRegFiles(__tmp_2793 + 0.U) := __tmp_2794(7, 0)

        CP := 65.U
      }

      is(65.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $13 = (($1: anvil.PrinterIndex.U) + (4: anvil.PrinterIndex.U))
        goto .66
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(13.U) := (generalRegFiles(1.U) + 4.U(64.W))

        CP := 66.U
      }

      is(66.U) {
        /*
        $14 = (($13: anvil.PrinterIndex.U) & ($2: anvil.PrinterIndex.U))
        goto .67
        */



        generalRegFiles(14.U) := (generalRegFiles(13.U) & generalRegFiles(2.U))

        CP := 67.U
      }

      is(67.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($14: anvil.PrinterIndex.U) as SP)) = (51: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($14: anvil.PrinterIndex.U)) = (51: U8)
        goto .68
        */


        val __tmp_2795 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(14.U).asUInt)
        val __tmp_2796 = (51.U(8.W)).asUInt
        arrayRegFiles(__tmp_2795 + 0.U) := __tmp_2796(7, 0)

        CP := 68.U
      }

      is(68.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $13 = (($1: anvil.PrinterIndex.U) + (5: anvil.PrinterIndex.U))
        goto .69
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(13.U) := (generalRegFiles(1.U) + 5.U(64.W))

        CP := 69.U
      }

      is(69.U) {
        /*
        $14 = (($13: anvil.PrinterIndex.U) & ($2: anvil.PrinterIndex.U))
        goto .70
        */



        generalRegFiles(14.U) := (generalRegFiles(13.U) & generalRegFiles(2.U))

        CP := 70.U
      }

      is(70.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($14: anvil.PrinterIndex.U) as SP)) = (51: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($14: anvil.PrinterIndex.U)) = (51: U8)
        goto .71
        */


        val __tmp_2797 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(14.U).asUInt)
        val __tmp_2798 = (51.U(8.W)).asUInt
        arrayRegFiles(__tmp_2797 + 0.U) := __tmp_2798(7, 0)

        CP := 71.U
      }

      is(71.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $13 = (($1: anvil.PrinterIndex.U) + (6: anvil.PrinterIndex.U))
        goto .72
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(13.U) := (generalRegFiles(1.U) + 6.U(64.W))

        CP := 72.U
      }

      is(72.U) {
        /*
        $14 = (($13: anvil.PrinterIndex.U) & ($2: anvil.PrinterIndex.U))
        goto .73
        */



        generalRegFiles(14.U) := (generalRegFiles(13.U) & generalRegFiles(2.U))

        CP := 73.U
      }

      is(73.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($14: anvil.PrinterIndex.U) as SP)) = (55: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($14: anvil.PrinterIndex.U)) = (55: U8)
        goto .74
        */


        val __tmp_2799 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(14.U).asUInt)
        val __tmp_2800 = (55.U(8.W)).asUInt
        arrayRegFiles(__tmp_2799 + 0.U) := __tmp_2800(7, 0)

        CP := 74.U
      }

      is(74.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $13 = (($1: anvil.PrinterIndex.U) + (7: anvil.PrinterIndex.U))
        goto .75
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(13.U) := (generalRegFiles(1.U) + 7.U(64.W))

        CP := 75.U
      }

      is(75.U) {
        /*
        $14 = (($13: anvil.PrinterIndex.U) & ($2: anvil.PrinterIndex.U))
        goto .76
        */



        generalRegFiles(14.U) := (generalRegFiles(13.U) & generalRegFiles(2.U))

        CP := 76.U
      }

      is(76.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($14: anvil.PrinterIndex.U) as SP)) = (50: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($14: anvil.PrinterIndex.U)) = (50: U8)
        goto .77
        */


        val __tmp_2801 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(14.U).asUInt)
        val __tmp_2802 = (50.U(8.W)).asUInt
        arrayRegFiles(__tmp_2801 + 0.U) := __tmp_2802(7, 0)

        CP := 77.U
      }

      is(77.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $13 = (($1: anvil.PrinterIndex.U) + (8: anvil.PrinterIndex.U))
        goto .78
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(13.U) := (generalRegFiles(1.U) + 8.U(64.W))

        CP := 78.U
      }

      is(78.U) {
        /*
        $14 = (($13: anvil.PrinterIndex.U) & ($2: anvil.PrinterIndex.U))
        goto .79
        */



        generalRegFiles(14.U) := (generalRegFiles(13.U) & generalRegFiles(2.U))

        CP := 79.U
      }

      is(79.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($14: anvil.PrinterIndex.U) as SP)) = (48: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($14: anvil.PrinterIndex.U)) = (48: U8)
        goto .80
        */


        val __tmp_2803 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(14.U).asUInt)
        val __tmp_2804 = (48.U(8.W)).asUInt
        arrayRegFiles(__tmp_2803 + 0.U) := __tmp_2804(7, 0)

        CP := 80.U
      }

      is(80.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $13 = (($1: anvil.PrinterIndex.U) + (9: anvil.PrinterIndex.U))
        goto .81
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(13.U) := (generalRegFiles(1.U) + 9.U(64.W))

        CP := 81.U
      }

      is(81.U) {
        /*
        $14 = (($13: anvil.PrinterIndex.U) & ($2: anvil.PrinterIndex.U))
        goto .82
        */



        generalRegFiles(14.U) := (generalRegFiles(13.U) & generalRegFiles(2.U))

        CP := 82.U
      }

      is(82.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($14: anvil.PrinterIndex.U) as SP)) = (51: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($14: anvil.PrinterIndex.U)) = (51: U8)
        goto .83
        */


        val __tmp_2805 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(14.U).asUInt)
        val __tmp_2806 = (51.U(8.W)).asUInt
        arrayRegFiles(__tmp_2805 + 0.U) := __tmp_2806(7, 0)

        CP := 83.U
      }

      is(83.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $13 = (($1: anvil.PrinterIndex.U) + (10: anvil.PrinterIndex.U))
        goto .84
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(13.U) := (generalRegFiles(1.U) + 10.U(64.W))

        CP := 84.U
      }

      is(84.U) {
        /*
        $14 = (($13: anvil.PrinterIndex.U) & ($2: anvil.PrinterIndex.U))
        goto .85
        */



        generalRegFiles(14.U) := (generalRegFiles(13.U) & generalRegFiles(2.U))

        CP := 85.U
      }

      is(85.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($14: anvil.PrinterIndex.U) as SP)) = (54: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($14: anvil.PrinterIndex.U)) = (54: U8)
        goto .86
        */


        val __tmp_2807 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(14.U).asUInt)
        val __tmp_2808 = (54.U(8.W)).asUInt
        arrayRegFiles(__tmp_2807 + 0.U) := __tmp_2808(7, 0)

        CP := 86.U
      }

      is(86.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $13 = (($1: anvil.PrinterIndex.U) + (11: anvil.PrinterIndex.U))
        goto .87
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(13.U) := (generalRegFiles(1.U) + 11.U(64.W))

        CP := 87.U
      }

      is(87.U) {
        /*
        $14 = (($13: anvil.PrinterIndex.U) & ($2: anvil.PrinterIndex.U))
        goto .88
        */



        generalRegFiles(14.U) := (generalRegFiles(13.U) & generalRegFiles(2.U))

        CP := 88.U
      }

      is(88.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($14: anvil.PrinterIndex.U) as SP)) = (56: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($14: anvil.PrinterIndex.U)) = (56: U8)
        goto .89
        */


        val __tmp_2809 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(14.U).asUInt)
        val __tmp_2810 = (56.U(8.W)).asUInt
        arrayRegFiles(__tmp_2809 + 0.U) := __tmp_2810(7, 0)

        CP := 89.U
      }

      is(89.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $13 = (($1: anvil.PrinterIndex.U) + (12: anvil.PrinterIndex.U))
        goto .90
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(13.U) := (generalRegFiles(1.U) + 12.U(64.W))

        CP := 90.U
      }

      is(90.U) {
        /*
        $14 = (($13: anvil.PrinterIndex.U) & ($2: anvil.PrinterIndex.U))
        goto .91
        */



        generalRegFiles(14.U) := (generalRegFiles(13.U) & generalRegFiles(2.U))

        CP := 91.U
      }

      is(91.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($14: anvil.PrinterIndex.U) as SP)) = (53: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($14: anvil.PrinterIndex.U)) = (53: U8)
        goto .92
        */


        val __tmp_2811 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(14.U).asUInt)
        val __tmp_2812 = (53.U(8.W)).asUInt
        arrayRegFiles(__tmp_2811 + 0.U) := __tmp_2812(7, 0)

        CP := 92.U
      }

      is(92.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $13 = (($1: anvil.PrinterIndex.U) + (13: anvil.PrinterIndex.U))
        goto .93
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(13.U) := (generalRegFiles(1.U) + 13.U(64.W))

        CP := 93.U
      }

      is(93.U) {
        /*
        $14 = (($13: anvil.PrinterIndex.U) & ($2: anvil.PrinterIndex.U))
        goto .94
        */



        generalRegFiles(14.U) := (generalRegFiles(13.U) & generalRegFiles(2.U))

        CP := 94.U
      }

      is(94.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($14: anvil.PrinterIndex.U) as SP)) = (52: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($14: anvil.PrinterIndex.U)) = (52: U8)
        goto .95
        */


        val __tmp_2813 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(14.U).asUInt)
        val __tmp_2814 = (52.U(8.W)).asUInt
        arrayRegFiles(__tmp_2813 + 0.U) := __tmp_2814(7, 0)

        CP := 95.U
      }

      is(95.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $13 = (($1: anvil.PrinterIndex.U) + (14: anvil.PrinterIndex.U))
        goto .96
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(13.U) := (generalRegFiles(1.U) + 14.U(64.W))

        CP := 96.U
      }

      is(96.U) {
        /*
        $14 = (($13: anvil.PrinterIndex.U) & ($2: anvil.PrinterIndex.U))
        goto .97
        */



        generalRegFiles(14.U) := (generalRegFiles(13.U) & generalRegFiles(2.U))

        CP := 97.U
      }

      is(97.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($14: anvil.PrinterIndex.U) as SP)) = (55: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($14: anvil.PrinterIndex.U)) = (55: U8)
        goto .98
        */


        val __tmp_2815 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(14.U).asUInt)
        val __tmp_2816 = (55.U(8.W)).asUInt
        arrayRegFiles(__tmp_2815 + 0.U) := __tmp_2816(7, 0)

        CP := 98.U
      }

      is(98.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $13 = (($1: anvil.PrinterIndex.U) + (15: anvil.PrinterIndex.U))
        goto .99
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(13.U) := (generalRegFiles(1.U) + 15.U(64.W))

        CP := 99.U
      }

      is(99.U) {
        /*
        $14 = (($13: anvil.PrinterIndex.U) & ($2: anvil.PrinterIndex.U))
        goto .100
        */



        generalRegFiles(14.U) := (generalRegFiles(13.U) & generalRegFiles(2.U))

        CP := 100.U
      }

      is(100.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($14: anvil.PrinterIndex.U) as SP)) = (55: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($14: anvil.PrinterIndex.U)) = (55: U8)
        goto .101
        */


        val __tmp_2817 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(14.U).asUInt)
        val __tmp_2818 = (55.U(8.W)).asUInt
        arrayRegFiles(__tmp_2817 + 0.U) := __tmp_2818(7, 0)

        CP := 101.U
      }

      is(101.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $13 = (($1: anvil.PrinterIndex.U) + (16: anvil.PrinterIndex.U))
        goto .102
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(13.U) := (generalRegFiles(1.U) + 16.U(64.W))

        CP := 102.U
      }

      is(102.U) {
        /*
        $14 = (($13: anvil.PrinterIndex.U) & ($2: anvil.PrinterIndex.U))
        goto .103
        */



        generalRegFiles(14.U) := (generalRegFiles(13.U) & generalRegFiles(2.U))

        CP := 103.U
      }

      is(103.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($14: anvil.PrinterIndex.U) as SP)) = (53: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($14: anvil.PrinterIndex.U)) = (53: U8)
        goto .104
        */


        val __tmp_2819 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(14.U).asUInt)
        val __tmp_2820 = (53.U(8.W)).asUInt
        arrayRegFiles(__tmp_2819 + 0.U) := __tmp_2820(7, 0)

        CP := 104.U
      }

      is(104.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $13 = (($1: anvil.PrinterIndex.U) + (17: anvil.PrinterIndex.U))
        goto .105
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(13.U) := (generalRegFiles(1.U) + 17.U(64.W))

        CP := 105.U
      }

      is(105.U) {
        /*
        $14 = (($13: anvil.PrinterIndex.U) & ($2: anvil.PrinterIndex.U))
        goto .106
        */



        generalRegFiles(14.U) := (generalRegFiles(13.U) & generalRegFiles(2.U))

        CP := 106.U
      }

      is(106.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($14: anvil.PrinterIndex.U) as SP)) = (56: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($14: anvil.PrinterIndex.U)) = (56: U8)
        goto .107
        */


        val __tmp_2821 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(14.U).asUInt)
        val __tmp_2822 = (56.U(8.W)).asUInt
        arrayRegFiles(__tmp_2821 + 0.U) := __tmp_2822(7, 0)

        CP := 107.U
      }

      is(107.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $13 = (($1: anvil.PrinterIndex.U) + (18: anvil.PrinterIndex.U))
        goto .108
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(13.U) := (generalRegFiles(1.U) + 18.U(64.W))

        CP := 108.U
      }

      is(108.U) {
        /*
        $14 = (($13: anvil.PrinterIndex.U) & ($2: anvil.PrinterIndex.U))
        goto .109
        */



        generalRegFiles(14.U) := (generalRegFiles(13.U) & generalRegFiles(2.U))

        CP := 109.U
      }

      is(109.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($14: anvil.PrinterIndex.U) as SP)) = (48: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($14: anvil.PrinterIndex.U)) = (48: U8)
        goto .110
        */


        val __tmp_2823 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(14.U).asUInt)
        val __tmp_2824 = (48.U(8.W)).asUInt
        arrayRegFiles(__tmp_2823 + 0.U) := __tmp_2824(7, 0)

        CP := 110.U
      }

      is(110.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $13 = (($1: anvil.PrinterIndex.U) + (19: anvil.PrinterIndex.U))
        goto .111
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(13.U) := (generalRegFiles(1.U) + 19.U(64.W))

        CP := 111.U
      }

      is(111.U) {
        /*
        $14 = (($13: anvil.PrinterIndex.U) & ($2: anvil.PrinterIndex.U))
        goto .112
        */



        generalRegFiles(14.U) := (generalRegFiles(13.U) & generalRegFiles(2.U))

        CP := 112.U
      }

      is(112.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($14: anvil.PrinterIndex.U) as SP)) = (56: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($14: anvil.PrinterIndex.U)) = (56: U8)
        goto .113
        */


        val __tmp_2825 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(14.U).asUInt)
        val __tmp_2826 = (56.U(8.W)).asUInt
        arrayRegFiles(__tmp_2825 + 0.U) := __tmp_2826(7, 0)

        CP := 113.U
      }

      is(113.U) {
        /*
        **(SP + (2: SP)) = (20: U64) [unsigned, U64, 8]  // $res = (20: U64)
        goto $ret@0
        */


        val __tmp_2827 = Cat(
          arrayRegFiles((SP + 2.U(16.W)) + 1.U),
          arrayRegFiles((SP + 2.U(16.W)) + 0.U)
        )
        val __tmp_2828 = (20.U(64.W)).asUInt
        arrayRegFiles(__tmp_2827 + 0.U) := __tmp_2828(7, 0)
        arrayRegFiles(__tmp_2827 + 1.U) := __tmp_2828(15, 8)
        arrayRegFiles(__tmp_2827 + 2.U) := __tmp_2828(23, 16)
        arrayRegFiles(__tmp_2827 + 3.U) := __tmp_2828(31, 24)
        arrayRegFiles(__tmp_2827 + 4.U) := __tmp_2828(39, 32)
        arrayRegFiles(__tmp_2827 + 5.U) := __tmp_2828(47, 40)
        arrayRegFiles(__tmp_2827 + 6.U) := __tmp_2828(55, 48)
        arrayRegFiles(__tmp_2827 + 7.U) := __tmp_2828(63, 56)

        CP := Cat(
          arrayRegFiles(SP + 0.U + 1.U),
          arrayRegFiles(SP + 0.U + 0.U)
        )

      }

      is(114.U) {
        /*
        $10 = (($3: S64) ≡ (0: S64))
        goto .115
        */



        generalRegFiles(10.U) := (generalRegFiles(3.U).asSInt === 0.S(64.W)).asUInt

        CP := 115.U
      }

      is(115.U) {
        /*
        if ($10: B) goto .116 else goto .119
        */


        CP := Mux((generalRegFiles(10.U).asUInt) === 1.U, 116.U, 119.U)
      }

      is(116.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $12 = (($1: anvil.PrinterIndex.U) & ($2: anvil.PrinterIndex.U))
        goto .117
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(12.U) := (generalRegFiles(1.U) & generalRegFiles(2.U))

        CP := 117.U
      }

      is(117.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($12: anvil.PrinterIndex.U) as SP)) = (48: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($12: anvil.PrinterIndex.U)) = (48: U8)
        goto .118
        */


        val __tmp_2829 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(12.U).asUInt)
        val __tmp_2830 = (48.U(8.W)).asUInt
        arrayRegFiles(__tmp_2829 + 0.U) := __tmp_2830(7, 0)

        CP := 118.U
      }

      is(118.U) {
        /*
        **(SP + (2: SP)) = (1: U64) [unsigned, U64, 8]  // $res = (1: U64)
        goto $ret@0
        */


        val __tmp_2831 = Cat(
          arrayRegFiles((SP + 2.U(16.W)) + 1.U),
          arrayRegFiles((SP + 2.U(16.W)) + 0.U)
        )
        val __tmp_2832 = (1.U(64.W)).asUInt
        arrayRegFiles(__tmp_2831 + 0.U) := __tmp_2832(7, 0)
        arrayRegFiles(__tmp_2831 + 1.U) := __tmp_2832(15, 8)
        arrayRegFiles(__tmp_2831 + 2.U) := __tmp_2832(23, 16)
        arrayRegFiles(__tmp_2831 + 3.U) := __tmp_2832(31, 24)
        arrayRegFiles(__tmp_2831 + 4.U) := __tmp_2832(39, 32)
        arrayRegFiles(__tmp_2831 + 5.U) := __tmp_2832(47, 40)
        arrayRegFiles(__tmp_2831 + 6.U) := __tmp_2832(55, 48)
        arrayRegFiles(__tmp_2831 + 7.U) := __tmp_2832(63, 56)

        CP := Cat(
          arrayRegFiles(SP + 0.U + 1.U),
          arrayRegFiles(SP + 0.U + 0.U)
        )

      }

      is(119.U) {
        /*
        decl buff: MS[anvil.PrinterIndex.I20, U8] [@4, 34]
        alloc $new@[168,16].5BB7E063: MS[anvil.PrinterIndex.I20, U8] [@38, 34]
        $11 = (SP + (38: SP))
        *(SP + (38: SP)) = (323602724: U32) [unsigned, U32, 4]  // sha3 type signature of MS[anvil.PrinterIndex.I20, U8]: 0x1349C924
        *(SP + (42: SP)) = (20: Z) [signed, Z, 8]  // size of MS[anvil.PrinterIndex.I20, U8]((0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8))
        goto .120
        */



        generalRegFiles(11.U) := (SP + 38.U(16.W))

        val __tmp_2833 = (SP + 38.U(16.W))
        val __tmp_2834 = (323602724.U(32.W)).asUInt
        arrayRegFiles(__tmp_2833 + 0.U) := __tmp_2834(7, 0)
        arrayRegFiles(__tmp_2833 + 1.U) := __tmp_2834(15, 8)
        arrayRegFiles(__tmp_2833 + 2.U) := __tmp_2834(23, 16)
        arrayRegFiles(__tmp_2833 + 3.U) := __tmp_2834(31, 24)

        val __tmp_2835 = (SP + 42.U(16.W))
        val __tmp_2836 = (20.S(64.W)).asUInt
        arrayRegFiles(__tmp_2835 + 0.U) := __tmp_2836(7, 0)
        arrayRegFiles(__tmp_2835 + 1.U) := __tmp_2836(15, 8)
        arrayRegFiles(__tmp_2835 + 2.U) := __tmp_2836(23, 16)
        arrayRegFiles(__tmp_2835 + 3.U) := __tmp_2836(31, 24)
        arrayRegFiles(__tmp_2835 + 4.U) := __tmp_2836(39, 32)
        arrayRegFiles(__tmp_2835 + 5.U) := __tmp_2836(47, 40)
        arrayRegFiles(__tmp_2835 + 6.U) := __tmp_2836(55, 48)
        arrayRegFiles(__tmp_2835 + 7.U) := __tmp_2836(63, 56)

        CP := 120.U
      }

      is(120.U) {
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
        goto .121
        */


        val __tmp_2837 = ((generalRegFiles(11.U) + 12.U(16.W)) + 0.S(8.W).asUInt)
        val __tmp_2838 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_2837 + 0.U) := __tmp_2838(7, 0)

        val __tmp_2839 = ((generalRegFiles(11.U) + 12.U(16.W)) + 1.S(8.W).asUInt)
        val __tmp_2840 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_2839 + 0.U) := __tmp_2840(7, 0)

        val __tmp_2841 = ((generalRegFiles(11.U) + 12.U(16.W)) + 2.S(8.W).asUInt)
        val __tmp_2842 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_2841 + 0.U) := __tmp_2842(7, 0)

        val __tmp_2843 = ((generalRegFiles(11.U) + 12.U(16.W)) + 3.S(8.W).asUInt)
        val __tmp_2844 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_2843 + 0.U) := __tmp_2844(7, 0)

        val __tmp_2845 = ((generalRegFiles(11.U) + 12.U(16.W)) + 4.S(8.W).asUInt)
        val __tmp_2846 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_2845 + 0.U) := __tmp_2846(7, 0)

        val __tmp_2847 = ((generalRegFiles(11.U) + 12.U(16.W)) + 5.S(8.W).asUInt)
        val __tmp_2848 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_2847 + 0.U) := __tmp_2848(7, 0)

        val __tmp_2849 = ((generalRegFiles(11.U) + 12.U(16.W)) + 6.S(8.W).asUInt)
        val __tmp_2850 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_2849 + 0.U) := __tmp_2850(7, 0)

        val __tmp_2851 = ((generalRegFiles(11.U) + 12.U(16.W)) + 7.S(8.W).asUInt)
        val __tmp_2852 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_2851 + 0.U) := __tmp_2852(7, 0)

        val __tmp_2853 = ((generalRegFiles(11.U) + 12.U(16.W)) + 8.S(8.W).asUInt)
        val __tmp_2854 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_2853 + 0.U) := __tmp_2854(7, 0)

        val __tmp_2855 = ((generalRegFiles(11.U) + 12.U(16.W)) + 9.S(8.W).asUInt)
        val __tmp_2856 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_2855 + 0.U) := __tmp_2856(7, 0)

        val __tmp_2857 = ((generalRegFiles(11.U) + 12.U(16.W)) + 10.S(8.W).asUInt)
        val __tmp_2858 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_2857 + 0.U) := __tmp_2858(7, 0)

        val __tmp_2859 = ((generalRegFiles(11.U) + 12.U(16.W)) + 11.S(8.W).asUInt)
        val __tmp_2860 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_2859 + 0.U) := __tmp_2860(7, 0)

        val __tmp_2861 = ((generalRegFiles(11.U) + 12.U(16.W)) + 12.S(8.W).asUInt)
        val __tmp_2862 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_2861 + 0.U) := __tmp_2862(7, 0)

        val __tmp_2863 = ((generalRegFiles(11.U) + 12.U(16.W)) + 13.S(8.W).asUInt)
        val __tmp_2864 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_2863 + 0.U) := __tmp_2864(7, 0)

        val __tmp_2865 = ((generalRegFiles(11.U) + 12.U(16.W)) + 14.S(8.W).asUInt)
        val __tmp_2866 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_2865 + 0.U) := __tmp_2866(7, 0)

        val __tmp_2867 = ((generalRegFiles(11.U) + 12.U(16.W)) + 15.S(8.W).asUInt)
        val __tmp_2868 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_2867 + 0.U) := __tmp_2868(7, 0)

        val __tmp_2869 = ((generalRegFiles(11.U) + 12.U(16.W)) + 16.S(8.W).asUInt)
        val __tmp_2870 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_2869 + 0.U) := __tmp_2870(7, 0)

        val __tmp_2871 = ((generalRegFiles(11.U) + 12.U(16.W)) + 17.S(8.W).asUInt)
        val __tmp_2872 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_2871 + 0.U) := __tmp_2872(7, 0)

        val __tmp_2873 = ((generalRegFiles(11.U) + 12.U(16.W)) + 18.S(8.W).asUInt)
        val __tmp_2874 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_2873 + 0.U) := __tmp_2874(7, 0)

        val __tmp_2875 = ((generalRegFiles(11.U) + 12.U(16.W)) + 19.S(8.W).asUInt)
        val __tmp_2876 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_2875 + 0.U) := __tmp_2876(7, 0)

        CP := 121.U
      }

      is(121.U) {
        /*
        (SP + (4: SP)) [MS[anvil.PrinterIndex.I20, U8], 34]  <-  ($11: MS[anvil.PrinterIndex.I20, U8]) [MS[anvil.PrinterIndex.I20, U8], ((*(($11: MS[anvil.PrinterIndex.I20, U8]) + (4: SP)) as SP) + (12: SP))]  // buff = ($11: MS[anvil.PrinterIndex.I20, U8])
        goto .122
        */


        val __tmp_2877 = (SP + 4.U(16.W))
        val __tmp_2878 = generalRegFiles(11.U)
        val __tmp_2879 = (Cat(
           arrayRegFiles((generalRegFiles(11.U) + 4.U(16.W)) + 7.U),
           arrayRegFiles((generalRegFiles(11.U) + 4.U(16.W)) + 6.U),
           arrayRegFiles((generalRegFiles(11.U) + 4.U(16.W)) + 5.U),
           arrayRegFiles((generalRegFiles(11.U) + 4.U(16.W)) + 4.U),
           arrayRegFiles((generalRegFiles(11.U) + 4.U(16.W)) + 3.U),
           arrayRegFiles((generalRegFiles(11.U) + 4.U(16.W)) + 2.U),
           arrayRegFiles((generalRegFiles(11.U) + 4.U(16.W)) + 1.U),
           arrayRegFiles((generalRegFiles(11.U) + 4.U(16.W)) + 0.U)
         ).asSInt.asUInt + 12.U(16.W))

        when(Idx < __tmp_2879) {
          arrayRegFiles(__tmp_2877 + Idx + 0.U) := arrayRegFiles(__tmp_2878 + Idx + 0.U)
          arrayRegFiles(__tmp_2877 + Idx + 1.U) := arrayRegFiles(__tmp_2878 + Idx + 1.U)
          arrayRegFiles(__tmp_2877 + Idx + 2.U) := arrayRegFiles(__tmp_2878 + Idx + 2.U)
          arrayRegFiles(__tmp_2877 + Idx + 3.U) := arrayRegFiles(__tmp_2878 + Idx + 3.U)
          arrayRegFiles(__tmp_2877 + Idx + 4.U) := arrayRegFiles(__tmp_2878 + Idx + 4.U)
          arrayRegFiles(__tmp_2877 + Idx + 5.U) := arrayRegFiles(__tmp_2878 + Idx + 5.U)
          arrayRegFiles(__tmp_2877 + Idx + 6.U) := arrayRegFiles(__tmp_2878 + Idx + 6.U)
          arrayRegFiles(__tmp_2877 + Idx + 7.U) := arrayRegFiles(__tmp_2878 + Idx + 7.U)
          Idx := Idx + 8.U
          LeftByteRounds := __tmp_2879 - Idx
        } .elsewhen(IdxLeftByteRounds < LeftByteRounds) {
          val __tmp_2880 = Idx - 8.U
          arrayRegFiles(__tmp_2877 + __tmp_2880 + IdxLeftByteRounds) := arrayRegFiles(__tmp_2878 + __tmp_2880 + IdxLeftByteRounds)
          IdxLeftByteRounds := IdxLeftByteRounds + 1.U
        } .otherwise {
          Idx := 0.U
          IdxLeftByteRounds := 0.U
          LeftByteRounds := 0.U
          CP := 122.U
        }


      }

      is(122.U) {
        /*
        unalloc $new@[168,16].5BB7E063: MS[anvil.PrinterIndex.I20, U8] [@38, 34]
        goto .123
        */


        CP := 123.U
      }

      is(123.U) {
        /*
        decl i: anvil.PrinterIndex.I20 @$4
        $4 = (0: anvil.PrinterIndex.I20)
        goto .124
        */



        generalRegFiles(4.U) := (0.S(8.W)).asUInt

        CP := 124.U
      }

      is(124.U) {
        /*
        decl neg: B @$5
        $10 = (($3: S64) < (0: S64))
        goto .125
        */



        generalRegFiles(10.U) := (generalRegFiles(3.U).asSInt < 0.S(64.W)).asUInt

        CP := 125.U
      }

      is(125.U) {
        /*
        $5 = ($10: B)
        goto .126
        */



        generalRegFiles(5.U) := generalRegFiles(10.U)

        CP := 126.U
      }

      is(126.U) {
        /*
        decl m: S64 @$6
        goto .127
        */


        CP := 127.U
      }

      is(127.U) {
        /*
        if ($5: B) goto .128 else goto .130
        */


        CP := Mux((generalRegFiles(5.U).asUInt) === 1.U, 128.U, 130.U)
      }

      is(128.U) {
        /*
        $12 = -(($3: S64))
        goto .129
        */



        generalRegFiles(12.U) := (-generalRegFiles(3.U).asSInt).asUInt

        CP := 129.U
      }

      is(129.U) {
        /*
        $10 = ($12: S64)
        goto .132
        */



        generalRegFiles(10.U) := (generalRegFiles(12.U).asSInt).asUInt

        CP := 132.U
      }

      is(130.U) {
        /*
        $14 = ($3: S64)
        goto .131
        */



        generalRegFiles(14.U) := (generalRegFiles(3.U).asSInt).asUInt

        CP := 131.U
      }

      is(131.U) {
        /*
        $10 = ($14: S64)
        goto .132
        */



        generalRegFiles(10.U) := (generalRegFiles(14.U).asSInt).asUInt

        CP := 132.U
      }

      is(132.U) {
        /*
        $6 = ($10: S64)
        goto .133
        */



        generalRegFiles(6.U) := (generalRegFiles(10.U).asSInt).asUInt

        CP := 133.U
      }

      is(133.U) {
        /*
        $11 = ($6: S64)
        goto .134
        */



        generalRegFiles(11.U) := (generalRegFiles(6.U).asSInt).asUInt

        CP := 134.U
      }

      is(134.U) {
        /*
        $10 = (($11: S64) > (0: S64))
        goto .135
        */



        generalRegFiles(10.U) := (generalRegFiles(11.U).asSInt > 0.S(64.W)).asUInt

        CP := 135.U
      }

      is(135.U) {
        /*
        if ($10: B) goto .136 else goto .165
        */


        CP := Mux((generalRegFiles(10.U).asUInt) === 1.U, 136.U, 165.U)
      }

      is(136.U) {
        /*
        $11 = ($6: S64)
        goto .137
        */



        generalRegFiles(11.U) := (generalRegFiles(6.U).asSInt).asUInt

        CP := 137.U
      }

      is(137.U) {
        /*
        $10 = (($11: S64) % (10: S64))
        goto .138
        */



        generalRegFiles(10.U) := ((generalRegFiles(11.U).asSInt % 10.S(64.W))).asUInt

        CP := 138.U
      }

      is(138.U) {
        /*
        switch (($10: S64))
          (0: S64): goto 139
          (1: S64): goto 141
          (2: S64): goto 143
          (3: S64): goto 145
          (4: S64): goto 147
          (5: S64): goto 149
          (6: S64): goto 151
          (7: S64): goto 153
          (8: S64): goto 155
          (9: S64): goto 157

        */


        val __tmp_2881 = generalRegFiles(10.U).asSInt

        switch(__tmp_2881) {

          is(0.S(64.W)) {
            CP := 139.U
          }


          is(1.S(64.W)) {
            CP := 141.U
          }


          is(2.S(64.W)) {
            CP := 143.U
          }


          is(3.S(64.W)) {
            CP := 145.U
          }


          is(4.S(64.W)) {
            CP := 147.U
          }


          is(5.S(64.W)) {
            CP := 149.U
          }


          is(6.S(64.W)) {
            CP := 151.U
          }


          is(7.S(64.W)) {
            CP := 153.U
          }


          is(8.S(64.W)) {
            CP := 155.U
          }


          is(9.S(64.W)) {
            CP := 157.U
          }

        }

      }

      is(139.U) {
        /*
        $11 = (SP + (4: SP))
        $10 = ($4: anvil.PrinterIndex.I20)
        goto .140
        */



        generalRegFiles(11.U) := (SP + 4.U(16.W))


        generalRegFiles(10.U) := (generalRegFiles(4.U).asSInt).asUInt

        CP := 140.U
      }

      is(140.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.I20, U8]) + (12: SP)) + (($10: anvil.PrinterIndex.I20) as SP)) = (48: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.I20, U8])(($10: anvil.PrinterIndex.I20)) = (48: U8)
        goto .159
        */


        val __tmp_2882 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(10.U).asSInt.asUInt)
        val __tmp_2883 = (48.U(8.W)).asUInt
        arrayRegFiles(__tmp_2882 + 0.U) := __tmp_2883(7, 0)

        CP := 159.U
      }

      is(141.U) {
        /*
        $11 = (SP + (4: SP))
        $10 = ($4: anvil.PrinterIndex.I20)
        goto .142
        */



        generalRegFiles(11.U) := (SP + 4.U(16.W))


        generalRegFiles(10.U) := (generalRegFiles(4.U).asSInt).asUInt

        CP := 142.U
      }

      is(142.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.I20, U8]) + (12: SP)) + (($10: anvil.PrinterIndex.I20) as SP)) = (49: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.I20, U8])(($10: anvil.PrinterIndex.I20)) = (49: U8)
        goto .159
        */


        val __tmp_2884 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(10.U).asSInt.asUInt)
        val __tmp_2885 = (49.U(8.W)).asUInt
        arrayRegFiles(__tmp_2884 + 0.U) := __tmp_2885(7, 0)

        CP := 159.U
      }

      is(143.U) {
        /*
        $11 = (SP + (4: SP))
        $10 = ($4: anvil.PrinterIndex.I20)
        goto .144
        */



        generalRegFiles(11.U) := (SP + 4.U(16.W))


        generalRegFiles(10.U) := (generalRegFiles(4.U).asSInt).asUInt

        CP := 144.U
      }

      is(144.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.I20, U8]) + (12: SP)) + (($10: anvil.PrinterIndex.I20) as SP)) = (50: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.I20, U8])(($10: anvil.PrinterIndex.I20)) = (50: U8)
        goto .159
        */


        val __tmp_2886 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(10.U).asSInt.asUInt)
        val __tmp_2887 = (50.U(8.W)).asUInt
        arrayRegFiles(__tmp_2886 + 0.U) := __tmp_2887(7, 0)

        CP := 159.U
      }

      is(145.U) {
        /*
        $11 = (SP + (4: SP))
        $10 = ($4: anvil.PrinterIndex.I20)
        goto .146
        */



        generalRegFiles(11.U) := (SP + 4.U(16.W))


        generalRegFiles(10.U) := (generalRegFiles(4.U).asSInt).asUInt

        CP := 146.U
      }

      is(146.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.I20, U8]) + (12: SP)) + (($10: anvil.PrinterIndex.I20) as SP)) = (51: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.I20, U8])(($10: anvil.PrinterIndex.I20)) = (51: U8)
        goto .159
        */


        val __tmp_2888 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(10.U).asSInt.asUInt)
        val __tmp_2889 = (51.U(8.W)).asUInt
        arrayRegFiles(__tmp_2888 + 0.U) := __tmp_2889(7, 0)

        CP := 159.U
      }

      is(147.U) {
        /*
        $11 = (SP + (4: SP))
        $10 = ($4: anvil.PrinterIndex.I20)
        goto .148
        */



        generalRegFiles(11.U) := (SP + 4.U(16.W))


        generalRegFiles(10.U) := (generalRegFiles(4.U).asSInt).asUInt

        CP := 148.U
      }

      is(148.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.I20, U8]) + (12: SP)) + (($10: anvil.PrinterIndex.I20) as SP)) = (52: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.I20, U8])(($10: anvil.PrinterIndex.I20)) = (52: U8)
        goto .159
        */


        val __tmp_2890 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(10.U).asSInt.asUInt)
        val __tmp_2891 = (52.U(8.W)).asUInt
        arrayRegFiles(__tmp_2890 + 0.U) := __tmp_2891(7, 0)

        CP := 159.U
      }

      is(149.U) {
        /*
        $11 = (SP + (4: SP))
        $10 = ($4: anvil.PrinterIndex.I20)
        goto .150
        */



        generalRegFiles(11.U) := (SP + 4.U(16.W))


        generalRegFiles(10.U) := (generalRegFiles(4.U).asSInt).asUInt

        CP := 150.U
      }

      is(150.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.I20, U8]) + (12: SP)) + (($10: anvil.PrinterIndex.I20) as SP)) = (53: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.I20, U8])(($10: anvil.PrinterIndex.I20)) = (53: U8)
        goto .159
        */


        val __tmp_2892 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(10.U).asSInt.asUInt)
        val __tmp_2893 = (53.U(8.W)).asUInt
        arrayRegFiles(__tmp_2892 + 0.U) := __tmp_2893(7, 0)

        CP := 159.U
      }

      is(151.U) {
        /*
        $11 = (SP + (4: SP))
        $10 = ($4: anvil.PrinterIndex.I20)
        goto .152
        */



        generalRegFiles(11.U) := (SP + 4.U(16.W))


        generalRegFiles(10.U) := (generalRegFiles(4.U).asSInt).asUInt

        CP := 152.U
      }

      is(152.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.I20, U8]) + (12: SP)) + (($10: anvil.PrinterIndex.I20) as SP)) = (54: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.I20, U8])(($10: anvil.PrinterIndex.I20)) = (54: U8)
        goto .159
        */


        val __tmp_2894 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(10.U).asSInt.asUInt)
        val __tmp_2895 = (54.U(8.W)).asUInt
        arrayRegFiles(__tmp_2894 + 0.U) := __tmp_2895(7, 0)

        CP := 159.U
      }

      is(153.U) {
        /*
        $11 = (SP + (4: SP))
        $10 = ($4: anvil.PrinterIndex.I20)
        goto .154
        */



        generalRegFiles(11.U) := (SP + 4.U(16.W))


        generalRegFiles(10.U) := (generalRegFiles(4.U).asSInt).asUInt

        CP := 154.U
      }

      is(154.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.I20, U8]) + (12: SP)) + (($10: anvil.PrinterIndex.I20) as SP)) = (55: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.I20, U8])(($10: anvil.PrinterIndex.I20)) = (55: U8)
        goto .159
        */


        val __tmp_2896 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(10.U).asSInt.asUInt)
        val __tmp_2897 = (55.U(8.W)).asUInt
        arrayRegFiles(__tmp_2896 + 0.U) := __tmp_2897(7, 0)

        CP := 159.U
      }

      is(155.U) {
        /*
        $11 = (SP + (4: SP))
        $10 = ($4: anvil.PrinterIndex.I20)
        goto .156
        */



        generalRegFiles(11.U) := (SP + 4.U(16.W))


        generalRegFiles(10.U) := (generalRegFiles(4.U).asSInt).asUInt

        CP := 156.U
      }

      is(156.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.I20, U8]) + (12: SP)) + (($10: anvil.PrinterIndex.I20) as SP)) = (56: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.I20, U8])(($10: anvil.PrinterIndex.I20)) = (56: U8)
        goto .159
        */


        val __tmp_2898 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(10.U).asSInt.asUInt)
        val __tmp_2899 = (56.U(8.W)).asUInt
        arrayRegFiles(__tmp_2898 + 0.U) := __tmp_2899(7, 0)

        CP := 159.U
      }

      is(157.U) {
        /*
        $11 = (SP + (4: SP))
        $10 = ($4: anvil.PrinterIndex.I20)
        goto .158
        */



        generalRegFiles(11.U) := (SP + 4.U(16.W))


        generalRegFiles(10.U) := (generalRegFiles(4.U).asSInt).asUInt

        CP := 158.U
      }

      is(158.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.I20, U8]) + (12: SP)) + (($10: anvil.PrinterIndex.I20) as SP)) = (57: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.I20, U8])(($10: anvil.PrinterIndex.I20)) = (57: U8)
        goto .159
        */


        val __tmp_2900 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(10.U).asSInt.asUInt)
        val __tmp_2901 = (57.U(8.W)).asUInt
        arrayRegFiles(__tmp_2900 + 0.U) := __tmp_2901(7, 0)

        CP := 159.U
      }

      is(159.U) {
        /*
        $11 = ($6: S64)
        goto .160
        */



        generalRegFiles(11.U) := (generalRegFiles(6.U).asSInt).asUInt

        CP := 160.U
      }

      is(160.U) {
        /*
        $10 = (($11: S64) / (10: S64))
        goto .161
        */



        generalRegFiles(10.U) := ((generalRegFiles(11.U).asSInt / 10.S(64.W))).asUInt

        CP := 161.U
      }

      is(161.U) {
        /*
        $6 = ($10: S64)
        goto .162
        */



        generalRegFiles(6.U) := (generalRegFiles(10.U).asSInt).asUInt

        CP := 162.U
      }

      is(162.U) {
        /*
        $11 = ($4: anvil.PrinterIndex.I20)
        goto .163
        */



        generalRegFiles(11.U) := (generalRegFiles(4.U).asSInt).asUInt

        CP := 163.U
      }

      is(163.U) {
        /*
        $10 = (($11: anvil.PrinterIndex.I20) + (1: anvil.PrinterIndex.I20))
        goto .164
        */



        generalRegFiles(10.U) := ((generalRegFiles(11.U).asSInt + 1.S(8.W))).asUInt

        CP := 164.U
      }

      is(164.U) {
        /*
        $4 = ($10: anvil.PrinterIndex.I20)
        goto .133
        */



        generalRegFiles(4.U) := (generalRegFiles(10.U).asSInt).asUInt

        CP := 133.U
      }

      is(165.U) {
        /*
        $11 = ($5: B)
        undecl neg: B @$5
        goto .166
        */



        generalRegFiles(11.U) := generalRegFiles(5.U)

        CP := 166.U
      }

      is(166.U) {
        /*
        if ($11: B) goto .167 else goto .172
        */


        CP := Mux((generalRegFiles(11.U).asUInt) === 1.U, 167.U, 172.U)
      }

      is(167.U) {
        /*
        $11 = (SP + (4: SP))
        $10 = ($4: anvil.PrinterIndex.I20)
        goto .168
        */



        generalRegFiles(11.U) := (SP + 4.U(16.W))


        generalRegFiles(10.U) := (generalRegFiles(4.U).asSInt).asUInt

        CP := 168.U
      }

      is(168.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.I20, U8]) + (12: SP)) + (($10: anvil.PrinterIndex.I20) as SP)) = (45: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.I20, U8])(($10: anvil.PrinterIndex.I20)) = (45: U8)
        goto .169
        */


        val __tmp_2902 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(10.U).asSInt.asUInt)
        val __tmp_2903 = (45.U(8.W)).asUInt
        arrayRegFiles(__tmp_2902 + 0.U) := __tmp_2903(7, 0)

        CP := 169.U
      }

      is(169.U) {
        /*
        $11 = ($4: anvil.PrinterIndex.I20)
        goto .170
        */



        generalRegFiles(11.U) := (generalRegFiles(4.U).asSInt).asUInt

        CP := 170.U
      }

      is(170.U) {
        /*
        $10 = (($11: anvil.PrinterIndex.I20) + (1: anvil.PrinterIndex.I20))
        goto .171
        */



        generalRegFiles(10.U) := ((generalRegFiles(11.U).asSInt + 1.S(8.W))).asUInt

        CP := 171.U
      }

      is(171.U) {
        /*
        $4 = ($10: anvil.PrinterIndex.I20)
        goto .172
        */



        generalRegFiles(4.U) := (generalRegFiles(10.U).asSInt).asUInt

        CP := 172.U
      }

      is(172.U) {
        /*
        decl j: anvil.PrinterIndex.I20 @$7
        $11 = ($4: anvil.PrinterIndex.I20)
        undecl i: anvil.PrinterIndex.I20 @$4
        goto .173
        */



        generalRegFiles(11.U) := (generalRegFiles(4.U).asSInt).asUInt

        CP := 173.U
      }

      is(173.U) {
        /*
        $10 = (($11: anvil.PrinterIndex.I20) - (1: anvil.PrinterIndex.I20))
        goto .174
        */



        generalRegFiles(10.U) := ((generalRegFiles(11.U).asSInt - 1.S(8.W))).asUInt

        CP := 174.U
      }

      is(174.U) {
        /*
        $7 = ($10: anvil.PrinterIndex.I20)
        goto .175
        */



        generalRegFiles(7.U) := (generalRegFiles(10.U).asSInt).asUInt

        CP := 175.U
      }

      is(175.U) {
        /*
        decl idx: anvil.PrinterIndex.U @$8
        $11 = ($1: anvil.PrinterIndex.U)
        goto .176
        */



        generalRegFiles(11.U) := generalRegFiles(1.U)

        CP := 176.U
      }

      is(176.U) {
        /*
        $8 = ($11: anvil.PrinterIndex.U)
        goto .177
        */



        generalRegFiles(8.U) := generalRegFiles(11.U)

        CP := 177.U
      }

      is(177.U) {
        /*
        decl r: U64 @$9
        $9 = (0: U64)
        goto .178
        */



        generalRegFiles(9.U) := 0.U(64.W)

        CP := 178.U
      }

      is(178.U) {
        /*
        $11 = ($7: anvil.PrinterIndex.I20)
        goto .179
        */



        generalRegFiles(11.U) := (generalRegFiles(7.U).asSInt).asUInt

        CP := 179.U
      }

      is(179.U) {
        /*
        $10 = (($11: anvil.PrinterIndex.I20) >= (0: anvil.PrinterIndex.I20))
        goto .180
        */



        generalRegFiles(10.U) := (generalRegFiles(11.U).asSInt >= 0.S(8.W)).asUInt

        CP := 180.U
      }

      is(180.U) {
        /*
        if ($10: B) goto .181 else goto .195
        */


        CP := Mux((generalRegFiles(10.U).asUInt) === 1.U, 181.U, 195.U)
      }

      is(181.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $10 = ($8: anvil.PrinterIndex.U)
        $13 = ($2: anvil.PrinterIndex.U)
        goto .182
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(10.U) := generalRegFiles(8.U)


        generalRegFiles(13.U) := generalRegFiles(2.U)

        CP := 182.U
      }

      is(182.U) {
        /*
        $12 = (($10: anvil.PrinterIndex.U) & ($13: anvil.PrinterIndex.U))
        goto .183
        */



        generalRegFiles(12.U) := (generalRegFiles(10.U) & generalRegFiles(13.U))

        CP := 183.U
      }

      is(183.U) {
        /*
        $14 = (SP + (4: SP))
        $15 = ($7: anvil.PrinterIndex.I20)
        goto .184
        */



        generalRegFiles(14.U) := (SP + 4.U(16.W))


        generalRegFiles(15.U) := (generalRegFiles(7.U).asSInt).asUInt

        CP := 184.U
      }

      is(184.U) {
        /*
        $16 = *((($14: MS[anvil.PrinterIndex.I20, U8]) + (12: SP)) + (($15: anvil.PrinterIndex.I20) as SP)) [unsigned, U8, 1]  // $16 = ($14: MS[anvil.PrinterIndex.I20, U8])(($15: anvil.PrinterIndex.I20))
        goto .185
        */


        val __tmp_2904 = (((generalRegFiles(14.U) + 12.U(16.W)) + generalRegFiles(15.U).asSInt.asUInt)).asUInt
        generalRegFiles(16.U) := Cat(
          arrayRegFiles(__tmp_2904 + 0.U)
        ).asUInt

        CP := 185.U
      }

      is(185.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($12: anvil.PrinterIndex.U) as SP)) = ($16: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($12: anvil.PrinterIndex.U)) = ($16: U8)
        goto .186
        */


        val __tmp_2905 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(12.U).asUInt)
        val __tmp_2906 = (generalRegFiles(16.U)).asUInt
        arrayRegFiles(__tmp_2905 + 0.U) := __tmp_2906(7, 0)

        CP := 186.U
      }

      is(186.U) {
        /*
        $11 = ($7: anvil.PrinterIndex.I20)
        goto .187
        */



        generalRegFiles(11.U) := (generalRegFiles(7.U).asSInt).asUInt

        CP := 187.U
      }

      is(187.U) {
        /*
        $10 = (($11: anvil.PrinterIndex.I20) - (1: anvil.PrinterIndex.I20))
        goto .188
        */



        generalRegFiles(10.U) := ((generalRegFiles(11.U).asSInt - 1.S(8.W))).asUInt

        CP := 188.U
      }

      is(188.U) {
        /*
        $7 = ($10: anvil.PrinterIndex.I20)
        goto .189
        */



        generalRegFiles(7.U) := (generalRegFiles(10.U).asSInt).asUInt

        CP := 189.U
      }

      is(189.U) {
        /*
        $11 = ($8: anvil.PrinterIndex.U)
        goto .190
        */



        generalRegFiles(11.U) := generalRegFiles(8.U)

        CP := 190.U
      }

      is(190.U) {
        /*
        $10 = (($11: anvil.PrinterIndex.U) + (1: anvil.PrinterIndex.U))
        goto .191
        */



        generalRegFiles(10.U) := (generalRegFiles(11.U) + 1.U(64.W))

        CP := 191.U
      }

      is(191.U) {
        /*
        $8 = ($10: anvil.PrinterIndex.U)
        goto .192
        */



        generalRegFiles(8.U) := generalRegFiles(10.U)

        CP := 192.U
      }

      is(192.U) {
        /*
        $11 = ($9: U64)
        goto .193
        */



        generalRegFiles(11.U) := generalRegFiles(9.U)

        CP := 193.U
      }

      is(193.U) {
        /*
        $10 = (($11: U64) + (1: U64))
        goto .194
        */



        generalRegFiles(10.U) := (generalRegFiles(11.U) + 1.U(64.W))

        CP := 194.U
      }

      is(194.U) {
        /*
        $9 = ($10: U64)
        goto .178
        */



        generalRegFiles(9.U) := generalRegFiles(10.U)

        CP := 178.U
      }

      is(195.U) {
        /*
        $11 = ($9: U64)
        undecl r: U64 @$9
        goto .196
        */



        generalRegFiles(11.U) := generalRegFiles(9.U)

        CP := 196.U
      }

      is(196.U) {
        /*
        **(SP + (2: SP)) = ($11: U64) [unsigned, U64, 8]  // $res = ($11: U64)
        goto $ret@0
        */


        val __tmp_2907 = Cat(
          arrayRegFiles((SP + 2.U(16.W)) + 1.U),
          arrayRegFiles((SP + 2.U(16.W)) + 0.U)
        )
        val __tmp_2908 = (generalRegFiles(11.U)).asUInt
        arrayRegFiles(__tmp_2907 + 0.U) := __tmp_2908(7, 0)
        arrayRegFiles(__tmp_2907 + 1.U) := __tmp_2908(15, 8)
        arrayRegFiles(__tmp_2907 + 2.U) := __tmp_2908(23, 16)
        arrayRegFiles(__tmp_2907 + 3.U) := __tmp_2908(31, 24)
        arrayRegFiles(__tmp_2907 + 4.U) := __tmp_2908(39, 32)
        arrayRegFiles(__tmp_2907 + 5.U) := __tmp_2908(47, 40)
        arrayRegFiles(__tmp_2907 + 6.U) := __tmp_2908(55, 48)
        arrayRegFiles(__tmp_2907 + 7.U) := __tmp_2908(63, 56)

        CP := Cat(
          arrayRegFiles(SP + 0.U + 1.U),
          arrayRegFiles(SP + 0.U + 0.U)
        )

      }

    }

}


