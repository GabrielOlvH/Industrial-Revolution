package me.steven.indrev.networks.item

import me.steven.indrev.networks.Network
import me.steven.indrev.networks.ServoNetworkState
import net.minecraft.server.world.ServerWorld

class ItemNetworkState(world: ServerWorld) : ServoNetworkState<ItemNetwork>(Network.Type.ITEM, world)