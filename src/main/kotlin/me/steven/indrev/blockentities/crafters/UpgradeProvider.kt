package me.steven.indrev.blockentities.crafters

import me.steven.indrev.items.upgrade.IRUpgradeItem
import me.steven.indrev.items.upgrade.Upgrade
import me.steven.indrev.utils.Tier
import me.steven.indrev.utils.component1
import me.steven.indrev.utils.component2
import net.minecraft.inventory.Inventory

interface UpgradeProvider {

    fun getUpgradeSlots(): IntArray

    fun getAvailableUpgrades(): Array<Upgrade>

    fun getBaseValue(upgrade: Upgrade): Double

    fun getUpgrades(inventory: Inventory): Map<Upgrade, Int> {
        val map = hashMapOf<Upgrade, Int>()
        getUpgradeSlots()
            .forEach { slot ->
                val (stack, item) = inventory.getStack(slot)
                if (item is IRUpgradeItem)
                    map.merge(item.upgrade, stack.count) { i, j -> i + j }
            }
        return map
    }

    fun isLocked(slot: Int, tier: Tier) = getUpgradeSlots().indexOf(slot) > tier.ordinal
}