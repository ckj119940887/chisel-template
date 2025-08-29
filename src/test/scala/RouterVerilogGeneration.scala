package Router

import chisel3.stage.{ChiselStage,ChiselGeneratorAnnotation}

object TopTestAddGeneration extends App {  
  (new ChiselStage).execute(
    Array("--target-dir", "generated_verilog"),
    Seq(ChiselGeneratorAnnotation(() => new TopTestAdd(3, 1024, 64, 64, 5, 2)))
  )
}

object TopFactorialGeneration extends App {  
  (new ChiselStage).execute(
    Array("--target-dir", "generated_verilog"),
    Seq(ChiselGeneratorAnnotation(() => new TopFactorial(2, 1024, 64, 64)))
  )
}

object TopOddEvenGeneration extends App {  
  (new ChiselStage).execute(
    Array("--target-dir", "generated_verilog"),
    Seq(ChiselGeneratorAnnotation(() => new TopOddEven(3, 1024, 64, 64)))
  )
}