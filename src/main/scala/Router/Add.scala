package Router

import chisel3._
import chisel3.util._
import chisel3.experimental._

class Add(addrWidth: Int, dataWidth: Int, cpWidth: Int, idWidth: Int) extends Module{
  val io = IO(new Bundle{
    val req   = Valid(new RequestBundle(addrWidth, dataWidth))
    val resp  = Flipped(Valid(new ResponseBundle(dataWidth)))
    val routeIn  = Flipped(Valid(new Packet(idWidth, dataWidth, cpWidth)))
    val routeOut = Valid(new Packet(idWidth, dataWidth, cpWidth))
  })

  val r_req            = Reg(new RequestBundle(addrWidth, dataWidth))
  val r_req_valid      = RegInit(false.B)
  val r_resp           = Reg(new ResponseBundle(dataWidth))
  val r_resp_valid     = RegInit(false.B)
  val addCP            = RegInit(0.U(4.W))
  val r_routeIn        = Reg(new Packet(idWidth, dataWidth, cpWidth))
  val r_routeIn_valid  = RegInit(false.B)
  val r_routeOut       = Reg(new Packet(idWidth, dataWidth, cpWidth))
  val r_routeOut_valid = RegInit(false.B)

  // initialize r_srcID to the ID of current IP
  val r_srcID          = RegInit(1.U(idWidth.W))
  val r_srcCP          = RegInit(0.U(cpWidth.W))
  val r_srcResAddr     = Reg(UInt(addrWidth.W))
  val r_paraX = Reg(UInt(dataWidth.W))
  val r_paraY = Reg(UInt(dataWidth.W))
  val r_paraZ = Reg(UInt(dataWidth.W))
  val r_res   = Reg(UInt(dataWidth.W))

  r_resp       := io.resp.bits
  r_resp_valid := io.resp.valid
  io.req.bits  := r_req
  io.req.valid := r_req_valid

  r_routeIn           := io.routeIn.bits
  r_routeIn_valid     := io.routeIn.valid
  io.routeOut.bits  := r_routeOut
  io.routeOut.valid := r_routeOut_valid

  switch(addCP) {
    is(0.U) {
        r_routeOut_valid := false.B
        when(r_routeIn_valid) {
            r_srcCP := r_routeIn.srcCP
            r_srcID := r_routeIn.srcID
            addCP   := r_routeIn.dstCP
        }
    }
    is(1.U) {
        r_req_valid := true.B
        r_req.addr  := 0.U
        r_req.write := false.B

        when(r_resp_valid) {
            r_paraX     := r_resp.data
            r_req_valid := false.B
            addCP       := 2.U
        }
    }
    is(2.U) {
        r_req_valid := true.B
        r_req.addr  := 4.U
        r_req.write := false.B

        when(r_resp_valid) {
            r_paraY     := r_resp.data
            r_req_valid := false.B
            addCP       := 3.U
        }
    }
    is(3.U) {
        r_req_valid := true.B
        r_req.addr  := 8.U
        r_req.write := false.B

        when(r_resp_valid) {
            r_paraZ     := r_resp.data
            r_req_valid := false.B
            addCP       := 4.U
        }
    }
    is(4.U) {
        r_res := r_paraX + r_paraY
        addCP := 5.U
    }
    is(5.U) {
        r_res := r_res + r_paraZ
        addCP := 6.U
    }
    is(6.U) {
        r_req_valid := true.B
        r_req.addr  := 12.U
        r_req.write := true.B
        r_req.data  := r_res

        when(r_resp_valid) {
            r_req_valid := false.B
            addCP       := 7.U
        }
    }
    is(7.U) {
        // call add
        r_routeOut.srcID := 1.U
        r_routeOut.srcCP := 0.U
        r_routeOut.dstID := r_srcID
        r_routeOut.dstCP := r_srcCP
        r_routeOut_valid := true.B
        addCP            := 0.U
    }
  }
}