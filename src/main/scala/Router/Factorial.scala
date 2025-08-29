package Router

import chisel3._
import chisel3.util._
import chisel3.experimental._

class FactorialStackData(val addrWidth: Int, val idWidth: Int, val cpWidth: Int, val dataWidth: Int) extends Bundle {
  val srcID      = UInt(idWidth.W)
  val srcCP      = UInt(cpWidth.W)
  val srcResAddr = UInt(addrWidth.W)
  val para       = UInt(dataWidth.W)
}

class Factorial(addrWidth: Int, dataWidth: Int, stackDepth: Int, cpWidth: Int, idWidth: Int, depth: Int) extends Module{
  val io = IO(new Bundle{
    val arbMem_req  = Valid(new BlockMemoryRequestBundle(addrWidth, dataWidth, depth))
    val arbMem_resp = Flipped(Valid(new BlockMemoryResponseBundle(dataWidth)))
    val arbMul_req  = Valid(new MultiplyRequestBundle(dataWidth))
    val arbMul_resp = Flipped(Valid(new MultiplyResponseBundle(dataWidth)))
    val routeIn     = Flipped(Valid(new Packet(idWidth, dataWidth, cpWidth)))
    val routeOut    = Valid(new Packet(idWidth, dataWidth, cpWidth))
  })

  val r_stack_push         = RegInit(false.B)
  val r_stack_pop          = RegInit(false.B)
  val r_stack_en           = RegInit(false.B)
  val r_stack_dataIn       = RegInit(0.U.asTypeOf(new FactorialStackData(addrWidth, idWidth, cpWidth, dataWidth)))
  val r_stack_dataOut      = Reg(new FactorialStackData(addrWidth, idWidth, cpWidth, dataWidth))
  val r_stack_valid        = Reg(Bool())
  val stack = Module(new Stack(new FactorialStackData(addrWidth, idWidth, cpWidth, dataWidth), dataWidth, stackDepth))
  stack.io.push    := r_stack_push
  stack.io.pop     := r_stack_pop
  stack.io.en      := r_stack_en
  stack.io.dataIn  := r_stack_dataIn
  r_stack_dataOut  := stack.io.dataOut
  r_stack_valid    := stack.io.valid

  val r_arbMem_req        = Reg(new BlockMemoryRequestBundle(addrWidth, dataWidth, depth))
  val r_arbMem_req_valid  = RegInit(false.B)
  val r_arbMem_resp       = Reg(new BlockMemoryResponseBundle(dataWidth))
  val r_arbMem_resp_valid = RegInit(false.B)

  r_arbMem_resp       := io.arbMem_resp.bits
  r_arbMem_resp_valid := io.arbMem_resp.valid
  io.arbMem_req.bits  := r_arbMem_req
  io.arbMem_req.valid := r_arbMem_req_valid

  val r_arbMul_req          = Reg(new MultiplyRequestBundle(dataWidth))
  val r_arbMul_req_valid    = RegInit(false.B)
  val r_arbMul_resp         = Reg(new MultiplyResponseBundle(dataWidth))
  val r_arbMul_resp_valid   = RegInit(false.B)
  r_arbMul_resp       := io.arbMul_resp.bits
  r_arbMul_resp_valid := io.arbMul_resp.valid
  io.arbMul_req.bits  := r_arbMul_req
  io.arbMul_req.valid := r_arbMul_req_valid

  val r_routeIn        = Reg(new Packet(idWidth, dataWidth, cpWidth))
  val r_routeIn_valid  = RegInit(false.B)
  val r_routeOut       = Reg(new Packet(idWidth, dataWidth, cpWidth))
  val r_routeOut_valid = RegInit(false.B)
  val factorialCP      = RegInit(0.U(5.W))
  val r_res            = Reg(UInt(dataWidth.W))
  // initialize r_srcID to the ID of current IP
  val r_srcID          = RegInit(0.U(idWidth.W))
  val r_srcCP          = RegInit(0.U(cpWidth.W))
  val r_srcResAddr     = Reg(UInt(addrWidth.W))
  val r_para           = Reg(UInt(dataWidth.W))

  r_routeIn         := io.routeIn.bits
  r_routeIn_valid   := io.routeIn.valid
  io.routeOut.bits  := r_routeOut
  io.routeOut.valid := r_routeOut_valid

  // global memory map
  // 0x0 r_srcResAddr for factorial
  // 0x8 r_para for factorial
  // 0x10 r_res for factorial

  switch(factorialCP) {
    is(0.U) {
        r_routeOut_valid := false.B
        when(r_routeIn_valid) {
            r_srcCP     := r_routeIn.srcCP
            r_srcID     := r_routeIn.srcID
            factorialCP := r_routeIn.dstCP
        }
    }
    is(1.U) {
        r_arbMem_req_valid     := true.B
        r_arbMem_req.readAddr  := 0.U
        r_arbMem_req.mode      := 1.U

        when(r_arbMem_resp_valid) {
            r_srcResAddr       := r_arbMem_resp.data
            r_arbMem_req_valid := false.B
            r_arbMem_req.mode  := 0.U
            factorialCP        := 2.U
        }
    }
    is(2.U) {
        r_arbMem_req_valid     := true.B
        r_arbMem_req.readAddr  := 8.U
        r_arbMem_req.mode      := 1.U

        when(r_arbMem_resp_valid) {
            r_para             := r_arbMem_resp.data
            r_arbMem_req_valid := false.B
            r_arbMem_req.mode  := 0.U
            factorialCP        := 3.U
        }
    }
    is(3.U) {
        factorialCP := Mux(r_para <= 1.U, 4.U, 5.U)
    }
    is(4.U) {
        // write the result back into the memory
        r_res       := 1.U
        factorialCP := 14.U
    }
    is(5.U) {
        r_stack_push              := true.B
        r_stack_en                := true.B
        r_stack_dataIn.srcID      := r_srcID
        r_stack_dataIn.srcCP      := r_srcCP
        r_stack_dataIn.srcResAddr := r_srcResAddr
        r_stack_dataIn.para       := r_para
        factorialCP               := 6.U
    }
    is(6.U) {
        r_stack_en := false.B

        r_arbMem_req_valid     := true.B
        r_arbMem_req.writeAddr := 0.U // r_srcResAddr for factorial
        r_arbMem_req.writeData := 16.U
        r_arbMem_req.mode      := 2.U

        when(r_arbMem_resp_valid) {
            r_arbMem_req_valid := false.B
            r_arbMem_req.mode  := 0.U
            factorialCP        := 7.U
        }
    }
    is(7.U) {
        r_arbMem_req_valid     := true.B
        r_arbMem_req.writeAddr := 8.U // r_para for factorial
        r_arbMem_req.writeData := r_para - 1.U
        r_arbMem_req.mode      := 2.U

        when(r_arbMem_resp_valid) {
            r_arbMem_req_valid := false.B
            r_arbMem_req.mode  := 0.U
            factorialCP        := 8.U
        }
    }
    is(8.U) {
        r_routeOut.srcID := 1.U
        r_routeOut.srcCP := 9.U
        r_routeOut.dstID := 1.U
        r_routeOut.dstCP := 1.U
        r_routeOut_valid := true.B
        factorialCP      := 0.U
    }
    is(9.U) {
        // get the result from memory
        r_arbMem_req_valid     := true.B
        r_arbMem_req.readAddr  := 16.U
        r_arbMem_req.mode      := 1.U

        when(r_arbMem_resp_valid) {
            r_res              := r_arbMem_resp.data
            r_arbMem_req_valid := false.B
            r_arbMem_req.mode  := 0.U
            factorialCP        := 10.U
        }
    }
    is(10.U) {
        r_stack_en  := true.B
        r_stack_pop := true.B
        factorialCP := 11.U
    }
    is(11.U) {
        r_stack_en  := false.B
        factorialCP := 12.U
    }
    is(12.U) {
        when(r_stack_valid) {
          r_srcCP      := r_stack_dataOut.srcCP
          r_srcID      := r_stack_dataOut.srcID
          r_srcResAddr := r_stack_dataOut.srcResAddr
          r_para       := r_stack_dataOut.para
          factorialCP  := 13.U
        }
    }
    is(13.U) {
        r_arbMul_req_valid := true.B
        r_arbMul_req.a     := r_para
        r_arbMul_req.b     := r_res
        when(r_arbMul_resp_valid) {
            r_res              := r_arbMul_resp.out
            r_arbMul_req_valid := false.B
            factorialCP := 14.U
        }
        // r_res       := r_res * r_para
        // factorialCP := 14.U
    }
    is(14.U) {
        r_arbMem_req_valid     := true.B
        r_arbMem_req.writeAddr := r_srcResAddr
        r_arbMem_req.writeData := r_res
        r_arbMem_req.mode      := 2.U

        when(r_arbMem_resp_valid) {
            r_arbMem_req_valid := false.B
            r_arbMem_req.mode  := 0.U
            factorialCP        := 15.U
        }
    }
    is(15.U) {
        factorialCP := Mux(r_srcID === 0.U, 16.U, 17.U)
    }
    is(16.U) {
        r_routeOut.srcID := 1.U
        r_routeOut.srcCP := 0.U
        r_routeOut.dstID := r_srcID
        r_routeOut.dstCP := r_srcCP
        r_routeOut_valid := true.B
        factorialCP := 0.U

        // initialize srcID and srcCP to default value
        r_srcID := 0.U
        r_srcCP := 0.U
    }
    is(17.U) {
        r_routeOut.srcID := 1.U
        r_routeOut.srcCP := 9.U
        r_routeOut.dstID := r_srcID
        r_routeOut.dstCP := r_srcCP
        r_routeOut_valid := true.B
        factorialCP      := 0.U
    }
  }
}
