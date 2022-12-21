package me.steven.indrev.utils

import net.minecraft.util.math.Direction

@JvmInline
value class Directions(val value: Int = 0) {

    constructor(vararg dirs: Direction) : this(dirs.map { it.id }.reduce { acc, i -> acc or (1 shl i) })

    fun contains(dir: Direction): Boolean = value and (1 shl dir.id) != 0

    fun with(dir: Direction): Directions = Directions(value or (1 shl dir.id))

    fun without(dir: Direction): Directions = Directions(value xor (1 shl dir.id))

    fun getDirections(): List<Direction> = Direction.values().filter(this::contains)

    companion object {
        val ALL = Directions(*Direction.values())
    }
}