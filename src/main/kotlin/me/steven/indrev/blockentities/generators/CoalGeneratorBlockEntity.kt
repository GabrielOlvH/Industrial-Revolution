package me.steven.indrev.blockentities.generators

import me.steven.indrev.blockentities.TemperatureController
import me.steven.indrev.inventories.DefaultSidedInventory
import me.steven.indrev.registry.MachineRegistry
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
    GeneratorBlockEntity(Tier.MK1, MachineRegistry.COAL_GENERATOR_REGISTRY) {
    private val inventory =
        DefaultSidedInventory(3, intArrayOf(2), intArrayOf()) { _, stack -> BURN_TIME_MAP.containsKey(stack?.item) }
    var burnTime: Int = 0
        set(value) {
            field = value.apply { propertyDelegate[3] = this }
        }
        get() = field.apply { propertyDelegate[3] = this }
    var maxBurnTime: Int = 0
        set(value) {
            field = value.apply { propertyDelegate[4] = this }
        }
        get() = field.apply { propertyDelegate[4] = this }

    override fun shouldGenerate(): Boolean {
        if (burnTime > 0) burnTime--
        else if (maxStoredPower > energy) {
            val invStack = inventory.getInvStack(2)
            if (!invStack.isEmpty && BURN_TIME_MAP.containsKey(invStack.item)) {
                burnTime = BURN_TIME_MAP[invStack.item] ?: return false
                maxBurnTime = burnTime
                invStack.count--
                if (invStack.isEmpty) inventory.setInvStack(2, ItemStack.EMPTY)
                else inventory.setInvStack(2, invStack)
            }
        }
        markDirty()
        return burnTime > 0 && energy < maxStoredPower
    }

    override fun getGenerationRatio(): Double = 0.15

    override fun getInventory(state: BlockState?, world: IWorld?, pos: BlockPos?): SidedInventory = inventory

    override fun createDelegate(): PropertyDelegate = ArrayPropertyDelegate(5)

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

    companion object {
        private val BURN_TIME_MAP = AbstractFurnaceBlockEntity.createFuelTimeMap()
    }
}