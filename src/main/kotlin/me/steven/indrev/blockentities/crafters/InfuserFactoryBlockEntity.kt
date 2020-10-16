package me.steven.indrev.blockentities.crafters

import me.steven.indrev.blocks.FactoryPartBlock
import me.steven.indrev.components.CraftingComponent
import me.steven.indrev.components.MultiblockComponent
import me.steven.indrev.components.TemperatureComponent
import me.steven.indrev.inventories.inventory
import me.steven.indrev.items.upgrade.Upgrade
import me.steven.indrev.recipes.machines.IRRecipeType
import me.steven.indrev.recipes.machines.InfuserRecipe
import me.steven.indrev.registry.IRRegistry
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.Tier
import me.steven.indrev.utils.identifier
import net.minecraft.screen.ArrayPropertyDelegate
import net.minecraft.util.BlockRotation
import net.minecraft.util.math.BlockPos
import net.minecraft.util.registry.Registry

class InfuserFactoryBlockEntity(tier: Tier) :
    CraftingMachineBlockEntity<InfuserRecipe>(tier, MachineRegistry.INFUSER_FACTORY_REGISTRY) {

    init {
        this.propertyDelegate = ArrayPropertyDelegate(15)
        this.temperatureComponent = TemperatureComponent({ this }, 0.06, 700..1100, 1400.0)
        this.inventoryComponent = inventory(this) {
            input { slots = intArrayOf(6, 7, 9, 10, 12, 13, 15, 16, 18, 19) }
            output { slots = intArrayOf(8, 11, 14, 17, 20) }
        }
        this.craftingComponents = Array(5) { index ->
            CraftingComponent(index, this).apply {
                inputSlots = intArrayOf(6 + (index * 3), 6 + (index * 3) + 1)
                outputSlots = intArrayOf(6 + (index * 3) + 2)
            }
        }
        this.multiblockComponent = MultiblockComponent.Builder()
            .corners(BlockPos(0, 0, 1), 1, IRRegistry.FACTORY_PART.defaultState)
            .corners(BlockPos(0, 0, 3), 1, IRRegistry.FACTORY_PART.defaultState)
            .add(BlockPos(0, 0, 2), IRRegistry.MACHINE_BLOCK.defaultState)
            .diamond(BlockPos(0, 0, 2), 1, STEEL_BLOCK)
            .add(BlockPos(0, 0, 3), STEEL_BLOCK)
            .add(BlockPos(0, 0, 1), STEEL_BLOCK)
            .corners(BlockPos(-2, 0, 0), 1, FACTORY_STATE_Z, BlockRotation.COUNTERCLOCKWISE_90)
            .corners(BlockPos(0, 0, 2), 1, FACTORY_STATE_X)
            .build(this)
    }

    override fun splitStacks() {
        splitStacks(intArrayOf(6, 9, 12, 15, 18))
        splitStacks(intArrayOf(7, 10, 13, 16, 19))
    }

    override val type: IRRecipeType<InfuserRecipe> = InfuserRecipe.TYPE

    override fun getUpgradeSlots(): IntArray = intArrayOf(2, 3, 4, 5)

    override fun getAvailableUpgrades(): Array<Upgrade> = Upgrade.DEFAULT

    companion object {
        val FACTORY_STATE_X = IRRegistry.FACTORY_PART.defaultState.with(FactoryPartBlock.CONNECTED_X, true)
        val FACTORY_STATE_Y = IRRegistry.FACTORY_PART.defaultState.with(FactoryPartBlock.CONNECTED_Y, true)
        val FACTORY_STATE_Z = IRRegistry.FACTORY_PART.defaultState.with(FactoryPartBlock.CONNECTED_Z, true)
        val STEEL_BLOCK = Registry.BLOCK.get(identifier("steel_block")).defaultState
    }
}