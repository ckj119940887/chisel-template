package GeneralRegFileToBRAM
import chisel3._
import chisel3.util._
import chisel3.experimental._



class InstanceofTest (val C_S_AXI_DATA_WIDTH:  Int = 32,
               val C_S_AXI_ADDR_WIDTH:  Int = 32,
               val ARRAY_REG_WIDTH:     Int = 8,
               val ARRAY_REG_DEPTH:     Int = 128,
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
        SP = 22
        DP = 0
        *(9: U32) = (886747591: U32) [unsigned, U32, 4]  // $display.$type (MS[anvil.PrinterIndex.U, U8]: 0x34DAB1C7)
        *(13: SP) = (2: Z) [signed, Z, 8]  // $display.size
        *(22: CP) = (0: CP) [unsigned, CP, 2]  // $ret
        goto .4
        */


        SP := 22.U(8.W)

        DP := 0.U(64.W)

        val __tmp_2561 = 9.U(32.W)
        val __tmp_2562 = (886747591.U(32.W)).asUInt
        arrayRegFiles(__tmp_2561 + 0.U) := __tmp_2562(7, 0)
        arrayRegFiles(__tmp_2561 + 1.U) := __tmp_2562(15, 8)
        arrayRegFiles(__tmp_2561 + 2.U) := __tmp_2562(23, 16)
        arrayRegFiles(__tmp_2561 + 3.U) := __tmp_2562(31, 24)

        val __tmp_2563 = 13.U(8.W)
        val __tmp_2564 = (2.S(64.W)).asUInt
        arrayRegFiles(__tmp_2563 + 0.U) := __tmp_2564(7, 0)
        arrayRegFiles(__tmp_2563 + 1.U) := __tmp_2564(15, 8)
        arrayRegFiles(__tmp_2563 + 2.U) := __tmp_2564(23, 16)
        arrayRegFiles(__tmp_2563 + 3.U) := __tmp_2564(31, 24)
        arrayRegFiles(__tmp_2563 + 4.U) := __tmp_2564(39, 32)
        arrayRegFiles(__tmp_2563 + 5.U) := __tmp_2564(47, 40)
        arrayRegFiles(__tmp_2563 + 6.U) := __tmp_2564(55, 48)
        arrayRegFiles(__tmp_2563 + 7.U) := __tmp_2564(63, 56)

        val __tmp_2565 = 22.U(16.W)
        val __tmp_2566 = (0.U(16.W)).asUInt
        arrayRegFiles(__tmp_2565 + 0.U) := __tmp_2566(7, 0)
        arrayRegFiles(__tmp_2565 + 1.U) := __tmp_2566(15, 8)

        CP := 4.U
      }

      is(4.U) {
        /*
        $0 = *(0: SP) [signed, Z, 8]  // $0 = $testNum
        goto .5
        */


        val __tmp_2567 = (0.U(8.W)).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_2567 + 7.U),
          arrayRegFiles(__tmp_2567 + 6.U),
          arrayRegFiles(__tmp_2567 + 5.U),
          arrayRegFiles(__tmp_2567 + 4.U),
          arrayRegFiles(__tmp_2567 + 3.U),
          arrayRegFiles(__tmp_2567 + 2.U),
          arrayRegFiles(__tmp_2567 + 1.U),
          arrayRegFiles(__tmp_2567 + 0.U)
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
        *SP = (9: CP) [unsigned, CP, 2]  // $ret@0 = 1337
        goto .8
        */


        val __tmp_2568 = SP
        val __tmp_2569 = (9.U(16.W)).asUInt
        arrayRegFiles(__tmp_2568 + 0.U) := __tmp_2569(7, 0)
        arrayRegFiles(__tmp_2568 + 1.U) := __tmp_2569(15, 8)

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
        alloc $new@[16,22].4DAFA9EE: B [@2, 12]
        $2 = (SP + (2: SP))
        *(SP + (2: SP)) = (2070117317: U32) [unsigned, U32, 4]  // sha3 type signature of B: 0x7B637BC5
        goto .13
        */



        generalRegFiles(2.U) := (SP + 2.U(8.W))

        val __tmp_2570 = (SP + 2.U(8.W))
        val __tmp_2571 = (2070117317.U(32.W)).asUInt
        arrayRegFiles(__tmp_2570 + 0.U) := __tmp_2571(7, 0)
        arrayRegFiles(__tmp_2570 + 1.U) := __tmp_2571(15, 8)
        arrayRegFiles(__tmp_2570 + 2.U) := __tmp_2571(23, 16)
        arrayRegFiles(__tmp_2570 + 3.U) := __tmp_2571(31, 24)

        CP := 13.U
      }

      is(13.U) {
        /*
        *(($2: B) + (4: SP)) = (3: Z) [signed, Z, 8]  // ($2: B).x = (3: Z)
        alloc instanceof$res@[16,11].0DA3CA64: Z [@14, 8]
        goto .14
        */


        val __tmp_2572 = (generalRegFiles(2.U) + 4.U(8.W))
        val __tmp_2573 = (3.S(64.W)).asUInt
        arrayRegFiles(__tmp_2572 + 0.U) := __tmp_2573(7, 0)
        arrayRegFiles(__tmp_2572 + 1.U) := __tmp_2573(15, 8)
        arrayRegFiles(__tmp_2572 + 2.U) := __tmp_2573(23, 16)
        arrayRegFiles(__tmp_2572 + 3.U) := __tmp_2573(31, 24)
        arrayRegFiles(__tmp_2572 + 4.U) := __tmp_2573(39, 32)
        arrayRegFiles(__tmp_2572 + 5.U) := __tmp_2573(47, 40)
        arrayRegFiles(__tmp_2572 + 6.U) := __tmp_2573(55, 48)
        arrayRegFiles(__tmp_2572 + 7.U) := __tmp_2573(63, 56)

        CP := 14.U
      }

      is(14.U) {
        /*
        SP = SP + 22
        goto .15
        */


        SP := SP + 22.U

        CP := 15.U
      }

      is(15.U) {
        /*
        *SP = (17: CP) [unsigned, CP, 2]  // $ret@0 = 1339
        *(SP + (2: SP)) = (SP - (8: SP)) [unsigned, SP, 1]  // $res@2 = -8
        $13 = ($2: B)
        goto .16
        */


        val __tmp_2574 = SP
        val __tmp_2575 = (17.U(16.W)).asUInt
        arrayRegFiles(__tmp_2574 + 0.U) := __tmp_2575(7, 0)
        arrayRegFiles(__tmp_2574 + 1.U) := __tmp_2575(15, 8)

        val __tmp_2576 = (SP + 2.U(8.W))
        val __tmp_2577 = ((SP - 8.U(8.W))).asUInt
        arrayRegFiles(__tmp_2576 + 0.U) := __tmp_2577(7, 0)


        generalRegFiles(13.U) := generalRegFiles(2.U)

        CP := 16.U
      }

      is(16.U) {
        /*
        decl $ret: CP [@0, 2], $res: SP [@2, 9], a: A @$0, a: A [@11, 12]
        $0 = ($13: A)
        goto .29
        */



        generalRegFiles(0.U) := generalRegFiles(13.U)

        CP := 29.U
      }

      is(17.U) {
        /*
        $3 = **(SP + (2: SP)) [signed, Z, 8]  // $3 = $res
        undecl a: A [@11, 12], a: A @$0, $res: SP [@2, 9], $ret: CP [@0, 2]
        goto .18
        */


        val __tmp_2578 = (Cat(
          arrayRegFiles((SP + 2.U(8.W)) + 0.U)
        )).asUInt
        generalRegFiles(3.U) := Cat(
          arrayRegFiles(__tmp_2578 + 7.U),
          arrayRegFiles(__tmp_2578 + 6.U),
          arrayRegFiles(__tmp_2578 + 5.U),
          arrayRegFiles(__tmp_2578 + 4.U),
          arrayRegFiles(__tmp_2578 + 3.U),
          arrayRegFiles(__tmp_2578 + 2.U),
          arrayRegFiles(__tmp_2578 + 1.U),
          arrayRegFiles(__tmp_2578 + 0.U)
        ).asSInt.pad(GENERAL_REG_WIDTH).asUInt

        CP := 18.U
      }

      is(18.U) {
        /*
        SP = SP - 22
        goto .19
        */


        SP := SP - 22.U

        CP := 19.U
      }

      is(19.U) {
        /*
        unalloc $new@[16,22].4DAFA9EE: B [@2, 12]
        goto .20
        */


        CP := 20.U
      }

      is(20.U) {
        /*
        alloc printS64$res@[16,11].0DA3CA64: U64 [@2, 8]
        goto .21
        */


        CP := 21.U
      }

      is(21.U) {
        /*
        SP = SP + 22
        goto .22
        */


        SP := SP + 22.U

        CP := 22.U
      }

      is(22.U) {
        /*
        *SP = (24: CP) [unsigned, CP, 2]  // $ret@0 = 1341
        *(SP + (2: SP)) = (SP - (20: SP)) [unsigned, SP, 1]  // $res@2 = -20
        $76 = (8: SP)
        $77 = DP
        $78 = (1: anvil.PrinterIndex.U)
        $79 = (($3: Z) as S64)
        goto .23
        */


        val __tmp_2579 = SP
        val __tmp_2580 = (24.U(16.W)).asUInt
        arrayRegFiles(__tmp_2579 + 0.U) := __tmp_2580(7, 0)
        arrayRegFiles(__tmp_2579 + 1.U) := __tmp_2580(15, 8)

        val __tmp_2581 = (SP + 2.U(8.W))
        val __tmp_2582 = ((SP - 20.U(8.W))).asUInt
        arrayRegFiles(__tmp_2581 + 0.U) := __tmp_2582(7, 0)


        generalRegFiles(76.U) := 8.U(8.W)


        generalRegFiles(77.U) := DP


        generalRegFiles(78.U) := 1.U(64.W)


        generalRegFiles(79.U) := (generalRegFiles(3.U).asSInt.asSInt).asUInt

        CP := 23.U
      }

      is(23.U) {
        /*
        decl $ret: CP [@0, 2], $res: SP [@2, 1], buffer: MS[anvil.PrinterIndex.U, U8] @$0, index: anvil.PrinterIndex.U @$1, mask: anvil.PrinterIndex.U @$2, n: S64 @$3
        $0 = ($76: MS[anvil.PrinterIndex.U, U8])
        $1 = ($77: anvil.PrinterIndex.U)
        $2 = ($78: anvil.PrinterIndex.U)
        $3 = ($79: S64)
        goto .43
        */



        generalRegFiles(0.U) := generalRegFiles(76.U)


        generalRegFiles(1.U) := generalRegFiles(77.U)


        generalRegFiles(2.U) := generalRegFiles(78.U)


        generalRegFiles(3.U) := (generalRegFiles(79.U).asSInt).asUInt

        CP := 43.U
      }

      is(24.U) {
        /*
        $4 = **(SP + (2: SP)) [unsigned, U64, 8]  // $4 = $res
        undecl n: S64 @$3, mask: anvil.PrinterIndex.U @$2, index: anvil.PrinterIndex.U @$1, buffer: MS[anvil.PrinterIndex.U, U8] @$0, $res: SP [@2, 1], $ret: CP [@0, 2]
        goto .25
        */


        val __tmp_2583 = (Cat(
          arrayRegFiles((SP + 2.U(8.W)) + 0.U)
        )).asUInt
        generalRegFiles(4.U) := Cat(
          arrayRegFiles(__tmp_2583 + 7.U),
          arrayRegFiles(__tmp_2583 + 6.U),
          arrayRegFiles(__tmp_2583 + 5.U),
          arrayRegFiles(__tmp_2583 + 4.U),
          arrayRegFiles(__tmp_2583 + 3.U),
          arrayRegFiles(__tmp_2583 + 2.U),
          arrayRegFiles(__tmp_2583 + 1.U),
          arrayRegFiles(__tmp_2583 + 0.U)
        ).asUInt

        CP := 25.U
      }

      is(25.U) {
        /*
        SP = SP - 22
        goto .26
        */


        SP := SP - 22.U

        CP := 26.U
      }

      is(26.U) {
        /*
        DP = DP + (($4: U64) as DP)
        goto .27
        */


        DP := DP + generalRegFiles(4.U).asUInt

        CP := 27.U
      }

      is(27.U) {
        /*
        *(((8: SP) + (12: SP)) + ((DP & (1: DP)) as SP)) = (10: U8) [unsigned, U8, 1]  // $display((DP & (1: DP))) = (10: U8)
        goto .28
        */


        val __tmp_2584 = ((8.U(8.W) + 12.U(8.W)) + (DP & 1.U(64.W)).asUInt)
        val __tmp_2585 = (10.U(8.W)).asUInt
        arrayRegFiles(__tmp_2584 + 0.U) := __tmp_2585(7, 0)

        CP := 28.U
      }

      is(28.U) {
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

      is(29.U) {
        /*
        $1 = ($0: A)
        goto .30
        */



        generalRegFiles(1.U) := generalRegFiles(0.U)

        CP := 30.U
      }

      is(30.U) {
        /*
        switch (*($1: A))
          (2070117317: U32): goto 31
          default: goto 32
        */


        val __tmp_2586 = Cat(
             arrayRegFiles(generalRegFiles(1.U) + 3.U),
             arrayRegFiles(generalRegFiles(1.U) + 2.U),
             arrayRegFiles(generalRegFiles(1.U) + 1.U),
             arrayRegFiles(generalRegFiles(1.U) + 0.U)
           )
        CP := 32.U
        switch(__tmp_2586) {

          is(2070117317.U(32.W)) {
            CP := 31.U
          }

        }

      }

      is(31.U) {
        /*
        $2 = true
        goto .33
        */



        generalRegFiles(2.U) := 1.U

        CP := 33.U
      }

      is(32.U) {
        /*
        $2 = false
        goto .33
        */



        generalRegFiles(2.U) := 0.U

        CP := 33.U
      }

      is(33.U) {
        /*
        if ($2: B) goto .34 else goto .42
        */


        CP := Mux((generalRegFiles(2.U).asUInt) === 1.U, 34.U, 42.U)
      }

      is(34.U) {
        /*
        $1 = ($0: A)
        goto .35
        */



        generalRegFiles(1.U) := generalRegFiles(0.U)

        CP := 35.U
      }

      is(35.U) {
        /*
        switch (*($1: A))
          (2070117317: U32): goto 36
          default: goto 37
        */


        val __tmp_2587 = Cat(
             arrayRegFiles(generalRegFiles(1.U) + 3.U),
             arrayRegFiles(generalRegFiles(1.U) + 2.U),
             arrayRegFiles(generalRegFiles(1.U) + 1.U),
             arrayRegFiles(generalRegFiles(1.U) + 0.U)
           )
        CP := 37.U
        switch(__tmp_2587) {

          is(2070117317.U(32.W)) {
            CP := 36.U
          }

        }

      }

      is(36.U) {
        /*
        $2 = ($1: A)
        goto .40
        */



        generalRegFiles(2.U) := generalRegFiles(1.U)

        CP := 40.U
      }

      is(37.U) {
        /*
        *(((8: SP) + (12: SP)) + ((DP & (1: DP)) as SP)) = (67: U8) [unsigned, U8, 1]  // $display((DP & (1: DP))) = (67: U8)
        *(((8: SP) + (12: SP)) + (((DP + (1: DP)) & (1: DP)) as SP)) = (97: U8) [unsigned, U8, 1]  // $display(((DP + (1: DP)) & (1: DP))) = (97: U8)
        *(((8: SP) + (12: SP)) + (((DP + (2: DP)) & (1: DP)) as SP)) = (110: U8) [unsigned, U8, 1]  // $display(((DP + (2: DP)) & (1: DP))) = (110: U8)
        *(((8: SP) + (12: SP)) + (((DP + (3: DP)) & (1: DP)) as SP)) = (110: U8) [unsigned, U8, 1]  // $display(((DP + (3: DP)) & (1: DP))) = (110: U8)
        *(((8: SP) + (12: SP)) + (((DP + (4: DP)) & (1: DP)) as SP)) = (111: U8) [unsigned, U8, 1]  // $display(((DP + (4: DP)) & (1: DP))) = (111: U8)
        *(((8: SP) + (12: SP)) + (((DP + (5: DP)) & (1: DP)) as SP)) = (116: U8) [unsigned, U8, 1]  // $display(((DP + (5: DP)) & (1: DP))) = (116: U8)
        *(((8: SP) + (12: SP)) + (((DP + (6: DP)) & (1: DP)) as SP)) = (32: U8) [unsigned, U8, 1]  // $display(((DP + (6: DP)) & (1: DP))) = (32: U8)
        *(((8: SP) + (12: SP)) + (((DP + (7: DP)) & (1: DP)) as SP)) = (99: U8) [unsigned, U8, 1]  // $display(((DP + (7: DP)) & (1: DP))) = (99: U8)
        *(((8: SP) + (12: SP)) + (((DP + (8: DP)) & (1: DP)) as SP)) = (97: U8) [unsigned, U8, 1]  // $display(((DP + (8: DP)) & (1: DP))) = (97: U8)
        *(((8: SP) + (12: SP)) + (((DP + (9: DP)) & (1: DP)) as SP)) = (115: U8) [unsigned, U8, 1]  // $display(((DP + (9: DP)) & (1: DP))) = (115: U8)
        *(((8: SP) + (12: SP)) + (((DP + (10: DP)) & (1: DP)) as SP)) = (116: U8) [unsigned, U8, 1]  // $display(((DP + (10: DP)) & (1: DP))) = (116: U8)
        *(((8: SP) + (12: SP)) + (((DP + (11: DP)) & (1: DP)) as SP)) = (32: U8) [unsigned, U8, 1]  // $display(((DP + (11: DP)) & (1: DP))) = (32: U8)
        *(((8: SP) + (12: SP)) + (((DP + (12: DP)) & (1: DP)) as SP)) = (116: U8) [unsigned, U8, 1]  // $display(((DP + (12: DP)) & (1: DP))) = (116: U8)
        *(((8: SP) + (12: SP)) + (((DP + (13: DP)) & (1: DP)) as SP)) = (111: U8) [unsigned, U8, 1]  // $display(((DP + (13: DP)) & (1: DP))) = (111: U8)
        *(((8: SP) + (12: SP)) + (((DP + (14: DP)) & (1: DP)) as SP)) = (32: U8) [unsigned, U8, 1]  // $display(((DP + (14: DP)) & (1: DP))) = (32: U8)
        *(((8: SP) + (12: SP)) + (((DP + (15: DP)) & (1: DP)) as SP)) = (66: U8) [unsigned, U8, 1]  // $display(((DP + (15: DP)) & (1: DP))) = (66: U8)
        *(((8: SP) + (12: SP)) + (((DP + (16: DP)) & (1: DP)) as SP)) = (10: U8) [unsigned, U8, 1]  // $display(((DP + (16: DP)) & (1: DP))) = (10: U8)
        goto .38
        */


        val __tmp_2588 = ((8.U(8.W) + 12.U(8.W)) + (DP & 1.U(64.W)).asUInt)
        val __tmp_2589 = (67.U(8.W)).asUInt
        arrayRegFiles(__tmp_2588 + 0.U) := __tmp_2589(7, 0)

        val __tmp_2590 = ((8.U(8.W) + 12.U(8.W)) + ((DP + 1.U(64.W)) & 1.U(64.W)).asUInt)
        val __tmp_2591 = (97.U(8.W)).asUInt
        arrayRegFiles(__tmp_2590 + 0.U) := __tmp_2591(7, 0)

        val __tmp_2592 = ((8.U(8.W) + 12.U(8.W)) + ((DP + 2.U(64.W)) & 1.U(64.W)).asUInt)
        val __tmp_2593 = (110.U(8.W)).asUInt
        arrayRegFiles(__tmp_2592 + 0.U) := __tmp_2593(7, 0)

        val __tmp_2594 = ((8.U(8.W) + 12.U(8.W)) + ((DP + 3.U(64.W)) & 1.U(64.W)).asUInt)
        val __tmp_2595 = (110.U(8.W)).asUInt
        arrayRegFiles(__tmp_2594 + 0.U) := __tmp_2595(7, 0)

        val __tmp_2596 = ((8.U(8.W) + 12.U(8.W)) + ((DP + 4.U(64.W)) & 1.U(64.W)).asUInt)
        val __tmp_2597 = (111.U(8.W)).asUInt
        arrayRegFiles(__tmp_2596 + 0.U) := __tmp_2597(7, 0)

        val __tmp_2598 = ((8.U(8.W) + 12.U(8.W)) + ((DP + 5.U(64.W)) & 1.U(64.W)).asUInt)
        val __tmp_2599 = (116.U(8.W)).asUInt
        arrayRegFiles(__tmp_2598 + 0.U) := __tmp_2599(7, 0)

        val __tmp_2600 = ((8.U(8.W) + 12.U(8.W)) + ((DP + 6.U(64.W)) & 1.U(64.W)).asUInt)
        val __tmp_2601 = (32.U(8.W)).asUInt
        arrayRegFiles(__tmp_2600 + 0.U) := __tmp_2601(7, 0)

        val __tmp_2602 = ((8.U(8.W) + 12.U(8.W)) + ((DP + 7.U(64.W)) & 1.U(64.W)).asUInt)
        val __tmp_2603 = (99.U(8.W)).asUInt
        arrayRegFiles(__tmp_2602 + 0.U) := __tmp_2603(7, 0)

        val __tmp_2604 = ((8.U(8.W) + 12.U(8.W)) + ((DP + 8.U(64.W)) & 1.U(64.W)).asUInt)
        val __tmp_2605 = (97.U(8.W)).asUInt
        arrayRegFiles(__tmp_2604 + 0.U) := __tmp_2605(7, 0)

        val __tmp_2606 = ((8.U(8.W) + 12.U(8.W)) + ((DP + 9.U(64.W)) & 1.U(64.W)).asUInt)
        val __tmp_2607 = (115.U(8.W)).asUInt
        arrayRegFiles(__tmp_2606 + 0.U) := __tmp_2607(7, 0)

        val __tmp_2608 = ((8.U(8.W) + 12.U(8.W)) + ((DP + 10.U(64.W)) & 1.U(64.W)).asUInt)
        val __tmp_2609 = (116.U(8.W)).asUInt
        arrayRegFiles(__tmp_2608 + 0.U) := __tmp_2609(7, 0)

        val __tmp_2610 = ((8.U(8.W) + 12.U(8.W)) + ((DP + 11.U(64.W)) & 1.U(64.W)).asUInt)
        val __tmp_2611 = (32.U(8.W)).asUInt
        arrayRegFiles(__tmp_2610 + 0.U) := __tmp_2611(7, 0)

        val __tmp_2612 = ((8.U(8.W) + 12.U(8.W)) + ((DP + 12.U(64.W)) & 1.U(64.W)).asUInt)
        val __tmp_2613 = (116.U(8.W)).asUInt
        arrayRegFiles(__tmp_2612 + 0.U) := __tmp_2613(7, 0)

        val __tmp_2614 = ((8.U(8.W) + 12.U(8.W)) + ((DP + 13.U(64.W)) & 1.U(64.W)).asUInt)
        val __tmp_2615 = (111.U(8.W)).asUInt
        arrayRegFiles(__tmp_2614 + 0.U) := __tmp_2615(7, 0)

        val __tmp_2616 = ((8.U(8.W) + 12.U(8.W)) + ((DP + 14.U(64.W)) & 1.U(64.W)).asUInt)
        val __tmp_2617 = (32.U(8.W)).asUInt
        arrayRegFiles(__tmp_2616 + 0.U) := __tmp_2617(7, 0)

        val __tmp_2618 = ((8.U(8.W) + 12.U(8.W)) + ((DP + 15.U(64.W)) & 1.U(64.W)).asUInt)
        val __tmp_2619 = (66.U(8.W)).asUInt
        arrayRegFiles(__tmp_2618 + 0.U) := __tmp_2619(7, 0)

        val __tmp_2620 = ((8.U(8.W) + 12.U(8.W)) + ((DP + 16.U(64.W)) & 1.U(64.W)).asUInt)
        val __tmp_2621 = (10.U(8.W)).asUInt
        arrayRegFiles(__tmp_2620 + 0.U) := __tmp_2621(7, 0)

        CP := 38.U
      }

      is(38.U) {
        /*
        DP = DP + 17
        goto .1
        */


        DP := DP + 17.U

        CP := 1.U
      }

      is(40.U) {
        /*
        $3 = *(($2: B) + (4: SP)) [signed, Z, 8]  // $3 = ($2: B).x
        goto .41
        */


        val __tmp_2622 = ((generalRegFiles(2.U) + 4.U(8.W))).asUInt
        generalRegFiles(3.U) := Cat(
          arrayRegFiles(__tmp_2622 + 7.U),
          arrayRegFiles(__tmp_2622 + 6.U),
          arrayRegFiles(__tmp_2622 + 5.U),
          arrayRegFiles(__tmp_2622 + 4.U),
          arrayRegFiles(__tmp_2622 + 3.U),
          arrayRegFiles(__tmp_2622 + 2.U),
          arrayRegFiles(__tmp_2622 + 1.U),
          arrayRegFiles(__tmp_2622 + 0.U)
        ).asSInt.pad(GENERAL_REG_WIDTH).asUInt

        CP := 41.U
      }

      is(41.U) {
        /*
        **(SP + (2: SP)) = ($3: Z) [signed, Z, 8]  // $res = ($3: Z)
        goto $ret@0
        */


        val __tmp_2623 = Cat(
          arrayRegFiles((SP + 2.U(8.W)) + 0.U)
        )
        val __tmp_2624 = (generalRegFiles(3.U).asSInt).asUInt
        arrayRegFiles(__tmp_2623 + 0.U) := __tmp_2624(7, 0)
        arrayRegFiles(__tmp_2623 + 1.U) := __tmp_2624(15, 8)
        arrayRegFiles(__tmp_2623 + 2.U) := __tmp_2624(23, 16)
        arrayRegFiles(__tmp_2623 + 3.U) := __tmp_2624(31, 24)
        arrayRegFiles(__tmp_2623 + 4.U) := __tmp_2624(39, 32)
        arrayRegFiles(__tmp_2623 + 5.U) := __tmp_2624(47, 40)
        arrayRegFiles(__tmp_2623 + 6.U) := __tmp_2624(55, 48)
        arrayRegFiles(__tmp_2623 + 7.U) := __tmp_2624(63, 56)

        CP := Cat(
          arrayRegFiles(SP + 0.U + 1.U),
          arrayRegFiles(SP + 0.U + 0.U)
        )

      }

      is(42.U) {
        /*
        **(SP + (2: SP)) = (0: Z) [signed, Z, 8]  // $res = (0: Z)
        goto $ret@0
        */


        val __tmp_2625 = Cat(
          arrayRegFiles((SP + 2.U(8.W)) + 0.U)
        )
        val __tmp_2626 = (0.S(64.W)).asUInt
        arrayRegFiles(__tmp_2625 + 0.U) := __tmp_2626(7, 0)
        arrayRegFiles(__tmp_2625 + 1.U) := __tmp_2626(15, 8)
        arrayRegFiles(__tmp_2625 + 2.U) := __tmp_2626(23, 16)
        arrayRegFiles(__tmp_2625 + 3.U) := __tmp_2626(31, 24)
        arrayRegFiles(__tmp_2625 + 4.U) := __tmp_2626(39, 32)
        arrayRegFiles(__tmp_2625 + 5.U) := __tmp_2626(47, 40)
        arrayRegFiles(__tmp_2625 + 6.U) := __tmp_2626(55, 48)
        arrayRegFiles(__tmp_2625 + 7.U) := __tmp_2626(63, 56)

        CP := Cat(
          arrayRegFiles(SP + 0.U + 1.U),
          arrayRegFiles(SP + 0.U + 0.U)
        )

      }

      is(43.U) {
        /*
        $10 = (($3: S64) ≡ (-9223372036854775808: S64))
        goto .44
        */



        generalRegFiles(10.U) := (generalRegFiles(3.U).asSInt === BigInt("-9223372036854775808").S(64.W)).asUInt

        CP := 44.U
      }

      is(44.U) {
        /*
        if ($10: B) goto .45 else goto .105
        */


        CP := Mux((generalRegFiles(10.U).asUInt) === 1.U, 45.U, 105.U)
      }

      is(45.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $12 = (($1: anvil.PrinterIndex.U) & ($2: anvil.PrinterIndex.U))
        goto .46
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(12.U) := (generalRegFiles(1.U) & generalRegFiles(2.U))

        CP := 46.U
      }

      is(46.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($12: anvil.PrinterIndex.U) as SP)) = (45: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($12: anvil.PrinterIndex.U)) = (45: U8)
        goto .47
        */


        val __tmp_2627 = ((generalRegFiles(11.U) + 12.U(8.W)) + generalRegFiles(12.U).asUInt)
        val __tmp_2628 = (45.U(8.W)).asUInt
        arrayRegFiles(__tmp_2627 + 0.U) := __tmp_2628(7, 0)

        CP := 47.U
      }

      is(47.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $13 = (($1: anvil.PrinterIndex.U) + (1: anvil.PrinterIndex.U))
        goto .48
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(13.U) := (generalRegFiles(1.U) + 1.U(64.W))

        CP := 48.U
      }

      is(48.U) {
        /*
        $14 = (($13: anvil.PrinterIndex.U) & ($2: anvil.PrinterIndex.U))
        goto .49
        */



        generalRegFiles(14.U) := (generalRegFiles(13.U) & generalRegFiles(2.U))

        CP := 49.U
      }

      is(49.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($14: anvil.PrinterIndex.U) as SP)) = (57: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($14: anvil.PrinterIndex.U)) = (57: U8)
        goto .50
        */


        val __tmp_2629 = ((generalRegFiles(11.U) + 12.U(8.W)) + generalRegFiles(14.U).asUInt)
        val __tmp_2630 = (57.U(8.W)).asUInt
        arrayRegFiles(__tmp_2629 + 0.U) := __tmp_2630(7, 0)

        CP := 50.U
      }

      is(50.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $13 = (($1: anvil.PrinterIndex.U) + (2: anvil.PrinterIndex.U))
        goto .51
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(13.U) := (generalRegFiles(1.U) + 2.U(64.W))

        CP := 51.U
      }

      is(51.U) {
        /*
        $14 = (($13: anvil.PrinterIndex.U) & ($2: anvil.PrinterIndex.U))
        goto .52
        */



        generalRegFiles(14.U) := (generalRegFiles(13.U) & generalRegFiles(2.U))

        CP := 52.U
      }

      is(52.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($14: anvil.PrinterIndex.U) as SP)) = (50: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($14: anvil.PrinterIndex.U)) = (50: U8)
        goto .53
        */


        val __tmp_2631 = ((generalRegFiles(11.U) + 12.U(8.W)) + generalRegFiles(14.U).asUInt)
        val __tmp_2632 = (50.U(8.W)).asUInt
        arrayRegFiles(__tmp_2631 + 0.U) := __tmp_2632(7, 0)

        CP := 53.U
      }

      is(53.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $13 = (($1: anvil.PrinterIndex.U) + (3: anvil.PrinterIndex.U))
        goto .54
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(13.U) := (generalRegFiles(1.U) + 3.U(64.W))

        CP := 54.U
      }

      is(54.U) {
        /*
        $14 = (($13: anvil.PrinterIndex.U) & ($2: anvil.PrinterIndex.U))
        goto .55
        */



        generalRegFiles(14.U) := (generalRegFiles(13.U) & generalRegFiles(2.U))

        CP := 55.U
      }

      is(55.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($14: anvil.PrinterIndex.U) as SP)) = (50: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($14: anvil.PrinterIndex.U)) = (50: U8)
        goto .56
        */


        val __tmp_2633 = ((generalRegFiles(11.U) + 12.U(8.W)) + generalRegFiles(14.U).asUInt)
        val __tmp_2634 = (50.U(8.W)).asUInt
        arrayRegFiles(__tmp_2633 + 0.U) := __tmp_2634(7, 0)

        CP := 56.U
      }

      is(56.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $13 = (($1: anvil.PrinterIndex.U) + (4: anvil.PrinterIndex.U))
        goto .57
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(13.U) := (generalRegFiles(1.U) + 4.U(64.W))

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
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($14: anvil.PrinterIndex.U) as SP)) = (51: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($14: anvil.PrinterIndex.U)) = (51: U8)
        goto .59
        */


        val __tmp_2635 = ((generalRegFiles(11.U) + 12.U(8.W)) + generalRegFiles(14.U).asUInt)
        val __tmp_2636 = (51.U(8.W)).asUInt
        arrayRegFiles(__tmp_2635 + 0.U) := __tmp_2636(7, 0)

        CP := 59.U
      }

      is(59.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $13 = (($1: anvil.PrinterIndex.U) + (5: anvil.PrinterIndex.U))
        goto .60
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(13.U) := (generalRegFiles(1.U) + 5.U(64.W))

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
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($14: anvil.PrinterIndex.U) as SP)) = (51: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($14: anvil.PrinterIndex.U)) = (51: U8)
        goto .62
        */


        val __tmp_2637 = ((generalRegFiles(11.U) + 12.U(8.W)) + generalRegFiles(14.U).asUInt)
        val __tmp_2638 = (51.U(8.W)).asUInt
        arrayRegFiles(__tmp_2637 + 0.U) := __tmp_2638(7, 0)

        CP := 62.U
      }

      is(62.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $13 = (($1: anvil.PrinterIndex.U) + (6: anvil.PrinterIndex.U))
        goto .63
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(13.U) := (generalRegFiles(1.U) + 6.U(64.W))

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
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($14: anvil.PrinterIndex.U) as SP)) = (55: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($14: anvil.PrinterIndex.U)) = (55: U8)
        goto .65
        */


        val __tmp_2639 = ((generalRegFiles(11.U) + 12.U(8.W)) + generalRegFiles(14.U).asUInt)
        val __tmp_2640 = (55.U(8.W)).asUInt
        arrayRegFiles(__tmp_2639 + 0.U) := __tmp_2640(7, 0)

        CP := 65.U
      }

      is(65.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $13 = (($1: anvil.PrinterIndex.U) + (7: anvil.PrinterIndex.U))
        goto .66
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(13.U) := (generalRegFiles(1.U) + 7.U(64.W))

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
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($14: anvil.PrinterIndex.U) as SP)) = (50: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($14: anvil.PrinterIndex.U)) = (50: U8)
        goto .68
        */


        val __tmp_2641 = ((generalRegFiles(11.U) + 12.U(8.W)) + generalRegFiles(14.U).asUInt)
        val __tmp_2642 = (50.U(8.W)).asUInt
        arrayRegFiles(__tmp_2641 + 0.U) := __tmp_2642(7, 0)

        CP := 68.U
      }

      is(68.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $13 = (($1: anvil.PrinterIndex.U) + (8: anvil.PrinterIndex.U))
        goto .69
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(13.U) := (generalRegFiles(1.U) + 8.U(64.W))

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
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($14: anvil.PrinterIndex.U) as SP)) = (48: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($14: anvil.PrinterIndex.U)) = (48: U8)
        goto .71
        */


        val __tmp_2643 = ((generalRegFiles(11.U) + 12.U(8.W)) + generalRegFiles(14.U).asUInt)
        val __tmp_2644 = (48.U(8.W)).asUInt
        arrayRegFiles(__tmp_2643 + 0.U) := __tmp_2644(7, 0)

        CP := 71.U
      }

      is(71.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $13 = (($1: anvil.PrinterIndex.U) + (9: anvil.PrinterIndex.U))
        goto .72
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(13.U) := (generalRegFiles(1.U) + 9.U(64.W))

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
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($14: anvil.PrinterIndex.U) as SP)) = (51: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($14: anvil.PrinterIndex.U)) = (51: U8)
        goto .74
        */


        val __tmp_2645 = ((generalRegFiles(11.U) + 12.U(8.W)) + generalRegFiles(14.U).asUInt)
        val __tmp_2646 = (51.U(8.W)).asUInt
        arrayRegFiles(__tmp_2645 + 0.U) := __tmp_2646(7, 0)

        CP := 74.U
      }

      is(74.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $13 = (($1: anvil.PrinterIndex.U) + (10: anvil.PrinterIndex.U))
        goto .75
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(13.U) := (generalRegFiles(1.U) + 10.U(64.W))

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
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($14: anvil.PrinterIndex.U) as SP)) = (54: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($14: anvil.PrinterIndex.U)) = (54: U8)
        goto .77
        */


        val __tmp_2647 = ((generalRegFiles(11.U) + 12.U(8.W)) + generalRegFiles(14.U).asUInt)
        val __tmp_2648 = (54.U(8.W)).asUInt
        arrayRegFiles(__tmp_2647 + 0.U) := __tmp_2648(7, 0)

        CP := 77.U
      }

      is(77.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $13 = (($1: anvil.PrinterIndex.U) + (11: anvil.PrinterIndex.U))
        goto .78
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(13.U) := (generalRegFiles(1.U) + 11.U(64.W))

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
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($14: anvil.PrinterIndex.U) as SP)) = (56: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($14: anvil.PrinterIndex.U)) = (56: U8)
        goto .80
        */


        val __tmp_2649 = ((generalRegFiles(11.U) + 12.U(8.W)) + generalRegFiles(14.U).asUInt)
        val __tmp_2650 = (56.U(8.W)).asUInt
        arrayRegFiles(__tmp_2649 + 0.U) := __tmp_2650(7, 0)

        CP := 80.U
      }

      is(80.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $13 = (($1: anvil.PrinterIndex.U) + (12: anvil.PrinterIndex.U))
        goto .81
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(13.U) := (generalRegFiles(1.U) + 12.U(64.W))

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
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($14: anvil.PrinterIndex.U) as SP)) = (53: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($14: anvil.PrinterIndex.U)) = (53: U8)
        goto .83
        */


        val __tmp_2651 = ((generalRegFiles(11.U) + 12.U(8.W)) + generalRegFiles(14.U).asUInt)
        val __tmp_2652 = (53.U(8.W)).asUInt
        arrayRegFiles(__tmp_2651 + 0.U) := __tmp_2652(7, 0)

        CP := 83.U
      }

      is(83.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $13 = (($1: anvil.PrinterIndex.U) + (13: anvil.PrinterIndex.U))
        goto .84
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(13.U) := (generalRegFiles(1.U) + 13.U(64.W))

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
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($14: anvil.PrinterIndex.U) as SP)) = (52: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($14: anvil.PrinterIndex.U)) = (52: U8)
        goto .86
        */


        val __tmp_2653 = ((generalRegFiles(11.U) + 12.U(8.W)) + generalRegFiles(14.U).asUInt)
        val __tmp_2654 = (52.U(8.W)).asUInt
        arrayRegFiles(__tmp_2653 + 0.U) := __tmp_2654(7, 0)

        CP := 86.U
      }

      is(86.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $13 = (($1: anvil.PrinterIndex.U) + (14: anvil.PrinterIndex.U))
        goto .87
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(13.U) := (generalRegFiles(1.U) + 14.U(64.W))

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
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($14: anvil.PrinterIndex.U) as SP)) = (55: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($14: anvil.PrinterIndex.U)) = (55: U8)
        goto .89
        */


        val __tmp_2655 = ((generalRegFiles(11.U) + 12.U(8.W)) + generalRegFiles(14.U).asUInt)
        val __tmp_2656 = (55.U(8.W)).asUInt
        arrayRegFiles(__tmp_2655 + 0.U) := __tmp_2656(7, 0)

        CP := 89.U
      }

      is(89.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $13 = (($1: anvil.PrinterIndex.U) + (15: anvil.PrinterIndex.U))
        goto .90
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(13.U) := (generalRegFiles(1.U) + 15.U(64.W))

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
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($14: anvil.PrinterIndex.U) as SP)) = (55: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($14: anvil.PrinterIndex.U)) = (55: U8)
        goto .92
        */


        val __tmp_2657 = ((generalRegFiles(11.U) + 12.U(8.W)) + generalRegFiles(14.U).asUInt)
        val __tmp_2658 = (55.U(8.W)).asUInt
        arrayRegFiles(__tmp_2657 + 0.U) := __tmp_2658(7, 0)

        CP := 92.U
      }

      is(92.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $13 = (($1: anvil.PrinterIndex.U) + (16: anvil.PrinterIndex.U))
        goto .93
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(13.U) := (generalRegFiles(1.U) + 16.U(64.W))

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
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($14: anvil.PrinterIndex.U) as SP)) = (53: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($14: anvil.PrinterIndex.U)) = (53: U8)
        goto .95
        */


        val __tmp_2659 = ((generalRegFiles(11.U) + 12.U(8.W)) + generalRegFiles(14.U).asUInt)
        val __tmp_2660 = (53.U(8.W)).asUInt
        arrayRegFiles(__tmp_2659 + 0.U) := __tmp_2660(7, 0)

        CP := 95.U
      }

      is(95.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $13 = (($1: anvil.PrinterIndex.U) + (17: anvil.PrinterIndex.U))
        goto .96
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(13.U) := (generalRegFiles(1.U) + 17.U(64.W))

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
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($14: anvil.PrinterIndex.U) as SP)) = (56: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($14: anvil.PrinterIndex.U)) = (56: U8)
        goto .98
        */


        val __tmp_2661 = ((generalRegFiles(11.U) + 12.U(8.W)) + generalRegFiles(14.U).asUInt)
        val __tmp_2662 = (56.U(8.W)).asUInt
        arrayRegFiles(__tmp_2661 + 0.U) := __tmp_2662(7, 0)

        CP := 98.U
      }

      is(98.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $13 = (($1: anvil.PrinterIndex.U) + (18: anvil.PrinterIndex.U))
        goto .99
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(13.U) := (generalRegFiles(1.U) + 18.U(64.W))

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
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($14: anvil.PrinterIndex.U) as SP)) = (48: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($14: anvil.PrinterIndex.U)) = (48: U8)
        goto .101
        */


        val __tmp_2663 = ((generalRegFiles(11.U) + 12.U(8.W)) + generalRegFiles(14.U).asUInt)
        val __tmp_2664 = (48.U(8.W)).asUInt
        arrayRegFiles(__tmp_2663 + 0.U) := __tmp_2664(7, 0)

        CP := 101.U
      }

      is(101.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $13 = (($1: anvil.PrinterIndex.U) + (19: anvil.PrinterIndex.U))
        goto .102
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(13.U) := (generalRegFiles(1.U) + 19.U(64.W))

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
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($14: anvil.PrinterIndex.U) as SP)) = (56: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($14: anvil.PrinterIndex.U)) = (56: U8)
        goto .104
        */


        val __tmp_2665 = ((generalRegFiles(11.U) + 12.U(8.W)) + generalRegFiles(14.U).asUInt)
        val __tmp_2666 = (56.U(8.W)).asUInt
        arrayRegFiles(__tmp_2665 + 0.U) := __tmp_2666(7, 0)

        CP := 104.U
      }

      is(104.U) {
        /*
        **(SP + (2: SP)) = (20: U64) [unsigned, U64, 8]  // $res = (20: U64)
        goto $ret@0
        */


        val __tmp_2667 = Cat(
          arrayRegFiles((SP + 2.U(8.W)) + 0.U)
        )
        val __tmp_2668 = (20.U(64.W)).asUInt
        arrayRegFiles(__tmp_2667 + 0.U) := __tmp_2668(7, 0)
        arrayRegFiles(__tmp_2667 + 1.U) := __tmp_2668(15, 8)
        arrayRegFiles(__tmp_2667 + 2.U) := __tmp_2668(23, 16)
        arrayRegFiles(__tmp_2667 + 3.U) := __tmp_2668(31, 24)
        arrayRegFiles(__tmp_2667 + 4.U) := __tmp_2668(39, 32)
        arrayRegFiles(__tmp_2667 + 5.U) := __tmp_2668(47, 40)
        arrayRegFiles(__tmp_2667 + 6.U) := __tmp_2668(55, 48)
        arrayRegFiles(__tmp_2667 + 7.U) := __tmp_2668(63, 56)

        CP := Cat(
          arrayRegFiles(SP + 0.U + 1.U),
          arrayRegFiles(SP + 0.U + 0.U)
        )

      }

      is(105.U) {
        /*
        $10 = (($3: S64) ≡ (0: S64))
        goto .106
        */



        generalRegFiles(10.U) := (generalRegFiles(3.U).asSInt === 0.S(64.W)).asUInt

        CP := 106.U
      }

      is(106.U) {
        /*
        if ($10: B) goto .107 else goto .110
        */


        CP := Mux((generalRegFiles(10.U).asUInt) === 1.U, 107.U, 110.U)
      }

      is(107.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $12 = (($1: anvil.PrinterIndex.U) & ($2: anvil.PrinterIndex.U))
        goto .108
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(12.U) := (generalRegFiles(1.U) & generalRegFiles(2.U))

        CP := 108.U
      }

      is(108.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($12: anvil.PrinterIndex.U) as SP)) = (48: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($12: anvil.PrinterIndex.U)) = (48: U8)
        goto .109
        */


        val __tmp_2669 = ((generalRegFiles(11.U) + 12.U(8.W)) + generalRegFiles(12.U).asUInt)
        val __tmp_2670 = (48.U(8.W)).asUInt
        arrayRegFiles(__tmp_2669 + 0.U) := __tmp_2670(7, 0)

        CP := 109.U
      }

      is(109.U) {
        /*
        **(SP + (2: SP)) = (1: U64) [unsigned, U64, 8]  // $res = (1: U64)
        goto $ret@0
        */


        val __tmp_2671 = Cat(
          arrayRegFiles((SP + 2.U(8.W)) + 0.U)
        )
        val __tmp_2672 = (1.U(64.W)).asUInt
        arrayRegFiles(__tmp_2671 + 0.U) := __tmp_2672(7, 0)
        arrayRegFiles(__tmp_2671 + 1.U) := __tmp_2672(15, 8)
        arrayRegFiles(__tmp_2671 + 2.U) := __tmp_2672(23, 16)
        arrayRegFiles(__tmp_2671 + 3.U) := __tmp_2672(31, 24)
        arrayRegFiles(__tmp_2671 + 4.U) := __tmp_2672(39, 32)
        arrayRegFiles(__tmp_2671 + 5.U) := __tmp_2672(47, 40)
        arrayRegFiles(__tmp_2671 + 6.U) := __tmp_2672(55, 48)
        arrayRegFiles(__tmp_2671 + 7.U) := __tmp_2672(63, 56)

        CP := Cat(
          arrayRegFiles(SP + 0.U + 1.U),
          arrayRegFiles(SP + 0.U + 0.U)
        )

      }

      is(110.U) {
        /*
        decl buff: MS[anvil.PrinterIndex.I20, U8] [@3, 34]
        alloc $new@[168,16].5BB7E063: MS[anvil.PrinterIndex.I20, U8] [@37, 34]
        $11 = (SP + (37: SP))
        *(SP + (37: SP)) = (323602724: U32) [unsigned, U32, 4]  // sha3 type signature of MS[anvil.PrinterIndex.I20, U8]: 0x1349C924
        *(SP + (41: SP)) = (20: Z) [signed, Z, 8]  // size of MS[anvil.PrinterIndex.I20, U8]((0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8))
        goto .111
        */



        generalRegFiles(11.U) := (SP + 37.U(8.W))

        val __tmp_2673 = (SP + 37.U(8.W))
        val __tmp_2674 = (323602724.U(32.W)).asUInt
        arrayRegFiles(__tmp_2673 + 0.U) := __tmp_2674(7, 0)
        arrayRegFiles(__tmp_2673 + 1.U) := __tmp_2674(15, 8)
        arrayRegFiles(__tmp_2673 + 2.U) := __tmp_2674(23, 16)
        arrayRegFiles(__tmp_2673 + 3.U) := __tmp_2674(31, 24)

        val __tmp_2675 = (SP + 41.U(8.W))
        val __tmp_2676 = (20.S(64.W)).asUInt
        arrayRegFiles(__tmp_2675 + 0.U) := __tmp_2676(7, 0)
        arrayRegFiles(__tmp_2675 + 1.U) := __tmp_2676(15, 8)
        arrayRegFiles(__tmp_2675 + 2.U) := __tmp_2676(23, 16)
        arrayRegFiles(__tmp_2675 + 3.U) := __tmp_2676(31, 24)
        arrayRegFiles(__tmp_2675 + 4.U) := __tmp_2676(39, 32)
        arrayRegFiles(__tmp_2675 + 5.U) := __tmp_2676(47, 40)
        arrayRegFiles(__tmp_2675 + 6.U) := __tmp_2676(55, 48)
        arrayRegFiles(__tmp_2675 + 7.U) := __tmp_2676(63, 56)

        CP := 111.U
      }

      is(111.U) {
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
        goto .112
        */


        val __tmp_2677 = ((generalRegFiles(11.U) + 12.U(8.W)) + 0.S(8.W).asUInt)
        val __tmp_2678 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_2677 + 0.U) := __tmp_2678(7, 0)

        val __tmp_2679 = ((generalRegFiles(11.U) + 12.U(8.W)) + 1.S(8.W).asUInt)
        val __tmp_2680 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_2679 + 0.U) := __tmp_2680(7, 0)

        val __tmp_2681 = ((generalRegFiles(11.U) + 12.U(8.W)) + 2.S(8.W).asUInt)
        val __tmp_2682 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_2681 + 0.U) := __tmp_2682(7, 0)

        val __tmp_2683 = ((generalRegFiles(11.U) + 12.U(8.W)) + 3.S(8.W).asUInt)
        val __tmp_2684 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_2683 + 0.U) := __tmp_2684(7, 0)

        val __tmp_2685 = ((generalRegFiles(11.U) + 12.U(8.W)) + 4.S(8.W).asUInt)
        val __tmp_2686 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_2685 + 0.U) := __tmp_2686(7, 0)

        val __tmp_2687 = ((generalRegFiles(11.U) + 12.U(8.W)) + 5.S(8.W).asUInt)
        val __tmp_2688 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_2687 + 0.U) := __tmp_2688(7, 0)

        val __tmp_2689 = ((generalRegFiles(11.U) + 12.U(8.W)) + 6.S(8.W).asUInt)
        val __tmp_2690 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_2689 + 0.U) := __tmp_2690(7, 0)

        val __tmp_2691 = ((generalRegFiles(11.U) + 12.U(8.W)) + 7.S(8.W).asUInt)
        val __tmp_2692 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_2691 + 0.U) := __tmp_2692(7, 0)

        val __tmp_2693 = ((generalRegFiles(11.U) + 12.U(8.W)) + 8.S(8.W).asUInt)
        val __tmp_2694 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_2693 + 0.U) := __tmp_2694(7, 0)

        val __tmp_2695 = ((generalRegFiles(11.U) + 12.U(8.W)) + 9.S(8.W).asUInt)
        val __tmp_2696 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_2695 + 0.U) := __tmp_2696(7, 0)

        val __tmp_2697 = ((generalRegFiles(11.U) + 12.U(8.W)) + 10.S(8.W).asUInt)
        val __tmp_2698 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_2697 + 0.U) := __tmp_2698(7, 0)

        val __tmp_2699 = ((generalRegFiles(11.U) + 12.U(8.W)) + 11.S(8.W).asUInt)
        val __tmp_2700 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_2699 + 0.U) := __tmp_2700(7, 0)

        val __tmp_2701 = ((generalRegFiles(11.U) + 12.U(8.W)) + 12.S(8.W).asUInt)
        val __tmp_2702 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_2701 + 0.U) := __tmp_2702(7, 0)

        val __tmp_2703 = ((generalRegFiles(11.U) + 12.U(8.W)) + 13.S(8.W).asUInt)
        val __tmp_2704 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_2703 + 0.U) := __tmp_2704(7, 0)

        val __tmp_2705 = ((generalRegFiles(11.U) + 12.U(8.W)) + 14.S(8.W).asUInt)
        val __tmp_2706 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_2705 + 0.U) := __tmp_2706(7, 0)

        val __tmp_2707 = ((generalRegFiles(11.U) + 12.U(8.W)) + 15.S(8.W).asUInt)
        val __tmp_2708 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_2707 + 0.U) := __tmp_2708(7, 0)

        val __tmp_2709 = ((generalRegFiles(11.U) + 12.U(8.W)) + 16.S(8.W).asUInt)
        val __tmp_2710 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_2709 + 0.U) := __tmp_2710(7, 0)

        val __tmp_2711 = ((generalRegFiles(11.U) + 12.U(8.W)) + 17.S(8.W).asUInt)
        val __tmp_2712 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_2711 + 0.U) := __tmp_2712(7, 0)

        val __tmp_2713 = ((generalRegFiles(11.U) + 12.U(8.W)) + 18.S(8.W).asUInt)
        val __tmp_2714 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_2713 + 0.U) := __tmp_2714(7, 0)

        val __tmp_2715 = ((generalRegFiles(11.U) + 12.U(8.W)) + 19.S(8.W).asUInt)
        val __tmp_2716 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_2715 + 0.U) := __tmp_2716(7, 0)

        CP := 112.U
      }

      is(112.U) {
        /*
        (SP + (3: SP)) [MS[anvil.PrinterIndex.I20, U8], 34]  <-  ($11: MS[anvil.PrinterIndex.I20, U8]) [MS[anvil.PrinterIndex.I20, U8], ((*(($11: MS[anvil.PrinterIndex.I20, U8]) + (4: SP)) as SP) + (12: SP))]  // buff = ($11: MS[anvil.PrinterIndex.I20, U8])
        goto .113
        */


        val __tmp_2717 = (SP + 3.U(8.W))
        val __tmp_2718 = generalRegFiles(11.U)
        val __tmp_2719 = (Cat(
           arrayRegFiles((generalRegFiles(11.U) + 4.U(8.W)) + 7.U),
           arrayRegFiles((generalRegFiles(11.U) + 4.U(8.W)) + 6.U),
           arrayRegFiles((generalRegFiles(11.U) + 4.U(8.W)) + 5.U),
           arrayRegFiles((generalRegFiles(11.U) + 4.U(8.W)) + 4.U),
           arrayRegFiles((generalRegFiles(11.U) + 4.U(8.W)) + 3.U),
           arrayRegFiles((generalRegFiles(11.U) + 4.U(8.W)) + 2.U),
           arrayRegFiles((generalRegFiles(11.U) + 4.U(8.W)) + 1.U),
           arrayRegFiles((generalRegFiles(11.U) + 4.U(8.W)) + 0.U)
         ).asSInt.asUInt + 12.U(8.W))

        when(Idx < __tmp_2719) {
          arrayRegFiles(__tmp_2717 + Idx + 0.U) := arrayRegFiles(__tmp_2718 + Idx + 0.U)
          arrayRegFiles(__tmp_2717 + Idx + 1.U) := arrayRegFiles(__tmp_2718 + Idx + 1.U)
          arrayRegFiles(__tmp_2717 + Idx + 2.U) := arrayRegFiles(__tmp_2718 + Idx + 2.U)
          arrayRegFiles(__tmp_2717 + Idx + 3.U) := arrayRegFiles(__tmp_2718 + Idx + 3.U)
          arrayRegFiles(__tmp_2717 + Idx + 4.U) := arrayRegFiles(__tmp_2718 + Idx + 4.U)
          arrayRegFiles(__tmp_2717 + Idx + 5.U) := arrayRegFiles(__tmp_2718 + Idx + 5.U)
          arrayRegFiles(__tmp_2717 + Idx + 6.U) := arrayRegFiles(__tmp_2718 + Idx + 6.U)
          arrayRegFiles(__tmp_2717 + Idx + 7.U) := arrayRegFiles(__tmp_2718 + Idx + 7.U)
          Idx := Idx + 8.U
          LeftByteRounds := __tmp_2719 - Idx
        } .elsewhen(IdxLeftByteRounds < LeftByteRounds) {
          val __tmp_2720 = Idx - 8.U
          arrayRegFiles(__tmp_2717 + __tmp_2720 + IdxLeftByteRounds) := arrayRegFiles(__tmp_2718 + __tmp_2720 + IdxLeftByteRounds)
          IdxLeftByteRounds := IdxLeftByteRounds + 1.U
        } .otherwise {
          Idx := 0.U
          IdxLeftByteRounds := 0.U
          LeftByteRounds := 0.U
          CP := 113.U
        }


      }

      is(113.U) {
        /*
        unalloc $new@[168,16].5BB7E063: MS[anvil.PrinterIndex.I20, U8] [@37, 34]
        goto .114
        */


        CP := 114.U
      }

      is(114.U) {
        /*
        decl i: anvil.PrinterIndex.I20 @$4
        $4 = (0: anvil.PrinterIndex.I20)
        goto .115
        */



        generalRegFiles(4.U) := (0.S(8.W)).asUInt

        CP := 115.U
      }

      is(115.U) {
        /*
        decl neg: B @$5
        $10 = (($3: S64) < (0: S64))
        goto .116
        */



        generalRegFiles(10.U) := (generalRegFiles(3.U).asSInt < 0.S(64.W)).asUInt

        CP := 116.U
      }

      is(116.U) {
        /*
        $5 = ($10: B)
        goto .117
        */



        generalRegFiles(5.U) := generalRegFiles(10.U)

        CP := 117.U
      }

      is(117.U) {
        /*
        decl m: S64 @$6
        goto .118
        */


        CP := 118.U
      }

      is(118.U) {
        /*
        if ($5: B) goto .119 else goto .121
        */


        CP := Mux((generalRegFiles(5.U).asUInt) === 1.U, 119.U, 121.U)
      }

      is(119.U) {
        /*
        $12 = -(($3: S64))
        goto .120
        */



        generalRegFiles(12.U) := (-generalRegFiles(3.U).asSInt).asUInt

        CP := 120.U
      }

      is(120.U) {
        /*
        $10 = ($12: S64)
        goto .123
        */



        generalRegFiles(10.U) := (generalRegFiles(12.U).asSInt).asUInt

        CP := 123.U
      }

      is(121.U) {
        /*
        $14 = ($3: S64)
        goto .122
        */



        generalRegFiles(14.U) := (generalRegFiles(3.U).asSInt).asUInt

        CP := 122.U
      }

      is(122.U) {
        /*
        $10 = ($14: S64)
        goto .123
        */



        generalRegFiles(10.U) := (generalRegFiles(14.U).asSInt).asUInt

        CP := 123.U
      }

      is(123.U) {
        /*
        $6 = ($10: S64)
        goto .124
        */



        generalRegFiles(6.U) := (generalRegFiles(10.U).asSInt).asUInt

        CP := 124.U
      }

      is(124.U) {
        /*
        $11 = ($6: S64)
        goto .125
        */



        generalRegFiles(11.U) := (generalRegFiles(6.U).asSInt).asUInt

        CP := 125.U
      }

      is(125.U) {
        /*
        $10 = (($11: S64) > (0: S64))
        goto .126
        */



        generalRegFiles(10.U) := (generalRegFiles(11.U).asSInt > 0.S(64.W)).asUInt

        CP := 126.U
      }

      is(126.U) {
        /*
        if ($10: B) goto .127 else goto .156
        */


        CP := Mux((generalRegFiles(10.U).asUInt) === 1.U, 127.U, 156.U)
      }

      is(127.U) {
        /*
        $11 = ($6: S64)
        goto .128
        */



        generalRegFiles(11.U) := (generalRegFiles(6.U).asSInt).asUInt

        CP := 128.U
      }

      is(128.U) {
        /*
        $10 = (($11: S64) % (10: S64))
        goto .129
        */



        generalRegFiles(10.U) := ((generalRegFiles(11.U).asSInt % 10.S(64.W))).asUInt

        CP := 129.U
      }

      is(129.U) {
        /*
        switch (($10: S64))
          (0: S64): goto 130
          (1: S64): goto 132
          (2: S64): goto 134
          (3: S64): goto 136
          (4: S64): goto 138
          (5: S64): goto 140
          (6: S64): goto 142
          (7: S64): goto 144
          (8: S64): goto 146
          (9: S64): goto 148

        */


        val __tmp_2721 = generalRegFiles(10.U).asSInt

        switch(__tmp_2721) {

          is(0.S(64.W)) {
            CP := 130.U
          }


          is(1.S(64.W)) {
            CP := 132.U
          }


          is(2.S(64.W)) {
            CP := 134.U
          }


          is(3.S(64.W)) {
            CP := 136.U
          }


          is(4.S(64.W)) {
            CP := 138.U
          }


          is(5.S(64.W)) {
            CP := 140.U
          }


          is(6.S(64.W)) {
            CP := 142.U
          }


          is(7.S(64.W)) {
            CP := 144.U
          }


          is(8.S(64.W)) {
            CP := 146.U
          }


          is(9.S(64.W)) {
            CP := 148.U
          }

        }

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
        *((($11: MS[anvil.PrinterIndex.I20, U8]) + (12: SP)) + (($10: anvil.PrinterIndex.I20) as SP)) = (48: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.I20, U8])(($10: anvil.PrinterIndex.I20)) = (48: U8)
        goto .150
        */


        val __tmp_2722 = ((generalRegFiles(11.U) + 12.U(8.W)) + generalRegFiles(10.U).asSInt.asUInt)
        val __tmp_2723 = (48.U(8.W)).asUInt
        arrayRegFiles(__tmp_2722 + 0.U) := __tmp_2723(7, 0)

        CP := 150.U
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
        *((($11: MS[anvil.PrinterIndex.I20, U8]) + (12: SP)) + (($10: anvil.PrinterIndex.I20) as SP)) = (49: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.I20, U8])(($10: anvil.PrinterIndex.I20)) = (49: U8)
        goto .150
        */


        val __tmp_2724 = ((generalRegFiles(11.U) + 12.U(8.W)) + generalRegFiles(10.U).asSInt.asUInt)
        val __tmp_2725 = (49.U(8.W)).asUInt
        arrayRegFiles(__tmp_2724 + 0.U) := __tmp_2725(7, 0)

        CP := 150.U
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
        *((($11: MS[anvil.PrinterIndex.I20, U8]) + (12: SP)) + (($10: anvil.PrinterIndex.I20) as SP)) = (50: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.I20, U8])(($10: anvil.PrinterIndex.I20)) = (50: U8)
        goto .150
        */


        val __tmp_2726 = ((generalRegFiles(11.U) + 12.U(8.W)) + generalRegFiles(10.U).asSInt.asUInt)
        val __tmp_2727 = (50.U(8.W)).asUInt
        arrayRegFiles(__tmp_2726 + 0.U) := __tmp_2727(7, 0)

        CP := 150.U
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
        *((($11: MS[anvil.PrinterIndex.I20, U8]) + (12: SP)) + (($10: anvil.PrinterIndex.I20) as SP)) = (51: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.I20, U8])(($10: anvil.PrinterIndex.I20)) = (51: U8)
        goto .150
        */


        val __tmp_2728 = ((generalRegFiles(11.U) + 12.U(8.W)) + generalRegFiles(10.U).asSInt.asUInt)
        val __tmp_2729 = (51.U(8.W)).asUInt
        arrayRegFiles(__tmp_2728 + 0.U) := __tmp_2729(7, 0)

        CP := 150.U
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
        *((($11: MS[anvil.PrinterIndex.I20, U8]) + (12: SP)) + (($10: anvil.PrinterIndex.I20) as SP)) = (52: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.I20, U8])(($10: anvil.PrinterIndex.I20)) = (52: U8)
        goto .150
        */


        val __tmp_2730 = ((generalRegFiles(11.U) + 12.U(8.W)) + generalRegFiles(10.U).asSInt.asUInt)
        val __tmp_2731 = (52.U(8.W)).asUInt
        arrayRegFiles(__tmp_2730 + 0.U) := __tmp_2731(7, 0)

        CP := 150.U
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
        *((($11: MS[anvil.PrinterIndex.I20, U8]) + (12: SP)) + (($10: anvil.PrinterIndex.I20) as SP)) = (53: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.I20, U8])(($10: anvil.PrinterIndex.I20)) = (53: U8)
        goto .150
        */


        val __tmp_2732 = ((generalRegFiles(11.U) + 12.U(8.W)) + generalRegFiles(10.U).asSInt.asUInt)
        val __tmp_2733 = (53.U(8.W)).asUInt
        arrayRegFiles(__tmp_2732 + 0.U) := __tmp_2733(7, 0)

        CP := 150.U
      }

      is(142.U) {
        /*
        $11 = (SP + (3: SP))
        $10 = ($4: anvil.PrinterIndex.I20)
        goto .143
        */



        generalRegFiles(11.U) := (SP + 3.U(8.W))


        generalRegFiles(10.U) := (generalRegFiles(4.U).asSInt).asUInt

        CP := 143.U
      }

      is(143.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.I20, U8]) + (12: SP)) + (($10: anvil.PrinterIndex.I20) as SP)) = (54: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.I20, U8])(($10: anvil.PrinterIndex.I20)) = (54: U8)
        goto .150
        */


        val __tmp_2734 = ((generalRegFiles(11.U) + 12.U(8.W)) + generalRegFiles(10.U).asSInt.asUInt)
        val __tmp_2735 = (54.U(8.W)).asUInt
        arrayRegFiles(__tmp_2734 + 0.U) := __tmp_2735(7, 0)

        CP := 150.U
      }

      is(144.U) {
        /*
        $11 = (SP + (3: SP))
        $10 = ($4: anvil.PrinterIndex.I20)
        goto .145
        */



        generalRegFiles(11.U) := (SP + 3.U(8.W))


        generalRegFiles(10.U) := (generalRegFiles(4.U).asSInt).asUInt

        CP := 145.U
      }

      is(145.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.I20, U8]) + (12: SP)) + (($10: anvil.PrinterIndex.I20) as SP)) = (55: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.I20, U8])(($10: anvil.PrinterIndex.I20)) = (55: U8)
        goto .150
        */


        val __tmp_2736 = ((generalRegFiles(11.U) + 12.U(8.W)) + generalRegFiles(10.U).asSInt.asUInt)
        val __tmp_2737 = (55.U(8.W)).asUInt
        arrayRegFiles(__tmp_2736 + 0.U) := __tmp_2737(7, 0)

        CP := 150.U
      }

      is(146.U) {
        /*
        $11 = (SP + (3: SP))
        $10 = ($4: anvil.PrinterIndex.I20)
        goto .147
        */



        generalRegFiles(11.U) := (SP + 3.U(8.W))


        generalRegFiles(10.U) := (generalRegFiles(4.U).asSInt).asUInt

        CP := 147.U
      }

      is(147.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.I20, U8]) + (12: SP)) + (($10: anvil.PrinterIndex.I20) as SP)) = (56: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.I20, U8])(($10: anvil.PrinterIndex.I20)) = (56: U8)
        goto .150
        */


        val __tmp_2738 = ((generalRegFiles(11.U) + 12.U(8.W)) + generalRegFiles(10.U).asSInt.asUInt)
        val __tmp_2739 = (56.U(8.W)).asUInt
        arrayRegFiles(__tmp_2738 + 0.U) := __tmp_2739(7, 0)

        CP := 150.U
      }

      is(148.U) {
        /*
        $11 = (SP + (3: SP))
        $10 = ($4: anvil.PrinterIndex.I20)
        goto .149
        */



        generalRegFiles(11.U) := (SP + 3.U(8.W))


        generalRegFiles(10.U) := (generalRegFiles(4.U).asSInt).asUInt

        CP := 149.U
      }

      is(149.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.I20, U8]) + (12: SP)) + (($10: anvil.PrinterIndex.I20) as SP)) = (57: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.I20, U8])(($10: anvil.PrinterIndex.I20)) = (57: U8)
        goto .150
        */


        val __tmp_2740 = ((generalRegFiles(11.U) + 12.U(8.W)) + generalRegFiles(10.U).asSInt.asUInt)
        val __tmp_2741 = (57.U(8.W)).asUInt
        arrayRegFiles(__tmp_2740 + 0.U) := __tmp_2741(7, 0)

        CP := 150.U
      }

      is(150.U) {
        /*
        $11 = ($6: S64)
        goto .151
        */



        generalRegFiles(11.U) := (generalRegFiles(6.U).asSInt).asUInt

        CP := 151.U
      }

      is(151.U) {
        /*
        $10 = (($11: S64) / (10: S64))
        goto .152
        */



        generalRegFiles(10.U) := ((generalRegFiles(11.U).asSInt / 10.S(64.W))).asUInt

        CP := 152.U
      }

      is(152.U) {
        /*
        $6 = ($10: S64)
        goto .153
        */



        generalRegFiles(6.U) := (generalRegFiles(10.U).asSInt).asUInt

        CP := 153.U
      }

      is(153.U) {
        /*
        $11 = ($4: anvil.PrinterIndex.I20)
        goto .154
        */



        generalRegFiles(11.U) := (generalRegFiles(4.U).asSInt).asUInt

        CP := 154.U
      }

      is(154.U) {
        /*
        $10 = (($11: anvil.PrinterIndex.I20) + (1: anvil.PrinterIndex.I20))
        goto .155
        */



        generalRegFiles(10.U) := ((generalRegFiles(11.U).asSInt + 1.S(8.W))).asUInt

        CP := 155.U
      }

      is(155.U) {
        /*
        $4 = ($10: anvil.PrinterIndex.I20)
        goto .124
        */



        generalRegFiles(4.U) := (generalRegFiles(10.U).asSInt).asUInt

        CP := 124.U
      }

      is(156.U) {
        /*
        $11 = ($5: B)
        undecl neg: B @$5
        goto .157
        */



        generalRegFiles(11.U) := generalRegFiles(5.U)

        CP := 157.U
      }

      is(157.U) {
        /*
        if ($11: B) goto .158 else goto .163
        */


        CP := Mux((generalRegFiles(11.U).asUInt) === 1.U, 158.U, 163.U)
      }

      is(158.U) {
        /*
        $11 = (SP + (3: SP))
        $10 = ($4: anvil.PrinterIndex.I20)
        goto .159
        */



        generalRegFiles(11.U) := (SP + 3.U(8.W))


        generalRegFiles(10.U) := (generalRegFiles(4.U).asSInt).asUInt

        CP := 159.U
      }

      is(159.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.I20, U8]) + (12: SP)) + (($10: anvil.PrinterIndex.I20) as SP)) = (45: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.I20, U8])(($10: anvil.PrinterIndex.I20)) = (45: U8)
        goto .160
        */


        val __tmp_2742 = ((generalRegFiles(11.U) + 12.U(8.W)) + generalRegFiles(10.U).asSInt.asUInt)
        val __tmp_2743 = (45.U(8.W)).asUInt
        arrayRegFiles(__tmp_2742 + 0.U) := __tmp_2743(7, 0)

        CP := 160.U
      }

      is(160.U) {
        /*
        $11 = ($4: anvil.PrinterIndex.I20)
        goto .161
        */



        generalRegFiles(11.U) := (generalRegFiles(4.U).asSInt).asUInt

        CP := 161.U
      }

      is(161.U) {
        /*
        $10 = (($11: anvil.PrinterIndex.I20) + (1: anvil.PrinterIndex.I20))
        goto .162
        */



        generalRegFiles(10.U) := ((generalRegFiles(11.U).asSInt + 1.S(8.W))).asUInt

        CP := 162.U
      }

      is(162.U) {
        /*
        $4 = ($10: anvil.PrinterIndex.I20)
        goto .163
        */



        generalRegFiles(4.U) := (generalRegFiles(10.U).asSInt).asUInt

        CP := 163.U
      }

      is(163.U) {
        /*
        decl j: anvil.PrinterIndex.I20 @$7
        $11 = ($4: anvil.PrinterIndex.I20)
        undecl i: anvil.PrinterIndex.I20 @$4
        goto .164
        */



        generalRegFiles(11.U) := (generalRegFiles(4.U).asSInt).asUInt

        CP := 164.U
      }

      is(164.U) {
        /*
        $10 = (($11: anvil.PrinterIndex.I20) - (1: anvil.PrinterIndex.I20))
        goto .165
        */



        generalRegFiles(10.U) := ((generalRegFiles(11.U).asSInt - 1.S(8.W))).asUInt

        CP := 165.U
      }

      is(165.U) {
        /*
        $7 = ($10: anvil.PrinterIndex.I20)
        goto .166
        */



        generalRegFiles(7.U) := (generalRegFiles(10.U).asSInt).asUInt

        CP := 166.U
      }

      is(166.U) {
        /*
        decl idx: anvil.PrinterIndex.U @$8
        $11 = ($1: anvil.PrinterIndex.U)
        goto .167
        */



        generalRegFiles(11.U) := generalRegFiles(1.U)

        CP := 167.U
      }

      is(167.U) {
        /*
        $8 = ($11: anvil.PrinterIndex.U)
        goto .168
        */



        generalRegFiles(8.U) := generalRegFiles(11.U)

        CP := 168.U
      }

      is(168.U) {
        /*
        decl r: U64 @$9
        $9 = (0: U64)
        goto .169
        */



        generalRegFiles(9.U) := 0.U(64.W)

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
        $10 = (($11: anvil.PrinterIndex.I20) >= (0: anvil.PrinterIndex.I20))
        goto .171
        */



        generalRegFiles(10.U) := (generalRegFiles(11.U).asSInt >= 0.S(8.W)).asUInt

        CP := 171.U
      }

      is(171.U) {
        /*
        if ($10: B) goto .172 else goto .186
        */


        CP := Mux((generalRegFiles(10.U).asUInt) === 1.U, 172.U, 186.U)
      }

      is(172.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $10 = ($8: anvil.PrinterIndex.U)
        $13 = ($2: anvil.PrinterIndex.U)
        goto .173
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(10.U) := generalRegFiles(8.U)


        generalRegFiles(13.U) := generalRegFiles(2.U)

        CP := 173.U
      }

      is(173.U) {
        /*
        $12 = (($10: anvil.PrinterIndex.U) & ($13: anvil.PrinterIndex.U))
        goto .174
        */



        generalRegFiles(12.U) := (generalRegFiles(10.U) & generalRegFiles(13.U))

        CP := 174.U
      }

      is(174.U) {
        /*
        $14 = (SP + (3: SP))
        $15 = ($7: anvil.PrinterIndex.I20)
        goto .175
        */



        generalRegFiles(14.U) := (SP + 3.U(8.W))


        generalRegFiles(15.U) := (generalRegFiles(7.U).asSInt).asUInt

        CP := 175.U
      }

      is(175.U) {
        /*
        $16 = *((($14: MS[anvil.PrinterIndex.I20, U8]) + (12: SP)) + (($15: anvil.PrinterIndex.I20) as SP)) [unsigned, U8, 1]  // $16 = ($14: MS[anvil.PrinterIndex.I20, U8])(($15: anvil.PrinterIndex.I20))
        goto .176
        */


        val __tmp_2744 = (((generalRegFiles(14.U) + 12.U(8.W)) + generalRegFiles(15.U).asSInt.asUInt)).asUInt
        generalRegFiles(16.U) := Cat(
          arrayRegFiles(__tmp_2744 + 0.U)
        ).asUInt

        CP := 176.U
      }

      is(176.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($12: anvil.PrinterIndex.U) as SP)) = ($16: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($12: anvil.PrinterIndex.U)) = ($16: U8)
        goto .177
        */


        val __tmp_2745 = ((generalRegFiles(11.U) + 12.U(8.W)) + generalRegFiles(12.U).asUInt)
        val __tmp_2746 = (generalRegFiles(16.U)).asUInt
        arrayRegFiles(__tmp_2745 + 0.U) := __tmp_2746(7, 0)

        CP := 177.U
      }

      is(177.U) {
        /*
        $11 = ($7: anvil.PrinterIndex.I20)
        goto .178
        */



        generalRegFiles(11.U) := (generalRegFiles(7.U).asSInt).asUInt

        CP := 178.U
      }

      is(178.U) {
        /*
        $10 = (($11: anvil.PrinterIndex.I20) - (1: anvil.PrinterIndex.I20))
        goto .179
        */



        generalRegFiles(10.U) := ((generalRegFiles(11.U).asSInt - 1.S(8.W))).asUInt

        CP := 179.U
      }

      is(179.U) {
        /*
        $7 = ($10: anvil.PrinterIndex.I20)
        goto .180
        */



        generalRegFiles(7.U) := (generalRegFiles(10.U).asSInt).asUInt

        CP := 180.U
      }

      is(180.U) {
        /*
        $11 = ($8: anvil.PrinterIndex.U)
        goto .181
        */



        generalRegFiles(11.U) := generalRegFiles(8.U)

        CP := 181.U
      }

      is(181.U) {
        /*
        $10 = (($11: anvil.PrinterIndex.U) + (1: anvil.PrinterIndex.U))
        goto .182
        */



        generalRegFiles(10.U) := (generalRegFiles(11.U) + 1.U(64.W))

        CP := 182.U
      }

      is(182.U) {
        /*
        $8 = ($10: anvil.PrinterIndex.U)
        goto .183
        */



        generalRegFiles(8.U) := generalRegFiles(10.U)

        CP := 183.U
      }

      is(183.U) {
        /*
        $11 = ($9: U64)
        goto .184
        */



        generalRegFiles(11.U) := generalRegFiles(9.U)

        CP := 184.U
      }

      is(184.U) {
        /*
        $10 = (($11: U64) + (1: U64))
        goto .185
        */



        generalRegFiles(10.U) := (generalRegFiles(11.U) + 1.U(64.W))

        CP := 185.U
      }

      is(185.U) {
        /*
        $9 = ($10: U64)
        goto .169
        */



        generalRegFiles(9.U) := generalRegFiles(10.U)

        CP := 169.U
      }

      is(186.U) {
        /*
        $11 = ($9: U64)
        undecl r: U64 @$9
        goto .187
        */



        generalRegFiles(11.U) := generalRegFiles(9.U)

        CP := 187.U
      }

      is(187.U) {
        /*
        **(SP + (2: SP)) = ($11: U64) [unsigned, U64, 8]  // $res = ($11: U64)
        goto $ret@0
        */


        val __tmp_2747 = Cat(
          arrayRegFiles((SP + 2.U(8.W)) + 0.U)
        )
        val __tmp_2748 = (generalRegFiles(11.U)).asUInt
        arrayRegFiles(__tmp_2747 + 0.U) := __tmp_2748(7, 0)
        arrayRegFiles(__tmp_2747 + 1.U) := __tmp_2748(15, 8)
        arrayRegFiles(__tmp_2747 + 2.U) := __tmp_2748(23, 16)
        arrayRegFiles(__tmp_2747 + 3.U) := __tmp_2748(31, 24)
        arrayRegFiles(__tmp_2747 + 4.U) := __tmp_2748(39, 32)
        arrayRegFiles(__tmp_2747 + 5.U) := __tmp_2748(47, 40)
        arrayRegFiles(__tmp_2747 + 6.U) := __tmp_2748(55, 48)
        arrayRegFiles(__tmp_2747 + 7.U) := __tmp_2748(63, 56)

        CP := Cat(
          arrayRegFiles(SP + 0.U + 1.U),
          arrayRegFiles(SP + 0.U + 0.U)
        )

      }

    }

}


