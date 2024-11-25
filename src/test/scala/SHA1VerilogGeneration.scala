package SHA1
import chisel3.stage.ChiselStage

object SHA1VerilogGeneration extends App {
  println(
    ChiselStage.emitSystemVerilog(new digest)
  )
}