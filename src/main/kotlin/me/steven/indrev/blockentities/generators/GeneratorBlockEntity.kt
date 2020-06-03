package me.steven.indrev.blockentities.generators

import me.steven.indrev.blockentities.HeatMachineBlockEntity
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.Tier
import net.minecraft.container.ArrayPropertyDelegate
import net.minecraft.container.PropertyDelegate
import team.reborn.energy.EnergySide

abstract class GeneratorBlockEntity(tier: Tier, registry: MachineRegistry) :
    HeatMachineBlockEntity(tier, registry) {

    override fun tick() {
        super.tick()
        if (world?.isClient == false) {
            if (shouldGenerate() && addEnergy(getGenerationRatio()) > 0)
                tickTemperature(true)
            else
                tickTemperature(false)
            sync()
            markDirty()
        }
    }

    override fun createDelegate(): PropertyDelegate = ArrayPropertyDelegate(3)

    override fun getMaxInput(side: EnergySide?): Double = 0.0

    abstract fun shouldGenerate(): Boolean

    abstract fun getGenerationRatio(): Double
}