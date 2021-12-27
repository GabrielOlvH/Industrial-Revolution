package me.steven.indrev.inventories

import me.steven.indrev.IndustrialRevolution
import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.components.InventoryComponent
import me.steven.indrev.items.upgrade.IREnhancerItem
import me.steven.indrev.utils.EMPTY_INT_ARRAY
import me.steven.indrev.utils.component1
import me.steven.indrev.utils.component2
import net.minecraft.item.ItemStack
import net.minecraft.util.math.Direction

open class Filterable {
    var filters: MutableMap<Int, (ItemStack, Direction?) -> Boolean> = hashMapOf()

    infix fun Int.filter(filter: (ItemStack, Direction?) -> Boolean) {
        filters[this] = filter
    }

    infix fun IntRange.filter(filter: (ItemStack, Direction?, Int) -> Boolean) {
        forEach { slot -> filters[slot] = { stack, direction -> filter(stack, direction, slot) } }
    }

    infix fun Int.filter(filter: (ItemStack) -> Boolean) {
        filters[this] = { stack, _ -> filter(stack) }
    }

    infix fun IntRange.filter(filter: (ItemStack, Int) -> Boolean) {
        forEach { slot -> filters[slot] = { stack, _ -> filter(stack,  slot) } }
    }
}

open class IRInventoryDSL : Filterable() {
    var coolerSlot: Int? = null
    private var input: FilteredSlots = FilteredSlots.EMPTY_FILTER
    private var output: FilteredSlots = FilteredSlots.EMPTY_FILTER
    var enhancerSlots: IntRange? = null
    var maxStackCount = 64

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
        val enhancerComponent = blockEntity.enhancerComponent
        if (enhancerComponent != null) size += enhancerComponent.slots.size
        return IRInventory(this, size, input.slots, output.slots) { slot, stack, dir ->
            if (stack == null) false
            else filters.computeIfAbsent(slot) {
                { (stack, item), _ ->
                    when {
                        coolerSlot != null && slot == coolerSlot -> stack.isIn(IndustrialRevolution.COOLERS_TAG)
                        input.slots.contains(slot) -> true
                        enhancerComponent != null -> item is IREnhancerItem && slot in enhancerComponent.slots && !enhancerComponent.isLocked(slot, blockEntity.tier) && enhancerComponent.compatible.contains(item.enhancer)
                        else -> false
                    }
                }
            }(stack, dir)
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

        fun filter(filter: (ItemStack, Direction?, Int) -> Boolean) {
            slots.forEach { slot -> filters[slot] = { stack, dir -> filter(stack, dir, slot) } }
        }

        fun filter(filter: (ItemStack, Int) -> Boolean) {
            slots.forEach { slot -> filters[slot] = { stack, _ -> filter(stack, slot) } }
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