package me.steven.indrev.blockentities.crafters

import me.steven.indrev.components.CraftingComponent
import me.steven.indrev.components.TemperatureComponent
import me.steven.indrev.inventories.inventory
import me.steven.indrev.items.upgrade.Upgrade
import me.steven.indrev.recipes.machines.IRRecipeType
import me.steven.indrev.recipes.machines.InfuserRecipe
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.Tier
import net.minecraft.screen.ArrayPropertyDelegate

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
    }

    override fun splitStacks() {
        splitStacks(intArrayOf(6, 9, 12, 15, 18))
        splitStacks(intArrayOf(7, 10, 13, 16, 19))
    }

    override val type: IRRecipeType<InfuserRecipe> = InfuserRecipe.TYPE

    override fun getUpgradeSlots(): IntArray = intArrayOf(2, 3, 4, 5)

    override fun getAvailableUpgrades(): Array<Upgrade> = Upgrade.values()
}