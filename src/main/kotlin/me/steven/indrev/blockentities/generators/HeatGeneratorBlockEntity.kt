package me.steven.indrev.blockentities.generators

import me.steven.indrev.api.machines.Tier
import me.steven.indrev.components.*
import me.steven.indrev.inventories.inventory
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.bucket
import net.minecraft.block.BlockState
import net.minecraft.fluid.Fluids
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.math.BlockPos

class HeatGeneratorBlockEntity(tier: Tier, pos: BlockPos, state: BlockState)
    : GeneratorBlockEntity(tier, MachineRegistry.HEAT_GENERATOR_REGISTRY, pos, state) {

    private var burnTime by autosync(GasBurningGeneratorBlockEntity.BURN_TIME_ID, 0)
    private var maxBurnTime by autosync(GasBurningGeneratorBlockEntity.TOTAL_BURN_TIME_ID, 0)

    init {
        this.temperatureComponent = TemperatureComponent(this, 0.8, 7000..9000, 10000)
        this.inventoryComponent = inventory(this) {
            input { slot = 2 }
        }
        this.fluidComponent = FluidComponent({ this }, bucket * 4)
        fluidComponent!!.inputTanks = intArrayOf(0)

        trackObject(TANK_ID, fluidComponent!![0])

        trackLong(GENERATION_RATIO_ID) { getGenerationRatio() }
    }

    override fun shouldGenerate(): Boolean {
        if (burnTime > 0) burnTime--
        else if (getCapacity() > energy) {
            val tank = fluidComponent!![0]
            val consume = getConsumptionRate()
            if (tank.variant.isOf(Fluids.LAVA)
                && tank.tryExtract(consume)) {
                burnTime = 10
                maxBurnTime = burnTime
                tank.extract(consume, true)
            }
            markDirty()
        }
        return burnTime > 0 && energy < getCapacity()
    }

    override fun getGenerationRatio(): Long {
        return (config.ratio * (temperatureComponent!!.temperature / temperatureComponent!!.optimalRange.first).coerceAtMost(1.0)).toLong()
    }

    fun getConsumptionRate(temperature: Double = temperatureComponent!!.temperature): Long {
        return ((temperature / temperatureComponent!!.optimalRange.first) / 810).toLong()
    }

    override fun fromTag(tag: NbtCompound) {
        super.fromTag(tag)
        burnTime = tag?.getInt("BurnTime") ?: 0
        maxBurnTime = tag?.getInt("MaxBurnTime") ?: 0
    }

    override fun toTag(tag: NbtCompound) {
        tag?.putInt("BurnTime", burnTime)
        tag?.putInt("MaxBurnTime", maxBurnTime)
        super.toTag(tag)
    }

    override fun toClientTag(tag: NbtCompound) {
        fluidComponent!!.toTag(tag)
    }

    override fun fromClientTag(tag: NbtCompound) {
        fluidComponent!!.fromTag(tag)
    }

    companion object {
        const val TANK_ID = 4
        const val GENERATION_RATIO_ID = 5
    }
}