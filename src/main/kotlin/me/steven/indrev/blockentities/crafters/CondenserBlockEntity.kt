package me.steven.indrev.blockentities.crafters

import alexiil.mc.lib.attributes.fluid.amount.FluidAmount
import me.steven.indrev.components.FluidComponent
import me.steven.indrev.inventories.inventory
import me.steven.indrev.items.upgrade.Upgrade
import me.steven.indrev.recipes.machines.CondenserRecipe
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.Tier
import net.minecraft.recipe.RecipeType

class CondenserBlockEntity(tier: Tier) :
    CraftingMachineBlockEntity<CondenserRecipe>(tier, MachineRegistry.CONDENSER_REGISTRY) {

    init {
        this.inventoryComponent = inventory(this) {
            output { slot = 2 }
            coolerSlot = 1
        }
        this.fluidComponent = FluidComponent({ this }, FluidAmount(8))
    }

    override val type: RecipeType<CondenserRecipe> = CondenserRecipe.TYPE

    override fun getUpgradeSlots(): IntArray = intArrayOf(3, 4, 5, 6)

    override fun getAvailableUpgrades(): Array<Upgrade> = Upgrade.DEFAULT
}