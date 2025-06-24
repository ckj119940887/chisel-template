module AXI_Full_Slave_Module #
(
	parameter integer C_S_AXI_ID_WIDTH	    = 1,
	parameter integer C_S_AXI_DATA_WIDTH	  = 32,
	parameter integer C_S_AXI_ADDR_WIDTH	  = 6,
	parameter integer C_S_AXI_AWUSER_WIDTH	= 0,
	parameter integer C_S_AXI_ARUSER_WIDTH	= 0,
	parameter integer C_S_AXI_WUSER_WIDTH	  = 0,
	parameter integer C_S_AXI_RUSER_WIDTH	  = 0,
	parameter integer C_S_AXI_BUSER_WIDTH	  = 0
)
(
  input  wire                                 S_AXI_ACLK          ,
  input  wire                                 S_AXI_ARESETN       ,

  input  wire [C_S_AXI_ID_WIDTH-1 : 0]        S_AXI_AWID          ,
  input  wire [C_S_AXI_ADDR_WIDTH-1 : 0]      S_AXI_AWADDR        ,
  input  wire [7 : 0]                         S_AXI_AWLEN         ,
  input  wire [2 : 0]                         S_AXI_AWSIZE        ,
  input  wire [1 : 0]                         S_AXI_AWBURST       ,
  input  wire                                 S_AXI_AWLOCK        ,
  input  wire [3 : 0]                         S_AXI_AWCACHE       ,
  input  wire [2 : 0]                         S_AXI_AWPROT        ,
  input  wire [3 : 0]                         S_AXI_AWQOS         ,
  input  wire [3 : 0]                         S_AXI_AWREGION      ,
  input  wire [C_S_AXI_AWUSER_WIDTH-1 : 0]    S_AXI_AWUSER        ,
  input  wire                                 S_AXI_AWVALID       ,
  output wire                                 S_AXI_AWREADY       ,
  
  input  wire [C_S_AXI_DATA_WIDTH-1 : 0]      S_AXI_WDATA         ,
  input  wire [(C_S_AXI_DATA_WIDTH/8)-1 : 0]  S_AXI_WSTRB         ,
  input  wire                                 S_AXI_WLAST         ,
  input  wire [C_S_AXI_WUSER_WIDTH-1 : 0]     S_AXI_WUSER         ,
  input  wire                                 S_AXI_WVALID        ,
  output wire                                 S_AXI_WREADY        ,
  
  output wire [C_S_AXI_ID_WIDTH-1 : 0]        S_AXI_BID           ,
  output wire [1 : 0]                         S_AXI_BRESP         ,
  output wire [C_S_AXI_BUSER_WIDTH-1 : 0]     S_AXI_BUSER         ,
  output wire                                 S_AXI_BVALID        ,
  input  wire                                 S_AXI_BREADY        ,
  
  input  wire [C_S_AXI_ID_WIDTH-1 : 0]        S_AXI_ARID          ,
  input  wire [C_S_AXI_ADDR_WIDTH-1 : 0]      S_AXI_ARADDR        ,
  input  wire [7 : 0]                         S_AXI_ARLEN         ,
  input  wire [2 : 0]                         S_AXI_ARSIZE        ,
  input  wire [1 : 0]                         S_AXI_ARBURST       ,
  input  wire                                 S_AXI_ARLOCK        ,
  input  wire [3 : 0]                         S_AXI_ARCACHE       ,
  input  wire [2 : 0]                         S_AXI_ARPROT        ,
  input  wire [3 : 0]                         S_AXI_ARQOS         ,
  input  wire [3 : 0]                         S_AXI_ARREGION      ,
  input  wire [C_S_AXI_ARUSER_WIDTH-1 : 0]    S_AXI_ARUSER        ,
  input  wire                                 S_AXI_ARVALID       ,
  output wire                                 S_AXI_ARREADY       ,
  
  output wire [C_S_AXI_ID_WIDTH-1 : 0]        S_AXI_RID           ,
  output wire [C_S_AXI_DATA_WIDTH-1 : 0]      S_AXI_RDATA         ,
  output wire [1 : 0]                         S_AXI_RRESP         ,
  output wire                                 S_AXI_RLAST         ,
  output wire [C_S_AXI_RUSER_WIDTH-1 : 0]     S_AXI_RUSER         ,
  output wire                                 S_AXI_RVALID        ,
  input  wire                                 S_AXI_RREADY          
);

/**************************parameter********************************/

/**************************register ********************************/
reg                              r_s_axi_awready                    ;

reg                              r_s_axi_wready                     ;

reg                              r_s_axi_bvalid                     ;

reg                              r_s_axi_arready                    ;

reg [C_S_AXI_DATA_WIDTH-1 : 0]   r_s_axi_rdata                      ; 
reg                              r_s_axi_rlast                      ;
reg                              r_s_axi_rvalid                     ;

reg [7 : 0]                      r_read_cnt                         ;

/**************************wire*************************************/
wire                             w_system_reset                     ;
wire                             w_s_axi_rlast                      ;

/**************************state machine****************************/


/**************************combination logic************************/
assign w_system_reset = ~S_AXI_ARESETN;

assign S_AXI_AWREADY = r_s_axi_awready;     

assign S_AXI_WREADY = 1'b1;       

assign S_AXI_BID    = S_AXI_AWID; 
assign S_AXI_BRESP  = 2'b00; 
assign S_AXI_BUSER  = S_AXI_AWUSER; 
assign S_AXI_BVALID = r_s_axi_bvalid; 

assign S_AXI_ARREADY = r_s_axi_arready;

assign S_AXI_RID    = S_AXI_ARID; 
assign S_AXI_RDATA  = r_s_axi_rdata; 
assign S_AXI_RRESP  = 2'b00; 
assign S_AXI_RLAST  = (S_AXI_ARLEN == 1) ? w_s_axi_rlast : r_s_axi_rlast; 
assign S_AXI_RUSER  = S_AXI_ARUSER; 
assign S_AXI_RVALID = r_s_axi_rvalid; 

assign w_s_axi_rlast = S_AXI_RVALID & S_AXI_RREADY;
/**************************sequential logic*************************/
always @(posedge S_AXI_ACLK)
  if(w_system_reset | S_AXI_WLAST)
    r_s_axi_awready <= 'd1;
  else if (S_AXI_AWVALID & S_AXI_AWREADY)
    r_s_axi_awready <= 'd0;
  else
    r_s_axi_awready <= r_s_axi_awready;

always @(posedge S_AXI_ACLK)
  if(w_system_reset)
    r_s_axi_bvalid <= 'd0;
  else if (S_AXI_WLAST)
    r_s_axi_bvalid <= 'd1;
  else
    r_s_axi_bvalid <= 'd0;

always @(posedge S_AXI_ACLK)
  if(w_system_reset | S_AXI_RLAST)
    r_s_axi_arready <= 'd1;
  else if (S_AXI_ARVALID & S_AXI_ARREADY)
    r_s_axi_arready <= 'd0;
  else
    r_s_axi_arready <= r_s_axi_arready;

always @(posedge S_AXI_ACLK)
  if(w_system_reset | S_AXI_RLAST)
    r_s_axi_rdata <= 'd1;
  else if (S_AXI_RVALID & S_AXI_RREADY)
    r_s_axi_rdata <= r_s_axi_rdata + 'd1;
  else
    r_s_axi_rdata <= r_s_axi_rdata;

always @(posedge S_AXI_ACLK)
  if(w_system_reset)
    r_s_axi_rlast <= 'd0;
  else if(S_AXI_ARLEN == 1)
    r_s_axi_rlast <= 'd0;
  else if((S_AXI_ARLEN == 2) & (S_AXI_RVALID & S_AXI_RREADY))
    r_s_axi_rlast <= 'd1;
  else if((S_AXI_ARLEN > 2) & (r_read_cnt == S_AXI_ARLEN - 2))
    r_s_axi_rlast <= 'd1;
  else 
    r_s_axi_rlast <= 'd0;

always @(posedge S_AXI_ACLK)
  if(w_system_reset | S_AXI_RLAST)
    r_s_axi_rvalid <= 'd0;
  else if (S_AXI_ARVALID & S_AXI_ARREADY)
    r_s_axi_rvalid <= 'd1;
  else
    r_s_axi_rvalid <= r_s_axi_rvalid;

always @(posedge S_AXI_ACLK)
  if(w_system_reset | S_AXI_RLAST)
    r_read_cnt <= 'd0;
  else if (S_AXI_RVALID & S_AXI_RREADY)
    r_read_cnt <= r_read_cnt + 'd1;
  else
    r_read_cnt <= 'd0;

endmodule