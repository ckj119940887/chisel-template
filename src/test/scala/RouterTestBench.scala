package Router

import chisel3._
import chiseltest._
import chiseltest.simulator.WriteVcdAnnotation
import org.scalatest.flatspec.AnyFlatSpec

class StackTestBench extends AnyFlatSpec with ChiselScalatestTester {
  "StackTestBench" should "work" in {
    test(new Stack(UInt(32.W), 32, 1024)).withAnnotations(Seq(WriteVcdAnnotation, VerilatorBackendAnnotation)) { dut =>
      dut.clock.setTimeout(3000)

      dut.reset.poke(true.B)
      for (i <- 0 until (5)) {
        dut.clock.step()
      }
      dut.reset.poke(false.B)
      dut.clock.step()

      for(i <- 0 until 10) {
        dut.io.en.poke(true.B)
        dut.io.push.poke(true.B)
        dut.io.dataIn.poke((i + 1).U)
        dut.clock.step()
      }

      dut.io.en.poke(false.B)

      for(i <- 0 until 10) {
        dut.io.en.poke(true.B)
        dut.io.pop.poke(true.B)
        dut.clock.step()

        dut.io.en.poke(false.B)
        dut.clock.step(2)
      }


      for(i <- 0 until 100) {
        dut.clock.step()
      }

    }
  }
}

class RouterTestBench extends AnyFlatSpec with ChiselScalatestTester {
  "RouterTestBench" should "work" in {
    test(new Router(5, 3, 32, 8)).withAnnotations(Seq(WriteVcdAnnotation, VerilatorBackendAnnotation)) { dut =>
      dut.clock.setTimeout(3000)

      dut.reset.poke(true.B)
      for (i <- 0 until (5)) {
        dut.clock.step()
      }
      dut.reset.poke(false.B)
      dut.clock.step()

      for(i <- 0 until 5) {
        dut.io.in(i).valid.poke(true.B)
        dut.io.in(i).bits.srcID.poke(i.U)
        dut.io.in(i).bits.dstID.poke(((i+1)%5).U)
        dut.io.in(i).bits.data.poke(true.B)
        dut.io.in(i).bits.srcCP.poke(0.U)
        dut.io.in(i).bits.dstCP.poke(0.U)
        dut.clock.step(3)

        dut.io.in(i).bits.data.poke(false.B)
        dut.clock.step()
      }

      for(i <- 0 until 100) {
        dut.clock.step()
      }

    }
  }
}

class MemoryArbiterTestBench extends AnyFlatSpec with ChiselScalatestTester {
  "ArbiterTestBench" should "work" in {
    test(new MemoryArbiterModule(4, 32, 32)).withAnnotations(Seq(WriteVcdAnnotation, VerilatorBackendAnnotation)) { dut =>
      dut.clock.setTimeout(3000)

      dut.reset.poke(true.B)
      for (i <- 0 until (5)) {
        dut.clock.step()
      }
      dut.reset.poke(false.B)
      dut.clock.step()

      dut.io.ipReqs.foreach { ip =>
        ip.valid.poke(false.B)
        ip.bits.addr.poke(0.U)
        ip.bits.data.poke(0.U)
        ip.bits.write.poke(false.B)
      }

      dut.io.memory.resp.valid.poke(false.B)
      dut.io.memory.resp.bits.data.poke(0.U)
      dut.clock.step()

      // IP1 也发起写请求
      dut.io.ipReqs(3).valid.poke(true.B)
      dut.io.ipReqs(3).bits.addr.poke(0x2000.U)
      dut.io.ipReqs(3).bits.data.poke(0xBBBB.U)
      dut.io.ipReqs(3).bits.write.poke(true.B)
      dut.clock.step()

      // IP 请求完成（断言 memory.req.fire）
      while (!dut.io.memory.req.valid.peek().litToBoolean) {
        dut.clock.step()
      }

      // memory resp 模拟返回响应（第一个响应 IP0）
      dut.io.memory.resp.valid.poke(true.B)
      dut.io.memory.resp.bits.data.poke(0x0EADBEEF.U)

      for(i <- 0 until 100) {
        dut.clock.step()
      }

    }
  }
}

class SimpleMemoryTestBench extends AnyFlatSpec with ChiselScalatestTester {
  "SimpleMemoryTestBench" should "work" in {
    test(new SimpleMemory(1024, 32)).withAnnotations(Seq(WriteVcdAnnotation, VerilatorBackendAnnotation)) { dut =>
      dut.clock.setTimeout(3000)

      dut.reset.poke(true.B)
      for (i <- 0 until (5)) {
        dut.clock.step()
      }
      dut.reset.poke(false.B)
      dut.clock.step()

      dut.io.en.poke(true.B)
      for(i <- 0 until 10) {
        dut.io.addr.poke(i.U)
        dut.io.din.poke((i+1).U)
        dut.io.we.poke(true.B)
        dut.clock.step()
      }

      dut.io.en.poke(false.B)
      dut.io.we.poke(false.B)
      dut.clock.step()

      dut.io.addr.poke(0.U)
      dut.clock.step()

      for(i <- 0 until 10) {
        dut.io.addr.poke(i.U)
        dut.io.en.poke(true.B)
        dut.io.we.poke(false.B)
        dut.clock.step()
      }

      for(i <- 0 until 100) {
        dut.clock.step()
      }

    }
  }
}

class SimpleMemoryWrapperTestBench extends AnyFlatSpec with ChiselScalatestTester {
  "SimpleMemoryWrapperTestBench" should "work" in {
    test(new SimpleMemoryWrapper(1024, 32)).withAnnotations(Seq(WriteVcdAnnotation, VerilatorBackendAnnotation)) { dut =>
      dut.clock.setTimeout(3000)

      dut.reset.poke(true.B)
      for (i <- 0 until (5)) {
        dut.clock.step()
      }
      dut.reset.poke(false.B)
      dut.clock.step()

      for(i <- 0 until 10) {
        dut.io.req.bits.addr.poke(i.U)
        dut.io.req.bits.data.poke((i+1).U)
        dut.io.req.bits.write.poke(true.B)
        dut.io.req.valid.poke(true.B)
        dut.clock.step()

        dut.io.req.bits.write.poke(false.B)
        dut.io.req.valid.poke(false.B)
        dut.clock.step(2)
      }

      dut.io.req.bits.addr.poke(0.U)
      dut.clock.step()

      for(i <- 0 until 10) {
        dut.io.req.bits.addr.poke(i.U)
        dut.io.req.bits.write.poke(false.B)
        dut.io.req.valid.poke(true.B)
        dut.clock.step()

        dut.io.req.valid.poke(false.B)
        dut.clock.step(2)
      }

      for(i <- 0 until 100) {
        dut.clock.step()
      }

    }
  }
}

class TopTestAddTestBench extends AnyFlatSpec with ChiselScalatestTester {
  "TopTestAddTestBench" should "work" in {
    test(new TopTestAdd(3, 1024, 32, 32)).withAnnotations(Seq(WriteVcdAnnotation, VerilatorBackendAnnotation)) { dut =>
      dut.clock.setTimeout(3000)

      dut.reset.poke(true.B)
      for (i <- 0 until (5)) {
        dut.clock.step()
      }
      dut.reset.poke(false.B)
      dut.clock.step()

      dut.io.start.poke(true.B)
      dut.clock.step()
      dut.io.start.poke(false.B)

      for(i <- 0 until 300) {
        dut.clock.step()
      }

    }
  }
}

class TopOddEvenTestBench extends AnyFlatSpec with ChiselScalatestTester {
  "TopOddEvenTestBench" should "work" in {
    test(new TopOddEven(3, 1024, 32, 32)).withAnnotations(Seq(WriteVcdAnnotation, VerilatorBackendAnnotation)) { dut =>
      dut.clock.setTimeout(3000)

      dut.reset.poke(true.B)
      for (i <- 0 until (5)) {
        dut.clock.step()
      }
      dut.reset.poke(false.B)
      dut.clock.step()

      dut.io.start.poke(true.B)
      dut.clock.step()
      dut.io.start.poke(false.B)

      for(i <- 0 until 1000) {
        dut.clock.step()
      }

    }
  }
}

class TopFactorialTestBench extends AnyFlatSpec with ChiselScalatestTester {
  "TopFactorialTestBench" should "work" in {
    test(new TopFactorial(2, 1024, 32, 32)).withAnnotations(Seq(WriteVcdAnnotation, VerilatorBackendAnnotation)) { dut =>
      dut.clock.setTimeout(3000)

      dut.reset.poke(true.B)
      for (i <- 0 until (5)) {
        dut.clock.step()
      }
      dut.reset.poke(false.B)
      dut.clock.step()

      dut.io.start.poke(true.B)
      dut.clock.step()
      dut.io.start.poke(false.B)

      for(i <- 0 until 1000) {
        dut.clock.step()
      }

    }
  }
}