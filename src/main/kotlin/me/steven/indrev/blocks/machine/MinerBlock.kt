package me.steven.indrev.blocks.machine

import me.steven.indrev.api.machines.Tier
import me.steven.indrev.config.IRConfig
import me.steven.indrev.gui.controllers.machines.MinerController
import me.steven.indrev.registry.MachineRegistry
import net.minecraft.client.item.TooltipContext
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.Formatting
import net.minecraft.world.BlockView

class MinerBlock(registry: MachineRegistry, settings: Settings, tier: Tier) : HorizontalFacingMachineBlock(
    registry,
    settings,
    tier,
    IRConfig.machines.miner,
    ::MinerController,
) {
    override fun appendTooltip(
        stack: ItemStack?,
        view: BlockView?,
        tooltip: MutableList<Text>?,
        options: TooltipContext?
    ) {
        super.appendTooltip(stack, view, tooltip, options)
        tooltip?.add(
            TranslatableText("block.indrev.miner.tooltip").formatted(Formatting.BLUE, Formatting.ITALIC)
        )
    }
}