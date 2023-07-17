package me.steven.indrev.utils

import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.minecraft.block.Block
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.registry.tag.TagKey

class Material(val material: String, val types: Map<Type, Item>) {
    enum class Type(val idFormat: String, val tag: String) {

        BLOCK("%s_block", "%s_blocks"),
        ORE("%s_ore", "%s_ores"),
        DEEPSLATE_ORE("deepslate_%s_ore", "%s_ores"),
        RAW_BLOCK_ORE("raw_%s_block", "raw_%s_blocks"),
        RAW_ORE("raw_%s", "raw_%s_ores"),
        INGOT("%s_ingot", "%s_ingots"),
        DUST("%s_dust", "%s_dusts"),
        PLATE("%s_plate", "%s_plates"),
        NUGGET("%s_nugget", "%s_nuggets");
    }

    companion object {

        val ORE_TYPES = arrayOf(Type.INGOT, Type.RAW_ORE, Type.DUST, Type.PLATE, Type.BLOCK, Type.RAW_BLOCK_ORE, Type.ORE, Type.DEEPSLATE_ORE, Type.NUGGET)
        val ALLOY_TYPES = arrayOf(Type.INGOT, Type.DUST, Type.PLATE, Type.BLOCK, Type.NUGGET)
        fun createOre(material: String): Material = create(material, *ORE_TYPES)
        fun createAlloy(material: String): Material = create(material, *ALLOY_TYPES)
        fun create(material: String, vararg types: Type): Material {
            val map = mutableMapOf<Type, Item>()
            for (type in types) {
                val item = if (type == Type.BLOCK || type == Type.ORE || type == Type.RAW_BLOCK_ORE || type == Type.DEEPSLATE_ORE) {
                    val block = Block(FabricBlockSettings.create())
                    BlockItem(block, itemSettings())
                } else {
                    Item(itemSettings())
                }
                map[type] = item
            }

            return Material(material, map)
        }

        fun register(vararg materials: Material) {
            Type.values().forEach { type ->
                materials.forEach { material ->
                    val item = material.types[type] ?: return@forEach
                    val id = identifier(type.idFormat.format(material.material))
                    id.item(item)
                    if (item is BlockItem) {
                        id.block(item.block)
                    }

                }
            }

        }
    }
}