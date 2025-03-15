package AnvilHardFloat

import chisel3._
import chiseltest._
import chiseltest.simulator.WriteVcdAnnotation
import org.scalatest.flatspec.AnyFlatSpec

class FPUTestAdd extends AnyFlatSpec with ChiselScalatestTester {
  "FPUTestAdd" should "work" in {
    test(new AnvilFPUAdd(expWidth = 8, sigWidth = 24)).withAnnotations(Seq(WriteVcdAnnotation, VerilatorBackendAnnotation)) { dut =>
      dut.clock.setTimeout(3000)

      dut.reset.poke(true.B)
      for (i <- 0 until (3)) {
        dut.clock.step()
      }
      dut.reset.poke(false.B)
      dut.clock.step()

      // testing float add operation, it will be set to true.B when you test substract operation
      dut.io.subOp.poke(false.B)
      // dut.io.a.poke(recFNFromFN(8, 24, 0x448F7CB2.U)) //1147.8967
      // dut.io.b.poke(recFNFromFN(8, 24, 0x448F598F.U)) //1146.7987
      dut.io.a.poke(0x448F7CB2.U) //1147.8967
      dut.io.b.poke(0x448F598F.U) //1146.7987
      dut.io.roundingMode.poke("b000".U)
      dut.io.detectTininess.poke(false.B)

      for(i <- 0 until 100) {
        dut.clock.step()
      }

    }
  }
}
