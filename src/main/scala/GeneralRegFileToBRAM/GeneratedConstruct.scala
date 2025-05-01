package GeneralRegFileToBRAM
import chisel3._
import chisel3.util._
import chisel3.experimental._



class MkISTest (val C_S_AXI_DATA_WIDTH:  Int = 32,
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
        SP = 36
        DP = 0
        *(10: U32) = (886747591: U32) [unsigned, U32, 4]  // $display.$type (MS[anvil.PrinterIndex.U, U8]: 0x34DAB1C7)
        *(14: SP) = (16: Z) [signed, Z, 8]  // $display.size
        *(36: CP) = (0: CP) [unsigned, CP, 2]  // $ret
        goto .4
        */


        SP := 36.U(16.W)

        DP := 0.U(64.W)

        val __tmp_1640 = 10.U(32.W)
        val __tmp_1641 = (886747591.U(32.W)).asUInt
        arrayRegFiles(__tmp_1640 + 0.U) := __tmp_1641(7, 0)
        arrayRegFiles(__tmp_1640 + 1.U) := __tmp_1641(15, 8)
        arrayRegFiles(__tmp_1640 + 2.U) := __tmp_1641(23, 16)
        arrayRegFiles(__tmp_1640 + 3.U) := __tmp_1641(31, 24)

        val __tmp_1642 = 14.U(16.W)
        val __tmp_1643 = (16.S(64.W)).asUInt
        arrayRegFiles(__tmp_1642 + 0.U) := __tmp_1643(7, 0)
        arrayRegFiles(__tmp_1642 + 1.U) := __tmp_1643(15, 8)
        arrayRegFiles(__tmp_1642 + 2.U) := __tmp_1643(23, 16)
        arrayRegFiles(__tmp_1642 + 3.U) := __tmp_1643(31, 24)
        arrayRegFiles(__tmp_1642 + 4.U) := __tmp_1643(39, 32)
        arrayRegFiles(__tmp_1642 + 5.U) := __tmp_1643(47, 40)
        arrayRegFiles(__tmp_1642 + 6.U) := __tmp_1643(55, 48)
        arrayRegFiles(__tmp_1642 + 7.U) := __tmp_1643(63, 56)

        val __tmp_1644 = 36.U(16.W)
        val __tmp_1645 = (0.U(16.W)).asUInt
        arrayRegFiles(__tmp_1644 + 0.U) := __tmp_1645(7, 0)
        arrayRegFiles(__tmp_1644 + 1.U) := __tmp_1645(15, 8)

        CP := 4.U
      }

      is(4.U) {
        /*
        $0 = *(0: SP) [signed, Z, 8]  // $0 = $testNum
        goto .5
        */


        val __tmp_1646 = (0.U(16.W)).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_1646 + 7.U),
          arrayRegFiles(__tmp_1646 + 6.U),
          arrayRegFiles(__tmp_1646 + 5.U),
          arrayRegFiles(__tmp_1646 + 4.U),
          arrayRegFiles(__tmp_1646 + 3.U),
          arrayRegFiles(__tmp_1646 + 2.U),
          arrayRegFiles(__tmp_1646 + 1.U),
          arrayRegFiles(__tmp_1646 + 0.U)
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
        *SP = (9: CP) [unsigned, CP, 2]  // $ret@0 = 1340
        goto .8
        */


        val __tmp_1647 = SP
        val __tmp_1648 = (9.U(16.W)).asUInt
        arrayRegFiles(__tmp_1647 + 0.U) := __tmp_1648(7, 0)
        arrayRegFiles(__tmp_1647 + 1.U) := __tmp_1648(15, 8)

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
        decl a: IS[Z, Z] [@2, 52]
        alloc mkIS$res@[9,11].CC5BFF47: IS[Z, Z] [@54, 52]
        goto .13
        */


        CP := 13.U
      }

      is(13.U) {
        /*
        SP = SP + 122
        goto .14
        */


        SP := SP + 122.U

        CP := 14.U
      }

      is(14.U) {
        /*
        *SP = (16: CP) [unsigned, CP, 2]  // $ret@0 = 1342
        *(SP + (2: SP)) = (SP - (68: SP)) [unsigned, SP, 2]  // $res@2 = -68
        $26 = (1: Z)
        $27 = (2: Z)
        $28 = (3: Z)
        goto .15
        */


        val __tmp_1649 = SP
        val __tmp_1650 = (16.U(16.W)).asUInt
        arrayRegFiles(__tmp_1649 + 0.U) := __tmp_1650(7, 0)
        arrayRegFiles(__tmp_1649 + 1.U) := __tmp_1650(15, 8)

        val __tmp_1651 = (SP + 2.U(16.W))
        val __tmp_1652 = ((SP - 68.U(16.W))).asUInt
        arrayRegFiles(__tmp_1651 + 0.U) := __tmp_1652(7, 0)
        arrayRegFiles(__tmp_1651 + 1.U) := __tmp_1652(15, 8)


        generalRegFiles(26.U) := (1.S(64.W)).asUInt


        generalRegFiles(27.U) := (2.S(64.W)).asUInt


        generalRegFiles(28.U) := (3.S(64.W)).asUInt

        CP := 15.U
      }

      is(15.U) {
        /*
        decl $ret: CP [@0, 2], $res: SP [@2, 54], x: Z @$0, y: Z @$1, z: Z @$2
        $0 = ($26: Z)
        $1 = ($27: Z)
        $2 = ($28: Z)
        goto .74
        */



        generalRegFiles(0.U) := (generalRegFiles(26.U).asSInt).asUInt


        generalRegFiles(1.U) := (generalRegFiles(27.U).asSInt).asUInt


        generalRegFiles(2.U) := (generalRegFiles(28.U).asSInt).asUInt

        CP := 74.U
      }

      is(16.U) {
        /*
        $3 = *(SP + (2: SP)) [unsigned, SP, 2]  // $3 = $res
        undecl z: Z @$2, y: Z @$1, x: Z @$0, $res: SP [@2, 54], $ret: CP [@0, 2]
        goto .17
        */


        val __tmp_1653 = ((SP + 2.U(16.W))).asUInt
        generalRegFiles(3.U) := Cat(
          arrayRegFiles(__tmp_1653 + 1.U),
          arrayRegFiles(__tmp_1653 + 0.U)
        ).asUInt

        CP := 17.U
      }

      is(17.U) {
        /*
        SP = SP - 122
        goto .18
        */


        SP := SP - 122.U

        CP := 18.U
      }

      is(18.U) {
        /*
        (SP + (2: SP)) [IS[Z, Z], 52]  <-  ($3: IS[Z, Z]) [IS[Z, Z], (((*(($3: IS[Z, Z]) + (4: SP)) as SP) * (8: SP)) + (12: SP))]  // a = ($3: IS[Z, Z])
        goto .19
        */


        val __tmp_1654 = (SP + 2.U(16.W))
        val __tmp_1655 = generalRegFiles(3.U)
        val __tmp_1656 = ((Cat(
            arrayRegFiles((generalRegFiles(3.U) + 4.U(16.W)) + 7.U),
            arrayRegFiles((generalRegFiles(3.U) + 4.U(16.W)) + 6.U),
            arrayRegFiles((generalRegFiles(3.U) + 4.U(16.W)) + 5.U),
            arrayRegFiles((generalRegFiles(3.U) + 4.U(16.W)) + 4.U),
            arrayRegFiles((generalRegFiles(3.U) + 4.U(16.W)) + 3.U),
            arrayRegFiles((generalRegFiles(3.U) + 4.U(16.W)) + 2.U),
            arrayRegFiles((generalRegFiles(3.U) + 4.U(16.W)) + 1.U),
            arrayRegFiles((generalRegFiles(3.U) + 4.U(16.W)) + 0.U)
          ).asSInt.asUInt * 8.U(16.W)) + 12.U(16.W))

        when(Idx < __tmp_1656) {
          arrayRegFiles(__tmp_1654 + Idx + 0.U) := arrayRegFiles(__tmp_1655 + Idx + 0.U)
          arrayRegFiles(__tmp_1654 + Idx + 1.U) := arrayRegFiles(__tmp_1655 + Idx + 1.U)
          arrayRegFiles(__tmp_1654 + Idx + 2.U) := arrayRegFiles(__tmp_1655 + Idx + 2.U)
          arrayRegFiles(__tmp_1654 + Idx + 3.U) := arrayRegFiles(__tmp_1655 + Idx + 3.U)
          arrayRegFiles(__tmp_1654 + Idx + 4.U) := arrayRegFiles(__tmp_1655 + Idx + 4.U)
          arrayRegFiles(__tmp_1654 + Idx + 5.U) := arrayRegFiles(__tmp_1655 + Idx + 5.U)
          arrayRegFiles(__tmp_1654 + Idx + 6.U) := arrayRegFiles(__tmp_1655 + Idx + 6.U)
          arrayRegFiles(__tmp_1654 + Idx + 7.U) := arrayRegFiles(__tmp_1655 + Idx + 7.U)
          Idx := Idx + 8.U
          LeftByteRounds := __tmp_1656 - Idx
        } .elsewhen(IdxLeftByteRounds < LeftByteRounds) {
          val __tmp_1657 = Idx - 8.U
          arrayRegFiles(__tmp_1654 + __tmp_1657 + IdxLeftByteRounds) := arrayRegFiles(__tmp_1655 + __tmp_1657 + IdxLeftByteRounds)
          IdxLeftByteRounds := IdxLeftByteRounds + 1.U
        } .otherwise {
          Idx := 0.U
          IdxLeftByteRounds := 0.U
          LeftByteRounds := 0.U
          CP := 19.U
        }


      }

      is(19.U) {
        /*
        $3 = (SP + (2: SP))
        goto .20
        */



        generalRegFiles(3.U) := (SP + 2.U(16.W))

        CP := 20.U
      }

      is(20.U) {
        /*
        if (((0: Z) <= (0: Z)) & ((0: Z) <= *(($3: IS[Z, Z]) + (4: SP)))) goto .26 else goto .21
        */


        CP := Mux((((0.S(64.W) <= 0.S(64.W)).asUInt & (0.S(64.W) <= Cat(
                            arrayRegFiles((generalRegFiles(3.U) + 4.U(16.W)) + 7.U),
                            arrayRegFiles((generalRegFiles(3.U) + 4.U(16.W)) + 6.U),
                            arrayRegFiles((generalRegFiles(3.U) + 4.U(16.W)) + 5.U),
                            arrayRegFiles((generalRegFiles(3.U) + 4.U(16.W)) + 4.U),
                            arrayRegFiles((generalRegFiles(3.U) + 4.U(16.W)) + 3.U),
                            arrayRegFiles((generalRegFiles(3.U) + 4.U(16.W)) + 2.U),
                            arrayRegFiles((generalRegFiles(3.U) + 4.U(16.W)) + 1.U),
                            arrayRegFiles((generalRegFiles(3.U) + 4.U(16.W)) + 0.U)
                          ).asSInt).asUInt).asUInt) === 1.U, 26.U, 21.U)
      }

      is(21.U) {
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
        goto .22
        */


        val __tmp_1658 = ((8.U(16.W) + 12.U(16.W)) + (DP & 15.U(64.W)).asUInt)
        val __tmp_1659 = (73.U(8.W)).asUInt
        arrayRegFiles(__tmp_1658 + 0.U) := __tmp_1659(7, 0)

        val __tmp_1660 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 1.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1661 = (110.U(8.W)).asUInt
        arrayRegFiles(__tmp_1660 + 0.U) := __tmp_1661(7, 0)

        val __tmp_1662 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 2.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1663 = (100.U(8.W)).asUInt
        arrayRegFiles(__tmp_1662 + 0.U) := __tmp_1663(7, 0)

        val __tmp_1664 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 3.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1665 = (101.U(8.W)).asUInt
        arrayRegFiles(__tmp_1664 + 0.U) := __tmp_1665(7, 0)

        val __tmp_1666 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 4.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1667 = (120.U(8.W)).asUInt
        arrayRegFiles(__tmp_1666 + 0.U) := __tmp_1667(7, 0)

        val __tmp_1668 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 5.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1669 = (32.U(8.W)).asUInt
        arrayRegFiles(__tmp_1668 + 0.U) := __tmp_1669(7, 0)

        val __tmp_1670 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 6.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1671 = (111.U(8.W)).asUInt
        arrayRegFiles(__tmp_1670 + 0.U) := __tmp_1671(7, 0)

        val __tmp_1672 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 7.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1673 = (117.U(8.W)).asUInt
        arrayRegFiles(__tmp_1672 + 0.U) := __tmp_1673(7, 0)

        val __tmp_1674 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 8.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1675 = (116.U(8.W)).asUInt
        arrayRegFiles(__tmp_1674 + 0.U) := __tmp_1675(7, 0)

        val __tmp_1676 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 9.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1677 = (32.U(8.W)).asUInt
        arrayRegFiles(__tmp_1676 + 0.U) := __tmp_1677(7, 0)

        val __tmp_1678 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 10.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1679 = (111.U(8.W)).asUInt
        arrayRegFiles(__tmp_1678 + 0.U) := __tmp_1679(7, 0)

        val __tmp_1680 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 11.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1681 = (102.U(8.W)).asUInt
        arrayRegFiles(__tmp_1680 + 0.U) := __tmp_1681(7, 0)

        val __tmp_1682 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 12.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1683 = (32.U(8.W)).asUInt
        arrayRegFiles(__tmp_1682 + 0.U) := __tmp_1683(7, 0)

        val __tmp_1684 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 13.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1685 = (98.U(8.W)).asUInt
        arrayRegFiles(__tmp_1684 + 0.U) := __tmp_1685(7, 0)

        val __tmp_1686 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 14.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1687 = (111.U(8.W)).asUInt
        arrayRegFiles(__tmp_1686 + 0.U) := __tmp_1687(7, 0)

        val __tmp_1688 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 15.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1689 = (117.U(8.W)).asUInt
        arrayRegFiles(__tmp_1688 + 0.U) := __tmp_1689(7, 0)

        val __tmp_1690 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 16.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1691 = (110.U(8.W)).asUInt
        arrayRegFiles(__tmp_1690 + 0.U) := __tmp_1691(7, 0)

        val __tmp_1692 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 17.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1693 = (100.U(8.W)).asUInt
        arrayRegFiles(__tmp_1692 + 0.U) := __tmp_1693(7, 0)

        val __tmp_1694 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 18.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1695 = (115.U(8.W)).asUInt
        arrayRegFiles(__tmp_1694 + 0.U) := __tmp_1695(7, 0)

        CP := 22.U
      }

      is(22.U) {
        /*
        DP = DP + 19
        goto .23
        */


        DP := DP + 19.U

        CP := 23.U
      }

      is(23.U) {
        /*
        *(((8: SP) + (12: SP)) + ((DP & (15: DP)) as SP)) = (10: U8) [unsigned, U8, 1]  // $display((DP & (15: DP))) = (10: U8)
        goto .24
        */


        val __tmp_1696 = ((8.U(16.W) + 12.U(16.W)) + (DP & 15.U(64.W)).asUInt)
        val __tmp_1697 = (10.U(8.W)).asUInt
        arrayRegFiles(__tmp_1696 + 0.U) := __tmp_1697(7, 0)

        CP := 24.U
      }

      is(24.U) {
        /*
        DP = DP + 1
        goto .1
        */


        DP := DP + 1.U

        CP := 1.U
      }

      is(26.U) {
        /*
        $4 = *((($3: IS[Z, Z]) + (12: SP)) + (((0: Z) as SP) * (8: SP))) [signed, Z, 8]  // $4 = ($3: IS[Z, Z])((0: Z))
        goto .27
        */


        val __tmp_1698 = (((generalRegFiles(3.U) + 12.U(16.W)) + (0.S(64.W).asUInt * 8.U(16.W)))).asUInt
        generalRegFiles(4.U) := Cat(
          arrayRegFiles(__tmp_1698 + 7.U),
          arrayRegFiles(__tmp_1698 + 6.U),
          arrayRegFiles(__tmp_1698 + 5.U),
          arrayRegFiles(__tmp_1698 + 4.U),
          arrayRegFiles(__tmp_1698 + 3.U),
          arrayRegFiles(__tmp_1698 + 2.U),
          arrayRegFiles(__tmp_1698 + 1.U),
          arrayRegFiles(__tmp_1698 + 0.U)
        ).asSInt.pad(GENERAL_REG_WIDTH).asUInt

        CP := 27.U
      }

      is(27.U) {
        /*
        $5 = (SP + (2: SP))
        goto .28
        */



        generalRegFiles(5.U) := (SP + 2.U(16.W))

        CP := 28.U
      }

      is(28.U) {
        /*
        if (((0: Z) <= (1: Z)) & ((1: Z) <= *(($5: IS[Z, Z]) + (4: SP)))) goto .34 else goto .29
        */


        CP := Mux((((0.S(64.W) <= 1.S(64.W)).asUInt & (1.S(64.W) <= Cat(
                            arrayRegFiles((generalRegFiles(5.U) + 4.U(16.W)) + 7.U),
                            arrayRegFiles((generalRegFiles(5.U) + 4.U(16.W)) + 6.U),
                            arrayRegFiles((generalRegFiles(5.U) + 4.U(16.W)) + 5.U),
                            arrayRegFiles((generalRegFiles(5.U) + 4.U(16.W)) + 4.U),
                            arrayRegFiles((generalRegFiles(5.U) + 4.U(16.W)) + 3.U),
                            arrayRegFiles((generalRegFiles(5.U) + 4.U(16.W)) + 2.U),
                            arrayRegFiles((generalRegFiles(5.U) + 4.U(16.W)) + 1.U),
                            arrayRegFiles((generalRegFiles(5.U) + 4.U(16.W)) + 0.U)
                          ).asSInt).asUInt).asUInt) === 1.U, 34.U, 29.U)
      }

      is(29.U) {
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
        goto .30
        */


        val __tmp_1699 = ((8.U(16.W) + 12.U(16.W)) + (DP & 15.U(64.W)).asUInt)
        val __tmp_1700 = (73.U(8.W)).asUInt
        arrayRegFiles(__tmp_1699 + 0.U) := __tmp_1700(7, 0)

        val __tmp_1701 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 1.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1702 = (110.U(8.W)).asUInt
        arrayRegFiles(__tmp_1701 + 0.U) := __tmp_1702(7, 0)

        val __tmp_1703 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 2.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1704 = (100.U(8.W)).asUInt
        arrayRegFiles(__tmp_1703 + 0.U) := __tmp_1704(7, 0)

        val __tmp_1705 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 3.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1706 = (101.U(8.W)).asUInt
        arrayRegFiles(__tmp_1705 + 0.U) := __tmp_1706(7, 0)

        val __tmp_1707 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 4.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1708 = (120.U(8.W)).asUInt
        arrayRegFiles(__tmp_1707 + 0.U) := __tmp_1708(7, 0)

        val __tmp_1709 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 5.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1710 = (32.U(8.W)).asUInt
        arrayRegFiles(__tmp_1709 + 0.U) := __tmp_1710(7, 0)

        val __tmp_1711 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 6.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1712 = (111.U(8.W)).asUInt
        arrayRegFiles(__tmp_1711 + 0.U) := __tmp_1712(7, 0)

        val __tmp_1713 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 7.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1714 = (117.U(8.W)).asUInt
        arrayRegFiles(__tmp_1713 + 0.U) := __tmp_1714(7, 0)

        val __tmp_1715 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 8.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1716 = (116.U(8.W)).asUInt
        arrayRegFiles(__tmp_1715 + 0.U) := __tmp_1716(7, 0)

        val __tmp_1717 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 9.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1718 = (32.U(8.W)).asUInt
        arrayRegFiles(__tmp_1717 + 0.U) := __tmp_1718(7, 0)

        val __tmp_1719 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 10.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1720 = (111.U(8.W)).asUInt
        arrayRegFiles(__tmp_1719 + 0.U) := __tmp_1720(7, 0)

        val __tmp_1721 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 11.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1722 = (102.U(8.W)).asUInt
        arrayRegFiles(__tmp_1721 + 0.U) := __tmp_1722(7, 0)

        val __tmp_1723 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 12.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1724 = (32.U(8.W)).asUInt
        arrayRegFiles(__tmp_1723 + 0.U) := __tmp_1724(7, 0)

        val __tmp_1725 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 13.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1726 = (98.U(8.W)).asUInt
        arrayRegFiles(__tmp_1725 + 0.U) := __tmp_1726(7, 0)

        val __tmp_1727 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 14.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1728 = (111.U(8.W)).asUInt
        arrayRegFiles(__tmp_1727 + 0.U) := __tmp_1728(7, 0)

        val __tmp_1729 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 15.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1730 = (117.U(8.W)).asUInt
        arrayRegFiles(__tmp_1729 + 0.U) := __tmp_1730(7, 0)

        val __tmp_1731 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 16.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1732 = (110.U(8.W)).asUInt
        arrayRegFiles(__tmp_1731 + 0.U) := __tmp_1732(7, 0)

        val __tmp_1733 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 17.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1734 = (100.U(8.W)).asUInt
        arrayRegFiles(__tmp_1733 + 0.U) := __tmp_1734(7, 0)

        val __tmp_1735 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 18.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1736 = (115.U(8.W)).asUInt
        arrayRegFiles(__tmp_1735 + 0.U) := __tmp_1736(7, 0)

        CP := 30.U
      }

      is(30.U) {
        /*
        DP = DP + 19
        goto .31
        */


        DP := DP + 19.U

        CP := 31.U
      }

      is(31.U) {
        /*
        *(((8: SP) + (12: SP)) + ((DP & (15: DP)) as SP)) = (10: U8) [unsigned, U8, 1]  // $display((DP & (15: DP))) = (10: U8)
        goto .32
        */


        val __tmp_1737 = ((8.U(16.W) + 12.U(16.W)) + (DP & 15.U(64.W)).asUInt)
        val __tmp_1738 = (10.U(8.W)).asUInt
        arrayRegFiles(__tmp_1737 + 0.U) := __tmp_1738(7, 0)

        CP := 32.U
      }

      is(32.U) {
        /*
        DP = DP + 1
        goto .1
        */


        DP := DP + 1.U

        CP := 1.U
      }

      is(34.U) {
        /*
        $6 = *((($5: IS[Z, Z]) + (12: SP)) + (((1: Z) as SP) * (8: SP))) [signed, Z, 8]  // $6 = ($5: IS[Z, Z])((1: Z))
        goto .35
        */


        val __tmp_1739 = (((generalRegFiles(5.U) + 12.U(16.W)) + (1.S(64.W).asUInt * 8.U(16.W)))).asUInt
        generalRegFiles(6.U) := Cat(
          arrayRegFiles(__tmp_1739 + 7.U),
          arrayRegFiles(__tmp_1739 + 6.U),
          arrayRegFiles(__tmp_1739 + 5.U),
          arrayRegFiles(__tmp_1739 + 4.U),
          arrayRegFiles(__tmp_1739 + 3.U),
          arrayRegFiles(__tmp_1739 + 2.U),
          arrayRegFiles(__tmp_1739 + 1.U),
          arrayRegFiles(__tmp_1739 + 0.U)
        ).asSInt.pad(GENERAL_REG_WIDTH).asUInt

        CP := 35.U
      }

      is(35.U) {
        /*
        $7 = (SP + (2: SP))
        goto .36
        */



        generalRegFiles(7.U) := (SP + 2.U(16.W))

        CP := 36.U
      }

      is(36.U) {
        /*
        if (((0: Z) <= (2: Z)) & ((2: Z) <= *(($7: IS[Z, Z]) + (4: SP)))) goto .42 else goto .37
        */


        CP := Mux((((0.S(64.W) <= 2.S(64.W)).asUInt & (2.S(64.W) <= Cat(
                            arrayRegFiles((generalRegFiles(7.U) + 4.U(16.W)) + 7.U),
                            arrayRegFiles((generalRegFiles(7.U) + 4.U(16.W)) + 6.U),
                            arrayRegFiles((generalRegFiles(7.U) + 4.U(16.W)) + 5.U),
                            arrayRegFiles((generalRegFiles(7.U) + 4.U(16.W)) + 4.U),
                            arrayRegFiles((generalRegFiles(7.U) + 4.U(16.W)) + 3.U),
                            arrayRegFiles((generalRegFiles(7.U) + 4.U(16.W)) + 2.U),
                            arrayRegFiles((generalRegFiles(7.U) + 4.U(16.W)) + 1.U),
                            arrayRegFiles((generalRegFiles(7.U) + 4.U(16.W)) + 0.U)
                          ).asSInt).asUInt).asUInt) === 1.U, 42.U, 37.U)
      }

      is(37.U) {
        /*
        undecl a: IS[Z, Z] [@2, 52]
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
        goto .38
        */


        val __tmp_1740 = ((8.U(16.W) + 12.U(16.W)) + (DP & 15.U(64.W)).asUInt)
        val __tmp_1741 = (73.U(8.W)).asUInt
        arrayRegFiles(__tmp_1740 + 0.U) := __tmp_1741(7, 0)

        val __tmp_1742 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 1.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1743 = (110.U(8.W)).asUInt
        arrayRegFiles(__tmp_1742 + 0.U) := __tmp_1743(7, 0)

        val __tmp_1744 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 2.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1745 = (100.U(8.W)).asUInt
        arrayRegFiles(__tmp_1744 + 0.U) := __tmp_1745(7, 0)

        val __tmp_1746 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 3.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1747 = (101.U(8.W)).asUInt
        arrayRegFiles(__tmp_1746 + 0.U) := __tmp_1747(7, 0)

        val __tmp_1748 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 4.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1749 = (120.U(8.W)).asUInt
        arrayRegFiles(__tmp_1748 + 0.U) := __tmp_1749(7, 0)

        val __tmp_1750 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 5.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1751 = (32.U(8.W)).asUInt
        arrayRegFiles(__tmp_1750 + 0.U) := __tmp_1751(7, 0)

        val __tmp_1752 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 6.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1753 = (111.U(8.W)).asUInt
        arrayRegFiles(__tmp_1752 + 0.U) := __tmp_1753(7, 0)

        val __tmp_1754 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 7.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1755 = (117.U(8.W)).asUInt
        arrayRegFiles(__tmp_1754 + 0.U) := __tmp_1755(7, 0)

        val __tmp_1756 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 8.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1757 = (116.U(8.W)).asUInt
        arrayRegFiles(__tmp_1756 + 0.U) := __tmp_1757(7, 0)

        val __tmp_1758 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 9.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1759 = (32.U(8.W)).asUInt
        arrayRegFiles(__tmp_1758 + 0.U) := __tmp_1759(7, 0)

        val __tmp_1760 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 10.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1761 = (111.U(8.W)).asUInt
        arrayRegFiles(__tmp_1760 + 0.U) := __tmp_1761(7, 0)

        val __tmp_1762 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 11.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1763 = (102.U(8.W)).asUInt
        arrayRegFiles(__tmp_1762 + 0.U) := __tmp_1763(7, 0)

        val __tmp_1764 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 12.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1765 = (32.U(8.W)).asUInt
        arrayRegFiles(__tmp_1764 + 0.U) := __tmp_1765(7, 0)

        val __tmp_1766 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 13.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1767 = (98.U(8.W)).asUInt
        arrayRegFiles(__tmp_1766 + 0.U) := __tmp_1767(7, 0)

        val __tmp_1768 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 14.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1769 = (111.U(8.W)).asUInt
        arrayRegFiles(__tmp_1768 + 0.U) := __tmp_1769(7, 0)

        val __tmp_1770 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 15.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1771 = (117.U(8.W)).asUInt
        arrayRegFiles(__tmp_1770 + 0.U) := __tmp_1771(7, 0)

        val __tmp_1772 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 16.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1773 = (110.U(8.W)).asUInt
        arrayRegFiles(__tmp_1772 + 0.U) := __tmp_1773(7, 0)

        val __tmp_1774 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 17.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1775 = (100.U(8.W)).asUInt
        arrayRegFiles(__tmp_1774 + 0.U) := __tmp_1775(7, 0)

        val __tmp_1776 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 18.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1777 = (115.U(8.W)).asUInt
        arrayRegFiles(__tmp_1776 + 0.U) := __tmp_1777(7, 0)

        CP := 38.U
      }

      is(38.U) {
        /*
        DP = DP + 19
        goto .39
        */


        DP := DP + 19.U

        CP := 39.U
      }

      is(39.U) {
        /*
        *(((8: SP) + (12: SP)) + ((DP & (15: DP)) as SP)) = (10: U8) [unsigned, U8, 1]  // $display((DP & (15: DP))) = (10: U8)
        goto .40
        */


        val __tmp_1778 = ((8.U(16.W) + 12.U(16.W)) + (DP & 15.U(64.W)).asUInt)
        val __tmp_1779 = (10.U(8.W)).asUInt
        arrayRegFiles(__tmp_1778 + 0.U) := __tmp_1779(7, 0)

        CP := 40.U
      }

      is(40.U) {
        /*
        DP = DP + 1
        goto .1
        */


        DP := DP + 1.U

        CP := 1.U
      }

      is(42.U) {
        /*
        undecl a: IS[Z, Z] [@2, 52]
        $8 = *((($7: IS[Z, Z]) + (12: SP)) + (((2: Z) as SP) * (8: SP))) [signed, Z, 8]  // $8 = ($7: IS[Z, Z])((2: Z))
        goto .43
        */


        val __tmp_1780 = (((generalRegFiles(7.U) + 12.U(16.W)) + (2.S(64.W).asUInt * 8.U(16.W)))).asUInt
        generalRegFiles(8.U) := Cat(
          arrayRegFiles(__tmp_1780 + 7.U),
          arrayRegFiles(__tmp_1780 + 6.U),
          arrayRegFiles(__tmp_1780 + 5.U),
          arrayRegFiles(__tmp_1780 + 4.U),
          arrayRegFiles(__tmp_1780 + 3.U),
          arrayRegFiles(__tmp_1780 + 2.U),
          arrayRegFiles(__tmp_1780 + 1.U),
          arrayRegFiles(__tmp_1780 + 0.U)
        ).asSInt.pad(GENERAL_REG_WIDTH).asUInt

        CP := 43.U
      }

      is(43.U) {
        /*
        *(((8: SP) + (12: SP)) + ((DP & (15: DP)) as SP)) = (91: U8) [unsigned, U8, 1]  // $display((DP & (15: DP))) = (91: U8)
        goto .44
        */


        val __tmp_1781 = ((8.U(16.W) + 12.U(16.W)) + (DP & 15.U(64.W)).asUInt)
        val __tmp_1782 = (91.U(8.W)).asUInt
        arrayRegFiles(__tmp_1781 + 0.U) := __tmp_1782(7, 0)

        CP := 44.U
      }

      is(44.U) {
        /*
        DP = DP + 1
        goto .45
        */


        DP := DP + 1.U

        CP := 45.U
      }

      is(45.U) {
        /*
        alloc printS64$res@[10,16].D63C46D5: U64 [@2, 8]
        goto .46
        */


        CP := 46.U
      }

      is(46.U) {
        /*
        SP = SP + 138
        goto .47
        */


        SP := SP + 138.U

        CP := 47.U
      }

      is(47.U) {
        /*
        *SP = (49: CP) [unsigned, CP, 2]  // $ret@0 = 1344
        *(SP + (2: SP)) = (SP - (136: SP)) [unsigned, SP, 2]  // $res@2 = -136
        $91 = (8: SP)
        $92 = DP
        $93 = (15: anvil.PrinterIndex.U)
        $94 = (($4: Z) as S64)
        *(SP - (8: SP)) = ($8: Z) [signed, Z, 8]  // save $8 (Z)
        *(SP - (16: SP)) = ($6: Z) [signed, Z, 8]  // save $6 (Z)
        goto .48
        */


        val __tmp_1783 = SP
        val __tmp_1784 = (49.U(16.W)).asUInt
        arrayRegFiles(__tmp_1783 + 0.U) := __tmp_1784(7, 0)
        arrayRegFiles(__tmp_1783 + 1.U) := __tmp_1784(15, 8)

        val __tmp_1785 = (SP + 2.U(16.W))
        val __tmp_1786 = ((SP - 136.U(16.W))).asUInt
        arrayRegFiles(__tmp_1785 + 0.U) := __tmp_1786(7, 0)
        arrayRegFiles(__tmp_1785 + 1.U) := __tmp_1786(15, 8)


        generalRegFiles(91.U) := 8.U(16.W)


        generalRegFiles(92.U) := DP


        generalRegFiles(93.U) := 15.U(64.W)


        generalRegFiles(94.U) := (generalRegFiles(4.U).asSInt.asSInt).asUInt

        val __tmp_1787 = (SP - 8.U(16.W))
        val __tmp_1788 = (generalRegFiles(8.U).asSInt).asUInt
        arrayRegFiles(__tmp_1787 + 0.U) := __tmp_1788(7, 0)
        arrayRegFiles(__tmp_1787 + 1.U) := __tmp_1788(15, 8)
        arrayRegFiles(__tmp_1787 + 2.U) := __tmp_1788(23, 16)
        arrayRegFiles(__tmp_1787 + 3.U) := __tmp_1788(31, 24)
        arrayRegFiles(__tmp_1787 + 4.U) := __tmp_1788(39, 32)
        arrayRegFiles(__tmp_1787 + 5.U) := __tmp_1788(47, 40)
        arrayRegFiles(__tmp_1787 + 6.U) := __tmp_1788(55, 48)
        arrayRegFiles(__tmp_1787 + 7.U) := __tmp_1788(63, 56)

        val __tmp_1789 = (SP - 16.U(16.W))
        val __tmp_1790 = (generalRegFiles(6.U).asSInt).asUInt
        arrayRegFiles(__tmp_1789 + 0.U) := __tmp_1790(7, 0)
        arrayRegFiles(__tmp_1789 + 1.U) := __tmp_1790(15, 8)
        arrayRegFiles(__tmp_1789 + 2.U) := __tmp_1790(23, 16)
        arrayRegFiles(__tmp_1789 + 3.U) := __tmp_1790(31, 24)
        arrayRegFiles(__tmp_1789 + 4.U) := __tmp_1790(39, 32)
        arrayRegFiles(__tmp_1789 + 5.U) := __tmp_1790(47, 40)
        arrayRegFiles(__tmp_1789 + 6.U) := __tmp_1790(55, 48)
        arrayRegFiles(__tmp_1789 + 7.U) := __tmp_1790(63, 56)

        CP := 48.U
      }

      is(48.U) {
        /*
        decl $ret: CP [@0, 2], $res: SP [@2, 2], buffer: MS[anvil.PrinterIndex.U, U8] @$0, index: anvil.PrinterIndex.U @$1, mask: anvil.PrinterIndex.U @$2, n: S64 @$3
        $0 = ($91: MS[anvil.PrinterIndex.U, U8])
        $1 = ($92: anvil.PrinterIndex.U)
        $2 = ($93: anvil.PrinterIndex.U)
        $3 = ($94: S64)
        goto .77
        */



        generalRegFiles(0.U) := generalRegFiles(91.U)


        generalRegFiles(1.U) := generalRegFiles(92.U)


        generalRegFiles(2.U) := generalRegFiles(93.U)


        generalRegFiles(3.U) := (generalRegFiles(94.U).asSInt).asUInt

        CP := 77.U
      }

      is(49.U) {
        /*
        $8 = *(SP - (8: SP)) [signed, Z, 8]  // restore $8 (Z)
        $6 = *(SP - (16: SP)) [signed, Z, 8]  // restore $6 (Z)
        $7 = **(SP + (2: SP)) [unsigned, U64, 8]  // $7 = $res
        undecl n: S64 @$3, mask: anvil.PrinterIndex.U @$2, index: anvil.PrinterIndex.U @$1, buffer: MS[anvil.PrinterIndex.U, U8] @$0, $res: SP [@2, 2], $ret: CP [@0, 2]
        goto .50
        */


        val __tmp_1791 = ((SP - 8.U(16.W))).asUInt
        generalRegFiles(8.U) := Cat(
          arrayRegFiles(__tmp_1791 + 7.U),
          arrayRegFiles(__tmp_1791 + 6.U),
          arrayRegFiles(__tmp_1791 + 5.U),
          arrayRegFiles(__tmp_1791 + 4.U),
          arrayRegFiles(__tmp_1791 + 3.U),
          arrayRegFiles(__tmp_1791 + 2.U),
          arrayRegFiles(__tmp_1791 + 1.U),
          arrayRegFiles(__tmp_1791 + 0.U)
        ).asSInt.pad(GENERAL_REG_WIDTH).asUInt

        val __tmp_1792 = ((SP - 16.U(16.W))).asUInt
        generalRegFiles(6.U) := Cat(
          arrayRegFiles(__tmp_1792 + 7.U),
          arrayRegFiles(__tmp_1792 + 6.U),
          arrayRegFiles(__tmp_1792 + 5.U),
          arrayRegFiles(__tmp_1792 + 4.U),
          arrayRegFiles(__tmp_1792 + 3.U),
          arrayRegFiles(__tmp_1792 + 2.U),
          arrayRegFiles(__tmp_1792 + 1.U),
          arrayRegFiles(__tmp_1792 + 0.U)
        ).asSInt.pad(GENERAL_REG_WIDTH).asUInt

        val __tmp_1793 = (Cat(
          arrayRegFiles((SP + 2.U(16.W)) + 1.U),
          arrayRegFiles((SP + 2.U(16.W)) + 0.U)
        )).asUInt
        generalRegFiles(7.U) := Cat(
          arrayRegFiles(__tmp_1793 + 7.U),
          arrayRegFiles(__tmp_1793 + 6.U),
          arrayRegFiles(__tmp_1793 + 5.U),
          arrayRegFiles(__tmp_1793 + 4.U),
          arrayRegFiles(__tmp_1793 + 3.U),
          arrayRegFiles(__tmp_1793 + 2.U),
          arrayRegFiles(__tmp_1793 + 1.U),
          arrayRegFiles(__tmp_1793 + 0.U)
        ).asUInt

        CP := 50.U
      }

      is(50.U) {
        /*
        SP = SP - 138
        goto .51
        */


        SP := SP - 138.U

        CP := 51.U
      }

      is(51.U) {
        /*
        DP = DP + (($7: U64) as DP)
        goto .52
        */


        DP := DP + generalRegFiles(7.U).asUInt

        CP := 52.U
      }

      is(52.U) {
        /*
        *(((8: SP) + (12: SP)) + ((DP & (15: DP)) as SP)) = (44: U8) [unsigned, U8, 1]  // $display((DP & (15: DP))) = (44: U8)
        *(((8: SP) + (12: SP)) + (((DP + (1: DP)) & (15: DP)) as SP)) = (32: U8) [unsigned, U8, 1]  // $display(((DP + (1: DP)) & (15: DP))) = (32: U8)
        goto .53
        */


        val __tmp_1794 = ((8.U(16.W) + 12.U(16.W)) + (DP & 15.U(64.W)).asUInt)
        val __tmp_1795 = (44.U(8.W)).asUInt
        arrayRegFiles(__tmp_1794 + 0.U) := __tmp_1795(7, 0)

        val __tmp_1796 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 1.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1797 = (32.U(8.W)).asUInt
        arrayRegFiles(__tmp_1796 + 0.U) := __tmp_1797(7, 0)

        CP := 53.U
      }

      is(53.U) {
        /*
        DP = DP + 2
        goto .54
        */


        DP := DP + 2.U

        CP := 54.U
      }

      is(54.U) {
        /*
        alloc printS64$res@[10,28].53C82437: U64 [@106, 8]
        goto .55
        */


        CP := 55.U
      }

      is(55.U) {
        /*
        SP = SP + 130
        goto .56
        */


        SP := SP + 130.U

        CP := 56.U
      }

      is(56.U) {
        /*
        *SP = (58: CP) [unsigned, CP, 2]  // $ret@0 = 1346
        *(SP + (2: SP)) = (SP - (24: SP)) [unsigned, SP, 2]  // $res@2 = -24
        $91 = (8: SP)
        $92 = DP
        $93 = (15: anvil.PrinterIndex.U)
        $94 = (($6: Z) as S64)
        *(SP - (8: SP)) = ($8: Z) [signed, Z, 8]  // save $8 (Z)
        goto .57
        */


        val __tmp_1798 = SP
        val __tmp_1799 = (58.U(16.W)).asUInt
        arrayRegFiles(__tmp_1798 + 0.U) := __tmp_1799(7, 0)
        arrayRegFiles(__tmp_1798 + 1.U) := __tmp_1799(15, 8)

        val __tmp_1800 = (SP + 2.U(16.W))
        val __tmp_1801 = ((SP - 24.U(16.W))).asUInt
        arrayRegFiles(__tmp_1800 + 0.U) := __tmp_1801(7, 0)
        arrayRegFiles(__tmp_1800 + 1.U) := __tmp_1801(15, 8)


        generalRegFiles(91.U) := 8.U(16.W)


        generalRegFiles(92.U) := DP


        generalRegFiles(93.U) := 15.U(64.W)


        generalRegFiles(94.U) := (generalRegFiles(6.U).asSInt.asSInt).asUInt

        val __tmp_1802 = (SP - 8.U(16.W))
        val __tmp_1803 = (generalRegFiles(8.U).asSInt).asUInt
        arrayRegFiles(__tmp_1802 + 0.U) := __tmp_1803(7, 0)
        arrayRegFiles(__tmp_1802 + 1.U) := __tmp_1803(15, 8)
        arrayRegFiles(__tmp_1802 + 2.U) := __tmp_1803(23, 16)
        arrayRegFiles(__tmp_1802 + 3.U) := __tmp_1803(31, 24)
        arrayRegFiles(__tmp_1802 + 4.U) := __tmp_1803(39, 32)
        arrayRegFiles(__tmp_1802 + 5.U) := __tmp_1803(47, 40)
        arrayRegFiles(__tmp_1802 + 6.U) := __tmp_1803(55, 48)
        arrayRegFiles(__tmp_1802 + 7.U) := __tmp_1803(63, 56)

        CP := 57.U
      }

      is(57.U) {
        /*
        decl $ret: CP [@0, 2], $res: SP [@2, 2], buffer: MS[anvil.PrinterIndex.U, U8] @$0, index: anvil.PrinterIndex.U @$1, mask: anvil.PrinterIndex.U @$2, n: S64 @$3
        $0 = ($91: MS[anvil.PrinterIndex.U, U8])
        $1 = ($92: anvil.PrinterIndex.U)
        $2 = ($93: anvil.PrinterIndex.U)
        $3 = ($94: S64)
        goto .77
        */



        generalRegFiles(0.U) := generalRegFiles(91.U)


        generalRegFiles(1.U) := generalRegFiles(92.U)


        generalRegFiles(2.U) := generalRegFiles(93.U)


        generalRegFiles(3.U) := (generalRegFiles(94.U).asSInt).asUInt

        CP := 77.U
      }

      is(58.U) {
        /*
        $8 = *(SP - (8: SP)) [signed, Z, 8]  // restore $8 (Z)
        $7 = **(SP + (2: SP)) [unsigned, U64, 8]  // $7 = $res
        undecl n: S64 @$3, mask: anvil.PrinterIndex.U @$2, index: anvil.PrinterIndex.U @$1, buffer: MS[anvil.PrinterIndex.U, U8] @$0, $res: SP [@2, 2], $ret: CP [@0, 2]
        goto .59
        */


        val __tmp_1804 = ((SP - 8.U(16.W))).asUInt
        generalRegFiles(8.U) := Cat(
          arrayRegFiles(__tmp_1804 + 7.U),
          arrayRegFiles(__tmp_1804 + 6.U),
          arrayRegFiles(__tmp_1804 + 5.U),
          arrayRegFiles(__tmp_1804 + 4.U),
          arrayRegFiles(__tmp_1804 + 3.U),
          arrayRegFiles(__tmp_1804 + 2.U),
          arrayRegFiles(__tmp_1804 + 1.U),
          arrayRegFiles(__tmp_1804 + 0.U)
        ).asSInt.pad(GENERAL_REG_WIDTH).asUInt

        val __tmp_1805 = (Cat(
          arrayRegFiles((SP + 2.U(16.W)) + 1.U),
          arrayRegFiles((SP + 2.U(16.W)) + 0.U)
        )).asUInt
        generalRegFiles(7.U) := Cat(
          arrayRegFiles(__tmp_1805 + 7.U),
          arrayRegFiles(__tmp_1805 + 6.U),
          arrayRegFiles(__tmp_1805 + 5.U),
          arrayRegFiles(__tmp_1805 + 4.U),
          arrayRegFiles(__tmp_1805 + 3.U),
          arrayRegFiles(__tmp_1805 + 2.U),
          arrayRegFiles(__tmp_1805 + 1.U),
          arrayRegFiles(__tmp_1805 + 0.U)
        ).asUInt

        CP := 59.U
      }

      is(59.U) {
        /*
        SP = SP - 130
        goto .60
        */


        SP := SP - 130.U

        CP := 60.U
      }

      is(60.U) {
        /*
        DP = DP + (($7: U64) as DP)
        goto .61
        */


        DP := DP + generalRegFiles(7.U).asUInt

        CP := 61.U
      }

      is(61.U) {
        /*
        *(((8: SP) + (12: SP)) + ((DP & (15: DP)) as SP)) = (44: U8) [unsigned, U8, 1]  // $display((DP & (15: DP))) = (44: U8)
        *(((8: SP) + (12: SP)) + (((DP + (1: DP)) & (15: DP)) as SP)) = (32: U8) [unsigned, U8, 1]  // $display(((DP + (1: DP)) & (15: DP))) = (32: U8)
        goto .62
        */


        val __tmp_1806 = ((8.U(16.W) + 12.U(16.W)) + (DP & 15.U(64.W)).asUInt)
        val __tmp_1807 = (44.U(8.W)).asUInt
        arrayRegFiles(__tmp_1806 + 0.U) := __tmp_1807(7, 0)

        val __tmp_1808 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 1.U(64.W)) & 15.U(64.W)).asUInt)
        val __tmp_1809 = (32.U(8.W)).asUInt
        arrayRegFiles(__tmp_1808 + 0.U) := __tmp_1809(7, 0)

        CP := 62.U
      }

      is(62.U) {
        /*
        DP = DP + 2
        goto .63
        */


        DP := DP + 2.U

        CP := 63.U
      }

      is(63.U) {
        /*
        alloc printS64$res@[10,40].717495C6: U64 [@114, 8]
        goto .64
        */


        CP := 64.U
      }

      is(64.U) {
        /*
        SP = SP + 122
        goto .65
        */


        SP := SP + 122.U

        CP := 65.U
      }

      is(65.U) {
        /*
        *SP = (67: CP) [unsigned, CP, 2]  // $ret@0 = 1348
        *(SP + (2: SP)) = (SP - (8: SP)) [unsigned, SP, 2]  // $res@2 = -8
        $91 = (8: SP)
        $92 = DP
        $93 = (15: anvil.PrinterIndex.U)
        $94 = (($8: Z) as S64)
        goto .66
        */


        val __tmp_1810 = SP
        val __tmp_1811 = (67.U(16.W)).asUInt
        arrayRegFiles(__tmp_1810 + 0.U) := __tmp_1811(7, 0)
        arrayRegFiles(__tmp_1810 + 1.U) := __tmp_1811(15, 8)

        val __tmp_1812 = (SP + 2.U(16.W))
        val __tmp_1813 = ((SP - 8.U(16.W))).asUInt
        arrayRegFiles(__tmp_1812 + 0.U) := __tmp_1813(7, 0)
        arrayRegFiles(__tmp_1812 + 1.U) := __tmp_1813(15, 8)


        generalRegFiles(91.U) := 8.U(16.W)


        generalRegFiles(92.U) := DP


        generalRegFiles(93.U) := 15.U(64.W)


        generalRegFiles(94.U) := (generalRegFiles(8.U).asSInt.asSInt).asUInt

        CP := 66.U
      }

      is(66.U) {
        /*
        decl $ret: CP [@0, 2], $res: SP [@2, 2], buffer: MS[anvil.PrinterIndex.U, U8] @$0, index: anvil.PrinterIndex.U @$1, mask: anvil.PrinterIndex.U @$2, n: S64 @$3
        $0 = ($91: MS[anvil.PrinterIndex.U, U8])
        $1 = ($92: anvil.PrinterIndex.U)
        $2 = ($93: anvil.PrinterIndex.U)
        $3 = ($94: S64)
        goto .77
        */



        generalRegFiles(0.U) := generalRegFiles(91.U)


        generalRegFiles(1.U) := generalRegFiles(92.U)


        generalRegFiles(2.U) := generalRegFiles(93.U)


        generalRegFiles(3.U) := (generalRegFiles(94.U).asSInt).asUInt

        CP := 77.U
      }

      is(67.U) {
        /*
        $7 = **(SP + (2: SP)) [unsigned, U64, 8]  // $7 = $res
        undecl n: S64 @$3, mask: anvil.PrinterIndex.U @$2, index: anvil.PrinterIndex.U @$1, buffer: MS[anvil.PrinterIndex.U, U8] @$0, $res: SP [@2, 2], $ret: CP [@0, 2]
        goto .68
        */


        val __tmp_1814 = (Cat(
          arrayRegFiles((SP + 2.U(16.W)) + 1.U),
          arrayRegFiles((SP + 2.U(16.W)) + 0.U)
        )).asUInt
        generalRegFiles(7.U) := Cat(
          arrayRegFiles(__tmp_1814 + 7.U),
          arrayRegFiles(__tmp_1814 + 6.U),
          arrayRegFiles(__tmp_1814 + 5.U),
          arrayRegFiles(__tmp_1814 + 4.U),
          arrayRegFiles(__tmp_1814 + 3.U),
          arrayRegFiles(__tmp_1814 + 2.U),
          arrayRegFiles(__tmp_1814 + 1.U),
          arrayRegFiles(__tmp_1814 + 0.U)
        ).asUInt

        CP := 68.U
      }

      is(68.U) {
        /*
        SP = SP - 122
        goto .69
        */


        SP := SP - 122.U

        CP := 69.U
      }

      is(69.U) {
        /*
        DP = DP + (($7: U64) as DP)
        goto .70
        */


        DP := DP + generalRegFiles(7.U).asUInt

        CP := 70.U
      }

      is(70.U) {
        /*
        *(((8: SP) + (12: SP)) + ((DP & (15: DP)) as SP)) = (93: U8) [unsigned, U8, 1]  // $display((DP & (15: DP))) = (93: U8)
        goto .71
        */


        val __tmp_1815 = ((8.U(16.W) + 12.U(16.W)) + (DP & 15.U(64.W)).asUInt)
        val __tmp_1816 = (93.U(8.W)).asUInt
        arrayRegFiles(__tmp_1815 + 0.U) := __tmp_1816(7, 0)

        CP := 71.U
      }

      is(71.U) {
        /*
        DP = DP + 1
        goto .72
        */


        DP := DP + 1.U

        CP := 72.U
      }

      is(72.U) {
        /*
        *(((8: SP) + (12: SP)) + ((DP & (15: DP)) as SP)) = (10: U8) [unsigned, U8, 1]  // $display((DP & (15: DP))) = (10: U8)
        goto .73
        */


        val __tmp_1817 = ((8.U(16.W) + 12.U(16.W)) + (DP & 15.U(64.W)).asUInt)
        val __tmp_1818 = (10.U(8.W)).asUInt
        arrayRegFiles(__tmp_1817 + 0.U) := __tmp_1818(7, 0)

        CP := 73.U
      }

      is(73.U) {
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

      is(74.U) {
        /*
        alloc $new@[5,10].C803F20F: IS[Z, Z] [@56, 52]
        $3 = (SP + (56: SP))
        *(SP + (56: SP)) = (2192300037: U32) [unsigned, U32, 4]  // sha3 type signature of IS[Z, Z]: 0x82ABD805
        *(SP + (60: SP)) = (3: Z) [signed, Z, 8]  // size of IS[Z, Z](($0: Z), ($1: Z), ($2: Z))
        goto .75
        */



        generalRegFiles(3.U) := (SP + 56.U(16.W))

        val __tmp_1819 = (SP + 56.U(16.W))
        val __tmp_1820 = (BigInt("2192300037").U(32.W)).asUInt
        arrayRegFiles(__tmp_1819 + 0.U) := __tmp_1820(7, 0)
        arrayRegFiles(__tmp_1819 + 1.U) := __tmp_1820(15, 8)
        arrayRegFiles(__tmp_1819 + 2.U) := __tmp_1820(23, 16)
        arrayRegFiles(__tmp_1819 + 3.U) := __tmp_1820(31, 24)

        val __tmp_1821 = (SP + 60.U(16.W))
        val __tmp_1822 = (3.S(64.W)).asUInt
        arrayRegFiles(__tmp_1821 + 0.U) := __tmp_1822(7, 0)
        arrayRegFiles(__tmp_1821 + 1.U) := __tmp_1822(15, 8)
        arrayRegFiles(__tmp_1821 + 2.U) := __tmp_1822(23, 16)
        arrayRegFiles(__tmp_1821 + 3.U) := __tmp_1822(31, 24)
        arrayRegFiles(__tmp_1821 + 4.U) := __tmp_1822(39, 32)
        arrayRegFiles(__tmp_1821 + 5.U) := __tmp_1822(47, 40)
        arrayRegFiles(__tmp_1821 + 6.U) := __tmp_1822(55, 48)
        arrayRegFiles(__tmp_1821 + 7.U) := __tmp_1822(63, 56)

        CP := 75.U
      }

      is(75.U) {
        /*
        *((($3: IS[Z, Z]) + (12: SP)) + (((0: Z) as SP) * (8: SP))) = ($0: Z) [signed, Z, 8]  // ($3: IS[Z, Z])((0: Z)) = ($0: Z)
        *((($3: IS[Z, Z]) + (12: SP)) + (((1: Z) as SP) * (8: SP))) = ($1: Z) [signed, Z, 8]  // ($3: IS[Z, Z])((1: Z)) = ($1: Z)
        *((($3: IS[Z, Z]) + (12: SP)) + (((2: Z) as SP) * (8: SP))) = ($2: Z) [signed, Z, 8]  // ($3: IS[Z, Z])((2: Z)) = ($2: Z)
        goto .76
        */


        val __tmp_1823 = ((generalRegFiles(3.U) + 12.U(16.W)) + (0.S(64.W).asUInt * 8.U(16.W)))
        val __tmp_1824 = (generalRegFiles(0.U).asSInt).asUInt
        arrayRegFiles(__tmp_1823 + 0.U) := __tmp_1824(7, 0)
        arrayRegFiles(__tmp_1823 + 1.U) := __tmp_1824(15, 8)
        arrayRegFiles(__tmp_1823 + 2.U) := __tmp_1824(23, 16)
        arrayRegFiles(__tmp_1823 + 3.U) := __tmp_1824(31, 24)
        arrayRegFiles(__tmp_1823 + 4.U) := __tmp_1824(39, 32)
        arrayRegFiles(__tmp_1823 + 5.U) := __tmp_1824(47, 40)
        arrayRegFiles(__tmp_1823 + 6.U) := __tmp_1824(55, 48)
        arrayRegFiles(__tmp_1823 + 7.U) := __tmp_1824(63, 56)

        val __tmp_1825 = ((generalRegFiles(3.U) + 12.U(16.W)) + (1.S(64.W).asUInt * 8.U(16.W)))
        val __tmp_1826 = (generalRegFiles(1.U).asSInt).asUInt
        arrayRegFiles(__tmp_1825 + 0.U) := __tmp_1826(7, 0)
        arrayRegFiles(__tmp_1825 + 1.U) := __tmp_1826(15, 8)
        arrayRegFiles(__tmp_1825 + 2.U) := __tmp_1826(23, 16)
        arrayRegFiles(__tmp_1825 + 3.U) := __tmp_1826(31, 24)
        arrayRegFiles(__tmp_1825 + 4.U) := __tmp_1826(39, 32)
        arrayRegFiles(__tmp_1825 + 5.U) := __tmp_1826(47, 40)
        arrayRegFiles(__tmp_1825 + 6.U) := __tmp_1826(55, 48)
        arrayRegFiles(__tmp_1825 + 7.U) := __tmp_1826(63, 56)

        val __tmp_1827 = ((generalRegFiles(3.U) + 12.U(16.W)) + (2.S(64.W).asUInt * 8.U(16.W)))
        val __tmp_1828 = (generalRegFiles(2.U).asSInt).asUInt
        arrayRegFiles(__tmp_1827 + 0.U) := __tmp_1828(7, 0)
        arrayRegFiles(__tmp_1827 + 1.U) := __tmp_1828(15, 8)
        arrayRegFiles(__tmp_1827 + 2.U) := __tmp_1828(23, 16)
        arrayRegFiles(__tmp_1827 + 3.U) := __tmp_1828(31, 24)
        arrayRegFiles(__tmp_1827 + 4.U) := __tmp_1828(39, 32)
        arrayRegFiles(__tmp_1827 + 5.U) := __tmp_1828(47, 40)
        arrayRegFiles(__tmp_1827 + 6.U) := __tmp_1828(55, 48)
        arrayRegFiles(__tmp_1827 + 7.U) := __tmp_1828(63, 56)

        CP := 76.U
      }

      is(76.U) {
        /*
        *(SP + (2: SP)) [IS[Z, Z], 52]  <-  ($3: IS[Z, Z]) [IS[Z, Z], (((*(($3: IS[Z, Z]) + (4: SP)) as SP) * (8: SP)) + (12: SP))]  // $res = ($3: IS[Z, Z])
        goto $ret@0
        */


        val __tmp_1829 = Cat(
          arrayRegFiles((SP + 2.U(16.W)) + 1.U),
          arrayRegFiles((SP + 2.U(16.W)) + 0.U)
        )
        val __tmp_1830 = generalRegFiles(3.U)
        val __tmp_1831 = ((Cat(
            arrayRegFiles((generalRegFiles(3.U) + 4.U(16.W)) + 7.U),
            arrayRegFiles((generalRegFiles(3.U) + 4.U(16.W)) + 6.U),
            arrayRegFiles((generalRegFiles(3.U) + 4.U(16.W)) + 5.U),
            arrayRegFiles((generalRegFiles(3.U) + 4.U(16.W)) + 4.U),
            arrayRegFiles((generalRegFiles(3.U) + 4.U(16.W)) + 3.U),
            arrayRegFiles((generalRegFiles(3.U) + 4.U(16.W)) + 2.U),
            arrayRegFiles((generalRegFiles(3.U) + 4.U(16.W)) + 1.U),
            arrayRegFiles((generalRegFiles(3.U) + 4.U(16.W)) + 0.U)
          ).asSInt.asUInt * 8.U(16.W)) + 12.U(16.W))

        when(Idx < __tmp_1831) {
          arrayRegFiles(__tmp_1829 + Idx + 0.U) := arrayRegFiles(__tmp_1830 + Idx + 0.U)
          arrayRegFiles(__tmp_1829 + Idx + 1.U) := arrayRegFiles(__tmp_1830 + Idx + 1.U)
          arrayRegFiles(__tmp_1829 + Idx + 2.U) := arrayRegFiles(__tmp_1830 + Idx + 2.U)
          arrayRegFiles(__tmp_1829 + Idx + 3.U) := arrayRegFiles(__tmp_1830 + Idx + 3.U)
          arrayRegFiles(__tmp_1829 + Idx + 4.U) := arrayRegFiles(__tmp_1830 + Idx + 4.U)
          arrayRegFiles(__tmp_1829 + Idx + 5.U) := arrayRegFiles(__tmp_1830 + Idx + 5.U)
          arrayRegFiles(__tmp_1829 + Idx + 6.U) := arrayRegFiles(__tmp_1830 + Idx + 6.U)
          arrayRegFiles(__tmp_1829 + Idx + 7.U) := arrayRegFiles(__tmp_1830 + Idx + 7.U)
          Idx := Idx + 8.U
          LeftByteRounds := __tmp_1831 - Idx
        } .elsewhen(IdxLeftByteRounds < LeftByteRounds) {
          val __tmp_1832 = Idx - 8.U
          arrayRegFiles(__tmp_1829 + __tmp_1832 + IdxLeftByteRounds) := arrayRegFiles(__tmp_1830 + __tmp_1832 + IdxLeftByteRounds)
          IdxLeftByteRounds := IdxLeftByteRounds + 1.U
        } .otherwise {
          Idx := 0.U
          IdxLeftByteRounds := 0.U
          LeftByteRounds := 0.U
          CP := Cat(
            arrayRegFiles(SP + 0.U + 1.U),
            arrayRegFiles(SP + 0.U + 0.U)
          )

        }


      }

      is(77.U) {
        /*
        $10 = (($3: S64) ≡ (-9223372036854775808: S64))
        goto .78
        */



        generalRegFiles(10.U) := (generalRegFiles(3.U).asSInt === BigInt("-9223372036854775808").S(64.W)).asUInt

        CP := 78.U
      }

      is(78.U) {
        /*
        if ($10: B) goto .79 else goto .139
        */


        CP := Mux((generalRegFiles(10.U).asUInt) === 1.U, 79.U, 139.U)
      }

      is(79.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $12 = (($1: anvil.PrinterIndex.U) & ($2: anvil.PrinterIndex.U))
        goto .80
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(12.U) := (generalRegFiles(1.U) & generalRegFiles(2.U))

        CP := 80.U
      }

      is(80.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($12: anvil.PrinterIndex.U) as SP)) = (45: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($12: anvil.PrinterIndex.U)) = (45: U8)
        goto .81
        */


        val __tmp_1833 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(12.U).asUInt)
        val __tmp_1834 = (45.U(8.W)).asUInt
        arrayRegFiles(__tmp_1833 + 0.U) := __tmp_1834(7, 0)

        CP := 81.U
      }

      is(81.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $13 = (($1: anvil.PrinterIndex.U) + (1: anvil.PrinterIndex.U))
        goto .82
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(13.U) := (generalRegFiles(1.U) + 1.U(64.W))

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
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($14: anvil.PrinterIndex.U) as SP)) = (57: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($14: anvil.PrinterIndex.U)) = (57: U8)
        goto .84
        */


        val __tmp_1835 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(14.U).asUInt)
        val __tmp_1836 = (57.U(8.W)).asUInt
        arrayRegFiles(__tmp_1835 + 0.U) := __tmp_1836(7, 0)

        CP := 84.U
      }

      is(84.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $13 = (($1: anvil.PrinterIndex.U) + (2: anvil.PrinterIndex.U))
        goto .85
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(13.U) := (generalRegFiles(1.U) + 2.U(64.W))

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
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($14: anvil.PrinterIndex.U) as SP)) = (50: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($14: anvil.PrinterIndex.U)) = (50: U8)
        goto .87
        */


        val __tmp_1837 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(14.U).asUInt)
        val __tmp_1838 = (50.U(8.W)).asUInt
        arrayRegFiles(__tmp_1837 + 0.U) := __tmp_1838(7, 0)

        CP := 87.U
      }

      is(87.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $13 = (($1: anvil.PrinterIndex.U) + (3: anvil.PrinterIndex.U))
        goto .88
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(13.U) := (generalRegFiles(1.U) + 3.U(64.W))

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
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($14: anvil.PrinterIndex.U) as SP)) = (50: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($14: anvil.PrinterIndex.U)) = (50: U8)
        goto .90
        */


        val __tmp_1839 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(14.U).asUInt)
        val __tmp_1840 = (50.U(8.W)).asUInt
        arrayRegFiles(__tmp_1839 + 0.U) := __tmp_1840(7, 0)

        CP := 90.U
      }

      is(90.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $13 = (($1: anvil.PrinterIndex.U) + (4: anvil.PrinterIndex.U))
        goto .91
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(13.U) := (generalRegFiles(1.U) + 4.U(64.W))

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
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($14: anvil.PrinterIndex.U) as SP)) = (51: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($14: anvil.PrinterIndex.U)) = (51: U8)
        goto .93
        */


        val __tmp_1841 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(14.U).asUInt)
        val __tmp_1842 = (51.U(8.W)).asUInt
        arrayRegFiles(__tmp_1841 + 0.U) := __tmp_1842(7, 0)

        CP := 93.U
      }

      is(93.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $13 = (($1: anvil.PrinterIndex.U) + (5: anvil.PrinterIndex.U))
        goto .94
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(13.U) := (generalRegFiles(1.U) + 5.U(64.W))

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
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($14: anvil.PrinterIndex.U) as SP)) = (51: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($14: anvil.PrinterIndex.U)) = (51: U8)
        goto .96
        */


        val __tmp_1843 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(14.U).asUInt)
        val __tmp_1844 = (51.U(8.W)).asUInt
        arrayRegFiles(__tmp_1843 + 0.U) := __tmp_1844(7, 0)

        CP := 96.U
      }

      is(96.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $13 = (($1: anvil.PrinterIndex.U) + (6: anvil.PrinterIndex.U))
        goto .97
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(13.U) := (generalRegFiles(1.U) + 6.U(64.W))

        CP := 97.U
      }

      is(97.U) {
        /*
        $14 = (($13: anvil.PrinterIndex.U) & ($2: anvil.PrinterIndex.U))
        goto .98
        */



        generalRegFiles(14.U) := (generalRegFiles(13.U) & generalRegFiles(2.U))

        CP := 98.U
      }

      is(98.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($14: anvil.PrinterIndex.U) as SP)) = (55: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($14: anvil.PrinterIndex.U)) = (55: U8)
        goto .99
        */


        val __tmp_1845 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(14.U).asUInt)
        val __tmp_1846 = (55.U(8.W)).asUInt
        arrayRegFiles(__tmp_1845 + 0.U) := __tmp_1846(7, 0)

        CP := 99.U
      }

      is(99.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $13 = (($1: anvil.PrinterIndex.U) + (7: anvil.PrinterIndex.U))
        goto .100
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(13.U) := (generalRegFiles(1.U) + 7.U(64.W))

        CP := 100.U
      }

      is(100.U) {
        /*
        $14 = (($13: anvil.PrinterIndex.U) & ($2: anvil.PrinterIndex.U))
        goto .101
        */



        generalRegFiles(14.U) := (generalRegFiles(13.U) & generalRegFiles(2.U))

        CP := 101.U
      }

      is(101.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($14: anvil.PrinterIndex.U) as SP)) = (50: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($14: anvil.PrinterIndex.U)) = (50: U8)
        goto .102
        */


        val __tmp_1847 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(14.U).asUInt)
        val __tmp_1848 = (50.U(8.W)).asUInt
        arrayRegFiles(__tmp_1847 + 0.U) := __tmp_1848(7, 0)

        CP := 102.U
      }

      is(102.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $13 = (($1: anvil.PrinterIndex.U) + (8: anvil.PrinterIndex.U))
        goto .103
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(13.U) := (generalRegFiles(1.U) + 8.U(64.W))

        CP := 103.U
      }

      is(103.U) {
        /*
        $14 = (($13: anvil.PrinterIndex.U) & ($2: anvil.PrinterIndex.U))
        goto .104
        */



        generalRegFiles(14.U) := (generalRegFiles(13.U) & generalRegFiles(2.U))

        CP := 104.U
      }

      is(104.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($14: anvil.PrinterIndex.U) as SP)) = (48: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($14: anvil.PrinterIndex.U)) = (48: U8)
        goto .105
        */


        val __tmp_1849 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(14.U).asUInt)
        val __tmp_1850 = (48.U(8.W)).asUInt
        arrayRegFiles(__tmp_1849 + 0.U) := __tmp_1850(7, 0)

        CP := 105.U
      }

      is(105.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $13 = (($1: anvil.PrinterIndex.U) + (9: anvil.PrinterIndex.U))
        goto .106
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(13.U) := (generalRegFiles(1.U) + 9.U(64.W))

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
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($14: anvil.PrinterIndex.U) as SP)) = (51: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($14: anvil.PrinterIndex.U)) = (51: U8)
        goto .108
        */


        val __tmp_1851 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(14.U).asUInt)
        val __tmp_1852 = (51.U(8.W)).asUInt
        arrayRegFiles(__tmp_1851 + 0.U) := __tmp_1852(7, 0)

        CP := 108.U
      }

      is(108.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $13 = (($1: anvil.PrinterIndex.U) + (10: anvil.PrinterIndex.U))
        goto .109
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(13.U) := (generalRegFiles(1.U) + 10.U(64.W))

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
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($14: anvil.PrinterIndex.U) as SP)) = (54: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($14: anvil.PrinterIndex.U)) = (54: U8)
        goto .111
        */


        val __tmp_1853 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(14.U).asUInt)
        val __tmp_1854 = (54.U(8.W)).asUInt
        arrayRegFiles(__tmp_1853 + 0.U) := __tmp_1854(7, 0)

        CP := 111.U
      }

      is(111.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $13 = (($1: anvil.PrinterIndex.U) + (11: anvil.PrinterIndex.U))
        goto .112
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(13.U) := (generalRegFiles(1.U) + 11.U(64.W))

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
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($14: anvil.PrinterIndex.U) as SP)) = (56: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($14: anvil.PrinterIndex.U)) = (56: U8)
        goto .114
        */


        val __tmp_1855 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(14.U).asUInt)
        val __tmp_1856 = (56.U(8.W)).asUInt
        arrayRegFiles(__tmp_1855 + 0.U) := __tmp_1856(7, 0)

        CP := 114.U
      }

      is(114.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $13 = (($1: anvil.PrinterIndex.U) + (12: anvil.PrinterIndex.U))
        goto .115
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(13.U) := (generalRegFiles(1.U) + 12.U(64.W))

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
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($14: anvil.PrinterIndex.U) as SP)) = (53: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($14: anvil.PrinterIndex.U)) = (53: U8)
        goto .117
        */


        val __tmp_1857 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(14.U).asUInt)
        val __tmp_1858 = (53.U(8.W)).asUInt
        arrayRegFiles(__tmp_1857 + 0.U) := __tmp_1858(7, 0)

        CP := 117.U
      }

      is(117.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $13 = (($1: anvil.PrinterIndex.U) + (13: anvil.PrinterIndex.U))
        goto .118
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(13.U) := (generalRegFiles(1.U) + 13.U(64.W))

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
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($14: anvil.PrinterIndex.U) as SP)) = (52: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($14: anvil.PrinterIndex.U)) = (52: U8)
        goto .120
        */


        val __tmp_1859 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(14.U).asUInt)
        val __tmp_1860 = (52.U(8.W)).asUInt
        arrayRegFiles(__tmp_1859 + 0.U) := __tmp_1860(7, 0)

        CP := 120.U
      }

      is(120.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $13 = (($1: anvil.PrinterIndex.U) + (14: anvil.PrinterIndex.U))
        goto .121
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(13.U) := (generalRegFiles(1.U) + 14.U(64.W))

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


        val __tmp_1861 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(14.U).asUInt)
        val __tmp_1862 = (55.U(8.W)).asUInt
        arrayRegFiles(__tmp_1861 + 0.U) := __tmp_1862(7, 0)

        CP := 123.U
      }

      is(123.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $13 = (($1: anvil.PrinterIndex.U) + (15: anvil.PrinterIndex.U))
        goto .124
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(13.U) := (generalRegFiles(1.U) + 15.U(64.W))

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
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($14: anvil.PrinterIndex.U) as SP)) = (55: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($14: anvil.PrinterIndex.U)) = (55: U8)
        goto .126
        */


        val __tmp_1863 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(14.U).asUInt)
        val __tmp_1864 = (55.U(8.W)).asUInt
        arrayRegFiles(__tmp_1863 + 0.U) := __tmp_1864(7, 0)

        CP := 126.U
      }

      is(126.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $13 = (($1: anvil.PrinterIndex.U) + (16: anvil.PrinterIndex.U))
        goto .127
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(13.U) := (generalRegFiles(1.U) + 16.U(64.W))

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
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($14: anvil.PrinterIndex.U) as SP)) = (53: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($14: anvil.PrinterIndex.U)) = (53: U8)
        goto .129
        */


        val __tmp_1865 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(14.U).asUInt)
        val __tmp_1866 = (53.U(8.W)).asUInt
        arrayRegFiles(__tmp_1865 + 0.U) := __tmp_1866(7, 0)

        CP := 129.U
      }

      is(129.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $13 = (($1: anvil.PrinterIndex.U) + (17: anvil.PrinterIndex.U))
        goto .130
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(13.U) := (generalRegFiles(1.U) + 17.U(64.W))

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
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($14: anvil.PrinterIndex.U) as SP)) = (56: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($14: anvil.PrinterIndex.U)) = (56: U8)
        goto .132
        */


        val __tmp_1867 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(14.U).asUInt)
        val __tmp_1868 = (56.U(8.W)).asUInt
        arrayRegFiles(__tmp_1867 + 0.U) := __tmp_1868(7, 0)

        CP := 132.U
      }

      is(132.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $13 = (($1: anvil.PrinterIndex.U) + (18: anvil.PrinterIndex.U))
        goto .133
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(13.U) := (generalRegFiles(1.U) + 18.U(64.W))

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
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($14: anvil.PrinterIndex.U) as SP)) = (48: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($14: anvil.PrinterIndex.U)) = (48: U8)
        goto .135
        */


        val __tmp_1869 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(14.U).asUInt)
        val __tmp_1870 = (48.U(8.W)).asUInt
        arrayRegFiles(__tmp_1869 + 0.U) := __tmp_1870(7, 0)

        CP := 135.U
      }

      is(135.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $13 = (($1: anvil.PrinterIndex.U) + (19: anvil.PrinterIndex.U))
        goto .136
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(13.U) := (generalRegFiles(1.U) + 19.U(64.W))

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


        val __tmp_1871 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(14.U).asUInt)
        val __tmp_1872 = (56.U(8.W)).asUInt
        arrayRegFiles(__tmp_1871 + 0.U) := __tmp_1872(7, 0)

        CP := 138.U
      }

      is(138.U) {
        /*
        **(SP + (2: SP)) = (20: U64) [unsigned, U64, 8]  // $res = (20: U64)
        goto $ret@0
        */


        val __tmp_1873 = Cat(
          arrayRegFiles((SP + 2.U(16.W)) + 1.U),
          arrayRegFiles((SP + 2.U(16.W)) + 0.U)
        )
        val __tmp_1874 = (20.U(64.W)).asUInt
        arrayRegFiles(__tmp_1873 + 0.U) := __tmp_1874(7, 0)
        arrayRegFiles(__tmp_1873 + 1.U) := __tmp_1874(15, 8)
        arrayRegFiles(__tmp_1873 + 2.U) := __tmp_1874(23, 16)
        arrayRegFiles(__tmp_1873 + 3.U) := __tmp_1874(31, 24)
        arrayRegFiles(__tmp_1873 + 4.U) := __tmp_1874(39, 32)
        arrayRegFiles(__tmp_1873 + 5.U) := __tmp_1874(47, 40)
        arrayRegFiles(__tmp_1873 + 6.U) := __tmp_1874(55, 48)
        arrayRegFiles(__tmp_1873 + 7.U) := __tmp_1874(63, 56)

        CP := Cat(
          arrayRegFiles(SP + 0.U + 1.U),
          arrayRegFiles(SP + 0.U + 0.U)
        )

      }

      is(139.U) {
        /*
        $10 = (($3: S64) ≡ (0: S64))
        goto .140
        */



        generalRegFiles(10.U) := (generalRegFiles(3.U).asSInt === 0.S(64.W)).asUInt

        CP := 140.U
      }

      is(140.U) {
        /*
        if ($10: B) goto .141 else goto .144
        */


        CP := Mux((generalRegFiles(10.U).asUInt) === 1.U, 141.U, 144.U)
      }

      is(141.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $12 = (($1: anvil.PrinterIndex.U) & ($2: anvil.PrinterIndex.U))
        goto .142
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(12.U) := (generalRegFiles(1.U) & generalRegFiles(2.U))

        CP := 142.U
      }

      is(142.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($12: anvil.PrinterIndex.U) as SP)) = (48: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($12: anvil.PrinterIndex.U)) = (48: U8)
        goto .143
        */


        val __tmp_1875 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(12.U).asUInt)
        val __tmp_1876 = (48.U(8.W)).asUInt
        arrayRegFiles(__tmp_1875 + 0.U) := __tmp_1876(7, 0)

        CP := 143.U
      }

      is(143.U) {
        /*
        **(SP + (2: SP)) = (1: U64) [unsigned, U64, 8]  // $res = (1: U64)
        goto $ret@0
        */


        val __tmp_1877 = Cat(
          arrayRegFiles((SP + 2.U(16.W)) + 1.U),
          arrayRegFiles((SP + 2.U(16.W)) + 0.U)
        )
        val __tmp_1878 = (1.U(64.W)).asUInt
        arrayRegFiles(__tmp_1877 + 0.U) := __tmp_1878(7, 0)
        arrayRegFiles(__tmp_1877 + 1.U) := __tmp_1878(15, 8)
        arrayRegFiles(__tmp_1877 + 2.U) := __tmp_1878(23, 16)
        arrayRegFiles(__tmp_1877 + 3.U) := __tmp_1878(31, 24)
        arrayRegFiles(__tmp_1877 + 4.U) := __tmp_1878(39, 32)
        arrayRegFiles(__tmp_1877 + 5.U) := __tmp_1878(47, 40)
        arrayRegFiles(__tmp_1877 + 6.U) := __tmp_1878(55, 48)
        arrayRegFiles(__tmp_1877 + 7.U) := __tmp_1878(63, 56)

        CP := Cat(
          arrayRegFiles(SP + 0.U + 1.U),
          arrayRegFiles(SP + 0.U + 0.U)
        )

      }

      is(144.U) {
        /*
        decl buff: MS[anvil.PrinterIndex.I20, U8] [@4, 34]
        alloc $new@[168,16].5BB7E063: MS[anvil.PrinterIndex.I20, U8] [@38, 34]
        $11 = (SP + (38: SP))
        *(SP + (38: SP)) = (323602724: U32) [unsigned, U32, 4]  // sha3 type signature of MS[anvil.PrinterIndex.I20, U8]: 0x1349C924
        *(SP + (42: SP)) = (20: Z) [signed, Z, 8]  // size of MS[anvil.PrinterIndex.I20, U8]((0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8))
        goto .145
        */



        generalRegFiles(11.U) := (SP + 38.U(16.W))

        val __tmp_1879 = (SP + 38.U(16.W))
        val __tmp_1880 = (323602724.U(32.W)).asUInt
        arrayRegFiles(__tmp_1879 + 0.U) := __tmp_1880(7, 0)
        arrayRegFiles(__tmp_1879 + 1.U) := __tmp_1880(15, 8)
        arrayRegFiles(__tmp_1879 + 2.U) := __tmp_1880(23, 16)
        arrayRegFiles(__tmp_1879 + 3.U) := __tmp_1880(31, 24)

        val __tmp_1881 = (SP + 42.U(16.W))
        val __tmp_1882 = (20.S(64.W)).asUInt
        arrayRegFiles(__tmp_1881 + 0.U) := __tmp_1882(7, 0)
        arrayRegFiles(__tmp_1881 + 1.U) := __tmp_1882(15, 8)
        arrayRegFiles(__tmp_1881 + 2.U) := __tmp_1882(23, 16)
        arrayRegFiles(__tmp_1881 + 3.U) := __tmp_1882(31, 24)
        arrayRegFiles(__tmp_1881 + 4.U) := __tmp_1882(39, 32)
        arrayRegFiles(__tmp_1881 + 5.U) := __tmp_1882(47, 40)
        arrayRegFiles(__tmp_1881 + 6.U) := __tmp_1882(55, 48)
        arrayRegFiles(__tmp_1881 + 7.U) := __tmp_1882(63, 56)

        CP := 145.U
      }

      is(145.U) {
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
        goto .146
        */


        val __tmp_1883 = ((generalRegFiles(11.U) + 12.U(16.W)) + 0.S(8.W).asUInt)
        val __tmp_1884 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_1883 + 0.U) := __tmp_1884(7, 0)

        val __tmp_1885 = ((generalRegFiles(11.U) + 12.U(16.W)) + 1.S(8.W).asUInt)
        val __tmp_1886 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_1885 + 0.U) := __tmp_1886(7, 0)

        val __tmp_1887 = ((generalRegFiles(11.U) + 12.U(16.W)) + 2.S(8.W).asUInt)
        val __tmp_1888 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_1887 + 0.U) := __tmp_1888(7, 0)

        val __tmp_1889 = ((generalRegFiles(11.U) + 12.U(16.W)) + 3.S(8.W).asUInt)
        val __tmp_1890 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_1889 + 0.U) := __tmp_1890(7, 0)

        val __tmp_1891 = ((generalRegFiles(11.U) + 12.U(16.W)) + 4.S(8.W).asUInt)
        val __tmp_1892 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_1891 + 0.U) := __tmp_1892(7, 0)

        val __tmp_1893 = ((generalRegFiles(11.U) + 12.U(16.W)) + 5.S(8.W).asUInt)
        val __tmp_1894 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_1893 + 0.U) := __tmp_1894(7, 0)

        val __tmp_1895 = ((generalRegFiles(11.U) + 12.U(16.W)) + 6.S(8.W).asUInt)
        val __tmp_1896 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_1895 + 0.U) := __tmp_1896(7, 0)

        val __tmp_1897 = ((generalRegFiles(11.U) + 12.U(16.W)) + 7.S(8.W).asUInt)
        val __tmp_1898 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_1897 + 0.U) := __tmp_1898(7, 0)

        val __tmp_1899 = ((generalRegFiles(11.U) + 12.U(16.W)) + 8.S(8.W).asUInt)
        val __tmp_1900 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_1899 + 0.U) := __tmp_1900(7, 0)

        val __tmp_1901 = ((generalRegFiles(11.U) + 12.U(16.W)) + 9.S(8.W).asUInt)
        val __tmp_1902 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_1901 + 0.U) := __tmp_1902(7, 0)

        val __tmp_1903 = ((generalRegFiles(11.U) + 12.U(16.W)) + 10.S(8.W).asUInt)
        val __tmp_1904 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_1903 + 0.U) := __tmp_1904(7, 0)

        val __tmp_1905 = ((generalRegFiles(11.U) + 12.U(16.W)) + 11.S(8.W).asUInt)
        val __tmp_1906 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_1905 + 0.U) := __tmp_1906(7, 0)

        val __tmp_1907 = ((generalRegFiles(11.U) + 12.U(16.W)) + 12.S(8.W).asUInt)
        val __tmp_1908 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_1907 + 0.U) := __tmp_1908(7, 0)

        val __tmp_1909 = ((generalRegFiles(11.U) + 12.U(16.W)) + 13.S(8.W).asUInt)
        val __tmp_1910 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_1909 + 0.U) := __tmp_1910(7, 0)

        val __tmp_1911 = ((generalRegFiles(11.U) + 12.U(16.W)) + 14.S(8.W).asUInt)
        val __tmp_1912 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_1911 + 0.U) := __tmp_1912(7, 0)

        val __tmp_1913 = ((generalRegFiles(11.U) + 12.U(16.W)) + 15.S(8.W).asUInt)
        val __tmp_1914 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_1913 + 0.U) := __tmp_1914(7, 0)

        val __tmp_1915 = ((generalRegFiles(11.U) + 12.U(16.W)) + 16.S(8.W).asUInt)
        val __tmp_1916 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_1915 + 0.U) := __tmp_1916(7, 0)

        val __tmp_1917 = ((generalRegFiles(11.U) + 12.U(16.W)) + 17.S(8.W).asUInt)
        val __tmp_1918 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_1917 + 0.U) := __tmp_1918(7, 0)

        val __tmp_1919 = ((generalRegFiles(11.U) + 12.U(16.W)) + 18.S(8.W).asUInt)
        val __tmp_1920 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_1919 + 0.U) := __tmp_1920(7, 0)

        val __tmp_1921 = ((generalRegFiles(11.U) + 12.U(16.W)) + 19.S(8.W).asUInt)
        val __tmp_1922 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_1921 + 0.U) := __tmp_1922(7, 0)

        CP := 146.U
      }

      is(146.U) {
        /*
        (SP + (4: SP)) [MS[anvil.PrinterIndex.I20, U8], 34]  <-  ($11: MS[anvil.PrinterIndex.I20, U8]) [MS[anvil.PrinterIndex.I20, U8], ((*(($11: MS[anvil.PrinterIndex.I20, U8]) + (4: SP)) as SP) + (12: SP))]  // buff = ($11: MS[anvil.PrinterIndex.I20, U8])
        goto .147
        */


        val __tmp_1923 = (SP + 4.U(16.W))
        val __tmp_1924 = generalRegFiles(11.U)
        val __tmp_1925 = (Cat(
           arrayRegFiles((generalRegFiles(11.U) + 4.U(16.W)) + 7.U),
           arrayRegFiles((generalRegFiles(11.U) + 4.U(16.W)) + 6.U),
           arrayRegFiles((generalRegFiles(11.U) + 4.U(16.W)) + 5.U),
           arrayRegFiles((generalRegFiles(11.U) + 4.U(16.W)) + 4.U),
           arrayRegFiles((generalRegFiles(11.U) + 4.U(16.W)) + 3.U),
           arrayRegFiles((generalRegFiles(11.U) + 4.U(16.W)) + 2.U),
           arrayRegFiles((generalRegFiles(11.U) + 4.U(16.W)) + 1.U),
           arrayRegFiles((generalRegFiles(11.U) + 4.U(16.W)) + 0.U)
         ).asSInt.asUInt + 12.U(16.W))

        when(Idx < __tmp_1925) {
          arrayRegFiles(__tmp_1923 + Idx + 0.U) := arrayRegFiles(__tmp_1924 + Idx + 0.U)
          arrayRegFiles(__tmp_1923 + Idx + 1.U) := arrayRegFiles(__tmp_1924 + Idx + 1.U)
          arrayRegFiles(__tmp_1923 + Idx + 2.U) := arrayRegFiles(__tmp_1924 + Idx + 2.U)
          arrayRegFiles(__tmp_1923 + Idx + 3.U) := arrayRegFiles(__tmp_1924 + Idx + 3.U)
          arrayRegFiles(__tmp_1923 + Idx + 4.U) := arrayRegFiles(__tmp_1924 + Idx + 4.U)
          arrayRegFiles(__tmp_1923 + Idx + 5.U) := arrayRegFiles(__tmp_1924 + Idx + 5.U)
          arrayRegFiles(__tmp_1923 + Idx + 6.U) := arrayRegFiles(__tmp_1924 + Idx + 6.U)
          arrayRegFiles(__tmp_1923 + Idx + 7.U) := arrayRegFiles(__tmp_1924 + Idx + 7.U)
          Idx := Idx + 8.U
          LeftByteRounds := __tmp_1925 - Idx
        } .elsewhen(IdxLeftByteRounds < LeftByteRounds) {
          val __tmp_1926 = Idx - 8.U
          arrayRegFiles(__tmp_1923 + __tmp_1926 + IdxLeftByteRounds) := arrayRegFiles(__tmp_1924 + __tmp_1926 + IdxLeftByteRounds)
          IdxLeftByteRounds := IdxLeftByteRounds + 1.U
        } .otherwise {
          Idx := 0.U
          IdxLeftByteRounds := 0.U
          LeftByteRounds := 0.U
          CP := 147.U
        }


      }

      is(147.U) {
        /*
        unalloc $new@[168,16].5BB7E063: MS[anvil.PrinterIndex.I20, U8] [@38, 34]
        goto .148
        */


        CP := 148.U
      }

      is(148.U) {
        /*
        decl i: anvil.PrinterIndex.I20 @$4
        $4 = (0: anvil.PrinterIndex.I20)
        goto .149
        */



        generalRegFiles(4.U) := (0.S(8.W)).asUInt

        CP := 149.U
      }

      is(149.U) {
        /*
        decl neg: B @$5
        $10 = (($3: S64) < (0: S64))
        goto .150
        */



        generalRegFiles(10.U) := (generalRegFiles(3.U).asSInt < 0.S(64.W)).asUInt

        CP := 150.U
      }

      is(150.U) {
        /*
        $5 = ($10: B)
        goto .151
        */



        generalRegFiles(5.U) := generalRegFiles(10.U)

        CP := 151.U
      }

      is(151.U) {
        /*
        decl m: S64 @$6
        goto .152
        */


        CP := 152.U
      }

      is(152.U) {
        /*
        if ($5: B) goto .153 else goto .155
        */


        CP := Mux((generalRegFiles(5.U).asUInt) === 1.U, 153.U, 155.U)
      }

      is(153.U) {
        /*
        $12 = -(($3: S64))
        goto .154
        */



        generalRegFiles(12.U) := (-generalRegFiles(3.U).asSInt).asUInt

        CP := 154.U
      }

      is(154.U) {
        /*
        $10 = ($12: S64)
        goto .157
        */



        generalRegFiles(10.U) := (generalRegFiles(12.U).asSInt).asUInt

        CP := 157.U
      }

      is(155.U) {
        /*
        $14 = ($3: S64)
        goto .156
        */



        generalRegFiles(14.U) := (generalRegFiles(3.U).asSInt).asUInt

        CP := 156.U
      }

      is(156.U) {
        /*
        $10 = ($14: S64)
        goto .157
        */



        generalRegFiles(10.U) := (generalRegFiles(14.U).asSInt).asUInt

        CP := 157.U
      }

      is(157.U) {
        /*
        $6 = ($10: S64)
        goto .158
        */



        generalRegFiles(6.U) := (generalRegFiles(10.U).asSInt).asUInt

        CP := 158.U
      }

      is(158.U) {
        /*
        $11 = ($6: S64)
        goto .159
        */



        generalRegFiles(11.U) := (generalRegFiles(6.U).asSInt).asUInt

        CP := 159.U
      }

      is(159.U) {
        /*
        $10 = (($11: S64) > (0: S64))
        goto .160
        */



        generalRegFiles(10.U) := (generalRegFiles(11.U).asSInt > 0.S(64.W)).asUInt

        CP := 160.U
      }

      is(160.U) {
        /*
        if ($10: B) goto .161 else goto .190
        */


        CP := Mux((generalRegFiles(10.U).asUInt) === 1.U, 161.U, 190.U)
      }

      is(161.U) {
        /*
        $11 = ($6: S64)
        goto .162
        */



        generalRegFiles(11.U) := (generalRegFiles(6.U).asSInt).asUInt

        CP := 162.U
      }

      is(162.U) {
        /*
        $10 = (($11: S64) % (10: S64))
        goto .163
        */



        generalRegFiles(10.U) := ((generalRegFiles(11.U).asSInt % 10.S(64.W))).asUInt

        CP := 163.U
      }

      is(163.U) {
        /*
        switch (($10: S64))
          (0: S64): goto 164
          (1: S64): goto 166
          (2: S64): goto 168
          (3: S64): goto 170
          (4: S64): goto 172
          (5: S64): goto 174
          (6: S64): goto 176
          (7: S64): goto 178
          (8: S64): goto 180
          (9: S64): goto 182

        */


        val __tmp_1927 = generalRegFiles(10.U).asSInt

        switch(__tmp_1927) {

          is(0.S(64.W)) {
            CP := 164.U
          }


          is(1.S(64.W)) {
            CP := 166.U
          }


          is(2.S(64.W)) {
            CP := 168.U
          }


          is(3.S(64.W)) {
            CP := 170.U
          }


          is(4.S(64.W)) {
            CP := 172.U
          }


          is(5.S(64.W)) {
            CP := 174.U
          }


          is(6.S(64.W)) {
            CP := 176.U
          }


          is(7.S(64.W)) {
            CP := 178.U
          }


          is(8.S(64.W)) {
            CP := 180.U
          }


          is(9.S(64.W)) {
            CP := 182.U
          }

        }

      }

      is(164.U) {
        /*
        $11 = (SP + (4: SP))
        $10 = ($4: anvil.PrinterIndex.I20)
        goto .165
        */



        generalRegFiles(11.U) := (SP + 4.U(16.W))


        generalRegFiles(10.U) := (generalRegFiles(4.U).asSInt).asUInt

        CP := 165.U
      }

      is(165.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.I20, U8]) + (12: SP)) + (($10: anvil.PrinterIndex.I20) as SP)) = (48: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.I20, U8])(($10: anvil.PrinterIndex.I20)) = (48: U8)
        goto .184
        */


        val __tmp_1928 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(10.U).asSInt.asUInt)
        val __tmp_1929 = (48.U(8.W)).asUInt
        arrayRegFiles(__tmp_1928 + 0.U) := __tmp_1929(7, 0)

        CP := 184.U
      }

      is(166.U) {
        /*
        $11 = (SP + (4: SP))
        $10 = ($4: anvil.PrinterIndex.I20)
        goto .167
        */



        generalRegFiles(11.U) := (SP + 4.U(16.W))


        generalRegFiles(10.U) := (generalRegFiles(4.U).asSInt).asUInt

        CP := 167.U
      }

      is(167.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.I20, U8]) + (12: SP)) + (($10: anvil.PrinterIndex.I20) as SP)) = (49: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.I20, U8])(($10: anvil.PrinterIndex.I20)) = (49: U8)
        goto .184
        */


        val __tmp_1930 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(10.U).asSInt.asUInt)
        val __tmp_1931 = (49.U(8.W)).asUInt
        arrayRegFiles(__tmp_1930 + 0.U) := __tmp_1931(7, 0)

        CP := 184.U
      }

      is(168.U) {
        /*
        $11 = (SP + (4: SP))
        $10 = ($4: anvil.PrinterIndex.I20)
        goto .169
        */



        generalRegFiles(11.U) := (SP + 4.U(16.W))


        generalRegFiles(10.U) := (generalRegFiles(4.U).asSInt).asUInt

        CP := 169.U
      }

      is(169.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.I20, U8]) + (12: SP)) + (($10: anvil.PrinterIndex.I20) as SP)) = (50: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.I20, U8])(($10: anvil.PrinterIndex.I20)) = (50: U8)
        goto .184
        */


        val __tmp_1932 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(10.U).asSInt.asUInt)
        val __tmp_1933 = (50.U(8.W)).asUInt
        arrayRegFiles(__tmp_1932 + 0.U) := __tmp_1933(7, 0)

        CP := 184.U
      }

      is(170.U) {
        /*
        $11 = (SP + (4: SP))
        $10 = ($4: anvil.PrinterIndex.I20)
        goto .171
        */



        generalRegFiles(11.U) := (SP + 4.U(16.W))


        generalRegFiles(10.U) := (generalRegFiles(4.U).asSInt).asUInt

        CP := 171.U
      }

      is(171.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.I20, U8]) + (12: SP)) + (($10: anvil.PrinterIndex.I20) as SP)) = (51: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.I20, U8])(($10: anvil.PrinterIndex.I20)) = (51: U8)
        goto .184
        */


        val __tmp_1934 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(10.U).asSInt.asUInt)
        val __tmp_1935 = (51.U(8.W)).asUInt
        arrayRegFiles(__tmp_1934 + 0.U) := __tmp_1935(7, 0)

        CP := 184.U
      }

      is(172.U) {
        /*
        $11 = (SP + (4: SP))
        $10 = ($4: anvil.PrinterIndex.I20)
        goto .173
        */



        generalRegFiles(11.U) := (SP + 4.U(16.W))


        generalRegFiles(10.U) := (generalRegFiles(4.U).asSInt).asUInt

        CP := 173.U
      }

      is(173.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.I20, U8]) + (12: SP)) + (($10: anvil.PrinterIndex.I20) as SP)) = (52: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.I20, U8])(($10: anvil.PrinterIndex.I20)) = (52: U8)
        goto .184
        */


        val __tmp_1936 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(10.U).asSInt.asUInt)
        val __tmp_1937 = (52.U(8.W)).asUInt
        arrayRegFiles(__tmp_1936 + 0.U) := __tmp_1937(7, 0)

        CP := 184.U
      }

      is(174.U) {
        /*
        $11 = (SP + (4: SP))
        $10 = ($4: anvil.PrinterIndex.I20)
        goto .175
        */



        generalRegFiles(11.U) := (SP + 4.U(16.W))


        generalRegFiles(10.U) := (generalRegFiles(4.U).asSInt).asUInt

        CP := 175.U
      }

      is(175.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.I20, U8]) + (12: SP)) + (($10: anvil.PrinterIndex.I20) as SP)) = (53: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.I20, U8])(($10: anvil.PrinterIndex.I20)) = (53: U8)
        goto .184
        */


        val __tmp_1938 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(10.U).asSInt.asUInt)
        val __tmp_1939 = (53.U(8.W)).asUInt
        arrayRegFiles(__tmp_1938 + 0.U) := __tmp_1939(7, 0)

        CP := 184.U
      }

      is(176.U) {
        /*
        $11 = (SP + (4: SP))
        $10 = ($4: anvil.PrinterIndex.I20)
        goto .177
        */



        generalRegFiles(11.U) := (SP + 4.U(16.W))


        generalRegFiles(10.U) := (generalRegFiles(4.U).asSInt).asUInt

        CP := 177.U
      }

      is(177.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.I20, U8]) + (12: SP)) + (($10: anvil.PrinterIndex.I20) as SP)) = (54: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.I20, U8])(($10: anvil.PrinterIndex.I20)) = (54: U8)
        goto .184
        */


        val __tmp_1940 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(10.U).asSInt.asUInt)
        val __tmp_1941 = (54.U(8.W)).asUInt
        arrayRegFiles(__tmp_1940 + 0.U) := __tmp_1941(7, 0)

        CP := 184.U
      }

      is(178.U) {
        /*
        $11 = (SP + (4: SP))
        $10 = ($4: anvil.PrinterIndex.I20)
        goto .179
        */



        generalRegFiles(11.U) := (SP + 4.U(16.W))


        generalRegFiles(10.U) := (generalRegFiles(4.U).asSInt).asUInt

        CP := 179.U
      }

      is(179.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.I20, U8]) + (12: SP)) + (($10: anvil.PrinterIndex.I20) as SP)) = (55: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.I20, U8])(($10: anvil.PrinterIndex.I20)) = (55: U8)
        goto .184
        */


        val __tmp_1942 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(10.U).asSInt.asUInt)
        val __tmp_1943 = (55.U(8.W)).asUInt
        arrayRegFiles(__tmp_1942 + 0.U) := __tmp_1943(7, 0)

        CP := 184.U
      }

      is(180.U) {
        /*
        $11 = (SP + (4: SP))
        $10 = ($4: anvil.PrinterIndex.I20)
        goto .181
        */



        generalRegFiles(11.U) := (SP + 4.U(16.W))


        generalRegFiles(10.U) := (generalRegFiles(4.U).asSInt).asUInt

        CP := 181.U
      }

      is(181.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.I20, U8]) + (12: SP)) + (($10: anvil.PrinterIndex.I20) as SP)) = (56: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.I20, U8])(($10: anvil.PrinterIndex.I20)) = (56: U8)
        goto .184
        */


        val __tmp_1944 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(10.U).asSInt.asUInt)
        val __tmp_1945 = (56.U(8.W)).asUInt
        arrayRegFiles(__tmp_1944 + 0.U) := __tmp_1945(7, 0)

        CP := 184.U
      }

      is(182.U) {
        /*
        $11 = (SP + (4: SP))
        $10 = ($4: anvil.PrinterIndex.I20)
        goto .183
        */



        generalRegFiles(11.U) := (SP + 4.U(16.W))


        generalRegFiles(10.U) := (generalRegFiles(4.U).asSInt).asUInt

        CP := 183.U
      }

      is(183.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.I20, U8]) + (12: SP)) + (($10: anvil.PrinterIndex.I20) as SP)) = (57: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.I20, U8])(($10: anvil.PrinterIndex.I20)) = (57: U8)
        goto .184
        */


        val __tmp_1946 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(10.U).asSInt.asUInt)
        val __tmp_1947 = (57.U(8.W)).asUInt
        arrayRegFiles(__tmp_1946 + 0.U) := __tmp_1947(7, 0)

        CP := 184.U
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
        $10 = (($11: S64) / (10: S64))
        goto .186
        */



        generalRegFiles(10.U) := ((generalRegFiles(11.U).asSInt / 10.S(64.W))).asUInt

        CP := 186.U
      }

      is(186.U) {
        /*
        $6 = ($10: S64)
        goto .187
        */



        generalRegFiles(6.U) := (generalRegFiles(10.U).asSInt).asUInt

        CP := 187.U
      }

      is(187.U) {
        /*
        $11 = ($4: anvil.PrinterIndex.I20)
        goto .188
        */



        generalRegFiles(11.U) := (generalRegFiles(4.U).asSInt).asUInt

        CP := 188.U
      }

      is(188.U) {
        /*
        $10 = (($11: anvil.PrinterIndex.I20) + (1: anvil.PrinterIndex.I20))
        goto .189
        */



        generalRegFiles(10.U) := ((generalRegFiles(11.U).asSInt + 1.S(8.W))).asUInt

        CP := 189.U
      }

      is(189.U) {
        /*
        $4 = ($10: anvil.PrinterIndex.I20)
        goto .158
        */



        generalRegFiles(4.U) := (generalRegFiles(10.U).asSInt).asUInt

        CP := 158.U
      }

      is(190.U) {
        /*
        $11 = ($5: B)
        undecl neg: B @$5
        goto .191
        */



        generalRegFiles(11.U) := generalRegFiles(5.U)

        CP := 191.U
      }

      is(191.U) {
        /*
        if ($11: B) goto .192 else goto .197
        */


        CP := Mux((generalRegFiles(11.U).asUInt) === 1.U, 192.U, 197.U)
      }

      is(192.U) {
        /*
        $11 = (SP + (4: SP))
        $10 = ($4: anvil.PrinterIndex.I20)
        goto .193
        */



        generalRegFiles(11.U) := (SP + 4.U(16.W))


        generalRegFiles(10.U) := (generalRegFiles(4.U).asSInt).asUInt

        CP := 193.U
      }

      is(193.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.I20, U8]) + (12: SP)) + (($10: anvil.PrinterIndex.I20) as SP)) = (45: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.I20, U8])(($10: anvil.PrinterIndex.I20)) = (45: U8)
        goto .194
        */


        val __tmp_1948 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(10.U).asSInt.asUInt)
        val __tmp_1949 = (45.U(8.W)).asUInt
        arrayRegFiles(__tmp_1948 + 0.U) := __tmp_1949(7, 0)

        CP := 194.U
      }

      is(194.U) {
        /*
        $11 = ($4: anvil.PrinterIndex.I20)
        goto .195
        */



        generalRegFiles(11.U) := (generalRegFiles(4.U).asSInt).asUInt

        CP := 195.U
      }

      is(195.U) {
        /*
        $10 = (($11: anvil.PrinterIndex.I20) + (1: anvil.PrinterIndex.I20))
        goto .196
        */



        generalRegFiles(10.U) := ((generalRegFiles(11.U).asSInt + 1.S(8.W))).asUInt

        CP := 196.U
      }

      is(196.U) {
        /*
        $4 = ($10: anvil.PrinterIndex.I20)
        goto .197
        */



        generalRegFiles(4.U) := (generalRegFiles(10.U).asSInt).asUInt

        CP := 197.U
      }

      is(197.U) {
        /*
        decl j: anvil.PrinterIndex.I20 @$7
        $11 = ($4: anvil.PrinterIndex.I20)
        undecl i: anvil.PrinterIndex.I20 @$4
        goto .198
        */



        generalRegFiles(11.U) := (generalRegFiles(4.U).asSInt).asUInt

        CP := 198.U
      }

      is(198.U) {
        /*
        $10 = (($11: anvil.PrinterIndex.I20) - (1: anvil.PrinterIndex.I20))
        goto .199
        */



        generalRegFiles(10.U) := ((generalRegFiles(11.U).asSInt - 1.S(8.W))).asUInt

        CP := 199.U
      }

      is(199.U) {
        /*
        $7 = ($10: anvil.PrinterIndex.I20)
        goto .200
        */



        generalRegFiles(7.U) := (generalRegFiles(10.U).asSInt).asUInt

        CP := 200.U
      }

      is(200.U) {
        /*
        decl idx: anvil.PrinterIndex.U @$8
        $11 = ($1: anvil.PrinterIndex.U)
        goto .201
        */



        generalRegFiles(11.U) := generalRegFiles(1.U)

        CP := 201.U
      }

      is(201.U) {
        /*
        $8 = ($11: anvil.PrinterIndex.U)
        goto .202
        */



        generalRegFiles(8.U) := generalRegFiles(11.U)

        CP := 202.U
      }

      is(202.U) {
        /*
        decl r: U64 @$9
        $9 = (0: U64)
        goto .203
        */



        generalRegFiles(9.U) := 0.U(64.W)

        CP := 203.U
      }

      is(203.U) {
        /*
        $11 = ($7: anvil.PrinterIndex.I20)
        goto .204
        */



        generalRegFiles(11.U) := (generalRegFiles(7.U).asSInt).asUInt

        CP := 204.U
      }

      is(204.U) {
        /*
        $10 = (($11: anvil.PrinterIndex.I20) >= (0: anvil.PrinterIndex.I20))
        goto .205
        */



        generalRegFiles(10.U) := (generalRegFiles(11.U).asSInt >= 0.S(8.W)).asUInt

        CP := 205.U
      }

      is(205.U) {
        /*
        if ($10: B) goto .206 else goto .220
        */


        CP := Mux((generalRegFiles(10.U).asUInt) === 1.U, 206.U, 220.U)
      }

      is(206.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $10 = ($8: anvil.PrinterIndex.U)
        $13 = ($2: anvil.PrinterIndex.U)
        goto .207
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(10.U) := generalRegFiles(8.U)


        generalRegFiles(13.U) := generalRegFiles(2.U)

        CP := 207.U
      }

      is(207.U) {
        /*
        $12 = (($10: anvil.PrinterIndex.U) & ($13: anvil.PrinterIndex.U))
        goto .208
        */



        generalRegFiles(12.U) := (generalRegFiles(10.U) & generalRegFiles(13.U))

        CP := 208.U
      }

      is(208.U) {
        /*
        $14 = (SP + (4: SP))
        $15 = ($7: anvil.PrinterIndex.I20)
        goto .209
        */



        generalRegFiles(14.U) := (SP + 4.U(16.W))


        generalRegFiles(15.U) := (generalRegFiles(7.U).asSInt).asUInt

        CP := 209.U
      }

      is(209.U) {
        /*
        $16 = *((($14: MS[anvil.PrinterIndex.I20, U8]) + (12: SP)) + (($15: anvil.PrinterIndex.I20) as SP)) [unsigned, U8, 1]  // $16 = ($14: MS[anvil.PrinterIndex.I20, U8])(($15: anvil.PrinterIndex.I20))
        goto .210
        */


        val __tmp_1950 = (((generalRegFiles(14.U) + 12.U(16.W)) + generalRegFiles(15.U).asSInt.asUInt)).asUInt
        generalRegFiles(16.U) := Cat(
          arrayRegFiles(__tmp_1950 + 0.U)
        ).asUInt

        CP := 210.U
      }

      is(210.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($12: anvil.PrinterIndex.U) as SP)) = ($16: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($12: anvil.PrinterIndex.U)) = ($16: U8)
        goto .211
        */


        val __tmp_1951 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(12.U).asUInt)
        val __tmp_1952 = (generalRegFiles(16.U)).asUInt
        arrayRegFiles(__tmp_1951 + 0.U) := __tmp_1952(7, 0)

        CP := 211.U
      }

      is(211.U) {
        /*
        $11 = ($7: anvil.PrinterIndex.I20)
        goto .212
        */



        generalRegFiles(11.U) := (generalRegFiles(7.U).asSInt).asUInt

        CP := 212.U
      }

      is(212.U) {
        /*
        $10 = (($11: anvil.PrinterIndex.I20) - (1: anvil.PrinterIndex.I20))
        goto .213
        */



        generalRegFiles(10.U) := ((generalRegFiles(11.U).asSInt - 1.S(8.W))).asUInt

        CP := 213.U
      }

      is(213.U) {
        /*
        $7 = ($10: anvil.PrinterIndex.I20)
        goto .214
        */



        generalRegFiles(7.U) := (generalRegFiles(10.U).asSInt).asUInt

        CP := 214.U
      }

      is(214.U) {
        /*
        $11 = ($8: anvil.PrinterIndex.U)
        goto .215
        */



        generalRegFiles(11.U) := generalRegFiles(8.U)

        CP := 215.U
      }

      is(215.U) {
        /*
        $10 = (($11: anvil.PrinterIndex.U) + (1: anvil.PrinterIndex.U))
        goto .216
        */



        generalRegFiles(10.U) := (generalRegFiles(11.U) + 1.U(64.W))

        CP := 216.U
      }

      is(216.U) {
        /*
        $8 = ($10: anvil.PrinterIndex.U)
        goto .217
        */



        generalRegFiles(8.U) := generalRegFiles(10.U)

        CP := 217.U
      }

      is(217.U) {
        /*
        $11 = ($9: U64)
        goto .218
        */



        generalRegFiles(11.U) := generalRegFiles(9.U)

        CP := 218.U
      }

      is(218.U) {
        /*
        $10 = (($11: U64) + (1: U64))
        goto .219
        */



        generalRegFiles(10.U) := (generalRegFiles(11.U) + 1.U(64.W))

        CP := 219.U
      }

      is(219.U) {
        /*
        $9 = ($10: U64)
        goto .203
        */



        generalRegFiles(9.U) := generalRegFiles(10.U)

        CP := 203.U
      }

      is(220.U) {
        /*
        $11 = ($9: U64)
        undecl r: U64 @$9
        goto .221
        */



        generalRegFiles(11.U) := generalRegFiles(9.U)

        CP := 221.U
      }

      is(221.U) {
        /*
        **(SP + (2: SP)) = ($11: U64) [unsigned, U64, 8]  // $res = ($11: U64)
        goto $ret@0
        */


        val __tmp_1953 = Cat(
          arrayRegFiles((SP + 2.U(16.W)) + 1.U),
          arrayRegFiles((SP + 2.U(16.W)) + 0.U)
        )
        val __tmp_1954 = (generalRegFiles(11.U)).asUInt
        arrayRegFiles(__tmp_1953 + 0.U) := __tmp_1954(7, 0)
        arrayRegFiles(__tmp_1953 + 1.U) := __tmp_1954(15, 8)
        arrayRegFiles(__tmp_1953 + 2.U) := __tmp_1954(23, 16)
        arrayRegFiles(__tmp_1953 + 3.U) := __tmp_1954(31, 24)
        arrayRegFiles(__tmp_1953 + 4.U) := __tmp_1954(39, 32)
        arrayRegFiles(__tmp_1953 + 5.U) := __tmp_1954(47, 40)
        arrayRegFiles(__tmp_1953 + 6.U) := __tmp_1954(55, 48)
        arrayRegFiles(__tmp_1953 + 7.U) := __tmp_1954(63, 56)

        CP := Cat(
          arrayRegFiles(SP + 0.U + 1.U),
          arrayRegFiles(SP + 0.U + 0.U)
        )

      }

    }

}


