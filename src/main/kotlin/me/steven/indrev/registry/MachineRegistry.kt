package me.steven.indrev.registry

import dev.technici4n.fasttransferlib.api.energy.EnergyApi
import dev.technici4n.fasttransferlib.api.energy.EnergyIo
import me.steven.indrev.api.machines.Tier
import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.blockentities.crafters.*
import me.steven.indrev.blockentities.farms.*
import me.steven.indrev.blockentities.generators.*
import me.steven.indrev.blockentities.laser.LaserBlockEntity
import me.steven.indrev.blockentities.modularworkbench.ModularWorkbenchBlockEntity
import me.steven.indrev.blockentities.storage.ChargePadBlockEntity
import me.steven.indrev.blockentities.storage.LazuliFluxContainerBlockEntity
import me.steven.indrev.blocks.machine.*
import me.steven.indrev.blocks.machine.solarpowerplant.SteamTurbineBlock
import me.steven.indrev.blocks.models.LazuliFluxContainerBakedModel
import me.steven.indrev.blocks.models.MachineBakedModel
import me.steven.indrev.blocks.models.MinerBakedModel
import me.steven.indrev.config.IConfig
import me.steven.indrev.config.IRConfig
import me.steven.indrev.gui.screenhandlers.machines.*
import me.steven.indrev.items.energy.MachineBlockItem
import me.steven.indrev.utils.*
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap
import net.fabricmc.fabric.api.client.rendereregistry.v1.BlockEntityRendererRegistry
import net.fabricmc.fabric.api.tool.attribute.v1.FabricToolTags
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.block.Block
import net.minecraft.block.Material
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher
import net.minecraft.client.render.block.entity.BlockEntityRenderer
import net.minecraft.client.render.model.UnbakedModel
import net.minecraft.client.util.SpriteIdentifier
import net.minecraft.item.BlockItem
import net.minecraft.screen.PlayerScreenHandler
import net.minecraft.sound.BlockSoundGroup
import net.minecraft.util.Identifier
import net.minecraft.util.math.Direction
import java.util.*
import java.util.function.Supplier

class MachineRegistry(private val key: String, val upgradeable: Boolean = true, vararg val tiers: Tier = Tier.values()) {

    private val configs: MutableMap<Tier, IConfig> = EnumMap(Tier::class.java)
    private val blocks: MutableMap<Tier, Block> = EnumMap(Tier::class.java)
    val blockEntities: MutableMap<Tier, BlockEntityType<*>> = EnumMap(Tier::class.java)
    @Environment(EnvType.CLIENT)
    val modelProvider: MutableMap<Tier, (String) -> UnbakedModel?> = EnumMap(Tier::class.java)

    fun blockProvider(blockProvider: MachineRegistry.(Tier) -> Block): MachineRegistry {
        tiers.forEach { tier ->
            val block = blockProvider(this, tier)
            if (FabricLoader.getInstance().environmentType == EnvType.CLIENT)
                BlockRenderLayerMap.INSTANCE.putBlock(block, RenderLayer.getCutout())
            val blockItem =
                if (block is MachineBlock) MachineBlockItem(block, itemSettings())
                else BlockItem(block, itemSettings())
            identifier("${key}_${tier.toString().toLowerCase(Locale.ROOT)}").apply {
                block(block)
                item(blockItem)
                if (block is MachineBlock) {
                    MAP[this] = this@MachineRegistry
                    if (block.config != null) configs[tier] = block.config
                }
            }
            blocks[tier] = block
        }
        return this
    }

    fun blockEntityProvider(entityProvider: (Tier) -> () -> BlockEntity): MachineRegistry {
        tiers.forEach { tier ->
            val blockEntityType =
                BlockEntityType.Builder.create(Supplier(entityProvider(tier)), block(tier)).build(null)
            identifier("${key}_${tier.toString().toLowerCase()}").apply {
                blockEntityType(blockEntityType)
            }
            blockEntities[tier] = blockEntityType
        }
        return this
    }

    fun forEachBlockEntity(action: (Tier, BlockEntityType<*>) -> Unit) = blockEntities.forEach(action)

    fun forEachBlock(action: (Tier, Block) -> Unit) = blocks.forEach(action)

    fun blockEntityType(tier: Tier) = blockEntities[tier]
        ?: throw IllegalStateException("invalid tier for machine $key")

    fun config(tier: Tier) = configs[tier]
        ?: throw java.lang.IllegalStateException("invalid tier for machine $key")

    fun block(tier: Tier) = blocks[tier]
        ?: throw java.lang.IllegalStateException("invalid tier for machine $key")

    fun energyProvider(provider: (Tier) -> (BlockEntity, Direction) -> EnergyIo?): MachineRegistry {
        blockEntities.forEach { (tier, type) ->
            EnergyApi.SIDED.registerForBlockEntities(provider(tier), type)
        }
        return this
    }

    fun modelProvider(provider: (Tier) -> (String) -> UnbakedModel?): MachineRegistry {

        if (FabricLoader.getInstance().environmentType == EnvType.CLIENT) {
            tiers.forEach { tier -> modelProvider[tier] = provider(tier) }
        }

       return this
    }

    fun defaultModelProvider(hasWorkingState: Boolean = true): MachineRegistry {
        if (FabricLoader.getInstance().environmentType == EnvType.CLIENT)
            modelProvider { tier ->
                { id ->
                    MachineBakedModel(id).also {
                        it.tierOverlay(tier)
                        if (hasWorkingState)
                            it.workingOverlayIds.add(
                                SpriteIdentifier(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE,
                                    identifier("block/${id.replace(Regex("_mk[0-4]"), "")}_on"))
                            )
                    }
                }
            }
        return this
    }

    fun noModelProvider(): MachineRegistry {
        if (FabricLoader.getInstance().environmentType == EnvType.CLIENT)
            modelProvider { { null } }
        return this
    }
    
    fun defaultEnergyProvider(): MachineRegistry = energyProvider { { be, _ -> be as? MachineBlockEntity<*>? } }

    @Suppress("UNCHECKED_CAST")
    @Environment(EnvType.CLIENT)
    fun <T : BlockEntity> registerBlockEntityRenderer(renderer: (BlockEntityRenderDispatcher) -> BlockEntityRenderer<T>) {
        blockEntities.forEach { (_, type) ->
            BlockEntityRendererRegistry.INSTANCE.register(type as BlockEntityType<T>) { dispatcher -> renderer(dispatcher) }
        }
    }

    @Environment(EnvType.CLIENT)
    fun setRenderLayer(layer: RenderLayer) {
        blocks.forEach { (_, block) -> BlockRenderLayerMap.INSTANCE.putBlock(block, layer) }
    }

    companion object {

        val MAP = hashMapOf<Identifier, MachineRegistry>()

        private val SETTINGS = {
            FabricBlockSettings.of(Material.METAL)
                .sounds(BlockSoundGroup.METAL)
                .requiresTool()
                .breakByTool(FabricToolTags.PICKAXES, 2)
                .strength(5.0f, 6.0f)
        }

        val COAL_GENERATOR_REGISTRY = MachineRegistry("coal_generator", false, Tier.MK1)
            .blockProvider { tier ->
                HorizontalFacingMachineBlock(
                    this, SETTINGS(), tier, IRConfig.generators.coalGenerator, ::CoalGeneratorScreenHandler
                )
            }
            .blockEntityProvider { { CoalGeneratorBlockEntity() } }
            .defaultEnergyProvider()
            .defaultModelProvider()

        val SOLAR_GENERATOR_REGISTRY = MachineRegistry("solar_generator", false, Tier.MK1, Tier.MK3)
            .blockProvider { tier ->
                MachineBlock(
                    this,
                    SETTINGS(), 
                    tier,
                    when (tier) {
                        Tier.MK1 -> IRConfig.generators.solarGeneratorMk1
                        else -> IRConfig.generators.solarGeneratorMk3
                    }, ::SolarGeneratorScreenHandler
                )
            }
            .blockEntityProvider { tier -> { SolarGeneratorBlockEntity(tier) } }
            .defaultEnergyProvider()
            .defaultModelProvider()

        val BIOMASS_GENERATOR_REGISTRY = MachineRegistry("biomass_generator", false, Tier.MK3)
            .blockProvider { tier ->
                HorizontalFacingMachineBlock(
                    this, SETTINGS(), tier, IRConfig.generators.biomassGenerator, ::BiomassGeneratorScreenHandler
                )
            }
            .blockEntityProvider { tier -> { BiomassGeneratorBlockEntity(tier) } }
            .defaultEnergyProvider()
            .defaultModelProvider()

        val HEAT_GENERATOR_REGISTRY = MachineRegistry("heat_generator", false, Tier.MK4)
            .blockProvider { tier ->
                HorizontalFacingMachineBlock(
                    this, SETTINGS().nonOpaque(), tier, IRConfig.generators.heatGenerator, ::HeatGeneratorScreenHandler
                )
            }
            .blockEntityProvider { tier -> { HeatGeneratorBlockEntity(tier) } }
            .defaultEnergyProvider()
            .noModelProvider()

        val LAZULI_FLUX_CONTAINER_REGISTRY = MachineRegistry("lazuli_flux_container", false)
            .blockProvider { tier -> LazuliFluxContainerBlock(this, SETTINGS(), tier) }
            .blockEntityProvider { tier -> { LazuliFluxContainerBlockEntity(tier) } }
            .energyProvider {
                { be, dir ->
                    val blockEntity = be as? LazuliFluxContainerBlockEntity
                    if (blockEntity != null) LazuliFluxContainerBlockEntity.LFCEnergyIo(blockEntity, dir) else null
                }
            }
            .modelProvider { { id -> LazuliFluxContainerBakedModel(id) } }

        val ELECTRIC_FURNACE_REGISTRY = MachineRegistry("electric_furnace")
            .blockProvider { tier ->
                HorizontalFacingMachineBlock(
                    this,
                    SETTINGS(), tier,
                    when (tier) {
                        Tier.MK1 -> IRConfig.machines.electricFurnaceMk1
                        Tier.MK2 -> IRConfig.machines.electricFurnaceMk2
                        Tier.MK3 -> IRConfig.machines.electricFurnaceMk3
                        else -> IRConfig.machines.electricFurnaceMk4
                    }, ::ElectricFurnaceScreenHandler
                )
            }
            .blockEntityProvider { tier -> { ElectricFurnaceBlockEntity(tier) } }
            .defaultEnergyProvider()
            .modelProvider { tier ->
                { id ->
                    MachineBakedModel(id).also {
                        it.workingOverlayIds.add(blockSpriteId("block/electric_furnace_emissive_on"))
                        it.tierOverlay(tier)
                    }
                }
            }

        val PULVERIZER_REGISTRY = MachineRegistry("pulverizer")
            .blockProvider { tier ->
                HorizontalFacingMachineBlock(
                    this,
                    SETTINGS(),
                    tier,
                    when (tier) {
                        Tier.MK1 -> IRConfig.machines.pulverizerMk1
                        Tier.MK2 -> IRConfig.machines.pulverizerMk2
                        Tier.MK3 -> IRConfig.machines.pulverizerMk3
                        else -> IRConfig.machines.pulverizerMk4
                    }, ::PulverizerScreenHandler
                )
            }
            .blockEntityProvider { tier -> { PulverizerBlockEntity(tier) } }
            .defaultEnergyProvider()
            .defaultModelProvider()

        val COMPRESSOR_REGISTRY = MachineRegistry("compressor")
            .blockProvider { tier ->
                HorizontalFacingMachineBlock(
                    this,
                    SETTINGS(),
                    tier,
                    when (tier) {
                        Tier.MK1 -> IRConfig.machines.compressorMk1
                        Tier.MK2 -> IRConfig.machines.compressorMk2
                        Tier.MK3 -> IRConfig.machines.compressorMk3
                        else -> IRConfig.machines.compressorMk4
                    }, ::CompressorScreenHandler
                )
            }
            .blockEntityProvider { tier -> { CompressorBlockEntity(tier) } }
            .defaultEnergyProvider()
            .defaultModelProvider()

        val INFUSER_REGISTRY = MachineRegistry("infuser")
            .blockProvider { tier ->
                HorizontalFacingMachineBlock(
                    this,
                    SETTINGS(),
                    tier,
                    when (tier) {
                        Tier.MK1 -> IRConfig.machines.infuserMk1
                        Tier.MK2 -> IRConfig.machines.infuserMk2
                        Tier.MK3 -> IRConfig.machines.infuserMk3
                        else -> IRConfig.machines.infuserMk4
                    }, ::SolidInfuserScreenHandler
                )
            }
            .blockEntityProvider { tier -> { SolidInfuserBlockEntity(tier) } }
            .defaultEnergyProvider()
            .modelProvider { tier ->
                { id ->
                    MachineBakedModel(id).also {
                        it.workingOverlayIds.add(blockSpriteId("block/infuser_emissive_on"))
                        it.tierOverlay(tier)
                    }
                }
            }

        val SAWMILL_REGISTRY = MachineRegistry("sawmill")
            .blockProvider { tier ->
                HorizontalFacingMachineBlock(
                    this,
                    SETTINGS(), tier,
                    when (tier) {
                        Tier.MK1 -> IRConfig.machines.sawmillMk1
                        Tier.MK2 -> IRConfig.machines.sawmillMk2
                        Tier.MK3 -> IRConfig.machines.sawmillMk3
                        else -> IRConfig.machines.sawmillMk4
                    }, ::SawmillScreenHandler
                )
            }
            .blockEntityProvider { tier -> { SawmillBlockEntity(tier) } }
            .defaultEnergyProvider()
            .defaultModelProvider()

        val RECYCLER_REGISTRY = MachineRegistry("recycler", false, Tier.MK2)
            .blockProvider { tier ->
                HorizontalFacingMachineBlock(
                    this, SETTINGS(), tier, IRConfig.machines.recycler, ::RecyclerScreenHandler
                )
            }
            .blockEntityProvider { tier -> { RecyclerBlockEntity(tier) } }
            .defaultEnergyProvider()
            .defaultModelProvider()

        val SMELTER_REGISTRY = MachineRegistry("smelter", false, Tier.MK4)
            .blockProvider { tier ->
                HorizontalFacingMachineBlock(
                    this, SETTINGS(), tier, IRConfig.machines.smelter, ::SmelterScreenHandler
                )
            }
            .blockEntityProvider { tier -> { SmelterBlockEntity(tier) } }
            .defaultEnergyProvider()
            .defaultModelProvider(hasWorkingState = false)

        val CONDENSER_REGISTRY = MachineRegistry("condenser", false, Tier.MK4)
            .blockProvider { tier ->
                HorizontalFacingMachineBlock(
                    this, SETTINGS(), tier, IRConfig.machines.condenser, ::CondenserScreenHandler
                )
            }
            .blockEntityProvider { tier -> { CondenserBlockEntity(tier) } }
            .defaultEnergyProvider()
            .defaultModelProvider()

        val ELECTRIC_FURNACE_FACTORY_REGISTRY = MachineRegistry("electric_furnace_factory", false, Tier.MK4)
            .blockProvider { tier ->
                HorizontalFacingMachineBlock(
                    this,
                    SETTINGS(),
                    tier,
                    IRConfig.machines.electricFurnaceFactory,
                    ::ElectricFurnaceFactoryScreenHandler
                )
            }
            .blockEntityProvider { tier -> { ElectricFurnaceFactoryBlockEntity(tier) } }
            .defaultEnergyProvider()
            .modelProvider {
                { id ->
                    MachineBakedModel(id).also {
                        it.baseSprite = blockSpriteId("block/electric_furnace")
                        it.factoryOverlay()
                    }
                }
            }

        val PULVERIZER_FACTORY_REGISTRY = MachineRegistry("pulverizer_factory", false, Tier.MK4)
            .blockProvider { tier ->
                HorizontalFacingMachineBlock(
                    this,
                    SETTINGS(),
                    tier,
                    IRConfig.machines.pulverizerFactory,
                    ::PulverizerFactoryScreenHandler
                )
            }
            .blockEntityProvider { tier -> { PulverizerFactoryBlockEntity(tier) } }
            .defaultEnergyProvider()
            .modelProvider {
                { id ->
                    MachineBakedModel(id).also {
                        it.baseSprite = blockSpriteId("block/pulverizer")
                        it.factoryOverlay()
                    }
                }
            }

        val COMPRESSOR_FACTORY_REGISTRY = MachineRegistry("compressor_factory", false, Tier.MK4)
            .blockProvider { tier ->
                HorizontalFacingMachineBlock(
                    this,
                    SETTINGS(),
                    tier,
                    IRConfig.machines.compressorFactory,
                    ::CompressorFactoryScreenHandler
                )
            }
            .blockEntityProvider { tier -> { CompressorFactoryBlockEntity(tier) } }
            .defaultEnergyProvider()
            .modelProvider {
                { id ->
                    MachineBakedModel(id).also {
                        it.baseSprite = blockSpriteId("block/compressor")
                        it.overlayIds.add(blockSpriteId("block/factory_overlay_compressor"))
                    }
                }
            }

        val SOLID_INFUSER_FACTORY_REGISTRY = MachineRegistry("infuser_factory", false, Tier.MK4)
            .blockProvider { tier ->
                HorizontalFacingMachineBlock(
                    this,
                    SETTINGS(),
                    tier,
                    IRConfig.machines.infuserFactory,
                    ::SolidInfuserFactoryScreenHandler
                )
            }
            .blockEntityProvider { tier -> { SolidInfuserFactoryBlockEntity(tier) } }
            .defaultEnergyProvider()
            .modelProvider {
                { id ->
                    MachineBakedModel(id).also {
                        it.baseSprite = blockSpriteId("block/infuser")
                        it.factoryOverlay()
                    }
                }
            }

        val DRAIN_REGISTRY = MachineRegistry("drain", false, Tier.MK1)
            .blockProvider { tier ->
                MachineBlock(
                    this, SETTINGS(), tier, IRConfig.machines.drain, null
                )
            }
            .blockEntityProvider { tier -> { DrainBlockEntity(tier) } }
            .defaultEnergyProvider()
            .defaultModelProvider(hasWorkingState = false)

        val PUMP_REGISTRY = MachineRegistry("pump", false, Tier.MK1)
            .blockProvider { PumpBlock(this, SETTINGS().nonOpaque()) }
            .blockEntityProvider { tier -> { PumpBlockEntity(tier) } }
            .energyProvider { { be, dir -> if (dir == Direction.UP) be as? MachineBlockEntity<*> else null } }
            .noModelProvider()

        val FLUID_INFUSER_REGISTRY = MachineRegistry("fluid_infuser", true)
            .blockProvider { tier ->
                HorizontalFacingMachineBlock(
                    this,
                    SETTINGS(),
                    tier,
                    when (tier) {
                        Tier.MK1 -> IRConfig.machines.fluidInfuserMk1
                        Tier.MK2 -> IRConfig.machines.fluidInfuserMk2
                        Tier.MK3 -> IRConfig.machines.fluidInfuserMk3
                        else -> IRConfig.machines.fluidInfuserMk4
                    },
                    ::FluidInfuserScreenHandler
                )
            }
            .blockEntityProvider { tier -> { FluidInfuserBlockEntity(tier) } }
            .defaultEnergyProvider()
            .defaultModelProvider()

        val CHOPPER_REGISTRY = MachineRegistry("chopper", true)
            .blockProvider { tier ->
                HorizontalFacingMachineBlock(
                    this,
                    SETTINGS(),
                    tier,
                    when (tier) {
                        Tier.MK1 -> IRConfig.machines.chopperMk1
                        Tier.MK2 -> IRConfig.machines.chopperMk2
                        Tier.MK3 -> IRConfig.machines.chopperMk3
                        else -> IRConfig.machines.chopperMk4
                    }, ::ChopperScreenHandler
                )
            }
            .blockEntityProvider { tier -> { ChopperBlockEntity(tier) } }
            .defaultEnergyProvider()
            .defaultModelProvider()

        val FARMER_REGISTRY = MachineRegistry("farmer", true)
            .blockProvider { tier ->
                HorizontalFacingMachineBlock(
                    this,
                    SETTINGS(),
                    tier,
                    when (tier) {
                        Tier.MK1 -> IRConfig.machines.farmerMk1
                        Tier.MK2 -> IRConfig.machines.farmerMk2
                        Tier.MK3 -> IRConfig.machines.farmerMk3
                        else -> IRConfig.machines.farmerMk4
                    }, ::FarmerScreenHandler
                )
            }
            .blockEntityProvider { tier -> { FarmerBlockEntity(tier) } }
            .defaultEnergyProvider()
            .defaultModelProvider(hasWorkingState = false)

        val RANCHER_REGISTRY = MachineRegistry("rancher", true)
            .blockProvider { tier ->
                HorizontalFacingMachineBlock(
                    this,
                    SETTINGS(),
                    tier,
                    when (tier) {
                        Tier.MK1 -> IRConfig.machines.rancherMk1
                        Tier.MK2 -> IRConfig.machines.rancherMk2
                        Tier.MK3 -> IRConfig.machines.rancherMk3
                        else -> IRConfig.machines.rancherMk4
                    }, ::RancherScreenHandler
                )
            }
            .blockEntityProvider { tier -> { RancherBlockEntity(tier) } }
            .defaultEnergyProvider()
            .defaultModelProvider()

        val MINER_REGISTRY = MachineRegistry("miner", false, Tier.MK4)
            .blockProvider { tier -> MinerBlock(this, SETTINGS(), tier) }
            .blockEntityProvider { tier -> { MinerBlockEntity(tier) } }
            .defaultEnergyProvider()
            .modelProvider {
                { id -> MinerBakedModel(id) }
            }

        val FISHING_FARM_REGISTRY = MachineRegistry("fishing_farm", false, Tier.MK2, Tier.MK3, Tier.MK4)
            .blockProvider { tier ->
                HorizontalFacingMachineBlock(
                    this,
                    SETTINGS(),
                    tier,
                    when (tier) {
                        Tier.MK2 -> IRConfig.machines.fishingMk2
                        Tier.MK3 -> IRConfig.machines.fishingMk3
                        else -> IRConfig.machines.fishingMk4
                    }, ::FishingFarmScreenHandler
                )
            }
            .blockEntityProvider { tier -> { FishingFarmBlockEntity(tier) } }
            .defaultEnergyProvider()
            .noModelProvider()

        val MODULAR_WORKBENCH_REGISTRY = MachineRegistry("modular_workbench", false, Tier.MK4)
            .blockProvider { tier ->
                ModularWorkbenchBlock(
                    this,
                    SETTINGS().nonOpaque(),
                    tier,
                    IRConfig.machines.modularWorkbench,
                    ::ModularWorkbenchScreenHandler
                )
            }
            .blockEntityProvider { tier -> { ModularWorkbenchBlockEntity(tier) } }
            .defaultEnergyProvider()
            .noModelProvider()

        val CHARGE_PAD_REGISTRY = MachineRegistry("charge_pad", false, Tier.MK4)
            .blockProvider { tier -> ChargePadBlock(this, SETTINGS(), tier) }
            .blockEntityProvider { tier -> { ChargePadBlockEntity(tier) } }
            .energyProvider { { be, dir -> if (dir == Direction.DOWN) ChargePadBlockEntity.ChargePadEnergyIo(be as ChargePadBlockEntity) else null } }
            .noModelProvider()

        val LASER_REGISTRY = MachineRegistry("laser", false, Tier.MK4)
            .blockProvider { LaserBlock(this, SETTINGS().nonOpaque()) }
            .blockEntityProvider { { LaserBlockEntity() } }
            .energyProvider { { be, dir -> if (dir == be.cachedState[FacingMachineBlock.FACING].opposite) be as LaserBlockEntity else null } }
            .noModelProvider()

        val STEAM_TURBINE_REGISTRY = MachineRegistry("steam_turbine", false, Tier.MK4)
            .blockProvider { SteamTurbineBlock(this, SETTINGS().nonOpaque()) }
            .blockEntityProvider { { SteamTurbineBlockEntity() } }
            .defaultModelProvider(true)
    }
}