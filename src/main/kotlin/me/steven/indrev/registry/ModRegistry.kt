package me.steven.indrev.registry

import io.github.cottonmc.resources.type.GenericResourceType
import me.steven.indrev.armor.Module
import me.steven.indrev.fluids.CoolantFluid
import me.steven.indrev.fluids.MoltenNetheriteFluid
import me.steven.indrev.items.*
import me.steven.indrev.items.armor.IRModularArmor
import me.steven.indrev.items.armor.IRModuleItem
import me.steven.indrev.items.rechargeable.IRRechargeableItem
import me.steven.indrev.items.upgrade.IRUpgradeItem
import me.steven.indrev.items.upgrade.Upgrade
import me.steven.indrev.utils.*
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.minecraft.block.Block
import net.minecraft.block.FluidBlock
import net.minecraft.block.Material
import net.minecraft.entity.EquipmentSlot
import net.minecraft.item.*
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry

object ModRegistry {

    fun registerAll() {
        identifier("hammer").item(HAMMER)

        NIKOLITE.registerAll()
        identifier("enriched_nikolite").item(DEFAULT_ITEM())
        identifier("enriched_nikolite_ingot").item(DEFAULT_ITEM())

        identifier("mining_drill").tierBasedItem { tier ->
            when (tier) {
                Tier.MK1 -> MINING_DRILL_MK1
                Tier.MK2 -> MINING_DRILL_MK2
                Tier.MK3 -> MINING_DRILL_MK3
                Tier.MK4, Tier.CREATIVE -> MINING_DRILL_MK4
            }
        }
        identifier("battery").item(IRRechargeableItem(itemSettings().maxDamage(4096), true))
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
    }

    private val DEFAULT_ITEM: () -> Item = { Item(itemSettings()) }

    val HAMMER = IRCraftingToolItem(itemSettings().maxDamage(32))

    val NIKOLITE = GenericResourceType.Builder("nikolite")
        .allOres()
        .withDustAffix()
        .noBlock()
        .build()
        .withItemAffixes("ingot")

    val COPPER_ORE = Registry.BLOCK.get(Identifier("c:copper_ore"))
    val TIN_ORE = Registry.BLOCK.get(Identifier("c:copper_ore"))

    val BIOMASS = DEFAULT_ITEM()

    val FAN = IRCoolerItem(itemSettings().maxDamage(512), 0.07)
    val COOLER_CELL = IRCoolerItem(itemSettings().maxDamage(256), 0.1)
    val HEATSINK = IRCoolerItem(itemSettings().maxDamage(128), 3.9)

    val MINING_DRILL_MK1 =
        IRMiningDrill(ToolMaterials.STONE, itemSettings().maxDamage(32000))
    val MINING_DRILL_MK2 =
        IRMiningDrill(ToolMaterials.IRON, itemSettings().maxDamage(32000))
    val MINING_DRILL_MK3 =
        IRMiningDrill(ToolMaterials.DIAMOND, itemSettings().maxDamage(32000))
    val MINING_DRILL_MK4 = IRMiningDrill(
        ToolMaterials.NETHERITE,
        itemSettings().maxDamage(32000)
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

    val MODULAR_ARMOR_HELMET = IRModularArmor(EquipmentSlot.HEAD, itemSettings().maxDamage(20))
    val MODULAR_ARMOR_CHEST = IRModularArmor(EquipmentSlot.CHEST, itemSettings().maxDamage(20))
    val MODULAR_ARMOR_LEGGINGS = IRModularArmor(EquipmentSlot.LEGS, itemSettings().maxDamage(20))
    val MODULAR_ARMOR_BOOTS = IRModularArmor(EquipmentSlot.FEET, itemSettings().maxDamage(20))

    val PROTECTION_MODULE_ITEM = IRModuleItem(Module.PROTECTION, itemSettings().maxCount(1))
    val SPEED_MODULE_ITEM = IRModuleItem(Module.SPEED, itemSettings().maxCount(1))
    val JUMP_BOOST_MODULE_ITEM = IRModuleItem(Module.JUMP_BOOST, itemSettings().maxCount(1))
    val BREATHING_MODULE_ITEM = IRModuleItem(Module.BREATHING, itemSettings().maxCount(1))
    val NIGHT_VISION_MODULE_ITEM = IRModuleItem(Module.NIGHT_VISION, itemSettings().maxCount(1))
}