module adder_tree_node #(parameter WIDTH_OF_IN = 16,
						 parameter WIDTH_OF_OUT = 17)
						(input  wire i_Clk,
						 input  wire i_Reset_N, 
						 input  wire [WIDTH_OF_IN-1:0] i_In1,
						 input  wire [WIDTH_OF_IN-1:0] i_In2,
						 input  wire i_Data_Valid,
						 output reg  [WIDTH_OF_OUT-1:0] o_Out);

// wire extension
wire [WIDTH_OF_OUT-1:0] w_in1_ext,w_in2_ext;
assign w_in1_ext = {{(WIDTH_OF_OUT-WIDTH_OF_IN){1'b0}},i_In1};
assign w_in2_ext = {{(WIDTH_OF_OUT-WIDTH_OF_IN){1'b0}},i_In2};


// addition
wire [WIDTH_OF_OUT-1:0] w_sum;
assign w_sum = w_in1_ext + w_in2_ext;


// output register
always @(posedge i_Clk) begin
	if (!i_Reset_N) begin
		o_Out <= 0;
	end
	else begin
		if (i_Data_Valid) begin
			o_Out <= w_sum;
		end else begin
			o_Out <= o_Out;
		end
	end
end

endmodule