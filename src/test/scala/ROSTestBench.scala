package ROS

import chisel3._
import chiseltest._
import chiseltest.simulator.WriteVcdAnnotation
import org.scalatest.flatspec.AnyFlatSpec

class AXI4LiteSlaveTempTestBench extends AnyFlatSpec with ChiselScalatestTester {
  "AXI4LiteSlaveTempTestBench" should "work" in {
    test(new AXI4LiteSlaveTemp(C_S_AXI_DATA_WIDTH = 64, C_S_AXI_ADDR_WIDTH = 7)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.clock.setTimeout(3000)

      dut.reset.poke(true.B)
      for (i <- 0 until (5)) {
        dut.clock.step()
      }
      dut.reset.poke(false.B)
      dut.clock.step()

      dut.io.TMP_DATA.poke(5.U)
      dut.clock.step()

      dut.io.S_AXI_AWVALID.poke(true.B)
      dut.io.S_AXI_AWADDR.poke(0.U)
      dut.io.S_AXI_WVALID.poke(true.B)
      dut.io.S_AXI_WDATA.poke("h01".U)
      dut.io.S_AXI_WSTRB.poke("hff".U)
      dut.clock.step()

      dut.io.S_AXI_AWVALID.poke(false.B)
      dut.io.S_AXI_WVALID.poke(false.B)
      dut.io.S_AXI_BREADY.poke(true.B)
      dut.clock.step(2)

      dut.io.S_AXI_BREADY.poke(false.B)
      dut.clock.step()

      dut.io.S_AXI_AWVALID.poke(true.B)
      dut.io.S_AXI_AWADDR.poke(8.U)
      dut.io.S_AXI_WVALID.poke(true.B)
      dut.io.S_AXI_WDATA.poke("h02".U)
      dut.io.S_AXI_WSTRB.poke("hff".U)
      dut.clock.step()

      dut.io.S_AXI_AWVALID.poke(false.B)
      dut.io.S_AXI_WVALID.poke(false.B)
      dut.io.S_AXI_BREADY.poke(true.B)
      dut.clock.step(2)

      dut.io.S_AXI_BREADY.poke(false.B)
      dut.clock.step()

      // read logic
      dut.io.S_AXI_ARVALID.poke(true.B)
      dut.io.S_AXI_ARADDR.poke(0.U)
      dut.clock.step()

      dut.io.S_AXI_ARVALID.poke(false.B)
      dut.io.S_AXI_RREADY.poke(true.B)
      dut.clock.step(2)

      dut.io.S_AXI_RREADY.poke(false.B)
      dut.clock.step()

      dut.io.S_AXI_ARVALID.poke(true.B)
      dut.io.S_AXI_ARADDR.poke(8.U)
      dut.clock.step()

      dut.io.S_AXI_ARVALID.poke(false.B)
      dut.io.S_AXI_RREADY.poke(true.B)
      dut.clock.step(2)

      dut.io.S_AXI_RREADY.poke(false.B)
      dut.clock.step()

      for (i <- 0 until (10)) {
        dut.clock.step()
      }

    }
  }
}