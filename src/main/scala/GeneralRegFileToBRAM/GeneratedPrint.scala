package GeneralRegFileToBRAM

import chisel3._
import chisel3.util._
import chisel3.experimental._



class PrintTestTest (val C_S_AXI_DATA_WIDTH:  Int = 32,
               val C_S_AXI_ADDR_WIDTH:  Int = 32,
               val ARRAY_REG_WIDTH:     Int = 8,
               val ARRAY_REG_DEPTH:     Int = 344,
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
    val arrayRegFiles = Reg(Vec(1 << log2Ceil(ARRAY_REG_DEPTH), UInt(ARRAY_REG_WIDTH.W)))
    // reg for general purpose
    val generalRegFilesU8 = Reg(Vec(3, UInt(8.W)))
    val generalRegFilesS64 = Reg(Vec(6, SInt(64.W)))
    val generalRegFilesU16 = Reg(Vec(7, UInt(16.W)))
    val generalRegFilesU64 = Reg(Vec(11, UInt(64.W)))
    val generalRegFilesS8 = Reg(Vec(5, SInt(8.W)))
    val generalRegFilesU1 = Reg(Vec(3, UInt(1.W)))
    val generalRegFilesU32 = Reg(Vec(7, UInt(32.W)))
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
        goto .5
        */


        SP := 84.U(16.W)

        DP := 0.U(64.W)

        val __tmp_4002 = 10.U(32.W)
        val __tmp_4003 = (886747591.U(32.W)).asUInt
        arrayRegFiles(__tmp_4002 + 0.U) := __tmp_4003(7, 0)
        arrayRegFiles(__tmp_4002 + 1.U) := __tmp_4003(15, 8)
        arrayRegFiles(__tmp_4002 + 2.U) := __tmp_4003(23, 16)
        arrayRegFiles(__tmp_4002 + 3.U) := __tmp_4003(31, 24)

        val __tmp_4004 = 14.U(16.W)
        val __tmp_4005 = (64.S(64.W)).asUInt
        arrayRegFiles(__tmp_4004 + 0.U) := __tmp_4005(7, 0)
        arrayRegFiles(__tmp_4004 + 1.U) := __tmp_4005(15, 8)
        arrayRegFiles(__tmp_4004 + 2.U) := __tmp_4005(23, 16)
        arrayRegFiles(__tmp_4004 + 3.U) := __tmp_4005(31, 24)
        arrayRegFiles(__tmp_4004 + 4.U) := __tmp_4005(39, 32)
        arrayRegFiles(__tmp_4004 + 5.U) := __tmp_4005(47, 40)
        arrayRegFiles(__tmp_4004 + 6.U) := __tmp_4005(55, 48)
        arrayRegFiles(__tmp_4004 + 7.U) := __tmp_4005(63, 56)

        val __tmp_4006 = 84.U(16.W)
        val __tmp_4007 = (0.U(16.W)).asUInt
        arrayRegFiles(__tmp_4006 + 0.U) := __tmp_4007(7, 0)
        arrayRegFiles(__tmp_4006 + 1.U) := __tmp_4007(15, 8)

        CP := 5.U
      }

      is(5.U) {
        /*
        $64S.0 = *(0: SP) [signed, Z, 8]  // $64S.0 = $testNum
        goto .6
        */


        val __tmp_4008 = (0.U(16.W)).asUInt
        generalRegFilesS64(0.U) := Cat(
          arrayRegFiles(__tmp_4008 + 7.U),
          arrayRegFiles(__tmp_4008 + 6.U),
          arrayRegFiles(__tmp_4008 + 5.U),
          arrayRegFiles(__tmp_4008 + 4.U),
          arrayRegFiles(__tmp_4008 + 3.U),
          arrayRegFiles(__tmp_4008 + 2.U),
          arrayRegFiles(__tmp_4008 + 1.U),
          arrayRegFiles(__tmp_4008 + 0.U)
        ).asSInt.pad(64)

        CP := 6.U
      }

      is(6.U) {
        /*
        if (($64S.0 < (0: Z)) | ($64S.0 ≡ (0: Z))) goto .7 else goto .11
        */


        CP := Mux((((generalRegFilesS64(0.U) < 0.S(64.W)).asUInt | (generalRegFilesS64(0.U) === 0.S(64.W)).asUInt).asUInt) === 1.U, 7.U, 11.U)
      }

      is(7.U) {
        /*
        SP = SP + 2
        goto .8
        */


        SP := SP + 2.U

        CP := 8.U
      }

      is(8.U) {
        /*
        decl $ret: CP [@0, 2]
        *SP = (9: CP) [unsigned, CP, 2]  // $ret@0 = 1364
        goto .12
        */


        val __tmp_4009 = SP
        val __tmp_4010 = (9.U(16.W)).asUInt
        arrayRegFiles(__tmp_4009 + 0.U) := __tmp_4010(7, 0)
        arrayRegFiles(__tmp_4009 + 1.U) := __tmp_4010(15, 8)

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
        decl $ret: CP [@0, 2]
        *SP = (14: CP) [unsigned, CP, 2]  // $ret@0 = 1365
        goto .17
        */


        val __tmp_4011 = SP
        val __tmp_4012 = (14.U(16.W)).asUInt
        arrayRegFiles(__tmp_4011 + 0.U) := __tmp_4012(7, 0)
        arrayRegFiles(__tmp_4011 + 1.U) := __tmp_4012(15, 8)

        CP := 17.U
      }

      is(14.U) {
        /*
        undecl $ret: CP [@0, 2]
        goto .15
        */


        CP := 15.U
      }

      is(15.U) {
        /*
        SP = SP - 2
        goto .16
        */


        SP := SP - 2.U

        CP := 16.U
      }

      is(16.U) {
        /*
        goto $ret@0
        */


        CP := Cat(
          arrayRegFiles(SP + 0.U + 1.U),
          arrayRegFiles(SP + 0.U + 0.U)
        )

      }

      is(17.U) {
        /*
        *(((8: SP) + (12: SP)) + ((DP & (63: DP)) as SP)) = (72: U8) [unsigned, U8, 1]  // $display((DP & (63: DP))) = (72: U8)
        *(((8: SP) + (12: SP)) + (((DP + (1: DP)) & (63: DP)) as SP)) = (101: U8) [unsigned, U8, 1]  // $display(((DP + (1: DP)) & (63: DP))) = (101: U8)
        *(((8: SP) + (12: SP)) + (((DP + (2: DP)) & (63: DP)) as SP)) = (108: U8) [unsigned, U8, 1]  // $display(((DP + (2: DP)) & (63: DP))) = (108: U8)
        *(((8: SP) + (12: SP)) + (((DP + (3: DP)) & (63: DP)) as SP)) = (108: U8) [unsigned, U8, 1]  // $display(((DP + (3: DP)) & (63: DP))) = (108: U8)
        *(((8: SP) + (12: SP)) + (((DP + (4: DP)) & (63: DP)) as SP)) = (111: U8) [unsigned, U8, 1]  // $display(((DP + (4: DP)) & (63: DP))) = (111: U8)
        *(((8: SP) + (12: SP)) + (((DP + (5: DP)) & (63: DP)) as SP)) = (32: U8) [unsigned, U8, 1]  // $display(((DP + (5: DP)) & (63: DP))) = (32: U8)
        *(((8: SP) + (12: SP)) + (((DP + (6: DP)) & (63: DP)) as SP)) = (119: U8) [unsigned, U8, 1]  // $display(((DP + (6: DP)) & (63: DP))) = (119: U8)
        *(((8: SP) + (12: SP)) + (((DP + (7: DP)) & (63: DP)) as SP)) = (111: U8) [unsigned, U8, 1]  // $display(((DP + (7: DP)) & (63: DP))) = (111: U8)
        *(((8: SP) + (12: SP)) + (((DP + (8: DP)) & (63: DP)) as SP)) = (114: U8) [unsigned, U8, 1]  // $display(((DP + (8: DP)) & (63: DP))) = (114: U8)
        *(((8: SP) + (12: SP)) + (((DP + (9: DP)) & (63: DP)) as SP)) = (108: U8) [unsigned, U8, 1]  // $display(((DP + (9: DP)) & (63: DP))) = (108: U8)
        *(((8: SP) + (12: SP)) + (((DP + (10: DP)) & (63: DP)) as SP)) = (100: U8) [unsigned, U8, 1]  // $display(((DP + (10: DP)) & (63: DP))) = (100: U8)
        *(((8: SP) + (12: SP)) + (((DP + (11: DP)) & (63: DP)) as SP)) = (33: U8) [unsigned, U8, 1]  // $display(((DP + (11: DP)) & (63: DP))) = (33: U8)
        goto .18
        */


        val __tmp_4013 = ((8.U(16.W) + 12.U(16.W)) + (DP & 63.U(64.W)).asUInt.pad(16))
        val __tmp_4014 = (72.U(8.W)).asUInt
        arrayRegFiles(__tmp_4013 + 0.U) := __tmp_4014(7, 0)

        val __tmp_4015 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 1.U(64.W)) & 63.U(64.W)).asUInt.pad(16))
        val __tmp_4016 = (101.U(8.W)).asUInt
        arrayRegFiles(__tmp_4015 + 0.U) := __tmp_4016(7, 0)

        val __tmp_4017 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 2.U(64.W)) & 63.U(64.W)).asUInt.pad(16))
        val __tmp_4018 = (108.U(8.W)).asUInt
        arrayRegFiles(__tmp_4017 + 0.U) := __tmp_4018(7, 0)

        val __tmp_4019 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 3.U(64.W)) & 63.U(64.W)).asUInt.pad(16))
        val __tmp_4020 = (108.U(8.W)).asUInt
        arrayRegFiles(__tmp_4019 + 0.U) := __tmp_4020(7, 0)

        val __tmp_4021 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 4.U(64.W)) & 63.U(64.W)).asUInt.pad(16))
        val __tmp_4022 = (111.U(8.W)).asUInt
        arrayRegFiles(__tmp_4021 + 0.U) := __tmp_4022(7, 0)

        val __tmp_4023 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 5.U(64.W)) & 63.U(64.W)).asUInt.pad(16))
        val __tmp_4024 = (32.U(8.W)).asUInt
        arrayRegFiles(__tmp_4023 + 0.U) := __tmp_4024(7, 0)

        val __tmp_4025 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 6.U(64.W)) & 63.U(64.W)).asUInt.pad(16))
        val __tmp_4026 = (119.U(8.W)).asUInt
        arrayRegFiles(__tmp_4025 + 0.U) := __tmp_4026(7, 0)

        val __tmp_4027 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 7.U(64.W)) & 63.U(64.W)).asUInt.pad(16))
        val __tmp_4028 = (111.U(8.W)).asUInt
        arrayRegFiles(__tmp_4027 + 0.U) := __tmp_4028(7, 0)

        val __tmp_4029 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 8.U(64.W)) & 63.U(64.W)).asUInt.pad(16))
        val __tmp_4030 = (114.U(8.W)).asUInt
        arrayRegFiles(__tmp_4029 + 0.U) := __tmp_4030(7, 0)

        val __tmp_4031 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 9.U(64.W)) & 63.U(64.W)).asUInt.pad(16))
        val __tmp_4032 = (108.U(8.W)).asUInt
        arrayRegFiles(__tmp_4031 + 0.U) := __tmp_4032(7, 0)

        val __tmp_4033 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 10.U(64.W)) & 63.U(64.W)).asUInt.pad(16))
        val __tmp_4034 = (100.U(8.W)).asUInt
        arrayRegFiles(__tmp_4033 + 0.U) := __tmp_4034(7, 0)

        val __tmp_4035 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 11.U(64.W)) & 63.U(64.W)).asUInt.pad(16))
        val __tmp_4036 = (33.U(8.W)).asUInt
        arrayRegFiles(__tmp_4035 + 0.U) := __tmp_4036(7, 0)

        CP := 18.U
      }

      is(18.U) {
        /*
        DP = DP + 12
        goto .19
        */


        DP := DP + 12.U

        CP := 19.U
      }

      is(19.U) {
        /*
        *(((8: SP) + (12: SP)) + ((DP & (63: DP)) as SP)) = (10: U8) [unsigned, U8, 1]  // $display((DP & (63: DP))) = (10: U8)
        goto .20
        */


        val __tmp_4037 = ((8.U(16.W) + 12.U(16.W)) + (DP & 63.U(64.W)).asUInt.pad(16))
        val __tmp_4038 = (10.U(8.W)).asUInt
        arrayRegFiles(__tmp_4037 + 0.U) := __tmp_4038(7, 0)

        CP := 20.U
      }

      is(20.U) {
        /*
        DP = DP + 1
        goto .21
        */


        DP := DP + 1.U

        CP := 21.U
      }

      is(21.U) {
        /*
        decl x: Z @$0
        goto .22
        */


        CP := 22.U
      }

      is(22.U) {
        /*
        $64S.0 = (5: Z)
        goto .23
        */



        generalRegFilesS64(0.U) := 5.S(64.W)

        CP := 23.U
      }

      is(23.U) {
        /*
        decl c: C @$0
        $32U.0 = (8801: C)
        goto .24
        */



        generalRegFilesU32(0.U) := 8801.U(32.W)

        CP := 24.U
      }

      is(24.U) {
        /*
        decl s: String [@2, 108]
        alloc $new@[8,11].FC07DDF8: IS[3, U8] [@110, 15]
        $16U.1 = (SP + (110: SP))
        *(SP + (110: SP)) = (3069765878: U32) [unsigned, U32, 4]  // sha3 type signature of String: 0xB6F8E8F6
        *(SP + (114: SP)) = (3: Z) [signed, Z, 8]  // size of "abc"
        goto .25
        */



        generalRegFilesU16(1.U) := (SP + 110.U(16.W))

        val __tmp_4039 = (SP + 110.U(16.W))
        val __tmp_4040 = (BigInt("3069765878").U(32.W)).asUInt
        arrayRegFiles(__tmp_4039 + 0.U) := __tmp_4040(7, 0)
        arrayRegFiles(__tmp_4039 + 1.U) := __tmp_4040(15, 8)
        arrayRegFiles(__tmp_4039 + 2.U) := __tmp_4040(23, 16)
        arrayRegFiles(__tmp_4039 + 3.U) := __tmp_4040(31, 24)

        val __tmp_4041 = (SP + 114.U(16.W))
        val __tmp_4042 = (3.S(64.W)).asUInt
        arrayRegFiles(__tmp_4041 + 0.U) := __tmp_4042(7, 0)
        arrayRegFiles(__tmp_4041 + 1.U) := __tmp_4042(15, 8)
        arrayRegFiles(__tmp_4041 + 2.U) := __tmp_4042(23, 16)
        arrayRegFiles(__tmp_4041 + 3.U) := __tmp_4042(31, 24)
        arrayRegFiles(__tmp_4041 + 4.U) := __tmp_4042(39, 32)
        arrayRegFiles(__tmp_4041 + 5.U) := __tmp_4042(47, 40)
        arrayRegFiles(__tmp_4041 + 6.U) := __tmp_4042(55, 48)
        arrayRegFiles(__tmp_4041 + 7.U) := __tmp_4042(63, 56)

        CP := 25.U
      }

      is(25.U) {
        /*
        *(($16U.1 + (12: SP)) + (0: SP)) = (97: U8) [unsigned, U8, 1]  // $16U.1((0: SP)) = (97: U8)
        *(($16U.1 + (12: SP)) + (1: SP)) = (98: U8) [unsigned, U8, 1]  // $16U.1((1: SP)) = (98: U8)
        *(($16U.1 + (12: SP)) + (2: SP)) = (99: U8) [unsigned, U8, 1]  // $16U.1((2: SP)) = (99: U8)
        goto .26
        */


        val __tmp_4043 = ((generalRegFilesU16(1.U) + 12.U(16.W)) + 0.U(16.W))
        val __tmp_4044 = (97.U(8.W)).asUInt
        arrayRegFiles(__tmp_4043 + 0.U) := __tmp_4044(7, 0)

        val __tmp_4045 = ((generalRegFilesU16(1.U) + 12.U(16.W)) + 1.U(16.W))
        val __tmp_4046 = (98.U(8.W)).asUInt
        arrayRegFiles(__tmp_4045 + 0.U) := __tmp_4046(7, 0)

        val __tmp_4047 = ((generalRegFilesU16(1.U) + 12.U(16.W)) + 2.U(16.W))
        val __tmp_4048 = (99.U(8.W)).asUInt
        arrayRegFiles(__tmp_4047 + 0.U) := __tmp_4048(7, 0)

        CP := 26.U
      }

      is(26.U) {
        /*
        (SP + (2: SP)) [String, 108]  <-  $16U.1 [IS[SP, U8], ((*($16U.1 + (4: SP)) as SP) + (12: SP))]  // s = $16U.1
        goto .27
        */


        val __tmp_4049 = (SP + 2.U(16.W))
        val __tmp_4050 = generalRegFilesU16(1.U)
        val __tmp_4051 = (Cat(
           arrayRegFiles((generalRegFilesU16(1.U) + 4.U(16.W)) + 7.U),
           arrayRegFiles((generalRegFilesU16(1.U) + 4.U(16.W)) + 6.U),
           arrayRegFiles((generalRegFilesU16(1.U) + 4.U(16.W)) + 5.U),
           arrayRegFiles((generalRegFilesU16(1.U) + 4.U(16.W)) + 4.U),
           arrayRegFiles((generalRegFilesU16(1.U) + 4.U(16.W)) + 3.U),
           arrayRegFiles((generalRegFilesU16(1.U) + 4.U(16.W)) + 2.U),
           arrayRegFiles((generalRegFilesU16(1.U) + 4.U(16.W)) + 1.U),
           arrayRegFiles((generalRegFilesU16(1.U) + 4.U(16.W)) + 0.U)
         ).asSInt.asUInt.pad(16) + 12.U(16.W))

        when(Idx <= __tmp_4051) {
          arrayRegFiles(__tmp_4049 + Idx + 0.U) := arrayRegFiles(__tmp_4050 + Idx + 0.U)
          arrayRegFiles(__tmp_4049 + Idx + 1.U) := arrayRegFiles(__tmp_4050 + Idx + 1.U)
          arrayRegFiles(__tmp_4049 + Idx + 2.U) := arrayRegFiles(__tmp_4050 + Idx + 2.U)
          arrayRegFiles(__tmp_4049 + Idx + 3.U) := arrayRegFiles(__tmp_4050 + Idx + 3.U)
          arrayRegFiles(__tmp_4049 + Idx + 4.U) := arrayRegFiles(__tmp_4050 + Idx + 4.U)
          arrayRegFiles(__tmp_4049 + Idx + 5.U) := arrayRegFiles(__tmp_4050 + Idx + 5.U)
          arrayRegFiles(__tmp_4049 + Idx + 6.U) := arrayRegFiles(__tmp_4050 + Idx + 6.U)
          arrayRegFiles(__tmp_4049 + Idx + 7.U) := arrayRegFiles(__tmp_4050 + Idx + 7.U)
          Idx := Idx + 8.U
          LeftByteRounds := __tmp_4051 - Idx
        } .elsewhen(IdxLeftByteRounds < LeftByteRounds) {
          val __tmp_4052 = Idx - 8.U
          arrayRegFiles(__tmp_4049 + __tmp_4052 + IdxLeftByteRounds) := arrayRegFiles(__tmp_4050 + __tmp_4052 + IdxLeftByteRounds)
          IdxLeftByteRounds := IdxLeftByteRounds + 1.U
        } .otherwise {
          Idx := 0.U
          IdxLeftByteRounds := 0.U
          LeftByteRounds := 0.U
          CP := 27.U
        }


      }

      is(27.U) {
        /*
        unalloc $new@[8,11].FC07DDF8: IS[3, U8] [@110, 15]
        goto .28
        */


        CP := 28.U
      }

      is(28.U) {
        /*
        $16U.1 = (SP + (2: SP))
        *(((8: SP) + (12: SP)) + ((DP & (63: DP)) as SP)) = (120: U8) [unsigned, U8, 1]  // $display((DP & (63: DP))) = (120: U8)
        *(((8: SP) + (12: SP)) + (((DP + (1: DP)) & (63: DP)) as SP)) = (32: U8) [unsigned, U8, 1]  // $display(((DP + (1: DP)) & (63: DP))) = (32: U8)
        *(((8: SP) + (12: SP)) + (((DP + (2: DP)) & (63: DP)) as SP)) = (61: U8) [unsigned, U8, 1]  // $display(((DP + (2: DP)) & (63: DP))) = (61: U8)
        *(((8: SP) + (12: SP)) + (((DP + (3: DP)) & (63: DP)) as SP)) = (32: U8) [unsigned, U8, 1]  // $display(((DP + (3: DP)) & (63: DP))) = (32: U8)
        goto .29
        */



        generalRegFilesU16(1.U) := (SP + 2.U(16.W))

        val __tmp_4053 = ((8.U(16.W) + 12.U(16.W)) + (DP & 63.U(64.W)).asUInt.pad(16))
        val __tmp_4054 = (120.U(8.W)).asUInt
        arrayRegFiles(__tmp_4053 + 0.U) := __tmp_4054(7, 0)

        val __tmp_4055 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 1.U(64.W)) & 63.U(64.W)).asUInt.pad(16))
        val __tmp_4056 = (32.U(8.W)).asUInt
        arrayRegFiles(__tmp_4055 + 0.U) := __tmp_4056(7, 0)

        val __tmp_4057 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 2.U(64.W)) & 63.U(64.W)).asUInt.pad(16))
        val __tmp_4058 = (61.U(8.W)).asUInt
        arrayRegFiles(__tmp_4057 + 0.U) := __tmp_4058(7, 0)

        val __tmp_4059 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 3.U(64.W)) & 63.U(64.W)).asUInt.pad(16))
        val __tmp_4060 = (32.U(8.W)).asUInt
        arrayRegFiles(__tmp_4059 + 0.U) := __tmp_4060(7, 0)

        CP := 29.U
      }

      is(29.U) {
        /*
        DP = DP + 4
        goto .30
        */


        DP := DP + 4.U

        CP := 30.U
      }

      is(30.U) {
        /*
        alloc printS64$res@[9,19].79E1A301: U64 [@110, 8]
        goto .31
        */


        CP := 31.U
      }

      is(31.U) {
        /*
        SP = SP + 140
        goto .32
        */


        SP := SP + 140.U

        CP := 32.U
      }

      is(32.U) {
        /*
        decl $ret: CP [@0, 2], $res: SP [@2, 2], buffer: SP [@4, 2], index: anvil.PrinterIndex.U [@6, 8], mask: anvil.PrinterIndex.U [@14, 8], n: S64 [@22, 8]
        *SP = (33: CP) [unsigned, CP, 2]  // $ret@0 = 1366
        *(SP + (2: SP)) = (SP - (30: SP)) [unsigned, SP, 2]  // $res@2 = -30
        *(SP + (4: SP)) = (8: SP) [unsigned, SP, 2]  // buffer = (8: SP)
        *(SP + (6: SP)) = DP [unsigned, anvil.PrinterIndex.U, 8]  // index = DP
        *(SP + (14: SP)) = (63: anvil.PrinterIndex.U) [unsigned, anvil.PrinterIndex.U, 8]  // mask = (63: anvil.PrinterIndex.U)
        *(SP + (22: SP)) = ($64S.0 as S64) [signed, S64, 8]  // n = ($64S.0 as S64)
        *(SP - (2: SP)) = $16U.1 [unsigned, SP, 2]  // save $1
        *(SP - (6: SP)) = $32U.0 [unsigned, C, 4]  // save $0
        goto .57
        */


        val __tmp_4061 = SP
        val __tmp_4062 = (33.U(16.W)).asUInt
        arrayRegFiles(__tmp_4061 + 0.U) := __tmp_4062(7, 0)
        arrayRegFiles(__tmp_4061 + 1.U) := __tmp_4062(15, 8)

        val __tmp_4063 = (SP + 2.U(16.W))
        val __tmp_4064 = ((SP - 30.U(16.W))).asUInt
        arrayRegFiles(__tmp_4063 + 0.U) := __tmp_4064(7, 0)
        arrayRegFiles(__tmp_4063 + 1.U) := __tmp_4064(15, 8)

        val __tmp_4065 = (SP + 4.U(16.W))
        val __tmp_4066 = (8.U(16.W)).asUInt
        arrayRegFiles(__tmp_4065 + 0.U) := __tmp_4066(7, 0)
        arrayRegFiles(__tmp_4065 + 1.U) := __tmp_4066(15, 8)

        val __tmp_4067 = (SP + 6.U(16.W))
        val __tmp_4068 = (DP).asUInt
        arrayRegFiles(__tmp_4067 + 0.U) := __tmp_4068(7, 0)
        arrayRegFiles(__tmp_4067 + 1.U) := __tmp_4068(15, 8)
        arrayRegFiles(__tmp_4067 + 2.U) := __tmp_4068(23, 16)
        arrayRegFiles(__tmp_4067 + 3.U) := __tmp_4068(31, 24)
        arrayRegFiles(__tmp_4067 + 4.U) := __tmp_4068(39, 32)
        arrayRegFiles(__tmp_4067 + 5.U) := __tmp_4068(47, 40)
        arrayRegFiles(__tmp_4067 + 6.U) := __tmp_4068(55, 48)
        arrayRegFiles(__tmp_4067 + 7.U) := __tmp_4068(63, 56)

        val __tmp_4069 = (SP + 14.U(16.W))
        val __tmp_4070 = (63.U(64.W)).asUInt
        arrayRegFiles(__tmp_4069 + 0.U) := __tmp_4070(7, 0)
        arrayRegFiles(__tmp_4069 + 1.U) := __tmp_4070(15, 8)
        arrayRegFiles(__tmp_4069 + 2.U) := __tmp_4070(23, 16)
        arrayRegFiles(__tmp_4069 + 3.U) := __tmp_4070(31, 24)
        arrayRegFiles(__tmp_4069 + 4.U) := __tmp_4070(39, 32)
        arrayRegFiles(__tmp_4069 + 5.U) := __tmp_4070(47, 40)
        arrayRegFiles(__tmp_4069 + 6.U) := __tmp_4070(55, 48)
        arrayRegFiles(__tmp_4069 + 7.U) := __tmp_4070(63, 56)

        val __tmp_4071 = (SP + 22.U(16.W))
        val __tmp_4072 = (generalRegFilesS64(0.U).asSInt).asUInt
        arrayRegFiles(__tmp_4071 + 0.U) := __tmp_4072(7, 0)
        arrayRegFiles(__tmp_4071 + 1.U) := __tmp_4072(15, 8)
        arrayRegFiles(__tmp_4071 + 2.U) := __tmp_4072(23, 16)
        arrayRegFiles(__tmp_4071 + 3.U) := __tmp_4072(31, 24)
        arrayRegFiles(__tmp_4071 + 4.U) := __tmp_4072(39, 32)
        arrayRegFiles(__tmp_4071 + 5.U) := __tmp_4072(47, 40)
        arrayRegFiles(__tmp_4071 + 6.U) := __tmp_4072(55, 48)
        arrayRegFiles(__tmp_4071 + 7.U) := __tmp_4072(63, 56)

        val __tmp_4073 = (SP - 2.U(16.W))
        val __tmp_4074 = (generalRegFilesU16(1.U)).asUInt
        arrayRegFiles(__tmp_4073 + 0.U) := __tmp_4074(7, 0)
        arrayRegFiles(__tmp_4073 + 1.U) := __tmp_4074(15, 8)

        val __tmp_4075 = (SP - 6.U(16.W))
        val __tmp_4076 = (generalRegFilesU32(0.U)).asUInt
        arrayRegFiles(__tmp_4075 + 0.U) := __tmp_4076(7, 0)
        arrayRegFiles(__tmp_4075 + 1.U) := __tmp_4076(15, 8)
        arrayRegFiles(__tmp_4075 + 2.U) := __tmp_4076(23, 16)
        arrayRegFiles(__tmp_4075 + 3.U) := __tmp_4076(31, 24)

        CP := 57.U
      }

      is(33.U) {
        /*
        $16U.1 = *(SP - (2: SP)) [unsigned, SP, 2]  // restore $1
        $32U.0 = *(SP - (6: SP)) [unsigned, C, 4]  // restore $0
        $64U.3 = **(SP + (2: SP)) [unsigned, U64, 8]  // $3 = $res
        undecl n: S64 [@22, 8], mask: anvil.PrinterIndex.U [@14, 8], index: anvil.PrinterIndex.U [@6, 8], buffer: SP [@4, 2], $res: SP [@2, 2], $ret: CP [@0, 2]
        goto .34
        */


        val __tmp_4077 = ((SP - 2.U(16.W))).asUInt
        generalRegFilesU16(1.U) := Cat(
          arrayRegFiles(__tmp_4077 + 1.U),
          arrayRegFiles(__tmp_4077 + 0.U)
        )

        val __tmp_4078 = ((SP - 6.U(16.W))).asUInt
        generalRegFilesU32(0.U) := Cat(
          arrayRegFiles(__tmp_4078 + 3.U),
          arrayRegFiles(__tmp_4078 + 2.U),
          arrayRegFiles(__tmp_4078 + 1.U),
          arrayRegFiles(__tmp_4078 + 0.U)
        )

        val __tmp_4079 = (Cat(
          arrayRegFiles((SP + 2.U(16.W)) + 1.U),
          arrayRegFiles((SP + 2.U(16.W)) + 0.U)
        )).asUInt
        generalRegFilesU64(3.U) := Cat(
          arrayRegFiles(__tmp_4079 + 7.U),
          arrayRegFiles(__tmp_4079 + 6.U),
          arrayRegFiles(__tmp_4079 + 5.U),
          arrayRegFiles(__tmp_4079 + 4.U),
          arrayRegFiles(__tmp_4079 + 3.U),
          arrayRegFiles(__tmp_4079 + 2.U),
          arrayRegFiles(__tmp_4079 + 1.U),
          arrayRegFiles(__tmp_4079 + 0.U)
        )

        CP := 34.U
      }

      is(34.U) {
        /*
        SP = SP - 140
        goto .35
        */


        SP := SP - 140.U

        CP := 35.U
      }

      is(35.U) {
        /*
        undecl x: Z @$0
        goto .36
        */


        CP := 36.U
      }

      is(36.U) {
        /*
        DP = DP + ($64U.3 as DP)
        goto .37
        */


        DP := DP + generalRegFilesU64(3.U).asUInt

        CP := 37.U
      }

      is(37.U) {
        /*
        *(((8: SP) + (12: SP)) + ((DP & (63: DP)) as SP)) = (44: U8) [unsigned, U8, 1]  // $display((DP & (63: DP))) = (44: U8)
        *(((8: SP) + (12: SP)) + (((DP + (1: DP)) & (63: DP)) as SP)) = (32: U8) [unsigned, U8, 1]  // $display(((DP + (1: DP)) & (63: DP))) = (32: U8)
        *(((8: SP) + (12: SP)) + (((DP + (2: DP)) & (63: DP)) as SP)) = (99: U8) [unsigned, U8, 1]  // $display(((DP + (2: DP)) & (63: DP))) = (99: U8)
        *(((8: SP) + (12: SP)) + (((DP + (3: DP)) & (63: DP)) as SP)) = (32: U8) [unsigned, U8, 1]  // $display(((DP + (3: DP)) & (63: DP))) = (32: U8)
        *(((8: SP) + (12: SP)) + (((DP + (4: DP)) & (63: DP)) as SP)) = (61: U8) [unsigned, U8, 1]  // $display(((DP + (4: DP)) & (63: DP))) = (61: U8)
        *(((8: SP) + (12: SP)) + (((DP + (5: DP)) & (63: DP)) as SP)) = (32: U8) [unsigned, U8, 1]  // $display(((DP + (5: DP)) & (63: DP))) = (32: U8)
        goto .38
        */


        val __tmp_4080 = ((8.U(16.W) + 12.U(16.W)) + (DP & 63.U(64.W)).asUInt.pad(16))
        val __tmp_4081 = (44.U(8.W)).asUInt
        arrayRegFiles(__tmp_4080 + 0.U) := __tmp_4081(7, 0)

        val __tmp_4082 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 1.U(64.W)) & 63.U(64.W)).asUInt.pad(16))
        val __tmp_4083 = (32.U(8.W)).asUInt
        arrayRegFiles(__tmp_4082 + 0.U) := __tmp_4083(7, 0)

        val __tmp_4084 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 2.U(64.W)) & 63.U(64.W)).asUInt.pad(16))
        val __tmp_4085 = (99.U(8.W)).asUInt
        arrayRegFiles(__tmp_4084 + 0.U) := __tmp_4085(7, 0)

        val __tmp_4086 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 3.U(64.W)) & 63.U(64.W)).asUInt.pad(16))
        val __tmp_4087 = (32.U(8.W)).asUInt
        arrayRegFiles(__tmp_4086 + 0.U) := __tmp_4087(7, 0)

        val __tmp_4088 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 4.U(64.W)) & 63.U(64.W)).asUInt.pad(16))
        val __tmp_4089 = (61.U(8.W)).asUInt
        arrayRegFiles(__tmp_4088 + 0.U) := __tmp_4089(7, 0)

        val __tmp_4090 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 5.U(64.W)) & 63.U(64.W)).asUInt.pad(16))
        val __tmp_4091 = (32.U(8.W)).asUInt
        arrayRegFiles(__tmp_4090 + 0.U) := __tmp_4091(7, 0)

        CP := 38.U
      }

      is(38.U) {
        /*
        DP = DP + 6
        goto .39
        */


        DP := DP + 6.U

        CP := 39.U
      }

      is(39.U) {
        /*
        alloc printC$res@[9,32].2BEEC4F6: U64 [@118, 8]
        goto .40
        */


        CP := 40.U
      }

      is(40.U) {
        /*
        SP = SP + 136
        goto .41
        */


        SP := SP + 136.U

        CP := 41.U
      }

      is(41.U) {
        /*
        decl $ret: CP [@0, 2], $res: SP [@2, 2], buffer: SP [@4, 2], index: anvil.PrinterIndex.U [@6, 8], mask: anvil.PrinterIndex.U [@14, 8], c: C [@22, 4]
        *SP = (42: CP) [unsigned, CP, 2]  // $ret@0 = 1367
        *(SP + (2: SP)) = (SP - (18: SP)) [unsigned, SP, 2]  // $res@2 = -18
        *(SP + (4: SP)) = (8: SP) [unsigned, SP, 2]  // buffer = (8: SP)
        *(SP + (6: SP)) = DP [unsigned, anvil.PrinterIndex.U, 8]  // index = DP
        *(SP + (14: SP)) = (63: anvil.PrinterIndex.U) [unsigned, anvil.PrinterIndex.U, 8]  // mask = (63: anvil.PrinterIndex.U)
        *(SP + (22: SP)) = $32U.0 [unsigned, C, 4]  // c = $32U.0
        *(SP - (2: SP)) = $16U.1 [unsigned, SP, 2]  // save $1
        goto .203
        */


        val __tmp_4092 = SP
        val __tmp_4093 = (42.U(16.W)).asUInt
        arrayRegFiles(__tmp_4092 + 0.U) := __tmp_4093(7, 0)
        arrayRegFiles(__tmp_4092 + 1.U) := __tmp_4093(15, 8)

        val __tmp_4094 = (SP + 2.U(16.W))
        val __tmp_4095 = ((SP - 18.U(16.W))).asUInt
        arrayRegFiles(__tmp_4094 + 0.U) := __tmp_4095(7, 0)
        arrayRegFiles(__tmp_4094 + 1.U) := __tmp_4095(15, 8)

        val __tmp_4096 = (SP + 4.U(16.W))
        val __tmp_4097 = (8.U(16.W)).asUInt
        arrayRegFiles(__tmp_4096 + 0.U) := __tmp_4097(7, 0)
        arrayRegFiles(__tmp_4096 + 1.U) := __tmp_4097(15, 8)

        val __tmp_4098 = (SP + 6.U(16.W))
        val __tmp_4099 = (DP).asUInt
        arrayRegFiles(__tmp_4098 + 0.U) := __tmp_4099(7, 0)
        arrayRegFiles(__tmp_4098 + 1.U) := __tmp_4099(15, 8)
        arrayRegFiles(__tmp_4098 + 2.U) := __tmp_4099(23, 16)
        arrayRegFiles(__tmp_4098 + 3.U) := __tmp_4099(31, 24)
        arrayRegFiles(__tmp_4098 + 4.U) := __tmp_4099(39, 32)
        arrayRegFiles(__tmp_4098 + 5.U) := __tmp_4099(47, 40)
        arrayRegFiles(__tmp_4098 + 6.U) := __tmp_4099(55, 48)
        arrayRegFiles(__tmp_4098 + 7.U) := __tmp_4099(63, 56)

        val __tmp_4100 = (SP + 14.U(16.W))
        val __tmp_4101 = (63.U(64.W)).asUInt
        arrayRegFiles(__tmp_4100 + 0.U) := __tmp_4101(7, 0)
        arrayRegFiles(__tmp_4100 + 1.U) := __tmp_4101(15, 8)
        arrayRegFiles(__tmp_4100 + 2.U) := __tmp_4101(23, 16)
        arrayRegFiles(__tmp_4100 + 3.U) := __tmp_4101(31, 24)
        arrayRegFiles(__tmp_4100 + 4.U) := __tmp_4101(39, 32)
        arrayRegFiles(__tmp_4100 + 5.U) := __tmp_4101(47, 40)
        arrayRegFiles(__tmp_4100 + 6.U) := __tmp_4101(55, 48)
        arrayRegFiles(__tmp_4100 + 7.U) := __tmp_4101(63, 56)

        val __tmp_4102 = (SP + 22.U(16.W))
        val __tmp_4103 = (generalRegFilesU32(0.U)).asUInt
        arrayRegFiles(__tmp_4102 + 0.U) := __tmp_4103(7, 0)
        arrayRegFiles(__tmp_4102 + 1.U) := __tmp_4103(15, 8)
        arrayRegFiles(__tmp_4102 + 2.U) := __tmp_4103(23, 16)
        arrayRegFiles(__tmp_4102 + 3.U) := __tmp_4103(31, 24)

        val __tmp_4104 = (SP - 2.U(16.W))
        val __tmp_4105 = (generalRegFilesU16(1.U)).asUInt
        arrayRegFiles(__tmp_4104 + 0.U) := __tmp_4105(7, 0)
        arrayRegFiles(__tmp_4104 + 1.U) := __tmp_4105(15, 8)

        CP := 203.U
      }

      is(42.U) {
        /*
        $16U.1 = *(SP - (2: SP)) [unsigned, SP, 2]  // restore $1
        $64U.3 = **(SP + (2: SP)) [unsigned, U64, 8]  // $3 = $res
        undecl c: C [@22, 4], mask: anvil.PrinterIndex.U [@14, 8], index: anvil.PrinterIndex.U [@6, 8], buffer: SP [@4, 2], $res: SP [@2, 2], $ret: CP [@0, 2]
        goto .43
        */


        val __tmp_4106 = ((SP - 2.U(16.W))).asUInt
        generalRegFilesU16(1.U) := Cat(
          arrayRegFiles(__tmp_4106 + 1.U),
          arrayRegFiles(__tmp_4106 + 0.U)
        )

        val __tmp_4107 = (Cat(
          arrayRegFiles((SP + 2.U(16.W)) + 1.U),
          arrayRegFiles((SP + 2.U(16.W)) + 0.U)
        )).asUInt
        generalRegFilesU64(3.U) := Cat(
          arrayRegFiles(__tmp_4107 + 7.U),
          arrayRegFiles(__tmp_4107 + 6.U),
          arrayRegFiles(__tmp_4107 + 5.U),
          arrayRegFiles(__tmp_4107 + 4.U),
          arrayRegFiles(__tmp_4107 + 3.U),
          arrayRegFiles(__tmp_4107 + 2.U),
          arrayRegFiles(__tmp_4107 + 1.U),
          arrayRegFiles(__tmp_4107 + 0.U)
        )

        CP := 43.U
      }

      is(43.U) {
        /*
        SP = SP - 136
        goto .44
        */


        SP := SP - 136.U

        CP := 44.U
      }

      is(44.U) {
        /*
        undecl c: C @$0
        goto .45
        */


        CP := 45.U
      }

      is(45.U) {
        /*
        DP = DP + ($64U.3 as DP)
        goto .46
        */


        DP := DP + generalRegFilesU64(3.U).asUInt

        CP := 46.U
      }

      is(46.U) {
        /*
        *(((8: SP) + (12: SP)) + ((DP & (63: DP)) as SP)) = (44: U8) [unsigned, U8, 1]  // $display((DP & (63: DP))) = (44: U8)
        *(((8: SP) + (12: SP)) + (((DP + (1: DP)) & (63: DP)) as SP)) = (32: U8) [unsigned, U8, 1]  // $display(((DP + (1: DP)) & (63: DP))) = (32: U8)
        *(((8: SP) + (12: SP)) + (((DP + (2: DP)) & (63: DP)) as SP)) = (115: U8) [unsigned, U8, 1]  // $display(((DP + (2: DP)) & (63: DP))) = (115: U8)
        *(((8: SP) + (12: SP)) + (((DP + (3: DP)) & (63: DP)) as SP)) = (32: U8) [unsigned, U8, 1]  // $display(((DP + (3: DP)) & (63: DP))) = (32: U8)
        *(((8: SP) + (12: SP)) + (((DP + (4: DP)) & (63: DP)) as SP)) = (61: U8) [unsigned, U8, 1]  // $display(((DP + (4: DP)) & (63: DP))) = (61: U8)
        *(((8: SP) + (12: SP)) + (((DP + (5: DP)) & (63: DP)) as SP)) = (32: U8) [unsigned, U8, 1]  // $display(((DP + (5: DP)) & (63: DP))) = (32: U8)
        goto .47
        */


        val __tmp_4108 = ((8.U(16.W) + 12.U(16.W)) + (DP & 63.U(64.W)).asUInt.pad(16))
        val __tmp_4109 = (44.U(8.W)).asUInt
        arrayRegFiles(__tmp_4108 + 0.U) := __tmp_4109(7, 0)

        val __tmp_4110 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 1.U(64.W)) & 63.U(64.W)).asUInt.pad(16))
        val __tmp_4111 = (32.U(8.W)).asUInt
        arrayRegFiles(__tmp_4110 + 0.U) := __tmp_4111(7, 0)

        val __tmp_4112 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 2.U(64.W)) & 63.U(64.W)).asUInt.pad(16))
        val __tmp_4113 = (115.U(8.W)).asUInt
        arrayRegFiles(__tmp_4112 + 0.U) := __tmp_4113(7, 0)

        val __tmp_4114 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 3.U(64.W)) & 63.U(64.W)).asUInt.pad(16))
        val __tmp_4115 = (32.U(8.W)).asUInt
        arrayRegFiles(__tmp_4114 + 0.U) := __tmp_4115(7, 0)

        val __tmp_4116 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 4.U(64.W)) & 63.U(64.W)).asUInt.pad(16))
        val __tmp_4117 = (61.U(8.W)).asUInt
        arrayRegFiles(__tmp_4116 + 0.U) := __tmp_4117(7, 0)

        val __tmp_4118 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 5.U(64.W)) & 63.U(64.W)).asUInt.pad(16))
        val __tmp_4119 = (32.U(8.W)).asUInt
        arrayRegFiles(__tmp_4118 + 0.U) := __tmp_4119(7, 0)

        CP := 47.U
      }

      is(47.U) {
        /*
        DP = DP + 6
        goto .48
        */


        DP := DP + 6.U

        CP := 48.U
      }

      is(48.U) {
        /*
        alloc printString$res@[9,45].1469CF57: U64 [@126, 8]
        goto .49
        */


        CP := 49.U
      }

      is(49.U) {
        /*
        SP = SP + 134
        goto .50
        */


        SP := SP + 134.U

        CP := 50.U
      }

      is(50.U) {
        /*
        decl $ret: CP [@0, 2], $res: SP [@2, 2], buffer: SP [@4, 2], index: anvil.PrinterIndex.U [@6, 8], mask: anvil.PrinterIndex.U [@14, 8], s: SP [@22, 2]
        *SP = (51: CP) [unsigned, CP, 2]  // $ret@0 = 1368
        *(SP + (2: SP)) = (SP - (8: SP)) [unsigned, SP, 2]  // $res@2 = -8
        *(SP + (4: SP)) = (8: SP) [unsigned, SP, 2]  // buffer = (8: SP)
        *(SP + (6: SP)) = DP [unsigned, anvil.PrinterIndex.U, 8]  // index = DP
        *(SP + (14: SP)) = (63: anvil.PrinterIndex.U) [unsigned, anvil.PrinterIndex.U, 8]  // mask = (63: anvil.PrinterIndex.U)
        *(SP + (22: SP)) = $16U.1 [unsigned, SP, 2]  // s = $16U.1
        goto .284
        */


        val __tmp_4120 = SP
        val __tmp_4121 = (51.U(16.W)).asUInt
        arrayRegFiles(__tmp_4120 + 0.U) := __tmp_4121(7, 0)
        arrayRegFiles(__tmp_4120 + 1.U) := __tmp_4121(15, 8)

        val __tmp_4122 = (SP + 2.U(16.W))
        val __tmp_4123 = ((SP - 8.U(16.W))).asUInt
        arrayRegFiles(__tmp_4122 + 0.U) := __tmp_4123(7, 0)
        arrayRegFiles(__tmp_4122 + 1.U) := __tmp_4123(15, 8)

        val __tmp_4124 = (SP + 4.U(16.W))
        val __tmp_4125 = (8.U(16.W)).asUInt
        arrayRegFiles(__tmp_4124 + 0.U) := __tmp_4125(7, 0)
        arrayRegFiles(__tmp_4124 + 1.U) := __tmp_4125(15, 8)

        val __tmp_4126 = (SP + 6.U(16.W))
        val __tmp_4127 = (DP).asUInt
        arrayRegFiles(__tmp_4126 + 0.U) := __tmp_4127(7, 0)
        arrayRegFiles(__tmp_4126 + 1.U) := __tmp_4127(15, 8)
        arrayRegFiles(__tmp_4126 + 2.U) := __tmp_4127(23, 16)
        arrayRegFiles(__tmp_4126 + 3.U) := __tmp_4127(31, 24)
        arrayRegFiles(__tmp_4126 + 4.U) := __tmp_4127(39, 32)
        arrayRegFiles(__tmp_4126 + 5.U) := __tmp_4127(47, 40)
        arrayRegFiles(__tmp_4126 + 6.U) := __tmp_4127(55, 48)
        arrayRegFiles(__tmp_4126 + 7.U) := __tmp_4127(63, 56)

        val __tmp_4128 = (SP + 14.U(16.W))
        val __tmp_4129 = (63.U(64.W)).asUInt
        arrayRegFiles(__tmp_4128 + 0.U) := __tmp_4129(7, 0)
        arrayRegFiles(__tmp_4128 + 1.U) := __tmp_4129(15, 8)
        arrayRegFiles(__tmp_4128 + 2.U) := __tmp_4129(23, 16)
        arrayRegFiles(__tmp_4128 + 3.U) := __tmp_4129(31, 24)
        arrayRegFiles(__tmp_4128 + 4.U) := __tmp_4129(39, 32)
        arrayRegFiles(__tmp_4128 + 5.U) := __tmp_4129(47, 40)
        arrayRegFiles(__tmp_4128 + 6.U) := __tmp_4129(55, 48)
        arrayRegFiles(__tmp_4128 + 7.U) := __tmp_4129(63, 56)

        val __tmp_4130 = (SP + 22.U(16.W))
        val __tmp_4131 = (generalRegFilesU16(1.U)).asUInt
        arrayRegFiles(__tmp_4130 + 0.U) := __tmp_4131(7, 0)
        arrayRegFiles(__tmp_4130 + 1.U) := __tmp_4131(15, 8)

        CP := 284.U
      }

      is(51.U) {
        /*
        $64U.3 = **(SP + (2: SP)) [unsigned, U64, 8]  // $3 = $res
        undecl s: SP [@22, 2], mask: anvil.PrinterIndex.U [@14, 8], index: anvil.PrinterIndex.U [@6, 8], buffer: SP [@4, 2], $res: SP [@2, 2], $ret: CP [@0, 2]
        goto .52
        */


        val __tmp_4132 = (Cat(
          arrayRegFiles((SP + 2.U(16.W)) + 1.U),
          arrayRegFiles((SP + 2.U(16.W)) + 0.U)
        )).asUInt
        generalRegFilesU64(3.U) := Cat(
          arrayRegFiles(__tmp_4132 + 7.U),
          arrayRegFiles(__tmp_4132 + 6.U),
          arrayRegFiles(__tmp_4132 + 5.U),
          arrayRegFiles(__tmp_4132 + 4.U),
          arrayRegFiles(__tmp_4132 + 3.U),
          arrayRegFiles(__tmp_4132 + 2.U),
          arrayRegFiles(__tmp_4132 + 1.U),
          arrayRegFiles(__tmp_4132 + 0.U)
        )

        CP := 52.U
      }

      is(52.U) {
        /*
        SP = SP - 134
        goto .53
        */


        SP := SP - 134.U

        CP := 53.U
      }

      is(53.U) {
        /*
        undecl s: String [@2, 108]
        goto .54
        */


        CP := 54.U
      }

      is(54.U) {
        /*
        DP = DP + ($64U.3 as DP)
        goto .55
        */


        DP := DP + generalRegFilesU64(3.U).asUInt

        CP := 55.U
      }

      is(55.U) {
        /*
        *(((8: SP) + (12: SP)) + ((DP & (63: DP)) as SP)) = (10: U8) [unsigned, U8, 1]  // $display((DP & (63: DP))) = (10: U8)
        goto .56
        */


        val __tmp_4133 = ((8.U(16.W) + 12.U(16.W)) + (DP & 63.U(64.W)).asUInt.pad(16))
        val __tmp_4134 = (10.U(8.W)).asUInt
        arrayRegFiles(__tmp_4133 + 0.U) := __tmp_4134(7, 0)

        CP := 56.U
      }

      is(56.U) {
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

      is(57.U) {
        /*
        $16U.0 = *(SP + (4: SP)) [unsigned, MS[anvil.PrinterIndex.U, U8], 2]  // buffer
        $64U.0 = *(SP + (6: SP)) [unsigned, anvil.PrinterIndex.U, 8]  // index
        $64U.1 = *(SP + (14: SP)) [unsigned, anvil.PrinterIndex.U, 8]  // mask
        $64S.0 = *(SP + (22: SP)) [signed, S64, 8]  // n
        goto .58
        */


        val __tmp_4135 = ((SP + 4.U(16.W))).asUInt
        generalRegFilesU16(0.U) := Cat(
          arrayRegFiles(__tmp_4135 + 1.U),
          arrayRegFiles(__tmp_4135 + 0.U)
        )

        val __tmp_4136 = ((SP + 6.U(16.W))).asUInt
        generalRegFilesU64(0.U) := Cat(
          arrayRegFiles(__tmp_4136 + 7.U),
          arrayRegFiles(__tmp_4136 + 6.U),
          arrayRegFiles(__tmp_4136 + 5.U),
          arrayRegFiles(__tmp_4136 + 4.U),
          arrayRegFiles(__tmp_4136 + 3.U),
          arrayRegFiles(__tmp_4136 + 2.U),
          arrayRegFiles(__tmp_4136 + 1.U),
          arrayRegFiles(__tmp_4136 + 0.U)
        )

        val __tmp_4137 = ((SP + 14.U(16.W))).asUInt
        generalRegFilesU64(1.U) := Cat(
          arrayRegFiles(__tmp_4137 + 7.U),
          arrayRegFiles(__tmp_4137 + 6.U),
          arrayRegFiles(__tmp_4137 + 5.U),
          arrayRegFiles(__tmp_4137 + 4.U),
          arrayRegFiles(__tmp_4137 + 3.U),
          arrayRegFiles(__tmp_4137 + 2.U),
          arrayRegFiles(__tmp_4137 + 1.U),
          arrayRegFiles(__tmp_4137 + 0.U)
        )

        val __tmp_4138 = ((SP + 22.U(16.W))).asUInt
        generalRegFilesS64(0.U) := Cat(
          arrayRegFiles(__tmp_4138 + 7.U),
          arrayRegFiles(__tmp_4138 + 6.U),
          arrayRegFiles(__tmp_4138 + 5.U),
          arrayRegFiles(__tmp_4138 + 4.U),
          arrayRegFiles(__tmp_4138 + 3.U),
          arrayRegFiles(__tmp_4138 + 2.U),
          arrayRegFiles(__tmp_4138 + 1.U),
          arrayRegFiles(__tmp_4138 + 0.U)
        ).asSInt.pad(64)

        CP := 58.U
      }

      is(58.U) {
        /*
        $1U.1 = ($64S.0 ≡ (-9223372036854775808: S64))
        goto .59
        */



        generalRegFilesU1(1.U) := (generalRegFilesS64(0.U) === BigInt("-9223372036854775808").S(64.W)).asUInt

        CP := 59.U
      }

      is(59.U) {
        /*
        if $1U.1 goto .60 else goto .120
        */


        CP := Mux((generalRegFilesU1(1.U).asUInt) === 1.U, 60.U, 120.U)
      }

      is(60.U) {
        /*
        $16U.3 = $16U.0
        $64U.8 = ($64U.0 & $64U.1)
        goto .61
        */



        generalRegFilesU16(3.U) := generalRegFilesU16(0.U)


        generalRegFilesU64(8.U) := (generalRegFilesU64(0.U) & generalRegFilesU64(1.U))

        CP := 61.U
      }

      is(61.U) {
        /*
        *(($16U.3 + (12: SP)) + ($64U.8 as SP)) = (45: U8) [unsigned, U8, 1]  // $16U.3($64U.8) = (45: U8)
        goto .62
        */


        val __tmp_4139 = ((generalRegFilesU16(3.U) + 12.U(16.W)) + generalRegFilesU64(8.U).asUInt.pad(16))
        val __tmp_4140 = (45.U(8.W)).asUInt
        arrayRegFiles(__tmp_4139 + 0.U) := __tmp_4140(7, 0)

        CP := 62.U
      }

      is(62.U) {
        /*
        $16U.3 = $16U.0
        $64U.9 = ($64U.0 + (1: anvil.PrinterIndex.U))
        goto .63
        */



        generalRegFilesU16(3.U) := generalRegFilesU16(0.U)


        generalRegFilesU64(9.U) := (generalRegFilesU64(0.U) + 1.U(64.W))

        CP := 63.U
      }

      is(63.U) {
        /*
        $64U.10 = ($64U.9 & $64U.1)
        goto .64
        */



        generalRegFilesU64(10.U) := (generalRegFilesU64(9.U) & generalRegFilesU64(1.U))

        CP := 64.U
      }

      is(64.U) {
        /*
        *(($16U.3 + (12: SP)) + ($64U.10 as SP)) = (57: U8) [unsigned, U8, 1]  // $16U.3($64U.10) = (57: U8)
        goto .65
        */


        val __tmp_4141 = ((generalRegFilesU16(3.U) + 12.U(16.W)) + generalRegFilesU64(10.U).asUInt.pad(16))
        val __tmp_4142 = (57.U(8.W)).asUInt
        arrayRegFiles(__tmp_4141 + 0.U) := __tmp_4142(7, 0)

        CP := 65.U
      }

      is(65.U) {
        /*
        $16U.3 = $16U.0
        $64U.9 = ($64U.0 + (2: anvil.PrinterIndex.U))
        goto .66
        */



        generalRegFilesU16(3.U) := generalRegFilesU16(0.U)


        generalRegFilesU64(9.U) := (generalRegFilesU64(0.U) + 2.U(64.W))

        CP := 66.U
      }

      is(66.U) {
        /*
        $64U.10 = ($64U.9 & $64U.1)
        goto .67
        */



        generalRegFilesU64(10.U) := (generalRegFilesU64(9.U) & generalRegFilesU64(1.U))

        CP := 67.U
      }

      is(67.U) {
        /*
        *(($16U.3 + (12: SP)) + ($64U.10 as SP)) = (50: U8) [unsigned, U8, 1]  // $16U.3($64U.10) = (50: U8)
        goto .68
        */


        val __tmp_4143 = ((generalRegFilesU16(3.U) + 12.U(16.W)) + generalRegFilesU64(10.U).asUInt.pad(16))
        val __tmp_4144 = (50.U(8.W)).asUInt
        arrayRegFiles(__tmp_4143 + 0.U) := __tmp_4144(7, 0)

        CP := 68.U
      }

      is(68.U) {
        /*
        $16U.3 = $16U.0
        $64U.9 = ($64U.0 + (3: anvil.PrinterIndex.U))
        goto .69
        */



        generalRegFilesU16(3.U) := generalRegFilesU16(0.U)


        generalRegFilesU64(9.U) := (generalRegFilesU64(0.U) + 3.U(64.W))

        CP := 69.U
      }

      is(69.U) {
        /*
        $64U.10 = ($64U.9 & $64U.1)
        goto .70
        */



        generalRegFilesU64(10.U) := (generalRegFilesU64(9.U) & generalRegFilesU64(1.U))

        CP := 70.U
      }

      is(70.U) {
        /*
        *(($16U.3 + (12: SP)) + ($64U.10 as SP)) = (50: U8) [unsigned, U8, 1]  // $16U.3($64U.10) = (50: U8)
        goto .71
        */


        val __tmp_4145 = ((generalRegFilesU16(3.U) + 12.U(16.W)) + generalRegFilesU64(10.U).asUInt.pad(16))
        val __tmp_4146 = (50.U(8.W)).asUInt
        arrayRegFiles(__tmp_4145 + 0.U) := __tmp_4146(7, 0)

        CP := 71.U
      }

      is(71.U) {
        /*
        $16U.3 = $16U.0
        $64U.9 = ($64U.0 + (4: anvil.PrinterIndex.U))
        goto .72
        */



        generalRegFilesU16(3.U) := generalRegFilesU16(0.U)


        generalRegFilesU64(9.U) := (generalRegFilesU64(0.U) + 4.U(64.W))

        CP := 72.U
      }

      is(72.U) {
        /*
        $64U.10 = ($64U.9 & $64U.1)
        goto .73
        */



        generalRegFilesU64(10.U) := (generalRegFilesU64(9.U) & generalRegFilesU64(1.U))

        CP := 73.U
      }

      is(73.U) {
        /*
        *(($16U.3 + (12: SP)) + ($64U.10 as SP)) = (51: U8) [unsigned, U8, 1]  // $16U.3($64U.10) = (51: U8)
        goto .74
        */


        val __tmp_4147 = ((generalRegFilesU16(3.U) + 12.U(16.W)) + generalRegFilesU64(10.U).asUInt.pad(16))
        val __tmp_4148 = (51.U(8.W)).asUInt
        arrayRegFiles(__tmp_4147 + 0.U) := __tmp_4148(7, 0)

        CP := 74.U
      }

      is(74.U) {
        /*
        $16U.3 = $16U.0
        $64U.9 = ($64U.0 + (5: anvil.PrinterIndex.U))
        goto .75
        */



        generalRegFilesU16(3.U) := generalRegFilesU16(0.U)


        generalRegFilesU64(9.U) := (generalRegFilesU64(0.U) + 5.U(64.W))

        CP := 75.U
      }

      is(75.U) {
        /*
        $64U.10 = ($64U.9 & $64U.1)
        goto .76
        */



        generalRegFilesU64(10.U) := (generalRegFilesU64(9.U) & generalRegFilesU64(1.U))

        CP := 76.U
      }

      is(76.U) {
        /*
        *(($16U.3 + (12: SP)) + ($64U.10 as SP)) = (51: U8) [unsigned, U8, 1]  // $16U.3($64U.10) = (51: U8)
        goto .77
        */


        val __tmp_4149 = ((generalRegFilesU16(3.U) + 12.U(16.W)) + generalRegFilesU64(10.U).asUInt.pad(16))
        val __tmp_4150 = (51.U(8.W)).asUInt
        arrayRegFiles(__tmp_4149 + 0.U) := __tmp_4150(7, 0)

        CP := 77.U
      }

      is(77.U) {
        /*
        $16U.3 = $16U.0
        $64U.9 = ($64U.0 + (6: anvil.PrinterIndex.U))
        goto .78
        */



        generalRegFilesU16(3.U) := generalRegFilesU16(0.U)


        generalRegFilesU64(9.U) := (generalRegFilesU64(0.U) + 6.U(64.W))

        CP := 78.U
      }

      is(78.U) {
        /*
        $64U.10 = ($64U.9 & $64U.1)
        goto .79
        */



        generalRegFilesU64(10.U) := (generalRegFilesU64(9.U) & generalRegFilesU64(1.U))

        CP := 79.U
      }

      is(79.U) {
        /*
        *(($16U.3 + (12: SP)) + ($64U.10 as SP)) = (55: U8) [unsigned, U8, 1]  // $16U.3($64U.10) = (55: U8)
        goto .80
        */


        val __tmp_4151 = ((generalRegFilesU16(3.U) + 12.U(16.W)) + generalRegFilesU64(10.U).asUInt.pad(16))
        val __tmp_4152 = (55.U(8.W)).asUInt
        arrayRegFiles(__tmp_4151 + 0.U) := __tmp_4152(7, 0)

        CP := 80.U
      }

      is(80.U) {
        /*
        $16U.3 = $16U.0
        $64U.9 = ($64U.0 + (7: anvil.PrinterIndex.U))
        goto .81
        */



        generalRegFilesU16(3.U) := generalRegFilesU16(0.U)


        generalRegFilesU64(9.U) := (generalRegFilesU64(0.U) + 7.U(64.W))

        CP := 81.U
      }

      is(81.U) {
        /*
        $64U.10 = ($64U.9 & $64U.1)
        goto .82
        */



        generalRegFilesU64(10.U) := (generalRegFilesU64(9.U) & generalRegFilesU64(1.U))

        CP := 82.U
      }

      is(82.U) {
        /*
        *(($16U.3 + (12: SP)) + ($64U.10 as SP)) = (50: U8) [unsigned, U8, 1]  // $16U.3($64U.10) = (50: U8)
        goto .83
        */


        val __tmp_4153 = ((generalRegFilesU16(3.U) + 12.U(16.W)) + generalRegFilesU64(10.U).asUInt.pad(16))
        val __tmp_4154 = (50.U(8.W)).asUInt
        arrayRegFiles(__tmp_4153 + 0.U) := __tmp_4154(7, 0)

        CP := 83.U
      }

      is(83.U) {
        /*
        $16U.3 = $16U.0
        $64U.9 = ($64U.0 + (8: anvil.PrinterIndex.U))
        goto .84
        */



        generalRegFilesU16(3.U) := generalRegFilesU16(0.U)


        generalRegFilesU64(9.U) := (generalRegFilesU64(0.U) + 8.U(64.W))

        CP := 84.U
      }

      is(84.U) {
        /*
        $64U.10 = ($64U.9 & $64U.1)
        goto .85
        */



        generalRegFilesU64(10.U) := (generalRegFilesU64(9.U) & generalRegFilesU64(1.U))

        CP := 85.U
      }

      is(85.U) {
        /*
        *(($16U.3 + (12: SP)) + ($64U.10 as SP)) = (48: U8) [unsigned, U8, 1]  // $16U.3($64U.10) = (48: U8)
        goto .86
        */


        val __tmp_4155 = ((generalRegFilesU16(3.U) + 12.U(16.W)) + generalRegFilesU64(10.U).asUInt.pad(16))
        val __tmp_4156 = (48.U(8.W)).asUInt
        arrayRegFiles(__tmp_4155 + 0.U) := __tmp_4156(7, 0)

        CP := 86.U
      }

      is(86.U) {
        /*
        $16U.3 = $16U.0
        $64U.9 = ($64U.0 + (9: anvil.PrinterIndex.U))
        goto .87
        */



        generalRegFilesU16(3.U) := generalRegFilesU16(0.U)


        generalRegFilesU64(9.U) := (generalRegFilesU64(0.U) + 9.U(64.W))

        CP := 87.U
      }

      is(87.U) {
        /*
        $64U.10 = ($64U.9 & $64U.1)
        goto .88
        */



        generalRegFilesU64(10.U) := (generalRegFilesU64(9.U) & generalRegFilesU64(1.U))

        CP := 88.U
      }

      is(88.U) {
        /*
        *(($16U.3 + (12: SP)) + ($64U.10 as SP)) = (51: U8) [unsigned, U8, 1]  // $16U.3($64U.10) = (51: U8)
        goto .89
        */


        val __tmp_4157 = ((generalRegFilesU16(3.U) + 12.U(16.W)) + generalRegFilesU64(10.U).asUInt.pad(16))
        val __tmp_4158 = (51.U(8.W)).asUInt
        arrayRegFiles(__tmp_4157 + 0.U) := __tmp_4158(7, 0)

        CP := 89.U
      }

      is(89.U) {
        /*
        $16U.3 = $16U.0
        $64U.9 = ($64U.0 + (10: anvil.PrinterIndex.U))
        goto .90
        */



        generalRegFilesU16(3.U) := generalRegFilesU16(0.U)


        generalRegFilesU64(9.U) := (generalRegFilesU64(0.U) + 10.U(64.W))

        CP := 90.U
      }

      is(90.U) {
        /*
        $64U.10 = ($64U.9 & $64U.1)
        goto .91
        */



        generalRegFilesU64(10.U) := (generalRegFilesU64(9.U) & generalRegFilesU64(1.U))

        CP := 91.U
      }

      is(91.U) {
        /*
        *(($16U.3 + (12: SP)) + ($64U.10 as SP)) = (54: U8) [unsigned, U8, 1]  // $16U.3($64U.10) = (54: U8)
        goto .92
        */


        val __tmp_4159 = ((generalRegFilesU16(3.U) + 12.U(16.W)) + generalRegFilesU64(10.U).asUInt.pad(16))
        val __tmp_4160 = (54.U(8.W)).asUInt
        arrayRegFiles(__tmp_4159 + 0.U) := __tmp_4160(7, 0)

        CP := 92.U
      }

      is(92.U) {
        /*
        $16U.3 = $16U.0
        $64U.9 = ($64U.0 + (11: anvil.PrinterIndex.U))
        goto .93
        */



        generalRegFilesU16(3.U) := generalRegFilesU16(0.U)


        generalRegFilesU64(9.U) := (generalRegFilesU64(0.U) + 11.U(64.W))

        CP := 93.U
      }

      is(93.U) {
        /*
        $64U.10 = ($64U.9 & $64U.1)
        goto .94
        */



        generalRegFilesU64(10.U) := (generalRegFilesU64(9.U) & generalRegFilesU64(1.U))

        CP := 94.U
      }

      is(94.U) {
        /*
        *(($16U.3 + (12: SP)) + ($64U.10 as SP)) = (56: U8) [unsigned, U8, 1]  // $16U.3($64U.10) = (56: U8)
        goto .95
        */


        val __tmp_4161 = ((generalRegFilesU16(3.U) + 12.U(16.W)) + generalRegFilesU64(10.U).asUInt.pad(16))
        val __tmp_4162 = (56.U(8.W)).asUInt
        arrayRegFiles(__tmp_4161 + 0.U) := __tmp_4162(7, 0)

        CP := 95.U
      }

      is(95.U) {
        /*
        $16U.3 = $16U.0
        $64U.9 = ($64U.0 + (12: anvil.PrinterIndex.U))
        goto .96
        */



        generalRegFilesU16(3.U) := generalRegFilesU16(0.U)


        generalRegFilesU64(9.U) := (generalRegFilesU64(0.U) + 12.U(64.W))

        CP := 96.U
      }

      is(96.U) {
        /*
        $64U.10 = ($64U.9 & $64U.1)
        goto .97
        */



        generalRegFilesU64(10.U) := (generalRegFilesU64(9.U) & generalRegFilesU64(1.U))

        CP := 97.U
      }

      is(97.U) {
        /*
        *(($16U.3 + (12: SP)) + ($64U.10 as SP)) = (53: U8) [unsigned, U8, 1]  // $16U.3($64U.10) = (53: U8)
        goto .98
        */


        val __tmp_4163 = ((generalRegFilesU16(3.U) + 12.U(16.W)) + generalRegFilesU64(10.U).asUInt.pad(16))
        val __tmp_4164 = (53.U(8.W)).asUInt
        arrayRegFiles(__tmp_4163 + 0.U) := __tmp_4164(7, 0)

        CP := 98.U
      }

      is(98.U) {
        /*
        $16U.3 = $16U.0
        $64U.9 = ($64U.0 + (13: anvil.PrinterIndex.U))
        goto .99
        */



        generalRegFilesU16(3.U) := generalRegFilesU16(0.U)


        generalRegFilesU64(9.U) := (generalRegFilesU64(0.U) + 13.U(64.W))

        CP := 99.U
      }

      is(99.U) {
        /*
        $64U.10 = ($64U.9 & $64U.1)
        goto .100
        */



        generalRegFilesU64(10.U) := (generalRegFilesU64(9.U) & generalRegFilesU64(1.U))

        CP := 100.U
      }

      is(100.U) {
        /*
        *(($16U.3 + (12: SP)) + ($64U.10 as SP)) = (52: U8) [unsigned, U8, 1]  // $16U.3($64U.10) = (52: U8)
        goto .101
        */


        val __tmp_4165 = ((generalRegFilesU16(3.U) + 12.U(16.W)) + generalRegFilesU64(10.U).asUInt.pad(16))
        val __tmp_4166 = (52.U(8.W)).asUInt
        arrayRegFiles(__tmp_4165 + 0.U) := __tmp_4166(7, 0)

        CP := 101.U
      }

      is(101.U) {
        /*
        $16U.3 = $16U.0
        $64U.9 = ($64U.0 + (14: anvil.PrinterIndex.U))
        goto .102
        */



        generalRegFilesU16(3.U) := generalRegFilesU16(0.U)


        generalRegFilesU64(9.U) := (generalRegFilesU64(0.U) + 14.U(64.W))

        CP := 102.U
      }

      is(102.U) {
        /*
        $64U.10 = ($64U.9 & $64U.1)
        goto .103
        */



        generalRegFilesU64(10.U) := (generalRegFilesU64(9.U) & generalRegFilesU64(1.U))

        CP := 103.U
      }

      is(103.U) {
        /*
        *(($16U.3 + (12: SP)) + ($64U.10 as SP)) = (55: U8) [unsigned, U8, 1]  // $16U.3($64U.10) = (55: U8)
        goto .104
        */


        val __tmp_4167 = ((generalRegFilesU16(3.U) + 12.U(16.W)) + generalRegFilesU64(10.U).asUInt.pad(16))
        val __tmp_4168 = (55.U(8.W)).asUInt
        arrayRegFiles(__tmp_4167 + 0.U) := __tmp_4168(7, 0)

        CP := 104.U
      }

      is(104.U) {
        /*
        $16U.3 = $16U.0
        $64U.9 = ($64U.0 + (15: anvil.PrinterIndex.U))
        goto .105
        */



        generalRegFilesU16(3.U) := generalRegFilesU16(0.U)


        generalRegFilesU64(9.U) := (generalRegFilesU64(0.U) + 15.U(64.W))

        CP := 105.U
      }

      is(105.U) {
        /*
        $64U.10 = ($64U.9 & $64U.1)
        goto .106
        */



        generalRegFilesU64(10.U) := (generalRegFilesU64(9.U) & generalRegFilesU64(1.U))

        CP := 106.U
      }

      is(106.U) {
        /*
        *(($16U.3 + (12: SP)) + ($64U.10 as SP)) = (55: U8) [unsigned, U8, 1]  // $16U.3($64U.10) = (55: U8)
        goto .107
        */


        val __tmp_4169 = ((generalRegFilesU16(3.U) + 12.U(16.W)) + generalRegFilesU64(10.U).asUInt.pad(16))
        val __tmp_4170 = (55.U(8.W)).asUInt
        arrayRegFiles(__tmp_4169 + 0.U) := __tmp_4170(7, 0)

        CP := 107.U
      }

      is(107.U) {
        /*
        $16U.3 = $16U.0
        $64U.9 = ($64U.0 + (16: anvil.PrinterIndex.U))
        goto .108
        */



        generalRegFilesU16(3.U) := generalRegFilesU16(0.U)


        generalRegFilesU64(9.U) := (generalRegFilesU64(0.U) + 16.U(64.W))

        CP := 108.U
      }

      is(108.U) {
        /*
        $64U.10 = ($64U.9 & $64U.1)
        goto .109
        */



        generalRegFilesU64(10.U) := (generalRegFilesU64(9.U) & generalRegFilesU64(1.U))

        CP := 109.U
      }

      is(109.U) {
        /*
        *(($16U.3 + (12: SP)) + ($64U.10 as SP)) = (53: U8) [unsigned, U8, 1]  // $16U.3($64U.10) = (53: U8)
        goto .110
        */


        val __tmp_4171 = ((generalRegFilesU16(3.U) + 12.U(16.W)) + generalRegFilesU64(10.U).asUInt.pad(16))
        val __tmp_4172 = (53.U(8.W)).asUInt
        arrayRegFiles(__tmp_4171 + 0.U) := __tmp_4172(7, 0)

        CP := 110.U
      }

      is(110.U) {
        /*
        $16U.3 = $16U.0
        $64U.9 = ($64U.0 + (17: anvil.PrinterIndex.U))
        goto .111
        */



        generalRegFilesU16(3.U) := generalRegFilesU16(0.U)


        generalRegFilesU64(9.U) := (generalRegFilesU64(0.U) + 17.U(64.W))

        CP := 111.U
      }

      is(111.U) {
        /*
        $64U.10 = ($64U.9 & $64U.1)
        goto .112
        */



        generalRegFilesU64(10.U) := (generalRegFilesU64(9.U) & generalRegFilesU64(1.U))

        CP := 112.U
      }

      is(112.U) {
        /*
        *(($16U.3 + (12: SP)) + ($64U.10 as SP)) = (56: U8) [unsigned, U8, 1]  // $16U.3($64U.10) = (56: U8)
        goto .113
        */


        val __tmp_4173 = ((generalRegFilesU16(3.U) + 12.U(16.W)) + generalRegFilesU64(10.U).asUInt.pad(16))
        val __tmp_4174 = (56.U(8.W)).asUInt
        arrayRegFiles(__tmp_4173 + 0.U) := __tmp_4174(7, 0)

        CP := 113.U
      }

      is(113.U) {
        /*
        $16U.3 = $16U.0
        $64U.9 = ($64U.0 + (18: anvil.PrinterIndex.U))
        goto .114
        */



        generalRegFilesU16(3.U) := generalRegFilesU16(0.U)


        generalRegFilesU64(9.U) := (generalRegFilesU64(0.U) + 18.U(64.W))

        CP := 114.U
      }

      is(114.U) {
        /*
        $64U.10 = ($64U.9 & $64U.1)
        goto .115
        */



        generalRegFilesU64(10.U) := (generalRegFilesU64(9.U) & generalRegFilesU64(1.U))

        CP := 115.U
      }

      is(115.U) {
        /*
        *(($16U.3 + (12: SP)) + ($64U.10 as SP)) = (48: U8) [unsigned, U8, 1]  // $16U.3($64U.10) = (48: U8)
        goto .116
        */


        val __tmp_4175 = ((generalRegFilesU16(3.U) + 12.U(16.W)) + generalRegFilesU64(10.U).asUInt.pad(16))
        val __tmp_4176 = (48.U(8.W)).asUInt
        arrayRegFiles(__tmp_4175 + 0.U) := __tmp_4176(7, 0)

        CP := 116.U
      }

      is(116.U) {
        /*
        $16U.3 = $16U.0
        $64U.9 = ($64U.0 + (19: anvil.PrinterIndex.U))
        goto .117
        */



        generalRegFilesU16(3.U) := generalRegFilesU16(0.U)


        generalRegFilesU64(9.U) := (generalRegFilesU64(0.U) + 19.U(64.W))

        CP := 117.U
      }

      is(117.U) {
        /*
        $64U.10 = ($64U.9 & $64U.1)
        goto .118
        */



        generalRegFilesU64(10.U) := (generalRegFilesU64(9.U) & generalRegFilesU64(1.U))

        CP := 118.U
      }

      is(118.U) {
        /*
        *(($16U.3 + (12: SP)) + ($64U.10 as SP)) = (56: U8) [unsigned, U8, 1]  // $16U.3($64U.10) = (56: U8)
        goto .119
        */


        val __tmp_4177 = ((generalRegFilesU16(3.U) + 12.U(16.W)) + generalRegFilesU64(10.U).asUInt.pad(16))
        val __tmp_4178 = (56.U(8.W)).asUInt
        arrayRegFiles(__tmp_4177 + 0.U) := __tmp_4178(7, 0)

        CP := 119.U
      }

      is(119.U) {
        /*
        **(SP + (2: SP)) = (20: U64) [unsigned, U64, 8]  // $res = (20: U64)
        goto $ret@0
        */


        val __tmp_4179 = Cat(
          arrayRegFiles((SP + 2.U(16.W)) + 1.U),
          arrayRegFiles((SP + 2.U(16.W)) + 0.U)
        )
        val __tmp_4180 = (20.U(64.W)).asUInt
        arrayRegFiles(__tmp_4179 + 0.U) := __tmp_4180(7, 0)
        arrayRegFiles(__tmp_4179 + 1.U) := __tmp_4180(15, 8)
        arrayRegFiles(__tmp_4179 + 2.U) := __tmp_4180(23, 16)
        arrayRegFiles(__tmp_4179 + 3.U) := __tmp_4180(31, 24)
        arrayRegFiles(__tmp_4179 + 4.U) := __tmp_4180(39, 32)
        arrayRegFiles(__tmp_4179 + 5.U) := __tmp_4180(47, 40)
        arrayRegFiles(__tmp_4179 + 6.U) := __tmp_4180(55, 48)
        arrayRegFiles(__tmp_4179 + 7.U) := __tmp_4180(63, 56)

        CP := Cat(
          arrayRegFiles(SP + 0.U + 1.U),
          arrayRegFiles(SP + 0.U + 0.U)
        )

      }

      is(120.U) {
        /*
        $1U.1 = ($64S.0 ≡ (0: S64))
        goto .121
        */



        generalRegFilesU1(1.U) := (generalRegFilesS64(0.U) === 0.S(64.W)).asUInt

        CP := 121.U
      }

      is(121.U) {
        /*
        if $1U.1 goto .122 else goto .125
        */


        CP := Mux((generalRegFilesU1(1.U).asUInt) === 1.U, 122.U, 125.U)
      }

      is(122.U) {
        /*
        $16U.3 = $16U.0
        $64U.8 = ($64U.0 & $64U.1)
        goto .123
        */



        generalRegFilesU16(3.U) := generalRegFilesU16(0.U)


        generalRegFilesU64(8.U) := (generalRegFilesU64(0.U) & generalRegFilesU64(1.U))

        CP := 123.U
      }

      is(123.U) {
        /*
        *(($16U.3 + (12: SP)) + ($64U.8 as SP)) = (48: U8) [unsigned, U8, 1]  // $16U.3($64U.8) = (48: U8)
        goto .124
        */


        val __tmp_4181 = ((generalRegFilesU16(3.U) + 12.U(16.W)) + generalRegFilesU64(8.U).asUInt.pad(16))
        val __tmp_4182 = (48.U(8.W)).asUInt
        arrayRegFiles(__tmp_4181 + 0.U) := __tmp_4182(7, 0)

        CP := 124.U
      }

      is(124.U) {
        /*
        **(SP + (2: SP)) = (1: U64) [unsigned, U64, 8]  // $res = (1: U64)
        goto $ret@0
        */


        val __tmp_4183 = Cat(
          arrayRegFiles((SP + 2.U(16.W)) + 1.U),
          arrayRegFiles((SP + 2.U(16.W)) + 0.U)
        )
        val __tmp_4184 = (1.U(64.W)).asUInt
        arrayRegFiles(__tmp_4183 + 0.U) := __tmp_4184(7, 0)
        arrayRegFiles(__tmp_4183 + 1.U) := __tmp_4184(15, 8)
        arrayRegFiles(__tmp_4183 + 2.U) := __tmp_4184(23, 16)
        arrayRegFiles(__tmp_4183 + 3.U) := __tmp_4184(31, 24)
        arrayRegFiles(__tmp_4183 + 4.U) := __tmp_4184(39, 32)
        arrayRegFiles(__tmp_4183 + 5.U) := __tmp_4184(47, 40)
        arrayRegFiles(__tmp_4183 + 6.U) := __tmp_4184(55, 48)
        arrayRegFiles(__tmp_4183 + 7.U) := __tmp_4184(63, 56)

        CP := Cat(
          arrayRegFiles(SP + 0.U + 1.U),
          arrayRegFiles(SP + 0.U + 0.U)
        )

      }

      is(125.U) {
        /*
        decl buff: MS[anvil.PrinterIndex.I20, U8] [@30, 34]
        alloc $new@[167,16].100A3CBA: MS[anvil.PrinterIndex.I20, U8] [@64, 34]
        $16U.4 = (SP + (64: SP))
        *(SP + (64: SP)) = (323602724: U32) [unsigned, U32, 4]  // sha3 type signature of MS[anvil.PrinterIndex.I20, U8]: 0x1349C924
        *(SP + (68: SP)) = (20: Z) [signed, Z, 8]  // size of MS[anvil.PrinterIndex.I20, U8]((0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8))
        goto .126
        */



        generalRegFilesU16(4.U) := (SP + 64.U(16.W))

        val __tmp_4185 = (SP + 64.U(16.W))
        val __tmp_4186 = (323602724.U(32.W)).asUInt
        arrayRegFiles(__tmp_4185 + 0.U) := __tmp_4186(7, 0)
        arrayRegFiles(__tmp_4185 + 1.U) := __tmp_4186(15, 8)
        arrayRegFiles(__tmp_4185 + 2.U) := __tmp_4186(23, 16)
        arrayRegFiles(__tmp_4185 + 3.U) := __tmp_4186(31, 24)

        val __tmp_4187 = (SP + 68.U(16.W))
        val __tmp_4188 = (20.S(64.W)).asUInt
        arrayRegFiles(__tmp_4187 + 0.U) := __tmp_4188(7, 0)
        arrayRegFiles(__tmp_4187 + 1.U) := __tmp_4188(15, 8)
        arrayRegFiles(__tmp_4187 + 2.U) := __tmp_4188(23, 16)
        arrayRegFiles(__tmp_4187 + 3.U) := __tmp_4188(31, 24)
        arrayRegFiles(__tmp_4187 + 4.U) := __tmp_4188(39, 32)
        arrayRegFiles(__tmp_4187 + 5.U) := __tmp_4188(47, 40)
        arrayRegFiles(__tmp_4187 + 6.U) := __tmp_4188(55, 48)
        arrayRegFiles(__tmp_4187 + 7.U) := __tmp_4188(63, 56)

        CP := 126.U
      }

      is(126.U) {
        /*
        *(($16U.4 + (12: SP)) + ((0: anvil.PrinterIndex.I20) as SP)) = (0: U8) [unsigned, U8, 1]  // $16U.4((0: anvil.PrinterIndex.I20)) = (0: U8)
        *(($16U.4 + (12: SP)) + ((1: anvil.PrinterIndex.I20) as SP)) = (0: U8) [unsigned, U8, 1]  // $16U.4((1: anvil.PrinterIndex.I20)) = (0: U8)
        *(($16U.4 + (12: SP)) + ((2: anvil.PrinterIndex.I20) as SP)) = (0: U8) [unsigned, U8, 1]  // $16U.4((2: anvil.PrinterIndex.I20)) = (0: U8)
        *(($16U.4 + (12: SP)) + ((3: anvil.PrinterIndex.I20) as SP)) = (0: U8) [unsigned, U8, 1]  // $16U.4((3: anvil.PrinterIndex.I20)) = (0: U8)
        *(($16U.4 + (12: SP)) + ((4: anvil.PrinterIndex.I20) as SP)) = (0: U8) [unsigned, U8, 1]  // $16U.4((4: anvil.PrinterIndex.I20)) = (0: U8)
        *(($16U.4 + (12: SP)) + ((5: anvil.PrinterIndex.I20) as SP)) = (0: U8) [unsigned, U8, 1]  // $16U.4((5: anvil.PrinterIndex.I20)) = (0: U8)
        *(($16U.4 + (12: SP)) + ((6: anvil.PrinterIndex.I20) as SP)) = (0: U8) [unsigned, U8, 1]  // $16U.4((6: anvil.PrinterIndex.I20)) = (0: U8)
        *(($16U.4 + (12: SP)) + ((7: anvil.PrinterIndex.I20) as SP)) = (0: U8) [unsigned, U8, 1]  // $16U.4((7: anvil.PrinterIndex.I20)) = (0: U8)
        *(($16U.4 + (12: SP)) + ((8: anvil.PrinterIndex.I20) as SP)) = (0: U8) [unsigned, U8, 1]  // $16U.4((8: anvil.PrinterIndex.I20)) = (0: U8)
        *(($16U.4 + (12: SP)) + ((9: anvil.PrinterIndex.I20) as SP)) = (0: U8) [unsigned, U8, 1]  // $16U.4((9: anvil.PrinterIndex.I20)) = (0: U8)
        *(($16U.4 + (12: SP)) + ((10: anvil.PrinterIndex.I20) as SP)) = (0: U8) [unsigned, U8, 1]  // $16U.4((10: anvil.PrinterIndex.I20)) = (0: U8)
        *(($16U.4 + (12: SP)) + ((11: anvil.PrinterIndex.I20) as SP)) = (0: U8) [unsigned, U8, 1]  // $16U.4((11: anvil.PrinterIndex.I20)) = (0: U8)
        *(($16U.4 + (12: SP)) + ((12: anvil.PrinterIndex.I20) as SP)) = (0: U8) [unsigned, U8, 1]  // $16U.4((12: anvil.PrinterIndex.I20)) = (0: U8)
        *(($16U.4 + (12: SP)) + ((13: anvil.PrinterIndex.I20) as SP)) = (0: U8) [unsigned, U8, 1]  // $16U.4((13: anvil.PrinterIndex.I20)) = (0: U8)
        *(($16U.4 + (12: SP)) + ((14: anvil.PrinterIndex.I20) as SP)) = (0: U8) [unsigned, U8, 1]  // $16U.4((14: anvil.PrinterIndex.I20)) = (0: U8)
        *(($16U.4 + (12: SP)) + ((15: anvil.PrinterIndex.I20) as SP)) = (0: U8) [unsigned, U8, 1]  // $16U.4((15: anvil.PrinterIndex.I20)) = (0: U8)
        *(($16U.4 + (12: SP)) + ((16: anvil.PrinterIndex.I20) as SP)) = (0: U8) [unsigned, U8, 1]  // $16U.4((16: anvil.PrinterIndex.I20)) = (0: U8)
        *(($16U.4 + (12: SP)) + ((17: anvil.PrinterIndex.I20) as SP)) = (0: U8) [unsigned, U8, 1]  // $16U.4((17: anvil.PrinterIndex.I20)) = (0: U8)
        *(($16U.4 + (12: SP)) + ((18: anvil.PrinterIndex.I20) as SP)) = (0: U8) [unsigned, U8, 1]  // $16U.4((18: anvil.PrinterIndex.I20)) = (0: U8)
        *(($16U.4 + (12: SP)) + ((19: anvil.PrinterIndex.I20) as SP)) = (0: U8) [unsigned, U8, 1]  // $16U.4((19: anvil.PrinterIndex.I20)) = (0: U8)
        goto .127
        */


        val __tmp_4189 = ((generalRegFilesU16(4.U) + 12.U(16.W)) + 0.S(8.W).asUInt.pad(16))
        val __tmp_4190 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4189 + 0.U) := __tmp_4190(7, 0)

        val __tmp_4191 = ((generalRegFilesU16(4.U) + 12.U(16.W)) + 1.S(8.W).asUInt.pad(16))
        val __tmp_4192 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4191 + 0.U) := __tmp_4192(7, 0)

        val __tmp_4193 = ((generalRegFilesU16(4.U) + 12.U(16.W)) + 2.S(8.W).asUInt.pad(16))
        val __tmp_4194 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4193 + 0.U) := __tmp_4194(7, 0)

        val __tmp_4195 = ((generalRegFilesU16(4.U) + 12.U(16.W)) + 3.S(8.W).asUInt.pad(16))
        val __tmp_4196 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4195 + 0.U) := __tmp_4196(7, 0)

        val __tmp_4197 = ((generalRegFilesU16(4.U) + 12.U(16.W)) + 4.S(8.W).asUInt.pad(16))
        val __tmp_4198 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4197 + 0.U) := __tmp_4198(7, 0)

        val __tmp_4199 = ((generalRegFilesU16(4.U) + 12.U(16.W)) + 5.S(8.W).asUInt.pad(16))
        val __tmp_4200 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4199 + 0.U) := __tmp_4200(7, 0)

        val __tmp_4201 = ((generalRegFilesU16(4.U) + 12.U(16.W)) + 6.S(8.W).asUInt.pad(16))
        val __tmp_4202 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4201 + 0.U) := __tmp_4202(7, 0)

        val __tmp_4203 = ((generalRegFilesU16(4.U) + 12.U(16.W)) + 7.S(8.W).asUInt.pad(16))
        val __tmp_4204 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4203 + 0.U) := __tmp_4204(7, 0)

        val __tmp_4205 = ((generalRegFilesU16(4.U) + 12.U(16.W)) + 8.S(8.W).asUInt.pad(16))
        val __tmp_4206 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4205 + 0.U) := __tmp_4206(7, 0)

        val __tmp_4207 = ((generalRegFilesU16(4.U) + 12.U(16.W)) + 9.S(8.W).asUInt.pad(16))
        val __tmp_4208 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4207 + 0.U) := __tmp_4208(7, 0)

        val __tmp_4209 = ((generalRegFilesU16(4.U) + 12.U(16.W)) + 10.S(8.W).asUInt.pad(16))
        val __tmp_4210 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4209 + 0.U) := __tmp_4210(7, 0)

        val __tmp_4211 = ((generalRegFilesU16(4.U) + 12.U(16.W)) + 11.S(8.W).asUInt.pad(16))
        val __tmp_4212 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4211 + 0.U) := __tmp_4212(7, 0)

        val __tmp_4213 = ((generalRegFilesU16(4.U) + 12.U(16.W)) + 12.S(8.W).asUInt.pad(16))
        val __tmp_4214 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4213 + 0.U) := __tmp_4214(7, 0)

        val __tmp_4215 = ((generalRegFilesU16(4.U) + 12.U(16.W)) + 13.S(8.W).asUInt.pad(16))
        val __tmp_4216 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4215 + 0.U) := __tmp_4216(7, 0)

        val __tmp_4217 = ((generalRegFilesU16(4.U) + 12.U(16.W)) + 14.S(8.W).asUInt.pad(16))
        val __tmp_4218 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4217 + 0.U) := __tmp_4218(7, 0)

        val __tmp_4219 = ((generalRegFilesU16(4.U) + 12.U(16.W)) + 15.S(8.W).asUInt.pad(16))
        val __tmp_4220 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4219 + 0.U) := __tmp_4220(7, 0)

        val __tmp_4221 = ((generalRegFilesU16(4.U) + 12.U(16.W)) + 16.S(8.W).asUInt.pad(16))
        val __tmp_4222 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4221 + 0.U) := __tmp_4222(7, 0)

        val __tmp_4223 = ((generalRegFilesU16(4.U) + 12.U(16.W)) + 17.S(8.W).asUInt.pad(16))
        val __tmp_4224 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4223 + 0.U) := __tmp_4224(7, 0)

        val __tmp_4225 = ((generalRegFilesU16(4.U) + 12.U(16.W)) + 18.S(8.W).asUInt.pad(16))
        val __tmp_4226 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4225 + 0.U) := __tmp_4226(7, 0)

        val __tmp_4227 = ((generalRegFilesU16(4.U) + 12.U(16.W)) + 19.S(8.W).asUInt.pad(16))
        val __tmp_4228 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_4227 + 0.U) := __tmp_4228(7, 0)

        CP := 127.U
      }

      is(127.U) {
        /*
        (SP + (30: SP)) [MS[anvil.PrinterIndex.I20, U8], 34]  <-  $16U.4 [MS[anvil.PrinterIndex.I20, U8], ((*($16U.4 + (4: SP)) as SP) + (12: SP))]  // buff = $16U.4
        goto .128
        */


        val __tmp_4229 = (SP + 30.U(16.W))
        val __tmp_4230 = generalRegFilesU16(4.U)
        val __tmp_4231 = (Cat(
           arrayRegFiles((generalRegFilesU16(4.U) + 4.U(16.W)) + 7.U),
           arrayRegFiles((generalRegFilesU16(4.U) + 4.U(16.W)) + 6.U),
           arrayRegFiles((generalRegFilesU16(4.U) + 4.U(16.W)) + 5.U),
           arrayRegFiles((generalRegFilesU16(4.U) + 4.U(16.W)) + 4.U),
           arrayRegFiles((generalRegFilesU16(4.U) + 4.U(16.W)) + 3.U),
           arrayRegFiles((generalRegFilesU16(4.U) + 4.U(16.W)) + 2.U),
           arrayRegFiles((generalRegFilesU16(4.U) + 4.U(16.W)) + 1.U),
           arrayRegFiles((generalRegFilesU16(4.U) + 4.U(16.W)) + 0.U)
         ).asSInt.asUInt.pad(16) + 12.U(16.W))

        when(Idx <= __tmp_4231) {
          arrayRegFiles(__tmp_4229 + Idx + 0.U) := arrayRegFiles(__tmp_4230 + Idx + 0.U)
          arrayRegFiles(__tmp_4229 + Idx + 1.U) := arrayRegFiles(__tmp_4230 + Idx + 1.U)
          arrayRegFiles(__tmp_4229 + Idx + 2.U) := arrayRegFiles(__tmp_4230 + Idx + 2.U)
          arrayRegFiles(__tmp_4229 + Idx + 3.U) := arrayRegFiles(__tmp_4230 + Idx + 3.U)
          arrayRegFiles(__tmp_4229 + Idx + 4.U) := arrayRegFiles(__tmp_4230 + Idx + 4.U)
          arrayRegFiles(__tmp_4229 + Idx + 5.U) := arrayRegFiles(__tmp_4230 + Idx + 5.U)
          arrayRegFiles(__tmp_4229 + Idx + 6.U) := arrayRegFiles(__tmp_4230 + Idx + 6.U)
          arrayRegFiles(__tmp_4229 + Idx + 7.U) := arrayRegFiles(__tmp_4230 + Idx + 7.U)
          Idx := Idx + 8.U
          LeftByteRounds := __tmp_4231 - Idx
        } .elsewhen(IdxLeftByteRounds < LeftByteRounds) {
          val __tmp_4232 = Idx - 8.U
          arrayRegFiles(__tmp_4229 + __tmp_4232 + IdxLeftByteRounds) := arrayRegFiles(__tmp_4230 + __tmp_4232 + IdxLeftByteRounds)
          IdxLeftByteRounds := IdxLeftByteRounds + 1.U
        } .otherwise {
          Idx := 0.U
          IdxLeftByteRounds := 0.U
          LeftByteRounds := 0.U
          CP := 128.U
        }


      }

      is(128.U) {
        /*
        unalloc $new@[167,16].100A3CBA: MS[anvil.PrinterIndex.I20, U8] [@64, 34]
        goto .129
        */


        CP := 129.U
      }

      is(129.U) {
        /*
        decl i: anvil.PrinterIndex.I20 @$0
        $8S.0 = (0: anvil.PrinterIndex.I20)
        goto .130
        */



        generalRegFilesS8(0.U) := 0.S(8.W)

        CP := 130.U
      }

      is(130.U) {
        /*
        decl neg: B @$0
        $1U.1 = ($64S.0 < (0: S64))
        goto .131
        */



        generalRegFilesU1(1.U) := (generalRegFilesS64(0.U) < 0.S(64.W)).asUInt

        CP := 131.U
      }

      is(131.U) {
        /*
        $1U.0 = $1U.1
        goto .132
        */



        generalRegFilesU1(0.U) := generalRegFilesU1(1.U)

        CP := 132.U
      }

      is(132.U) {
        /*
        decl m: S64 @$1
        goto .133
        */


        CP := 133.U
      }

      is(133.U) {
        /*
        if $1U.0 goto .134 else goto .136
        */


        CP := Mux((generalRegFilesU1(0.U).asUInt) === 1.U, 134.U, 136.U)
      }

      is(134.U) {
        /*
        $64S.4 = -($64S.0)
        goto .135
        */



        generalRegFilesS64(4.U) := -generalRegFilesS64(0.U)

        CP := 135.U
      }

      is(135.U) {
        /*
        $64S.2 = $64S.4
        goto .138
        */



        generalRegFilesS64(2.U) := generalRegFilesS64(4.U)

        CP := 138.U
      }

      is(136.U) {
        /*
        $64S.5 = $64S.0
        goto .137
        */



        generalRegFilesS64(5.U) := generalRegFilesS64(0.U)

        CP := 137.U
      }

      is(137.U) {
        /*
        $64S.2 = $64S.5
        goto .138
        */



        generalRegFilesS64(2.U) := generalRegFilesS64(5.U)

        CP := 138.U
      }

      is(138.U) {
        /*
        $64S.1 = $64S.2
        goto .139
        */



        generalRegFilesS64(1.U) := generalRegFilesS64(2.U)

        CP := 139.U
      }

      is(139.U) {
        /*
        $64S.3 = $64S.1
        goto .140
        */



        generalRegFilesS64(3.U) := generalRegFilesS64(1.U)

        CP := 140.U
      }

      is(140.U) {
        /*
        $1U.1 = ($64S.3 > (0: S64))
        goto .141
        */



        generalRegFilesU1(1.U) := (generalRegFilesS64(3.U) > 0.S(64.W)).asUInt

        CP := 141.U
      }

      is(141.U) {
        /*
        if $1U.1 goto .142 else goto .171
        */


        CP := Mux((generalRegFilesU1(1.U).asUInt) === 1.U, 142.U, 171.U)
      }

      is(142.U) {
        /*
        $64S.3 = $64S.1
        goto .143
        */



        generalRegFilesS64(3.U) := generalRegFilesS64(1.U)

        CP := 143.U
      }

      is(143.U) {
        /*
        $64S.2 = ($64S.3 % (10: S64))
        goto .144
        */



        generalRegFilesS64(2.U) := (generalRegFilesS64(3.U) % 10.S(64.W))

        CP := 144.U
      }

      is(144.U) {
        /*
        switch ($64S.2)
          (0: S64): goto 145
          (1: S64): goto 147
          (2: S64): goto 149
          (3: S64): goto 151
          (4: S64): goto 153
          (5: S64): goto 155
          (6: S64): goto 157
          (7: S64): goto 159
          (8: S64): goto 161
          (9: S64): goto 163

        */


        val __tmp_4233 = generalRegFilesS64(2.U)

        switch(__tmp_4233) {

          is(0.S(64.W)) {
            CP := 145.U
          }


          is(1.S(64.W)) {
            CP := 147.U
          }


          is(2.S(64.W)) {
            CP := 149.U
          }


          is(3.S(64.W)) {
            CP := 151.U
          }


          is(4.S(64.W)) {
            CP := 153.U
          }


          is(5.S(64.W)) {
            CP := 155.U
          }


          is(6.S(64.W)) {
            CP := 157.U
          }


          is(7.S(64.W)) {
            CP := 159.U
          }


          is(8.S(64.W)) {
            CP := 161.U
          }


          is(9.S(64.W)) {
            CP := 163.U
          }

        }

      }

      is(145.U) {
        /*
        $16U.4 = (SP + (30: SP))
        $8S.2 = $8S.0
        goto .146
        */



        generalRegFilesU16(4.U) := (SP + 30.U(16.W))


        generalRegFilesS8(2.U) := generalRegFilesS8(0.U)

        CP := 146.U
      }

      is(146.U) {
        /*
        *(($16U.4 + (12: SP)) + ($8S.2 as SP)) = (48: U8) [unsigned, U8, 1]  // $16U.4($8S.2) = (48: U8)
        goto .165
        */


        val __tmp_4234 = ((generalRegFilesU16(4.U) + 12.U(16.W)) + generalRegFilesS8(2.U).asUInt.pad(16))
        val __tmp_4235 = (48.U(8.W)).asUInt
        arrayRegFiles(__tmp_4234 + 0.U) := __tmp_4235(7, 0)

        CP := 165.U
      }

      is(147.U) {
        /*
        $16U.4 = (SP + (30: SP))
        $8S.2 = $8S.0
        goto .148
        */



        generalRegFilesU16(4.U) := (SP + 30.U(16.W))


        generalRegFilesS8(2.U) := generalRegFilesS8(0.U)

        CP := 148.U
      }

      is(148.U) {
        /*
        *(($16U.4 + (12: SP)) + ($8S.2 as SP)) = (49: U8) [unsigned, U8, 1]  // $16U.4($8S.2) = (49: U8)
        goto .165
        */


        val __tmp_4236 = ((generalRegFilesU16(4.U) + 12.U(16.W)) + generalRegFilesS8(2.U).asUInt.pad(16))
        val __tmp_4237 = (49.U(8.W)).asUInt
        arrayRegFiles(__tmp_4236 + 0.U) := __tmp_4237(7, 0)

        CP := 165.U
      }

      is(149.U) {
        /*
        $16U.4 = (SP + (30: SP))
        $8S.2 = $8S.0
        goto .150
        */



        generalRegFilesU16(4.U) := (SP + 30.U(16.W))


        generalRegFilesS8(2.U) := generalRegFilesS8(0.U)

        CP := 150.U
      }

      is(150.U) {
        /*
        *(($16U.4 + (12: SP)) + ($8S.2 as SP)) = (50: U8) [unsigned, U8, 1]  // $16U.4($8S.2) = (50: U8)
        goto .165
        */


        val __tmp_4238 = ((generalRegFilesU16(4.U) + 12.U(16.W)) + generalRegFilesS8(2.U).asUInt.pad(16))
        val __tmp_4239 = (50.U(8.W)).asUInt
        arrayRegFiles(__tmp_4238 + 0.U) := __tmp_4239(7, 0)

        CP := 165.U
      }

      is(151.U) {
        /*
        $16U.4 = (SP + (30: SP))
        $8S.2 = $8S.0
        goto .152
        */



        generalRegFilesU16(4.U) := (SP + 30.U(16.W))


        generalRegFilesS8(2.U) := generalRegFilesS8(0.U)

        CP := 152.U
      }

      is(152.U) {
        /*
        *(($16U.4 + (12: SP)) + ($8S.2 as SP)) = (51: U8) [unsigned, U8, 1]  // $16U.4($8S.2) = (51: U8)
        goto .165
        */


        val __tmp_4240 = ((generalRegFilesU16(4.U) + 12.U(16.W)) + generalRegFilesS8(2.U).asUInt.pad(16))
        val __tmp_4241 = (51.U(8.W)).asUInt
        arrayRegFiles(__tmp_4240 + 0.U) := __tmp_4241(7, 0)

        CP := 165.U
      }

      is(153.U) {
        /*
        $16U.4 = (SP + (30: SP))
        $8S.2 = $8S.0
        goto .154
        */



        generalRegFilesU16(4.U) := (SP + 30.U(16.W))


        generalRegFilesS8(2.U) := generalRegFilesS8(0.U)

        CP := 154.U
      }

      is(154.U) {
        /*
        *(($16U.4 + (12: SP)) + ($8S.2 as SP)) = (52: U8) [unsigned, U8, 1]  // $16U.4($8S.2) = (52: U8)
        goto .165
        */


        val __tmp_4242 = ((generalRegFilesU16(4.U) + 12.U(16.W)) + generalRegFilesS8(2.U).asUInt.pad(16))
        val __tmp_4243 = (52.U(8.W)).asUInt
        arrayRegFiles(__tmp_4242 + 0.U) := __tmp_4243(7, 0)

        CP := 165.U
      }

      is(155.U) {
        /*
        $16U.4 = (SP + (30: SP))
        $8S.2 = $8S.0
        goto .156
        */



        generalRegFilesU16(4.U) := (SP + 30.U(16.W))


        generalRegFilesS8(2.U) := generalRegFilesS8(0.U)

        CP := 156.U
      }

      is(156.U) {
        /*
        *(($16U.4 + (12: SP)) + ($8S.2 as SP)) = (53: U8) [unsigned, U8, 1]  // $16U.4($8S.2) = (53: U8)
        goto .165
        */


        val __tmp_4244 = ((generalRegFilesU16(4.U) + 12.U(16.W)) + generalRegFilesS8(2.U).asUInt.pad(16))
        val __tmp_4245 = (53.U(8.W)).asUInt
        arrayRegFiles(__tmp_4244 + 0.U) := __tmp_4245(7, 0)

        CP := 165.U
      }

      is(157.U) {
        /*
        $16U.4 = (SP + (30: SP))
        $8S.2 = $8S.0
        goto .158
        */



        generalRegFilesU16(4.U) := (SP + 30.U(16.W))


        generalRegFilesS8(2.U) := generalRegFilesS8(0.U)

        CP := 158.U
      }

      is(158.U) {
        /*
        *(($16U.4 + (12: SP)) + ($8S.2 as SP)) = (54: U8) [unsigned, U8, 1]  // $16U.4($8S.2) = (54: U8)
        goto .165
        */


        val __tmp_4246 = ((generalRegFilesU16(4.U) + 12.U(16.W)) + generalRegFilesS8(2.U).asUInt.pad(16))
        val __tmp_4247 = (54.U(8.W)).asUInt
        arrayRegFiles(__tmp_4246 + 0.U) := __tmp_4247(7, 0)

        CP := 165.U
      }

      is(159.U) {
        /*
        $16U.4 = (SP + (30: SP))
        $8S.2 = $8S.0
        goto .160
        */



        generalRegFilesU16(4.U) := (SP + 30.U(16.W))


        generalRegFilesS8(2.U) := generalRegFilesS8(0.U)

        CP := 160.U
      }

      is(160.U) {
        /*
        *(($16U.4 + (12: SP)) + ($8S.2 as SP)) = (55: U8) [unsigned, U8, 1]  // $16U.4($8S.2) = (55: U8)
        goto .165
        */


        val __tmp_4248 = ((generalRegFilesU16(4.U) + 12.U(16.W)) + generalRegFilesS8(2.U).asUInt.pad(16))
        val __tmp_4249 = (55.U(8.W)).asUInt
        arrayRegFiles(__tmp_4248 + 0.U) := __tmp_4249(7, 0)

        CP := 165.U
      }

      is(161.U) {
        /*
        $16U.4 = (SP + (30: SP))
        $8S.2 = $8S.0
        goto .162
        */



        generalRegFilesU16(4.U) := (SP + 30.U(16.W))


        generalRegFilesS8(2.U) := generalRegFilesS8(0.U)

        CP := 162.U
      }

      is(162.U) {
        /*
        *(($16U.4 + (12: SP)) + ($8S.2 as SP)) = (56: U8) [unsigned, U8, 1]  // $16U.4($8S.2) = (56: U8)
        goto .165
        */


        val __tmp_4250 = ((generalRegFilesU16(4.U) + 12.U(16.W)) + generalRegFilesS8(2.U).asUInt.pad(16))
        val __tmp_4251 = (56.U(8.W)).asUInt
        arrayRegFiles(__tmp_4250 + 0.U) := __tmp_4251(7, 0)

        CP := 165.U
      }

      is(163.U) {
        /*
        $16U.4 = (SP + (30: SP))
        $8S.2 = $8S.0
        goto .164
        */



        generalRegFilesU16(4.U) := (SP + 30.U(16.W))


        generalRegFilesS8(2.U) := generalRegFilesS8(0.U)

        CP := 164.U
      }

      is(164.U) {
        /*
        *(($16U.4 + (12: SP)) + ($8S.2 as SP)) = (57: U8) [unsigned, U8, 1]  // $16U.4($8S.2) = (57: U8)
        goto .165
        */


        val __tmp_4252 = ((generalRegFilesU16(4.U) + 12.U(16.W)) + generalRegFilesS8(2.U).asUInt.pad(16))
        val __tmp_4253 = (57.U(8.W)).asUInt
        arrayRegFiles(__tmp_4252 + 0.U) := __tmp_4253(7, 0)

        CP := 165.U
      }

      is(165.U) {
        /*
        $64S.3 = $64S.1
        goto .166
        */



        generalRegFilesS64(3.U) := generalRegFilesS64(1.U)

        CP := 166.U
      }

      is(166.U) {
        /*
        $64S.2 = ($64S.3 / (10: S64))
        goto .167
        */



        generalRegFilesS64(2.U) := (generalRegFilesS64(3.U) / 10.S(64.W))

        CP := 167.U
      }

      is(167.U) {
        /*
        $64S.1 = $64S.2
        goto .168
        */



        generalRegFilesS64(1.U) := generalRegFilesS64(2.U)

        CP := 168.U
      }

      is(168.U) {
        /*
        $8S.3 = $8S.0
        goto .169
        */



        generalRegFilesS8(3.U) := generalRegFilesS8(0.U)

        CP := 169.U
      }

      is(169.U) {
        /*
        $8S.2 = ($8S.3 + (1: anvil.PrinterIndex.I20))
        goto .170
        */



        generalRegFilesS8(2.U) := (generalRegFilesS8(3.U) + 1.S(8.W))

        CP := 170.U
      }

      is(170.U) {
        /*
        $8S.0 = $8S.2
        goto .139
        */



        generalRegFilesS8(0.U) := generalRegFilesS8(2.U)

        CP := 139.U
      }

      is(171.U) {
        /*
        $1U.2 = $1U.0
        undecl neg: B @$0
        goto .172
        */



        generalRegFilesU1(2.U) := generalRegFilesU1(0.U)

        CP := 172.U
      }

      is(172.U) {
        /*
        if $1U.2 goto .173 else goto .178
        */


        CP := Mux((generalRegFilesU1(2.U).asUInt) === 1.U, 173.U, 178.U)
      }

      is(173.U) {
        /*
        $16U.4 = (SP + (30: SP))
        $8S.2 = $8S.0
        goto .174
        */



        generalRegFilesU16(4.U) := (SP + 30.U(16.W))


        generalRegFilesS8(2.U) := generalRegFilesS8(0.U)

        CP := 174.U
      }

      is(174.U) {
        /*
        *(($16U.4 + (12: SP)) + ($8S.2 as SP)) = (45: U8) [unsigned, U8, 1]  // $16U.4($8S.2) = (45: U8)
        goto .175
        */


        val __tmp_4254 = ((generalRegFilesU16(4.U) + 12.U(16.W)) + generalRegFilesS8(2.U).asUInt.pad(16))
        val __tmp_4255 = (45.U(8.W)).asUInt
        arrayRegFiles(__tmp_4254 + 0.U) := __tmp_4255(7, 0)

        CP := 175.U
      }

      is(175.U) {
        /*
        $8S.3 = $8S.0
        goto .176
        */



        generalRegFilesS8(3.U) := generalRegFilesS8(0.U)

        CP := 176.U
      }

      is(176.U) {
        /*
        $8S.2 = ($8S.3 + (1: anvil.PrinterIndex.I20))
        goto .177
        */



        generalRegFilesS8(2.U) := (generalRegFilesS8(3.U) + 1.S(8.W))

        CP := 177.U
      }

      is(177.U) {
        /*
        $8S.0 = $8S.2
        goto .178
        */



        generalRegFilesS8(0.U) := generalRegFilesS8(2.U)

        CP := 178.U
      }

      is(178.U) {
        /*
        decl j: anvil.PrinterIndex.I20 @$1
        $8S.3 = $8S.0
        undecl i: anvil.PrinterIndex.I20 @$0
        goto .179
        */



        generalRegFilesS8(3.U) := generalRegFilesS8(0.U)

        CP := 179.U
      }

      is(179.U) {
        /*
        $8S.2 = ($8S.3 - (1: anvil.PrinterIndex.I20))
        goto .180
        */



        generalRegFilesS8(2.U) := (generalRegFilesS8(3.U) - 1.S(8.W))

        CP := 180.U
      }

      is(180.U) {
        /*
        $8S.1 = $8S.2
        goto .181
        */



        generalRegFilesS8(1.U) := generalRegFilesS8(2.U)

        CP := 181.U
      }

      is(181.U) {
        /*
        decl idx: anvil.PrinterIndex.U @$2
        $64U.6 = $64U.0
        goto .182
        */



        generalRegFilesU64(6.U) := generalRegFilesU64(0.U)

        CP := 182.U
      }

      is(182.U) {
        /*
        $64U.2 = $64U.6
        goto .183
        */



        generalRegFilesU64(2.U) := generalRegFilesU64(6.U)

        CP := 183.U
      }

      is(183.U) {
        /*
        decl r: U64 @$3
        $64U.3 = (0: U64)
        goto .184
        */



        generalRegFilesU64(3.U) := 0.U(64.W)

        CP := 184.U
      }

      is(184.U) {
        /*
        $8S.3 = $8S.1
        goto .185
        */



        generalRegFilesS8(3.U) := generalRegFilesS8(1.U)

        CP := 185.U
      }

      is(185.U) {
        /*
        $1U.1 = ($8S.3 >= (0: anvil.PrinterIndex.I20))
        goto .186
        */



        generalRegFilesU1(1.U) := (generalRegFilesS8(3.U) >= 0.S(8.W)).asUInt

        CP := 186.U
      }

      is(186.U) {
        /*
        if $1U.1 goto .187 else goto .201
        */


        CP := Mux((generalRegFilesU1(1.U).asUInt) === 1.U, 187.U, 201.U)
      }

      is(187.U) {
        /*
        $16U.3 = $16U.0
        $64U.4 = $64U.2
        $64U.9 = $64U.1
        goto .188
        */



        generalRegFilesU16(3.U) := generalRegFilesU16(0.U)


        generalRegFilesU64(4.U) := generalRegFilesU64(2.U)


        generalRegFilesU64(9.U) := generalRegFilesU64(1.U)

        CP := 188.U
      }

      is(188.U) {
        /*
        $64U.8 = ($64U.4 & $64U.9)
        goto .189
        */



        generalRegFilesU64(8.U) := (generalRegFilesU64(4.U) & generalRegFilesU64(9.U))

        CP := 189.U
      }

      is(189.U) {
        /*
        $16U.5 = (SP + (30: SP))
        $8S.4 = $8S.1
        goto .190
        */



        generalRegFilesU16(5.U) := (SP + 30.U(16.W))


        generalRegFilesS8(4.U) := generalRegFilesS8(1.U)

        CP := 190.U
      }

      is(190.U) {
        /*
        $8U.0 = *(($16U.5 + (12: SP)) + ($8S.4 as SP)) [unsigned, U8, 1]  // $8U.0 = $16U.5($8S.4)
        goto .191
        */


        val __tmp_4256 = (((generalRegFilesU16(5.U) + 12.U(16.W)) + generalRegFilesS8(4.U).asUInt.pad(16))).asUInt
        generalRegFilesU8(0.U) := Cat(
          arrayRegFiles(__tmp_4256 + 0.U)
        )

        CP := 191.U
      }

      is(191.U) {
        /*
        *(($16U.3 + (12: SP)) + ($64U.8 as SP)) = $8U.0 [unsigned, U8, 1]  // $16U.3($64U.8) = $8U.0
        goto .192
        */


        val __tmp_4257 = ((generalRegFilesU16(3.U) + 12.U(16.W)) + generalRegFilesU64(8.U).asUInt.pad(16))
        val __tmp_4258 = (generalRegFilesU8(0.U)).asUInt
        arrayRegFiles(__tmp_4257 + 0.U) := __tmp_4258(7, 0)

        CP := 192.U
      }

      is(192.U) {
        /*
        $8S.3 = $8S.1
        goto .193
        */



        generalRegFilesS8(3.U) := generalRegFilesS8(1.U)

        CP := 193.U
      }

      is(193.U) {
        /*
        $8S.2 = ($8S.3 - (1: anvil.PrinterIndex.I20))
        goto .194
        */



        generalRegFilesS8(2.U) := (generalRegFilesS8(3.U) - 1.S(8.W))

        CP := 194.U
      }

      is(194.U) {
        /*
        $8S.1 = $8S.2
        goto .195
        */



        generalRegFilesS8(1.U) := generalRegFilesS8(2.U)

        CP := 195.U
      }

      is(195.U) {
        /*
        $64U.6 = $64U.2
        goto .196
        */



        generalRegFilesU64(6.U) := generalRegFilesU64(2.U)

        CP := 196.U
      }

      is(196.U) {
        /*
        $64U.4 = ($64U.6 + (1: anvil.PrinterIndex.U))
        goto .197
        */



        generalRegFilesU64(4.U) := (generalRegFilesU64(6.U) + 1.U(64.W))

        CP := 197.U
      }

      is(197.U) {
        /*
        $64U.2 = $64U.4
        goto .198
        */



        generalRegFilesU64(2.U) := generalRegFilesU64(4.U)

        CP := 198.U
      }

      is(198.U) {
        /*
        $64U.7 = $64U.3
        goto .199
        */



        generalRegFilesU64(7.U) := generalRegFilesU64(3.U)

        CP := 199.U
      }

      is(199.U) {
        /*
        $64U.5 = ($64U.7 + (1: U64))
        goto .200
        */



        generalRegFilesU64(5.U) := (generalRegFilesU64(7.U) + 1.U(64.W))

        CP := 200.U
      }

      is(200.U) {
        /*
        $64U.3 = $64U.5
        goto .184
        */



        generalRegFilesU64(3.U) := generalRegFilesU64(5.U)

        CP := 184.U
      }

      is(201.U) {
        /*
        $64U.7 = $64U.3
        undecl r: U64 @$3
        goto .202
        */



        generalRegFilesU64(7.U) := generalRegFilesU64(3.U)

        CP := 202.U
      }

      is(202.U) {
        /*
        **(SP + (2: SP)) = $64U.7 [unsigned, U64, 8]  // $res = $64U.7
        goto $ret@0
        */


        val __tmp_4259 = Cat(
          arrayRegFiles((SP + 2.U(16.W)) + 1.U),
          arrayRegFiles((SP + 2.U(16.W)) + 0.U)
        )
        val __tmp_4260 = (generalRegFilesU64(7.U)).asUInt
        arrayRegFiles(__tmp_4259 + 0.U) := __tmp_4260(7, 0)
        arrayRegFiles(__tmp_4259 + 1.U) := __tmp_4260(15, 8)
        arrayRegFiles(__tmp_4259 + 2.U) := __tmp_4260(23, 16)
        arrayRegFiles(__tmp_4259 + 3.U) := __tmp_4260(31, 24)
        arrayRegFiles(__tmp_4259 + 4.U) := __tmp_4260(39, 32)
        arrayRegFiles(__tmp_4259 + 5.U) := __tmp_4260(47, 40)
        arrayRegFiles(__tmp_4259 + 6.U) := __tmp_4260(55, 48)
        arrayRegFiles(__tmp_4259 + 7.U) := __tmp_4260(63, 56)

        CP := Cat(
          arrayRegFiles(SP + 0.U + 1.U),
          arrayRegFiles(SP + 0.U + 0.U)
        )

      }

      is(203.U) {
        /*
        $16U.0 = *(SP + (4: SP)) [unsigned, MS[anvil.PrinterIndex.U, U8], 2]  // buffer
        $64U.0 = *(SP + (6: SP)) [unsigned, anvil.PrinterIndex.U, 8]  // index
        $64U.1 = *(SP + (14: SP)) [unsigned, anvil.PrinterIndex.U, 8]  // mask
        $32U.0 = *(SP + (22: SP)) [unsigned, C, 4]  // c
        goto .204
        */


        val __tmp_4261 = ((SP + 4.U(16.W))).asUInt
        generalRegFilesU16(0.U) := Cat(
          arrayRegFiles(__tmp_4261 + 1.U),
          arrayRegFiles(__tmp_4261 + 0.U)
        )

        val __tmp_4262 = ((SP + 6.U(16.W))).asUInt
        generalRegFilesU64(0.U) := Cat(
          arrayRegFiles(__tmp_4262 + 7.U),
          arrayRegFiles(__tmp_4262 + 6.U),
          arrayRegFiles(__tmp_4262 + 5.U),
          arrayRegFiles(__tmp_4262 + 4.U),
          arrayRegFiles(__tmp_4262 + 3.U),
          arrayRegFiles(__tmp_4262 + 2.U),
          arrayRegFiles(__tmp_4262 + 1.U),
          arrayRegFiles(__tmp_4262 + 0.U)
        )

        val __tmp_4263 = ((SP + 14.U(16.W))).asUInt
        generalRegFilesU64(1.U) := Cat(
          arrayRegFiles(__tmp_4263 + 7.U),
          arrayRegFiles(__tmp_4263 + 6.U),
          arrayRegFiles(__tmp_4263 + 5.U),
          arrayRegFiles(__tmp_4263 + 4.U),
          arrayRegFiles(__tmp_4263 + 3.U),
          arrayRegFiles(__tmp_4263 + 2.U),
          arrayRegFiles(__tmp_4263 + 1.U),
          arrayRegFiles(__tmp_4263 + 0.U)
        )

        val __tmp_4264 = ((SP + 22.U(16.W))).asUInt
        generalRegFilesU32(0.U) := Cat(
          arrayRegFiles(__tmp_4264 + 3.U),
          arrayRegFiles(__tmp_4264 + 2.U),
          arrayRegFiles(__tmp_4264 + 1.U),
          arrayRegFiles(__tmp_4264 + 0.U)
        )

        CP := 204.U
      }

      is(204.U) {
        /*
        decl raw: U32 @$1
        $32U.2 = ($32U.0 as U32)
        goto .205
        */



        generalRegFilesU32(2.U) := generalRegFilesU32(0.U).asUInt

        CP := 205.U
      }

      is(205.U) {
        /*
        $32U.1 = $32U.2
        goto .206
        */



        generalRegFilesU32(1.U) := generalRegFilesU32(2.U)

        CP := 206.U
      }

      is(206.U) {
        /*
        $1U.0 = ((0: U32) <= $32U.1)
        goto .207
        */



        generalRegFilesU1(0.U) := (0.U(32.W) <= generalRegFilesU32(1.U)).asUInt

        CP := 207.U
      }

      is(207.U) {
        /*
        if $1U.0 goto .208 else goto .210
        */


        CP := Mux((generalRegFilesU1(0.U).asUInt) === 1.U, 208.U, 210.U)
      }

      is(208.U) {
        /*
        $1U.1 = ($32U.1 <= (127: U32))
        goto .209
        */



        generalRegFilesU1(1.U) := (generalRegFilesU32(1.U) <= 127.U(32.W)).asUInt

        CP := 209.U
      }

      is(209.U) {
        /*
        $1U.2 = $1U.1
        goto .211
        */



        generalRegFilesU1(2.U) := generalRegFilesU1(1.U)

        CP := 211.U
      }

      is(210.U) {
        /*
        $1U.2 = true
        goto .211
        */



        generalRegFilesU1(2.U) := 1.U

        CP := 211.U
      }

      is(211.U) {
        /*
        if $1U.2 goto .212 else goto .215
        */


        CP := Mux((generalRegFilesU1(2.U).asUInt) === 1.U, 212.U, 215.U)
      }

      is(212.U) {
        /*
        $16U.3 = $16U.0
        $64U.4 = ($64U.0 & $64U.1)
        $8U.0 = ($32U.1 as U8)
        undecl raw: U32 @$1
        goto .213
        */



        generalRegFilesU16(3.U) := generalRegFilesU16(0.U)


        generalRegFilesU64(4.U) := (generalRegFilesU64(0.U) & generalRegFilesU64(1.U))


        generalRegFilesU8(0.U) := generalRegFilesU32(1.U).asUInt.pad(8)

        CP := 213.U
      }

      is(213.U) {
        /*
        *(($16U.3 + (12: SP)) + ($64U.4 as SP)) = $8U.0 [unsigned, U8, 1]  // $16U.3($64U.4) = $8U.0
        goto .214
        */


        val __tmp_4265 = ((generalRegFilesU16(3.U) + 12.U(16.W)) + generalRegFilesU64(4.U).asUInt.pad(16))
        val __tmp_4266 = (generalRegFilesU8(0.U)).asUInt
        arrayRegFiles(__tmp_4265 + 0.U) := __tmp_4266(7, 0)

        CP := 214.U
      }

      is(214.U) {
        /*
        **(SP + (2: SP)) = (1: U64) [unsigned, U64, 8]  // $res = (1: U64)
        goto $ret@0
        */


        val __tmp_4267 = Cat(
          arrayRegFiles((SP + 2.U(16.W)) + 1.U),
          arrayRegFiles((SP + 2.U(16.W)) + 0.U)
        )
        val __tmp_4268 = (1.U(64.W)).asUInt
        arrayRegFiles(__tmp_4267 + 0.U) := __tmp_4268(7, 0)
        arrayRegFiles(__tmp_4267 + 1.U) := __tmp_4268(15, 8)
        arrayRegFiles(__tmp_4267 + 2.U) := __tmp_4268(23, 16)
        arrayRegFiles(__tmp_4267 + 3.U) := __tmp_4268(31, 24)
        arrayRegFiles(__tmp_4267 + 4.U) := __tmp_4268(39, 32)
        arrayRegFiles(__tmp_4267 + 5.U) := __tmp_4268(47, 40)
        arrayRegFiles(__tmp_4267 + 6.U) := __tmp_4268(55, 48)
        arrayRegFiles(__tmp_4267 + 7.U) := __tmp_4268(63, 56)

        CP := Cat(
          arrayRegFiles(SP + 0.U + 1.U),
          arrayRegFiles(SP + 0.U + 0.U)
        )

      }

      is(215.U) {
        /*
        $1U.0 = ((80: U32) <= $32U.1)
        goto .216
        */



        generalRegFilesU1(0.U) := (80.U(32.W) <= generalRegFilesU32(1.U)).asUInt

        CP := 216.U
      }

      is(216.U) {
        /*
        if $1U.0 goto .217 else goto .219
        */


        CP := Mux((generalRegFilesU1(0.U).asUInt) === 1.U, 217.U, 219.U)
      }

      is(217.U) {
        /*
        $1U.1 = ($32U.1 <= (2047: U32))
        goto .218
        */



        generalRegFilesU1(1.U) := (generalRegFilesU32(1.U) <= 2047.U(32.W)).asUInt

        CP := 218.U
      }

      is(218.U) {
        /*
        $1U.2 = $1U.1
        goto .220
        */



        generalRegFilesU1(2.U) := generalRegFilesU1(1.U)

        CP := 220.U
      }

      is(219.U) {
        /*
        $1U.2 = true
        goto .220
        */



        generalRegFilesU1(2.U) := 1.U

        CP := 220.U
      }

      is(220.U) {
        /*
        if $1U.2 goto .221 else goto .233
        */


        CP := Mux((generalRegFilesU1(2.U).asUInt) === 1.U, 221.U, 233.U)
      }

      is(221.U) {
        /*
        $16U.3 = $16U.0
        $64U.4 = ($64U.0 & $64U.1)
        $32U.3 = ($32U.1 >>> (6: U32))
        goto .222
        */



        generalRegFilesU16(3.U) := generalRegFilesU16(0.U)


        generalRegFilesU64(4.U) := (generalRegFilesU64(0.U) & generalRegFilesU64(1.U))


        generalRegFilesU32(3.U) := (((generalRegFilesU32(1.U)) >> 6.U(32.W)(4,0)))

        CP := 222.U
      }

      is(222.U) {
        /*
        $32U.4 = ($32U.3 & (31: U32))
        goto .223
        */



        generalRegFilesU32(4.U) := (generalRegFilesU32(3.U) & 31.U(32.W))

        CP := 223.U
      }

      is(223.U) {
        /*
        $32U.5 = ((192: U32) | $32U.4)
        goto .224
        */



        generalRegFilesU32(5.U) := (192.U(32.W) | generalRegFilesU32(4.U))

        CP := 224.U
      }

      is(224.U) {
        /*
        $8U.1 = ($32U.5 as U8)
        goto .225
        */



        generalRegFilesU8(1.U) := generalRegFilesU32(5.U).asUInt.pad(8)

        CP := 225.U
      }

      is(225.U) {
        /*
        *(($16U.3 + (12: SP)) + ($64U.4 as SP)) = $8U.1 [unsigned, U8, 1]  // $16U.3($64U.4) = $8U.1
        goto .226
        */


        val __tmp_4269 = ((generalRegFilesU16(3.U) + 12.U(16.W)) + generalRegFilesU64(4.U).asUInt.pad(16))
        val __tmp_4270 = (generalRegFilesU8(1.U)).asUInt
        arrayRegFiles(__tmp_4269 + 0.U) := __tmp_4270(7, 0)

        CP := 226.U
      }

      is(226.U) {
        /*
        $16U.3 = $16U.0
        $64U.3 = ($64U.0 + (1: anvil.PrinterIndex.U))
        goto .227
        */



        generalRegFilesU16(3.U) := generalRegFilesU16(0.U)


        generalRegFilesU64(3.U) := (generalRegFilesU64(0.U) + 1.U(64.W))

        CP := 227.U
      }

      is(227.U) {
        /*
        $64U.2 = ($64U.3 & $64U.1)
        goto .228
        */



        generalRegFilesU64(2.U) := (generalRegFilesU64(3.U) & generalRegFilesU64(1.U))

        CP := 228.U
      }

      is(228.U) {
        /*
        $32U.4 = ($32U.1 & (63: U32))
        undecl raw: U32 @$1
        goto .229
        */



        generalRegFilesU32(4.U) := (generalRegFilesU32(1.U) & 63.U(32.W))

        CP := 229.U
      }

      is(229.U) {
        /*
        $32U.5 = ((128: U32) | $32U.4)
        goto .230
        */



        generalRegFilesU32(5.U) := (128.U(32.W) | generalRegFilesU32(4.U))

        CP := 230.U
      }

      is(230.U) {
        /*
        $8U.1 = ($32U.5 as U8)
        goto .231
        */



        generalRegFilesU8(1.U) := generalRegFilesU32(5.U).asUInt.pad(8)

        CP := 231.U
      }

      is(231.U) {
        /*
        *(($16U.3 + (12: SP)) + ($64U.2 as SP)) = $8U.1 [unsigned, U8, 1]  // $16U.3($64U.2) = $8U.1
        goto .232
        */


        val __tmp_4271 = ((generalRegFilesU16(3.U) + 12.U(16.W)) + generalRegFilesU64(2.U).asUInt.pad(16))
        val __tmp_4272 = (generalRegFilesU8(1.U)).asUInt
        arrayRegFiles(__tmp_4271 + 0.U) := __tmp_4272(7, 0)

        CP := 232.U
      }

      is(232.U) {
        /*
        **(SP + (2: SP)) = (2: U64) [unsigned, U64, 8]  // $res = (2: U64)
        goto $ret@0
        */


        val __tmp_4273 = Cat(
          arrayRegFiles((SP + 2.U(16.W)) + 1.U),
          arrayRegFiles((SP + 2.U(16.W)) + 0.U)
        )
        val __tmp_4274 = (2.U(64.W)).asUInt
        arrayRegFiles(__tmp_4273 + 0.U) := __tmp_4274(7, 0)
        arrayRegFiles(__tmp_4273 + 1.U) := __tmp_4274(15, 8)
        arrayRegFiles(__tmp_4273 + 2.U) := __tmp_4274(23, 16)
        arrayRegFiles(__tmp_4273 + 3.U) := __tmp_4274(31, 24)
        arrayRegFiles(__tmp_4273 + 4.U) := __tmp_4274(39, 32)
        arrayRegFiles(__tmp_4273 + 5.U) := __tmp_4274(47, 40)
        arrayRegFiles(__tmp_4273 + 6.U) := __tmp_4274(55, 48)
        arrayRegFiles(__tmp_4273 + 7.U) := __tmp_4274(63, 56)

        CP := Cat(
          arrayRegFiles(SP + 0.U + 1.U),
          arrayRegFiles(SP + 0.U + 0.U)
        )

      }

      is(233.U) {
        /*
        $1U.0 = ((800: U32) <= $32U.1)
        goto .234
        */



        generalRegFilesU1(0.U) := (800.U(32.W) <= generalRegFilesU32(1.U)).asUInt

        CP := 234.U
      }

      is(234.U) {
        /*
        if $1U.0 goto .235 else goto .237
        */


        CP := Mux((generalRegFilesU1(0.U).asUInt) === 1.U, 235.U, 237.U)
      }

      is(235.U) {
        /*
        $1U.1 = ($32U.1 <= (65535: U32))
        goto .236
        */



        generalRegFilesU1(1.U) := (generalRegFilesU32(1.U) <= 65535.U(32.W)).asUInt

        CP := 236.U
      }

      is(236.U) {
        /*
        $1U.2 = $1U.1
        goto .238
        */



        generalRegFilesU1(2.U) := generalRegFilesU1(1.U)

        CP := 238.U
      }

      is(237.U) {
        /*
        $1U.2 = true
        goto .238
        */



        generalRegFilesU1(2.U) := 1.U

        CP := 238.U
      }

      is(238.U) {
        /*
        if $1U.2 goto .239 else goto .258
        */


        CP := Mux((generalRegFilesU1(2.U).asUInt) === 1.U, 239.U, 258.U)
      }

      is(239.U) {
        /*
        $16U.3 = $16U.0
        $64U.4 = ($64U.0 & $64U.1)
        $32U.3 = ($32U.1 >>> (12: U32))
        goto .240
        */



        generalRegFilesU16(3.U) := generalRegFilesU16(0.U)


        generalRegFilesU64(4.U) := (generalRegFilesU64(0.U) & generalRegFilesU64(1.U))


        generalRegFilesU32(3.U) := (((generalRegFilesU32(1.U)) >> 12.U(32.W)(4,0)))

        CP := 240.U
      }

      is(240.U) {
        /*
        $32U.4 = ($32U.3 & (15: U32))
        goto .241
        */



        generalRegFilesU32(4.U) := (generalRegFilesU32(3.U) & 15.U(32.W))

        CP := 241.U
      }

      is(241.U) {
        /*
        $32U.5 = ((224: U32) | $32U.4)
        goto .242
        */



        generalRegFilesU32(5.U) := (224.U(32.W) | generalRegFilesU32(4.U))

        CP := 242.U
      }

      is(242.U) {
        /*
        $8U.1 = ($32U.5 as U8)
        goto .243
        */



        generalRegFilesU8(1.U) := generalRegFilesU32(5.U).asUInt.pad(8)

        CP := 243.U
      }

      is(243.U) {
        /*
        *(($16U.3 + (12: SP)) + ($64U.4 as SP)) = $8U.1 [unsigned, U8, 1]  // $16U.3($64U.4) = $8U.1
        goto .244
        */


        val __tmp_4275 = ((generalRegFilesU16(3.U) + 12.U(16.W)) + generalRegFilesU64(4.U).asUInt.pad(16))
        val __tmp_4276 = (generalRegFilesU8(1.U)).asUInt
        arrayRegFiles(__tmp_4275 + 0.U) := __tmp_4276(7, 0)

        CP := 244.U
      }

      is(244.U) {
        /*
        $16U.3 = $16U.0
        $64U.3 = ($64U.0 + (1: anvil.PrinterIndex.U))
        goto .245
        */



        generalRegFilesU16(3.U) := generalRegFilesU16(0.U)


        generalRegFilesU64(3.U) := (generalRegFilesU64(0.U) + 1.U(64.W))

        CP := 245.U
      }

      is(245.U) {
        /*
        $64U.2 = ($64U.3 & $64U.1)
        goto .246
        */



        generalRegFilesU64(2.U) := (generalRegFilesU64(3.U) & generalRegFilesU64(1.U))

        CP := 246.U
      }

      is(246.U) {
        /*
        $32U.4 = ($32U.1 >>> (6: U32))
        goto .247
        */



        generalRegFilesU32(4.U) := (((generalRegFilesU32(1.U)) >> 6.U(32.W)(4,0)))

        CP := 247.U
      }

      is(247.U) {
        /*
        $32U.5 = ($32U.4 & (63: U32))
        goto .248
        */



        generalRegFilesU32(5.U) := (generalRegFilesU32(4.U) & 63.U(32.W))

        CP := 248.U
      }

      is(248.U) {
        /*
        $32U.6 = ((128: U32) | $32U.5)
        goto .249
        */



        generalRegFilesU32(6.U) := (128.U(32.W) | generalRegFilesU32(5.U))

        CP := 249.U
      }

      is(249.U) {
        /*
        $8U.2 = ($32U.6 as U8)
        goto .250
        */



        generalRegFilesU8(2.U) := generalRegFilesU32(6.U).asUInt.pad(8)

        CP := 250.U
      }

      is(250.U) {
        /*
        *(($16U.3 + (12: SP)) + ($64U.2 as SP)) = $8U.2 [unsigned, U8, 1]  // $16U.3($64U.2) = $8U.2
        goto .251
        */


        val __tmp_4277 = ((generalRegFilesU16(3.U) + 12.U(16.W)) + generalRegFilesU64(2.U).asUInt.pad(16))
        val __tmp_4278 = (generalRegFilesU8(2.U)).asUInt
        arrayRegFiles(__tmp_4277 + 0.U) := __tmp_4278(7, 0)

        CP := 251.U
      }

      is(251.U) {
        /*
        $16U.3 = $16U.0
        $64U.3 = ($64U.0 + (2: anvil.PrinterIndex.U))
        goto .252
        */



        generalRegFilesU16(3.U) := generalRegFilesU16(0.U)


        generalRegFilesU64(3.U) := (generalRegFilesU64(0.U) + 2.U(64.W))

        CP := 252.U
      }

      is(252.U) {
        /*
        $64U.2 = ($64U.3 & $64U.1)
        goto .253
        */



        generalRegFilesU64(2.U) := (generalRegFilesU64(3.U) & generalRegFilesU64(1.U))

        CP := 253.U
      }

      is(253.U) {
        /*
        $32U.4 = ($32U.1 & (63: U32))
        undecl raw: U32 @$1
        goto .254
        */



        generalRegFilesU32(4.U) := (generalRegFilesU32(1.U) & 63.U(32.W))

        CP := 254.U
      }

      is(254.U) {
        /*
        $32U.5 = ((128: U32) | $32U.4)
        goto .255
        */



        generalRegFilesU32(5.U) := (128.U(32.W) | generalRegFilesU32(4.U))

        CP := 255.U
      }

      is(255.U) {
        /*
        $8U.1 = ($32U.5 as U8)
        goto .256
        */



        generalRegFilesU8(1.U) := generalRegFilesU32(5.U).asUInt.pad(8)

        CP := 256.U
      }

      is(256.U) {
        /*
        *(($16U.3 + (12: SP)) + ($64U.2 as SP)) = $8U.1 [unsigned, U8, 1]  // $16U.3($64U.2) = $8U.1
        goto .257
        */


        val __tmp_4279 = ((generalRegFilesU16(3.U) + 12.U(16.W)) + generalRegFilesU64(2.U).asUInt.pad(16))
        val __tmp_4280 = (generalRegFilesU8(1.U)).asUInt
        arrayRegFiles(__tmp_4279 + 0.U) := __tmp_4280(7, 0)

        CP := 257.U
      }

      is(257.U) {
        /*
        **(SP + (2: SP)) = (3: U64) [unsigned, U64, 8]  // $res = (3: U64)
        goto $ret@0
        */


        val __tmp_4281 = Cat(
          arrayRegFiles((SP + 2.U(16.W)) + 1.U),
          arrayRegFiles((SP + 2.U(16.W)) + 0.U)
        )
        val __tmp_4282 = (3.U(64.W)).asUInt
        arrayRegFiles(__tmp_4281 + 0.U) := __tmp_4282(7, 0)
        arrayRegFiles(__tmp_4281 + 1.U) := __tmp_4282(15, 8)
        arrayRegFiles(__tmp_4281 + 2.U) := __tmp_4282(23, 16)
        arrayRegFiles(__tmp_4281 + 3.U) := __tmp_4282(31, 24)
        arrayRegFiles(__tmp_4281 + 4.U) := __tmp_4282(39, 32)
        arrayRegFiles(__tmp_4281 + 5.U) := __tmp_4282(47, 40)
        arrayRegFiles(__tmp_4281 + 6.U) := __tmp_4282(55, 48)
        arrayRegFiles(__tmp_4281 + 7.U) := __tmp_4282(63, 56)

        CP := Cat(
          arrayRegFiles(SP + 0.U + 1.U),
          arrayRegFiles(SP + 0.U + 0.U)
        )

      }

      is(258.U) {
        /*
        $16U.3 = $16U.0
        $64U.4 = ($64U.0 & $64U.1)
        $32U.3 = ($32U.1 >>> (18: U32))
        goto .259
        */



        generalRegFilesU16(3.U) := generalRegFilesU16(0.U)


        generalRegFilesU64(4.U) := (generalRegFilesU64(0.U) & generalRegFilesU64(1.U))


        generalRegFilesU32(3.U) := (((generalRegFilesU32(1.U)) >> 18.U(32.W)(4,0)))

        CP := 259.U
      }

      is(259.U) {
        /*
        $32U.4 = ($32U.3 & (7: U32))
        goto .260
        */



        generalRegFilesU32(4.U) := (generalRegFilesU32(3.U) & 7.U(32.W))

        CP := 260.U
      }

      is(260.U) {
        /*
        $32U.5 = ((240: U32) | $32U.4)
        goto .261
        */



        generalRegFilesU32(5.U) := (240.U(32.W) | generalRegFilesU32(4.U))

        CP := 261.U
      }

      is(261.U) {
        /*
        $8U.1 = ($32U.5 as U8)
        goto .262
        */



        generalRegFilesU8(1.U) := generalRegFilesU32(5.U).asUInt.pad(8)

        CP := 262.U
      }

      is(262.U) {
        /*
        *(($16U.3 + (12: SP)) + ($64U.4 as SP)) = $8U.1 [unsigned, U8, 1]  // $16U.3($64U.4) = $8U.1
        goto .263
        */


        val __tmp_4283 = ((generalRegFilesU16(3.U) + 12.U(16.W)) + generalRegFilesU64(4.U).asUInt.pad(16))
        val __tmp_4284 = (generalRegFilesU8(1.U)).asUInt
        arrayRegFiles(__tmp_4283 + 0.U) := __tmp_4284(7, 0)

        CP := 263.U
      }

      is(263.U) {
        /*
        $16U.3 = $16U.0
        $64U.3 = ($64U.0 + (1: anvil.PrinterIndex.U))
        goto .264
        */



        generalRegFilesU16(3.U) := generalRegFilesU16(0.U)


        generalRegFilesU64(3.U) := (generalRegFilesU64(0.U) + 1.U(64.W))

        CP := 264.U
      }

      is(264.U) {
        /*
        $64U.2 = ($64U.3 & $64U.1)
        goto .265
        */



        generalRegFilesU64(2.U) := (generalRegFilesU64(3.U) & generalRegFilesU64(1.U))

        CP := 265.U
      }

      is(265.U) {
        /*
        $32U.4 = ($32U.1 >>> (12: U32))
        goto .266
        */



        generalRegFilesU32(4.U) := (((generalRegFilesU32(1.U)) >> 12.U(32.W)(4,0)))

        CP := 266.U
      }

      is(266.U) {
        /*
        $32U.5 = ($32U.4 & (63: U32))
        goto .267
        */



        generalRegFilesU32(5.U) := (generalRegFilesU32(4.U) & 63.U(32.W))

        CP := 267.U
      }

      is(267.U) {
        /*
        $32U.6 = ((128: U32) | $32U.5)
        goto .268
        */



        generalRegFilesU32(6.U) := (128.U(32.W) | generalRegFilesU32(5.U))

        CP := 268.U
      }

      is(268.U) {
        /*
        $8U.2 = ($32U.6 as U8)
        goto .269
        */



        generalRegFilesU8(2.U) := generalRegFilesU32(6.U).asUInt.pad(8)

        CP := 269.U
      }

      is(269.U) {
        /*
        *(($16U.3 + (12: SP)) + ($64U.2 as SP)) = $8U.2 [unsigned, U8, 1]  // $16U.3($64U.2) = $8U.2
        goto .270
        */


        val __tmp_4285 = ((generalRegFilesU16(3.U) + 12.U(16.W)) + generalRegFilesU64(2.U).asUInt.pad(16))
        val __tmp_4286 = (generalRegFilesU8(2.U)).asUInt
        arrayRegFiles(__tmp_4285 + 0.U) := __tmp_4286(7, 0)

        CP := 270.U
      }

      is(270.U) {
        /*
        $16U.3 = $16U.0
        $64U.3 = ($64U.0 + (2: anvil.PrinterIndex.U))
        goto .271
        */



        generalRegFilesU16(3.U) := generalRegFilesU16(0.U)


        generalRegFilesU64(3.U) := (generalRegFilesU64(0.U) + 2.U(64.W))

        CP := 271.U
      }

      is(271.U) {
        /*
        $64U.2 = ($64U.3 & $64U.1)
        goto .272
        */



        generalRegFilesU64(2.U) := (generalRegFilesU64(3.U) & generalRegFilesU64(1.U))

        CP := 272.U
      }

      is(272.U) {
        /*
        $32U.4 = ($32U.1 >>> (6: U32))
        goto .273
        */



        generalRegFilesU32(4.U) := (((generalRegFilesU32(1.U)) >> 6.U(32.W)(4,0)))

        CP := 273.U
      }

      is(273.U) {
        /*
        $32U.5 = ($32U.4 & (63: U32))
        goto .274
        */



        generalRegFilesU32(5.U) := (generalRegFilesU32(4.U) & 63.U(32.W))

        CP := 274.U
      }

      is(274.U) {
        /*
        $32U.6 = ((128: U32) | $32U.5)
        goto .275
        */



        generalRegFilesU32(6.U) := (128.U(32.W) | generalRegFilesU32(5.U))

        CP := 275.U
      }

      is(275.U) {
        /*
        $8U.2 = ($32U.6 as U8)
        goto .276
        */



        generalRegFilesU8(2.U) := generalRegFilesU32(6.U).asUInt.pad(8)

        CP := 276.U
      }

      is(276.U) {
        /*
        *(($16U.3 + (12: SP)) + ($64U.2 as SP)) = $8U.2 [unsigned, U8, 1]  // $16U.3($64U.2) = $8U.2
        goto .277
        */


        val __tmp_4287 = ((generalRegFilesU16(3.U) + 12.U(16.W)) + generalRegFilesU64(2.U).asUInt.pad(16))
        val __tmp_4288 = (generalRegFilesU8(2.U)).asUInt
        arrayRegFiles(__tmp_4287 + 0.U) := __tmp_4288(7, 0)

        CP := 277.U
      }

      is(277.U) {
        /*
        $16U.3 = $16U.0
        $64U.3 = ($64U.0 + (3: anvil.PrinterIndex.U))
        goto .278
        */



        generalRegFilesU16(3.U) := generalRegFilesU16(0.U)


        generalRegFilesU64(3.U) := (generalRegFilesU64(0.U) + 3.U(64.W))

        CP := 278.U
      }

      is(278.U) {
        /*
        $64U.2 = ($64U.3 & $64U.1)
        goto .279
        */



        generalRegFilesU64(2.U) := (generalRegFilesU64(3.U) & generalRegFilesU64(1.U))

        CP := 279.U
      }

      is(279.U) {
        /*
        $32U.4 = ($32U.1 & (63: U32))
        undecl raw: U32 @$1
        goto .280
        */



        generalRegFilesU32(4.U) := (generalRegFilesU32(1.U) & 63.U(32.W))

        CP := 280.U
      }

      is(280.U) {
        /*
        $32U.5 = ((128: U32) | $32U.4)
        goto .281
        */



        generalRegFilesU32(5.U) := (128.U(32.W) | generalRegFilesU32(4.U))

        CP := 281.U
      }

      is(281.U) {
        /*
        $8U.1 = ($32U.5 as U8)
        goto .282
        */



        generalRegFilesU8(1.U) := generalRegFilesU32(5.U).asUInt.pad(8)

        CP := 282.U
      }

      is(282.U) {
        /*
        *(($16U.3 + (12: SP)) + ($64U.2 as SP)) = $8U.1 [unsigned, U8, 1]  // $16U.3($64U.2) = $8U.1
        goto .283
        */


        val __tmp_4289 = ((generalRegFilesU16(3.U) + 12.U(16.W)) + generalRegFilesU64(2.U).asUInt.pad(16))
        val __tmp_4290 = (generalRegFilesU8(1.U)).asUInt
        arrayRegFiles(__tmp_4289 + 0.U) := __tmp_4290(7, 0)

        CP := 283.U
      }

      is(283.U) {
        /*
        **(SP + (2: SP)) = (4: U64) [unsigned, U64, 8]  // $res = (4: U64)
        goto $ret@0
        */


        val __tmp_4291 = Cat(
          arrayRegFiles((SP + 2.U(16.W)) + 1.U),
          arrayRegFiles((SP + 2.U(16.W)) + 0.U)
        )
        val __tmp_4292 = (4.U(64.W)).asUInt
        arrayRegFiles(__tmp_4291 + 0.U) := __tmp_4292(7, 0)
        arrayRegFiles(__tmp_4291 + 1.U) := __tmp_4292(15, 8)
        arrayRegFiles(__tmp_4291 + 2.U) := __tmp_4292(23, 16)
        arrayRegFiles(__tmp_4291 + 3.U) := __tmp_4292(31, 24)
        arrayRegFiles(__tmp_4291 + 4.U) := __tmp_4292(39, 32)
        arrayRegFiles(__tmp_4291 + 5.U) := __tmp_4292(47, 40)
        arrayRegFiles(__tmp_4291 + 6.U) := __tmp_4292(55, 48)
        arrayRegFiles(__tmp_4291 + 7.U) := __tmp_4292(63, 56)

        CP := Cat(
          arrayRegFiles(SP + 0.U + 1.U),
          arrayRegFiles(SP + 0.U + 0.U)
        )

      }

      is(284.U) {
        /*
        $16U.0 = *(SP + (4: SP)) [unsigned, MS[anvil.PrinterIndex.U, U8], 2]  // buffer
        $64U.0 = *(SP + (6: SP)) [unsigned, anvil.PrinterIndex.U, 8]  // index
        $64U.1 = *(SP + (14: SP)) [unsigned, anvil.PrinterIndex.U, 8]  // mask
        $16U.1 = *(SP + (22: SP)) [unsigned, String, 2]  // s
        goto .285
        */


        val __tmp_4293 = ((SP + 4.U(16.W))).asUInt
        generalRegFilesU16(0.U) := Cat(
          arrayRegFiles(__tmp_4293 + 1.U),
          arrayRegFiles(__tmp_4293 + 0.U)
        )

        val __tmp_4294 = ((SP + 6.U(16.W))).asUInt
        generalRegFilesU64(0.U) := Cat(
          arrayRegFiles(__tmp_4294 + 7.U),
          arrayRegFiles(__tmp_4294 + 6.U),
          arrayRegFiles(__tmp_4294 + 5.U),
          arrayRegFiles(__tmp_4294 + 4.U),
          arrayRegFiles(__tmp_4294 + 3.U),
          arrayRegFiles(__tmp_4294 + 2.U),
          arrayRegFiles(__tmp_4294 + 1.U),
          arrayRegFiles(__tmp_4294 + 0.U)
        )

        val __tmp_4295 = ((SP + 14.U(16.W))).asUInt
        generalRegFilesU64(1.U) := Cat(
          arrayRegFiles(__tmp_4295 + 7.U),
          arrayRegFiles(__tmp_4295 + 6.U),
          arrayRegFiles(__tmp_4295 + 5.U),
          arrayRegFiles(__tmp_4295 + 4.U),
          arrayRegFiles(__tmp_4295 + 3.U),
          arrayRegFiles(__tmp_4295 + 2.U),
          arrayRegFiles(__tmp_4295 + 1.U),
          arrayRegFiles(__tmp_4295 + 0.U)
        )

        val __tmp_4296 = ((SP + 22.U(16.W))).asUInt
        generalRegFilesU16(1.U) := Cat(
          arrayRegFiles(__tmp_4296 + 1.U),
          arrayRegFiles(__tmp_4296 + 0.U)
        )

        CP := 285.U
      }

      is(285.U) {
        /*
        decl u8is: IS[Z, U8] [@24, 17]
        $16U.4 = $16U.1
        goto .286
        */



        generalRegFilesU16(4.U) := generalRegFilesU16(1.U)

        CP := 286.U
      }

      is(286.U) {
        /*
        (SP + (24: SP)) [IS[Z, U8], 17]  <-  $16U.4 [String, ((*($16U.4 + (4: SP)) as SP) + (12: SP))]  // u8is = $16U.4
        goto .287
        */


        val __tmp_4297 = (SP + 24.U(16.W))
        val __tmp_4298 = generalRegFilesU16(4.U)
        val __tmp_4299 = (Cat(
           arrayRegFiles((generalRegFilesU16(4.U) + 4.U(16.W)) + 7.U),
           arrayRegFiles((generalRegFilesU16(4.U) + 4.U(16.W)) + 6.U),
           arrayRegFiles((generalRegFilesU16(4.U) + 4.U(16.W)) + 5.U),
           arrayRegFiles((generalRegFilesU16(4.U) + 4.U(16.W)) + 4.U),
           arrayRegFiles((generalRegFilesU16(4.U) + 4.U(16.W)) + 3.U),
           arrayRegFiles((generalRegFilesU16(4.U) + 4.U(16.W)) + 2.U),
           arrayRegFiles((generalRegFilesU16(4.U) + 4.U(16.W)) + 1.U),
           arrayRegFiles((generalRegFilesU16(4.U) + 4.U(16.W)) + 0.U)
         ).asSInt.asUInt.pad(16) + 12.U(16.W))

        when(Idx <= __tmp_4299) {
          arrayRegFiles(__tmp_4297 + Idx + 0.U) := arrayRegFiles(__tmp_4298 + Idx + 0.U)
          arrayRegFiles(__tmp_4297 + Idx + 1.U) := arrayRegFiles(__tmp_4298 + Idx + 1.U)
          arrayRegFiles(__tmp_4297 + Idx + 2.U) := arrayRegFiles(__tmp_4298 + Idx + 2.U)
          arrayRegFiles(__tmp_4297 + Idx + 3.U) := arrayRegFiles(__tmp_4298 + Idx + 3.U)
          arrayRegFiles(__tmp_4297 + Idx + 4.U) := arrayRegFiles(__tmp_4298 + Idx + 4.U)
          arrayRegFiles(__tmp_4297 + Idx + 5.U) := arrayRegFiles(__tmp_4298 + Idx + 5.U)
          arrayRegFiles(__tmp_4297 + Idx + 6.U) := arrayRegFiles(__tmp_4298 + Idx + 6.U)
          arrayRegFiles(__tmp_4297 + Idx + 7.U) := arrayRegFiles(__tmp_4298 + Idx + 7.U)
          Idx := Idx + 8.U
          LeftByteRounds := __tmp_4299 - Idx
        } .elsewhen(IdxLeftByteRounds < LeftByteRounds) {
          val __tmp_4300 = Idx - 8.U
          arrayRegFiles(__tmp_4297 + __tmp_4300 + IdxLeftByteRounds) := arrayRegFiles(__tmp_4298 + __tmp_4300 + IdxLeftByteRounds)
          IdxLeftByteRounds := IdxLeftByteRounds + 1.U
        } .otherwise {
          Idx := 0.U
          IdxLeftByteRounds := 0.U
          LeftByteRounds := 0.U
          CP := 287.U
        }


      }

      is(287.U) {
        /*
        decl i: Z @$0
        $64S.0 = (0: Z)
        goto .288
        */



        generalRegFilesS64(0.U) := 0.S(64.W)

        CP := 288.U
      }

      is(288.U) {
        /*
        decl size: Z @$1
        $16U.4 = $16U.1
        goto .289
        */



        generalRegFilesU16(4.U) := generalRegFilesU16(1.U)

        CP := 289.U
      }

      is(289.U) {
        /*
        $64S.3 = *($16U.4 + (4: SP)) [signed, Z, 8]  // $64S.3 = $16U.4.size
        goto .290
        */


        val __tmp_4301 = ((generalRegFilesU16(4.U) + 4.U(16.W))).asUInt
        generalRegFilesS64(3.U) := Cat(
          arrayRegFiles(__tmp_4301 + 7.U),
          arrayRegFiles(__tmp_4301 + 6.U),
          arrayRegFiles(__tmp_4301 + 5.U),
          arrayRegFiles(__tmp_4301 + 4.U),
          arrayRegFiles(__tmp_4301 + 3.U),
          arrayRegFiles(__tmp_4301 + 2.U),
          arrayRegFiles(__tmp_4301 + 1.U),
          arrayRegFiles(__tmp_4301 + 0.U)
        ).asSInt.pad(64)

        CP := 290.U
      }

      is(290.U) {
        /*
        $64S.1 = $64S.3
        goto .291
        */



        generalRegFilesS64(1.U) := generalRegFilesS64(3.U)

        CP := 291.U
      }

      is(291.U) {
        /*
        $64S.2 = $64S.0
        $64S.3 = $64S.1
        goto .292
        */



        generalRegFilesS64(2.U) := generalRegFilesS64(0.U)


        generalRegFilesS64(3.U) := generalRegFilesS64(1.U)

        CP := 292.U
      }

      is(292.U) {
        /*
        $1U.0 = ($64S.2 < $64S.3)
        goto .293
        */



        generalRegFilesU1(0.U) := (generalRegFilesS64(2.U) < generalRegFilesS64(3.U)).asUInt

        CP := 293.U
      }

      is(293.U) {
        /*
        if $1U.0 goto .294 else goto .311
        */


        CP := Mux((generalRegFilesU1(0.U).asUInt) === 1.U, 294.U, 311.U)
      }

      is(294.U) {
        /*
        $16U.5 = $16U.0
        $64U.2 = $64U.0
        $64S.4 = $64S.0
        goto .295
        */



        generalRegFilesU16(5.U) := generalRegFilesU16(0.U)


        generalRegFilesU64(2.U) := generalRegFilesU64(0.U)


        generalRegFilesS64(4.U) := generalRegFilesS64(0.U)

        CP := 295.U
      }

      is(295.U) {
        /*
        if ((0: Z) <= $64S.4) goto .301 else goto .296
        */


        CP := Mux(((0.S(64.W) <= generalRegFilesS64(4.U)).asUInt.asUInt) === 1.U, 301.U, 296.U)
      }

      is(296.U) {
        /*
        *(((8: SP) + (12: SP)) + ((DP & (63: DP)) as SP)) = (79: U8) [unsigned, U8, 1]  // $display((DP & (63: DP))) = (79: U8)
        *(((8: SP) + (12: SP)) + (((DP + (1: DP)) & (63: DP)) as SP)) = (117: U8) [unsigned, U8, 1]  // $display(((DP + (1: DP)) & (63: DP))) = (117: U8)
        *(((8: SP) + (12: SP)) + (((DP + (2: DP)) & (63: DP)) as SP)) = (116: U8) [unsigned, U8, 1]  // $display(((DP + (2: DP)) & (63: DP))) = (116: U8)
        *(((8: SP) + (12: SP)) + (((DP + (3: DP)) & (63: DP)) as SP)) = (32: U8) [unsigned, U8, 1]  // $display(((DP + (3: DP)) & (63: DP))) = (32: U8)
        *(((8: SP) + (12: SP)) + (((DP + (4: DP)) & (63: DP)) as SP)) = (111: U8) [unsigned, U8, 1]  // $display(((DP + (4: DP)) & (63: DP))) = (111: U8)
        *(((8: SP) + (12: SP)) + (((DP + (5: DP)) & (63: DP)) as SP)) = (102: U8) [unsigned, U8, 1]  // $display(((DP + (5: DP)) & (63: DP))) = (102: U8)
        *(((8: SP) + (12: SP)) + (((DP + (6: DP)) & (63: DP)) as SP)) = (32: U8) [unsigned, U8, 1]  // $display(((DP + (6: DP)) & (63: DP))) = (32: U8)
        *(((8: SP) + (12: SP)) + (((DP + (7: DP)) & (63: DP)) as SP)) = (98: U8) [unsigned, U8, 1]  // $display(((DP + (7: DP)) & (63: DP))) = (98: U8)
        *(((8: SP) + (12: SP)) + (((DP + (8: DP)) & (63: DP)) as SP)) = (111: U8) [unsigned, U8, 1]  // $display(((DP + (8: DP)) & (63: DP))) = (111: U8)
        *(((8: SP) + (12: SP)) + (((DP + (9: DP)) & (63: DP)) as SP)) = (117: U8) [unsigned, U8, 1]  // $display(((DP + (9: DP)) & (63: DP))) = (117: U8)
        *(((8: SP) + (12: SP)) + (((DP + (10: DP)) & (63: DP)) as SP)) = (110: U8) [unsigned, U8, 1]  // $display(((DP + (10: DP)) & (63: DP))) = (110: U8)
        *(((8: SP) + (12: SP)) + (((DP + (11: DP)) & (63: DP)) as SP)) = (100: U8) [unsigned, U8, 1]  // $display(((DP + (11: DP)) & (63: DP))) = (100: U8)
        *(((8: SP) + (12: SP)) + (((DP + (12: DP)) & (63: DP)) as SP)) = (32: U8) [unsigned, U8, 1]  // $display(((DP + (12: DP)) & (63: DP))) = (32: U8)
        *(((8: SP) + (12: SP)) + (((DP + (13: DP)) & (63: DP)) as SP)) = (97: U8) [unsigned, U8, 1]  // $display(((DP + (13: DP)) & (63: DP))) = (97: U8)
        *(((8: SP) + (12: SP)) + (((DP + (14: DP)) & (63: DP)) as SP)) = (110: U8) [unsigned, U8, 1]  // $display(((DP + (14: DP)) & (63: DP))) = (110: U8)
        *(((8: SP) + (12: SP)) + (((DP + (15: DP)) & (63: DP)) as SP)) = (118: U8) [unsigned, U8, 1]  // $display(((DP + (15: DP)) & (63: DP))) = (118: U8)
        *(((8: SP) + (12: SP)) + (((DP + (16: DP)) & (63: DP)) as SP)) = (105: U8) [unsigned, U8, 1]  // $display(((DP + (16: DP)) & (63: DP))) = (105: U8)
        *(((8: SP) + (12: SP)) + (((DP + (17: DP)) & (63: DP)) as SP)) = (108: U8) [unsigned, U8, 1]  // $display(((DP + (17: DP)) & (63: DP))) = (108: U8)
        *(((8: SP) + (12: SP)) + (((DP + (18: DP)) & (63: DP)) as SP)) = (46: U8) [unsigned, U8, 1]  // $display(((DP + (18: DP)) & (63: DP))) = (46: U8)
        *(((8: SP) + (12: SP)) + (((DP + (19: DP)) & (63: DP)) as SP)) = (80: U8) [unsigned, U8, 1]  // $display(((DP + (19: DP)) & (63: DP))) = (80: U8)
        *(((8: SP) + (12: SP)) + (((DP + (20: DP)) & (63: DP)) as SP)) = (114: U8) [unsigned, U8, 1]  // $display(((DP + (20: DP)) & (63: DP))) = (114: U8)
        *(((8: SP) + (12: SP)) + (((DP + (21: DP)) & (63: DP)) as SP)) = (105: U8) [unsigned, U8, 1]  // $display(((DP + (21: DP)) & (63: DP))) = (105: U8)
        *(((8: SP) + (12: SP)) + (((DP + (22: DP)) & (63: DP)) as SP)) = (110: U8) [unsigned, U8, 1]  // $display(((DP + (22: DP)) & (63: DP))) = (110: U8)
        *(((8: SP) + (12: SP)) + (((DP + (23: DP)) & (63: DP)) as SP)) = (116: U8) [unsigned, U8, 1]  // $display(((DP + (23: DP)) & (63: DP))) = (116: U8)
        *(((8: SP) + (12: SP)) + (((DP + (24: DP)) & (63: DP)) as SP)) = (101: U8) [unsigned, U8, 1]  // $display(((DP + (24: DP)) & (63: DP))) = (101: U8)
        *(((8: SP) + (12: SP)) + (((DP + (25: DP)) & (63: DP)) as SP)) = (114: U8) [unsigned, U8, 1]  // $display(((DP + (25: DP)) & (63: DP))) = (114: U8)
        *(((8: SP) + (12: SP)) + (((DP + (26: DP)) & (63: DP)) as SP)) = (73: U8) [unsigned, U8, 1]  // $display(((DP + (26: DP)) & (63: DP))) = (73: U8)
        *(((8: SP) + (12: SP)) + (((DP + (27: DP)) & (63: DP)) as SP)) = (110: U8) [unsigned, U8, 1]  // $display(((DP + (27: DP)) & (63: DP))) = (110: U8)
        *(((8: SP) + (12: SP)) + (((DP + (28: DP)) & (63: DP)) as SP)) = (100: U8) [unsigned, U8, 1]  // $display(((DP + (28: DP)) & (63: DP))) = (100: U8)
        *(((8: SP) + (12: SP)) + (((DP + (29: DP)) & (63: DP)) as SP)) = (101: U8) [unsigned, U8, 1]  // $display(((DP + (29: DP)) & (63: DP))) = (101: U8)
        *(((8: SP) + (12: SP)) + (((DP + (30: DP)) & (63: DP)) as SP)) = (120: U8) [unsigned, U8, 1]  // $display(((DP + (30: DP)) & (63: DP))) = (120: U8)
        *(((8: SP) + (12: SP)) + (((DP + (31: DP)) & (63: DP)) as SP)) = (46: U8) [unsigned, U8, 1]  // $display(((DP + (31: DP)) & (63: DP))) = (46: U8)
        *(((8: SP) + (12: SP)) + (((DP + (32: DP)) & (63: DP)) as SP)) = (85: U8) [unsigned, U8, 1]  // $display(((DP + (32: DP)) & (63: DP))) = (85: U8)
        *(((8: SP) + (12: SP)) + (((DP + (33: DP)) & (63: DP)) as SP)) = (32: U8) [unsigned, U8, 1]  // $display(((DP + (33: DP)) & (63: DP))) = (32: U8)
        *(((8: SP) + (12: SP)) + (((DP + (34: DP)) & (63: DP)) as SP)) = (118: U8) [unsigned, U8, 1]  // $display(((DP + (34: DP)) & (63: DP))) = (118: U8)
        *(((8: SP) + (12: SP)) + (((DP + (35: DP)) & (63: DP)) as SP)) = (97: U8) [unsigned, U8, 1]  // $display(((DP + (35: DP)) & (63: DP))) = (97: U8)
        *(((8: SP) + (12: SP)) + (((DP + (36: DP)) & (63: DP)) as SP)) = (108: U8) [unsigned, U8, 1]  // $display(((DP + (36: DP)) & (63: DP))) = (108: U8)
        *(((8: SP) + (12: SP)) + (((DP + (37: DP)) & (63: DP)) as SP)) = (117: U8) [unsigned, U8, 1]  // $display(((DP + (37: DP)) & (63: DP))) = (117: U8)
        *(((8: SP) + (12: SP)) + (((DP + (38: DP)) & (63: DP)) as SP)) = (101: U8) [unsigned, U8, 1]  // $display(((DP + (38: DP)) & (63: DP))) = (101: U8)
        goto .297
        */


        val __tmp_4302 = ((8.U(16.W) + 12.U(16.W)) + (DP & 63.U(64.W)).asUInt.pad(16))
        val __tmp_4303 = (79.U(8.W)).asUInt
        arrayRegFiles(__tmp_4302 + 0.U) := __tmp_4303(7, 0)

        val __tmp_4304 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 1.U(64.W)) & 63.U(64.W)).asUInt.pad(16))
        val __tmp_4305 = (117.U(8.W)).asUInt
        arrayRegFiles(__tmp_4304 + 0.U) := __tmp_4305(7, 0)

        val __tmp_4306 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 2.U(64.W)) & 63.U(64.W)).asUInt.pad(16))
        val __tmp_4307 = (116.U(8.W)).asUInt
        arrayRegFiles(__tmp_4306 + 0.U) := __tmp_4307(7, 0)

        val __tmp_4308 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 3.U(64.W)) & 63.U(64.W)).asUInt.pad(16))
        val __tmp_4309 = (32.U(8.W)).asUInt
        arrayRegFiles(__tmp_4308 + 0.U) := __tmp_4309(7, 0)

        val __tmp_4310 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 4.U(64.W)) & 63.U(64.W)).asUInt.pad(16))
        val __tmp_4311 = (111.U(8.W)).asUInt
        arrayRegFiles(__tmp_4310 + 0.U) := __tmp_4311(7, 0)

        val __tmp_4312 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 5.U(64.W)) & 63.U(64.W)).asUInt.pad(16))
        val __tmp_4313 = (102.U(8.W)).asUInt
        arrayRegFiles(__tmp_4312 + 0.U) := __tmp_4313(7, 0)

        val __tmp_4314 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 6.U(64.W)) & 63.U(64.W)).asUInt.pad(16))
        val __tmp_4315 = (32.U(8.W)).asUInt
        arrayRegFiles(__tmp_4314 + 0.U) := __tmp_4315(7, 0)

        val __tmp_4316 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 7.U(64.W)) & 63.U(64.W)).asUInt.pad(16))
        val __tmp_4317 = (98.U(8.W)).asUInt
        arrayRegFiles(__tmp_4316 + 0.U) := __tmp_4317(7, 0)

        val __tmp_4318 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 8.U(64.W)) & 63.U(64.W)).asUInt.pad(16))
        val __tmp_4319 = (111.U(8.W)).asUInt
        arrayRegFiles(__tmp_4318 + 0.U) := __tmp_4319(7, 0)

        val __tmp_4320 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 9.U(64.W)) & 63.U(64.W)).asUInt.pad(16))
        val __tmp_4321 = (117.U(8.W)).asUInt
        arrayRegFiles(__tmp_4320 + 0.U) := __tmp_4321(7, 0)

        val __tmp_4322 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 10.U(64.W)) & 63.U(64.W)).asUInt.pad(16))
        val __tmp_4323 = (110.U(8.W)).asUInt
        arrayRegFiles(__tmp_4322 + 0.U) := __tmp_4323(7, 0)

        val __tmp_4324 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 11.U(64.W)) & 63.U(64.W)).asUInt.pad(16))
        val __tmp_4325 = (100.U(8.W)).asUInt
        arrayRegFiles(__tmp_4324 + 0.U) := __tmp_4325(7, 0)

        val __tmp_4326 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 12.U(64.W)) & 63.U(64.W)).asUInt.pad(16))
        val __tmp_4327 = (32.U(8.W)).asUInt
        arrayRegFiles(__tmp_4326 + 0.U) := __tmp_4327(7, 0)

        val __tmp_4328 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 13.U(64.W)) & 63.U(64.W)).asUInt.pad(16))
        val __tmp_4329 = (97.U(8.W)).asUInt
        arrayRegFiles(__tmp_4328 + 0.U) := __tmp_4329(7, 0)

        val __tmp_4330 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 14.U(64.W)) & 63.U(64.W)).asUInt.pad(16))
        val __tmp_4331 = (110.U(8.W)).asUInt
        arrayRegFiles(__tmp_4330 + 0.U) := __tmp_4331(7, 0)

        val __tmp_4332 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 15.U(64.W)) & 63.U(64.W)).asUInt.pad(16))
        val __tmp_4333 = (118.U(8.W)).asUInt
        arrayRegFiles(__tmp_4332 + 0.U) := __tmp_4333(7, 0)

        val __tmp_4334 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 16.U(64.W)) & 63.U(64.W)).asUInt.pad(16))
        val __tmp_4335 = (105.U(8.W)).asUInt
        arrayRegFiles(__tmp_4334 + 0.U) := __tmp_4335(7, 0)

        val __tmp_4336 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 17.U(64.W)) & 63.U(64.W)).asUInt.pad(16))
        val __tmp_4337 = (108.U(8.W)).asUInt
        arrayRegFiles(__tmp_4336 + 0.U) := __tmp_4337(7, 0)

        val __tmp_4338 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 18.U(64.W)) & 63.U(64.W)).asUInt.pad(16))
        val __tmp_4339 = (46.U(8.W)).asUInt
        arrayRegFiles(__tmp_4338 + 0.U) := __tmp_4339(7, 0)

        val __tmp_4340 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 19.U(64.W)) & 63.U(64.W)).asUInt.pad(16))
        val __tmp_4341 = (80.U(8.W)).asUInt
        arrayRegFiles(__tmp_4340 + 0.U) := __tmp_4341(7, 0)

        val __tmp_4342 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 20.U(64.W)) & 63.U(64.W)).asUInt.pad(16))
        val __tmp_4343 = (114.U(8.W)).asUInt
        arrayRegFiles(__tmp_4342 + 0.U) := __tmp_4343(7, 0)

        val __tmp_4344 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 21.U(64.W)) & 63.U(64.W)).asUInt.pad(16))
        val __tmp_4345 = (105.U(8.W)).asUInt
        arrayRegFiles(__tmp_4344 + 0.U) := __tmp_4345(7, 0)

        val __tmp_4346 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 22.U(64.W)) & 63.U(64.W)).asUInt.pad(16))
        val __tmp_4347 = (110.U(8.W)).asUInt
        arrayRegFiles(__tmp_4346 + 0.U) := __tmp_4347(7, 0)

        val __tmp_4348 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 23.U(64.W)) & 63.U(64.W)).asUInt.pad(16))
        val __tmp_4349 = (116.U(8.W)).asUInt
        arrayRegFiles(__tmp_4348 + 0.U) := __tmp_4349(7, 0)

        val __tmp_4350 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 24.U(64.W)) & 63.U(64.W)).asUInt.pad(16))
        val __tmp_4351 = (101.U(8.W)).asUInt
        arrayRegFiles(__tmp_4350 + 0.U) := __tmp_4351(7, 0)

        val __tmp_4352 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 25.U(64.W)) & 63.U(64.W)).asUInt.pad(16))
        val __tmp_4353 = (114.U(8.W)).asUInt
        arrayRegFiles(__tmp_4352 + 0.U) := __tmp_4353(7, 0)

        val __tmp_4354 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 26.U(64.W)) & 63.U(64.W)).asUInt.pad(16))
        val __tmp_4355 = (73.U(8.W)).asUInt
        arrayRegFiles(__tmp_4354 + 0.U) := __tmp_4355(7, 0)

        val __tmp_4356 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 27.U(64.W)) & 63.U(64.W)).asUInt.pad(16))
        val __tmp_4357 = (110.U(8.W)).asUInt
        arrayRegFiles(__tmp_4356 + 0.U) := __tmp_4357(7, 0)

        val __tmp_4358 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 28.U(64.W)) & 63.U(64.W)).asUInt.pad(16))
        val __tmp_4359 = (100.U(8.W)).asUInt
        arrayRegFiles(__tmp_4358 + 0.U) := __tmp_4359(7, 0)

        val __tmp_4360 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 29.U(64.W)) & 63.U(64.W)).asUInt.pad(16))
        val __tmp_4361 = (101.U(8.W)).asUInt
        arrayRegFiles(__tmp_4360 + 0.U) := __tmp_4361(7, 0)

        val __tmp_4362 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 30.U(64.W)) & 63.U(64.W)).asUInt.pad(16))
        val __tmp_4363 = (120.U(8.W)).asUInt
        arrayRegFiles(__tmp_4362 + 0.U) := __tmp_4363(7, 0)

        val __tmp_4364 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 31.U(64.W)) & 63.U(64.W)).asUInt.pad(16))
        val __tmp_4365 = (46.U(8.W)).asUInt
        arrayRegFiles(__tmp_4364 + 0.U) := __tmp_4365(7, 0)

        val __tmp_4366 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 32.U(64.W)) & 63.U(64.W)).asUInt.pad(16))
        val __tmp_4367 = (85.U(8.W)).asUInt
        arrayRegFiles(__tmp_4366 + 0.U) := __tmp_4367(7, 0)

        val __tmp_4368 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 33.U(64.W)) & 63.U(64.W)).asUInt.pad(16))
        val __tmp_4369 = (32.U(8.W)).asUInt
        arrayRegFiles(__tmp_4368 + 0.U) := __tmp_4369(7, 0)

        val __tmp_4370 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 34.U(64.W)) & 63.U(64.W)).asUInt.pad(16))
        val __tmp_4371 = (118.U(8.W)).asUInt
        arrayRegFiles(__tmp_4370 + 0.U) := __tmp_4371(7, 0)

        val __tmp_4372 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 35.U(64.W)) & 63.U(64.W)).asUInt.pad(16))
        val __tmp_4373 = (97.U(8.W)).asUInt
        arrayRegFiles(__tmp_4372 + 0.U) := __tmp_4373(7, 0)

        val __tmp_4374 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 36.U(64.W)) & 63.U(64.W)).asUInt.pad(16))
        val __tmp_4375 = (108.U(8.W)).asUInt
        arrayRegFiles(__tmp_4374 + 0.U) := __tmp_4375(7, 0)

        val __tmp_4376 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 37.U(64.W)) & 63.U(64.W)).asUInt.pad(16))
        val __tmp_4377 = (117.U(8.W)).asUInt
        arrayRegFiles(__tmp_4376 + 0.U) := __tmp_4377(7, 0)

        val __tmp_4378 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 38.U(64.W)) & 63.U(64.W)).asUInt.pad(16))
        val __tmp_4379 = (101.U(8.W)).asUInt
        arrayRegFiles(__tmp_4378 + 0.U) := __tmp_4379(7, 0)

        CP := 297.U
      }

      is(297.U) {
        /*
        DP = DP + 39
        goto .298
        */


        DP := DP + 39.U

        CP := 298.U
      }

      is(298.U) {
        /*
        *(((8: SP) + (12: SP)) + ((DP & (63: DP)) as SP)) = (10: U8) [unsigned, U8, 1]  // $display((DP & (63: DP))) = (10: U8)
        goto .299
        */


        val __tmp_4380 = ((8.U(16.W) + 12.U(16.W)) + (DP & 63.U(64.W)).asUInt.pad(16))
        val __tmp_4381 = (10.U(8.W)).asUInt
        arrayRegFiles(__tmp_4380 + 0.U) := __tmp_4381(7, 0)

        CP := 299.U
      }

      is(299.U) {
        /*
        DP = DP + 1
        goto .1
        */


        DP := DP + 1.U

        CP := 1.U
      }

      is(301.U) {
        /*
        $64U.4 = ($64S.4 as anvil.PrinterIndex.U)
        goto .302
        */



        generalRegFilesU64(4.U) := generalRegFilesS64(4.U).asUInt

        CP := 302.U
      }

      is(302.U) {
        /*
        $64U.5 = ($64U.2 + $64U.4)
        goto .303
        */



        generalRegFilesU64(5.U) := (generalRegFilesU64(2.U) + generalRegFilesU64(4.U))

        CP := 303.U
      }

      is(303.U) {
        /*
        $64U.6 = $64U.1
        goto .304
        */



        generalRegFilesU64(6.U) := generalRegFilesU64(1.U)

        CP := 304.U
      }

      is(304.U) {
        /*
        $64U.7 = ($64U.5 & $64U.6)
        goto .305
        */



        generalRegFilesU64(7.U) := (generalRegFilesU64(5.U) & generalRegFilesU64(6.U))

        CP := 305.U
      }

      is(305.U) {
        /*
        $16U.6 = (SP + (24: SP))
        $64S.5 = $64S.0
        goto .306
        */



        generalRegFilesU16(6.U) := (SP + 24.U(16.W))


        generalRegFilesS64(5.U) := generalRegFilesS64(0.U)

        CP := 306.U
      }

      is(306.U) {
        /*
        $8U.0 = *(($16U.6 + (12: SP)) + ($64S.5 as SP)) [unsigned, U8, 1]  // $8U.0 = $16U.6($64S.5)
        goto .307
        */


        val __tmp_4382 = (((generalRegFilesU16(6.U) + 12.U(16.W)) + generalRegFilesS64(5.U).asUInt.pad(16))).asUInt
        generalRegFilesU8(0.U) := Cat(
          arrayRegFiles(__tmp_4382 + 0.U)
        )

        CP := 307.U
      }

      is(307.U) {
        /*
        *(($16U.5 + (12: SP)) + ($64U.7 as SP)) = $8U.0 [unsigned, U8, 1]  // $16U.5($64U.7) = $8U.0
        goto .308
        */


        val __tmp_4383 = ((generalRegFilesU16(5.U) + 12.U(16.W)) + generalRegFilesU64(7.U).asUInt.pad(16))
        val __tmp_4384 = (generalRegFilesU8(0.U)).asUInt
        arrayRegFiles(__tmp_4383 + 0.U) := __tmp_4384(7, 0)

        CP := 308.U
      }

      is(308.U) {
        /*
        $64S.2 = $64S.0
        goto .309
        */



        generalRegFilesS64(2.U) := generalRegFilesS64(0.U)

        CP := 309.U
      }

      is(309.U) {
        /*
        $64S.3 = ($64S.2 + (1: Z))
        goto .310
        */



        generalRegFilesS64(3.U) := (generalRegFilesS64(2.U) + 1.S(64.W))

        CP := 310.U
      }

      is(310.U) {
        /*
        $64S.0 = $64S.3
        goto .291
        */



        generalRegFilesS64(0.U) := generalRegFilesS64(3.U)

        CP := 291.U
      }

      is(311.U) {
        /*
        $64S.2 = $64S.0
        undecl i: Z @$0
        goto .312
        */



        generalRegFilesS64(2.U) := generalRegFilesS64(0.U)

        CP := 312.U
      }

      is(312.U) {
        /*
        $64U.3 = ($64S.2 as U64)
        goto .313
        */



        generalRegFilesU64(3.U) := generalRegFilesS64(2.U).asUInt

        CP := 313.U
      }

      is(313.U) {
        /*
        **(SP + (2: SP)) = $64U.3 [unsigned, U64, 8]  // $res = $64U.3
        goto $ret@0
        */


        val __tmp_4385 = Cat(
          arrayRegFiles((SP + 2.U(16.W)) + 1.U),
          arrayRegFiles((SP + 2.U(16.W)) + 0.U)
        )
        val __tmp_4386 = (generalRegFilesU64(3.U)).asUInt
        arrayRegFiles(__tmp_4385 + 0.U) := __tmp_4386(7, 0)
        arrayRegFiles(__tmp_4385 + 1.U) := __tmp_4386(15, 8)
        arrayRegFiles(__tmp_4385 + 2.U) := __tmp_4386(23, 16)
        arrayRegFiles(__tmp_4385 + 3.U) := __tmp_4386(31, 24)
        arrayRegFiles(__tmp_4385 + 4.U) := __tmp_4386(39, 32)
        arrayRegFiles(__tmp_4385 + 5.U) := __tmp_4386(47, 40)
        arrayRegFiles(__tmp_4385 + 6.U) := __tmp_4386(55, 48)
        arrayRegFiles(__tmp_4385 + 7.U) := __tmp_4386(63, 56)

        CP := Cat(
          arrayRegFiles(SP + 0.U + 1.U),
          arrayRegFiles(SP + 0.U + 0.U)
        )

      }

    }

}

class AXIWrapperChiselGeneratedPrintTestTest(val C_S_AXI_DATA_WIDTH:  Int = 32,
                                         val C_S_AXI_ADDR_WIDTH:  Int = 32,
                                         val ARRAY_REG_WIDTH:     Int = 8,
                                         val ARRAY_REG_DEPTH:     Int = 344,
                                         val STACK_POINTER_WIDTH: Int = 16,
                                         val CODE_POINTER_WIDTH:  Int = 16)  extends Module {
    val io = IO(new Bundle{
        // write address channel
        val S_AXI_AWADDR  = Input(UInt(C_S_AXI_ADDR_WIDTH.W))
        val S_AXI_AWPROT  = Input(UInt(3.W))
        val S_AXI_AWVALID = Input(Bool())
        val S_AXI_AWREADY = Output(Bool())

        // write data channel
        val S_AXI_WDATA  = Input(SInt(C_S_AXI_DATA_WIDTH.W))
        val S_AXI_WSTRB  = Input(UInt((C_S_AXI_DATA_WIDTH/8).W))
        val S_AXI_WVALID = Input(Bool())
        val S_AXI_WREADY = Output(Bool())

        // write response channel
        val S_AXI_BRESP  = Output(UInt(2.W))
        val S_AXI_BVALID = Output(Bool())
        val S_AXI_BREADY = Input(Bool())

        // read address channel
        val S_AXI_ARADDR  = Input(UInt(C_S_AXI_ADDR_WIDTH.W))
        val S_AXI_ARPROT  = Input(UInt(3.W))
        val S_AXI_ARVALID = Input(Bool())
        val S_AXI_ARREADY = Output(Bool())

        // read data channel
        val S_AXI_RDATA  = Output(SInt(C_S_AXI_DATA_WIDTH.W))
        val S_AXI_RRESP  = Output(UInt(2.W))
        val S_AXI_RVALID = Output(Bool())
        val S_AXI_RREADY = Input(Bool())
    })

    val lowActiveReset = !reset.asBool
  withReset(lowActiveReset) {

    // AXI4LITE signals, the signals need to be saved
    val axi_awaddr  = Reg(UInt(C_S_AXI_ADDR_WIDTH.W))
    val axi_araddr  = Reg(UInt(C_S_AXI_ADDR_WIDTH.W))
    // AXI4LITE output signal
    val axi_awready = Reg(Bool())
    val axi_wready  = Reg(Bool())
    val axi_bresp   = Reg(UInt(2.W))
    val axi_bvalid  = Reg(Bool())
    val axi_arready = Reg(Bool())
    val axi_rdata   = Reg(SInt(C_S_AXI_DATA_WIDTH.W))
    val axi_rresp   = Reg(UInt(2.W))
    val axi_rvalid  = Reg(Bool())


    // Example-specific design signals
    // local parameter for addressing 32 bit / 64 bit C_S_AXI_DATA_WIDTH
    // ADDR_LSB is used for addressing 32/64 bit registers/memories
    // ADDR_LSB = 2 for 32 bits (n downto 2)
    // ADDR_LSB = 3 for 64 bits (n downto 3)
    val ADDR_LSB: Int = (C_S_AXI_DATA_WIDTH/32) + 1
    val OPT_MEM_ADDR_BITS: Int = 10

    //----------------------------------------------
    //-- Signals for user logic register space example
    //------------------------------------------------
    val slv_reg_rden = Wire(Bool())
    val slv_reg_wren = Wire(Bool())
    val reg_data_out = Wire(SInt(C_S_AXI_DATA_WIDTH.W))
    val aw_en        = Reg(Bool())

    // Registers for target module port
    val io_valid_reg = Reg(UInt(32.W))
    val io_ready_reg = Reg(UInt(32.W))

    // instantiate the target module
    val arrayReadAddrValid = (axi_araddr(log2Ceil(ARRAY_REG_DEPTH), 0) >= 8.U) && ((axi_araddr(log2Ceil(ARRAY_REG_DEPTH), 0) + 3.U) < (ARRAY_REG_DEPTH.U + 8.U))
    val arrayWriteAddrValid = (axi_awaddr(log2Ceil(ARRAY_REG_DEPTH), 0) >= 8.U) && ((axi_awaddr(log2Ceil(ARRAY_REG_DEPTH), 0) + 3.U) < (ARRAY_REG_DEPTH.U + 8.U))
    val arrayWriteValid = slv_reg_wren & arrayWriteAddrValid
    val arrayReadValid = arrayReadAddrValid & axi_arready & io.S_AXI_ARVALID & ~axi_rvalid
    val arrayReady = axi_araddr(log2Ceil(ARRAY_REG_DEPTH), 0) === 4.U
    val modPrintTestTest = Module(new PrintTestTest(C_S_AXI_DATA_WIDTH  = C_S_AXI_DATA_WIDTH ,
                                                C_S_AXI_ADDR_WIDTH  = C_S_AXI_ADDR_WIDTH ,
                                                ARRAY_REG_WIDTH     = ARRAY_REG_WIDTH    ,
                                                ARRAY_REG_DEPTH     = ARRAY_REG_DEPTH    ,
                                                STACK_POINTER_WIDTH = STACK_POINTER_WIDTH,
                                                CODE_POINTER_WIDTH  = CODE_POINTER_WIDTH  ))
    modPrintTestTest.io.valid := Mux(io_valid_reg(0) & (io_ready_reg === 2.U), true.B, false.B)
    io_ready_reg := modPrintTestTest.io.ready
    modPrintTestTest.io.arrayRe := arrayReadValid
    modPrintTestTest.io.arrayWe := arrayWriteValid
    modPrintTestTest.io.arrayStrb := Mux(arrayWriteValid, io.S_AXI_WSTRB, 0.U)
    modPrintTestTest.io.arrayWriteAddr := Mux(arrayWriteValid,
                                                    Cat(axi_awaddr(log2Ceil(ARRAY_REG_DEPTH) - 1, ADDR_LSB), 0.U(ADDR_LSB.W)) - 8.U,
                                                    0.U)
    modPrintTestTest.io.arrayReadAddr  := Mux(arrayReadValid,
                                                    Cat(axi_araddr(log2Ceil(ARRAY_REG_DEPTH) - 1, ADDR_LSB), 0.U(ADDR_LSB.W)) - 8.U,
                                                    0.U)
    modPrintTestTest.io.arrayWData := Mux(arrayWriteValid, io.S_AXI_WDATA.asUInt, 0.U)

    when(arrayReady) {
        reg_data_out := io_ready_reg.asSInt
    } .elsewhen(arrayReadValid) {
        reg_data_out := modPrintTestTest.io.arrayRData.asSInt
    } .otherwise {
        reg_data_out := 0.S
    }

    when(lowActiveReset.asBool) {
        io_ready_reg := 0.U
    } .otherwise {
        io_ready_reg := modPrintTestTest.io.ready
    }

    // I/O Connections assignments
    io.S_AXI_AWREADY := axi_awready;
    io.S_AXI_WREADY  := axi_wready;
    io.S_AXI_BRESP   := axi_bresp;
    io.S_AXI_BVALID	 := axi_bvalid;
    io.S_AXI_ARREADY := axi_arready;
    io.S_AXI_RDATA   := axi_rdata;
    io.S_AXI_RRESP   := axi_rresp;
    io.S_AXI_RVALID  := axi_rvalid;

    // Implement axi_awready generation
    // axi_awready is asserted for one S_AXI_ACLK clock cycle when both
    // S_AXI_AWVALID and S_AXI_WVALID are asserted. axi_awready is
    // de-asserted when reset is low.
    when(lowActiveReset.asBool) {
        axi_awready := false.B
        aw_en       := true.B
    } .otherwise {
        when(~axi_awready && io.S_AXI_AWVALID && io.S_AXI_WVALID && aw_en) {
            // slave is ready to accept write address when
            // there is a valid write address and write data
            // on the write address and data bus. This design
            // expects no outstanding transactions.
            axi_awready := true.B
            aw_en       := false.B
        } .elsewhen(io.S_AXI_BREADY && axi_bvalid) {
            // the current operation is finished
            // prepare for the next write operation
            axi_awready := false.B
            aw_en       := true.B
        } .otherwise {
            axi_awready  := false.B
        }
    }

    // Implement axi_awaddr latching
    // This process is used to latch the address when both
    // S_AXI_AWVALID and S_AXI_WVALID are valid.
    when(lowActiveReset.asBool) {
        axi_awaddr := 0.U
    } .otherwise {
        when(~axi_awready && io.S_AXI_AWVALID && io.S_AXI_WVALID && aw_en) {
            axi_awaddr := io.S_AXI_AWADDR
        }
    }

    // Implement axi_wready generation
    // axi_wready is asserted for one S_AXI_ACLK clock cycle when both
    // S_AXI_AWVALID and S_AXI_WVALID are asserted. axi_wready is
    // de-asserted when reset is low.
    when(lowActiveReset.asBool) {
        axi_wready := false.B
    } .otherwise {
        when(~axi_wready && io.S_AXI_WVALID && io.S_AXI_AWVALID && aw_en) {
            // slave is ready to accept write data when
            // there is a valid write address and write data
            // on the write address and data bus. This design
            // expects no outstanding transactions.
            axi_wready := true.B
        } .otherwise {
            axi_wready := false.B
        }
    }

    // Implement memory mapped register select and write logic generation
    // The write data is accepted and written to memory mapped registers when
    // axi_awready, S_AXI_WVALID, axi_wready and S_AXI_WVALID are asserted. Write strobes are used to
    // select byte enables of slave registers while writing.
    // These registers are cleared when reset (active low) is applied.
    // Slave register write enable is asserted when valid address and data are available
    // and the slave is ready to accept the write address and write data.
    slv_reg_wren := axi_wready && io.S_AXI_WVALID && axi_awready && io.S_AXI_AWVALID

    val writeEffectiveAddr = axi_awaddr(log2Ceil(ARRAY_REG_DEPTH), 0)

    when(lowActiveReset.asBool) {
        io_valid_reg := 0.U
    } .otherwise {
        when(slv_reg_wren && writeEffectiveAddr === 0.U) {
                io_valid_reg := io.S_AXI_WDATA.asUInt
        }
    }

    // Implement write response logic generation
    // The write response and response valid signals are asserted by the slave
    // when axi_wready, S_AXI_WVALID, axi_wready and S_AXI_WVALID are asserted.
    // This marks the acceptance of address and indicates the status of
    // write transaction.
    when(lowActiveReset.asBool) {
        axi_bvalid := false.B
        axi_bresp  := 0.U
    } .otherwise {
        when(axi_awready && io.S_AXI_AWVALID && ~axi_bvalid && axi_wready && io.S_AXI_WVALID) {
            axi_bvalid := true.B
            axi_bresp  := 0.U
        } .otherwise {
            when(io.S_AXI_BREADY && axi_bvalid) {
                axi_bvalid := false.B
            }
        }
    }

    // Implement axi_arready generation
    // axi_arready is asserted for one S_AXI_ACLK clock cycle when
    // S_AXI_ARVALID is asserted. axi_awready is
    // de-asserted when reset (active low) is asserted.
    // The read address is also latched when S_AXI_ARVALID is
    // asserted. axi_araddr is reset to zero on reset assertion.
    when(lowActiveReset.asBool) {
        axi_arready := false.B
        axi_araddr  := 0.U
    } .otherwise {
        when(~axi_arready && io.S_AXI_ARVALID) {
            // indicates that the slave has acceped the valid read address
            axi_arready := true.B
            axi_araddr  := io.S_AXI_ARADDR
        } .otherwise {
            axi_arready := false.B
        }
    }

    // Implement axi_arvalid generation
    // axi_rvalid is asserted for one S_AXI_ACLK clock cycle when both
    // S_AXI_ARVALID and axi_arready are asserted. The slave registers
    // data are available on the axi_rdata bus at this instance. The
    // assertion of axi_rvalid marks the validity of read data on the
    // bus and axi_rresp indicates the status of read transaction.axi_rvalid
    // is deasserted on reset (active low). axi_rresp and axi_rdata are
    // cleared to zero on reset (active low).
    when(lowActiveReset.asBool) {
        axi_rvalid := false.B
        axi_rresp  := 0.U
    } .otherwise {
        when(axi_arready && io.S_AXI_ARVALID && ~axi_rvalid) {
            axi_rvalid := true.B
            axi_rresp  := 0.U
        } .elsewhen(axi_rvalid && io.S_AXI_RREADY) {
            axi_rvalid := false.B
        }
    }

    // Implement memory mapped register select and read logic generation
    // Slave register read enable is asserted when valid address is available
    // and the slave is ready to accept the read address.
    slv_reg_rden := axi_arready & io.S_AXI_ARVALID & ~axi_rvalid;

    // Output register or memory read data
    when(lowActiveReset.asBool) {
        axi_rdata := 0.S
    } .otherwise {
        // When there is a valid read address (S_AXI_ARVALID) with
        // acceptance of read address by the slave (axi_arready),
        // output the read dada
        when(slv_reg_rden) {
            axi_rdata := reg_data_out
        }
    }
  }
}

