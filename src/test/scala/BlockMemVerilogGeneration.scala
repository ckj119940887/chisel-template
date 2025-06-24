package GeneralRegFileToBRAM

import chisel3.stage.{ChiselStage,ChiselGeneratorAnnotation}

object BlockMemVerilogGeneration extends App {  
  (new ChiselStage).execute(
    Array("--target-dir", "generated_verilog"),
    Seq(ChiselGeneratorAnnotation(() => new BRAMIPWrapper(bramDepth = 1024, bramWidth = 8, portWidth = 32)))
  )
}

object AXI4LiteSlaveVerilogGeneration extends App {  
  (new ChiselStage).execute(
    Array("--target-dir", "generated_verilog"),
    Seq(ChiselGeneratorAnnotation(() => new AXI4LiteSlave(C_S_AXI_DATA_WIDTH = 64, C_S_AXI_ADDR_WIDTH = 7)))
  )
}

object IndexerVerilogGeneration extends App {  
  (new ChiselStage).execute(
    Array("--target-dir", "generated_verilog"),
    Seq(ChiselGeneratorAnnotation(() => new Indexer(width = 16)))
  )
}

object AXI4LiteSlaveStateMachineVerilogGeneration extends App {  
  (new ChiselStage).execute(
    Array("--target-dir", "generated_verilog"),
    Seq(ChiselGeneratorAnnotation(() => new AXIWrapperChiselGeneratedAddTest(C_S_AXI_DATA_WIDTH = 32, C_S_AXI_ADDR_WIDTH = 10, bramDepth = 1024)))
  )
}

object TestWhenVerilogGeneration extends App {  
  (new ChiselStage).execute(
    Array("--target-dir", "generated_verilog"),
    Seq(ChiselGeneratorAnnotation(() => new ConditionCheckExample()))
  )
}