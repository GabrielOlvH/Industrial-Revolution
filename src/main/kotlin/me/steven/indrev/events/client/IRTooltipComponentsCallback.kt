package me.steven.indrev.events.client

import me.steven.indrev.gui.tooltip.energy.EnergyTooltipComponent
import me.steven.indrev.gui.tooltip.energy.EnergyTooltipData
import me.steven.indrev.gui.tooltip.modular.ModularTooltipComponent
import me.steven.indrev.gui.tooltip.modular.ModularTooltipData
import me.steven.indrev.gui.tooltip.oredatacards.OreDataCardTooltipComponent
import me.steven.indrev.gui.tooltip.oredatacards.OreDataCardTooltipData
import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback
import net.minecraft.client.gui.tooltip.TooltipComponent
import net.minecraft.client.item.TooltipData

object IRTooltipComponentsCallback : TooltipComponentCallback {
    override fun getComponent(data: TooltipData?): TooltipComponent? {
        return when (data) {
            is ModularTooltipData -> ModularTooltipComponent(data)
            is EnergyTooltipData -> EnergyTooltipComponent(data)
            is OreDataCardTooltipData -> OreDataCardTooltipComponent(data)
            else -> null
        }
    }
}