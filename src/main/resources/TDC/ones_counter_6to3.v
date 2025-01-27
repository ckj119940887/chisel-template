`timescale 1ns / 1ps

// 6 bits to 3 bits counter
// Pure combinational logic to fully utilize the 6-input LUTs

module ones_counter_6to3(
    input wire [5:0] i_Sequence,
    output reg [2:0] o_Count
    );

    always @(i_Sequence) begin
        case(i_Sequence)
            0:  o_Count=0; // 6'b000000
            1:  o_Count=1; // 6'b000001
            2:  o_Count=1; // 6'b000010
            3:  o_Count=2; // 6'b000011
            4:  o_Count=1; // 6'b000100
            5:  o_Count=2; // 6'b000101
            6:  o_Count=2; // 6'b000110
            7:  o_Count=3; // 6'b000111
            8:  o_Count=1; // 6'b001000
            9:  o_Count=2; // 6'b001001
            10: o_Count=2; // 6'b001010
            11: o_Count=3; // 6'b001011
            12: o_Count=2; // 6'b001100
            13: o_Count=3; // 6'b001101
            14: o_Count=3; // 6'b001110
            15: o_Count=4; // 6'b001111
            16: o_Count=1; // 6'b010000
            17: o_Count=2; // 6'b010001
            18: o_Count=2; // 6'b010010
            19: o_Count=3; // 6'b010011
            20: o_Count=2; // 6'b010100
            21: o_Count=3; // 6'b010101
            22: o_Count=3; // 6'b010110
            23: o_Count=4; // 6'b010111
            24: o_Count=2; // 6'b011000
            25: o_Count=3; // 6'b011001
            26: o_Count=3; // 6'b011010
            27: o_Count=4; // 6'b011011
            28: o_Count=3; // 6'b011100
            29: o_Count=4; // 6'b011101
            30: o_Count=4; // 6'b011110
            31: o_Count=5; // 6'b011111
            32: o_Count=1; // 6'b100000
            33: o_Count=2; // 6'b100001
            34: o_Count=2; // 6'b100010
            35: o_Count=3; // 6'b100011
            36: o_Count=2; // 6'b100100
            37: o_Count=3; // 6'b100101
            38: o_Count=3; // 6'b100110
            39: o_Count=4; // 6'b100111
            40: o_Count=2; // 6'b101000
            41: o_Count=3; // 6'b101001
            42: o_Count=3; // 6'b101010
            43: o_Count=4; // 6'b101011
            44: o_Count=3; // 6'b101100
            45: o_Count=4; // 6'b101101
            46: o_Count=4; // 6'b101110
            47: o_Count=5; // 6'b101111
            48: o_Count=2; // 6'b110000
            49: o_Count=3; // 6'b110001
            50: o_Count=3; // 6'b110010
            51: o_Count=4; // 6'b110011
            52: o_Count=3; // 6'b110100
            53: o_Count=4; // 6'b110101
            54: o_Count=4; // 6'b110110
            55: o_Count=5; // 6'b110111
            56: o_Count=3; // 6'b111000
            57: o_Count=4; // 6'b111001
            58: o_Count=4; // 6'b111010
            59: o_Count=5; // 6'b111011
            60: o_Count=4; // 6'b111100
            61: o_Count=5; // 6'b111101
            62: o_Count=5; // 6'b111110
            63: o_Count=6; // 6'b111111
            default: o_Count=0;
        endcase
    end


endmodule