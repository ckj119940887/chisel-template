package TestArbiter

import chisel3._
import chisel3.util._
import chisel3.experimental._

class TestMemoryArbiter(val dataWidth: Int, val addrWidth: Int, depth: Int) extends Module{
    val io = IO(new Bundle{
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

    val modWrapper   = Module(new BlockMemoryWrapper(dataWidth = 64, addrWidth = 16, depth = 200))
    val arbMod       = Module(new BlockMemoryArbiterModule(numIPs = 1, dataWidth = 64, addrWidth = 16, depth = 200))
    val testFunction = Module(new BlockMemoryFunctionModule(dataWidth = 64, addrWidth = 16, depth = 200))

    arbMod.io.ip.req  <> modWrapper.io.req
    arbMod.io.ip.resp <> modWrapper.io.resp

    arbMod.io.ipReqs(0)  <> testFunction.io.arb_req
    arbMod.io.ipResps(0) <> testFunction.io.arb_resp

    io.M_AXI_AWID               := modWrapper.io.M_AXI_AWID
    io.M_AXI_AWADDR             := modWrapper.io.M_AXI_AWADDR
    io.M_AXI_AWLEN              := modWrapper.io.M_AXI_AWLEN
    io.M_AXI_AWSIZE             := modWrapper.io.M_AXI_AWSIZE
    io.M_AXI_AWBURST            := modWrapper.io.M_AXI_AWBURST
    io.M_AXI_AWLOCK             := modWrapper.io.M_AXI_AWLOCK
    io.M_AXI_AWCACHE            := modWrapper.io.M_AXI_AWCACHE
    io.M_AXI_AWPROT             := modWrapper.io.M_AXI_AWPROT
    io.M_AXI_AWQOS              := modWrapper.io.M_AXI_AWQOS
    io.M_AXI_AWUSER             := modWrapper.io.M_AXI_AWUSER
    io.M_AXI_AWVALID            := modWrapper.io.M_AXI_AWVALID
    modWrapper.io.M_AXI_AWREADY := io.M_AXI_AWREADY

    io.M_AXI_WDATA             := modWrapper.io.M_AXI_WDATA
    io.M_AXI_WSTRB             := modWrapper.io.M_AXI_WSTRB
    io.M_AXI_WLAST             := modWrapper.io.M_AXI_WLAST
    io.M_AXI_WUSER             := modWrapper.io.M_AXI_WUSER
    io.M_AXI_WVALID            := modWrapper.io.M_AXI_WVALID
    modWrapper.io.M_AXI_WREADY := io.M_AXI_WREADY

    modWrapper.io.M_AXI_BID    := io.M_AXI_BID
    modWrapper.io.M_AXI_BRESP  := io.M_AXI_BRESP
    modWrapper.io.M_AXI_BUSER  := io.M_AXI_BUSER
    modWrapper.io.M_AXI_BVALID := io.M_AXI_BVALID
    io.M_AXI_BREADY            := modWrapper.io.M_AXI_BREADY

    io.M_AXI_ARID               := modWrapper.io.M_AXI_ARID
    io.M_AXI_ARADDR             := modWrapper.io.M_AXI_ARADDR
    io.M_AXI_ARLEN              := modWrapper.io.M_AXI_ARLEN
    io.M_AXI_ARSIZE             := modWrapper.io.M_AXI_ARSIZE
    io.M_AXI_ARBURST            := modWrapper.io.M_AXI_ARBURST
    io.M_AXI_ARLOCK             := modWrapper.io.M_AXI_ARLOCK
    io.M_AXI_ARCACHE            := modWrapper.io.M_AXI_ARCACHE
    io.M_AXI_ARPROT             := modWrapper.io.M_AXI_ARPROT
    io.M_AXI_ARQOS              := modWrapper.io.M_AXI_ARQOS
    io.M_AXI_ARUSER             := modWrapper.io.M_AXI_ARUSER
    io.M_AXI_ARVALID            := modWrapper.io.M_AXI_ARVALID
    modWrapper.io.M_AXI_ARREADY := io.M_AXI_ARREADY

    modWrapper.io.M_AXI_RID    := io.M_AXI_RID
    modWrapper.io.M_AXI_RDATA  := io.M_AXI_RDATA
    modWrapper.io.M_AXI_RRESP  := io.M_AXI_RRESP
    modWrapper.io.M_AXI_RLAST  := io.M_AXI_RLAST
    modWrapper.io.M_AXI_RUSER  := io.M_AXI_RUSER
    modWrapper.io.M_AXI_RVALID := io.M_AXI_RVALID
    io.M_AXI_RREADY            := modWrapper.io.M_AXI_RREADY
}
/*
class TestOnlyBlockMemory(val dataWidth: Int, val depth: Int) extends Module {

    val io = IO(new Bundle{
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

        // write address channel
        val M_LITE_AXI_AWADDR  = Output(UInt(16.W))
        val M_LITE_AXI_AWPROT  = Output(UInt(3.W))
        val M_LITE_AXI_AWVALID = Output(Bool())
        val M_LITE_AXI_AWREADY = Input(Bool())
    
        // write data channel
        val M_LITE_AXI_WDATA  = Output(UInt(32.W))
        val M_LITE_AXI_WSTRB  = Output(UInt((32/8).W))
        val M_LITE_AXI_WVALID = Output(Bool())
        val M_LITE_AXI_WREADY = Input(Bool())
    
        // write response channel
        val M_LITE_AXI_BRESP  = Input(UInt(2.W))
        val M_LITE_AXI_BVALID = Input(Bool())
        val M_LITE_AXI_BREADY = Output(Bool())
    
        // read address channel
        val M_LITE_AXI_ARADDR  = Output(UInt(16.W))
        val M_LITE_AXI_ARPROT  = Output(UInt(3.W))
        val M_LITE_AXI_ARVALID = Output(Bool())
        val M_LITE_AXI_ARREADY = Input(Bool())
    
        // read data channel
        val M_LITE_AXI_RDATA  = Input(UInt(32.W))
        val M_LITE_AXI_RRESP  = Input(UInt(2.W))
        val M_LITE_AXI_RVALID = Input(Bool())
        val M_LITE_AXI_RREADY = Output(Bool())
    })

    val r_m_lite_axi_awaddr  = RegInit(0.U(16.W))
    val r_m_lite_axi_awvalid = RegInit(false.B)
    val r_m_lite_axi_wdata   = RegInit(0.U(32.W))
    val r_m_lite_axi_wstrb   = RegInit("b1111".U((32/8).W))
    val r_m_lite_axi_wvalid  = RegInit(false.B)
    val r_m_lite_axi_bready  = RegInit(false.B)
    val r_m_lite_axi_araddr  = RegInit(0.U(16.W))
    val r_m_lite_axi_arvalid = RegInit(false.B)
    val r_m_lite_axi_rready  = RegInit(false.B)

    io.M_LITE_AXI_AWADDR  := r_m_lite_axi_awaddr
    io.M_LITE_AXI_AWPROT  := 0.U
    io.M_LITE_AXI_AWVALID := r_m_lite_axi_awvalid

    io.M_LITE_AXI_WDATA   := r_m_lite_axi_wdata 
    io.M_LITE_AXI_WSTRB   := r_m_lite_axi_wstrb
    io.M_LITE_AXI_WVALID  := r_m_lite_axi_wvalid 

    io.M_LITE_AXI_BREADY  := r_m_lite_axi_bready

    io.M_LITE_AXI_ARADDR  := r_m_lite_axi_araddr 
    io.M_LITE_AXI_ARPROT  := 0.U
    io.M_LITE_AXI_ARVALID := r_m_lite_axi_arvalid

    io.M_LITE_AXI_RREADY  := r_m_lite_axi_rready

    val r_lite_aw_fire = r_m_lite_axi_awvalid & io.M_LITE_AXI_AWREADY

    val r_lite_w_fire = r_m_lite_axi_wvalid & io.M_LITE_AXI_WREADY

    val r_lite_b_fire = r_m_lite_axi_bready & io.M_LITE_AXI_BVALID

    val r_lite_ar_fire = r_m_lite_axi_arvalid & io.M_LITE_AXI_ARREADY

    val r_lite_r_fire = r_m_lite_axi_rready & io.M_LITE_AXI_RVALID

    val liteCP = RegInit(0.U(4.W))
    val r_lite_res = RegInit(0.U(32.W))
    switch(liteCP) {
      is(0.U) {
        r_m_lite_axi_awaddr  := 0.U
        r_m_lite_axi_awvalid := true.B
        r_m_lite_axi_wdata  := "h01020304".U
        r_m_lite_axi_wvalid := true.B
        when(r_lite_aw_fire & r_lite_w_fire) {
          liteCP := 1.U
          r_m_lite_axi_bready  := true.B
          r_m_lite_axi_awvalid := false.B
          r_m_lite_axi_wvalid  := false.B
        }
      }
      is(1.U) {
        when(r_lite_b_fire) {
          liteCP := 2.U
        }
      }
      is(2.U) {
        r_m_lite_axi_araddr  := 0.U
        r_m_lite_axi_arvalid := true.B
        when(r_lite_ar_fire) {
          liteCP := 3.U
          r_m_lite_axi_arvalid := false.B
          r_m_lite_axi_rready := true.B
        }
      }
      is(3.U) {
        when(r_lite_r_fire) {
          liteCP := 4.U
          r_lite_res := io.M_LITE_AXI_RDATA
        }
      }
      is(4.U) {
        printf("%d\n", r_lite_res)
      }
    }


    val mod = Module(new BlockMemory(C_M_AXI_ADDR_WIDTH = 16,
                                     C_M_AXI_DATA_WIDTH = dataWidth,
                                     MEMORY_DEPTH = depth,
                                     C_M_TARGET_SLAVE_BASE_ADDR = 0x0) )

    mod.io.mode         := 0.U 
    mod.io.readAddr     := 0.U
    mod.io.readOffset   := 0.U
    mod.io.readLen      := 0.U
    mod.io.writeAddr    := 0.U
    mod.io.writeOffset  := 0.U
    mod.io.writeLen     := 0.U
    mod.io.writeData    := 0.U
    mod.io.dmaSrcAddr   := 0.U
    mod.io.dmaDstAddr   := 0.U
    mod.io.dmaDstOffset := 0.U
    mod.io.dmaSrcLen    := 0.U    
    mod.io.dmaDstLen    := 0.U

    val BlockMemoryCP            = RegInit(0.U(4.W))
    val r_res            = Reg(UInt(dataWidth.W))

  switch(BlockMemoryCP) {
    /*
    is(0.U) {
      mod.io.mode        := 2.U
      mod.io.writeAddr   := 0.U
      mod.io.writeData   := "h0807060504030201".U
      mod.io.writeOffset := 0.U
      mod.io.writeLen    := 8.U
      when(mod.io.writeValid) {
        mod.io.mode      := 0.U
        BlockMemoryCP    := 1.U
      }
    }
    is(1.U) {
      mod.io.mode        := 2.U
      mod.io.writeAddr   := 8.U
      mod.io.writeData   := "h100F0E0D0C0B0A09".U
      mod.io.writeOffset := 0.U
      mod.io.writeLen    := 8.U
      when(mod.io.writeValid) {
        mod.io.mode      := 0.U
        BlockMemoryCP    := 2.U
      }
    }
    is(2.U) {
      mod.io.mode       := 1.U
      mod.io.readAddr   := 0.U
      mod.io.readOffset := 0.U
      mod.io.readLen    := 8.U
      when(mod.io.readValid) {
          r_res           := mod.io.readData
          mod.io.mode      := 0.U
          BlockMemoryCP   := 3.U
      }
    }
    is(3.U) {
      mod.io.mode       := 1.U
      mod.io.readAddr   := 8.U
      mod.io.readOffset := 0.U
      mod.io.readLen    := 8.U
      when(mod.io.readValid) {
          r_res           := mod.io.readData
          mod.io.mode      := 0.U
          BlockMemoryCP   := 4.U
      }
    }
    is(4.U) {
      mod.io.mode         := 3.U
      mod.io.dmaDstAddr   := 16.U
      mod.io.dmaSrcAddr   := 0.U
      mod.io.dmaDstLen    := 8.U
      mod.io.dmaSrcLen    := 8.U
      mod.io.dmaDstOffset := 0.U
      when(mod.io.dmaValid) {
          mod.io.mode      := 0.U
          BlockMemoryCP   := 5.U
      }
    }
    is(5.U) {
      mod.io.mode       := 1.U
      mod.io.readAddr   := 16.U
      mod.io.readOffset := 0.U
      mod.io.readLen    := 8.U
      when(mod.io.readValid) {
          r_res           := mod.io.readData
          mod.io.mode      := 0.U
          BlockMemoryCP   := 6.U
      }
    }
    is(6.U) {
      printf("result:%d\n", r_res)
    }
    */
  }

    io.M_AXI_AWID               := mod.io.M_AXI_AWID
    io.M_AXI_AWADDR             := mod.io.M_AXI_AWADDR
    io.M_AXI_AWLEN              := mod.io.M_AXI_AWLEN
    io.M_AXI_AWSIZE             := mod.io.M_AXI_AWSIZE
    io.M_AXI_AWBURST            := mod.io.M_AXI_AWBURST
    io.M_AXI_AWLOCK             := mod.io.M_AXI_AWLOCK
    io.M_AXI_AWCACHE            := mod.io.M_AXI_AWCACHE
    io.M_AXI_AWPROT             := mod.io.M_AXI_AWPROT
    io.M_AXI_AWQOS              := mod.io.M_AXI_AWQOS
    io.M_AXI_AWUSER             := mod.io.M_AXI_AWUSER
    io.M_AXI_AWVALID            := mod.io.M_AXI_AWVALID
    mod.io.M_AXI_AWREADY := io.M_AXI_AWREADY

    io.M_AXI_WDATA             := mod.io.M_AXI_WDATA
    io.M_AXI_WSTRB             := mod.io.M_AXI_WSTRB
    io.M_AXI_WLAST             := mod.io.M_AXI_WLAST
    io.M_AXI_WUSER             := mod.io.M_AXI_WUSER
    io.M_AXI_WVALID            := mod.io.M_AXI_WVALID
    mod.io.M_AXI_WREADY := io.M_AXI_WREADY

    mod.io.M_AXI_BID    := io.M_AXI_BID
    mod.io.M_AXI_BRESP  := io.M_AXI_BRESP
    mod.io.M_AXI_BUSER  := io.M_AXI_BUSER
    mod.io.M_AXI_BVALID := io.M_AXI_BVALID
    io.M_AXI_BREADY            := mod.io.M_AXI_BREADY

    io.M_AXI_ARID               := mod.io.M_AXI_ARID
    io.M_AXI_ARADDR             := mod.io.M_AXI_ARADDR
    io.M_AXI_ARLEN              := mod.io.M_AXI_ARLEN
    io.M_AXI_ARSIZE             := mod.io.M_AXI_ARSIZE
    io.M_AXI_ARBURST            := mod.io.M_AXI_ARBURST
    io.M_AXI_ARLOCK             := mod.io.M_AXI_ARLOCK
    io.M_AXI_ARCACHE            := mod.io.M_AXI_ARCACHE
    io.M_AXI_ARPROT             := mod.io.M_AXI_ARPROT
    io.M_AXI_ARQOS              := mod.io.M_AXI_ARQOS
    io.M_AXI_ARUSER             := mod.io.M_AXI_ARUSER
    io.M_AXI_ARVALID            := mod.io.M_AXI_ARVALID
    mod.io.M_AXI_ARREADY := io.M_AXI_ARREADY

    mod.io.M_AXI_RID    := io.M_AXI_RID
    mod.io.M_AXI_RDATA  := io.M_AXI_RDATA
    mod.io.M_AXI_RRESP  := io.M_AXI_RRESP
    mod.io.M_AXI_RLAST  := io.M_AXI_RLAST
    mod.io.M_AXI_RUSER  := io.M_AXI_RUSER
    mod.io.M_AXI_RVALID := io.M_AXI_RVALID
    io.M_AXI_RREADY            := mod.io.M_AXI_RREADY
}
*/