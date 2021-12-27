package me.steven.indrev.components.multiblock.definitions

import me.steven.indrev.blocks.machine.HorizontalFacingMachineBlock
import me.steven.indrev.components.multiblock.AbstractMultiblockMatcher
import me.steven.indrev.components.multiblock.StructureDefinition
import me.steven.indrev.components.multiblock.StructureHelper
import me.steven.indrev.components.multiblock.StructureHolder
import me.steven.indrev.registry.IRBlockRegistry
import net.minecraft.block.BlockState
import net.minecraft.state.property.Properties
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.World

/**
 * Multiple definitions rather than Structure variations were used here to avoid useless block state calls
 */
object FactoryStructureDefinition : StructureDefinition() {

    private val CONTROLLER_STATE = IRBlockRegistry.CONTROLLER.defaultState.with(Properties.HORIZONTAL_FACING, Direction.NORTH)
    private val DUCT_STATE = IRBlockRegistry.DUCT.defaultState.with(Properties.HORIZONTAL_FACING, Direction.WEST)
    private val CABINET_STATE = IRBlockRegistry.CABINET.defaultState.with(Properties.HORIZONTAL_FACING, Direction.WEST)
    private val INTAKE_STATE = IRBlockRegistry.INTAKE.defaultState.with(Properties.HORIZONTAL_FACING, Direction.WEST)

    override val identifier: String = "factory"
    override val isOptional: Boolean = false
    override val holder: StructureHolder =
        StructureHelper(this)
            .cube(BlockPos(-1, 1, 1), 3, 3, 1, IRBlockRegistry.FRAME.defaultState)
            .cube(BlockPos(0, 0, 1), 2, 3, 1, IRBlockRegistry.SILO.defaultState)
            .cube(BlockPos(1, -1, 1), 1, 3, 1, INTAKE_STATE)
            .cube(BlockPos(0, -1, 1), 1, 3, 1, DUCT_STATE)
            .cube(BlockPos(-1, 0, 2), 1, 2, 1, CABINET_STATE)
            .add(BlockPos(-1, 0, 1), CONTROLLER_STATE)
            .add(BlockPos(-1, -1, 1), IRBlockRegistry.WARNING_STROBE.defaultState)
            .create("factory")
            .build()

    object Inverted : StructureDefinition() {

        private val DUCT_STATE_INVERTED = IRBlockRegistry.DUCT.defaultState.with(Properties.HORIZONTAL_FACING, Direction.EAST)
        private val CABINET_STATE_INVERTED = IRBlockRegistry.CABINET.defaultState.with(Properties.HORIZONTAL_FACING, Direction.EAST)
        private val INTAKE_STATE_INVERTED = IRBlockRegistry.INTAKE.defaultState.with(Properties.HORIZONTAL_FACING, Direction.EAST)

        override val identifier: String = "factory_inverted"
        override val isOptional: Boolean = false
        override val holder: StructureHolder =
            StructureHelper(this)
                .cube(BlockPos(-1, 1, 1), 3, 3, 1, IRBlockRegistry.FRAME.defaultState)
                .cube(BlockPos(-1, 0, 1), 2, 3, 1, IRBlockRegistry.SILO.defaultState)
                .cube(BlockPos(-1, -1, 1), 1, 3, 1, INTAKE_STATE_INVERTED)
                .cube(BlockPos(0, -1, 1), 1, 3, 1, DUCT_STATE_INVERTED)
                .cube(BlockPos(1, 0, 2), 1, 2, 1, CABINET_STATE_INVERTED)
                .add(BlockPos(1, 0, 1), CONTROLLER_STATE)
                .add(BlockPos(1, -1, 1), IRBlockRegistry.WARNING_STROBE.defaultState)
                .create("factory")
                .build()
    }

    val SELECTOR: (BlockState, World, BlockPos) -> StructureDefinition = { state, world, pos ->
        val rotation =
            AbstractMultiblockMatcher.rotateBlock(state[HorizontalFacingMachineBlock.HORIZONTAL_FACING].opposite)
        val ductState = world.getBlockState(pos.subtract(BlockPos(0, -1, 1).rotate(rotation)))
        if (!ductState.isOf(IRBlockRegistry.DUCT)) FactoryStructureDefinition
        else {
            val ductFacing = ductState[HorizontalFacingMachineBlock.HORIZONTAL_FACING]
            if (ductFacing == DUCT_STATE.rotate(rotation)[HorizontalFacingMachineBlock.HORIZONTAL_FACING].opposite)
                FactoryStructureDefinition
            else
                Inverted
        }
    }
}