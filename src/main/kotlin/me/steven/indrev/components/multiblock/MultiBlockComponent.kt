package me.steven.indrev.components.multiblock

import net.minecraft.block.BlockState
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

open class MultiBlockComponent(
    val structureDecider: (BlockState, World, BlockPos) -> StructureDefinition
) {
    var shouldRenderHologram = false
    var variant = 0
    private var ticks = 0
    private var cachedMatchers: MutableMap<String, MultiblockMatcher> = hashMapOf()

    open fun tick(world: World, pos: BlockPos, blockState: BlockState) {
        ticks++
        if (ticks % 15 != 0) return
        getSelectedMatcher(world, pos, blockState).update(world, pos, blockState)
    }

    fun getSelectedMatcher(world: World, pos: BlockPos, blockState: BlockState): MultiblockMatcher {
        val selected = structureDecider(blockState, world, pos)
        return cachedMatchers.computeIfAbsent(selected.identifier) { selected.toMatcher() }
    }

    fun isBuilt(world: World, pos: BlockPos, blockState: BlockState, forceUpdate: Boolean = false): Boolean {
        if (forceUpdate) {
            getSelectedMatcher(world, pos, blockState).update(world, pos, blockState)
        }
        return getSelectedMatcher(world, pos, blockState).matches
    }

    fun toggleRender(isSneaking: Boolean) {
        if (!isSneaking)
            shouldRenderHologram = !shouldRenderHologram
        else
            variant++
    }

    fun readNbt(tag: NbtCompound?) {
        shouldRenderHologram = tag?.getBoolean("ShouldRenderHologram") ?: false
        variant = tag?.getInt("Variant") ?: 0
    }

    fun writeNbt(tag: NbtCompound): NbtCompound {
        tag.putBoolean("ShouldRenderHologram", shouldRenderHologram)
        tag.putInt("Variant", variant)
        return tag
    }
}