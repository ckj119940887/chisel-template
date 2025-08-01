module XilinxSPAdderUnsignedWrapper(
    input wire clk,
    input wire ce,
    input wire [15:0] A,
    input wire [15:0] B,
    output wire valid,
    output wire [15:0] S);

  localparam LATENCY = 2;
  reg [LATENCY-1:0] valid_shift = 'd0;

  XilinxSPAdderUnsigned u_XilinxSPAdderUnsigned (
    .CLK(clk),
    .CE(ce),
    .A(A),
    .B(B),
    .S(S)
  );

  always @(posedge clk) begin
    if (ce)
      valid_shift <= {valid_shift[LATENCY-2], 1'b1};
    else
      valid_shift <= 0;
  end

  assign valid = valid_shift[LATENCY-1];
endmodule
