package me.steven.indrev.blocks.machine.pipes

import alexiil.mc.lib.attributes.item.impl.EmptyGroupedItemInv
import me.steven.indrev.IndustrialRevolution
import me.steven.indrev.api.machines.Tier
import me.steven.indrev.blockentities.cables.CableBlockEntity
import me.steven.indrev.gui.controllers.pipes.PipeFilterController
import me.steven.indrev.gui.controllers.pipes.PipeFilterScreenFactory
import me.steven.indrev.networks.Network
import me.steven.indrev.networks.ServoNetworkState
import me.steven.indrev.networks.item.ItemNetworkState
import me.steven.indrev.utils.groupedItemInv
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.client.item.TooltipContext
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.ActionResult
import net.minecraft.util.Formatting
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.BlockView
import net.minecraft.world.World

class ItemPipeBlock(settings: Settings, val tier: Tier) : BasePipeBlock(settings, Network.Type.ITEM) {
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

    override fun onUse(
        state: BlockState,
        world: World,
        pos: BlockPos,
        player: PlayerEntity?,
        hand: Hand?,
        hit: BlockHitResult
    ): ActionResult {
        val dir = getSideFromHit(hit.pos, pos)
        if (hand == Hand.MAIN_HAND && !world.isClient && player!!.getStackInHand(hand).isEmpty && dir != null && state[getProperty(dir)]) {
            val state = Network.Type.ITEM.getNetworkState(world as ServerWorld) as ItemNetworkState
            if (state[pos]?.containers?.containsKey(pos.offset(dir)) == true) {
                player.openHandledScreen(PipeFilterScreenFactory(::PipeFilterController, pos, dir))
                return ActionResult.SUCCESS
            }
        }
        return super.onUse(state, world, pos, player, hand, hit)
    }

    override fun isConnectable(world: ServerWorld, pos: BlockPos, dir: Direction) =
        groupedItemInv(world, pos, dir) != EmptyGroupedItemInv.INSTANCE
                || world.getBlockState(pos).block.let { it is ItemPipeBlock && it.tier == tier }
                || (type.getNetworkState(world) as ServoNetworkState<*>).hasServo(pos.offset(dir), dir.opposite)

    override fun createBlockEntity(world: BlockView?): BlockEntity = CableBlockEntity(tier)

    fun getConfig() = when(tier) {
        Tier.MK1 -> IndustrialRevolution.CONFIG.cables.cableMk1
        Tier.MK2 -> IndustrialRevolution.CONFIG.cables.cableMk2
        Tier.MK3 -> IndustrialRevolution.CONFIG.cables.cableMk3
        else -> IndustrialRevolution.CONFIG.cables.cableMk4
    }
}