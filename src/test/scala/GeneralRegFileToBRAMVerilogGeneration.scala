package GeneralRegFileToBRAM

import chisel3.stage.{ChiselStage,ChiselGeneratorAnnotation}

object GeneralRegFileToBRAMVerilogGeneration extends App {  
  (new ChiselStage).execute(
    Array("--target-dir", "generated_verilog"),
    Seq(ChiselGeneratorAnnotation(() => new AXIWrapperChiselGeneralRegFileToBRAM(C_S_AXI_ADDR_WIDTH = 12, C_S_AXI_DATA_WIDTH = 32)))
    //Seq(ChiselGeneratorAnnotation(() => new GeneralRegFileToBRAM (ADDR_WIDTH = 11, DATA_WIDTH = 32)))
  )
}