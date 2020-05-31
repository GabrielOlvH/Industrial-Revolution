package me.steven.indrev.blockentities.crafters

import me.steven.indrev.inventories.DefaultSidedInventory
import me.steven.indrev.items.CoolerItem
import me.steven.indrev.items.rechargeable.RechargeableItem
import me.steven.indrev.items.upgrade.Upgrade
import me.steven.indrev.items.upgrade.UpgradeItem
import me.steven.indrev.recipes.InfuserRecipe
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.Tier
import net.minecraft.inventory.BasicInventory

class InfuserBlockEntity(tier: Tier) : CraftingMachineBlockEntity<InfuserRecipe>(MachineRegistry.INFUSER_BLOCK_ENTITY, tier, 1000.0) {
    private var currentRecipe: InfuserRecipe? = null

    override fun tryStartRecipe(inventory: DefaultSidedInventory): InfuserRecipe? {
        val inputStacks = BasicInventory(*(inventory.inputSlots).map { inventory.getInvStack(it) }.toTypedArray())
        val optional = world?.recipeManager?.getFirstMatch(InfuserRecipe.TYPE, inputStacks, world)
        val recipe = optional?.orElse(null) ?: return null
        val outputStack = inventory.getInvStack(2).copy()
        if (outputStack.isEmpty || (outputStack.count + recipe.output.count < outputStack.maxCount && outputStack.item == recipe.output.item)) {
            if (!isProcessing() && recipe.matches(inputStacks, this.world)) {
                processTime = recipe.processTime
                totalProcessTime = recipe.processTime
            }
            this.currentRecipe = recipe
        }
        return recipe
    }

    override fun createInventory(): DefaultSidedInventory = DefaultSidedInventory(9, intArrayOf(2, 3), intArrayOf(4)) { slot, stack ->
        val item = stack?.item
        when {
            item is UpgradeItem -> getUpgradeSlots().contains(slot)
            item is RechargeableItem && item.canOutput -> slot == 0
            item is CoolerItem -> slot == 1
            slot == 2 || slot == 3 -> true
            else -> false
        }
    }

    override fun getUpgradeSlots(): IntArray = intArrayOf(5, 6, 7, 8)

    override fun getAvailableUpgrades(): Array<Upgrade> = Upgrade.ALL

    override fun getCurrentRecipe(): InfuserRecipe? = currentRecipe
}