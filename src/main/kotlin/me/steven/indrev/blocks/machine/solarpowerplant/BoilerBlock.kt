package me.steven.indrev.blocks.machine.solarpowerplant

import me.steven.indrev.blockentities.solarpowerplant.BoilerBlockEntity
import me.steven.indrev.blocks.misc.HorizontalFacingBlock
import me.steven.indrev.components.multiblock.definitions.BoilerStructureDefinition
import me.steven.indrev.gui.IRScreenHandlerFactory
import me.steven.indrev.gui.screenhandlers.machines.BoilerScreenHandler
import net.minecraft.block.BlockEntityProvider
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityTicker
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.text.TranslatableText
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class BoilerBlock(settings: Settings) : HorizontalFacingBlock(settings), BlockEntityProvider {

    override fun onStateReplaced(state: BlockState, world: World, pos: BlockPos, newState: BlockState, moved: Boolean) {
        if (!state.isOf(this)) {
            BoilerStructureDefinition.getFluidValvePositions(pos, state).forEach { valvePos ->
                BoilerBlockEntity.FLUID_VALVES_MAPPER.remove(valvePos.asLong())
            }
        }
        super.onStateReplaced(state, world, pos, newState, moved)
    }

    override fun createBlockEntity(pos: BlockPos, state: BlockState): BlockEntity = BoilerBlockEntity(pos, state)

    override fun <T : BlockEntity?> getTicker(
        world: World,
        state: BlockState?,
        type: BlockEntityType<T>?
    ): BlockEntityTicker<T>? {
        return if (world.isClient) null
        else BlockEntityTicker { world, pos, state, blockEntity ->
            BoilerBlockEntity.tick(world, pos, state, blockEntity as? BoilerBlockEntity ?: return@BlockEntityTicker)
        }
    }

    override fun onUse(
        state: BlockState,
        world: World,
        pos: BlockPos?,
        player: PlayerEntity?,
        hand: Hand?,
        hit: BlockHitResult?
    ): ActionResult {
        if (!world.isClient) {
            val blockEntity = world.getBlockEntity(pos) as? BoilerBlockEntity ?: return ActionResult.PASS
            if (!blockEntity.multiblockComponent.isBuilt(world, pos!!, state)) {
                player?.sendMessage(TranslatableText("text.multiblock.not_built"), true)
                blockEntity.multiblockComponent.toggleRender(player!!.isSneaking)
                blockEntity.markDirty()
                blockEntity.sync()
            } else
                player?.openHandledScreen(IRScreenHandlerFactory(::BoilerScreenHandler, pos))
        }
        return ActionResult.success(world.isClient)
    }
}