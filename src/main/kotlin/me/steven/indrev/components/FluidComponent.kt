package me.steven.indrev.components

import alexiil.mc.lib.attributes.ListenerRemovalToken
import alexiil.mc.lib.attributes.ListenerToken
import alexiil.mc.lib.attributes.Simulation
import alexiil.mc.lib.attributes.fluid.FixedFluidInv
import alexiil.mc.lib.attributes.fluid.FluidInvTankChangeListener
import alexiil.mc.lib.attributes.fluid.amount.FluidAmount
import alexiil.mc.lib.attributes.fluid.volume.FluidKey
import alexiil.mc.lib.attributes.fluid.volume.FluidKeys
import alexiil.mc.lib.attributes.fluid.volume.FluidVolume
import net.minecraft.nbt.CompoundTag
import net.minecraft.util.math.Direction

class FluidComponent(val limit: FluidAmount) : FixedFluidInv {

    var volume: FluidVolume = object : FluidVolume(FluidKeys.WATER, FluidAmount.ZERO) {}

    val transferConfig: MutableMap<Direction, TransferMode> = mutableMapOf<Direction, TransferMode>().also { map ->
        Direction.values().forEach { dir -> map[dir] = TransferMode.NONE }
    }


    override fun getTankCount(): Int = 1

    override fun addListener(
        listener: FluidInvTankChangeListener?,
        removalToken: ListenerRemovalToken?
    ): ListenerToken {
        return ListenerToken { }
    }

    override fun getMaxAmount_F(tank: Int): FluidAmount = limit

    override fun setInvFluid(tank: Int, to: FluidVolume, simulation: Simulation?): Boolean {
        return if (isFluidValidForTank(tank, to.fluidKey)) {
            volume = to
            true
        } else false
    }

    override fun isFluidValidForTank(tank: Int, fluid: FluidKey?): Boolean = fluid == volume.fluidKey || volume.isEmpty

    override fun getInvFluid(tank: Int): FluidVolume = volume

    fun toTag(tag: CompoundTag) {
        tag.put("fluids", volume.toTag())
        val icTag = CompoundTag()
        transferConfig.forEach { (dir, mode) ->
            icTag.putString(dir.toString(), mode.toString())
        }
        tag.put("TransferConfig", icTag)
    }

    fun fromTag(tag: CompoundTag?) {
        val f = tag?.getCompound("fluids")
        volume = FluidVolume.fromTag(f)
        if (tag?.contains("TransferConfig") == true) {
            val icTag = tag.getCompound("TransferConfig")
            Direction.values().forEach { dir ->
                val value = icTag.getString(dir.toString()).toUpperCase()
                if (value.isNotEmpty()) {
                    val mode = TransferMode.valueOf(value)
                    transferConfig[dir] = mode
                }
            }
        }
    }
}