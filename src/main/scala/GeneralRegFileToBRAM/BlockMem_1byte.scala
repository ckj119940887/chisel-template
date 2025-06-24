package GeneralRegFileToBRAM 

import chisel3._
import chisel3.util._
import chisel3.experimental._
import chisel3.util.experimental.loadMemoryFromFile

class SPAdderUnsigned(val width: Int = 16) extends Module {
    val io = IO(new Bundle{
        val a = Input(UInt(width.W))
        val b = Input(UInt(width.W))
        val start = Input(Bool())
        val out = Output(UInt(width.W))
        val valid = Output(Bool())
    })

    val adder = Module(new XilinxSPAdderUnsignedWrapper)
    adder.io.clk := clock.asBool
    adder.io.A := io.a
    adder.io.B := io.b
    adder.io.ce := io.start
    io.valid := adder.io.valid
    io.out := adder.io.S

    /*
    val state = RegInit(0.U(2.W))
    val regA = Reg(UInt(width.W))
    val regB = Reg(UInt(width.W))
    val result = Reg(UInt(width.W))

    io.valid := Mux(state === 2.U, true.B, false.B)
    io.out := Mux(state === 2.U, result, 0.U)
    switch(state) {
        is(0.U) {
            state := Mux(io.start, 1.U, 0.U)
            regA := Mux(io.start, io.a, regA)
            regB := Mux(io.start, io.b, regB)
        }
        is(1.U) {
            result := regA + regB
            state := 2.U
        }
        is(2.U) {
            state := 0.U
        }
    }
    */
}

class XilinxSPAdderUnsignedWrapper extends BlackBox with HasBlackBoxResource {
  val io = IO(new Bundle {
    val clk   = Input(Bool())
    val ce    = Input(Bool())
    val A     = Input(UInt(16.W))
    val B     = Input(UInt(16.W))
    val valid = Output(Bool())
    val S     = Output(UInt(64.W))
  })

  addResource("/verilog/XilinxSPAdderUnsignedWrapper.v")
}

class SPAdderUnsigned8bit extends Module {
    val io = IO(new Bundle{
        val a = Input(UInt(8.W))
        val b = Input(UInt(8.W))
        val start = Input(Bool())
        val out = Output(UInt(8.W))
        val valid = Output(Bool())
    })

    val adder = Module(new XilinxSPAdderUnsigned8bitWrapper)
    adder.io.clk := clock.asBool
    adder.io.A := io.a
    adder.io.B := io.b
    adder.io.ce := io.start
    io.valid := adder.io.valid
    io.out := adder.io.S

    /*
    val state = RegInit(0.U(1.W))
    val result = Reg(UInt(8.W))

    io.valid := Mux(state === 1.U, true.B, false.B)
    io.out := Mux(state === 1.U, result, 0.U)
    switch(state) {
        is(0.U) {
            state := Mux(io.start, 1.U, 0.U)
            result := io.a + io.b
        }
        is(1.U) {
            state := 0.U
        }
    }
    */
}

class XilinxSPAdderUnsigned8bitWrapper extends BlackBox with HasBlackBoxResource {
  val io = IO(new Bundle {
    val clk   = Input(Bool())
    val ce    = Input(Bool())
    val A     = Input(UInt(16.W))
    val B     = Input(UInt(16.W))
    val valid = Output(Bool())
    val S     = Output(UInt(64.W))
  })

  addResource("/verilog/XilinxSPAdderUnsigned8bitWrapper.v")
}

class SPSubtractorUnsigned(val width: Int = 16) extends Module {
    val io = IO(new Bundle{
        val a = Input(UInt(width.W))
        val b = Input(UInt(width.W))
        val start = Input(Bool())
        val out = Output(UInt(width.W))
        val valid = Output(Bool())
    })

    val adder = Module(new XilinxSPSubtractorUnsignedWrapper)
    adder.io.clk := clock.asBool
    adder.io.A := io.a
    adder.io.B := io.b
    adder.io.ce := io.start
    io.valid := adder.io.valid
    io.out := adder.io.S

    /*
    val state = RegInit(0.U(2.W))
    val regA = Reg(UInt(width.W))
    val regB = Reg(UInt(width.W))
    val result = Reg(UInt(width.W))

    io.valid := Mux(state === 2.U, true.B, false.B)
    io.out := Mux(state === 2.U, result, 0.U)
    switch(state) {
        is(0.U) {
            state := Mux(io.start, 1.U, 0.U)
            regA := Mux(io.start, io.a, regA)
            regB := Mux(io.start, io.b, regB)
        }
        is(1.U) {
            result := regA - regB
            state := 2.U
        }
        is(2.U) {
            state := 0.U
        }
    }
    */
}

class XilinxSPSubtractorUnsignedWrapper extends BlackBox with HasBlackBoxResource {
  val io = IO(new Bundle {
    val clk   = Input(Bool())
    val ce    = Input(Bool())
    val A     = Input(UInt(16.W))
    val B     = Input(UInt(16.W))
    val valid = Output(Bool())
    val S     = Output(UInt(64.W))
  })

  addResource("/verilog/XilinxSPSubtractorUnsignedWrapper.v")
}

class SPAdderConstant1(val width: Int = 16) extends Module {
    val io = IO(new Bundle{
        val a = Input(UInt(width.W))
        val start = Input(Bool())
        val out = Output(UInt(width.W))
        val valid = Output(Bool())
    })

    val adder = Module(new XilinxSPAdderConstant1Wrapper)
    adder.io.clk := clock.asBool
    adder.io.A := io.a
    adder.io.ce := io.start
    io.valid := adder.io.valid
    io.out := adder.io.S

    /*
    val state = RegInit(0.U(2.W))
    val regA = Reg(UInt(width.W))
    val result = Reg(UInt(width.W))

    io.valid := Mux(state === 2.U, true.B, false.B)
    io.out := Mux(state === 2.U, result, 0.U)
    switch(state) {
        is(0.U) {
            state := Mux(io.start, 1.U, 0.U)
            regA := Mux(io.start, io.a, regA)
        }
        is(1.U) {
            result := regA + 1.U
            state := 2.U
        }
        is(2.U) {
            state := 0.U
        }
    }
    */
}

class XilinxSPAdderConstant1Wrapper extends BlackBox with HasBlackBoxResource {
  val io = IO(new Bundle {
    val clk   = Input(Bool())
    val ce    = Input(Bool())
    val A     = Input(UInt(16.W))
    val valid = Output(Bool())
    val S     = Output(UInt(64.W))
  })

  addResource("/verilog/XilinxSPAdderConstant1Wrapper.v")
}


class BRAMIP (val depth: Int = 1024, val width: Int = 8) extends Module {
    val io = IO(new Bundle{
        val ena = Input(Bool())
        val wea = Input(Bool())
        val addra = Input(UInt(log2Ceil(depth).W))
        val dina = Input(UInt(width.W))
        val douta = Output(UInt(width.W))

        val enb = Input(Bool())
        val web = Input(Bool())
        val addrb = Input(UInt(log2Ceil(depth).W))
        val dinb = Input(UInt(width.W))
        val doutb = Output(UInt(width.W))
    })
    
    val mem = SyncReadMem(depth, UInt(width.W))
    loadMemoryFromFile(mem, "BRAM_init.hex")

    io.douta := 0.U
    io.doutb := 0.U

    when(io.ena) {
      when(io.wea) {
        mem.write(io.addra, io.dina)
      } .otherwise {
        io.douta := mem.read(io.addra)
      }
    }

    when(io.enb) {
      when(io.web) {
        mem.write(io.addrb, io.dinb)
      } .otherwise {
        io.doutb := mem.read(io.addrb)
      }
    }
}

class XilinxBRAMWrapper extends BlackBox with HasBlackBoxResource {
  val io = IO(new Bundle {
    val clk = Input(Bool())
    val ena = Input(Bool())
    val wea = Input(Bool())
    val addra = Input(UInt(10.W))
    val dina = Input(UInt(8.W))
    val douta = Output(UInt(8.W))
    val enb = Input(Bool())
    val web = Input(Bool())
    val addrb = Input(UInt(10.W))
    val dinb = Input(UInt(8.W))
    val doutb = Output(UInt(8.W))
  })

  addResource("/verilog/XilinxBRAMWrapper.v")
}

class BRAMAddrComp(val spWidth: Int = 16, val shiftAmount: Int = 3, val dataWidth: Int = 64) extends Module {
  val io = IO(new Bundle {
    val addr     = Input(UInt(spWidth.W))
    val offset   = Input(UInt(spWidth.W))
    val start    = Input(Bool())       
    val isWrite  = Input(Bool())
    val writeLen = Input(UInt(4.W))

    val currAddr          = Output(UInt((spWidth - shiftAmount).W))
    val nextAddr          = Output(UInt((spWidth - shiftAmount).W))
    val mask              = Output(UInt((shiftAmount*2).W))
    val writeMaskFullBits = Output(UInt((dataWidth*2).W))
    val valid             = Output(Bool())      
  })

  val mask_r = Reg(UInt((shiftAmount * 2).W))
  val validReg = RegInit(false.B)

  // Adder for write
  val writeLen = RegInit(0.U(8.W))
  writeLen := io.writeLen << shiftAmount
  val writeMaskAdder = Module(new SPAdderUnsigned8bit)
  val writeMaskAdderResult = Reg(UInt(8.W))
  val writeMaskValid = RegInit(true.B)
  writeMaskAdder.io.a := mask_r
  writeMaskAdder.io.b := writeLen
  writeMaskAdder.io.start := io.isWrite & validReg
  when(writeMaskAdder.io.valid) {
    writeMaskAdderResult := writeMaskAdder.io.out
  }

  val maskFullBits = VecInit(Seq.tabulate(128) { i =>
    ((i.U >= mask_r) && (i.U < writeMaskAdderResult))
  }).asUInt

  // 1st Adder: addr + offset
  val adder1 = Module(new SPAdderUnsigned(spWidth))
  adder1.io.a := io.addr
  adder1.io.b := io.offset
  adder1.io.start := io.start

  val currAddr = Reg(UInt((spWidth - shiftAmount).W))
  when(adder1.io.valid) {
    currAddr := adder1.io.out >> shiftAmount
    mask_r   := adder1.io.out(shiftAmount-1, 0) << shiftAmount
    validReg := true.B
  }

  // 2nd Adder: base + 1
  val adder2 = Module(new SPAdderConstant1(spWidth - shiftAmount))
  adder2.io.a := currAddr
  adder2.io.start := validReg

  when(adder2.io.valid) {
    validReg := false.B
  }

  io.valid             := adder2.io.valid
  io.currAddr          := currAddr
  io.nextAddr          := adder2.io.out
  io.mask              := mask_r
  io.writeMaskFullBits := maskFullBits
}

class BRAMIPWrapper(val bramDepth: Int = 1024, val bramWidth: Int = 8, val portWidth: Int = 64) extends Module {
  val io = IO(new Bundle {
    val mode = Input(UInt(2.W)) // 00 -> disable, 01 -> read, 10 -> write, 11 -> DMA

    // Byte level read/write port
    val readAddr    = Input(UInt(log2Up(bramDepth).W))
    val readOffset  = Input(UInt(log2Up(bramDepth).W))
    val readLen     = Input(UInt(log2Up(portWidth+1).W))
    val readData    = Output(UInt(portWidth.W))
    val readValid   = Output(Bool())

    val writeAddr   = Input(UInt(log2Up(bramDepth).W))
    val writeOffset = Input(UInt(log2Up(bramDepth).W))
    val writeLen    = Input(UInt(log2Up(portWidth+1).W))
    val writeData   = Input(UInt(portWidth.W))
    val writeValid  = Output(Bool())

    // DMA
    val dmaSrcAddr   = Input(UInt(log2Up(bramDepth).W))  // byte address
    val dmaDstAddr   = Input(UInt(log2Up(bramDepth).W))  // byte address
    val dmaDstOffset = Input(UInt(log2Up(bramDepth).W))
    val dmaSrcLen    = Input(UInt(log2Up(bramDepth).W)) // byte count
    val dmaDstLen    = Input(UInt(log2Up(bramDepth).W)) // byte count
    val dmaValid     = Output(Bool())
  })

  val bram = Module(new BRAMIP(bramDepth, bramWidth))

  // val bram = Module(new XilinxBRAMWrapper)
  // bram.io.clk := clock.asBool

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

  val r_readCnt      = Reg(UInt(log2Up(portWidth/8 + 1).W))
  val r_lastReadCnt  = Reg(UInt(log2Up(portWidth/8 + 1).W))
  val r_readAddr     = Reg(UInt(log2Up(bramDepth).W))
  val r_readState    = RegInit(sReadIdle)
  val r_readBytes    = Reg(Vec((portWidth/8), UInt(8.W)))

  switch(r_readState) {
    is(sReadIdle) {
      when(w_readEnable) {
        r_readState   := sReadFirst
        r_readCnt     := 0.U
        r_lastReadCnt := 0.U
        r_readAddr    := io.readAddr + io.readOffset
      }
      for(i <- 0 until (portWidth/8)) {
        r_readBytes(i.U) := 0.U
      }
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

  val bytesSeq = Seq.tabulate(portWidth/8)(i => r_readBytes(i.U))
  val selectedBytes = bytesSeq.take(portWidth/8).reverse
  io.readData := Cat(selectedBytes)
  io.readValid := Mux(r_readState === sReadEnd, true.B, false.B)

  // === WRITE Operation ===
  val sWriteIdle :: sWriteTrans :: sWriteEnd :: Nil = Enum(3)

  val r_writeCnt      = Reg(UInt(log2Up(portWidth/8+1).W))
  val r_writeAddr     = Reg(UInt(log2Up(bramDepth).W))
  val r_writeState    = RegInit(sWriteIdle)
  val r_writeBytes    = Reg(Vec(portWidth/8, UInt(8.W)))
  val r_writeLen      = Reg(UInt(log2Up(portWidth/8+1).W))

  switch(r_writeState) {
    is(sWriteIdle) {
      when(w_writeEnable) {
        r_writeState      := sWriteTrans
        r_writeCnt        := 0.U
        r_writeAddr       := io.writeAddr + io.writeOffset
        r_writeLen        := io.writeLen - 1.U

        for (i <- 0 until (portWidth / 8)) {
          r_writeBytes(i.U) := io.writeData(8 * (i + 1) - 1, 8 * i)
        }
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

  val r_dmaSrcCount = Reg(UInt(log2Up(bramDepth).W))
  val r_dmaDstCount = Reg(UInt(log2Up(bramDepth).W))
  val r_dmaSrcAddr  = Reg(UInt(log2Up(bramDepth).W))
  val r_dmaDstAddr  = Reg(UInt(log2Up(bramDepth).W))
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
      /*
      when(r_dmaSrcCount >= io.dmaSrcLen) {
        r_dmaState := sDmaDone
      }
      */
    }
    is(sDmaDone) {
      r_dmaState := sDmaIdle
    }
  }

  io.dmaValid := Mux(r_dmaState === sDmaDone, true.B, false.B)

}

class BlockMemoryOptimized(val depth: Int = 840, val width: Int = 8) extends Module {
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

  val bram = Module(new BRAMIP(depth, width))

  //val bram = Module(new XilinxBRAMWrapper)
  //bram.io.clk := clock.asBool

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
  val sReadIdle :: sReadRequest :: sReadDataSave :: sReadCondUpdate :: sReadCondCheck :: sReadEnd :: Nil = Enum(6)

  val r_readLen      = Reg(UInt(4.W))
  val r_isReadLenEq1 = Reg(Bool())
  val r_isReadFinish = Reg(Bool())
  val r_readCnt      = Reg(UInt(4.W))
  val r_lastReadCnt  = Reg(UInt(4.W))
  val r_readAddr     = Reg(UInt(log2Ceil(depth).W))
  val r_readState    = RegInit(sReadIdle)
  val r_readData     = Reg(UInt(64.W))
  val r_readBytes    = Reg(Vec(8, UInt(8.W)))

  switch(r_readState) {
    is(sReadIdle) {
      when(w_readEnable) {
        r_readState   := sReadRequest
        r_readLen     := io.readLen
        r_readCnt     := 0.U
        r_lastReadCnt := 0.U
        r_readData    := 0.U
        r_readAddr    := io.readAddr + io.readOffset
      }
    }
    is(sReadRequest) {
      bram.io.addra  := r_readAddr
      bram.io.ena    := true.B
      bram.io.wea    := false.B

      r_lastReadCnt  := r_readCnt
      r_readCnt      := r_readCnt + 1.U
      r_readAddr     := r_readAddr + 1.U
      r_readState    := sReadDataSave
      r_isReadLenEq1 := r_readLen === 1.U
    }
    is(sReadDataSave) {
      bram.io.ena                := true.B
      r_readBytes(r_lastReadCnt) := bram.io.douta
      r_readState                := sReadCondUpdate
    }
    is(sReadCondUpdate) {
      r_isReadFinish := r_readCnt >= r_readLen
      r_readState    := Mux(r_isReadLenEq1, sReadEnd, sReadCondCheck)
      r_readData     := Cat(r_readBytes(7.U),
                            r_readBytes(6.U),
                            r_readBytes(5.U),
                            r_readBytes(4.U),
                            r_readBytes(3.U),
                            r_readBytes(2.U),
                            r_readBytes(1.U),
                            r_readBytes(0.U))
    }
    is(sReadCondCheck) {
      r_readState    := Mux(r_isReadFinish, sReadEnd, sReadRequest)
    }
    is(sReadEnd) {
      r_readState   := sReadIdle
    }
  }

  io.readData  := r_readData
  io.readValid := Mux(r_readState === sReadEnd, true.B, false.B)

  // === WRITE Operation ===
  val sWriteIdle :: sWriteCondUpdate :: sWriteTrans :: sWriteEnd :: Nil = Enum(4)

  val r_writeCnt      = Reg(UInt(4.W))
  val r_writeAddr     = Reg(UInt(log2Ceil(depth).W))
  val r_writeState    = RegInit(sWriteIdle)
  val r_writeBytes    = Reg(Vec(8, UInt(8.W)))
  val r_writeLen      = Reg(UInt(4.W))
  val r_writeDataByte = Reg(UInt(8.W))
  val r_isWriteFinish = Reg(Bool())

  r_writeBytes(0.U) := io.writeData(7, 0)
  r_writeBytes(1.U) := io.writeData(15, 8)
  r_writeBytes(2.U) := io.writeData(23, 16)
  r_writeBytes(3.U) := io.writeData(31, 24)
  r_writeBytes(4.U) := io.writeData(39, 32)
  r_writeBytes(5.U) := io.writeData(47, 40)
  r_writeBytes(6.U) := io.writeData(55, 48)
  r_writeBytes(7.U) := io.writeData(63, 56)

  switch(r_writeState) {
    is(sWriteIdle) {
      when(w_writeEnable) {
        r_writeState      := sWriteCondUpdate
        r_writeCnt        := 0.U
        r_writeAddr       := io.writeAddr + io.writeOffset
        r_writeLen        := io.writeLen - 1.U
      }
    }
    is(sWriteCondUpdate) {
      r_writeDataByte := r_writeBytes(r_writeCnt)
      r_isWriteFinish := r_writeCnt >= r_writeLen
      r_writeState    := sWriteTrans
    }
    is(sWriteTrans) {
      bram.io.addrb := r_writeAddr
      bram.io.enb   := true.B
      bram.io.web   := true.B
      bram.io.dinb  := r_writeDataByte

      r_writeCnt    := r_writeCnt + 1.U
      r_writeAddr   := r_writeAddr + 1.U
      r_writeState  := Mux(r_isWriteFinish, sWriteEnd, sWriteCondUpdate)
    }
    is(sWriteEnd) {
      r_writeState  := sWriteIdle
    }
  }

  io.writeValid := Mux(r_writeState === sWriteEnd, true.B, false.B)

  // DMA logic
  val sDmaIdle :: sDmaCondUpdate :: sDmaReadRequest :: sDmaWriteRequest :: sDmaSrcCheck :: sDmaDstCheck :: sDmaWriteExtra :: sDmaDone :: Nil = Enum(8)

  val r_dmaSrcCount  = Reg(UInt(log2Ceil(depth).W))
  val r_dmaDstCount  = Reg(UInt(log2Ceil(depth).W))
  val r_dmaSrcAddr   = Reg(UInt(log2Ceil(depth).W))
  val r_dmaDstAddr   = Reg(UInt(log2Ceil(depth).W))
  val r_dmaState     = RegInit(sDmaIdle)
  val r_dmaSrcLen    = Reg(UInt(log2Ceil(depth).W))
  val r_dmaDstLen    = Reg(UInt(log2Ceil(depth).W))
  val r_dmaSrcFinish = Reg(Bool())
  val r_dmaDstFinish = Reg(Bool())

  switch(r_dmaState) {
    is(sDmaIdle) {
      when(w_dmaEnable) {
        r_dmaSrcCount := 0.U
        r_dmaDstCount := 0.U
        r_dmaSrcAddr  := io.dmaSrcAddr
        r_dmaDstAddr  := io.dmaDstAddr + io.dmaDstOffset
        r_dmaSrcLen   := io.dmaSrcLen
        r_dmaDstLen   := io.dmaDstLen

        r_dmaState    := sDmaCondUpdate
      }
    }
    is(sDmaCondUpdate) {
      r_dmaSrcFinish := r_dmaSrcCount >= r_dmaSrcLen
      r_dmaState     := sDmaSrcCheck
    }
    is(sDmaReadRequest) {
      r_dmaState    := sDmaWriteRequest

      // read request
      bram.io.addra := r_dmaSrcAddr
      bram.io.ena   := true.B
      bram.io.wea   := false.B

      r_dmaSrcAddr  := r_dmaSrcAddr + 1.U
      r_dmaSrcCount := r_dmaSrcCount + 1.U
    }
    is(sDmaWriteRequest) {
      bram.io.ena    := true.B
      r_dmaSrcFinish := r_dmaSrcCount >= r_dmaSrcLen 

      // write the data from the read port
      bram.io.addrb := r_dmaDstAddr
      bram.io.enb   := true.B
      bram.io.web   := true.B
      bram.io.dinb  := bram.io.douta

      r_dmaDstAddr  := r_dmaDstAddr + 1.U
      r_dmaDstCount := r_dmaDstCount + 1.U

      r_dmaState    := sDmaSrcCheck
    }
    is(sDmaSrcCheck) {
      r_dmaDstFinish := r_dmaDstCount >= r_dmaDstLen
      r_dmaState     := Mux(r_dmaSrcFinish, sDmaDstCheck, sDmaReadRequest)
    }
    is(sDmaDstCheck) {
      r_dmaState := Mux(r_dmaDstFinish, sDmaDone, sDmaWriteExtra)
    }
    is(sDmaWriteExtra) {
      bram.io.addrb := r_dmaDstAddr
      bram.io.enb   := true.B
      bram.io.web   := true.B
      bram.io.dinb  := 0.U

      r_dmaDstAddr  := r_dmaDstAddr + 1.U
      r_dmaDstCount := r_dmaDstCount + 1.U

      r_dmaState    := sDmaSrcCheck
    }
    is(sDmaDone) {
      r_dmaState := sDmaIdle
    }
  }

  io.dmaValid := Mux(r_dmaState === sDmaDone, true.B, false.B)
}
