package TDC

import chisel3._
import chiseltest._
import chiseltest.simulator.WriteVcdAnnotation
import org.scalatest.flatspec.AnyFlatSpec
import scala.util.Random

class OnesCounter36to6TestBench extends AnyFlatSpec with ChiselScalatestTester {
    "TDC" should "OnesCounter36to6TestBench" in { 
        test(new Wrapper_ones_counter_36to6()).withAnnotations(Seq(WriteVcdAnnotation, VerilatorBackendAnnotation)) { dut =>
            dut.clock.setTimeout(8000)

            dut.io.i_Reset_N.poke(false.B)
            for (i <- 0 until (5)) {
                dut.clock.step()
            }
            dut.io.i_Reset_N.poke(true.B)

            for (i <- 0 until 1000) {
                val maxValue = (BigInt(1) << 36) - 1 // Maximum value for N bits
                val realNum = BigInt(36, Random) & maxValue  // Generate and mask to N bits

                dut.io.i_Sequence.poke(realNum.U)
                val count1s = realNum.bitCount

                dut.clock.step()
                assert(dut.io.o_Count.peek().litValue == count1s, s"${dut.io.o_Count.peek().litValue} != ${count1s}") 
                //println(s"${dut.io.o_Count.peek().litValue} -- ${count1s}")               
            }
        }
    }
}

class AdderTreeTestBench extends AnyFlatSpec with ChiselScalatestTester {
    "TDC" should "AdderTreeTestBench" in { 
        test(new Wrapper_adder_tree(NUM_OF_INS = 22, WIDTH_PER_IN = 6, WIDTH_FINAL_OUT = 16)).withAnnotations(Seq(WriteVcdAnnotation, VerilatorBackendAnnotation)) { dut =>
            dut.clock.setTimeout(8000)

            dut.io.i_Reset_N.poke(false.B)
            for (i <- 0 until (5)) {
                dut.clock.step()
            }
            dut.io.i_Reset_N.poke(true.B)

            for (i <- 0 until 1000) {
                val maxValue = (BigInt(1) << 128) - 1 // Maximum value for N bits
                val realNum = BigInt(128, Random) & maxValue  // Generate and mask to N bits

                dut.io.i_In_All.poke(realNum.U)
                dut.io.i_Data_Valid_In(true.B)

                dut.clock.step()
            }
        }
    }
}