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

        val __tmp_8 = 0.U
        val __tmp_9 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_8 + 0.U) := __tmp_9(7, 0)

        val __tmp_10 = 1.U
        val __tmp_11 = (3.U(16.W)).asUInt
        arrayRegFiles(__tmp_10 + 0.U) := __tmp_11(7, 0)
        arrayRegFiles(__tmp_10 + 1.U) := __tmp_11(15, 8)

        CP := 4.U
      }

      is(4.U) {
        /*
        decl i: Z [@215, 8]
        *(SP + 215) = 0 [signed, Z, 8]  // i = 0
        goto .5
        */


        val __tmp_12 = SP + 215.U
        val __tmp_13 = (0.S(64.W)).asUInt
        arrayRegFiles(__tmp_12 + 0.U) := __tmp_13(7, 0)
        arrayRegFiles(__tmp_12 + 1.U) := __tmp_13(15, 8)
        arrayRegFiles(__tmp_12 + 2.U) := __tmp_13(23, 16)
        arrayRegFiles(__tmp_12 + 3.U) := __tmp_13(31, 24)
        arrayRegFiles(__tmp_12 + 4.U) := __tmp_13(39, 32)
        arrayRegFiles(__tmp_12 + 5.U) := __tmp_13(47, 40)
        arrayRegFiles(__tmp_12 + 6.U) := __tmp_13(55, 48)
        arrayRegFiles(__tmp_12 + 7.U) := __tmp_13(63, 56)

        CP := 5.U
      }

      is(5.U) {
        /*
        $1 = *(SP + 215) [signed, Z, 8]  // $1 = i
        $0 = *(SP + 1) [unsigned, MS[Z, S16], 2]  // $0 = a
        goto .6
        */


        val __tmp_14 = (SP + 215.U).asUInt
        generalRegFiles(1.U) := Cat(
          arrayRegFiles(__tmp_14 + 7.U),
          arrayRegFiles(__tmp_14 + 6.U),
          arrayRegFiles(__tmp_14 + 5.U),
          arrayRegFiles(__tmp_14 + 4.U),
          arrayRegFiles(__tmp_14 + 3.U),
          arrayRegFiles(__tmp_14 + 2.U),
          arrayRegFiles(__tmp_14 + 1.U),
          arrayRegFiles(__tmp_14 + 0.U)
        ).asUInt

        val __tmp_15 = (SP + 1.U).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_15 + 1.U),
          arrayRegFiles(__tmp_15 + 0.U)
        ).asUInt

        CP := 6.U
      }

      is(6.U) {
        /*
        $2 = *($0 + 4) [signed, Z, 8]  // $2 = $0.size
        goto .7
        */


        val __tmp_16 = (generalRegFiles(0.U) + 4.U).asUInt
        generalRegFiles(2.U) := Cat(
          arrayRegFiles(__tmp_16 + 7.U),
          arrayRegFiles(__tmp_16 + 6.U),
          arrayRegFiles(__tmp_16 + 5.U),
          arrayRegFiles(__tmp_16 + 4.U),
          arrayRegFiles(__tmp_16 + 3.U),
          arrayRegFiles(__tmp_16 + 2.U),
          arrayRegFiles(__tmp_16 + 1.U),
          arrayRegFiles(__tmp_16 + 0.U)
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


        val __tmp_17 = (SP + 215.U).asUInt
        generalRegFiles(1.U) := Cat(
          arrayRegFiles(__tmp_17 + 7.U),
          arrayRegFiles(__tmp_17 + 6.U),
          arrayRegFiles(__tmp_17 + 5.U),
          arrayRegFiles(__tmp_17 + 4.U),
          arrayRegFiles(__tmp_17 + 3.U),
          arrayRegFiles(__tmp_17 + 2.U),
          arrayRegFiles(__tmp_17 + 1.U),
          arrayRegFiles(__tmp_17 + 0.U)
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


        val __tmp_18 = SP + 223.U
        val __tmp_19 = (generalRegFiles(0.U).asSInt).asUInt
        arrayRegFiles(__tmp_18 + 0.U) := __tmp_19(7, 0)
        arrayRegFiles(__tmp_18 + 1.U) := __tmp_19(15, 8)
        arrayRegFiles(__tmp_18 + 2.U) := __tmp_19(23, 16)
        arrayRegFiles(__tmp_18 + 3.U) := __tmp_19(31, 24)
        arrayRegFiles(__tmp_18 + 4.U) := __tmp_19(39, 32)
        arrayRegFiles(__tmp_18 + 5.U) := __tmp_19(47, 40)
        arrayRegFiles(__tmp_18 + 6.U) := __tmp_19(55, 48)
        arrayRegFiles(__tmp_18 + 7.U) := __tmp_19(63, 56)

        CP := 12.U
      }

      is(12.U) {
        /*
        $1 = *(SP + 223) [signed, Z, 8]  // $1 = j
        $0 = *(SP + 1) [unsigned, MS[Z, S16], 2]  // $0 = a
        goto .13
        */


        val __tmp_20 = (SP + 223.U).asUInt
        generalRegFiles(1.U) := Cat(
          arrayRegFiles(__tmp_20 + 7.U),
          arrayRegFiles(__tmp_20 + 6.U),
          arrayRegFiles(__tmp_20 + 5.U),
          arrayRegFiles(__tmp_20 + 4.U),
          arrayRegFiles(__tmp_20 + 3.U),
          arrayRegFiles(__tmp_20 + 2.U),
          arrayRegFiles(__tmp_20 + 1.U),
          arrayRegFiles(__tmp_20 + 0.U)
        ).asUInt

        val __tmp_21 = (SP + 1.U).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_21 + 1.U),
          arrayRegFiles(__tmp_21 + 0.U)
        ).asUInt

        CP := 13.U
      }

      is(13.U) {
        /*
        $2 = *($0 + 4) [signed, Z, 8]  // $2 = $0.size
        goto .14
        */


        val __tmp_22 = (generalRegFiles(0.U) + 4.U).asUInt
        generalRegFiles(2.U) := Cat(
          arrayRegFiles(__tmp_22 + 7.U),
          arrayRegFiles(__tmp_22 + 6.U),
          arrayRegFiles(__tmp_22 + 5.U),
          arrayRegFiles(__tmp_22 + 4.U),
          arrayRegFiles(__tmp_22 + 3.U),
          arrayRegFiles(__tmp_22 + 2.U),
          arrayRegFiles(__tmp_22 + 1.U),
          arrayRegFiles(__tmp_22 + 0.U)
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


        val __tmp_23 = (SP + 1.U).asUInt
        generalRegFiles(1.U) := Cat(
          arrayRegFiles(__tmp_23 + 1.U),
          arrayRegFiles(__tmp_23 + 0.U)
        ).asUInt

        val __tmp_24 = (SP + 215.U).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_24 + 7.U),
          arrayRegFiles(__tmp_24 + 6.U),
          arrayRegFiles(__tmp_24 + 5.U),
          arrayRegFiles(__tmp_24 + 4.U),
          arrayRegFiles(__tmp_24 + 3.U),
          arrayRegFiles(__tmp_24 + 2.U),
          arrayRegFiles(__tmp_24 + 1.U),
          arrayRegFiles(__tmp_24 + 0.U)
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


        val __tmp_25 = (generalRegFiles(1.U) + 12.U + generalRegFiles(0.U).asSInt.asUInt * 2.U).asUInt
        generalRegFiles(2.U) := Cat(
          arrayRegFiles(__tmp_25 + 1.U),
          arrayRegFiles(__tmp_25 + 0.U)
        ).asUInt

        val __tmp_26 = (SP + 1.U).asUInt
        generalRegFiles(3.U) := Cat(
          arrayRegFiles(__tmp_26 + 1.U),
          arrayRegFiles(__tmp_26 + 0.U)
        ).asUInt

        val __tmp_27 = (SP + 223.U).asUInt
        generalRegFiles(4.U) := Cat(
          arrayRegFiles(__tmp_27 + 7.U),
          arrayRegFiles(__tmp_27 + 6.U),
          arrayRegFiles(__tmp_27 + 5.U),
          arrayRegFiles(__tmp_27 + 4.U),
          arrayRegFiles(__tmp_27 + 3.U),
          arrayRegFiles(__tmp_27 + 2.U),
          arrayRegFiles(__tmp_27 + 1.U),
          arrayRegFiles(__tmp_27 + 0.U)
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


        val __tmp_28 = (generalRegFiles(3.U) + 12.U + generalRegFiles(4.U).asSInt.asUInt * 2.U).asUInt
        generalRegFiles(5.U) := Cat(
          arrayRegFiles(__tmp_28 + 1.U),
          arrayRegFiles(__tmp_28 + 0.U)
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


        val __tmp_29 = (SP + 1.U).asUInt
        generalRegFiles(1.U) := Cat(
          arrayRegFiles(__tmp_29 + 1.U),
          arrayRegFiles(__tmp_29 + 0.U)
        ).asUInt

        val __tmp_30 = (SP + 215.U).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_30 + 7.U),
          arrayRegFiles(__tmp_30 + 6.U),
          arrayRegFiles(__tmp_30 + 5.U),
          arrayRegFiles(__tmp_30 + 4.U),
          arrayRegFiles(__tmp_30 + 3.U),
          arrayRegFiles(__tmp_30 + 2.U),
          arrayRegFiles(__tmp_30 + 1.U),
          arrayRegFiles(__tmp_30 + 0.U)
        ).asUInt

        val __tmp_31 = (SP + 223.U).asUInt
        generalRegFiles(2.U) := Cat(
          arrayRegFiles(__tmp_31 + 7.U),
          arrayRegFiles(__tmp_31 + 6.U),
          arrayRegFiles(__tmp_31 + 5.U),
          arrayRegFiles(__tmp_31 + 4.U),
          arrayRegFiles(__tmp_31 + 3.U),
          arrayRegFiles(__tmp_31 + 2.U),
          arrayRegFiles(__tmp_31 + 1.U),
          arrayRegFiles(__tmp_31 + 0.U)
        ).asUInt

        SP := SP + 287.U

        CP := 24.U
      }

      is(24.U) {
        /*
        *SP = 49 [signed, CP, 1]  // $ret@0 = 50
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


        val __tmp_32 = SP
        val __tmp_33 = (49.S(8.W)).asUInt
        arrayRegFiles(__tmp_32 + 0.U) := __tmp_33(7, 0)

        val __tmp_34 = SP + 1.U
        val __tmp_35 = (generalRegFiles(1.U)).asUInt
        arrayRegFiles(__tmp_34 + 0.U) := __tmp_35(7, 0)
        arrayRegFiles(__tmp_34 + 1.U) := __tmp_35(15, 8)

        val __tmp_36 = SP + 3.U
        val __tmp_37 = (generalRegFiles(0.U).asSInt).asUInt
        arrayRegFiles(__tmp_36 + 0.U) := __tmp_37(7, 0)
        arrayRegFiles(__tmp_36 + 1.U) := __tmp_37(15, 8)
        arrayRegFiles(__tmp_36 + 2.U) := __tmp_37(23, 16)
        arrayRegFiles(__tmp_36 + 3.U) := __tmp_37(31, 24)
        arrayRegFiles(__tmp_36 + 4.U) := __tmp_37(39, 32)
        arrayRegFiles(__tmp_36 + 5.U) := __tmp_37(47, 40)
        arrayRegFiles(__tmp_36 + 6.U) := __tmp_37(55, 48)
        arrayRegFiles(__tmp_36 + 7.U) := __tmp_37(63, 56)

        val __tmp_38 = SP + 11.U
        val __tmp_39 = (generalRegFiles(2.U).asSInt).asUInt
        arrayRegFiles(__tmp_38 + 0.U) := __tmp_39(7, 0)
        arrayRegFiles(__tmp_38 + 1.U) := __tmp_39(15, 8)
        arrayRegFiles(__tmp_38 + 2.U) := __tmp_39(23, 16)
        arrayRegFiles(__tmp_38 + 3.U) := __tmp_39(31, 24)
        arrayRegFiles(__tmp_38 + 4.U) := __tmp_39(39, 32)
        arrayRegFiles(__tmp_38 + 5.U) := __tmp_39(47, 40)
        arrayRegFiles(__tmp_38 + 6.U) := __tmp_39(55, 48)
        arrayRegFiles(__tmp_38 + 7.U) := __tmp_39(63, 56)

        val __tmp_40 = SP - 56.U
        val __tmp_41 = (generalRegFiles(0.U)).asUInt
        arrayRegFiles(__tmp_40 + 0.U) := __tmp_41(7, 0)
        arrayRegFiles(__tmp_40 + 1.U) := __tmp_41(15, 8)
        arrayRegFiles(__tmp_40 + 2.U) := __tmp_41(23, 16)
        arrayRegFiles(__tmp_40 + 3.U) := __tmp_41(31, 24)
        arrayRegFiles(__tmp_40 + 4.U) := __tmp_41(39, 32)
        arrayRegFiles(__tmp_40 + 5.U) := __tmp_41(47, 40)
        arrayRegFiles(__tmp_40 + 6.U) := __tmp_41(55, 48)
        arrayRegFiles(__tmp_40 + 7.U) := __tmp_41(63, 56)

        val __tmp_42 = SP - 48.U
        val __tmp_43 = (generalRegFiles(1.U)).asUInt
        arrayRegFiles(__tmp_42 + 0.U) := __tmp_43(7, 0)
        arrayRegFiles(__tmp_42 + 1.U) := __tmp_43(15, 8)
        arrayRegFiles(__tmp_42 + 2.U) := __tmp_43(23, 16)
        arrayRegFiles(__tmp_42 + 3.U) := __tmp_43(31, 24)
        arrayRegFiles(__tmp_42 + 4.U) := __tmp_43(39, 32)
        arrayRegFiles(__tmp_42 + 5.U) := __tmp_43(47, 40)
        arrayRegFiles(__tmp_42 + 6.U) := __tmp_43(55, 48)
        arrayRegFiles(__tmp_42 + 7.U) := __tmp_43(63, 56)

        val __tmp_44 = SP - 40.U
        val __tmp_45 = (generalRegFiles(2.U)).asUInt
        arrayRegFiles(__tmp_44 + 0.U) := __tmp_45(7, 0)
        arrayRegFiles(__tmp_44 + 1.U) := __tmp_45(15, 8)
        arrayRegFiles(__tmp_44 + 2.U) := __tmp_45(23, 16)
        arrayRegFiles(__tmp_44 + 3.U) := __tmp_45(31, 24)
        arrayRegFiles(__tmp_44 + 4.U) := __tmp_45(39, 32)
        arrayRegFiles(__tmp_44 + 5.U) := __tmp_45(47, 40)
        arrayRegFiles(__tmp_44 + 6.U) := __tmp_45(55, 48)
        arrayRegFiles(__tmp_44 + 7.U) := __tmp_45(63, 56)

        val __tmp_46 = SP - 32.U
        val __tmp_47 = (generalRegFiles(3.U)).asUInt
        arrayRegFiles(__tmp_46 + 0.U) := __tmp_47(7, 0)
        arrayRegFiles(__tmp_46 + 1.U) := __tmp_47(15, 8)
        arrayRegFiles(__tmp_46 + 2.U) := __tmp_47(23, 16)
        arrayRegFiles(__tmp_46 + 3.U) := __tmp_47(31, 24)
        arrayRegFiles(__tmp_46 + 4.U) := __tmp_47(39, 32)
        arrayRegFiles(__tmp_46 + 5.U) := __tmp_47(47, 40)
        arrayRegFiles(__tmp_46 + 6.U) := __tmp_47(55, 48)
        arrayRegFiles(__tmp_46 + 7.U) := __tmp_47(63, 56)

        val __tmp_48 = SP - 24.U
        val __tmp_49 = (generalRegFiles(4.U)).asUInt
        arrayRegFiles(__tmp_48 + 0.U) := __tmp_49(7, 0)
        arrayRegFiles(__tmp_48 + 1.U) := __tmp_49(15, 8)
        arrayRegFiles(__tmp_48 + 2.U) := __tmp_49(23, 16)
        arrayRegFiles(__tmp_48 + 3.U) := __tmp_49(31, 24)
        arrayRegFiles(__tmp_48 + 4.U) := __tmp_49(39, 32)
        arrayRegFiles(__tmp_48 + 5.U) := __tmp_49(47, 40)
        arrayRegFiles(__tmp_48 + 6.U) := __tmp_49(55, 48)
        arrayRegFiles(__tmp_48 + 7.U) := __tmp_49(63, 56)

        val __tmp_50 = SP - 16.U
        val __tmp_51 = (generalRegFiles(5.U)).asUInt
        arrayRegFiles(__tmp_50 + 0.U) := __tmp_51(7, 0)
        arrayRegFiles(__tmp_50 + 1.U) := __tmp_51(15, 8)
        arrayRegFiles(__tmp_50 + 2.U) := __tmp_51(23, 16)
        arrayRegFiles(__tmp_50 + 3.U) := __tmp_51(31, 24)
        arrayRegFiles(__tmp_50 + 4.U) := __tmp_51(39, 32)
        arrayRegFiles(__tmp_50 + 5.U) := __tmp_51(47, 40)
        arrayRegFiles(__tmp_50 + 6.U) := __tmp_51(55, 48)
        arrayRegFiles(__tmp_50 + 7.U) := __tmp_51(63, 56)

        val __tmp_52 = SP - 8.U
        val __tmp_53 = (generalRegFiles(6.U)).asUInt
        arrayRegFiles(__tmp_52 + 0.U) := __tmp_53(7, 0)
        arrayRegFiles(__tmp_52 + 1.U) := __tmp_53(15, 8)
        arrayRegFiles(__tmp_52 + 2.U) := __tmp_53(23, 16)
        arrayRegFiles(__tmp_52 + 3.U) := __tmp_53(31, 24)
        arrayRegFiles(__tmp_52 + 4.U) := __tmp_53(39, 32)
        arrayRegFiles(__tmp_52 + 5.U) := __tmp_53(47, 40)
        arrayRegFiles(__tmp_52 + 6.U) := __tmp_53(55, 48)
        arrayRegFiles(__tmp_52 + 7.U) := __tmp_53(63, 56)

        CP := 25.U
      }

      is(25.U) {
        /*
        decl t: S16 [@19, 2]
        $1 = *(SP + 1) [unsigned, MS[Z, S16], 2]  // $1 = a
        $0 = *(SP + 3) [signed, Z, 8]  // $0 = i
        goto .26
        */


        val __tmp_54 = (SP + 1.U).asUInt
        generalRegFiles(1.U) := Cat(
          arrayRegFiles(__tmp_54 + 1.U),
          arrayRegFiles(__tmp_54 + 0.U)
        ).asUInt

        val __tmp_55 = (SP + 3.U).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_55 + 7.U),
          arrayRegFiles(__tmp_55 + 6.U),
          arrayRegFiles(__tmp_55 + 5.U),
          arrayRegFiles(__tmp_55 + 4.U),
          arrayRegFiles(__tmp_55 + 3.U),
          arrayRegFiles(__tmp_55 + 2.U),
          arrayRegFiles(__tmp_55 + 1.U),
          arrayRegFiles(__tmp_55 + 0.U)
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


        val __tmp_56 = (generalRegFiles(1.U) + 12.U + generalRegFiles(0.U).asSInt.asUInt * 2.U).asUInt
        generalRegFiles(2.U) := Cat(
          arrayRegFiles(__tmp_56 + 1.U),
          arrayRegFiles(__tmp_56 + 0.U)
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


        val __tmp_57 = SP + 19.U
        val __tmp_58 = (generalRegFiles(2.U)).asUInt
        arrayRegFiles(__tmp_57 + 0.U) := __tmp_58(7, 0)
        arrayRegFiles(__tmp_57 + 1.U) := __tmp_58(15, 8)

        val __tmp_59 = (SP + 1.U).asUInt
        generalRegFiles(1.U) := Cat(
          arrayRegFiles(__tmp_59 + 1.U),
          arrayRegFiles(__tmp_59 + 0.U)
        ).asUInt

        val __tmp_60 = (SP + 3.U).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_60 + 7.U),
          arrayRegFiles(__tmp_60 + 6.U),
          arrayRegFiles(__tmp_60 + 5.U),
          arrayRegFiles(__tmp_60 + 4.U),
          arrayRegFiles(__tmp_60 + 3.U),
          arrayRegFiles(__tmp_60 + 2.U),
          arrayRegFiles(__tmp_60 + 1.U),
          arrayRegFiles(__tmp_60 + 0.U)
        ).asUInt

        val __tmp_61 = (SP + 1.U).asUInt
        generalRegFiles(2.U) := Cat(
          arrayRegFiles(__tmp_61 + 1.U),
          arrayRegFiles(__tmp_61 + 0.U)
        ).asUInt

        val __tmp_62 = (SP + 11.U).asUInt
        generalRegFiles(3.U) := Cat(
          arrayRegFiles(__tmp_62 + 7.U),
          arrayRegFiles(__tmp_62 + 6.U),
          arrayRegFiles(__tmp_62 + 5.U),
          arrayRegFiles(__tmp_62 + 4.U),
          arrayRegFiles(__tmp_62 + 3.U),
          arrayRegFiles(__tmp_62 + 2.U),
          arrayRegFiles(__tmp_62 + 1.U),
          arrayRegFiles(__tmp_62 + 0.U)
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


        val __tmp_63 = (generalRegFiles(2.U) + 12.U + generalRegFiles(3.U).asSInt.asUInt * 2.U).asUInt
        generalRegFiles(4.U) := Cat(
          arrayRegFiles(__tmp_63 + 1.U),
          arrayRegFiles(__tmp_63 + 0.U)
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


        val __tmp_64 = generalRegFiles(1.U) + 12.U + generalRegFiles(0.U).asSInt.asUInt * 2.U
        val __tmp_65 = (generalRegFiles(4.U)).asUInt
        arrayRegFiles(__tmp_64 + 0.U) := __tmp_65(7, 0)
        arrayRegFiles(__tmp_64 + 1.U) := __tmp_65(15, 8)

        CP := 32.U
      }

      is(32.U) {
        /*
        $1 = *(SP + 1) [unsigned, MS[Z, S16], 2]  // $1 = a
        $0 = *(SP + 11) [signed, Z, 8]  // $0 = j
        $2 = *(SP + 19) [signed, S16, 2]  // $2 = t
        goto .33
        */


        val __tmp_66 = (SP + 1.U).asUInt
        generalRegFiles(1.U) := Cat(
          arrayRegFiles(__tmp_66 + 1.U),
          arrayRegFiles(__tmp_66 + 0.U)
        ).asUInt

        val __tmp_67 = (SP + 11.U).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_67 + 7.U),
          arrayRegFiles(__tmp_67 + 6.U),
          arrayRegFiles(__tmp_67 + 5.U),
          arrayRegFiles(__tmp_67 + 4.U),
          arrayRegFiles(__tmp_67 + 3.U),
          arrayRegFiles(__tmp_67 + 2.U),
          arrayRegFiles(__tmp_67 + 1.U),
          arrayRegFiles(__tmp_67 + 0.U)
        ).asUInt

        val __tmp_68 = (SP + 19.U).asUInt
        generalRegFiles(2.U) := Cat(
          arrayRegFiles(__tmp_68 + 1.U),
          arrayRegFiles(__tmp_68 + 0.U)
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


        val __tmp_69 = generalRegFiles(1.U) + 12.U + generalRegFiles(0.U).asSInt.asUInt * 2.U
        val __tmp_70 = (generalRegFiles(2.U).asSInt).asUInt
        arrayRegFiles(__tmp_69 + 0.U) := __tmp_70(7, 0)
        arrayRegFiles(__tmp_69 + 1.U) := __tmp_70(15, 8)

        CP := Cat(
          arrayRegFiles(SP + 0.U + 0.U)
        )

      }

      is(39.U) {
        /*
        $1 = *(SP + 223) [signed, Z, 8]  // $1 = j
        goto .40
        */


        val __tmp_71 = (SP + 223.U).asUInt
        generalRegFiles(1.U) := Cat(
          arrayRegFiles(__tmp_71 + 7.U),
          arrayRegFiles(__tmp_71 + 6.U),
          arrayRegFiles(__tmp_71 + 5.U),
          arrayRegFiles(__tmp_71 + 4.U),
          arrayRegFiles(__tmp_71 + 3.U),
          arrayRegFiles(__tmp_71 + 2.U),
          arrayRegFiles(__tmp_71 + 1.U),
          arrayRegFiles(__tmp_71 + 0.U)
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


        val __tmp_72 = SP + 223.U
        val __tmp_73 = (generalRegFiles(0.U).asSInt).asUInt
        arrayRegFiles(__tmp_72 + 0.U) := __tmp_73(7, 0)
        arrayRegFiles(__tmp_72 + 1.U) := __tmp_73(15, 8)
        arrayRegFiles(__tmp_72 + 2.U) := __tmp_73(23, 16)
        arrayRegFiles(__tmp_72 + 3.U) := __tmp_73(31, 24)
        arrayRegFiles(__tmp_72 + 4.U) := __tmp_73(39, 32)
        arrayRegFiles(__tmp_72 + 5.U) := __tmp_73(47, 40)
        arrayRegFiles(__tmp_72 + 6.U) := __tmp_73(55, 48)
        arrayRegFiles(__tmp_72 + 7.U) := __tmp_73(63, 56)

        CP := 12.U
      }

      is(44.U) {
        /*
        $1 = *(SP + 215) [signed, Z, 8]  // $1 = i
        goto .45
        */


        val __tmp_74 = (SP + 215.U).asUInt
        generalRegFiles(1.U) := Cat(
          arrayRegFiles(__tmp_74 + 7.U),
          arrayRegFiles(__tmp_74 + 6.U),
          arrayRegFiles(__tmp_74 + 5.U),
          arrayRegFiles(__tmp_74 + 4.U),
          arrayRegFiles(__tmp_74 + 3.U),
          arrayRegFiles(__tmp_74 + 2.U),
          arrayRegFiles(__tmp_74 + 1.U),
          arrayRegFiles(__tmp_74 + 0.U)
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


        val __tmp_75 = SP + 215.U
        val __tmp_76 = (generalRegFiles(0.U).asSInt).asUInt
        arrayRegFiles(__tmp_75 + 0.U) := __tmp_76(7, 0)
        arrayRegFiles(__tmp_75 + 1.U) := __tmp_76(15, 8)
        arrayRegFiles(__tmp_75 + 2.U) := __tmp_76(23, 16)
        arrayRegFiles(__tmp_75 + 3.U) := __tmp_76(31, 24)
        arrayRegFiles(__tmp_75 + 4.U) := __tmp_76(39, 32)
        arrayRegFiles(__tmp_75 + 5.U) := __tmp_76(47, 40)
        arrayRegFiles(__tmp_75 + 6.U) := __tmp_76(55, 48)
        arrayRegFiles(__tmp_75 + 7.U) := __tmp_76(63, 56)

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


        val __tmp_77 = (SP - 56.U).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_77 + 7.U),
          arrayRegFiles(__tmp_77 + 6.U),
          arrayRegFiles(__tmp_77 + 5.U),
          arrayRegFiles(__tmp_77 + 4.U),
          arrayRegFiles(__tmp_77 + 3.U),
          arrayRegFiles(__tmp_77 + 2.U),
          arrayRegFiles(__tmp_77 + 1.U),
          arrayRegFiles(__tmp_77 + 0.U)
        ).asUInt

        val __tmp_78 = (SP - 48.U).asUInt
        generalRegFiles(1.U) := Cat(
          arrayRegFiles(__tmp_78 + 7.U),
          arrayRegFiles(__tmp_78 + 6.U),
          arrayRegFiles(__tmp_78 + 5.U),
          arrayRegFiles(__tmp_78 + 4.U),
          arrayRegFiles(__tmp_78 + 3.U),
          arrayRegFiles(__tmp_78 + 2.U),
          arrayRegFiles(__tmp_78 + 1.U),
          arrayRegFiles(__tmp_78 + 0.U)
        ).asUInt

        val __tmp_79 = (SP - 40.U).asUInt
        generalRegFiles(2.U) := Cat(
          arrayRegFiles(__tmp_79 + 7.U),
          arrayRegFiles(__tmp_79 + 6.U),
          arrayRegFiles(__tmp_79 + 5.U),
          arrayRegFiles(__tmp_79 + 4.U),
          arrayRegFiles(__tmp_79 + 3.U),
          arrayRegFiles(__tmp_79 + 2.U),
          arrayRegFiles(__tmp_79 + 1.U),
          arrayRegFiles(__tmp_79 + 0.U)
        ).asUInt

        val __tmp_80 = (SP - 32.U).asUInt
        generalRegFiles(3.U) := Cat(
          arrayRegFiles(__tmp_80 + 7.U),
          arrayRegFiles(__tmp_80 + 6.U),
          arrayRegFiles(__tmp_80 + 5.U),
          arrayRegFiles(__tmp_80 + 4.U),
          arrayRegFiles(__tmp_80 + 3.U),
          arrayRegFiles(__tmp_80 + 2.U),
          arrayRegFiles(__tmp_80 + 1.U),
          arrayRegFiles(__tmp_80 + 0.U)
        ).asUInt

        val __tmp_81 = (SP - 24.U).asUInt
        generalRegFiles(4.U) := Cat(
          arrayRegFiles(__tmp_81 + 7.U),
          arrayRegFiles(__tmp_81 + 6.U),
          arrayRegFiles(__tmp_81 + 5.U),
          arrayRegFiles(__tmp_81 + 4.U),
          arrayRegFiles(__tmp_81 + 3.U),
          arrayRegFiles(__tmp_81 + 2.U),
          arrayRegFiles(__tmp_81 + 1.U),
          arrayRegFiles(__tmp_81 + 0.U)
        ).asUInt

        val __tmp_82 = (SP - 16.U).asUInt
        generalRegFiles(5.U) := Cat(
          arrayRegFiles(__tmp_82 + 7.U),
          arrayRegFiles(__tmp_82 + 6.U),
          arrayRegFiles(__tmp_82 + 5.U),
          arrayRegFiles(__tmp_82 + 4.U),
          arrayRegFiles(__tmp_82 + 3.U),
          arrayRegFiles(__tmp_82 + 2.U),
          arrayRegFiles(__tmp_82 + 1.U),
          arrayRegFiles(__tmp_82 + 0.U)
        ).asUInt

        val __tmp_83 = (SP - 8.U).asUInt
        generalRegFiles(6.U) := Cat(
          arrayRegFiles(__tmp_83 + 7.U),
          arrayRegFiles(__tmp_83 + 6.U),
          arrayRegFiles(__tmp_83 + 5.U),
          arrayRegFiles(__tmp_83 + 4.U),
          arrayRegFiles(__tmp_83 + 3.U),
          arrayRegFiles(__tmp_83 + 2.U),
          arrayRegFiles(__tmp_83 + 1.U),
          arrayRegFiles(__tmp_83 + 0.U)
        ).asUInt

        SP := SP - 287.U

        CP := 39.U
      }

    }

}
