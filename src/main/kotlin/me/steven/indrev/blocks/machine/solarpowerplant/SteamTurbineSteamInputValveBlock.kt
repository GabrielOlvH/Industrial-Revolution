package me.steven.indrev.blocks.machine.solarpowerplant

import me.steven.indrev.blockentities.generators.SteamTurbineSteamInputValveBlockEntity
import net.minecraft.block.BlockEntityProvider
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityTicker
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class SteamTurbineSteamInputValveBlock(settings: Settings) : FluidValveBlock(settings), BlockEntityProvider {
    override fun createBlockEntity(pos: BlockPos, state: BlockState): BlockEntity = SteamTurbineSteamInputValveBlockEntity(pos, state)

    override fun <T : BlockEntity?> getTicker(
        world: World,
        state: BlockState?,
        type: BlockEntityType<T>?
    ): BlockEntityTicker<T>? {
        return if (world.isClient) null
        else BlockEntityTicker { _, _, _, blockEntity ->
            (blockEntity as? SteamTurbineSteamInputValveBlockEntity)?.inserted = false
        }
    }
}