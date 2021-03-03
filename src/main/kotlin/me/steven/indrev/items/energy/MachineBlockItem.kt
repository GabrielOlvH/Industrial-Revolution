package me.steven.indrev.items.energy

import dev.technici4n.fasttransferlib.api.energy.EnergyApi
import dev.technici4n.fasttransferlib.api.energy.base.SimpleItemEnergyIo
import me.steven.indrev.api.machines.Tier
import me.steven.indrev.blocks.machine.MachineBlock
import me.steven.indrev.blocks.machine.pipes.CableBlock
import me.steven.indrev.config.GeneratorConfig
import me.steven.indrev.config.HeatMachineConfig
import me.steven.indrev.config.IConfig
import me.steven.indrev.utils.buildEnergyTooltip
import me.steven.indrev.utils.buildMachineTooltip
import me.steven.indrev.utils.energyOf
import net.minecraft.block.Block
import net.minecraft.client.item.TooltipContext
import net.minecraft.item.BlockItem
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import net.minecraft.world.World

class MachineBlockItem(private val machineBlock: Block, settings: Settings) : BlockItem(machineBlock, settings) {

    init {
        val capacity = when (val config = (machineBlock as? MachineBlock)?.config) {
            is IConfig -> config.maxEnergyStored
            is HeatMachineConfig -> config.maxEnergyStored
            is GeneratorConfig -> config.maxEnergyStored
            else -> Double.MAX_VALUE
        }
        EnergyApi.ITEM.register(SimpleItemEnergyIo.getProvider(capacity, Tier.MK4.io, 0.0), this)
    }

    override fun appendTooltip(
        stack: ItemStack?,
        world: World?,
        tooltip: MutableList<Text>?,
        options: TooltipContext?
    ) {
        val itemIo = energyOf(stack)
        if (machineBlock !is CableBlock && itemIo != null)
            buildEnergyTooltip(stack, tooltip)
        val config = (machineBlock as? MachineBlock)?.config ?: (machineBlock as? CableBlock)?.getConfig()
        buildMachineTooltip(config ?: return, tooltip)
    }
}