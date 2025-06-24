module AXI_Full_Slave_Module_Delay #
(
	parameter integer C_S_AXI_ID_WIDTH	= 1,
	parameter integer C_S_AXI_DATA_WIDTH	= 32,
	parameter integer C_S_AXI_ADDR_WIDTH	= 6,
	parameter integer C_S_AXI_AWUSER_WIDTH	= 0,
	parameter integer C_S_AXI_ARUSER_WIDTH	= 0,
	parameter integer C_S_AXI_WUSER_WIDTH	= 0,
	parameter integer C_S_AXI_RUSER_WIDTH	= 0,
	parameter integer C_S_AXI_BUSER_WIDTH	= 0
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
parameter P_ST_READ_IDLE        = 'd0,
          P_ST_READ_WAIT_0      = 'd1,
          P_ST_READ_WAIT_1      = 'd2,
          P_ST_READ_WAIT_2      = 'd3,
          P_ST_READ_WAIT_3      = 'd4,
          P_ST_WRITE_IDLE       = 'd5,
          P_ST_WRITE_WAIT_0     = 'd6,
          P_ST_WRITE_WAIT_1     = 'd7,
          P_ST_WRITE_WAIT_2     = 'd8,
          P_ST_WRITE_WAIT_3     = 'd9;

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
reg [7:0]                        r_st_read_current_delay            ;
reg [7:0]                        r_st_read_next_delay               ;
reg [7:0]                        r_st_write_current_delay           ;
reg [7:0]                        r_st_write_next_delay              ;

/**************************combination logic************************/
assign w_system_reset = ~S_AXI_ARESETN;

assign S_AXI_AWREADY = r_s_axi_awready;     

assign S_AXI_WREADY = 'd1;       

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
  else if (r_st_write_current_delay == P_ST_WRITE_WAIT_3)
    r_s_axi_bvalid <= 'd1;
  else
    r_s_axi_bvalid <= 'd0;

/**************************************/
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
  else if (r_st_read_current_delay == P_ST_READ_WAIT_3)
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

/**************************************/
always @(posedge S_AXI_ACLK)
  if(w_system_reset)
    r_st_write_current_delay <= P_ST_WRITE_IDLE;
  else
    r_st_write_current_delay <= r_st_write_next_delay;

always @(*)
  case(r_st_write_current_delay) 
    P_ST_WRITE_IDLE    : r_st_write_next_delay <= S_AXI_WLAST                                     ? P_ST_WRITE_WAIT_0 : P_ST_WRITE_IDLE   ; 
    P_ST_WRITE_WAIT_0  : r_st_write_next_delay <= (r_st_write_current_delay == P_ST_WRITE_WAIT_0) ? P_ST_WRITE_WAIT_1 : P_ST_WRITE_WAIT_0 ; 
    P_ST_WRITE_WAIT_1  : r_st_write_next_delay <= (r_st_write_current_delay == P_ST_WRITE_WAIT_1) ? P_ST_WRITE_WAIT_2 : P_ST_WRITE_WAIT_1 ; 
    P_ST_WRITE_WAIT_2  : r_st_write_next_delay <= (r_st_write_current_delay == P_ST_WRITE_WAIT_2) ? P_ST_WRITE_WAIT_3 : P_ST_WRITE_WAIT_2 ;     
    P_ST_WRITE_WAIT_3  : r_st_write_next_delay <= (r_st_write_current_delay == P_ST_WRITE_WAIT_3) ? P_ST_WRITE_IDLE   : P_ST_WRITE_WAIT_3 ;     
    default            : r_st_write_next_delay <= P_ST_WRITE_IDLE                                                                         ;
  endcase

/**************************************/
always @(posedge S_AXI_ACLK)
  if(w_system_reset)
    r_st_read_current_delay <= P_ST_READ_IDLE;
  else
    r_st_read_current_delay <= r_st_read_next_delay;

always @(*)
  case(r_st_read_current_delay) 
    P_ST_READ_IDLE        : r_st_read_next_delay <= (S_AXI_ARVALID & S_AXI_ARREADY)               ? P_ST_READ_WAIT_0 : P_ST_READ_IDLE   ; 
    P_ST_READ_WAIT_0      : r_st_read_next_delay <= (r_st_read_current_delay == P_ST_READ_WAIT_0) ? P_ST_READ_WAIT_1 : P_ST_READ_WAIT_0 ; 
    P_ST_READ_WAIT_1      : r_st_read_next_delay <= (r_st_read_current_delay == P_ST_READ_WAIT_1) ? P_ST_READ_WAIT_2 : P_ST_READ_WAIT_1 ; 
    P_ST_READ_WAIT_2      : r_st_read_next_delay <= (r_st_read_current_delay == P_ST_READ_WAIT_2) ? P_ST_READ_WAIT_3 : P_ST_READ_WAIT_2 ;     
    P_ST_READ_WAIT_3      : r_st_read_next_delay <= S_AXI_RLAST                                   ? P_ST_READ_IDLE   : P_ST_READ_WAIT_3 ;     
    default               : r_st_read_next_delay <= P_ST_READ_IDLE                                                                      ;
  endcase

endmodule