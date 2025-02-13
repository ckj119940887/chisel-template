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

class GeneralRegFileToRegFileProTestBench extends AnyFlatSpec with ChiselScalatestTester {
  "GeneralRegFileToRegFileProTestBench" should "work" in {
    test(new GeneralRegFileToRegFilePro()).withAnnotations(Seq(WriteVcdAnnotation, VerilatorBackendAnnotation)) { dut =>
      dut.clock.setTimeout(3000)

      dut.reset.poke(true.B)
      for (i <- 0 until (5)) {
        dut.clock.step()
      }
      dut.reset.poke(false.B)

      dut.io.arrayAddr.poke("h000".U)
      dut.io.arrayWe.poke(true.B)
      dut.io.arrayStrb.poke("b0111".U)
      dut.io.arrayWData.poke("h01020304".U)
      dut.clock.step()

      dut.io.arrayAddr.poke("h004".U)
      dut.io.arrayWe.poke(true.B)
      dut.io.arrayStrb.poke("b1111".U)
      dut.io.arrayWData.poke("h05060708".U)
      dut.clock.step()

      dut.io.arrayAddr.poke("h008".U)
      dut.io.arrayWe.poke(true.B)
      dut.io.arrayStrb.poke("b1111".U)
      dut.io.arrayWData.poke("h05060708".U)
      dut.clock.step()

      dut.io.arrayAddr.poke("h00C".U)
      dut.io.arrayWe.poke(true.B)
      dut.io.arrayStrb.poke("b1111".U)
      dut.io.arrayWData.poke("h05060708".U)
      dut.clock.step()

      dut.io.arrayWe.poke(false.B)
      dut.io.valid.poke(true.B)
      dut.clock.step()

      for(i <- 0 until 10) {
        dut.clock.step()
      }

      dut.io.arrayRe.poke(true.B)
      dut.io.arrayAddr.poke("h010".U)
      dut.clock.step()

      dut.io.arrayRe.poke(false.B)
      for(i <- 0 until 10) {
        dut.clock.step()
      }

    }
  }
}

class AXIWrapperChiselGeneralRegFileToRegFileProTestBench extends AnyFlatSpec with ChiselScalatestTester {
  "AXIWrapperChiselGeneralRegFileToRegFilePro" should "work" in {
    test(new AXIWrapperChiselGeneralRegFileToRegFilePro(C_S_AXI_ADDR_WIDTH= 32, C_S_AXI_DATA_WIDTH= 32)).withAnnotations(Seq(WriteVcdAnnotation, VerilatorBackendAnnotation)) { dut =>
      dut.clock.setTimeout(3000)

      dut.reset.poke(false.B)
      for (i <- 0 until (5)) {
        dut.clock.step()
      }
      dut.reset.poke(true.B)

      // write data
      dut.io.S_AXI_AWVALID.poke(true.B)
      dut.io.S_AXI_AWADDR.poke("h40000000".U)
      dut.clock.step()

      dut.io.S_AXI_WVALID.poke(true.B)
      dut.io.S_AXI_WDATA.poke(0x01020304.S)
      dut.io.S_AXI_WSTRB.poke("b1111".U)
      dut.clock.step()

      dut.io.S_AXI_BREADY.poke(true.B)
      dut.clock.step()

      // write data signal
      dut.io.S_AXI_AWVALID.poke(true.B)
      dut.io.S_AXI_AWADDR.poke("h40000004".U)
      dut.clock.step()

      dut.io.S_AXI_WVALID.poke(true.B)
      dut.io.S_AXI_WDATA.poke(0x05060708.S)
      dut.io.S_AXI_WSTRB.poke("b1111".U)
      dut.clock.step()

      dut.io.S_AXI_BREADY.poke(true.B)
      dut.clock.step()

      // write data signal
      dut.io.S_AXI_AWVALID.poke(true.B)
      dut.io.S_AXI_AWADDR.poke(0x40000008.U)
      dut.clock.step()

      dut.io.S_AXI_WVALID.poke(true.B)
      dut.io.S_AXI_WDATA.poke(0x05060708.S)
      dut.io.S_AXI_WSTRB.poke("b1111".U)
      dut.clock.step()

      dut.io.S_AXI_BREADY.poke(true.B)
      dut.clock.step()

      // write data signal
      dut.io.S_AXI_AWVALID.poke(true.B)
      dut.io.S_AXI_AWADDR.poke(0x4000000C.U)
      dut.clock.step()

      dut.io.S_AXI_WVALID.poke(true.B)
      dut.io.S_AXI_WDATA.poke(0x05060708.S)
      dut.io.S_AXI_WSTRB.poke("b1111".U)
      dut.clock.step()

      dut.io.S_AXI_BREADY.poke(true.B)
      dut.clock.step()

      // write valid signal
      dut.io.S_AXI_AWVALID.poke(true.B)
      dut.io.S_AXI_AWADDR.poke("h40000400".U)
      dut.clock.step()

      dut.io.S_AXI_WVALID.poke(true.B)
      dut.io.S_AXI_WDATA.poke(1.S)
      dut.io.S_AXI_WSTRB.poke("b1111".U)
      dut.clock.step()

      dut.io.S_AXI_BREADY.poke(true.B)
      dut.clock.step()

      // disable write valid signal
      dut.io.S_AXI_AWVALID.poke(false.B)
      dut.io.S_AXI_WVALID.poke(false.B)
      dut.io.S_AXI_BREADY.poke(false.B)
      dut.clock.step()

      for(i <- 0 until 20) {
        dut.clock.step()
      }

      // read ready signal
      dut.io.S_AXI_ARVALID.poke(true.B)
      dut.io.S_AXI_ARADDR.poke("h40000404".U)
      dut.clock.step()

      dut.io.S_AXI_RREADY.poke(true.B)
      dut.clock.step()

      dut.clock.step()

      // read ready signal
      dut.io.S_AXI_ARVALID.poke(true.B)
      dut.io.S_AXI_ARADDR.poke("h40000010".U)
      dut.clock.step()

      dut.io.S_AXI_RREADY.poke(true.B)
      dut.clock.step()

      dut.clock.step()

      // disable all read signal
      dut.io.S_AXI_ARVALID.poke(false.B)
      dut.io.S_AXI_RREADY.poke(false.B)
      dut.clock.step()

      for(i <- 0 until 20) {
        dut.clock.step()
      }
    }
  }
}