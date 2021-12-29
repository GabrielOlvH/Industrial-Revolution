package me.steven.indrev.registry

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import net.minecraft.fluid.Fluid

object IRFluidFuelRegistry {
    private val registry = Object2ObjectOpenHashMap<Fluid, FluidFuelInfo>()

    fun register(fluid: Fluid, info: FluidFuelInfo) {
        registry[fluid] = info
    }

    fun register(fluid: Fluid, burnTime: Int, combustionTemperature: Int, ratio: Int, consumptionRatio: Long) {
        registry[fluid] = FluidFuelInfo(burnTime, combustionTemperature, ratio, consumptionRatio)
    }

    fun isFuel(fluid: Fluid) = registry.contains(fluid)

    fun get(fluid: Fluid) = registry[fluid]

    data class FluidFuelInfo(val burnTime: Int, val combustionTemperature: Int, val generationRatio: Int, val consumptionRatio: Long)
}