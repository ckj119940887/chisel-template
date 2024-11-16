package FieldAccess

import chisel3._
import chisel3.util._

class FieldAccessBar extends Bundle {
  val i = SInt(32.W) 
  val x = Vec(20,  Bool()) 
  val f = new FieldAccessFoo 
}