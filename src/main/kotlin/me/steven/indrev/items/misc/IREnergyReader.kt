package me.steven.indrev.items.misc

import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.blockentities.crafters.UpgradeProvider
import me.steven.indrev.config.BasicMachineConfig
import me.steven.indrev.items.upgrade.Upgrade
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
            val energy = Energy.of(blockEntity).energy.toInt()
            val text = TranslatableText("item.indrev.energy_reader.use")
                .formatted(Formatting.BLUE)
                .append(LiteralText(" $energy").formatted(Formatting.WHITE))
            if (blockEntity is MachineBlockEntity<*>) {
                val energyCost =
                    when {
                        blockEntity is UpgradeProvider -> Upgrade.getEnergyCost(blockEntity.getUpgrades(blockEntity.inventoryComponent!!.inventory), blockEntity)
                        blockEntity.config is BasicMachineConfig -> (blockEntity.config as BasicMachineConfig).energyCost
                        else -> -1.0
                    }
                if (energyCost > 0.0) {
                    val energyCostText = TranslatableText(
                        "item.indrev.energy_reader.use1",
                        LiteralText(energyCost.toString()).formatted(Formatting.WHITE)
                    ).formatted(Formatting.BLUE)
                    text
                        .append(LiteralText(" | ").formatted(Formatting.BLACK, Formatting.BOLD))
                        .append(energyCostText)
                }
            }
            context.player?.sendMessage(text, true)
        }
        return ActionResult.FAIL
    }
}