package me.steven.indrev.items.upgrade

import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.blockentities.crafters.EnhancerProvider
import me.steven.indrev.config.IRConfig

enum class Enhancer {
    SPEED, BUFFER, BLAST_FURNACE, SMOKER, DAMAGE;

    companion object {
        val DEFAULT = arrayOf(SPEED, BUFFER)
        val FURNACE = arrayOf(SPEED, BUFFER, BLAST_FURNACE, SMOKER)

        fun getSpeed(enhancers: Map<Enhancer, Int>, provider: EnhancerProvider)
                = provider.getBaseValue(SPEED) + (IRConfig.upgrades.speedUpgradeModifier * (enhancers[SPEED] ?: 0))

        fun getDamageMultiplier(enhancers: Map<Enhancer, Int>, provider: EnhancerProvider): Double {
            return (IRConfig.upgrades.damageUpgradeModifier * (enhancers[DAMAGE] ?: 0).toDouble()).coerceAtLeast(1.0)
        }

        fun getBuffer(provider: MachineBlockEntity<*>) = getBuffer((provider as EnhancerProvider).getEnhancers(), provider)

        fun getBuffer(enhancers: Map<Enhancer, Int>?, provider: EnhancerProvider)
                = provider.getBaseValue(BUFFER) + (IRConfig.upgrades.bufferUpgradeModifier * ((enhancers?: emptyMap())[BUFFER] ?: 0))
    }
}