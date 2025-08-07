package GeneralRegFileToBRAM 

import chisel3._
import chiseltest._
import chiseltest.simulator.WriteVcdAnnotation
import org.scalatest.flatspec.AnyFlatSpec

class GeneratedFactorialTestBench extends AnyFlatSpec with ChiselScalatestTester {
  "GeneratedFactorialTestBench" should "work" in {
    test(new FactorialTest()).withAnnotations(Seq(WriteVcdAnnotation, VerilatorBackendAnnotation)) { dut =>
      dut.clock.setTimeout(3000)

      dut.reset.poke(true.B)
      for (i <- 0 until (5)) {
        dut.clock.step()
      }
      dut.reset.poke(false.B)
      dut.clock.step()

      dut.io.arrayWe.poke(true.B)
      dut.io.arrayWriteAddr.poke(0.U)
      dut.io.arrayWData.poke("hFFFFFFFF".U)
      dut.io.arrayStrb.poke("b1111".U)
      dut.clock.step()

      dut.io.arrayWe.poke(true.B)
      dut.io.arrayWriteAddr.poke(4.U)
      dut.io.arrayWData.poke("hFFFFFFFF".U)
      dut.io.arrayStrb.poke("b1111".U)
      dut.clock.step()

      dut.io.arrayWe.poke(false.B)
      dut.io.valid.poke(true.B)
      for(i <- 0 until 700) {
        dut.clock.step()
      }

    }
  }
}

class GeneratedAddTestBench extends AnyFlatSpec with ChiselScalatestTester {
  "GeneratedAddTestBench" should "work" in {
    test(new AddTest()).withAnnotations(Seq(WriteVcdAnnotation, VerilatorBackendAnnotation)) { dut =>
      dut.clock.setTimeout(3000)

      dut.reset.poke(true.B)
      for (i <- 0 until (5)) {
        dut.clock.step()
      }
      dut.reset.poke(false.B)
      dut.clock.step()

      dut.io.arrayWe.poke(true.B)
      dut.io.arrayWriteAddr.poke(0.U)
      dut.io.arrayWData.poke("hFFFFFFFF".U)
      dut.io.arrayStrb.poke("b1111".U)
      dut.clock.step()

      dut.io.arrayWe.poke(true.B)
      dut.io.arrayWriteAddr.poke(4.U)
      dut.io.arrayWData.poke("hFFFFFFFF".U)
      dut.io.arrayStrb.poke("b1111".U)
      dut.clock.step()

      dut.io.arrayWe.poke(false.B)
      dut.io.valid.poke(true.B)
      for(i <- 0 until 700) {
        dut.clock.step()
      }

    }
  }
}

class GeneratedMultTestBench extends AnyFlatSpec with ChiselScalatestTester {
  "GeneratedMultTestBench" should "work" in {
    test(new MultTest()).withAnnotations(Seq(WriteVcdAnnotation, VerilatorBackendAnnotation)) { dut =>
      dut.clock.setTimeout(3000)

      dut.reset.poke(true.B)
      for (i <- 0 until (5)) {
        dut.clock.step()
      }
      dut.reset.poke(false.B)
      dut.clock.step()

      dut.io.arrayWe.poke(true.B)
      dut.io.arrayWriteAddr.poke(0.U)
      dut.io.arrayWData.poke("hFFFFFFFF".U)
      dut.io.arrayStrb.poke("b1111".U)
      dut.clock.step()

      dut.io.arrayWe.poke(true.B)
      dut.io.arrayWriteAddr.poke(4.U)
      dut.io.arrayWData.poke("hFFFFFFFF".U)
      dut.io.arrayStrb.poke("b1111".U)
      dut.clock.step()

      dut.io.arrayWe.poke(false.B)
      dut.io.valid.poke(true.B)
      for(i <- 0 until 1000) {
        dut.clock.step()
      }

    }
  }
}

class GeneratedBubbleTestBench extends AnyFlatSpec with ChiselScalatestTester {
  "GeneratedBubbleTestBench" should "work" in {
    test(new BubbleTest()).withAnnotations(Seq(WriteVcdAnnotation, VerilatorBackendAnnotation)) { dut =>
      dut.clock.setTimeout(3000)

      dut.reset.poke(true.B)
      for (i <- 0 until (5)) {
        dut.clock.step()
      }
      dut.reset.poke(false.B)
      dut.clock.step()

      dut.io.arrayWe.poke(true.B)
      dut.io.arrayWriteAddr.poke(0.U)
      dut.io.arrayWData.poke("hFFFFFFFF".U)
      dut.io.arrayStrb.poke("b1111".U)
      dut.clock.step()

      dut.io.arrayWe.poke(true.B)
      dut.io.arrayWriteAddr.poke(4.U)
      dut.io.arrayWData.poke("hFFFFFFFF".U)
      dut.io.arrayStrb.poke("b1111".U)
      dut.clock.step()

      dut.io.arrayWe.poke(false.B)
      dut.io.valid.poke(true.B)
      for(i <- 0 until 700) {
        dut.clock.step()
      }

    }
  }
}

class GeneratedSumTestBench extends AnyFlatSpec with ChiselScalatestTester {
  "GeneratedSumTestBench" should "work" in {
    test(new SumTest()).withAnnotations(Seq(WriteVcdAnnotation, VerilatorBackendAnnotation)) { dut =>
      dut.clock.setTimeout(3000)

      dut.reset.poke(true.B)
      for (i <- 0 until (5)) {
        dut.clock.step()
      }
      dut.reset.poke(false.B)
      dut.clock.step()

      dut.io.arrayWe.poke(true.B)
      dut.io.arrayWriteAddr.poke(0.U)
      dut.io.arrayWData.poke("hFFFFFFFF".U)
      dut.io.arrayStrb.poke("b1111".U)
      dut.clock.step()

      dut.io.arrayWe.poke(true.B)
      dut.io.arrayWriteAddr.poke(4.U)
      dut.io.arrayWData.poke("hFFFFFFFF".U)
      dut.io.arrayStrb.poke("b1111".U)
      dut.clock.step()

      dut.io.arrayWe.poke(false.B)
      dut.io.valid.poke(true.B)
      for(i <- 0 until 400) {
        dut.clock.step()
      }

    }
  }
}

class GeneratedSquareTestBench extends AnyFlatSpec with ChiselScalatestTester {
  "GeneratedSquareTestBench" should "work" in {
    test(new SquareTest()).withAnnotations(Seq(WriteVcdAnnotation, VerilatorBackendAnnotation)) { dut =>
      dut.clock.setTimeout(3000)

      dut.reset.poke(true.B)
      for (i <- 0 until (5)) {
        dut.clock.step()
      }
      dut.reset.poke(false.B)
      dut.clock.step()

      dut.io.arrayWe.poke(true.B)
      dut.io.arrayWriteAddr.poke(0.U)
      dut.io.arrayWData.poke("hFFFFFFFF".U)
      dut.io.arrayStrb.poke("b1111".U)
      dut.clock.step()

      dut.io.arrayWe.poke(true.B)
      dut.io.arrayWriteAddr.poke(4.U)
      dut.io.arrayWData.poke("hFFFFFFFF".U)
      dut.io.arrayStrb.poke("b1111".U)
      dut.clock.step()

      dut.io.arrayWe.poke(false.B)
      dut.io.valid.poke(true.B)
      for(i <- 0 until 500) {
        dut.clock.step()
      }

    }
  }
}

class GeneratedConstructTestBench extends AnyFlatSpec with ChiselScalatestTester {
  "GeneratedConstructTestBench" should "work" in {
    test(new MkISTest()).withAnnotations(Seq(WriteVcdAnnotation, VerilatorBackendAnnotation)) { dut =>
      dut.clock.setTimeout(3000)

      dut.reset.poke(true.B)
      for (i <- 0 until (5)) {
        dut.clock.step()
      }
      dut.reset.poke(false.B)
      dut.clock.step()

      dut.io.arrayWe.poke(true.B)
      dut.io.arrayWriteAddr.poke(0.U)
      dut.io.arrayWData.poke("hFFFFFFFF".U)
      dut.io.arrayStrb.poke("b1111".U)
      dut.clock.step()

      dut.io.arrayWe.poke(true.B)
      dut.io.arrayWriteAddr.poke(4.U)
      dut.io.arrayWData.poke("hFFFFFFFF".U)
      dut.io.arrayStrb.poke("b1111".U)
      dut.clock.step()

      dut.io.arrayWe.poke(false.B)
      dut.io.valid.poke(true.B)
      for(i <- 0 until 350) {
        dut.clock.step()
      }

    }
  }
}

class GeneratedGlobalTestBench extends AnyFlatSpec with ChiselScalatestTester {
  "GeneratedGlobalTestBench" should "work" in {
    test(new GlobalTest()).withAnnotations(Seq(WriteVcdAnnotation, VerilatorBackendAnnotation)) { dut =>
      dut.clock.setTimeout(3000)

      dut.reset.poke(true.B)
      for (i <- 0 until (5)) {
        dut.clock.step()
      }
      dut.reset.poke(false.B)
      dut.clock.step()

      dut.io.arrayWe.poke(true.B)
      dut.io.arrayWriteAddr.poke(0.U)
      dut.io.arrayWData.poke("hFFFFFFFF".U)
      dut.io.arrayStrb.poke("b1111".U)
      dut.clock.step()

      dut.io.arrayWe.poke(true.B)
      dut.io.arrayWriteAddr.poke(4.U)
      dut.io.arrayWData.poke("hFFFFFFFF".U)
      dut.io.arrayStrb.poke("b1111".U)
      dut.clock.step()

      dut.io.arrayWe.poke(false.B)
      dut.io.valid.poke(true.B)
      for(i <- 0 until 200) {
        dut.clock.step()
      }

    }
  }
}

class GeneratedInstanceofTestBench extends AnyFlatSpec with ChiselScalatestTester {
  "GeneratedInstanceofTestBench" should "work" in {
    test(new InstanceofTest()).withAnnotations(Seq(WriteVcdAnnotation, VerilatorBackendAnnotation)) { dut =>
      dut.clock.setTimeout(3000)

      dut.reset.poke(true.B)
      for (i <- 0 until (5)) {
        dut.clock.step()
      }
      dut.reset.poke(false.B)
      dut.clock.step()

      dut.io.arrayWe.poke(true.B)
      dut.io.arrayWriteAddr.poke(0.U)
      dut.io.arrayWData.poke("hFFFFFFFF".U)
      dut.io.arrayStrb.poke("b1111".U)
      dut.clock.step()

      dut.io.arrayWe.poke(true.B)
      dut.io.arrayWriteAddr.poke(4.U)
      dut.io.arrayWData.poke("hFFFFFFFF".U)
      dut.io.arrayStrb.poke("b1111".U)
      dut.clock.step()

      dut.io.arrayWe.poke(false.B)
      dut.io.valid.poke(true.B)
      for(i <- 0 until 200) {
        dut.clock.step()
      }

    }
  }
}

class GeneratedAssertTestBench extends AnyFlatSpec with ChiselScalatestTester {
  "GeneratedAssertTestBench" should "work" in {
    test(new BarTest()).withAnnotations(Seq(WriteVcdAnnotation, VerilatorBackendAnnotation)) { dut =>
      dut.clock.setTimeout(3000)

      dut.reset.poke(true.B)
      for (i <- 0 until (5)) {
        dut.clock.step()
      }
      dut.reset.poke(false.B)
      dut.clock.step()

      dut.io.arrayWe.poke(true.B)
      dut.io.arrayWriteAddr.poke(0.U)
      dut.io.arrayWData.poke("hFFFFFFFF".U)
      dut.io.arrayStrb.poke("b1111".U)
      dut.clock.step()

      dut.io.arrayWe.poke(true.B)
      dut.io.arrayWriteAddr.poke(4.U)
      dut.io.arrayWData.poke("hFFFFFFFF".U)
      dut.io.arrayStrb.poke("b1111".U)
      dut.clock.step()

      dut.io.arrayWe.poke(false.B)
      dut.io.valid.poke(true.B)
      for(i <- 0 until 1800) {
        dut.clock.step()
      }

    }
  }
}

class GeneratedLocalTestBench extends AnyFlatSpec with ChiselScalatestTester {
  "GeneratedLocalTestBench" should "work" in {
    test(new LocalReuseTest()).withAnnotations(Seq(WriteVcdAnnotation, VerilatorBackendAnnotation)) { dut =>
      dut.clock.setTimeout(3000)

      dut.reset.poke(true.B)
      for (i <- 0 until (5)) {
        dut.clock.step()
      }
      dut.reset.poke(false.B)
      dut.clock.step()

      dut.io.arrayWe.poke(true.B)
      dut.io.arrayWriteAddr.poke(0.U)
      dut.io.arrayWData.poke("hFFFFFFFF".U)
      dut.io.arrayStrb.poke("b1111".U)
      dut.clock.step()

      dut.io.arrayWe.poke(true.B)
      dut.io.arrayWriteAddr.poke(4.U)
      dut.io.arrayWData.poke("hFFFFFFFF".U)
      dut.io.arrayStrb.poke("b1111".U)
      dut.clock.step()

      dut.io.arrayWe.poke(false.B)
      dut.io.valid.poke(true.B)
      for(i <- 0 until 400) {
        dut.clock.step()
      }

    }
  }
}

class GeneratedPrintlnU64TestBench extends AnyFlatSpec with ChiselScalatestTester {
  "GeneratedPrintlnU64TestBench" should "work" in {
    test(new PrintlnU64Test()).withAnnotations(Seq(WriteVcdAnnotation, VerilatorBackendAnnotation)) { dut =>
      dut.clock.setTimeout(3000)

      dut.reset.poke(true.B)
      for (i <- 0 until (5)) {
        dut.clock.step()
      }
      dut.reset.poke(false.B)
      dut.clock.step()

      dut.io.arrayWe.poke(true.B)
      dut.io.arrayWriteAddr.poke(0.U)
      dut.io.arrayWData.poke("hFFFFFFFF".U)
      dut.io.arrayStrb.poke("b1111".U)
      dut.clock.step()

      dut.io.arrayWe.poke(true.B)
      dut.io.arrayWriteAddr.poke(4.U)
      dut.io.arrayWData.poke("hFFFFFFFF".U)
      dut.io.arrayStrb.poke("b1111".U)
      dut.clock.step()

      dut.io.arrayWe.poke(false.B)
      dut.io.valid.poke(true.B)
      for(i <- 0 until 1000) {
        dut.clock.step()
      }

    }
  }
}

class GeneratedPrintTestBench extends AnyFlatSpec with ChiselScalatestTester {
  "GeneratedPrintTestBench" should "work" in {
    test(new PrintTestTest()).withAnnotations(Seq(WriteVcdAnnotation, VerilatorBackendAnnotation)) { dut =>
      dut.clock.setTimeout(3000)

      dut.reset.poke(true.B)
      for (i <- 0 until (5)) {
        dut.clock.step()
      }
      dut.reset.poke(false.B)
      dut.clock.step()

      dut.io.arrayWe.poke(true.B)
      dut.io.arrayWriteAddr.poke(0.U)
      dut.io.arrayWData.poke("hFFFFFFFF".U)
      dut.io.arrayStrb.poke("b1111".U)
      dut.clock.step()

      dut.io.arrayWe.poke(true.B)
      dut.io.arrayWriteAddr.poke(4.U)
      dut.io.arrayWData.poke("hFFFFFFFF".U)
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

class GeneratedDivRemTestBench extends AnyFlatSpec with ChiselScalatestTester {
  "GeneratedDivRemTestBench" should "work" in {
    test(new DivRemTest()).withAnnotations(Seq(WriteVcdAnnotation, VerilatorBackendAnnotation)) { dut =>
      dut.clock.setTimeout(3000)

      dut.reset.poke(true.B)
      for (i <- 0 until (5)) {
        dut.clock.step()
      }
      dut.reset.poke(false.B)
      dut.clock.step()

      dut.io.arrayWe.poke(true.B)
      dut.io.arrayWriteAddr.poke(0.U)
      dut.io.arrayWData.poke("hFFFFFFFF".U)
      dut.io.arrayStrb.poke("b1111".U)
      dut.clock.step()

      dut.io.arrayWe.poke(true.B)
      dut.io.arrayWriteAddr.poke(4.U)
      dut.io.arrayWData.poke("hFFFFFFFF".U)
      dut.io.arrayStrb.poke("b1111".U)
      dut.clock.step()

      dut.io.arrayWe.poke(false.B)
      dut.io.valid.poke(true.B)
      for(i <- 0 until 1500) {
        dut.clock.step()
      }

    }
  }
}

class GeneratedShiftTestBench extends AnyFlatSpec with ChiselScalatestTester {
  "GeneratedShiftTestBench" should "work" in {
    test(new ShiftU64ShiftS64Test()).withAnnotations(Seq(WriteVcdAnnotation, VerilatorBackendAnnotation)) { dut =>
      dut.clock.setTimeout(3000)

      dut.reset.poke(true.B)
      for (i <- 0 until (5)) {
        dut.clock.step()
      }
      dut.reset.poke(false.B)
      dut.clock.step()

      dut.io.arrayWe.poke(true.B)
      dut.io.arrayWriteAddr.poke(0.U)
      dut.io.arrayWData.poke("hFFFFFFFF".U)
      dut.io.arrayStrb.poke("b1111".U)
      dut.clock.step()

      dut.io.arrayWe.poke(true.B)
      dut.io.arrayWriteAddr.poke(4.U)
      dut.io.arrayWData.poke("hFFFFFFFF".U)
      dut.io.arrayStrb.poke("b1111".U)
      dut.clock.step()

      dut.io.arrayWe.poke(false.B)
      dut.io.valid.poke(true.B)
      for(i <- 0 until 150) {
        dut.clock.step()
      }

    }
  }
}

class GeneratedMiscAndTestBench extends AnyFlatSpec with ChiselScalatestTester {
  "GeneratedMiscAndTestBench" should "work" in {
    test(new AndUnsigned(64)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.clock.setTimeout(3000)

      dut.reset.poke(true.B)
      for (i <- 0 until (5)) {
        dut.clock.step()
      }
      dut.reset.poke(false.B)
      dut.clock.step()

      dut.io.a.poke(8.U(64.W))
      dut.io.b.poke(15.U(64.W))
      dut.io.start.poke(true.B)
      dut.clock.step()

      dut.io.start.poke(false.B)

      dut.clock.step()
      dut.clock.step()
      dut.clock.step()
      dut.clock.step()
      dut.clock.step()
    }
  }
}

class GeneratedMiscOrTestBench extends AnyFlatSpec with ChiselScalatestTester {
  "GeneratedMiscOrTestBench" should "work" in {
    test(new OrSigned(64)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.clock.setTimeout(3000)

      dut.reset.poke(true.B)
      for (i <- 0 until (5)) {
        dut.clock.step()
      }
      dut.reset.poke(false.B)
      dut.clock.step()

      dut.io.a.poke((-41).S(64.W))
      dut.io.b.poke((-42).S(64.W))
      dut.clock.step()

      dut.clock.step()
      dut.clock.step()
      dut.clock.step()
      dut.clock.step()
      dut.clock.step()
    }
  }
}

class GeneratedMiscXorTestBench extends AnyFlatSpec with ChiselScalatestTester {
  "GeneratedMiscXorTestBench" should "work" in {
    test(new XorSigned(64)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.clock.setTimeout(3000)

      dut.reset.poke(true.B)
      for (i <- 0 until (5)) {
        dut.clock.step()
      }
      dut.reset.poke(false.B)
      dut.clock.step()

      dut.io.a.poke((-41).S(64.W))
      dut.io.b.poke((-42).S(64.W))
      dut.clock.step()

      dut.clock.step()
      dut.clock.step()
      dut.clock.step()
      dut.clock.step()
      dut.clock.step()
    }
  }
}

class GeneratedMiscEqTestBench extends AnyFlatSpec with ChiselScalatestTester {
  "GeneratedMiscEqTestBench" should "work" in {
    test(new EqUnsigned(64)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.clock.setTimeout(3000)

      dut.reset.poke(true.B)
      for (i <- 0 until (5)) {
        dut.clock.step()
      }
      dut.reset.poke(false.B)
      dut.clock.step()

      dut.io.a.poke((41).U(64.W))
      dut.io.b.poke((42).U(64.W))
      dut.clock.step()

      dut.clock.step()
      dut.clock.step()
      dut.clock.step()
      dut.clock.step()
      dut.clock.step()
    }
  }
}

class GeneratedMiscNeTestBench extends AnyFlatSpec with ChiselScalatestTester {
  "GeneratedMiscNeTestBench" should "work" in {
    test(new NeSigned(64)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.clock.setTimeout(3000)

      dut.reset.poke(true.B)
      for (i <- 0 until (5)) {
        dut.clock.step()
      }
      dut.reset.poke(false.B)
      dut.clock.step()

      dut.io.a.poke((-41).S(64.W))
      dut.io.b.poke((-42).S(64.W))
      dut.clock.step()

      dut.clock.step()
      dut.clock.step()
      dut.clock.step()
      dut.clock.step()
      dut.clock.step()
    }
  }
}

class GeneratedMiscGeTestBench extends AnyFlatSpec with ChiselScalatestTester {
  "GeneratedMiscGeTestBench" should "work" in {
    test(new GeUnsigned(64)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.clock.setTimeout(3000)

      dut.reset.poke(true.B)
      for (i <- 0 until (5)) {
        dut.clock.step()
      }
      dut.reset.poke(false.B)
      dut.clock.step()

      dut.io.a.poke((42).U(64.W))
      dut.io.b.poke((41).U(64.W))
      dut.clock.step()

      dut.clock.step()
      dut.clock.step()
      dut.clock.step()
      dut.clock.step()
      dut.clock.step()
    }
  }
}

class GeneratedMiscGtTestBench extends AnyFlatSpec with ChiselScalatestTester {
  "GeneratedMiscGtTestBench" should "work" in {
    test(new GtSigned(64)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.clock.setTimeout(3000)

      dut.reset.poke(true.B)
      for (i <- 0 until (5)) {
        dut.clock.step()
      }
      dut.reset.poke(false.B)
      dut.clock.step()

      dut.io.a.poke((-42).S(64.W))
      dut.io.b.poke((41).S(64.W))
      dut.clock.step()

      dut.clock.step()
      dut.clock.step()
      dut.clock.step()
      dut.clock.step()
      dut.clock.step()
    }
  }
}

class GeneratedMiscLeTestBench extends AnyFlatSpec with ChiselScalatestTester {
  "GeneratedMiscLeTestBench" should "work" in {
    test(new LeSigned(64)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.clock.setTimeout(3000)

      dut.reset.poke(true.B)
      for (i <- 0 until (5)) {
        dut.clock.step()
      }
      dut.reset.poke(false.B)
      dut.clock.step()

      dut.io.a.poke((42).S(64.W))
      dut.io.b.poke((41).S(64.W))
      dut.clock.step()

      dut.clock.step()
      dut.clock.step()
      dut.clock.step()
      dut.clock.step()
      dut.clock.step()
    }
  }
}

class GeneratedMiscShrTestBench extends AnyFlatSpec with ChiselScalatestTester {
  "GeneratedMiscShrTestBench" should "work" in {
    test(new ShrUnsigned(64)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.clock.setTimeout(3000)

      dut.reset.poke(true.B)
      for (i <- 0 until (5)) {
        dut.clock.step()
      }
      dut.reset.poke(false.B)
      dut.clock.step()

      dut.io.a.poke((128).U(64.W))
      dut.io.b.poke((100).U(64.W))
      dut.clock.step()

      dut.clock.step()
      dut.clock.step()
      dut.clock.step()
      dut.clock.step()
      dut.clock.step()
    }
  }
}

class GeneratedMiscShlTestBench extends AnyFlatSpec with ChiselScalatestTester {
  "GeneratedMiscShlTestBench" should "work" in {
    test(new ShlSigned(64)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.clock.setTimeout(3000)

      dut.reset.poke(true.B)
      for (i <- 0 until (5)) {
        dut.clock.step()
      }
      dut.reset.poke(false.B)
      dut.clock.step()

      dut.io.a.poke((-128).S(64.W))
      dut.io.b.poke((100).U(64.W))
      dut.clock.step()

      dut.clock.step()
      dut.clock.step()
      dut.clock.step()
      dut.clock.step()
      dut.clock.step()
    }
  }
}

class GeneratedMiscUshrTestBench extends AnyFlatSpec with ChiselScalatestTester {
  "GeneratedMiscUshrTestBench" should "work" in {
    test(new UshrSigned(64)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.clock.setTimeout(3000)

      dut.reset.poke(true.B)
      for (i <- 0 until (5)) {
        dut.clock.step()
      }
      dut.reset.poke(false.B)
      dut.clock.step()

      dut.io.a.poke((-128).S(64.W))
      dut.io.b.poke((100).U(64.W))
      dut.clock.step()

      dut.clock.step()
      dut.clock.step()
      dut.clock.step()
      dut.clock.step()
      dut.clock.step()
    }
  }
}

class GeneratedTestPadTestBench extends AnyFlatSpec with ChiselScalatestTester {
  "GeneratedMiscTestPadTestBench" should "work" in {
    test(new TestPad(64)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.clock.setTimeout(3000)

      dut.reset.poke(true.B)
      for (i <- 0 until (5)) {
        dut.clock.step()
      }
      dut.reset.poke(false.B)
      dut.clock.step()

      dut.io.in.poke((-128).S(16.W))
      dut.clock.step()

      dut.clock.step()
      dut.clock.step()
      dut.clock.step()
      dut.clock.step()
      dut.clock.step()
    }
  }
}

class GeneratedMiscTestBench extends AnyFlatSpec with ChiselScalatestTester {
  "GeneratedMiscTestBench" should "work" in {
    test(new Indexer(16)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.clock.setTimeout(3000)

      dut.reset.poke(true.B)
      for (i <- 0 until (5)) {
        dut.clock.step()
      }
      dut.reset.poke(false.B)
      dut.clock.step()

      dut.io.baseOffset.poke(8.U(16.W))
      dut.io.dataOffset.poke(12.U(16.W))
      dut.io.index.poke(4.U(16.W))
      dut.io.elementSize.poke(3.U(16.W))
      dut.io.mask.poke("hFFFF".U)
      dut.io.ready.poke(true.B)
      dut.clock.step(20)
      // while (!dut.io.valid.peek().litToBoolean) {
      //   dut.clock.step(1)
      // }
      dut.io.ready.poke(false.B)
      dut.clock.step()

      dut.io.baseOffset.poke(52.U(16.W))
      dut.io.dataOffset.poke(12.U(16.W))
      dut.io.index.poke(1.U(16.W))
      dut.io.elementSize.poke(1.U(16.W))
      dut.io.mask.poke("hFFFF".U)
      dut.io.ready.poke(true.B)
      dut.clock.step(20)
      // while (!dut.io.valid.peek().litToBoolean) {
      //   dut.clock.step(1)
      // }
      dut.io.ready.poke(false.B)
      dut.clock.step()

      dut.clock.step(50)
    }
  }
}

class GeneratedMiscWrongTestShiftTestBench extends AnyFlatSpec with ChiselScalatestTester {
  "GeneratedMiscWrongTestShiftTestBench" should "work" in {
    test(new WrongTestShift()).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.clock.setTimeout(3000)

      dut.reset.poke(true.B)
      for (i <- 0 until (5)) {
        dut.clock.step()
      }
      dut.reset.poke(false.B)
      dut.clock.step()

      dut.io.a.poke(8.U(16.W))
      dut.clock.step()

      dut.clock.step()
      dut.clock.step()
      dut.clock.step()
      dut.clock.step()
      dut.clock.step()
    }
  }
}

class BRAMTestBench extends AnyFlatSpec with ChiselScalatestTester {
  "BRAMTestBench" should "work" in {
    test(new BRAM(depth = 1024, width = 32)).withAnnotations(Seq(WriteVcdAnnotation, VerilatorBackendAnnotation)) { dut =>
      dut.clock.setTimeout(3000)

      dut.reset.poke(true.B)
      for (i <- 0 until (5)) {
        dut.clock.step()
      }
      dut.reset.poke(false.B)
      dut.clock.step()

      dut.io.en.poke(true.B)
      dut.io.we.poke(true.B)
      dut.io.addr.poke(0x0.U)
      dut.io.din.poke("h12345678".U)
      dut.clock.step()

      dut.io.en.poke(true.B)
      dut.io.we.poke(true.B)
      dut.io.addr.poke(0x4.U)
      dut.io.din.poke("h87654321".U)
      dut.clock.step()

      dut.io.en.poke(true.B)
      dut.io.we.poke(false.B)
      dut.io.addr.poke(0x0.U)
      dut.clock.step()

      dut.io.en.poke(true.B)
      dut.io.we.poke(false.B)
      dut.io.addr.poke(0x1.U)
      dut.clock.step()

      for(i <- 0 to 10) {
        dut.clock.step()
      }

    }
  }
}

class GeneratedDivisionTestBench extends AnyFlatSpec with ChiselScalatestTester {
  "GeneratedDivisionTestBench" should "work" in {
    test(new PipelinedDivMod(64)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.clock.setTimeout(3000)

      dut.reset.poke(true.B)
      dut.io.start.poke(false.B)
      for (i <- 0 until (5)) {
        dut.clock.step()
      }
      dut.reset.poke(false.B)
      dut.clock.step()

      dut.io.start.poke(false.B)
      for(i <- 0 to 10) {
        dut.clock.step()
      }

      dut.io.a.poke(127.U(64.W))
      dut.io.b.poke(31.U(64.W))
      dut.io.start.poke(true.B)

      for(i <- 0 until 100) {
        dut.clock.step()
      }

      dut.io.start.poke(false.B)
      dut.clock.step()

      dut.io.a.poke(15.U(64.W))
      dut.io.b.poke(3.U(64.W))
      dut.io.start.poke(true.B)

      for(i <- 0 until 100) {
        dut.clock.step()
      }
    }
  }
}

class GeneratedDivTestBench extends AnyFlatSpec with ChiselScalatestTester {
  "GeneratedDivTestBench" should "work" in {
    test(new Div(64)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.clock.setTimeout(3000)

      dut.reset.poke(true.B)
      for (i <- 0 until (5)) {
        dut.clock.step()
      }
      dut.reset.poke(false.B)
      dut.clock.step()

      dut.io.a.poke(-127.S(64.W))
      dut.io.b.poke(31.S(64.W))

      dut.clock.step()
      dut.clock.step()
      dut.clock.step()
      dut.clock.step()
    }
  }
}

class GeneratedRemTestBench extends AnyFlatSpec with ChiselScalatestTester {
  "GeneratedRemTestBench" should "work" in {
    test(new Rem(64)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.clock.setTimeout(3000)

      dut.reset.poke(true.B)
      for (i <- 0 until (5)) {
        dut.clock.step()
      }
      dut.reset.poke(false.B)
      dut.clock.step()

      dut.io.a.poke(-127.S(64.W))
      dut.io.b.poke(31.S(64.W))

      dut.clock.step()
      dut.clock.step()
      dut.clock.step()
      dut.clock.step()
    }
  }
}

class GeneratedPipelineTestBench extends AnyFlatSpec with ChiselScalatestTester {
  "GeneratedPipelineTestBench" should "work" in {
    test(new Pipeline(2, 64)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.clock.setTimeout(3000)

      dut.reset.poke(true.B)
      for (i <- 0 until (5)) {
        dut.clock.step()
      }
      dut.reset.poke(false.B)
      dut.clock.step()

      dut.io.inVec(0).bits.a.poke(1.U(64.W))
      dut.io.inVec(0).bits.b.poke(2.U(64.W))
      dut.io.inVec(0).bits.bypassA.poke(false.B)
      dut.io.inVec(0).bits.bypassB.poke(false.B)
      dut.io.inVec(0).bits.bypassAIndex.poke(0.U)
      dut.io.inVec(0).bits.bypassBIndex.poke(0.U)
      dut.io.inVec(0).valid.poke(true.B)
      dut.clock.step()

      dut.io.inVec(0).bits.b.poke(4.U(64.W))
      dut.io.inVec(0).bits.bypassA.poke(true.B)
      dut.io.inVec(0).bits.bypassB.poke(false.B)
      dut.io.inVec(0).bits.bypassAIndex.poke(0.U)
      dut.io.inVec(0).bits.bypassBIndex.poke(0.U)
      dut.io.inVec(0).valid.poke(true.B)
      dut.clock.step()

      dut.io.inVec(0).bits.a.poke(5.U(64.W))
      dut.io.inVec(0).bits.b.poke(6.U(64.W))
      dut.io.inVec(0).valid.poke(true.B)
      dut.clock.step()
      dut.io.inVec(0).valid.poke(false.B)
      dut.clock.step()
      dut.clock.step()
      dut.clock.step()
    }
  }
}

class Generated3CycleAdderTestBench extends AnyFlatSpec with ChiselScalatestTester {
  "Generated3CycleAdderTestBench" should "work" in {
    test(new IndexAdder(64)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.clock.setTimeout(3000)

      dut.reset.poke(true.B)
      for (i <- 0 until (5)) {
        dut.clock.step()
      }
      dut.reset.poke(false.B)
      dut.clock.step()

      dut.io.a.poke(1.U(64.W))
      dut.io.b.poke(2.U(64.W))
      dut.io.start.poke(true.B)

      dut.clock.step()
      dut.io.a.poke(4.U(64.W))
      dut.io.b.poke(4.U(64.W))
      dut.clock.step()
      dut.io.start.poke(false.B)
      dut.clock.step()
      dut.clock.step()
      dut.io.a.poke(5.U(64.W))
      dut.io.b.poke(5.U(64.W))
      dut.io.start.poke(true.B)


      for (i <- 0 until (10)) {
        dut.clock.step()
      }

    }
  }
}

class TestNegativeTestBench extends AnyFlatSpec with ChiselScalatestTester {
  "TestNegativeTestBench" should "work" in {
    test(new TestNegative).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.clock.setTimeout(3000)

      dut.reset.poke(true.B)
      for (i <- 0 until (5)) {
        dut.clock.step()
      }
      dut.reset.poke(false.B)
      dut.clock.step()

      dut.io.a.poke(7.U(4.W))

      for (i <- 0 until (10)) {
        dut.clock.step()
      }

    }
  }
}

class BRAMAddrCompTestBench extends AnyFlatSpec with ChiselScalatestTester {
  "BRAMAddrCompTestBench" should "work" in {
    test(new BRAMAddrComp(16)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.clock.setTimeout(3000)

      dut.reset.poke(true.B)
      for (i <- 0 until (5)) {
        dut.clock.step()
      }
      dut.reset.poke(false.B)
      dut.clock.step()

      dut.io.addr.poke(7.U(16.W))
      dut.io.offset.poke(2.U(16.W))
      dut.io.isWrite.poke(true.B)
      dut.io.writeLen.poke(4.U)
      dut.io.start.poke(true.B)
      dut.clock.step(5)

      dut.io.start.poke(false.B)
      dut.io.isWrite.poke(false.B)
      dut.clock.step()

      // dut.io.addr.poke(16.U(16.W))
      // dut.io.offset.poke(0.U(16.W))
      // dut.io.start.poke(true.B)
      // dut.clock.step(5)

      // dut.io.start.poke(false.B)
      // dut.clock.step()

      for (i <- 0 until (10)) {
        dut.clock.step()
      }

    }
  }
}

class BRAMIPWrapperTestBench extends AnyFlatSpec with ChiselScalatestTester {
  "BRAMIPWrapperTestBench" should "work" in {
    test(new BRAMIPWrapper(bramDepth = 1024, bramWidth = 8, portWidth = 32)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.clock.setTimeout(3000)

      dut.reset.poke(true.B)
      for (i <- 0 until (5)) {
        dut.clock.step()
      }
      dut.reset.poke(false.B)
      dut.clock.step()

      // 1byte write mode
      for(i <- 0 until 10) {
          dut.io.mode.poke(2.U)
          dut.io.writeAddr.poke((i*4).U)
          dut.io.writeOffset.poke(0.U)
          dut.io.writeData.poke("h08070605".U)
          dut.io.writeLen.poke(4.U)
          dut.clock.step(6)
          dut.io.mode.poke(0.U)
          dut.clock.step()
      }

      // 1byte read mode
      for(i <- 0 until 40) {
        dut.io.mode.poke(1.U)
        dut.io.readAddr.poke(i.U)
        dut.io.readOffset.poke(0.U)
        dut.io.readLen.poke(1.U)
        dut.clock.step(4)
        // dut.io.readLen.poke((i+1).U)
        // if(i == 0)
        //   dut.clock.step(4)
        // else
        //   dut.clock.step(5+i)
        dut.io.mode.poke(0.U)
        dut.clock.step()
      }

      /*
      // 1byte dma mode
      dut.io.mode.poke(3.U)
      dut.io.dmaSrcAddr.poke(0.U)
      dut.io.dmaDstAddr.poke("hF0".U)
      dut.io.dmaDstOffset.poke(0.U)
      dut.io.dmaSrcLen.poke(20.U)
      dut.io.dmaDstLen.poke(25.U)
      dut.clock.step(22)
      dut.io.mode.poke(0.U)
      dut.clock.step()

      // 1byte read 0ode
      for(i <- 0 until 5) {
        dut.io.mode.poke(1.U)
        dut.io.readAddr.poke((0xF0+i*5).U)
        dut.io.readOffset.poke(0.U)
        dut.io.readLen.poke(5.U)
        //dut.clock.step(11)
        // dut.io.readLen.poke((i+1).U)
        dut.clock.step(3+5)
        dut.io.mode.poke(0.U)
        dut.clock.step()
      }
      */

      /*
      for(i <- 0 until 40) {
          dut.io.mode.poke(2.U)
          // dut.io.writeAddr.poke(i.U)
          // dut.io.writeOffset.poke(1.U)
          // dut.io.writeData.poke((i+1).U)
          // dut.io.writeLen.poke(1.U)
          dut.io.writeAddr.poke((i*8).U)
          dut.io.writeOffset.poke(0.U)
          dut.io.writeData.poke((i+1).U)
          dut.io.writeLen.poke(1.U)
          dut.clock.step(9)
          dut.io.mode.poke(0.U)
          dut.clock.step()
      }
      */

      /*
      // read mode
      for(i <- 0 until 5) {
        dut.io.mode.poke(1.U)
        dut.io.readAddr.poke((i*4).U)
        dut.io.readOffset.poke(0.U)
        dut.io.readLen.poke(4.U)
        // dut.io.readAddr.poke(i.U)
        // dut.io.readOffset.poke(1.U)
        dut.clock.step(7)
        dut.io.mode.poke(0.U)
        dut.clock.step()
      }
      */

      /*
      dut.io.mode.poke(3.U)
      dut.io.dmaSrcAddr.poke("h05".U)
      dut.io.dmaDstAddr.poke("hF2".U)
      dut.io.dmaLength.poke(20.U)
      dut.clock.step(75)
      dut.io.mode.poke(0.U)
      dut.clock.step()

      for(i <- 0 until 5) {
        dut.io.mode.poke(1.U)
        dut.io.readAddr.poke((0xF0 + i*8).U)
        dut.io.readOffset.poke(0.U)
        dut.clock.step(7)
        dut.io.mode.poke(0.U)
        dut.clock.step()
      }
      */

      for (i <- 0 until (10)) {
        dut.clock.step()
      }

    }
  }
}

class AXI4LiteSlaveTestBench extends AnyFlatSpec with ChiselScalatestTester {
  "AXI4LiteSlaveTestBench" should "work" in {
    test(new AXI4LiteSlave(C_S_AXI_DATA_WIDTH = 64, C_S_AXI_ADDR_WIDTH = 7)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.clock.setTimeout(3000)

      dut.reset.poke(false.B)
      for (i <- 0 until (5)) {
        dut.clock.step()
      }
      dut.reset.poke(true.B)
      dut.clock.step()

      dut.io.S_AXI_AWVALID.poke(true.B)
      dut.io.S_AXI_AWADDR.poke(0.U)
      dut.clock.step()

      dut.io.S_AXI_AWVALID.poke(false.B)
      dut.io.S_AXI_WVALID.poke(true.B)
      dut.io.S_AXI_WDATA.poke("h0f".U)
      dut.io.S_AXI_WSTRB.poke("h01".U)
      dut.clock.step()

      dut.io.S_AXI_WVALID.poke(false.B)
      dut.io.S_AXI_BREADY.poke(true.B)
      while (!dut.io.S_AXI_BVALID.peek().litToBoolean) {
        dut.clock.step(1)
      }

      dut.io.S_AXI_ARVALID.poke(true.B)
      dut.io.S_AXI_ARADDR.poke(0.U)
      dut.clock.step(1)

      dut.io.S_AXI_ARVALID.poke(false.B)
      dut.io.S_AXI_RREADY.poke(true.B)
      dut.clock.step(11)
      // while (!dut.io.axi.r.valid.peek().litToBoolean) {
      //   dut.clock.step(1)
      // }

      for (i <- 0 until (10)) {
        dut.clock.step()
      }

    }
  }
}

class AXI4LiteSlaveBRAMIPTestBench extends AnyFlatSpec with ChiselScalatestTester {
  "AXI4LiteSlaveBRAMIPTestBench" should "work" in {
    test(new AXI4LiteSlaveBRAMIP(C_S_AXI_DATA_WIDTH = 64, C_S_AXI_ADDR_WIDTH = 11, depth = 1024)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.clock.setTimeout(3000)

      dut.reset.poke(false.B)
      for (i <- 0 until (5)) {
        dut.clock.step()
      }
      dut.reset.poke(true.B)
      dut.clock.step()

      // write all FF to testNum
      dut.io.S_AXI_AWVALID.poke(true.B)
      dut.io.S_AXI_AWADDR.poke(0.U)
      dut.clock.step()

      dut.io.S_AXI_AWVALID.poke(false.B)
      dut.io.S_AXI_WVALID.poke(true.B)
      dut.io.S_AXI_WDATA.poke("hFFFFFFFFFFFFFFFF".U)
      dut.io.S_AXI_WSTRB.poke("hFF".U)
      dut.clock.step()

      dut.io.S_AXI_WVALID.poke(false.B)
      dut.io.S_AXI_BREADY.poke(true.B)
      while (!dut.io.S_AXI_BVALID.peek().litToBoolean) {
        dut.clock.step(1)
      }

      // write valid
      dut.io.S_AXI_AWVALID.poke(true.B)
      dut.io.S_AXI_AWADDR.poke(1024.U)
      dut.clock.step()

      dut.io.S_AXI_AWVALID.poke(false.B)
      dut.io.S_AXI_WVALID.poke(true.B)
      dut.io.S_AXI_WDATA.poke("h01".U)
      dut.io.S_AXI_WSTRB.poke("h01".U)
      dut.clock.step()

      dut.io.S_AXI_WVALID.poke(false.B)
      dut.io.S_AXI_BREADY.poke(true.B)
      while (!dut.io.S_AXI_BVALID.peek().litToBoolean) {
        dut.clock.step(1)
      }

      dut.io.S_AXI_ARVALID.poke(true.B)
      dut.io.S_AXI_ARADDR.poke(1024.U)
      dut.clock.step(1)

      dut.io.S_AXI_ARVALID.poke(false.B)
      dut.io.S_AXI_RREADY.poke(true.B)
      while (!dut.io.S_AXI_RVALID.peek().litToBoolean) {
        dut.clock.step(1)
      }

      /*
      dut.io.S_AXI_AWVALID.poke(true.B)
      dut.io.S_AXI_AWADDR.poke(0.U)
      dut.clock.step()

      dut.io.S_AXI_AWVALID.poke(false.B)
      dut.io.S_AXI_WVALID.poke(true.B)
      dut.io.S_AXI_WDATA.poke("h0f".U)
      dut.io.S_AXI_WSTRB.poke("h01".U)
      dut.clock.step()

      dut.io.S_AXI_WVALID.poke(false.B)
      dut.io.S_AXI_BREADY.poke(true.B)
      while (!dut.io.S_AXI_BVALID.peek().litToBoolean) {
        dut.clock.step(1)
      }

      dut.io.S_AXI_ARVALID.poke(true.B)
      dut.io.S_AXI_ARADDR.poke(0.U)
      dut.clock.step(1)

      dut.io.S_AXI_ARVALID.poke(false.B)
      dut.io.S_AXI_RREADY.poke(true.B)
      while (!dut.io.S_AXI_RVALID.peek().litToBoolean) {
        dut.clock.step(1)
      }
      */

      for (i <- 0 until (10)) {
        dut.clock.step()
      }

    }
  }
}

class AXI4LiteSlaveStateMachineTestBench extends AnyFlatSpec with ChiselScalatestTester {
  "AXI4LiteSlaveStateMachineTestBench" should "work" in {
    test(new AXI4LiteSlaveStateMachine(C_S_AXI_DATA_WIDTH = 32, C_S_AXI_ADDR_WIDTH = 11, bramDepth = 1024)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.clock.setTimeout(3000)

      dut.reset.poke(true.B)
      for (i <- 0 until (5)) {
        dut.clock.step()
      }
      dut.reset.poke(false.B)
      dut.clock.step()

      // write 1st FFFFFFFF to testNum
      dut.io.S_AXI_AWVALID.poke(true.B)
      dut.io.S_AXI_AWADDR.poke(0.U)
      dut.clock.step()
      dut.clock.step()
      // while (!(dut.io.S_AXI_AWVALID.peek().litToBoolean & dut.io.S_AXI_AWREADY.peek().litToBoolean)) {
      //   dut.clock.step(1)
      // }

      dut.io.S_AXI_AWVALID.poke(false.B)
      dut.io.S_AXI_WVALID.poke(true.B)
      dut.io.S_AXI_WDATA.poke("hFFFFFFFF".U)
      dut.io.S_AXI_WSTRB.poke("hF".U)
      dut.clock.step()
      dut.clock.step()
      // while (!(dut.io.S_AXI_WVALID.peek().litToBoolean & dut.io.S_AXI_WREADY.peek().litToBoolean)) {
      //   dut.clock.step(1)
      // }

      dut.io.S_AXI_WVALID.poke(false.B)
      dut.io.S_AXI_BREADY.poke(true.B)
      dut.clock.step(6)
      // while (!(dut.io.S_AXI_BVALID.peek().litToBoolean & dut.io.S_AXI_BREADY.peek().litToBoolean)) {
      //   dut.clock.step(1)
      // }

      // write 2nd FFFFFFFF to testNum
      dut.io.S_AXI_AWVALID.poke(true.B)
      dut.io.S_AXI_AWADDR.poke(4.U)
      dut.clock.step()
      dut.clock.step()

      dut.io.S_AXI_AWVALID.poke(false.B)
      dut.io.S_AXI_WVALID.poke(true.B)
      dut.io.S_AXI_WDATA.poke("hFFFFFFFF".U)
      dut.io.S_AXI_WSTRB.poke("hF".U)
      dut.clock.step()
      dut.clock.step()

      dut.io.S_AXI_WVALID.poke(false.B)
      dut.io.S_AXI_BREADY.poke(true.B)
      dut.clock.step(6)

      // read 1st testNum
      dut.io.S_AXI_ARVALID.poke(true.B)
      dut.io.S_AXI_ARADDR.poke(0.U)
      dut.clock.step(1)
      dut.clock.step(1)

      dut.io.S_AXI_ARVALID.poke(false.B)
      dut.io.S_AXI_RREADY.poke(true.B)
      dut.clock.step(8)

      // read 2nd testNum
      dut.io.S_AXI_ARVALID.poke(true.B)
      dut.io.S_AXI_ARADDR.poke(4.U)
      dut.clock.step(1)
      dut.clock.step(1)

      dut.io.S_AXI_ARVALID.poke(false.B)
      dut.io.S_AXI_RREADY.poke(true.B)
      dut.clock.step(8)

      // write valid
      dut.io.S_AXI_AWVALID.poke(true.B)
      dut.io.S_AXI_AWADDR.poke(1024.U)
      dut.clock.step()
      dut.clock.step()

      dut.io.S_AXI_AWVALID.poke(false.B)
      dut.io.S_AXI_WVALID.poke(true.B)
      dut.io.S_AXI_WDATA.poke("h01".U)
      dut.io.S_AXI_WSTRB.poke("h1".U)
      dut.clock.step()
      dut.clock.step()

      dut.io.S_AXI_WVALID.poke(false.B)
      dut.io.S_AXI_BREADY.poke(true.B)
      dut.clock.step(3)

      dut.clock.step(20)

      // read ready
      dut.io.S_AXI_ARVALID.poke(true.B)
      dut.io.S_AXI_ARADDR.poke(1024.U)
      dut.clock.step(1)
      dut.clock.step(1)

      dut.io.S_AXI_ARVALID.poke(false.B)
      dut.io.S_AXI_RREADY.poke(true.B)
      dut.clock.step(7)

      for (i <- 0 until (50)) {
        dut.clock.step()
      }

    }
  }
}

class BlockMemoryOptimizedTestBench extends AnyFlatSpec with ChiselScalatestTester {
  "BlockMemoryOptimizedTestBench" should "work" in {
    test(new BlockMemoryOptimized(depth = 1024, width = 8)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.clock.setTimeout(3000)

      dut.reset.poke(true.B)
      for (i <- 0 until (5)) {
        dut.clock.step()
      }
      dut.reset.poke(false.B)
      dut.clock.step()

      /*
      // 1byte write mode
      for(i <- 0 until 10) {
          dut.io.mode.poke(2.U)
          dut.io.writeAddr.poke((i*8).U)
          dut.io.writeOffset.poke(0.U)
          dut.io.writeData.poke("h100f0e0d0c0b0a09".U)
          dut.io.writeLen.poke(8.U)
          // dut.io.writeData.poke((i+2).U)
          // dut.io.writeLen.poke(1.U)
          //dut.clock.step(3)
          dut.clock.step(17)
          dut.io.mode.poke(0.U)
          dut.clock.step()
      }

      // 1byte read mode
      for(i <- 0 until 40) {
        dut.io.mode.poke(1.U)
        dut.io.readAddr.poke(i.U)
        dut.io.readOffset.poke(0.U)
        dut.io.readLen.poke(1.U)
        dut.clock.step(4)
        dut.io.mode.poke(0.U)
        dut.clock.step()
      }
      */

      // 1byte dma mode
      dut.io.mode.poke(3.U)
      dut.io.dmaSrcAddr.poke(0.U)
      dut.io.dmaDstAddr.poke("hF0".U)
      dut.io.dmaDstOffset.poke(0.U)
      //dut.io.dmaSrcLen.poke(20.U)
      dut.io.dmaSrcLen.poke(0.U)
      // dut.io.dmaDstLen.poke(20.U)
      // dut.clock.step(64)
      dut.io.dmaDstLen.poke(25.U)
      dut.clock.step(79)
      dut.io.mode.poke(0.U)
      dut.clock.step()

      // 1byte read 0ode
      for(i <- 0 until 5) {
        dut.io.mode.poke(1.U)
        dut.io.readAddr.poke((0xF0+i*8).U)
        dut.io.readOffset.poke(0.U)
        dut.io.readLen.poke(8.U)
        dut.clock.step(33)
        dut.io.mode.poke(0.U)
        dut.clock.step()
      }

      /*
      // 1byte read mode
      for(i <- 0 until 20) {
        dut.io.mode.poke(1.U)
        dut.io.readAddr.poke(i.U)
        dut.io.readOffset.poke(0.U)
        dut.io.readLen.poke(1.U)
        dut.clock.step(4)
        dut.io.mode.poke(0.U)
        dut.clock.step()
      }
      */

      /*
      // 4byte read mode
      for(i <- 0 until 5) {
        dut.io.mode.poke(1.U)
        dut.io.readAddr.poke((i*4).U)
        dut.io.readOffset.poke(0.U)
        dut.io.readLen.poke(4.U)
        // dut.io.readAddr.poke(i.U)
        // dut.io.readOffset.poke(1.U)
        dut.clock.step(17)
        dut.io.mode.poke(0.U)
        dut.clock.step()
      }
      */

      /*
      // 8byte read mode
      for(i <- 0 until 5) {
        dut.io.mode.poke(1.U)
        dut.io.readAddr.poke((i*8).U)
        dut.io.readOffset.poke(0.U)
        dut.io.readLen.poke(8.U)
        // dut.io.readAddr.poke(i.U)
        // dut.io.readOffset.poke(1.U)
        dut.clock.step(33)
        dut.io.mode.poke(0.U)
        dut.clock.step()
      }
      */

      for (i <- 0 until (10)) {
        dut.clock.step()
      }

    }
  }
}

class LabelToFSMTestBench extends AnyFlatSpec with ChiselScalatestTester {
  "LabelToFSMTestBench" should "work" in {
    test(new LabelToFSM(1227, 128)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.clock.setTimeout(3000)

      dut.reset.poke(true.B)
      for (i <- 0 until (5)) {
        dut.clock.step()
      }
      dut.reset.poke(false.B)
      dut.clock.step()

      dut.io.start.poke(true.B)
      dut.io.originalCpIndex.poke(2.U)
      dut.io.label.poke(299.U)
      dut.clock.step(3)

      dut.io.start.poke(false.B)
      dut.clock.step()
      
      dut.io.start.poke(true.B)
      dut.io.originalCpIndex.poke(1.U)
      dut.io.label.poke(300.U)
      dut.clock.step(3)

      dut.io.start.poke(false.B)
      dut.clock.step()

      for (i <- 0 until (10)) {
        dut.clock.step()
      }

    }
  }
}

class CombinationalLoopTestBench extends AnyFlatSpec with ChiselScalatestTester {
  "CombinationalLoopTestBench" should "work" in {
    test(new CombinationalLoopWireRegNext()).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.clock.setTimeout(3000)

      dut.reset.poke(true.B)
      for (i <- 0 until (5)) {
        dut.clock.step()
      }
      dut.reset.poke(false.B)
      dut.clock.step()

      dut.io.start.poke(true.B)
      dut.io.in.poke(2.U)
      dut.clock.step(10)

      for (i <- 0 until (10)) {
        dut.clock.step()
      }

    }
  }
}

class MultiStateMachineWithSignalTestBench extends AnyFlatSpec with ChiselScalatestTester {
  "MultiStateMachineWithSignalTestBench" should "work" in {
    test(new MultiStateMachineWithSignalSafe()).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.clock.setTimeout(3000)

      dut.reset.poke(true.B)
      for (i <- 0 until (5)) {
        dut.clock.step()
      }
      dut.reset.poke(false.B)
      dut.clock.step()

      dut.io.start.poke(true.B)
      dut.io.in.poke(2.U)
      dut.clock.step(10)

      for (i <- 0 until (100)) {
        dut.clock.step()
      }

    }
  }
}

class BroadcastBufferTestBench extends AnyFlatSpec with ChiselScalatestTester {
  "BroadcastBufferTestBench" should "work" in {
    test(new BroadcastBuffer(new StateBundle, 4)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.clock.setTimeout(3000)

      dut.reset.poke(true.B)
      for (i <- 0 until (5)) {
        dut.clock.step()
      }
      dut.reset.poke(false.B)
      dut.clock.step()

      // 设置初始 ready
      for (i <- 0 until 4) {
        dut.io.out(i).ready.poke(true.B)
      }

      // 输入一个 valid 信号
      dut.io.in(1).valid.poke(true.B)
      dut.io.in(1).bits.index.poke(2.U)
      dut.io.in(1).bits.state.poke(99.U)

      for (i <- Seq(0,2,3)) {
        dut.io.in(i).valid.poke(false.B)
      }

      dut.clock.step(2)
      dut.io.in(1).valid.poke(false.B)

      // for (i <- 0 until 4) {
      //   dut.io.out(i).valid.expect(true.B)
      //   dut.io.out(i).bits.index.expect(2.U)
      //   dut.io.out(i).bits.state.expect(99.U)
      // }

      for (i <- 0 until (100)) {
        dut.clock.step()
      }

    }
  }
}

class MultiStateMachineWithBroadcastBufferTestBench extends AnyFlatSpec with ChiselScalatestTester {
  "MultiStateMachineWithBroadcastBufferTestBench" should "work" in {
    test(new MultiStateMachineWithBroadcastBuffer()).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.clock.setTimeout(3000)

      dut.reset.poke(true.B)
      for (i <- 0 until (5)) {
        dut.clock.step()
      }
      dut.reset.poke(false.B)
      dut.clock.step()

      dut.io.start.poke(true.B)
      dut.io.in.poke(2.U)
      dut.clock.step(10)

      for (i <- 0 until (100)) {
        dut.clock.step()
      }

    }
  }
}

class AXI4LiteSlaveStateMachineRegTestBench extends AnyFlatSpec with ChiselScalatestTester {
  "AXI4LiteSlaveStateMachineRegTestBench" should "work" in {
    test(new AXI4LiteSlaveStateMachineReg(C_S_AXI_DATA_WIDTH = 32, C_S_AXI_ADDR_WIDTH = 11, bramDepth = 1024)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.clock.setTimeout(3000)

      dut.reset.poke(true.B)
      for (i <- 0 until (5)) {
        dut.clock.step()
      }
      dut.reset.poke(false.B)
      dut.clock.step()

      // write 1st FFFFFFFF to testNum
      dut.io.S_AXI_AWVALID.poke(true.B)
      dut.io.S_AXI_AWADDR.poke(0.U)
      dut.clock.step()

      dut.io.S_AXI_AWVALID.poke(false.B)
      dut.io.S_AXI_WVALID.poke(true.B)
      dut.io.S_AXI_WDATA.poke("hFFFFFFFF".U)
      dut.io.S_AXI_WSTRB.poke("hF".U)
      dut.clock.step(2)

      dut.io.S_AXI_WVALID.poke(false.B)
      dut.io.S_AXI_BREADY.poke(true.B)
      dut.clock.step(10)

      // write 2nd FFFFFFFF to testNum
      dut.io.S_AXI_AWVALID.poke(true.B)
      dut.io.S_AXI_AWADDR.poke(4.U)
      dut.clock.step()

      dut.io.S_AXI_AWVALID.poke(false.B)
      dut.io.S_AXI_WVALID.poke(true.B)
      dut.io.S_AXI_WDATA.poke("hFFFFFFFF".U)
      dut.io.S_AXI_WSTRB.poke("hF".U)
      dut.clock.step(2)

      dut.io.S_AXI_WVALID.poke(false.B)
      dut.io.S_AXI_BREADY.poke(true.B)
      dut.clock.step(10)

      // read 1st testNum
      dut.io.S_AXI_ARVALID.poke(true.B)
      dut.io.S_AXI_ARADDR.poke(0.U)
      dut.clock.step(1)

      dut.io.S_AXI_ARVALID.poke(false.B)
      dut.io.S_AXI_RREADY.poke(true.B)
      dut.clock.step(12)

      // read 2nd testNum
      dut.io.S_AXI_ARVALID.poke(true.B)
      dut.io.S_AXI_ARADDR.poke(4.U)
      dut.clock.step(1)

      dut.io.S_AXI_ARVALID.poke(false.B)
      dut.io.S_AXI_RREADY.poke(true.B)
      dut.clock.step(12)

      // write valid
      dut.io.S_AXI_AWVALID.poke(true.B)
      dut.io.S_AXI_AWADDR.poke(1024.U)
      dut.clock.step()

      dut.io.S_AXI_AWVALID.poke(false.B)
      dut.io.S_AXI_WVALID.poke(true.B)
      dut.io.S_AXI_WDATA.poke("h01".U)
      dut.io.S_AXI_WSTRB.poke("h1".U)
      dut.clock.step()
      dut.clock.step()

      dut.io.S_AXI_WVALID.poke(false.B)
      dut.io.S_AXI_BREADY.poke(true.B)
      dut.clock.step(4)

      dut.clock.step(20)

      // read ready
      dut.io.S_AXI_ARVALID.poke(true.B)
      dut.io.S_AXI_ARADDR.poke(1024.U)
      dut.clock.step(1)

      dut.io.S_AXI_ARVALID.poke(false.B)
      dut.io.S_AXI_RREADY.poke(true.B)
      dut.clock.step(10)

      for (i <- 0 until (50)) {
        dut.clock.step()
      }

    }
  }
}

class AXI4LiteSlaveStateMachineWithoutBRAMTestBench extends AnyFlatSpec with ChiselScalatestTester {
  "AXI4LiteSlaveStateMachineWithoutBRAMTestBench" should "work" in {
    test(new AXI4LiteSlaveStateMachineWithoutBRAM(C_S_AXI_DATA_WIDTH = 32, C_S_AXI_ADDR_WIDTH = 11)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.clock.setTimeout(3000)
      
      dut.reset.poke(false.B)
      for (i <- 0 until (5)) {
        dut.clock.step()
      }
      dut.reset.poke(true.B)
      dut.clock.step()

      // write 1 to valid 
      dut.io.S_AXI_AWVALID.poke(true.B)
      dut.io.S_AXI_AWADDR.poke(0.U)
      dut.io.S_AXI_WVALID.poke(true.B)
      dut.io.S_AXI_WDATA.poke("h01".U)
      dut.io.S_AXI_WSTRB.poke("h1".U)
      dut.clock.step(2)

      dut.io.S_AXI_AWVALID.poke(false.B)
      dut.io.S_AXI_WVALID.poke(false.B)
      dut.io.S_AXI_BREADY.poke(true.B)
      dut.clock.step(3)

      // write 8 to ready 
      dut.io.S_AXI_AWVALID.poke(true.B)
      dut.io.S_AXI_AWADDR.poke(4.U)
      dut.io.S_AXI_WVALID.poke(true.B)
      dut.io.S_AXI_WDATA.poke("h08".U)
      dut.io.S_AXI_WSTRB.poke("h1".U)
      dut.clock.step(2)

      dut.io.S_AXI_AWVALID.poke(false.B)
      dut.io.S_AXI_WVALID.poke(false.B)
      dut.io.S_AXI_BREADY.poke(true.B)
      dut.clock.step(2)

      // read valid 
      dut.io.S_AXI_ARVALID.poke(true.B)
      dut.io.S_AXI_ARADDR.poke(0.U)
      dut.clock.step(2)

      dut.io.S_AXI_ARVALID.poke(false.B)
      dut.io.S_AXI_RREADY.poke(true.B)
      dut.clock.step(3)

      // read ready
      dut.io.S_AXI_ARVALID.poke(true.B)
      dut.io.S_AXI_ARADDR.poke(4.U)
      dut.clock.step(2)

      dut.io.S_AXI_ARVALID.poke(false.B)
      dut.io.S_AXI_RREADY.poke(true.B)
      dut.clock.step()

      for (i <- 0 until (50)) {
        dut.clock.step()
      }

    }
  }
}

class AXI4LiteMasterSlaveTestBench extends AnyFlatSpec with ChiselScalatestTester {
  "AXI4LiteMasterSlaveTestBench" should "work" in {
    test(new AXI4_Master_Slave()).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.clock.setTimeout(3000)

      dut.reset.poke(false.B)
      for (i <- 0 until (5)) {
        dut.clock.step()
      }
      dut.reset.poke(true.B)
      dut.clock.step()

      for (i <- 0 until (100)) {
        dut.clock.step()
      }

    }
  }
}

class AXI4LiteMasterSlave_AXIDMA_TestBench extends AnyFlatSpec with ChiselScalatestTester {
  "AXI4LiteMasterSlave_AXIDMA_TestBench " should "work" in {
    test(new AXI4LiteMasterSlave_AXIDMA(C_S_AXI_DATA_WIDTH = 32, C_S_AXI_ADDR_WIDTH = 11, BASE_MEM_ADDR = 0xA0000000)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.clock.setTimeout(3000)

      dut.reset.poke(false.B)
      for (i <- 0 until (5)) {
        dut.clock.step()
      }
      dut.reset.poke(true.B)
      dut.clock.step()

      // write to MM2S_SA
      dut.io.S_AXI_AWVALID.poke(true.B)
      dut.io.S_AXI_AWADDR.poke(0.U)
      dut.clock.step(2)

      dut.io.S_AXI_AWVALID.poke(false.B)
      dut.io.S_AXI_WVALID.poke(true.B)
      dut.io.S_AXI_WDATA.poke("h0".U)
      dut.io.S_AXI_WSTRB.poke("hF".U)
      dut.clock.step(2)

      dut.io.S_AXI_WVALID.poke(false.B)
      dut.io.S_AXI_BREADY.poke(true.B)
      dut.clock.step(3)

      // write to MM2S_LENGTH
      dut.io.S_AXI_AWVALID.poke(true.B)
      dut.io.S_AXI_AWADDR.poke(4.U)
      dut.clock.step(2)

      dut.io.S_AXI_AWVALID.poke(false.B)
      dut.io.S_AXI_WVALID.poke(true.B)
      dut.io.S_AXI_WDATA.poke("h00000040".U)
      dut.io.S_AXI_WSTRB.poke("hF".U)
      dut.clock.step(2)

      dut.io.S_AXI_WVALID.poke(false.B)
      dut.io.S_AXI_BREADY.poke(true.B)
      dut.clock.step(3)

      // write to S2MM_SA
      dut.io.S_AXI_AWVALID.poke(true.B)
      dut.io.S_AXI_AWADDR.poke(8.U)
      dut.clock.step(2)

      dut.io.S_AXI_AWVALID.poke(false.B)
      dut.io.S_AXI_WVALID.poke(true.B)
      dut.io.S_AXI_WDATA.poke("h100".U)
      dut.io.S_AXI_WSTRB.poke("hF".U)
      dut.clock.step(2)

      dut.io.S_AXI_WVALID.poke(false.B)
      dut.io.S_AXI_BREADY.poke(true.B)
      dut.clock.step(3)

      // write to S2MM_LENGTH
      dut.io.S_AXI_AWVALID.poke(true.B)
      dut.io.S_AXI_AWADDR.poke(12.U)
      dut.clock.step(2)

      dut.io.S_AXI_AWVALID.poke(false.B)
      dut.io.S_AXI_WVALID.poke(true.B)
      dut.io.S_AXI_WDATA.poke("h40".U)
      dut.io.S_AXI_WSTRB.poke("hF".U)
      dut.clock.step(2)

      dut.io.S_AXI_WVALID.poke(false.B)
      dut.io.S_AXI_BREADY.poke(true.B)
      dut.clock.step(3)

      // write to MM2S_DMACR
      dut.io.S_AXI_AWVALID.poke(true.B)
      dut.io.S_AXI_AWADDR.poke(16.U)
      dut.clock.step(2)

      dut.io.S_AXI_AWVALID.poke(false.B)
      dut.io.S_AXI_WVALID.poke(true.B)
      dut.io.S_AXI_WDATA.poke("h1001".U)
      dut.io.S_AXI_WSTRB.poke("hF".U)
      dut.clock.step(2)

      dut.io.S_AXI_WVALID.poke(false.B)
      dut.io.S_AXI_BREADY.poke(true.B)
      dut.clock.step(3)

      // write to S2MM_DMACR
      dut.io.S_AXI_AWVALID.poke(true.B)
      dut.io.S_AXI_AWADDR.poke(20.U)
      dut.clock.step(2)

      dut.io.S_AXI_AWVALID.poke(false.B)
      dut.io.S_AXI_WVALID.poke(true.B)
      dut.io.S_AXI_WDATA.poke("h1001".U)
      dut.io.S_AXI_WSTRB.poke("hF".U)
      dut.clock.step(2)

      dut.io.S_AXI_WVALID.poke(false.B)
      dut.io.S_AXI_BREADY.poke(true.B)
      dut.clock.step(3)

      // write to control
      dut.io.S_AXI_AWVALID.poke(true.B)
      dut.io.S_AXI_AWADDR.poke(24.U)
      dut.clock.step(2)

      dut.io.S_AXI_AWVALID.poke(false.B)
      dut.io.S_AXI_WVALID.poke(true.B)
      dut.io.S_AXI_WDATA.poke("h1".U)
      dut.io.S_AXI_WSTRB.poke("hF".U)
      dut.clock.step(2)

      dut.io.S_AXI_WVALID.poke(false.B)
      dut.io.S_AXI_BREADY.poke(true.B)
      dut.clock.step(3)

      /***********AXI4 Lite Slave read logic****************/
      // read MM2S_SA
      dut.io.S_AXI_ARVALID.poke(true.B)
      dut.io.S_AXI_ARADDR.poke(0.U)
      dut.clock.step(2)

      dut.io.S_AXI_ARVALID.poke(false.B)
      dut.io.S_AXI_RREADY.poke(true.B)
      dut.clock.step(3)

      // read MM2S_LENGTH
      dut.io.S_AXI_ARVALID.poke(true.B)
      dut.io.S_AXI_ARADDR.poke(4.U)
      dut.clock.step(2)

      dut.io.S_AXI_ARVALID.poke(false.B)
      dut.io.S_AXI_RREADY.poke(true.B)
      dut.clock.step(3)

      // read S2MM_DA
      dut.io.S_AXI_ARVALID.poke(true.B)
      dut.io.S_AXI_ARADDR.poke(8.U)
      dut.clock.step(2)

      dut.io.S_AXI_ARVALID.poke(false.B)
      dut.io.S_AXI_RREADY.poke(true.B)
      dut.clock.step(3)

      // read S2MM_LENGTH
      dut.io.S_AXI_ARVALID.poke(true.B)
      dut.io.S_AXI_ARADDR.poke(12.U)
      dut.clock.step(2)

      dut.io.S_AXI_ARVALID.poke(false.B)
      dut.io.S_AXI_RREADY.poke(true.B)
      dut.clock.step(3)

      // read MM2S_DMACR
      dut.io.S_AXI_ARVALID.poke(true.B)
      dut.io.S_AXI_ARADDR.poke(16.U)
      dut.clock.step(2)

      dut.io.S_AXI_ARVALID.poke(false.B)
      dut.io.S_AXI_RREADY.poke(true.B)
      dut.clock.step(3)

      // read S2MM_DMACR
      dut.io.S_AXI_ARVALID.poke(true.B)
      dut.io.S_AXI_ARADDR.poke(20.U)
      dut.clock.step(2)

      dut.io.S_AXI_ARVALID.poke(false.B)
      dut.io.S_AXI_RREADY.poke(true.B)
      dut.clock.step(3)

      // read control
      dut.io.S_AXI_ARVALID.poke(true.B)
      dut.io.S_AXI_ARADDR.poke(24.U)
      dut.clock.step(2)

      dut.io.S_AXI_ARVALID.poke(false.B)
      dut.io.S_AXI_RREADY.poke(true.B)
      dut.clock.step(3)

      for (i <- 0 until (50)) {
        dut.clock.step()
      }

    }
  }
}

class RecursiveFactorialTestBench extends AnyFlatSpec with ChiselScalatestTester {
  "RecursiveFactorialTestBench" should "work" in {
    test(new RecursiveFactorial(width = 8, depth = 1024)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.clock.setTimeout(3000)

      dut.reset.poke(true.B)
      for (i <- 0 until (5)) {
        dut.clock.step()
      }
      dut.reset.poke(false.B)
      dut.clock.step()

      dut.io.n.poke(3.U)
      dut.io.start.poke(true.B)

      for (i <- 0 until (100)) {
        dut.clock.step()
      }

    }
  }
}

class BigBoxTestBench extends AnyFlatSpec with ChiselScalatestTester {
  "BigBoxTestBench" should "work" in {
    test(new BigBox()).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.clock.setTimeout(3000)

      dut.reset.poke(true.B)
      for (i <- 0 until (5)) {
        dut.clock.step()
      }
      dut.reset.poke(false.B)
      dut.clock.step()

      dut.io.out.ready.poke(true.B)

      for (i <- 0 until (100)) {
        dut.clock.step()
      }

    }
  }
}