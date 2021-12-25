package me.steven.indrev.components

import me.steven.indrev.api.sideconfigs.ConfigurationType
import me.steven.indrev.api.sideconfigs.SideConfiguration
import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.inventories.IRInventory
import net.minecraft.inventory.Inventory
import net.minecraft.inventory.InventoryChangedListener
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtList

class InventoryComponent(val syncable: MachineBlockEntity<*>, supplier: InventoryComponent.() -> IRInventory) : InventoryChangedListener {
    val inventory: IRInventory = supplier()

    init {
        inventory.addListener(this)
        inventory.component = this
    }

    val itemConfig: SideConfiguration = SideConfiguration(ConfigurationType.ITEM)

    override fun onInventoryChanged(sender: Inventory?) {
        syncable.markDirty()
    }

    fun readNbt(tag: NbtCompound?) {
        val tagList = tag?.get("Inventory") as NbtList? ?: NbtList()
        tagList.indices.forEach { i ->
            val stackTag = tagList.getCompound(i)
            val slot = stackTag.getInt("Slot")
            inventory.setStack(slot, ItemStack.fromNbt(stackTag))
        }
        itemConfig.readNbt(tag)
    }

    fun writeNbt(tag: NbtCompound): NbtCompound {
        val tagList = NbtList()
        for (i in 0 until inventory.size()) {
            val stackTag = NbtCompound()
            stackTag.putInt("Slot", i)
            tagList.add(inventory.getStack(i).writeNbt(stackTag))
        }
        tag.put("Inventory", tagList)
        itemConfig.writeNbt(tag)
        return tag
    }

}