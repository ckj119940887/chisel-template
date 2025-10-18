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

object TestTempSaveRestoreVerilogGeneration extends App {  
  (new ChiselStage).execute(
    Array("--target-dir", "generated_verilog"),
    Seq(ChiselGeneratorAnnotation(() => new TempSaveRestore(nU1 = 1, nU8 = 1, nU16 = 0, nU32 = 3, nU64 = 4, nS1 = 0, nS8 = 1, nS16 = 0, nS32 = 0, nS64 = 0)))
  )
}

// object TestOnlyBlockMemoryVerilogGeneration extends App {  
//   (new ChiselStage).execute(
//     Array("--target-dir", "generated_verilog"),
//     Seq(ChiselGeneratorAnnotation(() => new TestOnlyBlockMemory(dataWidth = 64, depth = 200)))
//   )
// }