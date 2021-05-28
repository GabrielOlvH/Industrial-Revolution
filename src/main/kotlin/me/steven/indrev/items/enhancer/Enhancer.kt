package me.steven.indrev.items.enhancer

import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.blockentities.crafters.EnhancerProvider
import me.steven.indrev.config.IRConfig

enum class Enhancer {
    ENERGY, SPEED, BUFFER, BLAST_FURNACE, SMOKER, DAMAGE;

    companion object {
        val DEFAULT = arrayOf(SPEED, ENERGY, BUFFER)
        val FURNACE = arrayOf(SPEED, ENERGY, BUFFER, BLAST_FURNACE, SMOKER)

        fun getSpeed(enhancements: Map<Enhancer, Int>, provider: EnhancerProvider)
                = provider.getBaseValue(SPEED) + (IRConfig.enhancers.speedEnhancerModifier * (enhancements[SPEED] ?: 0))

        fun getEnergyCost(enhancements: Map<Enhancer, Int>, provider: EnhancerProvider): Double {
            val a = (IRConfig.enhancers.energyEnhancerModifier * (enhancements[ENERGY] ?: 0))
            return (provider.getBaseValue(ENERGY) * ((enhancements[SPEED]
                ?: 0) * 2).coerceAtLeast(1)) / if (a > 0) a else 1.0
        }

        fun getDamageMultiplier(upgrades: Map<Enhancer, Int>, provider: EnhancerProvider): Double {
            return IRConfig.enhancers.damageEnhancerModifier * (upgrades[DAMAGE] ?: 0).toDouble()
        }

        fun getBuffer(provider: MachineBlockEntity<*>) = getBuffer((provider as EnhancerProvider).getEnhancers(provider.inventoryComponent!!.inventory), provider)

        fun getBuffer(enhancements: Map<Enhancer, Int>, provider: EnhancerProvider)
                = provider.getBaseValue(BUFFER) + (IRConfig.enhancers.bufferEnhancerModifier * (enhancements[BUFFER] ?: 0))
    }
}