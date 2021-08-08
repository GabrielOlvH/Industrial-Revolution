package me.steven.indrev.items.misc

import me.steven.indrev.gui.screenhandlers.resreport.ResourceReportScreenHandler
import me.steven.indrev.gui.screenhandlers.resreport.ResourceReportScreenHandlerFactory
import me.steven.indrev.utils.getChunkPos
import me.steven.indrev.world.chunkveins.ChunkVeinState
import net.minecraft.client.item.TooltipContext
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
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
        val tag = stack?.nbt ?: return
        val type = Identifier(tag.getString("VeinIdentifier"))
        val pos = getChunkPos(tag.getCompound("ChunkPos"))
        tooltip?.add(TranslatableText("item.indrev.chunk_scanner.tooltip2",
            TranslatableText("vein.${type.namespace}.${type.path}").formatted(Formatting.WHITE)).formatted(Formatting.BLUE))
        tooltip?.add(TranslatableText("item.indrev.chunk_scanner.tooltip3",
            LiteralText("X: ${pos.startX} Z: ${pos.startZ}").formatted(Formatting.WHITE),
            LiteralText("X: ${pos.endX} Z: ${pos.endZ}").formatted(Formatting.WHITE)).formatted(Formatting.BLUE))
        val dim = tag.getString("Dimension")
        tooltip?.add(TranslatableText("item.indrev.chunk_scanner.tooltip4", TranslatableText(dim).formatted(Formatting.WHITE)).formatted(Formatting.BLUE))
        tooltip?.add(TranslatableText("item.indrev.chunk_scanner.tooltip5").formatted(Formatting.DARK_GRAY, Formatting.ITALIC))
    }

    override fun use(world: World?, user: PlayerEntity, hand: Hand?): TypedActionResult<ItemStack> {
        if (world !is ServerWorld) return super.use(world, user, hand)
        val state = ChunkVeinState.getState(world)
        val tag = user.getStackInHand(hand).nbt ?: return super.use(world, user, hand)
        val chunkPos = getChunkPos(tag.getCompound("ChunkPos"))
        val veinData = state.veins[chunkPos]!!
        user.openHandledScreen(
            ResourceReportScreenHandlerFactory(
                { syncId, inv, ctx -> ResourceReportScreenHandler(syncId, inv, ctx, veinData) },
                user.blockPos,
                veinData
            )
        )
        return super.use(world, user, hand)
    }
}