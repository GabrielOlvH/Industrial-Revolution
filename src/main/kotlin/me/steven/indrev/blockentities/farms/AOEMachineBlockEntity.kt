package me.steven.indrev.blockentities.farms

import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.Tier
import net.minecraft.util.math.Box

abstract class AOEMachineBlockEntity(tier: Tier, registry: MachineRegistry) : MachineBlockEntity(tier, registry) {
    var renderWorkingArea = false
    abstract fun getWorkingArea(): Box
}