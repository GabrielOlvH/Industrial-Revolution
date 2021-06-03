package me.steven.indrev.items.energy

import me.steven.indrev.utils.energyOf
import net.minecraft.item.ItemStack
import kotlin.math.roundToInt

interface IREnergyItem {
    fun getDurabilityBarProgress(stack: ItemStack?): Int {
        val energyIo = energyOf(stack) ?: return 0
        return (13.0f - (energyIo.energyCapacity - energyIo.energy) * 13.0f / energyIo.energyCapacity).roundToInt()
    }

    fun hasDurabilityBar(stack: ItemStack?): Boolean = (energyOf(stack)?.energy ?: 0.0) > 0

    fun getDurabilityBarColor(stack: ItemStack?): Int {
        val durability = getDurabilityBarProgress(stack) / 13f
        val r = (149 - ((149 - 55) * durability)).toInt() and 255 shl 16
        val g = (122 - ((122) * durability)).toInt() shl 8
        val b = 255
        return r or g or b
    }
}