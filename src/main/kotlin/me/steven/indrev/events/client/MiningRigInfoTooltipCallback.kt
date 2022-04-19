package me.steven.indrev.events.client

import me.steven.indrev.api.OreDataCards
import me.steven.indrev.blockentities.miningrig.DataCardWriterBlockEntity
import me.steven.indrev.gui.screenhandlers.machines.DataCardWriterScreenHandler
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback
import net.minecraft.client.MinecraftClient
import net.minecraft.client.item.TooltipContext
import net.minecraft.item.ItemStack
import net.minecraft.text.LiteralText
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.Formatting

object MiningRigInfoTooltipCallback : ItemTooltipCallback {
    override fun getTooltip(stack: ItemStack, ctx: TooltipContext, lines: MutableList<Text>) {
        val handler = MinecraftClient.getInstance().player?.currentScreenHandler
        if (handler is DataCardWriterScreenHandler) {

            val data: OreDataCards.Data = handler.ctx.get { world, pos ->
                val blockEntity = world.getBlockEntity(pos) as? DataCardWriterBlockEntity ?: return@get OreDataCards.INVALID_DATA
                val cardStack = blockEntity.inventoryComponent!!.inventory.getStack(0)
                OreDataCards.readNbt(cardStack) ?: OreDataCards.INVALID_DATA
            }.orElse(OreDataCards.INVALID_DATA)

            val modifier = OreDataCards.Modifier.byItem(stack.item)
            var remainingLevels = 0
            var level = 0
            when (modifier) {
                OreDataCards.Modifier.RICHNESS -> {
                    level = stack.count / 16
                    remainingLevels = 40 - (data.modifiersUsed[modifier] ?: 0)
                }
                OreDataCards.Modifier.SPEED, OreDataCards.Modifier.SIZE -> {
                    level = stack.count / 64
                    remainingLevels = 1
                }
                OreDataCards.Modifier.RNG -> {}
                else -> return
            }

            val index = lines.size - if (ctx.isAdvanced) 1 else 0

            val modifierName = TranslatableText(modifier.translationKey)
            if (remainingLevels <= 0)
                lines.add(index, LiteralText("Cannot increase ").append(modifierName).append(" level anymore").formatted(
                    Formatting.RED))
            else if (level > 0)
                lines.add(index, LiteralText("+$level ").append(modifierName).append(" modifiers").formatted(Formatting.GREEN))
            else
                lines.add(index, LiteralText("Not enough to increase ").append(modifierName).formatted(Formatting.RED))
        }
    }
}