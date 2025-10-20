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
    Seq(ChiselGeneratorAnnotation(() => new TempSaveRestore(
      nU1 = 1, nU8 = 2, nU16 = 3, nU32 = 0, nU64 = 1,
      nS1 = 1, nS8 = 2, nS16 = 3, nS32 = 0, nS64 = 1,
      addrWidth = 32, dataWidth = 64, depth = 100,
      stackMaxDepth = 32, idWidth = 5, cpWidth = 8
    )))
  )
}

// object TestOnlyBlockMemoryVerilogGeneration extends App {  
//   (new ChiselStage).execute(
//     Array("--target-dir", "generated_verilog"),
//     Seq(ChiselGeneratorAnnotation(() => new TestOnlyBlockMemory(dataWidth = 64, depth = 200)))
//   )
// }