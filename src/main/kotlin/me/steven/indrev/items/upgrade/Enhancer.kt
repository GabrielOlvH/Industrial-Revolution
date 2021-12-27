package me.steven.indrev.items.upgrade

import me.steven.indrev.components.EnhancerComponent
import me.steven.indrev.config.IRConfig

enum class Enhancer {
    SPEED, BUFFER, BLAST_FURNACE, SMOKER, DAMAGE;

    companion object {
        val DEFAULT = arrayOf(SPEED, BUFFER)
        val FURNACE = arrayOf(SPEED, BUFFER, BLAST_FURNACE, SMOKER)

        fun getSpeed(enhancers: Map<Enhancer, Int>, provider: EnhancerComponent)
                = provider.baseValue(SPEED) + (IRConfig.upgrades.speedUpgradeModifier * (enhancers[SPEED] ?: 0))

        fun getDamageMultiplier(enhancers: Map<Enhancer, Int>, provider: EnhancerComponent): Double {
            return (IRConfig.upgrades.damageUpgradeModifier * (enhancers[DAMAGE] ?: 0).toDouble()).coerceAtLeast(1.0)
        }

        fun getBuffer(provider: EnhancerComponent?)
                = if (provider == null) 0 else (provider.baseValue(BUFFER) + (IRConfig.upgrades.bufferUpgradeModifier * (provider.enhancers[BUFFER] ?: 0))).toLong()
    }
}