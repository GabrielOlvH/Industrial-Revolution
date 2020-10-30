package me.steven.indrev.gui.widgets.machines

import io.github.cottonmc.cotton.gui.widget.TooltipBuilder
import io.github.cottonmc.cotton.gui.widget.WBar
import me.steven.indrev.components.TemperatureComponent
import me.steven.indrev.utils.identifier
import net.minecraft.text.LiteralText
import net.minecraft.text.TranslatableText
import net.minecraft.util.Formatting

class WTemperature(private val temperatureComponent: TemperatureComponent) : WBar(
    EMPTY_HEAT, FULL_HEAT, 2, 3, Direction.UP) {
    init {
        this.setSize(16, 64)
    }

    override fun addTooltip(information: TooltipBuilder?) {
        val temperature = properties[2]
        val maxTemperature = properties[3]
        val info = when {
            temperature > temperatureComponent.optimalRange.last ->
                TranslatableText("gui.widget.temperature_info.high").formatted(Formatting.DARK_RED, Formatting.ITALIC)
            temperature in temperatureComponent.optimalRange ->
                TranslatableText("gui.widget.temperature_info.medium").formatted(Formatting.YELLOW, Formatting.ITALIC)
            else ->
                TranslatableText("gui.widget.temperature_info.low").formatted(Formatting.GREEN, Formatting.ITALIC)
        }
        information?.add(TranslatableText("gui.widget.temperature").formatted(Formatting.BLUE))
        information?.add(LiteralText("$temperature / $maxTemperature ºC"))
        information?.add(info)
    }

    override fun canResize(): Boolean = false

    companion object {
        private val EMPTY_HEAT =
            identifier("textures/gui/widget_energy_empty.png")
        private val FULL_HEAT =
            identifier("textures/gui/widget_heat_full.png")
    }
}