package me.steven.indrev.networks.item

import me.steven.indrev.networks.EndpointData
import net.minecraft.inventory.Inventories
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundTag
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

    override fun toTag(tag: CompoundTag): CompoundTag {
        tag.put("filter", Inventories.toTag(CompoundTag(), filter))
        tag.putBoolean("w", whitelist)
        tag.putBoolean("d", matchDurability)
        tag.putBoolean("t", matchTag)
        return super.toTag(tag)
    }

    override fun fromTag(tag: CompoundTag): EndpointData {
        Inventories.fromTag(tag.getCompound("filter"), filter)
        whitelist = tag.getBoolean("w")
        matchDurability = tag.getBoolean("d")
        matchTag = tag.getBoolean("t")
        val data = super.fromTag(tag)
        type = data.type
        mode = data.mode
        return this
    }
}