package me.steven.indrev.inventories

import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.blockentities.crafters.UpgradeProvider
import me.steven.indrev.components.InventoryComponent
import me.steven.indrev.items.misc.IRCoolerItem
import me.steven.indrev.items.upgrade.IRUpgradeItem
import me.steven.indrev.registry.IRRegistry
import me.steven.indrev.utils.EMPTY_INT_ARRAY
import me.steven.indrev.utils.component1
import me.steven.indrev.utils.component2
import net.minecraft.item.ItemStack

open class Filterable {
    var filters: MutableMap<Int, (ItemStack) -> Boolean> = hashMapOf()

    infix fun Int.filter(filter: (ItemStack) -> Boolean) {
        filters[this] = filter
    }

    infix fun IntRange.filter(filter: (ItemStack, Int) -> Boolean) {
        forEach { slot -> filters[slot] = { stack -> filter(stack, slot) } }
    }
}

open class IRInventoryDSL : Filterable() {
    var batterySlot: Int = 0
    var coolerSlot: Int? = null
    private var input: FilteredSlots = FilteredSlots.EMPTY_FILTER
    private var output: FilteredSlots = FilteredSlots.EMPTY_FILTER
    var upgradeSlots: IntRange? = null

    fun input(block: FilteredSlots.() -> Unit) {
        input = FilteredSlots()
        block(input)
        input.filters.forEach { (key, value) -> filters[key] = value }
    }

    fun output(block: FilteredSlots.() -> Unit) {
        output = FilteredSlots()
        block(output)
        output.filters.forEach { (key, value) -> filters[key] = value }
    }

    fun build(blockEntity: MachineBlockEntity<*>): IRInventory {
        var size = input.slots.plus(output.slots).plus(filters.keys).distinct().size + 1
        if (coolerSlot == null && blockEntity.temperatureComponent != null) coolerSlot = 1
        if (coolerSlot != null) size++
        if (blockEntity is UpgradeProvider) size += blockEntity.getUpgradeSlots().size
        return IRInventory(this, size, input.slots, output.slots) { slot, stack ->
            if (stack == null) false
            else filters.computeIfAbsent(slot) { slot ->
                { (stack, item) ->
                    when {
                        slot == batterySlot -> true
                        coolerSlot != null && slot == coolerSlot -> item is IRCoolerItem || item == IRRegistry.HEAT_COIL
                        input.slots.contains(slot) -> true
                        blockEntity is UpgradeProvider -> item is IRUpgradeItem && slot in blockEntity.getUpgradeSlots() && !blockEntity.isLocked(slot, blockEntity.tier) && blockEntity.getAvailableUpgrades().contains(item.upgrade)
                        else -> false
                    }
                }
            }(stack)
        }
    }

    class FilteredSlots : Filterable() {
        var slots: IntArray = EMPTY_INT_ARRAY
        var slot: Int? = null
            set(value) {
                if (value != null)
                    slots = intArrayOf(value)
                field = value
            }

        fun filter(filter: (ItemStack, Int) -> Boolean) {
            slots.forEach { slot -> filters[slot] = { stack -> filter(stack, slot) } }
        }

        companion object {
            val EMPTY_FILTER = FilteredSlots()
        }
    }
}

fun inventory(blockEntity: MachineBlockEntity<*>, block: IRInventoryDSL.() -> Unit): InventoryComponent {
    val dsl = IRInventoryDSL()
    block(dsl)
    val inv = dsl.build(blockEntity)
    return InventoryComponent(blockEntity) { inv }
}