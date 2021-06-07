package me.steven.indrev

import me.steven.indrev.api.IRServerPlayerEntityExtension
import me.steven.indrev.api.machines.Tier
import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.config.IRConfig
import me.steven.indrev.datagen.DataGeneratorManager
import me.steven.indrev.gui.screenhandlers.IRGuiScreenHandler
import me.steven.indrev.gui.screenhandlers.machines.*
import me.steven.indrev.gui.screenhandlers.pipes.PipeFilterScreenHandler
import me.steven.indrev.gui.screenhandlers.resreport.ResourceReportScreenHandler
import me.steven.indrev.gui.screenhandlers.storage.CabinetScreenHandler
import me.steven.indrev.gui.screenhandlers.wrench.WrenchScreenHandler
import me.steven.indrev.networks.EndpointData
import me.steven.indrev.networks.NetworkEvents
import me.steven.indrev.recipes.CopyNBTShapedRecipe
import me.steven.indrev.recipes.RechargeableRecipe
import me.steven.indrev.recipes.SelfRemainderRecipe
import me.steven.indrev.recipes.machines.*
import me.steven.indrev.registry.*
import me.steven.indrev.registry.PacketRegistry.syncConfig
import me.steven.indrev.registry.PacketRegistry.syncVeinData
import me.steven.indrev.utils.getRecipes
import me.steven.indrev.utils.identifier
import me.steven.indrev.utils.registerScreenHandler
import me.steven.indrev.world.chunkveins.ChunkVeinData
import me.steven.indrev.world.chunkveins.VeinTypeResourceListener
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerBlockEntityEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes
import net.fabricmc.fabric.api.resource.ResourceManagerHelper
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry
import net.fabricmc.fabric.impl.screenhandler.ExtendedScreenHandlerType
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.item.ItemGroup
import net.minecraft.item.ItemStack
import net.minecraft.resource.ResourceType
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.sound.SoundEvent
import net.minecraft.util.math.Direction
import net.minecraft.util.registry.BuiltinRegistries
import net.minecraft.util.registry.Registry
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

object IndustrialRevolution : ModInitializer {
    override fun onInitialize() {
        IRConfig
        IRItemRegistry.registerAll()
        IRBlockRegistry.registerAll()
        IRFluidRegistry.registerAll()

        Registry.register(Registry.SOUND_EVENT, LASER_SOUND_ID, LASER_SOUND_EVENT)
        Registry.register(Registry.PARTICLE_TYPE, identifier("laser_particle"), LASER_PARTICLE)

        WorldGeneration.init()

        BuiltinRegistries.BIOME.forEach { biome -> WorldGeneration.handleBiome(biome) }
        RegistryEntryAddedCallback.event(BuiltinRegistries.BIOME).register { _, _, biome -> WorldGeneration.handleBiome(biome) }
        
        arrayOf(
            IRFluidRegistry.COOLANT_STILL,
            IRFluidRegistry.MOLTEN_NETHERITE_STILL,
            IRFluidRegistry.MOLTEN_IRON_STILL,
            IRFluidRegistry.MOLTEN_GOLD_STILL,
            IRFluidRegistry.MOLTEN_COPPER_STILL,
            IRFluidRegistry.MOLTEN_TIN_STILL,
            IRFluidRegistry.MOLTEN_SILVER_STILL,
            IRFluidRegistry.MOLTEN_LEAD_STILL,
            IRFluidRegistry.SULFURIC_ACID_STILL,
            IRFluidRegistry.TOXIC_MUD_STILL
        ).forEach { it.registerFluidKey() }

        IRLootTables.register()

        MachineRegistry

        Registry.register(Registry.RECIPE_SERIALIZER, PulverizerRecipe.IDENTIFIER, PulverizerRecipe.SERIALIZER)
        Registry.register(Registry.RECIPE_TYPE, PulverizerRecipe.IDENTIFIER, PulverizerRecipe.TYPE)
        Registry.register(Registry.RECIPE_SERIALIZER, CompressorRecipe.IDENTIFIER, CompressorRecipe.SERIALIZER)
        Registry.register(Registry.RECIPE_TYPE, CompressorRecipe.IDENTIFIER, CompressorRecipe.TYPE)
        Registry.register(Registry.RECIPE_SERIALIZER, InfuserRecipe.IDENTIFIER, InfuserRecipe.SERIALIZER)
        Registry.register(Registry.RECIPE_TYPE, InfuserRecipe.IDENTIFIER, InfuserRecipe.TYPE)
        Registry.register(Registry.RECIPE_SERIALIZER, FluidInfuserRecipe.IDENTIFIER, FluidInfuserRecipe.SERIALIZER)
        Registry.register(Registry.RECIPE_TYPE, FluidInfuserRecipe.IDENTIFIER, FluidInfuserRecipe.TYPE)
        Registry.register(Registry.RECIPE_SERIALIZER, RecyclerRecipe.IDENTIFIER, RecyclerRecipe.SERIALIZER)
        Registry.register(Registry.RECIPE_TYPE, RecyclerRecipe.IDENTIFIER, RecyclerRecipe.TYPE)
        Registry.register(Registry.RECIPE_SERIALIZER, SmelterRecipe.IDENTIFIER, SmelterRecipe.SERIALIZER)
        Registry.register(Registry.RECIPE_TYPE, SmelterRecipe.IDENTIFIER, SmelterRecipe.TYPE)
        Registry.register(Registry.RECIPE_SERIALIZER, CondenserRecipe.IDENTIFIER, CondenserRecipe.SERIALIZER)
        Registry.register(Registry.RECIPE_TYPE, CondenserRecipe.IDENTIFIER, CondenserRecipe.TYPE)
        Registry.register(Registry.RECIPE_SERIALIZER, SawmillRecipe.IDENTIFIER, SawmillRecipe.SERIALIZER)
        Registry.register(Registry.RECIPE_TYPE, SawmillRecipe.IDENTIFIER, SawmillRecipe.TYPE)
        Registry.register(Registry.RECIPE_SERIALIZER, ModuleRecipe.IDENTIFIER, ModuleRecipe.SERIALIZER)
        Registry.register(Registry.RECIPE_TYPE, ModuleRecipe.IDENTIFIER, ModuleRecipe.TYPE)
        Registry.register(Registry.RECIPE_SERIALIZER, LaserRecipe.IDENTIFIER, LaserRecipe.SERIALIZER)
        Registry.register(Registry.RECIPE_TYPE, LaserRecipe.IDENTIFIER, LaserRecipe.TYPE)

        Registry.register(Registry.RECIPE_SERIALIZER, RechargeableRecipe.IDENTIFIER, RechargeableRecipe.SERIALIZER)
        Registry.register(Registry.RECIPE_SERIALIZER, SelfRemainderRecipe.IDENTIFIER, SelfRemainderRecipe.SERIALIZER)
        Registry.register(Registry.RECIPE_SERIALIZER, CopyNBTShapedRecipe.IDENTIFIER, CopyNBTShapedRecipe.SERIALIZER)

        PacketRegistry.registerServer()

        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(VeinTypeResourceListener())

        ServerTickEvents.END_WORLD_TICK.register(NetworkEvents)
        ServerLifecycleEvents.SERVER_STOPPED.register(NetworkEvents)
        ServerBlockEntityEvents.BLOCK_ENTITY_LOAD.register(NetworkEvents)

        ServerPlayConnectionEvents.JOIN.register { handler, _, _ ->
            val player = handler.player
            syncVeinData(player)
            syncConfig(player)
            if (player is IRServerPlayerEntityExtension) {
                (player as IRServerPlayerEntityExtension).sync()
            }
        }

        ServerTickEvents.START_SERVER_TICK.register { server ->
            server.playerManager.playerList.forEach { player ->

                if (player is IRServerPlayerEntityExtension && player.shouldSync()) {
                    player.sync()
                }

                val handler = player.currentScreenHandler as? IRGuiScreenHandler ?: return@forEach
                handler.ctx.run { world, pos ->
                    val blockEntity = world.getBlockEntity(pos) as? MachineBlockEntity<*> ?: return@run
                    blockEntity.sync()
                }
            }
        }

        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register { s, _, _ ->
            s.recipeManager.getRecipes().keys.filterIsInstance<IRRecipeType<*>>().forEach { it.clearCache() }
        }

        if (FabricLoader.getInstance().getLaunchArguments(true).contains("-dataGen")) {
            ClientLifecycleEvents.CLIENT_STARTED.register(ClientLifecycleEvents.ClientStarted {
                DataGeneratorManager("indrev").generate()
            })
        }

        LOGGER.info("Industrial Revolution has initialized.")
    }

    val LOGGER: Logger = LogManager.getLogger("Industrial Revolution")

    const val MOD_ID = "indrev"

    val MOD_GROUP: ItemGroup =
        FabricItemGroupBuilder.build(identifier("indrev_group")) { ItemStack { MachineRegistry.PULVERIZER_REGISTRY.block(Tier.MK4).asItem() } }

    val COAL_GENERATOR_HANDLER = CoalGeneratorScreenHandler.SCREEN_ID.registerScreenHandler(::CoalGeneratorScreenHandler)
    val SOLAR_GENERATOR_HANDLER = SolarGeneratorScreenHandler.SCREEN_ID.registerScreenHandler(::SolarGeneratorScreenHandler)
    val BIOMASS_GENERATOR_HANDLER = BiomassGeneratorScreenHandler.SCREEN_ID.registerScreenHandler(::BiomassGeneratorScreenHandler)
    val HEAT_GENERATOR_HANDLER = HeatGeneratorScreenHandler.SCREEN_ID.registerScreenHandler(::HeatGeneratorScreenHandler)
    val BATTERY_HANDLER = LazuliFluxContainerScreenHandler.SCREEN_ID.registerScreenHandler(::LazuliFluxContainerScreenHandler)
    val ELECTRIC_FURNACE_HANDLER = ElectricFurnaceScreenHandler.SCREEN_ID.registerScreenHandler(::ElectricFurnaceScreenHandler)
    val PULVERIZER_HANDLER = PulverizerScreenHandler.SCREEN_ID.registerScreenHandler(::PulverizerScreenHandler)
    val COMPRESSOR_HANDLER = CompressorScreenHandler.SCREEN_ID.registerScreenHandler(::CompressorScreenHandler)
    val SOLID_INFUSER_HANDLER = SolidInfuserScreenHandler.SCREEN_ID.registerScreenHandler(::SolidInfuserScreenHandler)
    val RECYCLER_HANDLER = RecyclerScreenHandler.SCREEN_ID.registerScreenHandler(::RecyclerScreenHandler)
    val CHOPPER_HANDLER = ChopperScreenHandler.SCREEN_ID.registerScreenHandler(::ChopperScreenHandler)
    val RANCHER_HANDLER = RancherScreenHandler.SCREEN_ID.registerScreenHandler(::RancherScreenHandler)
    val MINING_RIG_HANDLER = MiningRigComputerScreenHandler.SCREEN_ID.registerScreenHandler(::MiningRigComputerScreenHandler)
    val FISHER_HANDLER = FisherScreenHandler.SCREEN_ID.registerScreenHandler(::FisherScreenHandler)
    val MODULAR_WORKBENCH_HANDLER =
        ModularWorkbenchScreenHandler.SCREEN_ID.registerScreenHandler(::ModularWorkbenchScreenHandler)
    val SMELTER_HANDLER = SmelterScreenHandler.SCREEN_ID.registerScreenHandler(::SmelterScreenHandler)
    val CONDENSER_HANDLER = CondenserScreenHandler.SCREEN_ID.registerScreenHandler(::CondenserScreenHandler)
    val FLUID_INFUSER_HANDLER = FluidInfuserScreenHandler.SCREEN_ID.registerScreenHandler(::FluidInfuserScreenHandler)
    val FARMER_HANDLER = FarmerScreenHandler.SCREEN_ID.registerScreenHandler(::FarmerScreenHandler)
    val SAWMILL_HANDLER = SawmillScreenHandler.SCREEN_ID.registerScreenHandler(::SawmillScreenHandler)
    val LASER_HANDLER = LaserEmitterScreenHandler.SCREEN_ID.registerScreenHandler(::LaserEmitterScreenHandler)

    val ELECTRIC_FURNACE_FACTORY_HANDLER = ElectricFurnaceFactoryScreenHandler.SCREEN_ID.registerScreenHandler(::ElectricFurnaceFactoryScreenHandler)
    val PULVERIZER_FACTORY_HANDLER = PulverizerFactoryScreenHandler.SCREEN_ID.registerScreenHandler(::PulverizerFactoryScreenHandler)
    val COMPRESSOR_FACTORY_HANDLER = CompressorFactoryScreenHandler.SCREEN_ID.registerScreenHandler(::CompressorFactoryScreenHandler)
    val SOLID_INFUSER_FACTORY_HANDLER = SolidInfuserFactoryScreenHandler.SCREEN_ID.registerScreenHandler(::SolidInfuserFactoryScreenHandler)

    val PIPE_FILTER_HANDLER = ScreenHandlerRegistry.registerExtended(PipeFilterScreenHandler.SCREEN_ID) { syncId, inv, buf ->
        val dir = buf.readEnumConstant(Direction::class.java)
        val pos = buf.readBlockPos()
        val list = (0 until 9).map { buf.readItemStack() }
        val whitelist = buf.readBoolean()
        val matchDurability = buf.readBoolean()
        val matchTag = buf.readBoolean()
        val hasServo = buf.readBoolean()
        val type = if (hasServo) buf.readEnumConstant(EndpointData.Type::class.java) else null
        val mode = if (hasServo) buf.readEnumConstant(EndpointData.Mode::class.java) else null

        val controller = PipeFilterScreenHandler(syncId, inv, whitelist, matchDurability, matchTag, mode, type)
        controller.direction = dir
        controller.blockPos = pos
        list.forEachIndexed { index, itemStack -> controller.backingList[index] = itemStack }
        controller
    } as ExtendedScreenHandlerType<PipeFilterScreenHandler>

    val DRILL_HANDLER = MiningRigDrillScreenHandler.SCREEN_ID.registerScreenHandler(::MiningRigDrillScreenHandler)

    val WRENCH_HANDLER = WrenchScreenHandler.SCREEN_ID.registerScreenHandler(::WrenchScreenHandler)

    val RESOURCE_REPORT_HANDLER = ScreenHandlerRegistry.registerExtended(ResourceReportScreenHandler.SCREEN_ID) { syncId, inv, buf ->
        val pos = buf.readBlockPos()
        val id = buf.readIdentifier()
        val explored = buf.readInt()
        val size = buf.readInt()
        val veinData = ChunkVeinData(id, size, explored)
        ResourceReportScreenHandler(syncId, inv, ScreenHandlerContext.create(inv.player.world, pos), veinData)
    } as ExtendedScreenHandlerType<ResourceReportScreenHandler>

    val CABINET_HANDLER = CabinetScreenHandler.SCREEN_ID.registerScreenHandler(::CabinetScreenHandler)

    val LASER_SOUND_ID = identifier("laser")
    val LASER_SOUND_EVENT = SoundEvent(LASER_SOUND_ID)
    val LASER_PARTICLE = FabricParticleTypes.simple()

    val SYNC_VEINS_PACKET = identifier("sync_veins_packet")
    val SYNC_CONFIG_PACKET = identifier("sync_config_packet")
    val UPDATE_MODULAR_TOOL_LEVEL = identifier("update_modular_level")
    val SYNC_PROPERTY = identifier("sync_property")
    val SYNC_MODULE_PACKET = identifier("sync_module")
    val SYNC_NETWORK_SERVOS = identifier("sync_network_servos")
}