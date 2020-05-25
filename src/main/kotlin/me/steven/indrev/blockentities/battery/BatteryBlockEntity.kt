package me.steven.indrev.blockentities.battery

import me.steven.indrev.blockentities.BasicMachineBlockEntity
import me.steven.indrev.blocks.BasicMachineBlock
import me.steven.indrev.content.MachineRegistry
import net.minecraft.container.ArrayPropertyDelegate
import net.minecraft.container.PropertyDelegate
import team.reborn.energy.EnergySide

class BatteryBlockEntity : BasicMachineBlockEntity(MachineRegistry.BATTERY_BLOCK_ENTITY, 750.0) {
    override fun createDelegate(): PropertyDelegate = ArrayPropertyDelegate(2)

    override fun getMaxOutput(side: EnergySide?): Double {
        val state = this.cachedState
        return if (side != EnergySide.fromMinecraft(state[BasicMachineBlock.FACING])) 32.0 else 0.0
    }

    override fun getMaxInput(side: EnergySide?): Double {
        val state = this.cachedState
        return if (side == EnergySide.fromMinecraft(state[BasicMachineBlock.FACING])) 32.0 else 0.0
    }
}