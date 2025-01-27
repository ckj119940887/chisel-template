package TDC

import chisel3._
import chisel3.util._
import chisel3.experimental._

class adder_tree (val NUM_OF_INS: Int = 5, 
                  val WIDTH_PER_IN: Int = 16, 
                  val WIDTH_FINAL_OUT: Int = 17) extends BlackBox with HasBlackBoxResource {
    val io = IO(new Bundle {
        val i_Clk = Input(Clock())
        val i_Reset_N = Input(Bool())
        val i_In_All = Input(UInt((NUM_OF_INS*WIDTH_PER_IN).W))
        val o_Out_All = Output(UInt(WIDTH_FINAL_OUT.W))
        val i_Data_Valid_In = Input(Bool())
        val o_Data_Valid_Out = Output(Bool())
    })

    addResource("/TDC/adder_tree.v")
    addResource("/TDC/adder_tree_layer.v")
    addResource("/TDC/adder_tree_node.v")
}

class Wrapper_adder_tree(val NUM_OF_INS: Int = 5, 
                         val WIDTH_PER_IN: Int = 16, 
                         val WIDTH_FINAL_OUT: Int = 17) extends Module {
    val io = IO(new Bundle{
        val i_Reset_N = Input(Bool())
        val i_In_All = Input(UInt((NUM_OF_INS*WIDTH_PER_IN).W))
        val o_Out_All = Output(UInt(WIDTH_FINAL_OUT.W))
        val i_Data_Valid_In = Input(Bool())
        val o_Data_Valid_Out = Output(Bool())
    })
    
    val adderTree = Module(new adder_tree(NUM_OF_INS, WIDTH_PER_IN, WIDTH_FINAL_OUT))

    adderTree.io.i_Clk := clock
    adderTree.io.i_Reset_N := io.i_Reset_N
    adderTree.io.i_In_All := io.i_In_All
    io.o_Out_All := adderTree.io.o_Out_All
    adderTree.io.i_Data_Valid_In := io.i_Data_Valid_In
    io.o_Data_Valid_Out := adderTree.io.o_Data_Valid_Out
}