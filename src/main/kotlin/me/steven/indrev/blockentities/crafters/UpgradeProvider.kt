package me.steven.indrev.blockentities.crafters

import it.unimi.dsi.fastutil.objects.Object2IntArrayMap
import me.steven.indrev.api.machines.Tier
import me.steven.indrev.items.upgrade.IRUpgradeItem
import me.steven.indrev.items.upgrade.Upgrade
import me.steven.indrev.utils.component1
import me.steven.indrev.utils.component2
import net.minecraft.inventory.Inventory

interface UpgradeProvider {

    fun getUpgradeSlots(): IntArray

    fun getAvailableUpgrades(): Array<Upgrade>

    fun getBaseValue(upgrade: Upgrade): Double

    fun getUpgrades(inventory: Inventory): Map<Upgrade, Int> {
        val map = Object2IntArrayMap<Upgrade>(4)
        map.defaultReturnValue(0)
        getUpgradeSlots()
            .forEach { slot ->
                val (stack, item) = inventory.getStack(slot)
                if (item is IRUpgradeItem)
                    map.mergeInt(item.upgrade, stack.count) { i, j -> i + j }
            }
        return map
    }

    fun isLocked(slot: Int, tier: Tier) = getUpgradeSlots().indexOf(slot) > tier.ordinal

    fun getMaxUpgrade(upgrade: Upgrade): Int {
        return when (upgrade) {
            Upgrade.SPEED, Upgrade.BUFFER -> 4
            else -> 1
        }
    }
}