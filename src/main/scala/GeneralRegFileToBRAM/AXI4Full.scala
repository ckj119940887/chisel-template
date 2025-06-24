package GeneralRegFileToBRAM 

import chisel3._
import chisel3.util._
import chisel3.experimental._
import chisel3.util.experimental.loadMemoryFromFile

case class AXI4FullBundleParameters(
  C_S_AXI_ID_WIDTH     : Int,
  C_S_AXI_DATA_WIDTH   : Int,
  C_S_AXI_ADDR_WIDTH   : Int,
  C_S_AXI_AWUSER_WIDTH : Int,
  C_S_AXI_ARUSER_WIDTH : Int,
  C_S_AXI_WUSER_WIDTH  : Int,
  C_S_AXI_RUSER_WIDTH  : Int,
  C_S_AXI_BUSER_WIDTH  : Int
)

class AXI4FullMasterBundle(val params: AXI4FullBundleParameters) extends Bundle {
  val aw = Decoupled(new Bundle {
    val addr   = UInt(params.C_S_AXI_ADDR_WIDTH.W)
    val id     = UInt(params.C_S_AXI_DATA_WIDTH.W)
    val len    = UInt(8.W)
    val size   = UInt(3.W)
    val burst  = UInt(2.W)
    val lock   = UInt(1.W)
    val cache  = UInt(4.W)
    val prot   = UInt(3.W)
    val qos    = UInt(4.W)
    val region = UInt(4.W)
    val user   = UInt(params.C_S_AXI_AWUSER_WIDTH.W)
  })

  val w = Decoupled(new Bundle {
    val data = UInt(params.C_S_AXI_DATA_WIDTH.W)
    val strb = UInt((params.C_S_AXI_DATA_WIDTH / 8).W)
    val last = Bool()
    val user = UInt(params.C_S_AXI_WUSER_WIDTH.W)
  })

  val b = Flipped(Decoupled(new Bundle {
    val id   = UInt(params.C_S_AXI_ID_WIDTH.W)
    val resp = UInt(2.W)
    val user = UInt(params.C_S_AXI_BUSER_WIDTH.W)
  }))

  val ar = Decoupled(new Bundle {
    val id     = UInt(params.C_S_AXI_ID_WIDTH.W)
    val addr   = UInt(params.C_S_AXI_ADDR_WIDTH.W)
    val len    = UInt(8.W)
    val size   = UInt(3.W)
    val burst  = UInt(2.W)
    val lock   = UInt(1.W)
    val cache  = UInt(4.W)
    val prot   = UInt(3.W)
    val qos    = UInt(4.W)
    val region = UInt(4.W)
    val user   = UInt(params.C_S_AXI_ARUSER_WIDTH.W)
  })

  val r = Flipped(Decoupled(new Bundle {
    val data = UInt(params.C_S_AXI_DATA_WIDTH.W)
    val id   = UInt(params.C_S_AXI_ID_WIDTH.W)
    val resp = UInt(2.W)
    val last = Bool()
    val user = UInt(params.C_S_AXI_RUSER_WIDTH.W)
  }))
}

class AXI4FullSlaveBundle(val params: AXI4FullBundleParameters) extends Bundle {
  val aw = Flipped(Decoupled(new Bundle {
    val addr   = UInt(params.C_S_AXI_ADDR_WIDTH.W)
    val id     = UInt(params.C_S_AXI_DATA_WIDTH.W)
    val len    = UInt(8.W)
    val size   = UInt(3.W)
    val burst  = UInt(2.W)
    val lock   = UInt(1.W)
    val cache  = UInt(4.W)
    val prot   = UInt(3.W)
    val qos    = UInt(4.W)
    val region = UInt(4.W)
    val user   = UInt(params.C_S_AXI_AWUSER_WIDTH.W)
  }))

  val w = Flipped(Decoupled(new Bundle {
    val data = UInt(params.C_S_AXI_DATA_WIDTH.W)
    val strb = UInt((params.C_S_AXI_DATA_WIDTH / 8).W)
    val last = Bool()
    val user = UInt(params.C_S_AXI_WUSER_WIDTH.W)
  }))

  val b = Decoupled(new Bundle {
    val id   = UInt(params.C_S_AXI_ID_WIDTH.W)
    val resp = UInt(2.W)
    val user = UInt(params.C_S_AXI_BUSER_WIDTH.W)
  })

  val ar = Flipped(Decoupled(new Bundle {
    val id     = UInt(params.C_S_AXI_ID_WIDTH.W)
    val addr   = UInt(params.C_S_AXI_ADDR_WIDTH.W)
    val len    = UInt(8.W)
    val size   = UInt(3.W)
    val burst  = UInt(2.W)
    val lock   = UInt(1.W)
    val cache  = UInt(4.W)
    val prot   = UInt(3.W)
    val qos    = UInt(4.W)
    val region = UInt(4.W)
    val user   = UInt(params.C_S_AXI_ARUSER_WIDTH.W)
  }))

  val r = Decoupled(new Bundle {
    val data = UInt(params.C_S_AXI_DATA_WIDTH.W)
    val id   = UInt(params.C_S_AXI_ID_WIDTH.W)
    val resp = UInt(2.W)
    val last = Bool()
    val user = UInt(params.C_S_AXI_RUSER_WIDTH.W)
  })
}

case class AXI4LiteBundleParameters(
  C_S_AXI_DATA_WIDTH   : Int,
  C_S_AXI_ADDR_WIDTH   : Int
)

class AXI4LiteSlaveBundle(val params: AXI4LiteBundleParameters) extends Bundle {
  val aw = Flipped(Decoupled(new Bundle {
    val addr = UInt(params.C_S_AXI_ADDR_WIDTH.W)
  }))

  val w = Flipped(Decoupled(new Bundle {
    val data = UInt(params.C_S_AXI_DATA_WIDTH.W)
    val strb = UInt((params.C_S_AXI_DATA_WIDTH / 8).W)
  }))

  val b = Decoupled(new Bundle {
    val resp = UInt(2.W)
  })

  val ar = Flipped(Decoupled(new Bundle {
    val addr = UInt(params.C_S_AXI_ADDR_WIDTH.W)
  }))

  val r = Decoupled(new Bundle {
    val data = UInt(params.C_S_AXI_DATA_WIDTH.W)
    val resp = UInt(2.W)
  })
}

class AXI4LiteSlave(val C_S_AXI_DATA_WIDTH: Int, val C_S_AXI_ADDR_WIDTH: Int) extends Module {
  val io = IO(new Bundle{
    // write address channel
    val S_AXI_AWADDR  = Input(UInt(C_S_AXI_ADDR_WIDTH.W))
    val S_AXI_AWPROT  = Input(UInt(3.W))
    val S_AXI_AWVALID = Input(Bool())
    val S_AXI_AWREADY = Output(Bool())
    
    // write data channel
    val S_AXI_WDATA  = Input(UInt(C_S_AXI_DATA_WIDTH.W))
    val S_AXI_WSTRB  = Input(UInt((C_S_AXI_DATA_WIDTH/8).W))
    val S_AXI_WVALID = Input(Bool())
    val S_AXI_WREADY = Output(Bool())
    
    // write response channel
    val S_AXI_BRESP  = Output(UInt(2.W))
    val S_AXI_BVALID = Output(Bool())
    val S_AXI_BREADY = Input(Bool())
    
    // read address channel
    val S_AXI_ARADDR  = Input(UInt(C_S_AXI_ADDR_WIDTH.W))
    val S_AXI_ARPROT  = Input(UInt(3.W))
    val S_AXI_ARVALID = Input(Bool())
    val S_AXI_ARREADY = Output(Bool())
    
    // read data channel
    val S_AXI_RDATA  = Output(UInt(C_S_AXI_DATA_WIDTH.W))
    val S_AXI_RRESP  = Output(UInt(2.W))
    val S_AXI_RVALID = Output(Bool())
    val S_AXI_RREADY = Input(Bool())
  })

  val w_system_reset = !reset.asBool
  withReset(w_system_reset) {
    // registers for diff channels
    val r_s_axi_awready = Reg(Bool()) 
    val r_s_axi_wready  = Reg(Bool()) 
    val r_s_axi_bvalid  = Reg(Bool()) 
    val r_s_axi_arready = Reg(Bool()) 
    val r_s_axi_rdata   = Reg(UInt(C_S_AXI_DATA_WIDTH.W)) 
    val r_s_axi_rvalid  = Reg(Bool()) 
    val r_read_cnt      = Reg(UInt(8.W)) 

    // bram related logic
    // val bram = Module(new BRAMIP(1024, 8))

    val bram = Module(new XilinxBRAMWrapper)
    bram.io.clk := clock.asBool

    // BRAM default
    bram.io.ena := false.B
    bram.io.wea := false.B
    bram.io.addra := 0.U
    bram.io.dina := 0.U

    bram.io.enb := false.B
    bram.io.web := false.B
    bram.io.addrb := 0.U
    bram.io.dinb := 0.U

    // write state machine
    val sWriteIdle :: sWriteTrans :: sWriteEnd :: Nil = Enum(3)
    val r_writeState = RegInit(sWriteIdle)
    val r_writeLen   = Reg(UInt(8.W))
    val r_writeCount = Reg(UInt(8.W))
    val r_writeData  = Reg(UInt(C_S_AXI_DATA_WIDTH.W))
    val r_writeAddr  = Reg(UInt(C_S_AXI_ADDR_WIDTH.W))

    // read state machine
    val sReadIdle :: sReadFirst :: sReadTrans :: sReadEnd :: Nil = Enum(4)
    val r_readState     = RegInit(sReadIdle)
    val r_readLen       = (C_S_AXI_DATA_WIDTH / 8).U
    val r_readLastCount = Reg(UInt(8.W))
    val r_readCount     = Reg(UInt(8.W))
    val r_readAddr      = Reg(UInt(C_S_AXI_ADDR_WIDTH.W))
    val r_readBytes     = Reg(Vec(C_S_AXI_DATA_WIDTH / 8, UInt(8.W)))
  
    // write address channel
    io.S_AXI_AWREADY := r_s_axi_awready    

    // write channel
    io.S_AXI_WREADY  := true.B

    // write response channel
    io.S_AXI_BRESP   := 0.U
    io.S_AXI_BVALID  := r_s_axi_bvalid
  
    // read address channel
    io.S_AXI_ARREADY := r_s_axi_arready
  
    // read channel
    io.S_AXI_RDATA   := r_s_axi_rdata
    io.S_AXI_RRESP   := 0.U
    io.S_AXI_RVALID  := r_s_axi_rvalid
  
    r_s_axi_awready := Mux(w_system_reset | (io.S_AXI_WREADY& io.S_AXI_WVALID), true.B, 
                           Mux(io.S_AXI_AWVALID && io.S_AXI_AWREADY, false.B, r_s_axi_awready))

    r_s_axi_bvalid  := Mux(w_system_reset, false.B, 
                           Mux(r_writeState === sWriteEnd, true.B, false.B))

    r_s_axi_arready := Mux(w_system_reset, true.B, 
                           Mux(io.S_AXI_ARVALID & io.S_AXI_ARREADY, true.B, r_s_axi_arready))

    r_s_axi_rvalid  := Mux(w_system_reset, false.B, 
                           Mux(r_readState === sReadEnd, true.B, false.B))

    r_s_axi_rdata   := Mux(r_readState === sReadEnd, r_readBytes.reverse.reduceRight(Cat(_, _)), r_s_axi_rdata)

    // write state machine
    switch(r_writeState) {
      is(sWriteIdle) {
        when(io.S_AXI_AWVALID && io.S_AXI_AWREADY) {
          r_writeAddr  := io.S_AXI_AWADDR
        }
        when(io.S_AXI_WVALID & io.S_AXI_WREADY) {
          r_writeState := sWriteTrans
          r_writeCount := 0.U
          r_writeLen   := PopCount(io.S_AXI_WSTRB)
          r_writeData  := io.S_AXI_WDATA
        }
      }
      is(sWriteTrans) {
        when(r_writeCount < r_writeLen) {
          bram.io.addrb := r_writeAddr
          bram.io.enb   := true.B
          bram.io.web   := true.B
          bram.io.dinb  := r_writeData(7,0)

          r_writeData   := r_writeData >> 8;
          r_writeCount  := r_writeCount + 1.U
          r_writeAddr   := r_writeAddr + 1.U
        }

        r_writeState    := Mux(r_writeCount >= r_writeLen, sWriteEnd, sWriteTrans) 
      }
      is(sWriteEnd) {
        r_writeState    := sWriteIdle
      }
    }

    // read state machine
    switch(r_readState) {
      is(sReadIdle) {
        when(io.S_AXI_ARREADY & io.S_AXI_ARVALID) {
          r_readState := sReadFirst
          r_readCount := 0.U
          r_readAddr  := io.S_AXI_ARADDR

          for(i <- 0 until (C_S_AXI_DATA_WIDTH / 8)) {
            r_readBytes(i.U) := 0.U
          }
        }
      }
      is(sReadFirst) {
        bram.io.addra   := r_readAddr
        bram.io.ena     := true.B
        bram.io.wea     := false.B

        r_readAddr      := r_readAddr + 1.U
        r_readLastCount := r_readCount
        r_readCount     := r_readCount + 1.U
        r_readState     := sReadTrans
      }
      is(sReadTrans) {
        r_readBytes(r_readLastCount) := bram.io.douta

        bram.io.addra                := r_readAddr
        bram.io.ena                  := true.B
        bram.io.wea                  := false.B

        r_readAddr                   := r_readAddr + 1.U
        r_readLastCount              := r_readCount
        r_readCount                  := r_readCount + 1.U
        r_readState                  := Mux(r_readCount < r_readLen, sReadTrans, sReadEnd)
      }
      is(sReadEnd) {
        r_readState := sReadIdle
      }
    }

  }
}

class AXI4LiteSlaveBRAMIP(val C_S_AXI_DATA_WIDTH: Int, 
                          val C_S_AXI_ADDR_WIDTH: Int,
                          val depth: Int) extends Module {
  val io = IO(new Bundle{
    // write address channel
    val S_AXI_AWADDR  = Input(UInt(C_S_AXI_ADDR_WIDTH.W))
    val S_AXI_AWPROT  = Input(UInt(3.W))
    val S_AXI_AWVALID = Input(Bool())
    val S_AXI_AWREADY = Output(Bool())
    
    // write data channel
    val S_AXI_WDATA  = Input(UInt(C_S_AXI_DATA_WIDTH.W))
    val S_AXI_WSTRB  = Input(UInt((C_S_AXI_DATA_WIDTH/8).W))
    val S_AXI_WVALID = Input(Bool())
    val S_AXI_WREADY = Output(Bool())
    
    // write response channel
    val S_AXI_BRESP  = Output(UInt(2.W))
    val S_AXI_BVALID = Output(Bool())
    val S_AXI_BREADY = Input(Bool())
    
    // read address channel
    val S_AXI_ARADDR  = Input(UInt(C_S_AXI_ADDR_WIDTH.W))
    val S_AXI_ARPROT  = Input(UInt(3.W))
    val S_AXI_ARVALID = Input(Bool())
    val S_AXI_ARREADY = Output(Bool())
    
    // read data channel
    val S_AXI_RDATA  = Output(UInt(C_S_AXI_DATA_WIDTH.W))
    val S_AXI_RRESP  = Output(UInt(2.W))
    val S_AXI_RVALID = Output(Bool())
    val S_AXI_RREADY = Input(Bool())
  })

  val w_system_reset = !reset.asBool
  withReset(w_system_reset) {
    // registers for diff channels
    val r_s_axi_awready = Reg(Bool()) 
    val r_s_axi_wready  = Reg(Bool()) 
    val r_s_axi_bvalid  = Reg(Bool()) 
    val r_s_axi_arready = Reg(Bool()) 
    val r_s_axi_rdata   = Reg(UInt(C_S_AXI_DATA_WIDTH.W)) 
    val r_s_axi_rvalid  = Reg(Bool()) 

    // registers for valid and ready
    val r_valid = RegInit(false.B)
    val r_ready = RegInit(0.U(2.W))

    // bram related logic
    val bramIp = Module(new BRAMIPWrapper(depth))

    // BRAM default
    bramIp.io.mode         := 0.U
    bramIp.io.readAddr     := 0.U
    bramIp.io.readOffset   := 0.U
    bramIp.io.readLen      := 0.U
    bramIp.io.writeAddr    := 0.U
    bramIp.io.writeOffset  := 0.U
    bramIp.io.writeLen     := 0.U
    bramIp.io.writeData    := 0.U
    bramIp.io.dmaSrcAddr   := 0.U 
    bramIp.io.dmaDstAddr   := 0.U 
    bramIp.io.dmaDstOffset := 0.U 
    bramIp.io.dmaSrcLen    := 0.U 
    bramIp.io.dmaDstLen    := 0.U 

    // write state machine
    val sWriteIdle :: sWriteTrans :: sWriteEnd :: Nil = Enum(3)
    val r_writeState = RegInit(sWriteIdle)
    val r_writeLen   = Reg(UInt(8.W))
    val r_writeData  = Reg(UInt(C_S_AXI_DATA_WIDTH.W))
    val r_writeAddr  = Reg(UInt(C_S_AXI_ADDR_WIDTH.W))

    // read state machine
    val sReadIdle :: sReadTrans :: sReadEnd :: Nil = Enum(3)
    val r_readState     = RegInit(sReadIdle)
    val r_readLen       = (C_S_AXI_DATA_WIDTH / 8).U
    val r_readAddr      = Reg(UInt(C_S_AXI_ADDR_WIDTH.W))
    val r_readData      = Reg(UInt(C_S_AXI_DATA_WIDTH.W))
  
    // write address channel
    io.S_AXI_AWREADY := r_s_axi_awready    

    // write channel
    io.S_AXI_WREADY  := true.B

    // write response channel
    io.S_AXI_BRESP   := 0.U
    io.S_AXI_BVALID  := r_s_axi_bvalid
  
    // read address channel
    io.S_AXI_ARREADY := r_s_axi_arready
  
    // read channel
    io.S_AXI_RDATA   := r_s_axi_rdata
    io.S_AXI_RRESP   := 0.U
    io.S_AXI_RVALID  := r_s_axi_rvalid
  
    r_s_axi_awready := Mux(w_system_reset | (io.S_AXI_WREADY& io.S_AXI_WVALID), true.B, 
                           Mux(io.S_AXI_AWVALID && io.S_AXI_AWREADY, false.B, r_s_axi_awready))

    r_s_axi_bvalid  := Mux(w_system_reset, false.B, 
                           Mux(r_writeState === sWriteEnd, true.B, false.B))

    r_s_axi_arready := Mux(w_system_reset, true.B, 
                           Mux(io.S_AXI_ARVALID & io.S_AXI_ARREADY, true.B, r_s_axi_arready))

    r_s_axi_rvalid  := Mux(w_system_reset, false.B, 
                           Mux(r_readState === sReadEnd, true.B, false.B))

    r_s_axi_rdata   := Mux(r_readState === sReadEnd, r_readData, r_s_axi_rdata)

    // valid and ready registers for memoryIP
    r_valid := Mux(io.S_AXI_WVALID & io.S_AXI_WREADY & (r_writeAddr === depth.U), io.S_AXI_WDATA(0), r_valid)
    r_ready := 1.U

    // write state machine
    switch(r_writeState) {
      is(sWriteIdle) {
        when(io.S_AXI_AWVALID && io.S_AXI_AWREADY) {
          r_writeAddr  := io.S_AXI_AWADDR
        }
        when(io.S_AXI_WVALID & io.S_AXI_WREADY) {
          r_writeState := Mux(r_writeAddr < depth.U, sWriteTrans, sWriteEnd)
          r_writeLen   := PopCount(io.S_AXI_WSTRB)
          r_writeData  := io.S_AXI_WDATA
        }
      }
      is(sWriteTrans) {
        bramIp.io.mode        := 2.U
        bramIp.io.writeAddr   := r_writeAddr
        bramIp.io.writeOffset := 0.U
        bramIp.io.writeLen    := r_writeLen
        bramIp.io.writeData   := r_writeData
        when(bramIp.io.writeValid) {
          bramIp.io.mode  := 0.U
          r_writeState    := sWriteEnd
        }
      }
      is(sWriteEnd) {
        r_writeState    := sWriteIdle
      }
    }

    // read state machine
    switch(r_readState) {
      is(sReadIdle) {
        when(io.S_AXI_ARREADY & io.S_AXI_ARVALID) {
          r_readState := Mux(io.S_AXI_ARADDR === depth.U , sReadEnd, sReadTrans)
          r_readData  := Mux(io.S_AXI_ARADDR === depth.U , r_ready, r_readData)
          r_readAddr  := io.S_AXI_ARADDR
        }
      }
      is(sReadTrans) {
        bramIp.io.mode        := 1.U
        bramIp.io.readAddr    := r_readAddr
        bramIp.io.readOffset  := 0.U
        bramIp.io.readLen     := r_readLen
        when(bramIp.io.readValid) {
          r_readData     := bramIp.io.readData
          bramIp.io.mode := 0.U
          r_readState    := sReadEnd
        }
      }
      is(sReadEnd) {
        r_readState := sReadIdle
      }
    }

  }
}