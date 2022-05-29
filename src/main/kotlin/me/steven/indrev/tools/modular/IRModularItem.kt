package me.steven.indrev.tools.modular

import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import me.steven.indrev.utils.translatable
import net.minecraft.util.Formatting

interface IRModularItem<T : Module> {
    fun getCompatibleModules(itemStack: ItemStack): Array<T>

    fun getInstalled(stack: ItemStack): List<T> {
        val tag = stack.nbt ?: return emptyList()
        return getCompatibleModules(stack).mapNotNull { module ->
            if (tag.contains(module.key)) module
            else null
        }
    }

    fun getInstalledTooltip(upgrades: List<Module>, stack: ItemStack, tooltip: MutableList<Text>?) {
        if (upgrades.isNotEmpty()) {
            tooltip?.add(translatable("item.indrev.modular.upgrade").formatted(Formatting.GOLD))
            upgrades.forEach { upgrade ->
                val level = upgrade.getLevel(stack)
                val text = translatable("item.indrev.modular.upgrade.${upgrade.key}", level)
                if (upgrade.getMaxInstalledLevel(stack) != level)
                    text.formatted(Formatting.ITALIC)
                tooltip?.add(text.formatted(Formatting.BLUE))
            }
        }
    }
    fun getCount(stack: ItemStack): Int {
        return getCompatibleModules(stack).map { module ->
            val tag = stack.nbt ?: return@map 0
            if (tag.contains(module.key)) tag.getInt(module.key)
            else 0
        }.sum()
    }
}