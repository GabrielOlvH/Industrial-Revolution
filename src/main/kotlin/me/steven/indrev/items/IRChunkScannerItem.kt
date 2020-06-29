package me.steven.indrev.items

import me.steven.indrev.utils.asString
import me.steven.indrev.utils.getChunkPos
import me.steven.indrev.world.chunkveins.ChunkVeinData
import me.steven.indrev.world.chunkveins.ChunkVeinType
import me.steven.indrev.world.chunkveins.WorldChunkVeinData
import net.minecraft.client.item.TooltipContext
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.text.LiteralText
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.Formatting
import net.minecraft.util.Hand
import net.minecraft.util.TypedActionResult
import net.minecraft.util.UseAction
import net.minecraft.world.World
import kotlin.random.asKotlinRandom

class IRChunkScannerItem(settings: Settings) : Item(settings) {
    override fun appendTooltip(stack: ItemStack?, world: World?, tooltip: MutableList<Text>?, context: TooltipContext?) {
        super.appendTooltip(stack, world, tooltip, context)
        val tag = stack?.tag
        if (tag == null) {
            tooltip?.add(TranslatableText("item.indrev.chunk_scanner.tooltip1").formatted(Formatting.BLUE, Formatting.ITALIC))
            return
        }
        val type = ChunkVeinType.valueOf(tag.getString("ChunkVeinType"))
        val pos = getChunkPos(tag.getString("ChunkPos"))
        tooltip?.add(TranslatableText("item.indrev.chunk_scanner.tooltip2",
            LiteralText(type.toString()).formatted(Formatting.WHITE)).formatted(Formatting.BLUE))
        tooltip?.add(TranslatableText("item.indrev.chunk_scanner.tooltip3",
            LiteralText("X: ${pos.startX} Z: ${pos.startZ}").formatted(Formatting.WHITE),
            LiteralText("X: ${pos.endX} Z: ${pos.endZ}").formatted(Formatting.WHITE)).formatted(Formatting.BLUE))
        // COMPATIBILITY WITH PREVIOUSLY SCANNED CHUNKS
        if (tag.contains("Dimension")) {
            val dim = tag.getString("Dimension")
            tooltip?.add(TranslatableText("item.indrev.chunk_scanner.tooltip4", TranslatableText(dim).formatted(Formatting.WHITE)).formatted(Formatting.BLUE))
        }
    }

    override fun finishUsing(stack: ItemStack, world: World?, user: LivingEntity?): ItemStack {
        if (world?.isClient == false) {
            val rnd = world.random.asKotlinRandom()
            val chunkPos = world.getChunk(user?.blockPos)?.pos
            if (chunkPos != null) {
                val state =
                    (world as ServerWorld).persistentStateManager.getOrCreate(
                        { WorldChunkVeinData(WorldChunkVeinData.STATE_OVERWORLD_KEY) },
                        WorldChunkVeinData.STATE_OVERWORLD_KEY
                    )
                val type = ChunkVeinType
                    .values()
                    .filter { it.dimension == world.registryKey }
                    .random(rnd)
                val data = ChunkVeinData(type, type.sizeRange.random(rnd))
                state.veins[chunkPos] = data
                state.markDirty()
                val tag = CompoundTag()
                tag.putString("ChunkVeinType", type.toString())
                tag.putString("ChunkPos", chunkPos.asString())
                tag.putString("Dimension", world.registryKey.value.path)
                stack.tag = tag

                if (user is PlayerEntity) {
                    user.sendMessage(TranslatableText("item.indrev.chunk_scanner.scanned1"), true)
                    user.sendMessage(TranslatableText("item.indrev.chunk_scanner.scanned2", data.chunkVeinType), true)
                    world.playSound(user.x, user.y, user.z, SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 1.0f, 1.0f, false)
                }
                return stack
            }
        }
        return stack
    }

    override fun use(world: World?, user: PlayerEntity, hand: Hand?): TypedActionResult<ItemStack?>? {
        val stack = user.getStackInHand(hand)
        if (world?.isClient == false) {
            val chunkPos = world.getChunk(user.blockPos)?.pos
            if (chunkPos != null) {
                val state =
                    (world as ServerWorld).persistentStateManager.getOrCreate(
                        { WorldChunkVeinData(WorldChunkVeinData.STATE_OVERWORLD_KEY) },
                        WorldChunkVeinData.STATE_OVERWORLD_KEY
                    )
                val data = state.veins[chunkPos]
                if (data?.chunkVeinType != null) {
                    val tag = CompoundTag()
                    tag.putString("ChunkVeinType", data.chunkVeinType.toString())
                    tag.putString("ChunkPos", chunkPos.asString())
                    tag.putString("Dimension", world.registryKey.value.path)
                    stack.tag = tag
                    user.sendMessage(
                        TranslatableText("item.indrev.chunk_scanner.already_scanned", data.chunkVeinType),
                        true
                    )
                    return TypedActionResult.fail(stack)
                }
            }
            if (stack.tag != null) return TypedActionResult.pass(stack)
            user.sendMessage(TranslatableText("item.indrev.chunk_scanner.scanning"), true)
            user.setCurrentHand(hand)
        }
        return TypedActionResult.consume(stack)
    }

    override fun getMaxUseTime(stack: ItemStack?): Int = 200

    override fun getUseAction(stack: ItemStack?): UseAction = if (stack?.tag == null) UseAction.BOW else UseAction.NONE
}