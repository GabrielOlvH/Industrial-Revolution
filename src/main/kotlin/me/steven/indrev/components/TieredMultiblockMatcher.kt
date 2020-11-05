package me.steven.indrev.components

import com.google.common.collect.ImmutableMap
import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.utils.Tier
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.block.BlockState
import net.minecraft.util.math.BlockPos

class TieredMultiblockMatcher private constructor(val blockEntity: MachineBlockEntity<*>, val structures: Array<Map<BlockPos, BlockState>>)
    : AbstractMultiblockMatcher() {

    val components: Array<MultiblockMatcher> = structures.map { map -> MultiblockMatcher(blockEntity, map) }.toTypedArray()

    var currentTier: Tier? = null

    override fun tick() {
        for ((index, component) in components.withIndex()) {
            component.tick()
            if (!component.isBuilt) {
                if (index > 0) {
                    currentTier = Tier.values()[index - 1]
                    isBuilt = true
                } else {
                    currentTier = null
                    isBuilt = false
                }
                return
            }
        }
        currentTier = Tier.MK4
        isBuilt = true
    }

    @Environment(EnvType.CLIENT)
    override fun getRenderingStructure(): Map<BlockPos, BlockState> = components.flatMap { it.structure.entries }.associate { Pair(it.key, it.value) }

    class Builder(vararg val tiers: Tier, val mapProvider: () -> MutableMap<BlockPos, BlockState> = { HashMap() }) {

        private val structures: Array<Map<BlockPos, BlockState>> = Array(tiers.size) { mapProvider() }

        fun configure(tier: Tier, builder: MultiblockMatcher.Builder.() -> Unit): Builder {
            val b = MultiblockMatcher.Builder()
            builder(b)
            structures[tier.ordinal] = b.test
            return this
        }

        fun build(blockEntity: MachineBlockEntity<*>) = TieredMultiblockMatcher(blockEntity, structures.map { ImmutableMap.copyOf(it) }.toTypedArray())
    }
}