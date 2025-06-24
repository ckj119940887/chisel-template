package GeneralRegFileToBRAM 

import chisel3._
import chisel3.util._
import chisel3.experimental._

class AdderUnsigned (val width: Int = 64) extends Module {
    val io = IO(new Bundle{
        val a = Input(UInt(width.W))
        val b = Input(UInt(width.W))
        val op = Input(Bool())
        val out = Output(UInt(width.W))
    })

    io.out := Mux(io.op, io.a + io.b, io.a - io.b)
}

class AdderSigned (val width: Int = 64) extends Module {
    val io = IO(new Bundle{
        val a = Input(SInt(width.W))
        val b = Input(SInt(width.W))
        val op = Input(Bool())
        val out = Output(SInt(width.W))
    })

    io.out := Mux(io.op, io.a + io.b, io.a - io.b)
}


class AndUnsigned(val width:Int = 64) extends Module {
    val io = IO(new Bundle{
        val a = Input(UInt(width.W))
        val b = Input(UInt(width.W))
        val start = Input(Bool())
        val valid = Output(Bool())
        val out = Output(UInt(width.W))
    })
    io.valid := RegNext(io.start)
    io.out := RegNext(io.a & io.b)
}

class AndSigned(val width:Int = 64) extends Module {
    val io = IO(new Bundle{
        val a = Input(SInt(width.W))
        val b = Input(SInt(width.W))
        val out = Output(SInt(width.W))
    })
    io.out := io.a & io.b
}

class OrUnsigned(val width:Int = 64) extends Module {
    val io = IO(new Bundle{
        val a = Input(UInt(width.W))
        val b = Input(UInt(width.W))
        val out = Output(UInt(width.W))
    })
    io.out := io.a | io.b
}

class OrSigned(val width:Int = 64) extends Module {
    val io = IO(new Bundle{
        val a = Input(SInt(width.W))
        val b = Input(SInt(width.W))
        val out = Output(SInt(width.W))
    })
    io.out := io.a | io.b
}

class XorUnsigned(val width:Int = 64) extends Module {
    val io = IO(new Bundle{
        val a = Input(UInt(width.W))
        val b = Input(UInt(width.W))
        val out = Output(UInt(width.W))
    })
    io.out := io.a ^ io.b
}

class XorSigned(val width:Int = 64) extends Module {
    val io = IO(new Bundle{
        val a = Input(SInt(width.W))
        val b = Input(SInt(width.W))
        val out = Output(SInt(width.W))
    })
    io.out := io.a ^ io.b
}

class EqUnsigned(val width:Int = 64) extends Module {
    val io = IO(new Bundle{
        val a = Input(UInt(width.W))
        val b = Input(UInt(width.W))
        val out = Output(Bool())
    })
    io.out := io.a === io.b
}

class EqSigned(val width:Int = 64) extends Module {
    val io = IO(new Bundle{
        val a = Input(SInt(width.W))
        val b = Input(SInt(width.W))
        val out = Output(Bool())
    })
    io.out := io.a === io.b
}

class NeUnsigned(val width:Int = 64) extends Module {
    val io = IO(new Bundle{
        val a = Input(UInt(width.W))
        val b = Input(UInt(width.W))
        val out = Output(Bool())
    })
    io.out := io.a =/= io.b
}

class NeSigned(val width:Int = 64) extends Module {
    val io = IO(new Bundle{
        val a = Input(SInt(width.W))
        val b = Input(SInt(width.W))
        val out = Output(Bool())
    })
    io.out := io.a =/= io.b
}

class GeUnsigned(val width:Int = 64) extends Module {
    val io = IO(new Bundle{
        val a = Input(UInt(width.W))
        val b = Input(UInt(width.W))
        val out = Output(Bool())
    })
    io.out := io.a >= io.b
}

class GeSigned(val width:Int = 64) extends Module {
    val io = IO(new Bundle{
        val a = Input(SInt(width.W))
        val b = Input(SInt(width.W))
        val out = Output(Bool())
    })
    io.out := io.a >= io.b
}

class GtUnsigned(val width:Int = 64) extends Module {
    val io = IO(new Bundle{
        val a = Input(UInt(width.W))
        val b = Input(UInt(width.W))
        val out = Output(Bool())
    })
    io.out := io.a > io.b
}

class GtSigned(val width:Int = 64) extends Module {
    val io = IO(new Bundle{
        val a = Input(SInt(width.W))
        val b = Input(SInt(width.W))
        val out = Output(Bool())
    })
    io.out := io.a > io.b
}

class LeUnsigned(val width:Int = 64) extends Module {
    val io = IO(new Bundle{
        val a = Input(UInt(width.W))
        val b = Input(UInt(width.W))
        val out = Output(Bool())
    })
    io.out := io.a <= io.b
}

class LeSigned(val width:Int = 64) extends Module {
    val io = IO(new Bundle{
        val a = Input(SInt(width.W))
        val b = Input(SInt(width.W))
        val out = Output(Bool())
    })
    io.out := io.a <= io.b
}

class LtUnsigned(val width:Int = 64) extends Module {
    val io = IO(new Bundle{
        val a = Input(UInt(width.W))
        val b = Input(UInt(width.W))
        val out = Output(Bool())
    })
    io.out := io.a < io.b
}

class LtSigned(val width:Int = 64) extends Module {
    val io = IO(new Bundle{
        val a = Input(SInt(width.W))
        val b = Input(SInt(width.W))
        val out = Output(Bool())
    })
    io.out := io.a < io.b
}

class ShrUnsigned(val width:Int = 64) extends Module {
    val io = IO(new Bundle{
        val a = Input(UInt(width.W))
        val b = Input(UInt(width.W))
        val out = Output(UInt(width.W))
    })
    io.out := Mux(io.b >= 64.U, 0.U, io.a >> io.b(6,0))
}

class ShrSigned(val width:Int = 64) extends Module {
    val io = IO(new Bundle{
        val a = Input(SInt(width.W))
        val b = Input(UInt(width.W))
        val out = Output(SInt(width.W))
    })
    io.out := Mux(io.b >= 64.U, io.a >> 64.U, io.a >> io.b(6,0))
}

class ShlUnsigned(val width:Int = 64) extends Module {
    val io = IO(new Bundle{
        val a = Input(UInt(width.W))
        val b = Input(UInt(width.W))
        val out = Output(UInt(width.W))
    })
    io.out := Mux(io.b >= 64.U, 0.U, io.a << io.b(6,0))
}

class ShlSigned(val width:Int = 64) extends Module {
    val io = IO(new Bundle{
        val a = Input(SInt(width.W))
        val b = Input(UInt(width.W))
        val out = Output(SInt(width.W))
    })
    io.out := Mux(io.b >= 64.U, 0.U, io.a << io.b(6,0))
}

class UshrUnsigned(val width:Int = 64) extends Module {
    val io = IO(new Bundle{
        val a = Input(UInt(width.W))
        val b = Input(UInt(width.W))
        val out = Output(UInt(width.W))
    })
    io.out := Mux(io.b >= 64.U, 0.U, io.a >> io.b(6,0))
}

class UshrSigned(val width:Int = 64) extends Module {
    val io = IO(new Bundle{
        val a = Input(SInt(width.W))
        val b = Input(UInt(width.W))
        val out = Output(SInt(width.W))
    })
    io.out := Mux(io.b >= 64.U, 0.U, io.a.asUInt >> io.b(6,0)).asSInt
}

class TestPad(val width:Int = 64) extends Module {
    val io = IO(new Bundle{
        val in = Input(SInt(16.W))
        val out = Output(SInt(width.W))
    })
    io.out := io.in.pad(width)
}

class Div(val width:Int = 64) extends Module {
    val io = IO(new Bundle{
        val a = Input(SInt(width.W))
        val b = Input(SInt(width.W))
        val out = Output(SInt(width.W))
    })
    io.out := io.a / io.b
}

class Rem(val width:Int = 64) extends Module {
    val io = IO(new Bundle{
        val a = Input(SInt(width.W))
        val b = Input(SInt(width.W))
        val out = Output(SInt(width.W))
    })
    io.out := io.a % io.b
}

class WrongTestShift() extends Module {
    val io = IO(new Bundle{
        val a = Input(UInt(16.W))
        val out = Output(UInt(16.W))
    })
    val reg = RegInit(1024.U(16.W))

    reg := reg.asUInt << (3.U(16.W) & "b1111111".U)

    io.out := reg
}

class IndexAdder(val width: Int = 64) extends Module {
    val io = IO(new Bundle{
        val a = Input(UInt(width.W))
        val b = Input(UInt(width.W))
        val start = Input(Bool())
        val out = Output(UInt(width.W))
        val valid = Output(Bool())
    })

    val add = Module(new XilinxIndexAdderWrapper)
    add.io.clk := clock.asBool
    add.io.ce  := io.start
    add.io.A   := io.a
    add.io.B   := io.b
    io.valid   := add.io.valid
    io.out     := add.io.S

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

class XilinxIndexAdderWrapper extends BlackBox with HasBlackBoxResource {
  val io = IO(new Bundle {
    val clk   = Input(Bool())
    val ce    = Input(Bool())
    val A     = Input(UInt(16.W))
    val B     = Input(UInt(16.W))
    val valid = Output(Bool())
    val S     = Output(UInt(16.W))
  })

  addResource("/verilog/XilinxIndexAdderWrapper.v")
}

class XilinxIndexMultiplierWrapper extends BlackBox with HasBlackBoxResource {
  val io = IO(new Bundle {
    val clk   = Input(Bool())
    val ce    = Input(Bool())
    val A     = Input(UInt(16.W))
    val B     = Input(UInt(16.W))
    val valid = Output(Bool())
    val P     = Output(UInt(16.W))
  })

  addResource("/verilog/XilinxIndexMultiplierWrapper.v")
}

class IndexMultiplier(val width: Int = 64) extends Module {
    val io = IO(new Bundle{
        val a     = Input(UInt(width.W))
        val b     = Input(UInt(width.W))
        val start = Input(Bool())
        val out   = Output(UInt(width.W))
        val valid = Output(Bool())
    })

    val mult = Module(new XilinxIndexMultiplierWrapper)
    mult.io.clk := clock.asBool
    mult.io.ce  := io.start
    mult.io.A   := io.a
    mult.io.B   := io.b
    io.valid    := mult.io.valid
    io.out      := mult.io.P

    /*
    val state = RegInit(0.U(4.W))
    val regA = Reg(UInt(width.W))
    val regB = Reg(UInt(width.W))
    val result = Reg(UInt(width.W))

    io.valid := Mux(state === 9.U, true.B, false.B)
    io.out := Mux(state === 9.U, result, 0.U)

    switch(state) {
        is(0.U) {
            state := Mux(io.start, 1.U, 0.U)
            regA := Mux(io.start, io.a, regA)
            regB := Mux(io.start, io.b, regB)
        }
        is(1.U) {
            state := 2.U
        }
        is(2.U) {
            state := 3.U
        }
        is(3.U) {
            state := 4.U
        }
        is(4.U) {
            state := 5.U
        }
        is(5.U) {
            state := 6.U
        }
        is(6.U) {
            state := 7.U
        }
        is(7.U) {
            state := 8.U
        }
        is(8.U) {
            result := regA * regB 
            state := 9.U
        }
        is(9.U) {
            state := 0.U
        }
    }
    */
}

class Indexer(val width: Int = 16) extends Module {
    val io = IO(new Bundle{
        val baseOffset = Input(UInt(width.W))
        val dataOffset = Input(UInt(width.W))
        val index = Input(UInt(width.W))
        val elementSize = Input(UInt(width.W))
        val mask = Input(UInt(width.W))
        val ready = Input(Bool())
        val valid = Output(Bool())
        val out = Output(UInt(width.W))
    })


    val sIdle :: sAdd1 :: sMult :: sAdd2 :: sEnd :: Nil = Enum(5)
    val stateReg        = RegInit(sIdle)
    val regBaseAddr     = Reg(UInt(width.W))
    val regIndex        = Reg(UInt(width.W))
    val regElementSize  = Reg(UInt(width.W))
    val regMult         = Reg(UInt(width.W))
    val regMask         = Reg(UInt(width.W))
    val result          = Reg(UInt(width.W))

    val adder           = Module(new IndexAdder(width))
    val multiplier      = Module(new IndexMultiplier(width))

    adder.io.a          := 0.U
    adder.io.b          := 0.U
    adder.io.start      := false.B
    multiplier.io.a     := 0.U
    multiplier.io.b     := 0.U
    multiplier.io.start := false.B

    switch(stateReg) {
        is(sIdle) {
            stateReg       := Mux(io.ready, sAdd1, sIdle)

            regIndex       := io.index
            regElementSize := io.elementSize
            regMask        := io.mask
        }
        is(sAdd1) {
            adder.io.a     := io.baseOffset
            adder.io.b     := io.dataOffset
            adder.io.start := true.B

            when(adder.io.valid) {
                adder.io.start      := false.B

                stateReg            := sMult
                regBaseAddr         := adder.io.out
            }
        }
        is(sMult) {
            multiplier.io.a     := regIndex
            multiplier.io.b     := regElementSize
            multiplier.io.start := true.B

            when(multiplier.io.valid) {
                multiplier.io.start := false.B
                regMult             := multiplier.io.out & regMask
                stateReg            := sAdd2
            }
        }
        is(sAdd2) {
            adder.io.a     := regBaseAddr 
            adder.io.b     := regMult
            adder.io.start := true.B

            when(adder.io.valid) {
              adder.io.start := false.B
              result         := adder.io.out
              stateReg       := sEnd
            }
        }
        is(sEnd) {
            stateReg := sIdle
        }
    }

    io.out   := Mux(stateReg === sEnd, result, 0.U)
    io.valid := Mux(stateReg === sEnd, true.B, false.B)

    /*
    val stateReg = RegInit(0.U(2.W))
    switch(stateReg) {
        is(0.U) {
            stateReg := Mux(io.ready, 1.U, 0.U)
        }
        is(1.U) {
            stateReg := 2.U
        }
        is(2.U) {
            stateReg := 3.U
        }
        is(3.U) {
            stateReg := Mux(!io.ready, 0.U, 3.U)
        }
    }

    io.valid := Mux(stateReg === 3.U, true.B, false.B)

    val regBaseAddr = RegNext(io.baseOffset + io.dataOffset)

    val regIndex = RegNext(io.index)
    val regMult = RegNext(regIndex * io.elementSize)

    io.out := RegNext(regBaseAddr + (regMult & io.mask))
    */
}