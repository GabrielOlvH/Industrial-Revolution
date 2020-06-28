package me.steven.indrev

import me.steven.indrev.blockentities.cables.CableBlockEntity
import me.steven.indrev.blockentities.cables.CableBlockEntityRenderer
import me.steven.indrev.blockentities.farms.AOEMachineBlockEntity
import me.steven.indrev.blockentities.farms.AOEMachineBlockEntityRenderer
import me.steven.indrev.gui.IRInventoryScreen
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.registry.ModRegistry
import me.steven.indrev.utils.identifier
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry
import net.fabricmc.fabric.api.client.rendereregistry.v1.BlockEntityRendererRegistry
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback
import net.fabricmc.fabric.api.resource.ResourceManagerHelper
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.texture.Sprite
import net.minecraft.fluid.FluidState
import net.minecraft.resource.ResourceManager
import net.minecraft.resource.ResourceType
import net.minecraft.screen.PlayerScreenHandler
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.world.BlockRenderView


@Suppress("UNCHECKED_CAST")
class IndustrialRevolutionClient : ClientModInitializer {
    override fun onInitializeClient() {
        ScreenRegistry.register(IndustrialRevolution.COAL_GENERATOR_HANDLER) { controller, inventory, _ ->
            IRInventoryScreen(controller, inventory.player)
        }

        ScreenRegistry.register(IndustrialRevolution.SOLAR_GENERATOR_HANDLER) { controller, inventory, _ ->
            IRInventoryScreen(controller, inventory.player)
        }

        ScreenRegistry.register(IndustrialRevolution.HEAT_GENERATOR_HANDLER) { controller, inventory, _ ->
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
        BlockRenderLayerMap.INSTANCE.putFluids(RenderLayer.getTranslucent(), ModRegistry.COOLANT_FLUID_STILL, ModRegistry.COOLANT_FLUID_FLOWING)

        val stillCoolantSprite = Identifier("block/water_still")
        val flowingCoolantSprite = Identifier("block/water_flow")
        val stillMoltenNetheriteSprite = identifier("block/molten_netherite_still")
        val flowingMoltenNetheriteSprite = identifier("block/molten_netherite_flow")

        ClientSpriteRegistryCallback.event(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE).register(ClientSpriteRegistryCallback { _, registry ->
            registry.register(stillCoolantSprite)
            registry.register(flowingCoolantSprite)
            registry.register(stillMoltenNetheriteSprite)
            registry.register(flowingMoltenNetheriteSprite)
        })

        val coolantSprites = arrayOfNulls<Sprite>(2)
        val moltenFluidSprites = arrayOfNulls<Sprite>(2)
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(object : SimpleSynchronousResourceReloadListener {
            override fun apply(manager: ResourceManager?) {
                val atlas = MinecraftClient.getInstance().getSpriteAtlas(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE)
                coolantSprites[0] = atlas.apply(stillCoolantSprite)
                coolantSprites[1] = atlas.apply(flowingCoolantSprite)
            }

            override fun getFabricId(): Identifier = identifier("water_reload_listener")
        })

        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(object : SimpleSynchronousResourceReloadListener {
            override fun apply(manager: ResourceManager?) {
                val atlas = MinecraftClient.getInstance().getSpriteAtlas(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE)
                moltenFluidSprites[0] = atlas.apply(stillMoltenNetheriteSprite)
                moltenFluidSprites[1] = atlas.apply(flowingMoltenNetheriteSprite)
            }

            override fun getFabricId(): Identifier = identifier("lava_reload_listener")
        })

        val coolantFluidRender = object : FluidRenderHandler {
            override fun getFluidSprites(view: BlockRenderView, pos: BlockPos, state: FluidState): Array<Sprite?> = coolantSprites

            override fun getFluidColor(view: BlockRenderView, pos: BlockPos, state: FluidState): Int = 0x0C2340
        }

        FluidRenderHandlerRegistry.INSTANCE.register(ModRegistry.COOLANT_FLUID_STILL, coolantFluidRender)
        FluidRenderHandlerRegistry.INSTANCE.register(ModRegistry.COOLANT_FLUID_FLOWING, coolantFluidRender)

        val moltenNetheriteFluidRender = object : FluidRenderHandler {
            override fun getFluidSprites(view: BlockRenderView, pos: BlockPos, state: FluidState): Array<Sprite?> = moltenFluidSprites

            override fun getFluidColor(view: BlockRenderView, pos: BlockPos, state: FluidState): Int = 0x654740
        }

        FluidRenderHandlerRegistry.INSTANCE.register(ModRegistry.MOLTEN_NETHERITE_STILL, moltenNetheriteFluidRender)
        FluidRenderHandlerRegistry.INSTANCE.register(ModRegistry.MOLTEN_NETHERITE_FLOWING, moltenNetheriteFluidRender)
    }
}