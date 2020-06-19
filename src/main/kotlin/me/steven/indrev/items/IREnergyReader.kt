package me.steven.indrev.items

import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.item.Item
import net.minecraft.item.ItemUsageContext
import net.minecraft.text.TranslatableText
import net.minecraft.util.ActionResult
import team.reborn.energy.Energy

class IREnergyReader(settings: Settings) : Item(settings) {
    @Environment(EnvType.CLIENT)
    override fun useOnBlock(context: ItemUsageContext?): ActionResult {
        if (context?.world?.isClient == true) return ActionResult.SUCCESS
        val blockPos = context?.blockPos
        val blockEntity = context?.world?.getBlockEntity(blockPos)
        if (blockEntity != null && Energy.valid(blockEntity)) {
            val energy = Energy.of(blockEntity).energy
            context.player?.sendMessage(TranslatableText("item.indrev.energy_reader.use", energy.toInt()), true)
        }
        return ActionResult.FAIL
    }
}