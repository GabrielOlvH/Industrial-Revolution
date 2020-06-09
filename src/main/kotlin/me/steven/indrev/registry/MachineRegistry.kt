package me.steven.indrev.registry

import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.blockentities.battery.BatteryBlockEntity
import me.steven.indrev.blockentities.cables.CableBlockEntity
import me.steven.indrev.blockentities.crafters.*
import me.steven.indrev.blockentities.generators.*
import me.steven.indrev.blockentities.miner.MinerBlockEntity
import me.steven.indrev.blocks.CableBlock
import me.steven.indrev.blocks.FacingMachineBlock
import me.steven.indrev.blocks.MachineBlock
import me.steven.indrev.blocks.nuclear.NuclearReactorCore
import me.steven.indrev.blocks.nuclear.NuclearReactorPart
import me.steven.indrev.gui.battery.BatteryScreen
import me.steven.indrev.gui.biomassgen.BiomassGeneratorController
import me.steven.indrev.gui.coalgenerator.CoalGeneratorScreen
import me.steven.indrev.gui.compressor.CompressorScreen
import me.steven.indrev.gui.furnace.ElectricFurnaceScreen
import me.steven.indrev.gui.infuser.InfuserScreen
import me.steven.indrev.gui.miner.MinerScreen
import me.steven.indrev.gui.nuclearreactor.NuclearReactorScreen
import me.steven.indrev.gui.pulverizer.PulverizerScreen
import me.steven.indrev.gui.recycler.RecyclerScreen
import me.steven.indrev.gui.solargenerator.SolarGeneratorScreen
import me.steven.indrev.utils.*
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.fabricmc.fabric.api.tool.attribute.v1.FabricToolTags
import net.minecraft.block.Block
import net.minecraft.block.Material
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.item.BlockItem
import net.minecraft.sound.BlockSoundGroup
import net.minecraft.util.Identifier
import java.util.function.Supplier

class MachineRegistry(private val identifier: Identifier, private vararg val tiers: Tier = Tier.values()) {

    private val blocks: MutableMap<Tier, Block> = mutableMapOf()
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
            blocks[tier] = block
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

    fun blockEntityType(tier: Tier) = blockEntities[tier]
        ?: throw IllegalStateException("invalid tier for machine $identifier")

    fun block(tier: Tier) = blocks[tier]
        ?: throw java.lang.IllegalStateException("invalid tier for machine $identifier")

    fun buffer(tier: Tier) = baseInternalBuffers[tier]
        ?: throw IllegalStateException("invalid tier for machine $identifier")

    companion object {

        private val MACHINE_BLOCK_SETTINGS = {
            FabricBlockSettings.of(Material.METAL).sounds(BlockSoundGroup.METAL).breakByTool(FabricToolTags.PICKAXES).strength(5.0f, 6.0f)
        }

        val COAL_GENERATOR_REGISTRY = MachineRegistry(identifier("coal_generator"), Tier.MK1).also { registry ->
            registry.register(
                { tier ->
                    FacingMachineBlock(
                        MACHINE_BLOCK_SETTINGS(), tier, CoalGeneratorScreen.SCREEN_ID
                    ) { CoalGeneratorBlockEntity() }
                },
                { { CoalGeneratorBlockEntity() } }
            ).buffer { 1000.0 }
        }

        val SOLAR_GENERATOR_REGISTRY = MachineRegistry(identifier("solar_generator"), Tier.MK1, Tier.MK3).also { registry ->
            registry.register(
                { tier ->
                    MachineBlock(
                        MACHINE_BLOCK_SETTINGS(), tier, SolarGeneratorScreen.SCREEN_ID
                    ) { SolarGeneratorBlockEntity(tier) }
                },
                { tier -> { SolarGeneratorBlockEntity(tier) } }
            ).buffer { 32.0 }
        }

        val BIOMASS_GENERATOR_REGISTRY = MachineRegistry(identifier("biomass_generator"), Tier.MK3).also { registry ->
            registry.register(
                { tier ->
                    FacingMachineBlock(
                        MACHINE_BLOCK_SETTINGS(), tier, BiomassGeneratorController.SCREEN_ID
                    ) { BiomassGeneratorBlockEntity(tier) }
                },
                { tier -> { BiomassGeneratorBlockEntity(tier) } }
            ).buffer { 20000.0 }
        }

        val NUCLEAR_GENERATOR_REGISTRY = MachineRegistry(identifier("nuclear_generator"), Tier.MK4).also { registry ->
            registry.register(
                {
                    NuclearReactorCore(MACHINE_BLOCK_SETTINGS(), NuclearReactorScreen.SCREEN_ID)
                    { NuclearReactorBlockEntity() }
                },
                { { NuclearReactorBlockEntity() } }
            ).buffer { 100000.0 }
        }

        val NUCLEAR_PART_BLOCK = NuclearReactorPart(MACHINE_BLOCK_SETTINGS()).also { identifier("nuclear_reactor_part").block(it) }
        val NUCLEAR_PART_BLOCK_ITEM = BlockItem(NUCLEAR_PART_BLOCK, itemSettings()).also { identifier("nuclear_reactor_part").item(it) }
        val NUCLEAR_PART_BLOCK_ENTITY =
            BlockEntityType.Builder.create(Supplier { NuclearReactorProxyBlockEntity() }, NUCLEAR_PART_BLOCK).build(null).also { identifier("nuclear_reactor_part").blockEntityType(it) }

        val ELECTRIC_FURNACE_REGISTRY = MachineRegistry(identifier("electric_furnace")).also { registry ->
            registry.register(
                { tier ->
                    FacingMachineBlock(
                        MACHINE_BLOCK_SETTINGS(), tier, ElectricFurnaceScreen.SCREEN_ID
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
                    FacingMachineBlock(
                        MACHINE_BLOCK_SETTINGS(), tier, PulverizerScreen.SCREEN_ID
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
                    FacingMachineBlock(
                        MACHINE_BLOCK_SETTINGS(), tier, CompressorScreen.SCREEN_ID
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
                    FacingMachineBlock(
                        MACHINE_BLOCK_SETTINGS(), tier, InfuserScreen.SCREEN_ID
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
                    FacingMachineBlock(
                        MACHINE_BLOCK_SETTINGS(), tier, BatteryScreen.SCREEN_ID, true
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

        val MINER_REGISTRY = MachineRegistry(identifier("miner"), Tier.MK4).also { registry ->
            registry.register(
                { tier ->
                    FacingMachineBlock(
                        MACHINE_BLOCK_SETTINGS(), tier, MinerScreen.SCREEN_ID
                    ) { MinerBlockEntity(tier) }
                },
                { tier -> { MinerBlockEntity(tier) } }
            ).buffer { 500000.0 }
        }

        val RECYCLER_REGISTRY = MachineRegistry(identifier("recycler"), Tier.MK2).also { registry ->
            registry.register(
                { tier ->
                    FacingMachineBlock(
                        MACHINE_BLOCK_SETTINGS(), tier, RecyclerScreen.SCREEN_ID
                    ) { RecyclerBlockEntity(tier) }
                },
                { tier -> { RecyclerBlockEntity(tier) } }
            ).buffer { 50000.0 }
        }

        val CABLE_REGISTRY = MachineRegistry(identifier("cable")).also { registry ->
            registry.register(
                { tier -> CableBlock(MACHINE_BLOCK_SETTINGS(), tier) },
                { tier -> { CableBlockEntity(tier) } }
            ).buffer { tier -> tier.io * 2 }
        }
    }
}