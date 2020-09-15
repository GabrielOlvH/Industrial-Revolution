package me.steven.indrev

import alexiil.mc.lib.attributes.fluid.FluidInvUtil
import alexiil.mc.lib.attributes.fluid.impl.GroupedFluidInvFixedWrapper
import me.sargunvohra.mcmods.autoconfig1u.AutoConfig
import me.sargunvohra.mcmods.autoconfig1u.serializer.GsonConfigSerializer
import me.sargunvohra.mcmods.autoconfig1u.serializer.PartitioningSerializer
import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.blockentities.farms.AOEMachineBlockEntity
import me.steven.indrev.components.TransferMode
import me.steven.indrev.config.IRConfig
import me.steven.indrev.energy.NetworkEvents
import me.steven.indrev.gui.controllers.IRGuiController
import me.steven.indrev.gui.controllers.machines.*
import me.steven.indrev.gui.controllers.resreport.ResourceReportController
import me.steven.indrev.gui.controllers.wrench.WrenchController
import me.steven.indrev.gui.widgets.machines.WFluid
import me.steven.indrev.recipes.PatchouliBookRecipe
import me.steven.indrev.recipes.RechargeableRecipe
import me.steven.indrev.recipes.SelfRemainderRecipe
import me.steven.indrev.recipes.compatibility.IRBlastingRecipe
import me.steven.indrev.recipes.compatibility.IRShapelessRecipe
import me.steven.indrev.recipes.compatibility.IRSmeltingRecipe
import me.steven.indrev.recipes.machines.*
import me.steven.indrev.registry.IRLootTables
import me.steven.indrev.registry.IRRegistry
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.identifier
import me.steven.indrev.utils.registerScreenHandler
import me.steven.indrev.world.chunkveins.ChunkVeinData
import me.steven.indrev.world.chunkveins.VeinTypeResourceListener
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry
import net.fabricmc.fabric.api.resource.ResourceManagerHelper
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry
import net.fabricmc.fabric.impl.screenhandler.ExtendedScreenHandlerType
import net.minecraft.item.ItemGroup
import net.minecraft.item.ItemStack
import net.minecraft.resource.ResourceType
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.math.Direction
import net.minecraft.util.registry.Registry
import org.apache.logging.log4j.LogManager
import team.reborn.energy.Energy

object IndustrialRevolution : ModInitializer {
    override fun onInitialize() {
        AutoConfig.register(
            IRConfig::class.java,
            PartitioningSerializer.wrap(::GsonConfigSerializer)
        )
        Energy.registerHolder(MachineBlockEntity::class.java) { obj -> obj as MachineBlockEntity<*> }
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
        MachineRegistry.COAL_GENERATOR_REGISTRY

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
        Registry.register(Registry.RECIPE_SERIALIZER, PatchouliBookRecipe.IDENTIFIER, PatchouliBookRecipe.SERIALIZER)
        Registry.register(Registry.RECIPE_TYPE, PatchouliBookRecipe.IDENTIFIER, PatchouliBookRecipe.TYPE)
        Registry.register(Registry.RECIPE_SERIALIZER, RechargeableRecipe.IDENTIFIER, RechargeableRecipe.SERIALIZER)
        Registry.register(Registry.RECIPE_TYPE, IRSmeltingRecipe.IDENTIFIER, IRSmeltingRecipe.TYPE)
        Registry.register(Registry.RECIPE_SERIALIZER, IRSmeltingRecipe.IDENTIFIER, IRSmeltingRecipe.SERIALIZER)
        Registry.register(Registry.RECIPE_TYPE, IRBlastingRecipe.IDENTIFIER, IRBlastingRecipe.TYPE)
        Registry.register(Registry.RECIPE_SERIALIZER, IRBlastingRecipe.IDENTIFIER, IRBlastingRecipe.SERIALIZER)
        Registry.register(Registry.RECIPE_TYPE, IRShapelessRecipe.IDENTIFIER, IRShapelessRecipe.TYPE)
        Registry.register(Registry.RECIPE_SERIALIZER, IRShapelessRecipe.IDENTIFIER, IRShapelessRecipe.SERIALIZER)
        Registry.register(Registry.RECIPE_SERIALIZER, SelfRemainderRecipe.IDENTIFIER, SelfRemainderRecipe.SERIALIZER)
        Registry.register(Registry.RECIPE_SERIALIZER, SmelterRecipe.IDENTIFIER, SmelterRecipe.SERIALIZER)
        Registry.register(Registry.RECIPE_TYPE, SmelterRecipe.IDENTIFIER, SmelterRecipe.TYPE)
        Registry.register(Registry.RECIPE_SERIALIZER, CondenserRecipe.IDENTIFIER, CondenserRecipe.SERIALIZER)
        Registry.register(Registry.RECIPE_TYPE, CondenserRecipe.IDENTIFIER, CondenserRecipe.TYPE)

        ServerSidePacketRegistry.INSTANCE.register(WrenchController.SAVE_PACKET_ID) { ctx, buf ->
            val isItemConfig = buf.readBoolean()
            val pos = buf.readBlockPos()
            val dir = Direction.byId(buf.readInt())
            val mode = TransferMode.values()[buf.readInt()]
            ctx.taskQueue.execute {
                val world = ctx.player.world
                val blockEntity = world.getBlockEntity(pos) as? MachineBlockEntity<*> ?: return@execute
                if (isItemConfig && blockEntity.inventoryComponent != null) {
                    blockEntity.inventoryComponent!!.itemConfig[dir] = mode
                } else if (blockEntity.fluidComponent != null)
                    blockEntity.fluidComponent!!.transferConfig[dir] = mode
            }
        }

        ServerSidePacketRegistry.INSTANCE.register(AOEMachineBlockEntity.UPDATE_VALUE_PACKET_ID) { ctx, buf ->
            val value = buf.readInt()
            val pos = buf.readBlockPos()
            val world = ctx.player.world
            ctx.taskQueue.execute {
                if (world.isChunkLoaded(pos)) {
                    val blockEntity = world.getBlockEntity(pos) as? AOEMachineBlockEntity<*> ?: return@execute
                    blockEntity.range = value
                    blockEntity.markDirty()
                }
            }
        }

        ServerSidePacketRegistry.INSTANCE.register(WFluid.FLUID_CLICK_PACKET) { ctx, buf ->
            val pos = buf.readBlockPos()
            val player = ctx.player as ServerPlayerEntity
            val world = player.world
            ctx.taskQueue.execute {
                if (world.isChunkLoaded(pos)) {
                    val blockEntity = world.getBlockEntity(pos) as? MachineBlockEntity<*> ?: return@execute
                    val fluidComponent = blockEntity.fluidComponent ?: return@execute
                    FluidInvUtil.interactCursorWithTank(GroupedFluidInvFixedWrapper(fluidComponent), player)
                }
            }
        }

        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(VeinTypeResourceListener())
        LOGGER.info("Industrial Revolution has initialized.")

        ServerTickEvents.END_WORLD_TICK.register(NetworkEvents)
        ServerLifecycleEvents.SERVER_STOPPED.register(NetworkEvents)

        ServerTickEvents.START_SERVER_TICK.register { server ->
            server.playerManager.playerList.forEach { player ->
                val handler = player.currentScreenHandler as? IRGuiController ?: return@forEach
                handler.ctx.run { world, pos ->
                    val blockEntity = world.getBlockEntity(pos) as? MachineBlockEntity<*> ?: return@run
                    blockEntity.sync()
                }
            }
        }
    }

    val LOGGER = LogManager.getLogger("Industrial Revolution")

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

    val WRENCH_HANDLER = WrenchController.SCREEN_ID.registerScreenHandler(::WrenchController)

    val RESOURCE_REPORT_HANDLER = ScreenHandlerRegistry.registerExtended(ResourceReportController.SCREEN_ID) { syncId, inv, buf ->
        val pos = buf.readBlockPos()
        val id = buf.readIdentifier()
        val explored = buf.readInt()
        val size = buf.readInt()
        val veinData = ChunkVeinData(id, size, explored)
        ResourceReportController(syncId, inv, ScreenHandlerContext.create(inv.player.world, pos), veinData)
    } as ExtendedScreenHandlerType<ResourceReportController>

    val CONFIG: IRConfig by lazy { AutoConfig.getConfigHolder(IRConfig::class.java).config }
}