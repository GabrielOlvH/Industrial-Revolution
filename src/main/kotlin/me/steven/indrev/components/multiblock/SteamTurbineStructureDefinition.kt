package me.steven.indrev.components.multiblock

import me.steven.indrev.blocks.machine.HorizontalFacingMachineBlock
import me.steven.indrev.blocks.misc.VerticalFacingBlock
import me.steven.indrev.registry.IRBlockRegistry
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.block.FacingBlock
import net.minecraft.block.HorizontalFacingBlock
import net.minecraft.util.BlockRotation
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import kotlin.math.abs

object SteamTurbineStructureDefinition : StructureDefinition() {

    private val ROTOR_UP = IRBlockRegistry.STEAM_TURBINE_ROTOR_BLOCK.defaultState.with(VerticalFacingBlock.FACING, Direction.UP)
    private val ROTOR_DOWN = IRBlockRegistry.STEAM_TURBINE_ROTOR_BLOCK.defaultState.with(VerticalFacingBlock.FACING, Direction.DOWN)
    private val PRESSURE_VALVE_SOUTH = IRBlockRegistry.STEAM_TURBINE_PRESSURE_VALVE_BLOCK.defaultState.with(HorizontalFacingBlock.FACING, Direction.SOUTH)
    private val STEAM_INPUT_VALVE_NORTH = IRBlockRegistry.STEAM_TURBINE_STEAM_INPUT_VALVE_BLOCK.defaultState.with(FacingBlock.FACING, Direction.NORTH)
    private val ENERGY_OUTPUT_NORTH = IRBlockRegistry.STEAM_TURBINE_ENERGY_OUTPUT.defaultState.with(HorizontalFacingBlock.FACING, Direction.NORTH)
    private val CASING = IRBlockRegistry.STEAM_TURBINE_CASING_BLOCK
    private val RESISTANT_GLASS = Blocks.GLASS.defaultState

    override val identifier: String = "steam_turbine"
    override val isOptional: Boolean = false
    override val holder: StructureHolder = StructureHelper(this)
        .from(createStructureMap(2))
        .create("5x5x5")
        .from(createStructureMap(3))
        .create("7x7x7")
        .from(createStructureMap(4))
        .create("9x9x9")
        .from(createStructureMap(5))
        .create("11x11x11")
        .from(createStructureMap(6))
        .create("13x13x13")
        .from(createStructureMap(7))
        .create("15x15x15")
        .build()

    fun getInputValvePositions(pos: BlockPos, state: BlockState, matcher: AbstractMultiblockMatcher): List<BlockPos> {
        val rotation =
            AbstractMultiblockMatcher.rotateBlock(state[HorizontalFacingMachineBlock.HORIZONTAL_FACING])

        matcher.structureIds.firstOrNull()?.also { id ->
            val radius = getRadius(id)
            return arrayOf(
                BlockPos(-radius + 1, 0, 0),
                BlockPos(radius - 1, 0, 0),
                BlockPos(0, -radius + 1, 0),
                BlockPos(0, radius - 1, 0)
            )
                .map { offset -> pos.subtract(offset.rotate(rotation).rotate(BlockRotation.CLOCKWISE_180)) }.toList()
        }
        return emptyList()
    }

    fun getRadius(id: StructureIdentifier) = (id.variant.substring(0, id.variant.indexOf("x")).toInt() - 1) / 2

    private fun createStructureMap(radius: Int): Map<BlockPos, BlockStateFilter> {
        val map = hashMapOf<BlockPos, BlockStateFilter>()
        for (x in -radius..radius) {
            for (y in -radius..radius) {
                for (z in -radius..radius) {
                    if (arrayOf(abs(x), abs(y), abs(z)).count { it == radius } >= 2 || (y == 0 && abs(z) == radius) || (x == 0 && abs(z) == radius) || abs(y) == radius)
                        map[BlockPos(x, y, z + radius)] = BlockStateFilter(Blocks.IRON_BLOCK.defaultState)
                    else if (arrayOf(abs(x), abs(y), abs(z)).count { it == radius } != 0)
                        map[BlockPos(x, y, z + radius)] = BlockStateFilter({ state -> state == CASING || state == RESISTANT_GLASS}, RESISTANT_GLASS)
                }
            }
        }

        map[BlockPos(0, -radius, radius)] = BlockStateFilter(ROTOR_UP)
        map[BlockPos(0, radius, radius)] = BlockStateFilter(ROTOR_DOWN)

        map[BlockPos(-radius + 1, 0, 0)] = BlockStateFilter(STEAM_INPUT_VALVE_NORTH)
        map[BlockPos(radius - 1, 0, 0)] = BlockStateFilter(STEAM_INPUT_VALVE_NORTH)
        map[BlockPos(0, -radius + 1, 0)] = BlockStateFilter(STEAM_INPUT_VALVE_NORTH)
        map[BlockPos(0, radius - 1, 0)] = BlockStateFilter(STEAM_INPUT_VALVE_NORTH)

        map[BlockPos(-radius + 1, 0, radius * 2)] = BlockStateFilter(PRESSURE_VALVE_SOUTH)
        map[BlockPos(radius - 1, 0, radius * 2)] = BlockStateFilter(PRESSURE_VALVE_SOUTH)
        map[BlockPos(0, -radius + 1, radius * 2)] = BlockStateFilter(PRESSURE_VALVE_SOUTH)
        map[BlockPos(0, radius - 1, radius * 2)] = BlockStateFilter(PRESSURE_VALVE_SOUTH)

        for (i in -radius + 1 until radius)
            map[BlockPos(0, i, radius)] = BlockStateFilter(Blocks.ACACIA_FENCE.defaultState)

        map[BlockPos(0, 1, 0)] = BlockStateFilter(ENERGY_OUTPUT_NORTH)

        map.remove(BlockPos(0, 0, 0))
        return map
    }
}