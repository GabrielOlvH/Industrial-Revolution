package me.steven.indrev.blocks.machine.pipes

import me.steven.indrev.api.machines.Tier
import me.steven.indrev.config.IRConfig
import me.steven.indrev.networks.Network
import me.steven.indrev.utils.energyOf
import net.minecraft.client.item.TooltipContext
import net.minecraft.item.ItemStack
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.Formatting
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.BlockView

class CableBlock(settings: Settings, tier: Tier) : BasePipeBlock(settings, tier, Network.Type.ENERGY) {
    override fun appendTooltip(
        stack: ItemStack?,
        world: BlockView?,
        tooltip: MutableList<Text>?,
        options: TooltipContext?
    ) {
        tooltip?.add(
            TranslatableText("gui.indrev.tooltip.maxTransferRate").formatted(Formatting.AQUA)
                .append(TranslatableText("gui.indrev.tooltip.lftick", getMaxTransferRate()).formatted(Formatting.GRAY))
        )
    }

    override fun isConnectable(world: ServerWorld, pos: BlockPos, dir: Direction) =
        energyOf(world, pos, dir) != null || world.getBlockState(pos).block.let { it is CableBlock && it.tier == tier }

    fun getMaxTransferRate() = when(tier) {
        Tier.MK1 -> IRConfig.cables.cableMk1
        Tier.MK2 -> IRConfig.cables.cableMk2
        Tier.MK3 -> IRConfig.cables.cableMk3
        else -> IRConfig.cables.cableMk4
    }
}