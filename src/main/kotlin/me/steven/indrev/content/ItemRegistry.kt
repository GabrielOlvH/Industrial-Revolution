package me.steven.indrev.content

import me.steven.indrev.IndustrialRevolution
import me.steven.indrev.block
import me.steven.indrev.identifier
import me.steven.indrev.item
import me.steven.indrev.items.Upgrade
import me.steven.indrev.items.UpgradeItem
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.fabricmc.fabric.api.tool.attribute.v1.FabricToolTags
import net.minecraft.block.Block
import net.minecraft.block.Material
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.sound.BlockSoundGroup

class ItemRegistry {

    fun registerAll() {
        identifier("copper_ore").block(COPPER_ORE).item(COPPER_ORE_ITEM)
        identifier("pulverized_copper").item(PULVERIZED_COPPER)
        identifier("copper_ingot").item(COPPER_INGOT)
        identifier("tin_ore").block(TIN_ORE).item(TIN_ORE_ITEM)
        identifier("pulverized_tin").item(PULVERIZED_TIN)
        identifier("tin_ingot").item(TIN_INGOT)
        identifier("nikolite_ore").block(NIKOLITE_ORE).item(NIKOLITE_ORE_ITEM)
        identifier("nikolite").item(NIKOLITE)

        identifier("pulverized_iron").item(PULVERIZED_IRON)
        identifier("pulverized_gold").item(PULVERIZED_GOLD)
        identifier("pulverized_coal").item(PULVERIZED_COAL)
        identifier("pulverized_diamond").item(PULVERIZED_DIAMOND)

        identifier("buffer_upgrade").item(BUFFER_UPGRADE)
        identifier("speed_upgrade").item(SPEED_UPGRADE)
        identifier("energy_upgrade").item(ENERGY_UPGRADE)
    }

    companion object {
        private val ORE_BLOCK_SETTINGS: FabricBlockSettings = FabricBlockSettings.of(Material.STONE).sounds(BlockSoundGroup.STONE).breakByTool(FabricToolTags.PICKAXES)

        val COPPER_ORE = Block(ORE_BLOCK_SETTINGS)
        val COPPER_ORE_ITEM = BlockItem(COPPER_ORE, Item.Settings().group(IndustrialRevolution.MOD_GROUP))
        val PULVERIZED_COPPER = Item(Item.Settings().group(IndustrialRevolution.MOD_GROUP))
        val COPPER_INGOT = Item(Item.Settings().group(IndustrialRevolution.MOD_GROUP))
        val TIN_ORE = Block(ORE_BLOCK_SETTINGS)
        val TIN_ORE_ITEM = BlockItem(TIN_ORE, Item.Settings().group(IndustrialRevolution.MOD_GROUP))
        val PULVERIZED_TIN = Item(Item.Settings().group(IndustrialRevolution.MOD_GROUP))
        val TIN_INGOT = Item(Item.Settings().group(IndustrialRevolution.MOD_GROUP))
        val NIKOLITE_ORE = Block(ORE_BLOCK_SETTINGS)
        val NIKOLITE_ORE_ITEM = BlockItem(NIKOLITE_ORE, Item.Settings().group(IndustrialRevolution.MOD_GROUP))
        val NIKOLITE = Item(Item.Settings().group(IndustrialRevolution.MOD_GROUP))

        val PULVERIZED_IRON = Item(Item.Settings().group(IndustrialRevolution.MOD_GROUP))
        val PULVERIZED_GOLD = Item(Item.Settings().group(IndustrialRevolution.MOD_GROUP))
        val PULVERIZED_COAL = Item(Item.Settings().group(IndustrialRevolution.MOD_GROUP))
        val PULVERIZED_DIAMOND = Item(Item.Settings().group(IndustrialRevolution.MOD_GROUP))

        val BUFFER_UPGRADE = UpgradeItem(Item.Settings().group(IndustrialRevolution.MOD_GROUP).maxCount(1), Upgrade.BUFFER)
        val SPEED_UPGRADE = UpgradeItem(Item.Settings().group(IndustrialRevolution.MOD_GROUP).maxCount(1), Upgrade.SPEED)
        val ENERGY_UPGRADE = UpgradeItem(Item.Settings().group(IndustrialRevolution.MOD_GROUP).maxCount(1), Upgrade.ENERGY)
    }
}