package me.steven.indrev.items

import net.minecraft.item.Item
import net.minecraft.item.ItemUsageContext
import net.minecraft.text.LiteralText
import net.minecraft.text.TranslatableText
import net.minecraft.util.ActionResult
import net.minecraft.util.Formatting
import team.reborn.energy.Energy

class IREnergyReader(settings: Settings) : Item(settings) {
    override fun useOnBlock(context: ItemUsageContext?): ActionResult {
        if (context?.world?.isClient == true) return ActionResult.SUCCESS
        val blockPos = context?.blockPos
        val blockEntity = context?.world?.getBlockEntity(blockPos)
        if (blockEntity != null && Energy.valid(blockEntity)) {
            val energy = Energy.of(blockEntity).energy
            context.player?.sendMessage(TranslatableText("item.indrev.energy_reader.use").formatted(Formatting.BLUE).append(LiteralText(" $energy").formatted(Formatting.WHITE)), true)
        }
        return ActionResult.FAIL
    }
}