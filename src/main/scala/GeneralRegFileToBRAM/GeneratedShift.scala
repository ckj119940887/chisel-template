package GeneralRegFileToBRAM
import chisel3._
import chisel3.util._
import chisel3.experimental._



class ShiftU64ShiftS64Test (val C_S_AXI_DATA_WIDTH:  Int = 32,
               val C_S_AXI_ADDR_WIDTH:  Int = 32,
               val ARRAY_REG_WIDTH:     Int = 8,
               val ARRAY_REG_DEPTH:     Int = 344,
               val GENERAL_REG_WIDTH:   Int = 64,
               val GENERAL_REG_DEPTH:   Int = 11,
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
    val generalRegFiles = Reg(Vec(1 << log2Ceil(GENERAL_REG_DEPTH), UInt(GENERAL_REG_WIDTH.W)))
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
        SP = 148
        DP = 0
        *(10: U32) = (886747591: U32) [unsigned, U32, 4]  // $display.$type (MS[anvil.PrinterIndex.U, U8]: 0x34DAB1C7)
        *(14: SP) = (128: Z) [signed, Z, 8]  // $display.size
        *(148: CP) = (0: CP) [unsigned, CP, 2]  // $ret
        goto .5
        */


        SP := 148.U(16.W)

        DP := 0.U(64.W)

        val __tmp_6799 = 10.U(32.W)
        val __tmp_6800 = (886747591.U(32.W)).asUInt
        arrayRegFiles(__tmp_6799 + 0.U) := __tmp_6800(7, 0)
        arrayRegFiles(__tmp_6799 + 1.U) := __tmp_6800(15, 8)
        arrayRegFiles(__tmp_6799 + 2.U) := __tmp_6800(23, 16)
        arrayRegFiles(__tmp_6799 + 3.U) := __tmp_6800(31, 24)

        val __tmp_6801 = 14.U(16.W)
        val __tmp_6802 = (128.S(64.W)).asUInt
        arrayRegFiles(__tmp_6801 + 0.U) := __tmp_6802(7, 0)
        arrayRegFiles(__tmp_6801 + 1.U) := __tmp_6802(15, 8)
        arrayRegFiles(__tmp_6801 + 2.U) := __tmp_6802(23, 16)
        arrayRegFiles(__tmp_6801 + 3.U) := __tmp_6802(31, 24)
        arrayRegFiles(__tmp_6801 + 4.U) := __tmp_6802(39, 32)
        arrayRegFiles(__tmp_6801 + 5.U) := __tmp_6802(47, 40)
        arrayRegFiles(__tmp_6801 + 6.U) := __tmp_6802(55, 48)
        arrayRegFiles(__tmp_6801 + 7.U) := __tmp_6802(63, 56)

        val __tmp_6803 = 148.U(16.W)
        val __tmp_6804 = (0.U(16.W)).asUInt
        arrayRegFiles(__tmp_6803 + 0.U) := __tmp_6804(7, 0)
        arrayRegFiles(__tmp_6803 + 1.U) := __tmp_6804(15, 8)

        CP := 5.U
      }

      is(5.U) {
        /*
        $64S.0 = *(0: SP) [signed, Z, 8]  // $64S.0 = $testNum
        goto .6
        */


        val __tmp_6805 = (0.U(16.W)).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_6805 + 7.U),
          arrayRegFiles(__tmp_6805 + 6.U),
          arrayRegFiles(__tmp_6805 + 5.U),
          arrayRegFiles(__tmp_6805 + 4.U),
          arrayRegFiles(__tmp_6805 + 3.U),
          arrayRegFiles(__tmp_6805 + 2.U),
          arrayRegFiles(__tmp_6805 + 1.U),
          arrayRegFiles(__tmp_6805 + 0.U)
        ).asSInt.pad(GENERAL_REG_WIDTH).asUInt

        CP := 6.U
      }

      is(6.U) {
        /*
        if (($64S.0 < (0: Z)) | ($64S.0 ≡ (0: Z))) goto .7 else goto .11
        */


        CP := Mux((((generalRegFiles(0.U).asSInt < 0.S(64.W)).asUInt | (generalRegFiles(0.U).asSInt === 0.S(64.W)).asUInt).asUInt) === 1.U, 7.U, 11.U)
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
        *SP = (9: CP) [unsigned, CP, 2]  // $ret@0 = 1717
        goto .12
        */


        val __tmp_6806 = SP
        val __tmp_6807 = (9.U(16.W)).asUInt
        arrayRegFiles(__tmp_6806 + 0.U) := __tmp_6807(7, 0)
        arrayRegFiles(__tmp_6806 + 1.U) := __tmp_6807(15, 8)

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
        decl r0: U64 [@2, 8]
        alloc shiftU64$res@[23,12].4D1A864C: U64 [@10, 8]
        goto .13
        */


        CP := 13.U
      }

      is(13.U) {
        /*
        SP = SP + 34
        goto .14
        */


        SP := SP + 34.U

        CP := 14.U
      }

      is(14.U) {
        /*
        decl $ret: CP [@0, 2], $res: SP [@2, 10], m: U64 [@12, 8], n: U64 [@20, 8]
        *SP = (15: CP) [unsigned, CP, 2]  // $ret@0 = 1718
        *(SP + (2: SP)) = (SP - (24: SP)) [unsigned, SP, 2]  // $res@2 = -24
        *(SP + (12: SP)) = (9223372036854775808: U64) [unsigned, U64, 8]  // m = (9223372036854775808: U64)
        *(SP + (20: SP)) = (60: U64) [unsigned, U64, 8]  // n = (60: U64)
        goto .49
        */


        val __tmp_6808 = SP
        val __tmp_6809 = (15.U(16.W)).asUInt
        arrayRegFiles(__tmp_6808 + 0.U) := __tmp_6809(7, 0)
        arrayRegFiles(__tmp_6808 + 1.U) := __tmp_6809(15, 8)

        val __tmp_6810 = (SP + 2.U(16.W))
        val __tmp_6811 = ((SP - 24.U(16.W))).asUInt
        arrayRegFiles(__tmp_6810 + 0.U) := __tmp_6811(7, 0)
        arrayRegFiles(__tmp_6810 + 1.U) := __tmp_6811(15, 8)

        val __tmp_6812 = (SP + 12.U(16.W))
        val __tmp_6813 = (BigInt("9223372036854775808").U(64.W)).asUInt
        arrayRegFiles(__tmp_6812 + 0.U) := __tmp_6813(7, 0)
        arrayRegFiles(__tmp_6812 + 1.U) := __tmp_6813(15, 8)
        arrayRegFiles(__tmp_6812 + 2.U) := __tmp_6813(23, 16)
        arrayRegFiles(__tmp_6812 + 3.U) := __tmp_6813(31, 24)
        arrayRegFiles(__tmp_6812 + 4.U) := __tmp_6813(39, 32)
        arrayRegFiles(__tmp_6812 + 5.U) := __tmp_6813(47, 40)
        arrayRegFiles(__tmp_6812 + 6.U) := __tmp_6813(55, 48)
        arrayRegFiles(__tmp_6812 + 7.U) := __tmp_6813(63, 56)

        val __tmp_6814 = (SP + 20.U(16.W))
        val __tmp_6815 = (60.U(64.W)).asUInt
        arrayRegFiles(__tmp_6814 + 0.U) := __tmp_6815(7, 0)
        arrayRegFiles(__tmp_6814 + 1.U) := __tmp_6815(15, 8)
        arrayRegFiles(__tmp_6814 + 2.U) := __tmp_6815(23, 16)
        arrayRegFiles(__tmp_6814 + 3.U) := __tmp_6815(31, 24)
        arrayRegFiles(__tmp_6814 + 4.U) := __tmp_6815(39, 32)
        arrayRegFiles(__tmp_6814 + 5.U) := __tmp_6815(47, 40)
        arrayRegFiles(__tmp_6814 + 6.U) := __tmp_6815(55, 48)
        arrayRegFiles(__tmp_6814 + 7.U) := __tmp_6815(63, 56)

        CP := 49.U
      }

      is(15.U) {
        /*
        $64U.4 = **(SP + (2: SP)) [unsigned, U64, 8]  // $4 = $res
        undecl n: U64 [@20, 8], m: U64 [@12, 8], $res: SP [@2, 10], $ret: CP [@0, 2]
        goto .16
        */


        val __tmp_6816 = (Cat(
          arrayRegFiles((SP + 2.U(16.W)) + 1.U),
          arrayRegFiles((SP + 2.U(16.W)) + 0.U)
        )).asUInt
        generalRegFiles(4.U) := Cat(
          arrayRegFiles(__tmp_6816 + 7.U),
          arrayRegFiles(__tmp_6816 + 6.U),
          arrayRegFiles(__tmp_6816 + 5.U),
          arrayRegFiles(__tmp_6816 + 4.U),
          arrayRegFiles(__tmp_6816 + 3.U),
          arrayRegFiles(__tmp_6816 + 2.U),
          arrayRegFiles(__tmp_6816 + 1.U),
          arrayRegFiles(__tmp_6816 + 0.U)
        ).asUInt

        CP := 16.U
      }

      is(16.U) {
        /*
        SP = SP - 34
        goto .17
        */


        SP := SP - 34.U

        CP := 17.U
      }

      is(17.U) {
        /*
        *(SP + (2: SP)) = $64U.4 [signed, U64, 8]  // r0 = $64U.4
        goto .18
        */


        val __tmp_6817 = (SP + 2.U(16.W))
        val __tmp_6818 = (generalRegFiles(4.U)).asUInt
        arrayRegFiles(__tmp_6817 + 0.U) := __tmp_6818(7, 0)
        arrayRegFiles(__tmp_6817 + 1.U) := __tmp_6818(15, 8)
        arrayRegFiles(__tmp_6817 + 2.U) := __tmp_6818(23, 16)
        arrayRegFiles(__tmp_6817 + 3.U) := __tmp_6818(31, 24)
        arrayRegFiles(__tmp_6817 + 4.U) := __tmp_6818(39, 32)
        arrayRegFiles(__tmp_6817 + 5.U) := __tmp_6818(47, 40)
        arrayRegFiles(__tmp_6817 + 6.U) := __tmp_6818(55, 48)
        arrayRegFiles(__tmp_6817 + 7.U) := __tmp_6818(63, 56)

        CP := 18.U
      }

      is(18.U) {
        /*
        $64U.4 = *(SP + (2: SP)) [unsigned, U64, 8]  // $64U.4 = r0
        alloc printU64Hex$res@[24,11].119B3FA4: U64 [@18, 8]
        goto .19
        */


        val __tmp_6819 = ((SP + 2.U(16.W))).asUInt
        generalRegFiles(4.U) := Cat(
          arrayRegFiles(__tmp_6819 + 7.U),
          arrayRegFiles(__tmp_6819 + 6.U),
          arrayRegFiles(__tmp_6819 + 5.U),
          arrayRegFiles(__tmp_6819 + 4.U),
          arrayRegFiles(__tmp_6819 + 3.U),
          arrayRegFiles(__tmp_6819 + 2.U),
          arrayRegFiles(__tmp_6819 + 1.U),
          arrayRegFiles(__tmp_6819 + 0.U)
        ).asUInt

        CP := 19.U
      }

      is(19.U) {
        /*
        SP = SP + 34
        goto .20
        */


        SP := SP + 34.U

        CP := 20.U
      }

      is(20.U) {
        /*
        decl $ret: CP [@0, 2], $res: SP [@2, 2], buffer: SP [@4, 2], index: anvil.PrinterIndex.U [@6, 8], mask: anvil.PrinterIndex.U [@14, 8], n: U64 [@22, 8], digits: Z [@30, 8]
        *SP = (21: CP) [unsigned, CP, 2]  // $ret@0 = 1719
        *(SP + (2: SP)) = (SP - (16: SP)) [unsigned, SP, 2]  // $res@2 = -16
        *(SP + (4: SP)) = (8: SP) [unsigned, SP, 2]  // buffer = (8: SP)
        *(SP + (6: SP)) = DP [unsigned, anvil.PrinterIndex.U, 8]  // index = DP
        *(SP + (14: SP)) = (127: anvil.PrinterIndex.U) [unsigned, anvil.PrinterIndex.U, 8]  // mask = (127: anvil.PrinterIndex.U)
        *(SP + (22: SP)) = $64U.4 [unsigned, U64, 8]  // n = $64U.4
        *(SP + (30: SP)) = (16: Z) [signed, Z, 8]  // digits = (16: Z)
        goto .80
        */


        val __tmp_6820 = SP
        val __tmp_6821 = (21.U(16.W)).asUInt
        arrayRegFiles(__tmp_6820 + 0.U) := __tmp_6821(7, 0)
        arrayRegFiles(__tmp_6820 + 1.U) := __tmp_6821(15, 8)

        val __tmp_6822 = (SP + 2.U(16.W))
        val __tmp_6823 = ((SP - 16.U(16.W))).asUInt
        arrayRegFiles(__tmp_6822 + 0.U) := __tmp_6823(7, 0)
        arrayRegFiles(__tmp_6822 + 1.U) := __tmp_6823(15, 8)

        val __tmp_6824 = (SP + 4.U(16.W))
        val __tmp_6825 = (8.U(16.W)).asUInt
        arrayRegFiles(__tmp_6824 + 0.U) := __tmp_6825(7, 0)
        arrayRegFiles(__tmp_6824 + 1.U) := __tmp_6825(15, 8)

        val __tmp_6826 = (SP + 6.U(16.W))
        val __tmp_6827 = (DP).asUInt
        arrayRegFiles(__tmp_6826 + 0.U) := __tmp_6827(7, 0)
        arrayRegFiles(__tmp_6826 + 1.U) := __tmp_6827(15, 8)
        arrayRegFiles(__tmp_6826 + 2.U) := __tmp_6827(23, 16)
        arrayRegFiles(__tmp_6826 + 3.U) := __tmp_6827(31, 24)
        arrayRegFiles(__tmp_6826 + 4.U) := __tmp_6827(39, 32)
        arrayRegFiles(__tmp_6826 + 5.U) := __tmp_6827(47, 40)
        arrayRegFiles(__tmp_6826 + 6.U) := __tmp_6827(55, 48)
        arrayRegFiles(__tmp_6826 + 7.U) := __tmp_6827(63, 56)

        val __tmp_6828 = (SP + 14.U(16.W))
        val __tmp_6829 = (127.U(64.W)).asUInt
        arrayRegFiles(__tmp_6828 + 0.U) := __tmp_6829(7, 0)
        arrayRegFiles(__tmp_6828 + 1.U) := __tmp_6829(15, 8)
        arrayRegFiles(__tmp_6828 + 2.U) := __tmp_6829(23, 16)
        arrayRegFiles(__tmp_6828 + 3.U) := __tmp_6829(31, 24)
        arrayRegFiles(__tmp_6828 + 4.U) := __tmp_6829(39, 32)
        arrayRegFiles(__tmp_6828 + 5.U) := __tmp_6829(47, 40)
        arrayRegFiles(__tmp_6828 + 6.U) := __tmp_6829(55, 48)
        arrayRegFiles(__tmp_6828 + 7.U) := __tmp_6829(63, 56)

        val __tmp_6830 = (SP + 22.U(16.W))
        val __tmp_6831 = (generalRegFiles(4.U)).asUInt
        arrayRegFiles(__tmp_6830 + 0.U) := __tmp_6831(7, 0)
        arrayRegFiles(__tmp_6830 + 1.U) := __tmp_6831(15, 8)
        arrayRegFiles(__tmp_6830 + 2.U) := __tmp_6831(23, 16)
        arrayRegFiles(__tmp_6830 + 3.U) := __tmp_6831(31, 24)
        arrayRegFiles(__tmp_6830 + 4.U) := __tmp_6831(39, 32)
        arrayRegFiles(__tmp_6830 + 5.U) := __tmp_6831(47, 40)
        arrayRegFiles(__tmp_6830 + 6.U) := __tmp_6831(55, 48)
        arrayRegFiles(__tmp_6830 + 7.U) := __tmp_6831(63, 56)

        val __tmp_6832 = (SP + 30.U(16.W))
        val __tmp_6833 = (16.S(64.W)).asUInt
        arrayRegFiles(__tmp_6832 + 0.U) := __tmp_6833(7, 0)
        arrayRegFiles(__tmp_6832 + 1.U) := __tmp_6833(15, 8)
        arrayRegFiles(__tmp_6832 + 2.U) := __tmp_6833(23, 16)
        arrayRegFiles(__tmp_6832 + 3.U) := __tmp_6833(31, 24)
        arrayRegFiles(__tmp_6832 + 4.U) := __tmp_6833(39, 32)
        arrayRegFiles(__tmp_6832 + 5.U) := __tmp_6833(47, 40)
        arrayRegFiles(__tmp_6832 + 6.U) := __tmp_6833(55, 48)
        arrayRegFiles(__tmp_6832 + 7.U) := __tmp_6833(63, 56)

        CP := 80.U
      }

      is(21.U) {
        /*
        $64U.5 = **(SP + (2: SP)) [unsigned, U64, 8]  // $5 = $res
        undecl digits: Z [@30, 8], n: U64 [@22, 8], mask: anvil.PrinterIndex.U [@14, 8], index: anvil.PrinterIndex.U [@6, 8], buffer: SP [@4, 2], $res: SP [@2, 2], $ret: CP [@0, 2]
        goto .22
        */


        val __tmp_6834 = (Cat(
          arrayRegFiles((SP + 2.U(16.W)) + 1.U),
          arrayRegFiles((SP + 2.U(16.W)) + 0.U)
        )).asUInt
        generalRegFiles(5.U) := Cat(
          arrayRegFiles(__tmp_6834 + 7.U),
          arrayRegFiles(__tmp_6834 + 6.U),
          arrayRegFiles(__tmp_6834 + 5.U),
          arrayRegFiles(__tmp_6834 + 4.U),
          arrayRegFiles(__tmp_6834 + 3.U),
          arrayRegFiles(__tmp_6834 + 2.U),
          arrayRegFiles(__tmp_6834 + 1.U),
          arrayRegFiles(__tmp_6834 + 0.U)
        ).asUInt

        CP := 22.U
      }

      is(22.U) {
        /*
        SP = SP - 34
        goto .23
        */


        SP := SP - 34.U

        CP := 23.U
      }

      is(23.U) {
        /*
        DP = DP + ($64U.5 as DP)
        goto .24
        */


        DP := DP + generalRegFiles(5.U).asUInt

        CP := 24.U
      }

      is(24.U) {
        /*
        unalloc printU64Hex$res@[24,11].119B3FA4: U64 [@18, 8]
        *(((8: SP) + (12: SP)) + ((DP & (127: DP)) as SP)) = (10: U8) [unsigned, U8, 1]  // $display((DP & (127: DP))) = (10: U8)
        goto .25
        */


        val __tmp_6835 = ((8.U(16.W) + 12.U(16.W)) + (DP & 127.U(64.W)).asUInt)
        val __tmp_6836 = (10.U(8.W)).asUInt
        arrayRegFiles(__tmp_6835 + 0.U) := __tmp_6836(7, 0)

        CP := 25.U
      }

      is(25.U) {
        /*
        DP = DP + 1
        goto .26
        */


        DP := DP + 1.U

        CP := 26.U
      }

      is(26.U) {
        /*
        $64U.4 = *(SP + (2: SP)) [unsigned, U64, 8]  // $64U.4 = r0
        undecl r0: U64 [@2, 8]
        goto .27
        */


        val __tmp_6837 = ((SP + 2.U(16.W))).asUInt
        generalRegFiles(4.U) := Cat(
          arrayRegFiles(__tmp_6837 + 7.U),
          arrayRegFiles(__tmp_6837 + 6.U),
          arrayRegFiles(__tmp_6837 + 5.U),
          arrayRegFiles(__tmp_6837 + 4.U),
          arrayRegFiles(__tmp_6837 + 3.U),
          arrayRegFiles(__tmp_6837 + 2.U),
          arrayRegFiles(__tmp_6837 + 1.U),
          arrayRegFiles(__tmp_6837 + 0.U)
        ).asUInt

        CP := 27.U
      }

      is(27.U) {
        /*
        $1U.0 = ($64U.4 ≡ (9223372036854775808: U64))
        goto .28
        */



        generalRegFiles(0.U) := (generalRegFiles(4.U) === BigInt("9223372036854775808").U(64.W)).asUInt

        CP := 28.U
      }

      is(28.U) {
        /*
        if $1U.0 goto .30 else goto .1
        */


        CP := Mux((generalRegFiles(0.U).asUInt) === 1.U, 30.U, 1.U)
      }

      is(30.U) {
        /*
        decl r1: S64 [@2, 8]
        alloc shiftS64$res@[26,12].88CDFE08: S64 [@18, 8]
        goto .31
        */


        CP := 31.U
      }

      is(31.U) {
        /*
        SP = SP + 34
        goto .32
        */


        SP := SP + 34.U

        CP := 32.U
      }

      is(32.U) {
        /*
        decl $ret: CP [@0, 2], $res: SP [@2, 10], m: S64 [@12, 8], n: S64 [@20, 8]
        *SP = (33: CP) [unsigned, CP, 2]  // $ret@0 = 1720
        *(SP + (2: SP)) = (SP - (16: SP)) [unsigned, SP, 2]  // $res@2 = -16
        *(SP + (12: SP)) = (4611686018427387904: S64) [signed, S64, 8]  // m = (4611686018427387904: S64)
        *(SP + (20: SP)) = (60: S64) [signed, S64, 8]  // n = (60: S64)
        goto .174
        */


        val __tmp_6838 = SP
        val __tmp_6839 = (33.U(16.W)).asUInt
        arrayRegFiles(__tmp_6838 + 0.U) := __tmp_6839(7, 0)
        arrayRegFiles(__tmp_6838 + 1.U) := __tmp_6839(15, 8)

        val __tmp_6840 = (SP + 2.U(16.W))
        val __tmp_6841 = ((SP - 16.U(16.W))).asUInt
        arrayRegFiles(__tmp_6840 + 0.U) := __tmp_6841(7, 0)
        arrayRegFiles(__tmp_6840 + 1.U) := __tmp_6841(15, 8)

        val __tmp_6842 = (SP + 12.U(16.W))
        val __tmp_6843 = (BigInt("4611686018427387904").S(64.W)).asUInt
        arrayRegFiles(__tmp_6842 + 0.U) := __tmp_6843(7, 0)
        arrayRegFiles(__tmp_6842 + 1.U) := __tmp_6843(15, 8)
        arrayRegFiles(__tmp_6842 + 2.U) := __tmp_6843(23, 16)
        arrayRegFiles(__tmp_6842 + 3.U) := __tmp_6843(31, 24)
        arrayRegFiles(__tmp_6842 + 4.U) := __tmp_6843(39, 32)
        arrayRegFiles(__tmp_6842 + 5.U) := __tmp_6843(47, 40)
        arrayRegFiles(__tmp_6842 + 6.U) := __tmp_6843(55, 48)
        arrayRegFiles(__tmp_6842 + 7.U) := __tmp_6843(63, 56)

        val __tmp_6844 = (SP + 20.U(16.W))
        val __tmp_6845 = (60.S(64.W)).asUInt
        arrayRegFiles(__tmp_6844 + 0.U) := __tmp_6845(7, 0)
        arrayRegFiles(__tmp_6844 + 1.U) := __tmp_6845(15, 8)
        arrayRegFiles(__tmp_6844 + 2.U) := __tmp_6845(23, 16)
        arrayRegFiles(__tmp_6844 + 3.U) := __tmp_6845(31, 24)
        arrayRegFiles(__tmp_6844 + 4.U) := __tmp_6845(39, 32)
        arrayRegFiles(__tmp_6844 + 5.U) := __tmp_6845(47, 40)
        arrayRegFiles(__tmp_6844 + 6.U) := __tmp_6845(55, 48)
        arrayRegFiles(__tmp_6844 + 7.U) := __tmp_6845(63, 56)

        CP := 174.U
      }

      is(33.U) {
        /*
        $64S.2 = **(SP + (2: SP)) [signed, S64, 8]  // $2 = $res
        undecl n: S64 [@20, 8], m: S64 [@12, 8], $res: SP [@2, 10], $ret: CP [@0, 2]
        goto .34
        */


        val __tmp_6846 = (Cat(
          arrayRegFiles((SP + 2.U(16.W)) + 1.U),
          arrayRegFiles((SP + 2.U(16.W)) + 0.U)
        )).asUInt
        generalRegFiles(2.U) := Cat(
          arrayRegFiles(__tmp_6846 + 7.U),
          arrayRegFiles(__tmp_6846 + 6.U),
          arrayRegFiles(__tmp_6846 + 5.U),
          arrayRegFiles(__tmp_6846 + 4.U),
          arrayRegFiles(__tmp_6846 + 3.U),
          arrayRegFiles(__tmp_6846 + 2.U),
          arrayRegFiles(__tmp_6846 + 1.U),
          arrayRegFiles(__tmp_6846 + 0.U)
        ).asSInt.pad(GENERAL_REG_WIDTH).asUInt

        CP := 34.U
      }

      is(34.U) {
        /*
        SP = SP - 34
        goto .35
        */


        SP := SP - 34.U

        CP := 35.U
      }

      is(35.U) {
        /*
        *(SP + (2: SP)) = $64S.2 [signed, S64, 8]  // r1 = $64S.2
        goto .36
        */


        val __tmp_6847 = (SP + 2.U(16.W))
        val __tmp_6848 = (generalRegFiles(2.U).asSInt).asUInt
        arrayRegFiles(__tmp_6847 + 0.U) := __tmp_6848(7, 0)
        arrayRegFiles(__tmp_6847 + 1.U) := __tmp_6848(15, 8)
        arrayRegFiles(__tmp_6847 + 2.U) := __tmp_6848(23, 16)
        arrayRegFiles(__tmp_6847 + 3.U) := __tmp_6848(31, 24)
        arrayRegFiles(__tmp_6847 + 4.U) := __tmp_6848(39, 32)
        arrayRegFiles(__tmp_6847 + 5.U) := __tmp_6848(47, 40)
        arrayRegFiles(__tmp_6847 + 6.U) := __tmp_6848(55, 48)
        arrayRegFiles(__tmp_6847 + 7.U) := __tmp_6848(63, 56)

        CP := 36.U
      }

      is(36.U) {
        /*
        $64S.2 = *(SP + (2: SP)) [signed, S64, 8]  // $64S.2 = r1
        alloc printS64$res@[27,11].C4B52E32: U64 [@26, 8]
        goto .37
        */


        val __tmp_6849 = ((SP + 2.U(16.W))).asUInt
        generalRegFiles(2.U) := Cat(
          arrayRegFiles(__tmp_6849 + 7.U),
          arrayRegFiles(__tmp_6849 + 6.U),
          arrayRegFiles(__tmp_6849 + 5.U),
          arrayRegFiles(__tmp_6849 + 4.U),
          arrayRegFiles(__tmp_6849 + 3.U),
          arrayRegFiles(__tmp_6849 + 2.U),
          arrayRegFiles(__tmp_6849 + 1.U),
          arrayRegFiles(__tmp_6849 + 0.U)
        ).asSInt.pad(GENERAL_REG_WIDTH).asUInt

        CP := 37.U
      }

      is(37.U) {
        /*
        SP = SP + 34
        goto .38
        */


        SP := SP + 34.U

        CP := 38.U
      }

      is(38.U) {
        /*
        decl $ret: CP [@0, 2], $res: SP [@2, 2], buffer: SP [@4, 2], index: anvil.PrinterIndex.U [@6, 8], mask: anvil.PrinterIndex.U [@14, 8], n: S64 [@22, 8]
        *SP = (39: CP) [unsigned, CP, 2]  // $ret@0 = 1721
        *(SP + (2: SP)) = (SP - (8: SP)) [unsigned, SP, 2]  // $res@2 = -8
        *(SP + (4: SP)) = (8: SP) [unsigned, SP, 2]  // buffer = (8: SP)
        *(SP + (6: SP)) = DP [unsigned, anvil.PrinterIndex.U, 8]  // index = DP
        *(SP + (14: SP)) = (127: anvil.PrinterIndex.U) [unsigned, anvil.PrinterIndex.U, 8]  // mask = (127: anvil.PrinterIndex.U)
        *(SP + (22: SP)) = $64S.2 [signed, S64, 8]  // n = $64S.2
        goto .207
        */


        val __tmp_6850 = SP
        val __tmp_6851 = (39.U(16.W)).asUInt
        arrayRegFiles(__tmp_6850 + 0.U) := __tmp_6851(7, 0)
        arrayRegFiles(__tmp_6850 + 1.U) := __tmp_6851(15, 8)

        val __tmp_6852 = (SP + 2.U(16.W))
        val __tmp_6853 = ((SP - 8.U(16.W))).asUInt
        arrayRegFiles(__tmp_6852 + 0.U) := __tmp_6853(7, 0)
        arrayRegFiles(__tmp_6852 + 1.U) := __tmp_6853(15, 8)

        val __tmp_6854 = (SP + 4.U(16.W))
        val __tmp_6855 = (8.U(16.W)).asUInt
        arrayRegFiles(__tmp_6854 + 0.U) := __tmp_6855(7, 0)
        arrayRegFiles(__tmp_6854 + 1.U) := __tmp_6855(15, 8)

        val __tmp_6856 = (SP + 6.U(16.W))
        val __tmp_6857 = (DP).asUInt
        arrayRegFiles(__tmp_6856 + 0.U) := __tmp_6857(7, 0)
        arrayRegFiles(__tmp_6856 + 1.U) := __tmp_6857(15, 8)
        arrayRegFiles(__tmp_6856 + 2.U) := __tmp_6857(23, 16)
        arrayRegFiles(__tmp_6856 + 3.U) := __tmp_6857(31, 24)
        arrayRegFiles(__tmp_6856 + 4.U) := __tmp_6857(39, 32)
        arrayRegFiles(__tmp_6856 + 5.U) := __tmp_6857(47, 40)
        arrayRegFiles(__tmp_6856 + 6.U) := __tmp_6857(55, 48)
        arrayRegFiles(__tmp_6856 + 7.U) := __tmp_6857(63, 56)

        val __tmp_6858 = (SP + 14.U(16.W))
        val __tmp_6859 = (127.U(64.W)).asUInt
        arrayRegFiles(__tmp_6858 + 0.U) := __tmp_6859(7, 0)
        arrayRegFiles(__tmp_6858 + 1.U) := __tmp_6859(15, 8)
        arrayRegFiles(__tmp_6858 + 2.U) := __tmp_6859(23, 16)
        arrayRegFiles(__tmp_6858 + 3.U) := __tmp_6859(31, 24)
        arrayRegFiles(__tmp_6858 + 4.U) := __tmp_6859(39, 32)
        arrayRegFiles(__tmp_6858 + 5.U) := __tmp_6859(47, 40)
        arrayRegFiles(__tmp_6858 + 6.U) := __tmp_6859(55, 48)
        arrayRegFiles(__tmp_6858 + 7.U) := __tmp_6859(63, 56)

        val __tmp_6860 = (SP + 22.U(16.W))
        val __tmp_6861 = (generalRegFiles(2.U).asSInt).asUInt
        arrayRegFiles(__tmp_6860 + 0.U) := __tmp_6861(7, 0)
        arrayRegFiles(__tmp_6860 + 1.U) := __tmp_6861(15, 8)
        arrayRegFiles(__tmp_6860 + 2.U) := __tmp_6861(23, 16)
        arrayRegFiles(__tmp_6860 + 3.U) := __tmp_6861(31, 24)
        arrayRegFiles(__tmp_6860 + 4.U) := __tmp_6861(39, 32)
        arrayRegFiles(__tmp_6860 + 5.U) := __tmp_6861(47, 40)
        arrayRegFiles(__tmp_6860 + 6.U) := __tmp_6861(55, 48)
        arrayRegFiles(__tmp_6860 + 7.U) := __tmp_6861(63, 56)

        CP := 207.U
      }

      is(39.U) {
        /*
        $64U.5 = **(SP + (2: SP)) [unsigned, U64, 8]  // $5 = $res
        undecl n: S64 [@22, 8], mask: anvil.PrinterIndex.U [@14, 8], index: anvil.PrinterIndex.U [@6, 8], buffer: SP [@4, 2], $res: SP [@2, 2], $ret: CP [@0, 2]
        goto .40
        */


        val __tmp_6862 = (Cat(
          arrayRegFiles((SP + 2.U(16.W)) + 1.U),
          arrayRegFiles((SP + 2.U(16.W)) + 0.U)
        )).asUInt
        generalRegFiles(5.U) := Cat(
          arrayRegFiles(__tmp_6862 + 7.U),
          arrayRegFiles(__tmp_6862 + 6.U),
          arrayRegFiles(__tmp_6862 + 5.U),
          arrayRegFiles(__tmp_6862 + 4.U),
          arrayRegFiles(__tmp_6862 + 3.U),
          arrayRegFiles(__tmp_6862 + 2.U),
          arrayRegFiles(__tmp_6862 + 1.U),
          arrayRegFiles(__tmp_6862 + 0.U)
        ).asUInt

        CP := 40.U
      }

      is(40.U) {
        /*
        SP = SP - 34
        goto .41
        */


        SP := SP - 34.U

        CP := 41.U
      }

      is(41.U) {
        /*
        DP = DP + ($64U.5 as DP)
        goto .42
        */


        DP := DP + generalRegFiles(5.U).asUInt

        CP := 42.U
      }

      is(42.U) {
        /*
        unalloc printS64$res@[27,11].C4B52E32: U64 [@26, 8]
        *(((8: SP) + (12: SP)) + ((DP & (127: DP)) as SP)) = (10: U8) [unsigned, U8, 1]  // $display((DP & (127: DP))) = (10: U8)
        goto .43
        */


        val __tmp_6863 = ((8.U(16.W) + 12.U(16.W)) + (DP & 127.U(64.W)).asUInt)
        val __tmp_6864 = (10.U(8.W)).asUInt
        arrayRegFiles(__tmp_6863 + 0.U) := __tmp_6864(7, 0)

        CP := 43.U
      }

      is(43.U) {
        /*
        DP = DP + 1
        goto .44
        */


        DP := DP + 1.U

        CP := 44.U
      }

      is(44.U) {
        /*
        $64S.2 = *(SP + (2: SP)) [signed, S64, 8]  // $64S.2 = r1
        undecl r1: S64 [@2, 8]
        goto .45
        */


        val __tmp_6865 = ((SP + 2.U(16.W))).asUInt
        generalRegFiles(2.U) := Cat(
          arrayRegFiles(__tmp_6865 + 7.U),
          arrayRegFiles(__tmp_6865 + 6.U),
          arrayRegFiles(__tmp_6865 + 5.U),
          arrayRegFiles(__tmp_6865 + 4.U),
          arrayRegFiles(__tmp_6865 + 3.U),
          arrayRegFiles(__tmp_6865 + 2.U),
          arrayRegFiles(__tmp_6865 + 1.U),
          arrayRegFiles(__tmp_6865 + 0.U)
        ).asSInt.pad(GENERAL_REG_WIDTH).asUInt

        CP := 45.U
      }

      is(45.U) {
        /*
        $1U.0 = ($64S.2 ≡ (4611686018427387904: S64))
        goto .46
        */



        generalRegFiles(0.U) := (generalRegFiles(2.U).asSInt === BigInt("4611686018427387904").S(64.W)).asUInt

        CP := 46.U
      }

      is(46.U) {
        /*
        if $1U.0 goto .48 else goto .1
        */


        CP := Mux((generalRegFiles(0.U).asUInt) === 1.U, 48.U, 1.U)
      }

      is(48.U) {
        /*
        goto $ret@0
        */


        CP := Cat(
          arrayRegFiles(SP + 0.U + 1.U),
          arrayRegFiles(SP + 0.U + 0.U)
        )

      }

      is(49.U) {
        /*
        $64U.0 = *(SP + (12: SP)) [unsigned, U64, 8]  // m
        $64U.1 = *(SP + (20: SP)) [unsigned, U64, 8]  // n
        goto .50
        */


        val __tmp_6866 = ((SP + 12.U(16.W))).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_6866 + 7.U),
          arrayRegFiles(__tmp_6866 + 6.U),
          arrayRegFiles(__tmp_6866 + 5.U),
          arrayRegFiles(__tmp_6866 + 4.U),
          arrayRegFiles(__tmp_6866 + 3.U),
          arrayRegFiles(__tmp_6866 + 2.U),
          arrayRegFiles(__tmp_6866 + 1.U),
          arrayRegFiles(__tmp_6866 + 0.U)
        ).asUInt

        val __tmp_6867 = ((SP + 20.U(16.W))).asUInt
        generalRegFiles(1.U) := Cat(
          arrayRegFiles(__tmp_6867 + 7.U),
          arrayRegFiles(__tmp_6867 + 6.U),
          arrayRegFiles(__tmp_6867 + 5.U),
          arrayRegFiles(__tmp_6867 + 4.U),
          arrayRegFiles(__tmp_6867 + 3.U),
          arrayRegFiles(__tmp_6867 + 2.U),
          arrayRegFiles(__tmp_6867 + 1.U),
          arrayRegFiles(__tmp_6867 + 0.U)
        ).asUInt

        CP := 50.U
      }

      is(50.U) {
        /*
        $64U.7 = *(SP + (12: SP)) [unsigned, U64, 8]  // $64U.7 = m
        alloc printU64Hex$res@[7,11].824E3759: U64 [@28, 8]
        goto .51
        */


        val __tmp_6868 = ((SP + 12.U(16.W))).asUInt
        generalRegFiles(7.U) := Cat(
          arrayRegFiles(__tmp_6868 + 7.U),
          arrayRegFiles(__tmp_6868 + 6.U),
          arrayRegFiles(__tmp_6868 + 5.U),
          arrayRegFiles(__tmp_6868 + 4.U),
          arrayRegFiles(__tmp_6868 + 3.U),
          arrayRegFiles(__tmp_6868 + 2.U),
          arrayRegFiles(__tmp_6868 + 1.U),
          arrayRegFiles(__tmp_6868 + 0.U)
        ).asUInt

        CP := 51.U
      }

      is(51.U) {
        /*
        SP = SP + 60
        goto .52
        */


        SP := SP + 60.U

        CP := 52.U
      }

      is(52.U) {
        /*
        decl $ret: CP [@0, 2], $res: SP [@2, 2], buffer: SP [@4, 2], index: anvil.PrinterIndex.U [@6, 8], mask: anvil.PrinterIndex.U [@14, 8], n: U64 [@22, 8], digits: Z [@30, 8]
        *SP = (53: CP) [unsigned, CP, 2]  // $ret@0 = 1722
        *(SP + (2: SP)) = (SP - (32: SP)) [unsigned, SP, 2]  // $res@2 = -32
        *(SP + (4: SP)) = (8: SP) [unsigned, SP, 2]  // buffer = (8: SP)
        *(SP + (6: SP)) = DP [unsigned, anvil.PrinterIndex.U, 8]  // index = DP
        *(SP + (14: SP)) = (127: anvil.PrinterIndex.U) [unsigned, anvil.PrinterIndex.U, 8]  // mask = (127: anvil.PrinterIndex.U)
        *(SP + (22: SP)) = $64U.7 [unsigned, U64, 8]  // n = $64U.7
        *(SP + (30: SP)) = (16: Z) [signed, Z, 8]  // digits = (16: Z)
        goto .80
        */


        val __tmp_6869 = SP
        val __tmp_6870 = (53.U(16.W)).asUInt
        arrayRegFiles(__tmp_6869 + 0.U) := __tmp_6870(7, 0)
        arrayRegFiles(__tmp_6869 + 1.U) := __tmp_6870(15, 8)

        val __tmp_6871 = (SP + 2.U(16.W))
        val __tmp_6872 = ((SP - 32.U(16.W))).asUInt
        arrayRegFiles(__tmp_6871 + 0.U) := __tmp_6872(7, 0)
        arrayRegFiles(__tmp_6871 + 1.U) := __tmp_6872(15, 8)

        val __tmp_6873 = (SP + 4.U(16.W))
        val __tmp_6874 = (8.U(16.W)).asUInt
        arrayRegFiles(__tmp_6873 + 0.U) := __tmp_6874(7, 0)
        arrayRegFiles(__tmp_6873 + 1.U) := __tmp_6874(15, 8)

        val __tmp_6875 = (SP + 6.U(16.W))
        val __tmp_6876 = (DP).asUInt
        arrayRegFiles(__tmp_6875 + 0.U) := __tmp_6876(7, 0)
        arrayRegFiles(__tmp_6875 + 1.U) := __tmp_6876(15, 8)
        arrayRegFiles(__tmp_6875 + 2.U) := __tmp_6876(23, 16)
        arrayRegFiles(__tmp_6875 + 3.U) := __tmp_6876(31, 24)
        arrayRegFiles(__tmp_6875 + 4.U) := __tmp_6876(39, 32)
        arrayRegFiles(__tmp_6875 + 5.U) := __tmp_6876(47, 40)
        arrayRegFiles(__tmp_6875 + 6.U) := __tmp_6876(55, 48)
        arrayRegFiles(__tmp_6875 + 7.U) := __tmp_6876(63, 56)

        val __tmp_6877 = (SP + 14.U(16.W))
        val __tmp_6878 = (127.U(64.W)).asUInt
        arrayRegFiles(__tmp_6877 + 0.U) := __tmp_6878(7, 0)
        arrayRegFiles(__tmp_6877 + 1.U) := __tmp_6878(15, 8)
        arrayRegFiles(__tmp_6877 + 2.U) := __tmp_6878(23, 16)
        arrayRegFiles(__tmp_6877 + 3.U) := __tmp_6878(31, 24)
        arrayRegFiles(__tmp_6877 + 4.U) := __tmp_6878(39, 32)
        arrayRegFiles(__tmp_6877 + 5.U) := __tmp_6878(47, 40)
        arrayRegFiles(__tmp_6877 + 6.U) := __tmp_6878(55, 48)
        arrayRegFiles(__tmp_6877 + 7.U) := __tmp_6878(63, 56)

        val __tmp_6879 = (SP + 22.U(16.W))
        val __tmp_6880 = (generalRegFiles(7.U)).asUInt
        arrayRegFiles(__tmp_6879 + 0.U) := __tmp_6880(7, 0)
        arrayRegFiles(__tmp_6879 + 1.U) := __tmp_6880(15, 8)
        arrayRegFiles(__tmp_6879 + 2.U) := __tmp_6880(23, 16)
        arrayRegFiles(__tmp_6879 + 3.U) := __tmp_6880(31, 24)
        arrayRegFiles(__tmp_6879 + 4.U) := __tmp_6880(39, 32)
        arrayRegFiles(__tmp_6879 + 5.U) := __tmp_6880(47, 40)
        arrayRegFiles(__tmp_6879 + 6.U) := __tmp_6880(55, 48)
        arrayRegFiles(__tmp_6879 + 7.U) := __tmp_6880(63, 56)

        val __tmp_6881 = (SP + 30.U(16.W))
        val __tmp_6882 = (16.S(64.W)).asUInt
        arrayRegFiles(__tmp_6881 + 0.U) := __tmp_6882(7, 0)
        arrayRegFiles(__tmp_6881 + 1.U) := __tmp_6882(15, 8)
        arrayRegFiles(__tmp_6881 + 2.U) := __tmp_6882(23, 16)
        arrayRegFiles(__tmp_6881 + 3.U) := __tmp_6882(31, 24)
        arrayRegFiles(__tmp_6881 + 4.U) := __tmp_6882(39, 32)
        arrayRegFiles(__tmp_6881 + 5.U) := __tmp_6882(47, 40)
        arrayRegFiles(__tmp_6881 + 6.U) := __tmp_6882(55, 48)
        arrayRegFiles(__tmp_6881 + 7.U) := __tmp_6882(63, 56)

        CP := 80.U
      }

      is(53.U) {
        /*
        $64U.8 = **(SP + (2: SP)) [unsigned, U64, 8]  // $8 = $res
        undecl digits: Z [@30, 8], n: U64 [@22, 8], mask: anvil.PrinterIndex.U [@14, 8], index: anvil.PrinterIndex.U [@6, 8], buffer: SP [@4, 2], $res: SP [@2, 2], $ret: CP [@0, 2]
        goto .54
        */


        val __tmp_6883 = (Cat(
          arrayRegFiles((SP + 2.U(16.W)) + 1.U),
          arrayRegFiles((SP + 2.U(16.W)) + 0.U)
        )).asUInt
        generalRegFiles(8.U) := Cat(
          arrayRegFiles(__tmp_6883 + 7.U),
          arrayRegFiles(__tmp_6883 + 6.U),
          arrayRegFiles(__tmp_6883 + 5.U),
          arrayRegFiles(__tmp_6883 + 4.U),
          arrayRegFiles(__tmp_6883 + 3.U),
          arrayRegFiles(__tmp_6883 + 2.U),
          arrayRegFiles(__tmp_6883 + 1.U),
          arrayRegFiles(__tmp_6883 + 0.U)
        ).asUInt

        CP := 54.U
      }

      is(54.U) {
        /*
        SP = SP - 60
        goto .55
        */


        SP := SP - 60.U

        CP := 55.U
      }

      is(55.U) {
        /*
        DP = DP + ($64U.8 as DP)
        goto .56
        */


        DP := DP + generalRegFiles(8.U).asUInt

        CP := 56.U
      }

      is(56.U) {
        /*
        *(((8: SP) + (12: SP)) + ((DP & (127: DP)) as SP)) = (10: U8) [unsigned, U8, 1]  // $display((DP & (127: DP))) = (10: U8)
        goto .57
        */


        val __tmp_6884 = ((8.U(16.W) + 12.U(16.W)) + (DP & 127.U(64.W)).asUInt)
        val __tmp_6885 = (10.U(8.W)).asUInt
        arrayRegFiles(__tmp_6884 + 0.U) := __tmp_6885(7, 0)

        CP := 57.U
      }

      is(57.U) {
        /*
        DP = DP + 1
        goto .58
        */


        DP := DP + 1.U

        CP := 58.U
      }

      is(58.U) {
        /*
        decl i: U64 [@36, 8]
        $64U.7 = *(SP + (12: SP)) [unsigned, U64, 8]  // $64U.7 = m
        $64U.9 = *(SP + (20: SP)) [unsigned, U64, 8]  // $64U.9 = n
        alloc shrU64$res@[8,13].1CBD5A96: U64 [@44, 8]
        goto .59
        */


        val __tmp_6886 = ((SP + 12.U(16.W))).asUInt
        generalRegFiles(7.U) := Cat(
          arrayRegFiles(__tmp_6886 + 7.U),
          arrayRegFiles(__tmp_6886 + 6.U),
          arrayRegFiles(__tmp_6886 + 5.U),
          arrayRegFiles(__tmp_6886 + 4.U),
          arrayRegFiles(__tmp_6886 + 3.U),
          arrayRegFiles(__tmp_6886 + 2.U),
          arrayRegFiles(__tmp_6886 + 1.U),
          arrayRegFiles(__tmp_6886 + 0.U)
        ).asUInt

        val __tmp_6887 = ((SP + 20.U(16.W))).asUInt
        generalRegFiles(9.U) := Cat(
          arrayRegFiles(__tmp_6887 + 7.U),
          arrayRegFiles(__tmp_6887 + 6.U),
          arrayRegFiles(__tmp_6887 + 5.U),
          arrayRegFiles(__tmp_6887 + 4.U),
          arrayRegFiles(__tmp_6887 + 3.U),
          arrayRegFiles(__tmp_6887 + 2.U),
          arrayRegFiles(__tmp_6887 + 1.U),
          arrayRegFiles(__tmp_6887 + 0.U)
        ).asUInt

        CP := 59.U
      }

      is(59.U) {
        /*
        SP = SP + 60
        goto .60
        */


        SP := SP + 60.U

        CP := 60.U
      }

      is(60.U) {
        /*
        decl $ret: CP [@0, 2], $res: SP [@2, 2], n: U64 [@4, 8], m: U64 [@12, 8]
        *SP = (61: CP) [unsigned, CP, 2]  // $ret@0 = 1723
        *(SP + (2: SP)) = (SP - (16: SP)) [unsigned, SP, 2]  // $res@2 = -16
        *(SP + (4: SP)) = $64U.7 [unsigned, U64, 8]  // n = $64U.7
        *(SP + (12: SP)) = $64U.9 [unsigned, U64, 8]  // m = $64U.9
        goto .397
        */


        val __tmp_6888 = SP
        val __tmp_6889 = (61.U(16.W)).asUInt
        arrayRegFiles(__tmp_6888 + 0.U) := __tmp_6889(7, 0)
        arrayRegFiles(__tmp_6888 + 1.U) := __tmp_6889(15, 8)

        val __tmp_6890 = (SP + 2.U(16.W))
        val __tmp_6891 = ((SP - 16.U(16.W))).asUInt
        arrayRegFiles(__tmp_6890 + 0.U) := __tmp_6891(7, 0)
        arrayRegFiles(__tmp_6890 + 1.U) := __tmp_6891(15, 8)

        val __tmp_6892 = (SP + 4.U(16.W))
        val __tmp_6893 = (generalRegFiles(7.U)).asUInt
        arrayRegFiles(__tmp_6892 + 0.U) := __tmp_6893(7, 0)
        arrayRegFiles(__tmp_6892 + 1.U) := __tmp_6893(15, 8)
        arrayRegFiles(__tmp_6892 + 2.U) := __tmp_6893(23, 16)
        arrayRegFiles(__tmp_6892 + 3.U) := __tmp_6893(31, 24)
        arrayRegFiles(__tmp_6892 + 4.U) := __tmp_6893(39, 32)
        arrayRegFiles(__tmp_6892 + 5.U) := __tmp_6893(47, 40)
        arrayRegFiles(__tmp_6892 + 6.U) := __tmp_6893(55, 48)
        arrayRegFiles(__tmp_6892 + 7.U) := __tmp_6893(63, 56)

        val __tmp_6894 = (SP + 12.U(16.W))
        val __tmp_6895 = (generalRegFiles(9.U)).asUInt
        arrayRegFiles(__tmp_6894 + 0.U) := __tmp_6895(7, 0)
        arrayRegFiles(__tmp_6894 + 1.U) := __tmp_6895(15, 8)
        arrayRegFiles(__tmp_6894 + 2.U) := __tmp_6895(23, 16)
        arrayRegFiles(__tmp_6894 + 3.U) := __tmp_6895(31, 24)
        arrayRegFiles(__tmp_6894 + 4.U) := __tmp_6895(39, 32)
        arrayRegFiles(__tmp_6894 + 5.U) := __tmp_6895(47, 40)
        arrayRegFiles(__tmp_6894 + 6.U) := __tmp_6895(55, 48)
        arrayRegFiles(__tmp_6894 + 7.U) := __tmp_6895(63, 56)

        CP := 397.U
      }

      is(61.U) {
        /*
        $64U.10 = **(SP + (2: SP)) [unsigned, U64, 8]  // $10 = $res
        undecl m: U64 [@12, 8], n: U64 [@4, 8], $res: SP [@2, 2], $ret: CP [@0, 2]
        goto .62
        */


        val __tmp_6896 = (Cat(
          arrayRegFiles((SP + 2.U(16.W)) + 1.U),
          arrayRegFiles((SP + 2.U(16.W)) + 0.U)
        )).asUInt
        generalRegFiles(10.U) := Cat(
          arrayRegFiles(__tmp_6896 + 7.U),
          arrayRegFiles(__tmp_6896 + 6.U),
          arrayRegFiles(__tmp_6896 + 5.U),
          arrayRegFiles(__tmp_6896 + 4.U),
          arrayRegFiles(__tmp_6896 + 3.U),
          arrayRegFiles(__tmp_6896 + 2.U),
          arrayRegFiles(__tmp_6896 + 1.U),
          arrayRegFiles(__tmp_6896 + 0.U)
        ).asUInt

        CP := 62.U
      }

      is(62.U) {
        /*
        SP = SP - 60
        goto .63
        */


        SP := SP - 60.U

        CP := 63.U
      }

      is(63.U) {
        /*
        *(SP + (36: SP)) = $64U.10 [signed, U64, 8]  // i = $64U.10
        goto .64
        */


        val __tmp_6897 = (SP + 36.U(16.W))
        val __tmp_6898 = (generalRegFiles(10.U)).asUInt
        arrayRegFiles(__tmp_6897 + 0.U) := __tmp_6898(7, 0)
        arrayRegFiles(__tmp_6897 + 1.U) := __tmp_6898(15, 8)
        arrayRegFiles(__tmp_6897 + 2.U) := __tmp_6898(23, 16)
        arrayRegFiles(__tmp_6897 + 3.U) := __tmp_6898(31, 24)
        arrayRegFiles(__tmp_6897 + 4.U) := __tmp_6898(39, 32)
        arrayRegFiles(__tmp_6897 + 5.U) := __tmp_6898(47, 40)
        arrayRegFiles(__tmp_6897 + 6.U) := __tmp_6898(55, 48)
        arrayRegFiles(__tmp_6897 + 7.U) := __tmp_6898(63, 56)

        CP := 64.U
      }

      is(64.U) {
        /*
        $64U.7 = *(SP + (36: SP)) [unsigned, U64, 8]  // $64U.7 = i
        alloc printU64Hex$res@[9,11].ACDC3772: U64 [@52, 8]
        goto .65
        */


        val __tmp_6899 = ((SP + 36.U(16.W))).asUInt
        generalRegFiles(7.U) := Cat(
          arrayRegFiles(__tmp_6899 + 7.U),
          arrayRegFiles(__tmp_6899 + 6.U),
          arrayRegFiles(__tmp_6899 + 5.U),
          arrayRegFiles(__tmp_6899 + 4.U),
          arrayRegFiles(__tmp_6899 + 3.U),
          arrayRegFiles(__tmp_6899 + 2.U),
          arrayRegFiles(__tmp_6899 + 1.U),
          arrayRegFiles(__tmp_6899 + 0.U)
        ).asUInt

        CP := 65.U
      }

      is(65.U) {
        /*
        SP = SP + 60
        goto .66
        */


        SP := SP + 60.U

        CP := 66.U
      }

      is(66.U) {
        /*
        decl $ret: CP [@0, 2], $res: SP [@2, 2], buffer: SP [@4, 2], index: anvil.PrinterIndex.U [@6, 8], mask: anvil.PrinterIndex.U [@14, 8], n: U64 [@22, 8], digits: Z [@30, 8]
        *SP = (67: CP) [unsigned, CP, 2]  // $ret@0 = 1724
        *(SP + (2: SP)) = (SP - (8: SP)) [unsigned, SP, 2]  // $res@2 = -8
        *(SP + (4: SP)) = (8: SP) [unsigned, SP, 2]  // buffer = (8: SP)
        *(SP + (6: SP)) = DP [unsigned, anvil.PrinterIndex.U, 8]  // index = DP
        *(SP + (14: SP)) = (127: anvil.PrinterIndex.U) [unsigned, anvil.PrinterIndex.U, 8]  // mask = (127: anvil.PrinterIndex.U)
        *(SP + (22: SP)) = $64U.7 [unsigned, U64, 8]  // n = $64U.7
        *(SP + (30: SP)) = (16: Z) [signed, Z, 8]  // digits = (16: Z)
        goto .80
        */


        val __tmp_6900 = SP
        val __tmp_6901 = (67.U(16.W)).asUInt
        arrayRegFiles(__tmp_6900 + 0.U) := __tmp_6901(7, 0)
        arrayRegFiles(__tmp_6900 + 1.U) := __tmp_6901(15, 8)

        val __tmp_6902 = (SP + 2.U(16.W))
        val __tmp_6903 = ((SP - 8.U(16.W))).asUInt
        arrayRegFiles(__tmp_6902 + 0.U) := __tmp_6903(7, 0)
        arrayRegFiles(__tmp_6902 + 1.U) := __tmp_6903(15, 8)

        val __tmp_6904 = (SP + 4.U(16.W))
        val __tmp_6905 = (8.U(16.W)).asUInt
        arrayRegFiles(__tmp_6904 + 0.U) := __tmp_6905(7, 0)
        arrayRegFiles(__tmp_6904 + 1.U) := __tmp_6905(15, 8)

        val __tmp_6906 = (SP + 6.U(16.W))
        val __tmp_6907 = (DP).asUInt
        arrayRegFiles(__tmp_6906 + 0.U) := __tmp_6907(7, 0)
        arrayRegFiles(__tmp_6906 + 1.U) := __tmp_6907(15, 8)
        arrayRegFiles(__tmp_6906 + 2.U) := __tmp_6907(23, 16)
        arrayRegFiles(__tmp_6906 + 3.U) := __tmp_6907(31, 24)
        arrayRegFiles(__tmp_6906 + 4.U) := __tmp_6907(39, 32)
        arrayRegFiles(__tmp_6906 + 5.U) := __tmp_6907(47, 40)
        arrayRegFiles(__tmp_6906 + 6.U) := __tmp_6907(55, 48)
        arrayRegFiles(__tmp_6906 + 7.U) := __tmp_6907(63, 56)

        val __tmp_6908 = (SP + 14.U(16.W))
        val __tmp_6909 = (127.U(64.W)).asUInt
        arrayRegFiles(__tmp_6908 + 0.U) := __tmp_6909(7, 0)
        arrayRegFiles(__tmp_6908 + 1.U) := __tmp_6909(15, 8)
        arrayRegFiles(__tmp_6908 + 2.U) := __tmp_6909(23, 16)
        arrayRegFiles(__tmp_6908 + 3.U) := __tmp_6909(31, 24)
        arrayRegFiles(__tmp_6908 + 4.U) := __tmp_6909(39, 32)
        arrayRegFiles(__tmp_6908 + 5.U) := __tmp_6909(47, 40)
        arrayRegFiles(__tmp_6908 + 6.U) := __tmp_6909(55, 48)
        arrayRegFiles(__tmp_6908 + 7.U) := __tmp_6909(63, 56)

        val __tmp_6910 = (SP + 22.U(16.W))
        val __tmp_6911 = (generalRegFiles(7.U)).asUInt
        arrayRegFiles(__tmp_6910 + 0.U) := __tmp_6911(7, 0)
        arrayRegFiles(__tmp_6910 + 1.U) := __tmp_6911(15, 8)
        arrayRegFiles(__tmp_6910 + 2.U) := __tmp_6911(23, 16)
        arrayRegFiles(__tmp_6910 + 3.U) := __tmp_6911(31, 24)
        arrayRegFiles(__tmp_6910 + 4.U) := __tmp_6911(39, 32)
        arrayRegFiles(__tmp_6910 + 5.U) := __tmp_6911(47, 40)
        arrayRegFiles(__tmp_6910 + 6.U) := __tmp_6911(55, 48)
        arrayRegFiles(__tmp_6910 + 7.U) := __tmp_6911(63, 56)

        val __tmp_6912 = (SP + 30.U(16.W))
        val __tmp_6913 = (16.S(64.W)).asUInt
        arrayRegFiles(__tmp_6912 + 0.U) := __tmp_6913(7, 0)
        arrayRegFiles(__tmp_6912 + 1.U) := __tmp_6913(15, 8)
        arrayRegFiles(__tmp_6912 + 2.U) := __tmp_6913(23, 16)
        arrayRegFiles(__tmp_6912 + 3.U) := __tmp_6913(31, 24)
        arrayRegFiles(__tmp_6912 + 4.U) := __tmp_6913(39, 32)
        arrayRegFiles(__tmp_6912 + 5.U) := __tmp_6913(47, 40)
        arrayRegFiles(__tmp_6912 + 6.U) := __tmp_6913(55, 48)
        arrayRegFiles(__tmp_6912 + 7.U) := __tmp_6913(63, 56)

        CP := 80.U
      }

      is(67.U) {
        /*
        $64U.8 = **(SP + (2: SP)) [unsigned, U64, 8]  // $8 = $res
        undecl digits: Z [@30, 8], n: U64 [@22, 8], mask: anvil.PrinterIndex.U [@14, 8], index: anvil.PrinterIndex.U [@6, 8], buffer: SP [@4, 2], $res: SP [@2, 2], $ret: CP [@0, 2]
        goto .68
        */


        val __tmp_6914 = (Cat(
          arrayRegFiles((SP + 2.U(16.W)) + 1.U),
          arrayRegFiles((SP + 2.U(16.W)) + 0.U)
        )).asUInt
        generalRegFiles(8.U) := Cat(
          arrayRegFiles(__tmp_6914 + 7.U),
          arrayRegFiles(__tmp_6914 + 6.U),
          arrayRegFiles(__tmp_6914 + 5.U),
          arrayRegFiles(__tmp_6914 + 4.U),
          arrayRegFiles(__tmp_6914 + 3.U),
          arrayRegFiles(__tmp_6914 + 2.U),
          arrayRegFiles(__tmp_6914 + 1.U),
          arrayRegFiles(__tmp_6914 + 0.U)
        ).asUInt

        CP := 68.U
      }

      is(68.U) {
        /*
        SP = SP - 60
        goto .69
        */


        SP := SP - 60.U

        CP := 69.U
      }

      is(69.U) {
        /*
        DP = DP + ($64U.8 as DP)
        goto .70
        */


        DP := DP + generalRegFiles(8.U).asUInt

        CP := 70.U
      }

      is(70.U) {
        /*
        unalloc printU64Hex$res@[9,11].ACDC3772: U64 [@52, 8]
        *(((8: SP) + (12: SP)) + ((DP & (127: DP)) as SP)) = (10: U8) [unsigned, U8, 1]  // $display((DP & (127: DP))) = (10: U8)
        goto .71
        */


        val __tmp_6915 = ((8.U(16.W) + 12.U(16.W)) + (DP & 127.U(64.W)).asUInt)
        val __tmp_6916 = (10.U(8.W)).asUInt
        arrayRegFiles(__tmp_6915 + 0.U) := __tmp_6916(7, 0)

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
        $64U.7 = *(SP + (36: SP)) [unsigned, U64, 8]  // $64U.7 = i
        $64U.9 = *(SP + (20: SP)) [unsigned, U64, 8]  // $64U.9 = n
        alloc shlU64$res@[10,9].6BE26F25: U64 [@52, 8]
        goto .73
        */


        val __tmp_6917 = ((SP + 36.U(16.W))).asUInt
        generalRegFiles(7.U) := Cat(
          arrayRegFiles(__tmp_6917 + 7.U),
          arrayRegFiles(__tmp_6917 + 6.U),
          arrayRegFiles(__tmp_6917 + 5.U),
          arrayRegFiles(__tmp_6917 + 4.U),
          arrayRegFiles(__tmp_6917 + 3.U),
          arrayRegFiles(__tmp_6917 + 2.U),
          arrayRegFiles(__tmp_6917 + 1.U),
          arrayRegFiles(__tmp_6917 + 0.U)
        ).asUInt

        val __tmp_6918 = ((SP + 20.U(16.W))).asUInt
        generalRegFiles(9.U) := Cat(
          arrayRegFiles(__tmp_6918 + 7.U),
          arrayRegFiles(__tmp_6918 + 6.U),
          arrayRegFiles(__tmp_6918 + 5.U),
          arrayRegFiles(__tmp_6918 + 4.U),
          arrayRegFiles(__tmp_6918 + 3.U),
          arrayRegFiles(__tmp_6918 + 2.U),
          arrayRegFiles(__tmp_6918 + 1.U),
          arrayRegFiles(__tmp_6918 + 0.U)
        ).asUInt

        CP := 73.U
      }

      is(73.U) {
        /*
        SP = SP + 60
        goto .74
        */


        SP := SP + 60.U

        CP := 74.U
      }

      is(74.U) {
        /*
        decl $ret: CP [@0, 2], $res: SP [@2, 2], n: U64 [@4, 8], m: U64 [@12, 8]
        *SP = (75: CP) [unsigned, CP, 2]  // $ret@0 = 1725
        *(SP + (2: SP)) = (SP - (8: SP)) [unsigned, SP, 2]  // $res@2 = -8
        *(SP + (4: SP)) = $64U.7 [unsigned, U64, 8]  // n = $64U.7
        *(SP + (12: SP)) = $64U.9 [unsigned, U64, 8]  // m = $64U.9
        goto .428
        */


        val __tmp_6919 = SP
        val __tmp_6920 = (75.U(16.W)).asUInt
        arrayRegFiles(__tmp_6919 + 0.U) := __tmp_6920(7, 0)
        arrayRegFiles(__tmp_6919 + 1.U) := __tmp_6920(15, 8)

        val __tmp_6921 = (SP + 2.U(16.W))
        val __tmp_6922 = ((SP - 8.U(16.W))).asUInt
        arrayRegFiles(__tmp_6921 + 0.U) := __tmp_6922(7, 0)
        arrayRegFiles(__tmp_6921 + 1.U) := __tmp_6922(15, 8)

        val __tmp_6923 = (SP + 4.U(16.W))
        val __tmp_6924 = (generalRegFiles(7.U)).asUInt
        arrayRegFiles(__tmp_6923 + 0.U) := __tmp_6924(7, 0)
        arrayRegFiles(__tmp_6923 + 1.U) := __tmp_6924(15, 8)
        arrayRegFiles(__tmp_6923 + 2.U) := __tmp_6924(23, 16)
        arrayRegFiles(__tmp_6923 + 3.U) := __tmp_6924(31, 24)
        arrayRegFiles(__tmp_6923 + 4.U) := __tmp_6924(39, 32)
        arrayRegFiles(__tmp_6923 + 5.U) := __tmp_6924(47, 40)
        arrayRegFiles(__tmp_6923 + 6.U) := __tmp_6924(55, 48)
        arrayRegFiles(__tmp_6923 + 7.U) := __tmp_6924(63, 56)

        val __tmp_6925 = (SP + 12.U(16.W))
        val __tmp_6926 = (generalRegFiles(9.U)).asUInt
        arrayRegFiles(__tmp_6925 + 0.U) := __tmp_6926(7, 0)
        arrayRegFiles(__tmp_6925 + 1.U) := __tmp_6926(15, 8)
        arrayRegFiles(__tmp_6925 + 2.U) := __tmp_6926(23, 16)
        arrayRegFiles(__tmp_6925 + 3.U) := __tmp_6926(31, 24)
        arrayRegFiles(__tmp_6925 + 4.U) := __tmp_6926(39, 32)
        arrayRegFiles(__tmp_6925 + 5.U) := __tmp_6926(47, 40)
        arrayRegFiles(__tmp_6925 + 6.U) := __tmp_6926(55, 48)
        arrayRegFiles(__tmp_6925 + 7.U) := __tmp_6926(63, 56)

        CP := 428.U
      }

      is(75.U) {
        /*
        $64U.10 = **(SP + (2: SP)) [unsigned, U64, 8]  // $10 = $res
        undecl m: U64 [@12, 8], n: U64 [@4, 8], $res: SP [@2, 2], $ret: CP [@0, 2]
        goto .76
        */


        val __tmp_6927 = (Cat(
          arrayRegFiles((SP + 2.U(16.W)) + 1.U),
          arrayRegFiles((SP + 2.U(16.W)) + 0.U)
        )).asUInt
        generalRegFiles(10.U) := Cat(
          arrayRegFiles(__tmp_6927 + 7.U),
          arrayRegFiles(__tmp_6927 + 6.U),
          arrayRegFiles(__tmp_6927 + 5.U),
          arrayRegFiles(__tmp_6927 + 4.U),
          arrayRegFiles(__tmp_6927 + 3.U),
          arrayRegFiles(__tmp_6927 + 2.U),
          arrayRegFiles(__tmp_6927 + 1.U),
          arrayRegFiles(__tmp_6927 + 0.U)
        ).asUInt

        CP := 76.U
      }

      is(76.U) {
        /*
        SP = SP - 60
        goto .77
        */


        SP := SP - 60.U

        CP := 77.U
      }

      is(77.U) {
        /*
        *(SP + (36: SP)) = $64U.10 [signed, U64, 8]  // i = $64U.10
        goto .78
        */


        val __tmp_6928 = (SP + 36.U(16.W))
        val __tmp_6929 = (generalRegFiles(10.U)).asUInt
        arrayRegFiles(__tmp_6928 + 0.U) := __tmp_6929(7, 0)
        arrayRegFiles(__tmp_6928 + 1.U) := __tmp_6929(15, 8)
        arrayRegFiles(__tmp_6928 + 2.U) := __tmp_6929(23, 16)
        arrayRegFiles(__tmp_6928 + 3.U) := __tmp_6929(31, 24)
        arrayRegFiles(__tmp_6928 + 4.U) := __tmp_6929(39, 32)
        arrayRegFiles(__tmp_6928 + 5.U) := __tmp_6929(47, 40)
        arrayRegFiles(__tmp_6928 + 6.U) := __tmp_6929(55, 48)
        arrayRegFiles(__tmp_6928 + 7.U) := __tmp_6929(63, 56)

        CP := 78.U
      }

      is(78.U) {
        /*
        unalloc shlU64$res@[10,9].6BE26F25: U64 [@52, 8]
        $64U.7 = *(SP + (36: SP)) [unsigned, U64, 8]  // $64U.7 = i
        undecl i: U64 [@36, 8]
        goto .79
        */


        val __tmp_6930 = ((SP + 36.U(16.W))).asUInt
        generalRegFiles(7.U) := Cat(
          arrayRegFiles(__tmp_6930 + 7.U),
          arrayRegFiles(__tmp_6930 + 6.U),
          arrayRegFiles(__tmp_6930 + 5.U),
          arrayRegFiles(__tmp_6930 + 4.U),
          arrayRegFiles(__tmp_6930 + 3.U),
          arrayRegFiles(__tmp_6930 + 2.U),
          arrayRegFiles(__tmp_6930 + 1.U),
          arrayRegFiles(__tmp_6930 + 0.U)
        ).asUInt

        CP := 79.U
      }

      is(79.U) {
        /*
        **(SP + (2: SP)) = $64U.7 [unsigned, U64, 8]  // $res = $64U.7
        goto $ret@0
        */


        val __tmp_6931 = Cat(
          arrayRegFiles((SP + 2.U(16.W)) + 1.U),
          arrayRegFiles((SP + 2.U(16.W)) + 0.U)
        )
        val __tmp_6932 = (generalRegFiles(7.U)).asUInt
        arrayRegFiles(__tmp_6931 + 0.U) := __tmp_6932(7, 0)
        arrayRegFiles(__tmp_6931 + 1.U) := __tmp_6932(15, 8)
        arrayRegFiles(__tmp_6931 + 2.U) := __tmp_6932(23, 16)
        arrayRegFiles(__tmp_6931 + 3.U) := __tmp_6932(31, 24)
        arrayRegFiles(__tmp_6931 + 4.U) := __tmp_6932(39, 32)
        arrayRegFiles(__tmp_6931 + 5.U) := __tmp_6932(47, 40)
        arrayRegFiles(__tmp_6931 + 6.U) := __tmp_6932(55, 48)
        arrayRegFiles(__tmp_6931 + 7.U) := __tmp_6932(63, 56)

        CP := Cat(
          arrayRegFiles(SP + 0.U + 1.U),
          arrayRegFiles(SP + 0.U + 0.U)
        )

      }

      is(80.U) {
        /*
        $16U.0 = *(SP + (4: SP)) [unsigned, MS[anvil.PrinterIndex.U, U8], 2]  // buffer
        $64U.1 = *(SP + (6: SP)) [unsigned, anvil.PrinterIndex.U, 8]  // index
        $64U.2 = *(SP + (14: SP)) [unsigned, anvil.PrinterIndex.U, 8]  // mask
        $64U.3 = *(SP + (22: SP)) [unsigned, U64, 8]  // n
        $64S.4 = *(SP + (30: SP)) [signed, Z, 8]  // digits
        goto .81
        */


        val __tmp_6933 = ((SP + 4.U(16.W))).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_6933 + 1.U),
          arrayRegFiles(__tmp_6933 + 0.U)
        ).asUInt

        val __tmp_6934 = ((SP + 6.U(16.W))).asUInt
        generalRegFiles(1.U) := Cat(
          arrayRegFiles(__tmp_6934 + 7.U),
          arrayRegFiles(__tmp_6934 + 6.U),
          arrayRegFiles(__tmp_6934 + 5.U),
          arrayRegFiles(__tmp_6934 + 4.U),
          arrayRegFiles(__tmp_6934 + 3.U),
          arrayRegFiles(__tmp_6934 + 2.U),
          arrayRegFiles(__tmp_6934 + 1.U),
          arrayRegFiles(__tmp_6934 + 0.U)
        ).asUInt

        val __tmp_6935 = ((SP + 14.U(16.W))).asUInt
        generalRegFiles(2.U) := Cat(
          arrayRegFiles(__tmp_6935 + 7.U),
          arrayRegFiles(__tmp_6935 + 6.U),
          arrayRegFiles(__tmp_6935 + 5.U),
          arrayRegFiles(__tmp_6935 + 4.U),
          arrayRegFiles(__tmp_6935 + 3.U),
          arrayRegFiles(__tmp_6935 + 2.U),
          arrayRegFiles(__tmp_6935 + 1.U),
          arrayRegFiles(__tmp_6935 + 0.U)
        ).asUInt

        val __tmp_6936 = ((SP + 22.U(16.W))).asUInt
        generalRegFiles(3.U) := Cat(
          arrayRegFiles(__tmp_6936 + 7.U),
          arrayRegFiles(__tmp_6936 + 6.U),
          arrayRegFiles(__tmp_6936 + 5.U),
          arrayRegFiles(__tmp_6936 + 4.U),
          arrayRegFiles(__tmp_6936 + 3.U),
          arrayRegFiles(__tmp_6936 + 2.U),
          arrayRegFiles(__tmp_6936 + 1.U),
          arrayRegFiles(__tmp_6936 + 0.U)
        ).asUInt

        val __tmp_6937 = ((SP + 30.U(16.W))).asUInt
        generalRegFiles(4.U) := Cat(
          arrayRegFiles(__tmp_6937 + 7.U),
          arrayRegFiles(__tmp_6937 + 6.U),
          arrayRegFiles(__tmp_6937 + 5.U),
          arrayRegFiles(__tmp_6937 + 4.U),
          arrayRegFiles(__tmp_6937 + 3.U),
          arrayRegFiles(__tmp_6937 + 2.U),
          arrayRegFiles(__tmp_6937 + 1.U),
          arrayRegFiles(__tmp_6937 + 0.U)
        ).asSInt.pad(GENERAL_REG_WIDTH).asUInt

        CP := 81.U
      }

      is(81.U) {
        /*
        decl buff: MS[anvil.PrinterIndex.I16, U8] [@38, 30]
        alloc $new@[244,16].8366D910: MS[anvil.PrinterIndex.I16, U8] [@68, 30]
        $16U.3 = (SP + (68: SP))
        *(SP + (68: SP)) = (1541243932: U32) [unsigned, U32, 4]  // sha3 type signature of MS[anvil.PrinterIndex.I16, U8]: 0x5BDD841C
        *(SP + (72: SP)) = (16: Z) [signed, Z, 8]  // size of MS[anvil.PrinterIndex.I16, U8]((0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8))
        goto .82
        */



        generalRegFiles(3.U) := (SP + 68.U(16.W))

        val __tmp_6938 = (SP + 68.U(16.W))
        val __tmp_6939 = (1541243932.U(32.W)).asUInt
        arrayRegFiles(__tmp_6938 + 0.U) := __tmp_6939(7, 0)
        arrayRegFiles(__tmp_6938 + 1.U) := __tmp_6939(15, 8)
        arrayRegFiles(__tmp_6938 + 2.U) := __tmp_6939(23, 16)
        arrayRegFiles(__tmp_6938 + 3.U) := __tmp_6939(31, 24)

        val __tmp_6940 = (SP + 72.U(16.W))
        val __tmp_6941 = (16.S(64.W)).asUInt
        arrayRegFiles(__tmp_6940 + 0.U) := __tmp_6941(7, 0)
        arrayRegFiles(__tmp_6940 + 1.U) := __tmp_6941(15, 8)
        arrayRegFiles(__tmp_6940 + 2.U) := __tmp_6941(23, 16)
        arrayRegFiles(__tmp_6940 + 3.U) := __tmp_6941(31, 24)
        arrayRegFiles(__tmp_6940 + 4.U) := __tmp_6941(39, 32)
        arrayRegFiles(__tmp_6940 + 5.U) := __tmp_6941(47, 40)
        arrayRegFiles(__tmp_6940 + 6.U) := __tmp_6941(55, 48)
        arrayRegFiles(__tmp_6940 + 7.U) := __tmp_6941(63, 56)

        CP := 82.U
      }

      is(82.U) {
        /*
        *(($16U.3 + (12: SP)) + ((0: anvil.PrinterIndex.I16) as SP)) = (0: U8) [unsigned, U8, 1]  // $16U.3((0: anvil.PrinterIndex.I16)) = (0: U8)
        *(($16U.3 + (12: SP)) + ((1: anvil.PrinterIndex.I16) as SP)) = (0: U8) [unsigned, U8, 1]  // $16U.3((1: anvil.PrinterIndex.I16)) = (0: U8)
        *(($16U.3 + (12: SP)) + ((2: anvil.PrinterIndex.I16) as SP)) = (0: U8) [unsigned, U8, 1]  // $16U.3((2: anvil.PrinterIndex.I16)) = (0: U8)
        *(($16U.3 + (12: SP)) + ((3: anvil.PrinterIndex.I16) as SP)) = (0: U8) [unsigned, U8, 1]  // $16U.3((3: anvil.PrinterIndex.I16)) = (0: U8)
        *(($16U.3 + (12: SP)) + ((4: anvil.PrinterIndex.I16) as SP)) = (0: U8) [unsigned, U8, 1]  // $16U.3((4: anvil.PrinterIndex.I16)) = (0: U8)
        *(($16U.3 + (12: SP)) + ((5: anvil.PrinterIndex.I16) as SP)) = (0: U8) [unsigned, U8, 1]  // $16U.3((5: anvil.PrinterIndex.I16)) = (0: U8)
        *(($16U.3 + (12: SP)) + ((6: anvil.PrinterIndex.I16) as SP)) = (0: U8) [unsigned, U8, 1]  // $16U.3((6: anvil.PrinterIndex.I16)) = (0: U8)
        *(($16U.3 + (12: SP)) + ((7: anvil.PrinterIndex.I16) as SP)) = (0: U8) [unsigned, U8, 1]  // $16U.3((7: anvil.PrinterIndex.I16)) = (0: U8)
        *(($16U.3 + (12: SP)) + ((8: anvil.PrinterIndex.I16) as SP)) = (0: U8) [unsigned, U8, 1]  // $16U.3((8: anvil.PrinterIndex.I16)) = (0: U8)
        *(($16U.3 + (12: SP)) + ((9: anvil.PrinterIndex.I16) as SP)) = (0: U8) [unsigned, U8, 1]  // $16U.3((9: anvil.PrinterIndex.I16)) = (0: U8)
        *(($16U.3 + (12: SP)) + ((10: anvil.PrinterIndex.I16) as SP)) = (0: U8) [unsigned, U8, 1]  // $16U.3((10: anvil.PrinterIndex.I16)) = (0: U8)
        *(($16U.3 + (12: SP)) + ((11: anvil.PrinterIndex.I16) as SP)) = (0: U8) [unsigned, U8, 1]  // $16U.3((11: anvil.PrinterIndex.I16)) = (0: U8)
        *(($16U.3 + (12: SP)) + ((12: anvil.PrinterIndex.I16) as SP)) = (0: U8) [unsigned, U8, 1]  // $16U.3((12: anvil.PrinterIndex.I16)) = (0: U8)
        *(($16U.3 + (12: SP)) + ((13: anvil.PrinterIndex.I16) as SP)) = (0: U8) [unsigned, U8, 1]  // $16U.3((13: anvil.PrinterIndex.I16)) = (0: U8)
        *(($16U.3 + (12: SP)) + ((14: anvil.PrinterIndex.I16) as SP)) = (0: U8) [unsigned, U8, 1]  // $16U.3((14: anvil.PrinterIndex.I16)) = (0: U8)
        *(($16U.3 + (12: SP)) + ((15: anvil.PrinterIndex.I16) as SP)) = (0: U8) [unsigned, U8, 1]  // $16U.3((15: anvil.PrinterIndex.I16)) = (0: U8)
        goto .83
        */


        val __tmp_6942 = ((generalRegFiles(3.U) + 12.U(16.W)) + 0.S(8.W).asUInt)
        val __tmp_6943 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_6942 + 0.U) := __tmp_6943(7, 0)

        val __tmp_6944 = ((generalRegFiles(3.U) + 12.U(16.W)) + 1.S(8.W).asUInt)
        val __tmp_6945 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_6944 + 0.U) := __tmp_6945(7, 0)

        val __tmp_6946 = ((generalRegFiles(3.U) + 12.U(16.W)) + 2.S(8.W).asUInt)
        val __tmp_6947 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_6946 + 0.U) := __tmp_6947(7, 0)

        val __tmp_6948 = ((generalRegFiles(3.U) + 12.U(16.W)) + 3.S(8.W).asUInt)
        val __tmp_6949 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_6948 + 0.U) := __tmp_6949(7, 0)

        val __tmp_6950 = ((generalRegFiles(3.U) + 12.U(16.W)) + 4.S(8.W).asUInt)
        val __tmp_6951 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_6950 + 0.U) := __tmp_6951(7, 0)

        val __tmp_6952 = ((generalRegFiles(3.U) + 12.U(16.W)) + 5.S(8.W).asUInt)
        val __tmp_6953 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_6952 + 0.U) := __tmp_6953(7, 0)

        val __tmp_6954 = ((generalRegFiles(3.U) + 12.U(16.W)) + 6.S(8.W).asUInt)
        val __tmp_6955 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_6954 + 0.U) := __tmp_6955(7, 0)

        val __tmp_6956 = ((generalRegFiles(3.U) + 12.U(16.W)) + 7.S(8.W).asUInt)
        val __tmp_6957 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_6956 + 0.U) := __tmp_6957(7, 0)

        val __tmp_6958 = ((generalRegFiles(3.U) + 12.U(16.W)) + 8.S(8.W).asUInt)
        val __tmp_6959 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_6958 + 0.U) := __tmp_6959(7, 0)

        val __tmp_6960 = ((generalRegFiles(3.U) + 12.U(16.W)) + 9.S(8.W).asUInt)
        val __tmp_6961 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_6960 + 0.U) := __tmp_6961(7, 0)

        val __tmp_6962 = ((generalRegFiles(3.U) + 12.U(16.W)) + 10.S(8.W).asUInt)
        val __tmp_6963 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_6962 + 0.U) := __tmp_6963(7, 0)

        val __tmp_6964 = ((generalRegFiles(3.U) + 12.U(16.W)) + 11.S(8.W).asUInt)
        val __tmp_6965 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_6964 + 0.U) := __tmp_6965(7, 0)

        val __tmp_6966 = ((generalRegFiles(3.U) + 12.U(16.W)) + 12.S(8.W).asUInt)
        val __tmp_6967 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_6966 + 0.U) := __tmp_6967(7, 0)

        val __tmp_6968 = ((generalRegFiles(3.U) + 12.U(16.W)) + 13.S(8.W).asUInt)
        val __tmp_6969 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_6968 + 0.U) := __tmp_6969(7, 0)

        val __tmp_6970 = ((generalRegFiles(3.U) + 12.U(16.W)) + 14.S(8.W).asUInt)
        val __tmp_6971 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_6970 + 0.U) := __tmp_6971(7, 0)

        val __tmp_6972 = ((generalRegFiles(3.U) + 12.U(16.W)) + 15.S(8.W).asUInt)
        val __tmp_6973 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_6972 + 0.U) := __tmp_6973(7, 0)

        CP := 83.U
      }

      is(83.U) {
        /*
        (SP + (38: SP)) [MS[anvil.PrinterIndex.I16, U8], 30]  <-  $16U.3 [MS[anvil.PrinterIndex.I16, U8], ((*($16U.3 + (4: SP)) as SP) + (12: SP))]  // buff = $16U.3
        goto .84
        */


        val __tmp_6974 = (SP + 38.U(16.W))
        val __tmp_6975 = generalRegFiles(3.U)
        val __tmp_6976 = (Cat(
           arrayRegFiles((generalRegFiles(3.U) + 4.U(16.W)) + 7.U),
           arrayRegFiles((generalRegFiles(3.U) + 4.U(16.W)) + 6.U),
           arrayRegFiles((generalRegFiles(3.U) + 4.U(16.W)) + 5.U),
           arrayRegFiles((generalRegFiles(3.U) + 4.U(16.W)) + 4.U),
           arrayRegFiles((generalRegFiles(3.U) + 4.U(16.W)) + 3.U),
           arrayRegFiles((generalRegFiles(3.U) + 4.U(16.W)) + 2.U),
           arrayRegFiles((generalRegFiles(3.U) + 4.U(16.W)) + 1.U),
           arrayRegFiles((generalRegFiles(3.U) + 4.U(16.W)) + 0.U)
         ).asSInt.asUInt + 12.U(16.W))

        when(Idx <= __tmp_6976) {
          arrayRegFiles(__tmp_6974 + Idx + 0.U) := arrayRegFiles(__tmp_6975 + Idx + 0.U)
          arrayRegFiles(__tmp_6974 + Idx + 1.U) := arrayRegFiles(__tmp_6975 + Idx + 1.U)
          arrayRegFiles(__tmp_6974 + Idx + 2.U) := arrayRegFiles(__tmp_6975 + Idx + 2.U)
          arrayRegFiles(__tmp_6974 + Idx + 3.U) := arrayRegFiles(__tmp_6975 + Idx + 3.U)
          arrayRegFiles(__tmp_6974 + Idx + 4.U) := arrayRegFiles(__tmp_6975 + Idx + 4.U)
          arrayRegFiles(__tmp_6974 + Idx + 5.U) := arrayRegFiles(__tmp_6975 + Idx + 5.U)
          arrayRegFiles(__tmp_6974 + Idx + 6.U) := arrayRegFiles(__tmp_6975 + Idx + 6.U)
          arrayRegFiles(__tmp_6974 + Idx + 7.U) := arrayRegFiles(__tmp_6975 + Idx + 7.U)
          Idx := Idx + 8.U
          LeftByteRounds := __tmp_6976 - Idx
        } .elsewhen(IdxLeftByteRounds < LeftByteRounds) {
          val __tmp_6977 = Idx - 8.U
          arrayRegFiles(__tmp_6974 + __tmp_6977 + IdxLeftByteRounds) := arrayRegFiles(__tmp_6975 + __tmp_6977 + IdxLeftByteRounds)
          IdxLeftByteRounds := IdxLeftByteRounds + 1.U
        } .otherwise {
          Idx := 0.U
          IdxLeftByteRounds := 0.U
          LeftByteRounds := 0.U
          CP := 84.U
        }


      }

      is(84.U) {
        /*
        unalloc $new@[244,16].8366D910: MS[anvil.PrinterIndex.I16, U8] [@68, 30]
        goto .85
        */


        CP := 85.U
      }

      is(85.U) {
        /*
        decl i: anvil.PrinterIndex.I16 [@68, 1]
        *(SP + (68: SP)) = (0: anvil.PrinterIndex.I16) [signed, anvil.PrinterIndex.I16, 1]  // i = (0: anvil.PrinterIndex.I16)
        goto .86
        */


        val __tmp_6978 = (SP + 68.U(16.W))
        val __tmp_6979 = (0.S(8.W)).asUInt
        arrayRegFiles(__tmp_6978 + 0.U) := __tmp_6979(7, 0)

        CP := 86.U
      }

      is(86.U) {
        /*
        decl m: U64 [@69, 8]
        $64U.5 = *(SP + (22: SP)) [unsigned, U64, 8]  // $64U.5 = n
        goto .87
        */


        val __tmp_6980 = ((SP + 22.U(16.W))).asUInt
        generalRegFiles(5.U) := Cat(
          arrayRegFiles(__tmp_6980 + 7.U),
          arrayRegFiles(__tmp_6980 + 6.U),
          arrayRegFiles(__tmp_6980 + 5.U),
          arrayRegFiles(__tmp_6980 + 4.U),
          arrayRegFiles(__tmp_6980 + 3.U),
          arrayRegFiles(__tmp_6980 + 2.U),
          arrayRegFiles(__tmp_6980 + 1.U),
          arrayRegFiles(__tmp_6980 + 0.U)
        ).asUInt

        CP := 87.U
      }

      is(87.U) {
        /*
        *(SP + (69: SP)) = $64U.5 [signed, U64, 8]  // m = $64U.5
        goto .88
        */


        val __tmp_6981 = (SP + 69.U(16.W))
        val __tmp_6982 = (generalRegFiles(5.U)).asUInt
        arrayRegFiles(__tmp_6981 + 0.U) := __tmp_6982(7, 0)
        arrayRegFiles(__tmp_6981 + 1.U) := __tmp_6982(15, 8)
        arrayRegFiles(__tmp_6981 + 2.U) := __tmp_6982(23, 16)
        arrayRegFiles(__tmp_6981 + 3.U) := __tmp_6982(31, 24)
        arrayRegFiles(__tmp_6981 + 4.U) := __tmp_6982(39, 32)
        arrayRegFiles(__tmp_6981 + 5.U) := __tmp_6982(47, 40)
        arrayRegFiles(__tmp_6981 + 6.U) := __tmp_6982(55, 48)
        arrayRegFiles(__tmp_6981 + 7.U) := __tmp_6982(63, 56)

        CP := 88.U
      }

      is(88.U) {
        /*
        decl d: Z [@77, 8]
        $64S.2 = *(SP + (30: SP)) [signed, Z, 8]  // $64S.2 = digits
        goto .89
        */


        val __tmp_6983 = ((SP + 30.U(16.W))).asUInt
        generalRegFiles(2.U) := Cat(
          arrayRegFiles(__tmp_6983 + 7.U),
          arrayRegFiles(__tmp_6983 + 6.U),
          arrayRegFiles(__tmp_6983 + 5.U),
          arrayRegFiles(__tmp_6983 + 4.U),
          arrayRegFiles(__tmp_6983 + 3.U),
          arrayRegFiles(__tmp_6983 + 2.U),
          arrayRegFiles(__tmp_6983 + 1.U),
          arrayRegFiles(__tmp_6983 + 0.U)
        ).asSInt.pad(GENERAL_REG_WIDTH).asUInt

        CP := 89.U
      }

      is(89.U) {
        /*
        *(SP + (77: SP)) = $64S.2 [signed, Z, 8]  // d = $64S.2
        goto .90
        */


        val __tmp_6984 = (SP + 77.U(16.W))
        val __tmp_6985 = (generalRegFiles(2.U).asSInt).asUInt
        arrayRegFiles(__tmp_6984 + 0.U) := __tmp_6985(7, 0)
        arrayRegFiles(__tmp_6984 + 1.U) := __tmp_6985(15, 8)
        arrayRegFiles(__tmp_6984 + 2.U) := __tmp_6985(23, 16)
        arrayRegFiles(__tmp_6984 + 3.U) := __tmp_6985(31, 24)
        arrayRegFiles(__tmp_6984 + 4.U) := __tmp_6985(39, 32)
        arrayRegFiles(__tmp_6984 + 5.U) := __tmp_6985(47, 40)
        arrayRegFiles(__tmp_6984 + 6.U) := __tmp_6985(55, 48)
        arrayRegFiles(__tmp_6984 + 7.U) := __tmp_6985(63, 56)

        CP := 90.U
      }

      is(90.U) {
        /*
        $64U.5 = *(SP + (69: SP)) [unsigned, U64, 8]  // $64U.5 = m
        goto .91
        */


        val __tmp_6986 = ((SP + 69.U(16.W))).asUInt
        generalRegFiles(5.U) := Cat(
          arrayRegFiles(__tmp_6986 + 7.U),
          arrayRegFiles(__tmp_6986 + 6.U),
          arrayRegFiles(__tmp_6986 + 5.U),
          arrayRegFiles(__tmp_6986 + 4.U),
          arrayRegFiles(__tmp_6986 + 3.U),
          arrayRegFiles(__tmp_6986 + 2.U),
          arrayRegFiles(__tmp_6986 + 1.U),
          arrayRegFiles(__tmp_6986 + 0.U)
        ).asUInt

        CP := 91.U
      }

      is(91.U) {
        /*
        $1U.0 = ($64U.5 > (0: U64))
        goto .92
        */



        generalRegFiles(0.U) := (generalRegFiles(5.U) > 0.U(64.W)).asUInt

        CP := 92.U
      }

      is(92.U) {
        /*
        $64S.4 = *(SP + (77: SP)) [signed, Z, 8]  // $64S.4 = d
        goto .93
        */


        val __tmp_6987 = ((SP + 77.U(16.W))).asUInt
        generalRegFiles(4.U) := Cat(
          arrayRegFiles(__tmp_6987 + 7.U),
          arrayRegFiles(__tmp_6987 + 6.U),
          arrayRegFiles(__tmp_6987 + 5.U),
          arrayRegFiles(__tmp_6987 + 4.U),
          arrayRegFiles(__tmp_6987 + 3.U),
          arrayRegFiles(__tmp_6987 + 2.U),
          arrayRegFiles(__tmp_6987 + 1.U),
          arrayRegFiles(__tmp_6987 + 0.U)
        ).asSInt.pad(GENERAL_REG_WIDTH).asUInt

        CP := 93.U
      }

      is(93.U) {
        /*
        $1U.1 = ($64S.4 > (0: Z))
        goto .94
        */



        generalRegFiles(1.U) := (generalRegFiles(4.U).asSInt > 0.S(64.W)).asUInt

        CP := 94.U
      }

      is(94.U) {
        /*
        $1U.2 = ($1U.0 & $1U.1)
        goto .95
        */



        generalRegFiles(2.U) := (generalRegFiles(0.U) & generalRegFiles(1.U))

        CP := 95.U
      }

      is(95.U) {
        /*
        if $1U.2 goto .96 else goto .140
        */


        CP := Mux((generalRegFiles(2.U).asUInt) === 1.U, 96.U, 140.U)
      }

      is(96.U) {
        /*
        $64U.5 = *(SP + (69: SP)) [unsigned, U64, 8]  // $64U.5 = m
        goto .97
        */


        val __tmp_6988 = ((SP + 69.U(16.W))).asUInt
        generalRegFiles(5.U) := Cat(
          arrayRegFiles(__tmp_6988 + 7.U),
          arrayRegFiles(__tmp_6988 + 6.U),
          arrayRegFiles(__tmp_6988 + 5.U),
          arrayRegFiles(__tmp_6988 + 4.U),
          arrayRegFiles(__tmp_6988 + 3.U),
          arrayRegFiles(__tmp_6988 + 2.U),
          arrayRegFiles(__tmp_6988 + 1.U),
          arrayRegFiles(__tmp_6988 + 0.U)
        ).asUInt

        CP := 97.U
      }

      is(97.U) {
        /*
        $64U.7 = ($64U.5 & (15: U64))
        goto .98
        */



        generalRegFiles(7.U) := (generalRegFiles(5.U) & 15.U(64.W))

        CP := 98.U
      }

      is(98.U) {
        /*
        switch ($64U.7)
          (0: U64): goto 99
          (1: U64): goto 101
          (2: U64): goto 103
          (3: U64): goto 105
          (4: U64): goto 107
          (5: U64): goto 109
          (6: U64): goto 111
          (7: U64): goto 113
          (8: U64): goto 115
          (9: U64): goto 117
          (10: U64): goto 119
          (11: U64): goto 121
          (12: U64): goto 123
          (13: U64): goto 125
          (14: U64): goto 127
          (15: U64): goto 129

        */


        val __tmp_6989 = generalRegFiles(7.U)

        switch(__tmp_6989) {

          is(0.U(64.W)) {
            CP := 99.U
          }


          is(1.U(64.W)) {
            CP := 101.U
          }


          is(2.U(64.W)) {
            CP := 103.U
          }


          is(3.U(64.W)) {
            CP := 105.U
          }


          is(4.U(64.W)) {
            CP := 107.U
          }


          is(5.U(64.W)) {
            CP := 109.U
          }


          is(6.U(64.W)) {
            CP := 111.U
          }


          is(7.U(64.W)) {
            CP := 113.U
          }


          is(8.U(64.W)) {
            CP := 115.U
          }


          is(9.U(64.W)) {
            CP := 117.U
          }


          is(10.U(64.W)) {
            CP := 119.U
          }


          is(11.U(64.W)) {
            CP := 121.U
          }


          is(12.U(64.W)) {
            CP := 123.U
          }


          is(13.U(64.W)) {
            CP := 125.U
          }


          is(14.U(64.W)) {
            CP := 127.U
          }


          is(15.U(64.W)) {
            CP := 129.U
          }

        }

      }

      is(99.U) {
        /*
        $16U.3 = (SP + (38: SP))
        $8S.3 = *(SP + (68: SP)) [signed, anvil.PrinterIndex.I16, 1]  // $8S.3 = i
        goto .100
        */



        generalRegFiles(3.U) := (SP + 38.U(16.W))

        val __tmp_6990 = ((SP + 68.U(16.W))).asUInt
        generalRegFiles(3.U) := Cat(
          arrayRegFiles(__tmp_6990 + 0.U)
        ).asSInt.pad(GENERAL_REG_WIDTH).asUInt

        CP := 100.U
      }

      is(100.U) {
        /*
        *(($16U.3 + (12: SP)) + ($8S.3 as SP)) = (48: U8) [unsigned, U8, 1]  // $16U.3($8S.3) = (48: U8)
        goto .131
        */


        val __tmp_6991 = ((generalRegFiles(3.U) + 12.U(16.W)) + generalRegFiles(3.U).asSInt.asUInt)
        val __tmp_6992 = (48.U(8.W)).asUInt
        arrayRegFiles(__tmp_6991 + 0.U) := __tmp_6992(7, 0)

        CP := 131.U
      }

      is(101.U) {
        /*
        $16U.3 = (SP + (38: SP))
        $8S.3 = *(SP + (68: SP)) [signed, anvil.PrinterIndex.I16, 1]  // $8S.3 = i
        goto .102
        */



        generalRegFiles(3.U) := (SP + 38.U(16.W))

        val __tmp_6993 = ((SP + 68.U(16.W))).asUInt
        generalRegFiles(3.U) := Cat(
          arrayRegFiles(__tmp_6993 + 0.U)
        ).asSInt.pad(GENERAL_REG_WIDTH).asUInt

        CP := 102.U
      }

      is(102.U) {
        /*
        *(($16U.3 + (12: SP)) + ($8S.3 as SP)) = (49: U8) [unsigned, U8, 1]  // $16U.3($8S.3) = (49: U8)
        goto .131
        */


        val __tmp_6994 = ((generalRegFiles(3.U) + 12.U(16.W)) + generalRegFiles(3.U).asSInt.asUInt)
        val __tmp_6995 = (49.U(8.W)).asUInt
        arrayRegFiles(__tmp_6994 + 0.U) := __tmp_6995(7, 0)

        CP := 131.U
      }

      is(103.U) {
        /*
        $16U.3 = (SP + (38: SP))
        $8S.3 = *(SP + (68: SP)) [signed, anvil.PrinterIndex.I16, 1]  // $8S.3 = i
        goto .104
        */



        generalRegFiles(3.U) := (SP + 38.U(16.W))

        val __tmp_6996 = ((SP + 68.U(16.W))).asUInt
        generalRegFiles(3.U) := Cat(
          arrayRegFiles(__tmp_6996 + 0.U)
        ).asSInt.pad(GENERAL_REG_WIDTH).asUInt

        CP := 104.U
      }

      is(104.U) {
        /*
        *(($16U.3 + (12: SP)) + ($8S.3 as SP)) = (50: U8) [unsigned, U8, 1]  // $16U.3($8S.3) = (50: U8)
        goto .131
        */


        val __tmp_6997 = ((generalRegFiles(3.U) + 12.U(16.W)) + generalRegFiles(3.U).asSInt.asUInt)
        val __tmp_6998 = (50.U(8.W)).asUInt
        arrayRegFiles(__tmp_6997 + 0.U) := __tmp_6998(7, 0)

        CP := 131.U
      }

      is(105.U) {
        /*
        $16U.3 = (SP + (38: SP))
        $8S.3 = *(SP + (68: SP)) [signed, anvil.PrinterIndex.I16, 1]  // $8S.3 = i
        goto .106
        */



        generalRegFiles(3.U) := (SP + 38.U(16.W))

        val __tmp_6999 = ((SP + 68.U(16.W))).asUInt
        generalRegFiles(3.U) := Cat(
          arrayRegFiles(__tmp_6999 + 0.U)
        ).asSInt.pad(GENERAL_REG_WIDTH).asUInt

        CP := 106.U
      }

      is(106.U) {
        /*
        *(($16U.3 + (12: SP)) + ($8S.3 as SP)) = (51: U8) [unsigned, U8, 1]  // $16U.3($8S.3) = (51: U8)
        goto .131
        */


        val __tmp_7000 = ((generalRegFiles(3.U) + 12.U(16.W)) + generalRegFiles(3.U).asSInt.asUInt)
        val __tmp_7001 = (51.U(8.W)).asUInt
        arrayRegFiles(__tmp_7000 + 0.U) := __tmp_7001(7, 0)

        CP := 131.U
      }

      is(107.U) {
        /*
        $16U.3 = (SP + (38: SP))
        $8S.3 = *(SP + (68: SP)) [signed, anvil.PrinterIndex.I16, 1]  // $8S.3 = i
        goto .108
        */



        generalRegFiles(3.U) := (SP + 38.U(16.W))

        val __tmp_7002 = ((SP + 68.U(16.W))).asUInt
        generalRegFiles(3.U) := Cat(
          arrayRegFiles(__tmp_7002 + 0.U)
        ).asSInt.pad(GENERAL_REG_WIDTH).asUInt

        CP := 108.U
      }

      is(108.U) {
        /*
        *(($16U.3 + (12: SP)) + ($8S.3 as SP)) = (52: U8) [unsigned, U8, 1]  // $16U.3($8S.3) = (52: U8)
        goto .131
        */


        val __tmp_7003 = ((generalRegFiles(3.U) + 12.U(16.W)) + generalRegFiles(3.U).asSInt.asUInt)
        val __tmp_7004 = (52.U(8.W)).asUInt
        arrayRegFiles(__tmp_7003 + 0.U) := __tmp_7004(7, 0)

        CP := 131.U
      }

      is(109.U) {
        /*
        $16U.3 = (SP + (38: SP))
        $8S.3 = *(SP + (68: SP)) [signed, anvil.PrinterIndex.I16, 1]  // $8S.3 = i
        goto .110
        */



        generalRegFiles(3.U) := (SP + 38.U(16.W))

        val __tmp_7005 = ((SP + 68.U(16.W))).asUInt
        generalRegFiles(3.U) := Cat(
          arrayRegFiles(__tmp_7005 + 0.U)
        ).asSInt.pad(GENERAL_REG_WIDTH).asUInt

        CP := 110.U
      }

      is(110.U) {
        /*
        *(($16U.3 + (12: SP)) + ($8S.3 as SP)) = (53: U8) [unsigned, U8, 1]  // $16U.3($8S.3) = (53: U8)
        goto .131
        */


        val __tmp_7006 = ((generalRegFiles(3.U) + 12.U(16.W)) + generalRegFiles(3.U).asSInt.asUInt)
        val __tmp_7007 = (53.U(8.W)).asUInt
        arrayRegFiles(__tmp_7006 + 0.U) := __tmp_7007(7, 0)

        CP := 131.U
      }

      is(111.U) {
        /*
        $16U.3 = (SP + (38: SP))
        $8S.3 = *(SP + (68: SP)) [signed, anvil.PrinterIndex.I16, 1]  // $8S.3 = i
        goto .112
        */



        generalRegFiles(3.U) := (SP + 38.U(16.W))

        val __tmp_7008 = ((SP + 68.U(16.W))).asUInt
        generalRegFiles(3.U) := Cat(
          arrayRegFiles(__tmp_7008 + 0.U)
        ).asSInt.pad(GENERAL_REG_WIDTH).asUInt

        CP := 112.U
      }

      is(112.U) {
        /*
        *(($16U.3 + (12: SP)) + ($8S.3 as SP)) = (54: U8) [unsigned, U8, 1]  // $16U.3($8S.3) = (54: U8)
        goto .131
        */


        val __tmp_7009 = ((generalRegFiles(3.U) + 12.U(16.W)) + generalRegFiles(3.U).asSInt.asUInt)
        val __tmp_7010 = (54.U(8.W)).asUInt
        arrayRegFiles(__tmp_7009 + 0.U) := __tmp_7010(7, 0)

        CP := 131.U
      }

      is(113.U) {
        /*
        $16U.3 = (SP + (38: SP))
        $8S.3 = *(SP + (68: SP)) [signed, anvil.PrinterIndex.I16, 1]  // $8S.3 = i
        goto .114
        */



        generalRegFiles(3.U) := (SP + 38.U(16.W))

        val __tmp_7011 = ((SP + 68.U(16.W))).asUInt
        generalRegFiles(3.U) := Cat(
          arrayRegFiles(__tmp_7011 + 0.U)
        ).asSInt.pad(GENERAL_REG_WIDTH).asUInt

        CP := 114.U
      }

      is(114.U) {
        /*
        *(($16U.3 + (12: SP)) + ($8S.3 as SP)) = (55: U8) [unsigned, U8, 1]  // $16U.3($8S.3) = (55: U8)
        goto .131
        */


        val __tmp_7012 = ((generalRegFiles(3.U) + 12.U(16.W)) + generalRegFiles(3.U).asSInt.asUInt)
        val __tmp_7013 = (55.U(8.W)).asUInt
        arrayRegFiles(__tmp_7012 + 0.U) := __tmp_7013(7, 0)

        CP := 131.U
      }

      is(115.U) {
        /*
        $16U.3 = (SP + (38: SP))
        $8S.3 = *(SP + (68: SP)) [signed, anvil.PrinterIndex.I16, 1]  // $8S.3 = i
        goto .116
        */



        generalRegFiles(3.U) := (SP + 38.U(16.W))

        val __tmp_7014 = ((SP + 68.U(16.W))).asUInt
        generalRegFiles(3.U) := Cat(
          arrayRegFiles(__tmp_7014 + 0.U)
        ).asSInt.pad(GENERAL_REG_WIDTH).asUInt

        CP := 116.U
      }

      is(116.U) {
        /*
        *(($16U.3 + (12: SP)) + ($8S.3 as SP)) = (56: U8) [unsigned, U8, 1]  // $16U.3($8S.3) = (56: U8)
        goto .131
        */


        val __tmp_7015 = ((generalRegFiles(3.U) + 12.U(16.W)) + generalRegFiles(3.U).asSInt.asUInt)
        val __tmp_7016 = (56.U(8.W)).asUInt
        arrayRegFiles(__tmp_7015 + 0.U) := __tmp_7016(7, 0)

        CP := 131.U
      }

      is(117.U) {
        /*
        $16U.3 = (SP + (38: SP))
        $8S.3 = *(SP + (68: SP)) [signed, anvil.PrinterIndex.I16, 1]  // $8S.3 = i
        goto .118
        */



        generalRegFiles(3.U) := (SP + 38.U(16.W))

        val __tmp_7017 = ((SP + 68.U(16.W))).asUInt
        generalRegFiles(3.U) := Cat(
          arrayRegFiles(__tmp_7017 + 0.U)
        ).asSInt.pad(GENERAL_REG_WIDTH).asUInt

        CP := 118.U
      }

      is(118.U) {
        /*
        *(($16U.3 + (12: SP)) + ($8S.3 as SP)) = (57: U8) [unsigned, U8, 1]  // $16U.3($8S.3) = (57: U8)
        goto .131
        */


        val __tmp_7018 = ((generalRegFiles(3.U) + 12.U(16.W)) + generalRegFiles(3.U).asSInt.asUInt)
        val __tmp_7019 = (57.U(8.W)).asUInt
        arrayRegFiles(__tmp_7018 + 0.U) := __tmp_7019(7, 0)

        CP := 131.U
      }

      is(119.U) {
        /*
        $16U.3 = (SP + (38: SP))
        $8S.3 = *(SP + (68: SP)) [signed, anvil.PrinterIndex.I16, 1]  // $8S.3 = i
        goto .120
        */



        generalRegFiles(3.U) := (SP + 38.U(16.W))

        val __tmp_7020 = ((SP + 68.U(16.W))).asUInt
        generalRegFiles(3.U) := Cat(
          arrayRegFiles(__tmp_7020 + 0.U)
        ).asSInt.pad(GENERAL_REG_WIDTH).asUInt

        CP := 120.U
      }

      is(120.U) {
        /*
        *(($16U.3 + (12: SP)) + ($8S.3 as SP)) = (65: U8) [unsigned, U8, 1]  // $16U.3($8S.3) = (65: U8)
        goto .131
        */


        val __tmp_7021 = ((generalRegFiles(3.U) + 12.U(16.W)) + generalRegFiles(3.U).asSInt.asUInt)
        val __tmp_7022 = (65.U(8.W)).asUInt
        arrayRegFiles(__tmp_7021 + 0.U) := __tmp_7022(7, 0)

        CP := 131.U
      }

      is(121.U) {
        /*
        $16U.3 = (SP + (38: SP))
        $8S.3 = *(SP + (68: SP)) [signed, anvil.PrinterIndex.I16, 1]  // $8S.3 = i
        goto .122
        */



        generalRegFiles(3.U) := (SP + 38.U(16.W))

        val __tmp_7023 = ((SP + 68.U(16.W))).asUInt
        generalRegFiles(3.U) := Cat(
          arrayRegFiles(__tmp_7023 + 0.U)
        ).asSInt.pad(GENERAL_REG_WIDTH).asUInt

        CP := 122.U
      }

      is(122.U) {
        /*
        *(($16U.3 + (12: SP)) + ($8S.3 as SP)) = (66: U8) [unsigned, U8, 1]  // $16U.3($8S.3) = (66: U8)
        goto .131
        */


        val __tmp_7024 = ((generalRegFiles(3.U) + 12.U(16.W)) + generalRegFiles(3.U).asSInt.asUInt)
        val __tmp_7025 = (66.U(8.W)).asUInt
        arrayRegFiles(__tmp_7024 + 0.U) := __tmp_7025(7, 0)

        CP := 131.U
      }

      is(123.U) {
        /*
        $16U.3 = (SP + (38: SP))
        $8S.3 = *(SP + (68: SP)) [signed, anvil.PrinterIndex.I16, 1]  // $8S.3 = i
        goto .124
        */



        generalRegFiles(3.U) := (SP + 38.U(16.W))

        val __tmp_7026 = ((SP + 68.U(16.W))).asUInt
        generalRegFiles(3.U) := Cat(
          arrayRegFiles(__tmp_7026 + 0.U)
        ).asSInt.pad(GENERAL_REG_WIDTH).asUInt

        CP := 124.U
      }

      is(124.U) {
        /*
        *(($16U.3 + (12: SP)) + ($8S.3 as SP)) = (67: U8) [unsigned, U8, 1]  // $16U.3($8S.3) = (67: U8)
        goto .131
        */


        val __tmp_7027 = ((generalRegFiles(3.U) + 12.U(16.W)) + generalRegFiles(3.U).asSInt.asUInt)
        val __tmp_7028 = (67.U(8.W)).asUInt
        arrayRegFiles(__tmp_7027 + 0.U) := __tmp_7028(7, 0)

        CP := 131.U
      }

      is(125.U) {
        /*
        $16U.3 = (SP + (38: SP))
        $8S.3 = *(SP + (68: SP)) [signed, anvil.PrinterIndex.I16, 1]  // $8S.3 = i
        goto .126
        */



        generalRegFiles(3.U) := (SP + 38.U(16.W))

        val __tmp_7029 = ((SP + 68.U(16.W))).asUInt
        generalRegFiles(3.U) := Cat(
          arrayRegFiles(__tmp_7029 + 0.U)
        ).asSInt.pad(GENERAL_REG_WIDTH).asUInt

        CP := 126.U
      }

      is(126.U) {
        /*
        *(($16U.3 + (12: SP)) + ($8S.3 as SP)) = (68: U8) [unsigned, U8, 1]  // $16U.3($8S.3) = (68: U8)
        goto .131
        */


        val __tmp_7030 = ((generalRegFiles(3.U) + 12.U(16.W)) + generalRegFiles(3.U).asSInt.asUInt)
        val __tmp_7031 = (68.U(8.W)).asUInt
        arrayRegFiles(__tmp_7030 + 0.U) := __tmp_7031(7, 0)

        CP := 131.U
      }

      is(127.U) {
        /*
        $16U.3 = (SP + (38: SP))
        $8S.3 = *(SP + (68: SP)) [signed, anvil.PrinterIndex.I16, 1]  // $8S.3 = i
        goto .128
        */



        generalRegFiles(3.U) := (SP + 38.U(16.W))

        val __tmp_7032 = ((SP + 68.U(16.W))).asUInt
        generalRegFiles(3.U) := Cat(
          arrayRegFiles(__tmp_7032 + 0.U)
        ).asSInt.pad(GENERAL_REG_WIDTH).asUInt

        CP := 128.U
      }

      is(128.U) {
        /*
        *(($16U.3 + (12: SP)) + ($8S.3 as SP)) = (69: U8) [unsigned, U8, 1]  // $16U.3($8S.3) = (69: U8)
        goto .131
        */


        val __tmp_7033 = ((generalRegFiles(3.U) + 12.U(16.W)) + generalRegFiles(3.U).asSInt.asUInt)
        val __tmp_7034 = (69.U(8.W)).asUInt
        arrayRegFiles(__tmp_7033 + 0.U) := __tmp_7034(7, 0)

        CP := 131.U
      }

      is(129.U) {
        /*
        $16U.3 = (SP + (38: SP))
        $8S.3 = *(SP + (68: SP)) [signed, anvil.PrinterIndex.I16, 1]  // $8S.3 = i
        goto .130
        */



        generalRegFiles(3.U) := (SP + 38.U(16.W))

        val __tmp_7035 = ((SP + 68.U(16.W))).asUInt
        generalRegFiles(3.U) := Cat(
          arrayRegFiles(__tmp_7035 + 0.U)
        ).asSInt.pad(GENERAL_REG_WIDTH).asUInt

        CP := 130.U
      }

      is(130.U) {
        /*
        *(($16U.3 + (12: SP)) + ($8S.3 as SP)) = (70: U8) [unsigned, U8, 1]  // $16U.3($8S.3) = (70: U8)
        goto .131
        */


        val __tmp_7036 = ((generalRegFiles(3.U) + 12.U(16.W)) + generalRegFiles(3.U).asSInt.asUInt)
        val __tmp_7037 = (70.U(8.W)).asUInt
        arrayRegFiles(__tmp_7036 + 0.U) := __tmp_7037(7, 0)

        CP := 131.U
      }

      is(131.U) {
        /*
        $64U.5 = *(SP + (69: SP)) [unsigned, U64, 8]  // $64U.5 = m
        goto .132
        */


        val __tmp_7038 = ((SP + 69.U(16.W))).asUInt
        generalRegFiles(5.U) := Cat(
          arrayRegFiles(__tmp_7038 + 7.U),
          arrayRegFiles(__tmp_7038 + 6.U),
          arrayRegFiles(__tmp_7038 + 5.U),
          arrayRegFiles(__tmp_7038 + 4.U),
          arrayRegFiles(__tmp_7038 + 3.U),
          arrayRegFiles(__tmp_7038 + 2.U),
          arrayRegFiles(__tmp_7038 + 1.U),
          arrayRegFiles(__tmp_7038 + 0.U)
        ).asUInt

        CP := 132.U
      }

      is(132.U) {
        /*
        $64U.7 = ($64U.5 >>> (4: U64))
        goto .133
        */



        generalRegFiles(7.U) := (((generalRegFiles(5.U)) >> 4.U(64.W)(4,0)))

        CP := 133.U
      }

      is(133.U) {
        /*
        *(SP + (69: SP)) = $64U.7 [signed, U64, 8]  // m = $64U.7
        goto .134
        */


        val __tmp_7039 = (SP + 69.U(16.W))
        val __tmp_7040 = (generalRegFiles(7.U)).asUInt
        arrayRegFiles(__tmp_7039 + 0.U) := __tmp_7040(7, 0)
        arrayRegFiles(__tmp_7039 + 1.U) := __tmp_7040(15, 8)
        arrayRegFiles(__tmp_7039 + 2.U) := __tmp_7040(23, 16)
        arrayRegFiles(__tmp_7039 + 3.U) := __tmp_7040(31, 24)
        arrayRegFiles(__tmp_7039 + 4.U) := __tmp_7040(39, 32)
        arrayRegFiles(__tmp_7039 + 5.U) := __tmp_7040(47, 40)
        arrayRegFiles(__tmp_7039 + 6.U) := __tmp_7040(55, 48)
        arrayRegFiles(__tmp_7039 + 7.U) := __tmp_7040(63, 56)

        CP := 134.U
      }

      is(134.U) {
        /*
        $8S.2 = *(SP + (68: SP)) [signed, anvil.PrinterIndex.I16, 1]  // $8S.2 = i
        goto .135
        */


        val __tmp_7041 = ((SP + 68.U(16.W))).asUInt
        generalRegFiles(2.U) := Cat(
          arrayRegFiles(__tmp_7041 + 0.U)
        ).asSInt.pad(GENERAL_REG_WIDTH).asUInt

        CP := 135.U
      }

      is(135.U) {
        /*
        $8S.3 = ($8S.2 + (1: anvil.PrinterIndex.I16))
        goto .136
        */



        generalRegFiles(3.U) := ((generalRegFiles(2.U).asSInt + 1.S(8.W))).asUInt

        CP := 136.U
      }

      is(136.U) {
        /*
        *(SP + (68: SP)) = $8S.3 [signed, anvil.PrinterIndex.I16, 1]  // i = $8S.3
        goto .137
        */


        val __tmp_7042 = (SP + 68.U(16.W))
        val __tmp_7043 = (generalRegFiles(3.U).asSInt).asUInt
        arrayRegFiles(__tmp_7042 + 0.U) := __tmp_7043(7, 0)

        CP := 137.U
      }

      is(137.U) {
        /*
        $64S.2 = *(SP + (77: SP)) [signed, Z, 8]  // $64S.2 = d
        goto .138
        */


        val __tmp_7044 = ((SP + 77.U(16.W))).asUInt
        generalRegFiles(2.U) := Cat(
          arrayRegFiles(__tmp_7044 + 7.U),
          arrayRegFiles(__tmp_7044 + 6.U),
          arrayRegFiles(__tmp_7044 + 5.U),
          arrayRegFiles(__tmp_7044 + 4.U),
          arrayRegFiles(__tmp_7044 + 3.U),
          arrayRegFiles(__tmp_7044 + 2.U),
          arrayRegFiles(__tmp_7044 + 1.U),
          arrayRegFiles(__tmp_7044 + 0.U)
        ).asSInt.pad(GENERAL_REG_WIDTH).asUInt

        CP := 138.U
      }

      is(138.U) {
        /*
        $64S.3 = ($64S.2 - (1: Z))
        goto .139
        */



        generalRegFiles(3.U) := ((generalRegFiles(2.U).asSInt - 1.S(64.W))).asUInt

        CP := 139.U
      }

      is(139.U) {
        /*
        *(SP + (77: SP)) = $64S.3 [signed, Z, 8]  // d = $64S.3
        goto .90
        */


        val __tmp_7045 = (SP + 77.U(16.W))
        val __tmp_7046 = (generalRegFiles(3.U).asSInt).asUInt
        arrayRegFiles(__tmp_7045 + 0.U) := __tmp_7046(7, 0)
        arrayRegFiles(__tmp_7045 + 1.U) := __tmp_7046(15, 8)
        arrayRegFiles(__tmp_7045 + 2.U) := __tmp_7046(23, 16)
        arrayRegFiles(__tmp_7045 + 3.U) := __tmp_7046(31, 24)
        arrayRegFiles(__tmp_7045 + 4.U) := __tmp_7046(39, 32)
        arrayRegFiles(__tmp_7045 + 5.U) := __tmp_7046(47, 40)
        arrayRegFiles(__tmp_7045 + 6.U) := __tmp_7046(55, 48)
        arrayRegFiles(__tmp_7045 + 7.U) := __tmp_7046(63, 56)

        CP := 90.U
      }

      is(140.U) {
        /*
        decl idx: anvil.PrinterIndex.U [@85, 8]
        $64U.6 = *(SP + (6: SP)) [unsigned, anvil.PrinterIndex.U, 8]  // $64U.6 = index
        goto .141
        */


        val __tmp_7047 = ((SP + 6.U(16.W))).asUInt
        generalRegFiles(6.U) := Cat(
          arrayRegFiles(__tmp_7047 + 7.U),
          arrayRegFiles(__tmp_7047 + 6.U),
          arrayRegFiles(__tmp_7047 + 5.U),
          arrayRegFiles(__tmp_7047 + 4.U),
          arrayRegFiles(__tmp_7047 + 3.U),
          arrayRegFiles(__tmp_7047 + 2.U),
          arrayRegFiles(__tmp_7047 + 1.U),
          arrayRegFiles(__tmp_7047 + 0.U)
        ).asUInt

        CP := 141.U
      }

      is(141.U) {
        /*
        *(SP + (85: SP)) = $64U.6 [signed, anvil.PrinterIndex.U, 8]  // idx = $64U.6
        goto .142
        */


        val __tmp_7048 = (SP + 85.U(16.W))
        val __tmp_7049 = (generalRegFiles(6.U)).asUInt
        arrayRegFiles(__tmp_7048 + 0.U) := __tmp_7049(7, 0)
        arrayRegFiles(__tmp_7048 + 1.U) := __tmp_7049(15, 8)
        arrayRegFiles(__tmp_7048 + 2.U) := __tmp_7049(23, 16)
        arrayRegFiles(__tmp_7048 + 3.U) := __tmp_7049(31, 24)
        arrayRegFiles(__tmp_7048 + 4.U) := __tmp_7049(39, 32)
        arrayRegFiles(__tmp_7048 + 5.U) := __tmp_7049(47, 40)
        arrayRegFiles(__tmp_7048 + 6.U) := __tmp_7049(55, 48)
        arrayRegFiles(__tmp_7048 + 7.U) := __tmp_7049(63, 56)

        CP := 142.U
      }

      is(142.U) {
        /*
        $64S.2 = *(SP + (77: SP)) [signed, Z, 8]  // $64S.2 = d
        goto .143
        */


        val __tmp_7050 = ((SP + 77.U(16.W))).asUInt
        generalRegFiles(2.U) := Cat(
          arrayRegFiles(__tmp_7050 + 7.U),
          arrayRegFiles(__tmp_7050 + 6.U),
          arrayRegFiles(__tmp_7050 + 5.U),
          arrayRegFiles(__tmp_7050 + 4.U),
          arrayRegFiles(__tmp_7050 + 3.U),
          arrayRegFiles(__tmp_7050 + 2.U),
          arrayRegFiles(__tmp_7050 + 1.U),
          arrayRegFiles(__tmp_7050 + 0.U)
        ).asSInt.pad(GENERAL_REG_WIDTH).asUInt

        CP := 143.U
      }

      is(143.U) {
        /*
        $1U.0 = ($64S.2 > (0: Z))
        goto .144
        */



        generalRegFiles(0.U) := (generalRegFiles(2.U).asSInt > 0.S(64.W)).asUInt

        CP := 144.U
      }

      is(144.U) {
        /*
        if $1U.0 goto .145 else goto .154
        */


        CP := Mux((generalRegFiles(0.U).asUInt) === 1.U, 145.U, 154.U)
      }

      is(145.U) {
        /*
        $16U.4 = *(SP + (4: SP)) [unsigned, SP, 2]  // $16U.4 = buffer
        $64U.8 = *(SP + (85: SP)) [unsigned, anvil.PrinterIndex.U, 8]  // $64U.8 = idx
        $64U.9 = *(SP + (14: SP)) [unsigned, anvil.PrinterIndex.U, 8]  // $64U.9 = mask
        goto .146
        */


        val __tmp_7051 = ((SP + 4.U(16.W))).asUInt
        generalRegFiles(4.U) := Cat(
          arrayRegFiles(__tmp_7051 + 1.U),
          arrayRegFiles(__tmp_7051 + 0.U)
        ).asUInt

        val __tmp_7052 = ((SP + 85.U(16.W))).asUInt
        generalRegFiles(8.U) := Cat(
          arrayRegFiles(__tmp_7052 + 7.U),
          arrayRegFiles(__tmp_7052 + 6.U),
          arrayRegFiles(__tmp_7052 + 5.U),
          arrayRegFiles(__tmp_7052 + 4.U),
          arrayRegFiles(__tmp_7052 + 3.U),
          arrayRegFiles(__tmp_7052 + 2.U),
          arrayRegFiles(__tmp_7052 + 1.U),
          arrayRegFiles(__tmp_7052 + 0.U)
        ).asUInt

        val __tmp_7053 = ((SP + 14.U(16.W))).asUInt
        generalRegFiles(9.U) := Cat(
          arrayRegFiles(__tmp_7053 + 7.U),
          arrayRegFiles(__tmp_7053 + 6.U),
          arrayRegFiles(__tmp_7053 + 5.U),
          arrayRegFiles(__tmp_7053 + 4.U),
          arrayRegFiles(__tmp_7053 + 3.U),
          arrayRegFiles(__tmp_7053 + 2.U),
          arrayRegFiles(__tmp_7053 + 1.U),
          arrayRegFiles(__tmp_7053 + 0.U)
        ).asUInt

        CP := 146.U
      }

      is(146.U) {
        /*
        $64U.10 = ($64U.8 & $64U.9)
        goto .147
        */



        generalRegFiles(10.U) := (generalRegFiles(8.U) & generalRegFiles(9.U))

        CP := 147.U
      }

      is(147.U) {
        /*
        *(($16U.4 + (12: SP)) + ($64U.10 as SP)) = (48: U8) [unsigned, U8, 1]  // $16U.4($64U.10) = (48: U8)
        goto .148
        */


        val __tmp_7054 = ((generalRegFiles(4.U) + 12.U(16.W)) + generalRegFiles(10.U).asUInt)
        val __tmp_7055 = (48.U(8.W)).asUInt
        arrayRegFiles(__tmp_7054 + 0.U) := __tmp_7055(7, 0)

        CP := 148.U
      }

      is(148.U) {
        /*
        $64S.2 = *(SP + (77: SP)) [signed, Z, 8]  // $64S.2 = d
        goto .149
        */


        val __tmp_7056 = ((SP + 77.U(16.W))).asUInt
        generalRegFiles(2.U) := Cat(
          arrayRegFiles(__tmp_7056 + 7.U),
          arrayRegFiles(__tmp_7056 + 6.U),
          arrayRegFiles(__tmp_7056 + 5.U),
          arrayRegFiles(__tmp_7056 + 4.U),
          arrayRegFiles(__tmp_7056 + 3.U),
          arrayRegFiles(__tmp_7056 + 2.U),
          arrayRegFiles(__tmp_7056 + 1.U),
          arrayRegFiles(__tmp_7056 + 0.U)
        ).asSInt.pad(GENERAL_REG_WIDTH).asUInt

        CP := 149.U
      }

      is(149.U) {
        /*
        $64S.3 = ($64S.2 - (1: Z))
        goto .150
        */



        generalRegFiles(3.U) := ((generalRegFiles(2.U).asSInt - 1.S(64.W))).asUInt

        CP := 150.U
      }

      is(150.U) {
        /*
        *(SP + (77: SP)) = $64S.3 [signed, Z, 8]  // d = $64S.3
        goto .151
        */


        val __tmp_7057 = (SP + 77.U(16.W))
        val __tmp_7058 = (generalRegFiles(3.U).asSInt).asUInt
        arrayRegFiles(__tmp_7057 + 0.U) := __tmp_7058(7, 0)
        arrayRegFiles(__tmp_7057 + 1.U) := __tmp_7058(15, 8)
        arrayRegFiles(__tmp_7057 + 2.U) := __tmp_7058(23, 16)
        arrayRegFiles(__tmp_7057 + 3.U) := __tmp_7058(31, 24)
        arrayRegFiles(__tmp_7057 + 4.U) := __tmp_7058(39, 32)
        arrayRegFiles(__tmp_7057 + 5.U) := __tmp_7058(47, 40)
        arrayRegFiles(__tmp_7057 + 6.U) := __tmp_7058(55, 48)
        arrayRegFiles(__tmp_7057 + 7.U) := __tmp_7058(63, 56)

        CP := 151.U
      }

      is(151.U) {
        /*
        $64U.6 = *(SP + (85: SP)) [unsigned, anvil.PrinterIndex.U, 8]  // $64U.6 = idx
        goto .152
        */


        val __tmp_7059 = ((SP + 85.U(16.W))).asUInt
        generalRegFiles(6.U) := Cat(
          arrayRegFiles(__tmp_7059 + 7.U),
          arrayRegFiles(__tmp_7059 + 6.U),
          arrayRegFiles(__tmp_7059 + 5.U),
          arrayRegFiles(__tmp_7059 + 4.U),
          arrayRegFiles(__tmp_7059 + 3.U),
          arrayRegFiles(__tmp_7059 + 2.U),
          arrayRegFiles(__tmp_7059 + 1.U),
          arrayRegFiles(__tmp_7059 + 0.U)
        ).asUInt

        CP := 152.U
      }

      is(152.U) {
        /*
        $64U.8 = ($64U.6 + (1: anvil.PrinterIndex.U))
        goto .153
        */



        generalRegFiles(8.U) := (generalRegFiles(6.U) + 1.U(64.W))

        CP := 153.U
      }

      is(153.U) {
        /*
        *(SP + (85: SP)) = $64U.8 [signed, anvil.PrinterIndex.U, 8]  // idx = $64U.8
        goto .142
        */


        val __tmp_7060 = (SP + 85.U(16.W))
        val __tmp_7061 = (generalRegFiles(8.U)).asUInt
        arrayRegFiles(__tmp_7060 + 0.U) := __tmp_7061(7, 0)
        arrayRegFiles(__tmp_7060 + 1.U) := __tmp_7061(15, 8)
        arrayRegFiles(__tmp_7060 + 2.U) := __tmp_7061(23, 16)
        arrayRegFiles(__tmp_7060 + 3.U) := __tmp_7061(31, 24)
        arrayRegFiles(__tmp_7060 + 4.U) := __tmp_7061(39, 32)
        arrayRegFiles(__tmp_7060 + 5.U) := __tmp_7061(47, 40)
        arrayRegFiles(__tmp_7060 + 6.U) := __tmp_7061(55, 48)
        arrayRegFiles(__tmp_7060 + 7.U) := __tmp_7061(63, 56)

        CP := 142.U
      }

      is(154.U) {
        /*
        decl j: anvil.PrinterIndex.I16 [@93, 1]
        $8S.2 = *(SP + (68: SP)) [signed, anvil.PrinterIndex.I16, 1]  // $8S.2 = i
        undecl i: anvil.PrinterIndex.I16 [@68, 1]
        goto .155
        */


        val __tmp_7062 = ((SP + 68.U(16.W))).asUInt
        generalRegFiles(2.U) := Cat(
          arrayRegFiles(__tmp_7062 + 0.U)
        ).asSInt.pad(GENERAL_REG_WIDTH).asUInt

        CP := 155.U
      }

      is(155.U) {
        /*
        $8S.3 = ($8S.2 - (1: anvil.PrinterIndex.I16))
        goto .156
        */



        generalRegFiles(3.U) := ((generalRegFiles(2.U).asSInt - 1.S(8.W))).asUInt

        CP := 156.U
      }

      is(156.U) {
        /*
        *(SP + (93: SP)) = $8S.3 [signed, anvil.PrinterIndex.I16, 1]  // j = $8S.3
        goto .157
        */


        val __tmp_7063 = (SP + 93.U(16.W))
        val __tmp_7064 = (generalRegFiles(3.U).asSInt).asUInt
        arrayRegFiles(__tmp_7063 + 0.U) := __tmp_7064(7, 0)

        CP := 157.U
      }

      is(157.U) {
        /*
        $8S.2 = *(SP + (93: SP)) [signed, anvil.PrinterIndex.I16, 1]  // $8S.2 = j
        goto .158
        */


        val __tmp_7065 = ((SP + 93.U(16.W))).asUInt
        generalRegFiles(2.U) := Cat(
          arrayRegFiles(__tmp_7065 + 0.U)
        ).asSInt.pad(GENERAL_REG_WIDTH).asUInt

        CP := 158.U
      }

      is(158.U) {
        /*
        $1U.0 = ($8S.2 >= (0: anvil.PrinterIndex.I16))
        goto .159
        */



        generalRegFiles(0.U) := (generalRegFiles(2.U).asSInt >= 0.S(8.W)).asUInt

        CP := 159.U
      }

      is(159.U) {
        /*
        if $1U.0 goto .160 else goto .171
        */


        CP := Mux((generalRegFiles(0.U).asUInt) === 1.U, 160.U, 171.U)
      }

      is(160.U) {
        /*
        $16U.4 = *(SP + (4: SP)) [unsigned, SP, 2]  // $16U.4 = buffer
        $64U.8 = *(SP + (85: SP)) [unsigned, anvil.PrinterIndex.U, 8]  // $64U.8 = idx
        $64U.9 = *(SP + (14: SP)) [unsigned, anvil.PrinterIndex.U, 8]  // $64U.9 = mask
        goto .161
        */


        val __tmp_7066 = ((SP + 4.U(16.W))).asUInt
        generalRegFiles(4.U) := Cat(
          arrayRegFiles(__tmp_7066 + 1.U),
          arrayRegFiles(__tmp_7066 + 0.U)
        ).asUInt

        val __tmp_7067 = ((SP + 85.U(16.W))).asUInt
        generalRegFiles(8.U) := Cat(
          arrayRegFiles(__tmp_7067 + 7.U),
          arrayRegFiles(__tmp_7067 + 6.U),
          arrayRegFiles(__tmp_7067 + 5.U),
          arrayRegFiles(__tmp_7067 + 4.U),
          arrayRegFiles(__tmp_7067 + 3.U),
          arrayRegFiles(__tmp_7067 + 2.U),
          arrayRegFiles(__tmp_7067 + 1.U),
          arrayRegFiles(__tmp_7067 + 0.U)
        ).asUInt

        val __tmp_7068 = ((SP + 14.U(16.W))).asUInt
        generalRegFiles(9.U) := Cat(
          arrayRegFiles(__tmp_7068 + 7.U),
          arrayRegFiles(__tmp_7068 + 6.U),
          arrayRegFiles(__tmp_7068 + 5.U),
          arrayRegFiles(__tmp_7068 + 4.U),
          arrayRegFiles(__tmp_7068 + 3.U),
          arrayRegFiles(__tmp_7068 + 2.U),
          arrayRegFiles(__tmp_7068 + 1.U),
          arrayRegFiles(__tmp_7068 + 0.U)
        ).asUInt

        CP := 161.U
      }

      is(161.U) {
        /*
        $64U.10 = ($64U.8 & $64U.9)
        goto .162
        */



        generalRegFiles(10.U) := (generalRegFiles(8.U) & generalRegFiles(9.U))

        CP := 162.U
      }

      is(162.U) {
        /*
        $16U.5 = (SP + (38: SP))
        $8S.4 = *(SP + (93: SP)) [signed, anvil.PrinterIndex.I16, 1]  // $8S.4 = j
        goto .163
        */



        generalRegFiles(5.U) := (SP + 38.U(16.W))

        val __tmp_7069 = ((SP + 93.U(16.W))).asUInt
        generalRegFiles(4.U) := Cat(
          arrayRegFiles(__tmp_7069 + 0.U)
        ).asSInt.pad(GENERAL_REG_WIDTH).asUInt

        CP := 163.U
      }

      is(163.U) {
        /*
        $8U.0 = *(($16U.5 + (12: SP)) + ($8S.4 as SP)) [unsigned, U8, 1]  // $8U.0 = $16U.5($8S.4)
        goto .164
        */


        val __tmp_7070 = (((generalRegFiles(5.U) + 12.U(16.W)) + generalRegFiles(4.U).asSInt.asUInt)).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_7070 + 0.U)
        ).asUInt

        CP := 164.U
      }

      is(164.U) {
        /*
        *(($16U.4 + (12: SP)) + ($64U.10 as SP)) = $8U.0 [unsigned, U8, 1]  // $16U.4($64U.10) = $8U.0
        goto .165
        */


        val __tmp_7071 = ((generalRegFiles(4.U) + 12.U(16.W)) + generalRegFiles(10.U).asUInt)
        val __tmp_7072 = (generalRegFiles(0.U)).asUInt
        arrayRegFiles(__tmp_7071 + 0.U) := __tmp_7072(7, 0)

        CP := 165.U
      }

      is(165.U) {
        /*
        $8S.2 = *(SP + (93: SP)) [signed, anvil.PrinterIndex.I16, 1]  // $8S.2 = j
        goto .166
        */


        val __tmp_7073 = ((SP + 93.U(16.W))).asUInt
        generalRegFiles(2.U) := Cat(
          arrayRegFiles(__tmp_7073 + 0.U)
        ).asSInt.pad(GENERAL_REG_WIDTH).asUInt

        CP := 166.U
      }

      is(166.U) {
        /*
        $8S.3 = ($8S.2 - (1: anvil.PrinterIndex.I16))
        goto .167
        */



        generalRegFiles(3.U) := ((generalRegFiles(2.U).asSInt - 1.S(8.W))).asUInt

        CP := 167.U
      }

      is(167.U) {
        /*
        *(SP + (93: SP)) = $8S.3 [signed, anvil.PrinterIndex.I16, 1]  // j = $8S.3
        goto .168
        */


        val __tmp_7074 = (SP + 93.U(16.W))
        val __tmp_7075 = (generalRegFiles(3.U).asSInt).asUInt
        arrayRegFiles(__tmp_7074 + 0.U) := __tmp_7075(7, 0)

        CP := 168.U
      }

      is(168.U) {
        /*
        $64U.6 = *(SP + (85: SP)) [unsigned, anvil.PrinterIndex.U, 8]  // $64U.6 = idx
        goto .169
        */


        val __tmp_7076 = ((SP + 85.U(16.W))).asUInt
        generalRegFiles(6.U) := Cat(
          arrayRegFiles(__tmp_7076 + 7.U),
          arrayRegFiles(__tmp_7076 + 6.U),
          arrayRegFiles(__tmp_7076 + 5.U),
          arrayRegFiles(__tmp_7076 + 4.U),
          arrayRegFiles(__tmp_7076 + 3.U),
          arrayRegFiles(__tmp_7076 + 2.U),
          arrayRegFiles(__tmp_7076 + 1.U),
          arrayRegFiles(__tmp_7076 + 0.U)
        ).asUInt

        CP := 169.U
      }

      is(169.U) {
        /*
        $64U.8 = ($64U.6 + (1: anvil.PrinterIndex.U))
        goto .170
        */



        generalRegFiles(8.U) := (generalRegFiles(6.U) + 1.U(64.W))

        CP := 170.U
      }

      is(170.U) {
        /*
        *(SP + (85: SP)) = $64U.8 [signed, anvil.PrinterIndex.U, 8]  // idx = $64U.8
        goto .157
        */


        val __tmp_7077 = (SP + 85.U(16.W))
        val __tmp_7078 = (generalRegFiles(8.U)).asUInt
        arrayRegFiles(__tmp_7077 + 0.U) := __tmp_7078(7, 0)
        arrayRegFiles(__tmp_7077 + 1.U) := __tmp_7078(15, 8)
        arrayRegFiles(__tmp_7077 + 2.U) := __tmp_7078(23, 16)
        arrayRegFiles(__tmp_7077 + 3.U) := __tmp_7078(31, 24)
        arrayRegFiles(__tmp_7077 + 4.U) := __tmp_7078(39, 32)
        arrayRegFiles(__tmp_7077 + 5.U) := __tmp_7078(47, 40)
        arrayRegFiles(__tmp_7077 + 6.U) := __tmp_7078(55, 48)
        arrayRegFiles(__tmp_7077 + 7.U) := __tmp_7078(63, 56)

        CP := 157.U
      }

      is(171.U) {
        /*
        $64S.2 = *(SP + (30: SP)) [signed, Z, 8]  // $64S.2 = digits
        goto .172
        */


        val __tmp_7079 = ((SP + 30.U(16.W))).asUInt
        generalRegFiles(2.U) := Cat(
          arrayRegFiles(__tmp_7079 + 7.U),
          arrayRegFiles(__tmp_7079 + 6.U),
          arrayRegFiles(__tmp_7079 + 5.U),
          arrayRegFiles(__tmp_7079 + 4.U),
          arrayRegFiles(__tmp_7079 + 3.U),
          arrayRegFiles(__tmp_7079 + 2.U),
          arrayRegFiles(__tmp_7079 + 1.U),
          arrayRegFiles(__tmp_7079 + 0.U)
        ).asSInt.pad(GENERAL_REG_WIDTH).asUInt

        CP := 172.U
      }

      is(172.U) {
        /*
        $64U.7 = ($64S.2 as U64)
        goto .173
        */



        generalRegFiles(7.U) := generalRegFiles(2.U).asSInt.asUInt

        CP := 173.U
      }

      is(173.U) {
        /*
        **(SP + (2: SP)) = $64U.7 [unsigned, U64, 8]  // $res = $64U.7
        goto $ret@0
        */


        val __tmp_7080 = Cat(
          arrayRegFiles((SP + 2.U(16.W)) + 1.U),
          arrayRegFiles((SP + 2.U(16.W)) + 0.U)
        )
        val __tmp_7081 = (generalRegFiles(7.U)).asUInt
        arrayRegFiles(__tmp_7080 + 0.U) := __tmp_7081(7, 0)
        arrayRegFiles(__tmp_7080 + 1.U) := __tmp_7081(15, 8)
        arrayRegFiles(__tmp_7080 + 2.U) := __tmp_7081(23, 16)
        arrayRegFiles(__tmp_7080 + 3.U) := __tmp_7081(31, 24)
        arrayRegFiles(__tmp_7080 + 4.U) := __tmp_7081(39, 32)
        arrayRegFiles(__tmp_7080 + 5.U) := __tmp_7081(47, 40)
        arrayRegFiles(__tmp_7080 + 6.U) := __tmp_7081(55, 48)
        arrayRegFiles(__tmp_7080 + 7.U) := __tmp_7081(63, 56)

        CP := Cat(
          arrayRegFiles(SP + 0.U + 1.U),
          arrayRegFiles(SP + 0.U + 0.U)
        )

      }

      is(174.U) {
        /*
        $64S.0 = *(SP + (12: SP)) [signed, S64, 8]  // m
        $64S.1 = *(SP + (20: SP)) [signed, S64, 8]  // n
        goto .175
        */


        val __tmp_7082 = ((SP + 12.U(16.W))).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_7082 + 7.U),
          arrayRegFiles(__tmp_7082 + 6.U),
          arrayRegFiles(__tmp_7082 + 5.U),
          arrayRegFiles(__tmp_7082 + 4.U),
          arrayRegFiles(__tmp_7082 + 3.U),
          arrayRegFiles(__tmp_7082 + 2.U),
          arrayRegFiles(__tmp_7082 + 1.U),
          arrayRegFiles(__tmp_7082 + 0.U)
        ).asSInt.pad(GENERAL_REG_WIDTH).asUInt

        val __tmp_7083 = ((SP + 20.U(16.W))).asUInt
        generalRegFiles(1.U) := Cat(
          arrayRegFiles(__tmp_7083 + 7.U),
          arrayRegFiles(__tmp_7083 + 6.U),
          arrayRegFiles(__tmp_7083 + 5.U),
          arrayRegFiles(__tmp_7083 + 4.U),
          arrayRegFiles(__tmp_7083 + 3.U),
          arrayRegFiles(__tmp_7083 + 2.U),
          arrayRegFiles(__tmp_7083 + 1.U),
          arrayRegFiles(__tmp_7083 + 0.U)
        ).asSInt.pad(GENERAL_REG_WIDTH).asUInt

        CP := 175.U
      }

      is(175.U) {
        /*
        $64S.4 = *(SP + (12: SP)) [signed, S64, 8]  // $64S.4 = m
        alloc printS64$res@[15,11].20CE402B: U64 [@28, 8]
        goto .176
        */


        val __tmp_7084 = ((SP + 12.U(16.W))).asUInt
        generalRegFiles(4.U) := Cat(
          arrayRegFiles(__tmp_7084 + 7.U),
          arrayRegFiles(__tmp_7084 + 6.U),
          arrayRegFiles(__tmp_7084 + 5.U),
          arrayRegFiles(__tmp_7084 + 4.U),
          arrayRegFiles(__tmp_7084 + 3.U),
          arrayRegFiles(__tmp_7084 + 2.U),
          arrayRegFiles(__tmp_7084 + 1.U),
          arrayRegFiles(__tmp_7084 + 0.U)
        ).asSInt.pad(GENERAL_REG_WIDTH).asUInt

        CP := 176.U
      }

      is(176.U) {
        /*
        SP = SP + 60
        goto .177
        */


        SP := SP + 60.U

        CP := 177.U
      }

      is(177.U) {
        /*
        decl $ret: CP [@0, 2], $res: SP [@2, 2], buffer: SP [@4, 2], index: anvil.PrinterIndex.U [@6, 8], mask: anvil.PrinterIndex.U [@14, 8], n: S64 [@22, 8]
        *SP = (178: CP) [unsigned, CP, 2]  // $ret@0 = 1726
        *(SP + (2: SP)) = (SP - (32: SP)) [unsigned, SP, 2]  // $res@2 = -32
        *(SP + (4: SP)) = (8: SP) [unsigned, SP, 2]  // buffer = (8: SP)
        *(SP + (6: SP)) = DP [unsigned, anvil.PrinterIndex.U, 8]  // index = DP
        *(SP + (14: SP)) = (127: anvil.PrinterIndex.U) [unsigned, anvil.PrinterIndex.U, 8]  // mask = (127: anvil.PrinterIndex.U)
        *(SP + (22: SP)) = $64S.4 [signed, S64, 8]  // n = $64S.4
        goto .207
        */


        val __tmp_7085 = SP
        val __tmp_7086 = (178.U(16.W)).asUInt
        arrayRegFiles(__tmp_7085 + 0.U) := __tmp_7086(7, 0)
        arrayRegFiles(__tmp_7085 + 1.U) := __tmp_7086(15, 8)

        val __tmp_7087 = (SP + 2.U(16.W))
        val __tmp_7088 = ((SP - 32.U(16.W))).asUInt
        arrayRegFiles(__tmp_7087 + 0.U) := __tmp_7088(7, 0)
        arrayRegFiles(__tmp_7087 + 1.U) := __tmp_7088(15, 8)

        val __tmp_7089 = (SP + 4.U(16.W))
        val __tmp_7090 = (8.U(16.W)).asUInt
        arrayRegFiles(__tmp_7089 + 0.U) := __tmp_7090(7, 0)
        arrayRegFiles(__tmp_7089 + 1.U) := __tmp_7090(15, 8)

        val __tmp_7091 = (SP + 6.U(16.W))
        val __tmp_7092 = (DP).asUInt
        arrayRegFiles(__tmp_7091 + 0.U) := __tmp_7092(7, 0)
        arrayRegFiles(__tmp_7091 + 1.U) := __tmp_7092(15, 8)
        arrayRegFiles(__tmp_7091 + 2.U) := __tmp_7092(23, 16)
        arrayRegFiles(__tmp_7091 + 3.U) := __tmp_7092(31, 24)
        arrayRegFiles(__tmp_7091 + 4.U) := __tmp_7092(39, 32)
        arrayRegFiles(__tmp_7091 + 5.U) := __tmp_7092(47, 40)
        arrayRegFiles(__tmp_7091 + 6.U) := __tmp_7092(55, 48)
        arrayRegFiles(__tmp_7091 + 7.U) := __tmp_7092(63, 56)

        val __tmp_7093 = (SP + 14.U(16.W))
        val __tmp_7094 = (127.U(64.W)).asUInt
        arrayRegFiles(__tmp_7093 + 0.U) := __tmp_7094(7, 0)
        arrayRegFiles(__tmp_7093 + 1.U) := __tmp_7094(15, 8)
        arrayRegFiles(__tmp_7093 + 2.U) := __tmp_7094(23, 16)
        arrayRegFiles(__tmp_7093 + 3.U) := __tmp_7094(31, 24)
        arrayRegFiles(__tmp_7093 + 4.U) := __tmp_7094(39, 32)
        arrayRegFiles(__tmp_7093 + 5.U) := __tmp_7094(47, 40)
        arrayRegFiles(__tmp_7093 + 6.U) := __tmp_7094(55, 48)
        arrayRegFiles(__tmp_7093 + 7.U) := __tmp_7094(63, 56)

        val __tmp_7095 = (SP + 22.U(16.W))
        val __tmp_7096 = (generalRegFiles(4.U).asSInt).asUInt
        arrayRegFiles(__tmp_7095 + 0.U) := __tmp_7096(7, 0)
        arrayRegFiles(__tmp_7095 + 1.U) := __tmp_7096(15, 8)
        arrayRegFiles(__tmp_7095 + 2.U) := __tmp_7096(23, 16)
        arrayRegFiles(__tmp_7095 + 3.U) := __tmp_7096(31, 24)
        arrayRegFiles(__tmp_7095 + 4.U) := __tmp_7096(39, 32)
        arrayRegFiles(__tmp_7095 + 5.U) := __tmp_7096(47, 40)
        arrayRegFiles(__tmp_7095 + 6.U) := __tmp_7096(55, 48)
        arrayRegFiles(__tmp_7095 + 7.U) := __tmp_7096(63, 56)

        CP := 207.U
      }

      is(178.U) {
        /*
        $64U.3 = **(SP + (2: SP)) [unsigned, U64, 8]  // $3 = $res
        undecl n: S64 [@22, 8], mask: anvil.PrinterIndex.U [@14, 8], index: anvil.PrinterIndex.U [@6, 8], buffer: SP [@4, 2], $res: SP [@2, 2], $ret: CP [@0, 2]
        goto .179
        */


        val __tmp_7097 = (Cat(
          arrayRegFiles((SP + 2.U(16.W)) + 1.U),
          arrayRegFiles((SP + 2.U(16.W)) + 0.U)
        )).asUInt
        generalRegFiles(3.U) := Cat(
          arrayRegFiles(__tmp_7097 + 7.U),
          arrayRegFiles(__tmp_7097 + 6.U),
          arrayRegFiles(__tmp_7097 + 5.U),
          arrayRegFiles(__tmp_7097 + 4.U),
          arrayRegFiles(__tmp_7097 + 3.U),
          arrayRegFiles(__tmp_7097 + 2.U),
          arrayRegFiles(__tmp_7097 + 1.U),
          arrayRegFiles(__tmp_7097 + 0.U)
        ).asUInt

        CP := 179.U
      }

      is(179.U) {
        /*
        SP = SP - 60
        goto .180
        */


        SP := SP - 60.U

        CP := 180.U
      }

      is(180.U) {
        /*
        DP = DP + ($64U.3 as DP)
        goto .181
        */


        DP := DP + generalRegFiles(3.U).asUInt

        CP := 181.U
      }

      is(181.U) {
        /*
        *(((8: SP) + (12: SP)) + ((DP & (127: DP)) as SP)) = (10: U8) [unsigned, U8, 1]  // $display((DP & (127: DP))) = (10: U8)
        goto .182
        */


        val __tmp_7098 = ((8.U(16.W) + 12.U(16.W)) + (DP & 127.U(64.W)).asUInt)
        val __tmp_7099 = (10.U(8.W)).asUInt
        arrayRegFiles(__tmp_7098 + 0.U) := __tmp_7099(7, 0)

        CP := 182.U
      }

      is(182.U) {
        /*
        DP = DP + 1
        goto .183
        */


        DP := DP + 1.U

        CP := 183.U
      }

      is(183.U) {
        /*
        decl i: S64 [@36, 8]
        $64S.4 = *(SP + (12: SP)) [signed, S64, 8]  // $64S.4 = m
        $64S.5 = *(SP + (20: SP)) [signed, S64, 8]  // $64S.5 = n
        alloc shrS64$res@[16,13].92DAD3BB: S64 [@44, 8]
        goto .184
        */


        val __tmp_7100 = ((SP + 12.U(16.W))).asUInt
        generalRegFiles(4.U) := Cat(
          arrayRegFiles(__tmp_7100 + 7.U),
          arrayRegFiles(__tmp_7100 + 6.U),
          arrayRegFiles(__tmp_7100 + 5.U),
          arrayRegFiles(__tmp_7100 + 4.U),
          arrayRegFiles(__tmp_7100 + 3.U),
          arrayRegFiles(__tmp_7100 + 2.U),
          arrayRegFiles(__tmp_7100 + 1.U),
          arrayRegFiles(__tmp_7100 + 0.U)
        ).asSInt.pad(GENERAL_REG_WIDTH).asUInt

        val __tmp_7101 = ((SP + 20.U(16.W))).asUInt
        generalRegFiles(5.U) := Cat(
          arrayRegFiles(__tmp_7101 + 7.U),
          arrayRegFiles(__tmp_7101 + 6.U),
          arrayRegFiles(__tmp_7101 + 5.U),
          arrayRegFiles(__tmp_7101 + 4.U),
          arrayRegFiles(__tmp_7101 + 3.U),
          arrayRegFiles(__tmp_7101 + 2.U),
          arrayRegFiles(__tmp_7101 + 1.U),
          arrayRegFiles(__tmp_7101 + 0.U)
        ).asSInt.pad(GENERAL_REG_WIDTH).asUInt

        CP := 184.U
      }

      is(184.U) {
        /*
        SP = SP + 60
        goto .185
        */


        SP := SP + 60.U

        CP := 185.U
      }

      is(185.U) {
        /*
        decl $ret: CP [@0, 2], $res: SP [@2, 2], n: S64 [@4, 8], m: S64 [@12, 8]
        *SP = (186: CP) [unsigned, CP, 2]  // $ret@0 = 1727
        *(SP + (2: SP)) = (SP - (16: SP)) [unsigned, SP, 2]  // $res@2 = -16
        *(SP + (4: SP)) = ($64S.4 as S64) [signed, S64, 8]  // n = ($64S.4 as S64)
        *(SP + (12: SP)) = ($64S.5 as S64) [signed, S64, 8]  // m = ($64S.5 as S64)
        goto .459
        */


        val __tmp_7102 = SP
        val __tmp_7103 = (186.U(16.W)).asUInt
        arrayRegFiles(__tmp_7102 + 0.U) := __tmp_7103(7, 0)
        arrayRegFiles(__tmp_7102 + 1.U) := __tmp_7103(15, 8)

        val __tmp_7104 = (SP + 2.U(16.W))
        val __tmp_7105 = ((SP - 16.U(16.W))).asUInt
        arrayRegFiles(__tmp_7104 + 0.U) := __tmp_7105(7, 0)
        arrayRegFiles(__tmp_7104 + 1.U) := __tmp_7105(15, 8)

        val __tmp_7106 = (SP + 4.U(16.W))
        val __tmp_7107 = (generalRegFiles(4.U).asSInt.asSInt).asUInt
        arrayRegFiles(__tmp_7106 + 0.U) := __tmp_7107(7, 0)
        arrayRegFiles(__tmp_7106 + 1.U) := __tmp_7107(15, 8)
        arrayRegFiles(__tmp_7106 + 2.U) := __tmp_7107(23, 16)
        arrayRegFiles(__tmp_7106 + 3.U) := __tmp_7107(31, 24)
        arrayRegFiles(__tmp_7106 + 4.U) := __tmp_7107(39, 32)
        arrayRegFiles(__tmp_7106 + 5.U) := __tmp_7107(47, 40)
        arrayRegFiles(__tmp_7106 + 6.U) := __tmp_7107(55, 48)
        arrayRegFiles(__tmp_7106 + 7.U) := __tmp_7107(63, 56)

        val __tmp_7108 = (SP + 12.U(16.W))
        val __tmp_7109 = (generalRegFiles(5.U).asSInt.asSInt).asUInt
        arrayRegFiles(__tmp_7108 + 0.U) := __tmp_7109(7, 0)
        arrayRegFiles(__tmp_7108 + 1.U) := __tmp_7109(15, 8)
        arrayRegFiles(__tmp_7108 + 2.U) := __tmp_7109(23, 16)
        arrayRegFiles(__tmp_7108 + 3.U) := __tmp_7109(31, 24)
        arrayRegFiles(__tmp_7108 + 4.U) := __tmp_7109(39, 32)
        arrayRegFiles(__tmp_7108 + 5.U) := __tmp_7109(47, 40)
        arrayRegFiles(__tmp_7108 + 6.U) := __tmp_7109(55, 48)
        arrayRegFiles(__tmp_7108 + 7.U) := __tmp_7109(63, 56)

        CP := 459.U
      }

      is(186.U) {
        /*
        $64S.6 = **(SP + (2: SP)) [signed, S64, 8]  // $6 = $res
        undecl m: S64 [@12, 8], n: S64 [@4, 8], $res: SP [@2, 2], $ret: CP [@0, 2]
        goto .187
        */


        val __tmp_7110 = (Cat(
          arrayRegFiles((SP + 2.U(16.W)) + 1.U),
          arrayRegFiles((SP + 2.U(16.W)) + 0.U)
        )).asUInt
        generalRegFiles(6.U) := Cat(
          arrayRegFiles(__tmp_7110 + 7.U),
          arrayRegFiles(__tmp_7110 + 6.U),
          arrayRegFiles(__tmp_7110 + 5.U),
          arrayRegFiles(__tmp_7110 + 4.U),
          arrayRegFiles(__tmp_7110 + 3.U),
          arrayRegFiles(__tmp_7110 + 2.U),
          arrayRegFiles(__tmp_7110 + 1.U),
          arrayRegFiles(__tmp_7110 + 0.U)
        ).asSInt.pad(GENERAL_REG_WIDTH).asUInt

        CP := 187.U
      }

      is(187.U) {
        /*
        SP = SP - 60
        goto .188
        */


        SP := SP - 60.U

        CP := 188.U
      }

      is(188.U) {
        /*
        $64S.6 = ($64S.6 as S64)
        goto .189
        */



        generalRegFiles(6.U) := (generalRegFiles(6.U).asSInt.asSInt).asUInt

        CP := 189.U
      }

      is(189.U) {
        /*
        *(SP + (36: SP)) = $64S.6 [signed, S64, 8]  // i = $64S.6
        goto .190
        */


        val __tmp_7111 = (SP + 36.U(16.W))
        val __tmp_7112 = (generalRegFiles(6.U).asSInt).asUInt
        arrayRegFiles(__tmp_7111 + 0.U) := __tmp_7112(7, 0)
        arrayRegFiles(__tmp_7111 + 1.U) := __tmp_7112(15, 8)
        arrayRegFiles(__tmp_7111 + 2.U) := __tmp_7112(23, 16)
        arrayRegFiles(__tmp_7111 + 3.U) := __tmp_7112(31, 24)
        arrayRegFiles(__tmp_7111 + 4.U) := __tmp_7112(39, 32)
        arrayRegFiles(__tmp_7111 + 5.U) := __tmp_7112(47, 40)
        arrayRegFiles(__tmp_7111 + 6.U) := __tmp_7112(55, 48)
        arrayRegFiles(__tmp_7111 + 7.U) := __tmp_7112(63, 56)

        CP := 190.U
      }

      is(190.U) {
        /*
        $64S.4 = *(SP + (36: SP)) [signed, S64, 8]  // $64S.4 = i
        alloc printS64$res@[17,11].024EA5AE: U64 [@52, 8]
        goto .191
        */


        val __tmp_7113 = ((SP + 36.U(16.W))).asUInt
        generalRegFiles(4.U) := Cat(
          arrayRegFiles(__tmp_7113 + 7.U),
          arrayRegFiles(__tmp_7113 + 6.U),
          arrayRegFiles(__tmp_7113 + 5.U),
          arrayRegFiles(__tmp_7113 + 4.U),
          arrayRegFiles(__tmp_7113 + 3.U),
          arrayRegFiles(__tmp_7113 + 2.U),
          arrayRegFiles(__tmp_7113 + 1.U),
          arrayRegFiles(__tmp_7113 + 0.U)
        ).asSInt.pad(GENERAL_REG_WIDTH).asUInt

        CP := 191.U
      }

      is(191.U) {
        /*
        SP = SP + 60
        goto .192
        */


        SP := SP + 60.U

        CP := 192.U
      }

      is(192.U) {
        /*
        decl $ret: CP [@0, 2], $res: SP [@2, 2], buffer: SP [@4, 2], index: anvil.PrinterIndex.U [@6, 8], mask: anvil.PrinterIndex.U [@14, 8], n: S64 [@22, 8]
        *SP = (193: CP) [unsigned, CP, 2]  // $ret@0 = 1728
        *(SP + (2: SP)) = (SP - (8: SP)) [unsigned, SP, 2]  // $res@2 = -8
        *(SP + (4: SP)) = (8: SP) [unsigned, SP, 2]  // buffer = (8: SP)
        *(SP + (6: SP)) = DP [unsigned, anvil.PrinterIndex.U, 8]  // index = DP
        *(SP + (14: SP)) = (127: anvil.PrinterIndex.U) [unsigned, anvil.PrinterIndex.U, 8]  // mask = (127: anvil.PrinterIndex.U)
        *(SP + (22: SP)) = $64S.4 [signed, S64, 8]  // n = $64S.4
        goto .207
        */


        val __tmp_7114 = SP
        val __tmp_7115 = (193.U(16.W)).asUInt
        arrayRegFiles(__tmp_7114 + 0.U) := __tmp_7115(7, 0)
        arrayRegFiles(__tmp_7114 + 1.U) := __tmp_7115(15, 8)

        val __tmp_7116 = (SP + 2.U(16.W))
        val __tmp_7117 = ((SP - 8.U(16.W))).asUInt
        arrayRegFiles(__tmp_7116 + 0.U) := __tmp_7117(7, 0)
        arrayRegFiles(__tmp_7116 + 1.U) := __tmp_7117(15, 8)

        val __tmp_7118 = (SP + 4.U(16.W))
        val __tmp_7119 = (8.U(16.W)).asUInt
        arrayRegFiles(__tmp_7118 + 0.U) := __tmp_7119(7, 0)
        arrayRegFiles(__tmp_7118 + 1.U) := __tmp_7119(15, 8)

        val __tmp_7120 = (SP + 6.U(16.W))
        val __tmp_7121 = (DP).asUInt
        arrayRegFiles(__tmp_7120 + 0.U) := __tmp_7121(7, 0)
        arrayRegFiles(__tmp_7120 + 1.U) := __tmp_7121(15, 8)
        arrayRegFiles(__tmp_7120 + 2.U) := __tmp_7121(23, 16)
        arrayRegFiles(__tmp_7120 + 3.U) := __tmp_7121(31, 24)
        arrayRegFiles(__tmp_7120 + 4.U) := __tmp_7121(39, 32)
        arrayRegFiles(__tmp_7120 + 5.U) := __tmp_7121(47, 40)
        arrayRegFiles(__tmp_7120 + 6.U) := __tmp_7121(55, 48)
        arrayRegFiles(__tmp_7120 + 7.U) := __tmp_7121(63, 56)

        val __tmp_7122 = (SP + 14.U(16.W))
        val __tmp_7123 = (127.U(64.W)).asUInt
        arrayRegFiles(__tmp_7122 + 0.U) := __tmp_7123(7, 0)
        arrayRegFiles(__tmp_7122 + 1.U) := __tmp_7123(15, 8)
        arrayRegFiles(__tmp_7122 + 2.U) := __tmp_7123(23, 16)
        arrayRegFiles(__tmp_7122 + 3.U) := __tmp_7123(31, 24)
        arrayRegFiles(__tmp_7122 + 4.U) := __tmp_7123(39, 32)
        arrayRegFiles(__tmp_7122 + 5.U) := __tmp_7123(47, 40)
        arrayRegFiles(__tmp_7122 + 6.U) := __tmp_7123(55, 48)
        arrayRegFiles(__tmp_7122 + 7.U) := __tmp_7123(63, 56)

        val __tmp_7124 = (SP + 22.U(16.W))
        val __tmp_7125 = (generalRegFiles(4.U).asSInt).asUInt
        arrayRegFiles(__tmp_7124 + 0.U) := __tmp_7125(7, 0)
        arrayRegFiles(__tmp_7124 + 1.U) := __tmp_7125(15, 8)
        arrayRegFiles(__tmp_7124 + 2.U) := __tmp_7125(23, 16)
        arrayRegFiles(__tmp_7124 + 3.U) := __tmp_7125(31, 24)
        arrayRegFiles(__tmp_7124 + 4.U) := __tmp_7125(39, 32)
        arrayRegFiles(__tmp_7124 + 5.U) := __tmp_7125(47, 40)
        arrayRegFiles(__tmp_7124 + 6.U) := __tmp_7125(55, 48)
        arrayRegFiles(__tmp_7124 + 7.U) := __tmp_7125(63, 56)

        CP := 207.U
      }

      is(193.U) {
        /*
        $64U.3 = **(SP + (2: SP)) [unsigned, U64, 8]  // $3 = $res
        undecl n: S64 [@22, 8], mask: anvil.PrinterIndex.U [@14, 8], index: anvil.PrinterIndex.U [@6, 8], buffer: SP [@4, 2], $res: SP [@2, 2], $ret: CP [@0, 2]
        goto .194
        */


        val __tmp_7126 = (Cat(
          arrayRegFiles((SP + 2.U(16.W)) + 1.U),
          arrayRegFiles((SP + 2.U(16.W)) + 0.U)
        )).asUInt
        generalRegFiles(3.U) := Cat(
          arrayRegFiles(__tmp_7126 + 7.U),
          arrayRegFiles(__tmp_7126 + 6.U),
          arrayRegFiles(__tmp_7126 + 5.U),
          arrayRegFiles(__tmp_7126 + 4.U),
          arrayRegFiles(__tmp_7126 + 3.U),
          arrayRegFiles(__tmp_7126 + 2.U),
          arrayRegFiles(__tmp_7126 + 1.U),
          arrayRegFiles(__tmp_7126 + 0.U)
        ).asUInt

        CP := 194.U
      }

      is(194.U) {
        /*
        SP = SP - 60
        goto .195
        */


        SP := SP - 60.U

        CP := 195.U
      }

      is(195.U) {
        /*
        DP = DP + ($64U.3 as DP)
        goto .196
        */


        DP := DP + generalRegFiles(3.U).asUInt

        CP := 196.U
      }

      is(196.U) {
        /*
        unalloc printS64$res@[17,11].024EA5AE: U64 [@52, 8]
        *(((8: SP) + (12: SP)) + ((DP & (127: DP)) as SP)) = (10: U8) [unsigned, U8, 1]  // $display((DP & (127: DP))) = (10: U8)
        goto .197
        */


        val __tmp_7127 = ((8.U(16.W) + 12.U(16.W)) + (DP & 127.U(64.W)).asUInt)
        val __tmp_7128 = (10.U(8.W)).asUInt
        arrayRegFiles(__tmp_7127 + 0.U) := __tmp_7128(7, 0)

        CP := 197.U
      }

      is(197.U) {
        /*
        DP = DP + 1
        goto .198
        */


        DP := DP + 1.U

        CP := 198.U
      }

      is(198.U) {
        /*
        $64S.4 = *(SP + (36: SP)) [signed, S64, 8]  // $64S.4 = i
        $64S.5 = *(SP + (20: SP)) [signed, S64, 8]  // $64S.5 = n
        alloc shlU64$res@[18,9].7B13205C: U64 [@52, 8]
        goto .199
        */


        val __tmp_7129 = ((SP + 36.U(16.W))).asUInt
        generalRegFiles(4.U) := Cat(
          arrayRegFiles(__tmp_7129 + 7.U),
          arrayRegFiles(__tmp_7129 + 6.U),
          arrayRegFiles(__tmp_7129 + 5.U),
          arrayRegFiles(__tmp_7129 + 4.U),
          arrayRegFiles(__tmp_7129 + 3.U),
          arrayRegFiles(__tmp_7129 + 2.U),
          arrayRegFiles(__tmp_7129 + 1.U),
          arrayRegFiles(__tmp_7129 + 0.U)
        ).asSInt.pad(GENERAL_REG_WIDTH).asUInt

        val __tmp_7130 = ((SP + 20.U(16.W))).asUInt
        generalRegFiles(5.U) := Cat(
          arrayRegFiles(__tmp_7130 + 7.U),
          arrayRegFiles(__tmp_7130 + 6.U),
          arrayRegFiles(__tmp_7130 + 5.U),
          arrayRegFiles(__tmp_7130 + 4.U),
          arrayRegFiles(__tmp_7130 + 3.U),
          arrayRegFiles(__tmp_7130 + 2.U),
          arrayRegFiles(__tmp_7130 + 1.U),
          arrayRegFiles(__tmp_7130 + 0.U)
        ).asSInt.pad(GENERAL_REG_WIDTH).asUInt

        CP := 199.U
      }

      is(199.U) {
        /*
        SP = SP + 60
        goto .200
        */


        SP := SP + 60.U

        CP := 200.U
      }

      is(200.U) {
        /*
        decl $ret: CP [@0, 2], $res: SP [@2, 2], n: U64 [@4, 8], m: U64 [@12, 8]
        *SP = (201: CP) [unsigned, CP, 2]  // $ret@0 = 1729
        *(SP + (2: SP)) = (SP - (8: SP)) [unsigned, SP, 2]  // $res@2 = -8
        *(SP + (4: SP)) = ($64S.4 as U64) [unsigned, U64, 8]  // n = ($64S.4 as U64)
        *(SP + (12: SP)) = ($64S.5 as U64) [unsigned, U64, 8]  // m = ($64S.5 as U64)
        goto .428
        */


        val __tmp_7131 = SP
        val __tmp_7132 = (201.U(16.W)).asUInt
        arrayRegFiles(__tmp_7131 + 0.U) := __tmp_7132(7, 0)
        arrayRegFiles(__tmp_7131 + 1.U) := __tmp_7132(15, 8)

        val __tmp_7133 = (SP + 2.U(16.W))
        val __tmp_7134 = ((SP - 8.U(16.W))).asUInt
        arrayRegFiles(__tmp_7133 + 0.U) := __tmp_7134(7, 0)
        arrayRegFiles(__tmp_7133 + 1.U) := __tmp_7134(15, 8)

        val __tmp_7135 = (SP + 4.U(16.W))
        val __tmp_7136 = (generalRegFiles(4.U).asSInt.asUInt).asUInt
        arrayRegFiles(__tmp_7135 + 0.U) := __tmp_7136(7, 0)
        arrayRegFiles(__tmp_7135 + 1.U) := __tmp_7136(15, 8)
        arrayRegFiles(__tmp_7135 + 2.U) := __tmp_7136(23, 16)
        arrayRegFiles(__tmp_7135 + 3.U) := __tmp_7136(31, 24)
        arrayRegFiles(__tmp_7135 + 4.U) := __tmp_7136(39, 32)
        arrayRegFiles(__tmp_7135 + 5.U) := __tmp_7136(47, 40)
        arrayRegFiles(__tmp_7135 + 6.U) := __tmp_7136(55, 48)
        arrayRegFiles(__tmp_7135 + 7.U) := __tmp_7136(63, 56)

        val __tmp_7137 = (SP + 12.U(16.W))
        val __tmp_7138 = (generalRegFiles(5.U).asSInt.asUInt).asUInt
        arrayRegFiles(__tmp_7137 + 0.U) := __tmp_7138(7, 0)
        arrayRegFiles(__tmp_7137 + 1.U) := __tmp_7138(15, 8)
        arrayRegFiles(__tmp_7137 + 2.U) := __tmp_7138(23, 16)
        arrayRegFiles(__tmp_7137 + 3.U) := __tmp_7138(31, 24)
        arrayRegFiles(__tmp_7137 + 4.U) := __tmp_7138(39, 32)
        arrayRegFiles(__tmp_7137 + 5.U) := __tmp_7138(47, 40)
        arrayRegFiles(__tmp_7137 + 6.U) := __tmp_7138(55, 48)
        arrayRegFiles(__tmp_7137 + 7.U) := __tmp_7138(63, 56)

        CP := 428.U
      }

      is(201.U) {
        /*
        $64U.4 = **(SP + (2: SP)) [unsigned, U64, 8]  // $4 = $res
        undecl m: U64 [@12, 8], n: U64 [@4, 8], $res: SP [@2, 2], $ret: CP [@0, 2]
        goto .202
        */


        val __tmp_7139 = (Cat(
          arrayRegFiles((SP + 2.U(16.W)) + 1.U),
          arrayRegFiles((SP + 2.U(16.W)) + 0.U)
        )).asUInt
        generalRegFiles(4.U) := Cat(
          arrayRegFiles(__tmp_7139 + 7.U),
          arrayRegFiles(__tmp_7139 + 6.U),
          arrayRegFiles(__tmp_7139 + 5.U),
          arrayRegFiles(__tmp_7139 + 4.U),
          arrayRegFiles(__tmp_7139 + 3.U),
          arrayRegFiles(__tmp_7139 + 2.U),
          arrayRegFiles(__tmp_7139 + 1.U),
          arrayRegFiles(__tmp_7139 + 0.U)
        ).asUInt

        CP := 202.U
      }

      is(202.U) {
        /*
        SP = SP - 60
        goto .203
        */


        SP := SP - 60.U

        CP := 203.U
      }

      is(203.U) {
        /*
        $64S.6 = ($64U.4 as S64)
        goto .204
        */



        generalRegFiles(6.U) := (generalRegFiles(4.U).asSInt).asUInt

        CP := 204.U
      }

      is(204.U) {
        /*
        *(SP + (36: SP)) = $64S.6 [signed, S64, 8]  // i = $64S.6
        goto .205
        */


        val __tmp_7140 = (SP + 36.U(16.W))
        val __tmp_7141 = (generalRegFiles(6.U).asSInt).asUInt
        arrayRegFiles(__tmp_7140 + 0.U) := __tmp_7141(7, 0)
        arrayRegFiles(__tmp_7140 + 1.U) := __tmp_7141(15, 8)
        arrayRegFiles(__tmp_7140 + 2.U) := __tmp_7141(23, 16)
        arrayRegFiles(__tmp_7140 + 3.U) := __tmp_7141(31, 24)
        arrayRegFiles(__tmp_7140 + 4.U) := __tmp_7141(39, 32)
        arrayRegFiles(__tmp_7140 + 5.U) := __tmp_7141(47, 40)
        arrayRegFiles(__tmp_7140 + 6.U) := __tmp_7141(55, 48)
        arrayRegFiles(__tmp_7140 + 7.U) := __tmp_7141(63, 56)

        CP := 205.U
      }

      is(205.U) {
        /*
        unalloc shlU64$res@[18,9].7B13205C: U64 [@52, 8]
        $64S.4 = *(SP + (36: SP)) [signed, S64, 8]  // $64S.4 = i
        undecl i: S64 [@36, 8]
        goto .206
        */


        val __tmp_7142 = ((SP + 36.U(16.W))).asUInt
        generalRegFiles(4.U) := Cat(
          arrayRegFiles(__tmp_7142 + 7.U),
          arrayRegFiles(__tmp_7142 + 6.U),
          arrayRegFiles(__tmp_7142 + 5.U),
          arrayRegFiles(__tmp_7142 + 4.U),
          arrayRegFiles(__tmp_7142 + 3.U),
          arrayRegFiles(__tmp_7142 + 2.U),
          arrayRegFiles(__tmp_7142 + 1.U),
          arrayRegFiles(__tmp_7142 + 0.U)
        ).asSInt.pad(GENERAL_REG_WIDTH).asUInt

        CP := 206.U
      }

      is(206.U) {
        /*
        **(SP + (2: SP)) = $64S.4 [signed, S64, 8]  // $res = $64S.4
        goto $ret@0
        */


        val __tmp_7143 = Cat(
          arrayRegFiles((SP + 2.U(16.W)) + 1.U),
          arrayRegFiles((SP + 2.U(16.W)) + 0.U)
        )
        val __tmp_7144 = (generalRegFiles(4.U).asSInt).asUInt
        arrayRegFiles(__tmp_7143 + 0.U) := __tmp_7144(7, 0)
        arrayRegFiles(__tmp_7143 + 1.U) := __tmp_7144(15, 8)
        arrayRegFiles(__tmp_7143 + 2.U) := __tmp_7144(23, 16)
        arrayRegFiles(__tmp_7143 + 3.U) := __tmp_7144(31, 24)
        arrayRegFiles(__tmp_7143 + 4.U) := __tmp_7144(39, 32)
        arrayRegFiles(__tmp_7143 + 5.U) := __tmp_7144(47, 40)
        arrayRegFiles(__tmp_7143 + 6.U) := __tmp_7144(55, 48)
        arrayRegFiles(__tmp_7143 + 7.U) := __tmp_7144(63, 56)

        CP := Cat(
          arrayRegFiles(SP + 0.U + 1.U),
          arrayRegFiles(SP + 0.U + 0.U)
        )

      }

      is(207.U) {
        /*
        $16U.0 = *(SP + (4: SP)) [unsigned, MS[anvil.PrinterIndex.U, U8], 2]  // buffer
        $64U.1 = *(SP + (6: SP)) [unsigned, anvil.PrinterIndex.U, 8]  // index
        $64U.2 = *(SP + (14: SP)) [unsigned, anvil.PrinterIndex.U, 8]  // mask
        $64S.3 = *(SP + (22: SP)) [signed, S64, 8]  // n
        goto .208
        */


        val __tmp_7145 = ((SP + 4.U(16.W))).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_7145 + 1.U),
          arrayRegFiles(__tmp_7145 + 0.U)
        ).asUInt

        val __tmp_7146 = ((SP + 6.U(16.W))).asUInt
        generalRegFiles(1.U) := Cat(
          arrayRegFiles(__tmp_7146 + 7.U),
          arrayRegFiles(__tmp_7146 + 6.U),
          arrayRegFiles(__tmp_7146 + 5.U),
          arrayRegFiles(__tmp_7146 + 4.U),
          arrayRegFiles(__tmp_7146 + 3.U),
          arrayRegFiles(__tmp_7146 + 2.U),
          arrayRegFiles(__tmp_7146 + 1.U),
          arrayRegFiles(__tmp_7146 + 0.U)
        ).asUInt

        val __tmp_7147 = ((SP + 14.U(16.W))).asUInt
        generalRegFiles(2.U) := Cat(
          arrayRegFiles(__tmp_7147 + 7.U),
          arrayRegFiles(__tmp_7147 + 6.U),
          arrayRegFiles(__tmp_7147 + 5.U),
          arrayRegFiles(__tmp_7147 + 4.U),
          arrayRegFiles(__tmp_7147 + 3.U),
          arrayRegFiles(__tmp_7147 + 2.U),
          arrayRegFiles(__tmp_7147 + 1.U),
          arrayRegFiles(__tmp_7147 + 0.U)
        ).asUInt

        val __tmp_7148 = ((SP + 22.U(16.W))).asUInt
        generalRegFiles(3.U) := Cat(
          arrayRegFiles(__tmp_7148 + 7.U),
          arrayRegFiles(__tmp_7148 + 6.U),
          arrayRegFiles(__tmp_7148 + 5.U),
          arrayRegFiles(__tmp_7148 + 4.U),
          arrayRegFiles(__tmp_7148 + 3.U),
          arrayRegFiles(__tmp_7148 + 2.U),
          arrayRegFiles(__tmp_7148 + 1.U),
          arrayRegFiles(__tmp_7148 + 0.U)
        ).asSInt.pad(GENERAL_REG_WIDTH).asUInt

        CP := 208.U
      }

      is(208.U) {
        /*
        $64S.2 = *(SP + (22: SP)) [signed, S64, 8]  // $64S.2 = n
        goto .209
        */


        val __tmp_7149 = ((SP + 22.U(16.W))).asUInt
        generalRegFiles(2.U) := Cat(
          arrayRegFiles(__tmp_7149 + 7.U),
          arrayRegFiles(__tmp_7149 + 6.U),
          arrayRegFiles(__tmp_7149 + 5.U),
          arrayRegFiles(__tmp_7149 + 4.U),
          arrayRegFiles(__tmp_7149 + 3.U),
          arrayRegFiles(__tmp_7149 + 2.U),
          arrayRegFiles(__tmp_7149 + 1.U),
          arrayRegFiles(__tmp_7149 + 0.U)
        ).asSInt.pad(GENERAL_REG_WIDTH).asUInt

        CP := 209.U
      }

      is(209.U) {
        /*
        $1U.2 = ($64S.2 ≡ (-9223372036854775808: S64))
        goto .210
        */



        generalRegFiles(2.U) := (generalRegFiles(2.U).asSInt === BigInt("-9223372036854775808").S(64.W)).asUInt

        CP := 210.U
      }

      is(210.U) {
        /*
        if $1U.2 goto .211 else goto .310
        */


        CP := Mux((generalRegFiles(2.U).asUInt) === 1.U, 211.U, 310.U)
      }

      is(211.U) {
        /*
        $16U.3 = *(SP + (4: SP)) [unsigned, SP, 2]  // $16U.3 = buffer
        $64U.6 = *(SP + (6: SP)) [unsigned, anvil.PrinterIndex.U, 8]  // $64U.6 = index
        $64U.8 = *(SP + (14: SP)) [unsigned, anvil.PrinterIndex.U, 8]  // $64U.8 = mask
        goto .212
        */


        val __tmp_7150 = ((SP + 4.U(16.W))).asUInt
        generalRegFiles(3.U) := Cat(
          arrayRegFiles(__tmp_7150 + 1.U),
          arrayRegFiles(__tmp_7150 + 0.U)
        ).asUInt

        val __tmp_7151 = ((SP + 6.U(16.W))).asUInt
        generalRegFiles(6.U) := Cat(
          arrayRegFiles(__tmp_7151 + 7.U),
          arrayRegFiles(__tmp_7151 + 6.U),
          arrayRegFiles(__tmp_7151 + 5.U),
          arrayRegFiles(__tmp_7151 + 4.U),
          arrayRegFiles(__tmp_7151 + 3.U),
          arrayRegFiles(__tmp_7151 + 2.U),
          arrayRegFiles(__tmp_7151 + 1.U),
          arrayRegFiles(__tmp_7151 + 0.U)
        ).asUInt

        val __tmp_7152 = ((SP + 14.U(16.W))).asUInt
        generalRegFiles(8.U) := Cat(
          arrayRegFiles(__tmp_7152 + 7.U),
          arrayRegFiles(__tmp_7152 + 6.U),
          arrayRegFiles(__tmp_7152 + 5.U),
          arrayRegFiles(__tmp_7152 + 4.U),
          arrayRegFiles(__tmp_7152 + 3.U),
          arrayRegFiles(__tmp_7152 + 2.U),
          arrayRegFiles(__tmp_7152 + 1.U),
          arrayRegFiles(__tmp_7152 + 0.U)
        ).asUInt

        CP := 212.U
      }

      is(212.U) {
        /*
        $64U.9 = ($64U.6 & $64U.8)
        goto .213
        */



        generalRegFiles(9.U) := (generalRegFiles(6.U) & generalRegFiles(8.U))

        CP := 213.U
      }

      is(213.U) {
        /*
        *(($16U.3 + (12: SP)) + ($64U.9 as SP)) = (45: U8) [unsigned, U8, 1]  // $16U.3($64U.9) = (45: U8)
        goto .214
        */


        val __tmp_7153 = ((generalRegFiles(3.U) + 12.U(16.W)) + generalRegFiles(9.U).asUInt)
        val __tmp_7154 = (45.U(8.W)).asUInt
        arrayRegFiles(__tmp_7153 + 0.U) := __tmp_7154(7, 0)

        CP := 214.U
      }

      is(214.U) {
        /*
        $16U.3 = *(SP + (4: SP)) [unsigned, SP, 2]  // $16U.3 = buffer
        $64U.6 = *(SP + (6: SP)) [unsigned, anvil.PrinterIndex.U, 8]  // $64U.6 = index
        goto .215
        */


        val __tmp_7155 = ((SP + 4.U(16.W))).asUInt
        generalRegFiles(3.U) := Cat(
          arrayRegFiles(__tmp_7155 + 1.U),
          arrayRegFiles(__tmp_7155 + 0.U)
        ).asUInt

        val __tmp_7156 = ((SP + 6.U(16.W))).asUInt
        generalRegFiles(6.U) := Cat(
          arrayRegFiles(__tmp_7156 + 7.U),
          arrayRegFiles(__tmp_7156 + 6.U),
          arrayRegFiles(__tmp_7156 + 5.U),
          arrayRegFiles(__tmp_7156 + 4.U),
          arrayRegFiles(__tmp_7156 + 3.U),
          arrayRegFiles(__tmp_7156 + 2.U),
          arrayRegFiles(__tmp_7156 + 1.U),
          arrayRegFiles(__tmp_7156 + 0.U)
        ).asUInt

        CP := 215.U
      }

      is(215.U) {
        /*
        $64U.8 = ($64U.6 + (1: anvil.PrinterIndex.U))
        goto .216
        */



        generalRegFiles(8.U) := (generalRegFiles(6.U) + 1.U(64.W))

        CP := 216.U
      }

      is(216.U) {
        /*
        $64U.9 = *(SP + (14: SP)) [unsigned, anvil.PrinterIndex.U, 8]  // $64U.9 = mask
        goto .217
        */


        val __tmp_7157 = ((SP + 14.U(16.W))).asUInt
        generalRegFiles(9.U) := Cat(
          arrayRegFiles(__tmp_7157 + 7.U),
          arrayRegFiles(__tmp_7157 + 6.U),
          arrayRegFiles(__tmp_7157 + 5.U),
          arrayRegFiles(__tmp_7157 + 4.U),
          arrayRegFiles(__tmp_7157 + 3.U),
          arrayRegFiles(__tmp_7157 + 2.U),
          arrayRegFiles(__tmp_7157 + 1.U),
          arrayRegFiles(__tmp_7157 + 0.U)
        ).asUInt

        CP := 217.U
      }

      is(217.U) {
        /*
        $64U.10 = ($64U.8 & $64U.9)
        goto .218
        */



        generalRegFiles(10.U) := (generalRegFiles(8.U) & generalRegFiles(9.U))

        CP := 218.U
      }

      is(218.U) {
        /*
        *(($16U.3 + (12: SP)) + ($64U.10 as SP)) = (57: U8) [unsigned, U8, 1]  // $16U.3($64U.10) = (57: U8)
        goto .219
        */


        val __tmp_7158 = ((generalRegFiles(3.U) + 12.U(16.W)) + generalRegFiles(10.U).asUInt)
        val __tmp_7159 = (57.U(8.W)).asUInt
        arrayRegFiles(__tmp_7158 + 0.U) := __tmp_7159(7, 0)

        CP := 219.U
      }

      is(219.U) {
        /*
        $16U.3 = *(SP + (4: SP)) [unsigned, SP, 2]  // $16U.3 = buffer
        $64U.6 = *(SP + (6: SP)) [unsigned, anvil.PrinterIndex.U, 8]  // $64U.6 = index
        goto .220
        */


        val __tmp_7160 = ((SP + 4.U(16.W))).asUInt
        generalRegFiles(3.U) := Cat(
          arrayRegFiles(__tmp_7160 + 1.U),
          arrayRegFiles(__tmp_7160 + 0.U)
        ).asUInt

        val __tmp_7161 = ((SP + 6.U(16.W))).asUInt
        generalRegFiles(6.U) := Cat(
          arrayRegFiles(__tmp_7161 + 7.U),
          arrayRegFiles(__tmp_7161 + 6.U),
          arrayRegFiles(__tmp_7161 + 5.U),
          arrayRegFiles(__tmp_7161 + 4.U),
          arrayRegFiles(__tmp_7161 + 3.U),
          arrayRegFiles(__tmp_7161 + 2.U),
          arrayRegFiles(__tmp_7161 + 1.U),
          arrayRegFiles(__tmp_7161 + 0.U)
        ).asUInt

        CP := 220.U
      }

      is(220.U) {
        /*
        $64U.8 = ($64U.6 + (2: anvil.PrinterIndex.U))
        goto .221
        */



        generalRegFiles(8.U) := (generalRegFiles(6.U) + 2.U(64.W))

        CP := 221.U
      }

      is(221.U) {
        /*
        $64U.9 = *(SP + (14: SP)) [unsigned, anvil.PrinterIndex.U, 8]  // $64U.9 = mask
        goto .222
        */


        val __tmp_7162 = ((SP + 14.U(16.W))).asUInt
        generalRegFiles(9.U) := Cat(
          arrayRegFiles(__tmp_7162 + 7.U),
          arrayRegFiles(__tmp_7162 + 6.U),
          arrayRegFiles(__tmp_7162 + 5.U),
          arrayRegFiles(__tmp_7162 + 4.U),
          arrayRegFiles(__tmp_7162 + 3.U),
          arrayRegFiles(__tmp_7162 + 2.U),
          arrayRegFiles(__tmp_7162 + 1.U),
          arrayRegFiles(__tmp_7162 + 0.U)
        ).asUInt

        CP := 222.U
      }

      is(222.U) {
        /*
        $64U.10 = ($64U.8 & $64U.9)
        goto .223
        */



        generalRegFiles(10.U) := (generalRegFiles(8.U) & generalRegFiles(9.U))

        CP := 223.U
      }

      is(223.U) {
        /*
        *(($16U.3 + (12: SP)) + ($64U.10 as SP)) = (50: U8) [unsigned, U8, 1]  // $16U.3($64U.10) = (50: U8)
        goto .224
        */


        val __tmp_7163 = ((generalRegFiles(3.U) + 12.U(16.W)) + generalRegFiles(10.U).asUInt)
        val __tmp_7164 = (50.U(8.W)).asUInt
        arrayRegFiles(__tmp_7163 + 0.U) := __tmp_7164(7, 0)

        CP := 224.U
      }

      is(224.U) {
        /*
        $16U.3 = *(SP + (4: SP)) [unsigned, SP, 2]  // $16U.3 = buffer
        $64U.6 = *(SP + (6: SP)) [unsigned, anvil.PrinterIndex.U, 8]  // $64U.6 = index
        goto .225
        */


        val __tmp_7165 = ((SP + 4.U(16.W))).asUInt
        generalRegFiles(3.U) := Cat(
          arrayRegFiles(__tmp_7165 + 1.U),
          arrayRegFiles(__tmp_7165 + 0.U)
        ).asUInt

        val __tmp_7166 = ((SP + 6.U(16.W))).asUInt
        generalRegFiles(6.U) := Cat(
          arrayRegFiles(__tmp_7166 + 7.U),
          arrayRegFiles(__tmp_7166 + 6.U),
          arrayRegFiles(__tmp_7166 + 5.U),
          arrayRegFiles(__tmp_7166 + 4.U),
          arrayRegFiles(__tmp_7166 + 3.U),
          arrayRegFiles(__tmp_7166 + 2.U),
          arrayRegFiles(__tmp_7166 + 1.U),
          arrayRegFiles(__tmp_7166 + 0.U)
        ).asUInt

        CP := 225.U
      }

      is(225.U) {
        /*
        $64U.8 = ($64U.6 + (3: anvil.PrinterIndex.U))
        goto .226
        */



        generalRegFiles(8.U) := (generalRegFiles(6.U) + 3.U(64.W))

        CP := 226.U
      }

      is(226.U) {
        /*
        $64U.9 = *(SP + (14: SP)) [unsigned, anvil.PrinterIndex.U, 8]  // $64U.9 = mask
        goto .227
        */


        val __tmp_7167 = ((SP + 14.U(16.W))).asUInt
        generalRegFiles(9.U) := Cat(
          arrayRegFiles(__tmp_7167 + 7.U),
          arrayRegFiles(__tmp_7167 + 6.U),
          arrayRegFiles(__tmp_7167 + 5.U),
          arrayRegFiles(__tmp_7167 + 4.U),
          arrayRegFiles(__tmp_7167 + 3.U),
          arrayRegFiles(__tmp_7167 + 2.U),
          arrayRegFiles(__tmp_7167 + 1.U),
          arrayRegFiles(__tmp_7167 + 0.U)
        ).asUInt

        CP := 227.U
      }

      is(227.U) {
        /*
        $64U.10 = ($64U.8 & $64U.9)
        goto .228
        */



        generalRegFiles(10.U) := (generalRegFiles(8.U) & generalRegFiles(9.U))

        CP := 228.U
      }

      is(228.U) {
        /*
        *(($16U.3 + (12: SP)) + ($64U.10 as SP)) = (50: U8) [unsigned, U8, 1]  // $16U.3($64U.10) = (50: U8)
        goto .229
        */


        val __tmp_7168 = ((generalRegFiles(3.U) + 12.U(16.W)) + generalRegFiles(10.U).asUInt)
        val __tmp_7169 = (50.U(8.W)).asUInt
        arrayRegFiles(__tmp_7168 + 0.U) := __tmp_7169(7, 0)

        CP := 229.U
      }

      is(229.U) {
        /*
        $16U.3 = *(SP + (4: SP)) [unsigned, SP, 2]  // $16U.3 = buffer
        $64U.6 = *(SP + (6: SP)) [unsigned, anvil.PrinterIndex.U, 8]  // $64U.6 = index
        goto .230
        */


        val __tmp_7170 = ((SP + 4.U(16.W))).asUInt
        generalRegFiles(3.U) := Cat(
          arrayRegFiles(__tmp_7170 + 1.U),
          arrayRegFiles(__tmp_7170 + 0.U)
        ).asUInt

        val __tmp_7171 = ((SP + 6.U(16.W))).asUInt
        generalRegFiles(6.U) := Cat(
          arrayRegFiles(__tmp_7171 + 7.U),
          arrayRegFiles(__tmp_7171 + 6.U),
          arrayRegFiles(__tmp_7171 + 5.U),
          arrayRegFiles(__tmp_7171 + 4.U),
          arrayRegFiles(__tmp_7171 + 3.U),
          arrayRegFiles(__tmp_7171 + 2.U),
          arrayRegFiles(__tmp_7171 + 1.U),
          arrayRegFiles(__tmp_7171 + 0.U)
        ).asUInt

        CP := 230.U
      }

      is(230.U) {
        /*
        $64U.8 = ($64U.6 + (4: anvil.PrinterIndex.U))
        goto .231
        */



        generalRegFiles(8.U) := (generalRegFiles(6.U) + 4.U(64.W))

        CP := 231.U
      }

      is(231.U) {
        /*
        $64U.9 = *(SP + (14: SP)) [unsigned, anvil.PrinterIndex.U, 8]  // $64U.9 = mask
        goto .232
        */


        val __tmp_7172 = ((SP + 14.U(16.W))).asUInt
        generalRegFiles(9.U) := Cat(
          arrayRegFiles(__tmp_7172 + 7.U),
          arrayRegFiles(__tmp_7172 + 6.U),
          arrayRegFiles(__tmp_7172 + 5.U),
          arrayRegFiles(__tmp_7172 + 4.U),
          arrayRegFiles(__tmp_7172 + 3.U),
          arrayRegFiles(__tmp_7172 + 2.U),
          arrayRegFiles(__tmp_7172 + 1.U),
          arrayRegFiles(__tmp_7172 + 0.U)
        ).asUInt

        CP := 232.U
      }

      is(232.U) {
        /*
        $64U.10 = ($64U.8 & $64U.9)
        goto .233
        */



        generalRegFiles(10.U) := (generalRegFiles(8.U) & generalRegFiles(9.U))

        CP := 233.U
      }

      is(233.U) {
        /*
        *(($16U.3 + (12: SP)) + ($64U.10 as SP)) = (51: U8) [unsigned, U8, 1]  // $16U.3($64U.10) = (51: U8)
        goto .234
        */


        val __tmp_7173 = ((generalRegFiles(3.U) + 12.U(16.W)) + generalRegFiles(10.U).asUInt)
        val __tmp_7174 = (51.U(8.W)).asUInt
        arrayRegFiles(__tmp_7173 + 0.U) := __tmp_7174(7, 0)

        CP := 234.U
      }

      is(234.U) {
        /*
        $16U.3 = *(SP + (4: SP)) [unsigned, SP, 2]  // $16U.3 = buffer
        $64U.6 = *(SP + (6: SP)) [unsigned, anvil.PrinterIndex.U, 8]  // $64U.6 = index
        goto .235
        */


        val __tmp_7175 = ((SP + 4.U(16.W))).asUInt
        generalRegFiles(3.U) := Cat(
          arrayRegFiles(__tmp_7175 + 1.U),
          arrayRegFiles(__tmp_7175 + 0.U)
        ).asUInt

        val __tmp_7176 = ((SP + 6.U(16.W))).asUInt
        generalRegFiles(6.U) := Cat(
          arrayRegFiles(__tmp_7176 + 7.U),
          arrayRegFiles(__tmp_7176 + 6.U),
          arrayRegFiles(__tmp_7176 + 5.U),
          arrayRegFiles(__tmp_7176 + 4.U),
          arrayRegFiles(__tmp_7176 + 3.U),
          arrayRegFiles(__tmp_7176 + 2.U),
          arrayRegFiles(__tmp_7176 + 1.U),
          arrayRegFiles(__tmp_7176 + 0.U)
        ).asUInt

        CP := 235.U
      }

      is(235.U) {
        /*
        $64U.8 = ($64U.6 + (5: anvil.PrinterIndex.U))
        goto .236
        */



        generalRegFiles(8.U) := (generalRegFiles(6.U) + 5.U(64.W))

        CP := 236.U
      }

      is(236.U) {
        /*
        $64U.9 = *(SP + (14: SP)) [unsigned, anvil.PrinterIndex.U, 8]  // $64U.9 = mask
        goto .237
        */


        val __tmp_7177 = ((SP + 14.U(16.W))).asUInt
        generalRegFiles(9.U) := Cat(
          arrayRegFiles(__tmp_7177 + 7.U),
          arrayRegFiles(__tmp_7177 + 6.U),
          arrayRegFiles(__tmp_7177 + 5.U),
          arrayRegFiles(__tmp_7177 + 4.U),
          arrayRegFiles(__tmp_7177 + 3.U),
          arrayRegFiles(__tmp_7177 + 2.U),
          arrayRegFiles(__tmp_7177 + 1.U),
          arrayRegFiles(__tmp_7177 + 0.U)
        ).asUInt

        CP := 237.U
      }

      is(237.U) {
        /*
        $64U.10 = ($64U.8 & $64U.9)
        goto .238
        */



        generalRegFiles(10.U) := (generalRegFiles(8.U) & generalRegFiles(9.U))

        CP := 238.U
      }

      is(238.U) {
        /*
        *(($16U.3 + (12: SP)) + ($64U.10 as SP)) = (51: U8) [unsigned, U8, 1]  // $16U.3($64U.10) = (51: U8)
        goto .239
        */


        val __tmp_7178 = ((generalRegFiles(3.U) + 12.U(16.W)) + generalRegFiles(10.U).asUInt)
        val __tmp_7179 = (51.U(8.W)).asUInt
        arrayRegFiles(__tmp_7178 + 0.U) := __tmp_7179(7, 0)

        CP := 239.U
      }

      is(239.U) {
        /*
        $16U.3 = *(SP + (4: SP)) [unsigned, SP, 2]  // $16U.3 = buffer
        $64U.6 = *(SP + (6: SP)) [unsigned, anvil.PrinterIndex.U, 8]  // $64U.6 = index
        goto .240
        */


        val __tmp_7180 = ((SP + 4.U(16.W))).asUInt
        generalRegFiles(3.U) := Cat(
          arrayRegFiles(__tmp_7180 + 1.U),
          arrayRegFiles(__tmp_7180 + 0.U)
        ).asUInt

        val __tmp_7181 = ((SP + 6.U(16.W))).asUInt
        generalRegFiles(6.U) := Cat(
          arrayRegFiles(__tmp_7181 + 7.U),
          arrayRegFiles(__tmp_7181 + 6.U),
          arrayRegFiles(__tmp_7181 + 5.U),
          arrayRegFiles(__tmp_7181 + 4.U),
          arrayRegFiles(__tmp_7181 + 3.U),
          arrayRegFiles(__tmp_7181 + 2.U),
          arrayRegFiles(__tmp_7181 + 1.U),
          arrayRegFiles(__tmp_7181 + 0.U)
        ).asUInt

        CP := 240.U
      }

      is(240.U) {
        /*
        $64U.8 = ($64U.6 + (6: anvil.PrinterIndex.U))
        goto .241
        */



        generalRegFiles(8.U) := (generalRegFiles(6.U) + 6.U(64.W))

        CP := 241.U
      }

      is(241.U) {
        /*
        $64U.9 = *(SP + (14: SP)) [unsigned, anvil.PrinterIndex.U, 8]  // $64U.9 = mask
        goto .242
        */


        val __tmp_7182 = ((SP + 14.U(16.W))).asUInt
        generalRegFiles(9.U) := Cat(
          arrayRegFiles(__tmp_7182 + 7.U),
          arrayRegFiles(__tmp_7182 + 6.U),
          arrayRegFiles(__tmp_7182 + 5.U),
          arrayRegFiles(__tmp_7182 + 4.U),
          arrayRegFiles(__tmp_7182 + 3.U),
          arrayRegFiles(__tmp_7182 + 2.U),
          arrayRegFiles(__tmp_7182 + 1.U),
          arrayRegFiles(__tmp_7182 + 0.U)
        ).asUInt

        CP := 242.U
      }

      is(242.U) {
        /*
        $64U.10 = ($64U.8 & $64U.9)
        goto .243
        */



        generalRegFiles(10.U) := (generalRegFiles(8.U) & generalRegFiles(9.U))

        CP := 243.U
      }

      is(243.U) {
        /*
        *(($16U.3 + (12: SP)) + ($64U.10 as SP)) = (55: U8) [unsigned, U8, 1]  // $16U.3($64U.10) = (55: U8)
        goto .244
        */


        val __tmp_7183 = ((generalRegFiles(3.U) + 12.U(16.W)) + generalRegFiles(10.U).asUInt)
        val __tmp_7184 = (55.U(8.W)).asUInt
        arrayRegFiles(__tmp_7183 + 0.U) := __tmp_7184(7, 0)

        CP := 244.U
      }

      is(244.U) {
        /*
        $16U.3 = *(SP + (4: SP)) [unsigned, SP, 2]  // $16U.3 = buffer
        $64U.6 = *(SP + (6: SP)) [unsigned, anvil.PrinterIndex.U, 8]  // $64U.6 = index
        goto .245
        */


        val __tmp_7185 = ((SP + 4.U(16.W))).asUInt
        generalRegFiles(3.U) := Cat(
          arrayRegFiles(__tmp_7185 + 1.U),
          arrayRegFiles(__tmp_7185 + 0.U)
        ).asUInt

        val __tmp_7186 = ((SP + 6.U(16.W))).asUInt
        generalRegFiles(6.U) := Cat(
          arrayRegFiles(__tmp_7186 + 7.U),
          arrayRegFiles(__tmp_7186 + 6.U),
          arrayRegFiles(__tmp_7186 + 5.U),
          arrayRegFiles(__tmp_7186 + 4.U),
          arrayRegFiles(__tmp_7186 + 3.U),
          arrayRegFiles(__tmp_7186 + 2.U),
          arrayRegFiles(__tmp_7186 + 1.U),
          arrayRegFiles(__tmp_7186 + 0.U)
        ).asUInt

        CP := 245.U
      }

      is(245.U) {
        /*
        $64U.8 = ($64U.6 + (7: anvil.PrinterIndex.U))
        goto .246
        */



        generalRegFiles(8.U) := (generalRegFiles(6.U) + 7.U(64.W))

        CP := 246.U
      }

      is(246.U) {
        /*
        $64U.9 = *(SP + (14: SP)) [unsigned, anvil.PrinterIndex.U, 8]  // $64U.9 = mask
        goto .247
        */


        val __tmp_7187 = ((SP + 14.U(16.W))).asUInt
        generalRegFiles(9.U) := Cat(
          arrayRegFiles(__tmp_7187 + 7.U),
          arrayRegFiles(__tmp_7187 + 6.U),
          arrayRegFiles(__tmp_7187 + 5.U),
          arrayRegFiles(__tmp_7187 + 4.U),
          arrayRegFiles(__tmp_7187 + 3.U),
          arrayRegFiles(__tmp_7187 + 2.U),
          arrayRegFiles(__tmp_7187 + 1.U),
          arrayRegFiles(__tmp_7187 + 0.U)
        ).asUInt

        CP := 247.U
      }

      is(247.U) {
        /*
        $64U.10 = ($64U.8 & $64U.9)
        goto .248
        */



        generalRegFiles(10.U) := (generalRegFiles(8.U) & generalRegFiles(9.U))

        CP := 248.U
      }

      is(248.U) {
        /*
        *(($16U.3 + (12: SP)) + ($64U.10 as SP)) = (50: U8) [unsigned, U8, 1]  // $16U.3($64U.10) = (50: U8)
        goto .249
        */


        val __tmp_7188 = ((generalRegFiles(3.U) + 12.U(16.W)) + generalRegFiles(10.U).asUInt)
        val __tmp_7189 = (50.U(8.W)).asUInt
        arrayRegFiles(__tmp_7188 + 0.U) := __tmp_7189(7, 0)

        CP := 249.U
      }

      is(249.U) {
        /*
        $16U.3 = *(SP + (4: SP)) [unsigned, SP, 2]  // $16U.3 = buffer
        $64U.6 = *(SP + (6: SP)) [unsigned, anvil.PrinterIndex.U, 8]  // $64U.6 = index
        goto .250
        */


        val __tmp_7190 = ((SP + 4.U(16.W))).asUInt
        generalRegFiles(3.U) := Cat(
          arrayRegFiles(__tmp_7190 + 1.U),
          arrayRegFiles(__tmp_7190 + 0.U)
        ).asUInt

        val __tmp_7191 = ((SP + 6.U(16.W))).asUInt
        generalRegFiles(6.U) := Cat(
          arrayRegFiles(__tmp_7191 + 7.U),
          arrayRegFiles(__tmp_7191 + 6.U),
          arrayRegFiles(__tmp_7191 + 5.U),
          arrayRegFiles(__tmp_7191 + 4.U),
          arrayRegFiles(__tmp_7191 + 3.U),
          arrayRegFiles(__tmp_7191 + 2.U),
          arrayRegFiles(__tmp_7191 + 1.U),
          arrayRegFiles(__tmp_7191 + 0.U)
        ).asUInt

        CP := 250.U
      }

      is(250.U) {
        /*
        $64U.8 = ($64U.6 + (8: anvil.PrinterIndex.U))
        goto .251
        */



        generalRegFiles(8.U) := (generalRegFiles(6.U) + 8.U(64.W))

        CP := 251.U
      }

      is(251.U) {
        /*
        $64U.9 = *(SP + (14: SP)) [unsigned, anvil.PrinterIndex.U, 8]  // $64U.9 = mask
        goto .252
        */


        val __tmp_7192 = ((SP + 14.U(16.W))).asUInt
        generalRegFiles(9.U) := Cat(
          arrayRegFiles(__tmp_7192 + 7.U),
          arrayRegFiles(__tmp_7192 + 6.U),
          arrayRegFiles(__tmp_7192 + 5.U),
          arrayRegFiles(__tmp_7192 + 4.U),
          arrayRegFiles(__tmp_7192 + 3.U),
          arrayRegFiles(__tmp_7192 + 2.U),
          arrayRegFiles(__tmp_7192 + 1.U),
          arrayRegFiles(__tmp_7192 + 0.U)
        ).asUInt

        CP := 252.U
      }

      is(252.U) {
        /*
        $64U.10 = ($64U.8 & $64U.9)
        goto .253
        */



        generalRegFiles(10.U) := (generalRegFiles(8.U) & generalRegFiles(9.U))

        CP := 253.U
      }

      is(253.U) {
        /*
        *(($16U.3 + (12: SP)) + ($64U.10 as SP)) = (48: U8) [unsigned, U8, 1]  // $16U.3($64U.10) = (48: U8)
        goto .254
        */


        val __tmp_7193 = ((generalRegFiles(3.U) + 12.U(16.W)) + generalRegFiles(10.U).asUInt)
        val __tmp_7194 = (48.U(8.W)).asUInt
        arrayRegFiles(__tmp_7193 + 0.U) := __tmp_7194(7, 0)

        CP := 254.U
      }

      is(254.U) {
        /*
        $16U.3 = *(SP + (4: SP)) [unsigned, SP, 2]  // $16U.3 = buffer
        $64U.6 = *(SP + (6: SP)) [unsigned, anvil.PrinterIndex.U, 8]  // $64U.6 = index
        goto .255
        */


        val __tmp_7195 = ((SP + 4.U(16.W))).asUInt
        generalRegFiles(3.U) := Cat(
          arrayRegFiles(__tmp_7195 + 1.U),
          arrayRegFiles(__tmp_7195 + 0.U)
        ).asUInt

        val __tmp_7196 = ((SP + 6.U(16.W))).asUInt
        generalRegFiles(6.U) := Cat(
          arrayRegFiles(__tmp_7196 + 7.U),
          arrayRegFiles(__tmp_7196 + 6.U),
          arrayRegFiles(__tmp_7196 + 5.U),
          arrayRegFiles(__tmp_7196 + 4.U),
          arrayRegFiles(__tmp_7196 + 3.U),
          arrayRegFiles(__tmp_7196 + 2.U),
          arrayRegFiles(__tmp_7196 + 1.U),
          arrayRegFiles(__tmp_7196 + 0.U)
        ).asUInt

        CP := 255.U
      }

      is(255.U) {
        /*
        $64U.8 = ($64U.6 + (9: anvil.PrinterIndex.U))
        goto .256
        */



        generalRegFiles(8.U) := (generalRegFiles(6.U) + 9.U(64.W))

        CP := 256.U
      }

      is(256.U) {
        /*
        $64U.9 = *(SP + (14: SP)) [unsigned, anvil.PrinterIndex.U, 8]  // $64U.9 = mask
        goto .257
        */


        val __tmp_7197 = ((SP + 14.U(16.W))).asUInt
        generalRegFiles(9.U) := Cat(
          arrayRegFiles(__tmp_7197 + 7.U),
          arrayRegFiles(__tmp_7197 + 6.U),
          arrayRegFiles(__tmp_7197 + 5.U),
          arrayRegFiles(__tmp_7197 + 4.U),
          arrayRegFiles(__tmp_7197 + 3.U),
          arrayRegFiles(__tmp_7197 + 2.U),
          arrayRegFiles(__tmp_7197 + 1.U),
          arrayRegFiles(__tmp_7197 + 0.U)
        ).asUInt

        CP := 257.U
      }

      is(257.U) {
        /*
        $64U.10 = ($64U.8 & $64U.9)
        goto .258
        */



        generalRegFiles(10.U) := (generalRegFiles(8.U) & generalRegFiles(9.U))

        CP := 258.U
      }

      is(258.U) {
        /*
        *(($16U.3 + (12: SP)) + ($64U.10 as SP)) = (51: U8) [unsigned, U8, 1]  // $16U.3($64U.10) = (51: U8)
        goto .259
        */


        val __tmp_7198 = ((generalRegFiles(3.U) + 12.U(16.W)) + generalRegFiles(10.U).asUInt)
        val __tmp_7199 = (51.U(8.W)).asUInt
        arrayRegFiles(__tmp_7198 + 0.U) := __tmp_7199(7, 0)

        CP := 259.U
      }

      is(259.U) {
        /*
        $16U.3 = *(SP + (4: SP)) [unsigned, SP, 2]  // $16U.3 = buffer
        $64U.6 = *(SP + (6: SP)) [unsigned, anvil.PrinterIndex.U, 8]  // $64U.6 = index
        goto .260
        */


        val __tmp_7200 = ((SP + 4.U(16.W))).asUInt
        generalRegFiles(3.U) := Cat(
          arrayRegFiles(__tmp_7200 + 1.U),
          arrayRegFiles(__tmp_7200 + 0.U)
        ).asUInt

        val __tmp_7201 = ((SP + 6.U(16.W))).asUInt
        generalRegFiles(6.U) := Cat(
          arrayRegFiles(__tmp_7201 + 7.U),
          arrayRegFiles(__tmp_7201 + 6.U),
          arrayRegFiles(__tmp_7201 + 5.U),
          arrayRegFiles(__tmp_7201 + 4.U),
          arrayRegFiles(__tmp_7201 + 3.U),
          arrayRegFiles(__tmp_7201 + 2.U),
          arrayRegFiles(__tmp_7201 + 1.U),
          arrayRegFiles(__tmp_7201 + 0.U)
        ).asUInt

        CP := 260.U
      }

      is(260.U) {
        /*
        $64U.8 = ($64U.6 + (10: anvil.PrinterIndex.U))
        goto .261
        */



        generalRegFiles(8.U) := (generalRegFiles(6.U) + 10.U(64.W))

        CP := 261.U
      }

      is(261.U) {
        /*
        $64U.9 = *(SP + (14: SP)) [unsigned, anvil.PrinterIndex.U, 8]  // $64U.9 = mask
        goto .262
        */


        val __tmp_7202 = ((SP + 14.U(16.W))).asUInt
        generalRegFiles(9.U) := Cat(
          arrayRegFiles(__tmp_7202 + 7.U),
          arrayRegFiles(__tmp_7202 + 6.U),
          arrayRegFiles(__tmp_7202 + 5.U),
          arrayRegFiles(__tmp_7202 + 4.U),
          arrayRegFiles(__tmp_7202 + 3.U),
          arrayRegFiles(__tmp_7202 + 2.U),
          arrayRegFiles(__tmp_7202 + 1.U),
          arrayRegFiles(__tmp_7202 + 0.U)
        ).asUInt

        CP := 262.U
      }

      is(262.U) {
        /*
        $64U.10 = ($64U.8 & $64U.9)
        goto .263
        */



        generalRegFiles(10.U) := (generalRegFiles(8.U) & generalRegFiles(9.U))

        CP := 263.U
      }

      is(263.U) {
        /*
        *(($16U.3 + (12: SP)) + ($64U.10 as SP)) = (54: U8) [unsigned, U8, 1]  // $16U.3($64U.10) = (54: U8)
        goto .264
        */


        val __tmp_7203 = ((generalRegFiles(3.U) + 12.U(16.W)) + generalRegFiles(10.U).asUInt)
        val __tmp_7204 = (54.U(8.W)).asUInt
        arrayRegFiles(__tmp_7203 + 0.U) := __tmp_7204(7, 0)

        CP := 264.U
      }

      is(264.U) {
        /*
        $16U.3 = *(SP + (4: SP)) [unsigned, SP, 2]  // $16U.3 = buffer
        $64U.6 = *(SP + (6: SP)) [unsigned, anvil.PrinterIndex.U, 8]  // $64U.6 = index
        goto .265
        */


        val __tmp_7205 = ((SP + 4.U(16.W))).asUInt
        generalRegFiles(3.U) := Cat(
          arrayRegFiles(__tmp_7205 + 1.U),
          arrayRegFiles(__tmp_7205 + 0.U)
        ).asUInt

        val __tmp_7206 = ((SP + 6.U(16.W))).asUInt
        generalRegFiles(6.U) := Cat(
          arrayRegFiles(__tmp_7206 + 7.U),
          arrayRegFiles(__tmp_7206 + 6.U),
          arrayRegFiles(__tmp_7206 + 5.U),
          arrayRegFiles(__tmp_7206 + 4.U),
          arrayRegFiles(__tmp_7206 + 3.U),
          arrayRegFiles(__tmp_7206 + 2.U),
          arrayRegFiles(__tmp_7206 + 1.U),
          arrayRegFiles(__tmp_7206 + 0.U)
        ).asUInt

        CP := 265.U
      }

      is(265.U) {
        /*
        $64U.8 = ($64U.6 + (11: anvil.PrinterIndex.U))
        goto .266
        */



        generalRegFiles(8.U) := (generalRegFiles(6.U) + 11.U(64.W))

        CP := 266.U
      }

      is(266.U) {
        /*
        $64U.9 = *(SP + (14: SP)) [unsigned, anvil.PrinterIndex.U, 8]  // $64U.9 = mask
        goto .267
        */


        val __tmp_7207 = ((SP + 14.U(16.W))).asUInt
        generalRegFiles(9.U) := Cat(
          arrayRegFiles(__tmp_7207 + 7.U),
          arrayRegFiles(__tmp_7207 + 6.U),
          arrayRegFiles(__tmp_7207 + 5.U),
          arrayRegFiles(__tmp_7207 + 4.U),
          arrayRegFiles(__tmp_7207 + 3.U),
          arrayRegFiles(__tmp_7207 + 2.U),
          arrayRegFiles(__tmp_7207 + 1.U),
          arrayRegFiles(__tmp_7207 + 0.U)
        ).asUInt

        CP := 267.U
      }

      is(267.U) {
        /*
        $64U.10 = ($64U.8 & $64U.9)
        goto .268
        */



        generalRegFiles(10.U) := (generalRegFiles(8.U) & generalRegFiles(9.U))

        CP := 268.U
      }

      is(268.U) {
        /*
        *(($16U.3 + (12: SP)) + ($64U.10 as SP)) = (56: U8) [unsigned, U8, 1]  // $16U.3($64U.10) = (56: U8)
        goto .269
        */


        val __tmp_7208 = ((generalRegFiles(3.U) + 12.U(16.W)) + generalRegFiles(10.U).asUInt)
        val __tmp_7209 = (56.U(8.W)).asUInt
        arrayRegFiles(__tmp_7208 + 0.U) := __tmp_7209(7, 0)

        CP := 269.U
      }

      is(269.U) {
        /*
        $16U.3 = *(SP + (4: SP)) [unsigned, SP, 2]  // $16U.3 = buffer
        $64U.6 = *(SP + (6: SP)) [unsigned, anvil.PrinterIndex.U, 8]  // $64U.6 = index
        goto .270
        */


        val __tmp_7210 = ((SP + 4.U(16.W))).asUInt
        generalRegFiles(3.U) := Cat(
          arrayRegFiles(__tmp_7210 + 1.U),
          arrayRegFiles(__tmp_7210 + 0.U)
        ).asUInt

        val __tmp_7211 = ((SP + 6.U(16.W))).asUInt
        generalRegFiles(6.U) := Cat(
          arrayRegFiles(__tmp_7211 + 7.U),
          arrayRegFiles(__tmp_7211 + 6.U),
          arrayRegFiles(__tmp_7211 + 5.U),
          arrayRegFiles(__tmp_7211 + 4.U),
          arrayRegFiles(__tmp_7211 + 3.U),
          arrayRegFiles(__tmp_7211 + 2.U),
          arrayRegFiles(__tmp_7211 + 1.U),
          arrayRegFiles(__tmp_7211 + 0.U)
        ).asUInt

        CP := 270.U
      }

      is(270.U) {
        /*
        $64U.8 = ($64U.6 + (12: anvil.PrinterIndex.U))
        goto .271
        */



        generalRegFiles(8.U) := (generalRegFiles(6.U) + 12.U(64.W))

        CP := 271.U
      }

      is(271.U) {
        /*
        $64U.9 = *(SP + (14: SP)) [unsigned, anvil.PrinterIndex.U, 8]  // $64U.9 = mask
        goto .272
        */


        val __tmp_7212 = ((SP + 14.U(16.W))).asUInt
        generalRegFiles(9.U) := Cat(
          arrayRegFiles(__tmp_7212 + 7.U),
          arrayRegFiles(__tmp_7212 + 6.U),
          arrayRegFiles(__tmp_7212 + 5.U),
          arrayRegFiles(__tmp_7212 + 4.U),
          arrayRegFiles(__tmp_7212 + 3.U),
          arrayRegFiles(__tmp_7212 + 2.U),
          arrayRegFiles(__tmp_7212 + 1.U),
          arrayRegFiles(__tmp_7212 + 0.U)
        ).asUInt

        CP := 272.U
      }

      is(272.U) {
        /*
        $64U.10 = ($64U.8 & $64U.9)
        goto .273
        */



        generalRegFiles(10.U) := (generalRegFiles(8.U) & generalRegFiles(9.U))

        CP := 273.U
      }

      is(273.U) {
        /*
        *(($16U.3 + (12: SP)) + ($64U.10 as SP)) = (53: U8) [unsigned, U8, 1]  // $16U.3($64U.10) = (53: U8)
        goto .274
        */


        val __tmp_7213 = ((generalRegFiles(3.U) + 12.U(16.W)) + generalRegFiles(10.U).asUInt)
        val __tmp_7214 = (53.U(8.W)).asUInt
        arrayRegFiles(__tmp_7213 + 0.U) := __tmp_7214(7, 0)

        CP := 274.U
      }

      is(274.U) {
        /*
        $16U.3 = *(SP + (4: SP)) [unsigned, SP, 2]  // $16U.3 = buffer
        $64U.6 = *(SP + (6: SP)) [unsigned, anvil.PrinterIndex.U, 8]  // $64U.6 = index
        goto .275
        */


        val __tmp_7215 = ((SP + 4.U(16.W))).asUInt
        generalRegFiles(3.U) := Cat(
          arrayRegFiles(__tmp_7215 + 1.U),
          arrayRegFiles(__tmp_7215 + 0.U)
        ).asUInt

        val __tmp_7216 = ((SP + 6.U(16.W))).asUInt
        generalRegFiles(6.U) := Cat(
          arrayRegFiles(__tmp_7216 + 7.U),
          arrayRegFiles(__tmp_7216 + 6.U),
          arrayRegFiles(__tmp_7216 + 5.U),
          arrayRegFiles(__tmp_7216 + 4.U),
          arrayRegFiles(__tmp_7216 + 3.U),
          arrayRegFiles(__tmp_7216 + 2.U),
          arrayRegFiles(__tmp_7216 + 1.U),
          arrayRegFiles(__tmp_7216 + 0.U)
        ).asUInt

        CP := 275.U
      }

      is(275.U) {
        /*
        $64U.8 = ($64U.6 + (13: anvil.PrinterIndex.U))
        goto .276
        */



        generalRegFiles(8.U) := (generalRegFiles(6.U) + 13.U(64.W))

        CP := 276.U
      }

      is(276.U) {
        /*
        $64U.9 = *(SP + (14: SP)) [unsigned, anvil.PrinterIndex.U, 8]  // $64U.9 = mask
        goto .277
        */


        val __tmp_7217 = ((SP + 14.U(16.W))).asUInt
        generalRegFiles(9.U) := Cat(
          arrayRegFiles(__tmp_7217 + 7.U),
          arrayRegFiles(__tmp_7217 + 6.U),
          arrayRegFiles(__tmp_7217 + 5.U),
          arrayRegFiles(__tmp_7217 + 4.U),
          arrayRegFiles(__tmp_7217 + 3.U),
          arrayRegFiles(__tmp_7217 + 2.U),
          arrayRegFiles(__tmp_7217 + 1.U),
          arrayRegFiles(__tmp_7217 + 0.U)
        ).asUInt

        CP := 277.U
      }

      is(277.U) {
        /*
        $64U.10 = ($64U.8 & $64U.9)
        goto .278
        */



        generalRegFiles(10.U) := (generalRegFiles(8.U) & generalRegFiles(9.U))

        CP := 278.U
      }

      is(278.U) {
        /*
        *(($16U.3 + (12: SP)) + ($64U.10 as SP)) = (52: U8) [unsigned, U8, 1]  // $16U.3($64U.10) = (52: U8)
        goto .279
        */


        val __tmp_7218 = ((generalRegFiles(3.U) + 12.U(16.W)) + generalRegFiles(10.U).asUInt)
        val __tmp_7219 = (52.U(8.W)).asUInt
        arrayRegFiles(__tmp_7218 + 0.U) := __tmp_7219(7, 0)

        CP := 279.U
      }

      is(279.U) {
        /*
        $16U.3 = *(SP + (4: SP)) [unsigned, SP, 2]  // $16U.3 = buffer
        $64U.6 = *(SP + (6: SP)) [unsigned, anvil.PrinterIndex.U, 8]  // $64U.6 = index
        goto .280
        */


        val __tmp_7220 = ((SP + 4.U(16.W))).asUInt
        generalRegFiles(3.U) := Cat(
          arrayRegFiles(__tmp_7220 + 1.U),
          arrayRegFiles(__tmp_7220 + 0.U)
        ).asUInt

        val __tmp_7221 = ((SP + 6.U(16.W))).asUInt
        generalRegFiles(6.U) := Cat(
          arrayRegFiles(__tmp_7221 + 7.U),
          arrayRegFiles(__tmp_7221 + 6.U),
          arrayRegFiles(__tmp_7221 + 5.U),
          arrayRegFiles(__tmp_7221 + 4.U),
          arrayRegFiles(__tmp_7221 + 3.U),
          arrayRegFiles(__tmp_7221 + 2.U),
          arrayRegFiles(__tmp_7221 + 1.U),
          arrayRegFiles(__tmp_7221 + 0.U)
        ).asUInt

        CP := 280.U
      }

      is(280.U) {
        /*
        $64U.8 = ($64U.6 + (14: anvil.PrinterIndex.U))
        goto .281
        */



        generalRegFiles(8.U) := (generalRegFiles(6.U) + 14.U(64.W))

        CP := 281.U
      }

      is(281.U) {
        /*
        $64U.9 = *(SP + (14: SP)) [unsigned, anvil.PrinterIndex.U, 8]  // $64U.9 = mask
        goto .282
        */


        val __tmp_7222 = ((SP + 14.U(16.W))).asUInt
        generalRegFiles(9.U) := Cat(
          arrayRegFiles(__tmp_7222 + 7.U),
          arrayRegFiles(__tmp_7222 + 6.U),
          arrayRegFiles(__tmp_7222 + 5.U),
          arrayRegFiles(__tmp_7222 + 4.U),
          arrayRegFiles(__tmp_7222 + 3.U),
          arrayRegFiles(__tmp_7222 + 2.U),
          arrayRegFiles(__tmp_7222 + 1.U),
          arrayRegFiles(__tmp_7222 + 0.U)
        ).asUInt

        CP := 282.U
      }

      is(282.U) {
        /*
        $64U.10 = ($64U.8 & $64U.9)
        goto .283
        */



        generalRegFiles(10.U) := (generalRegFiles(8.U) & generalRegFiles(9.U))

        CP := 283.U
      }

      is(283.U) {
        /*
        *(($16U.3 + (12: SP)) + ($64U.10 as SP)) = (55: U8) [unsigned, U8, 1]  // $16U.3($64U.10) = (55: U8)
        goto .284
        */


        val __tmp_7223 = ((generalRegFiles(3.U) + 12.U(16.W)) + generalRegFiles(10.U).asUInt)
        val __tmp_7224 = (55.U(8.W)).asUInt
        arrayRegFiles(__tmp_7223 + 0.U) := __tmp_7224(7, 0)

        CP := 284.U
      }

      is(284.U) {
        /*
        $16U.3 = *(SP + (4: SP)) [unsigned, SP, 2]  // $16U.3 = buffer
        $64U.6 = *(SP + (6: SP)) [unsigned, anvil.PrinterIndex.U, 8]  // $64U.6 = index
        goto .285
        */


        val __tmp_7225 = ((SP + 4.U(16.W))).asUInt
        generalRegFiles(3.U) := Cat(
          arrayRegFiles(__tmp_7225 + 1.U),
          arrayRegFiles(__tmp_7225 + 0.U)
        ).asUInt

        val __tmp_7226 = ((SP + 6.U(16.W))).asUInt
        generalRegFiles(6.U) := Cat(
          arrayRegFiles(__tmp_7226 + 7.U),
          arrayRegFiles(__tmp_7226 + 6.U),
          arrayRegFiles(__tmp_7226 + 5.U),
          arrayRegFiles(__tmp_7226 + 4.U),
          arrayRegFiles(__tmp_7226 + 3.U),
          arrayRegFiles(__tmp_7226 + 2.U),
          arrayRegFiles(__tmp_7226 + 1.U),
          arrayRegFiles(__tmp_7226 + 0.U)
        ).asUInt

        CP := 285.U
      }

      is(285.U) {
        /*
        $64U.8 = ($64U.6 + (15: anvil.PrinterIndex.U))
        goto .286
        */



        generalRegFiles(8.U) := (generalRegFiles(6.U) + 15.U(64.W))

        CP := 286.U
      }

      is(286.U) {
        /*
        $64U.9 = *(SP + (14: SP)) [unsigned, anvil.PrinterIndex.U, 8]  // $64U.9 = mask
        goto .287
        */


        val __tmp_7227 = ((SP + 14.U(16.W))).asUInt
        generalRegFiles(9.U) := Cat(
          arrayRegFiles(__tmp_7227 + 7.U),
          arrayRegFiles(__tmp_7227 + 6.U),
          arrayRegFiles(__tmp_7227 + 5.U),
          arrayRegFiles(__tmp_7227 + 4.U),
          arrayRegFiles(__tmp_7227 + 3.U),
          arrayRegFiles(__tmp_7227 + 2.U),
          arrayRegFiles(__tmp_7227 + 1.U),
          arrayRegFiles(__tmp_7227 + 0.U)
        ).asUInt

        CP := 287.U
      }

      is(287.U) {
        /*
        $64U.10 = ($64U.8 & $64U.9)
        goto .288
        */



        generalRegFiles(10.U) := (generalRegFiles(8.U) & generalRegFiles(9.U))

        CP := 288.U
      }

      is(288.U) {
        /*
        *(($16U.3 + (12: SP)) + ($64U.10 as SP)) = (55: U8) [unsigned, U8, 1]  // $16U.3($64U.10) = (55: U8)
        goto .289
        */


        val __tmp_7228 = ((generalRegFiles(3.U) + 12.U(16.W)) + generalRegFiles(10.U).asUInt)
        val __tmp_7229 = (55.U(8.W)).asUInt
        arrayRegFiles(__tmp_7228 + 0.U) := __tmp_7229(7, 0)

        CP := 289.U
      }

      is(289.U) {
        /*
        $16U.3 = *(SP + (4: SP)) [unsigned, SP, 2]  // $16U.3 = buffer
        $64U.6 = *(SP + (6: SP)) [unsigned, anvil.PrinterIndex.U, 8]  // $64U.6 = index
        goto .290
        */


        val __tmp_7230 = ((SP + 4.U(16.W))).asUInt
        generalRegFiles(3.U) := Cat(
          arrayRegFiles(__tmp_7230 + 1.U),
          arrayRegFiles(__tmp_7230 + 0.U)
        ).asUInt

        val __tmp_7231 = ((SP + 6.U(16.W))).asUInt
        generalRegFiles(6.U) := Cat(
          arrayRegFiles(__tmp_7231 + 7.U),
          arrayRegFiles(__tmp_7231 + 6.U),
          arrayRegFiles(__tmp_7231 + 5.U),
          arrayRegFiles(__tmp_7231 + 4.U),
          arrayRegFiles(__tmp_7231 + 3.U),
          arrayRegFiles(__tmp_7231 + 2.U),
          arrayRegFiles(__tmp_7231 + 1.U),
          arrayRegFiles(__tmp_7231 + 0.U)
        ).asUInt

        CP := 290.U
      }

      is(290.U) {
        /*
        $64U.8 = ($64U.6 + (16: anvil.PrinterIndex.U))
        goto .291
        */



        generalRegFiles(8.U) := (generalRegFiles(6.U) + 16.U(64.W))

        CP := 291.U
      }

      is(291.U) {
        /*
        $64U.9 = *(SP + (14: SP)) [unsigned, anvil.PrinterIndex.U, 8]  // $64U.9 = mask
        goto .292
        */


        val __tmp_7232 = ((SP + 14.U(16.W))).asUInt
        generalRegFiles(9.U) := Cat(
          arrayRegFiles(__tmp_7232 + 7.U),
          arrayRegFiles(__tmp_7232 + 6.U),
          arrayRegFiles(__tmp_7232 + 5.U),
          arrayRegFiles(__tmp_7232 + 4.U),
          arrayRegFiles(__tmp_7232 + 3.U),
          arrayRegFiles(__tmp_7232 + 2.U),
          arrayRegFiles(__tmp_7232 + 1.U),
          arrayRegFiles(__tmp_7232 + 0.U)
        ).asUInt

        CP := 292.U
      }

      is(292.U) {
        /*
        $64U.10 = ($64U.8 & $64U.9)
        goto .293
        */



        generalRegFiles(10.U) := (generalRegFiles(8.U) & generalRegFiles(9.U))

        CP := 293.U
      }

      is(293.U) {
        /*
        *(($16U.3 + (12: SP)) + ($64U.10 as SP)) = (53: U8) [unsigned, U8, 1]  // $16U.3($64U.10) = (53: U8)
        goto .294
        */


        val __tmp_7233 = ((generalRegFiles(3.U) + 12.U(16.W)) + generalRegFiles(10.U).asUInt)
        val __tmp_7234 = (53.U(8.W)).asUInt
        arrayRegFiles(__tmp_7233 + 0.U) := __tmp_7234(7, 0)

        CP := 294.U
      }

      is(294.U) {
        /*
        $16U.3 = *(SP + (4: SP)) [unsigned, SP, 2]  // $16U.3 = buffer
        $64U.6 = *(SP + (6: SP)) [unsigned, anvil.PrinterIndex.U, 8]  // $64U.6 = index
        goto .295
        */


        val __tmp_7235 = ((SP + 4.U(16.W))).asUInt
        generalRegFiles(3.U) := Cat(
          arrayRegFiles(__tmp_7235 + 1.U),
          arrayRegFiles(__tmp_7235 + 0.U)
        ).asUInt

        val __tmp_7236 = ((SP + 6.U(16.W))).asUInt
        generalRegFiles(6.U) := Cat(
          arrayRegFiles(__tmp_7236 + 7.U),
          arrayRegFiles(__tmp_7236 + 6.U),
          arrayRegFiles(__tmp_7236 + 5.U),
          arrayRegFiles(__tmp_7236 + 4.U),
          arrayRegFiles(__tmp_7236 + 3.U),
          arrayRegFiles(__tmp_7236 + 2.U),
          arrayRegFiles(__tmp_7236 + 1.U),
          arrayRegFiles(__tmp_7236 + 0.U)
        ).asUInt

        CP := 295.U
      }

      is(295.U) {
        /*
        $64U.8 = ($64U.6 + (17: anvil.PrinterIndex.U))
        goto .296
        */



        generalRegFiles(8.U) := (generalRegFiles(6.U) + 17.U(64.W))

        CP := 296.U
      }

      is(296.U) {
        /*
        $64U.9 = *(SP + (14: SP)) [unsigned, anvil.PrinterIndex.U, 8]  // $64U.9 = mask
        goto .297
        */


        val __tmp_7237 = ((SP + 14.U(16.W))).asUInt
        generalRegFiles(9.U) := Cat(
          arrayRegFiles(__tmp_7237 + 7.U),
          arrayRegFiles(__tmp_7237 + 6.U),
          arrayRegFiles(__tmp_7237 + 5.U),
          arrayRegFiles(__tmp_7237 + 4.U),
          arrayRegFiles(__tmp_7237 + 3.U),
          arrayRegFiles(__tmp_7237 + 2.U),
          arrayRegFiles(__tmp_7237 + 1.U),
          arrayRegFiles(__tmp_7237 + 0.U)
        ).asUInt

        CP := 297.U
      }

      is(297.U) {
        /*
        $64U.10 = ($64U.8 & $64U.9)
        goto .298
        */



        generalRegFiles(10.U) := (generalRegFiles(8.U) & generalRegFiles(9.U))

        CP := 298.U
      }

      is(298.U) {
        /*
        *(($16U.3 + (12: SP)) + ($64U.10 as SP)) = (56: U8) [unsigned, U8, 1]  // $16U.3($64U.10) = (56: U8)
        goto .299
        */


        val __tmp_7238 = ((generalRegFiles(3.U) + 12.U(16.W)) + generalRegFiles(10.U).asUInt)
        val __tmp_7239 = (56.U(8.W)).asUInt
        arrayRegFiles(__tmp_7238 + 0.U) := __tmp_7239(7, 0)

        CP := 299.U
      }

      is(299.U) {
        /*
        $16U.3 = *(SP + (4: SP)) [unsigned, SP, 2]  // $16U.3 = buffer
        $64U.6 = *(SP + (6: SP)) [unsigned, anvil.PrinterIndex.U, 8]  // $64U.6 = index
        goto .300
        */


        val __tmp_7240 = ((SP + 4.U(16.W))).asUInt
        generalRegFiles(3.U) := Cat(
          arrayRegFiles(__tmp_7240 + 1.U),
          arrayRegFiles(__tmp_7240 + 0.U)
        ).asUInt

        val __tmp_7241 = ((SP + 6.U(16.W))).asUInt
        generalRegFiles(6.U) := Cat(
          arrayRegFiles(__tmp_7241 + 7.U),
          arrayRegFiles(__tmp_7241 + 6.U),
          arrayRegFiles(__tmp_7241 + 5.U),
          arrayRegFiles(__tmp_7241 + 4.U),
          arrayRegFiles(__tmp_7241 + 3.U),
          arrayRegFiles(__tmp_7241 + 2.U),
          arrayRegFiles(__tmp_7241 + 1.U),
          arrayRegFiles(__tmp_7241 + 0.U)
        ).asUInt

        CP := 300.U
      }

      is(300.U) {
        /*
        $64U.8 = ($64U.6 + (18: anvil.PrinterIndex.U))
        goto .301
        */



        generalRegFiles(8.U) := (generalRegFiles(6.U) + 18.U(64.W))

        CP := 301.U
      }

      is(301.U) {
        /*
        $64U.9 = *(SP + (14: SP)) [unsigned, anvil.PrinterIndex.U, 8]  // $64U.9 = mask
        goto .302
        */


        val __tmp_7242 = ((SP + 14.U(16.W))).asUInt
        generalRegFiles(9.U) := Cat(
          arrayRegFiles(__tmp_7242 + 7.U),
          arrayRegFiles(__tmp_7242 + 6.U),
          arrayRegFiles(__tmp_7242 + 5.U),
          arrayRegFiles(__tmp_7242 + 4.U),
          arrayRegFiles(__tmp_7242 + 3.U),
          arrayRegFiles(__tmp_7242 + 2.U),
          arrayRegFiles(__tmp_7242 + 1.U),
          arrayRegFiles(__tmp_7242 + 0.U)
        ).asUInt

        CP := 302.U
      }

      is(302.U) {
        /*
        $64U.10 = ($64U.8 & $64U.9)
        goto .303
        */



        generalRegFiles(10.U) := (generalRegFiles(8.U) & generalRegFiles(9.U))

        CP := 303.U
      }

      is(303.U) {
        /*
        *(($16U.3 + (12: SP)) + ($64U.10 as SP)) = (48: U8) [unsigned, U8, 1]  // $16U.3($64U.10) = (48: U8)
        goto .304
        */


        val __tmp_7243 = ((generalRegFiles(3.U) + 12.U(16.W)) + generalRegFiles(10.U).asUInt)
        val __tmp_7244 = (48.U(8.W)).asUInt
        arrayRegFiles(__tmp_7243 + 0.U) := __tmp_7244(7, 0)

        CP := 304.U
      }

      is(304.U) {
        /*
        $16U.3 = *(SP + (4: SP)) [unsigned, SP, 2]  // $16U.3 = buffer
        $64U.6 = *(SP + (6: SP)) [unsigned, anvil.PrinterIndex.U, 8]  // $64U.6 = index
        goto .305
        */


        val __tmp_7245 = ((SP + 4.U(16.W))).asUInt
        generalRegFiles(3.U) := Cat(
          arrayRegFiles(__tmp_7245 + 1.U),
          arrayRegFiles(__tmp_7245 + 0.U)
        ).asUInt

        val __tmp_7246 = ((SP + 6.U(16.W))).asUInt
        generalRegFiles(6.U) := Cat(
          arrayRegFiles(__tmp_7246 + 7.U),
          arrayRegFiles(__tmp_7246 + 6.U),
          arrayRegFiles(__tmp_7246 + 5.U),
          arrayRegFiles(__tmp_7246 + 4.U),
          arrayRegFiles(__tmp_7246 + 3.U),
          arrayRegFiles(__tmp_7246 + 2.U),
          arrayRegFiles(__tmp_7246 + 1.U),
          arrayRegFiles(__tmp_7246 + 0.U)
        ).asUInt

        CP := 305.U
      }

      is(305.U) {
        /*
        $64U.8 = ($64U.6 + (19: anvil.PrinterIndex.U))
        goto .306
        */



        generalRegFiles(8.U) := (generalRegFiles(6.U) + 19.U(64.W))

        CP := 306.U
      }

      is(306.U) {
        /*
        $64U.9 = *(SP + (14: SP)) [unsigned, anvil.PrinterIndex.U, 8]  // $64U.9 = mask
        goto .307
        */


        val __tmp_7247 = ((SP + 14.U(16.W))).asUInt
        generalRegFiles(9.U) := Cat(
          arrayRegFiles(__tmp_7247 + 7.U),
          arrayRegFiles(__tmp_7247 + 6.U),
          arrayRegFiles(__tmp_7247 + 5.U),
          arrayRegFiles(__tmp_7247 + 4.U),
          arrayRegFiles(__tmp_7247 + 3.U),
          arrayRegFiles(__tmp_7247 + 2.U),
          arrayRegFiles(__tmp_7247 + 1.U),
          arrayRegFiles(__tmp_7247 + 0.U)
        ).asUInt

        CP := 307.U
      }

      is(307.U) {
        /*
        $64U.10 = ($64U.8 & $64U.9)
        goto .308
        */



        generalRegFiles(10.U) := (generalRegFiles(8.U) & generalRegFiles(9.U))

        CP := 308.U
      }

      is(308.U) {
        /*
        *(($16U.3 + (12: SP)) + ($64U.10 as SP)) = (56: U8) [unsigned, U8, 1]  // $16U.3($64U.10) = (56: U8)
        goto .309
        */


        val __tmp_7248 = ((generalRegFiles(3.U) + 12.U(16.W)) + generalRegFiles(10.U).asUInt)
        val __tmp_7249 = (56.U(8.W)).asUInt
        arrayRegFiles(__tmp_7248 + 0.U) := __tmp_7249(7, 0)

        CP := 309.U
      }

      is(309.U) {
        /*
        **(SP + (2: SP)) = (20: U64) [unsigned, U64, 8]  // $res = (20: U64)
        goto $ret@0
        */


        val __tmp_7250 = Cat(
          arrayRegFiles((SP + 2.U(16.W)) + 1.U),
          arrayRegFiles((SP + 2.U(16.W)) + 0.U)
        )
        val __tmp_7251 = (20.U(64.W)).asUInt
        arrayRegFiles(__tmp_7250 + 0.U) := __tmp_7251(7, 0)
        arrayRegFiles(__tmp_7250 + 1.U) := __tmp_7251(15, 8)
        arrayRegFiles(__tmp_7250 + 2.U) := __tmp_7251(23, 16)
        arrayRegFiles(__tmp_7250 + 3.U) := __tmp_7251(31, 24)
        arrayRegFiles(__tmp_7250 + 4.U) := __tmp_7251(39, 32)
        arrayRegFiles(__tmp_7250 + 5.U) := __tmp_7251(47, 40)
        arrayRegFiles(__tmp_7250 + 6.U) := __tmp_7251(55, 48)
        arrayRegFiles(__tmp_7250 + 7.U) := __tmp_7251(63, 56)

        CP := Cat(
          arrayRegFiles(SP + 0.U + 1.U),
          arrayRegFiles(SP + 0.U + 0.U)
        )

      }

      is(310.U) {
        /*
        $64S.2 = *(SP + (22: SP)) [signed, S64, 8]  // $64S.2 = n
        goto .311
        */


        val __tmp_7252 = ((SP + 22.U(16.W))).asUInt
        generalRegFiles(2.U) := Cat(
          arrayRegFiles(__tmp_7252 + 7.U),
          arrayRegFiles(__tmp_7252 + 6.U),
          arrayRegFiles(__tmp_7252 + 5.U),
          arrayRegFiles(__tmp_7252 + 4.U),
          arrayRegFiles(__tmp_7252 + 3.U),
          arrayRegFiles(__tmp_7252 + 2.U),
          arrayRegFiles(__tmp_7252 + 1.U),
          arrayRegFiles(__tmp_7252 + 0.U)
        ).asSInt.pad(GENERAL_REG_WIDTH).asUInt

        CP := 311.U
      }

      is(311.U) {
        /*
        $1U.2 = ($64S.2 ≡ (0: S64))
        goto .312
        */



        generalRegFiles(2.U) := (generalRegFiles(2.U).asSInt === 0.S(64.W)).asUInt

        CP := 312.U
      }

      is(312.U) {
        /*
        if $1U.2 goto .313 else goto .317
        */


        CP := Mux((generalRegFiles(2.U).asUInt) === 1.U, 313.U, 317.U)
      }

      is(313.U) {
        /*
        $16U.3 = *(SP + (4: SP)) [unsigned, SP, 2]  // $16U.3 = buffer
        $64U.6 = *(SP + (6: SP)) [unsigned, anvil.PrinterIndex.U, 8]  // $64U.6 = index
        $64U.8 = *(SP + (14: SP)) [unsigned, anvil.PrinterIndex.U, 8]  // $64U.8 = mask
        goto .314
        */


        val __tmp_7253 = ((SP + 4.U(16.W))).asUInt
        generalRegFiles(3.U) := Cat(
          arrayRegFiles(__tmp_7253 + 1.U),
          arrayRegFiles(__tmp_7253 + 0.U)
        ).asUInt

        val __tmp_7254 = ((SP + 6.U(16.W))).asUInt
        generalRegFiles(6.U) := Cat(
          arrayRegFiles(__tmp_7254 + 7.U),
          arrayRegFiles(__tmp_7254 + 6.U),
          arrayRegFiles(__tmp_7254 + 5.U),
          arrayRegFiles(__tmp_7254 + 4.U),
          arrayRegFiles(__tmp_7254 + 3.U),
          arrayRegFiles(__tmp_7254 + 2.U),
          arrayRegFiles(__tmp_7254 + 1.U),
          arrayRegFiles(__tmp_7254 + 0.U)
        ).asUInt

        val __tmp_7255 = ((SP + 14.U(16.W))).asUInt
        generalRegFiles(8.U) := Cat(
          arrayRegFiles(__tmp_7255 + 7.U),
          arrayRegFiles(__tmp_7255 + 6.U),
          arrayRegFiles(__tmp_7255 + 5.U),
          arrayRegFiles(__tmp_7255 + 4.U),
          arrayRegFiles(__tmp_7255 + 3.U),
          arrayRegFiles(__tmp_7255 + 2.U),
          arrayRegFiles(__tmp_7255 + 1.U),
          arrayRegFiles(__tmp_7255 + 0.U)
        ).asUInt

        CP := 314.U
      }

      is(314.U) {
        /*
        $64U.9 = ($64U.6 & $64U.8)
        goto .315
        */



        generalRegFiles(9.U) := (generalRegFiles(6.U) & generalRegFiles(8.U))

        CP := 315.U
      }

      is(315.U) {
        /*
        *(($16U.3 + (12: SP)) + ($64U.9 as SP)) = (48: U8) [unsigned, U8, 1]  // $16U.3($64U.9) = (48: U8)
        goto .316
        */


        val __tmp_7256 = ((generalRegFiles(3.U) + 12.U(16.W)) + generalRegFiles(9.U).asUInt)
        val __tmp_7257 = (48.U(8.W)).asUInt
        arrayRegFiles(__tmp_7256 + 0.U) := __tmp_7257(7, 0)

        CP := 316.U
      }

      is(316.U) {
        /*
        **(SP + (2: SP)) = (1: U64) [unsigned, U64, 8]  // $res = (1: U64)
        goto $ret@0
        */


        val __tmp_7258 = Cat(
          arrayRegFiles((SP + 2.U(16.W)) + 1.U),
          arrayRegFiles((SP + 2.U(16.W)) + 0.U)
        )
        val __tmp_7259 = (1.U(64.W)).asUInt
        arrayRegFiles(__tmp_7258 + 0.U) := __tmp_7259(7, 0)
        arrayRegFiles(__tmp_7258 + 1.U) := __tmp_7259(15, 8)
        arrayRegFiles(__tmp_7258 + 2.U) := __tmp_7259(23, 16)
        arrayRegFiles(__tmp_7258 + 3.U) := __tmp_7259(31, 24)
        arrayRegFiles(__tmp_7258 + 4.U) := __tmp_7259(39, 32)
        arrayRegFiles(__tmp_7258 + 5.U) := __tmp_7259(47, 40)
        arrayRegFiles(__tmp_7258 + 6.U) := __tmp_7259(55, 48)
        arrayRegFiles(__tmp_7258 + 7.U) := __tmp_7259(63, 56)

        CP := Cat(
          arrayRegFiles(SP + 0.U + 1.U),
          arrayRegFiles(SP + 0.U + 0.U)
        )

      }

      is(317.U) {
        /*
        decl buff: MS[anvil.PrinterIndex.I20, U8] [@30, 34]
        alloc $new@[167,16].100A3CBA: MS[anvil.PrinterIndex.I20, U8] [@64, 34]
        $16U.4 = (SP + (64: SP))
        *(SP + (64: SP)) = (323602724: U32) [unsigned, U32, 4]  // sha3 type signature of MS[anvil.PrinterIndex.I20, U8]: 0x1349C924
        *(SP + (68: SP)) = (20: Z) [signed, Z, 8]  // size of MS[anvil.PrinterIndex.I20, U8]((0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8))
        goto .318
        */



        generalRegFiles(4.U) := (SP + 64.U(16.W))

        val __tmp_7260 = (SP + 64.U(16.W))
        val __tmp_7261 = (323602724.U(32.W)).asUInt
        arrayRegFiles(__tmp_7260 + 0.U) := __tmp_7261(7, 0)
        arrayRegFiles(__tmp_7260 + 1.U) := __tmp_7261(15, 8)
        arrayRegFiles(__tmp_7260 + 2.U) := __tmp_7261(23, 16)
        arrayRegFiles(__tmp_7260 + 3.U) := __tmp_7261(31, 24)

        val __tmp_7262 = (SP + 68.U(16.W))
        val __tmp_7263 = (20.S(64.W)).asUInt
        arrayRegFiles(__tmp_7262 + 0.U) := __tmp_7263(7, 0)
        arrayRegFiles(__tmp_7262 + 1.U) := __tmp_7263(15, 8)
        arrayRegFiles(__tmp_7262 + 2.U) := __tmp_7263(23, 16)
        arrayRegFiles(__tmp_7262 + 3.U) := __tmp_7263(31, 24)
        arrayRegFiles(__tmp_7262 + 4.U) := __tmp_7263(39, 32)
        arrayRegFiles(__tmp_7262 + 5.U) := __tmp_7263(47, 40)
        arrayRegFiles(__tmp_7262 + 6.U) := __tmp_7263(55, 48)
        arrayRegFiles(__tmp_7262 + 7.U) := __tmp_7263(63, 56)

        CP := 318.U
      }

      is(318.U) {
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
        goto .319
        */


        val __tmp_7264 = ((generalRegFiles(4.U) + 12.U(16.W)) + 0.S(8.W).asUInt)
        val __tmp_7265 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_7264 + 0.U) := __tmp_7265(7, 0)

        val __tmp_7266 = ((generalRegFiles(4.U) + 12.U(16.W)) + 1.S(8.W).asUInt)
        val __tmp_7267 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_7266 + 0.U) := __tmp_7267(7, 0)

        val __tmp_7268 = ((generalRegFiles(4.U) + 12.U(16.W)) + 2.S(8.W).asUInt)
        val __tmp_7269 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_7268 + 0.U) := __tmp_7269(7, 0)

        val __tmp_7270 = ((generalRegFiles(4.U) + 12.U(16.W)) + 3.S(8.W).asUInt)
        val __tmp_7271 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_7270 + 0.U) := __tmp_7271(7, 0)

        val __tmp_7272 = ((generalRegFiles(4.U) + 12.U(16.W)) + 4.S(8.W).asUInt)
        val __tmp_7273 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_7272 + 0.U) := __tmp_7273(7, 0)

        val __tmp_7274 = ((generalRegFiles(4.U) + 12.U(16.W)) + 5.S(8.W).asUInt)
        val __tmp_7275 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_7274 + 0.U) := __tmp_7275(7, 0)

        val __tmp_7276 = ((generalRegFiles(4.U) + 12.U(16.W)) + 6.S(8.W).asUInt)
        val __tmp_7277 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_7276 + 0.U) := __tmp_7277(7, 0)

        val __tmp_7278 = ((generalRegFiles(4.U) + 12.U(16.W)) + 7.S(8.W).asUInt)
        val __tmp_7279 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_7278 + 0.U) := __tmp_7279(7, 0)

        val __tmp_7280 = ((generalRegFiles(4.U) + 12.U(16.W)) + 8.S(8.W).asUInt)
        val __tmp_7281 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_7280 + 0.U) := __tmp_7281(7, 0)

        val __tmp_7282 = ((generalRegFiles(4.U) + 12.U(16.W)) + 9.S(8.W).asUInt)
        val __tmp_7283 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_7282 + 0.U) := __tmp_7283(7, 0)

        val __tmp_7284 = ((generalRegFiles(4.U) + 12.U(16.W)) + 10.S(8.W).asUInt)
        val __tmp_7285 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_7284 + 0.U) := __tmp_7285(7, 0)

        val __tmp_7286 = ((generalRegFiles(4.U) + 12.U(16.W)) + 11.S(8.W).asUInt)
        val __tmp_7287 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_7286 + 0.U) := __tmp_7287(7, 0)

        val __tmp_7288 = ((generalRegFiles(4.U) + 12.U(16.W)) + 12.S(8.W).asUInt)
        val __tmp_7289 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_7288 + 0.U) := __tmp_7289(7, 0)

        val __tmp_7290 = ((generalRegFiles(4.U) + 12.U(16.W)) + 13.S(8.W).asUInt)
        val __tmp_7291 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_7290 + 0.U) := __tmp_7291(7, 0)

        val __tmp_7292 = ((generalRegFiles(4.U) + 12.U(16.W)) + 14.S(8.W).asUInt)
        val __tmp_7293 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_7292 + 0.U) := __tmp_7293(7, 0)

        val __tmp_7294 = ((generalRegFiles(4.U) + 12.U(16.W)) + 15.S(8.W).asUInt)
        val __tmp_7295 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_7294 + 0.U) := __tmp_7295(7, 0)

        val __tmp_7296 = ((generalRegFiles(4.U) + 12.U(16.W)) + 16.S(8.W).asUInt)
        val __tmp_7297 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_7296 + 0.U) := __tmp_7297(7, 0)

        val __tmp_7298 = ((generalRegFiles(4.U) + 12.U(16.W)) + 17.S(8.W).asUInt)
        val __tmp_7299 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_7298 + 0.U) := __tmp_7299(7, 0)

        val __tmp_7300 = ((generalRegFiles(4.U) + 12.U(16.W)) + 18.S(8.W).asUInt)
        val __tmp_7301 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_7300 + 0.U) := __tmp_7301(7, 0)

        val __tmp_7302 = ((generalRegFiles(4.U) + 12.U(16.W)) + 19.S(8.W).asUInt)
        val __tmp_7303 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_7302 + 0.U) := __tmp_7303(7, 0)

        CP := 319.U
      }

      is(319.U) {
        /*
        (SP + (30: SP)) [MS[anvil.PrinterIndex.I20, U8], 34]  <-  $16U.4 [MS[anvil.PrinterIndex.I20, U8], ((*($16U.4 + (4: SP)) as SP) + (12: SP))]  // buff = $16U.4
        goto .320
        */


        val __tmp_7304 = (SP + 30.U(16.W))
        val __tmp_7305 = generalRegFiles(4.U)
        val __tmp_7306 = (Cat(
           arrayRegFiles((generalRegFiles(4.U) + 4.U(16.W)) + 7.U),
           arrayRegFiles((generalRegFiles(4.U) + 4.U(16.W)) + 6.U),
           arrayRegFiles((generalRegFiles(4.U) + 4.U(16.W)) + 5.U),
           arrayRegFiles((generalRegFiles(4.U) + 4.U(16.W)) + 4.U),
           arrayRegFiles((generalRegFiles(4.U) + 4.U(16.W)) + 3.U),
           arrayRegFiles((generalRegFiles(4.U) + 4.U(16.W)) + 2.U),
           arrayRegFiles((generalRegFiles(4.U) + 4.U(16.W)) + 1.U),
           arrayRegFiles((generalRegFiles(4.U) + 4.U(16.W)) + 0.U)
         ).asSInt.asUInt + 12.U(16.W))

        when(Idx <= __tmp_7306) {
          arrayRegFiles(__tmp_7304 + Idx + 0.U) := arrayRegFiles(__tmp_7305 + Idx + 0.U)
          arrayRegFiles(__tmp_7304 + Idx + 1.U) := arrayRegFiles(__tmp_7305 + Idx + 1.U)
          arrayRegFiles(__tmp_7304 + Idx + 2.U) := arrayRegFiles(__tmp_7305 + Idx + 2.U)
          arrayRegFiles(__tmp_7304 + Idx + 3.U) := arrayRegFiles(__tmp_7305 + Idx + 3.U)
          arrayRegFiles(__tmp_7304 + Idx + 4.U) := arrayRegFiles(__tmp_7305 + Idx + 4.U)
          arrayRegFiles(__tmp_7304 + Idx + 5.U) := arrayRegFiles(__tmp_7305 + Idx + 5.U)
          arrayRegFiles(__tmp_7304 + Idx + 6.U) := arrayRegFiles(__tmp_7305 + Idx + 6.U)
          arrayRegFiles(__tmp_7304 + Idx + 7.U) := arrayRegFiles(__tmp_7305 + Idx + 7.U)
          Idx := Idx + 8.U
          LeftByteRounds := __tmp_7306 - Idx
        } .elsewhen(IdxLeftByteRounds < LeftByteRounds) {
          val __tmp_7307 = Idx - 8.U
          arrayRegFiles(__tmp_7304 + __tmp_7307 + IdxLeftByteRounds) := arrayRegFiles(__tmp_7305 + __tmp_7307 + IdxLeftByteRounds)
          IdxLeftByteRounds := IdxLeftByteRounds + 1.U
        } .otherwise {
          Idx := 0.U
          IdxLeftByteRounds := 0.U
          LeftByteRounds := 0.U
          CP := 320.U
        }


      }

      is(320.U) {
        /*
        unalloc $new@[167,16].100A3CBA: MS[anvil.PrinterIndex.I20, U8] [@64, 34]
        goto .321
        */


        CP := 321.U
      }

      is(321.U) {
        /*
        decl i: anvil.PrinterIndex.I20 [@64, 1]
        *(SP + (64: SP)) = (0: anvil.PrinterIndex.I20) [signed, anvil.PrinterIndex.I20, 1]  // i = (0: anvil.PrinterIndex.I20)
        goto .322
        */


        val __tmp_7308 = (SP + 64.U(16.W))
        val __tmp_7309 = (0.S(8.W)).asUInt
        arrayRegFiles(__tmp_7308 + 0.U) := __tmp_7309(7, 0)

        CP := 322.U
      }

      is(322.U) {
        /*
        decl neg: B [@65, 1]
        $64S.2 = *(SP + (22: SP)) [signed, S64, 8]  // $64S.2 = n
        goto .323
        */


        val __tmp_7310 = ((SP + 22.U(16.W))).asUInt
        generalRegFiles(2.U) := Cat(
          arrayRegFiles(__tmp_7310 + 7.U),
          arrayRegFiles(__tmp_7310 + 6.U),
          arrayRegFiles(__tmp_7310 + 5.U),
          arrayRegFiles(__tmp_7310 + 4.U),
          arrayRegFiles(__tmp_7310 + 3.U),
          arrayRegFiles(__tmp_7310 + 2.U),
          arrayRegFiles(__tmp_7310 + 1.U),
          arrayRegFiles(__tmp_7310 + 0.U)
        ).asSInt.pad(GENERAL_REG_WIDTH).asUInt

        CP := 323.U
      }

      is(323.U) {
        /*
        $1U.2 = ($64S.2 < (0: S64))
        goto .324
        */



        generalRegFiles(2.U) := (generalRegFiles(2.U).asSInt < 0.S(64.W)).asUInt

        CP := 324.U
      }

      is(324.U) {
        /*
        *(SP + (65: SP)) = $1U.2 [signed, B, 1]  // neg = $1U.2
        goto .325
        */


        val __tmp_7311 = (SP + 65.U(16.W))
        val __tmp_7312 = (generalRegFiles(2.U)).asUInt
        arrayRegFiles(__tmp_7311 + 0.U) := __tmp_7312(7, 0)

        CP := 325.U
      }

      is(325.U) {
        /*
        decl m: S64 [@66, 8]
        $1U.1 = *(SP + (65: SP)) [unsigned, B, 1]  // $1U.1 = neg
        goto .326
        */


        val __tmp_7313 = ((SP + 65.U(16.W))).asUInt
        generalRegFiles(1.U) := Cat(
          arrayRegFiles(__tmp_7313 + 0.U)
        ).asUInt

        CP := 326.U
      }

      is(326.U) {
        /*
        if $1U.1 goto .327 else goto .330
        */


        CP := Mux((generalRegFiles(1.U).asUInt) === 1.U, 327.U, 330.U)
      }

      is(327.U) {
        /*
        $64S.4 = *(SP + (22: SP)) [signed, S64, 8]  // $64S.4 = n
        goto .328
        */


        val __tmp_7314 = ((SP + 22.U(16.W))).asUInt
        generalRegFiles(4.U) := Cat(
          arrayRegFiles(__tmp_7314 + 7.U),
          arrayRegFiles(__tmp_7314 + 6.U),
          arrayRegFiles(__tmp_7314 + 5.U),
          arrayRegFiles(__tmp_7314 + 4.U),
          arrayRegFiles(__tmp_7314 + 3.U),
          arrayRegFiles(__tmp_7314 + 2.U),
          arrayRegFiles(__tmp_7314 + 1.U),
          arrayRegFiles(__tmp_7314 + 0.U)
        ).asSInt.pad(GENERAL_REG_WIDTH).asUInt

        CP := 328.U
      }

      is(328.U) {
        /*
        $64S.5 = -($64S.4)
        goto .329
        */



        generalRegFiles(5.U) := (-generalRegFiles(4.U).asSInt).asUInt

        CP := 329.U
      }

      is(329.U) {
        /*
        $64S.3 = $64S.5
        goto .332
        */



        generalRegFiles(3.U) := (generalRegFiles(5.U).asSInt).asUInt

        CP := 332.U
      }

      is(330.U) {
        /*
        $64S.6 = *(SP + (22: SP)) [signed, S64, 8]  // $64S.6 = n
        goto .331
        */


        val __tmp_7315 = ((SP + 22.U(16.W))).asUInt
        generalRegFiles(6.U) := Cat(
          arrayRegFiles(__tmp_7315 + 7.U),
          arrayRegFiles(__tmp_7315 + 6.U),
          arrayRegFiles(__tmp_7315 + 5.U),
          arrayRegFiles(__tmp_7315 + 4.U),
          arrayRegFiles(__tmp_7315 + 3.U),
          arrayRegFiles(__tmp_7315 + 2.U),
          arrayRegFiles(__tmp_7315 + 1.U),
          arrayRegFiles(__tmp_7315 + 0.U)
        ).asSInt.pad(GENERAL_REG_WIDTH).asUInt

        CP := 331.U
      }

      is(331.U) {
        /*
        $64S.3 = $64S.6
        goto .332
        */



        generalRegFiles(3.U) := (generalRegFiles(6.U).asSInt).asUInt

        CP := 332.U
      }

      is(332.U) {
        /*
        *(SP + (66: SP)) = $64S.3 [signed, S64, 8]  // m = $64S.3
        goto .333
        */


        val __tmp_7316 = (SP + 66.U(16.W))
        val __tmp_7317 = (generalRegFiles(3.U).asSInt).asUInt
        arrayRegFiles(__tmp_7316 + 0.U) := __tmp_7317(7, 0)
        arrayRegFiles(__tmp_7316 + 1.U) := __tmp_7317(15, 8)
        arrayRegFiles(__tmp_7316 + 2.U) := __tmp_7317(23, 16)
        arrayRegFiles(__tmp_7316 + 3.U) := __tmp_7317(31, 24)
        arrayRegFiles(__tmp_7316 + 4.U) := __tmp_7317(39, 32)
        arrayRegFiles(__tmp_7316 + 5.U) := __tmp_7317(47, 40)
        arrayRegFiles(__tmp_7316 + 6.U) := __tmp_7317(55, 48)
        arrayRegFiles(__tmp_7316 + 7.U) := __tmp_7317(63, 56)

        CP := 333.U
      }

      is(333.U) {
        /*
        $64S.2 = *(SP + (66: SP)) [signed, S64, 8]  // $64S.2 = m
        goto .334
        */


        val __tmp_7318 = ((SP + 66.U(16.W))).asUInt
        generalRegFiles(2.U) := Cat(
          arrayRegFiles(__tmp_7318 + 7.U),
          arrayRegFiles(__tmp_7318 + 6.U),
          arrayRegFiles(__tmp_7318 + 5.U),
          arrayRegFiles(__tmp_7318 + 4.U),
          arrayRegFiles(__tmp_7318 + 3.U),
          arrayRegFiles(__tmp_7318 + 2.U),
          arrayRegFiles(__tmp_7318 + 1.U),
          arrayRegFiles(__tmp_7318 + 0.U)
        ).asSInt.pad(GENERAL_REG_WIDTH).asUInt

        CP := 334.U
      }

      is(334.U) {
        /*
        $1U.2 = ($64S.2 > (0: S64))
        goto .335
        */



        generalRegFiles(2.U) := (generalRegFiles(2.U).asSInt > 0.S(64.W)).asUInt

        CP := 335.U
      }

      is(335.U) {
        /*
        if $1U.2 goto .336 else goto .365
        */


        CP := Mux((generalRegFiles(2.U).asUInt) === 1.U, 336.U, 365.U)
      }

      is(336.U) {
        /*
        $64S.2 = *(SP + (66: SP)) [signed, S64, 8]  // $64S.2 = m
        goto .337
        */


        val __tmp_7319 = ((SP + 66.U(16.W))).asUInt
        generalRegFiles(2.U) := Cat(
          arrayRegFiles(__tmp_7319 + 7.U),
          arrayRegFiles(__tmp_7319 + 6.U),
          arrayRegFiles(__tmp_7319 + 5.U),
          arrayRegFiles(__tmp_7319 + 4.U),
          arrayRegFiles(__tmp_7319 + 3.U),
          arrayRegFiles(__tmp_7319 + 2.U),
          arrayRegFiles(__tmp_7319 + 1.U),
          arrayRegFiles(__tmp_7319 + 0.U)
        ).asSInt.pad(GENERAL_REG_WIDTH).asUInt

        CP := 337.U
      }

      is(337.U) {
        /*
        $64S.3 = ($64S.2 % (10: S64))
        goto .338
        */



        generalRegFiles(3.U) := ((generalRegFiles(2.U).asSInt % 10.S(64.W))).asUInt

        CP := 338.U
      }

      is(338.U) {
        /*
        switch ($64S.3)
          (0: S64): goto 339
          (1: S64): goto 341
          (2: S64): goto 343
          (3: S64): goto 345
          (4: S64): goto 347
          (5: S64): goto 349
          (6: S64): goto 351
          (7: S64): goto 353
          (8: S64): goto 355
          (9: S64): goto 357

        */


        val __tmp_7320 = generalRegFiles(3.U).asSInt

        switch(__tmp_7320) {

          is(0.S(64.W)) {
            CP := 339.U
          }


          is(1.S(64.W)) {
            CP := 341.U
          }


          is(2.S(64.W)) {
            CP := 343.U
          }


          is(3.S(64.W)) {
            CP := 345.U
          }


          is(4.S(64.W)) {
            CP := 347.U
          }


          is(5.S(64.W)) {
            CP := 349.U
          }


          is(6.S(64.W)) {
            CP := 351.U
          }


          is(7.S(64.W)) {
            CP := 353.U
          }


          is(8.S(64.W)) {
            CP := 355.U
          }


          is(9.S(64.W)) {
            CP := 357.U
          }

        }

      }

      is(339.U) {
        /*
        $16U.4 = (SP + (30: SP))
        $8S.3 = *(SP + (64: SP)) [signed, anvil.PrinterIndex.I20, 1]  // $8S.3 = i
        goto .340
        */



        generalRegFiles(4.U) := (SP + 30.U(16.W))

        val __tmp_7321 = ((SP + 64.U(16.W))).asUInt
        generalRegFiles(3.U) := Cat(
          arrayRegFiles(__tmp_7321 + 0.U)
        ).asSInt.pad(GENERAL_REG_WIDTH).asUInt

        CP := 340.U
      }

      is(340.U) {
        /*
        *(($16U.4 + (12: SP)) + ($8S.3 as SP)) = (48: U8) [unsigned, U8, 1]  // $16U.4($8S.3) = (48: U8)
        goto .359
        */


        val __tmp_7322 = ((generalRegFiles(4.U) + 12.U(16.W)) + generalRegFiles(3.U).asSInt.asUInt)
        val __tmp_7323 = (48.U(8.W)).asUInt
        arrayRegFiles(__tmp_7322 + 0.U) := __tmp_7323(7, 0)

        CP := 359.U
      }

      is(341.U) {
        /*
        $16U.4 = (SP + (30: SP))
        $8S.3 = *(SP + (64: SP)) [signed, anvil.PrinterIndex.I20, 1]  // $8S.3 = i
        goto .342
        */



        generalRegFiles(4.U) := (SP + 30.U(16.W))

        val __tmp_7324 = ((SP + 64.U(16.W))).asUInt
        generalRegFiles(3.U) := Cat(
          arrayRegFiles(__tmp_7324 + 0.U)
        ).asSInt.pad(GENERAL_REG_WIDTH).asUInt

        CP := 342.U
      }

      is(342.U) {
        /*
        *(($16U.4 + (12: SP)) + ($8S.3 as SP)) = (49: U8) [unsigned, U8, 1]  // $16U.4($8S.3) = (49: U8)
        goto .359
        */


        val __tmp_7325 = ((generalRegFiles(4.U) + 12.U(16.W)) + generalRegFiles(3.U).asSInt.asUInt)
        val __tmp_7326 = (49.U(8.W)).asUInt
        arrayRegFiles(__tmp_7325 + 0.U) := __tmp_7326(7, 0)

        CP := 359.U
      }

      is(343.U) {
        /*
        $16U.4 = (SP + (30: SP))
        $8S.3 = *(SP + (64: SP)) [signed, anvil.PrinterIndex.I20, 1]  // $8S.3 = i
        goto .344
        */



        generalRegFiles(4.U) := (SP + 30.U(16.W))

        val __tmp_7327 = ((SP + 64.U(16.W))).asUInt
        generalRegFiles(3.U) := Cat(
          arrayRegFiles(__tmp_7327 + 0.U)
        ).asSInt.pad(GENERAL_REG_WIDTH).asUInt

        CP := 344.U
      }

      is(344.U) {
        /*
        *(($16U.4 + (12: SP)) + ($8S.3 as SP)) = (50: U8) [unsigned, U8, 1]  // $16U.4($8S.3) = (50: U8)
        goto .359
        */


        val __tmp_7328 = ((generalRegFiles(4.U) + 12.U(16.W)) + generalRegFiles(3.U).asSInt.asUInt)
        val __tmp_7329 = (50.U(8.W)).asUInt
        arrayRegFiles(__tmp_7328 + 0.U) := __tmp_7329(7, 0)

        CP := 359.U
      }

      is(345.U) {
        /*
        $16U.4 = (SP + (30: SP))
        $8S.3 = *(SP + (64: SP)) [signed, anvil.PrinterIndex.I20, 1]  // $8S.3 = i
        goto .346
        */



        generalRegFiles(4.U) := (SP + 30.U(16.W))

        val __tmp_7330 = ((SP + 64.U(16.W))).asUInt
        generalRegFiles(3.U) := Cat(
          arrayRegFiles(__tmp_7330 + 0.U)
        ).asSInt.pad(GENERAL_REG_WIDTH).asUInt

        CP := 346.U
      }

      is(346.U) {
        /*
        *(($16U.4 + (12: SP)) + ($8S.3 as SP)) = (51: U8) [unsigned, U8, 1]  // $16U.4($8S.3) = (51: U8)
        goto .359
        */


        val __tmp_7331 = ((generalRegFiles(4.U) + 12.U(16.W)) + generalRegFiles(3.U).asSInt.asUInt)
        val __tmp_7332 = (51.U(8.W)).asUInt
        arrayRegFiles(__tmp_7331 + 0.U) := __tmp_7332(7, 0)

        CP := 359.U
      }

      is(347.U) {
        /*
        $16U.4 = (SP + (30: SP))
        $8S.3 = *(SP + (64: SP)) [signed, anvil.PrinterIndex.I20, 1]  // $8S.3 = i
        goto .348
        */



        generalRegFiles(4.U) := (SP + 30.U(16.W))

        val __tmp_7333 = ((SP + 64.U(16.W))).asUInt
        generalRegFiles(3.U) := Cat(
          arrayRegFiles(__tmp_7333 + 0.U)
        ).asSInt.pad(GENERAL_REG_WIDTH).asUInt

        CP := 348.U
      }

      is(348.U) {
        /*
        *(($16U.4 + (12: SP)) + ($8S.3 as SP)) = (52: U8) [unsigned, U8, 1]  // $16U.4($8S.3) = (52: U8)
        goto .359
        */


        val __tmp_7334 = ((generalRegFiles(4.U) + 12.U(16.W)) + generalRegFiles(3.U).asSInt.asUInt)
        val __tmp_7335 = (52.U(8.W)).asUInt
        arrayRegFiles(__tmp_7334 + 0.U) := __tmp_7335(7, 0)

        CP := 359.U
      }

      is(349.U) {
        /*
        $16U.4 = (SP + (30: SP))
        $8S.3 = *(SP + (64: SP)) [signed, anvil.PrinterIndex.I20, 1]  // $8S.3 = i
        goto .350
        */



        generalRegFiles(4.U) := (SP + 30.U(16.W))

        val __tmp_7336 = ((SP + 64.U(16.W))).asUInt
        generalRegFiles(3.U) := Cat(
          arrayRegFiles(__tmp_7336 + 0.U)
        ).asSInt.pad(GENERAL_REG_WIDTH).asUInt

        CP := 350.U
      }

      is(350.U) {
        /*
        *(($16U.4 + (12: SP)) + ($8S.3 as SP)) = (53: U8) [unsigned, U8, 1]  // $16U.4($8S.3) = (53: U8)
        goto .359
        */


        val __tmp_7337 = ((generalRegFiles(4.U) + 12.U(16.W)) + generalRegFiles(3.U).asSInt.asUInt)
        val __tmp_7338 = (53.U(8.W)).asUInt
        arrayRegFiles(__tmp_7337 + 0.U) := __tmp_7338(7, 0)

        CP := 359.U
      }

      is(351.U) {
        /*
        $16U.4 = (SP + (30: SP))
        $8S.3 = *(SP + (64: SP)) [signed, anvil.PrinterIndex.I20, 1]  // $8S.3 = i
        goto .352
        */



        generalRegFiles(4.U) := (SP + 30.U(16.W))

        val __tmp_7339 = ((SP + 64.U(16.W))).asUInt
        generalRegFiles(3.U) := Cat(
          arrayRegFiles(__tmp_7339 + 0.U)
        ).asSInt.pad(GENERAL_REG_WIDTH).asUInt

        CP := 352.U
      }

      is(352.U) {
        /*
        *(($16U.4 + (12: SP)) + ($8S.3 as SP)) = (54: U8) [unsigned, U8, 1]  // $16U.4($8S.3) = (54: U8)
        goto .359
        */


        val __tmp_7340 = ((generalRegFiles(4.U) + 12.U(16.W)) + generalRegFiles(3.U).asSInt.asUInt)
        val __tmp_7341 = (54.U(8.W)).asUInt
        arrayRegFiles(__tmp_7340 + 0.U) := __tmp_7341(7, 0)

        CP := 359.U
      }

      is(353.U) {
        /*
        $16U.4 = (SP + (30: SP))
        $8S.3 = *(SP + (64: SP)) [signed, anvil.PrinterIndex.I20, 1]  // $8S.3 = i
        goto .354
        */



        generalRegFiles(4.U) := (SP + 30.U(16.W))

        val __tmp_7342 = ((SP + 64.U(16.W))).asUInt
        generalRegFiles(3.U) := Cat(
          arrayRegFiles(__tmp_7342 + 0.U)
        ).asSInt.pad(GENERAL_REG_WIDTH).asUInt

        CP := 354.U
      }

      is(354.U) {
        /*
        *(($16U.4 + (12: SP)) + ($8S.3 as SP)) = (55: U8) [unsigned, U8, 1]  // $16U.4($8S.3) = (55: U8)
        goto .359
        */


        val __tmp_7343 = ((generalRegFiles(4.U) + 12.U(16.W)) + generalRegFiles(3.U).asSInt.asUInt)
        val __tmp_7344 = (55.U(8.W)).asUInt
        arrayRegFiles(__tmp_7343 + 0.U) := __tmp_7344(7, 0)

        CP := 359.U
      }

      is(355.U) {
        /*
        $16U.4 = (SP + (30: SP))
        $8S.3 = *(SP + (64: SP)) [signed, anvil.PrinterIndex.I20, 1]  // $8S.3 = i
        goto .356
        */



        generalRegFiles(4.U) := (SP + 30.U(16.W))

        val __tmp_7345 = ((SP + 64.U(16.W))).asUInt
        generalRegFiles(3.U) := Cat(
          arrayRegFiles(__tmp_7345 + 0.U)
        ).asSInt.pad(GENERAL_REG_WIDTH).asUInt

        CP := 356.U
      }

      is(356.U) {
        /*
        *(($16U.4 + (12: SP)) + ($8S.3 as SP)) = (56: U8) [unsigned, U8, 1]  // $16U.4($8S.3) = (56: U8)
        goto .359
        */


        val __tmp_7346 = ((generalRegFiles(4.U) + 12.U(16.W)) + generalRegFiles(3.U).asSInt.asUInt)
        val __tmp_7347 = (56.U(8.W)).asUInt
        arrayRegFiles(__tmp_7346 + 0.U) := __tmp_7347(7, 0)

        CP := 359.U
      }

      is(357.U) {
        /*
        $16U.4 = (SP + (30: SP))
        $8S.3 = *(SP + (64: SP)) [signed, anvil.PrinterIndex.I20, 1]  // $8S.3 = i
        goto .358
        */



        generalRegFiles(4.U) := (SP + 30.U(16.W))

        val __tmp_7348 = ((SP + 64.U(16.W))).asUInt
        generalRegFiles(3.U) := Cat(
          arrayRegFiles(__tmp_7348 + 0.U)
        ).asSInt.pad(GENERAL_REG_WIDTH).asUInt

        CP := 358.U
      }

      is(358.U) {
        /*
        *(($16U.4 + (12: SP)) + ($8S.3 as SP)) = (57: U8) [unsigned, U8, 1]  // $16U.4($8S.3) = (57: U8)
        goto .359
        */


        val __tmp_7349 = ((generalRegFiles(4.U) + 12.U(16.W)) + generalRegFiles(3.U).asSInt.asUInt)
        val __tmp_7350 = (57.U(8.W)).asUInt
        arrayRegFiles(__tmp_7349 + 0.U) := __tmp_7350(7, 0)

        CP := 359.U
      }

      is(359.U) {
        /*
        $64S.2 = *(SP + (66: SP)) [signed, S64, 8]  // $64S.2 = m
        goto .360
        */


        val __tmp_7351 = ((SP + 66.U(16.W))).asUInt
        generalRegFiles(2.U) := Cat(
          arrayRegFiles(__tmp_7351 + 7.U),
          arrayRegFiles(__tmp_7351 + 6.U),
          arrayRegFiles(__tmp_7351 + 5.U),
          arrayRegFiles(__tmp_7351 + 4.U),
          arrayRegFiles(__tmp_7351 + 3.U),
          arrayRegFiles(__tmp_7351 + 2.U),
          arrayRegFiles(__tmp_7351 + 1.U),
          arrayRegFiles(__tmp_7351 + 0.U)
        ).asSInt.pad(GENERAL_REG_WIDTH).asUInt

        CP := 360.U
      }

      is(360.U) {
        /*
        $64S.3 = ($64S.2 / (10: S64))
        goto .361
        */



        generalRegFiles(3.U) := ((generalRegFiles(2.U).asSInt / 10.S(64.W))).asUInt

        CP := 361.U
      }

      is(361.U) {
        /*
        *(SP + (66: SP)) = $64S.3 [signed, S64, 8]  // m = $64S.3
        goto .362
        */


        val __tmp_7352 = (SP + 66.U(16.W))
        val __tmp_7353 = (generalRegFiles(3.U).asSInt).asUInt
        arrayRegFiles(__tmp_7352 + 0.U) := __tmp_7353(7, 0)
        arrayRegFiles(__tmp_7352 + 1.U) := __tmp_7353(15, 8)
        arrayRegFiles(__tmp_7352 + 2.U) := __tmp_7353(23, 16)
        arrayRegFiles(__tmp_7352 + 3.U) := __tmp_7353(31, 24)
        arrayRegFiles(__tmp_7352 + 4.U) := __tmp_7353(39, 32)
        arrayRegFiles(__tmp_7352 + 5.U) := __tmp_7353(47, 40)
        arrayRegFiles(__tmp_7352 + 6.U) := __tmp_7353(55, 48)
        arrayRegFiles(__tmp_7352 + 7.U) := __tmp_7353(63, 56)

        CP := 362.U
      }

      is(362.U) {
        /*
        $8S.2 = *(SP + (64: SP)) [signed, anvil.PrinterIndex.I20, 1]  // $8S.2 = i
        goto .363
        */


        val __tmp_7354 = ((SP + 64.U(16.W))).asUInt
        generalRegFiles(2.U) := Cat(
          arrayRegFiles(__tmp_7354 + 0.U)
        ).asSInt.pad(GENERAL_REG_WIDTH).asUInt

        CP := 363.U
      }

      is(363.U) {
        /*
        $8S.3 = ($8S.2 + (1: anvil.PrinterIndex.I20))
        goto .364
        */



        generalRegFiles(3.U) := ((generalRegFiles(2.U).asSInt + 1.S(8.W))).asUInt

        CP := 364.U
      }

      is(364.U) {
        /*
        *(SP + (64: SP)) = $8S.3 [signed, anvil.PrinterIndex.I20, 1]  // i = $8S.3
        goto .333
        */


        val __tmp_7355 = (SP + 64.U(16.W))
        val __tmp_7356 = (generalRegFiles(3.U).asSInt).asUInt
        arrayRegFiles(__tmp_7355 + 0.U) := __tmp_7356(7, 0)

        CP := 333.U
      }

      is(365.U) {
        /*
        $1U.1 = *(SP + (65: SP)) [unsigned, B, 1]  // $1U.1 = neg
        undecl neg: B [@65, 1]
        goto .366
        */


        val __tmp_7357 = ((SP + 65.U(16.W))).asUInt
        generalRegFiles(1.U) := Cat(
          arrayRegFiles(__tmp_7357 + 0.U)
        ).asUInt

        CP := 366.U
      }

      is(366.U) {
        /*
        if $1U.1 goto .367 else goto .372
        */


        CP := Mux((generalRegFiles(1.U).asUInt) === 1.U, 367.U, 372.U)
      }

      is(367.U) {
        /*
        $16U.4 = (SP + (30: SP))
        $8S.3 = *(SP + (64: SP)) [signed, anvil.PrinterIndex.I20, 1]  // $8S.3 = i
        goto .368
        */



        generalRegFiles(4.U) := (SP + 30.U(16.W))

        val __tmp_7358 = ((SP + 64.U(16.W))).asUInt
        generalRegFiles(3.U) := Cat(
          arrayRegFiles(__tmp_7358 + 0.U)
        ).asSInt.pad(GENERAL_REG_WIDTH).asUInt

        CP := 368.U
      }

      is(368.U) {
        /*
        *(($16U.4 + (12: SP)) + ($8S.3 as SP)) = (45: U8) [unsigned, U8, 1]  // $16U.4($8S.3) = (45: U8)
        goto .369
        */


        val __tmp_7359 = ((generalRegFiles(4.U) + 12.U(16.W)) + generalRegFiles(3.U).asSInt.asUInt)
        val __tmp_7360 = (45.U(8.W)).asUInt
        arrayRegFiles(__tmp_7359 + 0.U) := __tmp_7360(7, 0)

        CP := 369.U
      }

      is(369.U) {
        /*
        $8S.2 = *(SP + (64: SP)) [signed, anvil.PrinterIndex.I20, 1]  // $8S.2 = i
        goto .370
        */


        val __tmp_7361 = ((SP + 64.U(16.W))).asUInt
        generalRegFiles(2.U) := Cat(
          arrayRegFiles(__tmp_7361 + 0.U)
        ).asSInt.pad(GENERAL_REG_WIDTH).asUInt

        CP := 370.U
      }

      is(370.U) {
        /*
        $8S.3 = ($8S.2 + (1: anvil.PrinterIndex.I20))
        goto .371
        */



        generalRegFiles(3.U) := ((generalRegFiles(2.U).asSInt + 1.S(8.W))).asUInt

        CP := 371.U
      }

      is(371.U) {
        /*
        *(SP + (64: SP)) = $8S.3 [signed, anvil.PrinterIndex.I20, 1]  // i = $8S.3
        goto .372
        */


        val __tmp_7362 = (SP + 64.U(16.W))
        val __tmp_7363 = (generalRegFiles(3.U).asSInt).asUInt
        arrayRegFiles(__tmp_7362 + 0.U) := __tmp_7363(7, 0)

        CP := 372.U
      }

      is(372.U) {
        /*
        decl j: anvil.PrinterIndex.I20 [@65, 1]
        $8S.2 = *(SP + (64: SP)) [signed, anvil.PrinterIndex.I20, 1]  // $8S.2 = i
        undecl i: anvil.PrinterIndex.I20 [@64, 1]
        goto .373
        */


        val __tmp_7364 = ((SP + 64.U(16.W))).asUInt
        generalRegFiles(2.U) := Cat(
          arrayRegFiles(__tmp_7364 + 0.U)
        ).asSInt.pad(GENERAL_REG_WIDTH).asUInt

        CP := 373.U
      }

      is(373.U) {
        /*
        $8S.3 = ($8S.2 - (1: anvil.PrinterIndex.I20))
        goto .374
        */



        generalRegFiles(3.U) := ((generalRegFiles(2.U).asSInt - 1.S(8.W))).asUInt

        CP := 374.U
      }

      is(374.U) {
        /*
        *(SP + (65: SP)) = $8S.3 [signed, anvil.PrinterIndex.I20, 1]  // j = $8S.3
        goto .375
        */


        val __tmp_7365 = (SP + 65.U(16.W))
        val __tmp_7366 = (generalRegFiles(3.U).asSInt).asUInt
        arrayRegFiles(__tmp_7365 + 0.U) := __tmp_7366(7, 0)

        CP := 375.U
      }

      is(375.U) {
        /*
        decl idx: anvil.PrinterIndex.U [@74, 8]
        $64U.4 = *(SP + (6: SP)) [unsigned, anvil.PrinterIndex.U, 8]  // $64U.4 = index
        goto .376
        */


        val __tmp_7367 = ((SP + 6.U(16.W))).asUInt
        generalRegFiles(4.U) := Cat(
          arrayRegFiles(__tmp_7367 + 7.U),
          arrayRegFiles(__tmp_7367 + 6.U),
          arrayRegFiles(__tmp_7367 + 5.U),
          arrayRegFiles(__tmp_7367 + 4.U),
          arrayRegFiles(__tmp_7367 + 3.U),
          arrayRegFiles(__tmp_7367 + 2.U),
          arrayRegFiles(__tmp_7367 + 1.U),
          arrayRegFiles(__tmp_7367 + 0.U)
        ).asUInt

        CP := 376.U
      }

      is(376.U) {
        /*
        *(SP + (74: SP)) = $64U.4 [signed, anvil.PrinterIndex.U, 8]  // idx = $64U.4
        goto .377
        */


        val __tmp_7368 = (SP + 74.U(16.W))
        val __tmp_7369 = (generalRegFiles(4.U)).asUInt
        arrayRegFiles(__tmp_7368 + 0.U) := __tmp_7369(7, 0)
        arrayRegFiles(__tmp_7368 + 1.U) := __tmp_7369(15, 8)
        arrayRegFiles(__tmp_7368 + 2.U) := __tmp_7369(23, 16)
        arrayRegFiles(__tmp_7368 + 3.U) := __tmp_7369(31, 24)
        arrayRegFiles(__tmp_7368 + 4.U) := __tmp_7369(39, 32)
        arrayRegFiles(__tmp_7368 + 5.U) := __tmp_7369(47, 40)
        arrayRegFiles(__tmp_7368 + 6.U) := __tmp_7369(55, 48)
        arrayRegFiles(__tmp_7368 + 7.U) := __tmp_7369(63, 56)

        CP := 377.U
      }

      is(377.U) {
        /*
        decl r: U64 [@82, 8]
        *(SP + (82: SP)) = (0: U64) [signed, U64, 8]  // r = (0: U64)
        goto .378
        */


        val __tmp_7370 = (SP + 82.U(16.W))
        val __tmp_7371 = (0.S(64.W)).asUInt
        arrayRegFiles(__tmp_7370 + 0.U) := __tmp_7371(7, 0)
        arrayRegFiles(__tmp_7370 + 1.U) := __tmp_7371(15, 8)
        arrayRegFiles(__tmp_7370 + 2.U) := __tmp_7371(23, 16)
        arrayRegFiles(__tmp_7370 + 3.U) := __tmp_7371(31, 24)
        arrayRegFiles(__tmp_7370 + 4.U) := __tmp_7371(39, 32)
        arrayRegFiles(__tmp_7370 + 5.U) := __tmp_7371(47, 40)
        arrayRegFiles(__tmp_7370 + 6.U) := __tmp_7371(55, 48)
        arrayRegFiles(__tmp_7370 + 7.U) := __tmp_7371(63, 56)

        CP := 378.U
      }

      is(378.U) {
        /*
        $8S.2 = *(SP + (65: SP)) [signed, anvil.PrinterIndex.I20, 1]  // $8S.2 = j
        goto .379
        */


        val __tmp_7372 = ((SP + 65.U(16.W))).asUInt
        generalRegFiles(2.U) := Cat(
          arrayRegFiles(__tmp_7372 + 0.U)
        ).asSInt.pad(GENERAL_REG_WIDTH).asUInt

        CP := 379.U
      }

      is(379.U) {
        /*
        $1U.2 = ($8S.2 >= (0: anvil.PrinterIndex.I20))
        goto .380
        */



        generalRegFiles(2.U) := (generalRegFiles(2.U).asSInt >= 0.S(8.W)).asUInt

        CP := 380.U
      }

      is(380.U) {
        /*
        if $1U.2 goto .381 else goto .395
        */


        CP := Mux((generalRegFiles(2.U).asUInt) === 1.U, 381.U, 395.U)
      }

      is(381.U) {
        /*
        $16U.3 = *(SP + (4: SP)) [unsigned, SP, 2]  // $16U.3 = buffer
        $64U.6 = *(SP + (74: SP)) [unsigned, anvil.PrinterIndex.U, 8]  // $64U.6 = idx
        $64U.8 = *(SP + (14: SP)) [unsigned, anvil.PrinterIndex.U, 8]  // $64U.8 = mask
        goto .382
        */


        val __tmp_7373 = ((SP + 4.U(16.W))).asUInt
        generalRegFiles(3.U) := Cat(
          arrayRegFiles(__tmp_7373 + 1.U),
          arrayRegFiles(__tmp_7373 + 0.U)
        ).asUInt

        val __tmp_7374 = ((SP + 74.U(16.W))).asUInt
        generalRegFiles(6.U) := Cat(
          arrayRegFiles(__tmp_7374 + 7.U),
          arrayRegFiles(__tmp_7374 + 6.U),
          arrayRegFiles(__tmp_7374 + 5.U),
          arrayRegFiles(__tmp_7374 + 4.U),
          arrayRegFiles(__tmp_7374 + 3.U),
          arrayRegFiles(__tmp_7374 + 2.U),
          arrayRegFiles(__tmp_7374 + 1.U),
          arrayRegFiles(__tmp_7374 + 0.U)
        ).asUInt

        val __tmp_7375 = ((SP + 14.U(16.W))).asUInt
        generalRegFiles(8.U) := Cat(
          arrayRegFiles(__tmp_7375 + 7.U),
          arrayRegFiles(__tmp_7375 + 6.U),
          arrayRegFiles(__tmp_7375 + 5.U),
          arrayRegFiles(__tmp_7375 + 4.U),
          arrayRegFiles(__tmp_7375 + 3.U),
          arrayRegFiles(__tmp_7375 + 2.U),
          arrayRegFiles(__tmp_7375 + 1.U),
          arrayRegFiles(__tmp_7375 + 0.U)
        ).asUInt

        CP := 382.U
      }

      is(382.U) {
        /*
        $64U.9 = ($64U.6 & $64U.8)
        goto .383
        */



        generalRegFiles(9.U) := (generalRegFiles(6.U) & generalRegFiles(8.U))

        CP := 383.U
      }

      is(383.U) {
        /*
        $16U.5 = (SP + (30: SP))
        $8S.4 = *(SP + (65: SP)) [signed, anvil.PrinterIndex.I20, 1]  // $8S.4 = j
        goto .384
        */



        generalRegFiles(5.U) := (SP + 30.U(16.W))

        val __tmp_7376 = ((SP + 65.U(16.W))).asUInt
        generalRegFiles(4.U) := Cat(
          arrayRegFiles(__tmp_7376 + 0.U)
        ).asSInt.pad(GENERAL_REG_WIDTH).asUInt

        CP := 384.U
      }

      is(384.U) {
        /*
        $8U.0 = *(($16U.5 + (12: SP)) + ($8S.4 as SP)) [unsigned, U8, 1]  // $8U.0 = $16U.5($8S.4)
        goto .385
        */


        val __tmp_7377 = (((generalRegFiles(5.U) + 12.U(16.W)) + generalRegFiles(4.U).asSInt.asUInt)).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_7377 + 0.U)
        ).asUInt

        CP := 385.U
      }

      is(385.U) {
        /*
        *(($16U.3 + (12: SP)) + ($64U.9 as SP)) = $8U.0 [unsigned, U8, 1]  // $16U.3($64U.9) = $8U.0
        goto .386
        */


        val __tmp_7378 = ((generalRegFiles(3.U) + 12.U(16.W)) + generalRegFiles(9.U).asUInt)
        val __tmp_7379 = (generalRegFiles(0.U)).asUInt
        arrayRegFiles(__tmp_7378 + 0.U) := __tmp_7379(7, 0)

        CP := 386.U
      }

      is(386.U) {
        /*
        $8S.2 = *(SP + (65: SP)) [signed, anvil.PrinterIndex.I20, 1]  // $8S.2 = j
        goto .387
        */


        val __tmp_7380 = ((SP + 65.U(16.W))).asUInt
        generalRegFiles(2.U) := Cat(
          arrayRegFiles(__tmp_7380 + 0.U)
        ).asSInt.pad(GENERAL_REG_WIDTH).asUInt

        CP := 387.U
      }

      is(387.U) {
        /*
        $8S.3 = ($8S.2 - (1: anvil.PrinterIndex.I20))
        goto .388
        */



        generalRegFiles(3.U) := ((generalRegFiles(2.U).asSInt - 1.S(8.W))).asUInt

        CP := 388.U
      }

      is(388.U) {
        /*
        *(SP + (65: SP)) = $8S.3 [signed, anvil.PrinterIndex.I20, 1]  // j = $8S.3
        goto .389
        */


        val __tmp_7381 = (SP + 65.U(16.W))
        val __tmp_7382 = (generalRegFiles(3.U).asSInt).asUInt
        arrayRegFiles(__tmp_7381 + 0.U) := __tmp_7382(7, 0)

        CP := 389.U
      }

      is(389.U) {
        /*
        $64U.4 = *(SP + (74: SP)) [unsigned, anvil.PrinterIndex.U, 8]  // $64U.4 = idx
        goto .390
        */


        val __tmp_7383 = ((SP + 74.U(16.W))).asUInt
        generalRegFiles(4.U) := Cat(
          arrayRegFiles(__tmp_7383 + 7.U),
          arrayRegFiles(__tmp_7383 + 6.U),
          arrayRegFiles(__tmp_7383 + 5.U),
          arrayRegFiles(__tmp_7383 + 4.U),
          arrayRegFiles(__tmp_7383 + 3.U),
          arrayRegFiles(__tmp_7383 + 2.U),
          arrayRegFiles(__tmp_7383 + 1.U),
          arrayRegFiles(__tmp_7383 + 0.U)
        ).asUInt

        CP := 390.U
      }

      is(390.U) {
        /*
        $64U.6 = ($64U.4 + (1: anvil.PrinterIndex.U))
        goto .391
        */



        generalRegFiles(6.U) := (generalRegFiles(4.U) + 1.U(64.W))

        CP := 391.U
      }

      is(391.U) {
        /*
        *(SP + (74: SP)) = $64U.6 [signed, anvil.PrinterIndex.U, 8]  // idx = $64U.6
        goto .392
        */


        val __tmp_7384 = (SP + 74.U(16.W))
        val __tmp_7385 = (generalRegFiles(6.U)).asUInt
        arrayRegFiles(__tmp_7384 + 0.U) := __tmp_7385(7, 0)
        arrayRegFiles(__tmp_7384 + 1.U) := __tmp_7385(15, 8)
        arrayRegFiles(__tmp_7384 + 2.U) := __tmp_7385(23, 16)
        arrayRegFiles(__tmp_7384 + 3.U) := __tmp_7385(31, 24)
        arrayRegFiles(__tmp_7384 + 4.U) := __tmp_7385(39, 32)
        arrayRegFiles(__tmp_7384 + 5.U) := __tmp_7385(47, 40)
        arrayRegFiles(__tmp_7384 + 6.U) := __tmp_7385(55, 48)
        arrayRegFiles(__tmp_7384 + 7.U) := __tmp_7385(63, 56)

        CP := 392.U
      }

      is(392.U) {
        /*
        $64U.5 = *(SP + (82: SP)) [unsigned, U64, 8]  // $64U.5 = r
        goto .393
        */


        val __tmp_7386 = ((SP + 82.U(16.W))).asUInt
        generalRegFiles(5.U) := Cat(
          arrayRegFiles(__tmp_7386 + 7.U),
          arrayRegFiles(__tmp_7386 + 6.U),
          arrayRegFiles(__tmp_7386 + 5.U),
          arrayRegFiles(__tmp_7386 + 4.U),
          arrayRegFiles(__tmp_7386 + 3.U),
          arrayRegFiles(__tmp_7386 + 2.U),
          arrayRegFiles(__tmp_7386 + 1.U),
          arrayRegFiles(__tmp_7386 + 0.U)
        ).asUInt

        CP := 393.U
      }

      is(393.U) {
        /*
        $64U.7 = ($64U.5 + (1: U64))
        goto .394
        */



        generalRegFiles(7.U) := (generalRegFiles(5.U) + 1.U(64.W))

        CP := 394.U
      }

      is(394.U) {
        /*
        *(SP + (82: SP)) = $64U.7 [signed, U64, 8]  // r = $64U.7
        goto .378
        */


        val __tmp_7387 = (SP + 82.U(16.W))
        val __tmp_7388 = (generalRegFiles(7.U)).asUInt
        arrayRegFiles(__tmp_7387 + 0.U) := __tmp_7388(7, 0)
        arrayRegFiles(__tmp_7387 + 1.U) := __tmp_7388(15, 8)
        arrayRegFiles(__tmp_7387 + 2.U) := __tmp_7388(23, 16)
        arrayRegFiles(__tmp_7387 + 3.U) := __tmp_7388(31, 24)
        arrayRegFiles(__tmp_7387 + 4.U) := __tmp_7388(39, 32)
        arrayRegFiles(__tmp_7387 + 5.U) := __tmp_7388(47, 40)
        arrayRegFiles(__tmp_7387 + 6.U) := __tmp_7388(55, 48)
        arrayRegFiles(__tmp_7387 + 7.U) := __tmp_7388(63, 56)

        CP := 378.U
      }

      is(395.U) {
        /*
        $64U.5 = *(SP + (82: SP)) [unsigned, U64, 8]  // $64U.5 = r
        undecl r: U64 [@82, 8]
        goto .396
        */


        val __tmp_7389 = ((SP + 82.U(16.W))).asUInt
        generalRegFiles(5.U) := Cat(
          arrayRegFiles(__tmp_7389 + 7.U),
          arrayRegFiles(__tmp_7389 + 6.U),
          arrayRegFiles(__tmp_7389 + 5.U),
          arrayRegFiles(__tmp_7389 + 4.U),
          arrayRegFiles(__tmp_7389 + 3.U),
          arrayRegFiles(__tmp_7389 + 2.U),
          arrayRegFiles(__tmp_7389 + 1.U),
          arrayRegFiles(__tmp_7389 + 0.U)
        ).asUInt

        CP := 396.U
      }

      is(396.U) {
        /*
        **(SP + (2: SP)) = $64U.5 [unsigned, U64, 8]  // $res = $64U.5
        goto $ret@0
        */


        val __tmp_7390 = Cat(
          arrayRegFiles((SP + 2.U(16.W)) + 1.U),
          arrayRegFiles((SP + 2.U(16.W)) + 0.U)
        )
        val __tmp_7391 = (generalRegFiles(5.U)).asUInt
        arrayRegFiles(__tmp_7390 + 0.U) := __tmp_7391(7, 0)
        arrayRegFiles(__tmp_7390 + 1.U) := __tmp_7391(15, 8)
        arrayRegFiles(__tmp_7390 + 2.U) := __tmp_7391(23, 16)
        arrayRegFiles(__tmp_7390 + 3.U) := __tmp_7391(31, 24)
        arrayRegFiles(__tmp_7390 + 4.U) := __tmp_7391(39, 32)
        arrayRegFiles(__tmp_7390 + 5.U) := __tmp_7391(47, 40)
        arrayRegFiles(__tmp_7390 + 6.U) := __tmp_7391(55, 48)
        arrayRegFiles(__tmp_7390 + 7.U) := __tmp_7391(63, 56)

        CP := Cat(
          arrayRegFiles(SP + 0.U + 1.U),
          arrayRegFiles(SP + 0.U + 0.U)
        )

      }

      is(397.U) {
        /*
        $64U.0 = *(SP + (4: SP)) [unsigned, U64, 8]  // n
        $64U.1 = *(SP + (12: SP)) [unsigned, U64, 8]  // m
        goto .398
        */


        val __tmp_7392 = ((SP + 4.U(16.W))).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_7392 + 7.U),
          arrayRegFiles(__tmp_7392 + 6.U),
          arrayRegFiles(__tmp_7392 + 5.U),
          arrayRegFiles(__tmp_7392 + 4.U),
          arrayRegFiles(__tmp_7392 + 3.U),
          arrayRegFiles(__tmp_7392 + 2.U),
          arrayRegFiles(__tmp_7392 + 1.U),
          arrayRegFiles(__tmp_7392 + 0.U)
        ).asUInt

        val __tmp_7393 = ((SP + 12.U(16.W))).asUInt
        generalRegFiles(1.U) := Cat(
          arrayRegFiles(__tmp_7393 + 7.U),
          arrayRegFiles(__tmp_7393 + 6.U),
          arrayRegFiles(__tmp_7393 + 5.U),
          arrayRegFiles(__tmp_7393 + 4.U),
          arrayRegFiles(__tmp_7393 + 3.U),
          arrayRegFiles(__tmp_7393 + 2.U),
          arrayRegFiles(__tmp_7393 + 1.U),
          arrayRegFiles(__tmp_7393 + 0.U)
        ).asUInt

        CP := 398.U
      }

      is(398.U) {
        /*
        $64U.2 = *(SP + (12: SP)) [unsigned, U64, 8]  // $64U.2 = m
        goto .399
        */


        val __tmp_7394 = ((SP + 12.U(16.W))).asUInt
        generalRegFiles(2.U) := Cat(
          arrayRegFiles(__tmp_7394 + 7.U),
          arrayRegFiles(__tmp_7394 + 6.U),
          arrayRegFiles(__tmp_7394 + 5.U),
          arrayRegFiles(__tmp_7394 + 4.U),
          arrayRegFiles(__tmp_7394 + 3.U),
          arrayRegFiles(__tmp_7394 + 2.U),
          arrayRegFiles(__tmp_7394 + 1.U),
          arrayRegFiles(__tmp_7394 + 0.U)
        ).asUInt

        CP := 399.U
      }

      is(399.U) {
        /*
        $1U.0 = ($64U.2 <= (20: U64))
        goto .400
        */



        generalRegFiles(0.U) := (generalRegFiles(2.U) <= 20.U(64.W)).asUInt

        CP := 400.U
      }

      is(400.U) {
        /*
        if $1U.0 goto .401 else goto .404
        */


        CP := Mux((generalRegFiles(0.U).asUInt) === 1.U, 401.U, 404.U)
      }

      is(401.U) {
        /*
        $64U.2 = *(SP + (4: SP)) [unsigned, U64, 8]  // $64U.2 = n
        $64U.3 = *(SP + (12: SP)) [unsigned, U64, 8]  // $64U.3 = m
        goto .402
        */


        val __tmp_7395 = ((SP + 4.U(16.W))).asUInt
        generalRegFiles(2.U) := Cat(
          arrayRegFiles(__tmp_7395 + 7.U),
          arrayRegFiles(__tmp_7395 + 6.U),
          arrayRegFiles(__tmp_7395 + 5.U),
          arrayRegFiles(__tmp_7395 + 4.U),
          arrayRegFiles(__tmp_7395 + 3.U),
          arrayRegFiles(__tmp_7395 + 2.U),
          arrayRegFiles(__tmp_7395 + 1.U),
          arrayRegFiles(__tmp_7395 + 0.U)
        ).asUInt

        val __tmp_7396 = ((SP + 12.U(16.W))).asUInt
        generalRegFiles(3.U) := Cat(
          arrayRegFiles(__tmp_7396 + 7.U),
          arrayRegFiles(__tmp_7396 + 6.U),
          arrayRegFiles(__tmp_7396 + 5.U),
          arrayRegFiles(__tmp_7396 + 4.U),
          arrayRegFiles(__tmp_7396 + 3.U),
          arrayRegFiles(__tmp_7396 + 2.U),
          arrayRegFiles(__tmp_7396 + 1.U),
          arrayRegFiles(__tmp_7396 + 0.U)
        ).asUInt

        CP := 402.U
      }

      is(402.U) {
        /*
        $64U.4 = ($64U.2 >>> $64U.3)
        goto .403
        */



        generalRegFiles(4.U) := (((generalRegFiles(2.U)) >> generalRegFiles(3.U)(4,0)))

        CP := 403.U
      }

      is(403.U) {
        /*
        **(SP + (2: SP)) = $64U.4 [unsigned, U64, 8]  // $res = $64U.4
        goto $ret@0
        */


        val __tmp_7397 = Cat(
          arrayRegFiles((SP + 2.U(16.W)) + 1.U),
          arrayRegFiles((SP + 2.U(16.W)) + 0.U)
        )
        val __tmp_7398 = (generalRegFiles(4.U)).asUInt
        arrayRegFiles(__tmp_7397 + 0.U) := __tmp_7398(7, 0)
        arrayRegFiles(__tmp_7397 + 1.U) := __tmp_7398(15, 8)
        arrayRegFiles(__tmp_7397 + 2.U) := __tmp_7398(23, 16)
        arrayRegFiles(__tmp_7397 + 3.U) := __tmp_7398(31, 24)
        arrayRegFiles(__tmp_7397 + 4.U) := __tmp_7398(39, 32)
        arrayRegFiles(__tmp_7397 + 5.U) := __tmp_7398(47, 40)
        arrayRegFiles(__tmp_7397 + 6.U) := __tmp_7398(55, 48)
        arrayRegFiles(__tmp_7397 + 7.U) := __tmp_7398(63, 56)

        CP := Cat(
          arrayRegFiles(SP + 0.U + 1.U),
          arrayRegFiles(SP + 0.U + 0.U)
        )

      }

      is(404.U) {
        /*
        $64U.2 = *(SP + (12: SP)) [unsigned, U64, 8]  // $64U.2 = m
        goto .405
        */


        val __tmp_7399 = ((SP + 12.U(16.W))).asUInt
        generalRegFiles(2.U) := Cat(
          arrayRegFiles(__tmp_7399 + 7.U),
          arrayRegFiles(__tmp_7399 + 6.U),
          arrayRegFiles(__tmp_7399 + 5.U),
          arrayRegFiles(__tmp_7399 + 4.U),
          arrayRegFiles(__tmp_7399 + 3.U),
          arrayRegFiles(__tmp_7399 + 2.U),
          arrayRegFiles(__tmp_7399 + 1.U),
          arrayRegFiles(__tmp_7399 + 0.U)
        ).asUInt

        CP := 405.U
      }

      is(405.U) {
        /*
        $1U.0 = ($64U.2 <= (40: U64))
        goto .406
        */



        generalRegFiles(0.U) := (generalRegFiles(2.U) <= 40.U(64.W)).asUInt

        CP := 406.U
      }

      is(406.U) {
        /*
        if $1U.0 goto .407 else goto .413
        */


        CP := Mux((generalRegFiles(0.U).asUInt) === 1.U, 407.U, 413.U)
      }

      is(407.U) {
        /*
        $64U.2 = *(SP + (4: SP)) [unsigned, U64, 8]  // $64U.2 = n
        goto .408
        */


        val __tmp_7400 = ((SP + 4.U(16.W))).asUInt
        generalRegFiles(2.U) := Cat(
          arrayRegFiles(__tmp_7400 + 7.U),
          arrayRegFiles(__tmp_7400 + 6.U),
          arrayRegFiles(__tmp_7400 + 5.U),
          arrayRegFiles(__tmp_7400 + 4.U),
          arrayRegFiles(__tmp_7400 + 3.U),
          arrayRegFiles(__tmp_7400 + 2.U),
          arrayRegFiles(__tmp_7400 + 1.U),
          arrayRegFiles(__tmp_7400 + 0.U)
        ).asUInt

        CP := 408.U
      }

      is(408.U) {
        /*
        $64U.3 = ($64U.2 >>> (20: U64))
        goto .409
        */



        generalRegFiles(3.U) := (((generalRegFiles(2.U)) >> 20.U(64.W)(4,0)))

        CP := 409.U
      }

      is(409.U) {
        /*
        $64U.4 = *(SP + (12: SP)) [unsigned, U64, 8]  // $64U.4 = m
        goto .410
        */


        val __tmp_7401 = ((SP + 12.U(16.W))).asUInt
        generalRegFiles(4.U) := Cat(
          arrayRegFiles(__tmp_7401 + 7.U),
          arrayRegFiles(__tmp_7401 + 6.U),
          arrayRegFiles(__tmp_7401 + 5.U),
          arrayRegFiles(__tmp_7401 + 4.U),
          arrayRegFiles(__tmp_7401 + 3.U),
          arrayRegFiles(__tmp_7401 + 2.U),
          arrayRegFiles(__tmp_7401 + 1.U),
          arrayRegFiles(__tmp_7401 + 0.U)
        ).asUInt

        CP := 410.U
      }

      is(410.U) {
        /*
        $64U.5 = ($64U.4 - (20: U64))
        goto .411
        */



        generalRegFiles(5.U) := (generalRegFiles(4.U) - 20.U(64.W))

        CP := 411.U
      }

      is(411.U) {
        /*
        $64U.6 = ($64U.3 >>> $64U.5)
        goto .412
        */



        generalRegFiles(6.U) := (((generalRegFiles(3.U)) >> generalRegFiles(5.U)(4,0)))

        CP := 412.U
      }

      is(412.U) {
        /*
        **(SP + (2: SP)) = $64U.6 [unsigned, U64, 8]  // $res = $64U.6
        goto $ret@0
        */


        val __tmp_7402 = Cat(
          arrayRegFiles((SP + 2.U(16.W)) + 1.U),
          arrayRegFiles((SP + 2.U(16.W)) + 0.U)
        )
        val __tmp_7403 = (generalRegFiles(6.U)).asUInt
        arrayRegFiles(__tmp_7402 + 0.U) := __tmp_7403(7, 0)
        arrayRegFiles(__tmp_7402 + 1.U) := __tmp_7403(15, 8)
        arrayRegFiles(__tmp_7402 + 2.U) := __tmp_7403(23, 16)
        arrayRegFiles(__tmp_7402 + 3.U) := __tmp_7403(31, 24)
        arrayRegFiles(__tmp_7402 + 4.U) := __tmp_7403(39, 32)
        arrayRegFiles(__tmp_7402 + 5.U) := __tmp_7403(47, 40)
        arrayRegFiles(__tmp_7402 + 6.U) := __tmp_7403(55, 48)
        arrayRegFiles(__tmp_7402 + 7.U) := __tmp_7403(63, 56)

        CP := Cat(
          arrayRegFiles(SP + 0.U + 1.U),
          arrayRegFiles(SP + 0.U + 0.U)
        )

      }

      is(413.U) {
        /*
        $64U.2 = *(SP + (12: SP)) [unsigned, U64, 8]  // $64U.2 = m
        goto .414
        */


        val __tmp_7404 = ((SP + 12.U(16.W))).asUInt
        generalRegFiles(2.U) := Cat(
          arrayRegFiles(__tmp_7404 + 7.U),
          arrayRegFiles(__tmp_7404 + 6.U),
          arrayRegFiles(__tmp_7404 + 5.U),
          arrayRegFiles(__tmp_7404 + 4.U),
          arrayRegFiles(__tmp_7404 + 3.U),
          arrayRegFiles(__tmp_7404 + 2.U),
          arrayRegFiles(__tmp_7404 + 1.U),
          arrayRegFiles(__tmp_7404 + 0.U)
        ).asUInt

        CP := 414.U
      }

      is(414.U) {
        /*
        $1U.0 = ($64U.2 <= (60: U64))
        goto .415
        */



        generalRegFiles(0.U) := (generalRegFiles(2.U) <= 60.U(64.W)).asUInt

        CP := 415.U
      }

      is(415.U) {
        /*
        if $1U.0 goto .416 else goto .422
        */


        CP := Mux((generalRegFiles(0.U).asUInt) === 1.U, 416.U, 422.U)
      }

      is(416.U) {
        /*
        $64U.2 = *(SP + (4: SP)) [unsigned, U64, 8]  // $64U.2 = n
        goto .417
        */


        val __tmp_7405 = ((SP + 4.U(16.W))).asUInt
        generalRegFiles(2.U) := Cat(
          arrayRegFiles(__tmp_7405 + 7.U),
          arrayRegFiles(__tmp_7405 + 6.U),
          arrayRegFiles(__tmp_7405 + 5.U),
          arrayRegFiles(__tmp_7405 + 4.U),
          arrayRegFiles(__tmp_7405 + 3.U),
          arrayRegFiles(__tmp_7405 + 2.U),
          arrayRegFiles(__tmp_7405 + 1.U),
          arrayRegFiles(__tmp_7405 + 0.U)
        ).asUInt

        CP := 417.U
      }

      is(417.U) {
        /*
        $64U.3 = ($64U.2 >>> (40: U64))
        goto .418
        */



        generalRegFiles(3.U) := (((generalRegFiles(2.U)) >> 40.U(64.W)(4,0)))

        CP := 418.U
      }

      is(418.U) {
        /*
        $64U.4 = *(SP + (12: SP)) [unsigned, U64, 8]  // $64U.4 = m
        goto .419
        */


        val __tmp_7406 = ((SP + 12.U(16.W))).asUInt
        generalRegFiles(4.U) := Cat(
          arrayRegFiles(__tmp_7406 + 7.U),
          arrayRegFiles(__tmp_7406 + 6.U),
          arrayRegFiles(__tmp_7406 + 5.U),
          arrayRegFiles(__tmp_7406 + 4.U),
          arrayRegFiles(__tmp_7406 + 3.U),
          arrayRegFiles(__tmp_7406 + 2.U),
          arrayRegFiles(__tmp_7406 + 1.U),
          arrayRegFiles(__tmp_7406 + 0.U)
        ).asUInt

        CP := 419.U
      }

      is(419.U) {
        /*
        $64U.5 = ($64U.4 - (40: U64))
        goto .420
        */



        generalRegFiles(5.U) := (generalRegFiles(4.U) - 40.U(64.W))

        CP := 420.U
      }

      is(420.U) {
        /*
        $64U.6 = ($64U.3 >>> $64U.5)
        goto .421
        */



        generalRegFiles(6.U) := (((generalRegFiles(3.U)) >> generalRegFiles(5.U)(4,0)))

        CP := 421.U
      }

      is(421.U) {
        /*
        **(SP + (2: SP)) = $64U.6 [unsigned, U64, 8]  // $res = $64U.6
        goto $ret@0
        */


        val __tmp_7407 = Cat(
          arrayRegFiles((SP + 2.U(16.W)) + 1.U),
          arrayRegFiles((SP + 2.U(16.W)) + 0.U)
        )
        val __tmp_7408 = (generalRegFiles(6.U)).asUInt
        arrayRegFiles(__tmp_7407 + 0.U) := __tmp_7408(7, 0)
        arrayRegFiles(__tmp_7407 + 1.U) := __tmp_7408(15, 8)
        arrayRegFiles(__tmp_7407 + 2.U) := __tmp_7408(23, 16)
        arrayRegFiles(__tmp_7407 + 3.U) := __tmp_7408(31, 24)
        arrayRegFiles(__tmp_7407 + 4.U) := __tmp_7408(39, 32)
        arrayRegFiles(__tmp_7407 + 5.U) := __tmp_7408(47, 40)
        arrayRegFiles(__tmp_7407 + 6.U) := __tmp_7408(55, 48)
        arrayRegFiles(__tmp_7407 + 7.U) := __tmp_7408(63, 56)

        CP := Cat(
          arrayRegFiles(SP + 0.U + 1.U),
          arrayRegFiles(SP + 0.U + 0.U)
        )

      }

      is(422.U) {
        /*
        $64U.2 = *(SP + (4: SP)) [unsigned, U64, 8]  // $64U.2 = n
        goto .423
        */


        val __tmp_7409 = ((SP + 4.U(16.W))).asUInt
        generalRegFiles(2.U) := Cat(
          arrayRegFiles(__tmp_7409 + 7.U),
          arrayRegFiles(__tmp_7409 + 6.U),
          arrayRegFiles(__tmp_7409 + 5.U),
          arrayRegFiles(__tmp_7409 + 4.U),
          arrayRegFiles(__tmp_7409 + 3.U),
          arrayRegFiles(__tmp_7409 + 2.U),
          arrayRegFiles(__tmp_7409 + 1.U),
          arrayRegFiles(__tmp_7409 + 0.U)
        ).asUInt

        CP := 423.U
      }

      is(423.U) {
        /*
        $64U.3 = ($64U.2 >>> (60: U64))
        goto .424
        */



        generalRegFiles(3.U) := (((generalRegFiles(2.U)) >> 60.U(64.W)(4,0)))

        CP := 424.U
      }

      is(424.U) {
        /*
        $64U.4 = *(SP + (12: SP)) [unsigned, U64, 8]  // $64U.4 = m
        goto .425
        */


        val __tmp_7410 = ((SP + 12.U(16.W))).asUInt
        generalRegFiles(4.U) := Cat(
          arrayRegFiles(__tmp_7410 + 7.U),
          arrayRegFiles(__tmp_7410 + 6.U),
          arrayRegFiles(__tmp_7410 + 5.U),
          arrayRegFiles(__tmp_7410 + 4.U),
          arrayRegFiles(__tmp_7410 + 3.U),
          arrayRegFiles(__tmp_7410 + 2.U),
          arrayRegFiles(__tmp_7410 + 1.U),
          arrayRegFiles(__tmp_7410 + 0.U)
        ).asUInt

        CP := 425.U
      }

      is(425.U) {
        /*
        $64U.5 = ($64U.4 - (60: U64))
        goto .426
        */



        generalRegFiles(5.U) := (generalRegFiles(4.U) - 60.U(64.W))

        CP := 426.U
      }

      is(426.U) {
        /*
        $64U.6 = ($64U.3 >>> $64U.5)
        goto .427
        */



        generalRegFiles(6.U) := (((generalRegFiles(3.U)) >> generalRegFiles(5.U)(4,0)))

        CP := 427.U
      }

      is(427.U) {
        /*
        **(SP + (2: SP)) = $64U.6 [unsigned, U64, 8]  // $res = $64U.6
        goto $ret@0
        */


        val __tmp_7411 = Cat(
          arrayRegFiles((SP + 2.U(16.W)) + 1.U),
          arrayRegFiles((SP + 2.U(16.W)) + 0.U)
        )
        val __tmp_7412 = (generalRegFiles(6.U)).asUInt
        arrayRegFiles(__tmp_7411 + 0.U) := __tmp_7412(7, 0)
        arrayRegFiles(__tmp_7411 + 1.U) := __tmp_7412(15, 8)
        arrayRegFiles(__tmp_7411 + 2.U) := __tmp_7412(23, 16)
        arrayRegFiles(__tmp_7411 + 3.U) := __tmp_7412(31, 24)
        arrayRegFiles(__tmp_7411 + 4.U) := __tmp_7412(39, 32)
        arrayRegFiles(__tmp_7411 + 5.U) := __tmp_7412(47, 40)
        arrayRegFiles(__tmp_7411 + 6.U) := __tmp_7412(55, 48)
        arrayRegFiles(__tmp_7411 + 7.U) := __tmp_7412(63, 56)

        CP := Cat(
          arrayRegFiles(SP + 0.U + 1.U),
          arrayRegFiles(SP + 0.U + 0.U)
        )

      }

      is(428.U) {
        /*
        $64U.0 = *(SP + (4: SP)) [unsigned, U64, 8]  // n
        $64U.1 = *(SP + (12: SP)) [unsigned, U64, 8]  // m
        goto .429
        */


        val __tmp_7413 = ((SP + 4.U(16.W))).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_7413 + 7.U),
          arrayRegFiles(__tmp_7413 + 6.U),
          arrayRegFiles(__tmp_7413 + 5.U),
          arrayRegFiles(__tmp_7413 + 4.U),
          arrayRegFiles(__tmp_7413 + 3.U),
          arrayRegFiles(__tmp_7413 + 2.U),
          arrayRegFiles(__tmp_7413 + 1.U),
          arrayRegFiles(__tmp_7413 + 0.U)
        ).asUInt

        val __tmp_7414 = ((SP + 12.U(16.W))).asUInt
        generalRegFiles(1.U) := Cat(
          arrayRegFiles(__tmp_7414 + 7.U),
          arrayRegFiles(__tmp_7414 + 6.U),
          arrayRegFiles(__tmp_7414 + 5.U),
          arrayRegFiles(__tmp_7414 + 4.U),
          arrayRegFiles(__tmp_7414 + 3.U),
          arrayRegFiles(__tmp_7414 + 2.U),
          arrayRegFiles(__tmp_7414 + 1.U),
          arrayRegFiles(__tmp_7414 + 0.U)
        ).asUInt

        CP := 429.U
      }

      is(429.U) {
        /*
        $64U.2 = *(SP + (12: SP)) [unsigned, U64, 8]  // $64U.2 = m
        goto .430
        */


        val __tmp_7415 = ((SP + 12.U(16.W))).asUInt
        generalRegFiles(2.U) := Cat(
          arrayRegFiles(__tmp_7415 + 7.U),
          arrayRegFiles(__tmp_7415 + 6.U),
          arrayRegFiles(__tmp_7415 + 5.U),
          arrayRegFiles(__tmp_7415 + 4.U),
          arrayRegFiles(__tmp_7415 + 3.U),
          arrayRegFiles(__tmp_7415 + 2.U),
          arrayRegFiles(__tmp_7415 + 1.U),
          arrayRegFiles(__tmp_7415 + 0.U)
        ).asUInt

        CP := 430.U
      }

      is(430.U) {
        /*
        $1U.0 = ($64U.2 <= (20: U64))
        goto .431
        */



        generalRegFiles(0.U) := (generalRegFiles(2.U) <= 20.U(64.W)).asUInt

        CP := 431.U
      }

      is(431.U) {
        /*
        if $1U.0 goto .432 else goto .435
        */


        CP := Mux((generalRegFiles(0.U).asUInt) === 1.U, 432.U, 435.U)
      }

      is(432.U) {
        /*
        $64U.2 = *(SP + (4: SP)) [unsigned, U64, 8]  // $64U.2 = n
        $64U.3 = *(SP + (12: SP)) [unsigned, U64, 8]  // $64U.3 = m
        goto .433
        */


        val __tmp_7416 = ((SP + 4.U(16.W))).asUInt
        generalRegFiles(2.U) := Cat(
          arrayRegFiles(__tmp_7416 + 7.U),
          arrayRegFiles(__tmp_7416 + 6.U),
          arrayRegFiles(__tmp_7416 + 5.U),
          arrayRegFiles(__tmp_7416 + 4.U),
          arrayRegFiles(__tmp_7416 + 3.U),
          arrayRegFiles(__tmp_7416 + 2.U),
          arrayRegFiles(__tmp_7416 + 1.U),
          arrayRegFiles(__tmp_7416 + 0.U)
        ).asUInt

        val __tmp_7417 = ((SP + 12.U(16.W))).asUInt
        generalRegFiles(3.U) := Cat(
          arrayRegFiles(__tmp_7417 + 7.U),
          arrayRegFiles(__tmp_7417 + 6.U),
          arrayRegFiles(__tmp_7417 + 5.U),
          arrayRegFiles(__tmp_7417 + 4.U),
          arrayRegFiles(__tmp_7417 + 3.U),
          arrayRegFiles(__tmp_7417 + 2.U),
          arrayRegFiles(__tmp_7417 + 1.U),
          arrayRegFiles(__tmp_7417 + 0.U)
        ).asUInt

        CP := 433.U
      }

      is(433.U) {
        /*
        $64U.4 = ($64U.2 << $64U.3)
        goto .434
        */



        generalRegFiles(4.U) := ((generalRegFiles(2.U)).asUInt << generalRegFiles(3.U)(4,0))

        CP := 434.U
      }

      is(434.U) {
        /*
        **(SP + (2: SP)) = $64U.4 [unsigned, U64, 8]  // $res = $64U.4
        goto $ret@0
        */


        val __tmp_7418 = Cat(
          arrayRegFiles((SP + 2.U(16.W)) + 1.U),
          arrayRegFiles((SP + 2.U(16.W)) + 0.U)
        )
        val __tmp_7419 = (generalRegFiles(4.U)).asUInt
        arrayRegFiles(__tmp_7418 + 0.U) := __tmp_7419(7, 0)
        arrayRegFiles(__tmp_7418 + 1.U) := __tmp_7419(15, 8)
        arrayRegFiles(__tmp_7418 + 2.U) := __tmp_7419(23, 16)
        arrayRegFiles(__tmp_7418 + 3.U) := __tmp_7419(31, 24)
        arrayRegFiles(__tmp_7418 + 4.U) := __tmp_7419(39, 32)
        arrayRegFiles(__tmp_7418 + 5.U) := __tmp_7419(47, 40)
        arrayRegFiles(__tmp_7418 + 6.U) := __tmp_7419(55, 48)
        arrayRegFiles(__tmp_7418 + 7.U) := __tmp_7419(63, 56)

        CP := Cat(
          arrayRegFiles(SP + 0.U + 1.U),
          arrayRegFiles(SP + 0.U + 0.U)
        )

      }

      is(435.U) {
        /*
        $64U.2 = *(SP + (12: SP)) [unsigned, U64, 8]  // $64U.2 = m
        goto .436
        */


        val __tmp_7420 = ((SP + 12.U(16.W))).asUInt
        generalRegFiles(2.U) := Cat(
          arrayRegFiles(__tmp_7420 + 7.U),
          arrayRegFiles(__tmp_7420 + 6.U),
          arrayRegFiles(__tmp_7420 + 5.U),
          arrayRegFiles(__tmp_7420 + 4.U),
          arrayRegFiles(__tmp_7420 + 3.U),
          arrayRegFiles(__tmp_7420 + 2.U),
          arrayRegFiles(__tmp_7420 + 1.U),
          arrayRegFiles(__tmp_7420 + 0.U)
        ).asUInt

        CP := 436.U
      }

      is(436.U) {
        /*
        $1U.0 = ($64U.2 <= (40: U64))
        goto .437
        */



        generalRegFiles(0.U) := (generalRegFiles(2.U) <= 40.U(64.W)).asUInt

        CP := 437.U
      }

      is(437.U) {
        /*
        if $1U.0 goto .438 else goto .444
        */


        CP := Mux((generalRegFiles(0.U).asUInt) === 1.U, 438.U, 444.U)
      }

      is(438.U) {
        /*
        $64U.2 = *(SP + (4: SP)) [unsigned, U64, 8]  // $64U.2 = n
        goto .439
        */


        val __tmp_7421 = ((SP + 4.U(16.W))).asUInt
        generalRegFiles(2.U) := Cat(
          arrayRegFiles(__tmp_7421 + 7.U),
          arrayRegFiles(__tmp_7421 + 6.U),
          arrayRegFiles(__tmp_7421 + 5.U),
          arrayRegFiles(__tmp_7421 + 4.U),
          arrayRegFiles(__tmp_7421 + 3.U),
          arrayRegFiles(__tmp_7421 + 2.U),
          arrayRegFiles(__tmp_7421 + 1.U),
          arrayRegFiles(__tmp_7421 + 0.U)
        ).asUInt

        CP := 439.U
      }

      is(439.U) {
        /*
        $64U.3 = ($64U.2 << (20: U64))
        goto .440
        */



        generalRegFiles(3.U) := ((generalRegFiles(2.U)).asUInt << 20.U(64.W)(4,0))

        CP := 440.U
      }

      is(440.U) {
        /*
        $64U.4 = *(SP + (12: SP)) [unsigned, U64, 8]  // $64U.4 = m
        goto .441
        */


        val __tmp_7422 = ((SP + 12.U(16.W))).asUInt
        generalRegFiles(4.U) := Cat(
          arrayRegFiles(__tmp_7422 + 7.U),
          arrayRegFiles(__tmp_7422 + 6.U),
          arrayRegFiles(__tmp_7422 + 5.U),
          arrayRegFiles(__tmp_7422 + 4.U),
          arrayRegFiles(__tmp_7422 + 3.U),
          arrayRegFiles(__tmp_7422 + 2.U),
          arrayRegFiles(__tmp_7422 + 1.U),
          arrayRegFiles(__tmp_7422 + 0.U)
        ).asUInt

        CP := 441.U
      }

      is(441.U) {
        /*
        $64U.5 = ($64U.4 - (20: U64))
        goto .442
        */



        generalRegFiles(5.U) := (generalRegFiles(4.U) - 20.U(64.W))

        CP := 442.U
      }

      is(442.U) {
        /*
        $64U.6 = ($64U.3 << $64U.5)
        goto .443
        */



        generalRegFiles(6.U) := ((generalRegFiles(3.U)).asUInt << generalRegFiles(5.U)(4,0))

        CP := 443.U
      }

      is(443.U) {
        /*
        **(SP + (2: SP)) = $64U.6 [unsigned, U64, 8]  // $res = $64U.6
        goto $ret@0
        */


        val __tmp_7423 = Cat(
          arrayRegFiles((SP + 2.U(16.W)) + 1.U),
          arrayRegFiles((SP + 2.U(16.W)) + 0.U)
        )
        val __tmp_7424 = (generalRegFiles(6.U)).asUInt
        arrayRegFiles(__tmp_7423 + 0.U) := __tmp_7424(7, 0)
        arrayRegFiles(__tmp_7423 + 1.U) := __tmp_7424(15, 8)
        arrayRegFiles(__tmp_7423 + 2.U) := __tmp_7424(23, 16)
        arrayRegFiles(__tmp_7423 + 3.U) := __tmp_7424(31, 24)
        arrayRegFiles(__tmp_7423 + 4.U) := __tmp_7424(39, 32)
        arrayRegFiles(__tmp_7423 + 5.U) := __tmp_7424(47, 40)
        arrayRegFiles(__tmp_7423 + 6.U) := __tmp_7424(55, 48)
        arrayRegFiles(__tmp_7423 + 7.U) := __tmp_7424(63, 56)

        CP := Cat(
          arrayRegFiles(SP + 0.U + 1.U),
          arrayRegFiles(SP + 0.U + 0.U)
        )

      }

      is(444.U) {
        /*
        $64U.2 = *(SP + (12: SP)) [unsigned, U64, 8]  // $64U.2 = m
        goto .445
        */


        val __tmp_7425 = ((SP + 12.U(16.W))).asUInt
        generalRegFiles(2.U) := Cat(
          arrayRegFiles(__tmp_7425 + 7.U),
          arrayRegFiles(__tmp_7425 + 6.U),
          arrayRegFiles(__tmp_7425 + 5.U),
          arrayRegFiles(__tmp_7425 + 4.U),
          arrayRegFiles(__tmp_7425 + 3.U),
          arrayRegFiles(__tmp_7425 + 2.U),
          arrayRegFiles(__tmp_7425 + 1.U),
          arrayRegFiles(__tmp_7425 + 0.U)
        ).asUInt

        CP := 445.U
      }

      is(445.U) {
        /*
        $1U.0 = ($64U.2 <= (60: U64))
        goto .446
        */



        generalRegFiles(0.U) := (generalRegFiles(2.U) <= 60.U(64.W)).asUInt

        CP := 446.U
      }

      is(446.U) {
        /*
        if $1U.0 goto .447 else goto .453
        */


        CP := Mux((generalRegFiles(0.U).asUInt) === 1.U, 447.U, 453.U)
      }

      is(447.U) {
        /*
        $64U.2 = *(SP + (4: SP)) [unsigned, U64, 8]  // $64U.2 = n
        goto .448
        */


        val __tmp_7426 = ((SP + 4.U(16.W))).asUInt
        generalRegFiles(2.U) := Cat(
          arrayRegFiles(__tmp_7426 + 7.U),
          arrayRegFiles(__tmp_7426 + 6.U),
          arrayRegFiles(__tmp_7426 + 5.U),
          arrayRegFiles(__tmp_7426 + 4.U),
          arrayRegFiles(__tmp_7426 + 3.U),
          arrayRegFiles(__tmp_7426 + 2.U),
          arrayRegFiles(__tmp_7426 + 1.U),
          arrayRegFiles(__tmp_7426 + 0.U)
        ).asUInt

        CP := 448.U
      }

      is(448.U) {
        /*
        $64U.3 = ($64U.2 << (40: U64))
        goto .449
        */



        generalRegFiles(3.U) := ((generalRegFiles(2.U)).asUInt << 40.U(64.W)(4,0))

        CP := 449.U
      }

      is(449.U) {
        /*
        $64U.4 = *(SP + (12: SP)) [unsigned, U64, 8]  // $64U.4 = m
        goto .450
        */


        val __tmp_7427 = ((SP + 12.U(16.W))).asUInt
        generalRegFiles(4.U) := Cat(
          arrayRegFiles(__tmp_7427 + 7.U),
          arrayRegFiles(__tmp_7427 + 6.U),
          arrayRegFiles(__tmp_7427 + 5.U),
          arrayRegFiles(__tmp_7427 + 4.U),
          arrayRegFiles(__tmp_7427 + 3.U),
          arrayRegFiles(__tmp_7427 + 2.U),
          arrayRegFiles(__tmp_7427 + 1.U),
          arrayRegFiles(__tmp_7427 + 0.U)
        ).asUInt

        CP := 450.U
      }

      is(450.U) {
        /*
        $64U.5 = ($64U.4 - (40: U64))
        goto .451
        */



        generalRegFiles(5.U) := (generalRegFiles(4.U) - 40.U(64.W))

        CP := 451.U
      }

      is(451.U) {
        /*
        $64U.6 = ($64U.3 << $64U.5)
        goto .452
        */



        generalRegFiles(6.U) := ((generalRegFiles(3.U)).asUInt << generalRegFiles(5.U)(4,0))

        CP := 452.U
      }

      is(452.U) {
        /*
        **(SP + (2: SP)) = $64U.6 [unsigned, U64, 8]  // $res = $64U.6
        goto $ret@0
        */


        val __tmp_7428 = Cat(
          arrayRegFiles((SP + 2.U(16.W)) + 1.U),
          arrayRegFiles((SP + 2.U(16.W)) + 0.U)
        )
        val __tmp_7429 = (generalRegFiles(6.U)).asUInt
        arrayRegFiles(__tmp_7428 + 0.U) := __tmp_7429(7, 0)
        arrayRegFiles(__tmp_7428 + 1.U) := __tmp_7429(15, 8)
        arrayRegFiles(__tmp_7428 + 2.U) := __tmp_7429(23, 16)
        arrayRegFiles(__tmp_7428 + 3.U) := __tmp_7429(31, 24)
        arrayRegFiles(__tmp_7428 + 4.U) := __tmp_7429(39, 32)
        arrayRegFiles(__tmp_7428 + 5.U) := __tmp_7429(47, 40)
        arrayRegFiles(__tmp_7428 + 6.U) := __tmp_7429(55, 48)
        arrayRegFiles(__tmp_7428 + 7.U) := __tmp_7429(63, 56)

        CP := Cat(
          arrayRegFiles(SP + 0.U + 1.U),
          arrayRegFiles(SP + 0.U + 0.U)
        )

      }

      is(453.U) {
        /*
        $64U.2 = *(SP + (4: SP)) [unsigned, U64, 8]  // $64U.2 = n
        goto .454
        */


        val __tmp_7430 = ((SP + 4.U(16.W))).asUInt
        generalRegFiles(2.U) := Cat(
          arrayRegFiles(__tmp_7430 + 7.U),
          arrayRegFiles(__tmp_7430 + 6.U),
          arrayRegFiles(__tmp_7430 + 5.U),
          arrayRegFiles(__tmp_7430 + 4.U),
          arrayRegFiles(__tmp_7430 + 3.U),
          arrayRegFiles(__tmp_7430 + 2.U),
          arrayRegFiles(__tmp_7430 + 1.U),
          arrayRegFiles(__tmp_7430 + 0.U)
        ).asUInt

        CP := 454.U
      }

      is(454.U) {
        /*
        $64U.3 = ($64U.2 << (60: U64))
        goto .455
        */



        generalRegFiles(3.U) := ((generalRegFiles(2.U)).asUInt << 60.U(64.W)(4,0))

        CP := 455.U
      }

      is(455.U) {
        /*
        $64U.4 = *(SP + (12: SP)) [unsigned, U64, 8]  // $64U.4 = m
        goto .456
        */


        val __tmp_7431 = ((SP + 12.U(16.W))).asUInt
        generalRegFiles(4.U) := Cat(
          arrayRegFiles(__tmp_7431 + 7.U),
          arrayRegFiles(__tmp_7431 + 6.U),
          arrayRegFiles(__tmp_7431 + 5.U),
          arrayRegFiles(__tmp_7431 + 4.U),
          arrayRegFiles(__tmp_7431 + 3.U),
          arrayRegFiles(__tmp_7431 + 2.U),
          arrayRegFiles(__tmp_7431 + 1.U),
          arrayRegFiles(__tmp_7431 + 0.U)
        ).asUInt

        CP := 456.U
      }

      is(456.U) {
        /*
        $64U.5 = ($64U.4 - (60: U64))
        goto .457
        */



        generalRegFiles(5.U) := (generalRegFiles(4.U) - 60.U(64.W))

        CP := 457.U
      }

      is(457.U) {
        /*
        $64U.6 = ($64U.3 << $64U.5)
        goto .458
        */



        generalRegFiles(6.U) := ((generalRegFiles(3.U)).asUInt << generalRegFiles(5.U)(4,0))

        CP := 458.U
      }

      is(458.U) {
        /*
        **(SP + (2: SP)) = $64U.6 [unsigned, U64, 8]  // $res = $64U.6
        goto $ret@0
        */


        val __tmp_7432 = Cat(
          arrayRegFiles((SP + 2.U(16.W)) + 1.U),
          arrayRegFiles((SP + 2.U(16.W)) + 0.U)
        )
        val __tmp_7433 = (generalRegFiles(6.U)).asUInt
        arrayRegFiles(__tmp_7432 + 0.U) := __tmp_7433(7, 0)
        arrayRegFiles(__tmp_7432 + 1.U) := __tmp_7433(15, 8)
        arrayRegFiles(__tmp_7432 + 2.U) := __tmp_7433(23, 16)
        arrayRegFiles(__tmp_7432 + 3.U) := __tmp_7433(31, 24)
        arrayRegFiles(__tmp_7432 + 4.U) := __tmp_7433(39, 32)
        arrayRegFiles(__tmp_7432 + 5.U) := __tmp_7433(47, 40)
        arrayRegFiles(__tmp_7432 + 6.U) := __tmp_7433(55, 48)
        arrayRegFiles(__tmp_7432 + 7.U) := __tmp_7433(63, 56)

        CP := Cat(
          arrayRegFiles(SP + 0.U + 1.U),
          arrayRegFiles(SP + 0.U + 0.U)
        )

      }

      is(459.U) {
        /*
        $64S.0 = *(SP + (4: SP)) [signed, S64, 8]  // n
        $64S.1 = *(SP + (12: SP)) [signed, S64, 8]  // m
        goto .460
        */


        val __tmp_7434 = ((SP + 4.U(16.W))).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_7434 + 7.U),
          arrayRegFiles(__tmp_7434 + 6.U),
          arrayRegFiles(__tmp_7434 + 5.U),
          arrayRegFiles(__tmp_7434 + 4.U),
          arrayRegFiles(__tmp_7434 + 3.U),
          arrayRegFiles(__tmp_7434 + 2.U),
          arrayRegFiles(__tmp_7434 + 1.U),
          arrayRegFiles(__tmp_7434 + 0.U)
        ).asSInt.pad(GENERAL_REG_WIDTH).asUInt

        val __tmp_7435 = ((SP + 12.U(16.W))).asUInt
        generalRegFiles(1.U) := Cat(
          arrayRegFiles(__tmp_7435 + 7.U),
          arrayRegFiles(__tmp_7435 + 6.U),
          arrayRegFiles(__tmp_7435 + 5.U),
          arrayRegFiles(__tmp_7435 + 4.U),
          arrayRegFiles(__tmp_7435 + 3.U),
          arrayRegFiles(__tmp_7435 + 2.U),
          arrayRegFiles(__tmp_7435 + 1.U),
          arrayRegFiles(__tmp_7435 + 0.U)
        ).asSInt.pad(GENERAL_REG_WIDTH).asUInt

        CP := 460.U
      }

      is(460.U) {
        /*
        $64S.2 = *(SP + (12: SP)) [signed, S64, 8]  // $64S.2 = m
        goto .461
        */


        val __tmp_7436 = ((SP + 12.U(16.W))).asUInt
        generalRegFiles(2.U) := Cat(
          arrayRegFiles(__tmp_7436 + 7.U),
          arrayRegFiles(__tmp_7436 + 6.U),
          arrayRegFiles(__tmp_7436 + 5.U),
          arrayRegFiles(__tmp_7436 + 4.U),
          arrayRegFiles(__tmp_7436 + 3.U),
          arrayRegFiles(__tmp_7436 + 2.U),
          arrayRegFiles(__tmp_7436 + 1.U),
          arrayRegFiles(__tmp_7436 + 0.U)
        ).asSInt.pad(GENERAL_REG_WIDTH).asUInt

        CP := 461.U
      }

      is(461.U) {
        /*
        $1U.0 = ($64S.2 <= (20: S64))
        goto .462
        */



        generalRegFiles(0.U) := (generalRegFiles(2.U).asSInt <= 20.S(64.W)).asUInt

        CP := 462.U
      }

      is(462.U) {
        /*
        if $1U.0 goto .463 else goto .466
        */


        CP := Mux((generalRegFiles(0.U).asUInt) === 1.U, 463.U, 466.U)
      }

      is(463.U) {
        /*
        $64S.2 = *(SP + (4: SP)) [signed, S64, 8]  // $64S.2 = n
        $64S.3 = *(SP + (12: SP)) [signed, S64, 8]  // $64S.3 = m
        goto .464
        */


        val __tmp_7437 = ((SP + 4.U(16.W))).asUInt
        generalRegFiles(2.U) := Cat(
          arrayRegFiles(__tmp_7437 + 7.U),
          arrayRegFiles(__tmp_7437 + 6.U),
          arrayRegFiles(__tmp_7437 + 5.U),
          arrayRegFiles(__tmp_7437 + 4.U),
          arrayRegFiles(__tmp_7437 + 3.U),
          arrayRegFiles(__tmp_7437 + 2.U),
          arrayRegFiles(__tmp_7437 + 1.U),
          arrayRegFiles(__tmp_7437 + 0.U)
        ).asSInt.pad(GENERAL_REG_WIDTH).asUInt

        val __tmp_7438 = ((SP + 12.U(16.W))).asUInt
        generalRegFiles(3.U) := Cat(
          arrayRegFiles(__tmp_7438 + 7.U),
          arrayRegFiles(__tmp_7438 + 6.U),
          arrayRegFiles(__tmp_7438 + 5.U),
          arrayRegFiles(__tmp_7438 + 4.U),
          arrayRegFiles(__tmp_7438 + 3.U),
          arrayRegFiles(__tmp_7438 + 2.U),
          arrayRegFiles(__tmp_7438 + 1.U),
          arrayRegFiles(__tmp_7438 + 0.U)
        ).asSInt.pad(GENERAL_REG_WIDTH).asUInt

        CP := 464.U
      }

      is(464.U) {
        /*
        $64S.4 = ($64S.2 >> $64S.3)
        goto .465
        */



        generalRegFiles(4.U) := (((generalRegFiles(2.U).asSInt).asSInt >> generalRegFiles(3.U).asSInt(4,0).asUInt)).asUInt

        CP := 465.U
      }

      is(465.U) {
        /*
        **(SP + (2: SP)) = $64S.4 [signed, S64, 8]  // $res = $64S.4
        goto $ret@0
        */


        val __tmp_7439 = Cat(
          arrayRegFiles((SP + 2.U(16.W)) + 1.U),
          arrayRegFiles((SP + 2.U(16.W)) + 0.U)
        )
        val __tmp_7440 = (generalRegFiles(4.U).asSInt).asUInt
        arrayRegFiles(__tmp_7439 + 0.U) := __tmp_7440(7, 0)
        arrayRegFiles(__tmp_7439 + 1.U) := __tmp_7440(15, 8)
        arrayRegFiles(__tmp_7439 + 2.U) := __tmp_7440(23, 16)
        arrayRegFiles(__tmp_7439 + 3.U) := __tmp_7440(31, 24)
        arrayRegFiles(__tmp_7439 + 4.U) := __tmp_7440(39, 32)
        arrayRegFiles(__tmp_7439 + 5.U) := __tmp_7440(47, 40)
        arrayRegFiles(__tmp_7439 + 6.U) := __tmp_7440(55, 48)
        arrayRegFiles(__tmp_7439 + 7.U) := __tmp_7440(63, 56)

        CP := Cat(
          arrayRegFiles(SP + 0.U + 1.U),
          arrayRegFiles(SP + 0.U + 0.U)
        )

      }

      is(466.U) {
        /*
        $64S.2 = *(SP + (12: SP)) [signed, S64, 8]  // $64S.2 = m
        goto .467
        */


        val __tmp_7441 = ((SP + 12.U(16.W))).asUInt
        generalRegFiles(2.U) := Cat(
          arrayRegFiles(__tmp_7441 + 7.U),
          arrayRegFiles(__tmp_7441 + 6.U),
          arrayRegFiles(__tmp_7441 + 5.U),
          arrayRegFiles(__tmp_7441 + 4.U),
          arrayRegFiles(__tmp_7441 + 3.U),
          arrayRegFiles(__tmp_7441 + 2.U),
          arrayRegFiles(__tmp_7441 + 1.U),
          arrayRegFiles(__tmp_7441 + 0.U)
        ).asSInt.pad(GENERAL_REG_WIDTH).asUInt

        CP := 467.U
      }

      is(467.U) {
        /*
        $1U.0 = ($64S.2 <= (40: S64))
        goto .468
        */



        generalRegFiles(0.U) := (generalRegFiles(2.U).asSInt <= 40.S(64.W)).asUInt

        CP := 468.U
      }

      is(468.U) {
        /*
        if $1U.0 goto .469 else goto .475
        */


        CP := Mux((generalRegFiles(0.U).asUInt) === 1.U, 469.U, 475.U)
      }

      is(469.U) {
        /*
        $64S.2 = *(SP + (4: SP)) [signed, S64, 8]  // $64S.2 = n
        goto .470
        */


        val __tmp_7442 = ((SP + 4.U(16.W))).asUInt
        generalRegFiles(2.U) := Cat(
          arrayRegFiles(__tmp_7442 + 7.U),
          arrayRegFiles(__tmp_7442 + 6.U),
          arrayRegFiles(__tmp_7442 + 5.U),
          arrayRegFiles(__tmp_7442 + 4.U),
          arrayRegFiles(__tmp_7442 + 3.U),
          arrayRegFiles(__tmp_7442 + 2.U),
          arrayRegFiles(__tmp_7442 + 1.U),
          arrayRegFiles(__tmp_7442 + 0.U)
        ).asSInt.pad(GENERAL_REG_WIDTH).asUInt

        CP := 470.U
      }

      is(470.U) {
        /*
        $64S.3 = ($64S.2 >> (20: S64))
        goto .471
        */



        generalRegFiles(3.U) := (((generalRegFiles(2.U).asSInt).asSInt >> 20.S(64.W)(4,0).asUInt)).asUInt

        CP := 471.U
      }

      is(471.U) {
        /*
        $64S.4 = *(SP + (12: SP)) [signed, S64, 8]  // $64S.4 = m
        goto .472
        */


        val __tmp_7443 = ((SP + 12.U(16.W))).asUInt
        generalRegFiles(4.U) := Cat(
          arrayRegFiles(__tmp_7443 + 7.U),
          arrayRegFiles(__tmp_7443 + 6.U),
          arrayRegFiles(__tmp_7443 + 5.U),
          arrayRegFiles(__tmp_7443 + 4.U),
          arrayRegFiles(__tmp_7443 + 3.U),
          arrayRegFiles(__tmp_7443 + 2.U),
          arrayRegFiles(__tmp_7443 + 1.U),
          arrayRegFiles(__tmp_7443 + 0.U)
        ).asSInt.pad(GENERAL_REG_WIDTH).asUInt

        CP := 472.U
      }

      is(472.U) {
        /*
        $64S.5 = ($64S.4 - (20: S64))
        goto .473
        */



        generalRegFiles(5.U) := ((generalRegFiles(4.U).asSInt - 20.S(64.W))).asUInt

        CP := 473.U
      }

      is(473.U) {
        /*
        $64S.6 = ($64S.3 >> $64S.5)
        goto .474
        */



        generalRegFiles(6.U) := (((generalRegFiles(3.U).asSInt).asSInt >> generalRegFiles(5.U).asSInt(4,0).asUInt)).asUInt

        CP := 474.U
      }

      is(474.U) {
        /*
        **(SP + (2: SP)) = $64S.6 [signed, S64, 8]  // $res = $64S.6
        goto $ret@0
        */


        val __tmp_7444 = Cat(
          arrayRegFiles((SP + 2.U(16.W)) + 1.U),
          arrayRegFiles((SP + 2.U(16.W)) + 0.U)
        )
        val __tmp_7445 = (generalRegFiles(6.U).asSInt).asUInt
        arrayRegFiles(__tmp_7444 + 0.U) := __tmp_7445(7, 0)
        arrayRegFiles(__tmp_7444 + 1.U) := __tmp_7445(15, 8)
        arrayRegFiles(__tmp_7444 + 2.U) := __tmp_7445(23, 16)
        arrayRegFiles(__tmp_7444 + 3.U) := __tmp_7445(31, 24)
        arrayRegFiles(__tmp_7444 + 4.U) := __tmp_7445(39, 32)
        arrayRegFiles(__tmp_7444 + 5.U) := __tmp_7445(47, 40)
        arrayRegFiles(__tmp_7444 + 6.U) := __tmp_7445(55, 48)
        arrayRegFiles(__tmp_7444 + 7.U) := __tmp_7445(63, 56)

        CP := Cat(
          arrayRegFiles(SP + 0.U + 1.U),
          arrayRegFiles(SP + 0.U + 0.U)
        )

      }

      is(475.U) {
        /*
        $64S.2 = *(SP + (12: SP)) [signed, S64, 8]  // $64S.2 = m
        goto .476
        */


        val __tmp_7446 = ((SP + 12.U(16.W))).asUInt
        generalRegFiles(2.U) := Cat(
          arrayRegFiles(__tmp_7446 + 7.U),
          arrayRegFiles(__tmp_7446 + 6.U),
          arrayRegFiles(__tmp_7446 + 5.U),
          arrayRegFiles(__tmp_7446 + 4.U),
          arrayRegFiles(__tmp_7446 + 3.U),
          arrayRegFiles(__tmp_7446 + 2.U),
          arrayRegFiles(__tmp_7446 + 1.U),
          arrayRegFiles(__tmp_7446 + 0.U)
        ).asSInt.pad(GENERAL_REG_WIDTH).asUInt

        CP := 476.U
      }

      is(476.U) {
        /*
        $1U.0 = ($64S.2 <= (60: S64))
        goto .477
        */



        generalRegFiles(0.U) := (generalRegFiles(2.U).asSInt <= 60.S(64.W)).asUInt

        CP := 477.U
      }

      is(477.U) {
        /*
        if $1U.0 goto .478 else goto .484
        */


        CP := Mux((generalRegFiles(0.U).asUInt) === 1.U, 478.U, 484.U)
      }

      is(478.U) {
        /*
        $64S.2 = *(SP + (4: SP)) [signed, S64, 8]  // $64S.2 = n
        goto .479
        */


        val __tmp_7447 = ((SP + 4.U(16.W))).asUInt
        generalRegFiles(2.U) := Cat(
          arrayRegFiles(__tmp_7447 + 7.U),
          arrayRegFiles(__tmp_7447 + 6.U),
          arrayRegFiles(__tmp_7447 + 5.U),
          arrayRegFiles(__tmp_7447 + 4.U),
          arrayRegFiles(__tmp_7447 + 3.U),
          arrayRegFiles(__tmp_7447 + 2.U),
          arrayRegFiles(__tmp_7447 + 1.U),
          arrayRegFiles(__tmp_7447 + 0.U)
        ).asSInt.pad(GENERAL_REG_WIDTH).asUInt

        CP := 479.U
      }

      is(479.U) {
        /*
        $64S.3 = ($64S.2 >> (40: S64))
        goto .480
        */



        generalRegFiles(3.U) := (((generalRegFiles(2.U).asSInt).asSInt >> 40.S(64.W)(4,0).asUInt)).asUInt

        CP := 480.U
      }

      is(480.U) {
        /*
        $64S.4 = *(SP + (12: SP)) [signed, S64, 8]  // $64S.4 = m
        goto .481
        */


        val __tmp_7448 = ((SP + 12.U(16.W))).asUInt
        generalRegFiles(4.U) := Cat(
          arrayRegFiles(__tmp_7448 + 7.U),
          arrayRegFiles(__tmp_7448 + 6.U),
          arrayRegFiles(__tmp_7448 + 5.U),
          arrayRegFiles(__tmp_7448 + 4.U),
          arrayRegFiles(__tmp_7448 + 3.U),
          arrayRegFiles(__tmp_7448 + 2.U),
          arrayRegFiles(__tmp_7448 + 1.U),
          arrayRegFiles(__tmp_7448 + 0.U)
        ).asSInt.pad(GENERAL_REG_WIDTH).asUInt

        CP := 481.U
      }

      is(481.U) {
        /*
        $64S.5 = ($64S.4 - (40: S64))
        goto .482
        */



        generalRegFiles(5.U) := ((generalRegFiles(4.U).asSInt - 40.S(64.W))).asUInt

        CP := 482.U
      }

      is(482.U) {
        /*
        $64S.6 = ($64S.3 >> $64S.5)
        goto .483
        */



        generalRegFiles(6.U) := (((generalRegFiles(3.U).asSInt).asSInt >> generalRegFiles(5.U).asSInt(4,0).asUInt)).asUInt

        CP := 483.U
      }

      is(483.U) {
        /*
        **(SP + (2: SP)) = $64S.6 [signed, S64, 8]  // $res = $64S.6
        goto $ret@0
        */


        val __tmp_7449 = Cat(
          arrayRegFiles((SP + 2.U(16.W)) + 1.U),
          arrayRegFiles((SP + 2.U(16.W)) + 0.U)
        )
        val __tmp_7450 = (generalRegFiles(6.U).asSInt).asUInt
        arrayRegFiles(__tmp_7449 + 0.U) := __tmp_7450(7, 0)
        arrayRegFiles(__tmp_7449 + 1.U) := __tmp_7450(15, 8)
        arrayRegFiles(__tmp_7449 + 2.U) := __tmp_7450(23, 16)
        arrayRegFiles(__tmp_7449 + 3.U) := __tmp_7450(31, 24)
        arrayRegFiles(__tmp_7449 + 4.U) := __tmp_7450(39, 32)
        arrayRegFiles(__tmp_7449 + 5.U) := __tmp_7450(47, 40)
        arrayRegFiles(__tmp_7449 + 6.U) := __tmp_7450(55, 48)
        arrayRegFiles(__tmp_7449 + 7.U) := __tmp_7450(63, 56)

        CP := Cat(
          arrayRegFiles(SP + 0.U + 1.U),
          arrayRegFiles(SP + 0.U + 0.U)
        )

      }

      is(484.U) {
        /*
        $64S.2 = *(SP + (4: SP)) [signed, S64, 8]  // $64S.2 = n
        goto .485
        */


        val __tmp_7451 = ((SP + 4.U(16.W))).asUInt
        generalRegFiles(2.U) := Cat(
          arrayRegFiles(__tmp_7451 + 7.U),
          arrayRegFiles(__tmp_7451 + 6.U),
          arrayRegFiles(__tmp_7451 + 5.U),
          arrayRegFiles(__tmp_7451 + 4.U),
          arrayRegFiles(__tmp_7451 + 3.U),
          arrayRegFiles(__tmp_7451 + 2.U),
          arrayRegFiles(__tmp_7451 + 1.U),
          arrayRegFiles(__tmp_7451 + 0.U)
        ).asSInt.pad(GENERAL_REG_WIDTH).asUInt

        CP := 485.U
      }

      is(485.U) {
        /*
        $64S.3 = ($64S.2 >> (60: S64))
        goto .486
        */



        generalRegFiles(3.U) := (((generalRegFiles(2.U).asSInt).asSInt >> 60.S(64.W)(4,0).asUInt)).asUInt

        CP := 486.U
      }

      is(486.U) {
        /*
        $64S.4 = *(SP + (12: SP)) [signed, S64, 8]  // $64S.4 = m
        goto .487
        */


        val __tmp_7452 = ((SP + 12.U(16.W))).asUInt
        generalRegFiles(4.U) := Cat(
          arrayRegFiles(__tmp_7452 + 7.U),
          arrayRegFiles(__tmp_7452 + 6.U),
          arrayRegFiles(__tmp_7452 + 5.U),
          arrayRegFiles(__tmp_7452 + 4.U),
          arrayRegFiles(__tmp_7452 + 3.U),
          arrayRegFiles(__tmp_7452 + 2.U),
          arrayRegFiles(__tmp_7452 + 1.U),
          arrayRegFiles(__tmp_7452 + 0.U)
        ).asSInt.pad(GENERAL_REG_WIDTH).asUInt

        CP := 487.U
      }

      is(487.U) {
        /*
        $64S.5 = ($64S.4 - (60: S64))
        goto .488
        */



        generalRegFiles(5.U) := ((generalRegFiles(4.U).asSInt - 60.S(64.W))).asUInt

        CP := 488.U
      }

      is(488.U) {
        /*
        $64S.6 = ($64S.3 >> $64S.5)
        goto .489
        */



        generalRegFiles(6.U) := (((generalRegFiles(3.U).asSInt).asSInt >> generalRegFiles(5.U).asSInt(4,0).asUInt)).asUInt

        CP := 489.U
      }

      is(489.U) {
        /*
        **(SP + (2: SP)) = $64S.6 [signed, S64, 8]  // $res = $64S.6
        goto $ret@0
        */


        val __tmp_7453 = Cat(
          arrayRegFiles((SP + 2.U(16.W)) + 1.U),
          arrayRegFiles((SP + 2.U(16.W)) + 0.U)
        )
        val __tmp_7454 = (generalRegFiles(6.U).asSInt).asUInt
        arrayRegFiles(__tmp_7453 + 0.U) := __tmp_7454(7, 0)
        arrayRegFiles(__tmp_7453 + 1.U) := __tmp_7454(15, 8)
        arrayRegFiles(__tmp_7453 + 2.U) := __tmp_7454(23, 16)
        arrayRegFiles(__tmp_7453 + 3.U) := __tmp_7454(31, 24)
        arrayRegFiles(__tmp_7453 + 4.U) := __tmp_7454(39, 32)
        arrayRegFiles(__tmp_7453 + 5.U) := __tmp_7454(47, 40)
        arrayRegFiles(__tmp_7453 + 6.U) := __tmp_7454(55, 48)
        arrayRegFiles(__tmp_7453 + 7.U) := __tmp_7454(63, 56)

        CP := Cat(
          arrayRegFiles(SP + 0.U + 1.U),
          arrayRegFiles(SP + 0.U + 0.U)
        )

      }

    }

}


