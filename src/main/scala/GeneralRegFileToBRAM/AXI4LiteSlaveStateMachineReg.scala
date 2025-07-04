package GeneralRegFileToBRAM 

import chisel3._
import chisel3.util._
import chisel3.experimental._
import chisel3.util.experimental.loadMemoryFromFile

class AXI4LiteSlaveStateMachineReg(val C_S_AXI_ADDR_WIDTH: Int, val C_S_AXI_DATA_WIDTH: Int, val bramDepth: Int) extends Module {
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
  })

  // registers for diff channels
  // write address channel
  val r_s_axi_awaddr  = Reg(UInt(C_S_AXI_ADDR_WIDTH.W))

  // write data channel
  val r_s_axi_wready  = RegInit(false.B)
  val r_s_axi_wdata   = Reg(UInt(C_S_AXI_DATA_WIDTH.W))

  // write response channel
  val r_s_axi_bvalid  = RegInit(false.B)

  // read address channel
  val r_s_axi_araddr  = Reg(UInt(C_S_AXI_ADDR_WIDTH.W)) 

  // read data channel
  val r_s_axi_rdata   = Reg(UInt(C_S_AXI_DATA_WIDTH.W)) 
  val r_s_axi_rvalid  = RegInit(false.B)

  // other signals
  val r_writeLen      = Reg(UInt((C_S_AXI_DATA_WIDTH / 8).W))

  // registers for valid and ready
  val r_valid = RegInit(false.B)
  val r_ready = RegInit(0.U(2.W))

  // bram related logic
  val bramIp = Module(new BRAMIPWrapper(bramDepth = bramDepth, bramWidth = 8, portWidth = 32))

  // BRAM default
  bramIp.io.mode         := 0.U
  bramIp.io.readAddr     := 0.U
  bramIp.io.readOffset   := 0.U
  bramIp.io.readLen      := 0.U
  bramIp.io.writeAddr    := 0.U
  bramIp.io.writeOffset  := 0.U
  bramIp.io.writeLen     := 0.U
  bramIp.io.writeData    := 0.U
  bramIp.io.dmaSrcAddr   := 0.U 
  bramIp.io.dmaDstAddr   := 0.U 
  bramIp.io.dmaDstOffset := 0.U 
  bramIp.io.dmaSrcLen    := 0.U 
  bramIp.io.dmaDstLen    := 0.U 

  // write logic
  val sWIdle :: sWActive :: sWBranch :: sWValid :: sWBram :: sWCheck :: sWEnd :: Nil = Enum(7)
  val writeState = RegInit(sWIdle)

  r_s_axi_awaddr      := Mux(io.S_AXI_AWVALID & io.S_AXI_AWREADY, io.S_AXI_AWADDR, r_s_axi_awaddr)
  val r_writeRegValid = RegNext(r_s_axi_awaddr === bramDepth.U)

  r_s_axi_wdata    := Mux(io.S_AXI_WVALID & io.S_AXI_WREADY, io.S_AXI_WDATA, r_s_axi_wdata)
  r_writeLen       := Mux(io.S_AXI_WVALID & io.S_AXI_WREADY, PopCount(io.S_AXI_WSTRB), r_writeLen)

  switch(writeState) {
    is(sWIdle) {
      writeState     := Mux(io.S_AXI_AWVALID & io.S_AXI_AWREADY, sWActive, writeState)
    }
    is(sWActive) {
      r_s_axi_wready := true.B
      writeState     := Mux(io.S_AXI_WVALID & io.S_AXI_WREADY, sWBranch, writeState)
    }
    is(sWBranch) {
      r_s_axi_wready := false.B
      writeState     := Mux(r_writeRegValid, sWValid, sWBram)
    }
    is(sWValid) {
      r_valid     := r_s_axi_wdata(0).asBool
      writeState  := sWCheck
    }
    is(sWBram) {
      bramIp.io.mode          := 2.U
      bramIp.io.writeAddr     := r_s_axi_awaddr
      bramIp.io.writeOffset   := 0.U
      bramIp.io.writeLen      := r_writeLen
      bramIp.io.writeData     := r_s_axi_wdata

      writeState  := Mux(bramIp.io.writeValid, sWCheck, writeState)
    }
    is(sWCheck) {
      r_s_axi_bvalid := true.B
      writeState     := Mux(io.S_AXI_BREADY & io.S_AXI_BVALID, sWEnd, writeState)
    }
    is(sWEnd) {
      r_s_axi_bvalid := false.B
      writeState     := sWIdle
    }
  }

  // read logic
  r_s_axi_araddr     := Mux(io.S_AXI_ARVALID & io.S_AXI_ARREADY, io.S_AXI_ARADDR, r_s_axi_araddr)

  // read data channel
  val sRIdle :: sARActive :: sRReady :: sRBram :: sRCheck :: sREnd :: Nil = Enum(6)
  val readState = RegInit(sRIdle)

  switch(readState) {
    is(sRIdle) {
      readState := Mux(io.S_AXI_ARVALID & io.S_AXI_ARREADY, sARActive, readState)
    }
    is(sARActive) {
      readState := Mux(r_s_axi_araddr === bramDepth.U, sRReady, sRBram)
    }
    is(sRReady) {
      r_s_axi_rdata  := r_ready
      readState      := sRCheck
    }
    is(sRBram) {
      bramIp.io.mode       := 1.U
      bramIp.io.readAddr   := r_s_axi_araddr
      bramIp.io.readOffset := 0.U
      bramIp.io.readLen    := (C_S_AXI_DATA_WIDTH/8).U

      readState            := Mux(bramIp.io.readValid, sRCheck, readState)
      r_s_axi_rdata        := bramIp.io.readData
    }
    is(sRCheck) {
      r_s_axi_rvalid := true.B
      readState := Mux(io.S_AXI_RVALID & io.S_AXI_RREADY, sREnd, readState)
    }
    is(sREnd) {
      r_s_axi_rvalid := false.B
      readState := sRIdle
    }
  }

  // write address channel
  io.S_AXI_AWREADY := ~r_s_axi_bvalid & io.S_AXI_AWVALID
  
  // write channel
  io.S_AXI_WREADY  := r_s_axi_wready

  // write response channel
  io.S_AXI_BRESP   := 0.U
  io.S_AXI_BVALID  := r_s_axi_bvalid
  
  // read address channel
  io.S_AXI_ARREADY := ~r_s_axi_rvalid & io.S_AXI_ARVALID

  // read channel
  io.S_AXI_RDATA   := r_s_axi_rdata
  io.S_AXI_RRESP   := 0.U
  io.S_AXI_RVALID  := r_s_axi_rvalid

  // computing logic
  val sCompIdle :: sComp1 :: sComp2 :: sComp3 :: sCompEnd :: Nil = Enum(5)
  val compState = RegInit(sCompIdle)

  r_ready := Mux(compState === sCompEnd, 1.U, 0.U)
  switch(compState) {
    is(sCompIdle) {
      compState := Mux(r_valid, sComp1, sCompIdle)
    }
    is(sComp1) {
      compState := sComp2
    }
    is(sComp2) {
      compState := sComp3
    }
    is(sComp3) {
      compState := sCompEnd
    }
  }
}
