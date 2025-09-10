package TestArbiter

import chisel3._
import chisel3.util._
import chisel3.experimental._



class XilinxAdderUnsigned64Wrapper extends BlackBox with HasBlackBoxResource {
  val io = IO(new Bundle {
    val clk = Input(Bool())
    val ce = Input(Bool())
    val A = Input(UInt(64.W))
    val B = Input(UInt(64.W))
    val valid = Output(Bool())
    val S = Output(UInt(64.W))
  })

  addResource("/verilog/XilinxAdderUnsigned64Wrapper.v")
}


class XilinxAdderSigned64Wrapper extends BlackBox with HasBlackBoxResource {
  val io = IO(new Bundle {
    val clk = Input(Bool())
    val ce = Input(Bool())
    val A = Input(SInt(64.W))
    val B = Input(SInt(64.W))
    val valid = Output(Bool())
    val S = Output(SInt(64.W))
  })

  addResource("/verilog/XilinxAdderSigned64Wrapper.v")
}


class XilinxSubtractorUnsigned64Wrapper extends BlackBox with HasBlackBoxResource {
  val io = IO(new Bundle {
    val clk = Input(Bool())
    val ce = Input(Bool())
    val A = Input(UInt(64.W))
    val B = Input(UInt(64.W))
    val valid = Output(Bool())
    val S = Output(UInt(64.W))
  })

  addResource("/verilog/XilinxSubtractorUnsigned64Wrapper.v")
}


class XilinxSubtractorSigned64Wrapper extends BlackBox with HasBlackBoxResource {
  val io = IO(new Bundle {
    val clk = Input(Bool())
    val ce = Input(Bool())
    val A = Input(SInt(64.W))
    val B = Input(SInt(64.W))
    val valid = Output(Bool())
    val S = Output(SInt(64.W))
  })

  addResource("/verilog/XilinxSubtractorSigned64Wrapper.v")
}


class XilinxMultiplierUnsigned64Wrapper extends BlackBox with HasBlackBoxResource {
  val io = IO(new Bundle {
    val clk = Input(Bool())
    val ce = Input(Bool())
    val a = Input(UInt(64.W))
    val b = Input(UInt(64.W))
    val valid = Output(Bool())
    val p = Output(UInt(64.W))
  })

  addResource("/verilog/XilinxMultiplierUnsigned64Wrapper.v")
}


class XilinxMultiplierSigned64Wrapper extends BlackBox with HasBlackBoxResource {
  val io = IO(new Bundle {
    val clk = Input(Bool())
    val ce = Input(Bool())
    val a = Input(SInt(64.W))
    val b = Input(SInt(64.W))
    val valid = Output(Bool())
    val p = Output(SInt(64.W))
  })

  addResource("/verilog/XilinxMultiplierSigned64Wrapper.v")
}


class XilinxDividerUnsigned64Wrapper extends BlackBox with HasBlackBoxResource {
  val io = IO(new Bundle {
    val clock = Input(Bool())
    val resetn = Input(Bool())
    val a = Input(UInt(64.W))
    val b = Input(UInt(64.W))
    val start = Input(Bool())
    val valid = Output(Bool())
    val quotient = Output(UInt(64.W))
    val remainder = Output(UInt(64.W))
  })

  addResource("/verilog/XilinxDividerUnsigned64Wrapper.v")
}


class XilinxDividerSigned64Wrapper extends BlackBox with HasBlackBoxResource {
  val io = IO(new Bundle {
    val clock = Input(Bool())
    val resetn = Input(Bool())
    val a = Input(SInt(64.W))
    val b = Input(SInt(64.W))
    val start = Input(Bool())
    val valid = Output(Bool())
    val quotient = Output(SInt(64.W))
    val remainder = Output(SInt(64.W))
  })

  addResource("/verilog/XilinxDividerSigned64Wrapper.v")
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
