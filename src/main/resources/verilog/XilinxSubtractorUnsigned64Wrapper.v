module XilinxSubtractorUnsigned64Wrapper(
    input wire clk,
    input wire ce,
    input wire [63:0] A,
    input wire [63:0] B,
    output wire valid,
    output wire [63:0] S);

  reg [5:0] valid_shift = 6'b0;

  XilinxSubtractorUnsigned64 u_XilinxSubtractorUnsigned64 (
    .CLK(clk),
    .CE(ce),
    .A(A),
    .B(B),
    .S(S)
  );

  always @(posedge clk) begin
    if(ce & ~valid_shift[5])
      valid_shift <= {valid_shift[4:0], 1'b1};
    else
      valid_shift <= 0;
  end

  assign valid = valid_shift[5];
endmodule
