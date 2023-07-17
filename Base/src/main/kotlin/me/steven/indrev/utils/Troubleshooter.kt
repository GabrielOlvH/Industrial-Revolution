package me.steven.indrev.utils

import me.steven.indrev.components.SyncableObject
import net.minecraft.network.PacketByteBuf
import net.minecraft.text.Text

class Troubleshooter(override val syncId: Int) : SyncableObject {
    override var isDirty: Boolean = false

    private val codes = mutableListOf<Int>()

    fun offer(code: Int) {
        if (!codes.contains(code)) {
            isDirty = true
            codes.add(code)
        }
    }

    fun solve(code: Int) {
        if (codes.contains(code)) {
            isDirty = true
            codes.remove(code)
        }
    }

    fun test(code: Int, condition: Boolean) {
        if (condition) solve(code)
        else offer(code)
    }

    fun contains(code: Int) = codes.contains(code)

    fun isEmpty() = codes.isEmpty()

    override fun toPacket(buf: PacketByteBuf) {
        buf.writeInt(codes.size)
        codes.forEach { buf.writeInt(it) }
    }

    override fun fromPacket(buf: PacketByteBuf) {
        codes.clear()
        repeat(buf.readInt()) {
            codes.add(buf.readInt())
        }
    }

    fun appendMessages(tooltip: MutableList<Text>) {
        codes.forEach { code ->
            tooltip.add(
                when (code) {
                    NO_SPACE -> Text.literal("Not enough space")
                    NO_ENERGY -> Text.literal("Not enough energy")
                    NO_TOOL -> Text.literal("No items")
                    NO_RANGE -> Text.literal("No range")
                    else -> Text.literal("Unknown issue.")
                }
            )
        }
    }

    companion object {
        const val NO_SPACE = 0
        const val NO_ENERGY = 1
        const val NO_TOOL = 2
        const val NO_RANGE = 3
    }

}