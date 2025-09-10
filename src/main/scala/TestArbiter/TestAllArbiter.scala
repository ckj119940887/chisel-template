package TestArbiter

import chisel3._
import chisel3.util._
import chisel3.experimental._

class TestAllArbiter extends Module{
    val io = IO(new Bundle{

    })

    val modWrapper   = Module(new AdderSigned64Wrapper(dataWidth = 64))
    val arbMod       = Module(new AdderSigned64ArbiterModule(numIPs = 1, dataWidth = 64))
    val testFunction = Module(new AdderSigned64FunctionModule(dataWidth = 64))

    // val modWrapper   = Module(new AdderUnsigned64Wrapper(dataWidth = 64))
    // val arbMod       = Module(new AdderUnsigned64ArbiterModule(numIPs = 1, dataWidth = 64))
    // val testFunction = Module(new AdderUnsigned64FunctionModule(dataWidth = 64))

    // val modWrapper   = Module(new AndUnsigned64Wrapper(dataWidth = 64))
    // val arbMod       = Module(new AndUnsigned64ArbiterModule(numIPs = 1, dataWidth = 64))
    // val testFunction = Module(new AndUnsigned64FunctionModule(dataWidth = 64))

    // val modWrapper   = Module(new DivisionSigned64Wrapper(dataWidth = 64))
    // val arbMod       = Module(new DivisionSigned64ArbiterModule(numIPs = 1, dataWidth = 64))
    // val testFunction = Module(new DivisionSigned64FunctionModule(dataWidth = 64))

    // val modWrapper   = Module(new DivisionUnsigned64Wrapper(dataWidth = 64))
    // val arbMod       = Module(new DivisionUnsigned64ArbiterModule(numIPs = 1, dataWidth = 64))
    // val testFunction = Module(new DivisionUnsigned64FunctionModule(dataWidth = 64))

    // val modWrapper   = Module(new EqSigned64Wrapper(dataWidth = 64))
    // val arbMod       = Module(new EqSigned64ArbiterModule(numIPs = 1, dataWidth = 64))
    // val testFunction = Module(new EqSigned64FunctionModule(dataWidth = 64))

    // val modWrapper   = Module(new EqUnsigned64Wrapper(dataWidth = 64))
    // val arbMod       = Module(new EqUnsigned64ArbiterModule(numIPs = 1, dataWidth = 64))
    // val testFunction = Module(new EqUnsigned64FunctionModule(dataWidth = 64))

    // val modWrapper   = Module(new GeSigned64Wrapper(dataWidth = 64))
    // val arbMod       = Module(new GeSigned64ArbiterModule(numIPs = 1, dataWidth = 64))
    // val testFunction = Module(new GeSigned64FunctionModule(dataWidth = 64))

    // val modWrapper   = Module(new GeUnsigned64Wrapper(dataWidth = 64))
    // val arbMod       = Module(new GeUnsigned64ArbiterModule(numIPs = 1, dataWidth = 64))
    // val testFunction = Module(new GeUnsigned64FunctionModule(dataWidth = 64))

    // val modWrapper   = Module(new GtSigned64Wrapper(dataWidth = 64))
    // val arbMod       = Module(new GtSigned64ArbiterModule(numIPs = 1, dataWidth = 64))
    // val testFunction = Module(new GtSigned64FunctionModule(dataWidth = 64))

    // val modWrapper   = Module(new GtUnsigned64Wrapper(dataWidth = 64))
    // val arbMod       = Module(new GtUnsigned64ArbiterModule(numIPs = 1, dataWidth = 64))
    // val testFunction = Module(new GtUnsigned64FunctionModule(dataWidth = 64))

    // val modWrapper   = Module(new LeSigned64Wrapper(dataWidth = 64))
    // val arbMod       = Module(new LeSigned64ArbiterModule(numIPs = 1, dataWidth = 64))
    // val testFunction = Module(new LeSigned64FunctionModule(dataWidth = 64))

    // val modWrapper   = Module(new LeUnsigned64Wrapper(dataWidth = 64))
    // val arbMod       = Module(new LeUnsigned64ArbiterModule(numIPs = 1, dataWidth = 64))
    // val testFunction = Module(new LeUnsigned64FunctionModule(dataWidth = 64))

    // val modWrapper   = Module(new LtSigned64Wrapper(dataWidth = 64))
    // val arbMod       = Module(new LtSigned64ArbiterModule(numIPs = 1, dataWidth = 64))
    // val testFunction = Module(new LtSigned64FunctionModule(dataWidth = 64))

    // val modWrapper   = Module(new LtUnsigned64Wrapper(dataWidth = 64))
    // val arbMod       = Module(new LtUnsigned64ArbiterModule(numIPs = 1, dataWidth = 64))
    // val testFunction = Module(new LtUnsigned64FunctionModule(dataWidth = 64))

    // val modWrapper   = Module(new MultiplierSigned64Wrapper(dataWidth = 64))
    // val arbMod       = Module(new MultiplierSigned64ArbiterModule(numIPs = 1, dataWidth = 64))
    // val testFunction = Module(new MultiplierSigned64FunctionModule(dataWidth = 64))

    // val modWrapper   = Module(new MultiplierUnsigned64Wrapper(dataWidth = 64))
    // val arbMod       = Module(new MultiplierUnsigned64ArbiterModule(numIPs = 1, dataWidth = 64))
    // val testFunction = Module(new MultiplierUnsigned64FunctionModule(dataWidth = 64))

    // val modWrapper   = Module(new NeSigned64Wrapper(dataWidth = 64))
    // val arbMod       = Module(new NeSigned64ArbiterModule(numIPs = 1, dataWidth = 64))
    // val testFunction = Module(new NeSigned64FunctionModule(dataWidth = 64))

    // val modWrapper   = Module(new NeUnsigned64Wrapper(dataWidth = 64))
    // val arbMod       = Module(new NeUnsigned64ArbiterModule(numIPs = 1, dataWidth = 64))
    // val testFunction = Module(new NeUnsigned64FunctionModule(dataWidth = 64))

    // val modWrapper   = Module(new OrSigned64Wrapper(dataWidth = 64))
    // val arbMod       = Module(new OrSigned64ArbiterModule(numIPs = 1, dataWidth = 64))
    // val testFunction = Module(new OrSigned64FunctionModule(dataWidth = 64))

    // val modWrapper   = Module(new OrUnsigned64Wrapper(dataWidth = 64))
    // val arbMod       = Module(new OrUnsigned64ArbiterModule(numIPs = 1, dataWidth = 64))
    // val testFunction = Module(new OrUnsigned64FunctionModule(dataWidth = 64))

    // val modWrapper   = Module(new RemainerSigned64Wrapper(dataWidth = 64))
    // val arbMod       = Module(new RemainerSigned64ArbiterModule(numIPs = 1, dataWidth = 64))
    // val testFunction = Module(new RemainerSigned64FunctionModule(dataWidth = 64))

    // val modWrapper   = Module(new RemainerUnsigned64Wrapper(dataWidth = 64))
    // val arbMod       = Module(new RemainerUnsigned64ArbiterModule(numIPs = 1, dataWidth = 64))
    // val testFunction = Module(new RemainerUnsigned64FunctionModule(dataWidth = 64))

    // val modWrapper   = Module(new ShlSigned64Wrapper(dataWidth = 64))
    // val arbMod       = Module(new ShlSigned64ArbiterModule(numIPs = 1, dataWidth = 64))
    // val testFunction = Module(new ShlSigned64FunctionModule(dataWidth = 64))

    // val modWrapper   = Module(new ShlUnsigned64Wrapper(dataWidth = 64))
    // val arbMod       = Module(new ShlUnsigned64ArbiterModule(numIPs = 1, dataWidth = 64))
    // val testFunction = Module(new ShlUnsigned64FunctionModule(dataWidth = 64))

    // val modWrapper   = Module(new ShrSigned64Wrapper(dataWidth = 64))
    // val arbMod       = Module(new ShrSigned64ArbiterModule(numIPs = 1, dataWidth = 64))
    // val testFunction = Module(new ShrSigned64FunctionModule(dataWidth = 64))

    // val modWrapper   = Module(new ShrUnsigned64Wrapper(dataWidth = 64))
    // val arbMod       = Module(new ShrUnsigned64ArbiterModule(numIPs = 1, dataWidth = 64))
    // val testFunction = Module(new ShrUnsigned64FunctionModule(dataWidth = 64))

    // val modWrapper   = Module(new SubtractorSigned64Wrapper(dataWidth = 64))
    // val arbMod       = Module(new SubtractorSigned64ArbiterModule(numIPs = 1, dataWidth = 64))
    // val testFunction = Module(new SubtractorSigned64FunctionModule(dataWidth = 64))

    // val modWrapper   = Module(new SubtractorUnsigned64Wrapper(dataWidth = 64))
    // val arbMod       = Module(new SubtractorUnsigned64ArbiterModule(numIPs = 1, dataWidth = 64))
    // val testFunction = Module(new SubtractorUnsigned64FunctionModule(dataWidth = 64))

    // val modWrapper   = Module(new UshrSigned64Wrapper(dataWidth = 64))
    // val arbMod       = Module(new UshrSigned64ArbiterModule(numIPs = 1, dataWidth = 64))
    // val testFunction = Module(new UshrSigned64FunctionModule(dataWidth = 64))

    // val modWrapper   = Module(new UshrUnsigned64Wrapper(dataWidth = 64))
    // val arbMod       = Module(new UshrUnsigned64ArbiterModule(numIPs = 1, dataWidth = 64))
    // val testFunction = Module(new UshrUnsigned64FunctionModule(dataWidth = 64))

    // val modWrapper   = Module(new XorSigned64Wrapper(dataWidth = 64))
    // val arbMod       = Module(new XorSigned64ArbiterModule(numIPs = 1, dataWidth = 64))
    // val testFunction = Module(new XorSigned64FunctionModule(dataWidth = 64))

    // val modWrapper   = Module(new XorUnsigned64Wrapper(dataWidth = 64))
    // val arbMod       = Module(new XorUnsigned64ArbiterModule(numIPs = 1, dataWidth = 64))
    // val testFunction = Module(new XorUnsigned64FunctionModule(dataWidth = 64))

    // val modWrapper   = Module(new IndexerWrapper(dataWidth = 8))
    // val arbMod       = Module(new IndexerArbiterModule(numIPs = 1, dataWidth = 8))
    // val testFunction = Module(new IndexerFunctionModule(dataWidth = 8))

    // val modWrapper   = Module(new BlockMemoryWrapper(dataWidth = 64, depth = 200))
    // val arbMod       = Module(new BlockMemoryArbiterModule(numIPs = 1, dataWidth = 64, depth = 200))
    // val testFunction = Module(new BlockMemoryFunctionModule(dataWidth = 64, depth = 200))

    arbMod.io.ip.req  <> modWrapper.io.req
    arbMod.io.ip.resp <> modWrapper.io.resp

    arbMod.io.ipReqs(0)  <> testFunction.io.arb_req
    arbMod.io.ipResps(0) <> testFunction.io.arb_resp
}

class TestNewShlSigned64 extends Module {
  val io = IO(new Bundle{

  })

  val mod = Module(new NewShlSigned64(64))
  mod.io.a := 0.S
  mod.io.b := 0.U
  mod.io.start := false.B

  val ShlSigned64CP    = RegInit(0.U(4.W))
  val r_res            = Reg(SInt(64.W))

  switch(ShlSigned64CP) {
    is(0.U) {
      mod.io.a := -1.S
      mod.io.b := 64.U
      mod.io.start := true.B

      when(mod.io.valid) {
          r_res         := mod.io.out
          mod.io.start  := false.B
          ShlSigned64CP := 1.U
      }
    }
    is(1.U) {
      mod.io.a := -1.S
      mod.io.b := 63.U
      mod.io.start := true.B

      when(mod.io.valid) {
          r_res         := mod.io.out
          mod.io.start  := false.B
          ShlSigned64CP := 2.U
      }
    }
    is(2.U) {
      printf("result:%d\n", r_res)
    }
  }
}

class TestNewShrSigned64 extends Module {
  val io = IO(new Bundle{

  })

  val mod = Module(new NewShrSigned64(64))
  mod.io.a := 0.S
  mod.io.b := 0.U
  mod.io.start := false.B

  val ShlSigned64CP    = RegInit(0.U(4.W))
  val r_res            = Reg(SInt(64.W))

  switch(ShlSigned64CP) {
    is(0.U) {
      mod.io.a := 128.S
      mod.io.b := 3.U
      mod.io.start := true.B

      when(mod.io.valid) {
          r_res         := mod.io.out
          mod.io.start  := false.B
          ShlSigned64CP := 1.U
      }
    }
    is(1.U) {
      mod.io.a := -1.S
      mod.io.b := 63.U
      mod.io.start := true.B

      when(mod.io.valid) {
          r_res         := mod.io.out
          mod.io.start  := false.B
          ShlSigned64CP := 2.U
      }
    }
    is(2.U) {
      printf("result:%d\n", r_res)
    }
  }
}

class TestNewUshrSigned64 extends Module {
  val io = IO(new Bundle{

  })

  val mod = Module(new NewUshrSigned64(64))
  mod.io.a := 0.S
  mod.io.b := 0.U
  mod.io.start := false.B

  val ShlSigned64CP    = RegInit(0.U(4.W))
  val r_res            = Reg(SInt(64.W))

  switch(ShlSigned64CP) {
    is(0.U) {
      mod.io.a := (-1).S
      mod.io.b := 63.U
      mod.io.start := true.B

      when(mod.io.valid) {
          r_res         := mod.io.out
          mod.io.start  := false.B
          ShlSigned64CP := 1.U
      }
    }
    is(1.U) {
      mod.io.a := -1.S
      mod.io.b := 64.U
      mod.io.start := true.B

      when(mod.io.valid) {
          r_res         := mod.io.out
          mod.io.start  := false.B
          ShlSigned64CP := 2.U
      }
    }
    is(2.U) {
      printf("result:%d\n", r_res)
    }
  }
}