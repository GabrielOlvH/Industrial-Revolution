package me.steven.indrev.config

import me.sargunvohra.mcmods.autoconfig1u.ConfigData
import me.sargunvohra.mcmods.autoconfig1u.annotation.Config
import me.sargunvohra.mcmods.autoconfig1u.annotation.ConfigEntry
import me.sargunvohra.mcmods.autoconfig1u.serializer.PartitioningSerializer
import me.steven.indrev.utils.Tier

@Config(name = "indrev")
class IRConfig : PartitioningSerializer.GlobalData() {

    @ConfigEntry.Category(value = "generators")
    @ConfigEntry.Gui.TransitiveObject
    var generators: Generators = Generators()

    @ConfigEntry.Category(value = "machines")
    @ConfigEntry.Gui.TransitiveObject
    val machines: Machines = Machines()

    @ConfigEntry.Category(value = "cables")
    @ConfigEntry.Gui.TransitiveObject
    val cables: Cables = Cables()

    @ConfigEntry.Category(value = "upgrades")
    @ConfigEntry.Gui.TransitiveObject
    val upgrades: Upgrades = Upgrades()

    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Category(value = "compatibility")
    @ConfigEntry.Gui.TransitiveObject
    val compatibility: Compatibility = Compatibility()

    @ConfigEntry.Category(value = "oregen")
    @ConfigEntry.Gui.TransitiveObject
    val oregen: OreGen = OreGen()

    @ConfigEntry.Category(value = "hud")
    @ConfigEntry.Gui.TransitiveObject
    val hud: Hud = Hud()
}

@Config(name = "generators")
class Generators : ConfigData {
    @ConfigEntry.Gui.CollapsibleObject
    val coalGenerator: GeneratorConfig = GeneratorConfig(16.0, 1.5, 1000.0, Tier.MK1.io)

    @ConfigEntry.Gui.CollapsibleObject
    val solarGeneratorMk1: GeneratorConfig = GeneratorConfig(8.0, 1.5, 500.0, Tier.MK1.io)

    @ConfigEntry.Gui.CollapsibleObject
    val solarGeneratorMk3: GeneratorConfig = GeneratorConfig(12.0, 1.5, 1500.0, Tier.MK3.io)

    @ConfigEntry.Gui.CollapsibleObject
    val heatGenerator: GeneratorConfig = GeneratorConfig(64.0, -1.0, 20000.0, Tier.MK4.io)

    @ConfigEntry.Gui.CollapsibleObject
    val biomassGenerator: GeneratorConfig = GeneratorConfig(128.0, 1.5, 20000.0, Tier.MK3.io)
}

class GeneratorConfig(
    val ratio: Double = 16.0,
    val temperatureBoost: Double = 1.5,
    override val maxEnergyStored: Double,
    val maxOutput: Double
) : IConfig

@Config(name = "machines")
class Machines : ConfigData {
    @ConfigEntry.Gui.CollapsibleObject
    val electricFurnaceMk1: HeatMachineConfig = HeatMachineConfig(4.0, 1.5, 2.0, 1000.0, Tier.MK1.io)

    @ConfigEntry.Gui.CollapsibleObject
    val electricFurnaceMk2: HeatMachineConfig = HeatMachineConfig(8.0, 2.5, 2.0, 5000.0, Tier.MK2.io)

    @ConfigEntry.Gui.CollapsibleObject
    val electricFurnaceMk3: HeatMachineConfig = HeatMachineConfig(16.0, 3.5, 2.0, 10000.0, Tier.MK3.io)

    @ConfigEntry.Gui.CollapsibleObject
    val electricFurnaceMk4: HeatMachineConfig = HeatMachineConfig(64.0, 4.5, 2.0, 100000.0, Tier.MK4.io)

    @ConfigEntry.Gui.CollapsibleObject
    val pulverizerMk1: HeatMachineConfig = HeatMachineConfig(4.0, 1.5, 2.0, 1000.0, Tier.MK1.io)

    @ConfigEntry.Gui.CollapsibleObject
    val pulverizerMk2: HeatMachineConfig = HeatMachineConfig(8.0, 2.5, 2.0, 5000.0, Tier.MK2.io)

    @ConfigEntry.Gui.CollapsibleObject
    val pulverizerMk3: HeatMachineConfig = HeatMachineConfig(16.0, 3.5, 2.0, 10000.0, Tier.MK3.io)

    @ConfigEntry.Gui.CollapsibleObject
    val pulverizerMk4: HeatMachineConfig = HeatMachineConfig(64.0, 4.5, 2.0, 100000.0, Tier.MK4.io)

    @ConfigEntry.Gui.CollapsibleObject
    val compressorMk1: HeatMachineConfig = HeatMachineConfig(4.0, 1.5, 2.0, 1000.0, Tier.MK1.io)

    @ConfigEntry.Gui.CollapsibleObject
    val compressorMk2: HeatMachineConfig = HeatMachineConfig(8.0, 2.5, 2.0, 5000.0, Tier.MK2.io)

    @ConfigEntry.Gui.CollapsibleObject
    val compressorMk3: HeatMachineConfig = HeatMachineConfig(16.0, 3.5, 2.0, 10000.0, Tier.MK3.io)

    @ConfigEntry.Gui.CollapsibleObject
    val compressorMk4: HeatMachineConfig = HeatMachineConfig(64.0, 4.5, 2.0, 100000.0, Tier.MK4.io)

    @ConfigEntry.Gui.CollapsibleObject
    val sawmillMk1: HeatMachineConfig = HeatMachineConfig(4.0, 1.5, 2.0, 1000.0, Tier.MK1.io)

    @ConfigEntry.Gui.CollapsibleObject
    val sawmillMk2: HeatMachineConfig = HeatMachineConfig(8.0, 2.5, 2.0, 5000.0, Tier.MK2.io)

    @ConfigEntry.Gui.CollapsibleObject
    val sawmillMk3: HeatMachineConfig = HeatMachineConfig(16.0, 3.5, 2.0, 10000.0, Tier.MK3.io)

    @ConfigEntry.Gui.CollapsibleObject
    val sawmillMk4: HeatMachineConfig = HeatMachineConfig(64.0, 4.5, 2.0, 100000.0, Tier.MK4.io)

    @ConfigEntry.Gui.CollapsibleObject
    val infuserMk1: MachineConfig = MachineConfig(4.0, 1.5, 1000.0, Tier.MK1.io)

    @ConfigEntry.Gui.CollapsibleObject
    val infuserMk2: MachineConfig = MachineConfig(8.0, 2.5, 5000.0, Tier.MK2.io)

    @ConfigEntry.Gui.CollapsibleObject
    val infuserMk3: MachineConfig = MachineConfig(16.0, 3.5, 10000.0, Tier.MK3.io)

    @ConfigEntry.Gui.CollapsibleObject
    val infuserMk4: MachineConfig = MachineConfig(64.0, 4.5, 100000.0, Tier.MK4.io)

    @ConfigEntry.Gui.CollapsibleObject
    val recycler: MachineConfig = MachineConfig(8.0, 2.5, 50000.0, Tier.MK2.io)

    @ConfigEntry.Gui.CollapsibleObject
    val chopperMk1: MachineConfig = MachineConfig(16.0, 50.0, 1000.0, Tier.MK1.io)

    @ConfigEntry.Gui.CollapsibleObject
    val chopperMk2: MachineConfig = MachineConfig(32.0, 40.0, 5000.0, Tier.MK2.io)

    @ConfigEntry.Gui.CollapsibleObject
    val chopperMk3: MachineConfig = MachineConfig(64.0, 30.0, 25000.0, Tier.MK3.io)

    @ConfigEntry.Gui.CollapsibleObject
    val chopperMk4: MachineConfig = MachineConfig(128.0, 20.0, 50000.0, Tier.MK4.io)

    @ConfigEntry.Gui.CollapsibleObject
    val farmerMk1: MachineConfig = MachineConfig(16.0, 50.0, 1000.0, Tier.MK1.io)

    @ConfigEntry.Gui.CollapsibleObject
    val farmerMk2: MachineConfig = MachineConfig(32.0, 40.0, 5000.0, Tier.MK2.io)

    @ConfigEntry.Gui.CollapsibleObject
    val farmerMk3: MachineConfig = MachineConfig(64.0, 30.0, 25000.0, Tier.MK3.io)

    @ConfigEntry.Gui.CollapsibleObject
    val farmerMk4: MachineConfig = MachineConfig(128.0, 20.0, 50000.0, Tier.MK4.io)

    @ConfigEntry.Gui.CollapsibleObject
    val rancherMk1: MachineConfig = MachineConfig(16.0, 50.0, 1000.0, Tier.MK1.io)

    @ConfigEntry.Gui.CollapsibleObject
    val rancherMk2: MachineConfig = MachineConfig(32.0, 40.0, 5000.0, Tier.MK2.io)

    @ConfigEntry.Gui.CollapsibleObject
    val rancherMk3: MachineConfig = MachineConfig(64.0, 30.0, 25000.0, Tier.MK3.io)

    @ConfigEntry.Gui.CollapsibleObject
    val rancherMk4: MachineConfig = MachineConfig(128.0, 20.0, 50000.0, Tier.MK4.io)

    @ConfigEntry.Gui.CollapsibleObject
    val miner: MachineConfig = MachineConfig(64.0, 100.0, 50000.0, Tier.MK4.io)

    @ConfigEntry.Gui.CollapsibleObject
    val fishingMk2: MachineConfig = MachineConfig(8.0, 500.0, 50000.0, Tier.MK2.io)

    @ConfigEntry.Gui.CollapsibleObject
    val fishingMk3: MachineConfig = MachineConfig(16.0, 300.0, 50000.0, Tier.MK3.io)

    @ConfigEntry.Gui.CollapsibleObject
    val fishingMk4: MachineConfig = MachineConfig(64.0, 200.0, 50000.0, Tier.MK4.io)

    @ConfigEntry.Gui.CollapsibleObject
    val drain: MachineConfig = MachineConfig(4.0, 20.0, 1000.0, Tier.MK1.io)

    @ConfigEntry.Gui.CollapsibleObject
    val smelter: MachineConfig = MachineConfig(64.0, 4.0, 50000.0, Tier.MK4.io)

    @ConfigEntry.Gui.CollapsibleObject
    val condenser: MachineConfig = MachineConfig(64.0, 4.0, 50000.0, Tier.MK4.io)

    @ConfigEntry.Gui.CollapsibleObject
    val fluidInfuserMk1: MachineConfig = MachineConfig(4.0, 1.5, 1000.0, Tier.MK1.io)

    @ConfigEntry.Gui.CollapsibleObject
    val fluidInfuserMk2: MachineConfig = MachineConfig(8.0, 2.5, 5000.0, Tier.MK2.io)

    @ConfigEntry.Gui.CollapsibleObject
    val fluidInfuserMk3: MachineConfig = MachineConfig(16.0, 3.5, 10000.0, Tier.MK3.io)

    @ConfigEntry.Gui.CollapsibleObject
    val fluidInfuserMk4: MachineConfig = MachineConfig(64.0, 4.5, 100000.0, Tier.MK4.io)

    @ConfigEntry.Gui.CollapsibleObject
    val modularWorkbench: MachineConfig = MachineConfig(16.0, 1.0, 500000.0, Tier.MK4.io)
}

// this is so auto config actually generates the config properly
class HeatMachineConfig(
    override val energyCost: Double,
    override val processSpeed: Double,
    val processTemperatureBoost: Double,
    override val maxEnergyStored: Double,
    override val maxInput: Double
) : BasicMachineConfig

open class MachineConfig(
    override val energyCost: Double,
    override val processSpeed: Double,
    override val maxEnergyStored: Double,
    override val maxInput: Double
) : BasicMachineConfig

interface BasicMachineConfig : IConfig {
    val energyCost: Double
    val processSpeed: Double
    val maxInput: Double
}

interface IConfig {
    val maxEnergyStored: Double
}

@Config(name = "cables")
class Cables : ConfigData {
    @ConfigEntry.Gui.CollapsibleObject
    val cableMk1 = CableConfig(Tier.MK1.io, Tier.MK1.io)

    @ConfigEntry.Gui.CollapsibleObject
    val cableMk2 = CableConfig(Tier.MK2.io, Tier.MK2.io)

    @ConfigEntry.Gui.CollapsibleObject
    val cableMk3 = CableConfig(Tier.MK3.io, Tier.MK3.io)

    @ConfigEntry.Gui.CollapsibleObject
    val cableMk4 = CableConfig(Tier.MK4.io, Tier.MK4.io)
}

class CableConfig(val maxOutput: Double, val maxInput: Double)

@Config(name = "upgrades")
class Upgrades : ConfigData {
    val speedUpgradeModifier = 4.0
    val energyUpgradeModifier = 0.1
    val bufferUpgradeModifier = 25000.0
}

@Config(name = "compat")
class Compatibility : ConfigData {
    val targetModId: String = ""
}

@Config(name = "oregen")
class OreGen : ConfigData {
    val copper = true
    val tin = true
    val nikolite = true
    val sulfuricAcidLake = true
    val sulfurCrystals = true
}

@Config(name = "hud")
class Hud : ConfigData {
    val renderPosX = 0
    val renderPosY = 0
    val renderHelmetArmor = true
    val renderHelmetShield = true
    val renderChestplateArmor = true
    val renderChestplateShield = true
    val renderLeggingsArmor = true
    val renderLeggingsShield = true
    val renderBootsArmor = true
    val renderBootsShield = true
}