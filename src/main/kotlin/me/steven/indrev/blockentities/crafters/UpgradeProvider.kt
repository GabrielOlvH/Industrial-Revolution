package me.steven.indrev.blockentities.crafters

import me.steven.indrev.items.upgrade.IRUpgradeItem
import me.steven.indrev.items.upgrade.Upgrade
import me.steven.indrev.utils.Tier
import net.minecraft.inventory.Inventory

interface UpgradeProvider {

    fun getUpgradeSlots(): IntArray

    fun getAvailableUpgrades(): Array<Upgrade>

    fun getBaseValue(upgrade: Upgrade): Double

    fun getUpgrades(inventory: Inventory): Map<Upgrade, Int> {
        val map = hashMapOf<Upgrade, Int>()
        getUpgradeSlots()
            .map { slot -> inventory.getStack(slot) }
            .filter { stack -> stack.item is IRUpgradeItem }
            .forEach { stack ->
                map.merge((stack.item as IRUpgradeItem).upgrade, stack.count) { i, j -> i + j }
            }
        return map
    }

    fun isLocked(slot: Int, tier: Tier) = getUpgradeSlots().indexOf(slot) > tier.ordinal
}