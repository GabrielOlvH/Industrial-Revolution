package me.steven.indrev.blockentities.generators

import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.Tier
import team.reborn.energy.EnergySide

abstract class GeneratorBlockEntity(tier: Tier, registry: MachineRegistry) :
    MachineBlockEntity(tier, registry) {

    override fun machineTick() {
        if (world?.isClient == false) {
            if (shouldGenerate() && maxStoredPower > energy) {
                this.energy += getGenerationRatio()
                this.temperatureController?.tick(true)
                setWorkingState(true)
            } else {
                setWorkingState(false)
                this.temperatureController?.tick(false)
            }
        }
    }

    override fun getMaxInput(side: EnergySide?): Double = 0.0

    abstract fun shouldGenerate(): Boolean

    abstract fun getGenerationRatio(): Double
}