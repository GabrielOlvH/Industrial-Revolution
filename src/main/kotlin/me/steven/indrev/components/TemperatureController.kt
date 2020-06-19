package me.steven.indrev.components

import io.github.cottonmc.cotton.gui.PropertyDelegateHolder
import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.items.IRCoolerItem
import net.minecraft.nbt.CompoundTag
import net.minecraft.screen.PropertyDelegate

class TemperatureController(
    private val machineProvider: () -> MachineBlockEntity,
    private val heatingSpeed: Double,
    val optimalRange: IntRange,
    val limit: Double
) : PropertyDelegateHolder {

    var temperature: Double by Property(2, 25.0)
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

    fun isFullEfficiency() = cooling <= 0 && temperature.toInt() in optimalRange

    fun tick(isHeatingUp: Boolean) {
        val machine = machineProvider()
        val coolerStack = machine.inventoryController?.getInventory()?.getStack(1)
        val coolerItem = coolerStack?.item

        if (!isHeatingUp)
            temperature -= 0.01
        else if (temperature > optimalRange.last && coolerStack != null && coolerItem is IRCoolerItem) {
            if (cooling < 0) {
                coolerStack.damage++
                cooling = 30
            }
            temperature -= coolerItem.coolingModifier
        } else if (temperature < optimalRange.last - 10)
            temperature += heatingSpeed
        else if (cooling < 0)
            cooling = 100 * machine.world!!.random.nextInt(2)
        else if (cooling > 0 && temperature < 25) {
            cooling--
            temperature -= 0.01
        } else
            temperature += heatingSpeed
    }

    override fun getPropertyDelegate(): PropertyDelegate = machineProvider().propertyDelegate
}