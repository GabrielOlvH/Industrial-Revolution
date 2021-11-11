package me.steven.indrev.items.energy

import me.steven.indrev.api.machines.Tier
import me.steven.indrev.blocks.machine.MachineBlock
import me.steven.indrev.gui.tooltip.energy.EnergyTooltipData
import me.steven.indrev.gui.tooltip.modular.ModularTooltipData
import me.steven.indrev.utils.buildMachineTooltip
import me.steven.indrev.utils.energyOf
import net.minecraft.block.Block
import net.minecraft.client.item.TooltipContext
import net.minecraft.client.item.TooltipData
import net.minecraft.item.BlockItem
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import net.minecraft.world.World
import team.reborn.energy.api.EnergyStorage
import team.reborn.energy.impl.SimpleItemEnergyStorageImpl
import java.util.*

class MachineBlockItem(private val machineBlock: Block, settings: Settings) : BlockItem(machineBlock, settings) {

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

    override fun getTooltipData(stack: ItemStack): Optional<TooltipData> {
        val handler = energyOf(stack) ?: return Optional.empty()
        return Optional.of(EnergyTooltipData(handler.amount, handler.capacity))
    }
}