package me.steven.indrev.blockentities.generators

import alexiil.mc.lib.attributes.Simulation
import alexiil.mc.lib.attributes.fluid.amount.FluidAmount
import me.steven.indrev.components.FluidComponent
import me.steven.indrev.components.TemperatureComponent
import me.steven.indrev.inventories.inventory
import me.steven.indrev.registry.IRRegistry
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.Property
import me.steven.indrev.utils.Tier
import net.minecraft.block.BlockState
import net.minecraft.fluid.Fluid
import net.minecraft.fluid.Fluids
import net.minecraft.nbt.CompoundTag
import net.minecraft.screen.ArrayPropertyDelegate

class HeatGeneratorBlockEntity(tier: Tier) : GeneratorBlockEntity(tier, MachineRegistry.HEAT_GENERATOR_REGISTRY) {
    init {
        this.propertyDelegate = ArrayPropertyDelegate(7)
        this.temperatureComponent = TemperatureComponent(
            { this },
            2.3,
            { if (burnTime > 0 && stableTemperature > 0) stableTemperature.toDouble() else this.temperatureComponent!!.explosionLimit },
            7000..9000,
            10000.0
        )
        this.inventoryComponent = inventory(this) {
            input { slot = 2 }
        }
        this.fluidComponent = FluidComponent({ this }, FluidAmount.ofWhole(4))
    }

    private var stableTemperature: Int = 0
    private var burnTime: Int by Property(4, 0)
    private var maxBurnTime: Int by Property(5, 0)

    override fun shouldGenerate(): Boolean {
        if (burnTime > 0) burnTime--
        else if (maxStoredPower > energy) {
            val fluidComponent = fluidComponent!!
            val volume = fluidComponent.tanks[0].volume
            val extractable = fluidComponent.extractable
            if (TEMPERATURE_MAP.containsKey(volume.rawFluid)
                && extractable.attemptAnyExtraction(FluidAmount.BUCKET, Simulation.SIMULATE).amount() == FluidAmount.BUCKET) {
                stableTemperature = TEMPERATURE_MAP[volume.rawFluid] ?: return false
                burnTime = 1600
                maxBurnTime = burnTime
                extractable.extract(FluidAmount.BUCKET)
            }
        }
        markDirty()
        return burnTime > 0 && energy < maxStoredPower
    }

    override fun getGenerationRatio(): Double {
        val ratio = 64.0 * (if (temperatureComponent?.isFullEfficiency() == true) stableTemperature / 1000 else 1)
        propertyDelegate[6] = ratio.toInt()
        return ratio
    }

    override fun fromTag(state: BlockState?, tag: CompoundTag?) {
        super.fromTag(state, tag)
        burnTime = tag?.getInt("BurnTime") ?: 0
        maxBurnTime = tag?.getInt("MaxBurnTime") ?: 0
        stableTemperature = tag?.getInt("StableTemperature") ?: 0
    }

    override fun toTag(tag: CompoundTag?): CompoundTag {
        tag?.putInt("BurnTime", burnTime)
        tag?.putInt("MaxBurnTime", maxBurnTime)
        tag?.putInt("StableTemperature", stableTemperature)
        return super.toTag(tag)
    }

    override fun fromClientTag(tag: CompoundTag?) {
        super.fromClientTag(tag)
        burnTime = tag?.getInt("BurnTime") ?: 0
        maxBurnTime = tag?.getInt("MaxBurnTime") ?: 0
        stableTemperature = tag?.getInt("StableTemperature") ?: 0
    }

    override fun toClientTag(tag: CompoundTag?): CompoundTag {
        tag?.putInt("BurnTime", burnTime)
        tag?.putInt("MaxBurnTime", maxBurnTime)
        tag?.putInt("StableTemperature", stableTemperature)
        return super.toClientTag(tag)
    }

    companion object {
        private val TEMPERATURE_MAP = mutableMapOf<Fluid, Int>().also {
            it[Fluids.LAVA] = 5500
            it[Fluids.FLOWING_LAVA] = 2255
            it[IRRegistry.MOLTEN_NETHERITE_STILL] = 8000
            it[IRRegistry.MOLTEN_NETHERITE_FLOWING] = 5500
        }
    }
}