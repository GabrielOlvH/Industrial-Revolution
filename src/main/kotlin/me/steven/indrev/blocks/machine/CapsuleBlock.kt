package me.steven.indrev.blocks.machine

import me.steven.indrev.blockentities.laser.CapsuleBlockEntity
import me.steven.indrev.recipes.machines.LaserRecipe
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.minecraft.block.Block
import net.minecraft.block.BlockEntityProvider
import net.minecraft.block.BlockState
import net.minecraft.block.Material
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityTicker
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.ItemScatterer
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.BlockView
import net.minecraft.world.World

class CapsuleBlock : Block(FabricBlockSettings.of(Material.GLASS).requiresTool().nonOpaque().strength(1f, 1f)), BlockEntityProvider {
    override fun onUse(
        state: BlockState?,
        world: World,
        pos: BlockPos?,
        player: PlayerEntity,
        hand: Hand?,
        hit: BlockHitResult?
    ): ActionResult {
        if (!world.isClient && hand == Hand.MAIN_HAND) {
            val stack = player.getStackInHand(hand)
            val recipe = LaserRecipe.TYPE.getMatchingRecipe(world as ServerWorld, stack)
                .firstOrNull { it.matches(stack, emptyList()) }
            val blockEntity = world.getBlockEntity(pos) as? CapsuleBlockEntity ?: return ActionResult.PASS
            if (recipe != null && blockEntity.inventory[0].isEmpty) {
                player.setStackInHand(hand, ItemStack.EMPTY)
                blockEntity.inventory[0] = stack
            } else if (!blockEntity.inventory[0].isEmpty) {
                val itemStack = blockEntity.inventory[0]
                itemStack.nbt = null
                player.inventory?.insertStack(itemStack)
                blockEntity.inventory[0] = ItemStack.EMPTY
            } else
                return ActionResult.PASS
            world.updateNeighbors(pos, this)
            blockEntity.markDirty()
            blockEntity.sync()
            return ActionResult.SUCCESS
        }
        return ActionResult.PASS
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
        if (blockEntity.world!!.isClient) return 0
        val stack = blockEntity.inventory[0]
        val recipe = LaserRecipe.TYPE.getMatchingRecipe(world as ServerWorld, stack)
            .firstOrNull { it.matches(stack, emptyList()) }
        return if (recipe == null) 15
        else 0
    }

    override fun createBlockEntity(pos: BlockPos, state: BlockState): BlockEntity = CapsuleBlockEntity(pos, state)

    override fun <T : BlockEntity?> getTicker(
        world: World,
        state: BlockState?,
        type: BlockEntityType<T>?
    ): BlockEntityTicker<T>? {
        return if (world.isClient) null
        else BlockEntityTicker { world, pos, state, blockEntity ->
            CapsuleBlockEntity.tick(world, pos, state, blockEntity as CapsuleBlockEntity)
        }
    }
}