package me.steven.indrev.blockentities.generators

import me.steven.indrev.api.machines.Tier
import me.steven.indrev.api.machines.TransferMode
import me.steven.indrev.api.machines.properties.Property
import me.steven.indrev.api.sideconfigs.ConfigurationType
import me.steven.indrev.blocks.machine.MachineBlock
import me.steven.indrev.registry.MachineRegistry
import net.minecraft.block.BlockState
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.util.math.Direction

abstract class SolidFuelGeneratorBlockEntity(tier: Tier, registry: MachineRegistry) : GeneratorBlockEntity(tier, registry) {
    private var burnTime: Int by Property(4, 0)
    private var maxBurnTime: Int by Property(5, 0)

    override fun shouldGenerate(): Boolean {
        if (burnTime > 0) burnTime--
        else if (energyCapacity > energy) {
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
        return burnTime > 0 && energy < energyCapacity
    }

    override fun applyDefault(
        state: BlockState,
        type: ConfigurationType,
        configuration: MutableMap<Direction, TransferMode>
    ) {
        val direction = (state.block as MachineBlock).getFacing(state)
        when (type) {
            ConfigurationType.ITEM -> {
                configuration[direction.rotateYClockwise()] = TransferMode.INPUT
            }
            else -> super.applyDefault(state, type, configuration)
        }
    }

    override fun getValidConfigurations(type: ConfigurationType): Array<TransferMode> {
        return when (type) {
            ConfigurationType.ITEM -> arrayOf(TransferMode.INPUT, TransferMode.NONE)
            else -> return super.getValidConfigurations(type)
        }
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