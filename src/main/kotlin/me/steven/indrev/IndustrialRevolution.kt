package me.steven.indrev

import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.blockentities.generators.NuclearReactorProxyBlockEntity
import me.steven.indrev.blocks.ProxyBlock
import me.steven.indrev.gui.controllers.*
import me.steven.indrev.recipes.*
import me.steven.indrev.registry.ModRegistry
import me.steven.indrev.utils.EMPTY_ENERGY_STORAGE
import me.steven.indrev.utils.identifier
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder
import net.fabricmc.fabric.api.container.ContainerProviderRegistry
import net.minecraft.block.entity.BlockEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemGroup
import net.minecraft.item.ItemStack
import net.minecraft.network.PacketByteBuf
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.util.Identifier
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
            CoalGeneratorController.SCREEN_ID
        ) { syncId: Int, _: Identifier?, player: PlayerEntity, buf: PacketByteBuf ->
            CoalGeneratorController(syncId, player.inventory, ScreenHandlerContext.create(player.world, buf.readBlockPos()))
        }

        ContainerProviderRegistry.INSTANCE.registerFactory(SolarGeneratorController.SCREEN_ID
        ) { syncId: Int, _: Identifier?, player: PlayerEntity, buf: PacketByteBuf ->
            SolarGeneratorController(syncId, player.inventory, ScreenHandlerContext.create(player.world, buf.readBlockPos()))
        }

        ContainerProviderRegistry.INSTANCE.registerFactory(NuclearReactorController.SCREEN_ID
        ) { syncId: Int, _: Identifier?, player: PlayerEntity, buf: PacketByteBuf ->
            NuclearReactorController(syncId, player.inventory, ScreenHandlerContext.create(player.world, buf.readBlockPos()))
        }

        ContainerProviderRegistry.INSTANCE.registerFactory(ElectricFurnaceController.SCREEN_ID
        ) { syncId: Int, _: Identifier?, player: PlayerEntity, buf: PacketByteBuf ->
            ElectricFurnaceController(syncId, player.inventory, ScreenHandlerContext.create(player.world, buf.readBlockPos()))
        }

        ContainerProviderRegistry.INSTANCE.registerFactory(PulverizerController.SCREEN_ID
        ) { syncId: Int, _: Identifier?, player: PlayerEntity, buf: PacketByteBuf ->
            PulverizerController(syncId, player.inventory, ScreenHandlerContext.create(player.world, buf.readBlockPos()))
        }

        ContainerProviderRegistry.INSTANCE.registerFactory(CompressorController.SCREEN_ID
        ) { syncId: Int, _: Identifier?, player: PlayerEntity, buf: PacketByteBuf ->
            CompressorController(syncId, player.inventory, ScreenHandlerContext.create(player.world, buf.readBlockPos()))
        }

        ContainerProviderRegistry.INSTANCE.registerFactory(BatteryController.SCREEN_ID
        ) { syncId: Int, _: Identifier?, player: PlayerEntity, buf: PacketByteBuf ->
            BatteryController(syncId, player.inventory, ScreenHandlerContext.create(player.world, buf.readBlockPos()))
        }

        ContainerProviderRegistry.INSTANCE.registerFactory(InfuserController.SCREEN_ID
        ) { syncId: Int, _: Identifier?, player: PlayerEntity, buf: PacketByteBuf ->
            InfuserController(syncId, player.inventory, ScreenHandlerContext.create(player.world, buf.readBlockPos()))
        }

        ContainerProviderRegistry.INSTANCE.registerFactory(MinerController.SCREEN_ID
        ) { syncId: Int, _: Identifier?, player: PlayerEntity, buf: PacketByteBuf ->
            MinerController(syncId, player.inventory, ScreenHandlerContext.create(player.world, buf.readBlockPos()))
        }

        ContainerProviderRegistry.INSTANCE.registerFactory(RecyclerController.SCREEN_ID
        ) { syncId: Int, _: Identifier?, player: PlayerEntity, buf: PacketByteBuf ->
            RecyclerController(syncId, player.inventory, ScreenHandlerContext.create(player.world, buf.readBlockPos()))
        }

        ContainerProviderRegistry.INSTANCE.registerFactory(BiomassGeneratorController.SCREEN_ID
        ) { syncId: Int, _: Identifier?, player: PlayerEntity, buf: PacketByteBuf ->
            BiomassGeneratorController(syncId, player.inventory, ScreenHandlerContext.create(player.world, buf.readBlockPos()))
        }

        ContainerProviderRegistry.INSTANCE.registerFactory(ChopperController.SCREEN_ID
        ) { syncId: Int, _: Identifier?, player: PlayerEntity, buf: PacketByteBuf ->
            ChopperController(syncId, player.inventory, ScreenHandlerContext.create(player.world, buf.readBlockPos()))
        }

        ContainerProviderRegistry.INSTANCE.registerFactory(RancherController.SCREEN_ID
        ) { syncId: Int, _: Identifier?, player: PlayerEntity, buf: PacketByteBuf ->
            RancherController(syncId, player.inventory, ScreenHandlerContext.create(player.world, buf.readBlockPos()))
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