package me.steven.indrev.components.fluid

import alexiil.mc.lib.attributes.ListenerRemovalToken
import alexiil.mc.lib.attributes.ListenerToken
import alexiil.mc.lib.attributes.Simulation
import alexiil.mc.lib.attributes.fluid.FixedFluidInv
import alexiil.mc.lib.attributes.fluid.FluidInvTankChangeListener
import alexiil.mc.lib.attributes.fluid.amount.FluidAmount
import alexiil.mc.lib.attributes.fluid.volume.FluidKey
import alexiil.mc.lib.attributes.fluid.volume.FluidKeys
import alexiil.mc.lib.attributes.fluid.volume.FluidVolume
import me.steven.indrev.blockentities.IRSyncableBlockEntity
import me.steven.indrev.utils.TransferMode
import net.minecraft.nbt.CompoundTag
import net.minecraft.util.math.Direction

open class FluidComponent(val syncable: () -> IRSyncableBlockEntity?, val limit: FluidAmount, private val tankCount: Int = 1) : FixedFluidInv {

    val tanks = Array(tankCount) { IRTank(FluidKeys.EMPTY.withAmount(FluidAmount.ZERO)) }

    val transferConfig: MutableMap<Direction, TransferMode> = mutableMapOf<Direction, TransferMode>().also { map ->
        Direction.values().forEach { dir -> map[dir] = TransferMode.NONE }
    }

    override fun getTankCount(): Int = tankCount

    override fun addListener(
        listener: FluidInvTankChangeListener?,
        removalToken: ListenerRemovalToken?
    ): ListenerToken {
        return ListenerToken { }
    }

    override fun getMaxAmount_F(tank: Int): FluidAmount = limit

    override fun setInvFluid(tankIndex: Int, to: FluidVolume, simulation: Simulation?): Boolean {
        return if (isFluidValidForTank(tankIndex, to.fluidKey)) {
            if (simulation?.isAction == true) {
                val tank = tanks[tankIndex]
                syncable()?.markForUpdate { tank.volume != to }
                tank.volume = to
            }
            true
        } else false
    }

    override fun isFluidValidForTank(tank: Int, fluid: FluidKey?): Boolean =
        fluid == tanks[tank].volume.fluidKey || tanks[tank].volume.isEmpty

    override fun getInvFluid(tank: Int): FluidVolume = tanks[tank].volume

    fun toTag(tag: CompoundTag) {
        val tanksTag = CompoundTag()
        tanks.forEachIndexed { index, tank ->
            val tankTag = CompoundTag()
            tankTag.put("fluids", tank.volume.toTag())
            tanksTag.put(index.toString(), tankTag)
        }
        tag.put("tanks", tanksTag)
        val icTag = CompoundTag()
        transferConfig.forEach { (dir, mode) ->
            icTag.putString(dir.toString(), mode.toString())
        }
        tag.put("TransferConfig", icTag)
    }

    fun fromTag(tag: CompoundTag?) {
        val tanksTag = tag?.getCompound("tanks")
        tanksTag?.keys?.forEach { key ->
            val index = key.toInt()
            val tankTag = tanksTag.getCompound(key)
            val volume = FluidVolume.fromTag(tankTag.getCompound("fluids"))
            val tank = tanks[index]
            tank.volume = volume
        }
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

    class IRTank(var volume: FluidVolume)
}