package me.steven.indrev.blockentities.battery

import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.blocks.VerticalFacingMachineBlock
import me.steven.indrev.components.InventoryController
import me.steven.indrev.inventories.IRInventory
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.EMPTY_INT_ARRAY
import me.steven.indrev.utils.Tier
import net.minecraft.screen.ArrayPropertyDelegate
import team.reborn.energy.Energy
import team.reborn.energy.EnergySide

class BatteryBlockEntity(tier: Tier) :
    MachineBlockEntity(tier, MachineRegistry.CONTAINER_REGISTRY) {

    init {
        this.propertyDelegate = ArrayPropertyDelegate(2)
        this.inventoryController = InventoryController {
            IRInventory(1, intArrayOf(0), EMPTY_INT_ARRAY) { _, stack -> Energy.valid(stack) }
        }
    }

    override fun machineTick() {
        if (world?.isClient == true) return
        val inventory = inventoryController?.inventory ?: return
        val stack = inventory.getStack(0)
        if (Energy.valid(stack)) {
            val handler = Energy.of(stack)
            Energy.of(this).into(handler).move()
            stack.damage = (stack.maxDamage - handler.energy.toInt()).coerceAtLeast(1)
        }
    }

    override fun getMaxOutput(side: EnergySide?): Double {
        val state = this.cachedState
        return if (side != EnergySide.fromMinecraft(state[VerticalFacingMachineBlock.FACING])) super.getMaxOutput(side) else 0.0
    }

    override fun getMaxInput(side: EnergySide?): Double {
        val state = this.cachedState
        return if (side == EnergySide.fromMinecraft(state[VerticalFacingMachineBlock.FACING])) super.getMaxInput(side) else 0.0
    }
}