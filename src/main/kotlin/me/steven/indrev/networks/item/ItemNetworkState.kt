package me.steven.indrev.networks.item

import me.steven.indrev.networks.EndpointData
import me.steven.indrev.networks.Network
import me.steven.indrev.networks.ServoNetworkState
import net.minecraft.server.world.ServerWorld

class ItemNetworkState(world: ServerWorld) : ServoNetworkState<ItemNetwork>(Network.Type.ITEM, world) {
    override fun createEndpointData(type: EndpointData.Type, mode: EndpointData.Mode?): EndpointData = ItemEndpointData(type, mode, false, false, false)


}