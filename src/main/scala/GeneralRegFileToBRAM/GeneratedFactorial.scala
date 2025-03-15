package GeneralRegFileToBRAM 

import chisel3._
import chisel3.util._
import chisel3.experimental._

class factorial (val C_S_AXI_DATA_WIDTH:  Int = 32,
               val C_S_AXI_ADDR_WIDTH:  Int = 32,
               val ARRAY_REG_WIDTH:     Int = 8,
               val ARRAY_REG_DEPTH:     Int = 1024,
               val GENERAL_REG_WIDTH:   Int = 64,
               val GENERAL_REG_DEPTH:   Int = 5,
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
        *1 = 3 [unsigned, SP, 2]  // data address of $res (size = 4)
        goto .4
        */


        SP := 0.U

        val __tmp_1696 = 0.U
        val __tmp_1697 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_1696 + 0.U) := __tmp_1697(7, 0)

        val __tmp_1698 = 1.U
        val __tmp_1699 = (3.U(16.W)).asUInt
        arrayRegFiles(__tmp_1698 + 0.U) := __tmp_1699(7, 0)
        arrayRegFiles(__tmp_1698 + 1.U) := __tmp_1699(15, 8)

        CP := 4.U
      }

      is(4.U) {
        /*
        $0 = *(SP + 7) [unsigned, U32, 4]  // $0 = n
        goto .5
        */


        val __tmp_1700 = (SP + 7.U).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_1700 + 3.U),
          arrayRegFiles(__tmp_1700 + 2.U),
          arrayRegFiles(__tmp_1700 + 1.U),
          arrayRegFiles(__tmp_1700 + 0.U)
        ).asUInt

        CP := 5.U
      }

      is(5.U) {
        /*
        $1 = ($0 ≡ 0)
        goto .6
        */


        generalRegFiles(1.U) := (generalRegFiles(0.U) === 0.U).asUInt
        CP := 6.U
      }

      is(6.U) {
        /*
        if $1 goto .7 else goto .8
        */


        CP := Mux((generalRegFiles(1.U).asUInt) === 1.U, 7.U, 8.U)
      }

      is(7.U) {
        /*
        **(SP + 1) = 1 [unsigned, U32, 4]  // $res = 1
        goto $ret@0
        */


        val __tmp_1701 = Cat(
          arrayRegFiles(SP + 1.U + 1.U),
          arrayRegFiles(SP + 1.U + 0.U)
        )
        val __tmp_1702 = (1.U(32.W)).asUInt
        arrayRegFiles(__tmp_1701 + 0.U) := __tmp_1702(7, 0)
        arrayRegFiles(__tmp_1701 + 1.U) := __tmp_1702(15, 8)
        arrayRegFiles(__tmp_1701 + 2.U) := __tmp_1702(23, 16)
        arrayRegFiles(__tmp_1701 + 3.U) := __tmp_1702(31, 24)

        CP := Cat(
          arrayRegFiles(SP + 0.U + 0.U)
        )

      }

      is(8.U) {
        /*
        $0 = *(SP + 7) [unsigned, U32, 4]  // $0 = n
        $1 = *(SP + 7) [unsigned, U32, 4]  // $1 = n
        goto .9
        */


        val __tmp_1703 = (SP + 7.U).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_1703 + 3.U),
          arrayRegFiles(__tmp_1703 + 2.U),
          arrayRegFiles(__tmp_1703 + 1.U),
          arrayRegFiles(__tmp_1703 + 0.U)
        ).asUInt

        val __tmp_1704 = (SP + 7.U).asUInt
        generalRegFiles(1.U) := Cat(
          arrayRegFiles(__tmp_1704 + 3.U),
          arrayRegFiles(__tmp_1704 + 2.U),
          arrayRegFiles(__tmp_1704 + 1.U),
          arrayRegFiles(__tmp_1704 + 0.U)
        ).asUInt

        CP := 9.U
      }

      is(9.U) {
        /*
        $2 = ($1 - 1)
        alloc factorial$res@[9,14].A651BB8C: U32 [@11, 4]
        SP = SP + 39
        decl $ret: CP [@0, 1], $res: SP [@1, 6], n: U32 [@7, 4]
        goto .10
        */


        generalRegFiles(2.U) := generalRegFiles(1.U) - 1.U
        SP := SP + 39.U

        CP := 10.U
      }

      is(10.U) {
        /*
        *SP = 11 [unsigned, CP, 1]  // $ret@0 = 12
        *(SP + 1) = (SP - 28) [unsigned, SP, 2]  // $res@1 = -28
        *(SP + 7) = $2 [unsigned, U32, 4]  // n = $2
        *(SP - 24) = $0 [unsigned, U64, 8]  // save $0
        *(SP - 16) = $1 [unsigned, U64, 8]  // save $1
        *(SP - 8) = $2 [unsigned, U64, 8]  // save $2
        goto .4
        */


        val __tmp_1705 = SP
        val __tmp_1706 = (11.U(8.W)).asUInt
        arrayRegFiles(__tmp_1705 + 0.U) := __tmp_1706(7, 0)

        val __tmp_1707 = SP + 1.U
        val __tmp_1708 = (SP - 28.U).asUInt
        arrayRegFiles(__tmp_1707 + 0.U) := __tmp_1708(7, 0)
        arrayRegFiles(__tmp_1707 + 1.U) := __tmp_1708(15, 8)

        val __tmp_1709 = SP + 7.U
        val __tmp_1710 = (generalRegFiles(2.U)).asUInt
        arrayRegFiles(__tmp_1709 + 0.U) := __tmp_1710(7, 0)
        arrayRegFiles(__tmp_1709 + 1.U) := __tmp_1710(15, 8)
        arrayRegFiles(__tmp_1709 + 2.U) := __tmp_1710(23, 16)
        arrayRegFiles(__tmp_1709 + 3.U) := __tmp_1710(31, 24)

        val __tmp_1711 = SP - 24.U
        val __tmp_1712 = (generalRegFiles(0.U)).asUInt
        arrayRegFiles(__tmp_1711 + 0.U) := __tmp_1712(7, 0)
        arrayRegFiles(__tmp_1711 + 1.U) := __tmp_1712(15, 8)
        arrayRegFiles(__tmp_1711 + 2.U) := __tmp_1712(23, 16)
        arrayRegFiles(__tmp_1711 + 3.U) := __tmp_1712(31, 24)
        arrayRegFiles(__tmp_1711 + 4.U) := __tmp_1712(39, 32)
        arrayRegFiles(__tmp_1711 + 5.U) := __tmp_1712(47, 40)
        arrayRegFiles(__tmp_1711 + 6.U) := __tmp_1712(55, 48)
        arrayRegFiles(__tmp_1711 + 7.U) := __tmp_1712(63, 56)

        val __tmp_1713 = SP - 16.U
        val __tmp_1714 = (generalRegFiles(1.U)).asUInt
        arrayRegFiles(__tmp_1713 + 0.U) := __tmp_1714(7, 0)
        arrayRegFiles(__tmp_1713 + 1.U) := __tmp_1714(15, 8)
        arrayRegFiles(__tmp_1713 + 2.U) := __tmp_1714(23, 16)
        arrayRegFiles(__tmp_1713 + 3.U) := __tmp_1714(31, 24)
        arrayRegFiles(__tmp_1713 + 4.U) := __tmp_1714(39, 32)
        arrayRegFiles(__tmp_1713 + 5.U) := __tmp_1714(47, 40)
        arrayRegFiles(__tmp_1713 + 6.U) := __tmp_1714(55, 48)
        arrayRegFiles(__tmp_1713 + 7.U) := __tmp_1714(63, 56)

        val __tmp_1715 = SP - 8.U
        val __tmp_1716 = (generalRegFiles(2.U)).asUInt
        arrayRegFiles(__tmp_1715 + 0.U) := __tmp_1716(7, 0)
        arrayRegFiles(__tmp_1715 + 1.U) := __tmp_1716(15, 8)
        arrayRegFiles(__tmp_1715 + 2.U) := __tmp_1716(23, 16)
        arrayRegFiles(__tmp_1715 + 3.U) := __tmp_1716(31, 24)
        arrayRegFiles(__tmp_1715 + 4.U) := __tmp_1716(39, 32)
        arrayRegFiles(__tmp_1715 + 5.U) := __tmp_1716(47, 40)
        arrayRegFiles(__tmp_1715 + 6.U) := __tmp_1716(55, 48)
        arrayRegFiles(__tmp_1715 + 7.U) := __tmp_1716(63, 56)

        CP := 4.U
      }

      is(11.U) {
        /*
        $0 = *(SP - 24) [unsigned, U64, 8]  // restore $0
        $1 = *(SP - 16) [unsigned, U64, 8]  // restore $1
        $2 = *(SP - 8) [unsigned, U64, 8]  // restore $2
        $3 = **(SP + 1) [unsigned, SP, 2]  // $3 = $ret
        undecl n: U32 [@7, 4], $res: SP [@1, 6], $ret: CP [@0, 1]
        SP = SP - 39
        goto .12
        */


        val __tmp_1717 = (SP - 24.U).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_1717 + 7.U),
          arrayRegFiles(__tmp_1717 + 6.U),
          arrayRegFiles(__tmp_1717 + 5.U),
          arrayRegFiles(__tmp_1717 + 4.U),
          arrayRegFiles(__tmp_1717 + 3.U),
          arrayRegFiles(__tmp_1717 + 2.U),
          arrayRegFiles(__tmp_1717 + 1.U),
          arrayRegFiles(__tmp_1717 + 0.U)
        ).asUInt

        val __tmp_1718 = (SP - 16.U).asUInt
        generalRegFiles(1.U) := Cat(
          arrayRegFiles(__tmp_1718 + 7.U),
          arrayRegFiles(__tmp_1718 + 6.U),
          arrayRegFiles(__tmp_1718 + 5.U),
          arrayRegFiles(__tmp_1718 + 4.U),
          arrayRegFiles(__tmp_1718 + 3.U),
          arrayRegFiles(__tmp_1718 + 2.U),
          arrayRegFiles(__tmp_1718 + 1.U),
          arrayRegFiles(__tmp_1718 + 0.U)
        ).asUInt

        val __tmp_1719 = (SP - 8.U).asUInt
        generalRegFiles(2.U) := Cat(
          arrayRegFiles(__tmp_1719 + 7.U),
          arrayRegFiles(__tmp_1719 + 6.U),
          arrayRegFiles(__tmp_1719 + 5.U),
          arrayRegFiles(__tmp_1719 + 4.U),
          arrayRegFiles(__tmp_1719 + 3.U),
          arrayRegFiles(__tmp_1719 + 2.U),
          arrayRegFiles(__tmp_1719 + 1.U),
          arrayRegFiles(__tmp_1719 + 0.U)
        ).asUInt

        val __tmp_1720 = (Cat(
          arrayRegFiles(SP + 1.U + 3.U),
          arrayRegFiles(SP + 1.U + 2.U),
          arrayRegFiles(SP + 1.U + 1.U),
          arrayRegFiles(SP + 1.U + 0.U)
        )).asUInt
        generalRegFiles(3.U) := Cat(
          arrayRegFiles(__tmp_1720 + 1.U),
          arrayRegFiles(__tmp_1720 + 0.U)
        ).asUInt

        SP := SP - 39.U

        CP := 12.U
      }

      is(12.U) {
        /*
        $4 = ($0 * $3)
        unalloc factorial$res@[9,14].A651BB8C: U32 [@11, 4]
        goto .13
        */


        generalRegFiles(4.U) := generalRegFiles(0.U) * generalRegFiles(3.U)
        CP := 13.U
      }

      is(13.U) {
        /*
        **(SP + 1) = $4 [unsigned, U32, 4]  // $res = $4
        goto $ret@0
        */


        val __tmp_1721 = Cat(
          arrayRegFiles(SP + 1.U + 1.U),
          arrayRegFiles(SP + 1.U + 0.U)
        )
        val __tmp_1722 = (generalRegFiles(4.U)).asUInt
        arrayRegFiles(__tmp_1721 + 0.U) := __tmp_1722(7, 0)
        arrayRegFiles(__tmp_1721 + 1.U) := __tmp_1722(15, 8)
        arrayRegFiles(__tmp_1721 + 2.U) := __tmp_1722(23, 16)
        arrayRegFiles(__tmp_1721 + 3.U) := __tmp_1722(31, 24)

        CP := Cat(
          arrayRegFiles(SP + 0.U + 0.U)
        )

      }

    }

}
