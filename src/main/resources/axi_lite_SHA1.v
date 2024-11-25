module AXILiteSHA1 #(
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
reg [31:0] io_bytes [19:0];
wire [31:0] io_out_digest [19:0];
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
                io_bytes[0][(byte_index*8) +: 8] <= S_AXI_WDATA[(byte_index*8) +: 8];
              end
          8'h1:
            for ( byte_index = 0; byte_index <= (C_S_AXI_DATA_WIDTH/8)-1; byte_index = byte_index+1 )
              if ( S_AXI_WSTRB[byte_index] == 1 ) begin
                // Respective byte enables are asserted as per write strobes
                // Slave register 1
                io_bytes[1][(byte_index*8) +: 8] <= S_AXI_WDATA[(byte_index*8) +: 8];
              end
          8'h2:
            for ( byte_index = 0; byte_index <= (C_S_AXI_DATA_WIDTH/8)-1; byte_index = byte_index+1 )
              if ( S_AXI_WSTRB[byte_index] == 1 ) begin
                // Respective byte enables are asserted as per write strobes
                // Slave register 2
                io_bytes[2][(byte_index*8) +: 8] <= S_AXI_WDATA[(byte_index*8) +: 8];
              end
          8'h3:
            for ( byte_index = 0; byte_index <= (C_S_AXI_DATA_WIDTH/8)-1; byte_index = byte_index+1 )
              if ( S_AXI_WSTRB[byte_index] == 1 ) begin
                // Respective byte enables are asserted as per write strobes
                // Slave register 3
                io_bytes[3][(byte_index*8) +: 8] <= S_AXI_WDATA[(byte_index*8) +: 8];
              end
          8'h4:
            for ( byte_index = 0; byte_index <= (C_S_AXI_DATA_WIDTH/8)-1; byte_index = byte_index+1 )
              if ( S_AXI_WSTRB[byte_index] == 1 ) begin
                // Respective byte enables are asserted as per write strobes
                // Slave register 3
                io_bytes[4][(byte_index*8) +: 8] <= S_AXI_WDATA[(byte_index*8) +: 8];
              end
          8'h5:
            for ( byte_index = 0; byte_index <= (C_S_AXI_DATA_WIDTH/8)-1; byte_index = byte_index+1 )
              if ( S_AXI_WSTRB[byte_index] == 1 ) begin
                // Respective byte enables are asserted as per write strobes
                // Slave register 3
                io_bytes[5][(byte_index*8) +: 8] <= S_AXI_WDATA[(byte_index*8) +: 8];
              end
          8'h6:
            for ( byte_index = 0; byte_index <= (C_S_AXI_DATA_WIDTH/8)-1; byte_index = byte_index+1 )
              if ( S_AXI_WSTRB[byte_index] == 1 ) begin
                // Respective byte enables are asserted as per write strobes
                // Slave register 3
                io_bytes[6][(byte_index*8) +: 8] <= S_AXI_WDATA[(byte_index*8) +: 8];
              end
          8'h7:
            for ( byte_index = 0; byte_index <= (C_S_AXI_DATA_WIDTH/8)-1; byte_index = byte_index+1 )
              if ( S_AXI_WSTRB[byte_index] == 1 ) begin
                // Respective byte enables are asserted as per write strobes
                // Slave register 3
                io_bytes[7][(byte_index*8) +: 8] <= S_AXI_WDATA[(byte_index*8) +: 8];
              end
          8'h8:
            for ( byte_index = 0; byte_index <= (C_S_AXI_DATA_WIDTH/8)-1; byte_index = byte_index+1 )
              if ( S_AXI_WSTRB[byte_index] == 1 ) begin
                // Respective byte enables are asserted as per write strobes
                // Slave register 3
                io_bytes[8][(byte_index*8) +: 8] <= S_AXI_WDATA[(byte_index*8) +: 8];
              end
          8'h9:
            for ( byte_index = 0; byte_index <= (C_S_AXI_DATA_WIDTH/8)-1; byte_index = byte_index+1 )
              if ( S_AXI_WSTRB[byte_index] == 1 ) begin
                // Respective byte enables are asserted as per write strobes
                // Slave register 3
                io_bytes[9][(byte_index*8) +: 8] <= S_AXI_WDATA[(byte_index*8) +: 8];
              end
          8'hA:
            for ( byte_index = 0; byte_index <= (C_S_AXI_DATA_WIDTH/8)-1; byte_index = byte_index+1 )
              if ( S_AXI_WSTRB[byte_index] == 1 ) begin
                // Respective byte enables are asserted as per write strobes
                // Slave register 3
                io_bytes[10][(byte_index*8) +: 8] <= S_AXI_WDATA[(byte_index*8) +: 8];
              end
          8'hB:
            for ( byte_index = 0; byte_index <= (C_S_AXI_DATA_WIDTH/8)-1; byte_index = byte_index+1 )
              if ( S_AXI_WSTRB[byte_index] == 1 ) begin
                // Respective byte enables are asserted as per write strobes
                // Slave register 3
                io_bytes[11][(byte_index*8) +: 8] <= S_AXI_WDATA[(byte_index*8) +: 8];
              end
          8'hC:
            for ( byte_index = 0; byte_index <= (C_S_AXI_DATA_WIDTH/8)-1; byte_index = byte_index+1 )
              if ( S_AXI_WSTRB[byte_index] == 1 ) begin
                // Respective byte enables are asserted as per write strobes
                // Slave register 3
                io_bytes[12][(byte_index*8) +: 8] <= S_AXI_WDATA[(byte_index*8) +: 8];
              end
          8'hD:
            for ( byte_index = 0; byte_index <= (C_S_AXI_DATA_WIDTH/8)-1; byte_index = byte_index+1 )
              if ( S_AXI_WSTRB[byte_index] == 1 ) begin
                // Respective byte enables are asserted as per write strobes
                // Slave register 3
                io_bytes[13][(byte_index*8) +: 8] <= S_AXI_WDATA[(byte_index*8) +: 8];
              end
          8'hE:
            for ( byte_index = 0; byte_index <= (C_S_AXI_DATA_WIDTH/8)-1; byte_index = byte_index+1 )
              if ( S_AXI_WSTRB[byte_index] == 1 ) begin
                // Respective byte enables are asserted as per write strobes
                // Slave register 3
                io_bytes[14][(byte_index*8) +: 8] <= S_AXI_WDATA[(byte_index*8) +: 8];
              end
          8'hF:
            for ( byte_index = 0; byte_index <= (C_S_AXI_DATA_WIDTH/8)-1; byte_index = byte_index+1 )
              if ( S_AXI_WSTRB[byte_index] == 1 ) begin
                // Respective byte enables are asserted as per write strobes
                // Slave register 3
                io_bytes[15][(byte_index*8) +: 8] <= S_AXI_WDATA[(byte_index*8) +: 8];
              end
          8'h10:
            for ( byte_index = 0; byte_index <= (C_S_AXI_DATA_WIDTH/8)-1; byte_index = byte_index+1 )
              if ( S_AXI_WSTRB[byte_index] == 1 ) begin
                // Respective byte enables are asserted as per write strobes
                // Slave register 3
                io_bytes[16][(byte_index*8) +: 8] <= S_AXI_WDATA[(byte_index*8) +: 8];
              end
          8'h11:
            for ( byte_index = 0; byte_index <= (C_S_AXI_DATA_WIDTH/8)-1; byte_index = byte_index+1 )
              if ( S_AXI_WSTRB[byte_index] == 1 ) begin
                // Respective byte enables are asserted as per write strobes
                // Slave register 3
                io_bytes[17][(byte_index*8) +: 8] <= S_AXI_WDATA[(byte_index*8) +: 8];
              end
          8'h12:
            for ( byte_index = 0; byte_index <= (C_S_AXI_DATA_WIDTH/8)-1; byte_index = byte_index+1 )
              if ( S_AXI_WSTRB[byte_index] == 1 ) begin
                // Respective byte enables are asserted as per write strobes
                // Slave register 3
                io_bytes[18][(byte_index*8) +: 8] <= S_AXI_WDATA[(byte_index*8) +: 8];
              end
          8'h13:
            for ( byte_index = 0; byte_index <= (C_S_AXI_DATA_WIDTH/8)-1; byte_index = byte_index+1 )
              if ( S_AXI_WSTRB[byte_index] == 1 ) begin
                // Respective byte enables are asserted as per write strobes
                // Slave register 3
                io_bytes[19][(byte_index*8) +: 8] <= S_AXI_WDATA[(byte_index*8) +: 8];
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
        8'h0    : reg_data_out <= io_bytes[0];
        8'h1    : reg_data_out <= io_bytes[1];
        8'h2    : reg_data_out <= io_bytes[2];
        8'h3    : reg_data_out <= io_bytes[3];
        8'h4    : reg_data_out <= io_bytes[4];
        8'h5    : reg_data_out <= io_bytes[5];
        8'h6    : reg_data_out <= io_bytes[6];
        8'h7    : reg_data_out <= io_bytes[7];
        8'h8    : reg_data_out <= io_bytes[8];
        8'h9    : reg_data_out <= io_bytes[9];
        8'hA    : reg_data_out <= io_bytes[10];
        8'hB    : reg_data_out <= io_bytes[11];
        8'hC    : reg_data_out <= io_bytes[12];
        8'hD    : reg_data_out <= io_bytes[13];
        8'hE    : reg_data_out <= io_bytes[14];
        8'hF    : reg_data_out <= io_bytes[15];
        8'h10   : reg_data_out <= io_bytes[16];
        8'h11   : reg_data_out <= io_bytes[17];
        8'h12   : reg_data_out <= io_bytes[18];
        8'h13   : reg_data_out <= io_bytes[19];
        8'h14   : reg_data_out <= io_out_digest[0];
        8'h15   : reg_data_out <= io_out_digest[1];
        8'h16   : reg_data_out <= io_out_digest[2];
        8'h17   : reg_data_out <= io_out_digest[3];
        8'h18   : reg_data_out <= io_out_digest[4];
        8'h19   : reg_data_out <= io_out_digest[5];
        8'h1A   : reg_data_out <= io_out_digest[6];
        8'h1B   : reg_data_out <= io_out_digest[7];
        8'h1C   : reg_data_out <= io_out_digest[8];
        8'h1D   : reg_data_out <= io_out_digest[9];
        8'h1E   : reg_data_out <= io_out_digest[10];
        8'h1F   : reg_data_out <= io_out_digest[11];
        8'h20   : reg_data_out <= io_out_digest[12];
        8'h21   : reg_data_out <= io_out_digest[13];
        8'h22   : reg_data_out <= io_out_digest[14];
        8'h23   : reg_data_out <= io_out_digest[15];
        8'h24   : reg_data_out <= io_out_digest[16];
        8'h25   : reg_data_out <= io_out_digest[17];
        8'h26   : reg_data_out <= io_out_digest[18];
        8'h27   : reg_data_out <= io_out_digest[19];
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
        io_ready_reg <= io_ready && io_valid_reg[0] ? 1'b1 : io_ready_reg;
    end
end

digest u_digest(
	.clock(S_AXI_ACLK),
	.reset(~S_AXI_ARESETN),
	.io_valid(io_valid_reg[0]),
    .io_ready(io_ready),
	.io_bytes_0(io_bytes[0]),
	.io_bytes_1(io_bytes[1]),
	.io_bytes_2(io_bytes[2]),
	.io_bytes_3(io_bytes[3]),
	.io_bytes_4(io_bytes[4]),
	.io_bytes_5(io_bytes[5]),
	.io_bytes_6(io_bytes[6]),
	.io_bytes_7(io_bytes[7]),
	.io_bytes_8(io_bytes[8]),
	.io_bytes_9(io_bytes[9]),
	.io_bytes_10(io_bytes[10]),
	.io_bytes_11(io_bytes[11]),
	.io_bytes_12(io_bytes[12]),
	.io_bytes_13(io_bytes[13]),
	.io_bytes_14(io_bytes[14]),
	.io_bytes_15(io_bytes[15]),
	.io_bytes_16(io_bytes[16]),
	.io_bytes_17(io_bytes[17]),
	.io_bytes_18(io_bytes[18]),
	.io_bytes_19(io_bytes[19]),
	.io_bytes_20(),
	.io_bytes_21(),
	.io_bytes_22(),
	.io_bytes_23(),
	.io_bytes_24(),
	.io_bytes_25(),
	.io_bytes_26(),
	.io_bytes_27(),
	.io_bytes_28(),
	.io_bytes_29(),
	.io_bytes_30(),
	.io_bytes_31(),
	.io_bytes_32(),
	.io_bytes_33(),
	.io_bytes_34(),
	.io_bytes_35(),
	.io_bytes_36(),
	.io_bytes_37(),
	.io_bytes_38(),
	.io_bytes_39(),
	.io_bytes_40(),
	.io_bytes_41(),
	.io_bytes_42(),
	.io_bytes_43(),
	.io_bytes_44(),
	.io_bytes_45(),
	.io_bytes_46(),
	.io_bytes_47(),
	.io_bytes_48(),
	.io_bytes_49(),
	.io_bytes_50(),
	.io_bytes_51(),
	.io_bytes_52(),
	.io_bytes_53(),
	.io_bytes_54(),
	.io_bytes_55(),
	.io_bytes_56(),
	.io_bytes_57(),
	.io_bytes_58(),
	.io_bytes_59(),
	.io_bytes_60(),
	.io_bytes_61(),
	.io_bytes_62(),
	.io_bytes_63(),
	.io_bytes_64(),
	.io_bytes_65(),
	.io_bytes_66(),
	.io_bytes_67(),
	.io_bytes_68(),
	.io_bytes_69(),
	.io_bytes_70(),
	.io_bytes_71(),
	.io_bytes_72(),
	.io_bytes_73(),
	.io_bytes_74(),
	.io_bytes_75(),
	.io_bytes_76(),
	.io_bytes_77(),
	.io_bytes_78(),
	.io_bytes_79(),
	.io_bytes_out_0(),
	.io_bytes_out_1(),
	.io_bytes_out_2(),
	.io_bytes_out_3(),
	.io_bytes_out_4(),
	.io_bytes_out_5(),
	.io_bytes_out_6(),
	.io_bytes_out_7(),
	.io_bytes_out_8(),
	.io_bytes_out_9(),
	.io_bytes_out_10(),
	.io_bytes_out_11(),
	.io_bytes_out_12(),
	.io_bytes_out_13(),
	.io_bytes_out_14(),
	.io_bytes_out_15(),
	.io_bytes_out_16(),
	.io_bytes_out_17(),
	.io_bytes_out_18(),
	.io_bytes_out_19(),
	.io_bytes_out_20(),
	.io_bytes_out_21(),
	.io_bytes_out_22(),
	.io_bytes_out_23(),
	.io_bytes_out_24(),
	.io_bytes_out_25(),
	.io_bytes_out_26(),
	.io_bytes_out_27(),
	.io_bytes_out_28(),
	.io_bytes_out_29(),
	.io_bytes_out_30(),
	.io_bytes_out_31(),
	.io_bytes_out_32(),
	.io_bytes_out_33(),
	.io_bytes_out_34(),
	.io_bytes_out_35(),
	.io_bytes_out_36(),
	.io_bytes_out_37(),
	.io_bytes_out_38(),
	.io_bytes_out_39(),
	.io_bytes_out_40(),
	.io_bytes_out_41(),
	.io_bytes_out_42(),
	.io_bytes_out_43(),
	.io_bytes_out_44(),
	.io_bytes_out_45(),
	.io_bytes_out_46(),
	.io_bytes_out_47(),
	.io_bytes_out_48(),
	.io_bytes_out_49(),
	.io_bytes_out_50(),
	.io_bytes_out_51(),
	.io_bytes_out_52(),
	.io_bytes_out_53(),
	.io_bytes_out_54(),
	.io_bytes_out_55(),
	.io_bytes_out_56(),
	.io_bytes_out_57(),
	.io_bytes_out_58(),
	.io_bytes_out_59(),
	.io_bytes_out_60(),
	.io_bytes_out_61(),
	.io_bytes_out_62(),
	.io_bytes_out_63(),
	.io_bytes_out_64(),
	.io_bytes_out_65(),
	.io_bytes_out_66(),
	.io_bytes_out_67(),
	.io_bytes_out_68(),
	.io_bytes_out_69(),
	.io_bytes_out_70(),
	.io_bytes_out_71(),
	.io_bytes_out_72(),
	.io_bytes_out_73(),
	.io_bytes_out_74(),
	.io_bytes_out_75(),
	.io_bytes_out_76(),
	.io_bytes_out_77(),
	.io_bytes_out_78(),
	.io_bytes_out_79(),
	.io_out_digest_0(io_out_digest[0]),
	.io_out_digest_1(io_out_digest[1]),
	.io_out_digest_2(io_out_digest[2]),
	.io_out_digest_3(io_out_digest[3]),
	.io_out_digest_4(io_out_digest[4]),
	.io_out_digest_5(io_out_digest[5]),
	.io_out_digest_6(io_out_digest[6]),
	.io_out_digest_7(io_out_digest[7]),
	.io_out_digest_8(io_out_digest[8]),
	.io_out_digest_9(io_out_digest[9]),
	.io_out_digest_10(io_out_digest[10]),
	.io_out_digest_11(io_out_digest[11]),
	.io_out_digest_12(io_out_digest[12]),
	.io_out_digest_13(io_out_digest[13]),
	.io_out_digest_14(io_out_digest[14]),
	.io_out_digest_15(io_out_digest[15]),
	.io_out_digest_16(io_out_digest[16]),
	.io_out_digest_17(io_out_digest[17]),
	.io_out_digest_18(io_out_digest[18]),
	.io_out_digest_19(io_out_digest[19]),
	.io_out_digest_20(),
	.io_out_digest_21(),
	.io_out_digest_22(),
	.io_out_digest_23(),
	.io_out_digest_24(),
	.io_out_digest_25(),
	.io_out_digest_26(),
	.io_out_digest_27(),
	.io_out_digest_28(),
	.io_out_digest_29(),
	.io_out_digest_30(),
	.io_out_digest_31(),
	.io_out_digest_32(),
	.io_out_digest_33(),
	.io_out_digest_34(),
	.io_out_digest_35(),
	.io_out_digest_36(),
	.io_out_digest_37(),
	.io_out_digest_38(),
	.io_out_digest_39(),
	.io_out_digest_40(),
	.io_out_digest_41(),
	.io_out_digest_42(),
	.io_out_digest_43(),
	.io_out_digest_44(),
	.io_out_digest_45(),
	.io_out_digest_46(),
	.io_out_digest_47(),
	.io_out_digest_48(),
	.io_out_digest_49(),
	.io_out_digest_50(),
	.io_out_digest_51(),
	.io_out_digest_52(),
	.io_out_digest_53(),
	.io_out_digest_54(),
	.io_out_digest_55(),
	.io_out_digest_56(),
	.io_out_digest_57(),
	.io_out_digest_58(),
	.io_out_digest_59(),
	.io_out_digest_60(),
	.io_out_digest_61(),
	.io_out_digest_62(),
	.io_out_digest_63(),
	.io_out_digest_64(),
	.io_out_digest_65(),
	.io_out_digest_66(),
	.io_out_digest_67(),
	.io_out_digest_68(),
	.io_out_digest_69(),
	.io_out_digest_70(),
	.io_out_digest_71(),
	.io_out_digest_72(),
	.io_out_digest_73(),
	.io_out_digest_74(),
	.io_out_digest_75(),
	.io_out_digest_76(),
	.io_out_digest_77(),
	.io_out_digest_78(),
	.io_out_digest_79()
);

endmodule
