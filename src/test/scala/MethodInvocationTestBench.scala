package MethodInvocation

import chisel3._
import chiseltest._
import chiseltest.simulator.WriteVcdAnnotation
import org.scalatest.flatspec.AnyFlatSpec


class MethodInvocationTestBench extends AnyFlatSpec with ChiselScalatestTester {
  "MethodInvocationTestBench" should "returnArray" in {
    test(new returnArray).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.io.a.i.poke(15.U)
      dut.io.b.poke(1.U)
      dut.io.valid.poke(true.B)

      dut.clock.step()
      dut.clock.step()
      dut.clock.step()
      dut.clock.step()
      dut.clock.step()
      dut.clock.step()
      dut.clock.step()
      dut.clock.step()
      dut.clock.step()
      dut.clock.step()
      dut.clock.step()
      dut.clock.step()
      dut.clock.step()
      dut.clock.step()
      dut.clock.step()
      dut.clock.step()
      dut.clock.step()
      dut.clock.step()
      dut.clock.step()
      dut.clock.step()
      dut.clock.step()
      dut.clock.step()
      dut.clock.step()
      dut.clock.step()
      dut.clock.step()
      dut.clock.step()
      dut.clock.step()
      dut.clock.step()
      dut.clock.step()
      dut.clock.step()
      dut.clock.step()
      dut.clock.step()
      dut.clock.step()
      dut.clock.step()
      dut.clock.step()
      dut.clock.step()
      dut.clock.step()
      dut.clock.step()
      dut.clock.step()
      dut.clock.step()
      dut.clock.step()
      dut.clock.step()
      dut.clock.step()
      dut.clock.step()
      dut.clock.step()
      dut.clock.step()
      dut.clock.step()
      dut.clock.step()
      dut.clock.step()
      dut.clock.step()
      dut.clock.step()
      dut.clock.step()
      dut.clock.step()
      dut.clock.step()
      dut.clock.step()
      dut.clock.step()
      dut.clock.step()
      dut.clock.step()
      dut.clock.step()
      dut.clock.step()

    }
  }

  "MethodInvocationTestBench" should "insertSort" in {
    test(new insertSort).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.io.addr_insertSort.poke(0.U)
      dut.io.din_insertSort.poke(10.U)
      dut.io.we_insertSort.poke(true.B)
      dut.clock.step()

      dut.io.addr_insertSort.poke(1.U)
      dut.io.din_insertSort.poke(9.U)
      dut.io.we_insertSort.poke(true.B)
      dut.clock.step()

      dut.io.addr_insertSort.poke(2.U)
      dut.io.din_insertSort.poke(8.U)
      dut.io.we_insertSort.poke(true.B)
      dut.clock.step()

      dut.io.addr_insertSort.poke(3.U)
      dut.io.din_insertSort.poke(7.U)
      dut.io.we_insertSort.poke(true.B)
      dut.clock.step()

      dut.io.addr_insertSort.poke(4.U)
      dut.io.din_insertSort.poke(6.U)
      dut.io.we_insertSort.poke(true.B)
      dut.clock.step()

      dut.io.addr_insertSort.poke(5.U)
      dut.io.din_insertSort.poke(5.U)
      dut.io.we_insertSort.poke(true.B)
      dut.clock.step()

      dut.io.addr_insertSort.poke(6.U)
      dut.io.din_insertSort.poke(4.U)
      dut.io.we_insertSort.poke(true.B)
      dut.clock.step()

      dut.io.addr_insertSort.poke(7.U)
      dut.io.din_insertSort.poke(3.U)
      dut.io.we_insertSort.poke(true.B)
      dut.clock.step()

      dut.io.addr_insertSort.poke(8.U)
      dut.io.din_insertSort.poke(2.U)
      dut.io.we_insertSort.poke(true.B)
      dut.clock.step()

      dut.io.addr_insertSort.poke(9.U)
      dut.io.din_insertSort.poke(1.U)
      dut.io.we_insertSort.poke(true.B)
      dut.clock.step()

      dut.io.we_insertSort.poke(false.B)
      dut.io.valid.poke(true.B)

      for(i <- 0 until 500) {
        dut.clock.step()
      }

    }
  }
}
