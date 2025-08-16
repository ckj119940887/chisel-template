package Router

import chisel3._
import chisel3.util._
import chisel3.experimental._

class Odd(addrWidth: Int, dataWidth: Int, stackDepth: Int, cpWidth: Int, idWidth: Int) extends Module{
  val io = IO(new Bundle{
    val req      = Valid(new RequestBundle(addrWidth, dataWidth))
    val resp     = Flipped(Valid(new ResponseBundle(dataWidth)))
    val routeIn  = Flipped(Valid(new Packet(idWidth, dataWidth, cpWidth)))
    val routeOut = Valid(new Packet(idWidth, dataWidth, cpWidth))
  })

  val r_stack_push    = RegInit(false.B)
  val r_stack_pop     = RegInit(false.B)
  val r_stack_en      = RegInit(false.B)
  val r_stack_dataIn  = RegInit(0.U.asTypeOf(new StackData(addrWidth, idWidth, cpWidth)))
  val r_stack_dataOut = Reg(new StackData(addrWidth, idWidth, cpWidth))
  val r_stack_valid   = Reg(Bool())
  val stack = Module(new Stack(new StackData(addrWidth, idWidth, cpWidth), dataWidth, stackDepth))
  stack.io.push   := r_stack_push
  stack.io.pop    := r_stack_pop
  stack.io.en     := r_stack_en
  stack.io.dataIn := r_stack_dataIn
  r_stack_dataOut := stack.io.dataOut
  r_stack_valid   := stack.io.valid

  val r_req            = Reg(new RequestBundle(addrWidth, dataWidth))
  val r_req_valid      = RegInit(false.B)
  val r_resp           = Reg(new ResponseBundle(dataWidth))
  val r_resp_valid     = RegInit(false.B)
  val r_routeIn        = Reg(new Packet(idWidth, dataWidth, cpWidth))
  val r_routeIn_valid  = RegInit(false.B)
  val r_routeOut       = Reg(new Packet(idWidth, dataWidth, cpWidth))
  val r_routeOut_valid = RegInit(false.B)
  val oddCP            = RegInit(0.U(5.W))
  val r_res            = Reg(UInt(dataWidth.W))
  // initialize r_srcID to the ID of current IP
  val r_srcID          = RegInit(2.U(idWidth.W))
  val r_srcCP          = RegInit(0.U(cpWidth.W))
  val r_srcResAddr     = Reg(UInt(addrWidth.W))
  val r_para           = Reg(UInt(dataWidth.W))

  r_resp            := io.resp.bits
  r_resp_valid      := io.resp.valid
  io.req.bits       := r_req
  io.req.valid      := r_req_valid
  
  r_routeIn         := io.routeIn.bits
  r_routeIn_valid   := io.routeIn.valid
  io.routeOut.bits  := r_routeOut
  io.routeOut.valid := r_routeOut_valid

  // global memory map
  // 0x0 r_srcResAddr for even
  // 0x4 r_para for even
  // 0x8 r_res for even
  // 0xC r_srcResAddr for odd
  // 0x10 r_para for odd
  // 0x14 r_res for odd

  switch(oddCP) {
    is(0.U) {
        r_routeOut_valid := false.B
        when(r_routeIn_valid) {
            r_srcCP := r_routeIn.srcCP
            r_srcID := r_routeIn.srcID
            oddCP   := r_routeIn.dstCP
        }
    }
    is(1.U) {
        r_req_valid := true.B
        r_req.addr  := 12.U
        r_req.write := false.B

        when(r_resp_valid) {
            r_srcResAddr := r_resp.data
            r_req_valid  := false.B
            oddCP        := 2.U
        }
    }
    is(2.U) {
        r_req_valid := true.B
        r_req.addr  := 16.U
        r_req.write := false.B

        when(r_resp_valid) {
            r_para      := r_resp.data
            r_req_valid := false.B
            oddCP       := 3.U
        }
    }
    is(3.U) {
        r_stack_push              := true.B
        r_stack_en                := true.B
        r_stack_dataIn.srcID      := r_srcID
        r_stack_dataIn.srcCP      := r_srcCP
        r_stack_dataIn.srcResAddr := r_srcResAddr
        oddCP                     := 4.U
    }
    is(4.U) {
        r_stack_en := false.B

        r_req_valid := true.B
        r_req.addr  := 0.U // r_srcResAddr for even
        r_req.write := true.B
        r_req.data  := 20.U

        when(r_resp_valid) {
            r_req_valid := false.B
            oddCP       := 5.U
        }
    }
    is(5.U) {
        r_req_valid := true.B
        r_req.addr  := 4.U // r_para for even
        r_req.write := true.B
        r_req.data  := r_para

        when(r_resp_valid) {
            r_req_valid := false.B
            oddCP      := 6.U
        }
    }
    is(6.U) {
        r_routeOut.srcID := 2.U
        r_routeOut.srcCP := 7.U
        r_routeOut.dstID := 1.U
        r_routeOut.dstCP := 1.U
        r_routeOut_valid := true.B
        oddCP            := 0.U
    }
    is(7.U) {
        // get the result from memory
        r_req_valid := true.B
        r_req.addr  := 20.U
        r_req.write := false.B

        when(r_resp_valid) {
            r_res       := r_resp.data
            r_req_valid := false.B
            oddCP       := 8.U
        }
    }
    is(8.U) {
        r_stack_en  := true.B
        r_stack_pop := true.B
        oddCP       := 9.U
    }
    is(9.U) {
        r_stack_en := false.B
        r_res      := Mux(r_res === 1.U, 0.U, 1.U)
        oddCP      := 10.U
    }
    is(10.U) {
        when(r_stack_valid) {
          r_srcCP      := r_stack_dataOut.srcCP
          r_srcID      := r_stack_dataOut.srcID
          r_srcResAddr := r_stack_dataOut.srcResAddr
          oddCP  := 11.U
        }
    }
    is(11.U) {
        r_req_valid := true.B
        r_req.addr  := r_srcResAddr
        r_req.write := true.B
        r_req.data  := r_res

        when(r_resp_valid) {
            r_req_valid := false.B
            oddCP      := 12.U
        }
    }
    is(12.U) {
        oddCP := Mux(r_srcID === 2.U, 13.U, 14.U)
    }
    is(13.U) {
        oddCP   := 0.U

        // initialize srcID and srcCP to default value
        r_srcID := 2.U
        r_srcCP := 0.U
    }
    is(14.U) {
        r_routeOut.srcID := 2.U
        r_routeOut.srcCP := 7.U
        r_routeOut.dstID := r_srcID
        r_routeOut.dstCP := r_srcCP
        r_routeOut_valid := true.B
        oddCP            := 0.U
    }
  }
}
