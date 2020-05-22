package me.steven.indrev.content

import me.steven.indrev.*
import me.steven.indrev.blocks.InterfacedMachineBlock
import me.steven.indrev.blocks.cables.CableBlock
import me.steven.indrev.blocks.cables.CableBlockEntity
import me.steven.indrev.blocks.crafters.ElectricFurnaceBlockEntity
import me.steven.indrev.blocks.crafters.PulverizerBlockEntity
import me.steven.indrev.blocks.generators.CoalGeneratorBlockEntity
import me.steven.indrev.gui.furnace.ElectricFurnaceScreen
import me.steven.indrev.gui.generators.CoalGeneratorScreen
import me.steven.indrev.gui.pulverizer.PulverizerScreen
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.fabricmc.fabric.api.tool.attribute.v1.FabricToolTags
import net.minecraft.block.Material
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.sound.BlockSoundGroup

class MachineRegistry {

    fun registerAll() {
        identifier("coal_generator").block(COAL_GENERATOR).item(COAL_GENERATOR_BLOCK_ITEM).blockEntityType(COAL_GENERATOR_BLOCK_ENTITY)
        identifier("electric_furnace").block(ELECTRIC_FURNACE).item(ELECTRIC_FURNACE_BLOCK_ITEM).blockEntityType(ELECTRIC_FURNACE_BLOCK_ENTITY)
        identifier("pulverizer").block(PULVERIZER).item(PULVERIZER_BLOCK_ITEM).blockEntityType(PULVERIZER_BLOCK_ENTITY)
        identifier("cable").block(CABLE).item(CABLE_ITEM).blockEntityType(CABLE_BLOCK_ENTITY)
    }

    companion object {

        private val MACHINE_BLOCK_SETTINGS = FabricBlockSettings.of(Material.METAL).sounds(BlockSoundGroup.METAL).breakByTool(FabricToolTags.PICKAXES)

        val COAL_GENERATOR: InterfacedMachineBlock = InterfacedMachineBlock(
                MACHINE_BLOCK_SETTINGS,
                CoalGeneratorScreen.SCREEN_ID,
                { it is CoalGeneratorBlockEntity }
        ) { CoalGeneratorBlockEntity() }
        val COAL_GENERATOR_BLOCK_ITEM: BlockItem = BlockItem(COAL_GENERATOR, Item.Settings().group(IndustrialRevolution.MOD_GROUP))
        val COAL_GENERATOR_BLOCK_ENTITY: BlockEntityType<CoalGeneratorBlockEntity> = COAL_GENERATOR.blockEntityType { CoalGeneratorBlockEntity() }

        val ELECTRIC_FURNACE: InterfacedMachineBlock = InterfacedMachineBlock(
                MACHINE_BLOCK_SETTINGS,
                ElectricFurnaceScreen.SCREEN_ID,
                { it is ElectricFurnaceBlockEntity }
        ) { ElectricFurnaceBlockEntity() }
        val ELECTRIC_FURNACE_BLOCK_ITEM: BlockItem = BlockItem(ELECTRIC_FURNACE, Item.Settings().group(IndustrialRevolution.MOD_GROUP))
        val ELECTRIC_FURNACE_BLOCK_ENTITY: BlockEntityType<ElectricFurnaceBlockEntity> = ELECTRIC_FURNACE.blockEntityType { ElectricFurnaceBlockEntity() }

        val PULVERIZER: InterfacedMachineBlock = InterfacedMachineBlock(
            MACHINE_BLOCK_SETTINGS,
            PulverizerScreen.SCREEN_ID,
            { it is PulverizerBlockEntity }
        ) { PulverizerBlockEntity() }
        val PULVERIZER_BLOCK_ITEM: BlockItem = BlockItem(PULVERIZER, Item.Settings().group(IndustrialRevolution.MOD_GROUP))
        val PULVERIZER_BLOCK_ENTITY: BlockEntityType<PulverizerBlockEntity> = PULVERIZER.blockEntityType { PulverizerBlockEntity() }

        val CABLE: CableBlock = CableBlock(MACHINE_BLOCK_SETTINGS)
        val CABLE_ITEM: BlockItem = BlockItem(CABLE, Item.Settings().group(IndustrialRevolution.MOD_GROUP))
        val CABLE_BLOCK_ENTITY: BlockEntityType<CableBlockEntity> = CABLE.blockEntityType { CableBlockEntity() }
    }
}