package OpenAVRSim

import chisel3._
import chiseltest._
import chiseltest.simulator.{VerilatorFlags, WriteVcdAnnotation, VerilatorBackendAnnotation}
import org.scalatest.flatspec.AnyFlatSpec

class OpenAVRSimTest extends AnyFlatSpec with ChiselScalatestTester {
  "OpenAVRSimTest" should "OpenAVRSim" in {
    test(new OpenAVRSim()).withAnnotations(
      Seq(WriteVcdAnnotation,
          VerilatorBackendAnnotation,
          VerilatorFlags(Seq("-I/home/kejun/git_dir/f24-706-kejun/esjc-801/chisel-template/src/main/resources/open-avr", "-Wall", "--trace")))
    ) { dut =>
      dut.clock.setTimeout(30000)

      for(i <- 0 until(28000)) {
        dut.clock.step()
      }

    }
  }
}