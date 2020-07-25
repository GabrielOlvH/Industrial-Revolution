package me.steven.indrev.utils

import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction

class MutableBlockPos(private var mX: Int, var mY: Int, var mZ: Int) : BlockPos(mX, mY, mZ) {
    constructor(pos: BlockPos) : this (pos.x, pos.y, pos.z)
    override fun offset(direction: Direction, amount: Int) : MutableBlockPos {
        mX += direction.offsetX * amount
        mY += direction.offsetY * amount
        mZ += direction.offsetZ * amount
        return this
    }

    override fun offset(direction: Direction): MutableBlockPos = offset(direction, 1)

    override fun getX(): Int = mX
    override fun getY(): Int = mY
    override fun getZ(): Int = mZ
}