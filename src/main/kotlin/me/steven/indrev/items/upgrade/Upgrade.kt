package me.steven.indrev.items.upgrade

import me.steven.indrev.IndustrialRevolution
import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.blockentities.crafters.UpgradeProvider

enum class Upgrade {
    ENERGY, SPEED, BUFFER, BLAST_FURNACE, SMOKER;

    companion object {
        val DEFAULT = arrayOf(SPEED, ENERGY, BUFFER)

        fun getSpeed(upgrades: Map<Upgrade, Int>, provider: UpgradeProvider)
                = provider.getBaseValue(SPEED) + (IndustrialRevolution.CONFIG.upgrades.speedUpgradeModifier * (upgrades[SPEED] ?: 0))

        fun getEnergyCost(upgrades: Map<Upgrade, Int>, provider: UpgradeProvider)
                = (provider.getBaseValue(ENERGY) * (upgrades[SPEED] ?: 1))  - (IndustrialRevolution.CONFIG.upgrades.energyUpgradeModifier * (upgrades[ENERGY] ?: 0))

        fun getBuffer(provider: MachineBlockEntity<*>) = getBuffer((provider as UpgradeProvider).getUpgrades(provider.inventoryComponent!!.inventory), provider)

        fun getBuffer(upgrades: Map<Upgrade, Int>, provider: UpgradeProvider)
                = provider.getBaseValue(BUFFER) + (IndustrialRevolution.CONFIG.upgrades.bufferUpgradeModifier * (upgrades[BUFFER] ?: 0))
    }
}