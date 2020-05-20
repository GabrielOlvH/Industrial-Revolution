package me.steven.indrev.gui.widgets

import io.github.cottonmc.cotton.gui.client.ScreenDrawing
import io.github.cottonmc.cotton.gui.widget.WWidget
import me.steven.indrev.identifier
import net.minecraft.client.resource.language.I18n
import net.minecraft.container.PropertyDelegate
import java.awt.Color
import kotlin.math.absoluteValue
import kotlin.math.ceil

class EnergyWidget(private val delegate: PropertyDelegate) : WWidget() {
    init {
        this.setSize(16, 64)
    }
    private var lastHeightUpdate = 0
    private var lastVUpdate = 0f
    override fun paintBackground(x: Int, y: Int) {
        ScreenDrawing.texturedRect(x, y, width, height, ENERGY_EMPTY, -1)
        val energy = delegate[0]
        val maxEnergy = delegate[1]
        if (energy > 0) {
            val v = energy.toFloat() / maxEnergy
            val h = ceil(v * height).toInt()
            val diff = h - lastHeightUpdate
            if (diff > 1) {
                lastHeightUpdate = h
                lastVUpdate = 1f - v
            }
            ScreenDrawing.texturedRect(x, y + (height - lastHeightUpdate), width, lastHeightUpdate, ENERGY_FULL, 0f, lastVUpdate, 1f, 1f, -1)
        }
    }

    override fun addInformation(information: MutableList<String>?) {
        val energy = delegate[0]
        val maxEnergy = delegate[1]
        information?.add(I18n.translate("gui.widget.energy", energy, maxEnergy))
        super.addInformation(information)
    }

    companion object {
        private val ENERGY_EMPTY = identifier("textures/gui/widget_energy_empty.png")
        private val ENERGY_FULL = identifier("textures/gui/widget_energy_full.png")
    }
}