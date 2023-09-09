package me.steven.indrev.transportation.events

import me.steven.indrev.transportation.blocks.PipeBlockEntity
import me.steven.indrev.transportation.networks.MANAGERS
import me.steven.indrev.transportation.networks.networkManager
import me.steven.indrev.transportation.packets.CreatePipeBlockEntityPacket
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerBlockEntityEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.minecraft.block.entity.BlockEntity
import net.minecraft.server.MinecraftServer
import net.minecraft.server.world.ServerWorld

object ServerEvents : ServerLifecycleEvents.ServerStopped, ServerTickEvents.StartWorldTick, ServerBlockEntityEvents.Load {
    override fun onStartTick(world: ServerWorld) {
        world.networkManager.runScheduledUpdates()

        world.networkManager.networks.forEach {
            if (it.ready) {
                it.tick()
            }
        }
    }

    override fun onServerStopped(server: MinecraftServer?) {
        MANAGERS.clear()
    }

    override fun onLoad(blockEntity: BlockEntity, world: ServerWorld) {
        if (blockEntity is PipeBlockEntity) {
            world.networkManager.scheduledUpdates.add(blockEntity.pos.asLong())
        }
    }
}