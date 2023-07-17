package me.steven.indrev

import me.steven.indrev.blocks.MACHINES
import me.steven.indrev.events.client.MachineModelLoader
import me.steven.indrev.packets.client.ClientPackets
import me.steven.indrev.screens.MACHINE_SCREEN_HANDLER
import me.steven.indrev.screens.machine.MachineHandledScreen
import me.steven.indrev.api.Tier
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry
import net.minecraft.client.gui.screen.ingame.HandledScreens
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories
import net.minecraft.screen.PlayerScreenHandler

fun initClient() {
    HandledScreens.register(MACHINE_SCREEN_HANDLER, ::MachineHandledScreen)
    ClientPackets.register()

    ModelLoadingRegistry.INSTANCE.registerVariantProvider { MachineModelLoader }
    MACHINES.forEach { (_, machine) ->
        BlockEntityRendererFactories.register(machine.type) { ctx -> machine.blockEntityRenderer(ctx) }
    }

   /* ClientSpriteRegistryCallback.event(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE).register { _, registry ->
        Tier.values().forEach { t -> registry.register(t.getOverlaySprite().textureId) }
    }*/
}