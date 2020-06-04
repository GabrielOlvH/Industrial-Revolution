package me.steven.indrev.blocks.nuclear

import net.minecraft.util.StringIdentifiable
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3i

enum class NuclearCoreSide(private val value: String, val offset: Vec3i) : StringIdentifiable {
    DOWN("down", Vec3i(0, -1, 0)),
    UP("up", Vec3i(0, 1, 0)),
    NORTH("north", Vec3i(0, 0, -1)),
    SOUTH("south", Vec3i(0, 0, 1)),
    WEST("west", Vec3i(-1, 0, 0)),
    EAST("east", Vec3i(1, 0, 0)),
    UNKNOWN("unknown", Vec3i(0, 0, 0));

    override fun asString(): String = value

    companion object {
        private val VALUES = values()
        fun fromMinecraft(e: Enum<*>?): NuclearCoreSide = if (e == null) UNKNOWN else VALUES[e.ordinal]
        fun BlockPos.offset(nuclearCoreSide: NuclearCoreSide): BlockPos {
            return this.add(nuclearCoreSide.offset)
        }
    }
}