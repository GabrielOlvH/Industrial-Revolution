package me.steven.indrev.blockentities.crafters

import me.steven.indrev.api.machines.Tier
import me.steven.indrev.api.machines.TransferMode
import me.steven.indrev.api.sideconfigs.ConfigurationType
import me.steven.indrev.components.TemperatureComponent
import me.steven.indrev.inventories.inventory
import me.steven.indrev.items.upgrade.Upgrade
import me.steven.indrev.recipes.machines.IRRecipeType
import me.steven.indrev.recipes.machines.InfuserRecipe
import me.steven.indrev.registry.MachineRegistry
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack
import net.minecraft.util.math.Direction

class SolidInfuserBlockEntity(tier: Tier) :
    CraftingMachineBlockEntity<InfuserRecipe>(tier, MachineRegistry.INFUSER_REGISTRY) {

    override val upgradeSlots: IntArray = intArrayOf(5, 6, 7, 8)
    override val availableUpgrades: Array<Upgrade> = Upgrade.DEFAULT

    init {
        this.temperatureComponent = TemperatureComponent({ this }, 0.06, 700..1100)
        this.inventoryComponent = inventory(this) {
            input { slots = intArrayOf(2, 3) }
            output { slot = 4 }
        }
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
                    return super.getFirstSlot(inventory, direction) { slot, stack -> slot == 2 && predicate(slot, stack) }
                mode == TransferMode.INPUT_SECOND ->
                    return super.getFirstSlot(inventory, direction) { slot, stack -> slot == 3 && predicate(slot, stack) }
            }

        }
        return super.getFirstSlot(inventory, direction, predicate)
    }

    override fun getValidConfigurations(type: ConfigurationType): Array<TransferMode> {
        return when (type) {
            ConfigurationType.ITEM -> TransferMode.values()
            else -> super.getValidConfigurations(type)
        }
    }

    override val type: IRRecipeType<InfuserRecipe> = InfuserRecipe.TYPE
}