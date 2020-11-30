package me.steven.indrev.blockentities.generators

import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.config.GeneratorConfig
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.Tier
import team.reborn.energy.EnergySide

abstract class GeneratorBlockEntity(tier: Tier, registry: MachineRegistry) :
    MachineBlockEntity<GeneratorConfig>(tier, registry) {

    override fun machineTick() {
        if (world?.isClient == false) {
            if (shouldGenerate() && maxStoredPower > energy) {
                this.energy += getGenerationRatio()
                this.temperatureComponent?.tick(true)
                workingState = true
            } else {
                workingState = false
                this.temperatureComponent?.tick(false)
            }
        }
    }

    override fun getMaxInput(side: EnergySide?): Double = 0.0

    abstract fun shouldGenerate(): Boolean

    open fun getGenerationRatio(): Double = config.ratio * if (this.temperatureComponent?.isFullEfficiency() == true) config.temperatureBoost else 1.0

    override fun getMaxOutput(side: EnergySide?): Double = config.maxOutput
}