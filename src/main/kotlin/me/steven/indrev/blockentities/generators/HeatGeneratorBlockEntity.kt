package me.steven.indrev.blockentities.generators

import alexiil.mc.lib.attributes.Simulation
import alexiil.mc.lib.attributes.fluid.amount.FluidAmount
import me.steven.indrev.api.machines.Tier
import me.steven.indrev.components.FluidComponent
import me.steven.indrev.components.TemperatureComponent
import me.steven.indrev.inventories.inventory
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.MB
import me.steven.indrev.utils.rawId
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable
import net.minecraft.block.BlockState
import net.minecraft.fluid.Fluids
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.math.BlockPos

class HeatGeneratorBlockEntity(tier: Tier, pos: BlockPos, state: BlockState)
    : GeneratorBlockEntity(tier, MachineRegistry.HEAT_GENERATOR_REGISTRY, pos, state), BlockEntityClientSerializable {
    init {
        this.temperatureComponent = TemperatureComponent(this, 0.8, 7000..9000, 10000)
        this.inventoryComponent = inventory(this) {
            input { slot = 2 }
        }
        this.fluidComponent = FluidComponent({ this }, FluidAmount.ofWhole(4))
        this.propertiesSize = 10
    }
    private var burnTime = 0
    private var maxBurnTime = 0

    override fun shouldGenerate(): Boolean {
        if (burnTime > 0) burnTime--
        else if (energyCapacity > energy) {
            val fluidComponent = fluidComponent!!
            val volume = fluidComponent[0]
            val extractable = fluidComponent.extractable
            fluidComponent.extractable
            val consume = getConsumptionRate()
            if (volume.rawFluid == Fluids.LAVA
                && extractable.attemptAnyExtraction(consume, Simulation.SIMULATE).amount() == consume) {
                burnTime = 10
                maxBurnTime = burnTime
                extractable.extract(consume)
            }
            markDirty()
        }
        return burnTime > 0 && energy < energyCapacity
    }

    override fun getGenerationRatio(): Double {
        return config.ratio * (temperatureComponent!!.temperature / temperatureComponent!!.optimalRange.first).coerceAtMost(1.0)
    }

    fun getConsumptionRate(temperature: Double = temperatureComponent!!.temperature): FluidAmount {
        val r = ((temperature / temperatureComponent!!.optimalRange.first).coerceIn(0.001, 1.0) * 500).toLong()
        return MB.mul(r)
    }

    override fun get(index: Int): Int {
        return when(index) {
            BURN_TIME_ID -> burnTime
            TOTAL_BURN_TIME_ID -> maxBurnTime
            FLUID_TANK_SIZE_ID -> fluidComponent!!.limit.asInt(1000)
            FLUID_TANK_AMOUNT_ID -> fluidComponent!![0].amount().asInt(1000)
            FLUID_TANK_FLUID_ID -> fluidComponent!![0].rawFluid.rawId
            GENERATION_RATIO_ID -> getGenerationRatio().toInt()
            else -> super.get(index)
        }
    }

    override fun readNbt(tag: NbtCompound?) {
        super.readNbt(tag)
        burnTime = tag?.getInt("BurnTime") ?: 0
        maxBurnTime = tag?.getInt("MaxBurnTime") ?: 0
    }

    override fun writeNbt(tag: NbtCompound?): NbtCompound {
        tag?.putInt("BurnTime", burnTime)
        tag?.putInt("MaxBurnTime", maxBurnTime)
        return super.writeNbt(tag)
    }

    override fun toClientTag(tag: NbtCompound): NbtCompound {
        fluidComponent!!.toTag(tag)
        return tag
    }

    override fun fromClientTag(tag: NbtCompound) {
        fluidComponent!!.fromTag(tag)
    }

    companion object {
        const val BURN_TIME_ID = 4
        const val TOTAL_BURN_TIME_ID = 5
        const val FLUID_TANK_SIZE_ID = 6
        const val FLUID_TANK_AMOUNT_ID = 7
        const val FLUID_TANK_FLUID_ID = 8
        const val GENERATION_RATIO_ID = 9
    }
}