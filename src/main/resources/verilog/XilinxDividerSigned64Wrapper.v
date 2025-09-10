module XilinxDividerSigned64Wrapper(
    input wire clock,
    input wire resetn,
    input wire [63:0] a,
    input wire [63:0] b,
    input wire start,
    output wire valid,
    output wire [63:0] quotient,
    output wire [63:0] remainder);

  localparam IDLE  = 2'b00;
  localparam START = 2'b01;
  localparam WAIT  = 2'b10;

  reg start_reg;
  reg [1:0] state;
  wire [127:0] dout_tdata;
  wire divisor_tready, dividend_tready;
  XilinxDividerSigned64 u_XilinxDividerSigned64 (
    .aclk(clock),                                      // input wire aclk
    .aresetn(resetn),                                // input wire aresetn
    .s_axis_divisor_tvalid(start_reg),    // input wire s_axis_divisor_tvalid
    .s_axis_divisor_tready(divisor_tready),
    .s_axis_divisor_tdata(b),      // input wire [63 : 0] s_axis_divisor_tdata
    .s_axis_dividend_tvalid(start_reg),  // input wire s_axis_dividend_tvalid
    .s_axis_dividend_tready(dividend_tready),
    .s_axis_dividend_tdata(a),    // input wire [63 : 0] s_axis_dividend_tdata
    .m_axis_dout_tvalid(valid),          // output wire m_axis_dout_tvalid
    .m_axis_dout_tdata(dout_tdata)            // output wire [127 : 0] m_axis_dout_tdata
  );

  always @(posedge clock) begin
    if (~resetn) begin
        start_reg <= 0;
        state <= IDLE;
    end else begin
        case(state)
            IDLE: begin
                if(start) begin
                    start_reg <= 1'b1;
                    state <= START;
                end
            end
            START: begin
                if(~valid) begin
                    start_reg <= 1'b0;
                    state <= WAIT;
                end
            end
            WAIT: begin
                if(~start) begin
                    state <= IDLE;
                end
            end
        endcase
    end
  end

  assign quotient = dout_tdata[127:64];
  assign remainder = dout_tdata[63:0];
endmodule
