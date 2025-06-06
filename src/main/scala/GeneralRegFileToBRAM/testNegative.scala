package GeneralRegFileToBRAM 

import chisel3._
import chiseltest._
import chiseltest.simulator.WriteVcdAnnotation
import org.scalatest.flatspec.AnyFlatSpec

class TestNegative extends Module {
    val io = IO(new Bundle{
        val a = Input(UInt(4.W))
        val out = Output(UInt(20.W))
    })
    io.out := (-8).S(20.W).asUInt
}