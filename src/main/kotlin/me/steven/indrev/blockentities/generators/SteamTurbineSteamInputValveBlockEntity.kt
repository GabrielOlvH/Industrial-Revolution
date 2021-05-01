package me.steven.indrev.blockentities.generators

import alexiil.mc.lib.attributes.Simulation
import alexiil.mc.lib.attributes.fluid.FluidTransferable
import alexiil.mc.lib.attributes.fluid.GroupedFluidInv
import alexiil.mc.lib.attributes.fluid.GroupedFluidInvView
import alexiil.mc.lib.attributes.fluid.amount.FluidAmount
import alexiil.mc.lib.attributes.fluid.filter.FluidFilter
import alexiil.mc.lib.attributes.fluid.volume.FluidKey
import alexiil.mc.lib.attributes.fluid.volume.FluidKeys
import alexiil.mc.lib.attributes.fluid.volume.FluidVolume
import me.steven.indrev.registry.IRBlockRegistry
import me.steven.indrev.registry.IRFluidRegistry
import net.minecraft.block.entity.BlockEntity
import net.minecraft.util.Tickable
import net.minecraft.util.math.BlockPos

class SteamTurbineSteamInputValveBlockEntity : BlockEntity(IRBlockRegistry.STEAM_TURBINE_STEAM_INPUT_VALVE_BLOCK_ENTITY), Tickable {

    val fluidInv = SteamTurbineSteamInputValveFluidInventoryWrapper()
    var steamTurbinePos: BlockPos = BlockPos(-1, -1, -1)
    var inserted = false

    override fun tick() {
        inserted = false
    }

    fun getSteamTurbine(): SteamTurbineBlockEntity? {
        val blockEntity = world!!.getBlockEntity(steamTurbinePos) as? SteamTurbineBlockEntity ?: return null
        return if (blockEntity.multiblockComponent?.isBuilt(world!!, pos, blockEntity.cachedState) == true)
            blockEntity
        else null
    }

    inner class SteamTurbineSteamInputValveFluidInventoryWrapper : FluidTransferable, GroupedFluidInv {
        override fun getStoredFluids(): MutableSet<FluidKey> = mutableSetOf()

        override fun getStatistics(filter: FluidFilter?): GroupedFluidInvView.FluidInvStatistic = GroupedFluidInvView.FluidInvStatistic.emptyOf { IRFluidRegistry.STEAM_STILL == it.rawFluid }

        override fun attemptInsertion(fluid: FluidVolume, simulation: Simulation): FluidVolume {
            if (inserted) return fluid
            val steamTurbine = getSteamTurbine()
            return if (steamTurbine == null) fluid
            else {
                val volume = steamTurbine.fluidComponent!!.attemptInsertion(fluid, simulation)
                if (simulation.isAction && fluid != volume) inserted = true
                volume
            }
        }

        override fun attemptExtraction(
            filter: FluidFilter?,
            maxAmount: FluidAmount?,
            simulation: Simulation?
        ): FluidVolume = FluidKeys.EMPTY.withAmount(FluidAmount.ZERO)

    }
}