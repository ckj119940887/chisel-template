package SHA1
import chisel3._ 
import chisel3.util._ 

class digest extends Module {
     val io = IO(new Bundle {
        val we_bytes = Input(Bool()) 
        val addr_bytes = Input(UInt(5.W)) 
        val din_bytes = Input(UInt(32.W)) 
        val valid = Input(Bool()) 
        val dout_bytes = Output(UInt(32.W)) 
        val ready = Output(Bool()) 
        val addr_digest = Input(UInt(5.W))
        val din_digest = Input(UInt(32.W))
        val dout_digest = Output(UInt(32.W))
        val we_digest = Input(Bool())
    })

    val state = RegInit(63.U(6.W))

    val bytes = Reg(Vec(20,  UInt(32.W)))
    bytes(io.addr_bytes) := Mux(io.we_bytes, io.din_bytes, bytes(io.addr_bytes)) 
    io.dout_bytes := Mux(!io.we_bytes, bytes(io.addr_bytes), DontCare)
    val i = Reg(UInt(32.W))
    val a = Reg(UInt(32.W))
    val b = Reg(UInt(32.W))
    val c = Reg(UInt(32.W))
    val d = Reg(UInt(32.W))
    val e = Reg(UInt(32.W))
    val olda = Reg(UInt(32.W))
    val oldb = Reg(UInt(32.W))
    val oldc = Reg(UInt(32.W))
    val oldd = Reg(UInt(32.W))
    val olde = Reg(UInt(32.W))
    val j = Reg(UInt(32.W))
    val t = Reg(UInt(32.W))
    val bytesLength = Reg(UInt(32.W))
    val blksLength = Reg(UInt(32.W))
    val temp = Reg(UInt(32.W))
    val blks = Reg(Vec(blksLength,  UInt(32.W)))
    val w = Reg(Vec( 80,  UInt(32.W)))
    val __m_rol = Module(new rol())
    __m_rol.io.num := 0.U
    __m_rol.io.cnt := 0.U
    __m_rol.io.valid := 0.U
    val __m_rol = Module(new rol())
    __m_rol.io.num := 0.U
    __m_rol.io.cnt := 0.U
    __m_rol.io.valid := 0.U
    val __m_rol = Module(new rol())
    __m_rol.io.num := 0.U
    __m_rol.io.cnt := 0.U
    __m_rol.io.valid := 0.U
    val digest = Reg(Vec( 20,  UInt(32.W)))
    val __m_fill = Module(new fill())
    __m_fill.io.value := 0.U
    __m_fill.io.arr := 0.U
    __m_fill.io.off := 0.U
    __m_fill.io.valid := 0.U
    val __m_fill = Module(new fill())
    __m_fill.io.value := 0.U
    __m_fill.io.arr := 0.U
    __m_fill.io.off := 0.U
    __m_fill.io.valid := 0.U
    val __m_fill = Module(new fill())
    __m_fill.io.value := 0.U
    __m_fill.io.arr := 0.U
    __m_fill.io.off := 0.U
    __m_fill.io.valid := 0.U
    val __m_fill = Module(new fill())
    __m_fill.io.value := 0.U
    __m_fill.io.arr := 0.U
    __m_fill.io.off := 0.U
    __m_fill.io.valid := 0.U
    val __m_fill = Module(new fill())
    __m_fill.io.value := 0.U
    __m_fill.io.arr := 0.U
    __m_fill.io.off := 0.U
    __m_fill.io.valid := 0.U
    digest(io.addr_digest) := Mux(io.we_digest, io.din_digest, digest(io.addr_digest)) 
    io.dout_digest := Mux(!io.we_digest, digest(io.addr_digest), DontCare)

    switch(state) {
        is(63.U) {
            state := Mux(io.valid, 0.U, state)
        }
        is(0.U) {
            bytesLength :=  3.U
            state := 1.U
        }
        is(1.U) {
            blksLength := bytesLength +  8.U >>  6.U +  1.U *  16.U
            state := 2.U
        }
        is(2.U) {
            i :=  0.U
            state := 3.U
        }
        is(3.U) {
            state := Mux(i < bytesLength, 4.U, 6.U)
        }
        is(4.U) {
            blks(i >>  2.U) := blks(i >>  2.U) | bytes(i) <<  24.U - i %  4.U *  8.U
            state := 5.U
        }
        is(5.U) {
            i := i  + 1.U
            state := 3.U
        }
        is(6.U) {
            blks(i >>  2.U) := blks(i >>  2.U) |  128.U <<  24.U - i %  4.U *  8.U
            state := 7.U
        }
        is(7.U) {
            blks(blksLength -  1.U) := bytesLength *  8.U
            state := 8.U
        }
        is(8.U) {
            a :=  1732584193.U
            state := 9.U
        }
        is(9.U) {
            b := 
            state := 10.U
        }
        is(10.U) {
            c := 
            state := 11.U
        }
        is(11.U) {
            d :=  271733878.U
            state := 12.U
        }
        is(12.U) {
            e := 
            state := 13.U
        }
        is(13.U) {
            i :=  0.U
            state := 14.U
        }
        is(14.U) {
            state := Mux(i < blksLength, 15.U, 45.U)
        }
        is(15.U) {
            olda := a
            state := 16.U
        }
        is(16.U) {
            oldb := b
            state := 17.U
        }
        is(17.U) {
            oldc := c
            state := 18.U
        }
        is(18.U) {
            oldd := d
            state := 19.U
        }
        is(19.U) {
            olde := e
            state := 20.U
        }
        is(20.U) {
            j :=  0.U
            state := 21.U
        }
        is(21.U) {
            state := Mux(j <  80.U, 22.U, 39.U)
        }
        is(22.U) {
            state := Mux(j <  16.U, 24.U, state)
        }
        is(23.U) {
            w(j) := blks(i + j)
            state := 25.U
        }
        is(24.U) {
            w(j) := __m_rol.io.out_rol
            __m_rol.io.valid := Mux(__m_rol.io.ready, RegNext(false.B), true.B)
            __m_rol.io.num := w(j -  3.U) ^ w(j -  8.U) ^ w(j -  14.U) ^ w(j -  16.U)
            __m_rol.io.cnt :=  1.U
            state := Mux(__m_rol.io.ready, 25.U, state)
        }
        is(25.U) {
            state := Mux(j <  20.U, 27.U, state)
        }
        is(26.U) {
            temp := ~ 1518500249.U + b & c | b & d
            state := 32.U
        }
        is(27.U) {
            state := Mux(j <  40.U, 29.U, state)
        }
        is(28.U) {
            temp :=  1859775393.U + b ^ c ^ d
            state := 32.U
        }
        is(29.U) {
            state := Mux(j <  60.U, 31.U, state)
        }
        is(30.U) {
            temp :=  + b & c | b & d | c & d
            state := 32.U
        }
        is(31.U) {
            temp :=  + b ^ c ^ d
            state := 32.U
        }
        is(32.U) {
            t := __m_rol.io.out_rol + e + w(j) + temp
            state := 33.U
        }
        is(33.U) {
            e := d
            state := 34.U
        }
        is(34.U) {
            d := c
            state := 35.U
        }
        is(35.U) {
            c := __m_rol.io.out_rol
            __m_rol.io.valid := Mux(__m_rol.io.ready, RegNext(false.B), true.B)
            __m_rol.io.num := a
            __m_rol.io.cnt :=  5.U
            __m_rol.io.num := b
            __m_rol.io.cnt :=  30.U
            state := Mux(__m_rol.io.ready, 36.U, state)
        }
        is(36.U) {
            b := a
            state := 37.U
        }
        is(37.U) {
            a := t
            state := 38.U
        }
        is(38.U) {
            j := j  + 1.U
            state := 21.U
        }
        is(39.U) {
            a := a + olda
            state := 40.U
        }
        is(40.U) {
            b := b + oldb
            state := 41.U
        }
        is(41.U) {
            c := c + oldc
            state := 42.U
        }
        is(42.U) {
            d := d + oldd
            state := 43.U
        }
        is(43.U) {
            e := e + olde
            state := 44.U
        }
        is(44.U) {
            i := i +  16.U
            state := 45.U
        }
        is(45.U) {
            state := 63.U
        }

    }
    io.ready := state === 45.U
}