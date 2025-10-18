package EM

import chisel3.stage.{ChiselStage,ChiselGeneratorAnnotation}

object EMVerilogGeneration extends App {  
  (new ChiselStage).execute(
    Array("--target-dir", "generated_verilog"),
    Seq(ChiselGeneratorAnnotation(() => new AXI4LiteSlavePmodDA3(C_S_AXI_ADDR_WIDTH = 10, C_S_AXI_DATA_WIDTH = 32)))
  )
}

object TriggerOfCounterVerilogGeneration extends App {  
  (new ChiselStage).execute(
    Array("--target-dir", "generated_verilog"),
    Seq(ChiselGeneratorAnnotation(() => new TriggerOfCounter(counterWidth = 15)))
  )
}