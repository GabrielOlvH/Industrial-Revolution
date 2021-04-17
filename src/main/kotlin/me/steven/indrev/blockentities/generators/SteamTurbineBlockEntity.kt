package me.steven.indrev.blockentities.generators

import alexiil.mc.lib.attributes.Simulation
import alexiil.mc.lib.attributes.fluid.FluidVolumeUtil
import alexiil.mc.lib.attributes.fluid.amount.FluidAmount
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

    var maxSpeed: Double = 0.0
    var speed: Double = 0.0

    @Environment(EnvType.CLIENT)
    var generating: Double = 0.0

    override fun machineTick() {
        super.machineTick()
        speed += (ACCELERATION * if (maxSpeed < speed) -0.05 else 1.0)
        speed = speed.coerceIn(0.0, maxSpeed)
        if (ticks % 10 == 0) maxSpeed = 0.0
    }

    override fun getGenerationRatio(): Double {
        val radius = getRadius()
        return ((speed * speed) / (radius / 2.0)) * config.ratio
    }

    override fun shouldGenerate(): Boolean {
        fluidComponent!![0] = FluidKeys.EMPTY.withAmount(FluidAmount.ZERO)
        return true
    }

    private fun getRadius(): Int {
        return 7
        val matcher = multiblockComponent!!.getSelectedMatcher(world!!, pos, cachedState)
        return SteamTurbineStructureDefinition.getRadius(matcher.structureIds.firstOrNull() ?: return 0)
    }

    private inner class SteamTurbineFluidComponent : FluidComponent({ this }, FluidAmount.ofWhole(1), 1) {

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
            if (simulation.isAction)
                maxSpeed += (volume.amount().asInexactDouble() - leftover.amount().asInexactDouble())
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

    override fun toTag(tag: CompoundTag?): CompoundTag {
        tag?.putDouble("Speed", speed)
        tag?.putDouble("MaxSpeed", maxSpeed)
        return super.toTag(tag)
    }

    override fun fromTag(state: BlockState?, tag: CompoundTag?) {
        speed = tag?.getDouble("Speed") ?: speed
        maxSpeed = tag?.getDouble("MaxSpeed") ?: maxSpeed
        super.fromTag(state, tag)
    }

    override fun toClientTag(tag: CompoundTag?): CompoundTag {
        tag?.putDouble("Speed", speed)
        tag?.putDouble("MaxSpeed", maxSpeed)
        tag?.putDouble("Generating", getGenerationRatio())
        return super.toClientTag(tag)
    }

    override fun fromClientTag(tag: CompoundTag?) {
        speed = tag?.getDouble("Speed") ?: speed
        maxSpeed = tag?.getDouble("MaxSpeed") ?: maxSpeed
        generating = tag?.getDouble("Generating") ?: generating
        super.fromClientTag(tag)
    }

    companion object {
        val INPUT_VALVES_MAPPER = Long2LongOpenHashMap()

        const val ACCELERATION: Double = 0.01
    }
}