package FieldAccess

import chisel3._
import chiseltest._
import chiseltest.simulator.WriteVcdAnnotation
import org.scalatest.flatspec.AnyFlatSpec


class FieldTestBench extends AnyFlatSpec with ChiselScalatestTester {
  "FieldTestBench" should "returnField" in {
    test(new returnField).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.io.a.poke(15.U)
      dut.io.valid.poke(true.B)
      dut.io.b.i.poke(20.U)

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
      dut.io.a.i.poke(15.U)
      dut.io.valid.poke(true.B)

      for(i <- 0 until 200) {
        dut.clock.step()
      }

      dut.io.addr_returnFieldArray.poke(0.U)
      dut.clock.step()

      dut.io.addr_returnFieldArray.poke(1.U)
      dut.clock.step()

      dut.io.addr_returnFieldArray.poke(2.U)
      dut.clock.step()

      dut.io.addr_returnFieldArray.poke(3.U)
      dut.clock.step()

      dut.io.addr_returnFieldArray.poke(4.U)
      dut.clock.step()

    }
  }

}
