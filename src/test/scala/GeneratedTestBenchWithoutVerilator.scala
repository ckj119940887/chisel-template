package GeneralRegFileToBRAM 

import chisel3._
import chiseltest._
import chiseltest.simulator.{WriteVcdAnnotation, VerilatorBackendAnnotation}
import org.scalatest.flatspec.AnyFlatSpec

/*
class DivisionTestBenchNoVerilator extends AnyFlatSpec with ChiselScalatestTester {
  "DivisionTestBenchNoVerilator" should "work" in {
    test(new PipelinedDivMod(64)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.clock.setTimeout(3000)

      dut.reset.poke(true.B)
      for (i <- 0 until (5)) {
        dut.clock.step()
      }
      dut.reset.poke(false.B)
      dut.clock.step()

      dut.io.a.poke("h000000000000001B".U)
      dut.io.b.poke("h0000000000000006".U)
      dut.io.start.poke(true.B)

      for(i <- 0 until 75) {
        dut.clock.step()
      }

    }
  }
}
*/

class SignedDivisionTestBenchNoVerilator extends AnyFlatSpec with ChiselScalatestTester {
  "SignedDivisionTestBenchNoVerilator" should "work" in {
    test(new SignedPipelinedDivMod(64)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.clock.setTimeout(3000)

      dut.reset.poke(true.B)
      for (i <- 0 until (5)) {
        dut.clock.step()
      }
      dut.reset.poke(false.B)
      dut.clock.step()

      dut.io.a.poke(((-5).S)(64.W))
      dut.io.b.poke(((1).S)(64.W))
      dut.io.start.poke(true.B)

      for(i <- 0 until 75) {
        dut.clock.step()
      }

    }
  }
}
