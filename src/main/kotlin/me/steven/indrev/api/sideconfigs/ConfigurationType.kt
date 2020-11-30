package me.steven.indrev.api.sideconfigs

import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.utils.TransferMode
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText

enum class ConfigurationType(val title: Text, vararg val validModes: TransferMode = TransferMode.values()) {
    ITEM(TranslatableText("item.indrev.wrench.item")),
    FLUID(TranslatableText("item.indrev.wrench.fluid")),
    ENERGY(TranslatableText("item.indrev.wrench.energy"), TransferMode.INPUT, TransferMode.OUTPUT, TransferMode.NONE);

    fun next(): ConfigurationType {
        return when (this) {
            ITEM -> FLUID
            FLUID -> ENERGY
            ENERGY -> ITEM
        }
    }

    fun next(available: Array<ConfigurationType>): ConfigurationType {
        var current = this
        for (i in values().indices) {
            val possible = current.next()
            if (available.contains(possible)) {
                return possible
            }
            current = possible
        }
        return this
    }

    companion object {
        fun getTypes(blockEntity: MachineBlockEntity<*>) = values().filter { type -> blockEntity.isConfigurable(type) && !blockEntity.isFixed(type) }.toTypedArray()
    }
}