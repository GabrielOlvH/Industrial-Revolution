package me.steven.indrev.blockentities.generators

import alexiil.mc.lib.attributes.Simulation
import alexiil.mc.lib.attributes.fluid.amount.FluidAmount
import alexiil.mc.lib.attributes.fluid.filter.FluidFilter
import alexiil.mc.lib.attributes.fluid.volume.FluidKey
import alexiil.mc.lib.attributes.fluid.volume.FluidKeys
import alexiil.mc.lib.attributes.fluid.volume.FluidVolume
import me.steven.indrev.api.machines.Tier
import me.steven.indrev.components.FluidComponent
import me.steven.indrev.components.multiblock.MultiBlockComponent
import me.steven.indrev.components.multiblock.SteamTurbineStructureDefinition
import me.steven.indrev.registry.IRFluidRegistry
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.minus
import me.steven.indrev.utils.plus
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable
import net.minecraft.block.BlockState
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class SteamTurbineBlockEntity(pos: BlockPos, state: BlockState) : GeneratorBlockEntity(Tier.MK4, MachineRegistry.STEAM_TURBINE_REGISTRY, pos, state), BlockEntityClientSerializable {

    init {
        this.multiblockComponent = SteamTurbineMultiblockComponent()
        this.fluidComponent = SteamTurbineFluidComponent()
    }

    var efficiency = 1.0
    var generatingTicks = 0
    var totalInserted: FluidAmount = FluidAmount.ZERO

    // used for the screen handler
    @Environment(EnvType.CLIENT)
    var consuming: FluidAmount = FluidAmount.ZERO

    override fun getGenerationRatio(): Double {
        val radius = getRadius()
        val eff  = totalInserted.div(20).asInexactDouble()
        return ((eff * 2) / (radius.toDouble() / 7)) * 2048
    }

    override fun shouldGenerate(): Boolean {
        if (generatingTicks <= 0) {
            if (!totalInserted.isZero) {
                generatingTicks = 20
                fluidComponent!!.extract(FluidAmount.MAX_BUCKETS)
            } else
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

    private inner class SteamTurbineFluidComponent : FluidComponent({this}, FluidAmount.ofWhole(1), 1) {

        override fun getMaxAmount_F(tank: Int): FluidAmount {
            return FluidAmount.ofWhole(getRadius() * getRadius().toLong()).mul((efficiency * 100).toLong()).div(100)
        }

        override fun isFluidValidForTank(tank: Int, fluid: FluidKey?): Boolean {
            return fluid?.rawFluid?.matchesType(IRFluidRegistry.STEAM_STILL) == true
        }

        override fun attemptInsertion(fluid: FluidVolume, simulation: Simulation): FluidVolume {
            if (generatingTicks > 0) return fluid
            val result = super.attemptInsertion(fluid, simulation)
            if (simulation.isAction)
                totalInserted += fluid.amount_F - result.amount_F
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

    override fun get(index: Int): Int {
        return when (index) {
            EFFICIENCY -> (efficiency * 100).toInt()
            GENERATING -> (getGenerationRatio() * 100).toInt()
            else -> super.get(index)
        }
    }

    override fun writeNbt(tag: NbtCompound?): NbtCompound {
        tag?.putDouble("Efficiency", efficiency)
        return super.writeNbt(tag)
    }

    override fun readNbt(tag: NbtCompound?) {
        efficiency = tag?.getDouble("Efficiency") ?: efficiency
        super.readNbt(tag)
    }

    override fun toClientTag(tag: NbtCompound): NbtCompound {
        tag.put("Consuming", getConsumptionRatio().toNbt())
        multiblockComponent?.writeNbt(tag)
        return tag
    }

    override fun fromClientTag(tag: NbtCompound) {
        consuming = FluidAmount.fromNbt(tag.getCompound("Consuming")) ?: consuming
        multiblockComponent?.readNbt(tag)
    }

    companion object {
        val STEAM_FLUID_KEY: FluidKey = FluidKeys.get(IRFluidRegistry.STEAM_STILL)
        val STEAM_FILTER = FluidFilter { f -> f == STEAM_FLUID_KEY }

        const val EFFICIENCY = 2
        const val GENERATING = 3
    }
}