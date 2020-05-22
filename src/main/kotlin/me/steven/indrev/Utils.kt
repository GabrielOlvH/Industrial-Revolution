package me.steven.indrev

import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.fabricmc.fabric.api.tool.attribute.v1.FabricToolTags
import net.minecraft.block.Block
import net.minecraft.block.Material
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.item.Item
import net.minecraft.sound.BlockSoundGroup
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry
import java.util.function.Supplier

fun identifier(id: String) = Identifier(IndustrialRevolution.MOD_ID, id)

fun Identifier.block(block: Block): Identifier {
    Registry.register(Registry.BLOCK, this, block)
    return this
}

fun Identifier.item(item: Item): Identifier {
    Registry.register(Registry.ITEM, this, item)
    return this
}

fun Identifier.blockEntityType(entityType: BlockEntityType<*>): Identifier {
    Registry.register(Registry.BLOCK_ENTITY_TYPE, this, entityType)
    return this
}

fun Identifier.ore(pulverizable: Boolean): Block {
    val key = this.namespace

    val block = Block(FabricBlockSettings.of(Material.STONE).sounds(BlockSoundGroup.STONE).breakByTool(FabricToolTags.PICKAXES))
    identifier("${key}_ore").block(block).item(Item(Item.Settings().group(IndustrialRevolution.MOD_GROUP)))
    if (pulverizable) identifier("pulverized_$key").item(Item(Item.Settings().group(IndustrialRevolution.MOD_GROUP)))
    return block
}

fun <T : BlockEntity> Block.blockEntityType(supplier: () -> T): BlockEntityType<T> = BlockEntityType.Builder.create(Supplier(supplier), this).build(null)