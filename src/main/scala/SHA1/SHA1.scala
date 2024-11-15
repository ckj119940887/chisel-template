package SHA1
import chisel3._ 
import chisel3.util._ 

class SHA1 extends Module {
     val io = IO(new Bundle {
        val valid = Input(Bool()) 
        val ready = Output(Bool()) 
    })

    val state = RegInit(15.U(4.W))

    val i = Reg(UInt(32.W))
    val ok = Reg(Bool())
    val input = RegInit(VecInit(Seq(65.U, 66.U, 67.U)))
    val expected = RegInit(VecInit(Seq(60.U, 1.U, 189.U, 187.U, 38.U, 243.U, 88.U, 186.U, 178.U, 127.U, 38.U, 121.U, 36.U, 170.U, 44.U, 154.U, 3.U, 252.U, 253.U, 184.U)))
    val __m_digest = Module(new digest())
    __m_digest.io.bytes := 0.U
    __m_digest.io.valid := 0.U

    switch(state) {
        is(15.U) {
            state := Mux(io.valid, 0.U, state)
        }
        is(0.U) {
            r := __m_digest.io.out_digest
            __m_digest.io.valid := Mux(__m_digest.io.ready, RegNext(false.B), true.B)
            __m_digest.io.bytes := input
            state := Mux(__m_digest.io.ready, 1.U, state)
        }
        is(1.U) {
            ok :=  true.B
            state := 2.U
        }
        is(2.U) {
            i :=  0.U
            state := 3.U
        }
        is(3.U) {
            state := Mux(i <  20.U, 4.U, 7.U)
        }
        is(4.U) {
            state := Mux(r(i) =/= expected(i), 6.U, state)
        }
        is(5.U) {
            ok :=  false.B
            state := 6.U
        }
        is(6.U) {
            i := i  + 1.U
            state := 3.U
        }
        is(7.U) {
            state := 15.U
        }

    }
    io.ready := state === 7.U
}