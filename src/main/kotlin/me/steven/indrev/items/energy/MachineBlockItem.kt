package me.steven.indrev.items.energy

import me.steven.indrev.api.machines.Tier
import me.steven.indrev.blocks.machine.MachineBlock
import me.steven.indrev.gui.tooltip.energy.EnergyTooltipDataProvider
import me.steven.indrev.utils.buildMachineTooltip
import net.minecraft.block.Block
import net.minecraft.client.item.TooltipContext
import net.minecraft.item.BlockItem
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import net.minecraft.world.World
import team.reborn.energy.api.EnergyStorage
import team.reborn.energy.impl.SimpleItemEnergyStorageImpl

class MachineBlockItem(private val machineBlock: Block, settings: Settings) : BlockItem(machineBlock, settings),
    EnergyTooltipDataProvider {

    init {
        val capacity = ((machineBlock as? MachineBlock)?.config?.maxEnergyStored ?: 0).toLong()
        EnergyStorage.ITEM.registerForItems({ _, ctx -> SimpleItemEnergyStorageImpl.createSimpleStorage(ctx, capacity, Tier.MK4.io, 0) }, this)
    }

    override fun appendTooltip(
        stack: ItemStack?,
        world: World?,
        tooltip: MutableList<Text>?,
        options: TooltipContext?
    ) {
        val config = (machineBlock as? MachineBlock)?.config
        buildMachineTooltip(config ?: return, tooltip)
    }
}