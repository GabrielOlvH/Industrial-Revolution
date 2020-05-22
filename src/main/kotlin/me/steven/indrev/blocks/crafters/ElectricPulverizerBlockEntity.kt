package me.steven.indrev.blocks.crafters

import me.steven.indrev.recipes.PulverizerRecipe
import me.steven.indrev.registry.MachineRegistry
import net.minecraft.inventory.BasicInventory
import net.minecraft.inventory.Inventory

class ElectricPulverizerBlockEntity : ElectricCraftingBlockEntity<PulverizerRecipe>(MachineRegistry.PULVERIZER_BLOCK_ENTITY) {
    override fun findRecipe(inventory: Inventory): PulverizerRecipe? {
        val inputStack = inventory.getInvStack(0)
        val optional = world?.recipeManager?.getFirstMatch(PulverizerRecipe.TYPE, BasicInventory(inputStack), world)?: return null
        return if (optional.isPresent) optional.get() else null
    }

    override fun startRecipe(recipe: PulverizerRecipe) {
        val inputStack = inventory.getInvStack(0)
        val outputStack = inventory.getInvStack(1).copy()
        if (outputStack.isEmpty || (outputStack.count + recipe.output.count < outputStack.maxCount && outputStack.item == recipe.output.item)) {
            processTime = recipe.processTime
            totalProcessTime = recipe.processTime
            processingItem = inputStack.item
            output = recipe.output
        }
    }
}