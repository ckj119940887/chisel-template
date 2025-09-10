package TestArbiter

import chisel3.stage.{ChiselStage,ChiselGeneratorAnnotation}

object TestArbiterVerilogGeneration extends App {  
  (new ChiselStage).execute(
    Array("--target-dir", "generated_verilog"),
    Seq(ChiselGeneratorAnnotation(() => new TestAllArbiter()))
  )
}