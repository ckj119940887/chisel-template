module AXILiteInsertSort #(
	// Users to add parameters here

	// User parameters ends
	// Do not modify the parameters beyond this line

	// Width of S_AXI data bus
	parameter integer C_S_AXI_DATA_WIDTH	= 32,
	// Width of S_AXI address bus
	parameter integer C_S_AXI_ADDR_WIDTH	= 32
)
(
	// Users to add ports here

	// User ports ends
	// Do not modify the ports beyond this line

	// Global Clock Signal
	input wire  S_AXI_ACLK,
	// Global Reset Signal. This Signal is Active LOW
	input wire  S_AXI_ARESETN,
	// Write address (issued by master, acceped by Slave)
	input wire [C_S_AXI_ADDR_WIDTH-1 : 0] S_AXI_AWADDR,
	// Write channel Protection type. This signal indicates the
   	// privilege and security level of the transaction, and whether
    // the transaction is a data access or an instruction access.
    input wire [2 : 0] S_AXI_AWPROT,
    // Write address valid. This signal indicates that the master signaling
    // valid write address and control information.
    input wire  S_AXI_AWVALID,
    // Write address ready. This signal indicates that the slave is ready
    // to accept an address and associated control signals.
    output wire  S_AXI_AWREADY,
    // Write data (issued by master, acceped by Slave)
    input wire [C_S_AXI_DATA_WIDTH-1 : 0] S_AXI_WDATA,
    // Write strobes. This signal indicates which byte lanes hold
    // valid data. There is one write strobe bit for each eight
    // bits of the write data bus.
    input wire [(C_S_AXI_DATA_WIDTH/8)-1 : 0] S_AXI_WSTRB,
    // Write valid. This signal indicates that valid write
    // data and strobes are available.
    input wire  S_AXI_WVALID,
    // Write ready. This signal indicates that the slave
    // can accept the write data.
    output wire  S_AXI_WREADY,
    // Write response. This signal indicates the status
    // of the write transaction.
    output wire [1 : 0] S_AXI_BRESP,
    // Write response valid. This signal indicates that the channel
    // is signaling a valid write response.
    output wire  S_AXI_BVALID,
    // Response ready. This signal indicates that the master
    // can accept a write response.
    input wire  S_AXI_BREADY,
    // Read address (issued by master, acceped by Slave)
    input wire [C_S_AXI_ADDR_WIDTH-1 : 0] S_AXI_ARADDR,
    // Protection type. This signal indicates the privilege
    // and security level of the transaction, and whether the
    // transaction is a data access or an instruction access.
    input wire [2 : 0] S_AXI_ARPROT,
    // Read address valid. This signal indicates that the channel
    // is signaling valid read address and control information.
    input wire  S_AXI_ARVALID,
    // Read address ready. This signal indicates that the slave is
    // ready to accept an address and associated control signals.
    output wire  S_AXI_ARREADY,
    // Read data (issued by slave)
    output wire [C_S_AXI_DATA_WIDTH-1 : 0] S_AXI_RDATA,
    // Read response. This signal indicates the status of the
    // read transfer.
    output wire [1 : 0] S_AXI_RRESP,
    // Read valid. This signal indicates that the channel is
    // signaling the required read data.
    output wire  S_AXI_RVALID,
    // Read ready. This signal indicates that the master can
    // accept the read data and response information.
    input wire  S_AXI_RREADY
);

// AXI4LITE signals
reg [C_S_AXI_ADDR_WIDTH-1 : 0] 	axi_awaddr;
reg  	axi_awready;
reg  	axi_wready;
reg [1 : 0] 	axi_bresp;
reg  	axi_bvalid;
reg [C_S_AXI_ADDR_WIDTH-1 : 0] 	axi_araddr;
reg  	axi_arready;
reg [C_S_AXI_DATA_WIDTH-1 : 0] 	axi_rdata;
reg [1 : 0] 	axi_rresp;
reg  	axi_rvalid;

// Example-specific design signals
// local parameter for addressing 32 bit / 64 bit C_S_AXI_DATA_WIDTH
// ADDR_LSB is used for addressing 32/64 bit registers/memories
// ADDR_LSB = 2 for 32 bits (n downto 2)
// ADDR_LSB = 3 for 64 bits (n downto 3)
localparam integer ADDR_LSB = (C_S_AXI_DATA_WIDTH/32) + 1;
localparam integer OPT_MEM_ADDR_BITS = 7;
//----------------------------------------------
//-- Signals for user logic register space example
//------------------------------------------------
reg [31:0] io_valid_reg;
reg io_ready_reg;
reg [31:0] io_array [19:0];
wire [31:0] io_array_out [19:0];
wire	 slv_reg_rden;
wire	 slv_reg_wren;
reg [C_S_AXI_DATA_WIDTH-1:0]	 reg_data_out;
integer	 byte_index;
reg	 aw_en;

// I/O Connections assignments

assign S_AXI_AWREADY	= axi_awready;
assign S_AXI_WREADY	= axi_wready;
assign S_AXI_BRESP	= axi_bresp;
assign S_AXI_BVALID	= axi_bvalid;
assign S_AXI_ARREADY	= axi_arready;
assign S_AXI_RDATA	= axi_rdata;
assign S_AXI_RRESP	= axi_rresp;
assign S_AXI_RVALID	= axi_rvalid;
// Implement axi_awready generation
// axi_awready is asserted for one S_AXI_ACLK clock cycle when both
// S_AXI_AWVALID and S_AXI_WVALID are asserted. axi_awready is
// de-asserted when reset is low.

always @( posedge S_AXI_ACLK )
begin
  if ( S_AXI_ARESETN == 1'b0 )
    begin
      axi_awready <= 1'b0;
      aw_en <= 1'b1;
    end
  else
    begin
      if (~axi_awready && S_AXI_AWVALID && S_AXI_WVALID && aw_en)
        begin
          // slave is ready to accept write address when
          // there is a valid write address and write data
          // on the write address and data bus. This design
          // expects no outstanding transactions.
          axi_awready <= 1'b1;
          aw_en <= 1'b0;
        end
        else if (S_AXI_BREADY && axi_bvalid)
            begin
              aw_en <= 1'b1;
              axi_awready <= 1'b0;
            end
      else
        begin
          axi_awready <= 1'b0;
        end
    end
end

// Implement axi_awaddr latching
// This process is used to latch the address when both
// S_AXI_AWVALID and S_AXI_WVALID are valid.

always @( posedge S_AXI_ACLK )
begin
  if ( S_AXI_ARESETN == 1'b0 )
    begin
      axi_awaddr <= 0;
    end
  else
    begin
      if (~axi_awready && S_AXI_AWVALID && S_AXI_WVALID && aw_en)
        begin
          // Write Address latching
          axi_awaddr <= S_AXI_AWADDR;
        end
    end
end

// Implement axi_wready generation
// axi_wready is asserted for one S_AXI_ACLK clock cycle when both
// S_AXI_AWVALID and S_AXI_WVALID are asserted. axi_wready is
// de-asserted when reset is low.

always @( posedge S_AXI_ACLK )
begin
  if ( S_AXI_ARESETN == 1'b0 )
    begin
      axi_wready <= 1'b0;
    end
  else
    begin
      if (~axi_wready && S_AXI_WVALID && S_AXI_AWVALID && aw_en )
        begin
          // slave is ready to accept write data when
          // there is a valid write address and write data
          // on the write address and data bus. This design
          // expects no outstanding transactions.
          axi_wready <= 1'b1;
        end
      else
        begin
          axi_wready <= 1'b0;
        end
    end
end

// Implement memory mapped register select and write logic generation
// The write data is accepted and written to memory mapped registers when
// axi_awready, S_AXI_WVALID, axi_wready and S_AXI_WVALID are asserted. Write strobes are used to
// select byte enables of slave registers while writing.
// These registers are cleared when reset (active low) is applied.
// Slave register write enable is asserted when valid address and data are available
// and the slave is ready to accept the write address and write data.
assign slv_reg_wren = axi_wready && S_AXI_WVALID && axi_awready && S_AXI_AWVALID;

always @( posedge S_AXI_ACLK )
begin
  if ( S_AXI_ARESETN == 1'b0 )
    begin
      io_valid_reg <= 0;
    end
  else begin
    if (slv_reg_wren)
      begin
        case ( axi_awaddr[ADDR_LSB+OPT_MEM_ADDR_BITS:ADDR_LSB] )
          8'h0:
            for ( byte_index = 0; byte_index <= (C_S_AXI_DATA_WIDTH/8)-1; byte_index = byte_index+1 )
              if ( S_AXI_WSTRB[byte_index] == 1 ) begin
                // Respective byte enables are asserted as per write strobes
                // Slave register 0
                io_array[0][(byte_index*8) +: 8] <= S_AXI_WDATA[(byte_index*8) +: 8];
              end
          8'h1:
            for ( byte_index = 0; byte_index <= (C_S_AXI_DATA_WIDTH/8)-1; byte_index = byte_index+1 )
              if ( S_AXI_WSTRB[byte_index] == 1 ) begin
                // Respective byte enables are asserted as per write strobes
                // Slave register 1
                io_array[1][(byte_index*8) +: 8] <= S_AXI_WDATA[(byte_index*8) +: 8];
              end
          8'h2:
            for ( byte_index = 0; byte_index <= (C_S_AXI_DATA_WIDTH/8)-1; byte_index = byte_index+1 )
              if ( S_AXI_WSTRB[byte_index] == 1 ) begin
                // Respective byte enables are asserted as per write strobes
                // Slave register 2
                io_array[2][(byte_index*8) +: 8] <= S_AXI_WDATA[(byte_index*8) +: 8];
              end
          8'h3:
            for ( byte_index = 0; byte_index <= (C_S_AXI_DATA_WIDTH/8)-1; byte_index = byte_index+1 )
              if ( S_AXI_WSTRB[byte_index] == 1 ) begin
                // Respective byte enables are asserted as per write strobes
                // Slave register 3
                io_array[3][(byte_index*8) +: 8] <= S_AXI_WDATA[(byte_index*8) +: 8];
              end
          8'h4:
            for ( byte_index = 0; byte_index <= (C_S_AXI_DATA_WIDTH/8)-1; byte_index = byte_index+1 )
              if ( S_AXI_WSTRB[byte_index] == 1 ) begin
                // Respective byte enables are asserted as per write strobes
                // Slave register 3
                io_array[4][(byte_index*8) +: 8] <= S_AXI_WDATA[(byte_index*8) +: 8];
              end
          8'h5:
            for ( byte_index = 0; byte_index <= (C_S_AXI_DATA_WIDTH/8)-1; byte_index = byte_index+1 )
              if ( S_AXI_WSTRB[byte_index] == 1 ) begin
                // Respective byte enables are asserted as per write strobes
                // Slave register 3
                io_array[5][(byte_index*8) +: 8] <= S_AXI_WDATA[(byte_index*8) +: 8];
              end
          8'h6:
            for ( byte_index = 0; byte_index <= (C_S_AXI_DATA_WIDTH/8)-1; byte_index = byte_index+1 )
              if ( S_AXI_WSTRB[byte_index] == 1 ) begin
                // Respective byte enables are asserted as per write strobes
                // Slave register 3
                io_array[6][(byte_index*8) +: 8] <= S_AXI_WDATA[(byte_index*8) +: 8];
              end
          8'h7:
            for ( byte_index = 0; byte_index <= (C_S_AXI_DATA_WIDTH/8)-1; byte_index = byte_index+1 )
              if ( S_AXI_WSTRB[byte_index] == 1 ) begin
                // Respective byte enables are asserted as per write strobes
                // Slave register 3
                io_array[7][(byte_index*8) +: 8] <= S_AXI_WDATA[(byte_index*8) +: 8];
              end
          8'h8:
            for ( byte_index = 0; byte_index <= (C_S_AXI_DATA_WIDTH/8)-1; byte_index = byte_index+1 )
              if ( S_AXI_WSTRB[byte_index] == 1 ) begin
                // Respective byte enables are asserted as per write strobes
                // Slave register 3
                io_array[8][(byte_index*8) +: 8] <= S_AXI_WDATA[(byte_index*8) +: 8];
              end
          8'h9:
            for ( byte_index = 0; byte_index <= (C_S_AXI_DATA_WIDTH/8)-1; byte_index = byte_index+1 )
              if ( S_AXI_WSTRB[byte_index] == 1 ) begin
                // Respective byte enables are asserted as per write strobes
                // Slave register 3
                io_array[9][(byte_index*8) +: 8] <= S_AXI_WDATA[(byte_index*8) +: 8];
              end
          8'hA:
            for ( byte_index = 0; byte_index <= (C_S_AXI_DATA_WIDTH/8)-1; byte_index = byte_index+1 )
              if ( S_AXI_WSTRB[byte_index] == 1 ) begin
                // Respective byte enables are asserted as per write strobes
                // Slave register 3
                io_array[10][(byte_index*8) +: 8] <= S_AXI_WDATA[(byte_index*8) +: 8];
              end
          8'hB:
            for ( byte_index = 0; byte_index <= (C_S_AXI_DATA_WIDTH/8)-1; byte_index = byte_index+1 )
              if ( S_AXI_WSTRB[byte_index] == 1 ) begin
                // Respective byte enables are asserted as per write strobes
                // Slave register 3
                io_array[11][(byte_index*8) +: 8] <= S_AXI_WDATA[(byte_index*8) +: 8];
              end
          8'hC:
            for ( byte_index = 0; byte_index <= (C_S_AXI_DATA_WIDTH/8)-1; byte_index = byte_index+1 )
              if ( S_AXI_WSTRB[byte_index] == 1 ) begin
                // Respective byte enables are asserted as per write strobes
                // Slave register 3
                io_array[12][(byte_index*8) +: 8] <= S_AXI_WDATA[(byte_index*8) +: 8];
              end
          8'hD:
            for ( byte_index = 0; byte_index <= (C_S_AXI_DATA_WIDTH/8)-1; byte_index = byte_index+1 )
              if ( S_AXI_WSTRB[byte_index] == 1 ) begin
                // Respective byte enables are asserted as per write strobes
                // Slave register 3
                io_array[13][(byte_index*8) +: 8] <= S_AXI_WDATA[(byte_index*8) +: 8];
              end
          8'hE:
            for ( byte_index = 0; byte_index <= (C_S_AXI_DATA_WIDTH/8)-1; byte_index = byte_index+1 )
              if ( S_AXI_WSTRB[byte_index] == 1 ) begin
                // Respective byte enables are asserted as per write strobes
                // Slave register 3
                io_array[14][(byte_index*8) +: 8] <= S_AXI_WDATA[(byte_index*8) +: 8];
              end
          8'hF:
            for ( byte_index = 0; byte_index <= (C_S_AXI_DATA_WIDTH/8)-1; byte_index = byte_index+1 )
              if ( S_AXI_WSTRB[byte_index] == 1 ) begin
                // Respective byte enables are asserted as per write strobes
                // Slave register 3
                io_array[15][(byte_index*8) +: 8] <= S_AXI_WDATA[(byte_index*8) +: 8];
              end
          8'h10:
            for ( byte_index = 0; byte_index <= (C_S_AXI_DATA_WIDTH/8)-1; byte_index = byte_index+1 )
              if ( S_AXI_WSTRB[byte_index] == 1 ) begin
                // Respective byte enables are asserted as per write strobes
                // Slave register 3
                io_array[16][(byte_index*8) +: 8] <= S_AXI_WDATA[(byte_index*8) +: 8];
              end
          8'h11:
            for ( byte_index = 0; byte_index <= (C_S_AXI_DATA_WIDTH/8)-1; byte_index = byte_index+1 )
              if ( S_AXI_WSTRB[byte_index] == 1 ) begin
                // Respective byte enables are asserted as per write strobes
                // Slave register 3
                io_array[17][(byte_index*8) +: 8] <= S_AXI_WDATA[(byte_index*8) +: 8];
              end
          8'h12:
            for ( byte_index = 0; byte_index <= (C_S_AXI_DATA_WIDTH/8)-1; byte_index = byte_index+1 )
              if ( S_AXI_WSTRB[byte_index] == 1 ) begin
                // Respective byte enables are asserted as per write strobes
                // Slave register 3
                io_array[18][(byte_index*8) +: 8] <= S_AXI_WDATA[(byte_index*8) +: 8];
              end
          8'h13:
            for ( byte_index = 0; byte_index <= (C_S_AXI_DATA_WIDTH/8)-1; byte_index = byte_index+1 )
              if ( S_AXI_WSTRB[byte_index] == 1 ) begin
                // Respective byte enables are asserted as per write strobes
                // Slave register 3
                io_array[19][(byte_index*8) +: 8] <= S_AXI_WDATA[(byte_index*8) +: 8];
              end
          8'h28:
            for ( byte_index = 0; byte_index <= (C_S_AXI_DATA_WIDTH/8)-1; byte_index = byte_index+1 )
              if ( S_AXI_WSTRB[byte_index] == 1 ) begin
                // Respective byte enables are asserted as per write strobes
                // Slave register 3
                io_valid_reg[(byte_index*8) +: 8] <= S_AXI_WDATA[(byte_index*8) +: 8];
              end
        endcase
      end
  end
end

// Implement write response logic generation
// The write response and response valid signals are asserted by the slave
// when axi_wready, S_AXI_WVALID, axi_wready and S_AXI_WVALID are asserted.
// This marks the acceptance of address and indicates the status of
// write transaction.

always @( posedge S_AXI_ACLK )
begin
  if ( S_AXI_ARESETN == 1'b0 )
    begin
      axi_bvalid  <= 0;
      axi_bresp   <= 2'b0;
    end
  else
    begin
      if (axi_awready && S_AXI_AWVALID && ~axi_bvalid && axi_wready && S_AXI_WVALID)
        begin
          // indicates a valid write response is available
          axi_bvalid <= 1'b1;
          axi_bresp  <= 2'b0; // 'OKAY' response
        end                   // work error responses in future
      else
        begin
          if (S_AXI_BREADY && axi_bvalid)
            //check if bready is asserted while bvalid is high)
            //(there is a possibility that bready is always asserted high)
            begin
              axi_bvalid <= 1'b0;
            end
        end
    end
end

// Implement axi_arready generation
// axi_arready is asserted for one S_AXI_ACLK clock cycle when
// S_AXI_ARVALID is asserted. axi_awready is
// de-asserted when reset (active low) is asserted.
// The read address is also latched when S_AXI_ARVALID is
// asserted. axi_araddr is reset to zero on reset assertion.

always @( posedge S_AXI_ACLK )
begin
  if ( S_AXI_ARESETN == 1'b0 )
    begin
      axi_arready <= 1'b0;
      axi_araddr  <= 32'b0;
    end
  else
    begin
      if (~axi_arready && S_AXI_ARVALID)
        begin
          // indicates that the slave has acceped the valid read address
          axi_arready <= 1'b1;
          // Read address latching
          axi_araddr  <= S_AXI_ARADDR;
        end
      else
        begin
          axi_arready <= 1'b0;
        end
    end
end

// Implement axi_arvalid generation
// axi_rvalid is asserted for one S_AXI_ACLK clock cycle when both
// S_AXI_ARVALID and axi_arready are asserted. The slave registers
// data are available on the axi_rdata bus at this instance. The
// assertion of axi_rvalid marks the validity of read data on the
// bus and axi_rresp indicates the status of read transaction.axi_rvalid
// is deasserted on reset (active low). axi_rresp and axi_rdata are
// cleared to zero on reset (active low).
always @( posedge S_AXI_ACLK )
begin
  if ( S_AXI_ARESETN == 1'b0 )
    begin
      axi_rvalid <= 0;
      axi_rresp  <= 0;
    end
  else
    begin
      if (axi_arready && S_AXI_ARVALID && ~axi_rvalid)
        begin
          // Valid read data is available at the read data bus
          axi_rvalid <= 1'b1;
          axi_rresp  <= 2'b0; // 'OKAY' response
        end
      else if (axi_rvalid && S_AXI_RREADY)
        begin
          // Read data is accepted by the master
          axi_rvalid <= 1'b0;
        end
    end
end

// Implement memory mapped register select and read logic generation
// Slave register read enable is asserted when valid address is available
// and the slave is ready to accept the read address.
assign slv_reg_rden = axi_arready & S_AXI_ARVALID & ~axi_rvalid;
always @(*)
begin
      // Address decoding for reading registers
      case ( axi_araddr[ADDR_LSB+OPT_MEM_ADDR_BITS:ADDR_LSB] )
        8'h0    : reg_data_out <= io_array[0];
        8'h1    : reg_data_out <= io_array[1];
        8'h2    : reg_data_out <= io_array[2];
        8'h3    : reg_data_out <= io_array[3];
        8'h4    : reg_data_out <= io_array[4];
        8'h5    : reg_data_out <= io_array[5];
        8'h6    : reg_data_out <= io_array[6];
        8'h7    : reg_data_out <= io_array[7];
        8'h8    : reg_data_out <= io_array[8];
        8'h9    : reg_data_out <= io_array[9];
        8'hA    : reg_data_out <= io_array[10];
        8'hB    : reg_data_out <= io_array[11];
        8'hC    : reg_data_out <= io_array[12];
        8'hD    : reg_data_out <= io_array[13];
        8'hE    : reg_data_out <= io_array[14];
        8'hF    : reg_data_out <= io_array[15];
        8'h10   : reg_data_out <= io_array[16];
        8'h11   : reg_data_out <= io_array[17];
        8'h12   : reg_data_out <= io_array[18];
        8'h13   : reg_data_out <= io_array[19];
        8'h14   : reg_data_out <= io_array_out[0];
        8'h15   : reg_data_out <= io_array_out[1];
        8'h16   : reg_data_out <= io_array_out[2];
        8'h17   : reg_data_out <= io_array_out[3];
        8'h18   : reg_data_out <= io_array_out[4];
        8'h19   : reg_data_out <= io_array_out[5];
        8'h1A   : reg_data_out <= io_array_out[6];
        8'h1B   : reg_data_out <= io_array_out[7];
        8'h1C   : reg_data_out <= io_array_out[8];
        8'h1D   : reg_data_out <= io_array_out[9];
        8'h1E   : reg_data_out <= io_array_out[10];
        8'h1F   : reg_data_out <= io_array_out[11];
        8'h20   : reg_data_out <= io_array_out[12];
        8'h21   : reg_data_out <= io_array_out[13];
        8'h22   : reg_data_out <= io_array_out[14];
        8'h23   : reg_data_out <= io_array_out[15];
        8'h24   : reg_data_out <= io_array_out[16];
        8'h25   : reg_data_out <= io_array_out[17];
        8'h26   : reg_data_out <= io_array_out[18];
        8'h27   : reg_data_out <= io_array_out[19];
        8'h28   : reg_data_out <= io_valid_reg;
        8'h29   : reg_data_out <= {31'h0, io_ready};
        default : reg_data_out <= 0;
      endcase
end

// Output register or memory read data
always @( posedge S_AXI_ACLK )
begin
  if ( S_AXI_ARESETN == 1'b0 )
    begin
      axi_rdata  <= 0;
    end
  else
    begin
      // When there is a valid read address (S_AXI_ARVALID) with
      // acceptance of read address by the slave (axi_arready),
      // output the read dada
      if (slv_reg_rden)
        begin
          axi_rdata <= reg_data_out;     // register read data
        end
    end
end

wire io_ready;

always @(posedge S_AXI_ACLK or negedge S_AXI_ARESETN) begin
    if(~S_AXI_ARESETN) begin
        io_ready_reg <= 0;
    end
    else begin
        io_ready_reg <= io_ready ? 1'b1 : io_ready_reg;
    end
end

insertSort u_insertSort(
	.clock(S_AXI_ACLK),
	.reset(~S_AXI_ARESETN),
	.io_valid(io_valid_reg[0]),
	.io_ready(io_ready),
	.io_array_0(io_array[0]),
	.io_array_1(io_array[1]),
	.io_array_2(io_array[2]),
	.io_array_3(io_array[3]),
	.io_array_4(io_array[4]),
	.io_array_5(io_array[5]),
	.io_array_6(io_array[6]),
	.io_array_7(io_array[7]),
	.io_array_8(io_array[8]),
	.io_array_9(io_array[9]),
	.io_array_10(io_array[10]),
	.io_array_11(io_array[11]),
	.io_array_12(io_array[12]),
	.io_array_13(io_array[13]),
	.io_array_14(io_array[14]),
	.io_array_15(io_array[15]),
	.io_array_16(io_array[16]),
	.io_array_17(io_array[17]),
	.io_array_18(io_array[18]),
	.io_array_19(io_array[19]),
	.io_array_out_0(io_array_out[0]),
	.io_array_out_1(io_array_out[1]),
	.io_array_out_2(io_array_out[2]),
	.io_array_out_3(io_array_out[3]),
	.io_array_out_4(io_array_out[4]),
	.io_array_out_5(io_array_out[5]),
	.io_array_out_6(io_array_out[6]),
	.io_array_out_7(io_array_out[7]),
	.io_array_out_8(io_array_out[8]),
	.io_array_out_9(io_array_out[9]),
	.io_array_out_10(io_array_out[10]),
	.io_array_out_11(io_array_out[11]),
	.io_array_out_12(io_array_out[12]),
	.io_array_out_13(io_array_out[13]),
	.io_array_out_14(io_array_out[14]),
	.io_array_out_15(io_array_out[15]),
	.io_array_out_16(io_array_out[16]),
	.io_array_out_17(io_array_out[17]),
	.io_array_out_18(io_array_out[18]),
	.io_array_out_19(io_array_out[19])
);

endmodule
