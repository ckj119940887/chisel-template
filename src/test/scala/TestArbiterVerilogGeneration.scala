package TestArbiter

import chisel3.stage.{ChiselStage,ChiselGeneratorAnnotation}

object TestArbiterVerilogGeneration extends App {  
  (new ChiselStage).execute(
    Array("--target-dir", "generated_verilog"),
    Seq(ChiselGeneratorAnnotation(() => new TestAllArbiter()))
  )
}

object TestMemoryArbiterVerilogGeneration extends App {  
  (new ChiselStage).execute(
    Array("--target-dir", "generated_verilog"),
    Seq(ChiselGeneratorAnnotation(() => new TestMemoryArbiter(dataWidth = 64, depth = 200)))
  )
}

object TestBlockMemoryVerilogGeneration extends App {  
  (new ChiselStage).execute(
    Array("--target-dir", "generated_verilog"),
    Seq(ChiselGeneratorAnnotation(() => new TestBlockMemory(dataWidth = 64, depth = 200)))
  )
}