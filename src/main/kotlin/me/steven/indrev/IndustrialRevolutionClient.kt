package me.steven.indrev

import me.steven.indrev.blocks.cables.CableBlockEntityRenderer
import me.steven.indrev.content.MachineRegistry
import me.steven.indrev.gui.furnace.ElectricFurnaceController
import me.steven.indrev.gui.furnace.ElectricFurnaceScreen
import me.steven.indrev.gui.generators.CoalGeneratorController
import me.steven.indrev.gui.generators.CoalGeneratorScreen
import me.steven.indrev.gui.pulverizer.PulverizerController
import me.steven.indrev.gui.pulverizer.PulverizerScreen
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.rendereregistry.v1.BlockEntityRendererRegistry
import net.fabricmc.fabric.api.client.screen.ScreenProviderRegistry
import net.minecraft.container.BlockContext
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.Identifier
import net.minecraft.util.PacketByteBuf

class IndustrialRevolutionClient : ClientModInitializer {
    override fun onInitializeClient() {
        ScreenProviderRegistry.INSTANCE.registerFactory(CoalGeneratorScreen.SCREEN_ID
        ) { syncId: Int, _: Identifier?, player: PlayerEntity, buf: PacketByteBuf ->
            CoalGeneratorScreen(
                    CoalGeneratorController(syncId, player.inventory, BlockContext.create(player.world, buf.readBlockPos())), player
            )
        }

        ScreenProviderRegistry.INSTANCE.registerFactory(ElectricFurnaceScreen.SCREEN_ID
        ) { syncId: Int, _: Identifier?, player: PlayerEntity, buf: PacketByteBuf ->
            ElectricFurnaceScreen(
                    ElectricFurnaceController(syncId, player.inventory, BlockContext.create(player.world, buf.readBlockPos())), player
            )
        }

        ScreenProviderRegistry.INSTANCE.registerFactory(PulverizerScreen.SCREEN_ID
        ) { syncId: Int, _: Identifier?, player: PlayerEntity, buf: PacketByteBuf ->
            PulverizerScreen(
                    PulverizerController(syncId, player.inventory, BlockContext.create(player.world, buf.readBlockPos())), player
            )
        }
        BlockEntityRendererRegistry.INSTANCE.register(MachineRegistry.CABLE_BLOCK_ENTITY) { CableBlockEntityRenderer(it) }
    }
}