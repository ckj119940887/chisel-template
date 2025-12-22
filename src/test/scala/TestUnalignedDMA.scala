package UnalignedDMA

import chisel3._
import chiseltest._
import chiseltest.simulator.WriteVcdAnnotation
import org.scalatest.flatspec.AnyFlatSpec
import chisel3.stage.{ChiselStage,ChiselGeneratorAnnotation}

object TestUnalignedDMA extends App {  
  (new ChiselStage).execute(
    Array("--target-dir", "generated_verilog"),
    Seq(ChiselGeneratorAnnotation(() => new UnalignedBlockMemory(C_M_AXI_DATA_WIDTH = 64, C_M_AXI_ADDR_WIDTH = 32, MEMORY_DEPTH = 1024, C_M_TARGET_SLAVE_BASE_ADDR = 0x0)))
  )
}