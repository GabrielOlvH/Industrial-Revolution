package me.steven.indrev.blockentities.generators

import me.steven.indrev.api.machines.Tier
import me.steven.indrev.components.FluidComponent
import me.steven.indrev.components.autosync
import me.steven.indrev.components.multiblock.MultiBlockComponent
import me.steven.indrev.components.multiblock.definitions.SteamTurbineStructureDefinition
import me.steven.indrev.components.trackLong
import me.steven.indrev.registry.IRFluidRegistry
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.bucket
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant
import net.minecraft.block.BlockState
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class SteamTurbineBlockEntity(pos: BlockPos, state: BlockState) : GeneratorBlockEntity(Tier.MK4, MachineRegistry.STEAM_TURBINE_REGISTRY, pos, state) {

    init {
        this.multiblockComponent = SteamTurbineMultiblockComponent()
        this.fluidComponent = SteamTurbineFluidComponent()
    }

    override val maxInput: Long = 0

    var efficiency by autosync(EFFICIENCY, 1.0)

    var generatingTicks = 0
    //var totalInserted: FluidAmount = FluidAmount.ZERO

    init {
        trackLong(GENERATING) { getGenerationRatio() * 100 }
    }


    // used for the screen handler
    @Environment(EnvType.CLIENT)
   // var consuming: FluidAmount = FluidAmount.ZERO

    //override fun getGenerationRatio(): Long {
        //val radius = getRadius()
        //val eff  = totalInserted.div(20).asInexactDouble()
        //return (((eff * 2) / (radius.toDouble() / 7)) * 2048).toLong()
   // }

    override fun shouldGenerate(): Boolean {
       /* if (generatingTicks <= 0) {
            if (!totalInserted.isZero) {
                generatingTicks = 20
                fluidComponent!![0].extract(Long.MAX_VALUE, true)
            } else
                return false
        }
        generatingTicks--*/
      //  if (generatingTicks <= 0) totalInserted = FluidAmount.ZERO
        return true
    }

   // private fun getConsumptionRatio(): FluidAmount {
       // return totalInserted.div(20)
    //}

    private fun getRadius(): Int {
        //return 7
        val matcher = multiblockComponent!!.getSelectedMatcher(world ?: return 0, pos, cachedState)
        return SteamTurbineStructureDefinition.getRadius(matcher.builtId ?: return 0)
    }

    private inner class SteamTurbineFluidComponent : FluidComponent({ this }, bucket, 1) {

        override fun getTankCapacity(index: Int): Long {
            return (((getRadius() * getRadius().toLong()) * efficiency) * 81L).toLong()
        }

        override fun isFluidValidForTank(index: Int, variant: FluidVariant): Boolean {
            return variant.isOf(IRFluidRegistry.STEAM_STILL)
        }
    }

    private inner class SteamTurbineMultiblockComponent : MultiBlockComponent({ _, _, _ -> SteamTurbineStructureDefinition }) {
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
                fluidComponent!![0].amount = 0
            }
        }
    }

    override fun toTag(tag: NbtCompound) {
        tag.putDouble("Efficiency", efficiency)
        super.toTag(tag)
    }

    override fun fromTag(tag: NbtCompound) {
        efficiency = tag.getDouble("Efficiency")
        super.fromTag(tag)
    }

    override fun toClientTag(tag: NbtCompound) {
       // tag.put("Consuming", getConsumptionRatio().toNbt())
        multiblockComponent?.writeNbt(tag)
    }

    override fun fromClientTag(tag: NbtCompound) {
      //  consuming = FluidAmount.fromNbt(tag.getCompound("Consuming")) ?: consuming
        multiblockComponent?.readNbt(tag)
    }

    companion object {
        const val EFFICIENCY = 2
        const val GENERATING = 3
    }
}