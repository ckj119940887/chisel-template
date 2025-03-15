package GeneralRegFileToBRAM 

import chisel3._
import chisel3.util._
import chisel3.experimental._

class global (val C_S_AXI_DATA_WIDTH:  Int = 32,
               val C_S_AXI_ADDR_WIDTH:  Int = 32,
               val ARRAY_REG_WIDTH:     Int = 8,
               val ARRAY_REG_DEPTH:     Int = 1024,
               val GENERAL_REG_WIDTH:   Int = 64,
               val GENERAL_REG_DEPTH:   Int = 1,
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
        SP = 9
        *9 = 0 [unsigned, CP, 1]  // $ret
        *10 = 12 [unsigned, SP, 2]  // data address of $res (size = 8)
        goto .4
        */


        SP := 9.U

        val __tmp_1723 = 9.U
        val __tmp_1724 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_1723 + 0.U) := __tmp_1724(7, 0)

        val __tmp_1725 = 10.U
        val __tmp_1726 = (12.U(16.W)).asUInt
        arrayRegFiles(__tmp_1725 + 0.U) := __tmp_1726(7, 0)
        arrayRegFiles(__tmp_1725 + 1.U) := __tmp_1726(15, 8)

        CP := 4.U
      }

      is(4.U) {
        /*
        if *0 goto .5 else goto .7
        */


        CP := Mux((Cat(
                     arrayRegFiles(0.U + 0.U)
                   ).asUInt) === 1.U, 5.U, 7.U)
      }

      is(5.U) {
        /*
        $0 = *1 [signed, Z, 8]  // $0 = Foo.x
        goto .6
        */


        val __tmp_1727 = (1.U).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_1727 + 7.U),
          arrayRegFiles(__tmp_1727 + 6.U),
          arrayRegFiles(__tmp_1727 + 5.U),
          arrayRegFiles(__tmp_1727 + 4.U),
          arrayRegFiles(__tmp_1727 + 3.U),
          arrayRegFiles(__tmp_1727 + 2.U),
          arrayRegFiles(__tmp_1727 + 1.U),
          arrayRegFiles(__tmp_1727 + 0.U)
        ).asUInt

        CP := 6.U
      }

      is(6.U) {
        /*
        **(SP + 1) = $0 [signed, Z, 8]  // $res = $0
        goto $ret@0
        */


        val __tmp_1728 = Cat(
          arrayRegFiles(SP + 1.U + 1.U),
          arrayRegFiles(SP + 1.U + 0.U)
        )
        val __tmp_1729 = (generalRegFiles(0.U).asSInt).asUInt
        arrayRegFiles(__tmp_1728 + 0.U) := __tmp_1729(7, 0)
        arrayRegFiles(__tmp_1728 + 1.U) := __tmp_1729(15, 8)
        arrayRegFiles(__tmp_1728 + 2.U) := __tmp_1729(23, 16)
        arrayRegFiles(__tmp_1728 + 3.U) := __tmp_1729(31, 24)
        arrayRegFiles(__tmp_1728 + 4.U) := __tmp_1729(39, 32)
        arrayRegFiles(__tmp_1728 + 5.U) := __tmp_1729(47, 40)
        arrayRegFiles(__tmp_1728 + 6.U) := __tmp_1729(55, 48)
        arrayRegFiles(__tmp_1728 + 7.U) := __tmp_1729(63, 56)

        CP := Cat(
          arrayRegFiles(SP + 0.U + 0.U)
        )

      }

      is(7.U) {
        /*
        SP = SP + 19
        decl $ret: CP [@0, 1]
        goto .8
        */


        SP := SP + 19.U

        CP := 8.U
      }

      is(8.U) {
        /*
        *SP = 11 [unsigned, CP, 1]  // $ret@0 = 10
        *(SP - 8) = $0 [unsigned, U64, 8]  // save $0
        goto .9
        */


        val __tmp_1730 = SP
        val __tmp_1731 = (11.U(8.W)).asUInt
        arrayRegFiles(__tmp_1730 + 0.U) := __tmp_1731(7, 0)

        val __tmp_1732 = SP - 8.U
        val __tmp_1733 = (generalRegFiles(0.U)).asUInt
        arrayRegFiles(__tmp_1732 + 0.U) := __tmp_1733(7, 0)
        arrayRegFiles(__tmp_1732 + 1.U) := __tmp_1733(15, 8)
        arrayRegFiles(__tmp_1732 + 2.U) := __tmp_1733(23, 16)
        arrayRegFiles(__tmp_1732 + 3.U) := __tmp_1733(31, 24)
        arrayRegFiles(__tmp_1732 + 4.U) := __tmp_1733(39, 32)
        arrayRegFiles(__tmp_1732 + 5.U) := __tmp_1733(47, 40)
        arrayRegFiles(__tmp_1732 + 6.U) := __tmp_1733(55, 48)
        arrayRegFiles(__tmp_1732 + 7.U) := __tmp_1733(63, 56)

        CP := 9.U
      }

      is(9.U) {
        /*
        *0 = true [unsigned, B, 1]  // Foo = true
        $0 = (2 * 4)
        goto .10
        */


        val __tmp_1734 = 0.U
        val __tmp_1735 = (1.U).asUInt
        arrayRegFiles(__tmp_1734 + 0.U) := __tmp_1735(7, 0)

        generalRegFiles(0.U) := (2.S * 4.S).asUInt
        CP := 10.U
      }

      is(10.U) {
        /*
        *1 = $0 [signed, Z, 8]  // Foo.x = $0
        goto $ret@0
        */


        val __tmp_1736 = 1.U
        val __tmp_1737 = (generalRegFiles(0.U).asSInt).asUInt
        arrayRegFiles(__tmp_1736 + 0.U) := __tmp_1737(7, 0)
        arrayRegFiles(__tmp_1736 + 1.U) := __tmp_1737(15, 8)
        arrayRegFiles(__tmp_1736 + 2.U) := __tmp_1737(23, 16)
        arrayRegFiles(__tmp_1736 + 3.U) := __tmp_1737(31, 24)
        arrayRegFiles(__tmp_1736 + 4.U) := __tmp_1737(39, 32)
        arrayRegFiles(__tmp_1736 + 5.U) := __tmp_1737(47, 40)
        arrayRegFiles(__tmp_1736 + 6.U) := __tmp_1737(55, 48)
        arrayRegFiles(__tmp_1736 + 7.U) := __tmp_1737(63, 56)

        CP := Cat(
          arrayRegFiles(SP + 0.U + 0.U)
        )

      }

      is(11.U) {
        /*
        $0 = *(SP - 8) [unsigned, U64, 8]  // restore $0
        undecl $ret: CP [@0, 1]
        SP = SP - 19
        goto .5
        */


        val __tmp_1738 = (SP - 8.U).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_1738 + 7.U),
          arrayRegFiles(__tmp_1738 + 6.U),
          arrayRegFiles(__tmp_1738 + 5.U),
          arrayRegFiles(__tmp_1738 + 4.U),
          arrayRegFiles(__tmp_1738 + 3.U),
          arrayRegFiles(__tmp_1738 + 2.U),
          arrayRegFiles(__tmp_1738 + 1.U),
          arrayRegFiles(__tmp_1738 + 0.U)
        ).asUInt

        SP := SP - 19.U

        CP := 5.U
      }

    }

}
