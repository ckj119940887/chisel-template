package GeneralRegFileToBRAM 

import chisel3._
import chisel3.util._
import chisel3.experimental._
import chisel3.util.experimental.loadMemoryFromFile

class AXI4LiteMasterStateMachineWithoutBRAM(val C_S_AXI_ADDR_WIDTH: Int, 
                                            val C_S_AXI_DATA_WIDTH: Int,
                                            val BASE_MEM_ADDR: Int) extends Module {
  val io = IO(new Bundle{
    // write address channel
    val M_AXI_AWADDR  = Output(UInt(C_S_AXI_ADDR_WIDTH.W))
    val M_AXI_AWPROT  = Output(UInt(3.W))
    val M_AXI_AWVALID = Output(Bool())
    val M_AXI_AWREADY = Input(Bool())
    
    // write data channel
    val M_AXI_WDATA  = Output(UInt(C_S_AXI_DATA_WIDTH.W))
    val M_AXI_WSTRB  = Output(UInt((C_S_AXI_DATA_WIDTH/8).W))
    val M_AXI_WVALID = Output(Bool())
    val M_AXI_WREADY = Input(Bool())
    
    // write response channel
    val M_AXI_BRESP  = Input(UInt(2.W))
    val M_AXI_BVALID = Input(Bool())
    val M_AXI_BREADY = Output(Bool())
    
    // read address channel
    val M_AXI_ARADDR  = Output(UInt(C_S_AXI_ADDR_WIDTH.W))
    val M_AXI_ARPROT  = Output(UInt(3.W))
    val M_AXI_ARVALID = Output(Bool())
    val M_AXI_ARREADY = Input(Bool())
    
    // read data channel
    val M_AXI_RDATA  = Input(UInt(C_S_AXI_DATA_WIDTH.W))
    val M_AXI_RRESP  = Input(UInt(2.W))
    val M_AXI_RVALID = Input(Bool())
    val M_AXI_RREADY = Output(Bool())

    val valid = Output(Bool())
  })

  val lowActiveReset = !reset.asBool

  withReset(lowActiveReset) {

    // registers for diff channels
    // write address channel
    val r_m_axi_awvalid = RegInit(false.B)
    val r_m_axi_awaddr  = RegInit(0.U(C_S_AXI_ADDR_WIDTH.W))

    // write data channel
    val r_m_axi_wvalid  = RegInit(false.B)
    val r_m_axi_wdata   = RegInit(1.U(C_S_AXI_DATA_WIDTH.W))
    val r_m_axi_wstrb   = Reg(UInt((C_S_AXI_DATA_WIDTH/8).W))

    // write response channel
    val r_m_axi_bready  = RegInit(false.B)

    // read address channel
    val r_m_axi_arvalid = RegInit(false.B)
    val r_m_axi_araddr  = RegInit(0.U(C_S_AXI_ADDR_WIDTH.W)) 

    // read data channel
    val r_m_axi_rready  = RegInit(false.B)
    val r_m_axi_rdata   = Reg(UInt(C_S_AXI_DATA_WIDTH.W))

    // registers for valid and ready
    // r_control(0) -- valid
    // r_control(1) -- ready 
    val r_control = Reg(Vec(2, UInt(32.W)))

    // write logic
    val sWIdle :: sAWActive :: sWActive :: sEnd :: Nil = Enum(4)
    val writeState = RegInit(sWIdle)

    val counter = RegInit(0.U(3.W))

    switch(writeState) {
      is(sWIdle) {
        r_m_axi_awvalid := Mux(counter < 2.U, true.B, false.B)
        writeState      := Mux(io.M_AXI_AWVALID & io.M_AXI_AWREADY & (counter < 2.U), sAWActive, sWIdle)
      }
      is(sAWActive) {
        r_m_axi_awvalid := false.B
        r_m_axi_wvalid  := true.B
        writeState      := Mux(io.M_AXI_WVALID & io.M_AXI_WREADY, sWActive, sAWActive)
      }
      is(sWActive) {
        r_m_axi_wvalid  := false.B
        r_m_axi_bready  := true.B
        writeState      := Mux(io.M_AXI_BVALID & io.M_AXI_BREADY, sEnd, sWActive)
      }
      is(sEnd) {
        r_m_axi_bready  := false.B
        r_m_axi_awaddr  := r_m_axi_awaddr + 4.U
        r_m_axi_wdata   := r_m_axi_wdata + 7.U
        counter         := counter + 1.U
        writeState      := sWIdle
      }
    }

    // read logic
    // read data channel
    val sRIdle :: sARActive :: sREnd :: Nil = Enum(3)
    val readState = RegInit(sRIdle)

    switch(readState) {
      is(sRIdle) {
        r_m_axi_arvalid := Mux(counter >= 2.U && counter < 4.U, true.B, false.B)
        readState       := Mux(io.M_AXI_ARVALID & io.M_AXI_ARREADY, sARActive, readState)
      }
      is(sARActive) {
        r_m_axi_arvalid := false.B
        r_m_axi_rready  := true.B
        readState       := Mux(io.M_AXI_RVALID & io.M_AXI_RREADY, sREnd, sARActive)
      }
      is(sREnd) {
        r_m_axi_rready             := false.B
        r_m_axi_araddr             := r_m_axi_araddr + 4.U
        r_control(r_m_axi_araddr)  := r_m_axi_rdata
        counter                    := counter + 1.U
        readState                  := sRIdle
      }
    }

    io.valid := counter === 4.U && r_control(1) === 8.U

    // write address channel
    io.M_AXI_AWADDR  := r_m_axi_awaddr
    io.M_AXI_AWVALID := r_m_axi_awvalid
    io.M_AXI_AWPROT  := 0.U
  
    // write channel
    io.M_AXI_WVALID  := r_m_axi_wvalid
    io.M_AXI_WDATA   := r_m_axi_wdata
    io.M_AXI_WSTRB   := r_m_axi_wstrb

    // write response channel
    io.M_AXI_BREADY  := r_m_axi_bready
  
    // read address channel
    io.M_AXI_ARADDR  := r_m_axi_araddr
    io.M_AXI_ARPROT  := 0.U
    io.M_AXI_ARVALID := r_m_axi_arvalid

    // read channel
    io.M_AXI_RREADY  := r_m_axi_rready
    r_m_axi_rdata    := io.M_AXI_RDATA

  }
}

class AXI4_Master_Slave extends Module {
  val io = IO(new Bundle{
    val valid = Output(Bool())
  })

  val axi4Master = Module(new AXI4LiteMasterStateMachineWithoutBRAM(32, 32, 0xA0000000))
  val axi4Slave  = Module(new AXI4LiteSlaveStateMachineWithoutBRAM(10, 32))

  axi4Slave.io.S_AXI_AWADDR   :=  axi4Master.io.M_AXI_AWADDR 
  axi4Slave.io.S_AXI_AWPROT   :=  axi4Master.io.M_AXI_AWPROT 
  axi4Slave.io.S_AXI_AWVALID  :=  axi4Master.io.M_AXI_AWVALID
  axi4Master.io.M_AXI_AWREADY :=  axi4Slave.io.S_AXI_AWREADY

  axi4Slave.io.S_AXI_WDATA    :=  axi4Master.io.M_AXI_WDATA  
  axi4Slave.io.S_AXI_WSTRB    :=  axi4Master.io.M_AXI_WSTRB  
  axi4Slave.io.S_AXI_WVALID   :=  axi4Master.io.M_AXI_WVALID 
  axi4Master.io.M_AXI_WREADY  :=  axi4Slave.io.S_AXI_WREADY 

  axi4Master.io.M_AXI_BRESP   :=  axi4Slave.io.S_AXI_BRESP  
  axi4Master.io.M_AXI_BVALID  :=  axi4Slave.io.S_AXI_BVALID 
  axi4Slave.io.S_AXI_BREADY   :=  axi4Master.io.M_AXI_BREADY 

  axi4Slave.io.S_AXI_ARADDR   :=  axi4Master.io.M_AXI_ARADDR 
  axi4Slave.io.S_AXI_ARPROT   :=  axi4Master.io.M_AXI_ARPROT 
  axi4Slave.io.S_AXI_ARVALID  :=  axi4Master.io.M_AXI_ARVALID
  axi4Master.io.M_AXI_ARREADY :=  axi4Slave.io.S_AXI_ARREADY

  axi4Master.io.M_AXI_RDATA   :=  axi4Slave.io.S_AXI_RDATA  
  axi4Master.io.M_AXI_RRESP   :=  axi4Slave.io.S_AXI_RRESP  
  axi4Master.io.M_AXI_RVALID  :=  axi4Slave.io.S_AXI_RVALID 
  axi4Slave.io.S_AXI_RREADY   :=  axi4Master.io.M_AXI_RREADY 
  
  io.valid := axi4Master.io.valid
}