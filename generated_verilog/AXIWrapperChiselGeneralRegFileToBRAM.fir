circuit AXIWrapperChiselGeneralRegFileToBRAM :
  module GeneralRegFileToBRAM :
    input clock : Clock
    input reset : Reset
    output io : { flip startAddr : UInt<12>, flip resultAddr : UInt<12>, flip valid : UInt<1>, ready : UInt<1>, bramClk : Clock, bramWe : UInt<1>, bramEn : UInt<1>, bramAddr : UInt<12>, bramWData : UInt<32>, flip bramRData : UInt<32>}

    reg generalRegFiles : UInt<32>[10], clock with :
      reset => (UInt<1>("h0"), generalRegFiles) @[GeneralRegFileToBRAM.scala 27:30]
    reg stateReg : UInt<4>, clock with :
      reset => (reset, UInt<4>("h0")) @[GeneralRegFileToBRAM.scala 29:27]
    reg sum : UInt<32>, clock with :
      reset => (reset, UInt<32>("h0")) @[GeneralRegFileToBRAM.scala 32:22]
    io.bramClk <= clock @[GeneralRegFileToBRAM.scala 34:16]
    node _io_bramWe_T = eq(stateReg, UInt<4>("h8")) @[GeneralRegFileToBRAM.scala 36:31]
    node _io_bramWe_T_1 = mux(_io_bramWe_T, UInt<1>("h1"), UInt<1>("h0")) @[GeneralRegFileToBRAM.scala 36:21]
    io.bramWe <= _io_bramWe_T_1 @[GeneralRegFileToBRAM.scala 36:15]
    io.bramEn <= UInt<1>("h1") @[GeneralRegFileToBRAM.scala 37:15]
    node _io_bramAddr_T = eq(stateReg, UInt<1>("h1")) @[GeneralRegFileToBRAM.scala 39:33]
    node _io_bramAddr_T_1 = mux(_io_bramAddr_T, io.startAddr, UInt<1>("h0")) @[GeneralRegFileToBRAM.scala 39:23]
    node _io_bramAddr_T_2 = eq(stateReg, UInt<2>("h2")) @[GeneralRegFileToBRAM.scala 40:33]
    node _io_bramAddr_T_3 = add(io.startAddr, UInt<3>("h4")) @[GeneralRegFileToBRAM.scala 40:55]
    node _io_bramAddr_T_4 = tail(_io_bramAddr_T_3, 1) @[GeneralRegFileToBRAM.scala 40:55]
    node _io_bramAddr_T_5 = mux(_io_bramAddr_T_2, _io_bramAddr_T_4, UInt<1>("h0")) @[GeneralRegFileToBRAM.scala 40:23]
    node _io_bramAddr_T_6 = or(_io_bramAddr_T_1, _io_bramAddr_T_5) @[GeneralRegFileToBRAM.scala 39:67]
    node _io_bramAddr_T_7 = eq(stateReg, UInt<2>("h3")) @[GeneralRegFileToBRAM.scala 41:33]
    node _io_bramAddr_T_8 = add(io.startAddr, UInt<4>("h8")) @[GeneralRegFileToBRAM.scala 41:55]
    node _io_bramAddr_T_9 = tail(_io_bramAddr_T_8, 1) @[GeneralRegFileToBRAM.scala 41:55]
    node _io_bramAddr_T_10 = mux(_io_bramAddr_T_7, _io_bramAddr_T_9, UInt<1>("h0")) @[GeneralRegFileToBRAM.scala 41:23]
    node _io_bramAddr_T_11 = or(_io_bramAddr_T_6, _io_bramAddr_T_10) @[GeneralRegFileToBRAM.scala 40:67]
    node _io_bramAddr_T_12 = eq(stateReg, UInt<3>("h4")) @[GeneralRegFileToBRAM.scala 42:33]
    node _io_bramAddr_T_13 = add(io.startAddr, UInt<4>("hc")) @[GeneralRegFileToBRAM.scala 42:55]
    node _io_bramAddr_T_14 = tail(_io_bramAddr_T_13, 1) @[GeneralRegFileToBRAM.scala 42:55]
    node _io_bramAddr_T_15 = mux(_io_bramAddr_T_12, _io_bramAddr_T_14, UInt<1>("h0")) @[GeneralRegFileToBRAM.scala 42:23]
    node _io_bramAddr_T_16 = or(_io_bramAddr_T_11, _io_bramAddr_T_15) @[GeneralRegFileToBRAM.scala 41:67]
    node _io_bramAddr_T_17 = eq(stateReg, UInt<3>("h5")) @[GeneralRegFileToBRAM.scala 43:33]
    node _io_bramAddr_T_18 = add(io.startAddr, UInt<5>("h10")) @[GeneralRegFileToBRAM.scala 43:55]
    node _io_bramAddr_T_19 = tail(_io_bramAddr_T_18, 1) @[GeneralRegFileToBRAM.scala 43:55]
    node _io_bramAddr_T_20 = mux(_io_bramAddr_T_17, _io_bramAddr_T_19, UInt<1>("h0")) @[GeneralRegFileToBRAM.scala 43:23]
    node _io_bramAddr_T_21 = or(_io_bramAddr_T_16, _io_bramAddr_T_20) @[GeneralRegFileToBRAM.scala 42:68]
    node _io_bramAddr_T_22 = eq(stateReg, UInt<4>("h8")) @[GeneralRegFileToBRAM.scala 44:33]
    node _io_bramAddr_T_23 = mux(_io_bramAddr_T_22, io.resultAddr, UInt<1>("h0")) @[GeneralRegFileToBRAM.scala 44:23]
    node _io_bramAddr_T_24 = or(_io_bramAddr_T_21, _io_bramAddr_T_23) @[GeneralRegFileToBRAM.scala 43:68]
    io.bramAddr <= _io_bramAddr_T_24 @[GeneralRegFileToBRAM.scala 39:17]
    node _io_bramWData_T = eq(stateReg, UInt<4>("h8")) @[GeneralRegFileToBRAM.scala 46:34]
    node _io_bramWData_T_1 = mux(_io_bramWData_T, sum, UInt<1>("h0")) @[GeneralRegFileToBRAM.scala 46:24]
    io.bramWData <= _io_bramWData_T_1 @[GeneralRegFileToBRAM.scala 46:18]
    node _io_ready_T = eq(stateReg, UInt<4>("h9")) @[GeneralRegFileToBRAM.scala 48:30]
    node _io_ready_T_1 = mux(_io_ready_T, UInt<1>("h1"), UInt<1>("h0")) @[GeneralRegFileToBRAM.scala 48:20]
    io.ready <= _io_ready_T_1 @[GeneralRegFileToBRAM.scala 48:14]
    node _T = eq(UInt<1>("h0"), stateReg) @[GeneralRegFileToBRAM.scala 50:22]
    when _T : @[GeneralRegFileToBRAM.scala 50:22]
      node _stateReg_T = mux(io.valid, UInt<1>("h1"), stateReg) @[GeneralRegFileToBRAM.scala 52:28]
      stateReg <= _stateReg_T @[GeneralRegFileToBRAM.scala 52:22]
    else :
      node _T_1 = eq(UInt<1>("h1"), stateReg) @[GeneralRegFileToBRAM.scala 50:22]
      when _T_1 : @[GeneralRegFileToBRAM.scala 50:22]
        stateReg <= UInt<2>("h2") @[GeneralRegFileToBRAM.scala 55:22]
      else :
        node _T_2 = eq(UInt<2>("h2"), stateReg) @[GeneralRegFileToBRAM.scala 50:22]
        when _T_2 : @[GeneralRegFileToBRAM.scala 50:22]
          generalRegFiles[0] <= io.bramRData @[GeneralRegFileToBRAM.scala 59:32]
          stateReg <= UInt<2>("h3") @[GeneralRegFileToBRAM.scala 61:22]
        else :
          node _T_3 = eq(UInt<2>("h3"), stateReg) @[GeneralRegFileToBRAM.scala 50:22]
          when _T_3 : @[GeneralRegFileToBRAM.scala 50:22]
            node _sum_T = add(sum, generalRegFiles[0]) @[GeneralRegFileToBRAM.scala 65:24]
            node _sum_T_1 = tail(_sum_T, 1) @[GeneralRegFileToBRAM.scala 65:24]
            sum <= _sum_T_1 @[GeneralRegFileToBRAM.scala 65:17]
            generalRegFiles[1] <= io.bramRData @[GeneralRegFileToBRAM.scala 67:32]
            stateReg <= UInt<3>("h4") @[GeneralRegFileToBRAM.scala 69:22]
          else :
            node _T_4 = eq(UInt<3>("h4"), stateReg) @[GeneralRegFileToBRAM.scala 50:22]
            when _T_4 : @[GeneralRegFileToBRAM.scala 50:22]
              node _sum_T_2 = add(sum, generalRegFiles[1]) @[GeneralRegFileToBRAM.scala 73:24]
              node _sum_T_3 = tail(_sum_T_2, 1) @[GeneralRegFileToBRAM.scala 73:24]
              sum <= _sum_T_3 @[GeneralRegFileToBRAM.scala 73:17]
              generalRegFiles[2] <= io.bramRData @[GeneralRegFileToBRAM.scala 75:32]
              stateReg <= UInt<3>("h5") @[GeneralRegFileToBRAM.scala 77:22]
            else :
              node _T_5 = eq(UInt<3>("h5"), stateReg) @[GeneralRegFileToBRAM.scala 50:22]
              when _T_5 : @[GeneralRegFileToBRAM.scala 50:22]
                node _sum_T_4 = add(sum, generalRegFiles[2]) @[GeneralRegFileToBRAM.scala 81:24]
                node _sum_T_5 = tail(_sum_T_4, 1) @[GeneralRegFileToBRAM.scala 81:24]
                sum <= _sum_T_5 @[GeneralRegFileToBRAM.scala 81:17]
                generalRegFiles[3] <= io.bramRData @[GeneralRegFileToBRAM.scala 83:32]
                stateReg <= UInt<3>("h6") @[GeneralRegFileToBRAM.scala 85:22]
              else :
                node _T_6 = eq(UInt<3>("h6"), stateReg) @[GeneralRegFileToBRAM.scala 50:22]
                when _T_6 : @[GeneralRegFileToBRAM.scala 50:22]
                  node _sum_T_6 = add(sum, generalRegFiles[3]) @[GeneralRegFileToBRAM.scala 89:24]
                  node _sum_T_7 = tail(_sum_T_6, 1) @[GeneralRegFileToBRAM.scala 89:24]
                  sum <= _sum_T_7 @[GeneralRegFileToBRAM.scala 89:17]
                  generalRegFiles[4] <= io.bramRData @[GeneralRegFileToBRAM.scala 91:32]
                  stateReg <= UInt<3>("h7") @[GeneralRegFileToBRAM.scala 93:22]
                else :
                  node _T_7 = eq(UInt<3>("h7"), stateReg) @[GeneralRegFileToBRAM.scala 50:22]
                  when _T_7 : @[GeneralRegFileToBRAM.scala 50:22]
                    node _sum_T_8 = add(sum, generalRegFiles[4]) @[GeneralRegFileToBRAM.scala 97:24]
                    node _sum_T_9 = tail(_sum_T_8, 1) @[GeneralRegFileToBRAM.scala 97:24]
                    sum <= _sum_T_9 @[GeneralRegFileToBRAM.scala 97:17]
                    stateReg <= UInt<4>("h8") @[GeneralRegFileToBRAM.scala 99:22]
                  else :
                    node _T_8 = eq(UInt<4>("h8"), stateReg) @[GeneralRegFileToBRAM.scala 50:22]
                    when _T_8 : @[GeneralRegFileToBRAM.scala 50:22]
                      stateReg <= UInt<4>("h9") @[GeneralRegFileToBRAM.scala 103:22]
                    else :
                      node _T_9 = eq(UInt<4>("h9"), stateReg) @[GeneralRegFileToBRAM.scala 50:22]
                      when _T_9 : @[GeneralRegFileToBRAM.scala 50:22]
                        skip


  module AXIWrapperChiselGeneralRegFileToBRAM :
    input clock : Clock
    input reset : UInt<1>
    output io : { flip S_AXI_AWADDR : UInt<12>, flip S_AXI_AWPROT : UInt<3>, flip S_AXI_AWVALID : UInt<1>, S_AXI_AWREADY : UInt<1>, flip S_AXI_WDATA : UInt<32>, flip S_AXI_WSTRB : UInt<4>, flip S_AXI_WVALID : UInt<1>, S_AXI_WREADY : UInt<1>, S_AXI_BRESP : UInt<2>, S_AXI_BVALID : UInt<1>, flip S_AXI_BREADY : UInt<1>, flip S_AXI_ARADDR : UInt<12>, flip S_AXI_ARPROT : UInt<3>, flip S_AXI_ARVALID : UInt<1>, S_AXI_ARREADY : UInt<1>, S_AXI_RDATA : UInt<32>, S_AXI_RRESP : UInt<2>, S_AXI_RVALID : UInt<1>, flip S_AXI_RREADY : UInt<1>, bramClk : Clock, bramWe : UInt<1>, bramEn : UInt<1>, bramAddr : UInt<12>, bramWData : UInt<32>, flip bramRData : UInt<32>}

    node _lowActiveReset_T = bits(reset, 0, 0) @[AXIWrapperChiselGeneralRegFileToBRAM.scala 47:33]
    node lowActiveReset = eq(_lowActiveReset_T, UInt<1>("h0")) @[AXIWrapperChiselGeneralRegFileToBRAM.scala 47:26]
    reg axi_awaddr : UInt<12>, clock with :
      reset => (UInt<1>("h0"), axi_awaddr) @[AXIWrapperChiselGeneralRegFileToBRAM.scala 53:26]
    reg axi_araddr : UInt<12>, clock with :
      reset => (UInt<1>("h0"), axi_araddr) @[AXIWrapperChiselGeneralRegFileToBRAM.scala 54:26]
    reg axi_awready : UInt<1>, clock with :
      reset => (UInt<1>("h0"), axi_awready) @[AXIWrapperChiselGeneralRegFileToBRAM.scala 56:26]
    reg axi_wready : UInt<1>, clock with :
      reset => (UInt<1>("h0"), axi_wready) @[AXIWrapperChiselGeneralRegFileToBRAM.scala 57:26]
    reg axi_bresp : UInt<2>, clock with :
      reset => (UInt<1>("h0"), axi_bresp) @[AXIWrapperChiselGeneralRegFileToBRAM.scala 58:26]
    reg axi_bvalid : UInt<1>, clock with :
      reset => (UInt<1>("h0"), axi_bvalid) @[AXIWrapperChiselGeneralRegFileToBRAM.scala 59:26]
    reg axi_arready : UInt<1>, clock with :
      reset => (UInt<1>("h0"), axi_arready) @[AXIWrapperChiselGeneralRegFileToBRAM.scala 60:26]
    reg axi_rdata : UInt<32>, clock with :
      reset => (UInt<1>("h0"), axi_rdata) @[AXIWrapperChiselGeneralRegFileToBRAM.scala 61:26]
    reg axi_rresp : UInt<2>, clock with :
      reset => (UInt<1>("h0"), axi_rresp) @[AXIWrapperChiselGeneralRegFileToBRAM.scala 62:26]
    reg axi_rvalid : UInt<1>, clock with :
      reset => (UInt<1>("h0"), axi_rvalid) @[AXIWrapperChiselGeneralRegFileToBRAM.scala 63:26]
    wire slv_reg_rden : UInt<1> @[AXIWrapperChiselGeneralRegFileToBRAM.scala 77:28]
    wire slv_reg_wren : UInt<1> @[AXIWrapperChiselGeneralRegFileToBRAM.scala 78:28]
    reg reg_data_out : UInt<32>, clock with :
      reset => (UInt<1>("h0"), reg_data_out) @[AXIWrapperChiselGeneralRegFileToBRAM.scala 79:27]
    reg aw_en : UInt<1>, clock with :
      reset => (UInt<1>("h0"), aw_en) @[AXIWrapperChiselGeneralRegFileToBRAM.scala 80:27]
    reg io_valid_reg : UInt<32>, clock with :
      reset => (UInt<1>("h0"), io_valid_reg) @[AXIWrapperChiselGeneralRegFileToBRAM.scala 83:27]
    reg io_ready_reg : UInt<1>, clock with :
      reset => (UInt<1>("h0"), io_ready_reg) @[AXIWrapperChiselGeneralRegFileToBRAM.scala 84:27]
    reg startAddr_reg : UInt<32>, clock with :
      reset => (UInt<1>("h0"), startAddr_reg) @[AXIWrapperChiselGeneralRegFileToBRAM.scala 85:28]
    reg resultAddr_reg : UInt<32>, clock with :
      reset => (UInt<1>("h0"), resultAddr_reg) @[AXIWrapperChiselGeneralRegFileToBRAM.scala 86:29]
    inst modRegFileToBRAM of GeneralRegFileToBRAM @[AXIWrapperChiselGeneralRegFileToBRAM.scala 89:34]
    modRegFileToBRAM.clock <= clock
    modRegFileToBRAM.reset <= lowActiveReset
    node _modRegFileToBRAM_io_startAddr_T = bits(startAddr_reg, 11, 0) @[AXIWrapperChiselGeneralRegFileToBRAM.scala 90:51]
    modRegFileToBRAM.io.startAddr <= _modRegFileToBRAM_io_startAddr_T @[AXIWrapperChiselGeneralRegFileToBRAM.scala 90:35]
    node _modRegFileToBRAM_io_resultAddr_T = bits(resultAddr_reg, 11, 0) @[AXIWrapperChiselGeneralRegFileToBRAM.scala 91:53]
    modRegFileToBRAM.io.resultAddr <= _modRegFileToBRAM_io_resultAddr_T @[AXIWrapperChiselGeneralRegFileToBRAM.scala 91:36]
    node _modRegFileToBRAM_io_valid_T = bits(io_valid_reg, 0, 0) @[AXIWrapperChiselGeneralRegFileToBRAM.scala 92:46]
    modRegFileToBRAM.io.valid <= _modRegFileToBRAM_io_valid_T @[AXIWrapperChiselGeneralRegFileToBRAM.scala 92:31]
    io.bramClk <= modRegFileToBRAM.io.bramClk @[AXIWrapperChiselGeneralRegFileToBRAM.scala 93:18]
    io.bramWe <= modRegFileToBRAM.io.bramWe @[AXIWrapperChiselGeneralRegFileToBRAM.scala 94:18]
    io.bramEn <= modRegFileToBRAM.io.bramEn @[AXIWrapperChiselGeneralRegFileToBRAM.scala 95:18]
    io.bramAddr <= modRegFileToBRAM.io.bramAddr @[AXIWrapperChiselGeneralRegFileToBRAM.scala 96:18]
    io.bramWData <= modRegFileToBRAM.io.bramWData @[AXIWrapperChiselGeneralRegFileToBRAM.scala 97:18]
    modRegFileToBRAM.io.bramRData <= io.bramRData @[AXIWrapperChiselGeneralRegFileToBRAM.scala 98:35]
    io.S_AXI_AWREADY <= axi_awready @[AXIWrapperChiselGeneralRegFileToBRAM.scala 101:22]
    io.S_AXI_WREADY <= axi_wready @[AXIWrapperChiselGeneralRegFileToBRAM.scala 102:22]
    io.S_AXI_BRESP <= axi_bresp @[AXIWrapperChiselGeneralRegFileToBRAM.scala 103:22]
    io.S_AXI_BVALID <= axi_bvalid @[AXIWrapperChiselGeneralRegFileToBRAM.scala 104:26]
    io.S_AXI_ARREADY <= axi_arready @[AXIWrapperChiselGeneralRegFileToBRAM.scala 105:22]
    io.S_AXI_RDATA <= axi_rdata @[AXIWrapperChiselGeneralRegFileToBRAM.scala 106:22]
    io.S_AXI_RRESP <= axi_rresp @[AXIWrapperChiselGeneralRegFileToBRAM.scala 107:22]
    io.S_AXI_RVALID <= axi_rvalid @[AXIWrapperChiselGeneralRegFileToBRAM.scala 108:22]
    node _T = bits(lowActiveReset, 0, 0) @[AXIWrapperChiselGeneralRegFileToBRAM.scala 114:25]
    when _T : @[AXIWrapperChiselGeneralRegFileToBRAM.scala 114:33]
      axi_awready <= UInt<1>("h0") @[AXIWrapperChiselGeneralRegFileToBRAM.scala 115:21]
      aw_en <= UInt<1>("h1") @[AXIWrapperChiselGeneralRegFileToBRAM.scala 116:21]
    else :
      node _T_1 = not(axi_awready) @[AXIWrapperChiselGeneralRegFileToBRAM.scala 118:14]
      node _T_2 = and(_T_1, io.S_AXI_AWVALID) @[AXIWrapperChiselGeneralRegFileToBRAM.scala 118:27]
      node _T_3 = and(_T_2, io.S_AXI_WVALID) @[AXIWrapperChiselGeneralRegFileToBRAM.scala 118:47]
      node _T_4 = and(_T_3, aw_en) @[AXIWrapperChiselGeneralRegFileToBRAM.scala 118:66]
      when _T_4 : @[AXIWrapperChiselGeneralRegFileToBRAM.scala 118:76]
        axi_awready <= UInt<1>("h1") @[AXIWrapperChiselGeneralRegFileToBRAM.scala 123:25]
        aw_en <= UInt<1>("h0") @[AXIWrapperChiselGeneralRegFileToBRAM.scala 124:25]
      else :
        node _T_5 = and(io.S_AXI_BREADY, axi_bvalid) @[AXIWrapperChiselGeneralRegFileToBRAM.scala 125:37]
        when _T_5 : @[AXIWrapperChiselGeneralRegFileToBRAM.scala 125:52]
          axi_awready <= UInt<1>("h0") @[AXIWrapperChiselGeneralRegFileToBRAM.scala 128:25]
          aw_en <= UInt<1>("h1") @[AXIWrapperChiselGeneralRegFileToBRAM.scala 129:25]
        else :
          axi_awready <= UInt<1>("h0") @[AXIWrapperChiselGeneralRegFileToBRAM.scala 131:26]
    node _T_6 = bits(lowActiveReset, 0, 0) @[AXIWrapperChiselGeneralRegFileToBRAM.scala 138:25]
    when _T_6 : @[AXIWrapperChiselGeneralRegFileToBRAM.scala 138:33]
      axi_awaddr <= UInt<1>("h0") @[AXIWrapperChiselGeneralRegFileToBRAM.scala 139:20]
    else :
      node _T_7 = not(axi_awready) @[AXIWrapperChiselGeneralRegFileToBRAM.scala 141:14]
      node _T_8 = and(_T_7, io.S_AXI_AWVALID) @[AXIWrapperChiselGeneralRegFileToBRAM.scala 141:27]
      node _T_9 = and(_T_8, io.S_AXI_WVALID) @[AXIWrapperChiselGeneralRegFileToBRAM.scala 141:47]
      node _T_10 = and(_T_9, aw_en) @[AXIWrapperChiselGeneralRegFileToBRAM.scala 141:66]
      when _T_10 : @[AXIWrapperChiselGeneralRegFileToBRAM.scala 141:76]
        axi_awaddr <= io.S_AXI_AWADDR @[AXIWrapperChiselGeneralRegFileToBRAM.scala 142:24]
    node _T_11 = bits(lowActiveReset, 0, 0) @[AXIWrapperChiselGeneralRegFileToBRAM.scala 150:25]
    when _T_11 : @[AXIWrapperChiselGeneralRegFileToBRAM.scala 150:33]
      axi_wready <= UInt<1>("h0") @[AXIWrapperChiselGeneralRegFileToBRAM.scala 151:20]
    else :
      node _T_12 = not(axi_wready) @[AXIWrapperChiselGeneralRegFileToBRAM.scala 153:14]
      node _T_13 = and(_T_12, io.S_AXI_WVALID) @[AXIWrapperChiselGeneralRegFileToBRAM.scala 153:26]
      node _T_14 = and(_T_13, io.S_AXI_AWVALID) @[AXIWrapperChiselGeneralRegFileToBRAM.scala 153:45]
      node _T_15 = and(_T_14, aw_en) @[AXIWrapperChiselGeneralRegFileToBRAM.scala 153:65]
      when _T_15 : @[AXIWrapperChiselGeneralRegFileToBRAM.scala 153:75]
        axi_wready <= UInt<1>("h1") @[AXIWrapperChiselGeneralRegFileToBRAM.scala 158:24]
      else :
        axi_wready <= UInt<1>("h0") @[AXIWrapperChiselGeneralRegFileToBRAM.scala 160:24]
    node _slv_reg_wren_T = and(axi_wready, io.S_AXI_WVALID) @[AXIWrapperChiselGeneralRegFileToBRAM.scala 171:32]
    node _slv_reg_wren_T_1 = and(_slv_reg_wren_T, axi_awready) @[AXIWrapperChiselGeneralRegFileToBRAM.scala 171:51]
    node _slv_reg_wren_T_2 = and(_slv_reg_wren_T_1, io.S_AXI_AWVALID) @[AXIWrapperChiselGeneralRegFileToBRAM.scala 171:66]
    slv_reg_wren <= _slv_reg_wren_T_2 @[AXIWrapperChiselGeneralRegFileToBRAM.scala 171:18]
    node _T_16 = bits(lowActiveReset, 0, 0) @[AXIWrapperChiselGeneralRegFileToBRAM.scala 173:25]
    when _T_16 : @[AXIWrapperChiselGeneralRegFileToBRAM.scala 173:33]
      io_valid_reg <= UInt<1>("h0") @[AXIWrapperChiselGeneralRegFileToBRAM.scala 174:22]
      startAddr_reg <= UInt<1>("h0") @[AXIWrapperChiselGeneralRegFileToBRAM.scala 175:23]
      resultAddr_reg <= UInt<1>("h0") @[AXIWrapperChiselGeneralRegFileToBRAM.scala 176:24]
    else :
      node _T_17 = bits(axi_awaddr, 9, 2) @[AXIWrapperChiselGeneralRegFileToBRAM.scala 178:26]
      node _T_18 = eq(UInt<1>("h0"), _T_17) @[AXIWrapperChiselGeneralRegFileToBRAM.scala 178:68]
      when _T_18 : @[AXIWrapperChiselGeneralRegFileToBRAM.scala 178:68]
        when slv_reg_wren : @[AXIWrapperChiselGeneralRegFileToBRAM.scala 180:36]
          startAddr_reg <= io.S_AXI_WDATA @[AXIWrapperChiselGeneralRegFileToBRAM.scala 181:35]
      else :
        node _T_19 = eq(UInt<1>("h1"), _T_17) @[AXIWrapperChiselGeneralRegFileToBRAM.scala 178:68]
        when _T_19 : @[AXIWrapperChiselGeneralRegFileToBRAM.scala 178:68]
          when slv_reg_wren : @[AXIWrapperChiselGeneralRegFileToBRAM.scala 185:36]
            resultAddr_reg <= io.S_AXI_WDATA @[AXIWrapperChiselGeneralRegFileToBRAM.scala 186:36]
        else :
          node _T_20 = eq(UInt<2>("h2"), _T_17) @[AXIWrapperChiselGeneralRegFileToBRAM.scala 178:68]
          when _T_20 : @[AXIWrapperChiselGeneralRegFileToBRAM.scala 178:68]
            when slv_reg_wren : @[AXIWrapperChiselGeneralRegFileToBRAM.scala 190:36]
              io_valid_reg <= io.S_AXI_WDATA @[AXIWrapperChiselGeneralRegFileToBRAM.scala 191:34]
    node _T_21 = bits(lowActiveReset, 0, 0) @[AXIWrapperChiselGeneralRegFileToBRAM.scala 202:25]
    when _T_21 : @[AXIWrapperChiselGeneralRegFileToBRAM.scala 202:33]
      axi_bvalid <= UInt<1>("h0") @[AXIWrapperChiselGeneralRegFileToBRAM.scala 203:20]
      axi_bresp <= UInt<1>("h0") @[AXIWrapperChiselGeneralRegFileToBRAM.scala 204:20]
    else :
      node _T_22 = and(axi_awready, io.S_AXI_AWVALID) @[AXIWrapperChiselGeneralRegFileToBRAM.scala 206:26]
      node _T_23 = not(axi_bvalid) @[AXIWrapperChiselGeneralRegFileToBRAM.scala 206:49]
      node _T_24 = and(_T_22, _T_23) @[AXIWrapperChiselGeneralRegFileToBRAM.scala 206:46]
      node _T_25 = and(_T_24, axi_wready) @[AXIWrapperChiselGeneralRegFileToBRAM.scala 206:61]
      node _T_26 = and(_T_25, io.S_AXI_WVALID) @[AXIWrapperChiselGeneralRegFileToBRAM.scala 206:75]
      when _T_26 : @[AXIWrapperChiselGeneralRegFileToBRAM.scala 206:95]
        axi_bvalid <= UInt<1>("h1") @[AXIWrapperChiselGeneralRegFileToBRAM.scala 207:24]
        axi_bresp <= UInt<1>("h0") @[AXIWrapperChiselGeneralRegFileToBRAM.scala 208:24]
      else :
        node _T_27 = and(io.S_AXI_BREADY, axi_bvalid) @[AXIWrapperChiselGeneralRegFileToBRAM.scala 210:34]
        when _T_27 : @[AXIWrapperChiselGeneralRegFileToBRAM.scala 210:49]
          axi_bvalid <= UInt<1>("h0") @[AXIWrapperChiselGeneralRegFileToBRAM.scala 211:28]
    node _T_28 = bits(lowActiveReset, 0, 0) @[AXIWrapperChiselGeneralRegFileToBRAM.scala 222:25]
    when _T_28 : @[AXIWrapperChiselGeneralRegFileToBRAM.scala 222:33]
      axi_arready <= UInt<1>("h0") @[AXIWrapperChiselGeneralRegFileToBRAM.scala 223:21]
      axi_araddr <= UInt<1>("h0") @[AXIWrapperChiselGeneralRegFileToBRAM.scala 224:21]
    else :
      node _T_29 = not(axi_arready) @[AXIWrapperChiselGeneralRegFileToBRAM.scala 226:14]
      node _T_30 = and(_T_29, io.S_AXI_ARVALID) @[AXIWrapperChiselGeneralRegFileToBRAM.scala 226:27]
      when _T_30 : @[AXIWrapperChiselGeneralRegFileToBRAM.scala 226:48]
        axi_arready <= UInt<1>("h1") @[AXIWrapperChiselGeneralRegFileToBRAM.scala 228:25]
        axi_araddr <= io.S_AXI_ARADDR @[AXIWrapperChiselGeneralRegFileToBRAM.scala 229:25]
      else :
        axi_arready <= UInt<1>("h0") @[AXIWrapperChiselGeneralRegFileToBRAM.scala 231:25]
    node _T_31 = bits(lowActiveReset, 0, 0) @[AXIWrapperChiselGeneralRegFileToBRAM.scala 243:25]
    when _T_31 : @[AXIWrapperChiselGeneralRegFileToBRAM.scala 243:33]
      axi_rvalid <= UInt<1>("h0") @[AXIWrapperChiselGeneralRegFileToBRAM.scala 244:20]
      axi_rresp <= UInt<1>("h0") @[AXIWrapperChiselGeneralRegFileToBRAM.scala 245:20]
    else :
      node _T_32 = and(axi_arready, io.S_AXI_ARVALID) @[AXIWrapperChiselGeneralRegFileToBRAM.scala 247:26]
      node _T_33 = not(axi_rvalid) @[AXIWrapperChiselGeneralRegFileToBRAM.scala 247:49]
      node _T_34 = and(_T_32, _T_33) @[AXIWrapperChiselGeneralRegFileToBRAM.scala 247:46]
      when _T_34 : @[AXIWrapperChiselGeneralRegFileToBRAM.scala 247:62]
        axi_rvalid <= UInt<1>("h1") @[AXIWrapperChiselGeneralRegFileToBRAM.scala 248:24]
        axi_rresp <= UInt<1>("h0") @[AXIWrapperChiselGeneralRegFileToBRAM.scala 249:24]
      else :
        node _T_35 = and(axi_rvalid, io.S_AXI_RREADY) @[AXIWrapperChiselGeneralRegFileToBRAM.scala 250:32]
        when _T_35 : @[AXIWrapperChiselGeneralRegFileToBRAM.scala 250:52]
          axi_rvalid <= UInt<1>("h0") @[AXIWrapperChiselGeneralRegFileToBRAM.scala 251:24]
    node _T_36 = bits(lowActiveReset, 0, 0) @[AXIWrapperChiselGeneralRegFileToBRAM.scala 255:25]
    when _T_36 : @[AXIWrapperChiselGeneralRegFileToBRAM.scala 255:33]
      io_ready_reg <= UInt<1>("h0") @[AXIWrapperChiselGeneralRegFileToBRAM.scala 256:22]
    else :
      node _io_ready_reg_T = mux(modRegFileToBRAM.io.ready, UInt<1>("h1"), io_ready_reg) @[AXIWrapperChiselGeneralRegFileToBRAM.scala 258:28]
      io_ready_reg <= _io_ready_reg_T @[AXIWrapperChiselGeneralRegFileToBRAM.scala 258:22]
    node _slv_reg_rden_T = and(axi_arready, io.S_AXI_ARVALID) @[AXIWrapperChiselGeneralRegFileToBRAM.scala 264:33]
    node _slv_reg_rden_T_1 = not(axi_rvalid) @[AXIWrapperChiselGeneralRegFileToBRAM.scala 264:54]
    node _slv_reg_rden_T_2 = and(_slv_reg_rden_T, _slv_reg_rden_T_1) @[AXIWrapperChiselGeneralRegFileToBRAM.scala 264:52]
    slv_reg_rden <= _slv_reg_rden_T_2 @[AXIWrapperChiselGeneralRegFileToBRAM.scala 264:18]
    node readAddr = bits(axi_araddr, 9, 2) @[AXIWrapperChiselGeneralRegFileToBRAM.scala 266:30]
    node _reg_data_out_T = eq(readAddr, UInt<2>("h3")) @[AXIWrapperChiselGeneralRegFileToBRAM.scala 267:34]
    node _reg_data_out_T_1 = cat(UInt<1>("h0"), io_ready_reg) @[Cat.scala 33:92]
    node _reg_data_out_T_2 = mux(_reg_data_out_T, _reg_data_out_T_1, reg_data_out) @[AXIWrapperChiselGeneralRegFileToBRAM.scala 267:24]
    reg_data_out <= _reg_data_out_T_2 @[AXIWrapperChiselGeneralRegFileToBRAM.scala 267:18]
    node _T_37 = bits(lowActiveReset, 0, 0) @[AXIWrapperChiselGeneralRegFileToBRAM.scala 270:25]
    when _T_37 : @[AXIWrapperChiselGeneralRegFileToBRAM.scala 270:33]
      axi_rdata <= UInt<1>("h0") @[AXIWrapperChiselGeneralRegFileToBRAM.scala 271:19]
    else :
      when slv_reg_rden : @[AXIWrapperChiselGeneralRegFileToBRAM.scala 276:28]
        axi_rdata <= reg_data_out @[AXIWrapperChiselGeneralRegFileToBRAM.scala 277:23]


