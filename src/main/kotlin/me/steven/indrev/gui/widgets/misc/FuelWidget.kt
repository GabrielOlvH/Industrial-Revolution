package me.steven.indrev.gui.widgets.misc

import io.github.cottonmc.cotton.gui.client.ScreenDrawing
import io.github.cottonmc.cotton.gui.widget.WWidget
import me.steven.indrev.utils.identifier
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.screen.PropertyDelegate

class FuelWidget(private val propertyDelegate: PropertyDelegate) : WWidget() {
    init {
        this.setSize(14, 14)
    }

    override fun paint(matrices: MatrixStack?, x: Int, y: Int, mouseX: Int, mouseY: Int) {
        ScreenDrawing.texturedRect(x, y, width, height, UNLIT_TEXTURE_ID, -1)
        val burnTime = propertyDelegate[3]
        val maxBurnTime = propertyDelegate[4]
        if (burnTime > 0 && maxBurnTime > 0) {
            var percent = burnTime.toFloat() / maxBurnTime.toFloat()
            percent = (percent * height).toInt() / height.toFloat()
            val barSize = (height * percent).toInt()
            if (barSize > 0)
                ScreenDrawing.texturedRect(
                    x, y + getHeight() - barSize, width, barSize,
                    LIT_TEXTURE_ID, 0f, 1 - percent, 1f, 1f, -1)
        }
    }

    companion object {
        val LIT_TEXTURE_ID =
            identifier("textures/gui/widget_fuel_burning.png")
        val UNLIT_TEXTURE_ID =
            identifier("textures/gui/widget_fuel_not_burning.png")
    }
}