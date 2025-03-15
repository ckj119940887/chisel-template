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
        *11 = 13 [unsigned, SP, 2]  // data address of a (size = 812)
        goto .4
        */


        SP := 0.U

        val __tmp_1841 = 0.U
        val __tmp_1842 = (0.U(8.W)).asUInt
        arrayRegFiles(__tmp_1841 + 0.U) := __tmp_1842(7, 0)

        val __tmp_1843 = 1.U
        val __tmp_1844 = (3.U(16.W)).asUInt
        arrayRegFiles(__tmp_1843 + 0.U) := __tmp_1844(7, 0)
        arrayRegFiles(__tmp_1843 + 1.U) := __tmp_1844(15, 8)

        val __tmp_1845 = 11.U
        val __tmp_1846 = (13.U(16.W)).asUInt
        arrayRegFiles(__tmp_1845 + 0.U) := __tmp_1846(7, 0)
        arrayRegFiles(__tmp_1845 + 1.U) := __tmp_1846(15, 8)

        CP := 4.U
      }

      is(4.U) {
        /*
        $1 = *(SP + 825) [signed, Z, 8]  // $1 = i
        $0 = *(SP + 11) [unsigned, IS[Z, Z], 2]  // $0 = a
        goto .5
        */


        val __tmp_1847 = (SP + 825.U).asUInt
        generalRegFiles(1.U) := Cat(
          arrayRegFiles(__tmp_1847 + 7.U),
          arrayRegFiles(__tmp_1847 + 6.U),
          arrayRegFiles(__tmp_1847 + 5.U),
          arrayRegFiles(__tmp_1847 + 4.U),
          arrayRegFiles(__tmp_1847 + 3.U),
          arrayRegFiles(__tmp_1847 + 2.U),
          arrayRegFiles(__tmp_1847 + 1.U),
          arrayRegFiles(__tmp_1847 + 0.U)
        ).asUInt

        val __tmp_1848 = (SP + 11.U).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_1848 + 1.U),
          arrayRegFiles(__tmp_1848 + 0.U)
        ).asUInt

        CP := 5.U
      }

      is(5.U) {
        /*
        $2 = *($0 + 4) [signed, Z, 8]  // $2 = $0.size
        goto .6
        */


        val __tmp_1849 = (generalRegFiles(0.U) + 4.U).asUInt
        generalRegFiles(2.U) := Cat(
          arrayRegFiles(__tmp_1849 + 7.U),
          arrayRegFiles(__tmp_1849 + 6.U),
          arrayRegFiles(__tmp_1849 + 5.U),
          arrayRegFiles(__tmp_1849 + 4.U),
          arrayRegFiles(__tmp_1849 + 3.U),
          arrayRegFiles(__tmp_1849 + 2.U),
          arrayRegFiles(__tmp_1849 + 1.U),
          arrayRegFiles(__tmp_1849 + 0.U)
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


        val __tmp_1850 = (SP + 11.U).asUInt
        generalRegFiles(1.U) := Cat(
          arrayRegFiles(__tmp_1850 + 1.U),
          arrayRegFiles(__tmp_1850 + 0.U)
        ).asUInt

        val __tmp_1851 = (SP + 825.U).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_1851 + 7.U),
          arrayRegFiles(__tmp_1851 + 6.U),
          arrayRegFiles(__tmp_1851 + 5.U),
          arrayRegFiles(__tmp_1851 + 4.U),
          arrayRegFiles(__tmp_1851 + 3.U),
          arrayRegFiles(__tmp_1851 + 2.U),
          arrayRegFiles(__tmp_1851 + 1.U),
          arrayRegFiles(__tmp_1851 + 0.U)
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
        val __tmp_1852 = (SP + 833.U).asUInt
        generalRegFiles(3.U) := Cat(
          arrayRegFiles(__tmp_1852 + 7.U),
          arrayRegFiles(__tmp_1852 + 6.U),
          arrayRegFiles(__tmp_1852 + 5.U),
          arrayRegFiles(__tmp_1852 + 4.U),
          arrayRegFiles(__tmp_1852 + 3.U),
          arrayRegFiles(__tmp_1852 + 2.U),
          arrayRegFiles(__tmp_1852 + 1.U),
          arrayRegFiles(__tmp_1852 + 0.U)
        ).asUInt

        val __tmp_1853 = (SP + 11.U).asUInt
        generalRegFiles(5.U) := Cat(
          arrayRegFiles(__tmp_1853 + 1.U),
          arrayRegFiles(__tmp_1853 + 0.U)
        ).asUInt

        val __tmp_1854 = (SP + 825.U).asUInt
        generalRegFiles(4.U) := Cat(
          arrayRegFiles(__tmp_1854 + 7.U),
          arrayRegFiles(__tmp_1854 + 6.U),
          arrayRegFiles(__tmp_1854 + 5.U),
          arrayRegFiles(__tmp_1854 + 4.U),
          arrayRegFiles(__tmp_1854 + 3.U),
          arrayRegFiles(__tmp_1854 + 2.U),
          arrayRegFiles(__tmp_1854 + 1.U),
          arrayRegFiles(__tmp_1854 + 0.U)
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


        val __tmp_1855 = (generalRegFiles(5.U) + 12.U + generalRegFiles(4.U).asSInt.asUInt * 8.U).asUInt
        generalRegFiles(6.U) := Cat(
          arrayRegFiles(__tmp_1855 + 7.U),
          arrayRegFiles(__tmp_1855 + 6.U),
          arrayRegFiles(__tmp_1855 + 5.U),
          arrayRegFiles(__tmp_1855 + 4.U),
          arrayRegFiles(__tmp_1855 + 3.U),
          arrayRegFiles(__tmp_1855 + 2.U),
          arrayRegFiles(__tmp_1855 + 1.U),
          arrayRegFiles(__tmp_1855 + 0.U)
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
        *SP = 17 [unsigned, CP, 1]  // $ret@0 = 18
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


        val __tmp_1856 = SP
        val __tmp_1857 = (17.U(8.W)).asUInt
        arrayRegFiles(__tmp_1856 + 0.U) := __tmp_1857(7, 0)

        val __tmp_1858 = SP + 1.U
        val __tmp_1859 = (SP - 72.U).asUInt
        arrayRegFiles(__tmp_1858 + 0.U) := __tmp_1859(7, 0)
        arrayRegFiles(__tmp_1858 + 1.U) := __tmp_1859(15, 8)

        val __tmp_1860 = SP + 11.U
        val __tmp_1861 = (generalRegFiles(1.U)).asUInt
        arrayRegFiles(__tmp_1860 + 0.U) := __tmp_1861(7, 0)
        arrayRegFiles(__tmp_1860 + 1.U) := __tmp_1861(15, 8)

        val __tmp_1862 = SP + 825.U
        val __tmp_1863 = (generalRegFiles(2.U).asSInt).asUInt
        arrayRegFiles(__tmp_1862 + 0.U) := __tmp_1863(7, 0)
        arrayRegFiles(__tmp_1862 + 1.U) := __tmp_1863(15, 8)
        arrayRegFiles(__tmp_1862 + 2.U) := __tmp_1863(23, 16)
        arrayRegFiles(__tmp_1862 + 3.U) := __tmp_1863(31, 24)
        arrayRegFiles(__tmp_1862 + 4.U) := __tmp_1863(39, 32)
        arrayRegFiles(__tmp_1862 + 5.U) := __tmp_1863(47, 40)
        arrayRegFiles(__tmp_1862 + 6.U) := __tmp_1863(55, 48)
        arrayRegFiles(__tmp_1862 + 7.U) := __tmp_1863(63, 56)

        val __tmp_1864 = SP + 833.U
        val __tmp_1865 = (generalRegFiles(7.U).asSInt).asUInt
        arrayRegFiles(__tmp_1864 + 0.U) := __tmp_1865(7, 0)
        arrayRegFiles(__tmp_1864 + 1.U) := __tmp_1865(15, 8)
        arrayRegFiles(__tmp_1864 + 2.U) := __tmp_1865(23, 16)
        arrayRegFiles(__tmp_1864 + 3.U) := __tmp_1865(31, 24)
        arrayRegFiles(__tmp_1864 + 4.U) := __tmp_1865(39, 32)
        arrayRegFiles(__tmp_1864 + 5.U) := __tmp_1865(47, 40)
        arrayRegFiles(__tmp_1864 + 6.U) := __tmp_1865(55, 48)
        arrayRegFiles(__tmp_1864 + 7.U) := __tmp_1865(63, 56)

        val __tmp_1866 = SP - 64.U
        val __tmp_1867 = (generalRegFiles(0.U)).asUInt
        arrayRegFiles(__tmp_1866 + 0.U) := __tmp_1867(7, 0)
        arrayRegFiles(__tmp_1866 + 1.U) := __tmp_1867(15, 8)
        arrayRegFiles(__tmp_1866 + 2.U) := __tmp_1867(23, 16)
        arrayRegFiles(__tmp_1866 + 3.U) := __tmp_1867(31, 24)
        arrayRegFiles(__tmp_1866 + 4.U) := __tmp_1867(39, 32)
        arrayRegFiles(__tmp_1866 + 5.U) := __tmp_1867(47, 40)
        arrayRegFiles(__tmp_1866 + 6.U) := __tmp_1867(55, 48)
        arrayRegFiles(__tmp_1866 + 7.U) := __tmp_1867(63, 56)

        val __tmp_1868 = SP - 56.U
        val __tmp_1869 = (generalRegFiles(1.U)).asUInt
        arrayRegFiles(__tmp_1868 + 0.U) := __tmp_1869(7, 0)
        arrayRegFiles(__tmp_1868 + 1.U) := __tmp_1869(15, 8)
        arrayRegFiles(__tmp_1868 + 2.U) := __tmp_1869(23, 16)
        arrayRegFiles(__tmp_1868 + 3.U) := __tmp_1869(31, 24)
        arrayRegFiles(__tmp_1868 + 4.U) := __tmp_1869(39, 32)
        arrayRegFiles(__tmp_1868 + 5.U) := __tmp_1869(47, 40)
        arrayRegFiles(__tmp_1868 + 6.U) := __tmp_1869(55, 48)
        arrayRegFiles(__tmp_1868 + 7.U) := __tmp_1869(63, 56)

        val __tmp_1870 = SP - 48.U
        val __tmp_1871 = (generalRegFiles(2.U)).asUInt
        arrayRegFiles(__tmp_1870 + 0.U) := __tmp_1871(7, 0)
        arrayRegFiles(__tmp_1870 + 1.U) := __tmp_1871(15, 8)
        arrayRegFiles(__tmp_1870 + 2.U) := __tmp_1871(23, 16)
        arrayRegFiles(__tmp_1870 + 3.U) := __tmp_1871(31, 24)
        arrayRegFiles(__tmp_1870 + 4.U) := __tmp_1871(39, 32)
        arrayRegFiles(__tmp_1870 + 5.U) := __tmp_1871(47, 40)
        arrayRegFiles(__tmp_1870 + 6.U) := __tmp_1871(55, 48)
        arrayRegFiles(__tmp_1870 + 7.U) := __tmp_1871(63, 56)

        val __tmp_1872 = SP - 40.U
        val __tmp_1873 = (generalRegFiles(3.U)).asUInt
        arrayRegFiles(__tmp_1872 + 0.U) := __tmp_1873(7, 0)
        arrayRegFiles(__tmp_1872 + 1.U) := __tmp_1873(15, 8)
        arrayRegFiles(__tmp_1872 + 2.U) := __tmp_1873(23, 16)
        arrayRegFiles(__tmp_1872 + 3.U) := __tmp_1873(31, 24)
        arrayRegFiles(__tmp_1872 + 4.U) := __tmp_1873(39, 32)
        arrayRegFiles(__tmp_1872 + 5.U) := __tmp_1873(47, 40)
        arrayRegFiles(__tmp_1872 + 6.U) := __tmp_1873(55, 48)
        arrayRegFiles(__tmp_1872 + 7.U) := __tmp_1873(63, 56)

        val __tmp_1874 = SP - 32.U
        val __tmp_1875 = (generalRegFiles(4.U)).asUInt
        arrayRegFiles(__tmp_1874 + 0.U) := __tmp_1875(7, 0)
        arrayRegFiles(__tmp_1874 + 1.U) := __tmp_1875(15, 8)
        arrayRegFiles(__tmp_1874 + 2.U) := __tmp_1875(23, 16)
        arrayRegFiles(__tmp_1874 + 3.U) := __tmp_1875(31, 24)
        arrayRegFiles(__tmp_1874 + 4.U) := __tmp_1875(39, 32)
        arrayRegFiles(__tmp_1874 + 5.U) := __tmp_1875(47, 40)
        arrayRegFiles(__tmp_1874 + 6.U) := __tmp_1875(55, 48)
        arrayRegFiles(__tmp_1874 + 7.U) := __tmp_1875(63, 56)

        val __tmp_1876 = SP - 24.U
        val __tmp_1877 = (generalRegFiles(5.U)).asUInt
        arrayRegFiles(__tmp_1876 + 0.U) := __tmp_1877(7, 0)
        arrayRegFiles(__tmp_1876 + 1.U) := __tmp_1877(15, 8)
        arrayRegFiles(__tmp_1876 + 2.U) := __tmp_1877(23, 16)
        arrayRegFiles(__tmp_1876 + 3.U) := __tmp_1877(31, 24)
        arrayRegFiles(__tmp_1876 + 4.U) := __tmp_1877(39, 32)
        arrayRegFiles(__tmp_1876 + 5.U) := __tmp_1877(47, 40)
        arrayRegFiles(__tmp_1876 + 6.U) := __tmp_1877(55, 48)
        arrayRegFiles(__tmp_1876 + 7.U) := __tmp_1877(63, 56)

        val __tmp_1878 = SP - 16.U
        val __tmp_1879 = (generalRegFiles(6.U)).asUInt
        arrayRegFiles(__tmp_1878 + 0.U) := __tmp_1879(7, 0)
        arrayRegFiles(__tmp_1878 + 1.U) := __tmp_1879(15, 8)
        arrayRegFiles(__tmp_1878 + 2.U) := __tmp_1879(23, 16)
        arrayRegFiles(__tmp_1878 + 3.U) := __tmp_1879(31, 24)
        arrayRegFiles(__tmp_1878 + 4.U) := __tmp_1879(39, 32)
        arrayRegFiles(__tmp_1878 + 5.U) := __tmp_1879(47, 40)
        arrayRegFiles(__tmp_1878 + 6.U) := __tmp_1879(55, 48)
        arrayRegFiles(__tmp_1878 + 7.U) := __tmp_1879(63, 56)

        val __tmp_1880 = SP - 8.U
        val __tmp_1881 = (generalRegFiles(7.U)).asUInt
        arrayRegFiles(__tmp_1880 + 0.U) := __tmp_1881(7, 0)
        arrayRegFiles(__tmp_1880 + 1.U) := __tmp_1881(15, 8)
        arrayRegFiles(__tmp_1880 + 2.U) := __tmp_1881(23, 16)
        arrayRegFiles(__tmp_1880 + 3.U) := __tmp_1881(31, 24)
        arrayRegFiles(__tmp_1880 + 4.U) := __tmp_1881(39, 32)
        arrayRegFiles(__tmp_1880 + 5.U) := __tmp_1881(47, 40)
        arrayRegFiles(__tmp_1880 + 6.U) := __tmp_1881(55, 48)
        arrayRegFiles(__tmp_1880 + 7.U) := __tmp_1881(63, 56)

        CP := 4.U
      }

      is(15.U) {
        /*
        $1 = *(SP + 833) [signed, Z, 8]  // $1 = acc
        goto .16
        */


        val __tmp_1882 = (SP + 833.U).asUInt
        generalRegFiles(1.U) := Cat(
          arrayRegFiles(__tmp_1882 + 7.U),
          arrayRegFiles(__tmp_1882 + 6.U),
          arrayRegFiles(__tmp_1882 + 5.U),
          arrayRegFiles(__tmp_1882 + 4.U),
          arrayRegFiles(__tmp_1882 + 3.U),
          arrayRegFiles(__tmp_1882 + 2.U),
          arrayRegFiles(__tmp_1882 + 1.U),
          arrayRegFiles(__tmp_1882 + 0.U)
        ).asUInt

        CP := 16.U
      }

      is(16.U) {
        /*
        **(SP + 1) = $1 [signed, Z, 8]  // $res = $1
        goto $ret@0
        */


        val __tmp_1883 = Cat(
          arrayRegFiles(SP + 1.U + 1.U),
          arrayRegFiles(SP + 1.U + 0.U)
        )
        val __tmp_1884 = (generalRegFiles(1.U).asSInt).asUInt
        arrayRegFiles(__tmp_1883 + 0.U) := __tmp_1884(7, 0)
        arrayRegFiles(__tmp_1883 + 1.U) := __tmp_1884(15, 8)
        arrayRegFiles(__tmp_1883 + 2.U) := __tmp_1884(23, 16)
        arrayRegFiles(__tmp_1883 + 3.U) := __tmp_1884(31, 24)
        arrayRegFiles(__tmp_1883 + 4.U) := __tmp_1884(39, 32)
        arrayRegFiles(__tmp_1883 + 5.U) := __tmp_1884(47, 40)
        arrayRegFiles(__tmp_1883 + 6.U) := __tmp_1884(55, 48)
        arrayRegFiles(__tmp_1883 + 7.U) := __tmp_1884(63, 56)

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


        val __tmp_1885 = (SP - 64.U).asUInt
        generalRegFiles(0.U) := Cat(
          arrayRegFiles(__tmp_1885 + 7.U),
          arrayRegFiles(__tmp_1885 + 6.U),
          arrayRegFiles(__tmp_1885 + 5.U),
          arrayRegFiles(__tmp_1885 + 4.U),
          arrayRegFiles(__tmp_1885 + 3.U),
          arrayRegFiles(__tmp_1885 + 2.U),
          arrayRegFiles(__tmp_1885 + 1.U),
          arrayRegFiles(__tmp_1885 + 0.U)
        ).asUInt

        val __tmp_1886 = (SP - 56.U).asUInt
        generalRegFiles(1.U) := Cat(
          arrayRegFiles(__tmp_1886 + 7.U),
          arrayRegFiles(__tmp_1886 + 6.U),
          arrayRegFiles(__tmp_1886 + 5.U),
          arrayRegFiles(__tmp_1886 + 4.U),
          arrayRegFiles(__tmp_1886 + 3.U),
          arrayRegFiles(__tmp_1886 + 2.U),
          arrayRegFiles(__tmp_1886 + 1.U),
          arrayRegFiles(__tmp_1886 + 0.U)
        ).asUInt

        val __tmp_1887 = (SP - 48.U).asUInt
        generalRegFiles(2.U) := Cat(
          arrayRegFiles(__tmp_1887 + 7.U),
          arrayRegFiles(__tmp_1887 + 6.U),
          arrayRegFiles(__tmp_1887 + 5.U),
          arrayRegFiles(__tmp_1887 + 4.U),
          arrayRegFiles(__tmp_1887 + 3.U),
          arrayRegFiles(__tmp_1887 + 2.U),
          arrayRegFiles(__tmp_1887 + 1.U),
          arrayRegFiles(__tmp_1887 + 0.U)
        ).asUInt

        val __tmp_1888 = (SP - 40.U).asUInt
        generalRegFiles(3.U) := Cat(
          arrayRegFiles(__tmp_1888 + 7.U),
          arrayRegFiles(__tmp_1888 + 6.U),
          arrayRegFiles(__tmp_1888 + 5.U),
          arrayRegFiles(__tmp_1888 + 4.U),
          arrayRegFiles(__tmp_1888 + 3.U),
          arrayRegFiles(__tmp_1888 + 2.U),
          arrayRegFiles(__tmp_1888 + 1.U),
          arrayRegFiles(__tmp_1888 + 0.U)
        ).asUInt

        val __tmp_1889 = (SP - 32.U).asUInt
        generalRegFiles(4.U) := Cat(
          arrayRegFiles(__tmp_1889 + 7.U),
          arrayRegFiles(__tmp_1889 + 6.U),
          arrayRegFiles(__tmp_1889 + 5.U),
          arrayRegFiles(__tmp_1889 + 4.U),
          arrayRegFiles(__tmp_1889 + 3.U),
          arrayRegFiles(__tmp_1889 + 2.U),
          arrayRegFiles(__tmp_1889 + 1.U),
          arrayRegFiles(__tmp_1889 + 0.U)
        ).asUInt

        val __tmp_1890 = (SP - 24.U).asUInt
        generalRegFiles(5.U) := Cat(
          arrayRegFiles(__tmp_1890 + 7.U),
          arrayRegFiles(__tmp_1890 + 6.U),
          arrayRegFiles(__tmp_1890 + 5.U),
          arrayRegFiles(__tmp_1890 + 4.U),
          arrayRegFiles(__tmp_1890 + 3.U),
          arrayRegFiles(__tmp_1890 + 2.U),
          arrayRegFiles(__tmp_1890 + 1.U),
          arrayRegFiles(__tmp_1890 + 0.U)
        ).asUInt

        val __tmp_1891 = (SP - 16.U).asUInt
        generalRegFiles(6.U) := Cat(
          arrayRegFiles(__tmp_1891 + 7.U),
          arrayRegFiles(__tmp_1891 + 6.U),
          arrayRegFiles(__tmp_1891 + 5.U),
          arrayRegFiles(__tmp_1891 + 4.U),
          arrayRegFiles(__tmp_1891 + 3.U),
          arrayRegFiles(__tmp_1891 + 2.U),
          arrayRegFiles(__tmp_1891 + 1.U),
          arrayRegFiles(__tmp_1891 + 0.U)
        ).asUInt

        val __tmp_1892 = (SP - 8.U).asUInt
        generalRegFiles(7.U) := Cat(
          arrayRegFiles(__tmp_1892 + 7.U),
          arrayRegFiles(__tmp_1892 + 6.U),
          arrayRegFiles(__tmp_1892 + 5.U),
          arrayRegFiles(__tmp_1892 + 4.U),
          arrayRegFiles(__tmp_1892 + 3.U),
          arrayRegFiles(__tmp_1892 + 2.U),
          arrayRegFiles(__tmp_1892 + 1.U),
          arrayRegFiles(__tmp_1892 + 0.U)
        ).asUInt

        val __tmp_1893 = (Cat(
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
          arrayRegFiles(__tmp_1893 + 1.U),
          arrayRegFiles(__tmp_1893 + 0.U)
        ).asUInt

        SP := SP - 913.U

        CP := 18.U
      }

      is(18.U) {
        /*
        **(SP + 1) = $8 [signed, Z, 8]  // $res = $8
        goto $ret@0
        */


        val __tmp_1894 = Cat(
          arrayRegFiles(SP + 1.U + 1.U),
          arrayRegFiles(SP + 1.U + 0.U)
        )
        val __tmp_1895 = (generalRegFiles(8.U).asSInt).asUInt
        arrayRegFiles(__tmp_1894 + 0.U) := __tmp_1895(7, 0)
        arrayRegFiles(__tmp_1894 + 1.U) := __tmp_1895(15, 8)
        arrayRegFiles(__tmp_1894 + 2.U) := __tmp_1895(23, 16)
        arrayRegFiles(__tmp_1894 + 3.U) := __tmp_1895(31, 24)
        arrayRegFiles(__tmp_1894 + 4.U) := __tmp_1895(39, 32)
        arrayRegFiles(__tmp_1894 + 5.U) := __tmp_1895(47, 40)
        arrayRegFiles(__tmp_1894 + 6.U) := __tmp_1895(55, 48)
        arrayRegFiles(__tmp_1894 + 7.U) := __tmp_1895(63, 56)

        CP := Cat(
          arrayRegFiles(SP + 0.U + 0.U)
        )

      }

    }

}
