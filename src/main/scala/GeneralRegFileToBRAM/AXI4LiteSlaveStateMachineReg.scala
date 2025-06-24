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
  val r_s_axi_awvalid = Reg(Bool())
  val r_s_axi_awready = RegInit(false.B) 

  // write data channel
  val r_s_axi_wdata   = Reg(UInt(C_S_AXI_DATA_WIDTH.W))
  val r_s_axi_wstrb   = Reg(UInt((C_S_AXI_DATA_WIDTH/8).W))
  val r_s_axi_wvalid  = Reg(Bool())
  val r_s_axi_wready  = RegInit(false.B)

  // write response channel
  val r_s_axi_bready  = Reg(Bool())
  val r_s_axi_bvalid  = RegInit(false.B)

  // read address channel
  val r_s_axi_araddr  = Reg(UInt(C_S_AXI_ADDR_WIDTH.W)) 
  val r_s_axi_arvalid = Reg(Bool()) 
  val r_s_axi_arready = Reg(Bool()) 

  // read data channel
  val r_s_axi_rdata   = Reg(UInt(C_S_AXI_DATA_WIDTH.W)) 
  val r_s_axi_rready  = Reg(Bool()) 
  val r_s_axi_rvalid  = Reg(Bool()) 

  r_s_axi_awaddr  := io.S_AXI_AWADDR
  r_s_axi_awvalid := io.S_AXI_AWVALID

  r_s_axi_wdata   := io.S_AXI_WDATA
  r_s_axi_wstrb   := io.S_AXI_WSTRB
  r_s_axi_wvalid  := io.S_AXI_WVALID

  r_s_axi_bready  := io.S_AXI_BREADY

  r_s_axi_araddr  := io.S_AXI_ARADDR
  r_s_axi_arvalid := io.S_AXI_ARVALID

  r_s_axi_rready  := io.S_AXI_RREADY

  // other signals
  val r_writeLen      = Reg(UInt((C_S_AXI_DATA_WIDTH / 8).W))
  r_writeLen      := PopCount(io.S_AXI_WSTRB)

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

  val sWriteIdle :: sAWActive :: sWActive1 :: sWActive2 :: sBActive:: Nil = Enum(5)
  val writeState   = RegInit(sWriteIdle)
  val wActiveState = RegInit(sWriteIdle)

  wActiveState    := Mux(io.S_AXI_AWADDR === bramDepth.U, sWActive1, sWActive2)
  r_s_axi_awready := io.S_AXI_AWVALID
  r_s_axi_wready  := false.B
  r_s_axi_bvalid  := false.B
  switch(writeState) {
    is(sWriteIdle) {
      writeState  := Mux(r_s_axi_awvalid & r_s_axi_awready, sAWActive, sWriteIdle)
    }
    is(sAWActive) {
      r_s_axi_wready := io.S_AXI_WVALID
      when(r_s_axi_wready & r_s_axi_wvalid) {
        writeState := wActiveState
      }
    }
    is(sWActive1) {
      writeState              := sBActive
      r_valid                 := r_s_axi_wdata(0).asBool
      r_s_axi_bvalid          := true.B
    }
    is(sWActive2) {
      bramIp.io.mode          := 2.U
      bramIp.io.writeAddr     := r_s_axi_awaddr
      bramIp.io.writeOffset   := 0.U
      bramIp.io.writeLen      := r_writeLen
      bramIp.io.writeData     := r_s_axi_wdata

      when(bramIp.io.writeValid) {
        bramIp.io.mode          := 0.U
        writeState              := sBActive
        r_s_axi_bvalid          := true.B
      }
    }
    is(sBActive) {
      writeState := Mux(r_s_axi_bvalid & r_s_axi_bready, sWriteIdle, sBActive)
    }
  }

  val sReadIdle :: sRActive1 :: sRActive2 :: sReadEnd :: Nil = Enum(4)
  val readState    = RegInit(sReadIdle)
  val rActiveState = RegInit(sReadIdle)

  rActiveState    := Mux(io.S_AXI_ARADDR === bramDepth.U, sRActive1, sRActive2)
  r_s_axi_arready := io.S_AXI_ARVALID
  r_s_axi_rvalid  := false.B
  switch(readState) {
    is(sReadIdle) {
      when(r_s_axi_arready & r_s_axi_arvalid) {
        readState := rActiveState
      }
    }
    is(sRActive1) {
      r_s_axi_rdata         := r_ready
      readState             := sReadEnd
      r_s_axi_rvalid        := true.B
    }
    is(sRActive2) {
      bramIp.io.mode        := 1.U
      bramIp.io.readAddr    := r_s_axi_araddr
      bramIp.io.readOffset  := 0.U
      bramIp.io.readLen     := (C_S_AXI_DATA_WIDTH/8).U

      when(bramIp.io.readValid) {
        bramIp.io.mode        := 0.U

        r_s_axi_rdata         := bramIp.io.readData
        readState             := sReadEnd
        r_s_axi_rvalid        := true.B
      }
    }
    is(sReadEnd) {
      readState := Mux(r_s_axi_rvalid & r_s_axi_rready, sReadIdle, sReadEnd)
    }
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
