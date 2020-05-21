package me.steven.indrev.blocks.generators

import me.steven.indrev.inventories.DefaultSidedInventory
import me.steven.indrev.registry.MachineRegistry
import net.minecraft.block.BlockState
import net.minecraft.block.entity.AbstractFurnaceBlockEntity
import net.minecraft.container.ArrayPropertyDelegate
import net.minecraft.container.PropertyDelegate
import net.minecraft.inventory.Inventory
import net.minecraft.inventory.SidedInventory
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IWorld

class CoalGeneratorBlockEntity : GeneratorBlockEntity(MachineRegistry.COAL_GENERATOR_BLOCK_ENTITY, 0.5) {
    private val inventory = DefaultSidedInventory(1)
    var burnTime: Int = 0
    var maxBurnTime: Int = 0

    override fun tick() {
        if (world?.isClient == true) return
        super.tick()
            if (shouldGenerate()) {
                burnTime--
                propertyDelegate[2] = burnTime
            }
            else if (maxStoredPower > getEnergy()) {
                val invStack = inventory.getInvStack(0)
                if (!invStack.isEmpty && BURN_TIME_MAP.containsKey(invStack.item)) {
                    burnTime = BURN_TIME_MAP[invStack.item] ?: return
                    maxBurnTime = burnTime
                    propertyDelegate[3] = maxBurnTime
                    invStack.count--
                    if (invStack.isEmpty) inventory.setInvStack(0, ItemStack.EMPTY)
                    else inventory.setInvStack(0, invStack)
                }
            }
        markDirty()
    }

    override fun createDelegate(): PropertyDelegate = ArrayPropertyDelegate(4)

    override fun getMaxOutput(): Double = 8.0

    override fun fromTag(tag: CompoundTag?) {
        super.fromTag(tag)
        burnTime = tag?.getInt("BurnTime") ?: 0
        maxBurnTime = tag?.getInt("MaxBurnTime") ?: 0
        propertyDelegate[2] = burnTime
        propertyDelegate[3] = maxBurnTime
    }

    override fun toTag(tag: CompoundTag?): CompoundTag {
        tag?.putInt("BurnTime", burnTime)
        tag?.putInt("MaxBurnTime", maxBurnTime)
        return super.toTag(tag)
    }

    override fun fromClientTag(tag: CompoundTag?) {
        super.fromClientTag(tag)
        burnTime = tag?.getInt("BurnTime") ?: 0
        maxBurnTime = tag?.getInt("MaxBurnTime") ?: 0
        propertyDelegate[2] = burnTime
        propertyDelegate[3] = maxBurnTime
    }

    override fun toClientTag(tag: CompoundTag?): CompoundTag {
        tag?.putInt("BurnTime", burnTime)
        tag?.putInt("MaxBurnTime", maxBurnTime)
        return super.toClientTag(tag)
    }

    override fun getInventory(): Inventory = inventory

    override fun getInventory(state: BlockState?, world: IWorld?, pos: BlockPos?): SidedInventory = inventory

    override fun shouldGenerate(): Boolean = burnTime > 0 && getEnergy() < maxStoredPower

    override fun getPropertyDelegate(): PropertyDelegate {
        val delegate = super.getPropertyDelegate()
        delegate[2] = burnTime
        delegate[3] = maxBurnTime
        return delegate
    }

    companion object {
        private val BURN_TIME_MAP = AbstractFurnaceBlockEntity.createFuelTimeMap()
    }
}