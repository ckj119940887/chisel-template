package TestArbiter

import chisel3._
import chisel3.util._
import chisel3.experimental._


class BlockMemory( val C_M_AXI_DATA_WIDTH: Int,
                     val MEMORY_DEPTH: Int,
                     val C_M_TARGET_SLAVE_BASE_ADDR: BigInt = 0x0) extends Module {

  val io = IO(new Bundle{
    val mode = Input(UInt(2.W)) // 00 -> disable, 01 -> read, 10 -> write, 11 -> DMA

    // Byte level read/write port
    val readAddr    = Input(UInt(log2Up(MEMORY_DEPTH).W))
    val readOffset  = Input(UInt(log2Up(MEMORY_DEPTH).W))
    val readLen     = Input(UInt(log2Up(C_M_AXI_DATA_WIDTH / 8 + 1).W))
    val readData    = Output(UInt(C_M_AXI_DATA_WIDTH.W))
    val readValid   = Output(Bool())

    val writeAddr   = Input(UInt(log2Up(MEMORY_DEPTH).W))
    val writeOffset = Input(UInt(log2Up(MEMORY_DEPTH).W))
    val writeLen    = Input(UInt(log2Up(C_M_AXI_DATA_WIDTH / 8 + 1).W))
    val writeData   = Input(UInt(C_M_AXI_DATA_WIDTH.W))
    val writeValid  = Output(Bool())

    // DMA
    val dmaSrcAddr   = Input(UInt(log2Up(MEMORY_DEPTH).W))  // byte address
    val dmaDstAddr   = Input(UInt(log2Up(MEMORY_DEPTH).W))  // byte address
    val dmaDstOffset = Input(UInt(log2Up(MEMORY_DEPTH).W))
    val dmaSrcLen    = Input(UInt(log2Up(MEMORY_DEPTH).W)) // byte count
    val dmaDstLen    = Input(UInt(log2Up(MEMORY_DEPTH).W)) // byte count
    val dmaValid     = Output(Bool())

    // master write address channel
    val M_AXI_AWID    = Output(UInt(1.W))
    val M_AXI_AWADDR  = Output(UInt(log2Up(MEMORY_DEPTH).W))
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
    val M_AXI_ARADDR  = Output(UInt(log2Up(MEMORY_DEPTH).W))
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
  val r_m_axi_awaddr  = RegInit(0.U(log2Up(MEMORY_DEPTH).W))
  val r_m_axi_awlen   = RegInit(0.U(8.W))

  // write data channel
  val r_m_axi_wvalid  = RegInit(false.B)
  val r_m_axi_wdata   = RegInit(0.U(C_M_AXI_DATA_WIDTH.W))
  val r_m_axi_wstrb   = RegInit(0.U((C_M_AXI_DATA_WIDTH/8).W))
  val r_m_axi_wlast   = RegInit(false.B)
  val r_w_valid       = RegInit(false.B)

  // write response channel
  val r_m_axi_bready  = RegInit(false.B)
  val r_b_valid       = RegInit(false.B)

  // read address channel
  val r_m_axi_arvalid = RegInit(false.B)
  val r_m_axi_araddr  = RegInit(0.U(log2Up(MEMORY_DEPTH).W))
  val r_m_axi_arlen   = RegInit(0.U(8.W))

  // read data channel
  val r_m_axi_rready  = RegInit(false.B)
  val r_r_valid       = RegInit(false.B)

  val r_read_req      = RegNext(io.mode === 1.U, init = false.B)
  val r_write_req     = RegNext(io.mode === 2.U, init = false.B)
  val r_dma_req       = RegNext(io.mode === 3.U, init = false.B)

  // read logic
  val r_read_buffer   = RegInit(0.U((2 * C_M_AXI_DATA_WIDTH).W))
  val r_buffer_shift0 = RegInit(0.U(C_M_AXI_DATA_WIDTH.W))
  val r_buffer_shift1 = RegInit(0.U(C_M_AXI_DATA_WIDTH.W))
  val r_buffer_shift2 = RegInit(0.U(C_M_AXI_DATA_WIDTH.W))
  val r_buffer_shift3 = RegInit(0.U(C_M_AXI_DATA_WIDTH.W))
  val r_buffer_shift4 = RegInit(0.U(C_M_AXI_DATA_WIDTH.W))
  val r_buffer_shift5 = RegInit(0.U(C_M_AXI_DATA_WIDTH.W))
  val r_buffer_shift6 = RegInit(0.U(C_M_AXI_DATA_WIDTH.W))
  val r_buffer_shift7 = RegInit(0.U(C_M_AXI_DATA_WIDTH.W))
  val r_final_buffer  = RegInit(0.U(C_M_AXI_DATA_WIDTH.W))
  val r_read_addr     = RegNext(io.readAddr + io.readOffset, init = 0.U)
  val r_read_offset   = RegNext(r_read_addr(2,0), init = 0.U)
  val r_read_req_next = RegNext(r_read_req, init = false.B)

  r_buffer_shift0 := r_read_buffer
  r_buffer_shift1 := r_read_buffer >> 8
  r_buffer_shift2 := r_read_buffer >> 16
  r_buffer_shift3 := r_read_buffer >> 24
  r_buffer_shift4 := r_read_buffer >> 32
  r_buffer_shift5 := r_read_buffer >> 40
  r_buffer_shift6 := r_read_buffer >> 48
  r_buffer_shift7 := r_read_buffer >> 56
  r_final_buffer  := MuxLookup(r_read_offset, 0.U,
                              Seq(
                                  0.U -> Cat(0.U(C_M_AXI_DATA_WIDTH.W), r_buffer_shift0),
                                  1.U -> Cat(0.U(C_M_AXI_DATA_WIDTH.W), r_buffer_shift1),
                                  2.U -> Cat(0.U(C_M_AXI_DATA_WIDTH.W), r_buffer_shift2),
                                  3.U -> Cat(0.U(C_M_AXI_DATA_WIDTH.W), r_buffer_shift3),
                                  4.U -> Cat(0.U(C_M_AXI_DATA_WIDTH.W), r_buffer_shift4),
                                  5.U -> Cat(0.U(C_M_AXI_DATA_WIDTH.W), r_buffer_shift5),
                                  6.U -> Cat(0.U(C_M_AXI_DATA_WIDTH.W), r_buffer_shift6),
                                  7.U -> Cat(0.U(C_M_AXI_DATA_WIDTH.W), r_buffer_shift7)
                              ))

  io.readValid        := RegNext(RegNext(r_read_req & r_r_valid))
  io.readData         := r_final_buffer

  r_m_axi_arlen     := Mux(r_dma_req, 0.U, 1.U)

  when(r_read_req & ~r_read_req_next) {
    r_m_axi_arvalid := true.B
    r_m_axi_araddr  := r_read_addr + C_M_TARGET_SLAVE_BASE_ADDR.U
  }

  when(io.M_AXI_ARVALID & io.M_AXI_ARREADY) {
    r_m_axi_arvalid := false.B
  }

  when(io.M_AXI_RVALID & io.M_AXI_RREADY) {
    r_read_buffer   := Cat(io.M_AXI_RDATA, r_read_buffer(2 * C_M_AXI_DATA_WIDTH - 1, C_M_AXI_DATA_WIDTH))
  }

  when(io.M_AXI_RVALID & io.M_AXI_RREADY & io.M_AXI_RLAST) {
    r_r_valid       := true.B
  }

  when(r_r_valid) {
    r_r_valid       := false.B
  }

  // write logic
  io.writeValid           := RegNext(r_write_req & r_b_valid, init = false.B)
  val r_write_buffer      = RegInit(0.U((2 * C_M_AXI_DATA_WIDTH).W))
  val r_write_padding     = RegInit(0.U((2 * C_M_AXI_DATA_WIDTH).W))
  val r_write_masking     = RegInit(0.U((2 * C_M_AXI_DATA_WIDTH).W))
  val r_write_reversing   = RegInit(0.U((2 * C_M_AXI_DATA_WIDTH).W))
  val r_write_data        = RegInit(0.U((2 * C_M_AXI_DATA_WIDTH).W))
  val r_write_data_shift  = RegInit(0.U((2 * C_M_AXI_DATA_WIDTH).W))
  val r_write_data_1      = RegInit(0.U((2 * C_M_AXI_DATA_WIDTH).W))
  val r_write_data_2      = RegInit(0.U((2 * C_M_AXI_DATA_WIDTH).W))
  val r_write_addr        = RegNext(io.writeAddr + io.writeOffset, init = 0.U)
  val r_write_req_next    = RegNext(r_write_req, init=false.B)
  val r_write_running     = RegInit(false.B)
  val r_write_offset      = RegInit(0.U(3.W))
  val r_aw_enable         = RegInit(false.B)
  val r_first_write_valid = RegInit(false.B)
  val w_m_axi_wlast       = io.M_AXI_WVALID & io.M_AXI_WREADY

  r_m_axi_awlen     := Mux(r_dma_req, 0.U, 1.U)

  r_write_offset    := r_write_addr(2, 0)
  r_write_padding   := MuxLookup(io.writeLen, 1.U,
                                  Seq(
                                      1.U -> "hFF".U,
                                      2.U -> "hFFFF".U,
                                      3.U -> "hFFFFFF".U,
                                      4.U -> "hFFFFFFFF".U,
                                      5.U -> "hFFFFFFFFFF".U,
                                      6.U -> "hFFFFFFFFFFFF".U,
                                      7.U -> "hFFFFFFFFFFFFFF".U,
                                      8.U -> "hFFFFFFFFFFFFFFFF".U
                                  ))
  r_write_masking   := MuxLookup(r_write_offset, 0.U,
                                  Seq(
                                      0.U -> r_write_padding,
                                      1.U -> (r_write_padding << 8),
                                      2.U -> (r_write_padding << 16),
                                      3.U -> (r_write_padding << 24),
                                      4.U -> (r_write_padding << 32),
                                      5.U -> (r_write_padding << 40),
                                      6.U -> (r_write_padding << 48),
                                      7.U -> (r_write_padding << 56)
                                  ))
  r_write_reversing := ~r_write_masking

  r_write_data      := Cat(0.U(C_M_AXI_DATA_WIDTH.W), io.writeData)
  r_write_data_shift:= MuxLookup(r_write_offset, 0.U,
                                  Seq(
                                      0.U -> r_write_data,
                                      1.U -> (r_write_data << 8),
                                      2.U -> (r_write_data << 16),
                                      3.U -> (r_write_data << 24),
                                      4.U -> (r_write_data << 32),
                                      5.U -> (r_write_data << 40),
                                      6.U -> (r_write_data << 48),
                                      7.U -> (r_write_data << 56)
                                  ))

  when(r_write_req & ~r_write_req_next) {
    r_m_axi_arvalid := true.B
    r_m_axi_araddr  := r_write_addr + C_M_TARGET_SLAVE_BASE_ADDR.U
  }

  when(io.M_AXI_RVALID & io.M_AXI_RREADY) {
    r_write_buffer  := Cat(io.M_AXI_RDATA, r_write_buffer(2 * C_M_AXI_DATA_WIDTH - 1, C_M_AXI_DATA_WIDTH))
  }

  when(r_r_valid) {
    r_write_data_1  := r_write_buffer & r_write_reversing
  }

  when(r_write_req & RegNext(r_r_valid)) {
    r_write_running := true.B
    r_write_data_2  := r_write_data_1 | r_write_data_shift
  }

  when(r_write_running & ~r_aw_enable) {
    r_aw_enable     := true.B
    r_m_axi_awvalid := true.B
    r_m_axi_awaddr  := r_write_addr + C_M_TARGET_SLAVE_BASE_ADDR.U
  }

  when(r_write_running) {
    r_first_write_valid := true.B
    r_m_axi_wvalid  := true.B
    r_m_axi_wdata   := Mux(r_first_write_valid, r_write_data_2(2 * C_M_AXI_DATA_WIDTH - 1, C_M_AXI_DATA_WIDTH), r_write_data_2(C_M_AXI_DATA_WIDTH - 1, 0))
    r_m_axi_wstrb   := "hFF".U
  }

  when(io.M_AXI_AWVALID & io.M_AXI_AWREADY) {
    r_m_axi_awvalid := false.B
  }

  when(io.M_AXI_WVALID & io.M_AXI_WREADY) {
    r_m_axi_wlast   := true.B
  }

  when(io.M_AXI_WVALID & io.M_AXI_WREADY & io.M_AXI_WLAST) {
    r_aw_enable     := false.B
    r_write_running := false.B
    r_w_valid       := true.B
    r_m_axi_wvalid  := false.B
  }

  when(r_w_valid & io.M_AXI_BVALID & io.M_AXI_BREADY) {
    r_w_valid       := false.B
    r_b_valid       := true.B
    r_m_axi_bready  := false.B
    r_m_axi_wlast   := false.B
    r_first_write_valid := false.B
  } .otherwise {
    r_m_axi_bready  := true.B
  }

  when(r_b_valid) {
    r_b_valid       := false.B
  }

  // dma logic
  val r_dma_req_next     = RegNext(r_dma_req, init= false.B)
  val r_dmaSrc_addr      = RegInit(0.U(log2Up(MEMORY_DEPTH).W))
  val r_dmaSrc_len       = RegInit(0.U(log2Up(MEMORY_DEPTH).W))
  val r_dmaDst_addr      = RegInit(0.U(log2Up(MEMORY_DEPTH).W))
  val r_dmaDst_len       = RegInit(0.U(log2Up(MEMORY_DEPTH).W))
  val r_dma_read_data    = RegInit(0.U(C_M_AXI_DATA_WIDTH.W))
  val r_dma_status       = RegInit(0.U(2.W)) // 0.U - Idle, 1.U - read, 2.U - write
  val r_dmaSrc_finish    = RegNext(r_dmaSrc_len === 0.U, init = false.B)
  val r_dmaDst_finish    = RegNext(r_dmaDst_len === 0.U, init = false.B)
  val r_dmaErase_enable  = RegInit(false.B)
  val r_dmaRead_running  = RegInit(false.B)
  val r_dmaWrite_running = RegInit(false.B)

  io.dmaValid := RegNext(r_dma_req & (r_dma_status === 3.U))

  when(r_dma_req & ~r_dma_req_next) {
    r_dmaSrc_addr      := io.dmaSrcAddr
    r_dmaDst_addr      := io.dmaDstAddr + io.dmaDstOffset
    r_dmaSrc_len       := io.dmaSrcLen
    r_dmaDst_len       := io.dmaDstLen
    // r_dmaSrc_finish    := false.B
    // r_dmaDst_finish    := false.B

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
    r_m_axi_araddr     := r_dmaSrc_addr + C_M_TARGET_SLAVE_BASE_ADDR.U
  }

  when(io.M_AXI_RVALID & io.M_AXI_RREADY & io.M_AXI_RLAST) {
    r_dma_read_data    := io.M_AXI_RDATA
  }

  when(r_dma_status === 2.U & ~r_dmaWrite_running) {
    r_dmaWrite_running := true.B

    r_m_axi_awvalid    := true.B
    r_m_axi_awaddr     := r_dmaDst_addr + C_M_TARGET_SLAVE_BASE_ADDR.U
    r_m_axi_wvalid     := true.B
    r_m_axi_wdata      := Mux((r_dmaSrc_finish & !r_dmaDst_finish) | r_dmaErase_enable, 0.U, r_dma_read_data)
    r_m_axi_wstrb      := "hFF".U
  }

  // AXI4 Full port connection
  io.M_AXI_AWID    := 0.U
  io.M_AXI_AWLEN   := r_m_axi_awlen
  io.M_AXI_AWSIZE  := log2Up(C_M_AXI_DATA_WIDTH / 8 - 1).U
  io.M_AXI_AWBURST := 1.U
  io.M_AXI_AWLOCK  := false.B
  io.M_AXI_AWCACHE := 2.U
  io.M_AXI_AWPROT  := 0.U
  io.M_AXI_AWQOS   := 0.U
  io.M_AXI_AWUSER  := 0.U
  io.M_AXI_AWADDR  := Cat(r_m_axi_awaddr(log2Up(MEMORY_DEPTH) - 1, 3), 0.U(3.W))
  io.M_AXI_AWVALID := r_m_axi_awvalid

  io.M_AXI_WSTRB   := r_m_axi_wstrb
  io.M_AXI_WUSER   := 0.U
  io.M_AXI_WDATA   := r_m_axi_wdata
  io.M_AXI_WLAST   := Mux(r_write_req, r_m_axi_wlast, w_m_axi_wlast)
  io.M_AXI_WVALID  := r_m_axi_wvalid

  io.M_AXI_BREADY  := true.B

  io.M_AXI_ARID    := 0.U
  io.M_AXI_ARLEN   := r_m_axi_arlen
  io.M_AXI_ARSIZE  := log2Up(C_M_AXI_DATA_WIDTH / 8 - 1).U
  io.M_AXI_ARBURST := 1.U
  io.M_AXI_ARLOCK  := false.B
  io.M_AXI_ARCACHE := 2.U
  io.M_AXI_ARPROT  := 0.U
  io.M_AXI_ARQOS   := 0.U
  io.M_AXI_ARUSER  := 0.U
  io.M_AXI_ARADDR  := Cat(r_m_axi_araddr(log2Up(MEMORY_DEPTH) - 1, 3), 0.U(3.W))
  io.M_AXI_ARVALID := r_m_axi_arvalid

  io.M_AXI_RREADY  := true.B
}


class BlockMemoryRequestBundle(dataWidth: Int, depth: Int) extends Bundle {

  val mode         = UInt(2.W)
  val readAddr     = UInt(log2Up(depth).W)
  val readOffset   = UInt(log2Up(depth).W)
  val readLen      = UInt(4.W)
  val writeAddr    = UInt(log2Up(depth).W)
  val writeOffset  = UInt(log2Up(depth).W)
  val writeLen     = UInt(4.W)
  val writeData    = UInt(dataWidth.W)
  val dmaSrcAddr   = UInt(log2Up(depth).W)
  val dmaDstAddr   = UInt(log2Up(depth).W)
  val dmaDstOffset = UInt(log2Up(depth).W)
  val dmaSrcLen    = UInt(log2Up(depth).W)
  val dmaDstLen    = UInt(log2Up(depth).W)

}


class BlockMemoryResponseBundle(dataWidth: Int) extends Bundle {
  val data = UInt(dataWidth.W)
}


class BlockMemoryIO(dataWidth: Int, depth: Int) extends Bundle {
  val req = Valid(new BlockMemoryRequestBundle(dataWidth, depth))
  val resp = Flipped(Valid(new BlockMemoryResponseBundle(dataWidth)))
}


class BlockMemoryArbiterIO(numIPs: Int, dataWidth: Int, depth: Int) extends Bundle {
  val ipReqs  = Flipped(Vec(numIPs, Valid(new BlockMemoryRequestBundle(dataWidth, depth))))
  val ipResps = Vec(numIPs, Valid(new BlockMemoryResponseBundle(dataWidth)))
  val ip      = new BlockMemoryIO(dataWidth, depth)
}


class BlockMemoryArbiterModule(numIPs: Int, dataWidth: Int, depth: Int) extends Module {
  val io = IO(new BlockMemoryArbiterIO(numIPs, dataWidth, depth))

  // ------------------ Stage 0: Input Cache ------------------
  val r_ipReq_valid = RegInit(VecInit(Seq.fill(numIPs)(false.B)))
  val r_ipReq_valid_next = RegInit(VecInit(Seq.fill(numIPs)(false.B)))
  val r_ipReq_enable = RegInit(VecInit(Seq.fill(numIPs)(false.B)))
  val r_ipReq_bits = RegInit(VecInit(Seq.fill(numIPs)(0.U.asTypeOf(new BlockMemoryRequestBundle(dataWidth, depth)))))

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
  val r_reqBits  = RegInit(0.U.asTypeOf(new BlockMemoryRequestBundle(dataWidth, depth)))
  val r_chosen   = RegInit(0.U(log2Up(numIPs).W))

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
  val r_mem_resp_valid = RegNext(io.ip.resp.valid, init = false.B)
  val r_mem_resp_bits  = RegNext(io.ip.resp.bits, init = 0.U.asTypeOf(new BlockMemoryResponseBundle(dataWidth)))
  val r_mem_resp_id    = RegNext(r_chosen, init = 0.U)

  val r_ipResp_valid = RegInit(VecInit(Seq.fill(numIPs)(false.B)))
  val r_ipResp_bits  = RegInit(VecInit(Seq.fill(numIPs)(0.U.asTypeOf(new BlockMemoryResponseBundle(dataWidth)))))

  for (i <- 0 until numIPs) {
    r_ipResp_valid(i)    := false.B
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


class BlockMemoryWrapper(val dataWidth: Int , depth: Int) extends Module {
    val io = IO(new Bundle{
        val req = Input(Valid(new BlockMemoryRequestBundle(dataWidth, depth)))
        val resp = Output(Valid(new BlockMemoryResponseBundle(dataWidth)))

        // master write address channel
        val M_AXI_AWID    = Output(UInt(1.W))
        val M_AXI_AWADDR  = Output(UInt(log2Up(depth).W))
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
        val M_AXI_ARADDR  = Output(UInt(log2Up(depth).W))
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

    val mod = Module(new BlockMemory(dataWidth, depth))

    val r_req            = RegInit(0.U.asTypeOf(new BlockMemoryRequestBundle(dataWidth, depth)))
    val r_req_valid      = RegNext(io.req.valid, false.B)
    val r_req_valid_next = RegNext(r_req_valid, false.B)

    val memory_valid = WireInit(false.B)
    memory_valid := mod.io.readValid | mod.io.writeValid | mod.io.dmaValid
    val r_resp_data  = RegNext(mod.io.readData, init = 0.U)
    val r_resp_valid = RegNext(memory_valid, init = false.B)


    val r_mode = RegInit(0.U(2.W))
    r_mode := Mux(r_req_valid & ~memory_valid, r_req.mode, 0.U)


    r_req := io.req.bits


    mod.io.mode := r_mode
    mod.io.readAddr := r_req.readAddr
    mod.io.readOffset := r_req.readOffset
    mod.io.readLen := r_req.readLen
    mod.io.writeAddr := r_req.writeAddr
    mod.io.writeOffset := r_req.writeOffset
    mod.io.writeLen := r_req.writeLen
    mod.io.writeData := r_req.writeData
    mod.io.dmaSrcAddr := r_req.dmaSrcAddr
    mod.io.dmaDstAddr := r_req.dmaDstAddr
    mod.io.dmaDstOffset := r_req.dmaDstOffset
    mod.io.dmaSrcLen := r_req.dmaSrcLen
    mod.io.dmaDstLen := r_req.dmaDstLen
    io.resp.bits.data := r_resp_data
    io.resp.valid    := r_resp_valid


    io.M_AXI_AWID        := mod.io.M_AXI_AWID
    io.M_AXI_AWADDR      := mod.io.M_AXI_AWADDR
    io.M_AXI_AWLEN       := mod.io.M_AXI_AWLEN
    io.M_AXI_AWSIZE      := mod.io.M_AXI_AWSIZE
    io.M_AXI_AWBURST     := mod.io.M_AXI_AWBURST
    io.M_AXI_AWLOCK      := mod.io.M_AXI_AWLOCK
    io.M_AXI_AWCACHE     := mod.io.M_AXI_AWCACHE
    io.M_AXI_AWPROT      := mod.io.M_AXI_AWPROT
    io.M_AXI_AWQOS       := mod.io.M_AXI_AWQOS
    io.M_AXI_AWUSER      := mod.io.M_AXI_AWUSER
    io.M_AXI_AWVALID     := mod.io.M_AXI_AWVALID
    mod.io.M_AXI_AWREADY := io.M_AXI_AWREADY

    io.M_AXI_WDATA      := mod.io.M_AXI_WDATA
    io.M_AXI_WSTRB      := mod.io.M_AXI_WSTRB
    io.M_AXI_WLAST      := mod.io.M_AXI_WLAST
    io.M_AXI_WUSER      := mod.io.M_AXI_WUSER
    io.M_AXI_WVALID     := mod.io.M_AXI_WVALID
    mod.io.M_AXI_WREADY := io.M_AXI_WREADY

    mod.io.M_AXI_BID    := io.M_AXI_BID
    mod.io.M_AXI_BRESP  := io.M_AXI_BRESP
    mod.io.M_AXI_BUSER  := io.M_AXI_BUSER
    mod.io.M_AXI_BVALID := io.M_AXI_BVALID
    io.M_AXI_BREADY     := mod.io.M_AXI_BREADY

    io.M_AXI_ARID        := mod.io.M_AXI_ARID
    io.M_AXI_ARADDR      := mod.io.M_AXI_ARADDR
    io.M_AXI_ARLEN       := mod.io.M_AXI_ARLEN
    io.M_AXI_ARSIZE      := mod.io.M_AXI_ARSIZE
    io.M_AXI_ARBURST     := mod.io.M_AXI_ARBURST
    io.M_AXI_ARLOCK      := mod.io.M_AXI_ARLOCK
    io.M_AXI_ARCACHE     := mod.io.M_AXI_ARCACHE
    io.M_AXI_ARPROT      := mod.io.M_AXI_ARPROT
    io.M_AXI_ARQOS       := mod.io.M_AXI_ARQOS
    io.M_AXI_ARUSER      := mod.io.M_AXI_ARUSER
    io.M_AXI_ARVALID     := mod.io.M_AXI_ARVALID
    mod.io.M_AXI_ARREADY := io.M_AXI_ARREADY

    mod.io.M_AXI_RID    := io.M_AXI_RID
    mod.io.M_AXI_RDATA  := io.M_AXI_RDATA
    mod.io.M_AXI_RRESP  := io.M_AXI_RRESP
    mod.io.M_AXI_RLAST  := io.M_AXI_RLAST
    mod.io.M_AXI_RUSER  := io.M_AXI_RUSER
    mod.io.M_AXI_RVALID := io.M_AXI_RVALID
    io.M_AXI_RREADY     := mod.io.M_AXI_RREADY

}


class BlockMemoryFunctionModule(dataWidth: Int, depth: Int) extends Module{
  val io = IO(new Bundle{
    val arb_req  = Valid(new BlockMemoryRequestBundle(dataWidth, depth))
    val arb_resp = Flipped(Valid(new BlockMemoryResponseBundle(dataWidth)))
  })

  val r_arb_req          = RegInit(0.U.asTypeOf(new BlockMemoryRequestBundle(dataWidth, depth)))
  val r_arb_req_valid    = RegInit(false.B)
  val r_arb_resp         = RegInit(0.U.asTypeOf(new BlockMemoryResponseBundle(dataWidth)))
  val r_arb_resp_valid   = RegInit(false.B)
  r_arb_resp       := io.arb_resp.bits
  r_arb_resp_valid := io.arb_resp.valid
  io.arb_req.bits  := r_arb_req
  io.arb_req.valid := r_arb_req_valid

  val BlockMemoryCP            = RegInit(0.U(4.W))
  val r_res            = RegInit(0.U(dataWidth.W))

  switch(BlockMemoryCP) {
    is(0.U) {
      r_arb_req_valid       := true.B
      r_arb_req.mode        := 2.U
      r_arb_req.writeAddr   := 0.U
      r_arb_req.writeData   := "h0807060504030201".U
      r_arb_req.writeOffset := 0.U
      r_arb_req.writeLen    := 8.U
      when(r_arb_resp_valid) {
          r_res           := r_arb_resp.data
          r_arb_req_valid := false.B
          BlockMemoryCP   := 1.U
      }
    }
    is(1.U) {
      r_arb_req_valid       := true.B
      r_arb_req.mode        := 2.U
      r_arb_req.writeAddr   := 8.U
      r_arb_req.writeData   := "h100F0E0D0C0B0A09".U
      r_arb_req.writeOffset := 0.U
      r_arb_req.writeLen    := 8.U
      when(r_arb_resp_valid) {
          r_res           := r_arb_resp.data
          r_arb_req_valid := false.B
          BlockMemoryCP   := 2.U
      }
    }
    is(2.U) {
      r_arb_req_valid      := true.B
      r_arb_req.mode       := 1.U
      r_arb_req.readAddr   := 0.U
      r_arb_req.readOffset := 0.U
      r_arb_req.readLen    := 8.U
      when(r_arb_resp_valid) {
          r_res           := r_arb_resp.data
          r_arb_req_valid := false.B
          BlockMemoryCP   := 3.U
      }
    }
    is(3.U) {
      r_arb_req_valid      := true.B
      r_arb_req.mode       := 1.U
      r_arb_req.readAddr   := 8.U
      r_arb_req.readOffset := 0.U
      r_arb_req.readLen    := 8.U
      when(r_arb_resp_valid) {
          r_res           := r_arb_resp.data
          r_arb_req_valid := false.B
          BlockMemoryCP   := 4.U
      }
    }
    is(4.U) {
      r_arb_req_valid        := true.B
      r_arb_req.mode         := 3.U
      r_arb_req.dmaDstAddr   := 16.U
      r_arb_req.dmaSrcAddr   := 0.U
      r_arb_req.dmaDstLen    := 8.U
      r_arb_req.dmaSrcLen    := 8.U
      r_arb_req.dmaDstOffset := 0.U
      when(r_arb_resp_valid) {
          r_res           := r_arb_resp.data
          r_arb_req_valid := false.B
          BlockMemoryCP   := 5.U
      }
    }
    is(5.U) {
      r_arb_req_valid      := true.B
      r_arb_req.mode       := 1.U
      r_arb_req.readAddr   := 16.U
      r_arb_req.readOffset := 0.U
      r_arb_req.readLen    := 8.U
      when(r_arb_resp_valid) {
          r_res           := r_arb_resp.data
          r_arb_req_valid := false.B
          BlockMemoryCP   := 6.U
      }
    }
    is(6.U) {
      printf("result:%d\n", r_res)
    }
  }
}

