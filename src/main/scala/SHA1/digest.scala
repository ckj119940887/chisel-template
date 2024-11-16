package SHA1
import chisel3._ 
import chisel3.util._ 

class digest extends Module {
     val io = IO(new Bundle {
        val bytes = Input(Vec(80, SInt(32.W))) 
        val valid = Input(Bool()) 
        val bytes_out = Output(Vec(80, SInt(32.W))) 
        val ready = Output(Bool()) 
        val out_digest = Output(Vec(80, SInt(32.W))) 
    })

    val state = RegInit(63.U(6.W))
    when(reset.asBool()) {
        state := 63.U
    }
    val bytes = Reg(Vec(80,  SInt(32.W)))
    when(RegNext(!io.valid)){
        bytes := io.bytes
    }
    io.bytes_out <> bytes
    val i = Reg(SInt(32.W))
    val a = Reg(SInt(32.W))
    val b = Reg(SInt(32.W))
    val c = Reg(SInt(32.W))
    val d = Reg(SInt(32.W))
    val e = Reg(SInt(32.W))
    val olda = Reg(SInt(32.W))
    val oldb = Reg(SInt(32.W))
    val oldc = Reg(SInt(32.W))
    val oldd = Reg(SInt(32.W))
    val olde = Reg(SInt(32.W))
    val j = Reg(SInt(32.W))
    val t = Reg(SInt(32.W))
    val bytesLength = Reg(SInt(32.W))
    val blksLength = Reg(SInt(32.W))
    val temp = Reg(SInt(32.W))
    val blks = Reg(Vec(80,  SInt(32.W)))
    val w = Reg(Vec(80,  SInt(32.W)))
    val __m_rol_0 = Module(new rol())
    __m_rol_0.io.num := DontCare
    __m_rol_0.io.cnt := DontCare
    __m_rol_0.io.valid := false.B
    val __m_rol_1 = Module(new rol())
    __m_rol_1.io.num := DontCare
    __m_rol_1.io.cnt := DontCare
    __m_rol_1.io.valid := false.B
    val __m_rol_2 = Module(new rol())
    __m_rol_2.io.num := DontCare
    __m_rol_2.io.cnt := DontCare
    __m_rol_2.io.valid := false.B
    val digest = Reg(Vec(80,  SInt(32.W)))
    val __m_fill_0 = Module(new fill())
    __m_fill_0.io.value := DontCare
    __m_fill_0.io.arr := DontCare
    __m_fill_0.io.off := DontCare
    __m_fill_0.io.valid := false.B
    val __m_fill_1 = Module(new fill())
    __m_fill_1.io.value := DontCare
    __m_fill_1.io.arr := DontCare
    __m_fill_1.io.off := DontCare
    __m_fill_1.io.valid := false.B
    val __m_fill_2 = Module(new fill())
    __m_fill_2.io.value := DontCare
    __m_fill_2.io.arr := DontCare
    __m_fill_2.io.off := DontCare
    __m_fill_2.io.valid := false.B
    val __m_fill_3 = Module(new fill())
    __m_fill_3.io.value := DontCare
    __m_fill_3.io.arr := DontCare
    __m_fill_3.io.off := DontCare
    __m_fill_3.io.valid := false.B
    val __m_fill_4 = Module(new fill())
    __m_fill_4.io.value := DontCare
    __m_fill_4.io.arr := DontCare
    __m_fill_4.io.off := DontCare
    __m_fill_4.io.valid := false.B
    digest <> io.out_digest

    switch(state) {
        is(63.U) {
            state := Mux(io.valid, 0.U, state)
        }
        is(0.U) {
            bytesLength :=  3.S(32.W)
            state := 1.U
        }
        is(1.U) {
            blksLength := (bytesLength +  8.S(32.W) >> ( 6.S(32.W)).asUInt()(4,0)).asSInt() +  1.S(32.W) *  16.S(32.W)
            state := 2.U
        }
        is(2.U) {
            i :=  0.S(32.W)
            state := 3.U
        }
        is(3.U) {
            state := Mux(i < bytesLength, 4.U, 7.U)
        }
        is(4.U) {
            temp := (bytes((i).asUInt()) << ( 24.S(32.W) - i %  4.S(32.W) *  8.S(32.W)).asUInt()(4,0)).asSInt()
            state := 5.U
        }
        is(5.U) {
            blks(((i >> ( 2.S(32.W)).asUInt()(4,0)).asSInt()).asUInt()) := blks(((i >> ( 2.S(32.W)).asUInt()(4,0)).asSInt()).asUInt()) | temp
            state := 6.U
        }
        is(6.U) {
            i := i  + 1.S
            state := 3.U
        }
        is(7.U) {
            temp := ( 128.S(32.W) << ( 24.S(32.W) - i %  4.S(32.W) *  8.S(32.W)).asUInt()(4,0)).asSInt()
            state := 8.U
        }
        is(8.U) {
            blks(((i >> ( 2.S(32.W)).asUInt()(4,0)).asSInt()).asUInt()) := blks(((i >> ( 2.S(32.W)).asUInt()(4,0)).asSInt()).asUInt()) | temp
            state := 9.U
        }
        is(9.U) {
            blks((blksLength -  1.S(32.W)).asUInt()) := bytesLength *  8.S(32.W)
            state := 10.U
        }
        is(10.U) {
            a :=  1732584193.S(32.W)
            state := 11.U
        }
        is(11.U) {
            b := (- 271733879.S(32.W))
            state := 12.U
        }
        is(12.U) {
            c := (- 1732584194.S(32.W))
            state := 13.U
        }
        is(13.U) {
            d :=  271733878.S(32.W)
            state := 14.U
        }
        is(14.U) {
            e := (- 1009589776.S(32.W))
            state := 15.U
        }
        is(15.U) {
            i :=  0.S(32.W)
            state := 16.U
        }
        is(16.U) {
            state := Mux(i < blksLength, 17.U, 54.U)
        }
        is(17.U) {
            olda := a
            state := 18.U
        }
        is(18.U) {
            oldb := b
            state := 19.U
        }
        is(19.U) {
            oldc := c
            state := 20.U
        }
        is(20.U) {
            oldd := d
            state := 21.U
        }
        is(21.U) {
            olde := e
            state := 22.U
        }
        is(22.U) {
            j :=  0.S(32.W)
            state := 23.U
        }
        is(23.U) {
            state := Mux(j <  80.S(32.W), 24.U, 48.U)
        }
        is(24.U) {
            state := Mux(j <  16.S(32.W), 25.U, 26.U)
        }
        is(25.U) {
            w((j).asUInt()) := blks((i + j).asUInt())
            state := 28.U
        }
        is(26.U) {
            temp := w((j -  3.S(32.W)).asUInt()) ^ w((j -  8.S(32.W)).asUInt()) ^ w((j -  14.S(32.W)).asUInt()) ^ w((j -  16.S(32.W)).asUInt())
            state := 27.U
        }
        is(27.U) {
            w((j).asUInt()) := __m_rol_0.io.out_rol
            __m_rol_0.io.valid := Mux(__m_rol_0.io.ready, RegNext(false.B), true.B)
            __m_rol_0.io.num := temp
            __m_rol_0.io.cnt :=  1.S(32.W)
            state := Mux(__m_rol_0.io.ready, 28.U, state)
        }
        is(28.U) {
            state := Mux(j <  20.S(32.W), 29.U, 32.U)
        }
        is(29.U) {
            temp := b & c
            state := 30.U
        }
        is(30.U) {
            temp := temp | (~b) & d
            state := 31.U
        }
        is(31.U) {
            temp :=  1518500249.S(32.W) + temp
            state := 40.U
        }
        is(32.U) {
            state := Mux(j <  40.S(32.W), 33.U, 35.U)
        }
        is(33.U) {
            temp := b ^ c ^ d
            state := 34.U
        }
        is(34.U) {
            temp :=  1859775393.S(32.W) + temp
            state := 40.U
        }
        is(35.U) {
            state := Mux(j <  60.S(32.W), 36.U, 38.U)
        }
        is(36.U) {
            temp := b & c | b & d | c & d
            state := 37.U
        }
        is(37.U) {
            temp := (- 1894007588.S(32.W)) + temp
            state := 40.U
        }
        is(38.U) {
            temp := b ^ c ^ d
            state := 39.U
        }
        is(39.U) {
            temp := (- 899497514.S(32.W)) + temp
            state := 40.U
        }
        is(40.U) {
            t := __m_rol_1.io.out_rol
            __m_rol_1.io.valid := Mux(__m_rol_1.io.ready, RegNext(false.B), true.B)
            __m_rol_1.io.num := a
            __m_rol_1.io.cnt :=  5.S(32.W)
            state := Mux(__m_rol_1.io.ready, 41.U, state)
        }
        is(41.U) {
            t := t + e + w((j).asUInt()) + temp
            state := 42.U
        }
        is(42.U) {
            e := d
            state := 43.U
        }
        is(43.U) {
            d := c
            state := 44.U
        }
        is(44.U) {
            c := __m_rol_2.io.out_rol
            __m_rol_2.io.valid := Mux(__m_rol_2.io.ready, RegNext(false.B), true.B)
            __m_rol_2.io.num := b
            __m_rol_2.io.cnt :=  30.S(32.W)
            state := Mux(__m_rol_2.io.ready, 45.U, state)
        }
        is(45.U) {
            b := a
            state := 46.U
        }
        is(46.U) {
            a := t
            state := 47.U
        }
        is(47.U) {
            j := j  + 1.S
            state := 23.U
        }
        is(48.U) {
            a := a + olda
            state := 49.U
        }
        is(49.U) {
            b := b + oldb
            state := 50.U
        }
        is(50.U) {
            c := c + oldc
            state := 51.U
        }
        is(51.U) {
            d := d + oldd
            state := 52.U
        }
        is(52.U) {
            e := e + olde
            state := 53.U
        }
        is(53.U) {
            i := i +  16.S(32.W)
            state := 54.U
        }
        is(54.U) {
            __m_fill_0.io.value := a
            __m_fill_0.io.arr := digest
            when(__m_fill_0.io.ready){
                digest := __m_fill_0.io.arr_out
            }
            __m_fill_0.io.off :=  0.S(32.W)
            __m_fill_0.io.valid := Mux(__m_fill_0.io.ready, RegNext(false.B), true.B)
            state := Mux(__m_fill_0.io.ready, 55.U, state)
        }
        is(55.U) {
            __m_fill_1.io.value := b
            __m_fill_1.io.arr := digest
            when(__m_fill_1.io.ready){
                digest := __m_fill_1.io.arr_out
            }
            __m_fill_1.io.off :=  4.S(32.W)
            __m_fill_1.io.valid := Mux(__m_fill_1.io.ready, RegNext(false.B), true.B)
            state := Mux(__m_fill_1.io.ready, 56.U, state)
        }
        is(56.U) {
            __m_fill_2.io.value := c
            __m_fill_2.io.arr := digest
            when(__m_fill_2.io.ready){
                digest := __m_fill_2.io.arr_out
            }
            __m_fill_2.io.off :=  8.S(32.W)
            __m_fill_2.io.valid := Mux(__m_fill_2.io.ready, RegNext(false.B), true.B)
            state := Mux(__m_fill_2.io.ready, 57.U, state)
        }
        is(57.U) {
            __m_fill_3.io.value := d
            __m_fill_3.io.arr := digest
            when(__m_fill_3.io.ready){
                digest := __m_fill_3.io.arr_out
            }
            __m_fill_3.io.off :=  12.S(32.W)
            __m_fill_3.io.valid := Mux(__m_fill_3.io.ready, RegNext(false.B), true.B)
            state := Mux(__m_fill_3.io.ready, 58.U, state)
        }
        is(58.U) {
            __m_fill_4.io.value := e
            __m_fill_4.io.arr := digest
            when(__m_fill_4.io.ready){
                digest := __m_fill_4.io.arr_out
            }
            __m_fill_4.io.off :=  16.S(32.W)
            __m_fill_4.io.valid := Mux(__m_fill_4.io.ready, RegNext(false.B), true.B)
            state := Mux(__m_fill_4.io.ready, 59.U, state)
        }
        is(59.U) {
            state := 63.U
        }

    }
    io.ready := state === 59.U
}