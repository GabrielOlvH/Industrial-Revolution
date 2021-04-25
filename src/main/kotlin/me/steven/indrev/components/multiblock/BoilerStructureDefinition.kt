package me.steven.indrev.components.multiblock

import me.steven.indrev.blocks.machine.HorizontalFacingMachineBlock
import me.steven.indrev.registry.IRBlockRegistry
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.block.HorizontalFacingBlock
import net.minecraft.util.BlockRotation
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import kotlin.math.abs

object BoilerStructureDefinition : StructureDefinition() {
    private val CASING = IRBlockRegistry.STEAM_TURBINE_CASING_BLOCK.defaultState
    private val RESISTANT_GLASS = Blocks.GLASS.defaultState
    private val SMELTER = IRBlockRegistry.SOLAR_POWER_PLANT_SMELTER_BLOCK.defaultState
    private val FLUID_VALVE = IRBlockRegistry.SOLAR_POWER_PLANT_FLUID_OUTPUT_BLOCK.defaultState.with(
        HorizontalFacingBlock.FACING, Direction.NORTH)

    override val identifier: String = "boiler"
    override val isOptional: Boolean = false
    override val holder: StructureHolder = StructureHelper(this)
        .cube(BlockPos(-1, -2, 0), 3, 3, 4, Blocks.IRON_BLOCK.defaultState)
        .add(BlockPos(0, -2, 1), FLUID_VALVE)
        .add(BlockPos(1, 1, 1), FLUID_VALVE)
        .add(BlockPos(-1, 1, 1), FLUID_VALVE)
        .remove(BlockPos(0, 0, 1))
        .remove(BlockPos(0, -1, 1))
        .remove(BlockPos.ORIGIN)
        .create("default")
        .build()

    fun getFluidValvePositions(pos: BlockPos, state: BlockState): List<BlockPos> {
        val rotation =
            AbstractMultiblockMatcher.rotateBlock(state[HorizontalFacingMachineBlock.HORIZONTAL_FACING])

        val radius = 3
        val positions = hashSetOf<BlockPos>()
        for (x in -radius..radius) {
            for (z in -radius..radius) {
                if ((abs(z) == radius) || (abs(x) == radius))
                    positions.add(BlockPos(x, 0, z + radius))
            }
        }
        return positions
            .map { offset -> pos.subtract(offset.rotate(rotation).rotate(BlockRotation.CLOCKWISE_180)) }.toList()
    }
}