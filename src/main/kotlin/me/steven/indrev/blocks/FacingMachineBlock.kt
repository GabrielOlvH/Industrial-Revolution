package me.steven.indrev.blocks

import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.utils.Tier
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.ItemPlacementContext
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.state.StateManager
import net.minecraft.state.property.DirectionProperty
import net.minecraft.state.property.Properties

open class FacingMachineBlock(
    settings: Settings,
    tier: Tier,
    screenHandler: ((Int, PlayerInventory, ScreenHandlerContext) -> ScreenHandler)?,
    blockEntityProvider: () -> MachineBlockEntity
) : MachineBlock(settings, tier, screenHandler, blockEntityProvider) {

    override fun getPlacementState(ctx: ItemPlacementContext?): BlockState? {
        super.getPlacementState(ctx)
        return this.defaultState.with(HORIZONTAL_FACING, ctx?.playerFacing?.opposite)
    }

    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>?) {
        super.appendProperties(builder)
        builder?.add(HORIZONTAL_FACING)
    }

    companion object {
        val HORIZONTAL_FACING: DirectionProperty = Properties.HORIZONTAL_FACING
    }
}