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
    test(new AndSigned(64)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
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
      dut.clock.step()

      dut.clock.step()
      dut.clock.step()
      dut.clock.step()
      dut.clock.step()
      dut.clock.step()

      dut.io.baseOffset.poke(0.U(16.W))
      dut.io.dataOffset.poke(0.U(16.W))
      dut.io.index.poke(0.U(16.W))
      dut.io.elementSize.poke(0.U(16.W))
      dut.io.mask.poke("h0000".U)
      dut.io.ready.poke(false.B)
      dut.clock.step()

      dut.io.baseOffset.poke(52.U(16.W))
      dut.io.dataOffset.poke(12.U(16.W))
      dut.io.index.poke(1.U(16.W))
      dut.io.elementSize.poke(1.U(16.W))
      dut.io.mask.poke("hFFFF".U)
      dut.io.ready.poke(true.B)
      dut.clock.step()

      dut.clock.step()
      dut.clock.step()
      dut.clock.step()
      dut.clock.step()
      dut.clock.step()
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