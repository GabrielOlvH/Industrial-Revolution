package me.steven.indrev.blockentities.crafters

import me.steven.indrev.inventories.DefaultSidedInventory
import me.steven.indrev.items.CoolerItem
import me.steven.indrev.items.rechargeable.RechargeableItem
import me.steven.indrev.items.upgrade.Upgrade
import me.steven.indrev.items.upgrade.UpgradeItem
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.Tier
import net.minecraft.inventory.BasicInventory
import net.minecraft.recipe.RecipeType
import net.minecraft.recipe.SmeltingRecipe

class ElectricFurnaceBlockEntity(tier: Tier) :
    CraftingMachineBlockEntity<SmeltingRecipe>(tier, MachineRegistry.ELECTRIC_FURNACE_REGISTRY) {
    private var currentRecipe: SmeltingRecipe? = null
    override fun tryStartRecipe(inventory: DefaultSidedInventory): SmeltingRecipe? {
        val inputStacks = BasicInventory(*(inventory.inputSlots).map { inventory.getInvStack(it) }.toTypedArray())
        val optional = world?.recipeManager?.getFirstMatch(RecipeType.SMELTING, inputStacks, world)
        val recipe = optional?.orElse(null) ?: return null
        val outputStack = inventory.getInvStack(3).copy()
        if (outputStack.isEmpty || (outputStack.count + recipe.output.count < outputStack.maxCount && outputStack.item == recipe.output.item)) {
            if (!isProcessing() && recipe.matches(inputStacks, this.world)) {
                processTime = recipe.cookTime
                totalProcessTime = recipe.cookTime
            }
            this.currentRecipe = recipe
        }
        return recipe
    }

    override fun createInventory(): DefaultSidedInventory =
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

    override fun getUpgradeSlots(): IntArray = intArrayOf(4, 5, 6, 7)

    override fun getAvailableUpgrades(): Array<Upgrade> = Upgrade.ALL

    override fun getCurrentRecipe(): SmeltingRecipe? = currentRecipe

    override fun getOptimalRange(): IntRange = 1600..2000

    override fun getBaseHeatingEfficiency(): Double = 0.06

    override fun getLimitTemperature(): Double = 2200.0
}