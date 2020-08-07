package me.steven.indrev.items.energy

import me.steven.indrev.blocks.MachineBlock
import me.steven.indrev.registry.MachineRegistry
import net.minecraft.item.BlockItem
import team.reborn.energy.EnergyHolder
import team.reborn.energy.EnergyTier

class MachineBlockItem(private val machineBlock: MachineBlock, private val registry: MachineRegistry, settings: Settings) : BlockItem(machineBlock, settings), EnergyHolder {

    override fun getMaxStoredPower(): Double = registry.buffer(machineBlock.tier)

    override fun getTier(): EnergyTier = EnergyTier.HIGH
}