package GeneralRegFileToBRAM 

import chisel3._
import chisel3.util._
import chisel3.experimental._

class CombinationalLoopWire extends Module {
    val io = IO(new Bundle{
        val start = Input(Bool())
        val in    = Input(UInt(32.W))
        val out   = Output(UInt(32.W))
    })

    val initVals = Seq(1.U(2.W), 0.U(2.W))
    val CP       = VecInit(initVals.map(v => RegInit(v)))
    val nextCP   = WireInit(VecInit(initVals))
    val r_temp   = RegInit(0.U(32.W))

    switch(CP(0.U)) {
        is(1.U) {
            nextCP(0.U) := Mux(io.start, 2.U, CP(0.U))
        }
        is(2.U) {
            nextCP(0.U) := 3.U
            nextCP(1.U) := 1.U
            r_temp  := io.in + 4.U
        }
    }
    CP(0.U) := nextCP(0.U)

    switch(CP(1.U)) {
        is(1.U) {
            nextCP(1.U) := 2.U
        }
        is(2.U) {
            nextCP(1.U) := 3.U
            nextCP(0.U) := 1.U
        }
    }
    CP(1.U) := nextCP(1.U)

    io.out := r_temp
}

class CombinationalLoopIndex extends Module {
    val io = IO(new Bundle{
        val start = Input(Bool())
        val in    = Input(UInt(32.W))
        val out   = Output(UInt(32.W))
    })

    val initVals = Seq(1.U(2.W), 0.U(2.W))
    val CP       = VecInit(initVals.map(v => RegInit(v)))
    val indexCP  = RegInit(0.U(2.W))
    val stateCP  = RegInit(3.U(2.W))
    val r_temp   = RegInit(0.U(32.W))

    switch(CP(0.U)) {
        is(1.U) {
            CP(0.U) := Mux(io.start, 2.U, 1.U)
        }
        is(2.U) {
            CP(0.U) := 3.U

            //CP(1.U) := 1.U
            indexCP := 1.U
            stateCP := 1.U

            r_temp  := io.in + 4.U
        }
    }

    switch(CP(1.U)) {
        is(1.U) {
            CP(1.U) := 2.U
        }
        is(2.U) {
            CP(1.U) := 3.U
            //CP(0.U) := 1.U
            indexCP := 0.U
            stateCP := 1.U
        }
    }

    when(indexCP =/= 3.U) {
        indexCP := 3.U
        CP(indexCP) := RegNext(stateCP(0.U))
    }

    io.out := r_temp
}

class AdderUnsigned64(val width: Int = 64) extends Module {
    val io = IO(new Bundle{
        val a = Input(UInt(width.W))
        val b = Input(UInt(width.W))
        val start = Input(Bool())
        val out = Output(UInt(width.W))
        val valid = Output(Bool())
    })

    val state = RegInit(0.U(2.W))
    val regA = Reg(UInt(width.W))
    val regB = Reg(UInt(width.W))
    val result = Reg(UInt(width.W))

    io.valid := Mux(state === 2.U, true.B, false.B)
    io.out := Mux(state === 2.U, result, 0.U)
    switch(state) {
        is(0.U) {
            state := Mux(io.start, 1.U, 0.U)
            regA := Mux(io.start, io.a, regA)
            regB := Mux(io.start, io.b, regB)
        }
        is(1.U) {
            result := regA + regB
            state := 2.U
        }
        is(2.U) {
            state := 0.U
        }
    }
}

class CombinationalLoopWireRegNext extends Module {
    val io = IO(new Bundle{
        val start = Input(Bool())
        val in    = Input(UInt(32.W))
        val out   = Output(UInt(32.W))
    })

    val adder = Module(new AdderUnsigned64(64))
    adder.io.a     := 0.U
    adder.io.b     := 0.U
    adder.io.start := false.B

    val initVals = Seq(2.U(3.W), 7.U(3.W))
    val CP       = VecInit(initVals.map(v => RegInit(v)))
    val wireInitVals = Seq(7.U(3.W), 7.U(3.W))
    val nextCP   = VecInit(initVals.map(v => RegInit(v))) //WireInit(VecInit(wireInitVals))//Reg(Vec(2, UInt(3.W)))//
    val r_temp   = RegInit(0.U(32.W))

    switch(CP(0.U)) {
        is(1.U) {
            nextCP(0.U) := Mux(io.start, 2.U, 1.U)
        }
        is(2.U) {
            nextCP(0.U) := 3.U
        }
        is(3.U) {

            adder.io.a     := io.in
            adder.io.b     := 4.U
            adder.io.start := Mux(adder.io.valid, false.B, true.B)

            nextCP(0.U) := 3.U
            when(adder.io.valid) {
              nextCP(0.U) := 7.U
              nextCP(1.U) := 1.U
              r_temp  := adder.io.out
            }
        }
    }
    when(io.start) {
      CP(0.U) := RegNext(nextCP(0.U))
    }

    switch(CP(1.U)) {
        is(1.U) {
            nextCP(1.U) := 2.U
        }
        is(2.U) {
            nextCP(1.U) := 3.U
        }
        is(3.U) {
            nextCP(1.U) := 1.U
            nextCP(0.U) := 3.U
        }
    }
    when(io.start) {
      CP(1.U) := RegNext(nextCP(1.U))
    }

    io.out := r_temp
}