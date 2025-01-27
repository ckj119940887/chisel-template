`timescale 1ns / 1ps

module ones_counter_36to6(
    input  wire i_Clk,
    input  wire i_Reset_N,
    input  wire [35:0] i_Sequence,
    output reg  [5:0] o_Count
    );

    localparam WIDTH_OF_IN = 6;
    wire [2:0] w_sum_a, w_sum_b, w_sum_c, w_sum_d, w_sum_e, w_sum_f;
    wire [4:0] w_sum1, w_sum2;
    wire [4:0] w_sum;

    ones_counter_6to3 a (.i_Sequence(i_Sequence[WIDTH_OF_IN-1:0]), .o_Count(w_sum_a));
    ones_counter_6to3 b (.i_Sequence(i_Sequence[WIDTH_OF_IN*2-1:WIDTH_OF_IN]), .o_Count(w_sum_b));
    ones_counter_6to3 c (.i_Sequence(i_Sequence[WIDTH_OF_IN*3-1:WIDTH_OF_IN*2]), .o_Count(w_sum_c));
    ones_counter_6to3 d (.i_Sequence(i_Sequence[WIDTH_OF_IN*4-1:WIDTH_OF_IN*3]), .o_Count(w_sum_d));
    ones_counter_6to3 e (.i_Sequence(i_Sequence[WIDTH_OF_IN*5-1:WIDTH_OF_IN*4]), .o_Count(w_sum_e));
    ones_counter_6to3 f (.i_Sequence(i_Sequence[WIDTH_OF_IN*6-1:WIDTH_OF_IN*5]), .o_Count(w_sum_f));
    
    assign w_sum1 = w_sum_a + w_sum_b + w_sum_c;
    assign w_sum2 = w_sum_d + w_sum_e + w_sum_f;
    assign w_sum = w_sum1 + w_sum2;
    
    always @(posedge i_Clk) begin
        if (!i_Reset_N) begin
            o_Count <= 0;
        end else begin
            o_Count <= w_sum;
        end
    end

endmodule