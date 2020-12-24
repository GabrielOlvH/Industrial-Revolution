package me.steven.indrev

import me.sargunvohra.mcmods.autoconfig1u.AutoConfig
import me.sargunvohra.mcmods.autoconfig1u.serializer.GsonConfigSerializer
import me.sargunvohra.mcmods.autoconfig1u.serializer.PartitioningSerializer
import me.steven.indrev.api.IRServerPlayerEntityExtension
import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.config.IRConfig
import me.steven.indrev.datagen.DataGeneratorManager
import me.steven.indrev.energy.NetworkEvents
import me.steven.indrev.gui.controllers.IRGuiController
import me.steven.indrev.gui.controllers.machines.*
import me.steven.indrev.gui.controllers.resreport.ResourceReportController
import me.steven.indrev.gui.controllers.storage.CabinetController
import me.steven.indrev.gui.controllers.wrench.WrenchController
import me.steven.indrev.recipes.CopyNBTShapedRecipe
import me.steven.indrev.recipes.RechargeableRecipe
import me.steven.indrev.recipes.SelfRemainderRecipe
import me.steven.indrev.recipes.machines.*
import me.steven.indrev.registry.IRLootTables
import me.steven.indrev.registry.IRRegistry
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.registry.PacketRegistry
import me.steven.indrev.utils.*
import me.steven.indrev.world.chunkveins.ChunkVeinData
import me.steven.indrev.world.chunkveins.VeinTypeResourceListener
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.resource.ResourceManagerHelper
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry
import net.fabricmc.fabric.impl.screenhandler.ExtendedScreenHandlerType
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.item.ItemGroup
import net.minecraft.item.ItemStack
import net.minecraft.resource.ResourceType
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.util.registry.Registry
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

object IndustrialRevolution : ModInitializer {
    override fun onInitialize() {
        AutoConfig.register(
            IRConfig::class.java,
            PartitioningSerializer.wrap(::GsonConfigSerializer)
        )
        IRRegistry.registerAll()
        arrayOf(
            IRRegistry.COOLANT_STILL,
            IRRegistry.MOLTEN_NETHERITE_STILL,
            IRRegistry.MOLTEN_IRON_STILL,
            IRRegistry.MOLTEN_GOLD_STILL,
            IRRegistry.MOLTEN_COPPER_STILL,
            IRRegistry.MOLTEN_TIN_STILL,
            IRRegistry.SULFURIC_ACID_STILL,
            IRRegistry.TOXIC_MUD_STILL
        ).forEach { it.registerFluidKey() }
        IRLootTables.register()
        MachineRegistry
        if (FabricLoader.getInstance().getLaunchArguments(true).contains("-dataGen")) {
             ClientLifecycleEvents.CLIENT_STARTED.register(ClientLifecycleEvents.ClientStarted { _ ->
                 DataGeneratorManager("indrev").generate()
             })
        }

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

        Registry.register(Registry.RECIPE_SERIALIZER, RechargeableRecipe.IDENTIFIER, RechargeableRecipe.SERIALIZER)
        Registry.register(Registry.RECIPE_SERIALIZER, SelfRemainderRecipe.IDENTIFIER, SelfRemainderRecipe.SERIALIZER)
        Registry.register(Registry.RECIPE_SERIALIZER, CopyNBTShapedRecipe.IDENTIFIER, CopyNBTShapedRecipe.SERIALIZER)

        PacketRegistry.registerServer()

        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(VeinTypeResourceListener())

        ServerTickEvents.END_WORLD_TICK.register(NetworkEvents)
        ServerLifecycleEvents.SERVER_STOPPED.register(NetworkEvents)

        ServerTickEvents.START_SERVER_TICK.register { server ->
            server.playerManager.playerList.forEach { player ->

                if (player is IRServerPlayerEntityExtension && player.shouldSync()) {
                    player.sync()
                }

                val handler = player.currentScreenHandler as? IRGuiController ?: return@forEach
                handler.ctx.run { world, pos ->
                    val blockEntity = world.getBlockEntity(pos) as? MachineBlockEntity<*> ?: return@run
                    blockEntity.sync()
                }
            }
        }

        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register { s, _, _ ->
            s.recipeManager.getRecipes().keys.filterIsInstance<IRRecipeType<*>>().forEach { it.clearCache() }
        }

        LOGGER.info("Industrial Revolution has initialized.")
    }

    val LOGGER: Logger = LogManager.getLogger("Industrial Revolution")

    const val MOD_ID = "indrev"

    val MOD_GROUP: ItemGroup =
        FabricItemGroupBuilder.build(identifier("indrev_group")) { ItemStack { IRRegistry.NIKOLITE_ORE().asItem() } }

    val COAL_GENERATOR_HANDLER = CoalGeneratorController.SCREEN_ID.registerScreenHandler(::CoalGeneratorController)
    val SOLAR_GENERATOR_HANDLER = SolarGeneratorController.SCREEN_ID.registerScreenHandler(::SolarGeneratorController)
    val BIOMASS_GENERATOR_HANDLER = BiomassGeneratorController.SCREEN_ID.registerScreenHandler(::BiomassGeneratorController)
    val HEAT_GENERATOR_HANDLER = HeatGeneratorController.SCREEN_ID.registerScreenHandler(::HeatGeneratorController)
    val BATTERY_HANDLER = BatteryController.SCREEN_ID.registerScreenHandler(::BatteryController)
    val ELECTRIC_FURNACE_HANDLER = ElectricFurnaceController.SCREEN_ID.registerScreenHandler(::ElectricFurnaceController)
    val PULVERIZER_HANDLER = PulverizerController.SCREEN_ID.registerScreenHandler(::PulverizerController)
    val COMPRESSOR_HANDLER = CompressorController.SCREEN_ID.registerScreenHandler(::CompressorController)
    val INFUSER_HANDLER = InfuserController.SCREEN_ID.registerScreenHandler(::InfuserController)
    val RECYCLER_HANDLER = RecyclerController.SCREEN_ID.registerScreenHandler(::RecyclerController)
    val CHOPPER_HANDLER = ChopperController.SCREEN_ID.registerScreenHandler(::ChopperController)
    val RANCHER_HANDLER = RancherController.SCREEN_ID.registerScreenHandler(::RancherController)
    val MINER_HANDLER = MinerController.SCREEN_ID.registerScreenHandler(::MinerController)
    val FISHING_FARM_HANDLER = FishingFarmController.SCREEN_ID.registerScreenHandler(::FishingFarmController)
    val MODULAR_WORKBENCH_HANDLER =
        ModularWorkbenchController.SCREEN_ID.registerScreenHandler(::ModularWorkbenchController)
    val SMELTER_HANDLER = SmelterController.SCREEN_ID.registerScreenHandler(::SmelterController)
    val CONDENSER_HANDLER = CondenserController.SCREEN_ID.registerScreenHandler(::CondenserController)
    val FLUID_INFUSER_HANDLER = FluidInfuserController.SCREEN_ID.registerScreenHandler(::FluidInfuserController)
    val FARMER_HANDLER = FarmerController.SCREEN_ID.registerScreenHandler(::FarmerController)
    val SAWMILL_HANDLER = SawmillController.SCREEN_ID.registerScreenHandler(::SawmillController)

    val ELECTRIC_FURNACE_FACTORY_HANDLER = ElectricFurnaceFactoryController.SCREEN_ID.registerScreenHandler(::ElectricFurnaceFactoryController)
    val PULVERIZER_FACTORY_HANDLER = PulverizerFactoryController.SCREEN_ID.registerScreenHandler(::PulverizerFactoryController)
    val COMPRESSOR_FACTORY_HANDLER = CompressorFactoryController.SCREEN_ID.registerScreenHandler(::CompressorFactoryController)
    val INFUSER_FACTORY_HANDLER = InfuserFactoryController.SCREEN_ID.registerScreenHandler(::InfuserFactoryController)

    val DRILL_HANDLER = DrillController.SCREEN_ID.registerScreenHandler(::DrillController)

    val WRENCH_HANDLER = WrenchController.SCREEN_ID.registerScreenHandler(::WrenchController)

    val RESOURCE_REPORT_HANDLER = ScreenHandlerRegistry.registerExtended(ResourceReportController.SCREEN_ID) { syncId, inv, buf ->
        val pos = buf.readBlockPos()
        val id = buf.readIdentifier()
        val explored = buf.readInt()
        val size = buf.readInt()
        val veinData = ChunkVeinData(id, size, explored)
        ResourceReportController(syncId, inv, ScreenHandlerContext.create(inv.player.world, pos), veinData)
    } as ExtendedScreenHandlerType<ResourceReportController>

    val CABINET_HANDLER = CabinetController.SCREEN_ID.registerScreenHandler(::CabinetController)

    val CONFIG: IRConfig by lazy { AutoConfig.getConfigHolder(IRConfig::class.java).config }

    val SYNC_VEINS_PACKET = identifier("sync_veins_packet")
    val UPDATE_MODULAR_TOOL_LEVEL = identifier("update_modular_level")
    val SYNC_PROPERTY = identifier("sync_property")
    val SYNC_MODULE_PACKET = identifier("sync_module")
    val RERENDER_CHUNK_PACKET = identifier("rerender_chunk")
    val SCHEDULE_RERENDER_CHUNK_PACKET = identifier("scheduled_rerender_chunk")
}