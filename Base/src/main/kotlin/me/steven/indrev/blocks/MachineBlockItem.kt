package me.steven.indrev.blocks

import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.utils.Tier
import me.steven.indrev.utils.itemSettings
import net.minecraft.advancement.criterion.Criteria
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.ShapeContext
import net.minecraft.item.Item
import net.minecraft.item.ItemPlacementContext
import net.minecraft.item.ItemUsageContext
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.sound.SoundCategory
import net.minecraft.util.ActionResult
import net.minecraft.world.event.GameEvent

class MachineBlockItem(val block: Block, val tier: Tier) : Item(itemSettings()) {

    override fun useOnBlock(context: ItemUsageContext): ActionResult {
        val actionResult = place(ItemPlacementContext(context))
        return if (!actionResult.isAccepted && this.isFood) {
            val actionResult2 = this.use(context.world, context.player, context.hand).result
            if (actionResult2 == ActionResult.CONSUME) ActionResult.CONSUME_PARTIAL else actionResult2
        } else {
            actionResult
        }
    }

    private fun place(context: ItemPlacementContext): ActionResult {
        return if (!context.canPlace()) {
            ActionResult.FAIL
        } else {
            val blockState = this.getPlacementState(context)
            if (blockState == null || !context.world.setBlockState(context.blockPos, blockState, 11)) {
                ActionResult.FAIL
            } else {
                val blockPos = context.blockPos
                val world = context.world
                val playerEntity = context.player
                val itemStack = context.stack
                val blockState2 = world.getBlockState(blockPos)
                if (blockState2.isOf(blockState.block)) {
                    blockState2.block.onPlaced(world, blockPos, blockState2, playerEntity, itemStack)
                    if (playerEntity is ServerPlayerEntity) {
                        Criteria.PLACED_BLOCK.trigger(playerEntity as ServerPlayerEntity?, blockPos, itemStack)
                    }
                    val blockEntity = world.getBlockEntity(blockPos) as? MachineBlockEntity<*>
                    if (blockEntity != null) {
                        blockEntity.tier = tier
                    }
                }
                val blockSoundGroup = blockState2.soundGroup
                world.playSound(playerEntity, blockPos, blockState2.soundGroup.placeSound, SoundCategory.BLOCKS, (blockSoundGroup.getVolume() + 1.0f) / 2.0f, blockSoundGroup.getPitch() * 0.8f)
                world.emitGameEvent(GameEvent.BLOCK_PLACE, blockPos, GameEvent.Emitter.of(playerEntity, blockState2))
                if (playerEntity == null || !playerEntity.abilities.creativeMode) {
                    itemStack.decrement(1)
                }
                ActionResult.success(world.isClient)
            }
        }
    }

    private fun getPlacementState(context: ItemPlacementContext): BlockState? {
        val blockState = block.getPlacementState(context)
        return if (blockState != null && this.canPlace(context, blockState)) blockState else null
    }

    private fun canPlace(context: ItemPlacementContext, state: BlockState): Boolean {
        val playerEntity = context.player
        val shapeContext = if (playerEntity == null) ShapeContext.absent() else ShapeContext.of(playerEntity)
        return state.canPlaceAt(context.world, context.blockPos) && context.world.canPlace(state, context.blockPos, shapeContext)
    }
}