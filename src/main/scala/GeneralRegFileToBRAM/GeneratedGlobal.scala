package GeneralRegFileToBRAM
import chisel3._
import chisel3.util._
import chisel3.experimental._



class GlobalTest (val C_S_AXI_DATA_WIDTH:  Int = 32,
               val C_S_AXI_ADDR_WIDTH:  Int = 32,
               val ARRAY_REG_WIDTH:     Int = 8,
               val ARRAY_REG_DEPTH:     Int = 120,
               val GENERAL_REG_WIDTH:   Int = 64,
               val GENERAL_REG_DEPTH:   Int = 80,
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
        SP = 31
        DP = 0
        *(9: U32) = (886747591: U32) [unsigned, U32, 4]  // $display.$type (MS[anvil.PrinterIndex.U, U8]: 0x34DAB1C7)
        *(13: SP) = (2: Z) [signed, Z, 8]  // $display.size
        *(31: CP) = (0: CP) [unsigned, CP, 2]  // $ret
        goto .4
        */


        SP := 31.U(8.W)

        DP := 0.U(64.W)

        val __tmp_2409 = 9.U(32.W)
        val __tmp_2410 = (886747591.U(32.W)).asUInt
        arrayRegFiles(__tmp_2409 + 0.U) := __tmp_2410(7, 0)
        arrayRegFiles(__tmp_2409 + 1.U) := __tmp_2410(15, 8)
        arrayRegFiles(__tmp_2409 + 2.U) := __tmp_2410(23, 16)
        arrayRegFiles(__tmp_2409 + 3.U) := __tmp_2410(31, 24)

        val __tmp_2411 = 13.U(8.W)
        val __tmp_2412 = (2.S(64.W)).asUInt
        arrayRegFiles(__tmp_2411 + 0.U) := __tmp_2412(7, 0)
        arrayRegFiles(__tmp_2411 + 1.U) := __tmp_2412(15, 8)
        arrayRegFiles(__tmp_2411 + 2.U) := __tmp_2412(23, 16)
        arrayRegFiles(__tmp_2411 + 3.U) := __tmp_2412(31, 24)
        arrayRegFiles(__tmp_2411 + 4.U) := __tmp_2412(39, 32)
        arrayRegFiles(__tmp_2411 + 5.U) := __tmp_2412(47, 40)
        arrayRegFiles(__tmp_2411 + 6.U) := __tmp_2412(55, 48)
        arrayRegFiles(__tmp_2411 + 7.U) := __tmp_2412(63, 56)

        val __tmp_2413 = 31.U(16.W)
        val __tmp_2414 = (0.U(16.W)).asUInt
        arrayRegFiles(__tmp_2413 + 0.U) := __tmp_2414(7, 0)
        arrayRegFiles(__tmp_2413 + 1.U) := __tmp_2414(15, 8)

        CP := 4.U
      }

      is(4.U) {
        /*
        $0 = *(0: SP) [signed, Z, 8]  // $0 = $testNum
        goto .5
        */


        val __tmp_2415 = (0.U(8.W)).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_2415 + 7.U),
          arrayRegFiles(__tmp_2415 + 6.U),
          arrayRegFiles(__tmp_2415 + 5.U),
          arrayRegFiles(__tmp_2415 + 4.U),
          arrayRegFiles(__tmp_2415 + 3.U),
          arrayRegFiles(__tmp_2415 + 2.U),
          arrayRegFiles(__tmp_2415 + 1.U),
          arrayRegFiles(__tmp_2415 + 0.U)
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
        *SP = (9: CP) [unsigned, CP, 2]  // $ret@0 = 1325
        goto .8
        */


        val __tmp_2416 = SP
        val __tmp_2417 = (9.U(16.W)).asUInt
        arrayRegFiles(__tmp_2416 + 0.U) := __tmp_2417(7, 0)
        arrayRegFiles(__tmp_2416 + 1.U) := __tmp_2417(15, 8)

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
        alloc global$res@[13,11].3AB476F6: Z [@2, 8]
        goto .13
        */


        CP := 13.U
      }

      is(13.U) {
        /*
        SP = SP + 18
        goto .14
        */


        SP := SP + 18.U

        CP := 14.U
      }

      is(14.U) {
        /*
        *SP = (16: CP) [unsigned, CP, 2]  // $ret@0 = 1327
        *(SP + (2: SP)) = (SP - (16: SP)) [unsigned, SP, 1]  // $res@2 = -16
        goto .15
        */


        val __tmp_2418 = SP
        val __tmp_2419 = (16.U(16.W)).asUInt
        arrayRegFiles(__tmp_2418 + 0.U) := __tmp_2419(7, 0)
        arrayRegFiles(__tmp_2418 + 1.U) := __tmp_2419(15, 8)

        val __tmp_2420 = (SP + 2.U(8.W))
        val __tmp_2421 = ((SP - 16.U(8.W))).asUInt
        arrayRegFiles(__tmp_2420 + 0.U) := __tmp_2421(7, 0)

        CP := 15.U
      }

      is(15.U) {
        /*
        decl $ret: CP [@0, 2], $res: SP [@2, 9]
        goto .27
        */


        CP := 27.U
      }

      is(16.U) {
        /*
        $2 = **(SP + (2: SP)) [signed, Z, 8]  // $2 = $res
        undecl $res: SP [@2, 9], $ret: CP [@0, 2]
        goto .17
        */


        val __tmp_2422 = (Cat(
          arrayRegFiles((SP + 2.U(8.W)) + 0.U)
        )).asUInt
        generalRegFiles(2.U) := Cat(
          arrayRegFiles(__tmp_2422 + 7.U),
          arrayRegFiles(__tmp_2422 + 6.U),
          arrayRegFiles(__tmp_2422 + 5.U),
          arrayRegFiles(__tmp_2422 + 4.U),
          arrayRegFiles(__tmp_2422 + 3.U),
          arrayRegFiles(__tmp_2422 + 2.U),
          arrayRegFiles(__tmp_2422 + 1.U),
          arrayRegFiles(__tmp_2422 + 0.U)
        ).asSInt.pad(GENERAL_REG_WIDTH).asUInt

        CP := 17.U
      }

      is(17.U) {
        /*
        SP = SP - 18
        goto .18
        */


        SP := SP - 18.U

        CP := 18.U
      }

      is(18.U) {
        /*
        alloc printS64$res@[13,11].3AB476F6: U64 [@10, 8]
        goto .19
        */


        CP := 19.U
      }

      is(19.U) {
        /*
        SP = SP + 18
        goto .20
        */


        SP := SP + 18.U

        CP := 20.U
      }

      is(20.U) {
        /*
        *SP = (22: CP) [unsigned, CP, 2]  // $ret@0 = 1329
        *(SP + (2: SP)) = (SP - (8: SP)) [unsigned, SP, 1]  // $res@2 = -8
        $76 = (8: SP)
        $77 = DP
        $78 = (1: anvil.PrinterIndex.U)
        $79 = (($2: Z) as S64)
        goto .21
        */


        val __tmp_2423 = SP
        val __tmp_2424 = (22.U(16.W)).asUInt
        arrayRegFiles(__tmp_2423 + 0.U) := __tmp_2424(7, 0)
        arrayRegFiles(__tmp_2423 + 1.U) := __tmp_2424(15, 8)

        val __tmp_2425 = (SP + 2.U(8.W))
        val __tmp_2426 = ((SP - 8.U(8.W))).asUInt
        arrayRegFiles(__tmp_2425 + 0.U) := __tmp_2426(7, 0)


        generalRegFiles(76.U) := 8.U(8.W)


        generalRegFiles(77.U) := DP


        generalRegFiles(78.U) := 1.U(64.W)


        generalRegFiles(79.U) := (generalRegFiles(2.U).asSInt.asSInt).asUInt

        CP := 21.U
      }

      is(21.U) {
        /*
        decl $ret: CP [@0, 2], $res: SP [@2, 1], buffer: MS[anvil.PrinterIndex.U, U8] @$0, index: anvil.PrinterIndex.U @$1, mask: anvil.PrinterIndex.U @$2, n: S64 @$3
        $0 = ($76: MS[anvil.PrinterIndex.U, U8])
        $1 = ($77: anvil.PrinterIndex.U)
        $2 = ($78: anvil.PrinterIndex.U)
        $3 = ($79: S64)
        goto .35
        */



        generalRegFiles(0.U) := generalRegFiles(76.U)


        generalRegFiles(1.U) := generalRegFiles(77.U)


        generalRegFiles(2.U) := generalRegFiles(78.U)


        generalRegFiles(3.U) := (generalRegFiles(79.U).asSInt).asUInt

        CP := 35.U
      }

      is(22.U) {
        /*
        $3 = **(SP + (2: SP)) [unsigned, U64, 8]  // $3 = $res
        undecl n: S64 @$3, mask: anvil.PrinterIndex.U @$2, index: anvil.PrinterIndex.U @$1, buffer: MS[anvil.PrinterIndex.U, U8] @$0, $res: SP [@2, 1], $ret: CP [@0, 2]
        goto .23
        */


        val __tmp_2427 = (Cat(
          arrayRegFiles((SP + 2.U(8.W)) + 0.U)
        )).asUInt
        generalRegFiles(3.U) := Cat(
          arrayRegFiles(__tmp_2427 + 7.U),
          arrayRegFiles(__tmp_2427 + 6.U),
          arrayRegFiles(__tmp_2427 + 5.U),
          arrayRegFiles(__tmp_2427 + 4.U),
          arrayRegFiles(__tmp_2427 + 3.U),
          arrayRegFiles(__tmp_2427 + 2.U),
          arrayRegFiles(__tmp_2427 + 1.U),
          arrayRegFiles(__tmp_2427 + 0.U)
        ).asUInt

        CP := 23.U
      }

      is(23.U) {
        /*
        SP = SP - 18
        goto .24
        */


        SP := SP - 18.U

        CP := 24.U
      }

      is(24.U) {
        /*
        DP = DP + (($3: U64) as DP)
        goto .25
        */


        DP := DP + generalRegFiles(3.U).asUInt

        CP := 25.U
      }

      is(25.U) {
        /*
        *(((8: SP) + (12: SP)) + ((DP & (1: DP)) as SP)) = (10: U8) [unsigned, U8, 1]  // $display((DP & (1: DP))) = (10: U8)
        goto .26
        */


        val __tmp_2428 = ((8.U(8.W) + 12.U(8.W)) + (DP & 1.U(64.W)).asUInt)
        val __tmp_2429 = (10.U(8.W)).asUInt
        arrayRegFiles(__tmp_2428 + 0.U) := __tmp_2429(7, 0)

        CP := 26.U
      }

      is(26.U) {
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

      is(27.U) {
        /*
        if *(22: SP) goto .33 else goto .28
        */


        CP := Mux((Cat(
                     arrayRegFiles(22.U(8.W) + 0.U)
                   ).asUInt) === 1.U, 33.U, 28.U)
      }

      is(28.U) {
        /*
        SP = SP + 11
        goto .29
        */


        SP := SP + 11.U

        CP := 29.U
      }

      is(29.U) {
        /*
        *SP = (31: CP) [unsigned, CP, 2]  // $ret@0 = 1331
        goto .30
        */


        val __tmp_2430 = SP
        val __tmp_2431 = (31.U(16.W)).asUInt
        arrayRegFiles(__tmp_2430 + 0.U) := __tmp_2431(7, 0)
        arrayRegFiles(__tmp_2430 + 1.U) := __tmp_2431(15, 8)

        CP := 30.U
      }

      is(30.U) {
        /*
        decl $ret: CP [@0, 2]
        goto .180
        */


        CP := 180.U
      }

      is(31.U) {
        /*
        undecl $ret: CP [@0, 2]
        goto .32
        */


        CP := 32.U
      }

      is(32.U) {
        /*
        SP = SP - 11
        goto .33
        */


        SP := SP - 11.U

        CP := 33.U
      }

      is(33.U) {
        /*
        $0 = *(23: SP) [signed, Z, 8]  // $0 = Foo.x
        goto .34
        */


        val __tmp_2432 = (23.U(8.W)).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_2432 + 7.U),
          arrayRegFiles(__tmp_2432 + 6.U),
          arrayRegFiles(__tmp_2432 + 5.U),
          arrayRegFiles(__tmp_2432 + 4.U),
          arrayRegFiles(__tmp_2432 + 3.U),
          arrayRegFiles(__tmp_2432 + 2.U),
          arrayRegFiles(__tmp_2432 + 1.U),
          arrayRegFiles(__tmp_2432 + 0.U)
        ).asSInt.pad(GENERAL_REG_WIDTH).asUInt

        CP := 34.U
      }

      is(34.U) {
        /*
        **(SP + (2: SP)) = ($0: Z) [signed, Z, 8]  // $res = ($0: Z)
        goto $ret@0
        */


        val __tmp_2433 = Cat(
          arrayRegFiles((SP + 2.U(8.W)) + 0.U)
        )
        val __tmp_2434 = (generalRegFiles(0.U).asSInt).asUInt
        arrayRegFiles(__tmp_2433 + 0.U) := __tmp_2434(7, 0)
        arrayRegFiles(__tmp_2433 + 1.U) := __tmp_2434(15, 8)
        arrayRegFiles(__tmp_2433 + 2.U) := __tmp_2434(23, 16)
        arrayRegFiles(__tmp_2433 + 3.U) := __tmp_2434(31, 24)
        arrayRegFiles(__tmp_2433 + 4.U) := __tmp_2434(39, 32)
        arrayRegFiles(__tmp_2433 + 5.U) := __tmp_2434(47, 40)
        arrayRegFiles(__tmp_2433 + 6.U) := __tmp_2434(55, 48)
        arrayRegFiles(__tmp_2433 + 7.U) := __tmp_2434(63, 56)

        CP := Cat(
          arrayRegFiles(SP + 0.U + 1.U),
          arrayRegFiles(SP + 0.U + 0.U)
        )

      }

      is(35.U) {
        /*
        $10 = (($3: S64) ≡ (-9223372036854775808: S64))
        goto .36
        */



        generalRegFiles(10.U) := (generalRegFiles(3.U).asSInt === BigInt("-9223372036854775808").S(64.W)).asUInt

        CP := 36.U
      }

      is(36.U) {
        /*
        if ($10: B) goto .37 else goto .97
        */


        CP := Mux((generalRegFiles(10.U).asUInt) === 1.U, 37.U, 97.U)
      }

      is(37.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $12 = (($1: anvil.PrinterIndex.U) & ($2: anvil.PrinterIndex.U))
        goto .38
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(12.U) := (generalRegFiles(1.U) & generalRegFiles(2.U))

        CP := 38.U
      }

      is(38.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($12: anvil.PrinterIndex.U) as SP)) = (45: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($12: anvil.PrinterIndex.U)) = (45: U8)
        goto .39
        */


        val __tmp_2435 = ((generalRegFiles(11.U) + 12.U(8.W)) + generalRegFiles(12.U).asUInt)
        val __tmp_2436 = (45.U(8.W)).asUInt
        arrayRegFiles(__tmp_2435 + 0.U) := __tmp_2436(7, 0)

        CP := 39.U
      }

      is(39.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $13 = (($1: anvil.PrinterIndex.U) + (1: anvil.PrinterIndex.U))
        goto .40
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(13.U) := (generalRegFiles(1.U) + 1.U(64.W))

        CP := 40.U
      }

      is(40.U) {
        /*
        $14 = (($13: anvil.PrinterIndex.U) & ($2: anvil.PrinterIndex.U))
        goto .41
        */



        generalRegFiles(14.U) := (generalRegFiles(13.U) & generalRegFiles(2.U))

        CP := 41.U
      }

      is(41.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($14: anvil.PrinterIndex.U) as SP)) = (57: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($14: anvil.PrinterIndex.U)) = (57: U8)
        goto .42
        */


        val __tmp_2437 = ((generalRegFiles(11.U) + 12.U(8.W)) + generalRegFiles(14.U).asUInt)
        val __tmp_2438 = (57.U(8.W)).asUInt
        arrayRegFiles(__tmp_2437 + 0.U) := __tmp_2438(7, 0)

        CP := 42.U
      }

      is(42.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $13 = (($1: anvil.PrinterIndex.U) + (2: anvil.PrinterIndex.U))
        goto .43
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(13.U) := (generalRegFiles(1.U) + 2.U(64.W))

        CP := 43.U
      }

      is(43.U) {
        /*
        $14 = (($13: anvil.PrinterIndex.U) & ($2: anvil.PrinterIndex.U))
        goto .44
        */



        generalRegFiles(14.U) := (generalRegFiles(13.U) & generalRegFiles(2.U))

        CP := 44.U
      }

      is(44.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($14: anvil.PrinterIndex.U) as SP)) = (50: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($14: anvil.PrinterIndex.U)) = (50: U8)
        goto .45
        */


        val __tmp_2439 = ((generalRegFiles(11.U) + 12.U(8.W)) + generalRegFiles(14.U).asUInt)
        val __tmp_2440 = (50.U(8.W)).asUInt
        arrayRegFiles(__tmp_2439 + 0.U) := __tmp_2440(7, 0)

        CP := 45.U
      }

      is(45.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $13 = (($1: anvil.PrinterIndex.U) + (3: anvil.PrinterIndex.U))
        goto .46
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(13.U) := (generalRegFiles(1.U) + 3.U(64.W))

        CP := 46.U
      }

      is(46.U) {
        /*
        $14 = (($13: anvil.PrinterIndex.U) & ($2: anvil.PrinterIndex.U))
        goto .47
        */



        generalRegFiles(14.U) := (generalRegFiles(13.U) & generalRegFiles(2.U))

        CP := 47.U
      }

      is(47.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($14: anvil.PrinterIndex.U) as SP)) = (50: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($14: anvil.PrinterIndex.U)) = (50: U8)
        goto .48
        */


        val __tmp_2441 = ((generalRegFiles(11.U) + 12.U(8.W)) + generalRegFiles(14.U).asUInt)
        val __tmp_2442 = (50.U(8.W)).asUInt
        arrayRegFiles(__tmp_2441 + 0.U) := __tmp_2442(7, 0)

        CP := 48.U
      }

      is(48.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $13 = (($1: anvil.PrinterIndex.U) + (4: anvil.PrinterIndex.U))
        goto .49
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(13.U) := (generalRegFiles(1.U) + 4.U(64.W))

        CP := 49.U
      }

      is(49.U) {
        /*
        $14 = (($13: anvil.PrinterIndex.U) & ($2: anvil.PrinterIndex.U))
        goto .50
        */



        generalRegFiles(14.U) := (generalRegFiles(13.U) & generalRegFiles(2.U))

        CP := 50.U
      }

      is(50.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($14: anvil.PrinterIndex.U) as SP)) = (51: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($14: anvil.PrinterIndex.U)) = (51: U8)
        goto .51
        */


        val __tmp_2443 = ((generalRegFiles(11.U) + 12.U(8.W)) + generalRegFiles(14.U).asUInt)
        val __tmp_2444 = (51.U(8.W)).asUInt
        arrayRegFiles(__tmp_2443 + 0.U) := __tmp_2444(7, 0)

        CP := 51.U
      }

      is(51.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $13 = (($1: anvil.PrinterIndex.U) + (5: anvil.PrinterIndex.U))
        goto .52
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(13.U) := (generalRegFiles(1.U) + 5.U(64.W))

        CP := 52.U
      }

      is(52.U) {
        /*
        $14 = (($13: anvil.PrinterIndex.U) & ($2: anvil.PrinterIndex.U))
        goto .53
        */



        generalRegFiles(14.U) := (generalRegFiles(13.U) & generalRegFiles(2.U))

        CP := 53.U
      }

      is(53.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($14: anvil.PrinterIndex.U) as SP)) = (51: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($14: anvil.PrinterIndex.U)) = (51: U8)
        goto .54
        */


        val __tmp_2445 = ((generalRegFiles(11.U) + 12.U(8.W)) + generalRegFiles(14.U).asUInt)
        val __tmp_2446 = (51.U(8.W)).asUInt
        arrayRegFiles(__tmp_2445 + 0.U) := __tmp_2446(7, 0)

        CP := 54.U
      }

      is(54.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $13 = (($1: anvil.PrinterIndex.U) + (6: anvil.PrinterIndex.U))
        goto .55
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(13.U) := (generalRegFiles(1.U) + 6.U(64.W))

        CP := 55.U
      }

      is(55.U) {
        /*
        $14 = (($13: anvil.PrinterIndex.U) & ($2: anvil.PrinterIndex.U))
        goto .56
        */



        generalRegFiles(14.U) := (generalRegFiles(13.U) & generalRegFiles(2.U))

        CP := 56.U
      }

      is(56.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($14: anvil.PrinterIndex.U) as SP)) = (55: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($14: anvil.PrinterIndex.U)) = (55: U8)
        goto .57
        */


        val __tmp_2447 = ((generalRegFiles(11.U) + 12.U(8.W)) + generalRegFiles(14.U).asUInt)
        val __tmp_2448 = (55.U(8.W)).asUInt
        arrayRegFiles(__tmp_2447 + 0.U) := __tmp_2448(7, 0)

        CP := 57.U
      }

      is(57.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $13 = (($1: anvil.PrinterIndex.U) + (7: anvil.PrinterIndex.U))
        goto .58
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(13.U) := (generalRegFiles(1.U) + 7.U(64.W))

        CP := 58.U
      }

      is(58.U) {
        /*
        $14 = (($13: anvil.PrinterIndex.U) & ($2: anvil.PrinterIndex.U))
        goto .59
        */



        generalRegFiles(14.U) := (generalRegFiles(13.U) & generalRegFiles(2.U))

        CP := 59.U
      }

      is(59.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($14: anvil.PrinterIndex.U) as SP)) = (50: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($14: anvil.PrinterIndex.U)) = (50: U8)
        goto .60
        */


        val __tmp_2449 = ((generalRegFiles(11.U) + 12.U(8.W)) + generalRegFiles(14.U).asUInt)
        val __tmp_2450 = (50.U(8.W)).asUInt
        arrayRegFiles(__tmp_2449 + 0.U) := __tmp_2450(7, 0)

        CP := 60.U
      }

      is(60.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $13 = (($1: anvil.PrinterIndex.U) + (8: anvil.PrinterIndex.U))
        goto .61
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(13.U) := (generalRegFiles(1.U) + 8.U(64.W))

        CP := 61.U
      }

      is(61.U) {
        /*
        $14 = (($13: anvil.PrinterIndex.U) & ($2: anvil.PrinterIndex.U))
        goto .62
        */



        generalRegFiles(14.U) := (generalRegFiles(13.U) & generalRegFiles(2.U))

        CP := 62.U
      }

      is(62.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($14: anvil.PrinterIndex.U) as SP)) = (48: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($14: anvil.PrinterIndex.U)) = (48: U8)
        goto .63
        */


        val __tmp_2451 = ((generalRegFiles(11.U) + 12.U(8.W)) + generalRegFiles(14.U).asUInt)
        val __tmp_2452 = (48.U(8.W)).asUInt
        arrayRegFiles(__tmp_2451 + 0.U) := __tmp_2452(7, 0)

        CP := 63.U
      }

      is(63.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $13 = (($1: anvil.PrinterIndex.U) + (9: anvil.PrinterIndex.U))
        goto .64
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(13.U) := (generalRegFiles(1.U) + 9.U(64.W))

        CP := 64.U
      }

      is(64.U) {
        /*
        $14 = (($13: anvil.PrinterIndex.U) & ($2: anvil.PrinterIndex.U))
        goto .65
        */



        generalRegFiles(14.U) := (generalRegFiles(13.U) & generalRegFiles(2.U))

        CP := 65.U
      }

      is(65.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($14: anvil.PrinterIndex.U) as SP)) = (51: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($14: anvil.PrinterIndex.U)) = (51: U8)
        goto .66
        */


        val __tmp_2453 = ((generalRegFiles(11.U) + 12.U(8.W)) + generalRegFiles(14.U).asUInt)
        val __tmp_2454 = (51.U(8.W)).asUInt
        arrayRegFiles(__tmp_2453 + 0.U) := __tmp_2454(7, 0)

        CP := 66.U
      }

      is(66.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $13 = (($1: anvil.PrinterIndex.U) + (10: anvil.PrinterIndex.U))
        goto .67
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(13.U) := (generalRegFiles(1.U) + 10.U(64.W))

        CP := 67.U
      }

      is(67.U) {
        /*
        $14 = (($13: anvil.PrinterIndex.U) & ($2: anvil.PrinterIndex.U))
        goto .68
        */



        generalRegFiles(14.U) := (generalRegFiles(13.U) & generalRegFiles(2.U))

        CP := 68.U
      }

      is(68.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($14: anvil.PrinterIndex.U) as SP)) = (54: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($14: anvil.PrinterIndex.U)) = (54: U8)
        goto .69
        */


        val __tmp_2455 = ((generalRegFiles(11.U) + 12.U(8.W)) + generalRegFiles(14.U).asUInt)
        val __tmp_2456 = (54.U(8.W)).asUInt
        arrayRegFiles(__tmp_2455 + 0.U) := __tmp_2456(7, 0)

        CP := 69.U
      }

      is(69.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $13 = (($1: anvil.PrinterIndex.U) + (11: anvil.PrinterIndex.U))
        goto .70
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(13.U) := (generalRegFiles(1.U) + 11.U(64.W))

        CP := 70.U
      }

      is(70.U) {
        /*
        $14 = (($13: anvil.PrinterIndex.U) & ($2: anvil.PrinterIndex.U))
        goto .71
        */



        generalRegFiles(14.U) := (generalRegFiles(13.U) & generalRegFiles(2.U))

        CP := 71.U
      }

      is(71.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($14: anvil.PrinterIndex.U) as SP)) = (56: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($14: anvil.PrinterIndex.U)) = (56: U8)
        goto .72
        */


        val __tmp_2457 = ((generalRegFiles(11.U) + 12.U(8.W)) + generalRegFiles(14.U).asUInt)
        val __tmp_2458 = (56.U(8.W)).asUInt
        arrayRegFiles(__tmp_2457 + 0.U) := __tmp_2458(7, 0)

        CP := 72.U
      }

      is(72.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $13 = (($1: anvil.PrinterIndex.U) + (12: anvil.PrinterIndex.U))
        goto .73
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(13.U) := (generalRegFiles(1.U) + 12.U(64.W))

        CP := 73.U
      }

      is(73.U) {
        /*
        $14 = (($13: anvil.PrinterIndex.U) & ($2: anvil.PrinterIndex.U))
        goto .74
        */



        generalRegFiles(14.U) := (generalRegFiles(13.U) & generalRegFiles(2.U))

        CP := 74.U
      }

      is(74.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($14: anvil.PrinterIndex.U) as SP)) = (53: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($14: anvil.PrinterIndex.U)) = (53: U8)
        goto .75
        */


        val __tmp_2459 = ((generalRegFiles(11.U) + 12.U(8.W)) + generalRegFiles(14.U).asUInt)
        val __tmp_2460 = (53.U(8.W)).asUInt
        arrayRegFiles(__tmp_2459 + 0.U) := __tmp_2460(7, 0)

        CP := 75.U
      }

      is(75.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $13 = (($1: anvil.PrinterIndex.U) + (13: anvil.PrinterIndex.U))
        goto .76
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(13.U) := (generalRegFiles(1.U) + 13.U(64.W))

        CP := 76.U
      }

      is(76.U) {
        /*
        $14 = (($13: anvil.PrinterIndex.U) & ($2: anvil.PrinterIndex.U))
        goto .77
        */



        generalRegFiles(14.U) := (generalRegFiles(13.U) & generalRegFiles(2.U))

        CP := 77.U
      }

      is(77.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($14: anvil.PrinterIndex.U) as SP)) = (52: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($14: anvil.PrinterIndex.U)) = (52: U8)
        goto .78
        */


        val __tmp_2461 = ((generalRegFiles(11.U) + 12.U(8.W)) + generalRegFiles(14.U).asUInt)
        val __tmp_2462 = (52.U(8.W)).asUInt
        arrayRegFiles(__tmp_2461 + 0.U) := __tmp_2462(7, 0)

        CP := 78.U
      }

      is(78.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $13 = (($1: anvil.PrinterIndex.U) + (14: anvil.PrinterIndex.U))
        goto .79
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(13.U) := (generalRegFiles(1.U) + 14.U(64.W))

        CP := 79.U
      }

      is(79.U) {
        /*
        $14 = (($13: anvil.PrinterIndex.U) & ($2: anvil.PrinterIndex.U))
        goto .80
        */



        generalRegFiles(14.U) := (generalRegFiles(13.U) & generalRegFiles(2.U))

        CP := 80.U
      }

      is(80.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($14: anvil.PrinterIndex.U) as SP)) = (55: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($14: anvil.PrinterIndex.U)) = (55: U8)
        goto .81
        */


        val __tmp_2463 = ((generalRegFiles(11.U) + 12.U(8.W)) + generalRegFiles(14.U).asUInt)
        val __tmp_2464 = (55.U(8.W)).asUInt
        arrayRegFiles(__tmp_2463 + 0.U) := __tmp_2464(7, 0)

        CP := 81.U
      }

      is(81.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $13 = (($1: anvil.PrinterIndex.U) + (15: anvil.PrinterIndex.U))
        goto .82
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(13.U) := (generalRegFiles(1.U) + 15.U(64.W))

        CP := 82.U
      }

      is(82.U) {
        /*
        $14 = (($13: anvil.PrinterIndex.U) & ($2: anvil.PrinterIndex.U))
        goto .83
        */



        generalRegFiles(14.U) := (generalRegFiles(13.U) & generalRegFiles(2.U))

        CP := 83.U
      }

      is(83.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($14: anvil.PrinterIndex.U) as SP)) = (55: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($14: anvil.PrinterIndex.U)) = (55: U8)
        goto .84
        */


        val __tmp_2465 = ((generalRegFiles(11.U) + 12.U(8.W)) + generalRegFiles(14.U).asUInt)
        val __tmp_2466 = (55.U(8.W)).asUInt
        arrayRegFiles(__tmp_2465 + 0.U) := __tmp_2466(7, 0)

        CP := 84.U
      }

      is(84.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $13 = (($1: anvil.PrinterIndex.U) + (16: anvil.PrinterIndex.U))
        goto .85
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(13.U) := (generalRegFiles(1.U) + 16.U(64.W))

        CP := 85.U
      }

      is(85.U) {
        /*
        $14 = (($13: anvil.PrinterIndex.U) & ($2: anvil.PrinterIndex.U))
        goto .86
        */



        generalRegFiles(14.U) := (generalRegFiles(13.U) & generalRegFiles(2.U))

        CP := 86.U
      }

      is(86.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($14: anvil.PrinterIndex.U) as SP)) = (53: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($14: anvil.PrinterIndex.U)) = (53: U8)
        goto .87
        */


        val __tmp_2467 = ((generalRegFiles(11.U) + 12.U(8.W)) + generalRegFiles(14.U).asUInt)
        val __tmp_2468 = (53.U(8.W)).asUInt
        arrayRegFiles(__tmp_2467 + 0.U) := __tmp_2468(7, 0)

        CP := 87.U
      }

      is(87.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $13 = (($1: anvil.PrinterIndex.U) + (17: anvil.PrinterIndex.U))
        goto .88
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(13.U) := (generalRegFiles(1.U) + 17.U(64.W))

        CP := 88.U
      }

      is(88.U) {
        /*
        $14 = (($13: anvil.PrinterIndex.U) & ($2: anvil.PrinterIndex.U))
        goto .89
        */



        generalRegFiles(14.U) := (generalRegFiles(13.U) & generalRegFiles(2.U))

        CP := 89.U
      }

      is(89.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($14: anvil.PrinterIndex.U) as SP)) = (56: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($14: anvil.PrinterIndex.U)) = (56: U8)
        goto .90
        */


        val __tmp_2469 = ((generalRegFiles(11.U) + 12.U(8.W)) + generalRegFiles(14.U).asUInt)
        val __tmp_2470 = (56.U(8.W)).asUInt
        arrayRegFiles(__tmp_2469 + 0.U) := __tmp_2470(7, 0)

        CP := 90.U
      }

      is(90.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $13 = (($1: anvil.PrinterIndex.U) + (18: anvil.PrinterIndex.U))
        goto .91
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(13.U) := (generalRegFiles(1.U) + 18.U(64.W))

        CP := 91.U
      }

      is(91.U) {
        /*
        $14 = (($13: anvil.PrinterIndex.U) & ($2: anvil.PrinterIndex.U))
        goto .92
        */



        generalRegFiles(14.U) := (generalRegFiles(13.U) & generalRegFiles(2.U))

        CP := 92.U
      }

      is(92.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($14: anvil.PrinterIndex.U) as SP)) = (48: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($14: anvil.PrinterIndex.U)) = (48: U8)
        goto .93
        */


        val __tmp_2471 = ((generalRegFiles(11.U) + 12.U(8.W)) + generalRegFiles(14.U).asUInt)
        val __tmp_2472 = (48.U(8.W)).asUInt
        arrayRegFiles(__tmp_2471 + 0.U) := __tmp_2472(7, 0)

        CP := 93.U
      }

      is(93.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $13 = (($1: anvil.PrinterIndex.U) + (19: anvil.PrinterIndex.U))
        goto .94
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(13.U) := (generalRegFiles(1.U) + 19.U(64.W))

        CP := 94.U
      }

      is(94.U) {
        /*
        $14 = (($13: anvil.PrinterIndex.U) & ($2: anvil.PrinterIndex.U))
        goto .95
        */



        generalRegFiles(14.U) := (generalRegFiles(13.U) & generalRegFiles(2.U))

        CP := 95.U
      }

      is(95.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($14: anvil.PrinterIndex.U) as SP)) = (56: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($14: anvil.PrinterIndex.U)) = (56: U8)
        goto .96
        */


        val __tmp_2473 = ((generalRegFiles(11.U) + 12.U(8.W)) + generalRegFiles(14.U).asUInt)
        val __tmp_2474 = (56.U(8.W)).asUInt
        arrayRegFiles(__tmp_2473 + 0.U) := __tmp_2474(7, 0)

        CP := 96.U
      }

      is(96.U) {
        /*
        **(SP + (2: SP)) = (20: U64) [unsigned, U64, 8]  // $res = (20: U64)
        goto $ret@0
        */


        val __tmp_2475 = Cat(
          arrayRegFiles((SP + 2.U(8.W)) + 0.U)
        )
        val __tmp_2476 = (20.U(64.W)).asUInt
        arrayRegFiles(__tmp_2475 + 0.U) := __tmp_2476(7, 0)
        arrayRegFiles(__tmp_2475 + 1.U) := __tmp_2476(15, 8)
        arrayRegFiles(__tmp_2475 + 2.U) := __tmp_2476(23, 16)
        arrayRegFiles(__tmp_2475 + 3.U) := __tmp_2476(31, 24)
        arrayRegFiles(__tmp_2475 + 4.U) := __tmp_2476(39, 32)
        arrayRegFiles(__tmp_2475 + 5.U) := __tmp_2476(47, 40)
        arrayRegFiles(__tmp_2475 + 6.U) := __tmp_2476(55, 48)
        arrayRegFiles(__tmp_2475 + 7.U) := __tmp_2476(63, 56)

        CP := Cat(
          arrayRegFiles(SP + 0.U + 1.U),
          arrayRegFiles(SP + 0.U + 0.U)
        )

      }

      is(97.U) {
        /*
        $10 = (($3: S64) ≡ (0: S64))
        goto .98
        */



        generalRegFiles(10.U) := (generalRegFiles(3.U).asSInt === 0.S(64.W)).asUInt

        CP := 98.U
      }

      is(98.U) {
        /*
        if ($10: B) goto .99 else goto .102
        */


        CP := Mux((generalRegFiles(10.U).asUInt) === 1.U, 99.U, 102.U)
      }

      is(99.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $12 = (($1: anvil.PrinterIndex.U) & ($2: anvil.PrinterIndex.U))
        goto .100
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(12.U) := (generalRegFiles(1.U) & generalRegFiles(2.U))

        CP := 100.U
      }

      is(100.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($12: anvil.PrinterIndex.U) as SP)) = (48: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($12: anvil.PrinterIndex.U)) = (48: U8)
        goto .101
        */


        val __tmp_2477 = ((generalRegFiles(11.U) + 12.U(8.W)) + generalRegFiles(12.U).asUInt)
        val __tmp_2478 = (48.U(8.W)).asUInt
        arrayRegFiles(__tmp_2477 + 0.U) := __tmp_2478(7, 0)

        CP := 101.U
      }

      is(101.U) {
        /*
        **(SP + (2: SP)) = (1: U64) [unsigned, U64, 8]  // $res = (1: U64)
        goto $ret@0
        */


        val __tmp_2479 = Cat(
          arrayRegFiles((SP + 2.U(8.W)) + 0.U)
        )
        val __tmp_2480 = (1.U(64.W)).asUInt
        arrayRegFiles(__tmp_2479 + 0.U) := __tmp_2480(7, 0)
        arrayRegFiles(__tmp_2479 + 1.U) := __tmp_2480(15, 8)
        arrayRegFiles(__tmp_2479 + 2.U) := __tmp_2480(23, 16)
        arrayRegFiles(__tmp_2479 + 3.U) := __tmp_2480(31, 24)
        arrayRegFiles(__tmp_2479 + 4.U) := __tmp_2480(39, 32)
        arrayRegFiles(__tmp_2479 + 5.U) := __tmp_2480(47, 40)
        arrayRegFiles(__tmp_2479 + 6.U) := __tmp_2480(55, 48)
        arrayRegFiles(__tmp_2479 + 7.U) := __tmp_2480(63, 56)

        CP := Cat(
          arrayRegFiles(SP + 0.U + 1.U),
          arrayRegFiles(SP + 0.U + 0.U)
        )

      }

      is(102.U) {
        /*
        decl buff: MS[anvil.PrinterIndex.I20, U8] [@3, 34]
        alloc $new@[168,16].5BB7E063: MS[anvil.PrinterIndex.I20, U8] [@37, 34]
        $11 = (SP + (37: SP))
        *(SP + (37: SP)) = (323602724: U32) [unsigned, U32, 4]  // sha3 type signature of MS[anvil.PrinterIndex.I20, U8]: 0x1349C924
        *(SP + (41: SP)) = (20: Z) [signed, Z, 8]  // size of MS[anvil.PrinterIndex.I20, U8]((0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8))
        goto .103
        */



        generalRegFiles(11.U) := (SP + 37.U(8.W))

        val __tmp_2481 = (SP + 37.U(8.W))
        val __tmp_2482 = (323602724.U(32.W)).asUInt
        arrayRegFiles(__tmp_2481 + 0.U) := __tmp_2482(7, 0)
        arrayRegFiles(__tmp_2481 + 1.U) := __tmp_2482(15, 8)
        arrayRegFiles(__tmp_2481 + 2.U) := __tmp_2482(23, 16)
        arrayRegFiles(__tmp_2481 + 3.U) := __tmp_2482(31, 24)

        val __tmp_2483 = (SP + 41.U(8.W))
        val __tmp_2484 = (20.S(64.W)).asUInt
        arrayRegFiles(__tmp_2483 + 0.U) := __tmp_2484(7, 0)
        arrayRegFiles(__tmp_2483 + 1.U) := __tmp_2484(15, 8)
        arrayRegFiles(__tmp_2483 + 2.U) := __tmp_2484(23, 16)
        arrayRegFiles(__tmp_2483 + 3.U) := __tmp_2484(31, 24)
        arrayRegFiles(__tmp_2483 + 4.U) := __tmp_2484(39, 32)
        arrayRegFiles(__tmp_2483 + 5.U) := __tmp_2484(47, 40)
        arrayRegFiles(__tmp_2483 + 6.U) := __tmp_2484(55, 48)
        arrayRegFiles(__tmp_2483 + 7.U) := __tmp_2484(63, 56)

        CP := 103.U
      }

      is(103.U) {
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
        goto .104
        */


        val __tmp_2485 = ((generalRegFiles(11.U) + 12.U(8.W)) + 0.S(8.W).asUInt)
        val __tmp_2486 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_2485 + 0.U) := __tmp_2486(7, 0)

        val __tmp_2487 = ((generalRegFiles(11.U) + 12.U(8.W)) + 1.S(8.W).asUInt)
        val __tmp_2488 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_2487 + 0.U) := __tmp_2488(7, 0)

        val __tmp_2489 = ((generalRegFiles(11.U) + 12.U(8.W)) + 2.S(8.W).asUInt)
        val __tmp_2490 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_2489 + 0.U) := __tmp_2490(7, 0)

        val __tmp_2491 = ((generalRegFiles(11.U) + 12.U(8.W)) + 3.S(8.W).asUInt)
        val __tmp_2492 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_2491 + 0.U) := __tmp_2492(7, 0)

        val __tmp_2493 = ((generalRegFiles(11.U) + 12.U(8.W)) + 4.S(8.W).asUInt)
        val __tmp_2494 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_2493 + 0.U) := __tmp_2494(7, 0)

        val __tmp_2495 = ((generalRegFiles(11.U) + 12.U(8.W)) + 5.S(8.W).asUInt)
        val __tmp_2496 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_2495 + 0.U) := __tmp_2496(7, 0)

        val __tmp_2497 = ((generalRegFiles(11.U) + 12.U(8.W)) + 6.S(8.W).asUInt)
        val __tmp_2498 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_2497 + 0.U) := __tmp_2498(7, 0)

        val __tmp_2499 = ((generalRegFiles(11.U) + 12.U(8.W)) + 7.S(8.W).asUInt)
        val __tmp_2500 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_2499 + 0.U) := __tmp_2500(7, 0)

        val __tmp_2501 = ((generalRegFiles(11.U) + 12.U(8.W)) + 8.S(8.W).asUInt)
        val __tmp_2502 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_2501 + 0.U) := __tmp_2502(7, 0)

        val __tmp_2503 = ((generalRegFiles(11.U) + 12.U(8.W)) + 9.S(8.W).asUInt)
        val __tmp_2504 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_2503 + 0.U) := __tmp_2504(7, 0)

        val __tmp_2505 = ((generalRegFiles(11.U) + 12.U(8.W)) + 10.S(8.W).asUInt)
        val __tmp_2506 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_2505 + 0.U) := __tmp_2506(7, 0)

        val __tmp_2507 = ((generalRegFiles(11.U) + 12.U(8.W)) + 11.S(8.W).asUInt)
        val __tmp_2508 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_2507 + 0.U) := __tmp_2508(7, 0)

        val __tmp_2509 = ((generalRegFiles(11.U) + 12.U(8.W)) + 12.S(8.W).asUInt)
        val __tmp_2510 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_2509 + 0.U) := __tmp_2510(7, 0)

        val __tmp_2511 = ((generalRegFiles(11.U) + 12.U(8.W)) + 13.S(8.W).asUInt)
        val __tmp_2512 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_2511 + 0.U) := __tmp_2512(7, 0)

        val __tmp_2513 = ((generalRegFiles(11.U) + 12.U(8.W)) + 14.S(8.W).asUInt)
        val __tmp_2514 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_2513 + 0.U) := __tmp_2514(7, 0)

        val __tmp_2515 = ((generalRegFiles(11.U) + 12.U(8.W)) + 15.S(8.W).asUInt)
        val __tmp_2516 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_2515 + 0.U) := __tmp_2516(7, 0)

        val __tmp_2517 = ((generalRegFiles(11.U) + 12.U(8.W)) + 16.S(8.W).asUInt)
        val __tmp_2518 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_2517 + 0.U) := __tmp_2518(7, 0)

        val __tmp_2519 = ((generalRegFiles(11.U) + 12.U(8.W)) + 17.S(8.W).asUInt)
        val __tmp_2520 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_2519 + 0.U) := __tmp_2520(7, 0)

        val __tmp_2521 = ((generalRegFiles(11.U) + 12.U(8.W)) + 18.S(8.W).asUInt)
        val __tmp_2522 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_2521 + 0.U) := __tmp_2522(7, 0)

        val __tmp_2523 = ((generalRegFiles(11.U) + 12.U(8.W)) + 19.S(8.W).asUInt)
        val __tmp_2524 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_2523 + 0.U) := __tmp_2524(7, 0)

        CP := 104.U
      }

      is(104.U) {
        /*
        (SP + (3: SP)) [MS[anvil.PrinterIndex.I20, U8], 34]  <-  ($11: MS[anvil.PrinterIndex.I20, U8]) [MS[anvil.PrinterIndex.I20, U8], ((*(($11: MS[anvil.PrinterIndex.I20, U8]) + (4: SP)) as SP) + (12: SP))]  // buff = ($11: MS[anvil.PrinterIndex.I20, U8])
        goto .105
        */


        val __tmp_2525 = (SP + 3.U(8.W))
        val __tmp_2526 = generalRegFiles(11.U)
        val __tmp_2527 = (Cat(
           arrayRegFiles((generalRegFiles(11.U) + 4.U(8.W)) + 7.U),
           arrayRegFiles((generalRegFiles(11.U) + 4.U(8.W)) + 6.U),
           arrayRegFiles((generalRegFiles(11.U) + 4.U(8.W)) + 5.U),
           arrayRegFiles((generalRegFiles(11.U) + 4.U(8.W)) + 4.U),
           arrayRegFiles((generalRegFiles(11.U) + 4.U(8.W)) + 3.U),
           arrayRegFiles((generalRegFiles(11.U) + 4.U(8.W)) + 2.U),
           arrayRegFiles((generalRegFiles(11.U) + 4.U(8.W)) + 1.U),
           arrayRegFiles((generalRegFiles(11.U) + 4.U(8.W)) + 0.U)
         ).asSInt.asUInt + 12.U(8.W))

        when(Idx < __tmp_2527) {
          arrayRegFiles(__tmp_2525 + Idx + 0.U) := arrayRegFiles(__tmp_2526 + Idx + 0.U)
          arrayRegFiles(__tmp_2525 + Idx + 1.U) := arrayRegFiles(__tmp_2526 + Idx + 1.U)
          arrayRegFiles(__tmp_2525 + Idx + 2.U) := arrayRegFiles(__tmp_2526 + Idx + 2.U)
          arrayRegFiles(__tmp_2525 + Idx + 3.U) := arrayRegFiles(__tmp_2526 + Idx + 3.U)
          arrayRegFiles(__tmp_2525 + Idx + 4.U) := arrayRegFiles(__tmp_2526 + Idx + 4.U)
          arrayRegFiles(__tmp_2525 + Idx + 5.U) := arrayRegFiles(__tmp_2526 + Idx + 5.U)
          arrayRegFiles(__tmp_2525 + Idx + 6.U) := arrayRegFiles(__tmp_2526 + Idx + 6.U)
          arrayRegFiles(__tmp_2525 + Idx + 7.U) := arrayRegFiles(__tmp_2526 + Idx + 7.U)
          Idx := Idx + 8.U
          LeftByteRounds := __tmp_2527 - Idx
        } .elsewhen(IdxLeftByteRounds < LeftByteRounds) {
          val __tmp_2528 = Idx - 8.U
          arrayRegFiles(__tmp_2525 + __tmp_2528 + IdxLeftByteRounds) := arrayRegFiles(__tmp_2526 + __tmp_2528 + IdxLeftByteRounds)
          IdxLeftByteRounds := IdxLeftByteRounds + 1.U
        } .otherwise {
          Idx := 0.U
          IdxLeftByteRounds := 0.U
          LeftByteRounds := 0.U
          CP := 105.U
        }


      }

      is(105.U) {
        /*
        unalloc $new@[168,16].5BB7E063: MS[anvil.PrinterIndex.I20, U8] [@37, 34]
        goto .106
        */


        CP := 106.U
      }

      is(106.U) {
        /*
        decl i: anvil.PrinterIndex.I20 @$4
        $4 = (0: anvil.PrinterIndex.I20)
        goto .107
        */



        generalRegFiles(4.U) := (0.S(8.W)).asUInt

        CP := 107.U
      }

      is(107.U) {
        /*
        decl neg: B @$5
        $10 = (($3: S64) < (0: S64))
        goto .108
        */



        generalRegFiles(10.U) := (generalRegFiles(3.U).asSInt < 0.S(64.W)).asUInt

        CP := 108.U
      }

      is(108.U) {
        /*
        $5 = ($10: B)
        goto .109
        */



        generalRegFiles(5.U) := generalRegFiles(10.U)

        CP := 109.U
      }

      is(109.U) {
        /*
        decl m: S64 @$6
        goto .110
        */


        CP := 110.U
      }

      is(110.U) {
        /*
        if ($5: B) goto .111 else goto .113
        */


        CP := Mux((generalRegFiles(5.U).asUInt) === 1.U, 111.U, 113.U)
      }

      is(111.U) {
        /*
        $12 = -(($3: S64))
        goto .112
        */



        generalRegFiles(12.U) := (-generalRegFiles(3.U).asSInt).asUInt

        CP := 112.U
      }

      is(112.U) {
        /*
        $10 = ($12: S64)
        goto .115
        */



        generalRegFiles(10.U) := (generalRegFiles(12.U).asSInt).asUInt

        CP := 115.U
      }

      is(113.U) {
        /*
        $14 = ($3: S64)
        goto .114
        */



        generalRegFiles(14.U) := (generalRegFiles(3.U).asSInt).asUInt

        CP := 114.U
      }

      is(114.U) {
        /*
        $10 = ($14: S64)
        goto .115
        */



        generalRegFiles(10.U) := (generalRegFiles(14.U).asSInt).asUInt

        CP := 115.U
      }

      is(115.U) {
        /*
        $6 = ($10: S64)
        goto .116
        */



        generalRegFiles(6.U) := (generalRegFiles(10.U).asSInt).asUInt

        CP := 116.U
      }

      is(116.U) {
        /*
        $11 = ($6: S64)
        goto .117
        */



        generalRegFiles(11.U) := (generalRegFiles(6.U).asSInt).asUInt

        CP := 117.U
      }

      is(117.U) {
        /*
        $10 = (($11: S64) > (0: S64))
        goto .118
        */



        generalRegFiles(10.U) := (generalRegFiles(11.U).asSInt > 0.S(64.W)).asUInt

        CP := 118.U
      }

      is(118.U) {
        /*
        if ($10: B) goto .119 else goto .148
        */


        CP := Mux((generalRegFiles(10.U).asUInt) === 1.U, 119.U, 148.U)
      }

      is(119.U) {
        /*
        $11 = ($6: S64)
        goto .120
        */



        generalRegFiles(11.U) := (generalRegFiles(6.U).asSInt).asUInt

        CP := 120.U
      }

      is(120.U) {
        /*
        $10 = (($11: S64) % (10: S64))
        goto .121
        */



        generalRegFiles(10.U) := ((generalRegFiles(11.U).asSInt % 10.S(64.W))).asUInt

        CP := 121.U
      }

      is(121.U) {
        /*
        switch (($10: S64))
          (0: S64): goto 122
          (1: S64): goto 124
          (2: S64): goto 126
          (3: S64): goto 128
          (4: S64): goto 130
          (5: S64): goto 132
          (6: S64): goto 134
          (7: S64): goto 136
          (8: S64): goto 138
          (9: S64): goto 140

        */


        val __tmp_2529 = generalRegFiles(10.U).asSInt

        switch(__tmp_2529) {

          is(0.S(64.W)) {
            CP := 122.U
          }


          is(1.S(64.W)) {
            CP := 124.U
          }


          is(2.S(64.W)) {
            CP := 126.U
          }


          is(3.S(64.W)) {
            CP := 128.U
          }


          is(4.S(64.W)) {
            CP := 130.U
          }


          is(5.S(64.W)) {
            CP := 132.U
          }


          is(6.S(64.W)) {
            CP := 134.U
          }


          is(7.S(64.W)) {
            CP := 136.U
          }


          is(8.S(64.W)) {
            CP := 138.U
          }


          is(9.S(64.W)) {
            CP := 140.U
          }

        }

      }

      is(122.U) {
        /*
        $11 = (SP + (3: SP))
        $10 = ($4: anvil.PrinterIndex.I20)
        goto .123
        */



        generalRegFiles(11.U) := (SP + 3.U(8.W))


        generalRegFiles(10.U) := (generalRegFiles(4.U).asSInt).asUInt

        CP := 123.U
      }

      is(123.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.I20, U8]) + (12: SP)) + (($10: anvil.PrinterIndex.I20) as SP)) = (48: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.I20, U8])(($10: anvil.PrinterIndex.I20)) = (48: U8)
        goto .142
        */


        val __tmp_2530 = ((generalRegFiles(11.U) + 12.U(8.W)) + generalRegFiles(10.U).asSInt.asUInt)
        val __tmp_2531 = (48.U(8.W)).asUInt
        arrayRegFiles(__tmp_2530 + 0.U) := __tmp_2531(7, 0)

        CP := 142.U
      }

      is(124.U) {
        /*
        $11 = (SP + (3: SP))
        $10 = ($4: anvil.PrinterIndex.I20)
        goto .125
        */



        generalRegFiles(11.U) := (SP + 3.U(8.W))


        generalRegFiles(10.U) := (generalRegFiles(4.U).asSInt).asUInt

        CP := 125.U
      }

      is(125.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.I20, U8]) + (12: SP)) + (($10: anvil.PrinterIndex.I20) as SP)) = (49: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.I20, U8])(($10: anvil.PrinterIndex.I20)) = (49: U8)
        goto .142
        */


        val __tmp_2532 = ((generalRegFiles(11.U) + 12.U(8.W)) + generalRegFiles(10.U).asSInt.asUInt)
        val __tmp_2533 = (49.U(8.W)).asUInt
        arrayRegFiles(__tmp_2532 + 0.U) := __tmp_2533(7, 0)

        CP := 142.U
      }

      is(126.U) {
        /*
        $11 = (SP + (3: SP))
        $10 = ($4: anvil.PrinterIndex.I20)
        goto .127
        */



        generalRegFiles(11.U) := (SP + 3.U(8.W))


        generalRegFiles(10.U) := (generalRegFiles(4.U).asSInt).asUInt

        CP := 127.U
      }

      is(127.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.I20, U8]) + (12: SP)) + (($10: anvil.PrinterIndex.I20) as SP)) = (50: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.I20, U8])(($10: anvil.PrinterIndex.I20)) = (50: U8)
        goto .142
        */


        val __tmp_2534 = ((generalRegFiles(11.U) + 12.U(8.W)) + generalRegFiles(10.U).asSInt.asUInt)
        val __tmp_2535 = (50.U(8.W)).asUInt
        arrayRegFiles(__tmp_2534 + 0.U) := __tmp_2535(7, 0)

        CP := 142.U
      }

      is(128.U) {
        /*
        $11 = (SP + (3: SP))
        $10 = ($4: anvil.PrinterIndex.I20)
        goto .129
        */



        generalRegFiles(11.U) := (SP + 3.U(8.W))


        generalRegFiles(10.U) := (generalRegFiles(4.U).asSInt).asUInt

        CP := 129.U
      }

      is(129.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.I20, U8]) + (12: SP)) + (($10: anvil.PrinterIndex.I20) as SP)) = (51: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.I20, U8])(($10: anvil.PrinterIndex.I20)) = (51: U8)
        goto .142
        */


        val __tmp_2536 = ((generalRegFiles(11.U) + 12.U(8.W)) + generalRegFiles(10.U).asSInt.asUInt)
        val __tmp_2537 = (51.U(8.W)).asUInt
        arrayRegFiles(__tmp_2536 + 0.U) := __tmp_2537(7, 0)

        CP := 142.U
      }

      is(130.U) {
        /*
        $11 = (SP + (3: SP))
        $10 = ($4: anvil.PrinterIndex.I20)
        goto .131
        */



        generalRegFiles(11.U) := (SP + 3.U(8.W))


        generalRegFiles(10.U) := (generalRegFiles(4.U).asSInt).asUInt

        CP := 131.U
      }

      is(131.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.I20, U8]) + (12: SP)) + (($10: anvil.PrinterIndex.I20) as SP)) = (52: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.I20, U8])(($10: anvil.PrinterIndex.I20)) = (52: U8)
        goto .142
        */


        val __tmp_2538 = ((generalRegFiles(11.U) + 12.U(8.W)) + generalRegFiles(10.U).asSInt.asUInt)
        val __tmp_2539 = (52.U(8.W)).asUInt
        arrayRegFiles(__tmp_2538 + 0.U) := __tmp_2539(7, 0)

        CP := 142.U
      }

      is(132.U) {
        /*
        $11 = (SP + (3: SP))
        $10 = ($4: anvil.PrinterIndex.I20)
        goto .133
        */



        generalRegFiles(11.U) := (SP + 3.U(8.W))


        generalRegFiles(10.U) := (generalRegFiles(4.U).asSInt).asUInt

        CP := 133.U
      }

      is(133.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.I20, U8]) + (12: SP)) + (($10: anvil.PrinterIndex.I20) as SP)) = (53: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.I20, U8])(($10: anvil.PrinterIndex.I20)) = (53: U8)
        goto .142
        */


        val __tmp_2540 = ((generalRegFiles(11.U) + 12.U(8.W)) + generalRegFiles(10.U).asSInt.asUInt)
        val __tmp_2541 = (53.U(8.W)).asUInt
        arrayRegFiles(__tmp_2540 + 0.U) := __tmp_2541(7, 0)

        CP := 142.U
      }

      is(134.U) {
        /*
        $11 = (SP + (3: SP))
        $10 = ($4: anvil.PrinterIndex.I20)
        goto .135
        */



        generalRegFiles(11.U) := (SP + 3.U(8.W))


        generalRegFiles(10.U) := (generalRegFiles(4.U).asSInt).asUInt

        CP := 135.U
      }

      is(135.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.I20, U8]) + (12: SP)) + (($10: anvil.PrinterIndex.I20) as SP)) = (54: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.I20, U8])(($10: anvil.PrinterIndex.I20)) = (54: U8)
        goto .142
        */


        val __tmp_2542 = ((generalRegFiles(11.U) + 12.U(8.W)) + generalRegFiles(10.U).asSInt.asUInt)
        val __tmp_2543 = (54.U(8.W)).asUInt
        arrayRegFiles(__tmp_2542 + 0.U) := __tmp_2543(7, 0)

        CP := 142.U
      }

      is(136.U) {
        /*
        $11 = (SP + (3: SP))
        $10 = ($4: anvil.PrinterIndex.I20)
        goto .137
        */



        generalRegFiles(11.U) := (SP + 3.U(8.W))


        generalRegFiles(10.U) := (generalRegFiles(4.U).asSInt).asUInt

        CP := 137.U
      }

      is(137.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.I20, U8]) + (12: SP)) + (($10: anvil.PrinterIndex.I20) as SP)) = (55: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.I20, U8])(($10: anvil.PrinterIndex.I20)) = (55: U8)
        goto .142
        */


        val __tmp_2544 = ((generalRegFiles(11.U) + 12.U(8.W)) + generalRegFiles(10.U).asSInt.asUInt)
        val __tmp_2545 = (55.U(8.W)).asUInt
        arrayRegFiles(__tmp_2544 + 0.U) := __tmp_2545(7, 0)

        CP := 142.U
      }

      is(138.U) {
        /*
        $11 = (SP + (3: SP))
        $10 = ($4: anvil.PrinterIndex.I20)
        goto .139
        */



        generalRegFiles(11.U) := (SP + 3.U(8.W))


        generalRegFiles(10.U) := (generalRegFiles(4.U).asSInt).asUInt

        CP := 139.U
      }

      is(139.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.I20, U8]) + (12: SP)) + (($10: anvil.PrinterIndex.I20) as SP)) = (56: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.I20, U8])(($10: anvil.PrinterIndex.I20)) = (56: U8)
        goto .142
        */


        val __tmp_2546 = ((generalRegFiles(11.U) + 12.U(8.W)) + generalRegFiles(10.U).asSInt.asUInt)
        val __tmp_2547 = (56.U(8.W)).asUInt
        arrayRegFiles(__tmp_2546 + 0.U) := __tmp_2547(7, 0)

        CP := 142.U
      }

      is(140.U) {
        /*
        $11 = (SP + (3: SP))
        $10 = ($4: anvil.PrinterIndex.I20)
        goto .141
        */



        generalRegFiles(11.U) := (SP + 3.U(8.W))


        generalRegFiles(10.U) := (generalRegFiles(4.U).asSInt).asUInt

        CP := 141.U
      }

      is(141.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.I20, U8]) + (12: SP)) + (($10: anvil.PrinterIndex.I20) as SP)) = (57: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.I20, U8])(($10: anvil.PrinterIndex.I20)) = (57: U8)
        goto .142
        */


        val __tmp_2548 = ((generalRegFiles(11.U) + 12.U(8.W)) + generalRegFiles(10.U).asSInt.asUInt)
        val __tmp_2549 = (57.U(8.W)).asUInt
        arrayRegFiles(__tmp_2548 + 0.U) := __tmp_2549(7, 0)

        CP := 142.U
      }

      is(142.U) {
        /*
        $11 = ($6: S64)
        goto .143
        */



        generalRegFiles(11.U) := (generalRegFiles(6.U).asSInt).asUInt

        CP := 143.U
      }

      is(143.U) {
        /*
        $10 = (($11: S64) / (10: S64))
        goto .144
        */



        generalRegFiles(10.U) := ((generalRegFiles(11.U).asSInt / 10.S(64.W))).asUInt

        CP := 144.U
      }

      is(144.U) {
        /*
        $6 = ($10: S64)
        goto .145
        */



        generalRegFiles(6.U) := (generalRegFiles(10.U).asSInt).asUInt

        CP := 145.U
      }

      is(145.U) {
        /*
        $11 = ($4: anvil.PrinterIndex.I20)
        goto .146
        */



        generalRegFiles(11.U) := (generalRegFiles(4.U).asSInt).asUInt

        CP := 146.U
      }

      is(146.U) {
        /*
        $10 = (($11: anvil.PrinterIndex.I20) + (1: anvil.PrinterIndex.I20))
        goto .147
        */



        generalRegFiles(10.U) := ((generalRegFiles(11.U).asSInt + 1.S(8.W))).asUInt

        CP := 147.U
      }

      is(147.U) {
        /*
        $4 = ($10: anvil.PrinterIndex.I20)
        goto .116
        */



        generalRegFiles(4.U) := (generalRegFiles(10.U).asSInt).asUInt

        CP := 116.U
      }

      is(148.U) {
        /*
        $11 = ($5: B)
        undecl neg: B @$5
        goto .149
        */



        generalRegFiles(11.U) := generalRegFiles(5.U)

        CP := 149.U
      }

      is(149.U) {
        /*
        if ($11: B) goto .150 else goto .155
        */


        CP := Mux((generalRegFiles(11.U).asUInt) === 1.U, 150.U, 155.U)
      }

      is(150.U) {
        /*
        $11 = (SP + (3: SP))
        $10 = ($4: anvil.PrinterIndex.I20)
        goto .151
        */



        generalRegFiles(11.U) := (SP + 3.U(8.W))


        generalRegFiles(10.U) := (generalRegFiles(4.U).asSInt).asUInt

        CP := 151.U
      }

      is(151.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.I20, U8]) + (12: SP)) + (($10: anvil.PrinterIndex.I20) as SP)) = (45: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.I20, U8])(($10: anvil.PrinterIndex.I20)) = (45: U8)
        goto .152
        */


        val __tmp_2550 = ((generalRegFiles(11.U) + 12.U(8.W)) + generalRegFiles(10.U).asSInt.asUInt)
        val __tmp_2551 = (45.U(8.W)).asUInt
        arrayRegFiles(__tmp_2550 + 0.U) := __tmp_2551(7, 0)

        CP := 152.U
      }

      is(152.U) {
        /*
        $11 = ($4: anvil.PrinterIndex.I20)
        goto .153
        */



        generalRegFiles(11.U) := (generalRegFiles(4.U).asSInt).asUInt

        CP := 153.U
      }

      is(153.U) {
        /*
        $10 = (($11: anvil.PrinterIndex.I20) + (1: anvil.PrinterIndex.I20))
        goto .154
        */



        generalRegFiles(10.U) := ((generalRegFiles(11.U).asSInt + 1.S(8.W))).asUInt

        CP := 154.U
      }

      is(154.U) {
        /*
        $4 = ($10: anvil.PrinterIndex.I20)
        goto .155
        */



        generalRegFiles(4.U) := (generalRegFiles(10.U).asSInt).asUInt

        CP := 155.U
      }

      is(155.U) {
        /*
        decl j: anvil.PrinterIndex.I20 @$7
        $11 = ($4: anvil.PrinterIndex.I20)
        undecl i: anvil.PrinterIndex.I20 @$4
        goto .156
        */



        generalRegFiles(11.U) := (generalRegFiles(4.U).asSInt).asUInt

        CP := 156.U
      }

      is(156.U) {
        /*
        $10 = (($11: anvil.PrinterIndex.I20) - (1: anvil.PrinterIndex.I20))
        goto .157
        */



        generalRegFiles(10.U) := ((generalRegFiles(11.U).asSInt - 1.S(8.W))).asUInt

        CP := 157.U
      }

      is(157.U) {
        /*
        $7 = ($10: anvil.PrinterIndex.I20)
        goto .158
        */



        generalRegFiles(7.U) := (generalRegFiles(10.U).asSInt).asUInt

        CP := 158.U
      }

      is(158.U) {
        /*
        decl idx: anvil.PrinterIndex.U @$8
        $11 = ($1: anvil.PrinterIndex.U)
        goto .159
        */



        generalRegFiles(11.U) := generalRegFiles(1.U)

        CP := 159.U
      }

      is(159.U) {
        /*
        $8 = ($11: anvil.PrinterIndex.U)
        goto .160
        */



        generalRegFiles(8.U) := generalRegFiles(11.U)

        CP := 160.U
      }

      is(160.U) {
        /*
        decl r: U64 @$9
        $9 = (0: U64)
        goto .161
        */



        generalRegFiles(9.U) := 0.U(64.W)

        CP := 161.U
      }

      is(161.U) {
        /*
        $11 = ($7: anvil.PrinterIndex.I20)
        goto .162
        */



        generalRegFiles(11.U) := (generalRegFiles(7.U).asSInt).asUInt

        CP := 162.U
      }

      is(162.U) {
        /*
        $10 = (($11: anvil.PrinterIndex.I20) >= (0: anvil.PrinterIndex.I20))
        goto .163
        */



        generalRegFiles(10.U) := (generalRegFiles(11.U).asSInt >= 0.S(8.W)).asUInt

        CP := 163.U
      }

      is(163.U) {
        /*
        if ($10: B) goto .164 else goto .178
        */


        CP := Mux((generalRegFiles(10.U).asUInt) === 1.U, 164.U, 178.U)
      }

      is(164.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $10 = ($8: anvil.PrinterIndex.U)
        $13 = ($2: anvil.PrinterIndex.U)
        goto .165
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(10.U) := generalRegFiles(8.U)


        generalRegFiles(13.U) := generalRegFiles(2.U)

        CP := 165.U
      }

      is(165.U) {
        /*
        $12 = (($10: anvil.PrinterIndex.U) & ($13: anvil.PrinterIndex.U))
        goto .166
        */



        generalRegFiles(12.U) := (generalRegFiles(10.U) & generalRegFiles(13.U))

        CP := 166.U
      }

      is(166.U) {
        /*
        $14 = (SP + (3: SP))
        $15 = ($7: anvil.PrinterIndex.I20)
        goto .167
        */



        generalRegFiles(14.U) := (SP + 3.U(8.W))


        generalRegFiles(15.U) := (generalRegFiles(7.U).asSInt).asUInt

        CP := 167.U
      }

      is(167.U) {
        /*
        $16 = *((($14: MS[anvil.PrinterIndex.I20, U8]) + (12: SP)) + (($15: anvil.PrinterIndex.I20) as SP)) [unsigned, U8, 1]  // $16 = ($14: MS[anvil.PrinterIndex.I20, U8])(($15: anvil.PrinterIndex.I20))
        goto .168
        */


        val __tmp_2552 = (((generalRegFiles(14.U) + 12.U(8.W)) + generalRegFiles(15.U).asSInt.asUInt)).asUInt
        generalRegFiles(16.U) := Cat(
          arrayRegFiles(__tmp_2552 + 0.U)
        ).asUInt

        CP := 168.U
      }

      is(168.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($12: anvil.PrinterIndex.U) as SP)) = ($16: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($12: anvil.PrinterIndex.U)) = ($16: U8)
        goto .169
        */


        val __tmp_2553 = ((generalRegFiles(11.U) + 12.U(8.W)) + generalRegFiles(12.U).asUInt)
        val __tmp_2554 = (generalRegFiles(16.U)).asUInt
        arrayRegFiles(__tmp_2553 + 0.U) := __tmp_2554(7, 0)

        CP := 169.U
      }

      is(169.U) {
        /*
        $11 = ($7: anvil.PrinterIndex.I20)
        goto .170
        */



        generalRegFiles(11.U) := (generalRegFiles(7.U).asSInt).asUInt

        CP := 170.U
      }

      is(170.U) {
        /*
        $10 = (($11: anvil.PrinterIndex.I20) - (1: anvil.PrinterIndex.I20))
        goto .171
        */



        generalRegFiles(10.U) := ((generalRegFiles(11.U).asSInt - 1.S(8.W))).asUInt

        CP := 171.U
      }

      is(171.U) {
        /*
        $7 = ($10: anvil.PrinterIndex.I20)
        goto .172
        */



        generalRegFiles(7.U) := (generalRegFiles(10.U).asSInt).asUInt

        CP := 172.U
      }

      is(172.U) {
        /*
        $11 = ($8: anvil.PrinterIndex.U)
        goto .173
        */



        generalRegFiles(11.U) := generalRegFiles(8.U)

        CP := 173.U
      }

      is(173.U) {
        /*
        $10 = (($11: anvil.PrinterIndex.U) + (1: anvil.PrinterIndex.U))
        goto .174
        */



        generalRegFiles(10.U) := (generalRegFiles(11.U) + 1.U(64.W))

        CP := 174.U
      }

      is(174.U) {
        /*
        $8 = ($10: anvil.PrinterIndex.U)
        goto .175
        */



        generalRegFiles(8.U) := generalRegFiles(10.U)

        CP := 175.U
      }

      is(175.U) {
        /*
        $11 = ($9: U64)
        goto .176
        */



        generalRegFiles(11.U) := generalRegFiles(9.U)

        CP := 176.U
      }

      is(176.U) {
        /*
        $10 = (($11: U64) + (1: U64))
        goto .177
        */



        generalRegFiles(10.U) := (generalRegFiles(11.U) + 1.U(64.W))

        CP := 177.U
      }

      is(177.U) {
        /*
        $9 = ($10: U64)
        goto .161
        */



        generalRegFiles(9.U) := generalRegFiles(10.U)

        CP := 161.U
      }

      is(178.U) {
        /*
        $11 = ($9: U64)
        undecl r: U64 @$9
        goto .179
        */



        generalRegFiles(11.U) := generalRegFiles(9.U)

        CP := 179.U
      }

      is(179.U) {
        /*
        **(SP + (2: SP)) = ($11: U64) [unsigned, U64, 8]  // $res = ($11: U64)
        goto $ret@0
        */


        val __tmp_2555 = Cat(
          arrayRegFiles((SP + 2.U(8.W)) + 0.U)
        )
        val __tmp_2556 = (generalRegFiles(11.U)).asUInt
        arrayRegFiles(__tmp_2555 + 0.U) := __tmp_2556(7, 0)
        arrayRegFiles(__tmp_2555 + 1.U) := __tmp_2556(15, 8)
        arrayRegFiles(__tmp_2555 + 2.U) := __tmp_2556(23, 16)
        arrayRegFiles(__tmp_2555 + 3.U) := __tmp_2556(31, 24)
        arrayRegFiles(__tmp_2555 + 4.U) := __tmp_2556(39, 32)
        arrayRegFiles(__tmp_2555 + 5.U) := __tmp_2556(47, 40)
        arrayRegFiles(__tmp_2555 + 6.U) := __tmp_2556(55, 48)
        arrayRegFiles(__tmp_2555 + 7.U) := __tmp_2556(63, 56)

        CP := Cat(
          arrayRegFiles(SP + 0.U + 1.U),
          arrayRegFiles(SP + 0.U + 0.U)
        )

      }

      is(180.U) {
        /*
        *(22: SP) = true [unsigned, B, 1]  // Foo = true
        goto .181
        */


        val __tmp_2557 = 22.U(8.W)
        val __tmp_2558 = (1.U).asUInt
        arrayRegFiles(__tmp_2557 + 0.U) := __tmp_2558(7, 0)

        CP := 181.U
      }

      is(181.U) {
        /*
        $0 = ((2: Z) * (4: Z))
        goto .182
        */



        generalRegFiles(0.U) := ((2.S(64.W) * 4.S(64.W))).asUInt

        CP := 182.U
      }

      is(182.U) {
        /*
        *(23: SP) = ($0: Z) [signed, Z, 8]  // Foo.x = ($0: Z)
        goto $ret@0
        */


        val __tmp_2559 = 23.U(8.W)
        val __tmp_2560 = (generalRegFiles(0.U).asSInt).asUInt
        arrayRegFiles(__tmp_2559 + 0.U) := __tmp_2560(7, 0)
        arrayRegFiles(__tmp_2559 + 1.U) := __tmp_2560(15, 8)
        arrayRegFiles(__tmp_2559 + 2.U) := __tmp_2560(23, 16)
        arrayRegFiles(__tmp_2559 + 3.U) := __tmp_2560(31, 24)
        arrayRegFiles(__tmp_2559 + 4.U) := __tmp_2560(39, 32)
        arrayRegFiles(__tmp_2559 + 5.U) := __tmp_2560(47, 40)
        arrayRegFiles(__tmp_2559 + 6.U) := __tmp_2560(55, 48)
        arrayRegFiles(__tmp_2559 + 7.U) := __tmp_2560(63, 56)

        CP := Cat(
          arrayRegFiles(SP + 0.U + 1.U),
          arrayRegFiles(SP + 0.U + 0.U)
        )

      }

    }

}


