package me.steven.indrev.blockentities.storage

import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.config.BasicMachineConfig
import me.steven.indrev.inventories.inventory
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.Tier
import me.steven.indrev.utils.TransferMode
import net.minecraft.screen.ArrayPropertyDelegate
import net.minecraft.util.math.Direction
import team.reborn.energy.Energy
import team.reborn.energy.EnergySide

class BatteryBlockEntity(tier: Tier) :
    MachineBlockEntity<BasicMachineConfig>(tier, MachineRegistry.CONTAINER_REGISTRY) {

    init {
        this.propertyDelegate = ArrayPropertyDelegate(2)
        this.inventoryComponent = inventory(this) {
            0 filter { stack -> Energy.valid(stack) }
        }
    }

    val transferConfig: MutableMap<Direction, TransferMode> = mutableMapOf<Direction, TransferMode>().also { map ->
        Direction.values().forEach { dir -> map[dir] = TransferMode.NONE }
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
        return if (transferConfig[Direction.values()[side!!.ordinal]] == TransferMode.OUTPUT) super.getMaxOutput(side) else 0.0
    }

    override fun getMaxInput(side: EnergySide?): Double {
        return if (transferConfig[Direction.values()[side!!.ordinal]] == TransferMode.INPUT) super.getMaxInput(side) else 0.0
    }

    override fun getBaseBuffer(): Double = when (tier) {
        Tier.MK1 -> 10000.0
        Tier.MK2 -> 100000.0
        Tier.MK3 -> 1000000.0
        Tier.MK4 -> 10000000.0
        Tier.CREATIVE -> Double.MAX_VALUE
    }
}