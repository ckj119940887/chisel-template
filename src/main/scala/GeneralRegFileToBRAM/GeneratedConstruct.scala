package GeneralRegFileToBRAM 

import chisel3._
import chisel3.util._
import chisel3.experimental._

class mkIS (val C_S_AXI_DATA_WIDTH:  Int = 32,
               val C_S_AXI_ADDR_WIDTH:  Int = 32,
               val ARRAY_REG_WIDTH:     Int = 8,
               val ARRAY_REG_DEPTH:     Int = 2048,
               val GENERAL_REG_WIDTH:   Int = 64,
               val GENERAL_REG_DEPTH:   Int = 4,
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
    val DP = RegInit(0.U(64.W))
    // reg for index in memcopy
    val Idx = RegInit(0.U(16.W))
    // reg for recording how many rounds needed for the left bytes
    val LeftByteRounds = RegInit(0.U(8.W))
    val IdxLeftByteRounds = RegInit(0.U(8.W))

    val ArrayAddrWidth: Int = log2Ceil(ARRAY_REG_DEPTH)

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
        *1 = 3 [unsigned, SP, 2]  // data address of $res (size = 812)
        goto .4
        */


        SP := 0.U(16.W)

        val __tmp_2335 = 0.U(8.W)
        val __tmp_2336 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_2335 + 0.U) := __tmp_2336(7, 0)

        val __tmp_2337 = 1.U(16.W)
        val __tmp_2338 = (3.U(16.W)).asUInt
        arrayRegFiles(__tmp_2337 + 0.U) := __tmp_2338(7, 0)
        arrayRegFiles(__tmp_2337 + 1.U) := __tmp_2338(15, 8)

        CP := 4.U
      }

      is(4.U) {
        /*
        $0 = *(SP + 815) [signed, Z, 8]  // $0 = x
        $1 = *(SP + 823) [signed, Z, 8]  // $1 = y
        $2 = *(SP + 831) [signed, Z, 8]  // $2 = z
        alloc $new@[5,10].C803F20F: IS[Z, Z] [@839, 812]
        goto .5
        */


        val __tmp_2339 = (SP + 815.U(16.W)).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_2339 + 7.U),
          arrayRegFiles(__tmp_2339 + 6.U),
          arrayRegFiles(__tmp_2339 + 5.U),
          arrayRegFiles(__tmp_2339 + 4.U),
          arrayRegFiles(__tmp_2339 + 3.U),
          arrayRegFiles(__tmp_2339 + 2.U),
          arrayRegFiles(__tmp_2339 + 1.U),
          arrayRegFiles(__tmp_2339 + 0.U)
        ).asUInt

        val __tmp_2340 = (SP + 823.U(16.W)).asUInt
        generalRegFiles(1.U) := Cat(
          arrayRegFiles(__tmp_2340 + 7.U),
          arrayRegFiles(__tmp_2340 + 6.U),
          arrayRegFiles(__tmp_2340 + 5.U),
          arrayRegFiles(__tmp_2340 + 4.U),
          arrayRegFiles(__tmp_2340 + 3.U),
          arrayRegFiles(__tmp_2340 + 2.U),
          arrayRegFiles(__tmp_2340 + 1.U),
          arrayRegFiles(__tmp_2340 + 0.U)
        ).asUInt

        val __tmp_2341 = (SP + 831.U(16.W)).asUInt
        generalRegFiles(2.U) := Cat(
          arrayRegFiles(__tmp_2341 + 7.U),
          arrayRegFiles(__tmp_2341 + 6.U),
          arrayRegFiles(__tmp_2341 + 5.U),
          arrayRegFiles(__tmp_2341 + 4.U),
          arrayRegFiles(__tmp_2341 + 3.U),
          arrayRegFiles(__tmp_2341 + 2.U),
          arrayRegFiles(__tmp_2341 + 1.U),
          arrayRegFiles(__tmp_2341 + 0.U)
        ).asUInt

        CP := 5.U
      }

      is(5.U) {
        /*
        $3 = (SP + 839)
        *(SP + 839) = 2192300037 [unsigned, U32, 4]  // sha3 type signature of IS[Z, Z]: 0x82ABD805
        *(SP + 843) = 3 [signed, Z, 8]  // size of IS[Z, Z]($0, $1, $2)
        goto .6
        */


        generalRegFiles(3.U) := SP + 839.U(16.W)
        val __tmp_2342 = SP + 839.U(16.W)
        val __tmp_2343 = (2192300037L.U(32.W)).asUInt
        arrayRegFiles(__tmp_2342 + 0.U) := __tmp_2343(7, 0)
        arrayRegFiles(__tmp_2342 + 1.U) := __tmp_2343(15, 8)
        arrayRegFiles(__tmp_2342 + 2.U) := __tmp_2343(23, 16)
        arrayRegFiles(__tmp_2342 + 3.U) := __tmp_2343(31, 24)

        val __tmp_2344 = SP + 843.U(16.W)
        val __tmp_2345 = (3.S(64.W)).asUInt
        arrayRegFiles(__tmp_2344 + 0.U) := __tmp_2345(7, 0)
        arrayRegFiles(__tmp_2344 + 1.U) := __tmp_2345(15, 8)
        arrayRegFiles(__tmp_2344 + 2.U) := __tmp_2345(23, 16)
        arrayRegFiles(__tmp_2344 + 3.U) := __tmp_2345(31, 24)
        arrayRegFiles(__tmp_2344 + 4.U) := __tmp_2345(39, 32)
        arrayRegFiles(__tmp_2344 + 5.U) := __tmp_2345(47, 40)
        arrayRegFiles(__tmp_2344 + 6.U) := __tmp_2345(55, 48)
        arrayRegFiles(__tmp_2344 + 7.U) := __tmp_2345(63, 56)

        CP := 6.U
      }

      is(6.U) {
        /*
        *(($3 + 12) + ((0 as SP) * 8)) = $0 [signed, Z, 8]  // $3(0) = $0
        goto .7
        */


        val __tmp_2346 = generalRegFiles(3.U) + 12.U(16.W) + 0.S(64.W).asUInt * 8.U(16.W)
        val __tmp_2347 = (generalRegFiles(0.U).asSInt).asUInt
        arrayRegFiles(__tmp_2346 + 0.U) := __tmp_2347(7, 0)
        arrayRegFiles(__tmp_2346 + 1.U) := __tmp_2347(15, 8)
        arrayRegFiles(__tmp_2346 + 2.U) := __tmp_2347(23, 16)
        arrayRegFiles(__tmp_2346 + 3.U) := __tmp_2347(31, 24)
        arrayRegFiles(__tmp_2346 + 4.U) := __tmp_2347(39, 32)
        arrayRegFiles(__tmp_2346 + 5.U) := __tmp_2347(47, 40)
        arrayRegFiles(__tmp_2346 + 6.U) := __tmp_2347(55, 48)
        arrayRegFiles(__tmp_2346 + 7.U) := __tmp_2347(63, 56)

        CP := 7.U
      }

      is(7.U) {
        /*
        *(($3 + 12) + ((1 as SP) * 8)) = $1 [signed, Z, 8]  // $3(1) = $1
        goto .8
        */


        val __tmp_2348 = generalRegFiles(3.U) + 12.U(16.W) + 1.S(64.W).asUInt * 8.U(16.W)
        val __tmp_2349 = (generalRegFiles(1.U).asSInt).asUInt
        arrayRegFiles(__tmp_2348 + 0.U) := __tmp_2349(7, 0)
        arrayRegFiles(__tmp_2348 + 1.U) := __tmp_2349(15, 8)
        arrayRegFiles(__tmp_2348 + 2.U) := __tmp_2349(23, 16)
        arrayRegFiles(__tmp_2348 + 3.U) := __tmp_2349(31, 24)
        arrayRegFiles(__tmp_2348 + 4.U) := __tmp_2349(39, 32)
        arrayRegFiles(__tmp_2348 + 5.U) := __tmp_2349(47, 40)
        arrayRegFiles(__tmp_2348 + 6.U) := __tmp_2349(55, 48)
        arrayRegFiles(__tmp_2348 + 7.U) := __tmp_2349(63, 56)

        CP := 8.U
      }

      is(8.U) {
        /*
        *(($3 + 12) + ((2 as SP) * 8)) = $2 [signed, Z, 8]  // $3(2) = $2
        goto .9
        */


        val __tmp_2350 = generalRegFiles(3.U) + 12.U(16.W) + 2.S(64.W).asUInt * 8.U(16.W)
        val __tmp_2351 = (generalRegFiles(2.U).asSInt).asUInt
        arrayRegFiles(__tmp_2350 + 0.U) := __tmp_2351(7, 0)
        arrayRegFiles(__tmp_2350 + 1.U) := __tmp_2351(15, 8)
        arrayRegFiles(__tmp_2350 + 2.U) := __tmp_2351(23, 16)
        arrayRegFiles(__tmp_2350 + 3.U) := __tmp_2351(31, 24)
        arrayRegFiles(__tmp_2350 + 4.U) := __tmp_2351(39, 32)
        arrayRegFiles(__tmp_2350 + 5.U) := __tmp_2351(47, 40)
        arrayRegFiles(__tmp_2350 + 6.U) := __tmp_2351(55, 48)
        arrayRegFiles(__tmp_2350 + 7.U) := __tmp_2351(63, 56)

        CP := 9.U
      }

      is(9.U) {
        /*
        unalloc $new@[5,10].C803F20F: IS[Z, Z] [@839, 812]
        goto .10
        */


        CP := 10.U
      }

      is(10.U) {
        /*
        *(SP + 1) [IS[Z, Z], 812]  <-  $3 [IS[Z, Z], (((*($3 + 4) as SP) * 8) + 12)]  // $res = $3
        goto $ret@0
        */


        val __tmp_2352 = Cat(
          arrayRegFiles(SP + 1.U(16.W) + 1.U),
          arrayRegFiles(SP + 1.U(16.W) + 0.U)
        )
        val __tmp_2353 = generalRegFiles(3.U)
        val __tmp_2354 = Cat(
          arrayRegFiles(generalRegFiles(3.U) + 4.U(16.W) + 7.U),
          arrayRegFiles(generalRegFiles(3.U) + 4.U(16.W) + 6.U),
          arrayRegFiles(generalRegFiles(3.U) + 4.U(16.W) + 5.U),
          arrayRegFiles(generalRegFiles(3.U) + 4.U(16.W) + 4.U),
          arrayRegFiles(generalRegFiles(3.U) + 4.U(16.W) + 3.U),
          arrayRegFiles(generalRegFiles(3.U) + 4.U(16.W) + 2.U),
          arrayRegFiles(generalRegFiles(3.U) + 4.U(16.W) + 1.U),
          arrayRegFiles(generalRegFiles(3.U) + 4.U(16.W) + 0.U)
        ).asSInt.asUInt * 8.U(16.W) + 12.U(64.W)

        when(Idx < __tmp_2354) {
          arrayRegFiles(__tmp_2352 + Idx + 0.U) := arrayRegFiles(__tmp_2353 + Idx + 0.U)
          arrayRegFiles(__tmp_2352 + Idx + 1.U) := arrayRegFiles(__tmp_2353 + Idx + 1.U)
          arrayRegFiles(__tmp_2352 + Idx + 2.U) := arrayRegFiles(__tmp_2353 + Idx + 2.U)
          arrayRegFiles(__tmp_2352 + Idx + 3.U) := arrayRegFiles(__tmp_2353 + Idx + 3.U)
          arrayRegFiles(__tmp_2352 + Idx + 4.U) := arrayRegFiles(__tmp_2353 + Idx + 4.U)
          arrayRegFiles(__tmp_2352 + Idx + 5.U) := arrayRegFiles(__tmp_2353 + Idx + 5.U)
          arrayRegFiles(__tmp_2352 + Idx + 6.U) := arrayRegFiles(__tmp_2353 + Idx + 6.U)
          arrayRegFiles(__tmp_2352 + Idx + 7.U) := arrayRegFiles(__tmp_2353 + Idx + 7.U)
          Idx := Idx + 8.U
          LeftByteRounds := __tmp_2354 - Idx
        } .elsewhen(IdxLeftByteRounds < LeftByteRounds) {
          val __tmp_2355 = Idx - 8.U
          arrayRegFiles(__tmp_2352 + __tmp_2355 + IdxLeftByteRounds) := arrayRegFiles(__tmp_2353 + __tmp_2355 + IdxLeftByteRounds)
          IdxLeftByteRounds := IdxLeftByteRounds + 1.U
        } .otherwise {
          Idx := 0.U
          IdxLeftByteRounds := 0.U
          LeftByteRounds := 0.U
          CP := Cat(
            arrayRegFiles(SP + 0.U + 0.U)
          )

        }


      }

    }

}
