package me.steven.indrev.content

import me.steven.indrev.block
import me.steven.indrev.identifier
import me.steven.indrev.item
import me.steven.indrev.itemSettings
import me.steven.indrev.items.ItemTool
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
        identifier("hammer").item(HAMMER)
        identifier("cutter").item(CUTTER)

        identifier("copper_ore").block(COPPER_ORE).item(COPPER_ORE_ITEM)
        identifier("pulverized_copper").item(BASIC_ITEM())
        identifier("copper_ingot").item(BASIC_ITEM())
        identifier("tin_ore").block(TIN_ORE).item(TIN_ORE_ITEM)
        identifier("pulverized_tin").item(BASIC_ITEM())
        identifier("tin_ingot").item(BASIC_ITEM())
        identifier("nikolite_ore").block(NIKOLITE_ORE).item(NIKOLITE_ORE_ITEM)
        identifier("nikolite").item(BASIC_ITEM())

        identifier("pulverized_iron").item(BASIC_ITEM())
        identifier("pulverized_gold").item(BASIC_ITEM())
        identifier("pulverized_coal").item(BASIC_ITEM())
        identifier("pulverized_diamond").item(BASIC_ITEM())

        identifier("iron_plate").item(BASIC_ITEM())
        identifier("basic_capacitor").item(BASIC_ITEM())

        identifier("buffer_upgrade").item(BUFFER_UPGRADE)
        identifier("speed_upgrade").item(SPEED_UPGRADE)
        identifier("energy_upgrade").item(ENERGY_UPGRADE)
    }

    companion object {
        private val ORE_BLOCK_SETTINGS: FabricBlockSettings = FabricBlockSettings.of(Material.STONE).sounds(BlockSoundGroup.STONE).breakByTool(FabricToolTags.PICKAXES)

        private val BASIC_ITEM: () -> Item = { Item(itemSettings()) }

        val HAMMER = ItemTool(itemSettings().maxDamage(32))
        val CUTTER = ItemTool(itemSettings().maxDamage(32))

        val COPPER_ORE = Block(ORE_BLOCK_SETTINGS)
        val COPPER_ORE_ITEM = BlockItem(COPPER_ORE, itemSettings())
        val TIN_ORE = Block(ORE_BLOCK_SETTINGS)
        val TIN_ORE_ITEM = BlockItem(TIN_ORE, itemSettings())
        val NIKOLITE_ORE = Block(ORE_BLOCK_SETTINGS)
        val NIKOLITE_ORE_ITEM = BlockItem(NIKOLITE_ORE, itemSettings())

        val BUFFER_UPGRADE = UpgradeItem(itemSettings().maxCount(1), Upgrade.BUFFER)
        val SPEED_UPGRADE = UpgradeItem(itemSettings().maxCount(1), Upgrade.SPEED)
        val ENERGY_UPGRADE = UpgradeItem(itemSettings().maxCount(1), Upgrade.ENERGY)
    }
}