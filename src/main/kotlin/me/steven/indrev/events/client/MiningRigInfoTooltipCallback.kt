package me.steven.indrev.events.client

import me.steven.indrev.api.OreDataCards
import me.steven.indrev.blockentities.miningrig.DataCardWriterBlockEntity
import me.steven.indrev.gui.screenhandlers.machines.DataCardWriterScreenHandler
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback
import net.minecraft.client.MinecraftClient
import net.minecraft.client.item.TooltipContext
import net.minecraft.item.ItemStack
import me.steven.indrev.utils.literal
import net.minecraft.text.Text
import me.steven.indrev.utils.translatable
import net.minecraft.util.Formatting

object MiningRigInfoTooltipCallback : ItemTooltipCallback {
    override fun getTooltip(stack: ItemStack, ctx: TooltipContext, lines: MutableList<Text>) {
        val handler = MinecraftClient.getInstance().player?.currentScreenHandler
        if (handler is DataCardWriterScreenHandler) {

            val index = lines.size - if (ctx.isAdvanced) 1 else 0

            handler.ctx.run { world, pos ->
                val blockEntity = world.getBlockEntity(pos) as? DataCardWriterBlockEntity ?: return@run
                DataCardWriterBlockEntity.ORES_SLOTS.forEach { slot ->
                    val oreStack = blockEntity.inventoryComponent!!.inventory.getStack(slot)
                    if (oreStack.equals(stack) && stack.count < 64) {
                        lines.add(index, literal("Missing ${64-stack.count} blocks.").formatted(Formatting.RED))
                        lines.add(index, literal("Not enough blocks to collect data.").formatted(Formatting.RED))
                    }
                }
            }

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


            val modifierName = translatable(modifier.translationKey)
            if (remainingLevels <= 0)
                lines.add(index, literal("Cannot increase ").append(modifierName).append(" level anymore").formatted(
                    Formatting.RED))
            else if (level > 0)
                lines.add(index, literal("+$level ").append(modifierName).append(" modifiers").formatted(Formatting.GREEN))
            else
                lines.add(index, literal("Not enough to increase ").append(modifierName).formatted(Formatting.RED))
        }
    }
}