package me.steven.indrev.items.miningdrill

import me.steven.indrev.tools.Module
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.Formatting

enum class DrillModule(
    override val key: String,
    override val maxLevel: Int,
    override val isValid: (ItemStack) -> Boolean = { itemStack -> itemStack.item is IRMiningDrill && getCount(itemStack) < (itemStack.item as IRMiningDrill).getMaxModules()  }
) : Module {
    RANGE("range", 5),
    SPEED("mining_speed", 5),
    FORTUNE("fortune", 3),
    SILK_TOUCH("silk_touch", 1);

    override fun getTooltip(stack: ItemStack, tooltip: MutableList<Text>?) {
        super.getTooltip(stack, tooltip)
        tooltip?.add(TranslatableText("item.indrev.module_parts").formatted(Formatting.BLUE))
        tooltip?.add(TranslatableText("item.indrev.module_parts_drill").formatted(Formatting.GOLD))
    }

    companion object {
        fun getInstalled(stack: ItemStack): Array<DrillModule> {
            val tag = stack.tag ?: return emptyArray()
            return values().mapNotNull { module ->
                if (tag.contains(module.key)) module
                else null
            }.toTypedArray()
        }
        fun getCount(stack: ItemStack): Int = values().map { module ->
            val tag = stack.orCreateTag
            if (tag.contains(module.key)) tag.getInt(module.key)
            else 0
        }.sum()
    }

}