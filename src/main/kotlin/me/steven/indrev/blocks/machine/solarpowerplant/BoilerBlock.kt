package me.steven.indrev.blocks.machine.solarpowerplant

import me.steven.indrev.blockentities.solarpowerplant.BoilerBlockEntity
import me.steven.indrev.blockentities.solarpowerplant.SolarPowerPlantTowerBlockEntity
import me.steven.indrev.blocks.misc.HorizontalFacingBlock
import me.steven.indrev.gui.IRScreenHandlerFactory
import me.steven.indrev.gui.screenhandlers.machines.BoilerScreenHandler
import net.minecraft.block.BlockEntityProvider
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.text.TranslatableText
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.world.BlockView
import net.minecraft.world.World

class BoilerBlock(settings: Settings) : HorizontalFacingBlock(settings), BlockEntityProvider {

    override fun createBlockEntity(world: BlockView?): BlockEntity = BoilerBlockEntity()

    override fun onUse(
        state: BlockState,
        world: World,
        pos: BlockPos?,
        player: PlayerEntity?,
        hand: Hand?,
        hit: BlockHitResult?
    ): ActionResult {
        if (!world.isClient) {
            val blockEntity = world.getBlockEntity(pos) as? SolarPowerPlantTowerBlockEntity ?: return ActionResult.PASS
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