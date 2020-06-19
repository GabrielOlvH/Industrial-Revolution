package me.steven.indrev.registry

import io.github.cottonmc.resources.type.GenericResourceType
import me.steven.indrev.items.IRChunkScannerItem
import me.steven.indrev.items.IRCoolerItem
import me.steven.indrev.items.IRCraftingToolItem
import me.steven.indrev.items.IREnergyReader
import me.steven.indrev.items.rechargeable.IRRechargeableItem
import me.steven.indrev.items.rechargeable.IRRechargeableMiningItem
import me.steven.indrev.items.upgrade.IRUpgradeItem
import me.steven.indrev.items.upgrade.Upgrade
import me.steven.indrev.utils.*
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.fabricmc.fabric.api.tool.attribute.v1.FabricToolTags
import net.minecraft.block.Block
import net.minecraft.block.Material
import net.minecraft.item.Item
import net.minecraft.sound.BlockSoundGroup
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry

object ModRegistry {

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
                else -> throw IllegalArgumentException("no creative battery!")
            }
            IRRechargeableItem(itemSettings().maxDamage(dmg), true)
        }
        identifier("circuit").tierBasedItem { DEFAULT_ITEM() }

        identifier("fan").item(FAN)
        identifier("cooler_cell").item(COOLER_CELL)

        identifier("uranium_rod").item(URANIUM_ROD_ITEM)

        identifier("chunk_scanner").item(IRChunkScannerItem(itemSettings()))

        identifier("buffer_upgrade").item(BUFFER_UPGRADE)
        identifier("speed_upgrade").item(SPEED_UPGRADE)
        identifier("energy_upgrade").item(ENERGY_UPGRADE)

        identifier("energy_reader").item(ENERGY_READER)

        identifier("area_indicator").block(AREA_INDICATOR)

        identifier("biomass").item(BIOMASS)
    }

    private val ORE_BLOCK_SETTINGS = {
        FabricBlockSettings.of(Material.STONE).sounds(BlockSoundGroup.STONE).breakByTool(FabricToolTags.PICKAXES)
            .strength(3.0F, 3.0F)
    }

    private val DEFAULT_ITEM: () -> Item = { Item(itemSettings()) }

    val HAMMER = IRCraftingToolItem(itemSettings().maxDamage(32))
    val CUTTER = IRCraftingToolItem(itemSettings().maxDamage(32))

    val NIKOLITE = GenericResourceType.Builder("nikolite")
        .allOres()
        .withDustAffix()
        .noBlock()
        .build()
        .withItemAffixes("ingot")

    val COPPER_ORE = Registry.BLOCK.get(Identifier("c:copper_ore"))
    val TIN_ORE = Registry.BLOCK.get(Identifier("c:copper_ore"))

    val BIOMASS = DEFAULT_ITEM()

    val FAN = IRCoolerItem(itemSettings().maxDamage(512), -0.07)
    val COOLER_CELL = IRCoolerItem(itemSettings().maxDamage(256), -0.1)

    val URANIUM_ROD_ITEM = Item(itemSettings().maxDamage(1024))

    val MINING_DRILL = IRRechargeableMiningItem(itemSettings().maxDamage(32000))

    val ENERGY_READER = IREnergyReader(itemSettings())

    val AREA_INDICATOR = Block(FabricBlockSettings.of(Material.WOOL))

    val BUFFER_UPGRADE = IRUpgradeItem(itemSettings().maxCount(1), Upgrade.BUFFER)
    val SPEED_UPGRADE = IRUpgradeItem(itemSettings().maxCount(1), Upgrade.SPEED)
    val ENERGY_UPGRADE = IRUpgradeItem(itemSettings().maxCount(1), Upgrade.ENERGY)
}