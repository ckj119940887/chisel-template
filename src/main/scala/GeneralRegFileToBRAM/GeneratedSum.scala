package GeneralRegFileToBRAM
import chisel3._
import chisel3.util._
import chisel3.experimental._



class SumTest (val C_S_AXI_DATA_WIDTH:  Int = 32,
               val C_S_AXI_ADDR_WIDTH:  Int = 32,
               val ARRAY_REG_WIDTH:     Int = 8,
               val ARRAY_REG_DEPTH:     Int = 256,
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

        val __tmp_4531 = 10.U(32.W)
        val __tmp_4532 = (886747591.U(32.W)).asUInt
        arrayRegFiles(__tmp_4531 + 0.U) := __tmp_4532(7, 0)
        arrayRegFiles(__tmp_4531 + 1.U) := __tmp_4532(15, 8)
        arrayRegFiles(__tmp_4531 + 2.U) := __tmp_4532(23, 16)
        arrayRegFiles(__tmp_4531 + 3.U) := __tmp_4532(31, 24)

        val __tmp_4533 = 14.U(16.W)
        val __tmp_4534 = (8.S(64.W)).asUInt
        arrayRegFiles(__tmp_4533 + 0.U) := __tmp_4534(7, 0)
        arrayRegFiles(__tmp_4533 + 1.U) := __tmp_4534(15, 8)
        arrayRegFiles(__tmp_4533 + 2.U) := __tmp_4534(23, 16)
        arrayRegFiles(__tmp_4533 + 3.U) := __tmp_4534(31, 24)
        arrayRegFiles(__tmp_4533 + 4.U) := __tmp_4534(39, 32)
        arrayRegFiles(__tmp_4533 + 5.U) := __tmp_4534(47, 40)
        arrayRegFiles(__tmp_4533 + 6.U) := __tmp_4534(55, 48)
        arrayRegFiles(__tmp_4533 + 7.U) := __tmp_4534(63, 56)

        val __tmp_4535 = 28.U(16.W)
        val __tmp_4536 = (0.U(16.W)).asUInt
        arrayRegFiles(__tmp_4535 + 0.U) := __tmp_4536(7, 0)
        arrayRegFiles(__tmp_4535 + 1.U) := __tmp_4536(15, 8)

        CP := 4.U
      }

      is(4.U) {
        /*
        $0 = *(0: SP) [signed, Z, 8]  // $0 = $testNum
        goto .5
        */


        val __tmp_4537 = (0.U(16.W)).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_4537 + 7.U),
          arrayRegFiles(__tmp_4537 + 6.U),
          arrayRegFiles(__tmp_4537 + 5.U),
          arrayRegFiles(__tmp_4537 + 4.U),
          arrayRegFiles(__tmp_4537 + 3.U),
          arrayRegFiles(__tmp_4537 + 2.U),
          arrayRegFiles(__tmp_4537 + 1.U),
          arrayRegFiles(__tmp_4537 + 0.U)
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
        *SP = (9: CP) [unsigned, CP, 2]  // $ret@0 = 1363
        *(SP - (8: SP)) = ($0: Z) [signed, Z, 8]  // save $0 (Z)
        goto .8
        */


        val __tmp_4538 = SP
        val __tmp_4539 = (9.U(16.W)).asUInt
        arrayRegFiles(__tmp_4538 + 0.U) := __tmp_4539(7, 0)
        arrayRegFiles(__tmp_4538 + 1.U) := __tmp_4539(15, 8)

        val __tmp_4540 = (SP - 8.U(16.W))
        val __tmp_4541 = (generalRegFiles(0.U).asSInt).asUInt
        arrayRegFiles(__tmp_4540 + 0.U) := __tmp_4541(7, 0)
        arrayRegFiles(__tmp_4540 + 1.U) := __tmp_4541(15, 8)
        arrayRegFiles(__tmp_4540 + 2.U) := __tmp_4541(23, 16)
        arrayRegFiles(__tmp_4540 + 3.U) := __tmp_4541(31, 24)
        arrayRegFiles(__tmp_4540 + 4.U) := __tmp_4541(39, 32)
        arrayRegFiles(__tmp_4540 + 5.U) := __tmp_4541(47, 40)
        arrayRegFiles(__tmp_4540 + 6.U) := __tmp_4541(55, 48)
        arrayRegFiles(__tmp_4540 + 7.U) := __tmp_4541(63, 56)

        CP := 8.U
      }

      is(8.U) {
        /*
        decl $ret: CP [@0, 2]
        goto .27
        */


        CP := 27.U
      }

      is(9.U) {
        /*
        $0 = *(SP - (8: SP)) [signed, Z, 8]  // restore $0 (Z)
        goto .10
        */


        val __tmp_4542 = ((SP - 8.U(16.W))).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_4542 + 7.U),
          arrayRegFiles(__tmp_4542 + 6.U),
          arrayRegFiles(__tmp_4542 + 5.U),
          arrayRegFiles(__tmp_4542 + 4.U),
          arrayRegFiles(__tmp_4542 + 3.U),
          arrayRegFiles(__tmp_4542 + 2.U),
          arrayRegFiles(__tmp_4542 + 1.U),
          arrayRegFiles(__tmp_4542 + 0.U)
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


        val __tmp_4543 = (SP + 0.U(16.W))
        val __tmp_4544 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4543 + 0.U) := __tmp_4544(7, 0)

        val __tmp_4545 = (SP + 1.U(16.W))
        val __tmp_4546 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4545 + 0.U) := __tmp_4546(7, 0)

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
        SP = SP + 10
        goto .14
        */


        SP := SP + 10.U

        CP := 14.U
      }

      is(14.U) {
        /*
        *SP = (16: CP) [unsigned, CP, 2]  // $ret@0 = 1365
        *(SP - (8: SP)) = ($0: Z) [signed, Z, 8]  // save $0 (Z)
        goto .15
        */


        val __tmp_4547 = SP
        val __tmp_4548 = (16.U(16.W)).asUInt
        arrayRegFiles(__tmp_4547 + 0.U) := __tmp_4548(7, 0)
        arrayRegFiles(__tmp_4547 + 1.U) := __tmp_4548(15, 8)

        val __tmp_4549 = (SP - 8.U(16.W))
        val __tmp_4550 = (generalRegFiles(0.U).asSInt).asUInt
        arrayRegFiles(__tmp_4549 + 0.U) := __tmp_4550(7, 0)
        arrayRegFiles(__tmp_4549 + 1.U) := __tmp_4550(15, 8)
        arrayRegFiles(__tmp_4549 + 2.U) := __tmp_4550(23, 16)
        arrayRegFiles(__tmp_4549 + 3.U) := __tmp_4550(31, 24)
        arrayRegFiles(__tmp_4549 + 4.U) := __tmp_4550(39, 32)
        arrayRegFiles(__tmp_4549 + 5.U) := __tmp_4550(47, 40)
        arrayRegFiles(__tmp_4549 + 6.U) := __tmp_4550(55, 48)
        arrayRegFiles(__tmp_4549 + 7.U) := __tmp_4550(63, 56)

        CP := 15.U
      }

      is(15.U) {
        /*
        decl $ret: CP [@0, 2]
        goto .46
        */


        CP := 46.U
      }

      is(16.U) {
        /*
        $0 = *(SP - (8: SP)) [signed, Z, 8]  // restore $0 (Z)
        goto .17
        */


        val __tmp_4551 = ((SP - 8.U(16.W))).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_4551 + 7.U),
          arrayRegFiles(__tmp_4551 + 6.U),
          arrayRegFiles(__tmp_4551 + 5.U),
          arrayRegFiles(__tmp_4551 + 4.U),
          arrayRegFiles(__tmp_4551 + 3.U),
          arrayRegFiles(__tmp_4551 + 2.U),
          arrayRegFiles(__tmp_4551 + 1.U),
          arrayRegFiles(__tmp_4551 + 0.U)
        ).asSInt.pad(GENERAL_REG_WIDTH).asUInt

        CP := 17.U
      }

      is(17.U) {
        /*
        *(SP + (0: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $ret byte 0
        *(SP + (1: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $ret byte 1
        undecl $ret: CP [@0, 2]
        goto .18
        */


        val __tmp_4552 = (SP + 0.U(16.W))
        val __tmp_4553 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4552 + 0.U) := __tmp_4553(7, 0)

        val __tmp_4554 = (SP + 1.U(16.W))
        val __tmp_4555 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4554 + 0.U) := __tmp_4555(7, 0)

        CP := 18.U
      }

      is(18.U) {
        /*
        SP = SP - 10
        goto .19
        */


        SP := SP - 10.U

        CP := 19.U
      }

      is(19.U) {
        /*
        if ((($0: Z) < (0: Z)) | (($0: Z) ≡ (2: Z))) goto .20 else goto .26
        */


        CP := Mux((((generalRegFiles(0.U).asSInt < 0.S(64.W)).asUInt | (generalRegFiles(0.U).asSInt === 2.S(64.W)).asUInt).asUInt) === 1.U, 20.U, 26.U)
      }

      is(20.U) {
        /*
        SP = SP + 2
        goto .21
        */


        SP := SP + 2.U

        CP := 21.U
      }

      is(21.U) {
        /*
        *SP = (23: CP) [unsigned, CP, 2]  // $ret@0 = 1367
        goto .22
        */


        val __tmp_4556 = SP
        val __tmp_4557 = (23.U(16.W)).asUInt
        arrayRegFiles(__tmp_4556 + 0.U) := __tmp_4557(7, 0)
        arrayRegFiles(__tmp_4556 + 1.U) := __tmp_4557(15, 8)

        CP := 22.U
      }

      is(22.U) {
        /*
        decl $ret: CP [@0, 2]
        goto .65
        */


        CP := 65.U
      }

      is(23.U) {
        /*
        goto .24
        */


        CP := 24.U
      }

      is(24.U) {
        /*
        *(SP + (0: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $ret byte 0
        *(SP + (1: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $ret byte 1
        undecl $ret: CP [@0, 2]
        goto .25
        */


        val __tmp_4558 = (SP + 0.U(16.W))
        val __tmp_4559 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4558 + 0.U) := __tmp_4559(7, 0)

        val __tmp_4560 = (SP + 1.U(16.W))
        val __tmp_4561 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4560 + 0.U) := __tmp_4561(7, 0)

        CP := 25.U
      }

      is(25.U) {
        /*
        SP = SP - 2
        goto .26
        */


        SP := SP - 2.U

        CP := 26.U
      }

      is(26.U) {
        /*
        goto $ret@0
        */


        CP := Cat(
          arrayRegFiles(SP + 0.U + 1.U),
          arrayRegFiles(SP + 0.U + 0.U)
        )

      }

      is(27.U) {
        /*
        alloc $new@[13,15].0B95249C: IS[Z, Z] [@2, 36]
        $2 = (SP + (2: SP))
        *(SP + (2: SP)) = (2192300037: U32) [unsigned, U32, 4]  // sha3 type signature of IS[Z, Z]: 0x82ABD805
        *(SP + (6: SP)) = (0: Z) [signed, Z, 8]  // size of IS[Z, Z]()
        goto .28
        */



        generalRegFiles(2.U) := (SP + 2.U(16.W))

        val __tmp_4562 = (SP + 2.U(16.W))
        val __tmp_4563 = (BigInt("2192300037").U(32.W)).asUInt
        arrayRegFiles(__tmp_4562 + 0.U) := __tmp_4563(7, 0)
        arrayRegFiles(__tmp_4562 + 1.U) := __tmp_4563(15, 8)
        arrayRegFiles(__tmp_4562 + 2.U) := __tmp_4563(23, 16)
        arrayRegFiles(__tmp_4562 + 3.U) := __tmp_4563(31, 24)

        val __tmp_4564 = (SP + 6.U(16.W))
        val __tmp_4565 = (0.S(64.W)).asUInt
        arrayRegFiles(__tmp_4564 + 0.U) := __tmp_4565(7, 0)
        arrayRegFiles(__tmp_4564 + 1.U) := __tmp_4565(15, 8)
        arrayRegFiles(__tmp_4564 + 2.U) := __tmp_4565(23, 16)
        arrayRegFiles(__tmp_4564 + 3.U) := __tmp_4565(31, 24)
        arrayRegFiles(__tmp_4564 + 4.U) := __tmp_4565(39, 32)
        arrayRegFiles(__tmp_4564 + 5.U) := __tmp_4565(47, 40)
        arrayRegFiles(__tmp_4564 + 6.U) := __tmp_4565(55, 48)
        arrayRegFiles(__tmp_4564 + 7.U) := __tmp_4565(63, 56)

        CP := 28.U
      }

      is(28.U) {
        /*
        alloc sum$res@[13,11].810E887B: Z [@38, 8]
        goto .29
        */


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
        *SP = (32: CP) [unsigned, CP, 2]  // $ret@0 = 1369
        *(SP + (2: SP)) = (SP - (8: SP)) [unsigned, SP, 2]  // $res@2 = -8
        $29 = ($2: IS[Z, Z])
        $30 = (0: Z)
        $31 = (0: Z)
        goto .31
        */


        val __tmp_4566 = SP
        val __tmp_4567 = (32.U(16.W)).asUInt
        arrayRegFiles(__tmp_4566 + 0.U) := __tmp_4567(7, 0)
        arrayRegFiles(__tmp_4566 + 1.U) := __tmp_4567(15, 8)

        val __tmp_4568 = (SP + 2.U(16.W))
        val __tmp_4569 = ((SP - 8.U(16.W))).asUInt
        arrayRegFiles(__tmp_4568 + 0.U) := __tmp_4569(7, 0)
        arrayRegFiles(__tmp_4568 + 1.U) := __tmp_4569(15, 8)


        generalRegFiles(29.U) := generalRegFiles(2.U)


        generalRegFiles(30.U) := (0.S(64.W)).asUInt


        generalRegFiles(31.U) := (0.S(64.W)).asUInt

        CP := 31.U
      }

      is(31.U) {
        /*
        decl $ret: CP [@0, 2], $res: SP [@2, 10], a: IS[Z, Z] @$0, a: IS[Z, Z] [@12, 36], i: Z @$1, acc: Z @$2
        $0 = ($29: IS[Z, Z])
        $1 = ($30: Z)
        $2 = ($31: Z)
        goto .84
        */



        generalRegFiles(0.U) := generalRegFiles(29.U)


        generalRegFiles(1.U) := (generalRegFiles(30.U).asSInt).asUInt


        generalRegFiles(2.U) := (generalRegFiles(31.U).asSInt).asUInt

        CP := 84.U
      }

      is(32.U) {
        /*
        $3 = **(SP + (2: SP)) [signed, Z, 8]  // $3 = $res
        goto .33
        */


        val __tmp_4570 = (Cat(
          arrayRegFiles((SP + 2.U(16.W)) + 1.U),
          arrayRegFiles((SP + 2.U(16.W)) + 0.U)
        )).asUInt
        generalRegFiles(3.U) := Cat(
          arrayRegFiles(__tmp_4570 + 7.U),
          arrayRegFiles(__tmp_4570 + 6.U),
          arrayRegFiles(__tmp_4570 + 5.U),
          arrayRegFiles(__tmp_4570 + 4.U),
          arrayRegFiles(__tmp_4570 + 3.U),
          arrayRegFiles(__tmp_4570 + 2.U),
          arrayRegFiles(__tmp_4570 + 1.U),
          arrayRegFiles(__tmp_4570 + 0.U)
        ).asSInt.pad(GENERAL_REG_WIDTH).asUInt

        CP := 33.U
      }

      is(33.U) {
        /*
        *(SP + (12: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 0
        *(SP + (13: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 1
        *(SP + (14: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 2
        *(SP + (15: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 3
        *(SP + (16: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 4
        *(SP + (17: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 5
        *(SP + (18: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 6
        *(SP + (19: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 7
        *(SP + (20: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 8
        *(SP + (21: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 9
        *(SP + (22: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 10
        *(SP + (23: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 11
        *(SP + (24: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 12
        *(SP + (25: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 13
        *(SP + (26: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 14
        *(SP + (27: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 15
        *(SP + (28: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 16
        *(SP + (29: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 17
        *(SP + (30: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 18
        *(SP + (31: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 19
        *(SP + (32: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 20
        *(SP + (33: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 21
        *(SP + (34: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 22
        *(SP + (35: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 23
        *(SP + (36: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 24
        *(SP + (37: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 25
        *(SP + (38: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 26
        *(SP + (39: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 27
        *(SP + (40: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 28
        *(SP + (41: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 29
        *(SP + (42: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 30
        *(SP + (43: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 31
        *(SP + (44: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 32
        *(SP + (45: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 33
        *(SP + (46: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 34
        *(SP + (47: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 35
        *(SP + (2: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $res byte 0
        *(SP + (3: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $res byte 1
        *(SP + (4: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $res byte 2
        *(SP + (5: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $res byte 3
        *(SP + (6: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $res byte 4
        *(SP + (7: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $res byte 5
        *(SP + (8: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $res byte 6
        *(SP + (9: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $res byte 7
        *(SP + (10: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $res byte 8
        *(SP + (11: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $res byte 9
        *(SP + (0: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $ret byte 0
        *(SP + (1: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $ret byte 1
        undecl acc: Z @$2, i: Z @$1, a: IS[Z, Z] [@12, 36], a: IS[Z, Z] @$0, $res: SP [@2, 10], $ret: CP [@0, 2]
        goto .34
        */


        val __tmp_4571 = (SP + 12.U(16.W))
        val __tmp_4572 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4571 + 0.U) := __tmp_4572(7, 0)

        val __tmp_4573 = (SP + 13.U(16.W))
        val __tmp_4574 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4573 + 0.U) := __tmp_4574(7, 0)

        val __tmp_4575 = (SP + 14.U(16.W))
        val __tmp_4576 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4575 + 0.U) := __tmp_4576(7, 0)

        val __tmp_4577 = (SP + 15.U(16.W))
        val __tmp_4578 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4577 + 0.U) := __tmp_4578(7, 0)

        val __tmp_4579 = (SP + 16.U(16.W))
        val __tmp_4580 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4579 + 0.U) := __tmp_4580(7, 0)

        val __tmp_4581 = (SP + 17.U(16.W))
        val __tmp_4582 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4581 + 0.U) := __tmp_4582(7, 0)

        val __tmp_4583 = (SP + 18.U(16.W))
        val __tmp_4584 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4583 + 0.U) := __tmp_4584(7, 0)

        val __tmp_4585 = (SP + 19.U(16.W))
        val __tmp_4586 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4585 + 0.U) := __tmp_4586(7, 0)

        val __tmp_4587 = (SP + 20.U(16.W))
        val __tmp_4588 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4587 + 0.U) := __tmp_4588(7, 0)

        val __tmp_4589 = (SP + 21.U(16.W))
        val __tmp_4590 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4589 + 0.U) := __tmp_4590(7, 0)

        val __tmp_4591 = (SP + 22.U(16.W))
        val __tmp_4592 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4591 + 0.U) := __tmp_4592(7, 0)

        val __tmp_4593 = (SP + 23.U(16.W))
        val __tmp_4594 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4593 + 0.U) := __tmp_4594(7, 0)

        val __tmp_4595 = (SP + 24.U(16.W))
        val __tmp_4596 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4595 + 0.U) := __tmp_4596(7, 0)

        val __tmp_4597 = (SP + 25.U(16.W))
        val __tmp_4598 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4597 + 0.U) := __tmp_4598(7, 0)

        val __tmp_4599 = (SP + 26.U(16.W))
        val __tmp_4600 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4599 + 0.U) := __tmp_4600(7, 0)

        val __tmp_4601 = (SP + 27.U(16.W))
        val __tmp_4602 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4601 + 0.U) := __tmp_4602(7, 0)

        val __tmp_4603 = (SP + 28.U(16.W))
        val __tmp_4604 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4603 + 0.U) := __tmp_4604(7, 0)

        val __tmp_4605 = (SP + 29.U(16.W))
        val __tmp_4606 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4605 + 0.U) := __tmp_4606(7, 0)

        val __tmp_4607 = (SP + 30.U(16.W))
        val __tmp_4608 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4607 + 0.U) := __tmp_4608(7, 0)

        val __tmp_4609 = (SP + 31.U(16.W))
        val __tmp_4610 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4609 + 0.U) := __tmp_4610(7, 0)

        val __tmp_4611 = (SP + 32.U(16.W))
        val __tmp_4612 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4611 + 0.U) := __tmp_4612(7, 0)

        val __tmp_4613 = (SP + 33.U(16.W))
        val __tmp_4614 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4613 + 0.U) := __tmp_4614(7, 0)

        val __tmp_4615 = (SP + 34.U(16.W))
        val __tmp_4616 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4615 + 0.U) := __tmp_4616(7, 0)

        val __tmp_4617 = (SP + 35.U(16.W))
        val __tmp_4618 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4617 + 0.U) := __tmp_4618(7, 0)

        val __tmp_4619 = (SP + 36.U(16.W))
        val __tmp_4620 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4619 + 0.U) := __tmp_4620(7, 0)

        val __tmp_4621 = (SP + 37.U(16.W))
        val __tmp_4622 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4621 + 0.U) := __tmp_4622(7, 0)

        val __tmp_4623 = (SP + 38.U(16.W))
        val __tmp_4624 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4623 + 0.U) := __tmp_4624(7, 0)

        val __tmp_4625 = (SP + 39.U(16.W))
        val __tmp_4626 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4625 + 0.U) := __tmp_4626(7, 0)

        val __tmp_4627 = (SP + 40.U(16.W))
        val __tmp_4628 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4627 + 0.U) := __tmp_4628(7, 0)

        val __tmp_4629 = (SP + 41.U(16.W))
        val __tmp_4630 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4629 + 0.U) := __tmp_4630(7, 0)

        val __tmp_4631 = (SP + 42.U(16.W))
        val __tmp_4632 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4631 + 0.U) := __tmp_4632(7, 0)

        val __tmp_4633 = (SP + 43.U(16.W))
        val __tmp_4634 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4633 + 0.U) := __tmp_4634(7, 0)

        val __tmp_4635 = (SP + 44.U(16.W))
        val __tmp_4636 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4635 + 0.U) := __tmp_4636(7, 0)

        val __tmp_4637 = (SP + 45.U(16.W))
        val __tmp_4638 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4637 + 0.U) := __tmp_4638(7, 0)

        val __tmp_4639 = (SP + 46.U(16.W))
        val __tmp_4640 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4639 + 0.U) := __tmp_4640(7, 0)

        val __tmp_4641 = (SP + 47.U(16.W))
        val __tmp_4642 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4641 + 0.U) := __tmp_4642(7, 0)

        val __tmp_4643 = (SP + 2.U(16.W))
        val __tmp_4644 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4643 + 0.U) := __tmp_4644(7, 0)

        val __tmp_4645 = (SP + 3.U(16.W))
        val __tmp_4646 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4645 + 0.U) := __tmp_4646(7, 0)

        val __tmp_4647 = (SP + 4.U(16.W))
        val __tmp_4648 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4647 + 0.U) := __tmp_4648(7, 0)

        val __tmp_4649 = (SP + 5.U(16.W))
        val __tmp_4650 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4649 + 0.U) := __tmp_4650(7, 0)

        val __tmp_4651 = (SP + 6.U(16.W))
        val __tmp_4652 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4651 + 0.U) := __tmp_4652(7, 0)

        val __tmp_4653 = (SP + 7.U(16.W))
        val __tmp_4654 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4653 + 0.U) := __tmp_4654(7, 0)

        val __tmp_4655 = (SP + 8.U(16.W))
        val __tmp_4656 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4655 + 0.U) := __tmp_4656(7, 0)

        val __tmp_4657 = (SP + 9.U(16.W))
        val __tmp_4658 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4657 + 0.U) := __tmp_4658(7, 0)

        val __tmp_4659 = (SP + 10.U(16.W))
        val __tmp_4660 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4659 + 0.U) := __tmp_4660(7, 0)

        val __tmp_4661 = (SP + 11.U(16.W))
        val __tmp_4662 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4661 + 0.U) := __tmp_4662(7, 0)

        val __tmp_4663 = (SP + 0.U(16.W))
        val __tmp_4664 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4663 + 0.U) := __tmp_4664(7, 0)

        val __tmp_4665 = (SP + 1.U(16.W))
        val __tmp_4666 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4665 + 0.U) := __tmp_4666(7, 0)

        CP := 34.U
      }

      is(34.U) {
        /*
        SP = SP - 46
        goto .35
        */


        SP := SP - 46.U

        CP := 35.U
      }

      is(35.U) {
        /*
        *(SP + (2: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[13,15].0B95249C byte 0
        *(SP + (3: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[13,15].0B95249C byte 1
        *(SP + (4: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[13,15].0B95249C byte 2
        *(SP + (5: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[13,15].0B95249C byte 3
        *(SP + (6: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[13,15].0B95249C byte 4
        *(SP + (7: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[13,15].0B95249C byte 5
        *(SP + (8: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[13,15].0B95249C byte 6
        *(SP + (9: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[13,15].0B95249C byte 7
        *(SP + (10: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[13,15].0B95249C byte 8
        *(SP + (11: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[13,15].0B95249C byte 9
        *(SP + (12: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[13,15].0B95249C byte 10
        *(SP + (13: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[13,15].0B95249C byte 11
        *(SP + (14: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[13,15].0B95249C byte 12
        *(SP + (15: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[13,15].0B95249C byte 13
        *(SP + (16: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[13,15].0B95249C byte 14
        *(SP + (17: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[13,15].0B95249C byte 15
        *(SP + (18: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[13,15].0B95249C byte 16
        *(SP + (19: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[13,15].0B95249C byte 17
        *(SP + (20: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[13,15].0B95249C byte 18
        *(SP + (21: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[13,15].0B95249C byte 19
        *(SP + (22: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[13,15].0B95249C byte 20
        *(SP + (23: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[13,15].0B95249C byte 21
        *(SP + (24: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[13,15].0B95249C byte 22
        *(SP + (25: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[13,15].0B95249C byte 23
        *(SP + (26: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[13,15].0B95249C byte 24
        *(SP + (27: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[13,15].0B95249C byte 25
        *(SP + (28: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[13,15].0B95249C byte 26
        *(SP + (29: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[13,15].0B95249C byte 27
        *(SP + (30: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[13,15].0B95249C byte 28
        *(SP + (31: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[13,15].0B95249C byte 29
        *(SP + (32: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[13,15].0B95249C byte 30
        *(SP + (33: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[13,15].0B95249C byte 31
        *(SP + (34: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[13,15].0B95249C byte 32
        *(SP + (35: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[13,15].0B95249C byte 33
        *(SP + (36: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[13,15].0B95249C byte 34
        *(SP + (37: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[13,15].0B95249C byte 35
        unalloc $new@[13,15].0B95249C: IS[Z, Z] [@2, 36]
        goto .36
        */


        val __tmp_4667 = (SP + 2.U(16.W))
        val __tmp_4668 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4667 + 0.U) := __tmp_4668(7, 0)

        val __tmp_4669 = (SP + 3.U(16.W))
        val __tmp_4670 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4669 + 0.U) := __tmp_4670(7, 0)

        val __tmp_4671 = (SP + 4.U(16.W))
        val __tmp_4672 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4671 + 0.U) := __tmp_4672(7, 0)

        val __tmp_4673 = (SP + 5.U(16.W))
        val __tmp_4674 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4673 + 0.U) := __tmp_4674(7, 0)

        val __tmp_4675 = (SP + 6.U(16.W))
        val __tmp_4676 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4675 + 0.U) := __tmp_4676(7, 0)

        val __tmp_4677 = (SP + 7.U(16.W))
        val __tmp_4678 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4677 + 0.U) := __tmp_4678(7, 0)

        val __tmp_4679 = (SP + 8.U(16.W))
        val __tmp_4680 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4679 + 0.U) := __tmp_4680(7, 0)

        val __tmp_4681 = (SP + 9.U(16.W))
        val __tmp_4682 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4681 + 0.U) := __tmp_4682(7, 0)

        val __tmp_4683 = (SP + 10.U(16.W))
        val __tmp_4684 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4683 + 0.U) := __tmp_4684(7, 0)

        val __tmp_4685 = (SP + 11.U(16.W))
        val __tmp_4686 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4685 + 0.U) := __tmp_4686(7, 0)

        val __tmp_4687 = (SP + 12.U(16.W))
        val __tmp_4688 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4687 + 0.U) := __tmp_4688(7, 0)

        val __tmp_4689 = (SP + 13.U(16.W))
        val __tmp_4690 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4689 + 0.U) := __tmp_4690(7, 0)

        val __tmp_4691 = (SP + 14.U(16.W))
        val __tmp_4692 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4691 + 0.U) := __tmp_4692(7, 0)

        val __tmp_4693 = (SP + 15.U(16.W))
        val __tmp_4694 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4693 + 0.U) := __tmp_4694(7, 0)

        val __tmp_4695 = (SP + 16.U(16.W))
        val __tmp_4696 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4695 + 0.U) := __tmp_4696(7, 0)

        val __tmp_4697 = (SP + 17.U(16.W))
        val __tmp_4698 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4697 + 0.U) := __tmp_4698(7, 0)

        val __tmp_4699 = (SP + 18.U(16.W))
        val __tmp_4700 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4699 + 0.U) := __tmp_4700(7, 0)

        val __tmp_4701 = (SP + 19.U(16.W))
        val __tmp_4702 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4701 + 0.U) := __tmp_4702(7, 0)

        val __tmp_4703 = (SP + 20.U(16.W))
        val __tmp_4704 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4703 + 0.U) := __tmp_4704(7, 0)

        val __tmp_4705 = (SP + 21.U(16.W))
        val __tmp_4706 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4705 + 0.U) := __tmp_4706(7, 0)

        val __tmp_4707 = (SP + 22.U(16.W))
        val __tmp_4708 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4707 + 0.U) := __tmp_4708(7, 0)

        val __tmp_4709 = (SP + 23.U(16.W))
        val __tmp_4710 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4709 + 0.U) := __tmp_4710(7, 0)

        val __tmp_4711 = (SP + 24.U(16.W))
        val __tmp_4712 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4711 + 0.U) := __tmp_4712(7, 0)

        val __tmp_4713 = (SP + 25.U(16.W))
        val __tmp_4714 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4713 + 0.U) := __tmp_4714(7, 0)

        val __tmp_4715 = (SP + 26.U(16.W))
        val __tmp_4716 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4715 + 0.U) := __tmp_4716(7, 0)

        val __tmp_4717 = (SP + 27.U(16.W))
        val __tmp_4718 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4717 + 0.U) := __tmp_4718(7, 0)

        val __tmp_4719 = (SP + 28.U(16.W))
        val __tmp_4720 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4719 + 0.U) := __tmp_4720(7, 0)

        val __tmp_4721 = (SP + 29.U(16.W))
        val __tmp_4722 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4721 + 0.U) := __tmp_4722(7, 0)

        val __tmp_4723 = (SP + 30.U(16.W))
        val __tmp_4724 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4723 + 0.U) := __tmp_4724(7, 0)

        val __tmp_4725 = (SP + 31.U(16.W))
        val __tmp_4726 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4725 + 0.U) := __tmp_4726(7, 0)

        val __tmp_4727 = (SP + 32.U(16.W))
        val __tmp_4728 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4727 + 0.U) := __tmp_4728(7, 0)

        val __tmp_4729 = (SP + 33.U(16.W))
        val __tmp_4730 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4729 + 0.U) := __tmp_4730(7, 0)

        val __tmp_4731 = (SP + 34.U(16.W))
        val __tmp_4732 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4731 + 0.U) := __tmp_4732(7, 0)

        val __tmp_4733 = (SP + 35.U(16.W))
        val __tmp_4734 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4733 + 0.U) := __tmp_4734(7, 0)

        val __tmp_4735 = (SP + 36.U(16.W))
        val __tmp_4736 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4735 + 0.U) := __tmp_4736(7, 0)

        val __tmp_4737 = (SP + 37.U(16.W))
        val __tmp_4738 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4737 + 0.U) := __tmp_4738(7, 0)

        CP := 36.U
      }

      is(36.U) {
        /*
        alloc printS64$res@[13,11].810E887B: U64 [@2, 8]
        goto .37
        */


        CP := 37.U
      }

      is(37.U) {
        /*
        SP = SP + 46
        goto .38
        */


        SP := SP + 46.U

        CP := 38.U
      }

      is(38.U) {
        /*
        *SP = (40: CP) [unsigned, CP, 2]  // $ret@0 = 1371
        *(SP + (2: SP)) = (SP - (44: SP)) [unsigned, SP, 2]  // $res@2 = -44
        $91 = (8: SP)
        $92 = DP
        $93 = (7: anvil.PrinterIndex.U)
        $94 = (($3: Z) as S64)
        goto .39
        */


        val __tmp_4739 = SP
        val __tmp_4740 = (40.U(16.W)).asUInt
        arrayRegFiles(__tmp_4739 + 0.U) := __tmp_4740(7, 0)
        arrayRegFiles(__tmp_4739 + 1.U) := __tmp_4740(15, 8)

        val __tmp_4741 = (SP + 2.U(16.W))
        val __tmp_4742 = ((SP - 44.U(16.W))).asUInt
        arrayRegFiles(__tmp_4741 + 0.U) := __tmp_4742(7, 0)
        arrayRegFiles(__tmp_4741 + 1.U) := __tmp_4742(15, 8)


        generalRegFiles(91.U) := 8.U(16.W)


        generalRegFiles(92.U) := DP


        generalRegFiles(93.U) := 7.U(64.W)


        generalRegFiles(94.U) := (generalRegFiles(3.U).asSInt.asSInt).asUInt

        CP := 39.U
      }

      is(39.U) {
        /*
        decl $ret: CP [@0, 2], $res: SP [@2, 2], buffer: MS[anvil.PrinterIndex.U, U8] @$0, index: anvil.PrinterIndex.U @$1, mask: anvil.PrinterIndex.U @$2, n: S64 @$3
        $0 = ($91: MS[anvil.PrinterIndex.U, U8])
        $1 = ($92: anvil.PrinterIndex.U)
        $2 = ($93: anvil.PrinterIndex.U)
        $3 = ($94: S64)
        goto .101
        */



        generalRegFiles(0.U) := generalRegFiles(91.U)


        generalRegFiles(1.U) := generalRegFiles(92.U)


        generalRegFiles(2.U) := generalRegFiles(93.U)


        generalRegFiles(3.U) := (generalRegFiles(94.U).asSInt).asUInt

        CP := 101.U
      }

      is(40.U) {
        /*
        $4 = **(SP + (2: SP)) [unsigned, U64, 8]  // $4 = $res
        goto .41
        */


        val __tmp_4743 = (Cat(
          arrayRegFiles((SP + 2.U(16.W)) + 1.U),
          arrayRegFiles((SP + 2.U(16.W)) + 0.U)
        )).asUInt
        generalRegFiles(4.U) := Cat(
          arrayRegFiles(__tmp_4743 + 7.U),
          arrayRegFiles(__tmp_4743 + 6.U),
          arrayRegFiles(__tmp_4743 + 5.U),
          arrayRegFiles(__tmp_4743 + 4.U),
          arrayRegFiles(__tmp_4743 + 3.U),
          arrayRegFiles(__tmp_4743 + 2.U),
          arrayRegFiles(__tmp_4743 + 1.U),
          arrayRegFiles(__tmp_4743 + 0.U)
        ).asUInt

        CP := 41.U
      }

      is(41.U) {
        /*
        *(SP + (2: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $res byte 0
        *(SP + (3: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $res byte 1
        *(SP + (0: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $ret byte 0
        *(SP + (1: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $ret byte 1
        undecl n: S64 @$3, mask: anvil.PrinterIndex.U @$2, index: anvil.PrinterIndex.U @$1, buffer: MS[anvil.PrinterIndex.U, U8] @$0, $res: SP [@2, 2], $ret: CP [@0, 2]
        goto .42
        */


        val __tmp_4744 = (SP + 2.U(16.W))
        val __tmp_4745 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4744 + 0.U) := __tmp_4745(7, 0)

        val __tmp_4746 = (SP + 3.U(16.W))
        val __tmp_4747 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4746 + 0.U) := __tmp_4747(7, 0)

        val __tmp_4748 = (SP + 0.U(16.W))
        val __tmp_4749 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4748 + 0.U) := __tmp_4749(7, 0)

        val __tmp_4750 = (SP + 1.U(16.W))
        val __tmp_4751 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4750 + 0.U) := __tmp_4751(7, 0)

        CP := 42.U
      }

      is(42.U) {
        /*
        SP = SP - 46
        goto .43
        */


        SP := SP - 46.U

        CP := 43.U
      }

      is(43.U) {
        /*
        DP = DP + (($4: U64) as DP)
        goto .44
        */


        DP := DP + generalRegFiles(4.U).asUInt

        CP := 44.U
      }

      is(44.U) {
        /*
        *(((8: SP) + (12: SP)) + ((DP & (7: DP)) as SP)) = (10: U8) [unsigned, U8, 1]  // $display((DP & (7: DP))) = (10: U8)
        goto .45
        */


        val __tmp_4752 = ((8.U(16.W) + 12.U(16.W)) + (DP & 7.U(64.W)).asUInt)
        val __tmp_4753 = (10.U(8.W)).asUInt
        arrayRegFiles(__tmp_4752 + 0.U) := __tmp_4753(7, 0)

        CP := 45.U
      }

      is(45.U) {
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

      is(46.U) {
        /*
        alloc $new@[17,15].CF927D4F: IS[Z, Z] [@2, 36]
        $2 = (SP + (2: SP))
        *(SP + (2: SP)) = (2192300037: U32) [unsigned, U32, 4]  // sha3 type signature of IS[Z, Z]: 0x82ABD805
        *(SP + (6: SP)) = (2: Z) [signed, Z, 8]  // size of IS[Z, Z]((1: Z), (2: Z))
        goto .47
        */



        generalRegFiles(2.U) := (SP + 2.U(16.W))

        val __tmp_4754 = (SP + 2.U(16.W))
        val __tmp_4755 = (BigInt("2192300037").U(32.W)).asUInt
        arrayRegFiles(__tmp_4754 + 0.U) := __tmp_4755(7, 0)
        arrayRegFiles(__tmp_4754 + 1.U) := __tmp_4755(15, 8)
        arrayRegFiles(__tmp_4754 + 2.U) := __tmp_4755(23, 16)
        arrayRegFiles(__tmp_4754 + 3.U) := __tmp_4755(31, 24)

        val __tmp_4756 = (SP + 6.U(16.W))
        val __tmp_4757 = (2.S(64.W)).asUInt
        arrayRegFiles(__tmp_4756 + 0.U) := __tmp_4757(7, 0)
        arrayRegFiles(__tmp_4756 + 1.U) := __tmp_4757(15, 8)
        arrayRegFiles(__tmp_4756 + 2.U) := __tmp_4757(23, 16)
        arrayRegFiles(__tmp_4756 + 3.U) := __tmp_4757(31, 24)
        arrayRegFiles(__tmp_4756 + 4.U) := __tmp_4757(39, 32)
        arrayRegFiles(__tmp_4756 + 5.U) := __tmp_4757(47, 40)
        arrayRegFiles(__tmp_4756 + 6.U) := __tmp_4757(55, 48)
        arrayRegFiles(__tmp_4756 + 7.U) := __tmp_4757(63, 56)

        CP := 47.U
      }

      is(47.U) {
        /*
        *((($2: IS[Z, Z]) + (12: SP)) + (((0: Z) as SP) * (8: SP))) = (1: Z) [signed, Z, 8]  // ($2: IS[Z, Z])((0: Z)) = (1: Z)
        *((($2: IS[Z, Z]) + (12: SP)) + (((1: Z) as SP) * (8: SP))) = (2: Z) [signed, Z, 8]  // ($2: IS[Z, Z])((1: Z)) = (2: Z)
        alloc sum$res@[17,11].365E3C94: Z [@38, 8]
        goto .48
        */


        val __tmp_4758 = ((generalRegFiles(2.U) + 12.U(16.W)) + (0.S(64.W).asUInt * 8.U(16.W)))
        val __tmp_4759 = (1.S(64.W)).asUInt
        arrayRegFiles(__tmp_4758 + 0.U) := __tmp_4759(7, 0)
        arrayRegFiles(__tmp_4758 + 1.U) := __tmp_4759(15, 8)
        arrayRegFiles(__tmp_4758 + 2.U) := __tmp_4759(23, 16)
        arrayRegFiles(__tmp_4758 + 3.U) := __tmp_4759(31, 24)
        arrayRegFiles(__tmp_4758 + 4.U) := __tmp_4759(39, 32)
        arrayRegFiles(__tmp_4758 + 5.U) := __tmp_4759(47, 40)
        arrayRegFiles(__tmp_4758 + 6.U) := __tmp_4759(55, 48)
        arrayRegFiles(__tmp_4758 + 7.U) := __tmp_4759(63, 56)

        val __tmp_4760 = ((generalRegFiles(2.U) + 12.U(16.W)) + (1.S(64.W).asUInt * 8.U(16.W)))
        val __tmp_4761 = (2.S(64.W)).asUInt
        arrayRegFiles(__tmp_4760 + 0.U) := __tmp_4761(7, 0)
        arrayRegFiles(__tmp_4760 + 1.U) := __tmp_4761(15, 8)
        arrayRegFiles(__tmp_4760 + 2.U) := __tmp_4761(23, 16)
        arrayRegFiles(__tmp_4760 + 3.U) := __tmp_4761(31, 24)
        arrayRegFiles(__tmp_4760 + 4.U) := __tmp_4761(39, 32)
        arrayRegFiles(__tmp_4760 + 5.U) := __tmp_4761(47, 40)
        arrayRegFiles(__tmp_4760 + 6.U) := __tmp_4761(55, 48)
        arrayRegFiles(__tmp_4760 + 7.U) := __tmp_4761(63, 56)

        CP := 48.U
      }

      is(48.U) {
        /*
        SP = SP + 46
        goto .49
        */


        SP := SP + 46.U

        CP := 49.U
      }

      is(49.U) {
        /*
        *SP = (51: CP) [unsigned, CP, 2]  // $ret@0 = 1373
        *(SP + (2: SP)) = (SP - (8: SP)) [unsigned, SP, 2]  // $res@2 = -8
        $29 = ($2: IS[Z, Z])
        $30 = (0: Z)
        $31 = (0: Z)
        goto .50
        */


        val __tmp_4762 = SP
        val __tmp_4763 = (51.U(16.W)).asUInt
        arrayRegFiles(__tmp_4762 + 0.U) := __tmp_4763(7, 0)
        arrayRegFiles(__tmp_4762 + 1.U) := __tmp_4763(15, 8)

        val __tmp_4764 = (SP + 2.U(16.W))
        val __tmp_4765 = ((SP - 8.U(16.W))).asUInt
        arrayRegFiles(__tmp_4764 + 0.U) := __tmp_4765(7, 0)
        arrayRegFiles(__tmp_4764 + 1.U) := __tmp_4765(15, 8)


        generalRegFiles(29.U) := generalRegFiles(2.U)


        generalRegFiles(30.U) := (0.S(64.W)).asUInt


        generalRegFiles(31.U) := (0.S(64.W)).asUInt

        CP := 50.U
      }

      is(50.U) {
        /*
        decl $ret: CP [@0, 2], $res: SP [@2, 10], a: IS[Z, Z] @$0, a: IS[Z, Z] [@12, 36], i: Z @$1, acc: Z @$2
        $0 = ($29: IS[Z, Z])
        $1 = ($30: Z)
        $2 = ($31: Z)
        goto .84
        */



        generalRegFiles(0.U) := generalRegFiles(29.U)


        generalRegFiles(1.U) := (generalRegFiles(30.U).asSInt).asUInt


        generalRegFiles(2.U) := (generalRegFiles(31.U).asSInt).asUInt

        CP := 84.U
      }

      is(51.U) {
        /*
        $3 = **(SP + (2: SP)) [signed, Z, 8]  // $3 = $res
        goto .52
        */


        val __tmp_4766 = (Cat(
          arrayRegFiles((SP + 2.U(16.W)) + 1.U),
          arrayRegFiles((SP + 2.U(16.W)) + 0.U)
        )).asUInt
        generalRegFiles(3.U) := Cat(
          arrayRegFiles(__tmp_4766 + 7.U),
          arrayRegFiles(__tmp_4766 + 6.U),
          arrayRegFiles(__tmp_4766 + 5.U),
          arrayRegFiles(__tmp_4766 + 4.U),
          arrayRegFiles(__tmp_4766 + 3.U),
          arrayRegFiles(__tmp_4766 + 2.U),
          arrayRegFiles(__tmp_4766 + 1.U),
          arrayRegFiles(__tmp_4766 + 0.U)
        ).asSInt.pad(GENERAL_REG_WIDTH).asUInt

        CP := 52.U
      }

      is(52.U) {
        /*
        *(SP + (12: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 0
        *(SP + (13: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 1
        *(SP + (14: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 2
        *(SP + (15: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 3
        *(SP + (16: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 4
        *(SP + (17: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 5
        *(SP + (18: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 6
        *(SP + (19: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 7
        *(SP + (20: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 8
        *(SP + (21: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 9
        *(SP + (22: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 10
        *(SP + (23: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 11
        *(SP + (24: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 12
        *(SP + (25: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 13
        *(SP + (26: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 14
        *(SP + (27: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 15
        *(SP + (28: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 16
        *(SP + (29: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 17
        *(SP + (30: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 18
        *(SP + (31: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 19
        *(SP + (32: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 20
        *(SP + (33: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 21
        *(SP + (34: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 22
        *(SP + (35: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 23
        *(SP + (36: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 24
        *(SP + (37: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 25
        *(SP + (38: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 26
        *(SP + (39: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 27
        *(SP + (40: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 28
        *(SP + (41: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 29
        *(SP + (42: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 30
        *(SP + (43: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 31
        *(SP + (44: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 32
        *(SP + (45: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 33
        *(SP + (46: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 34
        *(SP + (47: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 35
        *(SP + (2: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $res byte 0
        *(SP + (3: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $res byte 1
        *(SP + (4: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $res byte 2
        *(SP + (5: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $res byte 3
        *(SP + (6: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $res byte 4
        *(SP + (7: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $res byte 5
        *(SP + (8: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $res byte 6
        *(SP + (9: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $res byte 7
        *(SP + (10: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $res byte 8
        *(SP + (11: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $res byte 9
        *(SP + (0: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $ret byte 0
        *(SP + (1: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $ret byte 1
        undecl acc: Z @$2, i: Z @$1, a: IS[Z, Z] [@12, 36], a: IS[Z, Z] @$0, $res: SP [@2, 10], $ret: CP [@0, 2]
        goto .53
        */


        val __tmp_4767 = (SP + 12.U(16.W))
        val __tmp_4768 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4767 + 0.U) := __tmp_4768(7, 0)

        val __tmp_4769 = (SP + 13.U(16.W))
        val __tmp_4770 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4769 + 0.U) := __tmp_4770(7, 0)

        val __tmp_4771 = (SP + 14.U(16.W))
        val __tmp_4772 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4771 + 0.U) := __tmp_4772(7, 0)

        val __tmp_4773 = (SP + 15.U(16.W))
        val __tmp_4774 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4773 + 0.U) := __tmp_4774(7, 0)

        val __tmp_4775 = (SP + 16.U(16.W))
        val __tmp_4776 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4775 + 0.U) := __tmp_4776(7, 0)

        val __tmp_4777 = (SP + 17.U(16.W))
        val __tmp_4778 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4777 + 0.U) := __tmp_4778(7, 0)

        val __tmp_4779 = (SP + 18.U(16.W))
        val __tmp_4780 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4779 + 0.U) := __tmp_4780(7, 0)

        val __tmp_4781 = (SP + 19.U(16.W))
        val __tmp_4782 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4781 + 0.U) := __tmp_4782(7, 0)

        val __tmp_4783 = (SP + 20.U(16.W))
        val __tmp_4784 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4783 + 0.U) := __tmp_4784(7, 0)

        val __tmp_4785 = (SP + 21.U(16.W))
        val __tmp_4786 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4785 + 0.U) := __tmp_4786(7, 0)

        val __tmp_4787 = (SP + 22.U(16.W))
        val __tmp_4788 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4787 + 0.U) := __tmp_4788(7, 0)

        val __tmp_4789 = (SP + 23.U(16.W))
        val __tmp_4790 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4789 + 0.U) := __tmp_4790(7, 0)

        val __tmp_4791 = (SP + 24.U(16.W))
        val __tmp_4792 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4791 + 0.U) := __tmp_4792(7, 0)

        val __tmp_4793 = (SP + 25.U(16.W))
        val __tmp_4794 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4793 + 0.U) := __tmp_4794(7, 0)

        val __tmp_4795 = (SP + 26.U(16.W))
        val __tmp_4796 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4795 + 0.U) := __tmp_4796(7, 0)

        val __tmp_4797 = (SP + 27.U(16.W))
        val __tmp_4798 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4797 + 0.U) := __tmp_4798(7, 0)

        val __tmp_4799 = (SP + 28.U(16.W))
        val __tmp_4800 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4799 + 0.U) := __tmp_4800(7, 0)

        val __tmp_4801 = (SP + 29.U(16.W))
        val __tmp_4802 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4801 + 0.U) := __tmp_4802(7, 0)

        val __tmp_4803 = (SP + 30.U(16.W))
        val __tmp_4804 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4803 + 0.U) := __tmp_4804(7, 0)

        val __tmp_4805 = (SP + 31.U(16.W))
        val __tmp_4806 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4805 + 0.U) := __tmp_4806(7, 0)

        val __tmp_4807 = (SP + 32.U(16.W))
        val __tmp_4808 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4807 + 0.U) := __tmp_4808(7, 0)

        val __tmp_4809 = (SP + 33.U(16.W))
        val __tmp_4810 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4809 + 0.U) := __tmp_4810(7, 0)

        val __tmp_4811 = (SP + 34.U(16.W))
        val __tmp_4812 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4811 + 0.U) := __tmp_4812(7, 0)

        val __tmp_4813 = (SP + 35.U(16.W))
        val __tmp_4814 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4813 + 0.U) := __tmp_4814(7, 0)

        val __tmp_4815 = (SP + 36.U(16.W))
        val __tmp_4816 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4815 + 0.U) := __tmp_4816(7, 0)

        val __tmp_4817 = (SP + 37.U(16.W))
        val __tmp_4818 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4817 + 0.U) := __tmp_4818(7, 0)

        val __tmp_4819 = (SP + 38.U(16.W))
        val __tmp_4820 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4819 + 0.U) := __tmp_4820(7, 0)

        val __tmp_4821 = (SP + 39.U(16.W))
        val __tmp_4822 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4821 + 0.U) := __tmp_4822(7, 0)

        val __tmp_4823 = (SP + 40.U(16.W))
        val __tmp_4824 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4823 + 0.U) := __tmp_4824(7, 0)

        val __tmp_4825 = (SP + 41.U(16.W))
        val __tmp_4826 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4825 + 0.U) := __tmp_4826(7, 0)

        val __tmp_4827 = (SP + 42.U(16.W))
        val __tmp_4828 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4827 + 0.U) := __tmp_4828(7, 0)

        val __tmp_4829 = (SP + 43.U(16.W))
        val __tmp_4830 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4829 + 0.U) := __tmp_4830(7, 0)

        val __tmp_4831 = (SP + 44.U(16.W))
        val __tmp_4832 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4831 + 0.U) := __tmp_4832(7, 0)

        val __tmp_4833 = (SP + 45.U(16.W))
        val __tmp_4834 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4833 + 0.U) := __tmp_4834(7, 0)

        val __tmp_4835 = (SP + 46.U(16.W))
        val __tmp_4836 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4835 + 0.U) := __tmp_4836(7, 0)

        val __tmp_4837 = (SP + 47.U(16.W))
        val __tmp_4838 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4837 + 0.U) := __tmp_4838(7, 0)

        val __tmp_4839 = (SP + 2.U(16.W))
        val __tmp_4840 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4839 + 0.U) := __tmp_4840(7, 0)

        val __tmp_4841 = (SP + 3.U(16.W))
        val __tmp_4842 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4841 + 0.U) := __tmp_4842(7, 0)

        val __tmp_4843 = (SP + 4.U(16.W))
        val __tmp_4844 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4843 + 0.U) := __tmp_4844(7, 0)

        val __tmp_4845 = (SP + 5.U(16.W))
        val __tmp_4846 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4845 + 0.U) := __tmp_4846(7, 0)

        val __tmp_4847 = (SP + 6.U(16.W))
        val __tmp_4848 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4847 + 0.U) := __tmp_4848(7, 0)

        val __tmp_4849 = (SP + 7.U(16.W))
        val __tmp_4850 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4849 + 0.U) := __tmp_4850(7, 0)

        val __tmp_4851 = (SP + 8.U(16.W))
        val __tmp_4852 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4851 + 0.U) := __tmp_4852(7, 0)

        val __tmp_4853 = (SP + 9.U(16.W))
        val __tmp_4854 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4853 + 0.U) := __tmp_4854(7, 0)

        val __tmp_4855 = (SP + 10.U(16.W))
        val __tmp_4856 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4855 + 0.U) := __tmp_4856(7, 0)

        val __tmp_4857 = (SP + 11.U(16.W))
        val __tmp_4858 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4857 + 0.U) := __tmp_4858(7, 0)

        val __tmp_4859 = (SP + 0.U(16.W))
        val __tmp_4860 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4859 + 0.U) := __tmp_4860(7, 0)

        val __tmp_4861 = (SP + 1.U(16.W))
        val __tmp_4862 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4861 + 0.U) := __tmp_4862(7, 0)

        CP := 53.U
      }

      is(53.U) {
        /*
        SP = SP - 46
        goto .54
        */


        SP := SP - 46.U

        CP := 54.U
      }

      is(54.U) {
        /*
        *(SP + (2: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[17,15].CF927D4F byte 0
        *(SP + (3: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[17,15].CF927D4F byte 1
        *(SP + (4: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[17,15].CF927D4F byte 2
        *(SP + (5: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[17,15].CF927D4F byte 3
        *(SP + (6: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[17,15].CF927D4F byte 4
        *(SP + (7: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[17,15].CF927D4F byte 5
        *(SP + (8: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[17,15].CF927D4F byte 6
        *(SP + (9: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[17,15].CF927D4F byte 7
        *(SP + (10: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[17,15].CF927D4F byte 8
        *(SP + (11: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[17,15].CF927D4F byte 9
        *(SP + (12: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[17,15].CF927D4F byte 10
        *(SP + (13: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[17,15].CF927D4F byte 11
        *(SP + (14: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[17,15].CF927D4F byte 12
        *(SP + (15: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[17,15].CF927D4F byte 13
        *(SP + (16: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[17,15].CF927D4F byte 14
        *(SP + (17: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[17,15].CF927D4F byte 15
        *(SP + (18: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[17,15].CF927D4F byte 16
        *(SP + (19: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[17,15].CF927D4F byte 17
        *(SP + (20: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[17,15].CF927D4F byte 18
        *(SP + (21: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[17,15].CF927D4F byte 19
        *(SP + (22: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[17,15].CF927D4F byte 20
        *(SP + (23: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[17,15].CF927D4F byte 21
        *(SP + (24: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[17,15].CF927D4F byte 22
        *(SP + (25: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[17,15].CF927D4F byte 23
        *(SP + (26: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[17,15].CF927D4F byte 24
        *(SP + (27: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[17,15].CF927D4F byte 25
        *(SP + (28: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[17,15].CF927D4F byte 26
        *(SP + (29: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[17,15].CF927D4F byte 27
        *(SP + (30: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[17,15].CF927D4F byte 28
        *(SP + (31: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[17,15].CF927D4F byte 29
        *(SP + (32: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[17,15].CF927D4F byte 30
        *(SP + (33: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[17,15].CF927D4F byte 31
        *(SP + (34: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[17,15].CF927D4F byte 32
        *(SP + (35: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[17,15].CF927D4F byte 33
        *(SP + (36: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[17,15].CF927D4F byte 34
        *(SP + (37: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[17,15].CF927D4F byte 35
        unalloc $new@[17,15].CF927D4F: IS[Z, Z] [@2, 36]
        goto .55
        */


        val __tmp_4863 = (SP + 2.U(16.W))
        val __tmp_4864 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4863 + 0.U) := __tmp_4864(7, 0)

        val __tmp_4865 = (SP + 3.U(16.W))
        val __tmp_4866 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4865 + 0.U) := __tmp_4866(7, 0)

        val __tmp_4867 = (SP + 4.U(16.W))
        val __tmp_4868 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4867 + 0.U) := __tmp_4868(7, 0)

        val __tmp_4869 = (SP + 5.U(16.W))
        val __tmp_4870 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4869 + 0.U) := __tmp_4870(7, 0)

        val __tmp_4871 = (SP + 6.U(16.W))
        val __tmp_4872 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4871 + 0.U) := __tmp_4872(7, 0)

        val __tmp_4873 = (SP + 7.U(16.W))
        val __tmp_4874 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4873 + 0.U) := __tmp_4874(7, 0)

        val __tmp_4875 = (SP + 8.U(16.W))
        val __tmp_4876 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4875 + 0.U) := __tmp_4876(7, 0)

        val __tmp_4877 = (SP + 9.U(16.W))
        val __tmp_4878 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4877 + 0.U) := __tmp_4878(7, 0)

        val __tmp_4879 = (SP + 10.U(16.W))
        val __tmp_4880 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4879 + 0.U) := __tmp_4880(7, 0)

        val __tmp_4881 = (SP + 11.U(16.W))
        val __tmp_4882 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4881 + 0.U) := __tmp_4882(7, 0)

        val __tmp_4883 = (SP + 12.U(16.W))
        val __tmp_4884 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4883 + 0.U) := __tmp_4884(7, 0)

        val __tmp_4885 = (SP + 13.U(16.W))
        val __tmp_4886 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4885 + 0.U) := __tmp_4886(7, 0)

        val __tmp_4887 = (SP + 14.U(16.W))
        val __tmp_4888 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4887 + 0.U) := __tmp_4888(7, 0)

        val __tmp_4889 = (SP + 15.U(16.W))
        val __tmp_4890 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4889 + 0.U) := __tmp_4890(7, 0)

        val __tmp_4891 = (SP + 16.U(16.W))
        val __tmp_4892 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4891 + 0.U) := __tmp_4892(7, 0)

        val __tmp_4893 = (SP + 17.U(16.W))
        val __tmp_4894 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4893 + 0.U) := __tmp_4894(7, 0)

        val __tmp_4895 = (SP + 18.U(16.W))
        val __tmp_4896 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4895 + 0.U) := __tmp_4896(7, 0)

        val __tmp_4897 = (SP + 19.U(16.W))
        val __tmp_4898 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4897 + 0.U) := __tmp_4898(7, 0)

        val __tmp_4899 = (SP + 20.U(16.W))
        val __tmp_4900 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4899 + 0.U) := __tmp_4900(7, 0)

        val __tmp_4901 = (SP + 21.U(16.W))
        val __tmp_4902 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4901 + 0.U) := __tmp_4902(7, 0)

        val __tmp_4903 = (SP + 22.U(16.W))
        val __tmp_4904 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4903 + 0.U) := __tmp_4904(7, 0)

        val __tmp_4905 = (SP + 23.U(16.W))
        val __tmp_4906 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4905 + 0.U) := __tmp_4906(7, 0)

        val __tmp_4907 = (SP + 24.U(16.W))
        val __tmp_4908 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4907 + 0.U) := __tmp_4908(7, 0)

        val __tmp_4909 = (SP + 25.U(16.W))
        val __tmp_4910 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4909 + 0.U) := __tmp_4910(7, 0)

        val __tmp_4911 = (SP + 26.U(16.W))
        val __tmp_4912 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4911 + 0.U) := __tmp_4912(7, 0)

        val __tmp_4913 = (SP + 27.U(16.W))
        val __tmp_4914 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4913 + 0.U) := __tmp_4914(7, 0)

        val __tmp_4915 = (SP + 28.U(16.W))
        val __tmp_4916 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4915 + 0.U) := __tmp_4916(7, 0)

        val __tmp_4917 = (SP + 29.U(16.W))
        val __tmp_4918 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4917 + 0.U) := __tmp_4918(7, 0)

        val __tmp_4919 = (SP + 30.U(16.W))
        val __tmp_4920 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4919 + 0.U) := __tmp_4920(7, 0)

        val __tmp_4921 = (SP + 31.U(16.W))
        val __tmp_4922 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4921 + 0.U) := __tmp_4922(7, 0)

        val __tmp_4923 = (SP + 32.U(16.W))
        val __tmp_4924 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4923 + 0.U) := __tmp_4924(7, 0)

        val __tmp_4925 = (SP + 33.U(16.W))
        val __tmp_4926 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4925 + 0.U) := __tmp_4926(7, 0)

        val __tmp_4927 = (SP + 34.U(16.W))
        val __tmp_4928 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4927 + 0.U) := __tmp_4928(7, 0)

        val __tmp_4929 = (SP + 35.U(16.W))
        val __tmp_4930 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4929 + 0.U) := __tmp_4930(7, 0)

        val __tmp_4931 = (SP + 36.U(16.W))
        val __tmp_4932 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4931 + 0.U) := __tmp_4932(7, 0)

        val __tmp_4933 = (SP + 37.U(16.W))
        val __tmp_4934 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4933 + 0.U) := __tmp_4934(7, 0)

        CP := 55.U
      }

      is(55.U) {
        /*
        alloc printS64$res@[17,11].365E3C94: U64 [@2, 8]
        goto .56
        */


        CP := 56.U
      }

      is(56.U) {
        /*
        SP = SP + 46
        goto .57
        */


        SP := SP + 46.U

        CP := 57.U
      }

      is(57.U) {
        /*
        *SP = (59: CP) [unsigned, CP, 2]  // $ret@0 = 1375
        *(SP + (2: SP)) = (SP - (44: SP)) [unsigned, SP, 2]  // $res@2 = -44
        $91 = (8: SP)
        $92 = DP
        $93 = (7: anvil.PrinterIndex.U)
        $94 = (($3: Z) as S64)
        goto .58
        */


        val __tmp_4935 = SP
        val __tmp_4936 = (59.U(16.W)).asUInt
        arrayRegFiles(__tmp_4935 + 0.U) := __tmp_4936(7, 0)
        arrayRegFiles(__tmp_4935 + 1.U) := __tmp_4936(15, 8)

        val __tmp_4937 = (SP + 2.U(16.W))
        val __tmp_4938 = ((SP - 44.U(16.W))).asUInt
        arrayRegFiles(__tmp_4937 + 0.U) := __tmp_4938(7, 0)
        arrayRegFiles(__tmp_4937 + 1.U) := __tmp_4938(15, 8)


        generalRegFiles(91.U) := 8.U(16.W)


        generalRegFiles(92.U) := DP


        generalRegFiles(93.U) := 7.U(64.W)


        generalRegFiles(94.U) := (generalRegFiles(3.U).asSInt.asSInt).asUInt

        CP := 58.U
      }

      is(58.U) {
        /*
        decl $ret: CP [@0, 2], $res: SP [@2, 2], buffer: MS[anvil.PrinterIndex.U, U8] @$0, index: anvil.PrinterIndex.U @$1, mask: anvil.PrinterIndex.U @$2, n: S64 @$3
        $0 = ($91: MS[anvil.PrinterIndex.U, U8])
        $1 = ($92: anvil.PrinterIndex.U)
        $2 = ($93: anvil.PrinterIndex.U)
        $3 = ($94: S64)
        goto .101
        */



        generalRegFiles(0.U) := generalRegFiles(91.U)


        generalRegFiles(1.U) := generalRegFiles(92.U)


        generalRegFiles(2.U) := generalRegFiles(93.U)


        generalRegFiles(3.U) := (generalRegFiles(94.U).asSInt).asUInt

        CP := 101.U
      }

      is(59.U) {
        /*
        $4 = **(SP + (2: SP)) [unsigned, U64, 8]  // $4 = $res
        goto .60
        */


        val __tmp_4939 = (Cat(
          arrayRegFiles((SP + 2.U(16.W)) + 1.U),
          arrayRegFiles((SP + 2.U(16.W)) + 0.U)
        )).asUInt
        generalRegFiles(4.U) := Cat(
          arrayRegFiles(__tmp_4939 + 7.U),
          arrayRegFiles(__tmp_4939 + 6.U),
          arrayRegFiles(__tmp_4939 + 5.U),
          arrayRegFiles(__tmp_4939 + 4.U),
          arrayRegFiles(__tmp_4939 + 3.U),
          arrayRegFiles(__tmp_4939 + 2.U),
          arrayRegFiles(__tmp_4939 + 1.U),
          arrayRegFiles(__tmp_4939 + 0.U)
        ).asUInt

        CP := 60.U
      }

      is(60.U) {
        /*
        *(SP + (2: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $res byte 0
        *(SP + (3: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $res byte 1
        *(SP + (0: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $ret byte 0
        *(SP + (1: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $ret byte 1
        undecl n: S64 @$3, mask: anvil.PrinterIndex.U @$2, index: anvil.PrinterIndex.U @$1, buffer: MS[anvil.PrinterIndex.U, U8] @$0, $res: SP [@2, 2], $ret: CP [@0, 2]
        goto .61
        */


        val __tmp_4940 = (SP + 2.U(16.W))
        val __tmp_4941 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4940 + 0.U) := __tmp_4941(7, 0)

        val __tmp_4942 = (SP + 3.U(16.W))
        val __tmp_4943 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4942 + 0.U) := __tmp_4943(7, 0)

        val __tmp_4944 = (SP + 0.U(16.W))
        val __tmp_4945 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4944 + 0.U) := __tmp_4945(7, 0)

        val __tmp_4946 = (SP + 1.U(16.W))
        val __tmp_4947 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4946 + 0.U) := __tmp_4947(7, 0)

        CP := 61.U
      }

      is(61.U) {
        /*
        SP = SP - 46
        goto .62
        */


        SP := SP - 46.U

        CP := 62.U
      }

      is(62.U) {
        /*
        DP = DP + (($4: U64) as DP)
        goto .63
        */


        DP := DP + generalRegFiles(4.U).asUInt

        CP := 63.U
      }

      is(63.U) {
        /*
        *(((8: SP) + (12: SP)) + ((DP & (7: DP)) as SP)) = (10: U8) [unsigned, U8, 1]  // $display((DP & (7: DP))) = (10: U8)
        goto .64
        */


        val __tmp_4948 = ((8.U(16.W) + 12.U(16.W)) + (DP & 7.U(64.W)).asUInt)
        val __tmp_4949 = (10.U(8.W)).asUInt
        arrayRegFiles(__tmp_4948 + 0.U) := __tmp_4949(7, 0)

        CP := 64.U
      }

      is(64.U) {
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

      is(65.U) {
        /*
        alloc $new@[21,15].B87C7CA2: IS[Z, Z] [@2, 36]
        $2 = (SP + (2: SP))
        *(SP + (2: SP)) = (2192300037: U32) [unsigned, U32, 4]  // sha3 type signature of IS[Z, Z]: 0x82ABD805
        *(SP + (6: SP)) = (3: Z) [signed, Z, 8]  // size of IS[Z, Z]((1: Z), (2: Z), (3: Z))
        goto .66
        */



        generalRegFiles(2.U) := (SP + 2.U(16.W))

        val __tmp_4950 = (SP + 2.U(16.W))
        val __tmp_4951 = (BigInt("2192300037").U(32.W)).asUInt
        arrayRegFiles(__tmp_4950 + 0.U) := __tmp_4951(7, 0)
        arrayRegFiles(__tmp_4950 + 1.U) := __tmp_4951(15, 8)
        arrayRegFiles(__tmp_4950 + 2.U) := __tmp_4951(23, 16)
        arrayRegFiles(__tmp_4950 + 3.U) := __tmp_4951(31, 24)

        val __tmp_4952 = (SP + 6.U(16.W))
        val __tmp_4953 = (3.S(64.W)).asUInt
        arrayRegFiles(__tmp_4952 + 0.U) := __tmp_4953(7, 0)
        arrayRegFiles(__tmp_4952 + 1.U) := __tmp_4953(15, 8)
        arrayRegFiles(__tmp_4952 + 2.U) := __tmp_4953(23, 16)
        arrayRegFiles(__tmp_4952 + 3.U) := __tmp_4953(31, 24)
        arrayRegFiles(__tmp_4952 + 4.U) := __tmp_4953(39, 32)
        arrayRegFiles(__tmp_4952 + 5.U) := __tmp_4953(47, 40)
        arrayRegFiles(__tmp_4952 + 6.U) := __tmp_4953(55, 48)
        arrayRegFiles(__tmp_4952 + 7.U) := __tmp_4953(63, 56)

        CP := 66.U
      }

      is(66.U) {
        /*
        *((($2: IS[Z, Z]) + (12: SP)) + (((0: Z) as SP) * (8: SP))) = (1: Z) [signed, Z, 8]  // ($2: IS[Z, Z])((0: Z)) = (1: Z)
        *((($2: IS[Z, Z]) + (12: SP)) + (((1: Z) as SP) * (8: SP))) = (2: Z) [signed, Z, 8]  // ($2: IS[Z, Z])((1: Z)) = (2: Z)
        *((($2: IS[Z, Z]) + (12: SP)) + (((2: Z) as SP) * (8: SP))) = (3: Z) [signed, Z, 8]  // ($2: IS[Z, Z])((2: Z)) = (3: Z)
        alloc sum$res@[21,11].FA3D09EB: Z [@38, 8]
        goto .67
        */


        val __tmp_4954 = ((generalRegFiles(2.U) + 12.U(16.W)) + (0.S(64.W).asUInt * 8.U(16.W)))
        val __tmp_4955 = (1.S(64.W)).asUInt
        arrayRegFiles(__tmp_4954 + 0.U) := __tmp_4955(7, 0)
        arrayRegFiles(__tmp_4954 + 1.U) := __tmp_4955(15, 8)
        arrayRegFiles(__tmp_4954 + 2.U) := __tmp_4955(23, 16)
        arrayRegFiles(__tmp_4954 + 3.U) := __tmp_4955(31, 24)
        arrayRegFiles(__tmp_4954 + 4.U) := __tmp_4955(39, 32)
        arrayRegFiles(__tmp_4954 + 5.U) := __tmp_4955(47, 40)
        arrayRegFiles(__tmp_4954 + 6.U) := __tmp_4955(55, 48)
        arrayRegFiles(__tmp_4954 + 7.U) := __tmp_4955(63, 56)

        val __tmp_4956 = ((generalRegFiles(2.U) + 12.U(16.W)) + (1.S(64.W).asUInt * 8.U(16.W)))
        val __tmp_4957 = (2.S(64.W)).asUInt
        arrayRegFiles(__tmp_4956 + 0.U) := __tmp_4957(7, 0)
        arrayRegFiles(__tmp_4956 + 1.U) := __tmp_4957(15, 8)
        arrayRegFiles(__tmp_4956 + 2.U) := __tmp_4957(23, 16)
        arrayRegFiles(__tmp_4956 + 3.U) := __tmp_4957(31, 24)
        arrayRegFiles(__tmp_4956 + 4.U) := __tmp_4957(39, 32)
        arrayRegFiles(__tmp_4956 + 5.U) := __tmp_4957(47, 40)
        arrayRegFiles(__tmp_4956 + 6.U) := __tmp_4957(55, 48)
        arrayRegFiles(__tmp_4956 + 7.U) := __tmp_4957(63, 56)

        val __tmp_4958 = ((generalRegFiles(2.U) + 12.U(16.W)) + (2.S(64.W).asUInt * 8.U(16.W)))
        val __tmp_4959 = (3.S(64.W)).asUInt
        arrayRegFiles(__tmp_4958 + 0.U) := __tmp_4959(7, 0)
        arrayRegFiles(__tmp_4958 + 1.U) := __tmp_4959(15, 8)
        arrayRegFiles(__tmp_4958 + 2.U) := __tmp_4959(23, 16)
        arrayRegFiles(__tmp_4958 + 3.U) := __tmp_4959(31, 24)
        arrayRegFiles(__tmp_4958 + 4.U) := __tmp_4959(39, 32)
        arrayRegFiles(__tmp_4958 + 5.U) := __tmp_4959(47, 40)
        arrayRegFiles(__tmp_4958 + 6.U) := __tmp_4959(55, 48)
        arrayRegFiles(__tmp_4958 + 7.U) := __tmp_4959(63, 56)

        CP := 67.U
      }

      is(67.U) {
        /*
        SP = SP + 46
        goto .68
        */


        SP := SP + 46.U

        CP := 68.U
      }

      is(68.U) {
        /*
        *SP = (70: CP) [unsigned, CP, 2]  // $ret@0 = 1377
        *(SP + (2: SP)) = (SP - (8: SP)) [unsigned, SP, 2]  // $res@2 = -8
        $29 = ($2: IS[Z, Z])
        $30 = (1: Z)
        $31 = (0: Z)
        goto .69
        */


        val __tmp_4960 = SP
        val __tmp_4961 = (70.U(16.W)).asUInt
        arrayRegFiles(__tmp_4960 + 0.U) := __tmp_4961(7, 0)
        arrayRegFiles(__tmp_4960 + 1.U) := __tmp_4961(15, 8)

        val __tmp_4962 = (SP + 2.U(16.W))
        val __tmp_4963 = ((SP - 8.U(16.W))).asUInt
        arrayRegFiles(__tmp_4962 + 0.U) := __tmp_4963(7, 0)
        arrayRegFiles(__tmp_4962 + 1.U) := __tmp_4963(15, 8)


        generalRegFiles(29.U) := generalRegFiles(2.U)


        generalRegFiles(30.U) := (1.S(64.W)).asUInt


        generalRegFiles(31.U) := (0.S(64.W)).asUInt

        CP := 69.U
      }

      is(69.U) {
        /*
        decl $ret: CP [@0, 2], $res: SP [@2, 10], a: IS[Z, Z] @$0, a: IS[Z, Z] [@12, 36], i: Z @$1, acc: Z @$2
        $0 = ($29: IS[Z, Z])
        $1 = ($30: Z)
        $2 = ($31: Z)
        goto .84
        */



        generalRegFiles(0.U) := generalRegFiles(29.U)


        generalRegFiles(1.U) := (generalRegFiles(30.U).asSInt).asUInt


        generalRegFiles(2.U) := (generalRegFiles(31.U).asSInt).asUInt

        CP := 84.U
      }

      is(70.U) {
        /*
        $3 = **(SP + (2: SP)) [signed, Z, 8]  // $3 = $res
        goto .71
        */


        val __tmp_4964 = (Cat(
          arrayRegFiles((SP + 2.U(16.W)) + 1.U),
          arrayRegFiles((SP + 2.U(16.W)) + 0.U)
        )).asUInt
        generalRegFiles(3.U) := Cat(
          arrayRegFiles(__tmp_4964 + 7.U),
          arrayRegFiles(__tmp_4964 + 6.U),
          arrayRegFiles(__tmp_4964 + 5.U),
          arrayRegFiles(__tmp_4964 + 4.U),
          arrayRegFiles(__tmp_4964 + 3.U),
          arrayRegFiles(__tmp_4964 + 2.U),
          arrayRegFiles(__tmp_4964 + 1.U),
          arrayRegFiles(__tmp_4964 + 0.U)
        ).asSInt.pad(GENERAL_REG_WIDTH).asUInt

        CP := 71.U
      }

      is(71.U) {
        /*
        *(SP + (12: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 0
        *(SP + (13: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 1
        *(SP + (14: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 2
        *(SP + (15: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 3
        *(SP + (16: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 4
        *(SP + (17: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 5
        *(SP + (18: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 6
        *(SP + (19: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 7
        *(SP + (20: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 8
        *(SP + (21: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 9
        *(SP + (22: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 10
        *(SP + (23: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 11
        *(SP + (24: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 12
        *(SP + (25: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 13
        *(SP + (26: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 14
        *(SP + (27: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 15
        *(SP + (28: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 16
        *(SP + (29: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 17
        *(SP + (30: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 18
        *(SP + (31: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 19
        *(SP + (32: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 20
        *(SP + (33: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 21
        *(SP + (34: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 22
        *(SP + (35: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 23
        *(SP + (36: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 24
        *(SP + (37: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 25
        *(SP + (38: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 26
        *(SP + (39: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 27
        *(SP + (40: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 28
        *(SP + (41: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 29
        *(SP + (42: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 30
        *(SP + (43: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 31
        *(SP + (44: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 32
        *(SP + (45: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 33
        *(SP + (46: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 34
        *(SP + (47: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 35
        *(SP + (2: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $res byte 0
        *(SP + (3: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $res byte 1
        *(SP + (4: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $res byte 2
        *(SP + (5: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $res byte 3
        *(SP + (6: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $res byte 4
        *(SP + (7: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $res byte 5
        *(SP + (8: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $res byte 6
        *(SP + (9: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $res byte 7
        *(SP + (10: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $res byte 8
        *(SP + (11: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $res byte 9
        *(SP + (0: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $ret byte 0
        *(SP + (1: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $ret byte 1
        undecl acc: Z @$2, i: Z @$1, a: IS[Z, Z] [@12, 36], a: IS[Z, Z] @$0, $res: SP [@2, 10], $ret: CP [@0, 2]
        goto .72
        */


        val __tmp_4965 = (SP + 12.U(16.W))
        val __tmp_4966 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4965 + 0.U) := __tmp_4966(7, 0)

        val __tmp_4967 = (SP + 13.U(16.W))
        val __tmp_4968 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4967 + 0.U) := __tmp_4968(7, 0)

        val __tmp_4969 = (SP + 14.U(16.W))
        val __tmp_4970 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4969 + 0.U) := __tmp_4970(7, 0)

        val __tmp_4971 = (SP + 15.U(16.W))
        val __tmp_4972 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4971 + 0.U) := __tmp_4972(7, 0)

        val __tmp_4973 = (SP + 16.U(16.W))
        val __tmp_4974 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4973 + 0.U) := __tmp_4974(7, 0)

        val __tmp_4975 = (SP + 17.U(16.W))
        val __tmp_4976 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4975 + 0.U) := __tmp_4976(7, 0)

        val __tmp_4977 = (SP + 18.U(16.W))
        val __tmp_4978 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4977 + 0.U) := __tmp_4978(7, 0)

        val __tmp_4979 = (SP + 19.U(16.W))
        val __tmp_4980 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4979 + 0.U) := __tmp_4980(7, 0)

        val __tmp_4981 = (SP + 20.U(16.W))
        val __tmp_4982 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4981 + 0.U) := __tmp_4982(7, 0)

        val __tmp_4983 = (SP + 21.U(16.W))
        val __tmp_4984 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4983 + 0.U) := __tmp_4984(7, 0)

        val __tmp_4985 = (SP + 22.U(16.W))
        val __tmp_4986 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4985 + 0.U) := __tmp_4986(7, 0)

        val __tmp_4987 = (SP + 23.U(16.W))
        val __tmp_4988 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4987 + 0.U) := __tmp_4988(7, 0)

        val __tmp_4989 = (SP + 24.U(16.W))
        val __tmp_4990 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4989 + 0.U) := __tmp_4990(7, 0)

        val __tmp_4991 = (SP + 25.U(16.W))
        val __tmp_4992 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4991 + 0.U) := __tmp_4992(7, 0)

        val __tmp_4993 = (SP + 26.U(16.W))
        val __tmp_4994 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4993 + 0.U) := __tmp_4994(7, 0)

        val __tmp_4995 = (SP + 27.U(16.W))
        val __tmp_4996 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4995 + 0.U) := __tmp_4996(7, 0)

        val __tmp_4997 = (SP + 28.U(16.W))
        val __tmp_4998 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4997 + 0.U) := __tmp_4998(7, 0)

        val __tmp_4999 = (SP + 29.U(16.W))
        val __tmp_5000 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4999 + 0.U) := __tmp_5000(7, 0)

        val __tmp_5001 = (SP + 30.U(16.W))
        val __tmp_5002 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5001 + 0.U) := __tmp_5002(7, 0)

        val __tmp_5003 = (SP + 31.U(16.W))
        val __tmp_5004 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5003 + 0.U) := __tmp_5004(7, 0)

        val __tmp_5005 = (SP + 32.U(16.W))
        val __tmp_5006 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5005 + 0.U) := __tmp_5006(7, 0)

        val __tmp_5007 = (SP + 33.U(16.W))
        val __tmp_5008 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5007 + 0.U) := __tmp_5008(7, 0)

        val __tmp_5009 = (SP + 34.U(16.W))
        val __tmp_5010 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5009 + 0.U) := __tmp_5010(7, 0)

        val __tmp_5011 = (SP + 35.U(16.W))
        val __tmp_5012 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5011 + 0.U) := __tmp_5012(7, 0)

        val __tmp_5013 = (SP + 36.U(16.W))
        val __tmp_5014 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5013 + 0.U) := __tmp_5014(7, 0)

        val __tmp_5015 = (SP + 37.U(16.W))
        val __tmp_5016 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5015 + 0.U) := __tmp_5016(7, 0)

        val __tmp_5017 = (SP + 38.U(16.W))
        val __tmp_5018 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5017 + 0.U) := __tmp_5018(7, 0)

        val __tmp_5019 = (SP + 39.U(16.W))
        val __tmp_5020 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5019 + 0.U) := __tmp_5020(7, 0)

        val __tmp_5021 = (SP + 40.U(16.W))
        val __tmp_5022 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5021 + 0.U) := __tmp_5022(7, 0)

        val __tmp_5023 = (SP + 41.U(16.W))
        val __tmp_5024 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5023 + 0.U) := __tmp_5024(7, 0)

        val __tmp_5025 = (SP + 42.U(16.W))
        val __tmp_5026 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5025 + 0.U) := __tmp_5026(7, 0)

        val __tmp_5027 = (SP + 43.U(16.W))
        val __tmp_5028 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5027 + 0.U) := __tmp_5028(7, 0)

        val __tmp_5029 = (SP + 44.U(16.W))
        val __tmp_5030 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5029 + 0.U) := __tmp_5030(7, 0)

        val __tmp_5031 = (SP + 45.U(16.W))
        val __tmp_5032 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5031 + 0.U) := __tmp_5032(7, 0)

        val __tmp_5033 = (SP + 46.U(16.W))
        val __tmp_5034 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5033 + 0.U) := __tmp_5034(7, 0)

        val __tmp_5035 = (SP + 47.U(16.W))
        val __tmp_5036 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5035 + 0.U) := __tmp_5036(7, 0)

        val __tmp_5037 = (SP + 2.U(16.W))
        val __tmp_5038 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5037 + 0.U) := __tmp_5038(7, 0)

        val __tmp_5039 = (SP + 3.U(16.W))
        val __tmp_5040 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5039 + 0.U) := __tmp_5040(7, 0)

        val __tmp_5041 = (SP + 4.U(16.W))
        val __tmp_5042 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5041 + 0.U) := __tmp_5042(7, 0)

        val __tmp_5043 = (SP + 5.U(16.W))
        val __tmp_5044 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5043 + 0.U) := __tmp_5044(7, 0)

        val __tmp_5045 = (SP + 6.U(16.W))
        val __tmp_5046 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5045 + 0.U) := __tmp_5046(7, 0)

        val __tmp_5047 = (SP + 7.U(16.W))
        val __tmp_5048 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5047 + 0.U) := __tmp_5048(7, 0)

        val __tmp_5049 = (SP + 8.U(16.W))
        val __tmp_5050 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5049 + 0.U) := __tmp_5050(7, 0)

        val __tmp_5051 = (SP + 9.U(16.W))
        val __tmp_5052 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5051 + 0.U) := __tmp_5052(7, 0)

        val __tmp_5053 = (SP + 10.U(16.W))
        val __tmp_5054 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5053 + 0.U) := __tmp_5054(7, 0)

        val __tmp_5055 = (SP + 11.U(16.W))
        val __tmp_5056 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5055 + 0.U) := __tmp_5056(7, 0)

        val __tmp_5057 = (SP + 0.U(16.W))
        val __tmp_5058 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5057 + 0.U) := __tmp_5058(7, 0)

        val __tmp_5059 = (SP + 1.U(16.W))
        val __tmp_5060 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5059 + 0.U) := __tmp_5060(7, 0)

        CP := 72.U
      }

      is(72.U) {
        /*
        SP = SP - 46
        goto .73
        */


        SP := SP - 46.U

        CP := 73.U
      }

      is(73.U) {
        /*
        *(SP + (2: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[21,15].B87C7CA2 byte 0
        *(SP + (3: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[21,15].B87C7CA2 byte 1
        *(SP + (4: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[21,15].B87C7CA2 byte 2
        *(SP + (5: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[21,15].B87C7CA2 byte 3
        *(SP + (6: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[21,15].B87C7CA2 byte 4
        *(SP + (7: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[21,15].B87C7CA2 byte 5
        *(SP + (8: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[21,15].B87C7CA2 byte 6
        *(SP + (9: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[21,15].B87C7CA2 byte 7
        *(SP + (10: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[21,15].B87C7CA2 byte 8
        *(SP + (11: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[21,15].B87C7CA2 byte 9
        *(SP + (12: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[21,15].B87C7CA2 byte 10
        *(SP + (13: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[21,15].B87C7CA2 byte 11
        *(SP + (14: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[21,15].B87C7CA2 byte 12
        *(SP + (15: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[21,15].B87C7CA2 byte 13
        *(SP + (16: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[21,15].B87C7CA2 byte 14
        *(SP + (17: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[21,15].B87C7CA2 byte 15
        *(SP + (18: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[21,15].B87C7CA2 byte 16
        *(SP + (19: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[21,15].B87C7CA2 byte 17
        *(SP + (20: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[21,15].B87C7CA2 byte 18
        *(SP + (21: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[21,15].B87C7CA2 byte 19
        *(SP + (22: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[21,15].B87C7CA2 byte 20
        *(SP + (23: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[21,15].B87C7CA2 byte 21
        *(SP + (24: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[21,15].B87C7CA2 byte 22
        *(SP + (25: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[21,15].B87C7CA2 byte 23
        *(SP + (26: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[21,15].B87C7CA2 byte 24
        *(SP + (27: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[21,15].B87C7CA2 byte 25
        *(SP + (28: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[21,15].B87C7CA2 byte 26
        *(SP + (29: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[21,15].B87C7CA2 byte 27
        *(SP + (30: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[21,15].B87C7CA2 byte 28
        *(SP + (31: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[21,15].B87C7CA2 byte 29
        *(SP + (32: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[21,15].B87C7CA2 byte 30
        *(SP + (33: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[21,15].B87C7CA2 byte 31
        *(SP + (34: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[21,15].B87C7CA2 byte 32
        *(SP + (35: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[21,15].B87C7CA2 byte 33
        *(SP + (36: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[21,15].B87C7CA2 byte 34
        *(SP + (37: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[21,15].B87C7CA2 byte 35
        unalloc $new@[21,15].B87C7CA2: IS[Z, Z] [@2, 36]
        goto .74
        */


        val __tmp_5061 = (SP + 2.U(16.W))
        val __tmp_5062 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5061 + 0.U) := __tmp_5062(7, 0)

        val __tmp_5063 = (SP + 3.U(16.W))
        val __tmp_5064 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5063 + 0.U) := __tmp_5064(7, 0)

        val __tmp_5065 = (SP + 4.U(16.W))
        val __tmp_5066 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5065 + 0.U) := __tmp_5066(7, 0)

        val __tmp_5067 = (SP + 5.U(16.W))
        val __tmp_5068 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5067 + 0.U) := __tmp_5068(7, 0)

        val __tmp_5069 = (SP + 6.U(16.W))
        val __tmp_5070 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5069 + 0.U) := __tmp_5070(7, 0)

        val __tmp_5071 = (SP + 7.U(16.W))
        val __tmp_5072 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5071 + 0.U) := __tmp_5072(7, 0)

        val __tmp_5073 = (SP + 8.U(16.W))
        val __tmp_5074 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5073 + 0.U) := __tmp_5074(7, 0)

        val __tmp_5075 = (SP + 9.U(16.W))
        val __tmp_5076 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5075 + 0.U) := __tmp_5076(7, 0)

        val __tmp_5077 = (SP + 10.U(16.W))
        val __tmp_5078 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5077 + 0.U) := __tmp_5078(7, 0)

        val __tmp_5079 = (SP + 11.U(16.W))
        val __tmp_5080 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5079 + 0.U) := __tmp_5080(7, 0)

        val __tmp_5081 = (SP + 12.U(16.W))
        val __tmp_5082 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5081 + 0.U) := __tmp_5082(7, 0)

        val __tmp_5083 = (SP + 13.U(16.W))
        val __tmp_5084 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5083 + 0.U) := __tmp_5084(7, 0)

        val __tmp_5085 = (SP + 14.U(16.W))
        val __tmp_5086 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5085 + 0.U) := __tmp_5086(7, 0)

        val __tmp_5087 = (SP + 15.U(16.W))
        val __tmp_5088 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5087 + 0.U) := __tmp_5088(7, 0)

        val __tmp_5089 = (SP + 16.U(16.W))
        val __tmp_5090 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5089 + 0.U) := __tmp_5090(7, 0)

        val __tmp_5091 = (SP + 17.U(16.W))
        val __tmp_5092 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5091 + 0.U) := __tmp_5092(7, 0)

        val __tmp_5093 = (SP + 18.U(16.W))
        val __tmp_5094 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5093 + 0.U) := __tmp_5094(7, 0)

        val __tmp_5095 = (SP + 19.U(16.W))
        val __tmp_5096 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5095 + 0.U) := __tmp_5096(7, 0)

        val __tmp_5097 = (SP + 20.U(16.W))
        val __tmp_5098 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5097 + 0.U) := __tmp_5098(7, 0)

        val __tmp_5099 = (SP + 21.U(16.W))
        val __tmp_5100 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5099 + 0.U) := __tmp_5100(7, 0)

        val __tmp_5101 = (SP + 22.U(16.W))
        val __tmp_5102 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5101 + 0.U) := __tmp_5102(7, 0)

        val __tmp_5103 = (SP + 23.U(16.W))
        val __tmp_5104 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5103 + 0.U) := __tmp_5104(7, 0)

        val __tmp_5105 = (SP + 24.U(16.W))
        val __tmp_5106 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5105 + 0.U) := __tmp_5106(7, 0)

        val __tmp_5107 = (SP + 25.U(16.W))
        val __tmp_5108 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5107 + 0.U) := __tmp_5108(7, 0)

        val __tmp_5109 = (SP + 26.U(16.W))
        val __tmp_5110 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5109 + 0.U) := __tmp_5110(7, 0)

        val __tmp_5111 = (SP + 27.U(16.W))
        val __tmp_5112 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5111 + 0.U) := __tmp_5112(7, 0)

        val __tmp_5113 = (SP + 28.U(16.W))
        val __tmp_5114 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5113 + 0.U) := __tmp_5114(7, 0)

        val __tmp_5115 = (SP + 29.U(16.W))
        val __tmp_5116 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5115 + 0.U) := __tmp_5116(7, 0)

        val __tmp_5117 = (SP + 30.U(16.W))
        val __tmp_5118 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5117 + 0.U) := __tmp_5118(7, 0)

        val __tmp_5119 = (SP + 31.U(16.W))
        val __tmp_5120 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5119 + 0.U) := __tmp_5120(7, 0)

        val __tmp_5121 = (SP + 32.U(16.W))
        val __tmp_5122 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5121 + 0.U) := __tmp_5122(7, 0)

        val __tmp_5123 = (SP + 33.U(16.W))
        val __tmp_5124 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5123 + 0.U) := __tmp_5124(7, 0)

        val __tmp_5125 = (SP + 34.U(16.W))
        val __tmp_5126 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5125 + 0.U) := __tmp_5126(7, 0)

        val __tmp_5127 = (SP + 35.U(16.W))
        val __tmp_5128 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5127 + 0.U) := __tmp_5128(7, 0)

        val __tmp_5129 = (SP + 36.U(16.W))
        val __tmp_5130 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5129 + 0.U) := __tmp_5130(7, 0)

        val __tmp_5131 = (SP + 37.U(16.W))
        val __tmp_5132 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5131 + 0.U) := __tmp_5132(7, 0)

        CP := 74.U
      }

      is(74.U) {
        /*
        alloc printS64$res@[21,11].FA3D09EB: U64 [@2, 8]
        goto .75
        */


        CP := 75.U
      }

      is(75.U) {
        /*
        SP = SP + 46
        goto .76
        */


        SP := SP + 46.U

        CP := 76.U
      }

      is(76.U) {
        /*
        *SP = (78: CP) [unsigned, CP, 2]  // $ret@0 = 1379
        *(SP + (2: SP)) = (SP - (44: SP)) [unsigned, SP, 2]  // $res@2 = -44
        $91 = (8: SP)
        $92 = DP
        $93 = (7: anvil.PrinterIndex.U)
        $94 = (($3: Z) as S64)
        goto .77
        */


        val __tmp_5133 = SP
        val __tmp_5134 = (78.U(16.W)).asUInt
        arrayRegFiles(__tmp_5133 + 0.U) := __tmp_5134(7, 0)
        arrayRegFiles(__tmp_5133 + 1.U) := __tmp_5134(15, 8)

        val __tmp_5135 = (SP + 2.U(16.W))
        val __tmp_5136 = ((SP - 44.U(16.W))).asUInt
        arrayRegFiles(__tmp_5135 + 0.U) := __tmp_5136(7, 0)
        arrayRegFiles(__tmp_5135 + 1.U) := __tmp_5136(15, 8)


        generalRegFiles(91.U) := 8.U(16.W)


        generalRegFiles(92.U) := DP


        generalRegFiles(93.U) := 7.U(64.W)


        generalRegFiles(94.U) := (generalRegFiles(3.U).asSInt.asSInt).asUInt

        CP := 77.U
      }

      is(77.U) {
        /*
        decl $ret: CP [@0, 2], $res: SP [@2, 2], buffer: MS[anvil.PrinterIndex.U, U8] @$0, index: anvil.PrinterIndex.U @$1, mask: anvil.PrinterIndex.U @$2, n: S64 @$3
        $0 = ($91: MS[anvil.PrinterIndex.U, U8])
        $1 = ($92: anvil.PrinterIndex.U)
        $2 = ($93: anvil.PrinterIndex.U)
        $3 = ($94: S64)
        goto .101
        */



        generalRegFiles(0.U) := generalRegFiles(91.U)


        generalRegFiles(1.U) := generalRegFiles(92.U)


        generalRegFiles(2.U) := generalRegFiles(93.U)


        generalRegFiles(3.U) := (generalRegFiles(94.U).asSInt).asUInt

        CP := 101.U
      }

      is(78.U) {
        /*
        $4 = **(SP + (2: SP)) [unsigned, U64, 8]  // $4 = $res
        goto .79
        */


        val __tmp_5137 = (Cat(
          arrayRegFiles((SP + 2.U(16.W)) + 1.U),
          arrayRegFiles((SP + 2.U(16.W)) + 0.U)
        )).asUInt
        generalRegFiles(4.U) := Cat(
          arrayRegFiles(__tmp_5137 + 7.U),
          arrayRegFiles(__tmp_5137 + 6.U),
          arrayRegFiles(__tmp_5137 + 5.U),
          arrayRegFiles(__tmp_5137 + 4.U),
          arrayRegFiles(__tmp_5137 + 3.U),
          arrayRegFiles(__tmp_5137 + 2.U),
          arrayRegFiles(__tmp_5137 + 1.U),
          arrayRegFiles(__tmp_5137 + 0.U)
        ).asUInt

        CP := 79.U
      }

      is(79.U) {
        /*
        *(SP + (2: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $res byte 0
        *(SP + (3: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $res byte 1
        *(SP + (0: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $ret byte 0
        *(SP + (1: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $ret byte 1
        undecl n: S64 @$3, mask: anvil.PrinterIndex.U @$2, index: anvil.PrinterIndex.U @$1, buffer: MS[anvil.PrinterIndex.U, U8] @$0, $res: SP [@2, 2], $ret: CP [@0, 2]
        goto .80
        */


        val __tmp_5138 = (SP + 2.U(16.W))
        val __tmp_5139 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5138 + 0.U) := __tmp_5139(7, 0)

        val __tmp_5140 = (SP + 3.U(16.W))
        val __tmp_5141 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5140 + 0.U) := __tmp_5141(7, 0)

        val __tmp_5142 = (SP + 0.U(16.W))
        val __tmp_5143 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5142 + 0.U) := __tmp_5143(7, 0)

        val __tmp_5144 = (SP + 1.U(16.W))
        val __tmp_5145 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5144 + 0.U) := __tmp_5145(7, 0)

        CP := 80.U
      }

      is(80.U) {
        /*
        SP = SP - 46
        goto .81
        */


        SP := SP - 46.U

        CP := 81.U
      }

      is(81.U) {
        /*
        DP = DP + (($4: U64) as DP)
        goto .82
        */


        DP := DP + generalRegFiles(4.U).asUInt

        CP := 82.U
      }

      is(82.U) {
        /*
        *(((8: SP) + (12: SP)) + ((DP & (7: DP)) as SP)) = (10: U8) [unsigned, U8, 1]  // $display((DP & (7: DP))) = (10: U8)
        goto .83
        */


        val __tmp_5146 = ((8.U(16.W) + 12.U(16.W)) + (DP & 7.U(64.W)).asUInt)
        val __tmp_5147 = (10.U(8.W)).asUInt
        arrayRegFiles(__tmp_5146 + 0.U) := __tmp_5147(7, 0)

        CP := 83.U
      }

      is(83.U) {
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

      is(84.U) {
        /*
        $4 = ($0: IS[Z, Z])
        goto .85
        */



        generalRegFiles(4.U) := generalRegFiles(0.U)

        CP := 85.U
      }

      is(85.U) {
        /*
        $5 = *(($4: IS[Z, Z]) + (4: SP)) [signed, Z, 8]  // $5 = ($4: IS[Z, Z]).size
        goto .86
        */


        val __tmp_5148 = ((generalRegFiles(4.U) + 4.U(16.W))).asUInt
        generalRegFiles(5.U) := Cat(
          arrayRegFiles(__tmp_5148 + 7.U),
          arrayRegFiles(__tmp_5148 + 6.U),
          arrayRegFiles(__tmp_5148 + 5.U),
          arrayRegFiles(__tmp_5148 + 4.U),
          arrayRegFiles(__tmp_5148 + 3.U),
          arrayRegFiles(__tmp_5148 + 2.U),
          arrayRegFiles(__tmp_5148 + 1.U),
          arrayRegFiles(__tmp_5148 + 0.U)
        ).asSInt.pad(GENERAL_REG_WIDTH).asUInt

        CP := 86.U
      }

      is(86.U) {
        /*
        $6 = (($1: Z) < ($5: Z))
        goto .87
        */



        generalRegFiles(6.U) := (generalRegFiles(1.U).asSInt < generalRegFiles(5.U).asSInt).asUInt

        CP := 87.U
      }

      is(87.U) {
        /*
        if ($6: B) goto .88 else goto .100
        */


        CP := Mux((generalRegFiles(6.U).asUInt) === 1.U, 88.U, 100.U)
      }

      is(88.U) {
        /*
        $7 = ($0: IS[Z, Z])
        $5 = (($1: Z) + (1: Z))
        $8 = ($0: IS[Z, Z])
        goto .89
        */



        generalRegFiles(7.U) := generalRegFiles(0.U)


        generalRegFiles(5.U) := ((generalRegFiles(1.U).asSInt + 1.S(64.W))).asUInt


        generalRegFiles(8.U) := generalRegFiles(0.U)

        CP := 89.U
      }

      is(89.U) {
        /*
        if (((0: Z) <= ($1: Z)) & (($1: Z) <= *(($8: IS[Z, Z]) + (4: SP)))) goto .93 else goto .90
        */


        CP := Mux((((0.S(64.W) <= generalRegFiles(1.U).asSInt).asUInt & (generalRegFiles(1.U).asSInt <= Cat(
                            arrayRegFiles((generalRegFiles(8.U) + 4.U(16.W)) + 7.U),
                            arrayRegFiles((generalRegFiles(8.U) + 4.U(16.W)) + 6.U),
                            arrayRegFiles((generalRegFiles(8.U) + 4.U(16.W)) + 5.U),
                            arrayRegFiles((generalRegFiles(8.U) + 4.U(16.W)) + 4.U),
                            arrayRegFiles((generalRegFiles(8.U) + 4.U(16.W)) + 3.U),
                            arrayRegFiles((generalRegFiles(8.U) + 4.U(16.W)) + 2.U),
                            arrayRegFiles((generalRegFiles(8.U) + 4.U(16.W)) + 1.U),
                            arrayRegFiles((generalRegFiles(8.U) + 4.U(16.W)) + 0.U)
                          ).asSInt).asUInt).asUInt) === 1.U, 93.U, 90.U)
      }

      is(90.U) {
        /*
        *(((8: SP) + (12: SP)) + ((DP & (7: DP)) as SP)) = (73: U8) [unsigned, U8, 1]  // $display((DP & (7: DP))) = (73: U8)
        *(((8: SP) + (12: SP)) + (((DP + (1: DP)) & (7: DP)) as SP)) = (110: U8) [unsigned, U8, 1]  // $display(((DP + (1: DP)) & (7: DP))) = (110: U8)
        *(((8: SP) + (12: SP)) + (((DP + (2: DP)) & (7: DP)) as SP)) = (100: U8) [unsigned, U8, 1]  // $display(((DP + (2: DP)) & (7: DP))) = (100: U8)
        *(((8: SP) + (12: SP)) + (((DP + (3: DP)) & (7: DP)) as SP)) = (101: U8) [unsigned, U8, 1]  // $display(((DP + (3: DP)) & (7: DP))) = (101: U8)
        *(((8: SP) + (12: SP)) + (((DP + (4: DP)) & (7: DP)) as SP)) = (120: U8) [unsigned, U8, 1]  // $display(((DP + (4: DP)) & (7: DP))) = (120: U8)
        *(((8: SP) + (12: SP)) + (((DP + (5: DP)) & (7: DP)) as SP)) = (32: U8) [unsigned, U8, 1]  // $display(((DP + (5: DP)) & (7: DP))) = (32: U8)
        *(((8: SP) + (12: SP)) + (((DP + (6: DP)) & (7: DP)) as SP)) = (111: U8) [unsigned, U8, 1]  // $display(((DP + (6: DP)) & (7: DP))) = (111: U8)
        *(((8: SP) + (12: SP)) + (((DP + (7: DP)) & (7: DP)) as SP)) = (117: U8) [unsigned, U8, 1]  // $display(((DP + (7: DP)) & (7: DP))) = (117: U8)
        *(((8: SP) + (12: SP)) + (((DP + (8: DP)) & (7: DP)) as SP)) = (116: U8) [unsigned, U8, 1]  // $display(((DP + (8: DP)) & (7: DP))) = (116: U8)
        *(((8: SP) + (12: SP)) + (((DP + (9: DP)) & (7: DP)) as SP)) = (32: U8) [unsigned, U8, 1]  // $display(((DP + (9: DP)) & (7: DP))) = (32: U8)
        *(((8: SP) + (12: SP)) + (((DP + (10: DP)) & (7: DP)) as SP)) = (111: U8) [unsigned, U8, 1]  // $display(((DP + (10: DP)) & (7: DP))) = (111: U8)
        *(((8: SP) + (12: SP)) + (((DP + (11: DP)) & (7: DP)) as SP)) = (102: U8) [unsigned, U8, 1]  // $display(((DP + (11: DP)) & (7: DP))) = (102: U8)
        *(((8: SP) + (12: SP)) + (((DP + (12: DP)) & (7: DP)) as SP)) = (32: U8) [unsigned, U8, 1]  // $display(((DP + (12: DP)) & (7: DP))) = (32: U8)
        *(((8: SP) + (12: SP)) + (((DP + (13: DP)) & (7: DP)) as SP)) = (98: U8) [unsigned, U8, 1]  // $display(((DP + (13: DP)) & (7: DP))) = (98: U8)
        *(((8: SP) + (12: SP)) + (((DP + (14: DP)) & (7: DP)) as SP)) = (111: U8) [unsigned, U8, 1]  // $display(((DP + (14: DP)) & (7: DP))) = (111: U8)
        *(((8: SP) + (12: SP)) + (((DP + (15: DP)) & (7: DP)) as SP)) = (117: U8) [unsigned, U8, 1]  // $display(((DP + (15: DP)) & (7: DP))) = (117: U8)
        *(((8: SP) + (12: SP)) + (((DP + (16: DP)) & (7: DP)) as SP)) = (110: U8) [unsigned, U8, 1]  // $display(((DP + (16: DP)) & (7: DP))) = (110: U8)
        *(((8: SP) + (12: SP)) + (((DP + (17: DP)) & (7: DP)) as SP)) = (100: U8) [unsigned, U8, 1]  // $display(((DP + (17: DP)) & (7: DP))) = (100: U8)
        *(((8: SP) + (12: SP)) + (((DP + (18: DP)) & (7: DP)) as SP)) = (115: U8) [unsigned, U8, 1]  // $display(((DP + (18: DP)) & (7: DP))) = (115: U8)
        goto .249
        */


        val __tmp_5149 = ((8.U(16.W) + 12.U(16.W)) + (DP & 7.U(64.W)).asUInt)
        val __tmp_5150 = (73.U(8.W)).asUInt
        arrayRegFiles(__tmp_5149 + 0.U) := __tmp_5150(7, 0)

        val __tmp_5151 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 1.U(64.W)) & 7.U(64.W)).asUInt)
        val __tmp_5152 = (110.U(8.W)).asUInt
        arrayRegFiles(__tmp_5151 + 0.U) := __tmp_5152(7, 0)

        val __tmp_5153 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 2.U(64.W)) & 7.U(64.W)).asUInt)
        val __tmp_5154 = (100.U(8.W)).asUInt
        arrayRegFiles(__tmp_5153 + 0.U) := __tmp_5154(7, 0)

        val __tmp_5155 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 3.U(64.W)) & 7.U(64.W)).asUInt)
        val __tmp_5156 = (101.U(8.W)).asUInt
        arrayRegFiles(__tmp_5155 + 0.U) := __tmp_5156(7, 0)

        val __tmp_5157 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 4.U(64.W)) & 7.U(64.W)).asUInt)
        val __tmp_5158 = (120.U(8.W)).asUInt
        arrayRegFiles(__tmp_5157 + 0.U) := __tmp_5158(7, 0)

        val __tmp_5159 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 5.U(64.W)) & 7.U(64.W)).asUInt)
        val __tmp_5160 = (32.U(8.W)).asUInt
        arrayRegFiles(__tmp_5159 + 0.U) := __tmp_5160(7, 0)

        val __tmp_5161 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 6.U(64.W)) & 7.U(64.W)).asUInt)
        val __tmp_5162 = (111.U(8.W)).asUInt
        arrayRegFiles(__tmp_5161 + 0.U) := __tmp_5162(7, 0)

        val __tmp_5163 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 7.U(64.W)) & 7.U(64.W)).asUInt)
        val __tmp_5164 = (117.U(8.W)).asUInt
        arrayRegFiles(__tmp_5163 + 0.U) := __tmp_5164(7, 0)

        val __tmp_5165 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 8.U(64.W)) & 7.U(64.W)).asUInt)
        val __tmp_5166 = (116.U(8.W)).asUInt
        arrayRegFiles(__tmp_5165 + 0.U) := __tmp_5166(7, 0)

        val __tmp_5167 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 9.U(64.W)) & 7.U(64.W)).asUInt)
        val __tmp_5168 = (32.U(8.W)).asUInt
        arrayRegFiles(__tmp_5167 + 0.U) := __tmp_5168(7, 0)

        val __tmp_5169 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 10.U(64.W)) & 7.U(64.W)).asUInt)
        val __tmp_5170 = (111.U(8.W)).asUInt
        arrayRegFiles(__tmp_5169 + 0.U) := __tmp_5170(7, 0)

        val __tmp_5171 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 11.U(64.W)) & 7.U(64.W)).asUInt)
        val __tmp_5172 = (102.U(8.W)).asUInt
        arrayRegFiles(__tmp_5171 + 0.U) := __tmp_5172(7, 0)

        val __tmp_5173 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 12.U(64.W)) & 7.U(64.W)).asUInt)
        val __tmp_5174 = (32.U(8.W)).asUInt
        arrayRegFiles(__tmp_5173 + 0.U) := __tmp_5174(7, 0)

        val __tmp_5175 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 13.U(64.W)) & 7.U(64.W)).asUInt)
        val __tmp_5176 = (98.U(8.W)).asUInt
        arrayRegFiles(__tmp_5175 + 0.U) := __tmp_5176(7, 0)

        val __tmp_5177 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 14.U(64.W)) & 7.U(64.W)).asUInt)
        val __tmp_5178 = (111.U(8.W)).asUInt
        arrayRegFiles(__tmp_5177 + 0.U) := __tmp_5178(7, 0)

        val __tmp_5179 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 15.U(64.W)) & 7.U(64.W)).asUInt)
        val __tmp_5180 = (117.U(8.W)).asUInt
        arrayRegFiles(__tmp_5179 + 0.U) := __tmp_5180(7, 0)

        val __tmp_5181 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 16.U(64.W)) & 7.U(64.W)).asUInt)
        val __tmp_5182 = (110.U(8.W)).asUInt
        arrayRegFiles(__tmp_5181 + 0.U) := __tmp_5182(7, 0)

        val __tmp_5183 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 17.U(64.W)) & 7.U(64.W)).asUInt)
        val __tmp_5184 = (100.U(8.W)).asUInt
        arrayRegFiles(__tmp_5183 + 0.U) := __tmp_5184(7, 0)

        val __tmp_5185 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 18.U(64.W)) & 7.U(64.W)).asUInt)
        val __tmp_5186 = (115.U(8.W)).asUInt
        arrayRegFiles(__tmp_5185 + 0.U) := __tmp_5186(7, 0)

        CP := 249.U
      }

      is(93.U) {
        /*
        $9 = *((($8: IS[Z, Z]) + (12: SP)) + ((($1: Z) as SP) * (8: SP))) [signed, Z, 8]  // $9 = ($8: IS[Z, Z])(($1: Z))
        goto .94
        */


        val __tmp_5187 = (((generalRegFiles(8.U) + 12.U(16.W)) + (generalRegFiles(1.U).asSInt.asUInt * 8.U(16.W)))).asUInt
        generalRegFiles(9.U) := Cat(
          arrayRegFiles(__tmp_5187 + 7.U),
          arrayRegFiles(__tmp_5187 + 6.U),
          arrayRegFiles(__tmp_5187 + 5.U),
          arrayRegFiles(__tmp_5187 + 4.U),
          arrayRegFiles(__tmp_5187 + 3.U),
          arrayRegFiles(__tmp_5187 + 2.U),
          arrayRegFiles(__tmp_5187 + 1.U),
          arrayRegFiles(__tmp_5187 + 0.U)
        ).asSInt.pad(GENERAL_REG_WIDTH).asUInt

        CP := 94.U
      }

      is(94.U) {
        /*
        $10 = (($2: Z) + ($9: Z))
        goto .95
        */



        generalRegFiles(10.U) := ((generalRegFiles(2.U).asSInt + generalRegFiles(9.U).asSInt)).asUInt

        CP := 95.U
      }

      is(95.U) {
        /*
        alloc sum$res@[6,12].FB94AB9A: Z [@48, 8]
        goto .96
        */


        CP := 96.U
      }

      is(96.U) {
        /*
        SP = SP + 56
        goto .253
        */


        SP := SP + 56.U

        CP := 253.U
      }

      is(98.U) {
        /*
        SP = SP - 56
        goto .99
        */


        SP := SP - 56.U

        CP := 99.U
      }

      is(99.U) {
        /*
        **(SP + (2: SP)) = ($11: Z) [signed, Z, 8]  // $res = ($11: Z)
        goto $ret@0
        */


        val __tmp_5188 = Cat(
          arrayRegFiles((SP + 2.U(16.W)) + 1.U),
          arrayRegFiles((SP + 2.U(16.W)) + 0.U)
        )
        val __tmp_5189 = (generalRegFiles(11.U).asSInt).asUInt
        arrayRegFiles(__tmp_5188 + 0.U) := __tmp_5189(7, 0)
        arrayRegFiles(__tmp_5188 + 1.U) := __tmp_5189(15, 8)
        arrayRegFiles(__tmp_5188 + 2.U) := __tmp_5189(23, 16)
        arrayRegFiles(__tmp_5188 + 3.U) := __tmp_5189(31, 24)
        arrayRegFiles(__tmp_5188 + 4.U) := __tmp_5189(39, 32)
        arrayRegFiles(__tmp_5188 + 5.U) := __tmp_5189(47, 40)
        arrayRegFiles(__tmp_5188 + 6.U) := __tmp_5189(55, 48)
        arrayRegFiles(__tmp_5188 + 7.U) := __tmp_5189(63, 56)

        CP := Cat(
          arrayRegFiles(SP + 0.U + 1.U),
          arrayRegFiles(SP + 0.U + 0.U)
        )

      }

      is(100.U) {
        /*
        **(SP + (2: SP)) = ($2: Z) [signed, Z, 8]  // $res = ($2: Z)
        goto $ret@0
        */


        val __tmp_5190 = Cat(
          arrayRegFiles((SP + 2.U(16.W)) + 1.U),
          arrayRegFiles((SP + 2.U(16.W)) + 0.U)
        )
        val __tmp_5191 = (generalRegFiles(2.U).asSInt).asUInt
        arrayRegFiles(__tmp_5190 + 0.U) := __tmp_5191(7, 0)
        arrayRegFiles(__tmp_5190 + 1.U) := __tmp_5191(15, 8)
        arrayRegFiles(__tmp_5190 + 2.U) := __tmp_5191(23, 16)
        arrayRegFiles(__tmp_5190 + 3.U) := __tmp_5191(31, 24)
        arrayRegFiles(__tmp_5190 + 4.U) := __tmp_5191(39, 32)
        arrayRegFiles(__tmp_5190 + 5.U) := __tmp_5191(47, 40)
        arrayRegFiles(__tmp_5190 + 6.U) := __tmp_5191(55, 48)
        arrayRegFiles(__tmp_5190 + 7.U) := __tmp_5191(63, 56)

        CP := Cat(
          arrayRegFiles(SP + 0.U + 1.U),
          arrayRegFiles(SP + 0.U + 0.U)
        )

      }

      is(101.U) {
        /*
        $10 = (($3: S64) ≡ (-9223372036854775808: S64))
        goto .102
        */



        generalRegFiles(10.U) := (generalRegFiles(3.U).asSInt === BigInt("-9223372036854775808").S(64.W)).asUInt

        CP := 102.U
      }

      is(102.U) {
        /*
        if ($10: B) goto .103 else goto .163
        */


        CP := Mux((generalRegFiles(10.U).asUInt) === 1.U, 103.U, 163.U)
      }

      is(103.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $12 = (($1: anvil.PrinterIndex.U) & ($2: anvil.PrinterIndex.U))
        goto .104
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(12.U) := (generalRegFiles(1.U) & generalRegFiles(2.U))

        CP := 104.U
      }

      is(104.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($12: anvil.PrinterIndex.U) as SP)) = (45: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($12: anvil.PrinterIndex.U)) = (45: U8)
        goto .105
        */


        val __tmp_5192 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(12.U).asUInt)
        val __tmp_5193 = (45.U(8.W)).asUInt
        arrayRegFiles(__tmp_5192 + 0.U) := __tmp_5193(7, 0)

        CP := 105.U
      }

      is(105.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $13 = (($1: anvil.PrinterIndex.U) + (1: anvil.PrinterIndex.U))
        goto .106
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(13.U) := (generalRegFiles(1.U) + 1.U(64.W))

        CP := 106.U
      }

      is(106.U) {
        /*
        $14 = (($13: anvil.PrinterIndex.U) & ($2: anvil.PrinterIndex.U))
        goto .107
        */



        generalRegFiles(14.U) := (generalRegFiles(13.U) & generalRegFiles(2.U))

        CP := 107.U
      }

      is(107.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($14: anvil.PrinterIndex.U) as SP)) = (57: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($14: anvil.PrinterIndex.U)) = (57: U8)
        goto .108
        */


        val __tmp_5194 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(14.U).asUInt)
        val __tmp_5195 = (57.U(8.W)).asUInt
        arrayRegFiles(__tmp_5194 + 0.U) := __tmp_5195(7, 0)

        CP := 108.U
      }

      is(108.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $13 = (($1: anvil.PrinterIndex.U) + (2: anvil.PrinterIndex.U))
        goto .109
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(13.U) := (generalRegFiles(1.U) + 2.U(64.W))

        CP := 109.U
      }

      is(109.U) {
        /*
        $14 = (($13: anvil.PrinterIndex.U) & ($2: anvil.PrinterIndex.U))
        goto .110
        */



        generalRegFiles(14.U) := (generalRegFiles(13.U) & generalRegFiles(2.U))

        CP := 110.U
      }

      is(110.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($14: anvil.PrinterIndex.U) as SP)) = (50: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($14: anvil.PrinterIndex.U)) = (50: U8)
        goto .111
        */


        val __tmp_5196 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(14.U).asUInt)
        val __tmp_5197 = (50.U(8.W)).asUInt
        arrayRegFiles(__tmp_5196 + 0.U) := __tmp_5197(7, 0)

        CP := 111.U
      }

      is(111.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $13 = (($1: anvil.PrinterIndex.U) + (3: anvil.PrinterIndex.U))
        goto .112
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(13.U) := (generalRegFiles(1.U) + 3.U(64.W))

        CP := 112.U
      }

      is(112.U) {
        /*
        $14 = (($13: anvil.PrinterIndex.U) & ($2: anvil.PrinterIndex.U))
        goto .113
        */



        generalRegFiles(14.U) := (generalRegFiles(13.U) & generalRegFiles(2.U))

        CP := 113.U
      }

      is(113.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($14: anvil.PrinterIndex.U) as SP)) = (50: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($14: anvil.PrinterIndex.U)) = (50: U8)
        goto .114
        */


        val __tmp_5198 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(14.U).asUInt)
        val __tmp_5199 = (50.U(8.W)).asUInt
        arrayRegFiles(__tmp_5198 + 0.U) := __tmp_5199(7, 0)

        CP := 114.U
      }

      is(114.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $13 = (($1: anvil.PrinterIndex.U) + (4: anvil.PrinterIndex.U))
        goto .115
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(13.U) := (generalRegFiles(1.U) + 4.U(64.W))

        CP := 115.U
      }

      is(115.U) {
        /*
        $14 = (($13: anvil.PrinterIndex.U) & ($2: anvil.PrinterIndex.U))
        goto .116
        */



        generalRegFiles(14.U) := (generalRegFiles(13.U) & generalRegFiles(2.U))

        CP := 116.U
      }

      is(116.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($14: anvil.PrinterIndex.U) as SP)) = (51: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($14: anvil.PrinterIndex.U)) = (51: U8)
        goto .117
        */


        val __tmp_5200 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(14.U).asUInt)
        val __tmp_5201 = (51.U(8.W)).asUInt
        arrayRegFiles(__tmp_5200 + 0.U) := __tmp_5201(7, 0)

        CP := 117.U
      }

      is(117.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $13 = (($1: anvil.PrinterIndex.U) + (5: anvil.PrinterIndex.U))
        goto .118
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(13.U) := (generalRegFiles(1.U) + 5.U(64.W))

        CP := 118.U
      }

      is(118.U) {
        /*
        $14 = (($13: anvil.PrinterIndex.U) & ($2: anvil.PrinterIndex.U))
        goto .119
        */



        generalRegFiles(14.U) := (generalRegFiles(13.U) & generalRegFiles(2.U))

        CP := 119.U
      }

      is(119.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($14: anvil.PrinterIndex.U) as SP)) = (51: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($14: anvil.PrinterIndex.U)) = (51: U8)
        goto .120
        */


        val __tmp_5202 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(14.U).asUInt)
        val __tmp_5203 = (51.U(8.W)).asUInt
        arrayRegFiles(__tmp_5202 + 0.U) := __tmp_5203(7, 0)

        CP := 120.U
      }

      is(120.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $13 = (($1: anvil.PrinterIndex.U) + (6: anvil.PrinterIndex.U))
        goto .121
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(13.U) := (generalRegFiles(1.U) + 6.U(64.W))

        CP := 121.U
      }

      is(121.U) {
        /*
        $14 = (($13: anvil.PrinterIndex.U) & ($2: anvil.PrinterIndex.U))
        goto .122
        */



        generalRegFiles(14.U) := (generalRegFiles(13.U) & generalRegFiles(2.U))

        CP := 122.U
      }

      is(122.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($14: anvil.PrinterIndex.U) as SP)) = (55: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($14: anvil.PrinterIndex.U)) = (55: U8)
        goto .123
        */


        val __tmp_5204 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(14.U).asUInt)
        val __tmp_5205 = (55.U(8.W)).asUInt
        arrayRegFiles(__tmp_5204 + 0.U) := __tmp_5205(7, 0)

        CP := 123.U
      }

      is(123.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $13 = (($1: anvil.PrinterIndex.U) + (7: anvil.PrinterIndex.U))
        goto .124
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(13.U) := (generalRegFiles(1.U) + 7.U(64.W))

        CP := 124.U
      }

      is(124.U) {
        /*
        $14 = (($13: anvil.PrinterIndex.U) & ($2: anvil.PrinterIndex.U))
        goto .125
        */



        generalRegFiles(14.U) := (generalRegFiles(13.U) & generalRegFiles(2.U))

        CP := 125.U
      }

      is(125.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($14: anvil.PrinterIndex.U) as SP)) = (50: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($14: anvil.PrinterIndex.U)) = (50: U8)
        goto .126
        */


        val __tmp_5206 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(14.U).asUInt)
        val __tmp_5207 = (50.U(8.W)).asUInt
        arrayRegFiles(__tmp_5206 + 0.U) := __tmp_5207(7, 0)

        CP := 126.U
      }

      is(126.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $13 = (($1: anvil.PrinterIndex.U) + (8: anvil.PrinterIndex.U))
        goto .127
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(13.U) := (generalRegFiles(1.U) + 8.U(64.W))

        CP := 127.U
      }

      is(127.U) {
        /*
        $14 = (($13: anvil.PrinterIndex.U) & ($2: anvil.PrinterIndex.U))
        goto .128
        */



        generalRegFiles(14.U) := (generalRegFiles(13.U) & generalRegFiles(2.U))

        CP := 128.U
      }

      is(128.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($14: anvil.PrinterIndex.U) as SP)) = (48: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($14: anvil.PrinterIndex.U)) = (48: U8)
        goto .129
        */


        val __tmp_5208 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(14.U).asUInt)
        val __tmp_5209 = (48.U(8.W)).asUInt
        arrayRegFiles(__tmp_5208 + 0.U) := __tmp_5209(7, 0)

        CP := 129.U
      }

      is(129.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $13 = (($1: anvil.PrinterIndex.U) + (9: anvil.PrinterIndex.U))
        goto .130
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(13.U) := (generalRegFiles(1.U) + 9.U(64.W))

        CP := 130.U
      }

      is(130.U) {
        /*
        $14 = (($13: anvil.PrinterIndex.U) & ($2: anvil.PrinterIndex.U))
        goto .131
        */



        generalRegFiles(14.U) := (generalRegFiles(13.U) & generalRegFiles(2.U))

        CP := 131.U
      }

      is(131.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($14: anvil.PrinterIndex.U) as SP)) = (51: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($14: anvil.PrinterIndex.U)) = (51: U8)
        goto .132
        */


        val __tmp_5210 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(14.U).asUInt)
        val __tmp_5211 = (51.U(8.W)).asUInt
        arrayRegFiles(__tmp_5210 + 0.U) := __tmp_5211(7, 0)

        CP := 132.U
      }

      is(132.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $13 = (($1: anvil.PrinterIndex.U) + (10: anvil.PrinterIndex.U))
        goto .133
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(13.U) := (generalRegFiles(1.U) + 10.U(64.W))

        CP := 133.U
      }

      is(133.U) {
        /*
        $14 = (($13: anvil.PrinterIndex.U) & ($2: anvil.PrinterIndex.U))
        goto .134
        */



        generalRegFiles(14.U) := (generalRegFiles(13.U) & generalRegFiles(2.U))

        CP := 134.U
      }

      is(134.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($14: anvil.PrinterIndex.U) as SP)) = (54: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($14: anvil.PrinterIndex.U)) = (54: U8)
        goto .135
        */


        val __tmp_5212 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(14.U).asUInt)
        val __tmp_5213 = (54.U(8.W)).asUInt
        arrayRegFiles(__tmp_5212 + 0.U) := __tmp_5213(7, 0)

        CP := 135.U
      }

      is(135.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $13 = (($1: anvil.PrinterIndex.U) + (11: anvil.PrinterIndex.U))
        goto .136
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(13.U) := (generalRegFiles(1.U) + 11.U(64.W))

        CP := 136.U
      }

      is(136.U) {
        /*
        $14 = (($13: anvil.PrinterIndex.U) & ($2: anvil.PrinterIndex.U))
        goto .137
        */



        generalRegFiles(14.U) := (generalRegFiles(13.U) & generalRegFiles(2.U))

        CP := 137.U
      }

      is(137.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($14: anvil.PrinterIndex.U) as SP)) = (56: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($14: anvil.PrinterIndex.U)) = (56: U8)
        goto .138
        */


        val __tmp_5214 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(14.U).asUInt)
        val __tmp_5215 = (56.U(8.W)).asUInt
        arrayRegFiles(__tmp_5214 + 0.U) := __tmp_5215(7, 0)

        CP := 138.U
      }

      is(138.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $13 = (($1: anvil.PrinterIndex.U) + (12: anvil.PrinterIndex.U))
        goto .139
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(13.U) := (generalRegFiles(1.U) + 12.U(64.W))

        CP := 139.U
      }

      is(139.U) {
        /*
        $14 = (($13: anvil.PrinterIndex.U) & ($2: anvil.PrinterIndex.U))
        goto .140
        */



        generalRegFiles(14.U) := (generalRegFiles(13.U) & generalRegFiles(2.U))

        CP := 140.U
      }

      is(140.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($14: anvil.PrinterIndex.U) as SP)) = (53: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($14: anvil.PrinterIndex.U)) = (53: U8)
        goto .141
        */


        val __tmp_5216 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(14.U).asUInt)
        val __tmp_5217 = (53.U(8.W)).asUInt
        arrayRegFiles(__tmp_5216 + 0.U) := __tmp_5217(7, 0)

        CP := 141.U
      }

      is(141.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $13 = (($1: anvil.PrinterIndex.U) + (13: anvil.PrinterIndex.U))
        goto .142
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(13.U) := (generalRegFiles(1.U) + 13.U(64.W))

        CP := 142.U
      }

      is(142.U) {
        /*
        $14 = (($13: anvil.PrinterIndex.U) & ($2: anvil.PrinterIndex.U))
        goto .143
        */



        generalRegFiles(14.U) := (generalRegFiles(13.U) & generalRegFiles(2.U))

        CP := 143.U
      }

      is(143.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($14: anvil.PrinterIndex.U) as SP)) = (52: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($14: anvil.PrinterIndex.U)) = (52: U8)
        goto .144
        */


        val __tmp_5218 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(14.U).asUInt)
        val __tmp_5219 = (52.U(8.W)).asUInt
        arrayRegFiles(__tmp_5218 + 0.U) := __tmp_5219(7, 0)

        CP := 144.U
      }

      is(144.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $13 = (($1: anvil.PrinterIndex.U) + (14: anvil.PrinterIndex.U))
        goto .145
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(13.U) := (generalRegFiles(1.U) + 14.U(64.W))

        CP := 145.U
      }

      is(145.U) {
        /*
        $14 = (($13: anvil.PrinterIndex.U) & ($2: anvil.PrinterIndex.U))
        goto .146
        */



        generalRegFiles(14.U) := (generalRegFiles(13.U) & generalRegFiles(2.U))

        CP := 146.U
      }

      is(146.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($14: anvil.PrinterIndex.U) as SP)) = (55: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($14: anvil.PrinterIndex.U)) = (55: U8)
        goto .147
        */


        val __tmp_5220 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(14.U).asUInt)
        val __tmp_5221 = (55.U(8.W)).asUInt
        arrayRegFiles(__tmp_5220 + 0.U) := __tmp_5221(7, 0)

        CP := 147.U
      }

      is(147.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $13 = (($1: anvil.PrinterIndex.U) + (15: anvil.PrinterIndex.U))
        goto .148
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(13.U) := (generalRegFiles(1.U) + 15.U(64.W))

        CP := 148.U
      }

      is(148.U) {
        /*
        $14 = (($13: anvil.PrinterIndex.U) & ($2: anvil.PrinterIndex.U))
        goto .149
        */



        generalRegFiles(14.U) := (generalRegFiles(13.U) & generalRegFiles(2.U))

        CP := 149.U
      }

      is(149.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($14: anvil.PrinterIndex.U) as SP)) = (55: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($14: anvil.PrinterIndex.U)) = (55: U8)
        goto .150
        */


        val __tmp_5222 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(14.U).asUInt)
        val __tmp_5223 = (55.U(8.W)).asUInt
        arrayRegFiles(__tmp_5222 + 0.U) := __tmp_5223(7, 0)

        CP := 150.U
      }

      is(150.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $13 = (($1: anvil.PrinterIndex.U) + (16: anvil.PrinterIndex.U))
        goto .151
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(13.U) := (generalRegFiles(1.U) + 16.U(64.W))

        CP := 151.U
      }

      is(151.U) {
        /*
        $14 = (($13: anvil.PrinterIndex.U) & ($2: anvil.PrinterIndex.U))
        goto .152
        */



        generalRegFiles(14.U) := (generalRegFiles(13.U) & generalRegFiles(2.U))

        CP := 152.U
      }

      is(152.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($14: anvil.PrinterIndex.U) as SP)) = (53: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($14: anvil.PrinterIndex.U)) = (53: U8)
        goto .153
        */


        val __tmp_5224 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(14.U).asUInt)
        val __tmp_5225 = (53.U(8.W)).asUInt
        arrayRegFiles(__tmp_5224 + 0.U) := __tmp_5225(7, 0)

        CP := 153.U
      }

      is(153.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $13 = (($1: anvil.PrinterIndex.U) + (17: anvil.PrinterIndex.U))
        goto .154
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(13.U) := (generalRegFiles(1.U) + 17.U(64.W))

        CP := 154.U
      }

      is(154.U) {
        /*
        $14 = (($13: anvil.PrinterIndex.U) & ($2: anvil.PrinterIndex.U))
        goto .155
        */



        generalRegFiles(14.U) := (generalRegFiles(13.U) & generalRegFiles(2.U))

        CP := 155.U
      }

      is(155.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($14: anvil.PrinterIndex.U) as SP)) = (56: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($14: anvil.PrinterIndex.U)) = (56: U8)
        goto .156
        */


        val __tmp_5226 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(14.U).asUInt)
        val __tmp_5227 = (56.U(8.W)).asUInt
        arrayRegFiles(__tmp_5226 + 0.U) := __tmp_5227(7, 0)

        CP := 156.U
      }

      is(156.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $13 = (($1: anvil.PrinterIndex.U) + (18: anvil.PrinterIndex.U))
        goto .157
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(13.U) := (generalRegFiles(1.U) + 18.U(64.W))

        CP := 157.U
      }

      is(157.U) {
        /*
        $14 = (($13: anvil.PrinterIndex.U) & ($2: anvil.PrinterIndex.U))
        goto .158
        */



        generalRegFiles(14.U) := (generalRegFiles(13.U) & generalRegFiles(2.U))

        CP := 158.U
      }

      is(158.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($14: anvil.PrinterIndex.U) as SP)) = (48: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($14: anvil.PrinterIndex.U)) = (48: U8)
        goto .159
        */


        val __tmp_5228 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(14.U).asUInt)
        val __tmp_5229 = (48.U(8.W)).asUInt
        arrayRegFiles(__tmp_5228 + 0.U) := __tmp_5229(7, 0)

        CP := 159.U
      }

      is(159.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $13 = (($1: anvil.PrinterIndex.U) + (19: anvil.PrinterIndex.U))
        goto .160
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(13.U) := (generalRegFiles(1.U) + 19.U(64.W))

        CP := 160.U
      }

      is(160.U) {
        /*
        $14 = (($13: anvil.PrinterIndex.U) & ($2: anvil.PrinterIndex.U))
        goto .161
        */



        generalRegFiles(14.U) := (generalRegFiles(13.U) & generalRegFiles(2.U))

        CP := 161.U
      }

      is(161.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($14: anvil.PrinterIndex.U) as SP)) = (56: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($14: anvil.PrinterIndex.U)) = (56: U8)
        goto .162
        */


        val __tmp_5230 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(14.U).asUInt)
        val __tmp_5231 = (56.U(8.W)).asUInt
        arrayRegFiles(__tmp_5230 + 0.U) := __tmp_5231(7, 0)

        CP := 162.U
      }

      is(162.U) {
        /*
        **(SP + (2: SP)) = (20: U64) [unsigned, U64, 8]  // $res = (20: U64)
        goto $ret@0
        */


        val __tmp_5232 = Cat(
          arrayRegFiles((SP + 2.U(16.W)) + 1.U),
          arrayRegFiles((SP + 2.U(16.W)) + 0.U)
        )
        val __tmp_5233 = (20.U(64.W)).asUInt
        arrayRegFiles(__tmp_5232 + 0.U) := __tmp_5233(7, 0)
        arrayRegFiles(__tmp_5232 + 1.U) := __tmp_5233(15, 8)
        arrayRegFiles(__tmp_5232 + 2.U) := __tmp_5233(23, 16)
        arrayRegFiles(__tmp_5232 + 3.U) := __tmp_5233(31, 24)
        arrayRegFiles(__tmp_5232 + 4.U) := __tmp_5233(39, 32)
        arrayRegFiles(__tmp_5232 + 5.U) := __tmp_5233(47, 40)
        arrayRegFiles(__tmp_5232 + 6.U) := __tmp_5233(55, 48)
        arrayRegFiles(__tmp_5232 + 7.U) := __tmp_5233(63, 56)

        CP := Cat(
          arrayRegFiles(SP + 0.U + 1.U),
          arrayRegFiles(SP + 0.U + 0.U)
        )

      }

      is(163.U) {
        /*
        $10 = (($3: S64) ≡ (0: S64))
        goto .164
        */



        generalRegFiles(10.U) := (generalRegFiles(3.U).asSInt === 0.S(64.W)).asUInt

        CP := 164.U
      }

      is(164.U) {
        /*
        if ($10: B) goto .165 else goto .168
        */


        CP := Mux((generalRegFiles(10.U).asUInt) === 1.U, 165.U, 168.U)
      }

      is(165.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $12 = (($1: anvil.PrinterIndex.U) & ($2: anvil.PrinterIndex.U))
        goto .166
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(12.U) := (generalRegFiles(1.U) & generalRegFiles(2.U))

        CP := 166.U
      }

      is(166.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($12: anvil.PrinterIndex.U) as SP)) = (48: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($12: anvil.PrinterIndex.U)) = (48: U8)
        goto .167
        */


        val __tmp_5234 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(12.U).asUInt)
        val __tmp_5235 = (48.U(8.W)).asUInt
        arrayRegFiles(__tmp_5234 + 0.U) := __tmp_5235(7, 0)

        CP := 167.U
      }

      is(167.U) {
        /*
        **(SP + (2: SP)) = (1: U64) [unsigned, U64, 8]  // $res = (1: U64)
        goto $ret@0
        */


        val __tmp_5236 = Cat(
          arrayRegFiles((SP + 2.U(16.W)) + 1.U),
          arrayRegFiles((SP + 2.U(16.W)) + 0.U)
        )
        val __tmp_5237 = (1.U(64.W)).asUInt
        arrayRegFiles(__tmp_5236 + 0.U) := __tmp_5237(7, 0)
        arrayRegFiles(__tmp_5236 + 1.U) := __tmp_5237(15, 8)
        arrayRegFiles(__tmp_5236 + 2.U) := __tmp_5237(23, 16)
        arrayRegFiles(__tmp_5236 + 3.U) := __tmp_5237(31, 24)
        arrayRegFiles(__tmp_5236 + 4.U) := __tmp_5237(39, 32)
        arrayRegFiles(__tmp_5236 + 5.U) := __tmp_5237(47, 40)
        arrayRegFiles(__tmp_5236 + 6.U) := __tmp_5237(55, 48)
        arrayRegFiles(__tmp_5236 + 7.U) := __tmp_5237(63, 56)

        CP := Cat(
          arrayRegFiles(SP + 0.U + 1.U),
          arrayRegFiles(SP + 0.U + 0.U)
        )

      }

      is(168.U) {
        /*
        decl buff: MS[anvil.PrinterIndex.I20, U8] [@4, 34]
        alloc $new@[168,16].5BB7E063: MS[anvil.PrinterIndex.I20, U8] [@38, 34]
        $11 = (SP + (38: SP))
        *(SP + (38: SP)) = (323602724: U32) [unsigned, U32, 4]  // sha3 type signature of MS[anvil.PrinterIndex.I20, U8]: 0x1349C924
        *(SP + (42: SP)) = (20: Z) [signed, Z, 8]  // size of MS[anvil.PrinterIndex.I20, U8]((0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8))
        goto .169
        */



        generalRegFiles(11.U) := (SP + 38.U(16.W))

        val __tmp_5238 = (SP + 38.U(16.W))
        val __tmp_5239 = (323602724.U(32.W)).asUInt
        arrayRegFiles(__tmp_5238 + 0.U) := __tmp_5239(7, 0)
        arrayRegFiles(__tmp_5238 + 1.U) := __tmp_5239(15, 8)
        arrayRegFiles(__tmp_5238 + 2.U) := __tmp_5239(23, 16)
        arrayRegFiles(__tmp_5238 + 3.U) := __tmp_5239(31, 24)

        val __tmp_5240 = (SP + 42.U(16.W))
        val __tmp_5241 = (20.S(64.W)).asUInt
        arrayRegFiles(__tmp_5240 + 0.U) := __tmp_5241(7, 0)
        arrayRegFiles(__tmp_5240 + 1.U) := __tmp_5241(15, 8)
        arrayRegFiles(__tmp_5240 + 2.U) := __tmp_5241(23, 16)
        arrayRegFiles(__tmp_5240 + 3.U) := __tmp_5241(31, 24)
        arrayRegFiles(__tmp_5240 + 4.U) := __tmp_5241(39, 32)
        arrayRegFiles(__tmp_5240 + 5.U) := __tmp_5241(47, 40)
        arrayRegFiles(__tmp_5240 + 6.U) := __tmp_5241(55, 48)
        arrayRegFiles(__tmp_5240 + 7.U) := __tmp_5241(63, 56)

        CP := 169.U
      }

      is(169.U) {
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
        goto .170
        */


        val __tmp_5242 = ((generalRegFiles(11.U) + 12.U(16.W)) + 0.S(8.W).asUInt)
        val __tmp_5243 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5242 + 0.U) := __tmp_5243(7, 0)

        val __tmp_5244 = ((generalRegFiles(11.U) + 12.U(16.W)) + 1.S(8.W).asUInt)
        val __tmp_5245 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5244 + 0.U) := __tmp_5245(7, 0)

        val __tmp_5246 = ((generalRegFiles(11.U) + 12.U(16.W)) + 2.S(8.W).asUInt)
        val __tmp_5247 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5246 + 0.U) := __tmp_5247(7, 0)

        val __tmp_5248 = ((generalRegFiles(11.U) + 12.U(16.W)) + 3.S(8.W).asUInt)
        val __tmp_5249 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5248 + 0.U) := __tmp_5249(7, 0)

        val __tmp_5250 = ((generalRegFiles(11.U) + 12.U(16.W)) + 4.S(8.W).asUInt)
        val __tmp_5251 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5250 + 0.U) := __tmp_5251(7, 0)

        val __tmp_5252 = ((generalRegFiles(11.U) + 12.U(16.W)) + 5.S(8.W).asUInt)
        val __tmp_5253 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5252 + 0.U) := __tmp_5253(7, 0)

        val __tmp_5254 = ((generalRegFiles(11.U) + 12.U(16.W)) + 6.S(8.W).asUInt)
        val __tmp_5255 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5254 + 0.U) := __tmp_5255(7, 0)

        val __tmp_5256 = ((generalRegFiles(11.U) + 12.U(16.W)) + 7.S(8.W).asUInt)
        val __tmp_5257 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5256 + 0.U) := __tmp_5257(7, 0)

        val __tmp_5258 = ((generalRegFiles(11.U) + 12.U(16.W)) + 8.S(8.W).asUInt)
        val __tmp_5259 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5258 + 0.U) := __tmp_5259(7, 0)

        val __tmp_5260 = ((generalRegFiles(11.U) + 12.U(16.W)) + 9.S(8.W).asUInt)
        val __tmp_5261 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5260 + 0.U) := __tmp_5261(7, 0)

        val __tmp_5262 = ((generalRegFiles(11.U) + 12.U(16.W)) + 10.S(8.W).asUInt)
        val __tmp_5263 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5262 + 0.U) := __tmp_5263(7, 0)

        val __tmp_5264 = ((generalRegFiles(11.U) + 12.U(16.W)) + 11.S(8.W).asUInt)
        val __tmp_5265 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5264 + 0.U) := __tmp_5265(7, 0)

        val __tmp_5266 = ((generalRegFiles(11.U) + 12.U(16.W)) + 12.S(8.W).asUInt)
        val __tmp_5267 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5266 + 0.U) := __tmp_5267(7, 0)

        val __tmp_5268 = ((generalRegFiles(11.U) + 12.U(16.W)) + 13.S(8.W).asUInt)
        val __tmp_5269 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5268 + 0.U) := __tmp_5269(7, 0)

        val __tmp_5270 = ((generalRegFiles(11.U) + 12.U(16.W)) + 14.S(8.W).asUInt)
        val __tmp_5271 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5270 + 0.U) := __tmp_5271(7, 0)

        val __tmp_5272 = ((generalRegFiles(11.U) + 12.U(16.W)) + 15.S(8.W).asUInt)
        val __tmp_5273 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5272 + 0.U) := __tmp_5273(7, 0)

        val __tmp_5274 = ((generalRegFiles(11.U) + 12.U(16.W)) + 16.S(8.W).asUInt)
        val __tmp_5275 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5274 + 0.U) := __tmp_5275(7, 0)

        val __tmp_5276 = ((generalRegFiles(11.U) + 12.U(16.W)) + 17.S(8.W).asUInt)
        val __tmp_5277 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5276 + 0.U) := __tmp_5277(7, 0)

        val __tmp_5278 = ((generalRegFiles(11.U) + 12.U(16.W)) + 18.S(8.W).asUInt)
        val __tmp_5279 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5278 + 0.U) := __tmp_5279(7, 0)

        val __tmp_5280 = ((generalRegFiles(11.U) + 12.U(16.W)) + 19.S(8.W).asUInt)
        val __tmp_5281 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5280 + 0.U) := __tmp_5281(7, 0)

        CP := 170.U
      }

      is(170.U) {
        /*
        (SP + (4: SP)) [MS[anvil.PrinterIndex.I20, U8], 34]  <-  ($11: MS[anvil.PrinterIndex.I20, U8]) [MS[anvil.PrinterIndex.I20, U8], (34: SP)]  // buff = ($11: MS[anvil.PrinterIndex.I20, U8])
        goto .258
        */


        val __tmp_5282 = (SP + 4.U(16.W))
        val __tmp_5283 = generalRegFiles(11.U)
        val __tmp_5284 = 34.U(16.W)

        when(Idx < __tmp_5284) {
          arrayRegFiles(__tmp_5282 + Idx + 0.U) := arrayRegFiles(__tmp_5283 + Idx + 0.U)
          arrayRegFiles(__tmp_5282 + Idx + 1.U) := arrayRegFiles(__tmp_5283 + Idx + 1.U)
          arrayRegFiles(__tmp_5282 + Idx + 2.U) := arrayRegFiles(__tmp_5283 + Idx + 2.U)
          arrayRegFiles(__tmp_5282 + Idx + 3.U) := arrayRegFiles(__tmp_5283 + Idx + 3.U)
          arrayRegFiles(__tmp_5282 + Idx + 4.U) := arrayRegFiles(__tmp_5283 + Idx + 4.U)
          arrayRegFiles(__tmp_5282 + Idx + 5.U) := arrayRegFiles(__tmp_5283 + Idx + 5.U)
          arrayRegFiles(__tmp_5282 + Idx + 6.U) := arrayRegFiles(__tmp_5283 + Idx + 6.U)
          arrayRegFiles(__tmp_5282 + Idx + 7.U) := arrayRegFiles(__tmp_5283 + Idx + 7.U)
          Idx := Idx + 8.U
          LeftByteRounds := __tmp_5284 - Idx
        } .elsewhen(IdxLeftByteRounds < LeftByteRounds) {
          val __tmp_5285 = Idx - 8.U
          arrayRegFiles(__tmp_5282 + __tmp_5285 + IdxLeftByteRounds) := arrayRegFiles(__tmp_5283 + __tmp_5285 + IdxLeftByteRounds)
          IdxLeftByteRounds := IdxLeftByteRounds + 1.U
        } .otherwise {
          Idx := 0.U
          IdxLeftByteRounds := 0.U
          LeftByteRounds := 0.U
          CP := 258.U
        }


      }

      is(171.U) {
        /*
        decl i: anvil.PrinterIndex.I20 @$4
        $4 = (0: anvil.PrinterIndex.I20)
        goto .172
        */



        generalRegFiles(4.U) := (0.S(8.W)).asUInt

        CP := 172.U
      }

      is(172.U) {
        /*
        decl neg: B @$5
        $10 = (($3: S64) < (0: S64))
        goto .173
        */



        generalRegFiles(10.U) := (generalRegFiles(3.U).asSInt < 0.S(64.W)).asUInt

        CP := 173.U
      }

      is(173.U) {
        /*
        $5 = ($10: B)
        goto .174
        */



        generalRegFiles(5.U) := generalRegFiles(10.U)

        CP := 174.U
      }

      is(174.U) {
        /*
        decl m: S64 @$6
        goto .175
        */


        CP := 175.U
      }

      is(175.U) {
        /*
        if ($5: B) goto .176 else goto .178
        */


        CP := Mux((generalRegFiles(5.U).asUInt) === 1.U, 176.U, 178.U)
      }

      is(176.U) {
        /*
        $12 = -(($3: S64))
        goto .177
        */



        generalRegFiles(12.U) := (-generalRegFiles(3.U).asSInt).asUInt

        CP := 177.U
      }

      is(177.U) {
        /*
        $10 = ($12: S64)
        goto .180
        */



        generalRegFiles(10.U) := (generalRegFiles(12.U).asSInt).asUInt

        CP := 180.U
      }

      is(178.U) {
        /*
        $14 = ($3: S64)
        goto .179
        */



        generalRegFiles(14.U) := (generalRegFiles(3.U).asSInt).asUInt

        CP := 179.U
      }

      is(179.U) {
        /*
        $10 = ($14: S64)
        goto .180
        */



        generalRegFiles(10.U) := (generalRegFiles(14.U).asSInt).asUInt

        CP := 180.U
      }

      is(180.U) {
        /*
        $6 = ($10: S64)
        goto .181
        */



        generalRegFiles(6.U) := (generalRegFiles(10.U).asSInt).asUInt

        CP := 181.U
      }

      is(181.U) {
        /*
        $11 = ($6: S64)
        goto .182
        */



        generalRegFiles(11.U) := (generalRegFiles(6.U).asSInt).asUInt

        CP := 182.U
      }

      is(182.U) {
        /*
        $10 = (($11: S64) > (0: S64))
        goto .183
        */



        generalRegFiles(10.U) := (generalRegFiles(11.U).asSInt > 0.S(64.W)).asUInt

        CP := 183.U
      }

      is(183.U) {
        /*
        if ($10: B) goto .184 else goto .213
        */


        CP := Mux((generalRegFiles(10.U).asUInt) === 1.U, 184.U, 213.U)
      }

      is(184.U) {
        /*
        $11 = ($6: S64)
        goto .185
        */



        generalRegFiles(11.U) := (generalRegFiles(6.U).asSInt).asUInt

        CP := 185.U
      }

      is(185.U) {
        /*
        $10 = (($11: S64) % (10: S64))
        goto .186
        */



        generalRegFiles(10.U) := ((generalRegFiles(11.U).asSInt % 10.S(64.W))).asUInt

        CP := 186.U
      }

      is(186.U) {
        /*
        switch (($10: S64))
          (0: S64): goto 187
          (1: S64): goto 189
          (2: S64): goto 191
          (3: S64): goto 193
          (4: S64): goto 195
          (5: S64): goto 197
          (6: S64): goto 199
          (7: S64): goto 201
          (8: S64): goto 203
          (9: S64): goto 205

        */


        val __tmp_5286 = generalRegFiles(10.U).asSInt

        switch(__tmp_5286) {

          is(0.S(64.W)) {
            CP := 187.U
          }


          is(1.S(64.W)) {
            CP := 189.U
          }


          is(2.S(64.W)) {
            CP := 191.U
          }


          is(3.S(64.W)) {
            CP := 193.U
          }


          is(4.S(64.W)) {
            CP := 195.U
          }


          is(5.S(64.W)) {
            CP := 197.U
          }


          is(6.S(64.W)) {
            CP := 199.U
          }


          is(7.S(64.W)) {
            CP := 201.U
          }


          is(8.S(64.W)) {
            CP := 203.U
          }


          is(9.S(64.W)) {
            CP := 205.U
          }

        }

      }

      is(187.U) {
        /*
        $11 = (SP + (4: SP))
        $10 = ($4: anvil.PrinterIndex.I20)
        goto .188
        */



        generalRegFiles(11.U) := (SP + 4.U(16.W))


        generalRegFiles(10.U) := (generalRegFiles(4.U).asSInt).asUInt

        CP := 188.U
      }

      is(188.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.I20, U8]) + (12: SP)) + (($10: anvil.PrinterIndex.I20) as SP)) = (48: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.I20, U8])(($10: anvil.PrinterIndex.I20)) = (48: U8)
        goto .207
        */


        val __tmp_5287 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(10.U).asSInt.asUInt)
        val __tmp_5288 = (48.U(8.W)).asUInt
        arrayRegFiles(__tmp_5287 + 0.U) := __tmp_5288(7, 0)

        CP := 207.U
      }

      is(189.U) {
        /*
        $11 = (SP + (4: SP))
        $10 = ($4: anvil.PrinterIndex.I20)
        goto .190
        */



        generalRegFiles(11.U) := (SP + 4.U(16.W))


        generalRegFiles(10.U) := (generalRegFiles(4.U).asSInt).asUInt

        CP := 190.U
      }

      is(190.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.I20, U8]) + (12: SP)) + (($10: anvil.PrinterIndex.I20) as SP)) = (49: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.I20, U8])(($10: anvil.PrinterIndex.I20)) = (49: U8)
        goto .207
        */


        val __tmp_5289 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(10.U).asSInt.asUInt)
        val __tmp_5290 = (49.U(8.W)).asUInt
        arrayRegFiles(__tmp_5289 + 0.U) := __tmp_5290(7, 0)

        CP := 207.U
      }

      is(191.U) {
        /*
        $11 = (SP + (4: SP))
        $10 = ($4: anvil.PrinterIndex.I20)
        goto .192
        */



        generalRegFiles(11.U) := (SP + 4.U(16.W))


        generalRegFiles(10.U) := (generalRegFiles(4.U).asSInt).asUInt

        CP := 192.U
      }

      is(192.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.I20, U8]) + (12: SP)) + (($10: anvil.PrinterIndex.I20) as SP)) = (50: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.I20, U8])(($10: anvil.PrinterIndex.I20)) = (50: U8)
        goto .207
        */


        val __tmp_5291 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(10.U).asSInt.asUInt)
        val __tmp_5292 = (50.U(8.W)).asUInt
        arrayRegFiles(__tmp_5291 + 0.U) := __tmp_5292(7, 0)

        CP := 207.U
      }

      is(193.U) {
        /*
        $11 = (SP + (4: SP))
        $10 = ($4: anvil.PrinterIndex.I20)
        goto .194
        */



        generalRegFiles(11.U) := (SP + 4.U(16.W))


        generalRegFiles(10.U) := (generalRegFiles(4.U).asSInt).asUInt

        CP := 194.U
      }

      is(194.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.I20, U8]) + (12: SP)) + (($10: anvil.PrinterIndex.I20) as SP)) = (51: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.I20, U8])(($10: anvil.PrinterIndex.I20)) = (51: U8)
        goto .207
        */


        val __tmp_5293 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(10.U).asSInt.asUInt)
        val __tmp_5294 = (51.U(8.W)).asUInt
        arrayRegFiles(__tmp_5293 + 0.U) := __tmp_5294(7, 0)

        CP := 207.U
      }

      is(195.U) {
        /*
        $11 = (SP + (4: SP))
        $10 = ($4: anvil.PrinterIndex.I20)
        goto .196
        */



        generalRegFiles(11.U) := (SP + 4.U(16.W))


        generalRegFiles(10.U) := (generalRegFiles(4.U).asSInt).asUInt

        CP := 196.U
      }

      is(196.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.I20, U8]) + (12: SP)) + (($10: anvil.PrinterIndex.I20) as SP)) = (52: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.I20, U8])(($10: anvil.PrinterIndex.I20)) = (52: U8)
        goto .207
        */


        val __tmp_5295 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(10.U).asSInt.asUInt)
        val __tmp_5296 = (52.U(8.W)).asUInt
        arrayRegFiles(__tmp_5295 + 0.U) := __tmp_5296(7, 0)

        CP := 207.U
      }

      is(197.U) {
        /*
        $11 = (SP + (4: SP))
        $10 = ($4: anvil.PrinterIndex.I20)
        goto .198
        */



        generalRegFiles(11.U) := (SP + 4.U(16.W))


        generalRegFiles(10.U) := (generalRegFiles(4.U).asSInt).asUInt

        CP := 198.U
      }

      is(198.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.I20, U8]) + (12: SP)) + (($10: anvil.PrinterIndex.I20) as SP)) = (53: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.I20, U8])(($10: anvil.PrinterIndex.I20)) = (53: U8)
        goto .207
        */


        val __tmp_5297 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(10.U).asSInt.asUInt)
        val __tmp_5298 = (53.U(8.W)).asUInt
        arrayRegFiles(__tmp_5297 + 0.U) := __tmp_5298(7, 0)

        CP := 207.U
      }

      is(199.U) {
        /*
        $11 = (SP + (4: SP))
        $10 = ($4: anvil.PrinterIndex.I20)
        goto .200
        */



        generalRegFiles(11.U) := (SP + 4.U(16.W))


        generalRegFiles(10.U) := (generalRegFiles(4.U).asSInt).asUInt

        CP := 200.U
      }

      is(200.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.I20, U8]) + (12: SP)) + (($10: anvil.PrinterIndex.I20) as SP)) = (54: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.I20, U8])(($10: anvil.PrinterIndex.I20)) = (54: U8)
        goto .207
        */


        val __tmp_5299 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(10.U).asSInt.asUInt)
        val __tmp_5300 = (54.U(8.W)).asUInt
        arrayRegFiles(__tmp_5299 + 0.U) := __tmp_5300(7, 0)

        CP := 207.U
      }

      is(201.U) {
        /*
        $11 = (SP + (4: SP))
        $10 = ($4: anvil.PrinterIndex.I20)
        goto .202
        */



        generalRegFiles(11.U) := (SP + 4.U(16.W))


        generalRegFiles(10.U) := (generalRegFiles(4.U).asSInt).asUInt

        CP := 202.U
      }

      is(202.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.I20, U8]) + (12: SP)) + (($10: anvil.PrinterIndex.I20) as SP)) = (55: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.I20, U8])(($10: anvil.PrinterIndex.I20)) = (55: U8)
        goto .207
        */


        val __tmp_5301 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(10.U).asSInt.asUInt)
        val __tmp_5302 = (55.U(8.W)).asUInt
        arrayRegFiles(__tmp_5301 + 0.U) := __tmp_5302(7, 0)

        CP := 207.U
      }

      is(203.U) {
        /*
        $11 = (SP + (4: SP))
        $10 = ($4: anvil.PrinterIndex.I20)
        goto .204
        */



        generalRegFiles(11.U) := (SP + 4.U(16.W))


        generalRegFiles(10.U) := (generalRegFiles(4.U).asSInt).asUInt

        CP := 204.U
      }

      is(204.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.I20, U8]) + (12: SP)) + (($10: anvil.PrinterIndex.I20) as SP)) = (56: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.I20, U8])(($10: anvil.PrinterIndex.I20)) = (56: U8)
        goto .207
        */


        val __tmp_5303 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(10.U).asSInt.asUInt)
        val __tmp_5304 = (56.U(8.W)).asUInt
        arrayRegFiles(__tmp_5303 + 0.U) := __tmp_5304(7, 0)

        CP := 207.U
      }

      is(205.U) {
        /*
        $11 = (SP + (4: SP))
        $10 = ($4: anvil.PrinterIndex.I20)
        goto .206
        */



        generalRegFiles(11.U) := (SP + 4.U(16.W))


        generalRegFiles(10.U) := (generalRegFiles(4.U).asSInt).asUInt

        CP := 206.U
      }

      is(206.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.I20, U8]) + (12: SP)) + (($10: anvil.PrinterIndex.I20) as SP)) = (57: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.I20, U8])(($10: anvil.PrinterIndex.I20)) = (57: U8)
        goto .207
        */


        val __tmp_5305 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(10.U).asSInt.asUInt)
        val __tmp_5306 = (57.U(8.W)).asUInt
        arrayRegFiles(__tmp_5305 + 0.U) := __tmp_5306(7, 0)

        CP := 207.U
      }

      is(207.U) {
        /*
        $11 = ($6: S64)
        goto .208
        */



        generalRegFiles(11.U) := (generalRegFiles(6.U).asSInt).asUInt

        CP := 208.U
      }

      is(208.U) {
        /*
        $10 = (($11: S64) / (10: S64))
        goto .209
        */



        generalRegFiles(10.U) := ((generalRegFiles(11.U).asSInt / 10.S(64.W))).asUInt

        CP := 209.U
      }

      is(209.U) {
        /*
        $6 = ($10: S64)
        goto .210
        */



        generalRegFiles(6.U) := (generalRegFiles(10.U).asSInt).asUInt

        CP := 210.U
      }

      is(210.U) {
        /*
        $11 = ($4: anvil.PrinterIndex.I20)
        goto .211
        */



        generalRegFiles(11.U) := (generalRegFiles(4.U).asSInt).asUInt

        CP := 211.U
      }

      is(211.U) {
        /*
        $10 = (($11: anvil.PrinterIndex.I20) + (1: anvil.PrinterIndex.I20))
        goto .212
        */



        generalRegFiles(10.U) := ((generalRegFiles(11.U).asSInt + 1.S(8.W))).asUInt

        CP := 212.U
      }

      is(212.U) {
        /*
        $4 = ($10: anvil.PrinterIndex.I20)
        goto .181
        */



        generalRegFiles(4.U) := (generalRegFiles(10.U).asSInt).asUInt

        CP := 181.U
      }

      is(213.U) {
        /*
        $11 = ($5: B)
        goto .259
        */



        generalRegFiles(11.U) := generalRegFiles(5.U)

        CP := 259.U
      }

      is(214.U) {
        /*
        if ($11: B) goto .215 else goto .220
        */


        CP := Mux((generalRegFiles(11.U).asUInt) === 1.U, 215.U, 220.U)
      }

      is(215.U) {
        /*
        $11 = (SP + (4: SP))
        $10 = ($4: anvil.PrinterIndex.I20)
        goto .216
        */



        generalRegFiles(11.U) := (SP + 4.U(16.W))


        generalRegFiles(10.U) := (generalRegFiles(4.U).asSInt).asUInt

        CP := 216.U
      }

      is(216.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.I20, U8]) + (12: SP)) + (($10: anvil.PrinterIndex.I20) as SP)) = (45: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.I20, U8])(($10: anvil.PrinterIndex.I20)) = (45: U8)
        goto .217
        */


        val __tmp_5307 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(10.U).asSInt.asUInt)
        val __tmp_5308 = (45.U(8.W)).asUInt
        arrayRegFiles(__tmp_5307 + 0.U) := __tmp_5308(7, 0)

        CP := 217.U
      }

      is(217.U) {
        /*
        $11 = ($4: anvil.PrinterIndex.I20)
        goto .218
        */



        generalRegFiles(11.U) := (generalRegFiles(4.U).asSInt).asUInt

        CP := 218.U
      }

      is(218.U) {
        /*
        $10 = (($11: anvil.PrinterIndex.I20) + (1: anvil.PrinterIndex.I20))
        goto .219
        */



        generalRegFiles(10.U) := ((generalRegFiles(11.U).asSInt + 1.S(8.W))).asUInt

        CP := 219.U
      }

      is(219.U) {
        /*
        $4 = ($10: anvil.PrinterIndex.I20)
        goto .220
        */



        generalRegFiles(4.U) := (generalRegFiles(10.U).asSInt).asUInt

        CP := 220.U
      }

      is(220.U) {
        /*
        decl j: anvil.PrinterIndex.I20 @$7
        $11 = ($4: anvil.PrinterIndex.I20)
        goto .260
        */



        generalRegFiles(11.U) := (generalRegFiles(4.U).asSInt).asUInt

        CP := 260.U
      }

      is(221.U) {
        /*
        $10 = (($11: anvil.PrinterIndex.I20) - (1: anvil.PrinterIndex.I20))
        goto .222
        */



        generalRegFiles(10.U) := ((generalRegFiles(11.U).asSInt - 1.S(8.W))).asUInt

        CP := 222.U
      }

      is(222.U) {
        /*
        $7 = ($10: anvil.PrinterIndex.I20)
        goto .223
        */



        generalRegFiles(7.U) := (generalRegFiles(10.U).asSInt).asUInt

        CP := 223.U
      }

      is(223.U) {
        /*
        decl idx: anvil.PrinterIndex.U @$8
        $11 = ($1: anvil.PrinterIndex.U)
        goto .224
        */



        generalRegFiles(11.U) := generalRegFiles(1.U)

        CP := 224.U
      }

      is(224.U) {
        /*
        $8 = ($11: anvil.PrinterIndex.U)
        goto .225
        */



        generalRegFiles(8.U) := generalRegFiles(11.U)

        CP := 225.U
      }

      is(225.U) {
        /*
        decl r: U64 @$9
        $9 = (0: U64)
        goto .226
        */



        generalRegFiles(9.U) := 0.U(64.W)

        CP := 226.U
      }

      is(226.U) {
        /*
        $11 = ($7: anvil.PrinterIndex.I20)
        goto .227
        */



        generalRegFiles(11.U) := (generalRegFiles(7.U).asSInt).asUInt

        CP := 227.U
      }

      is(227.U) {
        /*
        $10 = (($11: anvil.PrinterIndex.I20) >= (0: anvil.PrinterIndex.I20))
        goto .228
        */



        generalRegFiles(10.U) := (generalRegFiles(11.U).asSInt >= 0.S(8.W)).asUInt

        CP := 228.U
      }

      is(228.U) {
        /*
        if ($10: B) goto .229 else goto .243
        */


        CP := Mux((generalRegFiles(10.U).asUInt) === 1.U, 229.U, 243.U)
      }

      is(229.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $10 = ($8: anvil.PrinterIndex.U)
        $13 = ($2: anvil.PrinterIndex.U)
        goto .230
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(10.U) := generalRegFiles(8.U)


        generalRegFiles(13.U) := generalRegFiles(2.U)

        CP := 230.U
      }

      is(230.U) {
        /*
        $12 = (($10: anvil.PrinterIndex.U) & ($13: anvil.PrinterIndex.U))
        goto .231
        */



        generalRegFiles(12.U) := (generalRegFiles(10.U) & generalRegFiles(13.U))

        CP := 231.U
      }

      is(231.U) {
        /*
        $14 = (SP + (4: SP))
        $15 = ($7: anvil.PrinterIndex.I20)
        goto .232
        */



        generalRegFiles(14.U) := (SP + 4.U(16.W))


        generalRegFiles(15.U) := (generalRegFiles(7.U).asSInt).asUInt

        CP := 232.U
      }

      is(232.U) {
        /*
        $16 = *((($14: MS[anvil.PrinterIndex.I20, U8]) + (12: SP)) + (($15: anvil.PrinterIndex.I20) as SP)) [unsigned, U8, 1]  // $16 = ($14: MS[anvil.PrinterIndex.I20, U8])(($15: anvil.PrinterIndex.I20))
        goto .233
        */


        val __tmp_5309 = (((generalRegFiles(14.U) + 12.U(16.W)) + generalRegFiles(15.U).asSInt.asUInt)).asUInt
        generalRegFiles(16.U) := Cat(
          arrayRegFiles(__tmp_5309 + 0.U)
        ).asUInt

        CP := 233.U
      }

      is(233.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($12: anvil.PrinterIndex.U) as SP)) = ($16: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($12: anvil.PrinterIndex.U)) = ($16: U8)
        goto .234
        */


        val __tmp_5310 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(12.U).asUInt)
        val __tmp_5311 = (generalRegFiles(16.U)).asUInt
        arrayRegFiles(__tmp_5310 + 0.U) := __tmp_5311(7, 0)

        CP := 234.U
      }

      is(234.U) {
        /*
        $11 = ($7: anvil.PrinterIndex.I20)
        goto .235
        */



        generalRegFiles(11.U) := (generalRegFiles(7.U).asSInt).asUInt

        CP := 235.U
      }

      is(235.U) {
        /*
        $10 = (($11: anvil.PrinterIndex.I20) - (1: anvil.PrinterIndex.I20))
        goto .236
        */



        generalRegFiles(10.U) := ((generalRegFiles(11.U).asSInt - 1.S(8.W))).asUInt

        CP := 236.U
      }

      is(236.U) {
        /*
        $7 = ($10: anvil.PrinterIndex.I20)
        goto .237
        */



        generalRegFiles(7.U) := (generalRegFiles(10.U).asSInt).asUInt

        CP := 237.U
      }

      is(237.U) {
        /*
        $11 = ($8: anvil.PrinterIndex.U)
        goto .238
        */



        generalRegFiles(11.U) := generalRegFiles(8.U)

        CP := 238.U
      }

      is(238.U) {
        /*
        $10 = (($11: anvil.PrinterIndex.U) + (1: anvil.PrinterIndex.U))
        goto .239
        */



        generalRegFiles(10.U) := (generalRegFiles(11.U) + 1.U(64.W))

        CP := 239.U
      }

      is(239.U) {
        /*
        $8 = ($10: anvil.PrinterIndex.U)
        goto .240
        */



        generalRegFiles(8.U) := generalRegFiles(10.U)

        CP := 240.U
      }

      is(240.U) {
        /*
        $11 = ($9: U64)
        goto .241
        */



        generalRegFiles(11.U) := generalRegFiles(9.U)

        CP := 241.U
      }

      is(241.U) {
        /*
        $10 = (($11: U64) + (1: U64))
        goto .242
        */



        generalRegFiles(10.U) := (generalRegFiles(11.U) + 1.U(64.W))

        CP := 242.U
      }

      is(242.U) {
        /*
        $9 = ($10: U64)
        goto .226
        */



        generalRegFiles(9.U) := generalRegFiles(10.U)

        CP := 226.U
      }

      is(243.U) {
        /*
        $11 = ($9: U64)
        goto .261
        */



        generalRegFiles(11.U) := generalRegFiles(9.U)

        CP := 261.U
      }

      is(244.U) {
        /*
        **(SP + (2: SP)) = ($11: U64) [unsigned, U64, 8]  // $res = ($11: U64)
        goto $ret@0
        */


        val __tmp_5312 = Cat(
          arrayRegFiles((SP + 2.U(16.W)) + 1.U),
          arrayRegFiles((SP + 2.U(16.W)) + 0.U)
        )
        val __tmp_5313 = (generalRegFiles(11.U)).asUInt
        arrayRegFiles(__tmp_5312 + 0.U) := __tmp_5313(7, 0)
        arrayRegFiles(__tmp_5312 + 1.U) := __tmp_5313(15, 8)
        arrayRegFiles(__tmp_5312 + 2.U) := __tmp_5313(23, 16)
        arrayRegFiles(__tmp_5312 + 3.U) := __tmp_5313(31, 24)
        arrayRegFiles(__tmp_5312 + 4.U) := __tmp_5313(39, 32)
        arrayRegFiles(__tmp_5312 + 5.U) := __tmp_5313(47, 40)
        arrayRegFiles(__tmp_5312 + 6.U) := __tmp_5313(55, 48)
        arrayRegFiles(__tmp_5312 + 7.U) := __tmp_5313(63, 56)

        CP := Cat(
          arrayRegFiles(SP + 0.U + 1.U),
          arrayRegFiles(SP + 0.U + 0.U)
        )

      }

      is(248.U) {
        /*
        SP = SP - 56
        goto .99
        */


        SP := SP - 56.U

        CP := 99.U
      }

      is(249.U) {
        /*
        DP = DP + 19
        goto .250
        */


        DP := DP + 19.U

        CP := 250.U
      }

      is(250.U) {
        /*
        *(((8: SP) + (12: SP)) + ((DP & (7: DP)) as SP)) = (10: U8) [unsigned, U8, 1]  // $display((DP & (7: DP))) = (10: U8)
        goto .251
        */


        val __tmp_5314 = ((8.U(16.W) + 12.U(16.W)) + (DP & 7.U(64.W)).asUInt)
        val __tmp_5315 = (10.U(8.W)).asUInt
        arrayRegFiles(__tmp_5314 + 0.U) := __tmp_5315(7, 0)

        CP := 251.U
      }

      is(251.U) {
        /*
        DP = DP + 1
        goto .1
        */


        DP := DP + 1.U

        CP := 1.U
      }

      is(253.U) {
        /*
        *SP = (255: CP) [unsigned, CP, 2]  // $ret@0 = 1385
        *(SP + (2: SP)) = (SP - (8: SP)) [unsigned, SP, 2]  // $res@2 = -8
        $29 = ($7: IS[Z, Z])
        $30 = ($5: Z)
        $31 = ($10: Z)
        goto .254
        */


        val __tmp_5316 = SP
        val __tmp_5317 = (255.U(16.W)).asUInt
        arrayRegFiles(__tmp_5316 + 0.U) := __tmp_5317(7, 0)
        arrayRegFiles(__tmp_5316 + 1.U) := __tmp_5317(15, 8)

        val __tmp_5318 = (SP + 2.U(16.W))
        val __tmp_5319 = ((SP - 8.U(16.W))).asUInt
        arrayRegFiles(__tmp_5318 + 0.U) := __tmp_5319(7, 0)
        arrayRegFiles(__tmp_5318 + 1.U) := __tmp_5319(15, 8)


        generalRegFiles(29.U) := generalRegFiles(7.U)


        generalRegFiles(30.U) := (generalRegFiles(5.U).asSInt).asUInt


        generalRegFiles(31.U) := (generalRegFiles(10.U).asSInt).asUInt

        CP := 254.U
      }

      is(254.U) {
        /*
        decl $ret: CP [@0, 2], $res: SP [@2, 10], a: IS[Z, Z] @$0, a: IS[Z, Z] [@12, 36], i: Z @$1, acc: Z @$2
        $0 = ($29: IS[Z, Z])
        $1 = ($30: Z)
        $2 = ($31: Z)
        goto .84
        */



        generalRegFiles(0.U) := generalRegFiles(29.U)


        generalRegFiles(1.U) := (generalRegFiles(30.U).asSInt).asUInt


        generalRegFiles(2.U) := (generalRegFiles(31.U).asSInt).asUInt

        CP := 84.U
      }

      is(255.U) {
        /*
        $11 = **(SP + (2: SP)) [signed, Z, 8]  // $11 = $res
        goto .256
        */


        val __tmp_5320 = (Cat(
          arrayRegFiles((SP + 2.U(16.W)) + 1.U),
          arrayRegFiles((SP + 2.U(16.W)) + 0.U)
        )).asUInt
        generalRegFiles(11.U) := Cat(
          arrayRegFiles(__tmp_5320 + 7.U),
          arrayRegFiles(__tmp_5320 + 6.U),
          arrayRegFiles(__tmp_5320 + 5.U),
          arrayRegFiles(__tmp_5320 + 4.U),
          arrayRegFiles(__tmp_5320 + 3.U),
          arrayRegFiles(__tmp_5320 + 2.U),
          arrayRegFiles(__tmp_5320 + 1.U),
          arrayRegFiles(__tmp_5320 + 0.U)
        ).asSInt.pad(GENERAL_REG_WIDTH).asUInt

        CP := 256.U
      }

      is(256.U) {
        /*
        *(SP + (12: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 0
        *(SP + (13: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 1
        *(SP + (14: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 2
        *(SP + (15: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 3
        *(SP + (16: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 4
        *(SP + (17: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 5
        *(SP + (18: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 6
        *(SP + (19: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 7
        *(SP + (20: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 8
        *(SP + (21: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 9
        *(SP + (22: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 10
        *(SP + (23: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 11
        *(SP + (24: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 12
        *(SP + (25: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 13
        *(SP + (26: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 14
        *(SP + (27: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 15
        *(SP + (28: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 16
        *(SP + (29: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 17
        *(SP + (30: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 18
        *(SP + (31: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 19
        *(SP + (32: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 20
        *(SP + (33: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 21
        *(SP + (34: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 22
        *(SP + (35: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 23
        *(SP + (36: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 24
        *(SP + (37: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 25
        *(SP + (38: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 26
        *(SP + (39: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 27
        *(SP + (40: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 28
        *(SP + (41: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 29
        *(SP + (42: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 30
        *(SP + (43: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 31
        *(SP + (44: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 32
        *(SP + (45: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 33
        *(SP + (46: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 34
        *(SP + (47: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing a byte 35
        *(SP + (2: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $res byte 0
        *(SP + (3: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $res byte 1
        *(SP + (4: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $res byte 2
        *(SP + (5: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $res byte 3
        *(SP + (6: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $res byte 4
        *(SP + (7: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $res byte 5
        *(SP + (8: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $res byte 6
        *(SP + (9: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $res byte 7
        *(SP + (10: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $res byte 8
        *(SP + (11: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $res byte 9
        *(SP + (0: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $ret byte 0
        *(SP + (1: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $ret byte 1
        undecl acc: Z @$2, i: Z @$1, a: IS[Z, Z] [@12, 36], a: IS[Z, Z] @$0, $res: SP [@2, 10], $ret: CP [@0, 2]
        goto .257
        */


        val __tmp_5321 = (SP + 12.U(16.W))
        val __tmp_5322 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5321 + 0.U) := __tmp_5322(7, 0)

        val __tmp_5323 = (SP + 13.U(16.W))
        val __tmp_5324 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5323 + 0.U) := __tmp_5324(7, 0)

        val __tmp_5325 = (SP + 14.U(16.W))
        val __tmp_5326 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5325 + 0.U) := __tmp_5326(7, 0)

        val __tmp_5327 = (SP + 15.U(16.W))
        val __tmp_5328 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5327 + 0.U) := __tmp_5328(7, 0)

        val __tmp_5329 = (SP + 16.U(16.W))
        val __tmp_5330 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5329 + 0.U) := __tmp_5330(7, 0)

        val __tmp_5331 = (SP + 17.U(16.W))
        val __tmp_5332 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5331 + 0.U) := __tmp_5332(7, 0)

        val __tmp_5333 = (SP + 18.U(16.W))
        val __tmp_5334 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5333 + 0.U) := __tmp_5334(7, 0)

        val __tmp_5335 = (SP + 19.U(16.W))
        val __tmp_5336 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5335 + 0.U) := __tmp_5336(7, 0)

        val __tmp_5337 = (SP + 20.U(16.W))
        val __tmp_5338 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5337 + 0.U) := __tmp_5338(7, 0)

        val __tmp_5339 = (SP + 21.U(16.W))
        val __tmp_5340 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5339 + 0.U) := __tmp_5340(7, 0)

        val __tmp_5341 = (SP + 22.U(16.W))
        val __tmp_5342 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5341 + 0.U) := __tmp_5342(7, 0)

        val __tmp_5343 = (SP + 23.U(16.W))
        val __tmp_5344 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5343 + 0.U) := __tmp_5344(7, 0)

        val __tmp_5345 = (SP + 24.U(16.W))
        val __tmp_5346 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5345 + 0.U) := __tmp_5346(7, 0)

        val __tmp_5347 = (SP + 25.U(16.W))
        val __tmp_5348 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5347 + 0.U) := __tmp_5348(7, 0)

        val __tmp_5349 = (SP + 26.U(16.W))
        val __tmp_5350 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5349 + 0.U) := __tmp_5350(7, 0)

        val __tmp_5351 = (SP + 27.U(16.W))
        val __tmp_5352 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5351 + 0.U) := __tmp_5352(7, 0)

        val __tmp_5353 = (SP + 28.U(16.W))
        val __tmp_5354 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5353 + 0.U) := __tmp_5354(7, 0)

        val __tmp_5355 = (SP + 29.U(16.W))
        val __tmp_5356 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5355 + 0.U) := __tmp_5356(7, 0)

        val __tmp_5357 = (SP + 30.U(16.W))
        val __tmp_5358 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5357 + 0.U) := __tmp_5358(7, 0)

        val __tmp_5359 = (SP + 31.U(16.W))
        val __tmp_5360 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5359 + 0.U) := __tmp_5360(7, 0)

        val __tmp_5361 = (SP + 32.U(16.W))
        val __tmp_5362 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5361 + 0.U) := __tmp_5362(7, 0)

        val __tmp_5363 = (SP + 33.U(16.W))
        val __tmp_5364 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5363 + 0.U) := __tmp_5364(7, 0)

        val __tmp_5365 = (SP + 34.U(16.W))
        val __tmp_5366 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5365 + 0.U) := __tmp_5366(7, 0)

        val __tmp_5367 = (SP + 35.U(16.W))
        val __tmp_5368 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5367 + 0.U) := __tmp_5368(7, 0)

        val __tmp_5369 = (SP + 36.U(16.W))
        val __tmp_5370 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5369 + 0.U) := __tmp_5370(7, 0)

        val __tmp_5371 = (SP + 37.U(16.W))
        val __tmp_5372 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5371 + 0.U) := __tmp_5372(7, 0)

        val __tmp_5373 = (SP + 38.U(16.W))
        val __tmp_5374 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5373 + 0.U) := __tmp_5374(7, 0)

        val __tmp_5375 = (SP + 39.U(16.W))
        val __tmp_5376 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5375 + 0.U) := __tmp_5376(7, 0)

        val __tmp_5377 = (SP + 40.U(16.W))
        val __tmp_5378 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5377 + 0.U) := __tmp_5378(7, 0)

        val __tmp_5379 = (SP + 41.U(16.W))
        val __tmp_5380 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5379 + 0.U) := __tmp_5380(7, 0)

        val __tmp_5381 = (SP + 42.U(16.W))
        val __tmp_5382 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5381 + 0.U) := __tmp_5382(7, 0)

        val __tmp_5383 = (SP + 43.U(16.W))
        val __tmp_5384 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5383 + 0.U) := __tmp_5384(7, 0)

        val __tmp_5385 = (SP + 44.U(16.W))
        val __tmp_5386 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5385 + 0.U) := __tmp_5386(7, 0)

        val __tmp_5387 = (SP + 45.U(16.W))
        val __tmp_5388 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5387 + 0.U) := __tmp_5388(7, 0)

        val __tmp_5389 = (SP + 46.U(16.W))
        val __tmp_5390 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5389 + 0.U) := __tmp_5390(7, 0)

        val __tmp_5391 = (SP + 47.U(16.W))
        val __tmp_5392 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5391 + 0.U) := __tmp_5392(7, 0)

        val __tmp_5393 = (SP + 2.U(16.W))
        val __tmp_5394 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5393 + 0.U) := __tmp_5394(7, 0)

        val __tmp_5395 = (SP + 3.U(16.W))
        val __tmp_5396 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5395 + 0.U) := __tmp_5396(7, 0)

        val __tmp_5397 = (SP + 4.U(16.W))
        val __tmp_5398 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5397 + 0.U) := __tmp_5398(7, 0)

        val __tmp_5399 = (SP + 5.U(16.W))
        val __tmp_5400 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5399 + 0.U) := __tmp_5400(7, 0)

        val __tmp_5401 = (SP + 6.U(16.W))
        val __tmp_5402 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5401 + 0.U) := __tmp_5402(7, 0)

        val __tmp_5403 = (SP + 7.U(16.W))
        val __tmp_5404 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5403 + 0.U) := __tmp_5404(7, 0)

        val __tmp_5405 = (SP + 8.U(16.W))
        val __tmp_5406 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5405 + 0.U) := __tmp_5406(7, 0)

        val __tmp_5407 = (SP + 9.U(16.W))
        val __tmp_5408 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5407 + 0.U) := __tmp_5408(7, 0)

        val __tmp_5409 = (SP + 10.U(16.W))
        val __tmp_5410 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5409 + 0.U) := __tmp_5410(7, 0)

        val __tmp_5411 = (SP + 11.U(16.W))
        val __tmp_5412 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5411 + 0.U) := __tmp_5412(7, 0)

        val __tmp_5413 = (SP + 0.U(16.W))
        val __tmp_5414 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5413 + 0.U) := __tmp_5414(7, 0)

        val __tmp_5415 = (SP + 1.U(16.W))
        val __tmp_5416 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5415 + 0.U) := __tmp_5416(7, 0)

        CP := 257.U
      }

      is(257.U) {
        /*
        SP = SP - 56
        goto .99
        */


        SP := SP - 56.U

        CP := 99.U
      }

      is(258.U) {
        /*
        *(SP + (38: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[168,16].5BB7E063 byte 0
        *(SP + (39: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[168,16].5BB7E063 byte 1
        *(SP + (40: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[168,16].5BB7E063 byte 2
        *(SP + (41: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[168,16].5BB7E063 byte 3
        *(SP + (42: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[168,16].5BB7E063 byte 4
        *(SP + (43: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[168,16].5BB7E063 byte 5
        *(SP + (44: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[168,16].5BB7E063 byte 6
        *(SP + (45: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[168,16].5BB7E063 byte 7
        *(SP + (46: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[168,16].5BB7E063 byte 8
        *(SP + (47: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[168,16].5BB7E063 byte 9
        *(SP + (48: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[168,16].5BB7E063 byte 10
        *(SP + (49: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[168,16].5BB7E063 byte 11
        *(SP + (50: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[168,16].5BB7E063 byte 12
        *(SP + (51: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[168,16].5BB7E063 byte 13
        *(SP + (52: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[168,16].5BB7E063 byte 14
        *(SP + (53: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[168,16].5BB7E063 byte 15
        *(SP + (54: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[168,16].5BB7E063 byte 16
        *(SP + (55: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[168,16].5BB7E063 byte 17
        *(SP + (56: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[168,16].5BB7E063 byte 18
        *(SP + (57: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[168,16].5BB7E063 byte 19
        *(SP + (58: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[168,16].5BB7E063 byte 20
        *(SP + (59: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[168,16].5BB7E063 byte 21
        *(SP + (60: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[168,16].5BB7E063 byte 22
        *(SP + (61: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[168,16].5BB7E063 byte 23
        *(SP + (62: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[168,16].5BB7E063 byte 24
        *(SP + (63: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[168,16].5BB7E063 byte 25
        *(SP + (64: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[168,16].5BB7E063 byte 26
        *(SP + (65: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[168,16].5BB7E063 byte 27
        *(SP + (66: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[168,16].5BB7E063 byte 28
        *(SP + (67: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[168,16].5BB7E063 byte 29
        *(SP + (68: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[168,16].5BB7E063 byte 30
        *(SP + (69: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[168,16].5BB7E063 byte 31
        *(SP + (70: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[168,16].5BB7E063 byte 32
        *(SP + (71: SP)) = (0: U8) [unsigned, U8, 1]  // // erasing $new@[168,16].5BB7E063 byte 33
        unalloc $new@[168,16].5BB7E063: MS[anvil.PrinterIndex.I20, U8] [@38, 34]
        goto .171
        */


        val __tmp_5417 = (SP + 38.U(16.W))
        val __tmp_5418 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5417 + 0.U) := __tmp_5418(7, 0)

        val __tmp_5419 = (SP + 39.U(16.W))
        val __tmp_5420 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5419 + 0.U) := __tmp_5420(7, 0)

        val __tmp_5421 = (SP + 40.U(16.W))
        val __tmp_5422 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5421 + 0.U) := __tmp_5422(7, 0)

        val __tmp_5423 = (SP + 41.U(16.W))
        val __tmp_5424 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5423 + 0.U) := __tmp_5424(7, 0)

        val __tmp_5425 = (SP + 42.U(16.W))
        val __tmp_5426 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5425 + 0.U) := __tmp_5426(7, 0)

        val __tmp_5427 = (SP + 43.U(16.W))
        val __tmp_5428 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5427 + 0.U) := __tmp_5428(7, 0)

        val __tmp_5429 = (SP + 44.U(16.W))
        val __tmp_5430 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5429 + 0.U) := __tmp_5430(7, 0)

        val __tmp_5431 = (SP + 45.U(16.W))
        val __tmp_5432 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5431 + 0.U) := __tmp_5432(7, 0)

        val __tmp_5433 = (SP + 46.U(16.W))
        val __tmp_5434 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5433 + 0.U) := __tmp_5434(7, 0)

        val __tmp_5435 = (SP + 47.U(16.W))
        val __tmp_5436 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5435 + 0.U) := __tmp_5436(7, 0)

        val __tmp_5437 = (SP + 48.U(16.W))
        val __tmp_5438 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5437 + 0.U) := __tmp_5438(7, 0)

        val __tmp_5439 = (SP + 49.U(16.W))
        val __tmp_5440 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5439 + 0.U) := __tmp_5440(7, 0)

        val __tmp_5441 = (SP + 50.U(16.W))
        val __tmp_5442 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5441 + 0.U) := __tmp_5442(7, 0)

        val __tmp_5443 = (SP + 51.U(16.W))
        val __tmp_5444 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5443 + 0.U) := __tmp_5444(7, 0)

        val __tmp_5445 = (SP + 52.U(16.W))
        val __tmp_5446 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5445 + 0.U) := __tmp_5446(7, 0)

        val __tmp_5447 = (SP + 53.U(16.W))
        val __tmp_5448 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5447 + 0.U) := __tmp_5448(7, 0)

        val __tmp_5449 = (SP + 54.U(16.W))
        val __tmp_5450 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5449 + 0.U) := __tmp_5450(7, 0)

        val __tmp_5451 = (SP + 55.U(16.W))
        val __tmp_5452 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5451 + 0.U) := __tmp_5452(7, 0)

        val __tmp_5453 = (SP + 56.U(16.W))
        val __tmp_5454 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5453 + 0.U) := __tmp_5454(7, 0)

        val __tmp_5455 = (SP + 57.U(16.W))
        val __tmp_5456 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5455 + 0.U) := __tmp_5456(7, 0)

        val __tmp_5457 = (SP + 58.U(16.W))
        val __tmp_5458 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5457 + 0.U) := __tmp_5458(7, 0)

        val __tmp_5459 = (SP + 59.U(16.W))
        val __tmp_5460 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5459 + 0.U) := __tmp_5460(7, 0)

        val __tmp_5461 = (SP + 60.U(16.W))
        val __tmp_5462 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5461 + 0.U) := __tmp_5462(7, 0)

        val __tmp_5463 = (SP + 61.U(16.W))
        val __tmp_5464 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5463 + 0.U) := __tmp_5464(7, 0)

        val __tmp_5465 = (SP + 62.U(16.W))
        val __tmp_5466 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5465 + 0.U) := __tmp_5466(7, 0)

        val __tmp_5467 = (SP + 63.U(16.W))
        val __tmp_5468 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5467 + 0.U) := __tmp_5468(7, 0)

        val __tmp_5469 = (SP + 64.U(16.W))
        val __tmp_5470 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5469 + 0.U) := __tmp_5470(7, 0)

        val __tmp_5471 = (SP + 65.U(16.W))
        val __tmp_5472 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5471 + 0.U) := __tmp_5472(7, 0)

        val __tmp_5473 = (SP + 66.U(16.W))
        val __tmp_5474 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5473 + 0.U) := __tmp_5474(7, 0)

        val __tmp_5475 = (SP + 67.U(16.W))
        val __tmp_5476 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5475 + 0.U) := __tmp_5476(7, 0)

        val __tmp_5477 = (SP + 68.U(16.W))
        val __tmp_5478 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5477 + 0.U) := __tmp_5478(7, 0)

        val __tmp_5479 = (SP + 69.U(16.W))
        val __tmp_5480 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5479 + 0.U) := __tmp_5480(7, 0)

        val __tmp_5481 = (SP + 70.U(16.W))
        val __tmp_5482 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5481 + 0.U) := __tmp_5482(7, 0)

        val __tmp_5483 = (SP + 71.U(16.W))
        val __tmp_5484 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_5483 + 0.U) := __tmp_5484(7, 0)

        CP := 171.U
      }

      is(259.U) {
        /*
        undecl neg: B @$5
        goto .214
        */


        CP := 214.U
      }

      is(260.U) {
        /*
        undecl i: anvil.PrinterIndex.I20 @$4
        goto .221
        */


        CP := 221.U
      }

      is(261.U) {
        /*
        undecl r: U64 @$9
        goto .244
        */


        CP := 244.U
      }

    }

}


