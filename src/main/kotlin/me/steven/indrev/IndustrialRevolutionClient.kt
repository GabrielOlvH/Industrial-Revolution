package me.steven.indrev

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import me.steven.indrev.api.OreDataCards
import me.steven.indrev.blockentities.GlobalStateController
import me.steven.indrev.blockentities.crafters.*
import me.steven.indrev.blockentities.miningrig.DrillBlockEntityRenderer
import me.steven.indrev.blockentities.farms.*
import me.steven.indrev.blockentities.generators.HeatGeneratorBlockEntityRenderer
import me.steven.indrev.blockentities.generators.SteamTurbineBlockEntity
import me.steven.indrev.blockentities.laser.CapsuleBlockEntityRenderer
import me.steven.indrev.blockentities.laser.LaserBlockEntityRenderer
import me.steven.indrev.blockentities.miningrig.MiningRigBlockEntityRenderer
import me.steven.indrev.blockentities.modularworkbench.ModularWorkbenchBlockEntityRenderer
import me.steven.indrev.blockentities.solarpowerplant.HeliostatBlockEntityRenderer
import me.steven.indrev.blockentities.storage.ChargePadBlockEntityRenderer
import me.steven.indrev.blockentities.storage.LazuliFluxContainerBlockEntityRenderer
import me.steven.indrev.blockentities.storage.TankBlockEntityRenderer
import me.steven.indrev.components.multiblock.MultiblockBlockEntityRenderer
import me.steven.indrev.config.IRConfig
import me.steven.indrev.events.client.*
import me.steven.indrev.fluids.FluidType
import me.steven.indrev.gui.IRInventoryScreen
import me.steven.indrev.gui.screenhandlers.*
import me.steven.indrev.gui.screenhandlers.pipes.PipeFilterScreen
import me.steven.indrev.networks.Network
import me.steven.indrev.networks.client.ClientNetworkState
import me.steven.indrev.packets.PacketRegistry
import me.steven.indrev.registry.*
import me.steven.indrev.events.client.IRWorldRenderer
import me.steven.indrev.utils.identifier
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.`object`.builder.v1.client.model.FabricModelPredicateProviderRegistry
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback
import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.particle.FlameParticle
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.util.InputUtil
import net.minecraft.item.ElytraItem
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
            IRFluidRegistry.MOLTEN_SILVER_STILL
        ).forEach { it.registerRender(FluidType.LAVA) }
        arrayOf(
            IRFluidRegistry.HYDROGEN_STILL,
            IRFluidRegistry.OXYGEN_STILL,
            IRFluidRegistry.METHANE_STILL,
        ).forEach { it.registerRender(FluidType.GAS) }
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
            RECYCLER_HANDLER,
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
            DATA_CARD_WRITER_HANDLER
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
        MachineRegistry.SOLID_INFUSER_FACTORY_REGISTRY.registerBlockEntityRenderer { MultiblockBlockEntityRenderer<SolidInfuserFactoryBlockEntity> { be -> be.multiblockComponent!! } }
        MachineRegistry.COMPRESSOR_FACTORY_REGISTRY.registerBlockEntityRenderer { MultiblockBlockEntityRenderer<CompressorFactoryBlockEntity> { be -> be.multiblockComponent!! } }
        MachineRegistry.PULVERIZER_FACTORY_REGISTRY.registerBlockEntityRenderer { MultiblockBlockEntityRenderer<PulverizerFactoryBlockEntity> { be -> be.multiblockComponent!! } }
        MachineRegistry.ELECTRIC_FURNACE_FACTORY_REGISTRY.registerBlockEntityRenderer { MultiblockBlockEntityRenderer<ElectricFurnaceFactoryBlockEntity> { be -> be.multiblockComponent!! } }
        MachineRegistry.MINING_RIG_REGISTRY.registerBlockEntityRenderer(::MiningRigBlockEntityRenderer)
        MachineRegistry.PUMP_REGISTRY.registerBlockEntityRenderer(::PumpBlockEntityRenderer)
        MachineRegistry.LAZULI_FLUX_CONTAINER_REGISTRY.registerBlockEntityRenderer(::LazuliFluxContainerBlockEntityRenderer)
        MachineRegistry.HEAT_GENERATOR_REGISTRY.registerBlockEntityRenderer(::HeatGeneratorBlockEntityRenderer)
        MachineRegistry.LASER_EMITTER_REGISTRY.registerBlockEntityRenderer(::LaserBlockEntityRenderer)
        MachineRegistry.STEAM_TURBINE_REGISTRY.registerBlockEntityRenderer { MultiblockBlockEntityRenderer<SteamTurbineBlockEntity> { be -> be.multiblockComponent!! } }
        BlockEntityRendererRegistry.register(IRBlockRegistry.TANK_BLOCK_ENTITY) { TankBlockEntityRenderer() }
        BlockEntityRendererRegistry.register(IRBlockRegistry.DRILL_BLOCK_ENTITY_TYPE) { DrillBlockEntityRenderer() }
        BlockEntityRendererRegistry.register(IRBlockRegistry.CAPSULE_BLOCK_ENTITY) { CapsuleBlockEntityRenderer() }
        BlockEntityRendererRegistry.register(IRBlockRegistry.BIOMASS_COMPOSTER_BLOCK_ENTITY) { BiomassComposterBlockEntityRenderer() }
        BlockEntityRendererRegistry.register(IRBlockRegistry.SOLAR_POWER_PLANT_TOWER_BLOCK_ENTITY) { MultiblockBlockEntityRenderer { be -> be.multiblockComponent } }
        BlockEntityRendererRegistry.register(IRBlockRegistry.HELIOSTAT_BLOCK_ENTITY) { HeliostatBlockEntityRenderer() }

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

        FabricModelPredicateProviderRegistry.register(IRItemRegistry.REINFORCED_ELYTRA, identifier("broken")) { stack, _, _, _ ->
            if (ElytraItem.isUsable(stack)) 0.0f else 1.0f
        }

        FabricModelPredicateProviderRegistry.register(IRItemRegistry.ORE_DATA_CARD, identifier("empty")) { stack, _, _, _ ->
            if (OreDataCards.readNbt(stack) == null) 0.0f else 1.0f
        }

        PacketRegistry.registerClient()

        GlobalStateController.initClient()


        HudRenderCallback.EVENT.register(IRHudRenderCallback)
        WorldRenderEvents.BEFORE_BLOCK_OUTLINE.register(IRWorldRenderer)
        WorldRenderEvents.BEFORE_ENTITIES.register(MatterProjectorPreviewRenderer)

        ClientTickEvents.END_CLIENT_TICK.register(IRClientTickEvents)

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

        LivingEntityFeatureRendererRegistrationCallback.EVENT.register(IRLivingEntityFeatureRendererCallback)
        TooltipComponentCallback.EVENT.register(IRTooltipComponentsCallback)
        ItemTooltipCallback.EVENT.register(MiningRigInfoTooltipCallback)

        AprilFools.init()
    }

    val MODULAR_CONTROLLER_KEYBINDING: KeyBinding = KeyBindingHelper.registerKeyBinding(
        KeyBinding(
            "key.indrev.modular",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_K,
            "category.indrev"
        )
    )

    val GAMER_AXE_TOGGLE_KEYBINDING: KeyBinding = KeyBindingHelper.registerKeyBinding(
        KeyBinding(
            "key.indrev.gamer_axe_toggle",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_G,
            "category.indrev"
        )
    )

    val CLIENT_NETWORK_STATE = Object2ObjectOpenHashMap<Network.Type<*>, ClientNetworkState<*>>()
}
