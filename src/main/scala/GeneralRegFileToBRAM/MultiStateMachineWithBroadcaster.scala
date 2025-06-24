package GeneralRegFileToBRAM 

import chisel3._
import chisel3.util._
import chisel3.experimental._

class StateBundle extends Bundle {
  val index = UInt(4.W)
  val state = UInt(8.W)
}

class BroadcastBufferIO[T <: Data](gen: T, n: Int) extends Bundle {
  val in  = Flipped(Vec(n, Decoupled(gen)))
  val out = Vec(n, Decoupled(gen))
}

class BroadcastBuffer[T <: Data](gen: T, n: Int) extends Module {
  val io = IO(new BroadcastBufferIO(gen, n))

  // input data related register
  val inValidReg = RegInit(VecInit(Seq.fill(n)(false.B)))
  val inBitsReg  = Reg(Vec(n, gen))

  // valid and data related register
  val validReg = RegInit(false.B)
  val dataReg  = Reg(gen)

  // find the valid input
  val anyInputValid = inValidReg.reduce(_ || _)
  val selectedIdx = Wire(UInt(log2Ceil(n).W))
  val selectedData = Wire(gen)

  // output related register
  val outValidReg = RegNext(validReg, init = false.B)
  val outDataReg  = RegEnable(dataReg, enable = validReg)

  // register for every inputs
  for (i <- 0 until n) {
    inValidReg(i) := io.in(i).valid
    inBitsReg(i) := io.in(i).bits
    io.in(i).ready := !inValidReg(i) && !validReg
  }

  selectedIdx := 0.U
  selectedData := inBitsReg(0)
  for (i <- 0 until n) {
    when (inValidReg(i)) {
      selectedIdx := i.U 
      selectedData := inBitsReg(i)
    }
  }

  // accept the input data
  when (!validReg && anyInputValid) {
    dataReg := selectedData
    validReg := true.B
  }

  // broadcast output
  for (i <- 0 until n) {
    io.out(i).valid := outValidReg
    io.out(i).bits := outDataReg
  }

  // wait all consumer ready
  val allFired = io.out.map(_.ready).reduce(_ && _) && outValidReg
  when (allFired) {
    validReg := false.B
  }
}

class MultiStateMachineWithBroadcastBuffer extends Module {
  val io = IO(new Bundle{
    val start = Input(Bool())
    val in    = Input(UInt(32.W))
    val out   = Output(UInt(32.W))
  })

  val r_out           = Reg(UInt(32.W))
  val CP              = RegInit(VecInit(Seq(2.U(3.W), 7.U(3.W), 7.U(3.W))))
  val broadcastBuffer = Module(new BroadcastBuffer(new StateBundle, 3))
  val adder           = Module(new AdderUnsigned64())

  adder.io.a := 0.U
  adder.io.b := 0.U
  adder.io.start := false.B

  for(i <- 0 until 3) {
    broadcastBuffer.io.in(i).valid := false.B 
    broadcastBuffer.io.in(i).bits.index := 0.U
    broadcastBuffer.io.in(i).bits.state := 7.U
    
    broadcastBuffer.io.out(i).ready := true.B
  }

  io.out := r_out

  // CP0
  switch(CP(0)) {
    is(2.U) {
        CP(0) := Mux(io.start, 3.U, 2.U) 
    }
    is(3.U) { 
        CP(0) := 4.U
    }
    is(4.U) {
        broadcastBuffer.io.in(0).valid      := true.B
        broadcastBuffer.io.in(0).bits.index := 1.U
        broadcastBuffer.io.in(0).bits.state := 0.U

        CP(0) := 7.U
    }
    is(7.U) { 
        when(broadcastBuffer.io.out(0).valid & broadcastBuffer.io.out(0).bits.index === 0.U) {
            CP(0) := broadcastBuffer.io.out(0).bits.state
        }
    }
  }

  // CP1
  switch(CP(1)) {
    is(0.U) {
        CP(1) := 1.U
    }
    is(1.U) {
        CP(1) := 2.U
    }
    is(2.U) { 
        CP(1) := 3.U 
    }
    is(3.U) {
        CP(1) := 4.U
    }
    is(4.U) {
        adder.io.a := io.in
        adder.io.b := 2.U
        adder.io.start := true.B
        when(adder.io.valid) {
          r_out := adder.io.out
          broadcastBuffer.io.in(1).valid      := true.B
          broadcastBuffer.io.in(1).bits.index := 2.U
          broadcastBuffer.io.in(1).bits.state := 2.U

          CP(1) := 7.U
        }
    }
    is(7.U) { 
        when(broadcastBuffer.io.out(1).valid & broadcastBuffer.io.out(1).bits.index === 1.U) {
            CP(1) := broadcastBuffer.io.out(1).bits.state
        }
    }
  }

  // CP2
  switch(CP(2)) {
    is(2.U) {
        broadcastBuffer.io.in(2).valid      := true.B
        broadcastBuffer.io.in(2).bits.index := 0.U
        broadcastBuffer.io.in(2).bits.state := 2.U

        CP(2) := 7.U
    }
    is(7.U) { 
        when(broadcastBuffer.io.out(2).valid & broadcastBuffer.io.out(2).bits.index === 2.U) {
            CP(2) := broadcastBuffer.io.out(2).bits.state
        }
    }
  }

}