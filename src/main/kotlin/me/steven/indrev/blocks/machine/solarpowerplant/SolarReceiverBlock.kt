package me.steven.indrev.blocks.machine.solarpowerplant

import me.steven.indrev.blockentities.solarpowerplant.SolarReceiverBlockEntity
import net.minecraft.block.Block
import net.minecraft.block.BlockEntityProvider
import net.minecraft.block.entity.BlockEntity
import net.minecraft.world.BlockView

class SolarReceiverBlock(settings: Settings) : Block(settings), BlockEntityProvider {
    override fun createBlockEntity(world: BlockView?): BlockEntity = SolarReceiverBlockEntity()
}