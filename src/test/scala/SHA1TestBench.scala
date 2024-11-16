package SHA1

import chisel3._
import chiseltest._
import chiseltest.simulator.WriteVcdAnnotation
import org.scalatest.flatspec.AnyFlatSpec

class SHA1TestBench extends AnyFlatSpec with ChiselScalatestTester {
  "SHA1TestBench" should "fill" in {
    test(new fill).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.reset.poke(true.B)
      for(i <- 0 until(5)) {
        dut.clock.step()
      }
      dut.reset.poke(false.B)

      for(i <- 0 until(20)) {
        dut.io.arr(i).poke(i.S)
        dut.clock.step()
      }

      for(i <- 0 until(20)) {
        dut.io.value.poke(i.S)
        dut.io.off.poke(0.S)
        dut.io.valid.poke(true.B)

        for(j <- 0 until(20)) {
          dut.clock.step()
        }
      }

    }
  }

  "SHA1TestBench" should "rol" in {
    test(new rol).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.reset.poke(true.B)
      for(i <- 0 until(5)) {
        dut.clock.step()
      }
      dut.reset.poke(false.B)

      for(i <- 10050 to 10079) {
        dut.io.num.poke(i.S)
        dut.io.cnt.poke(16.S)
        dut.clock.step()
      }

    }
  }

  "SHA1TestBench" should "digest" in {
    test(new digest).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.clock.setTimeout(3000)

      dut.reset.poke(true.B)
      for(i <- 0 until(5)) {
        dut.clock.step()
      }
      dut.reset.poke(false.B)

      dut.io.bytes(0).poke(65.S)
      dut.clock.step()
      dut.io.bytes(1).poke(66.S)
      dut.clock.step()
      dut.io.bytes(2).poke(67.S)
      dut.clock.step()
      dut.io.valid.poke(true.B)

      for(i <- 0 until(2500)) {
        dut.clock.step()
      }

    }
  }

}