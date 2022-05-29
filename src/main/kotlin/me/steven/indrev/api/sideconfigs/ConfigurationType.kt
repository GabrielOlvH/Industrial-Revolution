package me.steven.indrev.api.sideconfigs

import me.steven.indrev.api.machines.TransferMode
import me.steven.indrev.utils.translatable
import net.minecraft.text.Text
import me.steven.indrev.utils.translatable

enum class ConfigurationType(val title: Text, vararg val validModes: TransferMode) {
    ITEM(translatable("item.indrev.wrench.item"), TransferMode.INPUT, TransferMode.OUTPUT, TransferMode.INPUT_OUTPUT, TransferMode.INPUT_FIRST, TransferMode.INPUT_SECOND, TransferMode.NONE),
    FLUID(translatable("item.indrev.wrench.fluid"), TransferMode.INPUT, TransferMode.OUTPUT, TransferMode.INPUT_OUTPUT, TransferMode.NONE),
    ENERGY(translatable("item.indrev.wrench.energy"), TransferMode.INPUT, TransferMode.OUTPUT, TransferMode.NONE);

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
        fun getTypes(blockEntity: Configurable) = values().filter { type -> blockEntity.isConfigurable(type) && !blockEntity.isFixed(type) }.toTypedArray()
    }
}