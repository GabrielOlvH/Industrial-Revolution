package me.steven.indrev.components

import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.items.CoolerItem
import net.minecraft.nbt.CompoundTag

class TemperatureController(private val machineProvider: () -> MachineBlockEntity, val heatingSpeed: Double, val optimalRange: IntRange, val limit: Double) {
    var temperature = 300.0
        set(value) {
            field = value.coerceAtLeast(0.0).apply { machineProvider().propertyDelegate[2] = this.toInt() }
        }
    var cooling = 0

    fun fromTag(tag: CompoundTag?) {
        temperature = tag?.getDouble("Temperature") ?: 0.0
        cooling = tag?.getInt("Cooling") ?: 0
    }

    fun toTag(tag: CompoundTag): CompoundTag {
        tag.putDouble("Temperature", temperature)
        tag.putInt("Cooling", cooling)
        return tag
    }

    fun tick(isHeatingUp: Boolean) {
        val machine = machineProvider()
        val coolerStack = machine.inventoryController?.getInventory()?.getInvStack(1)
        val coolerItem = coolerStack?.item

        if (isHeatingUp && coolerItem is CoolerItem
            && coolerStack.damage < coolerStack.maxDamage
            && (cooling > 0
                || temperature + 250 >= optimalRange.last)
        ) {
            if (cooling > 0) cooling--
            else if (temperature + 75 > optimalRange.last) {
                cooling = 200
                coolerStack.damage++
            }
            val modifier =
                if (cooling > 0) coolerItem.activeCoolingModifier else coolerItem.passiveCoolingModifier
            temperature += heatingSpeed + modifier
        } else if (isHeatingUp) temperature += heatingSpeed
        else if (temperature > 310) temperature -= 0.1
    }


    fun getCurrentTemperature(): Double = temperature

    fun setCurrentTemperature(temperature: Double) {
        this.temperature = temperature
    }
}