package me.steven.indrev.blocks.misc

import me.steven.indrev.blockentities.storage.CabinetBlockEntity
import net.minecraft.block.BlockEntityProvider
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.world.BlockView
import net.minecraft.world.World

class CabinetBlock(settings: Settings) : HorizontalFacingBlock(settings), BlockEntityProvider {
    override fun createBlockEntity(world: BlockView?): BlockEntity = CabinetBlockEntity()
    override fun onUse(
        state: BlockState?,
        world: World,
        pos: BlockPos?,
        player: PlayerEntity,
        hand: Hand?,
        hit: BlockHitResult?
    ): ActionResult {
        if (!world.isClient) {
            val blockEntity = world.getBlockEntity(pos) as? CabinetBlockEntity ?: return ActionResult.PASS
            player.openHandledScreen(blockEntity)
        }
        return ActionResult.success(world.isClient)
    }
}