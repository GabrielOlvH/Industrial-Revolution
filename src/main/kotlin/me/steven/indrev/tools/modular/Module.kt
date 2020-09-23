package me.steven.indrev.tools.modular

import net.minecraft.client.gui.screen.Screen
import net.minecraft.item.ItemStack
import net.minecraft.text.LiteralText
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.Formatting

interface Module {
    val key: String
    val maxLevel: Int

    fun isInstalled(itemStack: ItemStack): Boolean {
        return itemStack.orCreateTag.contains(key)
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
        @Suppress("UNCHECKED_CAST")
        inline fun <reified T : Module> getInstalled(stack: ItemStack): Array<T> {
            val tag = stack.tag ?: return emptyArray()
            val item = stack.item as? IRModularItem ?: return emptyArray()
            return item.getCompatibleModules(stack).filter { module -> module != ArmorModule.COLOR }.mapNotNull { module ->
                if (tag.contains(module.key)) module
                else null
            }.toTypedArray() as Array<T>
        }
        fun getInstalledTooltip(upgrades: Array<Module>, stack: ItemStack, tooltip: MutableList<Text>?) {
            if (upgrades.isNotEmpty()) {
                tooltip?.add(TranslatableText("item.indrev.modular.upgrade").formatted(Formatting.GOLD))
                upgrades.forEach { upgrade ->
                    val text = TranslatableText("item.indrev.modular.upgrade.${upgrade.key}", upgrade.getLevel(stack))
                    tooltip?.add(text.formatted(Formatting.BLUE))
                }
            }
        }
        fun getCount(stack: ItemStack): Int {
            val item = stack.item as? IRModularItem ?: return 0
            return item.getCompatibleModules(stack).map { module ->
                val tag = stack.orCreateTag
                if (tag.contains(module.key)) tag.getInt(module.key)
                else 0
            }.sum()
        }
    }
}