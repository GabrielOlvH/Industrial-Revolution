package me.steven.indrev.blockentities

import me.steven.indrev.items.CoolerItem
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.Tier
import net.minecraft.nbt.CompoundTag

abstract class HeatMachineBlockEntity(tier: Tier, registry: MachineRegistry) :
    InterfacedMachineBlockEntity(tier, registry) {
    var temperature = 300.0
        set(value) {
            field = value.coerceAtLeast(0.0).apply { propertyDelegate[2] = this.toInt() }
        }
        get() = field.apply { propertyDelegate[2] = this.toInt() }
    var cooling = 0

    override fun fromTag(tag: CompoundTag?) {
        temperature = tag?.getDouble("Temperature") ?: 0.0
        cooling = tag?.getInt("Cooling") ?: 0
        super.fromTag(tag)
    }

    override fun toTag(tag: CompoundTag?): CompoundTag {
        tag?.putDouble("Temperature", temperature)
        tag?.putInt("Cooling", cooling)
        return super.toTag(tag)
    }

    override fun fromClientTag(tag: CompoundTag?) {
        temperature = tag?.getDouble("Temperature") ?: 0.0
        cooling = tag?.getInt("Cooling") ?: 0
        super.fromClientTag(tag)
    }

    override fun toClientTag(tag: CompoundTag?): CompoundTag {
        tag?.putDouble("Temperature", temperature)
        tag?.putInt("Cooling", cooling)
        return super.toClientTag(tag)
    }

    protected fun tickTemperature(isHeatingUp: Boolean) {
        val coolerStack = this.getInventory(null, null, null).getInvStack(1)
        val coolerItem = coolerStack.item

        if (isHeatingUp && coolerItem is CoolerItem
            && coolerStack.damage < coolerStack.maxDamage
            && (cooling > 0
                || temperature + 250 >= getOptimalRange().last)
        ) {
            cooling--
            if (temperature + 350 < getOptimalRange().last && cooling <= 0) {
                cooling = 200
                coolerStack.damage++
            }
            val modifier =
                if (temperature + 25 >= getOptimalRange().last) coolerItem.activeCoolingModifier else coolerItem.passiveCoolingModifier
            temperature += getBaseHeatingEfficiency() + modifier
        } else if (isHeatingUp) temperature += getBaseHeatingEfficiency()
        else if (temperature > 310) temperature -= getBaseHeatingEfficiency() / 2
    }


    fun getCurrentTemperature(): Double = temperature

    fun setCurrentTemperature(temperature: Double) {
        this.temperature = temperature
    }

    abstract fun getOptimalRange(): IntRange
    abstract fun getBaseHeatingEfficiency(): Double
    abstract fun getLimitTemperature(): Double
}