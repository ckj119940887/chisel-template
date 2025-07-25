package GeneralRegFileToBRAM 

import chisel3._
import chisel3.util._
import chisel3.experimental._
import chisel3.util.experimental.loadMemoryFromFile

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

    // registers for valid and ready
    // r_control(0) -- valid
    // r_control(1) -- ready 
    val r_control = Reg(Vec(2, UInt(8.W)))

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
