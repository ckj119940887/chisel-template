package GeneralRegFileToBRAM 

import chisel3._
import chiseltest._
import chiseltest.simulator.{WriteVcdAnnotation, VerilatorBackendAnnotation}
import org.scalatest.flatspec.AnyFlatSpec

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

class ShiftTestBenchNoVerilator extends AnyFlatSpec with ChiselScalatestTester {
  "ShiftTestBenchNoVerilator" should "work" in {
    test(new shift()).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.clock.setTimeout(3000)

      dut.reset.poke(true.B)
      for (i <- 0 until (5)) {
        dut.clock.step()
      }
      dut.reset.poke(false.B)
      dut.clock.step()

      dut.io.a.poke((-5).S)
      dut.io.num.poke(5.U)

      for(i <- 0 until 10) {
        dut.clock.step()
      }

    }
  }
}

class GeneratedAddTestBenchNoVerilator extends AnyFlatSpec with ChiselScalatestTester {
  "GeneratedAddTestBenchNoVerilator" should "work" in {
    test(new testAdd()).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.clock.setTimeout(3000)

      dut.reset.poke(true.B)
      for (i <- 0 until (5)) {
        dut.clock.step()
      }
      dut.reset.poke(false.B)
      dut.clock.step()

      dut.io.arrayWe.poke(true.B)
      dut.io.arrayWriteAddr.poke(12.U)
      dut.io.arrayWData.poke("h00000001".U)
      dut.io.arrayStrb.poke("b1111".U)
      dut.clock.step()

      dut.io.arrayWe.poke(true.B)
      dut.io.arrayWriteAddr.poke(16.U)
      dut.io.arrayWData.poke("h00000000".U)
      dut.io.arrayStrb.poke("b1111".U)
      dut.clock.step()

      dut.io.arrayWe.poke(false.B)
      dut.io.valid.poke(true.B)
      for(i <- 0 until 300) {
        dut.clock.step()
      }

    }
  }
}

class GeneratedAssertTestBenchNoVerilator extends AnyFlatSpec with ChiselScalatestTester {
  "GeneratedAssertTestBenchNoVerilator" should "work" in {
    //test(new bar()).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
    test(new bar()).withAnnotations(Seq(VerilatorBackendAnnotation)) { dut =>
      dut.clock.setTimeout(3000)

      dut.reset.poke(true.B)
      for (i <- 0 until (5)) {
        dut.clock.step()
      }
      dut.reset.poke(false.B)
      dut.clock.step()

      dut.io.valid.poke(true.B)
      for(i <- 0 until 6500) {
        dut.clock.step()
      }

    }
  }
}