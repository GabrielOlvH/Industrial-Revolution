package me.steven.indrev.gui.widgets.machines

import io.github.cottonmc.cotton.gui.widget.TooltipBuilder
import io.github.cottonmc.cotton.gui.widget.WBar
import me.steven.indrev.utils.getShortEnergyDisplay
import me.steven.indrev.utils.identifier
import net.minecraft.text.LiteralText
import net.minecraft.text.TranslatableText
import net.minecraft.util.Formatting

class WEnergy : WBar(ENERGY_EMPTY, ENERGY_FULL, 0, 1, Direction.UP) {
    init {
        this.setSize(16, 64)
    }

    override fun addTooltip(information: TooltipBuilder?) {
        val energy = getShortEnergyDisplay(properties[0].toDouble())
        val maxEnergy = getShortEnergyDisplay(properties[1].toDouble())
        information?.add(TranslatableText("gui.widget.energy").formatted(Formatting.BLUE))
        information?.add(LiteralText("$energy / $maxEnergy LF"))

    }

    override fun canResize(): Boolean = false

    companion object {
        val ENERGY_EMPTY =
            identifier("textures/gui/widget_energy_empty.png")
        val ENERGY_FULL =
           identifier("textures/gui/widget_energy_full.png")
    }
}