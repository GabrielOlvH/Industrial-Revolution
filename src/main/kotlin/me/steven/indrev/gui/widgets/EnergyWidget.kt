package me.steven.indrev.gui.widgets

import io.github.cottonmc.cotton.gui.client.ScreenDrawing
import io.github.cottonmc.cotton.gui.widget.WWidget
import me.steven.indrev.utils.identifier
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.screen.PropertyDelegate
import net.minecraft.text.LiteralText
import net.minecraft.text.StringRenderable
import net.minecraft.text.TranslatableText
import kotlin.math.round

class EnergyWidget(private val delegate: PropertyDelegate) : WWidget() {
    init {
        this.setSize(16, 64)
    }

    override fun paint(matrices: MatrixStack?, x: Int, y: Int, mouseX: Int, mouseY: Int) {
        ScreenDrawing.texturedRect(x, y, width, height, ENERGY_EMPTY, -1)
        val energy = delegate[0]
        val maxEnergy = delegate[1]
        if (energy > 0) {
            val v = ((energy.toFloat() * 63 / maxEnergy) + 1) / 64
            val h = round(v * height).toInt()
            ScreenDrawing.texturedRect(x, y + (height - h), width, h, ENERGY_FULL, 0f, 1f - v, 1f, 1f, -1)
        }
    }

    override fun addTooltip(information: MutableList<StringRenderable>?) {
        val energy = delegate[0]
        val maxEnergy = delegate[1]
        information?.add(TranslatableText("gui.widget.energy"))
        information?.add(LiteralText("$energy / $maxEnergy LF"))
        super.addTooltip(information)
    }

    companion object {
        private val ENERGY_EMPTY =
            identifier("textures/gui/widget_energy_empty.png")
        private val ENERGY_FULL =
            identifier("textures/gui/widget_energy_full.png")
    }
}