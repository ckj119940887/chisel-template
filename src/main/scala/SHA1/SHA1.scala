package SHA1
import chisel3._ 
import chisel3.util._ 

class SHA1 extends Module {
     val io = IO(new Bundle {
        val valid = Input(Bool()) 
        val ready = Output(Bool()) 
    })

    val i = Reg(SInt(32.W))
    val ok = Reg(Bool())
    val input = RegInit(VecInit(Seq(65.S, 66.S, 67.S)))
    val expected = RegInit(VecInit(Seq(60.S, 1.S, 189.S, 187.S, 38.S, 243.S, 88.S, 186.S, 178.S, 127.S, 38.S, 121.S, 36.S, 170.S, 44.S, 154.S, 3.S, 252.S, 253.S, 184.S)))

    io.ready := io.valid
}