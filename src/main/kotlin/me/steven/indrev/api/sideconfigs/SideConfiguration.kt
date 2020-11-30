package me.steven.indrev.api.sideconfigs

import me.steven.indrev.utils.TransferMode
import net.minecraft.nbt.CompoundTag
import net.minecraft.util.math.Direction
import java.util.*

data class SideConfiguration(private val transferConfig: EnumMap<Direction, TransferMode> = EnumMap(Direction::class.java))
    : MutableMap<Direction, TransferMode> by transferConfig {
    init {
        Direction.values().forEach { dir -> this[dir] = TransferMode.NONE }
    }

    fun toTag(tag: CompoundTag?) {
        val icTag = CompoundTag()
        forEach { (dir, mode) ->
            icTag.putString(dir.toString(), mode.toString())
        }
        tag?.put("TransferConfig", icTag)
    }

    fun fromTag(tag: CompoundTag?) {
        if (tag?.contains("TransferConfig") == true) {
            val icTag = tag.getCompound("TransferConfig")
            Direction.values().forEach { dir ->
                val value = icTag.getString(dir.toString()).toUpperCase()
                if (value.isNotEmpty()) {
                    val mode = TransferMode.valueOf(value)
                    this[dir] = mode
                }
            }
        }
    }
}