package Router

import chisel3._
import chisel3.util._
import chisel3.experimental._

class TopOddEven(val numIPs: Int, val depth: Int, val addrWidth: Int, val dataWidth: Int) extends Module{
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

    val modEven         = Module(new Even(addrWidth = addrWidth, dataWidth = dataWidth, stackDepth = 256, cpWidth = 5, idWidth = 2))
    val r_modEven_valid = RegNext(modEven.io.valid)
    val r_modEven_start = RegInit(false.B)
    modEven.io.start    := r_modEven_start

    val modOdd         = Module(new Odd(addrWidth = addrWidth, dataWidth = dataWidth, stackDepth = 256, cpWidth = 5, idWidth = 2))
    val r_modOdd_valid = RegNext(modOdd.io.valid)
    val r_modOdd_start = RegInit(false.B)
    modOdd.io.start    := r_modOdd_start

    arb.io.ipReqs(0) := modEven.io.req
    arb.io.ipReqs(1) := modOdd.io.req
    modEven.io.resp  := arb.io.ipResps(0)
    modOdd.io.resp   := arb.io.ipResps(1)

    router.io.in(1)    := modEven.io.routeOut
    router.io.in(2)    := modOdd.io.routeOut
    modEven.io.routeIn := router.io.out(1)
    modOdd.io.routeIn  := router.io.out(2)

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
    val TopOddEvenCP     = RegInit(0.U(4.W))

    router.io.in(0).bits  := r_routeIn
    router.io.in(0).valid := r_routeIn_valid
    r_routeOut            := router.io.out(0).bits
    r_routeOut_valid      := router.io.out(0).valid

    arb.io.ipReqs(2).bits  := r_req
    arb.io.ipReqs(2).valid := r_req_valid
    r_resp                 := arb.io.ipResps(2).bits
    r_resp_valid           := arb.io.ipResps(2).valid
    io.valid               := r_valid

    switch(TopOddEvenCP) {
      is(0.U) {
        r_valid      := false.B
        TopOddEvenCP := Mux(r_start, 1.U, 0.U)
      }
      is(1.U) {
        r_req_valid := true.B
        r_req.addr  := 0.U
        r_req.write := true.B
        r_req.data  := 40.U

        when(r_resp_valid) {
            r_req_valid  := false.B
            TopOddEvenCP := 2.U
        }
      }
      is(2.U) {
        r_req_valid := true.B
        r_req.addr  := 4.U
        r_req.write := true.B
        r_req.data  := 3.U

        when(r_resp_valid) {
            r_req_valid  := false.B
            TopOddEvenCP := 3.U
        }
      }
      is(3.U) {
        r_modEven_start := true.B
        TopOddEvenCP := 4.U
      }
      is(4.U) {
        r_modEven_start := false.B
        when(r_modEven_valid) {
            TopOddEvenCP := 5.U
        }
      }
      is(5.U) {
        r_req_valid := true.B
        r_req.addr  := 40.U
        r_req.write := false.B

        when(r_resp_valid) {
            r_res        := r_resp.data
            printf("final result: %d\n", r_res)
            r_req_valid  := false.B
            TopOddEvenCP := 6.U
        }
      }
      is(6.U) {
        r_valid      := true.B
        TopOddEvenCP := 0.U
      }
    }
}