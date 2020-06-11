package me.steven.indrev.components

import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.blocks.ProxyBlock
import me.steven.indrev.inventories.DefaultSidedInventory
import net.minecraft.block.InventoryProvider
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag

class InventoryController(val machineProvider: () -> MachineBlockEntity, val supplier: () -> DefaultSidedInventory) {
    private var inv: DefaultSidedInventory? = null
        get() = field ?: supplier().apply { field = this }

    fun getInventory(): DefaultSidedInventory {
        val machine = machineProvider()
        if (machine.world != null) {
            val block = machine.cachedState.block
            if (block is ProxyBlock) {
                val center = block.getBlockEntityPos(machine.cachedState, machine.pos)
                val blockEntity = machine.world?.getBlockEntity(center)
                if (blockEntity is InventoryProvider) return blockEntity.getInventory(machine.cachedState, machine.world, machine.pos) as DefaultSidedInventory
            }
        }
        return inv!!
    }

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