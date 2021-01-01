package me.steven.indrev.registry

import dev.technici4n.fasttransferlib.api.energy.EnergyApi
import dev.technici4n.fasttransferlib.api.energy.base.SimpleItemEnergyIo
import me.steven.indrev.api.machines.Tier
import me.steven.indrev.armor.IRArmorMaterial
import me.steven.indrev.blockentities.drill.DrillBlockEntity
import me.steven.indrev.blockentities.storage.CabinetBlockEntity
import me.steven.indrev.blockentities.storage.TankBlockEntity
import me.steven.indrev.blocks.machine.DrillBlock
import me.steven.indrev.blocks.misc.*
import me.steven.indrev.datagen.utils.*
import me.steven.indrev.fluids.BaseFluid
import me.steven.indrev.items.armor.IRColorModuleItem
import me.steven.indrev.items.armor.IRModularArmorItem
import me.steven.indrev.items.armor.IRModuleItem
import me.steven.indrev.items.energy.*
import me.steven.indrev.items.misc.*
import me.steven.indrev.items.upgrade.IRUpgradeItem
import me.steven.indrev.items.upgrade.Upgrade
import me.steven.indrev.tools.IRToolMaterial
import me.steven.indrev.tools.modular.ArmorModule
import me.steven.indrev.tools.modular.DrillModule
import me.steven.indrev.tools.modular.GamerAxeModule
import me.steven.indrev.tools.modular.MiningToolModule
import me.steven.indrev.utils.*
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricMaterialBuilder
import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback
import net.fabricmc.fabric.api.tool.attribute.v1.FabricToolTags
import net.minecraft.block.Block
import net.minecraft.block.FluidBlock
import net.minecraft.block.Material
import net.minecraft.block.MaterialColor
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.client.item.TooltipContext
import net.minecraft.entity.EquipmentSlot
import net.minecraft.item.*
import net.minecraft.sound.BlockSoundGroup
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.Formatting
import net.minecraft.util.Rarity
import net.minecraft.util.registry.BuiltinRegistries
import net.minecraft.util.registry.Registry
import net.minecraft.world.World

@Suppress("MemberVisibilityCanBePrivate")
object IRRegistry {
    fun registerAll() {

        identifier("guide_book").item(IRGuideBookItem(itemSettings()))

        MaterialHelper("tin") {
            withItems("dust", "ingot", "plate", "nugget", "chunk", "purified_ore")
            withBlock()
            withOre()
            withTools(
                IRBasicPickaxe(IRToolMaterial.TIN, 1, -1.0f, itemSettings()),
                IRBasicAxe(IRToolMaterial.TIN, 4.5f, -2.0f, itemSettings()),
                IRBasicShovel(IRToolMaterial.TIN, 1.5f, -0.7f, itemSettings()),
                IRBasicSword(IRToolMaterial.TIN, 3, -1.5f, itemSettings()),
                IRBasicHoe(IRToolMaterial.TIN, 2, -0.5f, itemSettings())
            )
            withArmor(IRArmorMaterial.TIN)
        }.register()

        MaterialHelper("copper") {
            withItems("dust", "ingot", "plate", "nugget", "chunk", "purified_ore")
            withBlock()
            withOre()
            withTools(
                IRBasicPickaxe(IRToolMaterial.COPPER, 1, -1.0f, itemSettings()),
                IRBasicAxe(IRToolMaterial.COPPER, 4.5f, -2.0f, itemSettings()),
                IRBasicShovel(IRToolMaterial.COPPER, 1.5f, -0.7f, itemSettings()),
                IRBasicSword(IRToolMaterial.COPPER, 3, -1.5f, itemSettings()),
                IRBasicHoe(IRToolMaterial.COPPER, 2, -0.5f, itemSettings())
            )
            withArmor(IRArmorMaterial.COPPER)
        }.register()

        MaterialHelper("steel") {
            withItems("dust", "ingot", "plate", "nugget")
            withBlock()
            withTools(
                IRBasicPickaxe(IRToolMaterial.STEEL, 2, -1.8f, itemSettings()),
                IRBasicAxe(IRToolMaterial.STEEL, 7f, -3.1f, itemSettings()),
                IRBasicShovel(IRToolMaterial.STEEL, 1.0f, -1.0f, itemSettings()),
                IRBasicSword(IRToolMaterial.STEEL, 5, -2.0f, itemSettings()),
                IRBasicHoe(IRToolMaterial.STEEL, 3, -1.5f, itemSettings())
            )
            withArmor(IRArmorMaterial.STEEL)
        }.register()

        MaterialHelper("iron") { withItems("dust", "plate", "chunk", "purified_ore") }.register()

        MaterialHelper("netherite_scrap") { withItems("dust", "chunk", "purified_ore") }.register()

        MaterialHelper("nikolite") {
            withItems("dust", "ingot")
            withOre { settings -> NikoliteOreBlock(settings) }
        }.register()

        MaterialHelper("enriched_nikolite") { withItems("dust", "ingot") }.register()

        MaterialHelper("diamond") { withItems("dust") }.register()

        MaterialHelper("gold") { withItems("dust", "plate", "chunk", "purified_ore") }.register()

        MaterialHelper("coal") { withItems("dust") }.register()

        MaterialHelper("sulfur") {
            withItems("dust")
        }.register()

        MaterialHelper("lead") {
            withItems("dust", "ingot", "plate", "nugget", "chunk", "purified_ore")
            withBlock()
            withOre()
            withTools(
                IRBasicPickaxe(IRToolMaterial.LEAD, 1, -1.0f, itemSettings()),
                IRBasicAxe(IRToolMaterial.LEAD, 4.5f, -2.0f, itemSettings()),
                IRBasicShovel(IRToolMaterial.LEAD, 1.5f, -0.7f, itemSettings()),
                IRBasicSword(IRToolMaterial.LEAD, 3, -1.5f, itemSettings()),
                IRBasicHoe(IRToolMaterial.LEAD, 2, -0.5f, itemSettings())
            )
            withArmor(IRArmorMaterial.LEAD)
        }.register()

        MaterialHelper("bronze") {
            withItems("dust", "ingot", "plate", "nugget")
            withBlock()
            withTools(
                IRBasicPickaxe(IRToolMaterial.BRONZE, 1, -1.0f, itemSettings()),
                IRBasicAxe(IRToolMaterial.BRONZE, 4.5f, -2.0f, itemSettings()),
                IRBasicShovel(IRToolMaterial.BRONZE, 1.5f, -0.7f, itemSettings()),
                IRBasicSword(IRToolMaterial.BRONZE, 3, -1.5f, itemSettings()),
                IRBasicHoe(IRToolMaterial.BRONZE, 2, -0.5f, itemSettings())
            )
            withArmor(IRArmorMaterial.BRONZE)
        }.register()

        MaterialHelper("silver") {
            withItems("dust", "ingot", "plate", "nugget", "chunk", "purified_ore")
            withBlock()
            withOre()
            withTools(
                IRBasicPickaxe(IRToolMaterial.SILVER, 1, -1.0f, itemSettings()),
                IRBasicAxe(IRToolMaterial.SILVER, 4.5f, -2.0f, itemSettings()),
                IRBasicShovel(IRToolMaterial.SILVER, 1.5f, -0.7f, itemSettings()),
                IRBasicSword(IRToolMaterial.SILVER, 3, -1.5f, itemSettings()),
                IRBasicHoe(IRToolMaterial.SILVER, 2, -0.5f, itemSettings())
            )
            withArmor(IRArmorMaterial.SILVER)
        }.register()

        MaterialHelper("tungsten") {
            withItems("dust", "ingot", "plate", "nugget", "chunk", "purified_ore")
            withBlock()
            withOre()
        }.register()

        MaterialHelper("electrum") {
            withItems("dust", "ingot", "plate", "nugget")
            withBlock()
        }.register()

        MaterialHelper.register()

        identifier("sulfur_crystal").block(SULFUR_CRYSTAL_CLUSTER).item(SULFUR_CRYSTAL_ITEM)

        identifier("sawdust").item(DEFAULT_ITEM())
        identifier("planks").block(PLANKS).item(BlockItem(PLANKS, itemSettings()))
        identifier("plank_block").block(PLANK_BLOCK).item(BlockItem(PLANK_BLOCK, itemSettings()))

        identifier("hammer").item(HAMMER)

        identifier("stone_drill_head").item(STONE_DRILL_HEAD)
        identifier("iron_drill_head").item(IRON_DRILL_HEAD)
        identifier("diamond_drill_head").item(DIAMOND_DRILL_HEAD)
        identifier("netherite_drill_head").item(NETHERITE_DRILL_HEAD)

        identifier("mining_drill_mk1").item(MINING_DRILL_MK1)
        identifier("mining_drill_mk2").item(MINING_DRILL_MK2)
        identifier("mining_drill_mk3").item(MINING_DRILL_MK3)
        identifier("mining_drill_mk4").item(MINING_DRILL_MK4)

        EnergyApi.ITEM.register(SimpleItemEnergyIo.getProvider(4000.0, Tier.MK1.io, Tier.MK1.io), MINING_DRILL_MK1)
        EnergyApi.ITEM.register(SimpleItemEnergyIo.getProvider(8000.0, Tier.MK2.io, Tier.MK2.io), MINING_DRILL_MK2)
        EnergyApi.ITEM.register(SimpleItemEnergyIo.getProvider(16000.0, Tier.MK3.io, Tier.MK3.io), MINING_DRILL_MK3)
        EnergyApi.ITEM.register(SimpleItemEnergyIo.getProvider(32000.0, Tier.MK4.io, Tier.MK4.io), MINING_DRILL_MK4)

        identifier("battery").item(IRBatteryItem(itemSettings().maxDamage(4096), 4096.0))

        identifier("circuit_mk1").item(DEFAULT_ITEM())
        identifier("circuit_mk2").item(DEFAULT_ITEM())
        identifier("circuit_mk3").item(DEFAULT_ITEM())
        identifier("circuit_mk4").item(DEFAULT_ITEM())

        identifier("machine_block").block(MACHINE_BLOCK).item(BlockItem(MACHINE_BLOCK, itemSettings()))

        identifier("fan").item(FAN)
        identifier("cooler_cell").item(COOLER_CELL)
        identifier("heatsink").item(HEATSINK)
        identifier("heat_coil").item(HEAT_COIL)

        identifier("chunk_scanner").item(CHUNK_SCANNER_ITEM)
        identifier("scan_output").item(SCAN_OUTPUT_ITEM)

        identifier("empty_upgrade").item(DEFAULT_ITEM())
        identifier("buffer_upgrade").item(BUFFER_UPGRADE)
        identifier("speed_upgrade").item(SPEED_UPGRADE)
        identifier("energy_upgrade").item(ENERGY_UPGRADE)
        identifier("blast_furnace_upgrade").item(BLAST_FURNACE_UPGRADE)
        identifier("smoker_upgrade").item(SMOKER_UPGRADE)

        identifier("energy_reader").item(ENERGY_READER)

        identifier("tier_upgrade_mk2").item(IRMachineUpgradeItem(itemSettings(), Tier.MK1, Tier.MK2))
        identifier("tier_upgrade_mk3").item(IRMachineUpgradeItem(itemSettings(), Tier.MK2, Tier.MK3))
        identifier("tier_upgrade_mk4").item(IRMachineUpgradeItem(itemSettings(), Tier.MK3, Tier.MK4))

        identifier("biomass").item(BIOMASS)
        identifier("untanned_leather").item(DEFAULT_ITEM())

        COOLANT_IDENTIFIER.block(COOLANT)
        identifier("${COOLANT_IDENTIFIER.path}_still").fluid(COOLANT_STILL)
        identifier("${COOLANT_IDENTIFIER.path}_flowing").fluid(COOLANT_FLOWING)
        identifier("${COOLANT_IDENTIFIER.path}_bucket").item(COOLANT_BUCKET)

        MOLTEN_NETHERITE_IDENTIFIER.block(MOLTEN_NETHERITE)
        identifier("${MOLTEN_NETHERITE_IDENTIFIER.path}_still").fluid(MOLTEN_NETHERITE_STILL)
        identifier("${MOLTEN_NETHERITE_IDENTIFIER.path}_flowing").fluid(MOLTEN_NETHERITE_FLOWING)
        identifier("${MOLTEN_NETHERITE_IDENTIFIER.path}_bucket").item(MOLTEN_NETHERITE_BUCKET)

        MOLTEN_IRON_IDENTIFIER.block(MOLTEN_IRON)
        identifier("${MOLTEN_IRON_IDENTIFIER.path}_still").fluid(MOLTEN_IRON_STILL)
        identifier("${MOLTEN_IRON_IDENTIFIER.path}_flowing").fluid(MOLTEN_IRON_FLOWING)
        identifier("${MOLTEN_IRON_IDENTIFIER.path}_bucket").item(MOLTEN_IRON_BUCKET)

        MOLTEN_GOLD_IDENTIFIER.block(MOLTEN_GOLD)
        identifier("${MOLTEN_GOLD_IDENTIFIER.path}_still").fluid(MOLTEN_GOLD_STILL)
        identifier("${MOLTEN_GOLD_IDENTIFIER.path}_flowing").fluid(MOLTEN_GOLD_FLOWING)
        identifier("${MOLTEN_GOLD_IDENTIFIER.path}_bucket").item(MOLTEN_GOLD_BUCKET)

        MOLTEN_COPPER_IDENTIFIER.block(MOLTEN_COPPER)
        identifier("${MOLTEN_COPPER_IDENTIFIER.path}_still").fluid(MOLTEN_COPPER_STILL)
        identifier("${MOLTEN_COPPER_IDENTIFIER.path}_flowing").fluid(MOLTEN_COPPER_FLOWING)
        identifier("${MOLTEN_COPPER_IDENTIFIER.path}_bucket").item(MOLTEN_COPPER_BUCKET)

        MOLTEN_TIN_IDENTIFIER.block(MOLTEN_TIN)
        identifier("${MOLTEN_TIN_IDENTIFIER.path}_still").fluid(MOLTEN_TIN_STILL)
        identifier("${MOLTEN_TIN_IDENTIFIER.path}_flowing").fluid(MOLTEN_TIN_FLOWING)
        identifier("${MOLTEN_TIN_IDENTIFIER.path}_bucket").item(MOLTEN_TIN_BUCKET)

        MOLTEN_LEAD_IDENTIFIER.block(MOLTEN_LEAD)
        identifier("${MOLTEN_LEAD_IDENTIFIER.path}_still").fluid(MOLTEN_LEAD_STILL)
        identifier("${MOLTEN_LEAD_IDENTIFIER.path}_flowing").fluid(MOLTEN_LEAD_FLOWING)
        identifier("${MOLTEN_LEAD_IDENTIFIER.path}_bucket").item(MOLTEN_LEAD_BUCKET)

        MOLTEN_SILVER_IDENTIFIER.block(MOLTEN_SILVER)
        identifier("${MOLTEN_SILVER_IDENTIFIER.path}_still").fluid(MOLTEN_SILVER_STILL)
        identifier("${MOLTEN_SILVER_IDENTIFIER.path}_flowing").fluid(MOLTEN_SILVER_FLOWING)
        identifier("${MOLTEN_SILVER_IDENTIFIER.path}_bucket").item(MOLTEN_SILVER_BUCKET)

        SULFURIC_ACID_IDENTIFIER.block(SULFURIC_ACID)
        identifier("${SULFURIC_ACID_IDENTIFIER.path}_still").fluid(SULFURIC_ACID_STILL)
        identifier("${SULFURIC_ACID_IDENTIFIER.path}_flowing").fluid(SULFURIC_ACID_FLOWING)
        identifier("${SULFURIC_ACID_IDENTIFIER.path}_bucket").item(SULFURIC_ACID_BUCKET)

        TOXIC_MUD_IDENTIFIER.block(TOXIC_MUD)
        identifier("${TOXIC_MUD_IDENTIFIER.path}_still").fluid(TOXIC_MUD_STILL)
        identifier("${TOXIC_MUD_IDENTIFIER.path}_flowing").fluid(TOXIC_MUD_FLOWING)
        identifier("${TOXIC_MUD_IDENTIFIER.path}_bucket").item(TOXIC_MUD_BUCKET)

        identifier("wrench").item(WRENCH)

        identifier("tech_soup").item(TECH_SOUP)

        identifier("modular_armor_helmet").item(MODULAR_ARMOR_HELMET)
        identifier("modular_armor_chest").item(MODULAR_ARMOR_CHEST)
        identifier("modular_armor_legs").item(MODULAR_ARMOR_LEGGINGS)
        identifier("modular_armor_boots").item(MODULAR_ARMOR_BOOTS)

        identifier("module_protection").item(PROTECTION_MODULE_ITEM)
        identifier("module_speed").item(SPEED_MODULE_ITEM)
        identifier("module_jump_boost").item(JUMP_BOOST_MODULE_ITEM)
        identifier("module_night_vision").item(NIGHT_VISION_MODULE_ITEM)
        identifier("module_breathing").item(BREATHING_MODULE_ITEM)
        identifier("module_feather_falling").item(FEATHER_FALLING_MODULE_ITEM)
        identifier("module_auto_feeder").item(AUTO_FEEDER_MODULE_ITEM)
        identifier("module_charger").item(CHARGER_MODULE_ITEM)
        identifier("module_solar_panel").item(SOLAR_PANEL_MODULE_ITEM)
        identifier("module_piglin_tricker").item(PIGLIN_TRICKER_MODULE_ITEM)
        identifier("module_fire_resistance").item(FIRE_RESISTANCE_MODULE_ITEM)
        identifier("module_range").item(RANGE_MODULE_ITEM)
        identifier("module_efficiency").item(EFFICIENCY_MODULE_ITEM)
        identifier("module_fortune").item(FORTUNE_MODULE_ITEM)
        identifier("module_silk_touch").item(SILK_TOUCH_MODULE_ITEM)
        identifier("module_looting").item(LOOTING_MODULE_ITEM)
        identifier("module_fire_aspect").item(FIRE_ASPECT_MODULE_ITEM)
        identifier("module_sharpness").item(SHARPNESS_MODULE_ITEM)
        //identifier("module_reach").item(REACH_MODULE_ITEM)

        identifier("module_color_pink").item(PINK_MODULE_ITEM)
        identifier("module_color_red").item(RED_MODULE_ITEM)
        identifier("module_color_purple").item(PURPLE_MODULE_ITEM)
        identifier("module_color_blue").item(BLUE_MODULE_ITEM)
        identifier("module_color_cyan").item(CYAN_MODULE_ITEM)
        identifier("module_color_green").item(GREEN_MODULE_ITEM)
        identifier("module_color_yellow").item(YELLOW_MODULE_ITEM)
        identifier("module_color_orange").item(ORANGE_MODULE_ITEM)
        identifier("module_color_black").item(BLACK_MODULE_ITEM)
        identifier("module_color_brown").item(BROWN_MODULE_ITEM)

        identifier("portable_charger").item(PORTABLE_CHARGER_ITEM)

        identifier("gamer_axe").item(GAMER_AXE_ITEM)

        identifier("tank").block(TANK_BLOCK).item(TANK_BLOCK_ITEM).blockEntityType(TANK_BLOCK_ENTITY)

        identifier("controller").block(CONTROLLER).item(BlockItem(CONTROLLER, itemSettings()))
        identifier("frame").block(FRAME).item(BlockItem(FRAME, itemSettings()))
        identifier("duct").block(DUCT).item(BlockItem(DUCT, itemSettings()))
        identifier("silo").block(SILO).item(BlockItem(SILO, itemSettings()))
        identifier("warning_strobe").block(WARNING_STROBE).item(BlockItem(WARNING_STROBE, itemSettings()))
        identifier("intake").block(INTAKE).item(BlockItem(INTAKE, itemSettings()))
        identifier("cabinet").block(CABINET).item(BlockItem(CABINET, itemSettings())).blockEntityType(CABINET_BLOCK_ENTITY_TYPE)

        identifier("drill_top").block(DRILL_TOP)
        identifier("drill_middle").block(DRILL_MIDDLE)
        identifier("drill_bottom").block(DRILL_BOTTOM).item(BlockItem(DRILL_BOTTOM, itemSettings()))
        identifier("drill").blockEntityType(DRILL_BLOCK_ENTITY_TYPE)
        
        WorldGeneration.init()

        BuiltinRegistries.BIOME.forEach { biome -> WorldGeneration.handleBiome(biome) }
        RegistryEntryAddedCallback.event(BuiltinRegistries.BIOME).register { _, _, biome -> WorldGeneration.handleBiome(biome) }
    }

    private val DEFAULT_ITEM: () -> Item = { Item(itemSettings()) }

    val HAMMER = IRCraftingToolItem(itemSettings().maxDamage(32))

    val NIKOLITE_ORE = { Registry.BLOCK.get(identifier("nikolite_ore")) }
    val COPPER_ORE = { Registry.BLOCK.get(identifier("copper_ore")) }
    val TIN_ORE = { Registry.BLOCK.get(identifier("tin_ore")) }
    val STEEL_INGOT = { Registry.ITEM.get(identifier("steel_ingot")) }
    val COPPER_INGOT = { Registry.ITEM.get(identifier("copper_ingot")) }
    val TIN_INGOT = { Registry.ITEM.get(identifier("tin_ingot")) }

    val BIOMASS = DEFAULT_ITEM()

    val FAN = IRCoolerItem(itemSettings().maxDamage(512), 0.07)
    val COOLER_CELL = IRCoolerItem(itemSettings().maxDamage(256), 0.1)
    val HEATSINK = IRCoolerItem(itemSettings().maxDamage(128), 3.9)
    val HEAT_COIL = object : Item(itemSettings().maxDamage(128)) {
        override fun appendTooltip(
            stack: ItemStack?,
            world: World?,
            tooltip: MutableList<Text>?,
            context: TooltipContext?
        ) {
            tooltip?.add(TranslatableText("item.indrev.heat_coil.tooltip").formatted(Formatting.BLUE))
        }
    }

    val MINING_DRILL_MK1 =
        IRMiningDrillItem(ToolMaterials.STONE, Tier.MK1, 4000.0, 6f, itemSettings().customDamage(EnergyDamageHandler))
    val MINING_DRILL_MK2 =
        IRMiningDrillItem(ToolMaterials.IRON, Tier.MK2, 8000.0, 10f, itemSettings().customDamage(EnergyDamageHandler))
    val MINING_DRILL_MK3 =
        IRMiningDrillItem(ToolMaterials.DIAMOND, Tier.MK3, 16000.0, 14f, itemSettings().customDamage(EnergyDamageHandler))
    val MINING_DRILL_MK4 = IRModularDrillItem(
        ToolMaterials.NETHERITE, Tier.MK4, 32000.0, 16f, itemSettings().fireproof().customDamage(EnergyDamageHandler)
    )

    val CHUNK_SCANNER_ITEM = IRChunkScannerItem(itemSettings())
    val SCAN_OUTPUT_ITEM = IRResourceReportItem(itemSettings().maxCount(1))

    val ENERGY_READER = IREnergyReaderItem(itemSettings())

    val SULFUR_CRYSTAL_CLUSTER = SulfurCrystalBlock(FabricBlockSettings.of(Material.METAL).sounds(BlockSoundGroup.GLASS).requiresTool().strength(3f, 3f))
    val SULFUR_CRYSTAL_ITEM = DEFAULT_ITEM()

    val COOLANT_IDENTIFIER = identifier("coolant")
    val COOLANT_STILL: BaseFluid.Still =
        BaseFluid.Still(COOLANT_IDENTIFIER, { COOLANT }, { COOLANT_BUCKET }, 0x0C2340) { COOLANT_FLOWING }
    val COOLANT_FLOWING =
        BaseFluid.Flowing(COOLANT_IDENTIFIER, { COOLANT }, { COOLANT_BUCKET }, 0x0C2340) { COOLANT_STILL }
    val COOLANT_BUCKET = BucketItem(COOLANT_STILL, itemSettings().recipeRemainder(Items.BUCKET).maxCount(1))
    val COOLANT = object : FluidBlock(COOLANT_STILL, FabricBlockSettings.of(Material.LAVA)) {}

    val MOLTEN_NETHERITE_IDENTIFIER = identifier("molten_netherite")
    val MOLTEN_NETHERITE_STILL: BaseFluid.Still = BaseFluid.Still(MOLTEN_NETHERITE_IDENTIFIER, { MOLTEN_NETHERITE }, { MOLTEN_NETHERITE_BUCKET }, NETHERITE_SCRAP_BASE.toInt()) { MOLTEN_NETHERITE_FLOWING }
    val MOLTEN_NETHERITE_FLOWING = BaseFluid.Flowing(MOLTEN_NETHERITE_IDENTIFIER, { MOLTEN_NETHERITE }, { MOLTEN_NETHERITE_BUCKET }, NETHERITE_SCRAP_BASE.toInt()) { MOLTEN_NETHERITE_STILL }
    val MOLTEN_NETHERITE_BUCKET = BucketItem(MOLTEN_NETHERITE_STILL, itemSettings().recipeRemainder(Items.BUCKET).maxCount(1))
    val MOLTEN_NETHERITE = object : FluidBlock(MOLTEN_NETHERITE_STILL, FabricBlockSettings.of(Material.LAVA)) {}

    val MOLTEN_IRON_IDENTIFIER = identifier("molten_iron")
    val MOLTEN_IRON_STILL: BaseFluid.Still = BaseFluid.Still(MOLTEN_IRON_IDENTIFIER, { MOLTEN_IRON }, { MOLTEN_IRON_BUCKET }, IRON_BASE.toInt()) { MOLTEN_IRON_FLOWING }
    val MOLTEN_IRON_FLOWING = BaseFluid.Flowing(MOLTEN_IRON_IDENTIFIER, { MOLTEN_IRON }, { MOLTEN_IRON_BUCKET }, IRON_BASE.toInt()) { MOLTEN_IRON_STILL }
    val MOLTEN_IRON_BUCKET = BucketItem(MOLTEN_IRON_STILL, itemSettings().recipeRemainder(Items.BUCKET).maxCount(1))
    val MOLTEN_IRON = object : FluidBlock(MOLTEN_IRON_STILL, FabricBlockSettings.of(Material.LAVA)) {}

    val MOLTEN_GOLD_IDENTIFIER = identifier("molten_gold")
    val MOLTEN_GOLD_STILL: BaseFluid.Still = BaseFluid.Still(MOLTEN_GOLD_IDENTIFIER, { MOLTEN_GOLD }, { MOLTEN_GOLD_BUCKET }, 0xFFCC00) { MOLTEN_GOLD_FLOWING }
    val MOLTEN_GOLD_FLOWING = BaseFluid.Flowing(MOLTEN_GOLD_IDENTIFIER, { MOLTEN_GOLD }, { MOLTEN_GOLD_BUCKET }, 0xFFCC00) { MOLTEN_GOLD_STILL }
    val MOLTEN_GOLD_BUCKET = BucketItem(MOLTEN_GOLD_STILL, itemSettings().recipeRemainder(Items.BUCKET).maxCount(1))
    val MOLTEN_GOLD = object : FluidBlock(MOLTEN_GOLD_STILL, FabricBlockSettings.of(Material.LAVA)) {}

    val MOLTEN_COPPER_IDENTIFIER = identifier("molten_copper")
    val MOLTEN_COPPER_STILL: BaseFluid.Still = BaseFluid.Still(MOLTEN_COPPER_IDENTIFIER, { MOLTEN_COPPER }, { MOLTEN_COPPER_BUCKET }, COPPER_BASE.toInt()) { MOLTEN_COPPER_FLOWING }
    val MOLTEN_COPPER_FLOWING = BaseFluid.Flowing(MOLTEN_COPPER_IDENTIFIER, { MOLTEN_COPPER }, { MOLTEN_COPPER_BUCKET }, COPPER_BASE.toInt()) { MOLTEN_COPPER_STILL }
    val MOLTEN_COPPER_BUCKET = BucketItem(MOLTEN_COPPER_STILL, itemSettings().recipeRemainder(Items.BUCKET).maxCount(1))
    val MOLTEN_COPPER = object : FluidBlock(MOLTEN_COPPER_STILL, FabricBlockSettings.of(Material.LAVA)) {}

    val MOLTEN_TIN_IDENTIFIER = identifier("molten_tin")
    val MOLTEN_TIN_STILL: BaseFluid.Still =
        BaseFluid.Still(MOLTEN_TIN_IDENTIFIER, { MOLTEN_TIN }, { MOLTEN_TIN_BUCKET }, TIN_BASE.toInt()) { MOLTEN_TIN_FLOWING }
    val MOLTEN_TIN_FLOWING =
        BaseFluid.Flowing(MOLTEN_TIN_IDENTIFIER, { MOLTEN_TIN }, { MOLTEN_TIN_BUCKET }, TIN_BASE.toInt()) { MOLTEN_TIN_STILL }
    val MOLTEN_TIN_BUCKET = BucketItem(MOLTEN_TIN_STILL, itemSettings().recipeRemainder(Items.BUCKET).maxCount(1))
    val MOLTEN_TIN = object : FluidBlock(MOLTEN_TIN_STILL, FabricBlockSettings.of(Material.LAVA)) {}

    val MOLTEN_LEAD_IDENTIFIER = identifier("molten_lead")
    val MOLTEN_LEAD_STILL: BaseFluid.Still =
        BaseFluid.Still(MOLTEN_LEAD_IDENTIFIER, { MOLTEN_LEAD }, { MOLTEN_LEAD_BUCKET }, LEAD_BASE.toInt()) { MOLTEN_LEAD_FLOWING }
    val MOLTEN_LEAD_FLOWING =
        BaseFluid.Flowing(MOLTEN_LEAD_IDENTIFIER, { MOLTEN_LEAD }, { MOLTEN_LEAD_BUCKET }, LEAD_BASE.toInt()) { MOLTEN_LEAD_STILL }
    val MOLTEN_LEAD_BUCKET = BucketItem(MOLTEN_LEAD_STILL, itemSettings().recipeRemainder(Items.BUCKET).maxCount(1))
    val MOLTEN_LEAD = object : FluidBlock(MOLTEN_LEAD_STILL, FabricBlockSettings.of(Material.LAVA)) {}

    val MOLTEN_SILVER_IDENTIFIER = identifier("molten_silver")
    val MOLTEN_SILVER_STILL: BaseFluid.Still =
        BaseFluid.Still(MOLTEN_SILVER_IDENTIFIER, { MOLTEN_SILVER }, { MOLTEN_SILVER_BUCKET }, SILVER_BASE.toInt()) { MOLTEN_SILVER_FLOWING }
    val MOLTEN_SILVER_FLOWING =
        BaseFluid.Flowing(MOLTEN_SILVER_IDENTIFIER, { MOLTEN_SILVER }, { MOLTEN_SILVER_BUCKET }, SILVER_BASE.toInt()) { MOLTEN_SILVER_STILL }
    val MOLTEN_SILVER_BUCKET = BucketItem(MOLTEN_SILVER_STILL, itemSettings().recipeRemainder(Items.BUCKET).maxCount(1))
    val MOLTEN_SILVER = object : FluidBlock(MOLTEN_SILVER_STILL, FabricBlockSettings.of(Material.LAVA)) {}

    val ACID_MATERIAL: Material =
        FabricMaterialBuilder(MaterialColor.GREEN).allowsMovement().lightPassesThrough().notSolid().replaceable()
            .liquid().build()
    val MUD_MATERIAL: Material =
        FabricMaterialBuilder(MaterialColor.BROWN).allowsMovement().lightPassesThrough().notSolid().replaceable()
            .liquid().build()

    val SULFURIC_ACID_IDENTIFIER = identifier("sulfuric_acid")
    val SULFURIC_ACID_STILL: BaseFluid.Still = BaseFluid.Still(SULFURIC_ACID_IDENTIFIER, { SULFURIC_ACID }, { SULFURIC_ACID_BUCKET }, 0x003D1E) { SULFURIC_ACID_FLOWING }
    val SULFURIC_ACID_FLOWING = BaseFluid.Flowing(SULFURIC_ACID_IDENTIFIER, { SULFURIC_ACID }, { SULFURIC_ACID_BUCKET }, 0x003D1E) { SULFURIC_ACID_STILL }
    val SULFURIC_ACID_BUCKET = BucketItem(SULFURIC_ACID_STILL, itemSettings().recipeRemainder(Items.BUCKET).maxCount(1))
    val SULFURIC_ACID = AcidFluidBlock(SULFURIC_ACID_STILL, FabricBlockSettings.of(ACID_MATERIAL).ticksRandomly())

    val TOXIC_MUD_IDENTIFIER = identifier("toxic_mud")
    val TOXIC_MUD_STILL: BaseFluid.Still =
        BaseFluid.Still(TOXIC_MUD_IDENTIFIER, { TOXIC_MUD }, { TOXIC_MUD_BUCKET }, 0x5c3b0e) { TOXIC_MUD_FLOWING }
    val TOXIC_MUD_FLOWING =
        BaseFluid.Flowing(TOXIC_MUD_IDENTIFIER, { TOXIC_MUD }, { TOXIC_MUD_BUCKET }, 0x5c3b0e) { TOXIC_MUD_STILL }
    val TOXIC_MUD_BUCKET = BucketItem(TOXIC_MUD_STILL, itemSettings().recipeRemainder(Items.BUCKET).maxCount(1))
    val TOXIC_MUD = AcidFluidBlock(TOXIC_MUD_STILL, FabricBlockSettings.of(MUD_MATERIAL))

    val MACHINE_BLOCK = Block(
        FabricBlockSettings.of(Material.METAL).requiresTool().breakByTool(FabricToolTags.PICKAXES, 2).strength(3F, 6F)
    )
    val PLANKS = PlankBlock(
        FabricBlockSettings.of(Material.WOOD).breakByTool(FabricToolTags.AXES, 2).strength(3F, 6F)
    )
    val PLANK_BLOCK = Block(
        FabricBlockSettings.of(Material.WOOD).breakByTool(FabricToolTags.AXES, 2).strength(3F, 6F)
    )

    val CONTROLLER =  HorizontalFacingBlock(
        FabricBlockSettings.of(Material.METAL).requiresTool().breakByTool(FabricToolTags.PICKAXES, 2).strength(3F, 6F)
    )
    val DUCT =  DuctBlock(
        FabricBlockSettings.of(Material.METAL).requiresTool().breakByTool(FabricToolTags.PICKAXES, 2).strength(3F, 6F)
    )
    val FRAME = Block(
        FabricBlockSettings.of(Material.METAL).requiresTool().breakByTool(FabricToolTags.PICKAXES, 2).strength(3F, 6F)
    )
    val SILO = Block(
        FabricBlockSettings.of(Material.METAL).requiresTool().breakByTool(FabricToolTags.PICKAXES, 2).strength(3F, 6F)
    )
    val WARNING_STROBE = WarningStrobeBlock(
        FabricBlockSettings.of(Material.METAL).requiresTool().luminance(15).nonOpaque().breakByTool(FabricToolTags.PICKAXES, 2).strength(3F, 6F)
    )
    val INTAKE =  HorizontalFacingBlock(
        FabricBlockSettings.of(Material.METAL).requiresTool().breakByTool(FabricToolTags.PICKAXES, 2).strength(3F, 6F)
    )
    val CABINET = CabinetBlock(
        FabricBlockSettings.of(Material.METAL).requiresTool().breakByTool(FabricToolTags.PICKAXES, 2).strength(3F, 6F)
    )
    val CABINET_BLOCK_ENTITY_TYPE: BlockEntityType<CabinetBlockEntity> = BlockEntityType.Builder.create({ CabinetBlockEntity() }, CABINET).build(null)

    val DRILL_TOP = DrillBlock.TopDrillBlock(
        FabricBlockSettings.of(Material.METAL).requiresTool().nonOpaque().breakByTool(FabricToolTags.PICKAXES, 2).strength(3F, 6F)
    )
    val DRILL_MIDDLE = DrillBlock.MiddleDrillBlock(
        FabricBlockSettings.of(Material.METAL).requiresTool().nonOpaque().breakByTool(FabricToolTags.PICKAXES, 2).strength(3F, 6F)
    )
    val DRILL_BOTTOM = DrillBlock.BottomDrillBlock(
        FabricBlockSettings.of(Material.METAL).requiresTool().nonOpaque().breakByTool(FabricToolTags.PICKAXES, 2).strength(3F, 6F)
    )
    val DRILL_BLOCK_ENTITY_TYPE: BlockEntityType<DrillBlockEntity> = BlockEntityType.Builder.create({ DrillBlockEntity() }, DRILL_BOTTOM).build(null)
    
    val STONE_DRILL_HEAD = Item(itemSettings().maxDamage(256))
    val IRON_DRILL_HEAD = Item(itemSettings().maxDamage(1024))
    val DIAMOND_DRILL_HEAD = Item(itemSettings().maxDamage(2048))
    val NETHERITE_DRILL_HEAD = Item(itemSettings().maxDamage(4096))

    val BUFFER_UPGRADE = IRUpgradeItem(itemSettings().maxCount(32), Upgrade.BUFFER)
    val SPEED_UPGRADE = IRUpgradeItem(itemSettings().maxCount(32), Upgrade.SPEED)
    val ENERGY_UPGRADE = IRUpgradeItem(itemSettings().maxCount(32), Upgrade.ENERGY)
    val BLAST_FURNACE_UPGRADE = IRUpgradeItem(itemSettings().maxCount(1), Upgrade.BLAST_FURNACE)
    val SMOKER_UPGRADE = IRUpgradeItem(itemSettings().maxCount(1), Upgrade.SMOKER)

    val WRENCH = IRWrenchItem(itemSettings())

    val TECH_SOUP = Item(itemSettings().food(FoodComponent.Builder().hunger(12).saturationModifier(0.6f).build()))

    val MODULAR_ARMOR_HELMET = IRModularArmorItem(EquipmentSlot.HEAD, 250000.0, itemSettings().rarity(Rarity.EPIC).customDamage(EnergyDamageHandler))
    val MODULAR_ARMOR_CHEST = IRModularArmorItem(EquipmentSlot.CHEST, 250000.0, itemSettings().rarity(Rarity.EPIC).customDamage(EnergyDamageHandler))
    val MODULAR_ARMOR_LEGGINGS = IRModularArmorItem(EquipmentSlot.LEGS, 250000.0, itemSettings().rarity(Rarity.EPIC).customDamage(EnergyDamageHandler))
    val MODULAR_ARMOR_BOOTS = IRModularArmorItem(EquipmentSlot.FEET, 250000.0, itemSettings().rarity(Rarity.EPIC).customDamage(EnergyDamageHandler))

    val PROTECTION_MODULE_ITEM = IRModuleItem(ArmorModule.PROTECTION, itemSettings().maxCount(1))
    val SPEED_MODULE_ITEM = IRModuleItem(ArmorModule.SPEED, itemSettings().maxCount(1))
    val JUMP_BOOST_MODULE_ITEM = IRModuleItem(ArmorModule.JUMP_BOOST, itemSettings().maxCount(1))
    val BREATHING_MODULE_ITEM = IRModuleItem(ArmorModule.BREATHING, itemSettings().maxCount(1))
    val NIGHT_VISION_MODULE_ITEM = IRModuleItem(ArmorModule.NIGHT_VISION, itemSettings().maxCount(1))
    val FEATHER_FALLING_MODULE_ITEM = IRModuleItem(ArmorModule.FEATHER_FALLING, itemSettings().maxCount(1))
    val AUTO_FEEDER_MODULE_ITEM = IRModuleItem(ArmorModule.AUTO_FEEDER, itemSettings().maxCount(1))
    val CHARGER_MODULE_ITEM = IRModuleItem(ArmorModule.CHARGER, itemSettings().maxCount(1))
    val SOLAR_PANEL_MODULE_ITEM = IRModuleItem(ArmorModule.SOLAR_PANEL, itemSettings().maxCount(1))
    val PIGLIN_TRICKER_MODULE_ITEM = IRModuleItem(ArmorModule.PIGLIN_TRICKER, itemSettings().maxCount(1))
    val FIRE_RESISTANCE_MODULE_ITEM = IRModuleItem(ArmorModule.FIRE_RESISTANCE, itemSettings().maxCount(1))
    val SILK_TOUCH_MODULE_ITEM = IRModuleItem(DrillModule.SILK_TOUCH, itemSettings().maxCount(1))
    val FORTUNE_MODULE_ITEM = IRModuleItem(DrillModule.FORTUNE, itemSettings().maxCount(1))
    val RANGE_MODULE_ITEM = IRModuleItem(DrillModule.RANGE, itemSettings().maxCount(1))
    val REACH_MODULE_ITEM = IRModuleItem(GamerAxeModule.REACH, itemSettings().maxCount(1))
    val EFFICIENCY_MODULE_ITEM = IRModuleItem(MiningToolModule.EFFICIENCY, itemSettings().maxCount(1))
    val LOOTING_MODULE_ITEM = IRModuleItem(GamerAxeModule.LOOTING, itemSettings().maxCount(1))
    val FIRE_ASPECT_MODULE_ITEM = IRModuleItem(GamerAxeModule.FIRE_ASPECT, itemSettings().maxCount(1))
    val SHARPNESS_MODULE_ITEM = IRModuleItem(GamerAxeModule.SHARPNESS, itemSettings().maxCount(1))

    val PINK_MODULE_ITEM = IRColorModuleItem(0xFF74DD, itemSettings().maxCount(1))
    val RED_MODULE_ITEM = IRColorModuleItem(0xFF747C, itemSettings().maxCount(1))
    val PURPLE_MODULE_ITEM = IRColorModuleItem(0xD174FF, itemSettings().maxCount(1))
    val BLUE_MODULE_ITEM = IRColorModuleItem(0x7974FF, itemSettings().maxCount(1))
    val CYAN_MODULE_ITEM = IRColorModuleItem(0x74FFFC, itemSettings().maxCount(1))
    val GREEN_MODULE_ITEM = IRColorModuleItem(0x7BFF74, itemSettings().maxCount(1))
    val YELLOW_MODULE_ITEM = IRColorModuleItem(0xF7FF74, itemSettings().maxCount(1))
    val ORANGE_MODULE_ITEM = IRColorModuleItem(0xFFA674, itemSettings().maxCount(1))
    val BLACK_MODULE_ITEM = IRColorModuleItem(0x424242, itemSettings().maxCount(1))
    val BROWN_MODULE_ITEM = IRColorModuleItem(0x935F42, itemSettings().maxCount(1))

    val PORTABLE_CHARGER_ITEM = IRPortableChargerItem(itemSettings().maxDamage(250000), 250000.0)

    val GAMER_AXE_ITEM =
        IRGamerAxeItem(ToolMaterials.NETHERITE, 10000.0, Tier.MK4, 4f, -2f, itemSettings().rarity(Rarity.EPIC).customDamage(EnergyDamageHandler))

    val TANK_BLOCK = TankBlock(FabricBlockSettings.of(Material.GLASS).nonOpaque().strength(1f, 1f))

    val TANK_BLOCK_ITEM = BlockItem(TANK_BLOCK, itemSettings())

    val TANK_BLOCK_ENTITY: BlockEntityType<TankBlockEntity> = BlockEntityType.Builder.create({ TankBlockEntity() }, TANK_BLOCK).build(null)
}