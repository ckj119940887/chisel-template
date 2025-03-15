package GeneralRegFileToBRAM 

import chisel3._
import chisel3.util._
import chisel3.experimental._

class square (val C_S_AXI_DATA_WIDTH:  Int = 32,
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
        *1 = 3 [unsigned, SP, 2]  // data address of seq (size = 60)
        goto .4
        */


        SP := 0.U

        val __tmp_1814 = 0.U
        val __tmp_1815 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_1814 + 0.U) := __tmp_1815(7, 0)

        val __tmp_1816 = 1.U
        val __tmp_1817 = (3.U(16.W)).asUInt
        arrayRegFiles(__tmp_1816 + 0.U) := __tmp_1817(7, 0)
        arrayRegFiles(__tmp_1816 + 1.U) := __tmp_1817(15, 8)

        CP := 4.U
      }

      is(4.U) {
        /*
        decl i: I0_5 [@63, 1]
        *(SP + 63) = 0 [signed, I0_5, 1]  // i = 0
        goto .5
        */


        val __tmp_1818 = SP + 63.U
        val __tmp_1819 = (0.S(8.W)).asUInt
        arrayRegFiles(__tmp_1818 + 0.U) := __tmp_1819(7, 0)

        CP := 5.U
      }

      is(5.U) {
        /*
        $0 = *(SP + 63) [unsigned, I0_5, 1]  // $0 = i
        goto .6
        */


        val __tmp_1820 = (SP + 63.U).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_1820 + 0.U)
        ).asUInt

        CP := 6.U
      }

      is(6.U) {
        /*
        $1 = ($0 < 5)
        goto .7
        */


        generalRegFiles(1.U) := (generalRegFiles(0.U) < 5.U).asUInt
        CP := 7.U
      }

      is(7.U) {
        /*
        if $1 goto .8 else goto .23
        */


        CP := Mux((generalRegFiles(1.U).asUInt) === 1.U, 8.U, 23.U)
      }

      is(8.U) {
        /*
        $0 = *(SP + 1) [unsigned, MS[I0_5, U64], 2]  // $0 = seq
        $1 = *(SP + 63) [unsigned, I0_5, 1]  // $1 = i
        $3 = *(SP + 1) [unsigned, MS[I0_5, U64], 2]  // $3 = seq
        $2 = *(SP + 63) [unsigned, I0_5, 1]  // $2 = i
        goto .9
        */


        val __tmp_1821 = (SP + 1.U).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_1821 + 1.U),
          arrayRegFiles(__tmp_1821 + 0.U)
        ).asUInt

        val __tmp_1822 = (SP + 63.U).asUInt
        generalRegFiles(1.U) := Cat(
          arrayRegFiles(__tmp_1822 + 0.U)
        ).asUInt

        val __tmp_1823 = (SP + 1.U).asUInt
        generalRegFiles(3.U) := Cat(
          arrayRegFiles(__tmp_1823 + 1.U),
          arrayRegFiles(__tmp_1823 + 0.U)
        ).asUInt

        val __tmp_1824 = (SP + 63.U).asUInt
        generalRegFiles(2.U) := Cat(
          arrayRegFiles(__tmp_1824 + 0.U)
        ).asUInt

        CP := 9.U
      }

      is(9.U) {
        /*
        if ((0 <= $2) & (($2 as Z) <= *($3 + 4))) goto .10 else goto .1
        */


        CP := Mux(((0.U <= generalRegFiles(2.U)).asUInt & (generalRegFiles(2.U).asSInt <= Cat(
                            arrayRegFiles(generalRegFiles(3.U) + 4.U + 7.U),
                            arrayRegFiles(generalRegFiles(3.U) + 4.U + 6.U),
                            arrayRegFiles(generalRegFiles(3.U) + 4.U + 5.U),
                            arrayRegFiles(generalRegFiles(3.U) + 4.U + 4.U),
                            arrayRegFiles(generalRegFiles(3.U) + 4.U + 3.U),
                            arrayRegFiles(generalRegFiles(3.U) + 4.U + 2.U),
                            arrayRegFiles(generalRegFiles(3.U) + 4.U + 1.U),
                            arrayRegFiles(generalRegFiles(3.U) + 4.U + 0.U)
                          ).asSInt).asUInt.asUInt) === 1.U, 10.U, 1.U)
      }

      is(10.U) {
        /*
        $6 = *(($3 + 12) + (($2 as SP) * 8)) [unsigned, U64, 8]  // $6 = $3($2)
        $5 = *(SP + 1) [unsigned, MS[I0_5, U64], 2]  // $5 = seq
        $4 = *(SP + 63) [unsigned, I0_5, 1]  // $4 = i
        goto .11
        */


        val __tmp_1825 = (generalRegFiles(3.U) + 12.U + generalRegFiles(2.U).asUInt * 8.U).asUInt
        generalRegFiles(6.U) := Cat(
          arrayRegFiles(__tmp_1825 + 7.U),
          arrayRegFiles(__tmp_1825 + 6.U),
          arrayRegFiles(__tmp_1825 + 5.U),
          arrayRegFiles(__tmp_1825 + 4.U),
          arrayRegFiles(__tmp_1825 + 3.U),
          arrayRegFiles(__tmp_1825 + 2.U),
          arrayRegFiles(__tmp_1825 + 1.U),
          arrayRegFiles(__tmp_1825 + 0.U)
        ).asUInt

        val __tmp_1826 = (SP + 1.U).asUInt
        generalRegFiles(5.U) := Cat(
          arrayRegFiles(__tmp_1826 + 1.U),
          arrayRegFiles(__tmp_1826 + 0.U)
        ).asUInt

        val __tmp_1827 = (SP + 63.U).asUInt
        generalRegFiles(4.U) := Cat(
          arrayRegFiles(__tmp_1827 + 0.U)
        ).asUInt

        CP := 11.U
      }

      is(11.U) {
        /*
        if ((0 <= $4) & (($4 as Z) <= *($5 + 4))) goto .12 else goto .1
        */


        CP := Mux(((0.U <= generalRegFiles(4.U)).asUInt & (generalRegFiles(4.U).asSInt <= Cat(
                            arrayRegFiles(generalRegFiles(5.U) + 4.U + 7.U),
                            arrayRegFiles(generalRegFiles(5.U) + 4.U + 6.U),
                            arrayRegFiles(generalRegFiles(5.U) + 4.U + 5.U),
                            arrayRegFiles(generalRegFiles(5.U) + 4.U + 4.U),
                            arrayRegFiles(generalRegFiles(5.U) + 4.U + 3.U),
                            arrayRegFiles(generalRegFiles(5.U) + 4.U + 2.U),
                            arrayRegFiles(generalRegFiles(5.U) + 4.U + 1.U),
                            arrayRegFiles(generalRegFiles(5.U) + 4.U + 0.U)
                          ).asSInt).asUInt.asUInt) === 1.U, 12.U, 1.U)
      }

      is(12.U) {
        /*
        $7 = *(($5 + 12) + (($4 as SP) * 8)) [unsigned, U64, 8]  // $7 = $5($4)
        goto .13
        */


        val __tmp_1828 = (generalRegFiles(5.U) + 12.U + generalRegFiles(4.U).asUInt * 8.U).asUInt
        generalRegFiles(7.U) := Cat(
          arrayRegFiles(__tmp_1828 + 7.U),
          arrayRegFiles(__tmp_1828 + 6.U),
          arrayRegFiles(__tmp_1828 + 5.U),
          arrayRegFiles(__tmp_1828 + 4.U),
          arrayRegFiles(__tmp_1828 + 3.U),
          arrayRegFiles(__tmp_1828 + 2.U),
          arrayRegFiles(__tmp_1828 + 1.U),
          arrayRegFiles(__tmp_1828 + 0.U)
        ).asUInt

        CP := 13.U
      }

      is(13.U) {
        /*
        $8 = ($6 * $7)
        if ((0 <= $1) & (($1 as Z) <= *($0 + 4))) goto .14 else goto .1
        */


        generalRegFiles(8.U) := generalRegFiles(6.U) * generalRegFiles(7.U)
        CP := Mux(((0.U <= generalRegFiles(1.U)).asUInt & (generalRegFiles(1.U).asSInt <= Cat(
                            arrayRegFiles(generalRegFiles(0.U) + 4.U + 7.U),
                            arrayRegFiles(generalRegFiles(0.U) + 4.U + 6.U),
                            arrayRegFiles(generalRegFiles(0.U) + 4.U + 5.U),
                            arrayRegFiles(generalRegFiles(0.U) + 4.U + 4.U),
                            arrayRegFiles(generalRegFiles(0.U) + 4.U + 3.U),
                            arrayRegFiles(generalRegFiles(0.U) + 4.U + 2.U),
                            arrayRegFiles(generalRegFiles(0.U) + 4.U + 1.U),
                            arrayRegFiles(generalRegFiles(0.U) + 4.U + 0.U)
                          ).asSInt).asUInt.asUInt) === 1.U, 14.U, 1.U)
      }

      is(14.U) {
        /*
        *(($0 + 12) + (($1 as SP) * 8)) = $8 [unsigned, U64, 8]  // $0($1) = $8
        goto .15
        */


        val __tmp_1829 = generalRegFiles(0.U) + 12.U + generalRegFiles(1.U).asUInt * 8.U
        val __tmp_1830 = (generalRegFiles(8.U)).asUInt
        arrayRegFiles(__tmp_1829 + 0.U) := __tmp_1830(7, 0)
        arrayRegFiles(__tmp_1829 + 1.U) := __tmp_1830(15, 8)
        arrayRegFiles(__tmp_1829 + 2.U) := __tmp_1830(23, 16)
        arrayRegFiles(__tmp_1829 + 3.U) := __tmp_1830(31, 24)
        arrayRegFiles(__tmp_1829 + 4.U) := __tmp_1830(39, 32)
        arrayRegFiles(__tmp_1829 + 5.U) := __tmp_1830(47, 40)
        arrayRegFiles(__tmp_1829 + 6.U) := __tmp_1830(55, 48)
        arrayRegFiles(__tmp_1829 + 7.U) := __tmp_1830(63, 56)

        CP := 15.U
      }

      is(15.U) {
        /*
        $0 = *(SP + 63) [unsigned, I0_5, 1]  // $0 = i
        goto .16
        */


        val __tmp_1831 = (SP + 63.U).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_1831 + 0.U)
        ).asUInt

        CP := 16.U
      }

      is(16.U) {
        /*
        $1 = ($0 + 1)
        goto .17
        */


        generalRegFiles(1.U) := generalRegFiles(0.U) + 1.U
        CP := 17.U
      }

      is(17.U) {
        /*
        if ((0 <= $1) & ($1 <= 5)) goto .18 else goto .1
        */


        CP := Mux(((0.U <= generalRegFiles(1.U)).asUInt & (generalRegFiles(1.U) <= 5.U).asUInt.asUInt) === 1.U, 18.U, 1.U)
      }

      is(18.U) {
        /*
        *(SP + 63) = $1 [signed, I0_5, 1]  // i = $1
        goto .5
        */


        val __tmp_1832 = SP + 63.U
        val __tmp_1833 = (generalRegFiles(1.U)).asUInt
        arrayRegFiles(__tmp_1832 + 0.U) := __tmp_1833(7, 0)

        CP := 5.U
      }

      is(23.U) {
        /*
        $0 = *(SP + 1) [unsigned, MS[I0_5, U64], 2]  // $0 = seq
        $1 = *(SP + 1) [unsigned, MS[I0_5, U64], 2]  // $1 = seq
        goto .24
        */


        val __tmp_1834 = (SP + 1.U).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_1834 + 1.U),
          arrayRegFiles(__tmp_1834 + 0.U)
        ).asUInt

        val __tmp_1835 = (SP + 1.U).asUInt
        generalRegFiles(1.U) := Cat(
          arrayRegFiles(__tmp_1835 + 1.U),
          arrayRegFiles(__tmp_1835 + 0.U)
        ).asUInt

        CP := 24.U
      }

      is(24.U) {
        /*
        if ((0 <= 5) & ((5 as Z) <= *($1 + 4))) goto .25 else goto .1
        */


        CP := Mux(((0.U <= 5.U).asUInt & (5.U.asSInt <= Cat(
                            arrayRegFiles(generalRegFiles(1.U) + 4.U + 7.U),
                            arrayRegFiles(generalRegFiles(1.U) + 4.U + 6.U),
                            arrayRegFiles(generalRegFiles(1.U) + 4.U + 5.U),
                            arrayRegFiles(generalRegFiles(1.U) + 4.U + 4.U),
                            arrayRegFiles(generalRegFiles(1.U) + 4.U + 3.U),
                            arrayRegFiles(generalRegFiles(1.U) + 4.U + 2.U),
                            arrayRegFiles(generalRegFiles(1.U) + 4.U + 1.U),
                            arrayRegFiles(generalRegFiles(1.U) + 4.U + 0.U)
                          ).asSInt).asUInt.asUInt) === 1.U, 25.U, 1.U)
      }

      is(25.U) {
        /*
        $3 = *(($1 + 12) + ((5 as SP) * 8)) [unsigned, U64, 8]  // $3 = $1(5)
        $2 = *(SP + 1) [unsigned, MS[I0_5, U64], 2]  // $2 = seq
        goto .26
        */


        val __tmp_1836 = (generalRegFiles(1.U) + 12.U + 5.U.asUInt * 8.U).asUInt
        generalRegFiles(3.U) := Cat(
          arrayRegFiles(__tmp_1836 + 7.U),
          arrayRegFiles(__tmp_1836 + 6.U),
          arrayRegFiles(__tmp_1836 + 5.U),
          arrayRegFiles(__tmp_1836 + 4.U),
          arrayRegFiles(__tmp_1836 + 3.U),
          arrayRegFiles(__tmp_1836 + 2.U),
          arrayRegFiles(__tmp_1836 + 1.U),
          arrayRegFiles(__tmp_1836 + 0.U)
        ).asUInt

        val __tmp_1837 = (SP + 1.U).asUInt
        generalRegFiles(2.U) := Cat(
          arrayRegFiles(__tmp_1837 + 1.U),
          arrayRegFiles(__tmp_1837 + 0.U)
        ).asUInt

        CP := 26.U
      }

      is(26.U) {
        /*
        if ((0 <= 5) & ((5 as Z) <= *($2 + 4))) goto .27 else goto .1
        */


        CP := Mux(((0.U <= 5.U).asUInt & (5.U.asSInt <= Cat(
                            arrayRegFiles(generalRegFiles(2.U) + 4.U + 7.U),
                            arrayRegFiles(generalRegFiles(2.U) + 4.U + 6.U),
                            arrayRegFiles(generalRegFiles(2.U) + 4.U + 5.U),
                            arrayRegFiles(generalRegFiles(2.U) + 4.U + 4.U),
                            arrayRegFiles(generalRegFiles(2.U) + 4.U + 3.U),
                            arrayRegFiles(generalRegFiles(2.U) + 4.U + 2.U),
                            arrayRegFiles(generalRegFiles(2.U) + 4.U + 1.U),
                            arrayRegFiles(generalRegFiles(2.U) + 4.U + 0.U)
                          ).asSInt).asUInt.asUInt) === 1.U, 27.U, 1.U)
      }

      is(27.U) {
        /*
        $6 = *(($2 + 12) + ((5 as SP) * 8)) [unsigned, U64, 8]  // $6 = $2(5)
        goto .28
        */


        val __tmp_1838 = (generalRegFiles(2.U) + 12.U + 5.U.asUInt * 8.U).asUInt
        generalRegFiles(6.U) := Cat(
          arrayRegFiles(__tmp_1838 + 7.U),
          arrayRegFiles(__tmp_1838 + 6.U),
          arrayRegFiles(__tmp_1838 + 5.U),
          arrayRegFiles(__tmp_1838 + 4.U),
          arrayRegFiles(__tmp_1838 + 3.U),
          arrayRegFiles(__tmp_1838 + 2.U),
          arrayRegFiles(__tmp_1838 + 1.U),
          arrayRegFiles(__tmp_1838 + 0.U)
        ).asUInt

        CP := 28.U
      }

      is(28.U) {
        /*
        $5 = ($3 * $6)
        if ((0 <= 5) & ((5 as Z) <= *($0 + 4))) goto .29 else goto .1
        */


        generalRegFiles(5.U) := generalRegFiles(3.U) * generalRegFiles(6.U)
        CP := Mux(((0.U <= 5.U).asUInt & (5.U.asSInt <= Cat(
                            arrayRegFiles(generalRegFiles(0.U) + 4.U + 7.U),
                            arrayRegFiles(generalRegFiles(0.U) + 4.U + 6.U),
                            arrayRegFiles(generalRegFiles(0.U) + 4.U + 5.U),
                            arrayRegFiles(generalRegFiles(0.U) + 4.U + 4.U),
                            arrayRegFiles(generalRegFiles(0.U) + 4.U + 3.U),
                            arrayRegFiles(generalRegFiles(0.U) + 4.U + 2.U),
                            arrayRegFiles(generalRegFiles(0.U) + 4.U + 1.U),
                            arrayRegFiles(generalRegFiles(0.U) + 4.U + 0.U)
                          ).asSInt).asUInt.asUInt) === 1.U, 29.U, 1.U)
      }

      is(29.U) {
        /*
        *(($0 + 12) + ((5 as SP) * 8)) = $5 [unsigned, U64, 8]  // $0(5) = $5
        goto .30
        */


        val __tmp_1839 = generalRegFiles(0.U) + 12.U + 5.U.asUInt * 8.U
        val __tmp_1840 = (generalRegFiles(5.U)).asUInt
        arrayRegFiles(__tmp_1839 + 0.U) := __tmp_1840(7, 0)
        arrayRegFiles(__tmp_1839 + 1.U) := __tmp_1840(15, 8)
        arrayRegFiles(__tmp_1839 + 2.U) := __tmp_1840(23, 16)
        arrayRegFiles(__tmp_1839 + 3.U) := __tmp_1840(31, 24)
        arrayRegFiles(__tmp_1839 + 4.U) := __tmp_1840(39, 32)
        arrayRegFiles(__tmp_1839 + 5.U) := __tmp_1840(47, 40)
        arrayRegFiles(__tmp_1839 + 6.U) := __tmp_1840(55, 48)
        arrayRegFiles(__tmp_1839 + 7.U) := __tmp_1840(63, 56)

        CP := 30.U
      }

      is(30.U) {
        /*
        undecl i: I0_5 [@63, 1]
        goto $ret@0
        */


        CP := Cat(
          arrayRegFiles(SP + 0.U + 0.U)
        )

      }

    }

}
