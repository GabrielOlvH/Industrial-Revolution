package me.steven.indrev.blockentities.generators

import me.steven.indrev.content.MachineRegistry
import me.steven.indrev.inventories.DefaultSidedInventory
import me.steven.indrev.utils.Tier
import net.minecraft.block.BlockState
import net.minecraft.block.entity.AbstractFurnaceBlockEntity
import net.minecraft.container.ArrayPropertyDelegate
import net.minecraft.container.PropertyDelegate
import net.minecraft.inventory.SidedInventory
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IWorld

class CoalGeneratorBlockEntity :
    GeneratorBlockEntity(MachineRegistry.COAL_GENERATOR_BLOCK_ENTITY, Tier.LOW, 0.5, 1000.0) {
    private val inventory =
        DefaultSidedInventory(1, intArrayOf(0), intArrayOf()) { _, stack -> BURN_TIME_MAP.containsKey(stack?.item) }
    var burnTime: Int = 0
        set(value) {
            propertyDelegate[2] = value
            field = value
        }
        get() {
            propertyDelegate[2] = field
            return field
        }
    var maxBurnTime: Int = 0
        set(value) {
            propertyDelegate[3] = value
            field = value
        }
        get() {
            propertyDelegate[3] = field
            return field
        }

    override fun tick() {
        if (world?.isClient == true) return
        super.tick()
            if (shouldGenerate()) {
                burnTime--
                propertyDelegate[2] = burnTime
            } else if (maxStoredPower > energy) {
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

    override fun fromTag(tag: CompoundTag?) {
        super.fromTag(tag)
        burnTime = tag?.getInt("BurnTime") ?: 0
        maxBurnTime = tag?.getInt("MaxBurnTime") ?: 0
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
    }

    override fun toClientTag(tag: CompoundTag?): CompoundTag {
        tag?.putInt("BurnTime", burnTime)
        tag?.putInt("MaxBurnTime", maxBurnTime)
        return super.toClientTag(tag)
    }

    override fun getInventory(state: BlockState?, world: IWorld?, pos: BlockPos?): SidedInventory = inventory

    override fun shouldGenerate(): Boolean = burnTime > 0 && energy < maxStoredPower

    companion object {
        private val BURN_TIME_MAP = AbstractFurnaceBlockEntity.createFuelTimeMap()
    }
}