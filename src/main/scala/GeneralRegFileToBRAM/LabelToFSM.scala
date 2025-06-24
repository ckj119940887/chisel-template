package GeneralRegFileToBRAM 

import chisel3._
import chisel3.util._
import chisel3.experimental._

class LabelToFSM(val numOfStates: Int, val cpMax: Int) extends Module {
    val cpWidth: Int = (numOfStates / cpMax) + (if(numOfStates % cpMax == 0) 0 else 1)

    val io = IO(new Bundle{
        val start           = Input(Bool())
        val label           = Input(UInt(log2Up(numOfStates).W))
        val originalCpIndex = Input(UInt(log2Up(cpWidth).W))
        val valid           = Output(Bool())
        val isSameCpIndex   = Output(Bool())
        val cpIndex         = Output(UInt(log2Up(cpWidth).W))
        val stateIndex      = Output(UInt(log2Up(cpMax).W)) 
    })

    val sIdle :: sShift :: sCompare :: sEnd :: Nil = Enum(4)
    val r_originalCpIndex = Reg(UInt(log2Up(cpWidth).W))
    val r_state           = RegInit(sIdle)
    val r_label           = Reg(UInt(log2Up(numOfStates).W))
    val r_cpIndex         = Reg(UInt(log2Up(cpWidth).W))
    val r_stateIndex      = Reg(UInt(log2Up(cpMax).W))
    val r_isSameCpIndex   = Reg(Bool())

    switch(r_state) {
        is(sIdle) {
            r_state           := Mux(io.start, sShift, r_state)
            r_label           := io.label
            r_originalCpIndex := io.originalCpIndex
        }
        is(sShift) {
            r_cpIndex    := r_label >> log2Up(cpMax).U
            r_stateIndex := r_label(log2Up(cpMax) - 1, 0)
            r_state      := sCompare
        }
        is(sCompare) {
            r_isSameCpIndex := r_cpIndex === r_originalCpIndex
            r_state         := sEnd
        }
        is(sEnd) {
            r_state := sIdle
        }
    }

    io.cpIndex       := r_cpIndex
    io.isSameCpIndex := r_isSameCpIndex
    io.stateIndex    := r_stateIndex
    io.valid         := r_state === sEnd
}