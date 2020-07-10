package me.steven.indrev.items.armor

import me.steven.indrev.armor.Module
import net.minecraft.client.item.TooltipContext
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.Formatting
import net.minecraft.world.World

open class IRModuleItem(val module: Module, settings: Settings) : Item(settings) {
    override fun appendTooltip(
        stack: ItemStack?,
        world: World?,
        tooltip: MutableList<Text>?,
        context: TooltipContext?
    ) {
        tooltip?.add(
            TranslatableText("item.indrev.module_${module.key}.tooltip").formatted(
                Formatting.BLUE,
                Formatting.ITALIC
            )
        )
    }
}