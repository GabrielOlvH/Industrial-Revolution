package me.steven.indrev.items.upgrade

import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.blockentities.crafters.UpgradeProvider
import me.steven.indrev.config.IRConfig

enum class Upgrade {
    ENERGY, SPEED, BUFFER, BLAST_FURNACE, SMOKER;

    companion object {
        val DEFAULT = arrayOf(SPEED, ENERGY, BUFFER)
        val FURNACE = arrayOf(SPEED, ENERGY, BUFFER, BLAST_FURNACE, SMOKER)

        fun getSpeed(upgrades: Map<Upgrade, Int>, provider: UpgradeProvider)
                = provider.getBaseValue(SPEED) + (IRConfig.upgrades.speedUpgradeModifier * (upgrades[SPEED] ?: 0))

        fun getEnergyCost(upgrades: Map<Upgrade, Int>, provider: UpgradeProvider)
                = (provider.getBaseValue(ENERGY) * ((upgrades[SPEED] ?: 0) * 2).coerceAtLeast(1)) / (IRConfig.upgrades.energyUpgradeModifier * (upgrades[ENERGY] ?: 0)).coerceAtLeast(1.0)

        fun getBuffer(provider: MachineBlockEntity<*>) = getBuffer((provider as UpgradeProvider).getUpgrades(provider.inventoryComponent!!.inventory), provider)

        fun getBuffer(upgrades: Map<Upgrade, Int>, provider: UpgradeProvider)
                = provider.getBaseValue(BUFFER) + (IRConfig.upgrades.bufferUpgradeModifier * (upgrades[BUFFER] ?: 0))
    }
}