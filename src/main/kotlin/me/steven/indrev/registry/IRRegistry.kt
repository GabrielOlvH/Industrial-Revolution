package me.steven.indrev.registry

import me.sargunvohra.mcmods.autoconfig1u.AutoConfig
import me.sargunvohra.mcmods.autoconfig1u.ConfigData
import me.sargunvohra.mcmods.autoconfig1u.serializer.GsonConfigSerializer
import me.sargunvohra.mcmods.autoconfig1u.serializer.PartitioningSerializer
import me.steven.indrev.armor.IRArmorMaterial
import me.steven.indrev.armor.Module
import me.steven.indrev.config.IRConfig
import me.steven.indrev.fluids.BaseFluid
import me.steven.indrev.items.armor.IRColorModuleItem
import me.steven.indrev.items.armor.IRModularArmor
import me.steven.indrev.items.armor.IRModuleItem
import me.steven.indrev.items.energy.IRBatteryItem
import me.steven.indrev.items.energy.IRGamerAxeItem
import me.steven.indrev.items.energy.IRMiningDrill
import me.steven.indrev.items.energy.IRPortableChargerItem
import me.steven.indrev.items.misc.*
import me.steven.indrev.items.tools.*
import me.steven.indrev.items.upgrade.IRUpgradeItem
import me.steven.indrev.items.upgrade.Upgrade
import me.steven.indrev.tools.IRToolMaterial
import me.steven.indrev.utils.*
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.fabricmc.fabric.api.tool.attribute.v1.FabricToolTags
import net.minecraft.block.Block
import net.minecraft.block.FluidBlock
import net.minecraft.block.Material
import net.minecraft.entity.EquipmentSlot
import net.minecraft.item.*
import net.minecraft.util.registry.BuiltinRegistries
import net.minecraft.util.registry.Registry

@Suppress("MemberVisibilityCanBePrivate")
object IRRegistry {
    fun registerAll() {
        AutoConfig.register(
            IRConfig::class.java,
            PartitioningSerializer.wrap<IRConfig, ConfigData>(::GsonConfigSerializer)
        )
        ResourceHelper("tin") {
            withItems("dust", "ingot", "plate", "nugget", "chunk", "purified_ore")
            withBlock()
            withOre()
            withTools(
                IRBasicPickaxe(IRToolMaterial.TIN, 1, -2.8f, itemSettings()),
                IRBasicAxe(IRToolMaterial.TIN, 6f, -3.1f, itemSettings()),
                IRBasicShovel(IRToolMaterial.TIN, 1.5f, -3f, itemSettings()),
                IRBasicSword(IRToolMaterial.TIN, 3, -2.5f, itemSettings()),
                IRBasicHoe(IRToolMaterial.TIN, 2, -3.0f, itemSettings())
            )
            withArmor(IRArmorMaterial.TIN)
        }.register()
        ResourceHelper("copper") {
            withItems("dust", "ingot", "plate", "nugget", "chunk", "purified_ore")
            withBlock()
            withOre()
            withTools(
                IRBasicPickaxe(IRToolMaterial.COPPER, 1, -2.8f, itemSettings()),
                IRBasicAxe(IRToolMaterial.COPPER, 6f, -3.1f, itemSettings()),
                IRBasicShovel(IRToolMaterial.COPPER, 1.5f, -3f, itemSettings()),
                IRBasicSword(IRToolMaterial.COPPER, 4, -2.6f, itemSettings()),
                IRBasicHoe(IRToolMaterial.COPPER, 2, -3.0f, itemSettings())
            )
            withArmor(IRArmorMaterial.COPPER)
        }.register()
        ResourceHelper("steel") {
            withItems("dust", "ingot", "plate", "nugget")
            withBlock()
            withTools(
                IRBasicPickaxe(IRToolMaterial.STEEL, 1, -2.8f, itemSettings()),
                IRBasicAxe(IRToolMaterial.STEEL, 7f, -5.0f, itemSettings()),
                IRBasicShovel(IRToolMaterial.STEEL, 1.5f, -3f, itemSettings()),
                IRBasicSword(IRToolMaterial.STEEL, 5, -2.8f, itemSettings()),
                IRBasicHoe(IRToolMaterial.STEEL, 2, -3.0f, itemSettings())
            )
            withArmor(IRArmorMaterial.STEEL)
        }.register()
        ResourceHelper("iron") { withItems("dust", "plate", "chunk", "purified_ore") }.register()
        ResourceHelper("nikolite") {
            withItems("dust", "ingot")
            withOre()
        }.register()
        ResourceHelper("enriched_nikolite") { withItems("dust", "ingot") }.register()
        ResourceHelper("diamond") { withItems("dust") }.register()
        ResourceHelper("gold") { withItems("dust", "plate", "chunk", "purified_ore") }.register()
        ResourceHelper("coal") { withItems("dust") }.register()

        WorldGeneration.init()
        BuiltinRegistries.BIOME.forEach { biome -> WorldGeneration.handleBiome(biome) }
        //RegistryEntryAddedCallback.event(BuiltinRegistries.BIOME)
        //.register(RegistryEntryAddedCallback { _, _, biome -> WorldGeneration.handleBiome(biome) })

        identifier("hammer").item(HAMMER)

        identifier("mining_drill").tierBasedItem { tier ->
            when (tier) {
                Tier.MK1 -> MINING_DRILL_MK1
                Tier.MK2 -> MINING_DRILL_MK2
                Tier.MK3 -> MINING_DRILL_MK3
                Tier.MK4, Tier.CREATIVE -> MINING_DRILL_MK4
            }
        }
        identifier("battery").item(IRBatteryItem(itemSettings().maxDamage(4096), 4096.0, true))
        identifier("circuit").tierBasedItem { DEFAULT_ITEM() }

        identifier("machine_block").block(MACHINE_BLOCK).item(BlockItem(MACHINE_BLOCK, itemSettings()))

        identifier("fan").item(FAN)
        identifier("cooler_cell").item(COOLER_CELL)
        identifier("heatsink").item(HEATSINK)

        identifier("chunk_scanner").item(CHUNK_SCANNER_ITEM)
        identifier("scan_output").item(SCAN_OUTPUT_ITEM)

        identifier("empty_upgrade").item(DEFAULT_ITEM())
        identifier("buffer_upgrade").item(BUFFER_UPGRADE)
        identifier("speed_upgrade").item(SPEED_UPGRADE)
        identifier("energy_upgrade").item(ENERGY_UPGRADE)

        identifier("energy_reader").item(ENERGY_READER)

        identifier("area_indicator").block(AREA_INDICATOR)

        identifier("tier_upgrade_mk2").item(IRMachineUpgradeItem(itemSettings(), Tier.MK1, Tier.MK2))
        identifier("tier_upgrade_mk3").item(IRMachineUpgradeItem(itemSettings(), Tier.MK2, Tier.MK3))
        identifier("tier_upgrade_mk4").item(IRMachineUpgradeItem(itemSettings(), Tier.MK3, Tier.MK4))

        identifier("biomass").item(BIOMASS)

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

    val MINING_DRILL_MK1 =
        IRMiningDrill(ToolMaterials.STONE, Tier.MK1, 4000.0, 6f, itemSettings().maxDamage(4000))
    val MINING_DRILL_MK2 =
        IRMiningDrill(ToolMaterials.IRON, Tier.MK2, 8000.0, 10f, itemSettings().maxDamage(8000))
    val MINING_DRILL_MK3 =
        IRMiningDrill(ToolMaterials.DIAMOND, Tier.MK3, 16000.0, 14f, itemSettings().maxDamage(16000))
    val MINING_DRILL_MK4 = IRMiningDrill(
        ToolMaterials.NETHERITE, Tier.MK4, 32000.0, 50f, itemSettings().maxDamage(32000)
    )

    val CHUNK_SCANNER_ITEM = IRChunkScannerItem(itemSettings())
    val SCAN_OUTPUT_ITEM = IRScanOutputItem(itemSettings().maxCount(1))

    val ENERGY_READER = IREnergyReader(itemSettings())

    val AREA_INDICATOR = Block(FabricBlockSettings.of(Material.WOOL))

    val COOLANT_IDENTIFIER = identifier("coolant")
    val COOLANT_STILL: BaseFluid.Still =
        BaseFluid.Still(COOLANT_IDENTIFIER, { COOLANT }, { COOLANT_BUCKET }, 0x0C2340) { COOLANT_FLOWING }
    val COOLANT_FLOWING =
        BaseFluid.Flowing(COOLANT_IDENTIFIER, { COOLANT }, { COOLANT_BUCKET }, 0x0C2340) { COOLANT_STILL }
    val COOLANT_BUCKET = BucketItem(COOLANT_STILL, itemSettings().recipeRemainder(Items.BUCKET))
    val COOLANT = object : FluidBlock(COOLANT_STILL, FabricBlockSettings.of(Material.LAVA)) {}

    val MOLTEN_NETHERITE_IDENTIFIER = identifier("molten_netherite")
    val MOLTEN_NETHERITE_STILL: BaseFluid.Still = BaseFluid.Still(
        MOLTEN_NETHERITE_IDENTIFIER,
        { MOLTEN_NETHERITE },
        { MOLTEN_NETHERITE_BUCKET },
        0x654740
    ) { MOLTEN_NETHERITE_FLOWING }
    val MOLTEN_NETHERITE_FLOWING = BaseFluid.Flowing(
        MOLTEN_NETHERITE_IDENTIFIER,
        { MOLTEN_NETHERITE },
        { MOLTEN_NETHERITE_BUCKET },
        0x654740
    ) { MOLTEN_NETHERITE_STILL }
    val MOLTEN_NETHERITE_BUCKET = BucketItem(MOLTEN_NETHERITE_STILL, itemSettings().recipeRemainder(Items.BUCKET))
    val MOLTEN_NETHERITE = object : FluidBlock(MOLTEN_NETHERITE_STILL, FabricBlockSettings.of(Material.LAVA)) {}

    val MOLTEN_IRON_IDENTIFIER = identifier("molten_iron")
    val MOLTEN_IRON_STILL: BaseFluid.Still = BaseFluid.Still(
        MOLTEN_IRON_IDENTIFIER,
        { MOLTEN_IRON },
        { MOLTEN_IRON_BUCKET },
        0x7A0019
    ) { MOLTEN_IRON_FLOWING }
    val MOLTEN_IRON_FLOWING = BaseFluid.Flowing(
        MOLTEN_IRON_IDENTIFIER,
        { MOLTEN_IRON },
        { MOLTEN_IRON_BUCKET },
        0x7A0019
    ) { MOLTEN_IRON_STILL }
    val MOLTEN_IRON_BUCKET = BucketItem(MOLTEN_IRON_STILL, itemSettings().recipeRemainder(Items.BUCKET))
    val MOLTEN_IRON = object : FluidBlock(MOLTEN_IRON_STILL, FabricBlockSettings.of(Material.LAVA)) {}

    val MOLTEN_GOLD_IDENTIFIER = identifier("molten_gold")
    val MOLTEN_GOLD_STILL: BaseFluid.Still = BaseFluid.Still(
        MOLTEN_GOLD_IDENTIFIER,
        { MOLTEN_GOLD },
        { MOLTEN_GOLD_BUCKET },
        0xD8C800
    ) { MOLTEN_GOLD_FLOWING }
    val MOLTEN_GOLD_FLOWING = BaseFluid.Flowing(
        MOLTEN_GOLD_IDENTIFIER,
        { MOLTEN_GOLD },
        { MOLTEN_GOLD_BUCKET },
        0xD8C800
    ) { MOLTEN_GOLD_STILL }
    val MOLTEN_GOLD_BUCKET = BucketItem(MOLTEN_GOLD_STILL, itemSettings().recipeRemainder(Items.BUCKET))
    val MOLTEN_GOLD = object : FluidBlock(MOLTEN_GOLD_STILL, FabricBlockSettings.of(Material.LAVA)) {}

    val MOLTEN_COPPER_IDENTIFIER = identifier("molten_copper")
    val MOLTEN_COPPER_STILL: BaseFluid.Still = BaseFluid.Still(
        MOLTEN_COPPER_IDENTIFIER,
        { MOLTEN_COPPER },
        { MOLTEN_COPPER_BUCKET },
        0xEA7708
    ) { MOLTEN_COPPER_FLOWING }
    val MOLTEN_COPPER_FLOWING = BaseFluid.Flowing(
        MOLTEN_COPPER_IDENTIFIER,
        { MOLTEN_COPPER },
        { MOLTEN_COPPER_BUCKET },
        0xEA7708
    ) { MOLTEN_COPPER_STILL }
    val MOLTEN_COPPER_BUCKET = BucketItem(MOLTEN_COPPER_STILL, itemSettings().recipeRemainder(Items.BUCKET))
    val MOLTEN_COPPER = object : FluidBlock(MOLTEN_COPPER_STILL, FabricBlockSettings.of(Material.LAVA)) {}

    val MOLTEN_TIN_IDENTIFIER = identifier("molten_tin")
    val MOLTEN_TIN_STILL: BaseFluid.Still =
        BaseFluid.Still(MOLTEN_TIN_IDENTIFIER, { MOLTEN_TIN }, { MOLTEN_TIN_BUCKET }, 0xC6C3BF) { MOLTEN_TIN_FLOWING }
    val MOLTEN_TIN_FLOWING =
        BaseFluid.Flowing(MOLTEN_TIN_IDENTIFIER, { MOLTEN_TIN }, { MOLTEN_TIN_BUCKET }, 0xC6C3BF) { MOLTEN_TIN_STILL }
    val MOLTEN_TIN_BUCKET = BucketItem(MOLTEN_TIN_STILL, itemSettings().recipeRemainder(Items.BUCKET))
    val MOLTEN_TIN = object : FluidBlock(MOLTEN_TIN_STILL, FabricBlockSettings.of(Material.LAVA)) {}

    val MACHINE_BLOCK = Block(
        FabricBlockSettings.of(Material.METAL).requiresTool().breakByTool(FabricToolTags.PICKAXES, 2).strength(3F, 6F)
    )

    val BUFFER_UPGRADE = IRUpgradeItem(itemSettings().maxCount(1), Upgrade.BUFFER)
    val SPEED_UPGRADE = IRUpgradeItem(itemSettings().maxCount(1), Upgrade.SPEED)
    val ENERGY_UPGRADE = IRUpgradeItem(itemSettings().maxCount(1), Upgrade.ENERGY)

    val WRENCH = IRWrenchItem(itemSettings().maxDamage(64))

    val TECH_SOUP = Item(itemSettings().food(FoodComponent.Builder().hunger(12).saturationModifier(0.6f).build()))

    val MODULAR_ARMOR_HELMET = IRModularArmor(EquipmentSlot.HEAD, 500000.0, itemSettings().maxDamage(500000))
    val MODULAR_ARMOR_CHEST = IRModularArmor(EquipmentSlot.CHEST, 500000.0, itemSettings().maxDamage(500000))
    val MODULAR_ARMOR_LEGGINGS = IRModularArmor(EquipmentSlot.LEGS, 500000.0, itemSettings().maxDamage(500000))
    val MODULAR_ARMOR_BOOTS = IRModularArmor(EquipmentSlot.FEET, 500000.0, itemSettings().maxDamage(500000))

    val PROTECTION_MODULE_ITEM = IRModuleItem(Module.PROTECTION, itemSettings().maxCount(1))
    val SPEED_MODULE_ITEM = IRModuleItem(Module.SPEED, itemSettings().maxCount(1))
    val JUMP_BOOST_MODULE_ITEM = IRModuleItem(Module.JUMP_BOOST, itemSettings().maxCount(1))
    val BREATHING_MODULE_ITEM = IRModuleItem(Module.BREATHING, itemSettings().maxCount(1))
    val NIGHT_VISION_MODULE_ITEM = IRModuleItem(Module.NIGHT_VISION, itemSettings().maxCount(1))
    val FEATHER_FALLING_MODULE_ITEM = IRModuleItem(Module.FEATHER_FALLING, itemSettings().maxCount(1))
    val AUTO_FEEDER_MODULE_ITEM = IRModuleItem(Module.AUTO_FEEDER, itemSettings().maxCount(1))
    val CHARGER_MODULE_ITEM = IRModuleItem(Module.CHARGER, itemSettings().maxCount(1))
    val SOLAR_PANEL_MODULE_ITEM = IRModuleItem(Module.SOLAR_PANEL, itemSettings().maxCount(1))
    val PIGLIN_TRICKER_MODULE_ITEM = IRModuleItem(Module.PIGLIN_TRICKER, itemSettings().maxCount(1))
    val FIRE_RESISTANCE_MODULE_ITEM = IRModuleItem(Module.FIRE_RESISTANCE, itemSettings().maxCount(1))

    val PINK_MODULE_ITEM = IRColorModuleItem(0xFF74DD, itemSettings())
    val RED_MODULE_ITEM = IRColorModuleItem(0xFF747C, itemSettings())
    val PURPLE_MODULE_ITEM = IRColorModuleItem(0xD174FF, itemSettings())
    val BLUE_MODULE_ITEM = IRColorModuleItem(0x7974FF, itemSettings())
    val CYAN_MODULE_ITEM = IRColorModuleItem(0x74FFFC, itemSettings())
    val GREEN_MODULE_ITEM = IRColorModuleItem(0x7BFF74, itemSettings())
    val YELLOW_MODULE_ITEM = IRColorModuleItem(0xF7FF74, itemSettings())
    val ORANGE_MODULE_ITEM = IRColorModuleItem(0xFFA674, itemSettings())
    val BLACK_MODULE_ITEM = IRColorModuleItem(0x424242, itemSettings())
    val BROWN_MODULE_ITEM = IRColorModuleItem(0x935F42, itemSettings())

    val PORTABLE_CHARGER_ITEM = IRPortableChargerItem(itemSettings().maxDamage(250000), Tier.MK3, 250000.0)

    val GAMER_AXE_ITEM = IRGamerAxeItem(ToolMaterials.NETHERITE, 10000.0, Tier.MK4, 10f, -2f, itemSettings().maxDamage(10000))
}