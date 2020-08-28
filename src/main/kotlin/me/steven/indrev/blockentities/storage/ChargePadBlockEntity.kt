package me.steven.indrev.blockentities.storage

import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.components.InventoryComponent
import me.steven.indrev.inventories.IRInventory
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.EMPTY_INT_ARRAY
import me.steven.indrev.utils.Tier
import team.reborn.energy.Energy

class ChargePadBlockEntity(tier: Tier) : MachineBlockEntity(tier, MachineRegistry.CHARGE_PAD_REGISTRY) {
    init {
        this.inventoryComponent = InventoryComponent {
            IRInventory(1, EMPTY_INT_ARRAY, EMPTY_INT_ARRAY) { _, stack -> Energy.valid(stack) }
        }
    }

    override fun machineTick() {
        if (world?.isClient == true) return
        val inventory = inventoryComponent?.inventory ?: return
        val stack = inventory.getStack(0)
        if (Energy.valid(stack)) {
            setWorkingState(true)
            Energy.of(this).into(Energy.of(stack)).move()
        } else setWorkingState(false)
    }

    override fun getBaseBuffer(): Double = tier.io * 2
}