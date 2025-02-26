package GeneralRegFileToBRAM 

import chisel3._
import chisel3.util._
import chisel3.experimental._

class Bubble(val C_S_AXI_DATA_WIDTH: Int = 32,
             val C_S_AXI_ADDR_WIDTH: Int = 32,
             val ARRAY_REG_WIDTH: Int = 8,
             val ARRAY_REG_DEPTH: Int = 1024,
             val GENERAL_REG_WIDTH: Int = 64,
             val GENERAL_REG_DEPTH: Int = 64,
             val STACK_POINTER_WIDTH: Int = 64,
             val CODE_POINTER_WIDTH: Int = 64) extends Module {
    val io = IO(new Bundle{
        val valid = Input(Bool())
        val ready = Output(UInt(2.W))
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
    val CP = RegInit(1.U(CODE_POINTER_WIDTH.W))
    // reg for stack pointer
    val SP = RegInit(0.S(STACK_POINTER_WIDTH.W))

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

    io.ready := Mux(CP === 0.U, true.B, false.B)

    switch(CP) {
        is(1.U) {
            CP := Mux(io.valid, 2.U, CP)
        }
        is(2.U) {
            // SP = 0
            // *0 = 0 [unsigned, U64, 8]  // $ret
            // *8 = 16 [signed, S64, 8]  // data address of a
            // goto .3
            SP := 0.S

            val __tmp_0 = 0.U(64.W).asUInt
            arrayRegFiles((0.S + 0.S).asUInt) := __tmp_0(7,0) 
            arrayRegFiles((0.S + 1.S).asUInt) := __tmp_0(15,8) 
            arrayRegFiles((0.S + 2.S).asUInt) := __tmp_0(23,16) 
            arrayRegFiles((0.S + 3.S).asUInt) := __tmp_0(31,24) 
            arrayRegFiles((0.S + 4.S).asUInt) := __tmp_0(39,32) 
            arrayRegFiles((0.S + 5.S).asUInt) := __tmp_0(47,40) 
            arrayRegFiles((0.S + 6.S).asUInt) := __tmp_0(55,48) 
            arrayRegFiles((0.S + 7.S).asUInt) := __tmp_0(63,56) 

            val __tmp_1 = 16.S(64.W).asUInt
            arrayRegFiles((8.S + 0.S).asUInt) := __tmp_1(7,0) 
            arrayRegFiles((8.S + 1.S).asUInt) := __tmp_1(15,8) 
            arrayRegFiles((8.S + 2.S).asUInt) := __tmp_1(23,16) 
            arrayRegFiles((8.S + 3.S).asUInt) := __tmp_1(31,24) 
            arrayRegFiles((8.S + 4.S).asUInt) := __tmp_1(39,32) 
            arrayRegFiles((8.S + 5.S).asUInt) := __tmp_1(47,40) 
            arrayRegFiles((8.S + 6.S).asUInt) := __tmp_1(55,48) 
            arrayRegFiles((8.S + 7.S).asUInt) := __tmp_1(63,56) 

            CP := 3.U
        }
        is(3.U) {
            // decl i: Z [@228, 8]
            // *(SP + 228) = 0 [signed, Z, 8]  // i = 0
            // goto .4
            val __tmp_2 = 0.S(64.W).asUInt
            arrayRegFiles((SP + 228.S + 0.S).asUInt) := __tmp_2(7,0)
            arrayRegFiles((SP + 228.S + 1.S).asUInt) := __tmp_2(15,8)
            arrayRegFiles((SP + 228.S + 2.S).asUInt) := __tmp_2(23,16)
            arrayRegFiles((SP + 228.S + 3.S).asUInt) := __tmp_2(31,24)
            arrayRegFiles((SP + 228.S + 4.S).asUInt) := __tmp_2(39,32)
            arrayRegFiles((SP + 228.S + 5.S).asUInt) := __tmp_2(47,40)
            arrayRegFiles((SP + 228.S + 6.S).asUInt) := __tmp_2(55,48)
            arrayRegFiles((SP + 228.S + 7.S).asUInt) := __tmp_2(63,56)

            CP := 4.U
        }
        is(4.U) {
            // $1 = *(SP + 228) [signed, Z, 8]  // $1 = i
            // $0 = *(SP + 8) [signed, MS[Z, U16], 8]  // $0 = a
            // goto .5
            generalRegFiles(1.U) := Cat(
                arrayRegFiles((SP + 228.S + 7.S).asUInt),
                arrayRegFiles((SP + 228.S + 6.S).asUInt),
                arrayRegFiles((SP + 228.S + 5.S).asUInt),
                arrayRegFiles((SP + 228.S + 4.S).asUInt),
                arrayRegFiles((SP + 228.S + 3.S).asUInt),
                arrayRegFiles((SP + 228.S + 2.S).asUInt),
                arrayRegFiles((SP + 228.S + 1.S).asUInt),
                arrayRegFiles((SP + 228.S + 0.S).asUInt)
            )

            generalRegFiles(0.U) := Cat(
                arrayRegFiles((SP + 8.S + 7.S).asUInt),
                arrayRegFiles((SP + 8.S + 6.S).asUInt),
                arrayRegFiles((SP + 8.S + 5.S).asUInt),
                arrayRegFiles((SP + 8.S + 4.S).asUInt),
                arrayRegFiles((SP + 8.S + 3.S).asUInt),
                arrayRegFiles((SP + 8.S + 2.S).asUInt),
                arrayRegFiles((SP + 8.S + 1.S).asUInt),
                arrayRegFiles((SP + 8.S + 0.S).asUInt)
            )

            CP := 5.U
        }
        is(5.U) {
            // $2 = *($0 + 4) [signed, Z, 8]  // $2 = $0.size
            // goto .6
            val __tmp_3 = (generalRegFiles(0.U).asSInt + 4.S).asSInt
            generalRegFiles(2.U) := Cat(
                arrayRegFiles((__tmp_3 + 7.S).asUInt),
                arrayRegFiles((__tmp_3 + 6.S).asUInt),
                arrayRegFiles((__tmp_3 + 5.S).asUInt),
                arrayRegFiles((__tmp_3 + 4.S).asUInt),
                arrayRegFiles((__tmp_3 + 3.S).asUInt),
                arrayRegFiles((__tmp_3 + 2.S).asUInt),
                arrayRegFiles((__tmp_3 + 1.S).asUInt),
                arrayRegFiles((__tmp_3 + 0.S).asUInt)
            )

            CP := 6.U
        }
        is(6.U) {
            // $3 = ($1 < $2)
            // goto .7
            generalRegFiles(3.U) := (generalRegFiles(1.U) < generalRegFiles(2.U)).asUInt
            
            CP := 7.U
        }
        is(7.U) {
            // if $3 goto .8 else goto .39
            CP := Mux(generalRegFiles(3.U) === 1.U, 8.U, 39.U)
        }
        is(8.U) {
            // decl j: Z [@236, 8]
            // $1 = *(SP + 228) [signed, Z, 8]  // $1 = i
            // goto .9
            generalRegFiles(1.U) := Cat(
                arrayRegFiles((SP + 228.S + 7.S).asUInt),
                arrayRegFiles((SP + 228.S + 6.S).asUInt),
                arrayRegFiles((SP + 228.S + 5.S).asUInt),
                arrayRegFiles((SP + 228.S + 4.S).asUInt),
                arrayRegFiles((SP + 228.S + 3.S).asUInt),
                arrayRegFiles((SP + 228.S + 2.S).asUInt),
                arrayRegFiles((SP + 228.S + 1.S).asUInt),
                arrayRegFiles((SP + 228.S + 0.S).asUInt)
            )

            CP := 9.U
        }
        is(9.U) {
            // $2 = ($1 + 1)
            // goto .10
            generalRegFiles(2.U) := (generalRegFiles(1.U).asSInt + 1.S).asUInt

            CP := 10.U
        }
        is(10.U) {
            // *(SP + 236) = $2 [signed, Z, 8]  // j = $2
            // goto .11
            val __tmp_4 = generalRegFiles(2.U).asUInt
            arrayRegFiles((SP + 236.S + 0.S).asUInt) := __tmp_4(7,0)
            arrayRegFiles((SP + 236.S + 1.S).asUInt) := __tmp_4(15,8)
            arrayRegFiles((SP + 236.S + 2.S).asUInt) := __tmp_4(23,16)
            arrayRegFiles((SP + 236.S + 3.S).asUInt) := __tmp_4(31,24)
            arrayRegFiles((SP + 236.S + 4.S).asUInt) := __tmp_4(39,32)
            arrayRegFiles((SP + 236.S + 5.S).asUInt) := __tmp_4(47,40)
            arrayRegFiles((SP + 236.S + 6.S).asUInt) := __tmp_4(55,48)
            arrayRegFiles((SP + 236.S + 7.S).asUInt) := __tmp_4(63,56)

            CP := 11.U
        }
        is(11.U) {
            // $1 = *(SP + 236) [signed, Z, 8]  // $1 = j
            // $0 = *(SP + 8) [signed, MS[Z, U16], 8]  // $0 = a
            // goto .12
            generalRegFiles(1.U) := Cat(
                arrayRegFiles((SP + 236.S + 7.S).asUInt),
                arrayRegFiles((SP + 236.S + 6.S).asUInt),
                arrayRegFiles((SP + 236.S + 5.S).asUInt),
                arrayRegFiles((SP + 236.S + 4.S).asUInt),
                arrayRegFiles((SP + 236.S + 3.S).asUInt),
                arrayRegFiles((SP + 236.S + 2.S).asUInt),
                arrayRegFiles((SP + 236.S + 1.S).asUInt),
                arrayRegFiles((SP + 236.S + 0.S).asUInt)
            )

            generalRegFiles(0.U) := Cat(
                arrayRegFiles((SP + 8.S + 7.S).asUInt),
                arrayRegFiles((SP + 8.S + 6.S).asUInt),
                arrayRegFiles((SP + 8.S + 5.S).asUInt),
                arrayRegFiles((SP + 8.S + 4.S).asUInt),
                arrayRegFiles((SP + 8.S + 3.S).asUInt),
                arrayRegFiles((SP + 8.S + 2.S).asUInt),
                arrayRegFiles((SP + 8.S + 1.S).asUInt),
                arrayRegFiles((SP + 8.S + 0.S).asUInt)
            )

            CP := 12.U
        }
        is(12.U) {
            // $2 = *($0 + 4) [signed, Z, 8]  // $2 = $0.size
            // goto .13
            val __tmp_5 = (generalRegFiles(0.U).asSInt + 4.S).asSInt
            generalRegFiles(2.U) := Cat(
                arrayRegFiles((__tmp_5 + 7.S).asUInt),
                arrayRegFiles((__tmp_5 + 6.S).asUInt),
                arrayRegFiles((__tmp_5 + 5.S).asUInt),
                arrayRegFiles((__tmp_5 + 4.S).asUInt),
                arrayRegFiles((__tmp_5 + 3.S).asUInt),
                arrayRegFiles((__tmp_5 + 2.S).asUInt),
                arrayRegFiles((__tmp_5 + 1.S).asUInt),
                arrayRegFiles((__tmp_5 + 0.S).asUInt)
            )

            CP := 13.U
        }
        is(13.U) {
            // $3 = ($1 < $2)
            // goto .14
            generalRegFiles(3.U) := (generalRegFiles(1.U) < generalRegFiles(2.U)).asUInt

            CP := 14.U
        }
        is(14.U) {
            // if $3 goto .15 else goto .35
            CP := Mux(generalRegFiles(3.U) === 1.U, 15.U, 35.U)
        }
        is(15.U) {
            // $1 = *(SP + 8) [signed, MS[Z, U16], 8]  // $1 = a
            // $0 = *(SP + 228) [signed, Z, 8]  // $0 = i
            // goto .16
            val __tmp_6 = (SP + 8.S).asSInt
            generalRegFiles(1.U) := Cat(
                arrayRegFiles((__tmp_6 + 7.S).asUInt),
                arrayRegFiles((__tmp_6 + 6.S).asUInt),
                arrayRegFiles((__tmp_6 + 5.S).asUInt),
                arrayRegFiles((__tmp_6 + 4.S).asUInt),
                arrayRegFiles((__tmp_6 + 3.S).asUInt),
                arrayRegFiles((__tmp_6 + 2.S).asUInt),
                arrayRegFiles((__tmp_6 + 1.S).asUInt),
                arrayRegFiles((__tmp_6 + 0.S).asUInt)
            )

            val __tmp_7 = (SP + 228.S).asSInt
            generalRegFiles(0.U) := Cat(
                arrayRegFiles((__tmp_7 + 7.S).asUInt),
                arrayRegFiles((__tmp_7 + 6.S).asUInt),
                arrayRegFiles((__tmp_7 + 5.S).asUInt),
                arrayRegFiles((__tmp_7 + 4.S).asUInt),
                arrayRegFiles((__tmp_7 + 3.S).asUInt),
                arrayRegFiles((__tmp_7 + 2.S).asUInt),
                arrayRegFiles((__tmp_7 + 1.S).asUInt),
                arrayRegFiles((__tmp_7 + 0.S).asUInt)
            )

            CP := 16.U
        }
        is(16.U) {
            // $2 = *(($1 + 12) + (($0 as S64) * 2)) [unsigned, U16, 2]  // $2 = $1($0)
            // $3 = *(SP + 8) [signed, MS[Z, U16], 8]  // $3 = a
            // $4 = *(SP + 236) [signed, Z, 8]  // $4 = j
            // goto .17
            val __tmp_8 = ((generalRegFiles(1.U).asSInt + 12.S) + (generalRegFiles(0.U).asSInt * 2.S)).asSInt
            generalRegFiles(2.U) := Cat(
                arrayRegFiles((__tmp_8 + 1.S).asUInt),
                arrayRegFiles((__tmp_8 + 0.S).asUInt)
            )

            val __tmp_9 = (SP + 8.S).asSInt
            generalRegFiles(3.U) := Cat(
                arrayRegFiles((__tmp_9 + 7.S).asUInt),
                arrayRegFiles((__tmp_9 + 6.S).asUInt),
                arrayRegFiles((__tmp_9 + 5.S).asUInt),
                arrayRegFiles((__tmp_9 + 4.S).asUInt),
                arrayRegFiles((__tmp_9 + 3.S).asUInt),
                arrayRegFiles((__tmp_9 + 2.S).asUInt),
                arrayRegFiles((__tmp_9 + 1.S).asUInt),
                arrayRegFiles((__tmp_9 + 0.S).asUInt)
            )

            val __tmp_10 = (SP + 236.S).asSInt
            generalRegFiles(4.U) := Cat(
                arrayRegFiles((__tmp_10 + 7.S).asUInt),
                arrayRegFiles((__tmp_10 + 6.S).asUInt),
                arrayRegFiles((__tmp_10 + 5.S).asUInt),
                arrayRegFiles((__tmp_10 + 4.S).asUInt),
                arrayRegFiles((__tmp_10 + 3.S).asUInt),
                arrayRegFiles((__tmp_10 + 2.S).asUInt),
                arrayRegFiles((__tmp_10 + 1.S).asUInt),
                arrayRegFiles((__tmp_10 + 0.S).asUInt)
            )

            CP := 17.U
        }
        is(17.U) {
            // $5 = *(($3 + 12) + (($4 as S64) * 2)) [unsigned, U16, 2]  // $5 = $3($4)
            // goto .18
            val __tmp_11 = ((generalRegFiles(3.U).asSInt + 12.S) + (generalRegFiles(4.U).asSInt * 2.S)).asSInt
            generalRegFiles(5.U) := Cat(
                arrayRegFiles((__tmp_11 + 1.S).asUInt),
                arrayRegFiles((__tmp_11 + 0.S).asUInt)
            )

            CP := 18.U
        }
        is(18.U) {
            // $6 = ($2 > $5)
            // goto .19
            generalRegFiles(6.U) := (generalRegFiles(2.U) > generalRegFiles(5.U)).asUInt

            CP := 19.U
        }
        is(19.U) {
            // if $6 goto .20 else goto .30
            CP := Mux(generalRegFiles(6.U) === 1.U, 20.U, 32.U)
        }
        is(20.U) {
            // $1 = *(SP + 8) [signed, MS[Z, U16], 8]  // $1 = a
            // $0 = *(SP + 228) [signed, Z, 8]  // $0 = i
            // $2 = *(SP + 236) [signed, Z, 8]  // $2 = j
            // goto .21
            val __tmp_12 = (SP + 8.S).asSInt
            generalRegFiles(1.U) := Cat(
                arrayRegFiles((__tmp_12 + 7.S).asUInt),
                arrayRegFiles((__tmp_12 + 6.S).asUInt),
                arrayRegFiles((__tmp_12 + 5.S).asUInt),
                arrayRegFiles((__tmp_12 + 4.S).asUInt),
                arrayRegFiles((__tmp_12 + 3.S).asUInt),
                arrayRegFiles((__tmp_12 + 2.S).asUInt),
                arrayRegFiles((__tmp_12 + 1.S).asUInt),
                arrayRegFiles((__tmp_12 + 0.S).asUInt)
            )

            val __tmp_13 = (SP + 228.S).asSInt
            generalRegFiles(0.U) := Cat(
                arrayRegFiles((__tmp_13 + 7.S).asUInt),
                arrayRegFiles((__tmp_13 + 6.S).asUInt),
                arrayRegFiles((__tmp_13 + 5.S).asUInt),
                arrayRegFiles((__tmp_13 + 4.S).asUInt),
                arrayRegFiles((__tmp_13 + 3.S).asUInt),
                arrayRegFiles((__tmp_13 + 2.S).asUInt),
                arrayRegFiles((__tmp_13 + 1.S).asUInt),
                arrayRegFiles((__tmp_13 + 0.S).asUInt)
            )

            val __tmp_14 = (SP + 236.S).asSInt
            generalRegFiles(2.U) := Cat(
                arrayRegFiles((__tmp_14 + 7.S).asUInt),
                arrayRegFiles((__tmp_14 + 6.S).asUInt),
                arrayRegFiles((__tmp_14 + 5.S).asUInt),
                arrayRegFiles((__tmp_14 + 4.S).asUInt),
                arrayRegFiles((__tmp_14 + 3.S).asUInt),
                arrayRegFiles((__tmp_14 + 2.S).asUInt),
                arrayRegFiles((__tmp_14 + 1.S).asUInt),
                arrayRegFiles((__tmp_14 + 0.S).asUInt)
            )

            CP := 21.U
        }
        is(21.U) {
            //  SP = SP + 300
            //  goto .22
            SP := SP + 300.S
            CP := 22.U
        }
        is(22.U) {
            // decl $ret: U64 [@0, 8], a: MS[Z, U16] [@8, 8], i: Z [@16, 8], j: Z [@24, 8]
            // *SP = 30 [signed, U64, 8]  // $ret@0 = 9
            // *(SP + 8) = $1 [signed, S64, 8]  // a = $1
            // *(SP + 16) = $0 [signed, Z, 8]  // i = $0
            // *(SP + 24) = $2 [signed, Z, 8]  // j = $2
            // *(SP - 56) = $0 [unsigned, U64, 8]  // save $0
            // *(SP - 48) = $1 [unsigned, U64, 8]  // save $1
            // *(SP - 40) = $2 [unsigned, U64, 8]  // save $2
            // *(SP - 32) = $3 [unsigned, U64, 8]  // save $3
            // *(SP - 24) = $4 [unsigned, U64, 8]  // save $4
            // *(SP - 16) = $5 [unsigned, U64, 8]  // save $5
            // *(SP - 8) = $6 [unsigned, U64, 8]  // save $6
            // goto .23
            val __tmp_15 = 30.S(64.W).asUInt
            arrayRegFiles((SP + 0.S).asUInt) := __tmp_15(7,0)
            arrayRegFiles((SP + 1.S).asUInt) := __tmp_15(15,8)
            arrayRegFiles((SP + 2.S).asUInt) := __tmp_15(23,16)
            arrayRegFiles((SP + 3.S).asUInt) := __tmp_15(31,24)
            arrayRegFiles((SP + 4.S).asUInt) := __tmp_15(39,32)
            arrayRegFiles((SP + 5.S).asUInt) := __tmp_15(47,40)
            arrayRegFiles((SP + 6.S).asUInt) := __tmp_15(55,48)
            arrayRegFiles((SP + 7.S).asUInt) := __tmp_15(63,56)

            val __tmp_16 = (SP + 8.S).asSInt
            arrayRegFiles((__tmp_16 + 0.S).asUInt) := generalRegFiles(1.U)(7,0)
            arrayRegFiles((__tmp_16 + 1.S).asUInt) := generalRegFiles(1.U)(15,8)
            arrayRegFiles((__tmp_16 + 2.S).asUInt) := generalRegFiles(1.U)(23,16)
            arrayRegFiles((__tmp_16 + 3.S).asUInt) := generalRegFiles(1.U)(31,24)
            arrayRegFiles((__tmp_16 + 4.S).asUInt) := generalRegFiles(1.U)(39,32)
            arrayRegFiles((__tmp_16 + 5.S).asUInt) := generalRegFiles(1.U)(47,40)
            arrayRegFiles((__tmp_16 + 6.S).asUInt) := generalRegFiles(1.U)(55,48)
            arrayRegFiles((__tmp_16 + 7.S).asUInt) := generalRegFiles(1.U)(63,56)

            val __tmp_17 = (SP + 16.S).asSInt
            arrayRegFiles((__tmp_17 + 0.S).asUInt) := generalRegFiles(0.U)(7,0)
            arrayRegFiles((__tmp_17 + 1.S).asUInt) := generalRegFiles(0.U)(15,8)
            arrayRegFiles((__tmp_17 + 2.S).asUInt) := generalRegFiles(0.U)(23,16)
            arrayRegFiles((__tmp_17 + 3.S).asUInt) := generalRegFiles(0.U)(31,24)
            arrayRegFiles((__tmp_17 + 4.S).asUInt) := generalRegFiles(0.U)(39,32)
            arrayRegFiles((__tmp_17 + 5.S).asUInt) := generalRegFiles(0.U)(47,40)
            arrayRegFiles((__tmp_17 + 6.S).asUInt) := generalRegFiles(0.U)(55,48)
            arrayRegFiles((__tmp_17 + 7.S).asUInt) := generalRegFiles(0.U)(63,56)

            val __tmp_18 = (SP + 24.S).asSInt
            arrayRegFiles((__tmp_18 + 0.S).asUInt) := generalRegFiles(2.U)(7,0)
            arrayRegFiles((__tmp_18 + 1.S).asUInt) := generalRegFiles(2.U)(15,8)
            arrayRegFiles((__tmp_18 + 2.S).asUInt) := generalRegFiles(2.U)(23,16)
            arrayRegFiles((__tmp_18 + 3.S).asUInt) := generalRegFiles(2.U)(31,24)
            arrayRegFiles((__tmp_18 + 4.S).asUInt) := generalRegFiles(2.U)(39,32)
            arrayRegFiles((__tmp_18 + 5.S).asUInt) := generalRegFiles(2.U)(47,40)
            arrayRegFiles((__tmp_18 + 6.S).asUInt) := generalRegFiles(2.U)(55,48)
            arrayRegFiles((__tmp_18 + 7.S).asUInt) := generalRegFiles(2.U)(63,56)

            val __tmp_19 = (SP - 56.S).asSInt
            arrayRegFiles((__tmp_19 + 0.S).asUInt) := generalRegFiles(0.U)(7,0)
            arrayRegFiles((__tmp_19 + 1.S).asUInt) := generalRegFiles(0.U)(15,8)
            arrayRegFiles((__tmp_19 + 2.S).asUInt) := generalRegFiles(0.U)(23,16)
            arrayRegFiles((__tmp_19 + 3.S).asUInt) := generalRegFiles(0.U)(31,24)
            arrayRegFiles((__tmp_19 + 4.S).asUInt) := generalRegFiles(0.U)(39,32)
            arrayRegFiles((__tmp_19 + 5.S).asUInt) := generalRegFiles(0.U)(47,40)
            arrayRegFiles((__tmp_19 + 6.S).asUInt) := generalRegFiles(0.U)(55,48)
            arrayRegFiles((__tmp_19 + 7.S).asUInt) := generalRegFiles(0.U)(63,56)

            val __tmp_20 = (SP - 48.S).asSInt
            arrayRegFiles((__tmp_20 + 0.S).asUInt) := generalRegFiles(1.U)(7,0)
            arrayRegFiles((__tmp_20 + 1.S).asUInt) := generalRegFiles(1.U)(15,8)
            arrayRegFiles((__tmp_20 + 2.S).asUInt) := generalRegFiles(1.U)(23,16)
            arrayRegFiles((__tmp_20 + 3.S).asUInt) := generalRegFiles(1.U)(31,24)
            arrayRegFiles((__tmp_20 + 4.S).asUInt) := generalRegFiles(1.U)(39,32)
            arrayRegFiles((__tmp_20 + 5.S).asUInt) := generalRegFiles(1.U)(47,40)
            arrayRegFiles((__tmp_20 + 6.S).asUInt) := generalRegFiles(1.U)(55,48)
            arrayRegFiles((__tmp_20 + 7.S).asUInt) := generalRegFiles(1.U)(63,56)

            val __tmp_21 = (SP - 40.S).asSInt
            arrayRegFiles((__tmp_21 + 0.S).asUInt) := generalRegFiles(2.U)(7,0)
            arrayRegFiles((__tmp_21 + 1.S).asUInt) := generalRegFiles(2.U)(15,8)
            arrayRegFiles((__tmp_21 + 2.S).asUInt) := generalRegFiles(2.U)(23,16)
            arrayRegFiles((__tmp_21 + 3.S).asUInt) := generalRegFiles(2.U)(31,24)
            arrayRegFiles((__tmp_21 + 4.S).asUInt) := generalRegFiles(2.U)(39,32)
            arrayRegFiles((__tmp_21 + 5.S).asUInt) := generalRegFiles(2.U)(47,40)
            arrayRegFiles((__tmp_21 + 6.S).asUInt) := generalRegFiles(2.U)(55,48)
            arrayRegFiles((__tmp_21 + 7.S).asUInt) := generalRegFiles(2.U)(63,56)

            val __tmp_22 = (SP - 32.S).asSInt
            arrayRegFiles((__tmp_22 + 0.S).asUInt) := generalRegFiles(3.U)(7,0)
            arrayRegFiles((__tmp_22 + 1.S).asUInt) := generalRegFiles(3.U)(15,8)
            arrayRegFiles((__tmp_22 + 2.S).asUInt) := generalRegFiles(3.U)(23,16)
            arrayRegFiles((__tmp_22 + 3.S).asUInt) := generalRegFiles(3.U)(31,24)
            arrayRegFiles((__tmp_22 + 4.S).asUInt) := generalRegFiles(3.U)(39,32)
            arrayRegFiles((__tmp_22 + 5.S).asUInt) := generalRegFiles(3.U)(47,40)
            arrayRegFiles((__tmp_22 + 6.S).asUInt) := generalRegFiles(3.U)(55,48)
            arrayRegFiles((__tmp_22 + 7.S).asUInt) := generalRegFiles(3.U)(63,56)

            val __tmp_23 = (SP - 24.S).asSInt
            arrayRegFiles((__tmp_23 + 0.S).asUInt) := generalRegFiles(4.U)(7,0)
            arrayRegFiles((__tmp_23 + 1.S).asUInt) := generalRegFiles(4.U)(15,8)
            arrayRegFiles((__tmp_23 + 2.S).asUInt) := generalRegFiles(4.U)(23,16)
            arrayRegFiles((__tmp_23 + 3.S).asUInt) := generalRegFiles(4.U)(31,24)
            arrayRegFiles((__tmp_23 + 4.S).asUInt) := generalRegFiles(4.U)(39,32)
            arrayRegFiles((__tmp_23 + 5.S).asUInt) := generalRegFiles(4.U)(47,40)
            arrayRegFiles((__tmp_23 + 6.S).asUInt) := generalRegFiles(4.U)(55,48)
            arrayRegFiles((__tmp_23 + 7.S).asUInt) := generalRegFiles(4.U)(63,56)

            val __tmp_24 = (SP - 16.S).asSInt
            arrayRegFiles((__tmp_24 + 0.S).asUInt) := generalRegFiles(5.U)(7,0)
            arrayRegFiles((__tmp_24 + 1.S).asUInt) := generalRegFiles(5.U)(15,8)
            arrayRegFiles((__tmp_24 + 2.S).asUInt) := generalRegFiles(5.U)(23,16)
            arrayRegFiles((__tmp_24 + 3.S).asUInt) := generalRegFiles(5.U)(31,24)
            arrayRegFiles((__tmp_24 + 4.S).asUInt) := generalRegFiles(5.U)(39,32)
            arrayRegFiles((__tmp_24 + 5.S).asUInt) := generalRegFiles(5.U)(47,40)
            arrayRegFiles((__tmp_24 + 6.S).asUInt) := generalRegFiles(5.U)(55,48)
            arrayRegFiles((__tmp_24 + 7.S).asUInt) := generalRegFiles(5.U)(63,56)

            val __tmp_25 = (SP - 8.S).asSInt
            arrayRegFiles((__tmp_25 + 0.S).asUInt) := generalRegFiles(6.U)(7,0)
            arrayRegFiles((__tmp_25 + 1.S).asUInt) := generalRegFiles(6.U)(15,8)
            arrayRegFiles((__tmp_25 + 2.S).asUInt) := generalRegFiles(6.U)(23,16)
            arrayRegFiles((__tmp_25 + 3.S).asUInt) := generalRegFiles(6.U)(31,24)
            arrayRegFiles((__tmp_25 + 4.S).asUInt) := generalRegFiles(6.U)(39,32)
            arrayRegFiles((__tmp_25 + 5.S).asUInt) := generalRegFiles(6.U)(47,40)
            arrayRegFiles((__tmp_25 + 6.S).asUInt) := generalRegFiles(6.U)(55,48)
            arrayRegFiles((__tmp_25 + 7.S).asUInt) := generalRegFiles(6.U)(63,56)

            CP := 23.U
        }
        is(23.U) {
            // decl t: U16 [@32, 2]
            // $0 = *(SP + 8) [signed, MS[Z, U16], 8]  // $0 = a
            // $1 = *(SP + 16) [signed, Z, 8]  // $1 = i
            // goto .24
            val __tmp_26 = (SP + 8.S).asSInt
            generalRegFiles(0.U) := Cat(
                arrayRegFiles((__tmp_26 + 7.S).asUInt),
                arrayRegFiles((__tmp_26 + 6.S).asUInt),
                arrayRegFiles((__tmp_26 + 5.S).asUInt),
                arrayRegFiles((__tmp_26 + 4.S).asUInt),
                arrayRegFiles((__tmp_26 + 3.S).asUInt),
                arrayRegFiles((__tmp_26 + 2.S).asUInt),
                arrayRegFiles((__tmp_26 + 1.S).asUInt),
                arrayRegFiles((__tmp_26 + 0.S).asUInt)
            )

            val __tmp_27 = (SP + 16.S).asSInt
            generalRegFiles(1.U) := Cat(
                arrayRegFiles((__tmp_27 + 7.S).asUInt),
                arrayRegFiles((__tmp_27 + 6.S).asUInt),
                arrayRegFiles((__tmp_27 + 5.S).asUInt),
                arrayRegFiles((__tmp_27 + 4.S).asUInt),
                arrayRegFiles((__tmp_27 + 3.S).asUInt),
                arrayRegFiles((__tmp_27 + 2.S).asUInt),
                arrayRegFiles((__tmp_27 + 1.S).asUInt),
                arrayRegFiles((__tmp_27 + 0.S).asUInt)
            )

            CP := 24.U 
        }
        is(24.U) {
            // $2 = *(($0 + 12) + (($1 as S64) * 2)) [unsigned, U16, 2]  // $2 = $0($1)
            // goto .25
            val __tmp_28 = ((generalRegFiles(0.U).asSInt + 12.S) + (generalRegFiles(1.U).asSInt * 2.S)).asSInt
            generalRegFiles(2.U) := Cat(
                arrayRegFiles((__tmp_28 + 1.S).asUInt),
                arrayRegFiles((__tmp_28 + 0.S).asUInt)
            )

            CP := 25.U
        }
        is(25.U) {
            // *(SP + 32) = $2 [signed, U16, 2]  // t = $2
            // // bubble.sc:6
            // $0 = *(SP + 8) [signed, MS[Z, U16], 8]  // $0 = a
            // $1 = *(SP + 16) [signed, Z, 8]  // $1 = i
            // $2 = *(SP + 8) [signed, MS[Z, U16], 8]  // $2 = a
            // $3 = *(SP + 24) [signed, Z, 8]  // $3 = j
            // goto .26
            val __tmp_29 = (SP + 32.S).asSInt
            arrayRegFiles((__tmp_29 + 0.S).asUInt) := generalRegFiles(2.U)(7,0)
            arrayRegFiles((__tmp_29 + 1.S).asUInt) := generalRegFiles(2.U)(15,8)

            val __tmp_30 = (SP + 8.S).asSInt
            generalRegFiles(0.U) := Cat(
                arrayRegFiles((__tmp_30 + 7.S).asUInt),
                arrayRegFiles((__tmp_30 + 6.S).asUInt),
                arrayRegFiles((__tmp_30 + 5.S).asUInt),
                arrayRegFiles((__tmp_30 + 4.S).asUInt),
                arrayRegFiles((__tmp_30 + 3.S).asUInt),
                arrayRegFiles((__tmp_30 + 2.S).asUInt),
                arrayRegFiles((__tmp_30 + 1.S).asUInt),
                arrayRegFiles((__tmp_30 + 0.S).asUInt)
            )

            val __tmp_31 = (SP + 16.S).asSInt
            generalRegFiles(1.U) := Cat(
                arrayRegFiles((__tmp_31 + 7.S).asUInt),
                arrayRegFiles((__tmp_31 + 6.S).asUInt),
                arrayRegFiles((__tmp_31 + 5.S).asUInt),
                arrayRegFiles((__tmp_31 + 4.S).asUInt),
                arrayRegFiles((__tmp_31 + 3.S).asUInt),
                arrayRegFiles((__tmp_31 + 2.S).asUInt),
                arrayRegFiles((__tmp_31 + 1.S).asUInt),
                arrayRegFiles((__tmp_31 + 0.S).asUInt)
            )

            val __tmp_32 = (SP + 8.S).asSInt
            generalRegFiles(2.U) := Cat(
                arrayRegFiles((__tmp_32 + 7.S).asUInt),
                arrayRegFiles((__tmp_32 + 6.S).asUInt),
                arrayRegFiles((__tmp_32 + 5.S).asUInt),
                arrayRegFiles((__tmp_32 + 4.S).asUInt),
                arrayRegFiles((__tmp_32 + 3.S).asUInt),
                arrayRegFiles((__tmp_32 + 2.S).asUInt),
                arrayRegFiles((__tmp_32 + 1.S).asUInt),
                arrayRegFiles((__tmp_32 + 0.S).asUInt)
            )

            val __tmp_33 = (SP + 24.S).asSInt
            generalRegFiles(3.U) := Cat(
                arrayRegFiles((__tmp_33 + 7.S).asUInt),
                arrayRegFiles((__tmp_33 + 6.S).asUInt),
                arrayRegFiles((__tmp_33 + 5.S).asUInt),
                arrayRegFiles((__tmp_33 + 4.S).asUInt),
                arrayRegFiles((__tmp_33 + 3.S).asUInt),
                arrayRegFiles((__tmp_33 + 2.S).asUInt),
                arrayRegFiles((__tmp_33 + 1.S).asUInt),
                arrayRegFiles((__tmp_33 + 0.S).asUInt)
            )

            CP := 26.U
        }
        is(26.U) {
            // $4 = *(($2 + 12) + (($3 as S64) * 2)) [unsigned, U16, 2]  // $4 = $2($3)
            // goto .27
            val __tmp_34 = ((generalRegFiles(2.U).asSInt + 12.S) + (generalRegFiles(3.U).asSInt * 2.S)).asSInt
            generalRegFiles(4.U) := Cat(
                arrayRegFiles((__tmp_34 + 1.S).asUInt),
                arrayRegFiles((__tmp_34 + 0.S).asUInt)
            )

            CP := 27.U
        }
        is(27.U) {
            // *(($0 + 12) + (($1 as S64) * 2)) = $4 [unsigned, U16, 2]  // $0($1) = $4
            // goto .28
            val __tmp_35 = ((generalRegFiles(0.U).asSInt + 12.S) + (generalRegFiles(1.U).asSInt * 2.S)).asSInt
            arrayRegFiles((__tmp_35 + 0.S).asUInt) := generalRegFiles(4.U)(7,0)
            arrayRegFiles((__tmp_35 + 1.S).asUInt) := generalRegFiles(4.U)(15,8)

            CP := 28.U
        }
        is(28.U) {
            // $0 = *(SP + 8) [signed, MS[Z, U16], 8]  // $0 = a
            // $1 = *(SP + 24) [signed, Z, 8]  // $1 = j
            // $2 = *(SP + 32) [unsigned, U16, 2]  // $2 = t
            // goto .29
            val __tmp_36 = (SP + 8.S).asSInt
            generalRegFiles(0.U) := Cat(
                arrayRegFiles((__tmp_36 + 7.S).asUInt),
                arrayRegFiles((__tmp_36 + 6.S).asUInt),
                arrayRegFiles((__tmp_36 + 5.S).asUInt),
                arrayRegFiles((__tmp_36 + 4.S).asUInt),
                arrayRegFiles((__tmp_36 + 3.S).asUInt),
                arrayRegFiles((__tmp_36 + 2.S).asUInt),
                arrayRegFiles((__tmp_36 + 1.S).asUInt),
                arrayRegFiles((__tmp_36 + 0.S).asUInt)
            )

            val __tmp_37 = (SP + 24.S).asSInt
            generalRegFiles(1.U) := Cat(
                arrayRegFiles((__tmp_37 + 7.S).asUInt),
                arrayRegFiles((__tmp_37 + 6.S).asUInt),
                arrayRegFiles((__tmp_37 + 5.S).asUInt),
                arrayRegFiles((__tmp_37 + 4.S).asUInt),
                arrayRegFiles((__tmp_37 + 3.S).asUInt),
                arrayRegFiles((__tmp_37 + 2.S).asUInt),
                arrayRegFiles((__tmp_37 + 1.S).asUInt),
                arrayRegFiles((__tmp_37 + 0.S).asUInt)
            )

            val __tmp_38 = (SP + 32.S).asSInt
            generalRegFiles(2.U) := Cat(
                arrayRegFiles((__tmp_38 + 1.S).asUInt),
                arrayRegFiles((__tmp_38 + 0.S).asUInt)
            )
            CP := 29.U
        }
        is(29.U) {
            // *(($0 + 12) + (($1 as S64) * 2)) = $2 [unsigned, U16, 2]  // $0($1) = $2
            // // bubble.sc:5
            // undecl t: U16 [@32, 2]
            // goto $ret@0
            val __tmp_39 = ((generalRegFiles(0.U).asSInt + 12.S) + (generalRegFiles(1.U).asSInt * 2.S)).asSInt
            arrayRegFiles((__tmp_39 + 0.S).asUInt) := generalRegFiles(2.U)(7,0)
            arrayRegFiles((__tmp_39 + 1.S).asUInt) := generalRegFiles(2.U)(15,8)

            CP := Cat(
                arrayRegFiles((SP + 0.S + 7.S).asUInt),
                arrayRegFiles((SP + 0.S + 6.S).asUInt),
                arrayRegFiles((SP + 0.S + 5.S).asUInt),
                arrayRegFiles((SP + 0.S + 4.S).asUInt),
                arrayRegFiles((SP + 0.S + 3.S).asUInt),
                arrayRegFiles((SP + 0.S + 2.S).asUInt),
                arrayRegFiles((SP + 0.S + 1.S).asUInt),
                arrayRegFiles((SP + 0.S + 0.S).asUInt)
            )
        }
        is(30.U) {
            // $0 = *(SP - 56) [unsigned, U64, 8]  // restore $0
            // $1 = *(SP - 48) [unsigned, U64, 8]  // restore $1
            // $2 = *(SP - 40) [unsigned, U64, 8]  // restore $2
            // $3 = *(SP - 32) [unsigned, U64, 8]  // restore $3
            // $4 = *(SP - 24) [unsigned, U64, 8]  // restore $4
            // $5 = *(SP - 16) [unsigned, U64, 8]  // restore $5
            // $6 = *(SP - 8) [unsigned, U64, 8]  // restore $6
            // undecl j: Z [@24, 8], i: Z [@16, 8], a: MS[Z, U16] [@8, 8], $ret: U64 [@0, 8]
            // goto .31
            val __tmp_40 = (SP - 56.S).asSInt
            generalRegFiles(0.U) := Cat(
                arrayRegFiles((__tmp_40 + 7.S).asUInt),
                arrayRegFiles((__tmp_40 + 6.S).asUInt),
                arrayRegFiles((__tmp_40 + 5.S).asUInt),
                arrayRegFiles((__tmp_40 + 4.S).asUInt),
                arrayRegFiles((__tmp_40 + 3.S).asUInt),
                arrayRegFiles((__tmp_40 + 2.S).asUInt),
                arrayRegFiles((__tmp_40 + 1.S).asUInt),
                arrayRegFiles((__tmp_40 + 0.S).asUInt)
            )

            val __tmp_41 = (SP - 48.S).asSInt
            generalRegFiles(1.U) := Cat(
                arrayRegFiles((__tmp_41 + 7.S).asUInt),
                arrayRegFiles((__tmp_41 + 6.S).asUInt),
                arrayRegFiles((__tmp_41 + 5.S).asUInt),
                arrayRegFiles((__tmp_41 + 4.S).asUInt),
                arrayRegFiles((__tmp_41 + 3.S).asUInt),
                arrayRegFiles((__tmp_41 + 2.S).asUInt),
                arrayRegFiles((__tmp_41 + 1.S).asUInt),
                arrayRegFiles((__tmp_41 + 0.S).asUInt)
            )

            val __tmp_42 = (SP - 40.S).asSInt
            generalRegFiles(2.U) := Cat(
                arrayRegFiles((__tmp_42 + 7.S).asUInt),
                arrayRegFiles((__tmp_42 + 6.S).asUInt),
                arrayRegFiles((__tmp_42 + 5.S).asUInt),
                arrayRegFiles((__tmp_42 + 4.S).asUInt),
                arrayRegFiles((__tmp_42 + 3.S).asUInt),
                arrayRegFiles((__tmp_42 + 2.S).asUInt),
                arrayRegFiles((__tmp_42 + 1.S).asUInt),
                arrayRegFiles((__tmp_42 + 0.S).asUInt)
            )

            val __tmp_43 = (SP - 32.S).asSInt
            generalRegFiles(3.U) := Cat(
                arrayRegFiles((__tmp_43 + 7.S).asUInt),
                arrayRegFiles((__tmp_43 + 6.S).asUInt),
                arrayRegFiles((__tmp_43 + 5.S).asUInt),
                arrayRegFiles((__tmp_43 + 4.S).asUInt),
                arrayRegFiles((__tmp_43 + 3.S).asUInt),
                arrayRegFiles((__tmp_43 + 2.S).asUInt),
                arrayRegFiles((__tmp_43 + 1.S).asUInt),
                arrayRegFiles((__tmp_43 + 0.S).asUInt)
            )

            val __tmp_44 = (SP - 24.S).asSInt
            generalRegFiles(4.U) := Cat(
                arrayRegFiles((__tmp_44 + 7.S).asUInt),
                arrayRegFiles((__tmp_44 + 6.S).asUInt),
                arrayRegFiles((__tmp_44 + 5.S).asUInt),
                arrayRegFiles((__tmp_44 + 4.S).asUInt),
                arrayRegFiles((__tmp_44 + 3.S).asUInt),
                arrayRegFiles((__tmp_44 + 2.S).asUInt),
                arrayRegFiles((__tmp_44 + 1.S).asUInt),
                arrayRegFiles((__tmp_44 + 0.S).asUInt)
            )

            val __tmp_45 = (SP - 16.S).asSInt
            generalRegFiles(5.U) := Cat(
                arrayRegFiles((__tmp_45 + 7.S).asUInt),
                arrayRegFiles((__tmp_45 + 6.S).asUInt),
                arrayRegFiles((__tmp_45 + 5.S).asUInt),
                arrayRegFiles((__tmp_45 + 4.S).asUInt),
                arrayRegFiles((__tmp_45 + 3.S).asUInt),
                arrayRegFiles((__tmp_45 + 2.S).asUInt),
                arrayRegFiles((__tmp_45 + 1.S).asUInt),
                arrayRegFiles((__tmp_45 + 0.S).asUInt)
            )

            val __tmp_46 = (SP - 8.S).asSInt
            generalRegFiles(6.U) := Cat(
                arrayRegFiles((__tmp_46 + 7.S).asUInt),
                arrayRegFiles((__tmp_46 + 6.S).asUInt),
                arrayRegFiles((__tmp_46 + 5.S).asUInt),
                arrayRegFiles((__tmp_46 + 4.S).asUInt),
                arrayRegFiles((__tmp_46 + 3.S).asUInt),
                arrayRegFiles((__tmp_46 + 2.S).asUInt),
                arrayRegFiles((__tmp_46 + 1.S).asUInt),
                arrayRegFiles((__tmp_46 + 0.S).asUInt)
            )

            CP := 31.U
        }
        is(31.U) {
            // SP = SP - 300
            // goto .32
            SP := SP - 300.S

            CP := 32.U
        }
        is(32.U) {
            // $1 = *(SP + 236) [signed, Z, 8]  // $1 = j
            // goto .33
            val __tmp_47 = (SP + 236.S).asSInt
            generalRegFiles(1.U) := Cat(
                arrayRegFiles((__tmp_47 + 7.S).asUInt),
                arrayRegFiles((__tmp_47 + 6.S).asUInt),
                arrayRegFiles((__tmp_47 + 5.S).asUInt),
                arrayRegFiles((__tmp_47 + 4.S).asUInt),
                arrayRegFiles((__tmp_47 + 3.S).asUInt),
                arrayRegFiles((__tmp_47 + 2.S).asUInt),
                arrayRegFiles((__tmp_47 + 1.S).asUInt),
                arrayRegFiles((__tmp_47 + 0.S).asUInt)
            )

            CP := 33.U
        }
        is(33.U) {
            // $2 = ($1 + 1)
            // goto .34
            generalRegFiles(2.U) := (generalRegFiles(1.U).asSInt + 1.S).asUInt

            CP := 34.U
        }
        is(34.U) {
            // *(SP + 236) = $2 [signed, Z, 8]  // j = $2
            // goto .11
            val __tmp_48 = (SP + 236.S).asSInt
            arrayRegFiles((__tmp_48 + 0.S).asUInt) := generalRegFiles(2.U)(7,0)
            arrayRegFiles((__tmp_48 + 1.S).asUInt) := generalRegFiles(2.U)(15,8)
            arrayRegFiles((__tmp_48 + 2.S).asUInt) := generalRegFiles(2.U)(23,16)
            arrayRegFiles((__tmp_48 + 3.S).asUInt) := generalRegFiles(2.U)(31,24)
            arrayRegFiles((__tmp_48 + 4.S).asUInt) := generalRegFiles(2.U)(39,32)
            arrayRegFiles((__tmp_48 + 5.S).asUInt) := generalRegFiles(2.U)(47,40)
            arrayRegFiles((__tmp_48 + 6.S).asUInt) := generalRegFiles(2.U)(55,48)
            arrayRegFiles((__tmp_48 + 7.S).asUInt) := generalRegFiles(2.U)(63,56)

            CP := 11.U
        }
        is(35.U) {
            // $1 = *(SP + 228) [signed, Z, 8]  // $1 = i
            // goto .36
            val __tmp_49 = (SP + 228.S).asSInt
            generalRegFiles(1.U) := Cat(
                arrayRegFiles((__tmp_49 + 7.S).asUInt),
                arrayRegFiles((__tmp_49 + 6.S).asUInt),
                arrayRegFiles((__tmp_49 + 5.S).asUInt),
                arrayRegFiles((__tmp_49 + 4.S).asUInt),
                arrayRegFiles((__tmp_49 + 3.S).asUInt),
                arrayRegFiles((__tmp_49 + 2.S).asUInt),
                arrayRegFiles((__tmp_49 + 1.S).asUInt),
                arrayRegFiles((__tmp_49 + 0.S).asUInt)
            )

            CP := 36.U
        }
        is(36.U) {
            // $2 = ($1 + 1)
            // goto .37
            generalRegFiles(2.U) := (generalRegFiles(1.U).asSInt + 1.S).asUInt

            CP := 37.U
        }
        is(37.U) {
            // *(SP + 228) = $2 [signed, Z, 8]  // i = $2
            // goto .38
            val __tmp_50 = (SP + 228.S).asSInt
            arrayRegFiles((__tmp_50 + 0.S).asUInt) := generalRegFiles(2.U)(7,0)
            arrayRegFiles((__tmp_50 + 1.S).asUInt) := generalRegFiles(2.U)(15,8)
            arrayRegFiles((__tmp_50 + 2.S).asUInt) := generalRegFiles(2.U)(23,16)
            arrayRegFiles((__tmp_50 + 3.S).asUInt) := generalRegFiles(2.U)(31,24)
            arrayRegFiles((__tmp_50 + 4.S).asUInt) := generalRegFiles(2.U)(39,32)
            arrayRegFiles((__tmp_50 + 5.S).asUInt) := generalRegFiles(2.U)(47,40)
            arrayRegFiles((__tmp_50 + 6.S).asUInt) := generalRegFiles(2.U)(55,48)
            arrayRegFiles((__tmp_50 + 7.S).asUInt) := generalRegFiles(2.U)(63,56)

            CP := 38.U
        }
        is(38.U) {
            // undecl j: Z [@236, 8]
            // goto .4
            CP := 4.U
        }
        is(39.U) {
            //  undecl i: Z [@228, 8]
            //  goto $ret@0
            CP := Cat(
                arrayRegFiles((SP + 0.S + 7.S).asUInt),
                arrayRegFiles((SP + 0.S + 6.S).asUInt),
                arrayRegFiles((SP + 0.S + 5.S).asUInt),
                arrayRegFiles((SP + 0.S + 4.S).asUInt),
                arrayRegFiles((SP + 0.S + 3.S).asUInt),
                arrayRegFiles((SP + 0.S + 2.S).asUInt),
                arrayRegFiles((SP + 0.S + 1.S).asUInt),
                arrayRegFiles((SP + 0.S + 0.S).asUInt)
            )
        }
    }
}