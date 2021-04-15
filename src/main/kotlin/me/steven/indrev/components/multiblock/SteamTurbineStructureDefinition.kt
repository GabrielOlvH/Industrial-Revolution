package me.steven.indrev.components.multiblock

import me.steven.indrev.blocks.machine.HorizontalFacingMachineBlock
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.util.BlockRotation
import net.minecraft.util.math.BlockPos
import kotlin.math.abs

object SteamTurbineStructureDefinition : StructureDefinition() {
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

    fun getInputValves(pos: BlockPos, state: BlockState, matcher: AbstractMultiblockMatcher): List<BlockPos> {
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
                        map[BlockPos(x, y, z + radius)] = BlockStateFilter(Blocks.GLASS.defaultState)
                }
            }
        }

        map[BlockPos(0, -radius, radius)] = BlockStateFilter(Blocks.GOLD_BLOCK.defaultState)
        map[BlockPos(0, radius, radius)] = BlockStateFilter(Blocks.GOLD_BLOCK.defaultState)

        map[BlockPos(-radius + 1, 0, 0)] = BlockStateFilter(Blocks.DIAMOND_BLOCK.defaultState)
        map[BlockPos(radius - 1, 0, 0)] = BlockStateFilter(Blocks.DIAMOND_BLOCK.defaultState)
        map[BlockPos(0, -radius + 1, 0)] = BlockStateFilter(Blocks.DIAMOND_BLOCK.defaultState)
        map[BlockPos(0, radius - 1, 0)] = BlockStateFilter(Blocks.DIAMOND_BLOCK.defaultState)

        map[BlockPos(-radius + 1, 0, radius * 2)] = BlockStateFilter(Blocks.REDSTONE_BLOCK.defaultState)
        map[BlockPos(radius - 1, 0, radius * 2)] = BlockStateFilter(Blocks.REDSTONE_BLOCK.defaultState)
        map[BlockPos(0, -radius + 1, radius * 2)] = BlockStateFilter(Blocks.REDSTONE_BLOCK.defaultState)
        map[BlockPos(0, radius - 1, radius * 2)] = BlockStateFilter(Blocks.REDSTONE_BLOCK.defaultState)

        for (i in -radius + 1 until radius)
            map[BlockPos(0, i, radius)] = BlockStateFilter(Blocks.ACACIA_FENCE.defaultState)

        map.remove(BlockPos.ORIGIN)
        return map
    }
}