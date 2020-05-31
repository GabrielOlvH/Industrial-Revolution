package me.steven.indrev.gui.widgets

import io.github.cottonmc.cotton.gui.client.ScreenDrawing
import io.github.cottonmc.cotton.gui.widget.WWidget
import me.steven.indrev.blockentities.TemperatureController
import me.steven.indrev.utils.identifier
import net.minecraft.client.resource.language.I18n
import net.minecraft.container.PropertyDelegate
import kotlin.math.round

class TemperatureWidget(private val delegate: PropertyDelegate, private val temperatureController: TemperatureController) : WWidget() {
    init {
        this.setSize(16, 64)
    }

    override fun paintBackground(x: Int, y: Int) {
        ScreenDrawing.texturedRect(x, y, width, height, EMPTY_HEAT, -1)
        val temperature = delegate[2]
        val maxTemperature = temperatureController.getLimitTemperature().toFloat()
        if (temperature > 0) {
            val v = ((temperature.toFloat() * 63 / maxTemperature) + 1) / 64
            val h = round(v * height).toInt()
            ScreenDrawing.texturedRect(x, y + (height - h), width, h, FULL_HEAT, 0f, 1f - v, 1f, 1f, -1)
        }
    }

    override fun addInformation(information: MutableList<String>?) {
        val temperature = delegate[2]
        val maxTemperature = temperatureController.getLimitTemperature()
        information?.add(I18n.translate("gui.widget.temperature", temperature, maxTemperature))
        super.addInformation(information)
    }

    companion object {
        private val EMPTY_HEAT =
                identifier("textures/gui/widget_energy_empty.png")
        private val FULL_HEAT =
                identifier("textures/gui/widget_heat_full.png")
    }
}