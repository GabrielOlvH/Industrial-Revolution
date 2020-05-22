package me.steven.indrev

import me.steven.indrev.blocks.crafters.ElectricCraftingBlock
import me.steven.indrev.blocks.generators.GeneratorBlock
import me.steven.indrev.gui.furnace.ElectricFurnaceController
import me.steven.indrev.gui.furnace.ElectricFurnaceScreen
import me.steven.indrev.gui.generators.CoalGeneratorController
import me.steven.indrev.gui.generators.CoalGeneratorScreen
import me.steven.indrev.gui.pulverizer.PulverizerController
import me.steven.indrev.gui.pulverizer.PulverizerScreen
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.screen.ScreenProviderRegistry
import net.minecraft.container.BlockContext
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.Identifier
import net.minecraft.util.PacketByteBuf

class IndustrialRevolutionClient : ClientModInitializer {
    override fun onInitializeClient() {
        ScreenProviderRegistry.INSTANCE.registerFactory(GeneratorBlock.COAL_GENERATOR_SCREEN_ID
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

        ScreenProviderRegistry.INSTANCE.registerFactory(ElectricCraftingBlock.ELECTRIC_FURNACE_SCREEN_ID
        ) { syncId: Int, _: Identifier?, player: PlayerEntity, buf: PacketByteBuf ->
            ElectricFurnaceScreen(
                    ElectricFurnaceController(
                            syncId,
                            player.inventory,
                            BlockContext.create(player.world, buf.readBlockPos())
                    ),
                    player
            )
        }

        ScreenProviderRegistry.INSTANCE.registerFactory(ElectricCraftingBlock.PULVERIZER_SCREEN_ID
        ) { syncId: Int, _: Identifier?, player: PlayerEntity, buf: PacketByteBuf ->
            PulverizerScreen(
                    PulverizerController(
                            syncId,
                            player.inventory,
                            BlockContext.create(player.world, buf.readBlockPos())
                    ),
                    player
            )
        }
    }
}