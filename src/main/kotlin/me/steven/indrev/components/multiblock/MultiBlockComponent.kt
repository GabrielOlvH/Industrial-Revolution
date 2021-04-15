package me.steven.indrev.components.multiblock

import net.minecraft.block.BlockState
import net.minecraft.nbt.CompoundTag
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

open class MultiBlockComponent(
    private val isBuilt: (StructureIdentifier) -> Boolean,
    val structureDecider: (BlockState, World, BlockPos) -> StructureDefinition
) {
    var shouldRenderHologram = false
    var variant = 0
    private var ticks = 0
    private var cachedMatchers: MutableMap<String, AbstractMultiblockMatcher> = hashMapOf()

    open fun tick(world: World, pos: BlockPos, blockState: BlockState) {
        ticks++
        if (ticks % 15 != 0) return
        getSelectedMatcher(world, pos, blockState).tick(world, pos, blockState)
    }

    fun getSelectedMatcher(world: World, pos: BlockPos, blockState: BlockState): AbstractMultiblockMatcher {
        val selected = structureDecider(blockState, world, pos)
        return cachedMatchers.computeIfAbsent(selected.identifier) { selected.toMatcher() }
    }

    fun isBuilt(world: World, pos: BlockPos, blockState: BlockState) = getSelectedMatcher(world, pos, blockState).structureIds.any(isBuilt)

    fun toggleRender(isSneaking: Boolean) {
        if (!isSneaking)
            shouldRenderHologram = !shouldRenderHologram
        else
            variant++
    }

    fun fromTag(tag: CompoundTag?) {
        shouldRenderHologram = tag?.getBoolean("ShouldRenderHologram") ?: false
        variant = tag?.getInt("Variant") ?: 0
    }

    fun toTag(tag: CompoundTag): CompoundTag {
        tag.putBoolean("ShouldRenderHologram", shouldRenderHologram)
        tag.putInt("Variant", variant)
        return tag
    }
}