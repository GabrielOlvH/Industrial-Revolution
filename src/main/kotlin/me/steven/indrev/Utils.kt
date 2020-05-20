package me.steven.indrev

import me.steven.indrev.blocks.generators.GeneratorBlock
import me.steven.indrev.blocks.generators.GeneratorBlockEntity
import net.minecraft.block.Block
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.item.Item
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry

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

fun Identifier.generator(generatorBlock: GeneratorBlock, entityType: BlockEntityType<out GeneratorBlockEntity>): Identifier = this.block(generatorBlock).blockEntityType(entityType)