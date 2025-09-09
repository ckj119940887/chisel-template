package TestArbiter

import chisel3._
import chisel3.util._
import chisel3.experimental._

class NewUshrSigned64(val width: Int = 64) extends Module {
    val io = IO(new Bundle{
        val a = Input(SInt(width.W))
        val b = Input(UInt(width.W))
        val start = Input(Bool())
        val valid = Output(Bool())
        val out = Output(SInt(width.W))
    })

    val big  = RegNext(io.b >= width.U)
    val sh   = RegNext(io.b(5, 0))
    val aU   = RegNext(io.a.asUInt)

    val r_busy       = RegInit(true.B)

    val v1 = RegNext(io.start, init=false.B)
    val v2 = RegNext(v1,       init=false.B)
    val v3 = RegNext(v2,       init=false.B)
    val v4 = RegNext(v3,       init=false.B)
    val v5 = RegNext(v4,       init=false.B)
    val v6 = RegNext(v5,       init=false.B)
    val v7 = RegNext(v6,       init=false.B)
    val v8 = RegNext(v7,       init=false.B)
    val v9 = RegNext(v8,       init=false.B)

    when(v8 & ~v9) {
        r_busy := false.B
    } .elsewhen(io.valid) {
        r_busy := true.B
    }

    val s1 = RegNext(Mux(sh(0), aU >> 1, aU))
    val s2 = RegNext(Mux(sh(1), s1 >> 2, s1))
    val s3 = RegNext(Mux(sh(2), s2 >> 4, s2))
    val s4 = RegNext(Mux(sh(3), s3 >> 8, s3))
    val s5 = RegNext(Mux(sh(4), s4 >> 16, s4))
    val s6 = RegNext(Mux(sh(5), s5 >> 32, s5))
    val s7 = RegNext(Mux(big, 0.U, s6))

    io.valid := v1 & ~r_busy
    io.out := s7.asSInt
}