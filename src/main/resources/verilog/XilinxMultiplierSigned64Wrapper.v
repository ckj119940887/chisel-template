module XilinxMultiplierSigned64Wrapper(
    input wire clk,
    input wire ce,
    input wire [63:0] a,
    input wire [63:0] b,
    output wire valid,
    output wire [63:0] p);

  localparam LATENCY = 18;

  reg [LATENCY-1:0] valid_shift;
  XilinxMultiplierSigned64 u_XilinxMultiplierSigned64 (
    .CLK(clk),
    .CE(ce),
    .A(a),
    .B(b),
    .P(p)
  );

  always @(posedge clk) begin
    if(ce & ~valid_shift[LATENCY-1])
      valid_shift <= {valid_shift[LATENCY-2:0], 1'b1};
    else
      valid_shift <= 0;
  end

  assign valid = valid_shift[LATENCY-1];
endmodule
