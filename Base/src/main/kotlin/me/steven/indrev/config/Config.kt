package me.steven.indrev.config

import me.steven.indrev.utils.Tier

val machinesConfig = Machines()

class Config {

}

class Machines {
    val solidFuelGenerator = GeneratorConfig(16, 1.5, 1000, Tier.MK1.transferCapacity)

    val electricFurnace = craftingMachine()

    val pulverizer = craftingMachine()

    val alloySmelter = craftingMachine()

    val chemicalInfuser = craftingMachine()

    val compressor = craftingMachine()
}

open class MachineConfig(open val capacity: Long, open val maxInput: Long, open val maxOutput: Long)

open class GeneratorConfig(val generationRatio: Long, val temperatureBoost: Double, capacity: Long, maxOutput: Long) : MachineConfig(capacity, 0, maxOutput)

open class CraftingMachineConfig(open val processingSpeed: Double, open val temperatureBoost: Double, open val cost: Long, capacity: Long, maxInput: Long) : MachineConfig(capacity, maxInput, 0)

open class TieredConfig<T : MachineConfig>(val mk1: T, val mk2: T, val mk3: T, val mk4: T) {
    fun get(tier: Tier): T {
        return when (tier) {
            Tier.MK1 -> mk1
            Tier.MK2 -> mk2
            Tier.MK3 -> mk3
            Tier.MK4 -> mk4
            Tier.CREATIVE -> mk4
        }
    }
}

private fun craftingMachine() = TieredConfig(mk1CraftingMachine(), mk2CraftingMachine(), mk3CraftingMachine(), mk4CraftingMachine())

private fun mk1CraftingMachine() = CraftingMachineConfig(1.0, 0.5, 8, 1000, Tier.MK1.transferCapacity)

private fun mk2CraftingMachine() = CraftingMachineConfig(2.0, 0.5, 16, 4000, Tier.MK2.transferCapacity)

private fun mk3CraftingMachine() = CraftingMachineConfig(4.0, 0.5, 32, 8000, Tier.MK3.transferCapacity)

private fun mk4CraftingMachine() = CraftingMachineConfig(8.0, 0.5, 64, 16000, Tier.MK4.transferCapacity)