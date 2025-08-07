package Router

import chisel3._
import chisel3.util._
import chisel3.experimental._

class TopTestAdd(val numIPs: Int, val depth: Int, val addrWidth: Int, val dataWidth: Int) extends Module{
    val io = IO(new Bundle{
      // val ipReqs = Flipped(Vec(numIPs, Valid(new RequestBundle(addrWidth, dataWidth))))
      // val ipResps = Vec(numIPs, Valid(new ResponseBundle(dataWidth)))
      val start = Input(Bool())
      val valid = Output(Bool())
    })

    val mem = Module(new SimpleMemoryWrapper(depth = depth, addrWidth = addrWidth, dataWidth = dataWidth))
    val arb = Module(new MemoryArbiterModule(numIPs = numIPs, addrWidth = addrWidth, dataWidth = dataWidth))

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

    val modAdd         = Module(new Add(addrWidth, dataWidth))
    val r_modAdd_valid = RegNext(modAdd.io.valid)
    val r_modAdd_start = RegInit(false.B)
    modAdd.io.start    := r_modAdd_start

    val modTest         = Module(new Test(addrWidth, dataWidth))
    val r_modTest_valid = RegNext(modTest.io.valid)
    val r_modTest_start = RegInit(false.B)
    modTest.io.start    := r_modTest_start

    arb.io.ipReqs(0) := modAdd.io.req
    arb.io.ipReqs(1) := modTest.io.req
    modAdd.io.resp   := arb.io.ipResps(0)
    modTest.io.resp  := arb.io.ipResps(1)

    val r_start      = RegNext(io.start)
    val r_valid      = RegInit(false.B)
    val r_req        = Reg(new RequestBundle(addrWidth, dataWidth))
    val r_req_valid  = RegInit(false.B)
    val r_resp       = Reg(new ResponseBundle(dataWidth))
    val r_resp_valid = RegInit(false.B)
    val r_res        = Reg(UInt(dataWidth.W))
    val TopTestAddCP = RegInit(0.U(4.W))

    arb.io.ipReqs(2).bits  := r_req
    arb.io.ipReqs(2).valid := r_req_valid
    r_resp                 := arb.io.ipResps(2).bits
    r_resp_valid           := arb.io.ipResps(2).valid
    io.valid               := r_valid

    switch(TopTestAddCP) {
      is(0.U) {
        r_valid      := false.B
        TopTestAddCP := Mux(r_start, 1.U, 0.U)
      }
      is(1.U) {
        r_modTest_start := true.B
        TopTestAddCP    := 2.U
      }
      is(2.U) {
        r_modTest_start := false.B
        when(r_modTest_valid) {
          r_modAdd_start  := true.B
          TopTestAddCP    := 3.U
        }
      }
      is(3.U) {
        r_modAdd_start  := false.B
        when(r_modAdd_valid) {
          r_modTest_start := true.B
          TopTestAddCP    := 4.U
        }
      }
      is(4.U) {
        r_modTest_start := false.B
        when(r_modTest_valid) {
          TopTestAddCP    := 5.U
        }
      }
      is(5.U) {
        r_req_valid := true.B
        r_req.addr  := 16.U
        r_req.write := false.B

        when(r_resp_valid) {
            r_res        := r_resp.data
            printf("%d\n", r_res)
            r_req_valid  := false.B
            TopTestAddCP := 6.U
        }
      }
      is(6.U) {
        r_valid      := true.B
        TopTestAddCP := 0.U
      }
    }
}