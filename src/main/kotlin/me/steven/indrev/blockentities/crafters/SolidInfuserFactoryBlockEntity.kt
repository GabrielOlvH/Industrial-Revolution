package me.steven.indrev.blockentities.crafters

import me.steven.indrev.api.machines.Tier
import me.steven.indrev.api.machines.TransferMode
import me.steven.indrev.components.CraftingComponent
import me.steven.indrev.components.TemperatureComponent
import me.steven.indrev.components.multiblock.FactoryStructureDefinition
import me.steven.indrev.components.multiblock.MultiBlockComponent
import me.steven.indrev.inventories.inventory
import me.steven.indrev.items.upgrade.Upgrade
import me.steven.indrev.recipes.machines.IRRecipeType
import me.steven.indrev.recipes.machines.InfuserRecipe
import me.steven.indrev.registry.MachineRegistry
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack
import net.minecraft.screen.ArrayPropertyDelegate
import net.minecraft.util.math.Direction

class SolidInfuserFactoryBlockEntity(tier: Tier) :
    CraftingMachineBlockEntity<InfuserRecipe>(tier, MachineRegistry.SOLID_INFUSER_FACTORY_REGISTRY) {

    override val upgradeSlots: IntArray = intArrayOf(2, 3, 4, 5)
    override val availableUpgrades: Array<Upgrade> = Upgrade.DEFAULT

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
        this.multiblockComponent = MultiBlockComponent({ id -> id.variant == "factory" },FactoryStructureDefinition.SELECTOR)
    }

    override fun splitStacks() {
        splitStacks(TOP_SLOTS)
        splitStacks(BOTTOM_SLOTS)
    }

    override fun getFirstSlot(
        inventory: Inventory,
        direction: Direction,
        predicate: (Int, ItemStack) -> Boolean
    ): Int? {
        inventoryComponent?.let { component ->
            val mode = component.itemConfig[direction.opposite]
            when {
                inventory != component.inventory -> return@let
                mode == TransferMode.INPUT_FIRST ->
                    return super.getFirstSlot(inventory, direction) { slot, stack -> TOP_SLOTS.contains(slot) && predicate(slot, stack) }
                mode == TransferMode.INPUT_SECOND ->
                    return super.getFirstSlot(inventory, direction) { slot, stack -> BOTTOM_SLOTS.contains(slot) && predicate(slot, stack) }
            }

        }
        return super.getFirstSlot(inventory, direction, predicate)
    }

    override val type: IRRecipeType<InfuserRecipe> = InfuserRecipe.TYPE

    companion object {
        val TOP_SLOTS = intArrayOf(6, 9, 12, 15, 18)
        val BOTTOM_SLOTS = intArrayOf(7, 10, 13, 16, 19)
    }
}