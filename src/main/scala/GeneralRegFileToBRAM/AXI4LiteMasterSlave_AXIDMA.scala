package GeneralRegFileToBRAM 

import chisel3._
import chisel3.util._
import chisel3.experimental._
import chisel3.util.experimental.loadMemoryFromFile

class AXI4LiteMasterSlave_AXIDMA(val C_S_AXI_ADDR_WIDTH: Int, 
                                 val C_S_AXI_DATA_WIDTH: Int,
                                 val BASE_MEM_ADDR: Int) extends Module {
  val io = IO(new Bundle{
    // master write address channel
    val M_AXI_AWADDR  = Output(UInt(C_S_AXI_ADDR_WIDTH.W))
    val M_AXI_AWPROT  = Output(UInt(3.W))
    val M_AXI_AWVALID = Output(Bool())
    val M_AXI_AWREADY = Input(Bool())
    
    // master write data channel
    val M_AXI_WDATA  = Output(UInt(C_S_AXI_DATA_WIDTH.W))
    val M_AXI_WSTRB  = Output(UInt((C_S_AXI_DATA_WIDTH/8).W))
    val M_AXI_WVALID = Output(Bool())
    val M_AXI_WREADY = Input(Bool())
    
    // master write response channel
    val M_AXI_BRESP  = Input(UInt(2.W))
    val M_AXI_BVALID = Input(Bool())
    val M_AXI_BREADY = Output(Bool())
    
    // master read address channel
    val M_AXI_ARADDR  = Output(UInt(C_S_AXI_ADDR_WIDTH.W))
    val M_AXI_ARPROT  = Output(UInt(3.W))
    val M_AXI_ARVALID = Output(Bool())
    val M_AXI_ARREADY = Input(Bool())
    
    // master read data channel
    val M_AXI_RDATA  = Input(UInt(C_S_AXI_DATA_WIDTH.W))
    val M_AXI_RRESP  = Input(UInt(2.W))
    val M_AXI_RVALID = Input(Bool())
    val M_AXI_RREADY = Output(Bool())

    // slave write address channel
    val S_AXI_AWADDR  = Input(UInt(C_S_AXI_ADDR_WIDTH.W))
    val S_AXI_AWPROT  = Input(UInt(3.W))
    val S_AXI_AWVALID = Input(Bool())
    val S_AXI_AWREADY = Output(Bool())
    
    // slave write data channel
    val S_AXI_WDATA  = Input(UInt(C_S_AXI_DATA_WIDTH.W))
    val S_AXI_WSTRB  = Input(UInt((C_S_AXI_DATA_WIDTH/8).W))
    val S_AXI_WVALID = Input(Bool())
    val S_AXI_WREADY = Output(Bool())
    
    // slave write response channel
    val S_AXI_BRESP  = Output(UInt(2.W))
    val S_AXI_BVALID = Output(Bool())
    val S_AXI_BREADY = Input(Bool())
    
    // slave read address channel
    val S_AXI_ARADDR  = Input(UInt(C_S_AXI_ADDR_WIDTH.W))
    val S_AXI_ARPROT  = Input(UInt(3.W))
    val S_AXI_ARVALID = Input(Bool())
    val S_AXI_ARREADY = Output(Bool())
    
    // slave read data channel
    val S_AXI_RDATA  = Output(UInt(C_S_AXI_DATA_WIDTH.W))
    val S_AXI_RRESP  = Output(UInt(2.W))
    val S_AXI_RVALID = Output(Bool())
    val S_AXI_RREADY = Input(Bool())

    val MM2S_INT     = Input(Bool())
    val S2MM_INT     = Input(Bool())
  })

  val ADDR_LSB: Int = (C_S_AXI_DATA_WIDTH / 32) + 1
  val lowActiveReset = !reset.asBool

  withReset(lowActiveReset) {
    // registers for configuration
    // 0 -- MM2S_SA
    // 1 -- MM2S_LENGTH
    // 2 -- S2MM_DA
    // 3 -- S2MM_LENGTH
    // 4 -- S2MM_DMACR
    // 5 -- MM2S_DMACR
    val r_control = Reg(Vec(9, UInt(32.W)))

    val addrVals = Seq("h18".U(C_S_AXI_ADDR_WIDTH.W), "h28".U(C_S_AXI_ADDR_WIDTH.W), 
                       "h48".U(C_S_AXI_ADDR_WIDTH.W), "h58".U(C_S_AXI_ADDR_WIDTH.W), 
                       "h30".U(C_S_AXI_ADDR_WIDTH.W), "h00".U(C_S_AXI_ADDR_WIDTH.W),
                       "h30".U(C_S_AXI_ADDR_WIDTH.W), "h00".U(C_S_AXI_ADDR_WIDTH.W))
    val r_addrControl = RegInit(VecInit(addrVals))
    val r_idxControl = RegInit(0.U(4.W))

    // registers for diff channels
    // write address channel
    val r_s_axi_awready = RegInit(false.B)
    val r_s_axi_awaddr  = Reg(UInt(C_S_AXI_ADDR_WIDTH.W))

    // write data channel
    val r_s_axi_wready  = RegInit(false.B)
    val r_s_axi_wdata   = Reg(UInt(C_S_AXI_DATA_WIDTH.W))

    // write response channel
    val r_s_axi_bvalid  = RegInit(false.B)

    // read address channel
    val r_s_axi_arready = RegInit(false.B)
    val r_s_axi_araddr  = Reg(UInt(C_S_AXI_ADDR_WIDTH.W)) 

    // read data channel
    val r_s_axi_rvalid  = RegInit(false.B)
    val r_s_axi_rdata   = Reg(UInt(C_S_AXI_DATA_WIDTH.W))

    // write logic
    val sSlaveWIdle :: sSlaveAWActive :: sSlaveWActive :: sSlaveEnd :: Nil = Enum(4)
    val slaveWriteState = RegInit(sSlaveWIdle)

    r_s_axi_awaddr   := Mux(io.S_AXI_AWVALID & io.S_AXI_AWREADY, io.S_AXI_AWADDR(C_S_AXI_ADDR_WIDTH - 1, ADDR_LSB), r_s_axi_awaddr)
    r_s_axi_wdata    := Mux(io.S_AXI_WVALID & io.S_AXI_WREADY, io.S_AXI_WDATA, r_s_axi_wdata)

    switch(slaveWriteState) {
      is(sSlaveWIdle) {
        r_s_axi_awready := true.B
        slaveWriteState      := Mux(io.S_AXI_AWVALID & io.S_AXI_AWREADY, sSlaveAWActive, sSlaveWIdle)
      }
      is(sSlaveAWActive) {
        r_s_axi_awready := false.B
        r_s_axi_wready  := true.B
        slaveWriteState      := Mux(io.S_AXI_WVALID & io.S_AXI_WREADY, sSlaveWActive, sSlaveAWActive)
      }
      is(sSlaveWActive) {
        r_control(r_s_axi_awaddr) := r_s_axi_wdata

        r_s_axi_wready  := false.B
        r_s_axi_bvalid  := true.B
        slaveWriteState      := Mux(io.S_AXI_BVALID & io.S_AXI_BREADY, sSlaveEnd, sSlaveWActive)
      }
      is(sSlaveEnd) {
        r_s_axi_bvalid  := false.B
        slaveWriteState      := sSlaveWIdle
      }
    }

    // read logic
    r_s_axi_araddr     := Mux(io.S_AXI_ARVALID & io.S_AXI_ARREADY, io.S_AXI_ARADDR(C_S_AXI_ADDR_WIDTH - 1, ADDR_LSB), r_s_axi_araddr)

    // read data channel
    val sSlaveRIdle :: sSlaveARActive :: sSlaveREnd :: Nil = Enum(3)
    val slaveReadState = RegInit(sSlaveRIdle)

    switch(slaveReadState) {
      is(sSlaveRIdle) {
        r_s_axi_arready := true.B
        slaveReadState  := Mux(io.S_AXI_ARVALID & io.S_AXI_ARREADY, sSlaveARActive, sSlaveRIdle)
      }
      is(sSlaveARActive) {
        r_s_axi_arready := false.B
        r_s_axi_rvalid  := true.B
        r_s_axi_rdata   := r_control(r_s_axi_araddr)
        slaveReadState  := Mux(io.S_AXI_RVALID & io.S_AXI_RREADY, sSlaveREnd, sSlaveARActive)
      }
      is(sSlaveREnd) {
        r_s_axi_rvalid := false.B
        slaveReadState := sSlaveRIdle
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

    /*********************AXI4 Lite Master Logic******************************/
    val r_start_AXI4_master = RegNext(r_control(8) === 1.U)
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


    // write logic
    val sMasterWIdle :: sMasterActive :: sMasterEnd :: Nil = Enum(3)
    val masterWriteState = RegInit(sMasterWIdle)

    switch(masterWriteState) {
      is(sMasterWIdle) {
        r_m_axi_awaddr   := r_addrControl(r_idxControl)
        r_m_axi_wdata    := r_control(r_idxControl)
        r_m_axi_awvalid  := Mux(r_start_AXI4_master && r_idxControl < 6.U, true.B, false.B)
        r_m_axi_wvalid   := Mux(r_start_AXI4_master && r_idxControl < 6.U, true.B, false.B)
        r_m_axi_bready   := Mux(r_start_AXI4_master && r_idxControl < 6.U, true.B, false.B)
        masterWriteState := Mux(io.M_AXI_AWVALID & io.M_AXI_AWREADY & io.M_AXI_WVALID & io.M_AXI_WREADY, sMasterActive, sMasterWIdle)
      }
      is(sMasterActive) {
        r_m_axi_awvalid  := false.B
        r_m_axi_wvalid   := false.B
        masterWriteState := Mux(io.M_AXI_BVALID & io.M_AXI_BREADY, sMasterEnd, sMasterActive)
      }
      is(sMasterEnd) {
        r_m_axi_bready   := false.B
        r_idxControl     := r_idxControl + 1.U
        masterWriteState := sMasterWIdle
      }
    }

    // read logic
    // read data channel
    val sMasterRIdle :: sMasterARActive :: sMasterREnd :: Nil = Enum(3)
    val masterReadState = RegInit(sMasterRIdle)

    switch(masterReadState) {
      is(sMasterRIdle) {
        r_m_axi_araddr  := r_addrControl(r_idxControl)
        r_m_axi_arvalid := Mux(r_idxControl >= 6.U && r_idxControl < 8.U, true.B, false.B)
        r_m_axi_rready  := Mux(r_idxControl >= 6.U && r_idxControl < 8.U, true.B, false.B)
        masterReadState := Mux(io.M_AXI_ARVALID & io.M_AXI_ARREADY, sMasterARActive, sMasterRIdle)
      }
      is(sMasterARActive) {
        r_m_axi_arvalid := false.B
        masterReadState := Mux(io.M_AXI_RVALID & io.M_AXI_RREADY, sMasterREnd, sMasterARActive)
      }
      is(sMasterREnd) {
        r_m_axi_rready  := false.B
        r_idxControl    := r_idxControl + 1.U
        masterReadState := sMasterRIdle
      }
    }

    // master write address channel
    io.M_AXI_AWADDR  := r_m_axi_awaddr
    io.M_AXI_AWVALID := r_m_axi_awvalid
    io.M_AXI_AWPROT  := 0.U
  
    // master write channel
    io.M_AXI_WVALID  := r_m_axi_wvalid
    io.M_AXI_WDATA   := r_m_axi_wdata
    io.M_AXI_WSTRB   := r_m_axi_wstrb

    // master write response channel
    io.M_AXI_BREADY  := r_m_axi_bready
  
    // master read address channel
    io.M_AXI_ARADDR  := r_m_axi_araddr
    io.M_AXI_ARPROT  := 0.U
    io.M_AXI_ARVALID := r_m_axi_arvalid

    // master read channel
    io.M_AXI_RREADY  := r_m_axi_rready
    r_m_axi_rdata    := io.M_AXI_RDATA

  }
}