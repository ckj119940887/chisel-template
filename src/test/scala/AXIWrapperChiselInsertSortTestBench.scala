package AXIWrapperChisel

import chisel3._
import chiseltest._
import chiseltest.simulator.WriteVcdAnnotation
import org.scalatest.flatspec.AnyFlatSpec

class AXIWrapperChiselInsertSortTestBench extends AnyFlatSpec with ChiselScalatestTester {
  "AXIWrapperChiselInsertSort" should "work" in {
    test(new AXIWrapperChiselInsertSort(C_S_AXI_ADDR_WIDTH = 32, C_S_AXI_DATA_WIDTH = 32)).withAnnotations(Seq(WriteVcdAnnotation, VerilatorBackendAnnotation)) { dut =>
      dut.clock.setTimeout(3000)

      dut.reset.poke(false.B)
      for (i <- 0 until (5)) {
        dut.clock.step()
      }
      dut.reset.poke(true.B)

      for(i <- 0 until 10) {
        dut.io.S_AXI_AWVALID.poke(true.B)
        dut.io.S_AXI_AWADDR.poke(("h" + (0x40000000L + i * 4).toHexString).U)
        dut.clock.step()

        dut.io.S_AXI_WVALID.poke(true.B)
        dut.io.S_AXI_WDATA.poke((10-i).S)
        dut.io.S_AXI_WSTRB.poke("b1111".U)
        dut.clock.step()

        dut.io.S_AXI_BREADY.poke(true.B)
        dut.clock.step()
      }

      // write valid signal
      dut.io.S_AXI_AWVALID.poke(true.B)
      dut.io.S_AXI_AWADDR.poke("h400000A0".U)
      dut.clock.step()

      dut.io.S_AXI_WVALID.poke(true.B)
      dut.io.S_AXI_WDATA.poke(1.S)
      dut.io.S_AXI_WSTRB.poke("b1111".U)
      dut.clock.step()

      dut.io.S_AXI_BREADY.poke(true.B)
      dut.clock.step()

      dut.io.S_AXI_AWVALID.poke(false.B)
      dut.io.S_AXI_WVALID.poke(false.B)
      dut.io.S_AXI_BREADY.poke(false.B)

      for(i <- 0 until(1000)) {
        dut.clock.step()
      }

    }
  }
}