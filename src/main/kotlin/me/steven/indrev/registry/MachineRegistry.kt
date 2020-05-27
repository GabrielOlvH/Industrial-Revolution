package me.steven.indrev.registry

import me.steven.indrev.blockentities.battery.BatteryBlockEntity
import me.steven.indrev.blockentities.cables.CableBlockEntity
import me.steven.indrev.blockentities.crafters.CompressorBlockEntity
import me.steven.indrev.blockentities.crafters.ElectricFurnaceBlockEntity
import me.steven.indrev.blockentities.crafters.PulverizerBlockEntity
import me.steven.indrev.blockentities.generators.CoalGeneratorBlockEntity
import me.steven.indrev.blockentities.generators.SolarGeneratorBlockEntity
import me.steven.indrev.blocks.BasicMachineBlock
import me.steven.indrev.blocks.CableBlock
import me.steven.indrev.blocks.InterfacedMachineBlock
import me.steven.indrev.gui.battery.BatteryScreen
import me.steven.indrev.gui.compressor.CompressorScreen
import me.steven.indrev.gui.furnace.ElectricFurnaceScreen
import me.steven.indrev.gui.generators.CoalGeneratorScreen
import me.steven.indrev.gui.pulverizer.PulverizerScreen
import me.steven.indrev.utils.*
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.fabricmc.fabric.api.tool.attribute.v1.FabricToolTags
import net.minecraft.block.Material
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.item.BlockItem
import net.minecraft.sound.BlockSoundGroup

class MachineRegistry {

    fun registerAll() {
        identifier("coal_generator").block(COAL_GENERATOR).item(COAL_GENERATOR_BLOCK_ITEM).blockEntityType(COAL_GENERATOR_BLOCK_ENTITY)
        identifier("solar_generator").block(SOLAR_GENERATOR).item(SOLAR_GENERATOR_ITEM).blockEntityType(SOLAR_GENERATOR_BLOCK_ENTITY)
        identifier("electric_furnace").block(ELECTRIC_FURNACE).item(ELECTRIC_FURNACE_BLOCK_ITEM).blockEntityType(ELECTRIC_FURNACE_BLOCK_ENTITY)
        identifier("compressor").block(COMPRESSOR).item(COMPRESSOR_BLOCK_ITEM).blockEntityType(COMPRESSOR_BLOCK_ENTITY)
        identifier("pulverizer").block(PULVERIZER).item(PULVERIZER_BLOCK_ITEM).blockEntityType(PULVERIZER_BLOCK_ENTITY)
        identifier("lazuli_flux_container").block(BATTERY_BLOCK).item(BATTERY_BLOCK_ITEM).blockEntityType(BATTERY_BLOCK_ENTITY)
        identifier("cable").block(CABLE).item(CABLE_ITEM).blockEntityType(CABLE_BLOCK_ENTITY)
    }

    companion object {

        private val MACHINE_BLOCK_SETTINGS =
            FabricBlockSettings.of(Material.METAL).sounds(BlockSoundGroup.METAL).breakByTool(FabricToolTags.PICKAXES)

        val COAL_GENERATOR: InterfacedMachineBlock = InterfacedMachineBlock(
            MACHINE_BLOCK_SETTINGS, Tier.LOW, CoalGeneratorScreen.SCREEN_ID, { it is CoalGeneratorBlockEntity }
        ) { CoalGeneratorBlockEntity() }
        val COAL_GENERATOR_BLOCK_ITEM: BlockItem = BlockItem(
            COAL_GENERATOR,
            itemSettings()
        )
        val COAL_GENERATOR_BLOCK_ENTITY: BlockEntityType<CoalGeneratorBlockEntity> =
            COAL_GENERATOR.blockEntityType { CoalGeneratorBlockEntity() }

        val SOLAR_GENERATOR: BasicMachineBlock =
            BasicMachineBlock(MACHINE_BLOCK_SETTINGS, Tier.LOW) { SolarGeneratorBlockEntity() }
        val SOLAR_GENERATOR_ITEM: BlockItem = BlockItem(
            SOLAR_GENERATOR,
            itemSettings()
        )
        val SOLAR_GENERATOR_BLOCK_ENTITY: BlockEntityType<SolarGeneratorBlockEntity> =
            SOLAR_GENERATOR.blockEntityType { SolarGeneratorBlockEntity() }

        val ELECTRIC_FURNACE: InterfacedMachineBlock = InterfacedMachineBlock(
            MACHINE_BLOCK_SETTINGS, Tier.LOW, ElectricFurnaceScreen.SCREEN_ID, { it is ElectricFurnaceBlockEntity }
        ) { ElectricFurnaceBlockEntity(Tier.LOW) }
        val ELECTRIC_FURNACE_BLOCK_ITEM: BlockItem = BlockItem(
            ELECTRIC_FURNACE,
            itemSettings()
        )
        val ELECTRIC_FURNACE_BLOCK_ENTITY: BlockEntityType<ElectricFurnaceBlockEntity> =
            ELECTRIC_FURNACE.blockEntityType { ElectricFurnaceBlockEntity(Tier.LOW) }

        val PULVERIZER: InterfacedMachineBlock = InterfacedMachineBlock(
            MACHINE_BLOCK_SETTINGS, Tier.LOW, PulverizerScreen.SCREEN_ID, { it is PulverizerBlockEntity }
        ) { PulverizerBlockEntity(Tier.LOW) }
        val PULVERIZER_BLOCK_ITEM: BlockItem = BlockItem(
            PULVERIZER,
            itemSettings()
        )
        val PULVERIZER_BLOCK_ENTITY: BlockEntityType<PulverizerBlockEntity> =
            PULVERIZER.blockEntityType { PulverizerBlockEntity(Tier.LOW) }

        val COMPRESSOR: InterfacedMachineBlock = InterfacedMachineBlock(
            MACHINE_BLOCK_SETTINGS, Tier.LOW, CompressorScreen.SCREEN_ID, { it is CompressorBlockEntity }
        ) { CompressorBlockEntity(Tier.LOW) }
        val COMPRESSOR_BLOCK_ITEM: BlockItem = BlockItem(
            COMPRESSOR,
            itemSettings()
        )
        val COMPRESSOR_BLOCK_ENTITY: BlockEntityType<CompressorBlockEntity> =
            COMPRESSOR.blockEntityType { CompressorBlockEntity(Tier.LOW) }

        val BATTERY_BLOCK: InterfacedMachineBlock = InterfacedMachineBlock(
            MACHINE_BLOCK_SETTINGS, Tier.LOW, BatteryScreen.SCREEN_ID, { it is BatteryBlockEntity }
        ) { BatteryBlockEntity(Tier.LOW) }
        val BATTERY_BLOCK_ITEM: BlockItem = BlockItem(
            BATTERY_BLOCK,
            itemSettings()
        )
        val BATTERY_BLOCK_ENTITY: BlockEntityType<BatteryBlockEntity> =
            BATTERY_BLOCK.blockEntityType { BatteryBlockEntity(Tier.LOW) }

        val CABLE: CableBlock = CableBlock(MACHINE_BLOCK_SETTINGS, Tier.LOW)
        val CABLE_ITEM: BlockItem = BlockItem(CABLE, itemSettings())
        val CABLE_BLOCK_ENTITY: BlockEntityType<CableBlockEntity> = CABLE.blockEntityType { CableBlockEntity(Tier.LOW) }
    }
}