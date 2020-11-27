package me.steven.indrev.gui.widgets.machines

import io.github.cottonmc.cotton.gui.client.ScreenDrawing
import io.github.cottonmc.cotton.gui.widget.TooltipBuilder
import io.github.cottonmc.cotton.gui.widget.WButton
import me.steven.indrev.gui.controllers.wrench.WrenchController
import me.steven.indrev.utils.TransferMode
import me.steven.indrev.utils.draw2Colors
import me.steven.indrev.utils.identifier
import net.minecraft.client.gui.DrawableHelper
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.LiteralText
import net.minecraft.text.TranslatableText
import net.minecraft.util.Formatting
import net.minecraft.util.math.Direction

class WMachineSideDisplay(
    private val side: WrenchController.MachineSide,
    private val direction: Direction,
    var mode: TransferMode
) : WButton() {
    init {
        this.setSize(16, 16)
    }

    override fun setSize(x: Int, y: Int) {
        this.width = x
        this.height = y
    }

    override fun paint(matrices: MatrixStack, x: Int, y: Int, mouseX: Int, mouseY: Int) {
        ScreenDrawing.texturedRect(x, y, width, height, TEXTURE_ID, side.u1 / 16f, side.v1 / 16f, side.u2 / 16f, side.v2 / 16f, -1)
        if (mode == TransferMode.INPUT_OUTPUT)
            draw2Colors(matrices, x, y, x + width, y + height, TransferMode.INPUT.rgb, TransferMode.OUTPUT.rgb)
        else if (mode != TransferMode.NONE)
            DrawableHelper.fill(matrices, x, y, x + width, y + height, mode.rgb.toInt())
        if (isWithinBounds(mouseX, mouseY))
            DrawableHelper.fill(matrices, x, y, x + width, y + height, -2130706433)
    }

    override fun addTooltip(tooltip: TooltipBuilder?) {
        val modeText = TranslatableText("item.indrev.wrench.mode",
            TranslatableText("item.indrev.wrench.${mode.toString().toLowerCase()}").formatted(Formatting.WHITE)
        ).formatted(Formatting.BLUE)
        val side = TranslatableText("item.indrev.wrench.side.${side.toString().toLowerCase()}")
            .append(LiteralText(" (")
                .append(TranslatableText("item.indrev.wrench.side.${direction.toString().toLowerCase()}"))
                .append(LiteralText(")"))).formatted(Formatting.WHITE)
        tooltip?.add(modeText, side)
        super.addTooltip(tooltip)
    }

    companion object {
        val TEXTURE_ID = identifier("textures/block/machine_block.png")
    }
}