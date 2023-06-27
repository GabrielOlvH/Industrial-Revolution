package me.steven.indrev.transportation.blocks

import me.steven.indrev.api.Tier
import me.steven.indrev.transportation.networks.types.ItemPipeNetwork
import me.steven.indrev.transportation.networks.types.PipeNetwork
import net.minecraft.server.world.ServerWorld

class ItemPipeBlock(tier: Tier) : StoragePipeBlock(tier) {
    override fun createNetwork(world: ServerWorld): PipeNetwork<*> = ItemPipeNetwork(world)
}