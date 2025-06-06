package GeneralRegFileToBRAM

import chisel3.stage.{ChiselStage,ChiselGeneratorAnnotation}

object BlockMemVerilogGeneration extends App {  
  (new ChiselStage).execute(
    Array("--target-dir", "generated_verilog"),
    Seq(ChiselGeneratorAnnotation(() => new BRAMIPWrapper(depth = 1024, width = 64)))
  )
}