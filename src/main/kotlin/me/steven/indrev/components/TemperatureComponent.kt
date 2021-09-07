package me.steven.indrev.components

import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.blockentities.crafters.CraftingMachineBlockEntity
import me.steven.indrev.registry.IRItemRegistry
import me.steven.indrev.utils.component1
import me.steven.indrev.utils.component2
import net.minecraft.block.entity.BlockEntity
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound

class TemperatureComponent(
    private val blockEntity: BlockEntity,
    private val heatingSpeed: Double,
    val optimalRange: IntRange,
    val limit: Int
) : ComponentProvider by ensureIsProvider(blockEntity) {

    var cooling = true

    var temperature: Double by autosync(MachineBlockEntity.TEMPERATURE_ID, 25.0)

    init {
        trackInt(MachineBlockEntity.MAX_TEMPERATURE_ID) { limit }
    }

    private var ticks = 0

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
        val inventoryComponent = ComponentKey.ITEM.get(blockEntity)
        return (!cooling || inventoryComponent?.inventory?.coolerStack?.isEmpty != true)
                && temperature.toInt() in optimalRange
    }

    fun tick(shouldHeatUp: Boolean) {
        ticks++
        val inventoryComponent = ComponentKey.ITEM.get(blockEntity)
        val machine = blockEntity as? MachineBlockEntity<*>
        val random = blockEntity.world!!.random
        val inv = inventoryComponent?.inventory
        val (coolerStack, coolerItem) = inv?.coolerStack ?: ItemStack.EMPTY
        val isHeatingUp = shouldHeatUp || (machine != null && coolerItem == IRItemRegistry.HEAT_COIL && machine.use(16.0))

        if (cooling) {
            val modifier = (blockEntity as? CraftingMachineBlockEntity<*>)?.craftingComponents?.size ?: 0
            temperature -= heatingSpeed / if (isHeatingUp) 3 + modifier else 1

            if (coolerStack.isDamageable && ticks % 120 == 0)
                coolerStack.damage(1, random, null)
            if (coolerStack.damage >= coolerStack.maxDamage) {
                coolerStack.decrement(1)
            }

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
        } else if (ticks % 15 == 0) {
            temperature = (temperature + (2 * random.nextFloat() - 1) / 2).coerceIn(20.0, 35.0)
        }
    }
}