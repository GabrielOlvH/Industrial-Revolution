package me.steven.indrev.transportation.blocks

import me.steven.indrev.api.Tier
import me.steven.indrev.transportation.networks.networkManager
import me.steven.indrev.transportation.networks.types.StoragePipeNetwork
import me.steven.indrev.transportation.packets.CreatePipeBlockEntityPacket
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.minecraft.block.BlockEntityProvider
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityTicker
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.Text
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

abstract class StoragePipeBlock(tier: Tier) : PipeBlock(tier), BlockEntityProvider {
    override fun createBlockEntity(pos: BlockPos, state: BlockState): BlockEntity? = null

    override fun <T : BlockEntity?> getTicker(
        world: World,
        state: BlockState,
        type: BlockEntityType<T>
    ): BlockEntityTicker<T> {
        return BlockEntityTicker { world, pos, state, blockEntity -> tick(world, pos, state, blockEntity as PipeBlockEntity) }
    }

    private fun tick(world: World, pos: BlockPos, state: BlockState, blockEntity: PipeBlockEntity) {
        if (world is ServerWorld) {
            val pipeNetwork = world.networkManager.networksByPos[pos.asLong()] as? StoragePipeNetwork<*> ?: return
            pipeNetwork.tickingPositions.add(pos)
        }
    }

    override fun onUse(
        state: BlockState,
        world: World,
        pos: BlockPos,
        player: PlayerEntity,
        hand: Hand,
        hit: BlockHitResult
    ): ActionResult {
        if (world is ServerWorld) {
            val dir = getSideFromHit(hit.pos, pos) ?: return ActionResult.PASS
            val pipeNetwork = world.networkManager.networksByPos[pos.asLong()] ?: return ActionResult.PASS
            val offset = pos.offset(dir)
            if (pipeNetwork.contains(offset) && pipeNetwork.isValidStorage(world, offset, dir.opposite)) {
                val blockEntity = getOrCreateBlockEntity(world, player as ServerPlayerEntity, pos, state)
                val type = blockEntity.cycle(dir)
                blockEntity.markDirty()
                blockEntity.sync()
                player.sendMessage(Text.literal("Changed mode to $type"), true)
                return ActionResult.SUCCESS
            }
        }
        return ActionResult.PASS
    }

    private fun getOrCreateBlockEntity(world: World, player: ServerPlayerEntity, pos: BlockPos, state: BlockState): PipeBlockEntity {
        var blockEntity = world.getBlockEntity(pos)
        if (blockEntity !is PipeBlockEntity) {
            blockEntity = PipeBlockEntity(pos, state)
            world.addBlockEntity(blockEntity)
            val buf = PacketByteBufs.create()
            buf.writeBlockPos(pos)
            CreatePipeBlockEntityPacket.send(player, buf)
        }
        return blockEntity
    }
}