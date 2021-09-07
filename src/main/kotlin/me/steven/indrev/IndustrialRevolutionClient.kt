package me.steven.indrev

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import me.steven.indrev.armor.ModuleFeatureRenderer
import me.steven.indrev.armor.ReinforcedElytraFeatureRenderer
import me.steven.indrev.blockentities.GlobalStateController
import me.steven.indrev.blockentities.MultiblockBlockEntityRenderer
import me.steven.indrev.blockentities.crafters.CondenserBlockEntityRenderer
import me.steven.indrev.blockentities.crafters.FluidInfuserBlockEntityRenderer
import me.steven.indrev.blockentities.drill.DrillBlockEntityRenderer
import me.steven.indrev.blockentities.farms.*
import me.steven.indrev.blockentities.generators.HeatGeneratorBlockEntityRenderer
import me.steven.indrev.blockentities.laser.CapsuleBlockEntityRenderer
import me.steven.indrev.blockentities.laser.LaserBlockEntityRenderer
import me.steven.indrev.blockentities.modularworkbench.ModularWorkbenchBlockEntityRenderer
import me.steven.indrev.blockentities.solarpowerplant.HeliostatBlockEntityRenderer
import me.steven.indrev.blockentities.storage.ChargePadBlockEntityRenderer
import me.steven.indrev.blockentities.storage.LazuliFluxContainerBlockEntityRenderer
import me.steven.indrev.blockentities.storage.TankBlockEntityRenderer
import me.steven.indrev.config.IRConfig
import me.steven.indrev.fluids.FluidType
import me.steven.indrev.gui.IRInventoryScreen
import me.steven.indrev.gui.IRModularControllerScreen
import me.steven.indrev.gui.screenhandlers.*
import me.steven.indrev.gui.screenhandlers.modular.ModularItemConfigurationScreenHandler
import me.steven.indrev.gui.screenhandlers.pipes.PipeFilterScreen
import me.steven.indrev.networks.Network
import me.steven.indrev.networks.client.ClientNetworkState
import me.steven.indrev.packets.PacketRegistry
import me.steven.indrev.registry.*
import me.steven.indrev.tools.modular.IRModularItem
import me.steven.indrev.utils.IRWorldRenderer
import me.steven.indrev.utils.identifier
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.`object`.builder.v1.client.model.FabricModelPredicateProviderRegistry
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry
import net.fabricmc.fabric.api.client.rendereregistry.v1.BlockEntityRendererRegistry
import net.fabricmc.fabric.api.client.rendereregistry.v1.LivingEntityFeatureRendererRegistrationCallback
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback
import net.minecraft.client.MinecraftClient
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.particle.FlameParticle
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.entity.LivingEntityRenderer
import net.minecraft.client.render.entity.model.BipedEntityModel
import net.minecraft.client.render.entity.model.EntityModelLayers
import net.minecraft.client.util.InputUtil
import net.minecraft.entity.LivingEntity
import net.minecraft.screen.PlayerScreenHandler
import org.lwjgl.glfw.GLFW

@Suppress("UNCHECKED_CAST")
object IndustrialRevolutionClient : ClientModInitializer {
    override fun onInitializeClient() {
        FluidType.WATER.registerReloadListener()
        FluidType.LAVA.registerReloadListener()
        FluidType.GAS.registerReloadListener()
        arrayOf(
            IRFluidRegistry.COOLANT_STILL,
            IRFluidRegistry.SULFURIC_ACID_STILL,
            IRFluidRegistry.TOXIC_MUD_STILL,
            IRFluidRegistry.STEAM_STILL
        ).forEach { it.registerRender(FluidType.WATER) }
        arrayOf(
            IRFluidRegistry.MOLTEN_NETHERITE_STILL,
            IRFluidRegistry.MOLTEN_IRON_STILL,
            IRFluidRegistry.MOLTEN_GOLD_STILL,
            IRFluidRegistry.MOLTEN_COPPER_STILL,
            IRFluidRegistry.MOLTEN_TIN_STILL,
            IRFluidRegistry.MOLTEN_LEAD_STILL,
            IRFluidRegistry.MOLTEN_SILVER_STILL,
            IRFluidRegistry.MOLTEN_SALT_STILL
        ).forEach { it.registerRender(FluidType.LAVA) }
        arrayOf(
            IRFluidRegistry.HYDROGEN_STILL,
            IRFluidRegistry.OXYGEN_STILL,
            IRFluidRegistry.METHANE_STILL,
        ).forEach { it.registerRender(FluidType.GAS) }
        IRHudRender
        arrayOf(
            COAL_GENERATOR_HANDLER,
            SOLAR_GENERATOR_HANDLER,
            BIOMASS_GENERATOR_HANDLER,
            HEAT_GENERATOR_HANDLER,
            GAS_BURNING_GENERATOR_HANDLER,
            BATTERY_HANDLER,
            ELECTRIC_FURNACE_HANDLER,
            PULVERIZER_HANDLER,
            COMPRESSOR_HANDLER,
            SOLID_INFUSER_HANDLER,
            CHOPPER_HANDLER,
            RANCHER_HANDLER,
            MINING_RIG_HANDLER,
            MODULAR_WORKBENCH_HANDLER,
            FISHER_HANDLER,
            SCREWDRIVER_HANDLER,
            SMELTER_HANDLER,
            CONDENSER_HANDLER,
            FLUID_INFUSER_HANDLER,
            FARMER_HANDLER,
            SLAUGHTER_HANDLER,
            RESOURCE_REPORT_HANDLER,
            SAWMILL_HANDLER,
            ELECTRIC_FURNACE_FACTORY_HANDLER,
            PULVERIZER_FACTORY_HANDLER,
            COMPRESSOR_FACTORY_HANDLER,
            SOLID_INFUSER_FACTORY_HANDLER,
            CABINET_HANDLER,
            DRILL_HANDLER,
            LASER_HANDLER,
            ELECTROLYTIC_SEPARATOR_HANDLER,
            STEAM_TURBINE_HANDLER,
            SOLAR_POWER_PLANT_TOWER_HANDLER,
            SOLAR_POWER_PLANT_SMELTER_HANDLER,
            DISTILLER_HANDLER,
            BOILER_HANDLER
        ).forEach { handler ->
            ScreenRegistry.register(handler) { controller, inv, _ -> IRInventoryScreen(controller, inv.player) }
        }
        ScreenRegistry.register(PIPE_FILTER_HANDLER) { controller, inv, _ -> PipeFilterScreen(controller, inv.player) }

        MachineRegistry.CHOPPER_REGISTRY.registerBlockEntityRenderer(::ChopperBlockEntityRenderer)
        MachineRegistry.RANCHER_REGISTRY.registerBlockEntityRenderer(::AOEMachineBlockEntityRenderer)
        MachineRegistry.FARMER_REGISTRY.registerBlockEntityRenderer(::AOEMachineBlockEntityRenderer)
        MachineRegistry.SLAUGHTER_REGISTRY.registerBlockEntityRenderer(::AOEMachineBlockEntityRenderer)
        MachineRegistry.MODULAR_WORKBENCH_REGISTRY.registerBlockEntityRenderer(::ModularWorkbenchBlockEntityRenderer)
        MachineRegistry.CHARGE_PAD_REGISTRY.registerBlockEntityRenderer(::ChargePadBlockEntityRenderer)
        MachineRegistry.CONDENSER_REGISTRY.registerBlockEntityRenderer(::CondenserBlockEntityRenderer)
        MachineRegistry.FLUID_INFUSER_REGISTRY.registerBlockEntityRenderer(::FluidInfuserBlockEntityRenderer)
        MachineRegistry.SOLID_INFUSER_FACTORY_REGISTRY.registerBlockEntityRenderer(::MultiblockBlockEntityRenderer)
        MachineRegistry.COMPRESSOR_FACTORY_REGISTRY.registerBlockEntityRenderer(::MultiblockBlockEntityRenderer)
        MachineRegistry.PULVERIZER_FACTORY_REGISTRY.registerBlockEntityRenderer(::MultiblockBlockEntityRenderer)
        MachineRegistry.ELECTRIC_FURNACE_FACTORY_REGISTRY.registerBlockEntityRenderer(::MultiblockBlockEntityRenderer)
        MachineRegistry.MINING_RIG_REGISTRY.registerBlockEntityRenderer(::MiningRigBlockEntityRenderer)
        MachineRegistry.PUMP_REGISTRY.registerBlockEntityRenderer(::PumpBlockEntityRenderer)
        MachineRegistry.LAZULI_FLUX_CONTAINER_REGISTRY.registerBlockEntityRenderer(::LazuliFluxContainerBlockEntityRenderer)
        MachineRegistry.HEAT_GENERATOR_REGISTRY.registerBlockEntityRenderer(::HeatGeneratorBlockEntityRenderer)
        MachineRegistry.LASER_EMITTER_REGISTRY.registerBlockEntityRenderer(::LaserBlockEntityRenderer)
        MachineRegistry.STEAM_TURBINE_REGISTRY.registerBlockEntityRenderer(::MultiblockBlockEntityRenderer)
        BlockEntityRendererRegistry.INSTANCE.register(IRBlockRegistry.TANK_BLOCK_ENTITY) { TankBlockEntityRenderer() }
        BlockEntityRendererRegistry.INSTANCE.register(IRBlockRegistry.DRILL_BLOCK_ENTITY_TYPE) { DrillBlockEntityRenderer() }
        BlockEntityRendererRegistry.INSTANCE.register(IRBlockRegistry.CAPSULE_BLOCK_ENTITY) { CapsuleBlockEntityRenderer() }
        BlockEntityRendererRegistry.INSTANCE.register(IRBlockRegistry.BIOMASS_COMPOSTER_BLOCK_ENTITY) { BiomassComposterBlockEntityRenderer() }
        BlockEntityRendererRegistry.INSTANCE.register(IRBlockRegistry.SOLAR_POWER_PLANT_TOWER_BLOCK_ENTITY) { MultiblockBlockEntityRenderer() }
        BlockEntityRendererRegistry.INSTANCE.register(IRBlockRegistry.HELIOSTAT_BLOCK_ENTITY) { HeliostatBlockEntityRenderer() }
        BlockEntityRendererRegistry.INSTANCE.register(IRBlockRegistry.BOILER_BLOCK_ENTITY) { MultiblockBlockEntityRenderer() }


        MachineRegistry.MODULAR_WORKBENCH_REGISTRY.setRenderLayer(RenderLayer.getTranslucent())
        MachineRegistry.PUMP_REGISTRY.setRenderLayer(RenderLayer.getTranslucent())
        MachineRegistry.HEAT_GENERATOR_REGISTRY.setRenderLayer(RenderLayer.getCutout())
        BlockRenderLayerMap.INSTANCE.putBlock(IRBlockRegistry.TANK_BLOCK, RenderLayer.getCutout())
        BlockRenderLayerMap.INSTANCE.putBlock(IRBlockRegistry.SULFUR_CRYSTAL_CLUSTER, RenderLayer.getTranslucent())
        BlockRenderLayerMap.INSTANCE.putBlock(IRBlockRegistry.DRILL_TOP, RenderLayer.getCutout())
        BlockRenderLayerMap.INSTANCE.putBlock(IRBlockRegistry.DRILL_MIDDLE, RenderLayer.getCutout())
        BlockRenderLayerMap.INSTANCE.putBlock(IRBlockRegistry.DRILL_BOTTOM, RenderLayer.getCutout())
        BlockRenderLayerMap.INSTANCE.putBlock(IRBlockRegistry.CAPSULE_BLOCK, RenderLayer.getCutout())

        BlockRenderLayerMap.INSTANCE.putBlock(IRBlockRegistry.CABLE_MK1, RenderLayer.getCutout())
        BlockRenderLayerMap.INSTANCE.putBlock(IRBlockRegistry.CABLE_MK2, RenderLayer.getCutout())
        BlockRenderLayerMap.INSTANCE.putBlock(IRBlockRegistry.CABLE_MK3, RenderLayer.getCutout())
        BlockRenderLayerMap.INSTANCE.putBlock(IRBlockRegistry.CABLE_MK4, RenderLayer.getCutout())

        ModelLoadingRegistry.INSTANCE.registerModelProvider(IRModelManagers)
        ModelLoadingRegistry.INSTANCE.registerVariantProvider { IRModelManagers }

        FabricModelPredicateProviderRegistry.register(
            IRItemRegistry.GAMER_AXE_ITEM,
            identifier("activate")
        ) { stack, _, _, _ -> stack?.orCreateNbt?.getFloat("Progress") ?: 0f }

        PacketRegistry.registerClient()

        GlobalStateController.initClient()

        WorldRenderEvents.BEFORE_BLOCK_OUTLINE.register(IRWorldRenderer)

        ClientTickEvents.END_CLIENT_TICK.register { client ->
            while (MODULAR_CONTROLLER_KEYBINDING.wasPressed()) {
                val playerInventory = MinecraftClient.getInstance().player?.inventory ?: break
                val hasModularItem = (0 until playerInventory.size())
                    .associateWith { slot -> playerInventory.getStack(slot) }
                    .filter { (_, stack) -> stack.item is IRModularItem<*> }
                    .isNotEmpty()
                if (hasModularItem)
                    MinecraftClient.getInstance()
                        .setScreen(IRModularControllerScreen(ModularItemConfigurationScreenHandler(client.player!!.inventory)))
            }
        }

        ClientSpriteRegistryCallback.event(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE).register(
            ClientSpriteRegistryCallback { _, registry ->
                registry.register(identifier("block/lazuli_flux_container_lf_level"))
                registry.register(identifier("gui/hud_damaged"))
                registry.register(identifier("gui/hud_regenerating"))
                registry.register(identifier("gui/hud_warning"))
                registry.register(identifier("gui/hud_default"))
                registry.register(identifier("particle/laser_particle_1"))
                registry.register(identifier("particle/laser_particle_2"))
                registry.register(identifier("particle/laser_particle_3"))
            })

        ParticleFactoryRegistry.getInstance().register(IndustrialRevolution.LASER_PARTICLE) { spriteProvider ->
            FlameParticle.Factory(spriteProvider)
        }

        ClientPlayConnectionEvents.DISCONNECT.register { _, _ ->
            IRConfig.readConfigs()
        }

        LivingEntityFeatureRendererRegistrationCallback.EVENT.register(LivingEntityFeatureRendererRegistrationCallback { _, renderer, helper, ctx ->
            val slim = false
            helper.register(
                ModuleFeatureRenderer(
                    renderer as LivingEntityRenderer<LivingEntity, BipedEntityModel<LivingEntity>>,
                    BipedEntityModel(ctx.getPart(if (slim) EntityModelLayers.PLAYER_SLIM_INNER_ARMOR else EntityModelLayers.PLAYER_INNER_ARMOR)),
                    BipedEntityModel(ctx.getPart(if (slim) EntityModelLayers.PLAYER_SLIM_OUTER_ARMOR else EntityModelLayers.PLAYER_OUTER_ARMOR))
                )
            )

            helper.register(ReinforcedElytraFeatureRenderer(renderer, ctx.modelLoader))
        })

        WorldRenderEvents.BEFORE_ENTITIES.register(MatterProjectorPreviewRenderer)

        AprilFools.init()
    }

    private val MODULAR_CONTROLLER_KEYBINDING: KeyBinding = KeyBindingHelper.registerKeyBinding(
        KeyBinding(
            "key.indrev.modular",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_UNKNOWN,
            "category.indrev"
        )
    )

    val CLIENT_NETWORK_STATE = Object2ObjectOpenHashMap<Network.Type<*>, ClientNetworkState<*>>()
}
