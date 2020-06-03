package me.steven.indrev.items

import me.steven.indrev.world.ChunkVeinData
import me.steven.indrev.world.ChunkVeinType
import me.steven.indrev.world.WorldChunkVeinData
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.TranslatableText
import net.minecraft.util.Hand
import net.minecraft.util.TypedActionResult
import net.minecraft.util.UseAction
import net.minecraft.world.World
import kotlin.random.asKotlinRandom

class ChunkScannerItem(settings: Settings) : Item(settings){

    override fun finishUsing(stack: ItemStack, world: World?, user: LivingEntity?): ItemStack {
        if (world == null) return stack
        val rnd = world.random.asKotlinRandom()
        if (!world.isClient) {
            val chunkPos = world.getChunk(user?.blockPos)?.pos
            if (chunkPos != null) {
                val state =
                    (world as ServerWorld).persistentStateManager.getOrCreate(
                        { WorldChunkVeinData() },
                        WorldChunkVeinData.STATE_KEY
                    )
                val type = ChunkVeinType
                    .values()
                    .random(rnd)
                val data = ChunkVeinData(type, type.sizeRange.random(rnd))
                state.veins[chunkPos] = data
                state.markDirty()

                user?.sendMessage(TranslatableText("item.indrev.chunk_scanner.scanned1"))
                user?.sendMessage(TranslatableText("item.indrev.chunk_scanner.scanned2", data.chunkVeinType))
                return stack.also { it.decrement(1) }
            }
        }
        return stack
    }

    override fun use(world: World?, user: PlayerEntity, hand: Hand?): TypedActionResult<ItemStack?>? {
        if (world?.isClient == false) {
            val chunkPos = world.getChunk(user.blockPos)?.pos
            if (chunkPos != null) {
                val state =
                    (world as ServerWorld).persistentStateManager.getOrCreate(
                        { WorldChunkVeinData() },
                        WorldChunkVeinData.STATE_KEY
                    )
                val data = state.veins[chunkPos]
                if (data?.chunkVeinType != null) {
                    user.sendMessage(TranslatableText("item.indrev.chunk_scanner.already_scanned", data.chunkVeinType))
                    return TypedActionResult.fail(user.getStackInHand(hand))
                }
            }
        } else
            user.sendMessage(TranslatableText("item.indrev.chunk_scanner.scanning"))
        user.setCurrentHand(hand)
        return TypedActionResult.consume(user.getStackInHand(hand))
    }

    override fun getMaxUseTime(stack: ItemStack?): Int = 200

    override fun getUseAction(stack: ItemStack?): UseAction = UseAction.BOW
}