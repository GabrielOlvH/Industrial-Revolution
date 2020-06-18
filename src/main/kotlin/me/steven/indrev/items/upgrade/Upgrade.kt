package me.steven.indrev.items.upgrade

import me.steven.indrev.blockentities.crafters.UpgradeProvider
import net.minecraft.inventory.Inventory

enum class Upgrade(val apply: (Double, Int) -> Double) {
    SPEED({ base, count -> base + 1.2 * count }),
    ENERGY({ base, count -> base - 0.1 * count }),
    BUFFER({ base, count -> base + count * 20000 });

    fun apply(provider: UpgradeProvider, inventory: Inventory): Double {
        var count = 0
        for (i in provider.getUpgradeSlots()) {
            val invStack = inventory.getStack(i)
            val item = invStack.item
            if (item is IRUpgradeItem && item.upgrade == this) count++
        }
        return apply(provider.getBaseValue(this), count)
    }

    companion object {
        val ALL = arrayOf(SPEED, ENERGY, BUFFER)
    }
}