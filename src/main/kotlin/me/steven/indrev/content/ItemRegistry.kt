package me.steven.indrev.content

import me.steven.indrev.block
import me.steven.indrev.identifier
import me.steven.indrev.item
import me.steven.indrev.itemSettings
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
        val COPPER_ORE_ITEM = BlockItem(COPPER_ORE, itemSettings())
        val PULVERIZED_COPPER = Item(itemSettings())
        val COPPER_INGOT = Item(itemSettings())
        val TIN_ORE = Block(ORE_BLOCK_SETTINGS)
        val TIN_ORE_ITEM = BlockItem(TIN_ORE, itemSettings())
        val PULVERIZED_TIN = Item(itemSettings())
        val TIN_INGOT = Item(itemSettings())
        val NIKOLITE_ORE = Block(ORE_BLOCK_SETTINGS)
        val NIKOLITE_ORE_ITEM = BlockItem(NIKOLITE_ORE, itemSettings())
        val NIKOLITE = Item(itemSettings())

        val PULVERIZED_IRON = Item(itemSettings())
        val PULVERIZED_GOLD = Item(itemSettings())
        val PULVERIZED_COAL = Item(itemSettings())
        val PULVERIZED_DIAMOND = Item(itemSettings())

        val BUFFER_UPGRADE = UpgradeItem(itemSettings().maxCount(1), Upgrade.BUFFER)
        val SPEED_UPGRADE = UpgradeItem(itemSettings().maxCount(1), Upgrade.SPEED)
        val ENERGY_UPGRADE = UpgradeItem(itemSettings().maxCount(1), Upgrade.ENERGY)
    }
}