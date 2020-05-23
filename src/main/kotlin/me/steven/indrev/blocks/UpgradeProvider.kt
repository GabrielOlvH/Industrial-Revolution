package me.steven.indrev.blocks

import me.steven.indrev.items.Upgrade
import me.steven.indrev.items.UpgradeItem
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack

interface UpgradeProvider {

    fun getUpgradeSlots(): IntArray

    fun getAvailableUpgrades(): Array<Upgrade>

    fun getBaseValue(upgrade: Upgrade): Double

    fun getUpgrades(inventory: Inventory): Array<ItemStack> {
        val upgrades = mutableListOf<ItemStack>()
        for (i in getUpgradeSlots()) {
            val invStack = inventory.getInvStack(i)
            if (invStack.item is UpgradeItem) upgrades.add(invStack)
        }
        return upgrades.toTypedArray()
    }
}