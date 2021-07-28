package me.steven.indrev.blockentities.laser

import me.steven.indrev.blocks.machine.LaserBlock
import me.steven.indrev.registry.IRBlockRegistry
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.inventory.Inventories
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.collection.DefaultedList
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.World

class CapsuleBlockEntity(pos: BlockPos, state: BlockState) : BlockEntity(IRBlockRegistry.CAPSULE_BLOCK_ENTITY, pos, state), BlockEntityClientSerializable {

    var inventory: DefaultedList<ItemStack> = DefaultedList.ofSize(1, ItemStack.EMPTY)

    var lastProgress = 0

    fun getActiveLasersCount(): Int {
        return Direction.values().count {
            val laser = world?.getBlockEntity(pos.offset(it, 4)) as? LaserBlockEntity ?: return@count false
            laser.cachedState[LaserBlock.POWERED]
        }
    }

    override fun readNbt(tag: NbtCompound?) {
        super.readNbt(tag)
        inventory = DefaultedList.ofSize(1, ItemStack.EMPTY)
        Inventories.readNbt(tag, inventory)
    }

    override fun writeNbt(tag: NbtCompound?): NbtCompound? {
        super.writeNbt(tag)
        Inventories.writeNbt(tag, inventory)
        return tag
    }

    override fun fromClientTag(tag: NbtCompound) {
        inventory[0] = ItemStack.fromNbt(tag.getCompound("Item"))
    }

    override fun toClientTag(tag: NbtCompound): NbtCompound {
        tag.put("Item", inventory[0].writeNbt(NbtCompound()))
        return tag
    }

    companion object {
        fun tick(world: World, pos: BlockPos, state: BlockState, blockEntity: CapsuleBlockEntity) {
            if (world.isClient) return
            val tag = blockEntity.inventory[0].orCreateNbt
            val progress = tag.getInt("Progress")
            if (tag.contains("Progress") && progress == blockEntity.lastProgress) {
                tag.remove("Progress")
            }
            blockEntity.lastProgress = progress
        }
    }
}