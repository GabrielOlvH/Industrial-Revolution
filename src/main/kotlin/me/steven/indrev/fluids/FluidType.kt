package me.steven.indrev.fluids

import me.steven.indrev.utils.identifier
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback
import net.fabricmc.fabric.api.resource.ResourceManagerHelper
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener
import net.minecraft.client.MinecraftClient
import net.minecraft.client.texture.Sprite
import net.minecraft.resource.ResourceManager
import net.minecraft.resource.ResourceType
import net.minecraft.screen.PlayerScreenHandler
import net.minecraft.util.Identifier

enum class FluidType(val stillId: Identifier, val flowId: Identifier) {
    LAVA(identifier("block/molten_netherite_still"), identifier("block/molten_netherite_flow")),
    WATER(Identifier("block/water_still"), Identifier("block/water_flow"));

    var sprites = arrayOfNulls<Sprite>(2)

    fun registerReloadListener() {
        ClientSpriteRegistryCallback.event(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE)
            .register(ClientSpriteRegistryCallback { _, registry ->
                registry.register(stillId)
                registry.register(flowId)
            })
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES)
            .registerReloadListener(object : SimpleSynchronousResourceReloadListener {
                override fun apply(manager: ResourceManager?) {
                    val atlas = MinecraftClient.getInstance().getSpriteAtlas(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE)
                    sprites[0] = atlas.apply(stillId)
                    sprites[1] = atlas.apply(flowId)
                }

                override fun getFabricId(): Identifier =
                    identifier("${this@FluidType.name.toLowerCase()}_reload_listener")
            })
    }
}