package me.steven.indrev.utils

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import me.steven.indrev.api.ServerWorldExtension
import me.steven.indrev.networks.energy.EnergyNetworkState
import me.steven.indrev.networks.fluid.FluidNetworkState
import me.steven.indrev.networks.item.ItemNetworkState
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiCache
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.Direction
import team.reborn.energy.api.EnergyStorage

val ServerWorld.energyIoCache: Long2ObjectOpenHashMap<BlockApiCache<EnergyStorage, Direction?>>
    get() = (this as ServerWorldExtension).indrev_getEnergyCache()

val ServerWorld.energyNetworkState: EnergyNetworkState
    get() = (this as ServerWorldExtension).indrev_getEnergyNetworkState()

val ServerWorld.fluidNetworkState: FluidNetworkState
    get() = (this as ServerWorldExtension).indrev_getFluidNetworkState()

val ServerWorld.itemNetworkState: ItemNetworkState
    get() = (this as ServerWorldExtension).indrev_getItemNetworkState()