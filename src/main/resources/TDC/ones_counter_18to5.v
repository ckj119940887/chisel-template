`timescale 1ns / 1ps

module ones_counter_18to5(
    input  wire i_Clk,
    input  wire i_Reset_N,
    input  wire [17:0] i_Sequence,
    output reg  [4:0] o_Count
    );

    localparam WIDTH_OF_IN = 6;
    wire [2:0] w_sum_a, w_sum_b, w_sum_c;
    wire [4:0] w_sum;

    ones_counter_6to3 a (.i_Sequence(i_Sequence[WIDTH_OF_IN-1:0]), .o_Count(w_sum_a));
    ones_counter_6to3 b (.i_Sequence(i_Sequence[WIDTH_OF_IN*2-1:WIDTH_OF_IN]), .o_Count(w_sum_b));
    ones_counter_6to3 c (.i_Sequence(i_Sequence[WIDTH_OF_IN*3-1:WIDTH_OF_IN*2]), .o_Count(w_sum_c));
    
    assign w_sum = w_sum_a + w_sum_b + w_sum_c;
    
    always @(posedge i_Clk) begin
        if (!i_Reset_N) begin
            o_Count <= 0;
        end else begin
            o_Count <= w_sum;
        end
    end

endmodule