package TestArbiter 

import chisel3._
import chisel3.util._
import chisel3.experimental._

class TempTest extends Module {
    val io = IO(new Bundle{
        val in1 = Input(UInt(8.W))
        val in2 = Input(UInt(8.W))
        val out = Output(UInt(8.W))
    })

    io.out := io.in1 + io.in2

    val generalRegFilesU8 = RegInit(VecInit(Seq.fill(3)(0.U(8.W))))
    val generalRegFilesU16 = RegInit(VecInit(Seq.fill(5)(0.U(16.W))))

    generalRegFilesU8(0)  := "hFE".U
    generalRegFilesU8(1)  := 2.U
    generalRegFilesU8(2)  := 3.U
    generalRegFilesU16(0) := "h0404".U
    generalRegFilesU16(1) := "h0505".U
    generalRegFilesU16(2) := "h0606".U
    generalRegFilesU16(3) := "h0707".U
    generalRegFilesU16(4) := "h0808".U

    val parts = Seq.newBuilder[UInt]
    parts += Cat(generalRegFilesU8)
    parts += Cat(generalRegFilesU16)

    // 获取 Seq
    val partSeq = parts.result()
  
    // 最终拼接
    val bitstream_w =
      if (partSeq.nonEmpty) partSeq.reduce(Cat(_, _))
      else 0.U

    printf("bitstream: %x\n", bitstream_w)
    printf("(103,96): %x\n", bitstream_w(103,96))

}