`timescale 1ns / 1ps


module adder_tree #(parameter NUM_OF_INS = 5,
                    parameter WIDTH_PER_IN = 16,
                    parameter WIDTH_FINAL_OUT = 17)
                   (input  wire i_Clk,
                    input  wire i_Reset_N,
                    input  wire [NUM_OF_INS*WIDTH_PER_IN-1:0] i_In_All,
                    output wire [WIDTH_FINAL_OUT-1:0] o_Out_All,
                    input  wire i_Data_Valid_In,
                    output wire o_Data_Valid_Out);


localparam NUM_IN_PAIRS = NUM_OF_INS / 2;
localparam NUM_IN_MODULOUS = NUM_OF_INS - NUM_IN_PAIRS * 2;
localparam NUM_LAYER_OUTS = NUM_IN_PAIRS + NUM_IN_MODULOUS;
localparam WIDTH_PER_LAYER_OUT = WIDTH_PER_IN + 1; 

generate 
	if (NUM_OF_INS == 1) begin // final layer of the tree, output the results and terminate the recursion
		if (WIDTH_FINAL_OUT < WIDTH_PER_LAYER_OUT) begin // check if the final output width is sufficient
			initial begin
				$display("ERROR: WIDTH_FINAL_OUT (%0d) should be greater than WIDTH_PER_LAYER_OUT (%0d)", WIDTH_FINAL_OUT, WIDTH_PER_LAYER_OUT);
			    $stop();
			end
		end	
		assign o_Out_All = {{(WIDTH_FINAL_OUT-WIDTH_PER_IN){1'b0}},i_In_All}; // pad the output with zeros
		assign o_Data_Valid_Out = i_Data_Valid_In;
	end	else begin
		// instantiate the layer
		wire [NUM_LAYER_OUTS*WIDTH_PER_LAYER_OUT-1:0] w_layer_out_all;
		wire w_layer_data_valid;

		adder_tree_layer #(.NUM_OF_INS(NUM_OF_INS), 
                           .WIDTH_PER_IN(WIDTH_PER_IN), 
                           .WIDTH_PER_OUT(WIDTH_PER_LAYER_OUT))
                           add_layer (
			               .i_Clk(i_Clk),
                           .i_Reset_N(i_Reset_N),
                           .i_Layer_In_All(i_In_All),
			               .o_Layer_Out_All(w_layer_out_all),
			               .i_Data_Valid_In(i_Data_Valid_In),
			               .o_Data_Valid_Out(w_layer_data_valid));
	
		// recursively instantiate the remaining part of the tree
		adder_tree #(.NUM_OF_INS(NUM_LAYER_OUTS), 
                     .WIDTH_PER_IN(WIDTH_PER_LAYER_OUT), 
                     .WIDTH_FINAL_OUT(WIDTH_FINAL_OUT))
                     add_tree (
			         .i_Clk(i_Clk),
                     .i_Reset_N(i_Reset_N),
			         .i_In_All(w_layer_out_all),
			         .o_Out_All(o_Out_All),
			         .i_Data_Valid_In(w_layer_data_valid),
			         .o_Data_Valid_Out(o_Data_Valid_Out));
	end
endgenerate

endmodule