package me.steven.indrev.blockentities.crafters

import me.steven.indrev.IndustrialRevolution
import me.steven.indrev.components.FluidInfuserFluidComponent
import me.steven.indrev.components.InventoryComponent
import me.steven.indrev.config.IConfig
import me.steven.indrev.inventories.IRInventory
import me.steven.indrev.items.misc.IRCoolerItem
import me.steven.indrev.items.upgrade.IRUpgradeItem
import me.steven.indrev.items.upgrade.Upgrade
import me.steven.indrev.recipes.machines.FluidInfuserRecipe
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.Tier
import net.minecraft.inventory.Inventory
import team.reborn.energy.Energy

class FluidInfuserBlockEntity(tier: Tier) : CraftingMachineBlockEntity<FluidInfuserRecipe>(tier, MachineRegistry.FLUID_INFUSER_REGISTRY) {

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
        this.fluidComponent = FluidInfuserFluidComponent()
    }

    private var currentRecipe: FluidInfuserRecipe? = null

    override fun tryStartRecipe(inventory: IRInventory): FluidInfuserRecipe? {
        val inputStacks = inventory.getInputInventory()
        val fluid = fluidComponent!!.tanks[0].volume
        val recipe = world?.recipeManager?.listAllOfType(FluidInfuserRecipe.TYPE)
            ?.firstOrNull { it.matches(inputStacks, fluid) }
            ?: return null
        val fluidVolume = fluidComponent!!.tanks[1].volume
        val outputStack = inventory.getStack(3).copy()
        if ((fluidVolume.isEmpty || fluidVolume.amount().add(recipe.inputFluid.amount()) <= fluidComponent!!.limit)
            && (outputStack.isEmpty || (outputStack.count + recipe.output.count <= outputStack.maxCount && outputStack.item == recipe.output.item))) {
            if (!isProcessing()) {
                processTime = recipe.processTime
                totalProcessTime = recipe.processTime
            }
            this.currentRecipe = recipe
        }
        return recipe
    }

    override fun matchesRecipe(recipe: FluidInfuserRecipe?, inventory: Inventory): Boolean =
        recipe?.matches(inventory, fluidComponent!!.tanks[0].volume) == true

    override fun onCraft() {
        val inputTank = fluidComponent!!.tanks[0]
        val outputTank = fluidComponent!!.tanks[1]
        inputTank.volume = inputTank.volume.fluidKey.withAmount(inputTank.volume.amount().sub(currentRecipe?.inputFluid?.amount()))
        outputTank.volume = currentRecipe?.outputFluid?.fluidKey?.withAmount(outputTank.volume.amount().add(currentRecipe?.outputFluid?.amount())) ?: return
    }

    override fun getCurrentRecipe(): FluidInfuserRecipe? = currentRecipe

    override fun getConfig(): IConfig =
        when (tier) {
            Tier.MK1 -> IndustrialRevolution.CONFIG.machines.fluidInfuserMk1
            Tier.MK2 -> IndustrialRevolution.CONFIG.machines.fluidInfuserMk2
            Tier.MK3 -> IndustrialRevolution.CONFIG.machines.fluidInfuserMk3
            else -> IndustrialRevolution.CONFIG.machines.fluidInfuserMk4
        }

    override fun getUpgradeSlots(): IntArray = intArrayOf(4, 5, 6, 7)

    override fun getAvailableUpgrades(): Array<Upgrade> = Upgrade.ALL
}