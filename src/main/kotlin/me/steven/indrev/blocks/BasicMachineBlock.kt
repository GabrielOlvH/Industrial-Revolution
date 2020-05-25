package me.steven.indrev.blocks

import me.steven.indrev.blockentities.BasicMachineBlockEntity
import me.steven.indrev.utils.Tier
import net.minecraft.block.Block
import net.minecraft.block.BlockEntityProvider
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.client.item.TooltipContext
import net.minecraft.item.ItemPlacementContext
import net.minecraft.item.ItemStack
import net.minecraft.state.StateManager
import net.minecraft.state.property.DirectionProperty
import net.minecraft.state.property.Properties
import net.minecraft.text.Text
import net.minecraft.util.registry.Registry
import net.minecraft.world.BlockView

open class BasicMachineBlock(settings: Settings, tier: Tier, val blockEntityProvider: () -> BasicMachineBlockEntity) :
    Block(settings), BlockEntityProvider {

    override fun createBlockEntity(view: BlockView?): BlockEntity? = blockEntityProvider()

    override fun buildTooltip(
        stack: ItemStack?,
        view: BlockView?,
        tooltip: MutableList<Text>?,
        options: TooltipContext?
    ) {
        val id = Registry.ITEM.getId(stack?.item)
        if (!Registry.BLOCK.containsId(id)) return
        val block = Registry.BLOCK.get(id)
    }

    override fun getPlacementState(ctx: ItemPlacementContext?): BlockState? {
        return this.defaultState.with(FACING, ctx?.playerFacing?.opposite)
    }

    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>?) {
        builder?.add(FACING)
    }

    companion object {
        val FACING: DirectionProperty = Properties.HORIZONTAL_FACING
    }
}