package me.steven.indrev.blocks.machine.pipes

import alexiil.mc.lib.attributes.fluid.impl.EmptyGroupedFluidInv
import me.steven.indrev.api.machines.Tier
import me.steven.indrev.config.IRConfig
import me.steven.indrev.networks.Network
import me.steven.indrev.networks.ServoNetworkState
import me.steven.indrev.utils.groupedFluidInv
import net.minecraft.client.item.TooltipContext
import net.minecraft.item.ItemStack
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.Text
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.BlockView

class FluidPipeBlock(settings: Settings, tier: Tier) : BasePipeBlock(settings, tier, Network.Type.FLUID) {
    override fun appendTooltip(
        stack: ItemStack?,
        world: BlockView?,
        tooltip: MutableList<Text>?,
        options: TooltipContext?
    ) {

    }

    override fun isConnectable(world: ServerWorld, pos: BlockPos, dir: Direction) =
        groupedFluidInv(world, pos, dir) != EmptyGroupedFluidInv.INSTANCE
                || world.getBlockState(pos).block.let { it is FluidPipeBlock && it.tier == tier }
                || (type.getNetworkState(world) as ServoNetworkState<*>).hasServo(pos.offset(dir.opposite), dir)

    fun getConfig() = when(tier) {
        Tier.MK1 -> IRConfig.cables.cableMk1
        Tier.MK2 -> IRConfig.cables.cableMk2
        Tier.MK3 -> IRConfig.cables.cableMk3
        else -> IRConfig.cables.cableMk4
    }
}