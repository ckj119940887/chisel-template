package GeneralRegFileToBRAM 

import chisel3._
import chiseltest._
import chiseltest.simulator.WriteVcdAnnotation
import org.scalatest.flatspec.AnyFlatSpec

class GeneralRegFileToRegFileTestBench extends AnyFlatSpec with ChiselScalatestTester {
  "GeneralRegFileToRegFileTestBench" should "work" in {
    test(new GeneralRegFileToRegFile()).withAnnotations(Seq(WriteVcdAnnotation, VerilatorBackendAnnotation)) { dut =>
      dut.clock.setTimeout(3000)

      dut.reset.poke(true.B)
      for (i <- 0 until (5)) {
        dut.clock.step()
      }
      dut.reset.poke(false.B)

      // initialize array
      dut.io.i_array(0).poke(1.U)
      dut.io.i_array(1).poke(0.U)
      dut.io.i_array(2).poke(0.U)
      dut.io.i_array(3).poke(0.U)
      dut.io.i_array(4).poke(2.U)
      dut.io.i_array(5).poke(0.U)
      dut.io.i_array(6).poke(0.U)
      dut.io.i_array(7).poke(0.U)
      dut.io.i_array(8).poke(3.U)
      dut.io.i_array(9).poke(0.U)
      dut.io.i_array(10).poke(0.U)
      dut.io.i_array(11).poke(0.U)
      dut.io.i_array(12).poke(4.U)
      dut.io.i_array(13).poke(0.U)
      dut.io.i_array(14).poke(0.U)
      dut.io.i_array(15).poke(0.U)
      dut.clock.step()

      dut.io.valid.poke(true.B)
      dut.clock.step()

      for(i <- 0 until 10) {
        dut.clock.step()
      }

    }
  }
}

class AXIWrapperChiselGeneralRegFileToRegFileTestBench extends AnyFlatSpec with ChiselScalatestTester {
  "AXIWrapperChiselGeneralRegFileToRegFile" should "work" in {
    test(new AXIWrapperChiselGeneralRegFileToRegFile(C_S_AXI_ADDR_WIDTH= 13, C_S_AXI_DATA_WIDTH= 32)).withAnnotations(Seq(WriteVcdAnnotation, VerilatorBackendAnnotation)) { dut =>
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
      dut.io.S_AXI_WDATA.poke((-1).S)
      dut.io.S_AXI_WSTRB.poke("b1111".U)
      dut.clock.step()

      dut.io.S_AXI_BREADY.poke(true.B)
      dut.clock.step()

      // write resultAddr signal
      dut.io.S_AXI_AWVALID.poke(true.B)
      dut.io.S_AXI_AWADDR.poke("h004".U)
      dut.clock.step()

      dut.io.S_AXI_WVALID.poke(true.B)
      dut.io.S_AXI_WDATA.poke(20.S)
      dut.io.S_AXI_WSTRB.poke("b1111".U)
      dut.clock.step()

      dut.io.S_AXI_BREADY.poke(true.B)
      dut.clock.step()

      // write valid signal
      dut.io.S_AXI_AWVALID.poke(true.B)
      dut.io.S_AXI_AWADDR.poke("h008".U)
      dut.clock.step()

      dut.io.S_AXI_WVALID.poke(true.B)
      dut.io.S_AXI_WDATA.poke(1.S)
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