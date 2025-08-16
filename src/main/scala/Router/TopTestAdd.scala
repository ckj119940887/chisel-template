package Router

import chisel3._
import chisel3.util._
import chisel3.experimental._

class TopTestAdd(val numIPs: Int, val depth: Int, val addrWidth: Int, val dataWidth: Int, cpWidth: Int, idWidth: Int) extends Module{
    val io = IO(new Bundle{
      val start = Input(Bool())
      val valid = Output(Bool())
    })

    val mem = Module(new SimpleMemoryWrapper(depth = depth, addrWidth = addrWidth, dataWidth = dataWidth))
    val arb = Module(new MemoryArbiterModule(numIPs = numIPs, addrWidth = addrWidth, dataWidth = dataWidth))
    val router = Module(new Router(nPorts = 3, idWidth = 2, dataWidth, cpWidth = 5))

    mem.io.req.bits.addr  := arb.io.memory.req.bits.addr 
    mem.io.req.bits.data  := arb.io.memory.req.bits.data 
    mem.io.req.bits.write := arb.io.memory.req.bits.write 
    mem.io.req.valid      := arb.io.memory.req.valid

    arb.io.memory.resp.bits.data := mem.io.resp.bits.data
    arb.io.memory.resp.valid     := mem.io.resp.valid

    // for(i <- 0 until numIPs) {
    //   arb.io.ipReqs(i).bits.addr  := io.ipReqs(i).bits.addr
    //   arb.io.ipReqs(i).bits.data  := io.ipReqs(i).bits.data
    //   arb.io.ipReqs(i).bits.write := io.ipReqs(i).bits.write
    //   arb.io.ipReqs(i).valid      := io.ipReqs(i).valid

    //   io.ipResps(i).bits.data := arb.io.ipResps(i).bits.data
    //   io.ipResps(i).valid     := arb.io.ipResps(i).valid
    // }

    val modAdd         = Module(new Add(addrWidth, dataWidth, cpWidth, idWidth))
    val modTest         = Module(new Test(addrWidth, dataWidth, cpWidth, idWidth))

    arb.io.ipReqs(0) := modAdd.io.req
    arb.io.ipReqs(1) := modTest.io.req
    modAdd.io.resp   := arb.io.ipResps(0)
    modTest.io.resp  := arb.io.ipResps(1)

    router.io.in(1)    := modAdd.io.routeOut
    router.io.in(2)    := modTest.io.routeOut
    modAdd.io.routeIn  := router.io.out(1)
    modTest.io.routeIn := router.io.out(2)

    val r_start      = RegNext(io.start)
    val r_valid      = RegInit(false.B)
    val r_req        = Reg(new RequestBundle(addrWidth, dataWidth))
    val r_req_valid  = RegInit(false.B)
    val r_resp       = Reg(new ResponseBundle(dataWidth))
    val r_resp_valid = RegInit(false.B)
    val r_res        = Reg(UInt(dataWidth.W))
    val TopTestAddCP = RegInit(0.U(4.W))
    val r_routeIn        = Reg(new Packet(2, dataWidth, 5))
    val r_routeIn_valid  = RegInit(false.B)
    val r_routeOut       = Reg(new Packet(2, dataWidth, 5))
    val r_routeOut_valid = RegInit(false.B)

    arb.io.ipReqs(2).bits  := r_req
    arb.io.ipReqs(2).valid := r_req_valid
    r_resp                 := arb.io.ipResps(2).bits
    r_resp_valid           := arb.io.ipResps(2).valid

    router.io.in(0).bits  := r_routeOut
    router.io.in(0).valid := r_routeOut_valid
    r_routeIn             := router.io.out(0).bits
    r_routeIn_valid       := router.io.out(0).valid

    io.valid               := r_valid

    switch(TopTestAddCP) {
      is(0.U) {
        r_valid          := false.B
        r_routeOut_valid := false.B
        when(r_start) {
          TopTestAddCP := 1.U
        } .elsewhen(r_routeIn_valid) {
          TopTestAddCP := r_routeIn.dstCP
        }
      }
      is(1.U) {
        // call test
        r_routeOut.srcID := 0.U
        r_routeOut.srcCP := 3.U
        r_routeOut.dstID := 2.U
        r_routeOut.dstCP := 1.U
        r_routeOut_valid := true.B
        TopTestAddCP     := 2.U
      }
      is(2.U) {
        r_routeOut_valid := false.B
        when(r_routeIn_valid) {
          TopTestAddCP := r_routeIn.dstCP
        }
      }
      is(3.U) {
        r_req_valid := true.B
        r_req.addr  := 16.U
        r_req.write := false.B

        when(r_resp_valid) {
            r_res        := r_resp.data
            printf("%d\n", r_res)
            r_req_valid  := false.B
            TopTestAddCP := 4.U
        }
      }
      is(4.U) {
        r_valid      := true.B
        TopTestAddCP := 0.U
      }
    }
}