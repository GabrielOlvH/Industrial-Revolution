package me.steven.indrev.gui.tooltip.energy

import me.steven.indrev.gui.tooltip.CustomTooltipData
import net.minecraft.client.gui.tooltip.TooltipComponent

data class EnergyTooltipData(val energy: Double, val maxEnergy: Double) : CustomTooltipData {
    override fun toComponent(): TooltipComponent = EnergyTooltipComponent(this)
}