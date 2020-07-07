package me.steven.indrev.gui.widgets

import io.github.cottonmc.cotton.gui.client.ScreenDrawing
import io.github.cottonmc.cotton.gui.widget.WButton
import me.steven.indrev.components.InventoryController
import me.steven.indrev.gui.controllers.wrench.WrenchController
import net.minecraft.client.gui.DrawableHelper
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.StringRenderable
import net.minecraft.text.TranslatableText
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier

class MachineSideDisplayWidget(private val identifier: Identifier, private val side: WrenchController.MachineSide, var mode: InventoryController.Mode) : WButton() {
    init {
        this.setSize(16, 16)
    }

    override fun setSize(x: Int, y: Int) {
        this.width = x
        this.height = y
    }

    override fun paint(matrices: MatrixStack?, x: Int, y: Int, mouseX: Int, mouseY: Int) {
        ScreenDrawing.texturedRect(x, y, width, height, identifier, side.u1 / 16f, side.v1 / 16f, side.u2 / 16f, side.v2 / 16f, mode.rgb)
        if (isWithinBounds(mouseX, mouseY)) {
            DrawableHelper.fill(matrices, x, y, x + width, y + height, -2130706433)
        }
    }

    override fun addTooltip(tooltip: MutableList<StringRenderable>?) {
        tooltip?.add(TranslatableText("item.indrev.wrench.mode",
            TranslatableText("item.indrev.wrench.${mode.toString().toLowerCase()}").formatted(Formatting.WHITE))
            .formatted(Formatting.BLUE))
        super.addTooltip(tooltip)
    }
}