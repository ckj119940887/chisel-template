module XilinxBUFGWrapper (
  input wire I,
  output wire O
);
  BUFG bufg_inst(
    .I(I),
    .O(O)
  );
endmodule
