package InfixExpressionTest

import chisel3._
import chiseltest._
import chiseltest.simulator.WriteVcdAnnotation
import org.scalatest.flatspec.AnyFlatSpec
import scala.util.Random

class InfixExpressionTestBench extends AnyFlatSpec with ChiselScalatestTester {
  "InfixExpressionTestBench" should "and" in {
    test(new and).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      val rand = new Random()

      dut.io.valid.poke(true.B)

      for(i <- 0 until 100) {
        val inX = rand.nextInt(30000).S
        val inY = rand.nextInt(30000).S
        dut.io.x.poke(inX)
        dut.io.y.poke(inY)
        dut.clock.step()
        dut.io.out_and.expect((inX.litValue & inY.litValue).S)
      }

    }
  }

  "InfixExpressionTestBench" should "or" in {
    test(new or).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      val rand = new Random()

      dut.io.valid.poke(true.B)

      for(i <- 0 until 100) {
        val inX = rand.nextInt(30000).S
        val inY = rand.nextInt(30000).S
        dut.io.x.poke(inX)
        dut.io.y.poke(inY)
        dut.clock.step()
        dut.io.out_or.expect((inX.litValue | inY.litValue).S)
      }
    }
  }

  "InfixExpressionTestBench" should "xor" in {
    test(new xor).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      val rand = new Random()

      dut.io.valid.poke(true.B)

      for(i <- 0 until 100) {
        val inX = rand.nextInt(30000).S
        val inY = rand.nextInt(30000).S
        dut.io.x.poke(inX)
        dut.io.y.poke(inY)
        dut.clock.step()
        dut.io.out_xor.expect((inX.litValue ^ inY.litValue).S)
      }

    }
  }

  "InfixExpressionTestBench" should "add" in {
    test(new add).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      val rand = new Random()

      dut.io.valid.poke(true.B)
      for(i <- 0 until 100) {
        val inX = rand.nextInt(30000).S
        val inY = rand.nextInt(30000).S
        dut.io.x.poke(inX)
        dut.io.y.poke(inY)
        dut.clock.step()
        dut.io.out_add.expect((inX.litValue + inY.litValue).S)
      }
    }
  }

  "InfixExpressionTestBench" should "mul" in {
    test(new mul).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      val rand = new Random()

      dut.io.valid.poke(true.B)
      for(i <- 0 until 100) {
        val inX = rand.nextInt(128).S
        val inY = rand.nextInt(128).S
        dut.io.x.poke(inX)
        dut.io.y.poke(inY)
        dut.clock.step()
        dut.io.out_mul.expect((inX.litValue * inY.litValue).S)
      }
    }
  }

  def randomInRange(min: Int, max: Int): Int = {
    require(min <= max, "min should be less than or equal to max")
    min + Random.nextInt((max - min) + 1)
  }

  "InfixExpressionTestBench" should "mod" in {
    test(new mod).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      val rand = new Random()

      dut.io.valid.poke(true.B)
      for(i <- 0 until 100) {
        val inX = randomInRange(1, 128).S
        val inY = randomInRange(1, 128).S
        dut.io.x.poke(inX)
        dut.io.y.poke(inY)
        dut.clock.step()
        dut.io.out_mod.expect((inX.litValue % inY.litValue).S)
      }
    }
  }

  "InfixExpressionTestBench" should "div" in {
    test(new div).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      val rand = new Random()

      dut.io.valid.poke(true.B)
      for(i <- 0 until 100) {
        val inX = rand.nextInt(30000).S
        val inY = rand.nextInt(30000).S
        dut.io.x.poke(inX)
        dut.io.y.poke(inY)
        dut.clock.step()
        dut.io.out_div.expect((inX.litValue / inY.litValue).S)
      }
    }
  }

  "InfixExpressionTestBench" should "equal" in {
    test(new equal).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      val rand = new Random()

      dut.io.valid.poke(true.B)
      for(i <- 0 until 100) {
        val inX = rand.nextInt(30000).S
        val inY = rand.nextInt(30000).S
        dut.io.x.poke(inX)
        dut.io.y.poke(inY)
        dut.clock.step()
        val expectValue = if(inX.litValue == inY.litValue) true.B else false.B
        dut.io.out_equal.expect(expectValue)
      }
    }
  }

  "InfixExpressionTestBench" should "greater" in {
    test(new greater).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      val rand = new Random()

      dut.io.valid.poke(true.B)
      for(i <- 0 until 100) {
        val inX = rand.nextInt(30000).S
        val inY = rand.nextInt(30000).S
        dut.io.x.poke(inX)
        dut.io.y.poke(inY)
        dut.clock.step()
        val expectValue = if(inX.litValue > inY.litValue) true.B else false.B
        dut.io.out_greater.expect(expectValue)
      }
    }
  }

  "InfixExpressionTestBench" should "greaterEqual" in {
    test(new greaterEqual).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      val rand = new Random()

      dut.io.valid.poke(true.B)
      for(i <- 0 until 100) {
        val inX = rand.nextInt(30000).S
        val inY = rand.nextInt(30000).S
        dut.io.x.poke(inX)
        dut.io.y.poke(inY)
        dut.clock.step()
        val expectValue = if(inX.litValue >= inY.litValue) true.B else false.B
        dut.io.out_greaterEqual.expect(expectValue)
      }
    }
  }

  "InfixExpressionTestBench" should "less" in {
    test(new less).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      val rand = new Random()

      dut.io.valid.poke(true.B)
      for(i <- 0 until 100) {
        val inX = rand.nextInt(30000).S
        val inY = rand.nextInt(30000).S
        dut.io.x.poke(inX)
        dut.io.y.poke(inY)
        dut.clock.step()
        val expectValue = if(inX.litValue < inY.litValue) true.B else false.B
        dut.io.out_less.expect(expectValue)
      }
    }
  }

  "InfixExpressionTestBench" should "lessEqual" in {
    test(new lessEqual).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      val rand = new Random()

      dut.io.valid.poke(true.B)
      for(i <- 0 until 100) {
        val inX = rand.nextInt(30000).S
        val inY = rand.nextInt(30000).S
        dut.io.x.poke(inX)
        dut.io.y.poke(inY)
        dut.clock.step()
        val expectValue = if(inX.litValue <= inY.litValue) true.B else false.B
        dut.io.out_lessEqual.expect(expectValue)
      }
    }
  }

  "InfixExpressionTestBench" should "logicalAnd" in {
    test(new logicalAnd).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.io.valid.poke(true.B)

      dut.io.x.poke(true.B)
      dut.io.y.poke(true.B)
      dut.clock.step()
      dut.io.out_logicalAnd.expect(true.B)

      dut.io.x.poke(false.B)
      dut.io.y.poke(false.B)
      dut.clock.step()
      dut.io.out_logicalAnd.expect(false.B)

      dut.io.x.poke(false.B)
      dut.io.y.poke(true.B)
      dut.clock.step()
      dut.io.out_logicalAnd.expect(false.B)

      dut.io.x.poke(true.B)
      dut.io.y.poke(false.B)
      dut.clock.step()
      dut.io.out_logicalAnd.expect(false.B)

    }
  }

  "InfixExpressionTestBench" should "logicalOr" in {
    test(new logicalOr).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.io.valid.poke(true.B)

      dut.io.x.poke(true.B)
      dut.io.y.poke(true.B)
      dut.clock.step()
      dut.io.out_logicalOr.expect(true.B)

      dut.io.x.poke(false.B)
      dut.io.y.poke(false.B)
      dut.clock.step()
      dut.io.out_logicalOr.expect(false.B)

      dut.io.x.poke(false.B)
      dut.io.y.poke(true.B)
      dut.clock.step()
      dut.io.out_logicalOr.expect(true.B)

      dut.io.x.poke(true.B)
      dut.io.y.poke(false.B)
      dut.clock.step()
      dut.io.out_logicalOr.expect(true.B)

    }
  }

  /*
  "InfixExpressionTestBench" should "leftshift" in {
    test(new leftshift).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      val rand = new Random()

      dut.io.valid.poke(1.U)
      for(i <- 0 until 100) {
        val inX = rand.nextInt(128).U
        val inY = rand.nextInt(10).U
        println(s"x = ${inX}, y = ${inY}")
        dut.io.x.poke(inX)
        dut.io.y.poke(inY)
        dut.clock.step()
        dut.io.out_leftshift.expect(((inX.litValue.toInt & 0xFFF) << (inY.litValue.toInt & 0x1F)).U)
      }
    }
  }
  */
}