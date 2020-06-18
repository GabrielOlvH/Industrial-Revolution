package me.steven.indrev

import me.steven.indrev.blockentities.cables.CableBlockEntity
import me.steven.indrev.blockentities.cables.CableBlockEntityRenderer
import me.steven.indrev.blockentities.farms.AOEMachineBlockEntity
import me.steven.indrev.blockentities.farms.AOEMachineBlockEntityRenderer
import me.steven.indrev.gui.IRInventoryScreen
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.registry.ModRegistry
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap
import net.fabricmc.fabric.api.client.rendereregistry.v1.BlockEntityRendererRegistry
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.client.render.RenderLayer

class IndustrialRevolutionClient : ClientModInitializer {
    override fun onInitializeClient() {
        ScreenRegistry.register(IndustrialRevolution.COAL_GENERATOR_HANDLER) { controller, inventory, _ ->
            IRInventoryScreen(controller, inventory.player)
        }

        ScreenRegistry.register(IndustrialRevolution.SOLAR_GENERATOR_HANDLER) { controller, inventory, _ ->
            IRInventoryScreen(controller, inventory.player)
        }

        ScreenRegistry.register(IndustrialRevolution.ELECTRIC_FURNACE_HANDLER) { controller, inventory, _ ->
            IRInventoryScreen(controller, inventory.player)
        }

        ScreenRegistry.register(IndustrialRevolution.PULVERIZER_HANDLER) { controller, inventory, _ ->
            IRInventoryScreen(controller, inventory.player)
        }

        ScreenRegistry.register(IndustrialRevolution.COMPRESSOR_HANDLER) { controller, inventory, _ ->
            IRInventoryScreen(controller, inventory.player)
        }

        ScreenRegistry.register(IndustrialRevolution.BATTERY_HANDLER) { controller, inventory, _ ->
            IRInventoryScreen(controller, inventory.player)
        }

        ScreenRegistry.register(IndustrialRevolution.INFUSER_HANDLER) { controller, inventory, _ ->
            IRInventoryScreen(controller, inventory.player)
        }

        ScreenRegistry.register(IndustrialRevolution.MINER_HANDLER) { controller, inventory, _ ->
            IRInventoryScreen(controller, inventory.player)
        }

        ScreenRegistry.register(IndustrialRevolution.NUCLEAR_REACTOR_HANDLER) { controller, inventory, _ ->
            IRInventoryScreen(controller, inventory.player)
        }

        ScreenRegistry.register(IndustrialRevolution.RECYCLER_HANDLER) { controller, inventory, _ ->
            IRInventoryScreen(controller, inventory.player)
        }

        ScreenRegistry.register(IndustrialRevolution.BIOMASS_GENERATOR_HANDLER) { controller, inventory, _ ->
            IRInventoryScreen(controller, inventory.player)
        }

        ScreenRegistry.register(IndustrialRevolution.CHOPPER_HANDLER) { controller, inventory, _ ->
            IRInventoryScreen(controller, inventory.player)
        }

        ScreenRegistry.register(IndustrialRevolution.RANCHER_HANDLER) { controller, inventory, _ ->
            IRInventoryScreen(controller, inventory.player)
        }


        MachineRegistry.CABLE_REGISTRY.forEach { _, blockEntity ->
            BlockEntityRendererRegistry.INSTANCE.register(blockEntity as BlockEntityType<CableBlockEntity>) {
                CableBlockEntityRenderer(it)
            }
        }

        MachineRegistry.CHOPPER_REGISTRY.forEach { _, blockEntity ->
            BlockEntityRendererRegistry.INSTANCE.register(blockEntity as BlockEntityType<AOEMachineBlockEntity>) {
                AOEMachineBlockEntityRenderer(it)
            }
        }

        MachineRegistry.RANCHER_REGISTRY.forEach { _, blockEntity ->
            BlockEntityRendererRegistry.INSTANCE.register(blockEntity as BlockEntityType<AOEMachineBlockEntity>) {
                AOEMachineBlockEntityRenderer(it)
            }
        }

        BlockRenderLayerMap.INSTANCE.putBlock(ModRegistry.AREA_INDICATOR, RenderLayer.getTranslucent())
    }
}