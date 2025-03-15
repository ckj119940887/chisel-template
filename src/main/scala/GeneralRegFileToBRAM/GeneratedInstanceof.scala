package GeneralRegFileToBRAM 

import chisel3._
import chisel3.util._
import chisel3.experimental._

class instanceof (val C_S_AXI_DATA_WIDTH:  Int = 32,
               val C_S_AXI_ADDR_WIDTH:  Int = 32,
               val ARRAY_REG_WIDTH:     Int = 8,
               val ARRAY_REG_DEPTH:     Int = 1024,
               val GENERAL_REG_WIDTH:   Int = 64,
               val GENERAL_REG_DEPTH:   Int = 3,
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
        *1 = 3 [unsigned, SP, 2]  // data address of $res (size = 8)
        *11 = 13 [unsigned, SP, 2]  // data address of a (size = 12)
        goto .4
        */


        SP := 0.U

        val __tmp_260 = 0.U
        val __tmp_261 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_260 + 0.U) := __tmp_261(7, 0)

        val __tmp_262 = 1.U
        val __tmp_263 = (3.U(16.W)).asUInt
        arrayRegFiles(__tmp_262 + 0.U) := __tmp_263(7, 0)
        arrayRegFiles(__tmp_262 + 1.U) := __tmp_263(15, 8)

        val __tmp_264 = 11.U
        val __tmp_265 = (13.U(16.W)).asUInt
        arrayRegFiles(__tmp_264 + 0.U) := __tmp_265(7, 0)
        arrayRegFiles(__tmp_264 + 1.U) := __tmp_265(15, 8)

        CP := 4.U
      }

      is(4.U) {
        /*
        $0 = *(SP + 11) [unsigned, A, 2]  // $0 = a
        goto .5
        */


        val __tmp_266 = (SP + 11.U).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_266 + 1.U),
          arrayRegFiles(__tmp_266 + 0.U)
        ).asUInt

        CP := 5.U
      }

      is(5.U) {
        /*
        switch (*$0)
          2070117317: goto 6
          default: goto 15
        */


        val __tmp_267 = Cat(
             arrayRegFiles(generalRegFiles(0.U) + 3.U),
             arrayRegFiles(generalRegFiles(0.U) + 2.U),
             arrayRegFiles(generalRegFiles(0.U) + 1.U),
             arrayRegFiles(generalRegFiles(0.U) + 0.U)
           )
        CP := 15.U
        switch(__tmp_267) {

          is(2070117317.U) {
            CP := 6.U
          }

        }

      }

      is(6.U) {
        /*
        $1 = true
        goto .7
        */


        generalRegFiles(1.U) := 1.U
        CP := 7.U
      }

      is(7.U) {
        /*
        if $1 goto .8 else goto .14
        */


        CP := Mux((generalRegFiles(1.U).asUInt) === 1.U, 8.U, 14.U)
      }

      is(8.U) {
        /*
        $0 = *(SP + 11) [unsigned, A, 2]  // $0 = a
        goto .9
        */


        val __tmp_268 = (SP + 11.U).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_268 + 1.U),
          arrayRegFiles(__tmp_268 + 0.U)
        ).asUInt

        CP := 9.U
      }

      is(9.U) {
        /*
        switch (*$0)
          2070117317: goto 10
          default: goto 1
        */


        val __tmp_269 = Cat(
             arrayRegFiles(generalRegFiles(0.U) + 3.U),
             arrayRegFiles(generalRegFiles(0.U) + 2.U),
             arrayRegFiles(generalRegFiles(0.U) + 1.U),
             arrayRegFiles(generalRegFiles(0.U) + 0.U)
           )
        CP := 1.U
        switch(__tmp_269) {

          is(2070117317.U) {
            CP := 10.U
          }

        }

      }

      is(10.U) {
        /*
        $1 = $0
        goto .11
        */


        generalRegFiles(1.U) := generalRegFiles(0.U)
        CP := 11.U
      }

      is(11.U) {
        /*
        $2 = *($1 + 4) [signed, Z, 8]  // $2 = $1.x
        goto .12
        */


        val __tmp_270 = (generalRegFiles(1.U) + 4.U).asUInt
        generalRegFiles(2.U) := Cat(
          arrayRegFiles(__tmp_270 + 7.U),
          arrayRegFiles(__tmp_270 + 6.U),
          arrayRegFiles(__tmp_270 + 5.U),
          arrayRegFiles(__tmp_270 + 4.U),
          arrayRegFiles(__tmp_270 + 3.U),
          arrayRegFiles(__tmp_270 + 2.U),
          arrayRegFiles(__tmp_270 + 1.U),
          arrayRegFiles(__tmp_270 + 0.U)
        ).asUInt

        CP := 12.U
      }

      is(12.U) {
        /*
        **(SP + 1) = $2 [signed, Z, 8]  // $res = $2
        goto $ret@0
        */


        val __tmp_271 = Cat(
          arrayRegFiles(SP + 1.U + 1.U),
          arrayRegFiles(SP + 1.U + 0.U)
        )
        val __tmp_272 = (generalRegFiles(2.U).asSInt).asUInt
        arrayRegFiles(__tmp_271 + 0.U) := __tmp_272(7, 0)
        arrayRegFiles(__tmp_271 + 1.U) := __tmp_272(15, 8)
        arrayRegFiles(__tmp_271 + 2.U) := __tmp_272(23, 16)
        arrayRegFiles(__tmp_271 + 3.U) := __tmp_272(31, 24)
        arrayRegFiles(__tmp_271 + 4.U) := __tmp_272(39, 32)
        arrayRegFiles(__tmp_271 + 5.U) := __tmp_272(47, 40)
        arrayRegFiles(__tmp_271 + 6.U) := __tmp_272(55, 48)
        arrayRegFiles(__tmp_271 + 7.U) := __tmp_272(63, 56)

        CP := Cat(
          arrayRegFiles(SP + 0.U + 0.U)
        )

      }

      is(14.U) {
        /*
        **(SP + 1) = 0 [signed, Z, 8]  // $res = 0
        goto $ret@0
        */


        val __tmp_273 = Cat(
          arrayRegFiles(SP + 1.U + 1.U),
          arrayRegFiles(SP + 1.U + 0.U)
        )
        val __tmp_274 = (0.S(64.W)).asUInt
        arrayRegFiles(__tmp_273 + 0.U) := __tmp_274(7, 0)
        arrayRegFiles(__tmp_273 + 1.U) := __tmp_274(15, 8)
        arrayRegFiles(__tmp_273 + 2.U) := __tmp_274(23, 16)
        arrayRegFiles(__tmp_273 + 3.U) := __tmp_274(31, 24)
        arrayRegFiles(__tmp_273 + 4.U) := __tmp_274(39, 32)
        arrayRegFiles(__tmp_273 + 5.U) := __tmp_274(47, 40)
        arrayRegFiles(__tmp_273 + 6.U) := __tmp_274(55, 48)
        arrayRegFiles(__tmp_273 + 7.U) := __tmp_274(63, 56)

        CP := Cat(
          arrayRegFiles(SP + 0.U + 0.U)
        )

      }

      is(15.U) {
        /*
        $1 = false
        goto .7
        */


        generalRegFiles(1.U) := 0.U
        CP := 7.U
      }

    }

}