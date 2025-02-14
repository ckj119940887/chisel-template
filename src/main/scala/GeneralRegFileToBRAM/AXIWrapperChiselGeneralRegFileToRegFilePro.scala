package GeneralRegFileToBRAM

import chisel3._
import chisel3.util._
import chisel3.experimental._

class AXIWrapperChiselGeneralRegFileToRegFilePro(val C_S_AXI_ADDR_WIDTH: Int = 32, 
                                              val C_S_AXI_DATA_WIDTH: Int = 32,
                                              val ARRAY_REG_WIDTH: Int = 8,
                                              val ARRAY_REG_DEPTH: Int = 1024,
                                              val GENERAL_REG_WIDTH: Int = 32,
                                              val GENERAL_REG_DEPTH: Int = 64,
                                              val NUM_STATES: Int = 10) extends Module {
    val io = IO(new Bundle{
        // write address channel
        val S_AXI_AWADDR  = Input(UInt(C_S_AXI_ADDR_WIDTH.W))
        val S_AXI_AWPROT  = Input(UInt(3.W))
        val S_AXI_AWVALID = Input(Bool())
        val S_AXI_AWREADY = Output(Bool())

        // write data channel
        val S_AXI_WDATA  = Input(SInt(C_S_AXI_DATA_WIDTH.W))
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
        val S_AXI_RDATA  = Output(SInt(C_S_AXI_DATA_WIDTH.W))
        val S_AXI_RRESP  = Output(UInt(2.W))
        val S_AXI_RVALID = Output(Bool())
        val S_AXI_RREADY = Input(Bool())
    })

    val lowActiveReset = !reset.asBool

  withReset(lowActiveReset) {

    // AXI4LITE signals, the signals need to be saved
    val axi_awaddr  = Reg(UInt(C_S_AXI_ADDR_WIDTH.W))
    val axi_araddr  = Reg(UInt(C_S_AXI_ADDR_WIDTH.W))
    // AXI4LITE output signal
    val axi_awready = Reg(Bool())
    val axi_wready  = Reg(Bool())
    val axi_bresp   = Reg(UInt(2.W))
    val axi_bvalid  = Reg(Bool())
    val axi_arready = Reg(Bool())
    val axi_rdata   = Reg(SInt(C_S_AXI_DATA_WIDTH.W))
    val axi_rresp   = Reg(UInt(2.W))
    val axi_rvalid  = Reg(Bool())


    // Example-specific design signals
    // local parameter for addressing 32 bit / 64 bit C_S_AXI_DATA_WIDTH
    // ADDR_LSB is used for addressing 32/64 bit registers/memories
    // ADDR_LSB = 2 for 32 bits (n downto 2)
    // ADDR_LSB = 3 for 64 bits (n downto 3)
    val ADDR_LSB: Int = (C_S_AXI_DATA_WIDTH/32) + 1
    val OPT_MEM_ADDR_BITS: Int = 10

    //----------------------------------------------
    //-- Signals for user logic register space example
    //------------------------------------------------
    val slv_reg_rden = Wire(Bool())
    val slv_reg_wren = Wire(Bool())
    val reg_data_out = Wire(SInt(C_S_AXI_DATA_WIDTH.W))
    val aw_en        = Reg(Bool())

    // Registers for target module port
    val io_valid_reg = Reg(UInt(32.W))
    val io_ready_reg = Reg(Bool())

    // instantiate the target module
    val arrayReadValid = (axi_araddr(log2Ceil(ARRAY_REG_DEPTH), 0) + 3.U) < ARRAY_REG_DEPTH.U
    val arrayWriteValid = (axi_awaddr(log2Ceil(ARRAY_REG_DEPTH), 0) + 3.U) < ARRAY_REG_DEPTH.U
    val arrayReady = axi_araddr(log2Ceil(ARRAY_REG_DEPTH), 0) === (ARRAY_REG_DEPTH * ARRAY_REG_WIDTH / 8 + 4).U
    val modRegFileToRegFilePro = Module(new GeneralRegFileToRegFilePro(
                               ARRAY_REG_WIDTH = ARRAY_REG_WIDTH,
                               ARRAY_REG_DEPTH = ARRAY_REG_DEPTH,
                               GENERAL_REG_WIDTH = GENERAL_REG_WIDTH,
                               GENERAL_REG_DEPTH = GENERAL_REG_DEPTH,
                               NUM_STATES = NUM_STATES))
    modRegFileToRegFilePro.io.valid := io_valid_reg(0)
    io_ready_reg := modRegFileToRegFilePro.io.ready
    modRegFileToRegFilePro.io.arrayRe := arrayReadValid
    modRegFileToRegFilePro.io.arrayWe := slv_reg_wren && arrayWriteValid
    modRegFileToRegFilePro.io.arrayStrb := Mux(slv_reg_wren && arrayWriteValid, io.S_AXI_WSTRB, 0.U)
    modRegFileToRegFilePro.io.arrayAddr := Mux(slv_reg_wren && arrayWriteValid, 
                                               axi_awaddr(log2Ceil(ARRAY_REG_DEPTH) - 1, 0), 
                                               Mux(arrayReadValid, axi_araddr(log2Ceil(ARRAY_REG_DEPTH) - 1, 0), 0.U))
    modRegFileToRegFilePro.io.arrayWData := Mux(slv_reg_wren && arrayWriteValid, io.S_AXI_WDATA.asUInt, 0.U)
    
    when(arrayReady) {
        reg_data_out := Cat(0.U, io_ready_reg.asUInt).asSInt
    } .elsewhen(arrayReadValid) {
        reg_data_out := modRegFileToRegFilePro.io.arrayRData.asSInt
    } .otherwise {
        reg_data_out := 0.S
    }

    when(lowActiveReset.asBool) {
        io_ready_reg := false.B
    } .otherwise {
        io_ready_reg := Mux(modRegFileToRegFilePro.io.ready, true.B, io_ready_reg)
    }

    // I/O Connections assignments
    io.S_AXI_AWREADY := axi_awready;
    io.S_AXI_WREADY  := axi_wready;
    io.S_AXI_BRESP   := axi_bresp;
    io.S_AXI_BVALID	 := axi_bvalid;
    io.S_AXI_ARREADY := axi_arready;
    io.S_AXI_RDATA   := axi_rdata;
    io.S_AXI_RRESP   := axi_rresp;
    io.S_AXI_RVALID  := axi_rvalid;

    // Implement axi_awready generation
    // axi_awready is asserted for one S_AXI_ACLK clock cycle when both
    // S_AXI_AWVALID and S_AXI_WVALID are asserted. axi_awready is
    // de-asserted when reset is low.
    when(lowActiveReset.asBool) {
        axi_awready := false.B
        aw_en       := true.B
    } .otherwise {
        when(~axi_awready && io.S_AXI_AWVALID && io.S_AXI_WVALID && aw_en) {
            // slave is ready to accept write address when
            // there is a valid write address and write data
            // on the write address and data bus. This design
            // expects no outstanding transactions.
            axi_awready := true.B
            aw_en       := false.B
        } .elsewhen(io.S_AXI_BREADY && axi_bvalid) {
            // the current operation is finished
            // prepare for the next write operation
            axi_awready := false.B
            aw_en       := true.B
        } .otherwise {
            axi_awready  := false.B
        }
    }

    // Implement axi_awaddr latching
    // This process is used to latch the address when both
    // S_AXI_AWVALID and S_AXI_WVALID are valid.
    when(lowActiveReset.asBool) {
        axi_awaddr := 0.U
    } .otherwise {
        when(~axi_awready && io.S_AXI_AWVALID && io.S_AXI_WVALID && aw_en) {
            axi_awaddr := io.S_AXI_AWADDR
        } 
    }

    // Implement axi_wready generation
    // axi_wready is asserted for one S_AXI_ACLK clock cycle when both
    // S_AXI_AWVALID and S_AXI_WVALID are asserted. axi_wready is
    // de-asserted when reset is low.
    when(lowActiveReset.asBool) {
        axi_wready := false.B
    } .otherwise {
        when(~axi_wready && io.S_AXI_WVALID && io.S_AXI_AWVALID && aw_en) {
            // slave is ready to accept write data when
            // there is a valid write address and write data
            // on the write address and data bus. This design
            // expects no outstanding transactions.
            axi_wready := true.B
        } .otherwise {
            axi_wready := false.B
        }
    }

    // Implement memory mapped register select and write logic generation
    // The write data is accepted and written to memory mapped registers when
    // axi_awready, S_AXI_WVALID, axi_wready and S_AXI_WVALID are asserted. Write strobes are used to
    // select byte enables of slave registers while writing.
    // These registers are cleared when reset (active low) is applied.
    // Slave register write enable is asserted when valid address and data are available
    // and the slave is ready to accept the write address and write data.
    slv_reg_wren := axi_wready && io.S_AXI_WVALID && axi_awready && io.S_AXI_AWVALID

    val writeEffectiveAddr = axi_awaddr(log2Ceil(ARRAY_REG_DEPTH), 0)

    when(lowActiveReset.asBool) {
        io_valid_reg := 0.U
    } .otherwise {
        when(slv_reg_wren && writeEffectiveAddr === (ARRAY_REG_DEPTH * ARRAY_REG_WIDTH / 8).U) {
                io_valid_reg := io.S_AXI_WDATA.asUInt
        }
    }

    // Implement write response logic generation
    // The write response and response valid signals are asserted by the slave
    // when axi_wready, S_AXI_WVALID, axi_wready and S_AXI_WVALID are asserted.
    // This marks the acceptance of address and indicates the status of
    // write transaction.
    when(lowActiveReset.asBool) {
        axi_bvalid := false.B
        axi_bresp  := 0.U
    } .otherwise {
        when(axi_awready && io.S_AXI_AWVALID && ~axi_bvalid && axi_wready && io.S_AXI_WVALID) {
            axi_bvalid := true.B
            axi_bresp  := 0.U
        } .otherwise {
            when(io.S_AXI_BREADY && axi_bvalid) {
                axi_bvalid := false.B
            }
        }
    }

    // Implement axi_arready generation
    // axi_arready is asserted for one S_AXI_ACLK clock cycle when
    // S_AXI_ARVALID is asserted. axi_awready is
    // de-asserted when reset (active low) is asserted.
    // The read address is also latched when S_AXI_ARVALID is
    // asserted. axi_araddr is reset to zero on reset assertion.
    when(lowActiveReset.asBool) {
        axi_arready := false.B
        axi_araddr  := 0.U
    } .otherwise {
        when(~axi_arready && io.S_AXI_ARVALID) {
            // indicates that the slave has acceped the valid read address
            axi_arready := true.B
            axi_araddr  := io.S_AXI_ARADDR
        } .otherwise {
            axi_arready := false.B
        }
    }

    // Implement axi_arvalid generation
    // axi_rvalid is asserted for one S_AXI_ACLK clock cycle when both
    // S_AXI_ARVALID and axi_arready are asserted. The slave registers
    // data are available on the axi_rdata bus at this instance. The
    // assertion of axi_rvalid marks the validity of read data on the
    // bus and axi_rresp indicates the status of read transaction.axi_rvalid
    // is deasserted on reset (active low). axi_rresp and axi_rdata are
    // cleared to zero on reset (active low).
    when(lowActiveReset.asBool) {
        axi_rvalid := false.B
        axi_rresp  := 0.U
    } .otherwise {
        when(axi_arready && io.S_AXI_ARVALID && ~axi_rvalid) {
            axi_rvalid := true.B
            axi_rresp  := 0.U
        } .elsewhen(axi_rvalid && io.S_AXI_RREADY) {
            axi_rvalid := false.B
        }
    }

    // Implement memory mapped register select and read logic generation
    // Slave register read enable is asserted when valid address is available
    // and the slave is ready to accept the read address.
    slv_reg_rden := axi_arready & io.S_AXI_ARVALID & ~axi_rvalid;

    // Output register or memory read data
    when(lowActiveReset.asBool) {
        axi_rdata := 0.S
    } .otherwise {
        // When there is a valid read address (S_AXI_ARVALID) with
        // acceptance of read address by the slave (axi_arready),
        // output the read dada
        when(slv_reg_rden) {
            axi_rdata := reg_data_out
        }
    }
  }
}