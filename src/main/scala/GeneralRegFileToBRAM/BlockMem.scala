package GeneralRegFileToBRAM 

import chisel3._
import chisel3.util._
import chisel3.experimental._
import chisel3.util.experimental.loadMemoryFromFile

/*
class SPAdderUnsigned(val width: Int = 16) extends Module {
    val io = IO(new Bundle{
        val a = Input(UInt(width.W))
        val b = Input(UInt(width.W))
        val start = Input(Bool())
        val out = Output(UInt(width.W))
        val valid = Output(Bool())
    })

    // val adder = Module(new XilinxSPAdderUnsignedWrapper)
    // adder.io.clk := clock.asBool
    // adder.io.A := io.a
    // adder.io.B := io.b
    // adder.io.ce := io.start
    // io.valid := adder.io.valid
    // io.out := adder.io.S

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

    // val adder = Module(new XilinxSPAdderUnsigned8bitWrapper)
    // adder.io.clk := clock.asBool
    // adder.io.A := io.a
    // adder.io.B := io.b
    // adder.io.ce := io.start
    // io.valid := adder.io.valid
    // io.out := adder.io.S

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

    // val adder = Module(new XilinxSPSubtractorUnsignedWrapper)
    // adder.io.clk := clock.asBool
    // adder.io.A := io.a
    // adder.io.B := io.b
    // adder.io.ce := io.start
    // io.valid := adder.io.valid
    // io.out := adder.io.S

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

    // val adder = Module(new XilinxSPAdderConstant1Wrapper)
    // adder.io.clk := clock.asBool
    // adder.io.A := io.a
    // adder.io.ce := io.start
    // io.valid := adder.io.valid
    // io.out := adder.io.S

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


class BRAMIP (val depth: Int = 1024, val width: Int = 64) extends Module {
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
    val dina = Input(UInt(64.W))
    val douta = Output(UInt(64.W))
    val enb = Input(Bool())
    val web = Input(Bool())
    val addrb = Input(UInt(10.W))
    val dinb = Input(UInt(64.W))
    val doutb = Output(UInt(64.W))
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

class BRAMIPWrapper(val depth: Int = 1024, val width: Int = 64) extends Module {
  val io = IO(new Bundle {
    val mode = Input(UInt(2.W)) // 00 -> disable, 01 -> read, 10 -> write, 11 -> DMA

    // Byte level read/write port
    val readAddr    = Input(UInt(log2Ceil(depth * width).W))
    val readOffset  = Input(UInt(log2Ceil(depth * width).W))
    val readData    = Output(UInt(64.W))
    val readValid   = Output(Bool())

    val writeAddr   = Input(UInt(log2Ceil(depth * width).W))
    val writeOffset = Input(UInt(log2Ceil(depth * width).W))
    val writeLen    = Input(UInt(4.W))
    val writeData   = Input(UInt(64.W))
    val writeValid  = Output(Bool())

    // DMA
    val dmaSrcAddr   = Input(UInt(log2Ceil(depth * width).W))  // byte address
    val dmaDstAddr   = Input(UInt(log2Ceil(depth * width).W))  // byte address
    val dmaDstOffset = Input(UInt(log2Ceil(depth * width).W))
    val dmaLength    = Input(UInt((log2Ceil(depth * width)).W)) // byte count
    val dmaValid     = Output(Bool())
  })

  val bram = Module(new BRAMIP(depth, 64))

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

  val shiftAmount = log2Ceil(width/8)

  val readEnable  = io.mode === 1.U
  val writeEnable = io.mode === 2.U
  val dmaEnable   = io.mode === 3.U

  // SPAdder
  val bramAdderComp = Module(new BRAMAddrComp(log2Ceil(depth * width), shiftAmount))
  val done = RegInit(false.B)

  bramAdderComp.io.start    := !done & (readEnable | writeEnable)
  bramAdderComp.io.isWrite  := writeEnable
  bramAdderComp.io.writeLen := io.writeLen
  bramAdderComp.io.addr     := 0.U
  bramAdderComp.io.offset   := 0.U
  when(readEnable & !done) {
    bramAdderComp.io.addr   := io.readAddr
    bramAdderComp.io.offset := io.readOffset
  }
  when(writeEnable & !done) {
    bramAdderComp.io.addr   := io.writeAddr
    bramAdderComp.io.offset := io.writeOffset
  }

  when(bramAdderComp.io.valid) {
    done := true.B
  }

  when(io.readValid | io.writeValid) {
    done := false.B
  }

  // === READ Operation ===
  val readEnable_r = RegNext(bramAdderComp.io.valid, false.B)
  val readStart = readEnable & bramAdderComp.io.valid & !readEnable_r

  val r_offset = bramAdderComp.io.mask 
  val r_word0  = bramAdderComp.io.currAddr
  val r_word1  = bramAdderComp.io.nextAddr

  val r_dataA1 = Reg(UInt(width.W))
  r_dataA1 := bram.io.douta
  val r_dataB1 = Reg(UInt(width.W))
  r_dataB1 := bram.io.doutb

  val r_comb = Cat(r_dataB1, r_dataA1) >> r_offset
  io.readData := r_comb(63, 0)
  io.readValid := RegNext(RegNext(readStart))

  // === WRITE Operation ===
  val writeEnable_r = RegNext(bramAdderComp.io.valid, false.B)
  val writeStart = writeEnable & bramAdderComp.io.valid && !writeEnable_r

  val w_valid0 = RegNext(writeStart, init = false.B)
  val w_valid1 = RegNext(w_valid0, init = false.B)
  val w_valid2 = RegNext(w_valid1, init = false.B)
  val w_valid3 = RegNext(w_valid2, init = false.B)

  val w_word0 = RegInit(0.U(log2Ceil(depth * 8).W)) 
  val w_word1 = RegInit(0.U(log2Ceil(depth * 8).W))
  val w_offset = RegInit(0.U((shiftAmount*2).W))
  val maskFullBits = RegInit(0.U((width*2).W))
  when(bramAdderComp.io.valid) {
    w_word0 := bramAdderComp.io.currAddr
    w_word1 := bramAdderComp.io.nextAddr
    w_offset := bramAdderComp.io.mask
    maskFullBits := bramAdderComp.io.writeMaskFullBits
  }

  val w_data = io.writeData

  val w_oldA = bram.io.douta
  val w_oldB = bram.io.doutb

  val fullOld = Cat(w_oldB, w_oldA)
  val shifted = (Cat(0.U(width.W), w_data) << w_offset)(width*2-1,0)
  val newAll = Reg(UInt((width*2).W)) 
  newAll := (fullOld & ~maskFullBits) | (shifted & maskFullBits)
  val newA = newAll(63, 0)
  val newB = newAll(127, 64)

  io.writeValid := w_valid3

  // === Unified BRAM Port A & B Control via Mux ===
  when(readEnable | writeEnable) {
    bram.io.ena := readEnable | writeEnable 
    bram.io.wea := w_valid2
    bram.io.addra := Mux(w_valid0, w_word0, Mux(w_valid2, w_word0, r_word0))
    bram.io.dina := Mux(w_valid2, newA, 0.U)

    bram.io.enb := readEnable | writeEnable
    bram.io.web := w_valid2
    bram.io.addrb := Mux(w_valid0, w_word1, Mux(w_valid2, w_word1, r_word1))
    bram.io.dinb := Mux(w_valid2, newB, 0.U)
  }

  // DMA logic
  // FSM States
  val sIdle :: sUpdateBytes :: sUpdatePtr :: sReadResp :: sMask :: sUpdateRemaining :: sWrite :: sDone :: Nil = Enum(8)
  val state = RegInit(sIdle)

  val dmaSPAdder0 = Module(new SPAdderUnsigned(log2Ceil(depth * width)))
  dmaSPAdder0.io.a     := 0.U
  dmaSPAdder0.io.b     := 0.U
  dmaSPAdder0.io.start := false.B
  val dmaSPAdder1 = Module(new SPAdderUnsigned(log2Ceil(depth * width)))
  dmaSPAdder1.io.a     := 0.U
  dmaSPAdder1.io.b     := 0.U
  dmaSPAdder1.io.start := false.B
  val dmaSPSubtractor = Module(new SPSubtractorUnsigned(log2Ceil(depth * width)))
  dmaSPSubtractor.io.a     := 0.U
  dmaSPSubtractor.io.b     := 0.U
  dmaSPSubtractor.io.start := false.B

  val srcPtr = Reg(UInt(log2Ceil(depth * width).W))
  val dstPtr = Reg(UInt(log2Ceil(depth * width).W))
  val nextSrcPtr = Reg(UInt(log2Ceil(depth * width).W))
  val nextDstPtr = Reg(UInt(log2Ceil(depth * width).W))
  val remaining = Reg(UInt(log2Ceil(depth * width).W))
  val nextRemaining = Reg(UInt(log2Ceil(depth * width).W))

  val srcWordAddr = srcPtr >> shiftAmount
  val dstWordAddr = dstPtr >> shiftAmount
  val srcOffset = srcPtr(shiftAmount-1, 0)
  val dstOffset = dstPtr(shiftAmount-1, 0)
  val srcOffsetBits_r = Reg(UInt((shiftAmount*2).W))
  val dstOffsetBits_r = Reg(UInt((shiftAmount*2).W))
  srcOffsetBits_r := srcOffset << shiftAmount
  dstOffsetBits_r := dstOffset << shiftAmount

  // bytes need to be transfered
  val bytesLeft = Reg(UInt((log2Ceil(depth * width) + 1).W))
  val byteCount = Mux(remaining < bytesLeft, remaining, bytesLeft)(shiftAmount,0)

  val readSrcData = Reg(UInt(width.W))
  val readDstData = Reg(UInt(width.W))

  val maskBits = Reg(UInt((width*2).W))
  val srcShifted = Reg(UInt((width*2).W))
  val newData = (readDstData & ~maskBits) | (srcShifted << dstOffsetBits_r)

  // output
  io.dmaValid := (state === sDone)
  when(dmaEnable) {
    bram.io.ena := true.B
    bram.io.enb := true.B
  }

  switch(state) {
    is(sIdle) {
      when(dmaEnable) {
        dmaSPAdder0.io.a     := io.dmaDstAddr
        dmaSPAdder0.io.b     := io.dmaDstOffset
        dmaSPAdder0.io.start := true.B
        when(dmaSPAdder0.io.valid) {
          srcPtr := io.dmaSrcAddr
          dstPtr := dmaSPAdder0.io.out
          remaining := io.dmaLength
          state := sUpdateBytes
        }
      }
    }

    is(sUpdateBytes) {
      dmaSPSubtractor.io.a     := 8.U
      dmaSPSubtractor.io.b     := Mux(srcOffset > dstOffset, srcOffset, dstOffset)
      dmaSPSubtractor.io.start := true.B
      when(dmaSPSubtractor.io.valid) {
        bytesLeft := dmaSPSubtractor.io.out
        state := sUpdatePtr
      }
    }

    is(sUpdatePtr) {
      dmaSPAdder0.io.a     := srcPtr
      dmaSPAdder0.io.b     := byteCount
      dmaSPAdder0.io.start := true.B
      dmaSPAdder1.io.a     := dstPtr
      dmaSPAdder1.io.b     := byteCount
      dmaSPAdder1.io.start := true.B
      when(dmaSPAdder0.io.valid & dmaSPAdder1.io.valid) {
        nextSrcPtr := dmaSPAdder0.io.out
        nextDstPtr := dmaSPAdder1.io.out

        // read request
        bram.io.addra := srcWordAddr
        bram.io.addrb := dstWordAddr

        state := sReadResp
      }
    }

    is(sReadResp) {
      readSrcData := bram.io.douta
      readDstData := bram.io.doutb

      //maskBits := ((1.U(64.W) << (byteCount << 3)) - 1.U)
      val bitCount = byteCount << shiftAmount
      maskBits := VecInit(Seq.tabulate(128)(i =>
        (i.U < bitCount)
      )).asUInt

      state := sMask
    }

    is(sMask) {
      // maskBits := maskBits << (dstOffset << 3)
      // srcShifted := (readSrcData >> (srcOffset << 3)) & maskBits
      maskBits := VecInit(Seq.tabulate(128)(i =>
        Mux(i.U >= dstOffsetBits_r & (i.U - dstOffsetBits_r) < 128.U,
            maskBits(i.U - dstOffsetBits_r),
            0.U
        )
      )).asUInt

      srcShifted := VecInit(Seq.tabulate(128)(i =>
        Mux(i.U + srcOffsetBits_r < 128.U, readSrcData(i.U + srcOffsetBits_r), 0.U)
      )).asUInt & maskBits

      state := sUpdateRemaining
    }
    
    is(sUpdateRemaining) {
      dmaSPSubtractor.io.a     := remaining
      dmaSPSubtractor.io.b     := byteCount
      dmaSPSubtractor.io.start := true.B
      when(dmaSPSubtractor.io.valid) {
        nextRemaining := dmaSPSubtractor.io.out
        state := sWrite
      }
    }

    is(sWrite) {
      bram.io.addra := dstWordAddr
      bram.io.dina := newData
      bram.io.wea := true.B

      srcPtr := nextSrcPtr
      dstPtr := nextDstPtr
      remaining := nextRemaining
      when(remaining <= byteCount) {
        state := sDone
      }.otherwise {
        state := sUpdateBytes
      }
    }

    is(sDone) {
      when(!dmaEnable) {
        state := sIdle
      }
    }
  }
}
*/