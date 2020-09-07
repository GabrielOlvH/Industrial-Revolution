package me.steven.indrev.blocks

import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.components.TransferMode
import me.steven.indrev.config.IConfig
import me.steven.indrev.utils.Tier
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.ItemPlacementContext
import net.minecraft.item.ItemStack
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.state.StateManager
import net.minecraft.state.property.DirectionProperty
import net.minecraft.state.property.Properties
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

open class HorizontalFacingMachineBlock(
    settings: Settings,
    tier: Tier,
    config: IConfig?,
    screenHandler: ((Int, PlayerInventory, ScreenHandlerContext) -> ScreenHandler)?,
    blockEntityProvider: () -> MachineBlockEntity<*>
) : MachineBlock(settings, tier, config, screenHandler, blockEntityProvider) {

    override fun getPlacementState(ctx: ItemPlacementContext?): BlockState? {
        super.getPlacementState(ctx)
        return this.defaultState.with(HORIZONTAL_FACING, ctx?.playerFacing?.opposite)
    }

    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>?) {
        super.appendProperties(builder)
        builder?.add(HORIZONTAL_FACING)
    }

    override fun onPlaced(world: World?, pos: BlockPos, state: BlockState?, placer: LivingEntity?, itemStack: ItemStack?) {
        val blockEntity = world?.getBlockEntity(pos)
        if (blockEntity is MachineBlockEntity<*>) {
            val direction = state?.get(HORIZONTAL_FACING) ?: return
            val inventoryController = blockEntity.inventoryComponent ?: return
            val itemConfig = inventoryController.itemConfig
            itemConfig[direction.rotateYClockwise()] = TransferMode.INPUT
            itemConfig[direction.rotateYCounterclockwise()] = TransferMode.OUTPUT
        }
        super.onPlaced(world, pos, state, placer, itemStack)
    }

    companion object {
        val HORIZONTAL_FACING: DirectionProperty = Properties.HORIZONTAL_FACING
    }
}