package me.steven.indrev

import me.steven.indrev.blockentities.cables.CableBlockEntity
import me.steven.indrev.blockentities.cables.CableBlockEntityRenderer
import me.steven.indrev.blockentities.crafters.CondenserBlockEntity
import me.steven.indrev.blockentities.crafters.CondenserBlockEntityRenderer
import me.steven.indrev.blockentities.crafters.FluidInfuserBlockEntity
import me.steven.indrev.blockentities.crafters.FluidInfuserBlockEntityRenderer
import me.steven.indrev.blockentities.farms.AOEMachineBlockEntity
import me.steven.indrev.blockentities.farms.AOEMachineBlockEntityRenderer
import me.steven.indrev.blockentities.modularworkbench.ModularWorkbenchBlockEntity
import me.steven.indrev.blockentities.modularworkbench.ModularWorkbenchBlockEntityRenderer
import me.steven.indrev.blockentities.storage.ChargePadBlockEntity
import me.steven.indrev.blockentities.storage.ChargePadBlockEntityRenderer
import me.steven.indrev.blockentities.storage.TankBlockEntityRenderer
import me.steven.indrev.fluids.FluidType
import me.steven.indrev.gui.IRInventoryScreen
import me.steven.indrev.items.misc.IRTankItemBakedModel
import me.steven.indrev.registry.IRHudRender
import me.steven.indrev.registry.IRRegistry
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.Tier
import me.steven.indrev.utils.identifier
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.`object`.builder.v1.client.model.FabricModelPredicateProviderRegistry
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry
import net.fabricmc.fabric.api.client.model.ModelVariantProvider
import net.fabricmc.fabric.api.client.rendereregistry.v1.BlockEntityRendererRegistry
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.model.ModelBakeSettings
import net.minecraft.client.render.model.ModelLoader
import net.minecraft.client.render.model.UnbakedModel
import net.minecraft.client.texture.Sprite
import net.minecraft.client.util.SpriteIdentifier
import net.minecraft.util.Identifier
import java.util.function.Function

@Suppress("UNCHECKED_CAST")
object IndustrialRevolutionClient : ClientModInitializer {
    override fun onInitializeClient() {
        FluidType.WATER.registerReloadListener()
        FluidType.LAVA.registerReloadListener()
        arrayOf(
            IRRegistry.COOLANT_STILL,
            IRRegistry.SULFURIC_ACID_STILL,
            IRRegistry.TOXIC_MUD_STILL
        ).forEach { it.registerRender(FluidType.WATER) }
        arrayOf(
            IRRegistry.MOLTEN_NETHERITE_STILL,
            IRRegistry.MOLTEN_IRON_STILL,
            IRRegistry.MOLTEN_GOLD_STILL,
            IRRegistry.MOLTEN_COPPER_STILL,
            IRRegistry.MOLTEN_TIN_STILL
        ).forEach { it.registerRender(FluidType.LAVA) }
        IRHudRender
        arrayOf(
            IndustrialRevolution.COAL_GENERATOR_HANDLER,
            IndustrialRevolution.SOLAR_GENERATOR_HANDLER,
            IndustrialRevolution.BIOMASS_GENERATOR_HANDLER,
            IndustrialRevolution.HEAT_GENERATOR_HANDLER,
            IndustrialRevolution.BATTERY_HANDLER,
            IndustrialRevolution.ELECTRIC_FURNACE_HANDLER,
            IndustrialRevolution.PULVERIZER_HANDLER,
            IndustrialRevolution.COMPRESSOR_HANDLER,
            IndustrialRevolution.INFUSER_HANDLER,
            IndustrialRevolution.RECYCLER_HANDLER,
            IndustrialRevolution.CHOPPER_HANDLER,
            IndustrialRevolution.RANCHER_HANDLER,
            IndustrialRevolution.MINER_HANDLER,
            IndustrialRevolution.MODULAR_WORKBENCH_HANDLER,
            IndustrialRevolution.FISHING_FARM_HANDLER,
            IndustrialRevolution.WRENCH_HANDLER,
            IndustrialRevolution.SMELTER_HANDLER,
            IndustrialRevolution.CONDENSER_HANDLER,
            IndustrialRevolution.FLUID_INFUSER_HANDLER
        ).forEach { handler ->
            ScreenRegistry.register(handler) { controller, inv, _ -> IRInventoryScreen(controller, inv.player) }
        }

        MachineRegistry.CABLE_REGISTRY.forEachBlockEntity { _, blockEntity ->
            BlockEntityRendererRegistry.INSTANCE.register(blockEntity as BlockEntityType<CableBlockEntity>, ::CableBlockEntityRenderer)
        }

        MachineRegistry.CHOPPER_REGISTRY.forEachBlockEntity { _, blockEntity ->
            BlockEntityRendererRegistry.INSTANCE.register(blockEntity as BlockEntityType<AOEMachineBlockEntity>, ::AOEMachineBlockEntityRenderer)
        }

        MachineRegistry.RANCHER_REGISTRY.forEachBlockEntity { _, blockEntity ->
            BlockEntityRendererRegistry.INSTANCE.register(blockEntity as BlockEntityType<AOEMachineBlockEntity>, ::AOEMachineBlockEntityRenderer)
        }

        MachineRegistry.MODULAR_WORKBENCH_REGISTRY.forEachBlockEntity { _, blockEntity ->
            BlockEntityRendererRegistry.INSTANCE.register(blockEntity as BlockEntityType<ModularWorkbenchBlockEntity>, ::ModularWorkbenchBlockEntityRenderer)
        }

        MachineRegistry.CHARGE_PAD_REGISTRY.forEachBlockEntity { _, blockEntity ->
            BlockEntityRendererRegistry.INSTANCE.register(blockEntity as BlockEntityType<ChargePadBlockEntity>, ::ChargePadBlockEntityRenderer)
        }

        MachineRegistry.CONDENSER_REGISTRY.forEachBlockEntity { _, blockEntity ->
            BlockEntityRendererRegistry.INSTANCE.register(blockEntity as BlockEntityType<CondenserBlockEntity>, ::CondenserBlockEntityRenderer)
        }

        MachineRegistry.FLUID_INFUSER_REGISTRY.forEachBlockEntity { _, blockEntity ->
            BlockEntityRendererRegistry.INSTANCE.register(blockEntity as BlockEntityType<FluidInfuserBlockEntity>, ::FluidInfuserBlockEntityRenderer)
        }

        BlockEntityRendererRegistry.INSTANCE.register(IRRegistry.TANK_BLOCK_ENTITY, ::TankBlockEntityRenderer)

        BlockRenderLayerMap.INSTANCE.putBlock(IRRegistry.AREA_INDICATOR, RenderLayer.getTranslucent())
        BlockRenderLayerMap.INSTANCE.putBlock(MachineRegistry.MODULAR_WORKBENCH_REGISTRY.block(Tier.MK4), RenderLayer.getTranslucent())
        BlockRenderLayerMap.INSTANCE.putBlock(IRRegistry.TANK_BLOCK, RenderLayer.getCutout())
        BlockRenderLayerMap.INSTANCE.putBlock(IRRegistry.SULFUR_CRYSTAL_CLUSTER, RenderLayer.getTranslucent())
        MachineRegistry.FISHING_FARM_REGISTRY.forEachBlock { _, block ->
            BlockRenderLayerMap.INSTANCE.putBlock(block, RenderLayer.getTranslucent())
        }

        val identifier = identifier("tank")
        ModelLoadingRegistry.INSTANCE.registerVariantProvider {
            ModelVariantProvider { modelIdentifier, _ ->
                if(modelIdentifier.namespace == identifier.namespace && modelIdentifier.path == identifier.path && modelIdentifier.variant == "inventory") {
                    return@ModelVariantProvider object : UnbakedModel {
                        override fun getModelDependencies(): MutableCollection<Identifier> = mutableListOf()
                        override fun bake(loader: ModelLoader, textureGetter: Function<SpriteIdentifier, Sprite>, rotationScreenHandler: ModelBakeSettings, modelId: Identifier) = IRTankItemBakedModel
                        override fun getTextureDependencies(
                            unbakedModelGetter: Function<Identifier, UnbakedModel>?,
                            unresolvedTextureReferences: MutableSet<com.mojang.datafixers.util.Pair<String, String>>?
                        ): MutableCollection<SpriteIdentifier> = mutableListOf()
                    }
                }
                return@ModelVariantProvider null
            }
        }

        FabricModelPredicateProviderRegistry.register(IRRegistry.GAMER_AXE_ITEM, identifier("activate")) predicate@{ stack, _, _ ->
            val tag = stack?.orCreateTag ?: return@predicate 0f
            return@predicate tag.getFloat("Progress")
        }
    }
}