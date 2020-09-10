package me.steven.indrev.blockentities.generators

import me.steven.indrev.components.InventoryComponent
import me.steven.indrev.components.Property
import me.steven.indrev.components.TemperatureComponent
import me.steven.indrev.inventories.IRInventory
import me.steven.indrev.items.misc.IRCoolerItem
import me.steven.indrev.registry.IRRegistry
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.EMPTY_INT_ARRAY
import me.steven.indrev.utils.Tier
import net.minecraft.item.ItemStack
import net.minecraft.screen.ArrayPropertyDelegate
import team.reborn.energy.Energy

class BiomassGeneratorBlockEntity(tier: Tier) : GeneratorBlockEntity(tier, MachineRegistry.BIOMASS_GENERATOR_REGISTRY) {

    init {
        this.propertyDelegate = ArrayPropertyDelegate(5)
        this.inventoryComponent = InventoryComponent({ this }) {
            IRInventory(3, intArrayOf(2), EMPTY_INT_ARRAY) { slot, stack ->
                val item = stack?.item
                when {
                    Energy.valid(stack) && Energy.of(stack).maxOutput > 0 -> slot == 0
                    item is IRCoolerItem -> slot == 1
                    slot == 2 -> stack?.item == IRRegistry.BIOMASS
                    else -> false
                }
            }
        }
        this.temperatureComponent = TemperatureComponent({ this }, 0.08, 900..2000, 2500.0)
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