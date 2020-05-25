package me.steven.indrev.blockentities.generators

import me.steven.indrev.blockentities.InterfacedMachineBlockEntity
import me.steven.indrev.utils.Tier
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.container.ArrayPropertyDelegate
import net.minecraft.container.PropertyDelegate
import team.reborn.energy.EnergySide

abstract class GeneratorBlockEntity(
    type: BlockEntityType<*>,
    tier: Tier,
    private val generationRatio: Double,
    maxBuffer: Double
) :
    InterfacedMachineBlockEntity(type, tier, maxBuffer) {

    override fun tick() {
        super.tick()
        if (world?.isClient == false && shouldGenerate() && addEnergy(generationRatio) > 0) markDirty()
    }

    override fun createDelegate(): PropertyDelegate = ArrayPropertyDelegate(2)

    override fun getMaxInput(side: EnergySide?): Double = 0.0

    abstract fun shouldGenerate(): Boolean
}