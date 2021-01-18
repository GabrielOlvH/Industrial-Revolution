package me.steven.indrev

import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap
import me.steven.indrev.blockentities.GlobalStateController
import me.steven.indrev.blockentities.MultiblockBlockEntityRenderer
import me.steven.indrev.blockentities.crafters.CondenserBlockEntityRenderer
import me.steven.indrev.blockentities.crafters.FluidInfuserBlockEntityRenderer
import me.steven.indrev.blockentities.drill.DrillBlockEntityRenderer
import me.steven.indrev.blockentities.farms.AOEMachineBlockEntityRenderer
import me.steven.indrev.blockentities.farms.MinerBlockEntityRenderer
import me.steven.indrev.blockentities.farms.PumpBlockEntityRenderer
import me.steven.indrev.blockentities.generators.HeatGeneratorBlockEntityRenderer
import me.steven.indrev.blockentities.modularworkbench.ModularWorkbenchBlockEntityRenderer
import me.steven.indrev.blockentities.storage.ChargePadBlockEntityRenderer
import me.steven.indrev.blockentities.storage.LazuliFluxContainerBlockEntityRenderer
import me.steven.indrev.blockentities.storage.TankBlockEntityRenderer
import me.steven.indrev.fluids.FluidType
import me.steven.indrev.gui.IRInventoryScreen
import me.steven.indrev.gui.IRModularControllerScreen
import me.steven.indrev.gui.controllers.modular.ModularController
import me.steven.indrev.registry.*
import me.steven.indrev.tools.modular.IRModularItem
import me.steven.indrev.utils.identifier
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.`object`.builder.v1.client.model.FabricModelPredicateProviderRegistry
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry
import net.fabricmc.fabric.api.client.rendereregistry.v1.BlockEntityRendererRegistry
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback
import net.minecraft.client.MinecraftClient
import net.minecraft.client.options.KeyBinding
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.util.InputUtil
import net.minecraft.screen.PlayerScreenHandler
import net.minecraft.util.math.BlockPos
import org.lwjgl.glfw.GLFW

@Suppress("UNCHECKED_CAST")
object IndustrialRevolutionClient : ClientModInitializer {
    override fun onInitializeClient() {
        FluidType.WATER.registerReloadListener()
        FluidType.LAVA.registerReloadListener()
        arrayOf(
            IRFluidRegistry.COOLANT_STILL,
            IRFluidRegistry.SULFURIC_ACID_STILL,
            IRFluidRegistry.TOXIC_MUD_STILL
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
            IndustrialRevolution.FLUID_INFUSER_HANDLER,
            IndustrialRevolution.FARMER_HANDLER,
            IndustrialRevolution.RESOURCE_REPORT_HANDLER,
            IndustrialRevolution.SAWMILL_HANDLER,
            IndustrialRevolution.ELECTRIC_FURNACE_FACTORY_HANDLER,
            IndustrialRevolution.PULVERIZER_FACTORY_HANDLER,
            IndustrialRevolution.COMPRESSOR_FACTORY_HANDLER,
            IndustrialRevolution.INFUSER_FACTORY_HANDLER,
            IndustrialRevolution.CABINET_HANDLER,
            IndustrialRevolution.DRILL_HANDLER
        ).forEach { handler ->
            ScreenRegistry.register(handler) { controller, inv, _ -> IRInventoryScreen(controller, inv.player) }
        }

        MachineRegistry.CHOPPER_REGISTRY.registerBlockEntityRenderer(::AOEMachineBlockEntityRenderer)
        MachineRegistry.RANCHER_REGISTRY.registerBlockEntityRenderer(::AOEMachineBlockEntityRenderer)
        MachineRegistry.FARMER_REGISTRY.registerBlockEntityRenderer(::AOEMachineBlockEntityRenderer)
        MachineRegistry.MODULAR_WORKBENCH_REGISTRY.registerBlockEntityRenderer(::ModularWorkbenchBlockEntityRenderer)
        MachineRegistry.CHARGE_PAD_REGISTRY.registerBlockEntityRenderer(::ChargePadBlockEntityRenderer)
        MachineRegistry.CONDENSER_REGISTRY.registerBlockEntityRenderer(::CondenserBlockEntityRenderer)
        MachineRegistry.FLUID_INFUSER_REGISTRY.registerBlockEntityRenderer(::FluidInfuserBlockEntityRenderer)
        MachineRegistry.SOLID_INFUSER_FACTORY_REGISTRY.registerBlockEntityRenderer(::MultiblockBlockEntityRenderer)
        MachineRegistry.COMPRESSOR_FACTORY_REGISTRY.registerBlockEntityRenderer(::MultiblockBlockEntityRenderer)
        MachineRegistry.PULVERIZER_FACTORY_REGISTRY.registerBlockEntityRenderer(::MultiblockBlockEntityRenderer)
        MachineRegistry.ELECTRIC_FURNACE_FACTORY_REGISTRY.registerBlockEntityRenderer(::MultiblockBlockEntityRenderer)
        MachineRegistry.MINER_REGISTRY.registerBlockEntityRenderer(::MinerBlockEntityRenderer)
        MachineRegistry.PUMP_REGISTRY.registerBlockEntityRenderer(::PumpBlockEntityRenderer)
        MachineRegistry.LAZULI_FLUX_CONTAINER_REGISTRY.registerBlockEntityRenderer(::LazuliFluxContainerBlockEntityRenderer)
        MachineRegistry.HEAT_GENERATOR_REGISTRY.registerBlockEntityRenderer(::HeatGeneratorBlockEntityRenderer)

        BlockEntityRendererRegistry.INSTANCE.register(IRBlockRegistry.TANK_BLOCK_ENTITY, ::TankBlockEntityRenderer)
        BlockEntityRendererRegistry.INSTANCE.register(IRBlockRegistry.DRILL_BLOCK_ENTITY_TYPE, ::DrillBlockEntityRenderer)

        MachineRegistry.MODULAR_WORKBENCH_REGISTRY.setRenderLayer(RenderLayer.getTranslucent())
        MachineRegistry.FISHING_FARM_REGISTRY.setRenderLayer(RenderLayer.getTranslucent())
        MachineRegistry.CABLE_REGISTRY.setRenderLayer(RenderLayer.getTranslucent())
        MachineRegistry.PUMP_REGISTRY.setRenderLayer(RenderLayer.getTranslucent())
        MachineRegistry.HEAT_GENERATOR_REGISTRY.setRenderLayer(RenderLayer.getCutout())
        BlockRenderLayerMap.INSTANCE.putBlock(IRBlockRegistry.TANK_BLOCK, RenderLayer.getCutout())
        BlockRenderLayerMap.INSTANCE.putBlock(IRBlockRegistry.SULFUR_CRYSTAL_CLUSTER, RenderLayer.getTranslucent())
        BlockRenderLayerMap.INSTANCE.putBlock(IRBlockRegistry.DRILL_TOP, RenderLayer.getCutout())
        BlockRenderLayerMap.INSTANCE.putBlock(IRBlockRegistry.DRILL_MIDDLE, RenderLayer.getCutout())
        BlockRenderLayerMap.INSTANCE.putBlock(IRBlockRegistry.DRILL_BOTTOM, RenderLayer.getCutout())

        ModelLoadingRegistry.INSTANCE.registerModelProvider(IRModelManagers)
        ModelLoadingRegistry.INSTANCE.registerVariantProvider { IRModelManagers }

        FabricModelPredicateProviderRegistry.register(
            IRItemRegistry.GAMER_AXE_ITEM,
            identifier("activate")
        ) { stack, _, _ -> stack?.orCreateTag?.getFloat("Progress") ?: 0f }

        PacketRegistry.registerClient()

        GlobalStateController.initClient()

        ClientTickEvents.END_CLIENT_TICK.register { client ->
            positionsToRerender.keys.forEach { pos ->
                positionsToRerender.addTo(pos, -1)
                if (positionsToRerender.getInt(pos) <= 0) {
                    positionsToRerender.removeInt(pos)
                    val blockState = client.world?.getBlockState(pos)
                    client.worldRenderer.updateBlock(client.world, pos, blockState, blockState, 8)
                }
            }
            while (MODULAR_CONTROLLER_KEYBINDING.wasPressed()) {
                val playerInventory = MinecraftClient.getInstance().player?.inventory ?: break
                val hasModularItem = (0 until playerInventory.size())
                    .associateWith { slot -> playerInventory.getStack(slot) }
                    .filter { (_, stack) -> stack.item is IRModularItem<*> }
                    .isNotEmpty()
                if (hasModularItem)
                    MinecraftClient.getInstance()
                        .openScreen(IRModularControllerScreen(ModularController(client.player!!.inventory)))
            }
        }

        ClientSpriteRegistryCallback.event(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE).register(
            ClientSpriteRegistryCallback { _, registry ->
                registry.register(identifier("block/lazuli_flux_container_lf_level"))
            })
    }

    val positionsToRerender = Reference2IntOpenHashMap<BlockPos>()

    private val MODULAR_CONTROLLER_KEYBINDING: KeyBinding = KeyBindingHelper.registerKeyBinding(
        KeyBinding(
            "key.indrev.modular",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_UNKNOWN,
            "category.indrev"
        )
    )
}