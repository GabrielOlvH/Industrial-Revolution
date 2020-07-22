package me.steven.indrev.blocks

import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.utils.Tier
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.ShapeContext
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.util.function.BooleanBiFunction
import net.minecraft.util.math.BlockPos
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes
import net.minecraft.world.BlockView
import java.util.stream.Stream

class ModularWorkbenchBlock(
    settings: Settings,
    tier: Tier,
    screenHandler: ((Int, PlayerInventory, ScreenHandlerContext) -> ScreenHandler)?,
    blockEntityProvider: () -> MachineBlockEntity
) : MachineBlock(settings, tier, screenHandler, blockEntityProvider) {

    override fun getOutlineShape(state: BlockState?, world: BlockView?, pos: BlockPos?, context: ShapeContext?): VoxelShape = SHAPE

    companion object {

        private val SHAPE = Stream.of(
            Block.createCuboidShape(3.0, 2.0, 13.0, 13.0, 15.0, 13.0),
            Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 1.0, 16.0),
            Block.createCuboidShape(1.0, 1.0, 1.0, 15.0, 2.0, 15.0),
            Block.createCuboidShape(0.0, 16.0, 0.0, 16.0, 17.0, 16.0),
            Block.createCuboidShape(1.0, 15.0, 1.0, 15.0, 16.0, 15.0),
            Block.createCuboidShape(2.0, 2.0, 2.0, 3.0, 15.0, 3.0),
            Block.createCuboidShape(13.0, 2.0, 13.0, 14.0, 15.0, 14.0),
            Block.createCuboidShape(13.0, 2.0, 2.0, 14.0, 15.0, 3.0),
            Block.createCuboidShape(2.0, 2.0, 13.0, 3.0, 15.0, 14.0),
            Block.createCuboidShape(3.0, 2.0, 3.0, 3.0, 15.0, 13.0),
            Block.createCuboidShape(13.0, 2.0, 3.0, 13.0, 15.0, 13.0),
            Block.createCuboidShape(3.0, 2.0, 3.0, 13.0, 15.0, 3.0)
        ).reduce { v1: VoxelShape?, v2: VoxelShape? -> VoxelShapes.combineAndSimplify(v1, v2, BooleanBiFunction.OR) }.get()
    }
}