package Router

import chisel3._
import chisel3.util._
import chisel3.experimental._

class Add(addrWidth: Int, dataWidth: Int, cpWidth: Int, idWidth: Int, depth: Int) extends Module{
  val io = IO(new Bundle{
    val arbMem_req    = Valid(new BlockMemoryRequestBundle(addrWidth, dataWidth, depth))
    val arbMem_resp   = Flipped(Valid(new BlockMemoryResponseBundle(dataWidth)))
    val arbAdder_req  = Valid(new AdderRequestBundle(dataWidth))
    val arbAdder_resp = Flipped(Valid(new AdderResponseBundle(dataWidth)))
    val routeIn       = Flipped(Valid(new Packet(idWidth, dataWidth, cpWidth)))
    val routeOut      = Valid(new Packet(idWidth, dataWidth, cpWidth))
  })

  val r_arbMem_req            = Reg(new BlockMemoryRequestBundle(addrWidth, dataWidth, depth))
  val r_arbMem_req_valid      = RegInit(false.B)
  val r_arbMem_resp           = Reg(new BlockMemoryResponseBundle(dataWidth))
  val r_arbMem_resp_valid     = RegInit(false.B)

  r_arbMem_resp       := io.arbMem_resp.bits
  r_arbMem_resp_valid := io.arbMem_resp.valid
  io.arbMem_req.bits  := r_arbMem_req
  io.arbMem_req.valid := r_arbMem_req_valid

  val r_arbAdder_req          = Reg(new AdderRequestBundle(dataWidth))
  val r_arbAdder_req_valid    = RegInit(false.B)
  val r_arbAdder_resp         = Reg(new AdderResponseBundle(dataWidth))
  val r_arbAdder_resp_valid   = RegInit(false.B)
  r_arbAdder_resp       := io.arbAdder_resp.bits
  r_arbAdder_resp_valid := io.arbAdder_resp.valid
  io.arbAdder_req.bits  := r_arbAdder_req
  io.arbAdder_req.valid := r_arbAdder_req_valid

  val addCP            = RegInit(0.U(4.W))

  val r_routeIn        = Reg(new Packet(idWidth, dataWidth, cpWidth))
  val r_routeIn_valid  = RegInit(false.B)
  val r_routeOut       = Reg(new Packet(idWidth, dataWidth, cpWidth))
  val r_routeOut_valid = RegInit(false.B)

  r_routeIn            := io.routeIn.bits
  r_routeIn_valid      := io.routeIn.valid
  io.routeOut.bits     := r_routeOut
  io.routeOut.valid    := r_routeOut_valid

  // initialize r_srcID to the ID of current IP
  val r_srcID          = RegInit(1.U(idWidth.W))
  val r_srcCP          = RegInit(0.U(cpWidth.W))
  val r_srcResAddr     = Reg(UInt(addrWidth.W))
  val r_paraX = Reg(UInt(dataWidth.W))
  val r_paraY = Reg(UInt(dataWidth.W))
  val r_paraZ = Reg(UInt(dataWidth.W))
  val r_res   = Reg(UInt(dataWidth.W))

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
        r_arbMem_req_valid     := true.B
        r_arbMem_req.readAddr  := 0.U
        r_arbMem_req.mode      := 1.U

        when(r_arbMem_resp_valid) {
            r_paraX            := r_arbMem_resp.data
            r_arbMem_req_valid := false.B
            r_arbMem_req.mode  := 0.U
            addCP              := 2.U
        }
    }
    is(2.U) {
        r_arbMem_req_valid     := true.B
        r_arbMem_req.readAddr  := 8.U
        r_arbMem_req.mode      := 1.U

        when(r_arbMem_resp_valid) {
            r_paraY            := r_arbMem_resp.data
            r_arbMem_req_valid := false.B
            r_arbMem_req.mode  := 0.U
            addCP              := 3.U
        }
    }
    is(3.U) {
        r_arbMem_req_valid     := true.B
        r_arbMem_req.readAddr  := 16.U
        r_arbMem_req.mode      := 1.U

        when(r_arbMem_resp_valid) {
            r_paraZ            := r_arbMem_resp.data
            r_arbMem_req_valid := false.B
            r_arbMem_req.mode  := 0.U
            addCP              := 4.U
        }
    }
    is(4.U) {
        r_arbAdder_req_valid := true.B
        r_arbAdder_req.a     := r_paraX
        r_arbAdder_req.b     := r_paraY
        when(r_arbAdder_resp_valid) {
            r_res                := r_arbAdder_resp.out
            r_arbAdder_req_valid := false.B
            addCP := 5.U
        }
        //r_res := r_paraX + r_paraY
        //addCP := 5.U
    }
    is(5.U) {
        r_arbAdder_req_valid := true.B
        r_arbAdder_req.a     := r_res
        r_arbAdder_req.b     := r_paraZ
        when(r_arbAdder_resp_valid) {
            r_res                := r_arbAdder_resp.out
            r_arbAdder_req_valid := false.B
            addCP := 6.U
        }
        // r_res := r_res + r_paraZ
        // addCP := 6.U
    }
    is(6.U) {
        r_arbMem_req_valid     := true.B
        r_arbMem_req.writeAddr := 24.U
        r_arbMem_req.writeData := r_res
        r_arbMem_req.mode      := 2.U

        when(r_arbMem_resp_valid) {
            r_arbMem_req_valid := false.B
            r_arbMem_req.mode  := 0.U
            addCP              := 7.U
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