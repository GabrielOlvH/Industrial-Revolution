package me.steven.indrev.registry

import me.steven.indrev.*
import me.steven.indrev.blocks.furnace.ElectricFurnaceBlock
import me.steven.indrev.blocks.furnace.ElectricFurnaceBlockEntity
import me.steven.indrev.blocks.generators.CoalGeneratorBlockEntity
import me.steven.indrev.blocks.generators.GeneratorBlock
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.fabricmc.fabric.api.tool.attribute.v1.FabricToolTags
import net.minecraft.block.Material
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.sound.BlockSoundGroup
import java.util.function.Supplier

class MachineRegistry {

    fun registerAll() {
        identifier("coal_generator").generator(COAL_GENERATOR, COAL_GENERATOR_BLOCK_ENTITY).item(COAL_GENERATOR_BLOCK_ITEM)
        identifier("electric_furnace").block(ELECTRIC_FURNACE).blockEntityType(ELECTRIC_FURNACE_BLOCK_ENTITY).item(ELECTRIC_FURNACE_BLOCK_ITEM)
    }

    companion object {
        val COAL_GENERATOR: GeneratorBlock = GeneratorBlock(
                FabricBlockSettings.of(Material.METAL).sounds(BlockSoundGroup.METAL).breakByTool(FabricToolTags.PICKAXES),
                GeneratorBlock.COAL_GENERATOR_SCREEN_ID,
                1000.0
        ) { CoalGeneratorBlockEntity() }
        val COAL_GENERATOR_BLOCK_ITEM: BlockItem = BlockItem(COAL_GENERATOR, Item.Settings().group(IndustrialRevolution.MOD_GROUP))
        val COAL_GENERATOR_BLOCK_ENTITY: BlockEntityType<CoalGeneratorBlockEntity> =
            BlockEntityType.Builder.create(Supplier { CoalGeneratorBlockEntity() }, COAL_GENERATOR).build(null)

        val ELECTRIC_FURNACE: ElectricFurnaceBlock = ElectricFurnaceBlock(
                FabricBlockSettings.of(Material.METAL).sounds(BlockSoundGroup.METAL).breakByTool(FabricToolTags.PICKAXES),
                ElectricFurnaceBlock.SCREEN_ID,
                 250.0
        ) { ElectricFurnaceBlockEntity() }
        val ELECTRIC_FURNACE_BLOCK_ITEM: BlockItem = BlockItem(ELECTRIC_FURNACE, Item.Settings().group(IndustrialRevolution.MOD_GROUP))
        val ELECTRIC_FURNACE_BLOCK_ENTITY: BlockEntityType<ElectricFurnaceBlockEntity> = BlockEntityType.Builder.create(Supplier { ElectricFurnaceBlockEntity() }, ELECTRIC_FURNACE).build(null)
    }
}