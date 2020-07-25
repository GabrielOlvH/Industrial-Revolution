package me.steven.indrev.items.upgrade

import me.steven.indrev.IndustrialRevolution
import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.blockentities.crafters.UpgradeProvider

enum class Upgrade(val apply: (Double, Int) -> Double) {
    SPEED({ base, count -> base + (IndustrialRevolution.CONFIG.upgrades.speedUpgradeModifier * count) }),
    ENERGY({ base, count -> base - (IndustrialRevolution.CONFIG.upgrades.energyUpgradeModifier * count) }),
    BUFFER({ base, count -> base + (count * IndustrialRevolution.CONFIG.upgrades.bufferUpgradeModifier) });

    operator fun invoke(provider: MachineBlockEntity): Double {
        var count = 0
        if (provider !is UpgradeProvider) return 0.0
        val inventory = provider.inventoryComponent?.inventory  ?: return 0.0
        for (i in provider.getUpgradeSlots()) {
            val invStack = inventory.getStack(i)
            val item = invStack.item as? IRUpgradeItem ?: continue
            if (item.upgrade == this) count++
        }
        return apply(provider.getBaseValue(this), count)
    }

    companion object {
        val ALL = arrayOf(SPEED, ENERGY, BUFFER)
    }
}