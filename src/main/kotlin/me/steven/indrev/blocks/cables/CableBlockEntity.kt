package me.steven.indrev.blocks.cables

import me.steven.indrev.blocks.BasicMachineBlockEntity
import me.steven.indrev.content.MachineRegistry
import net.minecraft.container.ArrayPropertyDelegate
import net.minecraft.container.PropertyDelegate

class CableBlockEntity : BasicMachineBlockEntity(MachineRegistry.CABLE_BLOCK_ENTITY, 10.0) {
    override fun createDelegate(): PropertyDelegate = ArrayPropertyDelegate(2)

    override fun getMaxInput(): Double = 32.0

    override fun getMaxOutput(): Double = 32.0
}