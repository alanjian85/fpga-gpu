// Copyright (C) 2023 Alan Jian (alanjian85@outlook.com)
// SPDX-License-Identifier: MIT

import chisel3._

class Display extends Module {
  val io = IO(new Bundle {
    val fbIdx = Input(UInt(FbSwapper.fbIdxWidth.W))
    val vram  = new RdAxi(Vram.addrWidth, Vram.dataWidth)
    val vga   = new VgaExt
  })

  val vgaSignal = Module(new VgaSignal)
  val vgaPos    = RegNext(vgaSignal.io.nextPos)
  vgaSignal.io.currPos := vgaPos
  io.vga               := vgaSignal.io.vga

  val fbReader = Module(new FbReader((in, row) => {
    val ditherer = Module(new Ditherer)
    ditherer.io.in  := in
    ditherer.io.row := row
    ditherer.io.out
  }))
  fbReader.io.fbIdx := io.fbIdx
  fbReader.io.pos   := vgaSignal.io.nextPos
  io.vram <> fbReader.io.vram

  vgaSignal.io.pix := fbReader.io.pix(vgaPos.x % fbReader.nrBanks.U)
}