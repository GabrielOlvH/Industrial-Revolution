package me.steven.indrev.config

import com.google.gson.GsonBuilder
import me.steven.indrev.IndustrialRevolution
import me.steven.indrev.api.machines.Tier
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.network.PacketByteBuf
import java.io.File

object IRConfig {

    private val gson = GsonBuilder().setPrettyPrinting().create()

    lateinit var generators: Generators
    lateinit var machines: Machines
    lateinit var cables: Cables
    lateinit var upgrades: Upgrades
    lateinit var oregen: OreGen
    lateinit var hud: Hud

    init {
       readConfigs()
    }

    fun readConfigs() {
        generators = readOrCreate("generators.json") { Generators() }
        machines = readOrCreate("machines.json") { Machines() }
        cables = readOrCreate("cables.json") { Cables() }
        upgrades = readOrCreate("upgrades.json") { Upgrades() }
        oregen = readOrCreate("oregen.json") { OreGen() }
        hud = readOrCreate("hud.json") { Hud() }
    }

    private inline fun <reified T> readOrCreate(file: String, default: () -> T): T {
        val dir = File(FabricLoader.getInstance().configDir.toFile(), "indrev")
        if (!dir.exists() && !dir.mkdirs()) {
            IndustrialRevolution.LOGGER.error("Could not create directory, using default configs.")
            return default()
        }
        val f = File(dir, file)
        try {
            if (f.exists())
                return gson.fromJson(f.readLines().joinToString(""), T::class.java)
            else if (!f.createNewFile())
                IndustrialRevolution.LOGGER.error("Failed to create default config file ($file), using default config.")
            else
                f.writeText(gson.toJson(default()))
            return default()
        } catch (e: Exception) {
            IndustrialRevolution.LOGGER.error("Failed to read config file! Using default values.", e)
            return default()
        }
    }
    
    fun writeToClient(buf: PacketByteBuf) {
        val gson = GsonBuilder().create()
        buf.writeString(gson.toJson(generators))
        buf.writeString(gson.toJson(machines))
        buf.writeString(gson.toJson(cables))
        buf.writeString(gson.toJson(upgrades))
        buf.writeString(gson.toJson(oregen))
        buf.writeString(gson.toJson(hud))
    }
    
    fun readFromServer(buf: PacketByteBuf) {
        generators = gson.fromJson(buf.readString(), Generators::class.java)
        machines = gson.fromJson(buf.readString(), Machines::class.java)
        cables = gson.fromJson(buf.readString(), Cables::class.java)
        upgrades = gson.fromJson(buf.readString(), Upgrades::class.java)
        oregen = gson.fromJson(buf.readString(), OreGen::class.java)
        hud = gson.fromJson(buf.readString(), Hud::class.java)
    }
}

class Generators {
    val coalGenerator: GeneratorConfig = GeneratorConfig(16.0, 1.5, 1000.0, Tier.MK1.io)

    val solarGeneratorMk1: GeneratorConfig = GeneratorConfig(8.0, 1.5, 500.0, Tier.MK1.io)

    val solarGeneratorMk3: GeneratorConfig = GeneratorConfig(12.0, 1.5, 1500.0, Tier.MK3.io)

    val heatGenerator: GeneratorConfig = GeneratorConfig(128.0, -1.0, 10000.0, Tier.MK4.io)

    val biomassGenerator: GeneratorConfig = GeneratorConfig(128.0, 1.5, 10000.0, Tier.MK3.io)
}

class GeneratorConfig(
    val ratio: Double = 16.0,
    val temperatureBoost: Double = 1.5,
    override val maxEnergyStored: Double,
    val maxOutput: Double
) : IConfig

class Machines {
    val electricFurnaceMk1: HeatMachineConfig = HeatMachineConfig(4.0, 1.0, 2.0, 1000.0, Tier.MK1.io)

    val electricFurnaceMk2: HeatMachineConfig = HeatMachineConfig(8.0, 2.0, 2.0, 5000.0, Tier.MK2.io)

    val electricFurnaceMk3: HeatMachineConfig = HeatMachineConfig(16.0, 3.0, 2.0, 10000.0, Tier.MK3.io)

    val electricFurnaceMk4: HeatMachineConfig = HeatMachineConfig(64.0, 4.0, 2.0, 100000.0, Tier.MK4.io)

    val electricFurnaceFactory: HeatMachineConfig = HeatMachineConfig(64.0, 4.0, 2.0, 100000.0, 16384.0)

    val pulverizerMk1: HeatMachineConfig = HeatMachineConfig(4.0, 1.0, 2.0, 1000.0, Tier.MK1.io)

    val pulverizerMk2: HeatMachineConfig = HeatMachineConfig(8.0, 2.0, 2.0, 5000.0, Tier.MK2.io)

    val pulverizerMk3: HeatMachineConfig = HeatMachineConfig(16.0, 3.0, 2.0, 10000.0, Tier.MK3.io)

    val pulverizerMk4: HeatMachineConfig = HeatMachineConfig(64.0, 4.0, 2.0, 100000.0, Tier.MK4.io)

    val pulverizerFactory: HeatMachineConfig = HeatMachineConfig(64.0, 4.0, 2.0, 100000.0, 16384.0)

    val compressorMk1: HeatMachineConfig = HeatMachineConfig(4.0, 1.0, 2.0, 1000.0, Tier.MK1.io)

    val compressorMk2: HeatMachineConfig = HeatMachineConfig(8.0, 2.0, 2.0, 5000.0, Tier.MK2.io)

    val compressorMk3: HeatMachineConfig = HeatMachineConfig(16.0, 3.0, 2.0, 10000.0, Tier.MK3.io)

    val compressorMk4: HeatMachineConfig = HeatMachineConfig(64.0, 4.0, 2.0, 100000.0, Tier.MK4.io)

    val compressorFactory: HeatMachineConfig = HeatMachineConfig(64.0, 4.0, 2.0, 100000.0, 16384.0)

    val sawmillMk1: HeatMachineConfig = HeatMachineConfig(4.0, 1.0, 2.0, 1000.0, Tier.MK1.io)

    val sawmillMk2: HeatMachineConfig = HeatMachineConfig(8.0, 2.0, 2.0, 5000.0, Tier.MK2.io)

    val sawmillMk3: HeatMachineConfig = HeatMachineConfig(16.0, 3.0, 2.0, 10000.0, Tier.MK3.io)

    val sawmillMk4: HeatMachineConfig = HeatMachineConfig(64.0, 4.0, 2.0, 100000.0, Tier.MK4.io)

    val infuserMk1: HeatMachineConfig = HeatMachineConfig(4.0, 1.0, 2.0, 1000.0, Tier.MK1.io)

    val infuserMk2: HeatMachineConfig = HeatMachineConfig(8.0, 2.0, 2.0,5000.0, Tier.MK2.io)

    val infuserMk3: HeatMachineConfig = HeatMachineConfig(16.0, 3.0, 2.0,10000.0, Tier.MK3.io)

    val infuserMk4: HeatMachineConfig = HeatMachineConfig(64.0, 4.0, 2.0,100000.0, Tier.MK4.io)

    val infuserFactory: HeatMachineConfig = HeatMachineConfig(64.0, 4.5, 2.0, 100000.0, 16384.0)

    val recycler: MachineConfig = MachineConfig(8.0, 2.5, 50000.0, Tier.MK2.io)

    val chopperMk1: MachineConfig = MachineConfig(16.0, 50.0, 1000.0, Tier.MK1.io)

    val chopperMk2: MachineConfig = MachineConfig(32.0, 40.0, 5000.0, Tier.MK2.io)

    val chopperMk3: MachineConfig = MachineConfig(64.0, 30.0, 25000.0, Tier.MK3.io)

    val chopperMk4: MachineConfig = MachineConfig(128.0, 20.0, 50000.0, Tier.MK4.io)

    val farmerMk1: MachineConfig = MachineConfig(16.0, 50.0, 1000.0, Tier.MK1.io)

    val farmerMk2: MachineConfig = MachineConfig(32.0, 40.0, 5000.0, Tier.MK2.io)

    val farmerMk3: MachineConfig = MachineConfig(64.0, 30.0, 25000.0, Tier.MK3.io)

    val farmerMk4: MachineConfig = MachineConfig(128.0, 20.0, 50000.0, Tier.MK4.io)

    val rancherMk1: MachineConfig = MachineConfig(16.0, 50.0, 1000.0, Tier.MK1.io)

    val rancherMk2: MachineConfig = MachineConfig(32.0, 40.0, 5000.0, Tier.MK2.io)

    val rancherMk3: MachineConfig = MachineConfig(64.0, 30.0, 25000.0, Tier.MK3.io)

    val rancherMk4: MachineConfig = MachineConfig(128.0, 20.0, 50000.0, Tier.MK4.io)

    val miner: MachineConfig = MachineConfig(64.0, 100.0, 50000.0, Tier.MK4.io)

    val drill: Double = 256.0

    val fishingMk2: MachineConfig = MachineConfig(8.0, 400.0, 50000.0, Tier.MK2.io)

    val fishingMk3: MachineConfig = MachineConfig(16.0, 400.0, 50000.0, Tier.MK3.io)

    val fishingMk4: MachineConfig = MachineConfig(64.0, 500.0, 50000.0, Tier.MK4.io)

    val drain: MachineConfig = MachineConfig(4.0, 20.0, 1000.0, Tier.MK1.io)

    val pump: MachineConfig = MachineConfig(4.0, 20.0, 50.0, Tier.MK1.io)

    val smelter: HeatMachineConfig = HeatMachineConfig(64.0, 4.0, 2.0,50000.0, Tier.MK4.io)

    val condenser: MachineConfig = MachineConfig(64.0, 4.0, 50000.0, Tier.MK4.io)

    val fluidInfuserMk1: MachineConfig = MachineConfig(4.0, 1.0, 1000.0, Tier.MK1.io)

    val fluidInfuserMk2: MachineConfig = MachineConfig(8.0, 2.0, 5000.0, Tier.MK2.io)

    val fluidInfuserMk3: MachineConfig = MachineConfig(16.0, 3.0, 10000.0, Tier.MK3.io)

    val fluidInfuserMk4: MachineConfig = MachineConfig(64.0, 4.0, 100000.0, Tier.MK4.io)

    val modularWorkbench: MachineConfig = MachineConfig(64.0, 1.0, 5000.0, Tier.MK4.io)

    val laser: MachineConfig = MachineConfig(4096.0, 1.0, 2500000.0, 16384.0)

    val lazuliFluxContainerMk1: LFCConfig = LFCConfig(10000.0, 128.0, 128.0)

    val lazuliFluxContainerMk2: LFCConfig = LFCConfig(100000.0, 512.0, 512.0)

    val lazuliFluxContainerMk3: LFCConfig = LFCConfig(1000000.0, 4096.0, 4096.0)

    val lazuliFluxContainerMk4: LFCConfig = LFCConfig(10000000.0, 16384.0, 16384.0)
}

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

class LFCConfig(
    override val maxEnergyStored: Double,
    val maxInput: Double,
    val maxOutput: Double
): IConfig

interface IConfig {
    val maxEnergyStored: Double
}

class Cables {
    val cableMk1 = 128.0
    val cableMk2 = 512.0
    val cableMk3 = 4096.0
    val cableMk4 = 16384.0

    val itemPipeMk1 = 32
    val itemPipeMk2 = 64
    val itemPipeMk3 = 128
    val itemPipeMk4 = 256

    val fluidPipeMk1 = 1
    val fluidPipeMk2 = 2
    val fluidPipeMk3 = 4
    val fluidPipeMk4 = 8
}

class Upgrades  {
    val speedUpgradeModifier = 6.5
    val energyUpgradeModifier = 1.12
    val bufferUpgradeModifier = 25000.0
}

class OreGen  {
    val copper = true
    val tin = true
    val nikolite = true
    val lead = true
    val silver = true
    val tungsten = true
    val sulfuricAcidLake = true
    val sulfurCrystals = true
}

class Hud {
    val renderPosX = 0
    val renderPosY = 0
}