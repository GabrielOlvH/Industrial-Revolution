package me.steven.indrev.blockentities.generators

import me.steven.indrev.components.TemperatureComponent
import me.steven.indrev.inventories.inventory
import me.steven.indrev.registry.IRRegistry
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.Property
import me.steven.indrev.utils.Tier
import net.minecraft.item.ItemStack
import net.minecraft.screen.ArrayPropertyDelegate

class BiomassGeneratorBlockEntity(tier: Tier) : GeneratorBlockEntity(tier, MachineRegistry.BIOMASS_GENERATOR_REGISTRY) {

    init {
        this.propertyDelegate = ArrayPropertyDelegate(5)
        this.temperatureComponent = TemperatureComponent({ this }, 0.08, 900..2000, 2500.0)
        this.inventoryComponent = inventory(this) {
            input { slot = 2 }
        }
    }

    private var burnTime: Int by Property(3, 0)
    private var maxBurnTime: Int by Property(4, 0)

    override fun shouldGenerate(): Boolean {
        if (burnTime > 0) burnTime--
        else if (maxStoredPower > energy) {
            val inventory = inventoryComponent?.inventory ?: return false
            val invStack = inventory.getStack(2)
            if (!invStack.isEmpty && invStack.item == IRRegistry.BIOMASS) {
                burnTime = 300
                maxBurnTime = burnTime
                invStack.count--
                if (invStack.isEmpty) inventory.setStack(2, ItemStack.EMPTY)
                else inventory.setStack(2, invStack)
            }
        }
        markDirty()
        return burnTime > 0 && energy < maxStoredPower
    }
}