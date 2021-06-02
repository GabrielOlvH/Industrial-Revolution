package me.steven.indrev.networks.item

import me.steven.indrev.networks.EndpointData
import net.minecraft.inventory.Inventories
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.collection.DefaultedList

class ItemEndpointData(
    type: Type,
    mode: Mode?,
    var whitelist: Boolean,
    var matchDurability: Boolean,
    var matchTag: Boolean,
    val filter: DefaultedList<ItemStack> = DefaultedList.ofSize(9, ItemStack.EMPTY)
    ) : EndpointData(type, mode) {

    fun matches(itemStack: ItemStack): Boolean {
        if (filter.isEmpty()) return !whitelist
        val findMatches = filter.filter { it.item == itemStack.item }
        if (findMatches.isEmpty()) return !whitelist
        var valid = true
        if (valid && matchDurability) valid = findMatches.any { it.damage == itemStack.damage }
        if (valid && matchTag) valid = findMatches.any { it.tag == itemStack.tag }
        if (valid) return whitelist
        return !whitelist
    }

    override fun writeNbt(tag: NbtCompound): NbtCompound {
        tag.put("filter", Inventories.writeNbt(NbtCompound(), filter))
        tag.putBoolean("w", whitelist)
        tag.putBoolean("d", matchDurability)
        tag.putBoolean("mt", matchTag)
        return super.writeNbt(tag)
    }

    override fun readNbt(tag: NbtCompound): EndpointData {
        Inventories.readNbt(tag.getCompound("filter"), filter)
        whitelist = tag.getBoolean("w")
        matchDurability = tag.getBoolean("d")
        matchTag = tag.getBoolean("mt")
        val data = super.readNbt(tag)
        type = data.type
        mode = data.mode
        return this
    }
}