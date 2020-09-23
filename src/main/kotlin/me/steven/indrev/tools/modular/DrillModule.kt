package me.steven.indrev.tools.modular

import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.Formatting

enum class DrillModule(
    override val key: String,
    override val maxLevel: Int
) : Module {
    RANGE("range", 5),
    FORTUNE("fortune", 3),
    SILK_TOUCH("silk_touch", 1);

    override fun getTooltip(stack: ItemStack, tooltip: MutableList<Text>?) {
        super.getTooltip(stack, tooltip)
        tooltip?.add(TranslatableText("item.indrev.module_parts").formatted(Formatting.BLUE))
        tooltip?.add(TranslatableText("item.indrev.module_parts_drill").formatted(Formatting.GOLD))
    }

    companion object {
        val COMPATIBLE: Array<Module> = arrayOf(RANGE, FORTUNE, SILK_TOUCH, MiningToolModule.EFFICIENCY)
    }
}