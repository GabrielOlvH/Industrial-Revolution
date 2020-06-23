package me.steven.indrev

import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.gui.controllers.*
import me.steven.indrev.recipes.*
import me.steven.indrev.registry.ModRegistry
import me.steven.indrev.utils.identifier
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry
import net.fabricmc.fabric.impl.screenhandler.ExtendedScreenHandlerType
import net.minecraft.item.ItemGroup
import net.minecraft.item.ItemStack
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.util.registry.Registry
import team.reborn.energy.Energy
import team.reborn.energy.minecraft.EnergyModInitializer

object IndustrialRevolution : EnergyModInitializer() {
    override fun onInitialize() {
        super.onInitialize()
        Energy.registerHolder(MachineBlockEntity::class.java) { obj -> obj as MachineBlockEntity }
        ModRegistry.registerAll()

        Registry.register(Registry.RECIPE_SERIALIZER, PulverizerRecipe.IDENTIFIER, PulverizerRecipe.SERIALIZER)
        Registry.register(Registry.RECIPE_TYPE, PulverizerRecipe.IDENTIFIER, PulverizerRecipe.TYPE)
        Registry.register(Registry.RECIPE_SERIALIZER, CompressorRecipe.IDENTIFIER, CompressorRecipe.SERIALIZER)
        Registry.register(Registry.RECIPE_TYPE, CompressorRecipe.IDENTIFIER, CompressorRecipe.TYPE)
        Registry.register(Registry.RECIPE_SERIALIZER, RechargeableRecipe.IDENTIFIER, RechargeableRecipe.SERIALIZER)
        Registry.register(Registry.RECIPE_TYPE, InfuserRecipe.IDENTIFIER, InfuserRecipe.TYPE)
        Registry.register(Registry.RECIPE_SERIALIZER, InfuserRecipe.IDENTIFIER, InfuserRecipe.SERIALIZER)
        Registry.register(Registry.RECIPE_SERIALIZER, RecyclerRecipe.IDENTIFIER, RecyclerRecipe.SERIALIZER)
        Registry.register(Registry.RECIPE_TYPE, RecyclerRecipe.IDENTIFIER, RecyclerRecipe.TYPE)
    }

    const val MOD_ID = "indrev"

    val MOD_GROUP: ItemGroup =
        FabricItemGroupBuilder.build(identifier("indrev_group")) { ItemStack(ModRegistry.NIKOLITE.dust.get()) }

    val COAL_GENERATOR_HANDLER: ExtendedScreenHandlerType<CoalGeneratorController> =
        ScreenHandlerRegistry.registerExtended(CoalGeneratorController.SCREEN_ID) { syncId, playerInventory, buf ->
            CoalGeneratorController(syncId, playerInventory, ScreenHandlerContext.create(playerInventory.player.world, buf.readBlockPos()))
        } as ExtendedScreenHandlerType<CoalGeneratorController>

    val SOLAR_GENERATOR_HANDLER: ExtendedScreenHandlerType<SolarGeneratorController> =
        ScreenHandlerRegistry.registerExtended(SolarGeneratorController.SCREEN_ID) { syncId, playerInventory, buf ->
            SolarGeneratorController(syncId, playerInventory, ScreenHandlerContext.create(playerInventory.player.world, buf.readBlockPos()))
        } as ExtendedScreenHandlerType<SolarGeneratorController>

    val ELECTRIC_FURNACE_HANDLER: ExtendedScreenHandlerType<ElectricFurnaceController> =
        ScreenHandlerRegistry.registerExtended(ElectricFurnaceController.SCREEN_ID) { syncId, playerInventory, buf ->
            ElectricFurnaceController(syncId, playerInventory, ScreenHandlerContext.create(playerInventory.player.world, buf.readBlockPos()))
        } as ExtendedScreenHandlerType<ElectricFurnaceController>

    val PULVERIZER_HANDLER: ExtendedScreenHandlerType<PulverizerController> =
        ScreenHandlerRegistry.registerExtended(PulverizerController.SCREEN_ID) { syncId, playerInventory, buf ->
            PulverizerController(syncId, playerInventory, ScreenHandlerContext.create(playerInventory.player.world, buf.readBlockPos()))
        } as ExtendedScreenHandlerType<PulverizerController>

    val COMPRESSOR_HANDLER: ExtendedScreenHandlerType<CompressorController> =
        ScreenHandlerRegistry.registerExtended(CompressorController.SCREEN_ID) { syncId, playerInventory, buf ->
            CompressorController(syncId, playerInventory, ScreenHandlerContext.create(playerInventory.player.world, buf.readBlockPos()))
        } as ExtendedScreenHandlerType<CompressorController>

    val BATTERY_HANDLER: ExtendedScreenHandlerType<BatteryController> =
        ScreenHandlerRegistry.registerExtended(BatteryController.SCREEN_ID) { syncId, playerInventory, buf ->
            BatteryController(syncId, playerInventory, ScreenHandlerContext.create(playerInventory.player.world, buf.readBlockPos()))
        } as ExtendedScreenHandlerType<BatteryController>

    val INFUSER_HANDLER: ExtendedScreenHandlerType<InfuserController> =
        ScreenHandlerRegistry.registerExtended(InfuserController.SCREEN_ID) { syncId, playerInventory, buf ->
            InfuserController(syncId, playerInventory, ScreenHandlerContext.create(playerInventory.player.world, buf.readBlockPos()))
        } as ExtendedScreenHandlerType<InfuserController>

    val MINER_HANDLER: ExtendedScreenHandlerType<MinerController> =
        ScreenHandlerRegistry.registerExtended(MinerController.SCREEN_ID) { syncId, playerInventory, buf ->
            MinerController(syncId, playerInventory, ScreenHandlerContext.create(playerInventory.player.world, buf.readBlockPos()))
        } as ExtendedScreenHandlerType<MinerController>

    val RECYCLER_HANDLER: ExtendedScreenHandlerType<RecyclerController> =
        ScreenHandlerRegistry.registerExtended(RecyclerController.SCREEN_ID) { syncId, playerInventory, buf ->
            RecyclerController(syncId, playerInventory, ScreenHandlerContext.create(playerInventory.player.world, buf.readBlockPos()))
        } as ExtendedScreenHandlerType<RecyclerController>

    val BIOMASS_GENERATOR_HANDLER: ExtendedScreenHandlerType<BiomassGeneratorController> =
        ScreenHandlerRegistry.registerExtended(BiomassGeneratorController.SCREEN_ID) { syncId, playerInventory, buf ->
            BiomassGeneratorController(syncId, playerInventory, ScreenHandlerContext.create(playerInventory.player.world, buf.readBlockPos()))
        } as ExtendedScreenHandlerType<BiomassGeneratorController>

    val CHOPPER_HANDLER: ExtendedScreenHandlerType<ChopperController> =
        ScreenHandlerRegistry.registerExtended(ChopperController.SCREEN_ID) { syncId, playerInventory, buf ->
            ChopperController(syncId, playerInventory, ScreenHandlerContext.create(playerInventory.player.world, buf.readBlockPos()))
        } as ExtendedScreenHandlerType<ChopperController>

    val RANCHER_HANDLER: ExtendedScreenHandlerType<RancherController> =
        ScreenHandlerRegistry.registerExtended(RancherController.SCREEN_ID) { syncId, playerInventory, buf ->
            RancherController(syncId, playerInventory, ScreenHandlerContext.create(playerInventory.player.world, buf.readBlockPos()))
        } as ExtendedScreenHandlerType<RancherController>
}