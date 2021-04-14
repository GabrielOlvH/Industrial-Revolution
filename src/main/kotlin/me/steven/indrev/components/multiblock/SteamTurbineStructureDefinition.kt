package me.steven.indrev.components.multiblock

import net.minecraft.block.Blocks
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