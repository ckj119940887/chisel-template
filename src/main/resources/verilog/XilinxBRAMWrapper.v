module XilinxBRAMWrapper(
    input wire clk,
    input wire ena,
    input wire wea,
    input wire [9:0] addra,
    input wire [63:0] dina,
    output wire [63:0] douta,
    input wire enb,
    input wire web,
    input wire [9:0] addrb,
    input wire [63:0] dinb,
    output wire [63:0] doutb
);

  XilinxBRAM u_XilinxBRAM (
    .clka(clk),    // input wire clka
    .ena(ena),      // input wire ena
    .wea(wea),      // input wire [0 : 0] wea
    .addra(addra),  // input wire [9 : 0] addra
    .dina(dina),    // input wire [63 : 0] dina
    .douta(douta),  // output wire [63 : 0] douta
    .clkb(clk),    // input wire clkb
    .enb(enb),      // input wire enb
    .web(web),      // input wire [0 : 0] web
    .addrb(addrb),  // input wire [9 : 0] addrb
    .dinb(dinb),    // input wire [63 : 0] dinb
    .doutb(doutb)  // output wire [63 : 0] doutb
  );

endmodule
