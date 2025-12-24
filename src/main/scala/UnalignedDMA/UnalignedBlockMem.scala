package UnalignedDMA

import chisel3._
import chisel3.util._
import chisel3.experimental._

class UnalignedBlockMemory(val C_M_AXI_DATA_WIDTH: Int,
                    val C_M_AXI_ADDR_WIDTH: Int,
                    val MEMORY_DEPTH: Int,
                    val C_M_TARGET_SLAVE_BASE_ADDR: BigInt = 0x0) extends Module {

  val io = IO(new Bundle{
    val mode = Input(UInt(2.W)) // 00 -> disable, 01 -> read, 10 -> write, 11 -> DMA

    // Byte level read/write port
    val readAddr    = Input(UInt(C_M_AXI_ADDR_WIDTH.W))
    val readOffset  = Input(UInt(C_M_AXI_ADDR_WIDTH.W))
    val readLen     = Input(UInt(log2Up(C_M_AXI_DATA_WIDTH / 8 + 1).W))
    val readData    = Output(UInt(C_M_AXI_DATA_WIDTH.W))
    val readValid   = Output(Bool())

    val writeAddr   = Input(UInt(C_M_AXI_ADDR_WIDTH.W))
    val writeOffset = Input(UInt(C_M_AXI_ADDR_WIDTH.W))
    val writeLen    = Input(UInt(log2Up(C_M_AXI_DATA_WIDTH / 8 + 1).W))
    val writeData   = Input(UInt(C_M_AXI_DATA_WIDTH.W))
    val writeValid  = Output(Bool())

    // DMA
    val dmaSrcAddr   = Input(UInt(C_M_AXI_ADDR_WIDTH.W))  // byte address
    val dmaDstAddr   = Input(UInt(C_M_AXI_ADDR_WIDTH.W))  // byte address
    val dmaDstOffset = Input(UInt(C_M_AXI_ADDR_WIDTH.W))
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
  val r_m_axi_awaddr  = RegInit(0.U(C_M_AXI_ADDR_WIDTH.W))
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
  val r_m_axi_araddr  = RegInit(0.U(C_M_AXI_ADDR_WIDTH.W))
  val r_m_axi_arlen   = RegInit(0.U(8.W))

  // read data channel
  val r_m_axi_rready  = RegInit(false.B)
  val r_r_valid       = RegInit(false.B)

  // dma field register
  val r_dma_req_read     = RegInit(false.B)
  val r_dma_req_write    = RegInit(false.B)
  val r_dmaSrc_addr      = RegInit(0.U(C_M_AXI_ADDR_WIDTH.W))
  val r_dmaSrc_len       = RegInit(0.U(log2Up(MEMORY_DEPTH).W))
  val r_dmaDst_addr      = RegInit(0.U(C_M_AXI_ADDR_WIDTH.W))
  val r_dmaDst_len       = RegInit(0.U(log2Up(MEMORY_DEPTH).W))
  val r_dma_read_data    = RegInit(0.U(C_M_AXI_DATA_WIDTH.W))
  // the write length used in unaligned write
  val r_dma_dst_len      = RegInit(0.U(log2Up(C_M_AXI_DATA_WIDTH / 8 + 1).W))

  // read, write, dma request
  val r_read_req       = RegNext((io.mode === 1.U) || r_dma_req_read, init = false.B)
  val r_write_req      = RegNext((io.mode === 2.U) || r_dma_req_write, init = false.B)
  val r_dma_req        = RegNext(io.mode === 3.U, init = false.B)
  val r_read_only_req  = RegNext(io.mode === 1.U, init = false.B)
  val r_write_only_req = RegNext(io.mode === 2.U, init = false.B)

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
  val r_read_addr     = RegNext(Mux(r_dma_req_read, r_dmaSrc_addr, io.readAddr + io.readOffset), init = 0.U)
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

  io.readValid        := RegNext(RegNext(r_read_only_req & r_r_valid))
  io.readData         := r_final_buffer

  r_m_axi_arlen     := 1.U

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
  io.writeValid           := RegNext(r_write_only_req & r_b_valid, init = false.B)
  val r_write_buffer      = RegInit(0.U((2 * C_M_AXI_DATA_WIDTH).W))
  val r_write_padding     = RegInit(0.U((2 * C_M_AXI_DATA_WIDTH).W))
  val r_write_masking     = RegInit(0.U((2 * C_M_AXI_DATA_WIDTH).W))
  val r_write_reversing   = RegInit(0.U((2 * C_M_AXI_DATA_WIDTH).W))
  val r_write_data        = RegInit(0.U((2 * C_M_AXI_DATA_WIDTH).W))
  val r_write_data_shift  = RegInit(0.U((2 * C_M_AXI_DATA_WIDTH).W))
  val r_write_data_1      = RegInit(0.U((2 * C_M_AXI_DATA_WIDTH).W))
  val r_write_data_2      = RegInit(0.U((2 * C_M_AXI_DATA_WIDTH).W))
  val r_write_addr        = RegNext(Mux(r_dma_req_write, r_dmaDst_addr, io.writeAddr + io.writeOffset), init = 0.U)
  val r_write_len         = RegInit(0.U(log2Up(C_M_AXI_DATA_WIDTH / 8 + 1).W))
  val r_write_req_next    = RegNext(r_write_req, init = false.B)
  val r_write_running     = RegInit(false.B)
  val r_write_offset      = RegInit(0.U(3.W))
  val r_aw_enable         = RegInit(false.B)
  val r_first_write_valid = RegInit(false.B)
  val w_m_axi_wlast       = io.M_AXI_WVALID & io.M_AXI_WREADY

  r_m_axi_awlen     := 1.U

  r_write_len       := Mux(r_dma_req_write, r_dma_dst_len, io.writeLen)
  r_write_offset    := r_write_addr(2, 0)
  r_write_padding   := MuxLookup(r_write_len, 1.U,
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

  r_write_data      := Mux(r_dma_req_write, Cat(0.U(C_M_AXI_DATA_WIDTH.W), r_dma_read_data & r_write_padding), Cat(0.U(C_M_AXI_DATA_WIDTH.W), io.writeData & r_write_padding))
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
  val r_dma_req_next     = RegNext(r_dma_req)
  val r_dmaSrc_finish    = RegInit(false.B)
  val r_dmaDst_finish    = RegInit(false.B)
  val r_dmaErase_enable  = RegInit(false.B)

  // data from read port
  io.dmaValid := RegNext(r_dmaDst_finish & r_b_valid, init = false.B)

  // initialize all registers
  when(r_dma_req & ~r_dma_req_next) {
    r_dmaSrc_addr      := io.dmaSrcAddr
    r_dmaDst_addr      := io.dmaDstAddr + io.dmaDstOffset
    r_dmaSrc_len       := io.dmaSrcLen
    r_dmaDst_len       := io.dmaDstLen

    r_dmaErase_enable  := io.dmaSrcLen === 0.U

    r_dma_req_read     := true.B
    r_dmaSrc_finish    := io.dmaSrcLen <= 8.U
    r_dmaDst_finish    := io.dmaDstLen <= 8.U
    r_dma_dst_len      := Mux(io.dmaDstLen > 8.U, 8.U, io.dmaDstLen)
  } 

  val unalignRead_finish = RegNext(RegNext(r_r_valid))
  
  // read transaction
  when((r_dmaSrc_finish & unalignRead_finish) || r_dmaErase_enable) {
    r_dma_req_read     := false.B

    r_dmaSrc_len       := 0.U
    r_dma_req_write    := true.B
  } .elsewhen(r_dma_req_read & unalignRead_finish) {
    r_dma_req_read     := false.B
    r_dmaSrc_len       := Mux(r_dmaSrc_len > 8.U, r_dmaSrc_len - 8.U, r_dmaSrc_len)
    r_dmaSrc_addr      := r_dmaSrc_addr + 8.U
    r_dmaSrc_finish    := r_dmaSrc_len <= 8.U

    r_dma_req_write    := true.B
    r_dma_dst_len      := Mux(r_dmaDst_len > 8.U, 8.U, r_dmaDst_len)
  } 

  // save read data
  when((r_dma_req & unalignRead_finish) || r_dmaErase_enable) {
    r_dma_read_data  := MuxLookup(r_dmaSrc_len, r_final_buffer,
                                    Seq(
                                        0.U -> 0.U,
                                        1.U -> r_final_buffer(7,0),
                                        2.U -> r_final_buffer(15,0),
                                        3.U -> r_final_buffer(23,0),
                                        4.U -> r_final_buffer(31,0),
                                        5.U -> r_final_buffer(39,0),
                                        6.U -> r_final_buffer(47,0),
                                        7.U -> r_final_buffer(55,0)
                                    ))
  }
  
  // write transaction
  when(r_dmaDst_finish & r_b_valid) {
    r_dma_req_write    := false.B

    r_dmaErase_enable  := false.B

    r_dmaSrc_finish    := false.B
    r_dmaDst_finish    := false.B
  } .elsewhen(r_dma_req_write & r_b_valid) {
    r_dma_req_write    := false.B
    r_dmaDst_len       := Mux(r_dmaDst_len > 8.U, r_dmaDst_len - 8.U, r_dmaDst_len)
    r_dmaDst_addr      := r_dmaDst_addr + 8.U
    r_dmaDst_finish    := r_dmaDst_len <= 8.U

    r_dma_req_read     := true.B
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
  io.M_AXI_AWADDR  := Cat(r_m_axi_awaddr(C_M_AXI_ADDR_WIDTH - 1, 3), 0.U(3.W))
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
  io.M_AXI_ARADDR  := Cat(r_m_axi_araddr(C_M_AXI_ADDR_WIDTH - 1, 3), 0.U(3.W))
  io.M_AXI_ARVALID := r_m_axi_arvalid

  io.M_AXI_RREADY  := true.B
}