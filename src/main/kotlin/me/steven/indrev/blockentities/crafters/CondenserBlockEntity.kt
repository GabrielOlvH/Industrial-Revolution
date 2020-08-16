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
import kotlin.math.ceil

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
        val fluid = fluidComponent!!.tanks[0].volume
        val recipe = world?.recipeManager?.listAllOfType(CondenserRecipe.TYPE)
            ?.firstOrNull { it.fluid.fluidKey == fluid.fluidKey && fluid.amount() >= it.fluid.amount() } ?: return null
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

    override fun machineTick() {
        if (world?.isClient == true) return
        val inventory = inventoryComponent?.inventory ?: return
        val fluidComponent = fluidComponent ?: return
        if (isProcessing()) {
            val recipe = getCurrentRecipe()
            if (recipe != null && (fluidComponent.tanks[0].volume.fluidKey != recipe.fluid.fluidKey || fluidComponent.tanks[0].volume.amount() <= recipe.fluid.amount()))
                tryStartRecipe(inventory) ?: reset()
            else if (Energy.of(this).use(Upgrade.ENERGY(this))) {
                setWorkingState(true)
                processTime = (processTime - ceil(Upgrade.SPEED(this))).coerceAtLeast(0.0).toInt()
                if (processTime <= 0) {
                    fluidComponent.extractable.extract(recipe?.fluid?.amount() ?: return reset())
                    val output = recipe.craft(inventory)
                    for (outputSlot in inventory.outputSlots) {
                        val outputStack = inventory.getStack(outputSlot)
                        if (outputStack.item == output.item)
                            inventory.setStack(outputSlot, outputStack.apply { increment(output.count) })
                        else if (outputStack.isEmpty)
                            inventory.setStack(outputSlot, output)
                        else continue
                        break
                    }
                    usedRecipes[recipe.id] = usedRecipes.computeIfAbsent(recipe.id) { 0 } + 1
                    onCraft()
                    reset()
                }
            }
        } else if (energy > 0 && !fluidComponent.tanks[0].volume.isEmpty && processTime <= 0) {
            reset()
            if (tryStartRecipe(inventory) == null) setWorkingState(false)
        }
        temperatureComponent?.tick(isProcessing())
    }

    override fun getCurrentRecipe(): CondenserRecipe? = currentRecipe

    override fun getConfig(): IConfig = IndustrialRevolution.CONFIG.machines.pulverizerMk4

    override fun getUpgradeSlots(): IntArray = intArrayOf(3, 4, 5, 6)

    override fun getAvailableUpgrades(): Array<Upgrade> = Upgrade.ALL
}