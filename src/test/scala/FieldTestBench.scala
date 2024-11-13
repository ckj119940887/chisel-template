import chisel3._
import chiseltest._
import chiseltest.simulator.WriteVcdAnnotation
import org.scalatest.flatspec.AnyFlatSpec

class FieldAccessFoo extends Bundle {
  val i = UInt(32.W)
}

class FieldAccessBar extends Bundle {
  val i = UInt(32.W)
  val f = new FieldAccessFoo
}

class FieldTest extends Module {
  val io = IO(new Bundle{
    val a = Input(UInt(32.W))
    val b = Output(UInt(32.W))
  })

  val faf = Reg(new FieldAccessFoo)
  val fab = Reg(new FieldAccessBar)
  faf.i := 15.U
  fab.f.i := 15.U
  fab.i := RegNext(fab.i) + 1.U

  val x = Reg(UInt(32.W))
  val y = Reg(UInt(32.W))

  io.b := io.a + faf.i + fab.f.i + fab.i
}

class FieldTestBench extends AnyFlatSpec with ChiselScalatestTester {
  "field" should "correctly" in {
    test(new FieldTest).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.io.a.poke(15.U)
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
      println(s"i: ${dut.io.b.peek().litValue}")
      //println(s"step: ${dut.io.step.peek().litValue}")
    }
  }
}
