package GeneralRegFileToBRAM 

import chisel3._
import chisel3.util._
import chisel3.experimental._

class GeneralInputPort(val dataWidth: Int = 64, val indexWidth: Int = 4) extends Bundle {
    val a = UInt(dataWidth.W)
    val b = UInt(dataWidth.W)
    val bypassA = Bool()
    val bypassB = Bool()
    val bypassAIndex = UInt(indexWidth.W)
    val bypassBIndex = UInt(indexWidth.W)
}

class GeneralOutputPort(val dataWidth: Int = 64) extends Bundle {
    val ready = Bool()
    val out = UInt(dataWidth.W)
}

class Pipeline(val numOfComponents: Int = 2, val width: Int = 64) extends Module{
    val io = IO(new Bundle{
        val inVec = Vec(numOfComponents, Flipped(DecoupledIO(new GeneralInputPort(width, log2Ceil(numOfComponents)))))
        val outVec = Output(Vec(numOfComponents, new GeneralOutputPort(width)))
    })

    val results = Reg(Vec(numOfComponents, UInt(width.W)))
    val adderUnsigned = Module(new AdderUnsigned(width))
    adderUnsigned.io.op := true.B
    val andUnsigned = Module(new AndUnsigned(width))
    val modules = Seq(adderUnsigned.io, andUnsigned.io)

    /*
    for(i <- 0 until numOfComponents) {
        modules(i).a := Mux(RegNext(io.inVec(i).bits.bypassA), results(io.inVec(i).bits.bypassAIndex), RegNext(io.inVec(i).bits.a))
        modules(i).b := Mux(RegNext(io.inVec(i).bits.bypassB), results(io.inVec(i).bits.bypassBIndex), RegNext(io.inVec(i).bits.b))
        io.inVec(i).ready := RegNext(Mux(io.inVec(i).valid, true.B, false.B))
        results(i) := Mux(io.inVec(i).ready, modules(i).out, results(i))

        io.outVec(i).ready := RegNext(RegNext(io.inVec(i).valid))
        io.outVec(i).out := RegNext(modules(i).out)
    }
    */
}