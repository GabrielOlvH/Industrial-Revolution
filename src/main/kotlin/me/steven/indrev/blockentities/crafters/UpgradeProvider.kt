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
        val map = mutableMapOf<Upgrade, Int>()
        getUpgradeSlots()
            .map { (inventory.getStack(it).item as? IRUpgradeItem)?.upgrade }.filterNotNull().forEach { upgrade ->
                map[upgrade] = map.getOrDefault(upgrade, 0) + 1
            }
        return map
    }

    fun isLocked(slot: Int, tier: Tier) = getUpgradeSlots().indexOf(slot) > tier.ordinal
}