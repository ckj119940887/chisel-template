module AXI_Lite_Slave_Module#(
	parameter integer C_S_AXI_DATA_WIDTH	= 32,
	parameter integer C_S_AXI_ADDR_WIDTH	= 32
)
(
	input  wire  S_AXI_ACLK,
	input  wire  S_AXI_ARESETN,
    
	input  wire [    C_S_AXI_ADDR_WIDTH-1 : 0]   S_AXI_AWADDR   ,
    input  wire [                       2 : 0]   S_AXI_AWPROT   ,
    input  wire                                  S_AXI_AWVALID  ,
    output wire                                  S_AXI_AWREADY  ,

    input  wire [    C_S_AXI_DATA_WIDTH-1 : 0]   S_AXI_WDATA    ,
    input  wire [(C_S_AXI_DATA_WIDTH/8)-1 : 0]   S_AXI_WSTRB    ,
    input  wire                                  S_AXI_WVALID   ,
    output wire                                  S_AXI_WREADY   ,

    output wire [                       1 : 0]   S_AXI_BRESP    ,
    output wire                                  S_AXI_BVALID   ,
    input  wire                                  S_AXI_BREADY   ,

    input  wire [    C_S_AXI_ADDR_WIDTH-1 : 0]   S_AXI_ARADDR   ,
    input  wire [                       2 : 0]   S_AXI_ARPROT   ,
    input  wire                                  S_AXI_ARVALID  ,
    output wire                                  S_AXI_ARREADY  ,

    output wire [    C_S_AXI_DATA_WIDTH-1 : 0]   S_AXI_RDATA    ,
    output wire [                       1 : 0]   S_AXI_RRESP    ,
    output wire                                  S_AXI_RVALID   ,
    input  wire                                  S_AXI_RREADY     
);

/**************************register*********************************/
reg [  C_S_AXI_ADDR_WIDTH-1 : 0] r_s_axi_awaddr                     ;
reg                              r_s_axi_awready                    ;

reg                              r_s_axi_wready                     ;

reg [                     1 : 0] r_s_axi_bresp                      ;
reg                              r_s_axi_needresp                   ;
reg                              r_s_axi_bvalid                     ;

reg [  C_S_AXI_ADDR_WIDTH-1 : 0] r_s_axi_araddr                     ;
reg                              r_s_axi_arready                    ;

reg                              r_s_axi_needread                   ;
reg                              r_s_axi_rvalid                     ;
reg [                     1 : 0] r_s_axi_rresp                      ;
reg [C_M_AXI_DATA_WIDTH - 1 : 0] r_s_axi_rdata                      ;

/**************************combinational logic**********************/
assign S_AXI_AWREADY = r_s_axi_awready        ;

assign S_AXI_WREADY  = r_s_axi_wready         ;

assign S_AXI_BRESP   = r_s_axi_bresp          ;
assign S_AXI_BVALID  = r_s_axi_bvalid         ;

assign S_AXI_ARREADY = r_s_axi_arready        ;

assign S_AXI_RVALID  = r_s_axi_rvalid         ;
assign S_AXI_RRESP   = r_s_axi_rresp          ;
assign S_AXI_RDATA   = r_s_axi_rdata          ;

/**************************sequential logic*************************/
// write addr channel
always @(posedge S_AXI_ACLK or negedge S_AXI_ARESETN) begin
    if(~S_AXI_ARESETN) begin
        r_s_axi_awready <= 1'b0;
    end
    else begin
        if(S_AXI_AWVALID) begin
            r_s_axi_awready <= 1'b1;
        end
        else begin
            r_s_axi_awready <= 1'b0;
        end
    end
end

always @(posedge S_AXI_ACLK or negedge S_AXI_ARESETN) begin
    if(~S_AXI_ARESETN) begin
        r_s_axi_awaddr <= 'b0;
    end
    else begin
        if(S_AXI_AWVALID & S_AXI_AWREADY) begin
            r_s_axi_awaddr <= S_AXI_AWADDR;
        end
    end
end

// write data channel
always @(posedge S_AXI_ACLK or negedge S_AXI_ARESETN) begin
    if(~S_AXI_ARESETN) begin
        r_s_axi_wready <= 1'b0;
    end
    else begin
        if(S_AXI_WVALID) begin
            r_s_axi_wready <= 1'b1;
        end
        else begin
            r_s_axi_wready <= 1'b0;
        end
    end
end

// write response channel
always @(posedge S_AXI_ACLK or negedge S_AXI_ARESETN) begin
    if(~S_AXI_ARESETN) begin
        r_s_axi_needresp <= 1'b0;
    end
    else begin
        if(S_AXI_WVALID & S_AXI_WREADY) begin
            r_s_axi_needresp <= 1'b1;
        end
        else begin
            r_s_axi_needresp <= 1'b0;
        end
    end
end

always @(posedge S_AXI_ACLK or negedge S_AXI_ARESETN) begin
    if(~S_AXI_ARESETN) begin
        r_s_axi_bvalid <= 1'b0;
    end
    else begin
        if(r_s_axi_needresp) begin
            r_s_axi_bvalid <= 1'b1;
        end

        if(S_AXI_BVALID & S_AXI_BREADY) begin
            r_s_axi_bvalid <= 1'b0;
        end
    end
end

// read addresss channel 
always @(posedge S_AXI_ACLK or negedge S_AXI_ARESETN) begin
    if(~S_AXI_ARESETN) begin
        r_s_axi_arready <= 1'b0;
    end
    else begin
        if(S_AXI_ARVALID) begin
            r_s_axi_arready <= 1'b1;
        end
        else begin
            r_s_axi_arready <= 1'b0;
        end
    end
end

always @(posedge S_AXI_ACLK or negedge S_AXI_ARESETN) begin
    if(~S_AXI_ARESETN) begin
        r_s_axi_needread <= 1'b0;
        r_s_axi_araddr <= 'b0;
    end
    else begin
        if(S_AXI_ARVALID & S_AXI_ARREADY) begin
            r_s_axi_araddr <= S_AXI_ARADDR;
            r_s_axi_needread <= 1'b1;
        end
        else begin
            r_s_axi_needread <= 1'b0;
        end
    end
end

// read data channel
always @(posedge S_AXI_ACLK or negedge S_AXI_ARESETN) begin
    if(~S_AXI_ARESETN) begin
        r_s_axi_rdata <= 'b0;
        r_s_axi_rvalid <= 1'b0;
    end
    else begin
        if(r_s_axi_needread & S_AXI_RREADY) begin
            // ready signal is asserted, read now
            r_s_axi_rdata <= ???;
            r_s_axi_rvalid <= 1'b1;
        end
        else if(r_s_axi_needread) begin
            // ready signal is not asserted, wait now
            r_s_axi_rvalid <= 1'b0;
        end
        else begin
            r_s_axi_rvalid <= 1'b0;
        end
    end
end


endmodule