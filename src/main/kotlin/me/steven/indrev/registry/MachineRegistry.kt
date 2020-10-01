package me.steven.indrev.registry

import me.steven.indrev.IndustrialRevolution.CONFIG
import me.steven.indrev.blockentities.cables.CableBlockEntity
import me.steven.indrev.blockentities.crafters.*
import me.steven.indrev.blockentities.farms.*
import me.steven.indrev.blockentities.generators.BiomassGeneratorBlockEntity
import me.steven.indrev.blockentities.generators.CoalGeneratorBlockEntity
import me.steven.indrev.blockentities.generators.HeatGeneratorBlockEntity
import me.steven.indrev.blockentities.generators.SolarGeneratorBlockEntity
import me.steven.indrev.blockentities.modularworkbench.ModularWorkbenchBlockEntity
import me.steven.indrev.blockentities.storage.BatteryBlockEntity
import me.steven.indrev.blockentities.storage.ChargePadBlockEntity
import me.steven.indrev.blocks.machine.*
import me.steven.indrev.config.IConfig
import me.steven.indrev.gui.controllers.machines.*
import me.steven.indrev.items.energy.MachineBlockItem
import me.steven.indrev.utils.*
import net.fabricmc.api.EnvType
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap
import net.fabricmc.fabric.api.tool.attribute.v1.FabricToolTags
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.block.Block
import net.minecraft.block.Material
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.client.item.TooltipContext
import net.minecraft.client.render.RenderLayer
import net.minecraft.item.ItemStack
import net.minecraft.sound.BlockSoundGroup
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier
import net.minecraft.world.BlockView
import java.util.function.Supplier

class MachineRegistry(private val identifier: Identifier, val upgradeable: Boolean = true, vararg val tiers: Tier = Tier.values()) {

    private val configs: MutableMap<Tier, IConfig> = mutableMapOf()
    private val blocks: MutableMap<Tier, Block> = mutableMapOf()
    private val blockEntities: MutableMap<Tier, BlockEntityType<*>> = mutableMapOf()

    fun register(blockProvider: (Tier) -> Block, entityProvider: (Tier) -> () -> BlockEntity): MachineRegistry {
        tiers.forEach { tier ->
            val block = blockProvider(tier)
            if (FabricLoader.getInstance().environmentType == EnvType.CLIENT)
                BlockRenderLayerMap.INSTANCE.putBlock(block, RenderLayer.getCutout())
            val blockItem = MachineBlockItem(block, itemSettings())
            val blockEntityType = BlockEntityType.Builder.create(Supplier(entityProvider(tier)), block).build(null)
            identifier("${identifier.path}_${tier.toString().toLowerCase()}").apply {
                block(block)
                item(blockItem)
                blockEntityType(blockEntityType)
                if (block is MachineBlock && block.config != null)
                    configs[tier] = block.config
            }
            blockEntities[tier] = blockEntityType
            blocks[tier] = block
        }
        return this
    }

    fun forEachBlockEntity(action: (Tier, BlockEntityType<*>) -> Unit) = blockEntities.forEach(action)

    fun forEachBlock(action: (Tier, Block) -> Unit) = blocks.forEach(action)

    fun blockEntityType(tier: Tier) = blockEntities[tier]
        ?: throw IllegalStateException("invalid tier for machine $identifier")

    fun config(tier: Tier) = configs[tier]
        ?: throw java.lang.IllegalStateException("invalid tier for machine $identifier")

    fun block(tier: Tier) = blocks[tier]
        ?: throw java.lang.IllegalStateException("invalid tier for machine $identifier")

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
                HorizontalFacingMachineBlock(
                    MACHINE_BLOCK_SETTINGS(), tier, CONFIG.generators.coalGenerator, ::CoalGeneratorController
                ) { CoalGeneratorBlockEntity() }
            },
            { { CoalGeneratorBlockEntity() } }
        )

        val SOLAR_GENERATOR_REGISTRY = MachineRegistry(
            identifier("solar_generator"),
            false,
            Tier.MK1,
            Tier.MK3
        ).register(
            { tier ->
                MachineBlock(
                    MACHINE_BLOCK_SETTINGS(), tier,
                    when (tier) {
                        Tier.MK1 -> CONFIG.generators.solarGeneratorMk1
                        else -> CONFIG.generators.solarGeneratorMk3
                    }, ::SolarGeneratorController
                ) { SolarGeneratorBlockEntity(tier) }
            },
            { tier -> { SolarGeneratorBlockEntity(tier) } }
        )

        val BIOMASS_GENERATOR_REGISTRY = MachineRegistry(identifier("biomass_generator"), false, Tier.MK3).register(
            { tier ->
                HorizontalFacingMachineBlock(
                    MACHINE_BLOCK_SETTINGS(), tier, CONFIG.generators.biomassGenerator, ::BiomassGeneratorController
                ) { BiomassGeneratorBlockEntity(tier) }
            },
            { tier -> { BiomassGeneratorBlockEntity(tier) } }
        )

        val HEAT_GENERATOR_REGISTRY = MachineRegistry(identifier("heat_generator"), false, Tier.MK4).register(
            { tier ->
                HorizontalFacingMachineBlock(
                    MACHINE_BLOCK_SETTINGS(), tier, CONFIG.generators.heatGenerator, ::HeatGeneratorController
                )  { HeatGeneratorBlockEntity(tier) }
            },
            { tier -> { HeatGeneratorBlockEntity(tier) } }
        )

        val CONTAINER_REGISTRY = MachineRegistry(identifier("lazuli_flux_container"), false).register(
            { tier ->
                FacingMachineBlock(
                    MACHINE_BLOCK_SETTINGS(), tier, null, ::BatteryController
                ) { BatteryBlockEntity(tier) }
            },
            { tier -> { BatteryBlockEntity(tier) } }
        )

        val ELECTRIC_FURNACE_REGISTRY = MachineRegistry(identifier("electric_furnace")).register(
            { tier ->
                HorizontalFacingMachineBlock(
                    MACHINE_BLOCK_SETTINGS(), tier,
                    when (tier) {
                        Tier.MK1 -> CONFIG.machines.electricFurnaceMk1
                        Tier.MK2 -> CONFIG.machines.electricFurnaceMk2
                        Tier.MK3 -> CONFIG.machines.electricFurnaceMk3
                        else -> CONFIG.machines.electricFurnaceMk4
                    }, ::ElectricFurnaceController
                ) { ElectricFurnaceBlockEntity(tier) }
            },
            { tier -> { ElectricFurnaceBlockEntity(tier) } }
        )

        val PULVERIZER_REGISTRY = MachineRegistry(identifier("pulverizer")).register(
            { tier ->
                HorizontalFacingMachineBlock(
                    MACHINE_BLOCK_SETTINGS(), tier,
                    when (tier) {
                        Tier.MK1 -> CONFIG.machines.pulverizerMk1
                        Tier.MK2 -> CONFIG.machines.pulverizerMk2
                        Tier.MK3 -> CONFIG.machines.pulverizerMk3
                        else -> CONFIG.machines.pulverizerMk4
                    }, ::PulverizerController
                ) { PulverizerBlockEntity(tier) }
            },
            { tier -> { PulverizerBlockEntity(tier) } }
        )

        val COMPRESSOR_REGISTRY = MachineRegistry(identifier("compressor")).register(
            { tier ->
                HorizontalFacingMachineBlock(
                    MACHINE_BLOCK_SETTINGS(), tier,
                    when (tier) {
                        Tier.MK1 -> CONFIG.machines.compressorMk1
                        Tier.MK2 -> CONFIG.machines.compressorMk2
                        Tier.MK3 -> CONFIG.machines.compressorMk3
                        else -> CONFIG.machines.compressorMk4
                    }, ::CompressorController
                ) { CompressorBlockEntity(tier) }
            },
            { tier -> { CompressorBlockEntity(tier) } }
        )

        val INFUSER_REGISTRY = MachineRegistry(identifier("infuser")).register(
            { tier ->
                HorizontalFacingMachineBlock(
                    MACHINE_BLOCK_SETTINGS(), tier,
                    when (tier) {
                        Tier.MK1 -> CONFIG.machines.infuserMk1
                        Tier.MK2 -> CONFIG.machines.infuserMk2
                        Tier.MK3 -> CONFIG.machines.infuserMk3
                        else -> CONFIG.machines.infuserMk4
                    }, ::InfuserController
                ) { SolidInfuserBlockEntity(tier) }
            },
            { tier -> { SolidInfuserBlockEntity(tier) } }
        )

        val SAWMILL_REGISTRY = MachineRegistry(identifier("sawmill")).register(
            { tier ->
                HorizontalFacingMachineBlock(
                    MACHINE_BLOCK_SETTINGS(), tier,
                    when (tier) {
                        Tier.MK1 -> CONFIG.machines.sawmillMk1
                        Tier.MK2 -> CONFIG.machines.sawmillMk2
                        Tier.MK3 -> CONFIG.machines.sawmillMk3
                        else -> CONFIG.machines.sawmillMk4
                    }, ::SawmillController
                ) { SawmillBlockEntity(tier) }
            },
            { tier -> { SawmillBlockEntity(tier) } }
        )

        val RECYCLER_REGISTRY = MachineRegistry(identifier("recycler"), false, Tier.MK2).register(
            { tier ->
                HorizontalFacingMachineBlock(
                    MACHINE_BLOCK_SETTINGS(), tier, CONFIG.machines.recycler, ::RecyclerController
                ) { RecyclerBlockEntity(tier) }
            },
            { tier -> { RecyclerBlockEntity(tier) } }
        )

        val SMELTER_REGISTRY = MachineRegistry(identifier("smelter"), false, Tier.MK4).register(
            { tier ->
                HorizontalFacingMachineBlock(
                    MACHINE_BLOCK_SETTINGS(),
                    tier,
                    CONFIG.machines.smelter,
                    ::SmelterController) { SmelterBlockEntity(tier) }
            },
            { tier -> { SmelterBlockEntity(tier) } }
        )

        val CONDENSER_REGISTRY = MachineRegistry(identifier("condenser"), false, Tier.MK4).register(
            { tier ->
                HorizontalFacingMachineBlock(
                    MACHINE_BLOCK_SETTINGS(),
                    tier,
                    CONFIG.machines.condenser,
                    ::CondenserController) { CondenserBlockEntity(tier) }
            },
            { tier -> { CondenserBlockEntity(tier) } }
        )

        val ELECTRIC_FURNACE_FACTORY_REGISTRY = MachineRegistry(identifier("electric_furnace_factory"), false, Tier.MK4).register(
            { tier ->
                HorizontalFacingMachineBlock(
                    MACHINE_BLOCK_SETTINGS(), tier,
                    when (tier) {
                        Tier.MK1 -> CONFIG.machines.electricFurnaceMk1
                        Tier.MK2 -> CONFIG.machines.electricFurnaceMk2
                        Tier.MK3 -> CONFIG.machines.electricFurnaceMk3
                        else -> CONFIG.machines.electricFurnaceMk4
                    }, ::ElectricFurnaceFactoryController
                ) { ElectricFurnaceFactoryBlockEntity(tier) }
            },
            { tier -> { ElectricFurnaceFactoryBlockEntity(tier) } }
        )

        val PULVERIZER_FACTORY_REGISTRY = MachineRegistry(identifier("pulverizer_factory"), false, Tier.MK4).register(
            { tier ->
                HorizontalFacingMachineBlock(
                    MACHINE_BLOCK_SETTINGS(), tier,
                    when (tier) {
                        Tier.MK1 -> CONFIG.machines.pulverizerMk1
                        Tier.MK2 -> CONFIG.machines.pulverizerMk2
                        Tier.MK3 -> CONFIG.machines.pulverizerMk3
                        else -> CONFIG.machines.pulverizerMk4
                    }, ::PulverizerFactoryController
                ) { PulverizerFactoryBlockEntity(tier) }
            },
            { tier -> { PulverizerFactoryBlockEntity(tier) } }
        )

        val COMPRESSOR_FACTORY_REGISTRY = MachineRegistry(identifier("compressor_factory"), false, Tier.MK4).register(
            { tier ->
                HorizontalFacingMachineBlock(
                    MACHINE_BLOCK_SETTINGS(), tier,
                    when (tier) {
                        Tier.MK1 -> CONFIG.machines.compressorMk1
                        Tier.MK2 -> CONFIG.machines.compressorMk2
                        Tier.MK3 -> CONFIG.machines.compressorMk3
                        else -> CONFIG.machines.compressorMk4
                    }, ::CompressorFactoryController
                ) { CompressorFactoryBlockEntity(tier) }
            },
            { tier -> { CompressorFactoryBlockEntity(tier) } }
        )

        val DRAIN_REGISTRY = MachineRegistry(identifier("drain"), false, Tier.MK1).register(
            { tier ->
                HorizontalFacingMachineBlock(
                    MACHINE_BLOCK_SETTINGS(),
                    tier,
                    CONFIG.machines.drain,
                    null) { DrainBlockEntity(tier) }
            },
            { tier -> { DrainBlockEntity(tier) } }
        )

        val FLUID_INFUSER_REGISTRY = MachineRegistry(identifier("fluid_infuser"), true).register(
            { tier ->
                HorizontalFacingMachineBlock(
                    MACHINE_BLOCK_SETTINGS(),
                    tier,
                    when (tier) {
                        Tier.MK1 -> CONFIG.machines.fluidInfuserMk1
                        Tier.MK2 -> CONFIG.machines.fluidInfuserMk2
                        Tier.MK3 -> CONFIG.machines.fluidInfuserMk3
                        else -> CONFIG.machines.fluidInfuserMk4
                    },
                    ::FluidInfuserController) { FluidInfuserBlockEntity(tier) }
            },
            { tier -> { FluidInfuserBlockEntity(tier) } }
        )

        val CHOPPER_REGISTRY = MachineRegistry(identifier("chopper"), true).register(
            { tier ->
                HorizontalFacingMachineBlock(
                    MACHINE_BLOCK_SETTINGS(),
                    tier,
                    when (tier) {
                        Tier.MK1 -> CONFIG.machines.chopperMk1
                        Tier.MK2 -> CONFIG.machines.chopperMk2
                        Tier.MK3 -> CONFIG.machines.chopperMk3
                        else -> CONFIG.machines.chopperMk4
                    }, ::ChopperController
                ) { ChopperBlockEntity(tier) }
            },
            { tier -> { ChopperBlockEntity(tier) } }
        )

        val FARMER_REGISTRY = MachineRegistry(identifier("farmer"), true).register(
            { tier ->
                HorizontalFacingMachineBlock(
                    MACHINE_BLOCK_SETTINGS(),
                    tier,
                    when (tier) {
                        Tier.MK1 -> CONFIG.machines.farmerMk1
                        Tier.MK2 -> CONFIG.machines.farmerMk2
                        Tier.MK3 -> CONFIG.machines.farmerMk3
                        else -> CONFIG.machines.farmerMk4
                    }, ::FarmerController
                ) { FarmerBlockEntity(tier) }
            },
            { tier -> { FarmerBlockEntity(tier) } }
        )

        val RANCHER_REGISTRY = MachineRegistry(identifier("rancher"), true).register(
            { tier ->
                HorizontalFacingMachineBlock(
                    MACHINE_BLOCK_SETTINGS(),
                    tier,
                    when (tier) {
                        Tier.MK1 -> CONFIG.machines.rancherMk1
                        Tier.MK2 -> CONFIG.machines.rancherMk2
                        Tier.MK3 -> CONFIG.machines.rancherMk3
                        else -> CONFIG.machines.rancherMk4
                    }, ::RancherController
                ) { RancherBlockEntity(tier) }
            },
            { tier -> { RancherBlockEntity(tier) } }
        )

        val MINER_REGISTRY = MachineRegistry(identifier("miner"), false, Tier.MK4).register(
            { tier ->
                object : HorizontalFacingMachineBlock(
                    MACHINE_BLOCK_SETTINGS(),
                    tier, CONFIG.machines.miner,
                    ::MinerController,
                    { MinerBlockEntity(tier, true) }
                ) {
                    override fun appendTooltip(stack: ItemStack?, view: BlockView?, tooltip: MutableList<Text>?, options: TooltipContext?) {
                        super.appendTooltip(stack, view, tooltip, options)
                        tooltip?.add(TranslatableText("block.indrev.miner.tooltip").formatted(Formatting.BLUE, Formatting.ITALIC))
                    }
                }
            },
            { tier -> { MinerBlockEntity(tier, true) } }
        )

        val FISHING_FARM_REGISTRY = MachineRegistry(identifier("fishing_farm"), false, Tier.MK2, Tier.MK3, Tier.MK4).register(
            { tier ->
                HorizontalFacingMachineBlock(
                    MACHINE_BLOCK_SETTINGS(), tier,
                    when (tier) {
                        Tier.MK2 -> CONFIG.machines.fishingMk2
                        Tier.MK3 -> CONFIG.machines.fishingMk3
                        else -> CONFIG.machines.fishingMk4
                    }, ::FishingFarmController
                ) { FishingFarmBlockEntity(tier) }
            },
            { tier -> { FishingFarmBlockEntity(tier) } }
        )

        val MODULAR_WORKBENCH_REGISTRY = MachineRegistry(identifier("modular_workbench"), false, Tier.MK4).register(
            { tier ->
                ModularWorkbenchBlock(
                    MACHINE_BLOCK_SETTINGS().nonOpaque(),
                    tier,
                    CONFIG.machines.modularWorkbench,
                    ::ModularWorkbenchController
                ) { ModularWorkbenchBlockEntity(tier) }
            },
            { tier -> { ModularWorkbenchBlockEntity(tier) } }
        )

        val CHARGE_PAD_REGISTRY = MachineRegistry(identifier("charge_pad"), false, Tier.MK4).register(
            { tier -> ChargePadBlock(MACHINE_BLOCK_SETTINGS(), tier) },
            { tier -> { ChargePadBlockEntity(tier) } }
        )

        val CABLE_REGISTRY = MachineRegistry(identifier("cable"), false, Tier.MK1, Tier.MK2, Tier.MK3, Tier.MK4)
            .register(
                { tier -> CableBlock(MACHINE_BLOCK_SETTINGS().lightLevel(0), tier) },
                { tier -> { CableBlockEntity(tier) } }
            )
    }
}