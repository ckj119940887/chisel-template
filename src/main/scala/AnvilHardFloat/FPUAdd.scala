package AnvilHardFloat

import chisel3._
import chisel3.util._
import chisel3.experimental._

class AnvilFPUAdd(val expWidth: Int, val sigWidth: Int) extends Module {
    val io = IO(new Bundle {
        val subOp = Input(Bool())
        val a = Input(Bits((expWidth + sigWidth).W))
        val b = Input(Bits((expWidth + sigWidth).W))
        val roundingMode = Input(UInt(3.W))
        val detectTininess = Input(Bool())
        val out = Output(UInt((expWidth + sigWidth + 1).W))
        val exceptionFlags = Output(UInt(5.W))
        val expectOut = Output(UInt((expWidth + sigWidth).W))
    })

    val addRecFN = Module(new AddRecFN(expWidth, sigWidth))

    val rawA = Module(new AnvilRawFloatFromFN(expWidth, sigWidth))
    rawA.io.in := io.a

    val rawB = Module(new AnvilRawFloatFromFN(expWidth, sigWidth))
    rawB.io.in := io.b

    addRecFN.io.subOp := io.subOp
    addRecFN.io.a := rawA.io.out.sign ##
          (Mux(rawA.io.out.isZero, 0.U(3.W), rawA.io.out.sExp(expWidth, expWidth - 2)) | Mux(rawA.io.out.isNaN, 1.U, 0.U)) ##
          rawA.io.out.sExp(expWidth - 3, 0) ## rawA.io.out.sig(sigWidth - 2, 0)
    addRecFN.io.b := rawB.io.out.sign ##
          (Mux(rawB.io.out.isZero, 0.U(3.W), rawB.io.out.sExp(expWidth, expWidth - 2)) | Mux(rawB.io.out.isNaN, 1.U, 0.U)) ##
          rawB.io.out.sExp(expWidth - 3, 0) ## rawB.io.out.sig(sigWidth - 2, 0)
    addRecFN.io.roundingMode := io.roundingMode
    addRecFN.io.detectTininess := io.detectTininess
    io.exceptionFlags := addRecFN.io.exceptionFlags
    io.out := addRecFN.io.out

    val expectOutMod = Module(new AnvilFNFromRecFN(expWidth, sigWidth))
    expectOutMod.io.in := addRecFN.io.out
    io.expectOut := expectOutMod.io.out
}