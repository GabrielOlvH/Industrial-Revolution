package me.steven.indrev

import me.sargunvohra.mcmods.autoconfig1u.AutoConfig
import me.sargunvohra.mcmods.autoconfig1u.ConfigData
import me.sargunvohra.mcmods.autoconfig1u.serializer.GsonConfigSerializer
import me.sargunvohra.mcmods.autoconfig1u.serializer.PartitioningSerializer
import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.components.InventoryComponent
import me.steven.indrev.config.IRConfig
import me.steven.indrev.gui.controllers.*
import me.steven.indrev.gui.controllers.wrench.WrenchController
import me.steven.indrev.recipes.*
import me.steven.indrev.registry.IRLootTables
import me.steven.indrev.registry.IRRegistry
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.identifier
import me.steven.indrev.utils.registerScreenHandler
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry
import net.minecraft.item.ItemGroup
import net.minecraft.item.ItemStack
import net.minecraft.util.math.Direction
import net.minecraft.util.registry.Registry
import team.reborn.energy.Energy
import team.reborn.energy.minecraft.EnergyModInitializer

object IndustrialRevolution : EnergyModInitializer() {
    override fun onInitialize() {
        super.onInitialize()
        AutoConfig.register(
            IRConfig::class.java,
            PartitioningSerializer.wrap<IRConfig, ConfigData>(::GsonConfigSerializer)
        )
        Energy.registerHolder(MachineBlockEntity::class.java) { obj -> obj as MachineBlockEntity }
        IRRegistry.registerAll()
        IRLootTables.register()
        MachineRegistry.COAL_GENERATOR_REGISTRY

        Registry.register(Registry.RECIPE_SERIALIZER, PulverizerRecipe.IDENTIFIER, PulverizerRecipe.SERIALIZER)
        Registry.register(Registry.RECIPE_TYPE, PulverizerRecipe.IDENTIFIER, PulverizerRecipe.TYPE)
        Registry.register(Registry.RECIPE_SERIALIZER, CompressorRecipe.IDENTIFIER, CompressorRecipe.SERIALIZER)
        Registry.register(Registry.RECIPE_TYPE, CompressorRecipe.IDENTIFIER, CompressorRecipe.TYPE)
        Registry.register(Registry.RECIPE_SERIALIZER, InfuserRecipe.IDENTIFIER, InfuserRecipe.SERIALIZER)
        Registry.register(Registry.RECIPE_TYPE, InfuserRecipe.IDENTIFIER, InfuserRecipe.TYPE)
        Registry.register(Registry.RECIPE_SERIALIZER, RecyclerRecipe.IDENTIFIER, RecyclerRecipe.SERIALIZER)
        Registry.register(Registry.RECIPE_TYPE, RecyclerRecipe.IDENTIFIER, RecyclerRecipe.TYPE)
        Registry.register(Registry.RECIPE_SERIALIZER, PatchouliBookRecipe.IDENTIFIER, PatchouliBookRecipe.SERIALIZER)
        Registry.register(Registry.RECIPE_TYPE, PatchouliBookRecipe.IDENTIFIER, PatchouliBookRecipe.TYPE)
        Registry.register(Registry.RECIPE_SERIALIZER, RechargeableRecipe.IDENTIFIER, RechargeableRecipe.SERIALIZER)
        Registry.register(Registry.RECIPE_TYPE, IRSmeltingRecipe.IDENTIFIER, IRSmeltingRecipe.TYPE)
        Registry.register(Registry.RECIPE_SERIALIZER, IRSmeltingRecipe.IDENTIFIER, IRSmeltingRecipe.SERIALIZER)
        Registry.register(Registry.RECIPE_TYPE, IRShapelessRecipe.IDENTIFIER, IRShapelessRecipe.TYPE)
        Registry.register(Registry.RECIPE_SERIALIZER, IRShapelessRecipe.IDENTIFIER, IRShapelessRecipe.SERIALIZER)

        ServerSidePacketRegistry.INSTANCE.register(WrenchController.SAVE_PACKET_ID) { ctx, buf ->
            val pos = buf.readBlockPos()
            val dir = Direction.byId(buf.readInt())
            val mode = InventoryComponent.Mode.values()[buf.readInt()]
            ctx.taskQueue.execute {
                val world = ctx.player.world
                val blockEntity = world.getBlockEntity(pos) as? MachineBlockEntity ?: return@execute
                if (blockEntity.inventoryComponent != null) {
                    blockEntity.inventoryComponent!!.itemConfig[dir] = mode
                }
            }
        }
    }

    const val MOD_ID = "indrev"

    val MOD_GROUP: ItemGroup =
        FabricItemGroupBuilder.build(identifier("indrev_group")) { ItemStack(IRRegistry.NIKOLITE_ORE) }

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
    val MODULAR_WORKBENCH_HANDLER =
        ModularWorkbenchController.SCREEN_ID.registerScreenHandler(::ModularWorkbenchController)

    val WRENCH_HANDLER = WrenchController.SCREEN_ID.registerScreenHandler(::WrenchController)

    val CONFIG: IRConfig by lazy { AutoConfig.getConfigHolder(IRConfig::class.java).config }
}