package me.steven.indrev.blockentities.generators

import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.Property
import me.steven.indrev.utils.Tier
import net.minecraft.block.BlockState
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundTag

abstract class SolidFuelGeneratorBlockEntity(tier: Tier, registry: MachineRegistry) : GeneratorBlockEntity(tier, registry) {
    private var burnTime: Int by Property(4, 0)
    private var maxBurnTime: Int by Property(5, 0)

    override fun shouldGenerate(): Boolean {
        if (burnTime > 0) burnTime--
        else if (maxStoredPower > energy) {
            val inventory = inventoryComponent?.inventory ?: return false
            val invStack = inventory.getStack(2)
            val item = invStack.item
            if (!invStack.isEmpty && getFuelMap().containsKey(invStack.item)) {
                burnTime = getFuelMap()[invStack.item] ?: return false
                maxBurnTime = burnTime
                invStack.decrement(1)
                if (item.hasRecipeRemainder())
                    inventory.setStack(2, ItemStack(item.recipeRemainder))
            }
            markDirty()
        }
        return burnTime > 0 && energy < maxStoredPower
    }

    override fun fromTag(state: BlockState?, tag: CompoundTag?) {
        super.fromTag(state, tag)
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

    abstract fun getFuelMap(): Map<Item, Int>
}