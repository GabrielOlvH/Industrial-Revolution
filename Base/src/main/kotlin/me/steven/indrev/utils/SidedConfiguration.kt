package me.steven.indrev.utils

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.PacketByteBuf
import net.minecraft.util.math.Direction
import java.util.EnumMap

class SidedConfiguration {

    private val map = EnumMap<Direction, Mode>(Direction::class.java)

    var forceDefault = true
    var autoInput = false
    var autoOutput = false

    fun getMode(dir: Direction): Mode {
        return when {
            !forceDefault -> map.getOrDefault(dir, Mode.NONE)
            dir == Direction.UP -> Mode.INPUT
            dir == Direction.DOWN -> Mode.OUTPUT
            else -> Mode.NONE
        }
    }

    fun setMode(dir: Direction, mode: Mode) = map.put(dir, mode)

    fun writeNbt(): NbtCompound {
        val nbt = NbtCompound()
        map.forEach { (dir, mode) ->
            nbt.putInt(dir.id.toString(), mode.id)
        }
        return nbt
    }

    fun readNbt(nbt: NbtCompound) {
        Direction.values().forEach { dir ->
            if (nbt.contains(dir.id.toString())) {
                val mode = Mode.BY_ID[nbt.getInt(dir.id.toString())]
                map[dir] = mode
            }
        }
    }

    fun toPacket(buf: PacketByteBuf) {
        Direction.values().forEach { dir ->
            buf.writeInt(dir.id)
            buf.writeInt(getMode(dir).id)
        }
    }

    fun fromPacket(buf: PacketByteBuf) {
        repeat(6) {
            val dirId = buf.readInt()
            val modeId = buf.readInt()
            val dir = Direction.byId(dirId)
            val mode = Mode.BY_ID[modeId]
            map[dir] = mode
        }
    }

    enum class Mode(val id: Int, val allowInput: Boolean, val allowOutput: Boolean) {
        NONE(-1, false, false),
        INPUT(0, true, false),
        OUTPUT(1, false, true),
        INPUT_OUTPUT(2, true, true);

        fun next(): Mode = BY_ID.getOrDefault(id + 1, NONE)

        companion object {
            val BY_ID = Int2ObjectOpenHashMap<Mode>()
            init {
                Mode.values().forEach { mode -> BY_ID[mode.id] = mode }
            }
        }
    }
}