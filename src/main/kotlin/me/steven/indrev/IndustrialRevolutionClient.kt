package me.steven.indrev

import me.steven.indrev.blockentities.cables.CableBlockEntity
import me.steven.indrev.blockentities.cables.CableBlockEntityRenderer
import me.steven.indrev.gui.battery.BatteryController
import me.steven.indrev.gui.battery.BatteryScreen
import me.steven.indrev.gui.coalgenerator.CoalGeneratorController
import me.steven.indrev.gui.coalgenerator.CoalGeneratorScreen
import me.steven.indrev.gui.compressor.CompressorController
import me.steven.indrev.gui.compressor.CompressorScreen
import me.steven.indrev.gui.furnace.ElectricFurnaceController
import me.steven.indrev.gui.furnace.ElectricFurnaceScreen
import me.steven.indrev.gui.infuser.InfuserController
import me.steven.indrev.gui.infuser.InfuserScreen
import me.steven.indrev.gui.miner.MinerController
import me.steven.indrev.gui.miner.MinerScreen
import me.steven.indrev.gui.nuclearreactor.NuclearReactorController
import me.steven.indrev.gui.nuclearreactor.NuclearReactorScreen
import me.steven.indrev.gui.pulverizer.PulverizerController
import me.steven.indrev.gui.pulverizer.PulverizerScreen
import me.steven.indrev.gui.recycler.RecyclerController
import me.steven.indrev.gui.recycler.RecyclerScreen
import me.steven.indrev.gui.solargenerator.SolarGeneratorController
import me.steven.indrev.gui.solargenerator.SolarGeneratorScreen
import me.steven.indrev.registry.MachineRegistry
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.rendereregistry.v1.BlockEntityRendererRegistry
import net.fabricmc.fabric.api.client.screen.ScreenProviderRegistry
import net.minecraft.block.entity.BlockEntityType
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

        ScreenProviderRegistry.INSTANCE.registerFactory(SolarGeneratorScreen.SCREEN_ID
        ) { syncId: Int, _: Identifier?, player: PlayerEntity, buf: PacketByteBuf ->
            SolarGeneratorScreen(
                SolarGeneratorController(syncId, player.inventory, BlockContext.create(player.world, buf.readBlockPos())), player
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

        ScreenProviderRegistry.INSTANCE.registerFactory(
            CompressorScreen.SCREEN_ID
        ) { syncId: Int, _: Identifier?, player: PlayerEntity, buf: PacketByteBuf ->
            CompressorScreen(
                CompressorController(syncId, player.inventory, BlockContext.create(player.world, buf.readBlockPos())),
                player
            )
        }

        ScreenProviderRegistry.INSTANCE.registerFactory(
            BatteryScreen.SCREEN_ID
        ) { syncId: Int, _: Identifier?, player: PlayerEntity, buf: PacketByteBuf ->
            BatteryScreen(
                BatteryController(syncId, player.inventory, BlockContext.create(player.world, buf.readBlockPos())),
                player
            )
        }

        ScreenProviderRegistry.INSTANCE.registerFactory(
            InfuserScreen.SCREEN_ID
        ) { syncId: Int, _: Identifier?, player: PlayerEntity, buf: PacketByteBuf ->
            InfuserScreen(
                InfuserController(syncId, player.inventory, BlockContext.create(player.world, buf.readBlockPos())),
                player
            )
        }

        ScreenProviderRegistry.INSTANCE.registerFactory(
            MinerScreen.SCREEN_ID
        ) { syncId: Int, _: Identifier?, player: PlayerEntity, buf: PacketByteBuf ->
            MinerScreen(
                MinerController(syncId, player.inventory, BlockContext.create(player.world, buf.readBlockPos())),
                player
            )
        }

        ScreenProviderRegistry.INSTANCE.registerFactory(
            NuclearReactorScreen.SCREEN_ID
        ) { syncId: Int, _: Identifier?, player: PlayerEntity, buf: PacketByteBuf ->
            NuclearReactorScreen(
                NuclearReactorController(syncId, player.inventory, BlockContext.create(player.world, buf.readBlockPos())),
                player
            )
        }

        ScreenProviderRegistry.INSTANCE.registerFactory(
            RecyclerScreen.SCREEN_ID
        ) { syncId: Int, _: Identifier?, player: PlayerEntity, buf: PacketByteBuf ->
            RecyclerScreen(
                RecyclerController(syncId, player.inventory, BlockContext.create(player.world, buf.readBlockPos())),
                player
            )
        }

        MachineRegistry.CABLE_REGISTRY.forEach { _, blockEntity ->
            BlockEntityRendererRegistry.INSTANCE.register(blockEntity as BlockEntityType<CableBlockEntity>) {
                CableBlockEntityRenderer(it)
            }
        }
    }
}