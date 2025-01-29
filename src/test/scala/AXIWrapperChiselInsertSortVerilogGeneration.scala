package AXIWrapperChisel

import chisel3.stage.{ChiselStage,ChiselGeneratorAnnotation}

object AXIWrapperChiselInsertSortVerilogGeneration extends App {
  (new ChiselStage).execute(
    Array("--target-dir", "generated_verilog"),
    Seq(ChiselGeneratorAnnotation(() => new AXIWrapperChiselInsertSort(C_S_AXI_ADDR_WIDTH = 32, C_S_AXI_DATA_WIDTH = 32)))
  )
}