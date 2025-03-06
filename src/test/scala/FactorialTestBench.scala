package GeneralRegFileToBRAM 

import chisel3._
import chiseltest._
import chiseltest.simulator.WriteVcdAnnotation
import org.scalatest.flatspec.AnyFlatSpec

class FactorialTestBench extends AnyFlatSpec with ChiselScalatestTester {
  "FactorialTestBench" should "work" in {
    test(new Factorial()).withAnnotations(Seq(WriteVcdAnnotation, VerilatorBackendAnnotation)) { dut =>
      dut.clock.setTimeout(3000)

      dut.reset.poke(true.B)
      for (i <- 0 until (5)) {
        dut.clock.step()
      }
      dut.reset.poke(false.B)
      dut.clock.step()

      dut.io.arrayWe.poke(true.B)
      dut.io.arrayWriteAddr.poke(20.U)
      dut.io.arrayWData.poke(4.U)
      dut.io.arrayStrb.poke("b1111".U)
      dut.clock.step()

      dut.io.arrayWe.poke(false.B)
      dut.io.valid.poke(true.B)
      for(i <- 0 until 100) {
        dut.clock.step()
      }

    }
  }
}

class GeneratedFactorialTestBench extends AnyFlatSpec with ChiselScalatestTester {
  "GeneratedFactorialTestBench" should "work" in {
    test(new factorial()).withAnnotations(Seq(WriteVcdAnnotation, VerilatorBackendAnnotation)) { dut =>
      dut.clock.setTimeout(3000)

      dut.reset.poke(true.B)
      for (i <- 0 until (5)) {
        dut.clock.step()
      }
      dut.reset.poke(false.B)
      dut.clock.step()

      dut.io.arrayWe.poke(true.B)
      dut.io.arrayWriteAddr.poke(7.U)
      dut.io.arrayWData.poke(3.U)
      dut.io.arrayStrb.poke("b1111".U)
      dut.clock.step()

      dut.io.arrayWe.poke(false.B)
      dut.io.valid.poke(true.B)
      for(i <- 0 until 100) {
        dut.clock.step()
      }

    }
  }
}

class GeneratedAddTestBench extends AnyFlatSpec with ChiselScalatestTester {
  "GeneratedAddTestBench" should "work" in {
    test(new add()).withAnnotations(Seq(WriteVcdAnnotation, VerilatorBackendAnnotation)) { dut =>
      dut.clock.setTimeout(3000)

      dut.reset.poke(true.B)
      for (i <- 0 until (5)) {
        dut.clock.step()
      }
      dut.reset.poke(false.B)
      dut.clock.step()

      dut.io.arrayWe.poke(true.B)
      dut.io.arrayWriteAddr.poke(11.U)
      dut.io.arrayWData.poke("h05060708".U)
      dut.io.arrayStrb.poke("b1111".U)
      dut.clock.step()

      dut.io.arrayWe.poke(true.B)
      dut.io.arrayWriteAddr.poke(15.U)
      dut.io.arrayWData.poke("h01020304".U)
      dut.io.arrayStrb.poke("b1111".U)
      dut.clock.step()

      dut.io.arrayWe.poke(true.B)
      dut.io.arrayWriteAddr.poke(19.U)
      dut.io.arrayWData.poke("h04030201".U)
      dut.io.arrayStrb.poke("b1111".U)
      dut.clock.step()

      dut.io.arrayWe.poke(true.B)
      dut.io.arrayWriteAddr.poke(23.U)
      dut.io.arrayWData.poke("h08070605".U)
      dut.io.arrayStrb.poke("b1111".U)
      dut.clock.step()

      dut.io.arrayWe.poke(false.B)
      dut.io.valid.poke(true.B)
      for(i <- 0 until 100) {
        dut.clock.step()
      }

    }
  }
}

class GeneratedMultTestBench extends AnyFlatSpec with ChiselScalatestTester {
  "GeneratedMultTestBench" should "work" in {
    test(new mult()).withAnnotations(Seq(WriteVcdAnnotation, VerilatorBackendAnnotation)) { dut =>
      dut.clock.setTimeout(3000)

      dut.reset.poke(true.B)
      for (i <- 0 until (5)) {
        dut.clock.step()
      }
      dut.reset.poke(false.B)
      dut.clock.step()

      dut.io.arrayWe.poke(true.B)
      dut.io.arrayWriteAddr.poke(11.U)
      dut.io.arrayWData.poke(10.U)
      dut.io.arrayStrb.poke("b1111".U)
      dut.clock.step()

      dut.io.arrayWe.poke(true.B)
      dut.io.arrayWriteAddr.poke(19.U)
      dut.io.arrayWData.poke(4.U)
      dut.io.arrayStrb.poke("b1111".U)
      dut.clock.step()

      dut.io.arrayWe.poke(false.B)
      dut.io.valid.poke(true.B)
      for(i <- 0 until 100) {
        dut.clock.step()
      }

    }
  }
}

class GeneratedBubbleTestBench extends AnyFlatSpec with ChiselScalatestTester {
  "GeneratedBubbleTestBench" should "work" in {
    test(new bubble()).withAnnotations(Seq(WriteVcdAnnotation, VerilatorBackendAnnotation)) { dut =>
      dut.clock.setTimeout(3000)

      dut.reset.poke(true.B)
      for (i <- 0 until (5)) {
        dut.clock.step()
      }
      dut.reset.poke(false.B)
      dut.clock.step()

      dut.io.arrayWe.poke(true.B)
      dut.io.arrayWriteAddr.poke(7.U)
      dut.io.arrayWData.poke(4.U)
      dut.io.arrayStrb.poke("b1111".U)
      dut.clock.step()

      dut.io.arrayWe.poke(true.B)
      dut.io.arrayWriteAddr.poke(15.U)
      dut.io.arrayWData.poke("h00010004".U)
      dut.io.arrayStrb.poke("b1111".U)
      dut.clock.step()

      dut.io.arrayWe.poke(true.B)
      dut.io.arrayWriteAddr.poke(19.U)
      dut.io.arrayWData.poke("h00020003".U)
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

class GeneratedSumTestBench extends AnyFlatSpec with ChiselScalatestTester {
  "GeneratedSumTestBench" should "work" in {
    test(new sum()).withAnnotations(Seq(WriteVcdAnnotation, VerilatorBackendAnnotation)) { dut =>
      dut.clock.setTimeout(3000)

      dut.reset.poke(true.B)
      for (i <- 0 until (5)) {
        dut.clock.step()
      }
      dut.reset.poke(false.B)
      dut.clock.step()

      // size
      dut.io.arrayWe.poke(true.B)
      dut.io.arrayWriteAddr.poke(17.U)
      dut.io.arrayWData.poke("h00000004".U)
      dut.io.arrayStrb.poke("b1111".U)
      dut.clock.step()

      dut.io.arrayWe.poke(true.B)
      dut.io.arrayWriteAddr.poke(21.U)
      dut.io.arrayWData.poke("h00000000".U)
      dut.io.arrayStrb.poke("b1111".U)
      dut.clock.step()

      // first number
      dut.io.arrayWe.poke(true.B)
      dut.io.arrayWriteAddr.poke(25.U)
      dut.io.arrayWData.poke("h00000001".U)
      dut.io.arrayStrb.poke("b1111".U)
      dut.clock.step()

      dut.io.arrayWe.poke(true.B)
      dut.io.arrayWriteAddr.poke(29.U)
      dut.io.arrayWData.poke("h00000000".U)
      dut.io.arrayStrb.poke("b1111".U)
      dut.clock.step()

      // second number
      dut.io.arrayWe.poke(true.B)
      dut.io.arrayWriteAddr.poke(33.U)
      dut.io.arrayWData.poke("h00000002".U)
      dut.io.arrayStrb.poke("b1111".U)
      dut.clock.step()

      dut.io.arrayWe.poke(true.B)
      dut.io.arrayWriteAddr.poke(37.U)
      dut.io.arrayWData.poke("h00000000".U)
      dut.io.arrayStrb.poke("b1111".U)
      dut.clock.step()

      // third number
      dut.io.arrayWe.poke(true.B)
      dut.io.arrayWriteAddr.poke(41.U)
      dut.io.arrayWData.poke("h00000003".U)
      dut.io.arrayStrb.poke("b1111".U)
      dut.clock.step()

      dut.io.arrayWe.poke(true.B)
      dut.io.arrayWriteAddr.poke(45.U)
      dut.io.arrayWData.poke("h00000000".U)
      dut.io.arrayStrb.poke("b1111".U)
      dut.clock.step()

      // fourth number
      dut.io.arrayWe.poke(true.B)
      dut.io.arrayWriteAddr.poke(49.U)
      dut.io.arrayWData.poke("h00000004".U)
      dut.io.arrayStrb.poke("b1111".U)
      dut.clock.step()

      dut.io.arrayWe.poke(true.B)
      dut.io.arrayWriteAddr.poke(53.U)
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

class GeneratedSquareTestBench extends AnyFlatSpec with ChiselScalatestTester {
  "GeneratedSquareTestBench" should "work" in {
    test(new square()).withAnnotations(Seq(WriteVcdAnnotation, VerilatorBackendAnnotation)) { dut =>
      dut.clock.setTimeout(3000)

      dut.reset.poke(true.B)
      for (i <- 0 until (5)) {
        dut.clock.step()
      }
      dut.reset.poke(false.B)
      dut.clock.step()

      // first number
      dut.io.arrayWe.poke(true.B)
      dut.io.arrayWriteAddr.poke(15.U)
      dut.io.arrayWData.poke("h00000001".U)
      dut.io.arrayStrb.poke("b1111".U)
      dut.clock.step()

      dut.io.arrayWe.poke(true.B)
      dut.io.arrayWriteAddr.poke(19.U)
      dut.io.arrayWData.poke("h00000000".U)
      dut.io.arrayStrb.poke("b1111".U)
      dut.clock.step()

      // second number
      dut.io.arrayWe.poke(true.B)
      dut.io.arrayWriteAddr.poke(23.U)
      dut.io.arrayWData.poke("h00000002".U)
      dut.io.arrayStrb.poke("b1111".U)
      dut.clock.step()

      dut.io.arrayWe.poke(true.B)
      dut.io.arrayWriteAddr.poke(27.U)
      dut.io.arrayWData.poke("h00000000".U)
      dut.io.arrayStrb.poke("b1111".U)
      dut.clock.step()

      // third number
      dut.io.arrayWe.poke(true.B)
      dut.io.arrayWriteAddr.poke(31.U)
      dut.io.arrayWData.poke("h00000003".U)
      dut.io.arrayStrb.poke("b1111".U)
      dut.clock.step()

      dut.io.arrayWe.poke(true.B)
      dut.io.arrayWriteAddr.poke(35.U)
      dut.io.arrayWData.poke("h00000000".U)
      dut.io.arrayStrb.poke("b1111".U)
      dut.clock.step()

      // fourth number
      dut.io.arrayWe.poke(true.B)
      dut.io.arrayWriteAddr.poke(39.U)
      dut.io.arrayWData.poke("h00000004".U)
      dut.io.arrayStrb.poke("b1111".U)
      dut.clock.step()

      dut.io.arrayWe.poke(true.B)
      dut.io.arrayWriteAddr.poke(43.U)
      dut.io.arrayWData.poke("h00000000".U)
      dut.io.arrayStrb.poke("b1111".U)
      dut.clock.step()

      // fifth number
      dut.io.arrayWe.poke(true.B)
      dut.io.arrayWriteAddr.poke(47.U)
      dut.io.arrayWData.poke("h00000005".U)
      dut.io.arrayStrb.poke("b1111".U)
      dut.clock.step()

      dut.io.arrayWe.poke(true.B)
      dut.io.arrayWriteAddr.poke(51.U)
      dut.io.arrayWData.poke("h00000000".U)
      dut.io.arrayStrb.poke("b1111".U)
      dut.clock.step()

      // sixth number
      dut.io.arrayWe.poke(true.B)
      dut.io.arrayWriteAddr.poke(55.U)
      dut.io.arrayWData.poke("h00000006".U)
      dut.io.arrayStrb.poke("b1111".U)
      dut.clock.step()

      dut.io.arrayWe.poke(true.B)
      dut.io.arrayWriteAddr.poke(59.U)
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

class GeneratedConstructTestBench extends AnyFlatSpec with ChiselScalatestTester {
  "GeneratedConstructTestBench" should "work" in {
    test(new mkIS()).withAnnotations(Seq(WriteVcdAnnotation, VerilatorBackendAnnotation)) { dut =>
      dut.clock.setTimeout(3000)

      dut.reset.poke(true.B)
      for (i <- 0 until (5)) {
        dut.clock.step()
      }
      dut.reset.poke(false.B)
      dut.clock.step()

      // x
      dut.io.arrayWe.poke(true.B)
      dut.io.arrayWriteAddr.poke(815.U)
      dut.io.arrayWData.poke("h00000001".U)
      dut.io.arrayStrb.poke("b1111".U)
      dut.clock.step()

      dut.io.arrayWe.poke(true.B)
      dut.io.arrayWriteAddr.poke(819.U)
      dut.io.arrayWData.poke("h00000000".U)
      dut.io.arrayStrb.poke("b1111".U)
      dut.clock.step()

      // y
      dut.io.arrayWe.poke(true.B)
      dut.io.arrayWriteAddr.poke(823.U)
      dut.io.arrayWData.poke("h00000002".U)
      dut.io.arrayStrb.poke("b1111".U)
      dut.clock.step()

      dut.io.arrayWe.poke(true.B)
      dut.io.arrayWriteAddr.poke(827.U)
      dut.io.arrayWData.poke("h00000000".U)
      dut.io.arrayStrb.poke("b1111".U)
      dut.clock.step()

      // z
      dut.io.arrayWe.poke(true.B)
      dut.io.arrayWriteAddr.poke(831.U)
      dut.io.arrayWData.poke("h00000003".U)
      dut.io.arrayStrb.poke("b1111".U)
      dut.clock.step()

      dut.io.arrayWe.poke(true.B)
      dut.io.arrayWriteAddr.poke(835.U)
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