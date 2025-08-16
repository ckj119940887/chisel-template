package Router

import chisel3._
import chisel3.util._
import chisel3.experimental._

class Test(addrWidth: Int, dataWidth: Int, cpWidth: Int, idWidth: Int) extends Module{
  val io = IO(new Bundle{
    val req      = Valid(new RequestBundle(addrWidth, dataWidth))
    val resp     = Flipped(Valid(new ResponseBundle(dataWidth)))
    val routeIn  = Flipped(Valid(new Packet(idWidth, dataWidth, cpWidth)))
    val routeOut = Valid(new Packet(idWidth, dataWidth, cpWidth))
  })

  val r_req            = Reg(new RequestBundle(addrWidth, dataWidth))
  val r_req_valid      = RegInit(false.B)
  val r_resp           = Reg(new ResponseBundle(dataWidth))
  val r_resp_valid     = RegInit(false.B)
  val testCP           = RegInit(0.U(4.W))
  val r_routeIn        = Reg(new Packet(idWidth, dataWidth, cpWidth))
  val r_routeIn_valid  = RegInit(false.B)
  val r_routeOut       = Reg(new Packet(idWidth, dataWidth, cpWidth))
  val r_routeOut_valid = RegInit(false.B)

  val r_srcID      = RegInit(2.U(idWidth.W))
  val r_srcCP      = RegInit(0.U(cpWidth.W))
  val r_srcResAddr = Reg(UInt(addrWidth.W))
  val r_res        = Reg(UInt(dataWidth.W))

  r_resp       := io.resp.bits
  r_resp_valid := io.resp.valid
  io.req.bits  := r_req
  io.req.valid := r_req_valid

  r_routeIn           := io.routeIn.bits
  r_routeIn_valid     := io.routeIn.valid
  io.routeOut.bits  := r_routeOut
  io.routeOut.valid := r_routeOut_valid

  switch(testCP) {
    is(0.U) {
        r_routeOut_valid := false.B
        when(r_start) {

        } .elsewhen(r_routeIn_valid) {
            r_srcCP := r_routeIn.srcCP
            r_srcID := r_routeIn.srcID
            testCP  := r_routeIn.dstCP
        }
    }
    is(1.U) {
        r_req_valid := true.B
        r_req.addr  := 0.U
        r_req.write := true.B
        r_req.data  := 1.U

        when(r_resp_valid) {
            r_req_valid := false.B
            testCP      := 2.U
        }
    }
    is(2.U) {
        r_req_valid := true.B
        r_req.addr  := 4.U
        r_req.write := true.B
        r_req.data  := 2.U

        when(r_resp_valid) {
            r_req_valid := false.B
            testCP      := 3.U
        }
    }
    is(3.U) {
        r_req_valid := true.B
        r_req.addr  := 8.U
        r_req.write := true.B
        r_req.data  := 3.U

        when(r_resp_valid) {
            r_req_valid := false.B
            testCP      := 4.U
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
        r_req_valid := true.B
        r_req.addr  := 12.U
        r_req.write := false.B

        when(r_resp_valid) {
            r_res       := r_resp.data
            r_req_valid := false.B
            testCP      := 7.U
        }
    }
    is(7.U) {
        r_req_valid := true.B
        r_req.addr  := 16.U
        r_req.write := true.B
        r_req.data  := r_res

        when(r_resp_valid) {
            r_req_valid := false.B
            testCP      := 8.U
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