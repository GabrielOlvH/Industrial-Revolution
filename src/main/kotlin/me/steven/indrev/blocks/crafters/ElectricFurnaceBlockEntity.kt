package me.steven.indrev.blocks.crafters

import me.steven.indrev.content.MachineRegistry
import me.steven.indrev.inventories.DefaultSidedInventory
import me.steven.indrev.items.Upgrade
import me.steven.indrev.items.UpgradeItem
import net.minecraft.inventory.BasicInventory
import net.minecraft.inventory.Inventory
import net.minecraft.inventory.SidedInventory
import net.minecraft.recipe.RecipeType
import net.minecraft.recipe.SmeltingRecipe

class ElectricFurnaceBlockEntity : CraftingMachineBlockEntity<SmeltingRecipe>(MachineRegistry.ELECTRIC_FURNACE_BLOCK_ENTITY, 250.0) {
    override fun findRecipe(inventory: Inventory): SmeltingRecipe? {
        val inputStack = inventory.getInvStack(0)
        val optional = world?.recipeManager?.getFirstMatch(RecipeType.SMELTING, BasicInventory(inputStack), world)
                ?: return null
        return if (optional.isPresent) optional.get() else null
    }

    override fun startRecipe(recipe: SmeltingRecipe) {
        val inputStack = inventory!!.getInvStack(0)
        val outputStack = inventory!!.getInvStack(1).copy()
        if (outputStack.isEmpty || (outputStack.count + recipe.output.count < outputStack.maxCount && outputStack.item == recipe.output.item)) {
            processTime = recipe.cookTime
            totalProcessTime = recipe.cookTime
            processingItem = inputStack.item
            output = recipe.output
        }
    }

    override fun createInventory(): SidedInventory = DefaultSidedInventory(6, intArrayOf(0), intArrayOf(1)) { slot, stack ->
        if (stack?.item is UpgradeItem) getUpgradeSlots().contains(slot) else true
    }.also { it.addListener(this) }

    override fun getUpgradeSlots(): IntArray = intArrayOf(2, 3, 4, 5)

    override fun getAvailableUpgrades(): Array<Upgrade> = Upgrade.ALL

    override fun getBaseValue(upgrade: Upgrade): Double = when (upgrade) {
        Upgrade.ENERGY -> 1.0
        Upgrade.SPEED -> 1.0
        Upgrade.BUFFER -> baseBuffer
    }
}