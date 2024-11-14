package NewArrayTest

import chisel3._
import chiseltest._
import chiseltest.simulator.WriteVcdAnnotation
import org.scalatest.flatspec.AnyFlatSpec
import scala.util.Random

class NewArrayTestBench extends AnyFlatSpec with ChiselScalatestTester {
  "NewArrayTestBench" should "returnIntArray" in {
    test(new returnIntArray).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>

      dut.io.we_returnIntArray.poke(true.B)
      dut.io.addr_returnIntArray.poke(0.U)
      dut.io.din_returnIntArray.poke(0.U)
      dut.clock.step()

      dut.io.we_returnIntArray.poke(true.B)
      dut.io.addr_returnIntArray.poke(1.U)
      dut.io.din_returnIntArray.poke(1.U)
      dut.clock.step()

      dut.io.we_returnIntArray.poke(true.B)
      dut.io.addr_returnIntArray.poke(2.U)
      dut.io.din_returnIntArray.poke(2.U)
      dut.clock.step()

      dut.io.we_returnIntArray.poke(true.B)
      dut.io.addr_returnIntArray.poke(3.U)
      dut.io.din_returnIntArray.poke(3.U)
      dut.clock.step()

      dut.io.we_returnIntArray.poke(true.B)
      dut.io.addr_returnIntArray.poke(4.U)
      dut.io.din_returnIntArray.poke(4.U)
      dut.clock.step()

      dut.io.we_returnIntArray.poke(true.B)
      dut.io.addr_returnIntArray.poke(5.U)
      dut.io.din_returnIntArray.poke(5.U)
      dut.clock.step()

      dut.io.we_returnIntArray.poke(true.B)
      dut.io.addr_returnIntArray.poke(6.U)
      dut.io.din_returnIntArray.poke(6.U)
      dut.clock.step()

      dut.io.we_returnIntArray.poke(true.B)
      dut.io.addr_returnIntArray.poke(7.U)
      dut.io.din_returnIntArray.poke(7.U)
      dut.clock.step()

      dut.io.we_returnIntArray.poke(true.B)
      dut.io.addr_returnIntArray.poke(8.U)
      dut.io.din_returnIntArray.poke(8.U)
      dut.clock.step()

      dut.io.we_returnIntArray.poke(true.B)
      dut.io.addr_returnIntArray.poke(9.U)
      dut.io.din_returnIntArray.poke(9.U)
      dut.clock.step()

      dut.io.we_returnIntArray.poke(false.B)
      dut.io.valid.poke(1.U)
      dut.io.a.poke(5.U)
      dut.io.b.poke(6.U)
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

      dut.io.addr_returnIntArray.poke(0.U)
      dut.clock.step()

      dut.io.addr_returnIntArray.poke(1.U)
      dut.clock.step()

      dut.io.addr_returnIntArray.poke(2.U)
      dut.clock.step()

      dut.io.addr_returnIntArray.poke(3.U)
      dut.clock.step()

      dut.io.addr_returnIntArray.poke(4.U)
      dut.clock.step()

      dut.io.addr_returnIntArray.poke(5.U)
      dut.clock.step()

      dut.io.addr_returnIntArray.poke(6.U)
      dut.clock.step()

      dut.io.addr_returnIntArray.poke(7.U)
      dut.clock.step()

      dut.io.addr_returnIntArray.poke(8.U)
      dut.clock.step()

      dut.io.addr_returnIntArray.poke(9.U)
      dut.clock.step()
    }
  }

  "NewArrayTestBench" should "returnSum" in {
    test(new returnSum).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.io.initial.poke(15.U)
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

    }
  }
}