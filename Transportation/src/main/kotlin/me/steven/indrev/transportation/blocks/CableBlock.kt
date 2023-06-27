package me.steven.indrev.transportation.blocks

import me.steven.indrev.api.Tier
import me.steven.indrev.transportation.networks.types.EnergyPipeNetwork
import me.steven.indrev.transportation.networks.types.PipeNetwork
import net.minecraft.server.world.ServerWorld

class CableBlock(tier: Tier) : PipeBlock(tier) {
    override fun createNetwork(world: ServerWorld): PipeNetwork<*> = EnergyPipeNetwork(world)
}