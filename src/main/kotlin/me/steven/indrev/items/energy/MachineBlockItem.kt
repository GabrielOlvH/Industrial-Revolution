package me.steven.indrev.items.energy

import me.steven.indrev.blocks.MachineBlock
import me.steven.indrev.config.GeneratorConfig
import me.steven.indrev.config.HeatMachineConfig
import me.steven.indrev.config.MachineConfig
import net.minecraft.item.BlockItem
import team.reborn.energy.EnergyHolder
import team.reborn.energy.EnergyTier

class MachineBlockItem(private val machineBlock: MachineBlock, settings: Settings) : BlockItem(machineBlock, settings), EnergyHolder {

    override fun getMaxStoredPower(): Double {
        return when (val config = machineBlock.config) {
            is MachineConfig -> config.maxEnergyStored
            is HeatMachineConfig -> config.maxEnergyStored
            is GeneratorConfig -> config.maxEnergyStored
            else -> 100.0
        }
    }

    override fun getTier(): EnergyTier = EnergyTier.HIGH
}