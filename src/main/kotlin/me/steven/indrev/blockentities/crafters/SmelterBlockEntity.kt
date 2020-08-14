package me.steven.indrev.blockentities.crafters

import alexiil.mc.lib.attributes.fluid.amount.FluidAmount
import me.steven.indrev.IndustrialRevolution
import me.steven.indrev.components.FluidComponent
import me.steven.indrev.components.InventoryComponent
import me.steven.indrev.config.IConfig
import me.steven.indrev.inventories.IRInventory
import me.steven.indrev.items.misc.IRCoolerItem
import me.steven.indrev.items.upgrade.IRUpgradeItem
import me.steven.indrev.items.upgrade.Upgrade
import me.steven.indrev.recipes.machines.SmelterRecipe
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.Tier
import team.reborn.energy.Energy

class SmelterBlockEntity(tier: Tier) :
    CraftingMachineBlockEntity<SmelterRecipe>(tier, MachineRegistry.SMELTER_REGISTRY) {

    init {
        this.inventoryComponent = InventoryComponent {
            IRInventory(7, intArrayOf(2), intArrayOf()) { slot, stack ->
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
        this.fluidComponent = FluidComponent(FluidAmount(50))
    }

    private var currentRecipe: SmelterRecipe? = null

    override fun tryStartRecipe(inventory: IRInventory): SmelterRecipe? {
        val inputStacks = inventory.getInputInventory()
        val recipe = world?.recipeManager?.getAllMatches(SmelterRecipe.TYPE, inputStacks, world)
            ?.firstOrNull { it.matches(inputStacks, world) } ?: return null
        val fluidVolume = fluidComponent!!.volume
        if (fluidVolume.isEmpty || fluidVolume.amount().add(recipe.fluid.amount()) <= fluidComponent!!.limit) {
            if (!isProcessing()) {
                processTime = recipe.processTime
                totalProcessTime = recipe.processTime
            }
            this.currentRecipe = recipe
        }
        return recipe
    }

    override fun onCraft() {
        fluidComponent!!.insertable.insert(currentRecipe!!.fluid)
    }

    override fun getCurrentRecipe(): SmelterRecipe? = currentRecipe

    override fun getConfig(): IConfig = IndustrialRevolution.CONFIG.machines.pulverizerMk4

    override fun getUpgradeSlots(): IntArray = intArrayOf(3, 4, 5, 6)

    override fun getAvailableUpgrades(): Array<Upgrade> = Upgrade.ALL
}