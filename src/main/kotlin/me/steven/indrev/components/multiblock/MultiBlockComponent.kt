package me.steven.indrev.components.multiblock

import net.minecraft.block.BlockState
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class MultiBlockComponent(
    private val isBuilt: (StructureIdentifier) -> Boolean,
    val structureDecider: (BlockState, World, BlockPos) -> StructureDefinition
) {
    var shouldRenderHologram = false
    private var ticks = 0
    private var cachedMatchers: MutableMap<String, AbstractMultiblockMatcher> = hashMapOf()

    fun tick(world: World, pos: BlockPos, blockState: BlockState) {
        ticks++
        if (ticks % 15 != 0) return
        getSelectedMatcher(world, pos, blockState).tick(world, pos, blockState)
    }

    fun getSelectedMatcher(world: World, pos: BlockPos, blockState: BlockState): AbstractMultiblockMatcher {
        val selected = structureDecider(blockState, world, pos)
        return cachedMatchers.computeIfAbsent(selected.identifier) { selected.toMatcher() }
    }

    fun isBuilt(world: World, pos: BlockPos, blockState: BlockState) = getSelectedMatcher(world, pos, blockState).structureIds.any(isBuilt)

    fun toggleRender() {
        shouldRenderHologram = !shouldRenderHologram
    }

    fun readNbt(tag: NbtCompound?) {
        shouldRenderHologram = tag?.getBoolean("ShouldRenderHologram") ?: false
    }

    fun writeNbt(tag: NbtCompound): NbtCompound {
        tag.putBoolean("ShouldRenderHologram", shouldRenderHologram)
        return tag
    }
}