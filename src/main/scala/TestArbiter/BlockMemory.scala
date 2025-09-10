package TestArbiter

import chisel3._
import chisel3.util._
import chisel3.experimental._



class BlockMemory(val depth: Int = 112, val width: Int = 8) extends Module {
  val io = IO(new Bundle {
    val mode = Input(UInt(2.W)) // 00 -> disable, 01 -> read, 10 -> write, 11 -> DMA

    // Byte level read/write port
    val readAddr    = Input(UInt(log2Ceil(depth).W))
    val readOffset  = Input(UInt(log2Ceil(depth).W))
    val readLen     = Input(UInt(4.W))
    val readData    = Output(UInt(64.W))
    val readValid   = Output(Bool())

    val writeAddr   = Input(UInt(log2Ceil(depth).W))
    val writeOffset = Input(UInt(log2Ceil(depth).W))
    val writeLen    = Input(UInt(4.W))
    val writeData   = Input(UInt(64.W))
    val writeValid  = Output(Bool())

    // DMA
    val dmaSrcAddr   = Input(UInt(log2Ceil(depth).W))  // byte address
    val dmaDstAddr   = Input(UInt(log2Ceil(depth).W))  // byte address
    val dmaDstOffset = Input(UInt(log2Ceil(depth).W))
    val dmaSrcLen    = Input(UInt(log2Ceil(depth).W)) // byte count
    val dmaDstLen    = Input(UInt(log2Ceil(depth).W)) // byte count
    val dmaValid     = Output(Bool())
  })


  val bram = Module(new XilinxBRAMWrapper)
  bram.io.clk := clock.asBool


  // BRAM default
  bram.io.ena := false.B
  bram.io.wea := false.B
  bram.io.addra := 0.U
  bram.io.dina := 0.U

  bram.io.enb := false.B
  bram.io.web := false.B
  bram.io.addrb := 0.U
  bram.io.dinb := 0.U

  val w_readEnable  = io.mode === 1.U
  val w_writeEnable = io.mode === 2.U
  val w_dmaEnable   = io.mode === 3.U

  // === READ Operation ===
  val sReadIdle :: sReadFirst :: sReadTrans :: sReadEnd :: Nil = Enum(4)

  val r_readCnt      = Reg(UInt(4.W))
  val r_lastReadCnt  = Reg(UInt(4.W))
  val r_readAddr     = Reg(UInt(log2Ceil(depth).W))
  val r_readState    = RegInit(sReadIdle)
  val r_readBytes    = Reg(Vec(8, UInt(8.W)))

  switch(r_readState) {
    is(sReadIdle) {
      when(w_readEnable) {
        r_readState   := sReadFirst
        r_readCnt     := 0.U
        r_lastReadCnt := 0.U
        r_readAddr    := io.readAddr + io.readOffset
      }
      r_readBytes(0) := 0.U
      r_readBytes(1) := 0.U
      r_readBytes(2) := 0.U
      r_readBytes(3) := 0.U
      r_readBytes(4) := 0.U
      r_readBytes(5) := 0.U
      r_readBytes(6) := 0.U
      r_readBytes(7) := 0.U
    }
    is(sReadFirst) {
      bram.io.addra := r_readAddr
      bram.io.ena   := true.B
      bram.io.wea   := false.B

      r_lastReadCnt := r_readCnt
      r_readCnt     := r_readCnt + 1.U
      r_readAddr    := r_readAddr + 1.U
      r_readState   := sReadTrans
    }
    is(sReadTrans) {
      r_readBytes(r_lastReadCnt) := bram.io.douta

      bram.io.addra          := r_readAddr
      bram.io.ena            := true.B
      bram.io.wea            := false.B

      r_lastReadCnt          := r_readCnt
      r_readCnt              := r_readCnt + 1.U
      r_readAddr             := r_readAddr + 1.U

      r_readState            := Mux(io.readLen === 1.U, sReadEnd, Mux(r_readCnt < io.readLen, sReadTrans, sReadEnd))
    }
    is(sReadEnd) {
      r_readState   := sReadIdle
    }
  }

  io.readData  := Cat(r_readBytes(7.U),
                      r_readBytes(6.U),
                      r_readBytes(5.U),
                      r_readBytes(4.U),
                      r_readBytes(3.U),
                      r_readBytes(2.U),
                      r_readBytes(1.U),
                      r_readBytes(0.U))
  io.readValid := Mux(r_readState === sReadEnd, true.B, false.B)

  // === WRITE Operation ===
  val sWriteIdle :: sWriteTrans :: sWriteEnd :: Nil = Enum(3)

  val r_writeCnt      = Reg(UInt(4.W))
  val r_writeAddr     = Reg(UInt(log2Ceil(depth).W))
  val r_writeState    = RegInit(sWriteIdle)
  val r_writeBytes    = Reg(Vec(8, UInt(8.W)))
  val r_writeLen      = Reg(UInt(4.W))

  switch(r_writeState) {
    is(sWriteIdle) {
      when(w_writeEnable) {
        r_writeState      := sWriteTrans
        r_writeCnt        := 0.U
        r_writeAddr       := io.writeAddr + io.writeOffset
        r_writeLen        := io.writeLen - 1.U

        r_writeBytes(0.U) := io.writeData(7, 0)
        r_writeBytes(1.U) := io.writeData(15, 8)
        r_writeBytes(2.U) := io.writeData(23, 16)
        r_writeBytes(3.U) := io.writeData(31, 24)
        r_writeBytes(4.U) := io.writeData(39, 32)
        r_writeBytes(5.U) := io.writeData(47, 40)
        r_writeBytes(6.U) := io.writeData(55, 48)
        r_writeBytes(7.U) := io.writeData(63, 56)
      }
    }
    is(sWriteTrans) {
      bram.io.addrb := r_writeAddr
      bram.io.enb   := true.B
      bram.io.web   := true.B
      bram.io.dinb  := r_writeBytes(r_writeCnt)

      r_writeCnt    := r_writeCnt + 1.U
      r_writeAddr   := r_writeAddr + 1.U
      r_writeState  := Mux(r_writeCnt < r_writeLen, sWriteTrans, sWriteEnd)
    }
    is(sWriteEnd) {
      r_writeState  := sWriteIdle
    }
  }

  io.writeValid := Mux(r_writeState === sWriteEnd, true.B, false.B)

  // DMA logic
  val sDmaIdle :: sDmaFirstRead :: sDmaTrans :: sDmaDone :: Nil = Enum(4)

  val r_dmaSrcCount = Reg(UInt(log2Ceil(depth).W))
  val r_dmaDstCount = Reg(UInt(log2Ceil(depth).W))
  val r_dmaSrcAddr  = Reg(UInt(log2Ceil(depth).W))
  val r_dmaDstAddr  = Reg(UInt(log2Ceil(depth).W))
  val r_dmaState    = RegInit(sDmaIdle)

  switch(r_dmaState) {
    is(sDmaIdle) {
      when(w_dmaEnable) {
        r_dmaState    := Mux(io.dmaSrcLen === 0.U, sDmaTrans, sDmaFirstRead)

        r_dmaSrcCount := 0.U
        r_dmaDstCount := 0.U
        r_dmaSrcAddr  := io.dmaSrcAddr
        r_dmaDstAddr  := io.dmaDstAddr + io.dmaDstOffset
      }
    }
    is(sDmaFirstRead) {
      r_dmaState    := sDmaTrans

      // first read
      bram.io.addra := r_dmaSrcAddr
      bram.io.ena   := true.B
      bram.io.wea   := false.B

      r_dmaSrcAddr  := r_dmaSrcAddr + 1.U
      r_dmaSrcCount := r_dmaSrcCount + 1.U
    }
    is(sDmaTrans) {
      // write the data from the read port
      when(r_dmaDstCount < io.dmaDstLen) {
        bram.io.addrb := r_dmaDstAddr
        bram.io.enb   := true.B
        bram.io.web   := true.B
        bram.io.dinb  := Mux(r_dmaDstCount >= r_dmaSrcCount, 0.U, bram.io.douta)

        r_dmaDstAddr  := r_dmaDstAddr + 1.U
        r_dmaDstCount := r_dmaDstCount + 1.U
      }

      // keep all the data from read port valid
      bram.io.ena   := true.B
      when(r_dmaSrcCount < io.dmaSrcLen) {
        bram.io.addra := r_dmaSrcAddr
        bram.io.wea   := false.B

        r_dmaSrcAddr  := r_dmaSrcAddr + 1.U
        r_dmaSrcCount := r_dmaSrcCount + 1.U
      }

      when((r_dmaDstCount >= io.dmaDstLen) & (r_dmaSrcCount >= io.dmaSrcLen)) {
        r_dmaState := sDmaDone
      }

    }
    is(sDmaDone) {
      r_dmaState := sDmaIdle
    }
  }

  io.dmaValid := Mux(r_dmaState === sDmaDone, true.B, false.B)
}


class BlockMemoryRequestBundle(dataWidth: Int, depth: Int) extends Bundle {

  val mode         = UInt(2.W)
  val readAddr     = UInt(log2Up(depth).W)
  val readOffset   = UInt(log2Up(depth).W)
  val readLen      = UInt(4.W)
  val writeAddr    = UInt(log2Up(depth).W)
  val writeOffset  = UInt(log2Up(depth).W)
  val writeLen     = UInt(4.W)
  val writeData    = UInt(dataWidth.W)
  val dmaSrcAddr   = UInt(log2Up(depth).W)
  val dmaDstAddr   = UInt(log2Up(depth).W)
  val dmaDstOffset = UInt(log2Up(depth).W)
  val dmaSrcLen    = UInt(log2Up(depth).W)
  val dmaDstLen    = UInt(log2Up(depth).W)

}


class BlockMemoryResponseBundle(dataWidth: Int) extends Bundle {
  val data = UInt(dataWidth.W)
}


class BlockMemoryIO(dataWidth: Int, depth: Int) extends Bundle {
  val req = Valid(new BlockMemoryRequestBundle(dataWidth, depth))
  val resp = Flipped(Valid(new BlockMemoryResponseBundle(dataWidth)))
}


class BlockMemoryArbiterIO(numIPs: Int, dataWidth: Int, depth: Int) extends Bundle {
  val ipReqs  = Flipped(Vec(numIPs, Valid(new BlockMemoryRequestBundle(dataWidth, depth))))
  val ipResps = Vec(numIPs, Valid(new BlockMemoryResponseBundle(dataWidth)))
  val ip      = new BlockMemoryIO(dataWidth, depth)
}


class BlockMemoryArbiterModule(numIPs: Int, dataWidth: Int, depth: Int) extends Module {
  val io = IO(new BlockMemoryArbiterIO(numIPs, dataWidth, depth))

  // ------------------ Stage 0: Input Cache ------------------
  val r_ipReq_valid = RegInit(VecInit(Seq.fill(numIPs)(false.B)))
  val r_ipReq_valid_next = RegInit(VecInit(Seq.fill(numIPs)(false.B)))
  val r_ipReq_enable = RegInit(VecInit(Seq.fill(numIPs)(false.B)))
  val r_ipReq_bits = Reg(Vec(numIPs, new BlockMemoryRequestBundle(dataWidth, depth)))

  for (i <- 0 until numIPs) {
    r_ipReq_valid(i) := io.ipReqs(i).valid
    r_ipReq_valid_next(i) := r_ipReq_valid(i)
    when(r_ipReq_valid(i) & ~r_ipReq_valid_next(i)) {
      r_ipReq_enable(i) := true.B
      r_ipReq_bits(i) := io.ipReqs(i).bits
    } .otherwise {
      r_ipReq_enable(i) := false.B
    }
  }

  // ------------------ Stage 1: Arbitration Decision Pipeline ------------------
  val r_foundReq = RegInit(false.B)
  val r_reqBits  = Reg(new BlockMemoryRequestBundle(dataWidth, depth))
  val r_chosen   = Reg(UInt(log2Ceil(numIPs).W))

  r_foundReq := r_ipReq_enable.reduce(_ || _)
  for (i <- 0 until numIPs) {
    when(r_ipReq_enable(i)) {
      r_reqBits := r_ipReq_bits(i)
      r_chosen  := i.U
    }
  }

  io.ip.req.valid := r_foundReq
  io.ip.req.bits  := r_reqBits

  // ------------------ Stage 2: memory.resp handling ------------------
  val r_mem_resp_valid = RegNext(io.ip.resp.valid)
  val r_mem_resp_bits  = RegNext(io.ip.resp.bits)
  val r_mem_resp_id    = RegNext(r_chosen)

  val r_ipResp_valid = RegInit(VecInit(Seq.fill(numIPs)(false.B)))
  val r_ipResp_bits  = Reg(Vec(numIPs, new BlockMemoryResponseBundle(dataWidth)))

  for (i <- 0 until numIPs) {
    r_ipResp_valid(i)    := false.B
    r_ipResp_bits(i).data := 0.U
  }

  when(r_mem_resp_valid) {
    r_ipResp_valid(r_mem_resp_id) := true.B
    r_ipResp_bits(r_mem_resp_id)  := r_mem_resp_bits
  } .otherwise {
    r_ipResp_valid(r_mem_resp_id) := false.B
  }

  for (i <- 0 until numIPs) {
    io.ipResps(i).valid := r_ipResp_valid(i)
    io.ipResps(i).bits  := r_ipResp_bits(i)
  }
}


class BlockMemoryWrapper(val dataWidth: Int , depth: Int) extends Module {
    val io = IO(new Bundle{
        val req = Input(Valid(new BlockMemoryRequestBundle(dataWidth, depth)))
        val resp = Output(Valid(new BlockMemoryResponseBundle(dataWidth)))

    })

    val mod = Module(new BlockMemory(dataWidth))

    val r_req            = Reg(new BlockMemoryRequestBundle(dataWidth, depth))
    val r_req_valid      = RegNext(io.req.valid, false.B)
    val r_req_valid_next = RegNext(r_req_valid, false.B)

    val memory_valid = mod.io.readValid | mod.io.writeValid | mod.io.dmaValid
    val r_resp_data  = RegNext(mod.io.readData)
    val r_resp_valid = RegNext(memory_valid)


    val r_mode = RegInit(0.U(2.W))
    r_mode := Mux(r_req_valid & ~memory_valid, r_req.mode, 0.U)


    r_req := io.req.bits


    mod.io.mode := r_mode
    mod.io.readAddr := r_req.readAddr
    mod.io.readOffset := r_req.readOffset
    mod.io.readLen := r_req.readLen
    mod.io.writeAddr := r_req.writeAddr
    mod.io.writeOffset := r_req.writeOffset
    mod.io.writeLen := r_req.writeLen
    mod.io.writeData := r_req.writeData
    mod.io.dmaSrcAddr := r_req.dmaSrcAddr
    mod.io.dmaDstAddr := r_req.dmaDstAddr
    mod.io.dmaDstOffset := r_req.dmaDstOffset
    mod.io.dmaSrcLen := r_req.dmaSrcLen
    mod.io.dmaDstLen := r_req.dmaDstLen
    io.resp.bits.data := r_resp_data
    io.resp.valid    := r_resp_valid
}


class BlockMemoryFunctionModule(dataWidth: Int, depth: Int) extends Module{
  val io = IO(new Bundle{
    val arb_req  = Valid(new BlockMemoryRequestBundle(dataWidth, depth))
    val arb_resp = Flipped(Valid(new BlockMemoryResponseBundle(dataWidth)))
  })

  val r_arb_req          = Reg(new BlockMemoryRequestBundle(dataWidth, depth))
  val r_arb_req_valid    = RegInit(false.B)
  val r_arb_resp         = Reg(new BlockMemoryResponseBundle(dataWidth))
  val r_arb_resp_valid   = RegInit(false.B)
  r_arb_resp       := io.arb_resp.bits
  r_arb_resp_valid := io.arb_resp.valid
  io.arb_req.bits  := r_arb_req
  io.arb_req.valid := r_arb_req_valid

  val BlockMemoryCP            = RegInit(0.U(4.W))
  val r_res            = Reg(UInt(dataWidth.W))

  switch(BlockMemoryCP) {
    is(0.U) {
      r_arb_req_valid       := true.B
      r_arb_req.mode        := 2.U
      r_arb_req.writeAddr   := 0.U
      r_arb_req.writeData   := "h0807060504030201".U
      r_arb_req.writeOffset := 0.U
      r_arb_req.writeLen    := 8.U
      when(r_arb_resp_valid) {
          r_res           := r_arb_resp.data
          r_arb_req_valid := false.B
          BlockMemoryCP   := 1.U
      }
    }
    is(1.U) {
      r_arb_req_valid       := true.B
      r_arb_req.mode        := 2.U
      r_arb_req.writeAddr   := 8.U
      r_arb_req.writeData   := "h100F0E0D0C0B0A09".U
      r_arb_req.writeOffset := 0.U
      r_arb_req.writeLen    := 8.U
      when(r_arb_resp_valid) {
          r_res           := r_arb_resp.data
          r_arb_req_valid := false.B
          BlockMemoryCP   := 2.U
      }
    }
    is(2.U) {
      r_arb_req_valid      := true.B
      r_arb_req.mode       := 1.U
      r_arb_req.readAddr   := 0.U
      r_arb_req.readOffset := 0.U
      r_arb_req.readLen    := 8.U
      when(r_arb_resp_valid) {
          r_res           := r_arb_resp.data
          r_arb_req_valid := false.B
          BlockMemoryCP   := 3.U
      }
    }
    is(3.U) {
      r_arb_req_valid      := true.B
      r_arb_req.mode       := 1.U
      r_arb_req.readAddr   := 8.U
      r_arb_req.readOffset := 0.U
      r_arb_req.readLen    := 8.U
      when(r_arb_resp_valid) {
          r_res           := r_arb_resp.data
          r_arb_req_valid := false.B
          BlockMemoryCP   := 4.U
      }
    }
    is(4.U) {
      r_arb_req_valid        := true.B
      r_arb_req.mode         := 3.U
      r_arb_req.dmaDstAddr   := 16.U
      r_arb_req.dmaSrcAddr   := 0.U
      r_arb_req.dmaDstLen    := 8.U
      r_arb_req.dmaSrcLen    := 8.U
      r_arb_req.dmaDstOffset := 0.U
      when(r_arb_resp_valid) {
          r_res           := r_arb_resp.data
          r_arb_req_valid := false.B
          BlockMemoryCP   := 5.U
      }
    }
    is(5.U) {
      r_arb_req_valid      := true.B
      r_arb_req.mode       := 1.U
      r_arb_req.readAddr   := 16.U
      r_arb_req.readOffset := 0.U
      r_arb_req.readLen    := 8.U
      when(r_arb_resp_valid) {
          r_res           := r_arb_resp.data
          r_arb_req_valid := false.B
          BlockMemoryCP   := 6.U
      }
    }
    is(6.U) {
      printf("result:%d\n", r_res)
    }
  }
}

