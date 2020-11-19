package me.steven.indrev.components.multiblock

import me.steven.indrev.blocks.machine.HorizontalFacingMachineBlock
import me.steven.indrev.registry.IRRegistry
import net.minecraft.block.BlockState
import net.minecraft.state.property.Properties
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.World

object FactoryStructureDefinition : StructureDefinition {

    private val CONTROLLER_STATE = IRRegistry.CONTROLLER.defaultState.with(Properties.HORIZONTAL_FACING, Direction.NORTH)
    private val DUCT_STATE = IRRegistry.DUCT.defaultState.with(Properties.HORIZONTAL_FACING, Direction.WEST)
    private val CABINET_STATE = IRRegistry.CABINET.defaultState.with(Properties.HORIZONTAL_FACING, Direction.WEST)
    private val INTAKE_STATE = IRRegistry.INTAKE.defaultState.with(Properties.HORIZONTAL_FACING, Direction.WEST)

    override val identifier: String = "factory"
    override val isOptional: Boolean = false
    override val structure: Map<BlockPos, BlockStateFilter> =
        StructureHelper()
            .cube(BlockPos(-1, 1, 1), 3, 3, 1, IRRegistry.FRAME.defaultState)
            .cube(BlockPos(0, 0, 1), 2, 3, 1, IRRegistry.SILO.defaultState)
            .cube(BlockPos(1, -1, 1), 1, 3, 1, INTAKE_STATE)
            .cube(BlockPos(0, -1, 1), 1, 3, 1, DUCT_STATE)
            .cube(BlockPos(-1, 0, 2), 1, 2, 1, CABINET_STATE)
            .add(BlockPos(-1, 0, 1), CONTROLLER_STATE)
            .add(BlockPos(-1, -1, 1), IRRegistry.WARNING_STROBE.defaultState)
            .create()

    object Inverted : StructureDefinition {

        private val DUCT_STATE_INVERTED = IRRegistry.DUCT.defaultState.with(Properties.HORIZONTAL_FACING, Direction.EAST)
        private val CABINET_STATE_INVERTED = IRRegistry.CABINET.defaultState.with(Properties.HORIZONTAL_FACING, Direction.EAST)
        private val INTAKE_STATE_INVERTED = IRRegistry.INTAKE.defaultState.with(Properties.HORIZONTAL_FACING, Direction.EAST)

        override val identifier: String = "factory_inverted"
        override val isOptional: Boolean = false
        override val structure: Map<BlockPos, BlockStateFilter> =
            StructureHelper()
                .cube(BlockPos(-1, 1, 1), 3, 3, 1, IRRegistry.FRAME.defaultState)
                .cube(BlockPos(-1, 0, 1), 2, 3, 1, IRRegistry.SILO.defaultState)
                .cube(BlockPos(-1, -1, 1), 1, 3, 1, INTAKE_STATE_INVERTED)
                .cube(BlockPos(0, -1, 1), 1, 3, 1, DUCT_STATE_INVERTED)
                .cube(BlockPos(1, 0, 2), 1, 2, 1, CABINET_STATE_INVERTED)
                .add(BlockPos(1, 0, 1), CONTROLLER_STATE)
                .add(BlockPos(1, -1, 1), IRRegistry.WARNING_STROBE.defaultState)
                .create()
    }

    val SELECTOR: (BlockState, World, BlockPos) -> StructureDefinition = { state, world, pos ->
        val rotation = AbstractMultiblockMatcher.rotateBlock(state[HorizontalFacingMachineBlock.HORIZONTAL_FACING].opposite)
        val ductState = world.getBlockState(pos.subtract(BlockPos(0, -1, 1).rotate(rotation)))
        if (!ductState.isOf(IRRegistry.DUCT)) FactoryStructureDefinition
        else {
            val ductFacing = ductState[HorizontalFacingMachineBlock.HORIZONTAL_FACING]
            if (ductFacing == DUCT_STATE.rotate(rotation)[HorizontalFacingMachineBlock.HORIZONTAL_FACING].opposite)
                FactoryStructureDefinition
            else
                Inverted
        }
    }
}