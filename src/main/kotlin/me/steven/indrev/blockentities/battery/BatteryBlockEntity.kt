package me.steven.indrev.blockentities.battery

import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.blocks.FacingMachineBlock
import me.steven.indrev.components.InventoryComponent
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
        this.inventoryComponent = InventoryComponent {
            IRInventory(1, intArrayOf(0), EMPTY_INT_ARRAY) { _, stack -> Energy.valid(stack) }
        }
    }

    override fun machineTick() {
        if (world?.isClient == true) return
        val inventory = inventoryComponent?.inventory ?: return
        val stack = inventory.getStack(0)
        if (Energy.valid(stack)) {
            val handler = Energy.of(stack)
            Energy.of(this).into(handler).move()
            stack.damage = (stack.maxDamage - handler.energy.toInt()).coerceAtLeast(1)
        }
    }

    override fun getMaxOutput(side: EnergySide?): Double {
        val state = this.cachedState
        return if (side != EnergySide.fromMinecraft(state[FacingMachineBlock.FACING])) super.getMaxOutput(side) else 0.0
    }

    override fun getMaxInput(side: EnergySide?): Double {
        val state = this.cachedState
        return if (side == EnergySide.fromMinecraft(state[FacingMachineBlock.FACING])) super.getMaxInput(side) else 0.0
    }
}