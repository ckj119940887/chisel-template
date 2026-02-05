package TestArbiter

import chisel3._
import chisel3.util._
import chisel3.experimental._

// ==========================================
// 1. 参数与接口定义 (AXI4 Parameters & Bundle)
// ==========================================
case class AXI4Params(
  addrWidth: Int = 32,
  dataWidth: Int = 32,
  idWidth: Int = 1,
  userWidth: Int = 1
)

class AXI4SlaveBundle(val p: AXI4Params) extends Bundle {
  // 写地址通道 (AW)
  val AWID     = Input(UInt(p.idWidth.W))
  val AWADDR   = Input(UInt(p.addrWidth.W))
  val AWLEN    = Input(UInt(8.W))
  val AWSIZE   = Input(UInt(3.W))
  val AWBURST  = Input(UInt(2.W))
  val AWLOCK   = Input(Bool())
  val AWCACHE  = Input(UInt(4.W))
  val AWPROT   = Input(UInt(3.W))
  val AWQOS    = Input(UInt(4.W))
  val AWUSER   = Input(UInt(p.userWidth.W))
  val AWVALID  = Input(Bool())
  val AWREADY  = Output(Bool())

  // 写数据通道 (W)
  val WDATA    = Input(UInt(p.dataWidth.W))
  val WSTRB    = Input(UInt((p.dataWidth / 8).W))
  val WLAST    = Input(Bool())
  val WUSER    = Input(UInt(p.userWidth.W))
  val WVALID   = Input(Bool())
  val WREADY   = Output(Bool())

  // 写响应通道 (B)
  val BID      = Output(UInt(p.idWidth.W))
  val BRESP    = Output(UInt(2.W))
  val BUSER    = Output(UInt(p.userWidth.W))
  val BVALID   = Output(Bool())
  val BREADY   = Input(Bool())

  // 读地址通道 (AR)
  val ARID     = Input(UInt(p.idWidth.W))
  val ARADDR   = Input(UInt(p.addrWidth.W))
  val ARLEN    = Input(UInt(8.W))
  val ARSIZE   = Input(UInt(3.W))
  val ARBURST  = Input(UInt(2.W))
  val ARLOCK   = Input(Bool())
  val ARCACHE  = Input(UInt(4.W))
  val ARPROT   = Input(UInt(3.W))
  val ARQOS    = Input(UInt(4.W))
  val ARUSER   = Input(UInt(p.userWidth.W))
  val ARVALID  = Input(Bool())
  val ARREADY  = Output(Bool())

  // 读数据通道 (R)
  val RID      = Output(UInt(p.idWidth.W))
  val RDATA    = Output(UInt(p.dataWidth.W))
  val RRESP    = Output(UInt(2.W))
  val RLAST    = Output(Bool())
  val RUSER    = Output(UInt(p.userWidth.W))
  val RVALID   = Output(Bool())
  val RREADY   = Input(Bool())
}

// ==========================================
// 2. AXI4 Memory Slave 核心模块实现
// ==========================================
class AXI4MemSlave(val p: AXI4Params) extends Module {
  // 使用 Flipped 对应 Master 的接口
  val io = IO(new AXI4SlaveBundle(p))

  // 内存定义：使用同步读内存并按字节切分以支持 WSTRB [1, 2]
  val memDepth = 1024
  val mem = SyncReadMem(memDepth, Vec(p.dataWidth / 8, UInt(8.W)))

  // --- 辅助函数：地址计算 (支持 FIXED, INCR, WRAP) ---
  def calculateNextAddr(current: UInt, burst: UInt, size: UInt, len: UInt): UInt = {
    val step = 1.U << size
    val burstTotalBytes = step * (len + 1.U)
    val wrapBoundary = (current / burstTotalBytes) * burstTotalBytes
    val wrapLimit = wrapBoundary + burstTotalBytes
    val nextIncr = current + step
    val nextWrap = Mux(nextIncr >= wrapLimit, wrapBoundary, nextIncr)
    
    // 修复后的 MuxLookup 语法 
    MuxLookup(burst, nextIncr, Seq(
      0.U -> current,  // FIXED [3]
      1.U -> nextIncr, // INCR [3]
      2.U -> nextWrap  // WRAP [4]
    ))
  }

  // --- 写通道状态机 ---
  val wIdle :: wData :: wResp :: Nil = Enum(3)
  val wState = RegInit(wIdle)
  val awAddrReg = Reg(UInt(p.addrWidth.W))
  val awLenReg  = Reg(UInt(8.W))
  val awSizeReg = Reg(UInt(3.W))
  val awBurstReg = Reg(UInt(2.W))
  val awIdReg    = Reg(UInt(p.idWidth.W))
  val wCount     = Reg(UInt(8.W))

  io.AWREADY := wState === wIdle
  io.WREADY  := wState === wData
  io.BVALID  := wState === wResp
  io.BID     := awIdReg
  io.BRESP   := 0.U 
  io.BUSER   := 0.U

  switch(wState) {
    is(wIdle) {
      when(io.AWVALID) {
        awAddrReg := io.AWADDR; awLenReg := io.AWLEN
        awSizeReg := io.AWSIZE; awBurstReg := io.AWBURST
        awIdReg   := io.AWID;   wCount := 0.U
        wState    := wData
      }
    }
    is(wData) {
      when(io.WVALID) {
        // 计算对齐的字地址
        val wordAddr = awAddrReg(log2Ceil(memDepth * (p.dataWidth/8)) - 1, log2Ceil(p.dataWidth/8))
        val dataVec = VecInit((0 until p.dataWidth / 8).map(i => io.WDATA(8 * i + 7, 8 * i)))
        mem.write(wordAddr, dataVec, io.WSTRB.asBools) // 应用写掩码 [5]
        
        awAddrReg := calculateNextAddr(awAddrReg, awBurstReg, awSizeReg, awLenReg)
        wCount    := wCount + 1.U
        when(io.WLAST || wCount === awLenReg) { wState := wResp }
      }
    }
    is(wResp) {
      when(io.BREADY) { wState := wIdle }
    }
  }

  // --- 读通道状态机 ---
  val rIdle :: rData :: Nil = Enum(2)
  val rState = RegInit(rIdle)
  val arAddrReg = Reg(UInt(p.addrWidth.W))
  val arLenReg  = Reg(UInt(8.W))
  val arSizeReg = Reg(UInt(3.W))
  val arBurstReg = Reg(UInt(2.W))
  val arIdReg    = Reg(UInt(p.idWidth.W))
  val rCount     = Reg(UInt(8.W))

  io.ARREADY := rState === rIdle
  io.RVALID  := rState === rData
  io.RID     := arIdReg
  io.RRESP   := 0.U
  io.RUSER   := 0.U

  // 同步读取处理：SyncReadMem 数据在一拍后返回 [1, 6]
  val readEnable = (rState === rIdle && io.ARVALID) || (rState === rData && io.RREADY)
  val readAddr = Mux(rState === rIdle, io.ARADDR, arAddrReg)
  val wordAddrRead = readAddr(log2Ceil(memDepth * (p.dataWidth/8)) - 1, log2Ceil(p.dataWidth/8))
  val memData = mem.read(wordAddrRead, readEnable)
  
  io.RDATA := memData.asUInt
  io.RLAST := rCount === arLenReg && rState === rData

  switch(rState) {
    is(rIdle) {
      when(io.ARVALID) {
        arAddrReg := calculateNextAddr(io.ARADDR, io.ARBURST, io.ARSIZE, io.ARLEN)
        arLenReg  := io.ARLEN; arSizeReg := io.ARSIZE
        arBurstReg := io.ARBURST; arIdReg := io.ARID
        rCount    := 0.U; rState := rData
      }
    }
    is(rData) {
      when(io.RREADY) {
        arAddrReg := calculateNextAddr(arAddrReg, arBurstReg, arSizeReg, arLenReg)
        rCount    := rCount + 1.U
        when(io.RLAST) { rState := rIdle }
      }
    }
  }
}