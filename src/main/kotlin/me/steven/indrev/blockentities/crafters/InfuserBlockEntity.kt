package me.steven.indrev.blockentities.crafters

import me.steven.indrev.IndustrialRevolution
import me.steven.indrev.components.InventoryComponent
import me.steven.indrev.config.MachineConfig
import me.steven.indrev.inventories.IRInventory
import me.steven.indrev.items.IRCoolerItem
import me.steven.indrev.items.rechargeable.IRRechargeableItem
import me.steven.indrev.items.upgrade.IRUpgradeItem
import me.steven.indrev.items.upgrade.Upgrade
import me.steven.indrev.recipes.InfuserRecipe
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.Tier

class InfuserBlockEntity(tier: Tier) :
    CraftingMachineBlockEntity<InfuserRecipe>(tier, MachineRegistry.INFUSER_REGISTRY) {

    init {
        this.inventoryComponent = InventoryComponent {
            IRInventory(9, intArrayOf(2, 3), intArrayOf(4)) { slot, stack ->
                val item = stack?.item
                when {
                    item is IRUpgradeItem -> getUpgradeSlots().contains(slot)
                    item is IRRechargeableItem && item.canOutput -> slot == 0
                    item is IRCoolerItem -> slot == 1
                    slot == 2 || slot == 3 -> true
                    else -> false
                }
            }
        }
    }

    private var currentRecipe: InfuserRecipe? = null

    override fun tryStartRecipe(inventory: IRInventory): InfuserRecipe? {
        val inputStacks = inventory.getInputInventory()
        val optional = world?.recipeManager?.getFirstMatch(InfuserRecipe.TYPE, inputStacks, world)
        val recipe = optional?.orElse(null) ?: return null
        val outputStack = inventory.getStack(4).copy()
        if (outputStack.isEmpty || (outputStack.count + recipe.output.count <= outputStack.maxCount && outputStack.item == recipe.output.item)) {
            if (!isProcessing() && recipe.matches(inputStacks, this.world)) {
                processTime = recipe.processTime
                totalProcessTime = recipe.processTime
            }
            this.currentRecipe = recipe
        }
        return recipe
    }

    override fun getUpgradeSlots(): IntArray = intArrayOf(5, 6, 7, 8)

    override fun getAvailableUpgrades(): Array<Upgrade> = Upgrade.ALL

    override fun getCurrentRecipe(): InfuserRecipe? = currentRecipe

    override fun getConfig(): MachineConfig =
        when (tier) {
            Tier.MK1 -> IndustrialRevolution.CONFIG.machines.infuserMk1
            Tier.MK2 -> IndustrialRevolution.CONFIG.machines.infuserMk2
            Tier.MK3 -> IndustrialRevolution.CONFIG.machines.infuserMk3
            else -> IndustrialRevolution.CONFIG.machines.infuserMk4
        }
}