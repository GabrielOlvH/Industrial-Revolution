package me.steven.indrev.gui.widgets.machines

import io.github.cottonmc.cotton.gui.widget.TooltipBuilder
import io.github.cottonmc.cotton.gui.widget.WBar
import me.steven.indrev.utils.getEnergyString
import me.steven.indrev.utils.identifier
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.LiteralText
import net.minecraft.text.TranslatableText
import net.minecraft.util.Formatting

class WCompactEnergy : WBar(ENERGY_EMPTY, ENERGY_FULL, 0, 1, Direction.UP) {
    init {
        this.setSize(18, 18)
    }

    override fun addTooltip(information: TooltipBuilder?) {
        val energy = getEnergyString(properties[0].toDouble())
        val maxEnergy = getEnergyString(properties[1].toDouble())
        information?.add(TranslatableText("gui.widget.energy").formatted(Formatting.BLUE))
        information?.add(LiteralText("$energy / $maxEnergy LF"))
    }

    override fun paint(matrices: MatrixStack?, x: Int, y: Int, mouseX: Int, mouseY: Int) {
        /*Scissors.push(x, y, width - 5, height)
        BackgroundPainter.VANILLA.paintBackground(matrices, x, y, this)
        Scissors.pop()
        setSize(18, 18)*/
        super.paint(matrices, x + 2, y + 3, mouseX, mouseY)
        /*setSize(56, 24)
        val energy = properties[0].toDouble()
        val maxEnergy = properties[1].toDouble()
        val percentage = (energy * 100 / maxEnergy).roundToInt()
        ScreenDrawing.drawString(matrices, LiteralText("$percentage%").styled { Style.EMPTY.withColor(0x1d68e0) }.asOrderedText(), HorizontalAlignment.CENTER, x + 5, y + 8, width, -1)*/
    }

    override fun canResize(): Boolean = false

    companion object {
        val ENERGY_EMPTY =
            identifier("textures/gui/power_icon.png")
        val ENERGY_FULL =
            identifier("textures/gui/energy_icon.png")
    }
}