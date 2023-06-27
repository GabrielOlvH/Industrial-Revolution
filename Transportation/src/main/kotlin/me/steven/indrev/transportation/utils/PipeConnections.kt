package me.steven.indrev.transportation.utils

import net.minecraft.util.math.Direction

@JvmInline
value class PipeConnections(val value: Int = 0) {

    constructor(vararg dirs: Direction) : this(dirs.map { it.id }.reduce { acc, i -> acc or (1 shl i) })

    fun contains(dir: Direction): Boolean = value and (1 shl dir.id) != 0

    fun with(dir: Direction): PipeConnections = PipeConnections(value or (1 shl dir.id))

    fun without(dir: Direction): PipeConnections = PipeConnections(value xor (1 shl dir.id))

    inline fun forEach(block: (Direction) -> Unit) {
        Direction.values().forEach { dir ->
            if (contains(dir)) block(dir)
        }
    }

    fun getDirections(): List<Direction> = Direction.values().filter(this::contains)

    override fun toString(): String {
        return "Connections[" + getDirections() + "]"
    }

    companion object {
        val ALL = PipeConnections(*Direction.values())
    }
}