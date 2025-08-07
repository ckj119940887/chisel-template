package Router

import chisel3._
import chisel3.util._
import chisel3.experimental._

class SimpleMemory(val depth: Int = 1024, val width: Int = 8) extends Module {
    val io = IO(new Bundle{
        val en = Input(Bool())
        val we = Input(Bool())
        val addr = Input(UInt(log2Ceil(depth).W))
        val din = Input(UInt(width.W))
        val dout = Output(UInt(width.W))
    })
    
    val mem = SyncReadMem(depth, UInt(width.W))

    io.dout := mem.read(io.addr)

    when(io.en & io.we) {
      mem.write(io.addr, io.din)
    }
}

class SimpleMemoryWrapper(val depth: Int = 1024, val addrWidth: Int = 32, val dataWidth: Int = 32) extends Module {
    val io = IO(new Bundle{
        val req = Input(Valid(new RequestBundle(addrWidth, dataWidth)))
        val resp = Output(Valid(new ResponseBundle(dataWidth)))
    })

    val mem = Module(new SimpleMemory(depth, dataWidth))
    mem.io.en := false.B
    mem.io.we := false.B
    mem.io.addr := 0.U
    mem.io.din := 0.U

    val r_req = Reg(new RequestBundle(addrWidth, dataWidth))
    val r_req_valid = RegNext(io.req.valid, false.B)
    val r_req_valid_next = RegNext(r_req_valid, false.B)
    val r_resp_data = RegNext(mem.io.dout)
    val r_resp_valid = RegNext(RegNext(r_req_valid & ~r_req_valid_next))

    r_req := io.req.bits

    io.resp.valid := r_resp_valid
    io.resp.bits.data := r_resp_data

    when(r_req_valid) {
      mem.io.en := true.B
      mem.io.we := r_req.write
      mem.io.addr := r_req.addr
      mem.io.din := r_req.data
    }
}
