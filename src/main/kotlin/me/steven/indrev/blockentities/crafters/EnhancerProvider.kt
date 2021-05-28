package me.steven.indrev.blockentities.crafters

import it.unimi.dsi.fastutil.objects.Object2IntMap
import me.steven.indrev.api.machines.Tier
import me.steven.indrev.items.enhancer.IREnhancerItem
import me.steven.indrev.items.enhancer.Enhancer
import me.steven.indrev.utils.component1
import me.steven.indrev.utils.component2
import net.minecraft.inventory.Inventory

interface EnhancerProvider {

    val backingMap: Object2IntMap<Enhancer>
    val enhancementsSlots: IntArray
    val availableEnhancers: Array<Enhancer>

    fun getBaseValue(enhancer: Enhancer): Double

    fun getEnhancers(inventory: Inventory): Map<Enhancer, Int> {
        backingMap.clear()
        enhancementsSlots
            .forEach { slot ->
                val (stack, item) = inventory.getStack(slot)
                if (item is IREnhancerItem && availableEnhancers.contains(item.enhancer))
                    backingMap.mergeInt(item.enhancer, stack.count) { i, j -> i + j }
            }
        return backingMap
    }

    fun isLocked(slot: Int, tier: Tier) = enhancementsSlots.indexOf(slot) > tier.ordinal

    fun getMaxEnhancer(enhancer: Enhancer): Int {
        return when (enhancer) {
            Enhancer.SPEED, Enhancer.BUFFER -> 4
            else -> 1
        }
    }
}