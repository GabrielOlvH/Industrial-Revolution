package me.steven.indrev.items

import me.steven.indrev.blockentities.crafters.UpgradeProvider
import net.minecraft.inventory.Inventory

enum class Upgrade(val apply: (Double, Int) -> Double) {
    SPEED({ base, count -> (base * 1.2 * count).coerceAtLeast(base) }),
    TEMPERATURE({ base, count -> (base / (1.5 * count.coerceAtLeast(0)).coerceAtLeast(1.0)) }),
    ENERGY({ base, count -> (base / 1.2 * count.coerceAtLeast(0)) }),
    BUFFER({ base, count -> (base * 2.0 * count).coerceAtLeast(base) });

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
        val ALL = arrayOf(SPEED, ENERGY, BUFFER, TEMPERATURE)
    }
}