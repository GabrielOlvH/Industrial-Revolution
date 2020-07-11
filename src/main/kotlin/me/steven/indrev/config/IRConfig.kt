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

}

@Config(name = "generators")
class Generators : ConfigData {
    @ConfigEntry.Gui.CollapsibleObject
    val coalGenerator: Generator = Generator(16.0, 1.5, 1000.0, Tier.MK1.io)

    @ConfigEntry.Gui.CollapsibleObject
    val solarGeneratorMk1: Generator = Generator(16.0, 1.5, 500.0, Tier.MK1.io)

    @ConfigEntry.Gui.CollapsibleObject
    val solarGeneratorMk3: Generator = Generator(64.0, 1.5, 1500.0, Tier.MK3.io)

    @ConfigEntry.Gui.CollapsibleObject
    val heatGenerator: Generator = Generator(64.0, -1.0, 20000.0, Tier.MK4.io)

    @ConfigEntry.Gui.CollapsibleObject
    val biomassGenerator: Generator = Generator(128.0, 1.5, 20000.0, Tier.MK3.io)
}

class Generator(
    val ratio: Double = 16.0,
    val temperatureBoost: Double = 1.5,
    val maxEnergyStored: Double,
    val maxOutput: Double
)

@Config(name = "machines")
class Machines : ConfigData {
    @ConfigEntry.Gui.CollapsibleObject
    val electricFurnaceMk1: Machine = Machine(4.0, 1.0, 2.0, 1000.0, Tier.MK1.io)

    @ConfigEntry.Gui.CollapsibleObject
    val electricFurnaceMk2: Machine = Machine(8.0, 2.0, 2.0, 5000.0, Tier.MK2.io)

    @ConfigEntry.Gui.CollapsibleObject
    val electricFurnaceMk3: Machine = Machine(16.0, 3.0, 2.0, 10000.0, Tier.MK3.io)

    @ConfigEntry.Gui.CollapsibleObject
    val electricFurnaceMk4: Machine = Machine(64.0, 4.0, 2.0, 100000.0, Tier.MK4.io)

    @ConfigEntry.Gui.CollapsibleObject
    val pulverizerMk1: Machine = Machine(4.0, 1.0, 2.0, 1000.0, Tier.MK1.io)

    @ConfigEntry.Gui.CollapsibleObject
    val pulverizerMk2: Machine = Machine(8.0, 2.0, 2.0, 5000.0, Tier.MK2.io)

    @ConfigEntry.Gui.CollapsibleObject
    val pulverizerMk3: Machine = Machine(16.0, 3.0, 2.0, 10000.0, Tier.MK3.io)

    @ConfigEntry.Gui.CollapsibleObject
    val pulverizerMk4: Machine = Machine(64.0, 4.0, 2.0, 100000.0, Tier.MK4.io)

    @ConfigEntry.Gui.CollapsibleObject
    val compressorMk1: Machine = Machine(4.0, 1.0, 2.0, 1000.0, Tier.MK1.io)

    @ConfigEntry.Gui.CollapsibleObject
    val compressorMk2: Machine = Machine(8.0, 2.0, 2.0, 5000.0, Tier.MK2.io)

    @ConfigEntry.Gui.CollapsibleObject
    val compressorMk3: Machine = Machine(16.0, 3.0, 2.0, 10000.0, Tier.MK3.io)

    @ConfigEntry.Gui.CollapsibleObject
    val compressorMk4: Machine = Machine(64.0, 4.0, 2.0, 100000.0, Tier.MK4.io)

    @ConfigEntry.Gui.CollapsibleObject
    val infuserMk1: Machine = Machine(4.0, 1.0, 2.0, 1000.0, Tier.MK1.io)

    @ConfigEntry.Gui.CollapsibleObject
    val infuserMk2: Machine = Machine(8.0, 2.0, 2.0, 5000.0, Tier.MK2.io)

    @ConfigEntry.Gui.CollapsibleObject
    val infuserMk3: Machine = Machine(16.0, 3.0, 2.0, 10000.0, Tier.MK3.io)

    @ConfigEntry.Gui.CollapsibleObject
    val infuserMk4: Machine = Machine(64.0, 4.0, 2.0, 100000.0, Tier.MK4.io)

    @ConfigEntry.Gui.CollapsibleObject
    val recycler: Machine = Machine(8.0, 2.0, 2.0, 50000.0, Tier.MK2.io)

    @ConfigEntry.Gui.CollapsibleObject
    val chopper: Machine = Machine(64.0, 3.0, 4.0, 50000.0, Tier.MK4.io)

    @ConfigEntry.Gui.CollapsibleObject
    val rancher: Machine = Machine(64.0, 3.0, 4.0, 50000.0, Tier.MK4.io)

    @ConfigEntry.Gui.CollapsibleObject
    val miner: Machine = Machine(64.0, 0.3, 0.5, 50000.0, Tier.MK4.io)
}

class Machine(
    val energyCost: Double,
    val processSpeed: Double,
    val processTemperatureBoost: Double,
    val maxEnergyStored: Double,
    val maxInput: Double
)

@Config(name = "cables")
class Cables : ConfigData {
    @ConfigEntry.Gui.CollapsibleObject
    val cableMk1 = Cable(Tier.MK1.io, Tier.MK1.io)

    @ConfigEntry.Gui.CollapsibleObject
    val cableMk2 = Cable(Tier.MK2.io, Tier.MK2.io)

    @ConfigEntry.Gui.CollapsibleObject
    val cableMk3 = Cable(Tier.MK3.io, Tier.MK3.io)

    @ConfigEntry.Gui.CollapsibleObject
    val cableMk4 = Cable(Tier.MK4.io, Tier.MK4.io)
}

class Cable(val maxOutput: Double, val maxInput: Double)