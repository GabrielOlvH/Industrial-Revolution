package me.steven.indrev.registry

import me.steven.indrev.utils.identifier
import me.steven.indrev.utils.itemSettings
import net.fabricmc.api.EnvType
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.block.Block
import net.minecraft.block.OreBlock
import net.minecraft.block.Material
import net.minecraft.client.render.RenderLayer
import net.minecraft.entity.EquipmentSlot
import net.minecraft.item.*
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry

class MaterialHelper(private val id: String, private val block: MaterialHelper.() -> Unit) {

    fun withItems(vararg variants: String): MaterialHelper {
        variants.forEach { variant ->
            val identifier = identifier("${id}_$variant")
            map[identifier] = { Registry.register(Registry.ITEM, identifier, Item(itemSettings())) }
        }
        return this
    }

    fun withItem(): MaterialHelper {
        Registry.register(Registry.ITEM, identifier(id), Item(itemSettings()))
        return this
    }

    fun withOre(rawOre: Boolean = true, supplier: (FabricBlockSettings) -> Block = { OreBlock(it) }): MaterialHelper {
        val ore = supplier(FabricBlockSettings.of(Material.STONE).requiresTool().strength(3f, 3f))
        val identifier = identifier("${id}_ore")
        map[identifier] = {
            Registry.register(Registry.BLOCK, identifier, ore)
            Registry.register(Registry.ITEM, identifier, BlockItem(ore, itemSettings()))
        }

        val deepslateOre = supplier(FabricBlockSettings.of(Material.STONE).requiresTool().strength(3f, 3f))
        val deepslateId = identifier("deepslate_${id}_ore")
        map[deepslateId] = {
            Registry.register(Registry.BLOCK, deepslateId, deepslateOre)
            Registry.register(Registry.ITEM, deepslateId, BlockItem(deepslateOre, itemSettings()))
        }

        if (rawOre) {
            val rawOreBlock = supplier(FabricBlockSettings.of(Material.STONE).requiresTool().strength(3f, 3f))
            val rawOreId = identifier("raw_${id}")
            val rawOreBlockId = identifier("raw_${id}_block")
            map[rawOreId] = {
                Registry.register(Registry.BLOCK, rawOreBlockId, rawOreBlock)
                Registry.register(Registry.ITEM, rawOreBlockId, BlockItem(rawOreBlock, itemSettings()))
                Registry.register(Registry.ITEM, rawOreId, Item(itemSettings()))
            }
        }

        return this
    }

    fun withTools(pickaxe: PickaxeItem, axe: AxeItem, shovel: ShovelItem, sword: SwordItem, hoe: HoeItem) {
        map[identifier("${id}_pickaxe")] = {
            Registry.register(Registry.ITEM, identifier("${id}_pickaxe"), pickaxe)
        }
        map[identifier("${id}_axe")] = {
            Registry.register(Registry.ITEM, identifier("${id}_axe"), axe)
        }
        map[identifier("${id}_shovel")] = {
            Registry.register(Registry.ITEM, identifier("${id}_shovel"), shovel)
        }
        map[identifier("${id}_sword")] = {
            Registry.register(Registry.ITEM, identifier("${id}_sword"), sword)
        }
        map[identifier("${id}_hoe")] = {
            Registry.register(Registry.ITEM, identifier("${id}_hoe"), hoe)
        }
    }

    fun withArmor(material: ArmorMaterial) {
        map[identifier("${id}_helmet")] = {
            Registry.register(Registry.ITEM, identifier("${id}_helmet"), ArmorItem(material, EquipmentSlot.HEAD, itemSettings()))
        }
        map[identifier("${id}_chestplate")] = {
            Registry.register(Registry.ITEM, identifier("${id}_chestplate"), ArmorItem(material, EquipmentSlot.CHEST, itemSettings()))
        }
        map[identifier("${id}_leggings")] = {
            Registry.register(Registry.ITEM, identifier("${id}_leggings"), ArmorItem(material, EquipmentSlot.LEGS, itemSettings()))
        }
        map[identifier("${id}_boots")] = {
            Registry.register(Registry.ITEM, identifier("${id}_boots"), ArmorItem(material, EquipmentSlot.FEET, itemSettings()))
        }
    }

    fun withBlock(): MaterialHelper {
        val block =
            Block(FabricBlockSettings.of(Material.METAL).requiresTool().strength(5f, 6f))
        val id = identifier("${id}_block")
        map[id] = {
            Registry.register(Registry.BLOCK, id, block)
            Registry.register(Registry.ITEM, id, BlockItem(block, itemSettings()))
        }
        if (FabricLoader.getInstance().environmentType == EnvType.CLIENT) {
            BlockRenderLayerMap.INSTANCE.putBlock(block, RenderLayer.getCutout())
        }
        return this
    }

    fun register() = block()

    companion object {
        val map = HashMap<Identifier, () -> Unit>()

        fun register() {
            map.entries.sortedWith(
                compareBy<MutableMap.MutableEntry<Identifier, () -> Unit>> { id -> id.key.path.contains("ore") && !id.key.path.contains("purified") }
                    .then(compareBy { id -> id.key.path.contains("raw") })
                    .then(compareBy { id -> id.key.path.contains("block") })
                    .then(compareBy { id -> id.key.path.contains("ingot") })
                    .then(compareBy { id -> id.key.path.contains("chunk") })
                    .then(compareBy { id -> id.key.path.contains("dust") })
                    .then(compareBy { id -> id.key.path.contains("purified") })
                    .then(compareBy { id -> id.key.path.contains("plate") && !id.key.path.contains("chestplate") })
                    .then(compareBy { id -> id.key.path.contains("nugget") })
                    .then(compareBy<MutableMap.MutableEntry<Identifier, () -> Unit>> { id -> id.key.path.substring(0, id.key.path.indexOf("_")) }
                        .then(compareBy { id -> id.key.path.contains("sword") })
                        .then(compareBy { id -> id.key.path.contains("pickaxe") })
                        .then(compareBy { id -> id.key.path.contains("axe") && !id.key.path.contains("pickaxe") })
                        .then(compareBy { id -> id.key.path.contains("shovel") })
                        .then(compareBy { id -> id.key.path.contains("hoe") })
                        .then(compareBy { id -> id.key.path.contains("helmet") })
                        .then(compareBy { id -> id.key.path.contains("chestplate") })
                        .then(compareBy { id -> id.key.path.contains("leggings") })
                        .then(compareBy { id -> id.key.path.contains("boots") })
                    )
            ).asReversed().forEach { it.value() }
        }
    }
}
