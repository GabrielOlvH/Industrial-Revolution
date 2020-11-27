package me.steven.indrev.utils

import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.blockentities.storage.BatteryBlockEntity
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.math.Direction

enum class WrenchConfigurationType(val title: Text, vararg val validModes: TransferMode = TransferMode.values()) {
    ITEM(TranslatableText("item.indrev.wrench.item")) {
        override fun isAvailableFor(blockEntity: MachineBlockEntity<*>): Boolean
                = blockEntity.inventoryComponent != null
                && blockEntity.inventoryComponent!!.inventory.inputSlots.isNotEmpty()
                && blockEntity.inventoryComponent!!.inventory.outputSlots.isNotEmpty()

        override fun getConfig(blockEntity: MachineBlockEntity<*>): MutableMap<Direction, TransferMode> = blockEntity.inventoryComponent!!.itemConfig
    },
    FLUID(TranslatableText("item.indrev.wrench.fluid")) {
        override fun isAvailableFor(blockEntity: MachineBlockEntity<*>): Boolean = blockEntity.fluidComponent != null

        override fun getConfig(blockEntity: MachineBlockEntity<*>): MutableMap<Direction, TransferMode>
                = blockEntity.fluidComponent!!.transferConfig
    },
    ENERGY(TranslatableText("item.indrev.wrench.energy"), TransferMode.INPUT, TransferMode.OUTPUT, TransferMode.NONE) {
        override fun isAvailableFor(blockEntity: MachineBlockEntity<*>): Boolean = blockEntity is BatteryBlockEntity

        override fun getConfig(blockEntity: MachineBlockEntity<*>): MutableMap<Direction, TransferMode>
                = (blockEntity as BatteryBlockEntity).transferConfig
    };

    abstract fun isAvailableFor(blockEntity: MachineBlockEntity<*>): Boolean

    abstract fun getConfig(blockEntity: MachineBlockEntity<*>): MutableMap<Direction, TransferMode>

    fun next(): WrenchConfigurationType {
        return when (this) {
            ITEM -> FLUID
            FLUID -> ENERGY
            ENERGY -> ITEM
        }
    }

    fun next(available: Array<WrenchConfigurationType>): WrenchConfigurationType {
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
        fun getTypes(blockEntity: MachineBlockEntity<*>) = values().filter { type -> type.isAvailableFor(blockEntity) }.toTypedArray()
    }
}