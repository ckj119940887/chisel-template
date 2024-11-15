package FieldAccess

import chisel3._
import chisel3.util._

class FieldAccessBar extends Bundle {
  val i = UInt(32.W) 
  val x = Vec(20,  Bool()) 
  val f = new FieldAccessFoo 
}