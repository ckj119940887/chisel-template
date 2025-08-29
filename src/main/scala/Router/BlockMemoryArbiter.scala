package Router

import chisel3._
import chisel3.util._

class BlockMemoryRequestBundle(addrWidth: Int, dataWidth: Int, depth: Int) extends Bundle {
  val mode       = UInt(2.W)
  val readAddr   = UInt(addrWidth.W)
  val writeAddr  = UInt(addrWidth.W)
  val writeData  = UInt(dataWidth.W)
  val dmaSrcAddr = UInt(addrWidth.W)
  val dmaDstAddr = UInt(addrWidth.W)
  val dmaSrcLen  = UInt(log2Up(depth).W)
  val dmaDstLen  = UInt(log2Up(depth).W)
}

class BlockMemoryResponseBundle(dataWidth: Int) extends Bundle {
  val data = UInt(dataWidth.W)
}

class BlockMemoryIO(addrWidth: Int, dataWidth: Int, depth: Int) extends Bundle {
  val req = Valid(new BlockMemoryRequestBundle(addrWidth, dataWidth, depth))
  val resp = Flipped(Valid(new BlockMemoryResponseBundle(dataWidth)))
}

class BlockMemoryArbiterIO(numIPs: Int, addrWidth: Int, dataWidth: Int, depth: Int) extends Bundle {
  val ipReqs  = Flipped(Vec(numIPs, Valid(new BlockMemoryRequestBundle(addrWidth, dataWidth, depth))))
  val ipResps = Vec(numIPs, Valid(new BlockMemoryResponseBundle(dataWidth)))
  val ip      = new BlockMemoryIO(addrWidth, dataWidth, depth)
}

class BlockMemoryArbiterModule(numIPs: Int, addrWidth: Int, dataWidth: Int, depth: Int) extends Module {
  val io = IO(new BlockMemoryArbiterIO(numIPs, addrWidth, dataWidth, depth))

  // ------------------ Stage 0: Input Cache ------------------
  val r_ipReq_valid = RegInit(VecInit(Seq.fill(numIPs)(false.B)))
  val r_ipReq_valid_next = RegInit(VecInit(Seq.fill(numIPs)(false.B)))
  val r_ipReq_enable = RegInit(VecInit(Seq.fill(numIPs)(false.B)))
  val r_ipReq_bits = Reg(Vec(numIPs, new BlockMemoryRequestBundle(addrWidth, dataWidth, depth)))

  for (i <- 0 until numIPs) {
    r_ipReq_valid(i) := io.ipReqs(i).valid
    r_ipReq_valid_next(i) := r_ipReq_valid(i)
    when(r_ipReq_valid(i) & ~r_ipReq_valid_next(i)) {
      r_ipReq_enable(i) := true.B
      r_ipReq_bits(i) := io.ipReqs(i).bits
    } .otherwise {
      r_ipReq_enable(i) := false.B
    }
  }

  // ------------------ Stage 1: Arbitration Decision Pipeline ------------------
  val r_foundReq = RegInit(false.B)
  val r_reqBits  = Reg(new BlockMemoryRequestBundle(addrWidth, dataWidth, depth))
  val r_chosen   = Reg(UInt(log2Ceil(numIPs).W))

  r_foundReq := r_ipReq_enable.reduce(_ || _)
  for (i <- 0 until numIPs) {
    when(r_ipReq_enable(i)) {
      r_reqBits := r_ipReq_bits(i)
      r_chosen  := i.U
    }
  }

  io.ip.req.valid := r_foundReq
  io.ip.req.bits  := r_reqBits 

  // ------------------ Stage 2: memory.resp handling ------------------
  val r_mem_resp_valid = RegNext(io.ip.resp.valid)
  val r_mem_resp_bits  = RegNext(io.ip.resp.bits)
  val r_mem_resp_id    = RegNext(r_chosen)

  val r_ipResp_valid = RegInit(VecInit(Seq.fill(numIPs)(false.B)))
  val r_ipResp_bits  = Reg(Vec(numIPs, new BlockMemoryResponseBundle(dataWidth)))

  for (i <- 0 until numIPs) {
    r_ipResp_valid(i)     := false.B
    r_ipResp_bits(i).data := 0.U
  }

  when(r_mem_resp_valid) {
    r_ipResp_valid(r_mem_resp_id) := true.B
    r_ipResp_bits(r_mem_resp_id)  := r_mem_resp_bits
  } .otherwise {
    r_ipResp_valid(r_mem_resp_id) := false.B
  }

  for (i <- 0 until numIPs) {
    io.ipResps(i).valid := r_ipResp_valid(i)
    io.ipResps(i).bits  := r_ipResp_bits(i)
  }
}

class BlockMemory(val C_M_AXI_ADDR_WIDTH: Int,
                  val C_M_AXI_DATA_WIDTH: Int,
                  val C_M_TARGET_SLAVE_BASE_ADDR: BigInt,
                  val MEMORY_DEPTH: Int) extends Module {

  val io = IO(new Bundle{
    val mode = Input(UInt(2.W)) // 00 -> disable, 01 -> read, 10 -> write, 11 -> DMA

    // Byte level read/write port
    val readAddr    = Input(UInt(C_M_AXI_ADDR_WIDTH.W))
    val readData    = Output(UInt(C_M_AXI_DATA_WIDTH.W))
    val readValid   = Output(Bool())

    val writeAddr   = Input(UInt(C_M_AXI_ADDR_WIDTH.W))
    val writeData   = Input(UInt(C_M_AXI_DATA_WIDTH.W))
    val writeValid  = Output(Bool())

    // DMA
    val dmaSrcAddr   = Input(UInt(C_M_AXI_ADDR_WIDTH.W))  // byte address
    val dmaDstAddr   = Input(UInt(C_M_AXI_ADDR_WIDTH.W))  // byte address
    val dmaSrcLen    = Input(UInt(log2Up(MEMORY_DEPTH).W)) // byte count
    val dmaDstLen    = Input(UInt(log2Up(MEMORY_DEPTH).W)) // byte count
    val dmaValid     = Output(Bool())

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

  // registers for diff channels
  // write address channel
  val r_m_axi_awvalid = RegInit(false.B)
  val r_m_axi_awaddr  = Reg(UInt(C_M_AXI_ADDR_WIDTH.W))

  // write data channel
  val r_m_axi_wvalid  = RegInit(false.B)
  val r_m_axi_wdata   = RegInit(0.U(C_M_AXI_DATA_WIDTH.W))
  val r_m_axi_wstrb   = Reg(UInt((C_M_AXI_DATA_WIDTH/8).W))
  val r_m_axi_wlast   = RegInit(false.B)
  val r_w_valid       = RegInit(false.B)

  // write response channel
  val r_b_valid       = RegInit(false.B)

  // read address channel
  val r_m_axi_arvalid = RegInit(false.B)
  val r_m_axi_araddr  = Reg(UInt(C_M_AXI_ADDR_WIDTH.W))

  // read data channel
  val r_m_axi_rready  = RegInit(false.B)
  val r_r_valid       = RegInit(false.B)

  val r_read_req      = RegNext(io.mode === 1.U)
  val r_write_req     = RegNext(io.mode === 2.U)
  val r_dma_req       = RegNext(io.mode === 3.U)

  // read logic
  val r_read_req_next = RegNext(r_read_req)
  val r_read_addr     = RegNext(io.readAddr + C_M_TARGET_SLAVE_BASE_ADDR.U)

  io.readValid        := r_read_req & r_r_valid
  io.readData         := RegNext(io.M_AXI_RDATA)

  when(r_read_req & ~r_read_req_next) {
    r_m_axi_arvalid   := true.B
    r_m_axi_araddr    := r_read_addr
  }

  when(io.M_AXI_ARVALID & io.M_AXI_ARREADY) {
    r_m_axi_arvalid   := false.B
  }

  when(io.M_AXI_RVALID & io.M_AXI_RREADY & io.M_AXI_RLAST) {
    r_r_valid         := true.B
  }

  when(r_r_valid) {
    r_r_valid         := false.B
  }

  // write logic
  io.writeValid        := r_write_req & r_b_valid
  val r_write_addr     = RegNext(io.writeAddr + C_M_TARGET_SLAVE_BASE_ADDR.U)
  val r_write_req_next = RegNext(r_write_req)
  val r_write_data     = RegNext(io.writeData)

  when(r_write_req & ~r_write_req_next) {
    r_m_axi_awvalid := true.B
    r_m_axi_awaddr  := r_write_addr
    r_m_axi_wvalid  := true.B
    r_m_axi_wdata   := r_write_data
    r_m_axi_wstrb   := "hFF".U
    r_m_axi_wlast   := true.B
  }

  when(io.M_AXI_AWVALID & io.M_AXI_AWREADY) {
    r_m_axi_awvalid := false.B
  }

  when(io.M_AXI_WVALID & io.M_AXI_WREADY & io.M_AXI_WLAST) {
    r_w_valid       := true.B
    r_m_axi_wvalid  := false.B
  }

  when(r_w_valid & io.M_AXI_BVALID & io.M_AXI_BREADY) {
    r_w_valid       := false.B
    r_b_valid       := true.B
    r_m_axi_wlast   := false.B
  }

  when(r_b_valid) {
    r_b_valid       := false.B
  }

  // dma logic
  val r_dma_req_next     = RegNext(r_dma_req)
  val r_dmaSrc_addr      = Reg(UInt(C_M_AXI_ADDR_WIDTH.W))
  val r_dmaSrc_len       = Reg(UInt(log2Up(MEMORY_DEPTH).W))
  val r_dmaDst_addr      = Reg(UInt(C_M_AXI_ADDR_WIDTH.W))
  val r_dmaDst_len       = Reg(UInt(log2Up(MEMORY_DEPTH).W))

  val r_dma_read_data    = Reg(UInt(C_M_AXI_DATA_WIDTH.W))
  val r_dma_status       = RegInit(0.U(2.W)) // 0.U - Idle, 1.U - read, 2.U - write
  val r_dmaSrc_finish    = RegNext(r_dmaSrc_len === 0.U)
  val r_dmaDst_finish    = RegNext(r_dmaDst_len === 0.U)
  val r_dmaErase_enable  = RegInit(false.B)
  val r_dmaRead_running  = RegInit(false.B)
  val r_dmaWrite_running = RegInit(false.B)

  io.dmaValid := RegNext(r_dma_req & (r_dma_status === 3.U))

  when(r_dma_req & ~r_dma_req_next) {
    r_dmaSrc_addr      := io.dmaSrcAddr
    r_dmaDst_addr      := io.dmaDstAddr
    r_dmaSrc_len       := io.dmaSrcLen
    r_dmaDst_len       := io.dmaDstLen

    r_dmaRead_running  := false.B
    r_dmaWrite_running := false.B

    r_dmaErase_enable  := io.dmaSrcLen === 0.U

    r_dma_status       := Mux(io.dmaSrcLen === 0.U, 2.U, 1.U)
  } .elsewhen(r_dma_req & r_r_valid) {
    r_dma_status       := 2.U
    r_dmaSrc_len       := Mux(r_dmaSrc_len =/= 0.U, r_dmaSrc_len - 8.U, r_dmaSrc_len)
  } .elsewhen(r_dma_req & r_b_valid) {
    r_dma_status       := Mux(!r_dmaSrc_finish, 1.U, Mux(r_dmaDst_finish, 3.U, 2.U))

    r_dmaRead_running  := false.B
    r_dmaWrite_running := false.B

    r_dmaSrc_addr      := r_dmaSrc_addr + 8.U
    r_dmaDst_addr      := r_dmaDst_addr + 8.U
  }

  when(r_dma_req & io.M_AXI_AWVALID & io.M_AXI_AWREADY) {
    r_dmaDst_len       := Mux(r_dmaDst_len =/= 0.U, r_dmaDst_len - 8.U, r_dmaDst_len)
  }

  when(r_dma_status === 3.U) {
    r_dma_status       := 0.U
    r_dmaErase_enable  := false.B
  }

  when(r_dma_status === 1.U & ~r_dmaRead_running) {
    r_dmaRead_running  := true.B

    r_m_axi_arvalid    := true.B
    r_m_axi_araddr     := r_dmaSrc_addr
  }

  when(io.M_AXI_RVALID & io.M_AXI_RREADY & io.M_AXI_RLAST) {
    r_dma_read_data    := io.M_AXI_RDATA
  }

  when(r_dma_status === 2.U & ~r_dmaWrite_running) {
    r_dmaWrite_running := true.B

    r_m_axi_awvalid    := true.B
    r_m_axi_awaddr     := r_dmaDst_addr
    r_m_axi_wvalid     := true.B
    r_m_axi_wdata      := Mux(r_dmaSrc_finish | r_dmaErase_enable, 0.U, r_dma_read_data)
    r_m_axi_wstrb      := "hFF".U
    r_m_axi_wlast      := true.B
  }

  // AXI4 Full port connection
  io.M_AXI_AWID    := 0.U
  io.M_AXI_AWLEN   := 0.U
  io.M_AXI_AWSIZE  := log2Up(C_M_AXI_DATA_WIDTH / 8 - 1).U
  io.M_AXI_AWBURST := 1.U
  io.M_AXI_AWLOCK  := false.B
  io.M_AXI_AWCACHE := 2.U
  io.M_AXI_AWPROT  := 0.U
  io.M_AXI_AWQOS   := 0.U
  io.M_AXI_AWUSER  := 0.U
  io.M_AXI_AWADDR  := r_m_axi_awaddr
  io.M_AXI_AWVALID := r_m_axi_awvalid

  io.M_AXI_WSTRB   := r_m_axi_wstrb
  io.M_AXI_WUSER   := 0.U
  io.M_AXI_WDATA   := r_m_axi_wdata
  io.M_AXI_WLAST   := r_m_axi_wlast
  io.M_AXI_WVALID  := r_m_axi_wvalid

  io.M_AXI_BREADY  := true.B

  io.M_AXI_ARID    := 0.U
  io.M_AXI_ARLEN   := 0.U
  io.M_AXI_ARSIZE  := log2Up(C_M_AXI_DATA_WIDTH / 8 - 1).U
  io.M_AXI_ARBURST := 1.U
  io.M_AXI_ARLOCK  := false.B
  io.M_AXI_ARCACHE := 2.U
  io.M_AXI_ARPROT  := 0.U
  io.M_AXI_ARQOS   := 0.U
  io.M_AXI_ARUSER  := 0.U
  io.M_AXI_ARADDR  := r_m_axi_araddr
  io.M_AXI_ARVALID := r_m_axi_arvalid

  io.M_AXI_RREADY  := true.B
}

class BlockMemoryWrapper(val depth: Int = 1024, val addrWidth: Int = 64, val dataWidth: Int = 64) extends Module {
    val io = IO(new Bundle{
        val req = Input(Valid(new BlockMemoryRequestBundle(addrWidth, dataWidth, depth)))
        val resp = Output(Valid(new BlockMemoryResponseBundle(dataWidth)))

        // master write address channel
        val M_AXI_AWID    = Output(UInt(1.W))
        val M_AXI_AWADDR  = Output(UInt(addrWidth.W))
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
        val M_AXI_WDATA  = Output(UInt(dataWidth.W))
        val M_AXI_WSTRB  = Output(UInt((dataWidth/8).W))
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
        val M_AXI_ARADDR  = Output(UInt(addrWidth.W))
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
        val M_AXI_RDATA  = Input(UInt(dataWidth.W))
        val M_AXI_RRESP  = Input(UInt(2.W))
        val M_AXI_RLAST  = Input(Bool())
        val M_AXI_RUSER  = Input(UInt(1.W))
        val M_AXI_RVALID = Input(Bool())
        val M_AXI_RREADY = Output(Bool())
    })

    val mem = Module(new BlockMemory(C_M_AXI_ADDR_WIDTH = addrWidth, C_M_AXI_DATA_WIDTH = dataWidth, C_M_TARGET_SLAVE_BASE_ADDR = 0x0, MEMORY_DEPTH = depth))
    // mem.io.mode       := 0.U
    // mem.io.readAddr   := 0.U
    // mem.io.writeAddr  := 0.U
    // mem.io.writeData  := 0.U
    // mem.io.dmaSrcAddr := 0.U
    // mem.io.dmaDstAddr := 0.U
    // mem.io.dmaSrcLen  := 0.U
    // mem.io.dmaDstLen  := 0.U

    io.M_AXI_AWID        := mem.io.M_AXI_AWID   
    io.M_AXI_AWADDR      := mem.io.M_AXI_AWADDR 
    io.M_AXI_AWLEN       := mem.io.M_AXI_AWLEN  
    io.M_AXI_AWSIZE      := mem.io.M_AXI_AWSIZE 
    io.M_AXI_AWBURST     := mem.io.M_AXI_AWBURST
    io.M_AXI_AWLOCK      := mem.io.M_AXI_AWLOCK 
    io.M_AXI_AWCACHE     := mem.io.M_AXI_AWCACHE
    io.M_AXI_AWPROT      := mem.io.M_AXI_AWPROT 
    io.M_AXI_AWQOS       := mem.io.M_AXI_AWQOS  
    io.M_AXI_AWUSER      := mem.io.M_AXI_AWUSER 
    io.M_AXI_AWVALID     := mem.io.M_AXI_AWVALID
    mem.io.M_AXI_AWREADY := io.M_AXI_AWREADY

    io.M_AXI_WDATA       := mem.io.M_AXI_WDATA 
    io.M_AXI_WSTRB       := mem.io.M_AXI_WSTRB 
    io.M_AXI_WLAST       := mem.io.M_AXI_WLAST 
    io.M_AXI_WUSER       := mem.io.M_AXI_WUSER 
    io.M_AXI_WVALID      := mem.io.M_AXI_WVALID
    mem.io.M_AXI_WREADY  := io.M_AXI_WREADY

    mem.io.M_AXI_BID     := io.M_AXI_BID   
    mem.io.M_AXI_BRESP   := io.M_AXI_BRESP 
    mem.io.M_AXI_BUSER   := io.M_AXI_BUSER 
    mem.io.M_AXI_BVALID  := io.M_AXI_BVALID
    io.M_AXI_BREADY      := mem.io.M_AXI_BREADY

    io.M_AXI_ARID        := mem.io.M_AXI_ARID   
    io.M_AXI_ARADDR      := mem.io.M_AXI_ARADDR 
    io.M_AXI_ARLEN       := mem.io.M_AXI_ARLEN  
    io.M_AXI_ARSIZE      := mem.io.M_AXI_ARSIZE 
    io.M_AXI_ARBURST     := mem.io.M_AXI_ARBURST
    io.M_AXI_ARLOCK      := mem.io.M_AXI_ARLOCK 
    io.M_AXI_ARCACHE     := mem.io.M_AXI_ARCACHE
    io.M_AXI_ARPROT      := mem.io.M_AXI_ARPROT 
    io.M_AXI_ARQOS       := mem.io.M_AXI_ARQOS  
    io.M_AXI_ARUSER      := mem.io.M_AXI_ARUSER 
    io.M_AXI_ARVALID     := mem.io.M_AXI_ARVALID
    mem.io.M_AXI_ARREADY := io.M_AXI_ARREADY

    mem.io.M_AXI_RID     := io.M_AXI_RID   
    mem.io.M_AXI_RDATA   := io.M_AXI_RDATA 
    mem.io.M_AXI_RRESP   := io.M_AXI_RRESP 
    mem.io.M_AXI_RLAST   := io.M_AXI_RLAST 
    mem.io.M_AXI_RUSER   := io.M_AXI_RUSER 
    mem.io.M_AXI_RVALID  := io.M_AXI_RVALID
    io.M_AXI_RREADY      := mem.io.M_AXI_RREADY

    val r_req            = Reg(new BlockMemoryRequestBundle(addrWidth, dataWidth, depth))
    val r_req_valid      = RegNext(io.req.valid, false.B)
    val r_req_valid_next = RegNext(r_req_valid, false.B)

    val r_resp_data  = RegNext(mem.io.readData)
    val r_resp_valid = RegNext(mem.io.readValid | mem.io.writeValid | mem.io.dmaValid)

    val r_busy       = RegInit(0.U(2.W))
    when(r_req_valid & ~r_req_valid_next) {
        r_busy := 3.U
    } .elsewhen(mem.io.readValid | mem.io.writeValid | mem.io.dmaValid) {
        r_busy := 0.U
    }

    r_req := io.req.bits

    io.resp.valid := r_resp_valid
    io.resp.bits.data := r_resp_data

    //when(r_req_valid) {
      mem.io.mode       := r_req.mode & r_busy     
      mem.io.readAddr   := r_req.readAddr  
      mem.io.writeAddr  := r_req.writeAddr 
      mem.io.writeData  := r_req.writeData 
      mem.io.dmaSrcAddr := r_req.dmaSrcAddr
      mem.io.dmaDstAddr := r_req.dmaDstAddr
      mem.io.dmaSrcLen  := r_req.dmaSrcLen 
      mem.io.dmaDstLen  := r_req.dmaDstLen 
    //}
}