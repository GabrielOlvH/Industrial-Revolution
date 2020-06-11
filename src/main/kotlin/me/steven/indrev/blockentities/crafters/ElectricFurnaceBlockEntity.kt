package me.steven.indrev.blockentities.crafters

import me.steven.indrev.components.InventoryController
import me.steven.indrev.components.TemperatureController
import me.steven.indrev.inventories.DefaultSidedInventory
import me.steven.indrev.items.CoolerItem
import me.steven.indrev.items.rechargeable.RechargeableItem
import me.steven.indrev.items.upgrade.Upgrade
import me.steven.indrev.items.upgrade.UpgradeItem
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.Tier
import net.minecraft.recipe.RecipeType
import net.minecraft.recipe.SmeltingRecipe

class ElectricFurnaceBlockEntity(tier: Tier) :
    CraftingMachineBlockEntity<SmeltingRecipe>(tier, MachineRegistry.ELECTRIC_FURNACE_REGISTRY) {

    init {
        this.inventoryController = InventoryController({ this }) {
            DefaultSidedInventory(8, intArrayOf(2), intArrayOf(3)) { slot, stack ->
                val item = stack?.item
                when {
                    item is UpgradeItem -> getUpgradeSlots().contains(slot)
                    item is RechargeableItem && item.canOutput -> slot == 0
                    item is CoolerItem -> slot == 1
                    slot == 2 -> true
                    else -> false
                }
            }
        }
        this.temperatureController = TemperatureController({ this }, 0.06, 1600..2000, 2200.0)
    }

    private var currentRecipe: SmeltingRecipe? = null

    override fun tryStartRecipe(inventory: DefaultSidedInventory): SmeltingRecipe? {
        val inputStacks = inventory.getInputInventory()
        val optional = world?.recipeManager?.getFirstMatch(RecipeType.SMELTING, inputStacks, world)
        val recipe = optional?.orElse(null) ?: return null
        val outputStack = inventory.getStack(3).copy()
        if (outputStack.isEmpty || (outputStack.count + recipe.output.count < outputStack.maxCount && outputStack.item == recipe.output.item)) {
            if (!isProcessing() && recipe.matches(inputStacks, this.world)) {
                processTime = recipe.cookTime
                totalProcessTime = recipe.cookTime
            }
            this.currentRecipe = recipe
        }
        return recipe
    }

    override fun getUpgradeSlots(): IntArray = intArrayOf(4, 5, 6, 7)

    override fun getAvailableUpgrades(): Array<Upgrade> = Upgrade.ALL

    override fun getCurrentRecipe(): SmeltingRecipe? = currentRecipe
}