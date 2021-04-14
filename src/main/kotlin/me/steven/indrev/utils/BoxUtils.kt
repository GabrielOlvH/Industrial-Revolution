package me.steven.indrev.utils

import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box

inline fun Box.any(f: (Int, Int, Int) -> Boolean): Boolean {
    for (x in minX.toInt()..maxX.toInt())
        for (y in minY.toInt()..maxY.toInt())
            for (z in minZ.toInt()..maxZ.toInt())
                if (f(x, y, z)) return true
    return false
}

inline fun Box.forEach(f: (Int, Int, Int) -> Unit) {
    for (x in minX.toInt() until maxX.toInt())
        for (y in minY.toInt() until maxY.toInt())
            for (z in minZ.toInt() until maxZ.toInt())
                f(x, y, z)
}

inline fun <T> Box.map(f: (Int, Int, Int) -> T): MutableList<T> {
    val list = ArrayList<T>((xLength * yLength * zLength).toInt())
    for (x in minX.toInt() until maxX.toInt())
        for (y in minY.toInt() until maxY.toInt())
            for (z in minZ.toInt() until maxZ.toInt())
                list.add(f(x, y, z))
    return list
}

operator fun Box.contains(pos: BlockPos): Boolean {
    return contains(pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble())
}

inline fun Box.firstOrNull(f: (Int, Int, Int) -> Boolean): BlockPos? {
    for (x in minX.toInt()..maxX.toInt())
        for (y in minY.toInt()..maxY.toInt())
            for (z in minZ.toInt()..maxZ.toInt())
                if (f(x, y, z)) return BlockPos(x, y, z)
    return null
}