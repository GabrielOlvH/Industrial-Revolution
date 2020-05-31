package me.steven.indrev.gui.widgets

import io.github.cottonmc.cotton.gui.client.ScreenDrawing
import io.github.cottonmc.cotton.gui.widget.WWidget
import me.steven.indrev.utils.identifier
import net.minecraft.container.PropertyDelegate
import kotlin.math.abs
import kotlin.math.ceil

class FuelWidget(private val propertyDelegate: PropertyDelegate): WWidget() {
    init {
        this.setSize(14, 14)
    }
    private var lastHeightUpdate = 14
    private var lastVUpdate = 0f
    override fun paintBackground(x: Int, y: Int) {
        ScreenDrawing.texturedRect(x, y, width, height, UNLIT_TEXTURE_ID, -1)
        val burnTime = propertyDelegate[3]
        val maxBurnTime = propertyDelegate[4]
        if (burnTime > 0 && maxBurnTime > 0) {
            val v = ((burnTime.toFloat() * 13 / maxBurnTime) + 1) / 14f
            val h = ceil(v * height).toInt()
            val diff = abs(lastHeightUpdate - h)
            if (diff > 1) {
                lastHeightUpdate = h
                lastVUpdate = 1f - v
            }
            ScreenDrawing.texturedRect(x, y + (height - lastHeightUpdate), width, lastHeightUpdate, LIT_TEXTURE_ID, 0f, lastVUpdate, 1f, 1f, -1)
        }
    }

    companion object {
        val LIT_TEXTURE_ID =
            identifier("textures/gui/widget_fuel_burning.png")
        val UNLIT_TEXTURE_ID =
            identifier("textures/gui/widget_fuel_not_burning.png")
    }
}