package GeneralRegFileToBRAM
import chisel3._
import chisel3.util._
import chisel3.experimental._



class PrintlnU64Test (val C_S_AXI_DATA_WIDTH:  Int = 32,
               val C_S_AXI_ADDR_WIDTH:  Int = 32,
               val ARRAY_REG_WIDTH:     Int = 8,
               val ARRAY_REG_DEPTH:     Int = 168,
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

        val __tmp_3395 = 10.U(32.W)
        val __tmp_3396 = (886747591.U(32.W)).asUInt
        arrayRegFiles(__tmp_3395 + 0.U) := __tmp_3396(7, 0)
        arrayRegFiles(__tmp_3395 + 1.U) := __tmp_3396(15, 8)
        arrayRegFiles(__tmp_3395 + 2.U) := __tmp_3396(23, 16)
        arrayRegFiles(__tmp_3395 + 3.U) := __tmp_3396(31, 24)

        val __tmp_3397 = 14.U(16.W)
        val __tmp_3398 = (64.S(64.W)).asUInt
        arrayRegFiles(__tmp_3397 + 0.U) := __tmp_3398(7, 0)
        arrayRegFiles(__tmp_3397 + 1.U) := __tmp_3398(15, 8)
        arrayRegFiles(__tmp_3397 + 2.U) := __tmp_3398(23, 16)
        arrayRegFiles(__tmp_3397 + 3.U) := __tmp_3398(31, 24)
        arrayRegFiles(__tmp_3397 + 4.U) := __tmp_3398(39, 32)
        arrayRegFiles(__tmp_3397 + 5.U) := __tmp_3398(47, 40)
        arrayRegFiles(__tmp_3397 + 6.U) := __tmp_3398(55, 48)
        arrayRegFiles(__tmp_3397 + 7.U) := __tmp_3398(63, 56)

        val __tmp_3399 = 84.U(16.W)
        val __tmp_3400 = (0.U(16.W)).asUInt
        arrayRegFiles(__tmp_3399 + 0.U) := __tmp_3400(7, 0)
        arrayRegFiles(__tmp_3399 + 1.U) := __tmp_3400(15, 8)

        CP := 4.U
      }

      is(4.U) {
        /*
        $0 = *(0: SP) [signed, Z, 8]  // $0 = $testNum
        goto .5
        */


        val __tmp_3401 = (0.U(16.W)).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_3401 + 7.U),
          arrayRegFiles(__tmp_3401 + 6.U),
          arrayRegFiles(__tmp_3401 + 5.U),
          arrayRegFiles(__tmp_3401 + 4.U),
          arrayRegFiles(__tmp_3401 + 3.U),
          arrayRegFiles(__tmp_3401 + 2.U),
          arrayRegFiles(__tmp_3401 + 1.U),
          arrayRegFiles(__tmp_3401 + 0.U)
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
        *SP = (9: CP) [unsigned, CP, 2]  // $ret@0 = 1325
        *(SP - (8: SP)) = ($0: Z) [signed, Z, 8]  // save $0 (Z)
        goto .8
        */


        val __tmp_3402 = SP
        val __tmp_3403 = (9.U(16.W)).asUInt
        arrayRegFiles(__tmp_3402 + 0.U) := __tmp_3403(7, 0)
        arrayRegFiles(__tmp_3402 + 1.U) := __tmp_3403(15, 8)

        val __tmp_3404 = (SP - 8.U(16.W))
        val __tmp_3405 = (generalRegFiles(0.U).asSInt).asUInt
        arrayRegFiles(__tmp_3404 + 0.U) := __tmp_3405(7, 0)
        arrayRegFiles(__tmp_3404 + 1.U) := __tmp_3405(15, 8)
        arrayRegFiles(__tmp_3404 + 2.U) := __tmp_3405(23, 16)
        arrayRegFiles(__tmp_3404 + 3.U) := __tmp_3405(31, 24)
        arrayRegFiles(__tmp_3404 + 4.U) := __tmp_3405(39, 32)
        arrayRegFiles(__tmp_3404 + 5.U) := __tmp_3405(47, 40)
        arrayRegFiles(__tmp_3404 + 6.U) := __tmp_3405(55, 48)
        arrayRegFiles(__tmp_3404 + 7.U) := __tmp_3405(63, 56)

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


        val __tmp_3406 = ((SP - 8.U(16.W))).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_3406 + 7.U),
          arrayRegFiles(__tmp_3406 + 6.U),
          arrayRegFiles(__tmp_3406 + 5.U),
          arrayRegFiles(__tmp_3406 + 4.U),
          arrayRegFiles(__tmp_3406 + 3.U),
          arrayRegFiles(__tmp_3406 + 2.U),
          arrayRegFiles(__tmp_3406 + 1.U),
          arrayRegFiles(__tmp_3406 + 0.U)
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
        *SP = (15: CP) [unsigned, CP, 2]  // $ret@0 = 1327
        *(SP - (8: SP)) = ($0: Z) [signed, Z, 8]  // save $0 (Z)
        goto .14
        */


        val __tmp_3407 = SP
        val __tmp_3408 = (15.U(16.W)).asUInt
        arrayRegFiles(__tmp_3407 + 0.U) := __tmp_3408(7, 0)
        arrayRegFiles(__tmp_3407 + 1.U) := __tmp_3408(15, 8)

        val __tmp_3409 = (SP - 8.U(16.W))
        val __tmp_3410 = (generalRegFiles(0.U).asSInt).asUInt
        arrayRegFiles(__tmp_3409 + 0.U) := __tmp_3410(7, 0)
        arrayRegFiles(__tmp_3409 + 1.U) := __tmp_3410(15, 8)
        arrayRegFiles(__tmp_3409 + 2.U) := __tmp_3410(23, 16)
        arrayRegFiles(__tmp_3409 + 3.U) := __tmp_3410(31, 24)
        arrayRegFiles(__tmp_3409 + 4.U) := __tmp_3410(39, 32)
        arrayRegFiles(__tmp_3409 + 5.U) := __tmp_3410(47, 40)
        arrayRegFiles(__tmp_3409 + 6.U) := __tmp_3410(55, 48)
        arrayRegFiles(__tmp_3409 + 7.U) := __tmp_3410(63, 56)

        CP := 14.U
      }

      is(14.U) {
        /*
        decl $ret: CP [@0, 2]
        goto .30
        */


        CP := 30.U
      }

      is(15.U) {
        /*
        $0 = *(SP - (8: SP)) [signed, Z, 8]  // restore $0 (Z)
        undecl $ret: CP [@0, 2]
        goto .16
        */


        val __tmp_3411 = ((SP - 8.U(16.W))).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_3411 + 7.U),
          arrayRegFiles(__tmp_3411 + 6.U),
          arrayRegFiles(__tmp_3411 + 5.U),
          arrayRegFiles(__tmp_3411 + 4.U),
          arrayRegFiles(__tmp_3411 + 3.U),
          arrayRegFiles(__tmp_3411 + 2.U),
          arrayRegFiles(__tmp_3411 + 1.U),
          arrayRegFiles(__tmp_3411 + 0.U)
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
        *SP = (21: CP) [unsigned, CP, 2]  // $ret@0 = 1329
        goto .20
        */


        val __tmp_3412 = SP
        val __tmp_3413 = (21.U(16.W)).asUInt
        arrayRegFiles(__tmp_3412 + 0.U) := __tmp_3413(7, 0)
        arrayRegFiles(__tmp_3412 + 1.U) := __tmp_3413(15, 8)

        CP := 20.U
      }

      is(20.U) {
        /*
        decl $ret: CP [@0, 2]
        goto .36
        */


        CP := 36.U
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
        SP = SP + 2
        goto .25
        */


        SP := SP + 2.U

        CP := 25.U
      }

      is(25.U) {
        /*
        *SP = (27: CP) [unsigned, CP, 2]  // $ret@0 = 1331
        $4 = (12357: U64)
        goto .26
        */


        val __tmp_3414 = SP
        val __tmp_3415 = (27.U(16.W)).asUInt
        arrayRegFiles(__tmp_3414 + 0.U) := __tmp_3415(7, 0)
        arrayRegFiles(__tmp_3414 + 1.U) := __tmp_3415(15, 8)


        generalRegFiles(4.U) := 12357.U(64.W)

        CP := 26.U
      }

      is(26.U) {
        /*
        decl $ret: CP [@0, 2], n: U64 @$0
        $0 = ($4: U64)
        goto .42
        */



        generalRegFiles(0.U) := generalRegFiles(4.U)

        CP := 42.U
      }

      is(27.U) {
        /*
        undecl n: U64 @$0, $ret: CP [@0, 2]
        goto .28
        */


        CP := 28.U
      }

      is(28.U) {
        /*
        SP = SP - 2
        goto .29
        */


        SP := SP - 2.U

        CP := 29.U
      }

      is(29.U) {
        /*
        goto $ret@0
        */


        CP := Cat(
          arrayRegFiles(SP + 0.U + 1.U),
          arrayRegFiles(SP + 0.U + 0.U)
        )

      }

      is(30.U) {
        /*
        SP = SP + 2
        goto .31
        */


        SP := SP + 2.U

        CP := 31.U
      }

      is(31.U) {
        /*
        *SP = (33: CP) [unsigned, CP, 2]  // $ret@0 = 1333
        $4 = (16: U64)
        goto .32
        */


        val __tmp_3416 = SP
        val __tmp_3417 = (33.U(16.W)).asUInt
        arrayRegFiles(__tmp_3416 + 0.U) := __tmp_3417(7, 0)
        arrayRegFiles(__tmp_3416 + 1.U) := __tmp_3417(15, 8)


        generalRegFiles(4.U) := 16.U(64.W)

        CP := 32.U
      }

      is(32.U) {
        /*
        decl $ret: CP [@0, 2], n: U64 @$0
        $0 = ($4: U64)
        goto .42
        */



        generalRegFiles(0.U) := generalRegFiles(4.U)

        CP := 42.U
      }

      is(33.U) {
        /*
        undecl n: U64 @$0, $ret: CP [@0, 2]
        goto .34
        */


        CP := 34.U
      }

      is(34.U) {
        /*
        SP = SP - 2
        goto .35
        */


        SP := SP - 2.U

        CP := 35.U
      }

      is(35.U) {
        /*
        goto $ret@0
        */


        CP := Cat(
          arrayRegFiles(SP + 0.U + 1.U),
          arrayRegFiles(SP + 0.U + 0.U)
        )

      }

      is(36.U) {
        /*
        SP = SP + 2
        goto .37
        */


        SP := SP + 2.U

        CP := 37.U
      }

      is(37.U) {
        /*
        *SP = (39: CP) [unsigned, CP, 2]  // $ret@0 = 1335
        $4 = (3: U64)
        goto .38
        */


        val __tmp_3418 = SP
        val __tmp_3419 = (39.U(16.W)).asUInt
        arrayRegFiles(__tmp_3418 + 0.U) := __tmp_3419(7, 0)
        arrayRegFiles(__tmp_3418 + 1.U) := __tmp_3419(15, 8)


        generalRegFiles(4.U) := 3.U(64.W)

        CP := 38.U
      }

      is(38.U) {
        /*
        decl $ret: CP [@0, 2], n: U64 @$0
        $0 = ($4: U64)
        goto .42
        */



        generalRegFiles(0.U) := generalRegFiles(4.U)

        CP := 42.U
      }

      is(39.U) {
        /*
        undecl n: U64 @$0, $ret: CP [@0, 2]
        goto .40
        */


        CP := 40.U
      }

      is(40.U) {
        /*
        SP = SP - 2
        goto .41
        */


        SP := SP - 2.U

        CP := 41.U
      }

      is(41.U) {
        /*
        goto $ret@0
        */


        CP := Cat(
          arrayRegFiles(SP + 0.U + 1.U),
          arrayRegFiles(SP + 0.U + 0.U)
        )

      }

      is(42.U) {
        /*
        alloc printU64Hex$res@[6,11].24FF3D9A: U64 [@2, 8]
        goto .43
        */


        CP := 43.U
      }

      is(43.U) {
        /*
        SP = SP + 10
        goto .47
        */


        SP := SP + 10.U

        CP := 47.U
      }

      is(45.U) {
        /*
        DP = DP + (($2: U64) as DP)
        goto .51
        */


        DP := DP + generalRegFiles(2.U).asUInt

        CP := 51.U
      }

      is(47.U) {
        /*
        *SP = (49: CP) [unsigned, CP, 2]  // $ret@0 = 1341
        *(SP + (2: SP)) = (SP - (8: SP)) [unsigned, SP, 2]  // $res@2 = -8
        $91 = (8: SP)
        $92 = DP
        $93 = (63: anvil.PrinterIndex.U)
        $94 = ($0: U64)
        $95 = (16: Z)
        goto .48
        */


        val __tmp_3420 = SP
        val __tmp_3421 = (49.U(16.W)).asUInt
        arrayRegFiles(__tmp_3420 + 0.U) := __tmp_3421(7, 0)
        arrayRegFiles(__tmp_3420 + 1.U) := __tmp_3421(15, 8)

        val __tmp_3422 = (SP + 2.U(16.W))
        val __tmp_3423 = ((SP - 8.U(16.W))).asUInt
        arrayRegFiles(__tmp_3422 + 0.U) := __tmp_3423(7, 0)
        arrayRegFiles(__tmp_3422 + 1.U) := __tmp_3423(15, 8)


        generalRegFiles(91.U) := 8.U(16.W)


        generalRegFiles(92.U) := DP


        generalRegFiles(93.U) := 63.U(64.W)


        generalRegFiles(94.U) := generalRegFiles(0.U)


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
        goto .53
        */



        generalRegFiles(0.U) := generalRegFiles(91.U)


        generalRegFiles(1.U) := generalRegFiles(92.U)


        generalRegFiles(2.U) := generalRegFiles(93.U)


        generalRegFiles(3.U) := generalRegFiles(94.U)


        generalRegFiles(4.U) := (generalRegFiles(95.U).asSInt).asUInt

        CP := 53.U
      }

      is(49.U) {
        /*
        $2 = **(SP + (2: SP)) [unsigned, U64, 8]  // $2 = $res
        undecl digits: Z @$4, n: U64 @$3, mask: anvil.PrinterIndex.U @$2, index: anvil.PrinterIndex.U @$1, buffer: MS[anvil.PrinterIndex.U, U8] @$0, $res: SP [@2, 2], $ret: CP [@0, 2]
        goto .50
        */


        val __tmp_3424 = (Cat(
          arrayRegFiles((SP + 2.U(16.W)) + 1.U),
          arrayRegFiles((SP + 2.U(16.W)) + 0.U)
        )).asUInt
        generalRegFiles(2.U) := Cat(
          arrayRegFiles(__tmp_3424 + 7.U),
          arrayRegFiles(__tmp_3424 + 6.U),
          arrayRegFiles(__tmp_3424 + 5.U),
          arrayRegFiles(__tmp_3424 + 4.U),
          arrayRegFiles(__tmp_3424 + 3.U),
          arrayRegFiles(__tmp_3424 + 2.U),
          arrayRegFiles(__tmp_3424 + 1.U),
          arrayRegFiles(__tmp_3424 + 0.U)
        ).asUInt

        CP := 50.U
      }

      is(50.U) {
        /*
        SP = SP - 10
        goto .45
        */


        SP := SP - 10.U

        CP := 45.U
      }

      is(51.U) {
        /*
        *(((8: SP) + (12: SP)) + ((DP & (63: DP)) as SP)) = (10: U8) [unsigned, U8, 1]  // $display((DP & (63: DP))) = (10: U8)
        goto .52
        */


        val __tmp_3425 = ((8.U(16.W) + 12.U(16.W)) + (DP & 63.U(64.W)).asUInt)
        val __tmp_3426 = (10.U(8.W)).asUInt
        arrayRegFiles(__tmp_3425 + 0.U) := __tmp_3426(7, 0)

        CP := 52.U
      }

      is(52.U) {
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

      is(53.U) {
        /*
        decl buff: MS[anvil.PrinterIndex.I16, U8] [@4, 30]
        alloc $new@[245,16].6203A7B3: MS[anvil.PrinterIndex.I16, U8] [@34, 30]
        $10 = (SP + (34: SP))
        *(SP + (34: SP)) = (1541243932: U32) [unsigned, U32, 4]  // sha3 type signature of MS[anvil.PrinterIndex.I16, U8]: 0x5BDD841C
        *(SP + (38: SP)) = (16: Z) [signed, Z, 8]  // size of MS[anvil.PrinterIndex.I16, U8]((0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8))
        goto .54
        */



        generalRegFiles(10.U) := (SP + 34.U(16.W))

        val __tmp_3427 = (SP + 34.U(16.W))
        val __tmp_3428 = (1541243932.U(32.W)).asUInt
        arrayRegFiles(__tmp_3427 + 0.U) := __tmp_3428(7, 0)
        arrayRegFiles(__tmp_3427 + 1.U) := __tmp_3428(15, 8)
        arrayRegFiles(__tmp_3427 + 2.U) := __tmp_3428(23, 16)
        arrayRegFiles(__tmp_3427 + 3.U) := __tmp_3428(31, 24)

        val __tmp_3429 = (SP + 38.U(16.W))
        val __tmp_3430 = (16.S(64.W)).asUInt
        arrayRegFiles(__tmp_3429 + 0.U) := __tmp_3430(7, 0)
        arrayRegFiles(__tmp_3429 + 1.U) := __tmp_3430(15, 8)
        arrayRegFiles(__tmp_3429 + 2.U) := __tmp_3430(23, 16)
        arrayRegFiles(__tmp_3429 + 3.U) := __tmp_3430(31, 24)
        arrayRegFiles(__tmp_3429 + 4.U) := __tmp_3430(39, 32)
        arrayRegFiles(__tmp_3429 + 5.U) := __tmp_3430(47, 40)
        arrayRegFiles(__tmp_3429 + 6.U) := __tmp_3430(55, 48)
        arrayRegFiles(__tmp_3429 + 7.U) := __tmp_3430(63, 56)

        CP := 54.U
      }

      is(54.U) {
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
        goto .55
        */


        val __tmp_3431 = ((generalRegFiles(10.U) + 12.U(16.W)) + 0.S(8.W).asUInt)
        val __tmp_3432 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_3431 + 0.U) := __tmp_3432(7, 0)

        val __tmp_3433 = ((generalRegFiles(10.U) + 12.U(16.W)) + 1.S(8.W).asUInt)
        val __tmp_3434 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_3433 + 0.U) := __tmp_3434(7, 0)

        val __tmp_3435 = ((generalRegFiles(10.U) + 12.U(16.W)) + 2.S(8.W).asUInt)
        val __tmp_3436 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_3435 + 0.U) := __tmp_3436(7, 0)

        val __tmp_3437 = ((generalRegFiles(10.U) + 12.U(16.W)) + 3.S(8.W).asUInt)
        val __tmp_3438 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_3437 + 0.U) := __tmp_3438(7, 0)

        val __tmp_3439 = ((generalRegFiles(10.U) + 12.U(16.W)) + 4.S(8.W).asUInt)
        val __tmp_3440 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_3439 + 0.U) := __tmp_3440(7, 0)

        val __tmp_3441 = ((generalRegFiles(10.U) + 12.U(16.W)) + 5.S(8.W).asUInt)
        val __tmp_3442 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_3441 + 0.U) := __tmp_3442(7, 0)

        val __tmp_3443 = ((generalRegFiles(10.U) + 12.U(16.W)) + 6.S(8.W).asUInt)
        val __tmp_3444 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_3443 + 0.U) := __tmp_3444(7, 0)

        val __tmp_3445 = ((generalRegFiles(10.U) + 12.U(16.W)) + 7.S(8.W).asUInt)
        val __tmp_3446 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_3445 + 0.U) := __tmp_3446(7, 0)

        val __tmp_3447 = ((generalRegFiles(10.U) + 12.U(16.W)) + 8.S(8.W).asUInt)
        val __tmp_3448 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_3447 + 0.U) := __tmp_3448(7, 0)

        val __tmp_3449 = ((generalRegFiles(10.U) + 12.U(16.W)) + 9.S(8.W).asUInt)
        val __tmp_3450 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_3449 + 0.U) := __tmp_3450(7, 0)

        val __tmp_3451 = ((generalRegFiles(10.U) + 12.U(16.W)) + 10.S(8.W).asUInt)
        val __tmp_3452 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_3451 + 0.U) := __tmp_3452(7, 0)

        val __tmp_3453 = ((generalRegFiles(10.U) + 12.U(16.W)) + 11.S(8.W).asUInt)
        val __tmp_3454 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_3453 + 0.U) := __tmp_3454(7, 0)

        val __tmp_3455 = ((generalRegFiles(10.U) + 12.U(16.W)) + 12.S(8.W).asUInt)
        val __tmp_3456 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_3455 + 0.U) := __tmp_3456(7, 0)

        val __tmp_3457 = ((generalRegFiles(10.U) + 12.U(16.W)) + 13.S(8.W).asUInt)
        val __tmp_3458 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_3457 + 0.U) := __tmp_3458(7, 0)

        val __tmp_3459 = ((generalRegFiles(10.U) + 12.U(16.W)) + 14.S(8.W).asUInt)
        val __tmp_3460 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_3459 + 0.U) := __tmp_3460(7, 0)

        val __tmp_3461 = ((generalRegFiles(10.U) + 12.U(16.W)) + 15.S(8.W).asUInt)
        val __tmp_3462 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_3461 + 0.U) := __tmp_3462(7, 0)

        CP := 55.U
      }

      is(55.U) {
        /*
        (SP + (4: SP)) [MS[anvil.PrinterIndex.I16, U8], 30]  <-  ($10: MS[anvil.PrinterIndex.I16, U8]) [MS[anvil.PrinterIndex.I16, U8], ((*(($10: MS[anvil.PrinterIndex.I16, U8]) + (4: SP)) as SP) + (12: SP))]  // buff = ($10: MS[anvil.PrinterIndex.I16, U8])
        goto .56
        */


        val __tmp_3463 = (SP + 4.U(16.W))
        val __tmp_3464 = generalRegFiles(10.U)
        val __tmp_3465 = (Cat(
           arrayRegFiles((generalRegFiles(10.U) + 4.U(16.W)) + 7.U),
           arrayRegFiles((generalRegFiles(10.U) + 4.U(16.W)) + 6.U),
           arrayRegFiles((generalRegFiles(10.U) + 4.U(16.W)) + 5.U),
           arrayRegFiles((generalRegFiles(10.U) + 4.U(16.W)) + 4.U),
           arrayRegFiles((generalRegFiles(10.U) + 4.U(16.W)) + 3.U),
           arrayRegFiles((generalRegFiles(10.U) + 4.U(16.W)) + 2.U),
           arrayRegFiles((generalRegFiles(10.U) + 4.U(16.W)) + 1.U),
           arrayRegFiles((generalRegFiles(10.U) + 4.U(16.W)) + 0.U)
         ).asSInt.asUInt + 12.U(16.W))

        when(Idx < __tmp_3465) {
          arrayRegFiles(__tmp_3463 + Idx + 0.U) := arrayRegFiles(__tmp_3464 + Idx + 0.U)
          arrayRegFiles(__tmp_3463 + Idx + 1.U) := arrayRegFiles(__tmp_3464 + Idx + 1.U)
          arrayRegFiles(__tmp_3463 + Idx + 2.U) := arrayRegFiles(__tmp_3464 + Idx + 2.U)
          arrayRegFiles(__tmp_3463 + Idx + 3.U) := arrayRegFiles(__tmp_3464 + Idx + 3.U)
          arrayRegFiles(__tmp_3463 + Idx + 4.U) := arrayRegFiles(__tmp_3464 + Idx + 4.U)
          arrayRegFiles(__tmp_3463 + Idx + 5.U) := arrayRegFiles(__tmp_3464 + Idx + 5.U)
          arrayRegFiles(__tmp_3463 + Idx + 6.U) := arrayRegFiles(__tmp_3464 + Idx + 6.U)
          arrayRegFiles(__tmp_3463 + Idx + 7.U) := arrayRegFiles(__tmp_3464 + Idx + 7.U)
          Idx := Idx + 8.U
          LeftByteRounds := __tmp_3465 - Idx
        } .elsewhen(IdxLeftByteRounds < LeftByteRounds) {
          val __tmp_3466 = Idx - 8.U
          arrayRegFiles(__tmp_3463 + __tmp_3466 + IdxLeftByteRounds) := arrayRegFiles(__tmp_3464 + __tmp_3466 + IdxLeftByteRounds)
          IdxLeftByteRounds := IdxLeftByteRounds + 1.U
        } .otherwise {
          Idx := 0.U
          IdxLeftByteRounds := 0.U
          LeftByteRounds := 0.U
          CP := 56.U
        }


      }

      is(56.U) {
        /*
        unalloc $new@[245,16].6203A7B3: MS[anvil.PrinterIndex.I16, U8] [@34, 30]
        goto .57
        */


        CP := 57.U
      }

      is(57.U) {
        /*
        decl i: anvil.PrinterIndex.I16 @$5
        $5 = (0: anvil.PrinterIndex.I16)
        goto .58
        */



        generalRegFiles(5.U) := (0.S(8.W)).asUInt

        CP := 58.U
      }

      is(58.U) {
        /*
        decl m: U64 @$6
        $6 = ($3: U64)
        goto .59
        */



        generalRegFiles(6.U) := generalRegFiles(3.U)

        CP := 59.U
      }

      is(59.U) {
        /*
        decl d: Z @$7
        $7 = ($4: Z)
        goto .60
        */



        generalRegFiles(7.U) := (generalRegFiles(4.U).asSInt).asUInt

        CP := 60.U
      }

      is(60.U) {
        /*
        $10 = ($6: U64)
        goto .61
        */



        generalRegFiles(10.U) := generalRegFiles(6.U)

        CP := 61.U
      }

      is(61.U) {
        /*
        $11 = (($10: U64) > (0: U64))
        goto .62
        */



        generalRegFiles(11.U) := (generalRegFiles(10.U) > 0.U(64.W)).asUInt

        CP := 62.U
      }

      is(62.U) {
        /*
        $12 = ($7: Z)
        goto .63
        */



        generalRegFiles(12.U) := (generalRegFiles(7.U).asSInt).asUInt

        CP := 63.U
      }

      is(63.U) {
        /*
        $13 = (($12: Z) > (0: Z))
        goto .64
        */



        generalRegFiles(13.U) := (generalRegFiles(12.U).asSInt > 0.S(64.W)).asUInt

        CP := 64.U
      }

      is(64.U) {
        /*
        $14 = (($11: B) & ($13: B))
        goto .65
        */



        generalRegFiles(14.U) := (generalRegFiles(11.U) & generalRegFiles(13.U))

        CP := 65.U
      }

      is(65.U) {
        /*
        if ($14: B) goto .66 else goto .110
        */


        CP := Mux((generalRegFiles(14.U).asUInt) === 1.U, 66.U, 110.U)
      }

      is(66.U) {
        /*
        $10 = ($6: U64)
        goto .67
        */



        generalRegFiles(10.U) := generalRegFiles(6.U)

        CP := 67.U
      }

      is(67.U) {
        /*
        $11 = (($10: U64) & (15: U64))
        goto .68
        */



        generalRegFiles(11.U) := (generalRegFiles(10.U) & 15.U(64.W))

        CP := 68.U
      }

      is(68.U) {
        /*
        switch (($11: U64))
          (0: U64): goto 69
          (1: U64): goto 71
          (2: U64): goto 73
          (3: U64): goto 75
          (4: U64): goto 77
          (5: U64): goto 79
          (6: U64): goto 81
          (7: U64): goto 83
          (8: U64): goto 85
          (9: U64): goto 87
          (10: U64): goto 89
          (11: U64): goto 91
          (12: U64): goto 93
          (13: U64): goto 95
          (14: U64): goto 97
          (15: U64): goto 99

        */


        val __tmp_3467 = generalRegFiles(11.U)

        switch(__tmp_3467) {

          is(0.U(64.W)) {
            CP := 69.U
          }


          is(1.U(64.W)) {
            CP := 71.U
          }


          is(2.U(64.W)) {
            CP := 73.U
          }


          is(3.U(64.W)) {
            CP := 75.U
          }


          is(4.U(64.W)) {
            CP := 77.U
          }


          is(5.U(64.W)) {
            CP := 79.U
          }


          is(6.U(64.W)) {
            CP := 81.U
          }


          is(7.U(64.W)) {
            CP := 83.U
          }


          is(8.U(64.W)) {
            CP := 85.U
          }


          is(9.U(64.W)) {
            CP := 87.U
          }


          is(10.U(64.W)) {
            CP := 89.U
          }


          is(11.U(64.W)) {
            CP := 91.U
          }


          is(12.U(64.W)) {
            CP := 93.U
          }


          is(13.U(64.W)) {
            CP := 95.U
          }


          is(14.U(64.W)) {
            CP := 97.U
          }


          is(15.U(64.W)) {
            CP := 99.U
          }

        }

      }

      is(69.U) {
        /*
        $10 = (SP + (4: SP))
        $11 = ($5: anvil.PrinterIndex.I16)
        goto .70
        */



        generalRegFiles(10.U) := (SP + 4.U(16.W))


        generalRegFiles(11.U) := (generalRegFiles(5.U).asSInt).asUInt

        CP := 70.U
      }

      is(70.U) {
        /*
        *((($10: MS[anvil.PrinterIndex.I16, U8]) + (12: SP)) + (($11: anvil.PrinterIndex.I16) as SP)) = (48: U8) [unsigned, U8, 1]  // ($10: MS[anvil.PrinterIndex.I16, U8])(($11: anvil.PrinterIndex.I16)) = (48: U8)
        goto .101
        */


        val __tmp_3468 = ((generalRegFiles(10.U) + 12.U(16.W)) + generalRegFiles(11.U).asSInt.asUInt)
        val __tmp_3469 = (48.U(8.W)).asUInt
        arrayRegFiles(__tmp_3468 + 0.U) := __tmp_3469(7, 0)

        CP := 101.U
      }

      is(71.U) {
        /*
        $10 = (SP + (4: SP))
        $11 = ($5: anvil.PrinterIndex.I16)
        goto .72
        */



        generalRegFiles(10.U) := (SP + 4.U(16.W))


        generalRegFiles(11.U) := (generalRegFiles(5.U).asSInt).asUInt

        CP := 72.U
      }

      is(72.U) {
        /*
        *((($10: MS[anvil.PrinterIndex.I16, U8]) + (12: SP)) + (($11: anvil.PrinterIndex.I16) as SP)) = (49: U8) [unsigned, U8, 1]  // ($10: MS[anvil.PrinterIndex.I16, U8])(($11: anvil.PrinterIndex.I16)) = (49: U8)
        goto .101
        */


        val __tmp_3470 = ((generalRegFiles(10.U) + 12.U(16.W)) + generalRegFiles(11.U).asSInt.asUInt)
        val __tmp_3471 = (49.U(8.W)).asUInt
        arrayRegFiles(__tmp_3470 + 0.U) := __tmp_3471(7, 0)

        CP := 101.U
      }

      is(73.U) {
        /*
        $10 = (SP + (4: SP))
        $11 = ($5: anvil.PrinterIndex.I16)
        goto .74
        */



        generalRegFiles(10.U) := (SP + 4.U(16.W))


        generalRegFiles(11.U) := (generalRegFiles(5.U).asSInt).asUInt

        CP := 74.U
      }

      is(74.U) {
        /*
        *((($10: MS[anvil.PrinterIndex.I16, U8]) + (12: SP)) + (($11: anvil.PrinterIndex.I16) as SP)) = (50: U8) [unsigned, U8, 1]  // ($10: MS[anvil.PrinterIndex.I16, U8])(($11: anvil.PrinterIndex.I16)) = (50: U8)
        goto .101
        */


        val __tmp_3472 = ((generalRegFiles(10.U) + 12.U(16.W)) + generalRegFiles(11.U).asSInt.asUInt)
        val __tmp_3473 = (50.U(8.W)).asUInt
        arrayRegFiles(__tmp_3472 + 0.U) := __tmp_3473(7, 0)

        CP := 101.U
      }

      is(75.U) {
        /*
        $10 = (SP + (4: SP))
        $11 = ($5: anvil.PrinterIndex.I16)
        goto .76
        */



        generalRegFiles(10.U) := (SP + 4.U(16.W))


        generalRegFiles(11.U) := (generalRegFiles(5.U).asSInt).asUInt

        CP := 76.U
      }

      is(76.U) {
        /*
        *((($10: MS[anvil.PrinterIndex.I16, U8]) + (12: SP)) + (($11: anvil.PrinterIndex.I16) as SP)) = (51: U8) [unsigned, U8, 1]  // ($10: MS[anvil.PrinterIndex.I16, U8])(($11: anvil.PrinterIndex.I16)) = (51: U8)
        goto .101
        */


        val __tmp_3474 = ((generalRegFiles(10.U) + 12.U(16.W)) + generalRegFiles(11.U).asSInt.asUInt)
        val __tmp_3475 = (51.U(8.W)).asUInt
        arrayRegFiles(__tmp_3474 + 0.U) := __tmp_3475(7, 0)

        CP := 101.U
      }

      is(77.U) {
        /*
        $10 = (SP + (4: SP))
        $11 = ($5: anvil.PrinterIndex.I16)
        goto .78
        */



        generalRegFiles(10.U) := (SP + 4.U(16.W))


        generalRegFiles(11.U) := (generalRegFiles(5.U).asSInt).asUInt

        CP := 78.U
      }

      is(78.U) {
        /*
        *((($10: MS[anvil.PrinterIndex.I16, U8]) + (12: SP)) + (($11: anvil.PrinterIndex.I16) as SP)) = (52: U8) [unsigned, U8, 1]  // ($10: MS[anvil.PrinterIndex.I16, U8])(($11: anvil.PrinterIndex.I16)) = (52: U8)
        goto .101
        */


        val __tmp_3476 = ((generalRegFiles(10.U) + 12.U(16.W)) + generalRegFiles(11.U).asSInt.asUInt)
        val __tmp_3477 = (52.U(8.W)).asUInt
        arrayRegFiles(__tmp_3476 + 0.U) := __tmp_3477(7, 0)

        CP := 101.U
      }

      is(79.U) {
        /*
        $10 = (SP + (4: SP))
        $11 = ($5: anvil.PrinterIndex.I16)
        goto .80
        */



        generalRegFiles(10.U) := (SP + 4.U(16.W))


        generalRegFiles(11.U) := (generalRegFiles(5.U).asSInt).asUInt

        CP := 80.U
      }

      is(80.U) {
        /*
        *((($10: MS[anvil.PrinterIndex.I16, U8]) + (12: SP)) + (($11: anvil.PrinterIndex.I16) as SP)) = (53: U8) [unsigned, U8, 1]  // ($10: MS[anvil.PrinterIndex.I16, U8])(($11: anvil.PrinterIndex.I16)) = (53: U8)
        goto .101
        */


        val __tmp_3478 = ((generalRegFiles(10.U) + 12.U(16.W)) + generalRegFiles(11.U).asSInt.asUInt)
        val __tmp_3479 = (53.U(8.W)).asUInt
        arrayRegFiles(__tmp_3478 + 0.U) := __tmp_3479(7, 0)

        CP := 101.U
      }

      is(81.U) {
        /*
        $10 = (SP + (4: SP))
        $11 = ($5: anvil.PrinterIndex.I16)
        goto .82
        */



        generalRegFiles(10.U) := (SP + 4.U(16.W))


        generalRegFiles(11.U) := (generalRegFiles(5.U).asSInt).asUInt

        CP := 82.U
      }

      is(82.U) {
        /*
        *((($10: MS[anvil.PrinterIndex.I16, U8]) + (12: SP)) + (($11: anvil.PrinterIndex.I16) as SP)) = (54: U8) [unsigned, U8, 1]  // ($10: MS[anvil.PrinterIndex.I16, U8])(($11: anvil.PrinterIndex.I16)) = (54: U8)
        goto .101
        */


        val __tmp_3480 = ((generalRegFiles(10.U) + 12.U(16.W)) + generalRegFiles(11.U).asSInt.asUInt)
        val __tmp_3481 = (54.U(8.W)).asUInt
        arrayRegFiles(__tmp_3480 + 0.U) := __tmp_3481(7, 0)

        CP := 101.U
      }

      is(83.U) {
        /*
        $10 = (SP + (4: SP))
        $11 = ($5: anvil.PrinterIndex.I16)
        goto .84
        */



        generalRegFiles(10.U) := (SP + 4.U(16.W))


        generalRegFiles(11.U) := (generalRegFiles(5.U).asSInt).asUInt

        CP := 84.U
      }

      is(84.U) {
        /*
        *((($10: MS[anvil.PrinterIndex.I16, U8]) + (12: SP)) + (($11: anvil.PrinterIndex.I16) as SP)) = (55: U8) [unsigned, U8, 1]  // ($10: MS[anvil.PrinterIndex.I16, U8])(($11: anvil.PrinterIndex.I16)) = (55: U8)
        goto .101
        */


        val __tmp_3482 = ((generalRegFiles(10.U) + 12.U(16.W)) + generalRegFiles(11.U).asSInt.asUInt)
        val __tmp_3483 = (55.U(8.W)).asUInt
        arrayRegFiles(__tmp_3482 + 0.U) := __tmp_3483(7, 0)

        CP := 101.U
      }

      is(85.U) {
        /*
        $10 = (SP + (4: SP))
        $11 = ($5: anvil.PrinterIndex.I16)
        goto .86
        */



        generalRegFiles(10.U) := (SP + 4.U(16.W))


        generalRegFiles(11.U) := (generalRegFiles(5.U).asSInt).asUInt

        CP := 86.U
      }

      is(86.U) {
        /*
        *((($10: MS[anvil.PrinterIndex.I16, U8]) + (12: SP)) + (($11: anvil.PrinterIndex.I16) as SP)) = (56: U8) [unsigned, U8, 1]  // ($10: MS[anvil.PrinterIndex.I16, U8])(($11: anvil.PrinterIndex.I16)) = (56: U8)
        goto .101
        */


        val __tmp_3484 = ((generalRegFiles(10.U) + 12.U(16.W)) + generalRegFiles(11.U).asSInt.asUInt)
        val __tmp_3485 = (56.U(8.W)).asUInt
        arrayRegFiles(__tmp_3484 + 0.U) := __tmp_3485(7, 0)

        CP := 101.U
      }

      is(87.U) {
        /*
        $10 = (SP + (4: SP))
        $11 = ($5: anvil.PrinterIndex.I16)
        goto .88
        */



        generalRegFiles(10.U) := (SP + 4.U(16.W))


        generalRegFiles(11.U) := (generalRegFiles(5.U).asSInt).asUInt

        CP := 88.U
      }

      is(88.U) {
        /*
        *((($10: MS[anvil.PrinterIndex.I16, U8]) + (12: SP)) + (($11: anvil.PrinterIndex.I16) as SP)) = (57: U8) [unsigned, U8, 1]  // ($10: MS[anvil.PrinterIndex.I16, U8])(($11: anvil.PrinterIndex.I16)) = (57: U8)
        goto .101
        */


        val __tmp_3486 = ((generalRegFiles(10.U) + 12.U(16.W)) + generalRegFiles(11.U).asSInt.asUInt)
        val __tmp_3487 = (57.U(8.W)).asUInt
        arrayRegFiles(__tmp_3486 + 0.U) := __tmp_3487(7, 0)

        CP := 101.U
      }

      is(89.U) {
        /*
        $10 = (SP + (4: SP))
        $11 = ($5: anvil.PrinterIndex.I16)
        goto .90
        */



        generalRegFiles(10.U) := (SP + 4.U(16.W))


        generalRegFiles(11.U) := (generalRegFiles(5.U).asSInt).asUInt

        CP := 90.U
      }

      is(90.U) {
        /*
        *((($10: MS[anvil.PrinterIndex.I16, U8]) + (12: SP)) + (($11: anvil.PrinterIndex.I16) as SP)) = (65: U8) [unsigned, U8, 1]  // ($10: MS[anvil.PrinterIndex.I16, U8])(($11: anvil.PrinterIndex.I16)) = (65: U8)
        goto .101
        */


        val __tmp_3488 = ((generalRegFiles(10.U) + 12.U(16.W)) + generalRegFiles(11.U).asSInt.asUInt)
        val __tmp_3489 = (65.U(8.W)).asUInt
        arrayRegFiles(__tmp_3488 + 0.U) := __tmp_3489(7, 0)

        CP := 101.U
      }

      is(91.U) {
        /*
        $10 = (SP + (4: SP))
        $11 = ($5: anvil.PrinterIndex.I16)
        goto .92
        */



        generalRegFiles(10.U) := (SP + 4.U(16.W))


        generalRegFiles(11.U) := (generalRegFiles(5.U).asSInt).asUInt

        CP := 92.U
      }

      is(92.U) {
        /*
        *((($10: MS[anvil.PrinterIndex.I16, U8]) + (12: SP)) + (($11: anvil.PrinterIndex.I16) as SP)) = (66: U8) [unsigned, U8, 1]  // ($10: MS[anvil.PrinterIndex.I16, U8])(($11: anvil.PrinterIndex.I16)) = (66: U8)
        goto .101
        */


        val __tmp_3490 = ((generalRegFiles(10.U) + 12.U(16.W)) + generalRegFiles(11.U).asSInt.asUInt)
        val __tmp_3491 = (66.U(8.W)).asUInt
        arrayRegFiles(__tmp_3490 + 0.U) := __tmp_3491(7, 0)

        CP := 101.U
      }

      is(93.U) {
        /*
        $10 = (SP + (4: SP))
        $11 = ($5: anvil.PrinterIndex.I16)
        goto .94
        */



        generalRegFiles(10.U) := (SP + 4.U(16.W))


        generalRegFiles(11.U) := (generalRegFiles(5.U).asSInt).asUInt

        CP := 94.U
      }

      is(94.U) {
        /*
        *((($10: MS[anvil.PrinterIndex.I16, U8]) + (12: SP)) + (($11: anvil.PrinterIndex.I16) as SP)) = (67: U8) [unsigned, U8, 1]  // ($10: MS[anvil.PrinterIndex.I16, U8])(($11: anvil.PrinterIndex.I16)) = (67: U8)
        goto .101
        */


        val __tmp_3492 = ((generalRegFiles(10.U) + 12.U(16.W)) + generalRegFiles(11.U).asSInt.asUInt)
        val __tmp_3493 = (67.U(8.W)).asUInt
        arrayRegFiles(__tmp_3492 + 0.U) := __tmp_3493(7, 0)

        CP := 101.U
      }

      is(95.U) {
        /*
        $10 = (SP + (4: SP))
        $11 = ($5: anvil.PrinterIndex.I16)
        goto .96
        */



        generalRegFiles(10.U) := (SP + 4.U(16.W))


        generalRegFiles(11.U) := (generalRegFiles(5.U).asSInt).asUInt

        CP := 96.U
      }

      is(96.U) {
        /*
        *((($10: MS[anvil.PrinterIndex.I16, U8]) + (12: SP)) + (($11: anvil.PrinterIndex.I16) as SP)) = (68: U8) [unsigned, U8, 1]  // ($10: MS[anvil.PrinterIndex.I16, U8])(($11: anvil.PrinterIndex.I16)) = (68: U8)
        goto .101
        */


        val __tmp_3494 = ((generalRegFiles(10.U) + 12.U(16.W)) + generalRegFiles(11.U).asSInt.asUInt)
        val __tmp_3495 = (68.U(8.W)).asUInt
        arrayRegFiles(__tmp_3494 + 0.U) := __tmp_3495(7, 0)

        CP := 101.U
      }

      is(97.U) {
        /*
        $10 = (SP + (4: SP))
        $11 = ($5: anvil.PrinterIndex.I16)
        goto .98
        */



        generalRegFiles(10.U) := (SP + 4.U(16.W))


        generalRegFiles(11.U) := (generalRegFiles(5.U).asSInt).asUInt

        CP := 98.U
      }

      is(98.U) {
        /*
        *((($10: MS[anvil.PrinterIndex.I16, U8]) + (12: SP)) + (($11: anvil.PrinterIndex.I16) as SP)) = (69: U8) [unsigned, U8, 1]  // ($10: MS[anvil.PrinterIndex.I16, U8])(($11: anvil.PrinterIndex.I16)) = (69: U8)
        goto .101
        */


        val __tmp_3496 = ((generalRegFiles(10.U) + 12.U(16.W)) + generalRegFiles(11.U).asSInt.asUInt)
        val __tmp_3497 = (69.U(8.W)).asUInt
        arrayRegFiles(__tmp_3496 + 0.U) := __tmp_3497(7, 0)

        CP := 101.U
      }

      is(99.U) {
        /*
        $10 = (SP + (4: SP))
        $11 = ($5: anvil.PrinterIndex.I16)
        goto .100
        */



        generalRegFiles(10.U) := (SP + 4.U(16.W))


        generalRegFiles(11.U) := (generalRegFiles(5.U).asSInt).asUInt

        CP := 100.U
      }

      is(100.U) {
        /*
        *((($10: MS[anvil.PrinterIndex.I16, U8]) + (12: SP)) + (($11: anvil.PrinterIndex.I16) as SP)) = (70: U8) [unsigned, U8, 1]  // ($10: MS[anvil.PrinterIndex.I16, U8])(($11: anvil.PrinterIndex.I16)) = (70: U8)
        goto .101
        */


        val __tmp_3498 = ((generalRegFiles(10.U) + 12.U(16.W)) + generalRegFiles(11.U).asSInt.asUInt)
        val __tmp_3499 = (70.U(8.W)).asUInt
        arrayRegFiles(__tmp_3498 + 0.U) := __tmp_3499(7, 0)

        CP := 101.U
      }

      is(101.U) {
        /*
        $10 = ($6: U64)
        goto .102
        */



        generalRegFiles(10.U) := generalRegFiles(6.U)

        CP := 102.U
      }

      is(102.U) {
        /*
        $11 = (($10: U64) >>> (4: U64))
        goto .103
        */



        generalRegFiles(11.U) := (((generalRegFiles(10.U)) >> 4.U(64.W)(4,0)))

        CP := 103.U
      }

      is(103.U) {
        /*
        $6 = ($11: U64)
        goto .104
        */



        generalRegFiles(6.U) := generalRegFiles(11.U)

        CP := 104.U
      }

      is(104.U) {
        /*
        $10 = ($5: anvil.PrinterIndex.I16)
        goto .105
        */



        generalRegFiles(10.U) := (generalRegFiles(5.U).asSInt).asUInt

        CP := 105.U
      }

      is(105.U) {
        /*
        $11 = (($10: anvil.PrinterIndex.I16) + (1: anvil.PrinterIndex.I16))
        goto .106
        */



        generalRegFiles(11.U) := ((generalRegFiles(10.U).asSInt + 1.S(8.W))).asUInt

        CP := 106.U
      }

      is(106.U) {
        /*
        $5 = ($11: anvil.PrinterIndex.I16)
        goto .107
        */



        generalRegFiles(5.U) := (generalRegFiles(11.U).asSInt).asUInt

        CP := 107.U
      }

      is(107.U) {
        /*
        $10 = ($7: Z)
        goto .108
        */



        generalRegFiles(10.U) := (generalRegFiles(7.U).asSInt).asUInt

        CP := 108.U
      }

      is(108.U) {
        /*
        $11 = (($10: Z) - (1: Z))
        goto .109
        */



        generalRegFiles(11.U) := ((generalRegFiles(10.U).asSInt - 1.S(64.W))).asUInt

        CP := 109.U
      }

      is(109.U) {
        /*
        $7 = ($11: Z)
        goto .60
        */



        generalRegFiles(7.U) := (generalRegFiles(11.U).asSInt).asUInt

        CP := 60.U
      }

      is(110.U) {
        /*
        decl idx: anvil.PrinterIndex.U @$8
        $10 = ($1: anvil.PrinterIndex.U)
        goto .111
        */



        generalRegFiles(10.U) := generalRegFiles(1.U)

        CP := 111.U
      }

      is(111.U) {
        /*
        $8 = ($10: anvil.PrinterIndex.U)
        goto .112
        */



        generalRegFiles(8.U) := generalRegFiles(10.U)

        CP := 112.U
      }

      is(112.U) {
        /*
        $10 = ($7: Z)
        goto .113
        */



        generalRegFiles(10.U) := (generalRegFiles(7.U).asSInt).asUInt

        CP := 113.U
      }

      is(113.U) {
        /*
        $11 = (($10: Z) > (0: Z))
        goto .114
        */



        generalRegFiles(11.U) := (generalRegFiles(10.U).asSInt > 0.S(64.W)).asUInt

        CP := 114.U
      }

      is(114.U) {
        /*
        if ($11: B) goto .115 else goto .124
        */


        CP := Mux((generalRegFiles(11.U).asUInt) === 1.U, 115.U, 124.U)
      }

      is(115.U) {
        /*
        $10 = ($0: MS[anvil.PrinterIndex.U, U8])
        $11 = ($8: anvil.PrinterIndex.U)
        $12 = ($2: anvil.PrinterIndex.U)
        goto .116
        */



        generalRegFiles(10.U) := generalRegFiles(0.U)


        generalRegFiles(11.U) := generalRegFiles(8.U)


        generalRegFiles(12.U) := generalRegFiles(2.U)

        CP := 116.U
      }

      is(116.U) {
        /*
        $13 = (($11: anvil.PrinterIndex.U) & ($12: anvil.PrinterIndex.U))
        goto .117
        */



        generalRegFiles(13.U) := (generalRegFiles(11.U) & generalRegFiles(12.U))

        CP := 117.U
      }

      is(117.U) {
        /*
        *((($10: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($13: anvil.PrinterIndex.U) as SP)) = (48: U8) [unsigned, U8, 1]  // ($10: MS[anvil.PrinterIndex.U, U8])(($13: anvil.PrinterIndex.U)) = (48: U8)
        goto .118
        */


        val __tmp_3500 = ((generalRegFiles(10.U) + 12.U(16.W)) + generalRegFiles(13.U).asUInt)
        val __tmp_3501 = (48.U(8.W)).asUInt
        arrayRegFiles(__tmp_3500 + 0.U) := __tmp_3501(7, 0)

        CP := 118.U
      }

      is(118.U) {
        /*
        $10 = ($7: Z)
        goto .119
        */



        generalRegFiles(10.U) := (generalRegFiles(7.U).asSInt).asUInt

        CP := 119.U
      }

      is(119.U) {
        /*
        $11 = (($10: Z) - (1: Z))
        goto .120
        */



        generalRegFiles(11.U) := ((generalRegFiles(10.U).asSInt - 1.S(64.W))).asUInt

        CP := 120.U
      }

      is(120.U) {
        /*
        $7 = ($11: Z)
        goto .121
        */



        generalRegFiles(7.U) := (generalRegFiles(11.U).asSInt).asUInt

        CP := 121.U
      }

      is(121.U) {
        /*
        $10 = ($8: anvil.PrinterIndex.U)
        goto .122
        */



        generalRegFiles(10.U) := generalRegFiles(8.U)

        CP := 122.U
      }

      is(122.U) {
        /*
        $11 = (($10: anvil.PrinterIndex.U) + (1: anvil.PrinterIndex.U))
        goto .123
        */



        generalRegFiles(11.U) := (generalRegFiles(10.U) + 1.U(64.W))

        CP := 123.U
      }

      is(123.U) {
        /*
        $8 = ($11: anvil.PrinterIndex.U)
        goto .112
        */



        generalRegFiles(8.U) := generalRegFiles(11.U)

        CP := 112.U
      }

      is(124.U) {
        /*
        decl j: anvil.PrinterIndex.I16 @$9
        $10 = ($5: anvil.PrinterIndex.I16)
        undecl i: anvil.PrinterIndex.I16 @$5
        goto .125
        */



        generalRegFiles(10.U) := (generalRegFiles(5.U).asSInt).asUInt

        CP := 125.U
      }

      is(125.U) {
        /*
        $11 = (($10: anvil.PrinterIndex.I16) - (1: anvil.PrinterIndex.I16))
        goto .126
        */



        generalRegFiles(11.U) := ((generalRegFiles(10.U).asSInt - 1.S(8.W))).asUInt

        CP := 126.U
      }

      is(126.U) {
        /*
        $9 = ($11: anvil.PrinterIndex.I16)
        goto .127
        */



        generalRegFiles(9.U) := (generalRegFiles(11.U).asSInt).asUInt

        CP := 127.U
      }

      is(127.U) {
        /*
        $10 = ($9: anvil.PrinterIndex.I16)
        goto .128
        */



        generalRegFiles(10.U) := (generalRegFiles(9.U).asSInt).asUInt

        CP := 128.U
      }

      is(128.U) {
        /*
        $11 = (($10: anvil.PrinterIndex.I16) >= (0: anvil.PrinterIndex.I16))
        goto .129
        */



        generalRegFiles(11.U) := (generalRegFiles(10.U).asSInt >= 0.S(8.W)).asUInt

        CP := 129.U
      }

      is(129.U) {
        /*
        if ($11: B) goto .130 else goto .141
        */


        CP := Mux((generalRegFiles(11.U).asUInt) === 1.U, 130.U, 141.U)
      }

      is(130.U) {
        /*
        $10 = ($0: MS[anvil.PrinterIndex.U, U8])
        $11 = ($8: anvil.PrinterIndex.U)
        $12 = ($2: anvil.PrinterIndex.U)
        goto .131
        */



        generalRegFiles(10.U) := generalRegFiles(0.U)


        generalRegFiles(11.U) := generalRegFiles(8.U)


        generalRegFiles(12.U) := generalRegFiles(2.U)

        CP := 131.U
      }

      is(131.U) {
        /*
        $13 = (($11: anvil.PrinterIndex.U) & ($12: anvil.PrinterIndex.U))
        goto .132
        */



        generalRegFiles(13.U) := (generalRegFiles(11.U) & generalRegFiles(12.U))

        CP := 132.U
      }

      is(132.U) {
        /*
        $14 = (SP + (4: SP))
        $15 = ($9: anvil.PrinterIndex.I16)
        goto .133
        */



        generalRegFiles(14.U) := (SP + 4.U(16.W))


        generalRegFiles(15.U) := (generalRegFiles(9.U).asSInt).asUInt

        CP := 133.U
      }

      is(133.U) {
        /*
        $16 = *((($14: MS[anvil.PrinterIndex.I16, U8]) + (12: SP)) + (($15: anvil.PrinterIndex.I16) as SP)) [unsigned, U8, 1]  // $16 = ($14: MS[anvil.PrinterIndex.I16, U8])(($15: anvil.PrinterIndex.I16))
        goto .134
        */


        val __tmp_3502 = (((generalRegFiles(14.U) + 12.U(16.W)) + generalRegFiles(15.U).asSInt.asUInt)).asUInt
        generalRegFiles(16.U) := Cat(
          arrayRegFiles(__tmp_3502 + 0.U)
        ).asUInt

        CP := 134.U
      }

      is(134.U) {
        /*
        *((($10: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($13: anvil.PrinterIndex.U) as SP)) = ($16: U8) [unsigned, U8, 1]  // ($10: MS[anvil.PrinterIndex.U, U8])(($13: anvil.PrinterIndex.U)) = ($16: U8)
        goto .135
        */


        val __tmp_3503 = ((generalRegFiles(10.U) + 12.U(16.W)) + generalRegFiles(13.U).asUInt)
        val __tmp_3504 = (generalRegFiles(16.U)).asUInt
        arrayRegFiles(__tmp_3503 + 0.U) := __tmp_3504(7, 0)

        CP := 135.U
      }

      is(135.U) {
        /*
        $10 = ($9: anvil.PrinterIndex.I16)
        goto .136
        */



        generalRegFiles(10.U) := (generalRegFiles(9.U).asSInt).asUInt

        CP := 136.U
      }

      is(136.U) {
        /*
        $11 = (($10: anvil.PrinterIndex.I16) - (1: anvil.PrinterIndex.I16))
        goto .137
        */



        generalRegFiles(11.U) := ((generalRegFiles(10.U).asSInt - 1.S(8.W))).asUInt

        CP := 137.U
      }

      is(137.U) {
        /*
        $9 = ($11: anvil.PrinterIndex.I16)
        goto .138
        */



        generalRegFiles(9.U) := (generalRegFiles(11.U).asSInt).asUInt

        CP := 138.U
      }

      is(138.U) {
        /*
        $10 = ($8: anvil.PrinterIndex.U)
        goto .139
        */



        generalRegFiles(10.U) := generalRegFiles(8.U)

        CP := 139.U
      }

      is(139.U) {
        /*
        $11 = (($10: anvil.PrinterIndex.U) + (1: anvil.PrinterIndex.U))
        goto .140
        */



        generalRegFiles(11.U) := (generalRegFiles(10.U) + 1.U(64.W))

        CP := 140.U
      }

      is(140.U) {
        /*
        $8 = ($11: anvil.PrinterIndex.U)
        goto .127
        */



        generalRegFiles(8.U) := generalRegFiles(11.U)

        CP := 127.U
      }

      is(141.U) {
        /*
        $10 = ($4: Z)
        goto .142
        */



        generalRegFiles(10.U) := (generalRegFiles(4.U).asSInt).asUInt

        CP := 142.U
      }

      is(142.U) {
        /*
        $11 = (($10: Z) as U64)
        goto .143
        */



        generalRegFiles(11.U) := generalRegFiles(10.U).asSInt.asUInt

        CP := 143.U
      }

      is(143.U) {
        /*
        **(SP + (2: SP)) = ($11: U64) [unsigned, U64, 8]  // $res = ($11: U64)
        goto $ret@0
        */


        val __tmp_3505 = Cat(
          arrayRegFiles((SP + 2.U(16.W)) + 1.U),
          arrayRegFiles((SP + 2.U(16.W)) + 0.U)
        )
        val __tmp_3506 = (generalRegFiles(11.U)).asUInt
        arrayRegFiles(__tmp_3505 + 0.U) := __tmp_3506(7, 0)
        arrayRegFiles(__tmp_3505 + 1.U) := __tmp_3506(15, 8)
        arrayRegFiles(__tmp_3505 + 2.U) := __tmp_3506(23, 16)
        arrayRegFiles(__tmp_3505 + 3.U) := __tmp_3506(31, 24)
        arrayRegFiles(__tmp_3505 + 4.U) := __tmp_3506(39, 32)
        arrayRegFiles(__tmp_3505 + 5.U) := __tmp_3506(47, 40)
        arrayRegFiles(__tmp_3505 + 6.U) := __tmp_3506(55, 48)
        arrayRegFiles(__tmp_3505 + 7.U) := __tmp_3506(63, 56)

        CP := Cat(
          arrayRegFiles(SP + 0.U + 1.U),
          arrayRegFiles(SP + 0.U + 0.U)
        )

      }

    }

}


