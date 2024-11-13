import chisel3._
import chisel3.util._
import chiseltest._
import chiseltest.simulator.WriteVcdAnnotation
import org.scalatest.flatspec.AnyFlatSpec

class StatementTest extends Module {
  val io = IO(new Bundle{
    val valid = Input(Bool())
    val ready = Output(Bool())
  })

  val x = Reg(UInt(32.W))
  val y = Reg(UInt(32.W))
  val b = Reg(Bool())
  val state = RegInit(0.U(4.W))

  switch(state) {
    is(0.U) {
      x :=  1.U
      y :=  2.U
      state := 1.U
    }
    is(1.U) {
      x := x +  2.U
      state := 2.U
    }
    is(2.U) {
      y := y + x
      state := 3.U
    }
    is(3.U) {
      b := x >  2.U
      state := 4.U
    }
    is(4.U) {
      x := x +  3.U
      state := 5.U
    }
    is(5.U) {
      y := y + x
      state := 6.U
    }
    is(6.U) {
      b := x + y >  2.U
      state := 7.U
    }
    is(7.U) {
      state := Mux(b, 8.U, 11.U)
    }
    is(8.U) {
      x := x +  7.U
      state := 9.U
    }
    is(9.U) {
      y := y + x
      state := 10.U
    }
    is(10.U) {
      x := x + y +  8.U
      state := 7.U
    }
    is(11.U) {
      b := x > y
      state := 12.U
    }

  }
  io.ready := state === 12.U

  printf("x:%d\n", x)
  printf("y:%d\n", y)
}

class StatementTestBench extends AnyFlatSpec with ChiselScalatestTester {
  "statement" should "correctly" in {
    test(new StatementTest).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.io.valid.poke(1)
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
      //println(s"i: ${dut.io.b.peek().litValue}")
      //println(s"step: ${dut.io.step.peek().litValue}")
    }
  }
}
