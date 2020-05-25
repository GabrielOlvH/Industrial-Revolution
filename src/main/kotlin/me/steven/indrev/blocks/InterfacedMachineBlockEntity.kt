package me.steven.indrev.blocks

import net.minecraft.block.InventoryProvider
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag

abstract class InterfacedMachineBlockEntity(type: BlockEntityType<*>, baseBuffer: Double) :
    BasicMachineBlockEntity(type, baseBuffer), InventoryProvider {
    private fun getInventory() = getInventory(null, null, null)

    override fun fromTag(tag: CompoundTag?) {
        val tagList = tag?.get("Inventory") as ListTag? ?: ListTag()
        tagList.indices.forEach { i ->
            val stackTag = tagList.getCompound(i)
            val slot = stackTag.getInt("Slot")
            getInventory().setInvStack(slot, ItemStack.fromTag(stackTag))
        }
        super.fromTag(tag)
    }

    override fun toTag(tag: CompoundTag?): CompoundTag {
        val tagList = ListTag()
        val inventory = getInventory()
        for (i in 0 until inventory.invSize) {
            val stackTag = CompoundTag()
            stackTag.putInt("Slot", i)
            tagList.add(inventory.getInvStack(i).toTag(stackTag))
        }
        tag?.put("Inventory", tagList)
        return super.toTag(tag)
    }

    override fun fromClientTag(tag: CompoundTag?) {
        val tagList = tag?.get("Inventory") as ListTag? ?: ListTag()
        tagList.indices.forEach { i ->
            val stackTag = tagList.getCompound(i)
            val slot = stackTag.getInt("Slot")
            getInventory().setInvStack(slot, ItemStack.fromTag(stackTag))
        }
        super.fromClientTag(tag)
    }

    override fun toClientTag(tag: CompoundTag?): CompoundTag {
        if (tag == null) return CompoundTag()
        val tagList = ListTag()
        val inventory = getInventory()
        for (i in 0 until inventory.invSize) {
            val stackTag = CompoundTag()
            stackTag.putInt("Slot", i)
            tagList.add(inventory.getInvStack(i).toTag(stackTag))
        }
        tag.put("Inventory", tagList)
        return super.toClientTag(tag)
    }
}