package me.steven.indrev.components

import io.github.cottonmc.cotton.gui.PropertyDelegateHolder
import me.steven.indrev.api.machines.properties.Property
import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.blockentities.crafters.CraftingMachineBlockEntity
import me.steven.indrev.registry.IRItemRegistry
import me.steven.indrev.utils.component1
import me.steven.indrev.utils.component2
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.screen.PropertyDelegate

class TemperatureComponent(
    private val machine: MachineBlockEntity<*>,
    private val heatingSpeed: Double,
    val optimalRange: IntRange,
    limit: Int
) : PropertyDelegateHolder {

    var temperature: Double by Property(2, 25.0)
    var cooling = true

    init {
        propertyDelegate[3] = limit
    }

    fun readNbt(tag: NbtCompound?) {
        temperature = tag?.getDouble("Temperature") ?: 0.0
        cooling = tag?.getBoolean("Cooling") ?: false
    }

    fun writeNbt(tag: NbtCompound): NbtCompound {
        tag.putDouble("Temperature", temperature)
        tag.putBoolean("Cooling", cooling)
        return tag
    }

    fun isFullEfficiency(): Boolean {
        return (!cooling || machine.inventoryComponent?.inventory?.coolerStack?.isEmpty != true)
                && temperature.toInt() in optimalRange
    }

    fun tick(shouldHeatUp: Boolean) {
        val random = machine.world!!.random
        val inv = machine.inventoryComponent?.inventory
        val (coolerStack, coolerItem) = inv?.coolerStack ?: ItemStack.EMPTY
        val isHeatingUp = shouldHeatUp || (coolerItem == IRItemRegistry.HEAT_COIL && machine.use(16.0))

        if (cooling) {
            val modifier = (machine as? CraftingMachineBlockEntity<*>)?.craftingComponents?.size ?: 0
            temperature -= heatingSpeed / if (isHeatingUp) 3 + modifier else 1

            if (coolerStack.isDamageable && machine.ticks % 120 == 0)
                coolerStack.damage(1, random, null)

            if (temperature <= optimalRange.first + (2 * random.nextFloat() - 1) * 10) {
                cooling = false
            }
        } else if (isHeatingUp) {
            temperature += heatingSpeed
            val n = (optimalRange.last + optimalRange.first) / 2.0
            if (temperature >= n + (2 * random.nextFloat() - 1) * 15) {
                cooling = true
            }
        } else if (temperature > 35.0) {
            temperature -= heatingSpeed / 1.5
        } else if (machine.ticks % 15 == 0) {
            temperature = (temperature + (2 * random.nextFloat() - 1) / 2).coerceIn(20.0, 35.0)
        }
    }

    override fun getPropertyDelegate(): PropertyDelegate = machine.propertyDelegate
}