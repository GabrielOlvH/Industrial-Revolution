package me.steven.indrev.items.upgrade

import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.blockentities.crafters.EnhancerProvider
import me.steven.indrev.config.IRConfig

enum class Enhancer {
    ENERGY, SPEED, BUFFER, BLAST_FURNACE, SMOKER, DAMAGE;

    companion object {
        val DEFAULT = arrayOf(SPEED, ENERGY, BUFFER)
        val FURNACE = arrayOf(SPEED, ENERGY, BUFFER, BLAST_FURNACE, SMOKER)

        fun getSpeed(upgrades: Map<Enhancer, Int>, provider: EnhancerProvider)
                = provider.getBaseValue(SPEED) + (IRConfig.upgrades.speedUpgradeModifier * (upgrades[SPEED] ?: 0))

        fun getEnergyCost(upgrades: Map<Enhancer, Int>, provider: EnhancerProvider): Double {
            val a = (IRConfig.upgrades.energyUpgradeModifier * (upgrades[ENERGY] ?: 0))
            return (provider.getBaseValue(ENERGY) * ((upgrades[SPEED]
                ?: 0) * 2).coerceAtLeast(1)) / if (a > 0) a else 1.0
        }

        fun getDamageMultiplier(upgrades: Map<Enhancer, Int>, provider: EnhancerProvider): Double {
            return IRConfig.upgrades.damageUpgradeModifier * (upgrades[DAMAGE] ?: 0).toDouble()
        }

        fun getBuffer(provider: MachineBlockEntity<*>) = getBuffer((provider as EnhancerProvider).getEnhancers(provider.inventoryComponent!!.inventory), provider)

        fun getBuffer(upgrades: Map<Enhancer, Int>, provider: EnhancerProvider)
                = provider.getBaseValue(BUFFER) + (IRConfig.upgrades.bufferUpgradeModifier * (upgrades[BUFFER] ?: 0))
    }
}