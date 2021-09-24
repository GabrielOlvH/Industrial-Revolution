package me.steven.indrev.gui.tooltip.energy

import me.steven.indrev.gui.tooltip.CustomTooltipData
import net.minecraft.client.gui.tooltip.TooltipComponent

data class EnergyTooltipData(val energy: Long, val maxEnergy: Long) : CustomTooltipData {
    override fun toComponent(): TooltipComponent = EnergyTooltipComponent(this)
}