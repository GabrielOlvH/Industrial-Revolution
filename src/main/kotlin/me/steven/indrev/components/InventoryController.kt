package me.steven.indrev.components

import me.steven.indrev.inventories.DefaultSidedInventory
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag

class InventoryController(val supplier: () -> DefaultSidedInventory) {
    private var inv: DefaultSidedInventory? = null
        get() = field ?: supplier().apply { field = this }

    fun getInventory(): DefaultSidedInventory = inv!!

    fun fromTag(tag: CompoundTag?) {
        val tagList = tag?.get("Inventory") as ListTag? ?: ListTag()
        tagList.indices.forEach { i ->
            val stackTag = tagList.getCompound(i)
            val slot = stackTag.getInt("Slot")
            getInventory().setStack(slot, ItemStack.fromTag(stackTag))
        }
    }

    fun toTag(tag: CompoundTag): CompoundTag {
        val tagList = ListTag()
        val inventory = getInventory()
        for (i in 0 until inventory.size()) {
            val stackTag = CompoundTag()
            stackTag.putInt("Slot", i)
            tagList.add(inventory.getStack(i).toTag(stackTag))
        }
        tag.put("Inventory", tagList)
        return tag
    }
}