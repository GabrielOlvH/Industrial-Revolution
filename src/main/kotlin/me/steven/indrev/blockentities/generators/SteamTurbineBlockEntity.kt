package me.steven.indrev.blockentities.generators

import alexiil.mc.lib.attributes.Simulation
import alexiil.mc.lib.attributes.fluid.FluidVolumeUtil
import alexiil.mc.lib.attributes.fluid.amount.FluidAmount
import alexiil.mc.lib.attributes.fluid.filter.FluidFilter
import alexiil.mc.lib.attributes.fluid.volume.FluidKey
import alexiil.mc.lib.attributes.fluid.volume.FluidKeys
import alexiil.mc.lib.attributes.fluid.volume.FluidVolume
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap
import me.steven.indrev.api.machines.Tier
import me.steven.indrev.components.fluid.FluidComponent
import me.steven.indrev.components.multiblock.MultiBlockComponent
import me.steven.indrev.components.multiblock.SteamTurbineStructureDefinition
import me.steven.indrev.registry.IRBlockRegistry
import me.steven.indrev.registry.IRFluidRegistry
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.MB
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.block.BlockState
import net.minecraft.nbt.CompoundTag
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class SteamTurbineBlockEntity : GeneratorBlockEntity(Tier.MK4, MachineRegistry.STEAM_TURBINE_REGISTRY) {

    init {
        this.multiblockComponent = SteamTurbineMultiblockComponent()
        this.fluidComponent = SteamTurbineFluidComponent()
    }

    var efficiency = 1.0

    //these values are used for the screen handler
    @Environment(EnvType.CLIENT)
    var generating: Double = 0.0
    @Environment(EnvType.CLIENT)
    var consuming: FluidAmount = FluidAmount.ZERO

    override fun getGenerationRatio(): Double {
        val radius = getRadius()
        val eff  = efficiency * 70
        return ((eff * eff) / (radius / 2.0)) * config.ratio
    }

    override fun shouldGenerate(): Boolean {
        val amount = getConsumptionRatio()
        val result = fluidComponent!!.attemptExtraction(STEAM_FILTER, amount, Simulation.SIMULATE)
        if (result.amount() != amount)
            return false
        fluidComponent!!.extract(STEAM_FLUID_KEY, amount)
        return true
    }

    private fun getConsumptionRatio(): FluidAmount {
        return FluidAmount.ofWhole((getRadius() * 10 * efficiency).toLong()).coerceAtLeast(MB)
    }

    private fun getRadius(): Int {
        return 7
        val matcher = multiblockComponent!!.getSelectedMatcher(world!!, pos, cachedState)
        return SteamTurbineStructureDefinition.getRadius(matcher.structureIds.firstOrNull() ?: return 0)
    }

    override fun supportsInsertion(): Boolean = false

    private inner class SteamTurbineFluidComponent : FluidComponent(this, FluidAmount.ofWhole(1), 1) {

        override fun getMaxAmount_F(tank: Int): FluidAmount {
            return FluidAmount.ofWhole(getRadius() * 10L)
        }

        override fun isFluidValidForTank(tank: Int, fluid: FluidKey?): Boolean {
            return fluid?.rawFluid?.matchesType(IRFluidRegistry.STEAM_STILL) == true
        }

        override fun insertFluid(tank: Int, volume: FluidVolume, simulation: Simulation): FluidVolume {
            //divided by 4 because it's the limit of the input valves
            val limit = getMaxAmount_F(tank).div(4)
            val result = FluidVolumeUtil.computeInsertion(getInvFluid(tank), limit, volume)
            val leftover = when {
                result.result === volume -> volume
                setInvFluid(tank, result.inTank, simulation) -> result.result
                else -> volume
            }
            return leftover
        }
    }

    private inner class SteamTurbineMultiblockComponent : MultiBlockComponent({ id -> id.structure == "steam_turbine" }, { _, _, _ -> SteamTurbineStructureDefinition }) {
        override fun tick(world: World, pos: BlockPos, blockState: BlockState) {
            super.tick(world, pos, blockState)
            SteamTurbineStructureDefinition
                .getInputValvePositions(pos, blockState, getSelectedMatcher(world, pos, blockState))
                .forEach { valvePos ->
                    val valveBlockState = world.getBlockState(valvePos)
                    if (valveBlockState.isOf(IRBlockRegistry.FLUID_VALVE))
                        FLUID_VALVES_MAPPER[valvePos.asLong()] = pos.asLong()
                    else
                        FLUID_VALVES_MAPPER.remove(valvePos.asLong())
                }

            if (!isBuilt(world, pos, blockState)) {
                fluidComponent!![0] = fluidComponent!![0].withAmount(fluidComponent!!.getMaxAmount_F(0))
            }
        }
    }

    override fun toTag(tag: CompoundTag?): CompoundTag {
        tag?.putDouble("Efficiency", efficiency)
        return super.toTag(tag)
    }

    override fun fromTag(state: BlockState?, tag: CompoundTag?) {
        efficiency = tag?.getDouble("Efficiency") ?: efficiency
        super.fromTag(state, tag)
    }

    override fun toClientTag(tag: CompoundTag?): CompoundTag {
        tag?.putDouble("Efficiency", efficiency)
        tag?.putDouble("Generating", getGenerationRatio())
        tag?.put("Consuming", getConsumptionRatio().toNbt())
        return super.toClientTag(tag)
    }

    override fun fromClientTag(tag: CompoundTag?) {
        efficiency = tag?.getDouble("Efficiency") ?: efficiency
        generating = tag?.getDouble("Generating") ?: generating
        consuming = FluidAmount.fromNbt(tag?.getCompound("Consuming")) ?: consuming
        super.fromClientTag(tag)
    }

    companion object {
        val FLUID_VALVES_MAPPER = Long2LongOpenHashMap()

        val STEAM_FLUID_KEY: FluidKey = FluidKeys.get(IRFluidRegistry.STEAM_STILL)
        val STEAM_FILTER = FluidFilter { f -> f == STEAM_FLUID_KEY }
    }
}