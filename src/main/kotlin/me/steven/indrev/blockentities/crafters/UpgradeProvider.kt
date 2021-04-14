package me.steven.indrev.blockentities.crafters

import it.unimi.dsi.fastutil.objects.Object2IntMap
import me.steven.indrev.api.machines.Tier
import me.steven.indrev.items.upgrade.IRUpgradeItem
import me.steven.indrev.items.upgrade.Upgrade
import me.steven.indrev.utils.component1
import me.steven.indrev.utils.component2
import net.minecraft.inventory.Inventory

interface UpgradeProvider {

    val backingMap: Object2IntMap<Upgrade>
    val upgradeSlots: IntArray
    val availableUpgrades: Array<Upgrade>

    fun getBaseValue(upgrade: Upgrade): Double

    fun getUpgrades(inventory: Inventory): Map<Upgrade, Int> {
        backingMap.clear()
        upgradeSlots
            .forEach { slot ->
                val (stack, item) = inventory.getStack(slot)
                if (item is IRUpgradeItem && availableUpgrades.contains(item.upgrade))
                    backingMap.mergeInt(item.upgrade, stack.count) { i, j -> i + j }
            }
        return backingMap
    }

    fun isLocked(slot: Int, tier: Tier) = upgradeSlots.indexOf(slot) > tier.ordinal

    fun getMaxUpgrade(upgrade: Upgrade): Int {
        return when (upgrade) {
            Upgrade.SPEED, Upgrade.BUFFER -> 4
            else -> 1
        }
    }
}