package me.steven.indrev.blocks.generators

import me.steven.indrev.blocks.BasicMachineBlockEntity
import net.minecraft.block.InventoryProvider
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.container.ArrayPropertyDelegate
import net.minecraft.container.PropertyDelegate
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import team.reborn.energy.EnergySide

abstract class GeneratorBlockEntity(type: BlockEntityType<*>, private val generationRatio: Double, maxBuffer: Double)
    : BasicMachineBlockEntity(type, maxBuffer), InventoryProvider {

    override fun tick() {
        super.tick()
        if (world?.isClient == false && shouldGenerate() && addEnergy(generationRatio) > 0) markDirty()
    }

    override fun createDelegate(): PropertyDelegate = ArrayPropertyDelegate(2)

    override fun getMaxInput(side: EnergySide?): Double = 0.0

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
        val inventory = getInventory(this.cachedState, this.world, this.pos)
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
        val inventory = getInventory(this.cachedState, this.world, this.pos)
        for (i in 0 until inventory.invSize) {
            val stackTag = CompoundTag()
            stackTag.putInt("Slot", i)
            tagList.add(inventory.getInvStack(i).toTag(stackTag))
        }
        tag.put("Inventory", tagList)
        return super.toClientTag(tag)
    }

    abstract fun shouldGenerate(): Boolean

    abstract fun getInventory(): Inventory
}