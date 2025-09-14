package ROS

import chisel3.stage.{ChiselStage,ChiselGeneratorAnnotation}

object ROSVerilogGeneration extends App {  
  (new ChiselStage).execute(
    Array("--target-dir", "generated_verilog"),
    Seq(ChiselGeneratorAnnotation(() => new AXI4LiteSlaveTemp(C_S_AXI_ADDR_WIDTH = 10, C_S_AXI_DATA_WIDTH = 32)))
  )
}