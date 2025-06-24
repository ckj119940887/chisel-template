module Top3(
  input wire i_sysclk ,
  input wire i_sysrstn 
);

parameter         C_M_TARGET_SLAVE_BASE_ADDR	= 32'h40000000;
parameter integer C_M_AXI_BURST_LEN             = 16;
parameter integer C_M_AXI_ID_WIDTH              = 1;
parameter integer C_M_AXI_ADDR_WIDTH	        = 32;
parameter integer C_M_AXI_DATA_WIDTH	        = 32;
parameter integer C_M_AXI_AWUSER_WIDTH	        = 0;
parameter integer C_M_AXI_ARUSER_WIDTH	        = 0;
parameter integer C_M_AXI_WUSER_WIDTH	        = 0;
parameter integer C_M_AXI_RUSER_WIDTH	        = 0;
parameter integer C_M_AXI_BUSER_WIDTH	        = 0;

wire                               M_AXI_ACLK        ;
wire                               M_AXI_ARESETN     ;

wire [C_M_AXI_ID_WIDTH-1 : 0]      M_AXI_AWID        ; 
wire [C_M_AXI_ADDR_WIDTH-1 : 0]    M_AXI_AWADDR      ; 
wire [7 : 0]                       M_AXI_AWLEN       ; 
wire [2 : 0]                       M_AXI_AWSIZE      ; 
wire [1 : 0]                       M_AXI_AWBURST     ; 
wire                               M_AXI_AWLOCK      ; 
wire [3 : 0]                       M_AXI_AWCACHE     ; 
wire [2 : 0]                       M_AXI_AWPROT      ; 
wire [3 : 0]                       M_AXI_AWQOS       ; 
wire [C_M_AXI_AWUSER_WIDTH-1 : 0]  M_AXI_AWUSER      ; 
wire                               M_AXI_AWVALID     ; 
wire                               M_AXI_AWREADY     ; 

wire [C_M_AXI_DATA_WIDTH-1 : 0]    M_AXI_WDATA       ; 
wire [C_M_AXI_DATA_WIDTH/8-1 : 0]  M_AXI_WSTRB       ; 
wire                               M_AXI_WLAST       ; 
wire [C_M_AXI_WUSER_WIDTH-1 : 0]   M_AXI_WUSER       ; 
wire                               M_AXI_WVALID      ; 
wire                               M_AXI_WREADY      ; 

wire [C_M_AXI_ID_WIDTH-1 : 0]      M_AXI_BID         ; 
wire [1 : 0]                       M_AXI_BRESP       ; 
wire [C_M_AXI_BUSER_WIDTH-1 : 0]   M_AXI_BUSER       ; 
wire                               M_AXI_BVALID      ; 
wire                               M_AXI_BREADY      ; 

wire [C_M_AXI_ID_WIDTH-1 : 0]      M_AXI_ARID        ; 
wire [C_M_AXI_ADDR_WIDTH-1 : 0]    M_AXI_ARADDR      ; 
wire [7 : 0]                       M_AXI_ARLEN       ; 
wire [2 : 0]                       M_AXI_ARSIZE      ; 
wire [1 : 0]                       M_AXI_ARBURST     ; 
wire                               M_AXI_ARLOCK      ; 
wire [3 : 0]                       M_AXI_ARCACHE     ; 
wire [2 : 0]                       M_AXI_ARPROT      ; 
wire [3 : 0]                       M_AXI_ARQOS       ; 
wire [C_M_AXI_ARUSER_WIDTH-1 : 0]  M_AXI_ARUSER      ; 
wire                               M_AXI_ARVALID     ; 
wire                               M_AXI_ARREADY     ; 

wire [C_M_AXI_ID_WIDTH-1 : 0]      M_AXI_RID         ; 
wire [C_M_AXI_DATA_WIDTH-1 : 0]    M_AXI_RDATA       ; 
wire [1 : 0]                       M_AXI_RRESP       ; 
wire                               M_AXI_RLAST       ; 
wire [C_M_AXI_RUSER_WIDTH-1 : 0]   M_AXI_RUSER       ; 
wire                               M_AXI_RVALID      ; 
wire                               M_AXI_RREADY      ;

assign M_AXI_ACLK    = i_sysclk                      ;
assign M_AXI_ARESETN = i_sysrstn                     ;

axi_full_0 u_axi_full_master(
  .m00_axi_awid        (M_AXI_AWID    ),           
  .m00_axi_awaddr      (M_AXI_AWADDR  ),       
  .m00_axi_awlen       (M_AXI_AWLEN   ),         
  .m00_axi_awsize      (M_AXI_AWSIZE  ),       
  .m00_axi_awburst     (M_AXI_AWBURST ),     
  .m00_axi_awlock      (M_AXI_AWLOCK  ),       
  .m00_axi_awcache     (M_AXI_AWCACHE ),     
  .m00_axi_awprot      (M_AXI_AWPROT  ),       
  .m00_axi_awqos       (M_AXI_AWQOS   ),         
  .m00_axi_awuser      (M_AXI_AWUSER  ),       
  .m00_axi_awvalid     (M_AXI_AWVALID ),     
  .m00_axi_awready     (M_AXI_AWREADY ),     

  .m00_axi_wdata       (M_AXI_WDATA  ),         
  .m00_axi_wstrb       (M_AXI_WSTRB  ),         
  .m00_axi_wlast       (M_AXI_WLAST  ),         
  .m00_axi_wuser       (M_AXI_WUSER  ),         
  .m00_axi_wvalid      (M_AXI_WVALID ),       
  .m00_axi_wready      (M_AXI_WREADY ),       

  .m00_axi_bid         (M_AXI_BID    ),             
  .m00_axi_bresp       (M_AXI_BRESP  ),         
  .m00_axi_buser       (M_AXI_BUSER  ),         
  .m00_axi_bvalid      (M_AXI_BVALID ),       
  .m00_axi_bready      (M_AXI_BREADY ),       

  .m00_axi_arid        (M_AXI_ARID    ),           
  .m00_axi_araddr      (M_AXI_ARADDR  ),       
  .m00_axi_arlen       (M_AXI_ARLEN   ),         
  .m00_axi_arsize      (M_AXI_ARSIZE  ),       
  .m00_axi_arburst     (M_AXI_ARBURST ),     
  .m00_axi_arlock      (M_AXI_ARLOCK  ),       
  .m00_axi_arcache     (M_AXI_ARCACHE ),     
  .m00_axi_arprot      (M_AXI_ARPROT  ),       
  .m00_axi_arqos       (M_AXI_ARQOS   ),         
  .m00_axi_aruser      (M_AXI_ARUSER  ),       
  .m00_axi_arvalid     (M_AXI_ARVALID ),     
  .m00_axi_arready     (M_AXI_ARREADY ),     

  .m00_axi_rid         (M_AXI_RID    ),             
  .m00_axi_rdata       (M_AXI_RDATA  ),        
  .m00_axi_rresp       (M_AXI_RRESP  ),        
  .m00_axi_rlast       (M_AXI_RLAST  ),        
  .m00_axi_ruser       (M_AXI_RUSER  ),        
  .m00_axi_rvalid      (M_AXI_RVALID ),       
  .m00_axi_rready      (M_AXI_RREADY ),       

  .m00_axi_aclk        (M_AXI_ACLK    ),           
  .m00_axi_aresetn     (M_AXI_ARESETN ),     

  .m00_axi_init_axi_txn(),
  .m00_axi_txn_done(),   
  .m00_axi_error(),         

  .s00_axi_awid         (),           
  .s00_axi_awaddr       (),       
  .s00_axi_awlen        (),         
  .s00_axi_awsize       (),       
  .s00_axi_awburst      (),     
  .s00_axi_awlock       (),       
  .s00_axi_awcache      (),     
  .s00_axi_awprot       (),       
  .s00_axi_awregion     (),   
  .s00_axi_awqos        (),         
  .s00_axi_awuser       (),       
  .s00_axi_awvalid      (),     
  .s00_axi_awready      (),     

  .s00_axi_wdata        (),         
  .s00_axi_wstrb        (),         
  .s00_axi_wlast        (),         
  .s00_axi_wuser        (),         
  .s00_axi_wvalid       (),       
  .s00_axi_wready       (),       

  .s00_axi_bid          (),             
  .s00_axi_bresp        (),         
  .s00_axi_buser        (),         
  .s00_axi_bvalid       (),       
  .s00_axi_bready       (),       

  .s00_axi_arid         (),           
  .s00_axi_araddr       (),       
  .s00_axi_arlen        (),         
  .s00_axi_arsize       (),       
  .s00_axi_arburst      (),     
  .s00_axi_arlock       (),       
  .s00_axi_arcache      (),     
  .s00_axi_arprot       (),       
  .s00_axi_arregion     (),   
  .s00_axi_arqos        (),         
  .s00_axi_aruser       (),       
  .s00_axi_arvalid      (),     
  .s00_axi_arready      (),     

  .s00_axi_rid          (),             
  .s00_axi_rdata        (),         
  .s00_axi_rresp        (),         
  .s00_axi_rlast        (),         
  .s00_axi_ruser        (),         
  .s00_axi_rvalid       (),       
  .s00_axi_rready       (),       

  .s00_axi_aclk         (),           
  .s00_axi_aresetn      ()      
);

AXI_Full_Slave_Module_Delay #
(
	.C_S_AXI_ID_WIDTH      (C_M_AXI_ID_WIDTH           ), 
	.C_S_AXI_DATA_WIDTH    (C_M_AXI_DATA_WIDTH	       ), 
	.C_S_AXI_ADDR_WIDTH    (C_M_AXI_ADDR_WIDTH	       ), 
	.C_S_AXI_AWUSER_WIDTH  (C_M_AXI_AWUSER_WIDTH       ), 
	.C_S_AXI_ARUSER_WIDTH  (C_M_AXI_ARUSER_WIDTH       ), 
	.C_S_AXI_WUSER_WIDTH   (C_M_AXI_WUSER_WIDTH	       ), 
	.C_S_AXI_RUSER_WIDTH   (C_M_AXI_RUSER_WIDTH	       ), 
	.C_S_AXI_BUSER_WIDTH   (C_M_AXI_BUSER_WIDTH	       ) 
) u_axi_full_slave_own(
  .S_AXI_ACLK          (M_AXI_ACLK   ),
  .S_AXI_ARESETN       (M_AXI_ARESETN),

  .S_AXI_AWID          (M_AXI_AWID    ),
  .S_AXI_AWADDR        (M_AXI_AWADDR  ),
  .S_AXI_AWLEN         (M_AXI_AWLEN   ),
  .S_AXI_AWSIZE        (M_AXI_AWSIZE  ),
  .S_AXI_AWBURST       (M_AXI_AWBURST ),
  .S_AXI_AWLOCK        (M_AXI_AWLOCK  ),
  .S_AXI_AWCACHE       (M_AXI_AWCACHE ),
  .S_AXI_AWPROT        (M_AXI_AWPROT  ),
  .S_AXI_AWQOS         (M_AXI_AWQOS   ),
  .S_AXI_AWREGION      (M_AXI_AWREGION),
  .S_AXI_AWUSER        (M_AXI_AWUSER  ),
  .S_AXI_AWVALID       (M_AXI_AWVALID ),
  .S_AXI_AWREADY       (M_AXI_AWREADY ),
  
  .S_AXI_WDATA         (M_AXI_WDATA ),
  .S_AXI_WSTRB         (M_AXI_WSTRB ),
  .S_AXI_WLAST         (M_AXI_WLAST ),
  .S_AXI_WUSER         (M_AXI_WUSER ),
  .S_AXI_WVALID        (M_AXI_WVALID),
  .S_AXI_WREADY        (M_AXI_WREADY),
  
  .S_AXI_BID           (M_AXI_BID   ),
  .S_AXI_BRESP         (M_AXI_BRESP ),
  .S_AXI_BUSER         (M_AXI_BUSER ),
  .S_AXI_BVALID        (M_AXI_BVALID),
  .S_AXI_BREADY        (M_AXI_BREADY),
  
  .S_AXI_ARID          (M_AXI_ARID    ),
  .S_AXI_ARADDR        (M_AXI_ARADDR  ),
  .S_AXI_ARLEN         (M_AXI_ARLEN   ),
  .S_AXI_ARSIZE        (M_AXI_ARSIZE  ),
  .S_AXI_ARBURST       (M_AXI_ARBURST ),
  .S_AXI_ARLOCK        (M_AXI_ARLOCK  ),
  .S_AXI_ARCACHE       (M_AXI_ARCACHE ),
  .S_AXI_ARPROT        (M_AXI_ARPROT  ),
  .S_AXI_ARQOS         (M_AXI_ARQOS   ),
  .S_AXI_ARREGION      (M_AXI_ARREGION),
  .S_AXI_ARUSER        (M_AXI_ARUSER  ),
  .S_AXI_ARVALID       (M_AXI_ARVALID ),
  .S_AXI_ARREADY       (M_AXI_ARREADY ),
  
  .S_AXI_RID           (M_AXI_RID   ),
  .S_AXI_RDATA         (M_AXI_RDATA ),
  .S_AXI_RRESP         (M_AXI_RRESP ),
  .S_AXI_RLAST         (M_AXI_RLAST ),
  .S_AXI_RUSER         (M_AXI_RUSER ),
  .S_AXI_RVALID        (M_AXI_RVALID),
  .S_AXI_RREADY        (M_AXI_RREADY)  
);
endmodule
