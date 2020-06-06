package me.steven.indrev.components

import io.github.cottonmc.cotton.gui.PropertyDelegateHolder
import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.items.CoolerItem
import net.minecraft.container.PropertyDelegate
import net.minecraft.nbt.CompoundTag

class TemperatureController(
    private val machineProvider: () -> MachineBlockEntity,
    private val heatingSpeed: Double,
    val optimalRange: IntRange,
    val limit: Double
) : PropertyDelegateHolder {

    var temperature: Double by Property(2, 300.0)
    var cooling = 0
    var explosionPower = 1f

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
        val explosionTemperature = limit - 500
        machine.explode = temperature > explosionTemperature + 10 && machine.world!!.random.nextInt((temperature - explosionTemperature).toInt()) > 100
    }


    fun getCurrentTemperature(): Double = temperature

    fun setCurrentTemperature(temperature: Double) {
        this.temperature = temperature
    }

    override fun getPropertyDelegate(): PropertyDelegate = machineProvider().propertyDelegate
}