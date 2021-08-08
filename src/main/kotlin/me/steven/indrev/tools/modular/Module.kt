package me.steven.indrev.tools.modular

import net.minecraft.client.gui.screen.Screen
import net.minecraft.item.ItemConvertible
import net.minecraft.item.ItemStack
import net.minecraft.text.LiteralText
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.Formatting

interface Module {
    val key: String
    val maxLevel: Int
    val item: ItemConvertible

    fun isInstalled(itemStack: ItemStack): Boolean {
        return !itemStack.isEmpty && itemStack.orCreateNbt.contains(key)
    }

    fun getLevel(itemStack: ItemStack): Int {
        if (itemStack.isEmpty) return 0
        val tag = itemStack.getOrCreateSubNbt("selected")
        return if (tag.contains(key)) tag.getInt(key) else getMaxInstalledLevel(itemStack)
    }

    fun getMaxInstalledLevel(itemStack: ItemStack): Int {
        if (itemStack.isEmpty) return 0
        val tag = itemStack.orCreateNbt
        return if (tag.contains(key)) tag.getInt(key) else 0
    }

    fun getTooltip(stack: ItemStack, tooltip: MutableList<Text>?) {
        val titleText = TranslatableText("item.indrev.module_${key}.tooltip")
        tooltip?.add(titleText.formatted(Formatting.BLUE, Formatting.ITALIC))
        tooltip?.add(LiteralText(" "))
        if (Screen.hasShiftDown()) {
            if (this != ArmorModule.COLOR) {
                val maxLevelText = TranslatableText("item.indrev.module_max_level", LiteralText(maxLevel.toString()).formatted(Formatting.GOLD))
                tooltip?.add(maxLevelText.formatted(Formatting.BLUE))
            }
        }
    }
}