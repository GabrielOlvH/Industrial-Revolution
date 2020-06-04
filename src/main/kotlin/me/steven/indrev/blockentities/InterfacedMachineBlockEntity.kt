package me.steven.indrev.blockentities

import me.steven.indrev.blocks.MultiBlockPart
import me.steven.indrev.inventories.DefaultSidedInventory
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.Tier
import net.minecraft.block.BlockState
import net.minecraft.block.InventoryProvider
import net.minecraft.inventory.SidedInventory
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.util.DefaultedList
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IWorld

abstract class InterfacedMachineBlockEntity(tier: Tier, registry: MachineRegistry) :
    MachineBlockEntity(tier, registry), InventoryProvider {

    private var machineInventory: DefaultSidedInventory? = null
        get() = field ?: createInventory().apply { field = this }

    fun getInventory(): DefaultSidedInventory {
        if (this.world != null) {
            val block = this.cachedState.block
            if (block is MultiBlockPart) {
                val center = block.getBlockEntityPos(this.cachedState, this.pos)
                val blockEntity = world?.getBlockEntity(center)
                if (blockEntity is InventoryProvider) return blockEntity.getInventory(cachedState, world, pos) as DefaultSidedInventory
            }
        }
        return machineInventory!!
    }

    override fun getInventory(state: BlockState?, world: IWorld?, pos: BlockPos?): SidedInventory = getInventory()

    override fun getInvStackList(): DefaultedList<ItemStack> = getInventory().stackList

    override fun setInvStackList(list: DefaultedList<ItemStack>?) {
        if (list != null) getInventory().stackList = list
    }

    override fun getInvSize(): Int = invStackList.size

    abstract fun createInventory(): DefaultSidedInventory

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