package me.steven.indrev.registry

import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.blockentities.battery.BatteryBlockEntity
import me.steven.indrev.blockentities.battery.ChargePadBlockEntity
import me.steven.indrev.blockentities.cables.CableBlockEntity
import me.steven.indrev.blockentities.crafters.*
import me.steven.indrev.blockentities.farms.ChopperBlockEntity
import me.steven.indrev.blockentities.farms.MinerBlockEntity
import me.steven.indrev.blockentities.farms.RancherBlockEntity
import me.steven.indrev.blockentities.generators.BiomassGeneratorBlockEntity
import me.steven.indrev.blockentities.generators.CoalGeneratorBlockEntity
import me.steven.indrev.blockentities.generators.HeatGeneratorBlockEntity
import me.steven.indrev.blockentities.generators.SolarGeneratorBlockEntity
import me.steven.indrev.blockentities.modularworkbench.ModularWorkbenchBlockEntity
import me.steven.indrev.blocks.*
import me.steven.indrev.gui.controllers.*
import me.steven.indrev.utils.*
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.fabricmc.fabric.api.tool.attribute.v1.FabricToolTags
import net.minecraft.block.Block
import net.minecraft.block.Material
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.client.item.TooltipContext
import net.minecraft.item.BlockItem
import net.minecraft.item.ItemStack
import net.minecraft.sound.BlockSoundGroup
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier
import net.minecraft.world.BlockView
import java.util.function.Supplier

class MachineRegistry(private val identifier: Identifier, val upgradeable: Boolean = true, private vararg val tiers: Tier = Tier.values()) {

    private val blocks: MutableMap<Tier, Block> = mutableMapOf()
    private val blockEntities: MutableMap<Tier, BlockEntityType<*>> = mutableMapOf()
    private val baseInternalBuffers: MutableMap<Tier, Double> = mutableMapOf()

    fun register(blockProvider: (Tier) -> Block, entityProvider: (Tier) -> () -> MachineBlockEntity): MachineRegistry {
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
            FabricBlockSettings.of(Material.METAL)
                .sounds(BlockSoundGroup.METAL)
                .requiresTool()
                .breakByTool(FabricToolTags.PICKAXES, 2)
                .strength(5.0f, 6.0f)
                .lightLevel { state -> if (state[MachineBlock.WORKING_PROPERTY]) 7 else 0 }
        }
        
        val COAL_GENERATOR_REGISTRY = MachineRegistry(identifier("coal_generator"), false, Tier.MK1).register(
            { tier ->
                FacingMachineBlock(
                    MACHINE_BLOCK_SETTINGS(), tier, ::CoalGeneratorController
                ) { CoalGeneratorBlockEntity() }
            },
            { { CoalGeneratorBlockEntity() } }
        ).buffer { 1000.0 }

        val SOLAR_GENERATOR_REGISTRY = MachineRegistry(
            identifier("solar_generator"),
            false,
            Tier.MK1,
            Tier.MK3
        ).register(
            { tier ->
                MachineBlock(
                    MACHINE_BLOCK_SETTINGS(), tier, ::SolarGeneratorController
                ) { SolarGeneratorBlockEntity(tier) }
            },
            { tier -> { SolarGeneratorBlockEntity(tier) } }
        ).buffer { tier -> tier.io * 2 }

        val BIOMASS_GENERATOR_REGISTRY = MachineRegistry(identifier("biomass_generator"), false, Tier.MK3).register(
            { tier ->
                FacingMachineBlock(
                    MACHINE_BLOCK_SETTINGS(), tier, ::BiomassGeneratorController
                ) { BiomassGeneratorBlockEntity(tier) }
            },
            { tier -> { BiomassGeneratorBlockEntity(tier) } }
        ).buffer { 20000.0 }

        val HEAT_GENERATOR_REGISTRY = MachineRegistry(identifier("heat_generator"), false, Tier.MK4).register(
            { tier ->
                FacingMachineBlock(
                    MACHINE_BLOCK_SETTINGS(), tier, ::HeatGeneratorController
                ) { HeatGeneratorBlockEntity(tier) }
            },
            { tier -> { HeatGeneratorBlockEntity(tier) } }
        ).buffer { 20000.0 }

        val CONTAINER_REGISTRY = MachineRegistry(identifier("lazuli_flux_container"), false).register(
            { tier ->
                VerticalFacingMachineBlock(
                    MACHINE_BLOCK_SETTINGS(), tier, ::BatteryController
                ) { BatteryBlockEntity(tier) }
            },
            { tier -> { BatteryBlockEntity(tier) } }
        ).buffer { tier ->
            when (tier) {
                Tier.MK1 -> 10000.0
                Tier.MK2 -> 100000.0
                Tier.MK3 -> 1000000.0
                Tier.MK4 -> 10000000.0
                Tier.CREATIVE -> Double.MAX_VALUE
            }
        }

        val ELECTRIC_FURNACE_REGISTRY = MachineRegistry(identifier("electric_furnace")).register(
            { tier ->
                FacingMachineBlock(
                    MACHINE_BLOCK_SETTINGS(), tier, ::ElectricFurnaceController
                ) { ElectricFurnaceBlockEntity(tier) }
            },
            { tier -> { ElectricFurnaceBlockEntity(tier) } }
        ).buffer { tier ->
            when (tier) {
                Tier.MK1 -> 1000.0
                Tier.MK2 -> 5000.0
                Tier.MK3 -> 10000.0
                Tier.MK4 -> 100000.0
                Tier.CREATIVE -> Double.MAX_VALUE
            }
        }

        val PULVERIZER_REGISTRY = MachineRegistry(identifier("pulverizer")).register(
            { tier ->
                FacingMachineBlock(
                    MACHINE_BLOCK_SETTINGS(), tier, ::PulverizerController
                ) { PulverizerBlockEntity(tier) }
            },
            { tier -> { PulverizerBlockEntity(tier) } }
        ).buffer { tier ->
            when (tier) {
                Tier.MK1 -> 1000.0
                Tier.MK2 -> 5000.0
                Tier.MK3 -> 10000.0
                Tier.MK4 -> 100000.0
                Tier.CREATIVE -> Double.MAX_VALUE
            }
        }

        val COMPRESSOR_REGISTRY = MachineRegistry(identifier("compressor")).register(
            { tier ->
                FacingMachineBlock(
                    MACHINE_BLOCK_SETTINGS(), tier, ::CompressorController
                ) { CompressorBlockEntity(tier) }
            },
            { tier -> { CompressorBlockEntity(tier) } }
        ).buffer { tier ->
            when (tier) {
                Tier.MK1 -> 1000.0
                Tier.MK2 -> 5000.0
                Tier.MK3 -> 10000.0
                Tier.MK4 -> 100000.0
                Tier.CREATIVE -> Double.MAX_VALUE
            }
        }

        val INFUSER_REGISTRY = MachineRegistry(identifier("infuser")).register(
            { tier ->
                FacingMachineBlock(
                    MACHINE_BLOCK_SETTINGS(), tier, ::InfuserController
                ) { InfuserBlockEntity(tier) }
            },
            { tier -> { InfuserBlockEntity(tier) } }
        ).buffer { tier ->
            when (tier) {
                Tier.MK1 -> 1000.0
                Tier.MK2 -> 5000.0
                Tier.MK3 -> 10000.0
                Tier.MK4 -> 100000.0
                Tier.CREATIVE -> Double.MAX_VALUE
            }
        }

        val RECYCLER_REGISTRY = MachineRegistry(identifier("recycler"), false, Tier.MK2).register(
            { tier ->
                FacingMachineBlock(
                    MACHINE_BLOCK_SETTINGS(), tier, ::RecyclerController
                ) { RecyclerBlockEntity(tier) }
            },
            { tier -> { RecyclerBlockEntity(tier) } }
        ).buffer { 50000.0 }

        val CABLE_REGISTRY = MachineRegistry(identifier("cable"), false, Tier.MK1, Tier.MK2, Tier.MK3, Tier.MK4)
            .register(
                { tier -> CableBlock(MACHINE_BLOCK_SETTINGS().lightLevel(0), tier) },
                { tier -> { CableBlockEntity(tier) } }
            ).buffer { tier -> tier.io * 2 }

        val CHOPPER_REGISTRY = MachineRegistry(identifier("chopper"), false, Tier.MK4).register(
            { tier ->
                FacingMachineBlock(
                    MACHINE_BLOCK_SETTINGS(), tier, ::ChopperController
                ) { ChopperBlockEntity(tier) }
            },
            { tier -> { ChopperBlockEntity(tier) } }
        ).buffer { tier ->
            when (tier) {
                Tier.MK1 -> 1000.0
                Tier.MK2 -> 5000.0
                Tier.MK3 -> 10000.0
                Tier.MK4 -> 50000.0
                Tier.CREATIVE -> Double.MAX_VALUE
            }
        }

        val RANCHER_REGISTRY = MachineRegistry(identifier("rancher"), false, Tier.MK4).register(
            { tier ->
                FacingMachineBlock(
                    MACHINE_BLOCK_SETTINGS(), tier, ::RancherController
                ) { RancherBlockEntity(tier) }
            },
            { tier -> { RancherBlockEntity(tier) } }
        ).buffer { tier ->
            when (tier) {
                Tier.MK1 -> 1000.0
                Tier.MK2 -> 5000.0
                Tier.MK3 -> 10000.0
                Tier.MK4 -> 50000.0
                Tier.CREATIVE -> Double.MAX_VALUE
            }
        }

        val MINER_REGISTRY = MachineRegistry(identifier("miner"), false, Tier.MK4).register(
            { tier ->
                object : FacingMachineBlock(
                    MACHINE_BLOCK_SETTINGS(),
                    tier,
                    ::MinerController,
                    { MinerBlockEntity(tier) }
                ) {
                    override fun buildTooltip(stack: ItemStack?, view: BlockView?, tooltip: MutableList<Text>?, options: TooltipContext?) {
                        super.buildTooltip(stack, view, tooltip, options)
                        tooltip?.add(TranslatableText("block.indrev.miner.tooltip").formatted(Formatting.BLUE, Formatting.ITALIC))
                    }
                }
            },
            { tier -> { MinerBlockEntity(tier) } }
        ).buffer { 50000.0 }

        val MODULAR_WORKBENCH_REGISTRY = MachineRegistry(identifier("modular_workbench"), false, Tier.MK4).register(
            { tier ->
                FacingMachineBlock(MACHINE_BLOCK_SETTINGS(), tier, ::ModularWorkbenchController) { ModularWorkbenchBlockEntity(tier) }
            },
            { tier -> { ModularWorkbenchBlockEntity(tier) } }
        ).buffer { 500000.0 }

        val CHARGE_PAD_REGISTRY = MachineRegistry(identifier("charge_pad"), false, Tier.MK4).register(
            { tier -> ChargePadBlock(MACHINE_BLOCK_SETTINGS(), tier) },
            { tier -> { ChargePadBlockEntity(tier) } }
        ).buffer { 4096.0 }
    }
}