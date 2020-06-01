package me.steven.indrev.registry

import me.steven.indrev.items.CoolerItem
import me.steven.indrev.items.CraftingTool
import me.steven.indrev.items.rechargeable.RechargeableItem
import me.steven.indrev.items.rechargeable.RechargeableMiningItem
import me.steven.indrev.items.upgrade.Upgrade
import me.steven.indrev.items.upgrade.UpgradeItem
import me.steven.indrev.utils.block
import me.steven.indrev.utils.identifier
import me.steven.indrev.utils.item
import me.steven.indrev.utils.itemSettings
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.fabricmc.fabric.api.tool.attribute.v1.FabricToolTags
import net.minecraft.block.Block
import net.minecraft.block.Material
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.sound.BlockSoundGroup

class ModRegistry {

    fun registerAll() {
        identifier("hammer").item(HAMMER)
        identifier("cutter").item(CUTTER)

        identifier("copper_ore").block(COPPER_ORE).item(COPPER_ORE_ITEM)
        identifier("pulverized_copper").item(DEFAULT_ITEM())
        identifier("copper_ingot").item(DEFAULT_ITEM())
        identifier("tin_ore").block(TIN_ORE).item(TIN_ORE_ITEM)
        identifier("pulverized_tin").item(DEFAULT_ITEM())
        identifier("tin_ingot").item(DEFAULT_ITEM())
        identifier("nikolite_ore").block(NIKOLITE_ORE).item(NIKOLITE_ORE_ITEM)
        identifier("nikolite").item(DEFAULT_ITEM())
        identifier("nikolite_ingot").item(DEFAULT_ITEM())
        identifier("enriched_nikolite").item(DEFAULT_ITEM())
        identifier("enriched_nikolite_ingot").item(DEFAULT_ITEM())

        identifier("mining_drill").item(MINING_DRILL)

        identifier("pulverized_iron").item(DEFAULT_ITEM())
        identifier("pulverized_gold").item(DEFAULT_ITEM())
        identifier("pulverized_coal").item(DEFAULT_ITEM())
        identifier("pulverized_diamond").item(DEFAULT_ITEM())
        identifier("pulverized_obsidian").item(DEFAULT_ITEM())
        identifier("steel_blend").item(DEFAULT_ITEM())
        identifier("steel_ingot").item(DEFAULT_ITEM())
        identifier("steel_plate").item(DEFAULT_ITEM())
        identifier("iron_plate").item(DEFAULT_ITEM())
        identifier("wire").item(DEFAULT_ITEM())
        identifier("basic_battery").item(BASIC_BATTERY)
        identifier("medium_battery").item(INTERMEDIARY_BATTERY)
        identifier("advanced_battery").item(ADVANCED_BATTERY)
        identifier("ultimate_battery").item(ULTIMATE_BATTERY)
        identifier("basic_circuit").item(DEFAULT_ITEM())
        identifier("intermediary_circuit").item(DEFAULT_ITEM())
        identifier("advanced_circuit").item(DEFAULT_ITEM())
        identifier("ultimate_circuit").item(DEFAULT_ITEM())

        identifier("fan").item(FAN)
        identifier("cooler_cell").item(COOLER_CELL)

        identifier("buffer_upgrade").item(BUFFER_UPGRADE)
        identifier("speed_upgrade").item(SPEED_UPGRADE)
        identifier("energy_upgrade").item(ENERGY_UPGRADE)
    }

    companion object {
        private val ORE_BLOCK_SETTINGS: FabricBlockSettings = FabricBlockSettings.of(Material.STONE).sounds(BlockSoundGroup.STONE).breakByTool(FabricToolTags.PICKAXES)

        private val DEFAULT_ITEM: () -> Item = { Item(itemSettings()) }

        val HAMMER = CraftingTool(itemSettings().maxDamage(32))
        val CUTTER = CraftingTool(itemSettings().maxDamage(32))

        val COPPER_ORE = Block(ORE_BLOCK_SETTINGS)
        val COPPER_ORE_ITEM = BlockItem(COPPER_ORE, itemSettings())
        val TIN_ORE = Block(ORE_BLOCK_SETTINGS)
        val TIN_ORE_ITEM = BlockItem(TIN_ORE, itemSettings())
        val NIKOLITE_ORE = Block(ORE_BLOCK_SETTINGS)
        val NIKOLITE_ORE_ITEM = BlockItem(NIKOLITE_ORE, itemSettings())

        val BASIC_BATTERY = RechargeableItem(itemSettings().maxDamage(256), true)
        val INTERMEDIARY_BATTERY = RechargeableItem(itemSettings().maxDamage(1024), true)
        val ADVANCED_BATTERY = RechargeableItem(itemSettings().maxDamage(4096), true)
        val ULTIMATE_BATTERY = RechargeableItem(itemSettings().maxDamage(8192), true)

        val FAN = CoolerItem(itemSettings().maxDamage(512), -0.07, -0.01)
        val COOLER_CELL = CoolerItem(itemSettings().maxDamage(256), -0.1, -0.05)

        val MINING_DRILL = RechargeableMiningItem(itemSettings().maxDamage(32000))

        val BUFFER_UPGRADE = UpgradeItem(itemSettings().maxCount(1), Upgrade.BUFFER)
        val SPEED_UPGRADE = UpgradeItem(itemSettings().maxCount(1), Upgrade.SPEED)
        val ENERGY_UPGRADE = UpgradeItem(itemSettings().maxCount(1), Upgrade.ENERGY)
    }
}