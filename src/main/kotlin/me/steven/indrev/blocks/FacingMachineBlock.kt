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

class FacingMachineBlock(
    settings: Settings,
    tier: Tier,
    config: Any?,
    screenHandler: ((Int, PlayerInventory, ScreenHandlerContext) -> ScreenHandler)?,
    blockEntityProvider: () -> MachineBlockEntity
) : MachineBlock(settings, tier, config, screenHandler, blockEntityProvider) {

    override fun getPlacementState(ctx: ItemPlacementContext?): BlockState? {
        super.getPlacementState(ctx)
        return this.defaultState.with(FACING, ctx?.playerLookDirection?.opposite)
    }

    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>?) {
        super.appendProperties(builder)
        builder?.add(FACING)
    }

    companion object {
        val FACING: DirectionProperty = Properties.FACING
    }
}