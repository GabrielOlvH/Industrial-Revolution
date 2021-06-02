package me.steven.indrev.blockentities.generators

import me.steven.indrev.api.machines.Tier
import me.steven.indrev.components.TemperatureComponent
import me.steven.indrev.inventories.inventory
import me.steven.indrev.registry.MachineRegistry
import net.minecraft.block.BlockState
import net.minecraft.block.entity.AbstractFurnaceBlockEntity
import net.minecraft.item.Item
import net.minecraft.screen.ArrayPropertyDelegate
import net.minecraft.util.math.BlockPos

class CoalGeneratorBlockEntity(pos: BlockPos, state: BlockState) :
    SolidFuelGeneratorBlockEntity(Tier.MK1, MachineRegistry.COAL_GENERATOR_REGISTRY, pos, state) {

    init {
        this.propertyDelegate = ArrayPropertyDelegate(6)
        this.temperatureComponent = TemperatureComponent({ this }, 0.08, 900..2000, 2500.0)
        this.inventoryComponent = inventory(this) {
            input {
                slot = 2
                2 filter { stack -> BURN_TIME_MAP.containsKey(stack.item) }
            }
        }
    }

    override fun getFuelMap(): Map<Item, Int> = BURN_TIME_MAP

    companion object {
        private val BURN_TIME_MAP = AbstractFurnaceBlockEntity.createFuelTimeMap()
    }
}