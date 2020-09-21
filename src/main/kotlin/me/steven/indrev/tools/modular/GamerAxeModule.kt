package me.steven.indrev.tools.modular

import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.Formatting

enum class GamerAxeModule(
    override val key: String,
    override val maxLevel: Int
) : Module {
    LOOTING("looting", 3),
    FIRE_ASPECT("fire_aspect", 1),
    SHARPNESS("sharpness", 5),//TODO actually implement
    REACH("reach", 4);//TODO actually implement

    override fun getTooltip(stack: ItemStack, tooltip: MutableList<Text>?) {
        super.getTooltip(stack, tooltip)
        tooltip?.add(TranslatableText("item.indrev.module_parts").formatted(Formatting.BLUE))
        tooltip?.add(TranslatableText("item.indrev.module_parts_gamer_axe").formatted(Formatting.GOLD))
    }

    companion object {
        val COMPATIBLE: Array<Module> = arrayOf(LOOTING, FIRE_ASPECT, SHARPNESS, REACH, MiningToolModule.EFFICIENCY)
        fun getInstalled(stack: ItemStack): Array<Module> {
            val tag = stack.tag ?: return emptyArray()
            return COMPATIBLE.mapNotNull { module ->
                if (tag.contains(module.key)) module
                else null
            }.toTypedArray()
        }
        fun getCount(stack: ItemStack): Int = COMPATIBLE.map { module ->
            val tag = stack.orCreateTag
            if (tag.contains(module.key)) tag.getInt(module.key)
            else 0
        }.sum()
    }
}