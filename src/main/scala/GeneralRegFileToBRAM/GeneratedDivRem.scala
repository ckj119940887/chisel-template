package GeneralRegFileToBRAM
import chisel3._
import chisel3.util._
import chisel3.experimental._



class DivRemTest (val C_S_AXI_DATA_WIDTH:  Int = 32,
               val C_S_AXI_ADDR_WIDTH:  Int = 32,
               val ARRAY_REG_WIDTH:     Int = 8,
               val ARRAY_REG_DEPTH:     Int = 160,
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
        SP = 52
        DP = 0
        *(10: U32) = (886747591: U32) [unsigned, U32, 4]  // $display.$type (MS[anvil.PrinterIndex.U, U8]: 0x34DAB1C7)
        *(14: SP) = (32: Z) [signed, Z, 8]  // $display.size
        *(52: CP) = (0: CP) [unsigned, CP, 2]  // $ret
        goto .4
        */


        SP := 52.U(16.W)

        DP := 0.U(64.W)

        val __tmp_1955 = 10.U(32.W)
        val __tmp_1956 = (886747591.U(32.W)).asUInt
        arrayRegFiles(__tmp_1955 + 0.U) := __tmp_1956(7, 0)
        arrayRegFiles(__tmp_1955 + 1.U) := __tmp_1956(15, 8)
        arrayRegFiles(__tmp_1955 + 2.U) := __tmp_1956(23, 16)
        arrayRegFiles(__tmp_1955 + 3.U) := __tmp_1956(31, 24)

        val __tmp_1957 = 14.U(16.W)
        val __tmp_1958 = (32.S(64.W)).asUInt
        arrayRegFiles(__tmp_1957 + 0.U) := __tmp_1958(7, 0)
        arrayRegFiles(__tmp_1957 + 1.U) := __tmp_1958(15, 8)
        arrayRegFiles(__tmp_1957 + 2.U) := __tmp_1958(23, 16)
        arrayRegFiles(__tmp_1957 + 3.U) := __tmp_1958(31, 24)
        arrayRegFiles(__tmp_1957 + 4.U) := __tmp_1958(39, 32)
        arrayRegFiles(__tmp_1957 + 5.U) := __tmp_1958(47, 40)
        arrayRegFiles(__tmp_1957 + 6.U) := __tmp_1958(55, 48)
        arrayRegFiles(__tmp_1957 + 7.U) := __tmp_1958(63, 56)

        val __tmp_1959 = 52.U(16.W)
        val __tmp_1960 = (0.U(16.W)).asUInt
        arrayRegFiles(__tmp_1959 + 0.U) := __tmp_1960(7, 0)
        arrayRegFiles(__tmp_1959 + 1.U) := __tmp_1960(15, 8)

        CP := 4.U
      }

      is(4.U) {
        /*
        $0 = *(0: SP) [signed, Z, 8]  // $0 = $testNum
        goto .5
        */


        val __tmp_1961 = (0.U(16.W)).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_1961 + 7.U),
          arrayRegFiles(__tmp_1961 + 6.U),
          arrayRegFiles(__tmp_1961 + 5.U),
          arrayRegFiles(__tmp_1961 + 4.U),
          arrayRegFiles(__tmp_1961 + 3.U),
          arrayRegFiles(__tmp_1961 + 2.U),
          arrayRegFiles(__tmp_1961 + 1.U),
          arrayRegFiles(__tmp_1961 + 0.U)
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
        *SP = (9: CP) [unsigned, CP, 2]  // $ret@0 = 1330
        goto .8
        */


        val __tmp_1962 = SP
        val __tmp_1963 = (9.U(16.W)).asUInt
        arrayRegFiles(__tmp_1962 + 0.U) := __tmp_1963(7, 0)
        arrayRegFiles(__tmp_1962 + 1.U) := __tmp_1963(15, 8)

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
        alloc div$res@[16,11].B057FC07: S32 [@2, 4]
        goto .13
        */


        CP := 13.U
      }

      is(13.U) {
        /*
        SP = SP + 30
        goto .14
        */


        SP := SP + 30.U

        CP := 14.U
      }

      is(14.U) {
        /*
        *SP = (16: CP) [unsigned, CP, 2]  // $ret@0 = 1332
        *(SP + (2: SP)) = (SP - (28: SP)) [unsigned, SP, 2]  // $res@2 = -28
        $12 = (3233: S32)
        $13 = (2: S32)
        goto .15
        */


        val __tmp_1964 = SP
        val __tmp_1965 = (16.U(16.W)).asUInt
        arrayRegFiles(__tmp_1964 + 0.U) := __tmp_1965(7, 0)
        arrayRegFiles(__tmp_1964 + 1.U) := __tmp_1965(15, 8)

        val __tmp_1966 = (SP + 2.U(16.W))
        val __tmp_1967 = ((SP - 28.U(16.W))).asUInt
        arrayRegFiles(__tmp_1966 + 0.U) := __tmp_1967(7, 0)
        arrayRegFiles(__tmp_1966 + 1.U) := __tmp_1967(15, 8)


        generalRegFiles(12.U) := (3233.S(32.W)).asUInt


        generalRegFiles(13.U) := (2.S(32.W)).asUInt

        CP := 15.U
      }

      is(15.U) {
        /*
        decl $ret: CP [@0, 2], $res: SP [@2, 6], n: S32 @$0, m: S32 @$1
        $0 = ($12: S32)
        $1 = ($13: S32)
        goto .42
        */



        generalRegFiles(0.U) := (generalRegFiles(12.U).asSInt).asUInt


        generalRegFiles(1.U) := (generalRegFiles(13.U).asSInt).asUInt

        CP := 42.U
      }

      is(16.U) {
        /*
        $4 = **(SP + (2: SP)) [signed, S32, 4]  // $4 = $res
        undecl m: S32 @$1, n: S32 @$0, $res: SP [@2, 6], $ret: CP [@0, 2]
        goto .17
        */


        val __tmp_1968 = (Cat(
          arrayRegFiles((SP + 2.U(16.W)) + 1.U),
          arrayRegFiles((SP + 2.U(16.W)) + 0.U)
        )).asUInt
        generalRegFiles(4.U) := Cat(
          arrayRegFiles(__tmp_1968 + 3.U),
          arrayRegFiles(__tmp_1968 + 2.U),
          arrayRegFiles(__tmp_1968 + 1.U),
          arrayRegFiles(__tmp_1968 + 0.U)
        ).asSInt.pad(GENERAL_REG_WIDTH).asUInt

        CP := 17.U
      }

      is(17.U) {
        /*
        SP = SP - 30
        goto .18
        */


        SP := SP - 30.U

        CP := 18.U
      }

      is(18.U) {
        /*
        alloc printS64$res@[16,11].B057FC07: U64 [@6, 8]
        goto .19
        */


        CP := 19.U
      }

      is(19.U) {
        /*
        SP = SP + 30
        goto .20
        */


        SP := SP + 30.U

        CP := 20.U
      }

      is(20.U) {
        /*
        *SP = (22: CP) [unsigned, CP, 2]  // $ret@0 = 1334
        *(SP + (2: SP)) = (SP - (24: SP)) [unsigned, SP, 2]  // $res@2 = -24
        $91 = (8: SP)
        $92 = DP
        $93 = (31: anvil.PrinterIndex.U)
        $94 = (($4: S32) as S64)
        goto .21
        */


        val __tmp_1969 = SP
        val __tmp_1970 = (22.U(16.W)).asUInt
        arrayRegFiles(__tmp_1969 + 0.U) := __tmp_1970(7, 0)
        arrayRegFiles(__tmp_1969 + 1.U) := __tmp_1970(15, 8)

        val __tmp_1971 = (SP + 2.U(16.W))
        val __tmp_1972 = ((SP - 24.U(16.W))).asUInt
        arrayRegFiles(__tmp_1971 + 0.U) := __tmp_1972(7, 0)
        arrayRegFiles(__tmp_1971 + 1.U) := __tmp_1972(15, 8)


        generalRegFiles(91.U) := 8.U(16.W)


        generalRegFiles(92.U) := DP


        generalRegFiles(93.U) := 31.U(64.W)


        generalRegFiles(94.U) := (generalRegFiles(4.U).asSInt.asSInt).asUInt

        CP := 21.U
      }

      is(21.U) {
        /*
        decl $ret: CP [@0, 2], $res: SP [@2, 2], buffer: MS[anvil.PrinterIndex.U, U8] @$0, index: anvil.PrinterIndex.U @$1, mask: anvil.PrinterIndex.U @$2, n: S64 @$3
        $0 = ($91: MS[anvil.PrinterIndex.U, U8])
        $1 = ($92: anvil.PrinterIndex.U)
        $2 = ($93: anvil.PrinterIndex.U)
        $3 = ($94: S64)
        goto .50
        */



        generalRegFiles(0.U) := generalRegFiles(91.U)


        generalRegFiles(1.U) := generalRegFiles(92.U)


        generalRegFiles(2.U) := generalRegFiles(93.U)


        generalRegFiles(3.U) := (generalRegFiles(94.U).asSInt).asUInt

        CP := 50.U
      }

      is(22.U) {
        /*
        $5 = **(SP + (2: SP)) [unsigned, U64, 8]  // $5 = $res
        undecl n: S64 @$3, mask: anvil.PrinterIndex.U @$2, index: anvil.PrinterIndex.U @$1, buffer: MS[anvil.PrinterIndex.U, U8] @$0, $res: SP [@2, 2], $ret: CP [@0, 2]
        goto .23
        */


        val __tmp_1973 = (Cat(
          arrayRegFiles((SP + 2.U(16.W)) + 1.U),
          arrayRegFiles((SP + 2.U(16.W)) + 0.U)
        )).asUInt
        generalRegFiles(5.U) := Cat(
          arrayRegFiles(__tmp_1973 + 7.U),
          arrayRegFiles(__tmp_1973 + 6.U),
          arrayRegFiles(__tmp_1973 + 5.U),
          arrayRegFiles(__tmp_1973 + 4.U),
          arrayRegFiles(__tmp_1973 + 3.U),
          arrayRegFiles(__tmp_1973 + 2.U),
          arrayRegFiles(__tmp_1973 + 1.U),
          arrayRegFiles(__tmp_1973 + 0.U)
        ).asUInt

        CP := 23.U
      }

      is(23.U) {
        /*
        SP = SP - 30
        goto .24
        */


        SP := SP - 30.U

        CP := 24.U
      }

      is(24.U) {
        /*
        DP = DP + (($5: U64) as DP)
        goto .25
        */


        DP := DP + generalRegFiles(5.U).asUInt

        CP := 25.U
      }

      is(25.U) {
        /*
        *(((8: SP) + (12: SP)) + ((DP & (31: DP)) as SP)) = (10: U8) [unsigned, U8, 1]  // $display((DP & (31: DP))) = (10: U8)
        goto .26
        */


        val __tmp_1974 = ((8.U(16.W) + 12.U(16.W)) + (DP & 31.U(64.W)).asUInt)
        val __tmp_1975 = (10.U(8.W)).asUInt
        arrayRegFiles(__tmp_1974 + 0.U) := __tmp_1975(7, 0)

        CP := 26.U
      }

      is(26.U) {
        /*
        DP = DP + 1
        goto .27
        */


        DP := DP + 1.U

        CP := 27.U
      }

      is(27.U) {
        /*
        alloc rem$res@[17,11].77F4D3A2: U64 [@14, 8]
        goto .28
        */


        CP := 28.U
      }

      is(28.U) {
        /*
        SP = SP + 30
        goto .29
        */


        SP := SP + 30.U

        CP := 29.U
      }

      is(29.U) {
        /*
        *SP = (31: CP) [unsigned, CP, 2]  // $ret@0 = 1336
        *(SP + (2: SP)) = (SP - (16: SP)) [unsigned, SP, 2]  // $res@2 = -16
        $12 = (255: U64)
        $13 = (16: U64)
        goto .30
        */


        val __tmp_1976 = SP
        val __tmp_1977 = (31.U(16.W)).asUInt
        arrayRegFiles(__tmp_1976 + 0.U) := __tmp_1977(7, 0)
        arrayRegFiles(__tmp_1976 + 1.U) := __tmp_1977(15, 8)

        val __tmp_1978 = (SP + 2.U(16.W))
        val __tmp_1979 = ((SP - 16.U(16.W))).asUInt
        arrayRegFiles(__tmp_1978 + 0.U) := __tmp_1979(7, 0)
        arrayRegFiles(__tmp_1978 + 1.U) := __tmp_1979(15, 8)


        generalRegFiles(12.U) := 255.U(64.W)


        generalRegFiles(13.U) := 16.U(64.W)

        CP := 30.U
      }

      is(30.U) {
        /*
        decl $ret: CP [@0, 2], $res: SP [@2, 10], n: U64 @$0, m: U64 @$1
        $0 = ($12: U64)
        $1 = ($13: U64)
        goto .195
        */



        generalRegFiles(0.U) := generalRegFiles(12.U)


        generalRegFiles(1.U) := generalRegFiles(13.U)

        CP := 195.U
      }

      is(31.U) {
        /*
        $4 = **(SP + (2: SP)) [unsigned, U64, 8]  // $4 = $res
        undecl m: U64 @$1, n: U64 @$0, $res: SP [@2, 10], $ret: CP [@0, 2]
        goto .32
        */


        val __tmp_1980 = (Cat(
          arrayRegFiles((SP + 2.U(16.W)) + 1.U),
          arrayRegFiles((SP + 2.U(16.W)) + 0.U)
        )).asUInt
        generalRegFiles(4.U) := Cat(
          arrayRegFiles(__tmp_1980 + 7.U),
          arrayRegFiles(__tmp_1980 + 6.U),
          arrayRegFiles(__tmp_1980 + 5.U),
          arrayRegFiles(__tmp_1980 + 4.U),
          arrayRegFiles(__tmp_1980 + 3.U),
          arrayRegFiles(__tmp_1980 + 2.U),
          arrayRegFiles(__tmp_1980 + 1.U),
          arrayRegFiles(__tmp_1980 + 0.U)
        ).asUInt

        CP := 32.U
      }

      is(32.U) {
        /*
        SP = SP - 30
        goto .33
        */


        SP := SP - 30.U

        CP := 33.U
      }

      is(33.U) {
        /*
        alloc printU64Hex$res@[17,11].77F4D3A2: U64 [@22, 8]
        goto .34
        */


        CP := 34.U
      }

      is(34.U) {
        /*
        SP = SP + 30
        goto .35
        */


        SP := SP + 30.U

        CP := 35.U
      }

      is(35.U) {
        /*
        *SP = (37: CP) [unsigned, CP, 2]  // $ret@0 = 1338
        *(SP + (2: SP)) = (SP - (8: SP)) [unsigned, SP, 2]  // $res@2 = -8
        $91 = (8: SP)
        $92 = DP
        $93 = (31: anvil.PrinterIndex.U)
        $94 = ($4: U64)
        $95 = (16: Z)
        goto .36
        */


        val __tmp_1981 = SP
        val __tmp_1982 = (37.U(16.W)).asUInt
        arrayRegFiles(__tmp_1981 + 0.U) := __tmp_1982(7, 0)
        arrayRegFiles(__tmp_1981 + 1.U) := __tmp_1982(15, 8)

        val __tmp_1983 = (SP + 2.U(16.W))
        val __tmp_1984 = ((SP - 8.U(16.W))).asUInt
        arrayRegFiles(__tmp_1983 + 0.U) := __tmp_1984(7, 0)
        arrayRegFiles(__tmp_1983 + 1.U) := __tmp_1984(15, 8)


        generalRegFiles(91.U) := 8.U(16.W)


        generalRegFiles(92.U) := DP


        generalRegFiles(93.U) := 31.U(64.W)


        generalRegFiles(94.U) := generalRegFiles(4.U)


        generalRegFiles(95.U) := (16.S(64.W)).asUInt

        CP := 36.U
      }

      is(36.U) {
        /*
        decl $ret: CP [@0, 2], $res: SP [@2, 2], buffer: MS[anvil.PrinterIndex.U, U8] @$0, index: anvil.PrinterIndex.U @$1, mask: anvil.PrinterIndex.U @$2, n: U64 @$3, digits: Z @$4
        $0 = ($91: MS[anvil.PrinterIndex.U, U8])
        $1 = ($92: anvil.PrinterIndex.U)
        $2 = ($93: anvil.PrinterIndex.U)
        $3 = ($94: U64)
        $4 = ($95: Z)
        goto .203
        */



        generalRegFiles(0.U) := generalRegFiles(91.U)


        generalRegFiles(1.U) := generalRegFiles(92.U)


        generalRegFiles(2.U) := generalRegFiles(93.U)


        generalRegFiles(3.U) := generalRegFiles(94.U)


        generalRegFiles(4.U) := (generalRegFiles(95.U).asSInt).asUInt

        CP := 203.U
      }

      is(37.U) {
        /*
        $5 = **(SP + (2: SP)) [unsigned, U64, 8]  // $5 = $res
        undecl digits: Z @$4, n: U64 @$3, mask: anvil.PrinterIndex.U @$2, index: anvil.PrinterIndex.U @$1, buffer: MS[anvil.PrinterIndex.U, U8] @$0, $res: SP [@2, 2], $ret: CP [@0, 2]
        goto .38
        */


        val __tmp_1985 = (Cat(
          arrayRegFiles((SP + 2.U(16.W)) + 1.U),
          arrayRegFiles((SP + 2.U(16.W)) + 0.U)
        )).asUInt
        generalRegFiles(5.U) := Cat(
          arrayRegFiles(__tmp_1985 + 7.U),
          arrayRegFiles(__tmp_1985 + 6.U),
          arrayRegFiles(__tmp_1985 + 5.U),
          arrayRegFiles(__tmp_1985 + 4.U),
          arrayRegFiles(__tmp_1985 + 3.U),
          arrayRegFiles(__tmp_1985 + 2.U),
          arrayRegFiles(__tmp_1985 + 1.U),
          arrayRegFiles(__tmp_1985 + 0.U)
        ).asUInt

        CP := 38.U
      }

      is(38.U) {
        /*
        SP = SP - 30
        goto .39
        */


        SP := SP - 30.U

        CP := 39.U
      }

      is(39.U) {
        /*
        DP = DP + (($5: U64) as DP)
        goto .40
        */


        DP := DP + generalRegFiles(5.U).asUInt

        CP := 40.U
      }

      is(40.U) {
        /*
        *(((8: SP) + (12: SP)) + ((DP & (31: DP)) as SP)) = (10: U8) [unsigned, U8, 1]  // $display((DP & (31: DP))) = (10: U8)
        goto .41
        */


        val __tmp_1986 = ((8.U(16.W) + 12.U(16.W)) + (DP & 31.U(64.W)).asUInt)
        val __tmp_1987 = (10.U(8.W)).asUInt
        arrayRegFiles(__tmp_1986 + 0.U) := __tmp_1987(7, 0)

        CP := 41.U
      }

      is(41.U) {
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

      is(42.U) {
        /*
        if (($1: S32) ≢ (0: S32)) goto .48 else goto .43
        */


        CP := Mux(((generalRegFiles(1.U).asSInt =/= 0.S(32.W)).asUInt.asUInt) === 1.U, 48.U, 43.U)
      }

      is(43.U) {
        /*
        *(((8: SP) + (12: SP)) + ((DP & (31: DP)) as SP)) = (68: U8) [unsigned, U8, 1]  // $display((DP & (31: DP))) = (68: U8)
        *(((8: SP) + (12: SP)) + (((DP + (1: DP)) & (31: DP)) as SP)) = (105: U8) [unsigned, U8, 1]  // $display(((DP + (1: DP)) & (31: DP))) = (105: U8)
        *(((8: SP) + (12: SP)) + (((DP + (2: DP)) & (31: DP)) as SP)) = (118: U8) [unsigned, U8, 1]  // $display(((DP + (2: DP)) & (31: DP))) = (118: U8)
        *(((8: SP) + (12: SP)) + (((DP + (3: DP)) & (31: DP)) as SP)) = (105: U8) [unsigned, U8, 1]  // $display(((DP + (3: DP)) & (31: DP))) = (105: U8)
        *(((8: SP) + (12: SP)) + (((DP + (4: DP)) & (31: DP)) as SP)) = (115: U8) [unsigned, U8, 1]  // $display(((DP + (4: DP)) & (31: DP))) = (115: U8)
        *(((8: SP) + (12: SP)) + (((DP + (5: DP)) & (31: DP)) as SP)) = (105: U8) [unsigned, U8, 1]  // $display(((DP + (5: DP)) & (31: DP))) = (105: U8)
        *(((8: SP) + (12: SP)) + (((DP + (6: DP)) & (31: DP)) as SP)) = (111: U8) [unsigned, U8, 1]  // $display(((DP + (6: DP)) & (31: DP))) = (111: U8)
        *(((8: SP) + (12: SP)) + (((DP + (7: DP)) & (31: DP)) as SP)) = (110: U8) [unsigned, U8, 1]  // $display(((DP + (7: DP)) & (31: DP))) = (110: U8)
        *(((8: SP) + (12: SP)) + (((DP + (8: DP)) & (31: DP)) as SP)) = (32: U8) [unsigned, U8, 1]  // $display(((DP + (8: DP)) & (31: DP))) = (32: U8)
        *(((8: SP) + (12: SP)) + (((DP + (9: DP)) & (31: DP)) as SP)) = (98: U8) [unsigned, U8, 1]  // $display(((DP + (9: DP)) & (31: DP))) = (98: U8)
        *(((8: SP) + (12: SP)) + (((DP + (10: DP)) & (31: DP)) as SP)) = (121: U8) [unsigned, U8, 1]  // $display(((DP + (10: DP)) & (31: DP))) = (121: U8)
        *(((8: SP) + (12: SP)) + (((DP + (11: DP)) & (31: DP)) as SP)) = (32: U8) [unsigned, U8, 1]  // $display(((DP + (11: DP)) & (31: DP))) = (32: U8)
        *(((8: SP) + (12: SP)) + (((DP + (12: DP)) & (31: DP)) as SP)) = (122: U8) [unsigned, U8, 1]  // $display(((DP + (12: DP)) & (31: DP))) = (122: U8)
        *(((8: SP) + (12: SP)) + (((DP + (13: DP)) & (31: DP)) as SP)) = (101: U8) [unsigned, U8, 1]  // $display(((DP + (13: DP)) & (31: DP))) = (101: U8)
        *(((8: SP) + (12: SP)) + (((DP + (14: DP)) & (31: DP)) as SP)) = (114: U8) [unsigned, U8, 1]  // $display(((DP + (14: DP)) & (31: DP))) = (114: U8)
        *(((8: SP) + (12: SP)) + (((DP + (15: DP)) & (31: DP)) as SP)) = (111: U8) [unsigned, U8, 1]  // $display(((DP + (15: DP)) & (31: DP))) = (111: U8)
        goto .44
        */


        val __tmp_1988 = ((8.U(16.W) + 12.U(16.W)) + (DP & 31.U(64.W)).asUInt)
        val __tmp_1989 = (68.U(8.W)).asUInt
        arrayRegFiles(__tmp_1988 + 0.U) := __tmp_1989(7, 0)

        val __tmp_1990 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 1.U(64.W)) & 31.U(64.W)).asUInt)
        val __tmp_1991 = (105.U(8.W)).asUInt
        arrayRegFiles(__tmp_1990 + 0.U) := __tmp_1991(7, 0)

        val __tmp_1992 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 2.U(64.W)) & 31.U(64.W)).asUInt)
        val __tmp_1993 = (118.U(8.W)).asUInt
        arrayRegFiles(__tmp_1992 + 0.U) := __tmp_1993(7, 0)

        val __tmp_1994 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 3.U(64.W)) & 31.U(64.W)).asUInt)
        val __tmp_1995 = (105.U(8.W)).asUInt
        arrayRegFiles(__tmp_1994 + 0.U) := __tmp_1995(7, 0)

        val __tmp_1996 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 4.U(64.W)) & 31.U(64.W)).asUInt)
        val __tmp_1997 = (115.U(8.W)).asUInt
        arrayRegFiles(__tmp_1996 + 0.U) := __tmp_1997(7, 0)

        val __tmp_1998 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 5.U(64.W)) & 31.U(64.W)).asUInt)
        val __tmp_1999 = (105.U(8.W)).asUInt
        arrayRegFiles(__tmp_1998 + 0.U) := __tmp_1999(7, 0)

        val __tmp_2000 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 6.U(64.W)) & 31.U(64.W)).asUInt)
        val __tmp_2001 = (111.U(8.W)).asUInt
        arrayRegFiles(__tmp_2000 + 0.U) := __tmp_2001(7, 0)

        val __tmp_2002 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 7.U(64.W)) & 31.U(64.W)).asUInt)
        val __tmp_2003 = (110.U(8.W)).asUInt
        arrayRegFiles(__tmp_2002 + 0.U) := __tmp_2003(7, 0)

        val __tmp_2004 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 8.U(64.W)) & 31.U(64.W)).asUInt)
        val __tmp_2005 = (32.U(8.W)).asUInt
        arrayRegFiles(__tmp_2004 + 0.U) := __tmp_2005(7, 0)

        val __tmp_2006 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 9.U(64.W)) & 31.U(64.W)).asUInt)
        val __tmp_2007 = (98.U(8.W)).asUInt
        arrayRegFiles(__tmp_2006 + 0.U) := __tmp_2007(7, 0)

        val __tmp_2008 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 10.U(64.W)) & 31.U(64.W)).asUInt)
        val __tmp_2009 = (121.U(8.W)).asUInt
        arrayRegFiles(__tmp_2008 + 0.U) := __tmp_2009(7, 0)

        val __tmp_2010 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 11.U(64.W)) & 31.U(64.W)).asUInt)
        val __tmp_2011 = (32.U(8.W)).asUInt
        arrayRegFiles(__tmp_2010 + 0.U) := __tmp_2011(7, 0)

        val __tmp_2012 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 12.U(64.W)) & 31.U(64.W)).asUInt)
        val __tmp_2013 = (122.U(8.W)).asUInt
        arrayRegFiles(__tmp_2012 + 0.U) := __tmp_2013(7, 0)

        val __tmp_2014 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 13.U(64.W)) & 31.U(64.W)).asUInt)
        val __tmp_2015 = (101.U(8.W)).asUInt
        arrayRegFiles(__tmp_2014 + 0.U) := __tmp_2015(7, 0)

        val __tmp_2016 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 14.U(64.W)) & 31.U(64.W)).asUInt)
        val __tmp_2017 = (114.U(8.W)).asUInt
        arrayRegFiles(__tmp_2016 + 0.U) := __tmp_2017(7, 0)

        val __tmp_2018 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 15.U(64.W)) & 31.U(64.W)).asUInt)
        val __tmp_2019 = (111.U(8.W)).asUInt
        arrayRegFiles(__tmp_2018 + 0.U) := __tmp_2019(7, 0)

        CP := 44.U
      }

      is(44.U) {
        /*
        DP = DP + 16
        goto .45
        */


        DP := DP + 16.U

        CP := 45.U
      }

      is(45.U) {
        /*
        *(((8: SP) + (12: SP)) + ((DP & (31: DP)) as SP)) = (10: U8) [unsigned, U8, 1]  // $display((DP & (31: DP))) = (10: U8)
        goto .46
        */


        val __tmp_2020 = ((8.U(16.W) + 12.U(16.W)) + (DP & 31.U(64.W)).asUInt)
        val __tmp_2021 = (10.U(8.W)).asUInt
        arrayRegFiles(__tmp_2020 + 0.U) := __tmp_2021(7, 0)

        CP := 46.U
      }

      is(46.U) {
        /*
        DP = DP + 1
        goto .1
        */


        DP := DP + 1.U

        CP := 1.U
      }

      is(48.U) {
        /*
        $2 = (($0: S32) / ($1: S32))
        goto .49
        */



        generalRegFiles(2.U) := ((generalRegFiles(0.U).asSInt / generalRegFiles(1.U).asSInt)).asUInt

        CP := 49.U
      }

      is(49.U) {
        /*
        **(SP + (2: SP)) = ($2: S32) [signed, S32, 4]  // $res = ($2: S32)
        goto $ret@0
        */


        val __tmp_2022 = Cat(
          arrayRegFiles((SP + 2.U(16.W)) + 1.U),
          arrayRegFiles((SP + 2.U(16.W)) + 0.U)
        )
        val __tmp_2023 = (generalRegFiles(2.U).asSInt).asUInt
        arrayRegFiles(__tmp_2022 + 0.U) := __tmp_2023(7, 0)
        arrayRegFiles(__tmp_2022 + 1.U) := __tmp_2023(15, 8)
        arrayRegFiles(__tmp_2022 + 2.U) := __tmp_2023(23, 16)
        arrayRegFiles(__tmp_2022 + 3.U) := __tmp_2023(31, 24)

        CP := Cat(
          arrayRegFiles(SP + 0.U + 1.U),
          arrayRegFiles(SP + 0.U + 0.U)
        )

      }

      is(50.U) {
        /*
        $10 = (($3: S64) ≡ (-9223372036854775808: S64))
        goto .51
        */



        generalRegFiles(10.U) := (generalRegFiles(3.U).asSInt === BigInt("-9223372036854775808").S(64.W)).asUInt

        CP := 51.U
      }

      is(51.U) {
        /*
        if ($10: B) goto .52 else goto .112
        */


        CP := Mux((generalRegFiles(10.U).asUInt) === 1.U, 52.U, 112.U)
      }

      is(52.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $12 = (($1: anvil.PrinterIndex.U) & ($2: anvil.PrinterIndex.U))
        goto .53
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(12.U) := (generalRegFiles(1.U) & generalRegFiles(2.U))

        CP := 53.U
      }

      is(53.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($12: anvil.PrinterIndex.U) as SP)) = (45: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($12: anvil.PrinterIndex.U)) = (45: U8)
        goto .54
        */


        val __tmp_2024 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(12.U).asUInt)
        val __tmp_2025 = (45.U(8.W)).asUInt
        arrayRegFiles(__tmp_2024 + 0.U) := __tmp_2025(7, 0)

        CP := 54.U
      }

      is(54.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $13 = (($1: anvil.PrinterIndex.U) + (1: anvil.PrinterIndex.U))
        goto .55
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(13.U) := (generalRegFiles(1.U) + 1.U(64.W))

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
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($14: anvil.PrinterIndex.U) as SP)) = (57: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($14: anvil.PrinterIndex.U)) = (57: U8)
        goto .57
        */


        val __tmp_2026 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(14.U).asUInt)
        val __tmp_2027 = (57.U(8.W)).asUInt
        arrayRegFiles(__tmp_2026 + 0.U) := __tmp_2027(7, 0)

        CP := 57.U
      }

      is(57.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $13 = (($1: anvil.PrinterIndex.U) + (2: anvil.PrinterIndex.U))
        goto .58
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(13.U) := (generalRegFiles(1.U) + 2.U(64.W))

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


        val __tmp_2028 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(14.U).asUInt)
        val __tmp_2029 = (50.U(8.W)).asUInt
        arrayRegFiles(__tmp_2028 + 0.U) := __tmp_2029(7, 0)

        CP := 60.U
      }

      is(60.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $13 = (($1: anvil.PrinterIndex.U) + (3: anvil.PrinterIndex.U))
        goto .61
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(13.U) := (generalRegFiles(1.U) + 3.U(64.W))

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
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($14: anvil.PrinterIndex.U) as SP)) = (50: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($14: anvil.PrinterIndex.U)) = (50: U8)
        goto .63
        */


        val __tmp_2030 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(14.U).asUInt)
        val __tmp_2031 = (50.U(8.W)).asUInt
        arrayRegFiles(__tmp_2030 + 0.U) := __tmp_2031(7, 0)

        CP := 63.U
      }

      is(63.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $13 = (($1: anvil.PrinterIndex.U) + (4: anvil.PrinterIndex.U))
        goto .64
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(13.U) := (generalRegFiles(1.U) + 4.U(64.W))

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


        val __tmp_2032 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(14.U).asUInt)
        val __tmp_2033 = (51.U(8.W)).asUInt
        arrayRegFiles(__tmp_2032 + 0.U) := __tmp_2033(7, 0)

        CP := 66.U
      }

      is(66.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $13 = (($1: anvil.PrinterIndex.U) + (5: anvil.PrinterIndex.U))
        goto .67
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(13.U) := (generalRegFiles(1.U) + 5.U(64.W))

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
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($14: anvil.PrinterIndex.U) as SP)) = (51: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($14: anvil.PrinterIndex.U)) = (51: U8)
        goto .69
        */


        val __tmp_2034 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(14.U).asUInt)
        val __tmp_2035 = (51.U(8.W)).asUInt
        arrayRegFiles(__tmp_2034 + 0.U) := __tmp_2035(7, 0)

        CP := 69.U
      }

      is(69.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $13 = (($1: anvil.PrinterIndex.U) + (6: anvil.PrinterIndex.U))
        goto .70
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(13.U) := (generalRegFiles(1.U) + 6.U(64.W))

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
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($14: anvil.PrinterIndex.U) as SP)) = (55: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($14: anvil.PrinterIndex.U)) = (55: U8)
        goto .72
        */


        val __tmp_2036 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(14.U).asUInt)
        val __tmp_2037 = (55.U(8.W)).asUInt
        arrayRegFiles(__tmp_2036 + 0.U) := __tmp_2037(7, 0)

        CP := 72.U
      }

      is(72.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $13 = (($1: anvil.PrinterIndex.U) + (7: anvil.PrinterIndex.U))
        goto .73
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(13.U) := (generalRegFiles(1.U) + 7.U(64.W))

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
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($14: anvil.PrinterIndex.U) as SP)) = (50: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($14: anvil.PrinterIndex.U)) = (50: U8)
        goto .75
        */


        val __tmp_2038 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(14.U).asUInt)
        val __tmp_2039 = (50.U(8.W)).asUInt
        arrayRegFiles(__tmp_2038 + 0.U) := __tmp_2039(7, 0)

        CP := 75.U
      }

      is(75.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $13 = (($1: anvil.PrinterIndex.U) + (8: anvil.PrinterIndex.U))
        goto .76
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(13.U) := (generalRegFiles(1.U) + 8.U(64.W))

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
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($14: anvil.PrinterIndex.U) as SP)) = (48: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($14: anvil.PrinterIndex.U)) = (48: U8)
        goto .78
        */


        val __tmp_2040 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(14.U).asUInt)
        val __tmp_2041 = (48.U(8.W)).asUInt
        arrayRegFiles(__tmp_2040 + 0.U) := __tmp_2041(7, 0)

        CP := 78.U
      }

      is(78.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $13 = (($1: anvil.PrinterIndex.U) + (9: anvil.PrinterIndex.U))
        goto .79
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(13.U) := (generalRegFiles(1.U) + 9.U(64.W))

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
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($14: anvil.PrinterIndex.U) as SP)) = (51: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($14: anvil.PrinterIndex.U)) = (51: U8)
        goto .81
        */


        val __tmp_2042 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(14.U).asUInt)
        val __tmp_2043 = (51.U(8.W)).asUInt
        arrayRegFiles(__tmp_2042 + 0.U) := __tmp_2043(7, 0)

        CP := 81.U
      }

      is(81.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $13 = (($1: anvil.PrinterIndex.U) + (10: anvil.PrinterIndex.U))
        goto .82
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(13.U) := (generalRegFiles(1.U) + 10.U(64.W))

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
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($14: anvil.PrinterIndex.U) as SP)) = (54: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($14: anvil.PrinterIndex.U)) = (54: U8)
        goto .84
        */


        val __tmp_2044 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(14.U).asUInt)
        val __tmp_2045 = (54.U(8.W)).asUInt
        arrayRegFiles(__tmp_2044 + 0.U) := __tmp_2045(7, 0)

        CP := 84.U
      }

      is(84.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $13 = (($1: anvil.PrinterIndex.U) + (11: anvil.PrinterIndex.U))
        goto .85
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(13.U) := (generalRegFiles(1.U) + 11.U(64.W))

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
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($14: anvil.PrinterIndex.U) as SP)) = (56: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($14: anvil.PrinterIndex.U)) = (56: U8)
        goto .87
        */


        val __tmp_2046 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(14.U).asUInt)
        val __tmp_2047 = (56.U(8.W)).asUInt
        arrayRegFiles(__tmp_2046 + 0.U) := __tmp_2047(7, 0)

        CP := 87.U
      }

      is(87.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $13 = (($1: anvil.PrinterIndex.U) + (12: anvil.PrinterIndex.U))
        goto .88
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(13.U) := (generalRegFiles(1.U) + 12.U(64.W))

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
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($14: anvil.PrinterIndex.U) as SP)) = (53: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($14: anvil.PrinterIndex.U)) = (53: U8)
        goto .90
        */


        val __tmp_2048 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(14.U).asUInt)
        val __tmp_2049 = (53.U(8.W)).asUInt
        arrayRegFiles(__tmp_2048 + 0.U) := __tmp_2049(7, 0)

        CP := 90.U
      }

      is(90.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $13 = (($1: anvil.PrinterIndex.U) + (13: anvil.PrinterIndex.U))
        goto .91
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(13.U) := (generalRegFiles(1.U) + 13.U(64.W))

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
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($14: anvil.PrinterIndex.U) as SP)) = (52: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($14: anvil.PrinterIndex.U)) = (52: U8)
        goto .93
        */


        val __tmp_2050 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(14.U).asUInt)
        val __tmp_2051 = (52.U(8.W)).asUInt
        arrayRegFiles(__tmp_2050 + 0.U) := __tmp_2051(7, 0)

        CP := 93.U
      }

      is(93.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $13 = (($1: anvil.PrinterIndex.U) + (14: anvil.PrinterIndex.U))
        goto .94
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(13.U) := (generalRegFiles(1.U) + 14.U(64.W))

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
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($14: anvil.PrinterIndex.U) as SP)) = (55: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($14: anvil.PrinterIndex.U)) = (55: U8)
        goto .96
        */


        val __tmp_2052 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(14.U).asUInt)
        val __tmp_2053 = (55.U(8.W)).asUInt
        arrayRegFiles(__tmp_2052 + 0.U) := __tmp_2053(7, 0)

        CP := 96.U
      }

      is(96.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $13 = (($1: anvil.PrinterIndex.U) + (15: anvil.PrinterIndex.U))
        goto .97
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(13.U) := (generalRegFiles(1.U) + 15.U(64.W))

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


        val __tmp_2054 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(14.U).asUInt)
        val __tmp_2055 = (55.U(8.W)).asUInt
        arrayRegFiles(__tmp_2054 + 0.U) := __tmp_2055(7, 0)

        CP := 99.U
      }

      is(99.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $13 = (($1: anvil.PrinterIndex.U) + (16: anvil.PrinterIndex.U))
        goto .100
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(13.U) := (generalRegFiles(1.U) + 16.U(64.W))

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
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($14: anvil.PrinterIndex.U) as SP)) = (53: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($14: anvil.PrinterIndex.U)) = (53: U8)
        goto .102
        */


        val __tmp_2056 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(14.U).asUInt)
        val __tmp_2057 = (53.U(8.W)).asUInt
        arrayRegFiles(__tmp_2056 + 0.U) := __tmp_2057(7, 0)

        CP := 102.U
      }

      is(102.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $13 = (($1: anvil.PrinterIndex.U) + (17: anvil.PrinterIndex.U))
        goto .103
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(13.U) := (generalRegFiles(1.U) + 17.U(64.W))

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
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($14: anvil.PrinterIndex.U) as SP)) = (56: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($14: anvil.PrinterIndex.U)) = (56: U8)
        goto .105
        */


        val __tmp_2058 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(14.U).asUInt)
        val __tmp_2059 = (56.U(8.W)).asUInt
        arrayRegFiles(__tmp_2058 + 0.U) := __tmp_2059(7, 0)

        CP := 105.U
      }

      is(105.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $13 = (($1: anvil.PrinterIndex.U) + (18: anvil.PrinterIndex.U))
        goto .106
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(13.U) := (generalRegFiles(1.U) + 18.U(64.W))

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
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($14: anvil.PrinterIndex.U) as SP)) = (48: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($14: anvil.PrinterIndex.U)) = (48: U8)
        goto .108
        */


        val __tmp_2060 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(14.U).asUInt)
        val __tmp_2061 = (48.U(8.W)).asUInt
        arrayRegFiles(__tmp_2060 + 0.U) := __tmp_2061(7, 0)

        CP := 108.U
      }

      is(108.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $13 = (($1: anvil.PrinterIndex.U) + (19: anvil.PrinterIndex.U))
        goto .109
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(13.U) := (generalRegFiles(1.U) + 19.U(64.W))

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
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($14: anvil.PrinterIndex.U) as SP)) = (56: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($14: anvil.PrinterIndex.U)) = (56: U8)
        goto .111
        */


        val __tmp_2062 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(14.U).asUInt)
        val __tmp_2063 = (56.U(8.W)).asUInt
        arrayRegFiles(__tmp_2062 + 0.U) := __tmp_2063(7, 0)

        CP := 111.U
      }

      is(111.U) {
        /*
        **(SP + (2: SP)) = (20: U64) [unsigned, U64, 8]  // $res = (20: U64)
        goto $ret@0
        */


        val __tmp_2064 = Cat(
          arrayRegFiles((SP + 2.U(16.W)) + 1.U),
          arrayRegFiles((SP + 2.U(16.W)) + 0.U)
        )
        val __tmp_2065 = (20.U(64.W)).asUInt
        arrayRegFiles(__tmp_2064 + 0.U) := __tmp_2065(7, 0)
        arrayRegFiles(__tmp_2064 + 1.U) := __tmp_2065(15, 8)
        arrayRegFiles(__tmp_2064 + 2.U) := __tmp_2065(23, 16)
        arrayRegFiles(__tmp_2064 + 3.U) := __tmp_2065(31, 24)
        arrayRegFiles(__tmp_2064 + 4.U) := __tmp_2065(39, 32)
        arrayRegFiles(__tmp_2064 + 5.U) := __tmp_2065(47, 40)
        arrayRegFiles(__tmp_2064 + 6.U) := __tmp_2065(55, 48)
        arrayRegFiles(__tmp_2064 + 7.U) := __tmp_2065(63, 56)

        CP := Cat(
          arrayRegFiles(SP + 0.U + 1.U),
          arrayRegFiles(SP + 0.U + 0.U)
        )

      }

      is(112.U) {
        /*
        $10 = (($3: S64) ≡ (0: S64))
        goto .113
        */



        generalRegFiles(10.U) := (generalRegFiles(3.U).asSInt === 0.S(64.W)).asUInt

        CP := 113.U
      }

      is(113.U) {
        /*
        if ($10: B) goto .114 else goto .117
        */


        CP := Mux((generalRegFiles(10.U).asUInt) === 1.U, 114.U, 117.U)
      }

      is(114.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $12 = (($1: anvil.PrinterIndex.U) & ($2: anvil.PrinterIndex.U))
        goto .115
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(12.U) := (generalRegFiles(1.U) & generalRegFiles(2.U))

        CP := 115.U
      }

      is(115.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($12: anvil.PrinterIndex.U) as SP)) = (48: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($12: anvil.PrinterIndex.U)) = (48: U8)
        goto .116
        */


        val __tmp_2066 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(12.U).asUInt)
        val __tmp_2067 = (48.U(8.W)).asUInt
        arrayRegFiles(__tmp_2066 + 0.U) := __tmp_2067(7, 0)

        CP := 116.U
      }

      is(116.U) {
        /*
        **(SP + (2: SP)) = (1: U64) [unsigned, U64, 8]  // $res = (1: U64)
        goto $ret@0
        */


        val __tmp_2068 = Cat(
          arrayRegFiles((SP + 2.U(16.W)) + 1.U),
          arrayRegFiles((SP + 2.U(16.W)) + 0.U)
        )
        val __tmp_2069 = (1.U(64.W)).asUInt
        arrayRegFiles(__tmp_2068 + 0.U) := __tmp_2069(7, 0)
        arrayRegFiles(__tmp_2068 + 1.U) := __tmp_2069(15, 8)
        arrayRegFiles(__tmp_2068 + 2.U) := __tmp_2069(23, 16)
        arrayRegFiles(__tmp_2068 + 3.U) := __tmp_2069(31, 24)
        arrayRegFiles(__tmp_2068 + 4.U) := __tmp_2069(39, 32)
        arrayRegFiles(__tmp_2068 + 5.U) := __tmp_2069(47, 40)
        arrayRegFiles(__tmp_2068 + 6.U) := __tmp_2069(55, 48)
        arrayRegFiles(__tmp_2068 + 7.U) := __tmp_2069(63, 56)

        CP := Cat(
          arrayRegFiles(SP + 0.U + 1.U),
          arrayRegFiles(SP + 0.U + 0.U)
        )

      }

      is(117.U) {
        /*
        decl buff: MS[anvil.PrinterIndex.I20, U8] [@4, 34]
        alloc $new@[168,16].5BB7E063: MS[anvil.PrinterIndex.I20, U8] [@38, 34]
        $11 = (SP + (38: SP))
        *(SP + (38: SP)) = (323602724: U32) [unsigned, U32, 4]  // sha3 type signature of MS[anvil.PrinterIndex.I20, U8]: 0x1349C924
        *(SP + (42: SP)) = (20: Z) [signed, Z, 8]  // size of MS[anvil.PrinterIndex.I20, U8]((0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8))
        goto .118
        */



        generalRegFiles(11.U) := (SP + 38.U(16.W))

        val __tmp_2070 = (SP + 38.U(16.W))
        val __tmp_2071 = (323602724.U(32.W)).asUInt
        arrayRegFiles(__tmp_2070 + 0.U) := __tmp_2071(7, 0)
        arrayRegFiles(__tmp_2070 + 1.U) := __tmp_2071(15, 8)
        arrayRegFiles(__tmp_2070 + 2.U) := __tmp_2071(23, 16)
        arrayRegFiles(__tmp_2070 + 3.U) := __tmp_2071(31, 24)

        val __tmp_2072 = (SP + 42.U(16.W))
        val __tmp_2073 = (20.S(64.W)).asUInt
        arrayRegFiles(__tmp_2072 + 0.U) := __tmp_2073(7, 0)
        arrayRegFiles(__tmp_2072 + 1.U) := __tmp_2073(15, 8)
        arrayRegFiles(__tmp_2072 + 2.U) := __tmp_2073(23, 16)
        arrayRegFiles(__tmp_2072 + 3.U) := __tmp_2073(31, 24)
        arrayRegFiles(__tmp_2072 + 4.U) := __tmp_2073(39, 32)
        arrayRegFiles(__tmp_2072 + 5.U) := __tmp_2073(47, 40)
        arrayRegFiles(__tmp_2072 + 6.U) := __tmp_2073(55, 48)
        arrayRegFiles(__tmp_2072 + 7.U) := __tmp_2073(63, 56)

        CP := 118.U
      }

      is(118.U) {
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
        goto .119
        */


        val __tmp_2074 = ((generalRegFiles(11.U) + 12.U(16.W)) + 0.S(8.W).asUInt)
        val __tmp_2075 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_2074 + 0.U) := __tmp_2075(7, 0)

        val __tmp_2076 = ((generalRegFiles(11.U) + 12.U(16.W)) + 1.S(8.W).asUInt)
        val __tmp_2077 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_2076 + 0.U) := __tmp_2077(7, 0)

        val __tmp_2078 = ((generalRegFiles(11.U) + 12.U(16.W)) + 2.S(8.W).asUInt)
        val __tmp_2079 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_2078 + 0.U) := __tmp_2079(7, 0)

        val __tmp_2080 = ((generalRegFiles(11.U) + 12.U(16.W)) + 3.S(8.W).asUInt)
        val __tmp_2081 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_2080 + 0.U) := __tmp_2081(7, 0)

        val __tmp_2082 = ((generalRegFiles(11.U) + 12.U(16.W)) + 4.S(8.W).asUInt)
        val __tmp_2083 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_2082 + 0.U) := __tmp_2083(7, 0)

        val __tmp_2084 = ((generalRegFiles(11.U) + 12.U(16.W)) + 5.S(8.W).asUInt)
        val __tmp_2085 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_2084 + 0.U) := __tmp_2085(7, 0)

        val __tmp_2086 = ((generalRegFiles(11.U) + 12.U(16.W)) + 6.S(8.W).asUInt)
        val __tmp_2087 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_2086 + 0.U) := __tmp_2087(7, 0)

        val __tmp_2088 = ((generalRegFiles(11.U) + 12.U(16.W)) + 7.S(8.W).asUInt)
        val __tmp_2089 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_2088 + 0.U) := __tmp_2089(7, 0)

        val __tmp_2090 = ((generalRegFiles(11.U) + 12.U(16.W)) + 8.S(8.W).asUInt)
        val __tmp_2091 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_2090 + 0.U) := __tmp_2091(7, 0)

        val __tmp_2092 = ((generalRegFiles(11.U) + 12.U(16.W)) + 9.S(8.W).asUInt)
        val __tmp_2093 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_2092 + 0.U) := __tmp_2093(7, 0)

        val __tmp_2094 = ((generalRegFiles(11.U) + 12.U(16.W)) + 10.S(8.W).asUInt)
        val __tmp_2095 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_2094 + 0.U) := __tmp_2095(7, 0)

        val __tmp_2096 = ((generalRegFiles(11.U) + 12.U(16.W)) + 11.S(8.W).asUInt)
        val __tmp_2097 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_2096 + 0.U) := __tmp_2097(7, 0)

        val __tmp_2098 = ((generalRegFiles(11.U) + 12.U(16.W)) + 12.S(8.W).asUInt)
        val __tmp_2099 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_2098 + 0.U) := __tmp_2099(7, 0)

        val __tmp_2100 = ((generalRegFiles(11.U) + 12.U(16.W)) + 13.S(8.W).asUInt)
        val __tmp_2101 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_2100 + 0.U) := __tmp_2101(7, 0)

        val __tmp_2102 = ((generalRegFiles(11.U) + 12.U(16.W)) + 14.S(8.W).asUInt)
        val __tmp_2103 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_2102 + 0.U) := __tmp_2103(7, 0)

        val __tmp_2104 = ((generalRegFiles(11.U) + 12.U(16.W)) + 15.S(8.W).asUInt)
        val __tmp_2105 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_2104 + 0.U) := __tmp_2105(7, 0)

        val __tmp_2106 = ((generalRegFiles(11.U) + 12.U(16.W)) + 16.S(8.W).asUInt)
        val __tmp_2107 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_2106 + 0.U) := __tmp_2107(7, 0)

        val __tmp_2108 = ((generalRegFiles(11.U) + 12.U(16.W)) + 17.S(8.W).asUInt)
        val __tmp_2109 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_2108 + 0.U) := __tmp_2109(7, 0)

        val __tmp_2110 = ((generalRegFiles(11.U) + 12.U(16.W)) + 18.S(8.W).asUInt)
        val __tmp_2111 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_2110 + 0.U) := __tmp_2111(7, 0)

        val __tmp_2112 = ((generalRegFiles(11.U) + 12.U(16.W)) + 19.S(8.W).asUInt)
        val __tmp_2113 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_2112 + 0.U) := __tmp_2113(7, 0)

        CP := 119.U
      }

      is(119.U) {
        /*
        (SP + (4: SP)) [MS[anvil.PrinterIndex.I20, U8], 34]  <-  ($11: MS[anvil.PrinterIndex.I20, U8]) [MS[anvil.PrinterIndex.I20, U8], ((*(($11: MS[anvil.PrinterIndex.I20, U8]) + (4: SP)) as SP) + (12: SP))]  // buff = ($11: MS[anvil.PrinterIndex.I20, U8])
        goto .120
        */


        val __tmp_2114 = (SP + 4.U(16.W))
        val __tmp_2115 = generalRegFiles(11.U)
        val __tmp_2116 = (Cat(
           arrayRegFiles((generalRegFiles(11.U) + 4.U(16.W)) + 7.U),
           arrayRegFiles((generalRegFiles(11.U) + 4.U(16.W)) + 6.U),
           arrayRegFiles((generalRegFiles(11.U) + 4.U(16.W)) + 5.U),
           arrayRegFiles((generalRegFiles(11.U) + 4.U(16.W)) + 4.U),
           arrayRegFiles((generalRegFiles(11.U) + 4.U(16.W)) + 3.U),
           arrayRegFiles((generalRegFiles(11.U) + 4.U(16.W)) + 2.U),
           arrayRegFiles((generalRegFiles(11.U) + 4.U(16.W)) + 1.U),
           arrayRegFiles((generalRegFiles(11.U) + 4.U(16.W)) + 0.U)
         ).asSInt.asUInt + 12.U(16.W))

        when(Idx < __tmp_2116) {
          arrayRegFiles(__tmp_2114 + Idx + 0.U) := arrayRegFiles(__tmp_2115 + Idx + 0.U)
          arrayRegFiles(__tmp_2114 + Idx + 1.U) := arrayRegFiles(__tmp_2115 + Idx + 1.U)
          arrayRegFiles(__tmp_2114 + Idx + 2.U) := arrayRegFiles(__tmp_2115 + Idx + 2.U)
          arrayRegFiles(__tmp_2114 + Idx + 3.U) := arrayRegFiles(__tmp_2115 + Idx + 3.U)
          arrayRegFiles(__tmp_2114 + Idx + 4.U) := arrayRegFiles(__tmp_2115 + Idx + 4.U)
          arrayRegFiles(__tmp_2114 + Idx + 5.U) := arrayRegFiles(__tmp_2115 + Idx + 5.U)
          arrayRegFiles(__tmp_2114 + Idx + 6.U) := arrayRegFiles(__tmp_2115 + Idx + 6.U)
          arrayRegFiles(__tmp_2114 + Idx + 7.U) := arrayRegFiles(__tmp_2115 + Idx + 7.U)
          Idx := Idx + 8.U
          LeftByteRounds := __tmp_2116 - Idx
        } .elsewhen(IdxLeftByteRounds < LeftByteRounds) {
          val __tmp_2117 = Idx - 8.U
          arrayRegFiles(__tmp_2114 + __tmp_2117 + IdxLeftByteRounds) := arrayRegFiles(__tmp_2115 + __tmp_2117 + IdxLeftByteRounds)
          IdxLeftByteRounds := IdxLeftByteRounds + 1.U
        } .otherwise {
          Idx := 0.U
          IdxLeftByteRounds := 0.U
          LeftByteRounds := 0.U
          CP := 120.U
        }


      }

      is(120.U) {
        /*
        unalloc $new@[168,16].5BB7E063: MS[anvil.PrinterIndex.I20, U8] [@38, 34]
        goto .121
        */


        CP := 121.U
      }

      is(121.U) {
        /*
        decl i: anvil.PrinterIndex.I20 @$4
        $4 = (0: anvil.PrinterIndex.I20)
        goto .122
        */



        generalRegFiles(4.U) := (0.S(8.W)).asUInt

        CP := 122.U
      }

      is(122.U) {
        /*
        decl neg: B @$5
        $10 = (($3: S64) < (0: S64))
        goto .123
        */



        generalRegFiles(10.U) := (generalRegFiles(3.U).asSInt < 0.S(64.W)).asUInt

        CP := 123.U
      }

      is(123.U) {
        /*
        $5 = ($10: B)
        goto .124
        */



        generalRegFiles(5.U) := generalRegFiles(10.U)

        CP := 124.U
      }

      is(124.U) {
        /*
        decl m: S64 @$6
        goto .125
        */


        CP := 125.U
      }

      is(125.U) {
        /*
        if ($5: B) goto .126 else goto .128
        */


        CP := Mux((generalRegFiles(5.U).asUInt) === 1.U, 126.U, 128.U)
      }

      is(126.U) {
        /*
        $12 = -(($3: S64))
        goto .127
        */



        generalRegFiles(12.U) := (-generalRegFiles(3.U).asSInt).asUInt

        CP := 127.U
      }

      is(127.U) {
        /*
        $10 = ($12: S64)
        goto .130
        */



        generalRegFiles(10.U) := (generalRegFiles(12.U).asSInt).asUInt

        CP := 130.U
      }

      is(128.U) {
        /*
        $14 = ($3: S64)
        goto .129
        */



        generalRegFiles(14.U) := (generalRegFiles(3.U).asSInt).asUInt

        CP := 129.U
      }

      is(129.U) {
        /*
        $10 = ($14: S64)
        goto .130
        */



        generalRegFiles(10.U) := (generalRegFiles(14.U).asSInt).asUInt

        CP := 130.U
      }

      is(130.U) {
        /*
        $6 = ($10: S64)
        goto .131
        */



        generalRegFiles(6.U) := (generalRegFiles(10.U).asSInt).asUInt

        CP := 131.U
      }

      is(131.U) {
        /*
        $11 = ($6: S64)
        goto .132
        */



        generalRegFiles(11.U) := (generalRegFiles(6.U).asSInt).asUInt

        CP := 132.U
      }

      is(132.U) {
        /*
        $10 = (($11: S64) > (0: S64))
        goto .133
        */



        generalRegFiles(10.U) := (generalRegFiles(11.U).asSInt > 0.S(64.W)).asUInt

        CP := 133.U
      }

      is(133.U) {
        /*
        if ($10: B) goto .134 else goto .163
        */


        CP := Mux((generalRegFiles(10.U).asUInt) === 1.U, 134.U, 163.U)
      }

      is(134.U) {
        /*
        $11 = ($6: S64)
        goto .135
        */



        generalRegFiles(11.U) := (generalRegFiles(6.U).asSInt).asUInt

        CP := 135.U
      }

      is(135.U) {
        /*
        $10 = (($11: S64) % (10: S64))
        goto .136
        */



        generalRegFiles(10.U) := ((generalRegFiles(11.U).asSInt % 10.S(64.W))).asUInt

        CP := 136.U
      }

      is(136.U) {
        /*
        switch (($10: S64))
          (0: S64): goto 137
          (1: S64): goto 139
          (2: S64): goto 141
          (3: S64): goto 143
          (4: S64): goto 145
          (5: S64): goto 147
          (6: S64): goto 149
          (7: S64): goto 151
          (8: S64): goto 153
          (9: S64): goto 155

        */


        val __tmp_2118 = generalRegFiles(10.U).asSInt

        switch(__tmp_2118) {

          is(0.S(64.W)) {
            CP := 137.U
          }


          is(1.S(64.W)) {
            CP := 139.U
          }


          is(2.S(64.W)) {
            CP := 141.U
          }


          is(3.S(64.W)) {
            CP := 143.U
          }


          is(4.S(64.W)) {
            CP := 145.U
          }


          is(5.S(64.W)) {
            CP := 147.U
          }


          is(6.S(64.W)) {
            CP := 149.U
          }


          is(7.S(64.W)) {
            CP := 151.U
          }


          is(8.S(64.W)) {
            CP := 153.U
          }


          is(9.S(64.W)) {
            CP := 155.U
          }

        }

      }

      is(137.U) {
        /*
        $11 = (SP + (4: SP))
        $10 = ($4: anvil.PrinterIndex.I20)
        goto .138
        */



        generalRegFiles(11.U) := (SP + 4.U(16.W))


        generalRegFiles(10.U) := (generalRegFiles(4.U).asSInt).asUInt

        CP := 138.U
      }

      is(138.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.I20, U8]) + (12: SP)) + (($10: anvil.PrinterIndex.I20) as SP)) = (48: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.I20, U8])(($10: anvil.PrinterIndex.I20)) = (48: U8)
        goto .157
        */


        val __tmp_2119 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(10.U).asSInt.asUInt)
        val __tmp_2120 = (48.U(8.W)).asUInt
        arrayRegFiles(__tmp_2119 + 0.U) := __tmp_2120(7, 0)

        CP := 157.U
      }

      is(139.U) {
        /*
        $11 = (SP + (4: SP))
        $10 = ($4: anvil.PrinterIndex.I20)
        goto .140
        */



        generalRegFiles(11.U) := (SP + 4.U(16.W))


        generalRegFiles(10.U) := (generalRegFiles(4.U).asSInt).asUInt

        CP := 140.U
      }

      is(140.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.I20, U8]) + (12: SP)) + (($10: anvil.PrinterIndex.I20) as SP)) = (49: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.I20, U8])(($10: anvil.PrinterIndex.I20)) = (49: U8)
        goto .157
        */


        val __tmp_2121 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(10.U).asSInt.asUInt)
        val __tmp_2122 = (49.U(8.W)).asUInt
        arrayRegFiles(__tmp_2121 + 0.U) := __tmp_2122(7, 0)

        CP := 157.U
      }

      is(141.U) {
        /*
        $11 = (SP + (4: SP))
        $10 = ($4: anvil.PrinterIndex.I20)
        goto .142
        */



        generalRegFiles(11.U) := (SP + 4.U(16.W))


        generalRegFiles(10.U) := (generalRegFiles(4.U).asSInt).asUInt

        CP := 142.U
      }

      is(142.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.I20, U8]) + (12: SP)) + (($10: anvil.PrinterIndex.I20) as SP)) = (50: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.I20, U8])(($10: anvil.PrinterIndex.I20)) = (50: U8)
        goto .157
        */


        val __tmp_2123 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(10.U).asSInt.asUInt)
        val __tmp_2124 = (50.U(8.W)).asUInt
        arrayRegFiles(__tmp_2123 + 0.U) := __tmp_2124(7, 0)

        CP := 157.U
      }

      is(143.U) {
        /*
        $11 = (SP + (4: SP))
        $10 = ($4: anvil.PrinterIndex.I20)
        goto .144
        */



        generalRegFiles(11.U) := (SP + 4.U(16.W))


        generalRegFiles(10.U) := (generalRegFiles(4.U).asSInt).asUInt

        CP := 144.U
      }

      is(144.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.I20, U8]) + (12: SP)) + (($10: anvil.PrinterIndex.I20) as SP)) = (51: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.I20, U8])(($10: anvil.PrinterIndex.I20)) = (51: U8)
        goto .157
        */


        val __tmp_2125 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(10.U).asSInt.asUInt)
        val __tmp_2126 = (51.U(8.W)).asUInt
        arrayRegFiles(__tmp_2125 + 0.U) := __tmp_2126(7, 0)

        CP := 157.U
      }

      is(145.U) {
        /*
        $11 = (SP + (4: SP))
        $10 = ($4: anvil.PrinterIndex.I20)
        goto .146
        */



        generalRegFiles(11.U) := (SP + 4.U(16.W))


        generalRegFiles(10.U) := (generalRegFiles(4.U).asSInt).asUInt

        CP := 146.U
      }

      is(146.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.I20, U8]) + (12: SP)) + (($10: anvil.PrinterIndex.I20) as SP)) = (52: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.I20, U8])(($10: anvil.PrinterIndex.I20)) = (52: U8)
        goto .157
        */


        val __tmp_2127 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(10.U).asSInt.asUInt)
        val __tmp_2128 = (52.U(8.W)).asUInt
        arrayRegFiles(__tmp_2127 + 0.U) := __tmp_2128(7, 0)

        CP := 157.U
      }

      is(147.U) {
        /*
        $11 = (SP + (4: SP))
        $10 = ($4: anvil.PrinterIndex.I20)
        goto .148
        */



        generalRegFiles(11.U) := (SP + 4.U(16.W))


        generalRegFiles(10.U) := (generalRegFiles(4.U).asSInt).asUInt

        CP := 148.U
      }

      is(148.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.I20, U8]) + (12: SP)) + (($10: anvil.PrinterIndex.I20) as SP)) = (53: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.I20, U8])(($10: anvil.PrinterIndex.I20)) = (53: U8)
        goto .157
        */


        val __tmp_2129 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(10.U).asSInt.asUInt)
        val __tmp_2130 = (53.U(8.W)).asUInt
        arrayRegFiles(__tmp_2129 + 0.U) := __tmp_2130(7, 0)

        CP := 157.U
      }

      is(149.U) {
        /*
        $11 = (SP + (4: SP))
        $10 = ($4: anvil.PrinterIndex.I20)
        goto .150
        */



        generalRegFiles(11.U) := (SP + 4.U(16.W))


        generalRegFiles(10.U) := (generalRegFiles(4.U).asSInt).asUInt

        CP := 150.U
      }

      is(150.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.I20, U8]) + (12: SP)) + (($10: anvil.PrinterIndex.I20) as SP)) = (54: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.I20, U8])(($10: anvil.PrinterIndex.I20)) = (54: U8)
        goto .157
        */


        val __tmp_2131 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(10.U).asSInt.asUInt)
        val __tmp_2132 = (54.U(8.W)).asUInt
        arrayRegFiles(__tmp_2131 + 0.U) := __tmp_2132(7, 0)

        CP := 157.U
      }

      is(151.U) {
        /*
        $11 = (SP + (4: SP))
        $10 = ($4: anvil.PrinterIndex.I20)
        goto .152
        */



        generalRegFiles(11.U) := (SP + 4.U(16.W))


        generalRegFiles(10.U) := (generalRegFiles(4.U).asSInt).asUInt

        CP := 152.U
      }

      is(152.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.I20, U8]) + (12: SP)) + (($10: anvil.PrinterIndex.I20) as SP)) = (55: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.I20, U8])(($10: anvil.PrinterIndex.I20)) = (55: U8)
        goto .157
        */


        val __tmp_2133 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(10.U).asSInt.asUInt)
        val __tmp_2134 = (55.U(8.W)).asUInt
        arrayRegFiles(__tmp_2133 + 0.U) := __tmp_2134(7, 0)

        CP := 157.U
      }

      is(153.U) {
        /*
        $11 = (SP + (4: SP))
        $10 = ($4: anvil.PrinterIndex.I20)
        goto .154
        */



        generalRegFiles(11.U) := (SP + 4.U(16.W))


        generalRegFiles(10.U) := (generalRegFiles(4.U).asSInt).asUInt

        CP := 154.U
      }

      is(154.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.I20, U8]) + (12: SP)) + (($10: anvil.PrinterIndex.I20) as SP)) = (56: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.I20, U8])(($10: anvil.PrinterIndex.I20)) = (56: U8)
        goto .157
        */


        val __tmp_2135 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(10.U).asSInt.asUInt)
        val __tmp_2136 = (56.U(8.W)).asUInt
        arrayRegFiles(__tmp_2135 + 0.U) := __tmp_2136(7, 0)

        CP := 157.U
      }

      is(155.U) {
        /*
        $11 = (SP + (4: SP))
        $10 = ($4: anvil.PrinterIndex.I20)
        goto .156
        */



        generalRegFiles(11.U) := (SP + 4.U(16.W))


        generalRegFiles(10.U) := (generalRegFiles(4.U).asSInt).asUInt

        CP := 156.U
      }

      is(156.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.I20, U8]) + (12: SP)) + (($10: anvil.PrinterIndex.I20) as SP)) = (57: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.I20, U8])(($10: anvil.PrinterIndex.I20)) = (57: U8)
        goto .157
        */


        val __tmp_2137 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(10.U).asSInt.asUInt)
        val __tmp_2138 = (57.U(8.W)).asUInt
        arrayRegFiles(__tmp_2137 + 0.U) := __tmp_2138(7, 0)

        CP := 157.U
      }

      is(157.U) {
        /*
        $11 = ($6: S64)
        goto .158
        */



        generalRegFiles(11.U) := (generalRegFiles(6.U).asSInt).asUInt

        CP := 158.U
      }

      is(158.U) {
        /*
        $10 = (($11: S64) / (10: S64))
        goto .159
        */



        generalRegFiles(10.U) := ((generalRegFiles(11.U).asSInt / 10.S(64.W))).asUInt

        CP := 159.U
      }

      is(159.U) {
        /*
        $6 = ($10: S64)
        goto .160
        */



        generalRegFiles(6.U) := (generalRegFiles(10.U).asSInt).asUInt

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
        goto .131
        */



        generalRegFiles(4.U) := (generalRegFiles(10.U).asSInt).asUInt

        CP := 131.U
      }

      is(163.U) {
        /*
        $11 = ($5: B)
        undecl neg: B @$5
        goto .164
        */



        generalRegFiles(11.U) := generalRegFiles(5.U)

        CP := 164.U
      }

      is(164.U) {
        /*
        if ($11: B) goto .165 else goto .170
        */


        CP := Mux((generalRegFiles(11.U).asUInt) === 1.U, 165.U, 170.U)
      }

      is(165.U) {
        /*
        $11 = (SP + (4: SP))
        $10 = ($4: anvil.PrinterIndex.I20)
        goto .166
        */



        generalRegFiles(11.U) := (SP + 4.U(16.W))


        generalRegFiles(10.U) := (generalRegFiles(4.U).asSInt).asUInt

        CP := 166.U
      }

      is(166.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.I20, U8]) + (12: SP)) + (($10: anvil.PrinterIndex.I20) as SP)) = (45: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.I20, U8])(($10: anvil.PrinterIndex.I20)) = (45: U8)
        goto .167
        */


        val __tmp_2139 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(10.U).asSInt.asUInt)
        val __tmp_2140 = (45.U(8.W)).asUInt
        arrayRegFiles(__tmp_2139 + 0.U) := __tmp_2140(7, 0)

        CP := 167.U
      }

      is(167.U) {
        /*
        $11 = ($4: anvil.PrinterIndex.I20)
        goto .168
        */



        generalRegFiles(11.U) := (generalRegFiles(4.U).asSInt).asUInt

        CP := 168.U
      }

      is(168.U) {
        /*
        $10 = (($11: anvil.PrinterIndex.I20) + (1: anvil.PrinterIndex.I20))
        goto .169
        */



        generalRegFiles(10.U) := ((generalRegFiles(11.U).asSInt + 1.S(8.W))).asUInt

        CP := 169.U
      }

      is(169.U) {
        /*
        $4 = ($10: anvil.PrinterIndex.I20)
        goto .170
        */



        generalRegFiles(4.U) := (generalRegFiles(10.U).asSInt).asUInt

        CP := 170.U
      }

      is(170.U) {
        /*
        decl j: anvil.PrinterIndex.I20 @$7
        $11 = ($4: anvil.PrinterIndex.I20)
        undecl i: anvil.PrinterIndex.I20 @$4
        goto .171
        */



        generalRegFiles(11.U) := (generalRegFiles(4.U).asSInt).asUInt

        CP := 171.U
      }

      is(171.U) {
        /*
        $10 = (($11: anvil.PrinterIndex.I20) - (1: anvil.PrinterIndex.I20))
        goto .172
        */



        generalRegFiles(10.U) := ((generalRegFiles(11.U).asSInt - 1.S(8.W))).asUInt

        CP := 172.U
      }

      is(172.U) {
        /*
        $7 = ($10: anvil.PrinterIndex.I20)
        goto .173
        */



        generalRegFiles(7.U) := (generalRegFiles(10.U).asSInt).asUInt

        CP := 173.U
      }

      is(173.U) {
        /*
        decl idx: anvil.PrinterIndex.U @$8
        $11 = ($1: anvil.PrinterIndex.U)
        goto .174
        */



        generalRegFiles(11.U) := generalRegFiles(1.U)

        CP := 174.U
      }

      is(174.U) {
        /*
        $8 = ($11: anvil.PrinterIndex.U)
        goto .175
        */



        generalRegFiles(8.U) := generalRegFiles(11.U)

        CP := 175.U
      }

      is(175.U) {
        /*
        decl r: U64 @$9
        $9 = (0: U64)
        goto .176
        */



        generalRegFiles(9.U) := 0.U(64.W)

        CP := 176.U
      }

      is(176.U) {
        /*
        $11 = ($7: anvil.PrinterIndex.I20)
        goto .177
        */



        generalRegFiles(11.U) := (generalRegFiles(7.U).asSInt).asUInt

        CP := 177.U
      }

      is(177.U) {
        /*
        $10 = (($11: anvil.PrinterIndex.I20) >= (0: anvil.PrinterIndex.I20))
        goto .178
        */



        generalRegFiles(10.U) := (generalRegFiles(11.U).asSInt >= 0.S(8.W)).asUInt

        CP := 178.U
      }

      is(178.U) {
        /*
        if ($10: B) goto .179 else goto .193
        */


        CP := Mux((generalRegFiles(10.U).asUInt) === 1.U, 179.U, 193.U)
      }

      is(179.U) {
        /*
        $11 = ($0: MS[anvil.PrinterIndex.U, U8])
        $10 = ($8: anvil.PrinterIndex.U)
        $13 = ($2: anvil.PrinterIndex.U)
        goto .180
        */



        generalRegFiles(11.U) := generalRegFiles(0.U)


        generalRegFiles(10.U) := generalRegFiles(8.U)


        generalRegFiles(13.U) := generalRegFiles(2.U)

        CP := 180.U
      }

      is(180.U) {
        /*
        $12 = (($10: anvil.PrinterIndex.U) & ($13: anvil.PrinterIndex.U))
        goto .181
        */



        generalRegFiles(12.U) := (generalRegFiles(10.U) & generalRegFiles(13.U))

        CP := 181.U
      }

      is(181.U) {
        /*
        $14 = (SP + (4: SP))
        $15 = ($7: anvil.PrinterIndex.I20)
        goto .182
        */



        generalRegFiles(14.U) := (SP + 4.U(16.W))


        generalRegFiles(15.U) := (generalRegFiles(7.U).asSInt).asUInt

        CP := 182.U
      }

      is(182.U) {
        /*
        $16 = *((($14: MS[anvil.PrinterIndex.I20, U8]) + (12: SP)) + (($15: anvil.PrinterIndex.I20) as SP)) [unsigned, U8, 1]  // $16 = ($14: MS[anvil.PrinterIndex.I20, U8])(($15: anvil.PrinterIndex.I20))
        goto .183
        */


        val __tmp_2141 = (((generalRegFiles(14.U) + 12.U(16.W)) + generalRegFiles(15.U).asSInt.asUInt)).asUInt
        generalRegFiles(16.U) := Cat(
          arrayRegFiles(__tmp_2141 + 0.U)
        ).asUInt

        CP := 183.U
      }

      is(183.U) {
        /*
        *((($11: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($12: anvil.PrinterIndex.U) as SP)) = ($16: U8) [unsigned, U8, 1]  // ($11: MS[anvil.PrinterIndex.U, U8])(($12: anvil.PrinterIndex.U)) = ($16: U8)
        goto .184
        */


        val __tmp_2142 = ((generalRegFiles(11.U) + 12.U(16.W)) + generalRegFiles(12.U).asUInt)
        val __tmp_2143 = (generalRegFiles(16.U)).asUInt
        arrayRegFiles(__tmp_2142 + 0.U) := __tmp_2143(7, 0)

        CP := 184.U
      }

      is(184.U) {
        /*
        $11 = ($7: anvil.PrinterIndex.I20)
        goto .185
        */



        generalRegFiles(11.U) := (generalRegFiles(7.U).asSInt).asUInt

        CP := 185.U
      }

      is(185.U) {
        /*
        $10 = (($11: anvil.PrinterIndex.I20) - (1: anvil.PrinterIndex.I20))
        goto .186
        */



        generalRegFiles(10.U) := ((generalRegFiles(11.U).asSInt - 1.S(8.W))).asUInt

        CP := 186.U
      }

      is(186.U) {
        /*
        $7 = ($10: anvil.PrinterIndex.I20)
        goto .187
        */



        generalRegFiles(7.U) := (generalRegFiles(10.U).asSInt).asUInt

        CP := 187.U
      }

      is(187.U) {
        /*
        $11 = ($8: anvil.PrinterIndex.U)
        goto .188
        */



        generalRegFiles(11.U) := generalRegFiles(8.U)

        CP := 188.U
      }

      is(188.U) {
        /*
        $10 = (($11: anvil.PrinterIndex.U) + (1: anvil.PrinterIndex.U))
        goto .189
        */



        generalRegFiles(10.U) := (generalRegFiles(11.U) + 1.U(64.W))

        CP := 189.U
      }

      is(189.U) {
        /*
        $8 = ($10: anvil.PrinterIndex.U)
        goto .190
        */



        generalRegFiles(8.U) := generalRegFiles(10.U)

        CP := 190.U
      }

      is(190.U) {
        /*
        $11 = ($9: U64)
        goto .191
        */



        generalRegFiles(11.U) := generalRegFiles(9.U)

        CP := 191.U
      }

      is(191.U) {
        /*
        $10 = (($11: U64) + (1: U64))
        goto .192
        */



        generalRegFiles(10.U) := (generalRegFiles(11.U) + 1.U(64.W))

        CP := 192.U
      }

      is(192.U) {
        /*
        $9 = ($10: U64)
        goto .176
        */



        generalRegFiles(9.U) := generalRegFiles(10.U)

        CP := 176.U
      }

      is(193.U) {
        /*
        $11 = ($9: U64)
        undecl r: U64 @$9
        goto .194
        */



        generalRegFiles(11.U) := generalRegFiles(9.U)

        CP := 194.U
      }

      is(194.U) {
        /*
        **(SP + (2: SP)) = ($11: U64) [unsigned, U64, 8]  // $res = ($11: U64)
        goto $ret@0
        */


        val __tmp_2144 = Cat(
          arrayRegFiles((SP + 2.U(16.W)) + 1.U),
          arrayRegFiles((SP + 2.U(16.W)) + 0.U)
        )
        val __tmp_2145 = (generalRegFiles(11.U)).asUInt
        arrayRegFiles(__tmp_2144 + 0.U) := __tmp_2145(7, 0)
        arrayRegFiles(__tmp_2144 + 1.U) := __tmp_2145(15, 8)
        arrayRegFiles(__tmp_2144 + 2.U) := __tmp_2145(23, 16)
        arrayRegFiles(__tmp_2144 + 3.U) := __tmp_2145(31, 24)
        arrayRegFiles(__tmp_2144 + 4.U) := __tmp_2145(39, 32)
        arrayRegFiles(__tmp_2144 + 5.U) := __tmp_2145(47, 40)
        arrayRegFiles(__tmp_2144 + 6.U) := __tmp_2145(55, 48)
        arrayRegFiles(__tmp_2144 + 7.U) := __tmp_2145(63, 56)

        CP := Cat(
          arrayRegFiles(SP + 0.U + 1.U),
          arrayRegFiles(SP + 0.U + 0.U)
        )

      }

      is(195.U) {
        /*
        if (($1: U64) ≢ (0: U64)) goto .201 else goto .196
        */


        CP := Mux(((generalRegFiles(1.U) =/= 0.U(64.W)).asUInt.asUInt) === 1.U, 201.U, 196.U)
      }

      is(196.U) {
        /*
        *(((8: SP) + (12: SP)) + ((DP & (31: DP)) as SP)) = (68: U8) [unsigned, U8, 1]  // $display((DP & (31: DP))) = (68: U8)
        *(((8: SP) + (12: SP)) + (((DP + (1: DP)) & (31: DP)) as SP)) = (105: U8) [unsigned, U8, 1]  // $display(((DP + (1: DP)) & (31: DP))) = (105: U8)
        *(((8: SP) + (12: SP)) + (((DP + (2: DP)) & (31: DP)) as SP)) = (118: U8) [unsigned, U8, 1]  // $display(((DP + (2: DP)) & (31: DP))) = (118: U8)
        *(((8: SP) + (12: SP)) + (((DP + (3: DP)) & (31: DP)) as SP)) = (105: U8) [unsigned, U8, 1]  // $display(((DP + (3: DP)) & (31: DP))) = (105: U8)
        *(((8: SP) + (12: SP)) + (((DP + (4: DP)) & (31: DP)) as SP)) = (115: U8) [unsigned, U8, 1]  // $display(((DP + (4: DP)) & (31: DP))) = (115: U8)
        *(((8: SP) + (12: SP)) + (((DP + (5: DP)) & (31: DP)) as SP)) = (105: U8) [unsigned, U8, 1]  // $display(((DP + (5: DP)) & (31: DP))) = (105: U8)
        *(((8: SP) + (12: SP)) + (((DP + (6: DP)) & (31: DP)) as SP)) = (111: U8) [unsigned, U8, 1]  // $display(((DP + (6: DP)) & (31: DP))) = (111: U8)
        *(((8: SP) + (12: SP)) + (((DP + (7: DP)) & (31: DP)) as SP)) = (110: U8) [unsigned, U8, 1]  // $display(((DP + (7: DP)) & (31: DP))) = (110: U8)
        *(((8: SP) + (12: SP)) + (((DP + (8: DP)) & (31: DP)) as SP)) = (32: U8) [unsigned, U8, 1]  // $display(((DP + (8: DP)) & (31: DP))) = (32: U8)
        *(((8: SP) + (12: SP)) + (((DP + (9: DP)) & (31: DP)) as SP)) = (98: U8) [unsigned, U8, 1]  // $display(((DP + (9: DP)) & (31: DP))) = (98: U8)
        *(((8: SP) + (12: SP)) + (((DP + (10: DP)) & (31: DP)) as SP)) = (121: U8) [unsigned, U8, 1]  // $display(((DP + (10: DP)) & (31: DP))) = (121: U8)
        *(((8: SP) + (12: SP)) + (((DP + (11: DP)) & (31: DP)) as SP)) = (32: U8) [unsigned, U8, 1]  // $display(((DP + (11: DP)) & (31: DP))) = (32: U8)
        *(((8: SP) + (12: SP)) + (((DP + (12: DP)) & (31: DP)) as SP)) = (122: U8) [unsigned, U8, 1]  // $display(((DP + (12: DP)) & (31: DP))) = (122: U8)
        *(((8: SP) + (12: SP)) + (((DP + (13: DP)) & (31: DP)) as SP)) = (101: U8) [unsigned, U8, 1]  // $display(((DP + (13: DP)) & (31: DP))) = (101: U8)
        *(((8: SP) + (12: SP)) + (((DP + (14: DP)) & (31: DP)) as SP)) = (114: U8) [unsigned, U8, 1]  // $display(((DP + (14: DP)) & (31: DP))) = (114: U8)
        *(((8: SP) + (12: SP)) + (((DP + (15: DP)) & (31: DP)) as SP)) = (111: U8) [unsigned, U8, 1]  // $display(((DP + (15: DP)) & (31: DP))) = (111: U8)
        goto .197
        */


        val __tmp_2146 = ((8.U(16.W) + 12.U(16.W)) + (DP & 31.U(64.W)).asUInt)
        val __tmp_2147 = (68.U(8.W)).asUInt
        arrayRegFiles(__tmp_2146 + 0.U) := __tmp_2147(7, 0)

        val __tmp_2148 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 1.U(64.W)) & 31.U(64.W)).asUInt)
        val __tmp_2149 = (105.U(8.W)).asUInt
        arrayRegFiles(__tmp_2148 + 0.U) := __tmp_2149(7, 0)

        val __tmp_2150 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 2.U(64.W)) & 31.U(64.W)).asUInt)
        val __tmp_2151 = (118.U(8.W)).asUInt
        arrayRegFiles(__tmp_2150 + 0.U) := __tmp_2151(7, 0)

        val __tmp_2152 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 3.U(64.W)) & 31.U(64.W)).asUInt)
        val __tmp_2153 = (105.U(8.W)).asUInt
        arrayRegFiles(__tmp_2152 + 0.U) := __tmp_2153(7, 0)

        val __tmp_2154 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 4.U(64.W)) & 31.U(64.W)).asUInt)
        val __tmp_2155 = (115.U(8.W)).asUInt
        arrayRegFiles(__tmp_2154 + 0.U) := __tmp_2155(7, 0)

        val __tmp_2156 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 5.U(64.W)) & 31.U(64.W)).asUInt)
        val __tmp_2157 = (105.U(8.W)).asUInt
        arrayRegFiles(__tmp_2156 + 0.U) := __tmp_2157(7, 0)

        val __tmp_2158 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 6.U(64.W)) & 31.U(64.W)).asUInt)
        val __tmp_2159 = (111.U(8.W)).asUInt
        arrayRegFiles(__tmp_2158 + 0.U) := __tmp_2159(7, 0)

        val __tmp_2160 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 7.U(64.W)) & 31.U(64.W)).asUInt)
        val __tmp_2161 = (110.U(8.W)).asUInt
        arrayRegFiles(__tmp_2160 + 0.U) := __tmp_2161(7, 0)

        val __tmp_2162 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 8.U(64.W)) & 31.U(64.W)).asUInt)
        val __tmp_2163 = (32.U(8.W)).asUInt
        arrayRegFiles(__tmp_2162 + 0.U) := __tmp_2163(7, 0)

        val __tmp_2164 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 9.U(64.W)) & 31.U(64.W)).asUInt)
        val __tmp_2165 = (98.U(8.W)).asUInt
        arrayRegFiles(__tmp_2164 + 0.U) := __tmp_2165(7, 0)

        val __tmp_2166 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 10.U(64.W)) & 31.U(64.W)).asUInt)
        val __tmp_2167 = (121.U(8.W)).asUInt
        arrayRegFiles(__tmp_2166 + 0.U) := __tmp_2167(7, 0)

        val __tmp_2168 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 11.U(64.W)) & 31.U(64.W)).asUInt)
        val __tmp_2169 = (32.U(8.W)).asUInt
        arrayRegFiles(__tmp_2168 + 0.U) := __tmp_2169(7, 0)

        val __tmp_2170 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 12.U(64.W)) & 31.U(64.W)).asUInt)
        val __tmp_2171 = (122.U(8.W)).asUInt
        arrayRegFiles(__tmp_2170 + 0.U) := __tmp_2171(7, 0)

        val __tmp_2172 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 13.U(64.W)) & 31.U(64.W)).asUInt)
        val __tmp_2173 = (101.U(8.W)).asUInt
        arrayRegFiles(__tmp_2172 + 0.U) := __tmp_2173(7, 0)

        val __tmp_2174 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 14.U(64.W)) & 31.U(64.W)).asUInt)
        val __tmp_2175 = (114.U(8.W)).asUInt
        arrayRegFiles(__tmp_2174 + 0.U) := __tmp_2175(7, 0)

        val __tmp_2176 = ((8.U(16.W) + 12.U(16.W)) + ((DP + 15.U(64.W)) & 31.U(64.W)).asUInt)
        val __tmp_2177 = (111.U(8.W)).asUInt
        arrayRegFiles(__tmp_2176 + 0.U) := __tmp_2177(7, 0)

        CP := 197.U
      }

      is(197.U) {
        /*
        DP = DP + 16
        goto .198
        */


        DP := DP + 16.U

        CP := 198.U
      }

      is(198.U) {
        /*
        *(((8: SP) + (12: SP)) + ((DP & (31: DP)) as SP)) = (10: U8) [unsigned, U8, 1]  // $display((DP & (31: DP))) = (10: U8)
        goto .199
        */


        val __tmp_2178 = ((8.U(16.W) + 12.U(16.W)) + (DP & 31.U(64.W)).asUInt)
        val __tmp_2179 = (10.U(8.W)).asUInt
        arrayRegFiles(__tmp_2178 + 0.U) := __tmp_2179(7, 0)

        CP := 199.U
      }

      is(199.U) {
        /*
        DP = DP + 1
        goto .1
        */


        DP := DP + 1.U

        CP := 1.U
      }

      is(201.U) {
        /*
        $2 = (($0: U64) % ($1: U64))
        goto .202
        */



        generalRegFiles(2.U) := (generalRegFiles(0.U) % generalRegFiles(1.U))

        CP := 202.U
      }

      is(202.U) {
        /*
        **(SP + (2: SP)) = ($2: U64) [unsigned, U64, 8]  // $res = ($2: U64)
        goto $ret@0
        */


        val __tmp_2180 = Cat(
          arrayRegFiles((SP + 2.U(16.W)) + 1.U),
          arrayRegFiles((SP + 2.U(16.W)) + 0.U)
        )
        val __tmp_2181 = (generalRegFiles(2.U)).asUInt
        arrayRegFiles(__tmp_2180 + 0.U) := __tmp_2181(7, 0)
        arrayRegFiles(__tmp_2180 + 1.U) := __tmp_2181(15, 8)
        arrayRegFiles(__tmp_2180 + 2.U) := __tmp_2181(23, 16)
        arrayRegFiles(__tmp_2180 + 3.U) := __tmp_2181(31, 24)
        arrayRegFiles(__tmp_2180 + 4.U) := __tmp_2181(39, 32)
        arrayRegFiles(__tmp_2180 + 5.U) := __tmp_2181(47, 40)
        arrayRegFiles(__tmp_2180 + 6.U) := __tmp_2181(55, 48)
        arrayRegFiles(__tmp_2180 + 7.U) := __tmp_2181(63, 56)

        CP := Cat(
          arrayRegFiles(SP + 0.U + 1.U),
          arrayRegFiles(SP + 0.U + 0.U)
        )

      }

      is(203.U) {
        /*
        decl buff: MS[anvil.PrinterIndex.I16, U8] [@4, 30]
        alloc $new@[245,16].6203A7B3: MS[anvil.PrinterIndex.I16, U8] [@34, 30]
        $10 = (SP + (34: SP))
        *(SP + (34: SP)) = (1541243932: U32) [unsigned, U32, 4]  // sha3 type signature of MS[anvil.PrinterIndex.I16, U8]: 0x5BDD841C
        *(SP + (38: SP)) = (16: Z) [signed, Z, 8]  // size of MS[anvil.PrinterIndex.I16, U8]((0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8))
        goto .204
        */



        generalRegFiles(10.U) := (SP + 34.U(16.W))

        val __tmp_2182 = (SP + 34.U(16.W))
        val __tmp_2183 = (1541243932.U(32.W)).asUInt
        arrayRegFiles(__tmp_2182 + 0.U) := __tmp_2183(7, 0)
        arrayRegFiles(__tmp_2182 + 1.U) := __tmp_2183(15, 8)
        arrayRegFiles(__tmp_2182 + 2.U) := __tmp_2183(23, 16)
        arrayRegFiles(__tmp_2182 + 3.U) := __tmp_2183(31, 24)

        val __tmp_2184 = (SP + 38.U(16.W))
        val __tmp_2185 = (16.S(64.W)).asUInt
        arrayRegFiles(__tmp_2184 + 0.U) := __tmp_2185(7, 0)
        arrayRegFiles(__tmp_2184 + 1.U) := __tmp_2185(15, 8)
        arrayRegFiles(__tmp_2184 + 2.U) := __tmp_2185(23, 16)
        arrayRegFiles(__tmp_2184 + 3.U) := __tmp_2185(31, 24)
        arrayRegFiles(__tmp_2184 + 4.U) := __tmp_2185(39, 32)
        arrayRegFiles(__tmp_2184 + 5.U) := __tmp_2185(47, 40)
        arrayRegFiles(__tmp_2184 + 6.U) := __tmp_2185(55, 48)
        arrayRegFiles(__tmp_2184 + 7.U) := __tmp_2185(63, 56)

        CP := 204.U
      }

      is(204.U) {
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
        goto .205
        */


        val __tmp_2186 = ((generalRegFiles(10.U) + 12.U(16.W)) + 0.S(8.W).asUInt)
        val __tmp_2187 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_2186 + 0.U) := __tmp_2187(7, 0)

        val __tmp_2188 = ((generalRegFiles(10.U) + 12.U(16.W)) + 1.S(8.W).asUInt)
        val __tmp_2189 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_2188 + 0.U) := __tmp_2189(7, 0)

        val __tmp_2190 = ((generalRegFiles(10.U) + 12.U(16.W)) + 2.S(8.W).asUInt)
        val __tmp_2191 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_2190 + 0.U) := __tmp_2191(7, 0)

        val __tmp_2192 = ((generalRegFiles(10.U) + 12.U(16.W)) + 3.S(8.W).asUInt)
        val __tmp_2193 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_2192 + 0.U) := __tmp_2193(7, 0)

        val __tmp_2194 = ((generalRegFiles(10.U) + 12.U(16.W)) + 4.S(8.W).asUInt)
        val __tmp_2195 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_2194 + 0.U) := __tmp_2195(7, 0)

        val __tmp_2196 = ((generalRegFiles(10.U) + 12.U(16.W)) + 5.S(8.W).asUInt)
        val __tmp_2197 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_2196 + 0.U) := __tmp_2197(7, 0)

        val __tmp_2198 = ((generalRegFiles(10.U) + 12.U(16.W)) + 6.S(8.W).asUInt)
        val __tmp_2199 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_2198 + 0.U) := __tmp_2199(7, 0)

        val __tmp_2200 = ((generalRegFiles(10.U) + 12.U(16.W)) + 7.S(8.W).asUInt)
        val __tmp_2201 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_2200 + 0.U) := __tmp_2201(7, 0)

        val __tmp_2202 = ((generalRegFiles(10.U) + 12.U(16.W)) + 8.S(8.W).asUInt)
        val __tmp_2203 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_2202 + 0.U) := __tmp_2203(7, 0)

        val __tmp_2204 = ((generalRegFiles(10.U) + 12.U(16.W)) + 9.S(8.W).asUInt)
        val __tmp_2205 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_2204 + 0.U) := __tmp_2205(7, 0)

        val __tmp_2206 = ((generalRegFiles(10.U) + 12.U(16.W)) + 10.S(8.W).asUInt)
        val __tmp_2207 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_2206 + 0.U) := __tmp_2207(7, 0)

        val __tmp_2208 = ((generalRegFiles(10.U) + 12.U(16.W)) + 11.S(8.W).asUInt)
        val __tmp_2209 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_2208 + 0.U) := __tmp_2209(7, 0)

        val __tmp_2210 = ((generalRegFiles(10.U) + 12.U(16.W)) + 12.S(8.W).asUInt)
        val __tmp_2211 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_2210 + 0.U) := __tmp_2211(7, 0)

        val __tmp_2212 = ((generalRegFiles(10.U) + 12.U(16.W)) + 13.S(8.W).asUInt)
        val __tmp_2213 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_2212 + 0.U) := __tmp_2213(7, 0)

        val __tmp_2214 = ((generalRegFiles(10.U) + 12.U(16.W)) + 14.S(8.W).asUInt)
        val __tmp_2215 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_2214 + 0.U) := __tmp_2215(7, 0)

        val __tmp_2216 = ((generalRegFiles(10.U) + 12.U(16.W)) + 15.S(8.W).asUInt)
        val __tmp_2217 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_2216 + 0.U) := __tmp_2217(7, 0)

        CP := 205.U
      }

      is(205.U) {
        /*
        (SP + (4: SP)) [MS[anvil.PrinterIndex.I16, U8], 30]  <-  ($10: MS[anvil.PrinterIndex.I16, U8]) [MS[anvil.PrinterIndex.I16, U8], ((*(($10: MS[anvil.PrinterIndex.I16, U8]) + (4: SP)) as SP) + (12: SP))]  // buff = ($10: MS[anvil.PrinterIndex.I16, U8])
        goto .206
        */


        val __tmp_2218 = (SP + 4.U(16.W))
        val __tmp_2219 = generalRegFiles(10.U)
        val __tmp_2220 = (Cat(
           arrayRegFiles((generalRegFiles(10.U) + 4.U(16.W)) + 7.U),
           arrayRegFiles((generalRegFiles(10.U) + 4.U(16.W)) + 6.U),
           arrayRegFiles((generalRegFiles(10.U) + 4.U(16.W)) + 5.U),
           arrayRegFiles((generalRegFiles(10.U) + 4.U(16.W)) + 4.U),
           arrayRegFiles((generalRegFiles(10.U) + 4.U(16.W)) + 3.U),
           arrayRegFiles((generalRegFiles(10.U) + 4.U(16.W)) + 2.U),
           arrayRegFiles((generalRegFiles(10.U) + 4.U(16.W)) + 1.U),
           arrayRegFiles((generalRegFiles(10.U) + 4.U(16.W)) + 0.U)
         ).asSInt.asUInt + 12.U(16.W))

        when(Idx < __tmp_2220) {
          arrayRegFiles(__tmp_2218 + Idx + 0.U) := arrayRegFiles(__tmp_2219 + Idx + 0.U)
          arrayRegFiles(__tmp_2218 + Idx + 1.U) := arrayRegFiles(__tmp_2219 + Idx + 1.U)
          arrayRegFiles(__tmp_2218 + Idx + 2.U) := arrayRegFiles(__tmp_2219 + Idx + 2.U)
          arrayRegFiles(__tmp_2218 + Idx + 3.U) := arrayRegFiles(__tmp_2219 + Idx + 3.U)
          arrayRegFiles(__tmp_2218 + Idx + 4.U) := arrayRegFiles(__tmp_2219 + Idx + 4.U)
          arrayRegFiles(__tmp_2218 + Idx + 5.U) := arrayRegFiles(__tmp_2219 + Idx + 5.U)
          arrayRegFiles(__tmp_2218 + Idx + 6.U) := arrayRegFiles(__tmp_2219 + Idx + 6.U)
          arrayRegFiles(__tmp_2218 + Idx + 7.U) := arrayRegFiles(__tmp_2219 + Idx + 7.U)
          Idx := Idx + 8.U
          LeftByteRounds := __tmp_2220 - Idx
        } .elsewhen(IdxLeftByteRounds < LeftByteRounds) {
          val __tmp_2221 = Idx - 8.U
          arrayRegFiles(__tmp_2218 + __tmp_2221 + IdxLeftByteRounds) := arrayRegFiles(__tmp_2219 + __tmp_2221 + IdxLeftByteRounds)
          IdxLeftByteRounds := IdxLeftByteRounds + 1.U
        } .otherwise {
          Idx := 0.U
          IdxLeftByteRounds := 0.U
          LeftByteRounds := 0.U
          CP := 206.U
        }


      }

      is(206.U) {
        /*
        unalloc $new@[245,16].6203A7B3: MS[anvil.PrinterIndex.I16, U8] [@34, 30]
        goto .207
        */


        CP := 207.U
      }

      is(207.U) {
        /*
        decl i: anvil.PrinterIndex.I16 @$5
        $5 = (0: anvil.PrinterIndex.I16)
        goto .208
        */



        generalRegFiles(5.U) := (0.S(8.W)).asUInt

        CP := 208.U
      }

      is(208.U) {
        /*
        decl m: U64 @$6
        $6 = ($3: U64)
        goto .209
        */



        generalRegFiles(6.U) := generalRegFiles(3.U)

        CP := 209.U
      }

      is(209.U) {
        /*
        decl d: Z @$7
        $7 = ($4: Z)
        goto .210
        */



        generalRegFiles(7.U) := (generalRegFiles(4.U).asSInt).asUInt

        CP := 210.U
      }

      is(210.U) {
        /*
        $10 = ($6: U64)
        goto .211
        */



        generalRegFiles(10.U) := generalRegFiles(6.U)

        CP := 211.U
      }

      is(211.U) {
        /*
        $11 = (($10: U64) > (0: U64))
        goto .212
        */



        generalRegFiles(11.U) := (generalRegFiles(10.U) > 0.U(64.W)).asUInt

        CP := 212.U
      }

      is(212.U) {
        /*
        $12 = ($7: Z)
        goto .213
        */



        generalRegFiles(12.U) := (generalRegFiles(7.U).asSInt).asUInt

        CP := 213.U
      }

      is(213.U) {
        /*
        $13 = (($12: Z) > (0: Z))
        goto .214
        */



        generalRegFiles(13.U) := (generalRegFiles(12.U).asSInt > 0.S(64.W)).asUInt

        CP := 214.U
      }

      is(214.U) {
        /*
        $14 = (($11: B) & ($13: B))
        goto .215
        */



        generalRegFiles(14.U) := (generalRegFiles(11.U) & generalRegFiles(13.U))

        CP := 215.U
      }

      is(215.U) {
        /*
        if ($14: B) goto .216 else goto .260
        */


        CP := Mux((generalRegFiles(14.U).asUInt) === 1.U, 216.U, 260.U)
      }

      is(216.U) {
        /*
        $10 = ($6: U64)
        goto .217
        */



        generalRegFiles(10.U) := generalRegFiles(6.U)

        CP := 217.U
      }

      is(217.U) {
        /*
        $11 = (($10: U64) & (15: U64))
        goto .218
        */



        generalRegFiles(11.U) := (generalRegFiles(10.U) & 15.U(64.W))

        CP := 218.U
      }

      is(218.U) {
        /*
        switch (($11: U64))
          (0: U64): goto 219
          (1: U64): goto 221
          (2: U64): goto 223
          (3: U64): goto 225
          (4: U64): goto 227
          (5: U64): goto 229
          (6: U64): goto 231
          (7: U64): goto 233
          (8: U64): goto 235
          (9: U64): goto 237
          (10: U64): goto 239
          (11: U64): goto 241
          (12: U64): goto 243
          (13: U64): goto 245
          (14: U64): goto 247
          (15: U64): goto 249

        */


        val __tmp_2222 = generalRegFiles(11.U)

        switch(__tmp_2222) {

          is(0.U(64.W)) {
            CP := 219.U
          }


          is(1.U(64.W)) {
            CP := 221.U
          }


          is(2.U(64.W)) {
            CP := 223.U
          }


          is(3.U(64.W)) {
            CP := 225.U
          }


          is(4.U(64.W)) {
            CP := 227.U
          }


          is(5.U(64.W)) {
            CP := 229.U
          }


          is(6.U(64.W)) {
            CP := 231.U
          }


          is(7.U(64.W)) {
            CP := 233.U
          }


          is(8.U(64.W)) {
            CP := 235.U
          }


          is(9.U(64.W)) {
            CP := 237.U
          }


          is(10.U(64.W)) {
            CP := 239.U
          }


          is(11.U(64.W)) {
            CP := 241.U
          }


          is(12.U(64.W)) {
            CP := 243.U
          }


          is(13.U(64.W)) {
            CP := 245.U
          }


          is(14.U(64.W)) {
            CP := 247.U
          }


          is(15.U(64.W)) {
            CP := 249.U
          }

        }

      }

      is(219.U) {
        /*
        $10 = (SP + (4: SP))
        $11 = ($5: anvil.PrinterIndex.I16)
        goto .220
        */



        generalRegFiles(10.U) := (SP + 4.U(16.W))


        generalRegFiles(11.U) := (generalRegFiles(5.U).asSInt).asUInt

        CP := 220.U
      }

      is(220.U) {
        /*
        *((($10: MS[anvil.PrinterIndex.I16, U8]) + (12: SP)) + (($11: anvil.PrinterIndex.I16) as SP)) = (48: U8) [unsigned, U8, 1]  // ($10: MS[anvil.PrinterIndex.I16, U8])(($11: anvil.PrinterIndex.I16)) = (48: U8)
        goto .251
        */


        val __tmp_2223 = ((generalRegFiles(10.U) + 12.U(16.W)) + generalRegFiles(11.U).asSInt.asUInt)
        val __tmp_2224 = (48.U(8.W)).asUInt
        arrayRegFiles(__tmp_2223 + 0.U) := __tmp_2224(7, 0)

        CP := 251.U
      }

      is(221.U) {
        /*
        $10 = (SP + (4: SP))
        $11 = ($5: anvil.PrinterIndex.I16)
        goto .222
        */



        generalRegFiles(10.U) := (SP + 4.U(16.W))


        generalRegFiles(11.U) := (generalRegFiles(5.U).asSInt).asUInt

        CP := 222.U
      }

      is(222.U) {
        /*
        *((($10: MS[anvil.PrinterIndex.I16, U8]) + (12: SP)) + (($11: anvil.PrinterIndex.I16) as SP)) = (49: U8) [unsigned, U8, 1]  // ($10: MS[anvil.PrinterIndex.I16, U8])(($11: anvil.PrinterIndex.I16)) = (49: U8)
        goto .251
        */


        val __tmp_2225 = ((generalRegFiles(10.U) + 12.U(16.W)) + generalRegFiles(11.U).asSInt.asUInt)
        val __tmp_2226 = (49.U(8.W)).asUInt
        arrayRegFiles(__tmp_2225 + 0.U) := __tmp_2226(7, 0)

        CP := 251.U
      }

      is(223.U) {
        /*
        $10 = (SP + (4: SP))
        $11 = ($5: anvil.PrinterIndex.I16)
        goto .224
        */



        generalRegFiles(10.U) := (SP + 4.U(16.W))


        generalRegFiles(11.U) := (generalRegFiles(5.U).asSInt).asUInt

        CP := 224.U
      }

      is(224.U) {
        /*
        *((($10: MS[anvil.PrinterIndex.I16, U8]) + (12: SP)) + (($11: anvil.PrinterIndex.I16) as SP)) = (50: U8) [unsigned, U8, 1]  // ($10: MS[anvil.PrinterIndex.I16, U8])(($11: anvil.PrinterIndex.I16)) = (50: U8)
        goto .251
        */


        val __tmp_2227 = ((generalRegFiles(10.U) + 12.U(16.W)) + generalRegFiles(11.U).asSInt.asUInt)
        val __tmp_2228 = (50.U(8.W)).asUInt
        arrayRegFiles(__tmp_2227 + 0.U) := __tmp_2228(7, 0)

        CP := 251.U
      }

      is(225.U) {
        /*
        $10 = (SP + (4: SP))
        $11 = ($5: anvil.PrinterIndex.I16)
        goto .226
        */



        generalRegFiles(10.U) := (SP + 4.U(16.W))


        generalRegFiles(11.U) := (generalRegFiles(5.U).asSInt).asUInt

        CP := 226.U
      }

      is(226.U) {
        /*
        *((($10: MS[anvil.PrinterIndex.I16, U8]) + (12: SP)) + (($11: anvil.PrinterIndex.I16) as SP)) = (51: U8) [unsigned, U8, 1]  // ($10: MS[anvil.PrinterIndex.I16, U8])(($11: anvil.PrinterIndex.I16)) = (51: U8)
        goto .251
        */


        val __tmp_2229 = ((generalRegFiles(10.U) + 12.U(16.W)) + generalRegFiles(11.U).asSInt.asUInt)
        val __tmp_2230 = (51.U(8.W)).asUInt
        arrayRegFiles(__tmp_2229 + 0.U) := __tmp_2230(7, 0)

        CP := 251.U
      }

      is(227.U) {
        /*
        $10 = (SP + (4: SP))
        $11 = ($5: anvil.PrinterIndex.I16)
        goto .228
        */



        generalRegFiles(10.U) := (SP + 4.U(16.W))


        generalRegFiles(11.U) := (generalRegFiles(5.U).asSInt).asUInt

        CP := 228.U
      }

      is(228.U) {
        /*
        *((($10: MS[anvil.PrinterIndex.I16, U8]) + (12: SP)) + (($11: anvil.PrinterIndex.I16) as SP)) = (52: U8) [unsigned, U8, 1]  // ($10: MS[anvil.PrinterIndex.I16, U8])(($11: anvil.PrinterIndex.I16)) = (52: U8)
        goto .251
        */


        val __tmp_2231 = ((generalRegFiles(10.U) + 12.U(16.W)) + generalRegFiles(11.U).asSInt.asUInt)
        val __tmp_2232 = (52.U(8.W)).asUInt
        arrayRegFiles(__tmp_2231 + 0.U) := __tmp_2232(7, 0)

        CP := 251.U
      }

      is(229.U) {
        /*
        $10 = (SP + (4: SP))
        $11 = ($5: anvil.PrinterIndex.I16)
        goto .230
        */



        generalRegFiles(10.U) := (SP + 4.U(16.W))


        generalRegFiles(11.U) := (generalRegFiles(5.U).asSInt).asUInt

        CP := 230.U
      }

      is(230.U) {
        /*
        *((($10: MS[anvil.PrinterIndex.I16, U8]) + (12: SP)) + (($11: anvil.PrinterIndex.I16) as SP)) = (53: U8) [unsigned, U8, 1]  // ($10: MS[anvil.PrinterIndex.I16, U8])(($11: anvil.PrinterIndex.I16)) = (53: U8)
        goto .251
        */


        val __tmp_2233 = ((generalRegFiles(10.U) + 12.U(16.W)) + generalRegFiles(11.U).asSInt.asUInt)
        val __tmp_2234 = (53.U(8.W)).asUInt
        arrayRegFiles(__tmp_2233 + 0.U) := __tmp_2234(7, 0)

        CP := 251.U
      }

      is(231.U) {
        /*
        $10 = (SP + (4: SP))
        $11 = ($5: anvil.PrinterIndex.I16)
        goto .232
        */



        generalRegFiles(10.U) := (SP + 4.U(16.W))


        generalRegFiles(11.U) := (generalRegFiles(5.U).asSInt).asUInt

        CP := 232.U
      }

      is(232.U) {
        /*
        *((($10: MS[anvil.PrinterIndex.I16, U8]) + (12: SP)) + (($11: anvil.PrinterIndex.I16) as SP)) = (54: U8) [unsigned, U8, 1]  // ($10: MS[anvil.PrinterIndex.I16, U8])(($11: anvil.PrinterIndex.I16)) = (54: U8)
        goto .251
        */


        val __tmp_2235 = ((generalRegFiles(10.U) + 12.U(16.W)) + generalRegFiles(11.U).asSInt.asUInt)
        val __tmp_2236 = (54.U(8.W)).asUInt
        arrayRegFiles(__tmp_2235 + 0.U) := __tmp_2236(7, 0)

        CP := 251.U
      }

      is(233.U) {
        /*
        $10 = (SP + (4: SP))
        $11 = ($5: anvil.PrinterIndex.I16)
        goto .234
        */



        generalRegFiles(10.U) := (SP + 4.U(16.W))


        generalRegFiles(11.U) := (generalRegFiles(5.U).asSInt).asUInt

        CP := 234.U
      }

      is(234.U) {
        /*
        *((($10: MS[anvil.PrinterIndex.I16, U8]) + (12: SP)) + (($11: anvil.PrinterIndex.I16) as SP)) = (55: U8) [unsigned, U8, 1]  // ($10: MS[anvil.PrinterIndex.I16, U8])(($11: anvil.PrinterIndex.I16)) = (55: U8)
        goto .251
        */


        val __tmp_2237 = ((generalRegFiles(10.U) + 12.U(16.W)) + generalRegFiles(11.U).asSInt.asUInt)
        val __tmp_2238 = (55.U(8.W)).asUInt
        arrayRegFiles(__tmp_2237 + 0.U) := __tmp_2238(7, 0)

        CP := 251.U
      }

      is(235.U) {
        /*
        $10 = (SP + (4: SP))
        $11 = ($5: anvil.PrinterIndex.I16)
        goto .236
        */



        generalRegFiles(10.U) := (SP + 4.U(16.W))


        generalRegFiles(11.U) := (generalRegFiles(5.U).asSInt).asUInt

        CP := 236.U
      }

      is(236.U) {
        /*
        *((($10: MS[anvil.PrinterIndex.I16, U8]) + (12: SP)) + (($11: anvil.PrinterIndex.I16) as SP)) = (56: U8) [unsigned, U8, 1]  // ($10: MS[anvil.PrinterIndex.I16, U8])(($11: anvil.PrinterIndex.I16)) = (56: U8)
        goto .251
        */


        val __tmp_2239 = ((generalRegFiles(10.U) + 12.U(16.W)) + generalRegFiles(11.U).asSInt.asUInt)
        val __tmp_2240 = (56.U(8.W)).asUInt
        arrayRegFiles(__tmp_2239 + 0.U) := __tmp_2240(7, 0)

        CP := 251.U
      }

      is(237.U) {
        /*
        $10 = (SP + (4: SP))
        $11 = ($5: anvil.PrinterIndex.I16)
        goto .238
        */



        generalRegFiles(10.U) := (SP + 4.U(16.W))


        generalRegFiles(11.U) := (generalRegFiles(5.U).asSInt).asUInt

        CP := 238.U
      }

      is(238.U) {
        /*
        *((($10: MS[anvil.PrinterIndex.I16, U8]) + (12: SP)) + (($11: anvil.PrinterIndex.I16) as SP)) = (57: U8) [unsigned, U8, 1]  // ($10: MS[anvil.PrinterIndex.I16, U8])(($11: anvil.PrinterIndex.I16)) = (57: U8)
        goto .251
        */


        val __tmp_2241 = ((generalRegFiles(10.U) + 12.U(16.W)) + generalRegFiles(11.U).asSInt.asUInt)
        val __tmp_2242 = (57.U(8.W)).asUInt
        arrayRegFiles(__tmp_2241 + 0.U) := __tmp_2242(7, 0)

        CP := 251.U
      }

      is(239.U) {
        /*
        $10 = (SP + (4: SP))
        $11 = ($5: anvil.PrinterIndex.I16)
        goto .240
        */



        generalRegFiles(10.U) := (SP + 4.U(16.W))


        generalRegFiles(11.U) := (generalRegFiles(5.U).asSInt).asUInt

        CP := 240.U
      }

      is(240.U) {
        /*
        *((($10: MS[anvil.PrinterIndex.I16, U8]) + (12: SP)) + (($11: anvil.PrinterIndex.I16) as SP)) = (65: U8) [unsigned, U8, 1]  // ($10: MS[anvil.PrinterIndex.I16, U8])(($11: anvil.PrinterIndex.I16)) = (65: U8)
        goto .251
        */


        val __tmp_2243 = ((generalRegFiles(10.U) + 12.U(16.W)) + generalRegFiles(11.U).asSInt.asUInt)
        val __tmp_2244 = (65.U(8.W)).asUInt
        arrayRegFiles(__tmp_2243 + 0.U) := __tmp_2244(7, 0)

        CP := 251.U
      }

      is(241.U) {
        /*
        $10 = (SP + (4: SP))
        $11 = ($5: anvil.PrinterIndex.I16)
        goto .242
        */



        generalRegFiles(10.U) := (SP + 4.U(16.W))


        generalRegFiles(11.U) := (generalRegFiles(5.U).asSInt).asUInt

        CP := 242.U
      }

      is(242.U) {
        /*
        *((($10: MS[anvil.PrinterIndex.I16, U8]) + (12: SP)) + (($11: anvil.PrinterIndex.I16) as SP)) = (66: U8) [unsigned, U8, 1]  // ($10: MS[anvil.PrinterIndex.I16, U8])(($11: anvil.PrinterIndex.I16)) = (66: U8)
        goto .251
        */


        val __tmp_2245 = ((generalRegFiles(10.U) + 12.U(16.W)) + generalRegFiles(11.U).asSInt.asUInt)
        val __tmp_2246 = (66.U(8.W)).asUInt
        arrayRegFiles(__tmp_2245 + 0.U) := __tmp_2246(7, 0)

        CP := 251.U
      }

      is(243.U) {
        /*
        $10 = (SP + (4: SP))
        $11 = ($5: anvil.PrinterIndex.I16)
        goto .244
        */



        generalRegFiles(10.U) := (SP + 4.U(16.W))


        generalRegFiles(11.U) := (generalRegFiles(5.U).asSInt).asUInt

        CP := 244.U
      }

      is(244.U) {
        /*
        *((($10: MS[anvil.PrinterIndex.I16, U8]) + (12: SP)) + (($11: anvil.PrinterIndex.I16) as SP)) = (67: U8) [unsigned, U8, 1]  // ($10: MS[anvil.PrinterIndex.I16, U8])(($11: anvil.PrinterIndex.I16)) = (67: U8)
        goto .251
        */


        val __tmp_2247 = ((generalRegFiles(10.U) + 12.U(16.W)) + generalRegFiles(11.U).asSInt.asUInt)
        val __tmp_2248 = (67.U(8.W)).asUInt
        arrayRegFiles(__tmp_2247 + 0.U) := __tmp_2248(7, 0)

        CP := 251.U
      }

      is(245.U) {
        /*
        $10 = (SP + (4: SP))
        $11 = ($5: anvil.PrinterIndex.I16)
        goto .246
        */



        generalRegFiles(10.U) := (SP + 4.U(16.W))


        generalRegFiles(11.U) := (generalRegFiles(5.U).asSInt).asUInt

        CP := 246.U
      }

      is(246.U) {
        /*
        *((($10: MS[anvil.PrinterIndex.I16, U8]) + (12: SP)) + (($11: anvil.PrinterIndex.I16) as SP)) = (68: U8) [unsigned, U8, 1]  // ($10: MS[anvil.PrinterIndex.I16, U8])(($11: anvil.PrinterIndex.I16)) = (68: U8)
        goto .251
        */


        val __tmp_2249 = ((generalRegFiles(10.U) + 12.U(16.W)) + generalRegFiles(11.U).asSInt.asUInt)
        val __tmp_2250 = (68.U(8.W)).asUInt
        arrayRegFiles(__tmp_2249 + 0.U) := __tmp_2250(7, 0)

        CP := 251.U
      }

      is(247.U) {
        /*
        $10 = (SP + (4: SP))
        $11 = ($5: anvil.PrinterIndex.I16)
        goto .248
        */



        generalRegFiles(10.U) := (SP + 4.U(16.W))


        generalRegFiles(11.U) := (generalRegFiles(5.U).asSInt).asUInt

        CP := 248.U
      }

      is(248.U) {
        /*
        *((($10: MS[anvil.PrinterIndex.I16, U8]) + (12: SP)) + (($11: anvil.PrinterIndex.I16) as SP)) = (69: U8) [unsigned, U8, 1]  // ($10: MS[anvil.PrinterIndex.I16, U8])(($11: anvil.PrinterIndex.I16)) = (69: U8)
        goto .251
        */


        val __tmp_2251 = ((generalRegFiles(10.U) + 12.U(16.W)) + generalRegFiles(11.U).asSInt.asUInt)
        val __tmp_2252 = (69.U(8.W)).asUInt
        arrayRegFiles(__tmp_2251 + 0.U) := __tmp_2252(7, 0)

        CP := 251.U
      }

      is(249.U) {
        /*
        $10 = (SP + (4: SP))
        $11 = ($5: anvil.PrinterIndex.I16)
        goto .250
        */



        generalRegFiles(10.U) := (SP + 4.U(16.W))


        generalRegFiles(11.U) := (generalRegFiles(5.U).asSInt).asUInt

        CP := 250.U
      }

      is(250.U) {
        /*
        *((($10: MS[anvil.PrinterIndex.I16, U8]) + (12: SP)) + (($11: anvil.PrinterIndex.I16) as SP)) = (70: U8) [unsigned, U8, 1]  // ($10: MS[anvil.PrinterIndex.I16, U8])(($11: anvil.PrinterIndex.I16)) = (70: U8)
        goto .251
        */


        val __tmp_2253 = ((generalRegFiles(10.U) + 12.U(16.W)) + generalRegFiles(11.U).asSInt.asUInt)
        val __tmp_2254 = (70.U(8.W)).asUInt
        arrayRegFiles(__tmp_2253 + 0.U) := __tmp_2254(7, 0)

        CP := 251.U
      }

      is(251.U) {
        /*
        $10 = ($6: U64)
        goto .252
        */



        generalRegFiles(10.U) := generalRegFiles(6.U)

        CP := 252.U
      }

      is(252.U) {
        /*
        $11 = (($10: U64) >>> (4: U64))
        goto .253
        */



        generalRegFiles(11.U) := (((generalRegFiles(10.U)) >> 4.U(64.W)(4,0)))

        CP := 253.U
      }

      is(253.U) {
        /*
        $6 = ($11: U64)
        goto .254
        */



        generalRegFiles(6.U) := generalRegFiles(11.U)

        CP := 254.U
      }

      is(254.U) {
        /*
        $10 = ($5: anvil.PrinterIndex.I16)
        goto .255
        */



        generalRegFiles(10.U) := (generalRegFiles(5.U).asSInt).asUInt

        CP := 255.U
      }

      is(255.U) {
        /*
        $11 = (($10: anvil.PrinterIndex.I16) + (1: anvil.PrinterIndex.I16))
        goto .256
        */



        generalRegFiles(11.U) := ((generalRegFiles(10.U).asSInt + 1.S(8.W))).asUInt

        CP := 256.U
      }

      is(256.U) {
        /*
        $5 = ($11: anvil.PrinterIndex.I16)
        goto .257
        */



        generalRegFiles(5.U) := (generalRegFiles(11.U).asSInt).asUInt

        CP := 257.U
      }

      is(257.U) {
        /*
        $10 = ($7: Z)
        goto .258
        */



        generalRegFiles(10.U) := (generalRegFiles(7.U).asSInt).asUInt

        CP := 258.U
      }

      is(258.U) {
        /*
        $11 = (($10: Z) - (1: Z))
        goto .259
        */



        generalRegFiles(11.U) := ((generalRegFiles(10.U).asSInt - 1.S(64.W))).asUInt

        CP := 259.U
      }

      is(259.U) {
        /*
        $7 = ($11: Z)
        goto .210
        */



        generalRegFiles(7.U) := (generalRegFiles(11.U).asSInt).asUInt

        CP := 210.U
      }

      is(260.U) {
        /*
        decl idx: anvil.PrinterIndex.U @$8
        $10 = ($1: anvil.PrinterIndex.U)
        goto .261
        */



        generalRegFiles(10.U) := generalRegFiles(1.U)

        CP := 261.U
      }

      is(261.U) {
        /*
        $8 = ($10: anvil.PrinterIndex.U)
        goto .262
        */



        generalRegFiles(8.U) := generalRegFiles(10.U)

        CP := 262.U
      }

      is(262.U) {
        /*
        $10 = ($7: Z)
        goto .263
        */



        generalRegFiles(10.U) := (generalRegFiles(7.U).asSInt).asUInt

        CP := 263.U
      }

      is(263.U) {
        /*
        $11 = (($10: Z) > (0: Z))
        goto .264
        */



        generalRegFiles(11.U) := (generalRegFiles(10.U).asSInt > 0.S(64.W)).asUInt

        CP := 264.U
      }

      is(264.U) {
        /*
        if ($11: B) goto .265 else goto .274
        */


        CP := Mux((generalRegFiles(11.U).asUInt) === 1.U, 265.U, 274.U)
      }

      is(265.U) {
        /*
        $10 = ($0: MS[anvil.PrinterIndex.U, U8])
        $11 = ($8: anvil.PrinterIndex.U)
        $12 = ($2: anvil.PrinterIndex.U)
        goto .266
        */



        generalRegFiles(10.U) := generalRegFiles(0.U)


        generalRegFiles(11.U) := generalRegFiles(8.U)


        generalRegFiles(12.U) := generalRegFiles(2.U)

        CP := 266.U
      }

      is(266.U) {
        /*
        $13 = (($11: anvil.PrinterIndex.U) & ($12: anvil.PrinterIndex.U))
        goto .267
        */



        generalRegFiles(13.U) := (generalRegFiles(11.U) & generalRegFiles(12.U))

        CP := 267.U
      }

      is(267.U) {
        /*
        *((($10: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($13: anvil.PrinterIndex.U) as SP)) = (48: U8) [unsigned, U8, 1]  // ($10: MS[anvil.PrinterIndex.U, U8])(($13: anvil.PrinterIndex.U)) = (48: U8)
        goto .268
        */


        val __tmp_2255 = ((generalRegFiles(10.U) + 12.U(16.W)) + generalRegFiles(13.U).asUInt)
        val __tmp_2256 = (48.U(8.W)).asUInt
        arrayRegFiles(__tmp_2255 + 0.U) := __tmp_2256(7, 0)

        CP := 268.U
      }

      is(268.U) {
        /*
        $10 = ($7: Z)
        goto .269
        */



        generalRegFiles(10.U) := (generalRegFiles(7.U).asSInt).asUInt

        CP := 269.U
      }

      is(269.U) {
        /*
        $11 = (($10: Z) - (1: Z))
        goto .270
        */



        generalRegFiles(11.U) := ((generalRegFiles(10.U).asSInt - 1.S(64.W))).asUInt

        CP := 270.U
      }

      is(270.U) {
        /*
        $7 = ($11: Z)
        goto .271
        */



        generalRegFiles(7.U) := (generalRegFiles(11.U).asSInt).asUInt

        CP := 271.U
      }

      is(271.U) {
        /*
        $10 = ($8: anvil.PrinterIndex.U)
        goto .272
        */



        generalRegFiles(10.U) := generalRegFiles(8.U)

        CP := 272.U
      }

      is(272.U) {
        /*
        $11 = (($10: anvil.PrinterIndex.U) + (1: anvil.PrinterIndex.U))
        goto .273
        */



        generalRegFiles(11.U) := (generalRegFiles(10.U) + 1.U(64.W))

        CP := 273.U
      }

      is(273.U) {
        /*
        $8 = ($11: anvil.PrinterIndex.U)
        goto .262
        */



        generalRegFiles(8.U) := generalRegFiles(11.U)

        CP := 262.U
      }

      is(274.U) {
        /*
        decl j: anvil.PrinterIndex.I16 @$9
        $10 = ($5: anvil.PrinterIndex.I16)
        undecl i: anvil.PrinterIndex.I16 @$5
        goto .275
        */



        generalRegFiles(10.U) := (generalRegFiles(5.U).asSInt).asUInt

        CP := 275.U
      }

      is(275.U) {
        /*
        $11 = (($10: anvil.PrinterIndex.I16) - (1: anvil.PrinterIndex.I16))
        goto .276
        */



        generalRegFiles(11.U) := ((generalRegFiles(10.U).asSInt - 1.S(8.W))).asUInt

        CP := 276.U
      }

      is(276.U) {
        /*
        $9 = ($11: anvil.PrinterIndex.I16)
        goto .277
        */



        generalRegFiles(9.U) := (generalRegFiles(11.U).asSInt).asUInt

        CP := 277.U
      }

      is(277.U) {
        /*
        $10 = ($9: anvil.PrinterIndex.I16)
        goto .278
        */



        generalRegFiles(10.U) := (generalRegFiles(9.U).asSInt).asUInt

        CP := 278.U
      }

      is(278.U) {
        /*
        $11 = (($10: anvil.PrinterIndex.I16) >= (0: anvil.PrinterIndex.I16))
        goto .279
        */



        generalRegFiles(11.U) := (generalRegFiles(10.U).asSInt >= 0.S(8.W)).asUInt

        CP := 279.U
      }

      is(279.U) {
        /*
        if ($11: B) goto .280 else goto .291
        */


        CP := Mux((generalRegFiles(11.U).asUInt) === 1.U, 280.U, 291.U)
      }

      is(280.U) {
        /*
        $10 = ($0: MS[anvil.PrinterIndex.U, U8])
        $11 = ($8: anvil.PrinterIndex.U)
        $12 = ($2: anvil.PrinterIndex.U)
        goto .281
        */



        generalRegFiles(10.U) := generalRegFiles(0.U)


        generalRegFiles(11.U) := generalRegFiles(8.U)


        generalRegFiles(12.U) := generalRegFiles(2.U)

        CP := 281.U
      }

      is(281.U) {
        /*
        $13 = (($11: anvil.PrinterIndex.U) & ($12: anvil.PrinterIndex.U))
        goto .282
        */



        generalRegFiles(13.U) := (generalRegFiles(11.U) & generalRegFiles(12.U))

        CP := 282.U
      }

      is(282.U) {
        /*
        $14 = (SP + (4: SP))
        $15 = ($9: anvil.PrinterIndex.I16)
        goto .283
        */



        generalRegFiles(14.U) := (SP + 4.U(16.W))


        generalRegFiles(15.U) := (generalRegFiles(9.U).asSInt).asUInt

        CP := 283.U
      }

      is(283.U) {
        /*
        $16 = *((($14: MS[anvil.PrinterIndex.I16, U8]) + (12: SP)) + (($15: anvil.PrinterIndex.I16) as SP)) [unsigned, U8, 1]  // $16 = ($14: MS[anvil.PrinterIndex.I16, U8])(($15: anvil.PrinterIndex.I16))
        goto .284
        */


        val __tmp_2257 = (((generalRegFiles(14.U) + 12.U(16.W)) + generalRegFiles(15.U).asSInt.asUInt)).asUInt
        generalRegFiles(16.U) := Cat(
          arrayRegFiles(__tmp_2257 + 0.U)
        ).asUInt

        CP := 284.U
      }

      is(284.U) {
        /*
        *((($10: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($13: anvil.PrinterIndex.U) as SP)) = ($16: U8) [unsigned, U8, 1]  // ($10: MS[anvil.PrinterIndex.U, U8])(($13: anvil.PrinterIndex.U)) = ($16: U8)
        goto .285
        */


        val __tmp_2258 = ((generalRegFiles(10.U) + 12.U(16.W)) + generalRegFiles(13.U).asUInt)
        val __tmp_2259 = (generalRegFiles(16.U)).asUInt
        arrayRegFiles(__tmp_2258 + 0.U) := __tmp_2259(7, 0)

        CP := 285.U
      }

      is(285.U) {
        /*
        $10 = ($9: anvil.PrinterIndex.I16)
        goto .286
        */



        generalRegFiles(10.U) := (generalRegFiles(9.U).asSInt).asUInt

        CP := 286.U
      }

      is(286.U) {
        /*
        $11 = (($10: anvil.PrinterIndex.I16) - (1: anvil.PrinterIndex.I16))
        goto .287
        */



        generalRegFiles(11.U) := ((generalRegFiles(10.U).asSInt - 1.S(8.W))).asUInt

        CP := 287.U
      }

      is(287.U) {
        /*
        $9 = ($11: anvil.PrinterIndex.I16)
        goto .288
        */



        generalRegFiles(9.U) := (generalRegFiles(11.U).asSInt).asUInt

        CP := 288.U
      }

      is(288.U) {
        /*
        $10 = ($8: anvil.PrinterIndex.U)
        goto .289
        */



        generalRegFiles(10.U) := generalRegFiles(8.U)

        CP := 289.U
      }

      is(289.U) {
        /*
        $11 = (($10: anvil.PrinterIndex.U) + (1: anvil.PrinterIndex.U))
        goto .290
        */



        generalRegFiles(11.U) := (generalRegFiles(10.U) + 1.U(64.W))

        CP := 290.U
      }

      is(290.U) {
        /*
        $8 = ($11: anvil.PrinterIndex.U)
        goto .277
        */



        generalRegFiles(8.U) := generalRegFiles(11.U)

        CP := 277.U
      }

      is(291.U) {
        /*
        $10 = ($4: Z)
        goto .292
        */



        generalRegFiles(10.U) := (generalRegFiles(4.U).asSInt).asUInt

        CP := 292.U
      }

      is(292.U) {
        /*
        $11 = (($10: Z) as U64)
        goto .293
        */



        generalRegFiles(11.U) := generalRegFiles(10.U).asSInt.asUInt

        CP := 293.U
      }

      is(293.U) {
        /*
        **(SP + (2: SP)) = ($11: U64) [unsigned, U64, 8]  // $res = ($11: U64)
        goto $ret@0
        */


        val __tmp_2260 = Cat(
          arrayRegFiles((SP + 2.U(16.W)) + 1.U),
          arrayRegFiles((SP + 2.U(16.W)) + 0.U)
        )
        val __tmp_2261 = (generalRegFiles(11.U)).asUInt
        arrayRegFiles(__tmp_2260 + 0.U) := __tmp_2261(7, 0)
        arrayRegFiles(__tmp_2260 + 1.U) := __tmp_2261(15, 8)
        arrayRegFiles(__tmp_2260 + 2.U) := __tmp_2261(23, 16)
        arrayRegFiles(__tmp_2260 + 3.U) := __tmp_2261(31, 24)
        arrayRegFiles(__tmp_2260 + 4.U) := __tmp_2261(39, 32)
        arrayRegFiles(__tmp_2260 + 5.U) := __tmp_2261(47, 40)
        arrayRegFiles(__tmp_2260 + 6.U) := __tmp_2261(55, 48)
        arrayRegFiles(__tmp_2260 + 7.U) := __tmp_2261(63, 56)

        CP := Cat(
          arrayRegFiles(SP + 0.U + 1.U),
          arrayRegFiles(SP + 0.U + 0.U)
        )

      }

    }

}


