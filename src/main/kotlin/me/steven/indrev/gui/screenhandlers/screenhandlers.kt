package me.steven.indrev.gui.screenhandlers

import me.steven.indrev.api.sideconfigs.ConfigurationType
import me.steven.indrev.api.sideconfigs.SideConfiguration
import me.steven.indrev.gui.screenhandlers.machines.*
import me.steven.indrev.gui.screenhandlers.pipes.PipeFilterScreenHandler
import me.steven.indrev.gui.screenhandlers.storage.CabinetScreenHandler
import me.steven.indrev.gui.screenhandlers.wrench.ScrewdriverScreenHandler
import me.steven.indrev.networks.EndpointData
import me.steven.indrev.utils.registerScreenHandler
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.util.math.Direction
import java.util.*

val COAL_GENERATOR_HANDLER = CoalGeneratorScreenHandler.SCREEN_ID.registerScreenHandler(::CoalGeneratorScreenHandler)
val SOLAR_GENERATOR_HANDLER = SolarGeneratorScreenHandler.SCREEN_ID.registerScreenHandler(::SolarGeneratorScreenHandler)
val BIOMASS_GENERATOR_HANDLER = BiomassGeneratorScreenHandler.SCREEN_ID.registerScreenHandler(::BiomassGeneratorScreenHandler)
val HEAT_GENERATOR_HANDLER = HeatGeneratorScreenHandler.SCREEN_ID.registerScreenHandler(::HeatGeneratorScreenHandler)
val GAS_BURNING_GENERATOR_HANDLER = GasBurningGeneratorScreenHandler.SCREEN_ID.registerScreenHandler(::GasBurningGeneratorScreenHandler)
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
val SLAUGHTER_HANDLER = SlaughterScreenHandler.SCREEN_ID.registerScreenHandler(::SlaughterScreenHandler)
val SAWMILL_HANDLER = SawmillScreenHandler.SCREEN_ID.registerScreenHandler(::SawmillScreenHandler)
val LASER_HANDLER = LaserEmitterScreenHandler.SCREEN_ID.registerScreenHandler(::LaserEmitterScreenHandler)
val ELECTROLYTIC_SEPARATOR_HANDLER = ElectrolyticSeparatorScreenHandler.SCREEN_ID.registerScreenHandler(::ElectrolyticSeparatorScreenHandler)
val DATA_CARD_WRITER_HANDLER = DataCardWriterScreenHandler.SCREEN_ID.registerScreenHandler(::DataCardWriterScreenHandler)

val ELECTRIC_FURNACE_FACTORY_HANDLER = ElectricFurnaceFactoryScreenHandler.SCREEN_ID.registerScreenHandler(::ElectricFurnaceFactoryScreenHandler)
val PULVERIZER_FACTORY_HANDLER = PulverizerFactoryScreenHandler.SCREEN_ID.registerScreenHandler(::PulverizerFactoryScreenHandler)
val COMPRESSOR_FACTORY_HANDLER = CompressorFactoryScreenHandler.SCREEN_ID.registerScreenHandler(::CompressorFactoryScreenHandler)
val SOLID_INFUSER_FACTORY_HANDLER = SolidInfuserFactoryScreenHandler.SCREEN_ID.registerScreenHandler(::SolidInfuserFactoryScreenHandler)

val STEAM_TURBINE_HANDLER = SteamTurbineScreenHandler.SCREEN_ID.registerScreenHandler(::SteamTurbineScreenHandler)
val SOLAR_POWER_PLANT_TOWER_HANDLER = SolarPowerPlantTowerScreenHandler.SCREEN_ID.registerScreenHandler(::SolarPowerPlantTowerScreenHandler)

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

val SCREWDRIVER_HANDLER = ScreenHandlerRegistry.registerExtended(ScrewdriverScreenHandler.SCREEN_ID) { syncId, inv, buf ->
    val pos = buf.readBlockPos()
    val itemConfig = SideConfiguration(ConfigurationType.ITEM)
    if (buf.readBoolean())
        itemConfig.readBuf(buf)
    val fluidConfig = SideConfiguration(ConfigurationType.FLUID)
    if (buf.readBoolean())
        fluidConfig.readBuf(buf)
    val energyConfig = SideConfiguration(ConfigurationType.ENERGY)
    if (buf.readBoolean())
        energyConfig.readBuf(buf)

    val map = EnumMap<ConfigurationType, SideConfiguration>(ConfigurationType::class.java)
    map[ConfigurationType.ITEM] = itemConfig
    map[ConfigurationType.FLUID] = fluidConfig
    map[ConfigurationType.ENERGY] = energyConfig
    ScrewdriverScreenHandler(syncId, inv, ScreenHandlerContext.create(inv.player.world, pos), map)
} as ExtendedScreenHandlerType<ScrewdriverScreenHandler>

val CABINET_HANDLER = CabinetScreenHandler.SCREEN_ID.registerScreenHandler(::CabinetScreenHandler)