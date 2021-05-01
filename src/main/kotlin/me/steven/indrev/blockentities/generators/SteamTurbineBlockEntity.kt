package me.steven.indrev.blockentities.generators

import alexiil.mc.lib.attributes.Simulation
import alexiil.mc.lib.attributes.fluid.amount.FluidAmount
import alexiil.mc.lib.attributes.fluid.filter.FluidFilter
import alexiil.mc.lib.attributes.fluid.volume.FluidKey
import alexiil.mc.lib.attributes.fluid.volume.FluidKeys
import alexiil.mc.lib.attributes.fluid.volume.FluidVolume
import me.steven.indrev.api.machines.Tier
import me.steven.indrev.components.fluid.FluidComponent
import me.steven.indrev.components.multiblock.MultiBlockComponent
import me.steven.indrev.components.multiblock.SteamTurbineStructureDefinition
import me.steven.indrev.registry.IRFluidRegistry
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.minus
import me.steven.indrev.utils.plus
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
    var generatingTicks = 0
    var totalInserted: FluidAmount = FluidAmount.ZERO

    //these values are used for the screen handler
    @Environment(EnvType.CLIENT)
    var generating: Double = 0.0
    @Environment(EnvType.CLIENT)
    var consuming: FluidAmount = FluidAmount.ZERO

    override fun getGenerationRatio(): Double {
        val radius = getRadius()
        val eff  = efficiency * radius * 10
        return ((eff * eff) * (radius.toDouble() / 7.0)) * config.ratio * (totalInserted.div(20)).asInexactDouble()
    }

    override fun shouldGenerate(): Boolean {
        if (generatingTicks <= 0) {
            if (!totalInserted.isZero)
                generatingTicks = 20
            else
                return false
        }
        generatingTicks--
        if (generatingTicks <= 0) totalInserted = FluidAmount.ZERO
        return true
    }

    private fun getConsumptionRatio(): FluidAmount {
        return totalInserted.div(20)
    }

    private fun getRadius(): Int {
        //return 7
        val matcher = multiblockComponent!!.getSelectedMatcher(world!!, pos, cachedState)
        return SteamTurbineStructureDefinition.getRadius(matcher.structureIds.firstOrNull() ?: return 0)
    }

    override fun supportsInsertion(): Boolean = false

    private inner class SteamTurbineFluidComponent : FluidComponent(this, FluidAmount.ofWhole(1), 1) {

        override fun getMaxAmount_F(tank: Int): FluidAmount {
            return FluidAmount.ofWhole(64L)
        }

        override fun isFluidValidForTank(tank: Int, fluid: FluidKey?): Boolean {
            return fluid?.rawFluid?.matchesType(IRFluidRegistry.STEAM_STILL) == true
        }

        override fun attemptInsertion(fluid: FluidVolume, simulation: Simulation): FluidVolume {
            if (generatingTicks > 0) return fluid
            val result = super.attemptInsertion(fluid, simulation)
            if (simulation.isAction) {
                val actual = fluid.amount_F - result.amount_F
                totalInserted += actual
                extract(actual)
            }
            return result
        }
    }

    private inner class SteamTurbineMultiblockComponent : MultiBlockComponent({ id -> id.structure == "steam_turbine" }, { _, _, _ -> SteamTurbineStructureDefinition }) {
        override fun tick(world: World, pos: BlockPos, blockState: BlockState) {
            super.tick(world, pos, blockState)
            SteamTurbineStructureDefinition
                .getInputValvePositions(pos, blockState, getSelectedMatcher(world, pos, blockState))
                .forEach { valvePos ->
                    val valveBlockEntity = world.getBlockEntity(valvePos)
                    if (valveBlockEntity is SteamTurbineSteamInputValveBlockEntity)
                        valveBlockEntity.steamTurbinePos = pos
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
        val STEAM_FLUID_KEY: FluidKey = FluidKeys.get(IRFluidRegistry.STEAM_STILL)
        val STEAM_FILTER = FluidFilter { f -> f == STEAM_FLUID_KEY }
    }
}