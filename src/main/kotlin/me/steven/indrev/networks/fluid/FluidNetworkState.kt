package me.steven.indrev.networks.fluid

import me.steven.indrev.networks.Network
import me.steven.indrev.networks.ServoNetworkState
import net.minecraft.server.world.ServerWorld

class FluidNetworkState(world: ServerWorld) : ServoNetworkState<FluidNetwork>(Network.Type.FLUID, world)