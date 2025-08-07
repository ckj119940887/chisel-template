package Router

import chisel3._
import chisel3.util._
import chisel3.experimental._

class Add(addrWidth: Int, dataWidth: Int) extends Module{
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
  val addCP        = RegInit(0.U(4.W))

  val r_paraX = Reg(UInt(dataWidth.W))
  val r_paraY = Reg(UInt(dataWidth.W))
  val r_paraZ = Reg(UInt(dataWidth.W))
  val r_res   = Reg(UInt(dataWidth.W))

  r_resp       := io.resp.bits
  r_resp_valid := io.resp.valid
  io.valid     := r_valid
  io.req.bits  := r_req
  io.req.valid := r_req_valid

  switch(addCP) {
    is(0.U) {
        r_valid := false.B
        addCP   := Mux(r_start, 1.U, 0.U)
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
        r_valid := true.B
        addCP := 0.U
    }
  }
}