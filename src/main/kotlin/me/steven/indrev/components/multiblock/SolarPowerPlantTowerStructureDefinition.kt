package me.steven.indrev.components.multiblock

import me.steven.indrev.blocks.machine.HorizontalFacingMachineBlock
import me.steven.indrev.registry.IRBlockRegistry
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.block.FacingBlock
import net.minecraft.util.BlockRotation
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import kotlin.math.abs

object SolarPowerPlantTowerStructureDefinition : StructureDefinition() {

    private val CASING = IRBlockRegistry.STEAM_TURBINE_CASING_BLOCK
    private val RESISTANT_GLASS = Blocks.GLASS.defaultState
    private val SMELTER = IRBlockRegistry.SOLAR_POWER_PLANT_SMELTER_BLOCK.defaultState
    private val FLUID_OUTPUT = IRBlockRegistry.FLUID_VALVE.defaultState.with(FacingBlock.FACING, Direction.NORTH)

    override val identifier: String = "solar_power_plant"
    override val isOptional: Boolean = false
    override val holder: StructureHolder = StructureHelper(this)
        .from(createStructureMap())
        .create("default")
        .build()

    fun getSmelterPositions(pos: BlockPos, state: BlockState): List<BlockPos> {
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

    fun getSolarReceiverPositions(pos: BlockPos, state: BlockState): List<BlockPos> {
        val rotation =
            AbstractMultiblockMatcher.rotateBlock(state[HorizontalFacingMachineBlock.HORIZONTAL_FACING])

        val radius = 3
        val positions = hashSetOf<BlockPos>()
        for (x in -radius + 1 until radius) {
            for (y in -10..-4) {
                for (z in -radius + 1 until radius) {
                    if (y == -8 && (abs(x) == radius-1 || abs(z) == radius-1))
                        positions.add(BlockPos(x, y, z + 3))
                }
            }
        }

        return positions
            .map { offset -> pos.subtract(offset.rotate(rotation).rotate(BlockRotation.CLOCKWISE_180)) }.toList()
    }

    private fun createStructureMap(): Map<BlockPos, BlockStateFilter> {
        val map = hashMapOf<BlockPos, BlockStateFilter>()
        val radius = 3
        for (x in -radius..radius) {
            for (y in -3..1) {
                for (z in -radius..radius) {
                    if (arrayOf(abs(x), abs(y), abs(z)).count { it == radius } >= 2 || y == -3 || y == 1)
                        map[BlockPos(x, y, z + radius)] = BlockStateFilter(Blocks.IRON_BLOCK.defaultState)
                    else if ((y == 0 && abs(z) == radius) || (y == 0 && abs(x) == radius))
                        map[BlockPos(x, y, z + radius)] = BlockStateFilter({state -> state == Blocks.IRON_BARS.defaultState || state == SMELTER }, SMELTER)
                    else if (arrayOf(abs(x), abs(y), abs(z)).count { it == radius } != 0)
                        map[BlockPos(x, y, z + radius)] = BlockStateFilter({ state -> state == CASING || state == RESISTANT_GLASS }, RESISTANT_GLASS)
                }
            }
        }
        map[BlockPos(0, 1, 0)] = BlockStateFilter(FLUID_OUTPUT)

        for (x in -radius + 1 until radius) {
            for (y in -10..-4) {
                for (z in -radius + 1 until radius) {
                    if (y == -8 && (abs(x) == radius-1 || abs(z) == radius-1))
                        map[BlockPos(x, y, z + 3)] = BlockStateFilter(Blocks.OBSIDIAN.defaultState)
                    else if (abs(x) == radius-1 || abs(z) == radius-1 || y == -10)
                        map[BlockPos(x, y, z + 3)] = BlockStateFilter(Blocks.IRON_BLOCK.defaultState)
                }
            }
        }

        map.remove(BlockPos.ORIGIN)

        return map
    }

}