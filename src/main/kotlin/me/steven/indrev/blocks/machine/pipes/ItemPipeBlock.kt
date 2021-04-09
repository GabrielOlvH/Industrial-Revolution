package me.steven.indrev.blocks.machine.pipes

import alexiil.mc.lib.attributes.item.impl.EmptyGroupedItemInv
import me.steven.indrev.api.machines.Tier
import me.steven.indrev.config.IRConfig
import me.steven.indrev.gui.controllers.pipes.PipeFilterController
import me.steven.indrev.gui.controllers.pipes.PipeFilterScreenFactory
import me.steven.indrev.networks.Network
import me.steven.indrev.networks.ServoNetworkState
import me.steven.indrev.networks.item.ItemNetworkState
import me.steven.indrev.utils.groupedItemInv
import net.minecraft.block.BlockState
import net.minecraft.client.item.TooltipContext
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.Text
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.BlockView
import net.minecraft.world.World

class ItemPipeBlock(settings: Settings, tier: Tier) : BasePipeBlock(settings, tier, Network.Type.ITEM) {
    override fun appendTooltip(
        stack: ItemStack?,
        world: BlockView?,
        tooltip: MutableList<Text>?,
        options: TooltipContext?
    ) {

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

    fun getConfig() = when(tier) {
        Tier.MK1 -> IRConfig.cables.cableMk1
        Tier.MK2 -> IRConfig.cables.cableMk2
        Tier.MK3 -> IRConfig.cables.cableMk3
        else -> IRConfig.cables.cableMk4
    }
}