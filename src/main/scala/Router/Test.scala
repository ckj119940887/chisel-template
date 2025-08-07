package Router

import chisel3._
import chisel3.util._
import chisel3.experimental._

class Test(addrWidth: Int, dataWidth: Int) extends Module{
  val io = IO(new Bundle{
    val start = Input(Bool())
    val valid = Output(Bool())
    val req   = Valid(new RequestBundle(addrWidth, dataWidth))
    val resp  = Flipped(Valid(new ResponseBundle(dataWidth)))
  })

  val r_start      = RegNext(io.start)
  val r_valid      = RegInit(false.B)
  val r_req        = Reg(new RequestBundle(addrWidth, dataWidth))
  val r_req_valid  = RegInit(false.B)
  val r_resp       = Reg(new ResponseBundle(dataWidth))
  val r_resp_valid = RegInit(false.B)
  val testCP       = RegInit(0.U(4.W))

  val r_res   = Reg(UInt(dataWidth.W))

  r_resp       := io.resp.bits
  r_resp_valid := io.resp.valid
  io.valid     := r_valid
  io.req.bits  := r_req
  io.req.valid := r_req_valid

  switch(testCP) {
    is(0.U) {
        r_valid := false.B
        testCP  := Mux(r_start, 1.U, 0.U)
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
        r_valid := true.B
        testCP  := 5.U
    }
    is(5.U) {
        r_valid := false.B
        testCP  := Mux(r_start, 6.U, 5.U)
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
            testCP       := 8.U
        }
    }
    is(8.U) {
        r_valid := true.B
        testCP := 0.U
    }
  }
}