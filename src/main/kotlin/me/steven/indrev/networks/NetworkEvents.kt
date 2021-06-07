package me.steven.indrev.networks

import me.steven.indrev.blocks.machine.pipes.BasePipeBlock
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerBlockEntityEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.minecraft.block.entity.BlockEntity
import net.minecraft.server.MinecraftServer
import net.minecraft.server.world.ServerWorld

object NetworkEvents : ServerTickEvents.EndWorldTick, ServerLifecycleEvents.ServerStopped, ServerBlockEntityEvents.Load {
    override fun onEndTick(world: ServerWorld) {
        Network.Type.ENERGY.tick(world)
        Network.Type.FLUID.tick(world)
        Network.Type.ITEM.tick(world)
    }

    override fun onServerStopped(server: MinecraftServer?) {
        Network.Type.ENERGY.clear()
        Network.Type.FLUID.clear()
        Network.Type.ITEM.clear()
    }

    override fun onLoad(blockEntity: BlockEntity, world: ServerWorld) {
        val pipeBlock = blockEntity.cachedState.block as? BasePipeBlock ?: return
        pipeBlock.type.queueUpdate(blockEntity.pos.asLong())
    }
}