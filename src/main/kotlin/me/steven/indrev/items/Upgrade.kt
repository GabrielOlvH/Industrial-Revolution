package me.steven.indrev.items

import me.steven.indrev.blocks.UpgradeProvider
import net.minecraft.inventory.Inventory

enum class Upgrade(private val apply: (Double, Int) -> Double) {
    SPEED({ base, count -> base * 1.2 * count }),
    ENERGY({ base, count -> base / (1.2 * count) }),
    BUFFER({ base, count -> base * 2.0 * count });

    fun apply(provider: UpgradeProvider, inventory: Inventory): Double {
        var count = 0
        for (i in provider.getUpgradeSlots()) {
            val invStack = inventory.getInvStack(i)
            val item = invStack.item
            if (item is UpgradeItem && item.upgrade == this) count++
        }
        return apply(provider.getBaseValue(this), count)
    }

    companion object {
        val ALL = arrayOf(SPEED, ENERGY, BUFFER)
    }
}