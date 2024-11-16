package NewArrayTest

import chisel3._
import chiseltest._
import chiseltest.simulator.WriteVcdAnnotation
import org.scalatest.flatspec.AnyFlatSpec
import scala.util.Random

class NewArrayTestBench extends AnyFlatSpec with ChiselScalatestTester {
  "NewArrayTestBench" should "returnIntArray" in {
    test(new returnIntArray).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.io.valid.poke(true.B)
      dut.io.a.poke(5.S)
      dut.io.b.poke(6.S)

      for(i <- 0 until(100)) {
        dut.clock.step()
      }

    }
  }

  "NewArrayTestBench" should "returnSum" in {
    test(new returnSum).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.io.initial.poke(15.S)
      dut.io.valid.poke(true.B)

      for(i <- 0 until(100)) {
        dut.clock.step()
      }

    }
  }
}