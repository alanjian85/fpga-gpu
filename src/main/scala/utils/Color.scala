// Copyright (C) 2023 Alan Jian (alanjian85@outlook.com)
// SPDX-License-Identifier: MIT

import chisel3._
import chisel3.util._
import scala.math._

class RGBChanWidth(val rWidth: Int, val gWidth: Int, val bWidth: Int)

class RGB(compWidth: RGBChanWidth, width: Int, alignedWidth: Int) extends Bundle {
  val rWidth = compWidth.rWidth
  val gWidth = compWidth.gWidth
  val bWidth = compWidth.bWidth

  val r = UInt(rWidth.W)
  val g = UInt(gWidth.W)
  val b = UInt(bWidth.W)

  def encode() = b ## g ## r

  def encodeAligned() = 0.U((alignedWidth - width).W) ## b ## g ## r

  def map(f: (UInt) => UInt) = {
    val res = Wire(new RGB(compWidth, width, alignedWidth))
    res.r := f(r)
    res.g := f(g)
    res.b := f(b)
    res
  }
}

class RGBFactory(val rWidth: Int, val gWidth: Int, val bWidth: Int) {
  val width        = rWidth + gWidth + bWidth
  val alignedWidth = pow(2, log2Up(width)).toInt
  val nrBytes      = alignedWidth / 8

  def apply() = new RGB(new RGBChanWidth(rWidth, gWidth, bWidth), width, alignedWidth)

  def decode(pix: UInt) = {
    val res = Wire(new RGB(new RGBChanWidth(rWidth, gWidth, bWidth), width, alignedWidth))
    res.r := pix(rWidth - 1, 0)
    res.g := pix(rWidth + gWidth - 1, rWidth)
    res.b := pix(rWidth + gWidth + bWidth - 1, rWidth + gWidth)
    res
  }
}
