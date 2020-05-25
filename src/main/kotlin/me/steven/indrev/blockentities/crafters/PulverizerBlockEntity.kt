package me.steven.indrev.blockentities.crafters

import me.steven.indrev.content.MachineRegistry
import me.steven.indrev.inventories.DefaultSidedInventory
import me.steven.indrev.items.Upgrade
import me.steven.indrev.items.UpgradeItem
import me.steven.indrev.recipes.PulverizerRecipe
import me.steven.indrev.utils.Tier
import net.minecraft.inventory.BasicInventory
import net.minecraft.inventory.Inventory
import net.minecraft.inventory.SidedInventory

class PulverizerBlockEntity(tier: Tier) :
    CraftingMachineBlockEntity<PulverizerRecipe>(MachineRegistry.PULVERIZER_BLOCK_ENTITY, tier, 250.0) {
    var recipe: PulverizerRecipe? = null
    override fun findRecipe(inventory: Inventory): PulverizerRecipe? {
        val inputStack = inventory.getInvStack(0)
        val optional =
            world?.recipeManager?.getFirstMatch(PulverizerRecipe.TYPE, BasicInventory(inputStack), world) ?: return null
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

    override fun createInventory(): SidedInventory = DefaultSidedInventory(7, intArrayOf(0), intArrayOf(1, 2)) { slot, stack ->
        if (stack?.item is UpgradeItem) getUpgradeSlots().contains(slot) else true
    }

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

    override fun getUpgradeSlots(): IntArray = intArrayOf(3, 4, 5, 6)

    override fun getAvailableUpgrades(): Array<Upgrade> = Upgrade.ALL

    override fun getBaseValue(upgrade: Upgrade): Double = when (upgrade) {
        Upgrade.ENERGY -> 1.0
        Upgrade.SPEED -> 1.0
        Upgrade.BUFFER -> baseBuffer
    }
}