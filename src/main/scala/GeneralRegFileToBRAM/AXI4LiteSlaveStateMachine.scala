package GeneralRegFileToBRAM 

import chisel3._
import chisel3.util._
import chisel3.experimental._
import chisel3.util.experimental.loadMemoryFromFile

class AXI4LiteSlaveStateMachine(val C_S_AXI_ADDR_WIDTH: Int, val C_S_AXI_DATA_WIDTH: Int, val bramDepth: Int) extends Module {
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
  val r_s_axi_awready = RegInit(false.B) 
  val r_s_axi_wready  = RegInit(false.B)
  val r_s_axi_bvalid  = RegInit(false.B)
  val r_s_axi_arready = Reg(Bool()) 
  val r_s_axi_rdata   = Reg(UInt(C_S_AXI_DATA_WIDTH.W)) 
  val r_s_axi_rvalid  = Reg(Bool()) 

  val r_writeAddr     = Reg(UInt(C_S_AXI_ADDR_WIDTH.W))
  val r_writeData     = Reg(UInt(C_S_AXI_DATA_WIDTH.W))
  val r_writeLen      = Reg(UInt((C_S_AXI_DATA_WIDTH / 8).W))

  val r_readAddr      = Reg(UInt(C_S_AXI_ADDR_WIDTH.W))
  val r_readData      = Reg(UInt(C_S_AXI_DATA_WIDTH.W))
  val r_readLen       = Reg(UInt((C_S_AXI_DATA_WIDTH / 8).W))

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

  val sWriteIdle :: sAWActive :: sWActive :: sBActive:: Nil = Enum(4)
  val writeState = RegInit(sWriteIdle)

  r_s_axi_awready := Mux(io.S_AXI_AWVALID, true.B ,false.B)
  r_s_axi_wready  := Mux((writeState === sAWActive) & io.S_AXI_WVALID,  true.B, false.B)
  r_s_axi_bvalid  := Mux((writeState === sWActive) & bramIp.io.writeValid, true.B, false.B) |
                     Mux(io.S_AXI_WVALID & io.S_AXI_WREADY & (r_writeAddr === bramDepth.U), true.B, false.B)
  switch(writeState) {
    is(sWriteIdle) {
      writeState  := Mux(io.S_AXI_AWVALID & io.S_AXI_AWREADY, sAWActive, sWriteIdle)
      r_writeAddr := Mux(io.S_AXI_AWVALID & io.S_AXI_AWREADY, io.S_AXI_AWADDR, r_writeAddr)
    }
    is(sAWActive) {
      writeState  := Mux(io.S_AXI_WVALID & io.S_AXI_WREADY, sWActive, sAWActive)
      r_writeLen  := Mux(io.S_AXI_WVALID & io.S_AXI_WREADY, PopCount(io.S_AXI_WSTRB), r_writeLen)
      r_writeData := Mux(io.S_AXI_WVALID & io.S_AXI_WREADY, io.S_AXI_WDATA, r_writeData)
    }
    is(sWActive) {
      when(r_writeAddr === bramDepth.U) {
        writeState              := sBActive
        r_valid                 := r_writeData(0).asBool
      } .otherwise {
        bramIp.io.mode          := 2.U
        bramIp.io.writeAddr     := r_writeAddr
        bramIp.io.writeOffset   := 0.U
        bramIp.io.writeLen      := r_writeLen
        bramIp.io.writeData     := r_writeData
      }

      when((r_writeAddr =/= bramDepth.U) & bramIp.io.writeValid) {
        bramIp.io.mode          := 0.U
        writeState              := sBActive
      }
    }
    is(sBActive) {

      writeState := Mux(io.S_AXI_BVALID & io.S_AXI_BREADY, sWriteIdle, sBActive)
    }
  }

  val sReadIdle :: sARActive :: sRActive :: sReadEnd :: Nil = Enum(4)
  val readState = RegInit(sReadIdle)

  r_s_axi_arready := Mux(io.S_AXI_ARVALID, true.B, false.B)
  r_s_axi_rvalid  := Mux((readState === sRActive) & (bramIp.io.readValid | r_readAddr === bramDepth.U), true.B, false.B)
  switch(readState) {
    is(sReadIdle) {
      readState := Mux(io.S_AXI_ARVALID, sARActive, sReadIdle)
    }
    is(sARActive) {
      readState := Mux(io.S_AXI_ARVALID & io.S_AXI_ARREADY, sRActive, sARActive)

      when(io.S_AXI_ARVALID & io.S_AXI_ARREADY) {
        r_readAddr := io.S_AXI_ARADDR
      }
    }
    is(sRActive) {
      when(r_readAddr === bramDepth.U) {
        r_s_axi_rdata         := r_ready
        readState             := sReadEnd
      } .otherwise {
        bramIp.io.mode        := 1.U
        bramIp.io.readAddr    := r_readAddr
        bramIp.io.readOffset  := 0.U
        bramIp.io.readLen     := (C_S_AXI_DATA_WIDTH/8).U
      }

      when((r_readAddr =/= bramDepth.U) & bramIp.io.readValid) {
        bramIp.io.mode        := 0.U

        r_s_axi_rdata         := bramIp.io.readData
        readState             := sReadEnd
      }
    }
    is(sReadEnd) {
      readState := Mux(io.S_AXI_RVALID & io.S_AXI_RREADY, sReadIdle, sReadEnd)
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

class AXIWrapperChiselGeneratedAddTest (
               val C_S_AXI_DATA_WIDTH:  Int = 32,
               val C_S_AXI_ADDR_WIDTH:  Int = 10,
               val bramDepth: Int = 1024) extends Module {

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

  withReset(!reset.asBool) {
    val mod = Module(new AXI4LiteSlaveStateMachine(
      C_S_AXI_DATA_WIDTH  = C_S_AXI_DATA_WIDTH,
      C_S_AXI_ADDR_WIDTH  = C_S_AXI_ADDR_WIDTH,
      bramDepth           = bramDepth
    ))

    mod.io.S_AXI_AWADDR  := io.S_AXI_AWADDR
    mod.io.S_AXI_AWPROT  := io.S_AXI_AWPROT
    mod.io.S_AXI_AWVALID := io.S_AXI_AWVALID
    io.S_AXI_AWREADY     := mod.io.S_AXI_AWREADY

    mod.io.S_AXI_WDATA   := io.S_AXI_WDATA
    mod.io.S_AXI_WSTRB   := io.S_AXI_WSTRB
    mod.io.S_AXI_WVALID  := io.S_AXI_WVALID
    io.S_AXI_WREADY      := mod.io.S_AXI_WREADY

    io.S_AXI_BRESP       := mod.io.S_AXI_BRESP
    io.S_AXI_BVALID      := mod.io.S_AXI_BVALID
    mod.io.S_AXI_BREADY  := io.S_AXI_BREADY

    mod.io.S_AXI_ARADDR  := io.S_AXI_ARADDR
    mod.io.S_AXI_ARPROT  := io.S_AXI_ARPROT
    mod.io.S_AXI_ARVALID := io.S_AXI_ARVALID
    io.S_AXI_ARREADY     := mod.io.S_AXI_ARREADY

    io.S_AXI_RDATA       := mod.io.S_AXI_RDATA
    io.S_AXI_RRESP       := mod.io.S_AXI_RRESP
    io.S_AXI_RVALID      := mod.io.S_AXI_RVALID
    mod.io.S_AXI_RREADY  := io.S_AXI_RREADY
  }
}
