module PmodDA3 #(
    parameter integer DIVIDE = 4  // 100 MHz / (2*DIVIDE) = SCLK. e.g., DIVIDE=4 -> 12.5 MHz
)(
    input  wire        clk,     // 100MHz board clock
    input  wire        reset,   // synchronous active-high reset
    input  wire        enable,  // start a transfer when ready
    input  wire [15:0] data,    // 16-bit value to shift to DAC
    output wire        CS,      // chip select (active-low)
    output wire        DIN,     // serial data out (MOSI)
    output wire        SCLK,    // serial clock
    output wire        LDAC,    // load DAC (active-low pulse)
    output wire        ready    // high when idle
);

    // Clock divider for SCLK
    reg [$clog2(DIVIDE)-1:0] div_cnt = 0;
    reg sclk_int = 1'b0;
    wire tick = (div_cnt == DIVIDE-1);

    always @(posedge clk) begin
        if (reset) begin
            div_cnt  <= 0;
            sclk_int <= 1'b0;
        end else begin
            if (div_cnt == DIVIDE-1) begin
                div_cnt  <= 0;
                sclk_int <= ~sclk_int;
            end else begin
                div_cnt <= div_cnt + 1'b1;
            end
        end
    end

    assign SCLK = sclk_int;

    // Simple state machine
    localparam IDLE   = 2'd0;
    localparam SHIFT  = 2'd1;
    localparam LDAC_P = 2'd2;

    reg [1:0]  state = IDLE;
    reg [15:0] shreg = 16'h0000;
    reg [4:0]  bitcnt = 5'd0;      // counts 0..16
    reg        cs_n = 1'b1;
    reg        ldac_n = 1'b1;
    reg        ready_r = 1'b1;

    // For DIN, drive MSB of shift register
    assign DIN   = shreg[15];
    assign CS    = cs_n;
    assign LDAC  = ldac_n;
    assign ready = ready_r;

    // Shift on one edge of SCLK; we use rising edges of sclk_int.
    // To avoid metastability with divided clock, we gate actions on tick & sclk edge.
    reg sclk_d = 1'b0;
    always @(posedge clk) begin
        if (reset) begin
            sclk_d  <= 1'b0;
        end else begin
            if (tick) sclk_d <= sclk_int;
        end
    end
    wire sclk_rise = tick && (sclk_int && !sclk_d);

    always @(posedge clk) begin
        if (reset) begin
            state   <= IDLE;
            shreg   <= 16'h0000;
            bitcnt  <= 5'd0;
            cs_n    <= 1'b1;
            ldac_n  <= 1'b1;
            ready_r <= 1'b1;
        end else begin
            case (state)
                IDLE: begin
                    cs_n    <= 1'b1;
                    ldac_n  <= 1'b1;
                    ready_r <= 1'b1;
                    bitcnt  <= 5'd0;
                    if (enable) begin
                        // Latch input and begin
                        shreg   <= data;
                        cs_n    <= 1'b0; // assert CS low
                        ready_r <= 1'b0;
                        state   <= SHIFT;
                    end
                end

                SHIFT: begin
                    // Shift out MSB-first on rising SCLK edge
                    if (sclk_rise) begin
                        shreg <= {shreg[14:0], 1'b0};
                        bitcnt <= bitcnt + 1'b1;
                        if (bitcnt == 5'd15) begin
                            // After 16 bits have been clocked
                            cs_n   <= 1'b1;   // deassert CS
                            ldac_n <= 1'b0;   // pulse LDAC low
                            state  <= LDAC_P;
                        end
                    end
                end

                LDAC_P: begin
                    // Keep LDAC low for one SCLK rising edge
                    if (sclk_rise) begin
                        ldac_n <= 1'b1;
                        state  <= IDLE;
                        ready_r<= 1'b1;
                    end
                end

                default: state <= IDLE;
            endcase
        end
    end

endmodule