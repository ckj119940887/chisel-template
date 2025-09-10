module XilinxIndexAdderWrapper (
    input wire clk,
    input wire ce,
    input wire [7:0] A,
    input wire [7:0] B,
    output wire valid,
    output wire [7:0] S);

  localparam LATENCY = 1;
  reg [LATENCY-1:0] valid_shift = 'd0;

  XilinxIndexAdder u_XilinxIndexAdder (
    .CLK(clk),
    .CE(ce),
    .A(A),
    .B(B),
    .S(S)
  );

  always @(posedge clk) begin
    if(ce & ~valid_shift[LATENCY-1])
      valid_shift <= 1'b1;
    else
      valid_shift <= 0;
  end

  assign valid = valid_shift[LATENCY-1];
endmodule
