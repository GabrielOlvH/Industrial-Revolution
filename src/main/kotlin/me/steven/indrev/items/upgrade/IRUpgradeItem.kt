package me.steven.indrev.items.upgrade

import me.steven.indrev.blockentities.crafters.UpgradeProvider
import me.steven.indrev.gui.IRInventoryScreen
import me.steven.indrev.gui.controllers.IRGuiController
import net.minecraft.client.MinecraftClient
import net.minecraft.client.item.TooltipContext
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.Formatting
import net.minecraft.world.World

class IRUpgradeItem(settings: Settings, val upgrade: Upgrade) : Item(settings) {
    override fun appendTooltip(stack: ItemStack?, world: World?, tooltip: MutableList<Text>?, context: TooltipContext?) {
        tooltip?.add(TranslatableText("item.indrev.${upgrade.toString().toLowerCase()}_upgrade.tooltip").formatted(Formatting.GREEN))
        val currentScreen = MinecraftClient.getInstance().currentScreen
        if (currentScreen is IRInventoryScreen<*>) {
            val handler = currentScreen.screenHandler as? IRGuiController ?: return
            handler.ctx.run { _, pos ->
                val blockEntity = world?.getBlockEntity(pos) as? UpgradeProvider? ?: return@run
                if (!blockEntity.getAvailableUpgrades().contains(upgrade))
                    tooltip?.add(TranslatableText("item.indrev.upgrades.incompatible").formatted(Formatting.DARK_RED))
            }
        }
    }
}