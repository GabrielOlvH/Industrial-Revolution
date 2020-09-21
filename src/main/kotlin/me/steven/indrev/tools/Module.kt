package me.steven.indrev.tools

import me.steven.indrev.armor.ArmorModule
import net.minecraft.client.gui.screen.Screen
import net.minecraft.item.ItemStack
import net.minecraft.text.LiteralText
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.Formatting

interface Module {
    val key: String
    val maxLevel: Int
    val isValid: (ItemStack) -> Boolean

    fun isInstalled(itemStack: ItemStack): Boolean {
        val tag = itemStack.orCreateTag
        return tag.contains(key)
    }

    fun getLevel(itemStack: ItemStack): Int {
        val tag = itemStack.orCreateTag
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

    companion object {

        fun getInstalledTooltip(upgrades: Array<Module>, stack: ItemStack, tooltip: MutableList<Text>?) {
            if (upgrades.isNotEmpty()) {
                tooltip?.add(TranslatableText("item.indrev.modular.upgrade").formatted(Formatting.GOLD))
                upgrades.forEach { upgrade ->
                    val text = TranslatableText("item.indrev.modular.upgrade.${upgrade.key}", upgrade.getLevel(stack))
                    tooltip?.add(text.formatted(Formatting.BLUE))
                }
            }
        }
    }
}