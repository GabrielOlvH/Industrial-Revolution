package me.steven.indrev.blocks

import net.minecraft.block.InventoryProvider
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.util.Tickable
import team.reborn.energy.Energy
import team.reborn.energy.EnergySide
import team.reborn.energy.EnergyStorage
import team.reborn.energy.EnergyTier

abstract class GeneratorBlockEntity(type: BlockEntityType<*>, private val generationRatio: Double, private val tier: EnergyTier) : BlockEntity(type), EnergyStorage, Tickable, InventoryProvider {
    var energy: Double = 0.0

    override fun tick() {
        if (shouldGenerate()) {
            val amountInserted = Energy.of(this).insert(generationRatio)
            if (amountInserted > 0) markDirty()
        }
    }

    override fun fromTag(tag: CompoundTag?) {
        super.fromTag(tag)
        energy = tag?.getDouble("Energy") ?: 0.0
        val tagList = tag?.get("Inventory") as ListTag? ?: ListTag()
        tagList.indices.forEach { i ->
            val stackTag = tagList.getCompound(i)
            val slot = stackTag.getInt("Slot")
            getInventory().setInvStack(slot, ItemStack.fromTag(stackTag))
        }
    }

    override fun toTag(tag: CompoundTag?): CompoundTag {
        tag?.putDouble("Energy", energy)
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

    override fun setStored(amount: Double) {
        this.energy = amount
    }

    override fun getMaxStoredPower(): Double {
        val block = this.cachedState.block
        if (block is GeneratorBlock) return block.maxBuffer
        return 0.0
    }

    override fun getTier(): EnergyTier = tier

    override fun getStored(side: EnergySide?): Double {
        val direction = EnergySide.fromMinecraft(this.cachedState[GeneratorBlock.FACING])
        if (direction == EnergySide.UNKNOWN || direction == side) return 0.0
        return energy
    }

    abstract fun shouldGenerate(): Boolean

    abstract fun getInventory(): Inventory
}