package GeneralRegFileToBRAM

import chisel3._
import chiseltest._
import chiseltest.simulator.WriteVcdAnnotation
import org.scalatest.flatspec.AnyFlatSpec

/*
class AXIWrapperChiselGeneratedFactorialTestBench extends AnyFlatSpec with ChiselScalatestTester {
  "AXIWrapperChiselGeneratedFactorialTestBench " should "work" in {
    test(new AXIWrapperChiselGeneratedFactorialTest()).withAnnotations(Seq(WriteVcdAnnotation, VerilatorBackendAnnotation)) { dut =>
      dut.clock.setTimeout(3000)

      dut.reset.poke(false.B)
      for (i <- 0 until (5)) {
        dut.clock.step()
      }
      dut.reset.poke(true.B)
      dut.clock.step()

      // write testnum
      dut.io.S_AXI_AWVALID.poke(true.B)
      dut.io.S_AXI_AWADDR.poke("h40000008".U)
      dut.clock.step()

      dut.io.S_AXI_WVALID.poke(true.B)
      dut.io.S_AXI_WDATA.poke((-1).S)
      dut.io.S_AXI_WSTRB.poke("b1111".U)
      dut.clock.step()

      dut.io.S_AXI_BREADY.poke(true.B)
      dut.clock.step()

      dut.io.S_AXI_AWVALID.poke(true.B)
      dut.io.S_AXI_AWADDR.poke("h4000000C".U)
      dut.clock.step()

      dut.io.S_AXI_WVALID.poke(true.B)
      dut.io.S_AXI_WDATA.poke((-1).S)
      dut.io.S_AXI_WSTRB.poke("b1111".U)
      dut.clock.step()

      dut.io.S_AXI_BREADY.poke(true.B)
      dut.clock.step()

      // write valid signal
      dut.io.S_AXI_AWVALID.poke(true.B)
      dut.io.S_AXI_AWADDR.poke("h40000000".U)
      dut.clock.step()

      dut.io.S_AXI_WVALID.poke(true.B)
      dut.io.S_AXI_WDATA.poke(1.S)
      dut.io.S_AXI_WSTRB.poke("b1111".U)
      dut.clock.step()

      dut.io.S_AXI_BREADY.poke(true.B)
      dut.clock.step()

      // disable all AXI write signal
      dut.io.S_AXI_AWVALID.poke(false.B)
      dut.io.S_AXI_WVALID.poke(false.B)
      dut.io.S_AXI_BREADY.poke(false.B)

      for(i <- 0 until 700) {
        dut.clock.step()
      }

      // read ready signals
      dut.io.S_AXI_ARVALID.poke(true.B)
      dut.io.S_AXI_ARADDR.poke("h40000004".U)
      dut.clock.step()

      dut.io.S_AXI_RREADY.poke(true.B)
      dut.clock.step()

      // read ascii code from offset 22
      for(i <- 0 until 45) {
        dut.io.S_AXI_ARVALID.poke(true.B)
        dut.io.S_AXI_ARADDR.poke(("h" + (0x40000008L + i * 4).toHexString).U)
        dut.clock.step()

        dut.io.S_AXI_RREADY.poke(true.B)
        dut.clock.step()
      }

      dut.clock.step(20)

    }
  }
}
class AXIWrapperChiselGeneratedGlobalTestBench extends AnyFlatSpec with ChiselScalatestTester {
  "AXIWrapperChiselGeneratedGlobalTestBench " should "work" in {
    test(new AXIWrapperChiselGeneratedGlobalTest()).withAnnotations(Seq(WriteVcdAnnotation, VerilatorBackendAnnotation)) { dut =>
      dut.clock.setTimeout(3000)

      dut.reset.poke(false.B)
      for (i <- 0 until (5)) {
        dut.clock.step()
      }
      dut.reset.poke(true.B)
      dut.clock.step()

      // write testnum
      dut.io.S_AXI_AWVALID.poke(true.B)
      dut.io.S_AXI_AWADDR.poke("h40000008".U)
      dut.clock.step()

      dut.io.S_AXI_WVALID.poke(true.B)
      dut.io.S_AXI_WDATA.poke((-1).S)
      dut.io.S_AXI_WSTRB.poke("b1111".U)
      dut.clock.step()

      dut.io.S_AXI_BREADY.poke(true.B)
      dut.clock.step()

      dut.io.S_AXI_AWVALID.poke(true.B)
      dut.io.S_AXI_AWADDR.poke("h4000000C".U)
      dut.clock.step()

      dut.io.S_AXI_WVALID.poke(true.B)
      dut.io.S_AXI_WDATA.poke((-1).S)
      dut.io.S_AXI_WSTRB.poke("b1111".U)
      dut.clock.step()

      dut.io.S_AXI_BREADY.poke(true.B)
      dut.clock.step()

      // write valid signal
      dut.io.S_AXI_AWVALID.poke(true.B)
      dut.io.S_AXI_AWADDR.poke("h40000000".U)
      dut.clock.step()

      dut.io.S_AXI_WVALID.poke(true.B)
      dut.io.S_AXI_WDATA.poke(1.S)
      dut.io.S_AXI_WSTRB.poke("b1111".U)
      dut.clock.step()

      dut.io.S_AXI_BREADY.poke(true.B)
      dut.clock.step()

      // disable all AXI write signal
      dut.io.S_AXI_AWVALID.poke(false.B)
      dut.io.S_AXI_WVALID.poke(false.B)
      dut.io.S_AXI_BREADY.poke(false.B)

      for(i <- 0 until 200) {
        dut.clock.step()
      }

      // read ready signals
      dut.io.S_AXI_ARVALID.poke(true.B)
      dut.io.S_AXI_ARADDR.poke("h40000004".U)
      dut.clock.step()

      dut.io.S_AXI_RREADY.poke(true.B)
      dut.clock.step()

      // read ascii code from offset 22
      for(i <- 0 until 45) {
        dut.io.S_AXI_ARVALID.poke(true.B)
        dut.io.S_AXI_ARADDR.poke(("h" + (0x40000008L + i * 4).toHexString).U)
        dut.clock.step()

        dut.io.S_AXI_RREADY.poke(true.B)
        dut.clock.step()
      }

      dut.clock.step(20)

    }
  }
}

class AXIWrapperChiselGeneratedAssertTestBench extends AnyFlatSpec with ChiselScalatestTester {
  "AXIWrapperChiselGeneratedAssertTestBench " should "work" in {
    test(new AXIWrapperChiselGeneratedBarTest()).withAnnotations(Seq(WriteVcdAnnotation, VerilatorBackendAnnotation)) { dut =>
      dut.clock.setTimeout(3000)

      dut.reset.poke(false.B)
      for (i <- 0 until (5)) {
        dut.clock.step()
      }
      dut.reset.poke(true.B)
      dut.clock.step()

      // write testnum
      dut.io.S_AXI_AWVALID.poke(true.B)
      dut.io.S_AXI_AWADDR.poke("h40000008".U)
      dut.clock.step()

      dut.io.S_AXI_WVALID.poke(true.B)
      dut.io.S_AXI_WDATA.poke((-1).S)
      dut.io.S_AXI_WSTRB.poke("b1111".U)
      dut.clock.step()

      dut.io.S_AXI_BREADY.poke(true.B)
      dut.clock.step()

      dut.io.S_AXI_AWVALID.poke(true.B)
      dut.io.S_AXI_AWADDR.poke("h4000000C".U)
      dut.clock.step()

      dut.io.S_AXI_WVALID.poke(true.B)
      dut.io.S_AXI_WDATA.poke((-1).S)
      dut.io.S_AXI_WSTRB.poke("b1111".U)
      dut.clock.step()

      dut.io.S_AXI_BREADY.poke(true.B)
      dut.clock.step()

      // write valid signal
      dut.io.S_AXI_AWVALID.poke(true.B)
      dut.io.S_AXI_AWADDR.poke("h40000000".U)
      dut.clock.step()

      dut.io.S_AXI_WVALID.poke(true.B)
      dut.io.S_AXI_WDATA.poke(1.S)
      dut.io.S_AXI_WSTRB.poke("b1111".U)
      dut.clock.step()

      dut.io.S_AXI_BREADY.poke(true.B)
      dut.clock.step()

      // disable all AXI write signal
      dut.io.S_AXI_AWVALID.poke(false.B)
      dut.io.S_AXI_WVALID.poke(false.B)
      dut.io.S_AXI_BREADY.poke(false.B)

      for(i <- 0 until 200) {
        dut.clock.step()
      }

      // read ready signals
      dut.io.S_AXI_ARVALID.poke(true.B)
      dut.io.S_AXI_ARADDR.poke("h40000004".U)
      dut.clock.step()

      dut.io.S_AXI_RREADY.poke(true.B)
      dut.clock.step()

      // read ascii code from offset 22
      for(i <- 0 until 45) {
        dut.io.S_AXI_ARVALID.poke(true.B)
        dut.io.S_AXI_ARADDR.poke(("h" + (0x40000008L + i * 4).toHexString).U)
        dut.clock.step()

        dut.io.S_AXI_RREADY.poke(true.B)
        dut.clock.step()
      }

      dut.clock.step(20)

    }
  }
}
*/