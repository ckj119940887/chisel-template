module AXI_Lite_Master_Module #(
	parameter integer C_M_AXI_DATA_WIDTH	= 32,
	parameter integer C_M_AXI_ADDR_WIDTH	= 32
)
(
	input  wire  M_AXI_ACLK,
	input  wire  M_AXI_ARESETN,
    
	output wire [    C_M_AXI_ADDR_WIDTH-1 : 0]   M_AXI_AWADDR         ,
    output wire [                       2 : 0]   M_AXI_AWPROT         ,
    output wire                                  M_AXI_AWVALID        ,
    input  wire                                  M_AXI_AWREADY        ,

    output wire [    C_M_AXI_DATA_WIDTH-1 : 0]   M_AXI_WDATA          ,
    output wire [(C_M_AXI_DATA_WIDTH/8)-1 : 0]   M_AXI_WSTRB          ,
    output wire                                  M_AXI_WVALID         ,
    input  wire                                  M_AXI_WREADY         ,

    input  wire [                       1 : 0]   M_AXI_BRESP          ,
    input  wire                                  M_AXI_BVALID         ,
    output wire                                  M_AXI_BREADY         ,

    output wire [    C_M_AXI_ADDR_WIDTH-1 : 0]   M_AXI_ARADDR         ,
    output wire [                       2 : 0]   M_AXI_ARPROT         ,
    output wire                                  M_AXI_ARVALID        ,
    input  wire                                  M_AXI_ARREADY        ,

    input  wire [    C_M_AXI_DATA_WIDTH-1 : 0]   M_AXI_RDATA          ,
    input  wire [                       1 : 0]   M_AXI_RRESP          ,
    input  wire                                  M_AXI_RVALID         ,
    output wire                                  M_AXI_RREADY         ,  

    // user port
    input  wire [    C_M_AXI_ADDR_WIDTH-1 : 0]   i_write_addr         ,
    input  wire [    C_M_AXI_DATA_WIDTH-1 : 0]   i_write_data         ,
    input  wire                                  i_write_valid        ,
    input  wire [    C_M_AXI_ADDR_WIDTH-1 : 0]   i_read_addr          ,
    input  wire                                  i_read_valid         ,
    output wire [    C_M_AXI_DATA_WIDTH-1 : 0]   o_read_data          ,
    output wire                                  o_read_data_valid    
);

/**************************wire*************************************/
wire [C_M_AXI_ADDR_WIDTH - 1 : 0]  w_fifo_waddr_dout                ;
wire                               w_fifo_waddr_full                ;
wire                               w_fifo_waddr_empty               ;
wire                               w_fifo_waddr_rden                ;

wire [C_M_AXI_DATA_WIDTH - 1 : 0]  w_fifo_wdata_dout                ;
wire                               w_fifo_wdata_full                ;
wire                               w_fifo_wdata_empty               ;
wire                               w_fifo_wdata_rden                ;

wire [C_M_AXI_ADDR_WIDTH - 1 : 0]  w_fifo_raddr_dout                ;
wire                               w_fifo_raddr_full                ;
wire                               w_fifo_raddr_empty               ;
wire                               w_fifo_rdata_rden                ;

wire                               w_AW_active                      ;
wire                               w_W_active                       ;
wire                               w_B_active                       ;
wire                               w_AR_active                      ;
wire                               w_R_active                       ;

/**************************register*********************************/
reg [  C_M_AXI_ADDR_WIDTH - 1 : 0] r_m_axi_awaddr                   ;
reg [                       2 : 0] r_m_axi_awprot                   ;
reg                                r_m_axi_awvalid                  ;

reg [  C_M_AXI_DATA_WIDTH - 1 : 0] r_m_axi_wdata                    ;
reg [(C_M_AXI_DATA_WIDTH/8-1) : 0] r_m_axi_wstrb                    ;
reg                                r_m_axi_wvalid                   ;

reg                                r_m_axi_bready                   ;

reg [  C_M_AXI_ADDR_WIDTH - 1 : 0] r_m_axi_araddr                   ;
reg [                       2 : 0] r_m_axi_arprot                   ;
reg                                r_m_axi_arvalid                  ;

reg                                r_m_axi_rready                   ;

reg [  C_M_AXI_DATA_WIDTH - 1 : 0] r_read_data                      ;
reg                                r_read_data_valid                ;

reg                                r_write_run                      ;
reg                                r_read_run                       ;

reg                                r_fifo_waddr_rden_1d             ;

/**************************combinational logic**********************/
assign M_AXI_AWADDR        = r_m_axi_awaddr;   
assign M_AXI_AWPROT        = r_m_axi_awprot; 
assign M_AXI_AWVALID       = r_m_axi_awvalid; 
assign M_AXI_WDATA         = r_m_axi_wdata; 
assign M_AXI_WSTRB         = r_m_axi_wstrb; 
assign M_AXI_WVALID        = r_m_axi_wvalid;    
assign M_AXI_BREADY        = r_m_axi_bready; 
assign M_AXI_ARADDR        = r_m_axi_araddr; 
assign M_AXI_ARPROT        = r_m_axi_arprot; 
assign M_AXI_ARVALID       = r_m_axi_arvalid;    
assign M_AXI_RREADY        = r_m_axi_rready;

assign o_read_data         = r_read_data;
assign o_read_data_valid   = r_read_data_valid;

assign w_AW_active = M_AXI_AWVALID & M_AXI_AWREADY; 
assign w_W_active  = M_AXI_WVALID & M_AXI_WREADY; 
assign w_B_active  = M_AXI_BVALID & M_AXI_BREADY; 
assign w_AR_active = M_AXI_ARVALID & M_AXI_ARREADY; 
assign w_R_active  = M_AXI_RVALID & M_AXI_RREADY; 

/**************************instance*********************************/
FIFO_32X32 u_FIFO_32X32_WADDR (
    .clk           (M_AXI_ACLK          ),
    .din           (i_write_addr        ),
    .wr_en         (i_write_valid       ),
    .rd_en         (w_fifo_waddr_rden   ),
    .dout          (w_fifo_waddr_dout   ),
    .full          (w_fifo_waddr_full   ),
    .empty         (w_fifo_waddr_empty  )
);

FIFO_32X32 u_FIFO_32X32_WDATA (
    .clk           (M_AXI_ACLK          ),
    .din           (i_write_data        ),
    .wr_en         (i_write_valid       ),
    .rd_en         (w_fifo_wdata_rden   ),
    .dout          (w_fifo_wdata_dout   ),
    .full          (w_fifo_wdata_full   ),
    .empty         (w_fifo_wdata_empty  )
);

FIFO_32X32 u_FIFO_32X32_RADDR (
    .clk           (M_AXI_ACLK          ),
    .din           (i_read_addr         ),
    .wr_en         (i_read_valid        ),
    .rd_en         (w_fifo_rdata_rden   ),
    .dout          (w_fifo_rdata_dout   ),
    .full          (w_fifo_rdata_full   ),
    .empty         (w_fifo_rdata_empty  )
);
/**************************sequential logic*************************/
always @(posedge M_AXI_ACLK, negedge M_AXI_ARESETN) begin
    if(~M_AXI_ARESETN) begin
        r_write_run <= 1'b0;
    end
    else if(w_B_active) begin
        r_write_run <= 1'b0;
    end
    else if(!w_fifo_waddr_empty) begin
        r_write_run <= 1'b1;
    end
    else begin
        r_write_run <= r_write_run;
    end
end

always @(posedge M_AXI_ACLK, negedge M_AXI_ARESETN) begin
    if(~M_AXI_ARESETN) begin
        r_fifo_waddr_rden <= 1'b0;
    end
    else if(!w_fifo_waddr_empty & !r_write_run) begin
        r_fifo_waddr_rden <= 1'b1;
    end
    else begin
        r_fifo_waddr_rden <= 1'b0;
    end
end

always @(posedge M_AXI_ACLK, negedge M_AXI_ARESETN) begin
    if(~M_AXI_ARESETN) begin
        r_fifo_wdata_rden <= 1'b0;
    end
    else if(!w_fifo_waddr_empty & !r_write_run) begin
        r_fifo_wdata_rden <= 1'b1;
    end
    else begin
        r_fifo_wdata_rden <= 1'b0;
    end
end

always @(posedge M_AXI_ACLK, negedge M_AXI_ARESETN) begin
    if(~M_AXI_ARESETN) begin
        r_fifo_waddr_rden_1d <= 'd0;
    end
    else begin
        r_fifo_waddr_rden_1d <= r_fifo_waddr_rden;
    end
end

always @(posedge M_AXI_ACLK, negedge M_AXI_ARESETN) begin
    if(~M_AXI_ARESETN) begin
        r_m_axi_awaddr  <= 'd0;
        r_m_axi_awprot  <= 'd0;
        r_m_axi_awvalid <= 'd0;
    end
    else begin

    end
end

always @(posedge M_AXI_ACLK, negedge M_AXI_ARESETN) begin
    if(~M_AXI_ARESETN) begin

    end
    else begin

    end
end

always @(posedge M_AXI_ACLK, negedge M_AXI_ARESETN) begin
    if(~M_AXI_ARESETN) begin

    end
    else begin

    end
end

always @(posedge M_AXI_ACLK, negedge M_AXI_ARESETN) begin
    if(~M_AXI_ARESETN) begin

    end
    else begin

    end
end

always @(posedge M_AXI_ACLK, negedge M_AXI_ARESETN) begin
    if(~M_AXI_ARESETN) begin

    end
    else begin

    end
end



endmodule