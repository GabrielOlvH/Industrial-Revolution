package me.steven.indrev.components

import me.steven.indrev.api.sideconfigs.ConfigurationType
import me.steven.indrev.api.sideconfigs.SideConfiguration
import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.inventories.IRInventory
import net.minecraft.inventory.Inventory
import net.minecraft.inventory.InventoryChangedListener
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag

class InventoryComponent(val syncable: MachineBlockEntity<*>, supplier: InventoryComponent.() -> IRInventory) : InventoryChangedListener {
    val inventory: IRInventory = supplier()

    init {
        inventory.addListener(this)
        inventory.component = this
    }

    val itemConfig: SideConfiguration = SideConfiguration(ConfigurationType.ITEM)

    override fun onInventoryChanged(sender: Inventory?) {
        syncable.markForUpdate()
    }

    fun fromTag(tag: CompoundTag?) {
        val tagList = tag?.get("Inventory") as ListTag? ?: ListTag()
        tagList.indices.forEach { i ->
            val stackTag = tagList.getCompound(i)
            val slot = stackTag.getInt("Slot")
            inventory.setStack(slot, ItemStack.fromTag(stackTag))
        }
        itemConfig.fromTag(tag)
    }

    fun toTag(tag: CompoundTag): CompoundTag {
        val tagList = ListTag()
        for (i in 0 until inventory.size()) {
            val stackTag = CompoundTag()
            stackTag.putInt("Slot", i)
            tagList.add(inventory.getStack(i).toTag(stackTag))
        }
        tag.put("Inventory", tagList)
        itemConfig.toTag(tag)
        return tag
    }

}