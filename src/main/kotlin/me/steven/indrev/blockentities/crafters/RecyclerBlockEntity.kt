package me.steven.indrev.blockentities.crafters

import me.steven.indrev.components.InventoryController
import me.steven.indrev.inventories.IRInventory
import me.steven.indrev.items.IRCoolerItem
import me.steven.indrev.items.rechargeable.IRRechargeableItem
import me.steven.indrev.items.upgrade.IRUpgradeItem
import me.steven.indrev.items.upgrade.Upgrade
import me.steven.indrev.recipes.RecyclerRecipe
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.Tier

class RecyclerBlockEntity(tier: Tier) : CraftingMachineBlockEntity<RecyclerRecipe>(tier, MachineRegistry.RECYCLER_REGISTRY), UpgradeProvider {

    private var currentRecipe: RecyclerRecipe? = null

    init {
        this.inventoryController = InventoryController {
            IRInventory(8, intArrayOf(2), intArrayOf(3)) { slot, stack ->
                val item = stack?.item
                when {
                    item is IRUpgradeItem -> getUpgradeSlots().contains(slot)
                    item is IRRechargeableItem && item.canOutput -> slot == 0
                    item is IRCoolerItem -> slot == 1
                    slot == 2 -> true
                    else -> false
                }
            }
        }
    }

    override fun tryStartRecipe(inventory: IRInventory): RecyclerRecipe? {
        val inputStacks = inventory.getInputInventory()
        val optional = world?.recipeManager?.getFirstMatch(RecyclerRecipe.TYPE, inputStacks, world)
        val recipe = optional?.orElse(null) ?: return null
        val outputStack = inventory.getStack(3).copy()
        if (outputStack.isEmpty || (outputStack.count + recipe.output.count <= outputStack.maxCount && outputStack.item == recipe.output.item)) {
            if (!isProcessing() && recipe.matches(inputStacks, this.world)) {
                processTime = recipe.processTime
                totalProcessTime = recipe.processTime
            }
            this.currentRecipe = recipe
        }
        return recipe
    }

    override fun getCurrentRecipe(): RecyclerRecipe? = currentRecipe

    override fun getUpgradeSlots(): IntArray = intArrayOf(4, 5, 6, 7)

    override fun getAvailableUpgrades(): Array<Upgrade> = Upgrade.ALL
}