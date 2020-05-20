package me.steven.indrev.registry

import me.steven.indrev.IndustrialRevolution
import me.steven.indrev.blocks.CoalGeneratorBlockEntity
import me.steven.indrev.blocks.GeneratorBlock
import me.steven.indrev.generator
import me.steven.indrev.identifier
import me.steven.indrev.item
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.fabricmc.fabric.api.tool.attribute.v1.FabricToolTags
import net.minecraft.block.Material
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.sound.BlockSoundGroup
import java.util.function.Supplier

class GeneratorRegistry {

    fun registerAll() {
        identifier("coal_generator").generator(COAL_GENERATOR, COAL_GENERATOR_BLOCK_ENTITY).item(COAL_GENERATOR_BLOCK_ITEM)
    }

    companion object {

        val COAL_GENERATOR: GeneratorBlock = GeneratorBlock(
            FabricBlockSettings.of(Material.METAL).sounds(BlockSoundGroup.METAL).breakByTool(FabricToolTags.PICKAXES),
            IndustrialRevolution.COAL_GENERATOR_SCREEN_ID,
            1000.0
        ) { CoalGeneratorBlockEntity() }
        val COAL_GENERATOR_BLOCK_ITEM: BlockItem = BlockItem(COAL_GENERATOR, Item.Settings().group(IndustrialRevolution.MOD_GROUP))
        val COAL_GENERATOR_BLOCK_ENTITY: BlockEntityType<CoalGeneratorBlockEntity> =
            BlockEntityType.Builder.create(Supplier { CoalGeneratorBlockEntity() }, COAL_GENERATOR).build(null)
    }
}