package Router

import chisel3._
import chisel3.util._
import chisel3.experimental._

class TopFactorial(val numIPs: Int, val depth: Int, val addrWidth: Int, val dataWidth: Int) extends Module{
    val io = IO(new Bundle{
      // write address channel
      val S_AXI_AWADDR  = Input(UInt(addrWidth.W))
      val S_AXI_AWPROT  = Input(UInt(3.W))
      val S_AXI_AWVALID = Input(Bool())
      val S_AXI_AWREADY = Output(Bool())

      // write data channel
      val S_AXI_WDATA  = Input(UInt(dataWidth.W))
      val S_AXI_WSTRB  = Input(UInt((dataWidth/8).W))
      val S_AXI_WVALID = Input(Bool())
      val S_AXI_WREADY = Output(Bool())

      // write response channel
      val S_AXI_BRESP  = Output(UInt(2.W))
      val S_AXI_BVALID = Output(Bool())
      val S_AXI_BREADY = Input(Bool())

      // read address channel
      val S_AXI_ARADDR  = Input(UInt(addrWidth.W))
      val S_AXI_ARPROT  = Input(UInt(3.W))
      val S_AXI_ARVALID = Input(Bool())
      val S_AXI_ARREADY = Output(Bool())

      // read data channel
      val S_AXI_RDATA  = Output(UInt(dataWidth.W))
      val S_AXI_RRESP  = Output(UInt(2.W))
      val S_AXI_RVALID = Output(Bool())
      val S_AXI_RREADY = Input(Bool())

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

    val r_start       = RegInit(false.B)
    val r_start_next  = RegNext(r_start)
    val r_start_pulse = Reg(Bool())
    val r_valid       = RegInit(false.B)
    r_start_pulse     := r_start & ~r_start_next

    val ADDR_LSB: Int = (dataWidth/ 32) + 1

    // registers for diff channels
    // write address channel
    val r_s_axi_awready = RegInit(true.B)
    val r_s_axi_awaddr  = Reg(UInt(addrWidth.W))

    // write data channel
    val r_s_axi_wready  = RegInit(true.B)
    val r_s_axi_wdata   = Reg(UInt(dataWidth.W))

    // write response channel
    val r_s_axi_bvalid  = RegInit(false.B)

    // read address channel
    val r_s_axi_arready = RegInit(true.B)
    val r_s_axi_araddr  = Reg(UInt(addrWidth.W))

    // read data channel
    val r_s_axi_rvalid  = RegInit(false.B)
    val r_s_axi_rdata   = Reg(UInt(dataWidth.W))

    // registers for valid and ready
    // r_control(0) -- valid
    // r_control(1) -- ready
    // r_control(2) -- DP
    val initControlVals = Seq(0.U(dataWidth.W), 0.U(dataWidth.W))
    val r_control = RegInit(VecInit(initControlVals))
    r_start := r_control(0)(0).asBool
    r_control(1) := r_valid.asUInt

    // write logic
    val r_aw_valid = RegInit(false.B)
    val r_w_valid  = RegInit(false.B)
    when(io.S_AXI_AWVALID & io.S_AXI_AWREADY) {
      r_s_axi_awready           := false.B
      r_s_axi_awaddr            := io.S_AXI_AWADDR(addrWidth - 1, ADDR_LSB)
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
      r_s_axi_araddr            := io.S_AXI_ARADDR(addrWidth - 1, ADDR_LSB)
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

    // val mem = Module(new SimpleMemoryWrapper(depth = depth, addrWidth = addrWidth, dataWidth = dataWidth))
    // val arbMem = Module(new MemoryArbiterModule(numIPs = numIPs, addrWidth = addrWidth, dataWidth = dataWidth))
    val mem           = Module(new BlockMemoryWrapper(depth = depth, addrWidth = addrWidth, dataWidth = dataWidth))
    val arbMem        = Module(new BlockMemoryArbiterModule(numIPs = numIPs, addrWidth = addrWidth, dataWidth = dataWidth, depth = depth))
    val mul           = Module(new MultiplyWrapper(dataWidth = dataWidth))
    val arbMul        = Module(new MultiplyArbiterModule(numIPs = 1, dataWidth = dataWidth))
    val router        = Module(new Router(nPorts = 2, idWidth = 2, dataWidth, cpWidth = 5))
    val modFactorial  = Module(new Factorial(addrWidth = addrWidth, dataWidth = dataWidth, stackDepth = 256, cpWidth = 5, idWidth = 2, depth = depth))

    mem.io.req  <> arbMem.io.ip.req
    mem.io.resp <> arbMem.io.ip.resp

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

    arbMul.io.ip.req  <> mul.io.req
    arbMul.io.ip.resp <> mul.io.resp

    arbMul.io.ipReqs(0)  <> modFactorial.io.arbMul_req
    arbMul.io.ipResps(0) <> modFactorial.io.arbMul_resp

    arbMem.io.ipReqs(1) <> modFactorial.io.arbMem_req
    modFactorial.io.arbMem_resp <> arbMem.io.ipResps(1)

    router.io.in(1) <> modFactorial.io.routeOut
    modFactorial.io.routeIn <> router.io.out(1)

    val r_arbMem_req        = Reg(new BlockMemoryRequestBundle(addrWidth, dataWidth, depth))
    val r_arbMem_req_valid  = RegInit(false.B)
    val r_arbMem_resp       = Reg(new BlockMemoryResponseBundle(dataWidth))
    val r_arbMem_resp_valid = RegInit(false.B)
    val r_res               = Reg(UInt(dataWidth.W))
    val r_routeIn           = Reg(new Packet(2, dataWidth, 5))
    val r_routeIn_valid     = RegInit(false.B)
    val r_routeOut          = Reg(new Packet(2, dataWidth, 5))
    val r_routeOut_valid    = RegInit(false.B)
    val TopFactorialCP      = RegInit(0.U(4.W))

    router.io.in(0).bits  := r_routeOut
    router.io.in(0).valid := r_routeOut_valid
    r_routeIn             := router.io.out(0).bits
    r_routeIn_valid       := router.io.out(0).valid

    arbMem.io.ipReqs(0).bits  := r_arbMem_req
    arbMem.io.ipReqs(0).valid := r_arbMem_req_valid
    r_arbMem_resp             := arbMem.io.ipResps(0).bits
    r_arbMem_resp_valid       := arbMem.io.ipResps(0).valid

    switch(TopFactorialCP) {
      is(0.U) {
        r_valid        := false.B
        TopFactorialCP := Mux(r_start_pulse, 1.U, 0.U)
      }
      is(1.U) {
        r_arbMem_req_valid     := true.B
        r_arbMem_req.writeAddr := 0.U
        r_arbMem_req.writeData := 80.U
        r_arbMem_req.mode      := 2.U

        when(r_arbMem_resp_valid) {
            r_arbMem_req_valid := false.B
            r_arbMem_req.mode  := 0.U
            TopFactorialCP     := 2.U
        }
      }
      is(2.U) {
        r_arbMem_req_valid     := true.B
        r_arbMem_req.writeAddr := 8.U
        r_arbMem_req.writeData := 4.U
        r_arbMem_req.mode      := 2.U

        when(r_arbMem_resp_valid) {
            r_arbMem_req_valid := false.B
            r_arbMem_req.mode  := 0.U
            TopFactorialCP     := 3.U
        }
      }
      is(3.U) {
        r_routeOut.srcID := 0.U
        r_routeOut.srcCP := 5.U
        r_routeOut.dstID := 1.U
        r_routeOut.dstCP := 1.U
        r_routeOut_valid := true.B
        TopFactorialCP   := 4.U
      }
      is(4.U) {
        r_routeOut_valid := false.B
        when(r_routeIn_valid) {
            TopFactorialCP := r_routeIn.dstCP
        }
      }
      is(5.U) {
        r_arbMem_req_valid     := true.B
        r_arbMem_req.readAddr  := 80.U
        r_arbMem_req.mode      := 1.U

        when(r_arbMem_resp_valid) {
            r_res              := r_arbMem_resp.data
            printf("final result: %d\n", r_res)
            r_arbMem_req_valid := false.B
            r_arbMem_req.mode  := 0.U
            TopFactorialCP     := 6.U
        }
      }
      is(6.U) {
        r_valid        := true.B
        TopFactorialCP := 0.U
      }
    }
}