package me.steven.indrev.blockentities.generators

import me.steven.indrev.components.InventoryController
import me.steven.indrev.components.Property
import me.steven.indrev.components.TemperatureController
import me.steven.indrev.inventories.DefaultSidedInventory
import me.steven.indrev.items.CoolerItem
import me.steven.indrev.items.rechargeable.RechargeableItem
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.registry.ModRegistry
import me.steven.indrev.utils.Tier
import net.minecraft.item.ItemStack
import net.minecraft.screen.ArrayPropertyDelegate

class BiomassGeneratorBlockEntity(tier: Tier) : GeneratorBlockEntity(tier, MachineRegistry.BIOMASS_GENERATOR_REGISTRY) {

    init {
        this.propertyDelegate = ArrayPropertyDelegate(5)
        this.inventoryController = InventoryController({ this }) {
            DefaultSidedInventory(3, intArrayOf(2), intArrayOf()) { slot, stack ->
                val item = stack?.item
                when {
                    item is RechargeableItem && item.canOutput -> slot == 0
                    item is CoolerItem -> slot == 1
                    slot == 2 -> stack?.item == ModRegistry.BIOMASS
                    else -> false
                }
            }
        }
        this.temperatureController = TemperatureController({ this }, 0.08, 900..2000, 2500.0)
    }

    private var burnTime: Int by Property(3, 0)
    private var maxBurnTime: Int by Property(4, 0)

    override fun shouldGenerate(): Boolean {
        if (burnTime > 0) burnTime--
        else if (maxStoredPower > energy) {
            val inventory = inventoryController?.getInventory() ?: return false
            val invStack = inventory.getStack(2)
            if (!invStack.isEmpty && invStack.item == ModRegistry.BIOMASS) {
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

    override fun getGenerationRatio(): Double = 0.5
}