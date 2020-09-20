package me.steven.indrev.items.armor

import me.steven.indrev.armor.ArmorModule
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.item.TooltipContext
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.text.LiteralText
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.Formatting
import net.minecraft.world.World

open class IRModuleItem(val module: ArmorModule, settings: Settings) : Item(settings) {
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
        tooltip?.add(LiteralText(" "))
        if (Screen.hasShiftDown()) {
            if (module != ArmorModule.COLOR)
                tooltip?.add(
                    TranslatableText(
                        "item.indrev.module_max_level",
                        LiteralText(module.maxLevel.toString()).formatted(Formatting.GOLD)
                    ).formatted(Formatting.BLUE)
                )
            tooltip?.add(
                TranslatableText("item.indrev.module_parts").formatted(Formatting.BLUE)
            )
            module.slots.forEach {
                tooltip?.add(
                    TranslatableText(
                        "item.indrev.module_parts_${it.toString().toLowerCase()}"
                    ).formatted(Formatting.GOLD)
                )
            }
        } else {
            tooltip?.add(
                TranslatableText("gui.indrev.tooltip.press_shift").formatted(
                    Formatting.BLUE,
                    Formatting.ITALIC
                )
            )
        }
    }
}