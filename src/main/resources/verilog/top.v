module top(
    input         clk_100MHz,       // basys clk signal
    inout         TMP_SDA,          // i2c sda on temp sensor - bidirectional
    output        TMP_SCL,          // i2c scl on temp sensor
    output [7:0]  LED               // basys leds = binary temp in deg C
    );
    
    wire w_200kHz;                  // 200kHz SCL
    wire [7:0] c_data;              // celsius data
    wire [7:0] f_data;              // fahrenheit data

    i2c_master master(
        .clk_200kHz(w_200kHz),
        .temp_data(c_data),
        .SDA(TMP_SDA),
        .SCL(TMP_SCL)
    );
    
    clkgen_200kHz cgen(
        .clk_100MHz(clk_100MHz),    // changed clk name to match constraints file
        .clk_200kHz(w_200kHz)
    );
    
    temp_converter tc(
        .c(c_data),
        .f(f_data)
    );
    
    //assign LED = SW ? f_data : c_data;
    assign LED = f_data;

endmodule