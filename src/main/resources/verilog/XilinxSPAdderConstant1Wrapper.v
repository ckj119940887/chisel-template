module XilinxSPAdderConstant1Wrapper(
    input wire clk,
    input wire ce,
    input wire [15:0] A,
    output wire valid,
    output wire [15:0] S);

  reg [1:0] valid_shift = 2'b0;

  XilinxSPAdderConstant1 u_XilinxSPAdderConstant1 (
    .CLK(clk),
    .CE(ce),
    .A(A),
    .S(S)
  );

  always @(posedge clk) begin
    if (ce)
      valid_shift <= {valid_shift[0], 1'b1};
    else
      valid_shift <= 0;
  end

  assign valid = valid_shift[1];
endmodule
