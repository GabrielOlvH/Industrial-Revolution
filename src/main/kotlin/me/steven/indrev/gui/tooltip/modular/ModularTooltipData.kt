package me.steven.indrev.gui.tooltip.modular

import me.steven.indrev.gui.tooltip.energy.EnergyTooltipData
import me.steven.indrev.tools.modular.Module

class ModularTooltipData(energy: Long, maxEnergy: Long, val modules: List<Module>, val levelProvider: (Module) -> Int) : EnergyTooltipData(energy, maxEnergy) {
}