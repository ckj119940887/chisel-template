package GeneralRegFileToBRAM 

import chisel3._
import chiseltest._
import chiseltest.simulator.WriteVcdAnnotation
import org.scalatest.flatspec.AnyFlatSpec

class GeneralRegFileToBRAMTestBench extends AnyFlatSpec with ChiselScalatestTester {
  "GeneralRegFileToBRAMTestBench" should "work" in {
    test(new Top(ADDR_WIDTH = 11, DATA_WIDTH = 32)).withAnnotations(Seq(WriteVcdAnnotation, VerilatorBackendAnnotation)) { dut =>
      dut.clock.setTimeout(3000)

      dut.reset.poke(true.B)
      for (i <- 0 until (5)) {
        dut.clock.step()
      }
      dut.reset.poke(false.B)

      // initialize DualPortBRAM
      dut.io.topEn.poke(true.B)
      dut.clock.step()

      dut.io.topWe.poke(true.B)
      dut.io.topAddr.poke(0.U)
      dut.io.topDin.poke(10.U)
      dut.clock.step()

      dut.io.topWe.poke(true.B)
      dut.io.topAddr.poke(1.U)
      dut.io.topDin.poke(11.U)
      dut.clock.step()

      dut.io.topWe.poke(true.B)
      dut.io.topAddr.poke(2.U)
      dut.io.topDin.poke(12.U)
      dut.clock.step()

      dut.io.topWe.poke(true.B)
      dut.io.topAddr.poke(3.U)
      dut.io.topDin.poke(13.U)
      dut.clock.step()

      dut.io.topWe.poke(true.B)
      dut.io.topAddr.poke(4.U)
      dut.io.topDin.poke(14.U)
      dut.clock.step()

      dut.io.topWe.poke(false.B)
      dut.clock.step()

      dut.io.startAddr.poke(0.U)
      dut.io.valid.poke(true.B)
      dut.io.resultAddr.poke(20.U)

      for(i <- 0 until 10) {
        dut.clock.step()
      }

    }
  }
}

class AXIWrapperChiselGeneralRegFileToBRAMTestBench extends AnyFlatSpec with ChiselScalatestTester {
  "AXIWrapperChiselGeneralRegFileToBRAM" should "work" in {
    test(new AXIWrapperChiselGeneralRegFileToBRAM(C_S_AXI_ADDR_WIDTH= 12, C_S_AXI_DATA_WIDTH= 32)).withAnnotations(Seq(WriteVcdAnnotation, VerilatorBackendAnnotation)) { dut =>
      dut.clock.setTimeout(3000)

      dut.reset.poke(false.B)
      for (i <- 0 until (5)) {
        dut.clock.step()
      }
      dut.reset.poke(true.B)

      // write startAddr signal
      dut.io.S_AXI_AWVALID.poke(true.B)
      dut.io.S_AXI_AWADDR.poke("h000".U)
      dut.clock.step()

      dut.io.S_AXI_WVALID.poke(true.B)
      dut.io.S_AXI_WDATA.poke(0.U)
      dut.io.S_AXI_WSTRB.poke("b1111".U)
      dut.clock.step()

      dut.io.S_AXI_BREADY.poke(true.B)
      dut.clock.step()

      // write resultAddr signal
      dut.io.S_AXI_AWVALID.poke(true.B)
      dut.io.S_AXI_AWADDR.poke("h004".U)
      dut.clock.step()

      dut.io.S_AXI_WVALID.poke(true.B)
      dut.io.S_AXI_WDATA.poke(20.U)
      dut.io.S_AXI_WSTRB.poke("b1111".U)
      dut.clock.step()

      dut.io.S_AXI_BREADY.poke(true.B)
      dut.clock.step()

      // write valid signal
      dut.io.S_AXI_AWVALID.poke(true.B)
      dut.io.S_AXI_AWADDR.poke("h008".U)
      dut.clock.step()

      dut.io.S_AXI_WVALID.poke(true.B)
      dut.io.S_AXI_WDATA.poke(1.U)
      dut.io.S_AXI_WSTRB.poke("b1111".U)
      dut.clock.step()

      dut.io.S_AXI_BREADY.poke(true.B)
      dut.clock.step()

      for(i <- 0 until 10) {
        dut.clock.step()
      }

    }
  }
}