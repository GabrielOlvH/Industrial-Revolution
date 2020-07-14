package me.steven.indrev.blockentities.battery

import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.components.InventoryController
import me.steven.indrev.inventories.IRInventory
import me.steven.indrev.items.rechargeable.Rechargeable
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.Tier
import team.reborn.energy.Energy

class ChargePadBlockEntity(tier: Tier) : MachineBlockEntity(tier, MachineRegistry.CHARGE_PAD_REGISTRY) {
    init {
        this.inventoryController = InventoryController {
            IRInventory(1, intArrayOf(0), intArrayOf(0)) { _, stack -> Energy.valid(stack) || stack?.item is Rechargeable }
        }
    }

    override fun machineTick() {
        if (world?.isClient == true) return
        val inventory = inventoryController?.inventory ?: return
        val stack = inventory.getStack(0)
        if (stack.item is Rechargeable && stack.isDamaged && stack.damage > 0 && Energy.of(this).use(1.0)) {
            setWorkingState(true)
            inventory.setStack(0, stack.copy().apply { damage-- })
        } else setWorkingState(false)
    }
}