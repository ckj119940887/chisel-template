package FieldAccess

import chisel3._
import chiseltest._
import chiseltest.simulator.WriteVcdAnnotation
import org.scalatest.flatspec.AnyFlatSpec


class FieldTestBench extends AnyFlatSpec with ChiselScalatestTester {
  "FieldTestBench" should "returnField" in {
    test(new returnField).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.io.a.poke(15.S)
      dut.io.valid.poke(true.B)
      dut.io.b.i.poke(20.S)

      dut.clock.step()
      dut.clock.step()
      dut.clock.step()
      dut.clock.step()
      dut.clock.step()
      dut.clock.step()

    }
  }

  "FieldTestBench" should "returnFieldArray" in {
    test(new returnFieldArray).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.io.a.i.poke(15.S)
      dut.io.valid.poke(true.B)

      for(i <- 0 until 200) {
        dut.clock.step()
      }
    }
  }

}
