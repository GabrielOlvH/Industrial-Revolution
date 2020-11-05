package me.steven.indrev.components

import net.minecraft.block.BlockState
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.World

class MultiblockComponent(val stateDecider: (BlockState, World) -> Int, vararg val states: AbstractMultiblockMatcher) {
    var shouldRenderHologram = false

    fun tick(blockState: BlockState, world: World) {
        val select = stateDecider(blockState, world)
        states[select].tick()
    }

    fun getSelectedMatcher(blockState: BlockState, world: World): AbstractMultiblockMatcher = states[stateDecider(blockState, world)]

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