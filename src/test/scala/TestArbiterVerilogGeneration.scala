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
    Seq(ChiselGeneratorAnnotation(() => new TestMemoryArbiter(dataWidth = 64, addrWidth = 16, depth = 200)))
  )
}

// object TestOnlyBlockMemoryVerilogGeneration extends App {  
//   (new ChiselStage).execute(
//     Array("--target-dir", "generated_verilog"),
//     Seq(ChiselGeneratorAnnotation(() => new TestOnlyBlockMemory(dataWidth = 64, depth = 200)))
//   )
// }