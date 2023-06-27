package me.steven.indrev.transportation.blocks

import me.steven.indrev.api.Tier
import me.steven.indrev.transportation.networks.types.FluidPipeNetwork
import me.steven.indrev.transportation.networks.types.ItemPipeNetwork
import me.steven.indrev.transportation.networks.types.PipeNetwork
import net.minecraft.server.world.ServerWorld

class FluidPipeBlock(tier: Tier) : StoragePipeBlock(tier) {
    override fun createNetwork(world: ServerWorld): PipeNetwork<*> = FluidPipeNetwork(world)
}