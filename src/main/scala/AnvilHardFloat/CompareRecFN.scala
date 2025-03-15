
/*============================================================================

This Chisel source file is part of a pre-release version of the HardFloat IEEE
Floating-Point Arithmetic Package, by John R. Hauser (with some contributions
from Yunsup Lee and Andrew Waterman, mainly concerning testing).

Copyright 2010, 2011, 2012, 2013, 2014, 2015, 2016 The Regents of the
University of California.  All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

 1. Redistributions of source code must retain the above copyright notice,
    this list of conditions, and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions, and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. Neither the name of the University nor the names of its contributors may
    be used to endorse or promote products derived from this software without
    specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS "AS IS", AND ANY
EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, ARE
DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

=============================================================================*/

package AnvilHardFloat

import chisel3._

class CompareRecFN(val expWidth: Int, val sigWidth: Int) extends RawModule
{
    val io = IO(new Bundle {
        val a = Input(Bits((expWidth + sigWidth + 1).W))
        val b = Input(Bits((expWidth + sigWidth + 1).W))
        val signaling = Input(Bool())
        val lt = Output(Bool())
        val eq = Output(Bool())
        val gt = Output(Bool())
        val exceptionFlags = Output(Bits(5.W))
    })

    val rawA = Module(new AnvilRawFloatFromRecFN(expWidth, sigWidth))
    rawA.io.in := io.a

    val rawB = Module(new AnvilRawFloatFromRecFN(expWidth, sigWidth))
    rawB.io.in := io.b

    val ordered = ! rawA.io.out.isNaN && ! rawB.io.out.isNaN
    val bothInfs  = rawA.io.out.isInf  && rawB.io.out.isInf
    val bothZeros = rawA.io.out.isZero && rawB.io.out.isZero
    val eqExps = (rawA.io.out.sExp === rawB.io.out.sExp)
    val common_ltMags =
        (rawA.io.out.sExp < rawB.io.out.sExp) || (eqExps && (rawA.io.out.sig < rawB.io.out.sig))
    val common_eqMags = eqExps && (rawA.io.out.sig === rawB.io.out.sig)

    val ordered_lt =
        ! bothZeros &&
            ((rawA.io.out.sign && ! rawB.io.out.sign) ||
                 (! bothInfs &&
                      ((rawA.io.out.sign && ! common_ltMags && ! common_eqMags) ||
                           (! rawB.io.out.sign && common_ltMags))))
    val ordered_eq =
        bothZeros || ((rawA.io.out.sign === rawB.io.out.sign) && (bothInfs || common_eqMags))

    val invalid =
        isSigNaNRawFloat(rawA.io.out) || isSigNaNRawFloat(rawB.io.out) ||
            (io.signaling && ! ordered)

    io.lt := ordered && ordered_lt
    io.eq := ordered && ordered_eq
    io.gt := ordered && ! ordered_lt && ! ordered_eq
    io.exceptionFlags := invalid ## 0.U(4.W)
}

