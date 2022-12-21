package me.steven.indrev.transportation

import me.steven.indrev.transportation.client.NetworkPathRenderer
import me.steven.indrev.transportation.client.events.ClientWorldEvents
import me.steven.indrev.transportation.packets.AddPipeRenderDataPacket
import me.steven.indrev.transportation.client.PipeModelProvider
import me.steven.indrev.transportation.packets.RemovePipeRenderDataPacket
import me.steven.indrev.transportation.packets.ShowPipePathPacket
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents
import net.minecraft.client.render.RenderLayer

fun initClient() {
    AddPipeRenderDataPacket.register()
    RemovePipeRenderDataPacket.register()
    ShowPipePathPacket.register()

    ClientTickEvents.END_CLIENT_TICK.register(ClientWorldEvents)

    ModelLoadingRegistry.INSTANCE.registerVariantProvider { PipeModelProvider }

    WorldRenderEvents.AFTER_TRANSLUCENT.register(NetworkPathRenderer)

    BlockRenderLayerMap.INSTANCE.putBlock(ITEM_PIPE_BLOCK, RenderLayer.getCutout())
    BlockRenderLayerMap.INSTANCE.putBlock(CABLE_BLOCK, RenderLayer.getCutout())
}