package me.steven.indrev.items.misc

import io.netty.buffer.Unpooled
import me.steven.indrev.IndustrialRevolution
import me.steven.indrev.gui.controllers.resreport.ResourceReportScreenHandlerFactory
import me.steven.indrev.utils.getChunkPos
import me.steven.indrev.world.chunkveins.WorldChunkVeinData
import net.minecraft.client.item.TooltipContext
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.network.PacketByteBuf
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.LiteralText
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.Formatting
import net.minecraft.util.Hand
import net.minecraft.util.Identifier
import net.minecraft.util.TypedActionResult
import net.minecraft.world.World

class IRResourceReportItem(settings: Settings) : Item(settings) {
    override fun appendTooltip(stack: ItemStack?, world: World?, tooltip: MutableList<Text>?, context: TooltipContext?) {
        val tag = stack?.tag ?: return
        val type = Identifier(tag.getString("VeinIdentifier"))
        val pos = getChunkPos(tag.getString("ChunkPos")) ?: return
        tooltip?.add(TranslatableText("item.indrev.chunk_scanner.tooltip2",
            TranslatableText("vein.${type.namespace}.${type.path}").formatted(Formatting.WHITE)).formatted(Formatting.BLUE))
        tooltip?.add(TranslatableText("item.indrev.chunk_scanner.tooltip3",
            LiteralText("X: ${pos.startX} Z: ${pos.startZ}").formatted(Formatting.WHITE),
            LiteralText("X: ${pos.endX} Z: ${pos.endZ}").formatted(Formatting.WHITE)).formatted(Formatting.BLUE))
        val dim = tag.getString("Dimension")
        tooltip?.add(TranslatableText("item.indrev.chunk_scanner.tooltip4", TranslatableText(dim).formatted(Formatting.WHITE)).formatted(Formatting.BLUE))
    }

    override fun use(world: World?, user: PlayerEntity, hand: Hand?): TypedActionResult<ItemStack> {
        if (world !is ServerWorld) return super.use(world, user, hand)
        val state =
            world.persistentStateManager.getOrCreate(
                { WorldChunkVeinData(WorldChunkVeinData.STATE_OVERWORLD_KEY) },
                WorldChunkVeinData.STATE_OVERWORLD_KEY
            )
        val tag = user.getStackInHand(hand).tag ?: return super.use(world, user, hand)
        val chunkPos = getChunkPos(tag.getString("ChunkPos")) ?: return super.use(world, user, hand)
        val veinData = state.veins[chunkPos]!!
        val buf = PacketByteBuf(Unpooled.buffer())
        buf.writeBlockPos(user.blockPos)
        buf.writeIdentifier(veinData.veinIdentifier)
        buf.writeInt(veinData.explored)
        buf.writeInt(veinData.size)
        user.openHandledScreen(
            ResourceReportScreenHandlerFactory(
                { syncId, inv, _ -> IndustrialRevolution.RESOURCE_REPORT_HANDLER.create(syncId, inv, buf) },
                user.blockPos,
                veinData
            )
        )
        return super.use(world, user, hand)
    }
}