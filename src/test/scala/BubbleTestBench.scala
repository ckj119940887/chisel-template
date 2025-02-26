package GeneralRegFileToBRAM 

import chisel3._
import chiseltest._
import chiseltest.simulator.WriteVcdAnnotation
import org.scalatest.flatspec.AnyFlatSpec

class BubbleTestBench extends AnyFlatSpec with ChiselScalatestTester {
  "BubbleTestBench" should "work" in {
    test(new Bubble()).withAnnotations(Seq(WriteVcdAnnotation, VerilatorBackendAnnotation)) { dut =>
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

      dut.io.arrayWe.poke(true.B)
      dut.io.arrayWriteAddr.poke(28.U)
      dut.io.arrayWData.poke("h00040001".U)
      dut.io.arrayStrb.poke("b1111".U)
      dut.clock.step()

      dut.io.arrayWe.poke(true.B)
      dut.io.arrayWriteAddr.poke(32.U)
      dut.io.arrayWData.poke("h00030002".U)
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