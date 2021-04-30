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
import net.minecraft.util.ItemScatterer
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
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
        if (stack?.item == IRItemRegistry.MODULAR_CORE && blockEntity.inventory[0].isEmpty) {
            player.setStackInHand(hand, ItemStack.EMPTY)
            blockEntity.inventory[0] = stack
        } else if (!blockEntity.inventory[0].isEmpty) {
            val itemStack = blockEntity.inventory[0]
            itemStack.tag = null
            player?.inventory?.insertStack(itemStack)
            blockEntity.inventory[0] = ItemStack.EMPTY
        } else
            return ActionResult.PASS
        world.updateNeighbors(pos, this)
        return ActionResult.SUCCESS
    }

    override fun onStateReplaced(
        state: BlockState?,
        world: World,
        pos: BlockPos,
        newState: BlockState?,
        moved: Boolean
    ) {
        val blockEntity = world.getBlockEntity(pos) as? CapsuleBlockEntity
        if (blockEntity != null)
            ItemScatterer.spawn(world, pos, blockEntity.inventory)
        super.onStateReplaced(state, world, pos, newState, moved)
    }

    override fun emitsRedstonePower(state: BlockState?): Boolean = true

    override fun getWeakRedstonePower(
        state: BlockState?,
        world: BlockView,
        pos: BlockPos,
        direction: Direction?
    ): Int {
        val blockEntity = world.getBlockEntity(pos) as? CapsuleBlockEntity ?: return 0
        return if (blockEntity.inventory[0].item == IRItemRegistry.MODULAR_CORE_ACTIVATED) 15
        else 0
    }

    override fun createBlockEntity(world: BlockView?): BlockEntity = CapsuleBlockEntity()
}