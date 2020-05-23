package me.steven.indrev.blocks

import me.steven.indrev.items.Upgrade
import me.steven.indrev.items.UpgradeItem
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack

interface UpgradeProvider {

    fun getUpgradeSlots(): IntArray

    fun getAvailableUpgrades(): Array<Upgrade>

    fun getUpgrades(inventory: Inventory): Array<ItemStack> {
        val upgrades = mutableListOf<ItemStack>()
        for (i in getUpgradeSlots()) {
            val invStack = inventory.getInvStack(i)
            if (invStack.item is UpgradeItem) upgrades.add(invStack)
        }
        return upgrades.toTypedArray()
    }

    fun getModifier(inventory: Inventory, upgrade: Upgrade): Double {
        var count = 0
        for (i in getUpgradeSlots()) {
            val invStack = inventory.getInvStack(i)
            val item = invStack.item
            if (item is UpgradeItem && item.upgrade == upgrade) count++
        }
        return if (count > 0) count.coerceAtMost(upgrade.maxCount) * upgrade.modifier else 1.0
    }
}