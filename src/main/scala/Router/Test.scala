package Router

import chisel3._
import chisel3.util._
import chisel3.experimental._

class Test(addrWidth: Int, dataWidth: Int, cpWidth: Int, idWidth: Int, depth: Int) extends Module{
  val io = IO(new Bundle{
    val arbMem_req  = Valid(new BlockMemoryRequestBundle(addrWidth, dataWidth, depth))
    val arbMem_resp = Flipped(Valid(new BlockMemoryResponseBundle(dataWidth)))
    val routeIn     = Flipped(Valid(new Packet(idWidth, dataWidth, cpWidth)))
    val routeOut    = Valid(new Packet(idWidth, dataWidth, cpWidth))
  })

  val r_arbMem_req            = Reg(new BlockMemoryRequestBundle(addrWidth, dataWidth, depth))
  val r_arbMem_req_valid      = RegInit(false.B)
  val r_arbMem_resp           = Reg(new BlockMemoryResponseBundle(dataWidth))
  val r_arbMem_resp_valid     = RegInit(false.B)

  val testCP           = RegInit(0.U(4.W))
  val r_routeIn        = RegInit(0.U.asTypeOf(new Packet(idWidth, dataWidth, cpWidth)))
  val r_routeIn_valid  = RegInit(false.B)
  val r_routeOut       = Reg(new Packet(idWidth, dataWidth, cpWidth))
  val r_routeOut_valid = RegInit(false.B)

  val r_srcID      = RegInit(2.U(idWidth.W))
  val r_srcCP      = RegInit(0.U(cpWidth.W))
  val r_srcResAddr = Reg(UInt(addrWidth.W))
  val r_res        = Reg(UInt(dataWidth.W))

  r_arbMem_resp       := io.arbMem_resp.bits
  r_arbMem_resp_valid := io.arbMem_resp.valid
  io.arbMem_req.bits  := r_arbMem_req
  io.arbMem_req.valid := r_arbMem_req_valid

  r_routeIn           := io.routeIn.bits
  r_routeIn_valid     := io.routeIn.valid
  io.routeOut.bits  := r_routeOut
  io.routeOut.valid := r_routeOut_valid

  switch(testCP) {
    is(0.U) {
        r_routeOut_valid := false.B
        when(r_routeIn_valid) {
            r_srcCP := r_routeIn.srcCP
            r_srcID := r_routeIn.srcID
            testCP  := r_routeIn.dstCP
        }
    }
    is(1.U) {
        r_arbMem_req_valid     := true.B
        r_arbMem_req.writeAddr := 0.U
        r_arbMem_req.writeData := 1.U
        r_arbMem_req.mode      := 2.U

        when(r_arbMem_resp_valid) {
            r_arbMem_req_valid := false.B
            r_arbMem_req.mode  := 0.U
            testCP             := 2.U
        }
    }
    is(2.U) {
        r_arbMem_req_valid     := true.B
        r_arbMem_req.writeAddr := 8.U
        r_arbMem_req.writeData := 2.U
        r_arbMem_req.mode      := 2.U

        when(r_arbMem_resp_valid) {
            r_arbMem_req_valid := false.B
            r_arbMem_req.mode  := 0.U
            testCP             := 3.U
        }
    }
    is(3.U) {
        r_arbMem_req_valid     := true.B
        r_arbMem_req.writeAddr := 16.U
        r_arbMem_req.writeData := 3.U
        r_arbMem_req.mode      := 2.U

        when(r_arbMem_resp_valid) {
            r_arbMem_req_valid := false.B
            r_arbMem_req.mode  := 0.U
            testCP             := 4.U
        }
    }
    is(4.U) {
        // call add
        r_routeOut.srcID := 2.U
        r_routeOut.srcCP := 6.U
        r_routeOut.dstID := 1.U
        r_routeOut.dstCP := 1.U
        r_routeOut_valid := true.B
        testCP           := 5.U
    }
    is(5.U) {
        r_routeOut_valid := false.B
        when(r_routeIn_valid) {
            testCP  := r_routeIn.dstCP
        }
    }
    is(6.U) {
        r_arbMem_req_valid     := true.B
        r_arbMem_req.readAddr  := 24.U
        r_arbMem_req.mode      := 1.U

        when(r_arbMem_resp_valid) {
            r_res              := r_arbMem_resp.data
            r_arbMem_req.mode  := 0.U
            r_arbMem_req_valid := false.B
            testCP             := 7.U
        }
    }
    is(7.U) {
        r_arbMem_req_valid     := true.B
        r_arbMem_req.writeAddr := 32.U
        r_arbMem_req.writeData := r_res
        r_arbMem_req.mode      := 2.U

        when(r_arbMem_resp_valid) {
            r_arbMem_req_valid := false.B
            r_arbMem_req.mode  := 0.U
            testCP             := 8.U
        }
    }
    is(8.U) {
        // return to Top
        r_routeOut.srcID := 2.U
        r_routeOut.srcCP := 0.U
        r_routeOut.dstID := r_srcID
        r_routeOut.dstCP := r_srcCP
        r_routeOut_valid := true.B
        testCP           := 0.U
    }
  }
}