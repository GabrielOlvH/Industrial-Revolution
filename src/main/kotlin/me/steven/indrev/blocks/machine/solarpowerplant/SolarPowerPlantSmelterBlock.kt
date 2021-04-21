package me.steven.indrev.blocks.machine.solarpowerplant

import me.steven.indrev.blockentities.solarpowerplant.SolarPowerPlantSmelterBlockEntity
import me.steven.indrev.gui.IRScreenHandlerFactory
import me.steven.indrev.gui.screenhandlers.machines.SolarPowerPlantSmelterScreenHandler
import net.minecraft.block.Block
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

class SolarPowerPlantSmelterBlock(settings: Settings) : Block(settings), BlockEntityProvider {
    override fun createBlockEntity(world: BlockView?): BlockEntity = SolarPowerPlantSmelterBlockEntity()

    override fun onUse(
        state: BlockState?,
        world: World,
        pos: BlockPos?,
        player: PlayerEntity?,
        hand: Hand?,
        hit: BlockHitResult?
    ): ActionResult {
        if (!world.isClient) {
            player?.openHandledScreen(IRScreenHandlerFactory(::SolarPowerPlantSmelterScreenHandler, pos!!))
        }
        return ActionResult.success(world.isClient)
    }
}