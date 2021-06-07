package me.steven.indrev.networks.client

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import net.minecraft.util.math.BlockPos

class ClientNetworkState {
    val requests = ObjectOpenHashSet<BlockPos>()
    val info = Int2ObjectOpenHashMap<ClientNetworkInfo>()
}