package me.steven.indrev.blockentities.crafters

import it.unimi.dsi.fastutil.objects.Object2IntMap
import me.steven.indrev.api.machines.Tier
import me.steven.indrev.items.upgrade.Enhancer
import me.steven.indrev.items.upgrade.IREnhancerItem
import me.steven.indrev.utils.component1
import me.steven.indrev.utils.component2
import net.minecraft.inventory.Inventory
import java.util.function.IntBinaryOperator

//TODO refactor this into an UpgradeComponent, much more flexible
interface EnhancerProvider {

    val backingMap: Object2IntMap<Enhancer>
    val enhancerSlots: IntArray
    val availableEnhancers: Array<Enhancer>

    fun getBaseValue(enhancer: Enhancer): Double

    fun updateEnhancers(inventory: Inventory) {
        backingMap.clear()
        enhancerSlots
            .forEach { slot ->
                val (stack, item) = inventory.getStack(slot)
                if (item is IREnhancerItem && availableEnhancers.contains(item.enhancer))
                    backingMap.mergeInt(item.enhancer, stack.count, IntBinaryOperator { i, j -> i + j })
            }
    }

    fun getEnhancers(): Object2IntMap<Enhancer> {
        return backingMap
    }

    fun isLocked(slot: Int, tier: Tier) = enhancerSlots.indexOf(slot) > tier.ordinal

    fun getMaxCount(enhancer: Enhancer): Int {
        return when (enhancer) {
            Enhancer.SPEED, Enhancer.BUFFER -> 4
            else -> 1
        }
    }
}