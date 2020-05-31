package me.steven.indrev.blockentities.crafters

import me.steven.indrev.inventories.DefaultSidedInventory
import me.steven.indrev.items.CoolerItem
import me.steven.indrev.items.Upgrade
import me.steven.indrev.items.UpgradeItem
import me.steven.indrev.items.rechargeable.RechargeableItem
import me.steven.indrev.recipes.PulverizerRecipe
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.Tier
import net.minecraft.inventory.BasicInventory

class PulverizerBlockEntity(tier: Tier) :
    CraftingMachineBlockEntity<PulverizerRecipe>(MachineRegistry.PULVERIZER_BLOCK_ENTITY, tier, 250.0) {
    private var currentRecipe: PulverizerRecipe? = null
    override fun tryStartRecipe(inventory: DefaultSidedInventory): PulverizerRecipe? {
        val inputStacks = BasicInventory(*(inventory.inputSlots).map { inventory.getInvStack(it) }.toTypedArray())
        val optional =
                world?.recipeManager?.getFirstMatch(PulverizerRecipe.TYPE, inputStacks, world)
        val recipe = optional?.orElse(null) ?: return null
        val outputStack = inventory.getInvStack(1).copy()
        if (outputStack.isEmpty || (outputStack.count + recipe.output.count < outputStack.maxCount && outputStack.item == recipe.output.item)) {
            if (!isProcessing() && recipe.matches(inputStacks, this.world)) {
                processTime = recipe.processTime
                totalProcessTime = recipe.processTime
            }
            this.currentRecipe = recipe
        }
        return recipe
    }

    override fun createInventory(): DefaultSidedInventory = DefaultSidedInventory(9, intArrayOf(2), intArrayOf(3, 4)) { slot, stack ->
        val item = stack?.item
        when {
            item is UpgradeItem -> getUpgradeSlots().contains(slot)
            item is RechargeableItem && item.canOutput -> slot == 0
            item is CoolerItem -> slot == 1
            slot == 2 -> true
            else -> false
        }
    }

    override fun onCraft() {
        if (this.inventory!!.invSize < 3) return
        val chance = this.currentRecipe?.extraOutput?.right ?: return
        if (chance < this.world?.random?.nextDouble() ?: 0.0) {
            val extra = this.currentRecipe?.extraOutput?.left ?: return
            val invStack = this.inventory!!.getInvStack(2).copy()
            if (invStack.item == extra.item && invStack.count < invStack.maxCount + extra.count) {
                invStack.count += extra.count
                this.inventory!!.setInvStack(4, invStack)
            } else if (invStack.isEmpty) {
                this.inventory!!.setInvStack(4, extra.copy())
            }
        }
    }

    override fun getUpgradeSlots(): IntArray = intArrayOf(5, 6, 7, 8)

    override fun getAvailableUpgrades(): Array<Upgrade> = Upgrade.ALL

    override fun getCurrentRecipe(): PulverizerRecipe? = currentRecipe
}