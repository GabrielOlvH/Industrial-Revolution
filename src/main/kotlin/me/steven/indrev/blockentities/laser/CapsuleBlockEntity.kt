package me.steven.indrev.blockentities.laser

import me.steven.indrev.blocks.machine.LaserBlock
import me.steven.indrev.registry.IRBlockRegistry
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.inventory.Inventories
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.util.Tickable
import net.minecraft.util.collection.DefaultedList
import net.minecraft.util.math.Direction

class CapsuleBlockEntity : BlockEntity(IRBlockRegistry.CAPSULE_BLOCK_ENTITY), BlockEntityClientSerializable, Tickable {

    var inventory: DefaultedList<ItemStack> = DefaultedList.ofSize(1, ItemStack.EMPTY)

    var lastProgress = 0

    override fun tick() {
        if (world?.isClient == true) return
        val tag = inventory[0].orCreateTag
        val progress = tag.getInt("Progress")
        if (tag.contains("Progress") && progress == lastProgress) {
            tag.remove("Progress")
        }
        this.lastProgress = progress
    }

    fun getActiveLasersCount(): Int {
        return Direction.values().count {
            val laser = world?.getBlockEntity(pos.offset(it, 4)) as? LaserBlockEntity ?: return@count false
            laser.cachedState[LaserBlock.POWERED]
        }
    }

    override fun fromTag(state: BlockState?, tag: CompoundTag?) {
        super.fromTag(state, tag)
        inventory = DefaultedList.ofSize(1, ItemStack.EMPTY)
        Inventories.fromTag(tag, inventory)
    }

    override fun toTag(tag: CompoundTag?): CompoundTag? {
        super.toTag(tag)
        Inventories.toTag(tag, inventory)
        return tag
    }

    override fun fromClientTag(tag: CompoundTag) {
        inventory[0] = ItemStack.fromTag(tag.getCompound("Item"))
    }

    override fun toClientTag(tag: CompoundTag): CompoundTag {
        tag.put("Item", inventory[0].toTag(CompoundTag()))
        return tag
    }
}