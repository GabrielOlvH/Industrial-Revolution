package me.steven.indrev.components.multiblock

import net.minecraft.block.BlockState
import net.minecraft.nbt.CompoundTag
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class MultiBlockComponent(val structureDecider: (BlockState, World, BlockPos) -> StructureDefinition) {
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

    fun toggleRender() {
        shouldRenderHologram = !shouldRenderHologram
    }

    fun fromTag(tag: CompoundTag?) {
        shouldRenderHologram = tag?.getBoolean("ShouldRenderHologram") ?: false
    }

    fun toTag(tag: CompoundTag): CompoundTag {
        tag.putBoolean("ShouldRenderHologram", shouldRenderHologram)
        return tag
    }
}