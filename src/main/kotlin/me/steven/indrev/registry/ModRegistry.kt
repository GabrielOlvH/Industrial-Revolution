package me.steven.indrev.registry

import io.github.cottonmc.resources.type.GenericResourceType
import me.steven.indrev.items.ChunkScannerItem
import me.steven.indrev.items.CoolerItem
import me.steven.indrev.items.CraftingTool
import me.steven.indrev.items.UraniumRodItem
import me.steven.indrev.items.rechargeable.RechargeableItem
import me.steven.indrev.items.rechargeable.RechargeableMiningItem
import me.steven.indrev.items.upgrade.Upgrade
import me.steven.indrev.items.upgrade.UpgradeItem
import me.steven.indrev.utils.*
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.fabricmc.fabric.api.tool.attribute.v1.FabricToolTags
import net.minecraft.block.Material
import net.minecraft.item.Item
import net.minecraft.sound.BlockSoundGroup
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry

class ModRegistry {

    fun registerAll() {
        identifier("hammer").item(HAMMER)
        identifier("cutter").item(CUTTER)

        NIKOLITE.registerAll()
        identifier("enriched_nikolite").item(DEFAULT_ITEM())
        identifier("enriched_nikolite_ingot").item(DEFAULT_ITEM())

        identifier("mining_drill").item(MINING_DRILL)
        identifier("wire").item(DEFAULT_ITEM())
        identifier("battery").tierBasedItem { tier ->
            val dmg = when (tier) {
                Tier.MK1 -> 256
                Tier.MK2 -> 1024
                Tier.MK3 -> 4096
                Tier.MK4 -> 8192
            }
            RechargeableItem(itemSettings().maxDamage(dmg), true)
        }
        identifier("circuit").tierBasedItem { DEFAULT_ITEM() }

        identifier("fan").item(FAN)
        identifier("cooler_cell").item(COOLER_CELL)

        identifier("uranium_rod").item(URANIUM_ROD_ITEM)

        identifier("chunk_scanner").item(ChunkScannerItem(itemSettings()))

        identifier("buffer_upgrade").item(BUFFER_UPGRADE)
        identifier("speed_upgrade").item(SPEED_UPGRADE)
        identifier("energy_upgrade").item(ENERGY_UPGRADE)
    }

    companion object {
        private val ORE_BLOCK_SETTINGS = {
            FabricBlockSettings.of(Material.STONE).sounds(BlockSoundGroup.STONE).breakByTool(FabricToolTags.PICKAXES)
                .strength(3.0F, 3.0F)
        }

        private val DEFAULT_ITEM: () -> Item = { Item(itemSettings()) }

        val HAMMER = CraftingTool(itemSettings().maxDamage(32))
        val CUTTER = CraftingTool(itemSettings().maxDamage(32))

        val NIKOLITE = GenericResourceType.Builder("nikolite")
            .allOres()
            .withDustAffix()
            .noBlock()
            .build()
            .withItemAffixes("ingot")

        val COPPER_ORE = Registry.BLOCK.get(Identifier("c:copper_ore"))
        val TIN_ORE = Registry.BLOCK.get(Identifier("c:copper_ore"))

        val FAN = CoolerItem(itemSettings().maxDamage(512), -0.07, -0.01)
        val COOLER_CELL = CoolerItem(itemSettings().maxDamage(256), -0.1, -0.05)

        val URANIUM_ROD_ITEM = UraniumRodItem(itemSettings().maxDamage(1024))

        val MINING_DRILL = RechargeableMiningItem(itemSettings().maxDamage(32000))

        val BUFFER_UPGRADE = UpgradeItem(itemSettings().maxCount(1), Upgrade.BUFFER)
        val SPEED_UPGRADE = UpgradeItem(itemSettings().maxCount(1), Upgrade.SPEED)
        val ENERGY_UPGRADE = UpgradeItem(itemSettings().maxCount(1), Upgrade.ENERGY)
    }
}