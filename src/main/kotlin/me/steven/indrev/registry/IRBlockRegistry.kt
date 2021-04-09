package me.steven.indrev.registry

import me.steven.indrev.api.machines.Tier
import me.steven.indrev.blockentities.cables.CoverableBlockEntity
import me.steven.indrev.blockentities.drill.DrillBlockEntity
import me.steven.indrev.blockentities.laser.CapsuleBlockEntity
import me.steven.indrev.blockentities.storage.CabinetBlockEntity
import me.steven.indrev.blockentities.storage.TankBlockEntity
import me.steven.indrev.blocks.machine.CapsuleBlock
import me.steven.indrev.blocks.machine.DrillBlock
import me.steven.indrev.blocks.machine.pipes.CableBlock
import me.steven.indrev.blocks.machine.pipes.FluidPipeBlock
import me.steven.indrev.blocks.machine.pipes.ItemPipeBlock
import me.steven.indrev.blocks.misc.*
import me.steven.indrev.utils.*
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.fabricmc.fabric.api.tool.attribute.v1.FabricToolTags
import net.minecraft.block.Block
import net.minecraft.block.Blocks
import net.minecraft.block.Material
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.item.BlockItem
import net.minecraft.sound.BlockSoundGroup
import net.minecraft.util.registry.Registry

@Suppress("MemberVisibilityCanBePrivate")
object IRBlockRegistry {

    fun registerAll() {

        identifier("sulfur_crystal").block(SULFUR_CRYSTAL_CLUSTER).item(IRItemRegistry.SULFUR_CRYSTAL_ITEM)

        identifier("planks").block(PLANKS).item(BlockItem(PLANKS, itemSettings()))
        identifier("plank_block").block(PLANK_BLOCK).item(BlockItem(PLANK_BLOCK, itemSettings()))

        identifier("machine_block").block(MACHINE_BLOCK).item(BlockItem(MACHINE_BLOCK, itemSettings()))

        identifier("controller").block(CONTROLLER).item(BlockItem(CONTROLLER, itemSettings()))
        identifier("frame").block(FRAME).item(BlockItem(FRAME, itemSettings()))
        identifier("duct").block(DUCT).item(BlockItem(DUCT, itemSettings()))
        identifier("silo").block(SILO).item(BlockItem(SILO, itemSettings()))
        identifier("warning_strobe").block(WARNING_STROBE).item(BlockItem(WARNING_STROBE, itemSettings()))
        identifier("intake").block(INTAKE).item(BlockItem(INTAKE, itemSettings()))
        identifier("cabinet").block(CABINET).item(BlockItem(CABINET, itemSettings())).blockEntityType(CABINET_BLOCK_ENTITY_TYPE)

        identifier("drill_top").block(DRILL_TOP)
        identifier("drill_middle").block(DRILL_MIDDLE)
        identifier("drill_bottom").block(DRILL_BOTTOM).item(BlockItem(DRILL_BOTTOM, itemSettings()))
        identifier("drill").blockEntityType(DRILL_BLOCK_ENTITY_TYPE)

        identifier("tank").block(TANK_BLOCK).item(IRItemRegistry.TANK_BLOCK_ITEM).blockEntityType(TANK_BLOCK_ENTITY)

        identifier("capsule").block(CAPSULE_BLOCK).item(IRItemRegistry.CAPSULE_BLOCK_ITEM).blockEntityType(CAPSULE_BLOCK_ENTITY)

        identifier("fluid_pipe_mk1").block(FLUID_PIPE_MK1)
        identifier("fluid_pipe_mk2").block(FLUID_PIPE_MK2)
        identifier("fluid_pipe_mk3").block(FLUID_PIPE_MK3)
        identifier("fluid_pipe_mk4").block(FLUID_PIPE_MK4)

        identifier("item_pipe_mk1").block(ITEM_PIPE_MK1)
        identifier("item_pipe_mk2").block(ITEM_PIPE_MK2)
        identifier("item_pipe_mk3").block(ITEM_PIPE_MK3)
        identifier("item_pipe_mk4").block(ITEM_PIPE_MK4)

        identifier("cable_mk1").block(CABLE_MK1).blockEntityType(COVERABLE_BLOCK_ENTITY_TYPE_MK1)
        identifier("cable_mk2").block(CABLE_MK2).blockEntityType(COVERABLE_BLOCK_ENTITY_TYPE_MK2)
        identifier("cable_mk3").block(CABLE_MK3).blockEntityType(COVERABLE_BLOCK_ENTITY_TYPE_MK3)
        identifier("cable_mk4").block(CABLE_MK4).blockEntityType(COVERABLE_BLOCK_ENTITY_TYPE_MK4)
    }

    val SULFUR_CRYSTAL_CLUSTER = SulfurCrystalBlock(FabricBlockSettings.of(Material.METAL).sounds(BlockSoundGroup.GLASS).requiresTool().strength(3f, 3f))

    val NIKOLITE_ORE = { Registry.BLOCK.get(identifier("nikolite_ore")) }
    val COPPER_ORE = { Registry.BLOCK.get(identifier("copper_ore")) }
    val TIN_ORE = { Registry.BLOCK.get(identifier("tin_ore")) }
    val LEAD_ORE = { Registry.BLOCK.get(identifier("lead_ore")) }
    val SILVER_ORE = { Registry.BLOCK.get(identifier("silver_ore")) }
    val TUNGSTEN_ORE = { Registry.BLOCK.get(identifier("tungsten_ore")) }

    val MACHINE_BLOCK = Block(
        FabricBlockSettings.of(Material.METAL).requiresTool().breakByTool(FabricToolTags.PICKAXES, 2).strength(3F, 6F)
    )
    val PLANKS = PlankBlock(
        FabricBlockSettings.of(Material.WOOD).breakByTool(FabricToolTags.AXES, 2).strength(3F, 6F)
    )
    val PLANK_BLOCK = Block(
        FabricBlockSettings.of(Material.WOOD).breakByTool(FabricToolTags.AXES, 2).strength(3F, 6F)
    )

    val CONTROLLER =  HorizontalFacingBlock(
        FabricBlockSettings.of(Material.METAL).requiresTool().breakByTool(FabricToolTags.PICKAXES, 2).strength(3F, 6F)
    )
    val DUCT =  DuctBlock(
        FabricBlockSettings.of(Material.METAL).requiresTool().breakByTool(FabricToolTags.PICKAXES, 2).strength(3F, 6F)
    )
    val FRAME = Block(
        FabricBlockSettings.of(Material.METAL).requiresTool().breakByTool(FabricToolTags.PICKAXES, 2).strength(3F, 6F)
    )
    val SILO = Block(
        FabricBlockSettings.of(Material.METAL).requiresTool().breakByTool(FabricToolTags.PICKAXES, 2).strength(3F, 6F)
    )
    val WARNING_STROBE = WarningStrobeBlock(
        FabricBlockSettings.of(Material.METAL).requiresTool().luminance(15).nonOpaque().breakByTool(FabricToolTags.PICKAXES, 2).strength(3F, 6F)
    )
    val INTAKE =  HorizontalFacingBlock(
        FabricBlockSettings.of(Material.METAL).requiresTool().breakByTool(FabricToolTags.PICKAXES, 2).strength(3F, 6F)
    )
    val CABINET = CabinetBlock(
        FabricBlockSettings.of(Material.METAL).requiresTool().breakByTool(FabricToolTags.PICKAXES, 2).strength(3F, 6F)
    )
    val CABINET_BLOCK_ENTITY_TYPE: BlockEntityType<CabinetBlockEntity> = BlockEntityType.Builder.create({ CabinetBlockEntity() }, CABINET).build(null)

    val DRILL_TOP = DrillBlock.TopDrillBlock(
        FabricBlockSettings.of(Material.METAL).requiresTool().nonOpaque().breakByTool(FabricToolTags.PICKAXES, 2).strength(3F, 6F)
    )
    val DRILL_MIDDLE = DrillBlock.MiddleDrillBlock(
        FabricBlockSettings.of(Material.METAL).requiresTool().nonOpaque().breakByTool(FabricToolTags.PICKAXES, 2).strength(3F, 6F)
    )
    val DRILL_BOTTOM = DrillBlock.BottomDrillBlock(
        FabricBlockSettings.of(Material.METAL).requiresTool().nonOpaque().breakByTool(FabricToolTags.PICKAXES, 2).strength(3F, 6F)
    )
    val DRILL_BLOCK_ENTITY_TYPE: BlockEntityType<DrillBlockEntity> = BlockEntityType.Builder.create({ DrillBlockEntity() }, DRILL_BOTTOM).build(null)

    val TANK_BLOCK = TankBlock(FabricBlockSettings.of(Material.GLASS).nonOpaque().strength(1f, 1f))

    val TANK_BLOCK_ENTITY: BlockEntityType<TankBlockEntity> = BlockEntityType.Builder.create({ TankBlockEntity() }, TANK_BLOCK).build(null)

    val CAPSULE_BLOCK = CapsuleBlock()

    val CAPSULE_BLOCK_ENTITY: BlockEntityType<CapsuleBlockEntity> = BlockEntityType.Builder.create({ CapsuleBlockEntity() }, CAPSULE_BLOCK).build(null)

    val FLUID_PIPE_MK1 = FluidPipeBlock(FabricBlockSettings.copyOf(Blocks.IRON_BLOCK), Tier.MK1)
    val FLUID_PIPE_MK2 = FluidPipeBlock(FabricBlockSettings.copyOf(Blocks.IRON_BLOCK), Tier.MK2)
    val FLUID_PIPE_MK3 = FluidPipeBlock(FabricBlockSettings.copyOf(Blocks.IRON_BLOCK), Tier.MK3)
    val FLUID_PIPE_MK4 = FluidPipeBlock(FabricBlockSettings.copyOf(Blocks.IRON_BLOCK), Tier.MK4)

    val ITEM_PIPE_MK1 = ItemPipeBlock(FabricBlockSettings.copyOf(Blocks.IRON_BLOCK), Tier.MK1)
    val ITEM_PIPE_MK2 = ItemPipeBlock(FabricBlockSettings.copyOf(Blocks.IRON_BLOCK), Tier.MK2)
    val ITEM_PIPE_MK3 = ItemPipeBlock(FabricBlockSettings.copyOf(Blocks.IRON_BLOCK), Tier.MK3)
    val ITEM_PIPE_MK4 = ItemPipeBlock(FabricBlockSettings.copyOf(Blocks.IRON_BLOCK), Tier.MK4)

    val CABLE_MK1 = CableBlock(FabricBlockSettings.copyOf(Blocks.IRON_BLOCK), Tier.MK1)
    val CABLE_MK2 = CableBlock(FabricBlockSettings.copyOf(Blocks.IRON_BLOCK), Tier.MK2)
    val CABLE_MK3 = CableBlock(FabricBlockSettings.copyOf(Blocks.IRON_BLOCK), Tier.MK3)
    val CABLE_MK4 = CableBlock(FabricBlockSettings.copyOf(Blocks.IRON_BLOCK), Tier.MK4)

    val COVERABLE_BLOCK_ENTITY_TYPE_MK1 = BlockEntityType.Builder.create({ CoverableBlockEntity(Tier.MK1) }, FLUID_PIPE_MK1, ITEM_PIPE_MK1, CABLE_MK1).build(null)

    val COVERABLE_BLOCK_ENTITY_TYPE_MK2 = BlockEntityType.Builder.create({ CoverableBlockEntity(Tier.MK2) }, FLUID_PIPE_MK2, ITEM_PIPE_MK2, CABLE_MK2).build(null)

    val COVERABLE_BLOCK_ENTITY_TYPE_MK3 = BlockEntityType.Builder.create({ CoverableBlockEntity(Tier.MK3) }, FLUID_PIPE_MK3, ITEM_PIPE_MK3, CABLE_MK3).build(null)

    val COVERABLE_BLOCK_ENTITY_TYPE_MK4 = BlockEntityType.Builder.create({ CoverableBlockEntity(Tier.MK4) }, FLUID_PIPE_MK4, ITEM_PIPE_MK4, CABLE_MK4).build(null)
}