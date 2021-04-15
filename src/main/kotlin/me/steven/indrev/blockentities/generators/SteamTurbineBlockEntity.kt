package me.steven.indrev.blockentities.generators

import alexiil.mc.lib.attributes.Simulation
import alexiil.mc.lib.attributes.fluid.FluidVolumeUtil
import alexiil.mc.lib.attributes.fluid.amount.FluidAmount
import alexiil.mc.lib.attributes.fluid.volume.FluidVolume
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap
import me.steven.indrev.api.machines.Tier
import me.steven.indrev.components.fluid.FluidComponent
import me.steven.indrev.components.multiblock.MultiBlockComponent
import me.steven.indrev.components.multiblock.SteamTurbineStructureDefinition
import me.steven.indrev.registry.IRBlockRegistry
import me.steven.indrev.registry.MachineRegistry
import net.minecraft.block.BlockState
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class SteamTurbineBlockEntity : GeneratorBlockEntity(Tier.MK4, MachineRegistry.STEAM_TURBINE_REGISTRY) {

    init {
        this.multiblockComponent = SteamTurbineMultiblockComponent()
        this.fluidComponent = SteamTurbineFluidComponent()
    }

    var insertedLastTick: Int = 0

    override fun machineTick() {
        super.machineTick()
        insertedLastTick = 0
    }

    override fun getGenerationRatio(): Double {
        val radius = getRadius()
        return ((insertedLastTick * insertedLastTick) / (radius / 2)) * config.ratio
    }

    override fun shouldGenerate(): Boolean = true

    private fun getRadius(): Int {
        val matcher = multiblockComponent!!.getSelectedMatcher(world!!, pos, cachedState)
        return SteamTurbineStructureDefinition.getRadius(matcher.structureIds.firstOrNull() ?: return 0)
    }

    private inner class SteamTurbineFluidComponent : FluidComponent({ this }, FluidAmount.ofWhole(1), 1) {

        override fun getMaxAmount_F(tank: Int): FluidAmount {
            return FluidAmount.ofWhole(getRadius() * 10L)
        }

        override fun insertFluid(tank: Int, volume: FluidVolume, simulation: Simulation): FluidVolume {
            //divided by 4 because it's the limit of the input valves
            val limit = getMaxAmount_F(tank).max(getMaxAmount_F(tank).div(4))
            val result = FluidVolumeUtil.computeInsertion(getInvFluid(tank), limit, volume)
            val leftover = when {
                result.result === volume -> volume
                setInvFluid(tank, result.inTank, simulation) -> result.result
                else -> volume
            }
            if (simulation.isAction)
                insertedLastTick += volume.amount().asInt(1000) - leftover.amount().asInt(1000)
            return leftover
        }
    }

    private inner class SteamTurbineMultiblockComponent : MultiBlockComponent({ id -> id.structure == "steam_turbine"}, { _, _, _ -> SteamTurbineStructureDefinition }) {
        override fun tick(world: World, pos: BlockPos, blockState: BlockState) {
            super.tick(world, pos, blockState)
            SteamTurbineStructureDefinition
                .getInputValves(pos, blockState, getSelectedMatcher(world, pos, blockState))
                .forEach { valvePos ->
                    val valveBlockState = world.getBlockState(valvePos)
                    if (valveBlockState.isOf(IRBlockRegistry.STEAM_TURBINE_INPUT_VALVE))
                        INPUT_VALVES_MAPPER[valvePos.asLong()] = pos.asLong()
                    else
                        INPUT_VALVES_MAPPER.remove(valvePos.asLong())
                }

            if (!isBuilt(world, pos, blockState)) {
                fluidComponent!![0] = fluidComponent!![0].withAmount(fluidComponent!!.getMaxAmount_F(0))
            }
        }
    }

    companion object {
        val INPUT_VALVES_MAPPER = Long2LongOpenHashMap()
    }
}