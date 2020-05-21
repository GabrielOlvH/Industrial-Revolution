package me.steven.indrev.blocks.furnace

import me.steven.indrev.registry.MachineRegistry
import net.minecraft.inventory.BasicInventory
import net.minecraft.inventory.Inventory
import net.minecraft.recipe.RecipeType

class ElectricFurnaceBlockEntity : ElectricCraftingBlockEntity(MachineRegistry.ELECTRIC_FURNACE_BLOCK_ENTITY) {
    override fun findRecipe(inventory: Inventory) {
        val inputStack = inventory.getInvStack(0)
        val outputStack = inventory.getInvStack(1).copy()
        world?.recipeManager?.getFirstMatch(RecipeType.SMELTING, BasicInventory(inputStack), world)?.ifPresent { recipe ->
            if (outputStack.isEmpty || (outputStack.count + recipe.output.count < outputStack.maxCount && outputStack.item == recipe.output.item)) {
                processTime = recipe.cookTime
                processingItem = inputStack.item
                output = recipe.output
                totalProcessTime = recipe.cookTime
            }
        }
    }
}