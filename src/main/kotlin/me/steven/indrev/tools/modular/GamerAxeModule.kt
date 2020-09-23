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
    SHARPNESS("sharpness", 5),
    REACH("reach", 4);

    override fun getTooltip(stack: ItemStack, tooltip: MutableList<Text>?) {
        super.getTooltip(stack, tooltip)
        tooltip?.add(TranslatableText("item.indrev.module_parts").formatted(Formatting.BLUE))
        tooltip?.add(TranslatableText("item.indrev.module_parts_gamer_axe").formatted(Formatting.GOLD))
    }

    companion object {
        val COMPATIBLE: Array<Module> = arrayOf(LOOTING, FIRE_ASPECT, SHARPNESS, REACH, MiningToolModule.EFFICIENCY)
    }
}