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
import me.steven.indrev.recipes.machines.CondenserRecipe
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.Tier
import team.reborn.energy.Energy

class CondenserBlockEntity(tier: Tier) :
    CraftingMachineBlockEntity<CondenserRecipe>(tier, MachineRegistry.CONDENSER_REGISTRY) {

    init {
        this.inventoryComponent = InventoryComponent {
            IRInventory(7, intArrayOf(), intArrayOf(2)) { slot, stack ->
                val item = stack?.item
                when {
                    item is IRUpgradeItem -> getUpgradeSlots().contains(slot)
                    Energy.valid(stack) && Energy.of(stack).maxOutput > 0 -> slot == 0
                    item is IRCoolerItem -> slot == 1
                    else -> false
                }
            }
        }
        this.fluidComponent = FluidComponent(FluidAmount(8))
    }

    private var currentRecipe: CondenserRecipe? = null

    override fun tryStartRecipe(inventory: IRInventory): CondenserRecipe? {
        val fluid = fluidComponent!!.volume
        val inputStacks = inventory.getInputInventory()
        val recipe = world?.recipeManager?.getAllMatches(CondenserRecipe.TYPE, inputStacks, world)
            ?.firstOrNull { it.fluid.fluidKey == fluid.fluidKey && fluid.amount() >= fluid.amount() } ?: return null
        val outputStack = inventory.getStack(2).copy()
        if (outputStack.isEmpty || (outputStack.count + recipe.output.count <= outputStack.maxCount && outputStack.item == recipe.output.item)) {
            if (!isProcessing()) {
                processTime = recipe.processTime
                totalProcessTime = recipe.processTime
            }
            this.currentRecipe = recipe
        }
        return recipe
    }

    override fun getCurrentRecipe(): CondenserRecipe? = currentRecipe

    override fun getConfig(): IConfig = IndustrialRevolution.CONFIG.machines.pulverizerMk4

    override fun getUpgradeSlots(): IntArray = intArrayOf(3, 4, 5, 6)

    override fun getAvailableUpgrades(): Array<Upgrade> = Upgrade.ALL

}