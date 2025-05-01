package GeneralRegFileToBRAM
import chisel3._
import chisel3.util._
import chisel3.experimental._



class MultTest (val C_S_AXI_DATA_WIDTH:  Int = 32,
               val C_S_AXI_ADDR_WIDTH:  Int = 32,
               val ARRAY_REG_WIDTH:     Int = 8,
               val ARRAY_REG_DEPTH:     Int = 176,
               val GENERAL_REG_WIDTH:   Int = 64,
               val GENERAL_REG_DEPTH:   Int = 96,
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
        SP = 84
        DP = 0
        *(10: U32) = (886747591: U32) [unsigned, U32, 4]  // $display.$type (MS[anvil.PrinterIndex.U, U8]: 0x34DAB1C7)
        *(14: SP) = (64: Z) [signed, Z, 8]  // $display.size
        *(84: CP) = (0: CP) [unsigned, CP, 2]  // $ret
        goto .4
        */


        SP := 84.U(16.W)

        DP := 0.U(64.W)

        val __tmp_2909 = 10.U(32.W)
        val __tmp_2910 = (886747591.U(32.W)).asUInt
        arrayRegFiles(__tmp_2909 + 0.U) := __tmp_2910(7, 0)
        arrayRegFiles(__tmp_2909 + 1.U) := __tmp_2910(15, 8)
        arrayRegFiles(__tmp_2909 + 2.U) := __tmp_2910(23, 16)
        arrayRegFiles(__tmp_2909 + 3.U) := __tmp_2910(31, 24)

        val __tmp_2911 = 14.U(16.W)
        val __tmp_2912 = (64.S(64.W)).asUInt
        arrayRegFiles(__tmp_2911 + 0.U) := __tmp_2912(7, 0)
        arrayRegFiles(__tmp_2911 + 1.U) := __tmp_2912(15, 8)
        arrayRegFiles(__tmp_2911 + 2.U) := __tmp_2912(23, 16)
        arrayRegFiles(__tmp_2911 + 3.U) := __tmp_2912(31, 24)
        arrayRegFiles(__tmp_2911 + 4.U) := __tmp_2912(39, 32)
        arrayRegFiles(__tmp_2911 + 5.U) := __tmp_2912(47, 40)
        arrayRegFiles(__tmp_2911 + 6.U) := __tmp_2912(55, 48)
        arrayRegFiles(__tmp_2911 + 7.U) := __tmp_2912(63, 56)

        val __tmp_2913 = 84.U(16.W)
        val __tmp_2914 = (0.U(16.W)).asUInt
        arrayRegFiles(__tmp_2913 + 0.U) := __tmp_2914(7, 0)
        arrayRegFiles(__tmp_2913 + 1.U) := __tmp_2914(15, 8)

        CP := 4.U
      }

      is(4.U) {
        /*
        $0 = *(0: SP) [signed, Z, 8]  // $0 = $testNum
        goto .5
        */


        val __tmp_2915 = (0.U(16.W)).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_2915 + 7.U),
          arrayRegFiles(__tmp_2915 + 6.U),
          arrayRegFiles(__tmp_2915 + 5.U),
          arrayRegFiles(__tmp_2915 + 4.U),
          arrayRegFiles(__tmp_2915 + 3.U),
          arrayRegFiles(__tmp_2915 + 2.U),
          arrayRegFiles(__tmp_2915 + 1.U),
          arrayRegFiles(__tmp_2915 + 0.U)
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
        *SP = (9: CP) [unsigned, CP, 2]  // $ret@0 = 1349
        *(SP - (8: SP)) = ($0: Z) [signed, Z, 8]  // save $0 (Z)
        goto .8
        */


        val __tmp_2916 = SP
        val __tmp_2917 = (9.U(16.W)).asUInt
        arrayRegFiles(__tmp_2916 + 0.U) := __tmp_2917(7, 0)
        arrayRegFiles(__tmp_2916 + 1.U) := __tmp_2917(15, 8)

        val __tmp_2918 = (SP - 8.U(16.W))
        val __tmp_2919 = (generalRegFiles(0.U).asSInt).asUInt
        arrayRegFiles(__tmp_2918 + 0.U) := __tmp_2919(7, 0)
        arrayRegFiles(__tmp_2918 + 1.U) := __tmp_2919(15, 8)
        arrayRegFiles(__tmp_2918 + 2.U) := __tmp_2919(23, 16)
        arrayRegFiles(__tmp_2918 + 3.U) := __tmp_2919(31, 24)
        arrayRegFiles(__tmp_2918 + 4.U) := __tmp_2919(39, 32)
        arrayRegFiles(__tmp_2918 + 5.U) := __tmp_2919(47, 40)
        arrayRegFiles(__tmp_2918 + 6.U) := __tmp_2919(55, 48)
        arrayRegFiles(__tmp_2918 + 7.U) := __tmp_2919(63, 56)

        CP := 8.U
      }

      is(8.U) {
        /*
        decl $ret: CP [@0, 2]
        goto .24
        */


        CP := 24.U
      }

      is(9.U) {
        /*
        $0 = *(SP - (8: SP)) [signed, Z, 8]  // restore $0 (Z)
        undecl $ret: CP [@0, 2]
        goto .10
        */


        val __tmp_2920 = ((SP - 8.U(16.W))).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_2920 + 7.U),
          arrayRegFiles(__tmp_2920 + 6.U),
          arrayRegFiles(__tmp_2920 + 5.U),
          arrayRegFiles(__tmp_2920 + 4.U),
          arrayRegFiles(__tmp_2920 + 3.U),
          arrayRegFiles(__tmp_2920 + 2.U),
          arrayRegFiles(__tmp_2920 + 1.U),
          arrayRegFiles(__tmp_2920 + 0.U)
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
        SP = SP + 10
        goto .13
        */


        SP := SP + 10.U

        CP := 13.U
      }

      is(13.U) {
        /*
        *SP = (15: CP) [unsigned, CP, 2]  // $ret@0 = 1351
        *(SP - (8: SP)) = ($0: Z) [signed, Z, 8]  // save $0 (Z)
        goto .14
        */


        val __tmp_2921 = SP
        val __tmp_2922 = (15.U(16.W)).asUInt
        arrayRegFiles(__tmp_2921 + 0.U) := __tmp_2922(7, 0)
        arrayRegFiles(__tmp_2921 + 1.U) := __tmp_2922(15, 8)

        val __tmp_2923 = (SP - 8.U(16.W))
        val __tmp_2924 = (generalRegFiles(0.U).asSInt).asUInt
        arrayRegFiles(__tmp_2923 + 0.U) := __tmp_2924(7, 0)
        arrayRegFiles(__tmp_2923 + 1.U) := __tmp_2924(15, 8)
        arrayRegFiles(__tmp_2923 + 2.U) := __tmp_2924(23, 16)
        arrayRegFiles(__tmp_2923 + 3.U) := __tmp_2924(31, 24)
        arrayRegFiles(__tmp_2923 + 4.U) := __tmp_2924(39, 32)
        arrayRegFiles(__tmp_2923 + 5.U) := __tmp_2924(47, 40)
        arrayRegFiles(__tmp_2923 + 6.U) := __tmp_2924(55, 48)
        arrayRegFiles(__tmp_2923 + 7.U) := __tmp_2924(63, 56)

        CP := 14.U
      }

      is(14.U) {
        /*
        decl $ret: CP [@0, 2]
        goto .39
        */


        CP := 39.U
      }

      is(15.U) {
        /*
        $0 = *(SP - (8: SP)) [signed, Z, 8]  // restore $0 (Z)
        undecl $ret: CP [@0, 2]
        goto .16
        */


        val __tmp_2925 = ((SP - 8.U(16.W))).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_2925 + 7.U),
          arrayRegFiles(__tmp_2925 + 6.U),
          arrayRegFiles(__tmp_2925 + 5.U),
          arrayRegFiles(__tmp_2925 + 4.U),
          arrayRegFiles(__tmp_2925 + 3.U),
          arrayRegFiles(__tmp_2925 + 2.U),
          arrayRegFiles(__tmp_2925 + 1.U),
          arrayRegFiles(__tmp_2925 + 0.U)
        ).asSInt.pad(GENERAL_REG_WIDTH).asUInt

        CP := 16.U
      }

      is(16.U) {
        /*
        SP = SP - 10
        goto .17
        */


        SP := SP - 10.U

        CP := 17.U
      }

      is(17.U) {
        /*
        if ((($0: Z) < (0: Z)) | (($0: Z) ≡ (2: Z))) goto .18 else goto .23
        */


        CP := Mux((((generalRegFiles(0.U).asSInt < 0.S(64.W)).asUInt | (generalRegFiles(0.U).asSInt === 2.S(64.W)).asUInt).asUInt) === 1.U, 18.U, 23.U)
      }

      is(18.U) {
        /*
        SP = SP + 2
        goto .19
        */


        SP := SP + 2.U

        CP := 19.U
      }

      is(19.U) {
        /*
        *SP = (21: CP) [unsigned, CP, 2]  // $ret@0 = 1353
        goto .20
        */


        val __tmp_2926 = SP
        val __tmp_2927 = (21.U(16.W)).asUInt
        arrayRegFiles(__tmp_2926 + 0.U) := __tmp_2927(7, 0)
        arrayRegFiles(__tmp_2926 + 1.U) := __tmp_2927(15, 8)

        CP := 20.U
      }

      is(20.U) {
        /*
        decl $ret: CP [@0, 2]
        goto .54
        */


        CP := 54.U
      }

      is(21.U) {
        /*
        undecl $ret: CP [@0, 2]
        goto .22
        */


        CP := 22.U
      }

      is(22.U) {
        /*
        SP = SP - 2
        goto .23
        */


        SP := SP - 2.U

        CP := 23.U
      }

      is(23.U) {
        /*
        goto $ret@0
        */


        CP := Cat(
          arrayRegFiles(SP + 0.U + 1.U),
          arrayRegFiles(SP + 0.U + 0.U)
        )

      }

      is(24.U) {
        /*
        alloc mult$res@[16,11].7C9A6ABB: U64 [@2, 8]
        goto .25
        */


        CP := 25.U
      }

      is(25.U) {
        /*
        SP = SP + 18
        goto .26
        */


        SP := SP + 18.U

        CP := 26.U
      }

      is(26.U) {
        /*
        *SP = (28: CP) [unsigned, CP, 2]  // $ret@0 = 1355
        *(SP + (2: SP)) = (SP - (16: SP)) [unsigned, SP, 2]  // $res@2 = -16
        $15 = (2: U64)
        $16 = (2: U64)
        goto .27
        */


        val __tmp_2928 = SP
        val __tmp_2929 = (28.U(16.W)).asUInt
        arrayRegFiles(__tmp_2928 + 0.U) := __tmp_2929(7, 0)
        arrayRegFiles(__tmp_2928 + 1.U) := __tmp_2929(15, 8)

        val __tmp_2930 = (SP + 2.U(16.W))
        val __tmp_2931 = ((SP - 16.U(16.W))).asUInt
        arrayRegFiles(__tmp_2930 + 0.U) := __tmp_2931(7, 0)
        arrayRegFiles(__tmp_2930 + 1.U) := __tmp_2931(15, 8)


        generalRegFiles(15.U) := 2.U(64.W)


        generalRegFiles(16.U) := 2.U(64.W)

        CP := 27.U
      }

      is(27.U) {
        /*
        decl $ret: CP [@0, 2], $res: SP [@2, 10], x: U64 @$0, y: U64 @$1
        $0 = ($15: U64)
        $1 = ($16: U64)
        goto .69
        */



        generalRegFiles(0.U) := generalRegFiles(15.U)


        generalRegFiles(1.U) := generalRegFiles(16.U)

        CP := 69.U
      }

      is(28.U) {
        /*
        $2 = **(SP + (2: SP)) [unsigned, U64, 8]  // $2 = $res
        undecl y: U64 @$1, x: U64 @$0, $res: SP [@2, 10], $ret: CP [@0, 2]
        goto .29
        */


        val __tmp_2932 = (Cat(
          arrayRegFiles((SP + 2.U(16.W)) + 1.U),
          arrayRegFiles((SP + 2.U(16.W)) + 0.U)
        )).asUInt
        generalRegFiles(2.U) := Cat(
          arrayRegFiles(__tmp_2932 + 7.U),
          arrayRegFiles(__tmp_2932 + 6.U),
          arrayRegFiles(__tmp_2932 + 5.U),
          arrayRegFiles(__tmp_2932 + 4.U),
          arrayRegFiles(__tmp_2932 + 3.U),
          arrayRegFiles(__tmp_2932 + 2.U),
          arrayRegFiles(__tmp_2932 + 1.U),
          arrayRegFiles(__tmp_2932 + 0.U)
        ).asUInt

        CP := 29.U
      }

      is(29.U) {
        /*
        SP = SP - 18
        goto .30
        */


        SP := SP - 18.U

        CP := 30.U
      }

      is(30.U) {
        /*
        alloc printU64Hex$res@[16,11].7C9A6ABB: U64 [@10, 8]
        goto .31
        */


        CP := 31.U
      }

      is(31.U) {
        /*
        SP = SP + 18
        goto .32
        */


        SP := SP + 18.U

        CP := 32.U
      }

      is(32.U) {
        /*
        *SP = (34: CP) [unsigned, CP, 2]  // $ret@0 = 1357
        *(SP + (2: SP)) = (SP - (8: SP)) [unsigned, SP, 2]  // $res@2 = -8
        $91 = (8: SP)
        $92 = DP
        $93 = (63: anvil.PrinterIndex.U)
        $94 = ($2: U64)
        $95 = (16: Z)
        goto .33
        */


        val __tmp_2933 = SP
        val __tmp_2934 = (34.U(16.W)).asUInt
        arrayRegFiles(__tmp_2933 + 0.U) := __tmp_2934(7, 0)
        arrayRegFiles(__tmp_2933 + 1.U) := __tmp_2934(15, 8)

        val __tmp_2935 = (SP + 2.U(16.W))
        val __tmp_2936 = ((SP - 8.U(16.W))).asUInt
        arrayRegFiles(__tmp_2935 + 0.U) := __tmp_2936(7, 0)
        arrayRegFiles(__tmp_2935 + 1.U) := __tmp_2936(15, 8)


        generalRegFiles(91.U) := 8.U(16.W)


        generalRegFiles(92.U) := DP


        generalRegFiles(93.U) := 63.U(64.W)


        generalRegFiles(94.U) := generalRegFiles(2.U)


        generalRegFiles(95.U) := (16.S(64.W)).asUInt

        CP := 33.U
      }

      is(33.U) {
        /*
        decl $ret: CP [@0, 2], $res: SP [@2, 2], buffer: MS[anvil.PrinterIndex.U, U8] @$0, index: anvil.PrinterIndex.U @$1, mask: anvil.PrinterIndex.U @$2, n: U64 @$3, digits: Z @$4
        $0 = ($91: MS[anvil.PrinterIndex.U, U8])
        $1 = ($92: anvil.PrinterIndex.U)
        $2 = ($93: anvil.PrinterIndex.U)
        $3 = ($94: U64)
        $4 = ($95: Z)
        goto .82
        */



        generalRegFiles(0.U) := generalRegFiles(91.U)


        generalRegFiles(1.U) := generalRegFiles(92.U)


        generalRegFiles(2.U) := generalRegFiles(93.U)


        generalRegFiles(3.U) := generalRegFiles(94.U)


        generalRegFiles(4.U) := (generalRegFiles(95.U).asSInt).asUInt

        CP := 82.U
      }

      is(34.U) {
        /*
        $3 = **(SP + (2: SP)) [unsigned, U64, 8]  // $3 = $res
        undecl digits: Z @$4, n: U64 @$3, mask: anvil.PrinterIndex.U @$2, index: anvil.PrinterIndex.U @$1, buffer: MS[anvil.PrinterIndex.U, U8] @$0, $res: SP [@2, 2], $ret: CP [@0, 2]
        goto .35
        */


        val __tmp_2937 = (Cat(
          arrayRegFiles((SP + 2.U(16.W)) + 1.U),
          arrayRegFiles((SP + 2.U(16.W)) + 0.U)
        )).asUInt
        generalRegFiles(3.U) := Cat(
          arrayRegFiles(__tmp_2937 + 7.U),
          arrayRegFiles(__tmp_2937 + 6.U),
          arrayRegFiles(__tmp_2937 + 5.U),
          arrayRegFiles(__tmp_2937 + 4.U),
          arrayRegFiles(__tmp_2937 + 3.U),
          arrayRegFiles(__tmp_2937 + 2.U),
          arrayRegFiles(__tmp_2937 + 1.U),
          arrayRegFiles(__tmp_2937 + 0.U)
        ).asUInt

        CP := 35.U
      }

      is(35.U) {
        /*
        SP = SP - 18
        goto .36
        */


        SP := SP - 18.U

        CP := 36.U
      }

      is(36.U) {
        /*
        DP = DP + (($3: U64) as DP)
        goto .37
        */


        DP := DP + generalRegFiles(3.U).asUInt

        CP := 37.U
      }

      is(37.U) {
        /*
        *(((8: SP) + (12: SP)) + ((DP & (63: DP)) as SP)) = (10: U8) [unsigned, U8, 1]  // $display((DP & (63: DP))) = (10: U8)
        goto .38
        */


        val __tmp_2938 = ((8.U(16.W) + 12.U(16.W)) + (DP & 63.U(64.W)).asUInt)
        val __tmp_2939 = (10.U(8.W)).asUInt
        arrayRegFiles(__tmp_2938 + 0.U) := __tmp_2939(7, 0)

        CP := 38.U
      }

      is(38.U) {
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

      is(39.U) {
        /*
        alloc mult$res@[20,11].9F4BF90C: U64 [@2, 8]
        goto .40
        */


        CP := 40.U
      }

      is(40.U) {
        /*
        SP = SP + 18
        goto .41
        */


        SP := SP + 18.U

        CP := 41.U
      }

      is(41.U) {
        /*
        *SP = (43: CP) [unsigned, CP, 2]  // $ret@0 = 1359
        *(SP + (2: SP)) = (SP - (16: SP)) [unsigned, SP, 2]  // $res@2 = -16
        $15 = (3: U64)
        $16 = (5: U64)
        goto .42
        */


        val __tmp_2940 = SP
        val __tmp_2941 = (43.U(16.W)).asUInt
        arrayRegFiles(__tmp_2940 + 0.U) := __tmp_2941(7, 0)
        arrayRegFiles(__tmp_2940 + 1.U) := __tmp_2941(15, 8)

        val __tmp_2942 = (SP + 2.U(16.W))
        val __tmp_2943 = ((SP - 16.U(16.W))).asUInt
        arrayRegFiles(__tmp_2942 + 0.U) := __tmp_2943(7, 0)
        arrayRegFiles(__tmp_2942 + 1.U) := __tmp_2943(15, 8)


        generalRegFiles(15.U) := 3.U(64.W)


        generalRegFiles(16.U) := 5.U(64.W)

        CP := 42.U
      }

      is(42.U) {
        /*
        decl $ret: CP [@0, 2], $res: SP [@2, 10], x: U64 @$0, y: U64 @$1
        $0 = ($15: U64)
        $1 = ($16: U64)
        goto .69
        */



        generalRegFiles(0.U) := generalRegFiles(15.U)


        generalRegFiles(1.U) := generalRegFiles(16.U)

        CP := 69.U
      }

      is(43.U) {
        /*
        $2 = **(SP + (2: SP)) [unsigned, U64, 8]  // $2 = $res
        undecl y: U64 @$1, x: U64 @$0, $res: SP [@2, 10], $ret: CP [@0, 2]
        goto .44
        */


        val __tmp_2944 = (Cat(
          arrayRegFiles((SP + 2.U(16.W)) + 1.U),
          arrayRegFiles((SP + 2.U(16.W)) + 0.U)
        )).asUInt
        generalRegFiles(2.U) := Cat(
          arrayRegFiles(__tmp_2944 + 7.U),
          arrayRegFiles(__tmp_2944 + 6.U),
          arrayRegFiles(__tmp_2944 + 5.U),
          arrayRegFiles(__tmp_2944 + 4.U),
          arrayRegFiles(__tmp_2944 + 3.U),
          arrayRegFiles(__tmp_2944 + 2.U),
          arrayRegFiles(__tmp_2944 + 1.U),
          arrayRegFiles(__tmp_2944 + 0.U)
        ).asUInt

        CP := 44.U
      }

      is(44.U) {
        /*
        SP = SP - 18
        goto .45
        */


        SP := SP - 18.U

        CP := 45.U
      }

      is(45.U) {
        /*
        alloc printU64Hex$res@[20,11].9F4BF90C: U64 [@10, 8]
        goto .46
        */


        CP := 46.U
      }

      is(46.U) {
        /*
        SP = SP + 18
        goto .47
        */


        SP := SP + 18.U

        CP := 47.U
      }

      is(47.U) {
        /*
        *SP = (49: CP) [unsigned, CP, 2]  // $ret@0 = 1361
        *(SP + (2: SP)) = (SP - (8: SP)) [unsigned, SP, 2]  // $res@2 = -8
        $91 = (8: SP)
        $92 = DP
        $93 = (63: anvil.PrinterIndex.U)
        $94 = ($2: U64)
        $95 = (16: Z)
        goto .48
        */


        val __tmp_2945 = SP
        val __tmp_2946 = (49.U(16.W)).asUInt
        arrayRegFiles(__tmp_2945 + 0.U) := __tmp_2946(7, 0)
        arrayRegFiles(__tmp_2945 + 1.U) := __tmp_2946(15, 8)

        val __tmp_2947 = (SP + 2.U(16.W))
        val __tmp_2948 = ((SP - 8.U(16.W))).asUInt
        arrayRegFiles(__tmp_2947 + 0.U) := __tmp_2948(7, 0)
        arrayRegFiles(__tmp_2947 + 1.U) := __tmp_2948(15, 8)


        generalRegFiles(91.U) := 8.U(16.W)


        generalRegFiles(92.U) := DP


        generalRegFiles(93.U) := 63.U(64.W)


        generalRegFiles(94.U) := generalRegFiles(2.U)


        generalRegFiles(95.U) := (16.S(64.W)).asUInt

        CP := 48.U
      }

      is(48.U) {
        /*
        decl $ret: CP [@0, 2], $res: SP [@2, 2], buffer: MS[anvil.PrinterIndex.U, U8] @$0, index: anvil.PrinterIndex.U @$1, mask: anvil.PrinterIndex.U @$2, n: U64 @$3, digits: Z @$4
        $0 = ($91: MS[anvil.PrinterIndex.U, U8])
        $1 = ($92: anvil.PrinterIndex.U)
        $2 = ($93: anvil.PrinterIndex.U)
        $3 = ($94: U64)
        $4 = ($95: Z)
        goto .82
        */



        generalRegFiles(0.U) := generalRegFiles(91.U)


        generalRegFiles(1.U) := generalRegFiles(92.U)


        generalRegFiles(2.U) := generalRegFiles(93.U)


        generalRegFiles(3.U) := generalRegFiles(94.U)


        generalRegFiles(4.U) := (generalRegFiles(95.U).asSInt).asUInt

        CP := 82.U
      }

      is(49.U) {
        /*
        $3 = **(SP + (2: SP)) [unsigned, U64, 8]  // $3 = $res
        undecl digits: Z @$4, n: U64 @$3, mask: anvil.PrinterIndex.U @$2, index: anvil.PrinterIndex.U @$1, buffer: MS[anvil.PrinterIndex.U, U8] @$0, $res: SP [@2, 2], $ret: CP [@0, 2]
        goto .50
        */


        val __tmp_2949 = (Cat(
          arrayRegFiles((SP + 2.U(16.W)) + 1.U),
          arrayRegFiles((SP + 2.U(16.W)) + 0.U)
        )).asUInt
        generalRegFiles(3.U) := Cat(
          arrayRegFiles(__tmp_2949 + 7.U),
          arrayRegFiles(__tmp_2949 + 6.U),
          arrayRegFiles(__tmp_2949 + 5.U),
          arrayRegFiles(__tmp_2949 + 4.U),
          arrayRegFiles(__tmp_2949 + 3.U),
          arrayRegFiles(__tmp_2949 + 2.U),
          arrayRegFiles(__tmp_2949 + 1.U),
          arrayRegFiles(__tmp_2949 + 0.U)
        ).asUInt

        CP := 50.U
      }

      is(50.U) {
        /*
        SP = SP - 18
        goto .51
        */


        SP := SP - 18.U

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
        *(((8: SP) + (12: SP)) + ((DP & (63: DP)) as SP)) = (10: U8) [unsigned, U8, 1]  // $display((DP & (63: DP))) = (10: U8)
        goto .53
        */


        val __tmp_2950 = ((8.U(16.W) + 12.U(16.W)) + (DP & 63.U(64.W)).asUInt)
        val __tmp_2951 = (10.U(8.W)).asUInt
        arrayRegFiles(__tmp_2950 + 0.U) := __tmp_2951(7, 0)

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
        alloc mult$res@[24,11].92DD733F: U64 [@2, 8]
        goto .55
        */


        CP := 55.U
      }

      is(55.U) {
        /*
        SP = SP + 18
        goto .56
        */


        SP := SP + 18.U

        CP := 56.U
      }

      is(56.U) {
        /*
        *SP = (58: CP) [unsigned, CP, 2]  // $ret@0 = 1363
        *(SP + (2: SP)) = (SP - (16: SP)) [unsigned, SP, 2]  // $res@2 = -16
        $15 = (0: U64)
        $16 = (12349: U64)
        goto .57
        */


        val __tmp_2952 = SP
        val __tmp_2953 = (58.U(16.W)).asUInt
        arrayRegFiles(__tmp_2952 + 0.U) := __tmp_2953(7, 0)
        arrayRegFiles(__tmp_2952 + 1.U) := __tmp_2953(15, 8)

        val __tmp_2954 = (SP + 2.U(16.W))
        val __tmp_2955 = ((SP - 16.U(16.W))).asUInt
        arrayRegFiles(__tmp_2954 + 0.U) := __tmp_2955(7, 0)
        arrayRegFiles(__tmp_2954 + 1.U) := __tmp_2955(15, 8)


        generalRegFiles(15.U) := 0.U(64.W)


        generalRegFiles(16.U) := 12349.U(64.W)

        CP := 57.U
      }

      is(57.U) {
        /*
        decl $ret: CP [@0, 2], $res: SP [@2, 10], x: U64 @$0, y: U64 @$1
        $0 = ($15: U64)
        $1 = ($16: U64)
        goto .69
        */



        generalRegFiles(0.U) := generalRegFiles(15.U)


        generalRegFiles(1.U) := generalRegFiles(16.U)

        CP := 69.U
      }

      is(58.U) {
        /*
        $2 = **(SP + (2: SP)) [unsigned, U64, 8]  // $2 = $res
        undecl y: U64 @$1, x: U64 @$0, $res: SP [@2, 10], $ret: CP [@0, 2]
        goto .59
        */


        val __tmp_2956 = (Cat(
          arrayRegFiles((SP + 2.U(16.W)) + 1.U),
          arrayRegFiles((SP + 2.U(16.W)) + 0.U)
        )).asUInt
        generalRegFiles(2.U) := Cat(
          arrayRegFiles(__tmp_2956 + 7.U),
          arrayRegFiles(__tmp_2956 + 6.U),
          arrayRegFiles(__tmp_2956 + 5.U),
          arrayRegFiles(__tmp_2956 + 4.U),
          arrayRegFiles(__tmp_2956 + 3.U),
          arrayRegFiles(__tmp_2956 + 2.U),
          arrayRegFiles(__tmp_2956 + 1.U),
          arrayRegFiles(__tmp_2956 + 0.U)
        ).asUInt

        CP := 59.U
      }

      is(59.U) {
        /*
        SP = SP - 18
        goto .60
        */


        SP := SP - 18.U

        CP := 60.U
      }

      is(60.U) {
        /*
        alloc printU64Hex$res@[24,11].92DD733F: U64 [@10, 8]
        goto .61
        */


        CP := 61.U
      }

      is(61.U) {
        /*
        SP = SP + 18
        goto .62
        */


        SP := SP + 18.U

        CP := 62.U
      }

      is(62.U) {
        /*
        *SP = (64: CP) [unsigned, CP, 2]  // $ret@0 = 1365
        *(SP + (2: SP)) = (SP - (8: SP)) [unsigned, SP, 2]  // $res@2 = -8
        $91 = (8: SP)
        $92 = DP
        $93 = (63: anvil.PrinterIndex.U)
        $94 = ($2: U64)
        $95 = (16: Z)
        goto .63
        */


        val __tmp_2957 = SP
        val __tmp_2958 = (64.U(16.W)).asUInt
        arrayRegFiles(__tmp_2957 + 0.U) := __tmp_2958(7, 0)
        arrayRegFiles(__tmp_2957 + 1.U) := __tmp_2958(15, 8)

        val __tmp_2959 = (SP + 2.U(16.W))
        val __tmp_2960 = ((SP - 8.U(16.W))).asUInt
        arrayRegFiles(__tmp_2959 + 0.U) := __tmp_2960(7, 0)
        arrayRegFiles(__tmp_2959 + 1.U) := __tmp_2960(15, 8)


        generalRegFiles(91.U) := 8.U(16.W)


        generalRegFiles(92.U) := DP


        generalRegFiles(93.U) := 63.U(64.W)


        generalRegFiles(94.U) := generalRegFiles(2.U)


        generalRegFiles(95.U) := (16.S(64.W)).asUInt

        CP := 63.U
      }

      is(63.U) {
        /*
        decl $ret: CP [@0, 2], $res: SP [@2, 2], buffer: MS[anvil.PrinterIndex.U, U8] @$0, index: anvil.PrinterIndex.U @$1, mask: anvil.PrinterIndex.U @$2, n: U64 @$3, digits: Z @$4
        $0 = ($91: MS[anvil.PrinterIndex.U, U8])
        $1 = ($92: anvil.PrinterIndex.U)
        $2 = ($93: anvil.PrinterIndex.U)
        $3 = ($94: U64)
        $4 = ($95: Z)
        goto .82
        */



        generalRegFiles(0.U) := generalRegFiles(91.U)


        generalRegFiles(1.U) := generalRegFiles(92.U)


        generalRegFiles(2.U) := generalRegFiles(93.U)


        generalRegFiles(3.U) := generalRegFiles(94.U)


        generalRegFiles(4.U) := (generalRegFiles(95.U).asSInt).asUInt

        CP := 82.U
      }

      is(64.U) {
        /*
        $3 = **(SP + (2: SP)) [unsigned, U64, 8]  // $3 = $res
        undecl digits: Z @$4, n: U64 @$3, mask: anvil.PrinterIndex.U @$2, index: anvil.PrinterIndex.U @$1, buffer: MS[anvil.PrinterIndex.U, U8] @$0, $res: SP [@2, 2], $ret: CP [@0, 2]
        goto .65
        */


        val __tmp_2961 = (Cat(
          arrayRegFiles((SP + 2.U(16.W)) + 1.U),
          arrayRegFiles((SP + 2.U(16.W)) + 0.U)
        )).asUInt
        generalRegFiles(3.U) := Cat(
          arrayRegFiles(__tmp_2961 + 7.U),
          arrayRegFiles(__tmp_2961 + 6.U),
          arrayRegFiles(__tmp_2961 + 5.U),
          arrayRegFiles(__tmp_2961 + 4.U),
          arrayRegFiles(__tmp_2961 + 3.U),
          arrayRegFiles(__tmp_2961 + 2.U),
          arrayRegFiles(__tmp_2961 + 1.U),
          arrayRegFiles(__tmp_2961 + 0.U)
        ).asUInt

        CP := 65.U
      }

      is(65.U) {
        /*
        SP = SP - 18
        goto .66
        */


        SP := SP - 18.U

        CP := 66.U
      }

      is(66.U) {
        /*
        DP = DP + (($3: U64) as DP)
        goto .67
        */


        DP := DP + generalRegFiles(3.U).asUInt

        CP := 67.U
      }

      is(67.U) {
        /*
        *(((8: SP) + (12: SP)) + ((DP & (63: DP)) as SP)) = (10: U8) [unsigned, U8, 1]  // $display((DP & (63: DP))) = (10: U8)
        goto .68
        */


        val __tmp_2962 = ((8.U(16.W) + 12.U(16.W)) + (DP & 63.U(64.W)).asUInt)
        val __tmp_2963 = (10.U(8.W)).asUInt
        arrayRegFiles(__tmp_2962 + 0.U) := __tmp_2963(7, 0)

        CP := 68.U
      }

      is(68.U) {
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

      is(69.U) {
        /*
        decl r: U64 @$2
        $2 = (0: U64)
        goto .70
        */



        generalRegFiles(2.U) := 0.U(64.W)

        CP := 70.U
      }

      is(70.U) {
        /*
        decl i: U64 @$3
        $3 = (0: U64)
        goto .71
        */



        generalRegFiles(3.U) := 0.U(64.W)

        CP := 71.U
      }

      is(71.U) {
        /*
        $4 = ($3: U64)
        $5 = ($0: U64)
        goto .72
        */



        generalRegFiles(4.U) := generalRegFiles(3.U)


        generalRegFiles(5.U) := generalRegFiles(0.U)

        CP := 72.U
      }

      is(72.U) {
        /*
        $6 = (($4: U64) < ($5: U64))
        goto .73
        */



        generalRegFiles(6.U) := (generalRegFiles(4.U) < generalRegFiles(5.U)).asUInt

        CP := 73.U
      }

      is(73.U) {
        /*
        if ($6: B) goto .74 else goto .80
        */


        CP := Mux((generalRegFiles(6.U).asUInt) === 1.U, 74.U, 80.U)
      }

      is(74.U) {
        /*
        $4 = ($2: U64)
        $5 = ($1: U64)
        goto .75
        */



        generalRegFiles(4.U) := generalRegFiles(2.U)


        generalRegFiles(5.U) := generalRegFiles(1.U)

        CP := 75.U
      }

      is(75.U) {
        /*
        $6 = (($4: U64) + ($5: U64))
        goto .76
        */



        generalRegFiles(6.U) := (generalRegFiles(4.U) + generalRegFiles(5.U))

        CP := 76.U
      }

      is(76.U) {
        /*
        $2 = ($6: U64)
        goto .77
        */



        generalRegFiles(2.U) := generalRegFiles(6.U)

        CP := 77.U
      }

      is(77.U) {
        /*
        $4 = ($3: U64)
        goto .78
        */



        generalRegFiles(4.U) := generalRegFiles(3.U)

        CP := 78.U
      }

      is(78.U) {
        /*
        $5 = (($4: U64) + (1: U64))
        goto .79
        */



        generalRegFiles(5.U) := (generalRegFiles(4.U) + 1.U(64.W))

        CP := 79.U
      }

      is(79.U) {
        /*
        $3 = ($5: U64)
        goto .71
        */



        generalRegFiles(3.U) := generalRegFiles(5.U)

        CP := 71.U
      }

      is(80.U) {
        /*
        $4 = ($2: U64)
        undecl r: U64 @$2
        goto .81
        */



        generalRegFiles(4.U) := generalRegFiles(2.U)

        CP := 81.U
      }

      is(81.U) {
        /*
        **(SP + (2: SP)) = ($4: U64) [unsigned, U64, 8]  // $res = ($4: U64)
        goto $ret@0
        */


        val __tmp_2964 = Cat(
          arrayRegFiles((SP + 2.U(16.W)) + 1.U),
          arrayRegFiles((SP + 2.U(16.W)) + 0.U)
        )
        val __tmp_2965 = (generalRegFiles(4.U)).asUInt
        arrayRegFiles(__tmp_2964 + 0.U) := __tmp_2965(7, 0)
        arrayRegFiles(__tmp_2964 + 1.U) := __tmp_2965(15, 8)
        arrayRegFiles(__tmp_2964 + 2.U) := __tmp_2965(23, 16)
        arrayRegFiles(__tmp_2964 + 3.U) := __tmp_2965(31, 24)
        arrayRegFiles(__tmp_2964 + 4.U) := __tmp_2965(39, 32)
        arrayRegFiles(__tmp_2964 + 5.U) := __tmp_2965(47, 40)
        arrayRegFiles(__tmp_2964 + 6.U) := __tmp_2965(55, 48)
        arrayRegFiles(__tmp_2964 + 7.U) := __tmp_2965(63, 56)

        CP := Cat(
          arrayRegFiles(SP + 0.U + 1.U),
          arrayRegFiles(SP + 0.U + 0.U)
        )

      }

      is(82.U) {
        /*
        decl buff: MS[anvil.PrinterIndex.I16, U8] [@4, 30]
        alloc $new@[245,16].6203A7B3: MS[anvil.PrinterIndex.I16, U8] [@34, 30]
        $10 = (SP + (34: SP))
        *(SP + (34: SP)) = (1541243932: U32) [unsigned, U32, 4]  // sha3 type signature of MS[anvil.PrinterIndex.I16, U8]: 0x5BDD841C
        *(SP + (38: SP)) = (16: Z) [signed, Z, 8]  // size of MS[anvil.PrinterIndex.I16, U8]((0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8))
        goto .83
        */



        generalRegFiles(10.U) := (SP + 34.U(16.W))

        val __tmp_2966 = (SP + 34.U(16.W))
        val __tmp_2967 = (1541243932.U(32.W)).asUInt
        arrayRegFiles(__tmp_2966 + 0.U) := __tmp_2967(7, 0)
        arrayRegFiles(__tmp_2966 + 1.U) := __tmp_2967(15, 8)
        arrayRegFiles(__tmp_2966 + 2.U) := __tmp_2967(23, 16)
        arrayRegFiles(__tmp_2966 + 3.U) := __tmp_2967(31, 24)

        val __tmp_2968 = (SP + 38.U(16.W))
        val __tmp_2969 = (16.S(64.W)).asUInt
        arrayRegFiles(__tmp_2968 + 0.U) := __tmp_2969(7, 0)
        arrayRegFiles(__tmp_2968 + 1.U) := __tmp_2969(15, 8)
        arrayRegFiles(__tmp_2968 + 2.U) := __tmp_2969(23, 16)
        arrayRegFiles(__tmp_2968 + 3.U) := __tmp_2969(31, 24)
        arrayRegFiles(__tmp_2968 + 4.U) := __tmp_2969(39, 32)
        arrayRegFiles(__tmp_2968 + 5.U) := __tmp_2969(47, 40)
        arrayRegFiles(__tmp_2968 + 6.U) := __tmp_2969(55, 48)
        arrayRegFiles(__tmp_2968 + 7.U) := __tmp_2969(63, 56)

        CP := 83.U
      }

      is(83.U) {
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
        goto .84
        */


        val __tmp_2970 = ((generalRegFiles(10.U) + 12.U(16.W)) + 0.S(8.W).asUInt)
        val __tmp_2971 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_2970 + 0.U) := __tmp_2971(7, 0)

        val __tmp_2972 = ((generalRegFiles(10.U) + 12.U(16.W)) + 1.S(8.W).asUInt)
        val __tmp_2973 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_2972 + 0.U) := __tmp_2973(7, 0)

        val __tmp_2974 = ((generalRegFiles(10.U) + 12.U(16.W)) + 2.S(8.W).asUInt)
        val __tmp_2975 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_2974 + 0.U) := __tmp_2975(7, 0)

        val __tmp_2976 = ((generalRegFiles(10.U) + 12.U(16.W)) + 3.S(8.W).asUInt)
        val __tmp_2977 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_2976 + 0.U) := __tmp_2977(7, 0)

        val __tmp_2978 = ((generalRegFiles(10.U) + 12.U(16.W)) + 4.S(8.W).asUInt)
        val __tmp_2979 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_2978 + 0.U) := __tmp_2979(7, 0)

        val __tmp_2980 = ((generalRegFiles(10.U) + 12.U(16.W)) + 5.S(8.W).asUInt)
        val __tmp_2981 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_2980 + 0.U) := __tmp_2981(7, 0)

        val __tmp_2982 = ((generalRegFiles(10.U) + 12.U(16.W)) + 6.S(8.W).asUInt)
        val __tmp_2983 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_2982 + 0.U) := __tmp_2983(7, 0)

        val __tmp_2984 = ((generalRegFiles(10.U) + 12.U(16.W)) + 7.S(8.W).asUInt)
        val __tmp_2985 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_2984 + 0.U) := __tmp_2985(7, 0)

        val __tmp_2986 = ((generalRegFiles(10.U) + 12.U(16.W)) + 8.S(8.W).asUInt)
        val __tmp_2987 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_2986 + 0.U) := __tmp_2987(7, 0)

        val __tmp_2988 = ((generalRegFiles(10.U) + 12.U(16.W)) + 9.S(8.W).asUInt)
        val __tmp_2989 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_2988 + 0.U) := __tmp_2989(7, 0)

        val __tmp_2990 = ((generalRegFiles(10.U) + 12.U(16.W)) + 10.S(8.W).asUInt)
        val __tmp_2991 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_2990 + 0.U) := __tmp_2991(7, 0)

        val __tmp_2992 = ((generalRegFiles(10.U) + 12.U(16.W)) + 11.S(8.W).asUInt)
        val __tmp_2993 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_2992 + 0.U) := __tmp_2993(7, 0)

        val __tmp_2994 = ((generalRegFiles(10.U) + 12.U(16.W)) + 12.S(8.W).asUInt)
        val __tmp_2995 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_2994 + 0.U) := __tmp_2995(7, 0)

        val __tmp_2996 = ((generalRegFiles(10.U) + 12.U(16.W)) + 13.S(8.W).asUInt)
        val __tmp_2997 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_2996 + 0.U) := __tmp_2997(7, 0)

        val __tmp_2998 = ((generalRegFiles(10.U) + 12.U(16.W)) + 14.S(8.W).asUInt)
        val __tmp_2999 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_2998 + 0.U) := __tmp_2999(7, 0)

        val __tmp_3000 = ((generalRegFiles(10.U) + 12.U(16.W)) + 15.S(8.W).asUInt)
        val __tmp_3001 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_3000 + 0.U) := __tmp_3001(7, 0)

        CP := 84.U
      }

      is(84.U) {
        /*
        (SP + (4: SP)) [MS[anvil.PrinterIndex.I16, U8], 30]  <-  ($10: MS[anvil.PrinterIndex.I16, U8]) [MS[anvil.PrinterIndex.I16, U8], ((*(($10: MS[anvil.PrinterIndex.I16, U8]) + (4: SP)) as SP) + (12: SP))]  // buff = ($10: MS[anvil.PrinterIndex.I16, U8])
        goto .85
        */


        val __tmp_3002 = (SP + 4.U(16.W))
        val __tmp_3003 = generalRegFiles(10.U)
        val __tmp_3004 = (Cat(
           arrayRegFiles((generalRegFiles(10.U) + 4.U(16.W)) + 7.U),
           arrayRegFiles((generalRegFiles(10.U) + 4.U(16.W)) + 6.U),
           arrayRegFiles((generalRegFiles(10.U) + 4.U(16.W)) + 5.U),
           arrayRegFiles((generalRegFiles(10.U) + 4.U(16.W)) + 4.U),
           arrayRegFiles((generalRegFiles(10.U) + 4.U(16.W)) + 3.U),
           arrayRegFiles((generalRegFiles(10.U) + 4.U(16.W)) + 2.U),
           arrayRegFiles((generalRegFiles(10.U) + 4.U(16.W)) + 1.U),
           arrayRegFiles((generalRegFiles(10.U) + 4.U(16.W)) + 0.U)
         ).asSInt.asUInt + 12.U(16.W))

        when(Idx < __tmp_3004) {
          arrayRegFiles(__tmp_3002 + Idx + 0.U) := arrayRegFiles(__tmp_3003 + Idx + 0.U)
          arrayRegFiles(__tmp_3002 + Idx + 1.U) := arrayRegFiles(__tmp_3003 + Idx + 1.U)
          arrayRegFiles(__tmp_3002 + Idx + 2.U) := arrayRegFiles(__tmp_3003 + Idx + 2.U)
          arrayRegFiles(__tmp_3002 + Idx + 3.U) := arrayRegFiles(__tmp_3003 + Idx + 3.U)
          arrayRegFiles(__tmp_3002 + Idx + 4.U) := arrayRegFiles(__tmp_3003 + Idx + 4.U)
          arrayRegFiles(__tmp_3002 + Idx + 5.U) := arrayRegFiles(__tmp_3003 + Idx + 5.U)
          arrayRegFiles(__tmp_3002 + Idx + 6.U) := arrayRegFiles(__tmp_3003 + Idx + 6.U)
          arrayRegFiles(__tmp_3002 + Idx + 7.U) := arrayRegFiles(__tmp_3003 + Idx + 7.U)
          Idx := Idx + 8.U
          LeftByteRounds := __tmp_3004 - Idx
        } .elsewhen(IdxLeftByteRounds < LeftByteRounds) {
          val __tmp_3005 = Idx - 8.U
          arrayRegFiles(__tmp_3002 + __tmp_3005 + IdxLeftByteRounds) := arrayRegFiles(__tmp_3003 + __tmp_3005 + IdxLeftByteRounds)
          IdxLeftByteRounds := IdxLeftByteRounds + 1.U
        } .otherwise {
          Idx := 0.U
          IdxLeftByteRounds := 0.U
          LeftByteRounds := 0.U
          CP := 85.U
        }


      }

      is(85.U) {
        /*
        unalloc $new@[245,16].6203A7B3: MS[anvil.PrinterIndex.I16, U8] [@34, 30]
        goto .86
        */


        CP := 86.U
      }

      is(86.U) {
        /*
        decl i: anvil.PrinterIndex.I16 @$5
        $5 = (0: anvil.PrinterIndex.I16)
        goto .87
        */



        generalRegFiles(5.U) := (0.S(8.W)).asUInt

        CP := 87.U
      }

      is(87.U) {
        /*
        decl m: U64 @$6
        $6 = ($3: U64)
        goto .88
        */



        generalRegFiles(6.U) := generalRegFiles(3.U)

        CP := 88.U
      }

      is(88.U) {
        /*
        decl d: Z @$7
        $7 = ($4: Z)
        goto .89
        */



        generalRegFiles(7.U) := (generalRegFiles(4.U).asSInt).asUInt

        CP := 89.U
      }

      is(89.U) {
        /*
        $10 = ($6: U64)
        goto .90
        */



        generalRegFiles(10.U) := generalRegFiles(6.U)

        CP := 90.U
      }

      is(90.U) {
        /*
        $11 = (($10: U64) > (0: U64))
        goto .91
        */



        generalRegFiles(11.U) := (generalRegFiles(10.U) > 0.U(64.W)).asUInt

        CP := 91.U
      }

      is(91.U) {
        /*
        $12 = ($7: Z)
        goto .92
        */



        generalRegFiles(12.U) := (generalRegFiles(7.U).asSInt).asUInt

        CP := 92.U
      }

      is(92.U) {
        /*
        $13 = (($12: Z) > (0: Z))
        goto .93
        */



        generalRegFiles(13.U) := (generalRegFiles(12.U).asSInt > 0.S(64.W)).asUInt

        CP := 93.U
      }

      is(93.U) {
        /*
        $14 = (($11: B) & ($13: B))
        goto .94
        */



        generalRegFiles(14.U) := (generalRegFiles(11.U) & generalRegFiles(13.U))

        CP := 94.U
      }

      is(94.U) {
        /*
        if ($14: B) goto .95 else goto .139
        */


        CP := Mux((generalRegFiles(14.U).asUInt) === 1.U, 95.U, 139.U)
      }

      is(95.U) {
        /*
        $10 = ($6: U64)
        goto .96
        */



        generalRegFiles(10.U) := generalRegFiles(6.U)

        CP := 96.U
      }

      is(96.U) {
        /*
        $11 = (($10: U64) & (15: U64))
        goto .97
        */



        generalRegFiles(11.U) := (generalRegFiles(10.U) & 15.U(64.W))

        CP := 97.U
      }

      is(97.U) {
        /*
        switch (($11: U64))
          (0: U64): goto 98
          (1: U64): goto 100
          (2: U64): goto 102
          (3: U64): goto 104
          (4: U64): goto 106
          (5: U64): goto 108
          (6: U64): goto 110
          (7: U64): goto 112
          (8: U64): goto 114
          (9: U64): goto 116
          (10: U64): goto 118
          (11: U64): goto 120
          (12: U64): goto 122
          (13: U64): goto 124
          (14: U64): goto 126
          (15: U64): goto 128

        */


        val __tmp_3006 = generalRegFiles(11.U)

        switch(__tmp_3006) {

          is(0.U(64.W)) {
            CP := 98.U
          }


          is(1.U(64.W)) {
            CP := 100.U
          }


          is(2.U(64.W)) {
            CP := 102.U
          }


          is(3.U(64.W)) {
            CP := 104.U
          }


          is(4.U(64.W)) {
            CP := 106.U
          }


          is(5.U(64.W)) {
            CP := 108.U
          }


          is(6.U(64.W)) {
            CP := 110.U
          }


          is(7.U(64.W)) {
            CP := 112.U
          }


          is(8.U(64.W)) {
            CP := 114.U
          }


          is(9.U(64.W)) {
            CP := 116.U
          }


          is(10.U(64.W)) {
            CP := 118.U
          }


          is(11.U(64.W)) {
            CP := 120.U
          }


          is(12.U(64.W)) {
            CP := 122.U
          }


          is(13.U(64.W)) {
            CP := 124.U
          }


          is(14.U(64.W)) {
            CP := 126.U
          }


          is(15.U(64.W)) {
            CP := 128.U
          }

        }

      }

      is(98.U) {
        /*
        $10 = (SP + (4: SP))
        $11 = ($5: anvil.PrinterIndex.I16)
        goto .99
        */



        generalRegFiles(10.U) := (SP + 4.U(16.W))


        generalRegFiles(11.U) := (generalRegFiles(5.U).asSInt).asUInt

        CP := 99.U
      }

      is(99.U) {
        /*
        *((($10: MS[anvil.PrinterIndex.I16, U8]) + (12: SP)) + (($11: anvil.PrinterIndex.I16) as SP)) = (48: U8) [unsigned, U8, 1]  // ($10: MS[anvil.PrinterIndex.I16, U8])(($11: anvil.PrinterIndex.I16)) = (48: U8)
        goto .130
        */


        val __tmp_3007 = ((generalRegFiles(10.U) + 12.U(16.W)) + generalRegFiles(11.U).asSInt.asUInt)
        val __tmp_3008 = (48.U(8.W)).asUInt
        arrayRegFiles(__tmp_3007 + 0.U) := __tmp_3008(7, 0)

        CP := 130.U
      }

      is(100.U) {
        /*
        $10 = (SP + (4: SP))
        $11 = ($5: anvil.PrinterIndex.I16)
        goto .101
        */



        generalRegFiles(10.U) := (SP + 4.U(16.W))


        generalRegFiles(11.U) := (generalRegFiles(5.U).asSInt).asUInt

        CP := 101.U
      }

      is(101.U) {
        /*
        *((($10: MS[anvil.PrinterIndex.I16, U8]) + (12: SP)) + (($11: anvil.PrinterIndex.I16) as SP)) = (49: U8) [unsigned, U8, 1]  // ($10: MS[anvil.PrinterIndex.I16, U8])(($11: anvil.PrinterIndex.I16)) = (49: U8)
        goto .130
        */


        val __tmp_3009 = ((generalRegFiles(10.U) + 12.U(16.W)) + generalRegFiles(11.U).asSInt.asUInt)
        val __tmp_3010 = (49.U(8.W)).asUInt
        arrayRegFiles(__tmp_3009 + 0.U) := __tmp_3010(7, 0)

        CP := 130.U
      }

      is(102.U) {
        /*
        $10 = (SP + (4: SP))
        $11 = ($5: anvil.PrinterIndex.I16)
        goto .103
        */



        generalRegFiles(10.U) := (SP + 4.U(16.W))


        generalRegFiles(11.U) := (generalRegFiles(5.U).asSInt).asUInt

        CP := 103.U
      }

      is(103.U) {
        /*
        *((($10: MS[anvil.PrinterIndex.I16, U8]) + (12: SP)) + (($11: anvil.PrinterIndex.I16) as SP)) = (50: U8) [unsigned, U8, 1]  // ($10: MS[anvil.PrinterIndex.I16, U8])(($11: anvil.PrinterIndex.I16)) = (50: U8)
        goto .130
        */


        val __tmp_3011 = ((generalRegFiles(10.U) + 12.U(16.W)) + generalRegFiles(11.U).asSInt.asUInt)
        val __tmp_3012 = (50.U(8.W)).asUInt
        arrayRegFiles(__tmp_3011 + 0.U) := __tmp_3012(7, 0)

        CP := 130.U
      }

      is(104.U) {
        /*
        $10 = (SP + (4: SP))
        $11 = ($5: anvil.PrinterIndex.I16)
        goto .105
        */



        generalRegFiles(10.U) := (SP + 4.U(16.W))


        generalRegFiles(11.U) := (generalRegFiles(5.U).asSInt).asUInt

        CP := 105.U
      }

      is(105.U) {
        /*
        *((($10: MS[anvil.PrinterIndex.I16, U8]) + (12: SP)) + (($11: anvil.PrinterIndex.I16) as SP)) = (51: U8) [unsigned, U8, 1]  // ($10: MS[anvil.PrinterIndex.I16, U8])(($11: anvil.PrinterIndex.I16)) = (51: U8)
        goto .130
        */


        val __tmp_3013 = ((generalRegFiles(10.U) + 12.U(16.W)) + generalRegFiles(11.U).asSInt.asUInt)
        val __tmp_3014 = (51.U(8.W)).asUInt
        arrayRegFiles(__tmp_3013 + 0.U) := __tmp_3014(7, 0)

        CP := 130.U
      }

      is(106.U) {
        /*
        $10 = (SP + (4: SP))
        $11 = ($5: anvil.PrinterIndex.I16)
        goto .107
        */



        generalRegFiles(10.U) := (SP + 4.U(16.W))


        generalRegFiles(11.U) := (generalRegFiles(5.U).asSInt).asUInt

        CP := 107.U
      }

      is(107.U) {
        /*
        *((($10: MS[anvil.PrinterIndex.I16, U8]) + (12: SP)) + (($11: anvil.PrinterIndex.I16) as SP)) = (52: U8) [unsigned, U8, 1]  // ($10: MS[anvil.PrinterIndex.I16, U8])(($11: anvil.PrinterIndex.I16)) = (52: U8)
        goto .130
        */


        val __tmp_3015 = ((generalRegFiles(10.U) + 12.U(16.W)) + generalRegFiles(11.U).asSInt.asUInt)
        val __tmp_3016 = (52.U(8.W)).asUInt
        arrayRegFiles(__tmp_3015 + 0.U) := __tmp_3016(7, 0)

        CP := 130.U
      }

      is(108.U) {
        /*
        $10 = (SP + (4: SP))
        $11 = ($5: anvil.PrinterIndex.I16)
        goto .109
        */



        generalRegFiles(10.U) := (SP + 4.U(16.W))


        generalRegFiles(11.U) := (generalRegFiles(5.U).asSInt).asUInt

        CP := 109.U
      }

      is(109.U) {
        /*
        *((($10: MS[anvil.PrinterIndex.I16, U8]) + (12: SP)) + (($11: anvil.PrinterIndex.I16) as SP)) = (53: U8) [unsigned, U8, 1]  // ($10: MS[anvil.PrinterIndex.I16, U8])(($11: anvil.PrinterIndex.I16)) = (53: U8)
        goto .130
        */


        val __tmp_3017 = ((generalRegFiles(10.U) + 12.U(16.W)) + generalRegFiles(11.U).asSInt.asUInt)
        val __tmp_3018 = (53.U(8.W)).asUInt
        arrayRegFiles(__tmp_3017 + 0.U) := __tmp_3018(7, 0)

        CP := 130.U
      }

      is(110.U) {
        /*
        $10 = (SP + (4: SP))
        $11 = ($5: anvil.PrinterIndex.I16)
        goto .111
        */



        generalRegFiles(10.U) := (SP + 4.U(16.W))


        generalRegFiles(11.U) := (generalRegFiles(5.U).asSInt).asUInt

        CP := 111.U
      }

      is(111.U) {
        /*
        *((($10: MS[anvil.PrinterIndex.I16, U8]) + (12: SP)) + (($11: anvil.PrinterIndex.I16) as SP)) = (54: U8) [unsigned, U8, 1]  // ($10: MS[anvil.PrinterIndex.I16, U8])(($11: anvil.PrinterIndex.I16)) = (54: U8)
        goto .130
        */


        val __tmp_3019 = ((generalRegFiles(10.U) + 12.U(16.W)) + generalRegFiles(11.U).asSInt.asUInt)
        val __tmp_3020 = (54.U(8.W)).asUInt
        arrayRegFiles(__tmp_3019 + 0.U) := __tmp_3020(7, 0)

        CP := 130.U
      }

      is(112.U) {
        /*
        $10 = (SP + (4: SP))
        $11 = ($5: anvil.PrinterIndex.I16)
        goto .113
        */



        generalRegFiles(10.U) := (SP + 4.U(16.W))


        generalRegFiles(11.U) := (generalRegFiles(5.U).asSInt).asUInt

        CP := 113.U
      }

      is(113.U) {
        /*
        *((($10: MS[anvil.PrinterIndex.I16, U8]) + (12: SP)) + (($11: anvil.PrinterIndex.I16) as SP)) = (55: U8) [unsigned, U8, 1]  // ($10: MS[anvil.PrinterIndex.I16, U8])(($11: anvil.PrinterIndex.I16)) = (55: U8)
        goto .130
        */


        val __tmp_3021 = ((generalRegFiles(10.U) + 12.U(16.W)) + generalRegFiles(11.U).asSInt.asUInt)
        val __tmp_3022 = (55.U(8.W)).asUInt
        arrayRegFiles(__tmp_3021 + 0.U) := __tmp_3022(7, 0)

        CP := 130.U
      }

      is(114.U) {
        /*
        $10 = (SP + (4: SP))
        $11 = ($5: anvil.PrinterIndex.I16)
        goto .115
        */



        generalRegFiles(10.U) := (SP + 4.U(16.W))


        generalRegFiles(11.U) := (generalRegFiles(5.U).asSInt).asUInt

        CP := 115.U
      }

      is(115.U) {
        /*
        *((($10: MS[anvil.PrinterIndex.I16, U8]) + (12: SP)) + (($11: anvil.PrinterIndex.I16) as SP)) = (56: U8) [unsigned, U8, 1]  // ($10: MS[anvil.PrinterIndex.I16, U8])(($11: anvil.PrinterIndex.I16)) = (56: U8)
        goto .130
        */


        val __tmp_3023 = ((generalRegFiles(10.U) + 12.U(16.W)) + generalRegFiles(11.U).asSInt.asUInt)
        val __tmp_3024 = (56.U(8.W)).asUInt
        arrayRegFiles(__tmp_3023 + 0.U) := __tmp_3024(7, 0)

        CP := 130.U
      }

      is(116.U) {
        /*
        $10 = (SP + (4: SP))
        $11 = ($5: anvil.PrinterIndex.I16)
        goto .117
        */



        generalRegFiles(10.U) := (SP + 4.U(16.W))


        generalRegFiles(11.U) := (generalRegFiles(5.U).asSInt).asUInt

        CP := 117.U
      }

      is(117.U) {
        /*
        *((($10: MS[anvil.PrinterIndex.I16, U8]) + (12: SP)) + (($11: anvil.PrinterIndex.I16) as SP)) = (57: U8) [unsigned, U8, 1]  // ($10: MS[anvil.PrinterIndex.I16, U8])(($11: anvil.PrinterIndex.I16)) = (57: U8)
        goto .130
        */


        val __tmp_3025 = ((generalRegFiles(10.U) + 12.U(16.W)) + generalRegFiles(11.U).asSInt.asUInt)
        val __tmp_3026 = (57.U(8.W)).asUInt
        arrayRegFiles(__tmp_3025 + 0.U) := __tmp_3026(7, 0)

        CP := 130.U
      }

      is(118.U) {
        /*
        $10 = (SP + (4: SP))
        $11 = ($5: anvil.PrinterIndex.I16)
        goto .119
        */



        generalRegFiles(10.U) := (SP + 4.U(16.W))


        generalRegFiles(11.U) := (generalRegFiles(5.U).asSInt).asUInt

        CP := 119.U
      }

      is(119.U) {
        /*
        *((($10: MS[anvil.PrinterIndex.I16, U8]) + (12: SP)) + (($11: anvil.PrinterIndex.I16) as SP)) = (65: U8) [unsigned, U8, 1]  // ($10: MS[anvil.PrinterIndex.I16, U8])(($11: anvil.PrinterIndex.I16)) = (65: U8)
        goto .130
        */


        val __tmp_3027 = ((generalRegFiles(10.U) + 12.U(16.W)) + generalRegFiles(11.U).asSInt.asUInt)
        val __tmp_3028 = (65.U(8.W)).asUInt
        arrayRegFiles(__tmp_3027 + 0.U) := __tmp_3028(7, 0)

        CP := 130.U
      }

      is(120.U) {
        /*
        $10 = (SP + (4: SP))
        $11 = ($5: anvil.PrinterIndex.I16)
        goto .121
        */



        generalRegFiles(10.U) := (SP + 4.U(16.W))


        generalRegFiles(11.U) := (generalRegFiles(5.U).asSInt).asUInt

        CP := 121.U
      }

      is(121.U) {
        /*
        *((($10: MS[anvil.PrinterIndex.I16, U8]) + (12: SP)) + (($11: anvil.PrinterIndex.I16) as SP)) = (66: U8) [unsigned, U8, 1]  // ($10: MS[anvil.PrinterIndex.I16, U8])(($11: anvil.PrinterIndex.I16)) = (66: U8)
        goto .130
        */


        val __tmp_3029 = ((generalRegFiles(10.U) + 12.U(16.W)) + generalRegFiles(11.U).asSInt.asUInt)
        val __tmp_3030 = (66.U(8.W)).asUInt
        arrayRegFiles(__tmp_3029 + 0.U) := __tmp_3030(7, 0)

        CP := 130.U
      }

      is(122.U) {
        /*
        $10 = (SP + (4: SP))
        $11 = ($5: anvil.PrinterIndex.I16)
        goto .123
        */



        generalRegFiles(10.U) := (SP + 4.U(16.W))


        generalRegFiles(11.U) := (generalRegFiles(5.U).asSInt).asUInt

        CP := 123.U
      }

      is(123.U) {
        /*
        *((($10: MS[anvil.PrinterIndex.I16, U8]) + (12: SP)) + (($11: anvil.PrinterIndex.I16) as SP)) = (67: U8) [unsigned, U8, 1]  // ($10: MS[anvil.PrinterIndex.I16, U8])(($11: anvil.PrinterIndex.I16)) = (67: U8)
        goto .130
        */


        val __tmp_3031 = ((generalRegFiles(10.U) + 12.U(16.W)) + generalRegFiles(11.U).asSInt.asUInt)
        val __tmp_3032 = (67.U(8.W)).asUInt
        arrayRegFiles(__tmp_3031 + 0.U) := __tmp_3032(7, 0)

        CP := 130.U
      }

      is(124.U) {
        /*
        $10 = (SP + (4: SP))
        $11 = ($5: anvil.PrinterIndex.I16)
        goto .125
        */



        generalRegFiles(10.U) := (SP + 4.U(16.W))


        generalRegFiles(11.U) := (generalRegFiles(5.U).asSInt).asUInt

        CP := 125.U
      }

      is(125.U) {
        /*
        *((($10: MS[anvil.PrinterIndex.I16, U8]) + (12: SP)) + (($11: anvil.PrinterIndex.I16) as SP)) = (68: U8) [unsigned, U8, 1]  // ($10: MS[anvil.PrinterIndex.I16, U8])(($11: anvil.PrinterIndex.I16)) = (68: U8)
        goto .130
        */


        val __tmp_3033 = ((generalRegFiles(10.U) + 12.U(16.W)) + generalRegFiles(11.U).asSInt.asUInt)
        val __tmp_3034 = (68.U(8.W)).asUInt
        arrayRegFiles(__tmp_3033 + 0.U) := __tmp_3034(7, 0)

        CP := 130.U
      }

      is(126.U) {
        /*
        $10 = (SP + (4: SP))
        $11 = ($5: anvil.PrinterIndex.I16)
        goto .127
        */



        generalRegFiles(10.U) := (SP + 4.U(16.W))


        generalRegFiles(11.U) := (generalRegFiles(5.U).asSInt).asUInt

        CP := 127.U
      }

      is(127.U) {
        /*
        *((($10: MS[anvil.PrinterIndex.I16, U8]) + (12: SP)) + (($11: anvil.PrinterIndex.I16) as SP)) = (69: U8) [unsigned, U8, 1]  // ($10: MS[anvil.PrinterIndex.I16, U8])(($11: anvil.PrinterIndex.I16)) = (69: U8)
        goto .130
        */


        val __tmp_3035 = ((generalRegFiles(10.U) + 12.U(16.W)) + generalRegFiles(11.U).asSInt.asUInt)
        val __tmp_3036 = (69.U(8.W)).asUInt
        arrayRegFiles(__tmp_3035 + 0.U) := __tmp_3036(7, 0)

        CP := 130.U
      }

      is(128.U) {
        /*
        $10 = (SP + (4: SP))
        $11 = ($5: anvil.PrinterIndex.I16)
        goto .129
        */



        generalRegFiles(10.U) := (SP + 4.U(16.W))


        generalRegFiles(11.U) := (generalRegFiles(5.U).asSInt).asUInt

        CP := 129.U
      }

      is(129.U) {
        /*
        *((($10: MS[anvil.PrinterIndex.I16, U8]) + (12: SP)) + (($11: anvil.PrinterIndex.I16) as SP)) = (70: U8) [unsigned, U8, 1]  // ($10: MS[anvil.PrinterIndex.I16, U8])(($11: anvil.PrinterIndex.I16)) = (70: U8)
        goto .130
        */


        val __tmp_3037 = ((generalRegFiles(10.U) + 12.U(16.W)) + generalRegFiles(11.U).asSInt.asUInt)
        val __tmp_3038 = (70.U(8.W)).asUInt
        arrayRegFiles(__tmp_3037 + 0.U) := __tmp_3038(7, 0)

        CP := 130.U
      }

      is(130.U) {
        /*
        $10 = ($6: U64)
        goto .131
        */



        generalRegFiles(10.U) := generalRegFiles(6.U)

        CP := 131.U
      }

      is(131.U) {
        /*
        $11 = (($10: U64) >>> (4: U64))
        goto .132
        */



        generalRegFiles(11.U) := (((generalRegFiles(10.U)) >> 4.U(64.W)(4,0)))

        CP := 132.U
      }

      is(132.U) {
        /*
        $6 = ($11: U64)
        goto .133
        */



        generalRegFiles(6.U) := generalRegFiles(11.U)

        CP := 133.U
      }

      is(133.U) {
        /*
        $10 = ($5: anvil.PrinterIndex.I16)
        goto .134
        */



        generalRegFiles(10.U) := (generalRegFiles(5.U).asSInt).asUInt

        CP := 134.U
      }

      is(134.U) {
        /*
        $11 = (($10: anvil.PrinterIndex.I16) + (1: anvil.PrinterIndex.I16))
        goto .135
        */



        generalRegFiles(11.U) := ((generalRegFiles(10.U).asSInt + 1.S(8.W))).asUInt

        CP := 135.U
      }

      is(135.U) {
        /*
        $5 = ($11: anvil.PrinterIndex.I16)
        goto .136
        */



        generalRegFiles(5.U) := (generalRegFiles(11.U).asSInt).asUInt

        CP := 136.U
      }

      is(136.U) {
        /*
        $10 = ($7: Z)
        goto .137
        */



        generalRegFiles(10.U) := (generalRegFiles(7.U).asSInt).asUInt

        CP := 137.U
      }

      is(137.U) {
        /*
        $11 = (($10: Z) - (1: Z))
        goto .138
        */



        generalRegFiles(11.U) := ((generalRegFiles(10.U).asSInt - 1.S(64.W))).asUInt

        CP := 138.U
      }

      is(138.U) {
        /*
        $7 = ($11: Z)
        goto .89
        */



        generalRegFiles(7.U) := (generalRegFiles(11.U).asSInt).asUInt

        CP := 89.U
      }

      is(139.U) {
        /*
        decl idx: anvil.PrinterIndex.U @$8
        $10 = ($1: anvil.PrinterIndex.U)
        goto .140
        */



        generalRegFiles(10.U) := generalRegFiles(1.U)

        CP := 140.U
      }

      is(140.U) {
        /*
        $8 = ($10: anvil.PrinterIndex.U)
        goto .141
        */



        generalRegFiles(8.U) := generalRegFiles(10.U)

        CP := 141.U
      }

      is(141.U) {
        /*
        $10 = ($7: Z)
        goto .142
        */



        generalRegFiles(10.U) := (generalRegFiles(7.U).asSInt).asUInt

        CP := 142.U
      }

      is(142.U) {
        /*
        $11 = (($10: Z) > (0: Z))
        goto .143
        */



        generalRegFiles(11.U) := (generalRegFiles(10.U).asSInt > 0.S(64.W)).asUInt

        CP := 143.U
      }

      is(143.U) {
        /*
        if ($11: B) goto .144 else goto .153
        */


        CP := Mux((generalRegFiles(11.U).asUInt) === 1.U, 144.U, 153.U)
      }

      is(144.U) {
        /*
        $10 = ($0: MS[anvil.PrinterIndex.U, U8])
        $11 = ($8: anvil.PrinterIndex.U)
        $12 = ($2: anvil.PrinterIndex.U)
        goto .145
        */



        generalRegFiles(10.U) := generalRegFiles(0.U)


        generalRegFiles(11.U) := generalRegFiles(8.U)


        generalRegFiles(12.U) := generalRegFiles(2.U)

        CP := 145.U
      }

      is(145.U) {
        /*
        $13 = (($11: anvil.PrinterIndex.U) & ($12: anvil.PrinterIndex.U))
        goto .146
        */



        generalRegFiles(13.U) := (generalRegFiles(11.U) & generalRegFiles(12.U))

        CP := 146.U
      }

      is(146.U) {
        /*
        *((($10: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($13: anvil.PrinterIndex.U) as SP)) = (48: U8) [unsigned, U8, 1]  // ($10: MS[anvil.PrinterIndex.U, U8])(($13: anvil.PrinterIndex.U)) = (48: U8)
        goto .147
        */


        val __tmp_3039 = ((generalRegFiles(10.U) + 12.U(16.W)) + generalRegFiles(13.U).asUInt)
        val __tmp_3040 = (48.U(8.W)).asUInt
        arrayRegFiles(__tmp_3039 + 0.U) := __tmp_3040(7, 0)

        CP := 147.U
      }

      is(147.U) {
        /*
        $10 = ($7: Z)
        goto .148
        */



        generalRegFiles(10.U) := (generalRegFiles(7.U).asSInt).asUInt

        CP := 148.U
      }

      is(148.U) {
        /*
        $11 = (($10: Z) - (1: Z))
        goto .149
        */



        generalRegFiles(11.U) := ((generalRegFiles(10.U).asSInt - 1.S(64.W))).asUInt

        CP := 149.U
      }

      is(149.U) {
        /*
        $7 = ($11: Z)
        goto .150
        */



        generalRegFiles(7.U) := (generalRegFiles(11.U).asSInt).asUInt

        CP := 150.U
      }

      is(150.U) {
        /*
        $10 = ($8: anvil.PrinterIndex.U)
        goto .151
        */



        generalRegFiles(10.U) := generalRegFiles(8.U)

        CP := 151.U
      }

      is(151.U) {
        /*
        $11 = (($10: anvil.PrinterIndex.U) + (1: anvil.PrinterIndex.U))
        goto .152
        */



        generalRegFiles(11.U) := (generalRegFiles(10.U) + 1.U(64.W))

        CP := 152.U
      }

      is(152.U) {
        /*
        $8 = ($11: anvil.PrinterIndex.U)
        goto .141
        */



        generalRegFiles(8.U) := generalRegFiles(11.U)

        CP := 141.U
      }

      is(153.U) {
        /*
        decl j: anvil.PrinterIndex.I16 @$9
        $10 = ($5: anvil.PrinterIndex.I16)
        undecl i: anvil.PrinterIndex.I16 @$5
        goto .154
        */



        generalRegFiles(10.U) := (generalRegFiles(5.U).asSInt).asUInt

        CP := 154.U
      }

      is(154.U) {
        /*
        $11 = (($10: anvil.PrinterIndex.I16) - (1: anvil.PrinterIndex.I16))
        goto .155
        */



        generalRegFiles(11.U) := ((generalRegFiles(10.U).asSInt - 1.S(8.W))).asUInt

        CP := 155.U
      }

      is(155.U) {
        /*
        $9 = ($11: anvil.PrinterIndex.I16)
        goto .156
        */



        generalRegFiles(9.U) := (generalRegFiles(11.U).asSInt).asUInt

        CP := 156.U
      }

      is(156.U) {
        /*
        $10 = ($9: anvil.PrinterIndex.I16)
        goto .157
        */



        generalRegFiles(10.U) := (generalRegFiles(9.U).asSInt).asUInt

        CP := 157.U
      }

      is(157.U) {
        /*
        $11 = (($10: anvil.PrinterIndex.I16) >= (0: anvil.PrinterIndex.I16))
        goto .158
        */



        generalRegFiles(11.U) := (generalRegFiles(10.U).asSInt >= 0.S(8.W)).asUInt

        CP := 158.U
      }

      is(158.U) {
        /*
        if ($11: B) goto .159 else goto .170
        */


        CP := Mux((generalRegFiles(11.U).asUInt) === 1.U, 159.U, 170.U)
      }

      is(159.U) {
        /*
        $10 = ($0: MS[anvil.PrinterIndex.U, U8])
        $11 = ($8: anvil.PrinterIndex.U)
        $12 = ($2: anvil.PrinterIndex.U)
        goto .160
        */



        generalRegFiles(10.U) := generalRegFiles(0.U)


        generalRegFiles(11.U) := generalRegFiles(8.U)


        generalRegFiles(12.U) := generalRegFiles(2.U)

        CP := 160.U
      }

      is(160.U) {
        /*
        $13 = (($11: anvil.PrinterIndex.U) & ($12: anvil.PrinterIndex.U))
        goto .161
        */



        generalRegFiles(13.U) := (generalRegFiles(11.U) & generalRegFiles(12.U))

        CP := 161.U
      }

      is(161.U) {
        /*
        $14 = (SP + (4: SP))
        $15 = ($9: anvil.PrinterIndex.I16)
        goto .162
        */



        generalRegFiles(14.U) := (SP + 4.U(16.W))


        generalRegFiles(15.U) := (generalRegFiles(9.U).asSInt).asUInt

        CP := 162.U
      }

      is(162.U) {
        /*
        $16 = *((($14: MS[anvil.PrinterIndex.I16, U8]) + (12: SP)) + (($15: anvil.PrinterIndex.I16) as SP)) [unsigned, U8, 1]  // $16 = ($14: MS[anvil.PrinterIndex.I16, U8])(($15: anvil.PrinterIndex.I16))
        goto .163
        */


        val __tmp_3041 = (((generalRegFiles(14.U) + 12.U(16.W)) + generalRegFiles(15.U).asSInt.asUInt)).asUInt
        generalRegFiles(16.U) := Cat(
          arrayRegFiles(__tmp_3041 + 0.U)
        ).asUInt

        CP := 163.U
      }

      is(163.U) {
        /*
        *((($10: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($13: anvil.PrinterIndex.U) as SP)) = ($16: U8) [unsigned, U8, 1]  // ($10: MS[anvil.PrinterIndex.U, U8])(($13: anvil.PrinterIndex.U)) = ($16: U8)
        goto .164
        */


        val __tmp_3042 = ((generalRegFiles(10.U) + 12.U(16.W)) + generalRegFiles(13.U).asUInt)
        val __tmp_3043 = (generalRegFiles(16.U)).asUInt
        arrayRegFiles(__tmp_3042 + 0.U) := __tmp_3043(7, 0)

        CP := 164.U
      }

      is(164.U) {
        /*
        $10 = ($9: anvil.PrinterIndex.I16)
        goto .165
        */



        generalRegFiles(10.U) := (generalRegFiles(9.U).asSInt).asUInt

        CP := 165.U
      }

      is(165.U) {
        /*
        $11 = (($10: anvil.PrinterIndex.I16) - (1: anvil.PrinterIndex.I16))
        goto .166
        */



        generalRegFiles(11.U) := ((generalRegFiles(10.U).asSInt - 1.S(8.W))).asUInt

        CP := 166.U
      }

      is(166.U) {
        /*
        $9 = ($11: anvil.PrinterIndex.I16)
        goto .167
        */



        generalRegFiles(9.U) := (generalRegFiles(11.U).asSInt).asUInt

        CP := 167.U
      }

      is(167.U) {
        /*
        $10 = ($8: anvil.PrinterIndex.U)
        goto .168
        */



        generalRegFiles(10.U) := generalRegFiles(8.U)

        CP := 168.U
      }

      is(168.U) {
        /*
        $11 = (($10: anvil.PrinterIndex.U) + (1: anvil.PrinterIndex.U))
        goto .169
        */



        generalRegFiles(11.U) := (generalRegFiles(10.U) + 1.U(64.W))

        CP := 169.U
      }

      is(169.U) {
        /*
        $8 = ($11: anvil.PrinterIndex.U)
        goto .156
        */



        generalRegFiles(8.U) := generalRegFiles(11.U)

        CP := 156.U
      }

      is(170.U) {
        /*
        $10 = ($4: Z)
        goto .171
        */



        generalRegFiles(10.U) := (generalRegFiles(4.U).asSInt).asUInt

        CP := 171.U
      }

      is(171.U) {
        /*
        $11 = (($10: Z) as U64)
        goto .172
        */



        generalRegFiles(11.U) := generalRegFiles(10.U).asSInt.asUInt

        CP := 172.U
      }

      is(172.U) {
        /*
        **(SP + (2: SP)) = ($11: U64) [unsigned, U64, 8]  // $res = ($11: U64)
        goto $ret@0
        */


        val __tmp_3044 = Cat(
          arrayRegFiles((SP + 2.U(16.W)) + 1.U),
          arrayRegFiles((SP + 2.U(16.W)) + 0.U)
        )
        val __tmp_3045 = (generalRegFiles(11.U)).asUInt
        arrayRegFiles(__tmp_3044 + 0.U) := __tmp_3045(7, 0)
        arrayRegFiles(__tmp_3044 + 1.U) := __tmp_3045(15, 8)
        arrayRegFiles(__tmp_3044 + 2.U) := __tmp_3045(23, 16)
        arrayRegFiles(__tmp_3044 + 3.U) := __tmp_3045(31, 24)
        arrayRegFiles(__tmp_3044 + 4.U) := __tmp_3045(39, 32)
        arrayRegFiles(__tmp_3044 + 5.U) := __tmp_3045(47, 40)
        arrayRegFiles(__tmp_3044 + 6.U) := __tmp_3045(55, 48)
        arrayRegFiles(__tmp_3044 + 7.U) := __tmp_3045(63, 56)

        CP := Cat(
          arrayRegFiles(SP + 0.U + 1.U),
          arrayRegFiles(SP + 0.U + 0.U)
        )

      }

    }

}


