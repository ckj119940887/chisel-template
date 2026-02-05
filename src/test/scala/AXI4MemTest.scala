package TestArbiter

import chisel3._
import chiseltest._
import chiseltest.simulator.WriteVcdAnnotation
import org.scalatest.flatspec.AnyFlatSpec

class AXI4SlaveTest extends AnyFlatSpec with ChiselScalatestTester {
  val params = AXI4Params(addrWidth = 32, dataWidth = 64)

  "AXI4MemSlave" should "correctly perform sequential handshakes" in {
    test(new AXI4MemSlave(params)).withAnnotations(Seq(WriteVcdAnnotation, TreadleBackendAnnotation)) { dut =>
      dut.clock.setTimeout(5000)
      dut.reset.poke(true.B); dut.clock.step(2); dut.reset.poke(false.B)

      // 1. 写循环：分别处理每个通道的握手
      for(i <- 0 until 5) {
        val currentAddr = (0x0 + i * 8).U
        val currentData = (100 + i).U

        // --- AW 通道握手 ---
        dut.io.AWADDR.poke(currentAddr)
        dut.io.AWLEN.poke(0.U)
        dut.io.AWBURST.poke(1.U)
        dut.io.AWVALID.poke(true.B)
        while(!dut.io.AWREADY.peekBoolean()) { dut.clock.step() }
        dut.clock.step() // 完成地址采样
        dut.io.AWVALID.poke(false.B)

        // --- W 通道握手 ---
        dut.io.WDATA.poke(currentData)
        dut.io.WSTRB.poke("b1111".U)
        dut.io.WLAST.poke(true.B)
        dut.io.WVALID.poke(true.B)
        while(!dut.io.WREADY.peekBoolean()) { dut.clock.step() }
        dut.clock.step() // 完成数据采样
        dut.io.WVALID.poke(false.B)

        // --- B 通道响应 ---
        dut.io.BREADY.poke(true.B)
        while(!dut.io.BVALID.peekBoolean()) { dut.clock.step() }
        dut.clock.step()
        dut.io.BREADY.poke(false.B)
      }

      // 2. 读循环验证
      for(i <- 0 until 5) {
        dut.io.ARADDR.poke((0x0 + i * 8).U)
        dut.io.ARVALID.poke(true.B)
        while(!dut.io.ARREADY.peekBoolean()) { dut.clock.step() }
        dut.clock.step()
        dut.io.ARVALID.poke(false.B)

        dut.io.RREADY.poke(true.B)
        while(!dut.io.RVALID.peekBoolean()) { dut.clock.step() }
        dut.io.RDATA.expect((100 + i).U)
        dut.clock.step()
      }
      println("All Sequential Tests Passed!")
    }
  }
}

class PureBlockMemoryTest extends AnyFlatSpec with ChiselScalatestTester {
  "PureBlockMemoryTest" should "correctly perform sequential handshakes" in {
    test(new TopPureBlockMemory()).withAnnotations(Seq(WriteVcdAnnotation, TreadleBackendAnnotation)) { dut =>
      dut.clock.setTimeout(5000)

      dut.reset.poke(true.B)
      dut.clock.step(2)
      dut.reset.poke(false.B)

      dut.io.start.poke(true.B)

      dut.clock.step(400)
    }
  }
}