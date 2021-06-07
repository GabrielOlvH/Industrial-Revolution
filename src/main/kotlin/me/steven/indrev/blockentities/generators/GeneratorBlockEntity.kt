package me.steven.indrev.blockentities.generators

import me.steven.indrev.api.machines.Tier
import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.config.GeneratorConfig
import me.steven.indrev.registry.MachineRegistry
import net.minecraft.block.BlockState
import net.minecraft.util.math.BlockPos

abstract class GeneratorBlockEntity(tier: Tier, registry: MachineRegistry, pos: BlockPos, state: BlockState) :
    MachineBlockEntity<GeneratorConfig>(tier, registry, pos, state) {

    override fun machineTick() {
        if (world?.isClient == false) {
            if (shouldGenerate() && energyCapacity > energy) {
                this.energy += getGenerationRatio()
                this.temperatureComponent?.tick(true)
                workingState = true
            } else {
                workingState = false
                this.temperatureComponent?.tick(false)
            }
        }
    }

    override val maxInput: Double = 0.0
    override val maxOutput: Double = config.maxOutput

    abstract fun shouldGenerate(): Boolean

    open fun getGenerationRatio(): Double = config.ratio * if (this.temperatureComponent?.isFullEfficiency() == true) config.temperatureBoost else 1.0
}