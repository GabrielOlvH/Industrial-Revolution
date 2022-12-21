package me.steven.indrev.extensions

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiCache
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.Direction
import team.reborn.energy.api.EnergyStorage

interface ServerWorldExtension {
    fun indrev_getEnergyIoCache(): Long2ObjectOpenHashMap<BlockApiCache<EnergyStorage, Direction>>
}

val ServerWorld.energyIoCache get() = (this as ServerWorldExtension).indrev_getEnergyIoCache()