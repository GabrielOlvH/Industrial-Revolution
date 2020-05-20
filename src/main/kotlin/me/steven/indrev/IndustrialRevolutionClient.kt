package me.steven.indrev

import me.steven.indrev.gui.CoalGeneratorController
import me.steven.indrev.gui.CoalGeneratorScreen
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.screen.ScreenProviderRegistry
import net.minecraft.container.BlockContext
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.Identifier
import net.minecraft.util.PacketByteBuf

class IndustrialRevolutionClient : ClientModInitializer {
    override fun onInitializeClient() {
        ScreenProviderRegistry.INSTANCE.registerFactory(IndustrialRevolution.COAL_GENERATOR_SCREEN_ID
        ) { syncId: Int, _: Identifier?, player: PlayerEntity, buf: PacketByteBuf ->
            CoalGeneratorScreen(
                CoalGeneratorController(
                    syncId,
                    player.inventory,
                    BlockContext.create(player.world, buf.readBlockPos())
                ),
                player
            )
        }
    }
}