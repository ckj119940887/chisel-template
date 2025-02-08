module GeneralRegFileToBRAM(
  input         clock,
  input         reset,
  input  [10:0] io_startAddr,
  input         io_ready,
  input  [10:0] io_resultAddr,
  output        io_valid,
  output        io_bramClk,
  output        io_bramWe,
  output [10:0] io_bramAddr,
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
  reg [31:0] generalRegFiles_0; // @[GeneralRegFileToBRAM.scala 26:30]
  reg [31:0] generalRegFiles_1; // @[GeneralRegFileToBRAM.scala 26:30]
  reg [31:0] generalRegFiles_2; // @[GeneralRegFileToBRAM.scala 26:30]
  reg [31:0] generalRegFiles_3; // @[GeneralRegFileToBRAM.scala 26:30]
  reg [31:0] generalRegFiles_4; // @[GeneralRegFileToBRAM.scala 26:30]
  reg [3:0] stateReg; // @[GeneralRegFileToBRAM.scala 28:27]
  reg [31:0] sum; // @[GeneralRegFileToBRAM.scala 31:22]
  wire  _io_bramWe_T = stateReg == 4'h8; // @[GeneralRegFileToBRAM.scala 35:31]
  wire [10:0] _io_bramAddr_T_1 = stateReg == 4'h1 ? io_startAddr : 11'h0; // @[GeneralRegFileToBRAM.scala 37:23]
  wire [10:0] _io_bramAddr_T_4 = io_startAddr + 11'h4; // @[GeneralRegFileToBRAM.scala 38:55]
  wire [10:0] _io_bramAddr_T_5 = stateReg == 4'h2 ? _io_bramAddr_T_4 : 11'h0; // @[GeneralRegFileToBRAM.scala 38:23]
  wire [10:0] _io_bramAddr_T_6 = _io_bramAddr_T_1 | _io_bramAddr_T_5; // @[GeneralRegFileToBRAM.scala 37:67]
  wire [10:0] _io_bramAddr_T_9 = io_startAddr + 11'h8; // @[GeneralRegFileToBRAM.scala 39:55]
  wire [10:0] _io_bramAddr_T_10 = stateReg == 4'h3 ? _io_bramAddr_T_9 : 11'h0; // @[GeneralRegFileToBRAM.scala 39:23]
  wire [10:0] _io_bramAddr_T_11 = _io_bramAddr_T_6 | _io_bramAddr_T_10; // @[GeneralRegFileToBRAM.scala 38:67]
  wire [10:0] _io_bramAddr_T_14 = io_startAddr + 11'hc; // @[GeneralRegFileToBRAM.scala 40:55]
  wire [10:0] _io_bramAddr_T_15 = stateReg == 4'h4 ? _io_bramAddr_T_14 : 11'h0; // @[GeneralRegFileToBRAM.scala 40:23]
  wire [10:0] _io_bramAddr_T_16 = _io_bramAddr_T_11 | _io_bramAddr_T_15; // @[GeneralRegFileToBRAM.scala 39:67]
  wire [10:0] _io_bramAddr_T_19 = io_startAddr + 11'h10; // @[GeneralRegFileToBRAM.scala 41:55]
  wire [10:0] _io_bramAddr_T_20 = stateReg == 4'h5 ? _io_bramAddr_T_19 : 11'h0; // @[GeneralRegFileToBRAM.scala 41:23]
  wire [10:0] _io_bramAddr_T_21 = _io_bramAddr_T_16 | _io_bramAddr_T_20; // @[GeneralRegFileToBRAM.scala 40:68]
  wire [10:0] _io_bramAddr_T_23 = _io_bramWe_T ? io_resultAddr : 11'h0; // @[GeneralRegFileToBRAM.scala 42:23]
  wire [31:0] _sum_T_1 = sum + generalRegFiles_0; // @[GeneralRegFileToBRAM.scala 63:24]
  wire [31:0] _sum_T_3 = sum + generalRegFiles_1; // @[GeneralRegFileToBRAM.scala 71:24]
  wire [31:0] _sum_T_5 = sum + generalRegFiles_2; // @[GeneralRegFileToBRAM.scala 79:24]
  wire [31:0] _sum_T_7 = sum + generalRegFiles_3; // @[GeneralRegFileToBRAM.scala 87:24]
  wire [31:0] _sum_T_9 = sum + generalRegFiles_4; // @[GeneralRegFileToBRAM.scala 95:24]
  wire [3:0] _GEN_0 = 4'h8 == stateReg ? 4'h9 : stateReg; // @[GeneralRegFileToBRAM.scala 101:22 48:22 28:27]
  wire [31:0] _GEN_1 = 4'h7 == stateReg ? _sum_T_9 : sum; // @[GeneralRegFileToBRAM.scala 48:22 95:17 31:22]
  wire [3:0] _GEN_2 = 4'h7 == stateReg ? 4'h8 : _GEN_0; // @[GeneralRegFileToBRAM.scala 48:22 97:22]
  wire [31:0] _GEN_3 = 4'h6 == stateReg ? _sum_T_7 : _GEN_1; // @[GeneralRegFileToBRAM.scala 48:22 87:17]
  wire [31:0] _GEN_4 = 4'h6 == stateReg ? io_bramRData : generalRegFiles_4; // @[GeneralRegFileToBRAM.scala 48:22 26:30 89:32]
  wire [3:0] _GEN_5 = 4'h6 == stateReg ? 4'h7 : _GEN_2; // @[GeneralRegFileToBRAM.scala 48:22 91:22]
  wire [31:0] _GEN_6 = 4'h5 == stateReg ? _sum_T_5 : _GEN_3; // @[GeneralRegFileToBRAM.scala 48:22 79:17]
  wire [31:0] _GEN_7 = 4'h5 == stateReg ? io_bramRData : generalRegFiles_3; // @[GeneralRegFileToBRAM.scala 48:22 26:30 81:32]
  wire [3:0] _GEN_8 = 4'h5 == stateReg ? 4'h6 : _GEN_5; // @[GeneralRegFileToBRAM.scala 48:22 83:22]
  wire [31:0] _GEN_9 = 4'h5 == stateReg ? generalRegFiles_4 : _GEN_4; // @[GeneralRegFileToBRAM.scala 48:22 26:30]
  wire [31:0] _GEN_10 = 4'h4 == stateReg ? _sum_T_3 : _GEN_6; // @[GeneralRegFileToBRAM.scala 48:22 71:17]
  wire [31:0] _GEN_11 = 4'h4 == stateReg ? io_bramRData : generalRegFiles_2; // @[GeneralRegFileToBRAM.scala 48:22 26:30 73:32]
  wire [3:0] _GEN_12 = 4'h4 == stateReg ? 4'h5 : _GEN_8; // @[GeneralRegFileToBRAM.scala 48:22 75:22]
  wire [31:0] _GEN_13 = 4'h4 == stateReg ? generalRegFiles_3 : _GEN_7; // @[GeneralRegFileToBRAM.scala 48:22 26:30]
  wire [31:0] _GEN_14 = 4'h4 == stateReg ? generalRegFiles_4 : _GEN_9; // @[GeneralRegFileToBRAM.scala 48:22 26:30]
  wire [31:0] _GEN_15 = 4'h3 == stateReg ? _sum_T_1 : _GEN_10; // @[GeneralRegFileToBRAM.scala 48:22 63:17]
  wire [3:0] _GEN_17 = 4'h3 == stateReg ? 4'h4 : _GEN_12; // @[GeneralRegFileToBRAM.scala 48:22 67:22]
  assign io_valid = stateReg == 4'h9; // @[GeneralRegFileToBRAM.scala 46:30]
  assign io_bramClk = clock; // @[GeneralRegFileToBRAM.scala 33:16]
  assign io_bramWe = stateReg == 4'h8; // @[GeneralRegFileToBRAM.scala 35:31]
  assign io_bramAddr = _io_bramAddr_T_21 | _io_bramAddr_T_23; // @[GeneralRegFileToBRAM.scala 41:68]
  assign io_bramWData = _io_bramWe_T ? sum : 32'h0; // @[GeneralRegFileToBRAM.scala 44:24]
  always @(posedge clock) begin
    if (!(4'h0 == stateReg)) begin // @[GeneralRegFileToBRAM.scala 48:22]
      if (!(4'h1 == stateReg)) begin // @[GeneralRegFileToBRAM.scala 48:22]
        if (4'h2 == stateReg) begin // @[GeneralRegFileToBRAM.scala 48:22]
          generalRegFiles_0 <= io_bramRData; // @[GeneralRegFileToBRAM.scala 57:32]
        end
      end
    end
    if (!(4'h0 == stateReg)) begin // @[GeneralRegFileToBRAM.scala 48:22]
      if (!(4'h1 == stateReg)) begin // @[GeneralRegFileToBRAM.scala 48:22]
        if (!(4'h2 == stateReg)) begin // @[GeneralRegFileToBRAM.scala 48:22]
          if (4'h3 == stateReg) begin // @[GeneralRegFileToBRAM.scala 48:22]
            generalRegFiles_1 <= io_bramRData; // @[GeneralRegFileToBRAM.scala 65:32]
          end
        end
      end
    end
    if (!(4'h0 == stateReg)) begin // @[GeneralRegFileToBRAM.scala 48:22]
      if (!(4'h1 == stateReg)) begin // @[GeneralRegFileToBRAM.scala 48:22]
        if (!(4'h2 == stateReg)) begin // @[GeneralRegFileToBRAM.scala 48:22]
          if (!(4'h3 == stateReg)) begin // @[GeneralRegFileToBRAM.scala 48:22]
            generalRegFiles_2 <= _GEN_11;
          end
        end
      end
    end
    if (!(4'h0 == stateReg)) begin // @[GeneralRegFileToBRAM.scala 48:22]
      if (!(4'h1 == stateReg)) begin // @[GeneralRegFileToBRAM.scala 48:22]
        if (!(4'h2 == stateReg)) begin // @[GeneralRegFileToBRAM.scala 48:22]
          if (!(4'h3 == stateReg)) begin // @[GeneralRegFileToBRAM.scala 48:22]
            generalRegFiles_3 <= _GEN_13;
          end
        end
      end
    end
    if (!(4'h0 == stateReg)) begin // @[GeneralRegFileToBRAM.scala 48:22]
      if (!(4'h1 == stateReg)) begin // @[GeneralRegFileToBRAM.scala 48:22]
        if (!(4'h2 == stateReg)) begin // @[GeneralRegFileToBRAM.scala 48:22]
          if (!(4'h3 == stateReg)) begin // @[GeneralRegFileToBRAM.scala 48:22]
            generalRegFiles_4 <= _GEN_14;
          end
        end
      end
    end
    if (reset) begin // @[GeneralRegFileToBRAM.scala 28:27]
      stateReg <= 4'h0; // @[GeneralRegFileToBRAM.scala 28:27]
    end else if (4'h0 == stateReg) begin // @[GeneralRegFileToBRAM.scala 48:22]
      if (io_ready) begin // @[GeneralRegFileToBRAM.scala 50:28]
        stateReg <= 4'h1;
      end
    end else if (4'h1 == stateReg) begin // @[GeneralRegFileToBRAM.scala 48:22]
      stateReg <= 4'h2; // @[GeneralRegFileToBRAM.scala 53:22]
    end else if (4'h2 == stateReg) begin // @[GeneralRegFileToBRAM.scala 48:22]
      stateReg <= 4'h3; // @[GeneralRegFileToBRAM.scala 59:22]
    end else begin
      stateReg <= _GEN_17;
    end
    if (reset) begin // @[GeneralRegFileToBRAM.scala 31:22]
      sum <= 32'h0; // @[GeneralRegFileToBRAM.scala 31:22]
    end else if (!(4'h0 == stateReg)) begin // @[GeneralRegFileToBRAM.scala 48:22]
      if (!(4'h1 == stateReg)) begin // @[GeneralRegFileToBRAM.scala 48:22]
        if (!(4'h2 == stateReg)) begin // @[GeneralRegFileToBRAM.scala 48:22]
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
