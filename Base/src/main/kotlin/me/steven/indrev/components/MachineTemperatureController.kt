package me.steven.indrev.components

import net.minecraft.nbt.NbtCompound
import net.minecraft.network.PacketByteBuf
import net.minecraft.util.math.random.Random

open class MachineTemperatureController(override val syncId: Int, val average: Int, onChange: () -> Unit) : SyncableObject {

    override var isDirty: Boolean = false

    val coolerInventory = MachineItemInventory(size = 1, canInsert = inSlots(0), canExtract = outSlots(0), onChange = onChange)

    var heating = false
    var temperature = 25.0
    var coolingTicks = 0

    val capacity = average + 500

    fun isWithinAverage() = temperature.toInt() in average - 250..average + 250

    fun isFullEfficiency() = isWithinAverage() && (heating || !coolerInventory[0].isEmpty())

    fun tick(random: Random) {
        if (temperature + 50 > average + 250) {
            heating = false
            coolingTicks = 1000
        }
        coolingTicks--
        if (coolingTicks > 0) heating = false

        val before = temperature.toInt()

        if (heating) {
            temperature += 0.05 + (random.nextFloat()/10)
        } else if (temperature > 30.5) {
            temperature -= 0.05 - (random.nextFloat()/16)
        } else {
            temperature -= random.nextGaussian() / 16
        }

        temperature = temperature.coerceIn(20.0, capacity.toDouble())

        val after = temperature.toInt()
        if (before != after) {
            isDirty = true
        }

    }

    fun writeNbt(): NbtCompound {
        val nbt = NbtCompound()
        nbt.putDouble("tmp", temperature)
        nbt.put("inv", coolerInventory.writeNbt())
        return nbt
    }

    fun readNbt(nbt: NbtCompound) {
        this.temperature = nbt.getDouble("tmp")
        this.coolerInventory.readNbt(nbt.getCompound("inv"))
    }

    fun exists() = this != NullTemperatureController

    override fun fromPacket(buf: PacketByteBuf) {
        temperature = buf.readDouble()
        heating = buf.readBoolean()
    }

    override fun toPacket(buf: PacketByteBuf) {
        buf.writeDouble(temperature)
        buf.writeBoolean(heating)
    }
}

object NullTemperatureController : MachineTemperatureController(-1, -1, {})