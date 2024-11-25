package AXILiteWrapper

import chisel3._
import chisel3.util._
import chisel3.experimental._

class AXILiteSHA1 (val LITE_ADDR_WIDTH: Int = 32, val LITE_DATA_WIDTH: Int = 32) extends BlackBox with HasBlackBoxResource {
  val io = IO(new Bundle {
    val S_AXI_ACLK = Input(Clock())
    val S_AXI_ARESETN = Input(Bool())

    val S_AXI_AWREADY = Output(UInt(1.W))
    val S_AXI_AWVALID = Input(UInt(1.W))
    val S_AXI_AWADDR = Input(UInt(LITE_ADDR_WIDTH.W))
    val S_AXI_AWPROT = Input(UInt(3.W))

    val S_AXI_WREADY = Output(UInt(1.W))
    val S_AXI_WVALID = Input(UInt(1.W))
    val S_AXI_WDATA = Input(SInt(LITE_DATA_WIDTH.W))
    val S_AXI_WSTRB = Input(UInt((LITE_DATA_WIDTH/8).W))

    val S_AXI_BREADY = Input(UInt(1.W))
    val S_AXI_BVALID = Output(UInt(1.W))
    val S_AXI_BRESP = Output(UInt(2.W))

    val S_AXI_ARREADY = Output(UInt(1.W))
    val S_AXI_ARVALID = Input(UInt(1.W))
    val S_AXI_ARADDR = Input(UInt(LITE_ADDR_WIDTH.W))
    val S_AXI_ARPROT = Input(UInt(3.W))

    val S_AXI_RREADY = Input(UInt(1.W))
    val S_AXI_RVALID = Output(UInt(1.W))
    val S_AXI_RDATA = Output(SInt(LITE_DATA_WIDTH.W))
    val S_AXI_RRESP = Output(UInt(2.W))
  })

  addResource("/axi_lite_SHA1.v")
  addResource("/digest.v")
}

class AXILiteWrapperSHA1(val LITE_ADDR_WIDTH: Int = 32, val LITE_DATA_WIDTH: Int = 32) extends Module {
  val io = IO(new Bundle {
    val S_AXI_ARESETN = Input(Bool())

    val S_AXI_AWREADY = Output(UInt(1.W))
    val S_AXI_AWVALID = Input(UInt(1.W))
    val S_AXI_AWADDR = Input(UInt(LITE_ADDR_WIDTH.W))
    val S_AXI_AWPROT = Input(UInt(3.W))

    val S_AXI_WREADY = Output(UInt(1.W))
    val S_AXI_WVALID = Input(UInt(1.W))
    val S_AXI_WDATA = Input(SInt(LITE_DATA_WIDTH.W))
    val S_AXI_WSTRB = Input(UInt((LITE_DATA_WIDTH/8).W))

    val S_AXI_BREADY = Input(UInt(1.W))
    val S_AXI_BVALID = Output(UInt(1.W))
    val S_AXI_BRESP = Output(UInt(2.W))

    val S_AXI_ARREADY = Output(UInt(1.W))
    val S_AXI_ARVALID = Input(UInt(1.W))
    val S_AXI_ARADDR = Input(UInt(LITE_ADDR_WIDTH.W))
    val S_AXI_ARPROT = Input(UInt(3.W))

    val S_AXI_RREADY = Input(UInt(1.W))
    val S_AXI_RVALID = Output(UInt(1.W))
    val S_AXI_RDATA = Output(SInt(LITE_DATA_WIDTH.W))
    val S_AXI_RRESP = Output(UInt(2.W))
  })

  val axiLite = Module(new AXILiteSHA1())

  axiLite.io.S_AXI_ACLK := clock
  axiLite.io.S_AXI_ARESETN := reset

  io.S_AXI_AWREADY := axiLite.io.S_AXI_AWREADY
  axiLite.io.S_AXI_AWVALID := io.S_AXI_AWVALID
  axiLite.io.S_AXI_AWADDR := io.S_AXI_AWADDR
  axiLite.io.S_AXI_AWPROT := io.S_AXI_AWPROT

  io.S_AXI_WREADY := axiLite.io.S_AXI_WREADY
  axiLite.io.S_AXI_WVALID := io.S_AXI_WVALID
  axiLite.io.S_AXI_WDATA := io.S_AXI_WDATA
  axiLite.io.S_AXI_WSTRB := io.S_AXI_WSTRB

  axiLite.io.S_AXI_BREADY := io.S_AXI_BREADY
  io.S_AXI_BVALID := axiLite.io.S_AXI_BVALID
  io.S_AXI_BRESP := axiLite.io.S_AXI_BRESP

  io.S_AXI_ARREADY := axiLite.io.S_AXI_ARREADY
  axiLite.io.S_AXI_ARVALID := io.S_AXI_ARVALID
  axiLite.io.S_AXI_ARADDR := io.S_AXI_ARADDR
  axiLite.io.S_AXI_ARPROT := io.S_AXI_ARPROT

  axiLite.io.S_AXI_RREADY := io.S_AXI_RREADY
  io.S_AXI_RVALID := axiLite.io.S_AXI_RVALID
  io.S_AXI_RDATA := axiLite.io.S_AXI_RDATA
  io.S_AXI_RRESP := axiLite.io.S_AXI_RRESP

}
