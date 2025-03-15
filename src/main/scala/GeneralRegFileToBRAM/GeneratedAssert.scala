package GeneralRegFileToBRAM 

import chisel3._
import chisel3.util._
import chisel3.experimental._

class bar(val C_S_AXI_DATA_WIDTH:  Int = 32,
               val C_S_AXI_ADDR_WIDTH:  Int = 32,
               val ARRAY_REG_WIDTH:     Int = 8,
               val ARRAY_REG_DEPTH:     Int = 1024,
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

    // divider
    //val divider64 = Module(new PipelinedDivMod(64))

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
      is(1.U) {
        printf("36 -- %x\n", arrayRegFiles(36))
        printf("37 -- %x\n", arrayRegFiles(37))
        printf("38 -- %x\n", arrayRegFiles(38))
        printf("39 -- %x\n", arrayRegFiles(39))
        printf("40 -- %x\n", arrayRegFiles(40))
        printf("41 -- %x\n", arrayRegFiles(41))
        printf("42 -- %x\n", arrayRegFiles(42))
        printf("43 -- %x\n", arrayRegFiles(43))
        printf("44 -- %x\n", arrayRegFiles(44))
        printf("45 -- %x\n", arrayRegFiles(45))
        printf("46 -- %x\n", arrayRegFiles(46))
        printf("47 -- %x\n", arrayRegFiles(47))
        printf("48 -- %x\n", arrayRegFiles(48))
        printf("49 -- %x\n", arrayRegFiles(49))
        printf("50 -- %x\n", arrayRegFiles(50))
        printf("51 -- %x\n", arrayRegFiles(51))
        printf("52 -- %x\n", arrayRegFiles(52))
        printf("53 -- %x\n", arrayRegFiles(53))
        printf("54 -- %x\n", arrayRegFiles(54))
        printf("55 -- %x\n", arrayRegFiles(55))
        printf("56 -- %x\n", arrayRegFiles(56))
      }

      is(2.U) {
        CP := Mux(io.valid, 3.U, CP)
      }

      is(3.U) {
        /*
        SP = 164
        *(0 [SP]) = (2 [SP]) [unsigned, SP, 2]  // $memory
        *(2 [SP]) = (886747591 [U32]) [unsigned, U32, 4]  // memory $type (MS[anvil.PrinterIndex.U, U8]: 0x34DAB1C7)
        *(6 [SP]) = (1024 [Z]) [signed, Z, 8]  // memory $size
        *(166 [SP]) = (0 [SP]) [unsigned, SP, 2]  // $sfCaller = 0
        DP = 0
        *(24 [U32]) = (886747591 [U32]) [unsigned, U32, 4]  // $display.$type (MS[anvil.PrinterIndex.U, U8]: 0x34DAB1C7)
        *(28 [SP]) = (128 [Z]) [signed, Z, 8]  // $display.size
        *(22 [SP]) = (24 [SP]) [unsigned, SP, 2]  // data address of $display (size = 140)
        *(164 [CP]) = (0 [CP]) [unsigned, CP, 2]  // $ret
        goto .4
        */


        SP := 164.U(16.W)

        val __tmp_761 = 0.U(16.W)
        val __tmp_762 = (2.U(16.W)).asUInt
        arrayRegFiles(__tmp_761 + 0.U) := __tmp_762(7, 0)
        arrayRegFiles(__tmp_761 + 1.U) := __tmp_762(15, 8)

        val __tmp_763 = 2.U(16.W)
        val __tmp_764 = (886747591.U(32.W)).asUInt
        arrayRegFiles(__tmp_763 + 0.U) := __tmp_764(7, 0)
        arrayRegFiles(__tmp_763 + 1.U) := __tmp_764(15, 8)
        arrayRegFiles(__tmp_763 + 2.U) := __tmp_764(23, 16)
        arrayRegFiles(__tmp_763 + 3.U) := __tmp_764(31, 24)

        val __tmp_765 = 6.U(16.W)
        val __tmp_766 = (1024.S(64.W)).asUInt
        arrayRegFiles(__tmp_765 + 0.U) := __tmp_766(7, 0)
        arrayRegFiles(__tmp_765 + 1.U) := __tmp_766(15, 8)
        arrayRegFiles(__tmp_765 + 2.U) := __tmp_766(23, 16)
        arrayRegFiles(__tmp_765 + 3.U) := __tmp_766(31, 24)
        arrayRegFiles(__tmp_765 + 4.U) := __tmp_766(39, 32)
        arrayRegFiles(__tmp_765 + 5.U) := __tmp_766(47, 40)
        arrayRegFiles(__tmp_765 + 6.U) := __tmp_766(55, 48)
        arrayRegFiles(__tmp_765 + 7.U) := __tmp_766(63, 56)

        val __tmp_767 = 166.U(16.W)
        val __tmp_768 = (0.U(16.W)).asUInt
        arrayRegFiles(__tmp_767 + 0.U) := __tmp_768(7, 0)
        arrayRegFiles(__tmp_767 + 1.U) := __tmp_768(15, 8)

        DP := 0.U(64.W)

        val __tmp_769 = 24.U(32.W)
        val __tmp_770 = (886747591.U(32.W)).asUInt
        arrayRegFiles(__tmp_769 + 0.U) := __tmp_770(7, 0)
        arrayRegFiles(__tmp_769 + 1.U) := __tmp_770(15, 8)
        arrayRegFiles(__tmp_769 + 2.U) := __tmp_770(23, 16)
        arrayRegFiles(__tmp_769 + 3.U) := __tmp_770(31, 24)

        val __tmp_771 = 28.U(16.W)
        val __tmp_772 = (128.S(64.W)).asUInt
        arrayRegFiles(__tmp_771 + 0.U) := __tmp_772(7, 0)
        arrayRegFiles(__tmp_771 + 1.U) := __tmp_772(15, 8)
        arrayRegFiles(__tmp_771 + 2.U) := __tmp_772(23, 16)
        arrayRegFiles(__tmp_771 + 3.U) := __tmp_772(31, 24)
        arrayRegFiles(__tmp_771 + 4.U) := __tmp_772(39, 32)
        arrayRegFiles(__tmp_771 + 5.U) := __tmp_772(47, 40)
        arrayRegFiles(__tmp_771 + 6.U) := __tmp_772(55, 48)
        arrayRegFiles(__tmp_771 + 7.U) := __tmp_772(63, 56)

        val __tmp_773 = 22.U(16.W)
        val __tmp_774 = (24.U(16.W)).asUInt
        arrayRegFiles(__tmp_773 + 0.U) := __tmp_774(7, 0)
        arrayRegFiles(__tmp_773 + 1.U) := __tmp_774(15, 8)

        val __tmp_775 = 164.U(16.W)
        val __tmp_776 = (0.U(16.W)).asUInt
        arrayRegFiles(__tmp_775 + 0.U) := __tmp_776(7, 0)
        arrayRegFiles(__tmp_775 + 1.U) := __tmp_776(15, 8)

        CP := 4.U
      }

      is(4.U) {
        /*
        *(SP + (8 [SP])) = (3069765878 [U32]) [unsigned, U32, 4]  // $sfDesc.type = 0xB6F8E8F6
        *(SP + (12 [SP])) = (22 [Z]) [signed, Z, 8]  // $sfDesc.size = 22
        *(SP + (20 [SP])) = (36 [U8]) [unsigned, U8, 1]  // '$'
        *(SP + (21 [SP])) = (116 [U8]) [unsigned, U8, 1]  // 't'
        *(SP + (22 [SP])) = (101 [U8]) [unsigned, U8, 1]  // 'e'
        *(SP + (23 [SP])) = (115 [U8]) [unsigned, U8, 1]  // 's'
        *(SP + (24 [SP])) = (116 [U8]) [unsigned, U8, 1]  // 't'
        *(SP + (25 [SP])) = (32 [U8]) [unsigned, U8, 1]  // ' '
        *(SP + (26 [SP])) = (40 [U8]) [unsigned, U8, 1]  // '('
        *(SP + (27 [SP])) = (97 [U8]) [unsigned, U8, 1]  // 'a'
        *(SP + (28 [SP])) = (115 [U8]) [unsigned, U8, 1]  // 's'
        *(SP + (29 [SP])) = (115 [U8]) [unsigned, U8, 1]  // 's'
        *(SP + (30 [SP])) = (101 [U8]) [unsigned, U8, 1]  // 'e'
        *(SP + (31 [SP])) = (114 [U8]) [unsigned, U8, 1]  // 'r'
        *(SP + (32 [SP])) = (116 [U8]) [unsigned, U8, 1]  // 't'
        *(SP + (33 [SP])) = (45 [U8]) [unsigned, U8, 1]  // '-'
        *(SP + (34 [SP])) = (116 [U8]) [unsigned, U8, 1]  // 't'
        *(SP + (35 [SP])) = (101 [U8]) [unsigned, U8, 1]  // 'e'
        *(SP + (36 [SP])) = (115 [U8]) [unsigned, U8, 1]  // 's'
        *(SP + (37 [SP])) = (116 [U8]) [unsigned, U8, 1]  // 't'
        *(SP + (38 [SP])) = (46 [U8]) [unsigned, U8, 1]  // '.'
        *(SP + (39 [SP])) = (115 [U8]) [unsigned, U8, 1]  // 's'
        *(SP + (40 [SP])) = (99 [U8]) [unsigned, U8, 1]  // 'c'
        *(SP + (41 [SP])) = (58 [U8]) [unsigned, U8, 1]  // ':'
        *(SP + (4 [SP])) = (9 [U32]) [signed, U32, 4]  // $sfLoc = (9 [U32])
        goto .5
        */


        val __tmp_777 = SP + 8.U(16.W)
        val __tmp_778 = (3069765878L.U(32.W)).asUInt
        arrayRegFiles(__tmp_777 + 0.U) := __tmp_778(7, 0)
        arrayRegFiles(__tmp_777 + 1.U) := __tmp_778(15, 8)
        arrayRegFiles(__tmp_777 + 2.U) := __tmp_778(23, 16)
        arrayRegFiles(__tmp_777 + 3.U) := __tmp_778(31, 24)

        val __tmp_779 = SP + 12.U(16.W)
        val __tmp_780 = (22.S(64.W)).asUInt
        arrayRegFiles(__tmp_779 + 0.U) := __tmp_780(7, 0)
        arrayRegFiles(__tmp_779 + 1.U) := __tmp_780(15, 8)
        arrayRegFiles(__tmp_779 + 2.U) := __tmp_780(23, 16)
        arrayRegFiles(__tmp_779 + 3.U) := __tmp_780(31, 24)
        arrayRegFiles(__tmp_779 + 4.U) := __tmp_780(39, 32)
        arrayRegFiles(__tmp_779 + 5.U) := __tmp_780(47, 40)
        arrayRegFiles(__tmp_779 + 6.U) := __tmp_780(55, 48)
        arrayRegFiles(__tmp_779 + 7.U) := __tmp_780(63, 56)

        val __tmp_781 = SP + 20.U(16.W)
        val __tmp_782 = (36.U(8.W)).asUInt
        arrayRegFiles(__tmp_781 + 0.U) := __tmp_782(7, 0)

        val __tmp_783 = SP + 21.U(16.W)
        val __tmp_784 = (116.U(8.W)).asUInt
        arrayRegFiles(__tmp_783 + 0.U) := __tmp_784(7, 0)

        val __tmp_785 = SP + 22.U(16.W)
        val __tmp_786 = (101.U(8.W)).asUInt
        arrayRegFiles(__tmp_785 + 0.U) := __tmp_786(7, 0)

        val __tmp_787 = SP + 23.U(16.W)
        val __tmp_788 = (115.U(8.W)).asUInt
        arrayRegFiles(__tmp_787 + 0.U) := __tmp_788(7, 0)

        val __tmp_789 = SP + 24.U(16.W)
        val __tmp_790 = (116.U(8.W)).asUInt
        arrayRegFiles(__tmp_789 + 0.U) := __tmp_790(7, 0)

        val __tmp_791 = SP + 25.U(16.W)
        val __tmp_792 = (32.U(8.W)).asUInt
        arrayRegFiles(__tmp_791 + 0.U) := __tmp_792(7, 0)

        val __tmp_793 = SP + 26.U(16.W)
        val __tmp_794 = (40.U(8.W)).asUInt
        arrayRegFiles(__tmp_793 + 0.U) := __tmp_794(7, 0)

        val __tmp_795 = SP + 27.U(16.W)
        val __tmp_796 = (97.U(8.W)).asUInt
        arrayRegFiles(__tmp_795 + 0.U) := __tmp_796(7, 0)

        val __tmp_797 = SP + 28.U(16.W)
        val __tmp_798 = (115.U(8.W)).asUInt
        arrayRegFiles(__tmp_797 + 0.U) := __tmp_798(7, 0)

        val __tmp_799 = SP + 29.U(16.W)
        val __tmp_800 = (115.U(8.W)).asUInt
        arrayRegFiles(__tmp_799 + 0.U) := __tmp_800(7, 0)

        val __tmp_801 = SP + 30.U(16.W)
        val __tmp_802 = (101.U(8.W)).asUInt
        arrayRegFiles(__tmp_801 + 0.U) := __tmp_802(7, 0)

        val __tmp_803 = SP + 31.U(16.W)
        val __tmp_804 = (114.U(8.W)).asUInt
        arrayRegFiles(__tmp_803 + 0.U) := __tmp_804(7, 0)

        val __tmp_805 = SP + 32.U(16.W)
        val __tmp_806 = (116.U(8.W)).asUInt
        arrayRegFiles(__tmp_805 + 0.U) := __tmp_806(7, 0)

        val __tmp_807 = SP + 33.U(16.W)
        val __tmp_808 = (45.U(8.W)).asUInt
        arrayRegFiles(__tmp_807 + 0.U) := __tmp_808(7, 0)

        val __tmp_809 = SP + 34.U(16.W)
        val __tmp_810 = (116.U(8.W)).asUInt
        arrayRegFiles(__tmp_809 + 0.U) := __tmp_810(7, 0)

        val __tmp_811 = SP + 35.U(16.W)
        val __tmp_812 = (101.U(8.W)).asUInt
        arrayRegFiles(__tmp_811 + 0.U) := __tmp_812(7, 0)

        val __tmp_813 = SP + 36.U(16.W)
        val __tmp_814 = (115.U(8.W)).asUInt
        arrayRegFiles(__tmp_813 + 0.U) := __tmp_814(7, 0)

        val __tmp_815 = SP + 37.U(16.W)
        val __tmp_816 = (116.U(8.W)).asUInt
        arrayRegFiles(__tmp_815 + 0.U) := __tmp_816(7, 0)

        val __tmp_817 = SP + 38.U(16.W)
        val __tmp_818 = (46.U(8.W)).asUInt
        arrayRegFiles(__tmp_817 + 0.U) := __tmp_818(7, 0)

        val __tmp_819 = SP + 39.U(16.W)
        val __tmp_820 = (115.U(8.W)).asUInt
        arrayRegFiles(__tmp_819 + 0.U) := __tmp_820(7, 0)

        val __tmp_821 = SP + 40.U(16.W)
        val __tmp_822 = (99.U(8.W)).asUInt
        arrayRegFiles(__tmp_821 + 0.U) := __tmp_822(7, 0)

        val __tmp_823 = SP + 41.U(16.W)
        val __tmp_824 = (58.U(8.W)).asUInt
        arrayRegFiles(__tmp_823 + 0.U) := __tmp_824(7, 0)

        val __tmp_825 = SP + 4.U(16.W)
        val __tmp_826 = (9.S(32.W)).asUInt
        arrayRegFiles(__tmp_825 + 0.U) := __tmp_826(7, 0)
        arrayRegFiles(__tmp_825 + 1.U) := __tmp_826(15, 8)
        arrayRegFiles(__tmp_825 + 2.U) := __tmp_826(23, 16)
        arrayRegFiles(__tmp_825 + 3.U) := __tmp_826(31, 24)

        CP := 5.U
      }

      is(5.U) {
        /*
        $0 = *(14 [SP]) [signed, Z, 8]  // $0 = $testNum
        goto .6
        */


        val __tmp_827 = (14.U(16.W)).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_827 + 7.U),
          arrayRegFiles(__tmp_827 + 6.U),
          arrayRegFiles(__tmp_827 + 5.U),
          arrayRegFiles(__tmp_827 + 4.U),
          arrayRegFiles(__tmp_827 + 3.U),
          arrayRegFiles(__tmp_827 + 2.U),
          arrayRegFiles(__tmp_827 + 1.U),
          arrayRegFiles(__tmp_827 + 0.U)
        ).asUInt

        CP := 6.U
      }

      is(6.U) {
        /*
        if (($0 < (0 [Z])) | ($0 ≡ (0 [Z]))) goto .7 else goto .99
        */


        CP := Mux(((generalRegFiles(0.U).asSInt < 0.S(64.W)).asUInt | (generalRegFiles(0.U).asSInt === 0.S(64.W)).asUInt.asUInt) === 1.U, 7.U, 99.U)
      }

      is(7.U) {
        /*
        *(SP + (4 [SP])) = (9 [U32]) [signed, U32, 4]  // $sfLoc = (9 [U32])
        goto .8
        */


        val __tmp_828 = SP + 4.U(16.W)
        val __tmp_829 = (9.S(32.W)).asUInt
        arrayRegFiles(__tmp_828 + 0.U) := __tmp_829(7, 0)
        arrayRegFiles(__tmp_828 + 1.U) := __tmp_829(15, 8)
        arrayRegFiles(__tmp_828 + 2.U) := __tmp_829(23, 16)
        arrayRegFiles(__tmp_828 + 3.U) := __tmp_829(31, 24)

        CP := 8.U
      }

      is(8.U) {
        /*
        SP = SP + 132
        goto .9
        */


        SP := SP + 132.U

        CP := 9.U
      }

      is(9.U) {
        /*
        decl $ret: CP [@0, 2], $sfCaller: SP [@2, 2], $sfLoc: U32 [@4, 4], $sfDesc: IS[20, U8] [@8, 32], $sfCurrentId: SP [@40, 2]
        *SP = (100 [CP]) [unsigned, CP, 2]  // $ret@0 = 2223
        *(SP + (2 [SP])) = (SP - (130 [SP])) [unsigned, SP, 2]  // $sfCaller@2 = -130
        *(SP + (40 [SP])) = (SP + (2 [SP])) [unsigned, SP, 2]  // $sfCurrentId@40 = 2
        *(SP - (88 [SP])) = $0 [unsigned, U64, 8]  // save $0
        *(SP - (80 [SP])) = $1 [unsigned, U64, 8]  // save $1
        *(SP - (72 [SP])) = $2 [unsigned, U64, 8]  // save $2
        *(SP - (64 [SP])) = $3 [unsigned, U64, 8]  // save $3
        *(SP - (56 [SP])) = $4 [unsigned, U64, 8]  // save $4
        *(SP - (48 [SP])) = $5 [unsigned, U64, 8]  // save $5
        *(SP - (40 [SP])) = $6 [unsigned, U64, 8]  // save $6
        *(SP - (32 [SP])) = $7 [unsigned, U64, 8]  // save $7
        *(SP - (24 [SP])) = $8 [unsigned, U64, 8]  // save $8
        *(SP - (16 [SP])) = $9 [unsigned, U64, 8]  // save $9
        *(SP - (8 [SP])) = $10 [unsigned, U64, 8]  // save $10
        goto .10
        */


        val __tmp_830 = SP
        val __tmp_831 = (100.U(16.W)).asUInt
        arrayRegFiles(__tmp_830 + 0.U) := __tmp_831(7, 0)
        arrayRegFiles(__tmp_830 + 1.U) := __tmp_831(15, 8)

        val __tmp_832 = SP + 2.U(16.W)
        val __tmp_833 = (SP - 130.U(16.W)).asUInt
        arrayRegFiles(__tmp_832 + 0.U) := __tmp_833(7, 0)
        arrayRegFiles(__tmp_832 + 1.U) := __tmp_833(15, 8)

        val __tmp_834 = SP + 40.U(16.W)
        val __tmp_835 = (SP + 2.U(16.W)).asUInt
        arrayRegFiles(__tmp_834 + 0.U) := __tmp_835(7, 0)
        arrayRegFiles(__tmp_834 + 1.U) := __tmp_835(15, 8)

        val __tmp_836 = SP - 88.U(16.W)
        val __tmp_837 = (generalRegFiles(0.U)).asUInt
        arrayRegFiles(__tmp_836 + 0.U) := __tmp_837(7, 0)
        arrayRegFiles(__tmp_836 + 1.U) := __tmp_837(15, 8)
        arrayRegFiles(__tmp_836 + 2.U) := __tmp_837(23, 16)
        arrayRegFiles(__tmp_836 + 3.U) := __tmp_837(31, 24)
        arrayRegFiles(__tmp_836 + 4.U) := __tmp_837(39, 32)
        arrayRegFiles(__tmp_836 + 5.U) := __tmp_837(47, 40)
        arrayRegFiles(__tmp_836 + 6.U) := __tmp_837(55, 48)
        arrayRegFiles(__tmp_836 + 7.U) := __tmp_837(63, 56)

        val __tmp_838 = SP - 80.U(16.W)
        val __tmp_839 = (generalRegFiles(1.U)).asUInt
        arrayRegFiles(__tmp_838 + 0.U) := __tmp_839(7, 0)
        arrayRegFiles(__tmp_838 + 1.U) := __tmp_839(15, 8)
        arrayRegFiles(__tmp_838 + 2.U) := __tmp_839(23, 16)
        arrayRegFiles(__tmp_838 + 3.U) := __tmp_839(31, 24)
        arrayRegFiles(__tmp_838 + 4.U) := __tmp_839(39, 32)
        arrayRegFiles(__tmp_838 + 5.U) := __tmp_839(47, 40)
        arrayRegFiles(__tmp_838 + 6.U) := __tmp_839(55, 48)
        arrayRegFiles(__tmp_838 + 7.U) := __tmp_839(63, 56)

        val __tmp_840 = SP - 72.U(16.W)
        val __tmp_841 = (generalRegFiles(2.U)).asUInt
        arrayRegFiles(__tmp_840 + 0.U) := __tmp_841(7, 0)
        arrayRegFiles(__tmp_840 + 1.U) := __tmp_841(15, 8)
        arrayRegFiles(__tmp_840 + 2.U) := __tmp_841(23, 16)
        arrayRegFiles(__tmp_840 + 3.U) := __tmp_841(31, 24)
        arrayRegFiles(__tmp_840 + 4.U) := __tmp_841(39, 32)
        arrayRegFiles(__tmp_840 + 5.U) := __tmp_841(47, 40)
        arrayRegFiles(__tmp_840 + 6.U) := __tmp_841(55, 48)
        arrayRegFiles(__tmp_840 + 7.U) := __tmp_841(63, 56)

        val __tmp_842 = SP - 64.U(16.W)
        val __tmp_843 = (generalRegFiles(3.U)).asUInt
        arrayRegFiles(__tmp_842 + 0.U) := __tmp_843(7, 0)
        arrayRegFiles(__tmp_842 + 1.U) := __tmp_843(15, 8)
        arrayRegFiles(__tmp_842 + 2.U) := __tmp_843(23, 16)
        arrayRegFiles(__tmp_842 + 3.U) := __tmp_843(31, 24)
        arrayRegFiles(__tmp_842 + 4.U) := __tmp_843(39, 32)
        arrayRegFiles(__tmp_842 + 5.U) := __tmp_843(47, 40)
        arrayRegFiles(__tmp_842 + 6.U) := __tmp_843(55, 48)
        arrayRegFiles(__tmp_842 + 7.U) := __tmp_843(63, 56)

        val __tmp_844 = SP - 56.U(16.W)
        val __tmp_845 = (generalRegFiles(4.U)).asUInt
        arrayRegFiles(__tmp_844 + 0.U) := __tmp_845(7, 0)
        arrayRegFiles(__tmp_844 + 1.U) := __tmp_845(15, 8)
        arrayRegFiles(__tmp_844 + 2.U) := __tmp_845(23, 16)
        arrayRegFiles(__tmp_844 + 3.U) := __tmp_845(31, 24)
        arrayRegFiles(__tmp_844 + 4.U) := __tmp_845(39, 32)
        arrayRegFiles(__tmp_844 + 5.U) := __tmp_845(47, 40)
        arrayRegFiles(__tmp_844 + 6.U) := __tmp_845(55, 48)
        arrayRegFiles(__tmp_844 + 7.U) := __tmp_845(63, 56)

        val __tmp_846 = SP - 48.U(16.W)
        val __tmp_847 = (generalRegFiles(5.U)).asUInt
        arrayRegFiles(__tmp_846 + 0.U) := __tmp_847(7, 0)
        arrayRegFiles(__tmp_846 + 1.U) := __tmp_847(15, 8)
        arrayRegFiles(__tmp_846 + 2.U) := __tmp_847(23, 16)
        arrayRegFiles(__tmp_846 + 3.U) := __tmp_847(31, 24)
        arrayRegFiles(__tmp_846 + 4.U) := __tmp_847(39, 32)
        arrayRegFiles(__tmp_846 + 5.U) := __tmp_847(47, 40)
        arrayRegFiles(__tmp_846 + 6.U) := __tmp_847(55, 48)
        arrayRegFiles(__tmp_846 + 7.U) := __tmp_847(63, 56)

        val __tmp_848 = SP - 40.U(16.W)
        val __tmp_849 = (generalRegFiles(6.U)).asUInt
        arrayRegFiles(__tmp_848 + 0.U) := __tmp_849(7, 0)
        arrayRegFiles(__tmp_848 + 1.U) := __tmp_849(15, 8)
        arrayRegFiles(__tmp_848 + 2.U) := __tmp_849(23, 16)
        arrayRegFiles(__tmp_848 + 3.U) := __tmp_849(31, 24)
        arrayRegFiles(__tmp_848 + 4.U) := __tmp_849(39, 32)
        arrayRegFiles(__tmp_848 + 5.U) := __tmp_849(47, 40)
        arrayRegFiles(__tmp_848 + 6.U) := __tmp_849(55, 48)
        arrayRegFiles(__tmp_848 + 7.U) := __tmp_849(63, 56)

        val __tmp_850 = SP - 32.U(16.W)
        val __tmp_851 = (generalRegFiles(7.U)).asUInt
        arrayRegFiles(__tmp_850 + 0.U) := __tmp_851(7, 0)
        arrayRegFiles(__tmp_850 + 1.U) := __tmp_851(15, 8)
        arrayRegFiles(__tmp_850 + 2.U) := __tmp_851(23, 16)
        arrayRegFiles(__tmp_850 + 3.U) := __tmp_851(31, 24)
        arrayRegFiles(__tmp_850 + 4.U) := __tmp_851(39, 32)
        arrayRegFiles(__tmp_850 + 5.U) := __tmp_851(47, 40)
        arrayRegFiles(__tmp_850 + 6.U) := __tmp_851(55, 48)
        arrayRegFiles(__tmp_850 + 7.U) := __tmp_851(63, 56)

        val __tmp_852 = SP - 24.U(16.W)
        val __tmp_853 = (generalRegFiles(8.U)).asUInt
        arrayRegFiles(__tmp_852 + 0.U) := __tmp_853(7, 0)
        arrayRegFiles(__tmp_852 + 1.U) := __tmp_853(15, 8)
        arrayRegFiles(__tmp_852 + 2.U) := __tmp_853(23, 16)
        arrayRegFiles(__tmp_852 + 3.U) := __tmp_853(31, 24)
        arrayRegFiles(__tmp_852 + 4.U) := __tmp_853(39, 32)
        arrayRegFiles(__tmp_852 + 5.U) := __tmp_853(47, 40)
        arrayRegFiles(__tmp_852 + 6.U) := __tmp_853(55, 48)
        arrayRegFiles(__tmp_852 + 7.U) := __tmp_853(63, 56)

        val __tmp_854 = SP - 16.U(16.W)
        val __tmp_855 = (generalRegFiles(9.U)).asUInt
        arrayRegFiles(__tmp_854 + 0.U) := __tmp_855(7, 0)
        arrayRegFiles(__tmp_854 + 1.U) := __tmp_855(15, 8)
        arrayRegFiles(__tmp_854 + 2.U) := __tmp_855(23, 16)
        arrayRegFiles(__tmp_854 + 3.U) := __tmp_855(31, 24)
        arrayRegFiles(__tmp_854 + 4.U) := __tmp_855(39, 32)
        arrayRegFiles(__tmp_854 + 5.U) := __tmp_855(47, 40)
        arrayRegFiles(__tmp_854 + 6.U) := __tmp_855(55, 48)
        arrayRegFiles(__tmp_854 + 7.U) := __tmp_855(63, 56)

        val __tmp_856 = SP - 8.U(16.W)
        val __tmp_857 = (generalRegFiles(10.U)).asUInt
        arrayRegFiles(__tmp_856 + 0.U) := __tmp_857(7, 0)
        arrayRegFiles(__tmp_856 + 1.U) := __tmp_857(15, 8)
        arrayRegFiles(__tmp_856 + 2.U) := __tmp_857(23, 16)
        arrayRegFiles(__tmp_856 + 3.U) := __tmp_857(31, 24)
        arrayRegFiles(__tmp_856 + 4.U) := __tmp_857(39, 32)
        arrayRegFiles(__tmp_856 + 5.U) := __tmp_857(47, 40)
        arrayRegFiles(__tmp_856 + 6.U) := __tmp_857(55, 48)
        arrayRegFiles(__tmp_856 + 7.U) := __tmp_857(63, 56)

        CP := 10.U
      }

      is(10.U) {
        /*
        *(SP + (8 [SP])) = (3069765878 [U32]) [unsigned, U32, 4]  // $sfDesc.type = 0xB6F8E8F6
        *(SP + (12 [SP])) = (20 [Z]) [signed, Z, 8]  // $sfDesc.size = 20
        *(SP + (20 [SP])) = (102 [U8]) [unsigned, U8, 1]  // 'f'
        *(SP + (21 [SP])) = (111 [U8]) [unsigned, U8, 1]  // 'o'
        *(SP + (22 [SP])) = (111 [U8]) [unsigned, U8, 1]  // 'o'
        *(SP + (23 [SP])) = (32 [U8]) [unsigned, U8, 1]  // ' '
        *(SP + (24 [SP])) = (40 [U8]) [unsigned, U8, 1]  // '('
        *(SP + (25 [SP])) = (97 [U8]) [unsigned, U8, 1]  // 'a'
        *(SP + (26 [SP])) = (115 [U8]) [unsigned, U8, 1]  // 's'
        *(SP + (27 [SP])) = (115 [U8]) [unsigned, U8, 1]  // 's'
        *(SP + (28 [SP])) = (101 [U8]) [unsigned, U8, 1]  // 'e'
        *(SP + (29 [SP])) = (114 [U8]) [unsigned, U8, 1]  // 'r'
        *(SP + (30 [SP])) = (116 [U8]) [unsigned, U8, 1]  // 't'
        *(SP + (31 [SP])) = (45 [U8]) [unsigned, U8, 1]  // '-'
        *(SP + (32 [SP])) = (116 [U8]) [unsigned, U8, 1]  // 't'
        *(SP + (33 [SP])) = (101 [U8]) [unsigned, U8, 1]  // 'e'
        *(SP + (34 [SP])) = (115 [U8]) [unsigned, U8, 1]  // 's'
        *(SP + (35 [SP])) = (116 [U8]) [unsigned, U8, 1]  // 't'
        *(SP + (36 [SP])) = (46 [U8]) [unsigned, U8, 1]  // '.'
        *(SP + (37 [SP])) = (115 [U8]) [unsigned, U8, 1]  // 's'
        *(SP + (38 [SP])) = (99 [U8]) [unsigned, U8, 1]  // 'c'
        *(SP + (39 [SP])) = (58 [U8]) [unsigned, U8, 1]  // ':'
        *(SP + (4 [SP])) = (10 [U32]) [signed, U32, 4]  // $sfLoc = (10 [U32])
        goto .11
        */


        val __tmp_858 = SP + 8.U(16.W)
        val __tmp_859 = (3069765878L.U(32.W)).asUInt
        arrayRegFiles(__tmp_858 + 0.U) := __tmp_859(7, 0)
        arrayRegFiles(__tmp_858 + 1.U) := __tmp_859(15, 8)
        arrayRegFiles(__tmp_858 + 2.U) := __tmp_859(23, 16)
        arrayRegFiles(__tmp_858 + 3.U) := __tmp_859(31, 24)

        val __tmp_860 = SP + 12.U(16.W)
        val __tmp_861 = (20.S(64.W)).asUInt
        arrayRegFiles(__tmp_860 + 0.U) := __tmp_861(7, 0)
        arrayRegFiles(__tmp_860 + 1.U) := __tmp_861(15, 8)
        arrayRegFiles(__tmp_860 + 2.U) := __tmp_861(23, 16)
        arrayRegFiles(__tmp_860 + 3.U) := __tmp_861(31, 24)
        arrayRegFiles(__tmp_860 + 4.U) := __tmp_861(39, 32)
        arrayRegFiles(__tmp_860 + 5.U) := __tmp_861(47, 40)
        arrayRegFiles(__tmp_860 + 6.U) := __tmp_861(55, 48)
        arrayRegFiles(__tmp_860 + 7.U) := __tmp_861(63, 56)

        val __tmp_862 = SP + 20.U(16.W)
        val __tmp_863 = (102.U(8.W)).asUInt
        arrayRegFiles(__tmp_862 + 0.U) := __tmp_863(7, 0)

        val __tmp_864 = SP + 21.U(16.W)
        val __tmp_865 = (111.U(8.W)).asUInt
        arrayRegFiles(__tmp_864 + 0.U) := __tmp_865(7, 0)

        val __tmp_866 = SP + 22.U(16.W)
        val __tmp_867 = (111.U(8.W)).asUInt
        arrayRegFiles(__tmp_866 + 0.U) := __tmp_867(7, 0)

        val __tmp_868 = SP + 23.U(16.W)
        val __tmp_869 = (32.U(8.W)).asUInt
        arrayRegFiles(__tmp_868 + 0.U) := __tmp_869(7, 0)

        val __tmp_870 = SP + 24.U(16.W)
        val __tmp_871 = (40.U(8.W)).asUInt
        arrayRegFiles(__tmp_870 + 0.U) := __tmp_871(7, 0)

        val __tmp_872 = SP + 25.U(16.W)
        val __tmp_873 = (97.U(8.W)).asUInt
        arrayRegFiles(__tmp_872 + 0.U) := __tmp_873(7, 0)

        val __tmp_874 = SP + 26.U(16.W)
        val __tmp_875 = (115.U(8.W)).asUInt
        arrayRegFiles(__tmp_874 + 0.U) := __tmp_875(7, 0)

        val __tmp_876 = SP + 27.U(16.W)
        val __tmp_877 = (115.U(8.W)).asUInt
        arrayRegFiles(__tmp_876 + 0.U) := __tmp_877(7, 0)

        val __tmp_878 = SP + 28.U(16.W)
        val __tmp_879 = (101.U(8.W)).asUInt
        arrayRegFiles(__tmp_878 + 0.U) := __tmp_879(7, 0)

        val __tmp_880 = SP + 29.U(16.W)
        val __tmp_881 = (114.U(8.W)).asUInt
        arrayRegFiles(__tmp_880 + 0.U) := __tmp_881(7, 0)

        val __tmp_882 = SP + 30.U(16.W)
        val __tmp_883 = (116.U(8.W)).asUInt
        arrayRegFiles(__tmp_882 + 0.U) := __tmp_883(7, 0)

        val __tmp_884 = SP + 31.U(16.W)
        val __tmp_885 = (45.U(8.W)).asUInt
        arrayRegFiles(__tmp_884 + 0.U) := __tmp_885(7, 0)

        val __tmp_886 = SP + 32.U(16.W)
        val __tmp_887 = (116.U(8.W)).asUInt
        arrayRegFiles(__tmp_886 + 0.U) := __tmp_887(7, 0)

        val __tmp_888 = SP + 33.U(16.W)
        val __tmp_889 = (101.U(8.W)).asUInt
        arrayRegFiles(__tmp_888 + 0.U) := __tmp_889(7, 0)

        val __tmp_890 = SP + 34.U(16.W)
        val __tmp_891 = (115.U(8.W)).asUInt
        arrayRegFiles(__tmp_890 + 0.U) := __tmp_891(7, 0)

        val __tmp_892 = SP + 35.U(16.W)
        val __tmp_893 = (116.U(8.W)).asUInt
        arrayRegFiles(__tmp_892 + 0.U) := __tmp_893(7, 0)

        val __tmp_894 = SP + 36.U(16.W)
        val __tmp_895 = (46.U(8.W)).asUInt
        arrayRegFiles(__tmp_894 + 0.U) := __tmp_895(7, 0)

        val __tmp_896 = SP + 37.U(16.W)
        val __tmp_897 = (115.U(8.W)).asUInt
        arrayRegFiles(__tmp_896 + 0.U) := __tmp_897(7, 0)

        val __tmp_898 = SP + 38.U(16.W)
        val __tmp_899 = (99.U(8.W)).asUInt
        arrayRegFiles(__tmp_898 + 0.U) := __tmp_899(7, 0)

        val __tmp_900 = SP + 39.U(16.W)
        val __tmp_901 = (58.U(8.W)).asUInt
        arrayRegFiles(__tmp_900 + 0.U) := __tmp_901(7, 0)

        val __tmp_902 = SP + 4.U(16.W)
        val __tmp_903 = (10.S(32.W)).asUInt
        arrayRegFiles(__tmp_902 + 0.U) := __tmp_903(7, 0)
        arrayRegFiles(__tmp_902 + 1.U) := __tmp_903(15, 8)
        arrayRegFiles(__tmp_902 + 2.U) := __tmp_903(23, 16)
        arrayRegFiles(__tmp_902 + 3.U) := __tmp_903(31, 24)

        CP := 11.U
      }

      is(11.U) {
        /*
        SP = SP + 130
        goto .12
        */


        SP := SP + 130.U

        CP := 12.U
      }

      is(12.U) {
        /*
        decl $ret: CP [@0, 2], $sfCaller: SP [@2, 2], $sfLoc: U32 [@4, 4], $sfDesc: IS[20, U8] [@8, 32], $sfCurrentId: SP [@40, 2], x: Z [@42, 8], y: Z [@50, 8]
        *SP = (102 [CP]) [unsigned, CP, 2]  // $ret@0 = 2224
        *(SP + (2 [SP])) = (SP - (128 [SP])) [unsigned, SP, 2]  // $sfCaller@2 = -128
        *(SP + (40 [SP])) = (SP + (2 [SP])) [unsigned, SP, 2]  // $sfCurrentId@40 = 2
        *(SP + (42 [SP])) = (3 [Z]) [signed, Z, 8]  // x = (3 [Z])
        *(SP + (50 [SP])) = (5 [Z]) [signed, Z, 8]  // y = (5 [Z])
        *(SP - (88 [SP])) = $0 [unsigned, U64, 8]  // save $0
        *(SP - (80 [SP])) = $1 [unsigned, U64, 8]  // save $1
        *(SP - (72 [SP])) = $2 [unsigned, U64, 8]  // save $2
        *(SP - (64 [SP])) = $3 [unsigned, U64, 8]  // save $3
        *(SP - (56 [SP])) = $4 [unsigned, U64, 8]  // save $4
        *(SP - (48 [SP])) = $5 [unsigned, U64, 8]  // save $5
        *(SP - (40 [SP])) = $6 [unsigned, U64, 8]  // save $6
        *(SP - (32 [SP])) = $7 [unsigned, U64, 8]  // save $7
        *(SP - (24 [SP])) = $8 [unsigned, U64, 8]  // save $8
        *(SP - (16 [SP])) = $9 [unsigned, U64, 8]  // save $9
        *(SP - (8 [SP])) = $10 [unsigned, U64, 8]  // save $10
        goto .13
        */


        val __tmp_904 = SP
        val __tmp_905 = (102.U(16.W)).asUInt
        arrayRegFiles(__tmp_904 + 0.U) := __tmp_905(7, 0)
        arrayRegFiles(__tmp_904 + 1.U) := __tmp_905(15, 8)

        val __tmp_906 = SP + 2.U(16.W)
        val __tmp_907 = (SP - 128.U(16.W)).asUInt
        arrayRegFiles(__tmp_906 + 0.U) := __tmp_907(7, 0)
        arrayRegFiles(__tmp_906 + 1.U) := __tmp_907(15, 8)

        val __tmp_908 = SP + 40.U(16.W)
        val __tmp_909 = (SP + 2.U(16.W)).asUInt
        arrayRegFiles(__tmp_908 + 0.U) := __tmp_909(7, 0)
        arrayRegFiles(__tmp_908 + 1.U) := __tmp_909(15, 8)

        val __tmp_910 = SP + 42.U(16.W)
        val __tmp_911 = (3.S(64.W)).asUInt
        arrayRegFiles(__tmp_910 + 0.U) := __tmp_911(7, 0)
        arrayRegFiles(__tmp_910 + 1.U) := __tmp_911(15, 8)
        arrayRegFiles(__tmp_910 + 2.U) := __tmp_911(23, 16)
        arrayRegFiles(__tmp_910 + 3.U) := __tmp_911(31, 24)
        arrayRegFiles(__tmp_910 + 4.U) := __tmp_911(39, 32)
        arrayRegFiles(__tmp_910 + 5.U) := __tmp_911(47, 40)
        arrayRegFiles(__tmp_910 + 6.U) := __tmp_911(55, 48)
        arrayRegFiles(__tmp_910 + 7.U) := __tmp_911(63, 56)

        val __tmp_912 = SP + 50.U(16.W)
        val __tmp_913 = (5.S(64.W)).asUInt
        arrayRegFiles(__tmp_912 + 0.U) := __tmp_913(7, 0)
        arrayRegFiles(__tmp_912 + 1.U) := __tmp_913(15, 8)
        arrayRegFiles(__tmp_912 + 2.U) := __tmp_913(23, 16)
        arrayRegFiles(__tmp_912 + 3.U) := __tmp_913(31, 24)
        arrayRegFiles(__tmp_912 + 4.U) := __tmp_913(39, 32)
        arrayRegFiles(__tmp_912 + 5.U) := __tmp_913(47, 40)
        arrayRegFiles(__tmp_912 + 6.U) := __tmp_913(55, 48)
        arrayRegFiles(__tmp_912 + 7.U) := __tmp_913(63, 56)

        val __tmp_914 = SP - 88.U(16.W)
        val __tmp_915 = (generalRegFiles(0.U)).asUInt
        arrayRegFiles(__tmp_914 + 0.U) := __tmp_915(7, 0)
        arrayRegFiles(__tmp_914 + 1.U) := __tmp_915(15, 8)
        arrayRegFiles(__tmp_914 + 2.U) := __tmp_915(23, 16)
        arrayRegFiles(__tmp_914 + 3.U) := __tmp_915(31, 24)
        arrayRegFiles(__tmp_914 + 4.U) := __tmp_915(39, 32)
        arrayRegFiles(__tmp_914 + 5.U) := __tmp_915(47, 40)
        arrayRegFiles(__tmp_914 + 6.U) := __tmp_915(55, 48)
        arrayRegFiles(__tmp_914 + 7.U) := __tmp_915(63, 56)

        val __tmp_916 = SP - 80.U(16.W)
        val __tmp_917 = (generalRegFiles(1.U)).asUInt
        arrayRegFiles(__tmp_916 + 0.U) := __tmp_917(7, 0)
        arrayRegFiles(__tmp_916 + 1.U) := __tmp_917(15, 8)
        arrayRegFiles(__tmp_916 + 2.U) := __tmp_917(23, 16)
        arrayRegFiles(__tmp_916 + 3.U) := __tmp_917(31, 24)
        arrayRegFiles(__tmp_916 + 4.U) := __tmp_917(39, 32)
        arrayRegFiles(__tmp_916 + 5.U) := __tmp_917(47, 40)
        arrayRegFiles(__tmp_916 + 6.U) := __tmp_917(55, 48)
        arrayRegFiles(__tmp_916 + 7.U) := __tmp_917(63, 56)

        val __tmp_918 = SP - 72.U(16.W)
        val __tmp_919 = (generalRegFiles(2.U)).asUInt
        arrayRegFiles(__tmp_918 + 0.U) := __tmp_919(7, 0)
        arrayRegFiles(__tmp_918 + 1.U) := __tmp_919(15, 8)
        arrayRegFiles(__tmp_918 + 2.U) := __tmp_919(23, 16)
        arrayRegFiles(__tmp_918 + 3.U) := __tmp_919(31, 24)
        arrayRegFiles(__tmp_918 + 4.U) := __tmp_919(39, 32)
        arrayRegFiles(__tmp_918 + 5.U) := __tmp_919(47, 40)
        arrayRegFiles(__tmp_918 + 6.U) := __tmp_919(55, 48)
        arrayRegFiles(__tmp_918 + 7.U) := __tmp_919(63, 56)

        val __tmp_920 = SP - 64.U(16.W)
        val __tmp_921 = (generalRegFiles(3.U)).asUInt
        arrayRegFiles(__tmp_920 + 0.U) := __tmp_921(7, 0)
        arrayRegFiles(__tmp_920 + 1.U) := __tmp_921(15, 8)
        arrayRegFiles(__tmp_920 + 2.U) := __tmp_921(23, 16)
        arrayRegFiles(__tmp_920 + 3.U) := __tmp_921(31, 24)
        arrayRegFiles(__tmp_920 + 4.U) := __tmp_921(39, 32)
        arrayRegFiles(__tmp_920 + 5.U) := __tmp_921(47, 40)
        arrayRegFiles(__tmp_920 + 6.U) := __tmp_921(55, 48)
        arrayRegFiles(__tmp_920 + 7.U) := __tmp_921(63, 56)

        val __tmp_922 = SP - 56.U(16.W)
        val __tmp_923 = (generalRegFiles(4.U)).asUInt
        arrayRegFiles(__tmp_922 + 0.U) := __tmp_923(7, 0)
        arrayRegFiles(__tmp_922 + 1.U) := __tmp_923(15, 8)
        arrayRegFiles(__tmp_922 + 2.U) := __tmp_923(23, 16)
        arrayRegFiles(__tmp_922 + 3.U) := __tmp_923(31, 24)
        arrayRegFiles(__tmp_922 + 4.U) := __tmp_923(39, 32)
        arrayRegFiles(__tmp_922 + 5.U) := __tmp_923(47, 40)
        arrayRegFiles(__tmp_922 + 6.U) := __tmp_923(55, 48)
        arrayRegFiles(__tmp_922 + 7.U) := __tmp_923(63, 56)

        val __tmp_924 = SP - 48.U(16.W)
        val __tmp_925 = (generalRegFiles(5.U)).asUInt
        arrayRegFiles(__tmp_924 + 0.U) := __tmp_925(7, 0)
        arrayRegFiles(__tmp_924 + 1.U) := __tmp_925(15, 8)
        arrayRegFiles(__tmp_924 + 2.U) := __tmp_925(23, 16)
        arrayRegFiles(__tmp_924 + 3.U) := __tmp_925(31, 24)
        arrayRegFiles(__tmp_924 + 4.U) := __tmp_925(39, 32)
        arrayRegFiles(__tmp_924 + 5.U) := __tmp_925(47, 40)
        arrayRegFiles(__tmp_924 + 6.U) := __tmp_925(55, 48)
        arrayRegFiles(__tmp_924 + 7.U) := __tmp_925(63, 56)

        val __tmp_926 = SP - 40.U(16.W)
        val __tmp_927 = (generalRegFiles(6.U)).asUInt
        arrayRegFiles(__tmp_926 + 0.U) := __tmp_927(7, 0)
        arrayRegFiles(__tmp_926 + 1.U) := __tmp_927(15, 8)
        arrayRegFiles(__tmp_926 + 2.U) := __tmp_927(23, 16)
        arrayRegFiles(__tmp_926 + 3.U) := __tmp_927(31, 24)
        arrayRegFiles(__tmp_926 + 4.U) := __tmp_927(39, 32)
        arrayRegFiles(__tmp_926 + 5.U) := __tmp_927(47, 40)
        arrayRegFiles(__tmp_926 + 6.U) := __tmp_927(55, 48)
        arrayRegFiles(__tmp_926 + 7.U) := __tmp_927(63, 56)

        val __tmp_928 = SP - 32.U(16.W)
        val __tmp_929 = (generalRegFiles(7.U)).asUInt
        arrayRegFiles(__tmp_928 + 0.U) := __tmp_929(7, 0)
        arrayRegFiles(__tmp_928 + 1.U) := __tmp_929(15, 8)
        arrayRegFiles(__tmp_928 + 2.U) := __tmp_929(23, 16)
        arrayRegFiles(__tmp_928 + 3.U) := __tmp_929(31, 24)
        arrayRegFiles(__tmp_928 + 4.U) := __tmp_929(39, 32)
        arrayRegFiles(__tmp_928 + 5.U) := __tmp_929(47, 40)
        arrayRegFiles(__tmp_928 + 6.U) := __tmp_929(55, 48)
        arrayRegFiles(__tmp_928 + 7.U) := __tmp_929(63, 56)

        val __tmp_930 = SP - 24.U(16.W)
        val __tmp_931 = (generalRegFiles(8.U)).asUInt
        arrayRegFiles(__tmp_930 + 0.U) := __tmp_931(7, 0)
        arrayRegFiles(__tmp_930 + 1.U) := __tmp_931(15, 8)
        arrayRegFiles(__tmp_930 + 2.U) := __tmp_931(23, 16)
        arrayRegFiles(__tmp_930 + 3.U) := __tmp_931(31, 24)
        arrayRegFiles(__tmp_930 + 4.U) := __tmp_931(39, 32)
        arrayRegFiles(__tmp_930 + 5.U) := __tmp_931(47, 40)
        arrayRegFiles(__tmp_930 + 6.U) := __tmp_931(55, 48)
        arrayRegFiles(__tmp_930 + 7.U) := __tmp_931(63, 56)

        val __tmp_932 = SP - 16.U(16.W)
        val __tmp_933 = (generalRegFiles(9.U)).asUInt
        arrayRegFiles(__tmp_932 + 0.U) := __tmp_933(7, 0)
        arrayRegFiles(__tmp_932 + 1.U) := __tmp_933(15, 8)
        arrayRegFiles(__tmp_932 + 2.U) := __tmp_933(23, 16)
        arrayRegFiles(__tmp_932 + 3.U) := __tmp_933(31, 24)
        arrayRegFiles(__tmp_932 + 4.U) := __tmp_933(39, 32)
        arrayRegFiles(__tmp_932 + 5.U) := __tmp_933(47, 40)
        arrayRegFiles(__tmp_932 + 6.U) := __tmp_933(55, 48)
        arrayRegFiles(__tmp_932 + 7.U) := __tmp_933(63, 56)

        val __tmp_934 = SP - 8.U(16.W)
        val __tmp_935 = (generalRegFiles(10.U)).asUInt
        arrayRegFiles(__tmp_934 + 0.U) := __tmp_935(7, 0)
        arrayRegFiles(__tmp_934 + 1.U) := __tmp_935(15, 8)
        arrayRegFiles(__tmp_934 + 2.U) := __tmp_935(23, 16)
        arrayRegFiles(__tmp_934 + 3.U) := __tmp_935(31, 24)
        arrayRegFiles(__tmp_934 + 4.U) := __tmp_935(39, 32)
        arrayRegFiles(__tmp_934 + 5.U) := __tmp_935(47, 40)
        arrayRegFiles(__tmp_934 + 6.U) := __tmp_935(55, 48)
        arrayRegFiles(__tmp_934 + 7.U) := __tmp_935(63, 56)

        CP := 13.U
      }

      is(13.U) {
        /*
        *(SP + (8 [SP])) = (3069765878 [U32]) [unsigned, U32, 4]  // $sfDesc.type = 0xB6F8E8F6
        *(SP + (12 [SP])) = (20 [Z]) [signed, Z, 8]  // $sfDesc.size = 20
        *(SP + (20 [SP])) = (98 [U8]) [unsigned, U8, 1]  // 'b'
        *(SP + (21 [SP])) = (97 [U8]) [unsigned, U8, 1]  // 'a'
        *(SP + (22 [SP])) = (114 [U8]) [unsigned, U8, 1]  // 'r'
        *(SP + (23 [SP])) = (32 [U8]) [unsigned, U8, 1]  // ' '
        *(SP + (24 [SP])) = (40 [U8]) [unsigned, U8, 1]  // '('
        *(SP + (25 [SP])) = (97 [U8]) [unsigned, U8, 1]  // 'a'
        *(SP + (26 [SP])) = (115 [U8]) [unsigned, U8, 1]  // 's'
        *(SP + (27 [SP])) = (115 [U8]) [unsigned, U8, 1]  // 's'
        *(SP + (28 [SP])) = (101 [U8]) [unsigned, U8, 1]  // 'e'
        *(SP + (29 [SP])) = (114 [U8]) [unsigned, U8, 1]  // 'r'
        *(SP + (30 [SP])) = (116 [U8]) [unsigned, U8, 1]  // 't'
        *(SP + (31 [SP])) = (45 [U8]) [unsigned, U8, 1]  // '-'
        *(SP + (32 [SP])) = (116 [U8]) [unsigned, U8, 1]  // 't'
        *(SP + (33 [SP])) = (101 [U8]) [unsigned, U8, 1]  // 'e'
        *(SP + (34 [SP])) = (115 [U8]) [unsigned, U8, 1]  // 's'
        *(SP + (35 [SP])) = (116 [U8]) [unsigned, U8, 1]  // 't'
        *(SP + (36 [SP])) = (46 [U8]) [unsigned, U8, 1]  // '.'
        *(SP + (37 [SP])) = (115 [U8]) [unsigned, U8, 1]  // 's'
        *(SP + (38 [SP])) = (99 [U8]) [unsigned, U8, 1]  // 'c'
        *(SP + (39 [SP])) = (58 [U8]) [unsigned, U8, 1]  // ':'
        *(SP + (4 [SP])) = (6 [U32]) [signed, U32, 4]  // $sfLoc = (6 [U32])
        goto .14
        */


        val __tmp_936 = SP + 8.U(16.W)
        val __tmp_937 = (3069765878L.U(32.W)).asUInt
        arrayRegFiles(__tmp_936 + 0.U) := __tmp_937(7, 0)
        arrayRegFiles(__tmp_936 + 1.U) := __tmp_937(15, 8)
        arrayRegFiles(__tmp_936 + 2.U) := __tmp_937(23, 16)
        arrayRegFiles(__tmp_936 + 3.U) := __tmp_937(31, 24)

        val __tmp_938 = SP + 12.U(16.W)
        val __tmp_939 = (20.S(64.W)).asUInt
        arrayRegFiles(__tmp_938 + 0.U) := __tmp_939(7, 0)
        arrayRegFiles(__tmp_938 + 1.U) := __tmp_939(15, 8)
        arrayRegFiles(__tmp_938 + 2.U) := __tmp_939(23, 16)
        arrayRegFiles(__tmp_938 + 3.U) := __tmp_939(31, 24)
        arrayRegFiles(__tmp_938 + 4.U) := __tmp_939(39, 32)
        arrayRegFiles(__tmp_938 + 5.U) := __tmp_939(47, 40)
        arrayRegFiles(__tmp_938 + 6.U) := __tmp_939(55, 48)
        arrayRegFiles(__tmp_938 + 7.U) := __tmp_939(63, 56)

        val __tmp_940 = SP + 20.U(16.W)
        val __tmp_941 = (98.U(8.W)).asUInt
        arrayRegFiles(__tmp_940 + 0.U) := __tmp_941(7, 0)

        val __tmp_942 = SP + 21.U(16.W)
        val __tmp_943 = (97.U(8.W)).asUInt
        arrayRegFiles(__tmp_942 + 0.U) := __tmp_943(7, 0)

        val __tmp_944 = SP + 22.U(16.W)
        val __tmp_945 = (114.U(8.W)).asUInt
        arrayRegFiles(__tmp_944 + 0.U) := __tmp_945(7, 0)

        val __tmp_946 = SP + 23.U(16.W)
        val __tmp_947 = (32.U(8.W)).asUInt
        arrayRegFiles(__tmp_946 + 0.U) := __tmp_947(7, 0)

        val __tmp_948 = SP + 24.U(16.W)
        val __tmp_949 = (40.U(8.W)).asUInt
        arrayRegFiles(__tmp_948 + 0.U) := __tmp_949(7, 0)

        val __tmp_950 = SP + 25.U(16.W)
        val __tmp_951 = (97.U(8.W)).asUInt
        arrayRegFiles(__tmp_950 + 0.U) := __tmp_951(7, 0)

        val __tmp_952 = SP + 26.U(16.W)
        val __tmp_953 = (115.U(8.W)).asUInt
        arrayRegFiles(__tmp_952 + 0.U) := __tmp_953(7, 0)

        val __tmp_954 = SP + 27.U(16.W)
        val __tmp_955 = (115.U(8.W)).asUInt
        arrayRegFiles(__tmp_954 + 0.U) := __tmp_955(7, 0)

        val __tmp_956 = SP + 28.U(16.W)
        val __tmp_957 = (101.U(8.W)).asUInt
        arrayRegFiles(__tmp_956 + 0.U) := __tmp_957(7, 0)

        val __tmp_958 = SP + 29.U(16.W)
        val __tmp_959 = (114.U(8.W)).asUInt
        arrayRegFiles(__tmp_958 + 0.U) := __tmp_959(7, 0)

        val __tmp_960 = SP + 30.U(16.W)
        val __tmp_961 = (116.U(8.W)).asUInt
        arrayRegFiles(__tmp_960 + 0.U) := __tmp_961(7, 0)

        val __tmp_962 = SP + 31.U(16.W)
        val __tmp_963 = (45.U(8.W)).asUInt
        arrayRegFiles(__tmp_962 + 0.U) := __tmp_963(7, 0)

        val __tmp_964 = SP + 32.U(16.W)
        val __tmp_965 = (116.U(8.W)).asUInt
        arrayRegFiles(__tmp_964 + 0.U) := __tmp_965(7, 0)

        val __tmp_966 = SP + 33.U(16.W)
        val __tmp_967 = (101.U(8.W)).asUInt
        arrayRegFiles(__tmp_966 + 0.U) := __tmp_967(7, 0)

        val __tmp_968 = SP + 34.U(16.W)
        val __tmp_969 = (115.U(8.W)).asUInt
        arrayRegFiles(__tmp_968 + 0.U) := __tmp_969(7, 0)

        val __tmp_970 = SP + 35.U(16.W)
        val __tmp_971 = (116.U(8.W)).asUInt
        arrayRegFiles(__tmp_970 + 0.U) := __tmp_971(7, 0)

        val __tmp_972 = SP + 36.U(16.W)
        val __tmp_973 = (46.U(8.W)).asUInt
        arrayRegFiles(__tmp_972 + 0.U) := __tmp_973(7, 0)

        val __tmp_974 = SP + 37.U(16.W)
        val __tmp_975 = (115.U(8.W)).asUInt
        arrayRegFiles(__tmp_974 + 0.U) := __tmp_975(7, 0)

        val __tmp_976 = SP + 38.U(16.W)
        val __tmp_977 = (99.U(8.W)).asUInt
        arrayRegFiles(__tmp_976 + 0.U) := __tmp_977(7, 0)

        val __tmp_978 = SP + 39.U(16.W)
        val __tmp_979 = (58.U(8.W)).asUInt
        arrayRegFiles(__tmp_978 + 0.U) := __tmp_979(7, 0)

        val __tmp_980 = SP + 4.U(16.W)
        val __tmp_981 = (6.S(32.W)).asUInt
        arrayRegFiles(__tmp_980 + 0.U) := __tmp_981(7, 0)
        arrayRegFiles(__tmp_980 + 1.U) := __tmp_981(15, 8)
        arrayRegFiles(__tmp_980 + 2.U) := __tmp_981(23, 16)
        arrayRegFiles(__tmp_980 + 3.U) := __tmp_981(31, 24)

        CP := 14.U
      }

      is(14.U) {
        /*
        $0 = *(SP + (42 [SP])) [signed, Z, 8]  // $0 = x
        $1 = *(SP + (50 [SP])) [signed, Z, 8]  // $1 = y
        goto .15
        */


        val __tmp_982 = (SP + 42.U(16.W)).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_982 + 7.U),
          arrayRegFiles(__tmp_982 + 6.U),
          arrayRegFiles(__tmp_982 + 5.U),
          arrayRegFiles(__tmp_982 + 4.U),
          arrayRegFiles(__tmp_982 + 3.U),
          arrayRegFiles(__tmp_982 + 2.U),
          arrayRegFiles(__tmp_982 + 1.U),
          arrayRegFiles(__tmp_982 + 0.U)
        ).asUInt

        val __tmp_983 = (SP + 50.U(16.W)).asUInt
        generalRegFiles(1.U) := Cat(
          arrayRegFiles(__tmp_983 + 7.U),
          arrayRegFiles(__tmp_983 + 6.U),
          arrayRegFiles(__tmp_983 + 5.U),
          arrayRegFiles(__tmp_983 + 4.U),
          arrayRegFiles(__tmp_983 + 3.U),
          arrayRegFiles(__tmp_983 + 2.U),
          arrayRegFiles(__tmp_983 + 1.U),
          arrayRegFiles(__tmp_983 + 0.U)
        ).asUInt

        CP := 15.U
      }

      is(15.U) {
        /*
        $2 = ($0 ≡ $1)
        goto .16
        */


        generalRegFiles(2.U) := (generalRegFiles(0.U).asSInt === generalRegFiles(1.U).asSInt).asUInt
        CP := 16.U
      }

      is(16.U) {
        /*
        if $2 goto .17 else goto .18
        */


        CP := Mux((generalRegFiles(2.U).asUInt) === 1.U, 17.U, 18.U)
      }

      is(17.U) {
        /*
        *(SP + (4 [SP])) = (5 [U32]) [signed, U32, 4]  // $sfLoc = (5 [U32])
        goto $ret@0
        */


        val __tmp_984 = SP + 4.U(16.W)
        val __tmp_985 = (5.S(32.W)).asUInt
        arrayRegFiles(__tmp_984 + 0.U) := __tmp_985(7, 0)
        arrayRegFiles(__tmp_984 + 1.U) := __tmp_985(15, 8)
        arrayRegFiles(__tmp_984 + 2.U) := __tmp_985(23, 16)
        arrayRegFiles(__tmp_984 + 3.U) := __tmp_985(31, 24)

        CP := Cat(
          arrayRegFiles(SP + 0.U + 1.U),
          arrayRegFiles(SP + 0.U + 0.U)
        )

      }

      is(18.U) {
        /*
        *(SP + (4 [SP])) = (6 [U32]) [signed, U32, 4]  // $sfLoc = (6 [U32])
        goto .19
        */


        val __tmp_986 = SP + 4.U(16.W)
        val __tmp_987 = (6.S(32.W)).asUInt
        arrayRegFiles(__tmp_986 + 0.U) := __tmp_987(7, 0)
        arrayRegFiles(__tmp_986 + 1.U) := __tmp_987(15, 8)
        arrayRegFiles(__tmp_986 + 2.U) := __tmp_987(23, 16)
        arrayRegFiles(__tmp_986 + 3.U) := __tmp_987(31, 24)

        CP := 19.U
      }

      is(19.U) {
        /*
        *((*(22 [SP]) + (12 [SP])) + ((DP & (127 [DP])) as SP)) = (120 [U8]) [unsigned, U8, 1]  // $display((DP & (127 [DP]))) = (120 [U8])
        *((*(22 [SP]) + (12 [SP])) + (((DP + (1 [DP])) & (127 [DP])) as SP)) = (32 [U8]) [unsigned, U8, 1]  // $display(((DP + (1 [DP])) & (127 [DP]))) = (32 [U8])
        *((*(22 [SP]) + (12 [SP])) + (((DP + (2 [DP])) & (127 [DP])) as SP)) = (105 [U8]) [unsigned, U8, 1]  // $display(((DP + (2 [DP])) & (127 [DP]))) = (105 [U8])
        *((*(22 [SP]) + (12 [SP])) + (((DP + (3 [DP])) & (127 [DP])) as SP)) = (115 [U8]) [unsigned, U8, 1]  // $display(((DP + (3 [DP])) & (127 [DP]))) = (115 [U8])
        *((*(22 [SP]) + (12 [SP])) + (((DP + (4 [DP])) & (127 [DP])) as SP)) = (32 [U8]) [unsigned, U8, 1]  // $display(((DP + (4 [DP])) & (127 [DP]))) = (32 [U8])
        *((*(22 [SP]) + (12 [SP])) + (((DP + (5 [DP])) & (127 [DP])) as SP)) = (110 [U8]) [unsigned, U8, 1]  // $display(((DP + (5 [DP])) & (127 [DP]))) = (110 [U8])
        *((*(22 [SP]) + (12 [SP])) + (((DP + (6 [DP])) & (127 [DP])) as SP)) = (111 [U8]) [unsigned, U8, 1]  // $display(((DP + (6 [DP])) & (127 [DP]))) = (111 [U8])
        *((*(22 [SP]) + (12 [SP])) + (((DP + (7 [DP])) & (127 [DP])) as SP)) = (116 [U8]) [unsigned, U8, 1]  // $display(((DP + (7 [DP])) & (127 [DP]))) = (116 [U8])
        *((*(22 [SP]) + (12 [SP])) + (((DP + (8 [DP])) & (127 [DP])) as SP)) = (32 [U8]) [unsigned, U8, 1]  // $display(((DP + (8 [DP])) & (127 [DP]))) = (32 [U8])
        *((*(22 [SP]) + (12 [SP])) + (((DP + (9 [DP])) & (127 [DP])) as SP)) = (101 [U8]) [unsigned, U8, 1]  // $display(((DP + (9 [DP])) & (127 [DP]))) = (101 [U8])
        *((*(22 [SP]) + (12 [SP])) + (((DP + (10 [DP])) & (127 [DP])) as SP)) = (113 [U8]) [unsigned, U8, 1]  // $display(((DP + (10 [DP])) & (127 [DP]))) = (113 [U8])
        *((*(22 [SP]) + (12 [SP])) + (((DP + (11 [DP])) & (127 [DP])) as SP)) = (117 [U8]) [unsigned, U8, 1]  // $display(((DP + (11 [DP])) & (127 [DP]))) = (117 [U8])
        *((*(22 [SP]) + (12 [SP])) + (((DP + (12 [DP])) & (127 [DP])) as SP)) = (97 [U8]) [unsigned, U8, 1]  // $display(((DP + (12 [DP])) & (127 [DP]))) = (97 [U8])
        *((*(22 [SP]) + (12 [SP])) + (((DP + (13 [DP])) & (127 [DP])) as SP)) = (108 [U8]) [unsigned, U8, 1]  // $display(((DP + (13 [DP])) & (127 [DP]))) = (108 [U8])
        *((*(22 [SP]) + (12 [SP])) + (((DP + (14 [DP])) & (127 [DP])) as SP)) = (32 [U8]) [unsigned, U8, 1]  // $display(((DP + (14 [DP])) & (127 [DP]))) = (32 [U8])
        *((*(22 [SP]) + (12 [SP])) + (((DP + (15 [DP])) & (127 [DP])) as SP)) = (116 [U8]) [unsigned, U8, 1]  // $display(((DP + (15 [DP])) & (127 [DP]))) = (116 [U8])
        *((*(22 [SP]) + (12 [SP])) + (((DP + (16 [DP])) & (127 [DP])) as SP)) = (111 [U8]) [unsigned, U8, 1]  // $display(((DP + (16 [DP])) & (127 [DP]))) = (111 [U8])
        *((*(22 [SP]) + (12 [SP])) + (((DP + (17 [DP])) & (127 [DP])) as SP)) = (32 [U8]) [unsigned, U8, 1]  // $display(((DP + (17 [DP])) & (127 [DP]))) = (32 [U8])
        *((*(22 [SP]) + (12 [SP])) + (((DP + (18 [DP])) & (127 [DP])) as SP)) = (121 [U8]) [unsigned, U8, 1]  // $display(((DP + (18 [DP])) & (127 [DP]))) = (121 [U8])
        goto .20
        */


        val __tmp_988 = Cat(
          arrayRegFiles(22.U(16.W) + 1.U),
          arrayRegFiles(22.U(16.W) + 0.U)
        ) + 12.U(16.W) + DP & 127.U(64.W).asUInt
        val __tmp_989 = (120.U(8.W)).asUInt
        arrayRegFiles(__tmp_988 + 0.U) := __tmp_989(7, 0)

        val __tmp_990 = Cat(
          arrayRegFiles(22.U(16.W) + 1.U),
          arrayRegFiles(22.U(16.W) + 0.U)
        ) + 12.U(16.W) + DP + 1.U(64.W) & 127.U(64.W).asUInt
        val __tmp_991 = (32.U(8.W)).asUInt
        arrayRegFiles(__tmp_990 + 0.U) := __tmp_991(7, 0)

        val __tmp_992 = Cat(
          arrayRegFiles(22.U(16.W) + 1.U),
          arrayRegFiles(22.U(16.W) + 0.U)
        ) + 12.U(16.W) + DP + 2.U(64.W) & 127.U(64.W).asUInt
        val __tmp_993 = (105.U(8.W)).asUInt
        arrayRegFiles(__tmp_992 + 0.U) := __tmp_993(7, 0)

        val __tmp_994 = Cat(
          arrayRegFiles(22.U(16.W) + 1.U),
          arrayRegFiles(22.U(16.W) + 0.U)
        ) + 12.U(16.W) + DP + 3.U(64.W) & 127.U(64.W).asUInt
        val __tmp_995 = (115.U(8.W)).asUInt
        arrayRegFiles(__tmp_994 + 0.U) := __tmp_995(7, 0)

        val __tmp_996 = Cat(
          arrayRegFiles(22.U(16.W) + 1.U),
          arrayRegFiles(22.U(16.W) + 0.U)
        ) + 12.U(16.W) + DP + 4.U(64.W) & 127.U(64.W).asUInt
        val __tmp_997 = (32.U(8.W)).asUInt
        arrayRegFiles(__tmp_996 + 0.U) := __tmp_997(7, 0)

        val __tmp_998 = Cat(
          arrayRegFiles(22.U(16.W) + 1.U),
          arrayRegFiles(22.U(16.W) + 0.U)
        ) + 12.U(16.W) + DP + 5.U(64.W) & 127.U(64.W).asUInt
        val __tmp_999 = (110.U(8.W)).asUInt
        arrayRegFiles(__tmp_998 + 0.U) := __tmp_999(7, 0)

        val __tmp_1000 = Cat(
          arrayRegFiles(22.U(16.W) + 1.U),
          arrayRegFiles(22.U(16.W) + 0.U)
        ) + 12.U(16.W) + DP + 6.U(64.W) & 127.U(64.W).asUInt
        val __tmp_1001 = (111.U(8.W)).asUInt
        arrayRegFiles(__tmp_1000 + 0.U) := __tmp_1001(7, 0)

        val __tmp_1002 = Cat(
          arrayRegFiles(22.U(16.W) + 1.U),
          arrayRegFiles(22.U(16.W) + 0.U)
        ) + 12.U(16.W) + DP + 7.U(64.W) & 127.U(64.W).asUInt
        val __tmp_1003 = (116.U(8.W)).asUInt
        arrayRegFiles(__tmp_1002 + 0.U) := __tmp_1003(7, 0)

        val __tmp_1004 = Cat(
          arrayRegFiles(22.U(16.W) + 1.U),
          arrayRegFiles(22.U(16.W) + 0.U)
        ) + 12.U(16.W) + DP + 8.U(64.W) & 127.U(64.W).asUInt
        val __tmp_1005 = (32.U(8.W)).asUInt
        arrayRegFiles(__tmp_1004 + 0.U) := __tmp_1005(7, 0)

        val __tmp_1006 = Cat(
          arrayRegFiles(22.U(16.W) + 1.U),
          arrayRegFiles(22.U(16.W) + 0.U)
        ) + 12.U(16.W) + DP + 9.U(64.W) & 127.U(64.W).asUInt
        val __tmp_1007 = (101.U(8.W)).asUInt
        arrayRegFiles(__tmp_1006 + 0.U) := __tmp_1007(7, 0)

        val __tmp_1008 = Cat(
          arrayRegFiles(22.U(16.W) + 1.U),
          arrayRegFiles(22.U(16.W) + 0.U)
        ) + 12.U(16.W) + DP + 10.U(64.W) & 127.U(64.W).asUInt
        val __tmp_1009 = (113.U(8.W)).asUInt
        arrayRegFiles(__tmp_1008 + 0.U) := __tmp_1009(7, 0)

        val __tmp_1010 = Cat(
          arrayRegFiles(22.U(16.W) + 1.U),
          arrayRegFiles(22.U(16.W) + 0.U)
        ) + 12.U(16.W) + DP + 11.U(64.W) & 127.U(64.W).asUInt
        val __tmp_1011 = (117.U(8.W)).asUInt
        arrayRegFiles(__tmp_1010 + 0.U) := __tmp_1011(7, 0)

        val __tmp_1012 = Cat(
          arrayRegFiles(22.U(16.W) + 1.U),
          arrayRegFiles(22.U(16.W) + 0.U)
        ) + 12.U(16.W) + DP + 12.U(64.W) & 127.U(64.W).asUInt
        val __tmp_1013 = (97.U(8.W)).asUInt
        arrayRegFiles(__tmp_1012 + 0.U) := __tmp_1013(7, 0)

        val __tmp_1014 = Cat(
          arrayRegFiles(22.U(16.W) + 1.U),
          arrayRegFiles(22.U(16.W) + 0.U)
        ) + 12.U(16.W) + DP + 13.U(64.W) & 127.U(64.W).asUInt
        val __tmp_1015 = (108.U(8.W)).asUInt
        arrayRegFiles(__tmp_1014 + 0.U) := __tmp_1015(7, 0)

        val __tmp_1016 = Cat(
          arrayRegFiles(22.U(16.W) + 1.U),
          arrayRegFiles(22.U(16.W) + 0.U)
        ) + 12.U(16.W) + DP + 14.U(64.W) & 127.U(64.W).asUInt
        val __tmp_1017 = (32.U(8.W)).asUInt
        arrayRegFiles(__tmp_1016 + 0.U) := __tmp_1017(7, 0)

        val __tmp_1018 = Cat(
          arrayRegFiles(22.U(16.W) + 1.U),
          arrayRegFiles(22.U(16.W) + 0.U)
        ) + 12.U(16.W) + DP + 15.U(64.W) & 127.U(64.W).asUInt
        val __tmp_1019 = (116.U(8.W)).asUInt
        arrayRegFiles(__tmp_1018 + 0.U) := __tmp_1019(7, 0)

        val __tmp_1020 = Cat(
          arrayRegFiles(22.U(16.W) + 1.U),
          arrayRegFiles(22.U(16.W) + 0.U)
        ) + 12.U(16.W) + DP + 16.U(64.W) & 127.U(64.W).asUInt
        val __tmp_1021 = (111.U(8.W)).asUInt
        arrayRegFiles(__tmp_1020 + 0.U) := __tmp_1021(7, 0)

        val __tmp_1022 = Cat(
          arrayRegFiles(22.U(16.W) + 1.U),
          arrayRegFiles(22.U(16.W) + 0.U)
        ) + 12.U(16.W) + DP + 17.U(64.W) & 127.U(64.W).asUInt
        val __tmp_1023 = (32.U(8.W)).asUInt
        arrayRegFiles(__tmp_1022 + 0.U) := __tmp_1023(7, 0)

        val __tmp_1024 = Cat(
          arrayRegFiles(22.U(16.W) + 1.U),
          arrayRegFiles(22.U(16.W) + 0.U)
        ) + 12.U(16.W) + DP + 18.U(64.W) & 127.U(64.W).asUInt
        val __tmp_1025 = (121.U(8.W)).asUInt
        arrayRegFiles(__tmp_1024 + 0.U) := __tmp_1025(7, 0)

        CP := 20.U
      }

      is(20.U) {
        /*
        DP = DP + 19
        goto .21
        */


        DP := DP + 19.U

        CP := 21.U
      }

      is(21.U) {
        /*
        *((*(22 [SP]) + (12 [SP])) + ((DP & (127 [DP])) as SP)) = (10 [U8]) [unsigned, U8, 1]  // $display((DP & (127 [DP]))) = (10 [U8])
        goto .22
        */


        val __tmp_1026 = Cat(
          arrayRegFiles(22.U(16.W) + 1.U),
          arrayRegFiles(22.U(16.W) + 0.U)
        ) + 12.U(16.W) + DP & 127.U(64.W).asUInt
        val __tmp_1027 = (10.U(8.W)).asUInt
        arrayRegFiles(__tmp_1026 + 0.U) := __tmp_1027(7, 0)

        CP := 22.U
      }

      is(22.U) {
        /*
        DP = DP + 1
        goto .23
        */


        DP := DP + 1.U

        CP := 23.U
      }

      is(23.U) {
        /*
        *(SP + (4 [SP])) = (5 [U32]) [signed, U32, 4]  // $sfLoc = (5 [U32])
        goto .24
        */


        val __tmp_1028 = SP + 4.U(16.W)
        val __tmp_1029 = (5.S(32.W)).asUInt
        arrayRegFiles(__tmp_1028 + 0.U) := __tmp_1029(7, 0)
        arrayRegFiles(__tmp_1028 + 1.U) := __tmp_1029(15, 8)
        arrayRegFiles(__tmp_1028 + 2.U) := __tmp_1029(23, 16)
        arrayRegFiles(__tmp_1028 + 3.U) := __tmp_1029(31, 24)

        CP := 24.U
      }

      is(24.U) {
        /*
        $0 = (*(SP + (40 [SP])) as anvil.PrinterIndex.U)
        alloc printStackTrace$res@[5,16].F35C27EB: U64 [@58, 8]
        goto .25
        */


        generalRegFiles(0.U) := Cat(
          arrayRegFiles(SP + 40.U(16.W) + 1.U),
          arrayRegFiles(SP + 40.U(16.W) + 0.U)
        ).asUInt
        CP := 25.U
      }

      is(25.U) {
        /*
        SP = SP + 74
        goto .26
        */


        SP := SP + 74.U

        CP := 26.U
      }

      is(26.U) {
        /*
        decl $ret: CP [@0, 2], $res: SP [@2, 2], $sfCaller: SP [@4, 2], $sfLoc: U32 [@6, 4], $sfDesc: IS[56, U8] [@10, 68], $sfCurrentId: SP [@78, 2], buffer: SP [@80, 2], index: anvil.PrinterIndex.U [@82, 8], memory: SP [@90, 2], mask: anvil.PrinterIndex.U [@92, 8], spSize: anvil.PrinterIndex.U [@100, 8], typeShaSize: anvil.PrinterIndex.U [@108, 8], locSize: anvil.PrinterIndex.U [@116, 8], sizeSize: anvil.PrinterIndex.U [@124, 8], sfCallerOffset: anvil.PrinterIndex.U [@132, 8]
        *SP = (105 [CP]) [unsigned, CP, 2]  // $ret@0 = 2225
        *(SP + (2 [SP])) = (SP - (16 [SP])) [unsigned, SP, 2]  // $res@2 = -16
        *(SP + (4 [SP])) = (SP - (72 [SP])) [unsigned, SP, 2]  // $sfCaller@4 = -72
        *(SP + (78 [SP])) = (SP + (4 [SP])) [unsigned, SP, 2]  // $sfCurrentId@78 = 4
        *(SP + (80 [SP])) = *(22 [SP]) [unsigned, SP, 2]  // buffer = *(22 [SP])
        *(SP + (82 [SP])) = DP [unsigned, anvil.PrinterIndex.U, 8]  // index = DP
        *(SP + (90 [SP])) = (0 [SP]) [unsigned, SP, 2]  // memory = (0 [SP])
        *(SP + (92 [SP])) = (127 [DP]) [unsigned, anvil.PrinterIndex.U, 8]  // mask = (127 [DP])
        *(SP + (100 [SP])) = (2 [anvil.PrinterIndex.U]) [unsigned, anvil.PrinterIndex.U, 8]  // spSize = (2 [anvil.PrinterIndex.U])
        *(SP + (108 [SP])) = (4 [anvil.PrinterIndex.U]) [unsigned, anvil.PrinterIndex.U, 8]  // typeShaSize = (4 [anvil.PrinterIndex.U])
        *(SP + (116 [SP])) = (4 [anvil.PrinterIndex.U]) [unsigned, anvil.PrinterIndex.U, 8]  // locSize = (4 [anvil.PrinterIndex.U])
        *(SP + (124 [SP])) = (8 [anvil.PrinterIndex.U]) [unsigned, anvil.PrinterIndex.U, 8]  // sizeSize = (8 [anvil.PrinterIndex.U])
        *(SP + (132 [SP])) = $0 [unsigned, anvil.PrinterIndex.U, 8]  // sfCallerOffset = $0
        *(SP - (8 [SP])) = $0 [unsigned, U64, 8]  // save $0
        goto .27
        */


        val __tmp_1030 = SP
        val __tmp_1031 = (105.U(16.W)).asUInt
        arrayRegFiles(__tmp_1030 + 0.U) := __tmp_1031(7, 0)
        arrayRegFiles(__tmp_1030 + 1.U) := __tmp_1031(15, 8)

        val __tmp_1032 = SP + 2.U(16.W)
        val __tmp_1033 = (SP - 16.U(16.W)).asUInt
        arrayRegFiles(__tmp_1032 + 0.U) := __tmp_1033(7, 0)
        arrayRegFiles(__tmp_1032 + 1.U) := __tmp_1033(15, 8)

        val __tmp_1034 = SP + 4.U(16.W)
        val __tmp_1035 = (SP - 72.U(16.W)).asUInt
        arrayRegFiles(__tmp_1034 + 0.U) := __tmp_1035(7, 0)
        arrayRegFiles(__tmp_1034 + 1.U) := __tmp_1035(15, 8)

        val __tmp_1036 = SP + 78.U(16.W)
        val __tmp_1037 = (SP + 4.U(16.W)).asUInt
        arrayRegFiles(__tmp_1036 + 0.U) := __tmp_1037(7, 0)
        arrayRegFiles(__tmp_1036 + 1.U) := __tmp_1037(15, 8)

        val __tmp_1038 = SP + 80.U(16.W)
        val __tmp_1039 = (Cat(
          arrayRegFiles(22.U(16.W) + 1.U),
          arrayRegFiles(22.U(16.W) + 0.U)
        )).asUInt
        arrayRegFiles(__tmp_1038 + 0.U) := __tmp_1039(7, 0)
        arrayRegFiles(__tmp_1038 + 1.U) := __tmp_1039(15, 8)

        val __tmp_1040 = SP + 82.U(16.W)
        val __tmp_1041 = (DP).asUInt
        arrayRegFiles(__tmp_1040 + 0.U) := __tmp_1041(7, 0)
        arrayRegFiles(__tmp_1040 + 1.U) := __tmp_1041(15, 8)
        arrayRegFiles(__tmp_1040 + 2.U) := __tmp_1041(23, 16)
        arrayRegFiles(__tmp_1040 + 3.U) := __tmp_1041(31, 24)
        arrayRegFiles(__tmp_1040 + 4.U) := __tmp_1041(39, 32)
        arrayRegFiles(__tmp_1040 + 5.U) := __tmp_1041(47, 40)
        arrayRegFiles(__tmp_1040 + 6.U) := __tmp_1041(55, 48)
        arrayRegFiles(__tmp_1040 + 7.U) := __tmp_1041(63, 56)

        val __tmp_1042 = SP + 90.U(16.W)
        val __tmp_1043 = (0.U(16.W)).asUInt
        arrayRegFiles(__tmp_1042 + 0.U) := __tmp_1043(7, 0)
        arrayRegFiles(__tmp_1042 + 1.U) := __tmp_1043(15, 8)

        val __tmp_1044 = SP + 92.U(16.W)
        val __tmp_1045 = (127.U(64.W)).asUInt
        arrayRegFiles(__tmp_1044 + 0.U) := __tmp_1045(7, 0)
        arrayRegFiles(__tmp_1044 + 1.U) := __tmp_1045(15, 8)
        arrayRegFiles(__tmp_1044 + 2.U) := __tmp_1045(23, 16)
        arrayRegFiles(__tmp_1044 + 3.U) := __tmp_1045(31, 24)
        arrayRegFiles(__tmp_1044 + 4.U) := __tmp_1045(39, 32)
        arrayRegFiles(__tmp_1044 + 5.U) := __tmp_1045(47, 40)
        arrayRegFiles(__tmp_1044 + 6.U) := __tmp_1045(55, 48)
        arrayRegFiles(__tmp_1044 + 7.U) := __tmp_1045(63, 56)

        val __tmp_1046 = SP + 100.U(16.W)
        val __tmp_1047 = (2.U(64.W)).asUInt
        arrayRegFiles(__tmp_1046 + 0.U) := __tmp_1047(7, 0)
        arrayRegFiles(__tmp_1046 + 1.U) := __tmp_1047(15, 8)
        arrayRegFiles(__tmp_1046 + 2.U) := __tmp_1047(23, 16)
        arrayRegFiles(__tmp_1046 + 3.U) := __tmp_1047(31, 24)
        arrayRegFiles(__tmp_1046 + 4.U) := __tmp_1047(39, 32)
        arrayRegFiles(__tmp_1046 + 5.U) := __tmp_1047(47, 40)
        arrayRegFiles(__tmp_1046 + 6.U) := __tmp_1047(55, 48)
        arrayRegFiles(__tmp_1046 + 7.U) := __tmp_1047(63, 56)

        val __tmp_1048 = SP + 108.U(16.W)
        val __tmp_1049 = (4.U(64.W)).asUInt
        arrayRegFiles(__tmp_1048 + 0.U) := __tmp_1049(7, 0)
        arrayRegFiles(__tmp_1048 + 1.U) := __tmp_1049(15, 8)
        arrayRegFiles(__tmp_1048 + 2.U) := __tmp_1049(23, 16)
        arrayRegFiles(__tmp_1048 + 3.U) := __tmp_1049(31, 24)
        arrayRegFiles(__tmp_1048 + 4.U) := __tmp_1049(39, 32)
        arrayRegFiles(__tmp_1048 + 5.U) := __tmp_1049(47, 40)
        arrayRegFiles(__tmp_1048 + 6.U) := __tmp_1049(55, 48)
        arrayRegFiles(__tmp_1048 + 7.U) := __tmp_1049(63, 56)

        val __tmp_1050 = SP + 116.U(16.W)
        val __tmp_1051 = (4.U(64.W)).asUInt
        arrayRegFiles(__tmp_1050 + 0.U) := __tmp_1051(7, 0)
        arrayRegFiles(__tmp_1050 + 1.U) := __tmp_1051(15, 8)
        arrayRegFiles(__tmp_1050 + 2.U) := __tmp_1051(23, 16)
        arrayRegFiles(__tmp_1050 + 3.U) := __tmp_1051(31, 24)
        arrayRegFiles(__tmp_1050 + 4.U) := __tmp_1051(39, 32)
        arrayRegFiles(__tmp_1050 + 5.U) := __tmp_1051(47, 40)
        arrayRegFiles(__tmp_1050 + 6.U) := __tmp_1051(55, 48)
        arrayRegFiles(__tmp_1050 + 7.U) := __tmp_1051(63, 56)

        val __tmp_1052 = SP + 124.U(16.W)
        val __tmp_1053 = (8.U(64.W)).asUInt
        arrayRegFiles(__tmp_1052 + 0.U) := __tmp_1053(7, 0)
        arrayRegFiles(__tmp_1052 + 1.U) := __tmp_1053(15, 8)
        arrayRegFiles(__tmp_1052 + 2.U) := __tmp_1053(23, 16)
        arrayRegFiles(__tmp_1052 + 3.U) := __tmp_1053(31, 24)
        arrayRegFiles(__tmp_1052 + 4.U) := __tmp_1053(39, 32)
        arrayRegFiles(__tmp_1052 + 5.U) := __tmp_1053(47, 40)
        arrayRegFiles(__tmp_1052 + 6.U) := __tmp_1053(55, 48)
        arrayRegFiles(__tmp_1052 + 7.U) := __tmp_1053(63, 56)

        val __tmp_1054 = SP + 132.U(16.W)
        val __tmp_1055 = (generalRegFiles(0.U)).asUInt
        arrayRegFiles(__tmp_1054 + 0.U) := __tmp_1055(7, 0)
        arrayRegFiles(__tmp_1054 + 1.U) := __tmp_1055(15, 8)
        arrayRegFiles(__tmp_1054 + 2.U) := __tmp_1055(23, 16)
        arrayRegFiles(__tmp_1054 + 3.U) := __tmp_1055(31, 24)
        arrayRegFiles(__tmp_1054 + 4.U) := __tmp_1055(39, 32)
        arrayRegFiles(__tmp_1054 + 5.U) := __tmp_1055(47, 40)
        arrayRegFiles(__tmp_1054 + 6.U) := __tmp_1055(55, 48)
        arrayRegFiles(__tmp_1054 + 7.U) := __tmp_1055(63, 56)

        val __tmp_1056 = SP - 8.U(16.W)
        val __tmp_1057 = (generalRegFiles(0.U)).asUInt
        arrayRegFiles(__tmp_1056 + 0.U) := __tmp_1057(7, 0)
        arrayRegFiles(__tmp_1056 + 1.U) := __tmp_1057(15, 8)
        arrayRegFiles(__tmp_1056 + 2.U) := __tmp_1057(23, 16)
        arrayRegFiles(__tmp_1056 + 3.U) := __tmp_1057(31, 24)
        arrayRegFiles(__tmp_1056 + 4.U) := __tmp_1057(39, 32)
        arrayRegFiles(__tmp_1056 + 5.U) := __tmp_1057(47, 40)
        arrayRegFiles(__tmp_1056 + 6.U) := __tmp_1057(55, 48)
        arrayRegFiles(__tmp_1056 + 7.U) := __tmp_1057(63, 56)

        CP := 27.U
      }

      is(27.U) {
        /*
        *(SP + (10 [SP])) = (3069765878 [U32]) [unsigned, U32, 4]  // $sfDesc.type = 0xB6F8E8F6
        *(SP + (14 [SP])) = (56 [Z]) [signed, Z, 8]  // $sfDesc.size = 56
        *(SP + (22 [SP])) = (111 [U8]) [unsigned, U8, 1]  // 'o'
        *(SP + (23 [SP])) = (114 [U8]) [unsigned, U8, 1]  // 'r'
        *(SP + (24 [SP])) = (103 [U8]) [unsigned, U8, 1]  // 'g'
        *(SP + (25 [SP])) = (46 [U8]) [unsigned, U8, 1]  // '.'
        *(SP + (26 [SP])) = (115 [U8]) [unsigned, U8, 1]  // 's'
        *(SP + (27 [SP])) = (105 [U8]) [unsigned, U8, 1]  // 'i'
        *(SP + (28 [SP])) = (114 [U8]) [unsigned, U8, 1]  // 'r'
        *(SP + (29 [SP])) = (101 [U8]) [unsigned, U8, 1]  // 'e'
        *(SP + (30 [SP])) = (117 [U8]) [unsigned, U8, 1]  // 'u'
        *(SP + (31 [SP])) = (109 [U8]) [unsigned, U8, 1]  // 'm'
        *(SP + (32 [SP])) = (46 [U8]) [unsigned, U8, 1]  // '.'
        *(SP + (33 [SP])) = (97 [U8]) [unsigned, U8, 1]  // 'a'
        *(SP + (34 [SP])) = (110 [U8]) [unsigned, U8, 1]  // 'n'
        *(SP + (35 [SP])) = (118 [U8]) [unsigned, U8, 1]  // 'v'
        *(SP + (36 [SP])) = (105 [U8]) [unsigned, U8, 1]  // 'i'
        *(SP + (37 [SP])) = (108 [U8]) [unsigned, U8, 1]  // 'l'
        *(SP + (38 [SP])) = (46 [U8]) [unsigned, U8, 1]  // '.'
        *(SP + (39 [SP])) = (82 [U8]) [unsigned, U8, 1]  // 'R'
        *(SP + (40 [SP])) = (117 [U8]) [unsigned, U8, 1]  // 'u'
        *(SP + (41 [SP])) = (110 [U8]) [unsigned, U8, 1]  // 'n'
        *(SP + (42 [SP])) = (116 [U8]) [unsigned, U8, 1]  // 't'
        *(SP + (43 [SP])) = (105 [U8]) [unsigned, U8, 1]  // 'i'
        *(SP + (44 [SP])) = (109 [U8]) [unsigned, U8, 1]  // 'm'
        *(SP + (45 [SP])) = (101 [U8]) [unsigned, U8, 1]  // 'e'
        *(SP + (46 [SP])) = (46 [U8]) [unsigned, U8, 1]  // '.'
        *(SP + (47 [SP])) = (112 [U8]) [unsigned, U8, 1]  // 'p'
        *(SP + (48 [SP])) = (114 [U8]) [unsigned, U8, 1]  // 'r'
        *(SP + (49 [SP])) = (105 [U8]) [unsigned, U8, 1]  // 'i'
        *(SP + (50 [SP])) = (110 [U8]) [unsigned, U8, 1]  // 'n'
        *(SP + (51 [SP])) = (116 [U8]) [unsigned, U8, 1]  // 't'
        *(SP + (52 [SP])) = (83 [U8]) [unsigned, U8, 1]  // 'S'
        *(SP + (53 [SP])) = (116 [U8]) [unsigned, U8, 1]  // 't'
        *(SP + (54 [SP])) = (97 [U8]) [unsigned, U8, 1]  // 'a'
        *(SP + (55 [SP])) = (99 [U8]) [unsigned, U8, 1]  // 'c'
        *(SP + (56 [SP])) = (107 [U8]) [unsigned, U8, 1]  // 'k'
        *(SP + (57 [SP])) = (84 [U8]) [unsigned, U8, 1]  // 'T'
        *(SP + (58 [SP])) = (114 [U8]) [unsigned, U8, 1]  // 'r'
        *(SP + (59 [SP])) = (97 [U8]) [unsigned, U8, 1]  // 'a'
        *(SP + (60 [SP])) = (99 [U8]) [unsigned, U8, 1]  // 'c'
        *(SP + (61 [SP])) = (101 [U8]) [unsigned, U8, 1]  // 'e'
        *(SP + (62 [SP])) = (32 [U8]) [unsigned, U8, 1]  // ' '
        *(SP + (63 [SP])) = (40 [U8]) [unsigned, U8, 1]  // '('
        *(SP + (64 [SP])) = (82 [U8]) [unsigned, U8, 1]  // 'R'
        *(SP + (65 [SP])) = (117 [U8]) [unsigned, U8, 1]  // 'u'
        *(SP + (66 [SP])) = (110 [U8]) [unsigned, U8, 1]  // 'n'
        *(SP + (67 [SP])) = (116 [U8]) [unsigned, U8, 1]  // 't'
        *(SP + (68 [SP])) = (105 [U8]) [unsigned, U8, 1]  // 'i'
        *(SP + (69 [SP])) = (109 [U8]) [unsigned, U8, 1]  // 'm'
        *(SP + (70 [SP])) = (101 [U8]) [unsigned, U8, 1]  // 'e'
        *(SP + (71 [SP])) = (46 [U8]) [unsigned, U8, 1]  // '.'
        *(SP + (72 [SP])) = (115 [U8]) [unsigned, U8, 1]  // 's'
        *(SP + (73 [SP])) = (99 [U8]) [unsigned, U8, 1]  // 'c'
        *(SP + (74 [SP])) = (97 [U8]) [unsigned, U8, 1]  // 'a'
        *(SP + (75 [SP])) = (108 [U8]) [unsigned, U8, 1]  // 'l'
        *(SP + (76 [SP])) = (97 [U8]) [unsigned, U8, 1]  // 'a'
        *(SP + (77 [SP])) = (58 [U8]) [unsigned, U8, 1]  // ':'
        *(SP + (6 [SP])) = (639 [U32]) [signed, U32, 4]  // $sfLoc = (639 [U32])
        goto .28
        */


        val __tmp_1058 = SP + 10.U(16.W)
        val __tmp_1059 = (3069765878L.U(32.W)).asUInt
        arrayRegFiles(__tmp_1058 + 0.U) := __tmp_1059(7, 0)
        arrayRegFiles(__tmp_1058 + 1.U) := __tmp_1059(15, 8)
        arrayRegFiles(__tmp_1058 + 2.U) := __tmp_1059(23, 16)
        arrayRegFiles(__tmp_1058 + 3.U) := __tmp_1059(31, 24)

        val __tmp_1060 = SP + 14.U(16.W)
        val __tmp_1061 = (56.S(64.W)).asUInt
        arrayRegFiles(__tmp_1060 + 0.U) := __tmp_1061(7, 0)
        arrayRegFiles(__tmp_1060 + 1.U) := __tmp_1061(15, 8)
        arrayRegFiles(__tmp_1060 + 2.U) := __tmp_1061(23, 16)
        arrayRegFiles(__tmp_1060 + 3.U) := __tmp_1061(31, 24)
        arrayRegFiles(__tmp_1060 + 4.U) := __tmp_1061(39, 32)
        arrayRegFiles(__tmp_1060 + 5.U) := __tmp_1061(47, 40)
        arrayRegFiles(__tmp_1060 + 6.U) := __tmp_1061(55, 48)
        arrayRegFiles(__tmp_1060 + 7.U) := __tmp_1061(63, 56)

        val __tmp_1062 = SP + 22.U(16.W)
        val __tmp_1063 = (111.U(8.W)).asUInt
        arrayRegFiles(__tmp_1062 + 0.U) := __tmp_1063(7, 0)

        val __tmp_1064 = SP + 23.U(16.W)
        val __tmp_1065 = (114.U(8.W)).asUInt
        arrayRegFiles(__tmp_1064 + 0.U) := __tmp_1065(7, 0)

        val __tmp_1066 = SP + 24.U(16.W)
        val __tmp_1067 = (103.U(8.W)).asUInt
        arrayRegFiles(__tmp_1066 + 0.U) := __tmp_1067(7, 0)

        val __tmp_1068 = SP + 25.U(16.W)
        val __tmp_1069 = (46.U(8.W)).asUInt
        arrayRegFiles(__tmp_1068 + 0.U) := __tmp_1069(7, 0)

        val __tmp_1070 = SP + 26.U(16.W)
        val __tmp_1071 = (115.U(8.W)).asUInt
        arrayRegFiles(__tmp_1070 + 0.U) := __tmp_1071(7, 0)

        val __tmp_1072 = SP + 27.U(16.W)
        val __tmp_1073 = (105.U(8.W)).asUInt
        arrayRegFiles(__tmp_1072 + 0.U) := __tmp_1073(7, 0)

        val __tmp_1074 = SP + 28.U(16.W)
        val __tmp_1075 = (114.U(8.W)).asUInt
        arrayRegFiles(__tmp_1074 + 0.U) := __tmp_1075(7, 0)

        val __tmp_1076 = SP + 29.U(16.W)
        val __tmp_1077 = (101.U(8.W)).asUInt
        arrayRegFiles(__tmp_1076 + 0.U) := __tmp_1077(7, 0)

        val __tmp_1078 = SP + 30.U(16.W)
        val __tmp_1079 = (117.U(8.W)).asUInt
        arrayRegFiles(__tmp_1078 + 0.U) := __tmp_1079(7, 0)

        val __tmp_1080 = SP + 31.U(16.W)
        val __tmp_1081 = (109.U(8.W)).asUInt
        arrayRegFiles(__tmp_1080 + 0.U) := __tmp_1081(7, 0)

        val __tmp_1082 = SP + 32.U(16.W)
        val __tmp_1083 = (46.U(8.W)).asUInt
        arrayRegFiles(__tmp_1082 + 0.U) := __tmp_1083(7, 0)

        val __tmp_1084 = SP + 33.U(16.W)
        val __tmp_1085 = (97.U(8.W)).asUInt
        arrayRegFiles(__tmp_1084 + 0.U) := __tmp_1085(7, 0)

        val __tmp_1086 = SP + 34.U(16.W)
        val __tmp_1087 = (110.U(8.W)).asUInt
        arrayRegFiles(__tmp_1086 + 0.U) := __tmp_1087(7, 0)

        val __tmp_1088 = SP + 35.U(16.W)
        val __tmp_1089 = (118.U(8.W)).asUInt
        arrayRegFiles(__tmp_1088 + 0.U) := __tmp_1089(7, 0)

        val __tmp_1090 = SP + 36.U(16.W)
        val __tmp_1091 = (105.U(8.W)).asUInt
        arrayRegFiles(__tmp_1090 + 0.U) := __tmp_1091(7, 0)

        val __tmp_1092 = SP + 37.U(16.W)
        val __tmp_1093 = (108.U(8.W)).asUInt
        arrayRegFiles(__tmp_1092 + 0.U) := __tmp_1093(7, 0)

        val __tmp_1094 = SP + 38.U(16.W)
        val __tmp_1095 = (46.U(8.W)).asUInt
        arrayRegFiles(__tmp_1094 + 0.U) := __tmp_1095(7, 0)

        val __tmp_1096 = SP + 39.U(16.W)
        val __tmp_1097 = (82.U(8.W)).asUInt
        arrayRegFiles(__tmp_1096 + 0.U) := __tmp_1097(7, 0)

        val __tmp_1098 = SP + 40.U(16.W)
        val __tmp_1099 = (117.U(8.W)).asUInt
        arrayRegFiles(__tmp_1098 + 0.U) := __tmp_1099(7, 0)

        val __tmp_1100 = SP + 41.U(16.W)
        val __tmp_1101 = (110.U(8.W)).asUInt
        arrayRegFiles(__tmp_1100 + 0.U) := __tmp_1101(7, 0)

        val __tmp_1102 = SP + 42.U(16.W)
        val __tmp_1103 = (116.U(8.W)).asUInt
        arrayRegFiles(__tmp_1102 + 0.U) := __tmp_1103(7, 0)

        val __tmp_1104 = SP + 43.U(16.W)
        val __tmp_1105 = (105.U(8.W)).asUInt
        arrayRegFiles(__tmp_1104 + 0.U) := __tmp_1105(7, 0)

        val __tmp_1106 = SP + 44.U(16.W)
        val __tmp_1107 = (109.U(8.W)).asUInt
        arrayRegFiles(__tmp_1106 + 0.U) := __tmp_1107(7, 0)

        val __tmp_1108 = SP + 45.U(16.W)
        val __tmp_1109 = (101.U(8.W)).asUInt
        arrayRegFiles(__tmp_1108 + 0.U) := __tmp_1109(7, 0)

        val __tmp_1110 = SP + 46.U(16.W)
        val __tmp_1111 = (46.U(8.W)).asUInt
        arrayRegFiles(__tmp_1110 + 0.U) := __tmp_1111(7, 0)

        val __tmp_1112 = SP + 47.U(16.W)
        val __tmp_1113 = (112.U(8.W)).asUInt
        arrayRegFiles(__tmp_1112 + 0.U) := __tmp_1113(7, 0)

        val __tmp_1114 = SP + 48.U(16.W)
        val __tmp_1115 = (114.U(8.W)).asUInt
        arrayRegFiles(__tmp_1114 + 0.U) := __tmp_1115(7, 0)

        val __tmp_1116 = SP + 49.U(16.W)
        val __tmp_1117 = (105.U(8.W)).asUInt
        arrayRegFiles(__tmp_1116 + 0.U) := __tmp_1117(7, 0)

        val __tmp_1118 = SP + 50.U(16.W)
        val __tmp_1119 = (110.U(8.W)).asUInt
        arrayRegFiles(__tmp_1118 + 0.U) := __tmp_1119(7, 0)

        val __tmp_1120 = SP + 51.U(16.W)
        val __tmp_1121 = (116.U(8.W)).asUInt
        arrayRegFiles(__tmp_1120 + 0.U) := __tmp_1121(7, 0)

        val __tmp_1122 = SP + 52.U(16.W)
        val __tmp_1123 = (83.U(8.W)).asUInt
        arrayRegFiles(__tmp_1122 + 0.U) := __tmp_1123(7, 0)

        val __tmp_1124 = SP + 53.U(16.W)
        val __tmp_1125 = (116.U(8.W)).asUInt
        arrayRegFiles(__tmp_1124 + 0.U) := __tmp_1125(7, 0)

        val __tmp_1126 = SP + 54.U(16.W)
        val __tmp_1127 = (97.U(8.W)).asUInt
        arrayRegFiles(__tmp_1126 + 0.U) := __tmp_1127(7, 0)

        val __tmp_1128 = SP + 55.U(16.W)
        val __tmp_1129 = (99.U(8.W)).asUInt
        arrayRegFiles(__tmp_1128 + 0.U) := __tmp_1129(7, 0)

        val __tmp_1130 = SP + 56.U(16.W)
        val __tmp_1131 = (107.U(8.W)).asUInt
        arrayRegFiles(__tmp_1130 + 0.U) := __tmp_1131(7, 0)

        val __tmp_1132 = SP + 57.U(16.W)
        val __tmp_1133 = (84.U(8.W)).asUInt
        arrayRegFiles(__tmp_1132 + 0.U) := __tmp_1133(7, 0)

        val __tmp_1134 = SP + 58.U(16.W)
        val __tmp_1135 = (114.U(8.W)).asUInt
        arrayRegFiles(__tmp_1134 + 0.U) := __tmp_1135(7, 0)

        val __tmp_1136 = SP + 59.U(16.W)
        val __tmp_1137 = (97.U(8.W)).asUInt
        arrayRegFiles(__tmp_1136 + 0.U) := __tmp_1137(7, 0)

        val __tmp_1138 = SP + 60.U(16.W)
        val __tmp_1139 = (99.U(8.W)).asUInt
        arrayRegFiles(__tmp_1138 + 0.U) := __tmp_1139(7, 0)

        val __tmp_1140 = SP + 61.U(16.W)
        val __tmp_1141 = (101.U(8.W)).asUInt
        arrayRegFiles(__tmp_1140 + 0.U) := __tmp_1141(7, 0)

        val __tmp_1142 = SP + 62.U(16.W)
        val __tmp_1143 = (32.U(8.W)).asUInt
        arrayRegFiles(__tmp_1142 + 0.U) := __tmp_1143(7, 0)

        val __tmp_1144 = SP + 63.U(16.W)
        val __tmp_1145 = (40.U(8.W)).asUInt
        arrayRegFiles(__tmp_1144 + 0.U) := __tmp_1145(7, 0)

        val __tmp_1146 = SP + 64.U(16.W)
        val __tmp_1147 = (82.U(8.W)).asUInt
        arrayRegFiles(__tmp_1146 + 0.U) := __tmp_1147(7, 0)

        val __tmp_1148 = SP + 65.U(16.W)
        val __tmp_1149 = (117.U(8.W)).asUInt
        arrayRegFiles(__tmp_1148 + 0.U) := __tmp_1149(7, 0)

        val __tmp_1150 = SP + 66.U(16.W)
        val __tmp_1151 = (110.U(8.W)).asUInt
        arrayRegFiles(__tmp_1150 + 0.U) := __tmp_1151(7, 0)

        val __tmp_1152 = SP + 67.U(16.W)
        val __tmp_1153 = (116.U(8.W)).asUInt
        arrayRegFiles(__tmp_1152 + 0.U) := __tmp_1153(7, 0)

        val __tmp_1154 = SP + 68.U(16.W)
        val __tmp_1155 = (105.U(8.W)).asUInt
        arrayRegFiles(__tmp_1154 + 0.U) := __tmp_1155(7, 0)

        val __tmp_1156 = SP + 69.U(16.W)
        val __tmp_1157 = (109.U(8.W)).asUInt
        arrayRegFiles(__tmp_1156 + 0.U) := __tmp_1157(7, 0)

        val __tmp_1158 = SP + 70.U(16.W)
        val __tmp_1159 = (101.U(8.W)).asUInt
        arrayRegFiles(__tmp_1158 + 0.U) := __tmp_1159(7, 0)

        val __tmp_1160 = SP + 71.U(16.W)
        val __tmp_1161 = (46.U(8.W)).asUInt
        arrayRegFiles(__tmp_1160 + 0.U) := __tmp_1161(7, 0)

        val __tmp_1162 = SP + 72.U(16.W)
        val __tmp_1163 = (115.U(8.W)).asUInt
        arrayRegFiles(__tmp_1162 + 0.U) := __tmp_1163(7, 0)

        val __tmp_1164 = SP + 73.U(16.W)
        val __tmp_1165 = (99.U(8.W)).asUInt
        arrayRegFiles(__tmp_1164 + 0.U) := __tmp_1165(7, 0)

        val __tmp_1166 = SP + 74.U(16.W)
        val __tmp_1167 = (97.U(8.W)).asUInt
        arrayRegFiles(__tmp_1166 + 0.U) := __tmp_1167(7, 0)

        val __tmp_1168 = SP + 75.U(16.W)
        val __tmp_1169 = (108.U(8.W)).asUInt
        arrayRegFiles(__tmp_1168 + 0.U) := __tmp_1169(7, 0)

        val __tmp_1170 = SP + 76.U(16.W)
        val __tmp_1171 = (97.U(8.W)).asUInt
        arrayRegFiles(__tmp_1170 + 0.U) := __tmp_1171(7, 0)

        val __tmp_1172 = SP + 77.U(16.W)
        val __tmp_1173 = (58.U(8.W)).asUInt
        arrayRegFiles(__tmp_1172 + 0.U) := __tmp_1173(7, 0)

        val __tmp_1174 = SP + 6.U(16.W)
        val __tmp_1175 = (639.S(32.W)).asUInt
        arrayRegFiles(__tmp_1174 + 0.U) := __tmp_1175(7, 0)
        arrayRegFiles(__tmp_1174 + 1.U) := __tmp_1175(15, 8)
        arrayRegFiles(__tmp_1174 + 2.U) := __tmp_1175(23, 16)
        arrayRegFiles(__tmp_1174 + 3.U) := __tmp_1175(31, 24)

        CP := 28.U
      }

      is(28.U) {
        /*
        decl sfCaller: anvil.PrinterIndex.U [@140, 8]
        $0 = *(SP + (132 [SP])) [unsigned, anvil.PrinterIndex.U, 8]  // $0 = sfCallerOffset
        goto .29
        */


        val __tmp_1176 = (SP + 132.U(16.W)).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_1176 + 7.U),
          arrayRegFiles(__tmp_1176 + 6.U),
          arrayRegFiles(__tmp_1176 + 5.U),
          arrayRegFiles(__tmp_1176 + 4.U),
          arrayRegFiles(__tmp_1176 + 3.U),
          arrayRegFiles(__tmp_1176 + 2.U),
          arrayRegFiles(__tmp_1176 + 1.U),
          arrayRegFiles(__tmp_1176 + 0.U)
        ).asUInt

        CP := 29.U
      }

      is(29.U) {
        /*
        *(SP + (140 [SP])) = $0 [signed, anvil.PrinterIndex.U, 8]  // sfCaller = $0
        goto .30
        */


        val __tmp_1177 = SP + 140.U(16.W)
        val __tmp_1178 = (generalRegFiles(0.U)).asUInt
        arrayRegFiles(__tmp_1177 + 0.U) := __tmp_1178(7, 0)
        arrayRegFiles(__tmp_1177 + 1.U) := __tmp_1178(15, 8)
        arrayRegFiles(__tmp_1177 + 2.U) := __tmp_1178(23, 16)
        arrayRegFiles(__tmp_1177 + 3.U) := __tmp_1178(31, 24)
        arrayRegFiles(__tmp_1177 + 4.U) := __tmp_1178(39, 32)
        arrayRegFiles(__tmp_1177 + 5.U) := __tmp_1178(47, 40)
        arrayRegFiles(__tmp_1177 + 6.U) := __tmp_1178(55, 48)
        arrayRegFiles(__tmp_1177 + 7.U) := __tmp_1178(63, 56)

        CP := 30.U
      }

      is(30.U) {
        /*
        *(SP + (6 [SP])) = (640 [U32]) [signed, U32, 4]  // $sfLoc = (640 [U32])
        goto .31
        */


        val __tmp_1179 = SP + 6.U(16.W)
        val __tmp_1180 = (640.S(32.W)).asUInt
        arrayRegFiles(__tmp_1179 + 0.U) := __tmp_1180(7, 0)
        arrayRegFiles(__tmp_1179 + 1.U) := __tmp_1180(15, 8)
        arrayRegFiles(__tmp_1179 + 2.U) := __tmp_1180(23, 16)
        arrayRegFiles(__tmp_1179 + 3.U) := __tmp_1180(31, 24)

        CP := 31.U
      }

      is(31.U) {
        /*
        decl r: U64 [@148, 8]
        *(SP + (148 [SP])) = (0 [U64]) [signed, U64, 8]  // r = (0 [U64])
        goto .32
        */


        val __tmp_1181 = SP + 148.U(16.W)
        val __tmp_1182 = (0.S(64.W)).asUInt
        arrayRegFiles(__tmp_1181 + 0.U) := __tmp_1182(7, 0)
        arrayRegFiles(__tmp_1181 + 1.U) := __tmp_1182(15, 8)
        arrayRegFiles(__tmp_1181 + 2.U) := __tmp_1182(23, 16)
        arrayRegFiles(__tmp_1181 + 3.U) := __tmp_1182(31, 24)
        arrayRegFiles(__tmp_1181 + 4.U) := __tmp_1182(39, 32)
        arrayRegFiles(__tmp_1181 + 5.U) := __tmp_1182(47, 40)
        arrayRegFiles(__tmp_1181 + 6.U) := __tmp_1182(55, 48)
        arrayRegFiles(__tmp_1181 + 7.U) := __tmp_1182(63, 56)

        CP := 32.U
      }

      is(32.U) {
        /*
        *(SP + (6 [SP])) = (641 [U32]) [signed, U32, 4]  // $sfLoc = (641 [U32])
        goto .33
        */


        val __tmp_1183 = SP + 6.U(16.W)
        val __tmp_1184 = (641.S(32.W)).asUInt
        arrayRegFiles(__tmp_1183 + 0.U) := __tmp_1184(7, 0)
        arrayRegFiles(__tmp_1183 + 1.U) := __tmp_1184(15, 8)
        arrayRegFiles(__tmp_1183 + 2.U) := __tmp_1184(23, 16)
        arrayRegFiles(__tmp_1183 + 3.U) := __tmp_1184(31, 24)

        CP := 33.U
      }

      is(33.U) {
        /*
        decl idx: anvil.PrinterIndex.U [@156, 8]
        $0 = *(SP + (82 [SP])) [unsigned, anvil.PrinterIndex.U, 8]  // $0 = index
        goto .34
        */


        val __tmp_1185 = (SP + 82.U(16.W)).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_1185 + 7.U),
          arrayRegFiles(__tmp_1185 + 6.U),
          arrayRegFiles(__tmp_1185 + 5.U),
          arrayRegFiles(__tmp_1185 + 4.U),
          arrayRegFiles(__tmp_1185 + 3.U),
          arrayRegFiles(__tmp_1185 + 2.U),
          arrayRegFiles(__tmp_1185 + 1.U),
          arrayRegFiles(__tmp_1185 + 0.U)
        ).asUInt

        CP := 34.U
      }

      is(34.U) {
        /*
        *(SP + (156 [SP])) = $0 [signed, anvil.PrinterIndex.U, 8]  // idx = $0
        goto .35
        */


        val __tmp_1186 = SP + 156.U(16.W)
        val __tmp_1187 = (generalRegFiles(0.U)).asUInt
        arrayRegFiles(__tmp_1186 + 0.U) := __tmp_1187(7, 0)
        arrayRegFiles(__tmp_1186 + 1.U) := __tmp_1187(15, 8)
        arrayRegFiles(__tmp_1186 + 2.U) := __tmp_1187(23, 16)
        arrayRegFiles(__tmp_1186 + 3.U) := __tmp_1187(31, 24)
        arrayRegFiles(__tmp_1186 + 4.U) := __tmp_1187(39, 32)
        arrayRegFiles(__tmp_1186 + 5.U) := __tmp_1187(47, 40)
        arrayRegFiles(__tmp_1186 + 6.U) := __tmp_1187(55, 48)
        arrayRegFiles(__tmp_1186 + 7.U) := __tmp_1187(63, 56)

        CP := 35.U
      }

      is(35.U) {
        /*
        *(SP + (6 [SP])) = (642 [U32]) [signed, U32, 4]  // $sfLoc = (642 [U32])
        goto .36
        */


        val __tmp_1188 = SP + 6.U(16.W)
        val __tmp_1189 = (642.S(32.W)).asUInt
        arrayRegFiles(__tmp_1188 + 0.U) := __tmp_1189(7, 0)
        arrayRegFiles(__tmp_1188 + 1.U) := __tmp_1189(15, 8)
        arrayRegFiles(__tmp_1188 + 2.U) := __tmp_1189(23, 16)
        arrayRegFiles(__tmp_1188 + 3.U) := __tmp_1189(31, 24)

        CP := 36.U
      }

      is(36.U) {
        /*
        *(SP + (6 [SP])) = (642 [U32]) [signed, U32, 4]  // $sfLoc = (642 [U32])
        goto .37
        */


        val __tmp_1190 = SP + 6.U(16.W)
        val __tmp_1191 = (642.S(32.W)).asUInt
        arrayRegFiles(__tmp_1190 + 0.U) := __tmp_1191(7, 0)
        arrayRegFiles(__tmp_1190 + 1.U) := __tmp_1191(15, 8)
        arrayRegFiles(__tmp_1190 + 2.U) := __tmp_1191(23, 16)
        arrayRegFiles(__tmp_1190 + 3.U) := __tmp_1191(31, 24)

        CP := 37.U
      }

      is(37.U) {
        /*
        $0 = *(SP + (140 [SP])) [unsigned, anvil.PrinterIndex.U, 8]  // $0 = sfCaller
        goto .38
        */


        val __tmp_1192 = (SP + 140.U(16.W)).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_1192 + 7.U),
          arrayRegFiles(__tmp_1192 + 6.U),
          arrayRegFiles(__tmp_1192 + 5.U),
          arrayRegFiles(__tmp_1192 + 4.U),
          arrayRegFiles(__tmp_1192 + 3.U),
          arrayRegFiles(__tmp_1192 + 2.U),
          arrayRegFiles(__tmp_1192 + 1.U),
          arrayRegFiles(__tmp_1192 + 0.U)
        ).asUInt

        CP := 38.U
      }

      is(38.U) {
        /*
        $1 = ($0 ≢ (0 [anvil.PrinterIndex.U]))
        goto .39
        */


        generalRegFiles(1.U) := (generalRegFiles(0.U) =/= 0.U(64.W)).asUInt
        CP := 39.U
      }

      is(39.U) {
        /*
        if $1 goto .40 else goto .95
        */


        CP := Mux((generalRegFiles(1.U).asUInt) === 1.U, 40.U, 95.U)
      }

      is(40.U) {
        /*
        *(SP + (6 [SP])) = (643 [U32]) [signed, U32, 4]  // $sfLoc = (643 [U32])
        goto .41
        */


        val __tmp_1193 = SP + 6.U(16.W)
        val __tmp_1194 = (643.S(32.W)).asUInt
        arrayRegFiles(__tmp_1193 + 0.U) := __tmp_1194(7, 0)
        arrayRegFiles(__tmp_1193 + 1.U) := __tmp_1194(15, 8)
        arrayRegFiles(__tmp_1193 + 2.U) := __tmp_1194(23, 16)
        arrayRegFiles(__tmp_1193 + 3.U) := __tmp_1194(31, 24)

        CP := 41.U
      }

      is(41.U) {
        /*
        decl offset: anvil.PrinterIndex.U [@164, 8]
        $0 = *(SP + (140 [SP])) [unsigned, anvil.PrinterIndex.U, 8]  // $0 = sfCaller
        $1 = *(SP + (100 [SP])) [unsigned, anvil.PrinterIndex.U, 8]  // $1 = spSize
        goto .42
        */


        val __tmp_1195 = (SP + 140.U(16.W)).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_1195 + 7.U),
          arrayRegFiles(__tmp_1195 + 6.U),
          arrayRegFiles(__tmp_1195 + 5.U),
          arrayRegFiles(__tmp_1195 + 4.U),
          arrayRegFiles(__tmp_1195 + 3.U),
          arrayRegFiles(__tmp_1195 + 2.U),
          arrayRegFiles(__tmp_1195 + 1.U),
          arrayRegFiles(__tmp_1195 + 0.U)
        ).asUInt

        val __tmp_1196 = (SP + 100.U(16.W)).asUInt
        generalRegFiles(1.U) := Cat(
          arrayRegFiles(__tmp_1196 + 7.U),
          arrayRegFiles(__tmp_1196 + 6.U),
          arrayRegFiles(__tmp_1196 + 5.U),
          arrayRegFiles(__tmp_1196 + 4.U),
          arrayRegFiles(__tmp_1196 + 3.U),
          arrayRegFiles(__tmp_1196 + 2.U),
          arrayRegFiles(__tmp_1196 + 1.U),
          arrayRegFiles(__tmp_1196 + 0.U)
        ).asUInt

        CP := 42.U
      }

      is(42.U) {
        /*
        $2 = ($0 + $1)
        goto .43
        */


        generalRegFiles(2.U) := generalRegFiles(0.U) + generalRegFiles(1.U)
        CP := 43.U
      }

      is(43.U) {
        /*
        $3 = *(SP + (108 [SP])) [unsigned, anvil.PrinterIndex.U, 8]  // $3 = typeShaSize
        goto .44
        */


        val __tmp_1197 = (SP + 108.U(16.W)).asUInt
        generalRegFiles(3.U) := Cat(
          arrayRegFiles(__tmp_1197 + 7.U),
          arrayRegFiles(__tmp_1197 + 6.U),
          arrayRegFiles(__tmp_1197 + 5.U),
          arrayRegFiles(__tmp_1197 + 4.U),
          arrayRegFiles(__tmp_1197 + 3.U),
          arrayRegFiles(__tmp_1197 + 2.U),
          arrayRegFiles(__tmp_1197 + 1.U),
          arrayRegFiles(__tmp_1197 + 0.U)
        ).asUInt

        CP := 44.U
      }

      is(44.U) {
        /*
        $4 = ($2 - $3)
        goto .45
        */


        generalRegFiles(4.U) := generalRegFiles(2.U) - generalRegFiles(3.U)
        CP := 45.U
      }

      is(45.U) {
        /*
        $5 = *(SP + (124 [SP])) [unsigned, anvil.PrinterIndex.U, 8]  // $5 = sizeSize
        goto .46
        */


        val __tmp_1198 = (SP + 124.U(16.W)).asUInt
        generalRegFiles(5.U) := Cat(
          arrayRegFiles(__tmp_1198 + 7.U),
          arrayRegFiles(__tmp_1198 + 6.U),
          arrayRegFiles(__tmp_1198 + 5.U),
          arrayRegFiles(__tmp_1198 + 4.U),
          arrayRegFiles(__tmp_1198 + 3.U),
          arrayRegFiles(__tmp_1198 + 2.U),
          arrayRegFiles(__tmp_1198 + 1.U),
          arrayRegFiles(__tmp_1198 + 0.U)
        ).asUInt

        CP := 46.U
      }

      is(46.U) {
        /*
        $6 = ($4 - $5)
        goto .47
        */


        generalRegFiles(6.U) := generalRegFiles(4.U) - generalRegFiles(5.U)
        CP := 47.U
      }

      is(47.U) {
        /*
        *(SP + (164 [SP])) = $6 [signed, anvil.PrinterIndex.U, 8]  // offset = $6
        goto .48
        */


        val __tmp_1199 = SP + 164.U(16.W)
        val __tmp_1200 = (generalRegFiles(6.U)).asUInt
        arrayRegFiles(__tmp_1199 + 0.U) := __tmp_1200(7, 0)
        arrayRegFiles(__tmp_1199 + 1.U) := __tmp_1200(15, 8)
        arrayRegFiles(__tmp_1199 + 2.U) := __tmp_1200(23, 16)
        arrayRegFiles(__tmp_1199 + 3.U) := __tmp_1200(31, 24)
        arrayRegFiles(__tmp_1199 + 4.U) := __tmp_1200(39, 32)
        arrayRegFiles(__tmp_1199 + 5.U) := __tmp_1200(47, 40)
        arrayRegFiles(__tmp_1199 + 6.U) := __tmp_1200(55, 48)
        arrayRegFiles(__tmp_1199 + 7.U) := __tmp_1200(63, 56)

        CP := 48.U
      }

      is(48.U) {
        /*
        *(SP + (6 [SP])) = (644 [U32]) [signed, U32, 4]  // $sfLoc = (644 [U32])
        goto .49
        */


        val __tmp_1201 = SP + 6.U(16.W)
        val __tmp_1202 = (644.S(32.W)).asUInt
        arrayRegFiles(__tmp_1201 + 0.U) := __tmp_1202(7, 0)
        arrayRegFiles(__tmp_1201 + 1.U) := __tmp_1202(15, 8)
        arrayRegFiles(__tmp_1201 + 2.U) := __tmp_1202(23, 16)
        arrayRegFiles(__tmp_1201 + 3.U) := __tmp_1202(31, 24)

        CP := 49.U
      }

      is(49.U) {
        /*
        decl sfLoc: U64 [@172, 8]
        $0 = *(SP + (90 [SP])) [unsigned, MS[anvil.PrinterIndex.U, U8], 2]  // $0 = memory
        $1 = *(SP + (164 [SP])) [unsigned, anvil.PrinterIndex.U, 8]  // $1 = offset
        $2 = *(SP + (116 [SP])) [unsigned, anvil.PrinterIndex.U, 8]  // $2 = locSize
        alloc load$res@[644,47].3C9051E9: anvil.PrinterIndex.U [@180, 8]
        goto .50
        */


        val __tmp_1203 = (SP + 90.U(16.W)).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_1203 + 1.U),
          arrayRegFiles(__tmp_1203 + 0.U)
        ).asUInt

        val __tmp_1204 = (SP + 164.U(16.W)).asUInt
        generalRegFiles(1.U) := Cat(
          arrayRegFiles(__tmp_1204 + 7.U),
          arrayRegFiles(__tmp_1204 + 6.U),
          arrayRegFiles(__tmp_1204 + 5.U),
          arrayRegFiles(__tmp_1204 + 4.U),
          arrayRegFiles(__tmp_1204 + 3.U),
          arrayRegFiles(__tmp_1204 + 2.U),
          arrayRegFiles(__tmp_1204 + 1.U),
          arrayRegFiles(__tmp_1204 + 0.U)
        ).asUInt

        val __tmp_1205 = (SP + 116.U(16.W)).asUInt
        generalRegFiles(2.U) := Cat(
          arrayRegFiles(__tmp_1205 + 7.U),
          arrayRegFiles(__tmp_1205 + 6.U),
          arrayRegFiles(__tmp_1205 + 5.U),
          arrayRegFiles(__tmp_1205 + 4.U),
          arrayRegFiles(__tmp_1205 + 3.U),
          arrayRegFiles(__tmp_1205 + 2.U),
          arrayRegFiles(__tmp_1205 + 1.U),
          arrayRegFiles(__tmp_1205 + 0.U)
        ).asUInt

        CP := 50.U
      }

      is(50.U) {
        /*
        SP = SP + 244
        goto .51
        */


        SP := SP + 244.U

        CP := 51.U
      }

      is(51.U) {
        /*
        decl $ret: CP [@0, 2], $res: SP [@2, 2], $sfCaller: SP [@4, 2], $sfLoc: U32 [@6, 4], $sfDesc: IS[45, U8] [@10, 57], $sfCurrentId: SP [@67, 2], memory: SP [@69, 2], offset: anvil.PrinterIndex.U [@71, 8], size: anvil.PrinterIndex.U [@79, 8]
        *SP = (109 [CP]) [unsigned, CP, 2]  // $ret@0 = 2226
        *(SP + (2 [SP])) = (SP - (64 [SP])) [unsigned, SP, 2]  // $res@2 = -64
        *(SP + (4 [SP])) = (SP - (240 [SP])) [unsigned, SP, 2]  // $sfCaller@4 = -240
        *(SP + (67 [SP])) = (SP + (4 [SP])) [unsigned, SP, 2]  // $sfCurrentId@67 = 4
        *(SP + (69 [SP])) = $0 [unsigned, SP, 2]  // memory = $0
        *(SP + (71 [SP])) = $1 [unsigned, anvil.PrinterIndex.U, 8]  // offset = $1
        *(SP + (79 [SP])) = $2 [unsigned, anvil.PrinterIndex.U, 8]  // size = $2
        *(SP - (24 [SP])) = $0 [unsigned, U64, 8]  // save $0
        *(SP - (16 [SP])) = $1 [unsigned, U64, 8]  // save $1
        *(SP - (8 [SP])) = $2 [unsigned, U64, 8]  // save $2
        goto .52
        */


        val __tmp_1206 = SP
        val __tmp_1207 = (109.U(16.W)).asUInt
        arrayRegFiles(__tmp_1206 + 0.U) := __tmp_1207(7, 0)
        arrayRegFiles(__tmp_1206 + 1.U) := __tmp_1207(15, 8)

        val __tmp_1208 = SP + 2.U(16.W)
        val __tmp_1209 = (SP - 64.U(16.W)).asUInt
        arrayRegFiles(__tmp_1208 + 0.U) := __tmp_1209(7, 0)
        arrayRegFiles(__tmp_1208 + 1.U) := __tmp_1209(15, 8)

        val __tmp_1210 = SP + 4.U(16.W)
        val __tmp_1211 = (SP - 240.U(16.W)).asUInt
        arrayRegFiles(__tmp_1210 + 0.U) := __tmp_1211(7, 0)
        arrayRegFiles(__tmp_1210 + 1.U) := __tmp_1211(15, 8)

        val __tmp_1212 = SP + 67.U(16.W)
        val __tmp_1213 = (SP + 4.U(16.W)).asUInt
        arrayRegFiles(__tmp_1212 + 0.U) := __tmp_1213(7, 0)
        arrayRegFiles(__tmp_1212 + 1.U) := __tmp_1213(15, 8)

        val __tmp_1214 = SP + 69.U(16.W)
        val __tmp_1215 = (generalRegFiles(0.U)).asUInt
        arrayRegFiles(__tmp_1214 + 0.U) := __tmp_1215(7, 0)
        arrayRegFiles(__tmp_1214 + 1.U) := __tmp_1215(15, 8)

        val __tmp_1216 = SP + 71.U(16.W)
        val __tmp_1217 = (generalRegFiles(1.U)).asUInt
        arrayRegFiles(__tmp_1216 + 0.U) := __tmp_1217(7, 0)
        arrayRegFiles(__tmp_1216 + 1.U) := __tmp_1217(15, 8)
        arrayRegFiles(__tmp_1216 + 2.U) := __tmp_1217(23, 16)
        arrayRegFiles(__tmp_1216 + 3.U) := __tmp_1217(31, 24)
        arrayRegFiles(__tmp_1216 + 4.U) := __tmp_1217(39, 32)
        arrayRegFiles(__tmp_1216 + 5.U) := __tmp_1217(47, 40)
        arrayRegFiles(__tmp_1216 + 6.U) := __tmp_1217(55, 48)
        arrayRegFiles(__tmp_1216 + 7.U) := __tmp_1217(63, 56)

        val __tmp_1218 = SP + 79.U(16.W)
        val __tmp_1219 = (generalRegFiles(2.U)).asUInt
        arrayRegFiles(__tmp_1218 + 0.U) := __tmp_1219(7, 0)
        arrayRegFiles(__tmp_1218 + 1.U) := __tmp_1219(15, 8)
        arrayRegFiles(__tmp_1218 + 2.U) := __tmp_1219(23, 16)
        arrayRegFiles(__tmp_1218 + 3.U) := __tmp_1219(31, 24)
        arrayRegFiles(__tmp_1218 + 4.U) := __tmp_1219(39, 32)
        arrayRegFiles(__tmp_1218 + 5.U) := __tmp_1219(47, 40)
        arrayRegFiles(__tmp_1218 + 6.U) := __tmp_1219(55, 48)
        arrayRegFiles(__tmp_1218 + 7.U) := __tmp_1219(63, 56)

        val __tmp_1220 = SP - 24.U(16.W)
        val __tmp_1221 = (generalRegFiles(0.U)).asUInt
        arrayRegFiles(__tmp_1220 + 0.U) := __tmp_1221(7, 0)
        arrayRegFiles(__tmp_1220 + 1.U) := __tmp_1221(15, 8)
        arrayRegFiles(__tmp_1220 + 2.U) := __tmp_1221(23, 16)
        arrayRegFiles(__tmp_1220 + 3.U) := __tmp_1221(31, 24)
        arrayRegFiles(__tmp_1220 + 4.U) := __tmp_1221(39, 32)
        arrayRegFiles(__tmp_1220 + 5.U) := __tmp_1221(47, 40)
        arrayRegFiles(__tmp_1220 + 6.U) := __tmp_1221(55, 48)
        arrayRegFiles(__tmp_1220 + 7.U) := __tmp_1221(63, 56)

        val __tmp_1222 = SP - 16.U(16.W)
        val __tmp_1223 = (generalRegFiles(1.U)).asUInt
        arrayRegFiles(__tmp_1222 + 0.U) := __tmp_1223(7, 0)
        arrayRegFiles(__tmp_1222 + 1.U) := __tmp_1223(15, 8)
        arrayRegFiles(__tmp_1222 + 2.U) := __tmp_1223(23, 16)
        arrayRegFiles(__tmp_1222 + 3.U) := __tmp_1223(31, 24)
        arrayRegFiles(__tmp_1222 + 4.U) := __tmp_1223(39, 32)
        arrayRegFiles(__tmp_1222 + 5.U) := __tmp_1223(47, 40)
        arrayRegFiles(__tmp_1222 + 6.U) := __tmp_1223(55, 48)
        arrayRegFiles(__tmp_1222 + 7.U) := __tmp_1223(63, 56)

        val __tmp_1224 = SP - 8.U(16.W)
        val __tmp_1225 = (generalRegFiles(2.U)).asUInt
        arrayRegFiles(__tmp_1224 + 0.U) := __tmp_1225(7, 0)
        arrayRegFiles(__tmp_1224 + 1.U) := __tmp_1225(15, 8)
        arrayRegFiles(__tmp_1224 + 2.U) := __tmp_1225(23, 16)
        arrayRegFiles(__tmp_1224 + 3.U) := __tmp_1225(31, 24)
        arrayRegFiles(__tmp_1224 + 4.U) := __tmp_1225(39, 32)
        arrayRegFiles(__tmp_1224 + 5.U) := __tmp_1225(47, 40)
        arrayRegFiles(__tmp_1224 + 6.U) := __tmp_1225(55, 48)
        arrayRegFiles(__tmp_1224 + 7.U) := __tmp_1225(63, 56)

        CP := 52.U
      }

      is(52.U) {
        /*
        *(SP + (10 [SP])) = (3069765878 [U32]) [unsigned, U32, 4]  // $sfDesc.type = 0xB6F8E8F6
        *(SP + (14 [SP])) = (45 [Z]) [signed, Z, 8]  // $sfDesc.size = 45
        *(SP + (22 [SP])) = (111 [U8]) [unsigned, U8, 1]  // 'o'
        *(SP + (23 [SP])) = (114 [U8]) [unsigned, U8, 1]  // 'r'
        *(SP + (24 [SP])) = (103 [U8]) [unsigned, U8, 1]  // 'g'
        *(SP + (25 [SP])) = (46 [U8]) [unsigned, U8, 1]  // '.'
        *(SP + (26 [SP])) = (115 [U8]) [unsigned, U8, 1]  // 's'
        *(SP + (27 [SP])) = (105 [U8]) [unsigned, U8, 1]  // 'i'
        *(SP + (28 [SP])) = (114 [U8]) [unsigned, U8, 1]  // 'r'
        *(SP + (29 [SP])) = (101 [U8]) [unsigned, U8, 1]  // 'e'
        *(SP + (30 [SP])) = (117 [U8]) [unsigned, U8, 1]  // 'u'
        *(SP + (31 [SP])) = (109 [U8]) [unsigned, U8, 1]  // 'm'
        *(SP + (32 [SP])) = (46 [U8]) [unsigned, U8, 1]  // '.'
        *(SP + (33 [SP])) = (97 [U8]) [unsigned, U8, 1]  // 'a'
        *(SP + (34 [SP])) = (110 [U8]) [unsigned, U8, 1]  // 'n'
        *(SP + (35 [SP])) = (118 [U8]) [unsigned, U8, 1]  // 'v'
        *(SP + (36 [SP])) = (105 [U8]) [unsigned, U8, 1]  // 'i'
        *(SP + (37 [SP])) = (108 [U8]) [unsigned, U8, 1]  // 'l'
        *(SP + (38 [SP])) = (46 [U8]) [unsigned, U8, 1]  // '.'
        *(SP + (39 [SP])) = (82 [U8]) [unsigned, U8, 1]  // 'R'
        *(SP + (40 [SP])) = (117 [U8]) [unsigned, U8, 1]  // 'u'
        *(SP + (41 [SP])) = (110 [U8]) [unsigned, U8, 1]  // 'n'
        *(SP + (42 [SP])) = (116 [U8]) [unsigned, U8, 1]  // 't'
        *(SP + (43 [SP])) = (105 [U8]) [unsigned, U8, 1]  // 'i'
        *(SP + (44 [SP])) = (109 [U8]) [unsigned, U8, 1]  // 'm'
        *(SP + (45 [SP])) = (101 [U8]) [unsigned, U8, 1]  // 'e'
        *(SP + (46 [SP])) = (46 [U8]) [unsigned, U8, 1]  // '.'
        *(SP + (47 [SP])) = (108 [U8]) [unsigned, U8, 1]  // 'l'
        *(SP + (48 [SP])) = (111 [U8]) [unsigned, U8, 1]  // 'o'
        *(SP + (49 [SP])) = (97 [U8]) [unsigned, U8, 1]  // 'a'
        *(SP + (50 [SP])) = (100 [U8]) [unsigned, U8, 1]  // 'd'
        *(SP + (51 [SP])) = (32 [U8]) [unsigned, U8, 1]  // ' '
        *(SP + (52 [SP])) = (40 [U8]) [unsigned, U8, 1]  // '('
        *(SP + (53 [SP])) = (82 [U8]) [unsigned, U8, 1]  // 'R'
        *(SP + (54 [SP])) = (117 [U8]) [unsigned, U8, 1]  // 'u'
        *(SP + (55 [SP])) = (110 [U8]) [unsigned, U8, 1]  // 'n'
        *(SP + (56 [SP])) = (116 [U8]) [unsigned, U8, 1]  // 't'
        *(SP + (57 [SP])) = (105 [U8]) [unsigned, U8, 1]  // 'i'
        *(SP + (58 [SP])) = (109 [U8]) [unsigned, U8, 1]  // 'm'
        *(SP + (59 [SP])) = (101 [U8]) [unsigned, U8, 1]  // 'e'
        *(SP + (60 [SP])) = (46 [U8]) [unsigned, U8, 1]  // '.'
        *(SP + (61 [SP])) = (115 [U8]) [unsigned, U8, 1]  // 's'
        *(SP + (62 [SP])) = (99 [U8]) [unsigned, U8, 1]  // 'c'
        *(SP + (63 [SP])) = (97 [U8]) [unsigned, U8, 1]  // 'a'
        *(SP + (64 [SP])) = (108 [U8]) [unsigned, U8, 1]  // 'l'
        *(SP + (65 [SP])) = (97 [U8]) [unsigned, U8, 1]  // 'a'
        *(SP + (66 [SP])) = (58 [U8]) [unsigned, U8, 1]  // ':'
        *(SP + (6 [SP])) = (619 [U32]) [signed, U32, 4]  // $sfLoc = (619 [U32])
        goto .53
        */


        val __tmp_1226 = SP + 10.U(16.W)
        val __tmp_1227 = (3069765878L.U(32.W)).asUInt
        arrayRegFiles(__tmp_1226 + 0.U) := __tmp_1227(7, 0)
        arrayRegFiles(__tmp_1226 + 1.U) := __tmp_1227(15, 8)
        arrayRegFiles(__tmp_1226 + 2.U) := __tmp_1227(23, 16)
        arrayRegFiles(__tmp_1226 + 3.U) := __tmp_1227(31, 24)

        val __tmp_1228 = SP + 14.U(16.W)
        val __tmp_1229 = (45.S(64.W)).asUInt
        arrayRegFiles(__tmp_1228 + 0.U) := __tmp_1229(7, 0)
        arrayRegFiles(__tmp_1228 + 1.U) := __tmp_1229(15, 8)
        arrayRegFiles(__tmp_1228 + 2.U) := __tmp_1229(23, 16)
        arrayRegFiles(__tmp_1228 + 3.U) := __tmp_1229(31, 24)
        arrayRegFiles(__tmp_1228 + 4.U) := __tmp_1229(39, 32)
        arrayRegFiles(__tmp_1228 + 5.U) := __tmp_1229(47, 40)
        arrayRegFiles(__tmp_1228 + 6.U) := __tmp_1229(55, 48)
        arrayRegFiles(__tmp_1228 + 7.U) := __tmp_1229(63, 56)

        val __tmp_1230 = SP + 22.U(16.W)
        val __tmp_1231 = (111.U(8.W)).asUInt
        arrayRegFiles(__tmp_1230 + 0.U) := __tmp_1231(7, 0)

        val __tmp_1232 = SP + 23.U(16.W)
        val __tmp_1233 = (114.U(8.W)).asUInt
        arrayRegFiles(__tmp_1232 + 0.U) := __tmp_1233(7, 0)

        val __tmp_1234 = SP + 24.U(16.W)
        val __tmp_1235 = (103.U(8.W)).asUInt
        arrayRegFiles(__tmp_1234 + 0.U) := __tmp_1235(7, 0)

        val __tmp_1236 = SP + 25.U(16.W)
        val __tmp_1237 = (46.U(8.W)).asUInt
        arrayRegFiles(__tmp_1236 + 0.U) := __tmp_1237(7, 0)

        val __tmp_1238 = SP + 26.U(16.W)
        val __tmp_1239 = (115.U(8.W)).asUInt
        arrayRegFiles(__tmp_1238 + 0.U) := __tmp_1239(7, 0)

        val __tmp_1240 = SP + 27.U(16.W)
        val __tmp_1241 = (105.U(8.W)).asUInt
        arrayRegFiles(__tmp_1240 + 0.U) := __tmp_1241(7, 0)

        val __tmp_1242 = SP + 28.U(16.W)
        val __tmp_1243 = (114.U(8.W)).asUInt
        arrayRegFiles(__tmp_1242 + 0.U) := __tmp_1243(7, 0)

        val __tmp_1244 = SP + 29.U(16.W)
        val __tmp_1245 = (101.U(8.W)).asUInt
        arrayRegFiles(__tmp_1244 + 0.U) := __tmp_1245(7, 0)

        val __tmp_1246 = SP + 30.U(16.W)
        val __tmp_1247 = (117.U(8.W)).asUInt
        arrayRegFiles(__tmp_1246 + 0.U) := __tmp_1247(7, 0)

        val __tmp_1248 = SP + 31.U(16.W)
        val __tmp_1249 = (109.U(8.W)).asUInt
        arrayRegFiles(__tmp_1248 + 0.U) := __tmp_1249(7, 0)

        val __tmp_1250 = SP + 32.U(16.W)
        val __tmp_1251 = (46.U(8.W)).asUInt
        arrayRegFiles(__tmp_1250 + 0.U) := __tmp_1251(7, 0)

        val __tmp_1252 = SP + 33.U(16.W)
        val __tmp_1253 = (97.U(8.W)).asUInt
        arrayRegFiles(__tmp_1252 + 0.U) := __tmp_1253(7, 0)

        val __tmp_1254 = SP + 34.U(16.W)
        val __tmp_1255 = (110.U(8.W)).asUInt
        arrayRegFiles(__tmp_1254 + 0.U) := __tmp_1255(7, 0)

        val __tmp_1256 = SP + 35.U(16.W)
        val __tmp_1257 = (118.U(8.W)).asUInt
        arrayRegFiles(__tmp_1256 + 0.U) := __tmp_1257(7, 0)

        val __tmp_1258 = SP + 36.U(16.W)
        val __tmp_1259 = (105.U(8.W)).asUInt
        arrayRegFiles(__tmp_1258 + 0.U) := __tmp_1259(7, 0)

        val __tmp_1260 = SP + 37.U(16.W)
        val __tmp_1261 = (108.U(8.W)).asUInt
        arrayRegFiles(__tmp_1260 + 0.U) := __tmp_1261(7, 0)

        val __tmp_1262 = SP + 38.U(16.W)
        val __tmp_1263 = (46.U(8.W)).asUInt
        arrayRegFiles(__tmp_1262 + 0.U) := __tmp_1263(7, 0)

        val __tmp_1264 = SP + 39.U(16.W)
        val __tmp_1265 = (82.U(8.W)).asUInt
        arrayRegFiles(__tmp_1264 + 0.U) := __tmp_1265(7, 0)

        val __tmp_1266 = SP + 40.U(16.W)
        val __tmp_1267 = (117.U(8.W)).asUInt
        arrayRegFiles(__tmp_1266 + 0.U) := __tmp_1267(7, 0)

        val __tmp_1268 = SP + 41.U(16.W)
        val __tmp_1269 = (110.U(8.W)).asUInt
        arrayRegFiles(__tmp_1268 + 0.U) := __tmp_1269(7, 0)

        val __tmp_1270 = SP + 42.U(16.W)
        val __tmp_1271 = (116.U(8.W)).asUInt
        arrayRegFiles(__tmp_1270 + 0.U) := __tmp_1271(7, 0)

        val __tmp_1272 = SP + 43.U(16.W)
        val __tmp_1273 = (105.U(8.W)).asUInt
        arrayRegFiles(__tmp_1272 + 0.U) := __tmp_1273(7, 0)

        val __tmp_1274 = SP + 44.U(16.W)
        val __tmp_1275 = (109.U(8.W)).asUInt
        arrayRegFiles(__tmp_1274 + 0.U) := __tmp_1275(7, 0)

        val __tmp_1276 = SP + 45.U(16.W)
        val __tmp_1277 = (101.U(8.W)).asUInt
        arrayRegFiles(__tmp_1276 + 0.U) := __tmp_1277(7, 0)

        val __tmp_1278 = SP + 46.U(16.W)
        val __tmp_1279 = (46.U(8.W)).asUInt
        arrayRegFiles(__tmp_1278 + 0.U) := __tmp_1279(7, 0)

        val __tmp_1280 = SP + 47.U(16.W)
        val __tmp_1281 = (108.U(8.W)).asUInt
        arrayRegFiles(__tmp_1280 + 0.U) := __tmp_1281(7, 0)

        val __tmp_1282 = SP + 48.U(16.W)
        val __tmp_1283 = (111.U(8.W)).asUInt
        arrayRegFiles(__tmp_1282 + 0.U) := __tmp_1283(7, 0)

        val __tmp_1284 = SP + 49.U(16.W)
        val __tmp_1285 = (97.U(8.W)).asUInt
        arrayRegFiles(__tmp_1284 + 0.U) := __tmp_1285(7, 0)

        val __tmp_1286 = SP + 50.U(16.W)
        val __tmp_1287 = (100.U(8.W)).asUInt
        arrayRegFiles(__tmp_1286 + 0.U) := __tmp_1287(7, 0)

        val __tmp_1288 = SP + 51.U(16.W)
        val __tmp_1289 = (32.U(8.W)).asUInt
        arrayRegFiles(__tmp_1288 + 0.U) := __tmp_1289(7, 0)

        val __tmp_1290 = SP + 52.U(16.W)
        val __tmp_1291 = (40.U(8.W)).asUInt
        arrayRegFiles(__tmp_1290 + 0.U) := __tmp_1291(7, 0)

        val __tmp_1292 = SP + 53.U(16.W)
        val __tmp_1293 = (82.U(8.W)).asUInt
        arrayRegFiles(__tmp_1292 + 0.U) := __tmp_1293(7, 0)

        val __tmp_1294 = SP + 54.U(16.W)
        val __tmp_1295 = (117.U(8.W)).asUInt
        arrayRegFiles(__tmp_1294 + 0.U) := __tmp_1295(7, 0)

        val __tmp_1296 = SP + 55.U(16.W)
        val __tmp_1297 = (110.U(8.W)).asUInt
        arrayRegFiles(__tmp_1296 + 0.U) := __tmp_1297(7, 0)

        val __tmp_1298 = SP + 56.U(16.W)
        val __tmp_1299 = (116.U(8.W)).asUInt
        arrayRegFiles(__tmp_1298 + 0.U) := __tmp_1299(7, 0)

        val __tmp_1300 = SP + 57.U(16.W)
        val __tmp_1301 = (105.U(8.W)).asUInt
        arrayRegFiles(__tmp_1300 + 0.U) := __tmp_1301(7, 0)

        val __tmp_1302 = SP + 58.U(16.W)
        val __tmp_1303 = (109.U(8.W)).asUInt
        arrayRegFiles(__tmp_1302 + 0.U) := __tmp_1303(7, 0)

        val __tmp_1304 = SP + 59.U(16.W)
        val __tmp_1305 = (101.U(8.W)).asUInt
        arrayRegFiles(__tmp_1304 + 0.U) := __tmp_1305(7, 0)

        val __tmp_1306 = SP + 60.U(16.W)
        val __tmp_1307 = (46.U(8.W)).asUInt
        arrayRegFiles(__tmp_1306 + 0.U) := __tmp_1307(7, 0)

        val __tmp_1308 = SP + 61.U(16.W)
        val __tmp_1309 = (115.U(8.W)).asUInt
        arrayRegFiles(__tmp_1308 + 0.U) := __tmp_1309(7, 0)

        val __tmp_1310 = SP + 62.U(16.W)
        val __tmp_1311 = (99.U(8.W)).asUInt
        arrayRegFiles(__tmp_1310 + 0.U) := __tmp_1311(7, 0)

        val __tmp_1312 = SP + 63.U(16.W)
        val __tmp_1313 = (97.U(8.W)).asUInt
        arrayRegFiles(__tmp_1312 + 0.U) := __tmp_1313(7, 0)

        val __tmp_1314 = SP + 64.U(16.W)
        val __tmp_1315 = (108.U(8.W)).asUInt
        arrayRegFiles(__tmp_1314 + 0.U) := __tmp_1315(7, 0)

        val __tmp_1316 = SP + 65.U(16.W)
        val __tmp_1317 = (97.U(8.W)).asUInt
        arrayRegFiles(__tmp_1316 + 0.U) := __tmp_1317(7, 0)

        val __tmp_1318 = SP + 66.U(16.W)
        val __tmp_1319 = (58.U(8.W)).asUInt
        arrayRegFiles(__tmp_1318 + 0.U) := __tmp_1319(7, 0)

        val __tmp_1320 = SP + 6.U(16.W)
        val __tmp_1321 = (619.S(32.W)).asUInt
        arrayRegFiles(__tmp_1320 + 0.U) := __tmp_1321(7, 0)
        arrayRegFiles(__tmp_1320 + 1.U) := __tmp_1321(15, 8)
        arrayRegFiles(__tmp_1320 + 2.U) := __tmp_1321(23, 16)
        arrayRegFiles(__tmp_1320 + 3.U) := __tmp_1321(31, 24)

        CP := 53.U
      }

      is(53.U) {
        /*
        decl r: anvil.PrinterIndex.U [@87, 8]
        *(SP + (87 [SP])) = (0 [anvil.PrinterIndex.U]) [signed, anvil.PrinterIndex.U, 8]  // r = (0 [anvil.PrinterIndex.U])
        goto .54
        */


        val __tmp_1322 = SP + 87.U(16.W)
        val __tmp_1323 = (0.S(64.W)).asUInt
        arrayRegFiles(__tmp_1322 + 0.U) := __tmp_1323(7, 0)
        arrayRegFiles(__tmp_1322 + 1.U) := __tmp_1323(15, 8)
        arrayRegFiles(__tmp_1322 + 2.U) := __tmp_1323(23, 16)
        arrayRegFiles(__tmp_1322 + 3.U) := __tmp_1323(31, 24)
        arrayRegFiles(__tmp_1322 + 4.U) := __tmp_1323(39, 32)
        arrayRegFiles(__tmp_1322 + 5.U) := __tmp_1323(47, 40)
        arrayRegFiles(__tmp_1322 + 6.U) := __tmp_1323(55, 48)
        arrayRegFiles(__tmp_1322 + 7.U) := __tmp_1323(63, 56)

        CP := 54.U
      }

      is(54.U) {
        /*
        *(SP + (6 [SP])) = (620 [U32]) [signed, U32, 4]  // $sfLoc = (620 [U32])
        goto .55
        */


        val __tmp_1324 = SP + 6.U(16.W)
        val __tmp_1325 = (620.S(32.W)).asUInt
        arrayRegFiles(__tmp_1324 + 0.U) := __tmp_1325(7, 0)
        arrayRegFiles(__tmp_1324 + 1.U) := __tmp_1325(15, 8)
        arrayRegFiles(__tmp_1324 + 2.U) := __tmp_1325(23, 16)
        arrayRegFiles(__tmp_1324 + 3.U) := __tmp_1325(31, 24)

        CP := 55.U
      }

      is(55.U) {
        /*
        decl i: anvil.PrinterIndex.U [@95, 8]
        *(SP + (95 [SP])) = (0 [anvil.PrinterIndex.U]) [signed, anvil.PrinterIndex.U, 8]  // i = (0 [anvil.PrinterIndex.U])
        goto .56
        */


        val __tmp_1326 = SP + 95.U(16.W)
        val __tmp_1327 = (0.S(64.W)).asUInt
        arrayRegFiles(__tmp_1326 + 0.U) := __tmp_1327(7, 0)
        arrayRegFiles(__tmp_1326 + 1.U) := __tmp_1327(15, 8)
        arrayRegFiles(__tmp_1326 + 2.U) := __tmp_1327(23, 16)
        arrayRegFiles(__tmp_1326 + 3.U) := __tmp_1327(31, 24)
        arrayRegFiles(__tmp_1326 + 4.U) := __tmp_1327(39, 32)
        arrayRegFiles(__tmp_1326 + 5.U) := __tmp_1327(47, 40)
        arrayRegFiles(__tmp_1326 + 6.U) := __tmp_1327(55, 48)
        arrayRegFiles(__tmp_1326 + 7.U) := __tmp_1327(63, 56)

        CP := 56.U
      }

      is(56.U) {
        /*
        *(SP + (6 [SP])) = (621 [U32]) [signed, U32, 4]  // $sfLoc = (621 [U32])
        goto .57
        */


        val __tmp_1328 = SP + 6.U(16.W)
        val __tmp_1329 = (621.S(32.W)).asUInt
        arrayRegFiles(__tmp_1328 + 0.U) := __tmp_1329(7, 0)
        arrayRegFiles(__tmp_1328 + 1.U) := __tmp_1329(15, 8)
        arrayRegFiles(__tmp_1328 + 2.U) := __tmp_1329(23, 16)
        arrayRegFiles(__tmp_1328 + 3.U) := __tmp_1329(31, 24)

        CP := 57.U
      }

      is(57.U) {
        /*
        *(SP + (6 [SP])) = (621 [U32]) [signed, U32, 4]  // $sfLoc = (621 [U32])
        goto .58
        */


        val __tmp_1330 = SP + 6.U(16.W)
        val __tmp_1331 = (621.S(32.W)).asUInt
        arrayRegFiles(__tmp_1330 + 0.U) := __tmp_1331(7, 0)
        arrayRegFiles(__tmp_1330 + 1.U) := __tmp_1331(15, 8)
        arrayRegFiles(__tmp_1330 + 2.U) := __tmp_1331(23, 16)
        arrayRegFiles(__tmp_1330 + 3.U) := __tmp_1331(31, 24)

        CP := 58.U
      }

      is(58.U) {
        /*
        $0 = *(SP + (95 [SP])) [unsigned, anvil.PrinterIndex.U, 8]  // $0 = i
        $1 = *(SP + (79 [SP])) [unsigned, anvil.PrinterIndex.U, 8]  // $1 = size
        goto .59
        */


        val __tmp_1332 = (SP + 95.U(16.W)).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_1332 + 7.U),
          arrayRegFiles(__tmp_1332 + 6.U),
          arrayRegFiles(__tmp_1332 + 5.U),
          arrayRegFiles(__tmp_1332 + 4.U),
          arrayRegFiles(__tmp_1332 + 3.U),
          arrayRegFiles(__tmp_1332 + 2.U),
          arrayRegFiles(__tmp_1332 + 1.U),
          arrayRegFiles(__tmp_1332 + 0.U)
        ).asUInt

        val __tmp_1333 = (SP + 79.U(16.W)).asUInt
        generalRegFiles(1.U) := Cat(
          arrayRegFiles(__tmp_1333 + 7.U),
          arrayRegFiles(__tmp_1333 + 6.U),
          arrayRegFiles(__tmp_1333 + 5.U),
          arrayRegFiles(__tmp_1333 + 4.U),
          arrayRegFiles(__tmp_1333 + 3.U),
          arrayRegFiles(__tmp_1333 + 2.U),
          arrayRegFiles(__tmp_1333 + 1.U),
          arrayRegFiles(__tmp_1333 + 0.U)
        ).asUInt

        CP := 59.U
      }

      is(59.U) {
        /*
        $2 = ($0 < $1)
        goto .60
        */


        generalRegFiles(2.U) := (generalRegFiles(0.U) < generalRegFiles(1.U)).asUInt
        CP := 60.U
      }

      is(60.U) {
        /*
        if $2 goto .61 else goto .91
        */


        CP := Mux((generalRegFiles(2.U).asUInt) === 1.U, 61.U, 91.U)
      }

      is(61.U) {
        /*
        *(SP + (6 [SP])) = (622 [U32]) [signed, U32, 4]  // $sfLoc = (622 [U32])
        goto .62
        */


        val __tmp_1334 = SP + 6.U(16.W)
        val __tmp_1335 = (622.S(32.W)).asUInt
        arrayRegFiles(__tmp_1334 + 0.U) := __tmp_1335(7, 0)
        arrayRegFiles(__tmp_1334 + 1.U) := __tmp_1335(15, 8)
        arrayRegFiles(__tmp_1334 + 2.U) := __tmp_1335(23, 16)
        arrayRegFiles(__tmp_1334 + 3.U) := __tmp_1335(31, 24)

        CP := 62.U
      }

      is(62.U) {
        /*
        decl b: anvil.PrinterIndex.U [@103, 8]
        $0 = *(SP + (69 [SP])) [unsigned, MS[anvil.PrinterIndex.U, U8], 2]  // $0 = memory
        $1 = *(SP + (71 [SP])) [unsigned, anvil.PrinterIndex.U, 8]  // $1 = offset
        $2 = *(SP + (95 [SP])) [unsigned, anvil.PrinterIndex.U, 8]  // $2 = i
        goto .63
        */


        val __tmp_1336 = (SP + 69.U(16.W)).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_1336 + 1.U),
          arrayRegFiles(__tmp_1336 + 0.U)
        ).asUInt

        val __tmp_1337 = (SP + 71.U(16.W)).asUInt
        generalRegFiles(1.U) := Cat(
          arrayRegFiles(__tmp_1337 + 7.U),
          arrayRegFiles(__tmp_1337 + 6.U),
          arrayRegFiles(__tmp_1337 + 5.U),
          arrayRegFiles(__tmp_1337 + 4.U),
          arrayRegFiles(__tmp_1337 + 3.U),
          arrayRegFiles(__tmp_1337 + 2.U),
          arrayRegFiles(__tmp_1337 + 1.U),
          arrayRegFiles(__tmp_1337 + 0.U)
        ).asUInt

        val __tmp_1338 = (SP + 95.U(16.W)).asUInt
        generalRegFiles(2.U) := Cat(
          arrayRegFiles(__tmp_1338 + 7.U),
          arrayRegFiles(__tmp_1338 + 6.U),
          arrayRegFiles(__tmp_1338 + 5.U),
          arrayRegFiles(__tmp_1338 + 4.U),
          arrayRegFiles(__tmp_1338 + 3.U),
          arrayRegFiles(__tmp_1338 + 2.U),
          arrayRegFiles(__tmp_1338 + 1.U),
          arrayRegFiles(__tmp_1338 + 0.U)
        ).asUInt

        CP := 63.U
      }

      is(63.U) {
        /*
        $3 = ($1 + $2)
        goto .64
        */


        generalRegFiles(3.U) := generalRegFiles(1.U) + generalRegFiles(2.U)
        CP := 64.U
      }

      is(64.U) {
        /*
        $4 = *(($0 + (12 [SP])) + ($3 as SP)) [unsigned, U8, 1]  // $4 = $0($3)
        goto .65
        */


        val __tmp_1339 = (generalRegFiles(0.U) + 12.U(16.W) + generalRegFiles(3.U).asUInt).asUInt
        generalRegFiles(4.U) := Cat(
          arrayRegFiles(__tmp_1339 + 0.U)
        ).asUInt

        CP := 65.U
      }

      is(65.U) {
        /*
        $5 = ($4 as Z)
        goto .66
        */


        generalRegFiles(5.U) := (generalRegFiles(4.U).asSInt).asUInt
        CP := 66.U
      }

      is(66.U) {
        /*
        if ((0 [Z]) <= $5) goto .67 else goto .82
        */


        CP := Mux(((0.S(64.W) <= generalRegFiles(5.U).asSInt).asUInt.asUInt) === 1.U, 67.U, 82.U)
      }

      is(67.U) {
        /*
        *(SP + (6 [SP])) = (622 [U32]) [signed, U32, 4]  // $sfLoc = (622 [U32])
        goto .68
        */


        val __tmp_1340 = SP + 6.U(16.W)
        val __tmp_1341 = (622.S(32.W)).asUInt
        arrayRegFiles(__tmp_1340 + 0.U) := __tmp_1341(7, 0)
        arrayRegFiles(__tmp_1340 + 1.U) := __tmp_1341(15, 8)
        arrayRegFiles(__tmp_1340 + 2.U) := __tmp_1341(23, 16)
        arrayRegFiles(__tmp_1340 + 3.U) := __tmp_1341(31, 24)

        CP := 68.U
      }

      is(68.U) {
        /*
        $6 = ($5 as Z)
        goto .69
        */


        generalRegFiles(6.U) := (generalRegFiles(5.U).asSInt.asSInt).asUInt
        CP := 69.U
      }

      is(69.U) {
        /*
        *(SP + (103 [SP])) = $6 [signed, anvil.PrinterIndex.U, 8]  // b = $6
        goto .70
        */


        val __tmp_1342 = SP + 103.U(16.W)
        val __tmp_1343 = (generalRegFiles(6.U)).asUInt
        arrayRegFiles(__tmp_1342 + 0.U) := __tmp_1343(7, 0)
        arrayRegFiles(__tmp_1342 + 1.U) := __tmp_1343(15, 8)
        arrayRegFiles(__tmp_1342 + 2.U) := __tmp_1343(23, 16)
        arrayRegFiles(__tmp_1342 + 3.U) := __tmp_1343(31, 24)
        arrayRegFiles(__tmp_1342 + 4.U) := __tmp_1343(39, 32)
        arrayRegFiles(__tmp_1342 + 5.U) := __tmp_1343(47, 40)
        arrayRegFiles(__tmp_1342 + 6.U) := __tmp_1343(55, 48)
        arrayRegFiles(__tmp_1342 + 7.U) := __tmp_1343(63, 56)

        CP := 70.U
      }

      is(70.U) {
        /*
        *(SP + (6 [SP])) = (623 [U32]) [signed, U32, 4]  // $sfLoc = (623 [U32])
        goto .71
        */


        val __tmp_1344 = SP + 6.U(16.W)
        val __tmp_1345 = (623.S(32.W)).asUInt
        arrayRegFiles(__tmp_1344 + 0.U) := __tmp_1345(7, 0)
        arrayRegFiles(__tmp_1344 + 1.U) := __tmp_1345(15, 8)
        arrayRegFiles(__tmp_1344 + 2.U) := __tmp_1345(23, 16)
        arrayRegFiles(__tmp_1344 + 3.U) := __tmp_1345(31, 24)

        CP := 71.U
      }

      is(71.U) {
        /*
        $0 = *(SP + (87 [SP])) [unsigned, anvil.PrinterIndex.U, 8]  // $0 = r
        $1 = *(SP + (103 [SP])) [unsigned, anvil.PrinterIndex.U, 8]  // $1 = b
        $2 = *(SP + (95 [SP])) [unsigned, anvil.PrinterIndex.U, 8]  // $2 = i
        goto .72
        */


        val __tmp_1346 = (SP + 87.U(16.W)).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_1346 + 7.U),
          arrayRegFiles(__tmp_1346 + 6.U),
          arrayRegFiles(__tmp_1346 + 5.U),
          arrayRegFiles(__tmp_1346 + 4.U),
          arrayRegFiles(__tmp_1346 + 3.U),
          arrayRegFiles(__tmp_1346 + 2.U),
          arrayRegFiles(__tmp_1346 + 1.U),
          arrayRegFiles(__tmp_1346 + 0.U)
        ).asUInt

        val __tmp_1347 = (SP + 103.U(16.W)).asUInt
        generalRegFiles(1.U) := Cat(
          arrayRegFiles(__tmp_1347 + 7.U),
          arrayRegFiles(__tmp_1347 + 6.U),
          arrayRegFiles(__tmp_1347 + 5.U),
          arrayRegFiles(__tmp_1347 + 4.U),
          arrayRegFiles(__tmp_1347 + 3.U),
          arrayRegFiles(__tmp_1347 + 2.U),
          arrayRegFiles(__tmp_1347 + 1.U),
          arrayRegFiles(__tmp_1347 + 0.U)
        ).asUInt

        val __tmp_1348 = (SP + 95.U(16.W)).asUInt
        generalRegFiles(2.U) := Cat(
          arrayRegFiles(__tmp_1348 + 7.U),
          arrayRegFiles(__tmp_1348 + 6.U),
          arrayRegFiles(__tmp_1348 + 5.U),
          arrayRegFiles(__tmp_1348 + 4.U),
          arrayRegFiles(__tmp_1348 + 3.U),
          arrayRegFiles(__tmp_1348 + 2.U),
          arrayRegFiles(__tmp_1348 + 1.U),
          arrayRegFiles(__tmp_1348 + 0.U)
        ).asUInt

        CP := 72.U
      }

      is(72.U) {
        /*
        $3 = ($2 * (8 [anvil.PrinterIndex.U]))
        goto .73
        */


        generalRegFiles(3.U) := generalRegFiles(2.U) * 8.U(64.W)
        CP := 73.U
      }

      is(73.U) {
        /*
        $4 = ($1 << $3)
        goto .74
        */


        generalRegFiles(4.U) := (generalRegFiles(1.U)).asUInt << generalRegFiles(3.U)(4,0)
        CP := 74.U
      }

      is(74.U) {
        /*
        $5 = ($0 | $4)
        goto .75
        */


        generalRegFiles(5.U) := generalRegFiles(0.U) | generalRegFiles(4.U)
        CP := 75.U
      }

      is(75.U) {
        /*
        *(SP + (87 [SP])) = $5 [signed, anvil.PrinterIndex.U, 8]  // r = $5
        goto .76
        */


        val __tmp_1349 = SP + 87.U(16.W)
        val __tmp_1350 = (generalRegFiles(5.U)).asUInt
        arrayRegFiles(__tmp_1349 + 0.U) := __tmp_1350(7, 0)
        arrayRegFiles(__tmp_1349 + 1.U) := __tmp_1350(15, 8)
        arrayRegFiles(__tmp_1349 + 2.U) := __tmp_1350(23, 16)
        arrayRegFiles(__tmp_1349 + 3.U) := __tmp_1350(31, 24)
        arrayRegFiles(__tmp_1349 + 4.U) := __tmp_1350(39, 32)
        arrayRegFiles(__tmp_1349 + 5.U) := __tmp_1350(47, 40)
        arrayRegFiles(__tmp_1349 + 6.U) := __tmp_1350(55, 48)
        arrayRegFiles(__tmp_1349 + 7.U) := __tmp_1350(63, 56)

        CP := 76.U
      }

      is(76.U) {
        /*
        *(SP + (6 [SP])) = (624 [U32]) [signed, U32, 4]  // $sfLoc = (624 [U32])
        goto .77
        */


        val __tmp_1351 = SP + 6.U(16.W)
        val __tmp_1352 = (624.S(32.W)).asUInt
        arrayRegFiles(__tmp_1351 + 0.U) := __tmp_1352(7, 0)
        arrayRegFiles(__tmp_1351 + 1.U) := __tmp_1352(15, 8)
        arrayRegFiles(__tmp_1351 + 2.U) := __tmp_1352(23, 16)
        arrayRegFiles(__tmp_1351 + 3.U) := __tmp_1352(31, 24)

        CP := 77.U
      }

      is(77.U) {
        /*
        $0 = *(SP + (95 [SP])) [unsigned, anvil.PrinterIndex.U, 8]  // $0 = i
        goto .78
        */


        val __tmp_1353 = (SP + 95.U(16.W)).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_1353 + 7.U),
          arrayRegFiles(__tmp_1353 + 6.U),
          arrayRegFiles(__tmp_1353 + 5.U),
          arrayRegFiles(__tmp_1353 + 4.U),
          arrayRegFiles(__tmp_1353 + 3.U),
          arrayRegFiles(__tmp_1353 + 2.U),
          arrayRegFiles(__tmp_1353 + 1.U),
          arrayRegFiles(__tmp_1353 + 0.U)
        ).asUInt

        CP := 78.U
      }

      is(78.U) {
        /*
        $1 = ($0 + (1 [anvil.PrinterIndex.U]))
        goto .79
        */


        generalRegFiles(1.U) := generalRegFiles(0.U) + 1.U(64.W)
        CP := 79.U
      }

      is(79.U) {
        /*
        *(SP + (95 [SP])) = $1 [signed, anvil.PrinterIndex.U, 8]  // i = $1
        goto .80
        */


        val __tmp_1354 = SP + 95.U(16.W)
        val __tmp_1355 = (generalRegFiles(1.U)).asUInt
        arrayRegFiles(__tmp_1354 + 0.U) := __tmp_1355(7, 0)
        arrayRegFiles(__tmp_1354 + 1.U) := __tmp_1355(15, 8)
        arrayRegFiles(__tmp_1354 + 2.U) := __tmp_1355(23, 16)
        arrayRegFiles(__tmp_1354 + 3.U) := __tmp_1355(31, 24)
        arrayRegFiles(__tmp_1354 + 4.U) := __tmp_1355(39, 32)
        arrayRegFiles(__tmp_1354 + 5.U) := __tmp_1355(47, 40)
        arrayRegFiles(__tmp_1354 + 6.U) := __tmp_1355(55, 48)
        arrayRegFiles(__tmp_1354 + 7.U) := __tmp_1355(63, 56)

        CP := 80.U
      }

      is(80.U) {
        /*
        *(SP + (6 [SP])) = (622 [U32]) [signed, U32, 4]  // $sfLoc = (622 [U32])
        goto .81
        */


        val __tmp_1356 = SP + 6.U(16.W)
        val __tmp_1357 = (622.S(32.W)).asUInt
        arrayRegFiles(__tmp_1356 + 0.U) := __tmp_1357(7, 0)
        arrayRegFiles(__tmp_1356 + 1.U) := __tmp_1357(15, 8)
        arrayRegFiles(__tmp_1356 + 2.U) := __tmp_1357(23, 16)
        arrayRegFiles(__tmp_1356 + 3.U) := __tmp_1357(31, 24)

        CP := 81.U
      }

      is(81.U) {
        /*
        undecl b: anvil.PrinterIndex.U [@103, 8]
        *(SP + (6 [SP])) = (621 [U32]) [signed, U32, 4]  // $sfLoc = (621 [U32])
        goto .57
        */


        val __tmp_1358 = SP + 6.U(16.W)
        val __tmp_1359 = (621.S(32.W)).asUInt
        arrayRegFiles(__tmp_1358 + 0.U) := __tmp_1359(7, 0)
        arrayRegFiles(__tmp_1358 + 1.U) := __tmp_1359(15, 8)
        arrayRegFiles(__tmp_1358 + 2.U) := __tmp_1359(23, 16)
        arrayRegFiles(__tmp_1358 + 3.U) := __tmp_1359(31, 24)

        CP := 57.U
      }

      is(82.U) {
        /*
        *(SP + (6 [SP])) = (622 [U32]) [signed, U32, 4]  // $sfLoc = (622 [U32])
        goto .83
        */


        val __tmp_1360 = SP + 6.U(16.W)
        val __tmp_1361 = (622.S(32.W)).asUInt
        arrayRegFiles(__tmp_1360 + 0.U) := __tmp_1361(7, 0)
        arrayRegFiles(__tmp_1360 + 1.U) := __tmp_1361(15, 8)
        arrayRegFiles(__tmp_1360 + 2.U) := __tmp_1361(23, 16)
        arrayRegFiles(__tmp_1360 + 3.U) := __tmp_1361(31, 24)

        CP := 83.U
      }

      is(83.U) {
        /*
        *((*(22 [SP]) + (12 [SP])) + ((DP & (127 [DP])) as SP)) = (79 [U8]) [unsigned, U8, 1]  // $display((DP & (127 [DP]))) = (79 [U8])
        *((*(22 [SP]) + (12 [SP])) + (((DP + (1 [DP])) & (127 [DP])) as SP)) = (117 [U8]) [unsigned, U8, 1]  // $display(((DP + (1 [DP])) & (127 [DP]))) = (117 [U8])
        *((*(22 [SP]) + (12 [SP])) + (((DP + (2 [DP])) & (127 [DP])) as SP)) = (116 [U8]) [unsigned, U8, 1]  // $display(((DP + (2 [DP])) & (127 [DP]))) = (116 [U8])
        *((*(22 [SP]) + (12 [SP])) + (((DP + (3 [DP])) & (127 [DP])) as SP)) = (32 [U8]) [unsigned, U8, 1]  // $display(((DP + (3 [DP])) & (127 [DP]))) = (32 [U8])
        *((*(22 [SP]) + (12 [SP])) + (((DP + (4 [DP])) & (127 [DP])) as SP)) = (111 [U8]) [unsigned, U8, 1]  // $display(((DP + (4 [DP])) & (127 [DP]))) = (111 [U8])
        *((*(22 [SP]) + (12 [SP])) + (((DP + (5 [DP])) & (127 [DP])) as SP)) = (102 [U8]) [unsigned, U8, 1]  // $display(((DP + (5 [DP])) & (127 [DP]))) = (102 [U8])
        *((*(22 [SP]) + (12 [SP])) + (((DP + (6 [DP])) & (127 [DP])) as SP)) = (32 [U8]) [unsigned, U8, 1]  // $display(((DP + (6 [DP])) & (127 [DP]))) = (32 [U8])
        *((*(22 [SP]) + (12 [SP])) + (((DP + (7 [DP])) & (127 [DP])) as SP)) = (98 [U8]) [unsigned, U8, 1]  // $display(((DP + (7 [DP])) & (127 [DP]))) = (98 [U8])
        *((*(22 [SP]) + (12 [SP])) + (((DP + (8 [DP])) & (127 [DP])) as SP)) = (111 [U8]) [unsigned, U8, 1]  // $display(((DP + (8 [DP])) & (127 [DP]))) = (111 [U8])
        *((*(22 [SP]) + (12 [SP])) + (((DP + (9 [DP])) & (127 [DP])) as SP)) = (117 [U8]) [unsigned, U8, 1]  // $display(((DP + (9 [DP])) & (127 [DP]))) = (117 [U8])
        *((*(22 [SP]) + (12 [SP])) + (((DP + (10 [DP])) & (127 [DP])) as SP)) = (110 [U8]) [unsigned, U8, 1]  // $display(((DP + (10 [DP])) & (127 [DP]))) = (110 [U8])
        *((*(22 [SP]) + (12 [SP])) + (((DP + (11 [DP])) & (127 [DP])) as SP)) = (100 [U8]) [unsigned, U8, 1]  // $display(((DP + (11 [DP])) & (127 [DP]))) = (100 [U8])
        *((*(22 [SP]) + (12 [SP])) + (((DP + (12 [DP])) & (127 [DP])) as SP)) = (32 [U8]) [unsigned, U8, 1]  // $display(((DP + (12 [DP])) & (127 [DP]))) = (32 [U8])
        *((*(22 [SP]) + (12 [SP])) + (((DP + (13 [DP])) & (127 [DP])) as SP)) = (97 [U8]) [unsigned, U8, 1]  // $display(((DP + (13 [DP])) & (127 [DP]))) = (97 [U8])
        *((*(22 [SP]) + (12 [SP])) + (((DP + (14 [DP])) & (127 [DP])) as SP)) = (110 [U8]) [unsigned, U8, 1]  // $display(((DP + (14 [DP])) & (127 [DP]))) = (110 [U8])
        *((*(22 [SP]) + (12 [SP])) + (((DP + (15 [DP])) & (127 [DP])) as SP)) = (118 [U8]) [unsigned, U8, 1]  // $display(((DP + (15 [DP])) & (127 [DP]))) = (118 [U8])
        *((*(22 [SP]) + (12 [SP])) + (((DP + (16 [DP])) & (127 [DP])) as SP)) = (105 [U8]) [unsigned, U8, 1]  // $display(((DP + (16 [DP])) & (127 [DP]))) = (105 [U8])
        *((*(22 [SP]) + (12 [SP])) + (((DP + (17 [DP])) & (127 [DP])) as SP)) = (108 [U8]) [unsigned, U8, 1]  // $display(((DP + (17 [DP])) & (127 [DP]))) = (108 [U8])
        *((*(22 [SP]) + (12 [SP])) + (((DP + (18 [DP])) & (127 [DP])) as SP)) = (46 [U8]) [unsigned, U8, 1]  // $display(((DP + (18 [DP])) & (127 [DP]))) = (46 [U8])
        *((*(22 [SP]) + (12 [SP])) + (((DP + (19 [DP])) & (127 [DP])) as SP)) = (80 [U8]) [unsigned, U8, 1]  // $display(((DP + (19 [DP])) & (127 [DP]))) = (80 [U8])
        *((*(22 [SP]) + (12 [SP])) + (((DP + (20 [DP])) & (127 [DP])) as SP)) = (114 [U8]) [unsigned, U8, 1]  // $display(((DP + (20 [DP])) & (127 [DP]))) = (114 [U8])
        *((*(22 [SP]) + (12 [SP])) + (((DP + (21 [DP])) & (127 [DP])) as SP)) = (105 [U8]) [unsigned, U8, 1]  // $display(((DP + (21 [DP])) & (127 [DP]))) = (105 [U8])
        *((*(22 [SP]) + (12 [SP])) + (((DP + (22 [DP])) & (127 [DP])) as SP)) = (110 [U8]) [unsigned, U8, 1]  // $display(((DP + (22 [DP])) & (127 [DP]))) = (110 [U8])
        *((*(22 [SP]) + (12 [SP])) + (((DP + (23 [DP])) & (127 [DP])) as SP)) = (116 [U8]) [unsigned, U8, 1]  // $display(((DP + (23 [DP])) & (127 [DP]))) = (116 [U8])
        *((*(22 [SP]) + (12 [SP])) + (((DP + (24 [DP])) & (127 [DP])) as SP)) = (101 [U8]) [unsigned, U8, 1]  // $display(((DP + (24 [DP])) & (127 [DP]))) = (101 [U8])
        *((*(22 [SP]) + (12 [SP])) + (((DP + (25 [DP])) & (127 [DP])) as SP)) = (114 [U8]) [unsigned, U8, 1]  // $display(((DP + (25 [DP])) & (127 [DP]))) = (114 [U8])
        *((*(22 [SP]) + (12 [SP])) + (((DP + (26 [DP])) & (127 [DP])) as SP)) = (73 [U8]) [unsigned, U8, 1]  // $display(((DP + (26 [DP])) & (127 [DP]))) = (73 [U8])
        *((*(22 [SP]) + (12 [SP])) + (((DP + (27 [DP])) & (127 [DP])) as SP)) = (110 [U8]) [unsigned, U8, 1]  // $display(((DP + (27 [DP])) & (127 [DP]))) = (110 [U8])
        *((*(22 [SP]) + (12 [SP])) + (((DP + (28 [DP])) & (127 [DP])) as SP)) = (100 [U8]) [unsigned, U8, 1]  // $display(((DP + (28 [DP])) & (127 [DP]))) = (100 [U8])
        *((*(22 [SP]) + (12 [SP])) + (((DP + (29 [DP])) & (127 [DP])) as SP)) = (101 [U8]) [unsigned, U8, 1]  // $display(((DP + (29 [DP])) & (127 [DP]))) = (101 [U8])
        *((*(22 [SP]) + (12 [SP])) + (((DP + (30 [DP])) & (127 [DP])) as SP)) = (120 [U8]) [unsigned, U8, 1]  // $display(((DP + (30 [DP])) & (127 [DP]))) = (120 [U8])
        *((*(22 [SP]) + (12 [SP])) + (((DP + (31 [DP])) & (127 [DP])) as SP)) = (46 [U8]) [unsigned, U8, 1]  // $display(((DP + (31 [DP])) & (127 [DP]))) = (46 [U8])
        *((*(22 [SP]) + (12 [SP])) + (((DP + (32 [DP])) & (127 [DP])) as SP)) = (85 [U8]) [unsigned, U8, 1]  // $display(((DP + (32 [DP])) & (127 [DP]))) = (85 [U8])
        *((*(22 [SP]) + (12 [SP])) + (((DP + (33 [DP])) & (127 [DP])) as SP)) = (32 [U8]) [unsigned, U8, 1]  // $display(((DP + (33 [DP])) & (127 [DP]))) = (32 [U8])
        *((*(22 [SP]) + (12 [SP])) + (((DP + (34 [DP])) & (127 [DP])) as SP)) = (118 [U8]) [unsigned, U8, 1]  // $display(((DP + (34 [DP])) & (127 [DP]))) = (118 [U8])
        *((*(22 [SP]) + (12 [SP])) + (((DP + (35 [DP])) & (127 [DP])) as SP)) = (97 [U8]) [unsigned, U8, 1]  // $display(((DP + (35 [DP])) & (127 [DP]))) = (97 [U8])
        *((*(22 [SP]) + (12 [SP])) + (((DP + (36 [DP])) & (127 [DP])) as SP)) = (108 [U8]) [unsigned, U8, 1]  // $display(((DP + (36 [DP])) & (127 [DP]))) = (108 [U8])
        *((*(22 [SP]) + (12 [SP])) + (((DP + (37 [DP])) & (127 [DP])) as SP)) = (117 [U8]) [unsigned, U8, 1]  // $display(((DP + (37 [DP])) & (127 [DP]))) = (117 [U8])
        *((*(22 [SP]) + (12 [SP])) + (((DP + (38 [DP])) & (127 [DP])) as SP)) = (101 [U8]) [unsigned, U8, 1]  // $display(((DP + (38 [DP])) & (127 [DP]))) = (101 [U8])
        goto .84
        */


        val __tmp_1362 = Cat(
          arrayRegFiles(22.U(16.W) + 1.U),
          arrayRegFiles(22.U(16.W) + 0.U)
        ) + 12.U(16.W) + DP & 127.U(64.W).asUInt
        val __tmp_1363 = (79.U(8.W)).asUInt
        arrayRegFiles(__tmp_1362 + 0.U) := __tmp_1363(7, 0)

        val __tmp_1364 = Cat(
          arrayRegFiles(22.U(16.W) + 1.U),
          arrayRegFiles(22.U(16.W) + 0.U)
        ) + 12.U(16.W) + DP + 1.U(64.W) & 127.U(64.W).asUInt
        val __tmp_1365 = (117.U(8.W)).asUInt
        arrayRegFiles(__tmp_1364 + 0.U) := __tmp_1365(7, 0)

        val __tmp_1366 = Cat(
          arrayRegFiles(22.U(16.W) + 1.U),
          arrayRegFiles(22.U(16.W) + 0.U)
        ) + 12.U(16.W) + DP + 2.U(64.W) & 127.U(64.W).asUInt
        val __tmp_1367 = (116.U(8.W)).asUInt
        arrayRegFiles(__tmp_1366 + 0.U) := __tmp_1367(7, 0)

        val __tmp_1368 = Cat(
          arrayRegFiles(22.U(16.W) + 1.U),
          arrayRegFiles(22.U(16.W) + 0.U)
        ) + 12.U(16.W) + DP + 3.U(64.W) & 127.U(64.W).asUInt
        val __tmp_1369 = (32.U(8.W)).asUInt
        arrayRegFiles(__tmp_1368 + 0.U) := __tmp_1369(7, 0)

        val __tmp_1370 = Cat(
          arrayRegFiles(22.U(16.W) + 1.U),
          arrayRegFiles(22.U(16.W) + 0.U)
        ) + 12.U(16.W) + DP + 4.U(64.W) & 127.U(64.W).asUInt
        val __tmp_1371 = (111.U(8.W)).asUInt
        arrayRegFiles(__tmp_1370 + 0.U) := __tmp_1371(7, 0)

        val __tmp_1372 = Cat(
          arrayRegFiles(22.U(16.W) + 1.U),
          arrayRegFiles(22.U(16.W) + 0.U)
        ) + 12.U(16.W) + DP + 5.U(64.W) & 127.U(64.W).asUInt
        val __tmp_1373 = (102.U(8.W)).asUInt
        arrayRegFiles(__tmp_1372 + 0.U) := __tmp_1373(7, 0)

        val __tmp_1374 = Cat(
          arrayRegFiles(22.U(16.W) + 1.U),
          arrayRegFiles(22.U(16.W) + 0.U)
        ) + 12.U(16.W) + DP + 6.U(64.W) & 127.U(64.W).asUInt
        val __tmp_1375 = (32.U(8.W)).asUInt
        arrayRegFiles(__tmp_1374 + 0.U) := __tmp_1375(7, 0)

        val __tmp_1376 = Cat(
          arrayRegFiles(22.U(16.W) + 1.U),
          arrayRegFiles(22.U(16.W) + 0.U)
        ) + 12.U(16.W) + DP + 7.U(64.W) & 127.U(64.W).asUInt
        val __tmp_1377 = (98.U(8.W)).asUInt
        arrayRegFiles(__tmp_1376 + 0.U) := __tmp_1377(7, 0)

        val __tmp_1378 = Cat(
          arrayRegFiles(22.U(16.W) + 1.U),
          arrayRegFiles(22.U(16.W) + 0.U)
        ) + 12.U(16.W) + DP + 8.U(64.W) & 127.U(64.W).asUInt
        val __tmp_1379 = (111.U(8.W)).asUInt
        arrayRegFiles(__tmp_1378 + 0.U) := __tmp_1379(7, 0)

        val __tmp_1380 = Cat(
          arrayRegFiles(22.U(16.W) + 1.U),
          arrayRegFiles(22.U(16.W) + 0.U)
        ) + 12.U(16.W) + DP + 9.U(64.W) & 127.U(64.W).asUInt
        val __tmp_1381 = (117.U(8.W)).asUInt
        arrayRegFiles(__tmp_1380 + 0.U) := __tmp_1381(7, 0)

        val __tmp_1382 = Cat(
          arrayRegFiles(22.U(16.W) + 1.U),
          arrayRegFiles(22.U(16.W) + 0.U)
        ) + 12.U(16.W) + DP + 10.U(64.W) & 127.U(64.W).asUInt
        val __tmp_1383 = (110.U(8.W)).asUInt
        arrayRegFiles(__tmp_1382 + 0.U) := __tmp_1383(7, 0)

        val __tmp_1384 = Cat(
          arrayRegFiles(22.U(16.W) + 1.U),
          arrayRegFiles(22.U(16.W) + 0.U)
        ) + 12.U(16.W) + DP + 11.U(64.W) & 127.U(64.W).asUInt
        val __tmp_1385 = (100.U(8.W)).asUInt
        arrayRegFiles(__tmp_1384 + 0.U) := __tmp_1385(7, 0)

        val __tmp_1386 = Cat(
          arrayRegFiles(22.U(16.W) + 1.U),
          arrayRegFiles(22.U(16.W) + 0.U)
        ) + 12.U(16.W) + DP + 12.U(64.W) & 127.U(64.W).asUInt
        val __tmp_1387 = (32.U(8.W)).asUInt
        arrayRegFiles(__tmp_1386 + 0.U) := __tmp_1387(7, 0)

        val __tmp_1388 = Cat(
          arrayRegFiles(22.U(16.W) + 1.U),
          arrayRegFiles(22.U(16.W) + 0.U)
        ) + 12.U(16.W) + DP + 13.U(64.W) & 127.U(64.W).asUInt
        val __tmp_1389 = (97.U(8.W)).asUInt
        arrayRegFiles(__tmp_1388 + 0.U) := __tmp_1389(7, 0)

        val __tmp_1390 = Cat(
          arrayRegFiles(22.U(16.W) + 1.U),
          arrayRegFiles(22.U(16.W) + 0.U)
        ) + 12.U(16.W) + DP + 14.U(64.W) & 127.U(64.W).asUInt
        val __tmp_1391 = (110.U(8.W)).asUInt
        arrayRegFiles(__tmp_1390 + 0.U) := __tmp_1391(7, 0)

        val __tmp_1392 = Cat(
          arrayRegFiles(22.U(16.W) + 1.U),
          arrayRegFiles(22.U(16.W) + 0.U)
        ) + 12.U(16.W) + DP + 15.U(64.W) & 127.U(64.W).asUInt
        val __tmp_1393 = (118.U(8.W)).asUInt
        arrayRegFiles(__tmp_1392 + 0.U) := __tmp_1393(7, 0)

        val __tmp_1394 = Cat(
          arrayRegFiles(22.U(16.W) + 1.U),
          arrayRegFiles(22.U(16.W) + 0.U)
        ) + 12.U(16.W) + DP + 16.U(64.W) & 127.U(64.W).asUInt
        val __tmp_1395 = (105.U(8.W)).asUInt
        arrayRegFiles(__tmp_1394 + 0.U) := __tmp_1395(7, 0)

        val __tmp_1396 = Cat(
          arrayRegFiles(22.U(16.W) + 1.U),
          arrayRegFiles(22.U(16.W) + 0.U)
        ) + 12.U(16.W) + DP + 17.U(64.W) & 127.U(64.W).asUInt
        val __tmp_1397 = (108.U(8.W)).asUInt
        arrayRegFiles(__tmp_1396 + 0.U) := __tmp_1397(7, 0)

        val __tmp_1398 = Cat(
          arrayRegFiles(22.U(16.W) + 1.U),
          arrayRegFiles(22.U(16.W) + 0.U)
        ) + 12.U(16.W) + DP + 18.U(64.W) & 127.U(64.W).asUInt
        val __tmp_1399 = (46.U(8.W)).asUInt
        arrayRegFiles(__tmp_1398 + 0.U) := __tmp_1399(7, 0)

        val __tmp_1400 = Cat(
          arrayRegFiles(22.U(16.W) + 1.U),
          arrayRegFiles(22.U(16.W) + 0.U)
        ) + 12.U(16.W) + DP + 19.U(64.W) & 127.U(64.W).asUInt
        val __tmp_1401 = (80.U(8.W)).asUInt
        arrayRegFiles(__tmp_1400 + 0.U) := __tmp_1401(7, 0)

        val __tmp_1402 = Cat(
          arrayRegFiles(22.U(16.W) + 1.U),
          arrayRegFiles(22.U(16.W) + 0.U)
        ) + 12.U(16.W) + DP + 20.U(64.W) & 127.U(64.W).asUInt
        val __tmp_1403 = (114.U(8.W)).asUInt
        arrayRegFiles(__tmp_1402 + 0.U) := __tmp_1403(7, 0)

        val __tmp_1404 = Cat(
          arrayRegFiles(22.U(16.W) + 1.U),
          arrayRegFiles(22.U(16.W) + 0.U)
        ) + 12.U(16.W) + DP + 21.U(64.W) & 127.U(64.W).asUInt
        val __tmp_1405 = (105.U(8.W)).asUInt
        arrayRegFiles(__tmp_1404 + 0.U) := __tmp_1405(7, 0)

        val __tmp_1406 = Cat(
          arrayRegFiles(22.U(16.W) + 1.U),
          arrayRegFiles(22.U(16.W) + 0.U)
        ) + 12.U(16.W) + DP + 22.U(64.W) & 127.U(64.W).asUInt
        val __tmp_1407 = (110.U(8.W)).asUInt
        arrayRegFiles(__tmp_1406 + 0.U) := __tmp_1407(7, 0)

        val __tmp_1408 = Cat(
          arrayRegFiles(22.U(16.W) + 1.U),
          arrayRegFiles(22.U(16.W) + 0.U)
        ) + 12.U(16.W) + DP + 23.U(64.W) & 127.U(64.W).asUInt
        val __tmp_1409 = (116.U(8.W)).asUInt
        arrayRegFiles(__tmp_1408 + 0.U) := __tmp_1409(7, 0)

        val __tmp_1410 = Cat(
          arrayRegFiles(22.U(16.W) + 1.U),
          arrayRegFiles(22.U(16.W) + 0.U)
        ) + 12.U(16.W) + DP + 24.U(64.W) & 127.U(64.W).asUInt
        val __tmp_1411 = (101.U(8.W)).asUInt
        arrayRegFiles(__tmp_1410 + 0.U) := __tmp_1411(7, 0)

        val __tmp_1412 = Cat(
          arrayRegFiles(22.U(16.W) + 1.U),
          arrayRegFiles(22.U(16.W) + 0.U)
        ) + 12.U(16.W) + DP + 25.U(64.W) & 127.U(64.W).asUInt
        val __tmp_1413 = (114.U(8.W)).asUInt
        arrayRegFiles(__tmp_1412 + 0.U) := __tmp_1413(7, 0)

        val __tmp_1414 = Cat(
          arrayRegFiles(22.U(16.W) + 1.U),
          arrayRegFiles(22.U(16.W) + 0.U)
        ) + 12.U(16.W) + DP + 26.U(64.W) & 127.U(64.W).asUInt
        val __tmp_1415 = (73.U(8.W)).asUInt
        arrayRegFiles(__tmp_1414 + 0.U) := __tmp_1415(7, 0)

        val __tmp_1416 = Cat(
          arrayRegFiles(22.U(16.W) + 1.U),
          arrayRegFiles(22.U(16.W) + 0.U)
        ) + 12.U(16.W) + DP + 27.U(64.W) & 127.U(64.W).asUInt
        val __tmp_1417 = (110.U(8.W)).asUInt
        arrayRegFiles(__tmp_1416 + 0.U) := __tmp_1417(7, 0)

        val __tmp_1418 = Cat(
          arrayRegFiles(22.U(16.W) + 1.U),
          arrayRegFiles(22.U(16.W) + 0.U)
        ) + 12.U(16.W) + DP + 28.U(64.W) & 127.U(64.W).asUInt
        val __tmp_1419 = (100.U(8.W)).asUInt
        arrayRegFiles(__tmp_1418 + 0.U) := __tmp_1419(7, 0)

        val __tmp_1420 = Cat(
          arrayRegFiles(22.U(16.W) + 1.U),
          arrayRegFiles(22.U(16.W) + 0.U)
        ) + 12.U(16.W) + DP + 29.U(64.W) & 127.U(64.W).asUInt
        val __tmp_1421 = (101.U(8.W)).asUInt
        arrayRegFiles(__tmp_1420 + 0.U) := __tmp_1421(7, 0)

        val __tmp_1422 = Cat(
          arrayRegFiles(22.U(16.W) + 1.U),
          arrayRegFiles(22.U(16.W) + 0.U)
        ) + 12.U(16.W) + DP + 30.U(64.W) & 127.U(64.W).asUInt
        val __tmp_1423 = (120.U(8.W)).asUInt
        arrayRegFiles(__tmp_1422 + 0.U) := __tmp_1423(7, 0)

        val __tmp_1424 = Cat(
          arrayRegFiles(22.U(16.W) + 1.U),
          arrayRegFiles(22.U(16.W) + 0.U)
        ) + 12.U(16.W) + DP + 31.U(64.W) & 127.U(64.W).asUInt
        val __tmp_1425 = (46.U(8.W)).asUInt
        arrayRegFiles(__tmp_1424 + 0.U) := __tmp_1425(7, 0)

        val __tmp_1426 = Cat(
          arrayRegFiles(22.U(16.W) + 1.U),
          arrayRegFiles(22.U(16.W) + 0.U)
        ) + 12.U(16.W) + DP + 32.U(64.W) & 127.U(64.W).asUInt
        val __tmp_1427 = (85.U(8.W)).asUInt
        arrayRegFiles(__tmp_1426 + 0.U) := __tmp_1427(7, 0)

        val __tmp_1428 = Cat(
          arrayRegFiles(22.U(16.W) + 1.U),
          arrayRegFiles(22.U(16.W) + 0.U)
        ) + 12.U(16.W) + DP + 33.U(64.W) & 127.U(64.W).asUInt
        val __tmp_1429 = (32.U(8.W)).asUInt
        arrayRegFiles(__tmp_1428 + 0.U) := __tmp_1429(7, 0)

        val __tmp_1430 = Cat(
          arrayRegFiles(22.U(16.W) + 1.U),
          arrayRegFiles(22.U(16.W) + 0.U)
        ) + 12.U(16.W) + DP + 34.U(64.W) & 127.U(64.W).asUInt
        val __tmp_1431 = (118.U(8.W)).asUInt
        arrayRegFiles(__tmp_1430 + 0.U) := __tmp_1431(7, 0)

        val __tmp_1432 = Cat(
          arrayRegFiles(22.U(16.W) + 1.U),
          arrayRegFiles(22.U(16.W) + 0.U)
        ) + 12.U(16.W) + DP + 35.U(64.W) & 127.U(64.W).asUInt
        val __tmp_1433 = (97.U(8.W)).asUInt
        arrayRegFiles(__tmp_1432 + 0.U) := __tmp_1433(7, 0)

        val __tmp_1434 = Cat(
          arrayRegFiles(22.U(16.W) + 1.U),
          arrayRegFiles(22.U(16.W) + 0.U)
        ) + 12.U(16.W) + DP + 36.U(64.W) & 127.U(64.W).asUInt
        val __tmp_1435 = (108.U(8.W)).asUInt
        arrayRegFiles(__tmp_1434 + 0.U) := __tmp_1435(7, 0)

        val __tmp_1436 = Cat(
          arrayRegFiles(22.U(16.W) + 1.U),
          arrayRegFiles(22.U(16.W) + 0.U)
        ) + 12.U(16.W) + DP + 37.U(64.W) & 127.U(64.W).asUInt
        val __tmp_1437 = (117.U(8.W)).asUInt
        arrayRegFiles(__tmp_1436 + 0.U) := __tmp_1437(7, 0)

        val __tmp_1438 = Cat(
          arrayRegFiles(22.U(16.W) + 1.U),
          arrayRegFiles(22.U(16.W) + 0.U)
        ) + 12.U(16.W) + DP + 38.U(64.W) & 127.U(64.W).asUInt
        val __tmp_1439 = (101.U(8.W)).asUInt
        arrayRegFiles(__tmp_1438 + 0.U) := __tmp_1439(7, 0)

        CP := 84.U
      }

      is(84.U) {
        /*
        DP = DP + 39
        goto .85
        */


        DP := DP + 39.U

        CP := 85.U
      }

      is(85.U) {
        /*
        *((*(22 [SP]) + (12 [SP])) + ((DP & (127 [DP])) as SP)) = (10 [U8]) [unsigned, U8, 1]  // $display((DP & (127 [DP]))) = (10 [U8])
        goto .86
        */


        val __tmp_1440 = Cat(
          arrayRegFiles(22.U(16.W) + 1.U),
          arrayRegFiles(22.U(16.W) + 0.U)
        ) + 12.U(16.W) + DP & 127.U(64.W).asUInt
        val __tmp_1441 = (10.U(8.W)).asUInt
        arrayRegFiles(__tmp_1440 + 0.U) := __tmp_1441(7, 0)

        CP := 86.U
      }

      is(86.U) {
        /*
        DP = DP + 1
        goto .87
        */


        DP := DP + 1.U

        CP := 87.U
      }

      is(87.U) {
        /*
        *(SP + (6 [SP])) = (618 [U32]) [signed, U32, 4]  // $sfLoc = (618 [U32])
        goto .88
        */


        val __tmp_1442 = SP + 6.U(16.W)
        val __tmp_1443 = (618.S(32.W)).asUInt
        arrayRegFiles(__tmp_1442 + 0.U) := __tmp_1443(7, 0)
        arrayRegFiles(__tmp_1442 + 1.U) := __tmp_1443(15, 8)
        arrayRegFiles(__tmp_1442 + 2.U) := __tmp_1443(23, 16)
        arrayRegFiles(__tmp_1442 + 3.U) := __tmp_1443(31, 24)

        CP := 88.U
      }

      is(88.U) {
        /*
        $0 = (*(SP + (67 [SP])) as anvil.PrinterIndex.U)
        alloc printStackTrace$res@[618,7].00AE355F: U64 [@111, 8]
        goto .89
        */


        generalRegFiles(0.U) := Cat(
          arrayRegFiles(SP + 67.U(16.W) + 1.U),
          arrayRegFiles(SP + 67.U(16.W) + 0.U)
        ).asUInt
        CP := 89.U
      }

      is(89.U) {
        /*
        SP = SP + 127
        goto .90
        */


        SP := SP + 127.U

        CP := 90.U
      }

      is(90.U) {
        /*
        decl $ret: CP [@0, 2], $res: SP [@2, 2], $sfCaller: SP [@4, 2], $sfLoc: U32 [@6, 4], $sfDesc: IS[56, U8] [@10, 68], $sfCurrentId: SP [@78, 2], buffer: SP [@80, 2], index: anvil.PrinterIndex.U [@82, 8], memory: SP [@90, 2], mask: anvil.PrinterIndex.U [@92, 8], spSize: anvil.PrinterIndex.U [@100, 8], typeShaSize: anvil.PrinterIndex.U [@108, 8], locSize: anvil.PrinterIndex.U [@116, 8], sizeSize: anvil.PrinterIndex.U [@124, 8], sfCallerOffset: anvil.PrinterIndex.U [@132, 8]
        *SP = (374 [CP]) [unsigned, CP, 2]  // $ret@0 = 2233
        *(SP + (2 [SP])) = (SP - (16 [SP])) [unsigned, SP, 2]  // $res@2 = -16
        *(SP + (4 [SP])) = (SP - (123 [SP])) [unsigned, SP, 2]  // $sfCaller@4 = -123
        *(SP + (78 [SP])) = (SP + (4 [SP])) [unsigned, SP, 2]  // $sfCurrentId@78 = 4
        *(SP + (80 [SP])) = *(22 [SP]) [unsigned, SP, 2]  // buffer = *(22 [SP])
        *(SP + (82 [SP])) = DP [unsigned, anvil.PrinterIndex.U, 8]  // index = DP
        *(SP + (90 [SP])) = (0 [SP]) [unsigned, SP, 2]  // memory = (0 [SP])
        *(SP + (92 [SP])) = (127 [DP]) [unsigned, anvil.PrinterIndex.U, 8]  // mask = (127 [DP])
        *(SP + (100 [SP])) = (2 [anvil.PrinterIndex.U]) [unsigned, anvil.PrinterIndex.U, 8]  // spSize = (2 [anvil.PrinterIndex.U])
        *(SP + (108 [SP])) = (4 [anvil.PrinterIndex.U]) [unsigned, anvil.PrinterIndex.U, 8]  // typeShaSize = (4 [anvil.PrinterIndex.U])
        *(SP + (116 [SP])) = (4 [anvil.PrinterIndex.U]) [unsigned, anvil.PrinterIndex.U, 8]  // locSize = (4 [anvil.PrinterIndex.U])
        *(SP + (124 [SP])) = (8 [anvil.PrinterIndex.U]) [unsigned, anvil.PrinterIndex.U, 8]  // sizeSize = (8 [anvil.PrinterIndex.U])
        *(SP + (132 [SP])) = $0 [unsigned, anvil.PrinterIndex.U, 8]  // sfCallerOffset = $0
        *(SP - (8 [SP])) = $0 [unsigned, U64, 8]  // save $0
        goto .27
        */


        val __tmp_1444 = SP
        val __tmp_1445 = (374.U(16.W)).asUInt
        arrayRegFiles(__tmp_1444 + 0.U) := __tmp_1445(7, 0)
        arrayRegFiles(__tmp_1444 + 1.U) := __tmp_1445(15, 8)

        val __tmp_1446 = SP + 2.U(16.W)
        val __tmp_1447 = (SP - 16.U(16.W)).asUInt
        arrayRegFiles(__tmp_1446 + 0.U) := __tmp_1447(7, 0)
        arrayRegFiles(__tmp_1446 + 1.U) := __tmp_1447(15, 8)

        val __tmp_1448 = SP + 4.U(16.W)
        val __tmp_1449 = (SP - 123.U(16.W)).asUInt
        arrayRegFiles(__tmp_1448 + 0.U) := __tmp_1449(7, 0)
        arrayRegFiles(__tmp_1448 + 1.U) := __tmp_1449(15, 8)

        val __tmp_1450 = SP + 78.U(16.W)
        val __tmp_1451 = (SP + 4.U(16.W)).asUInt
        arrayRegFiles(__tmp_1450 + 0.U) := __tmp_1451(7, 0)
        arrayRegFiles(__tmp_1450 + 1.U) := __tmp_1451(15, 8)

        val __tmp_1452 = SP + 80.U(16.W)
        val __tmp_1453 = (Cat(
          arrayRegFiles(22.U(16.W) + 1.U),
          arrayRegFiles(22.U(16.W) + 0.U)
        )).asUInt
        arrayRegFiles(__tmp_1452 + 0.U) := __tmp_1453(7, 0)
        arrayRegFiles(__tmp_1452 + 1.U) := __tmp_1453(15, 8)

        val __tmp_1454 = SP + 82.U(16.W)
        val __tmp_1455 = (DP).asUInt
        arrayRegFiles(__tmp_1454 + 0.U) := __tmp_1455(7, 0)
        arrayRegFiles(__tmp_1454 + 1.U) := __tmp_1455(15, 8)
        arrayRegFiles(__tmp_1454 + 2.U) := __tmp_1455(23, 16)
        arrayRegFiles(__tmp_1454 + 3.U) := __tmp_1455(31, 24)
        arrayRegFiles(__tmp_1454 + 4.U) := __tmp_1455(39, 32)
        arrayRegFiles(__tmp_1454 + 5.U) := __tmp_1455(47, 40)
        arrayRegFiles(__tmp_1454 + 6.U) := __tmp_1455(55, 48)
        arrayRegFiles(__tmp_1454 + 7.U) := __tmp_1455(63, 56)

        val __tmp_1456 = SP + 90.U(16.W)
        val __tmp_1457 = (0.U(16.W)).asUInt
        arrayRegFiles(__tmp_1456 + 0.U) := __tmp_1457(7, 0)
        arrayRegFiles(__tmp_1456 + 1.U) := __tmp_1457(15, 8)

        val __tmp_1458 = SP + 92.U(16.W)
        val __tmp_1459 = (127.U(64.W)).asUInt
        arrayRegFiles(__tmp_1458 + 0.U) := __tmp_1459(7, 0)
        arrayRegFiles(__tmp_1458 + 1.U) := __tmp_1459(15, 8)
        arrayRegFiles(__tmp_1458 + 2.U) := __tmp_1459(23, 16)
        arrayRegFiles(__tmp_1458 + 3.U) := __tmp_1459(31, 24)
        arrayRegFiles(__tmp_1458 + 4.U) := __tmp_1459(39, 32)
        arrayRegFiles(__tmp_1458 + 5.U) := __tmp_1459(47, 40)
        arrayRegFiles(__tmp_1458 + 6.U) := __tmp_1459(55, 48)
        arrayRegFiles(__tmp_1458 + 7.U) := __tmp_1459(63, 56)

        val __tmp_1460 = SP + 100.U(16.W)
        val __tmp_1461 = (2.U(64.W)).asUInt
        arrayRegFiles(__tmp_1460 + 0.U) := __tmp_1461(7, 0)
        arrayRegFiles(__tmp_1460 + 1.U) := __tmp_1461(15, 8)
        arrayRegFiles(__tmp_1460 + 2.U) := __tmp_1461(23, 16)
        arrayRegFiles(__tmp_1460 + 3.U) := __tmp_1461(31, 24)
        arrayRegFiles(__tmp_1460 + 4.U) := __tmp_1461(39, 32)
        arrayRegFiles(__tmp_1460 + 5.U) := __tmp_1461(47, 40)
        arrayRegFiles(__tmp_1460 + 6.U) := __tmp_1461(55, 48)
        arrayRegFiles(__tmp_1460 + 7.U) := __tmp_1461(63, 56)

        val __tmp_1462 = SP + 108.U(16.W)
        val __tmp_1463 = (4.U(64.W)).asUInt
        arrayRegFiles(__tmp_1462 + 0.U) := __tmp_1463(7, 0)
        arrayRegFiles(__tmp_1462 + 1.U) := __tmp_1463(15, 8)
        arrayRegFiles(__tmp_1462 + 2.U) := __tmp_1463(23, 16)
        arrayRegFiles(__tmp_1462 + 3.U) := __tmp_1463(31, 24)
        arrayRegFiles(__tmp_1462 + 4.U) := __tmp_1463(39, 32)
        arrayRegFiles(__tmp_1462 + 5.U) := __tmp_1463(47, 40)
        arrayRegFiles(__tmp_1462 + 6.U) := __tmp_1463(55, 48)
        arrayRegFiles(__tmp_1462 + 7.U) := __tmp_1463(63, 56)

        val __tmp_1464 = SP + 116.U(16.W)
        val __tmp_1465 = (4.U(64.W)).asUInt
        arrayRegFiles(__tmp_1464 + 0.U) := __tmp_1465(7, 0)
        arrayRegFiles(__tmp_1464 + 1.U) := __tmp_1465(15, 8)
        arrayRegFiles(__tmp_1464 + 2.U) := __tmp_1465(23, 16)
        arrayRegFiles(__tmp_1464 + 3.U) := __tmp_1465(31, 24)
        arrayRegFiles(__tmp_1464 + 4.U) := __tmp_1465(39, 32)
        arrayRegFiles(__tmp_1464 + 5.U) := __tmp_1465(47, 40)
        arrayRegFiles(__tmp_1464 + 6.U) := __tmp_1465(55, 48)
        arrayRegFiles(__tmp_1464 + 7.U) := __tmp_1465(63, 56)

        val __tmp_1466 = SP + 124.U(16.W)
        val __tmp_1467 = (8.U(64.W)).asUInt
        arrayRegFiles(__tmp_1466 + 0.U) := __tmp_1467(7, 0)
        arrayRegFiles(__tmp_1466 + 1.U) := __tmp_1467(15, 8)
        arrayRegFiles(__tmp_1466 + 2.U) := __tmp_1467(23, 16)
        arrayRegFiles(__tmp_1466 + 3.U) := __tmp_1467(31, 24)
        arrayRegFiles(__tmp_1466 + 4.U) := __tmp_1467(39, 32)
        arrayRegFiles(__tmp_1466 + 5.U) := __tmp_1467(47, 40)
        arrayRegFiles(__tmp_1466 + 6.U) := __tmp_1467(55, 48)
        arrayRegFiles(__tmp_1466 + 7.U) := __tmp_1467(63, 56)

        val __tmp_1468 = SP + 132.U(16.W)
        val __tmp_1469 = (generalRegFiles(0.U)).asUInt
        arrayRegFiles(__tmp_1468 + 0.U) := __tmp_1469(7, 0)
        arrayRegFiles(__tmp_1468 + 1.U) := __tmp_1469(15, 8)
        arrayRegFiles(__tmp_1468 + 2.U) := __tmp_1469(23, 16)
        arrayRegFiles(__tmp_1468 + 3.U) := __tmp_1469(31, 24)
        arrayRegFiles(__tmp_1468 + 4.U) := __tmp_1469(39, 32)
        arrayRegFiles(__tmp_1468 + 5.U) := __tmp_1469(47, 40)
        arrayRegFiles(__tmp_1468 + 6.U) := __tmp_1469(55, 48)
        arrayRegFiles(__tmp_1468 + 7.U) := __tmp_1469(63, 56)

        val __tmp_1470 = SP - 8.U(16.W)
        val __tmp_1471 = (generalRegFiles(0.U)).asUInt
        arrayRegFiles(__tmp_1470 + 0.U) := __tmp_1471(7, 0)
        arrayRegFiles(__tmp_1470 + 1.U) := __tmp_1471(15, 8)
        arrayRegFiles(__tmp_1470 + 2.U) := __tmp_1471(23, 16)
        arrayRegFiles(__tmp_1470 + 3.U) := __tmp_1471(31, 24)
        arrayRegFiles(__tmp_1470 + 4.U) := __tmp_1471(39, 32)
        arrayRegFiles(__tmp_1470 + 5.U) := __tmp_1471(47, 40)
        arrayRegFiles(__tmp_1470 + 6.U) := __tmp_1471(55, 48)
        arrayRegFiles(__tmp_1470 + 7.U) := __tmp_1471(63, 56)

        CP := 27.U
      }

      is(91.U) {
        /*
        *(SP + (6 [SP])) = (626 [U32]) [signed, U32, 4]  // $sfLoc = (626 [U32])
        goto .92
        */


        val __tmp_1472 = SP + 6.U(16.W)
        val __tmp_1473 = (626.S(32.W)).asUInt
        arrayRegFiles(__tmp_1472 + 0.U) := __tmp_1473(7, 0)
        arrayRegFiles(__tmp_1472 + 1.U) := __tmp_1473(15, 8)
        arrayRegFiles(__tmp_1472 + 2.U) := __tmp_1473(23, 16)
        arrayRegFiles(__tmp_1472 + 3.U) := __tmp_1473(31, 24)

        CP := 92.U
      }

      is(92.U) {
        /*
        $0 = *(SP + (87 [SP])) [unsigned, anvil.PrinterIndex.U, 8]  // $0 = r
        goto .93
        */


        val __tmp_1474 = (SP + 87.U(16.W)).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_1474 + 7.U),
          arrayRegFiles(__tmp_1474 + 6.U),
          arrayRegFiles(__tmp_1474 + 5.U),
          arrayRegFiles(__tmp_1474 + 4.U),
          arrayRegFiles(__tmp_1474 + 3.U),
          arrayRegFiles(__tmp_1474 + 2.U),
          arrayRegFiles(__tmp_1474 + 1.U),
          arrayRegFiles(__tmp_1474 + 0.U)
        ).asUInt

        CP := 93.U
      }

      is(93.U) {
        /*
        *(SP + (6 [SP])) = (618 [U32]) [signed, U32, 4]  // $sfLoc = (618 [U32])
        goto .94
        */


        val __tmp_1475 = SP + 6.U(16.W)
        val __tmp_1476 = (618.S(32.W)).asUInt
        arrayRegFiles(__tmp_1475 + 0.U) := __tmp_1476(7, 0)
        arrayRegFiles(__tmp_1475 + 1.U) := __tmp_1476(15, 8)
        arrayRegFiles(__tmp_1475 + 2.U) := __tmp_1476(23, 16)
        arrayRegFiles(__tmp_1475 + 3.U) := __tmp_1476(31, 24)

        CP := 94.U
      }

      is(94.U) {
        /*
        **(SP + (2 [SP])) = $0 [unsigned, anvil.PrinterIndex.U, 8]  // $res = $0
        goto $ret@0
        */


        val __tmp_1477 = Cat(
          arrayRegFiles(SP + 2.U(16.W) + 1.U),
          arrayRegFiles(SP + 2.U(16.W) + 0.U)
        )
        val __tmp_1478 = (generalRegFiles(0.U)).asUInt
        arrayRegFiles(__tmp_1477 + 0.U) := __tmp_1478(7, 0)
        arrayRegFiles(__tmp_1477 + 1.U) := __tmp_1478(15, 8)
        arrayRegFiles(__tmp_1477 + 2.U) := __tmp_1478(23, 16)
        arrayRegFiles(__tmp_1477 + 3.U) := __tmp_1478(31, 24)
        arrayRegFiles(__tmp_1477 + 4.U) := __tmp_1478(39, 32)
        arrayRegFiles(__tmp_1477 + 5.U) := __tmp_1478(47, 40)
        arrayRegFiles(__tmp_1477 + 6.U) := __tmp_1478(55, 48)
        arrayRegFiles(__tmp_1477 + 7.U) := __tmp_1478(63, 56)

        CP := Cat(
          arrayRegFiles(SP + 0.U + 1.U),
          arrayRegFiles(SP + 0.U + 0.U)
        )

      }

      is(95.U) {
        /*
        *(SP + (6 [SP])) = (668 [U32]) [signed, U32, 4]  // $sfLoc = (668 [U32])
        goto .96
        */


        val __tmp_1479 = SP + 6.U(16.W)
        val __tmp_1480 = (668.S(32.W)).asUInt
        arrayRegFiles(__tmp_1479 + 0.U) := __tmp_1480(7, 0)
        arrayRegFiles(__tmp_1479 + 1.U) := __tmp_1480(15, 8)
        arrayRegFiles(__tmp_1479 + 2.U) := __tmp_1480(23, 16)
        arrayRegFiles(__tmp_1479 + 3.U) := __tmp_1480(31, 24)

        CP := 96.U
      }

      is(96.U) {
        /*
        $0 = *(SP + (148 [SP])) [unsigned, U64, 8]  // $0 = r
        goto .97
        */


        val __tmp_1481 = (SP + 148.U(16.W)).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_1481 + 7.U),
          arrayRegFiles(__tmp_1481 + 6.U),
          arrayRegFiles(__tmp_1481 + 5.U),
          arrayRegFiles(__tmp_1481 + 4.U),
          arrayRegFiles(__tmp_1481 + 3.U),
          arrayRegFiles(__tmp_1481 + 2.U),
          arrayRegFiles(__tmp_1481 + 1.U),
          arrayRegFiles(__tmp_1481 + 0.U)
        ).asUInt

        CP := 97.U
      }

      is(97.U) {
        /*
        *(SP + (6 [SP])) = (638 [U32]) [signed, U32, 4]  // $sfLoc = (638 [U32])
        goto .98
        */


        val __tmp_1482 = SP + 6.U(16.W)
        val __tmp_1483 = (638.S(32.W)).asUInt
        arrayRegFiles(__tmp_1482 + 0.U) := __tmp_1483(7, 0)
        arrayRegFiles(__tmp_1482 + 1.U) := __tmp_1483(15, 8)
        arrayRegFiles(__tmp_1482 + 2.U) := __tmp_1483(23, 16)
        arrayRegFiles(__tmp_1482 + 3.U) := __tmp_1483(31, 24)

        CP := 98.U
      }

      is(98.U) {
        /*
        **(SP + (2 [SP])) = $0 [unsigned, U64, 8]  // $res = $0
        goto $ret@0
        */


        val __tmp_1484 = Cat(
          arrayRegFiles(SP + 2.U(16.W) + 1.U),
          arrayRegFiles(SP + 2.U(16.W) + 0.U)
        )
        val __tmp_1485 = (generalRegFiles(0.U)).asUInt
        arrayRegFiles(__tmp_1484 + 0.U) := __tmp_1485(7, 0)
        arrayRegFiles(__tmp_1484 + 1.U) := __tmp_1485(15, 8)
        arrayRegFiles(__tmp_1484 + 2.U) := __tmp_1485(23, 16)
        arrayRegFiles(__tmp_1484 + 3.U) := __tmp_1485(31, 24)
        arrayRegFiles(__tmp_1484 + 4.U) := __tmp_1485(39, 32)
        arrayRegFiles(__tmp_1484 + 5.U) := __tmp_1485(47, 40)
        arrayRegFiles(__tmp_1484 + 6.U) := __tmp_1485(55, 48)
        arrayRegFiles(__tmp_1484 + 7.U) := __tmp_1485(63, 56)

        CP := Cat(
          arrayRegFiles(SP + 0.U + 1.U),
          arrayRegFiles(SP + 0.U + 0.U)
        )

      }

      is(99.U) {
        /*
        *(SP + (4 [SP])) = (9 [U32]) [signed, U32, 4]  // $sfLoc = (9 [U32])
        goto $ret@0
        */


        val __tmp_1486 = SP + 4.U(16.W)
        val __tmp_1487 = (9.S(32.W)).asUInt
        arrayRegFiles(__tmp_1486 + 0.U) := __tmp_1487(7, 0)
        arrayRegFiles(__tmp_1486 + 1.U) := __tmp_1487(15, 8)
        arrayRegFiles(__tmp_1486 + 2.U) := __tmp_1487(23, 16)
        arrayRegFiles(__tmp_1486 + 3.U) := __tmp_1487(31, 24)

        CP := Cat(
          arrayRegFiles(SP + 0.U + 1.U),
          arrayRegFiles(SP + 0.U + 0.U)
        )

      }

      is(100.U) {
        /*
        $0 = *(SP - (88 [SP])) [unsigned, U64, 8]  // restore $0
        $1 = *(SP - (80 [SP])) [unsigned, U64, 8]  // restore $1
        $2 = *(SP - (72 [SP])) [unsigned, U64, 8]  // restore $2
        $3 = *(SP - (64 [SP])) [unsigned, U64, 8]  // restore $3
        $4 = *(SP - (56 [SP])) [unsigned, U64, 8]  // restore $4
        $5 = *(SP - (48 [SP])) [unsigned, U64, 8]  // restore $5
        $6 = *(SP - (40 [SP])) [unsigned, U64, 8]  // restore $6
        $7 = *(SP - (32 [SP])) [unsigned, U64, 8]  // restore $7
        $8 = *(SP - (24 [SP])) [unsigned, U64, 8]  // restore $8
        $9 = *(SP - (16 [SP])) [unsigned, U64, 8]  // restore $9
        $10 = *(SP - (8 [SP])) [unsigned, U64, 8]  // restore $10
        undecl $sfCurrentId: SP [@40, 2], $sfDesc: IS[20, U8] [@8, 32], $sfLoc: U32 [@4, 4], $sfCaller: SP [@2, 2], $ret: CP [@0, 2]
        goto .101
        */


        val __tmp_1488 = (SP - 88.U(16.W)).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_1488 + 7.U),
          arrayRegFiles(__tmp_1488 + 6.U),
          arrayRegFiles(__tmp_1488 + 5.U),
          arrayRegFiles(__tmp_1488 + 4.U),
          arrayRegFiles(__tmp_1488 + 3.U),
          arrayRegFiles(__tmp_1488 + 2.U),
          arrayRegFiles(__tmp_1488 + 1.U),
          arrayRegFiles(__tmp_1488 + 0.U)
        ).asUInt

        val __tmp_1489 = (SP - 80.U(16.W)).asUInt
        generalRegFiles(1.U) := Cat(
          arrayRegFiles(__tmp_1489 + 7.U),
          arrayRegFiles(__tmp_1489 + 6.U),
          arrayRegFiles(__tmp_1489 + 5.U),
          arrayRegFiles(__tmp_1489 + 4.U),
          arrayRegFiles(__tmp_1489 + 3.U),
          arrayRegFiles(__tmp_1489 + 2.U),
          arrayRegFiles(__tmp_1489 + 1.U),
          arrayRegFiles(__tmp_1489 + 0.U)
        ).asUInt

        val __tmp_1490 = (SP - 72.U(16.W)).asUInt
        generalRegFiles(2.U) := Cat(
          arrayRegFiles(__tmp_1490 + 7.U),
          arrayRegFiles(__tmp_1490 + 6.U),
          arrayRegFiles(__tmp_1490 + 5.U),
          arrayRegFiles(__tmp_1490 + 4.U),
          arrayRegFiles(__tmp_1490 + 3.U),
          arrayRegFiles(__tmp_1490 + 2.U),
          arrayRegFiles(__tmp_1490 + 1.U),
          arrayRegFiles(__tmp_1490 + 0.U)
        ).asUInt

        val __tmp_1491 = (SP - 64.U(16.W)).asUInt
        generalRegFiles(3.U) := Cat(
          arrayRegFiles(__tmp_1491 + 7.U),
          arrayRegFiles(__tmp_1491 + 6.U),
          arrayRegFiles(__tmp_1491 + 5.U),
          arrayRegFiles(__tmp_1491 + 4.U),
          arrayRegFiles(__tmp_1491 + 3.U),
          arrayRegFiles(__tmp_1491 + 2.U),
          arrayRegFiles(__tmp_1491 + 1.U),
          arrayRegFiles(__tmp_1491 + 0.U)
        ).asUInt

        val __tmp_1492 = (SP - 56.U(16.W)).asUInt
        generalRegFiles(4.U) := Cat(
          arrayRegFiles(__tmp_1492 + 7.U),
          arrayRegFiles(__tmp_1492 + 6.U),
          arrayRegFiles(__tmp_1492 + 5.U),
          arrayRegFiles(__tmp_1492 + 4.U),
          arrayRegFiles(__tmp_1492 + 3.U),
          arrayRegFiles(__tmp_1492 + 2.U),
          arrayRegFiles(__tmp_1492 + 1.U),
          arrayRegFiles(__tmp_1492 + 0.U)
        ).asUInt

        val __tmp_1493 = (SP - 48.U(16.W)).asUInt
        generalRegFiles(5.U) := Cat(
          arrayRegFiles(__tmp_1493 + 7.U),
          arrayRegFiles(__tmp_1493 + 6.U),
          arrayRegFiles(__tmp_1493 + 5.U),
          arrayRegFiles(__tmp_1493 + 4.U),
          arrayRegFiles(__tmp_1493 + 3.U),
          arrayRegFiles(__tmp_1493 + 2.U),
          arrayRegFiles(__tmp_1493 + 1.U),
          arrayRegFiles(__tmp_1493 + 0.U)
        ).asUInt

        val __tmp_1494 = (SP - 40.U(16.W)).asUInt
        generalRegFiles(6.U) := Cat(
          arrayRegFiles(__tmp_1494 + 7.U),
          arrayRegFiles(__tmp_1494 + 6.U),
          arrayRegFiles(__tmp_1494 + 5.U),
          arrayRegFiles(__tmp_1494 + 4.U),
          arrayRegFiles(__tmp_1494 + 3.U),
          arrayRegFiles(__tmp_1494 + 2.U),
          arrayRegFiles(__tmp_1494 + 1.U),
          arrayRegFiles(__tmp_1494 + 0.U)
        ).asUInt

        val __tmp_1495 = (SP - 32.U(16.W)).asUInt
        generalRegFiles(7.U) := Cat(
          arrayRegFiles(__tmp_1495 + 7.U),
          arrayRegFiles(__tmp_1495 + 6.U),
          arrayRegFiles(__tmp_1495 + 5.U),
          arrayRegFiles(__tmp_1495 + 4.U),
          arrayRegFiles(__tmp_1495 + 3.U),
          arrayRegFiles(__tmp_1495 + 2.U),
          arrayRegFiles(__tmp_1495 + 1.U),
          arrayRegFiles(__tmp_1495 + 0.U)
        ).asUInt

        val __tmp_1496 = (SP - 24.U(16.W)).asUInt
        generalRegFiles(8.U) := Cat(
          arrayRegFiles(__tmp_1496 + 7.U),
          arrayRegFiles(__tmp_1496 + 6.U),
          arrayRegFiles(__tmp_1496 + 5.U),
          arrayRegFiles(__tmp_1496 + 4.U),
          arrayRegFiles(__tmp_1496 + 3.U),
          arrayRegFiles(__tmp_1496 + 2.U),
          arrayRegFiles(__tmp_1496 + 1.U),
          arrayRegFiles(__tmp_1496 + 0.U)
        ).asUInt

        val __tmp_1497 = (SP - 16.U(16.W)).asUInt
        generalRegFiles(9.U) := Cat(
          arrayRegFiles(__tmp_1497 + 7.U),
          arrayRegFiles(__tmp_1497 + 6.U),
          arrayRegFiles(__tmp_1497 + 5.U),
          arrayRegFiles(__tmp_1497 + 4.U),
          arrayRegFiles(__tmp_1497 + 3.U),
          arrayRegFiles(__tmp_1497 + 2.U),
          arrayRegFiles(__tmp_1497 + 1.U),
          arrayRegFiles(__tmp_1497 + 0.U)
        ).asUInt

        val __tmp_1498 = (SP - 8.U(16.W)).asUInt
        generalRegFiles(10.U) := Cat(
          arrayRegFiles(__tmp_1498 + 7.U),
          arrayRegFiles(__tmp_1498 + 6.U),
          arrayRegFiles(__tmp_1498 + 5.U),
          arrayRegFiles(__tmp_1498 + 4.U),
          arrayRegFiles(__tmp_1498 + 3.U),
          arrayRegFiles(__tmp_1498 + 2.U),
          arrayRegFiles(__tmp_1498 + 1.U),
          arrayRegFiles(__tmp_1498 + 0.U)
        ).asUInt

        CP := 101.U
      }

      is(101.U) {
        /*
        SP = SP - 132
        goto .99
        */


        SP := SP - 132.U

        CP := 99.U
      }

      is(102.U) {
        /*
        $0 = *(SP - (88 [SP])) [unsigned, U64, 8]  // restore $0
        $1 = *(SP - (80 [SP])) [unsigned, U64, 8]  // restore $1
        $2 = *(SP - (72 [SP])) [unsigned, U64, 8]  // restore $2
        $3 = *(SP - (64 [SP])) [unsigned, U64, 8]  // restore $3
        $4 = *(SP - (56 [SP])) [unsigned, U64, 8]  // restore $4
        $5 = *(SP - (48 [SP])) [unsigned, U64, 8]  // restore $5
        $6 = *(SP - (40 [SP])) [unsigned, U64, 8]  // restore $6
        $7 = *(SP - (32 [SP])) [unsigned, U64, 8]  // restore $7
        $8 = *(SP - (24 [SP])) [unsigned, U64, 8]  // restore $8
        $9 = *(SP - (16 [SP])) [unsigned, U64, 8]  // restore $9
        $10 = *(SP - (8 [SP])) [unsigned, U64, 8]  // restore $10
        undecl y: Z [@50, 8], x: Z [@42, 8], $sfCurrentId: SP [@40, 2], $sfDesc: IS[20, U8] [@8, 32], $sfLoc: U32 [@4, 4], $sfCaller: SP [@2, 2], $ret: CP [@0, 2]
        goto .103
        */


        val __tmp_1499 = (SP - 88.U(16.W)).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_1499 + 7.U),
          arrayRegFiles(__tmp_1499 + 6.U),
          arrayRegFiles(__tmp_1499 + 5.U),
          arrayRegFiles(__tmp_1499 + 4.U),
          arrayRegFiles(__tmp_1499 + 3.U),
          arrayRegFiles(__tmp_1499 + 2.U),
          arrayRegFiles(__tmp_1499 + 1.U),
          arrayRegFiles(__tmp_1499 + 0.U)
        ).asUInt

        val __tmp_1500 = (SP - 80.U(16.W)).asUInt
        generalRegFiles(1.U) := Cat(
          arrayRegFiles(__tmp_1500 + 7.U),
          arrayRegFiles(__tmp_1500 + 6.U),
          arrayRegFiles(__tmp_1500 + 5.U),
          arrayRegFiles(__tmp_1500 + 4.U),
          arrayRegFiles(__tmp_1500 + 3.U),
          arrayRegFiles(__tmp_1500 + 2.U),
          arrayRegFiles(__tmp_1500 + 1.U),
          arrayRegFiles(__tmp_1500 + 0.U)
        ).asUInt

        val __tmp_1501 = (SP - 72.U(16.W)).asUInt
        generalRegFiles(2.U) := Cat(
          arrayRegFiles(__tmp_1501 + 7.U),
          arrayRegFiles(__tmp_1501 + 6.U),
          arrayRegFiles(__tmp_1501 + 5.U),
          arrayRegFiles(__tmp_1501 + 4.U),
          arrayRegFiles(__tmp_1501 + 3.U),
          arrayRegFiles(__tmp_1501 + 2.U),
          arrayRegFiles(__tmp_1501 + 1.U),
          arrayRegFiles(__tmp_1501 + 0.U)
        ).asUInt

        val __tmp_1502 = (SP - 64.U(16.W)).asUInt
        generalRegFiles(3.U) := Cat(
          arrayRegFiles(__tmp_1502 + 7.U),
          arrayRegFiles(__tmp_1502 + 6.U),
          arrayRegFiles(__tmp_1502 + 5.U),
          arrayRegFiles(__tmp_1502 + 4.U),
          arrayRegFiles(__tmp_1502 + 3.U),
          arrayRegFiles(__tmp_1502 + 2.U),
          arrayRegFiles(__tmp_1502 + 1.U),
          arrayRegFiles(__tmp_1502 + 0.U)
        ).asUInt

        val __tmp_1503 = (SP - 56.U(16.W)).asUInt
        generalRegFiles(4.U) := Cat(
          arrayRegFiles(__tmp_1503 + 7.U),
          arrayRegFiles(__tmp_1503 + 6.U),
          arrayRegFiles(__tmp_1503 + 5.U),
          arrayRegFiles(__tmp_1503 + 4.U),
          arrayRegFiles(__tmp_1503 + 3.U),
          arrayRegFiles(__tmp_1503 + 2.U),
          arrayRegFiles(__tmp_1503 + 1.U),
          arrayRegFiles(__tmp_1503 + 0.U)
        ).asUInt

        val __tmp_1504 = (SP - 48.U(16.W)).asUInt
        generalRegFiles(5.U) := Cat(
          arrayRegFiles(__tmp_1504 + 7.U),
          arrayRegFiles(__tmp_1504 + 6.U),
          arrayRegFiles(__tmp_1504 + 5.U),
          arrayRegFiles(__tmp_1504 + 4.U),
          arrayRegFiles(__tmp_1504 + 3.U),
          arrayRegFiles(__tmp_1504 + 2.U),
          arrayRegFiles(__tmp_1504 + 1.U),
          arrayRegFiles(__tmp_1504 + 0.U)
        ).asUInt

        val __tmp_1505 = (SP - 40.U(16.W)).asUInt
        generalRegFiles(6.U) := Cat(
          arrayRegFiles(__tmp_1505 + 7.U),
          arrayRegFiles(__tmp_1505 + 6.U),
          arrayRegFiles(__tmp_1505 + 5.U),
          arrayRegFiles(__tmp_1505 + 4.U),
          arrayRegFiles(__tmp_1505 + 3.U),
          arrayRegFiles(__tmp_1505 + 2.U),
          arrayRegFiles(__tmp_1505 + 1.U),
          arrayRegFiles(__tmp_1505 + 0.U)
        ).asUInt

        val __tmp_1506 = (SP - 32.U(16.W)).asUInt
        generalRegFiles(7.U) := Cat(
          arrayRegFiles(__tmp_1506 + 7.U),
          arrayRegFiles(__tmp_1506 + 6.U),
          arrayRegFiles(__tmp_1506 + 5.U),
          arrayRegFiles(__tmp_1506 + 4.U),
          arrayRegFiles(__tmp_1506 + 3.U),
          arrayRegFiles(__tmp_1506 + 2.U),
          arrayRegFiles(__tmp_1506 + 1.U),
          arrayRegFiles(__tmp_1506 + 0.U)
        ).asUInt

        val __tmp_1507 = (SP - 24.U(16.W)).asUInt
        generalRegFiles(8.U) := Cat(
          arrayRegFiles(__tmp_1507 + 7.U),
          arrayRegFiles(__tmp_1507 + 6.U),
          arrayRegFiles(__tmp_1507 + 5.U),
          arrayRegFiles(__tmp_1507 + 4.U),
          arrayRegFiles(__tmp_1507 + 3.U),
          arrayRegFiles(__tmp_1507 + 2.U),
          arrayRegFiles(__tmp_1507 + 1.U),
          arrayRegFiles(__tmp_1507 + 0.U)
        ).asUInt

        val __tmp_1508 = (SP - 16.U(16.W)).asUInt
        generalRegFiles(9.U) := Cat(
          arrayRegFiles(__tmp_1508 + 7.U),
          arrayRegFiles(__tmp_1508 + 6.U),
          arrayRegFiles(__tmp_1508 + 5.U),
          arrayRegFiles(__tmp_1508 + 4.U),
          arrayRegFiles(__tmp_1508 + 3.U),
          arrayRegFiles(__tmp_1508 + 2.U),
          arrayRegFiles(__tmp_1508 + 1.U),
          arrayRegFiles(__tmp_1508 + 0.U)
        ).asUInt

        val __tmp_1509 = (SP - 8.U(16.W)).asUInt
        generalRegFiles(10.U) := Cat(
          arrayRegFiles(__tmp_1509 + 7.U),
          arrayRegFiles(__tmp_1509 + 6.U),
          arrayRegFiles(__tmp_1509 + 5.U),
          arrayRegFiles(__tmp_1509 + 4.U),
          arrayRegFiles(__tmp_1509 + 3.U),
          arrayRegFiles(__tmp_1509 + 2.U),
          arrayRegFiles(__tmp_1509 + 1.U),
          arrayRegFiles(__tmp_1509 + 0.U)
        ).asUInt

        CP := 103.U
      }

      is(103.U) {
        /*
        SP = SP - 130
        goto .104
        */


        SP := SP - 130.U

        CP := 104.U
      }

      is(104.U) {
        /*
        *(SP + (4 [SP])) = (9 [U32]) [signed, U32, 4]  // $sfLoc = (9 [U32])
        goto $ret@0
        */


        val __tmp_1510 = SP + 4.U(16.W)
        val __tmp_1511 = (9.S(32.W)).asUInt
        arrayRegFiles(__tmp_1510 + 0.U) := __tmp_1511(7, 0)
        arrayRegFiles(__tmp_1510 + 1.U) := __tmp_1511(15, 8)
        arrayRegFiles(__tmp_1510 + 2.U) := __tmp_1511(23, 16)
        arrayRegFiles(__tmp_1510 + 3.U) := __tmp_1511(31, 24)

        CP := Cat(
          arrayRegFiles(SP + 0.U + 1.U),
          arrayRegFiles(SP + 0.U + 0.U)
        )

      }

      is(105.U) {
        /*
        $0 = *(SP - (8 [SP])) [unsigned, U64, 8]  // restore $0
        $1 = **(SP + (2 [SP])) [unsigned, U64, 8]  // $1 = $ret
        undecl sfCallerOffset: anvil.PrinterIndex.U [@132, 8], sizeSize: anvil.PrinterIndex.U [@124, 8], locSize: anvil.PrinterIndex.U [@116, 8], typeShaSize: anvil.PrinterIndex.U [@108, 8], spSize: anvil.PrinterIndex.U [@100, 8], mask: anvil.PrinterIndex.U [@92, 8], memory: SP [@90, 2], index: anvil.PrinterIndex.U [@82, 8], buffer: SP [@80, 2], $sfCurrentId: SP [@78, 2], $sfDesc: IS[56, U8] [@10, 68], $sfLoc: U32 [@6, 4], $sfCaller: SP [@4, 2], $res: SP [@2, 2], $ret: CP [@0, 2]
        goto .106
        */


        val __tmp_1512 = (SP - 8.U(16.W)).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_1512 + 7.U),
          arrayRegFiles(__tmp_1512 + 6.U),
          arrayRegFiles(__tmp_1512 + 5.U),
          arrayRegFiles(__tmp_1512 + 4.U),
          arrayRegFiles(__tmp_1512 + 3.U),
          arrayRegFiles(__tmp_1512 + 2.U),
          arrayRegFiles(__tmp_1512 + 1.U),
          arrayRegFiles(__tmp_1512 + 0.U)
        ).asUInt

        val __tmp_1513 = (Cat(
          arrayRegFiles(SP + 2.U(16.W) + 1.U),
          arrayRegFiles(SP + 2.U(16.W) + 0.U)
        )).asUInt
        generalRegFiles(1.U) := Cat(
          arrayRegFiles(__tmp_1513 + 7.U),
          arrayRegFiles(__tmp_1513 + 6.U),
          arrayRegFiles(__tmp_1513 + 5.U),
          arrayRegFiles(__tmp_1513 + 4.U),
          arrayRegFiles(__tmp_1513 + 3.U),
          arrayRegFiles(__tmp_1513 + 2.U),
          arrayRegFiles(__tmp_1513 + 1.U),
          arrayRegFiles(__tmp_1513 + 0.U)
        ).asUInt

        CP := 106.U
      }

      is(106.U) {
        /*
        SP = SP - 74
        goto .107
        */


        SP := SP - 74.U

        CP := 107.U
      }

      is(107.U) {
        /*
        DP = DP + ($1 as DP)
        goto .108
        */


        DP := generalRegFiles(1.U).asUInt

        CP := 108.U
      }

      is(108.U) {
        /*
        unalloc printStackTrace$res@[5,16].F35C27EB: U64 [@58, 8]
        goto .1
        */


        CP := 1.U
      }

      is(109.U) {
        /*
        $0 = *(SP - (24 [SP])) [unsigned, U64, 8]  // restore $0
        $1 = *(SP - (16 [SP])) [unsigned, U64, 8]  // restore $1
        $2 = *(SP - (8 [SP])) [unsigned, U64, 8]  // restore $2
        $3 = **(SP + (2 [SP])) [unsigned, anvil.PrinterIndex.U, 8]  // $3 = $ret
        undecl size: anvil.PrinterIndex.U [@79, 8], offset: anvil.PrinterIndex.U [@71, 8], memory: SP [@69, 2], $sfCurrentId: SP [@67, 2], $sfDesc: IS[45, U8] [@10, 57], $sfLoc: U32 [@6, 4], $sfCaller: SP [@4, 2], $res: SP [@2, 2], $ret: CP [@0, 2]
        goto .110
        */


        val __tmp_1514 = (SP - 24.U(16.W)).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_1514 + 7.U),
          arrayRegFiles(__tmp_1514 + 6.U),
          arrayRegFiles(__tmp_1514 + 5.U),
          arrayRegFiles(__tmp_1514 + 4.U),
          arrayRegFiles(__tmp_1514 + 3.U),
          arrayRegFiles(__tmp_1514 + 2.U),
          arrayRegFiles(__tmp_1514 + 1.U),
          arrayRegFiles(__tmp_1514 + 0.U)
        ).asUInt

        val __tmp_1515 = (SP - 16.U(16.W)).asUInt
        generalRegFiles(1.U) := Cat(
          arrayRegFiles(__tmp_1515 + 7.U),
          arrayRegFiles(__tmp_1515 + 6.U),
          arrayRegFiles(__tmp_1515 + 5.U),
          arrayRegFiles(__tmp_1515 + 4.U),
          arrayRegFiles(__tmp_1515 + 3.U),
          arrayRegFiles(__tmp_1515 + 2.U),
          arrayRegFiles(__tmp_1515 + 1.U),
          arrayRegFiles(__tmp_1515 + 0.U)
        ).asUInt

        val __tmp_1516 = (SP - 8.U(16.W)).asUInt
        generalRegFiles(2.U) := Cat(
          arrayRegFiles(__tmp_1516 + 7.U),
          arrayRegFiles(__tmp_1516 + 6.U),
          arrayRegFiles(__tmp_1516 + 5.U),
          arrayRegFiles(__tmp_1516 + 4.U),
          arrayRegFiles(__tmp_1516 + 3.U),
          arrayRegFiles(__tmp_1516 + 2.U),
          arrayRegFiles(__tmp_1516 + 1.U),
          arrayRegFiles(__tmp_1516 + 0.U)
        ).asUInt

        val __tmp_1517 = (Cat(
          arrayRegFiles(SP + 2.U(16.W) + 1.U),
          arrayRegFiles(SP + 2.U(16.W) + 0.U)
        )).asUInt
        generalRegFiles(3.U) := Cat(
          arrayRegFiles(__tmp_1517 + 7.U),
          arrayRegFiles(__tmp_1517 + 6.U),
          arrayRegFiles(__tmp_1517 + 5.U),
          arrayRegFiles(__tmp_1517 + 4.U),
          arrayRegFiles(__tmp_1517 + 3.U),
          arrayRegFiles(__tmp_1517 + 2.U),
          arrayRegFiles(__tmp_1517 + 1.U),
          arrayRegFiles(__tmp_1517 + 0.U)
        ).asUInt

        CP := 110.U
      }

      is(110.U) {
        /*
        SP = SP - 244
        goto .111
        */


        SP := SP - 244.U

        CP := 111.U
      }

      is(111.U) {
        /*
        $4 = ($3 as Z)
        goto .112
        */


        generalRegFiles(4.U) := (generalRegFiles(3.U).asSInt).asUInt
        CP := 112.U
      }

      is(112.U) {
        /*
        $5 = ($4 as U64)
        goto .113
        */


        generalRegFiles(5.U) := generalRegFiles(4.U).asSInt.asUInt
        CP := 113.U
      }

      is(113.U) {
        /*
        *(SP + (172 [SP])) = $5 [signed, U64, 8]  // sfLoc = $5
        goto .114
        */


        val __tmp_1518 = SP + 172.U(16.W)
        val __tmp_1519 = (generalRegFiles(5.U)).asUInt
        arrayRegFiles(__tmp_1518 + 0.U) := __tmp_1519(7, 0)
        arrayRegFiles(__tmp_1518 + 1.U) := __tmp_1519(15, 8)
        arrayRegFiles(__tmp_1518 + 2.U) := __tmp_1519(23, 16)
        arrayRegFiles(__tmp_1518 + 3.U) := __tmp_1519(31, 24)
        arrayRegFiles(__tmp_1518 + 4.U) := __tmp_1519(39, 32)
        arrayRegFiles(__tmp_1518 + 5.U) := __tmp_1519(47, 40)
        arrayRegFiles(__tmp_1518 + 6.U) := __tmp_1519(55, 48)
        arrayRegFiles(__tmp_1518 + 7.U) := __tmp_1519(63, 56)

        CP := 114.U
      }

      is(114.U) {
        /*
        *(SP + (6 [SP])) = (645 [U32]) [signed, U32, 4]  // $sfLoc = (645 [U32])
        goto .115
        */


        val __tmp_1520 = SP + 6.U(16.W)
        val __tmp_1521 = (645.S(32.W)).asUInt
        arrayRegFiles(__tmp_1520 + 0.U) := __tmp_1521(7, 0)
        arrayRegFiles(__tmp_1520 + 1.U) := __tmp_1521(15, 8)
        arrayRegFiles(__tmp_1520 + 2.U) := __tmp_1521(23, 16)
        arrayRegFiles(__tmp_1520 + 3.U) := __tmp_1521(31, 24)

        CP := 115.U
      }

      is(115.U) {
        /*
        $0 = *(SP + (164 [SP])) [unsigned, anvil.PrinterIndex.U, 8]  // $0 = offset
        $1 = *(SP + (116 [SP])) [unsigned, anvil.PrinterIndex.U, 8]  // $1 = locSize
        goto .116
        */


        val __tmp_1522 = (SP + 164.U(16.W)).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_1522 + 7.U),
          arrayRegFiles(__tmp_1522 + 6.U),
          arrayRegFiles(__tmp_1522 + 5.U),
          arrayRegFiles(__tmp_1522 + 4.U),
          arrayRegFiles(__tmp_1522 + 3.U),
          arrayRegFiles(__tmp_1522 + 2.U),
          arrayRegFiles(__tmp_1522 + 1.U),
          arrayRegFiles(__tmp_1522 + 0.U)
        ).asUInt

        val __tmp_1523 = (SP + 116.U(16.W)).asUInt
        generalRegFiles(1.U) := Cat(
          arrayRegFiles(__tmp_1523 + 7.U),
          arrayRegFiles(__tmp_1523 + 6.U),
          arrayRegFiles(__tmp_1523 + 5.U),
          arrayRegFiles(__tmp_1523 + 4.U),
          arrayRegFiles(__tmp_1523 + 3.U),
          arrayRegFiles(__tmp_1523 + 2.U),
          arrayRegFiles(__tmp_1523 + 1.U),
          arrayRegFiles(__tmp_1523 + 0.U)
        ).asUInt

        CP := 116.U
      }

      is(116.U) {
        /*
        $2 = ($0 + $1)
        goto .117
        */


        generalRegFiles(2.U) := generalRegFiles(0.U) + generalRegFiles(1.U)
        CP := 117.U
      }

      is(117.U) {
        /*
        $3 = *(SP + (108 [SP])) [unsigned, anvil.PrinterIndex.U, 8]  // $3 = typeShaSize
        goto .118
        */


        val __tmp_1524 = (SP + 108.U(16.W)).asUInt
        generalRegFiles(3.U) := Cat(
          arrayRegFiles(__tmp_1524 + 7.U),
          arrayRegFiles(__tmp_1524 + 6.U),
          arrayRegFiles(__tmp_1524 + 5.U),
          arrayRegFiles(__tmp_1524 + 4.U),
          arrayRegFiles(__tmp_1524 + 3.U),
          arrayRegFiles(__tmp_1524 + 2.U),
          arrayRegFiles(__tmp_1524 + 1.U),
          arrayRegFiles(__tmp_1524 + 0.U)
        ).asUInt

        CP := 118.U
      }

      is(118.U) {
        /*
        $4 = ($2 + $3)
        goto .119
        */


        generalRegFiles(4.U) := generalRegFiles(2.U) + generalRegFiles(3.U)
        CP := 119.U
      }

      is(119.U) {
        /*
        *(SP + (164 [SP])) = $4 [signed, anvil.PrinterIndex.U, 8]  // offset = $4
        goto .120
        */


        val __tmp_1525 = SP + 164.U(16.W)
        val __tmp_1526 = (generalRegFiles(4.U)).asUInt
        arrayRegFiles(__tmp_1525 + 0.U) := __tmp_1526(7, 0)
        arrayRegFiles(__tmp_1525 + 1.U) := __tmp_1526(15, 8)
        arrayRegFiles(__tmp_1525 + 2.U) := __tmp_1526(23, 16)
        arrayRegFiles(__tmp_1525 + 3.U) := __tmp_1526(31, 24)
        arrayRegFiles(__tmp_1525 + 4.U) := __tmp_1526(39, 32)
        arrayRegFiles(__tmp_1525 + 5.U) := __tmp_1526(47, 40)
        arrayRegFiles(__tmp_1525 + 6.U) := __tmp_1526(55, 48)
        arrayRegFiles(__tmp_1525 + 7.U) := __tmp_1526(63, 56)

        CP := 120.U
      }

      is(120.U) {
        /*
        *(SP + (6 [SP])) = (646 [U32]) [signed, U32, 4]  // $sfLoc = (646 [U32])
        goto .121
        */


        val __tmp_1527 = SP + 6.U(16.W)
        val __tmp_1528 = (646.S(32.W)).asUInt
        arrayRegFiles(__tmp_1527 + 0.U) := __tmp_1528(7, 0)
        arrayRegFiles(__tmp_1527 + 1.U) := __tmp_1528(15, 8)
        arrayRegFiles(__tmp_1527 + 2.U) := __tmp_1528(23, 16)
        arrayRegFiles(__tmp_1527 + 3.U) := __tmp_1528(31, 24)

        CP := 121.U
      }

      is(121.U) {
        /*
        decl sfDescSize: anvil.PrinterIndex.U [@188, 8]
        $0 = *(SP + (90 [SP])) [unsigned, MS[anvil.PrinterIndex.U, U8], 2]  // $0 = memory
        $1 = *(SP + (164 [SP])) [unsigned, anvil.PrinterIndex.U, 8]  // $1 = offset
        $2 = *(SP + (124 [SP])) [unsigned, anvil.PrinterIndex.U, 8]  // $2 = sizeSize
        alloc load$res@[646,24].7AFE7F1A: anvil.PrinterIndex.U [@196, 8]
        goto .122
        */


        val __tmp_1529 = (SP + 90.U(16.W)).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_1529 + 1.U),
          arrayRegFiles(__tmp_1529 + 0.U)
        ).asUInt

        val __tmp_1530 = (SP + 164.U(16.W)).asUInt
        generalRegFiles(1.U) := Cat(
          arrayRegFiles(__tmp_1530 + 7.U),
          arrayRegFiles(__tmp_1530 + 6.U),
          arrayRegFiles(__tmp_1530 + 5.U),
          arrayRegFiles(__tmp_1530 + 4.U),
          arrayRegFiles(__tmp_1530 + 3.U),
          arrayRegFiles(__tmp_1530 + 2.U),
          arrayRegFiles(__tmp_1530 + 1.U),
          arrayRegFiles(__tmp_1530 + 0.U)
        ).asUInt

        val __tmp_1531 = (SP + 124.U(16.W)).asUInt
        generalRegFiles(2.U) := Cat(
          arrayRegFiles(__tmp_1531 + 7.U),
          arrayRegFiles(__tmp_1531 + 6.U),
          arrayRegFiles(__tmp_1531 + 5.U),
          arrayRegFiles(__tmp_1531 + 4.U),
          arrayRegFiles(__tmp_1531 + 3.U),
          arrayRegFiles(__tmp_1531 + 2.U),
          arrayRegFiles(__tmp_1531 + 1.U),
          arrayRegFiles(__tmp_1531 + 0.U)
        ).asUInt

        CP := 122.U
      }

      is(122.U) {
        /*
        SP = SP + 244
        goto .123
        */


        SP := SP + 244.U

        CP := 123.U
      }

      is(123.U) {
        /*
        decl $ret: CP [@0, 2], $res: SP [@2, 2], $sfCaller: SP [@4, 2], $sfLoc: U32 [@6, 4], $sfDesc: IS[45, U8] [@10, 57], $sfCurrentId: SP [@67, 2], memory: SP [@69, 2], offset: anvil.PrinterIndex.U [@71, 8], size: anvil.PrinterIndex.U [@79, 8]
        *SP = (124 [CP]) [unsigned, CP, 2]  // $ret@0 = 2227
        *(SP + (2 [SP])) = (SP - (48 [SP])) [unsigned, SP, 2]  // $res@2 = -48
        *(SP + (4 [SP])) = (SP - (240 [SP])) [unsigned, SP, 2]  // $sfCaller@4 = -240
        *(SP + (67 [SP])) = (SP + (4 [SP])) [unsigned, SP, 2]  // $sfCurrentId@67 = 4
        *(SP + (69 [SP])) = $0 [unsigned, SP, 2]  // memory = $0
        *(SP + (71 [SP])) = $1 [unsigned, anvil.PrinterIndex.U, 8]  // offset = $1
        *(SP + (79 [SP])) = $2 [unsigned, anvil.PrinterIndex.U, 8]  // size = $2
        *(SP - (24 [SP])) = $0 [unsigned, U64, 8]  // save $0
        *(SP - (16 [SP])) = $1 [unsigned, U64, 8]  // save $1
        *(SP - (8 [SP])) = $2 [unsigned, U64, 8]  // save $2
        goto .52
        */


        val __tmp_1532 = SP
        val __tmp_1533 = (124.U(16.W)).asUInt
        arrayRegFiles(__tmp_1532 + 0.U) := __tmp_1533(7, 0)
        arrayRegFiles(__tmp_1532 + 1.U) := __tmp_1533(15, 8)

        val __tmp_1534 = SP + 2.U(16.W)
        val __tmp_1535 = (SP - 48.U(16.W)).asUInt
        arrayRegFiles(__tmp_1534 + 0.U) := __tmp_1535(7, 0)
        arrayRegFiles(__tmp_1534 + 1.U) := __tmp_1535(15, 8)

        val __tmp_1536 = SP + 4.U(16.W)
        val __tmp_1537 = (SP - 240.U(16.W)).asUInt
        arrayRegFiles(__tmp_1536 + 0.U) := __tmp_1537(7, 0)
        arrayRegFiles(__tmp_1536 + 1.U) := __tmp_1537(15, 8)

        val __tmp_1538 = SP + 67.U(16.W)
        val __tmp_1539 = (SP + 4.U(16.W)).asUInt
        arrayRegFiles(__tmp_1538 + 0.U) := __tmp_1539(7, 0)
        arrayRegFiles(__tmp_1538 + 1.U) := __tmp_1539(15, 8)

        val __tmp_1540 = SP + 69.U(16.W)
        val __tmp_1541 = (generalRegFiles(0.U)).asUInt
        arrayRegFiles(__tmp_1540 + 0.U) := __tmp_1541(7, 0)
        arrayRegFiles(__tmp_1540 + 1.U) := __tmp_1541(15, 8)

        val __tmp_1542 = SP + 71.U(16.W)
        val __tmp_1543 = (generalRegFiles(1.U)).asUInt
        arrayRegFiles(__tmp_1542 + 0.U) := __tmp_1543(7, 0)
        arrayRegFiles(__tmp_1542 + 1.U) := __tmp_1543(15, 8)
        arrayRegFiles(__tmp_1542 + 2.U) := __tmp_1543(23, 16)
        arrayRegFiles(__tmp_1542 + 3.U) := __tmp_1543(31, 24)
        arrayRegFiles(__tmp_1542 + 4.U) := __tmp_1543(39, 32)
        arrayRegFiles(__tmp_1542 + 5.U) := __tmp_1543(47, 40)
        arrayRegFiles(__tmp_1542 + 6.U) := __tmp_1543(55, 48)
        arrayRegFiles(__tmp_1542 + 7.U) := __tmp_1543(63, 56)

        val __tmp_1544 = SP + 79.U(16.W)
        val __tmp_1545 = (generalRegFiles(2.U)).asUInt
        arrayRegFiles(__tmp_1544 + 0.U) := __tmp_1545(7, 0)
        arrayRegFiles(__tmp_1544 + 1.U) := __tmp_1545(15, 8)
        arrayRegFiles(__tmp_1544 + 2.U) := __tmp_1545(23, 16)
        arrayRegFiles(__tmp_1544 + 3.U) := __tmp_1545(31, 24)
        arrayRegFiles(__tmp_1544 + 4.U) := __tmp_1545(39, 32)
        arrayRegFiles(__tmp_1544 + 5.U) := __tmp_1545(47, 40)
        arrayRegFiles(__tmp_1544 + 6.U) := __tmp_1545(55, 48)
        arrayRegFiles(__tmp_1544 + 7.U) := __tmp_1545(63, 56)

        val __tmp_1546 = SP - 24.U(16.W)
        val __tmp_1547 = (generalRegFiles(0.U)).asUInt
        arrayRegFiles(__tmp_1546 + 0.U) := __tmp_1547(7, 0)
        arrayRegFiles(__tmp_1546 + 1.U) := __tmp_1547(15, 8)
        arrayRegFiles(__tmp_1546 + 2.U) := __tmp_1547(23, 16)
        arrayRegFiles(__tmp_1546 + 3.U) := __tmp_1547(31, 24)
        arrayRegFiles(__tmp_1546 + 4.U) := __tmp_1547(39, 32)
        arrayRegFiles(__tmp_1546 + 5.U) := __tmp_1547(47, 40)
        arrayRegFiles(__tmp_1546 + 6.U) := __tmp_1547(55, 48)
        arrayRegFiles(__tmp_1546 + 7.U) := __tmp_1547(63, 56)

        val __tmp_1548 = SP - 16.U(16.W)
        val __tmp_1549 = (generalRegFiles(1.U)).asUInt
        arrayRegFiles(__tmp_1548 + 0.U) := __tmp_1549(7, 0)
        arrayRegFiles(__tmp_1548 + 1.U) := __tmp_1549(15, 8)
        arrayRegFiles(__tmp_1548 + 2.U) := __tmp_1549(23, 16)
        arrayRegFiles(__tmp_1548 + 3.U) := __tmp_1549(31, 24)
        arrayRegFiles(__tmp_1548 + 4.U) := __tmp_1549(39, 32)
        arrayRegFiles(__tmp_1548 + 5.U) := __tmp_1549(47, 40)
        arrayRegFiles(__tmp_1548 + 6.U) := __tmp_1549(55, 48)
        arrayRegFiles(__tmp_1548 + 7.U) := __tmp_1549(63, 56)

        val __tmp_1550 = SP - 8.U(16.W)
        val __tmp_1551 = (generalRegFiles(2.U)).asUInt
        arrayRegFiles(__tmp_1550 + 0.U) := __tmp_1551(7, 0)
        arrayRegFiles(__tmp_1550 + 1.U) := __tmp_1551(15, 8)
        arrayRegFiles(__tmp_1550 + 2.U) := __tmp_1551(23, 16)
        arrayRegFiles(__tmp_1550 + 3.U) := __tmp_1551(31, 24)
        arrayRegFiles(__tmp_1550 + 4.U) := __tmp_1551(39, 32)
        arrayRegFiles(__tmp_1550 + 5.U) := __tmp_1551(47, 40)
        arrayRegFiles(__tmp_1550 + 6.U) := __tmp_1551(55, 48)
        arrayRegFiles(__tmp_1550 + 7.U) := __tmp_1551(63, 56)

        CP := 52.U
      }

      is(124.U) {
        /*
        $0 = *(SP - (24 [SP])) [unsigned, U64, 8]  // restore $0
        $1 = *(SP - (16 [SP])) [unsigned, U64, 8]  // restore $1
        $2 = *(SP - (8 [SP])) [unsigned, U64, 8]  // restore $2
        $3 = **(SP + (2 [SP])) [unsigned, anvil.PrinterIndex.U, 8]  // $3 = $ret
        undecl size: anvil.PrinterIndex.U [@79, 8], offset: anvil.PrinterIndex.U [@71, 8], memory: SP [@69, 2], $sfCurrentId: SP [@67, 2], $sfDesc: IS[45, U8] [@10, 57], $sfLoc: U32 [@6, 4], $sfCaller: SP [@4, 2], $res: SP [@2, 2], $ret: CP [@0, 2]
        goto .125
        */


        val __tmp_1552 = (SP - 24.U(16.W)).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_1552 + 7.U),
          arrayRegFiles(__tmp_1552 + 6.U),
          arrayRegFiles(__tmp_1552 + 5.U),
          arrayRegFiles(__tmp_1552 + 4.U),
          arrayRegFiles(__tmp_1552 + 3.U),
          arrayRegFiles(__tmp_1552 + 2.U),
          arrayRegFiles(__tmp_1552 + 1.U),
          arrayRegFiles(__tmp_1552 + 0.U)
        ).asUInt

        val __tmp_1553 = (SP - 16.U(16.W)).asUInt
        generalRegFiles(1.U) := Cat(
          arrayRegFiles(__tmp_1553 + 7.U),
          arrayRegFiles(__tmp_1553 + 6.U),
          arrayRegFiles(__tmp_1553 + 5.U),
          arrayRegFiles(__tmp_1553 + 4.U),
          arrayRegFiles(__tmp_1553 + 3.U),
          arrayRegFiles(__tmp_1553 + 2.U),
          arrayRegFiles(__tmp_1553 + 1.U),
          arrayRegFiles(__tmp_1553 + 0.U)
        ).asUInt

        val __tmp_1554 = (SP - 8.U(16.W)).asUInt
        generalRegFiles(2.U) := Cat(
          arrayRegFiles(__tmp_1554 + 7.U),
          arrayRegFiles(__tmp_1554 + 6.U),
          arrayRegFiles(__tmp_1554 + 5.U),
          arrayRegFiles(__tmp_1554 + 4.U),
          arrayRegFiles(__tmp_1554 + 3.U),
          arrayRegFiles(__tmp_1554 + 2.U),
          arrayRegFiles(__tmp_1554 + 1.U),
          arrayRegFiles(__tmp_1554 + 0.U)
        ).asUInt

        val __tmp_1555 = (Cat(
          arrayRegFiles(SP + 2.U(16.W) + 1.U),
          arrayRegFiles(SP + 2.U(16.W) + 0.U)
        )).asUInt
        generalRegFiles(3.U) := Cat(
          arrayRegFiles(__tmp_1555 + 7.U),
          arrayRegFiles(__tmp_1555 + 6.U),
          arrayRegFiles(__tmp_1555 + 5.U),
          arrayRegFiles(__tmp_1555 + 4.U),
          arrayRegFiles(__tmp_1555 + 3.U),
          arrayRegFiles(__tmp_1555 + 2.U),
          arrayRegFiles(__tmp_1555 + 1.U),
          arrayRegFiles(__tmp_1555 + 0.U)
        ).asUInt

        CP := 125.U
      }

      is(125.U) {
        /*
        SP = SP - 244
        goto .126
        */


        SP := SP - 244.U

        CP := 126.U
      }

      is(126.U) {
        /*
        *(SP + (188 [SP])) = $3 [signed, anvil.PrinterIndex.U, 8]  // sfDescSize = $3
        goto .127
        */


        val __tmp_1556 = SP + 188.U(16.W)
        val __tmp_1557 = (generalRegFiles(3.U)).asUInt
        arrayRegFiles(__tmp_1556 + 0.U) := __tmp_1557(7, 0)
        arrayRegFiles(__tmp_1556 + 1.U) := __tmp_1557(15, 8)
        arrayRegFiles(__tmp_1556 + 2.U) := __tmp_1557(23, 16)
        arrayRegFiles(__tmp_1556 + 3.U) := __tmp_1557(31, 24)
        arrayRegFiles(__tmp_1556 + 4.U) := __tmp_1557(39, 32)
        arrayRegFiles(__tmp_1556 + 5.U) := __tmp_1557(47, 40)
        arrayRegFiles(__tmp_1556 + 6.U) := __tmp_1557(55, 48)
        arrayRegFiles(__tmp_1556 + 7.U) := __tmp_1557(63, 56)

        CP := 127.U
      }

      is(127.U) {
        /*
        *(SP + (6 [SP])) = (647 [U32]) [signed, U32, 4]  // $sfLoc = (647 [U32])
        goto .128
        */


        val __tmp_1558 = SP + 6.U(16.W)
        val __tmp_1559 = (647.S(32.W)).asUInt
        arrayRegFiles(__tmp_1558 + 0.U) := __tmp_1559(7, 0)
        arrayRegFiles(__tmp_1558 + 1.U) := __tmp_1559(15, 8)
        arrayRegFiles(__tmp_1558 + 2.U) := __tmp_1559(23, 16)
        arrayRegFiles(__tmp_1558 + 3.U) := __tmp_1559(31, 24)

        CP := 128.U
      }

      is(128.U) {
        /*
        $0 = *(SP + (164 [SP])) [unsigned, anvil.PrinterIndex.U, 8]  // $0 = offset
        $1 = *(SP + (124 [SP])) [unsigned, anvil.PrinterIndex.U, 8]  // $1 = sizeSize
        goto .129
        */


        val __tmp_1560 = (SP + 164.U(16.W)).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_1560 + 7.U),
          arrayRegFiles(__tmp_1560 + 6.U),
          arrayRegFiles(__tmp_1560 + 5.U),
          arrayRegFiles(__tmp_1560 + 4.U),
          arrayRegFiles(__tmp_1560 + 3.U),
          arrayRegFiles(__tmp_1560 + 2.U),
          arrayRegFiles(__tmp_1560 + 1.U),
          arrayRegFiles(__tmp_1560 + 0.U)
        ).asUInt

        val __tmp_1561 = (SP + 124.U(16.W)).asUInt
        generalRegFiles(1.U) := Cat(
          arrayRegFiles(__tmp_1561 + 7.U),
          arrayRegFiles(__tmp_1561 + 6.U),
          arrayRegFiles(__tmp_1561 + 5.U),
          arrayRegFiles(__tmp_1561 + 4.U),
          arrayRegFiles(__tmp_1561 + 3.U),
          arrayRegFiles(__tmp_1561 + 2.U),
          arrayRegFiles(__tmp_1561 + 1.U),
          arrayRegFiles(__tmp_1561 + 0.U)
        ).asUInt

        CP := 129.U
      }

      is(129.U) {
        /*
        $2 = ($0 + $1)
        goto .130
        */


        generalRegFiles(2.U) := generalRegFiles(0.U) + generalRegFiles(1.U)
        CP := 130.U
      }

      is(130.U) {
        /*
        *(SP + (164 [SP])) = $2 [signed, anvil.PrinterIndex.U, 8]  // offset = $2
        goto .131
        */


        val __tmp_1562 = SP + 164.U(16.W)
        val __tmp_1563 = (generalRegFiles(2.U)).asUInt
        arrayRegFiles(__tmp_1562 + 0.U) := __tmp_1563(7, 0)
        arrayRegFiles(__tmp_1562 + 1.U) := __tmp_1563(15, 8)
        arrayRegFiles(__tmp_1562 + 2.U) := __tmp_1563(23, 16)
        arrayRegFiles(__tmp_1562 + 3.U) := __tmp_1563(31, 24)
        arrayRegFiles(__tmp_1562 + 4.U) := __tmp_1563(39, 32)
        arrayRegFiles(__tmp_1562 + 5.U) := __tmp_1563(47, 40)
        arrayRegFiles(__tmp_1562 + 6.U) := __tmp_1563(55, 48)
        arrayRegFiles(__tmp_1562 + 7.U) := __tmp_1563(63, 56)

        CP := 131.U
      }

      is(131.U) {
        /*
        *(SP + (6 [SP])) = (648 [U32]) [signed, U32, 4]  // $sfLoc = (648 [U32])
        goto .132
        */


        val __tmp_1564 = SP + 6.U(16.W)
        val __tmp_1565 = (648.S(32.W)).asUInt
        arrayRegFiles(__tmp_1564 + 0.U) := __tmp_1565(7, 0)
        arrayRegFiles(__tmp_1564 + 1.U) := __tmp_1565(15, 8)
        arrayRegFiles(__tmp_1564 + 2.U) := __tmp_1565(23, 16)
        arrayRegFiles(__tmp_1564 + 3.U) := __tmp_1565(31, 24)

        CP := 132.U
      }

      is(132.U) {
        /*
        $0 = *(SP + (80 [SP])) [unsigned, MS[anvil.PrinterIndex.U, U8], 2]  // $0 = buffer
        $1 = *(SP + (156 [SP])) [unsigned, anvil.PrinterIndex.U, 8]  // $1 = idx
        $2 = *(SP + (92 [SP])) [unsigned, anvil.PrinterIndex.U, 8]  // $2 = mask
        goto .133
        */


        val __tmp_1566 = (SP + 80.U(16.W)).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_1566 + 1.U),
          arrayRegFiles(__tmp_1566 + 0.U)
        ).asUInt

        val __tmp_1567 = (SP + 156.U(16.W)).asUInt
        generalRegFiles(1.U) := Cat(
          arrayRegFiles(__tmp_1567 + 7.U),
          arrayRegFiles(__tmp_1567 + 6.U),
          arrayRegFiles(__tmp_1567 + 5.U),
          arrayRegFiles(__tmp_1567 + 4.U),
          arrayRegFiles(__tmp_1567 + 3.U),
          arrayRegFiles(__tmp_1567 + 2.U),
          arrayRegFiles(__tmp_1567 + 1.U),
          arrayRegFiles(__tmp_1567 + 0.U)
        ).asUInt

        val __tmp_1568 = (SP + 92.U(16.W)).asUInt
        generalRegFiles(2.U) := Cat(
          arrayRegFiles(__tmp_1568 + 7.U),
          arrayRegFiles(__tmp_1568 + 6.U),
          arrayRegFiles(__tmp_1568 + 5.U),
          arrayRegFiles(__tmp_1568 + 4.U),
          arrayRegFiles(__tmp_1568 + 3.U),
          arrayRegFiles(__tmp_1568 + 2.U),
          arrayRegFiles(__tmp_1568 + 1.U),
          arrayRegFiles(__tmp_1568 + 0.U)
        ).asUInt

        CP := 133.U
      }

      is(133.U) {
        /*
        $3 = ($1 & $2)
        goto .134
        */


        generalRegFiles(3.U) := generalRegFiles(1.U) & generalRegFiles(2.U)
        CP := 134.U
      }

      is(134.U) {
        /*
        *(($0 + (12 [SP])) + ($3 as SP)) = (32 [U8]) [unsigned, U8, 1]  // $0($3) = (32 [U8])
        goto .135
        */


        val __tmp_1569 = generalRegFiles(0.U) + 12.U(16.W) + generalRegFiles(3.U).asUInt
        val __tmp_1570 = (32.U(8.W)).asUInt
        arrayRegFiles(__tmp_1569 + 0.U) := __tmp_1570(7, 0)

        CP := 135.U
      }

      is(135.U) {
        /*
        *(SP + (6 [SP])) = (649 [U32]) [signed, U32, 4]  // $sfLoc = (649 [U32])
        goto .136
        */


        val __tmp_1571 = SP + 6.U(16.W)
        val __tmp_1572 = (649.S(32.W)).asUInt
        arrayRegFiles(__tmp_1571 + 0.U) := __tmp_1572(7, 0)
        arrayRegFiles(__tmp_1571 + 1.U) := __tmp_1572(15, 8)
        arrayRegFiles(__tmp_1571 + 2.U) := __tmp_1572(23, 16)
        arrayRegFiles(__tmp_1571 + 3.U) := __tmp_1572(31, 24)

        CP := 136.U
      }

      is(136.U) {
        /*
        $0 = *(SP + (80 [SP])) [unsigned, MS[anvil.PrinterIndex.U, U8], 2]  // $0 = buffer
        $1 = *(SP + (156 [SP])) [unsigned, anvil.PrinterIndex.U, 8]  // $1 = idx
        goto .137
        */


        val __tmp_1573 = (SP + 80.U(16.W)).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_1573 + 1.U),
          arrayRegFiles(__tmp_1573 + 0.U)
        ).asUInt

        val __tmp_1574 = (SP + 156.U(16.W)).asUInt
        generalRegFiles(1.U) := Cat(
          arrayRegFiles(__tmp_1574 + 7.U),
          arrayRegFiles(__tmp_1574 + 6.U),
          arrayRegFiles(__tmp_1574 + 5.U),
          arrayRegFiles(__tmp_1574 + 4.U),
          arrayRegFiles(__tmp_1574 + 3.U),
          arrayRegFiles(__tmp_1574 + 2.U),
          arrayRegFiles(__tmp_1574 + 1.U),
          arrayRegFiles(__tmp_1574 + 0.U)
        ).asUInt

        CP := 137.U
      }

      is(137.U) {
        /*
        $2 = ($1 + (1 [anvil.PrinterIndex.U]))
        goto .138
        */


        generalRegFiles(2.U) := generalRegFiles(1.U) + 1.U(64.W)
        CP := 138.U
      }

      is(138.U) {
        /*
        $3 = *(SP + (92 [SP])) [unsigned, anvil.PrinterIndex.U, 8]  // $3 = mask
        goto .139
        */


        val __tmp_1575 = (SP + 92.U(16.W)).asUInt
        generalRegFiles(3.U) := Cat(
          arrayRegFiles(__tmp_1575 + 7.U),
          arrayRegFiles(__tmp_1575 + 6.U),
          arrayRegFiles(__tmp_1575 + 5.U),
          arrayRegFiles(__tmp_1575 + 4.U),
          arrayRegFiles(__tmp_1575 + 3.U),
          arrayRegFiles(__tmp_1575 + 2.U),
          arrayRegFiles(__tmp_1575 + 1.U),
          arrayRegFiles(__tmp_1575 + 0.U)
        ).asUInt

        CP := 139.U
      }

      is(139.U) {
        /*
        $4 = ($2 & $3)
        goto .140
        */


        generalRegFiles(4.U) := generalRegFiles(2.U) & generalRegFiles(3.U)
        CP := 140.U
      }

      is(140.U) {
        /*
        unalloc load$res@[646,24].7AFE7F1A: anvil.PrinterIndex.U [@196, 8]
        *(($0 + (12 [SP])) + ($4 as SP)) = (32 [U8]) [unsigned, U8, 1]  // $0($4) = (32 [U8])
        goto .141
        */


        val __tmp_1576 = generalRegFiles(0.U) + 12.U(16.W) + generalRegFiles(4.U).asUInt
        val __tmp_1577 = (32.U(8.W)).asUInt
        arrayRegFiles(__tmp_1576 + 0.U) := __tmp_1577(7, 0)

        CP := 141.U
      }

      is(141.U) {
        /*
        *(SP + (6 [SP])) = (650 [U32]) [signed, U32, 4]  // $sfLoc = (650 [U32])
        goto .142
        */


        val __tmp_1578 = SP + 6.U(16.W)
        val __tmp_1579 = (650.S(32.W)).asUInt
        arrayRegFiles(__tmp_1578 + 0.U) := __tmp_1579(7, 0)
        arrayRegFiles(__tmp_1578 + 1.U) := __tmp_1579(15, 8)
        arrayRegFiles(__tmp_1578 + 2.U) := __tmp_1579(23, 16)
        arrayRegFiles(__tmp_1578 + 3.U) := __tmp_1579(31, 24)

        CP := 142.U
      }

      is(142.U) {
        /*
        $0 = *(SP + (156 [SP])) [unsigned, anvil.PrinterIndex.U, 8]  // $0 = idx
        goto .143
        */


        val __tmp_1580 = (SP + 156.U(16.W)).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_1580 + 7.U),
          arrayRegFiles(__tmp_1580 + 6.U),
          arrayRegFiles(__tmp_1580 + 5.U),
          arrayRegFiles(__tmp_1580 + 4.U),
          arrayRegFiles(__tmp_1580 + 3.U),
          arrayRegFiles(__tmp_1580 + 2.U),
          arrayRegFiles(__tmp_1580 + 1.U),
          arrayRegFiles(__tmp_1580 + 0.U)
        ).asUInt

        CP := 143.U
      }

      is(143.U) {
        /*
        $1 = ($0 + (2 [anvil.PrinterIndex.U]))
        goto .144
        */


        generalRegFiles(1.U) := generalRegFiles(0.U) + 2.U(64.W)
        CP := 144.U
      }

      is(144.U) {
        /*
        *(SP + (156 [SP])) = $1 [signed, anvil.PrinterIndex.U, 8]  // idx = $1
        goto .145
        */


        val __tmp_1581 = SP + 156.U(16.W)
        val __tmp_1582 = (generalRegFiles(1.U)).asUInt
        arrayRegFiles(__tmp_1581 + 0.U) := __tmp_1582(7, 0)
        arrayRegFiles(__tmp_1581 + 1.U) := __tmp_1582(15, 8)
        arrayRegFiles(__tmp_1581 + 2.U) := __tmp_1582(23, 16)
        arrayRegFiles(__tmp_1581 + 3.U) := __tmp_1582(31, 24)
        arrayRegFiles(__tmp_1581 + 4.U) := __tmp_1582(39, 32)
        arrayRegFiles(__tmp_1581 + 5.U) := __tmp_1582(47, 40)
        arrayRegFiles(__tmp_1581 + 6.U) := __tmp_1582(55, 48)
        arrayRegFiles(__tmp_1581 + 7.U) := __tmp_1582(63, 56)

        CP := 145.U
      }

      is(145.U) {
        /*
        *(SP + (6 [SP])) = (651 [U32]) [signed, U32, 4]  // $sfLoc = (651 [U32])
        goto .146
        */


        val __tmp_1583 = SP + 6.U(16.W)
        val __tmp_1584 = (651.S(32.W)).asUInt
        arrayRegFiles(__tmp_1583 + 0.U) := __tmp_1584(7, 0)
        arrayRegFiles(__tmp_1583 + 1.U) := __tmp_1584(15, 8)
        arrayRegFiles(__tmp_1583 + 2.U) := __tmp_1584(23, 16)
        arrayRegFiles(__tmp_1583 + 3.U) := __tmp_1584(31, 24)

        CP := 146.U
      }

      is(146.U) {
        /*
        $0 = *(SP + (148 [SP])) [unsigned, U64, 8]  // $0 = r
        goto .147
        */


        val __tmp_1585 = (SP + 148.U(16.W)).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_1585 + 7.U),
          arrayRegFiles(__tmp_1585 + 6.U),
          arrayRegFiles(__tmp_1585 + 5.U),
          arrayRegFiles(__tmp_1585 + 4.U),
          arrayRegFiles(__tmp_1585 + 3.U),
          arrayRegFiles(__tmp_1585 + 2.U),
          arrayRegFiles(__tmp_1585 + 1.U),
          arrayRegFiles(__tmp_1585 + 0.U)
        ).asUInt

        CP := 147.U
      }

      is(147.U) {
        /*
        $1 = ($0 + (2 [U64]))
        goto .148
        */


        generalRegFiles(1.U) := generalRegFiles(0.U) + 2.U(64.W)
        CP := 148.U
      }

      is(148.U) {
        /*
        *(SP + (148 [SP])) = $1 [signed, U64, 8]  // r = $1
        goto .149
        */


        val __tmp_1586 = SP + 148.U(16.W)
        val __tmp_1587 = (generalRegFiles(1.U)).asUInt
        arrayRegFiles(__tmp_1586 + 0.U) := __tmp_1587(7, 0)
        arrayRegFiles(__tmp_1586 + 1.U) := __tmp_1587(15, 8)
        arrayRegFiles(__tmp_1586 + 2.U) := __tmp_1587(23, 16)
        arrayRegFiles(__tmp_1586 + 3.U) := __tmp_1587(31, 24)
        arrayRegFiles(__tmp_1586 + 4.U) := __tmp_1587(39, 32)
        arrayRegFiles(__tmp_1586 + 5.U) := __tmp_1587(47, 40)
        arrayRegFiles(__tmp_1586 + 6.U) := __tmp_1587(55, 48)
        arrayRegFiles(__tmp_1586 + 7.U) := __tmp_1587(63, 56)

        CP := 149.U
      }

      is(149.U) {
        /*
        *(SP + (6 [SP])) = (652 [U32]) [signed, U32, 4]  // $sfLoc = (652 [U32])
        goto .150
        */


        val __tmp_1588 = SP + 6.U(16.W)
        val __tmp_1589 = (652.S(32.W)).asUInt
        arrayRegFiles(__tmp_1588 + 0.U) := __tmp_1589(7, 0)
        arrayRegFiles(__tmp_1588 + 1.U) := __tmp_1589(15, 8)
        arrayRegFiles(__tmp_1588 + 2.U) := __tmp_1589(23, 16)
        arrayRegFiles(__tmp_1588 + 3.U) := __tmp_1589(31, 24)

        CP := 150.U
      }

      is(150.U) {
        /*
        decl i: anvil.PrinterIndex.U [@196, 8]
        *(SP + (196 [SP])) = (0 [anvil.PrinterIndex.U]) [signed, anvil.PrinterIndex.U, 8]  // i = (0 [anvil.PrinterIndex.U])
        goto .151
        */


        val __tmp_1590 = SP + 196.U(16.W)
        val __tmp_1591 = (0.S(64.W)).asUInt
        arrayRegFiles(__tmp_1590 + 0.U) := __tmp_1591(7, 0)
        arrayRegFiles(__tmp_1590 + 1.U) := __tmp_1591(15, 8)
        arrayRegFiles(__tmp_1590 + 2.U) := __tmp_1591(23, 16)
        arrayRegFiles(__tmp_1590 + 3.U) := __tmp_1591(31, 24)
        arrayRegFiles(__tmp_1590 + 4.U) := __tmp_1591(39, 32)
        arrayRegFiles(__tmp_1590 + 5.U) := __tmp_1591(47, 40)
        arrayRegFiles(__tmp_1590 + 6.U) := __tmp_1591(55, 48)
        arrayRegFiles(__tmp_1590 + 7.U) := __tmp_1591(63, 56)

        CP := 151.U
      }

      is(151.U) {
        /*
        *(SP + (6 [SP])) = (653 [U32]) [signed, U32, 4]  // $sfLoc = (653 [U32])
        goto .152
        */


        val __tmp_1592 = SP + 6.U(16.W)
        val __tmp_1593 = (653.S(32.W)).asUInt
        arrayRegFiles(__tmp_1592 + 0.U) := __tmp_1593(7, 0)
        arrayRegFiles(__tmp_1592 + 1.U) := __tmp_1593(15, 8)
        arrayRegFiles(__tmp_1592 + 2.U) := __tmp_1593(23, 16)
        arrayRegFiles(__tmp_1592 + 3.U) := __tmp_1593(31, 24)

        CP := 152.U
      }

      is(152.U) {
        /*
        *(SP + (6 [SP])) = (653 [U32]) [signed, U32, 4]  // $sfLoc = (653 [U32])
        goto .153
        */


        val __tmp_1594 = SP + 6.U(16.W)
        val __tmp_1595 = (653.S(32.W)).asUInt
        arrayRegFiles(__tmp_1594 + 0.U) := __tmp_1595(7, 0)
        arrayRegFiles(__tmp_1594 + 1.U) := __tmp_1595(15, 8)
        arrayRegFiles(__tmp_1594 + 2.U) := __tmp_1595(23, 16)
        arrayRegFiles(__tmp_1594 + 3.U) := __tmp_1595(31, 24)

        CP := 153.U
      }

      is(153.U) {
        /*
        $0 = *(SP + (196 [SP])) [unsigned, anvil.PrinterIndex.U, 8]  // $0 = i
        $1 = *(SP + (188 [SP])) [unsigned, anvil.PrinterIndex.U, 8]  // $1 = sfDescSize
        goto .154
        */


        val __tmp_1596 = (SP + 196.U(16.W)).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_1596 + 7.U),
          arrayRegFiles(__tmp_1596 + 6.U),
          arrayRegFiles(__tmp_1596 + 5.U),
          arrayRegFiles(__tmp_1596 + 4.U),
          arrayRegFiles(__tmp_1596 + 3.U),
          arrayRegFiles(__tmp_1596 + 2.U),
          arrayRegFiles(__tmp_1596 + 1.U),
          arrayRegFiles(__tmp_1596 + 0.U)
        ).asUInt

        val __tmp_1597 = (SP + 188.U(16.W)).asUInt
        generalRegFiles(1.U) := Cat(
          arrayRegFiles(__tmp_1597 + 7.U),
          arrayRegFiles(__tmp_1597 + 6.U),
          arrayRegFiles(__tmp_1597 + 5.U),
          arrayRegFiles(__tmp_1597 + 4.U),
          arrayRegFiles(__tmp_1597 + 3.U),
          arrayRegFiles(__tmp_1597 + 2.U),
          arrayRegFiles(__tmp_1597 + 1.U),
          arrayRegFiles(__tmp_1597 + 0.U)
        ).asUInt

        CP := 154.U
      }

      is(154.U) {
        /*
        $2 = ($0 < $1)
        goto .155
        */


        generalRegFiles(2.U) := (generalRegFiles(0.U) < generalRegFiles(1.U)).asUInt
        CP := 155.U
      }

      is(155.U) {
        /*
        if $2 goto .156 else goto .174
        */


        CP := Mux((generalRegFiles(2.U).asUInt) === 1.U, 156.U, 174.U)
      }

      is(156.U) {
        /*
        *(SP + (6 [SP])) = (654 [U32]) [signed, U32, 4]  // $sfLoc = (654 [U32])
        goto .157
        */


        val __tmp_1598 = SP + 6.U(16.W)
        val __tmp_1599 = (654.S(32.W)).asUInt
        arrayRegFiles(__tmp_1598 + 0.U) := __tmp_1599(7, 0)
        arrayRegFiles(__tmp_1598 + 1.U) := __tmp_1599(15, 8)
        arrayRegFiles(__tmp_1598 + 2.U) := __tmp_1599(23, 16)
        arrayRegFiles(__tmp_1598 + 3.U) := __tmp_1599(31, 24)

        CP := 157.U
      }

      is(157.U) {
        /*
        $0 = *(SP + (80 [SP])) [unsigned, MS[anvil.PrinterIndex.U, U8], 2]  // $0 = buffer
        $1 = *(SP + (156 [SP])) [unsigned, anvil.PrinterIndex.U, 8]  // $1 = idx
        $2 = *(SP + (196 [SP])) [unsigned, anvil.PrinterIndex.U, 8]  // $2 = i
        goto .158
        */


        val __tmp_1600 = (SP + 80.U(16.W)).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_1600 + 1.U),
          arrayRegFiles(__tmp_1600 + 0.U)
        ).asUInt

        val __tmp_1601 = (SP + 156.U(16.W)).asUInt
        generalRegFiles(1.U) := Cat(
          arrayRegFiles(__tmp_1601 + 7.U),
          arrayRegFiles(__tmp_1601 + 6.U),
          arrayRegFiles(__tmp_1601 + 5.U),
          arrayRegFiles(__tmp_1601 + 4.U),
          arrayRegFiles(__tmp_1601 + 3.U),
          arrayRegFiles(__tmp_1601 + 2.U),
          arrayRegFiles(__tmp_1601 + 1.U),
          arrayRegFiles(__tmp_1601 + 0.U)
        ).asUInt

        val __tmp_1602 = (SP + 196.U(16.W)).asUInt
        generalRegFiles(2.U) := Cat(
          arrayRegFiles(__tmp_1602 + 7.U),
          arrayRegFiles(__tmp_1602 + 6.U),
          arrayRegFiles(__tmp_1602 + 5.U),
          arrayRegFiles(__tmp_1602 + 4.U),
          arrayRegFiles(__tmp_1602 + 3.U),
          arrayRegFiles(__tmp_1602 + 2.U),
          arrayRegFiles(__tmp_1602 + 1.U),
          arrayRegFiles(__tmp_1602 + 0.U)
        ).asUInt

        CP := 158.U
      }

      is(158.U) {
        /*
        $3 = ($1 + $2)
        goto .159
        */


        generalRegFiles(3.U) := generalRegFiles(1.U) + generalRegFiles(2.U)
        CP := 159.U
      }

      is(159.U) {
        /*
        $4 = *(SP + (92 [SP])) [unsigned, anvil.PrinterIndex.U, 8]  // $4 = mask
        goto .160
        */


        val __tmp_1603 = (SP + 92.U(16.W)).asUInt
        generalRegFiles(4.U) := Cat(
          arrayRegFiles(__tmp_1603 + 7.U),
          arrayRegFiles(__tmp_1603 + 6.U),
          arrayRegFiles(__tmp_1603 + 5.U),
          arrayRegFiles(__tmp_1603 + 4.U),
          arrayRegFiles(__tmp_1603 + 3.U),
          arrayRegFiles(__tmp_1603 + 2.U),
          arrayRegFiles(__tmp_1603 + 1.U),
          arrayRegFiles(__tmp_1603 + 0.U)
        ).asUInt

        CP := 160.U
      }

      is(160.U) {
        /*
        $5 = ($3 & $4)
        goto .161
        */


        generalRegFiles(5.U) := generalRegFiles(3.U) & generalRegFiles(4.U)
        CP := 161.U
      }

      is(161.U) {
        /*
        $6 = *(SP + (90 [SP])) [unsigned, MS[anvil.PrinterIndex.U, U8], 2]  // $6 = memory
        $7 = *(SP + (164 [SP])) [unsigned, anvil.PrinterIndex.U, 8]  // $7 = offset
        $8 = *(SP + (196 [SP])) [unsigned, anvil.PrinterIndex.U, 8]  // $8 = i
        goto .162
        */


        val __tmp_1604 = (SP + 90.U(16.W)).asUInt
        generalRegFiles(6.U) := Cat(
          arrayRegFiles(__tmp_1604 + 1.U),
          arrayRegFiles(__tmp_1604 + 0.U)
        ).asUInt

        val __tmp_1605 = (SP + 164.U(16.W)).asUInt
        generalRegFiles(7.U) := Cat(
          arrayRegFiles(__tmp_1605 + 7.U),
          arrayRegFiles(__tmp_1605 + 6.U),
          arrayRegFiles(__tmp_1605 + 5.U),
          arrayRegFiles(__tmp_1605 + 4.U),
          arrayRegFiles(__tmp_1605 + 3.U),
          arrayRegFiles(__tmp_1605 + 2.U),
          arrayRegFiles(__tmp_1605 + 1.U),
          arrayRegFiles(__tmp_1605 + 0.U)
        ).asUInt

        val __tmp_1606 = (SP + 196.U(16.W)).asUInt
        generalRegFiles(8.U) := Cat(
          arrayRegFiles(__tmp_1606 + 7.U),
          arrayRegFiles(__tmp_1606 + 6.U),
          arrayRegFiles(__tmp_1606 + 5.U),
          arrayRegFiles(__tmp_1606 + 4.U),
          arrayRegFiles(__tmp_1606 + 3.U),
          arrayRegFiles(__tmp_1606 + 2.U),
          arrayRegFiles(__tmp_1606 + 1.U),
          arrayRegFiles(__tmp_1606 + 0.U)
        ).asUInt

        CP := 162.U
      }

      is(162.U) {
        /*
        $9 = ($7 + $8)
        goto .163
        */


        generalRegFiles(9.U) := generalRegFiles(7.U) + generalRegFiles(8.U)
        CP := 163.U
      }

      is(163.U) {
        /*
        $10 = *(($6 + (12 [SP])) + ($9 as SP)) [unsigned, U8, 1]  // $10 = $6($9)
        goto .164
        */


        val __tmp_1607 = (generalRegFiles(6.U) + 12.U(16.W) + generalRegFiles(9.U).asUInt).asUInt
        generalRegFiles(10.U) := Cat(
          arrayRegFiles(__tmp_1607 + 0.U)
        ).asUInt

        CP := 164.U
      }

      is(164.U) {
        /*
        *(($0 + (12 [SP])) + ($5 as SP)) = $10 [unsigned, U8, 1]  // $0($5) = $10
        goto .165
        */


        val __tmp_1608 = generalRegFiles(0.U) + 12.U(16.W) + generalRegFiles(5.U).asUInt
        val __tmp_1609 = (generalRegFiles(10.U)).asUInt
        arrayRegFiles(__tmp_1608 + 0.U) := __tmp_1609(7, 0)

        CP := 165.U
      }

      is(165.U) {
        /*
        *(SP + (6 [SP])) = (655 [U32]) [signed, U32, 4]  // $sfLoc = (655 [U32])
        goto .166
        */


        val __tmp_1610 = SP + 6.U(16.W)
        val __tmp_1611 = (655.S(32.W)).asUInt
        arrayRegFiles(__tmp_1610 + 0.U) := __tmp_1611(7, 0)
        arrayRegFiles(__tmp_1610 + 1.U) := __tmp_1611(15, 8)
        arrayRegFiles(__tmp_1610 + 2.U) := __tmp_1611(23, 16)
        arrayRegFiles(__tmp_1610 + 3.U) := __tmp_1611(31, 24)

        CP := 166.U
      }

      is(166.U) {
        /*
        $0 = *(SP + (148 [SP])) [unsigned, U64, 8]  // $0 = r
        goto .167
        */


        val __tmp_1612 = (SP + 148.U(16.W)).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_1612 + 7.U),
          arrayRegFiles(__tmp_1612 + 6.U),
          arrayRegFiles(__tmp_1612 + 5.U),
          arrayRegFiles(__tmp_1612 + 4.U),
          arrayRegFiles(__tmp_1612 + 3.U),
          arrayRegFiles(__tmp_1612 + 2.U),
          arrayRegFiles(__tmp_1612 + 1.U),
          arrayRegFiles(__tmp_1612 + 0.U)
        ).asUInt

        CP := 167.U
      }

      is(167.U) {
        /*
        $1 = ($0 + (1 [U64]))
        goto .168
        */


        generalRegFiles(1.U) := generalRegFiles(0.U) + 1.U(64.W)
        CP := 168.U
      }

      is(168.U) {
        /*
        *(SP + (148 [SP])) = $1 [signed, U64, 8]  // r = $1
        goto .169
        */


        val __tmp_1613 = SP + 148.U(16.W)
        val __tmp_1614 = (generalRegFiles(1.U)).asUInt
        arrayRegFiles(__tmp_1613 + 0.U) := __tmp_1614(7, 0)
        arrayRegFiles(__tmp_1613 + 1.U) := __tmp_1614(15, 8)
        arrayRegFiles(__tmp_1613 + 2.U) := __tmp_1614(23, 16)
        arrayRegFiles(__tmp_1613 + 3.U) := __tmp_1614(31, 24)
        arrayRegFiles(__tmp_1613 + 4.U) := __tmp_1614(39, 32)
        arrayRegFiles(__tmp_1613 + 5.U) := __tmp_1614(47, 40)
        arrayRegFiles(__tmp_1613 + 6.U) := __tmp_1614(55, 48)
        arrayRegFiles(__tmp_1613 + 7.U) := __tmp_1614(63, 56)

        CP := 169.U
      }

      is(169.U) {
        /*
        *(SP + (6 [SP])) = (656 [U32]) [signed, U32, 4]  // $sfLoc = (656 [U32])
        goto .170
        */


        val __tmp_1615 = SP + 6.U(16.W)
        val __tmp_1616 = (656.S(32.W)).asUInt
        arrayRegFiles(__tmp_1615 + 0.U) := __tmp_1616(7, 0)
        arrayRegFiles(__tmp_1615 + 1.U) := __tmp_1616(15, 8)
        arrayRegFiles(__tmp_1615 + 2.U) := __tmp_1616(23, 16)
        arrayRegFiles(__tmp_1615 + 3.U) := __tmp_1616(31, 24)

        CP := 170.U
      }

      is(170.U) {
        /*
        $0 = *(SP + (196 [SP])) [unsigned, anvil.PrinterIndex.U, 8]  // $0 = i
        goto .171
        */


        val __tmp_1617 = (SP + 196.U(16.W)).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_1617 + 7.U),
          arrayRegFiles(__tmp_1617 + 6.U),
          arrayRegFiles(__tmp_1617 + 5.U),
          arrayRegFiles(__tmp_1617 + 4.U),
          arrayRegFiles(__tmp_1617 + 3.U),
          arrayRegFiles(__tmp_1617 + 2.U),
          arrayRegFiles(__tmp_1617 + 1.U),
          arrayRegFiles(__tmp_1617 + 0.U)
        ).asUInt

        CP := 171.U
      }

      is(171.U) {
        /*
        $1 = ($0 + (1 [anvil.PrinterIndex.U]))
        goto .172
        */


        generalRegFiles(1.U) := generalRegFiles(0.U) + 1.U(64.W)
        CP := 172.U
      }

      is(172.U) {
        /*
        *(SP + (196 [SP])) = $1 [signed, anvil.PrinterIndex.U, 8]  // i = $1
        goto .173
        */


        val __tmp_1618 = SP + 196.U(16.W)
        val __tmp_1619 = (generalRegFiles(1.U)).asUInt
        arrayRegFiles(__tmp_1618 + 0.U) := __tmp_1619(7, 0)
        arrayRegFiles(__tmp_1618 + 1.U) := __tmp_1619(15, 8)
        arrayRegFiles(__tmp_1618 + 2.U) := __tmp_1619(23, 16)
        arrayRegFiles(__tmp_1618 + 3.U) := __tmp_1619(31, 24)
        arrayRegFiles(__tmp_1618 + 4.U) := __tmp_1619(39, 32)
        arrayRegFiles(__tmp_1618 + 5.U) := __tmp_1619(47, 40)
        arrayRegFiles(__tmp_1618 + 6.U) := __tmp_1619(55, 48)
        arrayRegFiles(__tmp_1618 + 7.U) := __tmp_1619(63, 56)

        CP := 173.U
      }

      is(173.U) {
        /*
        *(SP + (6 [SP])) = (653 [U32]) [signed, U32, 4]  // $sfLoc = (653 [U32])
        goto .152
        */


        val __tmp_1620 = SP + 6.U(16.W)
        val __tmp_1621 = (653.S(32.W)).asUInt
        arrayRegFiles(__tmp_1620 + 0.U) := __tmp_1621(7, 0)
        arrayRegFiles(__tmp_1620 + 1.U) := __tmp_1621(15, 8)
        arrayRegFiles(__tmp_1620 + 2.U) := __tmp_1621(23, 16)
        arrayRegFiles(__tmp_1620 + 3.U) := __tmp_1621(31, 24)

        CP := 152.U
      }

      is(174.U) {
        /*
        *(SP + (6 [SP])) = (658 [U32]) [signed, U32, 4]  // $sfLoc = (658 [U32])
        goto .175
        */


        val __tmp_1622 = SP + 6.U(16.W)
        val __tmp_1623 = (658.S(32.W)).asUInt
        arrayRegFiles(__tmp_1622 + 0.U) := __tmp_1623(7, 0)
        arrayRegFiles(__tmp_1622 + 1.U) := __tmp_1623(15, 8)
        arrayRegFiles(__tmp_1622 + 2.U) := __tmp_1623(23, 16)
        arrayRegFiles(__tmp_1622 + 3.U) := __tmp_1623(31, 24)

        CP := 175.U
      }

      is(175.U) {
        /*
        $0 = *(SP + (156 [SP])) [unsigned, anvil.PrinterIndex.U, 8]  // $0 = idx
        $1 = *(SP + (196 [SP])) [unsigned, anvil.PrinterIndex.U, 8]  // $1 = i
        goto .176
        */


        val __tmp_1624 = (SP + 156.U(16.W)).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_1624 + 7.U),
          arrayRegFiles(__tmp_1624 + 6.U),
          arrayRegFiles(__tmp_1624 + 5.U),
          arrayRegFiles(__tmp_1624 + 4.U),
          arrayRegFiles(__tmp_1624 + 3.U),
          arrayRegFiles(__tmp_1624 + 2.U),
          arrayRegFiles(__tmp_1624 + 1.U),
          arrayRegFiles(__tmp_1624 + 0.U)
        ).asUInt

        val __tmp_1625 = (SP + 196.U(16.W)).asUInt
        generalRegFiles(1.U) := Cat(
          arrayRegFiles(__tmp_1625 + 7.U),
          arrayRegFiles(__tmp_1625 + 6.U),
          arrayRegFiles(__tmp_1625 + 5.U),
          arrayRegFiles(__tmp_1625 + 4.U),
          arrayRegFiles(__tmp_1625 + 3.U),
          arrayRegFiles(__tmp_1625 + 2.U),
          arrayRegFiles(__tmp_1625 + 1.U),
          arrayRegFiles(__tmp_1625 + 0.U)
        ).asUInt

        CP := 176.U
      }

      is(176.U) {
        /*
        $2 = ($0 + $1)
        goto .177
        */


        generalRegFiles(2.U) := generalRegFiles(0.U) + generalRegFiles(1.U)
        CP := 177.U
      }

      is(177.U) {
        /*
        *(SP + (156 [SP])) = $2 [signed, anvil.PrinterIndex.U, 8]  // idx = $2
        goto .178
        */


        val __tmp_1626 = SP + 156.U(16.W)
        val __tmp_1627 = (generalRegFiles(2.U)).asUInt
        arrayRegFiles(__tmp_1626 + 0.U) := __tmp_1627(7, 0)
        arrayRegFiles(__tmp_1626 + 1.U) := __tmp_1627(15, 8)
        arrayRegFiles(__tmp_1626 + 2.U) := __tmp_1627(23, 16)
        arrayRegFiles(__tmp_1626 + 3.U) := __tmp_1627(31, 24)
        arrayRegFiles(__tmp_1626 + 4.U) := __tmp_1627(39, 32)
        arrayRegFiles(__tmp_1626 + 5.U) := __tmp_1627(47, 40)
        arrayRegFiles(__tmp_1626 + 6.U) := __tmp_1627(55, 48)
        arrayRegFiles(__tmp_1626 + 7.U) := __tmp_1627(63, 56)

        CP := 178.U
      }

      is(178.U) {
        /*
        *(SP + (6 [SP])) = (659 [U32]) [signed, U32, 4]  // $sfLoc = (659 [U32])
        goto .179
        */


        val __tmp_1628 = SP + 6.U(16.W)
        val __tmp_1629 = (659.S(32.W)).asUInt
        arrayRegFiles(__tmp_1628 + 0.U) := __tmp_1629(7, 0)
        arrayRegFiles(__tmp_1628 + 1.U) := __tmp_1629(15, 8)
        arrayRegFiles(__tmp_1628 + 2.U) := __tmp_1629(23, 16)
        arrayRegFiles(__tmp_1628 + 3.U) := __tmp_1629(31, 24)

        CP := 179.U
      }

      is(179.U) {
        /*
        decl n: U64 [@204, 8]
        $0 = *(SP + (80 [SP])) [unsigned, MS[anvil.PrinterIndex.U, U8], 2]  // $0 = buffer
        $1 = *(SP + (156 [SP])) [unsigned, anvil.PrinterIndex.U, 8]  // $1 = idx
        $2 = *(SP + (92 [SP])) [unsigned, anvil.PrinterIndex.U, 8]  // $2 = mask
        $3 = *(SP + (172 [SP])) [unsigned, U64, 8]  // $3 = sfLoc
        alloc printU64$res@[659,15].C2D721FD: U64 [@212, 8]
        goto .180
        */


        val __tmp_1630 = (SP + 80.U(16.W)).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_1630 + 1.U),
          arrayRegFiles(__tmp_1630 + 0.U)
        ).asUInt

        val __tmp_1631 = (SP + 156.U(16.W)).asUInt
        generalRegFiles(1.U) := Cat(
          arrayRegFiles(__tmp_1631 + 7.U),
          arrayRegFiles(__tmp_1631 + 6.U),
          arrayRegFiles(__tmp_1631 + 5.U),
          arrayRegFiles(__tmp_1631 + 4.U),
          arrayRegFiles(__tmp_1631 + 3.U),
          arrayRegFiles(__tmp_1631 + 2.U),
          arrayRegFiles(__tmp_1631 + 1.U),
          arrayRegFiles(__tmp_1631 + 0.U)
        ).asUInt

        val __tmp_1632 = (SP + 92.U(16.W)).asUInt
        generalRegFiles(2.U) := Cat(
          arrayRegFiles(__tmp_1632 + 7.U),
          arrayRegFiles(__tmp_1632 + 6.U),
          arrayRegFiles(__tmp_1632 + 5.U),
          arrayRegFiles(__tmp_1632 + 4.U),
          arrayRegFiles(__tmp_1632 + 3.U),
          arrayRegFiles(__tmp_1632 + 2.U),
          arrayRegFiles(__tmp_1632 + 1.U),
          arrayRegFiles(__tmp_1632 + 0.U)
        ).asUInt

        val __tmp_1633 = (SP + 172.U(16.W)).asUInt
        generalRegFiles(3.U) := Cat(
          arrayRegFiles(__tmp_1633 + 7.U),
          arrayRegFiles(__tmp_1633 + 6.U),
          arrayRegFiles(__tmp_1633 + 5.U),
          arrayRegFiles(__tmp_1633 + 4.U),
          arrayRegFiles(__tmp_1633 + 3.U),
          arrayRegFiles(__tmp_1633 + 2.U),
          arrayRegFiles(__tmp_1633 + 1.U),
          arrayRegFiles(__tmp_1633 + 0.U)
        ).asUInt

        CP := 180.U
      }

      is(180.U) {
        /*
        SP = SP + 252
        goto .181
        */


        SP := SP + 252.U

        CP := 181.U
      }

      is(181.U) {
        /*
        decl $ret: CP [@0, 2], $res: SP [@2, 2], $sfCaller: SP [@4, 2], $sfLoc: U32 [@6, 4], $sfDesc: IS[49, U8] [@10, 61], $sfCurrentId: SP [@71, 2], buffer: SP [@73, 2], index: anvil.PrinterIndex.U [@75, 8], mask: anvil.PrinterIndex.U [@83, 8], n: U64 [@91, 8]
        *SP = (301 [CP]) [unsigned, CP, 2]  // $ret@0 = 2228
        *(SP + (2 [SP])) = (SP - (40 [SP])) [unsigned, SP, 2]  // $res@2 = -40
        *(SP + (4 [SP])) = (SP - (248 [SP])) [unsigned, SP, 2]  // $sfCaller@4 = -248
        *(SP + (71 [SP])) = (SP + (4 [SP])) [unsigned, SP, 2]  // $sfCurrentId@71 = 4
        *(SP + (73 [SP])) = $0 [unsigned, SP, 2]  // buffer = $0
        *(SP + (75 [SP])) = $1 [unsigned, anvil.PrinterIndex.U, 8]  // index = $1
        *(SP + (83 [SP])) = $2 [unsigned, anvil.PrinterIndex.U, 8]  // mask = $2
        *(SP + (91 [SP])) = $3 [unsigned, U64, 8]  // n = $3
        *(SP - (32 [SP])) = $0 [unsigned, U64, 8]  // save $0
        *(SP - (24 [SP])) = $1 [unsigned, U64, 8]  // save $1
        *(SP - (16 [SP])) = $2 [unsigned, U64, 8]  // save $2
        *(SP - (8 [SP])) = $3 [unsigned, U64, 8]  // save $3
        goto .182
        */


        val __tmp_1634 = SP
        val __tmp_1635 = (301.U(16.W)).asUInt
        arrayRegFiles(__tmp_1634 + 0.U) := __tmp_1635(7, 0)
        arrayRegFiles(__tmp_1634 + 1.U) := __tmp_1635(15, 8)

        val __tmp_1636 = SP + 2.U(16.W)
        val __tmp_1637 = (SP - 40.U(16.W)).asUInt
        arrayRegFiles(__tmp_1636 + 0.U) := __tmp_1637(7, 0)
        arrayRegFiles(__tmp_1636 + 1.U) := __tmp_1637(15, 8)

        val __tmp_1638 = SP + 4.U(16.W)
        val __tmp_1639 = (SP - 248.U(16.W)).asUInt
        arrayRegFiles(__tmp_1638 + 0.U) := __tmp_1639(7, 0)
        arrayRegFiles(__tmp_1638 + 1.U) := __tmp_1639(15, 8)

        val __tmp_1640 = SP + 71.U(16.W)
        val __tmp_1641 = (SP + 4.U(16.W)).asUInt
        arrayRegFiles(__tmp_1640 + 0.U) := __tmp_1641(7, 0)
        arrayRegFiles(__tmp_1640 + 1.U) := __tmp_1641(15, 8)

        val __tmp_1642 = SP + 73.U(16.W)
        val __tmp_1643 = (generalRegFiles(0.U)).asUInt
        arrayRegFiles(__tmp_1642 + 0.U) := __tmp_1643(7, 0)
        arrayRegFiles(__tmp_1642 + 1.U) := __tmp_1643(15, 8)

        val __tmp_1644 = SP + 75.U(16.W)
        val __tmp_1645 = (generalRegFiles(1.U)).asUInt
        arrayRegFiles(__tmp_1644 + 0.U) := __tmp_1645(7, 0)
        arrayRegFiles(__tmp_1644 + 1.U) := __tmp_1645(15, 8)
        arrayRegFiles(__tmp_1644 + 2.U) := __tmp_1645(23, 16)
        arrayRegFiles(__tmp_1644 + 3.U) := __tmp_1645(31, 24)
        arrayRegFiles(__tmp_1644 + 4.U) := __tmp_1645(39, 32)
        arrayRegFiles(__tmp_1644 + 5.U) := __tmp_1645(47, 40)
        arrayRegFiles(__tmp_1644 + 6.U) := __tmp_1645(55, 48)
        arrayRegFiles(__tmp_1644 + 7.U) := __tmp_1645(63, 56)

        val __tmp_1646 = SP + 83.U(16.W)
        val __tmp_1647 = (generalRegFiles(2.U)).asUInt
        arrayRegFiles(__tmp_1646 + 0.U) := __tmp_1647(7, 0)
        arrayRegFiles(__tmp_1646 + 1.U) := __tmp_1647(15, 8)
        arrayRegFiles(__tmp_1646 + 2.U) := __tmp_1647(23, 16)
        arrayRegFiles(__tmp_1646 + 3.U) := __tmp_1647(31, 24)
        arrayRegFiles(__tmp_1646 + 4.U) := __tmp_1647(39, 32)
        arrayRegFiles(__tmp_1646 + 5.U) := __tmp_1647(47, 40)
        arrayRegFiles(__tmp_1646 + 6.U) := __tmp_1647(55, 48)
        arrayRegFiles(__tmp_1646 + 7.U) := __tmp_1647(63, 56)

        val __tmp_1648 = SP + 91.U(16.W)
        val __tmp_1649 = (generalRegFiles(3.U)).asUInt
        arrayRegFiles(__tmp_1648 + 0.U) := __tmp_1649(7, 0)
        arrayRegFiles(__tmp_1648 + 1.U) := __tmp_1649(15, 8)
        arrayRegFiles(__tmp_1648 + 2.U) := __tmp_1649(23, 16)
        arrayRegFiles(__tmp_1648 + 3.U) := __tmp_1649(31, 24)
        arrayRegFiles(__tmp_1648 + 4.U) := __tmp_1649(39, 32)
        arrayRegFiles(__tmp_1648 + 5.U) := __tmp_1649(47, 40)
        arrayRegFiles(__tmp_1648 + 6.U) := __tmp_1649(55, 48)
        arrayRegFiles(__tmp_1648 + 7.U) := __tmp_1649(63, 56)

        val __tmp_1650 = SP - 32.U(16.W)
        val __tmp_1651 = (generalRegFiles(0.U)).asUInt
        arrayRegFiles(__tmp_1650 + 0.U) := __tmp_1651(7, 0)
        arrayRegFiles(__tmp_1650 + 1.U) := __tmp_1651(15, 8)
        arrayRegFiles(__tmp_1650 + 2.U) := __tmp_1651(23, 16)
        arrayRegFiles(__tmp_1650 + 3.U) := __tmp_1651(31, 24)
        arrayRegFiles(__tmp_1650 + 4.U) := __tmp_1651(39, 32)
        arrayRegFiles(__tmp_1650 + 5.U) := __tmp_1651(47, 40)
        arrayRegFiles(__tmp_1650 + 6.U) := __tmp_1651(55, 48)
        arrayRegFiles(__tmp_1650 + 7.U) := __tmp_1651(63, 56)

        val __tmp_1652 = SP - 24.U(16.W)
        val __tmp_1653 = (generalRegFiles(1.U)).asUInt
        arrayRegFiles(__tmp_1652 + 0.U) := __tmp_1653(7, 0)
        arrayRegFiles(__tmp_1652 + 1.U) := __tmp_1653(15, 8)
        arrayRegFiles(__tmp_1652 + 2.U) := __tmp_1653(23, 16)
        arrayRegFiles(__tmp_1652 + 3.U) := __tmp_1653(31, 24)
        arrayRegFiles(__tmp_1652 + 4.U) := __tmp_1653(39, 32)
        arrayRegFiles(__tmp_1652 + 5.U) := __tmp_1653(47, 40)
        arrayRegFiles(__tmp_1652 + 6.U) := __tmp_1653(55, 48)
        arrayRegFiles(__tmp_1652 + 7.U) := __tmp_1653(63, 56)

        val __tmp_1654 = SP - 16.U(16.W)
        val __tmp_1655 = (generalRegFiles(2.U)).asUInt
        arrayRegFiles(__tmp_1654 + 0.U) := __tmp_1655(7, 0)
        arrayRegFiles(__tmp_1654 + 1.U) := __tmp_1655(15, 8)
        arrayRegFiles(__tmp_1654 + 2.U) := __tmp_1655(23, 16)
        arrayRegFiles(__tmp_1654 + 3.U) := __tmp_1655(31, 24)
        arrayRegFiles(__tmp_1654 + 4.U) := __tmp_1655(39, 32)
        arrayRegFiles(__tmp_1654 + 5.U) := __tmp_1655(47, 40)
        arrayRegFiles(__tmp_1654 + 6.U) := __tmp_1655(55, 48)
        arrayRegFiles(__tmp_1654 + 7.U) := __tmp_1655(63, 56)

        val __tmp_1656 = SP - 8.U(16.W)
        val __tmp_1657 = (generalRegFiles(3.U)).asUInt
        arrayRegFiles(__tmp_1656 + 0.U) := __tmp_1657(7, 0)
        arrayRegFiles(__tmp_1656 + 1.U) := __tmp_1657(15, 8)
        arrayRegFiles(__tmp_1656 + 2.U) := __tmp_1657(23, 16)
        arrayRegFiles(__tmp_1656 + 3.U) := __tmp_1657(31, 24)
        arrayRegFiles(__tmp_1656 + 4.U) := __tmp_1657(39, 32)
        arrayRegFiles(__tmp_1656 + 5.U) := __tmp_1657(47, 40)
        arrayRegFiles(__tmp_1656 + 6.U) := __tmp_1657(55, 48)
        arrayRegFiles(__tmp_1656 + 7.U) := __tmp_1657(63, 56)

        CP := 182.U
      }

      is(182.U) {
        /*
        *(SP + (10 [SP])) = (3069765878 [U32]) [unsigned, U32, 4]  // $sfDesc.type = 0xB6F8E8F6
        *(SP + (14 [SP])) = (49 [Z]) [signed, Z, 8]  // $sfDesc.size = 49
        *(SP + (22 [SP])) = (111 [U8]) [unsigned, U8, 1]  // 'o'
        *(SP + (23 [SP])) = (114 [U8]) [unsigned, U8, 1]  // 'r'
        *(SP + (24 [SP])) = (103 [U8]) [unsigned, U8, 1]  // 'g'
        *(SP + (25 [SP])) = (46 [U8]) [unsigned, U8, 1]  // '.'
        *(SP + (26 [SP])) = (115 [U8]) [unsigned, U8, 1]  // 's'
        *(SP + (27 [SP])) = (105 [U8]) [unsigned, U8, 1]  // 'i'
        *(SP + (28 [SP])) = (114 [U8]) [unsigned, U8, 1]  // 'r'
        *(SP + (29 [SP])) = (101 [U8]) [unsigned, U8, 1]  // 'e'
        *(SP + (30 [SP])) = (117 [U8]) [unsigned, U8, 1]  // 'u'
        *(SP + (31 [SP])) = (109 [U8]) [unsigned, U8, 1]  // 'm'
        *(SP + (32 [SP])) = (46 [U8]) [unsigned, U8, 1]  // '.'
        *(SP + (33 [SP])) = (97 [U8]) [unsigned, U8, 1]  // 'a'
        *(SP + (34 [SP])) = (110 [U8]) [unsigned, U8, 1]  // 'n'
        *(SP + (35 [SP])) = (118 [U8]) [unsigned, U8, 1]  // 'v'
        *(SP + (36 [SP])) = (105 [U8]) [unsigned, U8, 1]  // 'i'
        *(SP + (37 [SP])) = (108 [U8]) [unsigned, U8, 1]  // 'l'
        *(SP + (38 [SP])) = (46 [U8]) [unsigned, U8, 1]  // '.'
        *(SP + (39 [SP])) = (82 [U8]) [unsigned, U8, 1]  // 'R'
        *(SP + (40 [SP])) = (117 [U8]) [unsigned, U8, 1]  // 'u'
        *(SP + (41 [SP])) = (110 [U8]) [unsigned, U8, 1]  // 'n'
        *(SP + (42 [SP])) = (116 [U8]) [unsigned, U8, 1]  // 't'
        *(SP + (43 [SP])) = (105 [U8]) [unsigned, U8, 1]  // 'i'
        *(SP + (44 [SP])) = (109 [U8]) [unsigned, U8, 1]  // 'm'
        *(SP + (45 [SP])) = (101 [U8]) [unsigned, U8, 1]  // 'e'
        *(SP + (46 [SP])) = (46 [U8]) [unsigned, U8, 1]  // '.'
        *(SP + (47 [SP])) = (112 [U8]) [unsigned, U8, 1]  // 'p'
        *(SP + (48 [SP])) = (114 [U8]) [unsigned, U8, 1]  // 'r'
        *(SP + (49 [SP])) = (105 [U8]) [unsigned, U8, 1]  // 'i'
        *(SP + (50 [SP])) = (110 [U8]) [unsigned, U8, 1]  // 'n'
        *(SP + (51 [SP])) = (116 [U8]) [unsigned, U8, 1]  // 't'
        *(SP + (52 [SP])) = (85 [U8]) [unsigned, U8, 1]  // 'U'
        *(SP + (53 [SP])) = (54 [U8]) [unsigned, U8, 1]  // '6'
        *(SP + (54 [SP])) = (52 [U8]) [unsigned, U8, 1]  // '4'
        *(SP + (55 [SP])) = (32 [U8]) [unsigned, U8, 1]  // ' '
        *(SP + (56 [SP])) = (40 [U8]) [unsigned, U8, 1]  // '('
        *(SP + (57 [SP])) = (82 [U8]) [unsigned, U8, 1]  // 'R'
        *(SP + (58 [SP])) = (117 [U8]) [unsigned, U8, 1]  // 'u'
        *(SP + (59 [SP])) = (110 [U8]) [unsigned, U8, 1]  // 'n'
        *(SP + (60 [SP])) = (116 [U8]) [unsigned, U8, 1]  // 't'
        *(SP + (61 [SP])) = (105 [U8]) [unsigned, U8, 1]  // 'i'
        *(SP + (62 [SP])) = (109 [U8]) [unsigned, U8, 1]  // 'm'
        *(SP + (63 [SP])) = (101 [U8]) [unsigned, U8, 1]  // 'e'
        *(SP + (64 [SP])) = (46 [U8]) [unsigned, U8, 1]  // '.'
        *(SP + (65 [SP])) = (115 [U8]) [unsigned, U8, 1]  // 's'
        *(SP + (66 [SP])) = (99 [U8]) [unsigned, U8, 1]  // 'c'
        *(SP + (67 [SP])) = (97 [U8]) [unsigned, U8, 1]  // 'a'
        *(SP + (68 [SP])) = (108 [U8]) [unsigned, U8, 1]  // 'l'
        *(SP + (69 [SP])) = (97 [U8]) [unsigned, U8, 1]  // 'a'
        *(SP + (70 [SP])) = (58 [U8]) [unsigned, U8, 1]  // ':'
        *(SP + (6 [SP])) = (206 [U32]) [signed, U32, 4]  // $sfLoc = (206 [U32])
        goto .183
        */


        val __tmp_1658 = SP + 10.U(16.W)
        val __tmp_1659 = (3069765878L.U(32.W)).asUInt
        arrayRegFiles(__tmp_1658 + 0.U) := __tmp_1659(7, 0)
        arrayRegFiles(__tmp_1658 + 1.U) := __tmp_1659(15, 8)
        arrayRegFiles(__tmp_1658 + 2.U) := __tmp_1659(23, 16)
        arrayRegFiles(__tmp_1658 + 3.U) := __tmp_1659(31, 24)

        val __tmp_1660 = SP + 14.U(16.W)
        val __tmp_1661 = (49.S(64.W)).asUInt
        arrayRegFiles(__tmp_1660 + 0.U) := __tmp_1661(7, 0)
        arrayRegFiles(__tmp_1660 + 1.U) := __tmp_1661(15, 8)
        arrayRegFiles(__tmp_1660 + 2.U) := __tmp_1661(23, 16)
        arrayRegFiles(__tmp_1660 + 3.U) := __tmp_1661(31, 24)
        arrayRegFiles(__tmp_1660 + 4.U) := __tmp_1661(39, 32)
        arrayRegFiles(__tmp_1660 + 5.U) := __tmp_1661(47, 40)
        arrayRegFiles(__tmp_1660 + 6.U) := __tmp_1661(55, 48)
        arrayRegFiles(__tmp_1660 + 7.U) := __tmp_1661(63, 56)

        val __tmp_1662 = SP + 22.U(16.W)
        val __tmp_1663 = (111.U(8.W)).asUInt
        arrayRegFiles(__tmp_1662 + 0.U) := __tmp_1663(7, 0)

        val __tmp_1664 = SP + 23.U(16.W)
        val __tmp_1665 = (114.U(8.W)).asUInt
        arrayRegFiles(__tmp_1664 + 0.U) := __tmp_1665(7, 0)

        val __tmp_1666 = SP + 24.U(16.W)
        val __tmp_1667 = (103.U(8.W)).asUInt
        arrayRegFiles(__tmp_1666 + 0.U) := __tmp_1667(7, 0)

        val __tmp_1668 = SP + 25.U(16.W)
        val __tmp_1669 = (46.U(8.W)).asUInt
        arrayRegFiles(__tmp_1668 + 0.U) := __tmp_1669(7, 0)

        val __tmp_1670 = SP + 26.U(16.W)
        val __tmp_1671 = (115.U(8.W)).asUInt
        arrayRegFiles(__tmp_1670 + 0.U) := __tmp_1671(7, 0)

        val __tmp_1672 = SP + 27.U(16.W)
        val __tmp_1673 = (105.U(8.W)).asUInt
        arrayRegFiles(__tmp_1672 + 0.U) := __tmp_1673(7, 0)

        val __tmp_1674 = SP + 28.U(16.W)
        val __tmp_1675 = (114.U(8.W)).asUInt
        arrayRegFiles(__tmp_1674 + 0.U) := __tmp_1675(7, 0)

        val __tmp_1676 = SP + 29.U(16.W)
        val __tmp_1677 = (101.U(8.W)).asUInt
        arrayRegFiles(__tmp_1676 + 0.U) := __tmp_1677(7, 0)

        val __tmp_1678 = SP + 30.U(16.W)
        val __tmp_1679 = (117.U(8.W)).asUInt
        arrayRegFiles(__tmp_1678 + 0.U) := __tmp_1679(7, 0)

        val __tmp_1680 = SP + 31.U(16.W)
        val __tmp_1681 = (109.U(8.W)).asUInt
        arrayRegFiles(__tmp_1680 + 0.U) := __tmp_1681(7, 0)

        val __tmp_1682 = SP + 32.U(16.W)
        val __tmp_1683 = (46.U(8.W)).asUInt
        arrayRegFiles(__tmp_1682 + 0.U) := __tmp_1683(7, 0)

        val __tmp_1684 = SP + 33.U(16.W)
        val __tmp_1685 = (97.U(8.W)).asUInt
        arrayRegFiles(__tmp_1684 + 0.U) := __tmp_1685(7, 0)

        val __tmp_1686 = SP + 34.U(16.W)
        val __tmp_1687 = (110.U(8.W)).asUInt
        arrayRegFiles(__tmp_1686 + 0.U) := __tmp_1687(7, 0)

        val __tmp_1688 = SP + 35.U(16.W)
        val __tmp_1689 = (118.U(8.W)).asUInt
        arrayRegFiles(__tmp_1688 + 0.U) := __tmp_1689(7, 0)

        val __tmp_1690 = SP + 36.U(16.W)
        val __tmp_1691 = (105.U(8.W)).asUInt
        arrayRegFiles(__tmp_1690 + 0.U) := __tmp_1691(7, 0)

        val __tmp_1692 = SP + 37.U(16.W)
        val __tmp_1693 = (108.U(8.W)).asUInt
        arrayRegFiles(__tmp_1692 + 0.U) := __tmp_1693(7, 0)

        val __tmp_1694 = SP + 38.U(16.W)
        val __tmp_1695 = (46.U(8.W)).asUInt
        arrayRegFiles(__tmp_1694 + 0.U) := __tmp_1695(7, 0)

        val __tmp_1696 = SP + 39.U(16.W)
        val __tmp_1697 = (82.U(8.W)).asUInt
        arrayRegFiles(__tmp_1696 + 0.U) := __tmp_1697(7, 0)

        val __tmp_1698 = SP + 40.U(16.W)
        val __tmp_1699 = (117.U(8.W)).asUInt
        arrayRegFiles(__tmp_1698 + 0.U) := __tmp_1699(7, 0)

        val __tmp_1700 = SP + 41.U(16.W)
        val __tmp_1701 = (110.U(8.W)).asUInt
        arrayRegFiles(__tmp_1700 + 0.U) := __tmp_1701(7, 0)

        val __tmp_1702 = SP + 42.U(16.W)
        val __tmp_1703 = (116.U(8.W)).asUInt
        arrayRegFiles(__tmp_1702 + 0.U) := __tmp_1703(7, 0)

        val __tmp_1704 = SP + 43.U(16.W)
        val __tmp_1705 = (105.U(8.W)).asUInt
        arrayRegFiles(__tmp_1704 + 0.U) := __tmp_1705(7, 0)

        val __tmp_1706 = SP + 44.U(16.W)
        val __tmp_1707 = (109.U(8.W)).asUInt
        arrayRegFiles(__tmp_1706 + 0.U) := __tmp_1707(7, 0)

        val __tmp_1708 = SP + 45.U(16.W)
        val __tmp_1709 = (101.U(8.W)).asUInt
        arrayRegFiles(__tmp_1708 + 0.U) := __tmp_1709(7, 0)

        val __tmp_1710 = SP + 46.U(16.W)
        val __tmp_1711 = (46.U(8.W)).asUInt
        arrayRegFiles(__tmp_1710 + 0.U) := __tmp_1711(7, 0)

        val __tmp_1712 = SP + 47.U(16.W)
        val __tmp_1713 = (112.U(8.W)).asUInt
        arrayRegFiles(__tmp_1712 + 0.U) := __tmp_1713(7, 0)

        val __tmp_1714 = SP + 48.U(16.W)
        val __tmp_1715 = (114.U(8.W)).asUInt
        arrayRegFiles(__tmp_1714 + 0.U) := __tmp_1715(7, 0)

        val __tmp_1716 = SP + 49.U(16.W)
        val __tmp_1717 = (105.U(8.W)).asUInt
        arrayRegFiles(__tmp_1716 + 0.U) := __tmp_1717(7, 0)

        val __tmp_1718 = SP + 50.U(16.W)
        val __tmp_1719 = (110.U(8.W)).asUInt
        arrayRegFiles(__tmp_1718 + 0.U) := __tmp_1719(7, 0)

        val __tmp_1720 = SP + 51.U(16.W)
        val __tmp_1721 = (116.U(8.W)).asUInt
        arrayRegFiles(__tmp_1720 + 0.U) := __tmp_1721(7, 0)

        val __tmp_1722 = SP + 52.U(16.W)
        val __tmp_1723 = (85.U(8.W)).asUInt
        arrayRegFiles(__tmp_1722 + 0.U) := __tmp_1723(7, 0)

        val __tmp_1724 = SP + 53.U(16.W)
        val __tmp_1725 = (54.U(8.W)).asUInt
        arrayRegFiles(__tmp_1724 + 0.U) := __tmp_1725(7, 0)

        val __tmp_1726 = SP + 54.U(16.W)
        val __tmp_1727 = (52.U(8.W)).asUInt
        arrayRegFiles(__tmp_1726 + 0.U) := __tmp_1727(7, 0)

        val __tmp_1728 = SP + 55.U(16.W)
        val __tmp_1729 = (32.U(8.W)).asUInt
        arrayRegFiles(__tmp_1728 + 0.U) := __tmp_1729(7, 0)

        val __tmp_1730 = SP + 56.U(16.W)
        val __tmp_1731 = (40.U(8.W)).asUInt
        arrayRegFiles(__tmp_1730 + 0.U) := __tmp_1731(7, 0)

        val __tmp_1732 = SP + 57.U(16.W)
        val __tmp_1733 = (82.U(8.W)).asUInt
        arrayRegFiles(__tmp_1732 + 0.U) := __tmp_1733(7, 0)

        val __tmp_1734 = SP + 58.U(16.W)
        val __tmp_1735 = (117.U(8.W)).asUInt
        arrayRegFiles(__tmp_1734 + 0.U) := __tmp_1735(7, 0)

        val __tmp_1736 = SP + 59.U(16.W)
        val __tmp_1737 = (110.U(8.W)).asUInt
        arrayRegFiles(__tmp_1736 + 0.U) := __tmp_1737(7, 0)

        val __tmp_1738 = SP + 60.U(16.W)
        val __tmp_1739 = (116.U(8.W)).asUInt
        arrayRegFiles(__tmp_1738 + 0.U) := __tmp_1739(7, 0)

        val __tmp_1740 = SP + 61.U(16.W)
        val __tmp_1741 = (105.U(8.W)).asUInt
        arrayRegFiles(__tmp_1740 + 0.U) := __tmp_1741(7, 0)

        val __tmp_1742 = SP + 62.U(16.W)
        val __tmp_1743 = (109.U(8.W)).asUInt
        arrayRegFiles(__tmp_1742 + 0.U) := __tmp_1743(7, 0)

        val __tmp_1744 = SP + 63.U(16.W)
        val __tmp_1745 = (101.U(8.W)).asUInt
        arrayRegFiles(__tmp_1744 + 0.U) := __tmp_1745(7, 0)

        val __tmp_1746 = SP + 64.U(16.W)
        val __tmp_1747 = (46.U(8.W)).asUInt
        arrayRegFiles(__tmp_1746 + 0.U) := __tmp_1747(7, 0)

        val __tmp_1748 = SP + 65.U(16.W)
        val __tmp_1749 = (115.U(8.W)).asUInt
        arrayRegFiles(__tmp_1748 + 0.U) := __tmp_1749(7, 0)

        val __tmp_1750 = SP + 66.U(16.W)
        val __tmp_1751 = (99.U(8.W)).asUInt
        arrayRegFiles(__tmp_1750 + 0.U) := __tmp_1751(7, 0)

        val __tmp_1752 = SP + 67.U(16.W)
        val __tmp_1753 = (97.U(8.W)).asUInt
        arrayRegFiles(__tmp_1752 + 0.U) := __tmp_1753(7, 0)

        val __tmp_1754 = SP + 68.U(16.W)
        val __tmp_1755 = (108.U(8.W)).asUInt
        arrayRegFiles(__tmp_1754 + 0.U) := __tmp_1755(7, 0)

        val __tmp_1756 = SP + 69.U(16.W)
        val __tmp_1757 = (97.U(8.W)).asUInt
        arrayRegFiles(__tmp_1756 + 0.U) := __tmp_1757(7, 0)

        val __tmp_1758 = SP + 70.U(16.W)
        val __tmp_1759 = (58.U(8.W)).asUInt
        arrayRegFiles(__tmp_1758 + 0.U) := __tmp_1759(7, 0)

        val __tmp_1760 = SP + 6.U(16.W)
        val __tmp_1761 = (206.S(32.W)).asUInt
        arrayRegFiles(__tmp_1760 + 0.U) := __tmp_1761(7, 0)
        arrayRegFiles(__tmp_1760 + 1.U) := __tmp_1761(15, 8)
        arrayRegFiles(__tmp_1760 + 2.U) := __tmp_1761(23, 16)
        arrayRegFiles(__tmp_1760 + 3.U) := __tmp_1761(31, 24)

        CP := 183.U
      }

      is(183.U) {
        /*
        $0 = *(SP + (91 [SP])) [unsigned, U64, 8]  // $0 = n
        goto .184
        */


        val __tmp_1762 = (SP + 91.U(16.W)).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_1762 + 7.U),
          arrayRegFiles(__tmp_1762 + 6.U),
          arrayRegFiles(__tmp_1762 + 5.U),
          arrayRegFiles(__tmp_1762 + 4.U),
          arrayRegFiles(__tmp_1762 + 3.U),
          arrayRegFiles(__tmp_1762 + 2.U),
          arrayRegFiles(__tmp_1762 + 1.U),
          arrayRegFiles(__tmp_1762 + 0.U)
        ).asUInt

        CP := 184.U
      }

      is(184.U) {
        /*
        $1 = ($0 ≡ (0 [U64]))
        goto .185
        */


        generalRegFiles(1.U) := (generalRegFiles(0.U) === 0.U(64.W)).asUInt
        CP := 185.U
      }

      is(185.U) {
        /*
        if $1 goto .186 else goto .192
        */


        CP := Mux((generalRegFiles(1.U).asUInt) === 1.U, 186.U, 192.U)
      }

      is(186.U) {
        /*
        *(SP + (6 [SP])) = (207 [U32]) [signed, U32, 4]  // $sfLoc = (207 [U32])
        goto .187
        */


        val __tmp_1763 = SP + 6.U(16.W)
        val __tmp_1764 = (207.S(32.W)).asUInt
        arrayRegFiles(__tmp_1763 + 0.U) := __tmp_1764(7, 0)
        arrayRegFiles(__tmp_1763 + 1.U) := __tmp_1764(15, 8)
        arrayRegFiles(__tmp_1763 + 2.U) := __tmp_1764(23, 16)
        arrayRegFiles(__tmp_1763 + 3.U) := __tmp_1764(31, 24)

        CP := 187.U
      }

      is(187.U) {
        /*
        $0 = *(SP + (73 [SP])) [unsigned, MS[anvil.PrinterIndex.U, U8], 2]  // $0 = buffer
        $1 = *(SP + (75 [SP])) [unsigned, anvil.PrinterIndex.U, 8]  // $1 = index
        $2 = *(SP + (83 [SP])) [unsigned, anvil.PrinterIndex.U, 8]  // $2 = mask
        goto .188
        */


        val __tmp_1765 = (SP + 73.U(16.W)).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_1765 + 1.U),
          arrayRegFiles(__tmp_1765 + 0.U)
        ).asUInt

        val __tmp_1766 = (SP + 75.U(16.W)).asUInt
        generalRegFiles(1.U) := Cat(
          arrayRegFiles(__tmp_1766 + 7.U),
          arrayRegFiles(__tmp_1766 + 6.U),
          arrayRegFiles(__tmp_1766 + 5.U),
          arrayRegFiles(__tmp_1766 + 4.U),
          arrayRegFiles(__tmp_1766 + 3.U),
          arrayRegFiles(__tmp_1766 + 2.U),
          arrayRegFiles(__tmp_1766 + 1.U),
          arrayRegFiles(__tmp_1766 + 0.U)
        ).asUInt

        val __tmp_1767 = (SP + 83.U(16.W)).asUInt
        generalRegFiles(2.U) := Cat(
          arrayRegFiles(__tmp_1767 + 7.U),
          arrayRegFiles(__tmp_1767 + 6.U),
          arrayRegFiles(__tmp_1767 + 5.U),
          arrayRegFiles(__tmp_1767 + 4.U),
          arrayRegFiles(__tmp_1767 + 3.U),
          arrayRegFiles(__tmp_1767 + 2.U),
          arrayRegFiles(__tmp_1767 + 1.U),
          arrayRegFiles(__tmp_1767 + 0.U)
        ).asUInt

        CP := 188.U
      }

      is(188.U) {
        /*
        $3 = ($1 & $2)
        goto .189
        */


        generalRegFiles(3.U) := generalRegFiles(1.U) & generalRegFiles(2.U)
        CP := 189.U
      }

      is(189.U) {
        /*
        *(($0 + (12 [SP])) + ($3 as SP)) = (48 [U8]) [unsigned, U8, 1]  // $0($3) = (48 [U8])
        goto .190
        */


        val __tmp_1768 = generalRegFiles(0.U) + 12.U(16.W) + generalRegFiles(3.U).asUInt
        val __tmp_1769 = (48.U(8.W)).asUInt
        arrayRegFiles(__tmp_1768 + 0.U) := __tmp_1769(7, 0)

        CP := 190.U
      }

      is(190.U) {
        /*
        *(SP + (6 [SP])) = (205 [U32]) [signed, U32, 4]  // $sfLoc = (205 [U32])
        goto .191
        */


        val __tmp_1770 = SP + 6.U(16.W)
        val __tmp_1771 = (205.S(32.W)).asUInt
        arrayRegFiles(__tmp_1770 + 0.U) := __tmp_1771(7, 0)
        arrayRegFiles(__tmp_1770 + 1.U) := __tmp_1771(15, 8)
        arrayRegFiles(__tmp_1770 + 2.U) := __tmp_1771(23, 16)
        arrayRegFiles(__tmp_1770 + 3.U) := __tmp_1771(31, 24)

        CP := 191.U
      }

      is(191.U) {
        /*
        **(SP + (2 [SP])) = (1 [U64]) [unsigned, U64, 8]  // $res = (1 [U64])
        goto $ret@0
        */


        val __tmp_1772 = Cat(
          arrayRegFiles(SP + 2.U(16.W) + 1.U),
          arrayRegFiles(SP + 2.U(16.W) + 0.U)
        )
        val __tmp_1773 = (1.U(64.W)).asUInt
        arrayRegFiles(__tmp_1772 + 0.U) := __tmp_1773(7, 0)
        arrayRegFiles(__tmp_1772 + 1.U) := __tmp_1773(15, 8)
        arrayRegFiles(__tmp_1772 + 2.U) := __tmp_1773(23, 16)
        arrayRegFiles(__tmp_1772 + 3.U) := __tmp_1773(31, 24)
        arrayRegFiles(__tmp_1772 + 4.U) := __tmp_1773(39, 32)
        arrayRegFiles(__tmp_1772 + 5.U) := __tmp_1773(47, 40)
        arrayRegFiles(__tmp_1772 + 6.U) := __tmp_1773(55, 48)
        arrayRegFiles(__tmp_1772 + 7.U) := __tmp_1773(63, 56)

        CP := Cat(
          arrayRegFiles(SP + 0.U + 1.U),
          arrayRegFiles(SP + 0.U + 0.U)
        )

      }

      is(192.U) {
        /*
        *(SP + (6 [SP])) = (210 [U32]) [signed, U32, 4]  // $sfLoc = (210 [U32])
        goto .193
        */


        val __tmp_1774 = SP + 6.U(16.W)
        val __tmp_1775 = (210.S(32.W)).asUInt
        arrayRegFiles(__tmp_1774 + 0.U) := __tmp_1775(7, 0)
        arrayRegFiles(__tmp_1774 + 1.U) := __tmp_1775(15, 8)
        arrayRegFiles(__tmp_1774 + 2.U) := __tmp_1775(23, 16)
        arrayRegFiles(__tmp_1774 + 3.U) := __tmp_1775(31, 24)

        CP := 193.U
      }

      is(193.U) {
        /*
        decl buff: MS[anvil.PrinterIndex.I20, U8] [@99, 36]
        *(SP + (99 [SP])) = (SP + (101 [SP])) [unsigned, SP, 2]  // address of buff
        alloc $new@[210,16].D266207C: MS[anvil.PrinterIndex.I20, U8] [@135, 34]
        $0 = (SP + (135 [SP]))
        *(SP + (135 [SP])) = (323602724 [U32]) [unsigned, U32, 4]  // sha3 type signature of MS[anvil.PrinterIndex.I20, U8]: 0x1349C924
        *(SP + (139 [SP])) = (20 [Z]) [signed, Z, 8]  // size of MS[anvil.PrinterIndex.I20, U8]((0 [U8]), (0 [U8]), (0 [U8]), (0 [U8]), (0 [U8]), (0 [U8]), (0 [U8]), (0 [U8]), (0 [U8]), (0 [U8]), (0 [U8]), (0 [U8]), (0 [U8]), (0 [U8]), (0 [U8]), (0 [U8]), (0 [U8]), (0 [U8]), (0 [U8]), (0 [U8]))
        goto .194
        */


        val __tmp_1776 = SP + 99.U(16.W)
        val __tmp_1777 = (SP + 101.U(16.W)).asUInt
        arrayRegFiles(__tmp_1776 + 0.U) := __tmp_1777(7, 0)
        arrayRegFiles(__tmp_1776 + 1.U) := __tmp_1777(15, 8)

        generalRegFiles(0.U) := SP + 135.U(16.W)
        val __tmp_1778 = SP + 135.U(16.W)
        val __tmp_1779 = (323602724.U(32.W)).asUInt
        arrayRegFiles(__tmp_1778 + 0.U) := __tmp_1779(7, 0)
        arrayRegFiles(__tmp_1778 + 1.U) := __tmp_1779(15, 8)
        arrayRegFiles(__tmp_1778 + 2.U) := __tmp_1779(23, 16)
        arrayRegFiles(__tmp_1778 + 3.U) := __tmp_1779(31, 24)

        val __tmp_1780 = SP + 139.U(16.W)
        val __tmp_1781 = (20.S(64.W)).asUInt
        arrayRegFiles(__tmp_1780 + 0.U) := __tmp_1781(7, 0)
        arrayRegFiles(__tmp_1780 + 1.U) := __tmp_1781(15, 8)
        arrayRegFiles(__tmp_1780 + 2.U) := __tmp_1781(23, 16)
        arrayRegFiles(__tmp_1780 + 3.U) := __tmp_1781(31, 24)
        arrayRegFiles(__tmp_1780 + 4.U) := __tmp_1781(39, 32)
        arrayRegFiles(__tmp_1780 + 5.U) := __tmp_1781(47, 40)
        arrayRegFiles(__tmp_1780 + 6.U) := __tmp_1781(55, 48)
        arrayRegFiles(__tmp_1780 + 7.U) := __tmp_1781(63, 56)

        CP := 194.U
      }

      is(194.U) {
        /*
        *(($0 + (12 [SP])) + ((0 [anvil.PrinterIndex.I20]) as SP)) = (0 [U8]) [unsigned, U8, 1]  // $0((0 [anvil.PrinterIndex.I20])) = (0 [U8])
        *(($0 + (12 [SP])) + ((1 [anvil.PrinterIndex.I20]) as SP)) = (0 [U8]) [unsigned, U8, 1]  // $0((1 [anvil.PrinterIndex.I20])) = (0 [U8])
        *(($0 + (12 [SP])) + ((2 [anvil.PrinterIndex.I20]) as SP)) = (0 [U8]) [unsigned, U8, 1]  // $0((2 [anvil.PrinterIndex.I20])) = (0 [U8])
        *(($0 + (12 [SP])) + ((3 [anvil.PrinterIndex.I20]) as SP)) = (0 [U8]) [unsigned, U8, 1]  // $0((3 [anvil.PrinterIndex.I20])) = (0 [U8])
        *(($0 + (12 [SP])) + ((4 [anvil.PrinterIndex.I20]) as SP)) = (0 [U8]) [unsigned, U8, 1]  // $0((4 [anvil.PrinterIndex.I20])) = (0 [U8])
        *(($0 + (12 [SP])) + ((5 [anvil.PrinterIndex.I20]) as SP)) = (0 [U8]) [unsigned, U8, 1]  // $0((5 [anvil.PrinterIndex.I20])) = (0 [U8])
        *(($0 + (12 [SP])) + ((6 [anvil.PrinterIndex.I20]) as SP)) = (0 [U8]) [unsigned, U8, 1]  // $0((6 [anvil.PrinterIndex.I20])) = (0 [U8])
        *(($0 + (12 [SP])) + ((7 [anvil.PrinterIndex.I20]) as SP)) = (0 [U8]) [unsigned, U8, 1]  // $0((7 [anvil.PrinterIndex.I20])) = (0 [U8])
        *(($0 + (12 [SP])) + ((8 [anvil.PrinterIndex.I20]) as SP)) = (0 [U8]) [unsigned, U8, 1]  // $0((8 [anvil.PrinterIndex.I20])) = (0 [U8])
        *(($0 + (12 [SP])) + ((9 [anvil.PrinterIndex.I20]) as SP)) = (0 [U8]) [unsigned, U8, 1]  // $0((9 [anvil.PrinterIndex.I20])) = (0 [U8])
        *(($0 + (12 [SP])) + ((10 [anvil.PrinterIndex.I20]) as SP)) = (0 [U8]) [unsigned, U8, 1]  // $0((10 [anvil.PrinterIndex.I20])) = (0 [U8])
        *(($0 + (12 [SP])) + ((11 [anvil.PrinterIndex.I20]) as SP)) = (0 [U8]) [unsigned, U8, 1]  // $0((11 [anvil.PrinterIndex.I20])) = (0 [U8])
        *(($0 + (12 [SP])) + ((12 [anvil.PrinterIndex.I20]) as SP)) = (0 [U8]) [unsigned, U8, 1]  // $0((12 [anvil.PrinterIndex.I20])) = (0 [U8])
        *(($0 + (12 [SP])) + ((13 [anvil.PrinterIndex.I20]) as SP)) = (0 [U8]) [unsigned, U8, 1]  // $0((13 [anvil.PrinterIndex.I20])) = (0 [U8])
        *(($0 + (12 [SP])) + ((14 [anvil.PrinterIndex.I20]) as SP)) = (0 [U8]) [unsigned, U8, 1]  // $0((14 [anvil.PrinterIndex.I20])) = (0 [U8])
        *(($0 + (12 [SP])) + ((15 [anvil.PrinterIndex.I20]) as SP)) = (0 [U8]) [unsigned, U8, 1]  // $0((15 [anvil.PrinterIndex.I20])) = (0 [U8])
        *(($0 + (12 [SP])) + ((16 [anvil.PrinterIndex.I20]) as SP)) = (0 [U8]) [unsigned, U8, 1]  // $0((16 [anvil.PrinterIndex.I20])) = (0 [U8])
        *(($0 + (12 [SP])) + ((17 [anvil.PrinterIndex.I20]) as SP)) = (0 [U8]) [unsigned, U8, 1]  // $0((17 [anvil.PrinterIndex.I20])) = (0 [U8])
        *(($0 + (12 [SP])) + ((18 [anvil.PrinterIndex.I20]) as SP)) = (0 [U8]) [unsigned, U8, 1]  // $0((18 [anvil.PrinterIndex.I20])) = (0 [U8])
        *(($0 + (12 [SP])) + ((19 [anvil.PrinterIndex.I20]) as SP)) = (0 [U8]) [unsigned, U8, 1]  // $0((19 [anvil.PrinterIndex.I20])) = (0 [U8])
        goto .195
        */


        val __tmp_1782 = generalRegFiles(0.U) + 12.U(16.W) + 0.S(8.W).asUInt
        val __tmp_1783 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_1782 + 0.U) := __tmp_1783(7, 0)

        val __tmp_1784 = generalRegFiles(0.U) + 12.U(16.W) + 1.S(8.W).asUInt
        val __tmp_1785 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_1784 + 0.U) := __tmp_1785(7, 0)

        val __tmp_1786 = generalRegFiles(0.U) + 12.U(16.W) + 2.S(8.W).asUInt
        val __tmp_1787 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_1786 + 0.U) := __tmp_1787(7, 0)

        val __tmp_1788 = generalRegFiles(0.U) + 12.U(16.W) + 3.S(8.W).asUInt
        val __tmp_1789 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_1788 + 0.U) := __tmp_1789(7, 0)

        val __tmp_1790 = generalRegFiles(0.U) + 12.U(16.W) + 4.S(8.W).asUInt
        val __tmp_1791 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_1790 + 0.U) := __tmp_1791(7, 0)

        val __tmp_1792 = generalRegFiles(0.U) + 12.U(16.W) + 5.S(8.W).asUInt
        val __tmp_1793 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_1792 + 0.U) := __tmp_1793(7, 0)

        val __tmp_1794 = generalRegFiles(0.U) + 12.U(16.W) + 6.S(8.W).asUInt
        val __tmp_1795 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_1794 + 0.U) := __tmp_1795(7, 0)

        val __tmp_1796 = generalRegFiles(0.U) + 12.U(16.W) + 7.S(8.W).asUInt
        val __tmp_1797 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_1796 + 0.U) := __tmp_1797(7, 0)

        val __tmp_1798 = generalRegFiles(0.U) + 12.U(16.W) + 8.S(8.W).asUInt
        val __tmp_1799 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_1798 + 0.U) := __tmp_1799(7, 0)

        val __tmp_1800 = generalRegFiles(0.U) + 12.U(16.W) + 9.S(8.W).asUInt
        val __tmp_1801 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_1800 + 0.U) := __tmp_1801(7, 0)

        val __tmp_1802 = generalRegFiles(0.U) + 12.U(16.W) + 10.S(8.W).asUInt
        val __tmp_1803 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_1802 + 0.U) := __tmp_1803(7, 0)

        val __tmp_1804 = generalRegFiles(0.U) + 12.U(16.W) + 11.S(8.W).asUInt
        val __tmp_1805 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_1804 + 0.U) := __tmp_1805(7, 0)

        val __tmp_1806 = generalRegFiles(0.U) + 12.U(16.W) + 12.S(8.W).asUInt
        val __tmp_1807 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_1806 + 0.U) := __tmp_1807(7, 0)

        val __tmp_1808 = generalRegFiles(0.U) + 12.U(16.W) + 13.S(8.W).asUInt
        val __tmp_1809 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_1808 + 0.U) := __tmp_1809(7, 0)

        val __tmp_1810 = generalRegFiles(0.U) + 12.U(16.W) + 14.S(8.W).asUInt
        val __tmp_1811 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_1810 + 0.U) := __tmp_1811(7, 0)

        val __tmp_1812 = generalRegFiles(0.U) + 12.U(16.W) + 15.S(8.W).asUInt
        val __tmp_1813 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_1812 + 0.U) := __tmp_1813(7, 0)

        val __tmp_1814 = generalRegFiles(0.U) + 12.U(16.W) + 16.S(8.W).asUInt
        val __tmp_1815 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_1814 + 0.U) := __tmp_1815(7, 0)

        val __tmp_1816 = generalRegFiles(0.U) + 12.U(16.W) + 17.S(8.W).asUInt
        val __tmp_1817 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_1816 + 0.U) := __tmp_1817(7, 0)

        val __tmp_1818 = generalRegFiles(0.U) + 12.U(16.W) + 18.S(8.W).asUInt
        val __tmp_1819 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_1818 + 0.U) := __tmp_1819(7, 0)

        val __tmp_1820 = generalRegFiles(0.U) + 12.U(16.W) + 19.S(8.W).asUInt
        val __tmp_1821 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_1820 + 0.U) := __tmp_1821(7, 0)

        CP := 195.U
      }

      is(195.U) {
        /*
        *(SP + (99 [SP])) [MS[anvil.PrinterIndex.I20, U8], 34]  <-  $0 [MS[anvil.PrinterIndex.I20, U8], ((*($0 + (4 [SP])) as SP) + (12 [SP]))]  // buff = $0
        goto .196
        */


        val __tmp_1822 = Cat(
          arrayRegFiles(SP + 99.U(16.W) + 1.U),
          arrayRegFiles(SP + 99.U(16.W) + 0.U)
        )
        val __tmp_1823 = generalRegFiles(0.U)
        val __tmp_1824 = Cat(
          arrayRegFiles(generalRegFiles(0.U) + 4.U(16.W) + 7.U),
          arrayRegFiles(generalRegFiles(0.U) + 4.U(16.W) + 6.U),
          arrayRegFiles(generalRegFiles(0.U) + 4.U(16.W) + 5.U),
          arrayRegFiles(generalRegFiles(0.U) + 4.U(16.W) + 4.U),
          arrayRegFiles(generalRegFiles(0.U) + 4.U(16.W) + 3.U),
          arrayRegFiles(generalRegFiles(0.U) + 4.U(16.W) + 2.U),
          arrayRegFiles(generalRegFiles(0.U) + 4.U(16.W) + 1.U),
          arrayRegFiles(generalRegFiles(0.U) + 4.U(16.W) + 0.U)
        ).asSInt.asUInt + 12.U(16.W)

        when(Idx < __tmp_1824) {
          arrayRegFiles(__tmp_1822 + Idx + 0.U) := arrayRegFiles(__tmp_1823 + Idx + 0.U)
          arrayRegFiles(__tmp_1822 + Idx + 1.U) := arrayRegFiles(__tmp_1823 + Idx + 1.U)
          arrayRegFiles(__tmp_1822 + Idx + 2.U) := arrayRegFiles(__tmp_1823 + Idx + 2.U)
          arrayRegFiles(__tmp_1822 + Idx + 3.U) := arrayRegFiles(__tmp_1823 + Idx + 3.U)
          arrayRegFiles(__tmp_1822 + Idx + 4.U) := arrayRegFiles(__tmp_1823 + Idx + 4.U)
          arrayRegFiles(__tmp_1822 + Idx + 5.U) := arrayRegFiles(__tmp_1823 + Idx + 5.U)
          arrayRegFiles(__tmp_1822 + Idx + 6.U) := arrayRegFiles(__tmp_1823 + Idx + 6.U)
          arrayRegFiles(__tmp_1822 + Idx + 7.U) := arrayRegFiles(__tmp_1823 + Idx + 7.U)
          Idx := Idx + 8.U
          LeftByteRounds := __tmp_1824 - Idx
        } .elsewhen(IdxLeftByteRounds < LeftByteRounds) {
          val __tmp_1825 = Idx - 8.U
          arrayRegFiles(__tmp_1822 + __tmp_1825 + IdxLeftByteRounds) := arrayRegFiles(__tmp_1823 + __tmp_1825 + IdxLeftByteRounds)
          IdxLeftByteRounds := IdxLeftByteRounds + 1.U
        } .otherwise {
          Idx := 0.U
          IdxLeftByteRounds := 0.U
          LeftByteRounds := 0.U
          CP := 196.U
        }


      }

      is(196.U) {
        /*
        unalloc $new@[210,16].D266207C: MS[anvil.PrinterIndex.I20, U8] [@135, 34]
        goto .197
        */


        CP := 197.U
      }

      is(197.U) {
        /*
        *(SP + (6 [SP])) = (213 [U32]) [signed, U32, 4]  // $sfLoc = (213 [U32])
        goto .198
        */


        val __tmp_1826 = SP + 6.U(16.W)
        val __tmp_1827 = (213.S(32.W)).asUInt
        arrayRegFiles(__tmp_1826 + 0.U) := __tmp_1827(7, 0)
        arrayRegFiles(__tmp_1826 + 1.U) := __tmp_1827(15, 8)
        arrayRegFiles(__tmp_1826 + 2.U) := __tmp_1827(23, 16)
        arrayRegFiles(__tmp_1826 + 3.U) := __tmp_1827(31, 24)

        CP := 198.U
      }

      is(198.U) {
        /*
        decl i: anvil.PrinterIndex.I20 [@135, 1]
        *(SP + (135 [SP])) = (0 [anvil.PrinterIndex.I20]) [signed, anvil.PrinterIndex.I20, 1]  // i = (0 [anvil.PrinterIndex.I20])
        goto .199
        */


        val __tmp_1828 = SP + 135.U(16.W)
        val __tmp_1829 = (0.S(8.W)).asUInt
        arrayRegFiles(__tmp_1828 + 0.U) := __tmp_1829(7, 0)

        CP := 199.U
      }

      is(199.U) {
        /*
        *(SP + (6 [SP])) = (214 [U32]) [signed, U32, 4]  // $sfLoc = (214 [U32])
        goto .200
        */


        val __tmp_1830 = SP + 6.U(16.W)
        val __tmp_1831 = (214.S(32.W)).asUInt
        arrayRegFiles(__tmp_1830 + 0.U) := __tmp_1831(7, 0)
        arrayRegFiles(__tmp_1830 + 1.U) := __tmp_1831(15, 8)
        arrayRegFiles(__tmp_1830 + 2.U) := __tmp_1831(23, 16)
        arrayRegFiles(__tmp_1830 + 3.U) := __tmp_1831(31, 24)

        CP := 200.U
      }

      is(200.U) {
        /*
        decl m: U64 [@136, 8]
        $0 = *(SP + (91 [SP])) [unsigned, U64, 8]  // $0 = n
        goto .201
        */


        val __tmp_1832 = (SP + 91.U(16.W)).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_1832 + 7.U),
          arrayRegFiles(__tmp_1832 + 6.U),
          arrayRegFiles(__tmp_1832 + 5.U),
          arrayRegFiles(__tmp_1832 + 4.U),
          arrayRegFiles(__tmp_1832 + 3.U),
          arrayRegFiles(__tmp_1832 + 2.U),
          arrayRegFiles(__tmp_1832 + 1.U),
          arrayRegFiles(__tmp_1832 + 0.U)
        ).asUInt

        CP := 201.U
      }

      is(201.U) {
        /*
        *(SP + (136 [SP])) = $0 [signed, U64, 8]  // m = $0
        goto .202
        */


        val __tmp_1833 = SP + 136.U(16.W)
        val __tmp_1834 = (generalRegFiles(0.U)).asUInt
        arrayRegFiles(__tmp_1833 + 0.U) := __tmp_1834(7, 0)
        arrayRegFiles(__tmp_1833 + 1.U) := __tmp_1834(15, 8)
        arrayRegFiles(__tmp_1833 + 2.U) := __tmp_1834(23, 16)
        arrayRegFiles(__tmp_1833 + 3.U) := __tmp_1834(31, 24)
        arrayRegFiles(__tmp_1833 + 4.U) := __tmp_1834(39, 32)
        arrayRegFiles(__tmp_1833 + 5.U) := __tmp_1834(47, 40)
        arrayRegFiles(__tmp_1833 + 6.U) := __tmp_1834(55, 48)
        arrayRegFiles(__tmp_1833 + 7.U) := __tmp_1834(63, 56)

        CP := 202.U
      }

      is(202.U) {
        /*
        *(SP + (6 [SP])) = (215 [U32]) [signed, U32, 4]  // $sfLoc = (215 [U32])
        goto .203
        */


        val __tmp_1835 = SP + 6.U(16.W)
        val __tmp_1836 = (215.S(32.W)).asUInt
        arrayRegFiles(__tmp_1835 + 0.U) := __tmp_1836(7, 0)
        arrayRegFiles(__tmp_1835 + 1.U) := __tmp_1836(15, 8)
        arrayRegFiles(__tmp_1835 + 2.U) := __tmp_1836(23, 16)
        arrayRegFiles(__tmp_1835 + 3.U) := __tmp_1836(31, 24)

        CP := 203.U
      }

      is(203.U) {
        /*
        *(SP + (6 [SP])) = (215 [U32]) [signed, U32, 4]  // $sfLoc = (215 [U32])
        goto .204
        */


        val __tmp_1837 = SP + 6.U(16.W)
        val __tmp_1838 = (215.S(32.W)).asUInt
        arrayRegFiles(__tmp_1837 + 0.U) := __tmp_1838(7, 0)
        arrayRegFiles(__tmp_1837 + 1.U) := __tmp_1838(15, 8)
        arrayRegFiles(__tmp_1837 + 2.U) := __tmp_1838(23, 16)
        arrayRegFiles(__tmp_1837 + 3.U) := __tmp_1838(31, 24)

        CP := 204.U
      }

      is(204.U) {
        /*
        $0 = *(SP + (136 [SP])) [unsigned, U64, 8]  // $0 = m
        goto .205
        */


        val __tmp_1839 = (SP + 136.U(16.W)).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_1839 + 7.U),
          arrayRegFiles(__tmp_1839 + 6.U),
          arrayRegFiles(__tmp_1839 + 5.U),
          arrayRegFiles(__tmp_1839 + 4.U),
          arrayRegFiles(__tmp_1839 + 3.U),
          arrayRegFiles(__tmp_1839 + 2.U),
          arrayRegFiles(__tmp_1839 + 1.U),
          arrayRegFiles(__tmp_1839 + 0.U)
        ).asUInt

        CP := 205.U
      }

      is(205.U) {
        /*
        $1 = ($0 > (0 [U64]))
        goto .206
        */


        generalRegFiles(1.U) := (generalRegFiles(0.U) > 0.U(64.W)).asUInt
        CP := 206.U
      }

      is(206.U) {
        /*
        if $1 goto .207 else goto .264
        */


        CP := Mux((generalRegFiles(1.U).asUInt) === 1.U, 207.U, 264.U)
      }

      is(207.U) {
        /*
        *(SP + (6 [SP])) = (216 [U32]) [signed, U32, 4]  // $sfLoc = (216 [U32])
        goto .208
        */


        val __tmp_1840 = SP + 6.U(16.W)
        val __tmp_1841 = (216.S(32.W)).asUInt
        arrayRegFiles(__tmp_1840 + 0.U) := __tmp_1841(7, 0)
        arrayRegFiles(__tmp_1840 + 1.U) := __tmp_1841(15, 8)
        arrayRegFiles(__tmp_1840 + 2.U) := __tmp_1841(23, 16)
        arrayRegFiles(__tmp_1840 + 3.U) := __tmp_1841(31, 24)

        CP := 208.U
      }

      is(208.U) {
        /*
        $0 = *(SP + (136 [SP])) [unsigned, U64, 8]  // $0 = m
        goto .209
        */


        val __tmp_1842 = (SP + 136.U(16.W)).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_1842 + 7.U),
          arrayRegFiles(__tmp_1842 + 6.U),
          arrayRegFiles(__tmp_1842 + 5.U),
          arrayRegFiles(__tmp_1842 + 4.U),
          arrayRegFiles(__tmp_1842 + 3.U),
          arrayRegFiles(__tmp_1842 + 2.U),
          arrayRegFiles(__tmp_1842 + 1.U),
          arrayRegFiles(__tmp_1842 + 0.U)
        ).asUInt

        CP := 209.U
      }

      is(209.U) {
        /*
        *(SP + (6 [SP])) = (216 [U32]) [signed, U32, 4]  // $sfLoc = (216 [U32])
        goto .210
        */


        val __tmp_1843 = SP + 6.U(16.W)
        val __tmp_1844 = (216.S(32.W)).asUInt
        arrayRegFiles(__tmp_1843 + 0.U) := __tmp_1844(7, 0)
        arrayRegFiles(__tmp_1843 + 1.U) := __tmp_1844(15, 8)
        arrayRegFiles(__tmp_1843 + 2.U) := __tmp_1844(23, 16)
        arrayRegFiles(__tmp_1843 + 3.U) := __tmp_1844(31, 24)

        CP := 210.U
      }

      is(210.U) {
        /*
        $1 = ($0 % (10 [U64]))
        goto .211
        */


        generalRegFiles(1.U) := generalRegFiles(0.U) % 10.U(64.W)
        CP := 211.U
      }

      is(211.U) {
        /*
        *(SP + (6 [SP])) = (205 [U32]) [signed, U32, 4]  // $sfLoc = (205 [U32])
        goto .212
        */


        val __tmp_1845 = SP + 6.U(16.W)
        val __tmp_1846 = (205.S(32.W)).asUInt
        arrayRegFiles(__tmp_1845 + 0.U) := __tmp_1846(7, 0)
        arrayRegFiles(__tmp_1845 + 1.U) := __tmp_1846(15, 8)
        arrayRegFiles(__tmp_1845 + 2.U) := __tmp_1846(23, 16)
        arrayRegFiles(__tmp_1845 + 3.U) := __tmp_1846(31, 24)

        CP := 212.U
      }

      is(212.U) {
        /*
        switch ($1)
          (0 [U64]): goto 213
          (1 [U64]): goto 228
          (2 [U64]): goto 232
          (3 [U64]): goto 236
          (4 [U64]): goto 240
          (5 [U64]): goto 244
          (6 [U64]): goto 248
          (7 [U64]): goto 252
          (8 [U64]): goto 256
          (9 [U64]): goto 260

        */


        val __tmp_1847 = generalRegFiles(1.U)

        switch(__tmp_1847) {

          is(0.U(64.W)) {
            CP := 213.U
          }


          is(1.U(64.W)) {
            CP := 228.U
          }


          is(2.U(64.W)) {
            CP := 232.U
          }


          is(3.U(64.W)) {
            CP := 236.U
          }


          is(4.U(64.W)) {
            CP := 240.U
          }


          is(5.U(64.W)) {
            CP := 244.U
          }


          is(6.U(64.W)) {
            CP := 248.U
          }


          is(7.U(64.W)) {
            CP := 252.U
          }


          is(8.U(64.W)) {
            CP := 256.U
          }


          is(9.U(64.W)) {
            CP := 260.U
          }

        }

      }

      is(213.U) {
        /*
        *(SP + (6 [SP])) = (217 [U32]) [signed, U32, 4]  // $sfLoc = (217 [U32])
        goto .214
        */


        val __tmp_1848 = SP + 6.U(16.W)
        val __tmp_1849 = (217.S(32.W)).asUInt
        arrayRegFiles(__tmp_1848 + 0.U) := __tmp_1849(7, 0)
        arrayRegFiles(__tmp_1848 + 1.U) := __tmp_1849(15, 8)
        arrayRegFiles(__tmp_1848 + 2.U) := __tmp_1849(23, 16)
        arrayRegFiles(__tmp_1848 + 3.U) := __tmp_1849(31, 24)

        CP := 214.U
      }

      is(214.U) {
        /*
        $0 = *(SP + (99 [SP])) [unsigned, MS[anvil.PrinterIndex.I20, U8], 2]  // $0 = buff
        $1 = *(SP + (135 [SP])) [signed, anvil.PrinterIndex.I20, 1]  // $1 = i
        goto .215
        */


        val __tmp_1850 = (SP + 99.U(16.W)).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_1850 + 1.U),
          arrayRegFiles(__tmp_1850 + 0.U)
        ).asUInt

        val __tmp_1851 = (SP + 135.U(16.W)).asUInt
        generalRegFiles(1.U) := Cat(
          arrayRegFiles(__tmp_1851 + 0.U)
        ).asUInt

        CP := 215.U
      }

      is(215.U) {
        /*
        *(($0 + (12 [SP])) + ($1 as SP)) = (48 [U8]) [unsigned, U8, 1]  // $0($1) = (48 [U8])
        goto .216
        */


        val __tmp_1852 = generalRegFiles(0.U) + 12.U(16.W) + generalRegFiles(1.U).asSInt.asUInt
        val __tmp_1853 = (48.U(8.W)).asUInt
        arrayRegFiles(__tmp_1852 + 0.U) := __tmp_1853(7, 0)

        CP := 216.U
      }

      is(216.U) {
        /*
        *(SP + (6 [SP])) = (205 [U32]) [signed, U32, 4]  // $sfLoc = (205 [U32])
        goto .217
        */


        val __tmp_1854 = SP + 6.U(16.W)
        val __tmp_1855 = (205.S(32.W)).asUInt
        arrayRegFiles(__tmp_1854 + 0.U) := __tmp_1855(7, 0)
        arrayRegFiles(__tmp_1854 + 1.U) := __tmp_1855(15, 8)
        arrayRegFiles(__tmp_1854 + 2.U) := __tmp_1855(23, 16)
        arrayRegFiles(__tmp_1854 + 3.U) := __tmp_1855(31, 24)

        CP := 217.U
      }

      is(217.U) {
        /*
        *(SP + (6 [SP])) = (228 [U32]) [signed, U32, 4]  // $sfLoc = (228 [U32])
        goto .218
        */


        val __tmp_1856 = SP + 6.U(16.W)
        val __tmp_1857 = (228.S(32.W)).asUInt
        arrayRegFiles(__tmp_1856 + 0.U) := __tmp_1857(7, 0)
        arrayRegFiles(__tmp_1856 + 1.U) := __tmp_1857(15, 8)
        arrayRegFiles(__tmp_1856 + 2.U) := __tmp_1857(23, 16)
        arrayRegFiles(__tmp_1856 + 3.U) := __tmp_1857(31, 24)

        CP := 218.U
      }

      is(218.U) {
        /*
        $0 = *(SP + (136 [SP])) [unsigned, U64, 8]  // $0 = m
        goto .219
        */


        val __tmp_1858 = (SP + 136.U(16.W)).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_1858 + 7.U),
          arrayRegFiles(__tmp_1858 + 6.U),
          arrayRegFiles(__tmp_1858 + 5.U),
          arrayRegFiles(__tmp_1858 + 4.U),
          arrayRegFiles(__tmp_1858 + 3.U),
          arrayRegFiles(__tmp_1858 + 2.U),
          arrayRegFiles(__tmp_1858 + 1.U),
          arrayRegFiles(__tmp_1858 + 0.U)
        ).asUInt

        CP := 219.U
      }

      is(219.U) {
        /*
        *(SP + (6 [SP])) = (228 [U32]) [signed, U32, 4]  // $sfLoc = (228 [U32])
        goto .220
        */


        val __tmp_1859 = SP + 6.U(16.W)
        val __tmp_1860 = (228.S(32.W)).asUInt
        arrayRegFiles(__tmp_1859 + 0.U) := __tmp_1860(7, 0)
        arrayRegFiles(__tmp_1859 + 1.U) := __tmp_1860(15, 8)
        arrayRegFiles(__tmp_1859 + 2.U) := __tmp_1860(23, 16)
        arrayRegFiles(__tmp_1859 + 3.U) := __tmp_1860(31, 24)

        CP := 220.U
      }

      is(220.U) {
        /*
        $1 = ($0 / (10 [U64]))
        goto .221
        */


        generalRegFiles(1.U) := generalRegFiles(0.U) / 10.U(64.W)
        CP := 221.U
      }

      is(221.U) {
        /*
        *(SP + (6 [SP])) = (228 [U32]) [signed, U32, 4]  // $sfLoc = (228 [U32])
        goto .222
        */


        val __tmp_1861 = SP + 6.U(16.W)
        val __tmp_1862 = (228.S(32.W)).asUInt
        arrayRegFiles(__tmp_1861 + 0.U) := __tmp_1862(7, 0)
        arrayRegFiles(__tmp_1861 + 1.U) := __tmp_1862(15, 8)
        arrayRegFiles(__tmp_1861 + 2.U) := __tmp_1862(23, 16)
        arrayRegFiles(__tmp_1861 + 3.U) := __tmp_1862(31, 24)

        CP := 222.U
      }

      is(222.U) {
        /*
        *(SP + (136 [SP])) = $1 [signed, U64, 8]  // m = $1
        goto .223
        */


        val __tmp_1863 = SP + 136.U(16.W)
        val __tmp_1864 = (generalRegFiles(1.U)).asUInt
        arrayRegFiles(__tmp_1863 + 0.U) := __tmp_1864(7, 0)
        arrayRegFiles(__tmp_1863 + 1.U) := __tmp_1864(15, 8)
        arrayRegFiles(__tmp_1863 + 2.U) := __tmp_1864(23, 16)
        arrayRegFiles(__tmp_1863 + 3.U) := __tmp_1864(31, 24)
        arrayRegFiles(__tmp_1863 + 4.U) := __tmp_1864(39, 32)
        arrayRegFiles(__tmp_1863 + 5.U) := __tmp_1864(47, 40)
        arrayRegFiles(__tmp_1863 + 6.U) := __tmp_1864(55, 48)
        arrayRegFiles(__tmp_1863 + 7.U) := __tmp_1864(63, 56)

        CP := 223.U
      }

      is(223.U) {
        /*
        *(SP + (6 [SP])) = (229 [U32]) [signed, U32, 4]  // $sfLoc = (229 [U32])
        goto .224
        */


        val __tmp_1865 = SP + 6.U(16.W)
        val __tmp_1866 = (229.S(32.W)).asUInt
        arrayRegFiles(__tmp_1865 + 0.U) := __tmp_1866(7, 0)
        arrayRegFiles(__tmp_1865 + 1.U) := __tmp_1866(15, 8)
        arrayRegFiles(__tmp_1865 + 2.U) := __tmp_1866(23, 16)
        arrayRegFiles(__tmp_1865 + 3.U) := __tmp_1866(31, 24)

        CP := 224.U
      }

      is(224.U) {
        /*
        $0 = *(SP + (135 [SP])) [signed, anvil.PrinterIndex.I20, 1]  // $0 = i
        goto .225
        */


        val __tmp_1867 = (SP + 135.U(16.W)).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_1867 + 0.U)
        ).asUInt

        CP := 225.U
      }

      is(225.U) {
        /*
        $1 = ($0 + (1 [anvil.PrinterIndex.I20]))
        goto .226
        */


        generalRegFiles(1.U) := (generalRegFiles(0.U).asSInt + 1.S(8.W)).asUInt
        CP := 226.U
      }

      is(226.U) {
        /*
        *(SP + (135 [SP])) = $1 [signed, anvil.PrinterIndex.I20, 1]  // i = $1
        goto .227
        */


        val __tmp_1868 = SP + 135.U(16.W)
        val __tmp_1869 = (generalRegFiles(1.U).asSInt).asUInt
        arrayRegFiles(__tmp_1868 + 0.U) := __tmp_1869(7, 0)

        CP := 227.U
      }

      is(227.U) {
        /*
        *(SP + (6 [SP])) = (215 [U32]) [signed, U32, 4]  // $sfLoc = (215 [U32])
        goto .203
        */


        val __tmp_1870 = SP + 6.U(16.W)
        val __tmp_1871 = (215.S(32.W)).asUInt
        arrayRegFiles(__tmp_1870 + 0.U) := __tmp_1871(7, 0)
        arrayRegFiles(__tmp_1870 + 1.U) := __tmp_1871(15, 8)
        arrayRegFiles(__tmp_1870 + 2.U) := __tmp_1871(23, 16)
        arrayRegFiles(__tmp_1870 + 3.U) := __tmp_1871(31, 24)

        CP := 203.U
      }

      is(228.U) {
        /*
        *(SP + (6 [SP])) = (218 [U32]) [signed, U32, 4]  // $sfLoc = (218 [U32])
        goto .229
        */


        val __tmp_1872 = SP + 6.U(16.W)
        val __tmp_1873 = (218.S(32.W)).asUInt
        arrayRegFiles(__tmp_1872 + 0.U) := __tmp_1873(7, 0)
        arrayRegFiles(__tmp_1872 + 1.U) := __tmp_1873(15, 8)
        arrayRegFiles(__tmp_1872 + 2.U) := __tmp_1873(23, 16)
        arrayRegFiles(__tmp_1872 + 3.U) := __tmp_1873(31, 24)

        CP := 229.U
      }

      is(229.U) {
        /*
        $0 = *(SP + (99 [SP])) [unsigned, MS[anvil.PrinterIndex.I20, U8], 2]  // $0 = buff
        $1 = *(SP + (135 [SP])) [signed, anvil.PrinterIndex.I20, 1]  // $1 = i
        goto .230
        */


        val __tmp_1874 = (SP + 99.U(16.W)).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_1874 + 1.U),
          arrayRegFiles(__tmp_1874 + 0.U)
        ).asUInt

        val __tmp_1875 = (SP + 135.U(16.W)).asUInt
        generalRegFiles(1.U) := Cat(
          arrayRegFiles(__tmp_1875 + 0.U)
        ).asUInt

        CP := 230.U
      }

      is(230.U) {
        /*
        *(($0 + (12 [SP])) + ($1 as SP)) = (49 [U8]) [unsigned, U8, 1]  // $0($1) = (49 [U8])
        goto .231
        */


        val __tmp_1876 = generalRegFiles(0.U) + 12.U(16.W) + generalRegFiles(1.U).asSInt.asUInt
        val __tmp_1877 = (49.U(8.W)).asUInt
        arrayRegFiles(__tmp_1876 + 0.U) := __tmp_1877(7, 0)

        CP := 231.U
      }

      is(231.U) {
        /*
        *(SP + (6 [SP])) = (205 [U32]) [signed, U32, 4]  // $sfLoc = (205 [U32])
        goto .217
        */


        val __tmp_1878 = SP + 6.U(16.W)
        val __tmp_1879 = (205.S(32.W)).asUInt
        arrayRegFiles(__tmp_1878 + 0.U) := __tmp_1879(7, 0)
        arrayRegFiles(__tmp_1878 + 1.U) := __tmp_1879(15, 8)
        arrayRegFiles(__tmp_1878 + 2.U) := __tmp_1879(23, 16)
        arrayRegFiles(__tmp_1878 + 3.U) := __tmp_1879(31, 24)

        CP := 217.U
      }

      is(232.U) {
        /*
        *(SP + (6 [SP])) = (219 [U32]) [signed, U32, 4]  // $sfLoc = (219 [U32])
        goto .233
        */


        val __tmp_1880 = SP + 6.U(16.W)
        val __tmp_1881 = (219.S(32.W)).asUInt
        arrayRegFiles(__tmp_1880 + 0.U) := __tmp_1881(7, 0)
        arrayRegFiles(__tmp_1880 + 1.U) := __tmp_1881(15, 8)
        arrayRegFiles(__tmp_1880 + 2.U) := __tmp_1881(23, 16)
        arrayRegFiles(__tmp_1880 + 3.U) := __tmp_1881(31, 24)

        CP := 233.U
      }

      is(233.U) {
        /*
        $0 = *(SP + (99 [SP])) [unsigned, MS[anvil.PrinterIndex.I20, U8], 2]  // $0 = buff
        $1 = *(SP + (135 [SP])) [signed, anvil.PrinterIndex.I20, 1]  // $1 = i
        goto .234
        */


        val __tmp_1882 = (SP + 99.U(16.W)).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_1882 + 1.U),
          arrayRegFiles(__tmp_1882 + 0.U)
        ).asUInt

        val __tmp_1883 = (SP + 135.U(16.W)).asUInt
        generalRegFiles(1.U) := Cat(
          arrayRegFiles(__tmp_1883 + 0.U)
        ).asUInt

        CP := 234.U
      }

      is(234.U) {
        /*
        *(($0 + (12 [SP])) + ($1 as SP)) = (50 [U8]) [unsigned, U8, 1]  // $0($1) = (50 [U8])
        goto .235
        */


        val __tmp_1884 = generalRegFiles(0.U) + 12.U(16.W) + generalRegFiles(1.U).asSInt.asUInt
        val __tmp_1885 = (50.U(8.W)).asUInt
        arrayRegFiles(__tmp_1884 + 0.U) := __tmp_1885(7, 0)

        CP := 235.U
      }

      is(235.U) {
        /*
        *(SP + (6 [SP])) = (205 [U32]) [signed, U32, 4]  // $sfLoc = (205 [U32])
        goto .217
        */


        val __tmp_1886 = SP + 6.U(16.W)
        val __tmp_1887 = (205.S(32.W)).asUInt
        arrayRegFiles(__tmp_1886 + 0.U) := __tmp_1887(7, 0)
        arrayRegFiles(__tmp_1886 + 1.U) := __tmp_1887(15, 8)
        arrayRegFiles(__tmp_1886 + 2.U) := __tmp_1887(23, 16)
        arrayRegFiles(__tmp_1886 + 3.U) := __tmp_1887(31, 24)

        CP := 217.U
      }

      is(236.U) {
        /*
        *(SP + (6 [SP])) = (220 [U32]) [signed, U32, 4]  // $sfLoc = (220 [U32])
        goto .237
        */


        val __tmp_1888 = SP + 6.U(16.W)
        val __tmp_1889 = (220.S(32.W)).asUInt
        arrayRegFiles(__tmp_1888 + 0.U) := __tmp_1889(7, 0)
        arrayRegFiles(__tmp_1888 + 1.U) := __tmp_1889(15, 8)
        arrayRegFiles(__tmp_1888 + 2.U) := __tmp_1889(23, 16)
        arrayRegFiles(__tmp_1888 + 3.U) := __tmp_1889(31, 24)

        CP := 237.U
      }

      is(237.U) {
        /*
        $0 = *(SP + (99 [SP])) [unsigned, MS[anvil.PrinterIndex.I20, U8], 2]  // $0 = buff
        $1 = *(SP + (135 [SP])) [signed, anvil.PrinterIndex.I20, 1]  // $1 = i
        goto .238
        */


        val __tmp_1890 = (SP + 99.U(16.W)).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_1890 + 1.U),
          arrayRegFiles(__tmp_1890 + 0.U)
        ).asUInt

        val __tmp_1891 = (SP + 135.U(16.W)).asUInt
        generalRegFiles(1.U) := Cat(
          arrayRegFiles(__tmp_1891 + 0.U)
        ).asUInt

        CP := 238.U
      }

      is(238.U) {
        /*
        *(($0 + (12 [SP])) + ($1 as SP)) = (51 [U8]) [unsigned, U8, 1]  // $0($1) = (51 [U8])
        goto .239
        */


        val __tmp_1892 = generalRegFiles(0.U) + 12.U(16.W) + generalRegFiles(1.U).asSInt.asUInt
        val __tmp_1893 = (51.U(8.W)).asUInt
        arrayRegFiles(__tmp_1892 + 0.U) := __tmp_1893(7, 0)

        CP := 239.U
      }

      is(239.U) {
        /*
        *(SP + (6 [SP])) = (205 [U32]) [signed, U32, 4]  // $sfLoc = (205 [U32])
        goto .217
        */


        val __tmp_1894 = SP + 6.U(16.W)
        val __tmp_1895 = (205.S(32.W)).asUInt
        arrayRegFiles(__tmp_1894 + 0.U) := __tmp_1895(7, 0)
        arrayRegFiles(__tmp_1894 + 1.U) := __tmp_1895(15, 8)
        arrayRegFiles(__tmp_1894 + 2.U) := __tmp_1895(23, 16)
        arrayRegFiles(__tmp_1894 + 3.U) := __tmp_1895(31, 24)

        CP := 217.U
      }

      is(240.U) {
        /*
        *(SP + (6 [SP])) = (221 [U32]) [signed, U32, 4]  // $sfLoc = (221 [U32])
        goto .241
        */


        val __tmp_1896 = SP + 6.U(16.W)
        val __tmp_1897 = (221.S(32.W)).asUInt
        arrayRegFiles(__tmp_1896 + 0.U) := __tmp_1897(7, 0)
        arrayRegFiles(__tmp_1896 + 1.U) := __tmp_1897(15, 8)
        arrayRegFiles(__tmp_1896 + 2.U) := __tmp_1897(23, 16)
        arrayRegFiles(__tmp_1896 + 3.U) := __tmp_1897(31, 24)

        CP := 241.U
      }

      is(241.U) {
        /*
        $0 = *(SP + (99 [SP])) [unsigned, MS[anvil.PrinterIndex.I20, U8], 2]  // $0 = buff
        $1 = *(SP + (135 [SP])) [signed, anvil.PrinterIndex.I20, 1]  // $1 = i
        goto .242
        */


        val __tmp_1898 = (SP + 99.U(16.W)).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_1898 + 1.U),
          arrayRegFiles(__tmp_1898 + 0.U)
        ).asUInt

        val __tmp_1899 = (SP + 135.U(16.W)).asUInt
        generalRegFiles(1.U) := Cat(
          arrayRegFiles(__tmp_1899 + 0.U)
        ).asUInt

        CP := 242.U
      }

      is(242.U) {
        /*
        *(($0 + (12 [SP])) + ($1 as SP)) = (52 [U8]) [unsigned, U8, 1]  // $0($1) = (52 [U8])
        goto .243
        */


        val __tmp_1900 = generalRegFiles(0.U) + 12.U(16.W) + generalRegFiles(1.U).asSInt.asUInt
        val __tmp_1901 = (52.U(8.W)).asUInt
        arrayRegFiles(__tmp_1900 + 0.U) := __tmp_1901(7, 0)

        CP := 243.U
      }

      is(243.U) {
        /*
        *(SP + (6 [SP])) = (205 [U32]) [signed, U32, 4]  // $sfLoc = (205 [U32])
        goto .217
        */


        val __tmp_1902 = SP + 6.U(16.W)
        val __tmp_1903 = (205.S(32.W)).asUInt
        arrayRegFiles(__tmp_1902 + 0.U) := __tmp_1903(7, 0)
        arrayRegFiles(__tmp_1902 + 1.U) := __tmp_1903(15, 8)
        arrayRegFiles(__tmp_1902 + 2.U) := __tmp_1903(23, 16)
        arrayRegFiles(__tmp_1902 + 3.U) := __tmp_1903(31, 24)

        CP := 217.U
      }

      is(244.U) {
        /*
        *(SP + (6 [SP])) = (222 [U32]) [signed, U32, 4]  // $sfLoc = (222 [U32])
        goto .245
        */


        val __tmp_1904 = SP + 6.U(16.W)
        val __tmp_1905 = (222.S(32.W)).asUInt
        arrayRegFiles(__tmp_1904 + 0.U) := __tmp_1905(7, 0)
        arrayRegFiles(__tmp_1904 + 1.U) := __tmp_1905(15, 8)
        arrayRegFiles(__tmp_1904 + 2.U) := __tmp_1905(23, 16)
        arrayRegFiles(__tmp_1904 + 3.U) := __tmp_1905(31, 24)

        CP := 245.U
      }

      is(245.U) {
        /*
        $0 = *(SP + (99 [SP])) [unsigned, MS[anvil.PrinterIndex.I20, U8], 2]  // $0 = buff
        $1 = *(SP + (135 [SP])) [signed, anvil.PrinterIndex.I20, 1]  // $1 = i
        goto .246
        */


        val __tmp_1906 = (SP + 99.U(16.W)).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_1906 + 1.U),
          arrayRegFiles(__tmp_1906 + 0.U)
        ).asUInt

        val __tmp_1907 = (SP + 135.U(16.W)).asUInt
        generalRegFiles(1.U) := Cat(
          arrayRegFiles(__tmp_1907 + 0.U)
        ).asUInt

        CP := 246.U
      }

      is(246.U) {
        /*
        *(($0 + (12 [SP])) + ($1 as SP)) = (53 [U8]) [unsigned, U8, 1]  // $0($1) = (53 [U8])
        goto .247
        */


        val __tmp_1908 = generalRegFiles(0.U) + 12.U(16.W) + generalRegFiles(1.U).asSInt.asUInt
        val __tmp_1909 = (53.U(8.W)).asUInt
        arrayRegFiles(__tmp_1908 + 0.U) := __tmp_1909(7, 0)

        CP := 247.U
      }

      is(247.U) {
        /*
        *(SP + (6 [SP])) = (205 [U32]) [signed, U32, 4]  // $sfLoc = (205 [U32])
        goto .217
        */


        val __tmp_1910 = SP + 6.U(16.W)
        val __tmp_1911 = (205.S(32.W)).asUInt
        arrayRegFiles(__tmp_1910 + 0.U) := __tmp_1911(7, 0)
        arrayRegFiles(__tmp_1910 + 1.U) := __tmp_1911(15, 8)
        arrayRegFiles(__tmp_1910 + 2.U) := __tmp_1911(23, 16)
        arrayRegFiles(__tmp_1910 + 3.U) := __tmp_1911(31, 24)

        CP := 217.U
      }

      is(248.U) {
        /*
        *(SP + (6 [SP])) = (223 [U32]) [signed, U32, 4]  // $sfLoc = (223 [U32])
        goto .249
        */


        val __tmp_1912 = SP + 6.U(16.W)
        val __tmp_1913 = (223.S(32.W)).asUInt
        arrayRegFiles(__tmp_1912 + 0.U) := __tmp_1913(7, 0)
        arrayRegFiles(__tmp_1912 + 1.U) := __tmp_1913(15, 8)
        arrayRegFiles(__tmp_1912 + 2.U) := __tmp_1913(23, 16)
        arrayRegFiles(__tmp_1912 + 3.U) := __tmp_1913(31, 24)

        CP := 249.U
      }

      is(249.U) {
        /*
        $0 = *(SP + (99 [SP])) [unsigned, MS[anvil.PrinterIndex.I20, U8], 2]  // $0 = buff
        $1 = *(SP + (135 [SP])) [signed, anvil.PrinterIndex.I20, 1]  // $1 = i
        goto .250
        */


        val __tmp_1914 = (SP + 99.U(16.W)).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_1914 + 1.U),
          arrayRegFiles(__tmp_1914 + 0.U)
        ).asUInt

        val __tmp_1915 = (SP + 135.U(16.W)).asUInt
        generalRegFiles(1.U) := Cat(
          arrayRegFiles(__tmp_1915 + 0.U)
        ).asUInt

        CP := 250.U
      }

      is(250.U) {
        /*
        *(($0 + (12 [SP])) + ($1 as SP)) = (54 [U8]) [unsigned, U8, 1]  // $0($1) = (54 [U8])
        goto .251
        */


        val __tmp_1916 = generalRegFiles(0.U) + 12.U(16.W) + generalRegFiles(1.U).asSInt.asUInt
        val __tmp_1917 = (54.U(8.W)).asUInt
        arrayRegFiles(__tmp_1916 + 0.U) := __tmp_1917(7, 0)

        CP := 251.U
      }

      is(251.U) {
        /*
        *(SP + (6 [SP])) = (205 [U32]) [signed, U32, 4]  // $sfLoc = (205 [U32])
        goto .217
        */


        val __tmp_1918 = SP + 6.U(16.W)
        val __tmp_1919 = (205.S(32.W)).asUInt
        arrayRegFiles(__tmp_1918 + 0.U) := __tmp_1919(7, 0)
        arrayRegFiles(__tmp_1918 + 1.U) := __tmp_1919(15, 8)
        arrayRegFiles(__tmp_1918 + 2.U) := __tmp_1919(23, 16)
        arrayRegFiles(__tmp_1918 + 3.U) := __tmp_1919(31, 24)

        CP := 217.U
      }

      is(252.U) {
        /*
        *(SP + (6 [SP])) = (224 [U32]) [signed, U32, 4]  // $sfLoc = (224 [U32])
        goto .253
        */


        val __tmp_1920 = SP + 6.U(16.W)
        val __tmp_1921 = (224.S(32.W)).asUInt
        arrayRegFiles(__tmp_1920 + 0.U) := __tmp_1921(7, 0)
        arrayRegFiles(__tmp_1920 + 1.U) := __tmp_1921(15, 8)
        arrayRegFiles(__tmp_1920 + 2.U) := __tmp_1921(23, 16)
        arrayRegFiles(__tmp_1920 + 3.U) := __tmp_1921(31, 24)

        CP := 253.U
      }

      is(253.U) {
        /*
        $0 = *(SP + (99 [SP])) [unsigned, MS[anvil.PrinterIndex.I20, U8], 2]  // $0 = buff
        $1 = *(SP + (135 [SP])) [signed, anvil.PrinterIndex.I20, 1]  // $1 = i
        goto .254
        */


        val __tmp_1922 = (SP + 99.U(16.W)).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_1922 + 1.U),
          arrayRegFiles(__tmp_1922 + 0.U)
        ).asUInt

        val __tmp_1923 = (SP + 135.U(16.W)).asUInt
        generalRegFiles(1.U) := Cat(
          arrayRegFiles(__tmp_1923 + 0.U)
        ).asUInt

        CP := 254.U
      }

      is(254.U) {
        /*
        *(($0 + (12 [SP])) + ($1 as SP)) = (55 [U8]) [unsigned, U8, 1]  // $0($1) = (55 [U8])
        goto .255
        */


        val __tmp_1924 = generalRegFiles(0.U) + 12.U(16.W) + generalRegFiles(1.U).asSInt.asUInt
        val __tmp_1925 = (55.U(8.W)).asUInt
        arrayRegFiles(__tmp_1924 + 0.U) := __tmp_1925(7, 0)

        CP := 255.U
      }

      is(255.U) {
        /*
        *(SP + (6 [SP])) = (205 [U32]) [signed, U32, 4]  // $sfLoc = (205 [U32])
        goto .217
        */


        val __tmp_1926 = SP + 6.U(16.W)
        val __tmp_1927 = (205.S(32.W)).asUInt
        arrayRegFiles(__tmp_1926 + 0.U) := __tmp_1927(7, 0)
        arrayRegFiles(__tmp_1926 + 1.U) := __tmp_1927(15, 8)
        arrayRegFiles(__tmp_1926 + 2.U) := __tmp_1927(23, 16)
        arrayRegFiles(__tmp_1926 + 3.U) := __tmp_1927(31, 24)

        CP := 217.U
      }

      is(256.U) {
        /*
        *(SP + (6 [SP])) = (225 [U32]) [signed, U32, 4]  // $sfLoc = (225 [U32])
        goto .257
        */


        val __tmp_1928 = SP + 6.U(16.W)
        val __tmp_1929 = (225.S(32.W)).asUInt
        arrayRegFiles(__tmp_1928 + 0.U) := __tmp_1929(7, 0)
        arrayRegFiles(__tmp_1928 + 1.U) := __tmp_1929(15, 8)
        arrayRegFiles(__tmp_1928 + 2.U) := __tmp_1929(23, 16)
        arrayRegFiles(__tmp_1928 + 3.U) := __tmp_1929(31, 24)

        CP := 257.U
      }

      is(257.U) {
        /*
        $0 = *(SP + (99 [SP])) [unsigned, MS[anvil.PrinterIndex.I20, U8], 2]  // $0 = buff
        $1 = *(SP + (135 [SP])) [signed, anvil.PrinterIndex.I20, 1]  // $1 = i
        goto .258
        */


        val __tmp_1930 = (SP + 99.U(16.W)).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_1930 + 1.U),
          arrayRegFiles(__tmp_1930 + 0.U)
        ).asUInt

        val __tmp_1931 = (SP + 135.U(16.W)).asUInt
        generalRegFiles(1.U) := Cat(
          arrayRegFiles(__tmp_1931 + 0.U)
        ).asUInt

        CP := 258.U
      }

      is(258.U) {
        /*
        *(($0 + (12 [SP])) + ($1 as SP)) = (56 [U8]) [unsigned, U8, 1]  // $0($1) = (56 [U8])
        goto .259
        */


        val __tmp_1932 = generalRegFiles(0.U) + 12.U(16.W) + generalRegFiles(1.U).asSInt.asUInt
        val __tmp_1933 = (56.U(8.W)).asUInt
        arrayRegFiles(__tmp_1932 + 0.U) := __tmp_1933(7, 0)

        CP := 259.U
      }

      is(259.U) {
        /*
        *(SP + (6 [SP])) = (205 [U32]) [signed, U32, 4]  // $sfLoc = (205 [U32])
        goto .217
        */


        val __tmp_1934 = SP + 6.U(16.W)
        val __tmp_1935 = (205.S(32.W)).asUInt
        arrayRegFiles(__tmp_1934 + 0.U) := __tmp_1935(7, 0)
        arrayRegFiles(__tmp_1934 + 1.U) := __tmp_1935(15, 8)
        arrayRegFiles(__tmp_1934 + 2.U) := __tmp_1935(23, 16)
        arrayRegFiles(__tmp_1934 + 3.U) := __tmp_1935(31, 24)

        CP := 217.U
      }

      is(260.U) {
        /*
        *(SP + (6 [SP])) = (226 [U32]) [signed, U32, 4]  // $sfLoc = (226 [U32])
        goto .261
        */


        val __tmp_1936 = SP + 6.U(16.W)
        val __tmp_1937 = (226.S(32.W)).asUInt
        arrayRegFiles(__tmp_1936 + 0.U) := __tmp_1937(7, 0)
        arrayRegFiles(__tmp_1936 + 1.U) := __tmp_1937(15, 8)
        arrayRegFiles(__tmp_1936 + 2.U) := __tmp_1937(23, 16)
        arrayRegFiles(__tmp_1936 + 3.U) := __tmp_1937(31, 24)

        CP := 261.U
      }

      is(261.U) {
        /*
        $0 = *(SP + (99 [SP])) [unsigned, MS[anvil.PrinterIndex.I20, U8], 2]  // $0 = buff
        $1 = *(SP + (135 [SP])) [signed, anvil.PrinterIndex.I20, 1]  // $1 = i
        goto .262
        */


        val __tmp_1938 = (SP + 99.U(16.W)).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_1938 + 1.U),
          arrayRegFiles(__tmp_1938 + 0.U)
        ).asUInt

        val __tmp_1939 = (SP + 135.U(16.W)).asUInt
        generalRegFiles(1.U) := Cat(
          arrayRegFiles(__tmp_1939 + 0.U)
        ).asUInt

        CP := 262.U
      }

      is(262.U) {
        /*
        *(($0 + (12 [SP])) + ($1 as SP)) = (57 [U8]) [unsigned, U8, 1]  // $0($1) = (57 [U8])
        goto .263
        */


        val __tmp_1940 = generalRegFiles(0.U) + 12.U(16.W) + generalRegFiles(1.U).asSInt.asUInt
        val __tmp_1941 = (57.U(8.W)).asUInt
        arrayRegFiles(__tmp_1940 + 0.U) := __tmp_1941(7, 0)

        CP := 263.U
      }

      is(263.U) {
        /*
        *(SP + (6 [SP])) = (205 [U32]) [signed, U32, 4]  // $sfLoc = (205 [U32])
        goto .217
        */


        val __tmp_1942 = SP + 6.U(16.W)
        val __tmp_1943 = (205.S(32.W)).asUInt
        arrayRegFiles(__tmp_1942 + 0.U) := __tmp_1943(7, 0)
        arrayRegFiles(__tmp_1942 + 1.U) := __tmp_1943(15, 8)
        arrayRegFiles(__tmp_1942 + 2.U) := __tmp_1943(23, 16)
        arrayRegFiles(__tmp_1942 + 3.U) := __tmp_1943(31, 24)

        CP := 217.U
      }

      is(264.U) {
        /*
        *(SP + (6 [SP])) = (231 [U32]) [signed, U32, 4]  // $sfLoc = (231 [U32])
        goto .265
        */


        val __tmp_1944 = SP + 6.U(16.W)
        val __tmp_1945 = (231.S(32.W)).asUInt
        arrayRegFiles(__tmp_1944 + 0.U) := __tmp_1945(7, 0)
        arrayRegFiles(__tmp_1944 + 1.U) := __tmp_1945(15, 8)
        arrayRegFiles(__tmp_1944 + 2.U) := __tmp_1945(23, 16)
        arrayRegFiles(__tmp_1944 + 3.U) := __tmp_1945(31, 24)

        CP := 265.U
      }

      is(265.U) {
        /*
        decl j: anvil.PrinterIndex.I20 [@144, 1]
        $0 = *(SP + (135 [SP])) [signed, anvil.PrinterIndex.I20, 1]  // $0 = i
        goto .266
        */


        val __tmp_1946 = (SP + 135.U(16.W)).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_1946 + 0.U)
        ).asUInt

        CP := 266.U
      }

      is(266.U) {
        /*
        $1 = ($0 - (1 [anvil.PrinterIndex.I20]))
        goto .267
        */


        generalRegFiles(1.U) := (generalRegFiles(0.U).asSInt - 1.S(8.W)).asUInt
        CP := 267.U
      }

      is(267.U) {
        /*
        *(SP + (144 [SP])) = $1 [signed, anvil.PrinterIndex.I20, 1]  // j = $1
        goto .268
        */


        val __tmp_1947 = SP + 144.U(16.W)
        val __tmp_1948 = (generalRegFiles(1.U).asSInt).asUInt
        arrayRegFiles(__tmp_1947 + 0.U) := __tmp_1948(7, 0)

        CP := 268.U
      }

      is(268.U) {
        /*
        *(SP + (6 [SP])) = (232 [U32]) [signed, U32, 4]  // $sfLoc = (232 [U32])
        goto .269
        */


        val __tmp_1949 = SP + 6.U(16.W)
        val __tmp_1950 = (232.S(32.W)).asUInt
        arrayRegFiles(__tmp_1949 + 0.U) := __tmp_1950(7, 0)
        arrayRegFiles(__tmp_1949 + 1.U) := __tmp_1950(15, 8)
        arrayRegFiles(__tmp_1949 + 2.U) := __tmp_1950(23, 16)
        arrayRegFiles(__tmp_1949 + 3.U) := __tmp_1950(31, 24)

        CP := 269.U
      }

      is(269.U) {
        /*
        decl idx: anvil.PrinterIndex.U [@145, 8]
        $0 = *(SP + (75 [SP])) [unsigned, anvil.PrinterIndex.U, 8]  // $0 = index
        goto .270
        */


        val __tmp_1951 = (SP + 75.U(16.W)).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_1951 + 7.U),
          arrayRegFiles(__tmp_1951 + 6.U),
          arrayRegFiles(__tmp_1951 + 5.U),
          arrayRegFiles(__tmp_1951 + 4.U),
          arrayRegFiles(__tmp_1951 + 3.U),
          arrayRegFiles(__tmp_1951 + 2.U),
          arrayRegFiles(__tmp_1951 + 1.U),
          arrayRegFiles(__tmp_1951 + 0.U)
        ).asUInt

        CP := 270.U
      }

      is(270.U) {
        /*
        *(SP + (145 [SP])) = $0 [signed, anvil.PrinterIndex.U, 8]  // idx = $0
        goto .271
        */


        val __tmp_1952 = SP + 145.U(16.W)
        val __tmp_1953 = (generalRegFiles(0.U)).asUInt
        arrayRegFiles(__tmp_1952 + 0.U) := __tmp_1953(7, 0)
        arrayRegFiles(__tmp_1952 + 1.U) := __tmp_1953(15, 8)
        arrayRegFiles(__tmp_1952 + 2.U) := __tmp_1953(23, 16)
        arrayRegFiles(__tmp_1952 + 3.U) := __tmp_1953(31, 24)
        arrayRegFiles(__tmp_1952 + 4.U) := __tmp_1953(39, 32)
        arrayRegFiles(__tmp_1952 + 5.U) := __tmp_1953(47, 40)
        arrayRegFiles(__tmp_1952 + 6.U) := __tmp_1953(55, 48)
        arrayRegFiles(__tmp_1952 + 7.U) := __tmp_1953(63, 56)

        CP := 271.U
      }

      is(271.U) {
        /*
        *(SP + (6 [SP])) = (233 [U32]) [signed, U32, 4]  // $sfLoc = (233 [U32])
        goto .272
        */


        val __tmp_1954 = SP + 6.U(16.W)
        val __tmp_1955 = (233.S(32.W)).asUInt
        arrayRegFiles(__tmp_1954 + 0.U) := __tmp_1955(7, 0)
        arrayRegFiles(__tmp_1954 + 1.U) := __tmp_1955(15, 8)
        arrayRegFiles(__tmp_1954 + 2.U) := __tmp_1955(23, 16)
        arrayRegFiles(__tmp_1954 + 3.U) := __tmp_1955(31, 24)

        CP := 272.U
      }

      is(272.U) {
        /*
        decl r: U64 [@153, 8]
        *(SP + (153 [SP])) = (0 [U64]) [signed, U64, 8]  // r = (0 [U64])
        goto .273
        */


        val __tmp_1956 = SP + 153.U(16.W)
        val __tmp_1957 = (0.S(64.W)).asUInt
        arrayRegFiles(__tmp_1956 + 0.U) := __tmp_1957(7, 0)
        arrayRegFiles(__tmp_1956 + 1.U) := __tmp_1957(15, 8)
        arrayRegFiles(__tmp_1956 + 2.U) := __tmp_1957(23, 16)
        arrayRegFiles(__tmp_1956 + 3.U) := __tmp_1957(31, 24)
        arrayRegFiles(__tmp_1956 + 4.U) := __tmp_1957(39, 32)
        arrayRegFiles(__tmp_1956 + 5.U) := __tmp_1957(47, 40)
        arrayRegFiles(__tmp_1956 + 6.U) := __tmp_1957(55, 48)
        arrayRegFiles(__tmp_1956 + 7.U) := __tmp_1957(63, 56)

        CP := 273.U
      }

      is(273.U) {
        /*
        *(SP + (6 [SP])) = (234 [U32]) [signed, U32, 4]  // $sfLoc = (234 [U32])
        goto .274
        */


        val __tmp_1958 = SP + 6.U(16.W)
        val __tmp_1959 = (234.S(32.W)).asUInt
        arrayRegFiles(__tmp_1958 + 0.U) := __tmp_1959(7, 0)
        arrayRegFiles(__tmp_1958 + 1.U) := __tmp_1959(15, 8)
        arrayRegFiles(__tmp_1958 + 2.U) := __tmp_1959(23, 16)
        arrayRegFiles(__tmp_1958 + 3.U) := __tmp_1959(31, 24)

        CP := 274.U
      }

      is(274.U) {
        /*
        *(SP + (6 [SP])) = (234 [U32]) [signed, U32, 4]  // $sfLoc = (234 [U32])
        goto .275
        */


        val __tmp_1960 = SP + 6.U(16.W)
        val __tmp_1961 = (234.S(32.W)).asUInt
        arrayRegFiles(__tmp_1960 + 0.U) := __tmp_1961(7, 0)
        arrayRegFiles(__tmp_1960 + 1.U) := __tmp_1961(15, 8)
        arrayRegFiles(__tmp_1960 + 2.U) := __tmp_1961(23, 16)
        arrayRegFiles(__tmp_1960 + 3.U) := __tmp_1961(31, 24)

        CP := 275.U
      }

      is(275.U) {
        /*
        $0 = *(SP + (144 [SP])) [signed, anvil.PrinterIndex.I20, 1]  // $0 = j
        goto .276
        */


        val __tmp_1962 = (SP + 144.U(16.W)).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_1962 + 0.U)
        ).asUInt

        CP := 276.U
      }

      is(276.U) {
        /*
        $1 = ($0 >= (0 [anvil.PrinterIndex.I20]))
        goto .277
        */


        generalRegFiles(1.U) := (generalRegFiles(0.U).asSInt >= 0.S(8.W)).asUInt
        CP := 277.U
      }

      is(277.U) {
        /*
        if $1 goto .278 else goto .297
        */


        CP := Mux((generalRegFiles(1.U).asUInt) === 1.U, 278.U, 297.U)
      }

      is(278.U) {
        /*
        *(SP + (6 [SP])) = (235 [U32]) [signed, U32, 4]  // $sfLoc = (235 [U32])
        goto .279
        */


        val __tmp_1963 = SP + 6.U(16.W)
        val __tmp_1964 = (235.S(32.W)).asUInt
        arrayRegFiles(__tmp_1963 + 0.U) := __tmp_1964(7, 0)
        arrayRegFiles(__tmp_1963 + 1.U) := __tmp_1964(15, 8)
        arrayRegFiles(__tmp_1963 + 2.U) := __tmp_1964(23, 16)
        arrayRegFiles(__tmp_1963 + 3.U) := __tmp_1964(31, 24)

        CP := 279.U
      }

      is(279.U) {
        /*
        $0 = *(SP + (73 [SP])) [unsigned, MS[anvil.PrinterIndex.U, U8], 2]  // $0 = buffer
        $1 = *(SP + (145 [SP])) [unsigned, anvil.PrinterIndex.U, 8]  // $1 = idx
        $2 = *(SP + (83 [SP])) [unsigned, anvil.PrinterIndex.U, 8]  // $2 = mask
        goto .280
        */


        val __tmp_1965 = (SP + 73.U(16.W)).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_1965 + 1.U),
          arrayRegFiles(__tmp_1965 + 0.U)
        ).asUInt

        val __tmp_1966 = (SP + 145.U(16.W)).asUInt
        generalRegFiles(1.U) := Cat(
          arrayRegFiles(__tmp_1966 + 7.U),
          arrayRegFiles(__tmp_1966 + 6.U),
          arrayRegFiles(__tmp_1966 + 5.U),
          arrayRegFiles(__tmp_1966 + 4.U),
          arrayRegFiles(__tmp_1966 + 3.U),
          arrayRegFiles(__tmp_1966 + 2.U),
          arrayRegFiles(__tmp_1966 + 1.U),
          arrayRegFiles(__tmp_1966 + 0.U)
        ).asUInt

        val __tmp_1967 = (SP + 83.U(16.W)).asUInt
        generalRegFiles(2.U) := Cat(
          arrayRegFiles(__tmp_1967 + 7.U),
          arrayRegFiles(__tmp_1967 + 6.U),
          arrayRegFiles(__tmp_1967 + 5.U),
          arrayRegFiles(__tmp_1967 + 4.U),
          arrayRegFiles(__tmp_1967 + 3.U),
          arrayRegFiles(__tmp_1967 + 2.U),
          arrayRegFiles(__tmp_1967 + 1.U),
          arrayRegFiles(__tmp_1967 + 0.U)
        ).asUInt

        CP := 280.U
      }

      is(280.U) {
        /*
        $3 = ($1 & $2)
        goto .281
        */


        generalRegFiles(3.U) := generalRegFiles(1.U) & generalRegFiles(2.U)
        CP := 281.U
      }

      is(281.U) {
        /*
        $4 = *(SP + (99 [SP])) [unsigned, MS[anvil.PrinterIndex.I20, U8], 2]  // $4 = buff
        $5 = *(SP + (144 [SP])) [signed, anvil.PrinterIndex.I20, 1]  // $5 = j
        goto .282
        */


        val __tmp_1968 = (SP + 99.U(16.W)).asUInt
        generalRegFiles(4.U) := Cat(
          arrayRegFiles(__tmp_1968 + 1.U),
          arrayRegFiles(__tmp_1968 + 0.U)
        ).asUInt

        val __tmp_1969 = (SP + 144.U(16.W)).asUInt
        generalRegFiles(5.U) := Cat(
          arrayRegFiles(__tmp_1969 + 0.U)
        ).asUInt

        CP := 282.U
      }

      is(282.U) {
        /*
        $6 = *(($4 + (12 [SP])) + ($5 as SP)) [unsigned, U8, 1]  // $6 = $4($5)
        goto .283
        */


        val __tmp_1970 = (generalRegFiles(4.U) + 12.U(16.W) + generalRegFiles(5.U).asSInt.asUInt).asUInt
        generalRegFiles(6.U) := Cat(
          arrayRegFiles(__tmp_1970 + 0.U)
        ).asUInt

        CP := 283.U
      }

      is(283.U) {
        /*
        *(($0 + (12 [SP])) + ($3 as SP)) = $6 [unsigned, U8, 1]  // $0($3) = $6
        goto .284
        */


        val __tmp_1971 = generalRegFiles(0.U) + 12.U(16.W) + generalRegFiles(3.U).asUInt
        val __tmp_1972 = (generalRegFiles(6.U)).asUInt
        arrayRegFiles(__tmp_1971 + 0.U) := __tmp_1972(7, 0)

        CP := 284.U
      }

      is(284.U) {
        /*
        *(SP + (6 [SP])) = (236 [U32]) [signed, U32, 4]  // $sfLoc = (236 [U32])
        goto .285
        */


        val __tmp_1973 = SP + 6.U(16.W)
        val __tmp_1974 = (236.S(32.W)).asUInt
        arrayRegFiles(__tmp_1973 + 0.U) := __tmp_1974(7, 0)
        arrayRegFiles(__tmp_1973 + 1.U) := __tmp_1974(15, 8)
        arrayRegFiles(__tmp_1973 + 2.U) := __tmp_1974(23, 16)
        arrayRegFiles(__tmp_1973 + 3.U) := __tmp_1974(31, 24)

        CP := 285.U
      }

      is(285.U) {
        /*
        $0 = *(SP + (144 [SP])) [signed, anvil.PrinterIndex.I20, 1]  // $0 = j
        goto .286
        */


        val __tmp_1975 = (SP + 144.U(16.W)).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_1975 + 0.U)
        ).asUInt

        CP := 286.U
      }

      is(286.U) {
        /*
        $1 = ($0 - (1 [anvil.PrinterIndex.I20]))
        goto .287
        */


        generalRegFiles(1.U) := (generalRegFiles(0.U).asSInt - 1.S(8.W)).asUInt
        CP := 287.U
      }

      is(287.U) {
        /*
        *(SP + (144 [SP])) = $1 [signed, anvil.PrinterIndex.I20, 1]  // j = $1
        goto .288
        */


        val __tmp_1976 = SP + 144.U(16.W)
        val __tmp_1977 = (generalRegFiles(1.U).asSInt).asUInt
        arrayRegFiles(__tmp_1976 + 0.U) := __tmp_1977(7, 0)

        CP := 288.U
      }

      is(288.U) {
        /*
        *(SP + (6 [SP])) = (237 [U32]) [signed, U32, 4]  // $sfLoc = (237 [U32])
        goto .289
        */


        val __tmp_1978 = SP + 6.U(16.W)
        val __tmp_1979 = (237.S(32.W)).asUInt
        arrayRegFiles(__tmp_1978 + 0.U) := __tmp_1979(7, 0)
        arrayRegFiles(__tmp_1978 + 1.U) := __tmp_1979(15, 8)
        arrayRegFiles(__tmp_1978 + 2.U) := __tmp_1979(23, 16)
        arrayRegFiles(__tmp_1978 + 3.U) := __tmp_1979(31, 24)

        CP := 289.U
      }

      is(289.U) {
        /*
        $0 = *(SP + (145 [SP])) [unsigned, anvil.PrinterIndex.U, 8]  // $0 = idx
        goto .290
        */


        val __tmp_1980 = (SP + 145.U(16.W)).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_1980 + 7.U),
          arrayRegFiles(__tmp_1980 + 6.U),
          arrayRegFiles(__tmp_1980 + 5.U),
          arrayRegFiles(__tmp_1980 + 4.U),
          arrayRegFiles(__tmp_1980 + 3.U),
          arrayRegFiles(__tmp_1980 + 2.U),
          arrayRegFiles(__tmp_1980 + 1.U),
          arrayRegFiles(__tmp_1980 + 0.U)
        ).asUInt

        CP := 290.U
      }

      is(290.U) {
        /*
        $1 = ($0 + (1 [anvil.PrinterIndex.U]))
        goto .291
        */


        generalRegFiles(1.U) := generalRegFiles(0.U) + 1.U(64.W)
        CP := 291.U
      }

      is(291.U) {
        /*
        *(SP + (145 [SP])) = $1 [signed, anvil.PrinterIndex.U, 8]  // idx = $1
        goto .292
        */


        val __tmp_1981 = SP + 145.U(16.W)
        val __tmp_1982 = (generalRegFiles(1.U)).asUInt
        arrayRegFiles(__tmp_1981 + 0.U) := __tmp_1982(7, 0)
        arrayRegFiles(__tmp_1981 + 1.U) := __tmp_1982(15, 8)
        arrayRegFiles(__tmp_1981 + 2.U) := __tmp_1982(23, 16)
        arrayRegFiles(__tmp_1981 + 3.U) := __tmp_1982(31, 24)
        arrayRegFiles(__tmp_1981 + 4.U) := __tmp_1982(39, 32)
        arrayRegFiles(__tmp_1981 + 5.U) := __tmp_1982(47, 40)
        arrayRegFiles(__tmp_1981 + 6.U) := __tmp_1982(55, 48)
        arrayRegFiles(__tmp_1981 + 7.U) := __tmp_1982(63, 56)

        CP := 292.U
      }

      is(292.U) {
        /*
        *(SP + (6 [SP])) = (238 [U32]) [signed, U32, 4]  // $sfLoc = (238 [U32])
        goto .293
        */


        val __tmp_1983 = SP + 6.U(16.W)
        val __tmp_1984 = (238.S(32.W)).asUInt
        arrayRegFiles(__tmp_1983 + 0.U) := __tmp_1984(7, 0)
        arrayRegFiles(__tmp_1983 + 1.U) := __tmp_1984(15, 8)
        arrayRegFiles(__tmp_1983 + 2.U) := __tmp_1984(23, 16)
        arrayRegFiles(__tmp_1983 + 3.U) := __tmp_1984(31, 24)

        CP := 293.U
      }

      is(293.U) {
        /*
        $0 = *(SP + (153 [SP])) [unsigned, U64, 8]  // $0 = r
        goto .294
        */


        val __tmp_1985 = (SP + 153.U(16.W)).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_1985 + 7.U),
          arrayRegFiles(__tmp_1985 + 6.U),
          arrayRegFiles(__tmp_1985 + 5.U),
          arrayRegFiles(__tmp_1985 + 4.U),
          arrayRegFiles(__tmp_1985 + 3.U),
          arrayRegFiles(__tmp_1985 + 2.U),
          arrayRegFiles(__tmp_1985 + 1.U),
          arrayRegFiles(__tmp_1985 + 0.U)
        ).asUInt

        CP := 294.U
      }

      is(294.U) {
        /*
        $1 = ($0 + (1 [U64]))
        goto .295
        */


        generalRegFiles(1.U) := generalRegFiles(0.U) + 1.U(64.W)
        CP := 295.U
      }

      is(295.U) {
        /*
        *(SP + (153 [SP])) = $1 [signed, U64, 8]  // r = $1
        goto .296
        */


        val __tmp_1986 = SP + 153.U(16.W)
        val __tmp_1987 = (generalRegFiles(1.U)).asUInt
        arrayRegFiles(__tmp_1986 + 0.U) := __tmp_1987(7, 0)
        arrayRegFiles(__tmp_1986 + 1.U) := __tmp_1987(15, 8)
        arrayRegFiles(__tmp_1986 + 2.U) := __tmp_1987(23, 16)
        arrayRegFiles(__tmp_1986 + 3.U) := __tmp_1987(31, 24)
        arrayRegFiles(__tmp_1986 + 4.U) := __tmp_1987(39, 32)
        arrayRegFiles(__tmp_1986 + 5.U) := __tmp_1987(47, 40)
        arrayRegFiles(__tmp_1986 + 6.U) := __tmp_1987(55, 48)
        arrayRegFiles(__tmp_1986 + 7.U) := __tmp_1987(63, 56)

        CP := 296.U
      }

      is(296.U) {
        /*
        *(SP + (6 [SP])) = (234 [U32]) [signed, U32, 4]  // $sfLoc = (234 [U32])
        goto .274
        */


        val __tmp_1988 = SP + 6.U(16.W)
        val __tmp_1989 = (234.S(32.W)).asUInt
        arrayRegFiles(__tmp_1988 + 0.U) := __tmp_1989(7, 0)
        arrayRegFiles(__tmp_1988 + 1.U) := __tmp_1989(15, 8)
        arrayRegFiles(__tmp_1988 + 2.U) := __tmp_1989(23, 16)
        arrayRegFiles(__tmp_1988 + 3.U) := __tmp_1989(31, 24)

        CP := 274.U
      }

      is(297.U) {
        /*
        *(SP + (6 [SP])) = (240 [U32]) [signed, U32, 4]  // $sfLoc = (240 [U32])
        goto .298
        */


        val __tmp_1990 = SP + 6.U(16.W)
        val __tmp_1991 = (240.S(32.W)).asUInt
        arrayRegFiles(__tmp_1990 + 0.U) := __tmp_1991(7, 0)
        arrayRegFiles(__tmp_1990 + 1.U) := __tmp_1991(15, 8)
        arrayRegFiles(__tmp_1990 + 2.U) := __tmp_1991(23, 16)
        arrayRegFiles(__tmp_1990 + 3.U) := __tmp_1991(31, 24)

        CP := 298.U
      }

      is(298.U) {
        /*
        $0 = *(SP + (153 [SP])) [unsigned, U64, 8]  // $0 = r
        goto .299
        */


        val __tmp_1992 = (SP + 153.U(16.W)).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_1992 + 7.U),
          arrayRegFiles(__tmp_1992 + 6.U),
          arrayRegFiles(__tmp_1992 + 5.U),
          arrayRegFiles(__tmp_1992 + 4.U),
          arrayRegFiles(__tmp_1992 + 3.U),
          arrayRegFiles(__tmp_1992 + 2.U),
          arrayRegFiles(__tmp_1992 + 1.U),
          arrayRegFiles(__tmp_1992 + 0.U)
        ).asUInt

        CP := 299.U
      }

      is(299.U) {
        /*
        *(SP + (6 [SP])) = (205 [U32]) [signed, U32, 4]  // $sfLoc = (205 [U32])
        goto .300
        */


        val __tmp_1993 = SP + 6.U(16.W)
        val __tmp_1994 = (205.S(32.W)).asUInt
        arrayRegFiles(__tmp_1993 + 0.U) := __tmp_1994(7, 0)
        arrayRegFiles(__tmp_1993 + 1.U) := __tmp_1994(15, 8)
        arrayRegFiles(__tmp_1993 + 2.U) := __tmp_1994(23, 16)
        arrayRegFiles(__tmp_1993 + 3.U) := __tmp_1994(31, 24)

        CP := 300.U
      }

      is(300.U) {
        /*
        **(SP + (2 [SP])) = $0 [unsigned, U64, 8]  // $res = $0
        goto $ret@0
        */


        val __tmp_1995 = Cat(
          arrayRegFiles(SP + 2.U(16.W) + 1.U),
          arrayRegFiles(SP + 2.U(16.W) + 0.U)
        )
        val __tmp_1996 = (generalRegFiles(0.U)).asUInt
        arrayRegFiles(__tmp_1995 + 0.U) := __tmp_1996(7, 0)
        arrayRegFiles(__tmp_1995 + 1.U) := __tmp_1996(15, 8)
        arrayRegFiles(__tmp_1995 + 2.U) := __tmp_1996(23, 16)
        arrayRegFiles(__tmp_1995 + 3.U) := __tmp_1996(31, 24)
        arrayRegFiles(__tmp_1995 + 4.U) := __tmp_1996(39, 32)
        arrayRegFiles(__tmp_1995 + 5.U) := __tmp_1996(47, 40)
        arrayRegFiles(__tmp_1995 + 6.U) := __tmp_1996(55, 48)
        arrayRegFiles(__tmp_1995 + 7.U) := __tmp_1996(63, 56)

        CP := Cat(
          arrayRegFiles(SP + 0.U + 1.U),
          arrayRegFiles(SP + 0.U + 0.U)
        )

      }

      is(301.U) {
        /*
        $0 = *(SP - (32 [SP])) [unsigned, U64, 8]  // restore $0
        $1 = *(SP - (24 [SP])) [unsigned, U64, 8]  // restore $1
        $2 = *(SP - (16 [SP])) [unsigned, U64, 8]  // restore $2
        $3 = *(SP - (8 [SP])) [unsigned, U64, 8]  // restore $3
        $4 = **(SP + (2 [SP])) [unsigned, U64, 8]  // $4 = $ret
        undecl n: U64 [@91, 8], mask: anvil.PrinterIndex.U [@83, 8], index: anvil.PrinterIndex.U [@75, 8], buffer: SP [@73, 2], $sfCurrentId: SP [@71, 2], $sfDesc: IS[49, U8] [@10, 61], $sfLoc: U32 [@6, 4], $sfCaller: SP [@4, 2], $res: SP [@2, 2], $ret: CP [@0, 2]
        goto .302
        */


        val __tmp_1997 = (SP - 32.U(16.W)).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_1997 + 7.U),
          arrayRegFiles(__tmp_1997 + 6.U),
          arrayRegFiles(__tmp_1997 + 5.U),
          arrayRegFiles(__tmp_1997 + 4.U),
          arrayRegFiles(__tmp_1997 + 3.U),
          arrayRegFiles(__tmp_1997 + 2.U),
          arrayRegFiles(__tmp_1997 + 1.U),
          arrayRegFiles(__tmp_1997 + 0.U)
        ).asUInt

        val __tmp_1998 = (SP - 24.U(16.W)).asUInt
        generalRegFiles(1.U) := Cat(
          arrayRegFiles(__tmp_1998 + 7.U),
          arrayRegFiles(__tmp_1998 + 6.U),
          arrayRegFiles(__tmp_1998 + 5.U),
          arrayRegFiles(__tmp_1998 + 4.U),
          arrayRegFiles(__tmp_1998 + 3.U),
          arrayRegFiles(__tmp_1998 + 2.U),
          arrayRegFiles(__tmp_1998 + 1.U),
          arrayRegFiles(__tmp_1998 + 0.U)
        ).asUInt

        val __tmp_1999 = (SP - 16.U(16.W)).asUInt
        generalRegFiles(2.U) := Cat(
          arrayRegFiles(__tmp_1999 + 7.U),
          arrayRegFiles(__tmp_1999 + 6.U),
          arrayRegFiles(__tmp_1999 + 5.U),
          arrayRegFiles(__tmp_1999 + 4.U),
          arrayRegFiles(__tmp_1999 + 3.U),
          arrayRegFiles(__tmp_1999 + 2.U),
          arrayRegFiles(__tmp_1999 + 1.U),
          arrayRegFiles(__tmp_1999 + 0.U)
        ).asUInt

        val __tmp_2000 = (SP - 8.U(16.W)).asUInt
        generalRegFiles(3.U) := Cat(
          arrayRegFiles(__tmp_2000 + 7.U),
          arrayRegFiles(__tmp_2000 + 6.U),
          arrayRegFiles(__tmp_2000 + 5.U),
          arrayRegFiles(__tmp_2000 + 4.U),
          arrayRegFiles(__tmp_2000 + 3.U),
          arrayRegFiles(__tmp_2000 + 2.U),
          arrayRegFiles(__tmp_2000 + 1.U),
          arrayRegFiles(__tmp_2000 + 0.U)
        ).asUInt

        val __tmp_2001 = (Cat(
          arrayRegFiles(SP + 2.U(16.W) + 1.U),
          arrayRegFiles(SP + 2.U(16.W) + 0.U)
        )).asUInt
        generalRegFiles(4.U) := Cat(
          arrayRegFiles(__tmp_2001 + 7.U),
          arrayRegFiles(__tmp_2001 + 6.U),
          arrayRegFiles(__tmp_2001 + 5.U),
          arrayRegFiles(__tmp_2001 + 4.U),
          arrayRegFiles(__tmp_2001 + 3.U),
          arrayRegFiles(__tmp_2001 + 2.U),
          arrayRegFiles(__tmp_2001 + 1.U),
          arrayRegFiles(__tmp_2001 + 0.U)
        ).asUInt

        CP := 302.U
      }

      is(302.U) {
        /*
        SP = SP - 252
        goto .303
        */


        SP := SP - 252.U

        CP := 303.U
      }

      is(303.U) {
        /*
        *(SP + (204 [SP])) = $4 [signed, U64, 8]  // n = $4
        goto .304
        */


        val __tmp_2002 = SP + 204.U(16.W)
        val __tmp_2003 = (generalRegFiles(4.U)).asUInt
        arrayRegFiles(__tmp_2002 + 0.U) := __tmp_2003(7, 0)
        arrayRegFiles(__tmp_2002 + 1.U) := __tmp_2003(15, 8)
        arrayRegFiles(__tmp_2002 + 2.U) := __tmp_2003(23, 16)
        arrayRegFiles(__tmp_2002 + 3.U) := __tmp_2003(31, 24)
        arrayRegFiles(__tmp_2002 + 4.U) := __tmp_2003(39, 32)
        arrayRegFiles(__tmp_2002 + 5.U) := __tmp_2003(47, 40)
        arrayRegFiles(__tmp_2002 + 6.U) := __tmp_2003(55, 48)
        arrayRegFiles(__tmp_2002 + 7.U) := __tmp_2003(63, 56)

        CP := 304.U
      }

      is(304.U) {
        /*
        unalloc printU64$res@[659,15].C2D721FD: U64 [@212, 8]
        *(SP + (6 [SP])) = (660 [U32]) [signed, U32, 4]  // $sfLoc = (660 [U32])
        goto .305
        */


        val __tmp_2004 = SP + 6.U(16.W)
        val __tmp_2005 = (660.S(32.W)).asUInt
        arrayRegFiles(__tmp_2004 + 0.U) := __tmp_2005(7, 0)
        arrayRegFiles(__tmp_2004 + 1.U) := __tmp_2005(15, 8)
        arrayRegFiles(__tmp_2004 + 2.U) := __tmp_2005(23, 16)
        arrayRegFiles(__tmp_2004 + 3.U) := __tmp_2005(31, 24)

        CP := 305.U
      }

      is(305.U) {
        /*
        $0 = *(SP + (148 [SP])) [unsigned, U64, 8]  // $0 = r
        $1 = *(SP + (204 [SP])) [unsigned, U64, 8]  // $1 = n
        goto .306
        */


        val __tmp_2006 = (SP + 148.U(16.W)).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_2006 + 7.U),
          arrayRegFiles(__tmp_2006 + 6.U),
          arrayRegFiles(__tmp_2006 + 5.U),
          arrayRegFiles(__tmp_2006 + 4.U),
          arrayRegFiles(__tmp_2006 + 3.U),
          arrayRegFiles(__tmp_2006 + 2.U),
          arrayRegFiles(__tmp_2006 + 1.U),
          arrayRegFiles(__tmp_2006 + 0.U)
        ).asUInt

        val __tmp_2007 = (SP + 204.U(16.W)).asUInt
        generalRegFiles(1.U) := Cat(
          arrayRegFiles(__tmp_2007 + 7.U),
          arrayRegFiles(__tmp_2007 + 6.U),
          arrayRegFiles(__tmp_2007 + 5.U),
          arrayRegFiles(__tmp_2007 + 4.U),
          arrayRegFiles(__tmp_2007 + 3.U),
          arrayRegFiles(__tmp_2007 + 2.U),
          arrayRegFiles(__tmp_2007 + 1.U),
          arrayRegFiles(__tmp_2007 + 0.U)
        ).asUInt

        CP := 306.U
      }

      is(306.U) {
        /*
        $2 = ($0 + $1)
        goto .307
        */


        generalRegFiles(2.U) := generalRegFiles(0.U) + generalRegFiles(1.U)
        CP := 307.U
      }

      is(307.U) {
        /*
        *(SP + (148 [SP])) = $2 [signed, U64, 8]  // r = $2
        goto .308
        */


        val __tmp_2008 = SP + 148.U(16.W)
        val __tmp_2009 = (generalRegFiles(2.U)).asUInt
        arrayRegFiles(__tmp_2008 + 0.U) := __tmp_2009(7, 0)
        arrayRegFiles(__tmp_2008 + 1.U) := __tmp_2009(15, 8)
        arrayRegFiles(__tmp_2008 + 2.U) := __tmp_2009(23, 16)
        arrayRegFiles(__tmp_2008 + 3.U) := __tmp_2009(31, 24)
        arrayRegFiles(__tmp_2008 + 4.U) := __tmp_2009(39, 32)
        arrayRegFiles(__tmp_2008 + 5.U) := __tmp_2009(47, 40)
        arrayRegFiles(__tmp_2008 + 6.U) := __tmp_2009(55, 48)
        arrayRegFiles(__tmp_2008 + 7.U) := __tmp_2009(63, 56)

        CP := 308.U
      }

      is(308.U) {
        /*
        *(SP + (6 [SP])) = (661 [U32]) [signed, U32, 4]  // $sfLoc = (661 [U32])
        goto .309
        */


        val __tmp_2010 = SP + 6.U(16.W)
        val __tmp_2011 = (661.S(32.W)).asUInt
        arrayRegFiles(__tmp_2010 + 0.U) := __tmp_2011(7, 0)
        arrayRegFiles(__tmp_2010 + 1.U) := __tmp_2011(15, 8)
        arrayRegFiles(__tmp_2010 + 2.U) := __tmp_2011(23, 16)
        arrayRegFiles(__tmp_2010 + 3.U) := __tmp_2011(31, 24)

        CP := 309.U
      }

      is(309.U) {
        /*
        $0 = *(SP + (156 [SP])) [unsigned, anvil.PrinterIndex.U, 8]  // $0 = idx
        $1 = *(SP + (204 [SP])) [unsigned, U64, 8]  // $1 = n
        goto .310
        */


        val __tmp_2012 = (SP + 156.U(16.W)).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_2012 + 7.U),
          arrayRegFiles(__tmp_2012 + 6.U),
          arrayRegFiles(__tmp_2012 + 5.U),
          arrayRegFiles(__tmp_2012 + 4.U),
          arrayRegFiles(__tmp_2012 + 3.U),
          arrayRegFiles(__tmp_2012 + 2.U),
          arrayRegFiles(__tmp_2012 + 1.U),
          arrayRegFiles(__tmp_2012 + 0.U)
        ).asUInt

        val __tmp_2013 = (SP + 204.U(16.W)).asUInt
        generalRegFiles(1.U) := Cat(
          arrayRegFiles(__tmp_2013 + 7.U),
          arrayRegFiles(__tmp_2013 + 6.U),
          arrayRegFiles(__tmp_2013 + 5.U),
          arrayRegFiles(__tmp_2013 + 4.U),
          arrayRegFiles(__tmp_2013 + 3.U),
          arrayRegFiles(__tmp_2013 + 2.U),
          arrayRegFiles(__tmp_2013 + 1.U),
          arrayRegFiles(__tmp_2013 + 0.U)
        ).asUInt

        CP := 310.U
      }

      is(310.U) {
        /*
        $2 = ($1 as Z)
        goto .311
        */


        generalRegFiles(2.U) := (generalRegFiles(1.U).asSInt).asUInt
        CP := 311.U
      }

      is(311.U) {
        /*
        if ((0 [Z]) <= $2) goto .312 else goto .342
        */


        CP := Mux(((0.S(64.W) <= generalRegFiles(2.U).asSInt).asUInt.asUInt) === 1.U, 312.U, 342.U)
      }

      is(312.U) {
        /*
        *(SP + (6 [SP])) = (661 [U32]) [signed, U32, 4]  // $sfLoc = (661 [U32])
        goto .313
        */


        val __tmp_2014 = SP + 6.U(16.W)
        val __tmp_2015 = (661.S(32.W)).asUInt
        arrayRegFiles(__tmp_2014 + 0.U) := __tmp_2015(7, 0)
        arrayRegFiles(__tmp_2014 + 1.U) := __tmp_2015(15, 8)
        arrayRegFiles(__tmp_2014 + 2.U) := __tmp_2015(23, 16)
        arrayRegFiles(__tmp_2014 + 3.U) := __tmp_2015(31, 24)

        CP := 313.U
      }

      is(313.U) {
        /*
        $3 = ($2 as Z)
        goto .314
        */


        generalRegFiles(3.U) := (generalRegFiles(2.U).asSInt.asSInt).asUInt
        CP := 314.U
      }

      is(314.U) {
        /*
        $4 = ($0 + $3)
        goto .315
        */


        generalRegFiles(4.U) := generalRegFiles(0.U) + generalRegFiles(3.U)
        CP := 315.U
      }

      is(315.U) {
        /*
        *(SP + (156 [SP])) = $4 [signed, anvil.PrinterIndex.U, 8]  // idx = $4
        goto .316
        */


        val __tmp_2016 = SP + 156.U(16.W)
        val __tmp_2017 = (generalRegFiles(4.U)).asUInt
        arrayRegFiles(__tmp_2016 + 0.U) := __tmp_2017(7, 0)
        arrayRegFiles(__tmp_2016 + 1.U) := __tmp_2017(15, 8)
        arrayRegFiles(__tmp_2016 + 2.U) := __tmp_2017(23, 16)
        arrayRegFiles(__tmp_2016 + 3.U) := __tmp_2017(31, 24)
        arrayRegFiles(__tmp_2016 + 4.U) := __tmp_2017(39, 32)
        arrayRegFiles(__tmp_2016 + 5.U) := __tmp_2017(47, 40)
        arrayRegFiles(__tmp_2016 + 6.U) := __tmp_2017(55, 48)
        arrayRegFiles(__tmp_2016 + 7.U) := __tmp_2017(63, 56)

        CP := 316.U
      }

      is(316.U) {
        /*
        *(SP + (6 [SP])) = (662 [U32]) [signed, U32, 4]  // $sfLoc = (662 [U32])
        goto .317
        */


        val __tmp_2018 = SP + 6.U(16.W)
        val __tmp_2019 = (662.S(32.W)).asUInt
        arrayRegFiles(__tmp_2018 + 0.U) := __tmp_2019(7, 0)
        arrayRegFiles(__tmp_2018 + 1.U) := __tmp_2019(15, 8)
        arrayRegFiles(__tmp_2018 + 2.U) := __tmp_2019(23, 16)
        arrayRegFiles(__tmp_2018 + 3.U) := __tmp_2019(31, 24)

        CP := 317.U
      }

      is(317.U) {
        /*
        $0 = *(SP + (80 [SP])) [unsigned, MS[anvil.PrinterIndex.U, U8], 2]  // $0 = buffer
        $1 = *(SP + (156 [SP])) [unsigned, anvil.PrinterIndex.U, 8]  // $1 = idx
        $2 = *(SP + (92 [SP])) [unsigned, anvil.PrinterIndex.U, 8]  // $2 = mask
        goto .318
        */


        val __tmp_2020 = (SP + 80.U(16.W)).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_2020 + 1.U),
          arrayRegFiles(__tmp_2020 + 0.U)
        ).asUInt

        val __tmp_2021 = (SP + 156.U(16.W)).asUInt
        generalRegFiles(1.U) := Cat(
          arrayRegFiles(__tmp_2021 + 7.U),
          arrayRegFiles(__tmp_2021 + 6.U),
          arrayRegFiles(__tmp_2021 + 5.U),
          arrayRegFiles(__tmp_2021 + 4.U),
          arrayRegFiles(__tmp_2021 + 3.U),
          arrayRegFiles(__tmp_2021 + 2.U),
          arrayRegFiles(__tmp_2021 + 1.U),
          arrayRegFiles(__tmp_2021 + 0.U)
        ).asUInt

        val __tmp_2022 = (SP + 92.U(16.W)).asUInt
        generalRegFiles(2.U) := Cat(
          arrayRegFiles(__tmp_2022 + 7.U),
          arrayRegFiles(__tmp_2022 + 6.U),
          arrayRegFiles(__tmp_2022 + 5.U),
          arrayRegFiles(__tmp_2022 + 4.U),
          arrayRegFiles(__tmp_2022 + 3.U),
          arrayRegFiles(__tmp_2022 + 2.U),
          arrayRegFiles(__tmp_2022 + 1.U),
          arrayRegFiles(__tmp_2022 + 0.U)
        ).asUInt

        CP := 318.U
      }

      is(318.U) {
        /*
        $3 = ($1 & $2)
        goto .319
        */


        generalRegFiles(3.U) := generalRegFiles(1.U) & generalRegFiles(2.U)
        CP := 319.U
      }

      is(319.U) {
        /*
        *(($0 + (12 [SP])) + ($3 as SP)) = (41 [U8]) [unsigned, U8, 1]  // $0($3) = (41 [U8])
        goto .320
        */


        val __tmp_2023 = generalRegFiles(0.U) + 12.U(16.W) + generalRegFiles(3.U).asUInt
        val __tmp_2024 = (41.U(8.W)).asUInt
        arrayRegFiles(__tmp_2023 + 0.U) := __tmp_2024(7, 0)

        CP := 320.U
      }

      is(320.U) {
        /*
        *(SP + (6 [SP])) = (663 [U32]) [signed, U32, 4]  // $sfLoc = (663 [U32])
        goto .321
        */


        val __tmp_2025 = SP + 6.U(16.W)
        val __tmp_2026 = (663.S(32.W)).asUInt
        arrayRegFiles(__tmp_2025 + 0.U) := __tmp_2026(7, 0)
        arrayRegFiles(__tmp_2025 + 1.U) := __tmp_2026(15, 8)
        arrayRegFiles(__tmp_2025 + 2.U) := __tmp_2026(23, 16)
        arrayRegFiles(__tmp_2025 + 3.U) := __tmp_2026(31, 24)

        CP := 321.U
      }

      is(321.U) {
        /*
        $0 = *(SP + (80 [SP])) [unsigned, MS[anvil.PrinterIndex.U, U8], 2]  // $0 = buffer
        $1 = *(SP + (156 [SP])) [unsigned, anvil.PrinterIndex.U, 8]  // $1 = idx
        goto .322
        */


        val __tmp_2027 = (SP + 80.U(16.W)).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_2027 + 1.U),
          arrayRegFiles(__tmp_2027 + 0.U)
        ).asUInt

        val __tmp_2028 = (SP + 156.U(16.W)).asUInt
        generalRegFiles(1.U) := Cat(
          arrayRegFiles(__tmp_2028 + 7.U),
          arrayRegFiles(__tmp_2028 + 6.U),
          arrayRegFiles(__tmp_2028 + 5.U),
          arrayRegFiles(__tmp_2028 + 4.U),
          arrayRegFiles(__tmp_2028 + 3.U),
          arrayRegFiles(__tmp_2028 + 2.U),
          arrayRegFiles(__tmp_2028 + 1.U),
          arrayRegFiles(__tmp_2028 + 0.U)
        ).asUInt

        CP := 322.U
      }

      is(322.U) {
        /*
        $2 = ($1 + (1 [anvil.PrinterIndex.U]))
        goto .323
        */


        generalRegFiles(2.U) := generalRegFiles(1.U) + 1.U(64.W)
        CP := 323.U
      }

      is(323.U) {
        /*
        $3 = *(SP + (92 [SP])) [unsigned, anvil.PrinterIndex.U, 8]  // $3 = mask
        goto .324
        */


        val __tmp_2029 = (SP + 92.U(16.W)).asUInt
        generalRegFiles(3.U) := Cat(
          arrayRegFiles(__tmp_2029 + 7.U),
          arrayRegFiles(__tmp_2029 + 6.U),
          arrayRegFiles(__tmp_2029 + 5.U),
          arrayRegFiles(__tmp_2029 + 4.U),
          arrayRegFiles(__tmp_2029 + 3.U),
          arrayRegFiles(__tmp_2029 + 2.U),
          arrayRegFiles(__tmp_2029 + 1.U),
          arrayRegFiles(__tmp_2029 + 0.U)
        ).asUInt

        CP := 324.U
      }

      is(324.U) {
        /*
        $4 = ($2 & $3)
        goto .325
        */


        generalRegFiles(4.U) := generalRegFiles(2.U) & generalRegFiles(3.U)
        CP := 325.U
      }

      is(325.U) {
        /*
        *(($0 + (12 [SP])) + ($4 as SP)) = (10 [U8]) [unsigned, U8, 1]  // $0($4) = (10 [U8])
        goto .326
        */


        val __tmp_2030 = generalRegFiles(0.U) + 12.U(16.W) + generalRegFiles(4.U).asUInt
        val __tmp_2031 = (10.U(8.W)).asUInt
        arrayRegFiles(__tmp_2030 + 0.U) := __tmp_2031(7, 0)

        CP := 326.U
      }

      is(326.U) {
        /*
        *(SP + (6 [SP])) = (664 [U32]) [signed, U32, 4]  // $sfLoc = (664 [U32])
        goto .327
        */


        val __tmp_2032 = SP + 6.U(16.W)
        val __tmp_2033 = (664.S(32.W)).asUInt
        arrayRegFiles(__tmp_2032 + 0.U) := __tmp_2033(7, 0)
        arrayRegFiles(__tmp_2032 + 1.U) := __tmp_2033(15, 8)
        arrayRegFiles(__tmp_2032 + 2.U) := __tmp_2033(23, 16)
        arrayRegFiles(__tmp_2032 + 3.U) := __tmp_2033(31, 24)

        CP := 327.U
      }

      is(327.U) {
        /*
        $0 = *(SP + (148 [SP])) [unsigned, U64, 8]  // $0 = r
        goto .328
        */


        val __tmp_2034 = (SP + 148.U(16.W)).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_2034 + 7.U),
          arrayRegFiles(__tmp_2034 + 6.U),
          arrayRegFiles(__tmp_2034 + 5.U),
          arrayRegFiles(__tmp_2034 + 4.U),
          arrayRegFiles(__tmp_2034 + 3.U),
          arrayRegFiles(__tmp_2034 + 2.U),
          arrayRegFiles(__tmp_2034 + 1.U),
          arrayRegFiles(__tmp_2034 + 0.U)
        ).asUInt

        CP := 328.U
      }

      is(328.U) {
        /*
        $1 = ($0 + (2 [U64]))
        goto .329
        */


        generalRegFiles(1.U) := generalRegFiles(0.U) + 2.U(64.W)
        CP := 329.U
      }

      is(329.U) {
        /*
        *(SP + (148 [SP])) = $1 [signed, U64, 8]  // r = $1
        goto .330
        */


        val __tmp_2035 = SP + 148.U(16.W)
        val __tmp_2036 = (generalRegFiles(1.U)).asUInt
        arrayRegFiles(__tmp_2035 + 0.U) := __tmp_2036(7, 0)
        arrayRegFiles(__tmp_2035 + 1.U) := __tmp_2036(15, 8)
        arrayRegFiles(__tmp_2035 + 2.U) := __tmp_2036(23, 16)
        arrayRegFiles(__tmp_2035 + 3.U) := __tmp_2036(31, 24)
        arrayRegFiles(__tmp_2035 + 4.U) := __tmp_2036(39, 32)
        arrayRegFiles(__tmp_2035 + 5.U) := __tmp_2036(47, 40)
        arrayRegFiles(__tmp_2035 + 6.U) := __tmp_2036(55, 48)
        arrayRegFiles(__tmp_2035 + 7.U) := __tmp_2036(63, 56)

        CP := 330.U
      }

      is(330.U) {
        /*
        *(SP + (6 [SP])) = (665 [U32]) [signed, U32, 4]  // $sfLoc = (665 [U32])
        goto .331
        */


        val __tmp_2037 = SP + 6.U(16.W)
        val __tmp_2038 = (665.S(32.W)).asUInt
        arrayRegFiles(__tmp_2037 + 0.U) := __tmp_2038(7, 0)
        arrayRegFiles(__tmp_2037 + 1.U) := __tmp_2038(15, 8)
        arrayRegFiles(__tmp_2037 + 2.U) := __tmp_2038(23, 16)
        arrayRegFiles(__tmp_2037 + 3.U) := __tmp_2038(31, 24)

        CP := 331.U
      }

      is(331.U) {
        /*
        $0 = *(SP + (156 [SP])) [unsigned, anvil.PrinterIndex.U, 8]  // $0 = idx
        goto .332
        */


        val __tmp_2039 = (SP + 156.U(16.W)).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_2039 + 7.U),
          arrayRegFiles(__tmp_2039 + 6.U),
          arrayRegFiles(__tmp_2039 + 5.U),
          arrayRegFiles(__tmp_2039 + 4.U),
          arrayRegFiles(__tmp_2039 + 3.U),
          arrayRegFiles(__tmp_2039 + 2.U),
          arrayRegFiles(__tmp_2039 + 1.U),
          arrayRegFiles(__tmp_2039 + 0.U)
        ).asUInt

        CP := 332.U
      }

      is(332.U) {
        /*
        $1 = ($0 + (2 [anvil.PrinterIndex.U]))
        goto .333
        */


        generalRegFiles(1.U) := generalRegFiles(0.U) + 2.U(64.W)
        CP := 333.U
      }

      is(333.U) {
        /*
        *(SP + (156 [SP])) = $1 [signed, anvil.PrinterIndex.U, 8]  // idx = $1
        goto .334
        */


        val __tmp_2040 = SP + 156.U(16.W)
        val __tmp_2041 = (generalRegFiles(1.U)).asUInt
        arrayRegFiles(__tmp_2040 + 0.U) := __tmp_2041(7, 0)
        arrayRegFiles(__tmp_2040 + 1.U) := __tmp_2041(15, 8)
        arrayRegFiles(__tmp_2040 + 2.U) := __tmp_2041(23, 16)
        arrayRegFiles(__tmp_2040 + 3.U) := __tmp_2041(31, 24)
        arrayRegFiles(__tmp_2040 + 4.U) := __tmp_2041(39, 32)
        arrayRegFiles(__tmp_2040 + 5.U) := __tmp_2041(47, 40)
        arrayRegFiles(__tmp_2040 + 6.U) := __tmp_2041(55, 48)
        arrayRegFiles(__tmp_2040 + 7.U) := __tmp_2041(63, 56)

        CP := 334.U
      }

      is(334.U) {
        /*
        *(SP + (6 [SP])) = (666 [U32]) [signed, U32, 4]  // $sfLoc = (666 [U32])
        goto .335
        */


        val __tmp_2042 = SP + 6.U(16.W)
        val __tmp_2043 = (666.S(32.W)).asUInt
        arrayRegFiles(__tmp_2042 + 0.U) := __tmp_2043(7, 0)
        arrayRegFiles(__tmp_2042 + 1.U) := __tmp_2043(15, 8)
        arrayRegFiles(__tmp_2042 + 2.U) := __tmp_2043(23, 16)
        arrayRegFiles(__tmp_2042 + 3.U) := __tmp_2043(31, 24)

        CP := 335.U
      }

      is(335.U) {
        /*
        $0 = *(SP + (90 [SP])) [unsigned, MS[anvil.PrinterIndex.U, U8], 2]  // $0 = memory
        $1 = *(SP + (140 [SP])) [unsigned, anvil.PrinterIndex.U, 8]  // $1 = sfCaller
        $2 = *(SP + (108 [SP])) [unsigned, anvil.PrinterIndex.U, 8]  // $2 = typeShaSize
        goto .336
        */


        val __tmp_2044 = (SP + 90.U(16.W)).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_2044 + 1.U),
          arrayRegFiles(__tmp_2044 + 0.U)
        ).asUInt

        val __tmp_2045 = (SP + 140.U(16.W)).asUInt
        generalRegFiles(1.U) := Cat(
          arrayRegFiles(__tmp_2045 + 7.U),
          arrayRegFiles(__tmp_2045 + 6.U),
          arrayRegFiles(__tmp_2045 + 5.U),
          arrayRegFiles(__tmp_2045 + 4.U),
          arrayRegFiles(__tmp_2045 + 3.U),
          arrayRegFiles(__tmp_2045 + 2.U),
          arrayRegFiles(__tmp_2045 + 1.U),
          arrayRegFiles(__tmp_2045 + 0.U)
        ).asUInt

        val __tmp_2046 = (SP + 108.U(16.W)).asUInt
        generalRegFiles(2.U) := Cat(
          arrayRegFiles(__tmp_2046 + 7.U),
          arrayRegFiles(__tmp_2046 + 6.U),
          arrayRegFiles(__tmp_2046 + 5.U),
          arrayRegFiles(__tmp_2046 + 4.U),
          arrayRegFiles(__tmp_2046 + 3.U),
          arrayRegFiles(__tmp_2046 + 2.U),
          arrayRegFiles(__tmp_2046 + 1.U),
          arrayRegFiles(__tmp_2046 + 0.U)
        ).asUInt

        CP := 336.U
      }

      is(336.U) {
        /*
        $3 = ($1 - $2)
        goto .337
        */


        generalRegFiles(3.U) := generalRegFiles(1.U) - generalRegFiles(2.U)
        CP := 337.U
      }

      is(337.U) {
        /*
        $4 = *(SP + (124 [SP])) [unsigned, anvil.PrinterIndex.U, 8]  // $4 = sizeSize
        goto .338
        */


        val __tmp_2047 = (SP + 124.U(16.W)).asUInt
        generalRegFiles(4.U) := Cat(
          arrayRegFiles(__tmp_2047 + 7.U),
          arrayRegFiles(__tmp_2047 + 6.U),
          arrayRegFiles(__tmp_2047 + 5.U),
          arrayRegFiles(__tmp_2047 + 4.U),
          arrayRegFiles(__tmp_2047 + 3.U),
          arrayRegFiles(__tmp_2047 + 2.U),
          arrayRegFiles(__tmp_2047 + 1.U),
          arrayRegFiles(__tmp_2047 + 0.U)
        ).asUInt

        CP := 338.U
      }

      is(338.U) {
        /*
        $5 = ($3 - $4)
        goto .339
        */


        generalRegFiles(5.U) := generalRegFiles(3.U) - generalRegFiles(4.U)
        CP := 339.U
      }

      is(339.U) {
        /*
        $6 = *(SP + (100 [SP])) [unsigned, anvil.PrinterIndex.U, 8]  // $6 = spSize
        alloc load$res@[666,18].2F90B465: anvil.PrinterIndex.U [@212, 8]
        goto .340
        */


        val __tmp_2048 = (SP + 100.U(16.W)).asUInt
        generalRegFiles(6.U) := Cat(
          arrayRegFiles(__tmp_2048 + 7.U),
          arrayRegFiles(__tmp_2048 + 6.U),
          arrayRegFiles(__tmp_2048 + 5.U),
          arrayRegFiles(__tmp_2048 + 4.U),
          arrayRegFiles(__tmp_2048 + 3.U),
          arrayRegFiles(__tmp_2048 + 2.U),
          arrayRegFiles(__tmp_2048 + 1.U),
          arrayRegFiles(__tmp_2048 + 0.U)
        ).asUInt

        CP := 340.U
      }

      is(340.U) {
        /*
        SP = SP + 276
        goto .341
        */


        SP := SP + 276.U

        CP := 341.U
      }

      is(341.U) {
        /*
        decl $ret: CP [@0, 2], $res: SP [@2, 2], $sfCaller: SP [@4, 2], $sfLoc: U32 [@6, 4], $sfDesc: IS[45, U8] [@10, 57], $sfCurrentId: SP [@67, 2], memory: SP [@69, 2], offset: anvil.PrinterIndex.U [@71, 8], size: anvil.PrinterIndex.U [@79, 8]
        *SP = (355 [CP]) [unsigned, CP, 2]  // $ret@0 = 2230
        *(SP + (2 [SP])) = (SP - (64 [SP])) [unsigned, SP, 2]  // $res@2 = -64
        *(SP + (4 [SP])) = (SP - (272 [SP])) [unsigned, SP, 2]  // $sfCaller@4 = -272
        *(SP + (67 [SP])) = (SP + (4 [SP])) [unsigned, SP, 2]  // $sfCurrentId@67 = 4
        *(SP + (69 [SP])) = $0 [unsigned, SP, 2]  // memory = $0
        *(SP + (71 [SP])) = $5 [unsigned, anvil.PrinterIndex.U, 8]  // offset = $5
        *(SP + (79 [SP])) = $6 [unsigned, anvil.PrinterIndex.U, 8]  // size = $6
        *(SP - (56 [SP])) = $0 [unsigned, U64, 8]  // save $0
        *(SP - (48 [SP])) = $1 [unsigned, U64, 8]  // save $1
        *(SP - (40 [SP])) = $2 [unsigned, U64, 8]  // save $2
        *(SP - (32 [SP])) = $3 [unsigned, U64, 8]  // save $3
        *(SP - (24 [SP])) = $4 [unsigned, U64, 8]  // save $4
        *(SP - (16 [SP])) = $5 [unsigned, U64, 8]  // save $5
        *(SP - (8 [SP])) = $6 [unsigned, U64, 8]  // save $6
        goto .52
        */


        val __tmp_2049 = SP
        val __tmp_2050 = (355.U(16.W)).asUInt
        arrayRegFiles(__tmp_2049 + 0.U) := __tmp_2050(7, 0)
        arrayRegFiles(__tmp_2049 + 1.U) := __tmp_2050(15, 8)

        val __tmp_2051 = SP + 2.U(16.W)
        val __tmp_2052 = (SP - 64.U(16.W)).asUInt
        arrayRegFiles(__tmp_2051 + 0.U) := __tmp_2052(7, 0)
        arrayRegFiles(__tmp_2051 + 1.U) := __tmp_2052(15, 8)

        val __tmp_2053 = SP + 4.U(16.W)
        val __tmp_2054 = (SP - 272.U(16.W)).asUInt
        arrayRegFiles(__tmp_2053 + 0.U) := __tmp_2054(7, 0)
        arrayRegFiles(__tmp_2053 + 1.U) := __tmp_2054(15, 8)

        val __tmp_2055 = SP + 67.U(16.W)
        val __tmp_2056 = (SP + 4.U(16.W)).asUInt
        arrayRegFiles(__tmp_2055 + 0.U) := __tmp_2056(7, 0)
        arrayRegFiles(__tmp_2055 + 1.U) := __tmp_2056(15, 8)

        val __tmp_2057 = SP + 69.U(16.W)
        val __tmp_2058 = (generalRegFiles(0.U)).asUInt
        arrayRegFiles(__tmp_2057 + 0.U) := __tmp_2058(7, 0)
        arrayRegFiles(__tmp_2057 + 1.U) := __tmp_2058(15, 8)

        val __tmp_2059 = SP + 71.U(16.W)
        val __tmp_2060 = (generalRegFiles(5.U)).asUInt
        arrayRegFiles(__tmp_2059 + 0.U) := __tmp_2060(7, 0)
        arrayRegFiles(__tmp_2059 + 1.U) := __tmp_2060(15, 8)
        arrayRegFiles(__tmp_2059 + 2.U) := __tmp_2060(23, 16)
        arrayRegFiles(__tmp_2059 + 3.U) := __tmp_2060(31, 24)
        arrayRegFiles(__tmp_2059 + 4.U) := __tmp_2060(39, 32)
        arrayRegFiles(__tmp_2059 + 5.U) := __tmp_2060(47, 40)
        arrayRegFiles(__tmp_2059 + 6.U) := __tmp_2060(55, 48)
        arrayRegFiles(__tmp_2059 + 7.U) := __tmp_2060(63, 56)

        val __tmp_2061 = SP + 79.U(16.W)
        val __tmp_2062 = (generalRegFiles(6.U)).asUInt
        arrayRegFiles(__tmp_2061 + 0.U) := __tmp_2062(7, 0)
        arrayRegFiles(__tmp_2061 + 1.U) := __tmp_2062(15, 8)
        arrayRegFiles(__tmp_2061 + 2.U) := __tmp_2062(23, 16)
        arrayRegFiles(__tmp_2061 + 3.U) := __tmp_2062(31, 24)
        arrayRegFiles(__tmp_2061 + 4.U) := __tmp_2062(39, 32)
        arrayRegFiles(__tmp_2061 + 5.U) := __tmp_2062(47, 40)
        arrayRegFiles(__tmp_2061 + 6.U) := __tmp_2062(55, 48)
        arrayRegFiles(__tmp_2061 + 7.U) := __tmp_2062(63, 56)

        val __tmp_2063 = SP - 56.U(16.W)
        val __tmp_2064 = (generalRegFiles(0.U)).asUInt
        arrayRegFiles(__tmp_2063 + 0.U) := __tmp_2064(7, 0)
        arrayRegFiles(__tmp_2063 + 1.U) := __tmp_2064(15, 8)
        arrayRegFiles(__tmp_2063 + 2.U) := __tmp_2064(23, 16)
        arrayRegFiles(__tmp_2063 + 3.U) := __tmp_2064(31, 24)
        arrayRegFiles(__tmp_2063 + 4.U) := __tmp_2064(39, 32)
        arrayRegFiles(__tmp_2063 + 5.U) := __tmp_2064(47, 40)
        arrayRegFiles(__tmp_2063 + 6.U) := __tmp_2064(55, 48)
        arrayRegFiles(__tmp_2063 + 7.U) := __tmp_2064(63, 56)

        val __tmp_2065 = SP - 48.U(16.W)
        val __tmp_2066 = (generalRegFiles(1.U)).asUInt
        arrayRegFiles(__tmp_2065 + 0.U) := __tmp_2066(7, 0)
        arrayRegFiles(__tmp_2065 + 1.U) := __tmp_2066(15, 8)
        arrayRegFiles(__tmp_2065 + 2.U) := __tmp_2066(23, 16)
        arrayRegFiles(__tmp_2065 + 3.U) := __tmp_2066(31, 24)
        arrayRegFiles(__tmp_2065 + 4.U) := __tmp_2066(39, 32)
        arrayRegFiles(__tmp_2065 + 5.U) := __tmp_2066(47, 40)
        arrayRegFiles(__tmp_2065 + 6.U) := __tmp_2066(55, 48)
        arrayRegFiles(__tmp_2065 + 7.U) := __tmp_2066(63, 56)

        val __tmp_2067 = SP - 40.U(16.W)
        val __tmp_2068 = (generalRegFiles(2.U)).asUInt
        arrayRegFiles(__tmp_2067 + 0.U) := __tmp_2068(7, 0)
        arrayRegFiles(__tmp_2067 + 1.U) := __tmp_2068(15, 8)
        arrayRegFiles(__tmp_2067 + 2.U) := __tmp_2068(23, 16)
        arrayRegFiles(__tmp_2067 + 3.U) := __tmp_2068(31, 24)
        arrayRegFiles(__tmp_2067 + 4.U) := __tmp_2068(39, 32)
        arrayRegFiles(__tmp_2067 + 5.U) := __tmp_2068(47, 40)
        arrayRegFiles(__tmp_2067 + 6.U) := __tmp_2068(55, 48)
        arrayRegFiles(__tmp_2067 + 7.U) := __tmp_2068(63, 56)

        val __tmp_2069 = SP - 32.U(16.W)
        val __tmp_2070 = (generalRegFiles(3.U)).asUInt
        arrayRegFiles(__tmp_2069 + 0.U) := __tmp_2070(7, 0)
        arrayRegFiles(__tmp_2069 + 1.U) := __tmp_2070(15, 8)
        arrayRegFiles(__tmp_2069 + 2.U) := __tmp_2070(23, 16)
        arrayRegFiles(__tmp_2069 + 3.U) := __tmp_2070(31, 24)
        arrayRegFiles(__tmp_2069 + 4.U) := __tmp_2070(39, 32)
        arrayRegFiles(__tmp_2069 + 5.U) := __tmp_2070(47, 40)
        arrayRegFiles(__tmp_2069 + 6.U) := __tmp_2070(55, 48)
        arrayRegFiles(__tmp_2069 + 7.U) := __tmp_2070(63, 56)

        val __tmp_2071 = SP - 24.U(16.W)
        val __tmp_2072 = (generalRegFiles(4.U)).asUInt
        arrayRegFiles(__tmp_2071 + 0.U) := __tmp_2072(7, 0)
        arrayRegFiles(__tmp_2071 + 1.U) := __tmp_2072(15, 8)
        arrayRegFiles(__tmp_2071 + 2.U) := __tmp_2072(23, 16)
        arrayRegFiles(__tmp_2071 + 3.U) := __tmp_2072(31, 24)
        arrayRegFiles(__tmp_2071 + 4.U) := __tmp_2072(39, 32)
        arrayRegFiles(__tmp_2071 + 5.U) := __tmp_2072(47, 40)
        arrayRegFiles(__tmp_2071 + 6.U) := __tmp_2072(55, 48)
        arrayRegFiles(__tmp_2071 + 7.U) := __tmp_2072(63, 56)

        val __tmp_2073 = SP - 16.U(16.W)
        val __tmp_2074 = (generalRegFiles(5.U)).asUInt
        arrayRegFiles(__tmp_2073 + 0.U) := __tmp_2074(7, 0)
        arrayRegFiles(__tmp_2073 + 1.U) := __tmp_2074(15, 8)
        arrayRegFiles(__tmp_2073 + 2.U) := __tmp_2074(23, 16)
        arrayRegFiles(__tmp_2073 + 3.U) := __tmp_2074(31, 24)
        arrayRegFiles(__tmp_2073 + 4.U) := __tmp_2074(39, 32)
        arrayRegFiles(__tmp_2073 + 5.U) := __tmp_2074(47, 40)
        arrayRegFiles(__tmp_2073 + 6.U) := __tmp_2074(55, 48)
        arrayRegFiles(__tmp_2073 + 7.U) := __tmp_2074(63, 56)

        val __tmp_2075 = SP - 8.U(16.W)
        val __tmp_2076 = (generalRegFiles(6.U)).asUInt
        arrayRegFiles(__tmp_2075 + 0.U) := __tmp_2076(7, 0)
        arrayRegFiles(__tmp_2075 + 1.U) := __tmp_2076(15, 8)
        arrayRegFiles(__tmp_2075 + 2.U) := __tmp_2076(23, 16)
        arrayRegFiles(__tmp_2075 + 3.U) := __tmp_2076(31, 24)
        arrayRegFiles(__tmp_2075 + 4.U) := __tmp_2076(39, 32)
        arrayRegFiles(__tmp_2075 + 5.U) := __tmp_2076(47, 40)
        arrayRegFiles(__tmp_2075 + 6.U) := __tmp_2076(55, 48)
        arrayRegFiles(__tmp_2075 + 7.U) := __tmp_2076(63, 56)

        CP := 52.U
      }

      is(342.U) {
        /*
        *(SP + (6 [SP])) = (661 [U32]) [signed, U32, 4]  // $sfLoc = (661 [U32])
        goto .343
        */


        val __tmp_2077 = SP + 6.U(16.W)
        val __tmp_2078 = (661.S(32.W)).asUInt
        arrayRegFiles(__tmp_2077 + 0.U) := __tmp_2078(7, 0)
        arrayRegFiles(__tmp_2077 + 1.U) := __tmp_2078(15, 8)
        arrayRegFiles(__tmp_2077 + 2.U) := __tmp_2078(23, 16)
        arrayRegFiles(__tmp_2077 + 3.U) := __tmp_2078(31, 24)

        CP := 343.U
      }

      is(343.U) {
        /*
        *((*(22 [SP]) + (12 [SP])) + ((DP & (127 [DP])) as SP)) = (79 [U8]) [unsigned, U8, 1]  // $display((DP & (127 [DP]))) = (79 [U8])
        *((*(22 [SP]) + (12 [SP])) + (((DP + (1 [DP])) & (127 [DP])) as SP)) = (117 [U8]) [unsigned, U8, 1]  // $display(((DP + (1 [DP])) & (127 [DP]))) = (117 [U8])
        *((*(22 [SP]) + (12 [SP])) + (((DP + (2 [DP])) & (127 [DP])) as SP)) = (116 [U8]) [unsigned, U8, 1]  // $display(((DP + (2 [DP])) & (127 [DP]))) = (116 [U8])
        *((*(22 [SP]) + (12 [SP])) + (((DP + (3 [DP])) & (127 [DP])) as SP)) = (32 [U8]) [unsigned, U8, 1]  // $display(((DP + (3 [DP])) & (127 [DP]))) = (32 [U8])
        *((*(22 [SP]) + (12 [SP])) + (((DP + (4 [DP])) & (127 [DP])) as SP)) = (111 [U8]) [unsigned, U8, 1]  // $display(((DP + (4 [DP])) & (127 [DP]))) = (111 [U8])
        *((*(22 [SP]) + (12 [SP])) + (((DP + (5 [DP])) & (127 [DP])) as SP)) = (102 [U8]) [unsigned, U8, 1]  // $display(((DP + (5 [DP])) & (127 [DP]))) = (102 [U8])
        *((*(22 [SP]) + (12 [SP])) + (((DP + (6 [DP])) & (127 [DP])) as SP)) = (32 [U8]) [unsigned, U8, 1]  // $display(((DP + (6 [DP])) & (127 [DP]))) = (32 [U8])
        *((*(22 [SP]) + (12 [SP])) + (((DP + (7 [DP])) & (127 [DP])) as SP)) = (98 [U8]) [unsigned, U8, 1]  // $display(((DP + (7 [DP])) & (127 [DP]))) = (98 [U8])
        *((*(22 [SP]) + (12 [SP])) + (((DP + (8 [DP])) & (127 [DP])) as SP)) = (111 [U8]) [unsigned, U8, 1]  // $display(((DP + (8 [DP])) & (127 [DP]))) = (111 [U8])
        *((*(22 [SP]) + (12 [SP])) + (((DP + (9 [DP])) & (127 [DP])) as SP)) = (117 [U8]) [unsigned, U8, 1]  // $display(((DP + (9 [DP])) & (127 [DP]))) = (117 [U8])
        *((*(22 [SP]) + (12 [SP])) + (((DP + (10 [DP])) & (127 [DP])) as SP)) = (110 [U8]) [unsigned, U8, 1]  // $display(((DP + (10 [DP])) & (127 [DP]))) = (110 [U8])
        *((*(22 [SP]) + (12 [SP])) + (((DP + (11 [DP])) & (127 [DP])) as SP)) = (100 [U8]) [unsigned, U8, 1]  // $display(((DP + (11 [DP])) & (127 [DP]))) = (100 [U8])
        *((*(22 [SP]) + (12 [SP])) + (((DP + (12 [DP])) & (127 [DP])) as SP)) = (32 [U8]) [unsigned, U8, 1]  // $display(((DP + (12 [DP])) & (127 [DP]))) = (32 [U8])
        *((*(22 [SP]) + (12 [SP])) + (((DP + (13 [DP])) & (127 [DP])) as SP)) = (97 [U8]) [unsigned, U8, 1]  // $display(((DP + (13 [DP])) & (127 [DP]))) = (97 [U8])
        *((*(22 [SP]) + (12 [SP])) + (((DP + (14 [DP])) & (127 [DP])) as SP)) = (110 [U8]) [unsigned, U8, 1]  // $display(((DP + (14 [DP])) & (127 [DP]))) = (110 [U8])
        *((*(22 [SP]) + (12 [SP])) + (((DP + (15 [DP])) & (127 [DP])) as SP)) = (118 [U8]) [unsigned, U8, 1]  // $display(((DP + (15 [DP])) & (127 [DP]))) = (118 [U8])
        *((*(22 [SP]) + (12 [SP])) + (((DP + (16 [DP])) & (127 [DP])) as SP)) = (105 [U8]) [unsigned, U8, 1]  // $display(((DP + (16 [DP])) & (127 [DP]))) = (105 [U8])
        *((*(22 [SP]) + (12 [SP])) + (((DP + (17 [DP])) & (127 [DP])) as SP)) = (108 [U8]) [unsigned, U8, 1]  // $display(((DP + (17 [DP])) & (127 [DP]))) = (108 [U8])
        *((*(22 [SP]) + (12 [SP])) + (((DP + (18 [DP])) & (127 [DP])) as SP)) = (46 [U8]) [unsigned, U8, 1]  // $display(((DP + (18 [DP])) & (127 [DP]))) = (46 [U8])
        *((*(22 [SP]) + (12 [SP])) + (((DP + (19 [DP])) & (127 [DP])) as SP)) = (80 [U8]) [unsigned, U8, 1]  // $display(((DP + (19 [DP])) & (127 [DP]))) = (80 [U8])
        *((*(22 [SP]) + (12 [SP])) + (((DP + (20 [DP])) & (127 [DP])) as SP)) = (114 [U8]) [unsigned, U8, 1]  // $display(((DP + (20 [DP])) & (127 [DP]))) = (114 [U8])
        *((*(22 [SP]) + (12 [SP])) + (((DP + (21 [DP])) & (127 [DP])) as SP)) = (105 [U8]) [unsigned, U8, 1]  // $display(((DP + (21 [DP])) & (127 [DP]))) = (105 [U8])
        *((*(22 [SP]) + (12 [SP])) + (((DP + (22 [DP])) & (127 [DP])) as SP)) = (110 [U8]) [unsigned, U8, 1]  // $display(((DP + (22 [DP])) & (127 [DP]))) = (110 [U8])
        *((*(22 [SP]) + (12 [SP])) + (((DP + (23 [DP])) & (127 [DP])) as SP)) = (116 [U8]) [unsigned, U8, 1]  // $display(((DP + (23 [DP])) & (127 [DP]))) = (116 [U8])
        *((*(22 [SP]) + (12 [SP])) + (((DP + (24 [DP])) & (127 [DP])) as SP)) = (101 [U8]) [unsigned, U8, 1]  // $display(((DP + (24 [DP])) & (127 [DP]))) = (101 [U8])
        *((*(22 [SP]) + (12 [SP])) + (((DP + (25 [DP])) & (127 [DP])) as SP)) = (114 [U8]) [unsigned, U8, 1]  // $display(((DP + (25 [DP])) & (127 [DP]))) = (114 [U8])
        *((*(22 [SP]) + (12 [SP])) + (((DP + (26 [DP])) & (127 [DP])) as SP)) = (73 [U8]) [unsigned, U8, 1]  // $display(((DP + (26 [DP])) & (127 [DP]))) = (73 [U8])
        *((*(22 [SP]) + (12 [SP])) + (((DP + (27 [DP])) & (127 [DP])) as SP)) = (110 [U8]) [unsigned, U8, 1]  // $display(((DP + (27 [DP])) & (127 [DP]))) = (110 [U8])
        *((*(22 [SP]) + (12 [SP])) + (((DP + (28 [DP])) & (127 [DP])) as SP)) = (100 [U8]) [unsigned, U8, 1]  // $display(((DP + (28 [DP])) & (127 [DP]))) = (100 [U8])
        *((*(22 [SP]) + (12 [SP])) + (((DP + (29 [DP])) & (127 [DP])) as SP)) = (101 [U8]) [unsigned, U8, 1]  // $display(((DP + (29 [DP])) & (127 [DP]))) = (101 [U8])
        *((*(22 [SP]) + (12 [SP])) + (((DP + (30 [DP])) & (127 [DP])) as SP)) = (120 [U8]) [unsigned, U8, 1]  // $display(((DP + (30 [DP])) & (127 [DP]))) = (120 [U8])
        *((*(22 [SP]) + (12 [SP])) + (((DP + (31 [DP])) & (127 [DP])) as SP)) = (46 [U8]) [unsigned, U8, 1]  // $display(((DP + (31 [DP])) & (127 [DP]))) = (46 [U8])
        *((*(22 [SP]) + (12 [SP])) + (((DP + (32 [DP])) & (127 [DP])) as SP)) = (85 [U8]) [unsigned, U8, 1]  // $display(((DP + (32 [DP])) & (127 [DP]))) = (85 [U8])
        *((*(22 [SP]) + (12 [SP])) + (((DP + (33 [DP])) & (127 [DP])) as SP)) = (32 [U8]) [unsigned, U8, 1]  // $display(((DP + (33 [DP])) & (127 [DP]))) = (32 [U8])
        *((*(22 [SP]) + (12 [SP])) + (((DP + (34 [DP])) & (127 [DP])) as SP)) = (118 [U8]) [unsigned, U8, 1]  // $display(((DP + (34 [DP])) & (127 [DP]))) = (118 [U8])
        *((*(22 [SP]) + (12 [SP])) + (((DP + (35 [DP])) & (127 [DP])) as SP)) = (97 [U8]) [unsigned, U8, 1]  // $display(((DP + (35 [DP])) & (127 [DP]))) = (97 [U8])
        *((*(22 [SP]) + (12 [SP])) + (((DP + (36 [DP])) & (127 [DP])) as SP)) = (108 [U8]) [unsigned, U8, 1]  // $display(((DP + (36 [DP])) & (127 [DP]))) = (108 [U8])
        *((*(22 [SP]) + (12 [SP])) + (((DP + (37 [DP])) & (127 [DP])) as SP)) = (117 [U8]) [unsigned, U8, 1]  // $display(((DP + (37 [DP])) & (127 [DP]))) = (117 [U8])
        *((*(22 [SP]) + (12 [SP])) + (((DP + (38 [DP])) & (127 [DP])) as SP)) = (101 [U8]) [unsigned, U8, 1]  // $display(((DP + (38 [DP])) & (127 [DP]))) = (101 [U8])
        goto .344
        */


        val __tmp_2079 = Cat(
          arrayRegFiles(22.U(16.W) + 1.U),
          arrayRegFiles(22.U(16.W) + 0.U)
        ) + 12.U(16.W) + DP & 127.U(64.W).asUInt
        val __tmp_2080 = (79.U(8.W)).asUInt
        arrayRegFiles(__tmp_2079 + 0.U) := __tmp_2080(7, 0)

        val __tmp_2081 = Cat(
          arrayRegFiles(22.U(16.W) + 1.U),
          arrayRegFiles(22.U(16.W) + 0.U)
        ) + 12.U(16.W) + DP + 1.U(64.W) & 127.U(64.W).asUInt
        val __tmp_2082 = (117.U(8.W)).asUInt
        arrayRegFiles(__tmp_2081 + 0.U) := __tmp_2082(7, 0)

        val __tmp_2083 = Cat(
          arrayRegFiles(22.U(16.W) + 1.U),
          arrayRegFiles(22.U(16.W) + 0.U)
        ) + 12.U(16.W) + DP + 2.U(64.W) & 127.U(64.W).asUInt
        val __tmp_2084 = (116.U(8.W)).asUInt
        arrayRegFiles(__tmp_2083 + 0.U) := __tmp_2084(7, 0)

        val __tmp_2085 = Cat(
          arrayRegFiles(22.U(16.W) + 1.U),
          arrayRegFiles(22.U(16.W) + 0.U)
        ) + 12.U(16.W) + DP + 3.U(64.W) & 127.U(64.W).asUInt
        val __tmp_2086 = (32.U(8.W)).asUInt
        arrayRegFiles(__tmp_2085 + 0.U) := __tmp_2086(7, 0)

        val __tmp_2087 = Cat(
          arrayRegFiles(22.U(16.W) + 1.U),
          arrayRegFiles(22.U(16.W) + 0.U)
        ) + 12.U(16.W) + DP + 4.U(64.W) & 127.U(64.W).asUInt
        val __tmp_2088 = (111.U(8.W)).asUInt
        arrayRegFiles(__tmp_2087 + 0.U) := __tmp_2088(7, 0)

        val __tmp_2089 = Cat(
          arrayRegFiles(22.U(16.W) + 1.U),
          arrayRegFiles(22.U(16.W) + 0.U)
        ) + 12.U(16.W) + DP + 5.U(64.W) & 127.U(64.W).asUInt
        val __tmp_2090 = (102.U(8.W)).asUInt
        arrayRegFiles(__tmp_2089 + 0.U) := __tmp_2090(7, 0)

        val __tmp_2091 = Cat(
          arrayRegFiles(22.U(16.W) + 1.U),
          arrayRegFiles(22.U(16.W) + 0.U)
        ) + 12.U(16.W) + DP + 6.U(64.W) & 127.U(64.W).asUInt
        val __tmp_2092 = (32.U(8.W)).asUInt
        arrayRegFiles(__tmp_2091 + 0.U) := __tmp_2092(7, 0)

        val __tmp_2093 = Cat(
          arrayRegFiles(22.U(16.W) + 1.U),
          arrayRegFiles(22.U(16.W) + 0.U)
        ) + 12.U(16.W) + DP + 7.U(64.W) & 127.U(64.W).asUInt
        val __tmp_2094 = (98.U(8.W)).asUInt
        arrayRegFiles(__tmp_2093 + 0.U) := __tmp_2094(7, 0)

        val __tmp_2095 = Cat(
          arrayRegFiles(22.U(16.W) + 1.U),
          arrayRegFiles(22.U(16.W) + 0.U)
        ) + 12.U(16.W) + DP + 8.U(64.W) & 127.U(64.W).asUInt
        val __tmp_2096 = (111.U(8.W)).asUInt
        arrayRegFiles(__tmp_2095 + 0.U) := __tmp_2096(7, 0)

        val __tmp_2097 = Cat(
          arrayRegFiles(22.U(16.W) + 1.U),
          arrayRegFiles(22.U(16.W) + 0.U)
        ) + 12.U(16.W) + DP + 9.U(64.W) & 127.U(64.W).asUInt
        val __tmp_2098 = (117.U(8.W)).asUInt
        arrayRegFiles(__tmp_2097 + 0.U) := __tmp_2098(7, 0)

        val __tmp_2099 = Cat(
          arrayRegFiles(22.U(16.W) + 1.U),
          arrayRegFiles(22.U(16.W) + 0.U)
        ) + 12.U(16.W) + DP + 10.U(64.W) & 127.U(64.W).asUInt
        val __tmp_2100 = (110.U(8.W)).asUInt
        arrayRegFiles(__tmp_2099 + 0.U) := __tmp_2100(7, 0)

        val __tmp_2101 = Cat(
          arrayRegFiles(22.U(16.W) + 1.U),
          arrayRegFiles(22.U(16.W) + 0.U)
        ) + 12.U(16.W) + DP + 11.U(64.W) & 127.U(64.W).asUInt
        val __tmp_2102 = (100.U(8.W)).asUInt
        arrayRegFiles(__tmp_2101 + 0.U) := __tmp_2102(7, 0)

        val __tmp_2103 = Cat(
          arrayRegFiles(22.U(16.W) + 1.U),
          arrayRegFiles(22.U(16.W) + 0.U)
        ) + 12.U(16.W) + DP + 12.U(64.W) & 127.U(64.W).asUInt
        val __tmp_2104 = (32.U(8.W)).asUInt
        arrayRegFiles(__tmp_2103 + 0.U) := __tmp_2104(7, 0)

        val __tmp_2105 = Cat(
          arrayRegFiles(22.U(16.W) + 1.U),
          arrayRegFiles(22.U(16.W) + 0.U)
        ) + 12.U(16.W) + DP + 13.U(64.W) & 127.U(64.W).asUInt
        val __tmp_2106 = (97.U(8.W)).asUInt
        arrayRegFiles(__tmp_2105 + 0.U) := __tmp_2106(7, 0)

        val __tmp_2107 = Cat(
          arrayRegFiles(22.U(16.W) + 1.U),
          arrayRegFiles(22.U(16.W) + 0.U)
        ) + 12.U(16.W) + DP + 14.U(64.W) & 127.U(64.W).asUInt
        val __tmp_2108 = (110.U(8.W)).asUInt
        arrayRegFiles(__tmp_2107 + 0.U) := __tmp_2108(7, 0)

        val __tmp_2109 = Cat(
          arrayRegFiles(22.U(16.W) + 1.U),
          arrayRegFiles(22.U(16.W) + 0.U)
        ) + 12.U(16.W) + DP + 15.U(64.W) & 127.U(64.W).asUInt
        val __tmp_2110 = (118.U(8.W)).asUInt
        arrayRegFiles(__tmp_2109 + 0.U) := __tmp_2110(7, 0)

        val __tmp_2111 = Cat(
          arrayRegFiles(22.U(16.W) + 1.U),
          arrayRegFiles(22.U(16.W) + 0.U)
        ) + 12.U(16.W) + DP + 16.U(64.W) & 127.U(64.W).asUInt
        val __tmp_2112 = (105.U(8.W)).asUInt
        arrayRegFiles(__tmp_2111 + 0.U) := __tmp_2112(7, 0)

        val __tmp_2113 = Cat(
          arrayRegFiles(22.U(16.W) + 1.U),
          arrayRegFiles(22.U(16.W) + 0.U)
        ) + 12.U(16.W) + DP + 17.U(64.W) & 127.U(64.W).asUInt
        val __tmp_2114 = (108.U(8.W)).asUInt
        arrayRegFiles(__tmp_2113 + 0.U) := __tmp_2114(7, 0)

        val __tmp_2115 = Cat(
          arrayRegFiles(22.U(16.W) + 1.U),
          arrayRegFiles(22.U(16.W) + 0.U)
        ) + 12.U(16.W) + DP + 18.U(64.W) & 127.U(64.W).asUInt
        val __tmp_2116 = (46.U(8.W)).asUInt
        arrayRegFiles(__tmp_2115 + 0.U) := __tmp_2116(7, 0)

        val __tmp_2117 = Cat(
          arrayRegFiles(22.U(16.W) + 1.U),
          arrayRegFiles(22.U(16.W) + 0.U)
        ) + 12.U(16.W) + DP + 19.U(64.W) & 127.U(64.W).asUInt
        val __tmp_2118 = (80.U(8.W)).asUInt
        arrayRegFiles(__tmp_2117 + 0.U) := __tmp_2118(7, 0)

        val __tmp_2119 = Cat(
          arrayRegFiles(22.U(16.W) + 1.U),
          arrayRegFiles(22.U(16.W) + 0.U)
        ) + 12.U(16.W) + DP + 20.U(64.W) & 127.U(64.W).asUInt
        val __tmp_2120 = (114.U(8.W)).asUInt
        arrayRegFiles(__tmp_2119 + 0.U) := __tmp_2120(7, 0)

        val __tmp_2121 = Cat(
          arrayRegFiles(22.U(16.W) + 1.U),
          arrayRegFiles(22.U(16.W) + 0.U)
        ) + 12.U(16.W) + DP + 21.U(64.W) & 127.U(64.W).asUInt
        val __tmp_2122 = (105.U(8.W)).asUInt
        arrayRegFiles(__tmp_2121 + 0.U) := __tmp_2122(7, 0)

        val __tmp_2123 = Cat(
          arrayRegFiles(22.U(16.W) + 1.U),
          arrayRegFiles(22.U(16.W) + 0.U)
        ) + 12.U(16.W) + DP + 22.U(64.W) & 127.U(64.W).asUInt
        val __tmp_2124 = (110.U(8.W)).asUInt
        arrayRegFiles(__tmp_2123 + 0.U) := __tmp_2124(7, 0)

        val __tmp_2125 = Cat(
          arrayRegFiles(22.U(16.W) + 1.U),
          arrayRegFiles(22.U(16.W) + 0.U)
        ) + 12.U(16.W) + DP + 23.U(64.W) & 127.U(64.W).asUInt
        val __tmp_2126 = (116.U(8.W)).asUInt
        arrayRegFiles(__tmp_2125 + 0.U) := __tmp_2126(7, 0)

        val __tmp_2127 = Cat(
          arrayRegFiles(22.U(16.W) + 1.U),
          arrayRegFiles(22.U(16.W) + 0.U)
        ) + 12.U(16.W) + DP + 24.U(64.W) & 127.U(64.W).asUInt
        val __tmp_2128 = (101.U(8.W)).asUInt
        arrayRegFiles(__tmp_2127 + 0.U) := __tmp_2128(7, 0)

        val __tmp_2129 = Cat(
          arrayRegFiles(22.U(16.W) + 1.U),
          arrayRegFiles(22.U(16.W) + 0.U)
        ) + 12.U(16.W) + DP + 25.U(64.W) & 127.U(64.W).asUInt
        val __tmp_2130 = (114.U(8.W)).asUInt
        arrayRegFiles(__tmp_2129 + 0.U) := __tmp_2130(7, 0)

        val __tmp_2131 = Cat(
          arrayRegFiles(22.U(16.W) + 1.U),
          arrayRegFiles(22.U(16.W) + 0.U)
        ) + 12.U(16.W) + DP + 26.U(64.W) & 127.U(64.W).asUInt
        val __tmp_2132 = (73.U(8.W)).asUInt
        arrayRegFiles(__tmp_2131 + 0.U) := __tmp_2132(7, 0)

        val __tmp_2133 = Cat(
          arrayRegFiles(22.U(16.W) + 1.U),
          arrayRegFiles(22.U(16.W) + 0.U)
        ) + 12.U(16.W) + DP + 27.U(64.W) & 127.U(64.W).asUInt
        val __tmp_2134 = (110.U(8.W)).asUInt
        arrayRegFiles(__tmp_2133 + 0.U) := __tmp_2134(7, 0)

        val __tmp_2135 = Cat(
          arrayRegFiles(22.U(16.W) + 1.U),
          arrayRegFiles(22.U(16.W) + 0.U)
        ) + 12.U(16.W) + DP + 28.U(64.W) & 127.U(64.W).asUInt
        val __tmp_2136 = (100.U(8.W)).asUInt
        arrayRegFiles(__tmp_2135 + 0.U) := __tmp_2136(7, 0)

        val __tmp_2137 = Cat(
          arrayRegFiles(22.U(16.W) + 1.U),
          arrayRegFiles(22.U(16.W) + 0.U)
        ) + 12.U(16.W) + DP + 29.U(64.W) & 127.U(64.W).asUInt
        val __tmp_2138 = (101.U(8.W)).asUInt
        arrayRegFiles(__tmp_2137 + 0.U) := __tmp_2138(7, 0)

        val __tmp_2139 = Cat(
          arrayRegFiles(22.U(16.W) + 1.U),
          arrayRegFiles(22.U(16.W) + 0.U)
        ) + 12.U(16.W) + DP + 30.U(64.W) & 127.U(64.W).asUInt
        val __tmp_2140 = (120.U(8.W)).asUInt
        arrayRegFiles(__tmp_2139 + 0.U) := __tmp_2140(7, 0)

        val __tmp_2141 = Cat(
          arrayRegFiles(22.U(16.W) + 1.U),
          arrayRegFiles(22.U(16.W) + 0.U)
        ) + 12.U(16.W) + DP + 31.U(64.W) & 127.U(64.W).asUInt
        val __tmp_2142 = (46.U(8.W)).asUInt
        arrayRegFiles(__tmp_2141 + 0.U) := __tmp_2142(7, 0)

        val __tmp_2143 = Cat(
          arrayRegFiles(22.U(16.W) + 1.U),
          arrayRegFiles(22.U(16.W) + 0.U)
        ) + 12.U(16.W) + DP + 32.U(64.W) & 127.U(64.W).asUInt
        val __tmp_2144 = (85.U(8.W)).asUInt
        arrayRegFiles(__tmp_2143 + 0.U) := __tmp_2144(7, 0)

        val __tmp_2145 = Cat(
          arrayRegFiles(22.U(16.W) + 1.U),
          arrayRegFiles(22.U(16.W) + 0.U)
        ) + 12.U(16.W) + DP + 33.U(64.W) & 127.U(64.W).asUInt
        val __tmp_2146 = (32.U(8.W)).asUInt
        arrayRegFiles(__tmp_2145 + 0.U) := __tmp_2146(7, 0)

        val __tmp_2147 = Cat(
          arrayRegFiles(22.U(16.W) + 1.U),
          arrayRegFiles(22.U(16.W) + 0.U)
        ) + 12.U(16.W) + DP + 34.U(64.W) & 127.U(64.W).asUInt
        val __tmp_2148 = (118.U(8.W)).asUInt
        arrayRegFiles(__tmp_2147 + 0.U) := __tmp_2148(7, 0)

        val __tmp_2149 = Cat(
          arrayRegFiles(22.U(16.W) + 1.U),
          arrayRegFiles(22.U(16.W) + 0.U)
        ) + 12.U(16.W) + DP + 35.U(64.W) & 127.U(64.W).asUInt
        val __tmp_2150 = (97.U(8.W)).asUInt
        arrayRegFiles(__tmp_2149 + 0.U) := __tmp_2150(7, 0)

        val __tmp_2151 = Cat(
          arrayRegFiles(22.U(16.W) + 1.U),
          arrayRegFiles(22.U(16.W) + 0.U)
        ) + 12.U(16.W) + DP + 36.U(64.W) & 127.U(64.W).asUInt
        val __tmp_2152 = (108.U(8.W)).asUInt
        arrayRegFiles(__tmp_2151 + 0.U) := __tmp_2152(7, 0)

        val __tmp_2153 = Cat(
          arrayRegFiles(22.U(16.W) + 1.U),
          arrayRegFiles(22.U(16.W) + 0.U)
        ) + 12.U(16.W) + DP + 37.U(64.W) & 127.U(64.W).asUInt
        val __tmp_2154 = (117.U(8.W)).asUInt
        arrayRegFiles(__tmp_2153 + 0.U) := __tmp_2154(7, 0)

        val __tmp_2155 = Cat(
          arrayRegFiles(22.U(16.W) + 1.U),
          arrayRegFiles(22.U(16.W) + 0.U)
        ) + 12.U(16.W) + DP + 38.U(64.W) & 127.U(64.W).asUInt
        val __tmp_2156 = (101.U(8.W)).asUInt
        arrayRegFiles(__tmp_2155 + 0.U) := __tmp_2156(7, 0)

        CP := 344.U
      }

      is(344.U) {
        /*
        DP = DP + 39
        goto .345
        */


        DP := DP + 39.U

        CP := 345.U
      }

      is(345.U) {
        /*
        *((*(22 [SP]) + (12 [SP])) + ((DP & (127 [DP])) as SP)) = (10 [U8]) [unsigned, U8, 1]  // $display((DP & (127 [DP]))) = (10 [U8])
        goto .346
        */


        val __tmp_2157 = Cat(
          arrayRegFiles(22.U(16.W) + 1.U),
          arrayRegFiles(22.U(16.W) + 0.U)
        ) + 12.U(16.W) + DP & 127.U(64.W).asUInt
        val __tmp_2158 = (10.U(8.W)).asUInt
        arrayRegFiles(__tmp_2157 + 0.U) := __tmp_2158(7, 0)

        CP := 346.U
      }

      is(346.U) {
        /*
        DP = DP + 1
        goto .347
        */


        DP := DP + 1.U

        CP := 347.U
      }

      is(347.U) {
        /*
        *(SP + (6 [SP])) = (638 [U32]) [signed, U32, 4]  // $sfLoc = (638 [U32])
        goto .348
        */


        val __tmp_2159 = SP + 6.U(16.W)
        val __tmp_2160 = (638.S(32.W)).asUInt
        arrayRegFiles(__tmp_2159 + 0.U) := __tmp_2160(7, 0)
        arrayRegFiles(__tmp_2159 + 1.U) := __tmp_2160(15, 8)
        arrayRegFiles(__tmp_2159 + 2.U) := __tmp_2160(23, 16)
        arrayRegFiles(__tmp_2159 + 3.U) := __tmp_2160(31, 24)

        CP := 348.U
      }

      is(348.U) {
        /*
        $0 = (*(SP + (78 [SP])) as anvil.PrinterIndex.U)
        alloc printStackTrace$res@[638,7].334C92B6: U64 [@212, 8]
        goto .349
        */


        generalRegFiles(0.U) := Cat(
          arrayRegFiles(SP + 78.U(16.W) + 1.U),
          arrayRegFiles(SP + 78.U(16.W) + 0.U)
        ).asUInt
        CP := 349.U
      }

      is(349.U) {
        /*
        SP = SP + 228
        goto .350
        */


        SP := SP + 228.U

        CP := 350.U
      }

      is(350.U) {
        /*
        decl $ret: CP [@0, 2], $res: SP [@2, 2], $sfCaller: SP [@4, 2], $sfLoc: U32 [@6, 4], $sfDesc: IS[56, U8] [@10, 68], $sfCurrentId: SP [@78, 2], buffer: SP [@80, 2], index: anvil.PrinterIndex.U [@82, 8], memory: SP [@90, 2], mask: anvil.PrinterIndex.U [@92, 8], spSize: anvil.PrinterIndex.U [@100, 8], typeShaSize: anvil.PrinterIndex.U [@108, 8], locSize: anvil.PrinterIndex.U [@116, 8], sizeSize: anvil.PrinterIndex.U [@124, 8], sfCallerOffset: anvil.PrinterIndex.U [@132, 8]
        *SP = (351 [CP]) [unsigned, CP, 2]  // $ret@0 = 2229
        *(SP + (2 [SP])) = (SP - (16 [SP])) [unsigned, SP, 2]  // $res@2 = -16
        *(SP + (4 [SP])) = (SP - (224 [SP])) [unsigned, SP, 2]  // $sfCaller@4 = -224
        *(SP + (78 [SP])) = (SP + (4 [SP])) [unsigned, SP, 2]  // $sfCurrentId@78 = 4
        *(SP + (80 [SP])) = *(22 [SP]) [unsigned, SP, 2]  // buffer = *(22 [SP])
        *(SP + (82 [SP])) = DP [unsigned, anvil.PrinterIndex.U, 8]  // index = DP
        *(SP + (90 [SP])) = (0 [SP]) [unsigned, SP, 2]  // memory = (0 [SP])
        *(SP + (92 [SP])) = (127 [DP]) [unsigned, anvil.PrinterIndex.U, 8]  // mask = (127 [DP])
        *(SP + (100 [SP])) = (2 [anvil.PrinterIndex.U]) [unsigned, anvil.PrinterIndex.U, 8]  // spSize = (2 [anvil.PrinterIndex.U])
        *(SP + (108 [SP])) = (4 [anvil.PrinterIndex.U]) [unsigned, anvil.PrinterIndex.U, 8]  // typeShaSize = (4 [anvil.PrinterIndex.U])
        *(SP + (116 [SP])) = (4 [anvil.PrinterIndex.U]) [unsigned, anvil.PrinterIndex.U, 8]  // locSize = (4 [anvil.PrinterIndex.U])
        *(SP + (124 [SP])) = (8 [anvil.PrinterIndex.U]) [unsigned, anvil.PrinterIndex.U, 8]  // sizeSize = (8 [anvil.PrinterIndex.U])
        *(SP + (132 [SP])) = $0 [unsigned, anvil.PrinterIndex.U, 8]  // sfCallerOffset = $0
        *(SP - (8 [SP])) = $0 [unsigned, U64, 8]  // save $0
        goto .27
        */


        val __tmp_2161 = SP
        val __tmp_2162 = (351.U(16.W)).asUInt
        arrayRegFiles(__tmp_2161 + 0.U) := __tmp_2162(7, 0)
        arrayRegFiles(__tmp_2161 + 1.U) := __tmp_2162(15, 8)

        val __tmp_2163 = SP + 2.U(16.W)
        val __tmp_2164 = (SP - 16.U(16.W)).asUInt
        arrayRegFiles(__tmp_2163 + 0.U) := __tmp_2164(7, 0)
        arrayRegFiles(__tmp_2163 + 1.U) := __tmp_2164(15, 8)

        val __tmp_2165 = SP + 4.U(16.W)
        val __tmp_2166 = (SP - 224.U(16.W)).asUInt
        arrayRegFiles(__tmp_2165 + 0.U) := __tmp_2166(7, 0)
        arrayRegFiles(__tmp_2165 + 1.U) := __tmp_2166(15, 8)

        val __tmp_2167 = SP + 78.U(16.W)
        val __tmp_2168 = (SP + 4.U(16.W)).asUInt
        arrayRegFiles(__tmp_2167 + 0.U) := __tmp_2168(7, 0)
        arrayRegFiles(__tmp_2167 + 1.U) := __tmp_2168(15, 8)

        val __tmp_2169 = SP + 80.U(16.W)
        val __tmp_2170 = (Cat(
          arrayRegFiles(22.U(16.W) + 1.U),
          arrayRegFiles(22.U(16.W) + 0.U)
        )).asUInt
        arrayRegFiles(__tmp_2169 + 0.U) := __tmp_2170(7, 0)
        arrayRegFiles(__tmp_2169 + 1.U) := __tmp_2170(15, 8)

        val __tmp_2171 = SP + 82.U(16.W)
        val __tmp_2172 = (DP).asUInt
        arrayRegFiles(__tmp_2171 + 0.U) := __tmp_2172(7, 0)
        arrayRegFiles(__tmp_2171 + 1.U) := __tmp_2172(15, 8)
        arrayRegFiles(__tmp_2171 + 2.U) := __tmp_2172(23, 16)
        arrayRegFiles(__tmp_2171 + 3.U) := __tmp_2172(31, 24)
        arrayRegFiles(__tmp_2171 + 4.U) := __tmp_2172(39, 32)
        arrayRegFiles(__tmp_2171 + 5.U) := __tmp_2172(47, 40)
        arrayRegFiles(__tmp_2171 + 6.U) := __tmp_2172(55, 48)
        arrayRegFiles(__tmp_2171 + 7.U) := __tmp_2172(63, 56)

        val __tmp_2173 = SP + 90.U(16.W)
        val __tmp_2174 = (0.U(16.W)).asUInt
        arrayRegFiles(__tmp_2173 + 0.U) := __tmp_2174(7, 0)
        arrayRegFiles(__tmp_2173 + 1.U) := __tmp_2174(15, 8)

        val __tmp_2175 = SP + 92.U(16.W)
        val __tmp_2176 = (127.U(64.W)).asUInt
        arrayRegFiles(__tmp_2175 + 0.U) := __tmp_2176(7, 0)
        arrayRegFiles(__tmp_2175 + 1.U) := __tmp_2176(15, 8)
        arrayRegFiles(__tmp_2175 + 2.U) := __tmp_2176(23, 16)
        arrayRegFiles(__tmp_2175 + 3.U) := __tmp_2176(31, 24)
        arrayRegFiles(__tmp_2175 + 4.U) := __tmp_2176(39, 32)
        arrayRegFiles(__tmp_2175 + 5.U) := __tmp_2176(47, 40)
        arrayRegFiles(__tmp_2175 + 6.U) := __tmp_2176(55, 48)
        arrayRegFiles(__tmp_2175 + 7.U) := __tmp_2176(63, 56)

        val __tmp_2177 = SP + 100.U(16.W)
        val __tmp_2178 = (2.U(64.W)).asUInt
        arrayRegFiles(__tmp_2177 + 0.U) := __tmp_2178(7, 0)
        arrayRegFiles(__tmp_2177 + 1.U) := __tmp_2178(15, 8)
        arrayRegFiles(__tmp_2177 + 2.U) := __tmp_2178(23, 16)
        arrayRegFiles(__tmp_2177 + 3.U) := __tmp_2178(31, 24)
        arrayRegFiles(__tmp_2177 + 4.U) := __tmp_2178(39, 32)
        arrayRegFiles(__tmp_2177 + 5.U) := __tmp_2178(47, 40)
        arrayRegFiles(__tmp_2177 + 6.U) := __tmp_2178(55, 48)
        arrayRegFiles(__tmp_2177 + 7.U) := __tmp_2178(63, 56)

        val __tmp_2179 = SP + 108.U(16.W)
        val __tmp_2180 = (4.U(64.W)).asUInt
        arrayRegFiles(__tmp_2179 + 0.U) := __tmp_2180(7, 0)
        arrayRegFiles(__tmp_2179 + 1.U) := __tmp_2180(15, 8)
        arrayRegFiles(__tmp_2179 + 2.U) := __tmp_2180(23, 16)
        arrayRegFiles(__tmp_2179 + 3.U) := __tmp_2180(31, 24)
        arrayRegFiles(__tmp_2179 + 4.U) := __tmp_2180(39, 32)
        arrayRegFiles(__tmp_2179 + 5.U) := __tmp_2180(47, 40)
        arrayRegFiles(__tmp_2179 + 6.U) := __tmp_2180(55, 48)
        arrayRegFiles(__tmp_2179 + 7.U) := __tmp_2180(63, 56)

        val __tmp_2181 = SP + 116.U(16.W)
        val __tmp_2182 = (4.U(64.W)).asUInt
        arrayRegFiles(__tmp_2181 + 0.U) := __tmp_2182(7, 0)
        arrayRegFiles(__tmp_2181 + 1.U) := __tmp_2182(15, 8)
        arrayRegFiles(__tmp_2181 + 2.U) := __tmp_2182(23, 16)
        arrayRegFiles(__tmp_2181 + 3.U) := __tmp_2182(31, 24)
        arrayRegFiles(__tmp_2181 + 4.U) := __tmp_2182(39, 32)
        arrayRegFiles(__tmp_2181 + 5.U) := __tmp_2182(47, 40)
        arrayRegFiles(__tmp_2181 + 6.U) := __tmp_2182(55, 48)
        arrayRegFiles(__tmp_2181 + 7.U) := __tmp_2182(63, 56)

        val __tmp_2183 = SP + 124.U(16.W)
        val __tmp_2184 = (8.U(64.W)).asUInt
        arrayRegFiles(__tmp_2183 + 0.U) := __tmp_2184(7, 0)
        arrayRegFiles(__tmp_2183 + 1.U) := __tmp_2184(15, 8)
        arrayRegFiles(__tmp_2183 + 2.U) := __tmp_2184(23, 16)
        arrayRegFiles(__tmp_2183 + 3.U) := __tmp_2184(31, 24)
        arrayRegFiles(__tmp_2183 + 4.U) := __tmp_2184(39, 32)
        arrayRegFiles(__tmp_2183 + 5.U) := __tmp_2184(47, 40)
        arrayRegFiles(__tmp_2183 + 6.U) := __tmp_2184(55, 48)
        arrayRegFiles(__tmp_2183 + 7.U) := __tmp_2184(63, 56)

        val __tmp_2185 = SP + 132.U(16.W)
        val __tmp_2186 = (generalRegFiles(0.U)).asUInt
        arrayRegFiles(__tmp_2185 + 0.U) := __tmp_2186(7, 0)
        arrayRegFiles(__tmp_2185 + 1.U) := __tmp_2186(15, 8)
        arrayRegFiles(__tmp_2185 + 2.U) := __tmp_2186(23, 16)
        arrayRegFiles(__tmp_2185 + 3.U) := __tmp_2186(31, 24)
        arrayRegFiles(__tmp_2185 + 4.U) := __tmp_2186(39, 32)
        arrayRegFiles(__tmp_2185 + 5.U) := __tmp_2186(47, 40)
        arrayRegFiles(__tmp_2185 + 6.U) := __tmp_2186(55, 48)
        arrayRegFiles(__tmp_2185 + 7.U) := __tmp_2186(63, 56)

        val __tmp_2187 = SP - 8.U(16.W)
        val __tmp_2188 = (generalRegFiles(0.U)).asUInt
        arrayRegFiles(__tmp_2187 + 0.U) := __tmp_2188(7, 0)
        arrayRegFiles(__tmp_2187 + 1.U) := __tmp_2188(15, 8)
        arrayRegFiles(__tmp_2187 + 2.U) := __tmp_2188(23, 16)
        arrayRegFiles(__tmp_2187 + 3.U) := __tmp_2188(31, 24)
        arrayRegFiles(__tmp_2187 + 4.U) := __tmp_2188(39, 32)
        arrayRegFiles(__tmp_2187 + 5.U) := __tmp_2188(47, 40)
        arrayRegFiles(__tmp_2187 + 6.U) := __tmp_2188(55, 48)
        arrayRegFiles(__tmp_2187 + 7.U) := __tmp_2188(63, 56)

        CP := 27.U
      }

      is(351.U) {
        /*
        $0 = *(SP - (8 [SP])) [unsigned, U64, 8]  // restore $0
        $1 = **(SP + (2 [SP])) [unsigned, U64, 8]  // $1 = $ret
        undecl sfCallerOffset: anvil.PrinterIndex.U [@132, 8], sizeSize: anvil.PrinterIndex.U [@124, 8], locSize: anvil.PrinterIndex.U [@116, 8], typeShaSize: anvil.PrinterIndex.U [@108, 8], spSize: anvil.PrinterIndex.U [@100, 8], mask: anvil.PrinterIndex.U [@92, 8], memory: SP [@90, 2], index: anvil.PrinterIndex.U [@82, 8], buffer: SP [@80, 2], $sfCurrentId: SP [@78, 2], $sfDesc: IS[56, U8] [@10, 68], $sfLoc: U32 [@6, 4], $sfCaller: SP [@4, 2], $res: SP [@2, 2], $ret: CP [@0, 2]
        goto .352
        */


        val __tmp_2189 = (SP - 8.U(16.W)).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_2189 + 7.U),
          arrayRegFiles(__tmp_2189 + 6.U),
          arrayRegFiles(__tmp_2189 + 5.U),
          arrayRegFiles(__tmp_2189 + 4.U),
          arrayRegFiles(__tmp_2189 + 3.U),
          arrayRegFiles(__tmp_2189 + 2.U),
          arrayRegFiles(__tmp_2189 + 1.U),
          arrayRegFiles(__tmp_2189 + 0.U)
        ).asUInt

        val __tmp_2190 = (Cat(
          arrayRegFiles(SP + 2.U(16.W) + 1.U),
          arrayRegFiles(SP + 2.U(16.W) + 0.U)
        )).asUInt
        generalRegFiles(1.U) := Cat(
          arrayRegFiles(__tmp_2190 + 7.U),
          arrayRegFiles(__tmp_2190 + 6.U),
          arrayRegFiles(__tmp_2190 + 5.U),
          arrayRegFiles(__tmp_2190 + 4.U),
          arrayRegFiles(__tmp_2190 + 3.U),
          arrayRegFiles(__tmp_2190 + 2.U),
          arrayRegFiles(__tmp_2190 + 1.U),
          arrayRegFiles(__tmp_2190 + 0.U)
        ).asUInt

        CP := 352.U
      }

      is(352.U) {
        /*
        SP = SP - 228
        goto .353
        */


        SP := SP - 228.U

        CP := 353.U
      }

      is(353.U) {
        /*
        DP = DP + ($1 as DP)
        goto .354
        */


        DP := generalRegFiles(1.U).asUInt

        CP := 354.U
      }

      is(354.U) {
        /*
        unalloc printStackTrace$res@[638,7].334C92B6: U64 [@212, 8]
        goto .1
        */


        CP := 1.U
      }

      is(355.U) {
        /*
        $0 = *(SP - (56 [SP])) [unsigned, U64, 8]  // restore $0
        $1 = *(SP - (48 [SP])) [unsigned, U64, 8]  // restore $1
        $2 = *(SP - (40 [SP])) [unsigned, U64, 8]  // restore $2
        $3 = *(SP - (32 [SP])) [unsigned, U64, 8]  // restore $3
        $4 = *(SP - (24 [SP])) [unsigned, U64, 8]  // restore $4
        $5 = *(SP - (16 [SP])) [unsigned, U64, 8]  // restore $5
        $6 = *(SP - (8 [SP])) [unsigned, U64, 8]  // restore $6
        $7 = **(SP + (2 [SP])) [unsigned, anvil.PrinterIndex.U, 8]  // $7 = $ret
        undecl size: anvil.PrinterIndex.U [@79, 8], offset: anvil.PrinterIndex.U [@71, 8], memory: SP [@69, 2], $sfCurrentId: SP [@67, 2], $sfDesc: IS[45, U8] [@10, 57], $sfLoc: U32 [@6, 4], $sfCaller: SP [@4, 2], $res: SP [@2, 2], $ret: CP [@0, 2]
        goto .356
        */


        val __tmp_2191 = (SP - 56.U(16.W)).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_2191 + 7.U),
          arrayRegFiles(__tmp_2191 + 6.U),
          arrayRegFiles(__tmp_2191 + 5.U),
          arrayRegFiles(__tmp_2191 + 4.U),
          arrayRegFiles(__tmp_2191 + 3.U),
          arrayRegFiles(__tmp_2191 + 2.U),
          arrayRegFiles(__tmp_2191 + 1.U),
          arrayRegFiles(__tmp_2191 + 0.U)
        ).asUInt

        val __tmp_2192 = (SP - 48.U(16.W)).asUInt
        generalRegFiles(1.U) := Cat(
          arrayRegFiles(__tmp_2192 + 7.U),
          arrayRegFiles(__tmp_2192 + 6.U),
          arrayRegFiles(__tmp_2192 + 5.U),
          arrayRegFiles(__tmp_2192 + 4.U),
          arrayRegFiles(__tmp_2192 + 3.U),
          arrayRegFiles(__tmp_2192 + 2.U),
          arrayRegFiles(__tmp_2192 + 1.U),
          arrayRegFiles(__tmp_2192 + 0.U)
        ).asUInt

        val __tmp_2193 = (SP - 40.U(16.W)).asUInt
        generalRegFiles(2.U) := Cat(
          arrayRegFiles(__tmp_2193 + 7.U),
          arrayRegFiles(__tmp_2193 + 6.U),
          arrayRegFiles(__tmp_2193 + 5.U),
          arrayRegFiles(__tmp_2193 + 4.U),
          arrayRegFiles(__tmp_2193 + 3.U),
          arrayRegFiles(__tmp_2193 + 2.U),
          arrayRegFiles(__tmp_2193 + 1.U),
          arrayRegFiles(__tmp_2193 + 0.U)
        ).asUInt

        val __tmp_2194 = (SP - 32.U(16.W)).asUInt
        generalRegFiles(3.U) := Cat(
          arrayRegFiles(__tmp_2194 + 7.U),
          arrayRegFiles(__tmp_2194 + 6.U),
          arrayRegFiles(__tmp_2194 + 5.U),
          arrayRegFiles(__tmp_2194 + 4.U),
          arrayRegFiles(__tmp_2194 + 3.U),
          arrayRegFiles(__tmp_2194 + 2.U),
          arrayRegFiles(__tmp_2194 + 1.U),
          arrayRegFiles(__tmp_2194 + 0.U)
        ).asUInt

        val __tmp_2195 = (SP - 24.U(16.W)).asUInt
        generalRegFiles(4.U) := Cat(
          arrayRegFiles(__tmp_2195 + 7.U),
          arrayRegFiles(__tmp_2195 + 6.U),
          arrayRegFiles(__tmp_2195 + 5.U),
          arrayRegFiles(__tmp_2195 + 4.U),
          arrayRegFiles(__tmp_2195 + 3.U),
          arrayRegFiles(__tmp_2195 + 2.U),
          arrayRegFiles(__tmp_2195 + 1.U),
          arrayRegFiles(__tmp_2195 + 0.U)
        ).asUInt

        val __tmp_2196 = (SP - 16.U(16.W)).asUInt
        generalRegFiles(5.U) := Cat(
          arrayRegFiles(__tmp_2196 + 7.U),
          arrayRegFiles(__tmp_2196 + 6.U),
          arrayRegFiles(__tmp_2196 + 5.U),
          arrayRegFiles(__tmp_2196 + 4.U),
          arrayRegFiles(__tmp_2196 + 3.U),
          arrayRegFiles(__tmp_2196 + 2.U),
          arrayRegFiles(__tmp_2196 + 1.U),
          arrayRegFiles(__tmp_2196 + 0.U)
        ).asUInt

        val __tmp_2197 = (SP - 8.U(16.W)).asUInt
        generalRegFiles(6.U) := Cat(
          arrayRegFiles(__tmp_2197 + 7.U),
          arrayRegFiles(__tmp_2197 + 6.U),
          arrayRegFiles(__tmp_2197 + 5.U),
          arrayRegFiles(__tmp_2197 + 4.U),
          arrayRegFiles(__tmp_2197 + 3.U),
          arrayRegFiles(__tmp_2197 + 2.U),
          arrayRegFiles(__tmp_2197 + 1.U),
          arrayRegFiles(__tmp_2197 + 0.U)
        ).asUInt

        val __tmp_2198 = (Cat(
          arrayRegFiles(SP + 2.U(16.W) + 1.U),
          arrayRegFiles(SP + 2.U(16.W) + 0.U)
        )).asUInt
        generalRegFiles(7.U) := Cat(
          arrayRegFiles(__tmp_2198 + 7.U),
          arrayRegFiles(__tmp_2198 + 6.U),
          arrayRegFiles(__tmp_2198 + 5.U),
          arrayRegFiles(__tmp_2198 + 4.U),
          arrayRegFiles(__tmp_2198 + 3.U),
          arrayRegFiles(__tmp_2198 + 2.U),
          arrayRegFiles(__tmp_2198 + 1.U),
          arrayRegFiles(__tmp_2198 + 0.U)
        ).asUInt

        CP := 356.U
      }

      is(356.U) {
        /*
        SP = SP - 276
        goto .357
        */


        SP := SP - 276.U

        CP := 357.U
      }

      is(357.U) {
        /*
        *(SP + (140 [SP])) = $7 [signed, anvil.PrinterIndex.U, 8]  // sfCaller = $7
        goto .358
        */


        val __tmp_2199 = SP + 140.U(16.W)
        val __tmp_2200 = (generalRegFiles(7.U)).asUInt
        arrayRegFiles(__tmp_2199 + 0.U) := __tmp_2200(7, 0)
        arrayRegFiles(__tmp_2199 + 1.U) := __tmp_2200(15, 8)
        arrayRegFiles(__tmp_2199 + 2.U) := __tmp_2200(23, 16)
        arrayRegFiles(__tmp_2199 + 3.U) := __tmp_2200(31, 24)
        arrayRegFiles(__tmp_2199 + 4.U) := __tmp_2200(39, 32)
        arrayRegFiles(__tmp_2199 + 5.U) := __tmp_2200(47, 40)
        arrayRegFiles(__tmp_2199 + 6.U) := __tmp_2200(55, 48)
        arrayRegFiles(__tmp_2199 + 7.U) := __tmp_2200(63, 56)

        CP := 358.U
      }

      is(358.U) {
        /*
        unalloc load$res@[666,18].2F90B465: anvil.PrinterIndex.U [@212, 8]
        *(SP + (6 [SP])) = (643 [U32]) [signed, U32, 4]  // $sfLoc = (643 [U32])
        goto .359
        */


        val __tmp_2201 = SP + 6.U(16.W)
        val __tmp_2202 = (643.S(32.W)).asUInt
        arrayRegFiles(__tmp_2201 + 0.U) := __tmp_2202(7, 0)
        arrayRegFiles(__tmp_2201 + 1.U) := __tmp_2202(15, 8)
        arrayRegFiles(__tmp_2201 + 2.U) := __tmp_2202(23, 16)
        arrayRegFiles(__tmp_2201 + 3.U) := __tmp_2202(31, 24)

        CP := 359.U
      }

      is(359.U) {
        /*
        undecl offset: anvil.PrinterIndex.U [@164, 8]
        *(SP + (6 [SP])) = (644 [U32]) [signed, U32, 4]  // $sfLoc = (644 [U32])
        goto .360
        */


        val __tmp_2203 = SP + 6.U(16.W)
        val __tmp_2204 = (644.S(32.W)).asUInt
        arrayRegFiles(__tmp_2203 + 0.U) := __tmp_2204(7, 0)
        arrayRegFiles(__tmp_2203 + 1.U) := __tmp_2204(15, 8)
        arrayRegFiles(__tmp_2203 + 2.U) := __tmp_2204(23, 16)
        arrayRegFiles(__tmp_2203 + 3.U) := __tmp_2204(31, 24)

        CP := 360.U
      }

      is(360.U) {
        /*
        undecl sfLoc: U64 [@172, 8]
        *(SP + (6 [SP])) = (646 [U32]) [signed, U32, 4]  // $sfLoc = (646 [U32])
        goto .361
        */


        val __tmp_2205 = SP + 6.U(16.W)
        val __tmp_2206 = (646.S(32.W)).asUInt
        arrayRegFiles(__tmp_2205 + 0.U) := __tmp_2206(7, 0)
        arrayRegFiles(__tmp_2205 + 1.U) := __tmp_2206(15, 8)
        arrayRegFiles(__tmp_2205 + 2.U) := __tmp_2206(23, 16)
        arrayRegFiles(__tmp_2205 + 3.U) := __tmp_2206(31, 24)

        CP := 361.U
      }

      is(361.U) {
        /*
        undecl sfDescSize: anvil.PrinterIndex.U [@188, 8]
        *(SP + (6 [SP])) = (652 [U32]) [signed, U32, 4]  // $sfLoc = (652 [U32])
        goto .362
        */


        val __tmp_2207 = SP + 6.U(16.W)
        val __tmp_2208 = (652.S(32.W)).asUInt
        arrayRegFiles(__tmp_2207 + 0.U) := __tmp_2208(7, 0)
        arrayRegFiles(__tmp_2207 + 1.U) := __tmp_2208(15, 8)
        arrayRegFiles(__tmp_2207 + 2.U) := __tmp_2208(23, 16)
        arrayRegFiles(__tmp_2207 + 3.U) := __tmp_2208(31, 24)

        CP := 362.U
      }

      is(362.U) {
        /*
        undecl i: anvil.PrinterIndex.U [@196, 8]
        *(SP + (6 [SP])) = (659 [U32]) [signed, U32, 4]  // $sfLoc = (659 [U32])
        goto .363
        */


        val __tmp_2209 = SP + 6.U(16.W)
        val __tmp_2210 = (659.S(32.W)).asUInt
        arrayRegFiles(__tmp_2209 + 0.U) := __tmp_2210(7, 0)
        arrayRegFiles(__tmp_2209 + 1.U) := __tmp_2210(15, 8)
        arrayRegFiles(__tmp_2209 + 2.U) := __tmp_2210(23, 16)
        arrayRegFiles(__tmp_2209 + 3.U) := __tmp_2210(31, 24)

        CP := 363.U
      }

      is(363.U) {
        /*
        undecl n: U64 [@204, 8]
        *(SP + (6 [SP])) = (642 [U32]) [signed, U32, 4]  // $sfLoc = (642 [U32])
        goto .36
        */


        val __tmp_2211 = SP + 6.U(16.W)
        val __tmp_2212 = (642.S(32.W)).asUInt
        arrayRegFiles(__tmp_2211 + 0.U) := __tmp_2212(7, 0)
        arrayRegFiles(__tmp_2211 + 1.U) := __tmp_2212(15, 8)
        arrayRegFiles(__tmp_2211 + 2.U) := __tmp_2212(23, 16)
        arrayRegFiles(__tmp_2211 + 3.U) := __tmp_2212(31, 24)

        CP := 36.U
      }

      is(364.U) {
        /*
        *((*(22 [SP]) + (12 [SP])) + ((DP & (127 [DP])) as SP)) = (10 [U8]) [unsigned, U8, 1]  // $display((DP & (127 [DP]))) = (10 [U8])
        goto .365
        */


        val __tmp_2213 = Cat(
          arrayRegFiles(22.U(16.W) + 1.U),
          arrayRegFiles(22.U(16.W) + 0.U)
        ) + 12.U(16.W) + DP & 127.U(64.W).asUInt
        val __tmp_2214 = (10.U(8.W)).asUInt
        arrayRegFiles(__tmp_2213 + 0.U) := __tmp_2214(7, 0)

        CP := 365.U
      }

      is(365.U) {
        /*
        DP = DP + 1
        goto .87
        */


        DP := DP + 1.U

        CP := 87.U
      }

      is(366.U) {
        /*
        $0 = *(SP - (8 [SP])) [unsigned, U64, 8]  // restore $0
        $1 = **(SP + (2 [SP])) [unsigned, U64, 8]  // $1 = $ret
        undecl sfCallerOffset: anvil.PrinterIndex.U [@132, 8], sizeSize: anvil.PrinterIndex.U [@124, 8], locSize: anvil.PrinterIndex.U [@116, 8], typeShaSize: anvil.PrinterIndex.U [@108, 8], spSize: anvil.PrinterIndex.U [@100, 8], mask: anvil.PrinterIndex.U [@92, 8], memory: SP [@90, 2], index: anvil.PrinterIndex.U [@82, 8], buffer: SP [@80, 2], $sfCurrentId: SP [@78, 2], $sfDesc: IS[56, U8] [@10, 68], $sfLoc: U32 [@6, 4], $sfCaller: SP [@4, 2], $res: SP [@2, 2], $ret: CP [@0, 2]
        goto .367
        */


        val __tmp_2215 = (SP - 8.U(16.W)).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_2215 + 7.U),
          arrayRegFiles(__tmp_2215 + 6.U),
          arrayRegFiles(__tmp_2215 + 5.U),
          arrayRegFiles(__tmp_2215 + 4.U),
          arrayRegFiles(__tmp_2215 + 3.U),
          arrayRegFiles(__tmp_2215 + 2.U),
          arrayRegFiles(__tmp_2215 + 1.U),
          arrayRegFiles(__tmp_2215 + 0.U)
        ).asUInt

        val __tmp_2216 = (Cat(
          arrayRegFiles(SP + 2.U(16.W) + 1.U),
          arrayRegFiles(SP + 2.U(16.W) + 0.U)
        )).asUInt
        generalRegFiles(1.U) := Cat(
          arrayRegFiles(__tmp_2216 + 7.U),
          arrayRegFiles(__tmp_2216 + 6.U),
          arrayRegFiles(__tmp_2216 + 5.U),
          arrayRegFiles(__tmp_2216 + 4.U),
          arrayRegFiles(__tmp_2216 + 3.U),
          arrayRegFiles(__tmp_2216 + 2.U),
          arrayRegFiles(__tmp_2216 + 1.U),
          arrayRegFiles(__tmp_2216 + 0.U)
        ).asUInt

        CP := 367.U
      }

      is(367.U) {
        /*
        SP = SP - 127
        goto .368
        */


        SP := SP - 127.U

        CP := 368.U
      }

      is(368.U) {
        /*
        DP = DP + ($1 as DP)
        goto .369
        */


        DP := generalRegFiles(1.U).asUInt

        CP := 369.U
      }

      is(369.U) {
        /*
        unalloc printStackTrace$res@[618,7].00AE355F: U64 [@111, 8]
        goto .1
        */


        CP := 1.U
      }

      is(370.U) {
        /*
        *((*(22 [SP]) + (12 [SP])) + ((DP & (127 [DP])) as SP)) = (10 [U8]) [unsigned, U8, 1]  // $display((DP & (127 [DP]))) = (10 [U8])
        goto .371
        */


        val __tmp_2217 = Cat(
          arrayRegFiles(22.U(16.W) + 1.U),
          arrayRegFiles(22.U(16.W) + 0.U)
        ) + 12.U(16.W) + DP & 127.U(64.W).asUInt
        val __tmp_2218 = (10.U(8.W)).asUInt
        arrayRegFiles(__tmp_2217 + 0.U) := __tmp_2218(7, 0)

        CP := 371.U
      }

      is(371.U) {
        /*
        DP = DP + 1
        goto .87
        */


        DP := DP + 1.U

        CP := 87.U
      }

      is(372.U) {
        /*
        $0 = *(SP - (8 [SP])) [unsigned, U64, 8]  // restore $0
        $1 = **(SP + (2 [SP])) [unsigned, U64, 8]  // $1 = $ret
        undecl sfCallerOffset: anvil.PrinterIndex.U [@132, 8], sizeSize: anvil.PrinterIndex.U [@124, 8], locSize: anvil.PrinterIndex.U [@116, 8], typeShaSize: anvil.PrinterIndex.U [@108, 8], spSize: anvil.PrinterIndex.U [@100, 8], mask: anvil.PrinterIndex.U [@92, 8], memory: SP [@90, 2], index: anvil.PrinterIndex.U [@82, 8], buffer: SP [@80, 2], $sfCurrentId: SP [@78, 2], $sfDesc: IS[56, U8] [@10, 68], $sfLoc: U32 [@6, 4], $sfCaller: SP [@4, 2], $res: SP [@2, 2], $ret: CP [@0, 2]
        goto .373
        */


        val __tmp_2219 = (SP - 8.U(16.W)).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_2219 + 7.U),
          arrayRegFiles(__tmp_2219 + 6.U),
          arrayRegFiles(__tmp_2219 + 5.U),
          arrayRegFiles(__tmp_2219 + 4.U),
          arrayRegFiles(__tmp_2219 + 3.U),
          arrayRegFiles(__tmp_2219 + 2.U),
          arrayRegFiles(__tmp_2219 + 1.U),
          arrayRegFiles(__tmp_2219 + 0.U)
        ).asUInt

        val __tmp_2220 = (Cat(
          arrayRegFiles(SP + 2.U(16.W) + 1.U),
          arrayRegFiles(SP + 2.U(16.W) + 0.U)
        )).asUInt
        generalRegFiles(1.U) := Cat(
          arrayRegFiles(__tmp_2220 + 7.U),
          arrayRegFiles(__tmp_2220 + 6.U),
          arrayRegFiles(__tmp_2220 + 5.U),
          arrayRegFiles(__tmp_2220 + 4.U),
          arrayRegFiles(__tmp_2220 + 3.U),
          arrayRegFiles(__tmp_2220 + 2.U),
          arrayRegFiles(__tmp_2220 + 1.U),
          arrayRegFiles(__tmp_2220 + 0.U)
        ).asUInt

        CP := 373.U
      }

      is(373.U) {
        /*
        SP = SP - 127
        goto .368
        */


        SP := SP - 127.U

        CP := 368.U
      }

      is(374.U) {
        /*
        $0 = *(SP - (8 [SP])) [unsigned, U64, 8]  // restore $0
        $1 = **(SP + (2 [SP])) [unsigned, U64, 8]  // $1 = $ret
        undecl sfCallerOffset: anvil.PrinterIndex.U [@132, 8], sizeSize: anvil.PrinterIndex.U [@124, 8], locSize: anvil.PrinterIndex.U [@116, 8], typeShaSize: anvil.PrinterIndex.U [@108, 8], spSize: anvil.PrinterIndex.U [@100, 8], mask: anvil.PrinterIndex.U [@92, 8], memory: SP [@90, 2], index: anvil.PrinterIndex.U [@82, 8], buffer: SP [@80, 2], $sfCurrentId: SP [@78, 2], $sfDesc: IS[56, U8] [@10, 68], $sfLoc: U32 [@6, 4], $sfCaller: SP [@4, 2], $res: SP [@2, 2], $ret: CP [@0, 2]
        goto .375
        */


        val __tmp_2221 = (SP - 8.U(16.W)).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_2221 + 7.U),
          arrayRegFiles(__tmp_2221 + 6.U),
          arrayRegFiles(__tmp_2221 + 5.U),
          arrayRegFiles(__tmp_2221 + 4.U),
          arrayRegFiles(__tmp_2221 + 3.U),
          arrayRegFiles(__tmp_2221 + 2.U),
          arrayRegFiles(__tmp_2221 + 1.U),
          arrayRegFiles(__tmp_2221 + 0.U)
        ).asUInt

        val __tmp_2222 = (Cat(
          arrayRegFiles(SP + 2.U(16.W) + 1.U),
          arrayRegFiles(SP + 2.U(16.W) + 0.U)
        )).asUInt
        generalRegFiles(1.U) := Cat(
          arrayRegFiles(__tmp_2222 + 7.U),
          arrayRegFiles(__tmp_2222 + 6.U),
          arrayRegFiles(__tmp_2222 + 5.U),
          arrayRegFiles(__tmp_2222 + 4.U),
          arrayRegFiles(__tmp_2222 + 3.U),
          arrayRegFiles(__tmp_2222 + 2.U),
          arrayRegFiles(__tmp_2222 + 1.U),
          arrayRegFiles(__tmp_2222 + 0.U)
        ).asUInt

        CP := 375.U
      }

      is(375.U) {
        /*
        SP = SP - 127
        goto .368
        */


        SP := SP - 127.U

        CP := 368.U
      }

    }

}
