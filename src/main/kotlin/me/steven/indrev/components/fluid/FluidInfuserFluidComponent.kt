package me.steven.indrev.components.fluid

import alexiil.mc.lib.attributes.Simulation
import alexiil.mc.lib.attributes.fluid.FluidTransferable
import alexiil.mc.lib.attributes.fluid.GroupedFluidInv
import alexiil.mc.lib.attributes.fluid.amount.FluidAmount
import alexiil.mc.lib.attributes.fluid.filter.FluidFilter
import alexiil.mc.lib.attributes.fluid.volume.FluidVolume
import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.utils.createWrapper

class FluidInfuserFluidComponent(machine: () -> MachineBlockEntity<*>) : FluidComponent(machine, FluidAmount.ofWhole(8) , 2) {
    init {
        this.inputTanks = intArrayOf(0)
        this.outputTanks = intArrayOf(1)
    }

    private val grouped = this.createWrapper(1, 0)

    override fun getGroupedInv(): GroupedFluidInv = grouped

    override fun attemptInsertion(fluid: FluidVolume, simulation: Simulation?): FluidVolume? {
        return grouped.attemptInsertion(fluid, simulation)
    }

    override fun getMinimumAcceptedAmount(): FluidAmount? {
        return grouped.minimumAcceptedAmount
    }

    override fun getInsertionFilter(): FluidFilter? {
        return grouped.insertionFilter
    }

    override fun attemptExtraction(
        filter: FluidFilter?,
        maxAmount: FluidAmount,
        simulation: Simulation?
    ): FluidVolume {
        return grouped.attemptExtraction(filter, maxAmount, simulation)
    }

    override fun attemptAnyExtraction(maxAmount: FluidAmount?, simulation: Simulation?): FluidVolume? {
        return grouped.attemptAnyExtraction(maxAmount, simulation)
    }

    override fun getInteractInventory(tank: Int): FluidTransferable {
        return createWrapper(tank, tank)
    }
}