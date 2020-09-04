package me.steven.indrev.blockentities.crafters

import me.steven.indrev.IndustrialRevolution
import me.steven.indrev.components.InventoryComponent
import me.steven.indrev.components.TemperatureComponent
import me.steven.indrev.config.IConfig
import me.steven.indrev.inventories.IRInventory
import me.steven.indrev.items.misc.IRCoolerItem
import me.steven.indrev.items.upgrade.IRUpgradeItem
import me.steven.indrev.items.upgrade.Upgrade
import me.steven.indrev.recipes.machines.PulverizerRecipe
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.Tier
import team.reborn.energy.Energy

class PulverizerBlockEntity(tier: Tier) :
    CraftingMachineBlockEntity<PulverizerRecipe>(tier, MachineRegistry.PULVERIZER_REGISTRY) {

    init {
        this.inventoryComponent = InventoryComponent {
            IRInventory(9, intArrayOf(2), intArrayOf(3, 4)) { slot, stack ->
                val item = stack?.item
                when {
                    item is IRUpgradeItem -> getUpgradeSlots().contains(slot)
                    Energy.valid(stack) && Energy.of(stack).maxOutput > 0 -> slot == 0
                    item is IRCoolerItem -> slot == 1
                    slot == 2 -> true
                    else -> false
                }
            }
        }
        this.temperatureComponent = TemperatureComponent({ this }, 0.06, 700..1100, 1400.0)
    }

    private var currentRecipe: PulverizerRecipe? = null

    override fun tryStartRecipe(inventory: IRInventory): PulverizerRecipe? {
        val inputStacks = inventory.getInputInventory()
        val optional =
            world?.recipeManager?.getFirstMatch(PulverizerRecipe.TYPE, inputStacks, world)
        val recipe = optional?.orElse(null) ?: return null
        val outputStack = inventory.getStack(3).copy()
        if (outputStack.isEmpty || (outputStack.count + recipe.output.count <= outputStack.maxCount && outputStack.item == recipe.output.item)) {
            if (!isProcessing() && recipe.matches(inputStacks, this.world)) {
                processTime = recipe.processTime
                totalProcessTime = recipe.processTime
            }
            this.currentRecipe = recipe
        }
        return recipe
    }

    override fun onCraft() {
        val inventory = inventoryComponent!!.inventory
        if (inventory.size() < 3) return
        val chance = this.currentRecipe?.extraOutput?.right ?: return
        if (chance < this.world?.random?.nextDouble() ?: 0.0) {
            val extra = this.currentRecipe?.extraOutput?.left ?: return
            val invStack = inventory.getStack(2).copy()
            if (invStack.item == extra.item && invStack.count < invStack.maxCount + extra.count) {
                invStack.count += extra.count
                inventory.setStack(4, invStack)
            } else if (invStack.isEmpty) {
                inventory.setStack(4, extra.copy())
            }
        }
    }

    override fun getUpgradeSlots(): IntArray = intArrayOf(5, 6, 7, 8)

    override fun getAvailableUpgrades(): Array<Upgrade> = Upgrade.DEFAULT

    override fun getCurrentRecipe(): PulverizerRecipe? = currentRecipe

    override fun getConfig(): IConfig =
        when (tier) {
            Tier.MK1 -> IndustrialRevolution.CONFIG.machines.pulverizerMk1
            Tier.MK2 -> IndustrialRevolution.CONFIG.machines.pulverizerMk2
            Tier.MK3 -> IndustrialRevolution.CONFIG.machines.pulverizerMk3
            else -> IndustrialRevolution.CONFIG.machines.pulverizerMk4
        }
}