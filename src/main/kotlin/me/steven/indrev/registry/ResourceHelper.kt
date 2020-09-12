package me.steven.indrev.registry

import me.steven.indrev.utils.identifier
import me.steven.indrev.utils.itemSettings
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.fabricmc.fabric.api.tool.attribute.v1.FabricToolTags
import net.minecraft.block.Block
import net.minecraft.block.Material
import net.minecraft.entity.EquipmentSlot
import net.minecraft.item.*
import net.minecraft.util.registry.Registry

class ResourceHelper(private val id: String, private val block: ResourceHelper.() -> Unit) {

    fun withItems(vararg variants: String): ResourceHelper {
        variants.forEach { variant ->
            Registry.register(Registry.ITEM, identifier("${id}_$variant"), Item(itemSettings()))
        }
        return this
    }

    fun withItem(): ResourceHelper {
        Registry.register(Registry.ITEM, identifier(id), Item(itemSettings()))
        return this
    }

    fun withOre(supplier: (FabricBlockSettings) -> Block = { Block(it) }): ResourceHelper {
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

    fun withBlock(): ResourceHelper {
        val block =
            Block(FabricBlockSettings.of(Material.METAL).requiresTool().breakByTool(FabricToolTags.PICKAXES, 2).strength(5f, 6f))
        val id = identifier("${id}_block")
        Registry.register(Registry.BLOCK, id, block)
        Registry.register(Registry.ITEM, id, BlockItem(block, itemSettings()))
        return this
    }

    fun register() = block()
}