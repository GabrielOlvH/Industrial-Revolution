package me.steven.indrev.registry

import me.steven.indrev.items.MaterialBakedModel
import me.steven.indrev.utils.identifier
import me.steven.indrev.utils.itemSettings
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.fabricmc.fabric.api.tool.attribute.v1.FabricToolTags
import net.minecraft.block.Block
import net.minecraft.block.Material
import net.minecraft.client.render.model.UnbakedModel
import net.minecraft.client.util.ModelIdentifier
import net.minecraft.entity.EquipmentSlot
import net.minecraft.item.*
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry
import java.util.*

class MaterialHelper(private val id: String, private val block: MaterialHelper.() -> Unit) {

    fun withItems(vararg variants: String): MaterialHelper {
        variants.forEach { variant ->
            Registry.register(Registry.ITEM, identifier("${id}_$variant"), Item(itemSettings()))
        }
        return this
    }

    fun withItem(): MaterialHelper {
        Registry.register(Registry.ITEM, identifier(id), Item(itemSettings()))
        return this
    }

    fun withOre(supplier: (FabricBlockSettings) -> Block = { Block(it) }): MaterialHelper {
        val ore = supplier(FabricBlockSettings.of(Material.STONE).requiresTool().breakByTool(FabricToolTags.PICKAXES, 1).strength(3f, 3f))
        val identifier = identifier("${id}_ore")
        Registry.register(Registry.BLOCK, identifier, ore)
        Registry.register(Registry.ITEM, identifier, BlockItem(ore, itemSettings()))
        return this
    }

    fun withTools(pickaxe: PickaxeItem, axe: AxeItem, shovel: ShovelItem, sword: SwordItem, hoe: HoeItem) {
        Registry.register(Registry.ITEM, identifier("${id}_pickaxe"), pickaxe)
        Registry.register(Registry.ITEM, identifier("${id}_axe"), axe)
        Registry.register(Registry.ITEM, identifier("${id}_shovel"), shovel)
        Registry.register(Registry.ITEM, identifier("${id}_sword"), sword)
        Registry.register(Registry.ITEM, identifier("${id}_hoe"), hoe)
    }

    fun withArmor(material: ArmorMaterial) {
        Registry.register(Registry.ITEM, identifier("${id}_helmet"), ArmorItem(material, EquipmentSlot.HEAD, itemSettings()))
        Registry.register(Registry.ITEM, identifier("${id}_chestplate"), ArmorItem(material, EquipmentSlot.CHEST, itemSettings()))
        Registry.register(Registry.ITEM, identifier("${id}_leggings"), ArmorItem(material, EquipmentSlot.LEGS, itemSettings()))
        Registry.register(Registry.ITEM, identifier("${id}_boots"), ArmorItem(material, EquipmentSlot.FEET, itemSettings()))
    }

    fun withBlock(): MaterialHelper {
        val block =
            Block(FabricBlockSettings.of(Material.METAL).requiresTool().breakByTool(FabricToolTags.PICKAXES, 2).strength(5f, 6f))
        val id = identifier("${id}_block")
        Registry.register(Registry.BLOCK, id, block)
        Registry.register(Registry.ITEM, id, BlockItem(block, itemSettings()))
        return this
    }

    fun register() = block()

    companion object {
        val MATERIAL_PROVIDERS: HashMap<ModelIdentifier, UnbakedModel> = hashMapOf()

        init {
            put("lead_ingot", MaterialBakedModel.Builder().ingotBase(LEAD_BASE).ingotHighlight(LEAD_HIGHLIGHT).ingotShadow(LEAD_SHADOW).build())
            put("lead_pickaxe", MaterialBakedModel.Builder().toolStick().pickaxeBase(LEAD_BASE).pickaxeHighlight(LEAD_HIGHLIGHT).pickaxeShadow(LEAD_SHADOW).build())
            put("lead_axe", MaterialBakedModel.Builder().toolStick().axeBase(LEAD_BASE).axeHighlight(LEAD_HIGHLIGHT).axeShadow(LEAD_SHADOW).build())
            put("lead_sword", MaterialBakedModel.Builder().toolStick().swordBase(LEAD_BASE).swordHighlight(LEAD_HIGHLIGHT).swordShadow(LEAD_SHADOW).build())
            put("lead_hoe", MaterialBakedModel.Builder().toolStick().hoeBase(LEAD_BASE).hoeHighlight(LEAD_HIGHLIGHT).hoeShadow(LEAD_SHADOW).build())
            put("lead_shovel", MaterialBakedModel.Builder().toolStick().shovelBase(LEAD_BASE).shovelHighlight(LEAD_HIGHLIGHT).shovelShadow(LEAD_SHADOW).build())
            put("lead_helmet", MaterialBakedModel.Builder().helmetBase(LEAD_BASE).helmetHighlight(LEAD_HIGHLIGHT).helmetShadow(LEAD_SHADOW).build())
            put("lead_dust", MaterialBakedModel.Builder().dustBase(LEAD_BASE).dustHighlight(LEAD_HIGHLIGHT).dustShadow(LEAD_SHADOW).build())
            put("lead_chestplate", MaterialBakedModel.Builder().chestplateBase(LEAD_BASE).chestplateHighlight(LEAD_HIGHLIGHT).chestplateShadow(LEAD_SHADOW).build())
            put("lead_boots", MaterialBakedModel.Builder().bootsBase(LEAD_BASE).bootsHighlight(LEAD_HIGHLIGHT).bootsShadow(LEAD_SHADOW).build())
            put("lead_leggings", MaterialBakedModel.Builder().leggingsBase(LEAD_BASE).leggingsHighlight(LEAD_HIGHLIGHT).leggingsShadow(LEAD_SHADOW).build())
            put("lead_nugget", MaterialBakedModel.Builder().nuggetBase(LEAD_BASE).nuggetHighlight(LEAD_HIGHLIGHT).nuggetShadow(LEAD_SHADOW).build())
            putBlock("lead_ore", MaterialBakedModel.Builder().block().oreBase(LEAD_BASE).oreHighlight(LEAD_HIGHLIGHT).build())
            putBlock("lead_block", MaterialBakedModel.Builder().block().blockBase(LEAD_BASE).blockHighlight(LEAD_HIGHLIGHT).blockShadow(LEAD_SHADOW).build())
            put("lead_purified_ore", MaterialBakedModel.Builder().purifiedOreBase(LEAD_BASE).purifiedOreHighlight(LEAD_HIGHLIGHT).purifiedOreShadow(LEAD_SHADOW).build())
            put("lead_chunk", MaterialBakedModel.Builder().chunkBase(LEAD_BASE).chunkHighlight(LEAD_HIGHLIGHT).chunkShadow(LEAD_SHADOW).build())

            put("silver_ingot", MaterialBakedModel.Builder().ingotBase(SILVER_BASE).ingotHighlight(SILVER_HIGHLIGHT).ingotShadow(SILVER_SHADOW).build())
            put("silver_pickaxe", MaterialBakedModel.Builder().toolStick().pickaxeBase(SILVER_BASE).pickaxeHighlight(SILVER_HIGHLIGHT).pickaxeShadow(SILVER_SHADOW).build())
            put("silver_axe", MaterialBakedModel.Builder().toolStick().axeBase(SILVER_BASE).axeHighlight(SILVER_HIGHLIGHT).axeShadow(SILVER_SHADOW).build())
            put("silver_sword", MaterialBakedModel.Builder().toolStick().swordBase(SILVER_BASE).swordHighlight(SILVER_HIGHLIGHT).swordShadow(SILVER_SHADOW).build())
            put("silver_hoe", MaterialBakedModel.Builder().toolStick().hoeBase(SILVER_BASE).hoeHighlight(SILVER_HIGHLIGHT).hoeShadow(SILVER_SHADOW).build())
            put("silver_shovel", MaterialBakedModel.Builder().toolStick().shovelBase(SILVER_BASE).shovelHighlight(SILVER_HIGHLIGHT).shovelShadow(SILVER_SHADOW).build())
            put("silver_helmet", MaterialBakedModel.Builder().helmetBase(SILVER_BASE).helmetHighlight(SILVER_HIGHLIGHT).helmetShadow(SILVER_SHADOW).build())
            put("silver_dust", MaterialBakedModel.Builder().dustBase(SILVER_BASE).dustHighlight(SILVER_HIGHLIGHT).dustShadow(SILVER_SHADOW).build())
            put("silver_chestplate", MaterialBakedModel.Builder().chestplateBase(SILVER_BASE).chestplateHighlight(SILVER_HIGHLIGHT).chestplateShadow(SILVER_SHADOW).build())
            put("silver_boots", MaterialBakedModel.Builder().bootsBase(SILVER_BASE).bootsHighlight(SILVER_HIGHLIGHT).bootsShadow(SILVER_SHADOW).build())
            put("silver_leggings", MaterialBakedModel.Builder().leggingsBase(SILVER_BASE).leggingsHighlight(SILVER_HIGHLIGHT).leggingsShadow(SILVER_SHADOW).build())
            put("silver_nugget", MaterialBakedModel.Builder().nuggetBase(SILVER_BASE).nuggetHighlight(SILVER_HIGHLIGHT).nuggetShadow(SILVER_SHADOW).build())
            putBlock("silver_ore", MaterialBakedModel.Builder().block().oreBase(SILVER_BASE).oreHighlight(SILVER_HIGHLIGHT).build())
            putBlock("silver_block", MaterialBakedModel.Builder().block().blockBase(SILVER_BASE).blockHighlight(SILVER_HIGHLIGHT).blockShadow(SILVER_SHADOW).build())
            put("silver_purified_ore", MaterialBakedModel.Builder().purifiedOreBase(SILVER_BASE).purifiedOreHighlight(SILVER_HIGHLIGHT).purifiedOreShadow(SILVER_SHADOW).build())
            put("silver_chunk", MaterialBakedModel.Builder().chunkBase(SILVER_BASE).chunkHighlight(SILVER_HIGHLIGHT).chunkShadow(SILVER_SHADOW).build())

            put("copper_ingot", MaterialBakedModel.Builder().ingotBase(COPPER_BASE).ingotHighlight(COPPER_HIGHLIGHT).ingotShadow(COPPER_SHADOW).build())
            put("copper_pickaxe", MaterialBakedModel.Builder().toolStick().pickaxeBase(COPPER_BASE).pickaxeHighlight(COPPER_HIGHLIGHT).pickaxeShadow(COPPER_SHADOW).build())
            put("copper_axe", MaterialBakedModel.Builder().toolStick().axeBase(COPPER_BASE).axeHighlight(COPPER_HIGHLIGHT).axeShadow(COPPER_SHADOW).build())
            put("copper_sword", MaterialBakedModel.Builder().toolStick().swordBase(COPPER_BASE).swordHighlight(COPPER_HIGHLIGHT).swordShadow(COPPER_SHADOW).build())
            put("copper_hoe", MaterialBakedModel.Builder().toolStick().hoeBase(COPPER_BASE).hoeHighlight(COPPER_HIGHLIGHT).hoeShadow(COPPER_SHADOW).build())
            put("copper_shovel", MaterialBakedModel.Builder().toolStick().shovelBase(COPPER_BASE).shovelHighlight(COPPER_HIGHLIGHT).shovelShadow(COPPER_SHADOW).build())
            put("copper_helmet", MaterialBakedModel.Builder().helmetBase(COPPER_BASE).helmetHighlight(COPPER_HIGHLIGHT).helmetShadow(COPPER_SHADOW).build())
            put("copper_dust", MaterialBakedModel.Builder().dustBase(COPPER_BASE).dustHighlight(COPPER_HIGHLIGHT).dustShadow(COPPER_SHADOW).build())
            put("copper_chestplate", MaterialBakedModel.Builder().chestplateBase(COPPER_BASE).chestplateHighlight(COPPER_HIGHLIGHT).chestplateShadow(COPPER_SHADOW).build())
            put("copper_boots", MaterialBakedModel.Builder().bootsBase(COPPER_BASE).bootsHighlight(COPPER_HIGHLIGHT).bootsShadow(COPPER_SHADOW).build())
            put("copper_leggings", MaterialBakedModel.Builder().leggingsBase(COPPER_BASE).leggingsHighlight(COPPER_HIGHLIGHT).leggingsShadow(COPPER_SHADOW).build())
            put("copper_nugget", MaterialBakedModel.Builder().nuggetBase(COPPER_BASE).nuggetHighlight(COPPER_HIGHLIGHT).nuggetShadow(COPPER_SHADOW).build())
            putBlock("copper_ore", MaterialBakedModel.Builder().block().oreBase(COPPER_BASE).oreHighlight(COPPER_HIGHLIGHT).build())
            putBlock("copper_block", MaterialBakedModel.Builder().block().blockBase(COPPER_BASE).blockHighlight(COPPER_HIGHLIGHT).blockShadow(COPPER_SHADOW).build())
            put("copper_purified_ore", MaterialBakedModel.Builder().purifiedOreBase(COPPER_BASE).purifiedOreHighlight(COPPER_HIGHLIGHT).purifiedOreShadow(COPPER_SHADOW).build())
            put("copper_chunk", MaterialBakedModel.Builder().chunkBase(COPPER_BASE).chunkHighlight(COPPER_HIGHLIGHT).chunkShadow(COPPER_SHADOW).build())

            put("tungsten_ingot", MaterialBakedModel.Builder().ingotBase(TUNGSTEN_BASE).ingotHighlight(TUNGSTEN_HIGHLIGHT).ingotShadow(TUNGSTEN_SHADOW).build())
            put("tungsten_pickaxe", MaterialBakedModel.Builder().toolStick().pickaxeBase(TUNGSTEN_BASE).pickaxeHighlight(TUNGSTEN_HIGHLIGHT).pickaxeShadow(TUNGSTEN_SHADOW).build())
            put("tungsten_axe", MaterialBakedModel.Builder().toolStick().axeBase(TUNGSTEN_BASE).axeHighlight(TUNGSTEN_HIGHLIGHT).axeShadow(TUNGSTEN_SHADOW).build())
            put("tungsten_sword", MaterialBakedModel.Builder().toolStick().swordBase(TUNGSTEN_BASE).swordHighlight(TUNGSTEN_HIGHLIGHT).swordShadow(TUNGSTEN_SHADOW).build())
            put("tungsten_hoe", MaterialBakedModel.Builder().toolStick().hoeBase(TUNGSTEN_BASE).hoeHighlight(TUNGSTEN_HIGHLIGHT).hoeShadow(TUNGSTEN_SHADOW).build())
            put("tungsten_shovel", MaterialBakedModel.Builder().toolStick().shovelBase(TUNGSTEN_BASE).shovelHighlight(TUNGSTEN_HIGHLIGHT).shovelShadow(TUNGSTEN_SHADOW).build())
            put("tungsten_helmet", MaterialBakedModel.Builder().helmetBase(TUNGSTEN_BASE).helmetHighlight(TUNGSTEN_HIGHLIGHT).helmetShadow(TUNGSTEN_SHADOW).build())
            put("tungsten_dust", MaterialBakedModel.Builder().dustBase(TUNGSTEN_BASE).dustHighlight(TUNGSTEN_HIGHLIGHT).dustShadow(TUNGSTEN_SHADOW).build())
            put("tungsten_chestplate", MaterialBakedModel.Builder().chestplateBase(TUNGSTEN_BASE).chestplateHighlight(TUNGSTEN_HIGHLIGHT).chestplateShadow(TUNGSTEN_SHADOW).build())
            put("tungsten_boots", MaterialBakedModel.Builder().bootsBase(TUNGSTEN_BASE).bootsHighlight(TUNGSTEN_HIGHLIGHT).bootsShadow(TUNGSTEN_SHADOW).build())
            put("tungsten_leggings", MaterialBakedModel.Builder().leggingsBase(TUNGSTEN_BASE).leggingsHighlight(TUNGSTEN_HIGHLIGHT).leggingsShadow(TUNGSTEN_SHADOW).build())
            put("tungsten_nugget", MaterialBakedModel.Builder().nuggetBase(TUNGSTEN_BASE).nuggetHighlight(TUNGSTEN_HIGHLIGHT).nuggetShadow(TUNGSTEN_SHADOW).build())
            putBlock("tungsten_ore", MaterialBakedModel.Builder().block().oreBase(TUNGSTEN_BASE).oreHighlight(TUNGSTEN_HIGHLIGHT).build())
            putBlock("tungsten_block", MaterialBakedModel.Builder().block().blockBase(TUNGSTEN_BASE).blockHighlight(TUNGSTEN_HIGHLIGHT).blockShadow(TUNGSTEN_SHADOW).build())
            put("tungsten_purified_ore", MaterialBakedModel.Builder().purifiedOreBase(TUNGSTEN_BASE).purifiedOreHighlight(TUNGSTEN_HIGHLIGHT).purifiedOreShadow(TUNGSTEN_SHADOW).build())
            put("tungsten_chunk", MaterialBakedModel.Builder().chunkBase(TUNGSTEN_BASE).chunkHighlight(TUNGSTEN_HIGHLIGHT).chunkShadow(TUNGSTEN_SHADOW).build())

            put("bronze_ingot", MaterialBakedModel.Builder().ingotBase(BRONZE_BASE).ingotHighlight(BRONZE_HIGHLIGHT).ingotShadow(BRONZE_SHADOW).build())
            put("bronze_pickaxe", MaterialBakedModel.Builder().toolStick().pickaxeBase(BRONZE_BASE).pickaxeHighlight(BRONZE_HIGHLIGHT).pickaxeShadow(BRONZE_SHADOW).build())
            put("bronze_axe", MaterialBakedModel.Builder().toolStick().axeBase(BRONZE_BASE).axeHighlight(BRONZE_HIGHLIGHT).axeShadow(BRONZE_SHADOW).build())
            put("bronze_sword", MaterialBakedModel.Builder().toolStick().swordBase(BRONZE_BASE).swordHighlight(BRONZE_HIGHLIGHT).swordShadow(BRONZE_SHADOW).build())
            put("bronze_hoe", MaterialBakedModel.Builder().toolStick().hoeBase(BRONZE_BASE).hoeHighlight(BRONZE_HIGHLIGHT).hoeShadow(BRONZE_SHADOW).build())
            put("bronze_shovel", MaterialBakedModel.Builder().toolStick().shovelBase(BRONZE_BASE).shovelHighlight(BRONZE_HIGHLIGHT).shovelShadow(BRONZE_SHADOW).build())
            put("bronze_helmet", MaterialBakedModel.Builder().helmetBase(BRONZE_BASE).helmetHighlight(BRONZE_HIGHLIGHT).helmetShadow(BRONZE_SHADOW).build())
            put("bronze_dust", MaterialBakedModel.Builder().dustBase(BRONZE_BASE).dustHighlight(BRONZE_HIGHLIGHT).dustShadow(BRONZE_SHADOW).build())
            put("bronze_chestplate", MaterialBakedModel.Builder().chestplateBase(BRONZE_BASE).chestplateHighlight(BRONZE_HIGHLIGHT).chestplateShadow(BRONZE_SHADOW).build())
            put("bronze_boots", MaterialBakedModel.Builder().bootsBase(BRONZE_BASE).bootsHighlight(BRONZE_HIGHLIGHT).bootsShadow(BRONZE_SHADOW).build())
            put("bronze_leggings", MaterialBakedModel.Builder().leggingsBase(BRONZE_BASE).leggingsHighlight(BRONZE_HIGHLIGHT).leggingsShadow(BRONZE_SHADOW).build())
            put("bronze_nugget", MaterialBakedModel.Builder().nuggetBase(BRONZE_BASE).nuggetHighlight(BRONZE_HIGHLIGHT).nuggetShadow(BRONZE_SHADOW).build())
            putBlock("bronze_ore", MaterialBakedModel.Builder().block().oreBase(BRONZE_BASE).oreHighlight(BRONZE_HIGHLIGHT).build())
            putBlock("bronze_block", MaterialBakedModel.Builder().block().blockBase(BRONZE_BASE).blockHighlight(BRONZE_HIGHLIGHT).blockShadow(BRONZE_SHADOW).build())
            put("bronze_purified_ore", MaterialBakedModel.Builder().purifiedOreBase(BRONZE_BASE).purifiedOreHighlight(BRONZE_HIGHLIGHT).purifiedOreShadow(BRONZE_SHADOW).build())
            put("bronze_chunk", MaterialBakedModel.Builder().chunkBase(BRONZE_BASE).chunkHighlight(BRONZE_HIGHLIGHT).chunkShadow(BRONZE_SHADOW).build())

            put("electrum_ingot", MaterialBakedModel.Builder().ingotBase(ELECTRUM_BASE).ingotHighlight(ELECTRUM_HIGHLIGHT).ingotShadow(ELECTRUM_SHADOW).build())
            put("electrum_pickaxe", MaterialBakedModel.Builder().toolStick().pickaxeBase(ELECTRUM_BASE).pickaxeHighlight(ELECTRUM_HIGHLIGHT).pickaxeShadow(ELECTRUM_SHADOW).build())
            put("electrum_axe", MaterialBakedModel.Builder().toolStick().axeBase(ELECTRUM_BASE).axeHighlight(ELECTRUM_HIGHLIGHT).axeShadow(ELECTRUM_SHADOW).build())
            put("electrum_sword", MaterialBakedModel.Builder().toolStick().swordBase(ELECTRUM_BASE).swordHighlight(ELECTRUM_HIGHLIGHT).swordShadow(ELECTRUM_SHADOW).build())
            put("electrum_hoe", MaterialBakedModel.Builder().toolStick().hoeBase(ELECTRUM_BASE).hoeHighlight(ELECTRUM_HIGHLIGHT).hoeShadow(ELECTRUM_SHADOW).build())
            put("electrum_shovel", MaterialBakedModel.Builder().toolStick().shovelBase(ELECTRUM_BASE).shovelHighlight(ELECTRUM_HIGHLIGHT).shovelShadow(ELECTRUM_SHADOW).build())
            put("electrum_helmet", MaterialBakedModel.Builder().helmetBase(ELECTRUM_BASE).helmetHighlight(ELECTRUM_HIGHLIGHT).helmetShadow(ELECTRUM_SHADOW).build())
            put("electrum_dust", MaterialBakedModel.Builder().dustBase(ELECTRUM_BASE).dustHighlight(ELECTRUM_HIGHLIGHT).dustShadow(ELECTRUM_SHADOW).build())
            put("electrum_chestplate", MaterialBakedModel.Builder().chestplateBase(ELECTRUM_BASE).chestplateHighlight(ELECTRUM_HIGHLIGHT).chestplateShadow(ELECTRUM_SHADOW).build())
            put("electrum_boots", MaterialBakedModel.Builder().bootsBase(ELECTRUM_BASE).bootsHighlight(ELECTRUM_HIGHLIGHT).bootsShadow(ELECTRUM_SHADOW).build())
            put("electrum_leggings", MaterialBakedModel.Builder().leggingsBase(ELECTRUM_BASE).leggingsHighlight(ELECTRUM_HIGHLIGHT).leggingsShadow(ELECTRUM_SHADOW).build())
            put("electrum_nugget", MaterialBakedModel.Builder().nuggetBase(ELECTRUM_BASE).nuggetHighlight(ELECTRUM_HIGHLIGHT).nuggetShadow(ELECTRUM_SHADOW).build())
            putBlock("electrum_ore", MaterialBakedModel.Builder().block().oreBase(ELECTRUM_BASE).oreHighlight(ELECTRUM_HIGHLIGHT).build())
            putBlock("electrum_block", MaterialBakedModel.Builder().block().blockBase(ELECTRUM_BASE).blockHighlight(ELECTRUM_HIGHLIGHT).blockShadow(ELECTRUM_SHADOW).build())
            put("electrum_purified_ore", MaterialBakedModel.Builder().purifiedOreBase(ELECTRUM_BASE).purifiedOreHighlight(ELECTRUM_HIGHLIGHT).purifiedOreShadow(ELECTRUM_SHADOW).build())
            put("electrum_chunk", MaterialBakedModel.Builder().chunkBase(ELECTRUM_BASE).chunkHighlight(ELECTRUM_HIGHLIGHT).chunkShadow(ELECTRUM_SHADOW).build())
        }

        private fun put(id: String, model: UnbakedModel) {
            MATERIAL_PROVIDERS[ModelIdentifier(identifier(id), "inventory")] = model
        }

        private fun putBlock(id: String, model: UnbakedModel) {
            MATERIAL_PROVIDERS[ModelIdentifier(identifier(id), "inventory")] = model
            MATERIAL_PROVIDERS[ModelIdentifier(identifier(id), "")] = model
        }
    }
}
