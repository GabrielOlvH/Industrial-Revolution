package me.steven.indrev.items.enhancer

import me.steven.indrev.blockentities.crafters.EnhancerProvider
import me.steven.indrev.gui.IRInventoryScreen
import me.steven.indrev.gui.screenhandlers.IRGuiScreenHandler
import net.minecraft.client.MinecraftClient
import net.minecraft.client.item.TooltipContext
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.text.LiteralText
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.Formatting
import net.minecraft.world.World

class IREnhancerItem(settings: Settings, val enhancer: Enhancer) : Item(settings) {
    override fun appendTooltip(stack: ItemStack?, world: World?, tooltip: MutableList<Text>?, context: TooltipContext?) {
        tooltip?.add(TranslatableText("item.indrev.${enhancer.toString().toLowerCase()}_enhancer.tooltip").formatted(Formatting.GREEN))
        tooltip?.add(LiteralText.EMPTY)
        val currentScreen = MinecraftClient.getInstance().currentScreen
        if (currentScreen is IRInventoryScreen<*>) {
            val handler = currentScreen.screenHandler as? IRGuiScreenHandler ?: return
            handler.ctx.run { _, pos ->
                val blockEntity = world?.getBlockEntity(pos) as? EnhancerProvider? ?: return@run
                if (!blockEntity.availableEnhancers.contains(enhancer))
                    tooltip?.add(TranslatableText("item.indrev.enhancers.incompatible").formatted(Formatting.DARK_RED))
                else
                    tooltip?.add(TranslatableText("item.indrev.enhancers.count", blockEntity.getMaxEnhancer(enhancer)).formatted(Formatting.AQUA))
            }
        }
    }
}