package me.steven.indrev.api

import dev.technici4n.fasttransferlib.api.energy.EnergyIo
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import me.steven.indrev.networks.NetworkState
import me.steven.indrev.networks.energy.EnergyNetwork
import me.steven.indrev.networks.fluid.FluidNetworkState
import me.steven.indrev.networks.item.ItemNetworkState
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiCache
import net.minecraft.util.math.Direction

interface ServerWorldExtension {
    fun indrev_getEnergyCache(): Long2ObjectOpenHashMap<BlockApiCache<EnergyIo, Direction>>

    fun indrev_getEnergyNetworkState(): NetworkState<EnergyNetwork>

    fun indrev_getFluidNetworkState(): FluidNetworkState

    fun indrev_getItemNetworkState(): ItemNetworkState
}