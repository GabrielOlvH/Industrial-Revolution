package me.steven.indrev

import me.steven.indrev.blockentities.battery.ChargePadBlockEntity
import me.steven.indrev.blockentities.battery.ChargePadBlockEntityRenderer
import me.steven.indrev.blockentities.cables.CableBlockEntity
import me.steven.indrev.blockentities.cables.CableBlockEntityRenderer
import me.steven.indrev.blockentities.farms.AOEMachineBlockEntity
import me.steven.indrev.blockentities.farms.AOEMachineBlockEntityRenderer
import me.steven.indrev.blockentities.modularworkbench.ModularWorkbenchBlockEntity
import me.steven.indrev.blockentities.modularworkbench.ModularWorkbenchBlockEntityRenderer
import me.steven.indrev.gui.IRInventoryScreen
import me.steven.indrev.registry.FluidRenderRegistry
import me.steven.indrev.registry.IRHudRender
import me.steven.indrev.registry.IRRegistry
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.Tier
import me.steven.indrev.utils.identifier
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.`object`.builder.v1.client.model.FabricModelPredicateProviderRegistry
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap
import net.fabricmc.fabric.api.client.rendereregistry.v1.BlockEntityRendererRegistry
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.client.render.RenderLayer

@Suppress("UNCHECKED_CAST")
object IndustrialRevolutionClient : ClientModInitializer {
    override fun onInitializeClient() {
        FluidRenderRegistry.registerAll()
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
            IndustrialRevolution.WRENCH_HANDLER
        ).forEach { handler ->
            ScreenRegistry.register(handler) { controller, inv, _ -> IRInventoryScreen(controller, inv.player) }
        }

        MachineRegistry.CABLE_REGISTRY.forEach { _, blockEntity ->
            BlockEntityRendererRegistry.INSTANCE.register(blockEntity as BlockEntityType<CableBlockEntity>, ::CableBlockEntityRenderer)
        }

        MachineRegistry.CHOPPER_REGISTRY.forEach { _, blockEntity ->
            BlockEntityRendererRegistry.INSTANCE.register(blockEntity as BlockEntityType<AOEMachineBlockEntity>, ::AOEMachineBlockEntityRenderer)
        }

        MachineRegistry.RANCHER_REGISTRY.forEach { _, blockEntity ->
            BlockEntityRendererRegistry.INSTANCE.register(blockEntity as BlockEntityType<AOEMachineBlockEntity>, ::AOEMachineBlockEntityRenderer)
        }

        MachineRegistry.MODULAR_WORKBENCH_REGISTRY.forEach { _, blockEntity ->
            BlockEntityRendererRegistry.INSTANCE.register(blockEntity as BlockEntityType<ModularWorkbenchBlockEntity>, ::ModularWorkbenchBlockEntityRenderer)
        }

        MachineRegistry.CHARGE_PAD_REGISTRY.forEach { _, blockEntity ->
            BlockEntityRendererRegistry.INSTANCE.register(blockEntity as BlockEntityType<ChargePadBlockEntity>, ::ChargePadBlockEntityRenderer)
        }

        BlockRenderLayerMap.INSTANCE.putBlock(IRRegistry.AREA_INDICATOR, RenderLayer.getTranslucent())
        BlockRenderLayerMap.INSTANCE.putBlock(MachineRegistry.MODULAR_WORKBENCH_REGISTRY.block(Tier.MK4), RenderLayer.getTranslucent())

        FabricModelPredicateProviderRegistry.register(IRRegistry.GAMER_AXE_ITEM, identifier("activate")) predicate@{ stack, _, _ ->
            val tag = stack?.orCreateTag ?: return@predicate 0f
            return@predicate tag.getFloat("Progress")
        }
    }
}