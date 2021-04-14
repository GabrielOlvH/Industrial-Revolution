package me.steven.indrev.gui.widgets.machines

import io.github.cottonmc.cotton.gui.widget.TooltipBuilder
import io.github.cottonmc.cotton.gui.widget.WBar
import me.steven.indrev.utils.identifier
import net.minecraft.text.LiteralText
import net.minecraft.text.TranslatableText
import net.minecraft.util.Identifier

class WProcessBar(direction: Direction = Direction.RIGHT, bg: Identifier = PROCESS_EMPTY, bar: Identifier = PROCESS_FULL, value: Int = 4, maxValue: Int = 5) : WBar(bg, bar, value, maxValue, direction) {

    override fun addTooltip(information: TooltipBuilder?) {
        val progress = properties[field]
        val max = properties[max]
        if (max <= 0) return
        val percentage = progress * 100 / max
        information?.add(TranslatableText("gui.widget.process", percentage).append(LiteralText("%")))
    }

    companion object {
        val PROCESS_EMPTY =
            identifier("textures/gui/widget_processing_empty.png")
        val PROCESS_FULL =
            identifier("textures/gui/widget_processing_full.png")
    }
}