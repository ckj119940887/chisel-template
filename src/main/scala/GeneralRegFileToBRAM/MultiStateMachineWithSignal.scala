package GeneralRegFileToBRAM 

import chisel3._
import chisel3.util._
import chisel3.experimental._

// class MultiStateMachineWithSignal extends Module {
//     val io = IO(new Bundle{
//         val start = Input(Bool())
//         val in    = Input(UInt(32.W))
//         val out   = Output(UInt(32.W))
//     })

//     val r_out = Reg(UInt(32.W))
//     io.out := r_out

//     val sIdle :: sCP0Start :: sCP0Stop :: sCP1Start :: sCP1Stop :: sCP2Start :: sCP2Stop :: sEnd :: Nil = Enum(8)
//     val ctrlCP   = RegInit(sIdle)

//     val initCPVals      = Seq(2.U(3.W), 7.U(3.W), 7.U(3.W))
//     val CP              = RegInit(VecInit(initCPVals)) //VecInit(initCPVals.map(v => RegInit(v)))
//     val initCPValidVals = Seq(true.B, false.B, false.B)
//     val CPValid         = RegInit(VecInit(initCPVals)) //VecInit(initCPValidVals.map(v => RegInit(v)))
//     val CPIdxNext       = RegInit(VecInit(Seq.fill(3)(0.U(2.W))))
//     val CPStateNext     = RegInit(VecInit(Seq.fill(3)(7.U(3.W))))
//     val CPStateNextQ    = RegInit(VecInit(Seq.fill(3)(7.U(3.W))))
    
//     switch(ctrlCP) {
//         is(sIdle) {
//             ctrlCP := Mux(CPValid(0.U), sCP0Start, sIdle) |
//                       Mux(CPValid(1.U), sCP1Start, sIdle) |
//                       Mux(CPValid(2.U), sCP2Start, sIdle) 
//         }
//         is(sCP0Start) {
//             ctrlCP := Mux(CPValid(0.U), sCP0Start, sCP0Stop)
//         }
//         is(sCP0Stop) {
//             CPValid(CPIdxNext(0.U))      := true.B
//             CPStateNextQ(CPIdxNext(0.U)) := CPStateNext(CPIdxNext(0.U))
//         }
//         is(sCP1Start) {
//             ctrlCP := Mux(CPValid(1.U), sCP1Start, sCP1Stop)
//         }
//         is(sCP1Stop) {
//             CPValid(CPIdxNext(1.U))      := true.B
//             CPStateNextQ(CPIdxNext(1.U)) := CPStateNext(CPIdxNext(1.U))
//         }
//         is(sCP2Start) {
//             ctrlCP := Mux(CPValid(2.U), sCP2Start, sCP2Stop)
//         }
//         is(sCP2Stop) {
//             CPValid(CPIdxNext(2.U))      := true.B
//             CPStateNextQ(CPIdxNext(2.U)) := CPStateNext(CPIdxNext(2.U))
//         }
//     }

//     switch(CP(0.U)) {
//         is(2.U) {
//             CP(0.U) := Mux(io.start, 3.U, 2.U)
//         }
//         is(3.U) {
//             CP(0.U) := 4.U
//         }
//         is(4.U) {
//             CPValid(0.U) := false.B
//             CP(0.U) := 7.U
//             CPIdxNext(0.U) := 1.U
//             CPStateNext(0.U) := 3.U
//         }
//         is(7.U) {
//             CP(0.U) := CPStateNextQ(0.U)
//         }
//     }

//     switch(CP(1.U)) {
//         is(2.U) {
//             CP(1.U) := 3.U
//         }
//         is(3.U) {
//             r_out := io.in + 2.U
//             CP(1.U) := 4.U
//         }
//         is(4.U) {
//             CPValid(1.U) := false.B
//             CP(1.U) := 7.U
//             CPIdxNext(1.U) := 2.U
//             CPStateNext(1.U) := 7.U
//         }
//         is(7.U) {
//             CP(1.U) := CPStateNextQ(1.U)
//         }
//     }

//     switch(CP(2.U)) {
//         is(7.U) {
//             CP(2.U) := CPStateNextQ(2.U)
//         }
//     }
// }

class MultiStateMachineWithSignalSafe extends Module {
  val io = IO(new Bundle{
    val start = Input(Bool())
    val in    = Input(UInt(32.W))
    val out   = Output(UInt(32.W))
  })

  val r_out = Reg(UInt(32.W))
  io.out := r_out

  val sIdle :: sCP0Start :: sCP0Stop :: sCP1Start :: sCP1Stop :: sCP2Start :: sCP2Stop :: Nil = Enum(7)
  val ctrlCP = RegInit(sIdle)

  val CP       = RegInit(VecInit(Seq(2.U(3.W), 7.U(3.W), 7.U(3.W))))
  val CPNext   = Wire(Vec(3, UInt(3.W)))
  val CPValid  = RegInit(VecInit(Seq(true.B, false.B, false.B)))
  val CPIdxNext = RegInit(VecInit(Seq.fill(3)(0.U(2.W))))
  val CPStateNext = RegInit(VecInit(Seq.fill(3)(7.U(3.W))))
  val CPStateNextQ = RegInit(VecInit(Seq.fill(3)(7.U(3.W))))

  val issueCPValid = Wire(Vec(3, Bool()))
  val issueCPIdx   = Wire(Vec(3, UInt(2.W)))
  val issueCPState = Wire(Vec(3, UInt(3.W)))

  for (i <- 0 until 3) {
    issueCPValid(i) := false.B
    issueCPIdx(i)   := 0.U
    issueCPState(i) := 7.U
    CPNext(i)       := CP(i) // 默认保持不变
  }

  switch(ctrlCP) {
    is(sIdle) {
        ctrlCP := Mux(CPValid(0), sCP0Start, sIdle) |
                  Mux(CPValid(1), sCP1Start, sIdle) |
                  Mux(CPValid(2), sCP2Start, sIdle) 
    }
    is(sCP0Start) {
      ctrlCP := Mux(CPValid(0), sCP0Start, sCP0Stop)
    }
    is(sCP0Stop) {
      issueCPValid(0) := true.B
      issueCPIdx(0) := CPIdxNext(0)
      issueCPState(0) := CPStateNext(0)

      ctrlCP := sIdle
    }
    is(sCP1Start) {
      ctrlCP := Mux(CPValid(1), sCP1Start, sCP1Stop)
    }
    is(sCP1Stop) {
      issueCPValid(1) := true.B
      issueCPIdx(1) := CPIdxNext(1)
      issueCPState(1) := CPStateNext(1)

      ctrlCP := sIdle
    }
    is(sCP2Start) {
      ctrlCP := Mux(CPValid(2), sCP2Start, sCP2Stop)
    }
    is(sCP2Stop) {
      issueCPValid(2) := true.B
      issueCPIdx(2) := CPIdxNext(2)
      issueCPState(2) := CPStateNext(2)

      ctrlCP := sIdle
    }
  }

  for (i <- 0 until 3) {
    when(RegNext(issueCPValid(i))) {
      CPValid(RegNext(issueCPIdx(i))) := true.B
      CPStateNextQ(RegNext(issueCPIdx(i))) := RegNext(issueCPState(i))
    }
  }

  // CP0
  switch(CP(0)) {
    is(2.U) { CPNext(0) := Mux(io.start, 3.U, 2.U) }
    is(3.U) { CPNext(0) := 4.U }
    is(4.U) {
      CPValid(0) := false.B
      CPStateNextQ(0) := 7.U
      CPNext(0) := 7.U
      CPIdxNext(0) := 1.U
      CPStateNext(0) := 3.U
    }
    is(7.U) { CPNext(0) := CPStateNextQ(0) }
  }

  // CP1
  switch(CP(1)) {
    is(2.U) { CPNext(1) := 3.U }
    is(3.U) {
      r_out := io.in + 2.U
      CPNext(1) := 4.U
    }
    is(4.U) {
      CPValid(1) := false.B
      CPStateNextQ(1) := 7.U
      CPNext(1) := 7.U
      CPIdxNext(1) := 2.U
      CPStateNext(1) := 0.U
    }
    is(7.U) { CPNext(1) := CPStateNextQ(1) }
  }

  // CP2
  switch(CP(2)) {
    is(0.U) {
        CPValid(2) := false.B
        CPStateNextQ(2) := 7.U
        CPNext(2) := 7.U
        CPIdxNext(2) := 0.U
        CPStateNext(2) := 2.U
    }
    is(7.U) { CPNext(2) := CPStateNextQ(2) }
  }

  for (i <- 0 until 3) {
    CP(i) := RegNext(CPNext(i))
  }
}