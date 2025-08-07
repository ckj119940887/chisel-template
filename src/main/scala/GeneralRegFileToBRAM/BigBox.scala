package GeneralRegFileToBRAM 

import chisel3._
import chisel3.util._
import chisel3.experimental._

class DataBundle extends Bundle {
    val data = UInt(32.W)
}

class Add extends Module {
    val io = IO(new Bundle{
        val in = Flipped(Decoupled(new DataBundle))
        val out = Decoupled(new DataBundle)
    })

    io.in.ready := false.B
    io.out.valid := false.B
    io.out.bits.data := 0.U

    val CP = RegInit(0.U(8.W))
    val r_x = Reg(UInt(32.W))
    val r_y = Reg(UInt(32.W))
    val r_z = Reg(UInt(32.W))
    val r_temp = Reg(UInt(32.W))

    switch(CP) {
        is(0.U) {
            r_temp := 0.U
            io.in.ready := true.B
            when(io.in.valid) {
                r_x := io.in.bits.data
                CP := 1.U
            }
        }
        is(1.U) {
            io.in.ready := true.B
            when(io.in.valid) {
                r_y := io.in.bits.data
                CP := 2.U
            }
        }
        is(2.U) {
            io.in.ready := true.B
            when(io.in.valid) {
                r_z := io.in.bits.data
                CP := 3.U
            }
        }
        is(3.U) {
            r_temp := r_x + r_y
            CP := 4.U
        }
        is(4.U) {
            r_temp := r_temp + r_z
            CP := 5.U
        }
        is(5.U) {
            io.out.bits.data := r_temp
            io.out.valid := true.B
            CP := Mux(io.out.ready, 0.U, CP)
        }
    }
}

class Test extends Module {
    val io = IO(new Bundle{
        val in = Flipped(Decoupled(new DataBundle))
        val out = Decoupled(new DataBundle)
    })

    io.in.ready := false.B
    io.out.valid := false.B
    io.out.bits.data := 0.U

    val CP = RegInit(0.U(8.W))
    val r_result = Reg(UInt(32.W))

    switch(CP) {
        is(0.U) {
            io.in.ready := true.B
            when(io.in.valid) {
                CP := 1.U
            }
        }
        is(1.U) {
            io.out.bits.data := 1.U
            io.out.valid := true.B
            CP := Mux(io.out.ready, 2.U, CP)
        }
        is(2.U) {
            io.out.bits.data := 2.U
            io.out.valid := true.B
            CP := Mux(io.out.ready, 3.U, CP)
        }
        is(3.U) {
            io.out.bits.data := 3.U
            io.out.valid := true.B
            CP := Mux(io.out.ready, 4.U, CP)
        }
        is(4.U) {
            io.in.ready := true.B
            when(io.in.valid) {
                r_result := io.in.bits.data
                CP := 5.U
            }
        }
        is(5.U) {
            io.out.valid := true.B
            io.out.bits.data := r_result
            when(io.out.ready) {
                CP := 0.U
            }
        }
    }
}

class BigBox extends Module {
    val io = IO(new Bundle{
        val out = Decoupled(new DataBundle)
    })

    io.out.valid := false.B
    io.out.bits.data := 0.U

    val CP = RegInit(0.U(8.W))

    val r_add_in_data = Reg(new DataBundle)
    val r_add_in_valid = RegInit(false.B)
    val r_add_in_ready = RegInit(false.B)
    val r_add_out_data = Reg(new DataBundle)
    val r_add_out_valid = RegInit(false.B)
    val r_add_out_ready = RegInit(false.B)
    val r_test_in_data = Reg(new DataBundle)
    val r_test_in_valid = RegInit(false.B)
    val r_test_in_ready = RegInit(false.B)
    val r_test_out_data = Reg(new DataBundle)
    val r_test_out_valid = RegInit(false.B)
    val r_test_out_ready = RegInit(false.B)

    r_add_in_ready := false.B
    r_add_in_valid := false.B
    r_add_out_ready := false.B
    r_add_out_valid := false.B
    r_test_in_ready := false.B
    r_test_in_valid := false.B
    r_test_out_ready := false.B
    r_test_out_valid := false.B

    val add = Module(new Add)
    val test = Module(new Test)

    add.io.in.bits.data := r_add_in_data.data
    add.io.in.valid := r_add_in_valid 
    r_add_in_ready := add.io.in.ready

    r_add_out_data.data := add.io.out.bits.data
    add.io.out.ready := r_add_out_ready 
    r_add_out_valid := add.io.out.valid

    test.io.in.bits.data := r_test_in_data.data
    test.io.in.valid := r_test_in_valid
    r_test_in_ready := test.io.in.ready

    r_test_out_data.data := test.io.out.bits.data
    test.io.out.ready := r_test_out_ready 
    r_test_out_valid := test.io.out.valid

    switch(CP) {
        is(0.U) {
            io.out.valid := true.B
            CP := Mux(io.out.ready, 1.U, CP)
        }
        is(1.U) {
            r_test_in_valid := true.B
            when(r_test_in_ready) {
                CP := 2.U
            }
        }
        is(2.U) {
            r_test_out_ready := true.B
            when(r_test_out_valid) {
                CP := 3.U
                r_test_out_ready := false.B
            }
        }
        is(3.U) {
            r_add_in_data.data := r_test_out_data.data
            r_add_in_valid := true.B
            when(r_add_in_ready) {
                CP := 4.U
            }
        }
        is(4.U) {
            r_test_out_ready := true.B
            when(r_test_out_valid) {
                CP  := 5.U
                r_test_out_ready := false.B
            }
        }
        is(5.U) {
            r_add_in_data.data := r_test_out_data.data
            r_add_in_valid := true.B
            when(r_add_in_ready) {
                CP := 6.U
            }
        }
        is(6.U) {
            r_test_out_ready := true.B
            when(r_test_out_valid) {
                CP := 7.U
                r_test_out_ready := false.B
            }
        }
        is(7.U) {
            r_add_in_data.data := r_test_out_data.data
            r_add_in_valid := true.B
            when(r_add_in_ready) {
                CP := 8.U
            }
        }
        is(8.U) {
            r_add_out_ready := true.B
            when(r_add_out_valid) {
                CP := 9.U
            }
        }
        is(9.U) {
            r_test_in_data.data := r_add_out_data.data
            r_test_in_valid := true.B
            when(r_test_in_ready) {
                CP := 10.U
            }
        }
        is(10.U) {
            r_test_out_ready := true.B
            when(r_test_out_valid) {
                CP := 11.U
            }
        }
        is(11.U) {
            io.out.bits.data := r_test_out_data.data
            io.out.valid := true.B
            when(io.out.ready) {
                CP := 0.U
            }
        }
    }
}