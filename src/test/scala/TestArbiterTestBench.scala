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

class TempSaveRestoreTestBench extends AnyFlatSpec with ChiselScalatestTester {
  "TestNewUshrSigned64TestBench" should "work" in {
    test(new TempSaveRestore(
      nU1 = 1, nU8 = 2, nU16 = 3, nU32 = 0, nU64 = 1,
      nS1 = 1, nS8 = 2, nS16 = 3, nS32 = 0, nS64 = 1,
      addrWidth = 32, dataWidth = 64, depth = 100,
      stackMaxDepth = 32, idWidth = 5, cpWidth = 8
    )).withAnnotations(Seq(WriteVcdAnnotation, VerilatorBackendAnnotation)) { dut =>
      dut.clock.setTimeout(3000)

      dut.reset.poke(true.B)
      for (i <- 0 until (5)) {
        dut.clock.step()
      }
      dut.reset.poke(false.B)
      dut.clock.step()

      dut.io.req.valid.poke(false.B)
      dut.clock.step()

      dut.io.req.bits.u1(0).poke(1.U)
      dut.io.req.bits.s1(0).poke((-1).S)

      dut.io.req.bits.u8(0).poke(2.U)
      dut.io.req.bits.u8(0).poke(3.U)
      dut.io.req.bits.s8(1).poke((-2).S)
      dut.io.req.bits.s8(1).poke((-3).S)

      dut.io.req.bits.u16(0).poke(4.U)
      dut.io.req.bits.u16(0).poke(5.U)
      dut.io.req.bits.u16(0).poke(6.U)
      dut.io.req.bits.s16(1).poke((-4).S)
      dut.io.req.bits.s16(1).poke((-5).S)
      dut.io.req.bits.s16(1).poke((-6).S)

      dut.io.req.bits.u64(0).poke(7.U)
      dut.io.req.bits.s64(0).poke((-7).S)

      dut.io.req.bits.srcId.poke(7.U)
      dut.io.req.bits.srcCp.poke(8.U)

      dut.io.req.bits.op.poke(1.U)

      dut.io.req.valid.poke(true.B)

      for(i <- 0 until 1000) {
        dut.clock.step()
      }
    }
  }
}

class TempTestTestBench extends AnyFlatSpec with ChiselScalatestTester {
  "TempTestTestBench" should "work" in {
    test(new TempTest()).withAnnotations(Seq(WriteVcdAnnotation, VerilatorBackendAnnotation)) { dut =>
      dut.clock.setTimeout(3000)

      dut.reset.poke(true.B)
      for (i <- 0 until (5)) {
        dut.clock.step()
      }
      dut.reset.poke(false.B)
      dut.clock.step()

      dut.io.in1.poke(1.U)
      dut.io.in2.poke(2.U)

      for(i <- 0 until 1000) {
        dut.clock.step()
      }
    }
  }
}