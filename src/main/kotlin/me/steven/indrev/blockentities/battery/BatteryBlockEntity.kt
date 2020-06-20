package me.steven.indrev.blockentities.battery

import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.blocks.VerticalFacingMachineBlock
import me.steven.indrev.components.InventoryController
import me.steven.indrev.inventories.DefaultSidedInventory
import me.steven.indrev.items.rechargeable.Rechargeable
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.Tier
import net.minecraft.screen.ArrayPropertyDelegate
import team.reborn.energy.Energy
import team.reborn.energy.EnergySide

class BatteryBlockEntity(tier: Tier) :
    MachineBlockEntity(tier, MachineRegistry.CONTAINER_REGISTRY) {

    init {
        this.propertyDelegate = ArrayPropertyDelegate(2)
        this.inventoryController = InventoryController({ this }) {
            DefaultSidedInventory(1, intArrayOf(0), intArrayOf()) { _, stack -> stack?.item is Rechargeable }
        }
    }

    override fun tick() {
        super.tick()
        if (world?.isClient == true) return
        val inventory = inventoryController?.getInventory() ?: return
        val stack = inventory.getStack(0)
        if (stack.item is Rechargeable && stack.isDamaged && Energy.of(this).use(1.0)) {
            inventory.setStack(0, stack.copy().apply { damage-- })
            update()
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