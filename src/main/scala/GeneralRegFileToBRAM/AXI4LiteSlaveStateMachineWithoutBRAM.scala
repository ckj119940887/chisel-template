package GeneralRegFileToBRAM 

import chisel3._
import chisel3.util._
import chisel3.experimental._
import chisel3.util.experimental.loadMemoryFromFile

class SyncBuffer(val width: Int = 64) extends Module {
  val io = IO(new Bundle {
    val clkSrc = Input(Clock()) 
    val clkDst = Input(Clock()) 
    val d      = Input(UInt(width.W))  
    val q      = Output(UInt(width.W)) 
  })

  val srcReg = withClock(io.clkSrc) {
    RegNext(io.d)
  }

  withClock(io.clkDst) {
    val sync1 = RegNext(srcReg)
    val sync2 = RegNext(sync1)
    io.q := sync2
  }
}

class TestAdder(val width: Int = 64) extends Module {
    val io = IO(new Bundle{
        val a = Input(UInt(width.W))
        val b = Input(UInt(width.W))
        val start = Input(Bool())
        val out = Output(UInt(width.W))
        val valid = Output(Bool())
    })

    val state  = RegInit(0.U(2.W))
    val regA   = RegInit(0.U(width.W))
    val regB   = RegInit(0.U(width.W))
    val result = RegInit(0.U(width.W))

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

class AXI4LiteSlaveStateMachineWithoutBRAM(val C_S_AXI_ADDR_WIDTH: Int, val C_S_AXI_DATA_WIDTH: Int) extends Module {
  val io = IO(new Bundle{
    // write address channel
    val S_AXI_AWADDR  = Input(UInt(C_S_AXI_ADDR_WIDTH.W))
    val S_AXI_AWPROT  = Input(UInt(3.W))
    val S_AXI_AWVALID = Input(Bool())
    val S_AXI_AWREADY = Output(Bool())
    
    // write data channel
    val S_AXI_WDATA  = Input(UInt(C_S_AXI_DATA_WIDTH.W))
    val S_AXI_WSTRB  = Input(UInt((C_S_AXI_DATA_WIDTH/8).W))
    val S_AXI_WVALID = Input(Bool())
    val S_AXI_WREADY = Output(Bool())
    
    // write response channel
    val S_AXI_BRESP  = Output(UInt(2.W))
    val S_AXI_BVALID = Output(Bool())
    val S_AXI_BREADY = Input(Bool())
    
    // read address channel
    val S_AXI_ARADDR  = Input(UInt(C_S_AXI_ADDR_WIDTH.W))
    val S_AXI_ARPROT  = Input(UInt(3.W))
    val S_AXI_ARVALID = Input(Bool())
    val S_AXI_ARREADY = Output(Bool())
    
    // read data channel
    val S_AXI_RDATA  = Output(UInt(C_S_AXI_DATA_WIDTH.W))
    val S_AXI_RRESP  = Output(UInt(2.W))
    val S_AXI_RVALID = Output(Bool())
    val S_AXI_RREADY = Input(Bool())

    // extra clock
    val extra_clock  = Input(Clock())
  })

  val ADDR_LSB: Int = (C_S_AXI_DATA_WIDTH / 32) + 1

  // registers for diff channels
  // write address channel
  val r_s_axi_awready = RegInit(true.B)
  val r_s_axi_awaddr  = Reg(UInt(C_S_AXI_ADDR_WIDTH.W))

  // write data channel
  val r_s_axi_wready  = RegInit(true.B)
  val r_s_axi_wdata   = Reg(UInt(C_S_AXI_DATA_WIDTH.W))

  // write response channel
  val r_s_axi_bvalid  = RegInit(false.B)

  // read address channel
  val r_s_axi_arready = RegInit(true.B)
  val r_s_axi_araddr  = Reg(UInt(C_S_AXI_ADDR_WIDTH.W)) 

  // read data channel
  val r_s_axi_rvalid  = RegInit(false.B)
  val r_s_axi_rdata   = Reg(UInt(C_S_AXI_DATA_WIDTH.W))

  // registers for valid and ready
  // r_control(0) -- in1 
  // r_control(1) -- in2 
  // r_control(2) -- out
  // r_control(3) -- valid
  // r_control(4) -- ready
  val r_control = Reg(Vec(5, UInt(C_S_AXI_DATA_WIDTH.W)))

  // write logic
  val r_aw_valid = RegInit(false.B)
  val r_w_valid  = RegInit(false.B)
  when(io.S_AXI_AWVALID & io.S_AXI_AWREADY) {
    r_s_axi_awready           := false.B
    r_s_axi_awaddr            := io.S_AXI_AWADDR(C_S_AXI_ADDR_WIDTH - 1, ADDR_LSB)
    r_aw_valid                := true.B
  }

  when(io.S_AXI_WVALID & io.S_AXI_WREADY) {
    r_s_axi_wready            := false.B
    r_s_axi_wdata             := io.S_AXI_WDATA
    r_w_valid                 := true.B
  }

  when(r_aw_valid & r_w_valid) {
    r_s_axi_bvalid            := true.B
    r_control(r_s_axi_awaddr) := r_s_axi_wdata

    r_aw_valid                := false.B
    r_w_valid                 := false.B
  }

  when(io.S_AXI_BVALID & io.S_AXI_BREADY) {
    r_s_axi_bvalid            := false.B
    r_s_axi_awready           := true.B
    r_s_axi_wready            := true.B
  }

  // read logic
  val r_ar_valid = RegInit(false.B)

  when(io.S_AXI_ARVALID & io.S_AXI_ARREADY) {
    r_s_axi_arready           := false.B
    r_s_axi_araddr            := io.S_AXI_ARADDR(C_S_AXI_ADDR_WIDTH - 1, ADDR_LSB)
    r_ar_valid                := true.B
  }

  when(r_ar_valid) {
    r_s_axi_rvalid            := true.B
    r_s_axi_rdata             := r_control(r_s_axi_araddr)
    r_ar_valid                := false.B
  }

  when(io.S_AXI_RVALID & io.S_AXI_RREADY) {
    r_s_axi_rvalid            := false.B
    r_s_axi_arready           := true.B
  }

  // write address channel
  io.S_AXI_AWREADY := r_s_axi_awready
  
  // write channel
  io.S_AXI_WREADY  := r_s_axi_wready

  // write response channel
  io.S_AXI_BRESP   := 0.U
  io.S_AXI_BVALID  := r_s_axi_bvalid
  
  // read address channel
  io.S_AXI_ARREADY := r_s_axi_arready

  // read channel
  io.S_AXI_RDATA   := r_s_axi_rdata
  io.S_AXI_RRESP   := 0.U
  io.S_AXI_RVALID  := r_s_axi_rvalid

  val adder = withClockAndReset(io.extra_clock, reset) {
    Module(new TestAdder(64))
  }

  val in1Buffer   = Module(new SyncBuffer(64))
  val in2Buffer   = Module(new SyncBuffer(64))
  val validBuffer = Module(new SyncBuffer(1))
  val outBuffer   = Module(new SyncBuffer(64))
  val readyBuffer = Module(new SyncBuffer(1))

  in1Buffer.io.clkSrc := clock
  in1Buffer.io.clkDst := io.extra_clock
  in1Buffer.io.d      := r_control(0)
  adder.io.a          := in1Buffer.io.q

  in2Buffer.io.clkSrc := clock
  in2Buffer.io.clkDst := io.extra_clock
  in2Buffer.io.d      := r_control(1)
  adder.io.b          := in2Buffer.io.q

  validBuffer.io.clkSrc := clock
  validBuffer.io.clkDst := io.extra_clock
  validBuffer.io.d      := r_control(3)(0)
  adder.io.start        := validBuffer.io.q.asBool

  outBuffer.io.clkSrc := io.extra_clock
  outBuffer.io.clkDst := clock
  outBuffer.io.d      := adder.io.out
  r_control(2)        := outBuffer.io.q

  readyBuffer.io.clkSrc := io.extra_clock
  readyBuffer.io.clkDst := clock
  readyBuffer.io.d      := adder.io.valid.asUInt
  r_control(4)          := readyBuffer.io.q
}