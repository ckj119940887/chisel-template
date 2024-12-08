package OpenAVRSim

import chisel3._
import chisel3.util._
import chisel3.experimental._

class top extends BlackBox with HasBlackBoxResource {
  val io = IO(new Bundle {
    val hw_clk = Input(Clock())

    val led = Output(UInt(8.W))
    val ftdi_rx = Input(UInt(1.W))
    val ftdi_tx = Output(UInt(1.W))
  })

  addResource("/open-avr/avr_core.v")
  addResource("/open-avr/avr_io_out.v")
  addResource("/open-avr/avr_io_timer.v")
  addResource("/open-avr/avr_io_uart.v")
  addResource("/open-avr/flash.v")
  addResource("/open-avr/ram.v")
  addResource("/open-avr/top.v")
}

class OpenAVRSim extends Module {
  val io = IO(new Bundle{
    val led = Output(UInt(8.W))
    val ftdi_rx = Input(UInt(1.W))
    val ftdi_tx = Output(UInt(1.W))
  })

  val openAVRSimTop = Module(new top)
  openAVRSimTop.io.hw_clk := clock
  io.led := openAVRSimTop.io.led
  openAVRSimTop.io.ftdi_rx := io.ftdi_rx
  io.ftdi_tx := openAVRSimTop.io.ftdi_tx
}