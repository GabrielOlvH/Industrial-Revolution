package me.steven.indrev.datagen.utils

import me.steven.indrev.utils.identifier
import net.minecraft.client.util.ModelIdentifier
import java.util.*

object MetalSpriteRegistry {
        val MATERIAL_PROVIDERS: HashMap<ModelIdentifier, MetalModel> = hashMapOf()

        init {
            put("lead_ingot", MetalModel.Builder().ingotBase(LEAD_BASE).ingotHighlight(LEAD_HIGHLIGHT).build())
            put("lead_plate", MetalModel.Builder().plateBase(LEAD_BASE).plateHighlight(LEAD_HIGHLIGHT).build())
            put("lead_pickaxe", MetalModel.Builder().toolStick().pickaxeBase(LEAD_BASE).pickaxeHighlight(LEAD_HIGHLIGHT).build())
            put("lead_axe", MetalModel.Builder().toolStick().axeBase(LEAD_BASE).axeHighlight(LEAD_HIGHLIGHT).build())
            put("lead_sword", MetalModel.Builder().toolStick().swordBase(LEAD_BASE).swordHighlight(LEAD_HIGHLIGHT).build())
            put("lead_hoe", MetalModel.Builder().toolStick().hoeBase(LEAD_BASE).hoeHighlight(LEAD_HIGHLIGHT).build())
            put("lead_shovel", MetalModel.Builder().toolStick().shovelBase(LEAD_BASE).shovelHighlight(LEAD_HIGHLIGHT).build())
            put("lead_helmet", MetalModel.Builder().helmetBase(LEAD_BASE).helmetHighlight(LEAD_HIGHLIGHT).build())
            put("lead_dust", MetalModel.Builder().dustBase(LEAD_BASE).dustHighlight(LEAD_HIGHLIGHT).build())
            put("lead_chestplate", MetalModel.Builder().chestplateBase(LEAD_BASE).chestplateHighlight(LEAD_HIGHLIGHT).build())
            put("lead_boots", MetalModel.Builder().bootsBase(LEAD_BASE).bootsHighlight(LEAD_HIGHLIGHT).build())
            put("lead_leggings", MetalModel.Builder().leggingsBase(LEAD_BASE).leggingsHighlight(LEAD_HIGHLIGHT).build())
            put("lead_nugget", MetalModel.Builder().nuggetBase(LEAD_BASE).nuggetHighlight(LEAD_HIGHLIGHT).build())
            putBlock("lead_ore", MetalModel.Builder().ore().oreBase(LEAD_BASE).oreHighlight(LEAD_HIGHLIGHT).build())
            putBlock("lead_block", MetalModel.Builder().block().blockBase(LEAD_BASE).build())
            put("lead_purified_ore", MetalModel.Builder().purifiedOreBase(LEAD_BASE).purifiedOreHighlight(LEAD_HIGHLIGHT).build())
            put("lead_chunk", MetalModel.Builder().chunkBase(LEAD_BASE).chunkHighlight(LEAD_HIGHLIGHT).build())
            put("molten_lead_bucket", MetalModel.Builder().bucket().moltenBucketBase(LEAD_BASE).moltenBucketHighlight(LEAD_HIGHLIGHT).build())


            put("silver_ingot", MetalModel.Builder().ingotBase(SILVER_BASE).ingotHighlight(SILVER_HIGHLIGHT).build())
            put("silver_plate", MetalModel.Builder().plateBase(SILVER_BASE).plateHighlight(SILVER_HIGHLIGHT).build())
            put("silver_pickaxe", MetalModel.Builder().toolStick().pickaxeBase(SILVER_BASE).pickaxeHighlight(
                SILVER_HIGHLIGHT
            ).build())
            put("silver_axe", MetalModel.Builder().toolStick().axeBase(SILVER_BASE).axeHighlight(
                SILVER_HIGHLIGHT
            ).build())
            put("silver_sword", MetalModel.Builder().toolStick().swordBase(SILVER_BASE).swordHighlight(
                SILVER_HIGHLIGHT
            ).build())
            put("silver_hoe", MetalModel.Builder().toolStick().hoeBase(SILVER_BASE).hoeHighlight(
                SILVER_HIGHLIGHT
            ).build())
            put("silver_shovel", MetalModel.Builder().toolStick().shovelBase(SILVER_BASE).shovelHighlight(
                SILVER_HIGHLIGHT
            ).build())
            put("silver_helmet", MetalModel.Builder().helmetBase(SILVER_BASE).helmetHighlight(SILVER_HIGHLIGHT).build())
            put("silver_dust", MetalModel.Builder().dustBase(SILVER_BASE).dustHighlight(SILVER_HIGHLIGHT).build())
            put("silver_chestplate", MetalModel.Builder().chestplateBase(SILVER_BASE).chestplateHighlight(
                SILVER_HIGHLIGHT
            ).build())
            put("silver_boots", MetalModel.Builder().bootsBase(SILVER_BASE).bootsHighlight(SILVER_HIGHLIGHT).build())
            put("silver_leggings", MetalModel.Builder().leggingsBase(SILVER_BASE).leggingsHighlight(
                SILVER_HIGHLIGHT
            ).build())
            put("silver_nugget", MetalModel.Builder().nuggetBase(SILVER_BASE).nuggetHighlight(SILVER_HIGHLIGHT).build())
            putBlock("silver_ore", MetalModel.Builder().ore().oreBase(SILVER_BASE).oreHighlight(SILVER_HIGHLIGHT).build())
            putBlock("silver_block", MetalModel.Builder().block().blockBase(SILVER_BASE).build())
            put("silver_purified_ore", MetalModel.Builder().purifiedOreBase(SILVER_BASE).purifiedOreHighlight(
                SILVER_HIGHLIGHT
            ).build())
            put("silver_chunk", MetalModel.Builder().chunkBase(SILVER_BASE).chunkHighlight(SILVER_HIGHLIGHT).build())
            put("molten_silver_bucket", MetalModel.Builder().bucket().moltenBucketBase(SILVER_BASE).moltenBucketHighlight(SILVER_HIGHLIGHT).build())

            put("copper_ingot", MetalModel.Builder().ingotBase(COPPER_BASE).ingotHighlight(COPPER_HIGHLIGHT).build())
            put("copper_plate", MetalModel.Builder().plateBase(COPPER_BASE).plateHighlight(COPPER_HIGHLIGHT).build())
            put("copper_pickaxe", MetalModel.Builder().toolStick().pickaxeBase(COPPER_BASE).pickaxeHighlight(
                COPPER_HIGHLIGHT
            ).build())
            put("copper_axe", MetalModel.Builder().toolStick().axeBase(COPPER_BASE).axeHighlight(
                COPPER_HIGHLIGHT
            ).build())
            put("copper_sword", MetalModel.Builder().toolStick().swordBase(COPPER_BASE).swordHighlight(
                COPPER_HIGHLIGHT
            ).build())
            put("copper_hoe", MetalModel.Builder().toolStick().hoeBase(COPPER_BASE).hoeHighlight(
                COPPER_HIGHLIGHT
            ).build())
            put("copper_shovel", MetalModel.Builder().toolStick().shovelBase(COPPER_BASE).shovelHighlight(
                COPPER_HIGHLIGHT
            ).build())
            put("copper_helmet", MetalModel.Builder().helmetBase(COPPER_BASE).helmetHighlight(COPPER_HIGHLIGHT).build())
            put("copper_dust", MetalModel.Builder().dustBase(COPPER_BASE).dustHighlight(COPPER_HIGHLIGHT).build())
            put("copper_chestplate", MetalModel.Builder().chestplateBase(COPPER_BASE).chestplateHighlight(
                COPPER_HIGHLIGHT
            ).build())
            put("copper_boots", MetalModel.Builder().bootsBase(COPPER_BASE).bootsHighlight(COPPER_HIGHLIGHT).build())
            put("copper_leggings", MetalModel.Builder().leggingsBase(COPPER_BASE).leggingsHighlight(
                COPPER_HIGHLIGHT
            ).build())
            put("copper_nugget", MetalModel.Builder().nuggetBase(COPPER_BASE).nuggetHighlight(COPPER_HIGHLIGHT).build())
            putBlock("copper_ore", MetalModel.Builder().ore().oreBase(COPPER_BASE).oreHighlight(COPPER_HIGHLIGHT).build())
            putBlock("copper_block", MetalModel.Builder().block().blockBase(COPPER_BASE).build())
            put("copper_purified_ore", MetalModel.Builder().purifiedOreBase(COPPER_BASE).purifiedOreHighlight(
                COPPER_HIGHLIGHT
            ).build())
            put("copper_chunk", MetalModel.Builder().chunkBase(COPPER_BASE).chunkHighlight(COPPER_HIGHLIGHT).build())
            put("molten_copper_bucket", MetalModel.Builder().bucket().moltenBucketBase(COPPER_BASE).moltenBucketHighlight(COPPER_HIGHLIGHT).build())

            put("tungsten_ingot", MetalModel.Builder().ingotBase(TUNGSTEN_BASE).ingotHighlight(
                TUNGSTEN_HIGHLIGHT
            ).build())
            put("tungsten_plate", MetalModel.Builder().plateBase(TUNGSTEN_BASE).plateHighlight(
                TUNGSTEN_HIGHLIGHT
            ).build())
            put("tungsten_pickaxe", MetalModel.Builder().toolStick().pickaxeBase(TUNGSTEN_BASE).pickaxeHighlight(
                TUNGSTEN_HIGHLIGHT
            ).build())
            put("tungsten_axe", MetalModel.Builder().toolStick().axeBase(TUNGSTEN_BASE).axeHighlight(
                TUNGSTEN_HIGHLIGHT
            ).build())
            put("tungsten_sword", MetalModel.Builder().toolStick().swordBase(TUNGSTEN_BASE).swordHighlight(
                TUNGSTEN_HIGHLIGHT
            ).build())
            put("tungsten_hoe", MetalModel.Builder().toolStick().hoeBase(TUNGSTEN_BASE).hoeHighlight(
                TUNGSTEN_HIGHLIGHT
            ).build())
            put("tungsten_shovel", MetalModel.Builder().toolStick().shovelBase(TUNGSTEN_BASE).shovelHighlight(
                TUNGSTEN_HIGHLIGHT
            ).build())
            put("tungsten_helmet", MetalModel.Builder().helmetBase(TUNGSTEN_BASE).helmetHighlight(
                TUNGSTEN_HIGHLIGHT
            ).build())
            put("tungsten_dust", MetalModel.Builder().dustBase(TUNGSTEN_BASE).dustHighlight(TUNGSTEN_HIGHLIGHT).build())
            put("tungsten_chestplate", MetalModel.Builder().chestplateBase(TUNGSTEN_BASE).chestplateHighlight(
                TUNGSTEN_HIGHLIGHT
            ).build())
            put("tungsten_boots", MetalModel.Builder().bootsBase(TUNGSTEN_BASE).bootsHighlight(
                TUNGSTEN_HIGHLIGHT
            ).build())
            put("tungsten_leggings", MetalModel.Builder().leggingsBase(TUNGSTEN_BASE).leggingsHighlight(
                TUNGSTEN_HIGHLIGHT
            ).build())
            put("tungsten_nugget", MetalModel.Builder().nuggetBase(TUNGSTEN_BASE).nuggetHighlight(
                TUNGSTEN_HIGHLIGHT
            ).build())
            putBlock("tungsten_ore", MetalModel.Builder().ore().oreBase(TUNGSTEN_BASE).oreHighlight(
                TUNGSTEN_HIGHLIGHT
            ).build())
            putBlock("tungsten_block", MetalModel.Builder().block().blockBase(TUNGSTEN_BASE).build())
            put("tungsten_purified_ore", MetalModel.Builder().purifiedOreBase(TUNGSTEN_BASE).purifiedOreHighlight(
                TUNGSTEN_HIGHLIGHT
            ).build())
            put("tungsten_chunk", MetalModel.Builder().chunkBase(TUNGSTEN_BASE).chunkHighlight(
                TUNGSTEN_HIGHLIGHT
            ).build())

            put("bronze_ingot", MetalModel.Builder().ingotBase(BRONZE_BASE).ingotHighlight(BRONZE_HIGHLIGHT).build())
            put("bronze_plate", MetalModel.Builder().plateBase(BRONZE_BASE).plateHighlight(BRONZE_HIGHLIGHT).build())
            put("bronze_pickaxe", MetalModel.Builder().toolStick().pickaxeBase(BRONZE_BASE).pickaxeHighlight(
                BRONZE_HIGHLIGHT
            ).build())
            put("bronze_axe", MetalModel.Builder().toolStick().axeBase(BRONZE_BASE).axeHighlight(
                BRONZE_HIGHLIGHT
            ).build())
            put("bronze_sword", MetalModel.Builder().toolStick().swordBase(BRONZE_BASE).swordHighlight(
                BRONZE_HIGHLIGHT
            ).build())
            put("bronze_hoe", MetalModel.Builder().toolStick().hoeBase(BRONZE_BASE).hoeHighlight(
                BRONZE_HIGHLIGHT
            ).build())
            put("bronze_shovel", MetalModel.Builder().toolStick().shovelBase(BRONZE_BASE).shovelHighlight(
                BRONZE_HIGHLIGHT
            ).build())
            put("bronze_helmet", MetalModel.Builder().helmetBase(BRONZE_BASE).helmetHighlight(BRONZE_HIGHLIGHT).build())
            put("bronze_dust", MetalModel.Builder().dustBase(BRONZE_BASE).dustHighlight(BRONZE_HIGHLIGHT).build())
            put("bronze_chestplate", MetalModel.Builder().chestplateBase(BRONZE_BASE).chestplateHighlight(
                BRONZE_HIGHLIGHT
            ).build())
            put("bronze_boots", MetalModel.Builder().bootsBase(BRONZE_BASE).bootsHighlight(BRONZE_HIGHLIGHT).build())
            put("bronze_leggings", MetalModel.Builder().leggingsBase(BRONZE_BASE).leggingsHighlight(
                BRONZE_HIGHLIGHT
            ).build())
            put("bronze_nugget", MetalModel.Builder().nuggetBase(BRONZE_BASE).nuggetHighlight(BRONZE_HIGHLIGHT).build())
            putBlock("bronze_ore", MetalModel.Builder().ore().oreBase(BRONZE_BASE).oreHighlight(BRONZE_HIGHLIGHT).build())
            putBlock("bronze_block", MetalModel.Builder().block().blockBase(BRONZE_BASE).build())
            put("bronze_purified_ore", MetalModel.Builder().purifiedOreBase(BRONZE_BASE).purifiedOreHighlight(
                BRONZE_HIGHLIGHT
            ).build())
            put("bronze_chunk", MetalModel.Builder().chunkBase(BRONZE_BASE).chunkHighlight(BRONZE_HIGHLIGHT).build())

            put("electrum_ingot", MetalModel.Builder().ingotBase(ELECTRUM_BASE).ingotHighlight(
                ELECTRUM_HIGHLIGHT
            ).build())
            put("electrum_plate", MetalModel.Builder().plateBase(ELECTRUM_BASE).plateHighlight(
                ELECTRUM_HIGHLIGHT
            ).build())
            put("electrum_pickaxe", MetalModel.Builder().toolStick().pickaxeBase(ELECTRUM_BASE).pickaxeHighlight(
                ELECTRUM_HIGHLIGHT
            ).build())
            put("electrum_axe", MetalModel.Builder().toolStick().axeBase(ELECTRUM_BASE).axeHighlight(
                ELECTRUM_HIGHLIGHT
            ).build())
            put("electrum_sword", MetalModel.Builder().toolStick().swordBase(ELECTRUM_BASE).swordHighlight(
                ELECTRUM_HIGHLIGHT
            ).build())
            put("electrum_hoe", MetalModel.Builder().toolStick().hoeBase(ELECTRUM_BASE).hoeHighlight(
                ELECTRUM_HIGHLIGHT
            ).build())
            put("electrum_shovel", MetalModel.Builder().toolStick().shovelBase(ELECTRUM_BASE).shovelHighlight(
                ELECTRUM_HIGHLIGHT
            ).build())
            put("electrum_helmet", MetalModel.Builder().helmetBase(ELECTRUM_BASE).helmetHighlight(ELECTRUM_HIGHLIGHT).build())
            put("electrum_dust", MetalModel.Builder().dustBase(ELECTRUM_BASE).dustHighlight(ELECTRUM_HIGHLIGHT).build())
            put("electrum_chestplate", MetalModel.Builder().chestplateBase(ELECTRUM_BASE).chestplateHighlight(
                ELECTRUM_HIGHLIGHT
            ).build())
            put("electrum_boots", MetalModel.Builder().bootsBase(ELECTRUM_BASE).bootsHighlight(ELECTRUM_HIGHLIGHT).build())
            put("electrum_leggings", MetalModel.Builder().leggingsBase(ELECTRUM_BASE).leggingsHighlight(
                ELECTRUM_HIGHLIGHT
            ).build())
            put("electrum_nugget", MetalModel.Builder().nuggetBase(ELECTRUM_BASE).nuggetHighlight(ELECTRUM_HIGHLIGHT).build())
            putBlock("electrum_ore", MetalModel.Builder().ore().oreBase(ELECTRUM_BASE).oreHighlight(ELECTRUM_HIGHLIGHT).build())
            putBlock("electrum_block", MetalModel.Builder().block().blockBase(ELECTRUM_BASE).build())
            put("electrum_purified_ore", MetalModel.Builder().purifiedOreBase(ELECTRUM_BASE).purifiedOreHighlight(
                ELECTRUM_HIGHLIGHT
            ).build())
            put("electrum_chunk", MetalModel.Builder().chunkBase(ELECTRUM_BASE).chunkHighlight(ELECTRUM_HIGHLIGHT).build())

            put("steel_ingot", MetalModel.Builder().ingotBase(STEEL_BASE).ingotHighlight(STEEL_HIGHLIGHT).build())
            put("steel_plate", MetalModel.Builder().plateBase(STEEL_BASE).plateHighlight(STEEL_HIGHLIGHT).build())
            put("steel_pickaxe", MetalModel.Builder().toolStick().pickaxeBase(STEEL_BASE).pickaxeHighlight(
                STEEL_HIGHLIGHT
            ).build())
            put("steel_axe", MetalModel.Builder().toolStick().axeBase(STEEL_BASE).axeHighlight(STEEL_HIGHLIGHT).build())
            put("steel_sword", MetalModel.Builder().toolStick().swordBase(STEEL_BASE).swordHighlight(STEEL_HIGHLIGHT).build())
            put("steel_hoe", MetalModel.Builder().toolStick().hoeBase(STEEL_BASE).hoeHighlight(STEEL_HIGHLIGHT).build())
            put("steel_shovel", MetalModel.Builder().toolStick().shovelBase(STEEL_BASE).shovelHighlight(STEEL_HIGHLIGHT).build())
            put("steel_helmet", MetalModel.Builder().helmetBase(STEEL_BASE).helmetHighlight(STEEL_HIGHLIGHT).build())
            put("steel_dust", MetalModel.Builder().dustBase(STEEL_BASE).dustHighlight(STEEL_HIGHLIGHT).build())
            put("steel_chestplate", MetalModel.Builder().chestplateBase(STEEL_BASE).chestplateHighlight(STEEL_HIGHLIGHT).build())
            put("steel_boots", MetalModel.Builder().bootsBase(STEEL_BASE).bootsHighlight(STEEL_HIGHLIGHT).build())
            put("steel_leggings", MetalModel.Builder().leggingsBase(STEEL_BASE).leggingsHighlight(STEEL_HIGHLIGHT).build())
            put("steel_nugget", MetalModel.Builder().nuggetBase(STEEL_BASE).nuggetHighlight(STEEL_HIGHLIGHT).build())
            putBlock("steel_ore", MetalModel.Builder().ore().oreBase(STEEL_BASE).oreHighlight(STEEL_HIGHLIGHT).build())
            putBlock("steel_block", MetalModel.Builder().block().blockBase(STEEL_BASE).build())
            put("steel_purified_ore", MetalModel.Builder().purifiedOreBase(STEEL_BASE).purifiedOreHighlight(
                STEEL_HIGHLIGHT
            ).build())
            put("steel_chunk", MetalModel.Builder().chunkBase(STEEL_BASE).chunkHighlight(STEEL_HIGHLIGHT).build())

            put("tin_ingot", MetalModel.Builder().ingotBase(TIN_BASE).ingotHighlight(TIN_HIGHLIGHT).ingotOutline(
                TIN_OUTLINE
            ).build())
            put("tin_plate", MetalModel.Builder().plateBase(TIN_BASE).plateHighlight(TIN_HIGHLIGHT).plateOutline(
                TIN_OUTLINE
            ).build())
            put("tin_pickaxe", MetalModel.Builder().toolStick().pickaxeBase(TIN_BASE).pickaxeHighlight(TIN_HIGHLIGHT).pickaxeOutline(
                TIN_OUTLINE
            ).build())
            put("tin_axe", MetalModel.Builder().toolStick().axeBase(TIN_BASE).axeHighlight(TIN_HIGHLIGHT).axeOutline(
                TIN_OUTLINE
            ).build())
            put("tin_sword", MetalModel.Builder().toolStick().swordBase(TIN_BASE).swordHighlight(TIN_HIGHLIGHT).swordOutline(
                TIN_OUTLINE
            ).build())
            put("tin_hoe", MetalModel.Builder().toolStick().hoeBase(TIN_BASE).hoeHighlight(TIN_HIGHLIGHT).hoeOutline(
                TIN_OUTLINE
            ).build())
            put("tin_shovel", MetalModel.Builder().toolStick().shovelBase(TIN_BASE).shovelHighlight(TIN_HIGHLIGHT).shovelOutline(
                TIN_OUTLINE
            ).build())
            put("tin_helmet", MetalModel.Builder().helmetBase(TIN_BASE).helmetHighlight(TIN_HIGHLIGHT).helmetOutline(
                TIN_OUTLINE
            ).build())
            put("tin_dust", MetalModel.Builder().dustBase(TIN_BASE).dustHighlight(TIN_HIGHLIGHT).dustOutline(TIN_OUTLINE).build())
            put("tin_chestplate", MetalModel.Builder().chestplateBase(TIN_BASE).chestplateHighlight(TIN_HIGHLIGHT).chestplateOutline(
                TIN_OUTLINE
            ).build())
            put("tin_boots", MetalModel.Builder().bootsBase(TIN_BASE).bootsHighlight(TIN_HIGHLIGHT).bootsOutline(
                TIN_OUTLINE
            ).build())
            put("tin_leggings", MetalModel.Builder().leggingsBase(TIN_BASE).leggingsHighlight(TIN_HIGHLIGHT).leggingsOutline(
                TIN_OUTLINE
            ).build())
            put("tin_nugget", MetalModel.Builder().nuggetBase(TIN_BASE).nuggetHighlight(TIN_HIGHLIGHT).nuggetOutline(
                TIN_OUTLINE
            ).build())
            putBlock("tin_ore", MetalModel.Builder().ore().oreBase(TIN_BASE).oreHighlight(TIN_HIGHLIGHT).build())
            putBlock("tin_block", MetalModel.Builder().block().blockBase(TIN_BASE).build())
            put("tin_purified_ore", MetalModel.Builder().purifiedOreBase(TIN_BASE).purifiedOreHighlight(TIN_HIGHLIGHT).purifiedOreOutline(
                TIN_OUTLINE
            ).build())
            put("tin_chunk", MetalModel.Builder().chunkBase(TIN_BASE).chunkHighlight(TIN_HIGHLIGHT).chunkOutline(
                TIN_OUTLINE
            ).build())
            put("molten_tin_bucket", MetalModel.Builder().bucket().moltenBucketBase(TIN_BASE).moltenBucketHighlight(TIN_HIGHLIGHT).moltenBucketOutline(TIN_OUTLINE).build())

            put("nikolite_ingot", MetalModel.Builder().ingotBase(NIKOLITE_BASE).ingotHighlight(NIKOLITE_HIGHLIGHT).build())
            put("nikolite_dust", MetalModel.Builder().dustBase(NIKOLITE_BASE).dustHighlight(NIKOLITE_HIGHLIGHT).build())
            putBlock("nikolite_ore", MetalModel.Builder().ore().oreBase(NIKOLITE_BASE).oreHighlight(NIKOLITE_HIGHLIGHT).build())

            put("iron_plate", MetalModel.Builder().plateBase(IRON_BASE).plateHighlight(IRON_HIGHLIGHT).build())
            put("iron_dust", MetalModel.Builder().dustBase(IRON_BASE).dustHighlight(IRON_HIGHLIGHT).build())
            put("iron_purified_ore", MetalModel.Builder().purifiedOreBase(IRON_BASE).purifiedOreHighlight(IRON_HIGHLIGHT).build())
            put("iron_chunk", MetalModel.Builder().chunkBase(IRON_BASE).chunkHighlight(IRON_HIGHLIGHT).build())

            put("diamond_dust", MetalModel.Builder().dustBase(DIAMOND_BASE).dustHighlight(DIAMOND_HIGHLIGHT).dustOutline(
                DIAMOND_OUTLINE
            ).build())
            put("sulfur_dust", MetalModel.Builder().dustBase(SULFUR_BASE).dustHighlight(SULFUR_HIGHLIGHT).dustOutline(
                SULFUR_OUTLINE
            ).build())
            put("coal_dust", MetalModel.Builder().dustBase(COAL_BASE).dustHighlight(COAL_HIGHLIGHT).build())

            put("netherite_scrap_dust", MetalModel.Builder().dustBase(NETHERITE_SCRAP_BASE).dustHighlight(
                NETHERITE_SCRAP_HIGHLIGHT
            ).dustOutline(NETHERITE_SCRAP_OUTLINE).build())
            put("netherite_scrap_chunk", MetalModel.Builder().chunkBase(NETHERITE_SCRAP_BASE).chunkHighlight(
                NETHERITE_SCRAP_HIGHLIGHT
            ).chunkOutline(NETHERITE_SCRAP_OUTLINE).build())
            put("netherite_scrap_purified_ore", MetalModel.Builder().purifiedOreBase(NETHERITE_SCRAP_BASE).purifiedOreHighlight(
                NETHERITE_SCRAP_HIGHLIGHT
            ).purifiedOreOutline(NETHERITE_SCRAP_OUTLINE).build())
            put("molten_netherite_bucket", MetalModel.Builder().bucket().moltenBucketBase(NETHERITE_SCRAP_BASE).moltenBucketHighlight(NETHERITE_SCRAP_HIGHLIGHT).build())

        }

        private fun put(id: String, model: MetalModel) {
            MATERIAL_PROVIDERS[ModelIdentifier(identifier(id), "inventory")] = model
        }

        private fun putBlock(id: String, model: MetalModel) {
            MATERIAL_PROVIDERS[ModelIdentifier(identifier(id), "inventory")] = model
            MATERIAL_PROVIDERS[ModelIdentifier(identifier(id), "")] = model
        }
}