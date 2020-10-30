package me.steven.indrev.tools.modular

import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.Formatting

enum class MiningToolModule(
    override val key: String,
    override val maxLevel: Int
) : Module {
    EFFICIENCY("efficiency", 5);

    override fun getTooltip(stack: ItemStack, tooltip: MutableList<Text>?) {
        super.getTooltip(stack, tooltip)
        tooltip?.add(TranslatableText("item.indrev.module_parts").formatted(Formatting.BLUE))
        tooltip?.add(TranslatableText("item.indrev.module_parts_drill").formatted(Formatting.GOLD))
        tooltip?.add(TranslatableText("item.indrev.module_parts_gamer_axe").formatted(Formatting.GOLD))
    }
}