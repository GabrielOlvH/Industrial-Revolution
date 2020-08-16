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
import me.steven.indrev.utils.EMPTY_INT_ARRAY
import me.steven.indrev.utils.Tier
import net.minecraft.item.ItemStack
import team.reborn.energy.Energy
import kotlin.math.ceil

class FluidInfuserBlockEntity(tier: Tier) : CraftingMachineBlockEntity<FluidInfuserRecipe>(tier, MachineRegistry.FLUID_INFUSER_REGISTRY) {

    init {
        this.inventoryComponent = InventoryComponent {
            IRInventory(7, intArrayOf(2), EMPTY_INT_ARRAY) { slot, stack ->
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
            ?.firstOrNull { it.matches(inputStacks, fluid, world) }
            ?: return null
        val fluidVolume = fluidComponent!!.tanks[1].volume
        if (fluidVolume.isEmpty || fluidVolume.amount().add(recipe.inputFluid.amount()) <= fluidComponent!!.limit) {
            if (!isProcessing()) {
                processTime = recipe.processTime
                totalProcessTime = recipe.processTime
            }
            this.currentRecipe = recipe
        }
        return recipe
    }

    override fun machineTick() {
        if (world?.isClient == true) return
        val inventory = inventoryComponent?.inventory ?: return
        val inputInventory = inventory.getInputInventory()
        if (inputInventory.isEmpty) {
            reset()
            setWorkingState(false)
        } else if (isProcessing()) {
            val recipe = getCurrentRecipe()
            if (recipe?.matches(inputInventory, fluidComponent!!.tanks[0].volume, this.world) == false)
                tryStartRecipe(inventory) ?: reset()
            else if (Energy.of(this).use(Upgrade.ENERGY(this))) {
                setWorkingState(true)
                processTime = (processTime - ceil(Upgrade.SPEED(this))).coerceAtLeast(0.0).toInt()
                if (processTime <= 0) {
                    inventory.inputSlots.forEachIndexed { index, slot ->
                        val stack = inputInventory.getStack(index)
                        val item = stack.item
                        if (
                            item.hasRecipeRemainder()
                        )
                            inventory.setStack(slot, ItemStack(item.recipeRemainder))
                        else {
                            stack.decrement(1)
                            inventory.setStack(slot, stack)
                        }
                    }
                    val inputTank = fluidComponent!!.tanks[0]
                    val outputTank = fluidComponent!!.tanks[1]
                    inputTank.volume = inputTank.volume.fluidKey.withAmount(inputTank.volume.amount().sub(recipe!!.inputFluid.amount()))
                    outputTank.volume = outputTank.volume.fluidKey.withAmount(outputTank.volume.amount().add(recipe.outputFluid.amount()))

                    usedRecipes[recipe.id] = usedRecipes.computeIfAbsent(recipe.id) { 0 } + 1
                    onCraft()
                    reset()
                }
            }
        } else if (energy > 0 && !inputInventory.isEmpty && processTime <= 0) {
            reset()
            if (tryStartRecipe(inventory) == null) setWorkingState(false)
        }
        temperatureComponent?.tick(isProcessing())
    }

    override fun getCurrentRecipe(): FluidInfuserRecipe? = currentRecipe

    override fun getConfig(): IConfig = IndustrialRevolution.CONFIG.machines.pulverizerMk4

    override fun getUpgradeSlots(): IntArray = intArrayOf(3, 4, 5, 6)

    override fun getAvailableUpgrades(): Array<Upgrade> = Upgrade.ALL
}