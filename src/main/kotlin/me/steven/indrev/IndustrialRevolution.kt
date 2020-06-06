package me.steven.indrev

import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.blockentities.generators.NuclearReactorProxyBlockEntity
import me.steven.indrev.blocks.ProxyBlock
import me.steven.indrev.gui.battery.BatteryController
import me.steven.indrev.gui.battery.BatteryScreen
import me.steven.indrev.gui.biomassgen.BiomassGeneratorController
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
import me.steven.indrev.recipes.*
import me.steven.indrev.registry.ModRegistry
import me.steven.indrev.utils.EMPTY_ENERGY_STORAGE
import me.steven.indrev.utils.identifier
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder
import net.fabricmc.fabric.api.container.ContainerProviderRegistry
import net.minecraft.block.entity.BlockEntity
import net.minecraft.container.BlockContext
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemGroup
import net.minecraft.item.ItemStack
import net.minecraft.util.Identifier
import net.minecraft.util.PacketByteBuf
import net.minecraft.util.registry.Registry
import team.reborn.energy.Energy
import team.reborn.energy.EnergyStorage
import team.reborn.energy.minecraft.EnergyModInitializer

class IndustrialRevolution : EnergyModInitializer() {
    override fun onInitialize() {
        super.onInitialize()
        Energy.registerHolder(NuclearReactorProxyBlockEntity::class.java) { obj ->
            obj as BlockEntity
            if (obj.hasWorld()) {
                val block = obj.cachedState.block
                if (block is ProxyBlock && block is EnergyStorage)
                    return@registerHolder obj.world?.getBlockEntity(block.getBlockEntityPos(obj.cachedState, obj.pos)) as EnergyStorage
            }
            return@registerHolder EMPTY_ENERGY_STORAGE
        }
        Energy.registerHolder(MachineBlockEntity::class.java) { obj -> obj as MachineBlockEntity }
        ModRegistry().registerAll()

        ContainerProviderRegistry.INSTANCE.registerFactory(
            CoalGeneratorScreen.SCREEN_ID
        ) { syncId: Int, _: Identifier?, player: PlayerEntity, buf: PacketByteBuf ->
            CoalGeneratorController(syncId, player.inventory, BlockContext.create(player.world, buf.readBlockPos()))
        }

        ContainerProviderRegistry.INSTANCE.registerFactory(SolarGeneratorScreen.SCREEN_ID
        ) { syncId: Int, _: Identifier?, player: PlayerEntity, buf: PacketByteBuf ->
            SolarGeneratorController(syncId, player.inventory, BlockContext.create(player.world, buf.readBlockPos()))
        }

        ContainerProviderRegistry.INSTANCE.registerFactory(NuclearReactorScreen.SCREEN_ID
        ) { syncId: Int, _: Identifier?, player: PlayerEntity, buf: PacketByteBuf ->
            NuclearReactorController(syncId, player.inventory, BlockContext.create(player.world, buf.readBlockPos()))
        }

        ContainerProviderRegistry.INSTANCE.registerFactory(ElectricFurnaceScreen.SCREEN_ID
        ) { syncId: Int, _: Identifier?, player: PlayerEntity, buf: PacketByteBuf ->
            ElectricFurnaceController(syncId, player.inventory, BlockContext.create(player.world, buf.readBlockPos()))
        }

        ContainerProviderRegistry.INSTANCE.registerFactory(PulverizerScreen.SCREEN_ID
        ) { syncId: Int, _: Identifier?, player: PlayerEntity, buf: PacketByteBuf ->
            PulverizerController(syncId, player.inventory, BlockContext.create(player.world, buf.readBlockPos()))
        }

        ContainerProviderRegistry.INSTANCE.registerFactory(CompressorScreen.SCREEN_ID
        ) { syncId: Int, _: Identifier?, player: PlayerEntity, buf: PacketByteBuf ->
            CompressorController(syncId, player.inventory, BlockContext.create(player.world, buf.readBlockPos()))
        }

        ContainerProviderRegistry.INSTANCE.registerFactory(BatteryScreen.SCREEN_ID
        ) { syncId: Int, _: Identifier?, player: PlayerEntity, buf: PacketByteBuf ->
            BatteryController(syncId, player.inventory, BlockContext.create(player.world, buf.readBlockPos()))
        }

        ContainerProviderRegistry.INSTANCE.registerFactory(InfuserScreen.SCREEN_ID
        ) { syncId: Int, _: Identifier?, player: PlayerEntity, buf: PacketByteBuf ->
            InfuserController(syncId, player.inventory, BlockContext.create(player.world, buf.readBlockPos()))
        }

        ContainerProviderRegistry.INSTANCE.registerFactory(MinerScreen.SCREEN_ID
        ) { syncId: Int, _: Identifier?, player: PlayerEntity, buf: PacketByteBuf ->
            MinerController(syncId, player.inventory, BlockContext.create(player.world, buf.readBlockPos()))
        }

        ContainerProviderRegistry.INSTANCE.registerFactory(RecyclerScreen.SCREEN_ID
        ) { syncId: Int, _: Identifier?, player: PlayerEntity, buf: PacketByteBuf ->
            RecyclerController(syncId, player.inventory, BlockContext.create(player.world, buf.readBlockPos()))
        }

        ContainerProviderRegistry.INSTANCE.registerFactory(BiomassGeneratorController.SCREEN_ID
        ) { syncId: Int, _: Identifier?, player: PlayerEntity, buf: PacketByteBuf ->
            BiomassGeneratorController(syncId, player.inventory, BlockContext.create(player.world, buf.readBlockPos()))
        }

        Registry.register(Registry.RECIPE_SERIALIZER, PulverizerRecipe.IDENTIFIER, PulverizerRecipe.SERIALIZER)
        Registry.register(Registry.RECIPE_TYPE, PulverizerRecipe.IDENTIFIER, PulverizerRecipe.TYPE)
        Registry.register(Registry.RECIPE_SERIALIZER, CompressorRecipe.IDENTIFIER, CompressorRecipe.SERIALIZER)
        Registry.register(Registry.RECIPE_TYPE, CompressorRecipe.IDENTIFIER, CompressorRecipe.TYPE)
        Registry.register(Registry.RECIPE_SERIALIZER, RechargeableRecipe.IDENTIFIER, RechargeableRecipe.SERIALIZER)
        Registry.register(Registry.RECIPE_TYPE, InfuserRecipe.IDENTIFIER, InfuserRecipe.TYPE)
        Registry.register(Registry.RECIPE_SERIALIZER, InfuserRecipe.IDENTIFIER, InfuserRecipe.SERIALIZER)
        Registry.register(Registry.RECIPE_SERIALIZER, RecyclerRecipe.IDENTIFIER, RecyclerRecipe.SERIALIZER)
        Registry.register(Registry.RECIPE_TYPE, RecyclerRecipe.IDENTIFIER, RecyclerRecipe.TYPE)
    }

    companion object {
        const val MOD_ID = "indrev"

        val MOD_GROUP: ItemGroup =
            FabricItemGroupBuilder.build(identifier("indrev_group")) { ItemStack(ModRegistry.NIKOLITE.dust.get()) }
    }
}