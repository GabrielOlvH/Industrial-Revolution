package me.steven.indrev.registry

import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.blockentities.battery.BatteryBlockEntity
import me.steven.indrev.blockentities.cables.CableBlockEntity
import me.steven.indrev.blockentities.crafters.CompressorBlockEntity
import me.steven.indrev.blockentities.crafters.ElectricFurnaceBlockEntity
import me.steven.indrev.blockentities.crafters.InfuserBlockEntity
import me.steven.indrev.blockentities.crafters.PulverizerBlockEntity
import me.steven.indrev.blockentities.generators.CoalGeneratorBlockEntity
import me.steven.indrev.blockentities.generators.SolarGeneratorBlockEntity
import me.steven.indrev.blockentities.miner.MinerBlockEntity
import me.steven.indrev.blocks.CableBlock
import me.steven.indrev.blocks.InterfacedMachineBlock
import me.steven.indrev.blocks.MachineBlock
import me.steven.indrev.gui.battery.BatteryScreen
import me.steven.indrev.gui.compressor.CompressorScreen
import me.steven.indrev.gui.furnace.ElectricFurnaceScreen
import me.steven.indrev.gui.generators.CoalGeneratorScreen
import me.steven.indrev.gui.infuser.InfuserScreen
import me.steven.indrev.gui.miner.MinerScreen
import me.steven.indrev.gui.pulverizer.PulverizerScreen
import me.steven.indrev.utils.*
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.fabricmc.fabric.api.tool.attribute.v1.FabricToolTags
import net.minecraft.block.Material
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.item.BlockItem
import net.minecraft.sound.BlockSoundGroup
import net.minecraft.util.Identifier
import java.util.function.Supplier

class MachineRegistry(private val identifier: Identifier, private vararg val tiers: Tier = Tier.values()) {

    private val blockEntities: MutableMap<Tier, BlockEntityType<*>> = mutableMapOf()
    private val baseInternalBuffers: MutableMap<Tier, Double> = mutableMapOf()

    fun register(blockProvider: (Tier) -> MachineBlock, entityProvider: (Tier) -> () -> MachineBlockEntity): MachineRegistry {
        tiers.forEach { tier ->
            val block = blockProvider(tier)
            val blockItem = BlockItem(block, itemSettings())
            val blockEntityType = BlockEntityType.Builder.create(Supplier(entityProvider(tier)), block).build(null)
            identifier("${identifier.path}_${tier.toString().toLowerCase()}").apply {
                block(block)
                item(blockItem)
                blockEntityType(blockEntityType)
            }
            blockEntities[tier] = blockEntityType
        }
        return this
    }

    fun buffer(bufferProvider: (Tier) -> Double): MachineRegistry {
        tiers.forEach { tier ->
            val buffer = bufferProvider(tier)
            baseInternalBuffers[tier] = buffer
        }
        return this
    }

    fun forEach(action: (Tier, BlockEntityType<*>) -> Unit) = blockEntities.forEach(action)

    fun blockEntityType(tier: Tier) = blockEntities[tier] ?: throw IllegalStateException("invalid tier for machine $identifier")

    fun buffer(tier: Tier) = baseInternalBuffers[tier] ?: throw IllegalStateException("invalid tier for machine $identifier")

    companion object {

        private val MACHINE_BLOCK_SETTINGS = {
            FabricBlockSettings.of(Material.METAL).sounds(BlockSoundGroup.METAL).breakByTool(FabricToolTags.PICKAXES).strength(5.0f, 6.0f)
        }


        val COAL_GENERATOR_REGISTRY = MachineRegistry(identifier("coal_generator"), Tier.MK1).also { registry ->
            registry.register(
                { tier ->
                    InterfacedMachineBlock(
                        MACHINE_BLOCK_SETTINGS(), tier, CoalGeneratorScreen.SCREEN_ID, { it is CoalGeneratorBlockEntity }
                    ) { CoalGeneratorBlockEntity() }
                },
                { { CoalGeneratorBlockEntity() } }
            ).buffer { 1000.0 }
        }

        val SOLAR_GENERATOR_REGISTRY = MachineRegistry(identifier("solar_generator"), Tier.MK1, Tier.MK3).also { registry ->
            registry.register(
                { tier -> MachineBlock(MACHINE_BLOCK_SETTINGS(), tier) { SolarGeneratorBlockEntity(tier) } },
                { tier -> { SolarGeneratorBlockEntity(tier) } }
            ).buffer { 32.0 }
        }

        val ELECTRIC_FURNACE_REGISTRY = MachineRegistry(identifier("electric_furnace")).also { registry ->
            registry.register(
                { tier ->
                    InterfacedMachineBlock(
                        MACHINE_BLOCK_SETTINGS(), tier, ElectricFurnaceScreen.SCREEN_ID, { it is ElectricFurnaceBlockEntity }
                    ) { ElectricFurnaceBlockEntity(tier) }
                },
                { tier -> { ElectricFurnaceBlockEntity(tier) } }
            ).buffer { tier ->
                when (tier) {
                    Tier.MK1 -> 1000.0
                    Tier.MK2 -> 5000.0
                    Tier.MK3 -> 10000.0
                    Tier.MK4 -> 100000.0
                }
            }
        }

        val PULVERIZER_REGISTRY = MachineRegistry(identifier("pulverizer")).also { registry ->
            registry.register(
                { tier ->
                    InterfacedMachineBlock(
                        MACHINE_BLOCK_SETTINGS(), tier, PulverizerScreen.SCREEN_ID, { it is PulverizerBlockEntity }
                    ) { PulverizerBlockEntity(tier) }
                },
                { tier -> { PulverizerBlockEntity(tier) } }
            ).buffer { tier ->
                when (tier) {
                    Tier.MK1 -> 1000.0
                    Tier.MK2 -> 5000.0
                    Tier.MK3 -> 10000.0
                    Tier.MK4 -> 100000.0
                }
            }
        }

        val COMPRESSOR_REGISTRY = MachineRegistry(identifier("compressor")).also { registry ->
            registry.register(
                { tier ->
                    InterfacedMachineBlock(
                        MACHINE_BLOCK_SETTINGS(), tier, CompressorScreen.SCREEN_ID, { it is CompressorBlockEntity }
                    ) { CompressorBlockEntity(tier) }
                },
                { tier -> { CompressorBlockEntity(tier) } }
            ).buffer { tier ->
                when (tier) {
                    Tier.MK1 -> 1000.0
                    Tier.MK2 -> 5000.0
                    Tier.MK3 -> 10000.0
                    Tier.MK4 -> 100000.0
                }
            }
        }

        val INFUSER_REGISTRY = MachineRegistry(identifier("infuser")).also { registry ->
            registry.register(
                { tier ->
                    InterfacedMachineBlock(
                        MACHINE_BLOCK_SETTINGS(), tier, InfuserScreen.SCREEN_ID, { it is InfuserBlockEntity }
                    ) { InfuserBlockEntity(tier) }
                },
                { tier -> { InfuserBlockEntity(tier) } }
            ).buffer { tier ->
                when (tier) {
                    Tier.MK1 -> 1000.0
                    Tier.MK2 -> 5000.0
                    Tier.MK3 -> 10000.0
                    Tier.MK4 -> 100000.0
                }
            }
        }

        val CONTAINER_REGISTRY = MachineRegistry(identifier("lazuli_flux_container")).also { registry ->
            registry.register(
                { tier ->
                    InterfacedMachineBlock(
                        MACHINE_BLOCK_SETTINGS(), tier, BatteryScreen.SCREEN_ID, { it is BatteryBlockEntity }
                    ) { BatteryBlockEntity(tier) }
                },
                { tier -> { BatteryBlockEntity(tier) } }
            ).buffer { tier ->
                when (tier) {
                    Tier.MK1 -> 5000.0
                    Tier.MK2 -> 10000.0
                    Tier.MK3 -> 50000.0
                    Tier.MK4 -> 200000.0
                }
            }
        }

        val MINER_REGISTRY = MachineRegistry(identifier("miner")).also { registry ->
            registry.register(
                { tier ->
                    InterfacedMachineBlock(
                        MACHINE_BLOCK_SETTINGS(), tier, MinerScreen.SCREEN_ID, { it is MinerBlockEntity }
                    ) { MinerBlockEntity(tier) }
                },
                { tier -> { MinerBlockEntity(tier) } }
            ).buffer { tier ->
                when (tier) {
                    Tier.MK1 -> 10000.0
                    Tier.MK2 -> 100000.0
                    Tier.MK3 -> 200000.0
                    Tier.MK4 -> 500000.0
                }
            }
        }

        val CABLE_REGISTRY = MachineRegistry(identifier("cable")).also { registry ->
            registry.register(
                { tier -> CableBlock(MACHINE_BLOCK_SETTINGS(), tier) },
                { tier -> { CableBlockEntity(tier) } }
            ).buffer { tier -> tier.io * 2 }
        }
    }
}