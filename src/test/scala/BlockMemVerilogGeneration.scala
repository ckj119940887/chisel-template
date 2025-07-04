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

object AXI4LiteSlaveStateMachineWithoutBRAMVerilogGeneration extends App {  
  (new ChiselStage).execute(
    Array("--target-dir", "generated_verilog"),
    Seq(ChiselGeneratorAnnotation(() => new AXI4LiteSlaveStateMachineWithoutBRAM(C_S_AXI_DATA_WIDTH = 32, C_S_AXI_ADDR_WIDTH = 10)))
  )
}

object AXI4LiteMasterSlave_AXIDMA_VerilogGeneration extends App {  
  (new ChiselStage).execute(
    Array("--target-dir", "generated_verilog"),
    Seq(ChiselGeneratorAnnotation(() => new AXI4LiteMasterSlave_AXIDMA(C_S_AXI_DATA_WIDTH = 32, C_S_AXI_ADDR_WIDTH = 10, BASE_MEM_ADDR = 0xA0000000)))
  )
}

object AXI4FullMaster_VerilogGeneration extends App {  
  (new ChiselStage).execute(
    Array("--target-dir", "generated_verilog"),
    Seq(ChiselGeneratorAnnotation(() => new AXI4FullMaster(C_M_AXI_DATA_WIDTH = 64, 
                                                           C_M_AXI_ADDR_WIDTH = 32, 
                                                           C_M_TARGET_SLAVE_BASE_ADDR = BigInt("A0000000", 16),
                                                           MEMORY_DEPTH = 1024)))
  )
}

object AXI4LiteSlaveDDR4_VerilogGeneration extends App {  
  (new ChiselStage).execute(
    Array("--target-dir", "generated_verilog"),
    Seq(ChiselGeneratorAnnotation(() => new AXI4LiteSlaveDDR4(C_S_AXI_DATA_WIDTH = 64, 
                                                              C_S_AXI_ADDR_WIDTH = 32,
                                                              C_M_AXI_DATA_WIDTH = 64,
                                                              C_M_AXI_ADDR_WIDTH = 49)))
  )
}