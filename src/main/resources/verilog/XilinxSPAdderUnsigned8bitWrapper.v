module XilinxSPAdderUnsigned8bitWrapper(
    input wire clk,
    input wire ce,
    input wire [7:0] A,
    input wire [7:0] B,
    output wire valid,
    output wire [7:0] S);

  reg [0:0] valid_shift = 1'b0;

  XilinxSPAdderUnsigned8bit u_XilinxSPAdderUnsigned8bit (
    .CLK(clk),
    .CE(ce),
    .A(A),
    .B(B),
    .S(S)
  );

  always @(posedge clk) begin
    if (ce)
      valid_shift <= 1'b1;
    else
      valid_shift <= 0;
  end

  assign valid = valid_shift;
endmodule
