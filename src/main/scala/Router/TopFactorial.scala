package Router

import chisel3._
import chisel3.util._
import chisel3.experimental._

class TopFactorial(val numIPs: Int, val depth: Int, val addrWidth: Int, val dataWidth: Int) extends Module{
    val io = IO(new Bundle{
      val start = Input(Bool())
      val valid = Output(Bool())
    })

    val mem = Module(new SimpleMemoryWrapper(depth = depth, addrWidth = addrWidth, dataWidth = dataWidth))
    val arb = Module(new MemoryArbiterModule(numIPs = numIPs, addrWidth = addrWidth, dataWidth = dataWidth))
    val router = Module(new Router(nPorts = 2, idWidth = 2, dataWidth, cpWidth = 5))

    mem.io.req.bits.addr  := arb.io.memory.req.bits.addr 
    mem.io.req.bits.data  := arb.io.memory.req.bits.data 
    mem.io.req.bits.write := arb.io.memory.req.bits.write 
    mem.io.req.valid      := arb.io.memory.req.valid

    arb.io.memory.resp.bits.data := mem.io.resp.bits.data
    arb.io.memory.resp.valid     := mem.io.resp.valid

    val modFactorial         = Module(new Factorial(addrWidth = addrWidth, dataWidth = dataWidth, stackDepth = 256, cpWidth = 5, idWidth = 2))

    arb.io.ipReqs(1)      := modFactorial.io.req
    modFactorial.io.resp  := arb.io.ipResps(1)

    router.io.in(1)         := modFactorial.io.routeOut
    modFactorial.io.routeIn := router.io.out(1)

    val r_start          = RegNext(io.start)
    val r_valid          = RegInit(false.B)
    val r_req            = Reg(new RequestBundle(addrWidth, dataWidth))
    val r_req_valid      = RegInit(false.B)
    val r_resp           = Reg(new ResponseBundle(dataWidth))
    val r_resp_valid     = RegInit(false.B)
    val r_res            = Reg(UInt(dataWidth.W))
    val r_routeIn        = Reg(new Packet(2, dataWidth, 5))
    val r_routeIn_valid  = RegInit(false.B)
    val r_routeOut       = Reg(new Packet(2, dataWidth, 5))
    val r_routeOut_valid = RegInit(false.B)
    val TopFactorialCP   = RegInit(0.U(4.W))

    router.io.in(0).bits  := r_routeOut
    router.io.in(0).valid := r_routeOut_valid
    r_routeIn             := router.io.out(0).bits
    r_routeIn_valid       := router.io.out(0).valid

    arb.io.ipReqs(0).bits  := r_req
    arb.io.ipReqs(0).valid := r_req_valid
    r_resp                 := arb.io.ipResps(0).bits
    r_resp_valid           := arb.io.ipResps(0).valid
    io.valid               := r_valid

    switch(TopFactorialCP) {
      is(0.U) {
        r_valid        := false.B
        TopFactorialCP := Mux(r_start, 1.U, 0.U)
      }
      is(1.U) {
        r_req_valid := true.B
        r_req.addr  := 0.U
        r_req.write := true.B
        r_req.data  := 40.U

        when(r_resp_valid) {
            r_req_valid    := false.B
            TopFactorialCP := 2.U
        }
      }
      is(2.U) {
        r_req_valid := true.B
        r_req.addr  := 4.U
        r_req.write := true.B
        r_req.data  := 4.U

        when(r_resp_valid) {
            r_req_valid    := false.B
            TopFactorialCP := 3.U
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
        r_req_valid := true.B
        r_req.addr  := 40.U
        r_req.write := false.B

        when(r_resp_valid) {
            r_res          := r_resp.data
            printf("final result: %d\n", r_res)
            r_req_valid    := false.B
            TopFactorialCP := 6.U
        }
      }
      is(6.U) {
        r_valid        := true.B
        TopFactorialCP := 0.U
      }
    }
}