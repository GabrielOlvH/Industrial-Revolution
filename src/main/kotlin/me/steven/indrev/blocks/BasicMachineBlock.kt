package me.steven.indrev.blocks

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
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.Identifier
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.world.BlockView
import net.minecraft.world.World
import team.reborn.energy.Energy

open class BasicMachineBlock(settings: Settings, private val screenId: Identifier, private val test: (BlockEntity?) -> Boolean, val blockEntityProvider: () -> BasicMachineBlockEntity) : Block(settings), BlockEntityProvider {

    override fun onUse(state: BlockState?, world: World, pos: BlockPos?, player: PlayerEntity?, hand: Hand?, hit: BlockHitResult?): ActionResult? {
        if (world.isClient) return ActionResult.PASS
        val blockEntity = world.getBlockEntity(pos)
        if (test(blockEntity)) {
            ContainerProviderRegistry.INSTANCE.openContainer(
                    screenId,
                    player
            ) { packetByteBuf -> packetByteBuf.writeBlockPos(pos) }
        }
        return ActionResult.SUCCESS
    }
    
    override fun createBlockEntity(view: BlockView?): BlockEntity? = blockEntityProvider()

    override fun getPlacementState(ctx: ItemPlacementContext?): BlockState? {
        return this.defaultState.with(FACING, ctx?.playerFacing?.opposite)
    }

    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>?) {
        builder?.add(FACING)
    }

    fun tryProvideEnergyTo(world: World?, sourcePos: BlockPos, targetPos: BlockPos): Double {
        val sourceBlockEntity = world?.getBlockEntity(sourcePos)
        if (sourceBlockEntity == null || !Energy.valid(sourceBlockEntity)) return 0.0
        val targetBlockEntity = world.getBlockEntity(targetPos)
        if (targetBlockEntity == null || !Energy.valid(targetBlockEntity)) return 0.0
        val sourceHandler = Energy.of(sourceBlockEntity)
        val targetHandler = Energy.of(targetBlockEntity)
        return sourceHandler.into(targetHandler).move()
    }

    companion object {
        val FACING: DirectionProperty = Properties.HORIZONTAL_FACING
    }
}