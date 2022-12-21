package me.steven.indrev.transportation.blocks

import me.steven.indrev.transportation.networks.types.ItemPipeNetwork
import me.steven.indrev.transportation.networks.types.PipeNetwork
import net.minecraft.server.world.ServerWorld

class ItemPipeBlock : StoragePipeBlock() {
    override fun createNetwork(world: ServerWorld): PipeNetwork<*> = ItemPipeNetwork(world)
}