package me.steven.indrev.blocks.generators

import net.fabricmc.fabric.api.container.ContainerProviderRegistry
import net.minecraft.block.Block
import net.minecraft.block.BlockEntityProvider
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemPlacementContext
import net.minecraft.state.StateManager
import net.minecraft.state.property.DirectionProperty
import net.minecraft.state.property.Properties
import net.minecraft.text.TranslatableText
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.Identifier
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.BlockView
import net.minecraft.world.World

class GeneratorBlock(settings: Settings, val screenId: Identifier, val maxBuffer: Double, val blockEntityProvider: () -> GeneratorBlockEntity) : Block(settings), BlockEntityProvider {
    init {
        this.defaultState = stateManager.defaultState.with(FACING, Direction.NORTH)
    }

    override fun createBlockEntity(view: BlockView?): BlockEntity? = blockEntityProvider()

    override fun getPlacementState(ctx: ItemPlacementContext?): BlockState? {
        return this.defaultState.with(FACING, ctx?.playerFacing?.opposite)
    }

    override fun onUse(state: BlockState?, world: World, pos: BlockPos?, player: PlayerEntity?, hand: Hand?, hit: BlockHitResult?): ActionResult? {
        if (world.isClient) return ActionResult.PASS
        val blockEntity = world.getBlockEntity(pos)
        if (blockEntity is GeneratorBlockEntity) {
            if (player?.isSneaking == true) {
                blockEntity as CoalGeneratorBlockEntity
                player.addChatMessage(TranslatableText("Energy: ${blockEntity.energy}"), false)
                player.addChatMessage(TranslatableText("Generating 0.1U/tick for the next ${blockEntity.burnTime} ticks"), false)
            }
            ContainerProviderRegistry.INSTANCE.openContainer(
                screenId,
                player
            ) { packetByteBuf -> packetByteBuf.writeBlockPos(pos) }
        }
        return ActionResult.SUCCESS
    }

    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>?) {
        builder?.add(FACING)
    }

    companion object {
        val FACING: DirectionProperty = Properties.HORIZONTAL_FACING
    }
}