package me.steven.indrev.blocks.machine.pipes

import me.steven.indrev.IndustrialRevolution
import me.steven.indrev.api.machines.Tier
import me.steven.indrev.blockentities.cables.CableBlockEntity
import me.steven.indrev.networks.Network
import me.steven.indrev.utils.energyOf
import net.minecraft.block.entity.BlockEntity
import net.minecraft.client.item.TooltipContext
import net.minecraft.item.ItemStack
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.Formatting
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.BlockView

class CableBlock(settings: Settings, val tier: Tier) : BasePipeBlock(settings, Network.Type.ENERGY) {
    override fun appendTooltip(
        stack: ItemStack?,
        world: BlockView?,
        tooltip: MutableList<Text>?,
        options: TooltipContext?
    ) {
        tooltip?.add(
            TranslatableText("gui.indrev.tooltip.maxInput").formatted(Formatting.AQUA)
                .append(TranslatableText("gui.indrev.tooltip.lftick", getConfig().maxInput).formatted(Formatting.GRAY))
        )
        tooltip?.add(
            TranslatableText("gui.indrev.tooltip.maxOutput").formatted(Formatting.AQUA)
                .append(TranslatableText("gui.indrev.tooltip.lftick", getConfig().maxOutput).formatted(Formatting.GRAY))
        )
    }

    override fun isConnectable(world: ServerWorld, pos: BlockPos, dir: Direction) =
        energyOf(world, pos, dir) != null || world.getBlockState(pos).block.let { it is CableBlock && it.tier == tier }

    override fun createBlockEntity(world: BlockView?): BlockEntity = CableBlockEntity(tier)

    fun getConfig() = when(tier) {
        Tier.MK1 -> IndustrialRevolution.CONFIG.cables.cableMk1
        Tier.MK2 -> IndustrialRevolution.CONFIG.cables.cableMk2
        Tier.MK3 -> IndustrialRevolution.CONFIG.cables.cableMk3
        else -> IndustrialRevolution.CONFIG.cables.cableMk4
    }
}