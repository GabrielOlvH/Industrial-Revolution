package me.steven.indrev.blockentities.solarpowerplant

import alexiil.mc.lib.attributes.fluid.amount.FluidAmount
import me.steven.indrev.api.machines.Tier
import me.steven.indrev.blockentities.crafters.CraftingMachineBlockEntity
import me.steven.indrev.components.TemperatureComponent
import me.steven.indrev.components.fluid.FluidComponent
import me.steven.indrev.inventories.inventory
import me.steven.indrev.items.upgrade.Upgrade
import me.steven.indrev.recipes.machines.DistillerRecipe
import me.steven.indrev.recipes.machines.IRRecipeType
import me.steven.indrev.registry.MachineRegistry

class DistillerBlockEntity : CraftingMachineBlockEntity<DistillerRecipe>(Tier.MK4, MachineRegistry.DISTILLER_REGISTRY) {

    override val upgradeSlots: IntArray = intArrayOf(3, 4, 5, 6)
    override val availableUpgrades: Array<Upgrade> = Upgrade.DEFAULT

    init {
        this.temperatureComponent = TemperatureComponent({ this }, 0.01, 70..120, 200.0)
        this.fluidComponent = FluidComponent(FluidAmount.BUCKET)
        this.inventoryComponent = inventory(this) {
            output { slot = 2 }
        }
    }

    override val type: IRRecipeType<DistillerRecipe> = DistillerRecipe.TYPE

    override fun getMaxUpgrade(upgrade: Upgrade): Int {
        return if (upgrade == Upgrade.SPEED) return 2 else super.getMaxUpgrade(upgrade)
    }
}