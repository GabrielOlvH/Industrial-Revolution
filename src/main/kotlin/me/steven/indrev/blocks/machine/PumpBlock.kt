package me.steven.indrev.blocks.machine

import me.steven.indrev.IndustrialRevolution
import me.steven.indrev.blockentities.farms.PumpBlockEntity
import me.steven.indrev.utils.Tier
import net.minecraft.block.BlockState
import net.minecraft.block.ShapeContext
import net.minecraft.util.math.BlockPos
import net.minecraft.util.shape.VoxelShape
import net.minecraft.world.BlockView

class PumpBlock(settings: Settings) : HorizontalFacingMachineBlock(
    settings,
    Tier.MK1,
    IndustrialRevolution.CONFIG.machines.drain,
    null,
    { PumpBlockEntity(Tier.MK1) }) {

    override fun getOutlineShape(
        state: BlockState?,
        world: BlockView?,
        pos: BlockPos?,
        context: ShapeContext?
    ): VoxelShape = SHAPE

    companion object {
        private val SHAPE = createCuboidShape(2.0, 0.0, 2.0, 14.0, 16.0, 14.0)
    }
}