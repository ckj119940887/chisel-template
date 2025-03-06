package GeneralRegFileToBRAM 

import chisel3._
import chisel3.util._
import chisel3.experimental._

class sum (val C_S_AXI_DATA_WIDTH:  Int = 32,
               val C_S_AXI_ADDR_WIDTH:  Int = 32,
               val ARRAY_REG_WIDTH:     Int = 8,
               val ARRAY_REG_DEPTH:     Int = 1024,
               val GENERAL_REG_WIDTH:   Int = 64,
               val GENERAL_REG_DEPTH:   Int = 9,
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
        *1 = 3 [unsigned, SP, 2]  // data address of $res (size = 8)
        *11 = 13 [unsigned, SP, 2]  // data address of a (size = 812)
        goto .4
        */


        SP := 0.U

        val __tmp_195 = 0.U
        val __tmp_196 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_195 + 0.U) := __tmp_196(7, 0)

        val __tmp_197 = 1.U
        val __tmp_198 = (3.U(16.W)).asUInt
        arrayRegFiles(__tmp_197 + 0.U) := __tmp_198(7, 0)
        arrayRegFiles(__tmp_197 + 1.U) := __tmp_198(15, 8)

        val __tmp_199 = 11.U
        val __tmp_200 = (13.U(16.W)).asUInt
        arrayRegFiles(__tmp_199 + 0.U) := __tmp_200(7, 0)
        arrayRegFiles(__tmp_199 + 1.U) := __tmp_200(15, 8)

        CP := 4.U
      }

      is(4.U) {
        /*
        $1 = *(SP + 825) [signed, Z, 8]  // $1 = i
        $0 = *(SP + 11) [unsigned, IS[Z, Z], 2]  // $0 = a
        goto .5
        */


        val __tmp_201 = (SP + 825.U).asUInt
        generalRegFiles(1.U) := Cat(
          arrayRegFiles(__tmp_201 + 7.U),
          arrayRegFiles(__tmp_201 + 6.U),
          arrayRegFiles(__tmp_201 + 5.U),
          arrayRegFiles(__tmp_201 + 4.U),
          arrayRegFiles(__tmp_201 + 3.U),
          arrayRegFiles(__tmp_201 + 2.U),
          arrayRegFiles(__tmp_201 + 1.U),
          arrayRegFiles(__tmp_201 + 0.U)
        ).asUInt

        val __tmp_202 = (SP + 11.U).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_202 + 1.U),
          arrayRegFiles(__tmp_202 + 0.U)
        ).asUInt

        CP := 5.U
      }

      is(5.U) {
        /*
        $2 = *($0 + 4) [signed, Z, 8]  // $2 = $0.size
        goto .6
        */


        val __tmp_203 = (generalRegFiles(0.U) + 4.U).asUInt
        generalRegFiles(2.U) := Cat(
          arrayRegFiles(__tmp_203 + 7.U),
          arrayRegFiles(__tmp_203 + 6.U),
          arrayRegFiles(__tmp_203 + 5.U),
          arrayRegFiles(__tmp_203 + 4.U),
          arrayRegFiles(__tmp_203 + 3.U),
          arrayRegFiles(__tmp_203 + 2.U),
          arrayRegFiles(__tmp_203 + 1.U),
          arrayRegFiles(__tmp_203 + 0.U)
        ).asUInt

        CP := 6.U
      }

      is(6.U) {
        /*
        $3 = ($1 < $2)
        goto .7
        */


        generalRegFiles(3.U) := (generalRegFiles(1.U).asSInt < generalRegFiles(2.U).asSInt).asUInt
        CP := 7.U
      }

      is(7.U) {
        /*
        if $3 goto .8 else goto .15
        */


        CP := Mux((generalRegFiles(3.U).asUInt) === 1.U, 8.U, 15.U)
      }

      is(8.U) {
        /*
        $1 = *(SP + 11) [unsigned, IS[Z, Z], 2]  // $1 = a
        $0 = *(SP + 825) [signed, Z, 8]  // $0 = i
        goto .9
        */


        val __tmp_204 = (SP + 11.U).asUInt
        generalRegFiles(1.U) := Cat(
          arrayRegFiles(__tmp_204 + 1.U),
          arrayRegFiles(__tmp_204 + 0.U)
        ).asUInt

        val __tmp_205 = (SP + 825.U).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_205 + 7.U),
          arrayRegFiles(__tmp_205 + 6.U),
          arrayRegFiles(__tmp_205 + 5.U),
          arrayRegFiles(__tmp_205 + 4.U),
          arrayRegFiles(__tmp_205 + 3.U),
          arrayRegFiles(__tmp_205 + 2.U),
          arrayRegFiles(__tmp_205 + 1.U),
          arrayRegFiles(__tmp_205 + 0.U)
        ).asUInt

        CP := 9.U
      }

      is(9.U) {
        /*
        $2 = ($0 + 1)
        $3 = *(SP + 833) [signed, Z, 8]  // $3 = acc
        $5 = *(SP + 11) [unsigned, IS[Z, Z], 2]  // $5 = a
        $4 = *(SP + 825) [signed, Z, 8]  // $4 = i
        goto .10
        */


        generalRegFiles(2.U) := (generalRegFiles(0.U).asSInt + 1.S).asUInt
        val __tmp_206 = (SP + 833.U).asUInt
        generalRegFiles(3.U) := Cat(
          arrayRegFiles(__tmp_206 + 7.U),
          arrayRegFiles(__tmp_206 + 6.U),
          arrayRegFiles(__tmp_206 + 5.U),
          arrayRegFiles(__tmp_206 + 4.U),
          arrayRegFiles(__tmp_206 + 3.U),
          arrayRegFiles(__tmp_206 + 2.U),
          arrayRegFiles(__tmp_206 + 1.U),
          arrayRegFiles(__tmp_206 + 0.U)
        ).asUInt

        val __tmp_207 = (SP + 11.U).asUInt
        generalRegFiles(5.U) := Cat(
          arrayRegFiles(__tmp_207 + 1.U),
          arrayRegFiles(__tmp_207 + 0.U)
        ).asUInt

        val __tmp_208 = (SP + 825.U).asUInt
        generalRegFiles(4.U) := Cat(
          arrayRegFiles(__tmp_208 + 7.U),
          arrayRegFiles(__tmp_208 + 6.U),
          arrayRegFiles(__tmp_208 + 5.U),
          arrayRegFiles(__tmp_208 + 4.U),
          arrayRegFiles(__tmp_208 + 3.U),
          arrayRegFiles(__tmp_208 + 2.U),
          arrayRegFiles(__tmp_208 + 1.U),
          arrayRegFiles(__tmp_208 + 0.U)
        ).asUInt

        CP := 10.U
      }

      is(10.U) {
        /*
        if ((0 <= $4) & ($4 <= *($5 + 4))) goto .11 else goto .1
        */


        CP := Mux(((0.S <= generalRegFiles(4.U).asSInt).asUInt & (generalRegFiles(4.U).asSInt <= Cat(
                            arrayRegFiles(generalRegFiles(5.U) + 4.U + 7.U),
                            arrayRegFiles(generalRegFiles(5.U) + 4.U + 6.U),
                            arrayRegFiles(generalRegFiles(5.U) + 4.U + 5.U),
                            arrayRegFiles(generalRegFiles(5.U) + 4.U + 4.U),
                            arrayRegFiles(generalRegFiles(5.U) + 4.U + 3.U),
                            arrayRegFiles(generalRegFiles(5.U) + 4.U + 2.U),
                            arrayRegFiles(generalRegFiles(5.U) + 4.U + 1.U),
                            arrayRegFiles(generalRegFiles(5.U) + 4.U + 0.U)
                          ).asSInt).asUInt.asUInt) === 1.U, 11.U, 1.U)
      }

      is(11.U) {
        /*
        $6 = *(($5 + 12) + (($4 as SP) * 8)) [signed, Z, 8]  // $6 = $5($4)
        goto .12
        */


        val __tmp_209 = (generalRegFiles(5.U) + 12.U + generalRegFiles(4.U).asSInt.asUInt * 8.U).asUInt
        generalRegFiles(6.U) := Cat(
          arrayRegFiles(__tmp_209 + 7.U),
          arrayRegFiles(__tmp_209 + 6.U),
          arrayRegFiles(__tmp_209 + 5.U),
          arrayRegFiles(__tmp_209 + 4.U),
          arrayRegFiles(__tmp_209 + 3.U),
          arrayRegFiles(__tmp_209 + 2.U),
          arrayRegFiles(__tmp_209 + 1.U),
          arrayRegFiles(__tmp_209 + 0.U)
        ).asUInt

        CP := 12.U
      }

      is(12.U) {
        /*
        $7 = ($3 + $6)
        alloc sum$res@[6,12].FB94AB9A: Z [@841, 8]
        SP = SP + 913
        decl $ret: CP [@0, 1], $res: SP [@1, 10], a: IS[Z, Z] [@11, 814], i: Z [@825, 8], acc: Z [@833, 8]
        goto .13
        */


        generalRegFiles(7.U) := (generalRegFiles(3.U).asSInt + generalRegFiles(6.U).asSInt).asUInt
        SP := SP + 913.U

        CP := 13.U
      }

      is(13.U) {
        /*
        *SP = 17 [signed, CP, 1]  // $ret@0 = 18
        *(SP + 1) = (SP - 72) [unsigned, SP, 2]  // $res@1 = -72
        *(SP + 11) = $1 [unsigned, SP, 2]  // a = $1
        *(SP + 825) = $2 [signed, Z, 8]  // i = $2
        *(SP + 833) = $7 [signed, Z, 8]  // acc = $7
        *(SP - 64) = $0 [unsigned, U64, 8]  // save $0
        *(SP - 56) = $1 [unsigned, U64, 8]  // save $1
        *(SP - 48) = $2 [unsigned, U64, 8]  // save $2
        *(SP - 40) = $3 [unsigned, U64, 8]  // save $3
        *(SP - 32) = $4 [unsigned, U64, 8]  // save $4
        *(SP - 24) = $5 [unsigned, U64, 8]  // save $5
        *(SP - 16) = $6 [unsigned, U64, 8]  // save $6
        *(SP - 8) = $7 [unsigned, U64, 8]  // save $7
        goto .4
        */


        val __tmp_210 = SP
        val __tmp_211 = (17.S(8.W)).asUInt
        arrayRegFiles(__tmp_210 + 0.U) := __tmp_211(7, 0)

        val __tmp_212 = SP + 1.U
        val __tmp_213 = (SP - 72.U).asUInt
        arrayRegFiles(__tmp_212 + 0.U) := __tmp_213(7, 0)
        arrayRegFiles(__tmp_212 + 1.U) := __tmp_213(15, 8)

        val __tmp_214 = SP + 11.U
        val __tmp_215 = (generalRegFiles(1.U)).asUInt
        arrayRegFiles(__tmp_214 + 0.U) := __tmp_215(7, 0)
        arrayRegFiles(__tmp_214 + 1.U) := __tmp_215(15, 8)

        val __tmp_216 = SP + 825.U
        val __tmp_217 = (generalRegFiles(2.U).asSInt).asUInt
        arrayRegFiles(__tmp_216 + 0.U) := __tmp_217(7, 0)
        arrayRegFiles(__tmp_216 + 1.U) := __tmp_217(15, 8)
        arrayRegFiles(__tmp_216 + 2.U) := __tmp_217(23, 16)
        arrayRegFiles(__tmp_216 + 3.U) := __tmp_217(31, 24)
        arrayRegFiles(__tmp_216 + 4.U) := __tmp_217(39, 32)
        arrayRegFiles(__tmp_216 + 5.U) := __tmp_217(47, 40)
        arrayRegFiles(__tmp_216 + 6.U) := __tmp_217(55, 48)
        arrayRegFiles(__tmp_216 + 7.U) := __tmp_217(63, 56)

        val __tmp_218 = SP + 833.U
        val __tmp_219 = (generalRegFiles(7.U).asSInt).asUInt
        arrayRegFiles(__tmp_218 + 0.U) := __tmp_219(7, 0)
        arrayRegFiles(__tmp_218 + 1.U) := __tmp_219(15, 8)
        arrayRegFiles(__tmp_218 + 2.U) := __tmp_219(23, 16)
        arrayRegFiles(__tmp_218 + 3.U) := __tmp_219(31, 24)
        arrayRegFiles(__tmp_218 + 4.U) := __tmp_219(39, 32)
        arrayRegFiles(__tmp_218 + 5.U) := __tmp_219(47, 40)
        arrayRegFiles(__tmp_218 + 6.U) := __tmp_219(55, 48)
        arrayRegFiles(__tmp_218 + 7.U) := __tmp_219(63, 56)

        val __tmp_220 = SP - 64.U
        val __tmp_221 = (generalRegFiles(0.U)).asUInt
        arrayRegFiles(__tmp_220 + 0.U) := __tmp_221(7, 0)
        arrayRegFiles(__tmp_220 + 1.U) := __tmp_221(15, 8)
        arrayRegFiles(__tmp_220 + 2.U) := __tmp_221(23, 16)
        arrayRegFiles(__tmp_220 + 3.U) := __tmp_221(31, 24)
        arrayRegFiles(__tmp_220 + 4.U) := __tmp_221(39, 32)
        arrayRegFiles(__tmp_220 + 5.U) := __tmp_221(47, 40)
        arrayRegFiles(__tmp_220 + 6.U) := __tmp_221(55, 48)
        arrayRegFiles(__tmp_220 + 7.U) := __tmp_221(63, 56)

        val __tmp_222 = SP - 56.U
        val __tmp_223 = (generalRegFiles(1.U)).asUInt
        arrayRegFiles(__tmp_222 + 0.U) := __tmp_223(7, 0)
        arrayRegFiles(__tmp_222 + 1.U) := __tmp_223(15, 8)
        arrayRegFiles(__tmp_222 + 2.U) := __tmp_223(23, 16)
        arrayRegFiles(__tmp_222 + 3.U) := __tmp_223(31, 24)
        arrayRegFiles(__tmp_222 + 4.U) := __tmp_223(39, 32)
        arrayRegFiles(__tmp_222 + 5.U) := __tmp_223(47, 40)
        arrayRegFiles(__tmp_222 + 6.U) := __tmp_223(55, 48)
        arrayRegFiles(__tmp_222 + 7.U) := __tmp_223(63, 56)

        val __tmp_224 = SP - 48.U
        val __tmp_225 = (generalRegFiles(2.U)).asUInt
        arrayRegFiles(__tmp_224 + 0.U) := __tmp_225(7, 0)
        arrayRegFiles(__tmp_224 + 1.U) := __tmp_225(15, 8)
        arrayRegFiles(__tmp_224 + 2.U) := __tmp_225(23, 16)
        arrayRegFiles(__tmp_224 + 3.U) := __tmp_225(31, 24)
        arrayRegFiles(__tmp_224 + 4.U) := __tmp_225(39, 32)
        arrayRegFiles(__tmp_224 + 5.U) := __tmp_225(47, 40)
        arrayRegFiles(__tmp_224 + 6.U) := __tmp_225(55, 48)
        arrayRegFiles(__tmp_224 + 7.U) := __tmp_225(63, 56)

        val __tmp_226 = SP - 40.U
        val __tmp_227 = (generalRegFiles(3.U)).asUInt
        arrayRegFiles(__tmp_226 + 0.U) := __tmp_227(7, 0)
        arrayRegFiles(__tmp_226 + 1.U) := __tmp_227(15, 8)
        arrayRegFiles(__tmp_226 + 2.U) := __tmp_227(23, 16)
        arrayRegFiles(__tmp_226 + 3.U) := __tmp_227(31, 24)
        arrayRegFiles(__tmp_226 + 4.U) := __tmp_227(39, 32)
        arrayRegFiles(__tmp_226 + 5.U) := __tmp_227(47, 40)
        arrayRegFiles(__tmp_226 + 6.U) := __tmp_227(55, 48)
        arrayRegFiles(__tmp_226 + 7.U) := __tmp_227(63, 56)

        val __tmp_228 = SP - 32.U
        val __tmp_229 = (generalRegFiles(4.U)).asUInt
        arrayRegFiles(__tmp_228 + 0.U) := __tmp_229(7, 0)
        arrayRegFiles(__tmp_228 + 1.U) := __tmp_229(15, 8)
        arrayRegFiles(__tmp_228 + 2.U) := __tmp_229(23, 16)
        arrayRegFiles(__tmp_228 + 3.U) := __tmp_229(31, 24)
        arrayRegFiles(__tmp_228 + 4.U) := __tmp_229(39, 32)
        arrayRegFiles(__tmp_228 + 5.U) := __tmp_229(47, 40)
        arrayRegFiles(__tmp_228 + 6.U) := __tmp_229(55, 48)
        arrayRegFiles(__tmp_228 + 7.U) := __tmp_229(63, 56)

        val __tmp_230 = SP - 24.U
        val __tmp_231 = (generalRegFiles(5.U)).asUInt
        arrayRegFiles(__tmp_230 + 0.U) := __tmp_231(7, 0)
        arrayRegFiles(__tmp_230 + 1.U) := __tmp_231(15, 8)
        arrayRegFiles(__tmp_230 + 2.U) := __tmp_231(23, 16)
        arrayRegFiles(__tmp_230 + 3.U) := __tmp_231(31, 24)
        arrayRegFiles(__tmp_230 + 4.U) := __tmp_231(39, 32)
        arrayRegFiles(__tmp_230 + 5.U) := __tmp_231(47, 40)
        arrayRegFiles(__tmp_230 + 6.U) := __tmp_231(55, 48)
        arrayRegFiles(__tmp_230 + 7.U) := __tmp_231(63, 56)

        val __tmp_232 = SP - 16.U
        val __tmp_233 = (generalRegFiles(6.U)).asUInt
        arrayRegFiles(__tmp_232 + 0.U) := __tmp_233(7, 0)
        arrayRegFiles(__tmp_232 + 1.U) := __tmp_233(15, 8)
        arrayRegFiles(__tmp_232 + 2.U) := __tmp_233(23, 16)
        arrayRegFiles(__tmp_232 + 3.U) := __tmp_233(31, 24)
        arrayRegFiles(__tmp_232 + 4.U) := __tmp_233(39, 32)
        arrayRegFiles(__tmp_232 + 5.U) := __tmp_233(47, 40)
        arrayRegFiles(__tmp_232 + 6.U) := __tmp_233(55, 48)
        arrayRegFiles(__tmp_232 + 7.U) := __tmp_233(63, 56)

        val __tmp_234 = SP - 8.U
        val __tmp_235 = (generalRegFiles(7.U)).asUInt
        arrayRegFiles(__tmp_234 + 0.U) := __tmp_235(7, 0)
        arrayRegFiles(__tmp_234 + 1.U) := __tmp_235(15, 8)
        arrayRegFiles(__tmp_234 + 2.U) := __tmp_235(23, 16)
        arrayRegFiles(__tmp_234 + 3.U) := __tmp_235(31, 24)
        arrayRegFiles(__tmp_234 + 4.U) := __tmp_235(39, 32)
        arrayRegFiles(__tmp_234 + 5.U) := __tmp_235(47, 40)
        arrayRegFiles(__tmp_234 + 6.U) := __tmp_235(55, 48)
        arrayRegFiles(__tmp_234 + 7.U) := __tmp_235(63, 56)

        CP := 4.U
      }

      is(15.U) {
        /*
        $1 = *(SP + 833) [signed, Z, 8]  // $1 = acc
        goto .16
        */


        val __tmp_236 = (SP + 833.U).asUInt
        generalRegFiles(1.U) := Cat(
          arrayRegFiles(__tmp_236 + 7.U),
          arrayRegFiles(__tmp_236 + 6.U),
          arrayRegFiles(__tmp_236 + 5.U),
          arrayRegFiles(__tmp_236 + 4.U),
          arrayRegFiles(__tmp_236 + 3.U),
          arrayRegFiles(__tmp_236 + 2.U),
          arrayRegFiles(__tmp_236 + 1.U),
          arrayRegFiles(__tmp_236 + 0.U)
        ).asUInt

        CP := 16.U
      }

      is(16.U) {
        /*
        **(SP + 1) = $1 [signed, Z, 8]  // $res = $1
        goto $ret@0
        */


        val __tmp_237 = Cat(
          arrayRegFiles(SP + 1.U + 1.U),
          arrayRegFiles(SP + 1.U + 0.U)
        )
        val __tmp_238 = (generalRegFiles(1.U).asSInt).asUInt
        arrayRegFiles(__tmp_237 + 0.U) := __tmp_238(7, 0)
        arrayRegFiles(__tmp_237 + 1.U) := __tmp_238(15, 8)
        arrayRegFiles(__tmp_237 + 2.U) := __tmp_238(23, 16)
        arrayRegFiles(__tmp_237 + 3.U) := __tmp_238(31, 24)
        arrayRegFiles(__tmp_237 + 4.U) := __tmp_238(39, 32)
        arrayRegFiles(__tmp_237 + 5.U) := __tmp_238(47, 40)
        arrayRegFiles(__tmp_237 + 6.U) := __tmp_238(55, 48)
        arrayRegFiles(__tmp_237 + 7.U) := __tmp_238(63, 56)

        CP := Cat(
          arrayRegFiles(SP + 0.U + 0.U)
        )

      }

      is(17.U) {
        /*
        $0 = *(SP - 64) [unsigned, U64, 8]  // restore $0
        $1 = *(SP - 56) [unsigned, U64, 8]  // restore $1
        $2 = *(SP - 48) [unsigned, U64, 8]  // restore $2
        $3 = *(SP - 40) [unsigned, U64, 8]  // restore $3
        $4 = *(SP - 32) [unsigned, U64, 8]  // restore $4
        $5 = *(SP - 24) [unsigned, U64, 8]  // restore $5
        $6 = *(SP - 16) [unsigned, U64, 8]  // restore $6
        $7 = *(SP - 8) [unsigned, U64, 8]  // restore $7
        $8 = **(SP + 1) [unsigned, SP, 2]  // $8 = $ret
        undecl acc: Z [@833, 8], i: Z [@825, 8], a: IS[Z, Z] [@11, 814], $res: SP [@1, 10], $ret: CP [@0, 1]
        SP = SP - 913
        goto .18
        */


        val __tmp_239 = (SP - 64.U).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_239 + 7.U),
          arrayRegFiles(__tmp_239 + 6.U),
          arrayRegFiles(__tmp_239 + 5.U),
          arrayRegFiles(__tmp_239 + 4.U),
          arrayRegFiles(__tmp_239 + 3.U),
          arrayRegFiles(__tmp_239 + 2.U),
          arrayRegFiles(__tmp_239 + 1.U),
          arrayRegFiles(__tmp_239 + 0.U)
        ).asUInt

        val __tmp_240 = (SP - 56.U).asUInt
        generalRegFiles(1.U) := Cat(
          arrayRegFiles(__tmp_240 + 7.U),
          arrayRegFiles(__tmp_240 + 6.U),
          arrayRegFiles(__tmp_240 + 5.U),
          arrayRegFiles(__tmp_240 + 4.U),
          arrayRegFiles(__tmp_240 + 3.U),
          arrayRegFiles(__tmp_240 + 2.U),
          arrayRegFiles(__tmp_240 + 1.U),
          arrayRegFiles(__tmp_240 + 0.U)
        ).asUInt

        val __tmp_241 = (SP - 48.U).asUInt
        generalRegFiles(2.U) := Cat(
          arrayRegFiles(__tmp_241 + 7.U),
          arrayRegFiles(__tmp_241 + 6.U),
          arrayRegFiles(__tmp_241 + 5.U),
          arrayRegFiles(__tmp_241 + 4.U),
          arrayRegFiles(__tmp_241 + 3.U),
          arrayRegFiles(__tmp_241 + 2.U),
          arrayRegFiles(__tmp_241 + 1.U),
          arrayRegFiles(__tmp_241 + 0.U)
        ).asUInt

        val __tmp_242 = (SP - 40.U).asUInt
        generalRegFiles(3.U) := Cat(
          arrayRegFiles(__tmp_242 + 7.U),
          arrayRegFiles(__tmp_242 + 6.U),
          arrayRegFiles(__tmp_242 + 5.U),
          arrayRegFiles(__tmp_242 + 4.U),
          arrayRegFiles(__tmp_242 + 3.U),
          arrayRegFiles(__tmp_242 + 2.U),
          arrayRegFiles(__tmp_242 + 1.U),
          arrayRegFiles(__tmp_242 + 0.U)
        ).asUInt

        val __tmp_243 = (SP - 32.U).asUInt
        generalRegFiles(4.U) := Cat(
          arrayRegFiles(__tmp_243 + 7.U),
          arrayRegFiles(__tmp_243 + 6.U),
          arrayRegFiles(__tmp_243 + 5.U),
          arrayRegFiles(__tmp_243 + 4.U),
          arrayRegFiles(__tmp_243 + 3.U),
          arrayRegFiles(__tmp_243 + 2.U),
          arrayRegFiles(__tmp_243 + 1.U),
          arrayRegFiles(__tmp_243 + 0.U)
        ).asUInt

        val __tmp_244 = (SP - 24.U).asUInt
        generalRegFiles(5.U) := Cat(
          arrayRegFiles(__tmp_244 + 7.U),
          arrayRegFiles(__tmp_244 + 6.U),
          arrayRegFiles(__tmp_244 + 5.U),
          arrayRegFiles(__tmp_244 + 4.U),
          arrayRegFiles(__tmp_244 + 3.U),
          arrayRegFiles(__tmp_244 + 2.U),
          arrayRegFiles(__tmp_244 + 1.U),
          arrayRegFiles(__tmp_244 + 0.U)
        ).asUInt

        val __tmp_245 = (SP - 16.U).asUInt
        generalRegFiles(6.U) := Cat(
          arrayRegFiles(__tmp_245 + 7.U),
          arrayRegFiles(__tmp_245 + 6.U),
          arrayRegFiles(__tmp_245 + 5.U),
          arrayRegFiles(__tmp_245 + 4.U),
          arrayRegFiles(__tmp_245 + 3.U),
          arrayRegFiles(__tmp_245 + 2.U),
          arrayRegFiles(__tmp_245 + 1.U),
          arrayRegFiles(__tmp_245 + 0.U)
        ).asUInt

        val __tmp_246 = (SP - 8.U).asUInt
        generalRegFiles(7.U) := Cat(
          arrayRegFiles(__tmp_246 + 7.U),
          arrayRegFiles(__tmp_246 + 6.U),
          arrayRegFiles(__tmp_246 + 5.U),
          arrayRegFiles(__tmp_246 + 4.U),
          arrayRegFiles(__tmp_246 + 3.U),
          arrayRegFiles(__tmp_246 + 2.U),
          arrayRegFiles(__tmp_246 + 1.U),
          arrayRegFiles(__tmp_246 + 0.U)
        ).asUInt

        val __tmp_247 = (Cat(
          arrayRegFiles(SP + 1.U + 7.U),
          arrayRegFiles(SP + 1.U + 6.U),
          arrayRegFiles(SP + 1.U + 5.U),
          arrayRegFiles(SP + 1.U + 4.U),
          arrayRegFiles(SP + 1.U + 3.U),
          arrayRegFiles(SP + 1.U + 2.U),
          arrayRegFiles(SP + 1.U + 1.U),
          arrayRegFiles(SP + 1.U + 0.U)
        ).asSInt).asUInt
        generalRegFiles(8.U) := Cat(
          arrayRegFiles(__tmp_247 + 1.U),
          arrayRegFiles(__tmp_247 + 0.U)
        ).asUInt

        SP := SP - 913.U

        CP := 18.U
      }

      is(18.U) {
        /*
        **(SP + 1) = $8 [signed, Z, 8]  // $res = $8
        goto $ret@0
        */


        val __tmp_248 = Cat(
          arrayRegFiles(SP + 1.U + 1.U),
          arrayRegFiles(SP + 1.U + 0.U)
        )
        val __tmp_249 = (generalRegFiles(8.U).asSInt).asUInt
        arrayRegFiles(__tmp_248 + 0.U) := __tmp_249(7, 0)
        arrayRegFiles(__tmp_248 + 1.U) := __tmp_249(15, 8)
        arrayRegFiles(__tmp_248 + 2.U) := __tmp_249(23, 16)
        arrayRegFiles(__tmp_248 + 3.U) := __tmp_249(31, 24)
        arrayRegFiles(__tmp_248 + 4.U) := __tmp_249(39, 32)
        arrayRegFiles(__tmp_248 + 5.U) := __tmp_249(47, 40)
        arrayRegFiles(__tmp_248 + 6.U) := __tmp_249(55, 48)
        arrayRegFiles(__tmp_248 + 7.U) := __tmp_249(63, 56)

        CP := Cat(
          arrayRegFiles(SP + 0.U + 0.U)
        )

      }

    }

}
