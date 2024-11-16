package MethodInvocation

import chisel3._
import chiseltest._
import chiseltest.simulator.WriteVcdAnnotation
import org.scalatest.flatspec.AnyFlatSpec


class MethodInvocationTestBench extends AnyFlatSpec with ChiselScalatestTester {
  "MethodInvocationTestBench" should "returnArray" in {
    test(new returnArray).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.io.a.i.poke(15.S)
      dut.io.b.poke(1.S)
      dut.io.valid.poke(true.B)

      for(i <- 0 until(100)) {
        dut.clock.step()
      }

    }
  }

  "MethodInvocationTestBench" should "insertSort" in {
    test(new insertSort).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      for(i <- 0 until(10)) {
        dut.io.array(i).poke((10 - i).S)
      }

      dut.io.valid.poke(true.B)

      for(i <- 0 until 200) {
        dut.clock.step()
      }

    }
  }

  "MethodInvocationTestBench" should "testInsertSort" in {
    test(new testInsertSort).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.io.valid.poke(true.B)

      for(i <- 0 until 500) {
        dut.clock.step()
      }

    }
  }

}
