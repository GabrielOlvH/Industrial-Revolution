package me.steven.indrev.utils

import me.steven.indrev.api.sideconfigs.ConfigurationType
import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.blocks.machine.MachineBlock
import me.steven.indrev.gui.IRScreenHandlerFactory
import me.steven.indrev.gui.screenhandlers.wrench.ScrewdriverScreenHandler
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.util.ActionResult
import net.minecraft.util.BlockRotation
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

fun wrench(
    world: World,
    pos: BlockPos,
    blockState: BlockState,
    blockEntity: BlockEntity?,
    player: PlayerEntity?,
    stack: ItemStack
): ActionResult {
    val block = blockState.block
    if (player?.isSneaking == true && block is MachineBlock) {
        block.writeNbtComponents(world, player, pos, blockState, blockEntity, stack)
        world.breakBlock(pos, false, player)
    } else {
        val rotated = blockState.rotate(BlockRotation.CLOCKWISE_90)
        if (rotated == blockState) return ActionResult.PASS
        world.setBlockState(pos, rotated)
    }
    return ActionResult.success(world.isClient)
}

fun screwdriver(
    world: World,
    pos: BlockPos,
    blockEntity: BlockEntity?,
    player: PlayerEntity?
): ActionResult {
    if (blockEntity is MachineBlockEntity<*>) {
        if (ConfigurationType.getTypes(blockEntity).isNotEmpty()) {
            player?.openHandledScreen(IRScreenHandlerFactory(::ScrewdriverScreenHandler, pos))
            return ActionResult.success(world.isClient)
        }
    }
    return ActionResult.PASS
}