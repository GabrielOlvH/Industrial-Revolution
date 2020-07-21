package me.steven.indrev.gui.widgets

import io.github.cottonmc.cotton.gui.client.ScreenDrawing
import io.github.cottonmc.cotton.gui.widget.WWidget
import me.steven.indrev.components.TemperatureController
import me.steven.indrev.utils.identifier
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.screen.PropertyDelegate
import net.minecraft.text.LiteralText
import net.minecraft.text.StringRenderable
import net.minecraft.text.TranslatableText
import net.minecraft.util.Formatting

class TemperatureWidget(private val delegate: PropertyDelegate, private val temperatureController: TemperatureController) : WWidget() {
    init {
        this.setSize(16, 64)
    }

    override fun paint(matrices: MatrixStack?, x: Int, y: Int, mouseX: Int, mouseY: Int) {
        ScreenDrawing.texturedRect(x, y, width, height, EMPTY_HEAT, -1)
        val temperature = delegate[2]
        val maxTemperature = temperatureController.explosionLimit.toFloat()
        if (temperature > 0) {
            var percent = temperature.toFloat() / maxTemperature.toFloat()
            percent = (percent * height).toInt() / height.toFloat()
            val barSize = (height * percent).toInt()
            ScreenDrawing.texturedRect(
                x, y + getHeight() - barSize, width, barSize,
                FULL_HEAT, 0f, 1 - percent, 1f, 1f, -1
            )
        }
    }

    override fun addTooltip(information: MutableList<StringRenderable>?) {
        val temperature = delegate[2]
        val maxTemperature = temperatureController.explosionLimit.toInt()
        val info = when {
            temperature > temperatureController.optimalRange.last ->
                TranslatableText("gui.widget.temperature_info.high").formatted(Formatting.DARK_RED, Formatting.ITALIC)
            temperature in temperatureController.optimalRange ->
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