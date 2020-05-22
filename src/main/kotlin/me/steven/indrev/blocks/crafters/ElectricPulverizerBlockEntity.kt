package me.steven.indrev.blocks.crafters

import me.steven.indrev.inventories.DefaultSidedInventory
import me.steven.indrev.recipes.PulverizerRecipe
import me.steven.indrev.registry.MachineRegistry
import net.minecraft.inventory.BasicInventory
import net.minecraft.inventory.Inventory
import net.minecraft.inventory.SidedInventory

class ElectricPulverizerBlockEntity : CraftingMachineBlockEntity<PulverizerRecipe>(MachineRegistry.PULVERIZER_BLOCK_ENTITY) {
    var recipe: PulverizerRecipe? = null
    override fun findRecipe(inventory: Inventory): PulverizerRecipe? {
        val inputStack = inventory.getInvStack(0)
        val optional = world?.recipeManager?.getFirstMatch(PulverizerRecipe.TYPE, BasicInventory(inputStack), world)?: return null
        return if (optional.isPresent) optional.get().apply { recipe = this } else null
    }

    override fun startRecipe(recipe: PulverizerRecipe) {
        val inputStack = inventory!!.getInvStack(0)
        val outputStack = inventory!!.getInvStack(1).copy()
        if (outputStack.isEmpty || (outputStack.count + recipe.output.count < outputStack.maxCount && outputStack.item == recipe.output.item)) {
            processTime = recipe.processTime
            totalProcessTime = recipe.processTime
            processingItem = inputStack.item
            output = recipe.output
            this.recipe = recipe
        }
    }

    override fun createInventory(): SidedInventory = DefaultSidedInventory(3)

    override fun onCraft() {
        if (this.inventory!!.invSize < 3) return
        val chance = this.recipe?.extraOutput?.right ?: return
        if (chance < this.world?.random?.nextDouble() ?: 0.0) {
            val extra = this.recipe?.extraOutput?.left ?: return
            val invStack = this.inventory!!.getInvStack(2).copy()
            if (invStack.item == extra.item && invStack.count < invStack.maxCount + extra.count) {
                invStack.count += extra.count
                this.inventory!!.setInvStack(2, invStack)
            } else if (invStack.isEmpty) {
                this.inventory!!.setInvStack(2, extra.copy())
            }
        }
    }
}