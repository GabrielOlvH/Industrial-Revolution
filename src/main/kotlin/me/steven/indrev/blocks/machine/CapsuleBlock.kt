package me.steven.indrev.blocks.machine

import me.steven.indrev.blockentities.laser.CapsuleBlockEntity
import me.steven.indrev.registry.IRItemRegistry
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.minecraft.block.Block
import net.minecraft.block.BlockEntityProvider
import net.minecraft.block.BlockState
import net.minecraft.block.Material
import net.minecraft.block.entity.BlockEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.world.BlockView
import net.minecraft.world.World

class CapsuleBlock : Block(FabricBlockSettings.of(Material.GLASS).nonOpaque().strength(1f, 1f)), BlockEntityProvider {
    override fun onUse(
        state: BlockState?,
        world: World?,
        pos: BlockPos?,
        player: PlayerEntity?,
        hand: Hand?,
        hit: BlockHitResult?
    ): ActionResult {
        val stack = player?.getStackInHand(hand)
        val blockEntity = world?.getBlockEntity(pos) as? CapsuleBlockEntity ?: return ActionResult.PASS
        if (stack?.item == IRItemRegistry.MODULAR_CORE) {
            player.setStackInHand(hand, ItemStack.EMPTY)
            blockEntity.inventory[0] = stack
        } else if (!blockEntity.inventory[0].isEmpty) {
            val itemStack = blockEntity.inventory[0]
            itemStack.tag = null
            player?.inventory?.insertStack(itemStack)
            blockEntity.inventory[0] = ItemStack.EMPTY
        } else
            return ActionResult.PASS
        return ActionResult.SUCCESS
    }

    override fun createBlockEntity(world: BlockView?): BlockEntity = CapsuleBlockEntity()
}