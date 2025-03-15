package GeneralRegFileToBRAM 

import chisel3._
import chisel3.util._
import chisel3.experimental._

class bubble (val C_S_AXI_DATA_WIDTH:  Int = 32,
               val C_S_AXI_ADDR_WIDTH:  Int = 32,
               val ARRAY_REG_WIDTH:     Int = 8,
               val ARRAY_REG_DEPTH:     Int = 1024,
               val GENERAL_REG_WIDTH:   Int = 64,
               val GENERAL_REG_DEPTH:   Int = 7,
               val STACK_POINTER_WIDTH: Int = 16,
               val CODE_POINTER_WIDTH:  Int = 8) extends Module {

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
    val DP = RegInit(0.U(STACK_POINTER_WIDTH.W))
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
        SP = 0
        *0 = 0 [unsigned, CP, 1]  // $ret
        *1 = 3 [unsigned, SP, 2]  // data address of a (size = 212)
        goto .4
        */


        SP := 0.U

        val __tmp_1599 = 0.U
        val __tmp_1600 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_1599 + 0.U) := __tmp_1600(7, 0)

        val __tmp_1601 = 1.U
        val __tmp_1602 = (3.U(16.W)).asUInt
        arrayRegFiles(__tmp_1601 + 0.U) := __tmp_1602(7, 0)
        arrayRegFiles(__tmp_1601 + 1.U) := __tmp_1602(15, 8)

        CP := 4.U
      }

      is(4.U) {
        /*
        decl i: Z [@215, 8]
        *(SP + 215) = 0 [signed, Z, 8]  // i = 0
        goto .5
        */


        val __tmp_1603 = SP + 215.U
        val __tmp_1604 = (0.S(64.W)).asUInt
        arrayRegFiles(__tmp_1603 + 0.U) := __tmp_1604(7, 0)
        arrayRegFiles(__tmp_1603 + 1.U) := __tmp_1604(15, 8)
        arrayRegFiles(__tmp_1603 + 2.U) := __tmp_1604(23, 16)
        arrayRegFiles(__tmp_1603 + 3.U) := __tmp_1604(31, 24)
        arrayRegFiles(__tmp_1603 + 4.U) := __tmp_1604(39, 32)
        arrayRegFiles(__tmp_1603 + 5.U) := __tmp_1604(47, 40)
        arrayRegFiles(__tmp_1603 + 6.U) := __tmp_1604(55, 48)
        arrayRegFiles(__tmp_1603 + 7.U) := __tmp_1604(63, 56)

        CP := 5.U
      }

      is(5.U) {
        /*
        $1 = *(SP + 215) [signed, Z, 8]  // $1 = i
        $0 = *(SP + 1) [unsigned, MS[Z, S16], 2]  // $0 = a
        goto .6
        */


        val __tmp_1605 = (SP + 215.U).asUInt
        generalRegFiles(1.U) := Cat(
          arrayRegFiles(__tmp_1605 + 7.U),
          arrayRegFiles(__tmp_1605 + 6.U),
          arrayRegFiles(__tmp_1605 + 5.U),
          arrayRegFiles(__tmp_1605 + 4.U),
          arrayRegFiles(__tmp_1605 + 3.U),
          arrayRegFiles(__tmp_1605 + 2.U),
          arrayRegFiles(__tmp_1605 + 1.U),
          arrayRegFiles(__tmp_1605 + 0.U)
        ).asUInt

        val __tmp_1606 = (SP + 1.U).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_1606 + 1.U),
          arrayRegFiles(__tmp_1606 + 0.U)
        ).asUInt

        CP := 6.U
      }

      is(6.U) {
        /*
        $2 = *($0 + 4) [signed, Z, 8]  // $2 = $0.size
        goto .7
        */


        val __tmp_1607 = (generalRegFiles(0.U) + 4.U).asUInt
        generalRegFiles(2.U) := Cat(
          arrayRegFiles(__tmp_1607 + 7.U),
          arrayRegFiles(__tmp_1607 + 6.U),
          arrayRegFiles(__tmp_1607 + 5.U),
          arrayRegFiles(__tmp_1607 + 4.U),
          arrayRegFiles(__tmp_1607 + 3.U),
          arrayRegFiles(__tmp_1607 + 2.U),
          arrayRegFiles(__tmp_1607 + 1.U),
          arrayRegFiles(__tmp_1607 + 0.U)
        ).asUInt

        CP := 7.U
      }

      is(7.U) {
        /*
        $3 = ($1 < $2)
        goto .8
        */


        generalRegFiles(3.U) := (generalRegFiles(1.U).asSInt < generalRegFiles(2.U).asSInt).asUInt
        CP := 8.U
      }

      is(8.U) {
        /*
        if $3 goto .9 else goto .48
        */


        CP := Mux((generalRegFiles(3.U).asUInt) === 1.U, 9.U, 48.U)
      }

      is(9.U) {
        /*
        decl j: Z [@223, 8]
        $1 = *(SP + 215) [signed, Z, 8]  // $1 = i
        goto .10
        */


        val __tmp_1608 = (SP + 215.U).asUInt
        generalRegFiles(1.U) := Cat(
          arrayRegFiles(__tmp_1608 + 7.U),
          arrayRegFiles(__tmp_1608 + 6.U),
          arrayRegFiles(__tmp_1608 + 5.U),
          arrayRegFiles(__tmp_1608 + 4.U),
          arrayRegFiles(__tmp_1608 + 3.U),
          arrayRegFiles(__tmp_1608 + 2.U),
          arrayRegFiles(__tmp_1608 + 1.U),
          arrayRegFiles(__tmp_1608 + 0.U)
        ).asUInt

        CP := 10.U
      }

      is(10.U) {
        /*
        $0 = ($1 + 1)
        goto .11
        */


        generalRegFiles(0.U) := (generalRegFiles(1.U).asSInt + 1.S).asUInt
        CP := 11.U
      }

      is(11.U) {
        /*
        *(SP + 223) = $0 [signed, Z, 8]  // j = $0
        goto .12
        */


        val __tmp_1609 = SP + 223.U
        val __tmp_1610 = (generalRegFiles(0.U).asSInt).asUInt
        arrayRegFiles(__tmp_1609 + 0.U) := __tmp_1610(7, 0)
        arrayRegFiles(__tmp_1609 + 1.U) := __tmp_1610(15, 8)
        arrayRegFiles(__tmp_1609 + 2.U) := __tmp_1610(23, 16)
        arrayRegFiles(__tmp_1609 + 3.U) := __tmp_1610(31, 24)
        arrayRegFiles(__tmp_1609 + 4.U) := __tmp_1610(39, 32)
        arrayRegFiles(__tmp_1609 + 5.U) := __tmp_1610(47, 40)
        arrayRegFiles(__tmp_1609 + 6.U) := __tmp_1610(55, 48)
        arrayRegFiles(__tmp_1609 + 7.U) := __tmp_1610(63, 56)

        CP := 12.U
      }

      is(12.U) {
        /*
        $1 = *(SP + 223) [signed, Z, 8]  // $1 = j
        $0 = *(SP + 1) [unsigned, MS[Z, S16], 2]  // $0 = a
        goto .13
        */


        val __tmp_1611 = (SP + 223.U).asUInt
        generalRegFiles(1.U) := Cat(
          arrayRegFiles(__tmp_1611 + 7.U),
          arrayRegFiles(__tmp_1611 + 6.U),
          arrayRegFiles(__tmp_1611 + 5.U),
          arrayRegFiles(__tmp_1611 + 4.U),
          arrayRegFiles(__tmp_1611 + 3.U),
          arrayRegFiles(__tmp_1611 + 2.U),
          arrayRegFiles(__tmp_1611 + 1.U),
          arrayRegFiles(__tmp_1611 + 0.U)
        ).asUInt

        val __tmp_1612 = (SP + 1.U).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_1612 + 1.U),
          arrayRegFiles(__tmp_1612 + 0.U)
        ).asUInt

        CP := 13.U
      }

      is(13.U) {
        /*
        $2 = *($0 + 4) [signed, Z, 8]  // $2 = $0.size
        goto .14
        */


        val __tmp_1613 = (generalRegFiles(0.U) + 4.U).asUInt
        generalRegFiles(2.U) := Cat(
          arrayRegFiles(__tmp_1613 + 7.U),
          arrayRegFiles(__tmp_1613 + 6.U),
          arrayRegFiles(__tmp_1613 + 5.U),
          arrayRegFiles(__tmp_1613 + 4.U),
          arrayRegFiles(__tmp_1613 + 3.U),
          arrayRegFiles(__tmp_1613 + 2.U),
          arrayRegFiles(__tmp_1613 + 1.U),
          arrayRegFiles(__tmp_1613 + 0.U)
        ).asUInt

        CP := 14.U
      }

      is(14.U) {
        /*
        $3 = ($1 < $2)
        goto .15
        */


        generalRegFiles(3.U) := (generalRegFiles(1.U).asSInt < generalRegFiles(2.U).asSInt).asUInt
        CP := 15.U
      }

      is(15.U) {
        /*
        if $3 goto .16 else goto .44
        */


        CP := Mux((generalRegFiles(3.U).asUInt) === 1.U, 16.U, 44.U)
      }

      is(16.U) {
        /*
        $1 = *(SP + 1) [unsigned, MS[Z, S16], 2]  // $1 = a
        $0 = *(SP + 215) [signed, Z, 8]  // $0 = i
        goto .17
        */


        val __tmp_1614 = (SP + 1.U).asUInt
        generalRegFiles(1.U) := Cat(
          arrayRegFiles(__tmp_1614 + 1.U),
          arrayRegFiles(__tmp_1614 + 0.U)
        ).asUInt

        val __tmp_1615 = (SP + 215.U).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_1615 + 7.U),
          arrayRegFiles(__tmp_1615 + 6.U),
          arrayRegFiles(__tmp_1615 + 5.U),
          arrayRegFiles(__tmp_1615 + 4.U),
          arrayRegFiles(__tmp_1615 + 3.U),
          arrayRegFiles(__tmp_1615 + 2.U),
          arrayRegFiles(__tmp_1615 + 1.U),
          arrayRegFiles(__tmp_1615 + 0.U)
        ).asUInt

        CP := 17.U
      }

      is(17.U) {
        /*
        if ((0 <= $0) & ($0 <= *($1 + 4))) goto .18 else goto .1
        */


        CP := Mux(((0.S <= generalRegFiles(0.U).asSInt).asUInt & (generalRegFiles(0.U).asSInt <= Cat(
                            arrayRegFiles(generalRegFiles(1.U) + 4.U + 7.U),
                            arrayRegFiles(generalRegFiles(1.U) + 4.U + 6.U),
                            arrayRegFiles(generalRegFiles(1.U) + 4.U + 5.U),
                            arrayRegFiles(generalRegFiles(1.U) + 4.U + 4.U),
                            arrayRegFiles(generalRegFiles(1.U) + 4.U + 3.U),
                            arrayRegFiles(generalRegFiles(1.U) + 4.U + 2.U),
                            arrayRegFiles(generalRegFiles(1.U) + 4.U + 1.U),
                            arrayRegFiles(generalRegFiles(1.U) + 4.U + 0.U)
                          ).asSInt).asUInt.asUInt) === 1.U, 18.U, 1.U)
      }

      is(18.U) {
        /*
        $2 = *(($1 + 12) + (($0 as SP) * 2)) [signed, S16, 2]  // $2 = $1($0)
        $3 = *(SP + 1) [unsigned, MS[Z, S16], 2]  // $3 = a
        $4 = *(SP + 223) [signed, Z, 8]  // $4 = j
        goto .19
        */


        val __tmp_1616 = (generalRegFiles(1.U) + 12.U + generalRegFiles(0.U).asSInt.asUInt * 2.U).asUInt
        generalRegFiles(2.U) := Cat(
          arrayRegFiles(__tmp_1616 + 1.U),
          arrayRegFiles(__tmp_1616 + 0.U)
        ).asUInt

        val __tmp_1617 = (SP + 1.U).asUInt
        generalRegFiles(3.U) := Cat(
          arrayRegFiles(__tmp_1617 + 1.U),
          arrayRegFiles(__tmp_1617 + 0.U)
        ).asUInt

        val __tmp_1618 = (SP + 223.U).asUInt
        generalRegFiles(4.U) := Cat(
          arrayRegFiles(__tmp_1618 + 7.U),
          arrayRegFiles(__tmp_1618 + 6.U),
          arrayRegFiles(__tmp_1618 + 5.U),
          arrayRegFiles(__tmp_1618 + 4.U),
          arrayRegFiles(__tmp_1618 + 3.U),
          arrayRegFiles(__tmp_1618 + 2.U),
          arrayRegFiles(__tmp_1618 + 1.U),
          arrayRegFiles(__tmp_1618 + 0.U)
        ).asUInt

        CP := 19.U
      }

      is(19.U) {
        /*
        if ((0 <= $4) & ($4 <= *($3 + 4))) goto .20 else goto .1
        */


        CP := Mux(((0.S <= generalRegFiles(4.U).asSInt).asUInt & (generalRegFiles(4.U).asSInt <= Cat(
                            arrayRegFiles(generalRegFiles(3.U) + 4.U + 7.U),
                            arrayRegFiles(generalRegFiles(3.U) + 4.U + 6.U),
                            arrayRegFiles(generalRegFiles(3.U) + 4.U + 5.U),
                            arrayRegFiles(generalRegFiles(3.U) + 4.U + 4.U),
                            arrayRegFiles(generalRegFiles(3.U) + 4.U + 3.U),
                            arrayRegFiles(generalRegFiles(3.U) + 4.U + 2.U),
                            arrayRegFiles(generalRegFiles(3.U) + 4.U + 1.U),
                            arrayRegFiles(generalRegFiles(3.U) + 4.U + 0.U)
                          ).asSInt).asUInt.asUInt) === 1.U, 20.U, 1.U)
      }

      is(20.U) {
        /*
        $5 = *(($3 + 12) + (($4 as SP) * 2)) [signed, S16, 2]  // $5 = $3($4)
        goto .21
        */


        val __tmp_1619 = (generalRegFiles(3.U) + 12.U + generalRegFiles(4.U).asSInt.asUInt * 2.U).asUInt
        generalRegFiles(5.U) := Cat(
          arrayRegFiles(__tmp_1619 + 1.U),
          arrayRegFiles(__tmp_1619 + 0.U)
        ).asUInt

        CP := 21.U
      }

      is(21.U) {
        /*
        $6 = ($2 > $5)
        goto .22
        */


        generalRegFiles(6.U) := (generalRegFiles(2.U) > generalRegFiles(5.U)).asUInt
        CP := 22.U
      }

      is(22.U) {
        /*
        if $6 goto .23 else goto .39
        */


        CP := Mux((generalRegFiles(6.U).asUInt) === 1.U, 23.U, 39.U)
      }

      is(23.U) {
        /*
        $1 = *(SP + 1) [unsigned, MS[Z, S16], 2]  // $1 = a
        $0 = *(SP + 215) [signed, Z, 8]  // $0 = i
        $2 = *(SP + 223) [signed, Z, 8]  // $2 = j
        SP = SP + 287
        decl $ret: CP [@0, 1], a: MS[Z, S16] [@1, 2], i: Z [@3, 8], j: Z [@11, 8]
        goto .24
        */


        val __tmp_1620 = (SP + 1.U).asUInt
        generalRegFiles(1.U) := Cat(
          arrayRegFiles(__tmp_1620 + 1.U),
          arrayRegFiles(__tmp_1620 + 0.U)
        ).asUInt

        val __tmp_1621 = (SP + 215.U).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_1621 + 7.U),
          arrayRegFiles(__tmp_1621 + 6.U),
          arrayRegFiles(__tmp_1621 + 5.U),
          arrayRegFiles(__tmp_1621 + 4.U),
          arrayRegFiles(__tmp_1621 + 3.U),
          arrayRegFiles(__tmp_1621 + 2.U),
          arrayRegFiles(__tmp_1621 + 1.U),
          arrayRegFiles(__tmp_1621 + 0.U)
        ).asUInt

        val __tmp_1622 = (SP + 223.U).asUInt
        generalRegFiles(2.U) := Cat(
          arrayRegFiles(__tmp_1622 + 7.U),
          arrayRegFiles(__tmp_1622 + 6.U),
          arrayRegFiles(__tmp_1622 + 5.U),
          arrayRegFiles(__tmp_1622 + 4.U),
          arrayRegFiles(__tmp_1622 + 3.U),
          arrayRegFiles(__tmp_1622 + 2.U),
          arrayRegFiles(__tmp_1622 + 1.U),
          arrayRegFiles(__tmp_1622 + 0.U)
        ).asUInt

        SP := SP + 287.U

        CP := 24.U
      }

      is(24.U) {
        /*
        *SP = 49 [unsigned, CP, 1]  // $ret@0 = 50
        *(SP + 1) = $1 [unsigned, SP, 2]  // a = $1
        *(SP + 3) = $0 [signed, Z, 8]  // i = $0
        *(SP + 11) = $2 [signed, Z, 8]  // j = $2
        *(SP - 56) = $0 [unsigned, U64, 8]  // save $0
        *(SP - 48) = $1 [unsigned, U64, 8]  // save $1
        *(SP - 40) = $2 [unsigned, U64, 8]  // save $2
        *(SP - 32) = $3 [unsigned, U64, 8]  // save $3
        *(SP - 24) = $4 [unsigned, U64, 8]  // save $4
        *(SP - 16) = $5 [unsigned, U64, 8]  // save $5
        *(SP - 8) = $6 [unsigned, U64, 8]  // save $6
        goto .25
        */


        val __tmp_1623 = SP
        val __tmp_1624 = (49.U(8.W)).asUInt
        arrayRegFiles(__tmp_1623 + 0.U) := __tmp_1624(7, 0)

        val __tmp_1625 = SP + 1.U
        val __tmp_1626 = (generalRegFiles(1.U)).asUInt
        arrayRegFiles(__tmp_1625 + 0.U) := __tmp_1626(7, 0)
        arrayRegFiles(__tmp_1625 + 1.U) := __tmp_1626(15, 8)

        val __tmp_1627 = SP + 3.U
        val __tmp_1628 = (generalRegFiles(0.U).asSInt).asUInt
        arrayRegFiles(__tmp_1627 + 0.U) := __tmp_1628(7, 0)
        arrayRegFiles(__tmp_1627 + 1.U) := __tmp_1628(15, 8)
        arrayRegFiles(__tmp_1627 + 2.U) := __tmp_1628(23, 16)
        arrayRegFiles(__tmp_1627 + 3.U) := __tmp_1628(31, 24)
        arrayRegFiles(__tmp_1627 + 4.U) := __tmp_1628(39, 32)
        arrayRegFiles(__tmp_1627 + 5.U) := __tmp_1628(47, 40)
        arrayRegFiles(__tmp_1627 + 6.U) := __tmp_1628(55, 48)
        arrayRegFiles(__tmp_1627 + 7.U) := __tmp_1628(63, 56)

        val __tmp_1629 = SP + 11.U
        val __tmp_1630 = (generalRegFiles(2.U).asSInt).asUInt
        arrayRegFiles(__tmp_1629 + 0.U) := __tmp_1630(7, 0)
        arrayRegFiles(__tmp_1629 + 1.U) := __tmp_1630(15, 8)
        arrayRegFiles(__tmp_1629 + 2.U) := __tmp_1630(23, 16)
        arrayRegFiles(__tmp_1629 + 3.U) := __tmp_1630(31, 24)
        arrayRegFiles(__tmp_1629 + 4.U) := __tmp_1630(39, 32)
        arrayRegFiles(__tmp_1629 + 5.U) := __tmp_1630(47, 40)
        arrayRegFiles(__tmp_1629 + 6.U) := __tmp_1630(55, 48)
        arrayRegFiles(__tmp_1629 + 7.U) := __tmp_1630(63, 56)

        val __tmp_1631 = SP - 56.U
        val __tmp_1632 = (generalRegFiles(0.U)).asUInt
        arrayRegFiles(__tmp_1631 + 0.U) := __tmp_1632(7, 0)
        arrayRegFiles(__tmp_1631 + 1.U) := __tmp_1632(15, 8)
        arrayRegFiles(__tmp_1631 + 2.U) := __tmp_1632(23, 16)
        arrayRegFiles(__tmp_1631 + 3.U) := __tmp_1632(31, 24)
        arrayRegFiles(__tmp_1631 + 4.U) := __tmp_1632(39, 32)
        arrayRegFiles(__tmp_1631 + 5.U) := __tmp_1632(47, 40)
        arrayRegFiles(__tmp_1631 + 6.U) := __tmp_1632(55, 48)
        arrayRegFiles(__tmp_1631 + 7.U) := __tmp_1632(63, 56)

        val __tmp_1633 = SP - 48.U
        val __tmp_1634 = (generalRegFiles(1.U)).asUInt
        arrayRegFiles(__tmp_1633 + 0.U) := __tmp_1634(7, 0)
        arrayRegFiles(__tmp_1633 + 1.U) := __tmp_1634(15, 8)
        arrayRegFiles(__tmp_1633 + 2.U) := __tmp_1634(23, 16)
        arrayRegFiles(__tmp_1633 + 3.U) := __tmp_1634(31, 24)
        arrayRegFiles(__tmp_1633 + 4.U) := __tmp_1634(39, 32)
        arrayRegFiles(__tmp_1633 + 5.U) := __tmp_1634(47, 40)
        arrayRegFiles(__tmp_1633 + 6.U) := __tmp_1634(55, 48)
        arrayRegFiles(__tmp_1633 + 7.U) := __tmp_1634(63, 56)

        val __tmp_1635 = SP - 40.U
        val __tmp_1636 = (generalRegFiles(2.U)).asUInt
        arrayRegFiles(__tmp_1635 + 0.U) := __tmp_1636(7, 0)
        arrayRegFiles(__tmp_1635 + 1.U) := __tmp_1636(15, 8)
        arrayRegFiles(__tmp_1635 + 2.U) := __tmp_1636(23, 16)
        arrayRegFiles(__tmp_1635 + 3.U) := __tmp_1636(31, 24)
        arrayRegFiles(__tmp_1635 + 4.U) := __tmp_1636(39, 32)
        arrayRegFiles(__tmp_1635 + 5.U) := __tmp_1636(47, 40)
        arrayRegFiles(__tmp_1635 + 6.U) := __tmp_1636(55, 48)
        arrayRegFiles(__tmp_1635 + 7.U) := __tmp_1636(63, 56)

        val __tmp_1637 = SP - 32.U
        val __tmp_1638 = (generalRegFiles(3.U)).asUInt
        arrayRegFiles(__tmp_1637 + 0.U) := __tmp_1638(7, 0)
        arrayRegFiles(__tmp_1637 + 1.U) := __tmp_1638(15, 8)
        arrayRegFiles(__tmp_1637 + 2.U) := __tmp_1638(23, 16)
        arrayRegFiles(__tmp_1637 + 3.U) := __tmp_1638(31, 24)
        arrayRegFiles(__tmp_1637 + 4.U) := __tmp_1638(39, 32)
        arrayRegFiles(__tmp_1637 + 5.U) := __tmp_1638(47, 40)
        arrayRegFiles(__tmp_1637 + 6.U) := __tmp_1638(55, 48)
        arrayRegFiles(__tmp_1637 + 7.U) := __tmp_1638(63, 56)

        val __tmp_1639 = SP - 24.U
        val __tmp_1640 = (generalRegFiles(4.U)).asUInt
        arrayRegFiles(__tmp_1639 + 0.U) := __tmp_1640(7, 0)
        arrayRegFiles(__tmp_1639 + 1.U) := __tmp_1640(15, 8)
        arrayRegFiles(__tmp_1639 + 2.U) := __tmp_1640(23, 16)
        arrayRegFiles(__tmp_1639 + 3.U) := __tmp_1640(31, 24)
        arrayRegFiles(__tmp_1639 + 4.U) := __tmp_1640(39, 32)
        arrayRegFiles(__tmp_1639 + 5.U) := __tmp_1640(47, 40)
        arrayRegFiles(__tmp_1639 + 6.U) := __tmp_1640(55, 48)
        arrayRegFiles(__tmp_1639 + 7.U) := __tmp_1640(63, 56)

        val __tmp_1641 = SP - 16.U
        val __tmp_1642 = (generalRegFiles(5.U)).asUInt
        arrayRegFiles(__tmp_1641 + 0.U) := __tmp_1642(7, 0)
        arrayRegFiles(__tmp_1641 + 1.U) := __tmp_1642(15, 8)
        arrayRegFiles(__tmp_1641 + 2.U) := __tmp_1642(23, 16)
        arrayRegFiles(__tmp_1641 + 3.U) := __tmp_1642(31, 24)
        arrayRegFiles(__tmp_1641 + 4.U) := __tmp_1642(39, 32)
        arrayRegFiles(__tmp_1641 + 5.U) := __tmp_1642(47, 40)
        arrayRegFiles(__tmp_1641 + 6.U) := __tmp_1642(55, 48)
        arrayRegFiles(__tmp_1641 + 7.U) := __tmp_1642(63, 56)

        val __tmp_1643 = SP - 8.U
        val __tmp_1644 = (generalRegFiles(6.U)).asUInt
        arrayRegFiles(__tmp_1643 + 0.U) := __tmp_1644(7, 0)
        arrayRegFiles(__tmp_1643 + 1.U) := __tmp_1644(15, 8)
        arrayRegFiles(__tmp_1643 + 2.U) := __tmp_1644(23, 16)
        arrayRegFiles(__tmp_1643 + 3.U) := __tmp_1644(31, 24)
        arrayRegFiles(__tmp_1643 + 4.U) := __tmp_1644(39, 32)
        arrayRegFiles(__tmp_1643 + 5.U) := __tmp_1644(47, 40)
        arrayRegFiles(__tmp_1643 + 6.U) := __tmp_1644(55, 48)
        arrayRegFiles(__tmp_1643 + 7.U) := __tmp_1644(63, 56)

        CP := 25.U
      }

      is(25.U) {
        /*
        decl t: S16 [@19, 2]
        $1 = *(SP + 1) [unsigned, MS[Z, S16], 2]  // $1 = a
        $0 = *(SP + 3) [signed, Z, 8]  // $0 = i
        goto .26
        */


        val __tmp_1645 = (SP + 1.U).asUInt
        generalRegFiles(1.U) := Cat(
          arrayRegFiles(__tmp_1645 + 1.U),
          arrayRegFiles(__tmp_1645 + 0.U)
        ).asUInt

        val __tmp_1646 = (SP + 3.U).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_1646 + 7.U),
          arrayRegFiles(__tmp_1646 + 6.U),
          arrayRegFiles(__tmp_1646 + 5.U),
          arrayRegFiles(__tmp_1646 + 4.U),
          arrayRegFiles(__tmp_1646 + 3.U),
          arrayRegFiles(__tmp_1646 + 2.U),
          arrayRegFiles(__tmp_1646 + 1.U),
          arrayRegFiles(__tmp_1646 + 0.U)
        ).asUInt

        CP := 26.U
      }

      is(26.U) {
        /*
        if ((0 <= $0) & ($0 <= *($1 + 4))) goto .27 else goto .1
        */


        CP := Mux(((0.S <= generalRegFiles(0.U).asSInt).asUInt & (generalRegFiles(0.U).asSInt <= Cat(
                            arrayRegFiles(generalRegFiles(1.U) + 4.U + 7.U),
                            arrayRegFiles(generalRegFiles(1.U) + 4.U + 6.U),
                            arrayRegFiles(generalRegFiles(1.U) + 4.U + 5.U),
                            arrayRegFiles(generalRegFiles(1.U) + 4.U + 4.U),
                            arrayRegFiles(generalRegFiles(1.U) + 4.U + 3.U),
                            arrayRegFiles(generalRegFiles(1.U) + 4.U + 2.U),
                            arrayRegFiles(generalRegFiles(1.U) + 4.U + 1.U),
                            arrayRegFiles(generalRegFiles(1.U) + 4.U + 0.U)
                          ).asSInt).asUInt.asUInt) === 1.U, 27.U, 1.U)
      }

      is(27.U) {
        /*
        $2 = *(($1 + 12) + (($0 as SP) * 2)) [signed, S16, 2]  // $2 = $1($0)
        goto .28
        */


        val __tmp_1647 = (generalRegFiles(1.U) + 12.U + generalRegFiles(0.U).asSInt.asUInt * 2.U).asUInt
        generalRegFiles(2.U) := Cat(
          arrayRegFiles(__tmp_1647 + 1.U),
          arrayRegFiles(__tmp_1647 + 0.U)
        ).asUInt

        CP := 28.U
      }

      is(28.U) {
        /*
        *(SP + 19) = $2 [signed, S16, 2]  // t = $2
        $1 = *(SP + 1) [unsigned, MS[Z, S16], 2]  // $1 = a
        $0 = *(SP + 3) [signed, Z, 8]  // $0 = i
        $2 = *(SP + 1) [unsigned, MS[Z, S16], 2]  // $2 = a
        $3 = *(SP + 11) [signed, Z, 8]  // $3 = j
        goto .29
        */


        val __tmp_1648 = SP + 19.U
        val __tmp_1649 = (generalRegFiles(2.U)).asUInt
        arrayRegFiles(__tmp_1648 + 0.U) := __tmp_1649(7, 0)
        arrayRegFiles(__tmp_1648 + 1.U) := __tmp_1649(15, 8)

        val __tmp_1650 = (SP + 1.U).asUInt
        generalRegFiles(1.U) := Cat(
          arrayRegFiles(__tmp_1650 + 1.U),
          arrayRegFiles(__tmp_1650 + 0.U)
        ).asUInt

        val __tmp_1651 = (SP + 3.U).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_1651 + 7.U),
          arrayRegFiles(__tmp_1651 + 6.U),
          arrayRegFiles(__tmp_1651 + 5.U),
          arrayRegFiles(__tmp_1651 + 4.U),
          arrayRegFiles(__tmp_1651 + 3.U),
          arrayRegFiles(__tmp_1651 + 2.U),
          arrayRegFiles(__tmp_1651 + 1.U),
          arrayRegFiles(__tmp_1651 + 0.U)
        ).asUInt

        val __tmp_1652 = (SP + 1.U).asUInt
        generalRegFiles(2.U) := Cat(
          arrayRegFiles(__tmp_1652 + 1.U),
          arrayRegFiles(__tmp_1652 + 0.U)
        ).asUInt

        val __tmp_1653 = (SP + 11.U).asUInt
        generalRegFiles(3.U) := Cat(
          arrayRegFiles(__tmp_1653 + 7.U),
          arrayRegFiles(__tmp_1653 + 6.U),
          arrayRegFiles(__tmp_1653 + 5.U),
          arrayRegFiles(__tmp_1653 + 4.U),
          arrayRegFiles(__tmp_1653 + 3.U),
          arrayRegFiles(__tmp_1653 + 2.U),
          arrayRegFiles(__tmp_1653 + 1.U),
          arrayRegFiles(__tmp_1653 + 0.U)
        ).asUInt

        CP := 29.U
      }

      is(29.U) {
        /*
        if ((0 <= $3) & ($3 <= *($2 + 4))) goto .30 else goto .1
        */


        CP := Mux(((0.S <= generalRegFiles(3.U).asSInt).asUInt & (generalRegFiles(3.U).asSInt <= Cat(
                            arrayRegFiles(generalRegFiles(2.U) + 4.U + 7.U),
                            arrayRegFiles(generalRegFiles(2.U) + 4.U + 6.U),
                            arrayRegFiles(generalRegFiles(2.U) + 4.U + 5.U),
                            arrayRegFiles(generalRegFiles(2.U) + 4.U + 4.U),
                            arrayRegFiles(generalRegFiles(2.U) + 4.U + 3.U),
                            arrayRegFiles(generalRegFiles(2.U) + 4.U + 2.U),
                            arrayRegFiles(generalRegFiles(2.U) + 4.U + 1.U),
                            arrayRegFiles(generalRegFiles(2.U) + 4.U + 0.U)
                          ).asSInt).asUInt.asUInt) === 1.U, 30.U, 1.U)
      }

      is(30.U) {
        /*
        $4 = *(($2 + 12) + (($3 as SP) * 2)) [signed, S16, 2]  // $4 = $2($3)
        if ((0 <= $0) & ($0 <= *($1 + 4))) goto .31 else goto .1
        */


        val __tmp_1654 = (generalRegFiles(2.U) + 12.U + generalRegFiles(3.U).asSInt.asUInt * 2.U).asUInt
        generalRegFiles(4.U) := Cat(
          arrayRegFiles(__tmp_1654 + 1.U),
          arrayRegFiles(__tmp_1654 + 0.U)
        ).asUInt

        CP := Mux(((0.S <= generalRegFiles(0.U).asSInt).asUInt & (generalRegFiles(0.U).asSInt <= Cat(
                            arrayRegFiles(generalRegFiles(1.U) + 4.U + 7.U),
                            arrayRegFiles(generalRegFiles(1.U) + 4.U + 6.U),
                            arrayRegFiles(generalRegFiles(1.U) + 4.U + 5.U),
                            arrayRegFiles(generalRegFiles(1.U) + 4.U + 4.U),
                            arrayRegFiles(generalRegFiles(1.U) + 4.U + 3.U),
                            arrayRegFiles(generalRegFiles(1.U) + 4.U + 2.U),
                            arrayRegFiles(generalRegFiles(1.U) + 4.U + 1.U),
                            arrayRegFiles(generalRegFiles(1.U) + 4.U + 0.U)
                          ).asSInt).asUInt.asUInt) === 1.U, 31.U, 1.U)
      }

      is(31.U) {
        /*
        *(($1 + 12) + (($0 as SP) * 2)) = $4 [signed, S16, 2]  // $1($0) = $4
        goto .32
        */


        val __tmp_1655 = generalRegFiles(1.U) + 12.U + generalRegFiles(0.U).asSInt.asUInt * 2.U
        val __tmp_1656 = (generalRegFiles(4.U)).asUInt
        arrayRegFiles(__tmp_1655 + 0.U) := __tmp_1656(7, 0)
        arrayRegFiles(__tmp_1655 + 1.U) := __tmp_1656(15, 8)

        CP := 32.U
      }

      is(32.U) {
        /*
        $1 = *(SP + 1) [unsigned, MS[Z, S16], 2]  // $1 = a
        $0 = *(SP + 11) [signed, Z, 8]  // $0 = j
        $2 = *(SP + 19) [signed, S16, 2]  // $2 = t
        goto .33
        */


        val __tmp_1657 = (SP + 1.U).asUInt
        generalRegFiles(1.U) := Cat(
          arrayRegFiles(__tmp_1657 + 1.U),
          arrayRegFiles(__tmp_1657 + 0.U)
        ).asUInt

        val __tmp_1658 = (SP + 11.U).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_1658 + 7.U),
          arrayRegFiles(__tmp_1658 + 6.U),
          arrayRegFiles(__tmp_1658 + 5.U),
          arrayRegFiles(__tmp_1658 + 4.U),
          arrayRegFiles(__tmp_1658 + 3.U),
          arrayRegFiles(__tmp_1658 + 2.U),
          arrayRegFiles(__tmp_1658 + 1.U),
          arrayRegFiles(__tmp_1658 + 0.U)
        ).asUInt

        val __tmp_1659 = (SP + 19.U).asUInt
        generalRegFiles(2.U) := Cat(
          arrayRegFiles(__tmp_1659 + 1.U),
          arrayRegFiles(__tmp_1659 + 0.U)
        ).asUInt

        CP := 33.U
      }

      is(33.U) {
        /*
        if ((0 <= $0) & ($0 <= *($1 + 4))) goto .34 else goto .1
        */


        CP := Mux(((0.S <= generalRegFiles(0.U).asSInt).asUInt & (generalRegFiles(0.U).asSInt <= Cat(
                            arrayRegFiles(generalRegFiles(1.U) + 4.U + 7.U),
                            arrayRegFiles(generalRegFiles(1.U) + 4.U + 6.U),
                            arrayRegFiles(generalRegFiles(1.U) + 4.U + 5.U),
                            arrayRegFiles(generalRegFiles(1.U) + 4.U + 4.U),
                            arrayRegFiles(generalRegFiles(1.U) + 4.U + 3.U),
                            arrayRegFiles(generalRegFiles(1.U) + 4.U + 2.U),
                            arrayRegFiles(generalRegFiles(1.U) + 4.U + 1.U),
                            arrayRegFiles(generalRegFiles(1.U) + 4.U + 0.U)
                          ).asSInt).asUInt.asUInt) === 1.U, 34.U, 1.U)
      }

      is(34.U) {
        /*
        *(($1 + 12) + (($0 as SP) * 2)) = $2 [signed, S16, 2]  // $1($0) = $2
        undecl t: S16 [@19, 2]
        goto $ret@0
        */


        val __tmp_1660 = generalRegFiles(1.U) + 12.U + generalRegFiles(0.U).asSInt.asUInt * 2.U
        val __tmp_1661 = (generalRegFiles(2.U).asSInt).asUInt
        arrayRegFiles(__tmp_1660 + 0.U) := __tmp_1661(7, 0)
        arrayRegFiles(__tmp_1660 + 1.U) := __tmp_1661(15, 8)

        CP := Cat(
          arrayRegFiles(SP + 0.U + 0.U)
        )

      }

      is(39.U) {
        /*
        $1 = *(SP + 223) [signed, Z, 8]  // $1 = j
        goto .40
        */


        val __tmp_1662 = (SP + 223.U).asUInt
        generalRegFiles(1.U) := Cat(
          arrayRegFiles(__tmp_1662 + 7.U),
          arrayRegFiles(__tmp_1662 + 6.U),
          arrayRegFiles(__tmp_1662 + 5.U),
          arrayRegFiles(__tmp_1662 + 4.U),
          arrayRegFiles(__tmp_1662 + 3.U),
          arrayRegFiles(__tmp_1662 + 2.U),
          arrayRegFiles(__tmp_1662 + 1.U),
          arrayRegFiles(__tmp_1662 + 0.U)
        ).asUInt

        CP := 40.U
      }

      is(40.U) {
        /*
        $0 = ($1 + 1)
        goto .41
        */


        generalRegFiles(0.U) := (generalRegFiles(1.U).asSInt + 1.S).asUInt
        CP := 41.U
      }

      is(41.U) {
        /*
        *(SP + 223) = $0 [signed, Z, 8]  // j = $0
        goto .12
        */


        val __tmp_1663 = SP + 223.U
        val __tmp_1664 = (generalRegFiles(0.U).asSInt).asUInt
        arrayRegFiles(__tmp_1663 + 0.U) := __tmp_1664(7, 0)
        arrayRegFiles(__tmp_1663 + 1.U) := __tmp_1664(15, 8)
        arrayRegFiles(__tmp_1663 + 2.U) := __tmp_1664(23, 16)
        arrayRegFiles(__tmp_1663 + 3.U) := __tmp_1664(31, 24)
        arrayRegFiles(__tmp_1663 + 4.U) := __tmp_1664(39, 32)
        arrayRegFiles(__tmp_1663 + 5.U) := __tmp_1664(47, 40)
        arrayRegFiles(__tmp_1663 + 6.U) := __tmp_1664(55, 48)
        arrayRegFiles(__tmp_1663 + 7.U) := __tmp_1664(63, 56)

        CP := 12.U
      }

      is(44.U) {
        /*
        $1 = *(SP + 215) [signed, Z, 8]  // $1 = i
        goto .45
        */


        val __tmp_1665 = (SP + 215.U).asUInt
        generalRegFiles(1.U) := Cat(
          arrayRegFiles(__tmp_1665 + 7.U),
          arrayRegFiles(__tmp_1665 + 6.U),
          arrayRegFiles(__tmp_1665 + 5.U),
          arrayRegFiles(__tmp_1665 + 4.U),
          arrayRegFiles(__tmp_1665 + 3.U),
          arrayRegFiles(__tmp_1665 + 2.U),
          arrayRegFiles(__tmp_1665 + 1.U),
          arrayRegFiles(__tmp_1665 + 0.U)
        ).asUInt

        CP := 45.U
      }

      is(45.U) {
        /*
        $0 = ($1 + 1)
        goto .46
        */


        generalRegFiles(0.U) := (generalRegFiles(1.U).asSInt + 1.S).asUInt
        CP := 46.U
      }

      is(46.U) {
        /*
        *(SP + 215) = $0 [signed, Z, 8]  // i = $0
        goto .47
        */


        val __tmp_1666 = SP + 215.U
        val __tmp_1667 = (generalRegFiles(0.U).asSInt).asUInt
        arrayRegFiles(__tmp_1666 + 0.U) := __tmp_1667(7, 0)
        arrayRegFiles(__tmp_1666 + 1.U) := __tmp_1667(15, 8)
        arrayRegFiles(__tmp_1666 + 2.U) := __tmp_1667(23, 16)
        arrayRegFiles(__tmp_1666 + 3.U) := __tmp_1667(31, 24)
        arrayRegFiles(__tmp_1666 + 4.U) := __tmp_1667(39, 32)
        arrayRegFiles(__tmp_1666 + 5.U) := __tmp_1667(47, 40)
        arrayRegFiles(__tmp_1666 + 6.U) := __tmp_1667(55, 48)
        arrayRegFiles(__tmp_1666 + 7.U) := __tmp_1667(63, 56)

        CP := 47.U
      }

      is(47.U) {
        /*
        undecl j: Z [@223, 8]
        goto .5
        */


        CP := 5.U
      }

      is(48.U) {
        /*
        undecl i: Z [@215, 8]
        goto $ret@0
        */


        CP := Cat(
          arrayRegFiles(SP + 0.U + 0.U)
        )

      }

      is(49.U) {
        /*
        $0 = *(SP - 56) [unsigned, U64, 8]  // restore $0
        $1 = *(SP - 48) [unsigned, U64, 8]  // restore $1
        $2 = *(SP - 40) [unsigned, U64, 8]  // restore $2
        $3 = *(SP - 32) [unsigned, U64, 8]  // restore $3
        $4 = *(SP - 24) [unsigned, U64, 8]  // restore $4
        $5 = *(SP - 16) [unsigned, U64, 8]  // restore $5
        $6 = *(SP - 8) [unsigned, U64, 8]  // restore $6
        undecl j: Z [@11, 8], i: Z [@3, 8], a: MS[Z, S16] [@1, 2], $ret: CP [@0, 1]
        SP = SP - 287
        goto .39
        */


        val __tmp_1668 = (SP - 56.U).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_1668 + 7.U),
          arrayRegFiles(__tmp_1668 + 6.U),
          arrayRegFiles(__tmp_1668 + 5.U),
          arrayRegFiles(__tmp_1668 + 4.U),
          arrayRegFiles(__tmp_1668 + 3.U),
          arrayRegFiles(__tmp_1668 + 2.U),
          arrayRegFiles(__tmp_1668 + 1.U),
          arrayRegFiles(__tmp_1668 + 0.U)
        ).asUInt

        val __tmp_1669 = (SP - 48.U).asUInt
        generalRegFiles(1.U) := Cat(
          arrayRegFiles(__tmp_1669 + 7.U),
          arrayRegFiles(__tmp_1669 + 6.U),
          arrayRegFiles(__tmp_1669 + 5.U),
          arrayRegFiles(__tmp_1669 + 4.U),
          arrayRegFiles(__tmp_1669 + 3.U),
          arrayRegFiles(__tmp_1669 + 2.U),
          arrayRegFiles(__tmp_1669 + 1.U),
          arrayRegFiles(__tmp_1669 + 0.U)
        ).asUInt

        val __tmp_1670 = (SP - 40.U).asUInt
        generalRegFiles(2.U) := Cat(
          arrayRegFiles(__tmp_1670 + 7.U),
          arrayRegFiles(__tmp_1670 + 6.U),
          arrayRegFiles(__tmp_1670 + 5.U),
          arrayRegFiles(__tmp_1670 + 4.U),
          arrayRegFiles(__tmp_1670 + 3.U),
          arrayRegFiles(__tmp_1670 + 2.U),
          arrayRegFiles(__tmp_1670 + 1.U),
          arrayRegFiles(__tmp_1670 + 0.U)
        ).asUInt

        val __tmp_1671 = (SP - 32.U).asUInt
        generalRegFiles(3.U) := Cat(
          arrayRegFiles(__tmp_1671 + 7.U),
          arrayRegFiles(__tmp_1671 + 6.U),
          arrayRegFiles(__tmp_1671 + 5.U),
          arrayRegFiles(__tmp_1671 + 4.U),
          arrayRegFiles(__tmp_1671 + 3.U),
          arrayRegFiles(__tmp_1671 + 2.U),
          arrayRegFiles(__tmp_1671 + 1.U),
          arrayRegFiles(__tmp_1671 + 0.U)
        ).asUInt

        val __tmp_1672 = (SP - 24.U).asUInt
        generalRegFiles(4.U) := Cat(
          arrayRegFiles(__tmp_1672 + 7.U),
          arrayRegFiles(__tmp_1672 + 6.U),
          arrayRegFiles(__tmp_1672 + 5.U),
          arrayRegFiles(__tmp_1672 + 4.U),
          arrayRegFiles(__tmp_1672 + 3.U),
          arrayRegFiles(__tmp_1672 + 2.U),
          arrayRegFiles(__tmp_1672 + 1.U),
          arrayRegFiles(__tmp_1672 + 0.U)
        ).asUInt

        val __tmp_1673 = (SP - 16.U).asUInt
        generalRegFiles(5.U) := Cat(
          arrayRegFiles(__tmp_1673 + 7.U),
          arrayRegFiles(__tmp_1673 + 6.U),
          arrayRegFiles(__tmp_1673 + 5.U),
          arrayRegFiles(__tmp_1673 + 4.U),
          arrayRegFiles(__tmp_1673 + 3.U),
          arrayRegFiles(__tmp_1673 + 2.U),
          arrayRegFiles(__tmp_1673 + 1.U),
          arrayRegFiles(__tmp_1673 + 0.U)
        ).asUInt

        val __tmp_1674 = (SP - 8.U).asUInt
        generalRegFiles(6.U) := Cat(
          arrayRegFiles(__tmp_1674 + 7.U),
          arrayRegFiles(__tmp_1674 + 6.U),
          arrayRegFiles(__tmp_1674 + 5.U),
          arrayRegFiles(__tmp_1674 + 4.U),
          arrayRegFiles(__tmp_1674 + 3.U),
          arrayRegFiles(__tmp_1674 + 2.U),
          arrayRegFiles(__tmp_1674 + 1.U),
          arrayRegFiles(__tmp_1674 + 0.U)
        ).asUInt

        SP := SP - 287.U

        CP := 39.U
      }

    }

}
