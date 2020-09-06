package me.steven.indrev.blockentities.crafters

import me.steven.indrev.IndustrialRevolution
import me.steven.indrev.components.InventoryComponent
import me.steven.indrev.components.TemperatureComponent
import me.steven.indrev.config.IConfig
import me.steven.indrev.inventories.IRInventory
import me.steven.indrev.items.misc.IRCoolerItem
import me.steven.indrev.items.upgrade.IRUpgradeItem
import me.steven.indrev.items.upgrade.Upgrade
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.Tier
import net.minecraft.recipe.AbstractCookingRecipe
import net.minecraft.recipe.RecipeType
import team.reborn.energy.Energy

class ElectricFurnaceBlockEntity(tier: Tier) :
    CraftingMachineBlockEntity<AbstractCookingRecipe>(tier, MachineRegistry.ELECTRIC_FURNACE_REGISTRY) {

    init {
        this.inventoryComponent = InventoryComponent {
            IRInventory(8, intArrayOf(2), intArrayOf(3)) { slot, stack ->
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
        this.temperatureComponent = TemperatureComponent({ this }, 0.1, 1300..1700, 2000.0)
    }

    private var currentRecipe: AbstractCookingRecipe? = null

    override fun tryStartRecipe(inventory: IRInventory): AbstractCookingRecipe? {
        val upgrades = getUpgrades(inventory)
        val inputStacks = inventory.getInputInventory()
        val recipeType = when (upgrades.keys.firstOrNull { it == Upgrade.BLAST_FURNACE || it == Upgrade.SMOKER }) {
            Upgrade.BLAST_FURNACE -> RecipeType.BLASTING
            Upgrade.SMOKER -> RecipeType.SMOKING
            else -> RecipeType.SMELTING
         } as RecipeType<AbstractCookingRecipe>
        val recipe = world?.recipeManager?.getFirstMatch(recipeType, inputStacks, world)?.orElse(null) ?: return null
        val outputStack = inventory.getStack(3).copy()
        if (outputStack.isEmpty || (outputStack.count + recipe.output.count <= outputStack.maxCount && outputStack.item == recipe.output.item)) {
            if (!isProcessing() && recipe.matches(inputStacks, this.world)) {
                processTime = recipe.cookTime
                totalProcessTime = recipe.cookTime
            }
            this.currentRecipe = recipe
        }
        return recipe
    }

    override fun getUpgradeSlots(): IntArray = intArrayOf(4, 5, 6, 7)

    override fun getAvailableUpgrades(): Array<Upgrade> = Upgrade.values()

    override fun getCurrentRecipe(): AbstractCookingRecipe? = currentRecipe

    override fun getConfig(): IConfig =
        when (tier) {
            Tier.MK1 -> IndustrialRevolution.CONFIG.machines.electricFurnaceMk1
            Tier.MK2 -> IndustrialRevolution.CONFIG.machines.electricFurnaceMk2
            Tier.MK3 -> IndustrialRevolution.CONFIG.machines.electricFurnaceMk3
            else -> IndustrialRevolution.CONFIG.machines.electricFurnaceMk4
        }
}