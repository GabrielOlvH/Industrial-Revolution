package me.steven.indrev.blocks.generators

import me.steven.indrev.blocks.ElectricBlockEntity
import net.minecraft.block.InventoryProvider
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.container.ArrayPropertyDelegate
import net.minecraft.container.PropertyDelegate
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.util.Tickable
import team.reborn.energy.EnergySide
import team.reborn.energy.EnergyTier

abstract class GeneratorBlockEntity(type: BlockEntityType<*>, private val generationRatio: Double)
    : ElectricBlockEntity(type), InventoryProvider {

    override fun createDelegate(): PropertyDelegate = ArrayPropertyDelegate(2)

    override fun tick() {
        super.tick()
        if (world?.isClient == true) return
        if (shouldGenerate()) {
            val amountInserted = (maxStoredPower - energy).coerceAtMost(generationRatio)
            energy += amountInserted
            getOrCreateDelegate()[0] = energy.toInt()
            if (amountInserted > 0)
                markDirty()
        }
    }

    override fun fromTag(tag: CompoundTag?) {
        super.fromTag(tag)
        val tagList = tag?.get("Inventory") as ListTag? ?: ListTag()
        tagList.indices.forEach { i ->
            val stackTag = tagList.getCompound(i)
            val slot = stackTag.getInt("Slot")
            getInventory().setInvStack(slot, ItemStack.fromTag(stackTag))
        }
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
        super.fromTag(tag)
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

    override fun getMaxInput(): Double = 0.0

    abstract fun shouldGenerate(): Boolean

    abstract fun getInventory(): Inventory
}