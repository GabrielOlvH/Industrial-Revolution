package me.steven.indrev.gui.widgets

import io.github.cottonmc.cotton.gui.client.BackgroundPainter
import io.github.cottonmc.cotton.gui.client.ScreenDrawing
import io.github.cottonmc.cotton.gui.widget.WWidget
import me.steven.indrev.blocks.CoalGeneratorBlockEntity
import me.steven.indrev.identifier
import net.minecraft.container.BlockContext
import net.minecraft.container.PropertyDelegate
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import java.awt.Color
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max

class FuelWidget(private val propertyDelegate: PropertyDelegate): WWidget() {
    init {
        this.setSize(14, 14)
    }
    private var lastHeightUpdate = 14
    private var lastVUpdate = 0f
    override fun paintBackground(x: Int, y: Int) {
        ScreenDrawing.texturedRect(x, y, width, height, UNLIT_TEXTURE_ID, -1)
        val burnTime = propertyDelegate.get(0)
        val maxBurnTime = propertyDelegate.get(1)
        if (burnTime > 0 && maxBurnTime > 0) {
            val v = ((burnTime.toFloat() * 13 / maxBurnTime) + 1) / 14f
            val h = ceil(v * height).toInt()
            if (abs(lastHeightUpdate - h) > 1) {
                lastHeightUpdate = h
                lastVUpdate = 1f - v
            }
            ScreenDrawing.texturedRect(x, y + (height - lastHeightUpdate), width, lastHeightUpdate, LIT_TEXTURE_ID, 0f, lastVUpdate, 1f, 1f, -1)
        }
    }

    companion object {
        val LIT_TEXTURE_ID = identifier("textures/gui/burning_icon.png")
        val UNLIT_TEXTURE_ID = identifier("textures/gui/not_burning_icon.png")
    }
}