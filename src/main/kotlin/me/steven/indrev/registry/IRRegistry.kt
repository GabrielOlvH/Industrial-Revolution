package me.steven.indrev.registry

import me.steven.indrev.IndustrialRevolution
import me.steven.indrev.armor.IRArmorMaterial
import me.steven.indrev.armor.Module
import me.steven.indrev.fluids.CoolantFluid
import me.steven.indrev.fluids.MoltenNetheriteFluid
import me.steven.indrev.items.*
import me.steven.indrev.items.armor.IRColorModuleItem
import me.steven.indrev.items.armor.IRModularArmor
import me.steven.indrev.items.armor.IRModuleItem
import me.steven.indrev.items.rechargeable.IRRechargeableItem
import me.steven.indrev.items.tools.*
import me.steven.indrev.items.upgrade.IRUpgradeItem
import me.steven.indrev.items.upgrade.Upgrade
import me.steven.indrev.tools.IRToolMaterial
import me.steven.indrev.utils.*
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback
import net.minecraft.block.Block
import net.minecraft.block.FluidBlock
import net.minecraft.block.Material
import net.minecraft.entity.EquipmentSlot
import net.minecraft.item.*
import net.minecraft.util.registry.Registry

@Suppress("MemberVisibilityCanBePrivate")
object IRRegistry {
    fun registerAll() {
        val oregen = IndustrialRevolution.CONFIG.oregen
        ResourceHelper("tin") {
            withItems("dust", "ingot", "plate", "gear")
            withBlock()
            withOre(if (oregen.tin) ResourceHelper.TIN_FEATURE else null)
            withTools(
                IRBasicPickaxe(IRToolMaterial.TIN, 1, -2.8f, itemSettings()),
                IRBasicAxe(IRToolMaterial.TIN, 6f, -3.1f, itemSettings()),
                IRBasicShovel(IRToolMaterial.TIN, 1.5f, -3f, itemSettings()),
                IRBasicSword(IRToolMaterial.TIN, 3, -3.4f, itemSettings()),
                IRBasicHoe(IRToolMaterial.TIN, -2, -1.0f, itemSettings())
            )
            withArmor(IRArmorMaterial.TIN)
        }.register()
        ResourceHelper("copper") {
            withItems("dust", "ingot", "plate", "gear")
            withBlock()
            withOre(if (oregen.copper) ResourceHelper.COPPER_FEATURE else null)
            withTools(
                IRBasicPickaxe(IRToolMaterial.COPPER, 1, -2.8f, itemSettings()),
                IRBasicAxe(IRToolMaterial.COPPER, 6f, -3.1f, itemSettings()),
                IRBasicShovel(IRToolMaterial.COPPER, 1.5f, -3f, itemSettings()),
                IRBasicSword(IRToolMaterial.COPPER, 3, -3.4f, itemSettings()),
                IRBasicHoe(IRToolMaterial.COPPER, -2, -1.0f, itemSettings())
            )
            withArmor(IRArmorMaterial.COPPER)
        }.register()
        ResourceHelper("steel") {
            withItems("dust", "ingot", "plate", "gear")
            withBlock()
            withTools(
                IRBasicPickaxe(IRToolMaterial.STEEL, 1, -2.8f, itemSettings()),
                IRBasicAxe(IRToolMaterial.STEEL, 7f, -3.0f, itemSettings()),
                IRBasicShovel(IRToolMaterial.STEEL, 1.5f, -3f, itemSettings()),
                IRBasicSword(IRToolMaterial.STEEL, 3, -3.4f, itemSettings()),
                IRBasicHoe(IRToolMaterial.STEEL, -2, -1.0f, itemSettings())
            )
            withArmor(IRArmorMaterial.STEEL)
        }.register()
        ResourceHelper("iron") { withItems("dust", "plate", "gear") }.register()
        ResourceHelper("nikolite") {
            withItems("dust", "ingot")
            withOre(if (oregen.nikolite) ResourceHelper.NIKOLITE_FEATURE else null)
        }.register()
        ResourceHelper("enriched_nikolite") { withItems("dust", "ingot") }.register()
        ResourceHelper("diamond") { withItems("dust") }.register()
        ResourceHelper("gold") { withItems("dust") }.register()
        ResourceHelper("coal") { withItems("dust") }.register()

        Registry.BIOME.forEach { biome -> ResourceHelper.registerFeatures(biome) }
        RegistryEntryAddedCallback.event(Registry.BIOME)
            .register(RegistryEntryAddedCallback { _, _, biome -> ResourceHelper.registerFeatures(biome) })

        identifier("hammer").item(HAMMER)

        identifier("mining_drill").tierBasedItem { tier ->
            when (tier) {
                Tier.MK1 -> MINING_DRILL_MK1
                Tier.MK2 -> MINING_DRILL_MK2
                Tier.MK3 -> MINING_DRILL_MK3
                Tier.MK4, Tier.CREATIVE -> MINING_DRILL_MK4
            }
        }
        identifier("battery").item(IRRechargeableItem(itemSettings().maxDamage(4096), 4096.0, true))
        identifier("circuit").tierBasedItem { DEFAULT_ITEM() }

        identifier("machine_block").block(MACHINE_BLOCK).item(BlockItem(MACHINE_BLOCK, itemSettings()))

        identifier("fan").item(FAN)
        identifier("cooler_cell").item(COOLER_CELL)
        identifier("heatsink").item(HEATSINK)

        identifier("chunk_scanner").item(IRChunkScannerItem(itemSettings().maxCount(1)))

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

        identifier("coolant").block(COOLANT)
        identifier("coolant_still").fluid(COOLANT_FLUID_STILL)
        identifier("coolant_flowing").fluid(COOLANT_FLUID_FLOWING)
        identifier("coolant_bucket").item(COOLANT_BUCKET)

        identifier("molten_netherite").block(MOLTEN_NETHERITE)
        identifier("molten_netherite_still").fluid(MOLTEN_NETHERITE_STILL)
        identifier("molten_netherite_flowing").fluid(MOLTEN_NETHERITE_FLOWING)
        identifier("molten_netherite_bucket").item(MOLTEN_NETHERITE_BUCKET)
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
    }

    private val DEFAULT_ITEM: () -> Item = { Item(itemSettings()) }

    val HAMMER = IRCraftingToolItem(itemSettings().maxDamage(32))

    val NIKOLITE_ORE by lazy { Registry.BLOCK.get(identifier("nikolite_ore")) }
    val COPPER_ORE by lazy { Registry.BLOCK.get(identifier("copper_ore")) }
    val TIN_ORE by lazy { Registry.BLOCK.get(identifier("tin_ore")) }
    val STEEL_INGOT by lazy { Registry.ITEM.get(identifier("steel_ingot")) }
    val COPPER_INGOT by lazy { Registry.ITEM.get(identifier("copper_ingot")) }
    val TIN_INGOT by lazy { Registry.ITEM.get(identifier("tin_ingot")) }

    val BIOMASS = DEFAULT_ITEM()

    val FAN = IRCoolerItem(itemSettings().maxDamage(512), 0.07)
    val COOLER_CELL = IRCoolerItem(itemSettings().maxDamage(256), 0.1)
    val HEATSINK = IRCoolerItem(itemSettings().maxDamage(128), 3.9)

    val MINING_DRILL_MK1 =
        IRMiningDrill(ToolMaterials.STONE, Tier.MK1, 32000.0, itemSettings().maxDamage(32000))
    val MINING_DRILL_MK2 =
        IRMiningDrill(ToolMaterials.IRON, Tier.MK2, 32000.0, itemSettings().maxDamage(32000))
    val MINING_DRILL_MK3 =
        IRMiningDrill(ToolMaterials.DIAMOND, Tier.MK3, 32000.0, itemSettings().maxDamage(32000))
    val MINING_DRILL_MK4 = IRMiningDrill(
        ToolMaterials.NETHERITE, Tier.MK4, 32000.0, itemSettings().maxDamage(32000)
    )

    val ENERGY_READER = IREnergyReader(itemSettings())

    val AREA_INDICATOR = Block(FabricBlockSettings.of(Material.WOOL))

    val COOLANT_FLUID_FLOWING = CoolantFluid.Flowing()
    val COOLANT_FLUID_STILL = CoolantFluid.Still()
    val COOLANT_BUCKET = BucketItem(COOLANT_FLUID_STILL, itemSettings())
    val COOLANT = object : FluidBlock(COOLANT_FLUID_STILL, FabricBlockSettings.of(Material.WATER)) {}

    val MOLTEN_NETHERITE_FLOWING = MoltenNetheriteFluid.Flowing()
    val MOLTEN_NETHERITE_STILL = MoltenNetheriteFluid.Still()
    val MOLTEN_NETHERITE_BUCKET = BucketItem(MOLTEN_NETHERITE_STILL, itemSettings())
    val MOLTEN_NETHERITE = object : FluidBlock(MOLTEN_NETHERITE_STILL, FabricBlockSettings.of(Material.LAVA)) {}

    val MACHINE_BLOCK = Block(FabricBlockSettings.of(Material.METAL).strength(3F, 6F))

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
}