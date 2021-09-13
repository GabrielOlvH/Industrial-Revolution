package me.steven.indrev.networks.item

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant
import net.minecraft.inventory.Inventories
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.collection.DefaultedList

class ItemFilterData(
    var whitelist: Boolean,
    var matchDurability: Boolean,
    var matchTag: Boolean,
    val filter: DefaultedList<ItemStack> = DefaultedList.ofSize(9, ItemStack.EMPTY)
) {

    constructor() : this(false, false, false)

    fun matches(itemStack: ItemVariant): Boolean {
        if (filter.isEmpty()) return !whitelist
        val findMatches = filter.filter { it.item == itemStack.item }
        if (findMatches.isEmpty()) return !whitelist
        var valid = true
        if (valid && matchDurability) valid = findMatches.any { it.damage == itemStack.toStack().damage }
        if (valid && matchTag) valid = findMatches.any { it.nbt == itemStack.nbt }
        if (valid) return whitelist
        return !whitelist
    }

    fun writeNbt(tag: NbtCompound): NbtCompound {
        tag.put("filter", Inventories.writeNbt(NbtCompound(), filter))
        tag.putBoolean("w", whitelist)
        tag.putBoolean("d", matchDurability)
        tag.putBoolean("mt", matchTag)
        return tag
    }

    fun readNbt(tag: NbtCompound): ItemFilterData {
        Inventories.readNbt(tag.getCompound("filter"), filter)
        whitelist = tag.getBoolean("w")
        matchDurability = tag.getBoolean("d")
        matchTag = tag.getBoolean("mt")
        return this
    }

    companion object {
        val ACCEPTING_FILTER_DATA = ItemFilterData(false, false, false)
    }
}