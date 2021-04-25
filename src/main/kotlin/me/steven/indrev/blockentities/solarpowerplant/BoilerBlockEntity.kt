package me.steven.indrev.blockentities.solarpowerplant

import alexiil.mc.lib.attributes.Simulation
import alexiil.mc.lib.attributes.fluid.FluidTransferable
import alexiil.mc.lib.attributes.fluid.GroupedFluidInv
import alexiil.mc.lib.attributes.fluid.amount.FluidAmount
import alexiil.mc.lib.attributes.fluid.filter.FluidFilter
import alexiil.mc.lib.attributes.fluid.volume.FluidVolume
import me.steven.indrev.components.fluid.FluidComponent
import me.steven.indrev.components.multiblock.BoilerStructureDefinition
import me.steven.indrev.components.multiblock.MultiBlockComponent
import me.steven.indrev.registry.IRBlockRegistry
import me.steven.indrev.utils.createWrapper
import net.minecraft.block.entity.BlockEntity

class BoilerBlockEntity : BlockEntity(IRBlockRegistry.BOILER_BLOCK_ENTITY) {
    val multiblockComponent = MultiBlockComponent({ id -> id.structure == "boiler" }) { _, _, _ -> BoilerStructureDefinition}
    val fluidComponent = BoilerFluidComponent()

    inner class BoilerFluidComponent : FluidComponent(FluidAmount.ofWhole(4), 2) {
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
}