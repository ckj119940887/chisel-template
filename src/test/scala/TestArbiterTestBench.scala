package TestArbiter

import chisel3._
import chiseltest._
import chiseltest.simulator.WriteVcdAnnotation
import org.scalatest.flatspec.AnyFlatSpec

class TestArbiterTestBench extends AnyFlatSpec with ChiselScalatestTester {
  "TestArbiterTestBench" should "work" in {
    test(new TestAllArbiter()).withAnnotations(Seq(WriteVcdAnnotation, VerilatorBackendAnnotation)) { dut =>
      dut.clock.setTimeout(3000)

      dut.reset.poke(true.B)
      for (i <- 0 until (5)) {
        dut.clock.step()
      }
      dut.reset.poke(false.B)
      dut.clock.step()

      for(i <- 0 until 1000) {
        dut.clock.step()
      }
    }
  }
}

class TestNewShlSigned64TestBench extends AnyFlatSpec with ChiselScalatestTester {
  "TestNewShlSigned64TestBench" should "work" in {
    test(new TestNewShlSigned64()).withAnnotations(Seq(WriteVcdAnnotation, VerilatorBackendAnnotation)) { dut =>
      dut.clock.setTimeout(3000)

      dut.reset.poke(true.B)
      for (i <- 0 until (5)) {
        dut.clock.step()
      }
      dut.reset.poke(false.B)
      dut.clock.step()

      for(i <- 0 until 1000) {
        dut.clock.step()
      }
    }
  }
}

class TestNewShrSigned64TestBench extends AnyFlatSpec with ChiselScalatestTester {
  "TestNewShrSigned64TestBench" should "work" in {
    test(new TestNewShrSigned64()).withAnnotations(Seq(WriteVcdAnnotation, VerilatorBackendAnnotation)) { dut =>
      dut.clock.setTimeout(3000)

      dut.reset.poke(true.B)
      for (i <- 0 until (5)) {
        dut.clock.step()
      }
      dut.reset.poke(false.B)
      dut.clock.step()

      for(i <- 0 until 1000) {
        dut.clock.step()
      }
    }
  }
}

class TestNewUshrSigned64TestBench extends AnyFlatSpec with ChiselScalatestTester {
  "TestNewUshrSigned64TestBench" should "work" in {
    test(new TestNewUshrSigned64()).withAnnotations(Seq(WriteVcdAnnotation, VerilatorBackendAnnotation)) { dut =>
      dut.clock.setTimeout(3000)

      dut.reset.poke(true.B)
      for (i <- 0 until (5)) {
        dut.clock.step()
      }
      dut.reset.poke(false.B)
      dut.clock.step()

      for(i <- 0 until 1000) {
        dut.clock.step()
      }
    }
  }
}