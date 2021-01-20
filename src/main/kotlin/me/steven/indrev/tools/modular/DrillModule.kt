package me.steven.indrev.tools.modular

import me.steven.indrev.registry.IRItemRegistry
import net.minecraft.item.ItemConvertible
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.Formatting

enum class DrillModule(
    override val key: String,
    override val maxLevel: Int,
    override val item: ItemConvertible
) : Module {
    RANGE("range", 5, IRItemRegistry.RANGE_MODULE_ITEM),
    FORTUNE("fortune", 3, IRItemRegistry.FORTUNE_MODULE_ITEM),
    SILK_TOUCH("silk_touch", 1, IRItemRegistry.SILK_TOUCH_MODULE_ITEM);

    override fun getTooltip(stack: ItemStack, tooltip: MutableList<Text>?) {
        super.getTooltip(stack, tooltip)
        tooltip?.add(TranslatableText("item.indrev.module_parts").formatted(Formatting.BLUE))
        tooltip?.add(TranslatableText("item.indrev.module_parts_drill").formatted(Formatting.GOLD))
    }

    companion object {
        val COMPATIBLE: Array<Module> = arrayOf(RANGE, FORTUNE, SILK_TOUCH, MiningToolModule.EFFICIENCY)
    }
}