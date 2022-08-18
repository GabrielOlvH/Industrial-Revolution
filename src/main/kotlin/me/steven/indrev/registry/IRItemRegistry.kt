package me.steven.indrev.registry

import me.steven.indrev.api.machines.Tier
import me.steven.indrev.armor.IRArmorMaterial
import me.steven.indrev.blocks.misc.NikoliteOreBlock
import me.steven.indrev.items.armor.*
import me.steven.indrev.items.energy.*
import me.steven.indrev.items.misc.*
import me.steven.indrev.items.upgrade.Enhancer
import me.steven.indrev.items.upgrade.IREnhancerItem
import me.steven.indrev.networks.EndpointData
import me.steven.indrev.tools.IRToolMaterial
import me.steven.indrev.tools.modular.ArmorModule
import me.steven.indrev.tools.modular.DrillModule
import me.steven.indrev.tools.modular.GamerAxeModule
import me.steven.indrev.tools.modular.MiningToolModule
import me.steven.indrev.utils.*
import net.minecraft.client.item.TooltipContext
import net.minecraft.entity.EquipmentSlot
import net.minecraft.item.*
import net.minecraft.text.Text
import me.steven.indrev.utils.translatable
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage
import net.minecraft.util.ActionResult
import net.minecraft.util.Formatting
import net.minecraft.util.Rarity
import net.minecraft.util.registry.Registry
import net.minecraft.world.World
import team.reborn.energy.api.EnergyStorage
import team.reborn.energy.impl.SimpleItemEnergyStorageImpl

@Suppress("MemberVisibilityCanBePrivate")
object IRItemRegistry {
    fun registerAll() {

        identifier("guide_book").item(GUIDE_BOOK)

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
            withItems("dust", "plate", "nugget", "chunk", "purified_ore")
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
            withOre(false) { settings -> NikoliteOreBlock(settings) }
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
                IRBasicAxe(IRToolMaterial.LEAD, 3.8f, -2.9f, itemSettings()),
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
                IRBasicAxe(IRToolMaterial.BRONZE, 5f, -2.0f, itemSettings()),
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
                IRBasicAxe(IRToolMaterial.SILVER, 3.5f, -2.5f, itemSettings()),
                IRBasicShovel(IRToolMaterial.SILVER, 1.5f, -0.7f, itemSettings()),
                IRBasicSword(IRToolMaterial.SILVER, 3, -1.5f, itemSettings()),
                IRBasicHoe(IRToolMaterial.SILVER, 2, -0.5f, itemSettings())
            )
            withArmor(IRArmorMaterial.SILVER)
        }.register()

        MaterialHelper("tungsten") {
            withItems("dust", "ingot", "plate", "nugget", "purified_ore")
            withBlock()
            withOre()
        }.register()

        MaterialHelper("electrum") {
            withItems("dust", "ingot", "plate", "nugget")
            withBlock()
        }.register()

        MaterialHelper.register()

        identifier("soot").item(SOOT)
        identifier("carbon_fiber_plate").item(DEFAULT_ITEM())
        identifier("carbon_fiber_rod").item(DEFAULT_ITEM())

        identifier("sawdust").item(DEFAULT_ITEM())

        identifier("hammer").item(HAMMER)

        identifier("stone_drill_head").item(STONE_DRILL_HEAD)
        identifier("iron_drill_head").item(IRON_DRILL_HEAD)
        identifier("diamond_drill_head").item(DIAMOND_DRILL_HEAD)
        identifier("netherite_drill_head").item(NETHERITE_DRILL_HEAD)

        identifier("mining_drill_mk1").item(MINING_DRILL_MK1)
        identifier("mining_drill_mk2").item(MINING_DRILL_MK2)
        identifier("mining_drill_mk3").item(MINING_DRILL_MK3)
        identifier("mining_drill_mk4").item(MINING_DRILL_MK4)

        EnergyStorage.ITEM.registerForItems({ _, ctx -> SimpleItemEnergyStorageImpl.createSimpleStorage(ctx, 4000, Tier.MK1.io, Tier.MK1.io) }, MINING_DRILL_MK1)
        EnergyStorage.ITEM.registerForItems({ _, ctx -> SimpleItemEnergyStorageImpl.createSimpleStorage(ctx, 8000, Tier.MK2.io, Tier.MK2.io) }, MINING_DRILL_MK2)
        EnergyStorage.ITEM.registerForItems({ _, ctx -> SimpleItemEnergyStorageImpl.createSimpleStorage(ctx, 16000, Tier.MK3.io, Tier.MK3.io) }, MINING_DRILL_MK3)
        EnergyStorage.ITEM.registerForItems({ _, ctx -> SimpleItemEnergyStorageImpl.createSimpleStorage(ctx, 32000, Tier.MK4.io, Tier.MK4.io) }, MINING_DRILL_MK4)

        identifier("battery").item(BATTERY)

        identifier("circuit_mk1").item(DEFAULT_ITEM())
        identifier("circuit_mk2").item(DEFAULT_ITEM())
        identifier("circuit_mk3").item(DEFAULT_ITEM())
        identifier("circuit_mk4").item(DEFAULT_ITEM())

        identifier("fan").item(FAN)
        identifier("cooler_cell").item(COOLER_CELL)
        identifier("heatsink").item(HEATSINK)
        identifier("heat_coil").item(HEAT_COIL)

        identifier("ore_data_card").item(ORE_DATA_CARD)

        identifier("empty_enhancer").item(DEFAULT_ITEM())
        identifier("buffer_enhancer").item(BUFFER_ENHANCER)
        identifier("speed_enhancer").item(SPEED_UPGRADE)
        identifier("blast_furnace_enhancer").item(BLAST_FURNACE_UPGRADE)
        identifier("smoker_enhancer").item(SMOKER_UPGRADE)
        identifier("damage_enhancer").item(DAMAGE_UPGRADE)

        identifier("energy_reader").item(ENERGY_READER)

        identifier("tier_upgrade_mk2").item(TIER_UPGRADE_MK2)
        identifier("tier_upgrade_mk3").item(TIER_UPGRADE_MK3)
        identifier("tier_upgrade_mk4").item(TIER_UPGRADE_MK4)

        identifier("biomass").item(BIOMASS)
        identifier("untanned_leather").item(DEFAULT_ITEM())

        identifier("wrench").item(WRENCH)
        identifier("screwdriver").item(SCREWDRIVER)

        identifier("jetpack_mk1").item(JETPACK_MK1)
        identifier("jetpack_mk2").item(JETPACK_MK2)
        identifier("jetpack_mk3").item(JETPACK_MK3)
        identifier("jetpack_mk4").item(JETPACK_MK4)

        identifier("carbon_fiber_helmet_frame").item(DEFAULT_ITEM())
        identifier("carbon_fiber_chest_frame").item(DEFAULT_ITEM())
        identifier("carbon_fiber_legs_frame").item(DEFAULT_ITEM())
        identifier("carbon_fiber_boots_frame").item(DEFAULT_ITEM())

        identifier("modular_armor_helmet").item(MODULAR_ARMOR_HELMET)
        identifier("modular_armor_chest").item(MODULAR_ARMOR_CHEST)
        FluidStorage.ITEM.registerForItems({ stack, ctx -> JetpackHandler.JetpackFluidStorage(MODULAR_ARMOR_CHEST, ctx) }, MODULAR_ARMOR_CHEST)
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
        identifier("module_elytra").item(ELYTRA_MODULE_ITEM)
        identifier("module_jetpack").item(JETPACK_MODULE_ITEM)
        identifier("module_magnet").item(MAGNET_MODULE)
        identifier("module_water_affinity").item(WATER_AFFINITY_MODULE)
        identifier("module_fire_resistance").item(FIRE_RESISTANCE_MODULE_ITEM)
        identifier("module_range").item(RANGE_MODULE_ITEM)
        identifier("module_efficiency").item(EFFICIENCY_MODULE_ITEM)
        identifier("module_fortune").item(FORTUNE_MODULE_ITEM)
        identifier("module_silk_touch").item(SILK_TOUCH_MODULE_ITEM)
        identifier("module_controlled_destruction").item(CONTROLLED_DESTRUCTION_MODULE_ITEM)
        identifier("module_matter_projector").item(MATTER_PROJECTOR_MODULE_ITEM)
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

        identifier("modular_core").item(MODULAR_CORE)
        identifier("modular_core_activated").item(MODULAR_CORE_ACTIVATED)

        identifier("fluid_pipe_mk1").item(FLUID_PIPE_ITEM_MK1)
        identifier("fluid_pipe_mk2").item(FLUID_PIPE_ITEM_MK2)
        identifier("fluid_pipe_mk3").item(FLUID_PIPE_ITEM_MK3)
        identifier("fluid_pipe_mk4").item(FLUID_PIPE_ITEM_MK4)

        identifier("item_pipe_mk1").item(ITEM_PIPE_ITEM_MK1)
        identifier("item_pipe_mk2").item(ITEM_PIPE_ITEM_MK2)
        identifier("item_pipe_mk3").item(ITEM_PIPE_ITEM_MK3)
        identifier("item_pipe_mk4").item(ITEM_PIPE_ITEM_MK4)

        identifier("cable_mk1").item(CABLE_ITEM_MK1)
        identifier("cable_mk2").item(CABLE_ITEM_MK2)
        identifier("cable_mk3").item(CABLE_ITEM_MK3)
        identifier("cable_mk4").item(CABLE_ITEM_MK4)

        identifier("servo_retriever").item(SERVO_RETRIEVER)
        identifier("servo_output").item(SERVO_OUTPUT)

        identifier("reinforced_elytra").item(REINFORCED_ELYTRA)
    }

    private val DEFAULT_ITEM: () -> Item = { Item(itemSettings()) }

    val GUIDE_BOOK = IRGuideBookItem(itemSettings())

    val BATTERY = IRBatteryItem(itemSettings(), 4096)

    val HAMMER = IRCraftingToolItem(itemSettings().maxDamage(32))

    val STEEL_INGOT = { Registry.ITEM.get(identifier("steel_ingot")) }
    val STEEL_PLATE = { Registry.ITEM.get(identifier("steel_plate")) }
    val COPPER_INGOT = { Registry.ITEM.get(identifier("copper_ingot")) }
    val TIN_INGOT = { Registry.ITEM.get(identifier("tin_ingot")) }
    val LEAD_INGOT = { Registry.ITEM.get(identifier("lead_ingot")) }
    val BRONZE_INGOT = { Registry.ITEM.get(identifier("bronze_ingot")) }
    val SILVER_INGOT = { Registry.ITEM.get(identifier("silver_ingot")) }
    val ENRICHED_NIKOLITE_DUST = { Registry.ITEM.get(identifier("enriched_nikolite_dust")) }

    val BIOMASS = DEFAULT_ITEM()

    val FAN = Item(itemSettings().maxDamage(128))
    val COOLER_CELL = Item(itemSettings().maxDamage(512))
    val HEATSINK = Item(itemSettings().maxDamage(1536))
    val HEAT_COIL = object : Item(itemSettings().maxDamage(128)) {
        override fun appendTooltip(
            stack: ItemStack?,
            world: World?,
            tooltip: MutableList<Text>?,
            context: TooltipContext?
        ) {
            tooltip?.add(translatable("item.indrev.heat_coil.tooltip").formatted(Formatting.BLUE))
        }
    }

    val TIER_UPGRADE_MK2 = IRMachineUpgradeItem(itemSettings(), Tier.MK1, Tier.MK2)
    val TIER_UPGRADE_MK3 = IRMachineUpgradeItem(itemSettings(), Tier.MK2, Tier.MK3)
    val TIER_UPGRADE_MK4 = IRMachineUpgradeItem(itemSettings(), Tier.MK3, Tier.MK4)

    val MINING_DRILL_MK1 =
        IRMiningDrillItem(ToolMaterials.STONE, Tier.MK1, 4000.0, 6f, itemSettings())
    val MINING_DRILL_MK2 =
        IRMiningDrillItem(ToolMaterials.IRON, Tier.MK2, 8000.0, 10f, itemSettings())
    val MINING_DRILL_MK3 =
        IRMiningDrillItem(ToolMaterials.DIAMOND, Tier.MK3, 16000.0, 14f, itemSettings())
    val MINING_DRILL_MK4 = IRModularDrillItem(
        ToolMaterials.NETHERITE, Tier.MK4, 32000.0, 16f, itemSettings().fireproof()
    )

    val ORE_DATA_CARD = OreDataCardItem()

    val ENERGY_READER = IREnergyReaderItem(itemSettings())

    val SOOT = DEFAULT_ITEM()

    val SULFUR_CRYSTAL_ITEM = DEFAULT_ITEM()

    val STONE_DRILL_HEAD = Item(itemSettings().maxDamage(256))
    val IRON_DRILL_HEAD = Item(itemSettings().maxDamage(1024))
    val DIAMOND_DRILL_HEAD = Item(itemSettings().maxDamage(2048))
    val NETHERITE_DRILL_HEAD = Item(itemSettings().maxDamage(4096))

    val BUFFER_ENHANCER = IREnhancerItem(itemSettings().maxCount(32), Enhancer.BUFFER)
    val SPEED_UPGRADE = IREnhancerItem(itemSettings().maxCount(32), Enhancer.SPEED)
    val BLAST_FURNACE_UPGRADE = IREnhancerItem(itemSettings().maxCount(1), Enhancer.BLAST_FURNACE)
    val SMOKER_UPGRADE = IREnhancerItem(itemSettings().maxCount(1), Enhancer.SMOKER)
    val DAMAGE_UPGRADE = IREnhancerItem(itemSettings().maxCount(1), Enhancer.DAMAGE)

    val WRENCH = object : Item(itemSettings().maxCount(1)) {
        override fun useOnBlock(context: ItemUsageContext): ActionResult {
            val state = context.world.getBlockState(context.blockPos)
            val blockEntity = context.world.getBlockEntity(context.blockPos)
            return wrench(context.world, context.blockPos, state, blockEntity, context.player, context.stack)
        }
    }
    val SCREWDRIVER = object : Item(itemSettings().maxCount(1)) {
        override fun useOnBlock(context: ItemUsageContext): ActionResult {
            val blockEntity = context.world.getBlockEntity(context.blockPos)
            val state = context.world.getBlockState(context.blockPos)
            return screwdriver(context.world, context.blockPos, state, blockEntity, context.player, context.stack)
        }
    }

    val JETPACK_MK1 = JetpackItem(Tier.MK1)
    val JETPACK_MK2 = JetpackItem(Tier.MK2)
    val JETPACK_MK3 = JetpackItem(Tier.MK3)
    val JETPACK_MK4 = JetpackItem(Tier.MK4)

    val MODULAR_ARMOR_HELMET = IRModularArmorItem(EquipmentSlot.HEAD, 250000, itemSettings().rarity(Rarity.EPIC).customDamage(EnergyDamageHandler))
    val MODULAR_ARMOR_CHEST = IRModularArmorItem(EquipmentSlot.CHEST, 250000, itemSettings().rarity(Rarity.EPIC).customDamage(EnergyDamageHandler))
    val MODULAR_ARMOR_LEGGINGS = IRModularArmorItem(EquipmentSlot.LEGS, 250000, itemSettings().rarity(Rarity.EPIC).customDamage(EnergyDamageHandler))
    val MODULAR_ARMOR_BOOTS = IRModularArmorItem(EquipmentSlot.FEET, 250000, itemSettings().rarity(Rarity.EPIC).customDamage(EnergyDamageHandler))

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
    val ELYTRA_MODULE_ITEM = IRModuleItem(ArmorModule.ELYTRA, itemSettings().maxCount(1))
    val JETPACK_MODULE_ITEM = IRModuleItem(ArmorModule.JETPACK, itemSettings().maxCount(1))
    val MAGNET_MODULE = IRModuleItem(ArmorModule.MAGNET, itemSettings().maxCount(1))
    val WATER_AFFINITY_MODULE = IRModuleItem(ArmorModule.WATER_AFFINITY, itemSettings().maxCount(1))
    val FIRE_RESISTANCE_MODULE_ITEM = IRModuleItem(ArmorModule.FIRE_RESISTANCE, itemSettings().maxCount(1))
    val SILK_TOUCH_MODULE_ITEM = IRModuleItem(DrillModule.SILK_TOUCH, itemSettings().maxCount(1))
    val CONTROLLED_DESTRUCTION_MODULE_ITEM = IRModuleItem(DrillModule.CONTROLLED_DESTRUCTION, itemSettings().maxCount(1))
    val MATTER_PROJECTOR_MODULE_ITEM = IRModuleItem(DrillModule.MATTER_PROJECTOR, itemSettings().maxCount(1))
    val FORTUNE_MODULE_ITEM = IRModuleItem(DrillModule.FORTUNE, itemSettings().maxCount(1))
    val RANGE_MODULE_ITEM = IRModuleItem(DrillModule.RANGE, itemSettings().maxCount(1))
 //   val REACH_MODULE_ITEM = IRModuleItem(GamerAxeModule.REACH, itemSettings().maxCount(1))
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

    val PORTABLE_CHARGER_ITEM = IRPortableChargerItem(itemSettings(), 250000)

    val GAMER_AXE_ITEM =
        IRGamerAxeItem(ToolMaterials.NETHERITE, 10000, Tier.MK4, 4f, -2f, itemSettings().rarity(Rarity.EPIC).customDamage(EnergyDamageHandler))

    val TANK_BLOCK_ITEM = BlockItem(IRBlockRegistry.TANK_BLOCK, itemSettings())

    val CAPSULE_BLOCK_ITEM = BlockItem(IRBlockRegistry.CAPSULE_BLOCK, itemSettings().maxCount(1))

    val MODULAR_CORE: Item = object : Item(itemSettings().maxCount(1)), IREnergyItem {
        override fun appendTooltip(
            stack: ItemStack?,
            world: World?,
            tooltip: MutableList<Text>?,
            context: TooltipContext?
        ) {
            buildEnergyTooltip(stack, tooltip)
        }
    }
    val MODULAR_CORE_ACTIVATED = object : Item(itemSettings().maxCount(1)) {
        override fun hasGlint(stack: ItemStack?): Boolean = true
    }

    val FLUID_PIPE_ITEM_MK1 = BlockItem(IRBlockRegistry.FLUID_PIPE_MK1, itemSettings())
    val FLUID_PIPE_ITEM_MK2 = BlockItem(IRBlockRegistry.FLUID_PIPE_MK2, itemSettings())
    val FLUID_PIPE_ITEM_MK3 = BlockItem(IRBlockRegistry.FLUID_PIPE_MK3, itemSettings())
    val FLUID_PIPE_ITEM_MK4 = BlockItem(IRBlockRegistry.FLUID_PIPE_MK4, itemSettings())

    val ITEM_PIPE_ITEM_MK1 = BlockItem(IRBlockRegistry.ITEM_PIPE_MK1, itemSettings())
    val ITEM_PIPE_ITEM_MK2 = BlockItem(IRBlockRegistry.ITEM_PIPE_MK2, itemSettings())
    val ITEM_PIPE_ITEM_MK3 = BlockItem(IRBlockRegistry.ITEM_PIPE_MK3, itemSettings())
    val ITEM_PIPE_ITEM_MK4 = BlockItem(IRBlockRegistry.ITEM_PIPE_MK4, itemSettings())

    val CABLE_ITEM_MK1 = BlockItem(IRBlockRegistry.CABLE_MK1, itemSettings())
    val CABLE_ITEM_MK2 = BlockItem(IRBlockRegistry.CABLE_MK2, itemSettings())
    val CABLE_ITEM_MK3 = BlockItem(IRBlockRegistry.CABLE_MK3, itemSettings())
    val CABLE_ITEM_MK4 = BlockItem(IRBlockRegistry.CABLE_MK4, itemSettings())

    val SERVO_RETRIEVER = IRServoItem(itemSettings().maxCount(16), EndpointData.Type.RETRIEVER)
    val SERVO_OUTPUT = IRServoItem(itemSettings().maxCount(16), EndpointData.Type.OUTPUT)

    val REINFORCED_ELYTRA = ReinforcedElytraItem()
}
