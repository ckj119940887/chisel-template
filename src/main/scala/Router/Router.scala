package Router

import chisel3._
import chisel3.util._
import chisel3.experimental._

class Packet(val idWidth: Int, val dataWidth: Int, val cpWidth: Int) extends Bundle {
  val srcID = UInt(idWidth.W)
  val dstID = UInt(idWidth.W)
  val data  = Bool()
  val srcCP = UInt(cpWidth.W)
  val dstCP = UInt(cpWidth.W)
}

class RouterIO(val nPorts: Int, val idWidth: Int, val dataWidth: Int, val cpWidth: Int) extends Bundle {
  val in  = Flipped(Vec(nPorts, Valid(new Packet(idWidth, dataWidth, cpWidth))))
  val out = Vec(nPorts, Valid(new Packet(idWidth, dataWidth, cpWidth)))
}

class Router(val nPorts: Int, val idWidth: Int, val dataWidth: Int, val cpWidth: Int) extends Module {
  val io = IO(new RouterIO(nPorts, idWidth, dataWidth, cpWidth))

  // input buffer
  val r_inputBuffer       = VecInit(Seq.fill(nPorts)(Reg(new Packet(idWidth, dataWidth, cpWidth))))
  val r_inputBuffer_valid = VecInit(Seq.fill(nPorts)(Reg(Bool())))
  for (i <- 0 until nPorts) {
    r_inputBuffer(i)       := io.in(i).bits
    r_inputBuffer_valid(i) := io.in(i).valid
  }

  // output buffer
  val r_outputBuffer       = VecInit(Seq.fill(nPorts)(Reg(new Packet(idWidth, dataWidth, cpWidth))))
  val r_outputBuffer_valid = VecInit(Seq.fill(nPorts)(Reg(Bool())))
  for (i <- 0 until nPorts) {
    io.out(i).bits  := r_outputBuffer(i)
    io.out(i).valid := r_outputBuffer_valid(i)
  }

  // default: no write
  for (i <- 0 until nPorts) {
    r_outputBuffer(i).srcID := 0.U
    r_outputBuffer(i).dstID := 0.U
    r_outputBuffer(i).data  := 0.U
    r_outputBuffer(i).srcCP := 0.U
    r_outputBuffer(i).dstCP := 0.U

    r_outputBuffer_valid(i) := false.B
  }

  // arbiter
  for (i <- 0 until nPorts) {
    when(r_inputBuffer_valid(i)) {
      r_outputBuffer_valid(r_inputBuffer(i).dstID) := true.B
      r_outputBuffer(r_inputBuffer(i).dstID)       := r_inputBuffer(i)
    }
  }
}