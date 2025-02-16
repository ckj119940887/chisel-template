package GeneralRegFileToBRAM

import chisel3.stage.{ChiselStage,ChiselGeneratorAnnotation}

object GeneralRegFileToRegFileProVerilogGeneration extends App {  
  (new ChiselStage).execute(
    Array("--target-dir", "generated_verilog"),
    Seq(ChiselGeneratorAnnotation(() => new AXIWrapperChiselGeneralRegFileToRegFilePro(C_S_AXI_ADDR_WIDTH = 32, C_S_AXI_DATA_WIDTH = 32)))
    //Seq(ChiselGeneratorAnnotation(() => new GeneralRegFileToBRAM (ADDR_WIDTH = 11, DATA_WIDTH = 32)))
  )
}