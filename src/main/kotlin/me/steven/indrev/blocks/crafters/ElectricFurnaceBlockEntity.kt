package me.steven.indrev.blocks.crafters

import me.steven.indrev.content.MachineRegistry
import me.steven.indrev.inventories.DefaultSidedInventory
import me.steven.indrev.items.Upgrade
import net.minecraft.inventory.BasicInventory
import net.minecraft.inventory.Inventory
import net.minecraft.inventory.SidedInventory
import net.minecraft.recipe.RecipeType
import net.minecraft.recipe.SmeltingRecipe

class ElectricFurnaceBlockEntity : CraftingMachineBlockEntity<SmeltingRecipe>(MachineRegistry.ELECTRIC_FURNACE_BLOCK_ENTITY, 250.0) {
    override fun findRecipe(inventory: Inventory): SmeltingRecipe? {
        val inputStack = inventory.getInvStack(0)
        val optional = world?.recipeManager?.getFirstMatch(RecipeType.SMELTING, BasicInventory(inputStack), world) ?: return null
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

    override fun createInventory(): SidedInventory = DefaultSidedInventory(6)

    override fun getUpgradeSlots(): IntArray = intArrayOf(2, 3, 4, 5)

    override fun getAvailableUpgrades(): Array<Upgrade> = Upgrade.ALL
}