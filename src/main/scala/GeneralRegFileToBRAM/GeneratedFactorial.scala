package GeneralRegFileToBRAM
import chisel3._
import chisel3.util._
import chisel3.experimental._



class FactorialTest (val C_S_AXI_DATA_WIDTH:  Int = 32,
               val C_S_AXI_ADDR_WIDTH:  Int = 32,
               val ARRAY_REG_WIDTH:     Int = 8,
               val ARRAY_REG_DEPTH:     Int = 144,
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

        val __tmp_2262 = 10.U(32.W)
        val __tmp_2263 = (886747591.U(32.W)).asUInt
        arrayRegFiles(__tmp_2262 + 0.U) := __tmp_2263(7, 0)
        arrayRegFiles(__tmp_2262 + 1.U) := __tmp_2263(15, 8)
        arrayRegFiles(__tmp_2262 + 2.U) := __tmp_2263(23, 16)
        arrayRegFiles(__tmp_2262 + 3.U) := __tmp_2263(31, 24)

        val __tmp_2264 = 14.U(16.W)
        val __tmp_2265 = (32.S(64.W)).asUInt
        arrayRegFiles(__tmp_2264 + 0.U) := __tmp_2265(7, 0)
        arrayRegFiles(__tmp_2264 + 1.U) := __tmp_2265(15, 8)
        arrayRegFiles(__tmp_2264 + 2.U) := __tmp_2265(23, 16)
        arrayRegFiles(__tmp_2264 + 3.U) := __tmp_2265(31, 24)
        arrayRegFiles(__tmp_2264 + 4.U) := __tmp_2265(39, 32)
        arrayRegFiles(__tmp_2264 + 5.U) := __tmp_2265(47, 40)
        arrayRegFiles(__tmp_2264 + 6.U) := __tmp_2265(55, 48)
        arrayRegFiles(__tmp_2264 + 7.U) := __tmp_2265(63, 56)

        val __tmp_2266 = 52.U(16.W)
        val __tmp_2267 = (0.U(16.W)).asUInt
        arrayRegFiles(__tmp_2266 + 0.U) := __tmp_2267(7, 0)
        arrayRegFiles(__tmp_2266 + 1.U) := __tmp_2267(15, 8)

        CP := 4.U
      }

      is(4.U) {
        /*
        $0 = *(0: SP) [signed, Z, 8]  // $0 = $testNum
        goto .5
        */


        val __tmp_2268 = (0.U(16.W)).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_2268 + 7.U),
          arrayRegFiles(__tmp_2268 + 6.U),
          arrayRegFiles(__tmp_2268 + 5.U),
          arrayRegFiles(__tmp_2268 + 4.U),
          arrayRegFiles(__tmp_2268 + 3.U),
          arrayRegFiles(__tmp_2268 + 2.U),
          arrayRegFiles(__tmp_2268 + 1.U),
          arrayRegFiles(__tmp_2268 + 0.U)
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
        *SP = (9: CP) [unsigned, CP, 2]  // $ret@0 = 1340
        *(SP - (8: SP)) = ($0: Z) [signed, Z, 8]  // save $0 (Z)
        goto .8
        */


        val __tmp_2269 = SP
        val __tmp_2270 = (9.U(16.W)).asUInt
        arrayRegFiles(__tmp_2269 + 0.U) := __tmp_2270(7, 0)
        arrayRegFiles(__tmp_2269 + 1.U) := __tmp_2270(15, 8)

        val __tmp_2271 = (SP - 8.U(16.W))
        val __tmp_2272 = (generalRegFiles(0.U).asSInt).asUInt
        arrayRegFiles(__tmp_2271 + 0.U) := __tmp_2272(7, 0)
        arrayRegFiles(__tmp_2271 + 1.U) := __tmp_2272(15, 8)
        arrayRegFiles(__tmp_2271 + 2.U) := __tmp_2272(23, 16)
        arrayRegFiles(__tmp_2271 + 3.U) := __tmp_2272(31, 24)
        arrayRegFiles(__tmp_2271 + 4.U) := __tmp_2272(39, 32)
        arrayRegFiles(__tmp_2271 + 5.U) := __tmp_2272(47, 40)
        arrayRegFiles(__tmp_2271 + 6.U) := __tmp_2272(55, 48)
        arrayRegFiles(__tmp_2271 + 7.U) := __tmp_2272(63, 56)

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


        val __tmp_2273 = ((SP - 8.U(16.W))).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_2273 + 7.U),
          arrayRegFiles(__tmp_2273 + 6.U),
          arrayRegFiles(__tmp_2273 + 5.U),
          arrayRegFiles(__tmp_2273 + 4.U),
          arrayRegFiles(__tmp_2273 + 3.U),
          arrayRegFiles(__tmp_2273 + 2.U),
          arrayRegFiles(__tmp_2273 + 1.U),
          arrayRegFiles(__tmp_2273 + 0.U)
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
        *SP = (15: CP) [unsigned, CP, 2]  // $ret@0 = 1342
        *(SP - (8: SP)) = ($0: Z) [signed, Z, 8]  // save $0 (Z)
        goto .14
        */


        val __tmp_2274 = SP
        val __tmp_2275 = (15.U(16.W)).asUInt
        arrayRegFiles(__tmp_2274 + 0.U) := __tmp_2275(7, 0)
        arrayRegFiles(__tmp_2274 + 1.U) := __tmp_2275(15, 8)

        val __tmp_2276 = (SP - 8.U(16.W))
        val __tmp_2277 = (generalRegFiles(0.U).asSInt).asUInt
        arrayRegFiles(__tmp_2276 + 0.U) := __tmp_2277(7, 0)
        arrayRegFiles(__tmp_2276 + 1.U) := __tmp_2277(15, 8)
        arrayRegFiles(__tmp_2276 + 2.U) := __tmp_2277(23, 16)
        arrayRegFiles(__tmp_2276 + 3.U) := __tmp_2277(31, 24)
        arrayRegFiles(__tmp_2276 + 4.U) := __tmp_2277(39, 32)
        arrayRegFiles(__tmp_2276 + 5.U) := __tmp_2277(47, 40)
        arrayRegFiles(__tmp_2276 + 6.U) := __tmp_2277(55, 48)
        arrayRegFiles(__tmp_2276 + 7.U) := __tmp_2277(63, 56)

        CP := 14.U
      }

      is(14.U) {
        /*
        decl $ret: CP [@0, 2]
        goto .39
        */


        CP := 39.U
      }

      is(15.U) {
        /*
        $0 = *(SP - (8: SP)) [signed, Z, 8]  // restore $0 (Z)
        undecl $ret: CP [@0, 2]
        goto .16
        */


        val __tmp_2278 = ((SP - 8.U(16.W))).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_2278 + 7.U),
          arrayRegFiles(__tmp_2278 + 6.U),
          arrayRegFiles(__tmp_2278 + 5.U),
          arrayRegFiles(__tmp_2278 + 4.U),
          arrayRegFiles(__tmp_2278 + 3.U),
          arrayRegFiles(__tmp_2278 + 2.U),
          arrayRegFiles(__tmp_2278 + 1.U),
          arrayRegFiles(__tmp_2278 + 0.U)
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
        *SP = (21: CP) [unsigned, CP, 2]  // $ret@0 = 1344
        goto .20
        */


        val __tmp_2279 = SP
        val __tmp_2280 = (21.U(16.W)).asUInt
        arrayRegFiles(__tmp_2279 + 0.U) := __tmp_2280(7, 0)
        arrayRegFiles(__tmp_2279 + 1.U) := __tmp_2280(15, 8)

        CP := 20.U
      }

      is(20.U) {
        /*
        decl $ret: CP [@0, 2]
        goto .54
        */


        CP := 54.U
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
        alloc factorial$res@[13,11].F02055C0: U32 [@2, 4]
        goto .25
        */


        CP := 25.U
      }

      is(25.U) {
        /*
        SP = SP + 14
        goto .26
        */


        SP := SP + 14.U

        CP := 26.U
      }

      is(26.U) {
        /*
        *SP = (28: CP) [unsigned, CP, 2]  // $ret@0 = 1346
        *(SP + (2: SP)) = (SP - (12: SP)) [unsigned, SP, 2]  // $res@2 = -12
        $10 = (1: U32)
        goto .27
        */


        val __tmp_2281 = SP
        val __tmp_2282 = (28.U(16.W)).asUInt
        arrayRegFiles(__tmp_2281 + 0.U) := __tmp_2282(7, 0)
        arrayRegFiles(__tmp_2281 + 1.U) := __tmp_2282(15, 8)

        val __tmp_2283 = (SP + 2.U(16.W))
        val __tmp_2284 = ((SP - 12.U(16.W))).asUInt
        arrayRegFiles(__tmp_2283 + 0.U) := __tmp_2284(7, 0)
        arrayRegFiles(__tmp_2283 + 1.U) := __tmp_2284(15, 8)


        generalRegFiles(10.U) := 1.U(32.W)

        CP := 27.U
      }

      is(27.U) {
        /*
        decl $ret: CP [@0, 2], $res: SP [@2, 6], n: U32 @$0
        $0 = ($10: U32)
        goto .69
        */



        generalRegFiles(0.U) := generalRegFiles(10.U)

        CP := 69.U
      }

      is(28.U) {
        /*
        $2 = **(SP + (2: SP)) [unsigned, U32, 4]  // $2 = $res
        undecl n: U32 @$0, $res: SP [@2, 6], $ret: CP [@0, 2]
        goto .29
        */


        val __tmp_2285 = (Cat(
          arrayRegFiles((SP + 2.U(16.W)) + 1.U),
          arrayRegFiles((SP + 2.U(16.W)) + 0.U)
        )).asUInt
        generalRegFiles(2.U) := Cat(
          arrayRegFiles(__tmp_2285 + 3.U),
          arrayRegFiles(__tmp_2285 + 2.U),
          arrayRegFiles(__tmp_2285 + 1.U),
          arrayRegFiles(__tmp_2285 + 0.U)
        ).asUInt

        CP := 29.U
      }

      is(29.U) {
        /*
        SP = SP - 14
        goto .30
        */


        SP := SP - 14.U

        CP := 30.U
      }

      is(30.U) {
        /*
        alloc printU64Hex$res@[13,11].F02055C0: U64 [@6, 8]
        goto .31
        */


        CP := 31.U
      }

      is(31.U) {
        /*
        SP = SP + 14
        goto .32
        */


        SP := SP + 14.U

        CP := 32.U
      }

      is(32.U) {
        /*
        *SP = (34: CP) [unsigned, CP, 2]  // $ret@0 = 1348
        *(SP + (2: SP)) = (SP - (8: SP)) [unsigned, SP, 2]  // $res@2 = -8
        $91 = (8: SP)
        $92 = DP
        $93 = (31: anvil.PrinterIndex.U)
        $94 = (($2: U32) as U64)
        $95 = (8: Z)
        goto .33
        */


        val __tmp_2286 = SP
        val __tmp_2287 = (34.U(16.W)).asUInt
        arrayRegFiles(__tmp_2286 + 0.U) := __tmp_2287(7, 0)
        arrayRegFiles(__tmp_2286 + 1.U) := __tmp_2287(15, 8)

        val __tmp_2288 = (SP + 2.U(16.W))
        val __tmp_2289 = ((SP - 8.U(16.W))).asUInt
        arrayRegFiles(__tmp_2288 + 0.U) := __tmp_2289(7, 0)
        arrayRegFiles(__tmp_2288 + 1.U) := __tmp_2289(15, 8)


        generalRegFiles(91.U) := 8.U(16.W)


        generalRegFiles(92.U) := DP


        generalRegFiles(93.U) := 31.U(64.W)


        generalRegFiles(94.U) := generalRegFiles(2.U).asUInt


        generalRegFiles(95.U) := (8.S(64.W)).asUInt

        CP := 33.U
      }

      is(33.U) {
        /*
        decl $ret: CP [@0, 2], $res: SP [@2, 2], buffer: MS[anvil.PrinterIndex.U, U8] @$0, index: anvil.PrinterIndex.U @$1, mask: anvil.PrinterIndex.U @$2, n: U64 @$3, digits: Z @$4
        $0 = ($91: MS[anvil.PrinterIndex.U, U8])
        $1 = ($92: anvil.PrinterIndex.U)
        $2 = ($93: anvil.PrinterIndex.U)
        $3 = ($94: U64)
        $4 = ($95: Z)
        goto .77
        */



        generalRegFiles(0.U) := generalRegFiles(91.U)


        generalRegFiles(1.U) := generalRegFiles(92.U)


        generalRegFiles(2.U) := generalRegFiles(93.U)


        generalRegFiles(3.U) := generalRegFiles(94.U)


        generalRegFiles(4.U) := (generalRegFiles(95.U).asSInt).asUInt

        CP := 77.U
      }

      is(34.U) {
        /*
        $3 = **(SP + (2: SP)) [unsigned, U64, 8]  // $3 = $res
        undecl digits: Z @$4, n: U64 @$3, mask: anvil.PrinterIndex.U @$2, index: anvil.PrinterIndex.U @$1, buffer: MS[anvil.PrinterIndex.U, U8] @$0, $res: SP [@2, 2], $ret: CP [@0, 2]
        goto .35
        */


        val __tmp_2290 = (Cat(
          arrayRegFiles((SP + 2.U(16.W)) + 1.U),
          arrayRegFiles((SP + 2.U(16.W)) + 0.U)
        )).asUInt
        generalRegFiles(3.U) := Cat(
          arrayRegFiles(__tmp_2290 + 7.U),
          arrayRegFiles(__tmp_2290 + 6.U),
          arrayRegFiles(__tmp_2290 + 5.U),
          arrayRegFiles(__tmp_2290 + 4.U),
          arrayRegFiles(__tmp_2290 + 3.U),
          arrayRegFiles(__tmp_2290 + 2.U),
          arrayRegFiles(__tmp_2290 + 1.U),
          arrayRegFiles(__tmp_2290 + 0.U)
        ).asUInt

        CP := 35.U
      }

      is(35.U) {
        /*
        SP = SP - 14
        goto .36
        */


        SP := SP - 14.U

        CP := 36.U
      }

      is(36.U) {
        /*
        DP = DP + (($3: U64) as DP)
        goto .37
        */


        DP := DP + generalRegFiles(3.U).asUInt

        CP := 37.U
      }

      is(37.U) {
        /*
        *(((8: SP) + (12: SP)) + ((DP & (31: DP)) as SP)) = (10: U8) [unsigned, U8, 1]  // $display((DP & (31: DP))) = (10: U8)
        goto .38
        */


        val __tmp_2291 = ((8.U(16.W) + 12.U(16.W)) + (DP & 31.U(64.W)).asUInt)
        val __tmp_2292 = (10.U(8.W)).asUInt
        arrayRegFiles(__tmp_2291 + 0.U) := __tmp_2292(7, 0)

        CP := 38.U
      }

      is(38.U) {
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

      is(39.U) {
        /*
        alloc factorial$res@[17,11].FD6E9E13: U32 [@2, 4]
        goto .40
        */


        CP := 40.U
      }

      is(40.U) {
        /*
        SP = SP + 14
        goto .41
        */


        SP := SP + 14.U

        CP := 41.U
      }

      is(41.U) {
        /*
        *SP = (43: CP) [unsigned, CP, 2]  // $ret@0 = 1350
        *(SP + (2: SP)) = (SP - (12: SP)) [unsigned, SP, 2]  // $res@2 = -12
        $10 = (3: U32)
        goto .42
        */


        val __tmp_2293 = SP
        val __tmp_2294 = (43.U(16.W)).asUInt
        arrayRegFiles(__tmp_2293 + 0.U) := __tmp_2294(7, 0)
        arrayRegFiles(__tmp_2293 + 1.U) := __tmp_2294(15, 8)

        val __tmp_2295 = (SP + 2.U(16.W))
        val __tmp_2296 = ((SP - 12.U(16.W))).asUInt
        arrayRegFiles(__tmp_2295 + 0.U) := __tmp_2296(7, 0)
        arrayRegFiles(__tmp_2295 + 1.U) := __tmp_2296(15, 8)


        generalRegFiles(10.U) := 3.U(32.W)

        CP := 42.U
      }

      is(42.U) {
        /*
        decl $ret: CP [@0, 2], $res: SP [@2, 6], n: U32 @$0
        $0 = ($10: U32)
        goto .69
        */



        generalRegFiles(0.U) := generalRegFiles(10.U)

        CP := 69.U
      }

      is(43.U) {
        /*
        $2 = **(SP + (2: SP)) [unsigned, U32, 4]  // $2 = $res
        undecl n: U32 @$0, $res: SP [@2, 6], $ret: CP [@0, 2]
        goto .44
        */


        val __tmp_2297 = (Cat(
          arrayRegFiles((SP + 2.U(16.W)) + 1.U),
          arrayRegFiles((SP + 2.U(16.W)) + 0.U)
        )).asUInt
        generalRegFiles(2.U) := Cat(
          arrayRegFiles(__tmp_2297 + 3.U),
          arrayRegFiles(__tmp_2297 + 2.U),
          arrayRegFiles(__tmp_2297 + 1.U),
          arrayRegFiles(__tmp_2297 + 0.U)
        ).asUInt

        CP := 44.U
      }

      is(44.U) {
        /*
        SP = SP - 14
        goto .45
        */


        SP := SP - 14.U

        CP := 45.U
      }

      is(45.U) {
        /*
        alloc printU64Hex$res@[17,11].FD6E9E13: U64 [@6, 8]
        goto .46
        */


        CP := 46.U
      }

      is(46.U) {
        /*
        SP = SP + 14
        goto .47
        */


        SP := SP + 14.U

        CP := 47.U
      }

      is(47.U) {
        /*
        *SP = (49: CP) [unsigned, CP, 2]  // $ret@0 = 1352
        *(SP + (2: SP)) = (SP - (8: SP)) [unsigned, SP, 2]  // $res@2 = -8
        $91 = (8: SP)
        $92 = DP
        $93 = (31: anvil.PrinterIndex.U)
        $94 = (($2: U32) as U64)
        $95 = (8: Z)
        goto .48
        */


        val __tmp_2298 = SP
        val __tmp_2299 = (49.U(16.W)).asUInt
        arrayRegFiles(__tmp_2298 + 0.U) := __tmp_2299(7, 0)
        arrayRegFiles(__tmp_2298 + 1.U) := __tmp_2299(15, 8)

        val __tmp_2300 = (SP + 2.U(16.W))
        val __tmp_2301 = ((SP - 8.U(16.W))).asUInt
        arrayRegFiles(__tmp_2300 + 0.U) := __tmp_2301(7, 0)
        arrayRegFiles(__tmp_2300 + 1.U) := __tmp_2301(15, 8)


        generalRegFiles(91.U) := 8.U(16.W)


        generalRegFiles(92.U) := DP


        generalRegFiles(93.U) := 31.U(64.W)


        generalRegFiles(94.U) := generalRegFiles(2.U).asUInt


        generalRegFiles(95.U) := (8.S(64.W)).asUInt

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
        goto .77
        */



        generalRegFiles(0.U) := generalRegFiles(91.U)


        generalRegFiles(1.U) := generalRegFiles(92.U)


        generalRegFiles(2.U) := generalRegFiles(93.U)


        generalRegFiles(3.U) := generalRegFiles(94.U)


        generalRegFiles(4.U) := (generalRegFiles(95.U).asSInt).asUInt

        CP := 77.U
      }

      is(49.U) {
        /*
        $3 = **(SP + (2: SP)) [unsigned, U64, 8]  // $3 = $res
        undecl digits: Z @$4, n: U64 @$3, mask: anvil.PrinterIndex.U @$2, index: anvil.PrinterIndex.U @$1, buffer: MS[anvil.PrinterIndex.U, U8] @$0, $res: SP [@2, 2], $ret: CP [@0, 2]
        goto .50
        */


        val __tmp_2302 = (Cat(
          arrayRegFiles((SP + 2.U(16.W)) + 1.U),
          arrayRegFiles((SP + 2.U(16.W)) + 0.U)
        )).asUInt
        generalRegFiles(3.U) := Cat(
          arrayRegFiles(__tmp_2302 + 7.U),
          arrayRegFiles(__tmp_2302 + 6.U),
          arrayRegFiles(__tmp_2302 + 5.U),
          arrayRegFiles(__tmp_2302 + 4.U),
          arrayRegFiles(__tmp_2302 + 3.U),
          arrayRegFiles(__tmp_2302 + 2.U),
          arrayRegFiles(__tmp_2302 + 1.U),
          arrayRegFiles(__tmp_2302 + 0.U)
        ).asUInt

        CP := 50.U
      }

      is(50.U) {
        /*
        SP = SP - 14
        goto .51
        */


        SP := SP - 14.U

        CP := 51.U
      }

      is(51.U) {
        /*
        DP = DP + (($3: U64) as DP)
        goto .52
        */


        DP := DP + generalRegFiles(3.U).asUInt

        CP := 52.U
      }

      is(52.U) {
        /*
        *(((8: SP) + (12: SP)) + ((DP & (31: DP)) as SP)) = (10: U8) [unsigned, U8, 1]  // $display((DP & (31: DP))) = (10: U8)
        goto .53
        */


        val __tmp_2303 = ((8.U(16.W) + 12.U(16.W)) + (DP & 31.U(64.W)).asUInt)
        val __tmp_2304 = (10.U(8.W)).asUInt
        arrayRegFiles(__tmp_2303 + 0.U) := __tmp_2304(7, 0)

        CP := 53.U
      }

      is(53.U) {
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

      is(54.U) {
        /*
        alloc factorial$res@[21,11].F5F6633A: U32 [@2, 4]
        goto .55
        */


        CP := 55.U
      }

      is(55.U) {
        /*
        SP = SP + 14
        goto .56
        */


        SP := SP + 14.U

        CP := 56.U
      }

      is(56.U) {
        /*
        *SP = (58: CP) [unsigned, CP, 2]  // $ret@0 = 1354
        *(SP + (2: SP)) = (SP - (12: SP)) [unsigned, SP, 2]  // $res@2 = -12
        $10 = (4: U32)
        goto .57
        */


        val __tmp_2305 = SP
        val __tmp_2306 = (58.U(16.W)).asUInt
        arrayRegFiles(__tmp_2305 + 0.U) := __tmp_2306(7, 0)
        arrayRegFiles(__tmp_2305 + 1.U) := __tmp_2306(15, 8)

        val __tmp_2307 = (SP + 2.U(16.W))
        val __tmp_2308 = ((SP - 12.U(16.W))).asUInt
        arrayRegFiles(__tmp_2307 + 0.U) := __tmp_2308(7, 0)
        arrayRegFiles(__tmp_2307 + 1.U) := __tmp_2308(15, 8)


        generalRegFiles(10.U) := 4.U(32.W)

        CP := 57.U
      }

      is(57.U) {
        /*
        decl $ret: CP [@0, 2], $res: SP [@2, 6], n: U32 @$0
        $0 = ($10: U32)
        goto .69
        */



        generalRegFiles(0.U) := generalRegFiles(10.U)

        CP := 69.U
      }

      is(58.U) {
        /*
        $2 = **(SP + (2: SP)) [unsigned, U32, 4]  // $2 = $res
        undecl n: U32 @$0, $res: SP [@2, 6], $ret: CP [@0, 2]
        goto .59
        */


        val __tmp_2309 = (Cat(
          arrayRegFiles((SP + 2.U(16.W)) + 1.U),
          arrayRegFiles((SP + 2.U(16.W)) + 0.U)
        )).asUInt
        generalRegFiles(2.U) := Cat(
          arrayRegFiles(__tmp_2309 + 3.U),
          arrayRegFiles(__tmp_2309 + 2.U),
          arrayRegFiles(__tmp_2309 + 1.U),
          arrayRegFiles(__tmp_2309 + 0.U)
        ).asUInt

        CP := 59.U
      }

      is(59.U) {
        /*
        SP = SP - 14
        goto .60
        */


        SP := SP - 14.U

        CP := 60.U
      }

      is(60.U) {
        /*
        alloc printU64Hex$res@[21,11].F5F6633A: U64 [@6, 8]
        goto .61
        */


        CP := 61.U
      }

      is(61.U) {
        /*
        SP = SP + 14
        goto .62
        */


        SP := SP + 14.U

        CP := 62.U
      }

      is(62.U) {
        /*
        *SP = (64: CP) [unsigned, CP, 2]  // $ret@0 = 1356
        *(SP + (2: SP)) = (SP - (8: SP)) [unsigned, SP, 2]  // $res@2 = -8
        $91 = (8: SP)
        $92 = DP
        $93 = (31: anvil.PrinterIndex.U)
        $94 = (($2: U32) as U64)
        $95 = (8: Z)
        goto .63
        */


        val __tmp_2310 = SP
        val __tmp_2311 = (64.U(16.W)).asUInt
        arrayRegFiles(__tmp_2310 + 0.U) := __tmp_2311(7, 0)
        arrayRegFiles(__tmp_2310 + 1.U) := __tmp_2311(15, 8)

        val __tmp_2312 = (SP + 2.U(16.W))
        val __tmp_2313 = ((SP - 8.U(16.W))).asUInt
        arrayRegFiles(__tmp_2312 + 0.U) := __tmp_2313(7, 0)
        arrayRegFiles(__tmp_2312 + 1.U) := __tmp_2313(15, 8)


        generalRegFiles(91.U) := 8.U(16.W)


        generalRegFiles(92.U) := DP


        generalRegFiles(93.U) := 31.U(64.W)


        generalRegFiles(94.U) := generalRegFiles(2.U).asUInt


        generalRegFiles(95.U) := (8.S(64.W)).asUInt

        CP := 63.U
      }

      is(63.U) {
        /*
        decl $ret: CP [@0, 2], $res: SP [@2, 2], buffer: MS[anvil.PrinterIndex.U, U8] @$0, index: anvil.PrinterIndex.U @$1, mask: anvil.PrinterIndex.U @$2, n: U64 @$3, digits: Z @$4
        $0 = ($91: MS[anvil.PrinterIndex.U, U8])
        $1 = ($92: anvil.PrinterIndex.U)
        $2 = ($93: anvil.PrinterIndex.U)
        $3 = ($94: U64)
        $4 = ($95: Z)
        goto .77
        */



        generalRegFiles(0.U) := generalRegFiles(91.U)


        generalRegFiles(1.U) := generalRegFiles(92.U)


        generalRegFiles(2.U) := generalRegFiles(93.U)


        generalRegFiles(3.U) := generalRegFiles(94.U)


        generalRegFiles(4.U) := (generalRegFiles(95.U).asSInt).asUInt

        CP := 77.U
      }

      is(64.U) {
        /*
        $3 = **(SP + (2: SP)) [unsigned, U64, 8]  // $3 = $res
        undecl digits: Z @$4, n: U64 @$3, mask: anvil.PrinterIndex.U @$2, index: anvil.PrinterIndex.U @$1, buffer: MS[anvil.PrinterIndex.U, U8] @$0, $res: SP [@2, 2], $ret: CP [@0, 2]
        goto .65
        */


        val __tmp_2314 = (Cat(
          arrayRegFiles((SP + 2.U(16.W)) + 1.U),
          arrayRegFiles((SP + 2.U(16.W)) + 0.U)
        )).asUInt
        generalRegFiles(3.U) := Cat(
          arrayRegFiles(__tmp_2314 + 7.U),
          arrayRegFiles(__tmp_2314 + 6.U),
          arrayRegFiles(__tmp_2314 + 5.U),
          arrayRegFiles(__tmp_2314 + 4.U),
          arrayRegFiles(__tmp_2314 + 3.U),
          arrayRegFiles(__tmp_2314 + 2.U),
          arrayRegFiles(__tmp_2314 + 1.U),
          arrayRegFiles(__tmp_2314 + 0.U)
        ).asUInt

        CP := 65.U
      }

      is(65.U) {
        /*
        SP = SP - 14
        goto .66
        */


        SP := SP - 14.U

        CP := 66.U
      }

      is(66.U) {
        /*
        DP = DP + (($3: U64) as DP)
        goto .67
        */


        DP := DP + generalRegFiles(3.U).asUInt

        CP := 67.U
      }

      is(67.U) {
        /*
        *(((8: SP) + (12: SP)) + ((DP & (31: DP)) as SP)) = (10: U8) [unsigned, U8, 1]  // $display((DP & (31: DP))) = (10: U8)
        goto .68
        */


        val __tmp_2315 = ((8.U(16.W) + 12.U(16.W)) + (DP & 31.U(64.W)).asUInt)
        val __tmp_2316 = (10.U(8.W)).asUInt
        arrayRegFiles(__tmp_2315 + 0.U) := __tmp_2316(7, 0)

        CP := 68.U
      }

      is(68.U) {
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

      is(69.U) {
        /*
        $2 = (($0: U32) <= (1: U32))
        goto .70
        */



        generalRegFiles(2.U) := (generalRegFiles(0.U) <= 1.U(32.W)).asUInt

        CP := 70.U
      }

      is(70.U) {
        /*
        if ($2: B) goto .71 else goto .72
        */


        CP := Mux((generalRegFiles(2.U).asUInt) === 1.U, 71.U, 72.U)
      }

      is(71.U) {
        /*
        **(SP + (2: SP)) = (1: U32) [unsigned, U32, 4]  // $res = (1: U32)
        goto $ret@0
        */


        val __tmp_2317 = Cat(
          arrayRegFiles((SP + 2.U(16.W)) + 1.U),
          arrayRegFiles((SP + 2.U(16.W)) + 0.U)
        )
        val __tmp_2318 = (1.U(32.W)).asUInt
        arrayRegFiles(__tmp_2317 + 0.U) := __tmp_2318(7, 0)
        arrayRegFiles(__tmp_2317 + 1.U) := __tmp_2318(15, 8)
        arrayRegFiles(__tmp_2317 + 2.U) := __tmp_2318(23, 16)
        arrayRegFiles(__tmp_2317 + 3.U) := __tmp_2318(31, 24)

        CP := Cat(
          arrayRegFiles(SP + 0.U + 1.U),
          arrayRegFiles(SP + 0.U + 0.U)
        )

      }

      is(72.U) {
        /*
        $3 = (($0: U32) - (1: U32))
        alloc factorial$res@[9,14].A651BB8C: U32 [@8, 4]
        goto .73
        */



        generalRegFiles(3.U) := (generalRegFiles(0.U) - 1.U(32.W))

        CP := 73.U
      }

      is(73.U) {
        /*
        SP = SP + 16
        goto .169
        */


        SP := SP + 16.U

        CP := 169.U
      }

      is(75.U) {
        /*
        $5 = (($0: U32) * ($4: U32))
        goto .76
        */



        generalRegFiles(5.U) := (generalRegFiles(0.U) * generalRegFiles(4.U))

        CP := 76.U
      }

      is(76.U) {
        /*
        **(SP + (2: SP)) = ($5: U32) [unsigned, U32, 4]  // $res = ($5: U32)
        goto $ret@0
        */


        val __tmp_2319 = Cat(
          arrayRegFiles((SP + 2.U(16.W)) + 1.U),
          arrayRegFiles((SP + 2.U(16.W)) + 0.U)
        )
        val __tmp_2320 = (generalRegFiles(5.U)).asUInt
        arrayRegFiles(__tmp_2319 + 0.U) := __tmp_2320(7, 0)
        arrayRegFiles(__tmp_2319 + 1.U) := __tmp_2320(15, 8)
        arrayRegFiles(__tmp_2319 + 2.U) := __tmp_2320(23, 16)
        arrayRegFiles(__tmp_2319 + 3.U) := __tmp_2320(31, 24)

        CP := Cat(
          arrayRegFiles(SP + 0.U + 1.U),
          arrayRegFiles(SP + 0.U + 0.U)
        )

      }

      is(77.U) {
        /*
        decl buff: MS[anvil.PrinterIndex.I16, U8] [@4, 30]
        alloc $new@[245,16].6203A7B3: MS[anvil.PrinterIndex.I16, U8] [@34, 30]
        $10 = (SP + (34: SP))
        *(SP + (34: SP)) = (1541243932: U32) [unsigned, U32, 4]  // sha3 type signature of MS[anvil.PrinterIndex.I16, U8]: 0x5BDD841C
        *(SP + (38: SP)) = (16: Z) [signed, Z, 8]  // size of MS[anvil.PrinterIndex.I16, U8]((0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8), (0: U8))
        goto .78
        */



        generalRegFiles(10.U) := (SP + 34.U(16.W))

        val __tmp_2321 = (SP + 34.U(16.W))
        val __tmp_2322 = (1541243932.U(32.W)).asUInt
        arrayRegFiles(__tmp_2321 + 0.U) := __tmp_2322(7, 0)
        arrayRegFiles(__tmp_2321 + 1.U) := __tmp_2322(15, 8)
        arrayRegFiles(__tmp_2321 + 2.U) := __tmp_2322(23, 16)
        arrayRegFiles(__tmp_2321 + 3.U) := __tmp_2322(31, 24)

        val __tmp_2323 = (SP + 38.U(16.W))
        val __tmp_2324 = (16.S(64.W)).asUInt
        arrayRegFiles(__tmp_2323 + 0.U) := __tmp_2324(7, 0)
        arrayRegFiles(__tmp_2323 + 1.U) := __tmp_2324(15, 8)
        arrayRegFiles(__tmp_2323 + 2.U) := __tmp_2324(23, 16)
        arrayRegFiles(__tmp_2323 + 3.U) := __tmp_2324(31, 24)
        arrayRegFiles(__tmp_2323 + 4.U) := __tmp_2324(39, 32)
        arrayRegFiles(__tmp_2323 + 5.U) := __tmp_2324(47, 40)
        arrayRegFiles(__tmp_2323 + 6.U) := __tmp_2324(55, 48)
        arrayRegFiles(__tmp_2323 + 7.U) := __tmp_2324(63, 56)

        CP := 78.U
      }

      is(78.U) {
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
        goto .79
        */


        val __tmp_2325 = ((generalRegFiles(10.U) + 12.U(16.W)) + 0.S(8.W).asUInt)
        val __tmp_2326 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_2325 + 0.U) := __tmp_2326(7, 0)

        val __tmp_2327 = ((generalRegFiles(10.U) + 12.U(16.W)) + 1.S(8.W).asUInt)
        val __tmp_2328 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_2327 + 0.U) := __tmp_2328(7, 0)

        val __tmp_2329 = ((generalRegFiles(10.U) + 12.U(16.W)) + 2.S(8.W).asUInt)
        val __tmp_2330 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_2329 + 0.U) := __tmp_2330(7, 0)

        val __tmp_2331 = ((generalRegFiles(10.U) + 12.U(16.W)) + 3.S(8.W).asUInt)
        val __tmp_2332 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_2331 + 0.U) := __tmp_2332(7, 0)

        val __tmp_2333 = ((generalRegFiles(10.U) + 12.U(16.W)) + 4.S(8.W).asUInt)
        val __tmp_2334 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_2333 + 0.U) := __tmp_2334(7, 0)

        val __tmp_2335 = ((generalRegFiles(10.U) + 12.U(16.W)) + 5.S(8.W).asUInt)
        val __tmp_2336 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_2335 + 0.U) := __tmp_2336(7, 0)

        val __tmp_2337 = ((generalRegFiles(10.U) + 12.U(16.W)) + 6.S(8.W).asUInt)
        val __tmp_2338 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_2337 + 0.U) := __tmp_2338(7, 0)

        val __tmp_2339 = ((generalRegFiles(10.U) + 12.U(16.W)) + 7.S(8.W).asUInt)
        val __tmp_2340 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_2339 + 0.U) := __tmp_2340(7, 0)

        val __tmp_2341 = ((generalRegFiles(10.U) + 12.U(16.W)) + 8.S(8.W).asUInt)
        val __tmp_2342 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_2341 + 0.U) := __tmp_2342(7, 0)

        val __tmp_2343 = ((generalRegFiles(10.U) + 12.U(16.W)) + 9.S(8.W).asUInt)
        val __tmp_2344 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_2343 + 0.U) := __tmp_2344(7, 0)

        val __tmp_2345 = ((generalRegFiles(10.U) + 12.U(16.W)) + 10.S(8.W).asUInt)
        val __tmp_2346 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_2345 + 0.U) := __tmp_2346(7, 0)

        val __tmp_2347 = ((generalRegFiles(10.U) + 12.U(16.W)) + 11.S(8.W).asUInt)
        val __tmp_2348 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_2347 + 0.U) := __tmp_2348(7, 0)

        val __tmp_2349 = ((generalRegFiles(10.U) + 12.U(16.W)) + 12.S(8.W).asUInt)
        val __tmp_2350 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_2349 + 0.U) := __tmp_2350(7, 0)

        val __tmp_2351 = ((generalRegFiles(10.U) + 12.U(16.W)) + 13.S(8.W).asUInt)
        val __tmp_2352 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_2351 + 0.U) := __tmp_2352(7, 0)

        val __tmp_2353 = ((generalRegFiles(10.U) + 12.U(16.W)) + 14.S(8.W).asUInt)
        val __tmp_2354 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_2353 + 0.U) := __tmp_2354(7, 0)

        val __tmp_2355 = ((generalRegFiles(10.U) + 12.U(16.W)) + 15.S(8.W).asUInt)
        val __tmp_2356 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_2355 + 0.U) := __tmp_2356(7, 0)

        CP := 79.U
      }

      is(79.U) {
        /*
        (SP + (4: SP)) [MS[anvil.PrinterIndex.I16, U8], 30]  <-  ($10: MS[anvil.PrinterIndex.I16, U8]) [MS[anvil.PrinterIndex.I16, U8], ((*(($10: MS[anvil.PrinterIndex.I16, U8]) + (4: SP)) as SP) + (12: SP))]  // buff = ($10: MS[anvil.PrinterIndex.I16, U8])
        goto .80
        */


        val __tmp_2357 = (SP + 4.U(16.W))
        val __tmp_2358 = generalRegFiles(10.U)
        val __tmp_2359 = (Cat(
           arrayRegFiles((generalRegFiles(10.U) + 4.U(16.W)) + 7.U),
           arrayRegFiles((generalRegFiles(10.U) + 4.U(16.W)) + 6.U),
           arrayRegFiles((generalRegFiles(10.U) + 4.U(16.W)) + 5.U),
           arrayRegFiles((generalRegFiles(10.U) + 4.U(16.W)) + 4.U),
           arrayRegFiles((generalRegFiles(10.U) + 4.U(16.W)) + 3.U),
           arrayRegFiles((generalRegFiles(10.U) + 4.U(16.W)) + 2.U),
           arrayRegFiles((generalRegFiles(10.U) + 4.U(16.W)) + 1.U),
           arrayRegFiles((generalRegFiles(10.U) + 4.U(16.W)) + 0.U)
         ).asSInt.asUInt + 12.U(16.W))

        when(Idx < __tmp_2359) {
          arrayRegFiles(__tmp_2357 + Idx + 0.U) := arrayRegFiles(__tmp_2358 + Idx + 0.U)
          arrayRegFiles(__tmp_2357 + Idx + 1.U) := arrayRegFiles(__tmp_2358 + Idx + 1.U)
          arrayRegFiles(__tmp_2357 + Idx + 2.U) := arrayRegFiles(__tmp_2358 + Idx + 2.U)
          arrayRegFiles(__tmp_2357 + Idx + 3.U) := arrayRegFiles(__tmp_2358 + Idx + 3.U)
          arrayRegFiles(__tmp_2357 + Idx + 4.U) := arrayRegFiles(__tmp_2358 + Idx + 4.U)
          arrayRegFiles(__tmp_2357 + Idx + 5.U) := arrayRegFiles(__tmp_2358 + Idx + 5.U)
          arrayRegFiles(__tmp_2357 + Idx + 6.U) := arrayRegFiles(__tmp_2358 + Idx + 6.U)
          arrayRegFiles(__tmp_2357 + Idx + 7.U) := arrayRegFiles(__tmp_2358 + Idx + 7.U)
          Idx := Idx + 8.U
          LeftByteRounds := __tmp_2359 - Idx
        } .elsewhen(IdxLeftByteRounds < LeftByteRounds) {
          val __tmp_2360 = Idx - 8.U
          arrayRegFiles(__tmp_2357 + __tmp_2360 + IdxLeftByteRounds) := arrayRegFiles(__tmp_2358 + __tmp_2360 + IdxLeftByteRounds)
          IdxLeftByteRounds := IdxLeftByteRounds + 1.U
        } .otherwise {
          Idx := 0.U
          IdxLeftByteRounds := 0.U
          LeftByteRounds := 0.U
          CP := 80.U
        }


      }

      is(80.U) {
        /*
        unalloc $new@[245,16].6203A7B3: MS[anvil.PrinterIndex.I16, U8] [@34, 30]
        goto .81
        */


        CP := 81.U
      }

      is(81.U) {
        /*
        decl i: anvil.PrinterIndex.I16 @$5
        $5 = (0: anvil.PrinterIndex.I16)
        goto .82
        */



        generalRegFiles(5.U) := (0.S(8.W)).asUInt

        CP := 82.U
      }

      is(82.U) {
        /*
        decl m: U64 @$6
        $6 = ($3: U64)
        goto .83
        */



        generalRegFiles(6.U) := generalRegFiles(3.U)

        CP := 83.U
      }

      is(83.U) {
        /*
        decl d: Z @$7
        $7 = ($4: Z)
        goto .84
        */



        generalRegFiles(7.U) := (generalRegFiles(4.U).asSInt).asUInt

        CP := 84.U
      }

      is(84.U) {
        /*
        $10 = ($6: U64)
        goto .85
        */



        generalRegFiles(10.U) := generalRegFiles(6.U)

        CP := 85.U
      }

      is(85.U) {
        /*
        $11 = (($10: U64) > (0: U64))
        goto .86
        */



        generalRegFiles(11.U) := (generalRegFiles(10.U) > 0.U(64.W)).asUInt

        CP := 86.U
      }

      is(86.U) {
        /*
        $12 = ($7: Z)
        goto .87
        */



        generalRegFiles(12.U) := (generalRegFiles(7.U).asSInt).asUInt

        CP := 87.U
      }

      is(87.U) {
        /*
        $13 = (($12: Z) > (0: Z))
        goto .88
        */



        generalRegFiles(13.U) := (generalRegFiles(12.U).asSInt > 0.S(64.W)).asUInt

        CP := 88.U
      }

      is(88.U) {
        /*
        $14 = (($11: B) & ($13: B))
        goto .89
        */



        generalRegFiles(14.U) := (generalRegFiles(11.U) & generalRegFiles(13.U))

        CP := 89.U
      }

      is(89.U) {
        /*
        if ($14: B) goto .90 else goto .134
        */


        CP := Mux((generalRegFiles(14.U).asUInt) === 1.U, 90.U, 134.U)
      }

      is(90.U) {
        /*
        $10 = ($6: U64)
        goto .91
        */



        generalRegFiles(10.U) := generalRegFiles(6.U)

        CP := 91.U
      }

      is(91.U) {
        /*
        $11 = (($10: U64) & (15: U64))
        goto .92
        */



        generalRegFiles(11.U) := (generalRegFiles(10.U) & 15.U(64.W))

        CP := 92.U
      }

      is(92.U) {
        /*
        switch (($11: U64))
          (0: U64): goto 93
          (1: U64): goto 95
          (2: U64): goto 97
          (3: U64): goto 99
          (4: U64): goto 101
          (5: U64): goto 103
          (6: U64): goto 105
          (7: U64): goto 107
          (8: U64): goto 109
          (9: U64): goto 111
          (10: U64): goto 113
          (11: U64): goto 115
          (12: U64): goto 117
          (13: U64): goto 119
          (14: U64): goto 121
          (15: U64): goto 123

        */


        val __tmp_2361 = generalRegFiles(11.U)

        switch(__tmp_2361) {

          is(0.U(64.W)) {
            CP := 93.U
          }


          is(1.U(64.W)) {
            CP := 95.U
          }


          is(2.U(64.W)) {
            CP := 97.U
          }


          is(3.U(64.W)) {
            CP := 99.U
          }


          is(4.U(64.W)) {
            CP := 101.U
          }


          is(5.U(64.W)) {
            CP := 103.U
          }


          is(6.U(64.W)) {
            CP := 105.U
          }


          is(7.U(64.W)) {
            CP := 107.U
          }


          is(8.U(64.W)) {
            CP := 109.U
          }


          is(9.U(64.W)) {
            CP := 111.U
          }


          is(10.U(64.W)) {
            CP := 113.U
          }


          is(11.U(64.W)) {
            CP := 115.U
          }


          is(12.U(64.W)) {
            CP := 117.U
          }


          is(13.U(64.W)) {
            CP := 119.U
          }


          is(14.U(64.W)) {
            CP := 121.U
          }


          is(15.U(64.W)) {
            CP := 123.U
          }

        }

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
        *((($10: MS[anvil.PrinterIndex.I16, U8]) + (12: SP)) + (($11: anvil.PrinterIndex.I16) as SP)) = (48: U8) [unsigned, U8, 1]  // ($10: MS[anvil.PrinterIndex.I16, U8])(($11: anvil.PrinterIndex.I16)) = (48: U8)
        goto .125
        */


        val __tmp_2362 = ((generalRegFiles(10.U) + 12.U(16.W)) + generalRegFiles(11.U).asSInt.asUInt)
        val __tmp_2363 = (48.U(8.W)).asUInt
        arrayRegFiles(__tmp_2362 + 0.U) := __tmp_2363(7, 0)

        CP := 125.U
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
        *((($10: MS[anvil.PrinterIndex.I16, U8]) + (12: SP)) + (($11: anvil.PrinterIndex.I16) as SP)) = (49: U8) [unsigned, U8, 1]  // ($10: MS[anvil.PrinterIndex.I16, U8])(($11: anvil.PrinterIndex.I16)) = (49: U8)
        goto .125
        */


        val __tmp_2364 = ((generalRegFiles(10.U) + 12.U(16.W)) + generalRegFiles(11.U).asSInt.asUInt)
        val __tmp_2365 = (49.U(8.W)).asUInt
        arrayRegFiles(__tmp_2364 + 0.U) := __tmp_2365(7, 0)

        CP := 125.U
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
        *((($10: MS[anvil.PrinterIndex.I16, U8]) + (12: SP)) + (($11: anvil.PrinterIndex.I16) as SP)) = (50: U8) [unsigned, U8, 1]  // ($10: MS[anvil.PrinterIndex.I16, U8])(($11: anvil.PrinterIndex.I16)) = (50: U8)
        goto .125
        */


        val __tmp_2366 = ((generalRegFiles(10.U) + 12.U(16.W)) + generalRegFiles(11.U).asSInt.asUInt)
        val __tmp_2367 = (50.U(8.W)).asUInt
        arrayRegFiles(__tmp_2366 + 0.U) := __tmp_2367(7, 0)

        CP := 125.U
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
        *((($10: MS[anvil.PrinterIndex.I16, U8]) + (12: SP)) + (($11: anvil.PrinterIndex.I16) as SP)) = (51: U8) [unsigned, U8, 1]  // ($10: MS[anvil.PrinterIndex.I16, U8])(($11: anvil.PrinterIndex.I16)) = (51: U8)
        goto .125
        */


        val __tmp_2368 = ((generalRegFiles(10.U) + 12.U(16.W)) + generalRegFiles(11.U).asSInt.asUInt)
        val __tmp_2369 = (51.U(8.W)).asUInt
        arrayRegFiles(__tmp_2368 + 0.U) := __tmp_2369(7, 0)

        CP := 125.U
      }

      is(101.U) {
        /*
        $10 = (SP + (4: SP))
        $11 = ($5: anvil.PrinterIndex.I16)
        goto .102
        */



        generalRegFiles(10.U) := (SP + 4.U(16.W))


        generalRegFiles(11.U) := (generalRegFiles(5.U).asSInt).asUInt

        CP := 102.U
      }

      is(102.U) {
        /*
        *((($10: MS[anvil.PrinterIndex.I16, U8]) + (12: SP)) + (($11: anvil.PrinterIndex.I16) as SP)) = (52: U8) [unsigned, U8, 1]  // ($10: MS[anvil.PrinterIndex.I16, U8])(($11: anvil.PrinterIndex.I16)) = (52: U8)
        goto .125
        */


        val __tmp_2370 = ((generalRegFiles(10.U) + 12.U(16.W)) + generalRegFiles(11.U).asSInt.asUInt)
        val __tmp_2371 = (52.U(8.W)).asUInt
        arrayRegFiles(__tmp_2370 + 0.U) := __tmp_2371(7, 0)

        CP := 125.U
      }

      is(103.U) {
        /*
        $10 = (SP + (4: SP))
        $11 = ($5: anvil.PrinterIndex.I16)
        goto .104
        */



        generalRegFiles(10.U) := (SP + 4.U(16.W))


        generalRegFiles(11.U) := (generalRegFiles(5.U).asSInt).asUInt

        CP := 104.U
      }

      is(104.U) {
        /*
        *((($10: MS[anvil.PrinterIndex.I16, U8]) + (12: SP)) + (($11: anvil.PrinterIndex.I16) as SP)) = (53: U8) [unsigned, U8, 1]  // ($10: MS[anvil.PrinterIndex.I16, U8])(($11: anvil.PrinterIndex.I16)) = (53: U8)
        goto .125
        */


        val __tmp_2372 = ((generalRegFiles(10.U) + 12.U(16.W)) + generalRegFiles(11.U).asSInt.asUInt)
        val __tmp_2373 = (53.U(8.W)).asUInt
        arrayRegFiles(__tmp_2372 + 0.U) := __tmp_2373(7, 0)

        CP := 125.U
      }

      is(105.U) {
        /*
        $10 = (SP + (4: SP))
        $11 = ($5: anvil.PrinterIndex.I16)
        goto .106
        */



        generalRegFiles(10.U) := (SP + 4.U(16.W))


        generalRegFiles(11.U) := (generalRegFiles(5.U).asSInt).asUInt

        CP := 106.U
      }

      is(106.U) {
        /*
        *((($10: MS[anvil.PrinterIndex.I16, U8]) + (12: SP)) + (($11: anvil.PrinterIndex.I16) as SP)) = (54: U8) [unsigned, U8, 1]  // ($10: MS[anvil.PrinterIndex.I16, U8])(($11: anvil.PrinterIndex.I16)) = (54: U8)
        goto .125
        */


        val __tmp_2374 = ((generalRegFiles(10.U) + 12.U(16.W)) + generalRegFiles(11.U).asSInt.asUInt)
        val __tmp_2375 = (54.U(8.W)).asUInt
        arrayRegFiles(__tmp_2374 + 0.U) := __tmp_2375(7, 0)

        CP := 125.U
      }

      is(107.U) {
        /*
        $10 = (SP + (4: SP))
        $11 = ($5: anvil.PrinterIndex.I16)
        goto .108
        */



        generalRegFiles(10.U) := (SP + 4.U(16.W))


        generalRegFiles(11.U) := (generalRegFiles(5.U).asSInt).asUInt

        CP := 108.U
      }

      is(108.U) {
        /*
        *((($10: MS[anvil.PrinterIndex.I16, U8]) + (12: SP)) + (($11: anvil.PrinterIndex.I16) as SP)) = (55: U8) [unsigned, U8, 1]  // ($10: MS[anvil.PrinterIndex.I16, U8])(($11: anvil.PrinterIndex.I16)) = (55: U8)
        goto .125
        */


        val __tmp_2376 = ((generalRegFiles(10.U) + 12.U(16.W)) + generalRegFiles(11.U).asSInt.asUInt)
        val __tmp_2377 = (55.U(8.W)).asUInt
        arrayRegFiles(__tmp_2376 + 0.U) := __tmp_2377(7, 0)

        CP := 125.U
      }

      is(109.U) {
        /*
        $10 = (SP + (4: SP))
        $11 = ($5: anvil.PrinterIndex.I16)
        goto .110
        */



        generalRegFiles(10.U) := (SP + 4.U(16.W))


        generalRegFiles(11.U) := (generalRegFiles(5.U).asSInt).asUInt

        CP := 110.U
      }

      is(110.U) {
        /*
        *((($10: MS[anvil.PrinterIndex.I16, U8]) + (12: SP)) + (($11: anvil.PrinterIndex.I16) as SP)) = (56: U8) [unsigned, U8, 1]  // ($10: MS[anvil.PrinterIndex.I16, U8])(($11: anvil.PrinterIndex.I16)) = (56: U8)
        goto .125
        */


        val __tmp_2378 = ((generalRegFiles(10.U) + 12.U(16.W)) + generalRegFiles(11.U).asSInt.asUInt)
        val __tmp_2379 = (56.U(8.W)).asUInt
        arrayRegFiles(__tmp_2378 + 0.U) := __tmp_2379(7, 0)

        CP := 125.U
      }

      is(111.U) {
        /*
        $10 = (SP + (4: SP))
        $11 = ($5: anvil.PrinterIndex.I16)
        goto .112
        */



        generalRegFiles(10.U) := (SP + 4.U(16.W))


        generalRegFiles(11.U) := (generalRegFiles(5.U).asSInt).asUInt

        CP := 112.U
      }

      is(112.U) {
        /*
        *((($10: MS[anvil.PrinterIndex.I16, U8]) + (12: SP)) + (($11: anvil.PrinterIndex.I16) as SP)) = (57: U8) [unsigned, U8, 1]  // ($10: MS[anvil.PrinterIndex.I16, U8])(($11: anvil.PrinterIndex.I16)) = (57: U8)
        goto .125
        */


        val __tmp_2380 = ((generalRegFiles(10.U) + 12.U(16.W)) + generalRegFiles(11.U).asSInt.asUInt)
        val __tmp_2381 = (57.U(8.W)).asUInt
        arrayRegFiles(__tmp_2380 + 0.U) := __tmp_2381(7, 0)

        CP := 125.U
      }

      is(113.U) {
        /*
        $10 = (SP + (4: SP))
        $11 = ($5: anvil.PrinterIndex.I16)
        goto .114
        */



        generalRegFiles(10.U) := (SP + 4.U(16.W))


        generalRegFiles(11.U) := (generalRegFiles(5.U).asSInt).asUInt

        CP := 114.U
      }

      is(114.U) {
        /*
        *((($10: MS[anvil.PrinterIndex.I16, U8]) + (12: SP)) + (($11: anvil.PrinterIndex.I16) as SP)) = (65: U8) [unsigned, U8, 1]  // ($10: MS[anvil.PrinterIndex.I16, U8])(($11: anvil.PrinterIndex.I16)) = (65: U8)
        goto .125
        */


        val __tmp_2382 = ((generalRegFiles(10.U) + 12.U(16.W)) + generalRegFiles(11.U).asSInt.asUInt)
        val __tmp_2383 = (65.U(8.W)).asUInt
        arrayRegFiles(__tmp_2382 + 0.U) := __tmp_2383(7, 0)

        CP := 125.U
      }

      is(115.U) {
        /*
        $10 = (SP + (4: SP))
        $11 = ($5: anvil.PrinterIndex.I16)
        goto .116
        */



        generalRegFiles(10.U) := (SP + 4.U(16.W))


        generalRegFiles(11.U) := (generalRegFiles(5.U).asSInt).asUInt

        CP := 116.U
      }

      is(116.U) {
        /*
        *((($10: MS[anvil.PrinterIndex.I16, U8]) + (12: SP)) + (($11: anvil.PrinterIndex.I16) as SP)) = (66: U8) [unsigned, U8, 1]  // ($10: MS[anvil.PrinterIndex.I16, U8])(($11: anvil.PrinterIndex.I16)) = (66: U8)
        goto .125
        */


        val __tmp_2384 = ((generalRegFiles(10.U) + 12.U(16.W)) + generalRegFiles(11.U).asSInt.asUInt)
        val __tmp_2385 = (66.U(8.W)).asUInt
        arrayRegFiles(__tmp_2384 + 0.U) := __tmp_2385(7, 0)

        CP := 125.U
      }

      is(117.U) {
        /*
        $10 = (SP + (4: SP))
        $11 = ($5: anvil.PrinterIndex.I16)
        goto .118
        */



        generalRegFiles(10.U) := (SP + 4.U(16.W))


        generalRegFiles(11.U) := (generalRegFiles(5.U).asSInt).asUInt

        CP := 118.U
      }

      is(118.U) {
        /*
        *((($10: MS[anvil.PrinterIndex.I16, U8]) + (12: SP)) + (($11: anvil.PrinterIndex.I16) as SP)) = (67: U8) [unsigned, U8, 1]  // ($10: MS[anvil.PrinterIndex.I16, U8])(($11: anvil.PrinterIndex.I16)) = (67: U8)
        goto .125
        */


        val __tmp_2386 = ((generalRegFiles(10.U) + 12.U(16.W)) + generalRegFiles(11.U).asSInt.asUInt)
        val __tmp_2387 = (67.U(8.W)).asUInt
        arrayRegFiles(__tmp_2386 + 0.U) := __tmp_2387(7, 0)

        CP := 125.U
      }

      is(119.U) {
        /*
        $10 = (SP + (4: SP))
        $11 = ($5: anvil.PrinterIndex.I16)
        goto .120
        */



        generalRegFiles(10.U) := (SP + 4.U(16.W))


        generalRegFiles(11.U) := (generalRegFiles(5.U).asSInt).asUInt

        CP := 120.U
      }

      is(120.U) {
        /*
        *((($10: MS[anvil.PrinterIndex.I16, U8]) + (12: SP)) + (($11: anvil.PrinterIndex.I16) as SP)) = (68: U8) [unsigned, U8, 1]  // ($10: MS[anvil.PrinterIndex.I16, U8])(($11: anvil.PrinterIndex.I16)) = (68: U8)
        goto .125
        */


        val __tmp_2388 = ((generalRegFiles(10.U) + 12.U(16.W)) + generalRegFiles(11.U).asSInt.asUInt)
        val __tmp_2389 = (68.U(8.W)).asUInt
        arrayRegFiles(__tmp_2388 + 0.U) := __tmp_2389(7, 0)

        CP := 125.U
      }

      is(121.U) {
        /*
        $10 = (SP + (4: SP))
        $11 = ($5: anvil.PrinterIndex.I16)
        goto .122
        */



        generalRegFiles(10.U) := (SP + 4.U(16.W))


        generalRegFiles(11.U) := (generalRegFiles(5.U).asSInt).asUInt

        CP := 122.U
      }

      is(122.U) {
        /*
        *((($10: MS[anvil.PrinterIndex.I16, U8]) + (12: SP)) + (($11: anvil.PrinterIndex.I16) as SP)) = (69: U8) [unsigned, U8, 1]  // ($10: MS[anvil.PrinterIndex.I16, U8])(($11: anvil.PrinterIndex.I16)) = (69: U8)
        goto .125
        */


        val __tmp_2390 = ((generalRegFiles(10.U) + 12.U(16.W)) + generalRegFiles(11.U).asSInt.asUInt)
        val __tmp_2391 = (69.U(8.W)).asUInt
        arrayRegFiles(__tmp_2390 + 0.U) := __tmp_2391(7, 0)

        CP := 125.U
      }

      is(123.U) {
        /*
        $10 = (SP + (4: SP))
        $11 = ($5: anvil.PrinterIndex.I16)
        goto .124
        */



        generalRegFiles(10.U) := (SP + 4.U(16.W))


        generalRegFiles(11.U) := (generalRegFiles(5.U).asSInt).asUInt

        CP := 124.U
      }

      is(124.U) {
        /*
        *((($10: MS[anvil.PrinterIndex.I16, U8]) + (12: SP)) + (($11: anvil.PrinterIndex.I16) as SP)) = (70: U8) [unsigned, U8, 1]  // ($10: MS[anvil.PrinterIndex.I16, U8])(($11: anvil.PrinterIndex.I16)) = (70: U8)
        goto .125
        */


        val __tmp_2392 = ((generalRegFiles(10.U) + 12.U(16.W)) + generalRegFiles(11.U).asSInt.asUInt)
        val __tmp_2393 = (70.U(8.W)).asUInt
        arrayRegFiles(__tmp_2392 + 0.U) := __tmp_2393(7, 0)

        CP := 125.U
      }

      is(125.U) {
        /*
        $10 = ($6: U64)
        goto .126
        */



        generalRegFiles(10.U) := generalRegFiles(6.U)

        CP := 126.U
      }

      is(126.U) {
        /*
        $11 = (($10: U64) >>> (4: U64))
        goto .127
        */



        generalRegFiles(11.U) := (((generalRegFiles(10.U)) >> 4.U(64.W)(4,0)))

        CP := 127.U
      }

      is(127.U) {
        /*
        $6 = ($11: U64)
        goto .128
        */



        generalRegFiles(6.U) := generalRegFiles(11.U)

        CP := 128.U
      }

      is(128.U) {
        /*
        $10 = ($5: anvil.PrinterIndex.I16)
        goto .129
        */



        generalRegFiles(10.U) := (generalRegFiles(5.U).asSInt).asUInt

        CP := 129.U
      }

      is(129.U) {
        /*
        $11 = (($10: anvil.PrinterIndex.I16) + (1: anvil.PrinterIndex.I16))
        goto .130
        */



        generalRegFiles(11.U) := ((generalRegFiles(10.U).asSInt + 1.S(8.W))).asUInt

        CP := 130.U
      }

      is(130.U) {
        /*
        $5 = ($11: anvil.PrinterIndex.I16)
        goto .131
        */



        generalRegFiles(5.U) := (generalRegFiles(11.U).asSInt).asUInt

        CP := 131.U
      }

      is(131.U) {
        /*
        $10 = ($7: Z)
        goto .132
        */



        generalRegFiles(10.U) := (generalRegFiles(7.U).asSInt).asUInt

        CP := 132.U
      }

      is(132.U) {
        /*
        $11 = (($10: Z) - (1: Z))
        goto .133
        */



        generalRegFiles(11.U) := ((generalRegFiles(10.U).asSInt - 1.S(64.W))).asUInt

        CP := 133.U
      }

      is(133.U) {
        /*
        $7 = ($11: Z)
        goto .84
        */



        generalRegFiles(7.U) := (generalRegFiles(11.U).asSInt).asUInt

        CP := 84.U
      }

      is(134.U) {
        /*
        decl idx: anvil.PrinterIndex.U @$8
        $10 = ($1: anvil.PrinterIndex.U)
        goto .135
        */



        generalRegFiles(10.U) := generalRegFiles(1.U)

        CP := 135.U
      }

      is(135.U) {
        /*
        $8 = ($10: anvil.PrinterIndex.U)
        goto .136
        */



        generalRegFiles(8.U) := generalRegFiles(10.U)

        CP := 136.U
      }

      is(136.U) {
        /*
        $10 = ($7: Z)
        goto .137
        */



        generalRegFiles(10.U) := (generalRegFiles(7.U).asSInt).asUInt

        CP := 137.U
      }

      is(137.U) {
        /*
        $11 = (($10: Z) > (0: Z))
        goto .138
        */



        generalRegFiles(11.U) := (generalRegFiles(10.U).asSInt > 0.S(64.W)).asUInt

        CP := 138.U
      }

      is(138.U) {
        /*
        if ($11: B) goto .139 else goto .148
        */


        CP := Mux((generalRegFiles(11.U).asUInt) === 1.U, 139.U, 148.U)
      }

      is(139.U) {
        /*
        $10 = ($0: MS[anvil.PrinterIndex.U, U8])
        $11 = ($8: anvil.PrinterIndex.U)
        $12 = ($2: anvil.PrinterIndex.U)
        goto .140
        */



        generalRegFiles(10.U) := generalRegFiles(0.U)


        generalRegFiles(11.U) := generalRegFiles(8.U)


        generalRegFiles(12.U) := generalRegFiles(2.U)

        CP := 140.U
      }

      is(140.U) {
        /*
        $13 = (($11: anvil.PrinterIndex.U) & ($12: anvil.PrinterIndex.U))
        goto .141
        */



        generalRegFiles(13.U) := (generalRegFiles(11.U) & generalRegFiles(12.U))

        CP := 141.U
      }

      is(141.U) {
        /*
        *((($10: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($13: anvil.PrinterIndex.U) as SP)) = (48: U8) [unsigned, U8, 1]  // ($10: MS[anvil.PrinterIndex.U, U8])(($13: anvil.PrinterIndex.U)) = (48: U8)
        goto .142
        */


        val __tmp_2394 = ((generalRegFiles(10.U) + 12.U(16.W)) + generalRegFiles(13.U).asUInt)
        val __tmp_2395 = (48.U(8.W)).asUInt
        arrayRegFiles(__tmp_2394 + 0.U) := __tmp_2395(7, 0)

        CP := 142.U
      }

      is(142.U) {
        /*
        $10 = ($7: Z)
        goto .143
        */



        generalRegFiles(10.U) := (generalRegFiles(7.U).asSInt).asUInt

        CP := 143.U
      }

      is(143.U) {
        /*
        $11 = (($10: Z) - (1: Z))
        goto .144
        */



        generalRegFiles(11.U) := ((generalRegFiles(10.U).asSInt - 1.S(64.W))).asUInt

        CP := 144.U
      }

      is(144.U) {
        /*
        $7 = ($11: Z)
        goto .145
        */



        generalRegFiles(7.U) := (generalRegFiles(11.U).asSInt).asUInt

        CP := 145.U
      }

      is(145.U) {
        /*
        $10 = ($8: anvil.PrinterIndex.U)
        goto .146
        */



        generalRegFiles(10.U) := generalRegFiles(8.U)

        CP := 146.U
      }

      is(146.U) {
        /*
        $11 = (($10: anvil.PrinterIndex.U) + (1: anvil.PrinterIndex.U))
        goto .147
        */



        generalRegFiles(11.U) := (generalRegFiles(10.U) + 1.U(64.W))

        CP := 147.U
      }

      is(147.U) {
        /*
        $8 = ($11: anvil.PrinterIndex.U)
        goto .136
        */



        generalRegFiles(8.U) := generalRegFiles(11.U)

        CP := 136.U
      }

      is(148.U) {
        /*
        decl j: anvil.PrinterIndex.I16 @$9
        $10 = ($5: anvil.PrinterIndex.I16)
        undecl i: anvil.PrinterIndex.I16 @$5
        goto .149
        */



        generalRegFiles(10.U) := (generalRegFiles(5.U).asSInt).asUInt

        CP := 149.U
      }

      is(149.U) {
        /*
        $11 = (($10: anvil.PrinterIndex.I16) - (1: anvil.PrinterIndex.I16))
        goto .150
        */



        generalRegFiles(11.U) := ((generalRegFiles(10.U).asSInt - 1.S(8.W))).asUInt

        CP := 150.U
      }

      is(150.U) {
        /*
        $9 = ($11: anvil.PrinterIndex.I16)
        goto .151
        */



        generalRegFiles(9.U) := (generalRegFiles(11.U).asSInt).asUInt

        CP := 151.U
      }

      is(151.U) {
        /*
        $10 = ($9: anvil.PrinterIndex.I16)
        goto .152
        */



        generalRegFiles(10.U) := (generalRegFiles(9.U).asSInt).asUInt

        CP := 152.U
      }

      is(152.U) {
        /*
        $11 = (($10: anvil.PrinterIndex.I16) >= (0: anvil.PrinterIndex.I16))
        goto .153
        */



        generalRegFiles(11.U) := (generalRegFiles(10.U).asSInt >= 0.S(8.W)).asUInt

        CP := 153.U
      }

      is(153.U) {
        /*
        if ($11: B) goto .154 else goto .165
        */


        CP := Mux((generalRegFiles(11.U).asUInt) === 1.U, 154.U, 165.U)
      }

      is(154.U) {
        /*
        $10 = ($0: MS[anvil.PrinterIndex.U, U8])
        $11 = ($8: anvil.PrinterIndex.U)
        $12 = ($2: anvil.PrinterIndex.U)
        goto .155
        */



        generalRegFiles(10.U) := generalRegFiles(0.U)


        generalRegFiles(11.U) := generalRegFiles(8.U)


        generalRegFiles(12.U) := generalRegFiles(2.U)

        CP := 155.U
      }

      is(155.U) {
        /*
        $13 = (($11: anvil.PrinterIndex.U) & ($12: anvil.PrinterIndex.U))
        goto .156
        */



        generalRegFiles(13.U) := (generalRegFiles(11.U) & generalRegFiles(12.U))

        CP := 156.U
      }

      is(156.U) {
        /*
        $14 = (SP + (4: SP))
        $15 = ($9: anvil.PrinterIndex.I16)
        goto .157
        */



        generalRegFiles(14.U) := (SP + 4.U(16.W))


        generalRegFiles(15.U) := (generalRegFiles(9.U).asSInt).asUInt

        CP := 157.U
      }

      is(157.U) {
        /*
        $16 = *((($14: MS[anvil.PrinterIndex.I16, U8]) + (12: SP)) + (($15: anvil.PrinterIndex.I16) as SP)) [unsigned, U8, 1]  // $16 = ($14: MS[anvil.PrinterIndex.I16, U8])(($15: anvil.PrinterIndex.I16))
        goto .158
        */


        val __tmp_2396 = (((generalRegFiles(14.U) + 12.U(16.W)) + generalRegFiles(15.U).asSInt.asUInt)).asUInt
        generalRegFiles(16.U) := Cat(
          arrayRegFiles(__tmp_2396 + 0.U)
        ).asUInt

        CP := 158.U
      }

      is(158.U) {
        /*
        *((($10: MS[anvil.PrinterIndex.U, U8]) + (12: SP)) + (($13: anvil.PrinterIndex.U) as SP)) = ($16: U8) [unsigned, U8, 1]  // ($10: MS[anvil.PrinterIndex.U, U8])(($13: anvil.PrinterIndex.U)) = ($16: U8)
        goto .159
        */


        val __tmp_2397 = ((generalRegFiles(10.U) + 12.U(16.W)) + generalRegFiles(13.U).asUInt)
        val __tmp_2398 = (generalRegFiles(16.U)).asUInt
        arrayRegFiles(__tmp_2397 + 0.U) := __tmp_2398(7, 0)

        CP := 159.U
      }

      is(159.U) {
        /*
        $10 = ($9: anvil.PrinterIndex.I16)
        goto .160
        */



        generalRegFiles(10.U) := (generalRegFiles(9.U).asSInt).asUInt

        CP := 160.U
      }

      is(160.U) {
        /*
        $11 = (($10: anvil.PrinterIndex.I16) - (1: anvil.PrinterIndex.I16))
        goto .161
        */



        generalRegFiles(11.U) := ((generalRegFiles(10.U).asSInt - 1.S(8.W))).asUInt

        CP := 161.U
      }

      is(161.U) {
        /*
        $9 = ($11: anvil.PrinterIndex.I16)
        goto .162
        */



        generalRegFiles(9.U) := (generalRegFiles(11.U).asSInt).asUInt

        CP := 162.U
      }

      is(162.U) {
        /*
        $10 = ($8: anvil.PrinterIndex.U)
        goto .163
        */



        generalRegFiles(10.U) := generalRegFiles(8.U)

        CP := 163.U
      }

      is(163.U) {
        /*
        $11 = (($10: anvil.PrinterIndex.U) + (1: anvil.PrinterIndex.U))
        goto .164
        */



        generalRegFiles(11.U) := (generalRegFiles(10.U) + 1.U(64.W))

        CP := 164.U
      }

      is(164.U) {
        /*
        $8 = ($11: anvil.PrinterIndex.U)
        goto .151
        */



        generalRegFiles(8.U) := generalRegFiles(11.U)

        CP := 151.U
      }

      is(165.U) {
        /*
        $10 = ($4: Z)
        goto .166
        */



        generalRegFiles(10.U) := (generalRegFiles(4.U).asSInt).asUInt

        CP := 166.U
      }

      is(166.U) {
        /*
        $11 = (($10: Z) as U64)
        goto .167
        */



        generalRegFiles(11.U) := generalRegFiles(10.U).asSInt.asUInt

        CP := 167.U
      }

      is(167.U) {
        /*
        **(SP + (2: SP)) = ($11: U64) [unsigned, U64, 8]  // $res = ($11: U64)
        goto $ret@0
        */


        val __tmp_2399 = Cat(
          arrayRegFiles((SP + 2.U(16.W)) + 1.U),
          arrayRegFiles((SP + 2.U(16.W)) + 0.U)
        )
        val __tmp_2400 = (generalRegFiles(11.U)).asUInt
        arrayRegFiles(__tmp_2399 + 0.U) := __tmp_2400(7, 0)
        arrayRegFiles(__tmp_2399 + 1.U) := __tmp_2400(15, 8)
        arrayRegFiles(__tmp_2399 + 2.U) := __tmp_2400(23, 16)
        arrayRegFiles(__tmp_2399 + 3.U) := __tmp_2400(31, 24)
        arrayRegFiles(__tmp_2399 + 4.U) := __tmp_2400(39, 32)
        arrayRegFiles(__tmp_2399 + 5.U) := __tmp_2400(47, 40)
        arrayRegFiles(__tmp_2399 + 6.U) := __tmp_2400(55, 48)
        arrayRegFiles(__tmp_2399 + 7.U) := __tmp_2400(63, 56)

        CP := Cat(
          arrayRegFiles(SP + 0.U + 1.U),
          arrayRegFiles(SP + 0.U + 0.U)
        )

      }

      is(169.U) {
        /*
        *SP = (171: CP) [unsigned, CP, 2]  // $ret@0 = 1362
        *(SP + (2: SP)) = (SP - (8: SP)) [unsigned, SP, 2]  // $res@2 = -8
        $10 = ($3: U32)
        *(SP - (4: SP)) = ($0: U32) [unsigned, U32, 4]  // save $0 (U32)
        goto .170
        */


        val __tmp_2401 = SP
        val __tmp_2402 = (171.U(16.W)).asUInt
        arrayRegFiles(__tmp_2401 + 0.U) := __tmp_2402(7, 0)
        arrayRegFiles(__tmp_2401 + 1.U) := __tmp_2402(15, 8)

        val __tmp_2403 = (SP + 2.U(16.W))
        val __tmp_2404 = ((SP - 8.U(16.W))).asUInt
        arrayRegFiles(__tmp_2403 + 0.U) := __tmp_2404(7, 0)
        arrayRegFiles(__tmp_2403 + 1.U) := __tmp_2404(15, 8)


        generalRegFiles(10.U) := generalRegFiles(3.U)

        val __tmp_2405 = (SP - 4.U(16.W))
        val __tmp_2406 = (generalRegFiles(0.U)).asUInt
        arrayRegFiles(__tmp_2405 + 0.U) := __tmp_2406(7, 0)
        arrayRegFiles(__tmp_2405 + 1.U) := __tmp_2406(15, 8)
        arrayRegFiles(__tmp_2405 + 2.U) := __tmp_2406(23, 16)
        arrayRegFiles(__tmp_2405 + 3.U) := __tmp_2406(31, 24)

        CP := 170.U
      }

      is(170.U) {
        /*
        decl $ret: CP [@0, 2], $res: SP [@2, 6], n: U32 @$0
        $0 = ($10: U32)
        goto .69
        */



        generalRegFiles(0.U) := generalRegFiles(10.U)

        CP := 69.U
      }

      is(171.U) {
        /*
        $0 = *(SP - (4: SP)) [unsigned, U32, 4]  // restore $0 (U32)
        $4 = **(SP + (2: SP)) [unsigned, U32, 4]  // $4 = $res
        undecl n: U32 @$0, $res: SP [@2, 6], $ret: CP [@0, 2]
        goto .172
        */


        val __tmp_2407 = ((SP - 4.U(16.W))).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_2407 + 3.U),
          arrayRegFiles(__tmp_2407 + 2.U),
          arrayRegFiles(__tmp_2407 + 1.U),
          arrayRegFiles(__tmp_2407 + 0.U)
        ).asUInt

        val __tmp_2408 = (Cat(
          arrayRegFiles((SP + 2.U(16.W)) + 1.U),
          arrayRegFiles((SP + 2.U(16.W)) + 0.U)
        )).asUInt
        generalRegFiles(4.U) := Cat(
          arrayRegFiles(__tmp_2408 + 3.U),
          arrayRegFiles(__tmp_2408 + 2.U),
          arrayRegFiles(__tmp_2408 + 1.U),
          arrayRegFiles(__tmp_2408 + 0.U)
        ).asUInt

        CP := 172.U
      }

      is(172.U) {
        /*
        SP = SP - 16
        goto .75
        */


        SP := SP - 16.U

        CP := 75.U
      }

    }

}


