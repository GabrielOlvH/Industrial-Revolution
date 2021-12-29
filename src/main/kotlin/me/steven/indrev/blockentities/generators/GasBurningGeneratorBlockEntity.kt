package me.steven.indrev.blockentities.generators

import me.steven.indrev.api.machines.Tier
import me.steven.indrev.api.machines.TransferMode
import me.steven.indrev.api.sideconfigs.ConfigurationType
import me.steven.indrev.components.FluidComponent
import me.steven.indrev.components.TemperatureComponent
import me.steven.indrev.components.autosync
import me.steven.indrev.components.trackObject
import me.steven.indrev.inventories.inventory
import me.steven.indrev.registry.IRFluidFuelRegistry
import me.steven.indrev.registry.IRItemRegistry
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.bucket
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant
import net.minecraft.block.BlockState
import net.minecraft.item.ItemStack
import net.minecraft.util.math.BlockPos

class GasBurningGeneratorBlockEntity(pos: BlockPos, state: BlockState) : GeneratorBlockEntity(Tier.MK4, MachineRegistry.GAS_BURNING_GENERATOR_REGISTRY, pos, state) {

    private var burnTime by autosync(BURN_TIME_ID, 0)
    private var maxBurnTime by autosync(TOTAL_BURN_TIME_ID, 0)

    init {
        this.temperatureComponent = TemperatureComponent(this, 0.3, 2200..2400, 2500)
        this.inventoryComponent = inventory(this) {
            coolerSlot = 0
            output {
                slot = 1
            }
        }
        this.fluidComponent = GasBurningGeneratorFluidComponent()
    }

    var generatingTicks = 0

    override fun machineTick() {
        super.machineTick()
        if (workingState) {
            generatingTicks++

            if (generatingTicks % 100 == 0 && world!!.random.nextDouble() < 0.6) {
                inventoryComponent!!.inventory.output(ItemStack(IRItemRegistry.SOOT))
            }
        }
    }

    override fun shouldGenerate(): Boolean {
        if (burnTime > 0) burnTime--
        else if (getCapacity() > energy) {
            val invFluid = fluidComponent!![0]
            val fluid = invFluid.resource.fluid
            if (invFluid.isEmpty || !IRFluidFuelRegistry.isFuel(fluid)) return false
            val fuel = IRFluidFuelRegistry.get(fluid)!!
            if (fluidComponent!![0].extract(fuel.consumptionRatio) == fuel.consumptionRatio) {
                burnTime = fuel.burnTime
                maxBurnTime = burnTime
            }
        }

        return burnTime > 0 && energy < getCapacity()
    }

    override fun getGenerationRatio(): Long {
        val invFluid = fluidComponent!![0]
        val fluid = invFluid.resource.fluid
        val modifier = if (temperatureComponent!!.isFullEfficiency()) config.temperatureBoost else 1.0
        return ((IRFluidFuelRegistry.get(fluid)?.generationRatio ?: 0) * modifier).toLong()
    }

    override fun getValidConfigurations(type: ConfigurationType): Array<TransferMode> {
        return when (type) {
            ConfigurationType.ITEM -> arrayOf(TransferMode.OUTPUT, TransferMode.NONE)
            ConfigurationType.FLUID -> arrayOf(TransferMode.INPUT, TransferMode.NONE)
            else -> super.getValidConfigurations(type)
        }
    }

    inner class GasBurningGeneratorFluidComponent : FluidComponent({ this }, bucket * 2, 1) {

        init {
            trackObject(TANK_ID, this[0])
        }

        override fun isFluidValidForTank(index: Int, variant: FluidVariant): Boolean {
            return IRFluidFuelRegistry.isFuel(variant.fluid)
        }
    }

    companion object {
        const val BURN_TIME_ID = 4
        const val TOTAL_BURN_TIME_ID = 5
        const val TANK_ID = 6
    }
}