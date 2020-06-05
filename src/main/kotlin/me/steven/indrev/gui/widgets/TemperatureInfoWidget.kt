package me.steven.indrev.gui.widgets

import io.github.cottonmc.cotton.gui.client.ScreenDrawing
import io.github.cottonmc.cotton.gui.widget.WWidget
import me.steven.indrev.components.TemperatureController
import me.steven.indrev.utils.identifier
import net.minecraft.client.resource.language.I18n
import net.minecraft.container.PropertyDelegate

class TemperatureInfoWidget(private val delegate: PropertyDelegate, private val temperatureController: TemperatureController) : WWidget() {
    init {
        this.setSize(8, 8)
    }

    override fun paintBackground(x: Int, y: Int) {
        val temperature = delegate[2]
        val widgetToDraw = when {
            temperature > temperatureController.optimalRange.last -> HIGH_HEAT
            temperature in temperatureController.optimalRange -> MEDIUM_HEAT
            else -> HEAT_LOW
        }
        ScreenDrawing.texturedRect(x, y, width, height, widgetToDraw, 0f, 0f, 1f, 1f, -1)
    }

    override fun addInformation(information: MutableList<String>?) {
        val temperature = delegate[2]
        val key = when {
            temperature > temperatureController.optimalRange.last -> "gui.widget.temperature_info.high"
            temperature in temperatureController.optimalRange -> "gui.widget.temperature_info.medium"
            else -> "gui.widget.temperature_info.low"
        }
        information?.add(I18n.translate(key))
    }

    companion object {
        private val HEAT_LOW = identifier("textures/gui/widget_heat_info_1.png")
        private val MEDIUM_HEAT = identifier("textures/gui/widget_heat_info_2.png")
        private val HIGH_HEAT = identifier("textures/gui/widget_heat_info_3.png")
    }
}