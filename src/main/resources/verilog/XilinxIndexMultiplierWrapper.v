module XilinxIndexMultiplierWrapper (
    input wire clk,
    input wire ce,
    input wire [15:0] A,
    input wire [15:0] B,
    output wire valid,
    output wire [15:0] P);

  localparam LATENCY = 4;
  reg [LATENCY-1:0] valid_shift = 'd0;

  XilinxIndexMultiplier u_XilinxIndexMultiplier (
    .CLK(clk),
    .CE(ce),
    .A(A),
    .B(B),
    .P(P)
  );

  always @(posedge clk) begin
    if (ce)
      valid_shift <= {valid_shift[LATENCY-2:0], 1'b1};
    else
      valid_shift <= 0;
  end

  assign valid = valid_shift[LATENCY-1];
endmodule
