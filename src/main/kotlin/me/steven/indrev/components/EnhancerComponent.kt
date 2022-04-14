package me.steven.indrev.components

import it.unimi.dsi.fastutil.ints.IntBinaryOperator
import it.unimi.dsi.fastutil.objects.Object2IntMap
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import me.steven.indrev.api.machines.Tier
import me.steven.indrev.items.upgrade.Enhancer
import me.steven.indrev.items.upgrade.IREnhancerItem
import me.steven.indrev.utils.component1
import me.steven.indrev.utils.component2
import net.minecraft.inventory.Inventory

open class EnhancerComponent(
    val slots: IntArray,
    val compatible: Array<Enhancer>,
    val maxSlotCount: (Enhancer) -> Int
) {

    val enhancers: Object2IntMap<Enhancer> = Object2IntOpenHashMap()

    fun updateEnhancers(inventory: Inventory) {
        enhancers.clear()
        slots
            .forEach { slot ->
                val (stack, item) = inventory.getStack(slot)
                if (item is IREnhancerItem && compatible.contains(item.enhancer))
                    enhancers.mergeInt(item.enhancer, stack.count, IntBinaryOperator { i, j -> i + j })
            }
    }

    fun getCount(enhancer: Enhancer) = enhancers.getInt(enhancer)

    open fun isLocked(slot: Int, tier: Tier) = slots.indexOf(slot) > tier.ordinal
}