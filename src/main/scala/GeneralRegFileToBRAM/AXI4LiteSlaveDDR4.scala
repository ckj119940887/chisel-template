package GeneralRegFileToBRAM 

import chisel3._
import chisel3.util._
import chisel3.experimental._
import chisel3.util.experimental.loadMemoryFromFile

class AXI4LiteSlaveDDR4(val C_S_AXI_ADDR_WIDTH: Int, 
                        val C_S_AXI_DATA_WIDTH: Int,
                        val C_M_AXI_ADDR_WIDTH: Int,
                        val C_M_AXI_DATA_WIDTH: Int) extends Module {
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

    // master write address channel
    val M_AXI_AWID    = Output(UInt(1.W))
    val M_AXI_AWADDR  = Output(UInt(C_M_AXI_ADDR_WIDTH.W))
    val M_AXI_AWLEN   = Output(UInt(8.W))
    val M_AXI_AWSIZE  = Output(UInt(3.W))
    val M_AXI_AWBURST = Output(UInt(2.W))
    val M_AXI_AWLOCK  = Output(Bool())
    val M_AXI_AWCACHE = Output(UInt(4.W))
    val M_AXI_AWPROT  = Output(UInt(3.W))
    val M_AXI_AWQOS   = Output(UInt(4.W))
    val M_AXI_AWUSER  = Output(UInt(1.W))
    val M_AXI_AWVALID = Output(Bool())
    val M_AXI_AWREADY = Input(Bool())
    
    // master write data channel
    val M_AXI_WDATA  = Output(UInt(C_M_AXI_DATA_WIDTH.W))
    val M_AXI_WSTRB  = Output(UInt((C_M_AXI_DATA_WIDTH/8).W))
    val M_AXI_WLAST  = Output(Bool()) 
    val M_AXI_WUSER  = Output(UInt(1.W))
    val M_AXI_WVALID = Output(Bool())
    val M_AXI_WREADY = Input(Bool())
    
    // master write response channel
    val M_AXI_BID    = Input(UInt(1.W))
    val M_AXI_BRESP  = Input(UInt(2.W))
    val M_AXI_BUSER  = Input(UInt(1.W))
    val M_AXI_BVALID = Input(Bool())
    val M_AXI_BREADY = Output(Bool())
    
    // master read address channel
    val M_AXI_ARID    = Output(UInt(1.W))
    val M_AXI_ARADDR  = Output(UInt(C_M_AXI_ADDR_WIDTH.W))
    val M_AXI_ARLEN   = Output(UInt(8.W))
    val M_AXI_ARSIZE  = Output(UInt(3.W))
    val M_AXI_ARBURST = Output(UInt(2.W))
    val M_AXI_ARLOCK  = Output(Bool())
    val M_AXI_ARCACHE = Output(UInt(4.W))
    val M_AXI_ARPROT  = Output(UInt(3.W))
    val M_AXI_ARQOS   = Output(UInt(4.W))
    val M_AXI_ARUSER  = Output(UInt(1.W))
    val M_AXI_ARVALID = Output(Bool())
    val M_AXI_ARREADY = Input(Bool())
    
    // master read data channel
    val M_AXI_RID    = Input(UInt(1.W))      
    val M_AXI_RDATA  = Input(UInt(C_M_AXI_DATA_WIDTH.W))
    val M_AXI_RRESP  = Input(UInt(2.W))
    val M_AXI_RLAST  = Input(Bool())
    val M_AXI_RUSER  = Input(UInt(1.W))
    val M_AXI_RVALID = Input(Bool())
    val M_AXI_RREADY = Output(Bool())
  })

  val ADDR_LSB: Int = (C_S_AXI_DATA_WIDTH / 32) + 1
  val lowActiveReset = !reset.asBool

  withReset(lowActiveReset) {
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

    // r_control(0)  -- mode 
    // r_control(1)  -- readAddr    
    // r_control(2)  -- readOffset  
    // r_control(3)  -- readLen     
    // r_control(4)  -- readData    
    // r_control(5)  -- readValid   
    // r_control(6)  -- writeAddr  
    // r_control(7)  -- writeOffset 
    // r_control(8)  -- writeLen    
    // r_control(9)  -- writeData   
    // r_control(10) -- writeValid  
    // r_control(11) -- dmaSrcAddr   
    // r_control(12) -- dmaDstAddr   
    // r_control(13) -- dmaDstOffset 
    // r_control(14) -- dmaSrcLen    
    // r_control(15) -- dmaDstLen    
    // r_control(16) -- dmaValid     
    val r_control = Reg(Vec(18, UInt(64.W)))

    val axi4FullMaster = Module(new AXI4FullMaster(C_M_AXI_ADDR_WIDTH = 49, 
                                                   C_M_AXI_DATA_WIDTH = 64,
                                                   C_M_TARGET_SLAVE_BASE_ADDR = BigInt("00000000", 16),
                                                   MEMORY_DEPTH = 1024))

    axi4FullMaster.io.mode         := 0.U
    val r_read_enable   = RegNext(r_control(0) === 1.U)
    val r_write_enable  = RegNext(r_control(0) === 2.U)
    val r_dma_enable    = RegNext(r_control(0) === 3.U)
    when(r_control(0) =/= 0.U) {
        axi4FullMaster.io.mode     := r_control(0)
    }

    when(axi4FullMaster.io.readValid | axi4FullMaster.io.writeValid | axi4FullMaster.io.dmaValid) {
        r_control(0) := 0.U
    }

    axi4FullMaster.io.readAddr     := r_control(1) 
    axi4FullMaster.io.readOffset   := r_control(2) 
    axi4FullMaster.io.readLen      := r_control(3) 
    when(axi4FullMaster.io.readValid) {
        r_control(4)               := axi4FullMaster.io.readData
        r_control(5)               := true.B
    }

    axi4FullMaster.io.writeAddr    := r_control(6) 
    axi4FullMaster.io.writeOffset  := r_control(7) 
    axi4FullMaster.io.writeLen     := r_control(8) 
    axi4FullMaster.io.writeData    := r_control(9) 
    when(axi4FullMaster.io.writeValid) {
        r_control(10)              := true.B 
    }

    axi4FullMaster.io.dmaSrcAddr   := r_control(11) 
    axi4FullMaster.io.dmaDstAddr   := r_control(12) 
    axi4FullMaster.io.dmaDstOffset := r_control(13) 
    axi4FullMaster.io.dmaSrcLen    := r_control(14) 
    axi4FullMaster.io.dmaDstLen    := r_control(15) 
    when(axi4FullMaster.io.dmaValid) {
        r_control(16)              := true.B      
    }

    io.M_AXI_AWID    := axi4FullMaster.io.M_AXI_AWID    
    io.M_AXI_AWADDR  := axi4FullMaster.io.M_AXI_AWADDR  
    io.M_AXI_AWLEN   := axi4FullMaster.io.M_AXI_AWLEN   
    io.M_AXI_AWSIZE  := axi4FullMaster.io.M_AXI_AWSIZE  
    io.M_AXI_AWBURST := axi4FullMaster.io.M_AXI_AWBURST 
    io.M_AXI_AWLOCK  := axi4FullMaster.io.M_AXI_AWLOCK  
    io.M_AXI_AWCACHE := axi4FullMaster.io.M_AXI_AWCACHE 
    io.M_AXI_AWPROT  := axi4FullMaster.io.M_AXI_AWPROT  
    io.M_AXI_AWQOS   := axi4FullMaster.io.M_AXI_AWQOS   
    io.M_AXI_AWUSER  := axi4FullMaster.io.M_AXI_AWUSER  
    io.M_AXI_AWVALID := axi4FullMaster.io.M_AXI_AWVALID 
    axi4FullMaster.io.M_AXI_AWREADY := io.M_AXI_AWREADY

    io.M_AXI_WDATA   := axi4FullMaster.io.M_AXI_WDATA  
    io.M_AXI_WSTRB   := axi4FullMaster.io.M_AXI_WSTRB  
    io.M_AXI_WLAST   := axi4FullMaster.io.M_AXI_WLAST  
    io.M_AXI_WUSER   := axi4FullMaster.io.M_AXI_WUSER  
    io.M_AXI_WVALID  := axi4FullMaster.io.M_AXI_WVALID 
    axi4FullMaster.io.M_AXI_WREADY := io.M_AXI_WREADY

    axi4FullMaster.io.M_AXI_BID    := io.M_AXI_BID   
    axi4FullMaster.io.M_AXI_BRESP  := io.M_AXI_BRESP 
    axi4FullMaster.io.M_AXI_BUSER  := io.M_AXI_BUSER 
    axi4FullMaster.io.M_AXI_BVALID := io.M_AXI_BVALID
    io.M_AXI_BREADY := axi4FullMaster.io.M_AXI_BREADY

    io.M_AXI_ARID    := axi4FullMaster.io.M_AXI_ARID    
    io.M_AXI_ARADDR  := axi4FullMaster.io.M_AXI_ARADDR  
    io.M_AXI_ARLEN   := axi4FullMaster.io.M_AXI_ARLEN   
    io.M_AXI_ARSIZE  := axi4FullMaster.io.M_AXI_ARSIZE  
    io.M_AXI_ARBURST := axi4FullMaster.io.M_AXI_ARBURST 
    io.M_AXI_ARLOCK  := axi4FullMaster.io.M_AXI_ARLOCK  
    io.M_AXI_ARCACHE := axi4FullMaster.io.M_AXI_ARCACHE 
    io.M_AXI_ARPROT  := axi4FullMaster.io.M_AXI_ARPROT  
    io.M_AXI_ARQOS   := axi4FullMaster.io.M_AXI_ARQOS   
    io.M_AXI_ARUSER  := axi4FullMaster.io.M_AXI_ARUSER  
    io.M_AXI_ARVALID := axi4FullMaster.io.M_AXI_ARVALID 
    axi4FullMaster.io.M_AXI_ARREADY := io.M_AXI_ARREADY

    axi4FullMaster.io.M_AXI_RID    := io.M_AXI_RID    
    axi4FullMaster.io.M_AXI_RDATA  := io.M_AXI_RDATA  
    axi4FullMaster.io.M_AXI_RRESP  := io.M_AXI_RRESP  
    axi4FullMaster.io.M_AXI_RLAST  := io.M_AXI_RLAST  
    axi4FullMaster.io.M_AXI_RUSER  := io.M_AXI_RUSER  
    axi4FullMaster.io.M_AXI_RVALID := io.M_AXI_RVALID 
    io.M_AXI_RREADY := axi4FullMaster.io.M_AXI_RREADY     

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

  }
}
