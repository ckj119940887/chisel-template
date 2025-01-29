package AXIWrapperChisel

import chisel3._
import chisel3.util._
import chisel3.experimental._

class AXIWrapper (val C_S_AXI_ADDR_WIDTH: Int = 32, val C_S_AXI_DATA_WIDTH: Int = 32) extends Module {
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


    // AXI4LITE signals, the signals need to be saved
    val axi_awaddr  = Reg(UInt(C_S_AXI_ADDR_WIDTH.W))
    val axi_araddr  = Reg(UInt(C_S_AXI_ADDR_WIDTH.W))
    // AXI4LITE output signal
    val axi_awready = Reg(Bool())
    val axi_wready  = Reg(Bool())
    val axi_bresp   = Reg(UInt(2.W))
    val axi_bvalid  = Reg(Bool())
    val axi_arready = Reg(Bool())
    val axi_rdata   = Reg(UInt(C_S_AXI_DATA_WIDTH.W))
    val axi_rresp   = Reg(UInt(2.W))
    val axi_rvalid  = Reg(Bool())


    // Example-specific design signals
    // local parameter for addressing 32 bit / 64 bit C_S_AXI_DATA_WIDTH
    // ADDR_LSB is used for addressing 32/64 bit registers/memories
    // ADDR_LSB = 2 for 32 bits (n downto 2)
    // ADDR_LSB = 3 for 64 bits (n downto 3)
    val ADDR_LSB: Int = (C_S_AXI_DATA_WIDTH/32) + 1
    val OPT_MEM_ADDR_BITS: Int = 7

    //----------------------------------------------
    //-- Signals for user logic register space example
    //------------------------------------------------
    val io_valid_reg = Reg(UInt(32.W))
    val io_ready_reg = Reg(UInt(1.W))
    val slv_reg_rden = Wire(Bool())
    val slv_reg_wren = Wire(Bool())
    val reg_data_out = Reg(UInt(C_S_AXI_DATA_WIDTH.W))
    val aw_en        = Reg(Bool())

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
    when(reset.asBool) {
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
    when(reset.asBool) {
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
    when(reset.asBool) {
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

    // Implement write response logic generation
    // The write response and response valid signals are asserted by the slave
    // when axi_wready, S_AXI_WVALID, axi_wready and S_AXI_WVALID are asserted.
    // This marks the acceptance of address and indicates the status of
    // write transaction.
    when(reset.asBool) {
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
    when(reset.asBool) {
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
    when(reset.asBool) {
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
    when(reset.asBool) {
        axi_rdata := 0.U
    } .otherwise {
        // When there is a valid read address (S_AXI_ARVALID) with
        // acceptance of read address by the slave (axi_arready),
        // output the read dada
        when(slv_reg_rden) {
            axi_rdata := reg_data_out
        }
    }

}