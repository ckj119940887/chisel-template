package GeneralRegFileToBRAM 

import chisel3._
import chisel3.util._
import chisel3.experimental._

class mult (val C_S_AXI_DATA_WIDTH:  Int = 32,
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
        goto .4
        */


        SP := 0.U

        val __tmp_1754 = 0.U
        val __tmp_1755 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_1754 + 0.U) := __tmp_1755(7, 0)

        val __tmp_1756 = 1.U
        val __tmp_1757 = (3.U(16.W)).asUInt
        arrayRegFiles(__tmp_1756 + 0.U) := __tmp_1757(7, 0)
        arrayRegFiles(__tmp_1756 + 1.U) := __tmp_1757(15, 8)

        CP := 4.U
      }

      is(4.U) {
        /*
        decl r: U64 [@27, 8]
        *(SP + 27) = 0 [signed, U64, 8]  // r = 0
        decl i: U64 [@35, 8]
        *(SP + 35) = 0 [signed, U64, 8]  // i = 0
        goto .5
        */


        val __tmp_1758 = SP + 27.U
        val __tmp_1759 = (0.S(64.W)).asUInt
        arrayRegFiles(__tmp_1758 + 0.U) := __tmp_1759(7, 0)
        arrayRegFiles(__tmp_1758 + 1.U) := __tmp_1759(15, 8)
        arrayRegFiles(__tmp_1758 + 2.U) := __tmp_1759(23, 16)
        arrayRegFiles(__tmp_1758 + 3.U) := __tmp_1759(31, 24)
        arrayRegFiles(__tmp_1758 + 4.U) := __tmp_1759(39, 32)
        arrayRegFiles(__tmp_1758 + 5.U) := __tmp_1759(47, 40)
        arrayRegFiles(__tmp_1758 + 6.U) := __tmp_1759(55, 48)
        arrayRegFiles(__tmp_1758 + 7.U) := __tmp_1759(63, 56)

        val __tmp_1760 = SP + 35.U
        val __tmp_1761 = (0.S(64.W)).asUInt
        arrayRegFiles(__tmp_1760 + 0.U) := __tmp_1761(7, 0)
        arrayRegFiles(__tmp_1760 + 1.U) := __tmp_1761(15, 8)
        arrayRegFiles(__tmp_1760 + 2.U) := __tmp_1761(23, 16)
        arrayRegFiles(__tmp_1760 + 3.U) := __tmp_1761(31, 24)
        arrayRegFiles(__tmp_1760 + 4.U) := __tmp_1761(39, 32)
        arrayRegFiles(__tmp_1760 + 5.U) := __tmp_1761(47, 40)
        arrayRegFiles(__tmp_1760 + 6.U) := __tmp_1761(55, 48)
        arrayRegFiles(__tmp_1760 + 7.U) := __tmp_1761(63, 56)

        CP := 5.U
      }

      is(5.U) {
        /*
        $0 = *(SP + 35) [unsigned, U64, 8]  // $0 = i
        $1 = *(SP + 11) [unsigned, U64, 8]  // $1 = x
        goto .6
        */


        val __tmp_1762 = (SP + 35.U).asUInt
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

        val __tmp_1763 = (SP + 11.U).asUInt
        generalRegFiles(1.U) := Cat(
          arrayRegFiles(__tmp_1763 + 7.U),
          arrayRegFiles(__tmp_1763 + 6.U),
          arrayRegFiles(__tmp_1763 + 5.U),
          arrayRegFiles(__tmp_1763 + 4.U),
          arrayRegFiles(__tmp_1763 + 3.U),
          arrayRegFiles(__tmp_1763 + 2.U),
          arrayRegFiles(__tmp_1763 + 1.U),
          arrayRegFiles(__tmp_1763 + 0.U)
        ).asUInt

        CP := 6.U
      }

      is(6.U) {
        /*
        $2 = ($0 < $1)
        goto .7
        */


        generalRegFiles(2.U) := (generalRegFiles(0.U) < generalRegFiles(1.U)).asUInt
        CP := 7.U
      }

      is(7.U) {
        /*
        if $2 goto .8 else goto .14
        */


        CP := Mux((generalRegFiles(2.U).asUInt) === 1.U, 8.U, 14.U)
      }

      is(8.U) {
        /*
        $0 = *(SP + 27) [unsigned, U64, 8]  // $0 = r
        $1 = *(SP + 19) [unsigned, U64, 8]  // $1 = y
        goto .9
        */


        val __tmp_1764 = (SP + 27.U).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_1764 + 7.U),
          arrayRegFiles(__tmp_1764 + 6.U),
          arrayRegFiles(__tmp_1764 + 5.U),
          arrayRegFiles(__tmp_1764 + 4.U),
          arrayRegFiles(__tmp_1764 + 3.U),
          arrayRegFiles(__tmp_1764 + 2.U),
          arrayRegFiles(__tmp_1764 + 1.U),
          arrayRegFiles(__tmp_1764 + 0.U)
        ).asUInt

        val __tmp_1765 = (SP + 19.U).asUInt
        generalRegFiles(1.U) := Cat(
          arrayRegFiles(__tmp_1765 + 7.U),
          arrayRegFiles(__tmp_1765 + 6.U),
          arrayRegFiles(__tmp_1765 + 5.U),
          arrayRegFiles(__tmp_1765 + 4.U),
          arrayRegFiles(__tmp_1765 + 3.U),
          arrayRegFiles(__tmp_1765 + 2.U),
          arrayRegFiles(__tmp_1765 + 1.U),
          arrayRegFiles(__tmp_1765 + 0.U)
        ).asUInt

        CP := 9.U
      }

      is(9.U) {
        /*
        $2 = ($0 + $1)
        goto .10
        */


        generalRegFiles(2.U) := generalRegFiles(0.U) + generalRegFiles(1.U)
        CP := 10.U
      }

      is(10.U) {
        /*
        *(SP + 27) = $2 [signed, U64, 8]  // r = $2
        goto .11
        */


        val __tmp_1766 = SP + 27.U
        val __tmp_1767 = (generalRegFiles(2.U)).asUInt
        arrayRegFiles(__tmp_1766 + 0.U) := __tmp_1767(7, 0)
        arrayRegFiles(__tmp_1766 + 1.U) := __tmp_1767(15, 8)
        arrayRegFiles(__tmp_1766 + 2.U) := __tmp_1767(23, 16)
        arrayRegFiles(__tmp_1766 + 3.U) := __tmp_1767(31, 24)
        arrayRegFiles(__tmp_1766 + 4.U) := __tmp_1767(39, 32)
        arrayRegFiles(__tmp_1766 + 5.U) := __tmp_1767(47, 40)
        arrayRegFiles(__tmp_1766 + 6.U) := __tmp_1767(55, 48)
        arrayRegFiles(__tmp_1766 + 7.U) := __tmp_1767(63, 56)

        CP := 11.U
      }

      is(11.U) {
        /*
        $0 = *(SP + 35) [unsigned, U64, 8]  // $0 = i
        goto .12
        */


        val __tmp_1768 = (SP + 35.U).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_1768 + 7.U),
          arrayRegFiles(__tmp_1768 + 6.U),
          arrayRegFiles(__tmp_1768 + 5.U),
          arrayRegFiles(__tmp_1768 + 4.U),
          arrayRegFiles(__tmp_1768 + 3.U),
          arrayRegFiles(__tmp_1768 + 2.U),
          arrayRegFiles(__tmp_1768 + 1.U),
          arrayRegFiles(__tmp_1768 + 0.U)
        ).asUInt

        CP := 12.U
      }

      is(12.U) {
        /*
        $1 = ($0 + 1)
        goto .13
        */


        generalRegFiles(1.U) := generalRegFiles(0.U) + 1.U
        CP := 13.U
      }

      is(13.U) {
        /*
        *(SP + 35) = $1 [signed, U64, 8]  // i = $1
        goto .5
        */


        val __tmp_1769 = SP + 35.U
        val __tmp_1770 = (generalRegFiles(1.U)).asUInt
        arrayRegFiles(__tmp_1769 + 0.U) := __tmp_1770(7, 0)
        arrayRegFiles(__tmp_1769 + 1.U) := __tmp_1770(15, 8)
        arrayRegFiles(__tmp_1769 + 2.U) := __tmp_1770(23, 16)
        arrayRegFiles(__tmp_1769 + 3.U) := __tmp_1770(31, 24)
        arrayRegFiles(__tmp_1769 + 4.U) := __tmp_1770(39, 32)
        arrayRegFiles(__tmp_1769 + 5.U) := __tmp_1770(47, 40)
        arrayRegFiles(__tmp_1769 + 6.U) := __tmp_1770(55, 48)
        arrayRegFiles(__tmp_1769 + 7.U) := __tmp_1770(63, 56)

        CP := 5.U
      }

      is(14.U) {
        /*
        $0 = *(SP + 27) [unsigned, U64, 8]  // $0 = r
        goto .15
        */


        val __tmp_1771 = (SP + 27.U).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_1771 + 7.U),
          arrayRegFiles(__tmp_1771 + 6.U),
          arrayRegFiles(__tmp_1771 + 5.U),
          arrayRegFiles(__tmp_1771 + 4.U),
          arrayRegFiles(__tmp_1771 + 3.U),
          arrayRegFiles(__tmp_1771 + 2.U),
          arrayRegFiles(__tmp_1771 + 1.U),
          arrayRegFiles(__tmp_1771 + 0.U)
        ).asUInt

        CP := 15.U
      }

      is(15.U) {
        /*
        **(SP + 1) = $0 [unsigned, U64, 8]  // $res = $0
        goto $ret@0
        */


        val __tmp_1772 = Cat(
          arrayRegFiles(SP + 1.U + 1.U),
          arrayRegFiles(SP + 1.U + 0.U)
        )
        val __tmp_1773 = (generalRegFiles(0.U)).asUInt
        arrayRegFiles(__tmp_1772 + 0.U) := __tmp_1773(7, 0)
        arrayRegFiles(__tmp_1772 + 1.U) := __tmp_1773(15, 8)
        arrayRegFiles(__tmp_1772 + 2.U) := __tmp_1773(23, 16)
        arrayRegFiles(__tmp_1772 + 3.U) := __tmp_1773(31, 24)
        arrayRegFiles(__tmp_1772 + 4.U) := __tmp_1773(39, 32)
        arrayRegFiles(__tmp_1772 + 5.U) := __tmp_1773(47, 40)
        arrayRegFiles(__tmp_1772 + 6.U) := __tmp_1773(55, 48)
        arrayRegFiles(__tmp_1772 + 7.U) := __tmp_1773(63, 56)

        CP := Cat(
          arrayRegFiles(SP + 0.U + 0.U)
        )

      }

    }

}
