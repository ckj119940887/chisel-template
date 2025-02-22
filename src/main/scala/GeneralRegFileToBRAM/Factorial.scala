package GeneralRegFileToBRAM 

import chisel3._
import chisel3.util._
import chisel3.experimental._

class Factorial (val C_S_AXI_DATA_WIDTH: Int = 32,
                 val C_S_AXI_ADDR_WIDTH: Int = 32,
                 val ARRAY_REG_WIDTH: Int = 8,
                 val ARRAY_REG_DEPTH: Int = 1024,
                 val GENERAL_REG_WIDTH: Int = 64,
                 val GENERAL_REG_DEPTH: Int = 64,
                 val STACK_POINTER_WIDTH: Int = 64,
                 val CODE_POINTER_WIDTH: Int = 64,
                 val NUM_STATES: Int = 10) extends Module {
    val io = IO(new Bundle{
        val valid = Input(Bool())
        val ready = Output(Bool())
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
            // *8 = 16 [signed, S64, 8]  // data address of $res
            // goto .3
            
            SP := 0.S

            val __tmp_0 = 0.U(64.W).asUInt
            arrayRegFiles(0.U) := __tmp_0(7,0) 
            arrayRegFiles(1.U) := __tmp_0(15,8) 
            arrayRegFiles(2.U) := __tmp_0(23,16) 
            arrayRegFiles(3.U) := __tmp_0(31,24) 
            arrayRegFiles(4.U) := __tmp_0(39,32) 
            arrayRegFiles(5.U) := __tmp_0(47,40) 
            arrayRegFiles(6.U) := __tmp_0(55,48) 
            arrayRegFiles(7.U) := __tmp_0(63,56) 

            val __tmp_1 = 16.S(64.W).asUInt
            arrayRegFiles(8.U)  := __tmp_1(7,0) 
            arrayRegFiles(9.U)  := __tmp_1(15,8) 
            arrayRegFiles(10.U) := __tmp_1(23,16) 
            arrayRegFiles(11.U) := __tmp_1(31,24) 
            arrayRegFiles(12.U) := __tmp_1(39,32) 
            arrayRegFiles(13.U) := __tmp_1(47,40) 
            arrayRegFiles(14.U) := __tmp_1(55,48) 
            arrayRegFiles(15.U) := __tmp_1(63,56) 

            CP := 3.U
        }
        is(3.U) {
            // $0 = *(SP + 20) [unsigned, U32, 4]  // $0 = n
            // goto .4
            generalRegFiles(0.U) := Cat(0.U,
                                        arrayRegFiles((SP + 23.S).asUInt),
                                        arrayRegFiles((SP + 22.S).asUInt),
                                        arrayRegFiles((SP + 21.S).asUInt),
                                        arrayRegFiles((SP + 20.S).asUInt))

            CP := 4.U
        }
        is(4.U) {
            // $1 = ($0 ≡ 0)
            // goto .5
            val __tmp_2 = (generalRegFiles(0.U) === 0.U).asUInt
            generalRegFiles(1.U) := __tmp_2
            CP := 5.U
        }
        is(5.U) {
            //if $1 goto .6 else goto .7
            CP := Mux(generalRegFiles(1.U) === 1.U, 6.U, 7.U)
        }
        is(6.U) {
            // **(SP + 8) = 1 [unsigned, U32, 4]  // $res = 1
            // goto $ret@0
            val __tmp_3 = Cat(
                arrayRegFiles((SP + 8.S + 7.S).asUInt),
                arrayRegFiles((SP + 8.S + 6.S).asUInt),
                arrayRegFiles((SP + 8.S + 5.S).asUInt),
                arrayRegFiles((SP + 8.S + 4.S).asUInt),
                arrayRegFiles((SP + 8.S + 3.S).asUInt),
                arrayRegFiles((SP + 8.S + 2.S).asUInt),
                arrayRegFiles((SP + 8.S + 1.S).asUInt),
                arrayRegFiles((SP + 8.S + 0.S).asUInt)
            ).asSInt

            val __tmp_4 = 1.S(64.W).asUInt
            arrayRegFiles((__tmp_3 + 0.S).asUInt) := __tmp_4(7,0)
            arrayRegFiles((__tmp_3 + 1.S).asUInt) := __tmp_4(15,8)
            arrayRegFiles((__tmp_3 + 2.S).asUInt) := __tmp_4(23,16)
            arrayRegFiles((__tmp_3 + 3.S).asUInt) := __tmp_4(31,24)

            CP := Cat(
                arrayRegFiles((SP + 7.S).asUInt),
                arrayRegFiles((SP + 6.S).asUInt),
                arrayRegFiles((SP + 5.S).asUInt),
                arrayRegFiles((SP + 4.S).asUInt),
                arrayRegFiles((SP + 3.S).asUInt),
                arrayRegFiles((SP + 2.S).asUInt),
                arrayRegFiles((SP + 1.S).asUInt),
                arrayRegFiles((SP + 0.S).asUInt)
            ).asUInt
        }
        is(7.U) {
            // $0 = *(SP + 20) [unsigned, U32, 4]  // $0 = n
            // $2 = *(SP + 20) [unsigned, U32, 4]  // $2 = n
            // goto .8            
            generalRegFiles(0.U) := Cat(
                                        0.U,
                                        arrayRegFiles((SP + 23.S).asUInt),
                                        arrayRegFiles((SP + 22.S).asUInt),
                                        arrayRegFiles((SP + 21.S).asUInt),
                                        arrayRegFiles((SP + 20.S).asUInt)
                                    ).asUInt
            generalRegFiles(2.U) := Cat(
                                        0.U,
                                        arrayRegFiles((SP + 23.S).asUInt),
                                        arrayRegFiles((SP + 22.S).asUInt),
                                        arrayRegFiles((SP + 21.S).asUInt),
                                        arrayRegFiles((SP + 20.S).asUInt)
                                    ).asUInt
            CP := 8.U
        }
        is(8.U) {
            // $3 = ($2 - 1)
            // alloc factorial$res@[9,14].A651BB8C: U32 [@24, 4]
            // goto .9
            generalRegFiles(3.U) := generalRegFiles(2.U) - 1.U
            CP := 9.U
        }
        is(9.U) {
            // SP = SP + 76 
            // goto .10
            SP := SP + 76.S
            CP := 10.U
        }
        is(10.U) {
            // decl $ret: U64 [@0, 8], $res: S64 [@8, 12], n: U32 [@20, 4]
            // *SP = 11 [signed, U64, 8]  // $ret@0 = 5
            // *(SP + 8) = (SP - 52) [signed, S64, 8]  // $res@8 = -52
            // *(SP + 20) = $3 [signed, U32, 4]  // n = $3
            // *(SP - 48) = $0 [unsigned, U64, 8]  // save $0
            // *(SP - 40) = $1 [unsigned, U64, 8]  // save $1
            // *(SP - 32) = $2 [unsigned, U64, 8]  // save $2
            // *(SP - 24) = $3 [unsigned, U64, 8]  // save $3
            // goto .3
            val __tmp_5 = 11.S(64.W).asUInt
            arrayRegFiles((SP + 0.S).asUInt) := __tmp_5(7,0)
            arrayRegFiles((SP + 1.S).asUInt) := __tmp_5(15,8)
            arrayRegFiles((SP + 2.S).asUInt) := __tmp_5(23,16)
            arrayRegFiles((SP + 3.S).asUInt) := __tmp_5(31,24)
            arrayRegFiles((SP + 4.S).asUInt) := __tmp_5(39,32)
            arrayRegFiles((SP + 5.S).asUInt) := __tmp_5(47,40)
            arrayRegFiles((SP + 6.S).asUInt) := __tmp_5(55,48)
            arrayRegFiles((SP + 7.S).asUInt) := __tmp_5(63,56)

            val __tmp_6 = (SP + (-52).S).asUInt
            arrayRegFiles((SP + 8.S + 0.S).asUInt) := __tmp_6(7,0)
            arrayRegFiles((SP + 8.S + 1.S).asUInt) := __tmp_6(15,8)
            arrayRegFiles((SP + 8.S + 2.S).asUInt) := __tmp_6(23,16)
            arrayRegFiles((SP + 8.S + 3.S).asUInt) := __tmp_6(31,24)
            arrayRegFiles((SP + 8.S + 4.S).asUInt) := __tmp_6(39,32)
            arrayRegFiles((SP + 8.S + 5.S).asUInt) := __tmp_6(47,40)
            arrayRegFiles((SP + 8.S + 6.S).asUInt) := __tmp_6(55,48)
            arrayRegFiles((SP + 8.S + 7.S).asUInt) := __tmp_6(63,56)

            val __tmp_7 = generalRegFiles(3.U).asUInt
            arrayRegFiles((SP + 20.S + 0.S).asUInt) := __tmp_7(7,0)
            arrayRegFiles((SP + 20.S + 1.S).asUInt) := __tmp_7(15,8)
            arrayRegFiles((SP + 20.S + 2.S).asUInt) := __tmp_7(23,16)
            arrayRegFiles((SP + 20.S + 3.S).asUInt) := __tmp_7(31,24)

            val __tmp_8 = generalRegFiles(0.U).asUInt
            arrayRegFiles((SP - 48.S + 0.S).asUInt) := __tmp_8(7,0)
            arrayRegFiles((SP - 48.S + 1.S).asUInt) := __tmp_8(15,8)
            arrayRegFiles((SP - 48.S + 2.S).asUInt) := __tmp_8(23,16)
            arrayRegFiles((SP - 48.S + 3.S).asUInt) := __tmp_8(31,24)
            arrayRegFiles((SP - 48.S + 4.S).asUInt) := __tmp_8(39,32)
            arrayRegFiles((SP - 48.S + 5.S).asUInt) := __tmp_8(47,40)
            arrayRegFiles((SP - 48.S + 6.S).asUInt) := __tmp_8(55,48)
            arrayRegFiles((SP - 48.S + 7.S).asUInt) := __tmp_8(63,56)

            val __tmp_9 = generalRegFiles(1.U).asUInt
            arrayRegFiles((SP - 40.S + 0.S).asUInt) := __tmp_9(7,0)
            arrayRegFiles((SP - 40.S + 1.S).asUInt) := __tmp_9(15,8)
            arrayRegFiles((SP - 40.S + 2.S).asUInt) := __tmp_9(23,16)
            arrayRegFiles((SP - 40.S + 3.S).asUInt) := __tmp_9(31,24)
            arrayRegFiles((SP - 40.S + 4.S).asUInt) := __tmp_9(39,32)
            arrayRegFiles((SP - 40.S + 5.S).asUInt) := __tmp_9(47,40)
            arrayRegFiles((SP - 40.S + 6.S).asUInt) := __tmp_9(55,48)
            arrayRegFiles((SP - 40.S + 7.S).asUInt) := __tmp_9(63,56)

            val __tmp_10 = generalRegFiles(2.U).asUInt
            arrayRegFiles((SP - 32.S + 0.S).asUInt) := __tmp_10(7,0)
            arrayRegFiles((SP - 32.S + 1.S).asUInt) := __tmp_10(15,8)
            arrayRegFiles((SP - 32.S + 2.S).asUInt) := __tmp_10(23,16)
            arrayRegFiles((SP - 32.S + 3.S).asUInt) := __tmp_10(31,24)
            arrayRegFiles((SP - 32.S + 4.S).asUInt) := __tmp_10(39,32)
            arrayRegFiles((SP - 32.S + 5.S).asUInt) := __tmp_10(47,40)
            arrayRegFiles((SP - 32.S + 6.S).asUInt) := __tmp_10(55,48)
            arrayRegFiles((SP - 32.S + 7.S).asUInt) := __tmp_10(63,56)

            val __tmp_11 = generalRegFiles(3.U).asUInt
            arrayRegFiles((SP - 24.S + 0.S).asUInt) := __tmp_11(7,0)
            arrayRegFiles((SP - 24.S + 1.S).asUInt) := __tmp_11(15,8)
            arrayRegFiles((SP - 24.S + 2.S).asUInt) := __tmp_11(23,16)
            arrayRegFiles((SP - 24.S + 3.S).asUInt) := __tmp_11(31,24)
            arrayRegFiles((SP - 24.S + 4.S).asUInt) := __tmp_11(39,32)
            arrayRegFiles((SP - 24.S + 5.S).asUInt) := __tmp_11(47,40)
            arrayRegFiles((SP - 24.S + 6.S).asUInt) := __tmp_11(55,48)
            arrayRegFiles((SP - 24.S + 7.S).asUInt) := __tmp_11(63,56)

            CP := 3.U
        }
        is(11.U) {
            // $0 = *(SP - 48) [unsigned, U64, 8]  // restore $0
            // $1 = *(SP - 40) [unsigned, U64, 8]  // restore $1
            // $2 = *(SP - 32) [unsigned, U64, 8]  // restore $2
            // $3 = *(SP - 24) [unsigned, U64, 8]  // restore $3
            // $4 = **(SP + 8) [unsigned, S64, 8]  // $4 = $ret
            // undecl n: U32 [@20, 4], $res: S64 [@8, 12], $ret: U64 [@0, 8]
            // goto .12

            generalRegFiles(0.U) := Cat(
                arrayRegFiles((SP - 48.S + 7.S).asUInt),
                arrayRegFiles((SP - 48.S + 6.S).asUInt),
                arrayRegFiles((SP - 48.S + 5.S).asUInt),
                arrayRegFiles((SP - 48.S + 4.S).asUInt),
                arrayRegFiles((SP - 48.S + 3.S).asUInt),
                arrayRegFiles((SP - 48.S + 2.S).asUInt),
                arrayRegFiles((SP - 48.S + 1.S).asUInt),
                arrayRegFiles((SP - 48.S + 0.S).asUInt)
            )

            generalRegFiles(1.U) := Cat(
                arrayRegFiles((SP - 40.S + 7.S).asUInt),
                arrayRegFiles((SP - 40.S + 6.S).asUInt),
                arrayRegFiles((SP - 40.S + 5.S).asUInt),
                arrayRegFiles((SP - 40.S + 4.S).asUInt),
                arrayRegFiles((SP - 40.S + 3.S).asUInt),
                arrayRegFiles((SP - 40.S + 2.S).asUInt),
                arrayRegFiles((SP - 40.S + 1.S).asUInt),
                arrayRegFiles((SP - 40.S + 0.S).asUInt)
            )

            generalRegFiles(2.U) := Cat(
                arrayRegFiles((SP - 32.S + 7.S).asUInt),
                arrayRegFiles((SP - 32.S + 6.S).asUInt),
                arrayRegFiles((SP - 32.S + 5.S).asUInt),
                arrayRegFiles((SP - 32.S + 4.S).asUInt),
                arrayRegFiles((SP - 32.S + 3.S).asUInt),
                arrayRegFiles((SP - 32.S + 2.S).asUInt),
                arrayRegFiles((SP - 32.S + 1.S).asUInt),
                arrayRegFiles((SP - 32.S + 0.S).asUInt)
            )

            generalRegFiles(3.U) := Cat(
                arrayRegFiles((SP - 24.S + 7.S).asUInt),
                arrayRegFiles((SP - 24.S + 6.S).asUInt),
                arrayRegFiles((SP - 24.S + 5.S).asUInt),
                arrayRegFiles((SP - 24.S + 4.S).asUInt),
                arrayRegFiles((SP - 24.S + 3.S).asUInt),
                arrayRegFiles((SP - 24.S + 2.S).asUInt),
                arrayRegFiles((SP - 24.S + 1.S).asUInt),
                arrayRegFiles((SP - 24.S + 0.S).asUInt)
            )

            val __tmp_12 = Cat(
                arrayRegFiles((SP + 8.S + 7.S).asUInt),
                arrayRegFiles((SP + 8.S + 6.S).asUInt),
                arrayRegFiles((SP + 8.S + 5.S).asUInt),
                arrayRegFiles((SP + 8.S + 4.S).asUInt),
                arrayRegFiles((SP + 8.S + 3.S).asUInt),
                arrayRegFiles((SP + 8.S + 2.S).asUInt),
                arrayRegFiles((SP + 8.S + 1.S).asUInt),
                arrayRegFiles((SP + 8.S + 0.S).asUInt)
            ).asSInt

            generalRegFiles(4.U) := Cat(
                arrayRegFiles((__tmp_12 + 7.S).asUInt),
                arrayRegFiles((__tmp_12 + 6.S).asUInt),
                arrayRegFiles((__tmp_12 + 5.S).asUInt),
                arrayRegFiles((__tmp_12 + 4.S).asUInt),
                arrayRegFiles((__tmp_12 + 3.S).asUInt),
                arrayRegFiles((__tmp_12 + 2.S).asUInt),
                arrayRegFiles((__tmp_12 + 1.S).asUInt),
                arrayRegFiles((__tmp_12 + 0.S).asUInt)
            )

            CP := 12.U
        }
        is(12.U) {
            // SP = SP - 76 
            // goto .13
            SP := SP - 76.S
            CP := 13.U
        }
        is(13.U) {
            // $5 = ($0 * $4)
            // unalloc factorial$res@[9,14].A651BB8C: U32 [@24, 4]
            // goto .14
            generalRegFiles(5.U) := generalRegFiles(0.U) * generalRegFiles(4.U)
            CP := 14.U
        }
        is(14.U) {
            // **(SP + 8) = $5 [unsigned, U32, 4]  // $res = $5
            // goto $ret@0
            val __tmp_13 = Cat(
                arrayRegFiles((SP + 8.S + 7.S).asUInt),
                arrayRegFiles((SP + 8.S + 6.S).asUInt),
                arrayRegFiles((SP + 8.S + 5.S).asUInt),
                arrayRegFiles((SP + 8.S + 4.S).asUInt),
                arrayRegFiles((SP + 8.S + 3.S).asUInt),
                arrayRegFiles((SP + 8.S + 2.S).asUInt),
                arrayRegFiles((SP + 8.S + 1.S).asUInt),
                arrayRegFiles((SP + 8.S + 0.S).asUInt)
            ).asSInt

            val __tmp_14 = generalRegFiles(5.U).asSInt
            arrayRegFiles((__tmp_13 + 0.S).asUInt) := __tmp_14(7,0)
            arrayRegFiles((__tmp_13 + 1.S).asUInt) := __tmp_14(15,8)
            arrayRegFiles((__tmp_13 + 2.S).asUInt) := __tmp_14(23,16)
            arrayRegFiles((__tmp_13 + 3.S).asUInt) := __tmp_14(31,24)

            CP := Cat(
                arrayRegFiles((SP + 7.S).asUInt),
                arrayRegFiles((SP + 6.S).asUInt),
                arrayRegFiles((SP + 5.S).asUInt),
                arrayRegFiles((SP + 4.S).asUInt),
                arrayRegFiles((SP + 3.S).asUInt),
                arrayRegFiles((SP + 2.S).asUInt),
                arrayRegFiles((SP + 1.S).asUInt),
                arrayRegFiles((SP + 0.S).asUInt)
            )
        }
    }
}