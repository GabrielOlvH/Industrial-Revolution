package me.steven.indrev.items.energy

import me.steven.indrev.blocks.CableBlock
import me.steven.indrev.blocks.MachineBlock
import me.steven.indrev.config.GeneratorConfig
import me.steven.indrev.config.HeatMachineConfig
import me.steven.indrev.config.IConfig
import me.steven.indrev.utils.buildEnergyTooltip
import me.steven.indrev.utils.buildMachineTooltip
import net.minecraft.block.Block
import net.minecraft.client.item.TooltipContext
import net.minecraft.item.BlockItem
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import net.minecraft.world.World
import team.reborn.energy.Energy
import team.reborn.energy.EnergyHolder
import team.reborn.energy.EnergyTier

class MachineBlockItem(private val machineBlock: Block, settings: Settings) : BlockItem(machineBlock, settings), EnergyHolder {

    override fun getMaxStoredPower(): Double {
        return when (val config = (machineBlock as? MachineBlock)?.config) {
            is IConfig -> config.maxEnergyStored
            is HeatMachineConfig -> config.maxEnergyStored
            is GeneratorConfig -> config.maxEnergyStored
            else -> Double.MAX_VALUE
        }
    }

    override fun appendTooltip(
        stack: ItemStack?,
        world: World?,
        tooltip: MutableList<Text>?,
        options: TooltipContext?
    ) {
        if (machineBlock !is CableBlock && Energy.valid(stack))
            buildEnergyTooltip(stack, tooltip)
        val config = (machineBlock as? MachineBlock)?.config ?: (machineBlock as? CableBlock)?.getConfig()
        buildMachineTooltip(config ?: return, tooltip)
    }

    override fun getTier(): EnergyTier = EnergyTier.HIGH
}