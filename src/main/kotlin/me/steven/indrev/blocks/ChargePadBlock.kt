package me.steven.indrev.blocks

import me.steven.indrev.blockentities.battery.ChargePadBlockEntity
import me.steven.indrev.items.rechargeable.Rechargeable
import me.steven.indrev.utils.Tier
import net.minecraft.block.BlockState
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import team.reborn.energy.Energy

class ChargePadBlock(settings: Settings, tier: Tier) : FacingMachineBlock(settings, tier, null, { ChargePadBlockEntity(tier) }) {
    override fun onUse(state: BlockState?, world: World, pos: BlockPos?, player: PlayerEntity?, hand: Hand?, hit: BlockHitResult?): ActionResult? {
        val blockEntity = world.getBlockEntity(pos)
        val stack = player?.mainHandStack
        val item = stack?.item
        if ((Energy.valid(stack) || item is Rechargeable) && blockEntity is ChargePadBlockEntity) {
            val inventory = blockEntity.inventoryController?.inventory ?: return ActionResult.PASS
            val remaining = inventory.addStack(stack)
            player?.setStackInHand(Hand.MAIN_HAND, remaining)
        }
        return ActionResult.PASS
    }

    override fun onEntityCollision(state: BlockState?, world: World?, pos: BlockPos?, entity: Entity?) {
        if (entity is PlayerEntity) {
            val blockEntity = world?.getBlockEntity(pos) as? ChargePadBlockEntity ?: return
            ARMOR_SLOTS.forEach { slot ->
                val stack = entity.inventory.getStack(slot)
                if (Energy.valid(stack))
                    Energy.of(blockEntity).into(Energy.of(stack)).move(blockEntity.tier.io)
            }
        }
    }

    companion object {
        val ARMOR_SLOTS = intArrayOf(39, 38, 37, 36)
    }
}