package me.steven.indrev.blocks

import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.utils.Tier
import net.fabricmc.fabric.impl.screenhandler.ExtendedScreenHandlerType
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.item.ItemPlacementContext
import net.minecraft.state.StateManager
import net.minecraft.state.property.DirectionProperty
import net.minecraft.state.property.Properties

class VerticalFacingMachineBlock(
    settings: Settings,
    tier: Tier,
    screenHandlerType: ExtendedScreenHandlerType<*>?,
    blockEntityProvider: () -> MachineBlockEntity) : FacingMachineBlock(settings, tier, screenHandlerType, blockEntityProvider) {

    override fun getPlacementState(ctx: ItemPlacementContext?): BlockState? {
        return this.defaultState.with(FACING, ctx?.playerLookDirection?.opposite)
    }

    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>?) {
        builder?.add(FACING)
    }

    companion object {
        val FACING: DirectionProperty = Properties.FACING
    }
}