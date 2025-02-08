module GeneralRegFileToBRAM(
  input         clock,
  input         reset,
  input  [11:0] io_startAddr,
  input  [11:0] io_resultAddr,
  input         io_valid,
  output        io_ready,
  output        io_bramClk,
  output        io_bramWe,
  output [11:0] io_bramAddr,
  output [31:0] io_bramWData,
  input  [31:0] io_bramRData
);
`ifdef RANDOMIZE_REG_INIT
  reg [31:0] _RAND_0;
  reg [31:0] _RAND_1;
  reg [31:0] _RAND_2;
  reg [31:0] _RAND_3;
  reg [31:0] _RAND_4;
  reg [31:0] _RAND_5;
  reg [31:0] _RAND_6;
`endif // RANDOMIZE_REG_INIT
  reg [31:0] generalRegFiles_0; // @[GeneralRegFileToBRAM.scala 27:30]
  reg [31:0] generalRegFiles_1; // @[GeneralRegFileToBRAM.scala 27:30]
  reg [31:0] generalRegFiles_2; // @[GeneralRegFileToBRAM.scala 27:30]
  reg [31:0] generalRegFiles_3; // @[GeneralRegFileToBRAM.scala 27:30]
  reg [31:0] generalRegFiles_4; // @[GeneralRegFileToBRAM.scala 27:30]
  reg [3:0] stateReg; // @[GeneralRegFileToBRAM.scala 29:27]
  reg [31:0] sum; // @[GeneralRegFileToBRAM.scala 32:22]
  wire  _io_bramWe_T = stateReg == 4'h8; // @[GeneralRegFileToBRAM.scala 36:31]
  wire [11:0] _io_bramAddr_T_1 = stateReg == 4'h1 ? io_startAddr : 12'h0; // @[GeneralRegFileToBRAM.scala 39:23]
  wire [11:0] _io_bramAddr_T_4 = io_startAddr + 12'h4; // @[GeneralRegFileToBRAM.scala 40:55]
  wire [11:0] _io_bramAddr_T_5 = stateReg == 4'h2 ? _io_bramAddr_T_4 : 12'h0; // @[GeneralRegFileToBRAM.scala 40:23]
  wire [11:0] _io_bramAddr_T_6 = _io_bramAddr_T_1 | _io_bramAddr_T_5; // @[GeneralRegFileToBRAM.scala 39:67]
  wire [11:0] _io_bramAddr_T_9 = io_startAddr + 12'h8; // @[GeneralRegFileToBRAM.scala 41:55]
  wire [11:0] _io_bramAddr_T_10 = stateReg == 4'h3 ? _io_bramAddr_T_9 : 12'h0; // @[GeneralRegFileToBRAM.scala 41:23]
  wire [11:0] _io_bramAddr_T_11 = _io_bramAddr_T_6 | _io_bramAddr_T_10; // @[GeneralRegFileToBRAM.scala 40:67]
  wire [11:0] _io_bramAddr_T_14 = io_startAddr + 12'hc; // @[GeneralRegFileToBRAM.scala 42:55]
  wire [11:0] _io_bramAddr_T_15 = stateReg == 4'h4 ? _io_bramAddr_T_14 : 12'h0; // @[GeneralRegFileToBRAM.scala 42:23]
  wire [11:0] _io_bramAddr_T_16 = _io_bramAddr_T_11 | _io_bramAddr_T_15; // @[GeneralRegFileToBRAM.scala 41:67]
  wire [11:0] _io_bramAddr_T_19 = io_startAddr + 12'h10; // @[GeneralRegFileToBRAM.scala 43:55]
  wire [11:0] _io_bramAddr_T_20 = stateReg == 4'h5 ? _io_bramAddr_T_19 : 12'h0; // @[GeneralRegFileToBRAM.scala 43:23]
  wire [11:0] _io_bramAddr_T_21 = _io_bramAddr_T_16 | _io_bramAddr_T_20; // @[GeneralRegFileToBRAM.scala 42:68]
  wire [11:0] _io_bramAddr_T_23 = _io_bramWe_T ? io_resultAddr : 12'h0; // @[GeneralRegFileToBRAM.scala 44:23]
  wire [31:0] _sum_T_1 = sum + generalRegFiles_0; // @[GeneralRegFileToBRAM.scala 65:24]
  wire [31:0] _sum_T_3 = sum + generalRegFiles_1; // @[GeneralRegFileToBRAM.scala 73:24]
  wire [31:0] _sum_T_5 = sum + generalRegFiles_2; // @[GeneralRegFileToBRAM.scala 81:24]
  wire [31:0] _sum_T_7 = sum + generalRegFiles_3; // @[GeneralRegFileToBRAM.scala 89:24]
  wire [31:0] _sum_T_9 = sum + generalRegFiles_4; // @[GeneralRegFileToBRAM.scala 97:24]
  wire [3:0] _GEN_0 = 4'h8 == stateReg ? 4'h9 : stateReg; // @[GeneralRegFileToBRAM.scala 103:22 50:22 29:27]
  wire [31:0] _GEN_1 = 4'h7 == stateReg ? _sum_T_9 : sum; // @[GeneralRegFileToBRAM.scala 50:22 97:17 32:22]
  wire [3:0] _GEN_2 = 4'h7 == stateReg ? 4'h8 : _GEN_0; // @[GeneralRegFileToBRAM.scala 50:22 99:22]
  wire [31:0] _GEN_3 = 4'h6 == stateReg ? _sum_T_7 : _GEN_1; // @[GeneralRegFileToBRAM.scala 50:22 89:17]
  wire [31:0] _GEN_4 = 4'h6 == stateReg ? io_bramRData : generalRegFiles_4; // @[GeneralRegFileToBRAM.scala 50:22 27:30 91:32]
  wire [3:0] _GEN_5 = 4'h6 == stateReg ? 4'h7 : _GEN_2; // @[GeneralRegFileToBRAM.scala 50:22 93:22]
  wire [31:0] _GEN_6 = 4'h5 == stateReg ? _sum_T_5 : _GEN_3; // @[GeneralRegFileToBRAM.scala 50:22 81:17]
  wire [31:0] _GEN_7 = 4'h5 == stateReg ? io_bramRData : generalRegFiles_3; // @[GeneralRegFileToBRAM.scala 50:22 27:30 83:32]
  wire [3:0] _GEN_8 = 4'h5 == stateReg ? 4'h6 : _GEN_5; // @[GeneralRegFileToBRAM.scala 50:22 85:22]
  wire [31:0] _GEN_9 = 4'h5 == stateReg ? generalRegFiles_4 : _GEN_4; // @[GeneralRegFileToBRAM.scala 50:22 27:30]
  wire [31:0] _GEN_10 = 4'h4 == stateReg ? _sum_T_3 : _GEN_6; // @[GeneralRegFileToBRAM.scala 50:22 73:17]
  wire [31:0] _GEN_11 = 4'h4 == stateReg ? io_bramRData : generalRegFiles_2; // @[GeneralRegFileToBRAM.scala 50:22 27:30 75:32]
  wire [3:0] _GEN_12 = 4'h4 == stateReg ? 4'h5 : _GEN_8; // @[GeneralRegFileToBRAM.scala 50:22 77:22]
  wire [31:0] _GEN_13 = 4'h4 == stateReg ? generalRegFiles_3 : _GEN_7; // @[GeneralRegFileToBRAM.scala 50:22 27:30]
  wire [31:0] _GEN_14 = 4'h4 == stateReg ? generalRegFiles_4 : _GEN_9; // @[GeneralRegFileToBRAM.scala 50:22 27:30]
  wire [31:0] _GEN_15 = 4'h3 == stateReg ? _sum_T_1 : _GEN_10; // @[GeneralRegFileToBRAM.scala 50:22 65:17]
  wire [3:0] _GEN_17 = 4'h3 == stateReg ? 4'h4 : _GEN_12; // @[GeneralRegFileToBRAM.scala 50:22 69:22]
  assign io_ready = stateReg == 4'h9; // @[GeneralRegFileToBRAM.scala 48:30]
  assign io_bramClk = clock; // @[GeneralRegFileToBRAM.scala 34:16]
  assign io_bramWe = stateReg == 4'h8; // @[GeneralRegFileToBRAM.scala 36:31]
  assign io_bramAddr = _io_bramAddr_T_21 | _io_bramAddr_T_23; // @[GeneralRegFileToBRAM.scala 43:68]
  assign io_bramWData = _io_bramWe_T ? sum : 32'h0; // @[GeneralRegFileToBRAM.scala 46:24]
  always @(posedge clock) begin
    if (!(4'h0 == stateReg)) begin // @[GeneralRegFileToBRAM.scala 50:22]
      if (!(4'h1 == stateReg)) begin // @[GeneralRegFileToBRAM.scala 50:22]
        if (4'h2 == stateReg) begin // @[GeneralRegFileToBRAM.scala 50:22]
          generalRegFiles_0 <= io_bramRData; // @[GeneralRegFileToBRAM.scala 59:32]
        end
      end
    end
    if (!(4'h0 == stateReg)) begin // @[GeneralRegFileToBRAM.scala 50:22]
      if (!(4'h1 == stateReg)) begin // @[GeneralRegFileToBRAM.scala 50:22]
        if (!(4'h2 == stateReg)) begin // @[GeneralRegFileToBRAM.scala 50:22]
          if (4'h3 == stateReg) begin // @[GeneralRegFileToBRAM.scala 50:22]
            generalRegFiles_1 <= io_bramRData; // @[GeneralRegFileToBRAM.scala 67:32]
          end
        end
      end
    end
    if (!(4'h0 == stateReg)) begin // @[GeneralRegFileToBRAM.scala 50:22]
      if (!(4'h1 == stateReg)) begin // @[GeneralRegFileToBRAM.scala 50:22]
        if (!(4'h2 == stateReg)) begin // @[GeneralRegFileToBRAM.scala 50:22]
          if (!(4'h3 == stateReg)) begin // @[GeneralRegFileToBRAM.scala 50:22]
            generalRegFiles_2 <= _GEN_11;
          end
        end
      end
    end
    if (!(4'h0 == stateReg)) begin // @[GeneralRegFileToBRAM.scala 50:22]
      if (!(4'h1 == stateReg)) begin // @[GeneralRegFileToBRAM.scala 50:22]
        if (!(4'h2 == stateReg)) begin // @[GeneralRegFileToBRAM.scala 50:22]
          if (!(4'h3 == stateReg)) begin // @[GeneralRegFileToBRAM.scala 50:22]
            generalRegFiles_3 <= _GEN_13;
          end
        end
      end
    end
    if (!(4'h0 == stateReg)) begin // @[GeneralRegFileToBRAM.scala 50:22]
      if (!(4'h1 == stateReg)) begin // @[GeneralRegFileToBRAM.scala 50:22]
        if (!(4'h2 == stateReg)) begin // @[GeneralRegFileToBRAM.scala 50:22]
          if (!(4'h3 == stateReg)) begin // @[GeneralRegFileToBRAM.scala 50:22]
            generalRegFiles_4 <= _GEN_14;
          end
        end
      end
    end
    if (reset) begin // @[GeneralRegFileToBRAM.scala 29:27]
      stateReg <= 4'h0; // @[GeneralRegFileToBRAM.scala 29:27]
    end else if (4'h0 == stateReg) begin // @[GeneralRegFileToBRAM.scala 50:22]
      if (io_valid) begin // @[GeneralRegFileToBRAM.scala 52:28]
        stateReg <= 4'h1;
      end
    end else if (4'h1 == stateReg) begin // @[GeneralRegFileToBRAM.scala 50:22]
      stateReg <= 4'h2; // @[GeneralRegFileToBRAM.scala 55:22]
    end else if (4'h2 == stateReg) begin // @[GeneralRegFileToBRAM.scala 50:22]
      stateReg <= 4'h3; // @[GeneralRegFileToBRAM.scala 61:22]
    end else begin
      stateReg <= _GEN_17;
    end
    if (reset) begin // @[GeneralRegFileToBRAM.scala 32:22]
      sum <= 32'h0; // @[GeneralRegFileToBRAM.scala 32:22]
    end else if (!(4'h0 == stateReg)) begin // @[GeneralRegFileToBRAM.scala 50:22]
      if (!(4'h1 == stateReg)) begin // @[GeneralRegFileToBRAM.scala 50:22]
        if (!(4'h2 == stateReg)) begin // @[GeneralRegFileToBRAM.scala 50:22]
          sum <= _GEN_15;
        end
      end
    end
  end
// Register and memory initialization
`ifdef RANDOMIZE_GARBAGE_ASSIGN
`define RANDOMIZE
`endif
`ifdef RANDOMIZE_INVALID_ASSIGN
`define RANDOMIZE
`endif
`ifdef RANDOMIZE_REG_INIT
`define RANDOMIZE
`endif
`ifdef RANDOMIZE_MEM_INIT
`define RANDOMIZE
`endif
`ifndef RANDOM
`define RANDOM $random
`endif
`ifdef RANDOMIZE_MEM_INIT
  integer initvar;
`endif
`ifndef SYNTHESIS
`ifdef FIRRTL_BEFORE_INITIAL
`FIRRTL_BEFORE_INITIAL
`endif
initial begin
  `ifdef RANDOMIZE
    `ifdef INIT_RANDOM
      `INIT_RANDOM
    `endif
    `ifndef VERILATOR
      `ifdef RANDOMIZE_DELAY
        #`RANDOMIZE_DELAY begin end
      `else
        #0.002 begin end
      `endif
    `endif
`ifdef RANDOMIZE_REG_INIT
  _RAND_0 = {1{`RANDOM}};
  generalRegFiles_0 = _RAND_0[31:0];
  _RAND_1 = {1{`RANDOM}};
  generalRegFiles_1 = _RAND_1[31:0];
  _RAND_2 = {1{`RANDOM}};
  generalRegFiles_2 = _RAND_2[31:0];
  _RAND_3 = {1{`RANDOM}};
  generalRegFiles_3 = _RAND_3[31:0];
  _RAND_4 = {1{`RANDOM}};
  generalRegFiles_4 = _RAND_4[31:0];
  _RAND_5 = {1{`RANDOM}};
  stateReg = _RAND_5[3:0];
  _RAND_6 = {1{`RANDOM}};
  sum = _RAND_6[31:0];
`endif // RANDOMIZE_REG_INIT
  `endif // RANDOMIZE
end // initial
`ifdef FIRRTL_AFTER_INITIAL
`FIRRTL_AFTER_INITIAL
`endif
`endif // SYNTHESIS
endmodule
module AXIWrapperChiselGeneralRegFileToBRAM(
  input         clock,
  input         reset,
  input  [11:0] io_S_AXI_AWADDR,
  input  [2:0]  io_S_AXI_AWPROT,
  input         io_S_AXI_AWVALID,
  output        io_S_AXI_AWREADY,
  input  [31:0] io_S_AXI_WDATA,
  input  [3:0]  io_S_AXI_WSTRB,
  input         io_S_AXI_WVALID,
  output        io_S_AXI_WREADY,
  output [1:0]  io_S_AXI_BRESP,
  output        io_S_AXI_BVALID,
  input         io_S_AXI_BREADY,
  input  [11:0] io_S_AXI_ARADDR,
  input  [2:0]  io_S_AXI_ARPROT,
  input         io_S_AXI_ARVALID,
  output        io_S_AXI_ARREADY,
  output [31:0] io_S_AXI_RDATA,
  output [1:0]  io_S_AXI_RRESP,
  output        io_S_AXI_RVALID,
  input         io_S_AXI_RREADY,
  output        io_bramClk,
  output        io_bramWe,
  output        io_bramEn,
  output [11:0] io_bramAddr,
  output [31:0] io_bramWData,
  input  [31:0] io_bramRData
);
`ifdef RANDOMIZE_REG_INIT
  reg [31:0] _RAND_0;
  reg [31:0] _RAND_1;
  reg [31:0] _RAND_2;
  reg [31:0] _RAND_3;
  reg [31:0] _RAND_4;
  reg [31:0] _RAND_5;
  reg [31:0] _RAND_6;
  reg [31:0] _RAND_7;
  reg [31:0] _RAND_8;
  reg [31:0] _RAND_9;
  reg [31:0] _RAND_10;
  reg [31:0] _RAND_11;
  reg [31:0] _RAND_12;
  reg [31:0] _RAND_13;
`endif // RANDOMIZE_REG_INIT
  wire  modRegFileToBRAM_clock; // @[AXIWrapperChiselGeneralRegFileToBRAM.scala 89:34]
  wire  modRegFileToBRAM_reset; // @[AXIWrapperChiselGeneralRegFileToBRAM.scala 89:34]
  wire [11:0] modRegFileToBRAM_io_startAddr; // @[AXIWrapperChiselGeneralRegFileToBRAM.scala 89:34]
  wire [11:0] modRegFileToBRAM_io_resultAddr; // @[AXIWrapperChiselGeneralRegFileToBRAM.scala 89:34]
  wire  modRegFileToBRAM_io_valid; // @[AXIWrapperChiselGeneralRegFileToBRAM.scala 89:34]
  wire  modRegFileToBRAM_io_ready; // @[AXIWrapperChiselGeneralRegFileToBRAM.scala 89:34]
  wire  modRegFileToBRAM_io_bramClk; // @[AXIWrapperChiselGeneralRegFileToBRAM.scala 89:34]
  wire  modRegFileToBRAM_io_bramWe; // @[AXIWrapperChiselGeneralRegFileToBRAM.scala 89:34]
  wire [11:0] modRegFileToBRAM_io_bramAddr; // @[AXIWrapperChiselGeneralRegFileToBRAM.scala 89:34]
  wire [31:0] modRegFileToBRAM_io_bramWData; // @[AXIWrapperChiselGeneralRegFileToBRAM.scala 89:34]
  wire [31:0] modRegFileToBRAM_io_bramRData; // @[AXIWrapperChiselGeneralRegFileToBRAM.scala 89:34]
  wire  lowActiveReset = ~reset; // @[AXIWrapperChiselGeneralRegFileToBRAM.scala 47:26]
  reg [11:0] axi_awaddr; // @[AXIWrapperChiselGeneralRegFileToBRAM.scala 53:26]
  reg [11:0] axi_araddr; // @[AXIWrapperChiselGeneralRegFileToBRAM.scala 54:26]
  reg  axi_awready; // @[AXIWrapperChiselGeneralRegFileToBRAM.scala 56:26]
  reg  axi_wready; // @[AXIWrapperChiselGeneralRegFileToBRAM.scala 57:26]
  reg  axi_bvalid; // @[AXIWrapperChiselGeneralRegFileToBRAM.scala 59:26]
  reg  axi_arready; // @[AXIWrapperChiselGeneralRegFileToBRAM.scala 60:26]
  reg [31:0] axi_rdata; // @[AXIWrapperChiselGeneralRegFileToBRAM.scala 61:26]
  reg  axi_rvalid; // @[AXIWrapperChiselGeneralRegFileToBRAM.scala 63:26]
  reg [31:0] reg_data_out; // @[AXIWrapperChiselGeneralRegFileToBRAM.scala 79:27]
  reg  aw_en; // @[AXIWrapperChiselGeneralRegFileToBRAM.scala 80:27]
  reg [31:0] io_valid_reg; // @[AXIWrapperChiselGeneralRegFileToBRAM.scala 83:27]
  reg  io_ready_reg; // @[AXIWrapperChiselGeneralRegFileToBRAM.scala 84:27]
  reg [31:0] startAddr_reg; // @[AXIWrapperChiselGeneralRegFileToBRAM.scala 85:28]
  reg [31:0] resultAddr_reg; // @[AXIWrapperChiselGeneralRegFileToBRAM.scala 86:29]
  wire  _T_4 = ~axi_awready & io_S_AXI_AWVALID & io_S_AXI_WVALID & aw_en; // @[AXIWrapperChiselGeneralRegFileToBRAM.scala 118:66]
  wire  _T_5 = io_S_AXI_BREADY & axi_bvalid; // @[AXIWrapperChiselGeneralRegFileToBRAM.scala 125:37]
  wire  _GEN_1 = io_S_AXI_BREADY & axi_bvalid | aw_en; // @[AXIWrapperChiselGeneralRegFileToBRAM.scala 125:52 129:25 80:27]
  wire  _GEN_3 = ~axi_awready & io_S_AXI_AWVALID & io_S_AXI_WVALID & aw_en ? 1'h0 : _GEN_1; // @[AXIWrapperChiselGeneralRegFileToBRAM.scala 118:76 124:25]
  wire  _T_15 = ~axi_wready & io_S_AXI_WVALID & io_S_AXI_AWVALID & aw_en; // @[AXIWrapperChiselGeneralRegFileToBRAM.scala 153:65]
  wire  slv_reg_wren = axi_wready & io_S_AXI_WVALID & axi_awready & io_S_AXI_AWVALID; // @[AXIWrapperChiselGeneralRegFileToBRAM.scala 171:66]
  wire [31:0] _GEN_12 = slv_reg_wren ? io_S_AXI_WDATA : io_valid_reg; // @[AXIWrapperChiselGeneralRegFileToBRAM.scala 190:36 191:34 83:27]
  wire  _GEN_22 = _T_5 ? 1'h0 : axi_bvalid; // @[AXIWrapperChiselGeneralRegFileToBRAM.scala 210:49 211:28 59:26]
  wire  _GEN_23 = axi_awready & io_S_AXI_AWVALID & ~axi_bvalid & axi_wready & io_S_AXI_WVALID | _GEN_22; // @[AXIWrapperChiselGeneralRegFileToBRAM.scala 206:95 207:24]
  wire  _T_30 = ~axi_arready & io_S_AXI_ARVALID; // @[AXIWrapperChiselGeneralRegFileToBRAM.scala 226:27]
  wire  _T_34 = axi_arready & io_S_AXI_ARVALID & ~axi_rvalid; // @[AXIWrapperChiselGeneralRegFileToBRAM.scala 247:46]
  wire  _GEN_31 = axi_rvalid & io_S_AXI_RREADY ? 1'h0 : axi_rvalid; // @[AXIWrapperChiselGeneralRegFileToBRAM.scala 250:52 251:24 63:26]
  wire  _GEN_32 = axi_arready & io_S_AXI_ARVALID & ~axi_rvalid | _GEN_31; // @[AXIWrapperChiselGeneralRegFileToBRAM.scala 247:62 248:24]
  wire [7:0] readAddr = axi_araddr[9:2]; // @[AXIWrapperChiselGeneralRegFileToBRAM.scala 266:30]
  wire [1:0] _reg_data_out_T_1 = {1'h0,io_ready_reg}; // @[Cat.scala 33:92]
  GeneralRegFileToBRAM modRegFileToBRAM ( // @[AXIWrapperChiselGeneralRegFileToBRAM.scala 89:34]
    .clock(modRegFileToBRAM_clock),
    .reset(modRegFileToBRAM_reset),
    .io_startAddr(modRegFileToBRAM_io_startAddr),
    .io_resultAddr(modRegFileToBRAM_io_resultAddr),
    .io_valid(modRegFileToBRAM_io_valid),
    .io_ready(modRegFileToBRAM_io_ready),
    .io_bramClk(modRegFileToBRAM_io_bramClk),
    .io_bramWe(modRegFileToBRAM_io_bramWe),
    .io_bramAddr(modRegFileToBRAM_io_bramAddr),
    .io_bramWData(modRegFileToBRAM_io_bramWData),
    .io_bramRData(modRegFileToBRAM_io_bramRData)
  );
  assign io_S_AXI_AWREADY = axi_awready; // @[AXIWrapperChiselGeneralRegFileToBRAM.scala 101:22]
  assign io_S_AXI_WREADY = axi_wready; // @[AXIWrapperChiselGeneralRegFileToBRAM.scala 102:22]
  assign io_S_AXI_BRESP = 2'h0; // @[AXIWrapperChiselGeneralRegFileToBRAM.scala 103:22]
  assign io_S_AXI_BVALID = axi_bvalid; // @[AXIWrapperChiselGeneralRegFileToBRAM.scala 104:26]
  assign io_S_AXI_ARREADY = axi_arready; // @[AXIWrapperChiselGeneralRegFileToBRAM.scala 105:22]
  assign io_S_AXI_RDATA = axi_rdata; // @[AXIWrapperChiselGeneralRegFileToBRAM.scala 106:22]
  assign io_S_AXI_RRESP = 2'h0; // @[AXIWrapperChiselGeneralRegFileToBRAM.scala 107:22]
  assign io_S_AXI_RVALID = axi_rvalid; // @[AXIWrapperChiselGeneralRegFileToBRAM.scala 108:22]
  assign io_bramClk = modRegFileToBRAM_io_bramClk; // @[AXIWrapperChiselGeneralRegFileToBRAM.scala 93:18]
  assign io_bramWe = modRegFileToBRAM_io_bramWe; // @[AXIWrapperChiselGeneralRegFileToBRAM.scala 94:18]
  assign io_bramEn = 1'h1; // @[AXIWrapperChiselGeneralRegFileToBRAM.scala 95:18]
  assign io_bramAddr = modRegFileToBRAM_io_bramAddr; // @[AXIWrapperChiselGeneralRegFileToBRAM.scala 96:18]
  assign io_bramWData = modRegFileToBRAM_io_bramWData; // @[AXIWrapperChiselGeneralRegFileToBRAM.scala 97:18]
  assign modRegFileToBRAM_clock = clock;
  assign modRegFileToBRAM_reset = ~reset; // @[AXIWrapperChiselGeneralRegFileToBRAM.scala 47:26]
  assign modRegFileToBRAM_io_startAddr = startAddr_reg[11:0]; // @[AXIWrapperChiselGeneralRegFileToBRAM.scala 90:51]
  assign modRegFileToBRAM_io_resultAddr = resultAddr_reg[11:0]; // @[AXIWrapperChiselGeneralRegFileToBRAM.scala 91:53]
  assign modRegFileToBRAM_io_valid = io_valid_reg[0]; // @[AXIWrapperChiselGeneralRegFileToBRAM.scala 92:46]
  assign modRegFileToBRAM_io_bramRData = io_bramRData; // @[AXIWrapperChiselGeneralRegFileToBRAM.scala 98:35]
  always @(posedge clock) begin
    if (lowActiveReset) begin // @[AXIWrapperChiselGeneralRegFileToBRAM.scala 138:33]
      axi_awaddr <= 12'h0; // @[AXIWrapperChiselGeneralRegFileToBRAM.scala 139:20]
    end else if (_T_4) begin // @[AXIWrapperChiselGeneralRegFileToBRAM.scala 141:76]
      axi_awaddr <= io_S_AXI_AWADDR; // @[AXIWrapperChiselGeneralRegFileToBRAM.scala 142:24]
    end
    if (lowActiveReset) begin // @[AXIWrapperChiselGeneralRegFileToBRAM.scala 222:33]
      axi_araddr <= 12'h0; // @[AXIWrapperChiselGeneralRegFileToBRAM.scala 224:21]
    end else if (~axi_arready & io_S_AXI_ARVALID) begin // @[AXIWrapperChiselGeneralRegFileToBRAM.scala 226:48]
      axi_araddr <= io_S_AXI_ARADDR; // @[AXIWrapperChiselGeneralRegFileToBRAM.scala 229:25]
    end
    if (lowActiveReset) begin // @[AXIWrapperChiselGeneralRegFileToBRAM.scala 114:33]
      axi_awready <= 1'h0; // @[AXIWrapperChiselGeneralRegFileToBRAM.scala 115:21]
    end else begin
      axi_awready <= _T_4;
    end
    if (lowActiveReset) begin // @[AXIWrapperChiselGeneralRegFileToBRAM.scala 150:33]
      axi_wready <= 1'h0; // @[AXIWrapperChiselGeneralRegFileToBRAM.scala 151:20]
    end else begin
      axi_wready <= _T_15;
    end
    if (lowActiveReset) begin // @[AXIWrapperChiselGeneralRegFileToBRAM.scala 202:33]
      axi_bvalid <= 1'h0; // @[AXIWrapperChiselGeneralRegFileToBRAM.scala 203:20]
    end else begin
      axi_bvalid <= _GEN_23;
    end
    if (lowActiveReset) begin // @[AXIWrapperChiselGeneralRegFileToBRAM.scala 222:33]
      axi_arready <= 1'h0; // @[AXIWrapperChiselGeneralRegFileToBRAM.scala 223:21]
    end else begin
      axi_arready <= _T_30;
    end
    if (lowActiveReset) begin // @[AXIWrapperChiselGeneralRegFileToBRAM.scala 270:33]
      axi_rdata <= 32'h0; // @[AXIWrapperChiselGeneralRegFileToBRAM.scala 271:19]
    end else if (_T_34) begin // @[AXIWrapperChiselGeneralRegFileToBRAM.scala 276:28]
      axi_rdata <= reg_data_out; // @[AXIWrapperChiselGeneralRegFileToBRAM.scala 277:23]
    end
    if (lowActiveReset) begin // @[AXIWrapperChiselGeneralRegFileToBRAM.scala 243:33]
      axi_rvalid <= 1'h0; // @[AXIWrapperChiselGeneralRegFileToBRAM.scala 244:20]
    end else begin
      axi_rvalid <= _GEN_32;
    end
    if (readAddr == 8'h3) begin // @[AXIWrapperChiselGeneralRegFileToBRAM.scala 267:24]
      reg_data_out <= {{30'd0}, _reg_data_out_T_1};
    end
    aw_en <= lowActiveReset | _GEN_3; // @[AXIWrapperChiselGeneralRegFileToBRAM.scala 114:33 116:21]
    if (lowActiveReset) begin // @[AXIWrapperChiselGeneralRegFileToBRAM.scala 173:33]
      io_valid_reg <= 32'h0; // @[AXIWrapperChiselGeneralRegFileToBRAM.scala 174:22]
    end else if (!(8'h0 == axi_awaddr[9:2])) begin // @[AXIWrapperChiselGeneralRegFileToBRAM.scala 178:68]
      if (!(8'h1 == axi_awaddr[9:2])) begin // @[AXIWrapperChiselGeneralRegFileToBRAM.scala 178:68]
        if (8'h2 == axi_awaddr[9:2]) begin // @[AXIWrapperChiselGeneralRegFileToBRAM.scala 178:68]
          io_valid_reg <= _GEN_12;
        end
      end
    end
    if (lowActiveReset) begin // @[AXIWrapperChiselGeneralRegFileToBRAM.scala 255:33]
      io_ready_reg <= 1'h0; // @[AXIWrapperChiselGeneralRegFileToBRAM.scala 256:22]
    end else begin
      io_ready_reg <= modRegFileToBRAM_io_ready | io_ready_reg; // @[AXIWrapperChiselGeneralRegFileToBRAM.scala 258:22]
    end
    if (lowActiveReset) begin // @[AXIWrapperChiselGeneralRegFileToBRAM.scala 173:33]
      startAddr_reg <= 32'h0; // @[AXIWrapperChiselGeneralRegFileToBRAM.scala 175:23]
    end else if (8'h0 == axi_awaddr[9:2]) begin // @[AXIWrapperChiselGeneralRegFileToBRAM.scala 178:68]
      if (slv_reg_wren) begin // @[AXIWrapperChiselGeneralRegFileToBRAM.scala 180:36]
        startAddr_reg <= io_S_AXI_WDATA; // @[AXIWrapperChiselGeneralRegFileToBRAM.scala 181:35]
      end
    end
    if (lowActiveReset) begin // @[AXIWrapperChiselGeneralRegFileToBRAM.scala 173:33]
      resultAddr_reg <= 32'h0; // @[AXIWrapperChiselGeneralRegFileToBRAM.scala 176:24]
    end else if (!(8'h0 == axi_awaddr[9:2])) begin // @[AXIWrapperChiselGeneralRegFileToBRAM.scala 178:68]
      if (8'h1 == axi_awaddr[9:2]) begin // @[AXIWrapperChiselGeneralRegFileToBRAM.scala 178:68]
        if (slv_reg_wren) begin // @[AXIWrapperChiselGeneralRegFileToBRAM.scala 185:36]
          resultAddr_reg <= io_S_AXI_WDATA; // @[AXIWrapperChiselGeneralRegFileToBRAM.scala 186:36]
        end
      end
    end
  end
// Register and memory initialization
`ifdef RANDOMIZE_GARBAGE_ASSIGN
`define RANDOMIZE
`endif
`ifdef RANDOMIZE_INVALID_ASSIGN
`define RANDOMIZE
`endif
`ifdef RANDOMIZE_REG_INIT
`define RANDOMIZE
`endif
`ifdef RANDOMIZE_MEM_INIT
`define RANDOMIZE
`endif
`ifndef RANDOM
`define RANDOM $random
`endif
`ifdef RANDOMIZE_MEM_INIT
  integer initvar;
`endif
`ifndef SYNTHESIS
`ifdef FIRRTL_BEFORE_INITIAL
`FIRRTL_BEFORE_INITIAL
`endif
initial begin
  `ifdef RANDOMIZE
    `ifdef INIT_RANDOM
      `INIT_RANDOM
    `endif
    `ifndef VERILATOR
      `ifdef RANDOMIZE_DELAY
        #`RANDOMIZE_DELAY begin end
      `else
        #0.002 begin end
      `endif
    `endif
`ifdef RANDOMIZE_REG_INIT
  _RAND_0 = {1{`RANDOM}};
  axi_awaddr = _RAND_0[11:0];
  _RAND_1 = {1{`RANDOM}};
  axi_araddr = _RAND_1[11:0];
  _RAND_2 = {1{`RANDOM}};
  axi_awready = _RAND_2[0:0];
  _RAND_3 = {1{`RANDOM}};
  axi_wready = _RAND_3[0:0];
  _RAND_4 = {1{`RANDOM}};
  axi_bvalid = _RAND_4[0:0];
  _RAND_5 = {1{`RANDOM}};
  axi_arready = _RAND_5[0:0];
  _RAND_6 = {1{`RANDOM}};
  axi_rdata = _RAND_6[31:0];
  _RAND_7 = {1{`RANDOM}};
  axi_rvalid = _RAND_7[0:0];
  _RAND_8 = {1{`RANDOM}};
  reg_data_out = _RAND_8[31:0];
  _RAND_9 = {1{`RANDOM}};
  aw_en = _RAND_9[0:0];
  _RAND_10 = {1{`RANDOM}};
  io_valid_reg = _RAND_10[31:0];
  _RAND_11 = {1{`RANDOM}};
  io_ready_reg = _RAND_11[0:0];
  _RAND_12 = {1{`RANDOM}};
  startAddr_reg = _RAND_12[31:0];
  _RAND_13 = {1{`RANDOM}};
  resultAddr_reg = _RAND_13[31:0];
`endif // RANDOMIZE_REG_INIT
  `endif // RANDOMIZE
end // initial
`ifdef FIRRTL_AFTER_INITIAL
`FIRRTL_AFTER_INITIAL
`endif
`endif // SYNTHESIS
endmodule
