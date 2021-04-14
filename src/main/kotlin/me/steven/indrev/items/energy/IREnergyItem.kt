package me.steven.indrev.items.energy

import me.shedaniel.cloth.api.durability.bar.DurabilityBarItem
import me.steven.indrev.utils.energyOf
import net.minecraft.item.ItemStack

interface IREnergyItem : DurabilityBarItem {
    override fun getDurabilityBarProgress(stack: ItemStack?): Double {
        val energyIo = energyOf(stack) ?: return 0.0
        return 1.0 - (energyIo.energy / energyIo.energyCapacity)
    }

    override fun hasDurabilityBar(stack: ItemStack?): Boolean = (energyOf(stack)?.energy ?: 0.0) > 0

    override fun getDurabilityBarColor(stack: ItemStack?): Int {
        val durability = 1 - getDurabilityBarProgress(stack)
        val r = (149 - ((149 - 55) * durability).toInt()) and 255 shl 16
        val g = (122 - ((122) * durability).toInt()) shl 8
        val b = 255
        return r or g or b
    }
}