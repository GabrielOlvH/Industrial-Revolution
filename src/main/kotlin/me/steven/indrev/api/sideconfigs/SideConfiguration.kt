package me.steven.indrev.api.sideconfigs

import me.steven.indrev.utils.TransferMode
import net.minecraft.nbt.CompoundTag
import net.minecraft.util.math.Direction
import java.util.*

data class SideConfiguration(val type: ConfigurationType, private val transferConfig: EnumMap<Direction, TransferMode> = EnumMap(Direction::class.java))
    : MutableMap<Direction, TransferMode> by transferConfig {
    init {
        Direction.values().forEach { dir -> this[dir] = TransferMode.NONE }
    }

    fun toTag(tag: CompoundTag?) {
        var transferConfigTag = tag?.getCompound("TransferConfig")
        if (tag?.contains("TransferConfig") == false) {
            transferConfigTag = CompoundTag()
            tag.put("TransferConfig", transferConfigTag)
        }
        val configTag = CompoundTag()
        forEach { (dir, mode) ->
            configTag.putString(dir.toString(), mode.toString())
        }
        transferConfigTag?.put(type.toString().toLowerCase(), configTag)
    }

    fun fromTag(tag: CompoundTag?) {
        if (tag?.contains("TransferConfig") == true) {
            val transferConfigTag = tag.getCompound("TransferConfig")
            val configTag = transferConfigTag.getCompound(type.toString().toLowerCase())
            Direction.values().forEach { dir ->
                val value = configTag.getString(dir.toString()).toUpperCase()
                if (value.isNotEmpty()) {
                    val mode = TransferMode.valueOf(value)
                    this[dir] = mode
                }
            }
        }
    }
}