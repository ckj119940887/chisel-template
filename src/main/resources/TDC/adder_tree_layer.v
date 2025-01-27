`timescale 1ns / 1ps

module adder_tree_layer # (parameter NUM_OF_INS = 5,
                           parameter WIDTH_PER_IN = 16,
                           parameter WIDTH_PER_OUT = 17,
                           parameter NUM_IN_PAIRS = NUM_OF_INS / 2,
                           parameter NUM_IN_MODULOUS = NUM_OF_INS - NUM_IN_PAIRS * 2,
                           parameter NUM_OF_OUTS = NUM_IN_PAIRS + NUM_IN_MODULOUS)
                          (input  wire i_Clk,
                           input  wire i_Reset_N,
                           input  wire [NUM_OF_INS * WIDTH_PER_IN - 1 : 0] i_Layer_In_All,
                           output wire [NUM_OF_OUTS * WIDTH_PER_OUT -1 :0] o_Layer_Out_All,
                           input  wire i_Data_Valid_In,
                           output reg  o_Data_Valid_Out);

always @(posedge i_Clk) begin // delay for output register
	if (!i_Reset_N) begin
		o_Data_Valid_Out <= 1'b0;
	end	else begin
		o_Data_Valid_Out <= i_Data_Valid_In;
	end
end

genvar i;
generate		
	// process the pairs as binary adder nodes
	for (i=0; i<NUM_IN_PAIRS; i=i+1) begin
		wire [2*WIDTH_PER_IN-1:0] node_ins;
		assign node_ins = i_Layer_In_All[2*(i+1)*WIDTH_PER_IN-1 : 2*i*WIDTH_PER_IN];
		adder_tree_node #(.WIDTH_OF_IN(WIDTH_PER_IN),
                          .WIDTH_OF_OUT(WIDTH_PER_OUT)) 
                          add_node (
                          .i_Clk(i_Clk),
                          .i_Reset_N(i_Reset_N),
                          .i_In1(node_ins[WIDTH_PER_IN-1:0]),
                          .i_In2(node_ins[2*WIDTH_PER_IN-1:WIDTH_PER_IN]),
                          .i_Data_Valid(i_Data_Valid_In),
                          .o_Out(o_Layer_Out_All[(i+1)*WIDTH_PER_OUT-1 : i*WIDTH_PER_OUT]));
	end	
	// process any odd fall-through words
	if (NUM_IN_MODULOUS) begin // treat this like a +0
		adder_tree_node #(.WIDTH_OF_IN(WIDTH_PER_IN),
                          .WIDTH_OF_OUT(WIDTH_PER_OUT)) 
                          add_node (
                          .i_Clk(i_Clk),
                          .i_Reset_N(i_Reset_N),
                          .i_In1(i_Layer_In_All[WIDTH_PER_IN*NUM_OF_INS-1 : WIDTH_PER_IN*(NUM_OF_INS-1)]),
                          .i_In2({WIDTH_PER_IN{1'b0}}),
                          .i_Data_Valid(i_Data_Valid_In),
                          .o_Out(o_Layer_Out_All[(NUM_IN_PAIRS+1)*WIDTH_PER_OUT-1 : NUM_IN_PAIRS*WIDTH_PER_OUT]));	
	end
endgenerate

endmodule